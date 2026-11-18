package com.example.toolbox.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.ui.graphics.Color
import com.example.toolbox.R
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
    assertTrue((STANDALONE_SHORTCUT_INTENT_FLAGS and android.content.Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
    assertTrue((STANDALONE_SHORTCUT_INTENT_FLAGS and android.content.Intent.FLAG_ACTIVITY_NEW_DOCUMENT) != 0)
    assertTrue((STANDALONE_SHORTCUT_INTENT_FLAGS and android.content.Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS) != 0)
    assertEquals(0, STANDALONE_SHORTCUT_INTENT_FLAGS and android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
    assertEquals(0, STANDALONE_SHORTCUT_INTENT_FLAGS and android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
  }

  @Test
  fun normalizeShortcutFolderPath_andIsSameStandaloneTarget_distinguishesAndMatchesFolderLocations() {
    assertNull(normalizeShortcutFolderPath(null))
    assertNull(normalizeShortcutFolderPath("   "))
    assertEquals(
      "/storage/emulated/0/Download",
      normalizeShortcutFolderPath("/storage/emulated/0/Download/"),
    )
    assertEquals(
      "/storage/emulated/0/Download",
      normalizeShortcutFolderPath("/storage/emulated/0/Pictures/../Download"),
    )

    // Same folder location matches even with trailing slash or missing explicit Tool.FILES
    assertTrue(
      isSameStandaloneTarget(
        firstTool = Tool.FILES,
        firstFolder = "/storage/emulated/0/Download",
        secondTool = null,
        secondFolder = "/storage/emulated/0/Download/",
      )
    )

    // Different folder locations are distinct app instance targets
    junit.framework.TestCase.assertFalse(
      isSameStandaloneTarget(
        firstTool = Tool.FILES,
        firstFolder = "/storage/emulated/0/Download",
        secondTool = Tool.FILES,
        secondFolder = "/storage/emulated/0/Pictures",
      )
    )

    // Folder shortcut is distinct from standalone Files tool without a folder
    junit.framework.TestCase.assertFalse(
      isSameStandaloneTarget(
        firstTool = Tool.FILES,
        firstFolder = "/storage/emulated/0/Download",
        secondTool = Tool.FILES,
        secondFolder = null,
      )
    )
  }

  @Test
  fun resolveShortcutBackgroundColor_defaultsToFallbackPurpleWhenContextIsNull() {
    assertEquals(Color(0xFF8E66A6), resolveShortcutBackgroundColor())
    assertEquals(Color(0xFF8E66A6), resolveShortcutBackgroundColor(null))
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
  fun allTools_haveValidDistinctVectorIconsAndAdaptiveMipmapResources() {
    val icons = Tool.entries.map { it.icon }
    assertEquals(Tool.entries.size, icons.distinct().size)

    val mipmapResIds = Tool.entries.map { resolveToolShortcutMipmapResId(it) }
    assertEquals(Tool.entries.size, mipmapResIds.distinct().size)

    for (tool in Tool.entries) {
      assertNotNull(tool.icon)
      assertTrue(tool.icon.viewportWidth > 0f)
      assertTrue(tool.icon.viewportHeight > 0f)
      assertTrue(resolveToolShortcutMipmapResId(tool) != 0)
    }
  }

  @Test
  fun folderShortcutIcon_matchesAppFolderIconAndFilesMipmapResource() {
    assertEquals(Icons.Default.Folder, FOLDER_SHORTCUT_ICON)
    assertEquals(Tool.FILES.icon, FOLDER_SHORTCUT_ICON)
    assertEquals(R.mipmap.ic_tool_files, resolveToolShortcutMipmapResId(Tool.FILES))
    assertTrue(FOLDER_SHORTCUT_ICON.viewportWidth > 0f)
    assertTrue(FOLDER_SHORTCUT_ICON.viewportHeight > 0f)
  }

  @Test
  fun resolveToolShortcutAliasClassName_distinctAndRoundTripsForAllTools() {
    val aliases = Tool.entries.map { resolveToolShortcutAliasClassName(it) }
    assertEquals(Tool.entries.size, aliases.distinct().size)

    for (tool in Tool.entries) {
      val alias = resolveToolShortcutAliasClassName(tool)
      assertTrue(alias.startsWith("com.example.toolbox.MainActivityTool"))
      assertEquals(tool, parseStandaloneToolFromComponent(alias))
      val shortAlias = ".${alias.substringAfterLast('.')}"
      assertEquals(tool, parseStandaloneToolFromComponent(shortAlias))
    }

    assertNull(parseStandaloneToolFromComponent(null))
    assertNull(parseStandaloneToolFromComponent(""))
    assertNull(parseStandaloneToolFromComponent("com.example.toolbox.MainActivity"))
  }

  @Test
  fun resolveStandaloneTaskMetadata_matchesToolAndFolderShortcuts() {
    // Main app launch (not a shortcut) returns null so default app task description is preserved
    assertNull(resolveStandaloneTaskLabel(standaloneTool = null, initialFolder = null))
    assertNull(resolveStandaloneTaskIconVector(standaloneTool = null, initialFolder = null))

    // Each standalone tool uses its own title and icon vector for Recent Apps
    for (tool in Tool.entries) {
      assertEquals(tool.title, resolveStandaloneTaskLabel(standaloneTool = tool, initialFolder = null))
      assertEquals(tool.icon, resolveStandaloneTaskIconVector(standaloneTool = tool, initialFolder = null))
    }

    // Folder shortcut uses the folder name and folder icon vector
    assertEquals(
      "Download",
      resolveStandaloneTaskLabel(standaloneTool = Tool.FILES, initialFolder = "/storage/emulated/0/Download"),
    )
    assertEquals(
      FOLDER_SHORTCUT_ICON,
      resolveStandaloneTaskIconVector(standaloneTool = Tool.FILES, initialFolder = "/storage/emulated/0/Download"),
    )
  }
}
