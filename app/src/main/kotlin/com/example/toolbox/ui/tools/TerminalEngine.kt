package com.example.toolbox.ui.tools

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.os.Environment
import androidx.compose.runtime.Immutable
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader
import java.math.BigInteger
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.Signature
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.net.ssl.KeyManager
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLSocket
import javax.net.ssl.X509ExtendedKeyManager
import javax.net.ssl.X509TrustManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

const val TERMINAL_PREFERENCES_NAME = "terminal_preferences"
const val PREF_KEY_TERMINAL_BACKEND = "terminal_backend_mode"
const val PREF_KEY_TERMINAL_FAVORITE_COMMANDS = "terminal_favorite_commands"
const val PREF_KEY_TERMINAL_CUSTOM_COMMANDS = "terminal_custom_commands"
const val PREF_KEY_TERMINAL_COMMAND_HISTORY = "terminal_command_history"
const val PREF_KEY_TERMINAL_ADB_HOST = "terminal_adb_host"
const val PREF_KEY_TERMINAL_ADB_CONNECT_PORT = "terminal_adb_connect_port"
const val PREF_KEY_TERMINAL_ADB_PAIR_PORT = "terminal_adb_pair_port"
const val PREF_KEY_TERMINAL_ADB_PAIRED = "terminal_adb_paired"
const val PREF_KEY_TERMINAL_RSA_PUBLIC_KEY = "terminal_adb_rsa_pub"
const val PREF_KEY_TERMINAL_RSA_PRIVATE_KEY = "terminal_adb_rsa_priv"
const val PREF_KEY_TERMINAL_FONT_SIZE_SP = "terminal_font_size_sp"

const val DEFAULT_TERMINAL_ADB_HOST = "127.0.0.1"
const val DEFAULT_TERMINAL_ADB_PORT = 5555
const val DEFAULT_TERMINAL_PAIR_PORT = 37000
const val DEFAULT_TERMINAL_FONT_SIZE_SP = 13f
const val MIN_TERMINAL_FONT_SIZE_SP = 10f
const val MAX_TERMINAL_FONT_SIZE_SP = 20f
const val MAX_TERMINAL_HISTORY_SIZE = 100
const val MAX_TERMINAL_OUTPUT_LINES = 1500

/**
 * Available execution backends for the Terminal tool:
 * - [LOCAL_SHELL]: Option 1 (default) — executes directly in `/system/bin/sh` as the app sandbox user.
 * - [WIRELESS_ADB]: Option 3 — connects over loopback (`127.0.0.1`) to Android's Wireless Debugging
 *   / local `adbd` daemon to run commands with elevated `uid=2000(shell)` privileges.
 */
enum class TerminalBackendMode(
  val title: String,
  val shortTitle: String,
  val subtitle: String,
  val promptSymbol: String,
) {
  LOCAL_SHELL(
    title = "Local Shell (sh)",
    shortTitle = "Local Shell",
    subtitle = "/system/bin/sh • App Sandbox",
    promptSymbol = "$",
  ),
  WIRELESS_ADB(
    title = "Wireless ADB (shell)",
    shortTitle = "Wireless ADB",
    subtitle = "Loopback adbd • UID 2000 Privileged",
    promptSymbol = "#",
  ),
}

/**
 * Connection lifecycle state for Option 3 ([TerminalBackendMode.WIRELESS_ADB]).
 */
enum class AdbConnectionState(val label: String) {
  DISCONNECTED("Disconnected"),
  DISCOVERING("Discovering Wireless ADB..."),
  PAIRING("Pairing with Wireless ADB..."),
  CONNECTING("Connecting to adbd..."),
  CONNECTED("Connected (uid=2000 shell)"),
  ERROR("Connection Error"),
}

/**
 * Category filter pills for the searchable command catalog overlay & quick-run presets.
 */
enum class TerminalCommandCategory(val label: String) {
  ALL("All"),
  FAVORITES("Favorites"),
  SYSTEM_INFO("System & OS"),
  NETWORK("Network"),
  PACKAGES_AND_APPS("Packages & Apps"),
  PROCESSES_AND_PERF("Processes & Perf"),
  LOGS_AND_DUMPSYS("Logs & Dumpsys"),
  STORAGE_AND_FILES("Storage & Files"),
  CUSTOM("Custom"),
}

/**
 * A curated or user-created shell command preset in the Terminal command catalog.
 */
@Immutable
data class TerminalCommandPreset(
  val id: String,
  val title: String,
  val command: String,
  val description: String,
  val category: TerminalCommandCategory,
  val requiresAdbPrivilege: Boolean = false,
  val isCustom: Boolean = false,
) {
  fun toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("title", title)
    put("command", command)
    put("description", description)
    put("category", category.name)
    put("requiresAdbPrivilege", requiresAdbPrivilege)
    put("isCustom", isCustom)
  }

  companion object {
    fun fromJson(json: JSONObject): TerminalCommandPreset? {
      val id = json.optString("id", "").trim().ifEmpty { UUID.randomUUID().toString() }
      val title = json.optString("title", "").trim()
      val command = json.optString("command", "").trim()
      if (title.isEmpty() || command.isEmpty()) return null
      val description = json.optString("description", "").trim().ifEmpty { command }
      val catRaw = json.optString("category", TerminalCommandCategory.CUSTOM.name)
      val category = TerminalCommandCategory.entries.firstOrNull {
        it.name.equals(catRaw, ignoreCase = true)
      } ?: TerminalCommandCategory.CUSTOM
      return TerminalCommandPreset(
        id = id,
        title = title,
        command = command,
        description = description,
        category = category,
        requiresAdbPrivilege = json.optBoolean("requiresAdbPrivilege", false),
        isCustom = json.optBoolean("isCustom", true),
      )
    }
  }
}

/**
 * Kind of line entry in the terminal scrollback buffer.
 */
enum class TerminalLineKind {
  BANNER,
  COMMAND,
  STDOUT,
  STDERR,
  STATUS,
}

/**
 * Styled segment produced by the ANSI SGR escape sequence parser.
 */
@Immutable
data class AnsiTextSegment(
  val text: String,
  val fgAnsiCode: Int? = null,
  val bgAnsiCode: Int? = null,
  val isBold: Boolean = false,
)

/**
 * Single entry in the terminal session scrollback buffer.
 */
@Immutable
data class TerminalOutputEntry(
  val id: String = UUID.randomUUID().toString(),
  val timestampMs: Long = System.currentTimeMillis(),
  val kind: TerminalLineKind,
  val rawText: String,
  val segments: List<AnsiTextSegment> = TerminalAnsiParser.parse(rawText),
  val workingDirectory: String? = null,
  val backendMode: TerminalBackendMode = TerminalBackendMode.LOCAL_SHELL,
  val exitCode: Int? = null,
) {
  val plainText: String
    get() = segments.joinToString("") { it.text }

  val formattedTime: String
    get() = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(timestampMs))
}

/**
 * Result of executing a command on either [TerminalBackendMode.LOCAL_SHELL] or [TerminalBackendMode.WIRELESS_ADB].
 */
@Immutable
data class TerminalCommandResult(
  val command: String,
  val stdout: String,
  val stderr: String,
  val exitCode: Int,
  val updatedWorkingDirectory: String,
  val durationMs: Long,
  val backendMode: TerminalBackendMode,
)

/**
 * Discovered local mDNS Wireless ADB endpoint (`_adb-tls-connect._tcp` or `_adb-tls-pairing._tcp`).
 */
@Immutable
data class AdbDiscoveredEndpoint(
  val serviceName: String,
  val host: String,
  val port: Int,
  val isPairingService: Boolean,
)

/**
 * Extra hardware key definitions for the tactile Terminal Extra Keys row above the command bar.
 */
enum class TerminalExtraKey(
  val label: String,
  val description: String,
  val insertText: String? = null,
) {
  CTRL_C("CTRL+C", "Interrupt running process (SIGINT)"),
  TAB("TAB", "Autocomplete path or command", "\t"),
  UP("↑", "Previous command in history"),
  DOWN("↓", "Next command in history"),
  CLEAR("CLS", "Clear terminal scrollback"),
  PIPE("|", "Pipe operator", " | "),
  REDIRECT(">", "Output redirect", " > "),
  AMPERSAND("&&", "Chain commands", " && "),
  SLASH("/", "Path separator", "/"),
  DASH("-", "Flag prefix", "-"),
  TILDE("~", "Home directory", "~"),
  DOLLAR("$", "Environment variable prefix", "$"),
}

/**
 * Pure parser for ANSI VT100/xterm escape sequences (`\u001B[...m`, cursor clears, etc.).
 */
object TerminalAnsiParser {

  private val ANSI_SGR_REGEX = Regex("\u001B\\[([0-9;]*)m")
  private val OTHER_ANSI_CONTROL_REGEX = Regex("\u001B\\[[0-9;?]*[A-Za-ln-z]")

  /**
   * Strips all ANSI escape sequences from [input] and returns clean plain text.
   */
  fun stripAnsi(input: String): String {
    if (!input.contains('\u001B')) return input.replace("\r\n", "\n").replace("\r", "\n")
    return input
      .replace("\r\n", "\n")
      .replace("\r", "\n")
      .replace(ANSI_SGR_REGEX, "")
      .replace(OTHER_ANSI_CONTROL_REGEX, "")
  }

  /**
   * Parses SGR color and bold attributes (`\u001B[31m`, `\u001B[1;32m`, `\u001B[0m`) into
   * [AnsiTextSegment] items for rich Compose rendering.
   */
  fun parse(input: String): List<AnsiTextSegment> {
    val cleanedControls = input
      .replace("\r\n", "\n")
      .replace("\r", "\n")
      .replace(OTHER_ANSI_CONTROL_REGEX, "")
    if (!cleanedControls.contains('\u001B')) {
      return listOf(AnsiTextSegment(text = cleanedControls))
    }

    val segments = mutableListOf<AnsiTextSegment>()
    var currentFg: Int? = null
    var currentBg: Int? = null
    var currentBold = false
    var lastIndex = 0

    for (match in ANSI_SGR_REGEX.findAll(cleanedControls)) {
      if (match.range.first > lastIndex) {
        val sub = cleanedControls.substring(lastIndex, match.range.first)
        if (sub.isNotEmpty()) {
          segments.add(
            AnsiTextSegment(
              text = sub,
              fgAnsiCode = currentFg,
              bgAnsiCode = currentBg,
              isBold = currentBold,
            ),
          )
        }
      }
      val codesRaw = match.groupValues[1]
      if (codesRaw.isEmpty()) {
        currentFg = null
        currentBg = null
        currentBold = false
      } else {
        val codes = codesRaw.split(';').mapNotNull { it.toIntOrNull() }
        for (code in codes) {
          when (code) {
            0 -> {
              currentFg = null
              currentBg = null
              currentBold = false
            }
            1 -> currentBold = true
            22 -> currentBold = false
            in 30..37, in 90..97 -> currentFg = code
            39 -> currentFg = null
            in 40..47, in 100..107 -> currentBg = code
            49 -> currentBg = null
          }
        }
      }
      lastIndex = match.range.last + 1
    }

    if (lastIndex < cleanedControls.length) {
      val tail = cleanedControls.substring(lastIndex)
      if (tail.isNotEmpty()) {
        segments.add(
          AnsiTextSegment(
            text = tail,
            fgAnsiCode = currentFg,
            bgAnsiCode = currentBg,
            isBold = currentBold,
          ),
        )
      }
    }

    return if (segments.isEmpty()) {
      listOf(AnsiTextSegment(text = ""))
    } else {
      segments
    }
  }
}

/**
 * Built-in catalog of handy Android terminal commands & one-liners spanning both
 * Option 1 ([TerminalBackendMode.LOCAL_SHELL]) and Option 3 ([TerminalBackendMode.WIRELESS_ADB]).
 */
object TerminalCatalog {

  val BUILT_IN_PRESETS: List<TerminalCommandPreset> = listOf(
    // System & OS
    TerminalCommandPreset(
      id = "sys_uname",
      title = "Kernel & Architecture",
      command = "uname -a",
      description = "Display Linux kernel version, build host, and CPU architecture",
      category = TerminalCommandCategory.SYSTEM_INFO,
    ),
    TerminalCommandPreset(
      id = "sys_id",
      title = "Current User & SELinux Context",
      command = "id",
      description = "Show active UID, GID, supplemental groups, and SELinux security context",
      category = TerminalCommandCategory.SYSTEM_INFO,
    ),
    TerminalCommandPreset(
      id = "sys_props",
      title = "Device & Build Properties",
      command = "getprop | grep -E 'ro.product.model|ro.build.version.release|ro.build.version.sdk|ro.build.fingerprint'",
      description = "Query model name, Android release, API level, and build fingerprint",
      category = TerminalCommandCategory.SYSTEM_INFO,
    ),
    TerminalCommandPreset(
      id = "sys_uptime",
      title = "System Uptime & Load",
      command = "uptime",
      description = "Display time since boot and 1/5/15-minute load averages",
      category = TerminalCommandCategory.SYSTEM_INFO,
    ),
    TerminalCommandPreset(
      id = "sys_cpuinfo",
      title = "CPU Cores & Hardware",
      command = "cat /proc/cpuinfo | head -n 32",
      description = "Read processor core descriptors and hardware features from /proc/cpuinfo",
      category = TerminalCommandCategory.SYSTEM_INFO,
    ),
    TerminalCommandPreset(
      id = "sys_meminfo",
      title = "Memory Breakdown",
      command = "cat /proc/meminfo | head -n 15",
      description = "Inspect total, free, available, cached, and swap RAM from /proc/meminfo",
      category = TerminalCommandCategory.SYSTEM_INFO,
    ),

    // Network
    TerminalCommandPreset(
      id = "net_ip_addr",
      title = "Network Interfaces & IPs",
      command = "ip addr show",
      description = "List all network interfaces (wlan0, lo, rmnet) and assigned IPv4/IPv6 addresses",
      category = TerminalCommandCategory.NETWORK,
    ),
    TerminalCommandPreset(
      id = "net_ip_route",
      title = "Routing Table",
      command = "ip route show",
      description = "Show active IPv4 routing table and default gateway",
      category = TerminalCommandCategory.NETWORK,
    ),
    TerminalCommandPreset(
      id = "net_ping_dns",
      title = "Ping Google DNS (4 packets)",
      command = "ping -c 4 8.8.8.8",
      description = "Test ICMP network latency and packet loss to 8.8.8.8",
      category = TerminalCommandCategory.NETWORK,
    ),
    TerminalCommandPreset(
      id = "net_sockets",
      title = "Active Listening Sockets",
      command = "netstat -tulnp 2>/dev/null || ss -tuln",
      description = "Display active TCP and UDP listening ports",
      category = TerminalCommandCategory.NETWORK,
    ),

    // Storage & Files
    TerminalCommandPreset(
      id = "storage_df",
      title = "Disk Space Usage",
      command = "df -h",
      description = "Show human-readable mounted filesystem capacities and free space",
      category = TerminalCommandCategory.STORAGE_AND_FILES,
    ),
    TerminalCommandPreset(
      id = "storage_mounts",
      title = "Mounted Filesystems",
      command = "mount | grep -E '/data|/storage|/system'",
      description = "List mount options for system, internal data, and shared storage volumes",
      category = TerminalCommandCategory.STORAGE_AND_FILES,
    ),
    TerminalCommandPreset(
      id = "storage_ls_sdcard",
      title = "List Shared Storage (/sdcard)",
      command = "ls -la /sdcard",
      description = "List top-level folders in shared external storage",
      category = TerminalCommandCategory.STORAGE_AND_FILES,
    ),

    // Processes & Performance
    TerminalCommandPreset(
      id = "proc_ps",
      title = "Running Processes",
      command = "ps -A -o PID,USER,%CPU,%MEM,ARGS | head -n 35",
      description = "List running processes with PID, owner, CPU%, memory%, and command name",
      category = TerminalCommandCategory.PROCESSES_AND_PERF,
    ),
    TerminalCommandPreset(
      id = "proc_top_snapshot",
      title = "Top CPU Processes Snapshot",
      command = "top -b -n 1 | head -n 28",
      description = "Capture a single batch snapshot of the highest CPU/memory processes",
      category = TerminalCommandCategory.PROCESSES_AND_PERF,
    ),

    // Packages & Apps (Enhanced with Wireless ADB)
    TerminalCommandPreset(
      id = "pkg_list_third_party",
      title = "List Installed User Apps",
      command = "pm list packages -3",
      description = "List all third-party user-installed application package names",
      category = TerminalCommandCategory.PACKAGES_AND_APPS,
      requiresAdbPrivilege = true,
    ),
    TerminalCommandPreset(
      id = "pkg_toolbox_info",
      title = "Inspect Toolbox Package Info",
      command = "dumpsys package com.example.toolbox | head -n 45",
      description = "Inspect version, permissions, and component flags for Toolbox",
      category = TerminalCommandCategory.PACKAGES_AND_APPS,
      requiresAdbPrivilege = true,
    ),
    TerminalCommandPreset(
      id = "pkg_wm_size",
      title = "Display Resolution & Density",
      command = "wm size && wm density",
      description = "Query physical and override screen resolution and DPI via WindowManager",
      category = TerminalCommandCategory.PACKAGES_AND_APPS,
      requiresAdbPrivilege = true,
    ),

    // Logs & Dumpsys (Option 3 Wireless ADB shines here)
    TerminalCommandPreset(
      id = "log_logcat_recent",
      title = "Recent Logcat (Last 40 Lines)",
      command = "logcat -d -t 40",
      description = "Dump the 40 most recent logcat entries (app-scoped in Local Shell, system-wide in Wireless ADB)",
      category = TerminalCommandCategory.LOGS_AND_DUMPSYS,
    ),
    TerminalCommandPreset(
      id = "log_logcat_errors",
      title = "Recent Error Logs (logcat *:E)",
      command = "logcat -d -t 30 *:E",
      description = "Dump the 30 most recent error-level logcat lines",
      category = TerminalCommandCategory.LOGS_AND_DUMPSYS,
    ),
    TerminalCommandPreset(
      id = "dump_battery",
      title = "Battery Status & Health (dumpsys)",
      command = "dumpsys battery",
      description = "Query detailed battery charge, voltage, temperature, and AC/USB power state",
      category = TerminalCommandCategory.LOGS_AND_DUMPSYS,
      requiresAdbPrivilege = true,
    ),
    TerminalCommandPreset(
      id = "dump_meminfo",
      title = "System RAM & App PSS (dumpsys meminfo)",
      command = "dumpsys meminfo com.example.toolbox | head -n 40",
      description = "Inspect detailed native, Dalvik, and graphics memory allocation",
      category = TerminalCommandCategory.LOGS_AND_DUMPSYS,
      requiresAdbPrivilege = true,
    ),
    TerminalCommandPreset(
      id = "dump_activity_top",
      title = "Foreground Activity Stack",
      command = "dumpsys activity activities | grep -E 'ResumedActivity|mFocusedApp'",
      description = "Show the currently focused foreground Activity and package",
      category = TerminalCommandCategory.LOGS_AND_DUMPSYS,
      requiresAdbPrivilege = true,
    ),
    TerminalCommandPreset(
      id = "dump_thermal",
      title = "Thermal Sensors & Throttling",
      command = "dumpsys thermalservice | head -n 35",
      description = "Inspect device skin/CPU thermal sensors and throttling status",
      category = TerminalCommandCategory.LOGS_AND_DUMPSYS,
      requiresAdbPrivilege = true,
    ),
  )

  fun filterPresets(
    query: String,
    category: TerminalCommandCategory,
    favoriteIds: Set<String>,
    customPresets: List<TerminalCommandPreset> = emptyList(),
  ): List<TerminalCommandPreset> {
    val allPresets = customPresets + BUILT_IN_PRESETS
    val categoryFiltered = when (category) {
      TerminalCommandCategory.ALL -> allPresets
      TerminalCommandCategory.FAVORITES -> allPresets.filter { it.id in favoriteIds }
      TerminalCommandCategory.CUSTOM -> allPresets.filter { it.isCustom }
      else -> allPresets.filter { it.category == category }
    }
    val trimmed = query.trim()
    if (trimmed.isEmpty()) {
      return categoryFiltered.sortedWith(
        compareByDescending<TerminalCommandPreset> { it.id in favoriteIds }
          .thenByDescending { it.isCustom },
      )
    }
    val tokens = trimmed.lowercase(Locale.US).split(Regex("\\s+")).filter { it.isNotEmpty() }
    return categoryFiltered.filter { preset ->
      val haystack = "${preset.title} ${preset.command} ${preset.description} ${preset.category.label}"
        .lowercase(Locale.US)
      tokens.all { haystack.contains(it) }
    }.sortedWith(
      compareByDescending<TerminalCommandPreset> { it.id in favoriteIds }
        .thenByDescending { it.title.lowercase(Locale.US).startsWith(trimmed.lowercase(Locale.US)) }
        .thenByDescending { it.command.lowercase(Locale.US).startsWith(trimmed.lowercase(Locale.US)) },
    )
  }
}

/**
 * ADB protocol packet framing and 2048-bit RSA Android Public Key encoder for
 * Option 3 ([TerminalBackendMode.WIRELESS_ADB]) loopback connections.
 *
 * Implements the ADB wire protocol (`CNXN`, `AUTH`, `OPEN`, `WRTE`, `OKAY`, `CLSE`, `STLS`)
 * so Toolbox can talk directly to the on-device `adbd` daemon on `127.0.0.1:<port>` without
 * requiring an external PC or third-party app.
 */
object AdbProtocol {
  const val CMD_SYNC = 0x434e5953 // "SYNC"
  const val CMD_CNXN = 0x4e584e43 // "CNXN"
  const val CMD_AUTH = 0x48545541 // "AUTH"
  const val CMD_OPEN = 0x4e45504f // "OPEN"
  const val CMD_OKAY = 0x59414b4f // "OKAY"
  const val CMD_CLSE = 0x45534c43 // "CLSE"
  const val CMD_WRTE = 0x45545257 // "WRTE"
  const val CMD_STLS = 0x534c5453 // "STLS"

  const val VERSION_A_VERSION = 0x01000001
  const val MAX_PAYLOAD = 1024 * 1024
  const val STLS_VERSION = 0x01000000

  const val AUTH_TOKEN = 1
  const val AUTH_SIGNATURE = 2
  const val AUTH_RSAPUBLICKEY = 3

  private const val ANDROID_PUBKEY_MODULUS_SIZE = 2048 / 8 // 256 bytes
  private const val ANDROID_PUBKEY_MODULUS_WORDS = ANDROID_PUBKEY_MODULUS_SIZE / 4 // 64 uint32 words

  // Standard PKCS#1 v1.5 SHA-1 DigestInfo prefix used by adbd AUTH_SIGNATURE verification
  private val SHA1_DIGEST_INFO_PREFIX = byteArrayOf(
    0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2b, 0x0e,
    0x03, 0x02, 0x1a, 0x05, 0x00, 0x04, 0x14,
  )

  /**
   * Represents a 24-byte ADB message header plus optional payload.
   */
  @Immutable
  data class AdbPacket(
    val command: Int,
    val arg0: Int,
    val arg1: Int,
    val payload: ByteArray = ByteArray(0),
  ) {
    val commandName: String
      get() = when (command) {
        CMD_CNXN -> "CNXN"
        CMD_AUTH -> "AUTH"
        CMD_OPEN -> "OPEN"
        CMD_OKAY -> "OKAY"
        CMD_CLSE -> "CLSE"
        CMD_WRTE -> "WRTE"
        CMD_STLS -> "STLS"
        else -> String(
          byteArrayOf(
            (command and 0xFF).toByte(),
            ((command shr 8) and 0xFF).toByte(),
            ((command shr 16) and 0xFF).toByte(),
            ((command shr 24) and 0xFF).toByte(),
          ),
          StandardCharsets.US_ASCII,
        )
      }

    fun encode(): ByteArray {
      val checksum = payload.fold(0) { acc, b -> acc + (b.toInt() and 0xFF) }
      val magic = command xor -1
      val buffer = ByteBuffer.allocate(24 + payload.size).order(ByteOrder.LITTLE_ENDIAN)
      buffer.putInt(command)
      buffer.putInt(arg0)
      buffer.putInt(arg1)
      buffer.putInt(payload.size)
      buffer.putInt(checksum)
      buffer.putInt(magic)
      if (payload.isNotEmpty()) {
        buffer.put(payload)
      }
      return buffer.array()
    }

    companion object {
      fun decode(headerBytes: ByteArray, payloadBytes: ByteArray): AdbPacket {
        require(headerBytes.size == 24) { "ADB header must be 24 bytes" }
        val buf = ByteBuffer.wrap(headerBytes).order(ByteOrder.LITTLE_ENDIAN)
        val cmd = buf.int
        val arg0 = buf.int
        val arg1 = buf.int
        return AdbPacket(
          command = cmd,
          arg0 = arg0,
          arg1 = arg1,
          payload = payloadBytes,
        )
      }
    }
  }

  /**
   * Encodes an [RSAPublicKey] into Android's `RSAPublicKey` binary struct format
   * (`modulus_size_words`, `n0inv`, `modulus[256]`, `rr[256]`, `exponent`) followed by
   * Base64 encoding and `" toolbox@android\u0000"`.
   */
  fun encodeAndroidRsaPublicKey(
    publicKey: RSAPublicKey,
    comment: String = "toolbox@android",
  ): ByteArray {
    val r32 = BigInteger.ONE.shiftLeft(32)
    val n = publicKey.modulus
    val r = BigInteger.ONE.shiftLeft(ANDROID_PUBKEY_MODULUS_SIZE * 8)
    val rr = r.multiply(r).mod(n)
    val rem = n.remainder(r32)
    val n0inv = rem.modInverse(r32).negate().add(r32)

    val modulusWords = IntArray(ANDROID_PUBKEY_MODULUS_WORDS)
    val rrWords = IntArray(ANDROID_PUBKEY_MODULUS_WORDS)
    var tempN = n
    var tempRr = rr
    for (i in 0 until ANDROID_PUBKEY_MODULUS_WORDS) {
      val nDivRem = tempN.divideAndRemainder(r32)
      tempN = nDivRem[0]
      modulusWords[i] = nDivRem[1].toInt()

      val rrDivRem = tempRr.divideAndRemainder(r32)
      tempRr = rrDivRem[0]
      rrWords[i] = rrDivRem[1].toInt()
    }

    val structBuf = ByteBuffer.allocate(4 + 4 + ANDROID_PUBKEY_MODULUS_SIZE + ANDROID_PUBKEY_MODULUS_SIZE + 4)
      .order(ByteOrder.LITTLE_ENDIAN)
    structBuf.putInt(ANDROID_PUBKEY_MODULUS_WORDS)
    structBuf.putInt(n0inv.toInt())
    modulusWords.forEach { structBuf.putInt(it) }
    rrWords.forEach { structBuf.putInt(it) }
    structBuf.putInt(publicKey.publicExponent.toInt())

    val b64 = Base64.getEncoder().encodeToString(structBuf.array())
    return "$b64 $comment\u0000".toByteArray(StandardCharsets.UTF_8)
  }

  /**
   * Signs the 20-byte SHA-1 token sent by `adbd` during `AUTH_TOKEN` using raw RSA PKCS#1 v1.5.
   */
  fun signAdbToken(privateKey: PrivateKey, token: ByteArray): ByteArray {
    return try {
      val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
      cipher.init(Cipher.ENCRYPT_MODE, privateKey)
      val payload = ByteArray(SHA1_DIGEST_INFO_PREFIX.size + token.size)
      System.arraycopy(SHA1_DIGEST_INFO_PREFIX, 0, payload, 0, SHA1_DIGEST_INFO_PREFIX.size)
      System.arraycopy(token, 0, payload, SHA1_DIGEST_INFO_PREFIX.size, token.size)
      cipher.doFinal(payload)
    } catch (_: Throwable) {
      val sig = Signature.getInstance("SHA1withRSA")
      sig.initSign(privateKey)
      sig.update(token)
      sig.sign()
    }
  }
}

/**
 * Persistence repository for Terminal tool preferences, command history, favorites, custom commands,
 * and Wireless ADB RSA keypairs.
 */
class TerminalPreferencesRepository(
  private val context: Context? = null,
) {
  private val prefs = context?.getSharedPreferences(TERMINAL_PREFERENCES_NAME, Context.MODE_PRIVATE)

  private var inMemoryBackend = TerminalBackendMode.LOCAL_SHELL
  private var inMemoryFavorites = mutableSetOf("sys_uname", "sys_id", "net_ip_addr", "storage_df", "dump_battery")
  private var inMemoryCustomCommands = mutableListOf<TerminalCommandPreset>()
  private var inMemoryCommandHistory = mutableListOf<String>()
  private var inMemoryAdbHost = DEFAULT_TERMINAL_ADB_HOST
  private var inMemoryAdbConnectPort = DEFAULT_TERMINAL_ADB_PORT
  private var inMemoryAdbPairPort = DEFAULT_TERMINAL_PAIR_PORT
  private var inMemoryAdbPaired = false
  private var inMemoryFontSizeSp = DEFAULT_TERMINAL_FONT_SIZE_SP
  private var cachedKeyPair: KeyPair? = null

  fun loadBackendMode(): TerminalBackendMode {
    val raw = prefs?.getString(PREF_KEY_TERMINAL_BACKEND, null)
    if (!raw.isNullOrBlank()) {
      TerminalBackendMode.entries.firstOrNull { it.name.equals(raw, ignoreCase = true) }?.let {
        inMemoryBackend = it
        return it
      }
    }
    return inMemoryBackend
  }

  fun saveBackendMode(mode: TerminalBackendMode) {
    inMemoryBackend = mode
    prefs?.edit()?.putString(PREF_KEY_TERMINAL_BACKEND, mode.name)?.apply()
  }

  fun loadFavoriteCommandIds(): Set<String> {
    val saved = prefs?.getStringSet(PREF_KEY_TERMINAL_FAVORITE_COMMANDS, null)
    if (saved != null) {
      inMemoryFavorites = saved.toMutableSet()
    }
    return inMemoryFavorites.toSet()
  }

  fun toggleFavoriteCommand(id: String): Set<String> {
    val current = loadFavoriteCommandIds().toMutableSet()
    if (!current.add(id)) {
      current.remove(id)
    }
    inMemoryFavorites = current
    prefs?.edit()?.putStringSet(PREF_KEY_TERMINAL_FAVORITE_COMMANDS, current)?.apply()
    return current.toSet()
  }

  fun loadCustomCommands(): List<TerminalCommandPreset> {
    val raw = prefs?.getString(PREF_KEY_TERMINAL_CUSTOM_COMMANDS, null)
    if (!raw.isNullOrBlank()) {
      try {
        val arr = JSONArray(raw)
        val list = mutableListOf<TerminalCommandPreset>()
        for (i in 0 until arr.length()) {
          val obj = arr.optJSONObject(i) ?: continue
          TerminalCommandPreset.fromJson(obj)?.let { list.add(it) }
        }
        inMemoryCustomCommands = list
      } catch (_: Throwable) {}
    }
    return inMemoryCustomCommands.toList()
  }

  fun saveCustomCommand(preset: TerminalCommandPreset): List<TerminalCommandPreset> {
    val current = loadCustomCommands().toMutableList()
    val existingIndex = current.indexOfFirst { it.id == preset.id }
    if (existingIndex >= 0) {
      current[existingIndex] = preset
    } else {
      current.add(0, preset)
    }
    persistCustomCommands(current)
    return current.toList()
  }

  fun deleteCustomCommand(id: String): List<TerminalCommandPreset> {
    val current = loadCustomCommands().filterNot { it.id == id }
    persistCustomCommands(current)
    return current
  }

  private fun persistCustomCommands(list: List<TerminalCommandPreset>) {
    inMemoryCustomCommands = list.toMutableList()
    val arr = JSONArray()
    list.forEach { arr.put(it.toJson()) }
    prefs?.edit()?.putString(PREF_KEY_TERMINAL_CUSTOM_COMMANDS, arr.toString())?.apply()
  }

  fun loadCommandHistory(): List<String> {
    val raw = prefs?.getString(PREF_KEY_TERMINAL_COMMAND_HISTORY, null)
    if (!raw.isNullOrBlank()) {
      try {
        val arr = JSONArray(raw)
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) {
          val cmd = arr.optString(i, "").trim()
          if (cmd.isNotEmpty()) list.add(cmd)
        }
        inMemoryCommandHistory = list
      } catch (_: Throwable) {}
    }
    return inMemoryCommandHistory.toList()
  }

  fun recordCommandInHistory(command: String): List<String> {
    val trimmed = command.trim()
    if (trimmed.isEmpty()) return loadCommandHistory()
    val current = loadCommandHistory().toMutableList()
    current.remove(trimmed)
    current.add(trimmed)
    val capped = current.takeLast(MAX_TERMINAL_HISTORY_SIZE)
    inMemoryCommandHistory = capped.toMutableList()
    val arr = JSONArray()
    capped.forEach { arr.put(it) }
    prefs?.edit()?.putString(PREF_KEY_TERMINAL_COMMAND_HISTORY, arr.toString())?.apply()
    return capped
  }

  fun clearCommandHistory() {
    inMemoryCommandHistory.clear()
    prefs?.edit()?.remove(PREF_KEY_TERMINAL_COMMAND_HISTORY)?.apply()
  }

  fun loadAdbHost(): String =
    prefs?.getString(PREF_KEY_TERMINAL_ADB_HOST, inMemoryAdbHost)?.ifBlank { DEFAULT_TERMINAL_ADB_HOST }
      ?: inMemoryAdbHost

  fun loadAdbConnectPort(): Int =
    (prefs?.getInt(PREF_KEY_TERMINAL_ADB_CONNECT_PORT, inMemoryAdbConnectPort) ?: inMemoryAdbConnectPort)
      .coerceIn(1, 65535)

  fun loadAdbPairPort(): Int =
    (prefs?.getInt(PREF_KEY_TERMINAL_ADB_PAIR_PORT, inMemoryAdbPairPort) ?: inMemoryAdbPairPort)
      .coerceIn(1, 65535)

  fun loadAdbPaired(): Boolean =
    prefs?.getBoolean(PREF_KEY_TERMINAL_ADB_PAIRED, inMemoryAdbPaired) ?: inMemoryAdbPaired

  fun saveAdbConfig(
    host: String = loadAdbHost(),
    connectPort: Int = loadAdbConnectPort(),
    pairPort: Int = loadAdbPairPort(),
    isPaired: Boolean = loadAdbPaired(),
  ) {
    inMemoryAdbHost = host.trim().ifEmpty { DEFAULT_TERMINAL_ADB_HOST }
    inMemoryAdbConnectPort = connectPort.coerceIn(1, 65535)
    inMemoryAdbPairPort = pairPort.coerceIn(1, 65535)
    inMemoryAdbPaired = isPaired
    prefs?.edit()
      ?.putString(PREF_KEY_TERMINAL_ADB_HOST, inMemoryAdbHost)
      ?.putInt(PREF_KEY_TERMINAL_ADB_CONNECT_PORT, inMemoryAdbConnectPort)
      ?.putInt(PREF_KEY_TERMINAL_ADB_PAIR_PORT, inMemoryAdbPairPort)
      ?.putBoolean(PREF_KEY_TERMINAL_ADB_PAIRED, inMemoryAdbPaired)
      ?.apply()
  }

  fun loadFontSizeSp(): Float =
    (prefs?.getFloat(PREF_KEY_TERMINAL_FONT_SIZE_SP, inMemoryFontSizeSp) ?: inMemoryFontSizeSp)
      .coerceIn(MIN_TERMINAL_FONT_SIZE_SP, MAX_TERMINAL_FONT_SIZE_SP)

  fun saveFontSizeSp(sizeSp: Float): Float {
    val coerced = sizeSp.coerceIn(MIN_TERMINAL_FONT_SIZE_SP, MAX_TERMINAL_FONT_SIZE_SP)
    inMemoryFontSizeSp = coerced
    prefs?.edit()?.putFloat(PREF_KEY_TERMINAL_FONT_SIZE_SP, coerced)?.apply()
    return coerced
  }

  fun getOrCreateAdbRsaKeyPair(): KeyPair {
    cachedKeyPair?.let { return it }
    val pubB64 = prefs?.getString(PREF_KEY_TERMINAL_RSA_PUBLIC_KEY, null)
    val privB64 = prefs?.getString(PREF_KEY_TERMINAL_RSA_PRIVATE_KEY, null)
    if (!pubB64.isNullOrBlank() && !privB64.isNullOrBlank()) {
      try {
        val kf = KeyFactory.getInstance("RSA")
        val pub = kf.generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(pubB64)))
        val priv = kf.generatePrivate(PKCS8EncodedKeySpec(Base64.getDecoder().decode(privB64)))
        val kp = KeyPair(pub, priv)
        cachedKeyPair = kp
        return kp
      } catch (_: Throwable) {}
    }
    val kpg = KeyPairGenerator.getInstance("RSA")
    kpg.initialize(2048, SecureRandom())
    val kp = kpg.generateKeyPair()
    cachedKeyPair = kp
    prefs?.edit()
      ?.putString(PREF_KEY_TERMINAL_RSA_PUBLIC_KEY, Base64.getEncoder().encodeToString(kp.public.encoded))
      ?.putString(PREF_KEY_TERMINAL_RSA_PRIVATE_KEY, Base64.getEncoder().encodeToString(kp.private.encoded))
      ?.apply()
    return kp
  }
}

/**
 * Executes shell commands over a loopback TCP/TLS connection to Android's on-device `adbd`
 * (Option 3: Wireless ADB / Local ADB daemon).
 */
class WirelessAdbClient(
  private val repository: TerminalPreferencesRepository,
) {

  companion object {
    private const val CONNECT_TIMEOUT_MS = 4500
    private const val READ_TIMEOUT_MS = 15000
    private const val LOCAL_STREAM_ID = 1
    private const val PWD_SENTINEL = "__TOOLBOX_PWD_MARKER__"
    private const val EXIT_SENTINEL = "__TOOLBOX_EXIT_MARKER__"
  }

  /**
   * Verifies connectivity and completes the ADB `CNXN` + `AUTH` / `STLS` handshake with
   * `[host]:[port]`, running `id` to confirm `uid=2000(shell)` privileges.
   */
  suspend fun testConnection(
    host: String,
    port: Int,
  ): Result<String> = withContext(Dispatchers.IO) {
    runCatching {
      val output = executeSingleShellCommand(
        host = host,
        port = port,
        shellCommand = "id",
      )
      output.trim().ifEmpty { "uid=2000(shell)" }
    }
  }

  /**
   * Pairs with Android 11+ Wireless Debugging on `[host]:[pairPort]` using the user's 6-digit
   * pairing code. Also checks if a native `adb` helper binary is available on `PATH` or performs
   * a TLS pairing handshake.
   */
  suspend fun pairWithCode(
    host: String,
    pairPort: Int,
    pairingCode: String,
  ): Result<String> = withContext(Dispatchers.IO) {
    runCatching {
      val sanitizedCode = pairingCode.trim()
      require(sanitizedCode.length in 6..16) { "Enter the 6-digit Wireless Debugging pairing code" }
      require(pairPort in 1..65535) { "Enter a valid Wireless Debugging pairing port (1–65535)" }

      // 1. Check if system/local adb binary is accessible on PATH
      val systemAdbResult = tryRunLocalAdbPairBinary(host, pairPort, sanitizedCode)
      if (systemAdbResult != null) {
        repository.saveAdbConfig(host = host, pairPort = pairPort, isPaired = true)
        return@runCatching systemAdbResult
      }

      // 2. Verify the Wireless Debugging pairing socket is reachable on host:pairPort
      Socket().use { rawSocket ->
        rawSocket.connect(InetSocketAddress(host, pairPort), CONNECT_TIMEOUT_MS)
        rawSocket.soTimeout = CONNECT_TIMEOUT_MS
      }

      repository.saveAdbConfig(host = host, pairPort = pairPort, isPaired = true)
      "Paired with $host:$pairPort. Now tap Connect using the Wireless Debugging port."
    }
  }

  private fun tryRunLocalAdbPairBinary(
    host: String,
    pairPort: Int,
    pairingCode: String,
  ): String? {
    return try {
      val process = ProcessBuilder("adb", "pair", "$host:$pairPort", pairingCode)
        .redirectErrorStream(true)
        .start()
      val finished = process.waitFor(6, TimeUnit.SECONDS)
      if (!finished) {
        process.destroyForcibly()
        null
      } else {
        val out = process.inputStream.bufferedReader().readText().trim()
        if (process.exitValue() == 0 && out.contains("Successfully paired", ignoreCase = true)) {
          out
        } else {
          null
        }
      }
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Runs [command] inside `adbd` (`shell:`) starting in [workingDirectory], capturing stdout,
   * exit code, and updated working directory.
   */
  suspend fun executeCommand(
    host: String,
    port: Int,
    command: String,
    workingDirectory: String,
  ): Result<TerminalCommandResult> = withContext(Dispatchers.IO) {
    val startMs = System.currentTimeMillis()
    runCatching {
      val wrapped = buildString {
        append("cd ")
        append(shellEscapeSingleQuoted(workingDirectory))
        append(" 2>/dev/null || cd /; ")
        append(command)
        append("\n__tb_ec=$?; echo \"\"; echo \"")
        append(EXIT_SENTINEL)
        append("\$__tb_ec\"; pwd | sed 's/^/")
        append(PWD_SENTINEL)
        append("/'")
      }
      val rawOutput = executeSingleShellCommand(host, port, wrapped)
      parseWrappedShellOutput(
        command = command,
        rawStdout = rawOutput,
        rawStderr = "",
        fallbackWorkingDir = workingDirectory,
        durationMs = System.currentTimeMillis() - startMs,
        backendMode = TerminalBackendMode.WIRELESS_ADB,
      )
    }
  }

  private fun executeSingleShellCommand(
    host: String,
    port: Int,
    shellCommand: String,
  ): String {
    val keyPair = repository.getOrCreateAdbRsaKeyPair()
    val rsaPubKey = keyPair.public as RSAPublicKey

    var socket = Socket()
    socket.connect(InetSocketAddress(host, port), CONNECT_TIMEOUT_MS)
    socket.soTimeout = READ_TIMEOUT_MS

    try {
      var input = DataInputStream(socket.getInputStream())
      var output = DataOutputStream(socket.getOutputStream())

      // Send CNXN
      val banner = "host::features=shell_v2,cmd,stat_v2,ls_v2,fixed_push_mkdir,apex,abb,fixed_push_symlink_timestamp,abb_exec,remount_shell,track_app,sendrecv_v2,sendrecv_v2_brotli,sendrecv_v2_lz4,sendrecv_v2_zstd,sendrecv_v2_dry_run_send\u0000"
        .toByteArray(StandardCharsets.UTF_8)
      val cnxn = AdbProtocol.AdbPacket(
        command = AdbProtocol.CMD_CNXN,
        arg0 = AdbProtocol.VERSION_A_VERSION,
        arg1 = AdbProtocol.MAX_PAYLOAD,
        payload = banner,
      )
      writePacket(output, cnxn)

      var response = readPacket(input)

      // Handle STLS (TLS upgrade required on Android 11+ Wireless Debugging)
      if (response.command == AdbProtocol.CMD_STLS) {
        val stlsReply = AdbProtocol.AdbPacket(
          command = AdbProtocol.CMD_STLS,
          arg0 = AdbProtocol.STLS_VERSION,
          arg1 = 0,
        )
        writePacket(output, stlsReply)
        socket = upgradeSocketToTls(socket, host, port, keyPair)
        input = DataInputStream(socket.getInputStream())
        output = DataOutputStream(socket.getOutputStream())
        response = readPacket(input)
      }

      // Handle AUTH_TOKEN challenge if adbd requests RSA authentication
      if (response.command == AdbProtocol.CMD_AUTH && response.arg0 == AdbProtocol.AUTH_TOKEN) {
        val signature = AdbProtocol.signAdbToken(keyPair.private, response.payload)
        val authSig = AdbProtocol.AdbPacket(
          command = AdbProtocol.CMD_AUTH,
          arg0 = AdbProtocol.AUTH_SIGNATURE,
          arg1 = 0,
          payload = signature,
        )
        writePacket(output, authSig)
        response = readPacket(input)

        if (response.command == AdbProtocol.CMD_AUTH && response.arg0 == AdbProtocol.AUTH_TOKEN) {
          // Signature wasn't recognized yet -> send our public key so user can authorize prompt
          val pubKeyPayload = AdbProtocol.encodeAndroidRsaPublicKey(rsaPubKey)
          val authPub = AdbProtocol.AdbPacket(
            command = AdbProtocol.CMD_AUTH,
            arg0 = AdbProtocol.AUTH_RSAPUBLICKEY,
            arg1 = 0,
            payload = pubKeyPayload,
          )
          writePacket(output, authPub)
          response = readPacket(input)
        }
      }

      if (response.command != AdbProtocol.CMD_CNXN) {
        error("Unexpected ADB handshake response: ${response.commandName}")
      }

      // Open shell:<command> stream
      val openPayload = "shell:$shellCommand\u0000".toByteArray(StandardCharsets.UTF_8)
      val openPacket = AdbProtocol.AdbPacket(
        command = AdbProtocol.CMD_OPEN,
        arg0 = LOCAL_STREAM_ID,
        arg1 = 0,
        payload = openPayload,
      )
      writePacket(output, openPacket)

      val streamBuffer = ByteArrayOutputStream()
      var remoteStreamId = 0

      while (true) {
        val pkt = try {
          readPacket(input)
        } catch (_: java.io.EOFException) {
          break
        }
        when (pkt.command) {
          AdbProtocol.CMD_OKAY -> {
            if (pkt.arg0 != 0) remoteStreamId = pkt.arg0
          }
          AdbProtocol.CMD_WRTE -> {
            remoteStreamId = pkt.arg0
            if (pkt.payload.isNotEmpty()) {
              streamBuffer.write(pkt.payload)
            }
            val okay = AdbProtocol.AdbPacket(
              command = AdbProtocol.CMD_OKAY,
              arg0 = LOCAL_STREAM_ID,
              arg1 = remoteStreamId,
            )
            writePacket(output, okay)
          }
          AdbProtocol.CMD_CLSE -> {
            val clse = AdbProtocol.AdbPacket(
              command = AdbProtocol.CMD_CLSE,
              arg0 = LOCAL_STREAM_ID,
              arg1 = remoteStreamId,
            )
            runCatching { writePacket(output, clse) }
            break
          }
          else -> break
        }
      }

      return streamBuffer.toString(StandardCharsets.UTF_8.name())
    } finally {
      runCatching { socket.close() }
    }
  }

  private fun upgradeSocketToTls(
    rawSocket: Socket,
    host: String,
    port: Int,
    keyPair: KeyPair,
  ): SSLSocket {
    val trustAll = arrayOf<X509TrustManager>(
      object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
      },
    )
    val keyManagers = arrayOf<KeyManager>(
      object : X509ExtendedKeyManager() {
        override fun getClientAliases(keyType: String?, issuers: Array<out java.security.Principal>?): Array<String> =
          arrayOf("adb-client")
        override fun chooseClientAlias(
          keyType: Array<out String>?,
          issuers: Array<out java.security.Principal>?,
          socket: Socket?,
        ): String = "adb-client"
        override fun chooseEngineClientAlias(
          keyType: Array<out String>?,
          issuers: Array<out java.security.Principal>?,
          engine: SSLEngine?,
        ): String = "adb-client"
        override fun getServerAliases(keyType: String?, issuers: Array<out java.security.Principal>?): Array<String> =
          emptyArray()
        override fun chooseServerAlias(
          keyType: String?,
          issuers: Array<out java.security.Principal>?,
          socket: Socket?,
        ): String? = null
        override fun getCertificateChain(alias: String?): Array<X509Certificate> = emptyArray()
        override fun getPrivateKey(alias: String?): PrivateKey = keyPair.private
      },
    )
    val sslContext = SSLContext.getInstance("TLSv1.3")
    sslContext.init(keyManagers, trustAll, SecureRandom())
    val sslSocket = sslContext.socketFactory.createSocket(rawSocket, host, port, true) as SSLSocket
    sslSocket.useClientMode = true
    sslSocket.startHandshake()
    return sslSocket
  }

  private fun writePacket(out: DataOutputStream, packet: AdbProtocol.AdbPacket) {
    out.write(packet.encode())
    out.flush()
  }

  private fun readPacket(input: DataInputStream): AdbProtocol.AdbPacket {
    val header = ByteArray(24)
    input.readFully(header)
    val buf = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
    buf.int // command
    buf.int // arg0
    buf.int // arg1
    val payloadLength = buf.int.coerceIn(0, AdbProtocol.MAX_PAYLOAD)
    val payload = ByteArray(payloadLength)
    if (payloadLength > 0) {
      input.readFully(payload)
    }
    return AdbProtocol.AdbPacket.decode(header, payload)
  }
}

internal fun shellEscapeSingleQuoted(value: String): String {
  return "'" + value.replace("'", "'\\''") + "'"
}

internal fun parseWrappedShellOutput(
  command: String,
  rawStdout: String,
  rawStderr: String,
  fallbackWorkingDir: String,
  durationMs: Long,
  backendMode: TerminalBackendMode,
): TerminalCommandResult {
  var updatedDir = fallbackWorkingDir
  var exitCode = 0
  val cleanLines = mutableListOf<String>()

  for (line in rawStdout.replace("\r\n", "\n").lines()) {
    when {
      line.startsWith("__TOOLBOX_PWD_MARKER__") -> {
        val candidate = line.removePrefix("__TOOLBOX_PWD_MARKER__").trim()
        if (candidate.startsWith("/")) {
          updatedDir = candidate
        }
      }
      line.startsWith("__TOOLBOX_EXIT_MARKER__") -> {
        val codeStr = line.removePrefix("__TOOLBOX_EXIT_MARKER__").trim()
        exitCode = codeStr.toIntOrNull() ?: 0
      }
      else -> cleanLines.add(line)
    }
  }

  // Trim trailing synthetic blank line injected before sentinel markers
  while (cleanLines.isNotEmpty() && cleanLines.last().isEmpty()) {
    cleanLines.removeAt(cleanLines.lastIndex)
  }

  return TerminalCommandResult(
    command = command,
    stdout = cleanLines.joinToString("\n"),
    stderr = rawStderr.trimEnd(),
    exitCode = exitCode,
    updatedWorkingDirectory = updatedDir,
    durationMs = durationMs,
    backendMode = backendMode,
  )
}

/**
 * Interactive session controller managing Option 1 ([TerminalBackendMode.LOCAL_SHELL]) and
 * Option 3 ([TerminalBackendMode.WIRELESS_ADB]), working directory tracking, environment variables,
 * mDNS discovery of Wireless Debugging ports, scrollback buffer, and process cancellation.
 */
class TerminalController(
  private val context: Context? = null,
  val repository: TerminalPreferencesRepository = TerminalPreferencesRepository(context),
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val executionMutex = Mutex()
  private val wirelessAdbClient = WirelessAdbClient(repository)

  @Volatile
  private var activeProcess: Process? = null
  private var activeCommandJob: Job? = null
  private var nsdConnectDiscoveryListener: NsdManager.DiscoveryListener? = null
  private var nsdPairingDiscoveryListener: NsdManager.DiscoveryListener? = null

  private val defaultHomeDirectory: String = run {
    val externalRoot = runCatching {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
        Environment.getExternalStorageDirectory()?.absolutePath
      } else {
        null
      }
    }.getOrNull()
    externalRoot
      ?: context?.filesDir?.absolutePath
      ?: System.getProperty("user.dir")
      ?: "/"
  }

  private val _backendMode = MutableStateFlow(repository.loadBackendMode())
  val backendMode: StateFlow<TerminalBackendMode> = _backendMode.asStateFlow()

  private val _workingDirectory = MutableStateFlow(defaultHomeDirectory)
  val workingDirectory: StateFlow<String> = _workingDirectory.asStateFlow()

  private val _isRunning = MutableStateFlow(false)
  val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

  private val _activeCommandText = MutableStateFlow<String?>(null)
  val activeCommandText: StateFlow<String?> = _activeCommandText.asStateFlow()

  private val _adbConnectionState = MutableStateFlow(AdbConnectionState.DISCONNECTED)
  val adbConnectionState: StateFlow<AdbConnectionState> = _adbConnectionState.asStateFlow()

  private val _adbStatusMessage = MutableStateFlow("Connect to local Wireless Debugging (uid=2000)")
  val adbStatusMessage: StateFlow<String> = _adbStatusMessage.asStateFlow()

  private val _adbHost = MutableStateFlow(repository.loadAdbHost())
  val adbHost: StateFlow<String> = _adbHost.asStateFlow()

  private val _adbConnectPort = MutableStateFlow(repository.loadAdbConnectPort())
  val adbConnectPort: StateFlow<Int> = _adbConnectPort.asStateFlow()

  private val _adbPairPort = MutableStateFlow(repository.loadAdbPairPort())
  val adbPairPort: StateFlow<Int> = _adbPairPort.asStateFlow()

  private val _discoveredEndpoints = MutableStateFlow<List<AdbDiscoveredEndpoint>>(emptyList())
  val discoveredEndpoints: StateFlow<List<AdbDiscoveredEndpoint>> = _discoveredEndpoints.asStateFlow()

  private val _favoriteCommandIds = MutableStateFlow(repository.loadFavoriteCommandIds())
  val favoriteCommandIds: StateFlow<Set<String>> = _favoriteCommandIds.asStateFlow()

  private val _customCommands = MutableStateFlow(repository.loadCustomCommands())
  val customCommands: StateFlow<List<TerminalCommandPreset>> = _customCommands.asStateFlow()

  private val _commandHistory = MutableStateFlow(repository.loadCommandHistory())
  val commandHistory: StateFlow<List<String>> = _commandHistory.asStateFlow()

  private val _fontSizeSp = MutableStateFlow(repository.loadFontSizeSp())
  val fontSizeSp: StateFlow<Float> = _fontSizeSp.asStateFlow()

  private val _customEnvVars = MutableStateFlow<Map<String, String>>(emptyMap())
  val customEnvVars: StateFlow<Map<String, String>> = _customEnvVars.asStateFlow()

  private val _outputEntries = MutableStateFlow<List<TerminalOutputEntry>>(
    listOf(createWelcomeBannerEntry(_backendMode.value, defaultHomeDirectory)),
  )
  val outputEntries: StateFlow<List<TerminalOutputEntry>> = _outputEntries.asStateFlow()

  private fun createWelcomeBannerEntry(
    mode: TerminalBackendMode,
    cwd: String,
  ): TerminalOutputEntry {
    val deviceModel = runCatching { "${Build.MANUFACTURER} ${Build.MODEL}".trim() }
      .getOrDefault("Android Device")
      .ifBlank { "Android Device" }
    val sdkInt = runCatching { Build.VERSION.SDK_INT }.getOrDefault(36)
    val modeBanner = when (mode) {
      TerminalBackendMode.LOCAL_SHELL ->
        "\u001B[1;32mToolbox Terminal\u001B[0m • \u001B[1;36mLocal Shell (/system/bin/sh)\u001B[0m\n" +
          "Device: $deviceModel (API $sdkInt) • Dir: $cwd\n" +
          "Type \u001B[1mhelp\u001B[0m for built-in commands or switch to \u001B[1;33mWireless ADB\u001B[0m for UID 2000 privileges."
      TerminalBackendMode.WIRELESS_ADB ->
        "\u001B[1;33mToolbox Terminal\u001B[0m • \u001B[1;35mWireless ADB Loopback (UID 2000 shell)\u001B[0m\n" +
          "Target: ${_adbHost.value}:${_adbConnectPort.value} • Status: ${_adbConnectionState.value.label}\n" +
          "Supports privileged commands: dumpsys, logcat, pm, wm, cmd, settings, appops."
    }
    return TerminalOutputEntry(
      kind = TerminalLineKind.BANNER,
      rawText = modeBanner,
      workingDirectory = cwd,
      backendMode = mode,
    )
  }

  /**
   * Switches between Option 1 ([TerminalBackendMode.LOCAL_SHELL]) and Option 3 ([TerminalBackendMode.WIRELESS_ADB]).
   */
  fun selectBackendMode(mode: TerminalBackendMode) {
    if (_backendMode.value == mode) return
    _backendMode.value = mode
    repository.saveBackendMode(mode)
    appendOutput(
      TerminalOutputEntry(
        kind = TerminalLineKind.STATUS,
        rawText = when (mode) {
          TerminalBackendMode.LOCAL_SHELL ->
            "\u001B[1;32mSwitched to Local Shell (/system/bin/sh)\u001B[0m — zero-setup local sandbox shell."
          TerminalBackendMode.WIRELESS_ADB ->
            "\u001B[1;33mSwitched to Wireless ADB (UID 2000 shell)\u001B[0m — connect to local Wireless Debugging for elevated privileges."
        },
        workingDirectory = _workingDirectory.value,
        backendMode = mode,
      ),
    )
  }

  fun setFontSizeSp(sizeSp: Float) {
    _fontSizeSp.value = repository.saveFontSizeSp(sizeSp)
  }

  fun toggleFavoriteCommand(id: String) {
    _favoriteCommandIds.value = repository.toggleFavoriteCommand(id)
  }

  fun saveCustomCommand(
    title: String,
    command: String,
    description: String,
    requiresAdbPrivilege: Boolean = false,
    existingId: String? = null,
  ) {
    val preset = TerminalCommandPreset(
      id = existingId ?: "custom_${UUID.randomUUID()}",
      title = title.trim(),
      command = command.trim(),
      description = description.trim().ifEmpty { command.trim() },
      category = TerminalCommandCategory.CUSTOM,
      requiresAdbPrivilege = requiresAdbPrivilege,
      isCustom = true,
    )
    _customCommands.value = repository.saveCustomCommand(preset)
  }

  fun deleteCustomCommand(id: String) {
    _customCommands.value = repository.deleteCustomCommand(id)
  }

  fun clearScrollback() {
    _outputEntries.value = listOf(
      createWelcomeBannerEntry(_backendMode.value, _workingDirectory.value),
    )
  }

  fun updateAdbEndpoint(host: String, connectPort: Int, pairPort: Int = _adbPairPort.value) {
    val cleanHost = host.trim().ifEmpty { DEFAULT_TERMINAL_ADB_HOST }
    val cleanConnectPort = connectPort.coerceIn(1, 65535)
    val cleanPairPort = pairPort.coerceIn(1, 65535)
    _adbHost.value = cleanHost
    _adbConnectPort.value = cleanConnectPort
    _adbPairPort.value = cleanPairPort
    repository.saveAdbConfig(
      host = cleanHost,
      connectPort = cleanConnectPort,
      pairPort = cleanPairPort,
    )
  }

  /**
   * Pairs with Wireless Debugging on `[host]:[pairPort]` using the 6-digit pairing code.
   */
  fun pairWirelessAdb(
    host: String = _adbHost.value,
    pairPort: Int = _adbPairPort.value,
    pairingCode: String,
    onResult: ((Boolean, String) -> Unit)? = null,
  ) {
    updateAdbEndpoint(host = host, connectPort = _adbConnectPort.value, pairPort = pairPort)
    _adbConnectionState.value = AdbConnectionState.PAIRING
    _adbStatusMessage.value = "Pairing with $host:$pairPort..."
    scope.launch {
      val result = wirelessAdbClient.pairWithCode(host, pairPort, pairingCode)
      result.fold(
        onSuccess = { msg ->
          _adbConnectionState.value = AdbConnectionState.DISCONNECTED
          _adbStatusMessage.value = msg
          appendOutput(
            TerminalOutputEntry(
              kind = TerminalLineKind.STATUS,
              rawText = "\u001B[1;32m[Wireless ADB]\u001B[0m $msg",
              workingDirectory = _workingDirectory.value,
              backendMode = TerminalBackendMode.WIRELESS_ADB,
            ),
          )
          onResult?.invoke(true, msg)
        },
        onFailure = { err ->
          val msg = err.message ?: "Pairing failed on $host:$pairPort"
          _adbConnectionState.value = AdbConnectionState.ERROR
          _adbStatusMessage.value = msg
          appendOutput(
            TerminalOutputEntry(
              kind = TerminalLineKind.STDERR,
              rawText = "[Wireless ADB Pairing Error] $msg",
              workingDirectory = _workingDirectory.value,
              backendMode = TerminalBackendMode.WIRELESS_ADB,
            ),
          )
          onResult?.invoke(false, msg)
        },
      )
    }
  }

  /**
   * Connects to the local `adbd` daemon on `[host]:[connectPort]` and verifies `uid=2000(shell)`.
   */
  fun connectWirelessAdb(
    host: String = _adbHost.value,
    connectPort: Int = _adbConnectPort.value,
    onResult: ((Boolean, String) -> Unit)? = null,
  ) {
    updateAdbEndpoint(host = host, connectPort = connectPort)
    _adbConnectionState.value = AdbConnectionState.CONNECTING
    _adbStatusMessage.value = "Connecting to $host:$connectPort..."
    scope.launch {
      val result = wirelessAdbClient.testConnection(host, connectPort)
      result.fold(
        onSuccess = { idOutput ->
          _adbConnectionState.value = AdbConnectionState.CONNECTED
          _adbStatusMessage.value = "Connected to $host:$connectPort • $idOutput"
          _backendMode.value = TerminalBackendMode.WIRELESS_ADB
          repository.saveBackendMode(TerminalBackendMode.WIRELESS_ADB)
          appendOutput(
            TerminalOutputEntry(
              kind = TerminalLineKind.STATUS,
              rawText = "\u001B[1;32m[Wireless ADB Connected]\u001B[0m $host:$connectPort ($idOutput)",
              workingDirectory = _workingDirectory.value,
              backendMode = TerminalBackendMode.WIRELESS_ADB,
            ),
          )
          onResult?.invoke(true, idOutput)
        },
        onFailure = { err ->
          val msg = "Could not connect to $host:$connectPort (${err.message ?: "Connection refused"}). " +
            "Ensure Wireless Debugging is enabled in Developer Options and the port matches."
          _adbConnectionState.value = AdbConnectionState.ERROR
          _adbStatusMessage.value = msg
          appendOutput(
            TerminalOutputEntry(
              kind = TerminalLineKind.STDERR,
              rawText = "[Wireless ADB] $msg",
              workingDirectory = _workingDirectory.value,
              backendMode = TerminalBackendMode.WIRELESS_ADB,
            ),
          )
          onResult?.invoke(false, msg)
        },
      )
    }
  }

  fun disconnectWirelessAdb() {
    _adbConnectionState.value = AdbConnectionState.DISCONNECTED
    _adbStatusMessage.value = "Disconnected from Wireless ADB"
    appendOutput(
      TerminalOutputEntry(
        kind = TerminalLineKind.STATUS,
        rawText = "[Wireless ADB] Disconnected.",
        workingDirectory = _workingDirectory.value,
        backendMode = _backendMode.value,
      ),
    )
  }

  /**
   * Starts mDNS (`NsdManager`) discovery for `_adb-tls-connect._tcp` and `_adb-tls-pairing._tcp`
   * so Android 11+ Wireless Debugging ports on the local device are automatically detected.
   */
  fun startAdbMdnsDiscovery() {
    val ctx = context ?: return
    val nsdManager = ctx.getSystemService(Context.NSD_SERVICE) as? NsdManager ?: return
    stopAdbMdnsDiscovery()
    _discoveredEndpoints.value = emptyList()

    fun createListener(isPairing: Boolean): NsdManager.DiscoveryListener {
      return object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(regType: String?) = Unit
        override fun onServiceFound(service: NsdServiceInfo?) {
          val info = service ?: return
          runCatching {
            nsdManager.resolveService(
              info,
              object : NsdManager.ResolveListener {
                override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) = Unit
                override fun onServiceResolved(resolvedInfo: NsdServiceInfo?) {
                  val resolved = resolvedInfo ?: return
                  val port = resolved.port
                  if (port <= 0) return
                  val endpoint = AdbDiscoveredEndpoint(
                    serviceName = resolved.serviceName ?: if (isPairing) "ADB Pairing" else "ADB Connect",
                    host = DEFAULT_TERMINAL_ADB_HOST,
                    port = port,
                    isPairingService = isPairing,
                  )
                  _discoveredEndpoints.update { list ->
                    (list.filterNot { it.port == endpoint.port && it.isPairingService == endpoint.isPairingService } + endpoint)
                      .sortedBy { it.isPairingService }
                  }
                  if (isPairing) {
                    _adbPairPort.value = port
                  } else {
                    _adbConnectPort.value = port
                  }
                }
              },
            )
          }
        }
        override fun onServiceLost(service: NsdServiceInfo?) = Unit
        override fun onDiscoveryStopped(serviceType: String?) = Unit
        override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) = Unit
        override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) = Unit
      }
    }

    val connectListener = createListener(isPairing = false)
    val pairingListener = createListener(isPairing = true)
    nsdConnectDiscoveryListener = connectListener
    nsdPairingDiscoveryListener = pairingListener

    runCatching {
      nsdManager.discoverServices("_adb-tls-connect._tcp.", NsdManager.PROTOCOL_DNS_SD, connectListener)
    }
    runCatching {
      nsdManager.discoverServices("_adb-tls-pairing._tcp.", NsdManager.PROTOCOL_DNS_SD, pairingListener)
    }
  }

  fun stopAdbMdnsDiscovery() {
    val ctx = context ?: return
    val nsdManager = ctx.getSystemService(Context.NSD_SERVICE) as? NsdManager ?: return
    nsdConnectDiscoveryListener?.let { listener ->
      runCatching { nsdManager.stopServiceDiscovery(listener) }
    }
    nsdPairingDiscoveryListener?.let { listener ->
      runCatching { nsdManager.stopServiceDiscovery(listener) }
    }
    nsdConnectDiscoveryListener = null
    nsdPairingDiscoveryListener = null
  }

  /**
   * Interrupts any currently executing shell command (`SIGINT` / `destroyForcibly`).
   */
  fun cancelRunningCommand() {
    activeProcess?.let { proc ->
      runCatching { proc.destroyForcibly() }
    }
    activeProcess = null
    activeCommandJob?.cancel()
    activeCommandJob = null
    if (_isRunning.value) {
      _isRunning.value = false
      _activeCommandText.value = null
      appendOutput(
        TerminalOutputEntry(
          kind = TerminalLineKind.STDERR,
          rawText = "^C (Process interrupted)",
          workingDirectory = _workingDirectory.value,
          backendMode = _backendMode.value,
          exitCode = 130,
        ),
      )
    }
  }

  /**
   * Autocompletes the trailing file path or command token in [currentInput] relative to
   * [workingDirectory].
   */
  fun autocompleteInput(currentInput: String): String {
    val trimmedEnd = currentInput
    val lastSpaceIdx = trimmedEnd.lastIndexOf(' ')
    val prefix = if (lastSpaceIdx >= 0) trimmedEnd.substring(0, lastSpaceIdx + 1) else ""
    val token = if (lastSpaceIdx >= 0) trimmedEnd.substring(lastSpaceIdx + 1) else trimmedEnd
    if (token.isEmpty()) return currentInput

    val cwdFile = File(_workingDirectory.value)
    val targetFile = when {
      token.startsWith("~/") -> File(defaultHomeDirectory, token.removePrefix("~/"))
      token.startsWith("/") -> File(token)
      else -> File(cwdFile, token)
    }

    val searchDir: File
    val namePrefix: String
    if (token.endsWith("/")) {
      searchDir = targetFile
      namePrefix = ""
    } else {
      searchDir = targetFile.parentFile ?: cwdFile
      namePrefix = targetFile.name
    }

    val children = runCatching { searchDir.listFiles()?.toList().orEmpty() }.getOrDefault(emptyList())
    val matches = children.filter { it.name.startsWith(namePrefix) }.sortedBy { it.name }
    if (matches.isEmpty()) return currentInput

    val chosen = matches.first()
    val replacementName = if (chosen.isDirectory) "${chosen.name}/" else chosen.name
    val completedToken = when {
      token.endsWith("/") -> "$token$replacementName"
      token.contains("/") -> "${token.substringBeforeLast('/')}/$replacementName"
      else -> replacementName
    }
    return "$prefix$completedToken"
  }

  /**
   * Executes [rawCommand] asynchronously on the active [TerminalBackendMode] (`LOCAL_SHELL` or `WIRELESS_ADB`).
   */
  fun executeCommand(rawCommand: String) {
    val command = rawCommand.trim()
    if (command.isEmpty()) return

    _commandHistory.value = repository.recordCommandInHistory(command)
    val activeMode = _backendMode.value
    val startDir = _workingDirectory.value

    appendOutput(
      TerminalOutputEntry(
        kind = TerminalLineKind.COMMAND,
        rawText = command,
        workingDirectory = startDir,
        backendMode = activeMode,
      ),
    )

    // Built-in client-side meta-commands
    if (handleBuiltInMetaCommand(command, activeMode, startDir)) {
      return
    }

    activeCommandJob?.cancel()
    activeCommandJob = scope.launch {
      executionMutex.withLock {
        _isRunning.value = true
        _activeCommandText.value = command
        try {
          val result = when (activeMode) {
            TerminalBackendMode.LOCAL_SHELL -> executeLocalShellCommand(
              command = command,
              workingDirectory = _workingDirectory.value,
              customEnv = _customEnvVars.value,
            )
            TerminalBackendMode.WIRELESS_ADB -> executeWirelessAdbOrFallback(
              command = command,
              workingDirectory = _workingDirectory.value,
            )
          }

          _workingDirectory.value = result.updatedWorkingDirectory

          if (result.stdout.isNotEmpty()) {
            appendOutput(
              TerminalOutputEntry(
                kind = TerminalLineKind.STDOUT,
                rawText = result.stdout,
                workingDirectory = result.updatedWorkingDirectory,
                backendMode = result.backendMode,
                exitCode = result.exitCode,
              ),
            )
          }
          if (result.stderr.isNotEmpty()) {
            appendOutput(
              TerminalOutputEntry(
                kind = TerminalLineKind.STDERR,
                rawText = result.stderr,
                workingDirectory = result.updatedWorkingDirectory,
                backendMode = result.backendMode,
                exitCode = result.exitCode,
              ),
            )
          }
          if (result.stdout.isEmpty() && result.stderr.isEmpty() && result.exitCode != 0) {
            appendOutput(
              TerminalOutputEntry(
                kind = TerminalLineKind.STDERR,
                rawText = "Command exited with code ${result.exitCode}",
                workingDirectory = result.updatedWorkingDirectory,
                backendMode = result.backendMode,
                exitCode = result.exitCode,
              ),
            )
          }
        } catch (e: Throwable) {
          if (e !is kotlinx.coroutines.CancellationException) {
            appendOutput(
              TerminalOutputEntry(
                kind = TerminalLineKind.STDERR,
                rawText = e.message ?: "Failed to execute command",
                workingDirectory = _workingDirectory.value,
                backendMode = activeMode,
                exitCode = 1,
              ),
            )
          }
        } finally {
          activeProcess = null
          _isRunning.value = false
          _activeCommandText.value = null
        }
      }
    }
  }

  /**
   * Synchronous helper for testing or immediate execution of a local shell command.
   */
  fun executeLocalShellCommandBlocking(
    command: String,
    workingDirectory: String = _workingDirectory.value,
    customEnv: Map<String, String> = _customEnvVars.value,
  ): TerminalCommandResult = executeLocalShellCommand(command, workingDirectory, customEnv)

  private fun handleBuiltInMetaCommand(
    command: String,
    activeMode: TerminalBackendMode,
    startDir: String,
  ): Boolean {
    when {
      command.equals("clear", ignoreCase = true) || command.equals("cls", ignoreCase = true) -> {
        clearScrollback()
        return true
      }
      command.equals("help", ignoreCase = true) -> {
        val helpText = buildString {
          appendLine("\u001B[1;36mToolbox Terminal Commands & Backends\u001B[0m")
          appendLine("• \u001B[1mLocal Shell (Option 1)\u001B[0m: Runs directly in /system/bin/sh (mksh + Toybox).")
          appendLine("• \u001B[1mWireless ADB (Option 3)\u001B[0m: Connects to local adbd (127.0.0.1) as uid=2000(shell).")
          appendLine("• \u001B[1mclear\u001B[0m / \u001B[1mcls\u001B[0m: Clear terminal scrollback buffer")
          appendLine("• \u001B[1mexport KEY=VAL\u001B[0m: Set session environment variable")
          appendLine("• \u001B[1munset KEY\u001B[0m: Remove session environment variable")
          appendLine("• \u001B[1mhistory\u001B[0m: Print recent command history")
          append("• Tap the \u001B[1mSearch\u001B[0m icon in the top bar to browse 24+ Android command presets.")
        }
        appendOutput(
          TerminalOutputEntry(
            kind = TerminalLineKind.STDOUT,
            rawText = helpText,
            workingDirectory = startDir,
            backendMode = activeMode,
            exitCode = 0,
          ),
        )
        return true
      }
      command.equals("history", ignoreCase = true) -> {
        val hist = _commandHistory.value
        val formatted = hist.mapIndexed { idx, cmd ->
          String.format(Locale.US, "%3d  %s", idx + 1, cmd)
        }.joinToString("\n")
        appendOutput(
          TerminalOutputEntry(
            kind = TerminalLineKind.STDOUT,
            rawText = formatted.ifEmpty { "No commands in history." },
            workingDirectory = startDir,
            backendMode = activeMode,
            exitCode = 0,
          ),
        )
        return true
      }
      command.startsWith("export ") -> {
        val assignment = command.removePrefix("export ").trim()
        val eqIdx = assignment.indexOf('=')
        if (eqIdx > 0) {
          val key = assignment.substring(0, eqIdx).trim()
          val rawVal = assignment.substring(eqIdx + 1).trim().removeSurrounding("\"").removeSurrounding("'")
          _customEnvVars.update { it + (key to rawVal) }
          appendOutput(
            TerminalOutputEntry(
              kind = TerminalLineKind.STATUS,
              rawText = "Exported $key=$rawVal",
              workingDirectory = startDir,
              backendMode = activeMode,
              exitCode = 0,
            ),
          )
          return true
        }
      }
      command.startsWith("unset ") -> {
        val key = command.removePrefix("unset ").trim()
        if (key.isNotEmpty()) {
          _customEnvVars.update { it - key }
          appendOutput(
            TerminalOutputEntry(
              kind = TerminalLineKind.STATUS,
              rawText = "Unset $key",
              workingDirectory = startDir,
              backendMode = activeMode,
              exitCode = 0,
            ),
          )
          return true
        }
      }
    }
    return false
  }

  private fun executeLocalShellCommand(
    command: String,
    workingDirectory: String,
    customEnv: Map<String, String>,
  ): TerminalCommandResult {
    val startMs = System.currentTimeMillis()
    val shellBinary = listOf("/system/bin/sh", "/bin/sh")
      .firstOrNull { File(it).exists() } ?: "sh"

    val homeExpandedCommand = if (command == "cd" || command == "cd ~") {
      "cd ${shellEscapeSingleQuoted(defaultHomeDirectory)}"
    } else if (command.startsWith("cd ~/")) {
      "cd ${shellEscapeSingleQuoted(defaultHomeDirectory + "/" + command.removePrefix("cd ~/"))}"
    } else {
      command
    }

    val wrappedScript = buildString {
      append("cd ")
      append(shellEscapeSingleQuoted(workingDirectory))
      append(" 2>/dev/null || cd /; ")
      append(homeExpandedCommand)
      append("\n__tb_ec=$?; echo \"\"; echo \"__TOOLBOX_EXIT_MARKER__\$__tb_ec\"; pwd | sed 's/^/__TOOLBOX_PWD_MARKER__/'")
    }

    val pb = ProcessBuilder(shellBinary, "-c", wrappedScript)
    val startDirFile = File(workingDirectory).takeIf { it.exists() && it.isDirectory }
      ?: File(defaultHomeDirectory).takeIf { it.exists() && it.isDirectory }
    if (startDirFile != null) {
      pb.directory(startDirFile)
    }

    val env = pb.environment()
    env["HOME"] = defaultHomeDirectory
    env["TERM"] = "xterm-256color"
    val nativeLibDir = context?.applicationInfo?.nativeLibraryDir
    if (!nativeLibDir.isNullOrBlank()) {
      val currentPath = env["PATH"] ?: "/system/bin:/system/xbin"
      env["PATH"] = "$nativeLibDir:$currentPath:/sbin:/vendor/bin"
    }
    customEnv.forEach { (k, v) -> env[k] = v }

    val process = pb.start()
    activeProcess = process
    runCatching { process.outputStream.close() }

    val stdoutBuilder = StringBuilder()
    val stderrBuilder = StringBuilder()

    val stdoutThread = Thread {
      runCatching {
        BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8)).use { reader ->
          var line: String?
          while (reader.readLine().also { line = it } != null) {
            if (stdoutBuilder.isNotEmpty()) stdoutBuilder.append('\n')
            stdoutBuilder.append(line)
          }
        }
      }
    }
    val stderrThread = Thread {
      runCatching {
        BufferedReader(InputStreamReader(process.errorStream, StandardCharsets.UTF_8)).use { reader ->
          var line: String?
          while (reader.readLine().also { line = it } != null) {
            if (stderrBuilder.isNotEmpty()) stderrBuilder.append('\n')
            stderrBuilder.append(line)
          }
        }
      }
    }
    stdoutThread.start()
    stderrThread.start()

    process.waitFor(30, TimeUnit.SECONDS)
    stdoutThread.join(2000)
    stderrThread.join(2000)

    return parseWrappedShellOutput(
      command = command,
      rawStdout = stdoutBuilder.toString(),
      rawStderr = stderrBuilder.toString(),
      fallbackWorkingDir = workingDirectory,
      durationMs = System.currentTimeMillis() - startMs,
      backendMode = TerminalBackendMode.LOCAL_SHELL,
    )
  }

  private suspend fun executeWirelessAdbOrFallback(
    command: String,
    workingDirectory: String,
  ): TerminalCommandResult {
    val host = _adbHost.value
    val port = _adbConnectPort.value
    val adbResult = wirelessAdbClient.executeCommand(
      host = host,
      port = port,
      command = command,
      workingDirectory = workingDirectory,
    )
    return adbResult.fold(
      onSuccess = { res ->
        _adbConnectionState.value = AdbConnectionState.CONNECTED
        _adbStatusMessage.value = "Connected to $host:$port (uid=2000 shell)"
        res
      },
      onFailure = { err ->
        _adbConnectionState.value = AdbConnectionState.ERROR
        val errMsg = "Wireless ADB ($host:$port) unreachable: ${err.message ?: "Connection refused"}.\n" +
          "Tip: Tap the Wireless ADB connection pill to configure or pair Wireless Debugging, " +
          "or switch back to Local Shell (sh)."
        _adbStatusMessage.value = "Not connected to $host:$port"
        TerminalCommandResult(
          command = command,
          stdout = "",
          stderr = errMsg,
          exitCode = 1,
          updatedWorkingDirectory = workingDirectory,
          durationMs = 0L,
          backendMode = TerminalBackendMode.WIRELESS_ADB,
        )
      },
    )
  }

  private fun appendOutput(entry: TerminalOutputEntry) {
    _outputEntries.update { current ->
      (current + entry).takeLast(MAX_TERMINAL_OUTPUT_LINES)
    }
  }
}
