package com.example.toolbox.ui.main

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

const val NAVIGATION_PREFERENCES_NAME = "navigation_preferences"
const val PREF_KEY_TOOLS_PER_ROW = "tools_per_row"
const val PREF_KEY_TOOL_ORDER = "tool_order"
const val PREF_KEY_THEME_MODE = "theme_mode"
const val PREF_KEY_THEME_COLOR = "theme_color"
const val PREF_KEY_HIDDEN_TOOLS = "hidden_tools"
const val PREF_KEY_TOOL_SORTING = "tool_sorting"
const val PREF_KEY_TOOL_SORT_ORDER = "tool_sort_order"
const val PREF_KEY_CATEGORY_SORTING = "category_sorting"
const val PREF_KEY_CATEGORY_TOOL_SORTING = "category_tool_sorting"
const val PREF_KEY_TOOL_USAGE = "tool_usage"

const val DEFAULT_THEME_COLOR_SYSTEM = "SYSTEM"
const val FALLBACK_THEME_COLOR_HEX = "#8E66A6"
const val DEFAULT_ACCENT_COLOR_AUTO = "AUTO"
const val PREF_KEY_ACCENT_COLOR = "accent_color"

/**
 * Options for the application theme mode.
 */
enum class ThemeMode {
  SYSTEM,
  LIGHT,
  DARK,
}

/**
 * Options for organizing tools in the Toolbox navigation panel.
 */
enum class ToolSortingOption(
  val displayName: String,
) {
  USER_CHOICE(displayName = "Manual"),
  TOOL_CATEGORIES(displayName = "Tool categories"),
  USAGE(displayName = "Usage"),
  ALPHABETICAL(displayName = "Alphabetical"),
}

/**
 * Options for ordering tools alphabetically (Ascending A-Z or Descending Z-A).
 */
enum class ToolSortOrder(
  val displayName: String,
) {
  ASCENDING(displayName = "Ascending"),
  DESCENDING(displayName = "Descending"),
}

/**
 * Options for ordering tool categories in the Toolbox navigation panel.
 */
enum class CategorySortingOption(
  val displayName: String,
) {
  MANUAL(displayName = "Manual"),
  ALPHABETICAL(displayName = "Alpha"),
  USAGE(displayName = "Usage"),
}

/**
 * Options for ordering tools within categories in the Toolbox navigation panel.
 */
enum class CategoryToolSortingOption(
  val displayName: String,
) {
  MANUAL(displayName = "Manual"),
  ALPHABETICAL(displayName = "Alpha"),
  USAGE(displayName = "Usage"),
}

/**
 * Represents a group of tools that share a common [ToolCategory].
 */
data class CategoryGroup(
  val category: ToolCategory,
  val tools: List<Tool>,
)

/**
 * Parses a comma-separated list of tool names into a list of [Tool] enums.
 *
 * Preserves the stored order, discards invalid/unknown tool names, removes duplicates,
 * appends any [Tool] entries that were not present in the saved preference,
 * and ensures that [Tool.SETTINGS] is ALWAYS the very last tool.
 */
fun parseToolOrder(rawString: String?): List<Tool> {
  val nonSettingsTools = Tool.entries.filter { it != Tool.SETTINGS }
  if (rawString.isNullOrBlank()) {
    return nonSettingsTools + Tool.SETTINGS
  }
  val names = rawString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
  val savedTools = names.mapNotNull { name ->
    try {
      Tool.valueOf(name)
    } catch (_: IllegalArgumentException) {
      null
    }
  }.filter { it != Tool.SETTINGS }.distinct()

  val missingTools = nonSettingsTools.filter { it !in savedTools }
  return savedTools + missingTools + Tool.SETTINGS
}

/**
 * Serializes a list of [Tool] enums into a comma-separated string of names.
 */
fun serializeToolOrder(tools: List<Tool>): String {
  return tools.joinToString(",") { it.name }
}

/**
 * Parses a comma-separated list of tool names into a set of hidden [Tool] enums.
 * [Tool.SETTINGS] can never be hidden and is always removed.
 */
fun parseHiddenTools(rawString: String?): Set<Tool> {
  if (rawString.isNullOrBlank()) {
    return emptySet()
  }
  val names = rawString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
  return names.mapNotNull { name ->
    try {
      Tool.valueOf(name)
    } catch (_: IllegalArgumentException) {
      null
    }
  }.filter { it != Tool.SETTINGS }.toSet()
}

/**
 * Serializes a set of hidden [Tool] enums into a comma-separated string of names,
 * stripping [Tool.SETTINGS] so settings is never persisted as hidden.
 */
fun serializeHiddenTools(tools: Set<Tool>): String {
  return tools.filter { it != Tool.SETTINGS }.joinToString(",") { it.name }
}

/**
 * Parses a comma-separated key:value string into a map of [Tool] to usage counts.
 */
fun parseToolUsage(rawString: String?): Map<Tool, Int> {
  if (rawString.isNullOrBlank()) return emptyMap()
  val result = mutableMapOf<Tool, Int>()
  rawString.split(",").forEach { entry ->
    val parts = entry.split(":")
    if (parts.size == 2) {
      val toolName = parts[0].trim()
      val count = parts[1].trim().toIntOrNull() ?: 0
      try {
        val tool = Tool.valueOf(toolName)
        if (count > 0) {
          result[tool] = count
        }
      } catch (_: IllegalArgumentException) {}
    }
  }
  return result
}

/**
 * Serializes a map of [Tool] usage counts into a comma-separated key:value string.
 */
fun serializeToolUsage(usage: Map<Tool, Int>): String {
  return usage.filter { it.value > 0 }
    .map { "${it.key.name}:${it.value}" }
    .joinToString(",")
}

/**
 * Sorts tools in descending order of usage counts while guaranteeing [Tool.SETTINGS] is last.
 */
fun sortToolsByUsage(
  tools: List<Tool>,
  usageCounts: Map<Tool, Int>,
): List<Tool> {
  val nonSettings = tools.filter { it != Tool.SETTINGS }
  val sorted = nonSettings.sortedWith(
    compareByDescending<Tool> { usageCounts[it] ?: 0 }
      .thenBy { tools.indexOf(it) }
  )
  return if (tools.contains(Tool.SETTINGS)) {
    sorted + Tool.SETTINGS
  } else {
    sorted
  }
}

/**
 * Sorts tools alphabetically by tool title in either ascending or descending order,
 * while guaranteeing [Tool.SETTINGS] is always last.
 */
fun sortToolsAlphabetically(
  tools: List<Tool>,
  sortOrder: ToolSortOrder = ToolSortOrder.ASCENDING,
): List<Tool> {
  val nonSettings = tools.filter { it != Tool.SETTINGS }
  val baseComparator = compareBy<Tool, String>(String.CASE_INSENSITIVE_ORDER) { it.title }.thenBy { it.name }
  val comparator = if (sortOrder == ToolSortOrder.ASCENDING) {
    baseComparator
  } else {
    baseComparator.reversed()
  }
  val sorted = nonSettings.sortedWith(comparator)
  return if (tools.contains(Tool.SETTINGS)) {
    sorted + Tool.SETTINGS
  } else {
    sorted
  }
}

/**
 * Sorts tools within a category by manual, alphabetical, or usage ordering.
 * [Tool.SETTINGS] is always guaranteed to be placed last.
 */
fun sortToolsWithinCategory(
  tools: List<Tool>,
  toolSorting: CategoryToolSortingOption,
  usageCounts: Map<Tool, Int> = emptyMap(),
): List<Tool> {
  val nonSettings = tools.filter { it != Tool.SETTINGS }
  val settingsTool = tools.filter { it == Tool.SETTINGS }

  val sorted = when (toolSorting) {
    CategoryToolSortingOption.MANUAL -> nonSettings
    CategoryToolSortingOption.ALPHABETICAL -> {
      nonSettings.sortedWith(
        compareBy<Tool, String>(String.CASE_INSENSITIVE_ORDER) { it.title }.thenBy { it.name }
      )
    }
    CategoryToolSortingOption.USAGE -> {
      nonSettings.sortedWith(
        compareByDescending<Tool> { usageCounts[it] ?: 0 }.thenBy { tools.indexOf(it) }
      )
    }
  }

  return sorted + settingsTool
}

/**
 * Sorts category groups and the tools within them by manual, alphabetical, or usage ordering.
 * [ToolCategory.SYSTEM] is always guaranteed to be placed last.
 */
fun sortCategoryGroups(
  groups: List<CategoryGroup>,
  categorySorting: CategorySortingOption,
  categoryToolSorting: CategoryToolSortingOption = CategoryToolSortingOption.MANUAL,
  usageCounts: Map<Tool, Int> = emptyMap(),
): List<CategoryGroup> {
  val groupsWithSortedTools = groups.map { group ->
    group.copy(tools = sortToolsWithinCategory(group.tools, categoryToolSorting, usageCounts))
  }

  val nonSystem = groupsWithSortedTools.filter { it.category != ToolCategory.SYSTEM }
  val systemGroup = groupsWithSortedTools.filter { it.category == ToolCategory.SYSTEM }

  val sorted = when (categorySorting) {
    CategorySortingOption.MANUAL -> {
      nonSystem.sortedBy { ToolCategory.entries.indexOf(it.category) }
    }
    CategorySortingOption.ALPHABETICAL -> {
      nonSystem.sortedWith(
        compareBy(String.CASE_INSENSITIVE_ORDER) { it.category.title }
      )
    }
    CategorySortingOption.USAGE -> {
      nonSystem.sortedWith(
        compareByDescending<CategoryGroup> { group ->
          group.tools.sumOf { usageCounts[it] ?: 0 }
        }.thenBy { ToolCategory.entries.indexOf(it.category) }
      )
    }
  }

  return sorted + systemGroup
}

/**
 * Repository interface for persisting user navigation preferences (tools per row, tool order,
 * theme mode, theme color, and tool visibility).
 */
interface NavigationPreferencesRepository {
  fun getToolsPerRow(): Int
  fun setToolsPerRow(count: Int)
  val toolsPerRowFlow: StateFlow<Int>

  fun getToolOrder(): List<Tool>
  fun setToolOrder(tools: List<Tool>)
  val toolOrderFlow: StateFlow<List<Tool>>

  fun getThemeMode(): ThemeMode
  fun setThemeMode(mode: ThemeMode)
  val themeModeFlow: StateFlow<ThemeMode>

  fun getThemeColor(): String
  fun setThemeColor(color: String)
  val themeColorFlow: StateFlow<String>

  fun getAccentColor(): String
  fun setAccentColor(color: String)
  val accentColorFlow: StateFlow<String>

  fun getHiddenTools(): Set<Tool>
  fun setHiddenTools(tools: Set<Tool>)
  val hiddenToolsFlow: StateFlow<Set<Tool>>

  fun getToolSorting(): ToolSortingOption
  fun setToolSorting(sorting: ToolSortingOption)
  val toolSortingFlow: StateFlow<ToolSortingOption>

  fun getToolSortOrder(): ToolSortOrder
  fun setToolSortOrder(order: ToolSortOrder)
  val toolSortOrderFlow: StateFlow<ToolSortOrder>

  fun getCategorySorting(): CategorySortingOption
  fun setCategorySorting(sorting: CategorySortingOption)
  val categorySortingFlow: StateFlow<CategorySortingOption>

  fun getCategoryToolSorting(): CategoryToolSortingOption
  fun setCategoryToolSorting(sorting: CategoryToolSortingOption)
  val categoryToolSortingFlow: StateFlow<CategoryToolSortingOption>

  fun getToolUsageCounts(): Map<Tool, Int>
  fun incrementToolUsage(tool: Tool)
  fun resetToolUsage()
  val toolUsageCountsFlow: StateFlow<Map<Tool, Int>>
}

/**
 * Android [SharedPreferences]-backed implementation of [NavigationPreferencesRepository].
 */
class DefaultNavigationPreferencesRepository(
  private val sharedPreferences: SharedPreferences,
) : NavigationPreferencesRepository {

  constructor(context: Context) : this(
    context.getSharedPreferences(NAVIGATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
  )

  private val _toolsPerRowFlow = MutableStateFlow(getToolsPerRow())
  override val toolsPerRowFlow: StateFlow<Int> = _toolsPerRowFlow.asStateFlow()

  private val _toolOrderFlow = MutableStateFlow(getToolOrder())
  override val toolOrderFlow: StateFlow<List<Tool>> = _toolOrderFlow.asStateFlow()

  private val _themeModeFlow = MutableStateFlow(getThemeMode())
  override val themeModeFlow: StateFlow<ThemeMode> = _themeModeFlow.asStateFlow()

  private val _themeColorFlow = MutableStateFlow(getThemeColor())
  override val themeColorFlow: StateFlow<String> = _themeColorFlow.asStateFlow()

  private val _accentColorFlow = MutableStateFlow(getAccentColor())
  override val accentColorFlow: StateFlow<String> = _accentColorFlow.asStateFlow()

  private val _hiddenToolsFlow = MutableStateFlow(getHiddenTools())
  override val hiddenToolsFlow: StateFlow<Set<Tool>> = _hiddenToolsFlow.asStateFlow()

  private val _toolSortingFlow = MutableStateFlow(getToolSorting())
  override val toolSortingFlow: StateFlow<ToolSortingOption> = _toolSortingFlow.asStateFlow()

  private val _toolSortOrderFlow = MutableStateFlow(getToolSortOrder())
  override val toolSortOrderFlow: StateFlow<ToolSortOrder> = _toolSortOrderFlow.asStateFlow()

  private val _categorySortingFlow = MutableStateFlow(getCategorySorting())
  override val categorySortingFlow: StateFlow<CategorySortingOption> = _categorySortingFlow.asStateFlow()

  private val _categoryToolSortingFlow = MutableStateFlow(getCategoryToolSorting())
  override val categoryToolSortingFlow: StateFlow<CategoryToolSortingOption> = _categoryToolSortingFlow.asStateFlow()

  private val _toolUsageCountsFlow = MutableStateFlow(getToolUsageCounts())
  override val toolUsageCountsFlow: StateFlow<Map<Tool, Int>> = _toolUsageCountsFlow.asStateFlow()

  override fun getToolsPerRow(): Int {
    val count = sharedPreferences.getInt(PREF_KEY_TOOLS_PER_ROW, DEFAULT_TOOLS_PER_ROW)
    return coerceToolsPerRow(count)
  }

  override fun setToolsPerRow(count: Int) {
    val coerced = coerceToolsPerRow(count)
    sharedPreferences.edit().putInt(PREF_KEY_TOOLS_PER_ROW, coerced).apply()
    _toolsPerRowFlow.value = coerced
  }

  override fun getToolOrder(): List<Tool> {
    val raw = sharedPreferences.getString(PREF_KEY_TOOL_ORDER, null)
    return parseToolOrder(raw)
  }

  override fun setToolOrder(tools: List<Tool>) {
    val sanitized = tools.filter { it != Tool.SETTINGS } + Tool.SETTINGS
    val serialized = serializeToolOrder(sanitized)
    sharedPreferences.edit().putString(PREF_KEY_TOOL_ORDER, serialized).apply()
    _toolOrderFlow.value = sanitized
  }

  override fun getThemeMode(): ThemeMode {
    val raw = sharedPreferences.getString(PREF_KEY_THEME_MODE, null) ?: return ThemeMode.SYSTEM
    return try {
      ThemeMode.valueOf(raw)
    } catch (_: IllegalArgumentException) {
      ThemeMode.SYSTEM
    }
  }

  override fun setThemeMode(mode: ThemeMode) {
    sharedPreferences.edit().putString(PREF_KEY_THEME_MODE, mode.name).apply()
    _themeModeFlow.value = mode
  }

  override fun getThemeColor(): String {
    return sharedPreferences.getString(PREF_KEY_THEME_COLOR, DEFAULT_THEME_COLOR_SYSTEM)
      ?: DEFAULT_THEME_COLOR_SYSTEM
  }

  override fun setThemeColor(color: String) {
    sharedPreferences.edit().putString(PREF_KEY_THEME_COLOR, color).apply()
    _themeColorFlow.value = color
  }

  override fun getAccentColor(): String {
    return sharedPreferences.getString(PREF_KEY_ACCENT_COLOR, DEFAULT_ACCENT_COLOR_AUTO)
      ?: DEFAULT_ACCENT_COLOR_AUTO
  }

  override fun setAccentColor(color: String) {
    sharedPreferences.edit().putString(PREF_KEY_ACCENT_COLOR, color).apply()
    _accentColorFlow.value = color
  }

  override fun getHiddenTools(): Set<Tool> {
    val raw = sharedPreferences.getString(PREF_KEY_HIDDEN_TOOLS, null)
    return parseHiddenTools(raw)
  }

  override fun setHiddenTools(tools: Set<Tool>) {
    val sanitized = tools.filter { it != Tool.SETTINGS }.toSet()
    val serialized = serializeHiddenTools(sanitized)
    sharedPreferences.edit().putString(PREF_KEY_HIDDEN_TOOLS, serialized).apply()
    _hiddenToolsFlow.value = sanitized
  }

  override fun getToolSorting(): ToolSortingOption {
    val raw = sharedPreferences.getString(PREF_KEY_TOOL_SORTING, null) ?: return ToolSortingOption.USER_CHOICE
    return try {
      ToolSortingOption.valueOf(raw)
    } catch (_: IllegalArgumentException) {
      ToolSortingOption.USER_CHOICE
    }
  }

  override fun setToolSorting(sorting: ToolSortingOption) {
    sharedPreferences.edit().putString(PREF_KEY_TOOL_SORTING, sorting.name).apply()
    _toolSortingFlow.value = sorting
  }

  override fun getToolSortOrder(): ToolSortOrder {
    val raw = sharedPreferences.getString(PREF_KEY_TOOL_SORT_ORDER, null) ?: return ToolSortOrder.ASCENDING
    return try {
      ToolSortOrder.valueOf(raw)
    } catch (_: IllegalArgumentException) {
      ToolSortOrder.ASCENDING
    }
  }

  override fun setToolSortOrder(order: ToolSortOrder) {
    sharedPreferences.edit().putString(PREF_KEY_TOOL_SORT_ORDER, order.name).apply()
    _toolSortOrderFlow.value = order
  }

  override fun getCategorySorting(): CategorySortingOption {
    val raw = sharedPreferences.getString(PREF_KEY_CATEGORY_SORTING, null) ?: return CategorySortingOption.MANUAL
    return try {
      CategorySortingOption.valueOf(raw)
    } catch (_: IllegalArgumentException) {
      CategorySortingOption.MANUAL
    }
  }

  override fun setCategorySorting(sorting: CategorySortingOption) {
    sharedPreferences.edit().putString(PREF_KEY_CATEGORY_SORTING, sorting.name).apply()
    _categorySortingFlow.value = sorting
  }

  override fun getCategoryToolSorting(): CategoryToolSortingOption {
    val raw = sharedPreferences.getString(PREF_KEY_CATEGORY_TOOL_SORTING, null) ?: return CategoryToolSortingOption.MANUAL
    return try {
      CategoryToolSortingOption.valueOf(raw)
    } catch (_: IllegalArgumentException) {
      CategoryToolSortingOption.MANUAL
    }
  }

  override fun setCategoryToolSorting(sorting: CategoryToolSortingOption) {
    sharedPreferences.edit().putString(PREF_KEY_CATEGORY_TOOL_SORTING, sorting.name).apply()
    _categoryToolSortingFlow.value = sorting
  }

  override fun getToolUsageCounts(): Map<Tool, Int> {
    val raw = sharedPreferences.getString(PREF_KEY_TOOL_USAGE, null)
    return parseToolUsage(raw)
  }

  override fun incrementToolUsage(tool: Tool) {
    val current = getToolUsageCounts().toMutableMap()
    val updated = (current[tool] ?: 0) + 1
    current[tool] = updated
    sharedPreferences.edit().putString(PREF_KEY_TOOL_USAGE, serializeToolUsage(current)).apply()
    _toolUsageCountsFlow.value = current
  }

  override fun resetToolUsage() {
    sharedPreferences.edit().remove(PREF_KEY_TOOL_USAGE).apply()
    _toolUsageCountsFlow.value = emptyMap()
  }
}

/**
 * In-memory implementation of [NavigationPreferencesRepository] useful for testing and Compose Previews.
 */
class InMemoryNavigationPreferencesRepository(
  initialToolsPerRow: Int = DEFAULT_TOOLS_PER_ROW,
  initialToolOrder: List<Tool> = Tool.entries.filter { it != Tool.SETTINGS } + Tool.SETTINGS,
  initialThemeMode: ThemeMode = ThemeMode.SYSTEM,
  initialThemeColor: String = DEFAULT_THEME_COLOR_SYSTEM,
  initialAccentColor: String = DEFAULT_ACCENT_COLOR_AUTO,
  initialHiddenTools: Set<Tool> = emptySet(),
  initialToolSorting: ToolSortingOption = ToolSortingOption.USER_CHOICE,
  initialToolSortOrder: ToolSortOrder = ToolSortOrder.ASCENDING,
  initialCategorySorting: CategorySortingOption = CategorySortingOption.MANUAL,
  initialCategoryToolSorting: CategoryToolSortingOption = CategoryToolSortingOption.MANUAL,
  initialToolUsage: Map<Tool, Int> = emptyMap(),
) : NavigationPreferencesRepository {
  private val _toolsPerRowFlow = MutableStateFlow(coerceToolsPerRow(initialToolsPerRow))
  override val toolsPerRowFlow: StateFlow<Int> = _toolsPerRowFlow.asStateFlow()

  private val _toolOrderFlow = MutableStateFlow(
    initialToolOrder.filter { it != Tool.SETTINGS } + Tool.SETTINGS
  )
  override val toolOrderFlow: StateFlow<List<Tool>> = _toolOrderFlow.asStateFlow()

  private val _themeModeFlow = MutableStateFlow(initialThemeMode)
  override val themeModeFlow: StateFlow<ThemeMode> = _themeModeFlow.asStateFlow()

  private val _themeColorFlow = MutableStateFlow(initialThemeColor)
  override val themeColorFlow: StateFlow<String> = _themeColorFlow.asStateFlow()

  private val _accentColorFlow = MutableStateFlow(initialAccentColor)
  override val accentColorFlow: StateFlow<String> = _accentColorFlow.asStateFlow()

  private val _hiddenToolsFlow = MutableStateFlow(initialHiddenTools.filter { it != Tool.SETTINGS }.toSet())
  override val hiddenToolsFlow: StateFlow<Set<Tool>> = _hiddenToolsFlow.asStateFlow()

  private val _toolSortingFlow = MutableStateFlow(initialToolSorting)
  override val toolSortingFlow: StateFlow<ToolSortingOption> = _toolSortingFlow.asStateFlow()

  private val _toolSortOrderFlow = MutableStateFlow(initialToolSortOrder)
  override val toolSortOrderFlow: StateFlow<ToolSortOrder> = _toolSortOrderFlow.asStateFlow()

  private val _categorySortingFlow = MutableStateFlow(initialCategorySorting)
  override val categorySortingFlow: StateFlow<CategorySortingOption> = _categorySortingFlow.asStateFlow()

  private val _categoryToolSortingFlow = MutableStateFlow(initialCategoryToolSorting)
  override val categoryToolSortingFlow: StateFlow<CategoryToolSortingOption> = _categoryToolSortingFlow.asStateFlow()

  private val _toolUsageCountsFlow = MutableStateFlow(initialToolUsage)
  override val toolUsageCountsFlow: StateFlow<Map<Tool, Int>> = _toolUsageCountsFlow.asStateFlow()

  override fun getToolsPerRow(): Int = _toolsPerRowFlow.value

  override fun setToolsPerRow(count: Int) {
    _toolsPerRowFlow.value = coerceToolsPerRow(count)
  }

  override fun getToolOrder(): List<Tool> = _toolOrderFlow.value

  override fun setToolOrder(tools: List<Tool>) {
    _toolOrderFlow.value = tools.filter { it != Tool.SETTINGS } + Tool.SETTINGS
  }

  override fun getThemeMode(): ThemeMode = _themeModeFlow.value

  override fun setThemeMode(mode: ThemeMode) {
    _themeModeFlow.value = mode
  }

  override fun getThemeColor(): String = _themeColorFlow.value

  override fun setThemeColor(color: String) {
    _themeColorFlow.value = color
  }

  override fun getAccentColor(): String = _accentColorFlow.value

  override fun setAccentColor(color: String) {
    _accentColorFlow.value = color
  }

  override fun getHiddenTools(): Set<Tool> = _hiddenToolsFlow.value

  override fun setHiddenTools(tools: Set<Tool>) {
    _hiddenToolsFlow.value = tools.filter { it != Tool.SETTINGS }.toSet()
  }

  override fun getToolSorting(): ToolSortingOption = _toolSortingFlow.value

  override fun setToolSorting(sorting: ToolSortingOption) {
    _toolSortingFlow.value = sorting
  }

  override fun getToolSortOrder(): ToolSortOrder = _toolSortOrderFlow.value

  override fun setToolSortOrder(order: ToolSortOrder) {
    _toolSortOrderFlow.value = order
  }

  override fun getCategorySorting(): CategorySortingOption = _categorySortingFlow.value

  override fun setCategorySorting(sorting: CategorySortingOption) {
    _categorySortingFlow.value = sorting
  }

  override fun getCategoryToolSorting(): CategoryToolSortingOption = _categoryToolSortingFlow.value

  override fun setCategoryToolSorting(sorting: CategoryToolSortingOption) {
    _categoryToolSortingFlow.value = sorting
  }

  override fun getToolUsageCounts(): Map<Tool, Int> = _toolUsageCountsFlow.value

  override fun incrementToolUsage(tool: Tool) {
    val current = _toolUsageCountsFlow.value.toMutableMap()
    current[tool] = (current[tool] ?: 0) + 1
    _toolUsageCountsFlow.value = current
  }

  override fun resetToolUsage() {
    _toolUsageCountsFlow.value = emptyMap()
  }
}

/**
 * Remembers a [NavigationPreferencesRepository] instance scoped to the current composition.
 * Returns an [InMemoryNavigationPreferencesRepository] in inspection mode (Compose preview).
 */
@Composable
fun rememberNavigationPreferencesRepository(): NavigationPreferencesRepository {
  val isInspection = LocalInspectionMode.current
  val context = LocalContext.current
  return remember(isInspection, context) {
    if (isInspection) {
      InMemoryNavigationPreferencesRepository()
    } else {
      DefaultNavigationPreferencesRepository(context.applicationContext)
    }
  }
}
