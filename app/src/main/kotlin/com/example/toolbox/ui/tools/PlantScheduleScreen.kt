package com.example.toolbox.ui.tools

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toolbox.ui.theme.ToolboxTheme
import java.util.UUID

@OptIn(
  ExperimentalMaterial3Api::class,
  ExperimentalLayoutApi::class,
  ExperimentalFoundationApi::class,
)
@Composable
fun PlantScheduleScreen(
  modifier: Modifier = Modifier,
  repository: PlantScheduleRepository? = null,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current
  val focusManager = LocalFocusManager.current

  val repo = remember(repository, context, isInspection) {
    repository ?: if (isInspection) {
      InMemoryPlantScheduleRepository(
        initialScheduledPlants = listOf(
          PlantScheduleEngine.BUILT_IN_PLANTS.first { it.id == "tomato" },
          PlantScheduleEngine.BUILT_IN_PLANTS.first { it.id == "basil" },
          PlantScheduleEngine.BUILT_IN_PLANTS.first { it.id == "lettuce" },
          PlantScheduleEngine.BUILT_IN_PLANTS.first { it.id == "carrot" },
        ),
      )
    } else {
      SharedPreferencesPlantScheduleRepository(context)
    }
  }

  var scheduledPlants by remember { mutableStateOf(repo.loadScheduledPlants()) }
  var customPlants by remember { mutableStateOf(repo.loadCustomPlants()) }

  val allCatalogPlants = remember(customPlants) {
    customPlants + PlantScheduleEngine.BUILT_IN_PLANTS
  }

  var searchQuery by rememberSaveable { mutableStateOf("") }
  var isSearchActive by rememberSaveable { mutableStateOf(false) }
  var selectedCategoryName by rememberSaveable { mutableStateOf<String?>(null) }
  val selectedCategory = remember(selectedCategoryName) {
    selectedCategoryName?.let { name ->
      PlantCategory.entries.firstOrNull { it.name == name }
    }
  }

  var selectedMonth by rememberSaveable { mutableIntStateOf(CalendarMonth.currentMonthNumber()) }

  // Multi-selection state (Section 5.5 of DESIGN_LANGUAGE.md)
  var isSelectionMode by rememberSaveable { mutableStateOf(false) }
  var selectedPlantIds by remember { mutableStateOf(setOf<String>()) }

  // Dialog & menu states (Section 5.2 of DESIGN_LANGUAGE.md)
  var showTopOverflowMenu by remember { mutableStateOf(false) }
  var showSelectionOverflowMenu by remember { mutableStateOf(false) }
  var showEditorDialog by remember { mutableStateOf(false) }
  var editingPlant by remember { mutableStateOf<PlantItem?>(null) }
  var initialCustomName by remember { mutableStateOf("") }
  var inspectingPlants by remember { mutableStateOf<List<PlantItem>>(emptyList()) }
  var pendingDeletePlants by remember { mutableStateOf<List<PlantItem>>(emptyList()) }

  val matchingCatalogPlants = remember(allCatalogPlants, searchQuery, selectedCategory) {
    PlantScheduleEngine.searchPlants(
      catalog = allCatalogPlants,
      query = searchQuery,
      category = selectedCategory,
    )
  }

  val scheduledPlantIds = remember(scheduledPlants) {
    scheduledPlants.map { it.id }.toSet()
  }

  val searchFocusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  fun closeSearch() {
    isSearchActive = false
    searchQuery = ""
    selectedCategoryName = null
    focusManager.clearFocus()
  }

  LaunchedEffect(isSearchActive) {
    if (isSearchActive) {
      kotlinx.coroutines.delay(100)
      try {
        searchFocusRequester.requestFocus()
        keyboardController?.show()
      } catch (_: Exception) {}
    } else {
      keyboardController?.hide()
    }
  }

  BackHandler(enabled = isSearchActive) {
    closeSearch()
  }

  fun exitSelectionMode() {
    isSelectionMode = false
    selectedPlantIds = emptySet()
  }

  fun updateSchedule(updated: List<PlantItem>) {
    scheduledPlants = updated
    repo.saveScheduledPlants(updated)
    val remainingIds = updated.map { it.id }.toSet()
    selectedPlantIds = selectedPlantIds.intersect(remainingIds)
    if (updated.isEmpty()) {
      isSelectionMode = false
    }
  }

  fun addPlant(plant: PlantItem) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val updated = PlantScheduleEngine.addPlantToSchedule(scheduledPlants, plant)
    updateSchedule(updated)
  }

  fun removePlant(plantId: String) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val updated = PlantScheduleEngine.removePlantFromSchedule(scheduledPlants, plantId)
    updateSchedule(updated)
  }

  fun removeMultiplePlants(plantIds: Set<String>) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val updated = scheduledPlants.filterNot { it.id in plantIds }
    updateSchedule(updated)
    exitSelectionMode()
  }

  fun duplicatePlantsAsCustom(plants: List<PlantItem>) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    var updatedCustom = customPlants
    var updatedSchedule = scheduledPlants
    plants.forEach { original ->
      val copy = original.copy(
        id = "custom_${UUID.randomUUID()}",
        name = "${original.name} (Copy)",
        isCustom = true,
      )
      updatedCustom = listOf(copy) + updatedCustom
      updatedSchedule = PlantScheduleEngine.addPlantToSchedule(updatedSchedule, copy)
    }
    customPlants = updatedCustom
    repo.saveCustomPlants(updatedCustom)
    updateSchedule(updatedSchedule)
  }

  fun togglePlant(plant: PlantItem) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val updated = PlantScheduleEngine.togglePlantInSchedule(scheduledPlants, plant)
    updateSchedule(updated)
  }

  fun togglePlantSelection(plantId: String) {
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    val next = if (plantId in selectedPlantIds) {
      selectedPlantIds - plantId
    } else {
      selectedPlantIds + plantId
    }
    selectedPlantIds = next
    if (next.isEmpty()) {
      isSelectionMode = false
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("plant_schedule_screen"),
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Section 5.1 & 5.5: Standard Tool Header Bar / Multi-Selection / Integrated Search Toolbar
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp),
          contentAlignment = Alignment.CenterStart,
        ) {
          if (isSelectionMode) {
            val selectedCount = selectedPlantIds.size
            val allSelected = scheduledPlants.isNotEmpty() && selectedCount == scheduledPlants.size
            val selectedItems = remember(scheduledPlants, selectedPlantIds) {
              scheduledPlants.filter { it.id in selectedPlantIds }
            }

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp)
                .testTag("multi_select_toolbar"),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              IconButton(
                onClick = { exitSelectionMode() },
                modifier = Modifier.testTag("exit_selection_mode_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Exit selection",
                  tint = MaterialTheme.colorScheme.onSurface,
                )
              }

              Checkbox(
                checked = allSelected,
                onCheckedChange = { checked ->
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  selectedPlantIds = if (checked) {
                    scheduledPlants.map { it.id }.toSet()
                  } else {
                    emptySet()
                  }
                },
                modifier = Modifier.testTag("select_all_plants_checkbox"),
              )

              Text(
                text = "$selectedCount selected",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                  .weight(1f)
                  .padding(start = 4.dp),
              )

              IconButton(
                onClick = { inspectingPlants = selectedItems },
                enabled = selectedCount > 0,
                modifier = Modifier.testTag("bulk_info_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.Info,
                  contentDescription = "Inspect selected plants",
                  tint = if (selectedCount > 0) {
                    MaterialTheme.colorScheme.onSurface
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                  },
                )
              }

              IconButton(
                onClick = { pendingDeletePlants = selectedItems },
                enabled = selectedCount > 0,
                modifier = Modifier.testTag("bulk_delete_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Delete selected plants",
                  tint = if (selectedCount > 0) {
                    MaterialTheme.colorScheme.error
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                  },
                )
              }

              Box {
                IconButton(
                  onClick = { showSelectionOverflowMenu = true },
                  enabled = selectedCount > 0,
                  modifier = Modifier.testTag("bulk_overflow_menu_button"),
                ) {
                  Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More bulk actions",
                  )
                }
                DropdownMenu(
                  expanded = showSelectionOverflowMenu,
                  onDismissRequest = { showSelectionOverflowMenu = false },
                ) {
                  if (selectedCount == 1) {
                    DropdownMenuItem(
                      text = { Text("Customize Schedule") },
                      leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                      onClick = {
                        showSelectionOverflowMenu = false
                        editingPlant = selectedItems.firstOrNull()
                        initialCustomName = ""
                        exitSelectionMode()
                        showEditorDialog = true
                      },
                    )
                  }
                  DropdownMenuItem(
                    text = { Text("Duplicate Selected") },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                    onClick = {
                      showSelectionOverflowMenu = false
                      duplicatePlantsAsCustom(selectedItems)
                      exitSelectionMode()
                    },
                  )
                  DropdownMenuItem(
                    text = {
                      Text(
                        text = "Remove Selected",
                        color = MaterialTheme.colorScheme.error,
                      )
                    },
                    leadingIcon = {
                      Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                      )
                    },
                    onClick = {
                      showSelectionOverflowMenu = false
                      pendingDeletePlants = selectedItems
                    },
                  )
                }
              }
            }
          } else {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                    imageVector = Icons.Default.Yard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                  )
                }
                Column {
                  Text(
                    text = "Planting & Harvest",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = "${scheduledPlants.size} scheduled • ${CalendarMonth.fromNumber(selectedMonth).fullName} focus",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }

              // Trailing IconButton actions per Section 5.1
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
              ) {
                IconButton(
                  onClick = { isSearchActive = true },
                  modifier = Modifier.testTag("toggle_search_bar_button"),
                ) {
                  Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search plants",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }

                Box {
                  IconButton(
                    onClick = { showTopOverflowMenu = true },
                    modifier = Modifier.testTag("header_overflow_menu_button"),
                  ) {
                    Icon(
                      imageVector = Icons.Default.MoreVert,
                      contentDescription = "More options",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                  DropdownMenu(
                    expanded = showTopOverflowMenu,
                    onDismissRequest = { showTopOverflowMenu = false },
                  ) {
                    DropdownMenuItem(
                      text = { Text("Add Custom Plant") },
                      leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                      onClick = {
                        showTopOverflowMenu = false
                        editingPlant = null
                        initialCustomName = searchQuery.trim()
                        showEditorDialog = true
                      },
                    )
                    if (scheduledPlants.isNotEmpty()) {
                      DropdownMenuItem(
                        text = { Text("Select Plants") },
                        leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
                        onClick = {
                          showTopOverflowMenu = false
                          isSelectionMode = true
                          selectedPlantIds = scheduledPlants.firstOrNull()?.let { setOf(it.id) } ?: emptySet()
                        },
                      )
                      DropdownMenuItem(
                        text = {
                          Text(
                            text = "Clear Schedule",
                            color = MaterialTheme.colorScheme.error,
                          )
                        },
                        leadingIcon = {
                          Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                          )
                        },
                        onClick = {
                          showTopOverflowMenu = false
                          pendingDeletePlants = scheduledPlants
                        },
                      )
                    }
                  }
                }
              }
            }
          }

          // Animated Expanding Search Bar integrated into the Toolbar
          androidx.compose.animation.AnimatedVisibility(
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
                .testTag("toolbar_search_bar"),
              color = MaterialTheme.colorScheme.surface,
              tonalElevation = 2.dp,
            ) {
              Row(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                IconButton(
                  onClick = { closeSearch() },
                  modifier = Modifier.testTag("close_search_button"),
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Close search",
                    tint = MaterialTheme.colorScheme.onSurface,
                  )
                }

                TextField(
                  value = searchQuery,
                  onValueChange = { searchQuery = it },
                  placeholder = {
                    Text(
                      text = "Search plants to add (e.g. Tomato, Basil, May)...",
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
                    if (searchQuery.isNotEmpty()) {
                      IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.testTag("plant_search_clear"),
                      ) {
                        Icon(
                          imageVector = Icons.Default.Clear,
                          contentDescription = "Clear search",
                          tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                      }
                    }
                  },
                  keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                  keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                  modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(searchFocusRequester)
                    .testTag("plant_search_input"),
                )
              }
            }
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

      // Tool Content Area + Search Catalog Overlay
      Box(
        modifier = Modifier
          .fillMaxSize()
          .weight(1f),
      ) {
        // Main Scrollable Tool Content
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(
            start = 16.dp,
            top = 14.dp,
            end = 16.dp,
            bottom = 88.dp, // Extra bottom padding for FAB
          ),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          // 1. Monthly Focus Breakdown / Garden Checklist Card (at the top)
          item {
            MonthActivitySummaryCard(
              monthNumber = selectedMonth,
              scheduledPlants = scheduledPlants,
              onSelectMonth = { selectedMonth = it },
            )
          }

          // 2. Year View Card (12-Month Matrix with Theme & Accent Colors)
          item {
            YearViewScheduleCard(
              scheduledPlants = scheduledPlants,
              selectedMonth = selectedMonth,
              isSelectionMode = isSelectionMode,
              selectedPlantIds = selectedPlantIds,
              onSelectMonth = { monthNum ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                selectedMonth = monthNum
              },
              onEditPlant = { plant ->
                editingPlant = plant
                initialCustomName = ""
                showEditorDialog = true
              },
              onInspectPlant = { plant ->
                inspectingPlants = listOf(plant)
              },
              onDuplicatePlant = { plant ->
                duplicatePlantsAsCustom(listOf(plant))
              },
              onRemovePlant = ::removePlant,
              onEnterSelectionMode = { plantId ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isSelectionMode = true
                selectedPlantIds = selectedPlantIds + plantId
              },
              onToggleSelectPlant = ::togglePlantSelection,
              onBrowseCatalog = { isSearchActive = true },
            )
          }
        }

        // Plant List & Pill Filters Overlay shown over the tool when search is active
        androidx.compose.animation.AnimatedVisibility(
          visible = isSearchActive && !isSelectionMode,
          enter = fadeIn(animationSpec = tween(200)) + expandVertically(expandFrom = Alignment.Top),
          exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(shrinkTowards = Alignment.Top),
          modifier = Modifier.fillMaxSize(),
        ) {
          CatalogSearchOverlay(
            allCatalogCount = allCatalogPlants.size,
            query = searchQuery,
            selectedCategory = selectedCategory,
            onSelectCategory = { category ->
              selectedCategoryName = category?.name
            },
            matchingPlants = matchingCatalogPlants,
            scheduledPlantIds = scheduledPlantIds,
            onAddPlant = ::addPlant,
            onRemovePlant = ::removePlant,
            onTogglePlant = ::togglePlant,
            onInspectPlant = { plant -> inspectingPlants = listOf(plant) },
            onCreateCustomWithQuery = {
              editingPlant = null
              initialCustomName = searchQuery.trim()
              showEditorDialog = true
            },
          )
        }
      }
    }

    // Section 5.3: Floating Action Button (CircleShape, primaryContainer, globally recognizable Add icon)
    FloatingActionButton(
      onClick = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        editingPlant = null
        initialCustomName = searchQuery.trim()
        showEditorDialog = true
      },
      shape = CircleShape,
      containerColor = MaterialTheme.colorScheme.primaryContainer,
      contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp)
        .testTag("add_custom_plant_button"),
    ) {
      Icon(
        imageVector = Icons.Default.Add,
        contentDescription = "Add Custom Plant",
      )
    }
  }

  // Section 5.2: Add / Edit Custom Plant Dialog
  if (showEditorDialog) {
    PlantEditorDialog(
      existingPlant = editingPlant,
      initialName = initialCustomName,
      onDismiss = {
        showEditorDialog = false
        editingPlant = null
      },
      onConfirm = { savedPlant ->
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (savedPlant.isCustom) {
          val updatedCustom = listOf(savedPlant) + customPlants.filterNot { it.id == savedPlant.id }
          customPlants = updatedCustom
          repo.saveCustomPlants(updatedCustom)
        }
        val updatedSchedule = if (scheduledPlants.any { it.id == savedPlant.id }) {
          PlantScheduleEngine.updateScheduledPlant(scheduledPlants, savedPlant)
        } else {
          PlantScheduleEngine.addPlantToSchedule(scheduledPlants, savedPlant)
        }
        updateSchedule(updatedSchedule)
        showEditorDialog = false
        editingPlant = null
      },
    )
  }

  // Section 5.2: Inspect Plant Details Dialog
  if (inspectingPlants.isNotEmpty()) {
    PlantDetailsDialog(
      plants = inspectingPlants,
      onDismiss = { inspectingPlants = emptyList() },
      onCustomize = { plant ->
        inspectingPlants = emptyList()
        editingPlant = plant
        initialCustomName = ""
        showEditorDialog = true
      },
    )
  }

  // Section 5.2: Destructive Delete Confirmation Dialog
  if (pendingDeletePlants.isNotEmpty()) {
    DeletePlantsConfirmDialog(
      plants = pendingDeletePlants,
      onDismiss = { pendingDeletePlants = emptyList() },
      onConfirmDelete = {
        val idsToDelete = pendingDeletePlants.map { it.id }.toSet()
        pendingDeletePlants = emptyList()
        removeMultiplePlants(idsToDelete)
      },
    )
  }
}

@Composable
private fun CatalogSearchOverlay(
  allCatalogCount: Int,
  query: String,
  selectedCategory: PlantCategory?,
  onSelectCategory: (PlantCategory?) -> Unit,
  matchingPlants: List<PlantItem>,
  scheduledPlantIds: Set<String>,
  onAddPlant: (PlantItem) -> Unit,
  onRemovePlant: (String) -> Unit,
  onTogglePlant: (PlantItem) -> Unit,
  onInspectPlant: (PlantItem) -> Unit,
  onCreateCustomWithQuery: () -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.background,
    modifier = Modifier
      .fillMaxSize()
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = {},
      )
      .testTag("catalog_search_overlay"),
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Category Filter Chips Bar (Section 4: RoundedCornerShape(20.dp))
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
      ) {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("catalog_category_pills"),
        ) {
          item {
            FilterChip(
              selected = selectedCategory == null,
              onClick = { onSelectCategory(null) },
              label = {
                Text(
                  text = "All ($allCatalogCount)",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Medium,
                )
              },
              shape = RoundedCornerShape(20.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
              ),
              modifier = Modifier.testTag("category_chip_ALL"),
            )
          }
          items(PlantCategory.entries) { category ->
            val isSelected = selectedCategory == category
            FilterChip(
              selected = isSelected,
              onClick = {
                onSelectCategory(if (isSelected) null else category)
              },
              label = {
                Text(
                  text = category.displayName,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Medium,
                )
              },
              shape = RoundedCornerShape(20.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
              ),
              modifier = Modifier.testTag("category_chip_${category.name}"),
            )
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

      // Plant List
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .weight(1f)
          .testTag("catalog_results_section"),
        contentPadding = PaddingValues(
          start = 16.dp,
          top = 12.dp,
          end = 16.dp,
          bottom = 88.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        item(key = "catalog_header") {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column {
              Text(
                text = if (query.isNotBlank()) {
                  "Search Results (${matchingPlants.size})"
                } else if (selectedCategory != null) {
                  "${selectedCategory.displayName} (${matchingPlants.size})"
                } else {
                  "Plant Catalog (${matchingPlants.size})"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "Tap + to add plants to your yearly planting & harvest schedule",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        if (matchingPlants.isEmpty()) {
          item(key = "catalog_empty") {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              ),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = "No plants found matching \"$query\"",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = "Create a custom plant entry with your own planting and harvesting months.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center,
                )
                FilledTonalButton(
                  onClick = onCreateCustomWithQuery,
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.testTag("create_custom_from_search_button"),
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Create \"${query.trim()}\"")
                }
              }
            }
          }
        } else {
          items(
            items = matchingPlants,
            key = { it.id },
          ) { plant ->
            val isScheduled = plant.id in scheduledPlantIds
            CatalogPlantItemCard(
              plant = plant,
              isScheduled = isScheduled,
              onAdd = { onAddPlant(plant) },
              onRemove = { onRemovePlant(plant.id) },
              onToggle = { onTogglePlant(plant) },
              onInspect = { onInspectPlant(plant) },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun CatalogPlantItemCard(
  plant: PlantItem,
  isScheduled: Boolean,
  onAdd: () -> Unit,
  onRemove: () -> Unit,
  onToggle: () -> Unit,
  onInspect: () -> Unit,
) {
  var showItemMenu by remember { mutableStateOf(false) }

  // Section 5.4: Card with surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(14.dp), subtle border
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(
      width = 1.dp,
      color = if (isScheduled) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
      } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
      },
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable { onToggle() }
      .testTag("catalog_plant_card_${plant.id}"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f),
        ) {
          // Leading CircleShape icon badge (Section 4 & 5.4)
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(
                if (isScheduled) {
                  MaterialTheme.colorScheme.primaryContainer
                } else {
                  MaterialTheme.colorScheme.secondaryContainer
                },
              ),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = if (isScheduled) Icons.Default.Check else Icons.Default.Yard,
              contentDescription = null,
              tint = if (isScheduled) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.onSecondaryContainer
              },
              modifier = Modifier.size(20.dp),
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = plant.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
              ) {
                Text(
                  text = plant.category.displayName,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
            }

            // Bullet-separated secondary metadata (Section 3 & 5.4)
            val metadataParts = buildList {
              if (plant.scientificName.isNotBlank()) add(plant.scientificName)
              if (plant.sunRequirement.isNotBlank()) add(plant.sunRequirement)
              if (plant.daysToMaturity.isNotBlank()) add(plant.daysToMaturity)
            }
            if (metadataParts.isNotEmpty()) {
              Text(
                text = metadataParts.joinToString(" • "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          if (isScheduled) {
            FilledTonalButton(
              onClick = onRemove,
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
              ),
              modifier = Modifier.testTag("added_plant_button_${plant.id}"),
            ) {
              Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Added", style = MaterialTheme.typography.labelMedium)
            }
          } else {
            Button(
              onClick = onAdd,
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              modifier = Modifier.testTag("add_plant_button_${plant.id}"),
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Add", style = MaterialTheme.typography.labelMedium)
            }
          }

          Box {
            IconButton(
              onClick = { showItemMenu = true },
              modifier = Modifier
                .size(32.dp)
                .testTag("catalog_plant_more_${plant.id}"),
            ) {
              Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options for ${plant.name}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
              )
            }
            DropdownMenu(
              expanded = showItemMenu,
              onDismissRequest = { showItemMenu = false },
            ) {
              DropdownMenuItem(
                text = { Text("Inspect Details") },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                onClick = {
                  showItemMenu = false
                  onInspect()
                },
              )
              DropdownMenuItem(
                text = { Text(if (isScheduled) "Remove from Schedule" else "Add to Schedule") },
                leadingIcon = {
                  Icon(
                    imageVector = if (isScheduled) Icons.Default.DeleteOutline else Icons.Default.Add,
                    contentDescription = null,
                  )
                },
                onClick = {
                  showItemMenu = false
                  if (isScheduled) onRemove() else onAdd()
                },
              )
            }
          }
        }
      }

      // Range Badges & Mini 12-Month Preview Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        ScheduleRangeBadge(
          label = "Plant: ${plant.plantingSummary()}",
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          dotColor = MaterialTheme.colorScheme.primary,
        )
        ScheduleRangeBadge(
          label = "Harvest: ${plant.harvestSummary()}",
          containerColor = MaterialTheme.colorScheme.tertiaryContainer,
          contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
          dotColor = MaterialTheme.colorScheme.tertiary,
        )
      }

      PlantYearMonthBar(
        plant = plant,
        selectedMonth = null,
        onMonthClick = null,
      )
    }
  }
}

@Composable
private fun ScheduleRangeBadge(
  label: String,
  containerColor: Color,
  contentColor: Color,
  dotColor: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(20.dp),
    color = containerColor,
    modifier = modifier,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
      Box(
        modifier = Modifier
          .size(7.dp)
          .clip(CircleShape)
          .background(dotColor),
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = contentColor,
      )
    }
  }
}

@Composable
private fun YearViewScheduleCard(
  scheduledPlants: List<PlantItem>,
  selectedMonth: Int,
  isSelectionMode: Boolean,
  selectedPlantIds: Set<String>,
  onSelectMonth: (Int) -> Unit,
  onEditPlant: (PlantItem) -> Unit,
  onInspectPlant: (PlantItem) -> Unit,
  onDuplicatePlant: (PlantItem) -> Unit,
  onRemovePlant: (String) -> Unit,
  onEnterSelectionMode: (String) -> Unit,
  onToggleSelectPlant: (String) -> Unit,
  onBrowseCatalog: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("year_view_card"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Section Title + Theme/Accent Color Legend
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp),
            )
          }
          Column {
            Text(
              text = "Annual Planting & Harvest Schedule",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Tap any month column to inspect • Long-press plants to multi-select",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      // Legend Row highlighting Theme (Primary) vs Accent (Tertiary)
      YearScheduleLegendRow()

      // 12-Month Header Strip
      MonthHeaderRow(
        selectedMonth = selectedMonth,
        onSelectMonth = onSelectMonth,
      )

      if (scheduledPlants.isEmpty()) {
        // Empty Schedule State
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("schedule_empty_state"),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Yard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
              )
            }
            Text(
              text = "Your schedule is empty",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Search for plants above or browse the catalog to add crops and see their planting and harvest windows across the year.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
            )
            OutlinedButton(
              onClick = onBrowseCatalog,
              shape = RoundedCornerShape(12.dp),
            ) {
              Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Browse Plant Catalog")
            }
          }
        }
      } else {
        Column(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("scheduled_plants_list"),
        ) {
          scheduledPlants.forEach { plant ->
            val isSelected = plant.id in selectedPlantIds
            ScheduledPlantYearRow(
              plant = plant,
              selectedMonth = selectedMonth,
              isSelectionMode = isSelectionMode,
              isSelected = isSelected,
              onSelectMonth = onSelectMonth,
              onEdit = { onEditPlant(plant) },
              onInspect = { onInspectPlant(plant) },
              onDuplicate = { onDuplicatePlant(plant) },
              onRemove = { onRemovePlant(plant.id) },
              onLongPress = { onEnterSelectionMode(plant.id) },
              onToggleSelection = { onToggleSelectPlant(plant.id) },
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun YearScheduleLegendRow() {
  FlowRow(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("year_schedule_legend"),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    // Theme Color = Planting
    LegendChip(
      label = "Planting (Theme)",
      boxColor = MaterialTheme.colorScheme.primary,
      containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
      contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
      testTag = "legend_planting_theme",
    )
    // Accent Color = Harvesting
    LegendChip(
      label = "Harvesting (Accent)",
      boxColor = MaterialTheme.colorScheme.tertiary,
      containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f),
      contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
      testTag = "legend_harvesting_accent",
    )
    // Both Planting & Harvesting
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier.testTag("legend_both_overlap"),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Row(
          modifier = Modifier
            .size(12.dp)
            .clip(CircleShape),
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .background(MaterialTheme.colorScheme.primary),
          )
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .background(MaterialTheme.colorScheme.tertiary),
          )
        }
        Text(
          text = "Plant & Harvest",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun LegendChip(
  label: String,
  boxColor: Color,
  containerColor: Color,
  contentColor: Color,
  testTag: String,
) {
  Surface(
    shape = RoundedCornerShape(20.dp),
    color = containerColor,
    modifier = Modifier.testTag(testTag),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Box(
        modifier = Modifier
          .size(10.dp)
          .clip(CircleShape)
          .background(boxColor),
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = contentColor,
      )
    }
  }
}

@Composable
private fun MonthHeaderRow(
  selectedMonth: Int,
  onSelectMonth: (Int) -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("year_month_header_row"),
    horizontalArrangement = Arrangement.spacedBy(3.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    CalendarMonth.entries.forEach { month ->
      val isSelected = month.number == selectedMonth
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) {
          MaterialTheme.colorScheme.primaryContainer
        } else {
          MaterialTheme.colorScheme.surface
        },
        border = if (isSelected) {
          BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else {
          BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        },
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(8.dp))
          .clickable { onSelectMonth(month.number) }
          .testTag("month_header_${month.number}"),
      ) {
        Column(
          modifier = Modifier.padding(vertical = 6.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = month.shortLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.onSurface
            },
          )
          Text(
            text = month.shortName,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = if (isSelected) {
              MaterialTheme.colorScheme.onPrimaryContainer
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScheduledPlantYearRow(
  plant: PlantItem,
  selectedMonth: Int,
  isSelectionMode: Boolean,
  isSelected: Boolean,
  onSelectMonth: (Int) -> Unit,
  onEdit: () -> Unit,
  onInspect: () -> Unit,
  onDuplicate: () -> Unit,
  onRemove: () -> Unit,
  onLongPress: () -> Unit,
  onToggleSelection: () -> Unit,
) {
  var expandedDetails by rememberSaveable(plant.id) { mutableStateOf(false) }
  var showItemMenu by remember { mutableStateOf(false) }

  // Section 5.4 & 5.5: Standard Card with surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(14.dp),
  // combinedClickable for long-press multi-select, and Checkbox replacement in selection mode.
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
      .combinedClickable(
        onClick = {
          if (isSelectionMode) {
            onToggleSelection()
          } else {
            expandedDetails = !expandedDetails
          }
        },
        onLongClick = {
          if (!isSelectionMode) {
            onLongPress()
          } else {
            onToggleSelection()
          }
        },
      )
      .testTag("scheduled_plant_item_${plant.id}"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f),
        ) {
          // Section 5.5: Leading CircleShape icon badge swaps to Checkbox during multi-select mode
          if (isSelectionMode) {
            Checkbox(
              checked = isSelected,
              onCheckedChange = { onToggleSelection() },
              modifier = Modifier
                .size(36.dp)
                .testTag("select_scheduled_plant_${plant.id}"),
            )
          } else {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Yard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
              )
            }
          }

          Column(modifier = Modifier.weight(1f)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = plant.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
              ) {
                Text(
                  text = plant.category.displayName,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
            }
            Text(
              text = "Plant: ${plant.plantingSummary()} • Harvest: ${plant.harvestSummary()}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        // Quick action icons + Section 5.4 MoreVert contextual menu
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          IconButton(
            onClick = onEdit,
            modifier = Modifier
              .size(32.dp)
              .testTag("edit_scheduled_plant_${plant.id}"),
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit ${plant.name} schedule",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp),
            )
          }
          IconButton(
            onClick = onRemove,
            modifier = Modifier
              .size(32.dp)
              .testTag("remove_scheduled_plant_${plant.id}"),
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Remove ${plant.name} from schedule",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(18.dp),
            )
          }
          Box {
            IconButton(
              onClick = { showItemMenu = true },
              modifier = Modifier
                .size(32.dp)
                .testTag("more_scheduled_plant_${plant.id}"),
            ) {
              Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options for ${plant.name}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
              )
            }
            DropdownMenu(
              expanded = showItemMenu,
              onDismissRequest = { showItemMenu = false },
            ) {
              DropdownMenuItem(
                text = { Text("Inspect Details") },
                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                onClick = {
                  showItemMenu = false
                  onInspect()
                },
              )
              DropdownMenuItem(
                text = { Text("Customize Schedule") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                onClick = {
                  showItemMenu = false
                  onEdit()
                },
              )
              DropdownMenuItem(
                text = { Text("Duplicate as Custom") },
                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                onClick = {
                  showItemMenu = false
                  onDuplicate()
                },
              )
              DropdownMenuItem(
                text = {
                  Text(
                    text = "Remove from Schedule",
                    color = MaterialTheme.colorScheme.error,
                  )
                },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                  )
                },
                onClick = {
                  showItemMenu = false
                  onRemove()
                },
              )
            }
          }
        }
      }

      // 12-Month Year Bar with Theme (Primary) & Accent (Tertiary) Highlights
      PlantYearMonthBar(
        plant = plant,
        selectedMonth = selectedMonth,
        onMonthClick = onSelectMonth,
      )

      // Expandable horticultural details
      AnimatedVisibility(visible = expandedDetails) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
          val metaItems = buildList {
            if (plant.sunRequirement.isNotBlank()) add("Sun: ${plant.sunRequirement}")
            if (plant.daysToMaturity.isNotBlank()) add("Maturity: ${plant.daysToMaturity}")
            if (plant.spacing.isNotBlank()) add("Spacing: ${plant.spacing}")
          }
          if (metaItems.isNotEmpty()) {
            Text(
              text = metaItems.joinToString(" • "),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          if (plant.notes.isNotBlank()) {
            Text(
              text = plant.notes,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }
      }
    }
  }
}

/**
 * Renders the 12-month horizontal year bar for a [PlantItem].
 * - Planting months use `MaterialTheme.colorScheme.primary` (Theme color)
 * - Harvesting months use `MaterialTheme.colorScheme.tertiary` (Accent color)
 * - Months with both planting and harvesting render a dual-toned split cell
 */
@Composable
fun PlantYearMonthBar(
  plant: PlantItem,
  selectedMonth: Int? = null,
  onMonthClick: ((Int) -> Unit)? = null,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("year_bar_${plant.id}"),
    horizontalArrangement = Arrangement.spacedBy(3.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    CalendarMonth.entries.forEach { month ->
      val activity = plant.activityForMonth(month.number)
      val isSelectedMonth = selectedMonth == month.number
      val stateLabel = when (activity) {
        MonthActivityState.PLANTING -> "Planting"
        MonthActivityState.HARVESTING -> "Harvesting"
        MonthActivityState.BOTH -> "Planting and Harvesting"
        MonthActivityState.NONE -> "Inactive"
      }

      Box(
        modifier = Modifier
          .weight(1f)
          .height(26.dp)
          .clip(RoundedCornerShape(6.dp))
          .then(
            if (isSelectedMonth) {
              Modifier.border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(6.dp),
              )
            } else {
              Modifier
            },
          )
          .then(
            if (onMonthClick != null) {
              Modifier.clickable { onMonthClick(month.number) }
            } else {
              Modifier
            },
          )
          .semantics {
            contentDescription = "${plant.name} ${month.shortName}: $stateLabel"
          }
          .testTag("year_cell_${plant.id}_${month.number}"),
        contentAlignment = Alignment.Center,
      ) {
        when (activity) {
          MonthActivityState.PLANTING -> {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = "P",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
              )
            }
          }

          MonthActivityState.HARVESTING -> {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.tertiary),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = "H",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiary,
              )
            }
          }

          MonthActivityState.BOTH -> {
            Row(modifier = Modifier.fillMaxSize()) {
              Box(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight()
                  .background(MaterialTheme.colorScheme.primary),
              )
              Box(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight()
                  .background(MaterialTheme.colorScheme.tertiary),
              )
            }
            Text(
              text = "P/H",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimary,
            )
          }

          MonthActivityState.NONE -> {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = month.shortLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MonthActivitySummaryCard(
  monthNumber: Int,
  scheduledPlants: List<PlantItem>,
  onSelectMonth: (Int) -> Unit,
) {
  val month = CalendarMonth.fromNumber(monthNumber)
  val plantingThisMonth = remember(scheduledPlants, monthNumber) {
    PlantScheduleEngine.plantsForPlantingInMonth(scheduledPlants, monthNumber)
  }
  val harvestingThisMonth = remember(scheduledPlants, monthNumber) {
    PlantScheduleEngine.plantsForHarvestInMonth(scheduledPlants, monthNumber)
  }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("month_summary_card"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      val prevMonth = if (monthNumber == 1) 12 else monthNumber - 1
      val nextMonth = if (monthNumber == 12) 1 else monthNumber + 1

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        IconButton(
          onClick = { onSelectMonth(prevMonth) },
          modifier = Modifier
            .size(36.dp)
            .testTag("prev_month_button"),
        ) {
          Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = "Previous month (${CalendarMonth.fromNumber(prevMonth).fullName})",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.weight(1f),
        ) {
          Text(
            text = month.fullName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${plantingThisMonth.size} to plant • ${harvestingThisMonth.size} to harvest",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        IconButton(
          onClick = { onSelectMonth(nextMonth) },
          modifier = Modifier
            .size(36.dp)
            .testTag("next_month_button"),
        ) {
          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Next month (${CalendarMonth.fromNumber(nextMonth).fullName})",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // Theme-colored Planting Column
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
          modifier = Modifier
            .weight(1f)
            .testTag("month_planting_column"),
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary),
              )
              Text(
                text = "Plant",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
            }
            if (plantingThisMonth.isEmpty()) {
              Text(
                text = "No scheduled plantings",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            } else {
              plantingThisMonth.forEach { plant ->
                Text(
                  text = "• ${plant.name}",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
            }
          }
        }

        // Accent-colored Harvesting Column
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
          modifier = Modifier
            .weight(1f)
            .testTag("month_harvesting_column"),
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.tertiary),
              )
              Text(
                text = "Harvest",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
              )
            }
            if (harvestingThisMonth.isEmpty()) {
              Text(
                text = "No scheduled harvests",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            } else {
              harvestingThisMonth.forEach { plant ->
                Text(
                  text = "• ${plant.name}",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Medium,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlantEditorDialog(
  existingPlant: PlantItem?,
  initialName: String,
  onDismiss: () -> Unit,
  onConfirm: (PlantItem) -> Unit,
) {
  val isEditing = existingPlant != null
  val initialText = existingPlant?.name ?: initialName
  var nameField by remember {
    mutableStateOf(
      TextFieldValue(
        text = initialText,
        selection = TextRange(0, initialText.length),
      ),
    )
  }
  var selectedCategory by remember {
    mutableStateOf(existingPlant?.category ?: PlantCategory.VEGETABLES)
  }
  var plantingMonths by remember {
    mutableStateOf(existingPlant?.normalizedPlantingMonths ?: setOf(4, 5))
  }
  var harvestMonths by remember {
    mutableStateOf(existingPlant?.normalizedHarvestMonths ?: setOf(7, 8, 9))
  }
  var notesText by remember {
    mutableStateOf(existingPlant?.notes ?: "")
  }

  // Section 5.2 & Section 6: Autofocus and pre-select text on both create AND edit
  val focusRequester = remember { FocusRequester() }
  LaunchedEffect(Unit) {
    try {
      focusRequester.requestFocus()
    } catch (_: Exception) {}
  }

  val trimmedName = nameField.text.trim()
  val isNameBlank = trimmedName.isEmpty()
  val hasNoMonthsSelected = plantingMonths.isEmpty() && harvestMonths.isEmpty()

  val isUnchanged = existingPlant != null &&
    trimmedName == existingPlant.name &&
    selectedCategory == existingPlant.category &&
    plantingMonths == existingPlant.normalizedPlantingMonths &&
    harvestMonths == existingPlant.normalizedHarvestMonths &&
    notesText.trim() == existingPlant.notes.trim()

  val previewPlant = remember(trimmedName, selectedCategory, plantingMonths, harvestMonths, notesText) {
    PlantItem(
      id = existingPlant?.id ?: "custom_${UUID.randomUUID()}",
      name = trimmedName.ifEmpty { "Plant Preview" },
      scientificName = existingPlant?.scientificName ?: "",
      category = selectedCategory,
      plantingMonths = plantingMonths,
      harvestMonths = harvestMonths,
      sunRequirement = existingPlant?.sunRequirement ?: "Full Sun",
      daysToMaturity = existingPlant?.daysToMaturity ?: "",
      spacing = existingPlant?.spacing ?: "",
      notes = notesText.trim(),
      isCustom = existingPlant?.isCustom ?: true,
    )
  }

  // Section 6: Disable confirm buttons when inputs are invalid OR unchanged
  val canConfirm = !isNameBlank && !hasNoMonthsSelected && !isUnchanged

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Yard,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = if (isEditing) "Customize Schedule" else "Add Custom Plant",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Select planting (theme) and harvest (accent) months",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        // Section 5.2: Context / Preview Card showing live Year Bar
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = previewPlant.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = "${selectedCategory.displayName} • Plant: ${previewPlant.plantingSummary()} • Harvest: ${previewPlant.harvestSummary()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            PlantYearMonthBar(plant = previewPlant)
          }
        }

        // Plant Name Input with inline validation (Section 5.2 & Section 6)
        OutlinedTextField(
          value = nameField,
          onValueChange = { nameField = it },
          label = { Text("Plant name") },
          singleLine = true,
          isError = nameField.text.isNotEmpty() && isNameBlank,
          supportingText = if (nameField.text.isNotEmpty() && isNameBlank) {
            { Text("Plant name cannot be blank") }
          } else if (hasNoMonthsSelected) {
            {
              Text(
                text = "Select at least one planting or harvesting month",
                color = MaterialTheme.colorScheme.error,
              )
            }
          } else {
            null
          },
          shape = RoundedCornerShape(14.dp),
          leadingIcon = {
            Icon(Icons.Default.Yard, contentDescription = null)
          },
          trailingIcon = {
            if (nameField.text.isNotEmpty()) {
              IconButton(onClick = { nameField = TextFieldValue("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear name")
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag("custom_plant_name_input"),
        )

        // Category Selection
        Text(
          text = "Category",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          PlantCategory.entries.forEach { cat ->
            FilterChip(
              selected = selectedCategory == cat,
              onClick = { selectedCategory = cat },
              label = {
                Text(
                  text = cat.displayName,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Medium,
                )
              },
              shape = RoundedCornerShape(20.dp),
            )
          }
        }

        // Planting Months Selector (Theme Color)
        Text(
          text = "Planting Months (Theme Color)",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.primary,
        )
        MonthMultiSelectorRow(
          selectedMonths = plantingMonths,
          activeContainerColor = MaterialTheme.colorScheme.primary,
          activeContentColor = MaterialTheme.colorScheme.onPrimary,
          tagPrefix = "dialog_planting_month",
          onToggleMonth = { m ->
            plantingMonths = if (m in plantingMonths) plantingMonths - m else plantingMonths + m
          },
        )

        // Harvesting Months Selector (Accent Color)
        Text(
          text = "Harvesting Months (Accent Color)",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.tertiary,
        )
        MonthMultiSelectorRow(
          selectedMonths = harvestMonths,
          activeContainerColor = MaterialTheme.colorScheme.tertiary,
          activeContentColor = MaterialTheme.colorScheme.onTertiary,
          tagPrefix = "dialog_harvest_month",
          onToggleMonth = { m ->
            harvestMonths = if (m in harvestMonths) harvestMonths - m else harvestMonths + m
          },
        )

        // Optional Notes
        OutlinedTextField(
          value = notesText,
          onValueChange = { notesText = it },
          label = { Text("Growing tips / notes (optional)") },
          shape = RoundedCornerShape(14.dp),
          maxLines = 3,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_plant_notes_input"),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirm(
            previewPlant.copy(
              name = trimmedName,
            ),
          )
        },
        enabled = canConfirm,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("confirm_custom_plant_button"),
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (isEditing) "Save" else "Add to Schedule")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
      ) {
        Text("Cancel")
      }
    },
  )
}

/**
 * Section 5.2: Inspect Plant Details Dialog following the unified Dialog Design Pattern.
 */
@Composable
private fun PlantDetailsDialog(
  plants: List<PlantItem>,
  onDismiss: () -> Unit,
  onCustomize: (PlantItem) -> Unit,
) {
  val singlePlant = plants.singleOrNull()
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = singlePlant?.name ?: "${plants.size} Selected Plants",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = singlePlant?.let {
              listOfNotNull(
                it.category.displayName,
                it.scientificName.takeIf { s -> s.isNotBlank() },
              ).joinToString(" • ")
            } ?: "Combined planting & harvest overview",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        plants.forEach { plant ->
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = plant.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "${plant.category.displayName} • Plant: ${plant.plantingSummary()} • Harvest: ${plant.harvestSummary()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              PlantYearMonthBar(plant = plant)
              val details = buildList {
                if (plant.sunRequirement.isNotBlank()) add("Sun: ${plant.sunRequirement}")
                if (plant.daysToMaturity.isNotBlank()) add("Maturity: ${plant.daysToMaturity}")
                if (plant.spacing.isNotBlank()) add("Spacing: ${plant.spacing}")
              }
              if (details.isNotEmpty()) {
                Text(
                  text = details.joinToString(" • "),
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              if (plant.notes.isNotBlank()) {
                Text(
                  text = plant.notes,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      if (singlePlant != null) {
        Button(
          onClick = { onCustomize(singlePlant) },
          shape = RoundedCornerShape(12.dp),
        ) {
          Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Customize")
        }
      } else {
        Button(
          onClick = onDismiss,
          shape = RoundedCornerShape(12.dp),
        ) {
          Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Done")
        }
      }
    },
    dismissButton = {
      if (singlePlant != null) {
        TextButton(
          onClick = onDismiss,
          shape = RoundedCornerShape(12.dp),
        ) {
          Text("Close")
        }
      }
    },
  )
}

/**
 * Section 5.2: Destructive Delete Confirmation Dialog following the unified Dialog Design Pattern.
 */
@Composable
private fun DeletePlantsConfirmDialog(
  plants: List<PlantItem>,
  onDismiss: () -> Unit,
  onConfirmDelete: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.errorContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = if (plants.size == 1) "Remove Plant" else "Remove ${plants.size} Plants",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Remove selected crops from your yearly schedule",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = plants.joinToString(" • ") { it.name },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "You can re-add built-in or custom plants anytime from the catalog.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirmDelete,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError,
        ),
        modifier = Modifier.testTag("confirm_delete_plants_button"),
      ) {
        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Remove")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
      ) {
        Text("Cancel")
      }
    },
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MonthMultiSelectorRow(
  selectedMonths: Set<Int>,
  activeContainerColor: Color,
  activeContentColor: Color,
  tagPrefix: String,
  onToggleMonth: (Int) -> Unit,
) {
  FlowRow(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    CalendarMonth.entries.forEach { month ->
      val isSelected = month.number in selectedMonths
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) activeContainerColor else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) activeContentColor else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .clickable { onToggleMonth(month.number) }
          .testTag("${tagPrefix}_${month.number}"),
      ) {
        Text(
          text = month.shortName,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun PlantScheduleScreenPreview() {
  ToolboxTheme {
    PlantScheduleScreen()
  }
}
