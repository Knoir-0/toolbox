package com.example.toolbox.ui.tools

import androidx.compose.ui.graphics.Color
import com.example.toolbox.ui.main.DEFAULT_THEME_COLOR_SYSTEM
import com.example.toolbox.ui.main.FALLBACK_THEME_COLOR_HEX
import com.example.toolbox.ui.main.InMemoryNavigationPreferencesRepository
import com.example.toolbox.ui.main.ThemeMode
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.theme.createCustomColorScheme
import com.example.toolbox.ui.theme.parseHexColor
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class SettingsScreenTest {

  @Test
  fun settingsTool_isLastEntryInToolEnum() {
    val entries = Tool.entries
    assertEquals(Tool.SETTINGS, entries.last())
    assertEquals("Settings", Tool.SETTINGS.title)
  }

  @Test
  fun presetColors_containsFallbackColor8E66A6() {
    val fallbackPreset = PRESET_COLORS.find { it.hex.equals(FALLBACK_THEME_COLOR_HEX, ignoreCase = true) }
    assertNotNull(fallbackPreset)
    assertTrue(fallbackPreset!!.isFallbackDefault)
    assertEquals(Color(0xFF8E66A6), fallbackPreset.color)
  }

  @Test
  fun parseHexColor_handlesVariousFormats() {
    assertEquals(Color(0xFF8E66A6), parseHexColor("#8E66A6"))
    assertEquals(Color(0xFF8E66A6), parseHexColor("8E66A6"))
    assertEquals(Color(0xFF00668B), parseHexColor("#00668B"))

    // Invalid formats
    assertNull(parseHexColor(""))
    assertNull(parseHexColor("#123"))
    assertNull(parseHexColor("invalid"))
    assertNull(parseHexColor("#GGGGGG"))
  }

  @Test
  fun createCustomColorScheme_producesValidSchemes() {
    val lightScheme = createCustomColorScheme(Color(0xFF8E66A6), darkTheme = false)
    assertNotNull(lightScheme)
    assertEquals(Color(0xFF8E66A6), lightScheme.primary)

    val darkScheme = createCustomColorScheme(Color(0xFF8E66A6), darkTheme = true)
    assertNotNull(darkScheme)
  }

  @Test
  fun settingsPreferences_inMemoryRepository_themeAndColorFlowsWork() {
    val repo = InMemoryNavigationPreferencesRepository()

    // Defaults
    assertEquals(ThemeMode.SYSTEM, repo.getThemeMode())
    assertEquals(DEFAULT_THEME_COLOR_SYSTEM, repo.getThemeColor())
    assertTrue(repo.getHiddenTools().isEmpty())

    // Theme mode updates
    repo.setThemeMode(ThemeMode.DARK)
    assertEquals(ThemeMode.DARK, repo.getThemeMode())
    assertEquals(ThemeMode.DARK, repo.themeModeFlow.value)

    repo.setThemeMode(ThemeMode.LIGHT)
    assertEquals(ThemeMode.LIGHT, repo.getThemeMode())
    assertEquals(ThemeMode.LIGHT, repo.themeModeFlow.value)

    // Theme color updates
    repo.setThemeColor(FALLBACK_THEME_COLOR_HEX)
    assertEquals(FALLBACK_THEME_COLOR_HEX, repo.getThemeColor())
    assertEquals(FALLBACK_THEME_COLOR_HEX, repo.themeColorFlow.value)

    repo.setThemeColor("#FF5722")
    assertEquals("#FF5722", repo.getThemeColor())
    assertEquals("#FF5722", repo.themeColorFlow.value)

    // Hidden tools updates - cannot hide Settings
    repo.setHiddenTools(setOf(Tool.CALCULATOR, Tool.SETTINGS, Tool.RULER))
    val hidden = repo.getHiddenTools()
    assertTrue(hidden.contains(Tool.CALCULATOR))
    assertTrue(hidden.contains(Tool.RULER))
    assertFalse(hidden.contains(Tool.SETTINGS))
    assertEquals(hidden, repo.hiddenToolsFlow.value)
  }

  @Test
  fun configurableTools_excludesSettings() {
    val configurable = Tool.entries.filter { it != Tool.SETTINGS }
    assertFalse(configurable.contains(Tool.SETTINGS))
    assertEquals(Tool.entries.size - 1, configurable.size)
  }
}
