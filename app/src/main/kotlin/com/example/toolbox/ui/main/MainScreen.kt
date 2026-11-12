package com.example.toolbox.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation3.runtime.NavKey
import com.example.toolbox.ui.theme.ToolboxTheme
import com.example.toolbox.ui.tools.ArchiveScreen
import com.example.toolbox.ui.tools.AudioEditorScreen
import com.example.toolbox.ui.tools.AudioRecorderScreen
import com.example.toolbox.ui.tools.BluetoothGamepadScreen
import com.example.toolbox.ui.tools.BluetoothRemoteScreen
import com.example.toolbox.ui.tools.CalculatorScreen
import com.example.toolbox.ui.tools.CameraScreen
import com.example.toolbox.ui.tools.ChordBookScreen
import com.example.toolbox.ui.tools.ColorPickerScreen
import com.example.toolbox.ui.tools.DecibelMeterScreen
import com.example.toolbox.ui.tools.DiceRollerScreen
import com.example.toolbox.ui.tools.FileConverterScreen
import com.example.toolbox.ui.tools.FilesScreen
import com.example.toolbox.ui.tools.GalleryScreen
import java.io.File
import com.example.toolbox.ui.tools.GuitarTunerScreen
import com.example.toolbox.ui.tools.ImageEditorScreen
import com.example.toolbox.ui.tools.MetronomeScreen
import com.example.toolbox.ui.tools.MusicPlayerScreen
import com.example.toolbox.ui.tools.NotesScreen
import com.example.toolbox.ui.tools.PlantScheduleScreen
import com.example.toolbox.ui.tools.RulerScreen
import com.example.toolbox.ui.tools.SettingsScreen
import com.example.toolbox.ui.tools.TodoListScreen
import com.example.toolbox.ui.tools.UnitConverterScreen
import com.example.toolbox.ui.tools.VideoEditorScreen
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

const val MIN_TOOLS_PER_ROW = 3
const val MAX_TOOLS_PER_ROW = 6
const val DEFAULT_TOOLS_PER_ROW = 3
const val ROWS_PER_PAGE_EXPANDED = 2
const val ROWS_PER_PAGE_COLLAPSED = 1
const val PAGE_HOVER_EDGE_DWELL_MS = 400L
const val PAGE_HOVER_REPEAT_DELAY_MS = 700L
val TOOL_PANEL_HANDLE_HEIGHT = 26.dp

/**
 * Defines the background style used by each tool screen.
 */
enum class ToolBackgroundStyle {
  SURFACE_CONTAINER_LOWEST,
  SURFACE_CONTAINER,
  BACKGROUND,
}

/**
 * Returns the [ToolBackgroundStyle] for the given [tool].
 * [Tool.RULER] uses [ToolBackgroundStyle.SURFACE_CONTAINER_LOWEST] to provide high contrast
 * for screen edge measurements.
 * [Tool.TODO_LIST] uses [ToolBackgroundStyle.SURFACE_CONTAINER] so the background behind
 * the toolbox navigation drawer matches the docked quick-add bar above it.
 * Other tools use [ToolBackgroundStyle.BACKGROUND].
 */
fun toolBackgroundStyle(tool: Tool): ToolBackgroundStyle = when (tool) {
  Tool.RULER -> ToolBackgroundStyle.SURFACE_CONTAINER_LOWEST
  Tool.TODO_LIST -> ToolBackgroundStyle.SURFACE_CONTAINER
  else -> ToolBackgroundStyle.BACKGROUND
}

/**
 * Returns the matching background color for a given [tool] so that the background
 * behind the toolbox navigation drawer seamlessly matches the tool screen above it.
 */
@Composable
fun toolBackgroundColor(tool: Tool): Color {
  return when (toolBackgroundStyle(tool)) {
    ToolBackgroundStyle.SURFACE_CONTAINER_LOWEST -> MaterialTheme.colorScheme.surfaceContainerLowest
    ToolBackgroundStyle.SURFACE_CONTAINER -> MaterialTheme.colorScheme.surfaceContainer
    ToolBackgroundStyle.BACKGROUND -> MaterialTheme.colorScheme.background
  }
}

/**
 * Ensures the tools-per-row count is within [MIN_TOOLS_PER_ROW]..[MAX_TOOLS_PER_ROW].
 */
fun coerceToolsPerRow(count: Int): Int = count.coerceIn(MIN_TOOLS_PER_ROW, MAX_TOOLS_PER_ROW)

/**
 * Calculates total rows required for [toolCount] items given [toolsPerRow].
 */
fun calculateRowCount(toolCount: Int, toolsPerRow: Int): Int {
  val columns = coerceToolsPerRow(toolsPerRow)
  return maxOf(1, (toolCount + columns - 1) / columns)
}

/**
 * Calculates total pages required when the navigation panel is opened,
 * paginating after [ROWS_PER_PAGE_EXPANDED] (3) rows.
 */
fun calculateExpandedPageCount(rowCount: Int): Int {
  return maxOf(1, (rowCount + ROWS_PER_PAGE_EXPANDED - 1) / ROWS_PER_PAGE_EXPANDED)
}

/**
 * Calculates the target slot index for a tool being dragged into [targetPage].
 *
 * When dragging forward ([direction] > 0), the tool enters at the beginning of [targetPage].
 * When dragging backward ([direction] < 0), the tool enters at the end of [targetPage].
 *
 * The returned index is clamped so it never exceeds [settingsIndex] - 1 (Settings cannot be moved).
 */
fun calculatePageTransitionTargetIndex(
  targetPage: Int,
  direction: Int,
  rowsPerPage: Int,
  effectiveColumns: Int,
  totalTools: Int,
  settingsIndex: Int = totalTools - 1,
): Int {
  val startRow = targetPage * rowsPerPage
  val startIndex = startRow * effectiveColumns
  val rowCount = calculateRowCount(totalTools, effectiveColumns)
  val endRow = minOf(startRow + rowsPerPage, rowCount)
  val endIndex = minOf(endRow * effectiveColumns, totalTools)

  val maxAllowedIndex = if (settingsIndex >= 0) settingsIndex - 1 else totalTools - 1
  val candidateIndex = if (direction > 0) {
    startIndex
  } else {
    maxOf(startIndex, endIndex - 1)
  }
  return candidateIndex.coerceIn(0, maxOf(0, maxAllowedIndex))
}

/**
 * Returns a new list with the tool at [fromIndex] moved to [toIndex].
 * [Tool.SETTINGS] cannot be re-arranged and is always maintained at the end.
 */
fun reorderTools(
  tools: List<Tool>,
  fromIndex: Int,
  toIndex: Int,
): List<Tool> {
  if (fromIndex !in tools.indices || toIndex !in tools.indices || fromIndex == toIndex) {
    return tools
  }
  // Settings cannot be moved, and nothing can be moved onto Settings
  if (tools[fromIndex] == Tool.SETTINGS || tools[toIndex] == Tool.SETTINGS) {
    return tools
  }
  val mutable = tools.toMutableList()
  val moved = mutable.removeAt(fromIndex)
  mutable.add(toIndex, moved)
  // Ensure Settings remains the last tool if present
  if (mutable.contains(Tool.SETTINGS)) {
    mutable.remove(Tool.SETTINGS)
    mutable.add(Tool.SETTINGS)
  }
  return mutable.toList()
}

/**
 * Merges a reordered subset of visible tools back into the full tool list,
 * preserving hidden tools in their positions, and guaranteeing [Tool.SETTINGS] is last.
 */
fun mergeReorderedVisible(
  allTools: List<Tool>,
  reorderedVisible: List<Tool>,
): List<Tool> {
  var visibleIndex = 0
  val merged = allTools.map { tool ->
    if (tool in reorderedVisible) {
      reorderedVisible[visibleIndex++]
    } else {
      tool
    }
  }
  return merged.filter { it != Tool.SETTINGS } + Tool.SETTINGS
}

/**
 * Resolves the target [SheetValue] for the tool panel after a vertical drag gesture finishes.
 *
 * - [SheetValue.Hidden] represents the panel dragged to the bottom of the screen, leaving only
 *   the drag handle visible ([minimizedHeightPx]).
 * - [SheetValue.PartiallyExpanded] represents the single-row collapsed panel ([collapsedHeightPx]).
 * - [SheetValue.Expanded] represents the multi-row expanded panel with controls ([expandedHeightPx]).
 */
@OptIn(ExperimentalMaterial3Api::class)
fun resolveToolPanelSheetValue(
  startValue: SheetValue,
  currentHeightPx: Float,
  minimizedHeightPx: Float,
  collapsedHeightPx: Float,
  expandedHeightPx: Float,
  minDragThresholdPx: Float,
): SheetValue {
  val safeExpandedPx = maxOf(expandedHeightPx, collapsedHeightPx)
  val safeCollapsedPx = collapsedHeightPx.coerceIn(minimizedHeightPx, safeExpandedPx)
  val startHeightPx = when (startValue) {
    SheetValue.Hidden -> minimizedHeightPx
    SheetValue.PartiallyExpanded -> safeCollapsedPx
    SheetValue.Expanded -> safeExpandedPx
  }
  val clampedCurrentPx = currentHeightPx.coerceIn(minimizedHeightPx, safeExpandedPx)
  val effectiveDragDeltaY = startHeightPx - clampedCurrentPx

  if (effectiveDragDeltaY >= minDragThresholdPx) {
    return when (startValue) {
      SheetValue.Expanded -> {
        val collapseToHiddenMidpoint = (safeCollapsedPx + minimizedHeightPx) / 2f
        if (clampedCurrentPx <= collapseToHiddenMidpoint) {
          SheetValue.Hidden
        } else {
          SheetValue.PartiallyExpanded
        }
      }
      SheetValue.PartiallyExpanded, SheetValue.Hidden -> SheetValue.Hidden
    }
  }

  if (effectiveDragDeltaY <= -minDragThresholdPx) {
    return when (startValue) {
      SheetValue.Hidden -> {
        val expandMidpoint = (safeCollapsedPx + safeExpandedPx) / 2f
        if (clampedCurrentPx >= expandMidpoint) {
          SheetValue.Expanded
        } else {
          SheetValue.PartiallyExpanded
        }
      }
      SheetValue.PartiallyExpanded, SheetValue.Expanded -> SheetValue.Expanded
    }
  }

  val distToHidden = kotlin.math.abs(clampedCurrentPx - minimizedHeightPx)
  val distToCollapsed = kotlin.math.abs(clampedCurrentPx - safeCollapsedPx)
  val distToExpanded = kotlin.math.abs(clampedCurrentPx - safeExpandedPx)
  return when (minOf(distToHidden, distToCollapsed, distToExpanded)) {
    distToHidden -> SheetValue.Hidden
    distToExpanded -> SheetValue.Expanded
    else -> SheetValue.PartiallyExpanded
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit = {},
  standaloneTool: Tool? = null,
  initialDirectory: File? = null,
  modifier: Modifier = Modifier,
  preferencesRepository: NavigationPreferencesRepository = rememberNavigationPreferencesRepository(),
) {
  val themeMode by preferencesRepository.themeModeFlow.collectAsState()
  val themeColor by preferencesRepository.themeColorFlow.collectAsState()
  val accentColor by preferencesRepository.accentColorFlow.collectAsState()

  ToolboxTheme(
    themeMode = themeMode,
    themeColor = themeColor,
    accentColor = accentColor,
  ) {
    if (standaloneTool != null) {
      StandaloneToolScreen(
        tool = standaloneTool,
        initialDirectory = initialDirectory,
        preferencesRepository = preferencesRepository,
        modifier = modifier,
      )
    } else {
      val toolOrder by preferencesRepository.toolOrderFlow.collectAsState()
      val hiddenTools by preferencesRepository.hiddenToolsFlow.collectAsState()

      val firstVisible = remember(toolOrder, hiddenTools, initialDirectory) {
        if (initialDirectory != null) {
          Tool.FILES
        } else {
          toolOrder.firstOrNull { it !in hiddenTools } ?: Tool.SETTINGS
        }
      }

      var selectedTool by rememberSaveable { mutableStateOf(if (initialDirectory != null) Tool.FILES else firstVisible) }

      ToolboxScaffold(
        selectedTool = selectedTool,
        onSelectTool = { selectedTool = it },
        initialDirectory = initialDirectory,
        preferencesRepository = preferencesRepository,
        modifier = modifier,
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolboxScaffold(
  selectedTool: Tool,
  onSelectTool: (Tool) -> Unit,
  modifier: Modifier = Modifier,
  initialDirectory: File? = null,
  initialTools: List<Tool>? = null,
  initialToolsPerRow: Int? = null,
  initialSheetValue: SheetValue = SheetValue.PartiallyExpanded,
  preferencesRepository: NavigationPreferencesRepository? = rememberNavigationPreferencesRepository(),
) {
  val persistedToolOrder by preferencesRepository?.toolOrderFlow?.collectAsState()
    ?: remember { mutableStateOf(initialTools ?: Tool.entries) }
  val hiddenTools by preferencesRepository?.hiddenToolsFlow?.collectAsState()
    ?: remember { mutableStateOf(emptySet()) }
  val persistedToolsPerRow by preferencesRepository?.toolsPerRowFlow?.collectAsState()
    ?: remember { mutableIntStateOf(initialToolsPerRow ?: DEFAULT_TOOLS_PER_ROW) }
  val toolSorting by preferencesRepository?.toolSortingFlow?.collectAsState()
    ?: remember { mutableStateOf(ToolSortingOption.USER_CHOICE) }
  val toolSortOrder by preferencesRepository?.toolSortOrderFlow?.collectAsState()
    ?: remember { mutableStateOf(ToolSortOrder.ASCENDING) }
  val categorySorting by preferencesRepository?.categorySortingFlow?.collectAsState()
    ?: remember { mutableStateOf(CategorySortingOption.MANUAL) }
  val categoryToolSorting by preferencesRepository?.categoryToolSortingFlow?.collectAsState()
    ?: remember { mutableStateOf(CategoryToolSortingOption.MANUAL) }
  val toolUsageCounts by preferencesRepository?.toolUsageCountsFlow?.collectAsState()
    ?: remember { mutableStateOf(emptyMap()) }
  val themeColor by preferencesRepository?.themeColorFlow?.collectAsState()
    ?: remember { mutableStateOf(DEFAULT_THEME_COLOR_SYSTEM) }

  val allOrderedTools = remember(initialTools, persistedToolOrder) {
    val base = initialTools ?: persistedToolOrder
    base.filter { it != Tool.SETTINGS } + Tool.SETTINGS
  }

  val visibleTools = remember(allOrderedTools, hiddenTools) {
    allOrderedTools.filter { it !in hiddenTools || it == Tool.SETTINGS }
  }

  val displayTools = remember(visibleTools, toolSorting, toolSortOrder, toolUsageCounts) {
    when (toolSorting) {
      ToolSortingOption.USAGE -> sortToolsByUsage(visibleTools, toolUsageCounts)
      ToolSortingOption.ALPHABETICAL -> sortToolsAlphabetically(visibleTools, toolSortOrder)
      else -> visibleTools
    }
  }

  // Auto-switch selectedTool if it becomes hidden
  LaunchedEffect(selectedTool, displayTools) {
    if (selectedTool in hiddenTools && selectedTool != Tool.SETTINGS) {
      onSelectTool(displayTools.firstOrNull() ?: Tool.SETTINGS)
    }
  }

  val toolsPerRow = coerceToolsPerRow(persistedToolsPerRow)
  var isDraggingTool by remember { mutableStateOf(false) }

  val density = LocalDensity.current
  // Native NavigationBar height (80.dp) + compact pull-up handle (26.dp) + bottom clearance + navigation bar insets
  val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
  val minimizedPeekHeight = TOOL_PANEL_HANDLE_HEIGHT + navBarBottom
  val basePeekHeight = if (toolSorting == ToolSortingOption.TOOL_CATEGORIES) 130.dp else 118.dp
  val sheetPeekHeight = basePeekHeight + navBarBottom

  val minimizedHeightPx = with(density) { minimizedPeekHeight.toPx() }
  val collapsedHeightPx = with(density) { sheetPeekHeight.toPx() }
  val defaultExpandedHeightPx = with(density) { (284.dp + navBarBottom).toPx() }
  val minDragThresholdPx = with(density) { 12.dp.toPx() }

  var sheetValue by rememberSaveable(initialSheetValue) { mutableStateOf(initialSheetValue) }
  var measuredExpandedHeightPx by remember { mutableFloatStateOf(defaultExpandedHeightPx) }
  val expandedHeightPx = maxOf(measuredExpandedHeightPx, collapsedHeightPx)

  var isDraggingSheet by remember { mutableStateOf(false) }
  var dragStartValue by remember { mutableStateOf(initialSheetValue) }
  var dragStartHeightPx by remember { mutableFloatStateOf(collapsedHeightPx) }
  var dragDeltaSumPx by remember { mutableFloatStateOf(0f) }
  var dragHeightPx by remember { mutableFloatStateOf(collapsedHeightPx) }

  val currentSheetValue by rememberUpdatedState(sheetValue)
  val currentMinimizedHeightPx by rememberUpdatedState(minimizedHeightPx)
  val currentCollapsedHeightPx by rememberUpdatedState(collapsedHeightPx)
  val currentExpandedHeightPx by rememberUpdatedState(expandedHeightPx)

  val sheetDraggableState = rememberDraggableState { deltaY ->
    dragDeltaSumPx += deltaY
    dragHeightPx = (dragStartHeightPx - dragDeltaSumPx)
      .coerceIn(currentMinimizedHeightPx, currentExpandedHeightPx)
  }

  val isExpanded = sheetValue == SheetValue.Expanded ||
    (isDraggingSheet && dragHeightPx > collapsedHeightPx + 1f)
  val isMinimized = !isDraggingSheet && sheetValue == SheetValue.Hidden
  val currentToolBackgroundColor = toolBackgroundColor(selectedTool)

  val currentVisibleSheetHeightDp = when {
    isDraggingSheet -> with(density) { dragHeightPx.toDp() }
    sheetValue == SheetValue.Hidden -> minimizedPeekHeight
    else -> sheetPeekHeight
  }
  val toolContentBottomPadding = minOf(currentVisibleSheetHeightDp, sheetPeekHeight)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(currentToolBackgroundColor),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(currentToolBackgroundColor)
        .statusBarsPadding(),
      contentAlignment = Alignment.Center,
    ) {
      ToolContentView(
        tool = selectedTool,
        bottomPadding = toolContentBottomPadding,
        initialDirectory = initialDirectory,
        preferencesRepository = preferencesRepository,
      )
    }

    val sheetHeightModifier = when {
      isDraggingSheet -> Modifier.height(
        with(density) { dragHeightPx.coerceIn(minimizedHeightPx, expandedHeightPx).toDp() }
      )
      sheetValue == SheetValue.Hidden -> Modifier.height(minimizedPeekHeight)
      sheetValue == SheetValue.PartiallyExpanded -> Modifier.height(sheetPeekHeight)
      else -> Modifier
    }

    Surface(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .then(sheetHeightModifier)
        .onSizeChanged { size ->
          if (sheetValue == SheetValue.Expanded && !isDraggingSheet && size.height > 0) {
            measuredExpandedHeightPx = size.height.toFloat()
          }
        }
        .draggable(
          state = sheetDraggableState,
          orientation = Orientation.Vertical,
          enabled = !isDraggingTool,
          onDragStarted = {
            dragStartValue = currentSheetValue
            dragStartHeightPx = when (currentSheetValue) {
              SheetValue.Hidden -> currentMinimizedHeightPx
              SheetValue.PartiallyExpanded -> currentCollapsedHeightPx
              SheetValue.Expanded -> currentExpandedHeightPx
            }
            dragDeltaSumPx = 0f
            dragHeightPx = dragStartHeightPx
            isDraggingSheet = true
          },
          onDragStopped = {
            val nextValue = resolveToolPanelSheetValue(
              startValue = dragStartValue,
              currentHeightPx = dragHeightPx,
              minimizedHeightPx = currentMinimizedHeightPx,
              collapsedHeightPx = currentCollapsedHeightPx,
              expandedHeightPx = currentExpandedHeightPx,
              minDragThresholdPx = minDragThresholdPx,
            )
            sheetValue = nextValue
            isDraggingSheet = false
            dragDeltaSumPx = 0f
          },
        ),
      shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
      color = NavigationBarDefaults.containerColor,
      tonalElevation = NavigationBarDefaults.Elevation,
      shadowElevation = 8.dp,
    ) {
      ToolsPullUpPanelContent(
        tools = displayTools,
        selectedTool = selectedTool,
        toolsPerRow = toolsPerRow,
        toolSorting = toolSorting,
        categorySorting = categorySorting,
        categoryToolSorting = categoryToolSorting,
        usageCounts = toolUsageCounts,
        themeColor = themeColor,
        isExpanded = isExpanded,
        isMinimized = isMinimized,
        onTogglePanel = {
          sheetValue = when (sheetValue) {
            SheetValue.Expanded -> SheetValue.PartiallyExpanded
            SheetValue.PartiallyExpanded -> SheetValue.Expanded
            SheetValue.Hidden -> SheetValue.PartiallyExpanded
          }
        },
        onSelectTool = { tool ->
          preferencesRepository?.incrementToolUsage(tool)
          onSelectTool(tool)
          if (sheetValue == SheetValue.Expanded) {
            sheetValue = SheetValue.PartiallyExpanded
          }
        },
        onToolsPerRowChange = { newCount ->
          val coerced = coerceToolsPerRow(newCount)
          preferencesRepository?.setToolsPerRow(coerced)
        },
        onReorderTools = { fromIndex, toIndex ->
          val reordered = reorderTools(displayTools, fromIndex, toIndex)
          val merged = mergeReorderedVisible(allOrderedTools, reordered)
          preferencesRepository?.setToolOrder(merged)
        },
        onDraggingChanged = { dragging ->
          isDraggingTool = dragging
        },
      )
    }
  }
}

@Composable
fun ToolsPullUpPanelContent(
  tools: List<Tool>,
  selectedTool: Tool,
  toolsPerRow: Int,
  isExpanded: Boolean,
  onTogglePanel: () -> Unit,
  onSelectTool: (Tool) -> Unit,
  onToolsPerRowChange: (Int) -> Unit,
  onReorderTools: (fromIndex: Int, toIndex: Int) -> Unit,
  toolSorting: ToolSortingOption = ToolSortingOption.USER_CHOICE,
  categorySorting: CategorySortingOption = CategorySortingOption.MANUAL,
  categoryToolSorting: CategoryToolSortingOption = CategoryToolSortingOption.MANUAL,
  usageCounts: Map<Tool, Int> = emptyMap(),
  themeColor: String? = null,
  onAddToHomeScreen: ((Tool) -> Unit)? = null,
  onDraggingChanged: (Boolean) -> Unit = {},
  isMinimized: Boolean = false,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var showReorderHelp by rememberSaveable { mutableStateOf(false) }

  LaunchedEffect(isExpanded) {
    if (!isExpanded) {
      showReorderHelp = false
    }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .testTag("tools_pull_up_panel"),
  ) {
    // Compact pull-up handle above the native-style navigation bar
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(TOOL_PANEL_HANDLE_HEIGHT)
        .clickable(onClick = onTogglePanel)
        .testTag("pull_up_panel_toggle")
        .semantics {
          contentDescription = if (isExpanded) "Collapse tools panel" else "Pull up tools panel"
        },
      contentAlignment = Alignment.Center,
    ) {
      Box(
        modifier = Modifier
          .width(32.dp)
          .height(4.dp)
          .background(
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            shape = CircleShape,
          ),
      )
    }

    if (!isMinimized) {
      // Native NavigationBar-style Reorderable Grid of Tool Icons
      ReorderableToolsGrid(
        tools = tools,
        selectedTool = selectedTool,
        toolsPerRow = coerceToolsPerRow(toolsPerRow),
        isExpanded = isExpanded,
        toolSorting = toolSorting,
        categorySorting = categorySorting,
        categoryToolSorting = categoryToolSorting,
        usageCounts = usageCounts,
        onSelectTool = onSelectTool,
        onReorderTools = onReorderTools,
        onDraggingChanged = onDraggingChanged,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 4.dp),
      )

      // Row controls (add to home screen button, slider with dots 3-6, + ? help button) are hidden until pulled up
      if (isExpanded) {
        HorizontalDivider(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
          color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )

        ToolsPerRowControl(
          toolsPerRow = toolsPerRow,
          showHelp = showReorderHelp,
          onToolsPerRowChange = onToolsPerRowChange,
          onToggleHelp = { showReorderHelp = !showReorderHelp },
          selectedTool = selectedTool,
          onAddToHomeScreen = {
            if (onAddToHomeScreen != null) {
              onAddToHomeScreen(selectedTool)
            } else {
              addToolToHomeScreen(context, selectedTool, themeColor = themeColor)
            }
          },
          toolSorting = toolSorting,
          categorySorting = categorySorting,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsPerRowControl(
  toolsPerRow: Int,
  showHelp: Boolean,
  onToolsPerRowChange: (Int) -> Unit,
  onToggleHelp: () -> Unit,
  selectedTool: Tool = Tool.CALCULATOR,
  onAddToHomeScreen: (() -> Unit)? = null,
  toolSorting: ToolSortingOption = ToolSortingOption.USER_CHOICE,
  categorySorting: CategorySortingOption = CategorySortingOption.MANUAL,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val coercedValue = coerceToolsPerRow(toolsPerRow)
  val stops = (MIN_TOOLS_PER_ROW..MAX_TOOLS_PER_ROW).toList()

  Column(
    modifier = modifier.testTag("tools_per_row_control"),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Add current tool to Home screen button + current count
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        FilledTonalIconButton(
          onClick = {
            if (onAddToHomeScreen != null) {
              onAddToHomeScreen()
            } else {
              addToolToHomeScreen(context, selectedTool)
            }
          },
          modifier = Modifier
            .size(32.dp)
            .testTag("add_to_home_screen_button"),
          colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
          ),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.AddToHomeScreen,
            contentDescription = "Add ${selectedTool.title} to Home screen",
            modifier = Modifier.size(16.dp),
          )
        }
        Text(
          text = coercedValue.toString(),
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.primary,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .width(16.dp)
            .testTag("tools_per_row_count")
            .semantics {
              contentDescription = "$coercedValue tools per row"
            },
        )
      }

      // Slider with colored bubble thumb and dots (3 - 6)
      Column(
        modifier = Modifier.weight(1f),
      ) {
        val activeColor = MaterialTheme.colorScheme.primary
        val inactiveColor = MaterialTheme.colorScheme.surfaceVariant
        val activeDotColor = MaterialTheme.colorScheme.onPrimary
        val inactiveDotColor = MaterialTheme.colorScheme.onSurfaceVariant

        Slider(
          value = coercedValue.toFloat(),
          onValueChange = { newValue ->
            onToolsPerRowChange(coerceToolsPerRow(newValue.roundToInt()))
          },
          valueRange = MIN_TOOLS_PER_ROW.toFloat()..MAX_TOOLS_PER_ROW.toFloat(),
          steps = MAX_TOOLS_PER_ROW - MIN_TOOLS_PER_ROW - 1,
          modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .testTag("tools_per_row_slider"),
          thumb = {
            Surface(
              modifier = Modifier.size(20.dp),
              shape = CircleShape,
              color = MaterialTheme.colorScheme.primary,
              shadowElevation = 3.dp,
            ) {}
          },
          track = { sliderState ->
            Canvas(
              modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            ) {
              val trackStroke = 6.dp.toPx()
              val dotRadius = 3.5.dp.toPx()
              val centerY = size.height / 2f
              val fraction = (sliderState.value - MIN_TOOLS_PER_ROW) /
                (MAX_TOOLS_PER_ROW - MIN_TOOLS_PER_ROW).toFloat()
              val activeEndX = size.width * fraction.coerceIn(0f, 1f)

              // Inactive track
              drawLine(
                color = inactiveColor,
                start = Offset(0f, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = trackStroke,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
              )
              // Active track
              drawLine(
                color = activeColor,
                start = Offset(0f, centerY),
                end = Offset(activeEndX, centerY),
                strokeWidth = trackStroke,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
              )
              // Dots for 3, 4, 5, 6
              stops.forEachIndexed { index, stop ->
                val stopFraction = index.toFloat() / (stops.size - 1).toFloat()
                val dotX = size.width * stopFraction
                val isReached = stop <= coercedValue
                drawCircle(
                  color = if (isReached) activeDotColor else inactiveDotColor,
                  radius = dotRadius,
                  center = Offset(dotX, centerY),
                )
              }
            }
          },
          colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
          ),
        )
      }

      // Little ? button on the right side of the slider
      FilledTonalIconButton(
        onClick = onToggleHelp,
        modifier = Modifier
          .size(32.dp)
          .testTag("reorder_help_button"),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
          containerColor = if (showHelp) {
            MaterialTheme.colorScheme.primaryContainer
          } else {
            MaterialTheme.colorScheme.secondaryContainer
          },
          contentColor = if (showHelp) {
            MaterialTheme.colorScheme.onPrimaryContainer
          } else {
            MaterialTheme.colorScheme.onSecondaryContainer
          },
        ),
      ) {
        Icon(
          imageVector = Icons.Default.QuestionMark,
          contentDescription = "How to rearrange tools",
          modifier = Modifier.size(16.dp),
        )
      }
    }

    // Pull-up / expandable explanation text for drag and drop rearrange
    AnimatedVisibility(
      visible = showHelp,
      enter = expandVertically() + fadeIn(),
      exit = shrinkVertically() + fadeOut(),
    ) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("reorder_help_text"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
      ) {
        Text(
          text = when (toolSorting) {
            ToolSortingOption.TOOL_CATEGORIES ->
              when (categorySorting) {
                CategorySortingOption.MANUAL ->
                  "Tools are grouped by category. Small categories share a single row when their combined tool count fits within the tools-per-row slider setting."
                CategorySortingOption.ALPHABETICAL ->
                  "Tools are grouped alphabetically by category name. Small categories share a single row when their combined tool count fits within the tools-per-row slider setting."
                CategorySortingOption.USAGE ->
                  "Tools are grouped by category and ordered by most frequently used categories first. Small categories share a single row when their combined tool count fits within the tools-per-row slider setting."
              }
            ToolSortingOption.USAGE ->
              "Tools are sorted by how frequently you use them. Switch to Manual in Settings to manually reorder tools."
            ToolSortingOption.USER_CHOICE ->
              "Touch and hold any tool icon, then drag and drop it to rearrange your toolbox. Drag to the edge of the panel to move tools between pages. The first row stays visible in the bottom navigation bar."
            ToolSortingOption.ALPHABETICAL ->
              "Tools are sorted alphabetically. Switch to Manual in Settings to manually reorder tools."
          },
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        )
      }
    }
  }
}

@Composable
fun ReorderableToolsGrid(
  tools: List<Tool>,
  selectedTool: Tool,
  toolsPerRow: Int,
  isExpanded: Boolean = true,
  toolSorting: ToolSortingOption = ToolSortingOption.USER_CHOICE,
  categorySorting: CategorySortingOption = CategorySortingOption.MANUAL,
  categoryToolSorting: CategoryToolSortingOption = CategoryToolSortingOption.MANUAL,
  usageCounts: Map<Tool, Int> = emptyMap(),
  onSelectTool: (Tool) -> Unit,
  onReorderTools: (fromIndex: Int, toIndex: Int) -> Unit,
  onDraggingChanged: (Boolean) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  if (toolSorting == ToolSortingOption.TOOL_CATEGORIES) {
    CategoryToolsGrid(
      tools = tools,
      selectedTool = selectedTool,
      toolsPerRow = toolsPerRow,
      categorySorting = categorySorting,
      categoryToolSorting = categoryToolSorting,
      usageCounts = usageCounts,
      isExpanded = isExpanded,
      onSelectTool = onSelectTool,
      modifier = modifier,
    )
    return
  }

  val effectiveColumns = coerceToolsPerRow(toolsPerRow)
  val rowCount = calculateRowCount(tools.size, effectiveColumns)
  val expandedPageCount = calculateExpandedPageCount(rowCount)

  val currentTools by rememberUpdatedState(tools)
  val currentOnReorderTools by rememberUpdatedState(onReorderTools)
  val currentOnDraggingChanged by rememberUpdatedState(onDraggingChanged)
  val hapticFeedback = LocalHapticFeedback.current
  val coroutineScope = rememberCoroutineScope()
  val density = LocalDensity.current

  val expandedPagerState = rememberPagerState(
    initialPage = 0,
    pageCount = { expandedPageCount },
  )
  val collapsedPagerState = rememberPagerState(
    initialPage = 0,
    pageCount = { rowCount },
  )

  var gridCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
  val slotBounds = remember(effectiveColumns, tools.size, isExpanded) { mutableStateMapOf<Int, Rect>() }

  var draggedTool by remember { mutableStateOf<Tool?>(null) }
  var dragPointerOffset by remember { mutableStateOf(Offset.Zero) }
  var grabOffsetFromCenter by remember { mutableStateOf(Offset.Zero) }
  var draggedSlotSize by remember { mutableStateOf<IntSize?>(null) }
  var edgeHoverDirection by remember { mutableIntStateOf(0) }

  val draggedIndex = draggedTool?.let { currentTools.indexOf(it) } ?: -1

  // Edge hover dwell and auto-page switching during drag
  LaunchedEffect(edgeHoverDirection, draggedTool) {
    if (draggedTool == null || edgeHoverDirection == 0) return@LaunchedEffect

    delay(PAGE_HOVER_EDGE_DWELL_MS)

    while (isActive && draggedTool != null && edgeHoverDirection != 0) {
      val pagerState = if (isExpanded) expandedPagerState else collapsedPagerState
      val pageCount = if (isExpanded) expandedPageCount else rowCount
      val currentPage = pagerState.currentPage
      val targetPage = currentPage + edgeHoverDirection

      if (targetPage in 0 until pageCount && !pagerState.isScrollInProgress) {
        val activeTool = draggedTool ?: break
        val fromIndex = currentTools.indexOf(activeTool)
        val rowsPerPage = if (isExpanded) ROWS_PER_PAGE_EXPANDED else ROWS_PER_PAGE_COLLAPSED
        val settingsIndex = currentTools.indexOf(Tool.SETTINGS)

        val targetIndex = calculatePageTransitionTargetIndex(
          targetPage = targetPage,
          direction = edgeHoverDirection,
          rowsPerPage = rowsPerPage,
          effectiveColumns = effectiveColumns,
          totalTools = currentTools.size,
          settingsIndex = settingsIndex,
        )

        if (fromIndex != -1 && targetIndex != fromIndex) {
          currentOnReorderTools(fromIndex, targetIndex)
        }

        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        pagerState.animateScrollToPage(targetPage)
      } else {
        break
      }

      delay(PAGE_HOVER_REPEAT_DELAY_MS)
    }
  }

  // Synchronize pagers between collapsed and expanded states
  LaunchedEffect(isExpanded) {
    if (isExpanded) {
      val targetExpandedPage = (collapsedPagerState.currentPage / ROWS_PER_PAGE_EXPANDED)
        .coerceIn(0, maxOf(0, expandedPageCount - 1))
      expandedPagerState.scrollToPage(targetExpandedPage)
    } else {
      val selectedIndex = tools.indexOf(selectedTool)
      val selectedRow = if (selectedIndex >= 0) selectedIndex / effectiveColumns else 0
      val expandedPageStartRow = expandedPagerState.currentPage * ROWS_PER_PAGE_EXPANDED
      val expandedPageEndRow = minOf(expandedPageStartRow + ROWS_PER_PAGE_EXPANDED, rowCount)
      val targetRow = if (selectedRow in expandedPageStartRow until expandedPageEndRow) {
        selectedRow
      } else {
        expandedPageStartRow.coerceIn(0, maxOf(0, rowCount - 1))
      }
      collapsedPagerState.scrollToPage(targetRow)
    }
  }

  // When selectedTool changes while collapsed, jump to the row of the selected tool
  LaunchedEffect(selectedTool, isExpanded) {
    if (!isExpanded) {
      val selectedIndex = tools.indexOf(selectedTool)
      if (selectedIndex >= 0) {
        val targetRow = (selectedIndex / effectiveColumns).coerceIn(0, maxOf(0, rowCount - 1))
        if (targetRow != collapsedPagerState.currentPage) {
          collapsedPagerState.scrollToPage(targetRow)
        }
      }
    }
  }

  // Clear slotBounds when page changes so stale coordinates from other pages are not retained
  LaunchedEffect(expandedPagerState.currentPage, collapsedPagerState.currentPage, isExpanded) {
    slotBounds.clear()
  }

  Box(
    modifier = modifier
      .testTag("tools_grid")
      .onGloballyPositioned { coords ->
        gridCoordinates = coords
      }
      .pointerInput(effectiveColumns, isExpanded, toolSorting) {
        if (toolSorting != ToolSortingOption.USER_CHOICE) return@pointerInput
        detectDragGesturesAfterLongPress(
          onDragStart = { startOffset ->
            val hitSlotIndex = findTargetSlotIndex(
              point = startOffset,
              slotBounds = slotBounds,
              itemCount = currentTools.size,
              requireContains = false,
            )
            if (hitSlotIndex != null && hitSlotIndex in currentTools.indices) {
              val toolToDrag = currentTools[hitSlotIndex]
              // Settings cannot be re-arranged
              if (toolToDrag != Tool.SETTINGS) {
                val slotRect = slotBounds[hitSlotIndex]
                draggedTool = toolToDrag
                dragPointerOffset = startOffset
                grabOffsetFromCenter = if (slotRect != null) {
                  startOffset - slotRect.center
                } else {
                  Offset.Zero
                }
                draggedSlotSize = slotRect?.let { IntSize(it.width.roundToInt(), it.height.roundToInt()) }
                edgeHoverDirection = 0
                currentOnDraggingChanged(true)
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
              }
            }
          },
          onDrag = { change, dragAmount ->
            val activeTool = draggedTool ?: return@detectDragGesturesAfterLongPress
            if (activeTool == Tool.SETTINGS) return@detectDragGesturesAfterLongPress
            change.consume()
            dragPointerOffset += dragAmount
            val desiredCenter = dragPointerOffset - grabOffsetFromCenter

            val pagerState = if (isExpanded) expandedPagerState else collapsedPagerState
            val pageCount = if (isExpanded) expandedPageCount else rowCount
            val currentPage = pagerState.currentPage

            val gridWidth = gridCoordinates?.size?.width ?: 1
            val gridHeight = gridCoordinates?.size?.height ?: 1
            val columnWidth = gridWidth.toFloat() / effectiveColumns.toFloat()
            val maxEdgeThresholdPx = with(density) { 48.dp.toPx() }
            val edgeThresholdPx = minOf(maxEdgeThresholdPx, columnWidth * 0.45f)

            val pointerX = dragPointerOffset.x
            val pointerY = dragPointerOffset.y
            val isWithinVerticalBounds = pointerY in (-60f)..(gridHeight.toFloat() + 60f)

            if (isWithinVerticalBounds) {
              if (pointerX <= edgeThresholdPx && currentPage > 0) {
                edgeHoverDirection = -1
              } else if (pointerX >= gridWidth - edgeThresholdPx && currentPage < pageCount - 1) {
                edgeHoverDirection = 1
              } else {
                edgeHoverDirection = 0
              }
            } else {
              edgeHoverDirection = 0
            }

            if (!pagerState.isScrollInProgress) {
              val targetIndex = findTargetSlotIndex(
                point = desiredCenter,
                slotBounds = slotBounds,
                itemCount = currentTools.size,
                requireContains = false,
              )
              val fromIndex = currentTools.indexOf(activeTool)
              val settingsIndex = currentTools.indexOf(Tool.SETTINGS)
              val maxAllowedTargetIndex = if (settingsIndex >= 0) settingsIndex - 1 else currentTools.lastIndex

              if (targetIndex != null && fromIndex != -1) {
                val clampedTarget = targetIndex.coerceIn(0, maxAllowedTargetIndex)
                if (clampedTarget != fromIndex) {
                  currentOnReorderTools(fromIndex, clampedTarget)
                  hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
              }
            }
          },
          onDragEnd = {
            draggedTool = null
            dragPointerOffset = Offset.Zero
            grabOffsetFromCenter = Offset.Zero
            draggedSlotSize = null
            edgeHoverDirection = 0
            currentOnDraggingChanged(false)
          },
          onDragCancel = {
            draggedTool = null
            dragPointerOffset = Offset.Zero
            grabOffsetFromCenter = Offset.Zero
            draggedSlotSize = null
            edgeHoverDirection = 0
            currentOnDraggingChanged(false)
          },
        )
      },
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      if (isExpanded) {
        HorizontalPager(
          state = expandedPagerState,
          userScrollEnabled = draggedTool == null,
          modifier = Modifier.fillMaxWidth(),
        ) { pageIndex ->
          val pageStartRow = pageIndex * ROWS_PER_PAGE_EXPANDED
          val pageEndRow = minOf(pageStartRow + ROWS_PER_PAGE_EXPANDED, rowCount)
          val rowsOnPage = pageStartRow until pageEndRow

          ToolsPageRows(
            rows = rowsOnPage,
            effectiveColumns = effectiveColumns,
            tools = currentTools,
            draggedIndex = draggedIndex,
            draggedTool = draggedTool,
            slotBounds = slotBounds,
            gridCoordinates = gridCoordinates,
            selectedTool = selectedTool,
            onSelectTool = onSelectTool,
          )
        }

        if (expandedPageCount > 1) {
          PanelPageIndicator(
            pageCount = expandedPageCount,
            currentPage = expandedPagerState.currentPage,
            onPageSelected = { page ->
              coroutineScope.launch {
                expandedPagerState.animateScrollToPage(page)
              }
            },
            testTag = "tools_panel_page_indicator",
            modifier = Modifier.padding(top = 4.dp),
          )
        }
      } else {
        HorizontalPager(
          state = collapsedPagerState,
          userScrollEnabled = draggedTool == null,
          modifier = Modifier.fillMaxWidth(),
        ) { rowIndex ->
          ToolsPageRows(
            rows = rowIndex..rowIndex,
            effectiveColumns = effectiveColumns,
            tools = currentTools,
            draggedIndex = draggedIndex,
            draggedTool = draggedTool,
            slotBounds = slotBounds,
            gridCoordinates = gridCoordinates,
            selectedTool = selectedTool,
            onSelectTool = onSelectTool,
          )
        }

        if (rowCount > 1) {
          PanelPageIndicator(
            pageCount = rowCount,
            currentPage = collapsedPagerState.currentPage,
            onPageSelected = { row ->
              coroutineScope.launch {
                collapsedPagerState.animateScrollToPage(row)
              }
            },
            testTag = "tools_panel_row_indicator",
            modifier = Modifier.padding(top = 2.dp),
          )
        }
      }
    }

    // Floating lifted item overlay during drag
    if (draggedTool != null && gridCoordinates != null) {
      val activeTool = draggedTool!!
      val desiredCenter = dragPointerOffset - grabOffsetFromCenter
      val defaultSlotWidthPx = (gridCoordinates?.size?.width ?: 1) / effectiveColumns.toFloat()
      val slotWidthPx = draggedSlotSize?.width?.toFloat() ?: defaultSlotWidthPx
      val slotHeightPx = draggedSlotSize?.height?.toFloat()
        ?: with(density) { 68.dp.toPx() }

      Box(
        modifier = Modifier
          .size(
            with(density) { slotWidthPx.toDp() },
            with(density) { slotHeightPx.toDp() },
          )
          .graphicsLayer {
            translationX = desiredCenter.x - slotWidthPx / 2f
            translationY = desiredCenter.y - slotHeightPx / 2f
          }
          .zIndex(100f),
      ) {
        ToolGridItem(
          tool = activeTool,
          isSelected = activeTool == selectedTool,
          isDragging = true,
          translationOffset = Offset.Zero,
          testTagOverride = "dragged_tool_overlay",
          onClick = {},
        )
      }
    }
  }
}

data class CategoryPage(
  val groups: List<CategoryGroup>,
)

@Composable
fun CategoryToolsGrid(
  tools: List<Tool>,
  selectedTool: Tool,
  toolsPerRow: Int,
  onSelectTool: (Tool) -> Unit,
  categorySorting: CategorySortingOption = CategorySortingOption.MANUAL,
  categoryToolSorting: CategoryToolSortingOption = CategoryToolSortingOption.MANUAL,
  usageCounts: Map<Tool, Int> = emptyMap(),
  isExpanded: Boolean = true,
  modifier: Modifier = Modifier,
) {
  val effectiveColumns = coerceToolsPerRow(toolsPerRow)
  val rawCategories = remember(tools) {
    ToolCategory.entries.mapNotNull { category ->
      val categoryTools = tools.filter { it.category == category }
      if (categoryTools.isNotEmpty()) {
        CategoryGroup(category, categoryTools)
      } else {
        null
      }
    }
  }
  val activeCategories = remember(rawCategories, categorySorting, categoryToolSorting, usageCounts) {
    sortCategoryGroups(rawCategories, categorySorting, categoryToolSorting, usageCounts)
  }

  // Pack categories into pages: multiple categories fit on one page as one row if categories are small enough
  val pages = remember(activeCategories, effectiveColumns) {
    val result = mutableListOf<CategoryPage>()
    var currentGroups = mutableListOf<CategoryGroup>()
    var currentCount = 0

    for (group in activeCategories) {
      if (currentGroups.isEmpty()) {
        currentGroups.add(group)
        currentCount = group.tools.size
      } else if (currentCount + group.tools.size <= effectiveColumns) {
        currentGroups.add(group)
        currentCount += group.tools.size
      } else {
        result.add(CategoryPage(currentGroups.toList()))
        currentGroups = mutableListOf(group)
        currentCount = group.tools.size
      }
    }
    if (currentGroups.isNotEmpty()) {
      result.add(CategoryPage(currentGroups.toList()))
    }
    result
  }

  val coroutineScope = rememberCoroutineScope()
  val categoryPagerState = rememberPagerState(
    initialPage = 0,
    pageCount = { maxOf(1, pages.size) },
  )

  LaunchedEffect(selectedTool, pages) {
    val targetIndex = pages.indexOfFirst { page ->
      page.groups.any { it.category == selectedTool.category }
    }
    if (targetIndex >= 0 && targetIndex != categoryPagerState.currentPage) {
      categoryPagerState.scrollToPage(targetIndex)
    }
  }

  Column(
    modifier = modifier.testTag("tools_grid"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
  ) {
    HorizontalPager(
      state = categoryPagerState,
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) { pageIndex ->
      if (pageIndex in pages.indices) {
        val page = pages[pageIndex]
        val totalPageTools = page.groups.sumOf { it.tools.size }
        val dummySpacers = maxOf(0, effectiveColumns - totalPageTools)
        val sideSpacerWeight = dummySpacers / 2f

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("tools_grid_row_$pageIndex"),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          if (sideSpacerWeight > 0f) {
            Spacer(modifier = Modifier.weight(sideSpacerWeight))
          }

          for ((groupIndex, group) in page.groups.withIndex()) {
            if (groupIndex > 0) {
              Box(
                modifier = Modifier
                  .padding(horizontal = 2.dp)
                  .height(36.dp)
                  .width(1.dp)
                  .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
              )
            }

            val toolRows = group.tools.chunked(effectiveColumns)

            Column(
              modifier = Modifier.weight(minOf(group.tools.size, effectiveColumns).toFloat()),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              // Category Label
              Text(
                text = group.category.title,
                style = if (page.groups.size > 1) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                  .fillMaxWidth()
                  .then(
                    if (groupIndex == 0) Modifier.testTag("category_label") else Modifier
                  )
                  .testTag("category_label_${group.category.name.lowercase()}"),
              )

              // Tools in this Category
              for (rowTools in toolRows) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  val rowDummySpacers = maxOf(0, effectiveColumns - rowTools.size)
                  val rowSideSpacer = if (toolRows.size > 1) rowDummySpacers / 2f else 0f

                  if (rowSideSpacer > 0f) {
                    Spacer(modifier = Modifier.weight(rowSideSpacer))
                  }
                  for (tool in rowTools) {
                    Box(
                      modifier = Modifier.weight(1f),
                      contentAlignment = Alignment.Center,
                    ) {
                      ToolGridItem(
                        tool = tool,
                        isSelected = tool == selectedTool,
                        isDragging = false,
                        translationOffset = Offset.Zero,
                        onClick = { onSelectTool(tool) },
                      )
                    }
                  }
                  if (rowSideSpacer > 0f) {
                    Spacer(modifier = Modifier.weight(rowSideSpacer))
                  }
                }
              }
            }
          }

          if (sideSpacerWeight > 0f) {
            Spacer(modifier = Modifier.weight(sideSpacerWeight))
          }
        }
      }
    }

    if (pages.size > 1) {
      PanelPageIndicator(
        pageCount = pages.size,
        currentPage = categoryPagerState.currentPage,
        onPageSelected = { page ->
          coroutineScope.launch {
            categoryPagerState.animateScrollToPage(page)
          }
        },
        testTag = "tools_panel_page_indicator",
        modifier = Modifier.padding(top = 2.dp),
      )
    }
  }
}

@Composable
private fun ToolsPageRows(
  rows: IntRange,
  effectiveColumns: Int,
  tools: List<Tool>,
  draggedIndex: Int,
  draggedTool: Tool?,
  slotBounds: MutableMap<Int, Rect>,
  gridCoordinates: LayoutCoordinates?,
  selectedTool: Tool,
  onSelectTool: (Tool) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    for (rowIndex in rows) {
      val rowStartIndex = rowIndex * effectiveColumns
      val rowEndIndex = minOf(rowStartIndex + effectiveColumns, tools.size)
      val isRowContainingDragged = draggedIndex in rowStartIndex until rowEndIndex

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .zIndex(if (isRowContainingDragged) 10f else 0f)
          .testTag("tools_grid_row_$rowIndex"),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        for (colIndex in 0 until effectiveColumns) {
          val slotIndex = rowStartIndex + colIndex
          if (slotIndex < tools.size) {
            val tool = tools[slotIndex]
            val isDragging = tool == draggedTool

            Box(
              modifier = Modifier
                .weight(1f)
                .zIndex(if (isDragging) 10f else 0f)
                .onGloballyPositioned { coords ->
                  val gridCoords = gridCoordinates
                  if (gridCoords != null && gridCoords.isAttached && coords.isAttached) {
                    slotBounds[slotIndex] = gridCoords.localBoundingBoxOf(coords, clipBounds = false)
                  }
                },
            ) {
              ToolGridItem(
                tool = tool,
                isSelected = tool == selectedTool,
                isDragging = false,
                translationOffset = Offset.Zero,
                modifier = Modifier.graphicsLayer {
                  if (isDragging) {
                    alpha = 0.25f
                  }
                },
                onClick = {
                  if (draggedTool == null) {
                    onSelectTool(tool)
                  }
                },
              )
            }
          } else {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }
  }
}

@Composable
fun PanelPageIndicator(
  pageCount: Int,
  currentPage: Int,
  onPageSelected: (Int) -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "tools_panel_page_indicator",
) {
  if (pageCount <= 1) return
  Row(
    modifier = modifier
      .fillMaxWidth()
      .testTag(testTag),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    for (i in 0 until pageCount) {
      val isSelected = i == currentPage
      Box(
        modifier = Modifier
          .padding(horizontal = 4.dp, vertical = 2.dp)
          .size(if (isSelected) 7.dp else 5.dp)
          .clip(CircleShape)
          .background(
            if (isSelected) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            },
          )
          .clickable { onPageSelected(i) }
          .testTag("${testTag}_dot_$i"),
      )
    }
  }
}

private fun findTargetSlotIndex(
  point: Offset,
  slotBounds: Map<Int, Rect>,
  itemCount: Int,
  requireContains: Boolean,
): Int? {
  for (index in 0 until itemCount) {
    val rect = slotBounds[index] ?: continue
    if (rect.contains(point)) {
      return index
    }
  }
  if (requireContains) return null

  var closestIndex: Int? = null
  var minDistanceSq = Float.MAX_VALUE
  for (index in 0 until itemCount) {
    val rect = slotBounds[index] ?: continue
    val dx = point.x - rect.center.x
    val dy = point.y - rect.center.y
    val distSq = dx * dx + dy * dy
    if (distSq < minDistanceSq) {
      minDistanceSq = distSq
      closestIndex = index
    }
  }
  return closestIndex
}

@Composable
private fun ToolGridItem(
  tool: Tool,
  isSelected: Boolean,
  isDragging: Boolean,
  translationOffset: Offset = Offset.Zero,
  testTagOverride: String? = null,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val indicatorColor = when {
    isDragging -> MaterialTheme.colorScheme.primaryContainer
    isSelected -> MaterialTheme.colorScheme.secondaryContainer
    else -> Color.Transparent
  }
  val iconTint = when {
    isDragging -> MaterialTheme.colorScheme.onPrimaryContainer
    isSelected -> MaterialTheme.colorScheme.onSecondaryContainer
    else -> MaterialTheme.colorScheme.onSurfaceVariant
  }
  val labelColor = when {
    isDragging -> MaterialTheme.colorScheme.primary
    isSelected -> MaterialTheme.colorScheme.onSurface
    else -> MaterialTheme.colorScheme.onSurfaceVariant
  }

  val interactionSource = remember { MutableInteractionSource() }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .graphicsLayer {
        translationX = translationOffset.x
        translationY = translationOffset.y
        scaleX = if (isDragging) 1.08f else 1f
        scaleY = if (isDragging) 1.08f else 1f
      }
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick,
      )
      .padding(horizontal = 4.dp, vertical = 6.dp)
      .testTag(testTagOverride ?: "tool_item_${tool.name}")
      .semantics {
        role = Role.Tab
        selected = isSelected
      },
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    // Native NavigationBarItem-style active indicator pill around the icon
    Box(
      modifier = Modifier
        .width(56.dp)
        .height(32.dp)
        .clip(CircleShape)
        .background(indicatorColor),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = tool.icon,
        contentDescription = tool.title,
        tint = iconTint,
        modifier = Modifier.size(24.dp),
      )
    }
    Text(
      text = tool.title,
      style = MaterialTheme.typography.labelMedium,
      color = labelColor,
      fontWeight = if (isSelected || isDragging) FontWeight.Bold else FontWeight.Medium,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center,
    )
  }
}

/**
 * Renders a single tool in full-screen standalone mode without any navigation bar or drawer.
 * Used when the tool is launched directly from a home screen shortcut.
 */
@Composable
fun StandaloneToolScreen(
  tool: Tool,
  initialDirectory: File? = null,
  modifier: Modifier = Modifier,
  preferencesRepository: NavigationPreferencesRepository = rememberNavigationPreferencesRepository(),
) {
  LaunchedEffect(tool) {
    preferencesRepository.incrementToolUsage(tool)
  }

  val bgColor = toolBackgroundColor(tool)
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(bgColor)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("standalone_tool_screen")
      .testTag("standalone_tool_screen_${tool.name}"),
    contentAlignment = Alignment.Center,
  ) {
    ToolContentView(
      tool = tool,
      bottomPadding = 0.dp,
      initialDirectory = initialDirectory,
      preferencesRepository = preferencesRepository,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolContentView(
  tool: Tool,
  bottomPadding: Dp = 0.dp,
  initialDirectory: File? = null,
  modifier: Modifier = Modifier,
  preferencesRepository: NavigationPreferencesRepository? = null,
) {
  val bgColor = toolBackgroundColor(tool)
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(bgColor),
  ) {
    when (tool) {
      Tool.DICE_ROLLER -> DiceRollerScreen(modifier = Modifier.padding(bottom = bottomPadding))
      Tool.UNIT_CONVERTER -> UnitConverterScreen(modifier = Modifier.padding(bottom = bottomPadding))
      Tool.GUITAR_TUNER -> GuitarTunerScreen(modifier = Modifier.padding(bottom = bottomPadding))
      Tool.METRONOME -> MetronomeScreen(modifier = Modifier.padding(bottom = bottomPadding))
      Tool.COLOR_PICKER -> ColorPickerScreen(modifier = Modifier.padding(bottom = bottomPadding))
      Tool.RULER -> RulerScreen(modifier = Modifier.padding(bottom = bottomPadding))
      Tool.BLUETOOTH_REMOTE -> BluetoothRemoteScreen(modifier = Modifier.padding(bottom = bottomPadding))
      Tool.CALCULATOR -> CalculatorScreen(bottomPadding = bottomPadding, modifier = Modifier)
      Tool.IMAGE_EDITOR -> ImageEditorScreen(modifier = Modifier.padding(bottom = bottomPadding))
      Tool.TODO_LIST -> TodoListScreen(modifier = Modifier.padding(bottom = bottomPadding))
      Tool.FILES -> {
        val activeThemeColor = preferencesRepository?.themeColorFlow?.collectAsState()?.value
        FilesScreen(
          initialDirectory = initialDirectory,
          themeColor = activeThemeColor,
          onOpenFileConverter = { preferencesRepository?.incrementToolUsage(Tool.FILE_CONVERTER) },
          onOpenImageEditor = { preferencesRepository?.incrementToolUsage(Tool.IMAGE_EDITOR) },
          onOpenVideoEditor = { preferencesRepository?.incrementToolUsage(Tool.VIDEO_EDITOR) },
          onOpenAudioEditor = { preferencesRepository?.incrementToolUsage(Tool.AUDIO_EDITOR) },
          onOpenArchive = { preferencesRepository?.incrementToolUsage(Tool.ARCHIVE) },
          modifier = Modifier.padding(bottom = bottomPadding),
        )
      }
      Tool.AUDIO_RECORDER -> AudioRecorderScreen(
        initialDirectory = initialDirectory,
        onOpenAudioEditor = { preferencesRepository?.incrementToolUsage(Tool.AUDIO_EDITOR) },
        modifier = Modifier.padding(bottom = bottomPadding),
      )
      Tool.FILE_CONVERTER -> FileConverterScreen(
        initialDirectory = initialDirectory,
        modifier = Modifier.padding(bottom = bottomPadding),
      )
      Tool.GALLERY -> GalleryScreen(
        rootDirectory = initialDirectory,
        onOpenImageEditor = { preferencesRepository?.incrementToolUsage(Tool.IMAGE_EDITOR) },
        onOpenVideoEditor = { preferencesRepository?.incrementToolUsage(Tool.VIDEO_EDITOR) },
        modifier = Modifier.padding(bottom = bottomPadding),
      )
      Tool.NOTES -> NotesScreen(
        modifier = Modifier.padding(bottom = bottomPadding),
      )
      Tool.CHORD_BOOK -> ChordBookScreen(
        modifier = Modifier.padding(bottom = bottomPadding),
      )
      Tool.CAMERA -> CameraScreen(
        initialDirectory = initialDirectory,
        onOpenGallery = { preferencesRepository?.incrementToolUsage(Tool.GALLERY) },
        modifier = Modifier.padding(bottom = bottomPadding),
      )
      Tool.AUDIO_EDITOR -> AudioEditorScreen(
        modifier = Modifier.padding(bottom = bottomPadding),
      )
      Tool.VIDEO_EDITOR -> VideoEditorScreen(
        initialDirectory = initialDirectory,
        modifier = Modifier.padding(bottom = bottomPadding),
      )
      Tool.PLANT_SCHEDULE -> PlantScheduleScreen(
        modifier = Modifier.padding(bottom = bottomPadding),
      )
      Tool.BLUETOOTH_GAMEPAD -> BluetoothGamepadScreen(
        modifier = Modifier.padding(bottom = bottomPadding),
      )
      Tool.DECIBEL_METER -> DecibelMeterScreen(
        modifier = Modifier.padding(bottom = bottomPadding),
      )
      Tool.ARCHIVE -> ArchiveScreen(
        initialDirectory = initialDirectory,
        modifier = Modifier.padding(bottom = bottomPadding),
      )
      Tool.MUSIC -> MusicPlayerScreen(
        initialDirectory = initialDirectory,
        modifier = Modifier.padding(bottom = bottomPadding),
      )
      Tool.SETTINGS -> SettingsScreen(
        preferencesRepository = preferencesRepository ?: rememberNavigationPreferencesRepository(),
        modifier = Modifier.padding(bottom = bottomPadding),
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
  ToolboxTheme {
    ToolboxScaffold(
      selectedTool = Tool.CALCULATOR,
      onSelectTool = {},
      initialSheetValue = SheetValue.Expanded,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true, name = "Main Screen - Navigation Collapsed")
@Composable
fun MainScreenCollapsedPreview() {
  ToolboxTheme {
    ToolboxScaffold(
      selectedTool = Tool.CALCULATOR,
      onSelectTool = {},
      initialSheetValue = SheetValue.PartiallyExpanded,
    )
  }
}
