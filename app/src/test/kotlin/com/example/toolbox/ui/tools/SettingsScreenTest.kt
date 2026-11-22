package com.example.toolbox.ui.tools

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.example.toolbox.ui.main.ACCENT_COLOR_AUTO_ANALOGOUS
import com.example.toolbox.ui.main.ACCENT_COLOR_AUTO_ANALOGOUS_MINUS_30
import com.example.toolbox.ui.main.ACCENT_COLOR_AUTO_COMPLEMENTARY
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
import com.example.toolbox.ui.main.BackgroundTexture
import com.example.toolbox.ui.main.DEFAULT_ACCENT_COLOR_AUTO
import com.example.toolbox.ui.main.DEFAULT_BACKGROUND_TEXTURE
import com.example.toolbox.ui.main.DEFAULT_HEXAGON_ICON_BUTTONS
import com.example.toolbox.ui.main.DEFAULT_HIDDEN_TOOLS
import com.example.toolbox.ui.main.DEFAULT_MUSIC_PLAYER_COLOR_OPTION
import com.example.toolbox.ui.main.DEFAULT_MUSIC_PLAYER_THEME_ENABLED
import com.example.toolbox.ui.main.DEFAULT_THEME_COLOR_SYSTEM
import com.example.toolbox.ui.main.CategorySortingOption
import com.example.toolbox.ui.main.CategoryToolSortingOption
import com.example.toolbox.ui.main.FALLBACK_THEME_COLOR_HEX
import com.example.toolbox.ui.main.InMemoryNavigationPreferencesRepository
import com.example.toolbox.ui.main.NavigationPreferencesBridge
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
import com.example.toolbox.ui.theme.createHorizontalHexagonVertices
import com.example.toolbox.ui.theme.createVerticalSidedHexagonVertices
import com.example.toolbox.ui.theme.shouldUseHexagonIconOutline
import com.example.toolbox.ui.theme.shouldUseHorizontalHexagonActionButtonOutline
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
  fun resolveEffectiveAccentColor_autoResolvesThemeDefaultAndUserSetResolvesCustom() {
    val oceanBlue = Color(0xFF00668B)

    // AUTO in dark theme -> Analogous +30° (ANALOGOUS)
    val darkAutoAccent = resolveEffectiveAccentColor(
      themeColorHex = "#00668B",
      accentColorSetting = DEFAULT_ACCENT_COLOR_AUTO,
      darkTheme = true,
    )
    assertEquals(
      calculateAccentHarmonyColor(oceanBlue, AccentHarmonyOption.ANALOGOUS),
      darkAutoAccent,
    )

    // AUTO in light theme -> Analogous +30° (ANALOGOUS)
    val lightAutoAccent = resolveEffectiveAccentColor(
      themeColorHex = "#00668B",
      accentColorSetting = DEFAULT_ACCENT_COLOR_AUTO,
      darkTheme = false,
    )
    assertEquals(
      calculateAccentHarmonyColor(oceanBlue, AccentHarmonyOption.ANALOGOUS),
      lightAutoAccent,
    )

    // Explicit complementary option -> orange-ish complement
    val compAccent = resolveEffectiveAccentColor("#00668B", ACCENT_COLOR_AUTO_COMPLEMENTARY)
    assertTrue(compAccent.red > compAccent.blue)

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
    val comp = resolveEffectiveAccentColor(redHex, ACCENT_COLOR_AUTO_COMPLEMENTARY)
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
    // Analogous is at the front of the list, matching between Settings and Colors tool
    assertEquals(HarmonyMode.ANALOGOUS, HarmonyMode.entries.first())
    assertEquals(
      listOf(
        HarmonyMode.ANALOGOUS,
        HarmonyMode.SHADES,
        HarmonyMode.COMPLEMENTARY,
        HarmonyMode.TRIADIC,
        HarmonyMode.SPLIT_COMPLEMENTARY,
      ),
      HarmonyMode.entries,
    )

    // Default accent harmony is Analogous (+30°)
    assertEquals(AccentHarmonyOption.ANALOGOUS, AccentHarmonyOption.defaultForTheme(darkTheme = true))
    assertEquals(AccentHarmonyOption.ANALOGOUS, AccentHarmonyOption.defaultForTheme(darkTheme = false))
    assertEquals(
      AccentHarmonyOption.ANALOGOUS,
      AccentHarmonyOption.fromPrefValue(DEFAULT_ACCENT_COLOR_AUTO, darkTheme = true),
    )
    assertEquals(
      AccentHarmonyOption.ANALOGOUS,
      AccentHarmonyOption.fromPrefValue(DEFAULT_ACCENT_COLOR_AUTO, darkTheme = false),
    )
    assertEquals(
      AccentHarmonyOption.ANALOGOUS,
      AccentHarmonyOption.defaultForMode(HarmonyMode.ANALOGOUS, darkTheme = true),
    )
    assertEquals(
      AccentHarmonyOption.ANALOGOUS,
      AccentHarmonyOption.defaultForMode(HarmonyMode.ANALOGOUS, darkTheme = false),
    )
    assertEquals(
      AccentHarmonyOption.SHADES,
      AccentHarmonyOption.defaultForMode(HarmonyMode.SHADES, darkTheme = true),
    )
    assertEquals(
      AccentHarmonyOption.SHADE_MID,
      AccentHarmonyOption.defaultForMode(HarmonyMode.SHADES, darkTheme = false),
    )

    for (mode in HarmonyMode.entries) {
      val defaultDarkOption = AccentHarmonyOption.defaultForMode(mode, darkTheme = true)
      val defaultLightOption = AccentHarmonyOption.defaultForMode(mode, darkTheme = false)
      assertEquals(mode, defaultDarkOption.harmonyMode)
      assertEquals(mode, defaultLightOption.harmonyMode)
      assertTrue(isAutoAccentColorSetting(defaultDarkOption.prefValue))
      assertTrue(isAutoAccentColorSetting(defaultLightOption.prefValue))

      val swatches = AccentHarmonyOption.swatchesForMode(mode)
      assertTrue("Mode $mode should have at least 2 swatches", swatches.size >= 2)
      assertTrue("Mode $mode swatches should include its dark default option", swatches.contains(defaultDarkOption))
      assertTrue("Mode $mode swatches should include its light default option", swatches.contains(defaultLightOption))

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

    assertTrue(isAutoAccentColorSetting(DEFAULT_ACCENT_COLOR_AUTO))
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
    assertEquals(ToolCategory.PHOTO_AND_VIDEO, Tool.COLOR_PICKER.category)
    assertEquals(ToolCategory.PHOTO_AND_VIDEO, Tool.IMAGE.category)
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
    assertEquals(ToolCategory.MATH_AND_MEASUREMENT, Tool.DECIBEL_METER.category)
    assertEquals(ToolCategory.FILES_AND_STORAGE, Tool.ARCHIVE.category)
    assertEquals(ToolCategory.AUDIO_AND_SOUND, Tool.MUSIC.category)
    assertEquals(ToolCategory.FILES_AND_STORAGE, Tool.PDF_VIEWER.category)
    assertEquals(ToolCategory.REMOTES_AND_DEVICES, Tool.OBD2_SCANNER.category)
    assertEquals(ToolCategory.MATH_AND_MEASUREMENT, Tool.ELEMENTS.category)
    assertEquals(ToolCategory.MISCELLANEOUS, Tool.GOOD.category)
    assertEquals(ToolCategory.SYSTEM, Tool.SEARCH.category)
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
        "Audio",
        "Devices",
        "Files",
        "Instruments",
        "Lifestyle",
        "Math & Measure",
        "Photo & Video",
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
    sourceRepo.setMusicPlayerThemeEnabled(false)
    sourceRepo.setMusicPlayerColorOption(THEME_COLOR_MUSIC_SECONDARY)
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
    assertFalse(targetRepo.getMusicPlayerThemeEnabled())
    assertEquals(THEME_COLOR_MUSIC_SECONDARY, targetRepo.getMusicPlayerColorOption())
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
    repo.setMusicPlayerThemeEnabled(false)
    repo.setMusicPlayerColorOption(THEME_COLOR_MUSIC_ACCENT)
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
    assertEquals(DEFAULT_MUSIC_PLAYER_THEME_ENABLED, repo.getMusicPlayerThemeEnabled())
    assertTrue(repo.getMusicPlayerThemeEnabled())
    assertEquals(DEFAULT_MUSIC_PLAYER_COLOR_OPTION, repo.getMusicPlayerColorOption())
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
      initialPlayerExpanded = true,
      isInspection = true,
    )
    try {
      assertEquals(track1.id, MusicPlayerEngine.activeTrack?.id)
      assertEquals(
        calculateAccentHarmonyColor(palette1.dominantColor, AccentHarmonyOption.ANALOGOUS),
        resolveEffectiveAccentColor(THEME_COLOR_MUSIC_PLAYER, DEFAULT_ACCENT_COLOR_AUTO, darkTheme = false),
      )
      assertEquals(
        calculateAccentHarmonyColor(palette1.dominantColor, AccentHarmonyOption.ANALOGOUS),
        resolveEffectiveAccentColor(THEME_COLOR_MUSIC_PLAYER, DEFAULT_ACCENT_COLOR_AUTO, darkTheme = true),
      )
      assertEquals(
        calculateAccentHarmonyColor(palette1.secondaryColor, AccentHarmonyOption.ANALOGOUS),
        resolveEffectiveAccentColor(THEME_COLOR_MUSIC_SECONDARY, DEFAULT_ACCENT_COLOR_AUTO, darkTheme = false),
      )
      assertEquals(
        calculateAccentHarmonyColor(palette1.accentColor, AccentHarmonyOption.ANALOGOUS),
        resolveEffectiveAccentColor(THEME_COLOR_MUSIC_ACCENT, DEFAULT_ACCENT_COLOR_AUTO, darkTheme = false),
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
        calculateAccentHarmonyColor(palette2.dominantColor, AccentHarmonyOption.ANALOGOUS),
        resolveEffectiveAccentColor(THEME_COLOR_MUSIC_PLAYER, DEFAULT_ACCENT_COLOR_AUTO, darkTheme = false),
      )
      assertEquals(
        calculateAccentHarmonyColor(palette2.dominantColor, AccentHarmonyOption.ANALOGOUS),
        resolveEffectiveAccentColor(THEME_COLOR_MUSIC_PLAYER, DEFAULT_ACCENT_COLOR_AUTO, darkTheme = true),
      )
      assertEquals(
        calculateAccentHarmonyColor(palette2.secondaryColor, AccentHarmonyOption.ANALOGOUS),
        resolveEffectiveAccentColor(THEME_COLOR_MUSIC_SECONDARY, DEFAULT_ACCENT_COLOR_AUTO, darkTheme = false),
      )
      assertEquals(
        calculateAccentHarmonyColor(palette2.accentColor, AccentHarmonyOption.ANALOGOUS),
        resolveEffectiveAccentColor(THEME_COLOR_MUSIC_ACCENT, DEFAULT_ACCENT_COLOR_AUTO, darkTheme = false),
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

  @Test
  fun musicPlayerThemeCheckbox_checkedByDefault_themesWhenPlaying_andFallsBackWhenNotPlayingOrUnchecked() {
    val repo = InMemoryNavigationPreferencesRepository()
    // 1. Checked by default, with fallback themeColor = SYSTEM
    assertTrue(repo.getMusicPlayerThemeEnabled())
    assertEquals(DEFAULT_MUSIC_PLAYER_COLOR_OPTION, repo.getMusicPlayerColorOption())
    assertEquals(DEFAULT_THEME_COLOR_SYSTEM, repo.getThemeColor())

    // 2. Selecting a user fallback theme color keeps Music Player checkbox checked
    repo.setThemeColor("#00668B")
    assertEquals("#00668B", repo.getThemeColor())
    assertTrue(repo.getMusicPlayerThemeEnabled())

    val track1 = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS[0]
    val palette1 = MusicPlayerEngine.resolveTrackPalette(track1)
    MusicPlayerEngine.updateActiveTrackPalette(track1, palette1)

    try {
      // 3. When checked AND music is playing, themes according to music player
      assertEquals(
        calculateAccentHarmonyColor(palette1.dominantColor, AccentHarmonyOption.ANALOGOUS),
        resolveEffectiveAccentColor(
          themeColorHex = repo.getThemeColor(),
          accentColorSetting = DEFAULT_ACCENT_COLOR_AUTO,
          musicPalette = palette1,
          musicPlayerThemeEnabled = repo.getMusicPlayerThemeEnabled(),
          musicPlayerColorOption = repo.getMusicPlayerColorOption(),
          isMusicPlaying = true,
          darkTheme = false,
        ),
      )

      // 4. When checked and music is NOT playing, falls back to user's selected theme color (#00668B)
      assertEquals(
        calculateAccentHarmonyColor(Color(0xFF00668B), AccentHarmonyOption.ANALOGOUS),
        resolveEffectiveAccentColor(
          themeColorHex = repo.getThemeColor(),
          accentColorSetting = DEFAULT_ACCENT_COLOR_AUTO,
          musicPalette = palette1,
          musicPlayerThemeEnabled = repo.getMusicPlayerThemeEnabled(),
          musicPlayerColorOption = repo.getMusicPlayerColorOption(),
          isMusicPlaying = false,
          darkTheme = false,
        ),
      )

      // 5. When unchecked, even if music IS playing, falls back to user's selected theme color (#00668B)
      repo.setMusicPlayerThemeEnabled(false)
      assertFalse(repo.getMusicPlayerThemeEnabled())
      assertEquals(
        calculateAccentHarmonyColor(Color(0xFF00668B), AccentHarmonyOption.ANALOGOUS),
        resolveEffectiveAccentColor(
          themeColorHex = repo.getThemeColor(),
          accentColorSetting = DEFAULT_ACCENT_COLOR_AUTO,
          musicPalette = palette1,
          musicPlayerThemeEnabled = repo.getMusicPlayerThemeEnabled(),
          musicPlayerColorOption = repo.getMusicPlayerColorOption(),
          isMusicPlaying = true,
          darkTheme = false,
        ),
      )
    } finally {
      MusicPlayerEngine.resetPlaybackSession()
    }
  }

  @Test
  fun permissionsSection_tracksUsagePerToolWithTimestamps_andSupportsGrantAndRevoke() {
    val repo = InMemoryNavigationPreferencesRepository()

    // 1. All app permissions are present and granted by default with 0 uses
    assertTrue(com.example.toolbox.ui.main.AppPermission.entries.isNotEmpty())
    com.example.toolbox.ui.main.AppPermission.entries.forEach { perm ->
      assertTrue(repo.isPermissionGranted(perm))
      assertEquals(0, repo.getTotalPermissionUsageCount(perm))
      assertEquals(0L, repo.getLastPermissionUsedTimestamp(perm))
      assertTrue(perm.toolsUsingPermission.isNotEmpty())
    }
    assertEquals(
      listOf(
        Tool.FILES,
        Tool.GALLERY,
        Tool.AUDIO_RECORDER,
        Tool.MUSIC,
      ),
      com.example.toolbox.ui.main.AppPermission.STORAGE.toolsUsingPermission,
    )

    // 2. Recording permission usage for specific tools updates counts and timestamps
    val t1 = 1_763_400_000_000L
    val t2 = 1_763_400_060_000L
    repo.recordPermissionUsage(
      com.example.toolbox.ui.main.AppPermission.CAMERA,
      Tool.CAMERA,
      timestampMs = t1,
    )
    repo.recordPermissionUsage(
      com.example.toolbox.ui.main.AppPermission.CAMERA,
      Tool.CAMERA,
      timestampMs = t2,
    )
    repo.recordPermissionUsage(
      com.example.toolbox.ui.main.AppPermission.CAMERA,
      Tool.COLOR_PICKER,
      timestampMs = t1,
    )

    val cameraToolStat = repo.getPermissionUsageForTool(
      com.example.toolbox.ui.main.AppPermission.CAMERA,
      Tool.CAMERA,
    )
    val colorPickerToolStat = repo.getPermissionUsageForTool(
      com.example.toolbox.ui.main.AppPermission.CAMERA,
      Tool.COLOR_PICKER,
    )
    assertEquals(2, cameraToolStat.useCount)
    assertEquals(t2, cameraToolStat.lastUsedTimestampMs)
    assertEquals(
      com.example.toolbox.ui.main.formatPermissionUsageTimestamp(t2),
      cameraToolStat.lastUsedFormatted,
    )
    assertEquals(1, colorPickerToolStat.useCount)
    assertEquals(t1, colorPickerToolStat.lastUsedTimestampMs)
    assertEquals(3, repo.getTotalPermissionUsageCount(com.example.toolbox.ui.main.AppPermission.CAMERA))
    assertEquals(t2, repo.getLastPermissionUsedTimestamp(com.example.toolbox.ui.main.AppPermission.CAMERA))

    // 3. Revoking a permission prevents further usage recording until re-granted
    repo.revokePermission(com.example.toolbox.ui.main.AppPermission.CAMERA)
    assertFalse(repo.isPermissionGranted(com.example.toolbox.ui.main.AppPermission.CAMERA))
    assertFalse(
      com.example.toolbox.ui.main.NavigationPreferencesBridge.isPermissionGranted(
        com.example.toolbox.ui.main.AppPermission.CAMERA
      )
    )
    repo.recordPermissionUsage(
      com.example.toolbox.ui.main.AppPermission.CAMERA,
      Tool.CAMERA,
      timestampMs = t2 + 10_000L,
    )
    assertEquals(
      2,
      repo.getPermissionUsageForTool(
        com.example.toolbox.ui.main.AppPermission.CAMERA,
        Tool.CAMERA,
      ).useCount,
    )

    // Re-granting the permission restores access and allows usage tracking again
    repo.grantPermission(com.example.toolbox.ui.main.AppPermission.CAMERA)
    assertTrue(repo.isPermissionGranted(com.example.toolbox.ui.main.AppPermission.CAMERA))
    repo.recordPermissionUsage(
      com.example.toolbox.ui.main.AppPermission.CAMERA,
      Tool.CAMERA,
      timestampMs = t2 + 20_000L,
    )
    assertEquals(
      3,
      repo.getPermissionUsageForTool(
        com.example.toolbox.ui.main.AppPermission.CAMERA,
        Tool.CAMERA,
      ).useCount,
    )
    assertEquals(
      t2 + 20_000L,
      repo.getPermissionUsageForTool(
        com.example.toolbox.ui.main.AppPermission.CAMERA,
        Tool.CAMERA,
      ).lastUsedTimestampMs,
    )

    // 4. Backup & Restore preserves permission grant states and usage stats
    repo.revokePermission(com.example.toolbox.ui.main.AppPermission.BLUETOOTH)
    val backupJson = com.example.toolbox.ui.main.exportSettingsToJson(repo)
    val restoredRepo = InMemoryNavigationPreferencesRepository()
    assertTrue(com.example.toolbox.ui.main.importSettingsFromJson(backupJson, restoredRepo))
    assertFalse(restoredRepo.isPermissionGranted(com.example.toolbox.ui.main.AppPermission.BLUETOOTH))
    assertTrue(restoredRepo.isPermissionGranted(com.example.toolbox.ui.main.AppPermission.CAMERA))
    assertEquals(
      3,
      restoredRepo.getPermissionUsageForTool(
        com.example.toolbox.ui.main.AppPermission.CAMERA,
        Tool.CAMERA,
      ).useCount,
    )
    assertEquals(
      t2 + 20_000L,
      restoredRepo.getPermissionUsageForTool(
        com.example.toolbox.ui.main.AppPermission.CAMERA,
        Tool.CAMERA,
      ).lastUsedTimestampMs,
    )

    // 5. Resetting settings restores all permissions to granted and clears usage stats
    com.example.toolbox.ui.main.resetAllSettingsToDefaults(restoredRepo)
    assertTrue(restoredRepo.isPermissionGranted(com.example.toolbox.ui.main.AppPermission.BLUETOOTH))
    assertEquals(0, restoredRepo.getTotalPermissionUsageCount(com.example.toolbox.ui.main.AppPermission.CAMERA))
  }

  @Test
  fun exportAndImportDataJson_roundTripPreservesAllToolData() {
    val notesRepo = InMemoryNotesRepository(
      initialNotes = listOf(
        NoteItem(
          id = "note_1",
          title = "Shopping List",
          isChecklist = true,
          checklistItems = listOf(NoteChecklistItem(id = "c1", text = "Milk", isChecked = true)),
          color = NoteColor.MINT,
          isPinned = true,
          labels = listOf("Groceries"),
        ),
      ),
      initialLabels = listOf("Groceries", "Work"),
      initialGridView = false,
      initialGridColumnsPerRow = 3,
    )

    val todoDate = "2026-11-19"
    val todoRepo = InMemoryTodoListRepository(
      initialTasks = listOf(
        TodoItem(
          id = "task_1",
          title = "Ship data backup feature",
          notes = "In Settings tool",
          subtasks = listOf(TodoSubTask(id = "sub_1", title = "Add tests", isCompleted = true)),
        ),
      ),
      completedDate = todoDate,
      completedCount = 2,
      initialCompletedTasks = listOf(TodoItem(id = "done_1", title = "Completed item")),
      initialSavedTasks = listOf(TodoItem(id = "saved_1", title = "Recurring template")),
    )

    val recipeRepo = InMemoryRecipeBookRepository(
      initialSaved = listOf(RecipeBookEngine.BUILT_IN_RECIPES.first()),
      initialCustomCatalog = listOf(
        RecipeItem(
          id = "custom_recipe_1",
          name = "Grandma's Pie",
          subtitle = "Secret Recipe",
          category = RecipeCategory.BAKING_AND_DESSERTS,
          prepTimeMinutes = 25,
          cookTimeMinutes = 45,
          servings = 8,
          ingredients = listOf("Apples", "Cinnamon"),
          instructions = listOf("Bake at 375F"),
          isCustom = true,
        ),
      ),
    )

    val plantRepo = InMemoryPlantScheduleRepository(
      initialScheduledPlants = listOf(PlantScheduleEngine.BUILT_IN_PLANTS.first()),
      initialCustomPlants = listOf(
        PlantItem(
          id = "custom_plant_1",
          name = "Heirloom Basil",
          scientificName = "Ocimum basilicum",
          category = PlantCategory.HERBS,
          plantingMonths = setOf(4, 5),
          harvestMonths = setOf(6, 7, 8),
          isCustom = true,
        ),
      ),
    )

    val tunerRepo = InMemoryGuitarTunerRepository(
      initialFavorites = setOf(GuitarTuningPreset.DROP_D, GuitarTuningPreset.DADGAD),
    )

    val sourceRepos = com.example.toolbox.ui.main.ToolboxDataRepositories(
      notesRepository = notesRepo,
      todoListRepository = todoRepo,
      recipeBookRepository = recipeRepo,
      plantScheduleRepository = plantRepo,
      guitarTunerRepository = tunerRepo,
      currentTodoDate = todoDate,
    )

    val exportedJson = com.example.toolbox.ui.main.exportDataToJson(sourceRepos)
    assertTrue(exportedJson.contains(com.example.toolbox.ui.main.DATA_BACKUP_FORMAT))

    val targetNotesRepo = InMemoryNotesRepository()
    val targetTodoRepo = InMemoryTodoListRepository(completedDate = todoDate)
    val targetRecipeRepo = InMemoryRecipeBookRepository()
    val targetPlantRepo = InMemoryPlantScheduleRepository()
    val targetTunerRepo = InMemoryGuitarTunerRepository()

    val targetRepos = com.example.toolbox.ui.main.ToolboxDataRepositories(
      notesRepository = targetNotesRepo,
      todoListRepository = targetTodoRepo,
      recipeBookRepository = targetRecipeRepo,
      plantScheduleRepository = targetPlantRepo,
      guitarTunerRepository = targetTunerRepo,
      currentTodoDate = todoDate,
    )

    val imported = com.example.toolbox.ui.main.importDataFromJson(exportedJson, targetRepos)
    assertTrue(imported)

    assertEquals(1, targetNotesRepo.loadNotes().size)
    assertEquals("Shopping List", targetNotesRepo.loadNotes().first().title)
    assertEquals(listOf("Groceries", "Work"), targetNotesRepo.loadLabels())
    assertFalse(targetNotesRepo.isGridView())
    assertEquals(3, targetNotesRepo.getGridColumnsPerRow())

    assertEquals(1, targetTodoRepo.loadTasks().size)
    assertEquals("Ship data backup feature", targetTodoRepo.loadTasks().first().title)
    assertEquals(2, targetTodoRepo.getCompletedTodayCount(todoDate))
    assertEquals(1, targetTodoRepo.loadCompletedTasks(todoDate).size)
    assertEquals(1, targetTodoRepo.loadSavedTasks().size)

    assertEquals(1, targetRecipeRepo.getSavedRecipes().size)
    assertEquals(1, targetRecipeRepo.getCustomCatalogRecipes().size)
    assertEquals("Grandma's Pie", targetRecipeRepo.getCustomCatalogRecipes().first().name)

    assertEquals(1, targetPlantRepo.loadScheduledPlants().size)
    assertEquals(1, targetPlantRepo.loadCustomPlants().size)
    assertEquals("Heirloom Basil", targetPlantRepo.loadCustomPlants().first().name)

    assertEquals(
      setOf(GuitarTuningPreset.DROP_D.chordTuningId, GuitarTuningPreset.DADGAD.chordTuningId),
      targetTunerRepo.loadFavoriteTuningIds(),
    )
  }

  @Test
  fun importDataFromJson_rejectsInvalidOrUnrecognizedJson() {
    val targetRepos = com.example.toolbox.ui.main.ToolboxDataRepositories(
      notesRepository = InMemoryNotesRepository(),
    )
    assertFalse(com.example.toolbox.ui.main.importDataFromJson("", targetRepos))
    assertFalse(com.example.toolbox.ui.main.importDataFromJson("not-json", targetRepos))
    assertFalse(com.example.toolbox.ui.main.importDataFromJson("{\"unrelated_key\": 123}", targetRepos))
  }

  @Test
  fun errorLogs_keepsUpToLast100ErrorsAndSupportsClear() {
    val repo = InMemoryNavigationPreferencesRepository()
    assertEquals(100, com.example.toolbox.ui.main.MAX_ERROR_LOG_ENTRIES)
    assertTrue(repo.getErrorLogs().isEmpty())

    // Log 105 errors sequentially
    for (i in 1..105) {
      repo.logError(
        message = "Error #$i",
        source = "Settings",
        details = "Detail for error $i",
        timestampMs = 1_763_400_000_000L + i * 1_000L,
      )
    }

    val logs = repo.getErrorLogs()
    assertEquals(100, logs.size)
    assertEquals(100, repo.errorLogsFlow.value.size)

    // Only the last 100 errors (Error #6 .. Error #105) are retained; oldest 5 are evicted
    assertEquals("Error #6", logs.first().message)
    assertEquals("Error #105", logs.last().message)
    assertFalse(logs.any { it.message == "Error #1" })
    assertFalse(logs.any { it.message == "Error #5" })

    // Logging a throwable also caps at 100 entries
    repo.logError(
      throwable = IllegalStateException("Throwable failure"),
      source = "Files",
      timestampMs = 1_763_400_200_000L,
    )
    val updatedLogs = repo.getErrorLogs()
    assertEquals(100, updatedLogs.size)
    assertEquals("Error #7", updatedLogs.first().message)
    assertEquals("Throwable failure", updatedLogs.last().message)
    assertEquals("Files", updatedLogs.last().source)
    assertTrue(updatedLogs.last().details.contains("IllegalStateException"))

    // Clearing error logs empties the list
    repo.clearErrorLogs()
    assertTrue(repo.getErrorLogs().isEmpty())
    assertTrue(repo.errorLogsFlow.value.isEmpty())
  }

  @Test
  fun errorLogs_filterAndHighlightHelpers_matchAndHighlightQuerySpans() {
    val entries = listOf(
      com.example.toolbox.ui.main.ErrorLogEntry(
        id = "1",
        timestampMs = 1_763_400_000_000L,
        message = "Failed to export settings to file",
        source = "Settings",
        details = "IOException: Permission denied",
      ),
      com.example.toolbox.ui.main.ErrorLogEntry(
        id = "2",
        timestampMs = 1_763_400_060_000L,
        message = "Audio decoder error",
        source = "Music",
        details = "IllegalStateException: codec reset",
      ),
    )

    assertEquals(2, filterErrorLogs(entries, "").size)
    assertEquals(1, filterErrorLogs(entries, "permission").size)
    assertEquals("1", filterErrorLogs(entries, "permission").first().id)
    assertEquals(1, filterErrorLogs(entries, "music").size)
    assertEquals("2", filterErrorLogs(entries, "music").first().id)
    assertTrue(filterErrorLogs(entries, "nonexistent_term").isEmpty())

    val highlighted = highlightErrorLogText(
      text = "Failed to export settings to file",
      query = "settings",
      highlightBackground = Color.Yellow,
      highlightContent = Color.Black,
    )
    assertEquals("Failed to export settings to file", highlighted.text)
    assertEquals(1, highlighted.spanStyles.size)
    assertEquals(17, highlighted.spanStyles.first().start)
    assertEquals(25, highlighted.spanStyles.first().end)
  }

  @Test
  fun feedbackButtons_haveOnePercentChanceForDevilAndAngelIcons() {
    assertEquals(0.01f, FEEDBACK_DEVIL_ANGEL_EASTER_EGG_CHANCE)

    // Under 1% threshold (< 0.01f) triggers devil and angel icons
    val luckyRandom = object : kotlin.random.Random() {
      override fun nextBits(bitCount: Int): Int = 0
      override fun nextFloat(): Float = 0.005f
    }
    assertTrue(shouldUseDevilAndAngelFeedbackIcons(luckyRandom))

    // At or above 1% threshold (>= 0.01f) keeps default icons
    val boundaryRandom = object : kotlin.random.Random() {
      override fun nextBits(bitCount: Int): Int = 0
      override fun nextFloat(): Float = 0.01f
    }
    assertFalse(shouldUseDevilAndAngelFeedbackIcons(boundaryRandom))

    val normalRandom = object : kotlin.random.Random() {
      override fun nextBits(bitCount: Int): Int = 0
      override fun nextFloat(): Float = 0.50f
    }
    assertFalse(shouldUseDevilAndAngelFeedbackIcons(normalRandom))

    // Icon resolution maps Report a Bug -> Devil and Request a Feature -> Angel when active
    assertEquals(DevilFeedbackIcon, resolveReportBugButtonIcon(useDevilAndAngelIcons = true))
    assertEquals(AngelFeedbackIcon, resolveRequestFeatureButtonIcon(useDevilAndAngelIcons = true))
    assertEquals(
      Icons.Default.BugReport,
      resolveReportBugButtonIcon(useDevilAndAngelIcons = false),
    )
    assertEquals(
      Icons.Default.Lightbulb,
      resolveRequestFeatureButtonIcon(useDevilAndAngelIcons = false),
    )
  }

  @Test
  fun licenseText_containsFullGplV3LicenseAndSupportsSearchHighlighting() {
    assertEquals("GNU General Public License v3.0", TOOLBOX_LICENSE_TITLE)
    assertTrue(TOOLBOX_LICENSE_TEXT.contains("GNU GENERAL PUBLIC LICENSE"))
    assertTrue(TOOLBOX_LICENSE_TEXT.contains("Version 3, 29 June 2007"))
    assertTrue(TOOLBOX_LICENSE_TEXT.contains("TERMS AND CONDITIONS"))
    assertTrue(TOOLBOX_LICENSE_TEXT.contains("END OF TERMS AND CONDITIONS"))

    val highlighted = highlightErrorLogText(
      text = TOOLBOX_LICENSE_TEXT,
      query = "Preamble",
      highlightBackground = Color.Yellow,
      highlightContent = Color.Black,
    )
    assertEquals(TOOLBOX_LICENSE_TEXT, highlighted.text)
    assertTrue(highlighted.spanStyles.isNotEmpty())
  }

  @Test
  fun backgroundTexture_defaultsToNone_andSupportsAllPresetsBackupAndReset() {
    assertEquals(BackgroundTexture.NONE, DEFAULT_BACKGROUND_TEXTURE)
    assertFalse(BackgroundTexture.presets.contains(BackgroundTexture.NONE))
    assertTrue(BackgroundTexture.presets.contains(BackgroundTexture.FUZZY))
    assertTrue(BackgroundTexture.presets.contains(BackgroundTexture.FADE))
    assertTrue(BackgroundTexture.presets.contains(BackgroundTexture.CONCENTRIC))
    assertTrue(BackgroundTexture.presets.contains(BackgroundTexture.HALFTONE))
    assertTrue(BackgroundTexture.presets.contains(BackgroundTexture.TOPOGRAPHY))
    assertTrue(BackgroundTexture.presets.contains(BackgroundTexture.AURORA))
    assertTrue(BackgroundTexture.presets.contains(BackgroundTexture.STARFIELD))
    assertTrue(BackgroundTexture.presets.contains(BackgroundTexture.SUNBURST))
    assertTrue(BackgroundTexture.presets.contains(BackgroundTexture.HEXAGONS))
    assertTrue(BackgroundTexture.presets.contains(BackgroundTexture.BLUEPRINT))
    assertTrue(BackgroundTexture.presets.contains(BackgroundTexture.WAVES))
    assertTrue(BackgroundTexture.presets.contains(BackgroundTexture.CONFETTI))
    assertTrue(BackgroundTexture.presets.contains(BackgroundTexture.CIRCUIT))

    // Case-insensitive parsing by enum name or display name, falling back to NONE
    for (texture in BackgroundTexture.entries) {
      assertEquals(texture, BackgroundTexture.fromPrefValue(texture.name))
      assertEquals(texture, BackgroundTexture.fromPrefValue(texture.name.lowercase()))
      assertEquals(texture, BackgroundTexture.fromPrefValue(texture.displayName))
      assertTrue(texture.displayName.isNotBlank())
      assertTrue(texture.subtitle.isNotBlank())
    }
    assertEquals(BackgroundTexture.NONE, BackgroundTexture.fromPrefValue(null))
    assertEquals(BackgroundTexture.NONE, BackgroundTexture.fromPrefValue(""))
    assertEquals(BackgroundTexture.NONE, BackgroundTexture.fromPrefValue("UNKNOWN_TEXTURE"))

    // Repository updates flow and bridge state
    val repo = InMemoryNavigationPreferencesRepository()
    assertEquals(BackgroundTexture.NONE, repo.getBackgroundTexture())
    assertEquals(BackgroundTexture.NONE, repo.backgroundTextureFlow.value)
    assertEquals(BackgroundTexture.NONE, NavigationPreferencesBridge.backgroundTexture)

    repo.setBackgroundTexture(BackgroundTexture.CONCENTRIC)
    assertEquals(BackgroundTexture.CONCENTRIC, repo.getBackgroundTexture())
    assertEquals(BackgroundTexture.CONCENTRIC, repo.backgroundTextureFlow.value)
    assertEquals(BackgroundTexture.CONCENTRIC, NavigationPreferencesBridge.backgroundTexture)

    // Export & Import settings preserves background texture
    val exportedJson = com.example.toolbox.ui.main.exportSettingsToJson(repo)
    val restoredRepo = InMemoryNavigationPreferencesRepository()
    assertTrue(com.example.toolbox.ui.main.importSettingsFromJson(exportedJson, restoredRepo))
    assertEquals(BackgroundTexture.CONCENTRIC, restoredRepo.getBackgroundTexture())

    // Reset restores NONE
    com.example.toolbox.ui.main.resetAllSettingsToDefaults(restoredRepo)
    assertEquals(BackgroundTexture.NONE, restoredRepo.getBackgroundTexture())
    assertEquals(BackgroundTexture.NONE, NavigationPreferencesBridge.backgroundTexture)
  }

  @Test
  fun hexagonIconButtons_defaultsToFalse_hasStrictlyVerticalLeftAndRightSides_andSupportsBackupAndReset() {
    assertFalse(DEFAULT_HEXAGON_ICON_BUTTONS)

    // Verify hexagon geometry for a 40x40 circular action icon button
    val vertices = createVerticalSidedHexagonVertices(Size(40f, 40f))
    assertEquals(6, vertices.size)
    // Top and bottom points are horizontally centered
    assertEquals(20f, vertices[0].x, 0.001f)
    assertEquals(0f, vertices[0].y, 0.001f)
    assertEquals(20f, vertices[3].x, 0.001f)
    assertEquals(40f, vertices[3].y, 0.001f)
    // Right side (vertices[1] -> vertices[2]) is strictly vertical (same X coordinate)
    assertEquals(vertices[1].x, vertices[2].x, 0.0001f)
    assertTrue(vertices[2].y > vertices[1].y)
    // Left side (vertices[4] -> vertices[5]) is strictly vertical (same X coordinate)
    assertEquals(vertices[4].x, vertices[5].x, 0.0001f)
    assertTrue(vertices[4].y > vertices[5].y)
    // All 6 sides have equal length r = 20f
    for (i in 0 until 6) {
      val a = vertices[i]
      val b = vertices[(i + 1) % 6]
      val edgeLen = kotlin.math.hypot(b.x - a.x, b.y - a.y)
      assertEquals(20f, edgeLen, 0.01f)
    }

    // Verify horizontal hexagon geometry for a 120x32 horizontal navigation pill
    val pillVertices = createHorizontalHexagonVertices(Size(120f, 32f))
    assertEquals(6, pillVertices.size)
    // Left and right points are vertically centered at y = 16f
    assertEquals(0f, pillVertices[0].x, 0.001f)
    assertEquals(16f, pillVertices[0].y, 0.001f)
    assertEquals(120f, pillVertices[3].x, 0.001f)
    assertEquals(16f, pillVertices[3].y, 0.001f)
    // Top side (pillVertices[1] -> pillVertices[2]) is strictly horizontal (y = 0f)
    assertEquals(0f, pillVertices[1].y, 0.0001f)
    assertEquals(0f, pillVertices[2].y, 0.0001f)
    assertTrue(pillVertices[2].x > pillVertices[1].x)
    // Bottom side (pillVertices[4] -> pillVertices[5]) is strictly horizontal (y = 32f)
    assertEquals(32f, pillVertices[4].y, 0.0001f)
    assertEquals(32f, pillVertices[5].y, 0.0001f)
    assertTrue(pillVertices[4].x > pillVertices[5].x)

    // Verify horizontal hexagon geometry for the 46x32 toolbox navigation panel tool icon highlight
    val navIconVertices = createHorizontalHexagonVertices(Size(46f, 32f))
    assertEquals(0f, navIconVertices[1].y, 0.0001f)
    assertEquals(0f, navIconVertices[2].y, 0.0001f)
    assertEquals(32f, navIconVertices[4].y, 0.0001f)
    assertEquals(32f, navIconVertices[5].y, 0.0001f)
    // Flat horizontal top/bottom span is > 24f so the 24dp tool icon fits with generous room inside
    assertTrue((navIconVertices[2].x - navIconVertices[1].x) > 24f)

    val repo = InMemoryNavigationPreferencesRepository()
    try {
      assertFalse(repo.getHexagonIconButtons())
      assertFalse(repo.hexagonIconButtonsFlow.value)
      assertFalse(NavigationPreferencesBridge.hexagonIconButtons)

      // When disabled, circular 40x40 button and wide 160x40 action button use standard rounded outlines
      assertFalse(shouldUseHexagonIconOutline(Size(40f, 40f)))
      assertFalse(shouldUseHorizontalHexagonActionButtonOutline(Size(160f, 40f)))
      assertFalse(shouldUseHorizontalHexagonActionButtonOutline(Size(96f, 36f)))

      // And Home Screen AppWidgets (Music Player, To-Do List, and Notes) use circular icon backgrounds & check circles
      val musicStateBefore = MusicPlayerWidgets.resolveWidgetState(
        context = null,
        session = null,
        externalMedia = null,
        decodeArtwork = false,
      )
      assertFalse(musicStateBefore.hexagonIconButtons)
      assertEquals(com.example.toolbox.R.drawable.bg_music_widget_circle, musicStateBefore.iconButtonBackgroundRes)

      val todoStateBefore = TodoListWidgets.resolveWidgetState(context = null)
      assertFalse(todoStateBefore.hexagonIconButtons)
      assertEquals(com.example.toolbox.R.drawable.bg_music_widget_circle, todoStateBefore.palette.iconButtonBackgroundRes)
      assertEquals(
        com.example.toolbox.R.drawable.ic_todo_widget_circle_unchecked,
        todoStateBefore.palette.taskCheckUncheckedIconRes,
      )
      assertEquals(
        com.example.toolbox.R.drawable.ic_todo_widget_circle_checked,
        todoStateBefore.palette.taskCheckCheckedIconRes,
      )

      val notesStateBefore = NotesWidgets.resolveWidgetState(context = null)
      assertFalse(notesStateBefore.hexagonIconButtons)
      assertEquals(com.example.toolbox.R.drawable.bg_music_widget_circle, notesStateBefore.iconButtonBackgroundRes)

      // Enable Hexagonify Icon Buttons
      repo.setHexagonIconButtons(true)
      assertTrue(repo.getHexagonIconButtons())
      assertTrue(repo.hexagonIconButtonsFlow.value)
      assertTrue(NavigationPreferencesBridge.hexagonIconButtons)

      // Circular 40x40 icon button uses vertical-sided Hexagon outline, while wide action buttons & modal buttons use horizontal Hexagon outline
      assertTrue(shouldUseHexagonIconOutline(Size(40f, 40f)))
      assertFalse(shouldUseHexagonIconOutline(Size(160f, 40f)))
      assertTrue(shouldUseHorizontalHexagonActionButtonOutline(Size(160f, 40f)))
      assertTrue(shouldUseHorizontalHexagonActionButtonOutline(Size(96f, 36f)))
      // Ultra-thin drag handles (e.g. 36x4) remain rounded
      assertFalse(shouldUseHorizontalHexagonActionButtonOutline(Size(36f, 4f)))

      // And all Home Screen AppWidgets (Music Player, To-Do List, and Notes) switch to vertical-sided hexagon backgrounds & check icons
      val musicStateAfter = MusicPlayerWidgets.resolveWidgetState(
        context = null,
        session = null,
        externalMedia = null,
        decodeArtwork = false,
      )
      assertTrue(musicStateAfter.hexagonIconButtons)
      assertEquals(com.example.toolbox.R.drawable.bg_music_widget_hexagon, musicStateAfter.iconButtonBackgroundRes)

      val todoStateAfter = TodoListWidgets.resolveWidgetState(context = null)
      assertTrue(todoStateAfter.hexagonIconButtons)
      assertEquals(com.example.toolbox.R.drawable.bg_music_widget_hexagon, todoStateAfter.palette.iconButtonBackgroundRes)
      assertEquals(
        com.example.toolbox.R.drawable.ic_todo_widget_hexagon_unchecked,
        todoStateAfter.palette.taskCheckUncheckedIconRes,
      )
      assertEquals(
        com.example.toolbox.R.drawable.ic_todo_widget_hexagon_checked,
        todoStateAfter.palette.taskCheckCheckedIconRes,
      )

      val notesStateAfter = NotesWidgets.resolveWidgetState(context = null)
      assertTrue(notesStateAfter.hexagonIconButtons)
      assertEquals(com.example.toolbox.R.drawable.bg_music_widget_hexagon, notesStateAfter.iconButtonBackgroundRes)

      // Export & Import settings preserves hexagonIconButtons
      val exportedJson = com.example.toolbox.ui.main.exportSettingsToJson(repo)
      val restoredRepo = InMemoryNavigationPreferencesRepository()
      assertTrue(com.example.toolbox.ui.main.importSettingsFromJson(exportedJson, restoredRepo))
      assertTrue(restoredRepo.getHexagonIconButtons())

      // Reset restores false
      com.example.toolbox.ui.main.resetAllSettingsToDefaults(restoredRepo)
      assertFalse(restoredRepo.getHexagonIconButtons())
      assertFalse(NavigationPreferencesBridge.hexagonIconButtons)
    } finally {
      NavigationPreferencesBridge.hexagonIconButtons = false
    }
  }

  @Test
  fun backgroundTextureWidget_includesMysteryShapeFirst_listsNoneAndAllOptions_scalesColumnsHorizontally_andSelectsTexture() {
    BackgroundTextureWidgets.clearInMemoryStateForTesting()
    val repo = InMemoryNavigationPreferencesRepository()
    try {
      // 1. Default 2x2 state (widthDp = 180f in 130f..209f -> 2 columns, 8 rows) has the unlabeled shape first, followed by None and all 13 presets (15 items total)
      val defaultState = BackgroundTextureWidgets.resolveWidgetState(context = null, repository = repo)
      assertEquals(BackgroundTexture.NONE, defaultState.activeTexture)
      assertFalse(defaultState.isHexagon)
      assertEquals(BackgroundTexture.entries, defaultState.textures)
      assertEquals(BackgroundTexture.NONE, defaultState.textures.first())
      assertEquals(14, defaultState.textures.size)
      assertEquals(15, defaultState.items.size)
      assertEquals(StyleWidgetItem.HexagonifyShape, defaultState.items.first())
      assertEquals(
        StyleWidgetItem.TextureOption(BackgroundTexture.NONE),
        defaultState.items[1],
      )
      assertEquals(2, defaultState.columnCount)
      assertEquals(8, defaultState.itemRows.size)
      assertEquals(
        listOf(
          StyleWidgetItem.HexagonifyShape,
          StyleWidgetItem.TextureOption(BackgroundTexture.NONE),
        ),
        defaultState.itemRows.first(),
      )

      // 2. Horizontal resize thresholds scale from 1 column up to 4 columns per row
      assertEquals(1, BackgroundTextureWidgets.resolveColumnCountForWidth(72f))
      assertEquals(1, BackgroundTextureWidgets.resolveColumnCountForWidth(129f))
      assertEquals(2, BackgroundTextureWidgets.resolveColumnCountForWidth(130f))
      assertEquals(2, BackgroundTextureWidgets.resolveColumnCountForWidth(180f))
      assertEquals(3, BackgroundTextureWidgets.resolveColumnCountForWidth(210f))
      assertEquals(3, BackgroundTextureWidgets.resolveColumnCountForWidth(260f))
      assertEquals(4, BackgroundTextureWidgets.resolveColumnCountForWidth(290f))
      assertEquals(4, BackgroundTextureWidgets.resolveColumnCountForWidth(360f))

      // 3. Row partitioning for 1, 2, 3, and 4 columns preserves order starting with HexagonifyShape then None
      val oneColumnRows = BackgroundTextureWidgets.buildStyleItemRows(columnCount = 1)
      assertEquals(15, oneColumnRows.size)
      assertEquals(listOf(StyleWidgetItem.HexagonifyShape), oneColumnRows.first())
      assertEquals(listOf(StyleWidgetItem.TextureOption(BackgroundTexture.NONE)), oneColumnRows[1])
      assertEquals(listOf(StyleWidgetItem.TextureOption(BackgroundTexture.CIRCUIT)), oneColumnRows.last())

      val twoColumnRows = BackgroundTextureWidgets.buildStyleItemRows(columnCount = 2)
      assertEquals(8, twoColumnRows.size)
      assertEquals(
        listOf(
          StyleWidgetItem.HexagonifyShape,
          StyleWidgetItem.TextureOption(BackgroundTexture.NONE),
        ),
        twoColumnRows.first(),
      )
      assertEquals(
        listOf(StyleWidgetItem.TextureOption(BackgroundTexture.CIRCUIT)),
        twoColumnRows.last(),
      )

      val threeColumnRows = BackgroundTextureWidgets.buildStyleItemRows(columnCount = 3)
      assertEquals(5, threeColumnRows.size)
      assertEquals(
        listOf(
          StyleWidgetItem.HexagonifyShape,
          StyleWidgetItem.TextureOption(BackgroundTexture.NONE),
          StyleWidgetItem.TextureOption(BackgroundTexture.FUZZY),
        ),
        threeColumnRows.first(),
      )
      assertEquals(
        listOf(
          StyleWidgetItem.TextureOption(BackgroundTexture.WAVES),
          StyleWidgetItem.TextureOption(BackgroundTexture.CONFETTI),
          StyleWidgetItem.TextureOption(BackgroundTexture.CIRCUIT),
        ),
        threeColumnRows.last(),
      )

      val fourColumnRows = BackgroundTextureWidgets.buildStyleItemRows(columnCount = 4)
      assertEquals(4, fourColumnRows.size)
      assertEquals(
        listOf(
          StyleWidgetItem.HexagonifyShape,
          StyleWidgetItem.TextureOption(BackgroundTexture.NONE),
          StyleWidgetItem.TextureOption(BackgroundTexture.FUZZY),
          StyleWidgetItem.TextureOption(BackgroundTexture.FADE),
        ),
        fourColumnRows.first(),
      )
      assertEquals(
        listOf(
          StyleWidgetItem.TextureOption(BackgroundTexture.WAVES),
          StyleWidgetItem.TextureOption(BackgroundTexture.CONFETTI),
          StyleWidgetItem.TextureOption(BackgroundTexture.CIRCUIT),
        ),
        fourColumnRows.last(),
      )

      // 4. Saved per-widget horizontal resize dimensions resolve multi-column vertically scrollable widget state
      val widgetId = 42
      BackgroundTextureWidgets.saveWidgetDimensions(
        context = null,
        appWidgetId = widgetId,
        widthDp = 96f,
        heightDp = 220f,
      )
      val oneColState = BackgroundTextureWidgets.resolveWidgetState(
        context = null,
        appWidgetId = widgetId,
      )
      assertEquals(1, oneColState.columnCount)
      assertEquals(15, oneColState.itemRows.size)

      BackgroundTextureWidgets.saveWidgetDimensions(
        context = null,
        appWidgetId = widgetId,
        widthDp = 250f,
        heightDp = 230f,
      )
      val threeColState = BackgroundTextureWidgets.resolveWidgetState(
        context = null,
        appWidgetId = widgetId,
      )
      assertEquals(3, threeColState.columnCount)
      assertEquals(5, threeColState.itemRows.size)

      BackgroundTextureWidgets.saveWidgetDimensions(
        context = null,
        appWidgetId = widgetId,
        widthDp = 340f,
        heightDp = 160f,
      )
      val fourColState = BackgroundTextureWidgets.resolveWidgetState(
        context = null,
        appWidgetId = widgetId,
      )
      assertEquals(4, fourColState.columnCount)
      assertEquals(4, fourColState.itemRows.size)

      // 5. Selecting a texture from the widget updates repository and bridge state
      BackgroundTextureWidgets.selectBackgroundTexture(
        context = null,
        texture = BackgroundTexture.AURORA,
        repository = repo,
        refreshWidgets = false,
      )
      assertEquals(BackgroundTexture.AURORA, repo.getBackgroundTexture())
      assertEquals(BackgroundTexture.AURORA, repo.backgroundTextureFlow.value)
      assertEquals(BackgroundTexture.AURORA, NavigationPreferencesBridge.backgroundTexture)
      assertEquals(
        BackgroundTexture.AURORA,
        BackgroundTextureWidgets.resolveWidgetState(context = null, appWidgetId = widgetId).activeTexture,
      )

      // Selecting None from the widget restores BackgroundTexture.NONE
      BackgroundTextureWidgets.selectBackgroundTexture(
        context = null,
        texture = BackgroundTexture.NONE,
        repository = repo,
        refreshWidgets = false,
      )
      assertEquals(BackgroundTexture.NONE, repo.getBackgroundTexture())
      assertEquals(BackgroundTexture.NONE, NavigationPreferencesBridge.backgroundTexture)

      BackgroundTextureWidgets.deleteWidgetDimensions(context = null, appWidgetId = widgetId)
      assertNull(BackgroundTextureWidgets.loadWidgetDimensions(context = null, appWidgetId = widgetId))
    } finally {
      BackgroundTextureWidgets.clearInMemoryStateForTesting()
      NavigationPreferencesBridge.backgroundTexture = BackgroundTexture.NONE
    }
  }

  @Test
  fun styleWidgetFirstItem_togglesBetweenCircleAndHexagon_andFollowsBackgroundTexture() {
    BackgroundTextureWidgets.clearInMemoryStateForTesting()
    val repo = InMemoryNavigationPreferencesRepository()
    try {
      // 1. Default state is Circle (isHexagon = false) with BackgroundTexture.NONE and HexagonifyShape as the first item
      val initialState = BackgroundTextureWidgets.resolveWidgetState(
        context = null,
        repository = repo,
      )
      assertEquals(StyleWidgetItem.HexagonifyShape, initialState.items.first())
      assertFalse(initialState.isHexagon)
      assertFalse(initialState.hexagonIconButtons)
      assertEquals(BackgroundTexture.NONE, initialState.activeTexture)
      assertEquals(BackgroundTexture.NONE, initialState.palette.backgroundTexture)

      // 2. Verify vertical-sided hexagon vertices geometry for the first-item shape
      val hexVertices = BackgroundTextureWidgets.createHexagonVertices(cx = 120f, cy = 120f, radius = 100f)
      assertEquals(6, hexVertices.size)
      assertEquals(120f, hexVertices[0].x, 0.001f)
      assertEquals(20f, hexVertices[0].y, 0.001f)
      assertEquals(120f, hexVertices[3].x, 0.001f)
      assertEquals(220f, hexVertices[3].y, 0.001f)
      assertEquals(hexVertices[1].x, hexVertices[2].x, 0.0001f)
      assertTrue(hexVertices[2].y > hexVertices[1].y)
      assertEquals(hexVertices[4].x, hexVertices[5].x, 0.0001f)
      assertTrue(hexVertices[4].y > hexVertices[5].y)

      // 3. Tapping the first item toggles Hexagonify ON (Circle -> Hexagon)
      val toggledOn = BackgroundTextureWidgets.toggleHexagonify(
        context = null,
        repository = repo,
        refreshWidgets = false,
      )
      assertTrue(toggledOn)
      assertTrue(repo.getHexagonIconButtons())
      assertTrue(repo.hexagonIconButtonsFlow.value)
      assertTrue(NavigationPreferencesBridge.hexagonIconButtons)

      val hexagonState = BackgroundTextureWidgets.resolveWidgetState(
        context = null,
        repository = repo,
      )
      assertTrue(hexagonState.isHexagon)
      assertTrue(hexagonState.hexagonIconButtons)
      assertTrue(hexagonState.palette.hexagonIconButtons)

      // 4. Changing background texture updates the first item's background texture
      BackgroundTextureWidgets.selectBackgroundTexture(
        context = null,
        texture = BackgroundTexture.HEXAGONS,
        repository = repo,
        refreshWidgets = false,
      )
      val honeycombHexState = BackgroundTextureWidgets.resolveWidgetState(
        context = null,
        repository = repo,
      )
      assertTrue(honeycombHexState.isHexagon)
      assertEquals(BackgroundTexture.HEXAGONS, honeycombHexState.activeTexture)
      assertEquals(BackgroundTexture.HEXAGONS, honeycombHexState.palette.backgroundTexture)

      // 5. Tapping the first item again toggles Hexagonify OFF (Hexagon -> Circle) while keeping texture
      val toggledOff = BackgroundTextureWidgets.toggleHexagonify(
        context = null,
        repository = repo,
        refreshWidgets = false,
      )
      assertFalse(toggledOff)
      assertFalse(repo.getHexagonIconButtons())
      assertFalse(repo.hexagonIconButtonsFlow.value)
      assertFalse(NavigationPreferencesBridge.hexagonIconButtons)

      val circleHoneycombState = BackgroundTextureWidgets.resolveWidgetState(
        context = null,
        repository = repo,
      )
      assertFalse(circleHoneycombState.isHexagon)
      assertEquals(BackgroundTexture.HEXAGONS, circleHoneycombState.activeTexture)
    } finally {
      BackgroundTextureWidgets.clearInMemoryStateForTesting()
      NavigationPreferencesBridge.hexagonIconButtons = false
      NavigationPreferencesBridge.backgroundTexture = BackgroundTexture.NONE
    }
  }

  @Test
  fun donateButton_hasApprovedEasterEggMessages_twoSecondViewTrigger_twoSecondDisplay_andHalfHourCooldown() {
    assertEquals("Donate", DONATE_DEFAULT_BUTTON_TEXT)
    assertEquals(0.03f, DONATE_EASTER_EGG_CHANCE)
    assertEquals(0.01f, DONATE_TOP_POSITION_EASTER_EGG_CHANCE)
    assertEquals(2_000L, DONATE_EASTER_EGG_VIEW_DURATION_MS)
    assertEquals(2_000L, DONATE_EASTER_EGG_DISPLAY_DURATION_MS)
    assertEquals(30L * 60L * 1_000L, DONATE_EASTER_EGG_COOLDOWN_MS)
    assertEquals("404 Not Found", DONATE_404_TITLE)
    assertEquals("¯\\_(ツ)_/¯", DONATE_404_SHRUG_EMOTE)

    // Verify exact approved easter egg message list (including additions and removals)
    val expectedMessages = listOf(
      "pretty please?",
      "help me help you",
      "runnin outta cash",
      "spare a coin?",
      "feed the dev",
      "fuel the code",
      "coffee fund empty",
      "accepting shiny coins",
      "send snacks pls",
      "100% free forever",
      "feed the machine",
      "fuel the machine",
      "am hungry",
      "help",
      "for my cats",
      "think of the cats",
      "please",
      "do good today",
      "please be kind",
      "with love",
      "you're doing great",
      "glad you're here",
      "drink some water",
      "be gentle",
      "you matter",
      "sending good vibes",
      "thanks for being you",
      "take a break",
      "rest easy",
      "one step at a time",
      "keep going",
      "proud of you",
      "stay hydrated",
      "you got this",
      "made for you",
      "i'm doing my best",
      "be you today",
      "be good",
      "help out",
    )
    assertEquals(expectedMessages, DONATE_EASTER_EGG_MESSAGES)
    assertFalse(DONATE_EASTER_EGG_MESSAGES.contains("100% ad-free forever"))
    assertFalse(DONATE_EASTER_EGG_MESSAGES.contains("poke me if you're generous"))
    assertFalse(DONATE_EASTER_EGG_MESSAGES.contains("keep the gears turning"))

    // 1% chance to move Donate button to the top of Settings
    val luckyTopRandom = object : kotlin.random.Random() {
      override fun nextBits(bitCount: Int): Int = 0
      override fun nextFloat(): Float = 0.005f
    }
    val boundaryTopRandom = object : kotlin.random.Random() {
      override fun nextBits(bitCount: Int): Int = 0
      override fun nextFloat(): Float = 0.01f
    }
    val normalTopRandom = object : kotlin.random.Random() {
      override fun nextBits(bitCount: Int): Int = 0
      override fun nextFloat(): Float = 0.42f
    }
    assertTrue(shouldMoveDonateButtonToTop(luckyTopRandom))
    assertFalse(shouldMoveDonateButtonToTop(boundaryTopRandom))
    assertFalse(shouldMoveDonateButtonToTop(normalTopRandom))

    // 1% chance to flip Donate text when not on cooldown, and 1-hour cooldown enforcement
    val repo = InMemoryNavigationPreferencesRepository()
    assertEquals(0L, repo.getDonateEasterEggLastTriggeredMs())

    val t0 = 1_763_500_000_000L
    val unluckyRollRandom = object : kotlin.random.Random() {
      override fun nextBits(bitCount: Int): Int = 0
      override fun nextFloat(): Float = 0.05f
    }
    assertNull(
      tryTriggerDonateEasterEgg(
        preferencesRepository = repo,
        nowTimestampMs = t0,
        random = unluckyRollRandom,
      )
    )
    assertEquals(0L, repo.getDonateEasterEggLastTriggeredMs())

    val luckyMessageRandom = object : kotlin.random.Random() {
      override fun nextBits(bitCount: Int): Int = 0
      override fun nextFloat(): Float = 0.004f
      override fun nextInt(until: Int): Int = (until - 1).coerceAtLeast(0)
    }
    val triggeredMessage = tryTriggerDonateEasterEgg(
      preferencesRepository = repo,
      nowTimestampMs = t0,
      random = luckyMessageRandom,
    )
    assertEquals("help out", triggeredMessage)
    assertEquals(t0, repo.getDonateEasterEggLastTriggeredMs())

    // During the 1-hour cooldown window, even a lucky <1% roll cannot trigger again
    assertTrue(isDonateEasterEggOnCooldown(lastTriggeredTimestampMs = t0, nowTimestampMs = t0))
    assertTrue(
      isDonateEasterEggOnCooldown(
        lastTriggeredTimestampMs = t0,
        nowTimestampMs = t0 + DONATE_EASTER_EGG_COOLDOWN_MS - 1L,
      )
    )
    assertNull(
      tryTriggerDonateEasterEgg(
        preferencesRepository = repo,
        nowTimestampMs = t0 + 60_000L,
        random = luckyMessageRandom,
      )
    )
    assertNull(
      tryTriggerDonateEasterEgg(
        preferencesRepository = repo,
        nowTimestampMs = t0 + DONATE_EASTER_EGG_COOLDOWN_MS - 1L,
        random = luckyMessageRandom,
      )
    )
    assertEquals(t0, repo.getDonateEasterEggLastTriggeredMs())

    // Exactly 1 hour later, cooldown expires and a lucky roll can trigger again
    val afterOneHour = t0 + DONATE_EASTER_EGG_COOLDOWN_MS
    assertFalse(
      isDonateEasterEggOnCooldown(
        lastTriggeredTimestampMs = t0,
        nowTimestampMs = afterOneHour,
      )
    )
    val firstMessageRandom = object : kotlin.random.Random() {
      override fun nextBits(bitCount: Int): Int = 0
      override fun nextFloat(): Float = 0.001f
      override fun nextInt(until: Int): Int = 0
    }
    val secondTriggeredMessage = tryTriggerDonateEasterEgg(
      preferencesRepository = repo,
      nowTimestampMs = afterOneHour,
      random = firstMessageRandom,
    )
    assertEquals("pretty please?", secondTriggeredMessage)
    assertEquals(afterOneHour, repo.getDonateEasterEggLastTriggeredMs())
  }

  @Test
  fun goodTool_containsAllPositiveMessages_refreshesToDifferentMessage_andPersistsState() {
    assertEquals("Good", Tool.GOOD.title)
    assertEquals(ToolCategory.MISCELLANEOUS, Tool.GOOD.category)
    assertFalse("Good tool should not be hidden by default", Tool.GOOD in DEFAULT_HIDDEN_TOOLS)
    assertEquals("tap to refresh", GOOD_TAP_TO_REFRESH_TEXT)

    val expectedPositiveMessages = listOf(
      "do good today",
      "please be kind",
      "with love",
      "you're doing great",
      "glad you're here",
      "drink some water",
      "be gentle",
      "you matter",
      "sending good vibes",
      "thanks for being you",
      "take a break",
      "rest easy",
      "one step at a time",
      "keep going",
      "proud of you",
      "stay hydrated",
      "you got this",
      "made for you",
      "i'm doing my best",
      "be you today",
      "be good",
      "help someone today",
      "share a smile",
      "check on a friend",
      "say something kind",
      "spread some joy",
      "listen more",
      "be patient",
      "clean a bit, it helps",
      "choose kindness",
      "hug someone",
      "shine bright",
      "be a light",
      "love is everywhere",
      "choose love",
      "be kind",
      "forgive",
      "leave the past",
      "don't worry",
      "it'll be okay",
      "take care",
      "be safe",
      "act with love",
      "act with kindness",
      "take action",
      "be you",
      "breathe",
      "let it go",
      "peace within",
      "be peaceful",
      "slow down",
      "find good",
      "enjoy life",
      "be present",
      "do rest sometimes",
      "look how far we've come",
      "act with grace",
      "be brave",
      "be bold",
      "start small",
      "stay curious",
      "be wise",
      "make something",
      "keep learning",
      "always try",
      "spread kindness",
      "spread love",
      "you are not alone",
      "you are loved",
      "always hope",
      "it's okay",
      "be alright",
      "this too shall pass",
      "endure",
      "please forgive",
      "explore, somehow",
      "know that you are loved",
      "love you",
      "explore today",
      "forget the past",
      "it's okay to drift",
      "make someone happy",
      "be happy",
      "together we build",
      "together we do better",
      "unite",
      "break the chains",
      "be free",
      "love",
      "you are human",
      "sometimes, plans change",
      "roll with it",
      "reach out",
      "forget about it",
      "make it happen",
      "stop waiting",
      "you know what you must do",
      "i am as i am",
      "do you",
      "spread positivity",
    )
    assertEquals(expectedPositiveMessages, GOOD_POSITIVE_MESSAGES)
    assertFalse(GOOD_POSITIVE_MESSAGES.contains("100% free forever"))
    assertFalse(GOOD_POSITIVE_MESSAGES.contains("help out"))
    assertTrue(DONATE_EASTER_EGG_MESSAGES.contains("100% free forever"))
    assertTrue(DONATE_EASTER_EGG_MESSAGES.contains("help out"))
    assertEquals("do good today", DEFAULT_GOOD_MESSAGE)

    // Repository remembers state and refreshes to a distinct message on tap
    val repo = InMemoryGoodRepository()
    assertEquals(DEFAULT_GOOD_MESSAGE, repo.getCurrentMessage())

    val zeroRandom = object : kotlin.random.Random() {
      override fun nextBits(bitCount: Int): Int = 0
      override fun nextInt(until: Int): Int = 0
    }
    val next1 = repo.refreshMessage(zeroRandom)
    // Because "do good today" is excluded from candidates when it is the current message,
    // index 0 of the remaining candidates is "please be kind"
    assertEquals("please be kind", next1)
    assertEquals("please be kind", repo.getCurrentMessage())

    val next2 = repo.refreshMessage(zeroRandom)
    // Now "please be kind" is excluded, so index 0 of candidates is "do good today"
    assertEquals("do good today", next2)
    assertEquals("do good today", repo.getCurrentMessage())

    // Explicit state persistence
    repo.setCurrentMessage("proud of you")
    assertEquals("proud of you", repo.getCurrentMessage())
  }
}
