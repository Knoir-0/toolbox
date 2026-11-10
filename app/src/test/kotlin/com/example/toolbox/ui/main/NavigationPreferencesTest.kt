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
  }

  @Test
  fun parseToolOrder_nullOrBlank_returnsAllToolEntriesInDefaultOrder() {
    assertEquals(Tool.entries, parseToolOrder(null))
    assertEquals(Tool.entries, parseToolOrder(""))
    assertEquals(Tool.entries, parseToolOrder("   "))
  }

  @Test
  fun parseToolOrder_validSavedOrder_preservesOrder() {
    val raw = "IMAGE_EDITOR,CALCULATOR,RULER,METRONOME,COLOR_PICKER,GUITAR_TUNER,UNIT_CONVERTER,DICE_ROLLER,BLUETOOTH_REMOTE,TODO_LIST,FILES,SETTINGS"
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
    assertEquals("By usage", ToolSortingOption.USAGE.displayName)
  }

  @Test
  fun toolSorting_fallbackToUserChoiceOnInvalidValue() {
    val fakePrefs = FakeSharedPreferences()
    fakePrefs.edit().putString(PREF_KEY_TOOL_SORTING, "INVALID_VALUE").apply()
    val repo = DefaultNavigationPreferencesRepository(fakePrefs)

    assertEquals(ToolSortingOption.USER_CHOICE, repo.getToolSorting())
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
