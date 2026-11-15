package com.example.toolbox.ui.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.toolbox.ui.main.NavigationPreferencesRepository
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.ToolCategory
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
  searchPreferencesRepository: SearchPreferencesRepository? = null,
  customDataSources: SearchDataSources? = null,
  initialQuery: String = "",
) {
  val context = LocalContext.current
  val haptics = LocalHapticFeedback.current
  val focusManager = LocalFocusManager.current

  val searchPrefs = remember(context, searchPreferencesRepository) {
    searchPreferencesRepository ?: SharedPreferencesSearchRepository(context)
  }

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

  var searchQuery by rememberSaveable { mutableStateOf(initialQuery) }
  var selectedCategoryFilter by rememberSaveable { mutableStateOf(SearchCategoryFilter.ALL) }
  var selectedToolFilterName by rememberSaveable { mutableStateOf<String?>(null) }
  var directoryCategoryFilterName by rememberSaveable { mutableStateOf<String?>(null) }
  var recentSearches by remember { mutableStateOf(searchPrefs.getRecentSearches()) }
  var pinnedTools by remember { mutableStateOf(searchPrefs.getPinnedTools()) }
  var inspectedResult by remember { mutableStateOf<SearchResultItem?>(null) }
  var copiedItemId by remember { mutableStateOf<String?>(null) }

  val selectedToolFilter = remember(selectedToolFilterName) {
    selectedToolFilterName?.let { name -> Tool.entries.find { it.name == name } }
  }

  val directoryCategoryFilter = remember(directoryCategoryFilterName) {
    directoryCategoryFilterName?.let { name -> ToolCategory.entries.find { it.name == name } }
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

  fun recordRecentSearch(query: String) {
    val trimmed = query.trim()
    if (trimmed.isNotEmpty()) {
      searchPrefs.addRecentSearch(trimmed)
      recentSearches = searchPrefs.getRecentSearches()
    }
  }

  fun launchToolFromSearch(tool: Tool, colorHex: String? = null) {
    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    recordRecentSearch(searchQuery)
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
    // Standard 68.dp Top Header Bar
    Surface(
      tonalElevation = 1.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 68.dp)
          .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.weight(1f),
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(20.dp),
            )
          }

          Column {
            Text(
              text = "Search",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = statusSubtitle,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("search_status_subtitle"),
            )
          }
        }

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
      }
    }

    // Search Input & Filter Chips Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { newValue ->
          searchQuery = newValue
          copiedItemId = null
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("search_query_input"),
        placeholder = {
          Text(
            text = "Search tools, math, units, notes, recipes, DTCs...",
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search Icon",
            tint = MaterialTheme.colorScheme.primary,
          )
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(
              onClick = {
                searchQuery = ""
                selectedToolFilterName = null
              },
              modifier = Modifier.testTag("search_clear_query_button"),
            ) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear search query",
              )
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
          onSearch = {
            recordRecentSearch(searchQuery)
            focusManager.clearFocus()
          }
        ),
      )

      // Domain Category Filter Chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SearchCategoryFilter.entries.forEach { filter ->
          val isSelected = selectedCategoryFilter == filter
          FilterChip(
            selected = isSelected,
            onClick = {
              selectedCategoryFilter = filter
            },
            label = {
              Text(
                text = filter.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
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
                  )
                }
              }
            }
        }
      }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

    // Main Content Area
    if (searchQuery.isBlank()) {
      SearchDiscoveryHomeView(
        recentSearches = recentSearches,
        pinnedTools = pinnedTools,
        hiddenTools = hiddenTools,
        usageCounts = usageCounts,
        selectedCategoryFilter = selectedCategoryFilter,
        directoryCategoryFilter = directoryCategoryFilter,
        onSelectDirectoryCategory = { category ->
          directoryCategoryFilterName = category?.name
        },
        onRunQuery = { query ->
          searchQuery = query
          recordRecentSearch(query)
        },
        onRemoveRecentSearch = { query ->
          searchPrefs.removeRecentSearch(query)
          recentSearches = searchPrefs.getRecentSearches()
        },
        onClearRecentSearches = {
          searchPrefs.clearRecentSearches()
          recentSearches = emptyList()
        },
        onTogglePinTool = { tool ->
          pinnedTools = searchPrefs.togglePinnedTool(tool)
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
            isCopied = copiedItemId == item.id,
            onCardClick = {
              recordRecentSearch(searchQuery)
              if (item.resultType == SearchResultType.TOOL) {
                launchToolFromSearch(item.tool, item.colorHex)
              } else {
                inspectedResult = item
              }
            },
            onInspectClick = {
              recordRecentSearch(searchQuery)
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
  recentSearches: List<String>,
  pinnedTools: List<Tool>,
  hiddenTools: Set<Tool>,
  usageCounts: Map<Tool, Int>,
  selectedCategoryFilter: SearchCategoryFilter,
  directoryCategoryFilter: ToolCategory?,
  onSelectDirectoryCategory: (ToolCategory?) -> Unit,
  onRunQuery: (String) -> Unit,
  onRemoveRecentSearch: (String) -> Unit,
  onClearRecentSearches: () -> Unit,
  onTogglePinTool: (Tool) -> Unit,
  onOpenTool: (Tool) -> Unit,
) {
  val filteredDirectoryTools = remember(selectedCategoryFilter, directoryCategoryFilter) {
    Tool.entries.filter { tool ->
      val matchesSearchCat = when (selectedCategoryFilter) {
        SearchCategoryFilter.ALL, SearchCategoryFilter.TOOLS -> true
        else -> SearchEngine.categoryFilterForTool(tool) == selectedCategoryFilter
      }
      val matchesToolCat = directoryCategoryFilter == null || tool.category == directoryCategoryFilter
      matchesSearchCat && matchesToolCat
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("search_discovery_home"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // 1. Pinned / Quick Launch Tools
    if (pinnedTools.isNotEmpty()) {
      item(key = "pinned_tools_section") {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_pinned_tools_card"),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Star,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp),
                )
                Text(
                  text = "Quick Launch Tools",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                )
              }
              Text(
                text = "Tap any tool to open",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              pinnedTools.forEach { tool ->
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = MaterialTheme.colorScheme.surface,
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenTool(tool) }
                    .testTag("search_quick_launch_${tool.name}"),
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    Box(
                      modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                      contentAlignment = Alignment.Center,
                    ) {
                      Icon(
                        imageVector = tool.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp),
                      )
                    }
                    Text(
                      text = tool.title,
                      style = MaterialTheme.typography.labelLarge,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onSurface,
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // 2. Recent Searches
    if (recentSearches.isNotEmpty()) {
      item(key = "recent_searches_section") {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_recent_searches_card"),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.History,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp),
                )
                Text(
                  text = "Recent Searches",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                )
              }

              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                  .clip(CircleShape)
                  .clickable { onClearRecentSearches() }
                  .testTag("search_clear_recent_button"),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear recent searches",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                  )
                  Text(
                    text = "Clear",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }

            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              recentSearches.forEach { recent ->
                Surface(
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.surface,
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                  modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onRunQuery(recent) }
                    .testTag("search_recent_item_$recent"),
                ) {
                  Row(
                    modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Text(
                      text = recent,
                      style = MaterialTheme.typography.labelMedium,
                      color = MaterialTheme.colorScheme.onSurface,
                    )
                    Icon(
                      imageVector = Icons.Default.Close,
                      contentDescription = "Remove $recent",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .clickable { onRemoveRecentSearch(recent) }
                        .testTag("search_remove_recent_$recent"),
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // 3. Instant Evaluators & Discovery Suggestions
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
            SearchEngine.DISCOVERY_SUGGESTIONS.forEach { suggestion ->
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

    // 4. All Tools Directory Header & Category Filter
    item(key = "directory_header_section") {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "All Tools Directory (${filteredDirectoryTools.size})",
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

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          val allSelected = directoryCategoryFilter == null
          FilterChip(
            selected = allSelected,
            onClick = { onSelectDirectoryCategory(null) },
            label = {
              Text(
                text = "All Categories",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (allSelected) FontWeight.Bold else FontWeight.Medium,
              )
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("search_dir_cat_ALL"),
          )

          ToolCategory.entries.forEach { category ->
            val isSelected = directoryCategoryFilter == category
            FilterChip(
              selected = isSelected,
              onClick = {
                onSelectDirectoryCategory(if (isSelected) null else category)
              },
              label = {
                Text(
                  text = category.title,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                )
              },
              shape = RoundedCornerShape(20.dp),
              modifier = Modifier.testTag("search_dir_cat_${category.name}"),
            )
          }
        }
      }
    }

    // 5. All Tools Directory Cards
    items(filteredDirectoryTools, key = { "dir_${it.name}" }) { tool ->
      val meta = SearchEngine.TOOL_METADATA[tool]
      val isHidden = tool in hiddenTools
      val isPinned = tool in pinnedTools
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
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
              if (isHidden) {
                Surface(
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.surface,
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Default.VisibilityOff,
                      contentDescription = "Hidden in bar",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(11.dp),
                    )
                    Text(
                      text = "Hidden",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
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

          IconButton(
            onClick = { onTogglePinTool(tool) },
            modifier = Modifier
              .size(34.dp)
              .testTag("search_pin_tool_${tool.name}"),
          ) {
            Icon(
              imageVector = if (isPinned) Icons.Default.Star else Icons.Default.StarBorder,
              contentDescription = if (isPinned) "Unpin ${tool.title}" else "Pin ${tool.title}",
              tint = if (isPinned) {
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
  }
}

@Composable
private fun SearchResultCard(
  item: SearchResultItem,
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
            Text(
              text = item.title,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = CircleShape,
              color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
              Text(
                text = "${item.tool.title} • ${item.badgeLabel}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              )
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
      searchPreferencesRepository = InMemorySearchPreferencesRepository(
        initialRecentSearches = listOf("100 km to mi", "P0420", "Pancakes"),
      ),
      customDataSources = SearchDataSources(),
    )
  }
}
