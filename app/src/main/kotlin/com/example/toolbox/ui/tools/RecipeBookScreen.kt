package com.example.toolbox.ui.tools

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

fun RecipeCategory.icon(): ImageVector = when (this) {
    RecipeCategory.BREAKFAST -> Icons.Default.BreakfastDining
    RecipeCategory.MAINS -> Icons.Default.DinnerDining
    RecipeCategory.PASTA_AND_GRAINS -> Icons.Default.RamenDining
    RecipeCategory.SOUPS_AND_SALADS -> Icons.Default.SoupKitchen
    RecipeCategory.SIDES_AND_SNACKS -> Icons.Default.LunchDining
    RecipeCategory.BAKING_AND_DESSERTS -> Icons.Default.BakeryDining
    RecipeCategory.SAUCES_AND_DRINKS -> Icons.Default.LocalCafe
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecipeBookScreen(
    modifier: Modifier = Modifier,
    repository: RecipeBookRepository? = null
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val repo = remember(repository) {
        repository ?: SharedPreferencesRecipeBookRepository(context)
    }

    var savedRecipes by remember { mutableStateOf(repo.getSavedRecipes()) }
    var customCatalog by remember { mutableStateOf(repo.getCustomCatalogRecipes()) }

    val allCatalog = remember(customCatalog) {
        RecipeBookEngine.BUILT_IN_RECIPES + customCatalog
    }

    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedSearchCategoryName by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedSearchCategory = selectedSearchCategoryName?.let {
        runCatching { RecipeCategory.valueOf(it) }.getOrNull()
    }
    val searchFocusRequester = remember { FocusRequester() }

    // Main book filtering state
    var filterFavoritesOnly by rememberSaveable { mutableStateOf(false) }
    var selectedBookCategoryName by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedBookCategory = selectedBookCategoryName?.let {
        runCatching { RecipeCategory.valueOf(it) }.getOrNull()
    }

    // Multi-selection state (Section 5.5 of DESIGN_LANGUAGE.md)
    var isSelectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedIds by rememberSaveable { mutableStateOf(emptySet<String>()) }

    // Dialog states
    var isFabExpanded by remember { mutableStateOf(false) }
    var showMultiSelectOverflow by remember { mutableStateOf(false) }
    var inspectingRecipe by remember { mutableStateOf<RecipeItem?>(null) }
    var editingRecipe by remember { mutableStateOf<RecipeItem?>(null) }
    var isCreatingCustom by remember { mutableStateOf(false) }
    var initialCustomName by remember { mutableStateOf("") }
    var pendingDeleteRecipes by remember { mutableStateOf<List<RecipeItem>?>(null) }

    fun persistSaved(updated: List<RecipeItem>) {
        savedRecipes = updated
        repo.saveSavedRecipes(updated)
    }

    fun persistCustomCatalog(updated: List<RecipeItem>) {
        customCatalog = updated
        repo.saveCustomCatalogRecipes(updated)
    }

    fun exitSelectionMode() {
        isSelectionMode = false
        selectedIds = emptySet()
        showMultiSelectOverflow = false
    }

    BackHandler(enabled = isSelectionMode || isSearchActive) {
        if (isSelectionMode) {
            exitSelectionMode()
        } else if (isSearchActive) {
            isSearchActive = false
            searchQuery = ""
            selectedSearchCategoryName = null
            keyboardController?.hide()
        }
    }

    val matchingCatalogRecipes = remember(allCatalog, searchQuery, selectedSearchCategory) {
        RecipeBookEngine.searchRecipes(
            catalog = allCatalog,
            query = searchQuery,
            category = selectedSearchCategory
        )
    }

    val savedRecipeIds = remember(savedRecipes) {
        savedRecipes.map { it.id }.toSet()
    }

    val favoriteCount = remember(savedRecipes) {
        savedRecipes.count { it.isFavorite }
    }

    val filteredBookRecipes = remember(savedRecipes, filterFavoritesOnly, selectedBookCategory) {
        savedRecipes.filter { recipe ->
            (!filterFavoritesOnly || recipe.isFavorite) &&
                (selectedBookCategory == null || recipe.category == selectedBookCategory)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("recipe_book_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ==================== TOP TOOLBAR ====================
            if (isSelectionMode) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 68.dp)
                        .testTag("multi_select_toolbar"),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { exitSelectionMode() },
                            modifier = Modifier.testTag("multi_select_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Exit Selection"
                            )
                        }

                        val selectableRecipes = if (savedRecipes.isNotEmpty()) {
                            savedRecipes
                        } else {
                            allCatalog.take(6)
                        }
                        val allSelected = selectableRecipes.isNotEmpty() &&
                            selectableRecipes.all { it.id in selectedIds }
                        Checkbox(
                            checked = allSelected,
                            onCheckedChange = { checked ->
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedIds = if (checked) {
                                    selectableRecipes.map { it.id }.toSet()
                                } else {
                                    emptySet()
                                }
                            },
                            modifier = Modifier.testTag("multi_select_all_checkbox")
                        )

                        Text(
                            text = "${selectedIds.size} selected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp)
                                .testTag("multi_select_count_text")
                        )

                        IconButton(
                            onClick = {
                                val selectedRecipe = (savedRecipes + allCatalog)
                                    .firstOrNull { it.id in selectedIds }
                                if (selectedRecipe != null) {
                                    inspectingRecipe = selectedRecipe
                                }
                            },
                            enabled = selectedIds.size == 1,
                            modifier = Modifier.testTag("multi_select_info_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Inspect Selected Recipe"
                            )
                        }

                        val deletableSelected = savedRecipes.filter { it.id in selectedIds }
                        IconButton(
                            onClick = {
                                if (deletableSelected.isNotEmpty()) {
                                    pendingDeleteRecipes = deletableSelected
                                }
                            },
                            enabled = deletableSelected.isNotEmpty(),
                            modifier = Modifier.testTag("multi_select_delete_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove Selected Recipes",
                                tint = if (deletableSelected.isNotEmpty()) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                }
                            )
                        }

                        Box {
                            IconButton(
                                onClick = { showMultiSelectOverflow = true },
                                modifier = Modifier.testTag("multi_select_overflow_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More Selection Options"
                                )
                            }
                            DropdownMenu(
                                expanded = showMultiSelectOverflow,
                                onDismissRequest = { showMultiSelectOverflow = false },
                                shape = RoundedCornerShape(16.dp),
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                if (selectedIds.size == 1) {
                                    DropdownMenuItem(
                                        text = { Text("Edit Recipe") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Edit, contentDescription = null)
                                        },
                                        onClick = {
                                            showMultiSelectOverflow = false
                                            val target = (savedRecipes + allCatalog)
                                                .firstOrNull { it.id in selectedIds }
                                            if (target != null) {
                                                editingRecipe = target
                                                exitSelectionMode()
                                            }
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Toggle Favorite") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Star, contentDescription = null)
                                    },
                                    enabled = selectedIds.isNotEmpty(),
                                    onClick = {
                                        showMultiSelectOverflow = false
                                        var updated = savedRecipes
                                        selectedIds.forEach { id ->
                                            if (updated.any { it.id == id }) {
                                                updated = RecipeBookEngine.toggleFavoriteRecipe(updated, id)
                                            } else {
                                                val catalogRecipe = allCatalog.firstOrNull { it.id == id }
                                                if (catalogRecipe != null) {
                                                    updated = RecipeBookEngine.addRecipeToBook(
                                                        updated,
                                                        catalogRecipe.copy(isFavorite = true)
                                                    )
                                                }
                                            }
                                        }
                                        persistSaved(updated)
                                        exitSelectionMode()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Remove Selected") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = MaterialTheme.colorScheme.error,
                                        leadingIconColor = MaterialTheme.colorScheme.error
                                    ),
                                    enabled = deletableSelected.isNotEmpty(),
                                    onClick = {
                                        showMultiSelectOverflow = false
                                        if (deletableSelected.isNotEmpty()) {
                                            pendingDeleteRecipes = deletableSelected
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 68.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Normal Header Title & Actions
                        androidx.compose.animation.AnimatedVisibility(
                            visible = !isSearchActive,
                            enter = fadeIn(tween(150)),
                            exit = fadeOut(tween(150))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RestaurantMenu,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Recipes",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val subtitleText = if (savedRecipes.isEmpty()) {
                                            "${allCatalog.size} preloaded recipes ready to search"
                                        } else if (favoriteCount > 0) {
                                            "${savedRecipes.size} saved • $favoriteCount favorite${if (favoriteCount == 1) "" else "s"}"
                                        } else {
                                            "${savedRecipes.size} saved recipes"
                                        }
                                        Text(
                                            text = subtitleText,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            isSearchActive = true
                                        },
                                        modifier = Modifier.testTag("toggle_search_bar_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search Recipe Catalog",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Animated Expanding Search Bar inside Toolbar (Matches PlantScheduleScreen)
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isSearchActive,
                            enter = expandHorizontally(
                                expandFrom = Alignment.End,
                                animationSpec = tween(250)
                            ) + fadeIn(tween(200)),
                            exit = shrinkHorizontally(
                                shrinkTowards = Alignment.End,
                                animationSpec = tween(200)
                            ) + fadeOut(tween(150))
                        ) {
                            LaunchedEffect(Unit) {
                                delay(50)
                                runCatching { searchFocusRequester.requestFocus() }
                                keyboardController?.show()
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("toolbar_search_bar"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        isSearchActive = false
                                        searchQuery = ""
                                        selectedSearchCategoryName = null
                                        keyboardController?.hide()
                                    },
                                    modifier = Modifier.testTag("close_search_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Close Search",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text(
                                            text = "Search recipes, ingredients, tags...",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(searchFocusRequester)
                                        .testTag("recipe_search_input")
                                )

                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.testTag("recipe_search_clear")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear search",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // ==================== CONTENT AREA + SEARCH OVERLAY ====================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                // Main Recipe Book Content
                MainRecipeBookContent(
                    savedRecipes = savedRecipes,
                    filteredRecipes = filteredBookRecipes,
                    allCatalog = allCatalog,
                    savedRecipeIds = savedRecipeIds,
                    favoriteCount = favoriteCount,
                    filterFavoritesOnly = filterFavoritesOnly,
                    selectedBookCategory = selectedBookCategory,
                    isSelectionMode = isSelectionMode,
                    selectedIds = selectedIds,
                    onToggleFavoritesFilter = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        filterFavoritesOnly = !filterFavoritesOnly
                    },
                    onSelectBookCategory = { category ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedBookCategoryName =
                            if (selectedBookCategory == category) null else category?.name
                    },
                    onOpenSearchCatalog = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isSearchActive = true
                    },
                    onOpenCustomCreator = {
                        initialCustomName = ""
                        isCreatingCustom = true
                    },
                    onQuickAddStarterRecipe = { recipe ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        persistSaved(
                            RecipeBookEngine.toggleRecipeInBook(
                                savedRecipes,
                                recipe.copy(isFavorite = true)
                            )
                        )
                    },
                    onClickRecipe = { recipe ->
                        if (isSelectionMode) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedIds = if (recipe.id in selectedIds) {
                                selectedIds - recipe.id
                            } else {
                                selectedIds + recipe.id
                            }
                            if (selectedIds.isEmpty()) {
                                isSelectionMode = false
                            }
                        } else {
                            inspectingRecipe = recipe
                        }
                    },
                    onLongClickRecipe = { recipe ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (!isSelectionMode) {
                            isSelectionMode = true
                            selectedIds = setOf(recipe.id)
                        } else {
                            selectedIds = if (recipe.id in selectedIds) {
                                selectedIds - recipe.id
                            } else {
                                selectedIds + recipe.id
                            }
                        }
                    },
                    onToggleFavorite = { recipe ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        persistSaved(RecipeBookEngine.toggleFavoriteRecipe(savedRecipes, recipe.id))
                    },
                    onEditRecipe = { recipe ->
                        editingRecipe = recipe
                    },
                    onRemoveRecipe = { recipe ->
                        pendingDeleteRecipes = listOf(recipe)
                    }
                )

                // Animated Search Overlay (Mirrors PlantScheduleScreen CatalogSearchOverlay)
                androidx.compose.animation.AnimatedVisibility(
                    visible = isSearchActive,
                    enter = expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = tween(250)
                    ) + fadeIn(tween(200)),
                    exit = shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = tween(200)
                    ) + fadeOut(tween(150))
                ) {
                    RecipeCatalogSearchOverlay(
                        query = searchQuery,
                        selectedCategory = selectedSearchCategory,
                        onSelectCategory = { cat ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedSearchCategoryName =
                                if (selectedSearchCategory == cat) null else cat?.name
                        },
                        matchingRecipes = matchingCatalogRecipes,
                        allCatalogCount = allCatalog.size,
                        savedRecipeIds = savedRecipeIds,
                        onToggleRecipe = { recipe ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            persistSaved(
                                RecipeBookEngine.toggleRecipeInBook(
                                    savedRecipes,
                                    recipe.copy(isFavorite = true)
                                )
                            )
                        },
                        onInspectRecipe = { recipe ->
                            inspectingRecipe = recipe
                        },
                        onCreateCustomWithName = { prefillName ->
                            initialCustomName = prefillName
                            isCreatingCustom = true
                        }
                    )
                }
            }
        }

        // Scrim overlay when speed dial FAB is expanded
        AnimatedVisibility(
            visible = isFabExpanded && !isSelectionMode,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isFabExpanded = false
                    }
            )
        }

        // Speed dial FAB menu (hidden during multi-select, mirrors PlantScheduleScreen)
        if (!isSelectionMode) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = fadeIn(tween(160)) + slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + expandVertically(),
                    exit = fadeOut(tween(120)) + slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(140)
                    ) + shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RecipeMiniFab(
                            label = "Add Custom Recipe",
                            icon = Icons.Default.Add,
                            testTag = "add_custom_recipe_fab_button",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isFabExpanded = false
                                initialCustomName = searchQuery.trim()
                                isCreatingCustom = true
                            }
                        )
                        RecipeMiniFab(
                            label = "Select Recipes",
                            icon = Icons.Default.Check,
                            testTag = "select_recipes_fab_button",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isFabExpanded = false
                                isSearchActive = false
                                isSelectionMode = true
                                selectedIds = emptySet()
                            }
                        )
                    }
                }

                val addIconRotation by animateFloatAsState(
                    targetValue = if (isFabExpanded) 135f else 0f,
                    label = "recipe_fab_rotation"
                )

                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isFabExpanded = !isFabExpanded
                    },
                    shape = CircleShape,
                    containerColor = if (isFabExpanded) {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    contentColor = if (isFabExpanded) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    modifier = Modifier.testTag("recipe_fab_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (isFabExpanded) "Close recipe actions" else "Recipe actions",
                        modifier = Modifier.rotate(addIconRotation)
                    )
                }
            }
        }
    }

    // ==================== DIALOGS ====================
    inspectingRecipe?.let { recipe ->
        val isSaved = recipe.id in savedRecipeIds
        val currentSavedVersion = savedRecipes.firstOrNull { it.id == recipe.id } ?: recipe
        RecipeDetailDialog(
            recipe = currentSavedVersion,
            isSavedInBook = isSaved,
            onDismiss = { inspectingRecipe = null },
            onToggleSaved = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                persistSaved(
                    RecipeBookEngine.toggleRecipeInBook(
                        savedRecipes,
                        currentSavedVersion.copy(isFavorite = true)
                    )
                )
            },
            onToggleFavorite = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (isSaved) {
                    val updated = RecipeBookEngine.toggleFavoriteRecipe(savedRecipes, currentSavedVersion.id)
                    persistSaved(updated)
                    inspectingRecipe = updated.firstOrNull { it.id == currentSavedVersion.id }
                } else {
                    val addedFav = currentSavedVersion.copy(isFavorite = !currentSavedVersion.isFavorite)
                    persistSaved(RecipeBookEngine.addRecipeToBook(savedRecipes, addedFav))
                    inspectingRecipe = addedFav
                }
            },
            onEditRecipe = {
                val target = currentSavedVersion
                inspectingRecipe = null
                editingRecipe = target
            }
        )
    }

    if (isCreatingCustom) {
        RecipeEditorDialog(
            initialRecipe = null,
            prefillName = initialCustomName,
            onDismiss = { isCreatingCustom = false },
            onConfirm = { created ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                persistCustomCatalog(customCatalog + created)
                persistSaved(RecipeBookEngine.addRecipeToBook(savedRecipes, created))
                isCreatingCustom = false
            }
        )
    }

    editingRecipe?.let { target ->
        RecipeEditorDialog(
            initialRecipe = target,
            prefillName = target.name,
            onDismiss = { editingRecipe = null },
            onConfirm = { updated ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (savedRecipes.any { it.id == updated.id }) {
                    persistSaved(RecipeBookEngine.updateSavedRecipe(savedRecipes, updated))
                } else {
                    persistSaved(RecipeBookEngine.addRecipeToBook(savedRecipes, updated))
                }
                if (updated.isCustom) {
                    val updatedCustom = if (customCatalog.any { it.id == updated.id }) {
                        customCatalog.map { if (it.id == updated.id) updated else it }
                    } else {
                        customCatalog + updated
                    }
                    persistCustomCatalog(updatedCustom)
                }
                editingRecipe = null
            }
        )
    }

    pendingDeleteRecipes?.let { targets ->
        val isSingle = targets.size == 1
        AlertDialog(
            onDismissRequest = { pendingDeleteRecipes = null },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.errorContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = if (isSingle) "Remove Recipe" else "Remove ${targets.size} Recipes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                val message = if (isSingle) {
                    "Remove \"${targets.first().name}\" from your saved recipes? You can always add it back from the recipe search catalog."
                } else {
                    "Remove ${targets.size} selected recipes from your saved recipes?"
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idsToRemove = targets.map { it.id }.toSet()
                        persistSaved(savedRecipes.filterNot { it.id in idsToRemove })
                        pendingDeleteRecipes = null
                        exitSelectionMode()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("confirm_delete_recipe_button")
                ) {
                    Text("Remove", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteRecipes = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun RecipeMiniFab(
    label: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
        SmallFloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.testTag(testTag)
        ) {
            Icon(icon, contentDescription = label)
        }
    }
}

@Composable
private fun RecipeCatalogSearchOverlay(
    query: String,
    selectedCategory: RecipeCategory?,
    onSelectCategory: (RecipeCategory?) -> Unit,
    matchingRecipes: List<RecipeItem>,
    allCatalogCount: Int,
    savedRecipeIds: Set<String>,
    onToggleRecipe: (RecipeItem) -> Unit,
    onInspectRecipe: (RecipeItem) -> Unit,
    onCreateCustomWithName: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("catalog_search_overlay")
    ) {
        // Category Filter Chips Bar (Matches PlantScheduleScreen)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("catalog_category_pills"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onSelectCategory(null) },
                        label = { Text("All ($allCatalogCount)") },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("category_chip_ALL")
                    )
                }
                items(RecipeCategory.entries) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { onSelectCategory(category) },
                        label = { Text(category.displayName) },
                        leadingIcon = {
                            Icon(
                                imageVector = category.icon(),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("category_chip_${category.name}")
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("catalog_results_section"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val headerTitle = when {
                        query.isNotBlank() -> "Search Results (${matchingRecipes.size})"
                        selectedCategory != null -> "${selectedCategory.displayName} (${matchingRecipes.size})"
                        else -> "Recipe Catalog (${matchingRecipes.size})"
                    }
                    Text(
                        text = headerTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Tap ★ to save to your recipes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (matchingRecipes.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No recipes found matching \"$query\"",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Create a custom recipe with your own ingredients and steps.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FilledTonalButton(
                                onClick = { onCreateCustomWithName(query.trim()) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("create_custom_from_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create \"${query.trim()}\"")
                            }
                        }
                    }
                }
            } else {
                items(matchingRecipes, key = { "catalog_${it.id}" }) { recipe ->
                    val isAlreadyAdded = recipe.id in savedRecipeIds
                    CatalogRecipeResultCard(
                        recipe = recipe,
                        isAlreadyAdded = isAlreadyAdded,
                        onToggle = { onToggleRecipe(recipe) },
                        onInspect = { onInspectRecipe(recipe) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CatalogRecipeResultCard(
    recipe: RecipeItem,
    isAlreadyAdded: Boolean,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggle: () -> Unit,
    onInspect: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onInspect,
                onLongClick = onLongClick
            )
            .testTag("catalog_recipe_card_${recipe.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = when {
                isSelected -> MaterialTheme.colorScheme.primary
                isAlreadyAdded -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onInspect() },
                    modifier = Modifier.testTag("select_checkbox_${recipe.id}")
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = recipe.category.icon(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = recipe.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "${recipe.subtitle} • ${recipe.formattedTotalTime()} • ${recipe.servings} servings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${recipe.ingredients.size} ingredients: ${recipe.ingredients.take(3).joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!isSelectionMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggle,
                        modifier = Modifier.testTag(
                            if (isAlreadyAdded) {
                                "added_recipe_button_${recipe.id}"
                            } else {
                                "add_recipe_button_${recipe.id}"
                            }
                        )
                    ) {
                        Icon(
                            imageVector = if (isAlreadyAdded) {
                                Icons.Default.Star
                            } else {
                                Icons.Default.StarBorder
                            },
                            contentDescription = if (isAlreadyAdded) {
                                "Remove ${recipe.name} from Recipe Book"
                            } else {
                                "Add ${recipe.name} to Recipe Book"
                            },
                            tint = if (isAlreadyAdded) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("catalog_overflow_${recipe.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Recipe Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            shape = RoundedCornerShape(16.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            DropdownMenuItem(
                                text = { Text("Inspect Recipe") },
                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onInspect()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (isAlreadyAdded) "Remove from Recipes" else "Add to Recipes"
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        if (isAlreadyAdded) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onToggle()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainRecipeBookContent(
    savedRecipes: List<RecipeItem>,
    filteredRecipes: List<RecipeItem>,
    allCatalog: List<RecipeItem>,
    savedRecipeIds: Set<String>,
    favoriteCount: Int,
    filterFavoritesOnly: Boolean,
    selectedBookCategory: RecipeCategory?,
    isSelectionMode: Boolean,
    selectedIds: Set<String>,
    onToggleFavoritesFilter: () -> Unit,
    onSelectBookCategory: (RecipeCategory?) -> Unit,
    onOpenSearchCatalog: () -> Unit,
    onOpenCustomCreator: () -> Unit,
    onQuickAddStarterRecipe: (RecipeItem) -> Unit,
    onClickRecipe: (RecipeItem) -> Unit,
    onLongClickRecipe: (RecipeItem) -> Unit,
    onToggleFavorite: (RecipeItem) -> Unit,
    onEditRecipe: (RecipeItem) -> Unit,
    onRemoveRecipe: (RecipeItem) -> Unit
) {
    if (savedRecipes.isEmpty()) {
        val featuredStarters = remember(allCatalog) {
            allCatalog.take(6)
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("empty_recipe_book_content"),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 88.dp // Extra bottom padding for FAB
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Popular Recipes to Get Started",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = onOpenSearchCatalog) {
                        Text("View All (${allCatalog.size})")
                    }
                }
            }

            items(featuredStarters, key = { "starter_${it.id}" }) { recipe ->
                CatalogRecipeResultCard(
                    recipe = recipe,
                    isAlreadyAdded = recipe.id in savedRecipeIds,
                    isSelectionMode = isSelectionMode,
                    isSelected = recipe.id in selectedIds,
                    onToggle = { onQuickAddStarterRecipe(recipe) },
                    onInspect = { onClickRecipe(recipe) },
                    onLongClick = { onLongClickRecipe(recipe) }
                )
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // Book Filter Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                val presentCategories = remember(savedRecipes) {
                    RecipeCategory.entries.filter { cat ->
                        savedRecipes.any { it.category == cat }
                    }
                }
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("book_filter_row"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = !filterFavoritesOnly && selectedBookCategory == null,
                            onClick = {
                                if (filterFavoritesOnly) onToggleFavoritesFilter()
                                onSelectBookCategory(null)
                            },
                            label = { Text("All (${savedRecipes.size})") },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("book_filter_all")
                        )
                    }
                    item {
                        FilterChip(
                            selected = filterFavoritesOnly,
                            onClick = onToggleFavoritesFilter,
                            label = { Text("Favorites ($favoriteCount)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (filterFavoritesOnly) {
                                        Icons.Default.Star
                                    } else {
                                        Icons.Default.StarBorder
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("book_filter_favorites")
                        )
                    }
                    items(presentCategories) { cat ->
                        FilterChip(
                            selected = selectedBookCategory == cat,
                            onClick = { onSelectBookCategory(cat) },
                            label = { Text(cat.displayName) },
                            leadingIcon = {
                                Icon(
                                    imageVector = cat.icon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("book_filter_category_${cat.name}")
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("saved_recipes_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredRecipes.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "No saved recipes match the active filter",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                TextButton(
                                    onClick = {
                                        if (filterFavoritesOnly) onToggleFavoritesFilter()
                                        onSelectBookCategory(null)
                                    }
                                ) {
                                    Text("Show All Saved Recipes")
                                }
                            }
                        }
                    }
                } else {
                    items(filteredRecipes, key = { "saved_${it.id}" }) { recipe ->
                        val isSelected = recipe.id in selectedIds
                        SavedRecipeCard(
                            recipe = recipe,
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected,
                            onClick = { onClickRecipe(recipe) },
                            onLongClick = { onLongClickRecipe(recipe) },
                            onToggleFavorite = { onToggleFavorite(recipe) },
                            onEdit = { onEditRecipe(recipe) },
                            onRemove = { onRemoveRecipe(recipe) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun SavedRecipeCard(
    recipe: RecipeItem,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    var showOverflow by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("saved_recipe_card_${recipe.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() },
                        modifier = Modifier.testTag("select_checkbox_${recipe.id}")
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = recipe.category.icon(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = recipe.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (recipe.isCustom) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "Custom",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "${recipe.subtitle} • ${recipe.category.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!isSelectionMode) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.testTag("favorite_recipe_button_${recipe.id}")
                    ) {
                        Icon(
                            imageVector = if (recipe.isFavorite) {
                                Icons.Default.Star
                            } else {
                                Icons.Default.StarBorder
                            },
                            contentDescription = if (recipe.isFavorite) {
                                "Remove from Favorites"
                            } else {
                                "Add to Favorites"
                            },
                            tint = if (recipe.isFavorite) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showOverflow = true },
                            modifier = Modifier.testTag("saved_recipe_overflow_${recipe.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showOverflow,
                            onDismissRequest = { showOverflow = false },
                            shape = RoundedCornerShape(16.dp),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            DropdownMenuItem(
                                text = { Text("Cook") },
                                leadingIcon = {
                                    Icon(Icons.Default.Restaurant, contentDescription = null)
                                },
                                onClick = {
                                    showOverflow = false
                                    onClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                },
                                onClick = {
                                    showOverflow = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Remove") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.error,
                                    leadingIconColor = MaterialTheme.colorScheme.error
                                ),
                                onClick = {
                                    showOverflow = false
                                    onRemove()
                                },
                                modifier = Modifier.testTag("menu_remove_recipe_${recipe.id}")
                            )
                        }
                    }
                }
            }

            // Metadata Badges Row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RecipeMetricPill(
                    icon = Icons.Default.Timer,
                    label = "Total ${recipe.formattedTotalTime()}"
                )
                RecipeMetricPill(
                    icon = Icons.Default.Restaurant,
                    label = "${recipe.servings} servings"
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "${recipe.ingredients.size} ingredients • ${recipe.instructions.size} steps • ${recipe.difficulty.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecipeMetricPill(
    icon: ImageVector,
    label: String
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeDetailDialog(
    recipe: RecipeItem,
    isSavedInBook: Boolean,
    onDismiss: () -> Unit,
    onToggleSaved: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEditRecipe: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var activeServings by remember(recipe.id, recipe.servings) {
        mutableIntStateOf(recipe.servings)
    }
    val checkedIngredients = remember(recipe.id) { mutableStateListOf<Int>() }
    val completedSteps = remember(recipe.id) { mutableStateListOf<Int>() }
    val scaledIngredients = remember(recipe, activeServings) {
        recipe.scaledIngredients(activeServings)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.testTag("recipe_detail_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = recipe.category.icon(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${recipe.subtitle} • ${recipe.category.displayName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.testTag("detail_favorite_button")
                ) {
                    Icon(
                        imageVector = if (recipe.isFavorite) {
                            Icons.Default.Star
                        } else {
                            Icons.Default.StarBorder
                        },
                        contentDescription = "Toggle Favorite",
                        tint = if (recipe.isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Time & Difficulty Overview Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Prep",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                            )
                            Text(
                                text = recipe.formattedPrepTime(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Cook",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                            )
                            Text(
                                text = recipe.formattedCookTime(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                            )
                            Text(
                                text = recipe.formattedTotalTime(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Difficulty",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                            )
                            Text(
                                text = recipe.difficulty.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // Interactive Servings Scaler
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Servings Scaler",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (activeServings == recipe.servings) {
                                    "Original recipe yield"
                                } else {
                                    "Scaled from ${recipe.servings} servings"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (activeServings > 1) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        activeServings -= 1
                                    }
                                },
                                enabled = activeServings > 1,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .testTag("servings_decrease_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease Servings",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "$activeServings",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("active_servings_text")
                            )

                            IconButton(
                                onClick = {
                                    if (activeServings < 50) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        activeServings += 1
                                    }
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .testTag("servings_increase_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase Servings",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Ingredients Section (Interactive Checklist)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Ingredients (${scaledIngredients.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    scaledIngredients.forEachIndexed { index, line ->
                        val isChecked = index in checkedIngredients
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (isChecked) {
                                        checkedIngredients.remove(index)
                                    } else {
                                        checkedIngredients.add(index)
                                    }
                                }
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                .testTag("ingredient_row_$index"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodyMedium,
                                textDecoration = if (isChecked) {
                                    TextDecoration.LineThrough
                                } else {
                                    TextDecoration.None
                                },
                                color = if (isChecked) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }

                // Step-by-Step Instructions Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Instructions (${recipe.instructions.size} steps)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    recipe.instructions.forEachIndexed { idx, step ->
                        val isStepDone = idx in completedSteps
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isStepDone) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (isStepDone) {
                                        completedSteps.remove(idx)
                                    } else {
                                        completedSteps.add(idx)
                                    }
                                }
                                .testTag("instruction_step_$idx")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isStepDone) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.secondaryContainer
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isStepDone) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${idx + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textDecoration = if (isStepDone) {
                                        TextDecoration.LineThrough
                                    } else {
                                        TextDecoration.None
                                    },
                                    color = if (isStepDone) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Chef's Notes
                if (recipe.notes.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Chef's Note",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = recipe.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                // Tags
                if (recipe.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        recipe.tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onEditRecipe,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("detail_edit_recipe_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }

                Button(
                    onClick = onToggleSaved,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("detail_toggle_saved_button")
                ) {
                    Icon(
                        imageVector = if (isSavedInBook) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSavedInBook) "Saved in Recipes" else "Add to Recipes",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_recipe_detail_button")
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun RecipeEditorDialog(
    initialRecipe: RecipeItem?,
    prefillName: String,
    onDismiss: () -> Unit,
    onConfirm: (RecipeItem) -> Unit
) {
    val isEditing = initialRecipe != null
    val startName = initialRecipe?.name ?: prefillName
    var nameField by remember {
        mutableStateOf(
            TextFieldValue(
                text = startName,
                selection = TextRange(0, startName.length)
            )
        )
    }
    var subtitle by remember { mutableStateOf(initialRecipe?.subtitle ?: "Homemade Recipe") }
    var category by remember { mutableStateOf(initialRecipe?.category ?: RecipeCategory.MAINS) }
    var difficulty by remember { mutableStateOf(initialRecipe?.difficulty ?: RecipeDifficulty.EASY) }
    var prepMinutesText by remember {
        mutableStateOf((initialRecipe?.prepTimeMinutes ?: 15).toString())
    }
    var cookMinutesText by remember {
        mutableStateOf((initialRecipe?.cookTimeMinutes ?: 20).toString())
    }
    var servingsText by remember {
        mutableStateOf((initialRecipe?.servings ?: 4).toString())
    }
    var ingredientsText by remember {
        mutableStateOf(
            initialRecipe?.ingredients?.joinToString("\n")
                ?: "2 cups flour\n1 tsp kosher salt\n2 tbsp olive oil"
        )
    }
    var instructionsText by remember {
        mutableStateOf(
            initialRecipe?.instructions?.joinToString("\n")
                ?: "Prep and measure all ingredients.\nCook over medium heat until golden and tender.\nSeason to taste and serve warm."
        )
    }
    var notesText by remember {
        mutableStateOf(initialRecipe?.notes ?: "")
    }

    val nameFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(80)
        runCatching { nameFocusRequester.requestFocus() }
        keyboardController?.show()
    }

    val parsedIngredients = ingredientsText
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    val parsedInstructions = instructionsText
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    val prepVal = prepMinutesText.toIntOrNull()?.coerceIn(0, 1440) ?: 0
    val cookVal = cookMinutesText.toIntOrNull()?.coerceIn(0, 1440) ?: 0
    val servingsVal = servingsText.toIntOrNull()?.coerceIn(1, 100) ?: 4

    val isValid = nameField.text.trim().isNotEmpty() &&
        parsedIngredients.isNotEmpty() &&
        parsedInstructions.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.testTag("recipe_editor_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = if (isEditing) "Edit Recipe" else "Create Custom Recipe",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Live Context Preview Card (Section 5.2 of DESIGN_LANGUAGE.md)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = category.icon(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = nameField.text.trim().ifEmpty { "Recipe Name" },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "${category.displayName} • ${RecipeBookEngine.formatDurationMinutes(prepVal + cookVal)} • $servingsVal servings",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = nameField,
                    onValueChange = { nameField = it },
                    label = { Text("Recipe Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(nameFocusRequester)
                        .testTag("custom_recipe_name_input")
                )

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Cuisine or Subtitle") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_recipe_subtitle_input")
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(RecipeCategory.entries) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.displayName) },
                            leadingIcon = {
                                Icon(
                                    imageVector = cat.icon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = prepMinutesText,
                        onValueChange = { prepMinutesText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Prep (min)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_recipe_prep_input")
                    )
                    OutlinedTextField(
                        value = cookMinutesText,
                        onValueChange = { cookMinutesText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Cook (min)") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_recipe_cook_input")
                    )
                    OutlinedTextField(
                        value = servingsText,
                        onValueChange = { servingsText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Servings") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_recipe_servings_input")
                    )
                }

                Text(
                    text = "Difficulty",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RecipeDifficulty.entries.forEach { diff ->
                        FilterChip(
                            selected = difficulty == diff,
                            onClick = { difficulty = diff },
                            label = { Text(diff.displayName) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = ingredientsText,
                    onValueChange = { ingredientsText = it },
                    label = { Text("Ingredients (one per line)") },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_recipe_ingredients_input")
                )

                OutlinedTextField(
                    value = instructionsText,
                    onValueChange = { instructionsText = it },
                    label = { Text("Instructions (one step per line)") },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_recipe_instructions_input")
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Chef's Notes (optional)") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_recipe_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val result = RecipeItem(
                        id = initialRecipe?.id ?: "custom_recipe_${System.currentTimeMillis()}",
                        name = nameField.text.trim(),
                        subtitle = subtitle.trim().ifEmpty { "Homemade Recipe" },
                        category = category,
                        prepTimeMinutes = prepVal,
                        cookTimeMinutes = cookVal,
                        servings = servingsVal,
                        difficulty = difficulty,
                        ingredients = parsedIngredients,
                        instructions = parsedInstructions,
                        tags = initialRecipe?.tags ?: setOf("Custom"),
                        notes = notesText.trim(),
                        isFavorite = initialRecipe?.isFavorite ?: false,
                        isCustom = initialRecipe?.isCustom ?: true
                    )
                    onConfirm(result)
                },
                enabled = isValid,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_custom_recipe_button")
            ) {
                Text(if (isEditing) "Save Changes" else "Add Recipe", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
