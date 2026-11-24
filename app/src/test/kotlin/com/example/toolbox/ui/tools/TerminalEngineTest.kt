package com.example.toolbox.ui.tools

import com.example.toolbox.ui.main.Tool
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.security.interfaces.RSAPublicKey
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TerminalEngineTest {

  @Test
  fun terminalTool_isRegisteredBeforeSettings() {
    assertEquals(Tool.SETTINGS, Tool.entries.last())
    assertTrue(Tool.entries.indexOf(Tool.TERMINAL) < Tool.entries.indexOf(Tool.SETTINGS))
  }

  @Test
  fun ansiParser_stripsAndParsesSgrColorsAndBold() {
    val raw = "\u001B[1;32mToolbox\u001B[0m \u001B[31mError\u001B[0m Plain"
    assertEquals("Toolbox Error Plain", TerminalAnsiParser.stripAnsi(raw))

    val segments = TerminalAnsiParser.parse(raw)
    assertEquals(4, segments.size)
    assertEquals("Toolbox", segments[0].text)
    assertTrue(segments[0].isBold)
    assertEquals(32, segments[0].fgAnsiCode)

    assertEquals(" ", segments[1].text)
    assertFalse(segments[1].isBold)
    assertEquals(null, segments[1].fgAnsiCode)

    assertEquals("Error", segments[2].text)
    assertEquals(31, segments[2].fgAnsiCode)

    assertEquals(" Plain", segments[3].text)
    assertEquals(null, segments[3].fgAnsiCode)
  }

  @Test
  fun adbProtocol_packetEncodeAndDecodeRoundTrips() {
    val payload = "shell:id\u0000".toByteArray(StandardCharsets.UTF_8)
    val packet = AdbProtocol.AdbPacket(
      command = AdbProtocol.CMD_OPEN,
      arg0 = 1,
      arg1 = 0,
      payload = payload,
    )
    assertEquals("OPEN", packet.commandName)

    val encoded = packet.encode()
    assertEquals(24 + payload.size, encoded.size)

    val header = encoded.copyOfRange(0, 24)
    val body = encoded.copyOfRange(24, encoded.size)
    val decoded = AdbProtocol.AdbPacket.decode(header, body)

    assertEquals(AdbProtocol.CMD_OPEN, decoded.command)
    assertEquals(1, decoded.arg0)
    assertEquals(0, decoded.arg1)
    assertArrayEquals(payload, decoded.payload)

    val buf = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
    assertEquals(AdbProtocol.CMD_OPEN, buf.int)
    assertEquals(1, buf.int)
    assertEquals(0, buf.int)
    assertEquals(payload.size, buf.int)
    val expectedChecksum = payload.fold(0) { acc, b -> acc + (b.toInt() and 0xFF) }
    assertEquals(expectedChecksum, buf.int)
    assertEquals(AdbProtocol.CMD_OPEN xor -1, buf.int)
  }

  @Test
  fun adbProtocol_rsaKeyGenerationAndSigningWorks() {
    val repo = TerminalPreferencesRepository(context = null)
    val kp = repo.getOrCreateAdbRsaKeyPair()
    assertNotNull(kp.public)
    assertNotNull(kp.private)

    val pubBytes = AdbProtocol.encodeAndroidRsaPublicKey(kp.public as RSAPublicKey, "toolbox@test")
    val pubStr = String(pubBytes, StandardCharsets.UTF_8)
    assertTrue(pubStr.endsWith(" toolbox@test\u0000"))

    val token = ByteArray(20) { (it + 1).toByte() }
    val sig = AdbProtocol.signAdbToken(kp.private, token)
    assertEquals(256, sig.size)
  }

  @Test
  fun localShellExecution_runsCommandAndTracksWorkingDirectory() {
    val controller = TerminalController(context = null)
    assertEquals(TerminalBackendMode.LOCAL_SHELL, controller.backendMode.value)

    val echoRes = controller.executeLocalShellCommandBlocking("echo hello_toolbox")
    assertEquals(0, echoRes.exitCode)
    assertEquals("hello_toolbox", echoRes.stdout.trim())
    assertEquals(TerminalBackendMode.LOCAL_SHELL, echoRes.backendMode)
  }

  @Test
  fun backendSwitch_switchesBetweenOption1LocalShellAndOption3WirelessAdb() {
    val controller = TerminalController(context = null)
    assertEquals(TerminalBackendMode.LOCAL_SHELL, controller.backendMode.value)

    controller.selectBackendMode(TerminalBackendMode.WIRELESS_ADB)
    assertEquals(TerminalBackendMode.WIRELESS_ADB, controller.backendMode.value)

    controller.selectBackendMode(TerminalBackendMode.LOCAL_SHELL)
    assertEquals(TerminalBackendMode.LOCAL_SHELL, controller.backendMode.value)
  }

  @Test
  fun catalogFilter_filtersByQueryCategoryAndFavorites() {
    val repo = TerminalPreferencesRepository(context = null)
    val favs = repo.loadFavoriteCommandIds()
    assertTrue(favs.isNotEmpty())

    val allResults = TerminalCatalog.filterPresets(
      query = "",
      category = TerminalCommandCategory.ALL,
      favoriteIds = favs,
    )
    assertEquals(TerminalCatalog.BUILT_IN_PRESETS.size, allResults.size)

    val dumpsysResults = TerminalCatalog.filterPresets(
      query = "dumpsys battery",
      category = TerminalCommandCategory.ALL,
      favoriteIds = favs,
    )
    assertTrue(dumpsysResults.any { it.id == "dump_battery" })
  }

  @Test
  fun hiddenShellEasterEggs_respondToClassicUnixCommands() {
    val controller = TerminalController(context = null)

    val deniedSandwich = controller.executeLocalShellCommandBlocking("make me a sandwich")
    assertEquals(0, deniedSandwich.exitCode)
    assertEquals(TerminalEasterEggs.MAKE_SANDWICH_DENIED, deniedSandwich.stdout)

    val sudoSandwich = controller.executeLocalShellCommandBlocking("sudo make me a sandwich")
    assertEquals(0, sudoSandwich.exitCode)
    assertEquals(TerminalEasterEggs.SUDO_MAKE_SANDWICH_GRANTED, sudoSandwich.stdout)

    val cowsayRes = controller.executeLocalShellCommandBlocking("cowsay hello toolbox")
    assertTrue(cowsayRes.stdout.contains("< hello toolbox >"))
    assertTrue(cowsayRes.stdout.contains("(oo)\\_______"))

    val slRes = controller.executeLocalShellCommandBlocking("sl")
    assertTrue(slRes.stdout.contains("Choo choo!"))

    val fortuneRes = controller.executeLocalShellCommandBlocking("fortune")
    assertTrue(GOOD_POSITIVE_MESSAGES.contains(fortuneRes.stdout))

    val neofetchRes = controller.executeLocalShellCommandBlocking("neofetch")
    assertTrue(TerminalAnsiParser.stripAnsi(neofetchRes.stdout).contains("toolbox@android"))

    val matrixRes = controller.executeLocalShellCommandBlocking("matrix")
    assertTrue(TerminalAnsiParser.stripAnsi(matrixRes.stdout).contains("Wake up, Neo..."))
  }
}
