package com.example.toolbox.ui.tools

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppSdpSettings
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

/** Callbacks from [BluetoothHidSession], delivered on the main thread. */
interface BluetoothHidSessionListener {
  fun onRegistrationChanged(registered: Boolean)
  fun onConnectionStateChanged(device: BluetoothDevice, state: Int)
  fun onError(message: String)
}

/**
 * Thin wrapper around [BluetoothHidDevice] that registers the phone as a composite
 * keyboard / consumer-control / mouse HID peripheral and forwards reports to a host.
 *
 * All callbacks are delivered on the main thread.
 *
 * Callers must hold `BLUETOOTH_CONNECT` (Android 12+) before invoking any method.
 */
@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.P)
class BluetoothHidSession(
  private val context: Context,
  private val adapter: BluetoothAdapter,
  private val listener: BluetoothHidSessionListener,
) {
  private val mainExecutor = ContextCompat.getMainExecutor(context)
  private var hidDevice: BluetoothHidDevice? = null
  private var proxyRequested = false

  var isRegistered: Boolean = false
    private set

  private val sdpSettings = BluetoothHidDeviceAppSdpSettings(
    "Toolbox Remote",
    "Toolbox Bluetooth TV remote, keyboard and mouse",
    "Toolbox",
    BluetoothHidDevice.SUBCLASS1_COMBO,
    HidProtocol.REPORT_DESCRIPTOR,
  )

  private val callback = object : BluetoothHidDevice.Callback() {
    override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
      isRegistered = registered
      listener.onRegistrationChanged(registered)
    }

    override fun onConnectionStateChanged(device: BluetoothDevice, state: Int) {
      listener.onConnectionStateChanged(device, state)
    }

    override fun onGetReport(device: BluetoothDevice, type: Byte, id: Byte, bufferSize: Int) {
      val hid = hidDevice ?: return
      val size = when (id.toInt()) {
        HidProtocol.REPORT_ID_KEYBOARD -> HidProtocol.KEYBOARD_REPORT_SIZE
        HidProtocol.REPORT_ID_CONSUMER -> HidProtocol.CONSUMER_REPORT_SIZE
        HidProtocol.REPORT_ID_MOUSE -> HidProtocol.MOUSE_REPORT_SIZE
        else -> null
      }
      if (type == BluetoothHidDevice.REPORT_TYPE_INPUT && size != null) {
        hid.replyReport(device, type, id, ByteArray(size))
      } else {
        hid.reportError(device, BluetoothHidDevice.ERROR_RSP_INVALID_RPT_ID)
      }
    }

    override fun onSetReport(device: BluetoothDevice, type: Byte, id: Byte, data: ByteArray?) {
      // Hosts may push keyboard LED state; acknowledge so they don't time out.
      hidDevice?.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS)
    }
  }

  private val serviceListener = object : BluetoothProfile.ServiceListener {
    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
      if (profile != BluetoothProfile.HID_DEVICE) return
      val hid = proxy as BluetoothHidDevice
      hidDevice = hid
      val ok = runCatching {
        hid.registerApp(sdpSettings, null, null, mainExecutor, callback)
      }.getOrDefault(false)
      if (!ok) {
        listener.onError("Couldn't register as a Bluetooth remote. Another app may already be acting as a Bluetooth keyboard.")
      }
    }

    override fun onServiceDisconnected(profile: Int) {
      if (profile != BluetoothProfile.HID_DEVICE) return
      hidDevice = null
      proxyRequested = false
      if (isRegistered) {
        isRegistered = false
        listener.onRegistrationChanged(false)
      }
    }
  }

  /** Binds to the HID Device profile service and registers the HID app. Idempotent. */
  fun open() {
    if (proxyRequested) return
    proxyRequested = adapter.getProfileProxy(context, serviceListener, BluetoothProfile.HID_DEVICE)
    if (!proxyRequested) {
      listener.onError("This phone doesn't support acting as a Bluetooth keyboard/remote (HID Device profile unavailable).")
    }
  }

  fun close() {
    val hid = hidDevice
    if (hid != null) {
      runCatching { hid.connectedDevices.forEach { hid.disconnect(it) } }
      runCatching { hid.unregisterApp() }
      runCatching { adapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, hid) }
    }
    hidDevice = null
    proxyRequested = false
    isRegistered = false
  }

  fun connect(device: BluetoothDevice): Boolean =
    runCatching { hidDevice?.connect(device) ?: false }.getOrDefault(false)

  fun disconnect(device: BluetoothDevice): Boolean =
    runCatching { hidDevice?.disconnect(device) ?: false }.getOrDefault(false)

  fun connectedDevices(): List<BluetoothDevice> =
    runCatching { hidDevice?.connectedDevices.orEmpty() }.getOrDefault(emptyList())

  fun sendReport(device: BluetoothDevice, reportId: Int, data: ByteArray): Boolean =
    runCatching { hidDevice?.sendReport(device, reportId, data) ?: false }.getOrDefault(false)
}
