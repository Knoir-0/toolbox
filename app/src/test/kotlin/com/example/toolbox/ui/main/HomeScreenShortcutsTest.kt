package com.example.toolbox.ui.main

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
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
}
