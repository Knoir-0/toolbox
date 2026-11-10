package com.example.toolbox.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.ui.graphics.Color
import com.example.toolbox.ui.tools.PRESET_COLORS
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class HomeScreenShortcutsTest {

  @Test
  fun parseStandaloneToolName_validTool_returnsTool() {
    assertEquals(Tool.FILES, parseStandaloneToolName("FILES"))
    assertEquals(Tool.DICE_ROLLER, parseStandaloneToolName("DICE_ROLLER"))
    assertEquals(Tool.SETTINGS, parseStandaloneToolName("SETTINGS"))
  }

  @Test
  fun parseStandaloneToolName_invalidOrNull_returnsNull() {
    assertNull(parseStandaloneToolName(null))
    assertNull(parseStandaloneToolName(""))
    assertNull(parseStandaloneToolName("INVALID_TOOL"))
  }

  @Test
  fun constants_haveExpectedValues() {
    assertEquals("extra_standalone_tool", EXTRA_STANDALONE_TOOL)
    assertEquals("extra_folder_path", EXTRA_FOLDER_PATH)
  }

  @Test
  fun resolveShortcutBackgroundColor_matchesPresetAndCustomThemeColors() {
    for (preset in PRESET_COLORS) {
      assertEquals(preset.color, resolveShortcutBackgroundColor(preset.hex))
      assertEquals(preset.color, resolveShortcutBackgroundColor(preset.hex.lowercase()))
    }

    // Custom hex color
    assertEquals(Color(0xFFFF5722), resolveShortcutBackgroundColor("#FF5722"))
    assertEquals(Color(0xFF123456), resolveShortcutBackgroundColor("123456"))

    // System or invalid falls back to Toolbox Purple (#8E66A6) when context is null
    assertEquals(Color(0xFF8E66A6), resolveShortcutBackgroundColor(DEFAULT_THEME_COLOR_SYSTEM))
    assertEquals(Color(0xFF8E66A6), resolveShortcutBackgroundColor(null))
    assertEquals(Color(0xFF8E66A6), resolveShortcutBackgroundColor("invalid"))
  }

  @Test
  fun resolveShortcutForegroundColor_contrastsWithBackground() {
    // Dark / medium theme colors use white foreground
    for (preset in PRESET_COLORS) {
      assertEquals(Color.White, resolveShortcutForegroundColor(preset.color))
    }
    assertEquals(Color.White, resolveShortcutForegroundColor(Color(0xFF000000)))

    // Very light custom theme colors use black foreground
    assertEquals(Color.Black, resolveShortcutForegroundColor(Color(0xFFFFFFFF)))
    assertEquals(Color.Black, resolveShortcutForegroundColor(Color(0xFFFFFF00)))
  }

  @Test
  fun allTools_haveValidDistinctVectorIconsForShortcuts() {
    val icons = Tool.entries.map { it.icon }
    assertEquals(Tool.entries.size, icons.distinct().size)
    for (tool in Tool.entries) {
      assertNotNull(tool.icon)
      assertTrue(tool.icon.viewportWidth > 0f)
      assertTrue(tool.icon.viewportHeight > 0f)
    }
  }

  @Test
  fun resolveLauncherIconVariant_matchesPresetsAndClosestCustomColor() {
    assertEquals(LauncherIconVariant.SYSTEM, resolveLauncherIconVariant(DEFAULT_THEME_COLOR_SYSTEM))
    assertEquals(LauncherIconVariant.SYSTEM, resolveLauncherIconVariant(null))
    assertEquals(LauncherIconVariant.SYSTEM, resolveLauncherIconVariant(""))
    assertEquals(LauncherIconVariant.SYSTEM, resolveLauncherIconVariant("invalid"))

    assertEquals(LauncherIconVariant.PURPLE, resolveLauncherIconVariant("#8E66A6"))
    assertEquals(LauncherIconVariant.OCEAN_BLUE, resolveLauncherIconVariant("#00668B"))
    assertEquals(LauncherIconVariant.FOREST_GREEN, resolveLauncherIconVariant("#386A20"))
    assertEquals(LauncherIconVariant.AMBER_ORANGE, resolveLauncherIconVariant("#E65100"))
    assertEquals(LauncherIconVariant.CRIMSON_RED, resolveLauncherIconVariant("#B3261E"))
    assertEquals(LauncherIconVariant.TEAL, resolveLauncherIconVariant("#006A60"))
    assertEquals(LauncherIconVariant.ROSE_PINK, resolveLauncherIconVariant("#984061"))
    assertEquals(LauncherIconVariant.INDIGO, resolveLauncherIconVariant("#3F51B5"))

    // Custom hex colors map to the nearest themed launcher icon variant
    assertEquals(LauncherIconVariant.CRIMSON_RED, resolveLauncherIconVariant("#CC1111"))
    assertEquals(LauncherIconVariant.FOREST_GREEN, resolveLauncherIconVariant("#2E7D32"))
  }

  @Test
  fun folderShortcutIcon_matchesAppFolderIconAndThemesBackground() {
    assertEquals(Icons.Default.Folder, FOLDER_SHORTCUT_ICON)
    assertEquals(Tool.FILES.icon, FOLDER_SHORTCUT_ICON)
    assertTrue(FOLDER_SHORTCUT_ICON.viewportWidth > 0f)
    assertTrue(FOLDER_SHORTCUT_ICON.viewportHeight > 0f)

    // Verify background color for folder shortcuts follows themeColor
    assertEquals(Color(0xFF00668B), resolveShortcutBackgroundColor("#00668B"))
    assertEquals(Color(0xFF386A20), resolveShortcutBackgroundColor("#386A20"))
    assertEquals(Color(0xFFE65100), resolveShortcutBackgroundColor("#E65100"))
  }
}
