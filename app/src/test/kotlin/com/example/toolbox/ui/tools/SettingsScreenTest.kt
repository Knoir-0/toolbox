package com.example.toolbox.ui.tools

import androidx.compose.ui.graphics.Color
import com.example.toolbox.ui.main.DEFAULT_THEME_COLOR_SYSTEM
import com.example.toolbox.ui.main.FALLBACK_THEME_COLOR_HEX
import com.example.toolbox.ui.main.InMemoryNavigationPreferencesRepository
import com.example.toolbox.ui.main.ThemeMode
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.ToolCategory
import com.example.toolbox.ui.main.ToolSortingOption
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

  @Test
  fun toolCategories_everyToolHasCategoryAndAllCategoriesHaveTools() {
    for (tool in Tool.entries) {
      assertNotNull(tool.category)
      assertTrue(tool.category.title.isNotBlank())
    }

    for (category in ToolCategory.entries) {
      val tools = Tool.entries.filter { it.category == category }
      assertTrue("Category ${category.name} should have tools", tools.isNotEmpty())
    }

    // Specific category assignments
    assertEquals(ToolCategory.MATH_AND_MEASUREMENT, Tool.CALCULATOR.category)
    assertEquals(ToolCategory.MATH_AND_MEASUREMENT, Tool.UNIT_CONVERTER.category)
    assertEquals(ToolCategory.MATH_AND_MEASUREMENT, Tool.RULER.category)
    assertEquals(ToolCategory.AUDIO_AND_MUSIC, Tool.GUITAR_TUNER.category)
    assertEquals(ToolCategory.AUDIO_AND_MUSIC, Tool.METRONOME.category)
    assertEquals(ToolCategory.MEDIA_AND_CREATIVE, Tool.COLOR_PICKER.category)
    assertEquals(ToolCategory.MEDIA_AND_CREATIVE, Tool.IMAGE_EDITOR.category)
    assertEquals(ToolCategory.PRODUCTIVITY, Tool.TODO_LIST.category)
    assertEquals(ToolCategory.PRODUCTIVITY, Tool.FILES.category)
    assertEquals(ToolCategory.ENTERTAINMENT_AND_DEVICES, Tool.DICE_ROLLER.category)
    assertEquals(ToolCategory.ENTERTAINMENT_AND_DEVICES, Tool.BLUETOOTH_REMOTE.category)
    assertEquals(ToolCategory.SYSTEM, Tool.SETTINGS.category)
  }

  @Test
  fun toolSortingPreferences_inMemoryRepository_updatesCorrectly() {
    val repo = InMemoryNavigationPreferencesRepository()
    assertEquals(ToolSortingOption.USER_CHOICE, repo.getToolSorting())
    assertEquals(ToolSortingOption.USER_CHOICE, repo.toolSortingFlow.value)

    repo.setToolSorting(ToolSortingOption.TOOL_CATEGORIES)
    assertEquals(ToolSortingOption.TOOL_CATEGORIES, repo.getToolSorting())
    assertEquals(ToolSortingOption.TOOL_CATEGORIES, repo.toolSortingFlow.value)

    repo.setToolSorting(ToolSortingOption.USAGE)
    assertEquals(ToolSortingOption.USAGE, repo.getToolSorting())
    assertEquals(ToolSortingOption.USAGE, repo.toolSortingFlow.value)

    repo.setToolSorting(ToolSortingOption.USER_CHOICE)
    assertEquals(ToolSortingOption.USER_CHOICE, repo.getToolSorting())
    assertEquals(ToolSortingOption.USER_CHOICE, repo.toolSortingFlow.value)

    // Usage tracking
    assertTrue(repo.getToolUsageCounts().isEmpty())
    repo.incrementToolUsage(Tool.CALCULATOR)
    assertEquals(1, repo.getToolUsageCounts()[Tool.CALCULATOR])
    repo.incrementToolUsage(Tool.CALCULATOR)
    assertEquals(2, repo.getToolUsageCounts()[Tool.CALCULATOR])
    repo.resetToolUsage()
    assertTrue(repo.getToolUsageCounts().isEmpty())
  }
}
