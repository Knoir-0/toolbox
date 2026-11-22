package com.example.toolbox.ui.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import com.example.toolbox.ui.theme.CircleShape
import com.example.toolbox.ui.theme.HorizontalHexagonShape
import com.example.toolbox.ui.theme.currentHexagonIconButtons
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay

/**
 * Configuration state for the integrated expanding search bar inside [ToolboxTopBar].
 * When provided, a Search icon button is rendered at the very far right of the toolbar,
 * and activating search expands a full-width search bar from the right edge without resizing the toolbar.
 */
data class ToolboxTopBarSearchState(
  val isActive: Boolean,
  val query: String,
  val onQueryChange: (String) -> Unit,
  val onOpenSearch: () -> Unit,
  val onCloseSearch: () -> Unit,
  val onClearQuery: () -> Unit = { onQueryChange("") },
  val placeholder: String = "Search...",
  val searchContentDescription: String = "Search",
  val closeContentDescription: String = "Close search",
  val clearContentDescription: String = "Clear search",
  val toggleButtonTestTag: String = "action_toggle_search",
  val searchBarTestTag: String = "toolbar_search_bar",
  val closeButtonTestTag: String = "action_close_search",
  val inputTestTag: String = "search_input",
  val clearButtonTestTag: String = "action_clear_search",
  val onSearchImeAction: (() -> Unit)? = null,
)

/**
 * Configuration state for the multi-selection mode inside [ToolboxTopBar].
 */
data class ToolboxTopBarSelectionState(
  val selectedCount: Int,
  val totalCount: Int,
  val onToggleSelectAll: () -> Unit,
  val onCloseSelection: () -> Unit,
  val rowTestTag: String? = null,
  val closeButtonTestTag: String = "action_close_selection",
  val selectAllRowTestTag: String = "action_select_all_checkbox",
  val checkboxTestTag: String? = null,
  val countTextTestTag: String? = null,
  val actions: @Composable RowScope.() -> Unit,
)

/**
 * Unified 68.dp top toolbar used across Toolbox screens.
 *
 * Follows DESIGN_LANGUAGE.md Section 5.1, 5.5, and 5.7:
 * - Fixed 68.dp height so transitioning between normal, search, and multi-select states never resizes the bar.
 * - 40.dp circular primaryContainer icon badge with 20.dp primary-tinted icon at root level.
 * - Plain 36.dp IconButton with ArrowBack (onSurface tint, no circular container) when navigating back/up.
 * - Title (titleLarge, Bold, onSurface) + Subtext (bodySmall, onSurfaceVariant).
 * - Custom trailing actions followed by the Search icon button at the very far right.
 * - Integrated horizontal expanding search bar (from Alignment.End) and multi-selection bar.
 */
@Composable
fun ToolboxTopBar(
  title: String,
  subtext: String?,
  icon: ImageVector,
  modifier: Modifier = Modifier,
  containerColor: Color = MaterialTheme.colorScheme.surface,
  badgeContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
  badgeIconTint: Color = MaterialTheme.colorScheme.primary,
  subtextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
  onNavigateBack: (() -> Unit)? = null,
  backContentDescription: String = "Back",
  backButtonTestTag: String = "action_navigate_back",
  additionalBackButtonTestTag: String? = null,
  leadingRowModifier: Modifier = Modifier,
  titleTestTag: String? = null,
  subtextTestTag: String? = null,
  searchState: ToolboxTopBarSearchState? = null,
  selectionState: ToolboxTopBarSelectionState? = null,
  actions: @Composable RowScope.() -> Unit = {},
) {
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  val isSearchActive = searchState?.isActive == true
  val isSelectionMode = selectionState != null

  if (searchState != null) {
    LaunchedEffect(isSearchActive) {
      if (isSearchActive) {
        runCatching { focusRequester.requestFocus() }
        delay(80)
        runCatching { focusRequester.requestFocus() }
        keyboardController?.show()
      } else {
        keyboardController?.hide()
      }
    }
  }

  Surface(
    color = containerColor,
    tonalElevation = 1.dp,
    modifier = modifier.fillMaxWidth(),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(68.dp),
      contentAlignment = Alignment.CenterStart,
    ) {
      if (selectionState != null) {
        ToolboxMultiSelectBarRow(selectionState = selectionState)
      } else {
        Row(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            modifier = Modifier
              .weight(1f)
              .then(leadingRowModifier),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            if (onNavigateBack != null) {
              Box(
                modifier = if (additionalBackButtonTestTag != null) {
                  Modifier.testTag(additionalBackButtonTestTag)
                } else {
                  Modifier
                },
              ) {
                IconButton(
                  onClick = onNavigateBack,
                  modifier = Modifier
                    .size(36.dp)
                    .testTag(backButtonTestTag),
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = backContentDescription,
                    tint = MaterialTheme.colorScheme.onSurface,
                  )
                }
              }
            } else {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(badgeContainerColor),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = icon,
                  contentDescription = null,
                  tint = badgeIconTint,
                  modifier = Modifier.size(20.dp),
                )
              }
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = if (titleTestTag != null) Modifier.testTag(titleTestTag) else Modifier,
              )
              if (!subtext.isNullOrEmpty()) {
                Text(
                  text = subtext,
                  style = MaterialTheme.typography.bodySmall,
                  color = subtextColor,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = if (subtextTestTag != null) Modifier.testTag(subtextTestTag) else Modifier,
                )
              }
            }
          }

          if (!isSearchActive) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
              actions()

              if (searchState != null) {
                IconButton(
                  onClick = searchState.onOpenSearch,
                  modifier = Modifier.testTag(searchState.toggleButtonTestTag),
                ) {
                  Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = searchState.searchContentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }
        }
      }

      if (searchState != null) {
        AnimatedVisibility(
          visible = isSearchActive && !isSelectionMode,
          enter = fadeIn(animationSpec = tween(220)) + expandHorizontally(
            animationSpec = spring(
              dampingRatio = Spring.DampingRatioNoBouncy,
              stiffness = Spring.StiffnessMediumLow,
            ),
            expandFrom = Alignment.End,
          ),
          exit = fadeOut(animationSpec = tween(180)) + shrinkHorizontally(
            animationSpec = tween(200),
            shrinkTowards = Alignment.End,
          ),
          modifier = Modifier.matchParentSize(),
        ) {
          Surface(
            modifier = Modifier
              .fillMaxSize()
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
              )
              .testTag(searchState.searchBarTestTag),
            color = containerColor,
            tonalElevation = 2.dp,
          ) {
            Row(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              IconButton(
                onClick = searchState.onCloseSearch,
                modifier = Modifier.testTag(searchState.closeButtonTestTag),
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = searchState.closeContentDescription,
                  tint = MaterialTheme.colorScheme.onSurface,
                )
              }

              TextField(
                value = searchState.query,
                onValueChange = searchState.onQueryChange,
                placeholder = {
                  Text(
                    text = searchState.placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                  color = MaterialTheme.colorScheme.onSurface,
                ),
                colors = TextFieldDefaults.colors(
                  focusedContainerColor = Color.Transparent,
                  unfocusedContainerColor = Color.Transparent,
                  disabledContainerColor = Color.Transparent,
                  focusedIndicatorColor = Color.Transparent,
                  unfocusedIndicatorColor = Color.Transparent,
                  disabledIndicatorColor = Color.Transparent,
                ),
                trailingIcon = {
                  if (searchState.query.isNotEmpty()) {
                    IconButton(
                      onClick = searchState.onClearQuery,
                      modifier = Modifier.testTag(searchState.clearButtonTestTag),
                    ) {
                      Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = searchState.clearContentDescription,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                  }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                  onSearch = {
                    if (searchState.onSearchImeAction != null) {
                      searchState.onSearchImeAction.invoke()
                    } else {
                      keyboardController?.hide()
                    }
                  },
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .focusRequester(focusRequester)
                  .testTag(searchState.inputTestTag),
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Shared Multi-Selection toolbar row matching FilesScreen's design language (Section 5.5).
 */
@Composable
fun ToolboxMultiSelectBarRow(
  selectionState: ToolboxTopBarSelectionState,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 8.dp)
      .then(
        if (selectionState.rowTestTag != null) {
          Modifier.testTag(selectionState.rowTestTag)
        } else {
          Modifier
        }
      ),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      modifier = Modifier.weight(1f, fill = false),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      IconButton(
        onClick = selectionState.onCloseSelection,
        modifier = Modifier
          .size(36.dp)
          .testTag(selectionState.closeButtonTestTag),
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Exit selection",
          tint = MaterialTheme.colorScheme.onSurface,
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .clickable(onClick = selectionState.onToggleSelectAll)
          .padding(horizontal = 4.dp, vertical = 4.dp)
          .testTag(selectionState.selectAllRowTestTag),
      ) {
        Checkbox(
          checked = selectionState.totalCount > 0 && selectionState.selectedCount == selectionState.totalCount,
          onCheckedChange = { selectionState.onToggleSelectAll() },
          colors = CheckboxDefaults.colors(
            checkedColor = MaterialTheme.colorScheme.primary,
          ),
          modifier = Modifier
            .size(32.dp)
            .then(
              if (selectionState.checkboxTestTag != null) {
                Modifier.testTag(selectionState.checkboxTestTag)
              } else {
                Modifier
              }
            ),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "${selectionState.selectedCount} selected",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = if (selectionState.countTextTestTag != null) {
            Modifier.testTag(selectionState.countTextTestTag)
          } else {
            Modifier
          },
        )
      }
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(2.dp),
      content = selectionState.actions,
    )
  }
}

/**
 * Toolbar button that cycles a tool's presentation mode through:
 * List view ([Icons.AutoMirrored.Filled.ViewList]) -> [minColumns]-column grid -> ... -> [maxColumns]-column grid -> List view.
 *
 * When in List view ([isListView] == true), displays the list icon ([Icons.AutoMirrored.Filled.ViewList]).
 * When in Grid/Card/Tile view ([isListView] == false), displays the current [columnsPerRow] count centered inside
 * an underlaid [Icons.Default.Fullscreen] corner-bracket frame.
 */
@Composable
fun ToolboxViewModeCycleButton(
  isListView: Boolean,
  columnsPerRow: Int,
  minColumns: Int = 2,
  maxColumns: Int,
  onSelectListView: () -> Unit,
  onSelectGridColumns: (Int) -> Unit,
  modifier: Modifier = Modifier,
  buttonTestTag: String = "action_toggle_view_mode",
  countTestTag: String? = null,
  listContentDescription: String = "List view",
  gridContentDescription: String = "$columnsPerRow per row",
  tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
  val coercedColumns = columnsPerRow.coerceIn(minColumns, maxColumns)
  val cycleViewMode = {
    if (isListView) {
      onSelectGridColumns(minColumns)
    } else if (coercedColumns < maxColumns) {
      onSelectGridColumns(coercedColumns + 1)
    } else {
      onSelectListView()
    }
  }

  Box(
    modifier = modifier.testTag(buttonTestTag),
    contentAlignment = Alignment.Center,
  ) {
    IconButton(
      onClick = cycleViewMode,
      modifier = Modifier
        .then(
          if (!isListView && countTestTag != null) {
            Modifier.testTag(countTestTag)
          } else {
            Modifier
          }
        )
        .semantics {
          this.contentDescription = if (isListView) listContentDescription else gridContentDescription
        },
    ) {
      if (isListView) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ViewList,
          contentDescription = null,
          tint = tint,
        )
      } else {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Fullscreen,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(26.dp),
          )
          Text(
            text = coercedColumns.toString(),
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 11.sp,
              lineHeight = 11.sp,
            ),
            fontWeight = FontWeight.ExtraBold,
            color = tint,
          )
        }
      }
    }
  }
}

/**
 * Pinch-to-zoom gesture modifier that adjusts the number of tiles per row between [minColumns] and [maxColumns],
 * and optionally transitions seamlessly between List view (1 column) and Grid view ([minColumns]..[maxColumns] columns)
 * when [onSelectListView] is provided.
 *
 * - Pinching in (two fingers moving closer together) transitions from List view (1 column) into a [minColumns]-column
 *   Grid view, and further pinching in increases the number of columns per row up to [maxColumns].
 * - Pinching out (two fingers spreading apart) decreases the number of columns per row down to [minColumns],
 *   and further pinching out transitions into List view (1 column) when [onSelectListView] is provided.
 */
@Composable
fun Modifier.pinchToChangeColumns(
  enabled: Boolean = true,
  isListView: Boolean = false,
  columnsPerRow: Int,
  minColumns: Int,
  maxColumns: Int,
  onColumnsChange: (Int) -> Unit,
  onSelectListView: (() -> Unit)? = null,
): Modifier {
  val currentIsListView by rememberUpdatedState(isListView)
  val currentColumnsState by rememberUpdatedState(columnsPerRow)
  val currentOnColumnsChange by rememberUpdatedState(onColumnsChange)
  val currentOnSelectListView by rememberUpdatedState(onSelectListView)
  return this.pointerInput(enabled, minColumns, maxColumns) {
    if (!enabled) return@pointerInput
    awaitEachGesture {
      var cumulativeZoom = 1f
      val supportsListTransition = currentOnSelectListView != null
      val minSlot = if (supportsListTransition) 1 else minColumns
      var liveSlot = if (supportsListTransition && currentIsListView) {
        1
      } else {
        currentColumnsState.coerceIn(minColumns, maxColumns)
      }
      var multiTouchStarted = false

      do {
        val event = awaitPointerEvent(PointerEventPass.Initial)
        val pressedCount = event.changes.count { it.pressed }
        if (pressedCount >= 2) {
          if (!multiTouchStarted) {
            multiTouchStarted = true
            cumulativeZoom = 1f
            liveSlot = if (currentOnSelectListView != null && currentIsListView) {
              1
            } else {
              currentColumnsState.coerceIn(minColumns, maxColumns)
            }
          }
          val zoomChange = event.calculateZoom()
          if (zoomChange != 1f && zoomChange > 0f && !zoomChange.isNaN()) {
            cumulativeZoom *= zoomChange

            // Pinching in (fingers moving closer, zoom < 1f) -> transition List (1) -> minColumns -> ... -> maxColumns
            while (cumulativeZoom <= 0.82f) {
              val nextSlot = if (liveSlot < minColumns) {
                minColumns
              } else {
                (liveSlot + 1).coerceIn(minColumns, maxColumns)
              }
              if (nextSlot != liveSlot) {
                liveSlot = nextSlot
                currentOnColumnsChange(nextSlot)
              }
              cumulativeZoom /= 0.82f
            }

            // Pinching out (fingers spreading apart, zoom > 1f) -> decrease maxColumns -> ... -> minColumns -> List (1)
            val effectiveMinSlot = if (currentOnSelectListView != null) 1 else minColumns
            while (cumulativeZoom >= 1.22f) {
              val nextSlot = if (liveSlot <= minColumns) {
                effectiveMinSlot
              } else {
                (liveSlot - 1).coerceIn(minColumns, maxColumns)
              }
              if (nextSlot != liveSlot) {
                liveSlot = nextSlot
                if (nextSlot < minColumns) {
                  currentOnSelectListView?.invoke()
                } else {
                  currentOnColumnsChange(nextSlot)
                }
              }
              cumulativeZoom /= 1.22f
            }

            event.changes.forEach { it.consume() }
          }
        } else {
          multiTouchStarted = false
          cumulativeZoom = 1f
        }
      } while (event.changes.any { it.pressed })
    }
  }
}

/**
 * Masks the right edge of a horizontally scrolling pill row so pills smoothly fade into the
 * background as they slide toward the floating [# per row] and [Sort/Filter] buttons, and are
 * completely transparent directly underneath and between the floating buttons.
 *
 * Place this modifier BEFORE `.horizontalScroll(...)` on the pill Row so the fade zone remains
 * anchored to the right edge of the viewport while pills scroll.
 */
fun Modifier.fadingPillRowUnderFloatingControls(
  hasColumnsButton: Boolean,
  fadeWidth: Dp = 32.dp,
  extraControlButtonsCount: Int = 0,
): Modifier = this
  .graphicsLayer {
    compositingStrategy = CompositingStrategy.Offscreen
  }
  .drawWithContent {
    drawContent()
    val baseOccupiedWidth = if (hasColumnsButton) 86.dp else 46.dp
    val controlsOccupiedWidthPx = (baseOccupiedWidth + (extraControlButtonsCount * 40).dp).toPx()
    val fadeWidthPx = fadeWidth.toPx()
    val fadeEndPx = (size.width - controlsOccupiedWidthPx + 2.dp.toPx()).coerceAtLeast(0f)
    val fadeStartPx = (fadeEndPx - fadeWidthPx).coerceAtLeast(0f)
    if (size.width > 0f && fadeEndPx > 0f) {
      val startFraction = (fadeStartPx / size.width).coerceIn(0f, 1f)
      val endFraction = (fadeEndPx / size.width).coerceIn(startFraction, 1f)
      drawRect(
        brush = Brush.horizontalGradient(
          0f to Color.Black,
          startFraction to Color.Black,
          endFraction to Color.Transparent,
          1f to Color.Transparent,
        ),
        blendMode = BlendMode.DstIn,
      )
    }
  }

/**
 * Draws a soft horizontal background-color gradient scrim behind the right-aligned floating
 * [# per row] and [Sort/Filter] controls so scrolling pills seamlessly dissolve into [backgroundColor]
 * without any visual bleed-through under or between the floating buttons.
 */
fun Modifier.pillBarFloatingControlsBackdrop(
  backgroundColor: Color = Color.Unspecified,
  fadeLeadIn: Dp = 28.dp,
): Modifier = this.drawBehind {
  if (backgroundColor != Color.Unspecified && size.width > 0f && size.height > 0f) {
    val leadInPx = fadeLeadIn.toPx()
    val verticalBleedPx = 6.dp.toPx()
    val totalWidth = size.width + leadInPx
    val solidStop = (leadInPx / totalWidth).coerceIn(0f, 1f)
    drawRect(
      brush = Brush.horizontalGradient(
        0f to backgroundColor.copy(alpha = 0f),
        (solidStop * 0.65f) to backgroundColor.copy(alpha = 0.72f),
        solidStop to backgroundColor,
        1f to backgroundColor,
        startX = -leadInPx,
        endX = size.width,
      ),
      topLeft = Offset(x = -leadInPx, y = -verticalBleedPx),
      size = Size(width = totalWidth, height = size.height + verticalBleedPx * 2f),
    )
  }
}

/**
 * Shared horizontal pill navigation / filter bar component (`Section 5.4` of `DESIGN_LANGUAGE.md`).
 *
 * Enforces consistent pill bar styling across all tools that display a horizontal row of navigation
 * or filter pills beneath [ToolboxTopBar] (or inside a search catalog overlay):
 * - Horizontally scrollable [Row] with `12.dp` horizontal padding (`6.dp` top/bottom padding) and `8.dp` pill spacing.
 * - Optional pinned floating right-edge control slot ([floatingControls]) with automatic offscreen alpha-fade
 *   masking ([fadingPillRowUnderFloatingControls]) and background backdrop scrim ([pillBarFloatingControlsBackdrop])
 *   so scrolling pills never bleed underneath or between floating buttons.
 * - Optional [overlayContent] slot inside the outer [Box] for drag-and-drop ghost pills.
 */
@Composable
fun ToolboxPillNavigationBar(
  modifier: Modifier = Modifier,
  barTestTag: String? = null,
  rowTestTag: String? = null,
  floatingControlsTestTag: String? = null,
  scrollState: ScrollState = rememberScrollState(),
  scrollEnabled: Boolean = true,
  floatingButtonsCount: Int = 0,
  backgroundColor: Color = MaterialTheme.colorScheme.background,
  startPadding: Dp = 12.dp,
  endPadding: Dp = when {
    floatingButtonsCount <= 0 -> 12.dp
    else -> (16 + floatingButtonsCount * 40).dp
  },
  verticalPadding: Dp = 6.dp,
  pillSpacing: Dp = 8.dp,
  floatingControls: (@Composable RowScope.() -> Unit)? = null,
  overlayContent: (@Composable BoxScope.() -> Unit)? = null,
  pills: @Composable RowScope.() -> Unit,
) {
  val hasFloatingControls = floatingControls != null && floatingButtonsCount > 0
  Box(
    modifier = modifier
      .fillMaxWidth()
      .then(if (barTestTag != null) Modifier.testTag(barTestTag) else Modifier),
    contentAlignment = Alignment.CenterStart,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .then(
          if (hasFloatingControls) {
            Modifier.fadingPillRowUnderFloatingControls(
              hasColumnsButton = floatingButtonsCount >= 2,
              extraControlButtonsCount = (floatingButtonsCount - 2).coerceAtLeast(0),
            )
          } else {
            Modifier
          }
        )
        .horizontalScroll(scrollState, enabled = scrollEnabled)
        .padding(
          start = startPadding,
          end = endPadding,
          top = verticalPadding,
          bottom = verticalPadding,
        )
        .then(if (rowTestTag != null) Modifier.testTag(rowTestTag) else Modifier),
      horizontalArrangement = Arrangement.spacedBy(pillSpacing),
      verticalAlignment = Alignment.CenterVertically,
      content = pills,
    )

    if (floatingControls != null) {
      Row(
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .pillBarFloatingControlsBackdrop(backgroundColor = backgroundColor)
          .padding(end = 12.dp)
          .zIndex(10f)
          .then(
            if (floatingControlsTestTag != null) {
              Modifier.testTag(floatingControlsTestTag)
            } else {
              Modifier
            }
          ),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = floatingControls,
      )
    }

    if (overlayContent != null) {
      overlayContent()
    }
  }
}

/**
 * Standard pill item inside [ToolboxPillNavigationBar].
 *
 * Enforces the unified `RoundedCornerShape(20.dp)` geometry, `labelMedium` typography
 * (`FontWeight.Bold` when selected, `FontWeight.Medium` when unselected), `16.dp` leading icon size,
 * and `primaryContainer` / `onPrimaryContainer` / `primary` color tokens across all tools.
 */
@Composable
fun ToolboxPillNavigationItem(
  selected: Boolean,
  onClick: () -> Unit,
  label: String,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  leadingIcon: ImageVector? = null,
  leadingIconContent: (@Composable () -> Unit)? = null,
  trailingIcon: (@Composable () -> Unit)? = null,
  testTag: String? = null,
) {
  val pillShape = if (currentHexagonIconButtons()) {
    HorizontalHexagonShape
  } else {
    RoundedCornerShape(20.dp)
  }
  FilterChip(
    selected = selected,
    onClick = onClick,
    enabled = enabled,
    shape = pillShape,
    leadingIcon = when {
      leadingIconContent != null -> leadingIconContent
      leadingIcon != null -> {
        {
          Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
        }
      }
      else -> null
    },
    trailingIcon = trailingIcon,
    label = {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    },
    colors = FilterChipDefaults.filterChipColors(
      selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
      selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
      selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
      selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ),
    modifier = modifier.then(
      if (testTag != null) Modifier.testTag(testTag) else Modifier
    ),
  )
}
