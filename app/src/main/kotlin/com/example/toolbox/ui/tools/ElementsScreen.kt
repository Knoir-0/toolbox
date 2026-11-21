package com.example.toolbox.ui.tools

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toolbox.ui.theme.ToolboxTheme
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Primary display mode for the Periodic Table of Elements tool.
 */
enum class ElementsDisplayMode(val label: String) {
  TABLE("Table"),
  LIST("List"),
  MOLAR_MASS("Molar Mass"),
}

/**
 * Color-coding theme for element tiles on the 18-column Periodic Table grid.
 */
enum class ElementsColorMode(val label: String) {
  CATEGORY("Category"),
  PHASE("Phase"),
  BLOCK("Orbital Block"),
}

private data class ElementVisualColors(
  val container: Color,
  val onContainer: Color,
  val border: Color,
  val accent: Color,
)

@Composable
private fun rememberCategoryVisualColors(category: ElementCategory): ElementVisualColors {
  val scheme = MaterialTheme.colorScheme
  val (baseContainer, baseContent, baseAccent) = when (category) {
    ElementCategory.ALKALI_METAL -> Triple(
      lerp(scheme.errorContainer, scheme.primaryContainer, 0.25f),
      scheme.onErrorContainer,
      lerp(scheme.error, scheme.primary, 0.25f),
    )
    ElementCategory.ALKALINE_EARTH_METAL -> Triple(
      lerp(scheme.tertiaryContainer, scheme.errorContainer, 0.35f),
      scheme.onTertiaryContainer,
      lerp(scheme.tertiary, scheme.error, 0.35f),
    )
    ElementCategory.TRANSITION_METAL -> Triple(
      scheme.primaryContainer,
      scheme.onPrimaryContainer,
      scheme.primary,
    )
    ElementCategory.POST_TRANSITION_METAL -> Triple(
      scheme.secondaryContainer,
      scheme.onSecondaryContainer,
      scheme.secondary,
    )
    ElementCategory.METALLOID -> Triple(
      lerp(scheme.tertiaryContainer, scheme.secondaryContainer, 0.45f),
      scheme.onTertiaryContainer,
      lerp(scheme.tertiary, scheme.secondary, 0.45f),
    )
    ElementCategory.REACTIVE_NONMETAL -> Triple(
      lerp(scheme.primaryContainer, scheme.tertiaryContainer, 0.6f),
      scheme.onPrimaryContainer,
      lerp(scheme.primary, scheme.tertiary, 0.6f),
    )
    ElementCategory.HALOGEN -> Triple(
      scheme.tertiaryContainer,
      scheme.onTertiaryContainer,
      scheme.tertiary,
    )
    ElementCategory.NOBLE_GAS -> Triple(
      lerp(scheme.secondaryContainer, scheme.primaryContainer, 0.5f),
      scheme.onSecondaryContainer,
      lerp(scheme.secondary, scheme.primary, 0.5f),
    )
    ElementCategory.LANTHANIDE -> Triple(
      lerp(scheme.primaryContainer, scheme.surfaceVariant, 0.35f),
      scheme.onPrimaryContainer,
      scheme.primary,
    )
    ElementCategory.ACTINIDE -> Triple(
      lerp(scheme.errorContainer, scheme.tertiaryContainer, 0.5f),
      scheme.onErrorContainer,
      lerp(scheme.error, scheme.tertiary, 0.5f),
    )
  }
  return ElementVisualColors(
    container = baseContainer,
    onContainer = baseContent,
    border = baseAccent.copy(alpha = 0.55f),
    accent = baseAccent,
  )
}

@Composable
private fun rememberPhaseVisualColors(phase: MatterPhase): ElementVisualColors {
  val scheme = MaterialTheme.colorScheme
  return when (phase) {
    MatterPhase.SOLID -> ElementVisualColors(
      container = scheme.primaryContainer,
      onContainer = scheme.onPrimaryContainer,
      border = scheme.primary.copy(alpha = 0.55f),
      accent = scheme.primary,
    )
    MatterPhase.LIQUID -> ElementVisualColors(
      container = scheme.tertiaryContainer,
      onContainer = scheme.onTertiaryContainer,
      border = scheme.tertiary.copy(alpha = 0.65f),
      accent = scheme.tertiary,
    )
    MatterPhase.GAS -> ElementVisualColors(
      container = scheme.secondaryContainer,
      onContainer = scheme.onSecondaryContainer,
      border = scheme.secondary.copy(alpha = 0.55f),
      accent = scheme.secondary,
    )
    MatterPhase.UNKNOWN -> ElementVisualColors(
      container = scheme.surfaceVariant,
      onContainer = scheme.onSurfaceVariant,
      border = scheme.outlineVariant,
      accent = scheme.outline,
    )
  }
}

@Composable
private fun rememberBlockVisualColors(block: ElementBlock): ElementVisualColors {
  val scheme = MaterialTheme.colorScheme
  return when (block) {
    ElementBlock.S -> ElementVisualColors(
      container = scheme.tertiaryContainer,
      onContainer = scheme.onTertiaryContainer,
      border = scheme.tertiary.copy(alpha = 0.55f),
      accent = scheme.tertiary,
    )
    ElementBlock.P -> ElementVisualColors(
      container = scheme.secondaryContainer,
      onContainer = scheme.onSecondaryContainer,
      border = scheme.secondary.copy(alpha = 0.55f),
      accent = scheme.secondary,
    )
    ElementBlock.D -> ElementVisualColors(
      container = scheme.primaryContainer,
      onContainer = scheme.onPrimaryContainer,
      border = scheme.primary.copy(alpha = 0.55f),
      accent = scheme.primary,
    )
    ElementBlock.F -> ElementVisualColors(
      container = lerp(scheme.errorContainer, scheme.primaryContainer, 0.45f),
      onContainer = scheme.onErrorContainer,
      border = scheme.error.copy(alpha = 0.55f),
      accent = scheme.error,
    )
  }
}

@Composable
private fun resolveElementVisualColors(
  element: ChemicalElement,
  colorMode: ElementsColorMode,
  activeTemperatureK: Double,
): ElementVisualColors {
  return when (colorMode) {
    ElementsColorMode.CATEGORY -> rememberCategoryVisualColors(element.category)
    ElementsColorMode.PHASE -> rememberPhaseVisualColors(element.phaseAtTemperature(activeTemperatureK))
    ElementsColorMode.BLOCK -> rememberBlockVisualColors(element.block)
  }
}

private fun formatElementSummaryText(
  element: ChemicalElement,
  tempUnit: ElementTempUnit,
): String {
  return buildString {
    appendLine("${element.name} (${element.symbol}) — Atomic #${element.atomicNumber}")
    appendLine("Category: ${element.category.label}")
    appendLine("Atomic Mass: ${element.formattedAtomicMass}")
    appendLine("Period ${element.period} • ${element.groupLabel} • ${element.block.label}")
    appendLine("Standard Phase (STP): ${element.standardPhase.label}")
    appendLine("Electron Configuration: ${element.electronConfiguration}")
    appendLine("Shells: ${element.shellsFormatted}")
    element.electronegativity?.let { appendLine("Electronegativity: $it (Pauling)") }
    element.density?.let { appendLine("Density: $it g/cm³") }
    element.meltingPointK?.let { appendLine("Melting Point: ${tempUnit.formatKelvin(it)}") }
    element.boilingPointK?.let { appendLine("Boiling Point: ${tempUnit.formatKelvin(it)}") }
    element.atomicRadiusPm?.let { appendLine("Atomic Radius: $it pm") }
    element.ionizationEnergyKjMol?.let { appendLine("1st Ionization Energy: $it kJ/mol") }
    appendLine("Oxidation States: ${element.oxidationStates}")
    appendLine("Discovered: ${element.discoveryYear} (${element.discoveredBy})")
    appendLine()
    appendLine(element.summary)
  }.trim()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ElementsScreen(
  modifier: Modifier = Modifier,
  initialAtomicNumber: Int? = null,
  repository: ElementsRepository? = null,
  onOpenCalculator: ((String) -> Unit)? = null,
  onOpenUnitConverter: ((value: String, fromUnitId: String?) -> Unit)? = null,
  onNavigateBack: (() -> Unit)? = null,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current
  val focusManager = LocalFocusManager.current

  val repo = remember(repository, context, isInspection) {
    repository ?: if (isInspection) {
      InMemoryElementsRepository(initialFavorites = setOf(1, 6, 8, 26, 79))
    } else {
      SharedPreferencesElementsRepository(context)
    }
  }

  var favoriteAtomicNumbers by remember { mutableStateOf(repo.loadFavoriteAtomicNumbers()) }
  var selectedAtomicNumber by rememberSaveable(initialAtomicNumber) {
    mutableIntStateOf(initialAtomicNumber?.coerceIn(1, 118) ?: 1)
  }
  val selectedElement = remember(selectedAtomicNumber) {
    PeriodicTableEngine.byAtomicNumber(selectedAtomicNumber) ?: PeriodicTableEngine.ALL_ELEMENTS.first()
  }

  var displayMode by rememberSaveable { mutableStateOf(ElementsDisplayMode.TABLE) }
  var colorMode by rememberSaveable { mutableStateOf(ElementsColorMode.CATEGORY) }
  var tempUnit by rememberSaveable { mutableStateOf(ElementTempUnit.CELSIUS) }

  // Category / Phase / Favorites filter pill bar
  var filterFavoritesOnly by rememberSaveable { mutableStateOf(false) }
  var selectedCategoryName by rememberSaveable { mutableStateOf<String?>(null) }
  val selectedCategory = remember(selectedCategoryName) {
    selectedCategoryName?.let { name -> ElementCategory.entries.firstOrNull { it.name == name } }
  }
  var selectedPhaseName by rememberSaveable { mutableStateOf<String?>(null) }
  val selectedPhase = remember(selectedPhaseName) {
    selectedPhaseName?.let { name -> MatterPhase.entries.firstOrNull { it.name == name } }
  }

  // Interactive temperature phase simulator
  var showTempSimulator by rememberSaveable { mutableStateOf(false) }
  var simulatorTempK by rememberSaveable {
    mutableFloatStateOf(PeriodicTableEngine.STP_TEMPERATURE_K.toFloat())
  }
  val activeTempK = if (showTempSimulator) {
    simulatorTempK.toDouble()
  } else {
    PeriodicTableEngine.STP_TEMPERATURE_K
  }

  // Toolbar search overlay state (Section 5.7 of DESIGN_LANGUAGE.md)
  var isSearchActive by rememberSaveable { mutableStateOf(false) }
  var searchQuery by rememberSaveable { mutableStateOf("") }

  // Detailed Element Inspector modal dialog state (Section 5.2 of DESIGN_LANGUAGE.md)
  var inspectingElement by remember { mutableStateOf<ChemicalElement?>(null) }

  // Molar Mass Calculator state
  var formulaInput by rememberSaveable { mutableStateOf("H2O") }

  fun closeSearch() {
    isSearchActive = false
    searchQuery = ""
    focusManager.clearFocus()
  }

  BackHandler(enabled = isSearchActive || inspectingElement != null) {
    when {
      inspectingElement != null -> inspectingElement = null
      isSearchActive -> closeSearch()
    }
  }

  fun toggleFavoriteElement(atomicNumber: Int) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    favoriteAtomicNumbers = repo.toggleFavorite(atomicNumber)
  }

  val filteredElements = remember(
    searchQuery,
    selectedCategory,
    selectedPhase,
    filterFavoritesOnly,
    favoriteAtomicNumbers,
    activeTempK,
  ) {
    PeriodicTableEngine.filterElements(
      query = searchQuery,
      category = selectedCategory,
      phase = selectedPhase,
      favoritesOnly = filterFavoritesOnly,
      favoriteAtomicNumbers = favoriteAtomicNumbers,
      temperatureK = activeTempK,
    )
  }

  val matchingAtomicNumbers = remember(filteredElements) {
    filteredElements.map { it.atomicNumber }.toSet()
  }

  val hasActiveFilter = filterFavoritesOnly ||
    selectedCategory != null ||
    selectedPhase != null ||
    searchQuery.isNotBlank()

  val subtext = remember(selectedElement, filteredElements.size, hasActiveFilter, displayMode) {
    when {
      displayMode == ElementsDisplayMode.MOLAR_MASS -> "Chemical formula molar mass & mass % analyzer"
      hasActiveFilter -> "${filteredElements.size} of 118 elements • Selected: ${selectedElement.symbol} (#${selectedElement.atomicNumber})"
      else -> "118 IUPAC elements • ${selectedElement.name} (${selectedElement.symbol}, #${selectedElement.atomicNumber})"
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("elements_screen"),
  ) {
    // ==================== 1. UNIFIED 68.DP TOP TOOLBAR ====================
    ToolboxTopBar(
      title = "Elements",
      subtext = subtext,
      icon = Icons.Default.Science,
      onNavigateBack = onNavigateBack,
      titleTestTag = "elements_toolbar_title",
      subtextTestTag = "elements_toolbar_subtext",
      actions = {
        // Temperature Unit Cycle Pill (K -> °C -> °F)
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              tempUnit = tempUnit.next()
            }
            .testTag("elements_temp_unit_toggle"),
        ) {
          Text(
            text = tempUnit.symbol,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          )
        }

        // Interactive Temperature Simulator Toggle
        IconButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showTempSimulator = !showTempSimulator
          },
          modifier = Modifier
            .size(36.dp)
            .testTag("elements_temp_simulator_toggle"),
        ) {
          Icon(
            imageVector = Icons.Default.Thermostat,
            contentDescription = "Toggle phase temperature simulator",
            tint = if (showTempSimulator) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(20.dp),
          )
        }

        // Display Mode Toggle (Table -> List -> Molar Mass -> Table)
        IconButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            displayMode = when (displayMode) {
              ElementsDisplayMode.TABLE -> ElementsDisplayMode.LIST
              ElementsDisplayMode.LIST -> ElementsDisplayMode.MOLAR_MASS
              ElementsDisplayMode.MOLAR_MASS -> ElementsDisplayMode.TABLE
            }
          },
          modifier = Modifier
            .size(36.dp)
            .testTag("elements_display_mode_toggle"),
        ) {
          Icon(
            imageVector = when (displayMode) {
              ElementsDisplayMode.TABLE -> Icons.AutoMirrored.Filled.ViewList
              ElementsDisplayMode.LIST -> Icons.Default.Calculate
              ElementsDisplayMode.MOLAR_MASS -> Icons.Default.GridOn
            },
            contentDescription = "Switch view mode",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
          )
        }
      },
      searchState = ToolboxTopBarSearchState(
        isActive = isSearchActive,
        query = searchQuery,
        onQueryChange = { searchQuery = it },
        onOpenSearch = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          isSearchActive = true
        },
        onCloseSearch = { closeSearch() },
        placeholder = "Search element name, symbol (Fe, Au), # or scientist...",
        searchContentDescription = "Search elements",
        toggleButtonTestTag = "elements_toggle_search_button",
        searchBarTestTag = "elements_toolbar_search_bar",
        closeButtonTestTag = "elements_close_search_button",
        inputTestTag = "elements_search_input",
        clearButtonTestTag = "elements_clear_search_button",
      ),
    )

    // ==================== 2. UNIFIED PILL NAVIGATION & FILTER BAR ====================
    ToolboxPillNavigationBar(
      rowTestTag = "elements_pill_navigation_bar",
    ) {
      // View Mode Pills
      ElementsDisplayMode.entries.forEach { mode ->
        ToolboxPillNavigationItem(
          selected = displayMode == mode,
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            displayMode = mode
          },
          leadingIcon = when (mode) {
            ElementsDisplayMode.TABLE -> Icons.Default.GridOn
            ElementsDisplayMode.LIST -> Icons.AutoMirrored.Filled.ViewList
            ElementsDisplayMode.MOLAR_MASS -> Icons.Default.Calculate
          },
          label = mode.label,
          testTag = "elements_mode_pill_${mode.name}",
        )
      }

      // All Reset Chip
      ToolboxPillNavigationItem(
        selected = !filterFavoritesOnly && selectedCategory == null && selectedPhase == null,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          filterFavoritesOnly = false
          selectedCategoryName = null
          selectedPhaseName = null
        },
        label = "All (118)",
        testTag = "elements_filter_all",
      )

      // Favorites Filter Chip
      ToolboxPillNavigationItem(
        selected = filterFavoritesOnly,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          filterFavoritesOnly = !filterFavoritesOnly
        },
        leadingIcon = if (filterFavoritesOnly) Icons.Default.Star else Icons.Default.StarBorder,
        label = if (favoriteAtomicNumbers.isNotEmpty()) {
          "Favorites (${favoriteAtomicNumbers.size})"
        } else {
          "Favorites"
        },
        testTag = "elements_filter_favorites",
      )

      // Phase Filter Chips (Solid, Liquid, Gas)
      listOf(MatterPhase.SOLID, MatterPhase.LIQUID, MatterPhase.GAS).forEach { phase ->
        val isPhaseSelected = selectedPhase == phase
        ToolboxPillNavigationItem(
          selected = isPhaseSelected,
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            selectedPhaseName = if (isPhaseSelected) null else phase.name
          },
          label = "${phase.label} (${phase.badge})",
          testTag = "elements_filter_phase_${phase.name}",
        )
      }

      // Chemical Category Filter Chips
      ElementCategory.entries.forEach { category ->
        val isCatSelected = selectedCategory == category
        ToolboxPillNavigationItem(
          selected = isCatSelected,
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            selectedCategoryName = if (isCatSelected) null else category.name
          },
          label = category.shortLabel,
          testTag = "elements_filter_category_${category.name}",
        )
      }
    }

    // ==================== 3. OPTIONAL TEMPERATURE PHASE SIMULATOR BAR ====================
    AnimatedVisibility(
      visible = showTempSimulator,
      enter = fadeIn(tween(180)) + expandVertically(expandFrom = Alignment.Top),
      exit = fadeOut(tween(150)) + shrinkVertically(shrinkTowards = Alignment.Top),
    ) {
      TemperaturePhaseSimulatorCard(
        temperatureK = simulatorTempK,
        tempUnit = tempUnit,
        onTemperatureChange = { simulatorTempK = it.coerceIn(0f, PeriodicTableEngine.MAX_SIMULATOR_TEMP_K) },
        onResetToStp = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          simulatorTempK = PeriodicTableEngine.STP_TEMPERATURE_K.toFloat()
        },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 4.dp),
      )
    }

    // ==================== 4. MAIN CONTENT + SEARCH OVERLAY ====================
    Box(
      modifier = Modifier
        .fillMaxSize()
        .weight(1f),
    ) {
      AnimatedContent(
        targetState = displayMode,
        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
        modifier = Modifier.fillMaxSize(),
        label = "elements_display_mode_content",
      ) { mode ->
        when (mode) {
          ElementsDisplayMode.TABLE -> {
            PeriodicTableInteractiveContent(
              selectedElement = selectedElement,
              matchingAtomicNumbers = matchingAtomicNumbers,
              hasActiveFilter = hasActiveFilter,
              favoriteAtomicNumbers = favoriteAtomicNumbers,
              colorMode = colorMode,
              onColorModeChange = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                colorMode = it
              },
              activeTemperatureK = activeTempK,
              tempUnit = tempUnit,
              onSelectElement = { element ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                selectedAtomicNumber = element.atomicNumber
              },
              onInspectElement = { element ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                selectedAtomicNumber = element.atomicNumber
                inspectingElement = element
              },
              onToggleFavorite = { element ->
                toggleFavoriteElement(element.atomicNumber)
              },
            )
          }
          ElementsDisplayMode.LIST -> {
            ElementsCatalogListView(
              elements = filteredElements,
              selectedElement = selectedElement,
              favoriteAtomicNumbers = favoriteAtomicNumbers,
              activeTemperatureK = activeTempK,
              tempUnit = tempUnit,
              onSelectElement = { element ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                selectedAtomicNumber = element.atomicNumber
                inspectingElement = element
              },
              onToggleFavorite = { element ->
                toggleFavoriteElement(element.atomicNumber)
              },
            )
          }
          ElementsDisplayMode.MOLAR_MASS -> {
            MolarMassCalculatorContent(
              formulaInput = formulaInput,
              onFormulaChange = { formulaInput = it },
              onInspectElement = { element ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                selectedAtomicNumber = element.atomicNumber
                inspectingElement = element
              },
              onSendToCalculator = onOpenCalculator,
              onCopyResult = { text ->
                CrossToolActions.copyTextToClipboard(
                  context = context,
                  label = "Molar Mass",
                  text = text,
                )
              },
            )
          }
        }
      }

      // Top-Expanding Search Catalog Overlay (Section 5.7 of DESIGN_LANGUAGE.md)
      androidx.compose.animation.AnimatedVisibility(
        visible = isSearchActive,
        enter = fadeIn(tween(200)) + expandVertically(expandFrom = Alignment.Top),
        exit = fadeOut(tween(180)) + shrinkVertically(shrinkTowards = Alignment.Top),
        modifier = Modifier.fillMaxSize(),
      ) {
        ElementsSearchOverlay(
          matchingElements = filteredElements,
          selectedElement = selectedElement,
          favoriteAtomicNumbers = favoriteAtomicNumbers,
          activeTemperatureK = activeTempK,
          tempUnit = tempUnit,
          onSelectElement = { element ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            selectedAtomicNumber = element.atomicNumber
            inspectingElement = element
            closeSearch()
          },
          onToggleFavorite = { element ->
            toggleFavoriteElement(element.atomicNumber)
          },
        )
      }
    }
  }

  // ==================== 5. DETAILED ELEMENT INSPECTOR DIALOG ====================
  inspectingElement?.let { element ->
    ElementInspectorDialog(
      element = element,
      isFavorite = element.atomicNumber in favoriteAtomicNumbers,
      activeTemperatureK = activeTempK,
      tempUnit = tempUnit,
      onToggleFavorite = { toggleFavoriteElement(element.atomicNumber) },
      onNavigateElement = { nextAtomicNum ->
        val next = PeriodicTableEngine.byAtomicNumber(nextAtomicNum)
        if (next != null) {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          selectedAtomicNumber = next.atomicNumber
          inspectingElement = next
        }
      },
      onCopySummary = {
        CrossToolActions.copyTextToClipboard(
          context = context,
          label = "${element.name} (${element.symbol})",
          text = formatElementSummaryText(element, tempUnit),
        )
      },
      onShareSummary = {
        CrossToolActions.shareText(
          context = context,
          title = "${element.name} (${element.symbol}) — Element #${element.atomicNumber}",
          text = formatElementSummaryText(element, tempUnit),
        )
      },
      onSaveToNotes = {
        CrossToolActions.saveTextToNotes(
          context = context,
          title = "${element.name} (${element.symbol}) — #${element.atomicNumber}",
          content = formatElementSummaryText(element, tempUnit),
          labels = listOf("Elements", element.category.shortLabel),
        )
      },
      onOpenInCalculator = onOpenCalculator?.let { openCalc ->
        {
          inspectingElement = null
          openCalc(element.atomicMass.toString())
        }
      },
      onOpenMeltingPointInConverter = if (onOpenUnitConverter != null && element.meltingPointK != null) {
        {
          val meltK = element.meltingPointK
          inspectingElement = null
          onOpenUnitConverter(String.format(Locale.US, "%.2f", meltK), "k")
        }
      } else {
        null
      },
      onDismiss = { inspectingElement = null },
    )
  }
}

@Composable
private fun TemperaturePhaseSimulatorCard(
  temperatureK: Float,
  tempUnit: ElementTempUnit,
  onTemperatureChange: (Float) -> Unit,
  onResetToStp: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val phaseCounts = remember(temperatureK) {
    val t = temperatureK.toDouble()
    var solids = 0
    var liquids = 0
    var gases = 0
    for (el in PeriodicTableEngine.ALL_ELEMENTS) {
      when (el.phaseAtTemperature(t)) {
        MatterPhase.SOLID -> solids++
        MatterPhase.LIQUID -> liquids++
        MatterPhase.GAS -> gases++
        MatterPhase.UNKNOWN -> {}
      }
    }
    Triple(solids, liquids, gases)
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = modifier.testTag("elements_temp_simulator_card"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Thermostat,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = "Phase @ ${tempUnit.formatKelvin(temperatureK.toDouble())}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "${phaseCounts.first}s • ${phaseCounts.second}l • ${phaseCounts.third}g",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
              .clip(CircleShape)
              .clickable(onClick = onResetToStp)
              .testTag("elements_reset_stp_button"),
          ) {
            Text(
              text = "STP (25 °C)",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
          }
        }
      }

      SlimDotSlider(
        value = temperatureK,
        onValueChange = onTemperatureChange,
        valueRange = PeriodicTableEngine.MIN_SIMULATOR_TEMP_K..PeriodicTableEngine.MAX_SIMULATOR_TEMP_K,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("elements_temp_simulator_slider"),
      )
    }
  }
}

@Composable
private fun PeriodicTableInteractiveContent(
  selectedElement: ChemicalElement,
  matchingAtomicNumbers: Set<Int>,
  hasActiveFilter: Boolean,
  favoriteAtomicNumbers: Set<Int>,
  colorMode: ElementsColorMode,
  onColorModeChange: (ElementsColorMode) -> Unit,
  activeTemperatureK: Double,
  tempUnit: ElementTempUnit,
  onSelectElement: (ChemicalElement) -> Unit,
  onInspectElement: (ChemicalElement) -> Unit,
  onToggleFavorite: (ChemicalElement) -> Unit,
) {
  val horizontalScrollState = rememberScrollState()
  val verticalScrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(verticalScrollState)
      .padding(bottom = 16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    // 1. Selected Element Hero Spotlight Card
    SelectedElementSpotlightCard(
      element = selectedElement,
      isFavorite = selectedElement.atomicNumber in favoriteAtomicNumbers,
      activeTemperatureK = activeTemperatureK,
      tempUnit = tempUnit,
      onInspect = { onInspectElement(selectedElement) },
      onToggleFavorite = { onToggleFavorite(selectedElement) },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 4.dp),
    )

    // 2. Color Mode & Legend Row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "18-Group IUPAC Periodic Table (Tap tile to select, double-tap/Info for details)",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f),
      )

      Spacer(modifier = Modifier.width(8.dp))

      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        ElementsColorMode.entries.forEach { mode ->
          val selected = colorMode == mode
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (selected) {
              MaterialTheme.colorScheme.primaryContainer
            } else {
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
            modifier = Modifier
              .clip(RoundedCornerShape(14.dp))
              .clickable { onColorModeChange(mode) }
              .testTag("elements_color_mode_${mode.name}"),
          ) {
            Text(
              text = mode.label,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
              color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
          }
        }
      }
    }

    // 3. 2D Horizontally Scrollable 18-Column Periodic Table Grid
    val tileWidth = 52.dp
    val tileHeight = 58.dp
    val tileSpacing = 3.dp
    val periodColumnWidth = 22.dp

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(horizontalScrollState)
        .padding(horizontal = 12.dp)
        .testTag("periodic_table_grid"),
      verticalArrangement = Arrangement.spacedBy(tileSpacing),
    ) {
      // Group Header Row (1..18)
      Row(
        horizontalArrangement = Arrangement.spacedBy(tileSpacing),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier.width(periodColumnWidth),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "P\\G",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        for (col in 1..18) {
          Box(
            modifier = Modifier
              .width(tileWidth)
              .height(18.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = col.toString(),
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      // Main Table Periods 1..7
      for (row in 1..7) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(tileSpacing),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // Period Number Badge
          Box(
            modifier = Modifier
              .width(periodColumnWidth)
              .height(tileHeight),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = row.toString(),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          for (col in 1..18) {
            val element = PeriodicTableEngine.byGridPosition(row, col)
            if (element != null) {
              val isSelected = element.atomicNumber == selectedElement.atomicNumber
              val isMatch = element.atomicNumber in matchingAtomicNumbers
              PeriodicTableElementTile(
                element = element,
                isSelected = isSelected,
                isDimmed = hasActiveFilter && !isMatch,
                isFavorite = element.atomicNumber in favoriteAtomicNumbers,
                colorMode = colorMode,
                activeTemperatureK = activeTemperatureK,
                tileWidth = tileWidth,
                tileHeight = tileHeight,
                onClick = {
                  if (isSelected) {
                    onInspectElement(element)
                  } else {
                    onSelectElement(element)
                  }
                },
              )
            } else if (row == 6 && col == 3) {
              SeriesPlaceholderTile(
                rangeText = "57–71",
                label = "La–Lu",
                category = ElementCategory.LANTHANIDE,
                tileWidth = tileWidth,
                tileHeight = tileHeight,
              )
            } else if (row == 7 && col == 3) {
              SeriesPlaceholderTile(
                rangeText = "89–103",
                label = "Ac–Lr",
                category = ElementCategory.ACTINIDE,
                tileWidth = tileWidth,
                tileHeight = tileHeight,
              )
            } else {
              Spacer(
                modifier = Modifier
                  .width(tileWidth)
                  .height(tileHeight),
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // F-Block: Lanthanides (Row 9, Cols 3..17) & Actinides (Row 10, Cols 3..17)
      listOf(
        9 to "6f" to "Lanthanides",
        10 to "7f" to "Actinides",
      ).forEach { (rowAndBadge, seriesLabel) ->
        val (fRow, badgeText) = rowAndBadge
        Row(
          horizontalArrangement = Arrangement.spacedBy(tileSpacing),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(
            modifier = Modifier
              .width(periodColumnWidth)
              .height(tileHeight),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = badgeText,
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
          }

          for (col in 1..18) {
            val element = PeriodicTableEngine.byGridPosition(fRow, col)
            if (element != null) {
              val isSelected = element.atomicNumber == selectedElement.atomicNumber
              val isMatch = element.atomicNumber in matchingAtomicNumbers
              PeriodicTableElementTile(
                element = element,
                isSelected = isSelected,
                isDimmed = hasActiveFilter && !isMatch,
                isFavorite = element.atomicNumber in favoriteAtomicNumbers,
                colorMode = colorMode,
                activeTemperatureK = activeTemperatureK,
                tileWidth = tileWidth,
                tileHeight = tileHeight,
                onClick = {
                  if (isSelected) {
                    onInspectElement(element)
                  } else {
                    onSelectElement(element)
                  }
                },
              )
            } else if (col == 2) {
              Box(
                modifier = Modifier
                  .width(tileWidth)
                  .height(tileHeight),
                contentAlignment = Alignment.CenterEnd,
              ) {
                Text(
                  text = seriesLabel,
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.End,
                  maxLines = 2,
                )
              }
            } else {
              Spacer(
                modifier = Modifier
                  .width(tileWidth)
                  .height(tileHeight),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PeriodicTableElementTile(
  element: ChemicalElement,
  isSelected: Boolean,
  isDimmed: Boolean,
  isFavorite: Boolean,
  colorMode: ElementsColorMode,
  activeTemperatureK: Double,
  tileWidth: Dp,
  tileHeight: Dp,
  onClick: () -> Unit,
) {
  val visualColors = resolveElementVisualColors(element, colorMode, activeTemperatureK)
  val phaseAtTemp = element.phaseAtTemperature(activeTemperatureK)

  val borderStroke = if (isSelected) {
    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
  } else {
    BorderStroke(1.dp, visualColors.border)
  }

  Surface(
    shape = RoundedCornerShape(10.dp),
    color = visualColors.container,
    border = borderStroke,
    tonalElevation = if (isSelected) 4.dp else 1.dp,
    modifier = Modifier
      .width(tileWidth)
      .height(tileHeight)
      .alpha(if (isDimmed) 0.22f else 1f)
      .clip(RoundedCornerShape(10.dp))
      .clickable(onClick = onClick)
      .testTag("element_tile_${element.symbol}"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 4.dp, vertical = 3.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // Top row: Atomic Number + Phase badge / Favorite star
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = element.atomicNumber.toString(),
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 9.sp,
            lineHeight = 10.sp,
          ),
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          color = visualColors.onContainer.copy(alpha = 0.85f),
        )
        Text(
          text = if (isFavorite) "★" else phaseAtTemp.badge,
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 8.sp,
            lineHeight = 9.sp,
          ),
          fontWeight = FontWeight.Bold,
          color = visualColors.accent,
        )
      }

      // Center: Large Element Symbol
      Text(
        text = element.symbol,
        style = MaterialTheme.typography.titleMedium.copy(
          fontSize = 16.sp,
          lineHeight = 17.sp,
        ),
        fontWeight = FontWeight.ExtraBold,
        color = visualColors.onContainer,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = Modifier.fillMaxWidth(),
      )

      // Bottom: Element Name or Mass
      Text(
        text = element.name,
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 7.5.sp,
          lineHeight = 8.5.sp,
        ),
        color = visualColors.onContainer.copy(alpha = 0.9f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun SeriesPlaceholderTile(
  rangeText: String,
  label: String,
  category: ElementCategory,
  tileWidth: Dp,
  tileHeight: Dp,
) {
  val visualColors = rememberCategoryVisualColors(category)
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = visualColors.container.copy(alpha = 0.55f),
    border = BorderStroke(1.dp, visualColors.border),
    modifier = Modifier
      .width(tileWidth)
      .height(tileHeight),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(3.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Text(
        text = rangeText,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = visualColors.onContainer,
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
        fontWeight = FontWeight.SemiBold,
        color = visualColors.accent,
      )
    }
  }
}

@Composable
private fun SelectedElementSpotlightCard(
  element: ChemicalElement,
  isFavorite: Boolean,
  activeTemperatureK: Double,
  tempUnit: ElementTempUnit,
  onInspect: () -> Unit,
  onToggleFavorite: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val visualColors = rememberCategoryVisualColors(element.category)
  val phaseAtTemp = element.phaseAtTemperature(activeTemperatureK)

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
    modifier = modifier
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onInspect)
      .testTag("selected_element_spotlight_card"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // Large Element Symbol Badge
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = visualColors.container,
        border = BorderStroke(1.5.dp, visualColors.border),
        modifier = Modifier.size(74.dp),
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(6.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = "#${element.atomicNumber}",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = visualColors.onContainer,
            )
            Text(
              text = phaseAtTemp.badge.uppercase(Locale.ROOT),
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              fontWeight = FontWeight.ExtraBold,
              color = visualColors.accent,
            )
          }
          Text(
            text = element.symbol,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = visualColors.onContainer,
          )
          Text(
            text = element.compactAtomicMass,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            fontFamily = FontFamily.Monospace,
            color = visualColors.onContainer.copy(alpha = 0.85f),
            maxLines = 1,
          )
        }
      }

      // Metadata Column
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(3.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = element.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = visualColors.container,
          ) {
            Text(
              text = element.category.shortLabel,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = visualColors.onContainer,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
          }
        }

        Text(
          text = "${element.formattedAtomicMass} • Period ${element.period} • ${element.groupLabel} • ${phaseAtTemp.label}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        Text(
          text = "Config: ${element.electronConfiguration}  |  Shells: ${element.shellsFormatted}",
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        val meltText = tempUnit.formatKelvin(element.meltingPointK)
        val boilText = tempUnit.formatKelvin(element.boilingPointK)
        val enText = element.electronegativity?.toString() ?: "—"
        Text(
          text = "Melt: $meltText • Boil: $boilText • EN: $enText",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      // Trailing Favorite + Info Buttons
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        IconButton(
          onClick = onToggleFavorite,
          modifier = Modifier
            .size(34.dp)
            .testTag("spotlight_favorite_button"),
        ) {
          Icon(
            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = if (isFavorite) "Remove favorite" else "Favorite element",
            tint = if (isFavorite) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(20.dp),
          )
        }
        IconButton(
          onClick = onInspect,
          modifier = Modifier
            .size(34.dp)
            .testTag("spotlight_inspect_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Inspect element details",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun ElementsCatalogListView(
  elements: List<ChemicalElement>,
  selectedElement: ChemicalElement,
  favoriteAtomicNumbers: Set<Int>,
  activeTemperatureK: Double,
  tempUnit: ElementTempUnit,
  onSelectElement: (ChemicalElement) -> Unit,
  onToggleFavorite: (ChemicalElement) -> Unit,
) {
  if (elements.isEmpty()) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
      contentAlignment = Alignment.Center,
    ) {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Science,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp),
          )
          Text(
            text = "No matching elements",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Try clearing the active category, phase, or favorites filter.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
          )
        }
      }
    }
    return
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("elements_list_view"),
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    items(elements, key = { it.atomicNumber }) { element ->
      ElementCatalogRowCard(
        element = element,
        isSelected = element.atomicNumber == selectedElement.atomicNumber,
        isFavorite = element.atomicNumber in favoriteAtomicNumbers,
        activeTemperatureK = activeTemperatureK,
        tempUnit = tempUnit,
        onClick = { onSelectElement(element) },
        onToggleFavorite = { onToggleFavorite(element) },
      )
    }
  }
}

@Composable
private fun ElementCatalogRowCard(
  element: ChemicalElement,
  isSelected: Boolean,
  isFavorite: Boolean,
  activeTemperatureK: Double,
  tempUnit: ElementTempUnit,
  onClick: () -> Unit,
  onToggleFavorite: () -> Unit,
) {
  val visualColors = rememberCategoryVisualColors(element.category)
  val phaseAtTemp = element.phaseAtTemperature(activeTemperatureK)

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
      } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
      },
    ),
    border = BorderStroke(
      width = if (isSelected) 1.5.dp else 1.dp,
      color = if (isSelected) {
        MaterialTheme.colorScheme.primary
      } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
      },
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .testTag("element_list_card_${element.symbol}"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Leading Symbol Monogram Tile
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = visualColors.container,
        border = BorderStroke(1.dp, visualColors.border),
        modifier = Modifier.size(50.dp),
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
        ) {
          Text(
            text = "#${element.atomicNumber}",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
            fontFamily = FontFamily.Monospace,
            color = visualColors.onContainer.copy(alpha = 0.85f),
          )
          Text(
            text = element.symbol,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = visualColors.onContainer,
          )
        }
      }

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = element.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = visualColors.container,
          ) {
            Text(
              text = element.category.shortLabel,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Medium,
              color = visualColors.onContainer,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
            )
          }
        }

        Text(
          text = "${element.formattedAtomicMass} • ${phaseAtTemp.label} • ${element.electronConfiguration}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        Text(
          text = "Melt: ${tempUnit.formatKelvin(element.meltingPointK)} • Boil: ${tempUnit.formatKelvin(element.boilingPointK)}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      IconButton(
        onClick = onToggleFavorite,
        modifier = Modifier
          .size(36.dp)
          .testTag("element_favorite_toggle_${element.symbol}"),
      ) {
        Icon(
          imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
          contentDescription = if (isFavorite) "Unfavorite ${element.name}" else "Favorite ${element.name}",
          tint = if (isFavorite) {
            MaterialTheme.colorScheme.primary
          } else {
            MaterialTheme.colorScheme.onSurfaceVariant
          },
          modifier = Modifier.size(20.dp),
        )
      }
    }
  }
}

@Composable
private fun ElementsSearchOverlay(
  matchingElements: List<ChemicalElement>,
  selectedElement: ChemicalElement,
  favoriteAtomicNumbers: Set<Int>,
  activeTemperatureK: Double,
  tempUnit: ElementTempUnit,
  onSelectElement: (ChemicalElement) -> Unit,
  onToggleFavorite: (ChemicalElement) -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.background,
    modifier = Modifier
      .fillMaxSize()
      .testTag("elements_search_overlay"),
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "${matchingElements.size} matching element${if (matchingElements.size == 1) "" else "s"}",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      ElementsCatalogListView(
        elements = matchingElements,
        selectedElement = selectedElement,
        favoriteAtomicNumbers = favoriteAtomicNumbers,
        activeTemperatureK = activeTemperatureK,
        tempUnit = tempUnit,
        onSelectElement = onSelectElement,
        onToggleFavorite = onToggleFavorite,
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MolarMassCalculatorContent(
  formulaInput: String,
  onFormulaChange: (String) -> Unit,
  onInspectElement: (ChemicalElement) -> Unit,
  onSendToCalculator: ((String) -> Unit)?,
  onCopyResult: (String) -> Unit,
) {
  val focusManager = LocalFocusManager.current
  val molarMassResult = remember(formulaInput) {
    PeriodicTableEngine.calculateMolarMass(formulaInput)
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("elements_molar_mass_view"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // 1. Formula Input Card
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Text(
            text = "Chemical Formula Molar Mass Calculator",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )

          OutlinedTextField(
            value = formulaInput,
            onValueChange = onFormulaChange,
            label = { Text("Chemical Formula (case-sensitive symbols)") },
            placeholder = { Text("e.g. H2O, C6H12O6, Ca(OH)2, CuSO4·5H2O") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Science,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
              )
            },
            trailingIcon = {
              if (formulaInput.isNotEmpty()) {
                IconButton(
                  onClick = { onFormulaChange("") },
                  modifier = Modifier.testTag("molar_mass_clear_button"),
                ) {
                  Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear formula",
                  )
                }
              }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("molar_mass_formula_input"),
          )

          // Quick Sample Formula Chips
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            PeriodicTableEngine.SAMPLE_FORMULAS.forEach { (formula, name) ->
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (formulaInput.trim() == formula) {
                  MaterialTheme.colorScheme.primaryContainer
                } else {
                  MaterialTheme.colorScheme.surface
                },
                border = BorderStroke(
                  1.dp,
                  MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                ),
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .clickable { onFormulaChange(formula) }
                  .testTag("sample_formula_chip_$formula"),
              ) {
                Text(
                  text = "$formula ($name)",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Medium,
                  color = if (formulaInput.trim() == formula) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                  } else {
                    MaterialTheme.colorScheme.onSurface
                  },
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
              }
            }
          }
        }
      }
    }

    // 2. Result or Error Card
    when (molarMassResult) {
      is MolarMassResult.Error -> {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("molar_mass_error_card"),
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
              )
              Text(
                text = molarMassResult.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
              )
            }
          }
        }
      }
      is MolarMassResult.Success -> {
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("molar_mass_result_card"),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column {
                  Text(
                    text = "Total Molar Mass (${molarMassResult.formula})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                  Text(
                    text = molarMassResult.formattedMolarMass,
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("molar_mass_total_value"),
                  )
                  Text(
                    text = "${molarMassResult.totalAtoms} total atoms across ${molarMassResult.components.size} element(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  IconButton(
                    onClick = { onCopyResult(molarMassResult.formattedMolarMass) },
                    modifier = Modifier.testTag("molar_mass_copy_button"),
                  ) {
                    Icon(
                      imageVector = Icons.Default.ContentCopy,
                      contentDescription = "Copy molar mass",
                      tint = MaterialTheme.colorScheme.primary,
                    )
                  }
                  if (onSendToCalculator != null) {
                    IconButton(
                      onClick = {
                        onSendToCalculator(
                          String.format(Locale.US, "%.4f", molarMassResult.totalMolarMass)
                        )
                      },
                      modifier = Modifier.testTag("molar_mass_open_calculator_button"),
                    ) {
                      Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "Open in Calculate",
                        tint = MaterialTheme.colorScheme.primary,
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // Per-Element Mass Composition Breakdown
        items(molarMassResult.components, key = { it.element.atomicNumber }) { comp ->
          val visualColors = rememberCategoryVisualColors(comp.element.category)
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .clickable { onInspectElement(comp.element) },
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = visualColors.container,
                    border = BorderStroke(1.dp, visualColors.border),
                    modifier = Modifier.size(38.dp),
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(
                        text = comp.element.symbol,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = visualColors.onContainer,
                      )
                    }
                  }

                  Column {
                    Text(
                      text = "${comp.element.name} × ${comp.count}",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                      text = "${comp.count} × ${comp.element.formattedAtomicMass}",
                      style = MaterialTheme.typography.labelSmall,
                      fontFamily = FontFamily.Monospace,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = String.format(Locale.US, "%.4f g/mol", comp.subtotalMass),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = String.format(Locale.US, "%.2f%%", comp.massPercentage),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                  )
                }
              }

              LinearProgressIndicator(
                progress = { (comp.massPercentage / 100.0).toFloat().coerceIn(0f, 1f) },
                color = visualColors.accent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(CircleShape),
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Rich Material 3 Element Inspector Dialog following Section 5.2 of DESIGN_LANGUAGE.md:
 * - 28.dp rounded corners, `surfaceContainerHigh`, `tonalElevation = 6.dp`
 * - 44.dp circular icon badge + title + subtext header
 * - Interactive 2D Bohr Electron Shell Diagram canvas
 * - Comprehensive physical, atomic, thermodynamic, and historical metadata table
 * - Cross-tool actions (Copy, Share, Save to Notes, Open in Calculate / Units)
 */
@Composable
private fun ElementInspectorDialog(
  element: ChemicalElement,
  isFavorite: Boolean,
  activeTemperatureK: Double,
  tempUnit: ElementTempUnit,
  onToggleFavorite: () -> Unit,
  onNavigateElement: (Int) -> Unit,
  onCopySummary: () -> Unit,
  onShareSummary: () -> Unit,
  onSaveToNotes: () -> Unit,
  onOpenInCalculator: (() -> Unit)?,
  onOpenMeltingPointInConverter: (() -> Unit)?,
  onDismiss: () -> Unit,
) {
  val visualColors = rememberCategoryVisualColors(element.category)
  val phaseAtTemp = element.phaseAtTemperature(activeTemperatureK)

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("element_inspector_dialog"),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(visualColors.container),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = element.symbol,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = visualColors.onContainer,
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "${element.name} (#${element.atomicNumber})",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${element.category.label} • ${element.formattedAtomicMass}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        IconButton(
          onClick = onToggleFavorite,
          modifier = Modifier
            .size(36.dp)
            .testTag("dialog_favorite_button"),
        ) {
          Icon(
            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = if (isFavorite) "Remove favorite" else "Add favorite",
            tint = if (isFavorite) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
            },
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Prev / Next Element Navigation Strip
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          val prevElement = PeriodicTableEngine.byAtomicNumber(element.atomicNumber - 1)
          val nextElement = PeriodicTableEngine.byAtomicNumber(element.atomicNumber + 1)

          TextButton(
            onClick = { if (prevElement != null) onNavigateElement(prevElement.atomicNumber) },
            enabled = prevElement != null,
            shape = CircleShape,
            modifier = Modifier.testTag("dialog_prev_element_button"),
          ) {
            Text(
              text = if (prevElement != null) "← #${prevElement.atomicNumber} ${prevElement.symbol}" else "←",
              style = MaterialTheme.typography.labelMedium,
            )
          }

          Surface(
            shape = RoundedCornerShape(20.dp),
            color = visualColors.container,
          ) {
            Text(
              text = "${phaseAtTemp.label}${if (element.isRadioactive) " • ☢ Radioactive" else ""}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = visualColors.onContainer,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
          }

          TextButton(
            onClick = { if (nextElement != null) onNavigateElement(nextElement.atomicNumber) },
            enabled = nextElement != null,
            shape = CircleShape,
            modifier = Modifier.testTag("dialog_next_element_button"),
          ) {
            Text(
              text = if (nextElement != null) "#${nextElement.atomicNumber} ${nextElement.symbol} →" else "→",
              style = MaterialTheme.typography.labelMedium,
            )
          }
        }

        // Context Preview & Bohr Electron Shell Diagram Card
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              BohrElectronShellDiagram(
                element = element,
                accentColor = visualColors.accent,
                modifier = Modifier.size(104.dp),
              )
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Text(
                  text = "Electron Shells (${element.electronShells.size} shells)",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = element.shellsFormatted,
                  style = MaterialTheme.typography.bodyMedium,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.primary,
                )
                Text(
                  text = element.electronConfiguration,
                  style = MaterialTheme.typography.bodySmall,
                  fontFamily = FontFamily.Monospace,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                  text = "Oxidation: ${element.oxidationStates}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            Text(
              text = element.summary,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }

        // Structured Properties Table Card
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            ElementPropertyRow("Atomic Number (Z)", element.atomicNumber.toString(), monospace = true)
            ElementPropertyRow("Atomic Mass", element.formattedAtomicMass, monospace = true)
            ElementPropertyRow("Period & Group", "Period ${element.period} • ${element.groupLabel}")
            ElementPropertyRow("Orbital Block", element.block.label)
            ElementPropertyRow("Phase at STP (25 °C)", element.standardPhase.label)
            ElementPropertyRow(
              "Melting Point",
              "${tempUnit.formatKelvin(element.meltingPointK)} (${ElementTempUnit.KELVIN.formatKelvin(element.meltingPointK)})",
              monospace = true,
            )
            ElementPropertyRow(
              "Boiling Point",
              "${tempUnit.formatKelvin(element.boilingPointK)} (${ElementTempUnit.KELVIN.formatKelvin(element.boilingPointK)})",
              monospace = true,
            )
            ElementPropertyRow(
              "Density",
              element.density?.let { "$it g/cm³" } ?: "—",
              monospace = true,
            )
            ElementPropertyRow(
              "Electronegativity",
              element.electronegativity?.let { "$it (Pauling)" } ?: "—",
              monospace = true,
            )
            ElementPropertyRow(
              "Atomic Radius",
              element.atomicRadiusPm?.let { "$it pm" } ?: "—",
              monospace = true,
            )
            ElementPropertyRow(
              "1st Ionization Energy",
              element.ionizationEnergyKjMol?.let { "$it kJ/mol" } ?: "—",
              monospace = true,
            )
            ElementPropertyRow("Discovered", "${element.discoveryYear} • ${element.discoveredBy}")
          }
        }

        // Cross-Tool Actions Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          FilledTonalButton(
            onClick = onSaveToNotes,
            shape = CircleShape,
            modifier = Modifier
              .weight(1f)
              .testTag("dialog_save_to_notes_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.NoteAdd,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save Note", maxLines = 1)
          }

          OutlinedButton(
            onClick = onShareSummary,
            shape = CircleShape,
            modifier = Modifier
              .weight(1f)
              .testTag("dialog_share_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Share", maxLines = 1)
          }
        }

        if (onOpenInCalculator != null || onOpenMeltingPointInConverter != null) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            if (onOpenInCalculator != null) {
              OutlinedButton(
                onClick = onOpenInCalculator,
                shape = CircleShape,
                modifier = Modifier
                  .weight(1f)
                  .testTag("dialog_open_calculator_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.Calculate,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Mass → Calc", maxLines = 1)
              }
            }
            if (onOpenMeltingPointInConverter != null) {
              OutlinedButton(
                onClick = onOpenMeltingPointInConverter,
                shape = CircleShape,
                modifier = Modifier
                  .weight(1f)
                  .testTag("dialog_open_converter_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.Straighten,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Melt → Units", maxLines = 1)
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onCopySummary,
        shape = CircleShape,
        modifier = Modifier.testTag("dialog_copy_summary_button"),
      ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Copy Data")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = CircleShape,
        modifier = Modifier.testTag("dialog_close_button"),
      ) {
        Text("Close")
      }
    },
  )
}

@Composable
private fun ElementPropertyRow(
  label: String,
  value: String,
  monospace: Boolean = false,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.weight(0.44f),
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.End,
      modifier = Modifier.weight(0.56f),
    )
  }
}

/**
 * 2D Bohr model canvas rendering concentric electron shells and distributed valence/core electrons around the nucleus.
 */
@Composable
private fun BohrElectronShellDiagram(
  element: ChemicalElement,
  accentColor: Color,
  modifier: Modifier = Modifier,
) {
  val ringColor = MaterialTheme.colorScheme.outlineVariant
  val nucleusColor = accentColor
  val electronColor = MaterialTheme.colorScheme.primary

  Canvas(modifier = modifier) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val maxRadius = (minOf(size.width, size.height) / 2f) - 6.dp.toPx()
    val nucleusRadius = 10.dp.toPx()

    // Draw nucleus
    drawCircle(
      color = nucleusColor,
      radius = nucleusRadius,
      center = center,
    )

    val shells = element.electronShells
    if (shells.isEmpty()) return@Canvas

    val minRingRadius = nucleusRadius + 7.dp.toPx()
    val ringStep = if (shells.size > 1) {
      (maxRadius - minRingRadius) / (shells.size - 1).toFloat()
    } else {
      0f
    }

    shells.forEachIndexed { shellIdx, electronCount ->
      val radius = if (shells.size == 1) {
        (minRingRadius + maxRadius) / 2f
      } else {
        minRingRadius + shellIdx * ringStep
      }

      drawCircle(
        color = ringColor,
        radius = radius,
        center = center,
        style = Stroke(width = 1.dp.toPx()),
      )

      val dotRadius = if (shells.size >= 6) 2.2.dp.toPx() else 2.8.dp.toPx()
      for (e in 0 until electronCount) {
        val angleRad = (2.0 * PI * e / electronCount.toDouble()) - (PI / 2.0)
        val ex = center.x + (radius * cos(angleRad)).toFloat()
        val ey = center.y + (radius * sin(angleRad)).toFloat()
        drawCircle(
          color = electronColor,
          radius = dotRadius,
          center = Offset(ex, ey),
        )
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun ElementsScreenPreview() {
  ToolboxTheme {
    ElementsScreen()
  }
}
