package com.example.toolbox.ui.main

import android.content.SharedPreferences
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class NavigationPreferencesTest {

  @Test
  fun paginationCalculations_rowCountAndExpandedPageCount() {
    // 10 tools at 3 per row -> 4 rows
    assertEquals(4, calculateRowCount(10, 3))
    // 4 rows paginated after 2 rows -> 2 pages
    assertEquals(2, calculateExpandedPageCount(4))

    // 10 tools at 4 per row -> 3 rows -> 2 pages
    assertEquals(3, calculateRowCount(10, 4))
    assertEquals(2, calculateExpandedPageCount(3))

    // 10 tools at 5 or 6 per row -> 2 rows -> 1 page
    assertEquals(2, calculateRowCount(10, 5))
    assertEquals(1, calculateExpandedPageCount(2))
    assertEquals(2, calculateRowCount(10, 6))
    assertEquals(1, calculateExpandedPageCount(2))

    // 18 tools at 3 per row -> 6 rows -> 3 pages
    assertEquals(6, calculateRowCount(18, 3))
    assertEquals(3, calculateExpandedPageCount(6))

    // 19 tools at 3 per row -> 7 rows -> 4 pages
    assertEquals(7, calculateRowCount(19, 3))
    assertEquals(4, calculateExpandedPageCount(7))

    // 20 tools at 3 per row -> 7 rows -> 4 pages
    assertEquals(7, calculateRowCount(20, 3))
    assertEquals(4, calculateExpandedPageCount(7))
  }

  @Test
  fun parseToolOrder_nullOrBlank_returnsAllToolEntriesInDefaultOrder() {
    val expectedDefaultOrder = listOf(
      Tool.CALCULATOR,
      Tool.NOTES,
      Tool.TODO_LIST,
      Tool.CAMERA,
      Tool.GALLERY,
      Tool.FILES,
      Tool.UNIT_CONVERTER,
      Tool.RULER,
      Tool.AUDIO_RECORDER,
      Tool.MUSIC,
      Tool.BLUETOOTH_REMOTE,
      Tool.BLUETOOTH_GAMEPAD,
      Tool.GUITAR_TUNER,
      Tool.CHORD_BOOK,
      Tool.RECIPES,
      Tool.PLANT_SCHEDULE,
      Tool.DICE_ROLLER,
      Tool.IMAGE_EDITOR,
      Tool.VIDEO_EDITOR,
      Tool.AUDIO_EDITOR,
      Tool.FILE_CONVERTER,
      Tool.PDF_VIEWER,
      Tool.ARCHIVE,
      Tool.DECIBEL_METER,
      Tool.METRONOME,
      Tool.COLOR_PICKER,
      Tool.SETTINGS,
    )
    assertEquals(expectedDefaultOrder, Tool.entries)
    assertEquals(Tool.entries, parseToolOrder(null))
    assertEquals(Tool.entries, parseToolOrder(""))
    assertEquals(Tool.entries, parseToolOrder("   "))
  }

  @Test
  fun parseToolOrder_validSavedOrder_preservesOrder() {
    val raw = "IMAGE_EDITOR,CALCULATOR,RULER,METRONOME,COLOR_PICKER,GUITAR_TUNER,UNIT_CONVERTER,DICE_ROLLER,BLUETOOTH_REMOTE,TODO_LIST,FILES,AUDIO_RECORDER,FILE_CONVERTER,GALLERY,NOTES,CHORD_BOOK,PLANT_SCHEDULE,CAMERA,AUDIO_EDITOR,VIDEO_EDITOR,BLUETOOTH_GAMEPAD,DECIBEL_METER,ARCHIVE,MUSIC,PDF_VIEWER,RECIPES,SETTINGS"
    val result = parseToolOrder(raw)
    val expected = listOf(
      Tool.IMAGE_EDITOR,
      Tool.CALCULATOR,
      Tool.RULER,
      Tool.METRONOME,
      Tool.COLOR_PICKER,
      Tool.GUITAR_TUNER,
      Tool.UNIT_CONVERTER,
      Tool.DICE_ROLLER,
      Tool.BLUETOOTH_REMOTE,
      Tool.TODO_LIST,
      Tool.FILES,
      Tool.AUDIO_RECORDER,
      Tool.FILE_CONVERTER,
      Tool.GALLERY,
      Tool.NOTES,
      Tool.CHORD_BOOK,
      Tool.PLANT_SCHEDULE,
      Tool.CAMERA,
      Tool.AUDIO_EDITOR,
      Tool.VIDEO_EDITOR,
      Tool.BLUETOOTH_GAMEPAD,
      Tool.DECIBEL_METER,
      Tool.ARCHIVE,
      Tool.MUSIC,
      Tool.PDF_VIEWER,
      Tool.RECIPES,
      Tool.SETTINGS,
    )
    assertEquals(expected, result)
  }

  @Test
  fun parseToolOrder_missingSomeTools_appendsMissingToolsAtEnd() {
    val raw = "CALCULATOR,RULER"
    val result = parseToolOrder(raw)
    assertEquals(Tool.CALCULATOR, result[0])
    assertEquals(Tool.RULER, result[1])
    assertEquals(Tool.entries.size, result.size)
    assertTrue(result.containsAll(Tool.entries))
  }

  @Test
  fun parseToolOrder_corruptedOrRemovedToolNames_ignoresInvalidEntries() {
    val raw = "NON_EXISTENT_TOOL,CALCULATOR,ANOTHER_BAD_NAME,RULER"
    val result = parseToolOrder(raw)
    assertEquals(Tool.CALCULATOR, result[0])
    assertEquals(Tool.RULER, result[1])
    assertEquals(Tool.entries.size, result.size)
    assertTrue(result.containsAll(Tool.entries))
  }

  @Test
  fun parseToolOrder_duplicateNames_deduplicatesAndAppendsMissing() {
    val raw = "CALCULATOR,RULER,CALCULATOR"
    val result = parseToolOrder(raw)
    assertEquals(Tool.CALCULATOR, result[0])
    assertEquals(Tool.RULER, result[1])
    assertEquals(Tool.entries.size, result.size)
    assertTrue(result.containsAll(Tool.entries))
  }

  @Test
  fun serializeToolOrder_formatsToolsAsCommaSeparatedNames() {
    val tools = listOf(Tool.CALCULATOR, Tool.RULER, Tool.DICE_ROLLER)
    val serialized = serializeToolOrder(tools)
    assertEquals("CALCULATOR,RULER,DICE_ROLLER", serialized)
  }

  @Test
  fun inMemoryRepository_getAndSetToolsPerRow() {
    val repo = InMemoryNavigationPreferencesRepository(initialToolsPerRow = 3)
    assertEquals(3, repo.getToolsPerRow())

    repo.setToolsPerRow(5)
    assertEquals(5, repo.getToolsPerRow())

    // Coercion test:
    repo.setToolsPerRow(1)
    assertEquals(MIN_TOOLS_PER_ROW, repo.getToolsPerRow())

    repo.setToolsPerRow(10)
    assertEquals(MAX_TOOLS_PER_ROW, repo.getToolsPerRow())
  }

  @Test
  fun inMemoryRepository_getAndSetToolOrder() {
    val repo = InMemoryNavigationPreferencesRepository()
    assertEquals(Tool.entries, repo.getToolOrder())

    val customOrder = listOf(
      Tool.RULER,
      Tool.CALCULATOR,
      Tool.DICE_ROLLER,
      Tool.UNIT_CONVERTER,
      Tool.GUITAR_TUNER,
      Tool.METRONOME,
      Tool.COLOR_PICKER,
      Tool.BLUETOOTH_REMOTE,
      Tool.IMAGE_EDITOR,
      Tool.TODO_LIST,
      Tool.FILES,
      Tool.AUDIO_RECORDER,
      Tool.FILE_CONVERTER,
      Tool.GALLERY,
      Tool.NOTES,
      Tool.CHORD_BOOK,
      Tool.PLANT_SCHEDULE,
      Tool.CAMERA,
      Tool.AUDIO_EDITOR,
      Tool.VIDEO_EDITOR,
      Tool.BLUETOOTH_GAMEPAD,
      Tool.DECIBEL_METER,
      Tool.ARCHIVE,
      Tool.MUSIC,
      Tool.PDF_VIEWER,
      Tool.RECIPE_BOOK,
      Tool.SETTINGS,
    )
    repo.setToolOrder(customOrder)
    assertEquals(customOrder, repo.getToolOrder())
  }

  @Test
  fun defaultNavigationPreferencesRepository_withFakeSharedPreferences_persistsSettings() {
    val fakePrefs = FakeSharedPreferences()
    val repo = DefaultNavigationPreferencesRepository(fakePrefs)

    // Initial default values
    assertEquals(DEFAULT_TOOLS_PER_ROW, repo.getToolsPerRow())
    assertEquals(Tool.entries, repo.getToolOrder())

    // Update tools per row
    repo.setToolsPerRow(4)
    assertEquals(4, repo.getToolsPerRow())
    assertEquals(4, fakePrefs.getInt(PREF_KEY_TOOLS_PER_ROW, -1))

    // Update tool order
    val customOrder = listOf(
      Tool.IMAGE_EDITOR,
      Tool.CALCULATOR,
      Tool.RULER,
      Tool.METRONOME,
      Tool.COLOR_PICKER,
      Tool.GUITAR_TUNER,
      Tool.UNIT_CONVERTER,
      Tool.DICE_ROLLER,
      Tool.BLUETOOTH_REMOTE,
      Tool.TODO_LIST,
      Tool.FILES,
      Tool.AUDIO_RECORDER,
      Tool.FILE_CONVERTER,
      Tool.GALLERY,
      Tool.NOTES,
      Tool.CHORD_BOOK,
      Tool.PLANT_SCHEDULE,
      Tool.CAMERA,
      Tool.AUDIO_EDITOR,
      Tool.VIDEO_EDITOR,
      Tool.BLUETOOTH_GAMEPAD,
      Tool.DECIBEL_METER,
      Tool.ARCHIVE,
      Tool.MUSIC,
      Tool.PDF_VIEWER,
      Tool.RECIPE_BOOK,
      Tool.SETTINGS,
    )
    repo.setToolOrder(customOrder)
    assertEquals(customOrder, repo.getToolOrder())
    assertEquals(serializeToolOrder(customOrder), fakePrefs.getString(PREF_KEY_TOOL_ORDER, null))
  }

  @Test
  fun parseToolOrder_settingsAlwaysAtEnd_evenWhenPlacedFirstOrMiddle() {
    val rawFirst = "SETTINGS,CALCULATOR,RULER"
    val resultFirst = parseToolOrder(rawFirst)
    assertEquals(Tool.SETTINGS, resultFirst.last())
    assertEquals(Tool.CALCULATOR, resultFirst[0])
    assertEquals(Tool.RULER, resultFirst[1])

    val rawMiddle = "CALCULATOR,SETTINGS,RULER"
    val resultMiddle = parseToolOrder(rawMiddle)
    assertEquals(Tool.SETTINGS, resultMiddle.last())
    assertEquals(Tool.CALCULATOR, resultMiddle[0])
    assertEquals(Tool.RULER, resultMiddle[1])
  }

  @Test
  fun reorderTools_disallowsMovingSettings() {
    val initial = Tool.entries
    val settingsIndex = initial.indexOf(Tool.SETTINGS)
    assertEquals(initial.lastIndex, settingsIndex)

    // Attempt to move Settings forward to index 0
    val result = reorderTools(initial, fromIndex = settingsIndex, toIndex = 0)
    assertEquals(initial, result)
  }

  @Test
  fun reorderTools_disallowsMovingOtherToolsOntoSettings() {
    val initial = Tool.entries
    val settingsIndex = initial.indexOf(Tool.SETTINGS)

    // Attempt to move Calculator to Settings index
    val result = reorderTools(initial, fromIndex = 0, toIndex = settingsIndex)
    assertEquals(initial, result)
  }

  @Test
  fun mergeReorderedVisible_preservesHiddenToolsAndMaintainsSettingsAtEnd() {
    val allTools = listOf(
      Tool.DICE_ROLLER,
      Tool.UNIT_CONVERTER,
      Tool.GUITAR_TUNER,
      Tool.METRONOME,
      Tool.SETTINGS,
    )
    // Suppose UNIT_CONVERTER is hidden
    val visible = listOf(
      Tool.DICE_ROLLER,
      Tool.GUITAR_TUNER,
      Tool.METRONOME,
      Tool.SETTINGS,
    )
    // Move METRONOME to the top of visible
    val reorderedVisible = listOf(
      Tool.METRONOME,
      Tool.DICE_ROLLER,
      Tool.GUITAR_TUNER,
      Tool.SETTINGS,
    )

    val merged = mergeReorderedVisible(allTools, reorderedVisible)
    val expected = listOf(
      Tool.METRONOME,
      Tool.UNIT_CONVERTER, // preserves its slot relative to unhidden tools
      Tool.DICE_ROLLER,
      Tool.GUITAR_TUNER,
      Tool.SETTINGS, // stays at end
    )
    assertEquals(expected, merged)
    assertEquals(Tool.SETTINGS, merged.last())
  }

  @Test
  fun hiddenTools_cannotContainSettings() {
    val raw = "CALCULATOR,SETTINGS,RULER"
    val parsed = parseHiddenTools(raw)
    assertEquals(setOf(Tool.CALCULATOR, Tool.RULER), parsed)
    assertTrue(!parsed.contains(Tool.SETTINGS))

    val serialized = serializeHiddenTools(setOf(Tool.CALCULATOR, Tool.SETTINGS, Tool.RULER))
    assertEquals("CALCULATOR,RULER", serialized)

    val fakePrefs = FakeSharedPreferences()
    val repo = DefaultNavigationPreferencesRepository(fakePrefs)
    repo.setHiddenTools(setOf(Tool.CALCULATOR, Tool.SETTINGS, Tool.DICE_ROLLER))
    assertEquals(setOf(Tool.CALCULATOR, Tool.DICE_ROLLER), repo.getHiddenTools())
  }

  @Test
  fun hiddenTools_fileConverterImageEditorVideoEditorAudioEditorAndDecibelMeterAreHiddenByDefault_andCanBeUnhidden() {
    val expectedDefaultHidden = setOf(
      Tool.FILE_CONVERTER,
      Tool.IMAGE_EDITOR,
      Tool.VIDEO_EDITOR,
      Tool.AUDIO_EDITOR,
      Tool.DECIBEL_METER,
      Tool.ARCHIVE,
      Tool.PDF_VIEWER,
      Tool.METRONOME,
      Tool.COLOR_PICKER,
    )
    // Null rawString (no saved preference) returns DEFAULT_HIDDEN_TOOLS containing FILE_CONVERTER, IMAGE_EDITOR, VIDEO_EDITOR, AUDIO_EDITOR, DECIBEL_METER
    assertEquals(expectedDefaultHidden, DEFAULT_HIDDEN_TOOLS)
    assertEquals(DEFAULT_HIDDEN_TOOLS, parseHiddenTools(null))
    // Explicit blank string (user unhid all tools) returns emptySet()
    assertTrue(parseHiddenTools("").isEmpty())

    val fakePrefs = FakeSharedPreferences()
    val repo = DefaultNavigationPreferencesRepository(fakePrefs)
    assertEquals(expectedDefaultHidden, repo.getHiddenTools())
    assertEquals(expectedDefaultHidden, repo.hiddenToolsFlow.value)

    // User unhides all tools
    repo.setHiddenTools(emptySet())
    assertTrue(repo.getHiddenTools().isEmpty())
    assertTrue(repo.hiddenToolsFlow.value.isEmpty())

    // Persisted empty set survives repository reload
    val reloadedRepo = DefaultNavigationPreferencesRepository(fakePrefs)
    assertTrue(reloadedRepo.getHiddenTools().isEmpty())

    // Existing installation with legacy PREF_KEY_HIDDEN_TOOLS automatically hides newly added default-hidden tools (e.g. ARCHIVE)
    val legacyPrefs = FakeSharedPreferences()
    legacyPrefs.edit()
      .putString(PREF_KEY_HIDDEN_TOOLS, "FILE_CONVERTER,IMAGE_EDITOR,VIDEO_EDITOR,AUDIO_EDITOR,DECIBEL_METER")
      .apply()
    val upgradedRepo = DefaultNavigationPreferencesRepository(legacyPrefs)
    assertTrue(upgradedRepo.getHiddenTools().contains(Tool.ARCHIVE))
    assertTrue(upgradedRepo.getHiddenTools().contains(Tool.PDF_VIEWER))
    assertTrue(upgradedRepo.hiddenToolsFlow.value.contains(Tool.ARCHIVE))
    assertTrue(upgradedRepo.hiddenToolsFlow.value.contains(Tool.PDF_VIEWER))

    // User can subsequently unhide ARCHIVE and the choice persists across reloads
    upgradedRepo.setHiddenTools(upgradedRepo.getHiddenTools() - Tool.ARCHIVE)
    val reloadedAfterUnhide = DefaultNavigationPreferencesRepository(legacyPrefs)
    assertTrue(!reloadedAfterUnhide.getHiddenTools().contains(Tool.ARCHIVE))
  }

  @Test
  fun themeMode_persistsCorrectly() {
    val fakePrefs = FakeSharedPreferences()
    val repo = DefaultNavigationPreferencesRepository(fakePrefs)

    // Default is SYSTEM
    assertEquals(ThemeMode.SYSTEM, repo.getThemeMode())

    repo.setThemeMode(ThemeMode.DARK)
    assertEquals(ThemeMode.DARK, repo.getThemeMode())
    assertEquals("DARK", fakePrefs.getString(PREF_KEY_THEME_MODE, null))

    repo.setThemeMode(ThemeMode.LIGHT)
    assertEquals(ThemeMode.LIGHT, repo.getThemeMode())
    assertEquals("LIGHT", fakePrefs.getString(PREF_KEY_THEME_MODE, null))
  }

  @Test
  fun themeColor_persistsCorrectly() {
    val fakePrefs = FakeSharedPreferences()
    val repo = DefaultNavigationPreferencesRepository(fakePrefs)

    // Default is SYSTEM
    assertEquals(DEFAULT_THEME_COLOR_SYSTEM, repo.getThemeColor())

    // Change to fallback purple #8E66A6
    repo.setThemeColor(FALLBACK_THEME_COLOR_HEX)
    assertEquals(FALLBACK_THEME_COLOR_HEX, repo.getThemeColor())
    assertEquals(FALLBACK_THEME_COLOR_HEX, fakePrefs.getString(PREF_KEY_THEME_COLOR, null))

    // Change to custom color
    repo.setThemeColor("#123456")
    assertEquals("#123456", repo.getThemeColor())
    assertEquals("#123456", fakePrefs.getString(PREF_KEY_THEME_COLOR, null))
  }

  @Test
  fun accentColor_persistsCorrectly() {
    val fakePrefs = FakeSharedPreferences()
    val repo = DefaultNavigationPreferencesRepository(fakePrefs)

    // Default is AUTO
    assertEquals(DEFAULT_ACCENT_COLOR_AUTO, repo.getAccentColor())
    assertEquals(DEFAULT_ACCENT_COLOR_AUTO, repo.accentColorFlow.value)

    // Set to preset hex
    repo.setAccentColor("#E65100")
    assertEquals("#E65100", repo.getAccentColor())
    assertEquals("#E65100", repo.accentColorFlow.value)
    assertEquals("#E65100", fakePrefs.getString(PREF_KEY_ACCENT_COLOR, null))

    // Set back to AUTO
    repo.setAccentColor(DEFAULT_ACCENT_COLOR_AUTO)
    assertEquals(DEFAULT_ACCENT_COLOR_AUTO, repo.getAccentColor())
    assertEquals(DEFAULT_ACCENT_COLOR_AUTO, repo.accentColorFlow.value)
  }

  @Test
  fun toolSorting_persistsCorrectly() {
    val fakePrefs = FakeSharedPreferences()
    val repo = DefaultNavigationPreferencesRepository(fakePrefs)

    // Default is USER_CHOICE
    assertEquals(ToolSortingOption.USER_CHOICE, repo.getToolSorting())
    assertEquals(ToolSortingOption.USER_CHOICE, repo.toolSortingFlow.value)

    // Set to TOOL_CATEGORIES
    repo.setToolSorting(ToolSortingOption.TOOL_CATEGORIES)
    assertEquals(ToolSortingOption.TOOL_CATEGORIES, repo.getToolSorting())
    assertEquals(ToolSortingOption.TOOL_CATEGORIES, repo.toolSortingFlow.value)
    assertEquals("TOOL_CATEGORIES", fakePrefs.getString(PREF_KEY_TOOL_SORTING, null))

    // Set back to USER_CHOICE
    repo.setToolSorting(ToolSortingOption.USER_CHOICE)
    assertEquals(ToolSortingOption.USER_CHOICE, repo.getToolSorting())
    assertEquals(ToolSortingOption.USER_CHOICE, repo.toolSortingFlow.value)
    assertEquals("USER_CHOICE", fakePrefs.getString(PREF_KEY_TOOL_SORTING, null))
  }

  @Test
  fun toolSortingOption_displayNamesAreExpected() {
    assertEquals("Manual", ToolSortingOption.USER_CHOICE.displayName)
    assertEquals("Tool categories", ToolSortingOption.TOOL_CATEGORIES.displayName)
    assertEquals("Usage", ToolSortingOption.USAGE.displayName)
    assertEquals("Alphabetical", ToolSortingOption.ALPHABETICAL.displayName)
  }

  @Test
  fun toolSortOrder_displayNamesAreExpected() {
    assertEquals("Ascending", ToolSortOrder.ASCENDING.displayName)
    assertEquals("Descending", ToolSortOrder.DESCENDING.displayName)
  }

  @Test
  fun categorySortingOption_displayNamesAreExpected() {
    assertEquals("Manual", CategorySortingOption.MANUAL.displayName)
    assertEquals("Alpha", CategorySortingOption.ALPHABETICAL.displayName)
    assertEquals("Usage", CategorySortingOption.USAGE.displayName)
  }

  @Test
  fun categoryToolSortingOption_displayNamesAreExpected() {
    assertEquals("Manual", CategoryToolSortingOption.MANUAL.displayName)
    assertEquals("Alpha", CategoryToolSortingOption.ALPHABETICAL.displayName)
    assertEquals("Usage", CategoryToolSortingOption.USAGE.displayName)
  }

  @Test
  fun categoryToolSorting_persistenceAndFallback() {
    val fakePrefs = FakeSharedPreferences()
    val repo = DefaultNavigationPreferencesRepository(fakePrefs)

    // Default is MANUAL
    assertEquals(CategoryToolSortingOption.MANUAL, repo.getCategoryToolSorting())
    assertEquals(CategoryToolSortingOption.MANUAL, repo.categoryToolSortingFlow.value)

    // Update to ALPHABETICAL
    repo.setCategoryToolSorting(CategoryToolSortingOption.ALPHABETICAL)
    assertEquals(CategoryToolSortingOption.ALPHABETICAL, repo.getCategoryToolSorting())
    assertEquals(CategoryToolSortingOption.ALPHABETICAL, repo.categoryToolSortingFlow.value)
    assertEquals("ALPHABETICAL", fakePrefs.getString(PREF_KEY_CATEGORY_TOOL_SORTING, null))

    // Update to USAGE
    repo.setCategoryToolSorting(CategoryToolSortingOption.USAGE)
    assertEquals(CategoryToolSortingOption.USAGE, repo.getCategoryToolSorting())
    assertEquals(CategoryToolSortingOption.USAGE, repo.categoryToolSortingFlow.value)
    assertEquals("USAGE", fakePrefs.getString(PREF_KEY_CATEGORY_TOOL_SORTING, null))

    // Fallback on invalid value
    fakePrefs.edit().putString(PREF_KEY_CATEGORY_TOOL_SORTING, "CORRUPT").apply()
    val repo2 = DefaultNavigationPreferencesRepository(fakePrefs)
    assertEquals(CategoryToolSortingOption.MANUAL, repo2.getCategoryToolSorting())
  }

  @Test
  fun sortToolsWithinCategory_manualAlphaAndUsage_keepsSettingsLast() {
    val mathTools = listOf(
      Tool.RULER, // "Ruler"
      Tool.CALCULATOR, // "Calculator"
      Tool.UNIT_CONVERTER, // "Units"
    )

    // Manual preserves order
    val manual = sortToolsWithinCategory(mathTools, CategoryToolSortingOption.MANUAL)
    assertEquals(listOf(Tool.RULER, Tool.CALCULATOR, Tool.UNIT_CONVERTER), manual)

    // Alpha sorts by title: Calculator < Ruler < Units
    val alpha = sortToolsWithinCategory(mathTools, CategoryToolSortingOption.ALPHABETICAL)
    assertEquals(listOf(Tool.CALCULATOR, Tool.RULER, Tool.UNIT_CONVERTER), alpha)

    // Usage sorts by usage count
    val usageCounts = mapOf(
      Tool.UNIT_CONVERTER to 30,
      Tool.RULER to 10,
      Tool.CALCULATOR to 5,
    )
    val usage = sortToolsWithinCategory(mathTools, CategoryToolSortingOption.USAGE, usageCounts)
    assertEquals(listOf(Tool.UNIT_CONVERTER, Tool.RULER, Tool.CALCULATOR), usage)
  }

  @Test
  fun categorySorting_persistenceAndFallback() {
    val fakePrefs = FakeSharedPreferences()
    val repo = DefaultNavigationPreferencesRepository(fakePrefs)

    // Default is MANUAL
    assertEquals(CategorySortingOption.MANUAL, repo.getCategorySorting())
    assertEquals(CategorySortingOption.MANUAL, repo.categorySortingFlow.value)

    // Update to ALPHABETICAL
    repo.setCategorySorting(CategorySortingOption.ALPHABETICAL)
    assertEquals(CategorySortingOption.ALPHABETICAL, repo.getCategorySorting())
    assertEquals(CategorySortingOption.ALPHABETICAL, repo.categorySortingFlow.value)
    assertEquals("ALPHABETICAL", fakePrefs.getString(PREF_KEY_CATEGORY_SORTING, null))

    // Update to USAGE
    repo.setCategorySorting(CategorySortingOption.USAGE)
    assertEquals(CategorySortingOption.USAGE, repo.getCategorySorting())
    assertEquals(CategorySortingOption.USAGE, repo.categorySortingFlow.value)
    assertEquals("USAGE", fakePrefs.getString(PREF_KEY_CATEGORY_SORTING, null))

    // Fallback on invalid value
    fakePrefs.edit().putString(PREF_KEY_CATEGORY_SORTING, "CORRUPT").apply()
    val repo2 = DefaultNavigationPreferencesRepository(fakePrefs)
    assertEquals(CategorySortingOption.MANUAL, repo2.getCategorySorting())
  }

  @Test
  fun sortCategoryGroups_manualAlphaAndUsage_keepsSystemLast() {
    val groups = listOf(
      CategoryGroup(ToolCategory.MISCELLANEOUS, listOf(Tool.PLANT_SCHEDULE, Tool.DICE_ROLLER)),
      CategoryGroup(ToolCategory.SYSTEM, listOf(Tool.SETTINGS)),
      CategoryGroup(ToolCategory.MATH_AND_MEASUREMENT, listOf(Tool.CALCULATOR, Tool.RULER)),
      CategoryGroup(ToolCategory.MUSIC_AND_INSTRUMENTS, listOf(Tool.GUITAR_TUNER)),
      CategoryGroup(ToolCategory.PRODUCTIVITY, listOf(Tool.TODO_LIST)),
    )

    // 1. MANUAL preserves ToolCategory.entries order, SYSTEM last
    val manualSorted = sortCategoryGroups(groups, CategorySortingOption.MANUAL)
    assertEquals(
      listOf(
        ToolCategory.MATH_AND_MEASUREMENT,
        ToolCategory.PRODUCTIVITY,
        ToolCategory.MUSIC_AND_INSTRUMENTS,
        ToolCategory.MISCELLANEOUS,
        ToolCategory.SYSTEM,
      ),
      manualSorted.map { it.category },
    )

    // 2. ALPHABETICAL sorts by category title (A-Z), SYSTEM last
    val alphaSorted = sortCategoryGroups(groups, CategorySortingOption.ALPHABETICAL)
    assertEquals(
      listOf(
        ToolCategory.MATH_AND_MEASUREMENT, // "Math & Measurement"
        ToolCategory.MISCELLANEOUS, // "Miscellaneous"
        ToolCategory.MUSIC_AND_INSTRUMENTS, // "Music & Instruments"
        ToolCategory.PRODUCTIVITY, // "Productivity"
        ToolCategory.SYSTEM, // "System" always last
      ),
      alphaSorted.map { it.category },
    )

    // 3. USAGE sorts by total usage of tools in group, SYSTEM last
    val usageCounts = mapOf(
      Tool.TODO_LIST to 50, // Productivity = 50
      Tool.GUITAR_TUNER to 10, // Music & Instruments = 10
      Tool.DICE_ROLLER to 5, // Miscellaneous = 5 + 20 = 25
      Tool.PLANT_SCHEDULE to 20,
      Tool.CALCULATOR to 100, // Math = 100 + 0 = 100
      Tool.SETTINGS to 999, // System should still be last!
    )
    val usageSorted = sortCategoryGroups(groups, CategorySortingOption.USAGE, usageCounts = usageCounts)
    assertEquals(
      listOf(
        ToolCategory.MATH_AND_MEASUREMENT, // 100
        ToolCategory.PRODUCTIVITY, // 50
        ToolCategory.MISCELLANEOUS, // 25
        ToolCategory.MUSIC_AND_INSTRUMENTS, // 10
        ToolCategory.SYSTEM, // always last
      ),
      usageSorted.map { it.category },
    )

    // 4. Combined: Category Alpha + Tool Alpha within categories
    val combinedSorted = sortCategoryGroups(
      groups = groups,
      categorySorting = CategorySortingOption.ALPHABETICAL,
      categoryToolSorting = CategoryToolSortingOption.ALPHABETICAL,
    )
    val miscGroup = combinedSorted.first { it.category == ToolCategory.MISCELLANEOUS }
    // Dice ("Dice") < Plants ("Plants")
    assertEquals(listOf(Tool.DICE_ROLLER, Tool.PLANT_SCHEDULE), miscGroup.tools)
  }

  @Test
  fun toolSorting_fallbackToUserChoiceOnInvalidValue() {
    val fakePrefs = FakeSharedPreferences()
    fakePrefs.edit().putString(PREF_KEY_TOOL_SORTING, "INVALID_VALUE").apply()
    val repo = DefaultNavigationPreferencesRepository(fakePrefs)

    assertEquals(ToolSortingOption.USER_CHOICE, repo.getToolSorting())
  }

  @Test
  fun toolSortOrder_persistenceAndFallback() {
    val fakePrefs = FakeSharedPreferences()
    val repo = DefaultNavigationPreferencesRepository(fakePrefs)

    // Default is ASCENDING
    assertEquals(ToolSortOrder.ASCENDING, repo.getToolSortOrder())
    assertEquals(ToolSortOrder.ASCENDING, repo.toolSortOrderFlow.value)

    // Update to DESCENDING
    repo.setToolSortOrder(ToolSortOrder.DESCENDING)
    assertEquals(ToolSortOrder.DESCENDING, repo.getToolSortOrder())
    assertEquals(ToolSortOrder.DESCENDING, repo.toolSortOrderFlow.value)
    assertEquals("DESCENDING", fakePrefs.getString(PREF_KEY_TOOL_SORT_ORDER, null))

    // Fallback on invalid value
    fakePrefs.edit().putString(PREF_KEY_TOOL_SORT_ORDER, "CORRUPT").apply()
    val repo2 = DefaultNavigationPreferencesRepository(fakePrefs)
    assertEquals(ToolSortOrder.ASCENDING, repo2.getToolSortOrder())
  }

  @Test
  fun sortToolsAlphabetically_ascendingAndDescending_keepsSettingsLast() {
    val tools = listOf(
      Tool.DICE_ROLLER, // Dice
      Tool.CALCULATOR, // Calculator
      Tool.SETTINGS, // Settings
      Tool.TODO_LIST, // To Do
      Tool.BLUETOOTH_REMOTE, // Remote
      Tool.UNIT_CONVERTER, // Units
    )

    val ascending = sortToolsAlphabetically(tools, ToolSortOrder.ASCENDING)
    assertEquals(
      listOf(
        Tool.CALCULATOR, // "Calculator"
        Tool.DICE_ROLLER, // "Dice"
        Tool.BLUETOOTH_REMOTE, // "Remote"
        Tool.TODO_LIST, // "To Do"
        Tool.UNIT_CONVERTER, // "Units"
        Tool.SETTINGS, // "Settings" always last
      ),
      ascending,
    )

    val descending = sortToolsAlphabetically(tools, ToolSortOrder.DESCENDING)
    assertEquals(
      listOf(
        Tool.UNIT_CONVERTER, // "Units"
        Tool.TODO_LIST, // "To Do"
        Tool.BLUETOOTH_REMOTE, // "Remote"
        Tool.DICE_ROLLER, // "Dice"
        Tool.CALCULATOR, // "Calculator"
        Tool.SETTINGS, // "Settings" always last
      ),
      descending,
    )
  }

  @Test
  fun parseAndSerializeToolUsage_roundtripWorksCorrectly() {
    val usage = mapOf(
      Tool.CALCULATOR to 15,
      Tool.RULER to 3,
      Tool.DICE_ROLLER to 1,
    )
    val serialized = serializeToolUsage(usage)
    val parsed = parseToolUsage(serialized)
    assertEquals(usage, parsed)

    // Null or blank string returns empty map
    assertEquals(emptyMap<Tool, Int>(), parseToolUsage(null))
    assertEquals(emptyMap<Tool, Int>(), parseToolUsage(""))
    assertEquals(emptyMap<Tool, Int>(), parseToolUsage("   "))

    // Malformed entries ignored
    val malformed = "CALCULATOR:10,INVALID_TOOL:5,CORRUPT,RULER:-1"
    val parsedMalformed = parseToolUsage(malformed)
    assertEquals(mapOf(Tool.CALCULATOR to 10), parsedMalformed)
  }

  @Test
  fun sortToolsByUsage_ordersByCountDescendingAndSettingsAlwaysLast() {
    val tools = listOf(
      Tool.DICE_ROLLER,
      Tool.UNIT_CONVERTER,
      Tool.GUITAR_TUNER,
      Tool.CALCULATOR,
      Tool.SETTINGS,
    )
    val usage = mapOf(
      Tool.GUITAR_TUNER to 5,
      Tool.CALCULATOR to 20,
      Tool.DICE_ROLLER to 2,
      Tool.SETTINGS to 100, // even if Settings has highest count, it must remain last
    )

    val sorted = sortToolsByUsage(tools, usage)
    val expected = listOf(
      Tool.CALCULATOR, // 20
      Tool.GUITAR_TUNER, // 5
      Tool.DICE_ROLLER, // 2
      Tool.UNIT_CONVERTER, // 0
      Tool.SETTINGS, // last!
    )
    assertEquals(expected, sorted)
    assertEquals(Tool.SETTINGS, sorted.last())
  }

  @Test
  fun toolUsage_persistsAndIncrementsCorrectly() {
    val fakePrefs = FakeSharedPreferences()
    val repo = DefaultNavigationPreferencesRepository(fakePrefs)

    assertTrue(repo.getToolUsageCounts().isEmpty())

    repo.incrementToolUsage(Tool.CALCULATOR)
    assertEquals(1, repo.getToolUsageCounts()[Tool.CALCULATOR])
    assertEquals(1, repo.toolUsageCountsFlow.value[Tool.CALCULATOR])

    repo.incrementToolUsage(Tool.CALCULATOR)
    assertEquals(2, repo.getToolUsageCounts()[Tool.CALCULATOR])

    repo.incrementToolUsage(Tool.RULER)
    assertEquals(2, repo.getToolUsageCounts()[Tool.CALCULATOR])
    assertEquals(1, repo.getToolUsageCounts()[Tool.RULER])

    // Reset clears usage
    repo.resetToolUsage()
    assertTrue(repo.getToolUsageCounts().isEmpty())
    assertTrue(repo.toolUsageCountsFlow.value.isEmpty())
  }

  @Test
  fun toolSorting_usageOptionPersistsCorrectly() {
    val fakePrefs = FakeSharedPreferences()
    val repo = DefaultNavigationPreferencesRepository(fakePrefs)

    repo.setToolSorting(ToolSortingOption.USAGE)
    assertEquals(ToolSortingOption.USAGE, repo.getToolSorting())
    assertEquals(ToolSortingOption.USAGE, repo.toolSortingFlow.value)
    assertEquals("USAGE", fakePrefs.getString(PREF_KEY_TOOL_SORTING, null))
  }

  @Test
  fun buildCategoryRows_packsSmallCategoriesAndSplitsLargeCategoriesForTwoRowExpandedPagination() {
    val groups = listOf(
      CategoryGroup(
        ToolCategory.MATH_AND_MEASUREMENT,
        listOf(Tool.CALCULATOR, Tool.UNIT_CONVERTER, Tool.RULER),
      ),
      CategoryGroup(
        ToolCategory.PRODUCTIVITY,
        listOf(Tool.NOTES, Tool.TODO_LIST),
      ),
      CategoryGroup(
        ToolCategory.FILES_AND_STORAGE,
        listOf(Tool.FILES),
      ),
      CategoryGroup(
        ToolCategory.MISCELLANEOUS,
        listOf(Tool.COLOR_PICKER, Tool.DICE_ROLLER, Tool.PLANT_SCHEDULE, Tool.RECIPES),
      ),
      CategoryGroup(
        ToolCategory.SYSTEM,
        listOf(Tool.SETTINGS),
      ),
    )

    // At 3 tools per row:
    // Row 0: Math & Measurement (3)
    // Row 1: Productivity (2) + Files (1)
    // Row 2: Miscellaneous part 1 (3)
    // Row 3: Miscellaneous part 2 (1) + System (1)
    val rows3 = buildCategoryRows(groups, effectiveColumns = 3)
    assertEquals(4, rows3.size)
    assertEquals(listOf(ToolCategory.MATH_AND_MEASUREMENT), rows3[0].groups.map { it.category })
    assertEquals(
      listOf(ToolCategory.PRODUCTIVITY, ToolCategory.FILES_AND_STORAGE),
      rows3[1].groups.map { it.category },
    )
    assertEquals(listOf(ToolCategory.MISCELLANEOUS), rows3[2].groups.map { it.category })
    assertEquals(3, rows3[2].groups.first().tools.size)
    assertEquals(
      listOf(ToolCategory.MISCELLANEOUS, ToolCategory.SYSTEM),
      rows3[3].groups.map { it.category },
    )
    // Expanded mode paginates 2 category rows per page -> 2 pages for 4 rows
    assertEquals(2, calculateExpandedPageCount(rows3.size))
  }
}

/**
 * Lightweight fake implementation of [SharedPreferences] for JVM unit testing.
 */
private class FakeSharedPreferences : SharedPreferences {
  private val map = mutableMapOf<String, Any?>()

  override fun getAll(): MutableMap<String, *> = HashMap(map)

  override fun getString(key: String?, defValue: String?): String? =
    (map[key] as? String) ?: defValue

  override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
    @Suppress("UNCHECKED_CAST")
    (map[key] as? MutableSet<String>) ?: defValues

  override fun getInt(key: String?, defValue: Int): Int =
    (map[key] as? Int) ?: defValue

  override fun getLong(key: String?, defValue: Long): Long =
    (map[key] as? Long) ?: defValue

  override fun getFloat(key: String?, defValue: Float): Float =
    (map[key] as? Float) ?: defValue

  override fun getBoolean(key: String?, defValue: Boolean): Boolean =
    (map[key] as? Boolean) ?: defValue

  override fun contains(key: String?): Boolean = map.containsKey(key)

  override fun edit(): SharedPreferences.Editor = FakeEditor(map)

  override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

  override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

  private class FakeEditor(private val backingMap: MutableMap<String, Any?>) : SharedPreferences.Editor {
    private val pending = mutableMapOf<String, Any?>()
    private val removals = mutableSetOf<String>()
    private var clearAll = false

    override fun putString(key: String?, value: String?): SharedPreferences.Editor {
      if (key != null) pending[key] = value
      return this
    }

    override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
      if (key != null) pending[key] = values
      return this
    }

    override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
      if (key != null) pending[key] = value
      return this
    }

    override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
      if (key != null) pending[key] = value
      return this
    }

    override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
      if (key != null) pending[key] = value
      return this
    }

    override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
      if (key != null) pending[key] = value
      return this
    }

    override fun remove(key: String?): SharedPreferences.Editor {
      if (key != null) removals.add(key)
      return this
    }

    override fun clear(): SharedPreferences.Editor {
      clearAll = true
      return this
    }

    override fun commit(): Boolean {
      apply()
      return true
    }

    override fun apply() {
      if (clearAll) backingMap.clear()
      for (key in removals) backingMap.remove(key)
      backingMap.putAll(pending)
    }
  }
}
