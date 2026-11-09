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

const val DEFAULT_THEME_COLOR_SYSTEM = "SYSTEM"
const val FALLBACK_THEME_COLOR_HEX = "#8E66A6"

/**
 * Options for the application theme mode.
 */
enum class ThemeMode {
  SYSTEM,
  LIGHT,
  DARK,
}

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

  fun getHiddenTools(): Set<Tool>
  fun setHiddenTools(tools: Set<Tool>)
  val hiddenToolsFlow: StateFlow<Set<Tool>>
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

  private val _hiddenToolsFlow = MutableStateFlow(getHiddenTools())
  override val hiddenToolsFlow: StateFlow<Set<Tool>> = _hiddenToolsFlow.asStateFlow()

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
}

/**
 * In-memory implementation of [NavigationPreferencesRepository] useful for testing and Compose Previews.
 */
class InMemoryNavigationPreferencesRepository(
  initialToolsPerRow: Int = DEFAULT_TOOLS_PER_ROW,
  initialToolOrder: List<Tool> = Tool.entries.filter { it != Tool.SETTINGS } + Tool.SETTINGS,
  initialThemeMode: ThemeMode = ThemeMode.SYSTEM,
  initialThemeColor: String = DEFAULT_THEME_COLOR_SYSTEM,
  initialHiddenTools: Set<Tool> = emptySet(),
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

  private val _hiddenToolsFlow = MutableStateFlow(initialHiddenTools.filter { it != Tool.SETTINGS }.toSet())
  override val hiddenToolsFlow: StateFlow<Set<Tool>> = _hiddenToolsFlow.asStateFlow()

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

  override fun getHiddenTools(): Set<Tool> = _hiddenToolsFlow.value

  override fun setHiddenTools(tools: Set<Tool>) {
    _hiddenToolsFlow.value = tools.filter { it != Tool.SETTINGS }.toSet()
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
