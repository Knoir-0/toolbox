package com.example.toolbox.ui.tools

import com.example.toolbox.ui.main.DEFAULT_HIDDEN_TOOLS
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.ToolCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ElementsEngineTest {

  @Test
  fun toolRegistration_elementsIsCalledElementsAndHiddenByDefault() {
    assertEquals("Elements", Tool.ELEMENTS.title)
    assertEquals(ToolCategory.MATH_AND_MEASUREMENT, Tool.ELEMENTS.category)
    assertTrue("Elements must be hidden by default", Tool.ELEMENTS in DEFAULT_HIDDEN_TOOLS)
  }

  @Test
  fun periodicTable_containsAll118ElementsInSequentialOrderWithDistinctSymbolsAndGridCells() {
    val all = PeriodicTableEngine.ALL_ELEMENTS
    assertEquals(118, all.size)

    // Atomic numbers 1..118 sequential
    assertEquals((1..118).toList(), all.map { it.atomicNumber })

    // All symbols and names are non-blank and unique
    assertEquals(118, all.map { it.symbol }.distinct().size)
    assertEquals(118, all.map { it.name }.distinct().size)

    // Grid coordinates (row, col) are unique and within bounds
    val cells = all.map { it.gridRow to it.gridCol }
    assertEquals(118, cells.distinct().size)

    for (el in all) {
      assertTrue("Invalid period for ${el.symbol}", el.period in 1..7)
      assertTrue("Invalid gridCol for ${el.symbol}", el.gridCol in 1..18)
      assertTrue("Invalid gridRow for ${el.symbol}", el.gridRow in 1..7 || el.gridRow in 9..10)
      assertTrue("Invalid atomicMass for ${el.symbol}", el.atomicMass > 0.0)
      assertEquals(
        "Electron shells sum for ${el.symbol} (${el.electronShells}) must equal atomicNumber ${el.atomicNumber}",
        el.atomicNumber,
        el.electronShells.sum(),
      )
      assertEquals(el, PeriodicTableEngine.byAtomicNumber(el.atomicNumber))
      assertEquals(el, PeriodicTableEngine.bySymbol(el.symbol))
      assertEquals(el, PeriodicTableEngine.bySymbol(el.symbol.lowercase()))
      assertEquals(el, PeriodicTableEngine.byGridPosition(el.gridRow, el.gridCol))
    }
  }

  @Test
  fun phaseAtTemperature_computesSolidLiquidAndGasCorrectlyAcrossTemperatureSpectrum() {
    val mercury = PeriodicTableEngine.bySymbol("Hg")!!
    val bromine = PeriodicTableEngine.bySymbol("Br")!!
    val gallium = PeriodicTableEngine.bySymbol("Ga")!!
    val tungsten = PeriodicTableEngine.bySymbol("W")!!
    val oxygen = PeriodicTableEngine.bySymbol("O")!!

    // At STP (298.15 K): Hg and Br are Liquid, Ga and W are Solid, O is Gas
    assertEquals(MatterPhase.LIQUID, mercury.phaseAtTemperature(PeriodicTableEngine.STP_TEMPERATURE_K))
    assertEquals(MatterPhase.LIQUID, bromine.phaseAtTemperature(PeriodicTableEngine.STP_TEMPERATURE_K))
    assertEquals(MatterPhase.SOLID, gallium.phaseAtTemperature(PeriodicTableEngine.STP_TEMPERATURE_K))
    assertEquals(MatterPhase.SOLID, tungsten.phaseAtTemperature(PeriodicTableEngine.STP_TEMPERATURE_K))
    assertEquals(MatterPhase.GAS, oxygen.phaseAtTemperature(PeriodicTableEngine.STP_TEMPERATURE_K))

    // At 310 K (human body temperature ~37 °C): Gallium (mp 302.91 K) melts into Liquid
    assertEquals(MatterPhase.LIQUID, gallium.phaseAtTemperature(310.0))

    // At 100 K (-173 °C): Mercury (mp 234.32 K) freezes into Solid, Oxygen (bp 90.19 K) remains Gas
    assertEquals(MatterPhase.SOLID, mercury.phaseAtTemperature(100.0))
    assertEquals(MatterPhase.GAS, oxygen.phaseAtTemperature(100.0))

    // At 70 K: Oxygen is Liquid (54.36 K <= 70 K < 90.19 K)
    assertEquals(MatterPhase.LIQUID, oxygen.phaseAtTemperature(70.0))

    // At 4000 K: Tungsten (mp 3695 K, bp 5828 K) is Liquid
    assertEquals(MatterPhase.LIQUID, tungsten.phaseAtTemperature(4000.0))
  }

  @Test
  fun filterElements_filtersByQueryCategoryPhaseBlockAndFavorites() {
    // Search by symbol
    val goldMatch = PeriodicTableEngine.filterElements(query = "Au")
    assertTrue(goldMatch.isNotEmpty())
    assertEquals(79, goldMatch.first().atomicNumber)

    // Search by atomic number
    val ironMatch = PeriodicTableEngine.filterElements(query = "26")
    assertEquals(26, ironMatch.first().atomicNumber)

    // Filter by Noble Gas category (He, Ne, Ar, Kr, Xe, Rn, Og = 7 elements)
    val nobleGases = PeriodicTableEngine.filterElements(category = ElementCategory.NOBLE_GAS)
    assertEquals(7, nobleGases.size)
    assertTrue(nobleGases.all { it.category == ElementCategory.NOBLE_GAS })

    // Filter by Liquid phase at STP (Br and Hg = 2 elements)
    val stpLiquids = PeriodicTableEngine.filterElements(
      phase = MatterPhase.LIQUID,
      temperatureK = PeriodicTableEngine.STP_TEMPERATURE_K,
    )
    assertEquals(listOf("Br", "Hg"), stpLiquids.map { it.symbol })

    // Filter by favorites
    val favResults = PeriodicTableEngine.filterElements(
      favoritesOnly = true,
      favoriteAtomicNumbers = setOf(6, 14, 79),
    )
    assertEquals(listOf("C", "Si", "Au"), favResults.map { it.symbol })
  }

  @Test
  fun calculateMolarMass_parsesSimpleParenthesizedAndHydrateFormulasCorrectly() {
    // 1. Water: H2O -> 2 * 1.008 + 15.999 = 18.015 g/mol
    val water = PeriodicTableEngine.calculateMolarMass("H2O") as MolarMassResult.Success
    assertEquals(18.015, water.totalMolarMass, 1e-3)
    assertEquals(3, water.totalAtoms)
    assertEquals(2, water.components.size)

    // 2. Glucose with Unicode subscripts: C₆H₁₂O₆ -> 6*12.011 + 12*1.008 + 6*15.999 = 180.156 g/mol
    val glucose = PeriodicTableEngine.calculateMolarMass("C₆H₁₂O₆") as MolarMassResult.Success
    assertEquals(180.156, glucose.totalMolarMass, 1e-3)
    assertEquals(24, glucose.totalAtoms)

    // 3. Parenthesized formula: Ca(OH)2 -> 40.078 + 2*(15.999 + 1.008) = 74.092 g/mol
    val calciumHydroxide = PeriodicTableEngine.calculateMolarMass("Ca(OH)2") as MolarMassResult.Success
    assertEquals(74.092, calciumHydroxide.totalMolarMass, 1e-3)
    assertEquals(5, calciumHydroxide.totalAtoms)

    // 4. Hydrate formula: CuSO4·5H2O
    val hydrate = PeriodicTableEngine.calculateMolarMass("CuSO4·5H2O") as MolarMassResult.Success
    assertEquals(249.681, hydrate.totalMolarMass, 1e-2)
    assertEquals(21, hydrate.totalAtoms)

    // 5. Invalid formula returns Error
    val unknown = PeriodicTableEngine.calculateMolarMass("Xz2O")
    assertTrue(unknown is MolarMassResult.Error)

    val unclosed = PeriodicTableEngine.calculateMolarMass("Ca(OH2")
    assertTrue(unclosed is MolarMassResult.Error)
  }

  @Test
  fun elementsRepository_togglesAndPersistsFavorites() {
    val repo = InMemoryElementsRepository()
    assertTrue(repo.loadFavoriteAtomicNumbers().isEmpty())

    repo.toggleFavorite(26)
    repo.toggleFavorite(79)
    assertEquals(setOf(26, 79), repo.loadFavoriteAtomicNumbers())

    repo.toggleFavorite(26)
    assertEquals(setOf(79), repo.loadFavoriteAtomicNumbers())
  }

  @Test
  fun elementTempUnit_convertsAndCyclesCorrectly() {
    assertEquals(ElementTempUnit.CELSIUS, ElementTempUnit.KELVIN.next())
    assertEquals(ElementTempUnit.FAHRENHEIT, ElementTempUnit.CELSIUS.next())
    assertEquals(ElementTempUnit.KELVIN, ElementTempUnit.FAHRENHEIT.next())

    assertEquals(0.0, ElementTempUnit.CELSIUS.fromKelvin(273.15), 1e-4)
    assertEquals(32.0, ElementTempUnit.FAHRENHEIT.fromKelvin(273.15), 1e-4)
    assertEquals("0 °C", ElementTempUnit.CELSIUS.formatKelvin(273.15))
    assertEquals("—", ElementTempUnit.CELSIUS.formatKelvin(null))
  }

  @Test
  fun tableZoomAndGridMetrics_defaultsToFullDeviceWidthAndSupportsPinchZoomInAndOut() {
    assertEquals(1.0f, PeriodicTableEngine.DEFAULT_TABLE_ZOOM, 1e-4f)
    assertEquals(1.0f, PeriodicTableEngine.MIN_TABLE_ZOOM, 1e-4f)
    assertEquals(4.0f, PeriodicTableEngine.MAX_TABLE_ZOOM, 1e-4f)

    // Pinch to zoom in (zoomChange > 1f) and clamp at MAX_TABLE_ZOOM
    val zoomedIn = PeriodicTableEngine.calculateTableZoom(PeriodicTableEngine.DEFAULT_TABLE_ZOOM, 2.2f)
    assertEquals(2.2f, zoomedIn, 1e-4f)
    assertEquals(
      PeriodicTableEngine.MAX_TABLE_ZOOM,
      PeriodicTableEngine.calculateTableZoom(zoomedIn, 3.0f),
      1e-4f,
    )

    // Pinch to zoom out (zoomChange < 1f) and clamp at MIN_TABLE_ZOOM (full device width)
    val zoomedOut = PeriodicTableEngine.calculateTableZoom(zoomedIn, 0.5f)
    assertEquals(1.1f, zoomedOut, 1e-4f)
    assertEquals(
      PeriodicTableEngine.DEFAULT_TABLE_ZOOM,
      PeriodicTableEngine.calculateTableZoom(zoomedOut, 0.4f),
      1e-4f,
    )

    // Grid metrics at default zoom fit all 18 columns + period column across the full device width
    val viewportWidthPx = 1080
    val defaultMetrics = PeriodicTableEngine.calculateTableGridMetrics(
      viewportWidthPx = viewportWidthPx,
      zoom = PeriodicTableEngine.DEFAULT_TABLE_ZOOM,
      horizontalPaddingPx = 12,
    )
    assertEquals(viewportWidthPx, defaultMetrics.tableWidthPx)
    assertTrue(
      "Expected 18-column contentWidthPx (${defaultMetrics.contentWidthPx}) to fit within viewport ($viewportWidthPx)",
      defaultMetrics.contentWidthPx <= viewportWidthPx,
    )
    assertTrue(
      "Expected 18-column table to span nearly the full device width (${defaultMetrics.contentWidthPx} vs $viewportWidthPx)",
      defaultMetrics.contentWidthPx >= viewportWidthPx - 48,
    )

    // Zooming in scales tile dimensions and total table width up; zooming out scales them back down
    val zoomedInMetrics = PeriodicTableEngine.calculateTableGridMetrics(
      viewportWidthPx = viewportWidthPx,
      zoom = 2.5f,
      horizontalPaddingPx = 12,
    )
    assertTrue(zoomedInMetrics.tileWidthPx > defaultMetrics.tileWidthPx)
    assertTrue(zoomedInMetrics.tileHeightPx > defaultMetrics.tileHeightPx)
    assertTrue(zoomedInMetrics.tableWidthPx > defaultMetrics.tableWidthPx)

    val zoomedPartiallyOutMetrics = PeriodicTableEngine.calculateTableGridMetrics(
      viewportWidthPx = viewportWidthPx,
      zoom = 1.5f,
      horizontalPaddingPx = 12,
    )
    assertTrue(zoomedPartiallyOutMetrics.tileWidthPx in (defaultMetrics.tileWidthPx + 1) until zoomedInMetrics.tileWidthPx)
    assertTrue(zoomedPartiallyOutMetrics.tableWidthPx in (defaultMetrics.tableWidthPx + 1) until zoomedInMetrics.tableWidthPx)
  }
}
