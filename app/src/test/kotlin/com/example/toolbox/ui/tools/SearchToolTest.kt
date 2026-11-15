package com.example.toolbox.ui.tools

import com.example.toolbox.ui.main.Tool
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchToolTest {

  @Test
  fun toolMetadata_coversEverySingleToolInToolbox() {
    for (tool in Tool.entries) {
      val metadata = SearchEngine.TOOL_METADATA[tool]
      assertNotNull("Missing ToolSearchMetadata for ${tool.name}", metadata)
      assertTrue(metadata!!.summary.isNotBlank())
      assertTrue(metadata.description.isNotBlank())
      assertTrue(metadata.capabilities.isNotEmpty())
      assertTrue(metadata.keywords.isNotEmpty())
    }
  }

  @Test
  fun search_findsAllToolsByTitleAndKeywords() {
    for (tool in Tool.entries) {
      val results = SearchEngine.search(query = tool.title)
      assertTrue(
        "Searching '${tool.title}' should surface Tool.${tool.name}",
        results.any { it.resultType == SearchResultType.TOOL && it.tool == tool },
      )
    }

    // Search by capability keyword
    val qrResults = SearchEngine.search("QR barcode")
    assertTrue(qrResults.any { it.tool == Tool.CAMERA })

    val zipResults = SearchEngine.search("tar.gz extract")
    assertTrue(zipResults.any { it.tool == Tool.ARCHIVE })
  }

  @Test
  fun search_evaluatesLiveMathExpressions() {
    val results = SearchEngine.search("24 * 15 + 40")
    val calcResult = results.firstOrNull { it.resultType == SearchResultType.LIVE_CALCULATION }
    assertNotNull(calcResult)
    assertEquals(Tool.CALCULATOR, calcResult!!.tool)
    assertEquals("400", calcResult.copyableValue)
    assertTrue(calcResult.title.contains("400"))
  }

  @Test
  fun search_evaluatesLiveUnitConversions() {
    val explicitResults = SearchEngine.search("100 km to mi")
    val conversion = explicitResults.firstOrNull { it.resultType == SearchResultType.LIVE_CONVERSION }
    assertNotNull(conversion)
    assertEquals(Tool.UNIT_CONVERTER, conversion!!.tool)
    assertTrue(conversion.title.contains("62.1371"))

    val tempResults = SearchEngine.search("100 c to f")
    val tempConversion = tempResults.firstOrNull { it.resultType == SearchResultType.LIVE_CONVERSION }
    assertNotNull(tempConversion)
    assertTrue(tempConversion!!.title.contains("212"))

    val singleUnitResults = SearchEngine.search("50 kg")
    assertTrue(singleUnitResults.any { it.resultType == SearchResultType.LIVE_CONVERSION && it.tool == Tool.UNIT_CONVERTER })
  }

  @Test
  fun search_evaluatesLiveHexColorsAndSwatches() {
    val hexResults = SearchEngine.search("#8E66A6")
    val liveColor = hexResults.firstOrNull { it.resultType == SearchResultType.LIVE_COLOR }
    assertNotNull(liveColor)
    assertEquals(Tool.COLOR_PICKER, liveColor!!.tool)
    assertEquals("#8E66A6", liveColor.colorHex)

    val swatchResults = SearchEngine.search("Emerald")
    assertTrue(swatchResults.any { it.tool == Tool.COLOR_PICKER && it.resultType == SearchResultType.COLOR_SWATCH })
  }

  @Test
  fun search_evaluatesLiveDiceNotationAndChordVoicings() {
    val diceResults = SearchEngine.search("2d20+5")
    val diceItem = diceResults.firstOrNull { it.resultType == SearchResultType.LIVE_DICE_ROLL }
    assertNotNull(diceItem)
    assertEquals(Tool.DICE_ROLLER, diceItem!!.tool)
    assertTrue(diceItem.title.contains("7–45"))

    val chordResults = SearchEngine.search("Cmaj7")
    val chordItem = chordResults.firstOrNull { it.resultType == SearchResultType.CHORD && it.tool == Tool.CHORD_BOOK }
    assertNotNull(chordItem)
    assertTrue(chordItem!!.subtitle.contains("C – E – G – B"))
  }

  @Test
  fun search_searchesUserNotesAndTodoTasks() {
    val notesRepo = InMemoryNotesRepository(
      initialNotes = listOf(
        NoteItem(
          id = "note-101",
          title = "Grocery Shopping List",
          content = "Buy organic avocados and sourdough bread",
          color = NoteColor.MINT,
          isPinned = true,
          labels = listOf("errands"),
        )
      )
    )
    val todoRepo = InMemoryTodoListRepository(
      initialTasks = listOf(
        TodoItem(
          id = "todo-202",
          title = "Renew passport application",
          notes = "Bring two passport photos",
          subtasks = listOf(TodoSubTask(id = "sub-1", title = "Fill form DS-82")),
        )
      )
    )
    val dataSources = SearchDataSources(
      notesRepository = notesRepo,
      todoListRepository = todoRepo,
    )

    val noteResults = SearchEngine.search("sourdough", dataSources = dataSources)
    assertTrue(noteResults.any { it.tool == Tool.NOTES && it.resultType == SearchResultType.NOTE && it.title == "Grocery Shopping List" })

    val todoResults = SearchEngine.search("DS-82", dataSources = dataSources)
    assertTrue(todoResults.any { it.tool == Tool.TODO_LIST && it.resultType == SearchResultType.TODO_TASK && it.title == "Renew passport application" })
  }

  @Test
  fun search_searchesRecipesPlantsObd2AndFileFormats() {
    val recipeResults = SearchEngine.search("Pancakes")
    assertTrue(recipeResults.any { it.tool == Tool.RECIPES && it.resultType == SearchResultType.RECIPE })

    val plantResults = SearchEngine.search("Tomato")
    assertTrue(plantResults.any { it.tool == Tool.PLANT_SCHEDULE && it.resultType == SearchResultType.PLANT })

    val obdResults = SearchEngine.search("P0420")
    assertTrue(obdResults.any { it.tool == Tool.OBD2_SCANNER && it.resultType == SearchResultType.OBD2_DTC })

    // Dynamic SAE J2012 code not in static list
    val dynamicObdResults = SearchEngine.search("P0456")
    assertTrue(dynamicObdResults.any { it.tool == Tool.OBD2_SCANNER && it.resultType == SearchResultType.OBD2_DTC })

    val formatResults = SearchEngine.search("FLAC")
    assertTrue(formatResults.any { it.tool == Tool.FILE_CONVERTER && it.resultType == SearchResultType.FILE_FORMAT })
  }

  @Test
  fun search_respectsCategoryFilterAndToolFilter() {
    val allResults = SearchEngine.search("c")
    assertTrue(allResults.isNotEmpty())

    val toolsOnly = SearchEngine.search(
      query = "c",
      categoryFilter = SearchCategoryFilter.TOOLS,
    )
    assertTrue(toolsOnly.isNotEmpty())
    assertTrue(toolsOnly.all { it.resultType == SearchResultType.TOOL })

    val chordBookOnly = SearchEngine.search(
      query = "Dorian",
      toolFilter = Tool.CHORD_BOOK,
    )
    assertTrue(chordBookOnly.isNotEmpty())
    assertTrue(chordBookOnly.all { it.tool == Tool.CHORD_BOOK })
  }

  @Test
  fun searchPreferencesRepository_managesRecentSearchesAndPinnedTools() {
    val repo = InMemorySearchPreferencesRepository()
    assertTrue(repo.getRecentSearches().isEmpty())

    repo.addRecentSearch("P0420")
    repo.addRecentSearch("100 km to mi")
    repo.addRecentSearch("p0420") // deduplicated & moved to top
    assertEquals(listOf("p0420", "100 km to mi"), repo.getRecentSearches())

    repo.removeRecentSearch("P0420")
    assertEquals(listOf("100 km to mi"), repo.getRecentSearches())

    repo.clearRecentSearches()
    assertTrue(repo.getRecentSearches().isEmpty())

    val initialPinned = repo.getPinnedTools()
    assertTrue(Tool.CALCULATOR in initialPinned)
    val afterUnpin = repo.togglePinnedTool(Tool.CALCULATOR)
    assertFalse(Tool.CALCULATOR in afterUnpin)
    val afterRepin = repo.togglePinnedTool(Tool.CALCULATOR)
    assertTrue(Tool.CALCULATOR in afterRepin)
  }
}
