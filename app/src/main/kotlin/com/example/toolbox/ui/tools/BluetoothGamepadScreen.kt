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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// =================================================================================================
// Gamepad input actions abstraction (keeps UI previewable and decoupled from ViewModel)
// =================================================================================================

/** All input the gamepad UI can emit. Implemented by [BluetoothRemoteViewModel] in production. */
interface GamepadInputActions {
  fun setButton(button: GamepadButton, pressed: Boolean)
  fun updateDpad(previous: Set<GamepadButton>, current: Set<GamepadButton>) {
    for (button in previous - current) {
      setButton(button, false)
    }
    for (button in current - previous) {
      setButton(button, true)
    }
  }
  fun setLeftJoystick(x: Float, y: Float)
  fun setRightJoystick(x: Float, y: Float)
}

private class ViewModelGamepadActions(private val vm: BluetoothRemoteViewModel) : GamepadInputActions {
  override fun setButton(button: GamepadButton, pressed: Boolean) = vm.setGamepadButton(button, pressed)
  override fun updateDpad(previous: Set<GamepadButton>, current: Set<GamepadButton>) = vm.setGamepadDpad(current)
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
  }
}

// =================================================================================================
// Left D-Pad (8-way with equal 45° sectors, rolling support, rim indicator & bridging corner chevrons)
// =================================================================================================

internal enum class DpadSector(
  val centerAngleDeg: Float,
  val buttons: Set<GamepadButton>,
) {
  RIGHT(0f, setOf(GamepadButton.DPAD_RIGHT)),
  DOWN_RIGHT(45f, setOf(GamepadButton.DPAD_DOWN, GamepadButton.DPAD_RIGHT)),
  DOWN(90f, setOf(GamepadButton.DPAD_DOWN)),
  DOWN_LEFT(135f, setOf(GamepadButton.DPAD_DOWN, GamepadButton.DPAD_LEFT)),
  LEFT(180f, setOf(GamepadButton.DPAD_LEFT)),
  UP_LEFT(-135f, setOf(GamepadButton.DPAD_UP, GamepadButton.DPAD_LEFT)),
  UP(-90f, setOf(GamepadButton.DPAD_UP)),
  UP_RIGHT(-45f, setOf(GamepadButton.DPAD_UP, GamepadButton.DPAD_RIGHT)),
}

private const val DPAD_DEAD_ZONE_RATIO = 0.20f
private const val DPAD_HALF_SECTOR_DEG = 22.5f
private const val DPAD_HYSTERESIS_DEG = 3.5f

private fun angularDistanceDeg(a: Float, b: Float): Float {
  var diff = (a - b) % 360f
  if (diff > 180f) diff -= 360f
  if (diff < -180f) diff += 360f
  return abs(diff)
}

/**
 * Maps a touch [offset] inside (or rolling around) a square D-Pad box of [sizePx] into the
 * active set of cardinal [GamepadButton]s (`1` button for cardinals, `2` for diagonals, or empty
 * in the center dead-zone / outside initial bounds).
 *
 * Uses 8 equal 45° sectors. When [allowOutsideBounds] is true (during an active roll/drag),
 * touches that sweep outside the circular outer radius remain active along their angle.
 * When [previousButtons] is non-empty, applies a small angular hysteresis window to prevent
 * sector boundary flicker while rolling.
 */
internal fun gamepadDpadButtonsForOffset(
  offset: Offset,
  sizePx: Float,
  allowOutsideBounds: Boolean = false,
  previousButtons: Set<GamepadButton> = emptySet(),
): Set<GamepadButton> {
  if (sizePx <= 0f) return emptySet()
  val radius = sizePx / 2f
  val dx = offset.x - radius
  val dy = offset.y - radius
  val dist = sqrt(dx * dx + dy * dy)
  if (!allowOutsideBounds && dist > radius) return emptySet()
  // Small center hub dead-zone
  if (dist < radius * DPAD_DEAD_ZONE_RATIO) return emptySet()

  val angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

  if (previousButtons.isNotEmpty()) {
    val previousSector = DpadSector.entries.firstOrNull { it.buttons == previousButtons }
    if (previousSector != null &&
      angularDistanceDeg(angleDeg, previousSector.centerAngleDeg) <= DPAD_HALF_SECTOR_DEG + DPAD_HYSTERESIS_DEG
    ) {
      return previousSector.buttons
    }
  }

  val normalized = ((angleDeg % 360f) + 360f) % 360f
  val sectorIndex = (((normalized + DPAD_HALF_SECTOR_DEG) % 360f) / 45f).toInt().coerceIn(0, 7)
  return DpadSector.entries[sectorIndex].buttons
}

internal fun gamepadDpadButtonForOffset(offset: Offset, sizePx: Float): GamepadButton? {
  val buttons = gamepadDpadButtonsForOffset(offset, sizePx)
  return buttons.singleOrNull()
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
  val bridgeTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
  val activeBridgeColor = MaterialTheme.colorScheme.primaryContainer
  val primaryColor = MaterialTheme.colorScheme.primary
  val haptics = LocalHapticFeedback.current
  val coroutineScope = rememberCoroutineScope()
  val currentActions by rememberUpdatedState(actions)
  var activeButtons by remember { mutableStateOf<Set<GamepadButton>>(emptySet()) }
  var touchOffset by remember { mutableStateOf<Offset?>(null) }

  val upPressed = GamepadButton.DPAD_UP in activeButtons
  val downPressed = GamepadButton.DPAD_DOWN in activeButtons
  val leftPressed = GamepadButton.DPAD_LEFT in activeButtons
  val rightPressed = GamepadButton.DPAD_RIGHT in activeButtons

  val upLeftActive = upPressed && leftPressed
  val upRightActive = upPressed && rightPressed
  val downLeftActive = downPressed && leftPressed
  val downRightActive = downPressed && rightPressed

  val cornerOffset = size * 0.27f

  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(housingColor)
      .border(1.dp, outlineColor, CircleShape)
      .testTag("bt_gamepad_dpad")
      .pointerInput(Unit) {
        var releaseJob: Job? = null
        awaitEachGesture {
          val down = awaitFirstDown(requireUnconsumed = false)
          down.consume()
          releaseJob?.cancel()
          releaseJob = null
          val sizePx = this.size.width.toFloat()
          val previouslyActive = activeButtons
          val initial = gamepadDpadButtonsForOffset(
            offset = down.position,
            sizePx = sizePx,
            allowOutsideBounds = false,
            previousButtons = emptySet(),
          )
          var current: Set<GamepadButton> = initial
          activeButtons = initial
          touchOffset = down.position
          if (initial != previouslyActive) {
            currentActions.updateDpad(previouslyActive, initial)
          }
          if (initial.isNotEmpty()) {
            haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
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
            touchOffset = change.position
            val next = gamepadDpadButtonsForOffset(
              offset = change.position,
              sizePx = sizePx,
              allowOutsideBounds = true,
              previousButtons = current,
            )
            if (next != current) {
              currentActions.updateDpad(current, next)
              current = next
              activeButtons = next
              if (next.isNotEmpty()) {
                haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
              }
            }
          }

          touchOffset = null
          val releasedButtons = current
          if (releasedButtons.isNotEmpty()) {
            val elapsed = System.currentTimeMillis() - pressStartTime
            if (elapsed < 45L) {
              releaseJob = coroutineScope.launch {
                delay(45L - elapsed)
                activeButtons = emptySet()
                currentActions.updateDpad(releasedButtons, emptySet())
              }
            } else {
              activeButtons = emptySet()
              currentActions.updateDpad(releasedButtons, emptySet())
            }
          } else {
            activeButtons = emptySet()
          }
        }
      },
    contentAlignment = Alignment.Center,
  ) {
    // Draw cross background, diagonal bridging arcs, and Option B dynamic rim/thumb indicator
    Canvas(modifier = Modifier.fillMaxSize()) {
      val center = Offset(this.size.width / 2f, this.size.height / 2f)
      val outerRadius = this.size.minDimension / 2f
      val bridgeRadius = outerRadius * 0.68f
      val bridgeTopLeft = Offset(center.x - bridgeRadius, center.y - bridgeRadius)
      val bridgeSize = Size(bridgeRadius * 2f, bridgeRadius * 2f)

      // Subtle guide ring connecting cardinal arms through the diagonal corner chevrons
      drawCircle(
        color = bridgeTrackColor,
        radius = bridgeRadius,
        center = center,
        style = Stroke(width = 1.5.dp.toPx()),
      )

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

      // Active diagonal bridge bands connecting the two lit cardinal arms through the corner chevron
      val bridgeStroke = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
      if (upRightActive) {
        drawArc(
          color = activeBridgeColor,
          startAngle = -90f,
          sweepAngle = 90f,
          useCenter = false,
          topLeft = bridgeTopLeft,
          size = bridgeSize,
          style = bridgeStroke,
        )
      }
      if (downRightActive) {
        drawArc(
          color = activeBridgeColor,
          startAngle = 0f,
          sweepAngle = 90f,
          useCenter = false,
          topLeft = bridgeTopLeft,
          size = bridgeSize,
          style = bridgeStroke,
        )
      }
      if (downLeftActive) {
        drawArc(
          color = activeBridgeColor,
          startAngle = 90f,
          sweepAngle = 90f,
          useCenter = false,
          topLeft = bridgeTopLeft,
          size = bridgeSize,
          style = bridgeStroke,
        )
      }
      if (upLeftActive) {
        drawArc(
          color = activeBridgeColor,
          startAngle = 180f,
          sweepAngle = 90f,
          useCenter = false,
          topLeft = bridgeTopLeft,
          size = bridgeSize,
          style = bridgeStroke,
        )
      }

      // Option B: Dynamic outer rim arc & floating thumb contact indicator while pressing/rolling
      val currentTouch = touchOffset
      val activeSector = DpadSector.entries.firstOrNull { it.buttons == activeButtons }
      if (activeSector != null) {
        val contactAngleDeg = if (currentTouch != null) {
          val dx = currentTouch.x - center.x
          val dy = currentTouch.y - center.y
          if (sqrt(dx * dx + dy * dy) >= outerRadius * DPAD_DEAD_ZONE_RATIO) {
            Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
          } else {
            activeSector.centerAngleDeg
          }
        } else {
          activeSector.centerAngleDeg
        }

        val rimInset = 3.dp.toPx()
        drawArc(
          color = primaryColor,
          startAngle = contactAngleDeg - 22.5f,
          sweepAngle = 45f,
          useCenter = false,
          topLeft = Offset(rimInset, rimInset),
          size = Size(this.size.width - rimInset * 2f, this.size.height - rimInset * 2f),
          style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round),
        )
      }

      if (currentTouch != null && activeButtons.isNotEmpty()) {
        val dx = currentTouch.x - center.x
        val dy = currentTouch.y - center.y
        val dist = sqrt(dx * dx + dy * dy)
        val maxContactRadius = outerRadius * 0.76f
        val clampedCenter = if (dist > maxContactRadius && dist > 0f) {
          Offset(
            center.x + (dx / dist) * maxContactRadius,
            center.y + (dy / dist) * maxContactRadius,
          )
        } else {
          currentTouch
        }
        drawCircle(
          color = primaryColor.copy(alpha = 0.22f),
          radius = 13.dp.toPx(),
          center = clampedCenter,
        )
        drawCircle(
          color = primaryColor.copy(alpha = 0.55f),
          radius = 6.dp.toPx(),
          center = clampedCenter,
        )
      }
    }

    // 4 Diagonal Corner Chevrons that bridge adjacent cardinal directions
    GamepadDPadCornerChevron(
      label = "Up Left",
      buttons = DpadSector.UP_LEFT.buttons,
      dirX = -1f,
      dirY = -1f,
      isPressed = upLeftActive,
      isAdjacentPressed = upPressed || leftPressed,
      actions = actions,
      modifier = Modifier.offset(x = -cornerOffset, y = -cornerOffset),
      tag = "bt_gamepad_dpad_up_left",
    )
    GamepadDPadCornerChevron(
      label = "Up Right",
      buttons = DpadSector.UP_RIGHT.buttons,
      dirX = 1f,
      dirY = -1f,
      isPressed = upRightActive,
      isAdjacentPressed = upPressed || rightPressed,
      actions = actions,
      modifier = Modifier.offset(x = cornerOffset, y = -cornerOffset),
      tag = "bt_gamepad_dpad_up_right",
    )
    GamepadDPadCornerChevron(
      label = "Down Left",
      buttons = DpadSector.DOWN_LEFT.buttons,
      dirX = -1f,
      dirY = 1f,
      isPressed = downLeftActive,
      isAdjacentPressed = downPressed || leftPressed,
      actions = actions,
      modifier = Modifier.offset(x = -cornerOffset, y = cornerOffset),
      tag = "bt_gamepad_dpad_down_left",
    )
    GamepadDPadCornerChevron(
      label = "Down Right",
      buttons = DpadSector.DOWN_RIGHT.buttons,
      dirX = 1f,
      dirY = 1f,
      isPressed = downRightActive,
      isAdjacentPressed = downPressed || rightPressed,
      actions = actions,
      modifier = Modifier.offset(x = cornerOffset, y = cornerOffset),
      tag = "bt_gamepad_dpad_down_right",
    )

    GamepadDPadArrow(
      icon = Icons.Default.KeyboardArrowUp,
      label = "Up",
      button = GamepadButton.DPAD_UP,
      isPressed = upPressed,
      actions = actions,
      alignment = Alignment.TopCenter,
      tag = "bt_gamepad_dpad_up",
    )
    GamepadDPadArrow(
      icon = Icons.Default.KeyboardArrowDown,
      label = "Down",
      button = GamepadButton.DPAD_DOWN,
      isPressed = downPressed,
      actions = actions,
      alignment = Alignment.BottomCenter,
      tag = "bt_gamepad_dpad_down",
    )
    GamepadDPadArrow(
      icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
      label = "Left",
      button = GamepadButton.DPAD_LEFT,
      isPressed = leftPressed,
      actions = actions,
      alignment = Alignment.CenterStart,
      tag = "bt_gamepad_dpad_left",
    )
    GamepadDPadArrow(
      icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
      label = "Right",
      button = GamepadButton.DPAD_RIGHT,
      isPressed = rightPressed,
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
private fun BoxScope.GamepadDPadCornerChevron(
  label: String,
  buttons: Set<GamepadButton>,
  dirX: Float,
  dirY: Float,
  isPressed: Boolean,
  isAdjacentPressed: Boolean,
  actions: GamepadInputActions,
  modifier: Modifier = Modifier,
  tag: String,
) {
  val containerColor = when {
    isPressed -> MaterialTheme.colorScheme.primaryContainer
    isAdjacentPressed -> MaterialTheme.colorScheme.surfaceContainerHighest
    else -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.75f)
  }
  val borderColor = when {
    isPressed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
    isAdjacentPressed -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
  }
  val chevronColor = when {
    isPressed -> MaterialTheme.colorScheme.onPrimaryContainer
    isAdjacentPressed -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
  }

  Box(
    modifier = modifier
      .align(Alignment.Center)
      .size(28.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(containerColor)
      .border(1.dp, borderColor, RoundedCornerShape(8.dp))
      .semantics {
        role = Role.Button
        contentDescription = label
        onClick {
          actions.updateDpad(emptySet(), buttons)
          actions.updateDpad(buttons, emptySet())
          true
        }
      }
      .testTag(tag),
    contentAlignment = Alignment.Center,
  ) {
    Canvas(modifier = Modifier.size(14.dp)) {
      val cx = this.size.width / 2f
      val cy = this.size.height / 2f
      val tipOffset = this.size.width * 0.28f
      val armLength = this.size.width * 0.44f
      val tipX = cx + dirX * tipOffset
      val tipY = cy + dirY * tipOffset
      val path = Path().apply {
        moveTo(tipX - dirX * armLength, tipY)
        lineTo(tipX, tipY)
        lineTo(tipX, tipY - dirY * armLength)
      }
      drawPath(
        path = path,
        color = chevronColor,
        style = Stroke(
          width = if (isPressed) 2.6.dp.toPx() else 2.dp.toPx(),
          cap = StrokeCap.Round,
          join = StrokeJoin.Round,
        ),
      )
    }
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
