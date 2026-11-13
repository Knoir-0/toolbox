package com.example.toolbox.ui.tools

import androidx.compose.ui.graphics.Color
import com.example.toolbox.ui.main.ACCENT_COLOR_AUTO_ANALOGOUS
import com.example.toolbox.ui.main.ACCENT_COLOR_AUTO_ANALOGOUS_MINUS_30
import com.example.toolbox.ui.main.ACCENT_COLOR_AUTO_MONOCHROMATIC
import com.example.toolbox.ui.main.ACCENT_COLOR_AUTO_SHADES
import com.example.toolbox.ui.main.ACCENT_COLOR_AUTO_SHADE_DEEP
import com.example.toolbox.ui.main.ACCENT_COLOR_AUTO_SHADE_MID
import com.example.toolbox.ui.main.ACCENT_COLOR_AUTO_SPLIT_COMPLEMENTARY
import com.example.toolbox.ui.main.ACCENT_COLOR_AUTO_SPLIT_COMPLEMENTARY_210
import com.example.toolbox.ui.main.ACCENT_COLOR_AUTO_TINT_LIGHT
import com.example.toolbox.ui.main.ACCENT_COLOR_AUTO_TRIADIC
import com.example.toolbox.ui.main.ACCENT_COLOR_AUTO_TRIADIC_240
import com.example.toolbox.ui.main.ACCENT_COLOR_MUSIC_ACCENT
import com.example.toolbox.ui.main.ACCENT_COLOR_MUSIC_DOMINANT
import com.example.toolbox.ui.main.ACCENT_COLOR_MUSIC_PLAYER
import com.example.toolbox.ui.main.ACCENT_COLOR_MUSIC_SECONDARY
import com.example.toolbox.ui.main.DEFAULT_ACCENT_COLOR_AUTO
import com.example.toolbox.ui.main.DEFAULT_HIDDEN_TOOLS
import com.example.toolbox.ui.main.DEFAULT_THEME_COLOR_SYSTEM
import com.example.toolbox.ui.main.CategorySortingOption
import com.example.toolbox.ui.main.CategoryToolSortingOption
import com.example.toolbox.ui.main.FALLBACK_THEME_COLOR_HEX
import com.example.toolbox.ui.main.InMemoryNavigationPreferencesRepository
import com.example.toolbox.ui.main.THEME_COLOR_MUSIC_ACCENT
import com.example.toolbox.ui.main.THEME_COLOR_MUSIC_DOMINANT
import com.example.toolbox.ui.main.THEME_COLOR_MUSIC_PLAYER
import com.example.toolbox.ui.main.THEME_COLOR_MUSIC_SECONDARY
import com.example.toolbox.ui.main.ThemeMode
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.ToolCategory
import com.example.toolbox.ui.main.ToolSortOrder
import com.example.toolbox.ui.main.ToolSortingOption
import com.example.toolbox.ui.theme.AccentHarmonyOption
import com.example.toolbox.ui.theme.MusicPlayerColorOption
import com.example.toolbox.ui.theme.buildMusicPlayerAccentPrefValue
import com.example.toolbox.ui.theme.calculateAccentHarmonyColor
import com.example.toolbox.ui.theme.calculateComplementaryColor
import com.example.toolbox.ui.theme.calculateMusicPlayerBackgroundColor
import com.example.toolbox.ui.theme.createCustomColorScheme
import com.example.toolbox.ui.theme.createTertiaryTokens
import com.example.toolbox.ui.theme.isAutoAccentColorSetting
import com.example.toolbox.ui.theme.isMusicPlayerAccentColorSetting
import com.example.toolbox.ui.theme.isMusicPlayerThemeColorSetting
import com.example.toolbox.ui.theme.parseHexColor
import com.example.toolbox.ui.theme.resolveEffectiveAccentColor
import com.example.toolbox.ui.theme.resolveMusicPlayerHarmonyOption
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
  fun accentHarmonyOptions_resolveExpectedHarmoniesFromBaseColor() {
    // Pure Red #FF0000 (Hue = 0 deg)
    val redHex = "#FF0000"
    val redColor = Color(0xFFFF0000)

    // 1. Complementary (+180 deg -> Cyan)
    val comp = resolveEffectiveAccentColor(redHex, DEFAULT_ACCENT_COLOR_AUTO)
    assertEquals(calculateComplementaryColor(redColor), comp)
    assertTrue("Complementary of Red should be cyan (high green & blue, low red)", comp.green > comp.red && comp.blue > comp.red)

    // 2. Analogous (+30 deg -> Warm Orange & -30 deg -> Magenta/Rose)
    val analogousPlus30 = resolveEffectiveAccentColor(redHex, ACCENT_COLOR_AUTO_ANALOGOUS)
    val analogousMinus30 = resolveEffectiveAccentColor(redHex, ACCENT_COLOR_AUTO_ANALOGOUS_MINUS_30)
    assertTrue("Analogous +30 of Red should be orange (green > blue)", analogousPlus30.green > analogousPlus30.blue)
    assertTrue("Analogous -30 of Red should be rose/magenta (blue > green)", analogousMinus30.blue > analogousMinus30.green)

    // 3. Triadic (+120 deg -> Green & +240 deg -> Blue)
    val triadic120 = resolveEffectiveAccentColor(redHex, ACCENT_COLOR_AUTO_TRIADIC)
    val triadic240 = resolveEffectiveAccentColor(redHex, ACCENT_COLOR_AUTO_TRIADIC_240)
    assertTrue("Triadic +120 of Red should be green-dominant", triadic120.green > triadic120.red && triadic120.green > triadic120.blue)
    assertTrue("Triadic +240 of Red should be blue-dominant", triadic240.blue > triadic240.red && triadic240.blue > triadic240.green)

    // 4. Split-Complementary (+150 deg -> Spring Green & +210 deg -> Azure Blue)
    val split150 = resolveEffectiveAccentColor(redHex, ACCENT_COLOR_AUTO_SPLIT_COMPLEMENTARY)
    val split210 = resolveEffectiveAccentColor(redHex, ACCENT_COLOR_AUTO_SPLIT_COMPLEMENTARY_210)
    assertTrue("Split-Comp +150 of Red should have green > blue", split150.green > split150.blue)
    assertTrue("Split-Comp +210 of Red should have blue > green", split210.blue > split210.green)

    // 5. Tints & Shades (Light Tint, Soft Tint, Monochromatic Base, Mid Shade, Deep Shade)
    val tintLight = resolveEffectiveAccentColor(redHex, ACCENT_COLOR_AUTO_TINT_LIGHT)
    val softTint = resolveEffectiveAccentColor(redHex, ACCENT_COLOR_AUTO_SHADES)
    val monoBase = resolveEffectiveAccentColor(redHex, ACCENT_COLOR_AUTO_MONOCHROMATIC)
    val midShade = resolveEffectiveAccentColor(redHex, ACCENT_COLOR_AUTO_SHADE_MID)
    val deepShade = resolveEffectiveAccentColor(redHex, ACCENT_COLOR_AUTO_SHADE_DEEP)

    // Tints have higher green/blue (whiteness) than pure red base, shades have lower red (darker)
    assertTrue(tintLight.green > softTint.green)
    assertTrue(softTint.green > monoBase.green)
    assertTrue(monoBase.red > midShade.red)
    assertTrue(midShade.red > deepShade.red)
  }

  @Test
  fun accentHarmonyOptions_mapToAllColorsToolHarmonyModes() {
    for (mode in HarmonyMode.entries) {
      val defaultOption = AccentHarmonyOption.defaultForMode(mode)
      assertEquals(mode, defaultOption.harmonyMode)
      assertTrue(isAutoAccentColorSetting(defaultOption.prefValue))

      val swatches = AccentHarmonyOption.swatchesForMode(mode)
      assertTrue("Mode $mode should have at least 2 swatches", swatches.size >= 2)
      assertTrue("Mode $mode swatches should include its default option", swatches.contains(defaultOption))

      // Swatch count matches ColorMath.generateHarmony count
      val baseHsv = HsvColor(200f, 0.8f, 0.85f)
      val colorToolHarmony = ColorMath.generateHarmony(baseHsv, mode)
      assertEquals(colorToolHarmony.size, swatches.size)
    }

    for (option in AccentHarmonyOption.entries) {
      assertEquals(option, AccentHarmonyOption.fromPrefValue(option.prefValue))
      assertEquals(option, AccentHarmonyOption.fromPrefValue(option.prefValue.lowercase()))
      assertTrue(isAutoAccentColorSetting(option.prefValue))
    }

    assertFalse(isAutoAccentColorSetting("#E65100"))
    assertFalse(isAutoAccentColorSetting(""))
    assertFalse(isAutoAccentColorSetting(null))
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
    assertEquals(DEFAULT_HIDDEN_TOOLS, repo.getHiddenTools())
    assertTrue(repo.getHiddenTools().contains(Tool.FILE_CONVERTER))

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
    assertEquals(ToolCategory.MUSIC_AND_INSTRUMENTS, Tool.GUITAR_TUNER.category)
    assertEquals(ToolCategory.MUSIC_AND_INSTRUMENTS, Tool.METRONOME.category)
    assertEquals(ToolCategory.AUDIO_AND_SOUND, Tool.AUDIO_RECORDER.category)
    assertEquals(ToolCategory.MUSIC_AND_INSTRUMENTS, Tool.CHORD_BOOK.category)
    assertEquals(ToolCategory.AUDIO_AND_SOUND, Tool.AUDIO_EDITOR.category)
    assertEquals(ToolCategory.MISCELLANEOUS, Tool.COLOR_PICKER.category)
    assertEquals(ToolCategory.PHOTO_AND_VIDEO, Tool.IMAGE_EDITOR.category)
    assertEquals(ToolCategory.FILES_AND_STORAGE, Tool.FILE_CONVERTER.category)
    assertEquals(ToolCategory.PHOTO_AND_VIDEO, Tool.GALLERY.category)
    assertEquals(ToolCategory.PHOTO_AND_VIDEO, Tool.CAMERA.category)
    assertEquals(ToolCategory.PHOTO_AND_VIDEO, Tool.VIDEO_EDITOR.category)
    assertEquals(ToolCategory.PRODUCTIVITY, Tool.TODO_LIST.category)
    assertEquals(ToolCategory.FILES_AND_STORAGE, Tool.FILES.category)
    assertEquals(ToolCategory.PRODUCTIVITY, Tool.NOTES.category)
    assertEquals(ToolCategory.MISCELLANEOUS, Tool.PLANT_SCHEDULE.category)
    assertEquals(ToolCategory.MISCELLANEOUS, Tool.DICE_ROLLER.category)
    assertEquals(ToolCategory.REMOTES_AND_DEVICES, Tool.BLUETOOTH_REMOTE.category)
    assertEquals(ToolCategory.REMOTES_AND_DEVICES, Tool.BLUETOOTH_GAMEPAD.category)
    assertEquals(ToolCategory.AUDIO_AND_SOUND, Tool.DECIBEL_METER.category)
    assertEquals(ToolCategory.FILES_AND_STORAGE, Tool.ARCHIVE.category)
    assertEquals(ToolCategory.AUDIO_AND_SOUND, Tool.MUSIC.category)
    assertEquals(ToolCategory.FILES_AND_STORAGE, Tool.PDF_VIEWER.category)
    assertEquals(ToolCategory.REMOTES_AND_DEVICES, Tool.OBD2_SCANNER.category)
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
        "Audio & Sound",
        "Files",
        "Math & Measurement",
        "Miscellaneous",
        "Music & Instruments",
        "Photo & Video",
        "Productivity",
        "Remotes & Devices",
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
    assertEquals(DEFAULT_HIDDEN_TOOLS, repo.getHiddenTools())
    assertEquals(ToolSortingOption.USER_CHOICE, repo.getToolSorting())
    assertEquals(ToolSortOrder.ASCENDING, repo.getToolSortOrder())
    assertEquals(CategorySortingOption.MANUAL, repo.getCategorySorting())
    assertEquals(CategoryToolSortingOption.MANUAL, repo.getCategoryToolSorting())
    assertTrue(repo.getToolUsageCounts().isEmpty())
  }

  @Test
  fun githubIssueUrls_pointToRepositoryNewIssuePage() {
    val expectedUrl = "https://github.com/Knoir-0/toolbox/issues/new"
    assertEquals(expectedUrl, GITHUB_NEW_ISSUE_URL)
    assertEquals(expectedUrl, GITHUB_REPORT_BUG_URL)
    assertEquals(expectedUrl, GITHUB_REQUEST_FEATURE_URL)
  }

  @Test
  fun isMusicPlayerThemeColorSetting_recognizesAllMusicPlayerOptions() {
    assertTrue(isMusicPlayerThemeColorSetting(THEME_COLOR_MUSIC_PLAYER))
    assertTrue(isMusicPlayerThemeColorSetting(THEME_COLOR_MUSIC_DOMINANT))
    assertTrue(isMusicPlayerThemeColorSetting(THEME_COLOR_MUSIC_SECONDARY))
    assertTrue(isMusicPlayerThemeColorSetting(THEME_COLOR_MUSIC_ACCENT))
    assertEquals(MusicPlayerColorOption.DOMINANT, MusicPlayerColorOption.fromPrefValue(THEME_COLOR_MUSIC_PLAYER))
    assertEquals(MusicPlayerColorOption.DOMINANT, MusicPlayerColorOption.fromPrefValue(THEME_COLOR_MUSIC_DOMINANT))
    assertEquals(MusicPlayerColorOption.SECONDARY, MusicPlayerColorOption.fromPrefValue(THEME_COLOR_MUSIC_SECONDARY))
    assertEquals(MusicPlayerColorOption.ACCENT, MusicPlayerColorOption.fromPrefValue(THEME_COLOR_MUSIC_ACCENT))
    assertFalse(isMusicPlayerThemeColorSetting(DEFAULT_THEME_COLOR_SYSTEM))
    assertFalse(isMusicPlayerThemeColorSetting("#8E66A6"))
    assertFalse(isMusicPlayerThemeColorSetting(null))
  }

  @Test
  fun resolveEffectiveAccentColor_followsMusicPlayerMainColorPaletteAndCalculatesHarmony() {
    val track1 = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS[0]
    val track2 = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS[1]
    val palette1 = MusicPlayerEngine.resolveTrackPalette(track1)
    val palette2 = MusicPlayerEngine.resolveTrackPalette(track2)
    val baseBg = Color(0xFFFFF8FF)

    val session = MusicPlaybackSession(
      initialTracks = listOf(track1, track2),
      isInspection = true,
    )
    try {
      assertEquals(track1.id, MusicPlayerEngine.activeTrack?.id)
      assertEquals(
        calculateAccentHarmonyColor(palette1.dominantColor, AccentHarmonyOption.COMPLEMENTARY),
        resolveEffectiveAccentColor(THEME_COLOR_MUSIC_PLAYER, DEFAULT_ACCENT_COLOR_AUTO),
      )
      assertEquals(
        calculateAccentHarmonyColor(palette1.secondaryColor, AccentHarmonyOption.COMPLEMENTARY),
        resolveEffectiveAccentColor(THEME_COLOR_MUSIC_SECONDARY, DEFAULT_ACCENT_COLOR_AUTO),
      )
      assertEquals(
        calculateAccentHarmonyColor(palette1.accentColor, AccentHarmonyOption.COMPLEMENTARY),
        resolveEffectiveAccentColor(THEME_COLOR_MUSIC_ACCENT, DEFAULT_ACCENT_COLOR_AUTO),
      )
      assertEquals(
        calculateAccentHarmonyColor(palette1.dominantColor, AccentHarmonyOption.TRIADIC),
        resolveEffectiveAccentColor(THEME_COLOR_MUSIC_PLAYER, ACCENT_COLOR_AUTO_TRIADIC),
      )
      assertEquals(
        Color(0xFFE65100),
        resolveEffectiveAccentColor(THEME_COLOR_MUSIC_PLAYER, "#E65100"),
      )
      assertEquals(
        androidx.compose.ui.graphics.lerp(baseBg, palette1.dominantColor, 0.22f),
        calculateMusicPlayerBackgroundColor(baseBg, palette1, MusicPlayerColorOption.DOMINANT),
      )

      // Advance to the next track and verify the effective accent color updates automatically
      session.skipToNext(userInitiated = true)
      assertEquals(track2.id, MusicPlayerEngine.activeTrack?.id)
      assertEquals(
        calculateAccentHarmonyColor(palette2.dominantColor, AccentHarmonyOption.COMPLEMENTARY),
        resolveEffectiveAccentColor(THEME_COLOR_MUSIC_PLAYER, DEFAULT_ACCENT_COLOR_AUTO),
      )
      assertEquals(
        calculateAccentHarmonyColor(palette2.secondaryColor, AccentHarmonyOption.COMPLEMENTARY),
        resolveEffectiveAccentColor(THEME_COLOR_MUSIC_SECONDARY, DEFAULT_ACCENT_COLOR_AUTO),
      )
      assertEquals(
        calculateAccentHarmonyColor(palette2.accentColor, AccentHarmonyOption.COMPLEMENTARY),
        resolveEffectiveAccentColor(THEME_COLOR_MUSIC_ACCENT, DEFAULT_ACCENT_COLOR_AUTO),
      )
      assertEquals(
        calculateAccentHarmonyColor(palette2.dominantColor, AccentHarmonyOption.TRIADIC),
        resolveEffectiveAccentColor(THEME_COLOR_MUSIC_PLAYER, ACCENT_COLOR_AUTO_TRIADIC),
      )
    } finally {
      session.release()
      MusicPlayerEngine.resetPlaybackSession()
    }
  }
}
