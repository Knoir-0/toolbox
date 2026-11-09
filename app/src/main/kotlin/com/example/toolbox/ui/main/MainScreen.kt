package com.example.toolbox.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.QuestionMark
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation3.runtime.NavKey
import com.example.toolbox.ui.theme.ToolboxTheme
import com.example.toolbox.ui.tools.BluetoothRemoteScreen
import com.example.toolbox.ui.tools.CalculatorScreen
import com.example.toolbox.ui.tools.ColorPickerScreen
import com.example.toolbox.ui.tools.DiceRollerScreen
import com.example.toolbox.ui.tools.GuitarTunerScreen
import com.example.toolbox.ui.tools.ImageEditorScreen
import com.example.toolbox.ui.tools.MetronomeScreen
import com.example.toolbox.ui.tools.RulerScreen
import com.example.toolbox.ui.tools.SettingsScreen
import com.example.toolbox.ui.tools.UnitConverterScreen
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

const val MIN_TOOLS_PER_ROW = 3
const val MAX_TOOLS_PER_ROW = 6
const val DEFAULT_TOOLS_PER_ROW = 3
const val ROWS_PER_PAGE_EXPANDED = 2
const val ROWS_PER_PAGE_COLLAPSED = 1

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
  onItemClick: (NavKey) -> Unit = {},
  modifier: Modifier = Modifier,
  preferencesRepository: NavigationPreferencesRepository = rememberNavigationPreferencesRepository(),
) {
  val themeMode by preferencesRepository.themeModeFlow.collectAsState()
  val themeColor by preferencesRepository.themeColorFlow.collectAsState()
  val toolOrder by preferencesRepository.toolOrderFlow.collectAsState()
  val hiddenTools by preferencesRepository.hiddenToolsFlow.collectAsState()

  val firstVisible = remember(toolOrder, hiddenTools) {
    toolOrder.firstOrNull { it !in hiddenTools } ?: Tool.SETTINGS
  }

  var selectedTool by rememberSaveable { mutableStateOf(firstVisible) }

  ToolboxTheme(
    themeMode = themeMode,
    themeColor = themeColor,
  ) {
    ToolboxScaffold(
      selectedTool = selectedTool,
      onSelectTool = { selectedTool = it },
      preferencesRepository = preferencesRepository,
      modifier = modifier,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolboxScaffold(
  selectedTool: Tool,
  onSelectTool: (Tool) -> Unit,
  modifier: Modifier = Modifier,
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

  val allOrderedTools = remember(initialTools, persistedToolOrder) {
    val base = initialTools ?: persistedToolOrder
    base.filter { it != Tool.SETTINGS } + Tool.SETTINGS
  }

  val visibleTools = remember(allOrderedTools, hiddenTools) {
    allOrderedTools.filter { it !in hiddenTools || it == Tool.SETTINGS }
  }

  // Auto-switch selectedTool if it becomes hidden
  LaunchedEffect(selectedTool, visibleTools) {
    if (selectedTool in hiddenTools && selectedTool != Tool.SETTINGS) {
      onSelectTool(visibleTools.firstOrNull() ?: Tool.SETTINGS)
    }
  }

  val toolsPerRow = coerceToolsPerRow(persistedToolsPerRow)
  var isDraggingTool by remember { mutableStateOf(false) }

  val sheetState = rememberStandardBottomSheetState(
    initialValue = initialSheetValue,
    skipHiddenState = true,
  )
  val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
  val coroutineScope = rememberCoroutineScope()

  // Native NavigationBar height (80.dp) + compact pull-up handle (28.dp) + bottom clearance + navigation bar insets
  val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
  val sheetPeekHeight = 118.dp + navBarBottom
  val isExpanded = sheetState.currentValue == SheetValue.Expanded ||
    sheetState.targetValue == SheetValue.Expanded

  BottomSheetScaffold(
    modifier = modifier.fillMaxSize(),
    scaffoldState = scaffoldState,
    sheetPeekHeight = sheetPeekHeight,
    sheetSwipeEnabled = !isDraggingTool,
    sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    sheetContainerColor = NavigationBarDefaults.containerColor,
    sheetTonalElevation = NavigationBarDefaults.Elevation,
    sheetShadowElevation = 8.dp,
    sheetDragHandle = null,
    sheetContent = {
      ToolsPullUpPanelContent(
        tools = visibleTools,
        selectedTool = selectedTool,
        toolsPerRow = toolsPerRow,
        isExpanded = isExpanded,
        onTogglePanel = {
          coroutineScope.launch {
            if (sheetState.currentValue == SheetValue.Expanded) {
              sheetState.partialExpand()
            } else {
              sheetState.expand()
            }
          }
        },
        onSelectTool = onSelectTool,
        onToolsPerRowChange = { newCount ->
          val coerced = coerceToolsPerRow(newCount)
          preferencesRepository?.setToolsPerRow(coerced)
        },
        onReorderTools = { fromIndex, toIndex ->
          val reordered = reorderTools(visibleTools, fromIndex, toIndex)
          val merged = mergeReorderedVisible(allOrderedTools, reordered)
          preferencesRepository?.setToolOrder(merged)
        },
        onDraggingChanged = { dragging ->
          isDraggingTool = dragging
        },
      )
    },
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding(),
      contentAlignment = Alignment.Center,
    ) {
      ToolContentView(
        tool = selectedTool,
        bottomPadding = innerPadding.calculateBottomPadding(),
        preferencesRepository = preferencesRepository,
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
  onDraggingChanged: (Boolean) -> Unit = {},
  modifier: Modifier = Modifier,
) {
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
        .height(26.dp)
        .clickable(onClick = onTogglePanel)
        .testTag("pull_up_panel_toggle")
        .semantics {
          contentDescription = if (isExpanded) "Collapse tools panel" else "Pull up tools panel"
        },
      contentAlignment = Alignment.Center,
    ) {
      Surface(
        modifier = Modifier
          .width(32.dp)
          .height(4.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
      ) {}
    }

    // Native NavigationBar-style Reorderable Grid of Tool Icons
    ReorderableToolsGrid(
      tools = tools,
      selectedTool = selectedTool,
      toolsPerRow = coerceToolsPerRow(toolsPerRow),
      isExpanded = isExpanded,
      onSelectTool = onSelectTool,
      onReorderTools = onReorderTools,
      onDraggingChanged = onDraggingChanged,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 4.dp),
    )

    // Row controls (slider with dots 3-6 + ? help button) are hidden until pulled up
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
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
      )
    } else {
      // Reserve off-screen height so BottomSheetScaffold always has an Expanded anchor
      Spacer(modifier = Modifier.height(88.dp))
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
  modifier: Modifier = Modifier,
) {
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
      // Grid icon + current count (replaces "Per row" text)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Icon(
          imageVector = Icons.Default.GridView,
          contentDescription = "Tools per row",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp),
        )
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
          text = "Touch and hold any tool icon, then drag and drop it to rearrange your toolbox. The first row stays visible in the bottom navigation bar.",
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
  onSelectTool: (Tool) -> Unit,
  onReorderTools: (fromIndex: Int, toIndex: Int) -> Unit,
  onDraggingChanged: (Boolean) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val effectiveColumns = coerceToolsPerRow(toolsPerRow)
  val rowCount = calculateRowCount(tools.size, effectiveColumns)
  val expandedPageCount = calculateExpandedPageCount(rowCount)

  val currentTools by rememberUpdatedState(tools)
  val currentOnReorderTools by rememberUpdatedState(onReorderTools)
  val currentOnDraggingChanged by rememberUpdatedState(onDraggingChanged)
  val hapticFeedback = LocalHapticFeedback.current
  val coroutineScope = rememberCoroutineScope()

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

  val draggedIndex = draggedTool?.let { currentTools.indexOf(it) } ?: -1

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

  Column(
    modifier = modifier
      .testTag("tools_grid")
      .onGloballyPositioned { coords ->
        gridCoordinates = coords
      }
      .pointerInput(effectiveColumns, isExpanded) {
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
          },
          onDragEnd = {
            draggedTool = null
            dragPointerOffset = Offset.Zero
            grabOffsetFromCenter = Offset.Zero
            currentOnDraggingChanged(false)
          },
          onDragCancel = {
            draggedTool = null
            dragPointerOffset = Offset.Zero
            grabOffsetFromCenter = Offset.Zero
            currentOnDraggingChanged(false)
          },
        )
      },
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
          dragPointerOffset = dragPointerOffset,
          grabOffsetFromCenter = grabOffsetFromCenter,
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
          dragPointerOffset = dragPointerOffset,
          grabOffsetFromCenter = grabOffsetFromCenter,
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
}

@Composable
private fun ToolsPageRows(
  rows: IntRange,
  effectiveColumns: Int,
  tools: List<Tool>,
  draggedIndex: Int,
  draggedTool: Tool?,
  dragPointerOffset: Offset,
  grabOffsetFromCenter: Offset,
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
            val slotRect = slotBounds[slotIndex]

            val translationOffset = if (isDragging && slotRect != null) {
              val desiredCenter = dragPointerOffset - grabOffsetFromCenter
              desiredCenter - slotRect.center
            } else {
              Offset.Zero
            }

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
                isDragging = isDragging,
                translationOffset = translationOffset,
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
  translationOffset: Offset,
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
      .testTag("tool_item_${tool.name}")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolContentView(
  tool: Tool,
  bottomPadding: Dp = 0.dp,
  modifier: Modifier = Modifier,
  preferencesRepository: NavigationPreferencesRepository? = null,
) {
  when (tool) {
    Tool.DICE_ROLLER -> DiceRollerScreen(modifier = modifier.padding(bottom = bottomPadding))
    Tool.UNIT_CONVERTER -> UnitConverterScreen(modifier = modifier.padding(bottom = bottomPadding))
    Tool.GUITAR_TUNER -> GuitarTunerScreen(modifier = modifier.padding(bottom = bottomPadding))
    Tool.METRONOME -> MetronomeScreen(modifier = modifier.padding(bottom = bottomPadding))
    Tool.COLOR_PICKER -> ColorPickerScreen(modifier = modifier.padding(bottom = bottomPadding))
    Tool.RULER -> RulerScreen(modifier = modifier.padding(bottom = bottomPadding))
    Tool.BLUETOOTH_REMOTE -> BluetoothRemoteScreen(modifier = modifier.padding(bottom = bottomPadding))
    Tool.CALCULATOR -> CalculatorScreen(bottomPadding = bottomPadding, modifier = modifier)
    Tool.IMAGE_EDITOR -> ImageEditorScreen(modifier = modifier.padding(bottom = bottomPadding))
    Tool.SETTINGS -> SettingsScreen(
      preferencesRepository = preferencesRepository ?: rememberNavigationPreferencesRepository(),
      modifier = modifier.padding(bottom = bottomPadding),
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
  ToolboxTheme {
    ToolboxScaffold(
      selectedTool = Tool.DICE_ROLLER,
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
      selectedTool = Tool.DICE_ROLLER,
      onSelectTool = {},
      initialSheetValue = SheetValue.PartiallyExpanded,
    )
  }
}
