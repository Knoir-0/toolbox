package com.example.toolbox.ui.main

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

const val NAVIGATION_PREFERENCES_NAME = "navigation_preferences"
const val PREF_KEY_TOOLS_PER_ROW = "tools_per_row"
const val PREF_KEY_TOOL_ORDER = "tool_order"
const val PREF_KEY_THEME_MODE = "theme_mode"
const val PREF_KEY_THEME_COLOR = "theme_color"
const val PREF_KEY_MUSIC_PLAYER_THEME_ENABLED = "music_player_theme_enabled"
const val PREF_KEY_MUSIC_PLAYER_COLOR_OPTION = "music_player_color_option"
const val PREF_KEY_HIDDEN_TOOLS = "hidden_tools"
const val PREF_KEY_KNOWN_DEFAULT_HIDDEN_TOOLS = "known_default_hidden_tools"
const val PREF_KEY_TOOL_SORTING = "tool_sorting"
const val PREF_KEY_TOOL_SORT_ORDER = "tool_sort_order"
const val PREF_KEY_CATEGORY_SORTING = "category_sorting"
const val PREF_KEY_CATEGORY_TOOL_SORTING = "category_tool_sorting"
const val PREF_KEY_TOOL_USAGE = "tool_usage"
const val PREF_KEY_PERMISSION_STATES = "permission_states"
const val PREF_KEY_PERMISSION_USAGE = "permission_usage"
const val PREF_KEY_LAST_OPENED_TOOL = "last_opened_tool"

const val DEFAULT_THEME_COLOR_SYSTEM = "SYSTEM"
const val FALLBACK_THEME_COLOR_HEX = "#8E66A6"
const val DEFAULT_ACCENT_COLOR_AUTO = "AUTO"
const val ACCENT_COLOR_AUTO_COMPLEMENTARY = "AUTO_COMPLEMENTARY"
const val ACCENT_COLOR_AUTO_ANALOGOUS = "AUTO_ANALOGOUS"
const val ACCENT_COLOR_AUTO_ANALOGOUS_MINUS_30 = "AUTO_ANALOGOUS_MINUS_30"
const val ACCENT_COLOR_AUTO_TRIADIC = "AUTO_TRIADIC"
const val ACCENT_COLOR_AUTO_TRIADIC_240 = "AUTO_TRIADIC_240"
const val ACCENT_COLOR_AUTO_SPLIT_COMPLEMENTARY = "AUTO_SPLIT_COMPLEMENTARY"
const val ACCENT_COLOR_AUTO_SPLIT_COMPLEMENTARY_210 = "AUTO_SPLIT_COMPLEMENTARY_210"
const val ACCENT_COLOR_AUTO_SHADES = "AUTO_SHADES"
const val ACCENT_COLOR_AUTO_TINT_LIGHT = "AUTO_TINT_LIGHT"
const val ACCENT_COLOR_AUTO_MONOCHROMATIC = "AUTO_MONOCHROMATIC"
const val ACCENT_COLOR_AUTO_SHADE_MID = "AUTO_SHADE_MID"
const val ACCENT_COLOR_AUTO_SHADE_DEEP = "AUTO_SHADE_DEEP"
const val THEME_COLOR_MUSIC_PLAYER = "MUSIC_PLAYER"
const val THEME_COLOR_MUSIC_DOMINANT = "MUSIC_PLAYER_DOMINANT"
const val THEME_COLOR_MUSIC_SECONDARY = "MUSIC_PLAYER_SECONDARY"
const val THEME_COLOR_MUSIC_ACCENT = "MUSIC_PLAYER_ACCENT"
const val DEFAULT_MUSIC_PLAYER_THEME_ENABLED = true
const val DEFAULT_MUSIC_PLAYER_COLOR_OPTION = THEME_COLOR_MUSIC_PLAYER
const val ACCENT_COLOR_MUSIC_PLAYER = THEME_COLOR_MUSIC_PLAYER
const val ACCENT_COLOR_MUSIC_DOMINANT = THEME_COLOR_MUSIC_DOMINANT
const val ACCENT_COLOR_MUSIC_SECONDARY = THEME_COLOR_MUSIC_SECONDARY
const val ACCENT_COLOR_MUSIC_ACCENT = THEME_COLOR_MUSIC_ACCENT
const val PREF_KEY_ACCENT_COLOR = "accent_color"

/**
 * Represents each Android permission (or permission group) declared and used by Toolbox,
 * along with its human-readable metadata and the [Tool]s that use it.
 */
enum class AppPermission(
  val title: String,
  val manifestPermission: String,
  val manifestPermissions: List<String>,
  val description: String,
  val toolsUsingPermission: List<Tool>,
) {
  CAMERA(
    title = "Camera",
    manifestPermission = "android.permission.CAMERA",
    manifestPermissions = listOf("android.permission.CAMERA"),
    description = "Capture photos, record video, and sample live camera colors",
    toolsUsingPermission = listOf(
      Tool.CAMERA,
      Tool.COLOR_PICKER,
    ),
  ),
  RECORD_AUDIO(
    title = "Microphone",
    manifestPermission = "android.permission.RECORD_AUDIO",
    manifestPermissions = listOf("android.permission.RECORD_AUDIO"),
    description = "Record voice memos, video audio, guitar pitch, and sound pressure levels",
    toolsUsingPermission = listOf(
      Tool.AUDIO_RECORDER,
      Tool.CAMERA,
      Tool.GUITAR_TUNER,
      Tool.DECIBEL_METER,
    ),
  ),
  STORAGE(
    title = "Files & Storage",
    manifestPermission = "android.permission.MANAGE_EXTERNAL_STORAGE",
    manifestPermissions = listOf(
      "android.permission.MANAGE_EXTERNAL_STORAGE",
      "android.permission.READ_EXTERNAL_STORAGE",
      "android.permission.WRITE_EXTERNAL_STORAGE",
    ),
    description = "Browse, edit, convert, and manage local files, media, documents, and archives",
    toolsUsingPermission = listOf(
      Tool.FILES,
      Tool.GALLERY,
      Tool.MUSIC,
      Tool.IMAGE_EDITOR,
      Tool.VIDEO_EDITOR,
      Tool.AUDIO_EDITOR,
      Tool.AUDIO_RECORDER,
      Tool.FILE_CONVERTER,
      Tool.ARCHIVE,
      Tool.PDF_VIEWER,
      Tool.SEARCH,
    ),
  ),
  BLUETOOTH(
    title = "Bluetooth & Nearby Devices",
    manifestPermission = "android.permission.BLUETOOTH_CONNECT",
    manifestPermissions = listOf(
      "android.permission.BLUETOOTH_CONNECT",
      "android.permission.BLUETOOTH_SCAN",
      "android.permission.BLUETOOTH_ADVERTISE",
      "android.permission.BLUETOOTH",
      "android.permission.BLUETOOTH_ADMIN",
      "android.permission.ACCESS_FINE_LOCATION",
    ),
    description = "Discover, pair, and connect to Bluetooth HID remotes, gamepads, and OBD2 scanners",
    toolsUsingPermission = listOf(
      Tool.BLUETOOTH_REMOTE,
      Tool.BLUETOOTH_GAMEPAD,
      Tool.OBD2_SCANNER,
      Tool.MUSIC,
    ),
  ),
  POST_NOTIFICATIONS(
    title = "Notifications",
    manifestPermission = "android.permission.POST_NOTIFICATIONS",
    manifestPermissions = listOf("android.permission.POST_NOTIFICATIONS"),
    description = "Display task reminders, plant watering alerts, and media playback notifications",
    toolsUsingPermission = listOf(
      Tool.TODO_LIST,
      Tool.PLANT_SCHEDULE,
      Tool.MUSIC,
    ),
  ),
  SCHEDULE_EXACT_ALARM(
    title = "Exact Alarms",
    manifestPermission = "android.permission.SCHEDULE_EXACT_ALARM",
    manifestPermissions = listOf(
      "android.permission.SCHEDULE_EXACT_ALARM",
      "android.permission.USE_EXACT_ALARM",
    ),
    description = "Schedule exact-time reminders for tasks and plant care",
    toolsUsingPermission = listOf(
      Tool.TODO_LIST,
      Tool.PLANT_SCHEDULE,
    ),
  ),
  NOTIFICATION_LISTENER(
    title = "System Media Access",
    manifestPermission = "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE",
    manifestPermissions = listOf("android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"),
    description = "Observe active Android system media player sessions for dynamic theme colors",
    toolsUsingPermission = listOf(
      Tool.SETTINGS,
      Tool.MUSIC,
    ),
  ),
  VIBRATE(
    title = "Vibration & Haptics",
    manifestPermission = "android.permission.VIBRATE",
    manifestPermissions = listOf("android.permission.VIBRATE"),
    description = "Provide haptic feedback for metronome beats, dice rolls, controllers, and alerts",
    toolsUsingPermission = listOf(
      Tool.METRONOME,
      Tool.DICE_ROLLER,
      Tool.BLUETOOTH_REMOTE,
      Tool.BLUETOOTH_GAMEPAD,
      Tool.TODO_LIST,
    ),
  ),
  RECEIVE_BOOT_COMPLETED(
    title = "Run at Startup",
    manifestPermission = "android.permission.RECEIVE_BOOT_COMPLETED",
    manifestPermissions = listOf("android.permission.RECEIVE_BOOT_COMPLETED"),
    description = "Reschedule active task and plant reminders automatically after device reboot",
    toolsUsingPermission = listOf(
      Tool.TODO_LIST,
      Tool.PLANT_SCHEDULE,
    ),
  );

  companion object {
    fun permissionsForTool(tool: Tool): List<AppPermission> =
      entries.filter { tool in it.toolsUsingPermission }
  }
}

/**
 * Formats a permission usage timestamp (`lastUsedTimestampMs`) in `yyyy-MM-dd HH:mm` format,
 * or `"Never"` if `<= 0L`.
 */
fun formatPermissionUsageTimestamp(timestampMs: Long): String {
  if (timestampMs <= 0L) return "Never"
  val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
  return sdf.format(Date(timestampMs))
}

/**
 * Tracks how many times a specific [Tool] has used a given [AppPermission] and the last
 * timestamp (`lastUsedTimestampMs`) when it was used.
 */
data class PermissionToolUsageStat(
  val permission: AppPermission,
  val tool: Tool,
  val useCount: Int = 0,
  val lastUsedTimestampMs: Long = 0L,
) {
  val hasBeenUsed: Boolean
    get() = useCount > 0 && lastUsedTimestampMs > 0L

  val lastUsedFormatted: String
    get() = formatPermissionUsageTimestamp(lastUsedTimestampMs)
}

/**
 * Observable bridge state for Music Player theme and App Permission settings so
 * [com.example.toolbox.ui.theme.ToolboxTheme] and tool screens stay synchronized.
 */
object NavigationPreferencesBridge {
  var musicPlayerThemeEnabled by mutableStateOf(DEFAULT_MUSIC_PLAYER_THEME_ENABLED)
  var musicPlayerColorOption by mutableStateOf(DEFAULT_MUSIC_PLAYER_COLOR_OPTION)
  var permissionGrantStates by mutableStateOf(AppPermission.entries.associateWith { true })

  fun isPermissionGranted(permission: AppPermission): Boolean =
    permissionGrantStates[permission] != false
}

val DEFAULT_HIDDEN_TOOLS: Set<Tool> = setOf(
  Tool.IMAGE_EDITOR,
  Tool.VIDEO_EDITOR,
  Tool.AUDIO_EDITOR,
  Tool.FILE_CONVERTER,
  Tool.PDF_VIEWER,
  Tool.ARCHIVE,
  Tool.DECIBEL_METER,
  Tool.METRONOME,
  Tool.COLOR_PICKER,
  Tool.OBD2_SCANNER,
)

private val LEGACY_DEFAULT_HIDDEN_TOOLS: Set<Tool> = setOf(
  Tool.FILE_CONVERTER,
  Tool.IMAGE_EDITOR,
  Tool.VIDEO_EDITOR,
  Tool.AUDIO_EDITOR,
  Tool.DECIBEL_METER,
)

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
 * When [rawString] is `null` (no saved preference), returns [DEFAULT_HIDDEN_TOOLS].
 * [Tool.SETTINGS] can never be hidden and is always removed.
 */
fun parseHiddenTools(rawString: String?): Set<Tool> {
  if (rawString == null) {
    return DEFAULT_HIDDEN_TOOLS
  }
  if (rawString.isBlank()) {
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
 * Parses a comma-separated `PERMISSION:true/false` string into a map of [AppPermission] grant states.
 * Permissions not present in [rawString] default to `true` (granted).
 */
fun parsePermissionStates(rawString: String?): Map<AppPermission, Boolean> {
  val result = AppPermission.entries.associateWith { true }.toMutableMap()
  if (rawString.isNullOrBlank()) return result
  rawString.split(",").forEach { entry ->
    val parts = entry.split(":")
    if (parts.size == 2) {
      val permName = parts[0].trim()
      val granted = parts[1].trim().equals("true", ignoreCase = true)
      try {
        val perm = AppPermission.valueOf(permName)
        result[perm] = granted
      } catch (_: IllegalArgumentException) {}
    }
  }
  return result
}

/**
 * Serializes a map of [AppPermission] grant states into a comma-separated `PERMISSION:true/false` string.
 */
fun serializePermissionStates(states: Map<AppPermission, Boolean>): String {
  return AppPermission.entries.joinToString(",") { perm ->
    val granted = states[perm] != false
    "${perm.name}:$granted"
  }
}

/**
 * Parses a semicolon-separated `PERMISSION|TOOL|COUNT|TIMESTAMP` string into nested permission usage maps.
 */
fun parsePermissionUsage(
  rawString: String?,
): Map<AppPermission, Map<Tool, PermissionToolUsageStat>> {
  if (rawString.isNullOrBlank()) return emptyMap()
  val result = mutableMapOf<AppPermission, MutableMap<Tool, PermissionToolUsageStat>>()
  rawString.split(";").forEach { entry ->
    val parts = entry.split("|")
    if (parts.size == 4) {
      val permName = parts[0].trim()
      val toolName = parts[1].trim()
      val count = parts[2].trim().toIntOrNull() ?: 0
      val timestamp = parts[3].trim().toLongOrNull() ?: 0L
      if (count > 0) {
        try {
          val perm = AppPermission.valueOf(permName)
          val tool = Tool.valueOf(toolName)
          val toolMap = result.getOrPut(perm) { mutableMapOf() }
          toolMap[tool] = PermissionToolUsageStat(
            permission = perm,
            tool = tool,
            useCount = count,
            lastUsedTimestampMs = timestamp,
          )
        } catch (_: IllegalArgumentException) {}
      }
    }
  }
  return result
}

/**
 * Serializes nested permission usage maps into a semicolon-separated `PERMISSION|TOOL|COUNT|TIMESTAMP` string.
 */
fun serializePermissionUsage(
  stats: Map<AppPermission, Map<Tool, PermissionToolUsageStat>>,
): String {
  val entries = mutableListOf<String>()
  stats.forEach { (perm, toolMap) ->
    toolMap.forEach { (tool, stat) ->
      if (stat.useCount > 0) {
        entries.add("${perm.name}|${tool.name}|${stat.useCount}|${stat.lastUsedTimestampMs}")
      }
    }
  }
  return entries.joinToString(";")
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

  fun getMusicPlayerThemeEnabled(): Boolean = DEFAULT_MUSIC_PLAYER_THEME_ENABLED
  fun setMusicPlayerThemeEnabled(enabled: Boolean) {}
  val musicPlayerThemeEnabledFlow: StateFlow<Boolean>
    get() = MutableStateFlow(getMusicPlayerThemeEnabled()).asStateFlow()

  fun getMusicPlayerColorOption(): String = DEFAULT_MUSIC_PLAYER_COLOR_OPTION
  fun setMusicPlayerColorOption(option: String) {}
  val musicPlayerColorOptionFlow: StateFlow<String>
    get() = MutableStateFlow(getMusicPlayerColorOption()).asStateFlow()

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
  fun setToolUsageCounts(usageCounts: Map<Tool, Int>) {
    resetToolUsage()
    usageCounts.forEach { (tool, count) ->
      repeat(count.coerceAtLeast(0)) {
        incrementToolUsage(tool)
      }
    }
  }
  fun incrementToolUsage(tool: Tool)
  fun resetToolUsage()
  val toolUsageCountsFlow: StateFlow<Map<Tool, Int>>

  fun getPermissionGrantStates(): Map<AppPermission, Boolean> =
    AppPermission.entries.associateWith { true }
  fun isPermissionGranted(permission: AppPermission): Boolean =
    getPermissionGrantStates()[permission] != false
  fun setPermissionGranted(permission: AppPermission, granted: Boolean) {}
  fun grantPermission(permission: AppPermission) = setPermissionGranted(permission, true)
  fun revokePermission(permission: AppPermission) = setPermissionGranted(permission, false)
  fun setPermissionGrantStates(states: Map<AppPermission, Boolean>) {
    AppPermission.entries.forEach { perm ->
      setPermissionGranted(perm, states[perm] != false)
    }
  }
  fun grantAllPermissions() {
    setPermissionGrantStates(AppPermission.entries.associateWith { true })
  }
  fun revokeAllPermissions() {
    setPermissionGrantStates(AppPermission.entries.associateWith { false })
  }
  val permissionGrantStatesFlow: StateFlow<Map<AppPermission, Boolean>>
    get() = MutableStateFlow(getPermissionGrantStates()).asStateFlow()

  fun getPermissionUsageStats(): Map<AppPermission, Map<Tool, PermissionToolUsageStat>> = emptyMap()
  fun setPermissionUsageStats(stats: Map<AppPermission, Map<Tool, PermissionToolUsageStat>>) {}
  fun getPermissionUsageForTool(permission: AppPermission, tool: Tool): PermissionToolUsageStat =
    getPermissionUsageStats()[permission]?.get(tool)
      ?: PermissionToolUsageStat(permission = permission, tool = tool)
  fun getTotalPermissionUsageCount(permission: AppPermission): Int =
    getPermissionUsageStats()[permission]?.values?.sumOf { it.useCount } ?: 0
  fun getLastPermissionUsedTimestamp(permission: AppPermission): Long =
    getPermissionUsageStats()[permission]?.values?.maxOfOrNull { it.lastUsedTimestampMs } ?: 0L
  fun recordPermissionUsage(
    permission: AppPermission,
    tool: Tool,
    timestampMs: Long = System.currentTimeMillis(),
  ) {}
  fun resetPermissionUsage() {}
  val permissionUsageStatsFlow: StateFlow<Map<AppPermission, Map<Tool, PermissionToolUsageStat>>>
    get() = MutableStateFlow(getPermissionUsageStats()).asStateFlow()

  fun getLastOpenedTool(): Tool? = null
  fun setLastOpenedTool(tool: Tool?) {}
  val lastOpenedToolFlow: StateFlow<Tool?>
    get() = MutableStateFlow(getLastOpenedTool()).asStateFlow()
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

  private val _musicPlayerThemeEnabledFlow = MutableStateFlow(getMusicPlayerThemeEnabled())
  override val musicPlayerThemeEnabledFlow: StateFlow<Boolean> = _musicPlayerThemeEnabledFlow.asStateFlow()

  private val _musicPlayerColorOptionFlow = MutableStateFlow(getMusicPlayerColorOption())
  override val musicPlayerColorOptionFlow: StateFlow<String> = _musicPlayerColorOptionFlow.asStateFlow()

  private val _accentColorFlow = MutableStateFlow(getAccentColor())
  override val accentColorFlow: StateFlow<String> = _accentColorFlow.asStateFlow()

  init {
    NavigationPreferencesBridge.musicPlayerThemeEnabled = _musicPlayerThemeEnabledFlow.value
    NavigationPreferencesBridge.musicPlayerColorOption = _musicPlayerColorOptionFlow.value
  }

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

  private val _permissionGrantStatesFlow = MutableStateFlow(getPermissionGrantStates())
  override val permissionGrantStatesFlow: StateFlow<Map<AppPermission, Boolean>> =
    _permissionGrantStatesFlow.asStateFlow()

  private val _permissionUsageStatsFlow = MutableStateFlow(getPermissionUsageStats())
  override val permissionUsageStatsFlow: StateFlow<Map<AppPermission, Map<Tool, PermissionToolUsageStat>>> =
    _permissionUsageStatsFlow.asStateFlow()

  private val _lastOpenedToolFlow = MutableStateFlow(getLastOpenedTool())
  override val lastOpenedToolFlow: StateFlow<Tool?> = _lastOpenedToolFlow.asStateFlow()

  init {
    NavigationPreferencesBridge.permissionGrantStates = _permissionGrantStatesFlow.value
  }

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
    if (
      color == THEME_COLOR_MUSIC_PLAYER ||
      color == THEME_COLOR_MUSIC_SECONDARY ||
      color == THEME_COLOR_MUSIC_ACCENT
    ) {
      setMusicPlayerColorOption(color)
      setMusicPlayerThemeEnabled(true)
    }
  }

  override fun getMusicPlayerThemeEnabled(): Boolean {
    return sharedPreferences.getBoolean(
      PREF_KEY_MUSIC_PLAYER_THEME_ENABLED,
      DEFAULT_MUSIC_PLAYER_THEME_ENABLED
    )
  }

  override fun setMusicPlayerThemeEnabled(enabled: Boolean) {
    sharedPreferences.edit().putBoolean(PREF_KEY_MUSIC_PLAYER_THEME_ENABLED, enabled).apply()
    _musicPlayerThemeEnabledFlow.value = enabled
    NavigationPreferencesBridge.musicPlayerThemeEnabled = enabled
  }

  override fun getMusicPlayerColorOption(): String {
    val raw = sharedPreferences.getString(PREF_KEY_MUSIC_PLAYER_COLOR_OPTION, null)
    if (
      raw == THEME_COLOR_MUSIC_PLAYER ||
      raw == THEME_COLOR_MUSIC_SECONDARY ||
      raw == THEME_COLOR_MUSIC_ACCENT
    ) {
      return raw
    }
    val legacyThemeColor = getThemeColor()
    return if (
      legacyThemeColor == THEME_COLOR_MUSIC_PLAYER ||
      legacyThemeColor == THEME_COLOR_MUSIC_SECONDARY ||
      legacyThemeColor == THEME_COLOR_MUSIC_ACCENT
    ) {
      legacyThemeColor
    } else {
      DEFAULT_MUSIC_PLAYER_COLOR_OPTION
    }
  }

  override fun setMusicPlayerColorOption(option: String) {
    val sanitized = if (
      option == THEME_COLOR_MUSIC_PLAYER ||
      option == THEME_COLOR_MUSIC_SECONDARY ||
      option == THEME_COLOR_MUSIC_ACCENT
    ) {
      option
    } else {
      DEFAULT_MUSIC_PLAYER_COLOR_OPTION
    }
    sharedPreferences.edit().putString(PREF_KEY_MUSIC_PLAYER_COLOR_OPTION, sanitized).apply()
    _musicPlayerColorOptionFlow.value = sanitized
    NavigationPreferencesBridge.musicPlayerColorOption = sanitized
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
    migrateNewDefaultHiddenToolsIfNeeded()
    val raw = sharedPreferences.getString(PREF_KEY_HIDDEN_TOOLS, null)
    return parseHiddenTools(raw)
  }

  override fun setHiddenTools(tools: Set<Tool>) {
    val sanitized = tools.filter { it != Tool.SETTINGS }.toSet()
    val serialized = serializeHiddenTools(sanitized)
    sharedPreferences.edit()
      .putString(PREF_KEY_HIDDEN_TOOLS, serialized)
      .putString(PREF_KEY_KNOWN_DEFAULT_HIDDEN_TOOLS, serializeHiddenTools(DEFAULT_HIDDEN_TOOLS))
      .apply()
    _hiddenToolsFlow.value = sanitized
  }

  private fun migrateNewDefaultHiddenToolsIfNeeded() {
    if (!sharedPreferences.contains(PREF_KEY_HIDDEN_TOOLS)) {
      return
    }
    val knownRaw = sharedPreferences.getString(PREF_KEY_KNOWN_DEFAULT_HIDDEN_TOOLS, null)
    val knownDefaults = if (knownRaw == null) {
      LEGACY_DEFAULT_HIDDEN_TOOLS
    } else {
      parseHiddenTools(knownRaw)
    }
    val newlyAddedDefaultHidden = DEFAULT_HIDDEN_TOOLS - knownDefaults
    if (newlyAddedDefaultHidden.isNotEmpty() || knownRaw == null) {
      val currentHidden = parseHiddenTools(sharedPreferences.getString(PREF_KEY_HIDDEN_TOOLS, null))
      val updatedHidden = (currentHidden + newlyAddedDefaultHidden).filter { it != Tool.SETTINGS }.toSet()
      sharedPreferences.edit()
        .putString(PREF_KEY_HIDDEN_TOOLS, serializeHiddenTools(updatedHidden))
        .putString(PREF_KEY_KNOWN_DEFAULT_HIDDEN_TOOLS, serializeHiddenTools(DEFAULT_HIDDEN_TOOLS))
        .apply()
    }
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

  override fun setToolUsageCounts(usageCounts: Map<Tool, Int>) {
    val sanitized = usageCounts.filter { it.value > 0 }
    if (sanitized.isEmpty()) {
      sharedPreferences.edit().remove(PREF_KEY_TOOL_USAGE).apply()
    } else {
      sharedPreferences.edit().putString(PREF_KEY_TOOL_USAGE, serializeToolUsage(sanitized)).apply()
    }
    _toolUsageCountsFlow.value = sanitized
  }

  override fun incrementToolUsage(tool: Tool) {
    val current = getToolUsageCounts().toMutableMap()
    val updated = (current[tool] ?: 0) + 1
    current[tool] = updated
    sharedPreferences.edit().putString(PREF_KEY_TOOL_USAGE, serializeToolUsage(current)).apply()
    _toolUsageCountsFlow.value = current
    val now = System.currentTimeMillis()
    AppPermission.permissionsForTool(tool).forEach { perm ->
      if (isPermissionGranted(perm)) {
        recordPermissionUsage(perm, tool, now)
      }
    }
  }

  override fun resetToolUsage() {
    sharedPreferences.edit().remove(PREF_KEY_TOOL_USAGE).apply()
    _toolUsageCountsFlow.value = emptyMap()
  }

  override fun getPermissionGrantStates(): Map<AppPermission, Boolean> {
    val raw = sharedPreferences.getString(PREF_KEY_PERMISSION_STATES, null)
    return parsePermissionStates(raw)
  }

  override fun setPermissionGranted(permission: AppPermission, granted: Boolean) {
    val updated = getPermissionGrantStates().toMutableMap()
    updated[permission] = granted
    sharedPreferences.edit()
      .putString(PREF_KEY_PERMISSION_STATES, serializePermissionStates(updated))
      .apply()
    _permissionGrantStatesFlow.value = updated
    NavigationPreferencesBridge.permissionGrantStates = updated
  }

  override fun setPermissionGrantStates(states: Map<AppPermission, Boolean>) {
    val normalized = AppPermission.entries.associateWith { states[it] != false }
    sharedPreferences.edit()
      .putString(PREF_KEY_PERMISSION_STATES, serializePermissionStates(normalized))
      .apply()
    _permissionGrantStatesFlow.value = normalized
    NavigationPreferencesBridge.permissionGrantStates = normalized
  }

  override fun getPermissionUsageStats(): Map<AppPermission, Map<Tool, PermissionToolUsageStat>> {
    val raw = sharedPreferences.getString(PREF_KEY_PERMISSION_USAGE, null)
    return parsePermissionUsage(raw)
  }

  override fun setPermissionUsageStats(stats: Map<AppPermission, Map<Tool, PermissionToolUsageStat>>) {
    val serialized = serializePermissionUsage(stats)
    if (serialized.isEmpty()) {
      sharedPreferences.edit().remove(PREF_KEY_PERMISSION_USAGE).apply()
    } else {
      sharedPreferences.edit().putString(PREF_KEY_PERMISSION_USAGE, serialized).apply()
    }
    _permissionUsageStatsFlow.value = parsePermissionUsage(serialized)
  }

  override fun recordPermissionUsage(
    permission: AppPermission,
    tool: Tool,
    timestampMs: Long,
  ) {
    if (!isPermissionGranted(permission)) return
    val current = getPermissionUsageStats().mapValues { it.value.toMutableMap() }.toMutableMap()
    val toolMap = current.getOrPut(permission) { mutableMapOf() }
    val existing = toolMap[tool]
    val newCount = (existing?.useCount ?: 0) + 1
    val newTimestamp = maxOf(existing?.lastUsedTimestampMs ?: 0L, timestampMs)
    toolMap[tool] = PermissionToolUsageStat(
      permission = permission,
      tool = tool,
      useCount = newCount,
      lastUsedTimestampMs = newTimestamp,
    )
    val ImmutableCopy = current.mapValues { it.value.toMap() }
    sharedPreferences.edit()
      .putString(PREF_KEY_PERMISSION_USAGE, serializePermissionUsage(ImmutableCopy))
      .apply()
    _permissionUsageStatsFlow.value = ImmutableCopy
  }

  override fun resetPermissionUsage() {
    sharedPreferences.edit().remove(PREF_KEY_PERMISSION_USAGE).apply()
    _permissionUsageStatsFlow.value = emptyMap()
  }

  override fun getLastOpenedTool(): Tool? {
    val raw = sharedPreferences.getString(PREF_KEY_LAST_OPENED_TOOL, null)
      ?.trim()
      ?.takeIf { it.isNotEmpty() }
      ?: return null
    return try {
      Tool.valueOf(raw)
    } catch (_: IllegalArgumentException) {
      null
    }
  }

  override fun setLastOpenedTool(tool: Tool?) {
    if (tool == null) {
      sharedPreferences.edit().remove(PREF_KEY_LAST_OPENED_TOOL).apply()
    } else {
      sharedPreferences.edit().putString(PREF_KEY_LAST_OPENED_TOOL, tool.name).apply()
    }
    _lastOpenedToolFlow.value = tool
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
  initialMusicPlayerThemeEnabled: Boolean = DEFAULT_MUSIC_PLAYER_THEME_ENABLED,
  initialMusicPlayerColorOption: String = if (
    initialThemeColor == THEME_COLOR_MUSIC_PLAYER ||
    initialThemeColor == THEME_COLOR_MUSIC_SECONDARY ||
    initialThemeColor == THEME_COLOR_MUSIC_ACCENT
  ) {
    initialThemeColor
  } else {
    DEFAULT_MUSIC_PLAYER_COLOR_OPTION
  },
  initialAccentColor: String = DEFAULT_ACCENT_COLOR_AUTO,
  initialHiddenTools: Set<Tool> = DEFAULT_HIDDEN_TOOLS,
  initialToolSorting: ToolSortingOption = ToolSortingOption.USER_CHOICE,
  initialToolSortOrder: ToolSortOrder = ToolSortOrder.ASCENDING,
  initialCategorySorting: CategorySortingOption = CategorySortingOption.MANUAL,
  initialCategoryToolSorting: CategoryToolSortingOption = CategoryToolSortingOption.MANUAL,
  initialToolUsage: Map<Tool, Int> = emptyMap(),
  initialPermissionStates: Map<AppPermission, Boolean> = AppPermission.entries.associateWith { true },
  initialPermissionUsage: Map<AppPermission, Map<Tool, PermissionToolUsageStat>> = emptyMap(),
  initialLastOpenedTool: Tool? = null,
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

  private val _musicPlayerThemeEnabledFlow = MutableStateFlow(initialMusicPlayerThemeEnabled)
  override val musicPlayerThemeEnabledFlow: StateFlow<Boolean> = _musicPlayerThemeEnabledFlow.asStateFlow()

  private val _musicPlayerColorOptionFlow = MutableStateFlow(initialMusicPlayerColorOption)
  override val musicPlayerColorOptionFlow: StateFlow<String> = _musicPlayerColorOptionFlow.asStateFlow()

  private val _accentColorFlow = MutableStateFlow(initialAccentColor)
  override val accentColorFlow: StateFlow<String> = _accentColorFlow.asStateFlow()

  init {
    NavigationPreferencesBridge.musicPlayerThemeEnabled = _musicPlayerThemeEnabledFlow.value
    NavigationPreferencesBridge.musicPlayerColorOption = _musicPlayerColorOptionFlow.value
  }

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

  private val _permissionGrantStatesFlow = MutableStateFlow(
    AppPermission.entries.associateWith { initialPermissionStates[it] != false }
  )
  override val permissionGrantStatesFlow: StateFlow<Map<AppPermission, Boolean>> =
    _permissionGrantStatesFlow.asStateFlow()

  private val _permissionUsageStatsFlow = MutableStateFlow(initialPermissionUsage)
  override val permissionUsageStatsFlow: StateFlow<Map<AppPermission, Map<Tool, PermissionToolUsageStat>>> =
    _permissionUsageStatsFlow.asStateFlow()

  private val _lastOpenedToolFlow = MutableStateFlow(initialLastOpenedTool)
  override val lastOpenedToolFlow: StateFlow<Tool?> = _lastOpenedToolFlow.asStateFlow()

  init {
    NavigationPreferencesBridge.permissionGrantStates = _permissionGrantStatesFlow.value
  }

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
    if (
      color == THEME_COLOR_MUSIC_PLAYER ||
      color == THEME_COLOR_MUSIC_SECONDARY ||
      color == THEME_COLOR_MUSIC_ACCENT
    ) {
      setMusicPlayerColorOption(color)
      setMusicPlayerThemeEnabled(true)
    }
  }

  override fun getMusicPlayerThemeEnabled(): Boolean = _musicPlayerThemeEnabledFlow.value

  override fun setMusicPlayerThemeEnabled(enabled: Boolean) {
    _musicPlayerThemeEnabledFlow.value = enabled
    NavigationPreferencesBridge.musicPlayerThemeEnabled = enabled
  }

  override fun getMusicPlayerColorOption(): String = _musicPlayerColorOptionFlow.value

  override fun setMusicPlayerColorOption(option: String) {
    val sanitized = if (
      option == THEME_COLOR_MUSIC_PLAYER ||
      option == THEME_COLOR_MUSIC_SECONDARY ||
      option == THEME_COLOR_MUSIC_ACCENT
    ) {
      option
    } else {
      DEFAULT_MUSIC_PLAYER_COLOR_OPTION
    }
    _musicPlayerColorOptionFlow.value = sanitized
    NavigationPreferencesBridge.musicPlayerColorOption = sanitized
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

  override fun setToolUsageCounts(usageCounts: Map<Tool, Int>) {
    _toolUsageCountsFlow.value = usageCounts.filter { it.value > 0 }
  }

  override fun incrementToolUsage(tool: Tool) {
    val current = _toolUsageCountsFlow.value.toMutableMap()
    current[tool] = (current[tool] ?: 0) + 1
    _toolUsageCountsFlow.value = current
    val now = System.currentTimeMillis()
    AppPermission.permissionsForTool(tool).forEach { perm ->
      if (isPermissionGranted(perm)) {
        recordPermissionUsage(perm, tool, now)
      }
    }
  }

  override fun resetToolUsage() {
    _toolUsageCountsFlow.value = emptyMap()
  }

  override fun getPermissionGrantStates(): Map<AppPermission, Boolean> =
    _permissionGrantStatesFlow.value

  override fun setPermissionGranted(permission: AppPermission, granted: Boolean) {
    val updated = _permissionGrantStatesFlow.value.toMutableMap()
    updated[permission] = granted
    _permissionGrantStatesFlow.value = updated
    NavigationPreferencesBridge.permissionGrantStates = updated
  }

  override fun setPermissionGrantStates(states: Map<AppPermission, Boolean>) {
    val normalized = AppPermission.entries.associateWith { states[it] != false }
    _permissionGrantStatesFlow.value = normalized
    NavigationPreferencesBridge.permissionGrantStates = normalized
  }

  override fun getPermissionUsageStats(): Map<AppPermission, Map<Tool, PermissionToolUsageStat>> =
    _permissionUsageStatsFlow.value

  override fun setPermissionUsageStats(stats: Map<AppPermission, Map<Tool, PermissionToolUsageStat>>) {
    _permissionUsageStatsFlow.value = stats
  }

  override fun recordPermissionUsage(
    permission: AppPermission,
    tool: Tool,
    timestampMs: Long,
  ) {
    if (!isPermissionGranted(permission)) return
    val current = _permissionUsageStatsFlow.value.mapValues { it.value.toMutableMap() }.toMutableMap()
    val toolMap = current.getOrPut(permission) { mutableMapOf() }
    val existing = toolMap[tool]
    val newCount = (existing?.useCount ?: 0) + 1
    val newTimestamp = maxOf(existing?.lastUsedTimestampMs ?: 0L, timestampMs)
    toolMap[tool] = PermissionToolUsageStat(
      permission = permission,
      tool = tool,
      useCount = newCount,
      lastUsedTimestampMs = newTimestamp,
    )
    _permissionUsageStatsFlow.value = current.mapValues { it.value.toMap() }
  }

  override fun resetPermissionUsage() {
    _permissionUsageStatsFlow.value = emptyMap()
  }

  override fun getLastOpenedTool(): Tool? = _lastOpenedToolFlow.value

  override fun setLastOpenedTool(tool: Tool?) {
    _lastOpenedToolFlow.value = tool
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

const val SETTINGS_BACKUP_FORMAT = "toolbox_settings_v1"
const val DEFAULT_SETTINGS_BACKUP_FILENAME = "toolbox_settings_backup.json"

private val RECOGNIZED_BACKUP_KEYS = setOf(
  "format",
  PREF_KEY_TOOLS_PER_ROW,
  PREF_KEY_TOOL_ORDER,
  PREF_KEY_THEME_MODE,
  PREF_KEY_THEME_COLOR,
  PREF_KEY_MUSIC_PLAYER_THEME_ENABLED,
  PREF_KEY_MUSIC_PLAYER_COLOR_OPTION,
  PREF_KEY_ACCENT_COLOR,
  PREF_KEY_HIDDEN_TOOLS,
  PREF_KEY_TOOL_SORTING,
  PREF_KEY_TOOL_SORT_ORDER,
  PREF_KEY_CATEGORY_SORTING,
  PREF_KEY_CATEGORY_TOOL_SORTING,
  PREF_KEY_TOOL_USAGE,
  PREF_KEY_PERMISSION_STATES,
  PREF_KEY_PERMISSION_USAGE,
  PREF_KEY_LAST_OPENED_TOOL,
)

/**
 * Exports all settings in [repository] to a formatted JSON string suitable for saving to a backup file.
 */
fun exportSettingsToJson(repository: NavigationPreferencesRepository): String {
  val json = JSONObject()
  json.put("format", SETTINGS_BACKUP_FORMAT)
  json.put(PREF_KEY_TOOLS_PER_ROW, repository.getToolsPerRow())
  json.put(PREF_KEY_TOOL_ORDER, serializeToolOrder(repository.getToolOrder()))
  json.put(PREF_KEY_THEME_MODE, repository.getThemeMode().name)
  json.put(PREF_KEY_THEME_COLOR, repository.getThemeColor())
  json.put(PREF_KEY_MUSIC_PLAYER_THEME_ENABLED, repository.getMusicPlayerThemeEnabled())
  json.put(PREF_KEY_MUSIC_PLAYER_COLOR_OPTION, repository.getMusicPlayerColorOption())
  json.put(PREF_KEY_ACCENT_COLOR, repository.getAccentColor())
  json.put(PREF_KEY_HIDDEN_TOOLS, serializeHiddenTools(repository.getHiddenTools()))
  json.put(PREF_KEY_TOOL_SORTING, repository.getToolSorting().name)
  json.put(PREF_KEY_TOOL_SORT_ORDER, repository.getToolSortOrder().name)
  json.put(PREF_KEY_CATEGORY_SORTING, repository.getCategorySorting().name)
  json.put(PREF_KEY_CATEGORY_TOOL_SORTING, repository.getCategoryToolSorting().name)
  json.put(PREF_KEY_TOOL_USAGE, serializeToolUsage(repository.getToolUsageCounts()))
  json.put(PREF_KEY_PERMISSION_STATES, serializePermissionStates(repository.getPermissionGrantStates()))
  json.put(PREF_KEY_PERMISSION_USAGE, serializePermissionUsage(repository.getPermissionUsageStats()))
  repository.getLastOpenedTool()?.let { lastTool ->
    json.put(PREF_KEY_LAST_OPENED_TOOL, lastTool.name)
  }
  return json.toString(2)
}

/**
 * Imports and applies settings from [jsonString] into [repository].
 * Returns `true` if the JSON was valid and contained recognized settings, or `false` otherwise.
 */
fun importSettingsFromJson(
  jsonString: String,
  repository: NavigationPreferencesRepository,
): Boolean {
  val trimmed = jsonString.trim()
  if (trimmed.isEmpty()) return false
  return try {
    val json = JSONObject(trimmed)
    val hasRecognizedKey = RECOGNIZED_BACKUP_KEYS.any { json.has(it) }
    if (!hasRecognizedKey) return false

    if (json.has(PREF_KEY_TOOLS_PER_ROW)) {
      repository.setToolsPerRow(coerceToolsPerRow(json.optInt(PREF_KEY_TOOLS_PER_ROW, DEFAULT_TOOLS_PER_ROW)))
    }
    if (json.has(PREF_KEY_TOOL_ORDER)) {
      repository.setToolOrder(parseToolOrder(json.optString(PREF_KEY_TOOL_ORDER, "")))
    }
    if (json.has(PREF_KEY_THEME_MODE)) {
      val modeName = json.optString(PREF_KEY_THEME_MODE, ThemeMode.SYSTEM.name)
      val mode = try {
        ThemeMode.valueOf(modeName)
      } catch (_: IllegalArgumentException) {
        ThemeMode.SYSTEM
      }
      repository.setThemeMode(mode)
    }
    if (json.has(PREF_KEY_THEME_COLOR)) {
      val color = json.optString(PREF_KEY_THEME_COLOR, DEFAULT_THEME_COLOR_SYSTEM)
        .ifBlank { DEFAULT_THEME_COLOR_SYSTEM }
      repository.setThemeColor(color)
    }
    if (json.has(PREF_KEY_MUSIC_PLAYER_THEME_ENABLED)) {
      repository.setMusicPlayerThemeEnabled(
        json.optBoolean(PREF_KEY_MUSIC_PLAYER_THEME_ENABLED, DEFAULT_MUSIC_PLAYER_THEME_ENABLED)
      )
    }
    if (json.has(PREF_KEY_MUSIC_PLAYER_COLOR_OPTION)) {
      val option = json.optString(
        PREF_KEY_MUSIC_PLAYER_COLOR_OPTION,
        DEFAULT_MUSIC_PLAYER_COLOR_OPTION
      ).ifBlank { DEFAULT_MUSIC_PLAYER_COLOR_OPTION }
      repository.setMusicPlayerColorOption(option)
    }
    if (json.has(PREF_KEY_ACCENT_COLOR)) {
      val accent = json.optString(PREF_KEY_ACCENT_COLOR, DEFAULT_ACCENT_COLOR_AUTO)
        .ifBlank { DEFAULT_ACCENT_COLOR_AUTO }
      repository.setAccentColor(accent)
    }
    if (json.has(PREF_KEY_HIDDEN_TOOLS)) {
      repository.setHiddenTools(parseHiddenTools(json.optString(PREF_KEY_HIDDEN_TOOLS, "")))
    }
    if (json.has(PREF_KEY_TOOL_SORTING)) {
      val sortingName = json.optString(PREF_KEY_TOOL_SORTING, ToolSortingOption.USER_CHOICE.name)
      val sorting = try {
        ToolSortingOption.valueOf(sortingName)
      } catch (_: IllegalArgumentException) {
        ToolSortingOption.USER_CHOICE
      }
      repository.setToolSorting(sorting)
    }
    if (json.has(PREF_KEY_TOOL_SORT_ORDER)) {
      val orderName = json.optString(PREF_KEY_TOOL_SORT_ORDER, ToolSortOrder.ASCENDING.name)
      val order = try {
        ToolSortOrder.valueOf(orderName)
      } catch (_: IllegalArgumentException) {
        ToolSortOrder.ASCENDING
      }
      repository.setToolSortOrder(order)
    }
    if (json.has(PREF_KEY_CATEGORY_SORTING)) {
      val catSortName = json.optString(PREF_KEY_CATEGORY_SORTING, CategorySortingOption.MANUAL.name)
      val catSort = try {
        CategorySortingOption.valueOf(catSortName)
      } catch (_: IllegalArgumentException) {
        CategorySortingOption.MANUAL
      }
      repository.setCategorySorting(catSort)
    }
    if (json.has(PREF_KEY_CATEGORY_TOOL_SORTING)) {
      val catToolSortName = json.optString(PREF_KEY_CATEGORY_TOOL_SORTING, CategoryToolSortingOption.MANUAL.name)
      val catToolSort = try {
        CategoryToolSortingOption.valueOf(catToolSortName)
      } catch (_: IllegalArgumentException) {
        CategoryToolSortingOption.MANUAL
      }
      repository.setCategoryToolSorting(catToolSort)
    }
    if (json.has(PREF_KEY_TOOL_USAGE)) {
      repository.setToolUsageCounts(parseToolUsage(json.optString(PREF_KEY_TOOL_USAGE, "")))
    }
    if (json.has(PREF_KEY_PERMISSION_STATES)) {
      repository.setPermissionGrantStates(
        parsePermissionStates(json.optString(PREF_KEY_PERMISSION_STATES, ""))
      )
    }
    if (json.has(PREF_KEY_PERMISSION_USAGE)) {
      repository.setPermissionUsageStats(
        parsePermissionUsage(json.optString(PREF_KEY_PERMISSION_USAGE, ""))
      )
    }
    if (json.has(PREF_KEY_LAST_OPENED_TOOL)) {
      val toolName = json.optString(PREF_KEY_LAST_OPENED_TOOL, "").trim()
      val parsedTool = if (toolName.isEmpty()) {
        null
      } else {
        try {
          Tool.valueOf(toolName)
        } catch (_: IllegalArgumentException) {
          null
        }
      }
      repository.setLastOpenedTool(parsedTool)
    }
    true
  } catch (_: Exception) {
    false
  }
}

/**
 * Resets all settings in [repository] back to their default values.
 */
fun resetAllSettingsToDefaults(repository: NavigationPreferencesRepository) {
  repository.setToolsPerRow(DEFAULT_TOOLS_PER_ROW)
  repository.setThemeMode(ThemeMode.SYSTEM)
  repository.setThemeColor(DEFAULT_THEME_COLOR_SYSTEM)
  repository.setMusicPlayerThemeEnabled(DEFAULT_MUSIC_PLAYER_THEME_ENABLED)
  repository.setMusicPlayerColorOption(DEFAULT_MUSIC_PLAYER_COLOR_OPTION)
  repository.setAccentColor(DEFAULT_ACCENT_COLOR_AUTO)
  repository.setHiddenTools(DEFAULT_HIDDEN_TOOLS)
  repository.setToolOrder(Tool.entries)
  repository.setToolSorting(ToolSortingOption.USER_CHOICE)
  repository.setToolSortOrder(ToolSortOrder.ASCENDING)
  repository.setCategorySorting(CategorySortingOption.MANUAL)
  repository.setCategoryToolSorting(CategoryToolSortingOption.MANUAL)
  repository.resetToolUsage()
  repository.grantAllPermissions()
  repository.resetPermissionUsage()
  repository.setLastOpenedTool(null)
}
