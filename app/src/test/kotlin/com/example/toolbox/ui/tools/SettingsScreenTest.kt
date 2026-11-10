package com.example.toolbox.ui.tools

import androidx.compose.ui.graphics.Color
import com.example.toolbox.ui.main.DEFAULT_ACCENT_COLOR_AUTO
import com.example.toolbox.ui.main.DEFAULT_THEME_COLOR_SYSTEM
import com.example.toolbox.ui.main.CategorySortingOption
import com.example.toolbox.ui.main.CategoryToolSortingOption
import com.example.toolbox.ui.main.FALLBACK_THEME_COLOR_HEX
import com.example.toolbox.ui.main.InMemoryNavigationPreferencesRepository
import com.example.toolbox.ui.main.ThemeMode
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.ToolCategory
import com.example.toolbox.ui.main.ToolSortOrder
import com.example.toolbox.ui.main.ToolSortingOption
import com.example.toolbox.ui.theme.calculateComplementaryColor
import com.example.toolbox.ui.theme.createCustomColorScheme
import com.example.toolbox.ui.theme.createTertiaryTokens
import com.example.toolbox.ui.theme.parseHexColor
import com.example.toolbox.ui.theme.resolveEffectiveAccentColor
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
    assertNotNull(lightScheme.tertiary)
    assertNotNull(lightScheme.tertiaryContainer)

    val darkScheme = createCustomColorScheme(Color(0xFF8E66A6), darkTheme = true)
    assertNotNull(darkScheme)
    assertNotNull(darkScheme.tertiary)
    assertNotNull(darkScheme.tertiaryContainer)

    // With explicit accent color
    val schemeWithAccent = createCustomColorScheme(
      seedColor = Color(0xFF00668B),
      darkTheme = false,
      accentColor = Color(0xFFE65100),
    )
    assertEquals(Color(0xFFE65100), schemeWithAccent.tertiary)
  }

  @Test
  fun calculateComplementaryColor_shiftsHue180Degrees() {
    // Ocean Blue #00668B (Hue ~196 deg) -> complement is warm orange/amber (Hue ~16 deg)
    val blue = Color(0xFF00668B)
    val blueComplement = calculateComplementaryColor(blue)
    assertTrue("Blue complement should have high red component", blueComplement.red > blueComplement.blue)

    // Amber Orange #E65100 (Hue ~21 deg) -> complement is cyan/blue (Hue ~201 deg)
    val orange = Color(0xFFE65100)
    val orangeComplement = calculateComplementaryColor(orange)
    assertTrue("Orange complement should have high blue component", orangeComplement.blue > orangeComplement.red)

    // Purple #8E66A6 (Hue ~278 deg) -> complement is lime/yellow-green (Hue ~98 deg)
    val purple = Color(0xFF8E66A6)
    val purpleComplement = calculateComplementaryColor(purple)
    assertTrue("Purple complement should have high green component", purpleComplement.green > purpleComplement.blue)
  }

  @Test
  fun resolveEffectiveAccentColor_autoResolvesComplementAndUserSetResolvesCustom() {
    // AUTO with Ocean blue theme -> complement is orange-ish
    val autoAccent = resolveEffectiveAccentColor("#00668B", DEFAULT_ACCENT_COLOR_AUTO)
    assertTrue(autoAccent.red > autoAccent.blue)

    // User set accent overrides theme color
    val userAccent = resolveEffectiveAccentColor("#00668B", "#386A20")
    assertEquals(Color(0xFF386A20), userAccent)
  }

  @Test
  fun accentPresetColors_containsExpectedOptions() {
    val orangePreset = ACCENT_PRESET_COLORS.find { it.hex.equals("#E65100", ignoreCase = true) }
    assertNotNull(orangePreset)
    assertEquals("Amber Orange", orangePreset!!.name)

    val bluePreset = ACCENT_PRESET_COLORS.find { it.hex.equals("#00668B", ignoreCase = true) }
    assertNotNull(bluePreset)

    val coralPreset = ACCENT_PRESET_COLORS.find { it.hex.equals("#FF6B57", ignoreCase = true) }
    assertNotNull(coralPreset)
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

    // Accent color updates
    assertEquals(DEFAULT_ACCENT_COLOR_AUTO, repo.getAccentColor())
    assertEquals(DEFAULT_ACCENT_COLOR_AUTO, repo.accentColorFlow.value)

    repo.setAccentColor("#E65100")
    assertEquals("#E65100", repo.getAccentColor())
    assertEquals("#E65100", repo.accentColorFlow.value)

    repo.setAccentColor(DEFAULT_ACCENT_COLOR_AUTO)
    assertEquals(DEFAULT_ACCENT_COLOR_AUTO, repo.getAccentColor())

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
    assertEquals(ToolCategory.AUDIO_AND_MUSIC, Tool.AUDIO_RECORDER.category)
    assertEquals(ToolCategory.MEDIA_AND_CREATIVE, Tool.COLOR_PICKER.category)
    assertEquals(ToolCategory.MEDIA_AND_CREATIVE, Tool.IMAGE_EDITOR.category)
    assertEquals(ToolCategory.MEDIA_AND_CREATIVE, Tool.FILE_CONVERTER.category)
    assertEquals(ToolCategory.MEDIA_AND_CREATIVE, Tool.GALLERY.category)
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

    assertEquals(CategorySortingOption.MANUAL, repo.getCategorySorting())
    assertEquals(CategorySortingOption.MANUAL, repo.categorySortingFlow.value)
    repo.setCategorySorting(CategorySortingOption.ALPHABETICAL)
    assertEquals(CategorySortingOption.ALPHABETICAL, repo.getCategorySorting())
    assertEquals(CategorySortingOption.ALPHABETICAL, repo.categorySortingFlow.value)
    repo.setCategorySorting(CategorySortingOption.USAGE)
    assertEquals(CategorySortingOption.USAGE, repo.getCategorySorting())
    assertEquals(CategorySortingOption.USAGE, repo.categorySortingFlow.value)
    repo.setCategorySorting(CategorySortingOption.MANUAL)

    assertEquals(CategoryToolSortingOption.MANUAL, repo.getCategoryToolSorting())
    assertEquals(CategoryToolSortingOption.MANUAL, repo.categoryToolSortingFlow.value)
    repo.setCategoryToolSorting(CategoryToolSortingOption.ALPHABETICAL)
    assertEquals(CategoryToolSortingOption.ALPHABETICAL, repo.getCategoryToolSorting())
    assertEquals(CategoryToolSortingOption.ALPHABETICAL, repo.categoryToolSortingFlow.value)
    repo.setCategoryToolSorting(CategoryToolSortingOption.USAGE)
    assertEquals(CategoryToolSortingOption.USAGE, repo.getCategoryToolSorting())
    assertEquals(CategoryToolSortingOption.USAGE, repo.categoryToolSortingFlow.value)
    repo.setCategoryToolSorting(CategoryToolSortingOption.MANUAL)

    repo.setToolSorting(ToolSortingOption.USAGE)
    assertEquals(ToolSortingOption.USAGE, repo.getToolSorting())
    assertEquals(ToolSortingOption.USAGE, repo.toolSortingFlow.value)

    repo.setToolSorting(ToolSortingOption.ALPHABETICAL)
    assertEquals(ToolSortingOption.ALPHABETICAL, repo.getToolSorting())
    assertEquals(ToolSortingOption.ALPHABETICAL, repo.toolSortingFlow.value)

    assertEquals(ToolSortOrder.ASCENDING, repo.getToolSortOrder())
    assertEquals(ToolSortOrder.ASCENDING, repo.toolSortOrderFlow.value)
    repo.setToolSortOrder(ToolSortOrder.DESCENDING)
    assertEquals(ToolSortOrder.DESCENDING, repo.getToolSortOrder())
    assertEquals(ToolSortOrder.DESCENDING, repo.toolSortOrderFlow.value)

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

  @Test
  fun toolCategories_sortedAlphabeticallyByTitle() {
    val sorted = ToolCategory.entries.sortedBy { it.title }
    val titles = sorted.map { it.title }
    assertEquals(
      listOf(
        "Audio & Music",
        "Entertainment & Devices",
        "Math & Measurement",
        "Media & Creative",
        "Productivity",
        "System",
      ),
      titles,
    )
  }

  @Test
  fun toolCategories_allPresentInDefaultCollapsedSet() {
    val defaultCollapsed = ToolCategory.entries.toSet()
    assertEquals(ToolCategory.entries.size, defaultCollapsed.size)
    for (cat in ToolCategory.entries) {
      assertTrue(defaultCollapsed.contains(cat))
    }
  }

  @Test
  fun settingsTool_inSystemCategory_cannotBeHiddenFromNavigation() {
    val repo = InMemoryNavigationPreferencesRepository()
    repo.setHiddenTools(setOf(Tool.SETTINGS, Tool.CALCULATOR))
    assertFalse(repo.getHiddenTools().contains(Tool.SETTINGS))
    assertTrue(repo.getHiddenTools().contains(Tool.CALCULATOR))
  }

  @Test
  fun parseStandaloneToolName_validAndInvalidCases() {
    for (tool in Tool.entries) {
      assertEquals(tool, com.example.toolbox.ui.main.parseStandaloneToolName(tool.name))
    }
    assertNull(com.example.toolbox.ui.main.parseStandaloneToolName(null))
    assertNull(com.example.toolbox.ui.main.parseStandaloneToolName(""))
    assertNull(com.example.toolbox.ui.main.parseStandaloneToolName("NON_EXISTENT_TOOL"))
  }

  @Test
  fun presetColorPresets_canBeFoundByHexCaseInsensitive() {
    for (preset in PRESET_COLORS) {
      val foundLower = PRESET_COLORS.find { it.hex.equals(preset.hex.lowercase(), ignoreCase = true) }
      val foundUpper = PRESET_COLORS.find { it.hex.equals(preset.hex.uppercase(), ignoreCase = true) }
      assertEquals(preset, foundLower)
      assertEquals(preset, foundUpper)
    }
  }

  @Test
  fun accentPresetColors_canBeFoundByHexCaseInsensitive() {
    for (preset in ACCENT_PRESET_COLORS) {
      val foundLower = ACCENT_PRESET_COLORS.find { it.hex.equals(preset.hex.lowercase(), ignoreCase = true) }
      val foundUpper = ACCENT_PRESET_COLORS.find { it.hex.equals(preset.hex.uppercase(), ignoreCase = true) }
      assertEquals(preset, foundLower)
      assertEquals(preset, foundUpper)
    }
  }

  @Test
  fun exportAndImportSettingsJson_roundTripPreservesAllSettings() {
    val sourceRepo = InMemoryNavigationPreferencesRepository()
    sourceRepo.setToolsPerRow(5)
    sourceRepo.setThemeMode(ThemeMode.DARK)
    sourceRepo.setThemeColor("#00668B")
    sourceRepo.setAccentColor("#E65100")
    sourceRepo.setHiddenTools(setOf(Tool.RULER, Tool.METRONOME))
    sourceRepo.setToolSorting(ToolSortingOption.ALPHABETICAL)
    sourceRepo.setToolSortOrder(ToolSortOrder.DESCENDING)
    sourceRepo.setCategorySorting(CategorySortingOption.USAGE)
    sourceRepo.setCategoryToolSorting(CategoryToolSortingOption.ALPHABETICAL)
    sourceRepo.setToolUsageCounts(mapOf(Tool.CALCULATOR to 7, Tool.DICE_ROLLER to 3))

    val exportedJson = com.example.toolbox.ui.main.exportSettingsToJson(sourceRepo)
    assertTrue(exportedJson.contains("toolbox_settings_v1"))

    val targetRepo = InMemoryNavigationPreferencesRepository()
    val imported = com.example.toolbox.ui.main.importSettingsFromJson(exportedJson, targetRepo)
    assertTrue(imported)

    assertEquals(5, targetRepo.getToolsPerRow())
    assertEquals(ThemeMode.DARK, targetRepo.getThemeMode())
    assertEquals("#00668B", targetRepo.getThemeColor())
    assertEquals("#E65100", targetRepo.getAccentColor())
    assertEquals(setOf(Tool.RULER, Tool.METRONOME), targetRepo.getHiddenTools())
    assertEquals(ToolSortingOption.ALPHABETICAL, targetRepo.getToolSorting())
    assertEquals(ToolSortOrder.DESCENDING, targetRepo.getToolSortOrder())
    assertEquals(CategorySortingOption.USAGE, targetRepo.getCategorySorting())
    assertEquals(CategoryToolSortingOption.ALPHABETICAL, targetRepo.getCategoryToolSorting())
    assertEquals(mapOf(Tool.CALCULATOR to 7, Tool.DICE_ROLLER to 3), targetRepo.getToolUsageCounts())
  }

  @Test
  fun importSettingsFromJson_rejectsInvalidOrUnrecognizedJson() {
    val repo = InMemoryNavigationPreferencesRepository()
    assertFalse(com.example.toolbox.ui.main.importSettingsFromJson("", repo))
    assertFalse(com.example.toolbox.ui.main.importSettingsFromJson("not-json", repo))
    assertFalse(com.example.toolbox.ui.main.importSettingsFromJson("{\"unrelated_key\": 123}", repo))
  }

  @Test
  fun resetAllSettingsToDefaults_restoresEveryPreference() {
    val repo = InMemoryNavigationPreferencesRepository()
    repo.setToolsPerRow(6)
    repo.setThemeMode(ThemeMode.DARK)
    repo.setThemeColor("#386A20")
    repo.setAccentColor("#B3261E")
    repo.setHiddenTools(setOf(Tool.CALCULATOR))
    repo.setToolSorting(ToolSortingOption.USAGE)
    repo.setToolSortOrder(ToolSortOrder.DESCENDING)
    repo.setCategorySorting(CategorySortingOption.ALPHABETICAL)
    repo.setCategoryToolSorting(CategoryToolSortingOption.USAGE)
    repo.incrementToolUsage(Tool.CALCULATOR)

    com.example.toolbox.ui.main.resetAllSettingsToDefaults(repo)

    assertEquals(com.example.toolbox.ui.main.DEFAULT_TOOLS_PER_ROW, repo.getToolsPerRow())
    assertEquals(ThemeMode.SYSTEM, repo.getThemeMode())
    assertEquals(DEFAULT_THEME_COLOR_SYSTEM, repo.getThemeColor())
    assertEquals(DEFAULT_ACCENT_COLOR_AUTO, repo.getAccentColor())
    assertTrue(repo.getHiddenTools().isEmpty())
    assertEquals(ToolSortingOption.USER_CHOICE, repo.getToolSorting())
    assertEquals(ToolSortOrder.ASCENDING, repo.getToolSortOrder())
    assertEquals(CategorySortingOption.MANUAL, repo.getCategorySorting())
    assertEquals(CategoryToolSortingOption.MANUAL, repo.getCategoryToolSorting())
    assertTrue(repo.getToolUsageCounts().isEmpty())
  }
}
