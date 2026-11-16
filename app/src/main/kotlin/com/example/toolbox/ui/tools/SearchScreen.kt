package com.example.toolbox.ui.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.toolbox.ui.main.NavigationPreferencesRepository
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.theme.ToolboxTheme
import com.example.toolbox.ui.theme.parseHexColor

/**
 * Universal Search Tool screen that searches across all tools, live calculators/converters,
 * persisted user data (Notes, To-Do, Recipes, Plants), and built-in reference catalogs.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
  modifier: Modifier = Modifier,
  onSelectTool: ((Tool) -> Unit)? = null,
  onOpenColorPicker: ((String?) -> Unit)? = null,
  preferencesRepository: NavigationPreferencesRepository? = null,
  customDataSources: SearchDataSources? = null,
  initialQuery: String = "",
) {
  val context = LocalContext.current
  val haptics = LocalHapticFeedback.current
  val focusManager = LocalFocusManager.current

  val hiddenTools by if (preferencesRepository != null) {
    preferencesRepository.hiddenToolsFlow.collectAsState()
  } else {
    remember { mutableStateOf(emptySet<Tool>()) }
  }

  val usageCounts by if (preferencesRepository != null) {
    preferencesRepository.toolUsageCountsFlow.collectAsState()
  } else {
    remember { mutableStateOf(emptyMap<Tool, Int>()) }
  }

  val dataSources = remember(context, customDataSources, hiddenTools, usageCounts) {
    customDataSources ?: SearchEngine.createDataSources(
      context = context,
      hiddenTools = hiddenTools,
      usageCounts = usageCounts,
    )
  }

  var isSearchActive by rememberSaveable { mutableStateOf(true) }
  var searchQuery by rememberSaveable { mutableStateOf(initialQuery) }
  var selectedCategoryFilter by rememberSaveable { mutableStateOf(SearchCategoryFilter.ALL) }
  var selectedToolFilterName by rememberSaveable { mutableStateOf<String?>(null) }
  var inspectedResult by remember { mutableStateOf<SearchResultItem?>(null) }
  var copiedItemId by remember { mutableStateOf<String?>(null) }

  val selectedToolFilter = remember(selectedToolFilterName) {
    selectedToolFilterName?.let { name -> Tool.entries.find { it.name == name } }
  }

  val results = remember(searchQuery, selectedCategoryFilter, selectedToolFilter, dataSources) {
    SearchEngine.search(
      query = searchQuery,
      categoryFilter = selectedCategoryFilter,
      toolFilter = selectedToolFilter,
      dataSources = dataSources,
    )
  }

  val perToolCounts = remember(searchQuery, dataSources) {
    SearchEngine.countResultsByTool(
      query = searchQuery,
      dataSources = dataSources,
    )
  }

  BackHandler(
    enabled = searchQuery.isNotEmpty() ||
      selectedCategoryFilter != SearchCategoryFilter.ALL ||
      selectedToolFilter != null,
  ) {
    if (searchQuery.isNotEmpty() || selectedToolFilter != null) {
      searchQuery = ""
      selectedToolFilterName = null
    } else {
      selectedCategoryFilter = SearchCategoryFilter.ALL
    }
    focusManager.clearFocus()
  }

  fun launchToolFromSearch(tool: Tool, colorHex: String? = null) {
    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    if (tool == Tool.COLOR_PICKER && colorHex != null && onOpenColorPicker != null) {
      onOpenColorPicker(colorHex)
    } else {
      onSelectTool?.invoke(tool)
    }
  }

  fun copyToClipboard(label: String, text: String, itemId: String) {
    try {
      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
      clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
      copiedItemId = itemId
    } catch (_: Exception) {
      // Ignore in headless unit test environments
    }
  }

  val statusSubtitle = remember(searchQuery, results.size, perToolCounts.size) {
    if (searchQuery.isBlank()) {
      "Search across all ${Tool.entries.size} tools, live calculators & catalogs"
    } else {
      val toolWord = if (perToolCounts.size == 1) "tool" else "tools"
      val resultWord = if (results.size == 1) "result" else "results"
      "${results.size} $resultWord across ${perToolCounts.size} $toolWord"
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("search_screen"),
  ) {
    // Standard 68.dp Top Header Bar with integrated & autofocused search bar
    ToolboxTopBar(
      title = "Search",
      subtext = statusSubtitle,
      icon = Icons.Default.Search,
      subtextTestTag = "search_status_subtitle",
      leadingRowModifier = Modifier.clickable {
        isSearchActive = true
      },
      actions = {
        if (searchQuery.isNotBlank() || selectedCategoryFilter != SearchCategoryFilter.ALL || selectedToolFilter != null) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            modifier = Modifier
              .clip(CircleShape)
              .clickable {
                searchQuery = ""
                selectedCategoryFilter = SearchCategoryFilter.ALL
                selectedToolFilterName = null
              }
              .testTag("search_reset_all_button"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Reset search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = "Reset",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      },
      searchState = ToolboxTopBarSearchState(
        isActive = isSearchActive,
        query = searchQuery,
        onQueryChange = { newValue ->
          searchQuery = newValue
          copiedItemId = null
        },
        onOpenSearch = {
          isSearchActive = true
        },
        onCloseSearch = {
          isSearchActive = false
          searchQuery = ""
          selectedToolFilterName = null
          focusManager.clearFocus()
        },
        onClearQuery = {
          searchQuery = ""
          selectedToolFilterName = null
        },
        placeholder = "Search tools, math, units, notes, recipes, DTCs...",
        searchContentDescription = "Search",
        closeContentDescription = "Close search",
        clearContentDescription = "Clear search query",
        toggleButtonTestTag = "action_toggle_search",
        searchBarTestTag = "toolbar_search_bar",
        closeButtonTestTag = "action_close_search",
        inputTestTag = "search_query_input",
        clearButtonTestTag = "search_clear_query_button",
        onSearchImeAction = {
          focusManager.clearFocus()
        },
      ),
    )

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

    // Category Pills Bar (aligned with ToolCategory) + Per-Tool Filter Chips
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .testTag("search_category_pills_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SearchCategoryFilter.entries.forEach { filter ->
          val isSelected = selectedCategoryFilter == filter
          FilterChip(
            selected = isSelected,
            onClick = {
              selectedCategoryFilter = if (isSelected && filter != SearchCategoryFilter.ALL) {
                SearchCategoryFilter.ALL
              } else {
                filter
              }
            },
            label = {
              Text(
                text = filter.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
              )
            },
            shape = RoundedCornerShape(20.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            modifier = Modifier.testTag("search_category_chip_${filter.name}"),
          )
        }
      }

      // Per-Tool Filter Bar when query has matches across tools
      AnimatedVisibility(
        visible = searchQuery.isNotBlank() && perToolCounts.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut(),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          val totalCount = perToolCounts.values.sum()
          val allSelected = selectedToolFilter == null
          Surface(
            shape = CircleShape,
            color = if (allSelected) {
              MaterialTheme.colorScheme.secondaryContainer
            } else {
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            },
            border = if (allSelected) {
              BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
            } else {
              null
            },
            modifier = Modifier
              .clip(CircleShape)
              .clickable { selectedToolFilterName = null }
              .testTag("search_tool_filter_ALL"),
          ) {
            Text(
              text = "All Tools ($totalCount)",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (allSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (allSelected) {
                MaterialTheme.colorScheme.onSecondaryContainer
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
              maxLines = 1,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            )
          }

          perToolCounts.entries
            .sortedByDescending { it.value }
            .forEach { (tool, count) ->
              val isSelected = selectedToolFilter == tool
              Surface(
                shape = CircleShape,
                color = if (isSelected) {
                  MaterialTheme.colorScheme.secondaryContainer
                } else {
                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                },
                border = if (isSelected) {
                  BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                } else {
                  null
                },
                modifier = Modifier
                  .clip(CircleShape)
                  .clickable {
                    selectedToolFilterName = if (isSelected) null else tool.name
                  }
                  .testTag("search_tool_filter_${tool.name}"),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                  Icon(
                    imageVector = tool.icon,
                    contentDescription = null,
                    tint = if (isSelected) {
                      MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                      MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(14.dp),
                  )
                  Text(
                    text = "${tool.title} ($count)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) {
                      MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                      MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                  )
                }
              }
            }
        }
      }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

    // Main Content Area
    if (searchQuery.isBlank()) {
      SearchDiscoveryHomeView(
        hiddenTools = hiddenTools,
        usageCounts = usageCounts,
        selectedCategoryFilter = selectedCategoryFilter,
        onRunQuery = { query ->
          isSearchActive = true
          searchQuery = query
        },
        onOpenTool = { tool ->
          launchToolFromSearch(tool)
        },
      )
    } else if (results.isEmpty()) {
      SearchEmptyResultsView(
        query = searchQuery,
        hasActiveFilters = selectedCategoryFilter != SearchCategoryFilter.ALL || selectedToolFilter != null,
        onResetFilters = {
          selectedCategoryFilter = SearchCategoryFilter.ALL
          selectedToolFilterName = null
        },
      )
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("search_results_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(results, key = { it.id }) { item ->
          SearchResultCard(
            item = item,
            isHiddenTool = item.resultType == SearchResultType.TOOL && item.tool in hiddenTools,
            isCopied = copiedItemId == item.id,
            onCardClick = {
              if (item.resultType == SearchResultType.TOOL) {
                launchToolFromSearch(item.tool, item.colorHex)
              } else {
                inspectedResult = item
              }
            },
            onInspectClick = {
              inspectedResult = item
            },
            onOpenToolClick = {
              launchToolFromSearch(item.tool, item.colorHex)
            },
            onCopyClick = { valueToCopy ->
              copyToClipboard(item.title, valueToCopy, item.id)
            },
          )
        }
      }
    }
  }

  // Result Detail Inspection Modal Dialog
  inspectedResult?.let { resultItem ->
    SearchResultInspectionDialog(
      item = resultItem,
      isCopied = copiedItemId == resultItem.id,
      onDismiss = { inspectedResult = null },
      onCopyValue = { valueToCopy ->
        copyToClipboard(resultItem.title, valueToCopy, resultItem.id)
      },
      onOpenTool = {
        val targetTool = resultItem.tool
        val targetHex = resultItem.colorHex
        inspectedResult = null
        launchToolFromSearch(targetTool, targetHex)
      },
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchDiscoveryHomeView(
  hiddenTools: Set<Tool>,
  usageCounts: Map<Tool, Int>,
  selectedCategoryFilter: SearchCategoryFilter,
  onRunQuery: (String) -> Unit,
  onOpenTool: (Tool) -> Unit,
) {
  val filteredDirectoryTools = remember(selectedCategoryFilter) {
    Tool.entries.filter { tool ->
      selectedCategoryFilter.toolCategory == null || tool.category == selectedCategoryFilter.toolCategory
    }
  }

  val filteredSuggestions = remember(selectedCategoryFilter) {
    SearchEngine.DISCOVERY_SUGGESTIONS.filter { suggestion ->
      selectedCategoryFilter.toolCategory == null || suggestion.tool.category == selectedCategoryFilter.toolCategory
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("search_discovery_home"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    // 1. Instant Evaluators & Discovery Suggestions
    if (filteredSuggestions.isNotEmpty()) {
      item(key = "discovery_suggestions_section") {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_suggestions_card"),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = "Instant Evaluators & Catalog Lookups",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
              )
            }

            Text(
              text = "Type a math formula, unit conversion, hex color, RPG dice roll, chord, scale, recipe, plant, or OBD2 trouble code:",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              filteredSuggestions.forEach { suggestion ->
                Surface(
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f),
                  modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onRunQuery(suggestion.query) }
                    .testTag("search_suggestion_${suggestion.query}"),
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Icon(
                      imageVector = suggestion.tool.icon,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.onSecondaryContainer,
                      modifier = Modifier.size(14.dp),
                    )
                    Text(
                      text = suggestion.label,
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // 2. Tools Directory Header
    item(key = "directory_header_section") {
      val headerTitle = if (selectedCategoryFilter == SearchCategoryFilter.ALL) {
        "All Tools (${filteredDirectoryTools.size})"
      } else {
        "${selectedCategoryFilter.label} (${filteredDirectoryTools.size})"
      }
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = headerTitle,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "${Tool.entries.size - hiddenTools.size} visible • ${hiddenTools.size} hidden",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    // 3. Tools Directory Cards
    items(filteredDirectoryTools, key = { "dir_${it.name}" }) { tool ->
      val meta = SearchEngine.TOOL_METADATA[tool]
      val isHidden = tool in hiddenTools
      val usageCount = usageCounts[tool] ?: 0

      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .clickable { onOpenTool(tool) }
          .testTag("search_directory_tool_${tool.name}"),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = tool.icon,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(22.dp),
            )
          }

          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = tool.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
              )
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
              ) {
                Text(
                  text = tool.category.title,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                  maxLines = 1,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
              if (isHidden) {
                Icon(
                  imageVector = Icons.Default.VisibilityOff,
                  contentDescription = "Hidden in bar",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier
                    .size(16.dp)
                    .testTag("search_tool_hidden_icon_${tool.name}"),
                )
              }
            }

            Text(
              text = meta?.summary ?: tool.category.title,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )

            if (usageCount > 0) {
              Text(
                text = "Used $usageCount ${if (usageCount == 1) "time" else "times"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }

          var toolMenuExpanded by remember { mutableStateOf(false) }
          val toolContext = LocalContext.current
          Box {
            IconButton(
              onClick = { toolMenuExpanded = true },
              modifier = Modifier
                .size(34.dp)
                .testTag("search_tool_more_${tool.name}"),
            ) {
              Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options for ${tool.title}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
              )
            }
            DropdownMenu(
              expanded = toolMenuExpanded,
              onDismissRequest = { toolMenuExpanded = false },
            ) {
              DropdownMenuItem(
                text = { Text("Open ${tool.title}") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Launch, contentDescription = null) },
                onClick = {
                  toolMenuExpanded = false
                  onOpenTool(tool)
                },
              )
              DropdownMenuItem(
                text = { Text("Copy Tool Info") },
                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                onClick = {
                  toolMenuExpanded = false
                  CrossToolActions.copyTextToClipboard(
                    toolContext,
                    tool.title,
                    "${tool.title} (${tool.category.title}) — ${meta?.summary ?: ""}",
                  )
                },
              )
              DropdownMenuItem(
                text = { Text("Save to Notes") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null) },
                onClick = {
                  toolMenuExpanded = false
                  CrossToolActions.saveTextToNotes(
                    toolContext,
                    tool.title,
                    "Category: ${tool.category.title}\n\n${meta?.summary ?: ""}",
                  )
                },
              )
              DropdownMenuItem(
                text = { Text("Share") },
                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                onClick = {
                  toolMenuExpanded = false
                  CrossToolActions.shareText(
                    toolContext,
                    tool.title,
                    "${tool.title} (${tool.category.title})\n${meta?.summary ?: ""}",
                  )
                },
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SearchResultCard(
  item: SearchResultItem,
  isHiddenTool: Boolean,
  isCopied: Boolean,
  onCardClick: () -> Unit,
  onInspectClick: () -> Unit,
  onOpenToolClick: () -> Unit,
  onCopyClick: (String) -> Unit,
) {
  val parsedSwatchColor = remember(item.colorHex) {
    item.colorHex?.let { parseHexColor(it) }
  }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = if (
      item.resultType == SearchResultType.LIVE_CALCULATION ||
        item.resultType == SearchResultType.LIVE_CONVERSION ||
        item.resultType == SearchResultType.LIVE_COLOR ||
        item.resultType == SearchResultType.LIVE_DICE_ROLL
    ) {
      BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
    } else {
      null
    },
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onCardClick)
      .testTag("search_result_${item.id}"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        if (parsedSwatchColor != null) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(parsedSwatchColor)
              .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
          )
        } else {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = item.tool.icon,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(20.dp),
            )
          }
        }

        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
              )
              if (isHiddenTool) {
                Icon(
                  imageVector = Icons.Default.VisibilityOff,
                  contentDescription = "Hidden in bar",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(16.dp),
                )
              }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = CircleShape,
              color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
              Text(
                text = if (item.resultType == SearchResultType.TOOL) {
                  item.tool.category.title
                } else {
                  "${item.tool.title} • ${item.badgeLabel}"
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              )
            }
            var resultMenuExpanded by remember { mutableStateOf(false) }
            val resultContext = LocalContext.current
            Box {
              IconButton(
                onClick = { resultMenuExpanded = true },
                modifier = Modifier
                  .size(30.dp)
                  .testTag("search_result_more_${item.id}"),
              ) {
                Icon(
                  imageVector = Icons.Default.MoreVert,
                  contentDescription = "More options for ${item.title}",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp),
                )
              }
              DropdownMenu(
                expanded = resultMenuExpanded,
                onDismissRequest = { resultMenuExpanded = false },
              ) {
                DropdownMenuItem(
                  text = { Text("Open ${item.tool.title}") },
                  leadingIcon = { Icon(Icons.AutoMirrored.Filled.Launch, contentDescription = null) },
                  onClick = {
                    resultMenuExpanded = false
                    onOpenToolClick()
                  },
                )
                if (item.detailRows.isNotEmpty() && item.resultType != SearchResultType.TOOL) {
                  DropdownMenuItem(
                    text = { Text("Inspect Details") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                    onClick = {
                      resultMenuExpanded = false
                      onInspectClick()
                    },
                  )
                }
                DropdownMenuItem(
                  text = { Text("Copy") },
                  leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                  onClick = {
                    resultMenuExpanded = false
                    onCopyClick(item.copyableValue ?: "${item.title}\n${item.subtitle}")
                  },
                )
                DropdownMenuItem(
                  text = { Text("Save to Notes") },
                  leadingIcon = { Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null) },
                  onClick = {
                    resultMenuExpanded = false
                    val body = buildString {
                      appendLine(item.subtitle)
                      if (item.snippet.isNotBlank()) {
                        appendLine()
                        appendLine(item.snippet)
                      }
                      if (item.detailRows.isNotEmpty()) {
                        appendLine()
                        item.detailRows.forEach { row ->
                          appendLine("• ${row.label}: ${row.value}")
                        }
                      }
                    }.trim()
                    CrossToolActions.saveTextToNotes(resultContext, item.title, body)
                  },
                )
                DropdownMenuItem(
                  text = { Text("Add to To Do") },
                  leadingIcon = { Icon(Icons.Default.Checklist, contentDescription = null) },
                  onClick = {
                    resultMenuExpanded = false
                    CrossToolActions.saveTaskToTodoList(
                      resultContext,
                      "${item.title} (${item.subtitle})",
                    )
                  },
                )
                DropdownMenuItem(
                  text = { Text("Share") },
                  leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                  onClick = {
                    resultMenuExpanded = false
                    CrossToolActions.shareText(
                      resultContext,
                      item.title,
                      "${item.title}\n${item.subtitle}\n${item.snippet}".trim(),
                    )
                  },
                )
              }
            }
          }

          Text(
            text = item.subtitle,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          if (item.snippet.isNotBlank()) {
            Text(
              text = item.snippet,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }

      // Action Pill Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (item.copyableValue != null) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            modifier = Modifier
              .clip(CircleShape)
              .clickable { onCopyClick(item.copyableValue) }
              .testTag("search_result_copy_${item.id}"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                contentDescription = "Copy result",
                tint = if (isCopied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp),
              )
              Text(
                text = if (isCopied) "Copied" else "Copy",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isCopied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          Spacer(modifier = Modifier.width(8.dp))
        }

        if (item.detailRows.isNotEmpty() && item.resultType != SearchResultType.TOOL) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            modifier = Modifier
              .clip(CircleShape)
              .clickable(onClick = onInspectClick)
              .testTag("search_result_details_${item.id}"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Inspect details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp),
              )
              Text(
                text = "Details",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.primaryContainer,
          modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onOpenToolClick)
            .testTag("search_result_open_${item.id}"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
          ) {
            Text(
              text = "Open ${item.tool.title}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Launch,
              contentDescription = "Open ${item.tool.title}",
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(13.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SearchEmptyResultsView(
  query: String,
  hasActiveFilters: Boolean,
  onResetFilters: () -> Unit,
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp)
      .testTag("search_empty_results"),
    contentAlignment = Alignment.Center,
  ) {
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
      ),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp),
          )
        }

        Text(
          text = "No matches for \"$query\"",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center,
        )

        Text(
          text = if (hasActiveFilters) {
            "No results matched the active category or tool filter. Try clearing filters to search across all ${Tool.entries.size} tools."
          } else {
            "Try searching for a tool name, math expression (24 * 7), unit conversion (100 km to mi), hex color (#8E66A6), chord (Cmaj7), recipe, plant, or OBD2 code (P0420)."
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
        )

        if (hasActiveFilters) {
          FilledTonalButton(
            onClick = onResetFilters,
            modifier = Modifier.testTag("search_empty_reset_filters_button"),
          ) {
            Text("Show All Categories & Tools")
          }
        }
      }
    }
  }
}

@Composable
private fun SearchResultInspectionDialog(
  item: SearchResultItem,
  isCopied: Boolean,
  onDismiss: () -> Unit,
  onCopyValue: (String) -> Unit,
  onOpenTool: () -> Unit,
) {
  val parsedSwatchColor = remember(item.colorHex) {
    item.colorHex?.let { parseHexColor(it) }
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
      tonalElevation = 6.dp,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("search_result_detail_dialog"),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Dialog Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          if (parsedSwatchColor != null) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(parsedSwatchColor)
                .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            )
          } else {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = item.tool.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp),
              )
            }
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = item.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "${item.tool.title} • ${item.badgeLabel}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold,
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(32.dp)
              .testTag("search_dialog_close_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close details",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Scrollable Detail Rows
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 340.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          if (item.detailRows.isEmpty()) {
            item {
              Text(
                text = item.snippet.ifBlank { item.subtitle },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          } else {
            items(item.detailRows) { row ->
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                ),
                modifier = Modifier.fillMaxWidth(),
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                  Text(
                    text = row.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                  )
                  Text(
                    text = row.value,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontFamily = if (
                        row.label.contains("HEX", ignoreCase = true) ||
                          row.label.contains("Formula", ignoreCase = true) ||
                          row.label.contains("Trouble Code", ignoreCase = true)
                      ) {
                        FontFamily.Monospace
                      } else {
                        FontFamily.Default
                      }
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                }
              }
            }
          }
        }

        // Dialog Actions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          if (item.copyableValue != null) {
            OutlinedButton(
              onClick = { onCopyValue(item.copyableValue) },
              modifier = Modifier.testTag("search_dialog_copy_button"),
            ) {
              Icon(
                imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (isCopied) "Copied" else "Copy")
            }
          } else {
            TextButton(onClick = onDismiss) {
              Text("Close")
            }
          }

          Button(
            onClick = onOpenTool,
            modifier = Modifier
              .semantics { contentDescription = "Open ${item.tool.title}" }
              .testTag("search_dialog_open_tool_button"),
          ) {
            Text("Open ${item.tool.title}")
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Launch,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
  ToolboxTheme {
    SearchScreen(
      customDataSources = SearchDataSources(),
    )
  }
}
