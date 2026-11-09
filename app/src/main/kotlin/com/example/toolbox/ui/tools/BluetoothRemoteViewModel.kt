package com.example.toolbox.ui.tools

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class BtDeviceKind { TV, COMPUTER, PHONE, AUDIO, OTHER }

data class BtDeviceInfo(
  val address: String,
  val name: String,
  val isBonded: Boolean,
  val kind: BtDeviceKind,
  val rssi: Int? = null,
)

sealed interface RemoteConnection {
  data object Disconnected : RemoteConnection
  data class Connecting(val device: BtDeviceInfo, val pairing: Boolean = false) : RemoteConnection
  data class Connected(val device: BtDeviceInfo) : RemoteConnection
}

enum class BluetoothSupport { SUPPORTED, NO_ADAPTER, API_TOO_OLD }

data class BluetoothRemoteUiState(
  val support: BluetoothSupport = BluetoothSupport.SUPPORTED,
  val isBluetoothEnabled: Boolean = false,
  val isHidRegistered: Boolean = false,
  val isScanning: Boolean = false,
  val devices: List<BtDeviceInfo> = emptyList(),
  val connection: RemoteConnection = RemoteConnection.Disconnected,
  val message: String? = null,
  val messageDeviceAddress: String? = null,
)

/**
 * Owns the Bluetooth HID session for the Bluetooth Remote tool: device discovery,
 * pairing/connecting, and translating remote/keyboard/mouse input into HID reports.
 *
 * Callers must have been granted the runtime Bluetooth permissions before calling [start].
 */
@SuppressLint("MissingPermission")
class BluetoothRemoteViewModel(application: Application) : AndroidViewModel(application) {

  private val context: Context get() = getApplication()
  private val adapter: BluetoothAdapter? =
    (application.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

  private val _uiState = MutableStateFlow(
    BluetoothRemoteUiState(
      support = when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.P -> BluetoothSupport.API_TOO_OLD
        adapter == null -> BluetoothSupport.NO_ADAPTER
        else -> BluetoothSupport.SUPPORTED
      },
    ),
  )
  val uiState: StateFlow<BluetoothRemoteUiState> = _uiState.asStateFlow()

  private var session: Any? = null // BluetoothHidSession on API 28+
  private var started = false
  private var receiverRegistered = false
  private val discovered = LinkedHashMap<String, BtDeviceInfo>()

  private var connectedDevice: BluetoothDevice? = null
  private var pendingConnect: BluetoothDevice? = null
  private var connectTimeoutJob: Job? = null

  private val sendMutex = Mutex()
  private var mouseButtons = 0
  private var mouseRemainderX = 0f
  private var mouseRemainderY = 0f
  private var scrollRemainder = 0f

  private val hidListener = object : BluetoothHidSessionListener {
    override fun onRegistrationChanged(registered: Boolean) {
      _uiState.update { it.copy(isHidRegistered = registered) }
      if (registered) {
        pendingConnect?.takeIf { it.bondState == BluetoothDevice.BOND_BONDED }?.let { device ->
          pendingConnect = null
          requestHidConnect(device)
        }
      }
    }

    override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
      handleConnectionState(device, state)
    }

    override fun onError(message: String) {
      postMessage(message)
      cancelConnecting()
    }
  }

  private val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      when (intent.action) {
        BluetoothAdapter.ACTION_STATE_CHANGED -> onAdapterStateChanged()
        BluetoothAdapter.ACTION_DISCOVERY_STARTED -> _uiState.update { it.copy(isScanning = true) }
        BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> _uiState.update { it.copy(isScanning = false) }
        BluetoothDevice.ACTION_FOUND -> {
          val device = intent.bluetoothDevice() ?: return
          val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
            .takeIf { it != Short.MIN_VALUE }?.toInt()
          val info = device.toInfo(rssi) ?: return
          discovered[info.address] = info
          refreshDevices()
        }
        BluetoothDevice.ACTION_NAME_CHANGED -> {
          val device = intent.bluetoothDevice() ?: return
          device.toInfo(discovered[device.address]?.rssi)?.let { discovered[it.address] = it }
          refreshDevices()
        }
        BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
          val device = intent.bluetoothDevice() ?: return
          onBondStateChanged(device)
          refreshDevices()
        }
      }
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Lifecycle
  // ---------------------------------------------------------------------------------------------

  /** Begins listening to Bluetooth state and registers the HID app. Safe to call repeatedly. */
  fun start() {
    if (started || _uiState.value.support != BluetoothSupport.SUPPORTED) {
      refreshDevices()
      return
    }
    started = true
    if (!receiverRegistered) {
      val filter = IntentFilter().apply {
        addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        addAction(BluetoothDevice.ACTION_FOUND)
        addAction(BluetoothDevice.ACTION_NAME_CHANGED)
        addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
      }
      ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
      receiverRegistered = true
    }
    onAdapterStateChanged()
  }

  override fun onCleared() {
    super.onCleared()
    runCatching { adapter?.cancelDiscovery() }
    if (receiverRegistered) {
      runCatching { context.unregisterReceiver(receiver) }
      receiverRegistered = false
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      (session as? BluetoothHidSession)?.close()
    }
    session = null
  }

  private fun onAdapterStateChanged() {
    val enabled = runCatching { adapter?.isEnabled == true }.getOrDefault(false)
    _uiState.update { it.copy(isBluetoothEnabled = enabled) }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
    if (enabled) {
      val hid = (session as? BluetoothHidSession)
        ?: BluetoothHidSession(context, adapter!!, hidListener).also { session = it }
      hid.open()
      // A host (e.g. a TV that already knows this phone) may connect immediately.
      hid.connectedDevices().firstOrNull()?.let {
        handleConnectionState(it, BluetoothProfile.STATE_CONNECTED)
      }
    } else {
      (session as? BluetoothHidSession)?.close()
      connectedDevice = null
      pendingConnect = null
      discovered.clear()
      _uiState.update {
        it.copy(isHidRegistered = false, isScanning = false, connection = RemoteConnection.Disconnected)
      }
    }
    refreshDevices()
  }

  // ---------------------------------------------------------------------------------------------
  // Discovery
  // ---------------------------------------------------------------------------------------------

  fun startScan() {
    val bt = adapter ?: return
    if (!bt.isEnabled) return
    discovered.clear()
    refreshDevices()
    runCatching {
      if (bt.isDiscovering) bt.cancelDiscovery()
      if (!bt.startDiscovery()) postMessage("Couldn't start scanning. Check that Location / Nearby devices access is allowed.")
    }.onFailure { postMessage("Scanning isn't permitted: ${it.message}") }
  }

  fun stopScan() {
    runCatching { adapter?.cancelDiscovery() }
    _uiState.update { it.copy(isScanning = false) }
  }

  fun refreshDevices() {
    val bonded = runCatching { adapter?.bondedDevices.orEmpty() }.getOrDefault(emptySet())
      .mapNotNull { it.toInfo(discovered[it.address]?.rssi) }
    val bondedAddresses = bonded.map { it.address }.toSet()
    val others = discovered.values.filter { it.address !in bondedAddresses }
    val sorted = (bonded + others).sortedWith(
      compareByDescending<BtDeviceInfo> { it.kind == BtDeviceKind.TV }
        .thenByDescending { it.isBonded }
        .thenBy { it.name.lowercase() },
    )
    _uiState.update { it.copy(devices = sorted) }
  }

  fun dismissMessage() {
    _uiState.update { it.copy(message = null, messageDeviceAddress = null) }
  }

  private fun postMessage(message: String, deviceAddress: String? = null) {
    val targetAddress = deviceAddress
      ?: pendingConnect?.address
      ?: (_uiState.value.connection as? RemoteConnection.Connecting)?.device?.address
      ?: connectedDevice?.address
    _uiState.update { it.copy(message = message, messageDeviceAddress = targetAddress) }
  }

  // ---------------------------------------------------------------------------------------------
  // Connection
  // ---------------------------------------------------------------------------------------------

  fun connect(address: String) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
    val bt = adapter ?: return
    val device = runCatching { bt.getRemoteDevice(address) }.getOrNull() ?: return
    val info = device.toInfo(null) ?: BtDeviceInfo(address, address, false, BtDeviceKind.OTHER)
    stopScan()

    val needsPairing = device.bondState != BluetoothDevice.BOND_BONDED
    _uiState.update { it.copy(connection = RemoteConnection.Connecting(info, pairing = needsPairing), message = null, messageDeviceAddress = null) }
    startConnectTimeout()

    if (needsPairing) {
      pendingConnect = device
      if (device.bondState != BluetoothDevice.BOND_BONDING && !device.createBond()) {
        postMessage("Couldn't start pairing with ${info.name}.")
        cancelConnecting()
      }
      return
    }

    val hid = session as? BluetoothHidSession
    if (hid == null || !hid.isRegistered) {
      pendingConnect = device
      hid?.open()
    } else {
      requestHidConnect(device)
    }
  }

  fun disconnect() {
    pendingConnect = null
    connectTimeoutJob?.cancel()
    releaseAllInput()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      val hid = session as? BluetoothHidSession
      connectedDevice?.let { hid?.disconnect(it) }
      (_uiState.value.connection as? RemoteConnection.Connecting)?.let { connecting ->
        runCatching { adapter?.getRemoteDevice(connecting.device.address) }.getOrNull()?.let { hid?.disconnect(it) }
      }
    }
    connectedDevice = null
    _uiState.update { it.copy(connection = RemoteConnection.Disconnected) }
  }

  private fun requestHidConnect(device: BluetoothDevice) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
    val hid = session as? BluetoothHidSession ?: return
    if (!hid.connect(device)) {
      postMessage("Couldn't connect to ${device.safeName()}. Try adding this phone from the TV's Bluetooth settings instead.")
      cancelConnecting()
    }
  }

  private fun onBondStateChanged(device: BluetoothDevice) {
    val pending = pendingConnect ?: return
    if (pending.address != device.address) return
    when (device.bondState) {
      BluetoothDevice.BOND_BONDED -> {
        val info = device.toInfo(null) ?: return
        _uiState.update { it.copy(connection = RemoteConnection.Connecting(info, pairing = false)) }
        startConnectTimeout()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
          (session as? BluetoothHidSession)?.isRegistered == true
        ) {
          pendingConnect = null
          requestHidConnect(device)
        }
      }
      BluetoothDevice.BOND_NONE -> {
        pendingConnect = null
        postMessage("Pairing with ${device.safeName()} failed or was cancelled.")
        cancelConnecting()
      }
    }
  }

  private fun handleConnectionState(device: BluetoothDevice, state: Int) {
    when (state) {
      BluetoothProfile.STATE_CONNECTED -> {
        connectTimeoutJob?.cancel()
        pendingConnect = null
        connectedDevice = device
        stopScan()
        val info = device.toInfo(null) ?: BtDeviceInfo(device.address, device.address, true, BtDeviceKind.OTHER)
        _uiState.update { it.copy(connection = RemoteConnection.Connected(info), message = null) }
      }
      BluetoothProfile.STATE_CONNECTING -> {
        if (_uiState.value.connection is RemoteConnection.Disconnected) {
          device.toInfo(null)?.let { info ->
            _uiState.update { it.copy(connection = RemoteConnection.Connecting(info)) }
          }
        }
      }
      BluetoothProfile.STATE_DISCONNECTED -> {
        val current = _uiState.value.connection
        val currentAddress = when (current) {
          is RemoteConnection.Connected -> current.device.address
          is RemoteConnection.Connecting -> current.device.address
          RemoteConnection.Disconnected -> null
        }
        if (currentAddress != device.address) return
        if (current is RemoteConnection.Connecting && current.pairing) return // still pairing
        connectTimeoutJob?.cancel()
        connectedDevice = null
        mouseButtons = 0
        val msg = if (current is RemoteConnection.Connecting) {
          "${device.safeName()} refused the connection. On the TV, open Settings → Remotes & Accessories → Add accessory while this phone is discoverable."
        } else {
          "Disconnected from ${device.safeName()}."
        }
        _uiState.update { it.copy(connection = RemoteConnection.Disconnected, message = msg, messageDeviceAddress = device.address) }
      }
    }
  }

  private fun startConnectTimeout() {
    connectTimeoutJob?.cancel()
    connectTimeoutJob = viewModelScope.launch {
      delay(CONNECT_TIMEOUT_MS)
      val currentConnecting = _uiState.value.connection as? RemoteConnection.Connecting
      if (currentConnecting != null) {
        postMessage(
          "Timed out connecting. Make sure the TV is on and nearby, or add this phone from the TV's Remotes & Accessories settings.",
          currentConnecting.device.address,
        )
        cancelConnecting()
      }
    }
  }

  private fun cancelConnecting() {
    connectTimeoutJob?.cancel()
    pendingConnect = null
    if (_uiState.value.connection is RemoteConnection.Connecting) {
      _uiState.update { it.copy(connection = RemoteConnection.Disconnected) }
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Input -> HID reports
  // ---------------------------------------------------------------------------------------------

  private fun send(reportId: Int, data: ByteArray): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
    val device = connectedDevice ?: return false
    return (session as? BluetoothHidSession)?.sendReport(device, reportId, data) ?: false
  }

  fun keyDown(keyCode: Int, modifiers: Int = 0) {
    send(HidProtocol.REPORT_ID_KEYBOARD, HidProtocol.keyboardReport(modifiers, keyCode))
  }

  fun keyUp() {
    send(HidProtocol.REPORT_ID_KEYBOARD, HidProtocol.emptyKeyboardReport())
  }

  fun consumerDown(key: ConsumerKey) {
    send(HidProtocol.REPORT_ID_CONSUMER, HidProtocol.consumerReport(key.usage))
  }

  fun consumerUp() {
    send(HidProtocol.REPORT_ID_CONSUMER, HidProtocol.consumerReport(0))
  }

  /** Taps a single key (press + release), serialised with typed text. */
  fun tapKey(keyCode: Int, modifiers: Int = 0) {
    viewModelScope.launch {
      sendMutex.withLock { strokeLocked(HidKeyStroke(keyCode, modifiers)) }
    }
  }

  /** Types [text] on the host, preceded by [backspaces] presses of Backspace. */
  fun typeText(text: String, backspaces: Int = 0) {
    if (text.isEmpty() && backspaces <= 0) return
    viewModelScope.launch {
      sendMutex.withLock {
        repeat(backspaces) { strokeLocked(HidKeyStroke(HidKey.BACKSPACE)) }
        text.forEach { c -> HidKeyMap.strokeFor(c)?.let { strokeLocked(it) } }
      }
    }
  }

  private suspend fun strokeLocked(stroke: HidKeyStroke) {
    keyDown(stroke.keyCode, stroke.modifiers)
    delay(KEY_STROKE_DELAY_MS)
    keyUp()
    delay(KEY_STROKE_DELAY_MS)
  }

  /** Moves the cursor by a fractional delta; sub-pixel remainders are carried to the next call. */
  fun moveMouse(dx: Float, dy: Float) {
    mouseRemainderX += dx
    mouseRemainderY += dy
    val ix = mouseRemainderX.toInt()
    val iy = mouseRemainderY.toInt()
    if (ix == 0 && iy == 0) return
    mouseRemainderX -= ix
    mouseRemainderY -= iy
    HidProtocol.splitMouseMovement(ix, iy).forEach { (sx, sy) ->
      send(HidProtocol.REPORT_ID_MOUSE, HidProtocol.mouseReport(mouseButtons, sx, sy))
    }
  }

  fun scroll(amount: Float) {
    scrollRemainder += amount
    val steps = scrollRemainder.toInt()
    if (steps == 0) return
    scrollRemainder -= steps
    send(HidProtocol.REPORT_ID_MOUSE, HidProtocol.mouseReport(mouseButtons, 0, 0, steps))
  }

  fun mouseButton(button: Int, pressed: Boolean) {
    mouseButtons = if (pressed) mouseButtons or button else mouseButtons and button.inv()
    send(HidProtocol.REPORT_ID_MOUSE, HidProtocol.mouseReport(mouseButtons, 0, 0))
  }

  fun mouseClick(button: Int = HidProtocol.MOUSE_BUTTON_LEFT) {
    viewModelScope.launch {
      mouseButton(button, true)
      delay(KEY_STROKE_DELAY_MS)
      mouseButton(button, false)
    }
  }

  private fun releaseAllInput() {
    if (connectedDevice == null) return
    keyUp()
    consumerUp()
    mouseButtons = 0
    send(HidProtocol.REPORT_ID_MOUSE, HidProtocol.mouseReport(0, 0, 0))
  }

  // ---------------------------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------------------------

  private fun Intent.bluetoothDevice(): BluetoothDevice? =
    IntentCompat.getParcelableExtra(this, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)

  private fun BluetoothDevice.safeName(): String =
    runCatching { name }.getOrNull()?.takeIf { it.isNotBlank() } ?: address

  private fun BluetoothDevice.toInfo(rssi: Int?): BtDeviceInfo? {
    val deviceName = runCatching { name }.getOrNull()?.takeIf { it.isNotBlank() } ?: return null
    val bonded = runCatching { bondState == BluetoothDevice.BOND_BONDED }.getOrDefault(false)
    val btClass = runCatching { bluetoothClass }.getOrNull()
    return BtDeviceInfo(
      address = address,
      name = deviceName,
      isBonded = bonded,
      kind = classifyDevice(deviceName, btClass?.majorDeviceClass, btClass?.deviceClass),
      rssi = rssi,
    )
  }

  companion object {
    private const val CONNECT_TIMEOUT_MS = 30_000L
    private const val KEY_STROKE_DELAY_MS = 12L

    private val TV_NAME_HINTS = listOf(
      "tv", "shield", "chromecast", "fire", "bravia", "streamer", "mibox", "projector",
    )

    fun classifyDevice(name: String, majorClass: Int?, deviceClass: Int?): BtDeviceKind {
      val lower = name.lowercase()
      val videoClasses = setOf(
        BluetoothClass.Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER,
        BluetoothClass.Device.AUDIO_VIDEO_VIDEO_MONITOR,
        BluetoothClass.Device.AUDIO_VIDEO_SET_TOP_BOX,
        BluetoothClass.Device.AUDIO_VIDEO_UNCATEGORIZED,
        BluetoothClass.Device.AUDIO_VIDEO_VIDEO_CONFERENCING,
      )
      return when {
        deviceClass != null && deviceClass in videoClasses -> BtDeviceKind.TV
        TV_NAME_HINTS.any { lower.contains(it) } -> BtDeviceKind.TV
        majorClass == BluetoothClass.Device.Major.COMPUTER -> BtDeviceKind.COMPUTER
        majorClass == BluetoothClass.Device.Major.PHONE -> BtDeviceKind.PHONE
        majorClass == BluetoothClass.Device.Major.AUDIO_VIDEO -> BtDeviceKind.AUDIO
        else -> BtDeviceKind.OTHER
      }
    }
  }
}
