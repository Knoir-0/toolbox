package com.example.toolbox.ui.tools

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.toolbox.ui.theme.ToolboxTheme
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.delay

/**
 * Sections within the connected OBD-II Scanner dashboard.
 */
enum class Obd2ScannerTab(val title: String) {
  LIVE_GAUGES("Live Gauges"),
  TROUBLE_CODES("Fault Codes"),
  VEHICLE_INFO("Monitors & VIN"),
}

private fun hasBlePermissions(context: Context): Boolean {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) ==
      PackageManager.PERMISSION_GRANTED &&
      ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
      PackageManager.PERMISSION_GRANTED
  } else {
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
      PackageManager.PERMISSION_GRANTED
  }
}

private fun requiredBlePermissions(): Array<String> {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
      Manifest.permission.BLUETOOTH_SCAN,
      Manifest.permission.BLUETOOTH_CONNECT,
    )
  } else {
    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
  }
}

private fun obdSignalLabel(rssi: Int): String = when {
  rssi >= -60 -> "Strong signal ($rssi dBm)"
  rssi >= -75 -> "Good signal ($rssi dBm)"
  rssi >= -85 -> "Fair signal ($rssi dBm)"
  else -> "Weak signal ($rssi dBm)"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Obd2ScannerScreen(
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current
  val focusManager = LocalFocusManager.current
  val keyboardController = LocalSoftwareKeyboardController.current
  val controller = remember { Obd2ScannerController(context.applicationContext) }

  DisposableEffect(controller) {
    onDispose {
      controller.stopBleScan()
    }
  }

  val connectionState by controller.connectionState.collectAsState()
  val connectedDeviceName by controller.connectedDeviceName.collectAsState()
  val connectingDevice by controller.connectingDevice.collectAsState()
  val statusMessage by controller.statusMessage.collectAsState()
  val errorMessage by controller.errorMessage.collectAsState()
  val pairedDevices by controller.pairedDevices.collectAsState()
  val discoveredDevices by controller.discoveredDevices.collectAsState()
  val telemetry by controller.liveTelemetry.collectAsState()
  val activeDtcs by controller.activeDtcs.collectAsState()
  val monitorSummary by controller.monitorSummary.collectAsState()
  val vehicleInfo by controller.vehicleInfo.collectAsState()
  val isPollingLive by controller.isPollingLive.collectAsState()

  var hasPermissions by remember {
    mutableStateOf(if (isInspection) true else hasBlePermissions(context))
  }
  var isBluetoothOn by remember {
    mutableStateOf(if (isInspection) true else controller.isBluetoothEnabled())
  }
  val hasBtAdapter = remember {
    if (isInspection) true else controller.hasBluetoothAdapter()
  }

  var selectedTab by rememberSaveable { mutableStateOf(Obd2ScannerTab.LIVE_GAUGES) }
  var useImperialUnits by rememberSaveable { mutableStateOf(false) }
  var showClearCodesDialog by rememberSaveable { mutableStateOf(false) }

  // Top Toolbar Search state (matching Plants & Recipes tools)
  var isSearchActive by rememberSaveable { mutableStateOf(false) }
  var dtcSearchQuery by rememberSaveable { mutableStateOf("") }
  var selectedDtcCategoryName by rememberSaveable { mutableStateOf<String?>(null) }
  val selectedDtcCategory = remember(selectedDtcCategoryName) {
    selectedDtcCategoryName?.let { name ->
      DtcSystemFilter.entries.firstOrNull { it.name == name }
    }
  }
  val searchFocusRequester = remember { FocusRequester() }

  fun closeSearch() {
    isSearchActive = false
    dtcSearchQuery = ""
    selectedDtcCategoryName = null
    focusManager.clearFocus()
    keyboardController?.hide()
  }

  LaunchedEffect(isSearchActive) {
    if (isSearchActive) {
      delay(100)
      runCatching {
        searchFocusRequester.requestFocus()
        keyboardController?.show()
      }
    } else {
      keyboardController?.hide()
    }
  }

  val isConnected = connectionState == Obd2ConnectionState.CONNECTED_BLE ||
    connectionState == Obd2ConnectionState.CONNECTED_SIMULATOR

  BackHandler(enabled = isSearchActive || isConnected) {
    if (isSearchActive) {
      closeSearch()
    } else if (isConnected) {
      controller.disconnect()
    }
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions(),
  ) { grants ->
    hasPermissions = grants.values.all { it } && hasBlePermissions(context)
    if (hasPermissions) {
      isBluetoothOn = controller.isBluetoothEnabled()
      controller.refreshPairedDevices()
    }
  }

  val enableBluetoothLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult(),
  ) {
    isBluetoothOn = controller.isBluetoothEnabled()
    if (hasPermissions) {
      controller.refreshPairedDevices()
    }
  }

  LaunchedEffect(hasPermissions) {
    if (hasPermissions && !isInspection) {
      isBluetoothOn = controller.isBluetoothEnabled()
      controller.refreshPairedDevices()
    }
  }

  if (!isInspection) {
    LifecycleResumeEffect(Unit) {
      hasPermissions = hasBlePermissions(context)
      isBluetoothOn = controller.isBluetoothEnabled()
      if (hasPermissions) {
        controller.refreshPairedDevices()
      }
      onPauseOrDispose { }
    }
  }

  val matchingCatalogDtcs = remember(dtcSearchQuery, selectedDtcCategory) {
    Obd2DtcCatalog.search(
      query = dtcSearchQuery,
      categoryFilter = selectedDtcCategory,
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("obd2_scanner_screen"),
  ) {
    // ==================== TOP TOOLBAR WITH EXPANDING SEARCH ====================
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 1.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 68.dp),
        contentAlignment = Alignment.CenterStart,
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "OBD2 Scanner",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              val subtitleText = if (isConnected) {
                statusMessage
              } else {
                "Select a paired adapter or tap + to add one"
              }
              Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            if (isConnected) {
              // Metric / Imperial quick toggle pill
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    useImperialUnits = !useImperialUnits
                  }
                  .testTag("obd2_unit_toggle"),
              ) {
                Text(
                  text = if (useImperialUnits) "MPH • °F" else "KM/H • °C",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
              }

              IconButton(
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  controller.disconnect()
                },
                modifier = Modifier.testTag("obd2_disconnect_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.LinkOff,
                  contentDescription = "Disconnect adapter",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            IconButton(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isSearchActive = true
              },
              modifier = Modifier.testTag("toggle_search_bar_button"),
            ) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search DTC codes",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        // Animated Expanding Search Bar integrated into the Toolbar (Matches Plants & Recipes)
        androidx.compose.animation.AnimatedVisibility(
          visible = isSearchActive,
          enter = fadeIn(animationSpec = tween(220)) + expandHorizontally(
            animationSpec = spring(
              dampingRatio = Spring.DampingRatioNoBouncy,
              stiffness = Spring.StiffnessMediumLow,
            ),
            expandFrom = Alignment.End,
          ),
          exit = fadeOut(animationSpec = tween(180)) + shrinkHorizontally(
            animationSpec = tween(200),
            shrinkTowards = Alignment.End,
          ),
          modifier = Modifier.matchParentSize(),
        ) {
          Surface(
            modifier = Modifier
              .fillMaxSize()
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
              )
              .testTag("toolbar_search_bar"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
          ) {
            Row(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              IconButton(
                onClick = { closeSearch() },
                modifier = Modifier.testTag("close_search_button"),
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Close search",
                  tint = MaterialTheme.colorScheme.onSurface,
                )
              }

              TextField(
                value = dtcSearchQuery,
                onValueChange = { dtcSearchQuery = it },
                placeholder = {
                  Text(
                    text = "Search DTC codes (e.g. P0420, P0300) or symptoms...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                  color = MaterialTheme.colorScheme.onSurface,
                ),
                colors = TextFieldDefaults.colors(
                  focusedContainerColor = Color.Transparent,
                  unfocusedContainerColor = Color.Transparent,
                  disabledContainerColor = Color.Transparent,
                  focusedIndicatorColor = Color.Transparent,
                  unfocusedIndicatorColor = Color.Transparent,
                  disabledIndicatorColor = Color.Transparent,
                ),
                trailingIcon = {
                  if (dtcSearchQuery.isNotEmpty()) {
                    IconButton(
                      onClick = { dtcSearchQuery = "" },
                      modifier = Modifier.testTag("obd2_dtc_search_clear"),
                    ) {
                      Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                  }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier
                  .fillMaxWidth()
                  .focusRequester(searchFocusRequester)
                  .testTag("obd2_dtc_search_input"),
              )
            }
          }
        }
      }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

    // ==================== MAIN CONTENT + SEARCH OVERLAY ====================
    Box(
      modifier = Modifier
        .fillMaxSize()
        .weight(1f),
    ) {
      // Screen 1 (Bluetooth Device Connection Screen) <-> Screen 2 (Connected OBD2 Scanner)
      AnimatedContent(
        targetState = isConnected,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = Modifier.fillMaxSize(),
        label = "obd2_connection_screen",
      ) { connected ->
        if (connected) {
          Obd2ConnectedDashboardContent(
            selectedTab = selectedTab,
            onSelectTab = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              selectedTab = it
            },
            connectedDeviceName = connectedDeviceName,
            telemetry = telemetry,
            useImperial = useImperialUnits,
            isPolling = isPollingLive,
            activeDtcs = activeDtcs,
            monitorSummary = monitorSummary,
            vehicleInfo = vehicleInfo,
            onTogglePolling = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              controller.toggleLivePolling()
            },
            onResetPeaks = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              controller.resetTelemetryPeaks()
            },
            onScanCodes = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              controller.scanTroubleCodes()
            },
            onRequestClearCodes = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              showClearCodesDialog = true
            },
            onOpenDtcSearch = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              isSearchActive = true
            },
            onRefreshVehicleInfo = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              controller.refreshVehicleInfoAndMonitors()
            },
            onCopyVin = { vin ->
              if (vin.isNotBlank()) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                clipboard?.setPrimaryClip(ClipData.newPlainText("VIN", vin))
                Toast.makeText(context, "Copied VIN to clipboard", Toast.LENGTH_SHORT).show()
              }
            },
          )
        } else {
          Obd2DeviceListContent(
            hasBtAdapter = hasBtAdapter,
            hasPermissions = hasPermissions,
            isBluetoothEnabled = isBluetoothOn,
            isScanning = connectionState == Obd2ConnectionState.SCANNING,
            pairedDevices = pairedDevices,
            nearbyDevices = discoveredDevices,
            connectingDevice = connectingDevice,
            errorMessage = errorMessage,
            onRequestPermissions = {
              permissionLauncher.launch(requiredBlePermissions())
            },
            onEnableBluetooth = {
              runCatching {
                enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
              }
            },
            onScan = { controller.startBleScan() },
            onStopScan = { controller.stopBleScan() },
            onConnectDevice = { device ->
              controller.connectToBleDevice(device)
            },
            onStartSimulator = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              controller.startSimulatorMode()
            },
            onCancelConnect = { controller.disconnect() },
            onDismissError = { controller.dismissError() },
          )
        }
      }

      // Top Toolbar Search Overlay (Matches Plants & Recipes Catalog Search Overlay)
      androidx.compose.animation.AnimatedVisibility(
        visible = isSearchActive,
        enter = fadeIn(animationSpec = tween(200)) + expandVertically(expandFrom = Alignment.Top),
        exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(shrinkTowards = Alignment.Top),
        modifier = Modifier.fillMaxSize(),
      ) {
        Obd2DtcSearchOverlay(
          allCatalogCount = Obd2DtcCatalog.ALL_ENTRIES.size,
          query = dtcSearchQuery,
          selectedCategory = selectedDtcCategory,
          onSelectCategory = { category ->
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            selectedDtcCategoryName = if (selectedDtcCategory == category) null else category?.name
          },
          matchingDtcs = matchingCatalogDtcs,
        )
      }
    }
  }

  // Clear DTCs & Reset MIL Confirmation Dialog (DESIGN_LANGUAGE.md 5.2)
  if (showClearCodesDialog) {
    AlertDialog(
      onDismissRequest = { showClearCodesDialog = false },
      shape = RoundedCornerShape(28.dp),
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      tonalElevation = 6.dp,
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.DeleteSweep,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(24.dp),
            )
          }
          Column {
            Text(
              text = "Clear Trouble Codes",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "OBD-II Mode 04 • Reset Check Engine Light",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(22.dp),
              )
              Column {
                Text(
                  text = "${activeDtcs.size} fault code(s) will be erased",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = "Freeze frame data and emissions readiness monitors will also reset until the next drive cycle completes.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            controller.clearTroubleCodes()
            showClearCodesDialog = false
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.testTag("obd2_confirm_clear_codes"),
        ) {
          Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Clear MIL & Codes")
        }
      },
      dismissButton = {
        TextButton(
          onClick = { showClearCodesDialog = false },
          shape = RoundedCornerShape(12.dp),
        ) {
          Text("Cancel")
        }
      },
    )
  }
}

// =================================================================================================
// Screen 1: Bluetooth Device Connection Screen (Matching BluetoothRemoteScreen & BluetoothGamepadScreen)
// =================================================================================================

@Composable
internal fun Obd2DeviceListContent(
  hasBtAdapter: Boolean,
  hasPermissions: Boolean,
  isBluetoothEnabled: Boolean,
  isScanning: Boolean,
  pairedDevices: List<Obd2BleDevice>,
  nearbyDevices: List<Obd2BleDevice>,
  connectingDevice: Obd2BleDevice?,
  errorMessage: String?,
  onRequestPermissions: () -> Unit,
  onEnableBluetooth: () -> Unit,
  onScan: () -> Unit,
  onStopScan: () -> Unit,
  onConnectDevice: (Obd2BleDevice) -> Unit,
  onStartSimulator: () -> Unit,
  onCancelConnect: () -> Unit,
  onDismissError: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showScanModal by rememberSaveable { mutableStateOf(false) }

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
      if (errorMessage != null) {
        item {
          Obd2MessageCard(message = errorMessage, onDismiss = onDismissError)
        }
      }

      when {
        !hasBtAdapter -> item {
          Obd2StatusCard(
            icon = Icons.Default.BluetoothDisabled,
            title = "Bluetooth unavailable",
            body = "This device doesn't have a Bluetooth adapter. You can still test the scanner using the Demo Vehicle below.",
          )
        }

        !hasPermissions -> item {
          Obd2StatusCard(
            icon = Icons.Default.Bluetooth,
            title = "Allow Nearby devices",
            body = "Bluetooth permission is needed to discover and connect to your BLE OBD-II vehicle adapter.",
            actionLabel = "Grant permission",
            onAction = onRequestPermissions,
          )
        }

        !isBluetoothEnabled -> item {
          Obd2StatusCard(
            icon = Icons.Default.BluetoothDisabled,
            title = "Bluetooth is off",
            body = "Turn on Bluetooth to connect to an ELM327 BLE OBD-II adapter.",
            actionLabel = "Turn on",
            onAction = onEnableBluetooth,
          )
        }

        else -> {
          val isConnectingPaired = connectingDevice != null &&
            pairedDevices.any { it.address == connectingDevice.address }
          if (connectingDevice != null && !isConnectingPaired) {
            item {
              Obd2ConnectingCard(device = connectingDevice, onCancel = onCancelConnect)
            }
          }

          item {
            Text(
              text = "Paired devices",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
          }

          if (pairedDevices.isEmpty()) {
            item {
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 4.dp),
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
                      imageVector = Icons.Default.DirectionsCar,
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
                    text = "Tap the + button below to scan for and connect a BLE OBD-II adapter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                  )
                }
              }
            }
          } else {
            items(pairedDevices, key = { "paired_${it.address}" }) { device ->
              Obd2DeviceRow(
                device = device,
                isConnecting = connectingDevice?.address == device.address,
                enabled = connectingDevice == null,
                onClick = { onConnectDevice(device) },
                onCancelConnect = onCancelConnect,
              )
            }
          }
        }
      }

      // Demo Vehicle Simulator device item (always available for testing without a car)
      item {
        Text(
          text = "Offline simulator",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(top = 12.dp, start = 4.dp),
        )
      }

      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("obd2_start_simulator_button"),
        ) {
          ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .clickable(enabled = connectingDevice == null, onClick = onStartSimulator),
            leadingContent = {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Default.DirectionsCar,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
              }
            },
            headlineContent = {
              Text("Demo Vehicle (BLE Simulator)", fontWeight = FontWeight.Medium)
            },
            supportingContent = {
              Text(
                text = "Simulated CAN ECU • Live gauges, DTCs & readiness monitors",
                style = MaterialTheme.typography.bodySmall,
              )
            },
            trailingContent = {
              Icon(Icons.Default.ChevronRight, contentDescription = "Start Demo Vehicle")
            },
          )
        }
      }
    }

    // Single + button at the bottom right (matching BluetoothRemoteScreen & BluetoothGamepadScreen)
    FloatingActionButton(
      onClick = {
        when {
          !hasPermissions -> onRequestPermissions()
          !isBluetoothEnabled -> onEnableBluetooth()
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

  if (showScanModal) {
    ScanObdDeviceModal(
      isScanning = isScanning,
      nearbyDevices = nearbyDevices,
      connectingDevice = connectingDevice,
      onScan = onScan,
      onStopScan = onStopScan,
      onConnect = { device ->
        onConnectDevice(device)
        showScanModal = false
      },
      onCancelConnect = onCancelConnect,
      onDismiss = {
        showScanModal = false
        onStopScan()
      },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScanObdDeviceModal(
  isScanning: Boolean,
  nearbyDevices: List<Obd2BleDevice>,
  connectingDevice: Obd2BleDevice?,
  onScan: () -> Unit,
  onStopScan: () -> Unit,
  onConnect: (Obd2BleDevice) -> Unit,
  onCancelConnect: () -> Unit,
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var prioritizeObdOnly by rememberSaveable { mutableStateOf(false) }

  val filteredNearby = remember(nearbyDevices, prioritizeObdOnly) {
    if (prioritizeObdOnly) {
      nearbyDevices.filter { it.isLikelyObd }
    } else {
      nearbyDevices
    }
  }

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
              text = "Make sure your BLE OBD-II adapter is plugged in and ignition is ON",
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
          if (isScanning) {
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
            onClick = { prioritizeObdOnly = !prioritizeObdOnly },
            modifier = Modifier.weight(1f),
          ) {
            Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (prioritizeObdOnly) "OBD-II Only" else "All BLE")
          }
        }
      }

      item { Obd2PairingTipCard() }

      val isConnectingNearby = connectingDevice != null &&
        filteredNearby.any { it.address == connectingDevice.address }
      if (connectingDevice != null && !isConnectingNearby) {
        item {
          Obd2ConnectingCard(device = connectingDevice, onCancel = onCancelConnect)
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

      if (filteredNearby.isEmpty()) {
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
              if (isScanning) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 2.5.dp)
                Spacer(Modifier.height(4.dp))
                Text(
                  text = "Searching for nearby BLE OBD-II adapters…",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              } else {
                Icon(
                  imageVector = Icons.Default.Devices,
                  contentDescription = null,
                  modifier = Modifier.size(36.dp),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                  text = "No nearby devices found",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }
      } else {
        items(filteredNearby, key = { "modal_nearby_${it.address}" }) { device ->
          Obd2DeviceRow(
            device = device,
            isConnecting = connectingDevice?.address == device.address,
            enabled = connectingDevice == null,
            onClick = { onConnect(device) },
            onCancelConnect = onCancelConnect,
          )
        }
      }
    }
  }
}

@Composable
private fun Obd2DeviceRow(
  device: Obd2BleDevice,
  isConnecting: Boolean = false,
  enabled: Boolean = true,
  onClick: () -> Unit = {},
  onCancelConnect: () -> Unit = {},
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isConnecting) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
      } else {
        MaterialTheme.colorScheme.surfaceContainerLow
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
          .clip(
            RoundedCornerShape(
              topStart = 16.dp,
              topEnd = 16.dp,
              bottomStart = if (isConnecting) 0.dp else 16.dp,
              bottomEnd = if (isConnecting) 0.dp else 16.dp,
            )
          )
          .clickable(enabled = enabled && !isConnecting, onClick = onClick),
        leadingContent = {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(
                if (device.isLikelyObd) {
                  MaterialTheme.colorScheme.primaryContainer
                } else {
                  MaterialTheme.colorScheme.surfaceVariant
                }
              ),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = if (device.isLikelyObd) {
                Icons.Default.DirectionsCar
              } else {
                Icons.Default.Bluetooth
              },
              contentDescription = null,
              tint = if (device.isLikelyObd) {
                MaterialTheme.colorScheme.onPrimaryContainer
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
            )
          }
        },
        headlineContent = { Text(device.name, fontWeight = FontWeight.Medium) },
        supportingContent = {
          val parts = buildList {
            if (device.isLikelyObd) add("OBD-II Adapter")
            add(if (device.isBonded) "Paired" else "BLE GATT")
            device.rssi?.let { add(obdSignalLabel(it)) }
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

      if (isConnecting) {
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
              text = "Connecting to ${device.name}…",
              style = MaterialTheme.typography.titleSmall,
            )
            Text(
              text = "Discovering GATT UART services and initializing ELM327.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          TextButton(onClick = onCancelConnect) {
            Text("Cancel")
          }
        }
      }
    }
  }
}

@Composable
private fun Obd2ConnectingCard(
  device: Obd2BleDevice,
  onCancel: () -> Unit,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
      Spacer(Modifier.width(16.dp))
      Column(Modifier.weight(1f)) {
        Text(
          text = "Connecting to ${device.name}…",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Text(
          text = "Initializing ELM327 BLE GATT connection.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
      }
      TextButton(onClick = onCancel) {
        Text("Cancel")
      }
    }
  }
}

@Composable
private fun Obd2StatusCard(
  icon: ImageVector,
  title: String,
  body: String,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
      }
      Spacer(Modifier.height(6.dp))
      Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
      if (actionLabel != null && onAction != null) {
        Spacer(Modifier.height(12.dp))
        Button(onClick = onAction) {
          Text(actionLabel)
        }
      }
    }
  }
}

@Composable
private fun Obd2MessageCard(
  message: String,
  onDismiss: () -> Unit,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = message,
        color = MaterialTheme.colorScheme.onErrorContainer,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.weight(1f),
      )
      IconButton(onClick = onDismiss) {
        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onErrorContainer)
      }
    }
  }
}

@Composable
private fun Obd2PairingTipCard() {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.Top,
    ) {
      Icon(
        imageVector = Icons.Default.Info,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp),
      )
      Spacer(Modifier.width(10.dp))
      Text(
        text = "Plug your ELM327 BLE dongle into the vehicle's 16-pin OBD-II port under the dash and turn the ignition to ON (or start the engine). BLE adapters connect directly without a PIN.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

// =================================================================================================
// Top Toolbar Search Overlay (Matches Plants & Recipes Catalog Search Overlay)
// =================================================================================================

@Composable
private fun Obd2DtcSearchOverlay(
  allCatalogCount: Int,
  query: String,
  selectedCategory: DtcSystemFilter?,
  onSelectCategory: (DtcSystemFilter?) -> Unit,
  matchingDtcs: List<Obd2DtcEntry>,
) {
  Surface(
    color = MaterialTheme.colorScheme.background,
    modifier = Modifier
      .fillMaxSize()
      .testTag("obd2_dtc_search_overlay"),
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Category Filter Chips Row (matching Plants & Recipes)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        FilterChip(
          selected = selectedCategory == null,
          onClick = { onSelectCategory(null) },
          label = { Text("All ($allCatalogCount)") },
          shape = RoundedCornerShape(20.dp),
        )
        DtcSystemFilter.entries.forEach { category ->
          FilterChip(
            selected = selectedCategory == category,
            onClick = { onSelectCategory(category) },
            label = { Text(category.label) },
            shape = RoundedCornerShape(20.dp),
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        item {
          Text(
            text = "${matchingDtcs.size} matching fault code${if (matchingDtcs.size == 1) "" else "s"}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
          )
        }

        items(matchingDtcs, key = { "search_${it.code}" }) { dtc ->
          Obd2DtcItemCard(dtc = dtc)
        }
      }
    }
  }
}

// =================================================================================================
// Screen 2: Connected OBD-II Scanner Dashboard (Live Gauges, Fault Codes, Monitors & VIN)
// =================================================================================================

@Composable
private fun Obd2ConnectedDashboardContent(
  selectedTab: Obd2ScannerTab,
  onSelectTab: (Obd2ScannerTab) -> Unit,
  connectedDeviceName: String?,
  telemetry: Obd2LiveTelemetry,
  useImperial: Boolean,
  isPolling: Boolean,
  activeDtcs: List<Obd2DtcEntry>,
  monitorSummary: Obd2MonitorSummary,
  vehicleInfo: Obd2VehicleInfo,
  onTogglePolling: () -> Unit,
  onResetPeaks: () -> Unit,
  onScanCodes: () -> Unit,
  onRequestClearCodes: () -> Unit,
  onOpenDtcSearch: () -> Unit,
  onRefreshVehicleInfo: () -> Unit,
  onCopyVin: (String) -> Unit,
) {
  Column(modifier = Modifier.fillMaxSize()) {
    // Navigation Tab Pills
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Obd2ScannerTab.entries.forEach { tab ->
        val selected = selectedTab == tab
        val badgeCount = if (tab == Obd2ScannerTab.TROUBLE_CODES && activeDtcs.isNotEmpty()) {
          activeDtcs.size
        } else {
          0
        }
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
          } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
          },
          border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
              MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            } else {
              MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            },
          ),
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onSelectTab(tab) }
            .testTag("obd2_tab_${tab.name}"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = tab.title,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
              color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            if (badgeCount > 0) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error,
              ) {
                Text(
                  text = badgeCount.toString(),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onError,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                )
              }
            }
          }
        }
      }
    }

    Box(modifier = Modifier.weight(1f)) {
      when (selectedTab) {
        Obd2ScannerTab.LIVE_GAUGES -> Obd2LiveGaugesContent(
          connectedDeviceName = connectedDeviceName,
          telemetry = telemetry,
          useImperial = useImperial,
          isPolling = isPolling,
          onTogglePolling = onTogglePolling,
          onResetPeaks = onResetPeaks,
        )

        Obd2ScannerTab.TROUBLE_CODES -> Obd2TroubleCodesContent(
          activeDtcs = activeDtcs,
          milOn = monitorSummary.milOn,
          onScanCodes = onScanCodes,
          onRequestClearCodes = onRequestClearCodes,
          onOpenDtcSearch = onOpenDtcSearch,
        )

        Obd2ScannerTab.VEHICLE_INFO -> Obd2VehicleAndMonitorsContent(
          vehicleInfo = vehicleInfo,
          monitorSummary = monitorSummary,
          onRefresh = onRefreshVehicleInfo,
          onCopyVin = onCopyVin,
        )
      }
    }
  }
}

@Composable
private fun Obd2LiveGaugesContent(
  connectedDeviceName: String?,
  telemetry: Obd2LiveTelemetry,
  useImperial: Boolean,
  isPolling: Boolean,
  onTogglePolling: () -> Unit,
  onResetPeaks: () -> Unit,
) {
  val (speedValueText, speedUnitText) = Obd2Parser.formatSpeed(telemetry.speedKmh, useImperial)
  val (maxSpeedText, _) = Obd2Parser.formatSpeed(telemetry.maxSpeedKmh, useImperial)
  val (coolantText, tempUnitText) = Obd2Parser.formatTemperature(telemetry.coolantTempC, useImperial)
  val (minCoolantText, _) = Obd2Parser.formatTemperature(telemetry.minCoolantTempC, useImperial)
  val (maxCoolantText, _) = Obd2Parser.formatTemperature(telemetry.maxCoolantTempC, useImperial)
  val (intakeTempText, _) = Obd2Parser.formatTemperature(telemetry.intakeAirTempC, useImperial)

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Dual Semicircular Hero Gauges Card (RPM + Speed)
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
              )
              Text(
                text = connectedDeviceName ?: "Live Telemetry (Mode 01)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              IconButton(
                onClick = onTogglePolling,
                modifier = Modifier.size(32.dp),
              ) {
                Icon(
                  imageVector = if (isPolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                  contentDescription = if (isPolling) "Pause live polling" else "Resume live polling",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp),
                )
              }
              IconButton(
                onClick = onResetPeaks,
                modifier = Modifier.size(32.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = "Reset peak readings",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            // Engine RPM Gauge
            Obd2DialGauge(
              title = "ENGINE RPM",
              valueText = String.format(Locale.US, "%d", telemetry.rpm.roundToInt()),
              unitText = "rpm",
              subtext = "Peak ${telemetry.maxRpm.roundToInt()} rpm",
              fraction = (telemetry.rpm / 8000f).coerceIn(0f, 1f),
              redlineStartFraction = 0.78f,
              useTertiaryColor = false,
              modifier = Modifier.weight(1f),
            )

            // Vehicle Speed Gauge
            val maxScaleSpeed = if (useImperial) 140f else 220f
            val currentDisplaySpeed = if (useImperial) {
              Obd2Parser.kmhToMph(telemetry.speedKmh)
            } else {
              telemetry.speedKmh
            }
            Obd2DialGauge(
              title = "VEHICLE SPEED",
              valueText = speedValueText,
              unitText = speedUnitText,
              subtext = "Peak $maxSpeedText $speedUnitText",
              fraction = (currentDisplaySpeed / maxScaleSpeed).coerceIn(0f, 1f),
              redlineStartFraction = 1f,
              useTertiaryColor = true,
              modifier = Modifier.weight(1f),
            )
          }
        }
      }
    }

    // 2x4 Sensor Telemetry Grid
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Obd2MetricCard(
            label = "Coolant Temp",
            pidBadge = "0105",
            value = "$coolantText $tempUnitText",
            subtext = "Min $minCoolantText • Max $maxCoolantText $tempUnitText",
            progress = ((telemetry.coolantTempC + 20f) / 150f).coerceIn(0f, 1f),
            isWarning = telemetry.coolantTempC >= 108f,
            modifier = Modifier.weight(1f),
          )
          Obd2MetricCard(
            label = "Battery Voltage",
            pidBadge = "ATRV",
            value = String.format(Locale.US, "%.1f V", telemetry.batteryVoltage),
            subtext = when {
              telemetry.batteryVoltage >= 13.5f -> "Alternator Charging"
              telemetry.batteryVoltage >= 12.2f -> "Nominal 12V Battery"
              else -> "Low System Voltage"
            },
            progress = ((telemetry.batteryVoltage - 10f) / 6f).coerceIn(0f, 1f),
            isWarning = telemetry.batteryVoltage < 11.8f,
            modifier = Modifier.weight(1f),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Obd2MetricCard(
            label = "Engine Load",
            pidBadge = "0104",
            value = String.format(Locale.US, "%.1f %%", telemetry.engineLoadPct),
            subtext = "Calculated torque load",
            progress = (telemetry.engineLoadPct / 100f).coerceIn(0f, 1f),
            modifier = Modifier.weight(1f),
          )
          Obd2MetricCard(
            label = "Throttle Position",
            pidBadge = "0111",
            value = String.format(Locale.US, "%.1f %%", telemetry.throttlePct),
            subtext = "Absolute throttle plate",
            progress = (telemetry.throttlePct / 100f).coerceIn(0f, 1f),
            modifier = Modifier.weight(1f),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Obd2MetricCard(
            label = "Short-Term Fuel Trim",
            pidBadge = "0106",
            value = String.format(Locale.US, "%+.1f %%", telemetry.shortTermFuelTrimPct),
            subtext = "Bank 1 closed-loop STFT",
            progress = ((telemetry.shortTermFuelTrimPct + 25f) / 50f).coerceIn(0f, 1f),
            isWarning = kotlin.math.abs(telemetry.shortTermFuelTrimPct) > 15f,
            modifier = Modifier.weight(1f),
          )
          Obd2MetricCard(
            label = "Long-Term Fuel Trim",
            pidBadge = "0107",
            value = String.format(Locale.US, "%+.1f %%", telemetry.longTermFuelTrimPct),
            subtext = "Bank 1 learned LTFT",
            progress = ((telemetry.longTermFuelTrimPct + 25f) / 50f).coerceIn(0f, 1f),
            isWarning = kotlin.math.abs(telemetry.longTermFuelTrimPct) > 15f,
            modifier = Modifier.weight(1f),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Obd2MetricCard(
            label = "Intake Air Temp",
            pidBadge = "010F",
            value = "$intakeTempText $tempUnitText",
            subtext = "Manifold / MAF IAT sensor",
            progress = ((telemetry.intakeAirTempC + 20f) / 100f).coerceIn(0f, 1f),
            modifier = Modifier.weight(1f),
          )
          Obd2MetricCard(
            label = "MAF Air Flow",
            pidBadge = "0110",
            value = String.format(Locale.US, "%.1f g/s", telemetry.mafGramsPerSec),
            subtext = "Mass air flow sensor",
            progress = (telemetry.mafGramsPerSec / 120f).coerceIn(0f, 1f),
            modifier = Modifier.weight(1f),
          )
        }
      }
    }
  }
}

@Composable
private fun Obd2DialGauge(
  title: String,
  valueText: String,
  unitText: String,
  subtext: String,
  fraction: Float,
  redlineStartFraction: Float,
  useTertiaryColor: Boolean,
  modifier: Modifier = Modifier,
) {
  val animatedFraction by animateFloatAsState(
    targetValue = fraction.coerceIn(0f, 1f),
    animationSpec = spring(stiffness = Spring.StiffnessLow),
    label = "obd2_gauge_fraction",
  )

  val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
  val redlineColor = MaterialTheme.colorScheme.error.copy(alpha = 0.65f)
  val activeColor = when {
    animatedFraction >= redlineStartFraction && redlineStartFraction < 1f -> MaterialTheme.colorScheme.error
    useTertiaryColor -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.primary
  }
  val needleTipColor = MaterialTheme.colorScheme.onSurface

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(4.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(118.dp),
      contentAlignment = Alignment.BottomCenter,
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val strokePx = 11.dp.toPx()
        val diameter = minOf(size.width - strokePx * 2f, (size.height - strokePx) * 1.75f)
        val topLeft = Offset(
          x = (size.width - diameter) / 2f,
          y = size.height - diameter * 0.56f,
        )
        val arcSize = Size(diameter, diameter)
        val startAngle = 165f
        val totalSweep = 210f

        // Background arc
        drawArc(
          color = trackColor,
          startAngle = startAngle,
          sweepAngle = totalSweep,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )

        // Optional redline zone
        if (redlineStartFraction < 1f) {
          val redStart = startAngle + totalSweep * redlineStartFraction
          val redSweep = totalSweep * (1f - redlineStartFraction)
          drawArc(
            color = redlineColor,
            startAngle = redStart,
            sweepAngle = redSweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokePx * 0.45f, cap = StrokeCap.Round),
          )
        }

        // Active value arc
        if (animatedFraction > 0.005f) {
          drawArc(
            color = activeColor,
            startAngle = startAngle,
            sweepAngle = totalSweep * animatedFraction,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
          )
        }

        // Indicator dot on the arc
        val angleDeg = startAngle + totalSweep * animatedFraction
        val angleRad = angleDeg * (PI / 180.0)
        val radius = diameter / 2f
        val center = Offset(topLeft.x + radius, topLeft.y + radius)
        val dotCenter = Offset(
          x = center.x + (radius * cos(angleRad)).toFloat(),
          y = center.y + (radius * sin(angleRad)).toFloat(),
        )
        drawCircle(
          color = needleTipColor,
          radius = strokePx * 0.42f,
          center = dotCenter,
        )
      }

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 4.dp),
      ) {
        Text(
          text = valueText,
          style = MaterialTheme.typography.headlineSmall,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = unitText,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    Text(
      text = subtext,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun Obd2MetricCard(
  label: String,
  pidBadge: String,
  value: String,
  subtext: String,
  progress: Float,
  isWarning: Boolean = false,
  modifier: Modifier = Modifier,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(
      width = 1.dp,
      color = if (isWarning) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
      } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
      },
    ),
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = label,
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
        ) {
          Text(
            text = pidBadge,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
          )
        }
      }

      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = if (isWarning) {
          MaterialTheme.colorScheme.error
        } else {
          MaterialTheme.colorScheme.onSurface
        },
      )

      LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        color = if (isWarning) {
          MaterialTheme.colorScheme.error
        } else {
          MaterialTheme.colorScheme.primary
        },
        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        strokeCap = StrokeCap.Round,
        modifier = Modifier
          .fillMaxWidth()
          .height(5.dp),
      )

      Text(
        text = subtext,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun Obd2TroubleCodesContent(
  activeDtcs: List<Obd2DtcEntry>,
  milOn: Boolean,
  onScanCodes: () -> Unit,
  onRequestClearCodes: () -> Unit,
  onOpenDtcSearch: () -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Scan & Clear MIL Action Card
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                  if (activeDtcs.isNotEmpty() || milOn) {
                    MaterialTheme.colorScheme.errorContainer
                  } else {
                    MaterialTheme.colorScheme.primaryContainer
                  }
                ),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = if (activeDtcs.isNotEmpty() || milOn) {
                  Icons.Default.Warning
                } else {
                  Icons.Default.CheckCircle
                },
                contentDescription = null,
                tint = if (activeDtcs.isNotEmpty() || milOn) {
                  MaterialTheme.colorScheme.error
                } else {
                  MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(22.dp),
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (activeDtcs.isNotEmpty()) {
                  "${activeDtcs.size} Fault Code(s) Detected"
                } else {
                  "No Active Fault Codes"
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = if (milOn) {
                  "Check Engine Light (MIL) is ON • Stored (Mode 03) & Pending (Mode 07)"
                } else {
                  "Check Engine Light (MIL) is OFF • Tap Scan to query ECU"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Button(
              onClick = onScanCodes,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("obd2_scan_dtcs_button"),
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Scan Codes")
            }

            OutlinedButton(
              onClick = onRequestClearCodes,
              enabled = activeDtcs.isNotEmpty() || milOn,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("obd2_clear_dtcs_button"),
            ) {
              Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Clear MIL")
            }
          }
        }
      }
    }

    // Active / Pending Vehicle DTCs section
    if (activeDtcs.isNotEmpty()) {
      item {
        Text(
          text = "Vehicle Trouble Codes (${activeDtcs.size})",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(horizontal = 4.dp),
        )
      }
      items(activeDtcs, key = { "active_${it.code}_${it.status.name}" }) { dtc ->
        Obd2DtcItemCard(dtc = dtc)
      }
    }

    // Prompt card to use the top-bar DTC search
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .clickable { onOpenDtcSearch() },
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Lookup Offline DTC Dictionary",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Tap the search icon in the top toolbar to search ${Obd2DtcCatalog.ALL_ENTRIES.size}+ fault codes & causes",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

@Composable
private fun Obd2DtcItemCard(
  dtc: Obd2DtcEntry,
  modifier: Modifier = Modifier,
) {
  val (badgeBg, badgeFg) = when (dtc.severity) {
    DtcSeverity.CRITICAL -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    DtcSeverity.WARNING -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    DtcSeverity.INFO -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
  }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = badgeBg,
          ) {
            Text(
              text = dtc.code,
              style = MaterialTheme.typography.bodyMedium,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = badgeFg,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
          }
          Text(
            text = dtc.system,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
          Text(
            text = if (dtc.status == DtcStatus.LOOKUP) dtc.severity.label else dtc.status.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
          )
        }
      }

      Text(
        text = dtc.description,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
      )

      Text(
        text = "Likely causes: ${dtc.commonCauses}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun Obd2VehicleAndMonitorsContent(
  vehicleInfo: Obd2VehicleInfo,
  monitorSummary: Obd2MonitorSummary,
  onRefresh: () -> Unit,
  onCopyVin: (String) -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Vehicle Identification (Mode 09) Card
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Default.Info,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp),
                )
              }
              Column {
                Text(
                  text = "Vehicle Identification (Mode 09)",
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = "${vehicleInfo.elmVersion} • ${vehicleInfo.protocolName}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            IconButton(onClick = onRefresh) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh Vehicle Info",
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

          // VIN Row with Copy button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "VIN (Vehicle Identification Number)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = vehicleInfo.vin.ifBlank { "Tap Refresh to query VIN (PID 0902)" },
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              if (vehicleInfo.modelYearHint.isNotBlank() || vehicleInfo.regionHint.isNotBlank()) {
                Text(
                  text = "Model Year: ${vehicleInfo.modelYearHint} • Region: ${vehicleInfo.regionHint}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
            if (vehicleInfo.vin.isNotBlank()) {
              IconButton(onClick = { onCopyVin(vehicleInfo.vin) }) {
                Icon(
                  imageVector = Icons.Default.ContentCopy,
                  contentDescription = "Copy VIN",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          }

          if (vehicleInfo.calibrationId.isNotBlank() || vehicleInfo.ecuName.isNotBlank()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Calibration ID (0904)",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                  text = vehicleInfo.calibrationId.ifBlank { "N/A" },
                  style = MaterialTheme.typography.bodyMedium,
                  fontFamily = FontFamily.Monospace,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "ECU Module (090A)",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                  text = vehicleInfo.ecuName.ifBlank { "ECM" },
                  style = MaterialTheme.typography.bodyMedium,
                  fontFamily = FontFamily.Monospace,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
            }
          }
        }
      }
    }

    // Emissions Readiness Summary Card (Mode 01 PID 01)
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(
                    if (monitorSummary.milOn) {
                      MaterialTheme.colorScheme.errorContainer
                    } else {
                      MaterialTheme.colorScheme.primaryContainer
                    }
                  ),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = if (monitorSummary.milOn) {
                    Icons.Default.ErrorOutline
                  } else {
                    Icons.Default.Verified
                  },
                  contentDescription = null,
                  tint = if (monitorSummary.milOn) {
                    MaterialTheme.colorScheme.error
                  } else {
                    MaterialTheme.colorScheme.primary
                  },
                  modifier = Modifier.size(20.dp),
                )
              }
              Column {
                Text(
                  text = "I/M Emissions Readiness (PID 0101)",
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                val ignitionLabel = if (monitorSummary.isCompressionIgnition) {
                  "Compression Ignition (Diesel)"
                } else {
                  "Spark Ignition (Gasoline)"
                }
                Text(
                  text = "$ignitionLabel • MIL: ${if (monitorSummary.milOn) "ON" else "OFF"} • ${monitorSummary.storedDtcCount} Stored DTCs",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }

          if (monitorSummary.monitors.isEmpty()) {
            FilledTonalButton(
              onClick = onRefresh,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Read Readiness Monitors")
            }
          } else {
            val totalSupported = monitorSummary.supportedMonitors.size.coerceAtLeast(1)
            val readyRatio = monitorSummary.readyCount.toFloat() / totalSupported.toFloat()
            LinearProgressIndicator(
              progress = { readyRatio },
              color = MaterialTheme.colorScheme.primary,
              trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
              strokeCap = StrokeCap.Round,
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            )
            Text(
              text = "${monitorSummary.readyCount} of ${monitorSummary.supportedMonitors.size} supported monitors Ready (${monitorSummary.incompleteCount} Incomplete)",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            monitorSummary.monitors.forEach { monitor ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = monitor.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = if (monitor.isContinuous) "Continuous Monitor" else "Once-Per-Trip Monitor",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }

                val (chipBg, chipFg) = when (monitor.status) {
                  MonitorStatus.READY ->
                    MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
                  MonitorStatus.INCOMPLETE ->
                    MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                  MonitorStatus.NOT_SUPPORTED ->
                    MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                }
                Surface(
                  shape = RoundedCornerShape(20.dp),
                  color = chipBg,
                ) {
                  Text(
                    text = monitor.status.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = chipFg,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun Obd2ScannerScreenPreview() {
  ToolboxTheme {
    Obd2ScannerScreen()
  }
}
