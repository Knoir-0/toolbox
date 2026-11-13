package com.example.toolbox.ui.tools

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toolbox.ui.theme.ToolboxTheme
import kotlin.math.abs
import kotlin.math.sqrt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// =================================================================================================
// Input actions abstraction (keeps the UI previewable and decoupled from the ViewModel)
// =================================================================================================

/** All input the remote UI can emit. Implemented by [BluetoothRemoteViewModel] in production. */
interface RemoteInputActions {
  fun keyDown(keyCode: Int)
  fun keyUp()
  fun tapKey(keyCode: Int)
  fun consumerDown(key: ConsumerKey)
  fun consumerUp()
  fun typeText(text: String, backspaces: Int)
  fun moveMouse(dx: Float, dy: Float)
  fun scroll(amount: Float)
  fun mouseButton(button: Int, pressed: Boolean)
  fun mouseClick()
}

private class ViewModelRemoteActions(private val vm: BluetoothRemoteViewModel) : RemoteInputActions {
  override fun keyDown(keyCode: Int) = vm.keyDown(keyCode)
  override fun keyUp() = vm.keyUp()
  override fun tapKey(keyCode: Int) = vm.tapKey(keyCode)
  override fun consumerDown(key: ConsumerKey) = vm.consumerDown(key)
  override fun consumerUp() = vm.consumerUp()
  override fun typeText(text: String, backspaces: Int) = vm.typeText(text, backspaces)
  override fun moveMouse(dx: Float, dy: Float) = vm.moveMouse(dx, dy)
  override fun scroll(amount: Float) = vm.scroll(amount)
  override fun mouseButton(button: Int, pressed: Boolean) = vm.mouseButton(button, pressed)
  override fun mouseClick() = vm.mouseClick()
}

private object NoOpRemoteActions : RemoteInputActions {
  override fun keyDown(keyCode: Int) = Unit
  override fun keyUp() = Unit
  override fun tapKey(keyCode: Int) = Unit
  override fun consumerDown(key: ConsumerKey) = Unit
  override fun consumerUp() = Unit
  override fun typeText(text: String, backspaces: Int) = Unit
  override fun moveMouse(dx: Float, dy: Float) = Unit
  override fun scroll(amount: Float) = Unit
  override fun mouseButton(button: Int, pressed: Boolean) = Unit
  override fun mouseClick() = Unit
}

/** Runtime permissions needed for discovery, pairing and acting as a HID device. */
internal fun requiredBluetoothPermissions(): Array<String> =
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
      Manifest.permission.BLUETOOTH_CONNECT,
      Manifest.permission.BLUETOOTH_SCAN,
      Manifest.permission.BLUETOOTH_ADVERTISE,
    )
  } else {
    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
  }

internal fun hasAllPermissions(context: Context): Boolean =
  requiredBluetoothPermissions().all {
    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
  }

// =================================================================================================
// Entry point
// =================================================================================================

@Composable
fun BluetoothRemoteScreen(modifier: Modifier = Modifier) {
  if (LocalInspectionMode.current) {
    DeviceListContent(
      state = BluetoothRemoteUiState(
        isBluetoothEnabled = true,
        isHidRegistered = true,
        devices = previewDevices,
      ),
      hasPermissions = true,
      onRequestPermissions = {},
      onEnableBluetooth = {},
      onMakeDiscoverable = {},
      onScan = {},
      onStopScan = {},
      onConnect = {},
      onCancelConnect = {},
      onDismissMessage = {},
      modifier = modifier,
    )
    return
  }

  val context = LocalContext.current
  val application = context.applicationContext as Application
  val viewModel: BluetoothRemoteViewModel = viewModel { BluetoothRemoteViewModel(application) }
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val actions = remember(viewModel) { ViewModelRemoteActions(viewModel) }

  var hasPermissions by remember { mutableStateOf(hasAllPermissions(context)) }
  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions(),
  ) { results ->
    hasPermissions = results.values.all { it } && hasAllPermissions(context)
  }
  val enableBluetoothLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.StartActivityForResult(),
  ) { viewModel.refreshDevices() }
  val discoverableLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.StartActivityForResult(),
  ) { }

  LaunchedEffect(hasPermissions) {
    if (hasPermissions) viewModel.start()
  }

  LifecycleResumeEffect(Unit) {
    hasPermissions = hasAllPermissions(context)
    if (hasPermissions) viewModel.refreshDevices()
    onPauseOrDispose { }
  }

  val connection = state.connection
  AnimatedContent(
    targetState = connection is RemoteConnection.Connected,
    transitionSpec = { fadeIn() togetherWith fadeOut() },
    modifier = modifier.fillMaxSize(),
    label = "remote_screen",
  ) { connected ->
    if (connected && connection is RemoteConnection.Connected) {
      RemoteControlContent(
        deviceName = connection.device.name,
        actions = actions,
        onDisconnect = viewModel::disconnect,
      )
    } else {
      DeviceListContent(
        state = state,
        hasPermissions = hasPermissions,
        onRequestPermissions = { permissionLauncher.launch(requiredBluetoothPermissions()) },
        onEnableBluetooth = {
          runCatching { enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)) }
        },
        onMakeDiscoverable = {
          runCatching {
            discoverableLauncher.launch(
              Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                .putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120),
            )
          }
        },
        onScan = viewModel::startScan,
        onStopScan = viewModel::stopScan,
        onConnect = { viewModel.connect(it.address) },
        onCancelConnect = viewModel::disconnect,
        onDismissMessage = viewModel::dismissMessage,
      )
    }
  }
}

// =================================================================================================
// Screen 1: device list
// =================================================================================================

@Composable
internal fun DeviceListContent(
  state: BluetoothRemoteUiState,
  hasPermissions: Boolean,
  onRequestPermissions: () -> Unit,
  onEnableBluetooth: () -> Unit,
  onMakeDiscoverable: () -> Unit,
  onScan: () -> Unit,
  onStopScan: () -> Unit,
  onConnect: (BtDeviceInfo) -> Unit,
  onCancelConnect: () -> Unit,
  onDismissMessage: () -> Unit,
  modifier: Modifier = Modifier,
  title: String = "Bluetooth Remote",
  icon: ImageVector = Icons.Default.SettingsRemote,
) {
  var showScanModal by rememberSaveable { mutableStateOf(false) }
  val connecting = state.connection as? RemoteConnection.Connecting
  val paired = state.devices.filter { it.isBonded }
  val nearby = state.devices.filterNot { it.isBonded }

  Box(
    modifier = modifier.fillMaxSize(),
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("bt_device_list"),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      item {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp),
          )
          Spacer(Modifier.width(12.dp))
          Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
              "Select a paired device or tap + to add a new one",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      val hasDeviceError = state.message != null &&
        state.messageDeviceAddress != null &&
        paired.any { it.address == state.messageDeviceAddress }
      if (state.message != null && !hasDeviceError) {
        item {
          MessageCard(message = state.message, onDismiss = onDismissMessage)
        }
      }

      when {
        state.support == BluetoothSupport.API_TOO_OLD -> item {
          StatusCard(
            icon = Icons.Default.BluetoothDisabled,
            title = "Android 9 or newer required",
            body = "Acting as a Bluetooth remote uses the HID Device profile, which was added in Android 9 (Pie).",
          )
        }

        state.support == BluetoothSupport.NO_ADAPTER -> item {
          StatusCard(
            icon = Icons.Default.BluetoothDisabled,
            title = "Bluetooth unavailable",
            body = "This device doesn't have a Bluetooth adapter.",
          )
        }

        !hasPermissions -> item {
          StatusCard(
            icon = Icons.Default.Bluetooth,
            title = "Allow Nearby devices",
            body = "Bluetooth permission is needed to find, pair with and control your TV.",
            actionLabel = "Grant permission",
            onAction = onRequestPermissions,
          )
        }

        !state.isBluetoothEnabled -> item {
          StatusCard(
            icon = Icons.Default.BluetoothDisabled,
            title = "Bluetooth is off",
            body = "Turn on Bluetooth to connect to a TV.",
            actionLabel = "Turn on",
            onAction = onEnableBluetooth,
          )
        }

        else -> {
          val isConnectingPaired = connecting != null && paired.any { it.address == connecting.device.address }
          if (connecting != null && !isConnectingPaired) {
            item {
              ConnectingCard(connecting = connecting, onCancel = onCancelConnect)
            }
          }

          item {
            Text(
              text = "Paired devices",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(top = 8.dp, start = 4.dp),
            )
          }

          if (paired.isEmpty()) {
            item {
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                  Box(
                    modifier = Modifier
                      .size(56.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      imageVector = Icons.Default.Tv,
                      contentDescription = null,
                      modifier = Modifier.size(28.dp),
                      tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                  }
                  Text(
                    text = "No paired devices",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                  )
                  Text(
                    text = "Tap the + button below to scan for and connect an Android TV or media device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                  )
                }
              }
            }
          } else {
            items(paired, key = { "paired_${it.address}" }) { device ->
              DeviceRow(
                device = device,
                connecting = if (connecting?.device?.address == device.address) connecting else null,
                errorMessage = if (state.messageDeviceAddress == device.address) state.message else null,
                enabled = connecting == null,
                onClick = { onConnect(device) },
                onCancelConnect = onCancelConnect,
                onDismissError = onDismissMessage,
              )
            }
          }
        }
      }
    }

    // Single + button at the bottom right (circular, floating above the scrollable list)
    FloatingActionButton(
      onClick = {
        when {
          !hasPermissions -> onRequestPermissions()
          !state.isBluetoothEnabled -> onEnableBluetooth()
          else -> showScanModal = true
        }
      },
      shape = CircleShape,
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onPrimary,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 16.dp, bottom = 16.dp)
        .testTag("bt_add_device_fab"),
    ) {
      Icon(Icons.Default.Add, contentDescription = "Add device", modifier = Modifier.size(28.dp))
    }
  }

  // Modal that scans for bluetooth devices
  if (showScanModal) {
    ScanDeviceModal(
      state = state,
      nearbyDevices = nearby,
      connecting = connecting,
      onScan = onScan,
      onStopScan = onStopScan,
      onMakeDiscoverable = onMakeDiscoverable,
      onConnect = { device ->
        onConnect(device)
        showScanModal = false
      },
      onCancelConnect = onCancelConnect,
      onDismissMessage = onDismissMessage,
      onDismiss = {
        showScanModal = false
        onStopScan()
      },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScanDeviceModal(
  state: BluetoothRemoteUiState,
  nearbyDevices: List<BtDeviceInfo>,
  connecting: RemoteConnection.Connecting?,
  onScan: () -> Unit,
  onStopScan: () -> Unit,
  onMakeDiscoverable: () -> Unit,
  onConnect: (BtDeviceInfo) -> Unit,
  onCancelConnect: () -> Unit,
  onDismissMessage: () -> Unit,
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  LaunchedEffect(Unit) {
    onScan()
  }

  DisposableEffect(Unit) {
    onDispose {
      onStopScan()
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surfaceContainer,
    dragHandle = {
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
      ) {
        BottomSheetDefaults.DragHandle()
      }
    },
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp),
      contentPadding = PaddingValues(bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Add new device",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "Make sure your TV or accessory is in pairing mode",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          if (state.isScanning) {
            OutlinedButton(
              onClick = onStopScan,
              modifier = Modifier.weight(1f),
            ) {
              CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
              Spacer(Modifier.width(8.dp))
              Text("Stop")
            }
          } else {
            Button(
              onClick = onScan,
              modifier = Modifier
                .weight(1f)
                .testTag("bt_scan_button"),
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(8.dp))
              Text("Scan again")
            }
          }
          FilledTonalButton(
            onClick = onMakeDiscoverable,
            modifier = Modifier.weight(1f),
          ) {
            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Discoverable")
          }
        }
      }

      item { PairingTipCard() }

      val isConnectingNearby = connecting != null && nearbyDevices.any { it.address == connecting.device.address }
      if (connecting != null && !isConnectingNearby) {
        item {
          ConnectingCard(connecting = connecting, onCancel = onCancelConnect)
        }
      }

      item {
        Text(
          text = "Nearby devices",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(top = 4.dp, start = 4.dp),
        )
      }

      if (nearbyDevices.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center,
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              if (state.isScanning) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 2.5.dp)
                Spacer(Modifier.height(4.dp))
                Text(
                  "Searching for nearby Bluetooth devices…",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              } else {
                Icon(
                  Icons.Default.Devices,
                  contentDescription = null,
                  modifier = Modifier.size(36.dp),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                  "No nearby devices found",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }
      } else {
        items(nearbyDevices, key = { "modal_nearby_${it.address}" }) { device ->
          DeviceRow(
            device = device,
            connecting = if (connecting?.device?.address == device.address) connecting else null,
            errorMessage = if (state.messageDeviceAddress == device.address) state.message else null,
            enabled = connecting == null,
            onClick = { onConnect(device) },
            onCancelConnect = onCancelConnect,
            onDismissError = onDismissMessage,
          )
        }
      }
    }
  }
}

@Composable
private fun DeviceRow(
  device: BtDeviceInfo,
  connecting: RemoteConnection.Connecting? = null,
  errorMessage: String? = null,
  enabled: Boolean = true,
  onClick: () -> Unit = {},
  onCancelConnect: () -> Unit = {},
  onDismissError: () -> Unit = {},
) {
  val isConnecting = connecting != null
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = when {
        errorMessage != null -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
        isConnecting -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surfaceContainerLow
      },
    ),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("bt_device_${device.address}"),
  ) {
    Column {
      ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (connecting != null || errorMessage != null) 0.dp else 16.dp, bottomEnd = if (connecting != null || errorMessage != null) 0.dp else 16.dp))
          .clickable(enabled = enabled && !isConnecting, onClick = onClick),
        leadingContent = {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(
                if (device.kind == BtDeviceKind.TV) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
              ),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = device.kind.icon(),
              contentDescription = null,
              tint = if (device.kind == BtDeviceKind.TV) MaterialTheme.colorScheme.onPrimaryContainer
              else MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        },
        headlineContent = { Text(device.name, fontWeight = FontWeight.Medium) },
        supportingContent = {
          val parts = buildList {
            add(if (device.isBonded) "Paired" else "Not paired")
            device.rssi?.let { add(signalLabel(it)) }
            add(device.address)
          }
          Text(parts.joinToString(" • "), style = MaterialTheme.typography.bodySmall)
        },
        trailingContent = {
          if (isConnecting) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
          } else {
            Icon(Icons.Default.ChevronRight, contentDescription = "Connect")
          }
        },
      )

      if (connecting != null) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
          Spacer(Modifier.width(12.dp))
          Column(Modifier.weight(1f)) {
            Text(
              text = if (connecting.pairing) "Pairing with ${connecting.device.name}…" else "Connecting to ${connecting.device.name}…",
              style = MaterialTheme.typography.titleSmall,
            )
            Text(
              text = if (connecting.pairing) "Accept the pairing request on both devices." else "This can take a few seconds.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          TextButton(onClick = onCancelConnect) {
            Text("Cancel")
          }
        }
      }

      if (errorMessage != null) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f),
          )
          IconButton(onClick = onDismissError) {
            Icon(
              Icons.Default.Close,
              contentDescription = "Dismiss",
              tint = MaterialTheme.colorScheme.onErrorContainer,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ConnectingCard(connecting: RemoteConnection.Connecting, onCancel: () -> Unit) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
      Spacer(Modifier.width(16.dp))
      Column(Modifier.weight(1f)) {
        Text(
          if (connecting.pairing) "Pairing with ${connecting.device.name}…" else "Connecting to ${connecting.device.name}…",
          style = MaterialTheme.typography.titleSmall,
        )
        Text(
          if (connecting.pairing) "Accept the pairing request on both devices." else "This can take a few seconds.",
          style = MaterialTheme.typography.bodySmall,
        )
      }
      TextButton(onClick = onCancel) { Text("Cancel") }
    }
  }
}

@Composable
private fun PairingTipCard() {
  var expanded by rememberSaveable { mutableStateOf(false) }
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    modifier = Modifier
      .fillMaxWidth()
      .clip(CardDefaults.shape)
      .clickable { expanded = !expanded },
  ) {
    Column(Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text("Connecting to a TV for the first time?", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        Icon(
          if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
          contentDescription = if (expanded) "Collapse" else "Expand",
        )
      }
      AnimatedVisibility(visible = expanded) {
        Text(
          text = "1. Tap Discoverable so the TV can see this phone.\n" +
            "2. On the TV, open Settings → Remotes & Accessories → Pair remote or accessory.\n" +
            "3. Select this phone and confirm the pairing code.\n\n" +
            "The remote opens automatically once the TV connects. Next time, just tap the TV in Paired devices.",
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.padding(top = 8.dp),
        )
      }
    }
  }
}

@Composable
private fun StatusCard(
  icon: ImageVector,
  title: String,
  body: String,
  actionLabel: String? = null,
  onAction: () -> Unit = {},
) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Icon(icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
      Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
      Text(body, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
      if (actionLabel != null) {
        Spacer(Modifier.height(4.dp))
        Button(onClick = onAction) { Text(actionLabel) }
      }
    }
  }
}

@Composable
private fun MessageCard(message: String, onDismiss: () -> Unit) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
      Text(
        message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier.weight(1f),
      )
      IconButton(onClick = onDismiss) {
        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onErrorContainer)
      }
    }
  }
}

private fun BtDeviceKind.icon(): ImageVector = when (this) {
  BtDeviceKind.TV -> Icons.Default.Tv
  BtDeviceKind.COMPUTER -> Icons.Default.Computer
  BtDeviceKind.PHONE -> Icons.Default.PhoneAndroid
  BtDeviceKind.AUDIO -> Icons.Default.Headphones
  BtDeviceKind.OTHER -> Icons.Default.Devices
}

private fun signalLabel(rssi: Int): String = when {
  rssi >= -60 -> "Strong signal"
  rssi >= -75 -> "Good signal"
  else -> "Weak signal"
}

// =================================================================================================
// Screen 2: remote control
// =================================================================================================

private enum class PointerMode { TOUCHPAD, GYRO }

@Composable
internal fun RemoteControlContent(
  deviceName: String,
  actions: RemoteInputActions,
  onDisconnect: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var keyboardOpen by rememberSaveable { mutableStateOf(false) }
  var mouseMode by rememberSaveable { mutableStateOf(false) }

  BackHandler {
    when {
      keyboardOpen -> keyboardOpen = false
      mouseMode -> mouseMode = false
      else -> onDisconnect()
    }
  }

  // Keep the display awake while the remote is in use.
  if (!LocalInspectionMode.current) {
    val view = LocalView.current
    DisposableEffect(view) {
      view.keepScreenOn = true
      onDispose { view.keepScreenOn = false }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState(), enabled = !mouseMode)
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("bt_remote_view"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // ---- Header ----
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      Box(
        Modifier
          .size(10.dp)
          .clip(CircleShape)
          .background(Color(0xFF34A853)),
      )
      Spacer(Modifier.width(8.dp))
      Column(Modifier.weight(1f)) {
        Text(deviceName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
        Text("Connected", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      IconButton(onClick = onDisconnect, modifier = Modifier.testTag("bt_disconnect")) {
        Icon(Icons.Default.LinkOff, contentDescription = "Disconnect")
      }
    }

    // ---- Top row: power / keyboard / mouse ----
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      ConsumerButton(
        key = ConsumerKey.POWER,
        icon = Icons.Default.PowerSettingsNew,
        label = "Power",
        actions = actions,
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier.testTag("bt_power"),
      )
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        FilledTonalIconToggleButton(
          checked = keyboardOpen,
          onCheckedChange = { keyboardOpen = it },
          modifier = Modifier
            .size(56.dp)
            .testTag("bt_keyboard_toggle"),
        ) {
          Icon(Icons.Default.Keyboard, contentDescription = "Keyboard")
        }
        FilledTonalIconToggleButton(
          checked = mouseMode,
          onCheckedChange = { mouseMode = it },
          modifier = Modifier
            .size(56.dp)
            .testTag("bt_mouse_toggle"),
        ) {
          Icon(Icons.Default.Mouse, contentDescription = "Mouse cursor")
        }
      }
    }

    AnimatedVisibility(visible = keyboardOpen) {
      KeyboardPanel(actions = actions, onClose = { keyboardOpen = false })
    }

    // ---- Main control surface ----
    AnimatedContent(
      targetState = mouseMode,
      transitionSpec = { fadeIn() togetherWith fadeOut() },
      label = "remote_main_surface",
    ) { mouse ->
      if (mouse) {
        MousePanel(actions = actions)
      } else {
        DPad(actions = actions)
      }
    }

    // ---- Navigation row ----
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      ConsumerButton(
        key = ConsumerKey.BACK,
        icon = Icons.AutoMirrored.Filled.ArrowBack,
        label = "Back",
        actions = actions,
        size = 64.dp,
        modifier = Modifier.testTag("bt_nav_back"),
      )
      ConsumerButton(
        key = ConsumerKey.HOME,
        icon = Icons.Default.Home,
        label = "Home",
        actions = actions,
        size = 64.dp,
        modifier = Modifier.testTag("bt_nav_home"),
      )
      ConsumerButton(
        key = ConsumerKey.MENU,
        icon = Icons.Default.Menu,
        label = "Menu",
        actions = actions,
        size = 64.dp,
        modifier = Modifier.testTag("bt_nav_menu"),
      )
    }

    // ---- Media bar (rewind, play/pause, fast forward) ----
    MediaBar(actions = actions)

    // ---- Volume bar (volume down, mute, volume up) ----
    VolumeBar(actions = actions)
    Spacer(Modifier.height(8.dp))
  }
}

// ---- D-pad ----

internal fun dpadKeyForOffset(offset: Offset, sizePx: Float): ConsumerKey? {
  val cx = sizePx / 2f
  val cy = sizePx / 2f
  val dx = offset.x - cx
  val dy = offset.y - cy
  val dist = sqrt(dx * dx + dy * dy)
  val radius = sizePx / 2f
  if (dist > radius) return null

  // 96dp out of 248dp is ~0.387 ratio of diameter (or 48dp radius)
  val centerRadius = radius * (96f / 248f)
  if (dist <= centerRadius) {
    return ConsumerKey.DPAD_CENTER
  }

  return if (abs(dx) <= abs(dy)) {
    if (dy < 0f) ConsumerKey.DPAD_UP else ConsumerKey.DPAD_DOWN
  } else {
    if (dx < 0f) ConsumerKey.DPAD_LEFT else ConsumerKey.DPAD_RIGHT
  }
}

@Composable
private fun DPad(actions: RemoteInputActions, modifier: Modifier = Modifier) {
  val ringColor = MaterialTheme.colorScheme.surfaceContainerHigh
  val haptics = LocalHapticFeedback.current
  val coroutineScope = rememberCoroutineScope()
  val currentActions by rememberUpdatedState(actions)
  var activeKey by remember { mutableStateOf<ConsumerKey?>(null) }

  Box(
    modifier = modifier
      .size(248.dp)
      .clip(CircleShape)
      .background(ringColor)
      .testTag("bt_dpad")
      .pointerInput(Unit) {
        awaitEachGesture {
          val down = awaitFirstDown(requireUnconsumed = false)
          down.consume()
          val sizePx = size.width.toFloat()
          val initialKey = dpadKeyForOffset(down.position, sizePx)
          if (initialKey != null) {
            var currentKey: ConsumerKey? = initialKey
            activeKey = initialKey
            haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
            currentActions.consumerDown(initialKey)
            val pressStartTime = System.currentTimeMillis()
            var isRepeating = false

            fun startRepeatIfDirectional(key: ConsumerKey) =
              if (key != ConsumerKey.DPAD_CENTER) {
                coroutineScope.launch {
                  delay(350L)
                  isRepeating = true
                  while (isActive) {
                    currentActions.consumerUp()
                    delay(30L)
                    if (!isActive) break
                    currentActions.consumerDown(key)
                    haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    delay(100L)
                  }
                }
              } else {
                null
              }

            var repeatJob = startRepeatIfDirectional(initialKey)

            while (true) {
              val event = awaitPointerEvent()
              val change = event.changes.firstOrNull { it.id == down.id } ?: break
              if (!change.pressed) {
                change.consume()
                break
              }
              change.consume()
              val newKey = dpadKeyForOffset(change.position, sizePx)
              if (newKey != currentKey) {
                repeatJob?.cancel()
                repeatJob = null
                if (currentKey != null) {
                  currentActions.consumerUp()
                }
                currentKey = newKey
                activeKey = newKey
                if (newKey != null) {
                  haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                  currentActions.consumerDown(newKey)
                  repeatJob = startRepeatIfDirectional(newKey)
                }
              }
            }

            repeatJob?.cancel()

            if (currentKey != null) {
              val elapsed = System.currentTimeMillis() - pressStartTime
              if (!isRepeating && elapsed < 45L) {
                coroutineScope.launch {
                  delay(45L - elapsed)
                  activeKey = null
                  currentActions.consumerUp()
                }
              } else {
                activeKey = null
                currentActions.consumerUp()
              }
            } else {
              activeKey = null
            }
          }
        }
      },
    contentAlignment = Alignment.Center,
  ) {
    DPadArrow(
      icon = Icons.Default.KeyboardArrowUp,
      label = "Up",
      key = ConsumerKey.DPAD_UP,
      isPressed = activeKey == ConsumerKey.DPAD_UP,
      actions = actions,
      alignment = Alignment.TopCenter,
      tag = "bt_dpad_up",
    )
    DPadArrow(
      icon = Icons.Default.KeyboardArrowDown,
      label = "Down",
      key = ConsumerKey.DPAD_DOWN,
      isPressed = activeKey == ConsumerKey.DPAD_DOWN,
      actions = actions,
      alignment = Alignment.BottomCenter,
      tag = "bt_dpad_down",
    )
    DPadArrow(
      icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
      label = "Left",
      key = ConsumerKey.DPAD_LEFT,
      isPressed = activeKey == ConsumerKey.DPAD_LEFT,
      actions = actions,
      alignment = Alignment.CenterStart,
      tag = "bt_dpad_left",
    )
    DPadArrow(
      icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
      label = "Right",
      key = ConsumerKey.DPAD_RIGHT,
      isPressed = activeKey == ConsumerKey.DPAD_RIGHT,
      actions = actions,
      alignment = Alignment.CenterEnd,
      tag = "bt_dpad_right",
    )
    DPadCenterButton(
      isPressed = activeKey == ConsumerKey.DPAD_CENTER,
      actions = actions,
    )
  }
}

@Composable
private fun BoxScope.DPadArrow(
  icon: ImageVector,
  label: String,
  key: ConsumerKey,
  isPressed: Boolean,
  actions: RemoteInputActions,
  alignment: Alignment,
  tag: String,
) {
  val contentColor = MaterialTheme.colorScheme.onSurface
  val pressedOverlay = contentColor.copy(alpha = 0.16f)
  Box(
    modifier = Modifier
      .align(alignment)
      .padding(4.dp)
      .size(72.dp)
      .clip(CircleShape)
      .background(if (isPressed) pressedOverlay else Color.Transparent)
      .semantics {
        role = Role.Button
        contentDescription = label
        onClick {
          actions.consumerDown(key)
          actions.consumerUp()
          true
        }
      }
      .testTag(tag),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = contentColor,
      modifier = Modifier.size(40.dp),
    )
  }
}

@Composable
private fun DPadCenterButton(
  isPressed: Boolean,
  actions: RemoteInputActions,
) {
  val containerColor = MaterialTheme.colorScheme.primaryContainer
  val contentColor = MaterialTheme.colorScheme.onPrimaryContainer
  val pressedOverlay = contentColor.copy(alpha = 0.16f)
  Box(
    modifier = Modifier
      .size(96.dp)
      .clip(CircleShape)
      .background(containerColor)
      .semantics {
        role = Role.Button
        contentDescription = "OK"
        onClick {
          actions.consumerDown(ConsumerKey.DPAD_CENTER)
          actions.consumerUp()
          true
        }
      }
      .testTag("bt_dpad_ok"),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(if (isPressed) pressedOverlay else Color.Transparent),
      contentAlignment = Alignment.Center,
    ) {
      Box(
        modifier = Modifier
          .size(20.dp)
          .clip(CircleShape)
          .background(contentColor),
      )
    }
  }
}

// ---- Media bar ----

@Composable
private fun MediaBar(
  actions: RemoteInputActions,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(32.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    modifier = modifier.testTag("bt_media_bar"),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      PressableButton(
        onPress = { actions.consumerDown(ConsumerKey.REWIND) },
        onRelease = { actions.consumerUp() },
        contentDescription = "Rewind",
        size = 64.dp,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        repeatOnHold = true,
        modifier = Modifier.testTag("bt_media_rewind"),
      ) {
        Icon(Icons.Default.FastRewind, contentDescription = null)
      }
      PressableButton(
        onPress = { actions.consumerDown(ConsumerKey.PLAY_PAUSE) },
        onRelease = { actions.consumerUp() },
        contentDescription = "Play/Pause",
        size = 64.dp,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag("bt_media_play_pause"),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
          Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(20.dp))
        }
      }
      PressableButton(
        onPress = { actions.consumerDown(ConsumerKey.FAST_FORWARD) },
        onRelease = { actions.consumerUp() },
        contentDescription = "Fast forward",
        size = 64.dp,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        repeatOnHold = true,
        modifier = Modifier.testTag("bt_media_fast_forward"),
      ) {
        Icon(Icons.Default.FastForward, contentDescription = null)
      }
    }
  }
}

// ---- Volume bar ----

@Composable
private fun VolumeBar(
  actions: RemoteInputActions,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(32.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    modifier = modifier.testTag("bt_volume_bar"),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      PressableButton(
        onPress = { actions.consumerDown(ConsumerKey.VOLUME_DOWN) },
        onRelease = { actions.consumerUp() },
        contentDescription = "Volume down",
        size = 64.dp,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        repeatOnHold = true,
        modifier = Modifier.testTag("bt_volume_down"),
      ) {
        Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = null)
      }
      PressableButton(
        onPress = { actions.consumerDown(ConsumerKey.MUTE) },
        onRelease = { actions.consumerUp() },
        contentDescription = "Mute",
        size = 64.dp,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag("bt_volume_mute"),
      ) {
        Icon(Icons.AutoMirrored.Filled.VolumeOff, contentDescription = null)
      }
      PressableButton(
        onPress = { actions.consumerDown(ConsumerKey.VOLUME_UP) },
        onRelease = { actions.consumerUp() },
        contentDescription = "Volume up",
        size = 64.dp,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        repeatOnHold = true,
        modifier = Modifier.testTag("bt_volume_up"),
      ) {
        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
      }
    }
  }
}

// ---- Keyboard ----

@Composable
private fun KeyboardPanel(actions: RemoteInputActions, onClose: () -> Unit) {
  var value by remember { mutableStateOf(TextFieldValue("")) }
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  LaunchedEffect(Unit) {
    runCatching { focusRequester.requestFocus() }
    keyboardController?.show()
  }

  Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    OutlinedTextField(
      value = value,
      onValueChange = { newValue ->
        val (backspaces, added) = HidKeyMap.diff(value.text, newValue.text)
        actions.typeText(added, backspaces)
        value = newValue
      },
      label = { Text("Type on TV") },
      placeholder = { Text("Text is sent as you type") },
      singleLine = true,
      keyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        autoCorrectEnabled = false,
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Send,
      ),
      keyboardActions = KeyboardActions(
        onSend = {
          actions.tapKey(HidKey.ENTER)
          value = TextFieldValue("")
        },
      ),
      trailingIcon = {
        IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = "Close keyboard") }
      },
      modifier = Modifier
        .fillMaxWidth()
        .focusRequester(focusRequester)
        .testTag("bt_keyboard_field"),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
      OutlinedButton(
        onClick = {
          // Delete on the TV; mirror it locally so live-typing diffs stay in sync.
          actions.tapKey(HidKey.BACKSPACE)
          if (value.text.isNotEmpty()) {
            val trimmed = value.text.dropLast(1)
            value = TextFieldValue(trimmed, selection = androidx.compose.ui.text.TextRange(trimmed.length))
          }
        },
        modifier = Modifier.weight(1f),
      ) {
        Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text("Delete")
      }
      OutlinedButton(
        onClick = { value = TextFieldValue("") },
        modifier = Modifier.weight(1f),
      ) { Text("Clear field") }
      Button(
        onClick = {
          actions.tapKey(HidKey.ENTER)
          value = TextFieldValue("")
        },
        modifier = Modifier.weight(1f),
      ) {
        Icon(Icons.AutoMirrored.Filled.KeyboardReturn, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text("Enter")
      }
    }
  }
}

// ---- Mouse ----

@Composable
private fun MousePanel(actions: RemoteInputActions, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val isPreview = LocalInspectionMode.current
  val hasGyro = remember {
    !isPreview && (context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager)
      ?.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null
  }
  var pointerMode by rememberSaveable { mutableStateOf(if (hasGyro) PointerMode.GYRO else PointerMode.TOUCHPAD) }
  var sensitivity by rememberSaveable { mutableFloatStateOf(1f) }
  var gyroPaused by rememberSaveable { mutableStateOf(false) }

  if (pointerMode == PointerMode.GYRO && hasGyro && !gyroPaused) {
    GyroPointerEffect(sensitivity = sensitivity, onMove = actions::moveMouse)
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
      SegmentedButton(
        selected = pointerMode == PointerMode.TOUCHPAD,
        onClick = { pointerMode = PointerMode.TOUCHPAD },
        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
        icon = { Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(18.dp)) },
      ) { Text("Touchpad") }
      SegmentedButton(
        selected = pointerMode == PointerMode.GYRO,
        onClick = { pointerMode = PointerMode.GYRO },
        enabled = hasGyro,
        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
        icon = { Icon(Icons.Default.ScreenRotation, contentDescription = null, modifier = Modifier.size(18.dp)) },
      ) { Text(if (hasGyro) "Gyroscope" else "No gyroscope") }
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(240.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      TrackSurface(
        pointerMode = pointerMode,
        gyroPaused = gyroPaused,
        sensitivity = sensitivity,
        actions = actions,
        modifier = Modifier.weight(1f),
      )
      ScrollStrip(actions = actions)
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
      MouseClickButton(
        label = "Click",
        actions = actions,
        modifier = Modifier.weight(2f),
      )
      if (pointerMode == PointerMode.GYRO) {
        FilledTonalIconToggleButton(
          checked = gyroPaused,
          onCheckedChange = { gyroPaused = it },
          modifier = Modifier.size(56.dp),
        ) {
          Icon(
            if (gyroPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
            contentDescription = if (gyroPaused) "Resume pointer" else "Pause pointer",
          )
        }
      }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      Text("Speed", style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(48.dp))
      SlimDotSlider(
        value = sensitivity,
        onValueChange = { sensitivity = it },
        valueRange = 0.3f..3f,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun TrackSurface(
  pointerMode: PointerMode,
  gyroPaused: Boolean,
  sensitivity: Float,
  actions: RemoteInputActions,
  modifier: Modifier = Modifier,
) {
  val currentSensitivity by rememberUpdatedState(sensitivity)
  val haptics = LocalHapticFeedback.current
  Surface(
    shape = RoundedCornerShape(24.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHighest,
    modifier = modifier
      .fillMaxHeight()
      .testTag("bt_touchpad")
      .pointerInput(pointerMode) {
        detectTapGestures(
          onTap = {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            actions.mouseClick()
          },
        )
      }
      .pointerInput(pointerMode) {
        if (pointerMode == PointerMode.TOUCHPAD) {
          detectDragGestures { change, dragAmount ->
            change.consume()
            actions.moveMouse(
              dragAmount.x * TOUCHPAD_SPEED * currentSensitivity,
              dragAmount.y * TOUCHPAD_SPEED * currentSensitivity,
            )
          }
        }
      },
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(16.dp),
    ) {
      Icon(
        if (pointerMode == PointerMode.GYRO) Icons.Default.ScreenRotation else Icons.Default.TouchApp,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(36.dp),
      )
      Spacer(Modifier.height(8.dp))
      Text(
        text = when {
          pointerMode == PointerMode.TOUCHPAD -> "Drag to move the cursor\nTap to click"
          gyroPaused -> "Pointer paused\nTap to click"
          else -> "Point your phone at the TV and move it to steer the cursor\nTap here to click"
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun ScrollStrip(actions: RemoteInputActions) {
  Surface(
    shape = RoundedCornerShape(24.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    modifier = Modifier
      .width(48.dp)
      .fillMaxHeight()
      .semantics { contentDescription = "Scroll" }
      .pointerInput(Unit) {
        detectVerticalDragGestures { change, dragAmount ->
          change.consume()
          // Dragging down scrolls content down (wheel negative), like a laptop touchpad.
          actions.scroll(-dragAmount / SCROLL_PX_PER_STEP)
        }
      },
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.padding(vertical = 12.dp),
    ) {
      Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
      Text("SCROLL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
private fun MouseClickButton(label: String, actions: RemoteInputActions, modifier: Modifier = Modifier) {
  PressableSurface(
    onPress = { actions.mouseButton(HidProtocol.MOUSE_BUTTON_LEFT, true) },
    onRelease = { actions.mouseButton(HidProtocol.MOUSE_BUTTON_LEFT, false) },
    contentDescription = label,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
    modifier = modifier.height(56.dp),
  ) {
    Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
  }
}

/** Streams gyroscope rotation into cursor movement while composed and the app is resumed. */
@Composable
private fun GyroPointerEffect(sensitivity: Float, onMove: (Float, Float) -> Unit) {
  val context = LocalContext.current
  val currentSensitivity by rememberUpdatedState(sensitivity)
  val currentOnMove by rememberUpdatedState(onMove)

  LifecycleResumeEffect(Unit) {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    val gyro = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    val gravitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
      ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val up = floatArrayOf(0f, 0f, 0f)
    var lastTimestamp = 0L

    val listener = object : SensorEventListener {
      override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
          Sensor.TYPE_GRAVITY -> event.values.copyInto(up, endIndex = 3)
          Sensor.TYPE_ACCELEROMETER -> for (i in 0..2) {
            // Low-pass filter to isolate gravity from hand movement.
            up[i] = up[i] * 0.9f + event.values[i] * 0.1f
          }
          Sensor.TYPE_GYROSCOPE -> {
            val ts = event.timestamp
            if (lastTimestamp != 0L) {
              val dt = ((ts - lastTimestamp) / 1_000_000_000f).coerceIn(0f, 0.1f)
              val yaw = GyroMouseMath.yawRate(
                event.values[0], event.values[1], event.values[2],
                up[0], up[1], up[2],
              )
              val (dx, dy) = GyroMouseMath.delta(yaw, event.values[0], dt, currentSensitivity)
              if (dx != 0f || dy != 0f) currentOnMove(dx, dy)
            }
            lastTimestamp = ts
          }
        }
      }

      override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    if (sensorManager != null && gyro != null) {
      gravitySensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
      sensorManager.registerListener(listener, gyro, SensorManager.SENSOR_DELAY_GAME)
    }
    onPauseOrDispose {
      sensorManager?.unregisterListener(listener)
    }
  }
}

// ---- Reusable press-and-hold buttons ----

@Composable
private fun ConsumerButton(
  key: ConsumerKey,
  icon: ImageVector,
  label: String,
  actions: RemoteInputActions,
  modifier: Modifier = Modifier,
  size: Dp = 56.dp,
  containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
  contentColor: Color = MaterialTheme.colorScheme.onSurface,
  repeatOnHold: Boolean = false,
) {
  PressableButton(
    onPress = { actions.consumerDown(key) },
    onRelease = { actions.consumerUp() },
    contentDescription = label,
    size = size,
    containerColor = containerColor,
    contentColor = contentColor,
    modifier = modifier,
    repeatOnHold = repeatOnHold,
  ) {
    Icon(icon, contentDescription = null)
  }
}

@Composable
internal fun PressableButton(
  onPress: () -> Unit,
  onRelease: () -> Unit,
  contentDescription: String,
  size: Dp,
  containerColor: Color,
  contentColor: Color,
  modifier: Modifier = Modifier,
  repeatOnHold: Boolean = false,
  content: @Composable RowScope.() -> Unit,
) {
  PressableSurface(
    onPress = onPress,
    onRelease = onRelease,
    contentDescription = contentDescription,
    shape = CircleShape,
    containerColor = containerColor,
    contentColor = contentColor,
    modifier = modifier.size(size),
    repeatOnHold = repeatOnHold,
    content = content,
  )
}

/**
 * A surface that reports press and release separately, with optional auto-repeat on hold,
 * safe touch consumption (preventing scroll interference), and minimum assertion duration.
 */
@Composable
internal fun PressableSurface(
  onPress: () -> Unit,
  onRelease: () -> Unit,
  contentDescription: String,
  shape: Shape,
  containerColor: Color,
  contentColor: Color,
  modifier: Modifier = Modifier,
  repeatOnHold: Boolean = false,
  content: @Composable RowScope.() -> Unit,
) {
  val haptics = LocalHapticFeedback.current
  val coroutineScope = rememberCoroutineScope()
  val currentOnPress by rememberUpdatedState(onPress)
  val currentOnRelease by rememberUpdatedState(onRelease)
  var pressed by remember { mutableStateOf(false) }
  val pressedOverlay = contentColor.copy(alpha = 0.16f)
  val interactionSource = remember { MutableInteractionSource() }

  Surface(
    shape = shape,
    color = containerColor,
    contentColor = contentColor,
    modifier = modifier
      .clip(shape)
      .semantics {
        role = Role.Button
        this.contentDescription = contentDescription
        onClick {
          currentOnPress()
          currentOnRelease()
          true
        }
      }
      .pointerInput(interactionSource, repeatOnHold) {
        awaitEachGesture {
          val down = awaitFirstDown(requireUnconsumed = false)
          down.consume()
          pressed = true
          haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
          currentOnPress()
          val pressStartTime = System.currentTimeMillis()

          var isRepeating = false
          val repeatJob = if (repeatOnHold) {
            coroutineScope.launch {
              delay(350L)
              isRepeating = true
              while (isActive) {
                currentOnRelease()
                delay(30L)
                if (!isActive) break
                currentOnPress()
                haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                delay(100L)
              }
            }
          } else null

          val marginPx = 48.dp.toPx()
          val minX = -marginPx
          val maxX = size.width + marginPx
          val minY = -marginPx
          val maxY = size.height + marginPx

          var cancelled = false
          while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) {
              change.consume()
              break
            }
            if (change.position.x !in minX..maxX || change.position.y !in minY..maxY) {
              cancelled = true
              break
            }
            change.consume()
          }

          repeatJob?.cancel()

          if (cancelled) {
            pressed = false
            currentOnRelease()
          } else {
            val elapsed = System.currentTimeMillis() - pressStartTime
            if (!isRepeating && elapsed < 45L) {
              coroutineScope.launch {
                delay(45L - elapsed)
                pressed = false
                currentOnRelease()
              }
            } else {
              pressed = false
              currentOnRelease()
            }
          }
        }
      },
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .background(if (pressed) pressedOverlay else Color.Transparent),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
      content = content,
    )
  }
}

private const val TOUCHPAD_SPEED = 1.6f
private const val SCROLL_PX_PER_STEP = 40f

// =================================================================================================
// Previews
// =================================================================================================

internal val previewDevices = listOf(
  BtDeviceInfo("AA:BB:CC:DD:EE:01", "Living Room TV", isBonded = true, kind = BtDeviceKind.TV),
  BtDeviceInfo("AA:BB:CC:DD:EE:02", "Pixel Buds Pro", isBonded = true, kind = BtDeviceKind.AUDIO),
  BtDeviceInfo("AA:BB:CC:DD:EE:03", "SHIELD", isBonded = false, kind = BtDeviceKind.TV, rssi = -58),
  BtDeviceInfo("AA:BB:CC:DD:EE:04", "Work Laptop", isBonded = false, kind = BtDeviceKind.COMPUTER, rssi = -80),
)

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun BluetoothDeviceListPreview() {
  ToolboxTheme {
    BluetoothRemoteScreen()
  }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun BluetoothRemoteControlPreview() {
  ToolboxTheme {
    Surface {
      RemoteControlContent(deviceName = "Living Room TV", actions = NoOpRemoteActions, onDisconnect = {})
    }
  }
}
