package com.example.toolbox.ui.tools

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toolbox.ui.theme.ToolboxTheme
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// =================================================================================================
// Gamepad input actions abstraction (keeps UI previewable and decoupled from ViewModel)
// =================================================================================================

/** All input the gamepad UI can emit. Implemented by [BluetoothRemoteViewModel] in production. */
interface GamepadInputActions {
  fun setButton(button: GamepadButton, pressed: Boolean)
  fun setLeftJoystick(x: Float, y: Float)
  fun setRightJoystick(x: Float, y: Float)
}

private class ViewModelGamepadActions(private val vm: BluetoothRemoteViewModel) : GamepadInputActions {
  override fun setButton(button: GamepadButton, pressed: Boolean) = vm.setGamepadButton(button, pressed)
  override fun setLeftJoystick(x: Float, y: Float) = vm.setLeftJoystick(x, y)
  override fun setRightJoystick(x: Float, y: Float) = vm.setRightJoystick(x, y)
}

internal object NoOpGamepadActions : GamepadInputActions {
  override fun setButton(button: GamepadButton, pressed: Boolean) = Unit
  override fun setLeftJoystick(x: Float, y: Float) = Unit
  override fun setRightJoystick(x: Float, y: Float) = Unit
}

// =================================================================================================
// Entry point
// =================================================================================================

@Composable
fun BluetoothGamepadScreen(modifier: Modifier = Modifier) {
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
      title = "Bluetooth Gamepad",
      icon = Icons.Default.SportsEsports,
    )
    return
  }

  val context = LocalContext.current
  val application = context.applicationContext as Application
  val viewModel: BluetoothRemoteViewModel = viewModel { BluetoothRemoteViewModel(application) }
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val actions = remember(viewModel) { ViewModelGamepadActions(viewModel) }

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
    label = "gamepad_screen",
  ) { connected ->
    if (connected && connection is RemoteConnection.Connected) {
      GamepadControlContent(
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
        title = "Bluetooth Gamepad",
        icon = Icons.Default.SportsEsports,
      )
    }
  }
}

// =================================================================================================
// Screen 2: Gamepad controller surface
// =================================================================================================

@Composable
internal fun GamepadControlContent(
  deviceName: String,
  actions: GamepadInputActions,
  onDisconnect: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler {
    onDisconnect()
  }

  // Keep the display awake while the gamepad is in use.
  if (!LocalInspectionMode.current) {
    val view = LocalView.current
    DisposableEffect(view) {
      view.keepScreenOn = true
      onDispose { view.keepScreenOn = false }
    }
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("bt_gamepad_view"),
  ) {
    val isLandscape = maxWidth > maxHeight * 1.2f

    if (isLandscape) {
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        // Top bar: Left shoulder + Center (Minus, Header, Plus) + Right shoulder
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            TriggerButton(
              label = "LT",
              contentDescription = "Left trigger",
              button = GamepadButton.LEFT_TRIGGER,
              isLeft = true,
              actions = actions,
              modifier = Modifier
                .width(76.dp)
                .height(44.dp)
                .testTag("bt_gamepad_lt"),
            )
            BumperButton(
              label = "LB",
              contentDescription = "Left bumper",
              button = GamepadButton.LEFT_BUMPER,
              actions = actions,
              modifier = Modifier
                .width(76.dp)
                .height(44.dp)
                .testTag("bt_gamepad_lb"),
            )
          }

          GamepadConnectionHeader(
            deviceName = deviceName,
            onDisconnect = onDisconnect,
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 12.dp),
          )

          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            BumperButton(
              label = "RB",
              contentDescription = "Right bumper",
              button = GamepadButton.RIGHT_BUMPER,
              actions = actions,
              modifier = Modifier
                .width(76.dp)
                .height(44.dp)
                .testTag("bt_gamepad_rb"),
            )
            TriggerButton(
              label = "RT",
              contentDescription = "Right trigger",
              button = GamepadButton.RIGHT_TRIGGER,
              isLeft = false,
              actions = actions,
              modifier = Modifier
                .width(76.dp)
                .height(44.dp)
                .testTag("bt_gamepad_rt"),
            )
          }
        }

        // Main landscape controls: Left D-Pad + Left Joystick + Minus/Plus + Right Joystick + ABXY
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          GamepadDPad(
            actions = actions,
            size = 144.dp,
          )
          GamepadJoystick(
            label = "L",
            contentDescription = "Left joystick",
            onMove = actions::setLeftJoystick,
            size = 136.dp,
            modifier = Modifier.testTag("bt_gamepad_left_joystick"),
          )
          MinusPlusCluster(actions = actions)
          GamepadJoystick(
            label = "R",
            contentDescription = "Right joystick",
            onMove = actions::setRightJoystick,
            size = 136.dp,
            modifier = Modifier.testTag("bt_gamepad_right_joystick"),
          )
          GamepadFaceButtons(
            actions = actions,
            size = 144.dp,
          )
        }
      }
    } else {
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        // ---- Header ----
        GamepadConnectionHeader(
          deviceName = deviceName,
          onDisconnect = onDisconnect,
          modifier = Modifier.fillMaxWidth(),
        )

        // ---- Shoulder Section: Triggers (LT / RT), Bumpers (LB / RB), and Minus / Plus ----
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          // Triggers Row (LT & RT)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            TriggerButton(
              label = "LT",
              contentDescription = "Left trigger",
              button = GamepadButton.LEFT_TRIGGER,
              isLeft = true,
              actions = actions,
              modifier = Modifier
                .width(124.dp)
                .height(46.dp)
                .testTag("bt_gamepad_lt"),
            )
            TriggerButton(
              label = "RT",
              contentDescription = "Right trigger",
              button = GamepadButton.RIGHT_TRIGGER,
              isLeft = false,
              actions = actions,
              modifier = Modifier
                .width(124.dp)
                .height(46.dp)
                .testTag("bt_gamepad_rt"),
            )
          }

          // Bumpers Row (LB & RB) with Minus (-) and Plus (+) in the center
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            BumperButton(
              label = "LB",
              contentDescription = "Left bumper",
              button = GamepadButton.LEFT_BUMPER,
              actions = actions,
              modifier = Modifier
                .width(104.dp)
                .height(42.dp)
                .testTag("bt_gamepad_lb"),
            )

            MinusPlusCluster(actions = actions)

            BumperButton(
              label = "RB",
              contentDescription = "Right bumper",
              button = GamepadButton.RIGHT_BUMPER,
              actions = actions,
              modifier = Modifier
                .width(104.dp)
                .height(42.dp)
                .testTag("bt_gamepad_rb"),
            )
          }
        }

        // ---- Middle Row: Left D-Pad & Right A / B / X / Y Diamond ----
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          GamepadDPad(
            actions = actions,
            size = 156.dp,
          )
          GamepadFaceButtons(
            actions = actions,
            size = 156.dp,
          )
        }

        // ---- Bottom Row: Left Joystick & Right Joystick ----
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          GamepadJoystick(
            label = "L",
            contentDescription = "Left joystick",
            onMove = actions::setLeftJoystick,
            size = 148.dp,
            modifier = Modifier.testTag("bt_gamepad_left_joystick"),
          )
          GamepadJoystick(
            label = "R",
            contentDescription = "Right joystick",
            onMove = actions::setRightJoystick,
            size = 148.dp,
            modifier = Modifier.testTag("bt_gamepad_right_joystick"),
          )
        }
      }
    }
  }
}

@Composable
private fun GamepadConnectionHeader(
  deviceName: String,
  onDisconnect: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier,
  ) {
    Box(
      Modifier
        .size(10.dp)
        .clip(CircleShape)
        .background(Color(0xFF34A853)),
    )
    Spacer(Modifier.width(8.dp))
    Column(Modifier.weight(1f)) {
      Text(
        text = deviceName,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
      )
      Text(
        text = "Connected",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    IconButton(
      onClick = onDisconnect,
      modifier = Modifier.testTag("bt_disconnect"),
    ) {
      Icon(Icons.Default.LinkOff, contentDescription = "Disconnect")
    }
  }
}

// =================================================================================================
// Triggers (LT / RT), Bumpers (LB / RB), and Minus / Plus (Select / Start)
// =================================================================================================

@Composable
private fun TriggerButton(
  label: String,
  contentDescription: String,
  button: GamepadButton,
  isLeft: Boolean,
  actions: GamepadInputActions,
  modifier: Modifier = Modifier,
) {
  val shape: Shape = if (isLeft) {
    RoundedCornerShape(topStart = 22.dp, topEnd = 10.dp, bottomStart = 10.dp, bottomEnd = 10.dp)
  } else {
    RoundedCornerShape(topStart = 10.dp, topEnd = 22.dp, bottomStart = 10.dp, bottomEnd = 10.dp)
  }
  PressableSurface(
    onPress = { actions.setButton(button, true) },
    onRelease = { actions.setButton(button, false) },
    contentDescription = contentDescription,
    shape = shape,
    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    modifier = modifier,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.ExtraBold,
    )
  }
}

@Composable
private fun BumperButton(
  label: String,
  contentDescription: String,
  button: GamepadButton,
  actions: GamepadInputActions,
  modifier: Modifier = Modifier,
) {
  PressableSurface(
    onPress = { actions.setButton(button, true) },
    onRelease = { actions.setButton(button, false) },
    contentDescription = contentDescription,
    shape = RoundedCornerShape(14.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor = MaterialTheme.colorScheme.onSurface,
    modifier = modifier,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
    )
  }
}

@Composable
private fun MinusPlusCluster(
  actions: GamepadInputActions,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      PressableButton(
        onPress = { actions.setButton(GamepadButton.MINUS, true) },
        onRelease = { actions.setButton(GamepadButton.MINUS, false) },
        contentDescription = "Minus",
        size = 40.dp,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag("bt_gamepad_minus"),
      ) {
        Icon(
          imageVector = Icons.Default.Remove,
          contentDescription = null,
          modifier = Modifier.size(20.dp),
        )
      }
      Text(
        text = "− / SELECT",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      PressableButton(
        onPress = { actions.setButton(GamepadButton.PLUS, true) },
        onRelease = { actions.setButton(GamepadButton.PLUS, false) },
        contentDescription = "Plus",
        size = 40.dp,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag("bt_gamepad_plus"),
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = null,
          modifier = Modifier.size(20.dp),
        )
      }
      Text(
        text = "+ / START",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

// =================================================================================================
// Left D-Pad
// =================================================================================================

internal fun gamepadDpadButtonForOffset(offset: Offset, sizePx: Float): GamepadButton? {
  if (sizePx <= 0f) return null
  val cx = sizePx / 2f
  val cy = sizePx / 2f
  val dx = offset.x - cx
  val dy = offset.y - cy
  val dist = sqrt(dx * dx + dy * dy)
  val radius = sizePx / 2f
  if (dist > radius) return null
  // Small center hub dead-zone
  if (dist < radius * 0.20f) return null

  return if (abs(dx) <= abs(dy)) {
    if (dy < 0f) GamepadButton.DPAD_UP else GamepadButton.DPAD_DOWN
  } else {
    if (dx < 0f) GamepadButton.DPAD_LEFT else GamepadButton.DPAD_RIGHT
  }
}

@Composable
private fun GamepadDPad(
  actions: GamepadInputActions,
  size: Dp,
  modifier: Modifier = Modifier,
) {
  val housingColor = MaterialTheme.colorScheme.surfaceContainerLow
  val crossColor = MaterialTheme.colorScheme.surfaceContainerHigh
  val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
  val haptics = LocalHapticFeedback.current
  val coroutineScope = rememberCoroutineScope()
  val currentActions by rememberUpdatedState(actions)
  var activeButton by remember { mutableStateOf<GamepadButton?>(null) }

  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(housingColor)
      .border(1.dp, outlineColor, CircleShape)
      .testTag("bt_gamepad_dpad")
      .pointerInput(Unit) {
        awaitEachGesture {
          val down = awaitFirstDown(requireUnconsumed = false)
          down.consume()
          val sizePx = this.size.width.toFloat()
          val initial = gamepadDpadButtonForOffset(down.position, sizePx)
          var current: GamepadButton? = initial
          activeButton = initial
          if (initial != null) {
            haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
            currentActions.setButton(initial, true)
          }
          val pressStartTime = System.currentTimeMillis()

          while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) {
              change.consume()
              break
            }
            change.consume()
            val next = gamepadDpadButtonForOffset(change.position, sizePx)
            if (next != current) {
              if (current != null) {
                currentActions.setButton(current, false)
              }
              current = next
              activeButton = next
              if (next != null) {
                haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                currentActions.setButton(next, true)
              }
            }
          }

          val releasedButton = current
          if (releasedButton != null) {
            val elapsed = System.currentTimeMillis() - pressStartTime
            if (elapsed < 45L) {
              coroutineScope.launch {
                delay(45L - elapsed)
                activeButton = null
                currentActions.setButton(releasedButton, false)
              }
            } else {
              activeButton = null
              currentActions.setButton(releasedButton, false)
            }
          } else {
            activeButton = null
          }
        }
      },
    contentAlignment = Alignment.Center,
  ) {
    // Draw cross background inside the circular D-Pad well
    Canvas(modifier = Modifier.fillMaxSize()) {
      val armThickness = this.size.width * 0.34f
      val armLength = this.size.width * 0.88f
      val corner = CornerRadius(10.dp.toPx(), 10.dp.toPx())
      // Vertical arm
      drawRoundRect(
        color = crossColor,
        topLeft = Offset((this.size.width - armThickness) / 2f, (this.size.height - armLength) / 2f),
        size = Size(armThickness, armLength),
        cornerRadius = corner,
      )
      // Horizontal arm
      drawRoundRect(
        color = crossColor,
        topLeft = Offset((this.size.width - armLength) / 2f, (this.size.height - armThickness) / 2f),
        size = Size(armLength, armThickness),
        cornerRadius = corner,
      )
    }

    GamepadDPadArrow(
      icon = Icons.Default.KeyboardArrowUp,
      label = "Up",
      button = GamepadButton.DPAD_UP,
      isPressed = activeButton == GamepadButton.DPAD_UP,
      actions = actions,
      alignment = Alignment.TopCenter,
      tag = "bt_gamepad_dpad_up",
    )
    GamepadDPadArrow(
      icon = Icons.Default.KeyboardArrowDown,
      label = "Down",
      button = GamepadButton.DPAD_DOWN,
      isPressed = activeButton == GamepadButton.DPAD_DOWN,
      actions = actions,
      alignment = Alignment.BottomCenter,
      tag = "bt_gamepad_dpad_down",
    )
    GamepadDPadArrow(
      icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
      label = "Left",
      button = GamepadButton.DPAD_LEFT,
      isPressed = activeButton == GamepadButton.DPAD_LEFT,
      actions = actions,
      alignment = Alignment.CenterStart,
      tag = "bt_gamepad_dpad_left",
    )
    GamepadDPadArrow(
      icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
      label = "Right",
      button = GamepadButton.DPAD_RIGHT,
      isPressed = activeButton == GamepadButton.DPAD_RIGHT,
      actions = actions,
      alignment = Alignment.CenterEnd,
      tag = "bt_gamepad_dpad_right",
    )

    // Center pivot dimple
    Box(
      modifier = Modifier
        .size(20.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    )
  }
}

@Composable
private fun BoxScope.GamepadDPadArrow(
  icon: ImageVector,
  label: String,
  button: GamepadButton,
  isPressed: Boolean,
  actions: GamepadInputActions,
  alignment: Alignment,
  tag: String,
) {
  val contentColor = if (isPressed) {
    MaterialTheme.colorScheme.onPrimaryContainer
  } else {
    MaterialTheme.colorScheme.onSurface
  }
  val containerColor = if (isPressed) {
    MaterialTheme.colorScheme.primaryContainer
  } else {
    Color.Transparent
  }
  Box(
    modifier = Modifier
      .align(alignment)
      .padding(6.dp)
      .size(48.dp)
      .clip(RoundedCornerShape(10.dp))
      .background(containerColor)
      .semantics {
        role = Role.Button
        contentDescription = label
        onClick {
          actions.setButton(button, true)
          actions.setButton(button, false)
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
      modifier = Modifier.size(30.dp),
    )
  }
}

// =================================================================================================
// A / B / X / Y Face Buttons Diamond
// =================================================================================================

@Composable
private fun GamepadFaceButtons(
  actions: GamepadInputActions,
  size: Dp,
  modifier: Modifier = Modifier,
) {
  val buttonSize = 50.dp
  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(MaterialTheme.colorScheme.surfaceContainerLow)
      .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
      .padding(6.dp)
      .testTag("bt_gamepad_abxy"),
  ) {
    // Top: Y
    FaceActionButton(
      label = "Y",
      button = GamepadButton.Y,
      actions = actions,
      size = buttonSize,
      containerColor = MaterialTheme.colorScheme.secondaryContainer,
      contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
      modifier = Modifier
        .align(Alignment.TopCenter)
        .testTag("bt_gamepad_y"),
    )
    // Left: X
    FaceActionButton(
      label = "X",
      button = GamepadButton.X,
      actions = actions,
      size = buttonSize,
      containerColor = MaterialTheme.colorScheme.secondaryContainer,
      contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
      modifier = Modifier
        .align(Alignment.CenterStart)
        .testTag("bt_gamepad_x"),
    )
    // Right: B
    FaceActionButton(
      label = "B",
      button = GamepadButton.B,
      actions = actions,
      size = buttonSize,
      containerColor = MaterialTheme.colorScheme.tertiaryContainer,
      contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .testTag("bt_gamepad_b"),
    )
    // Bottom: A
    FaceActionButton(
      label = "A",
      button = GamepadButton.A,
      actions = actions,
      size = buttonSize,
      containerColor = MaterialTheme.colorScheme.primaryContainer,
      contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .testTag("bt_gamepad_a"),
    )
  }
}

@Composable
private fun FaceActionButton(
  label: String,
  button: GamepadButton,
  actions: GamepadInputActions,
  size: Dp,
  containerColor: Color,
  contentColor: Color,
  modifier: Modifier = Modifier,
) {
  PressableButton(
    onPress = { actions.setButton(button, true) },
    onRelease = { actions.setButton(button, false) },
    contentDescription = label,
    size = size,
    containerColor = containerColor,
    contentColor = contentColor,
    modifier = modifier,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.ExtraBold,
    )
  }
}

// =================================================================================================
// Left & Right Analog Joysticks
// =================================================================================================

/**
 * Converts a touch [offset] inside a square joystick box of [sizePx] into normalized
 * `(x, y)` coordinates in `[-1f, 1f]`, clamped to the unit circle.
 */
internal fun joystickVectorForOffset(offset: Offset, sizePx: Float): Pair<Float, Float> {
  if (sizePx <= 0f) return 0f to 0f
  val radius = sizePx / 2f
  val rawX = (offset.x - radius) / radius
  val rawY = (offset.y - radius) / radius
  val mag = sqrt(rawX * rawX + rawY * rawY)
  return if (mag > 1f) {
    (rawX / mag) to (rawY / mag)
  } else {
    rawX.coerceIn(-1f, 1f) to rawY.coerceIn(-1f, 1f)
  }
}

@Composable
private fun GamepadJoystick(
  label: String,
  contentDescription: String,
  onMove: (Float, Float) -> Unit,
  size: Dp,
  modifier: Modifier = Modifier,
) {
  val haptics = LocalHapticFeedback.current
  val currentOnMove by rememberUpdatedState(onMove)
  var stickVector by remember { mutableStateOf(0f to 0f) }
  var isDragging by remember { mutableStateOf(false) }

  val animatedX by animateFloatAsState(
    targetValue = stickVector.first,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "joystick_x_$label",
  )
  val animatedY by animateFloatAsState(
    targetValue = stickVector.second,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "joystick_y_$label",
  )

  val thumbSize = size * 0.44f
  val maxTravelDp = (size - thumbSize) / 2f
  val maxTravelPx = with(LocalDensity.current) { maxTravelDp.toPx() }

  val baseColor = MaterialTheme.colorScheme.surfaceContainerHigh
  val ringColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
  val activeColor = MaterialTheme.colorScheme.primary
  val thumbContainerColor = if (isDragging) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.primaryContainer
  }
  val thumbContentColor = if (isDragging) {
    MaterialTheme.colorScheme.onPrimary
  } else {
    MaterialTheme.colorScheme.onPrimaryContainer
  }

  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(baseColor)
      .border(1.dp, ringColor, CircleShape)
      .semantics {
        this.contentDescription = contentDescription
      }
      .pointerInput(Unit) {
        awaitEachGesture {
          val down = awaitFirstDown(requireUnconsumed = false)
          down.consume()
          isDragging = true
          haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
          val sizePx = minOf(this.size.width, this.size.height).toFloat()
          val initial = joystickVectorForOffset(down.position, sizePx)
          stickVector = initial
          currentOnMove(initial.first, initial.second)

          while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) {
              change.consume()
              break
            }
            change.consume()
            val next = joystickVectorForOffset(change.position, sizePx)
            if (next != stickVector) {
              stickVector = next
              currentOnMove(next.first, next.second)
            }
          }

          isDragging = false
          stickVector = 0f to 0f
          currentOnMove(0f, 0f)
        }
      },
    contentAlignment = Alignment.Center,
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val center = Offset(this.size.width / 2f, this.size.height / 2f)
      val outerRadius = this.size.minDimension / 2f

      // Subtle inner concentric ring
      drawCircle(
        color = ringColor,
        radius = outerRadius * 0.62f,
        center = center,
        style = Stroke(width = 1.dp.toPx()),
      )

      // Crosshair tick lines
      val tickInset = outerRadius * 0.14f
      val tickEnd = outerRadius * 0.32f
      drawLine(
        color = ringColor,
        start = Offset(center.x, tickInset),
        end = Offset(center.x, tickEnd),
        strokeWidth = 1.5.dp.toPx(),
        cap = StrokeCap.Round,
      )
      drawLine(
        color = ringColor,
        start = Offset(center.x, this.size.height - tickInset),
        end = Offset(center.x, this.size.height - tickEnd),
        strokeWidth = 1.5.dp.toPx(),
        cap = StrokeCap.Round,
      )
      drawLine(
        color = ringColor,
        start = Offset(tickInset, center.y),
        end = Offset(tickEnd, center.y),
        strokeWidth = 1.5.dp.toPx(),
        cap = StrokeCap.Round,
      )
      drawLine(
        color = ringColor,
        start = Offset(this.size.width - tickInset, center.y),
        end = Offset(this.size.width - tickEnd, center.y),
        strokeWidth = 1.5.dp.toPx(),
        cap = StrokeCap.Round,
      )

      // Active stem vector line when deflected
      val displayX = if (isDragging) stickVector.first else animatedX
      val displayY = if (isDragging) stickVector.second else animatedY
      if (abs(displayX) > 0.02f || abs(displayY) > 0.02f) {
        drawLine(
          color = activeColor.copy(alpha = 0.5f),
          start = center,
          end = Offset(
            center.x + displayX * maxTravelPx,
            center.y + displayY * maxTravelPx,
          ),
          strokeWidth = 4.dp.toPx(),
          cap = StrokeCap.Round,
        )
      }
    }

    val displayX = if (isDragging) stickVector.first else animatedX
    val displayY = if (isDragging) stickVector.second else animatedY

    // Thumbstick cap
    Surface(
      shape = CircleShape,
      color = thumbContainerColor,
      contentColor = thumbContentColor,
      tonalElevation = 6.dp,
      border = BorderStroke(
        width = 2.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f),
      ),
      modifier = Modifier
        .size(thumbSize)
        .offset {
          IntOffset(
            x = (displayX * maxTravelPx).roundToInt(),
            y = (displayY * maxTravelPx).roundToInt(),
          )
        },
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(
          text = label,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

// =================================================================================================
// Previews
// =================================================================================================

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun BluetoothGamepadDeviceListPreview() {
  ToolboxTheme {
    BluetoothGamepadScreen()
  }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun BluetoothGamepadControlPreview() {
  ToolboxTheme {
    Surface {
      GamepadControlContent(
        deviceName = "Living Room TV",
        actions = NoOpGamepadActions,
        onDisconnect = {},
      )
    }
  }
}
