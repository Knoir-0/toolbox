package com.example.toolbox.ui.tools

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import com.example.toolbox.ui.theme.ToolboxTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RecipeBookScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun recipeBookScreen_openSearch_displaysPreloadedCatalogAndAddsRecipe() {
        val repo = InMemoryRecipeBookRepository()
        composeTestRule.setContent {
            ToolboxTheme {
                RecipeBookScreen(repository = repo)
            }
        }

        // Open search overlay from top toolbar button
        composeTestRule.onNodeWithTag("toggle_search_bar_button").performClick()
        composeTestRule.onNodeWithTag("catalog_search_overlay").assertIsDisplayed()
        composeTestRule.onNodeWithTag("catalog_category_pills").assertIsDisplayed()

        // Search for Pancakes and star to add to book
        composeTestRule.onNodeWithTag("recipe_search_input").performTextInput("Pancakes")
        composeTestRule.onNodeWithTag("add_recipe_button_fluffy_pancakes").performClick()

        // Close search and verify recipe is in the saved recipe book and marked favorite
        composeTestRule.onNodeWithTag("close_search_button").performClick()
        composeTestRule.onNodeWithTag("saved_recipe_card_fluffy_pancakes").assertIsDisplayed()
        assertTrue(repo.getSavedRecipes().first { it.id == "fluffy_pancakes" }.isFavorite)
    }

    @Test
    fun recipeBookScreen_fabMenu_showsSelectRecipesAndEntersSelectionMode() {
        val repo = InMemoryRecipeBookRepository()
        composeTestRule.setContent {
            ToolboxTheme {
                RecipeBookScreen(repository = repo)
            }
        }

        composeTestRule.onNodeWithTag("recipe_fab_menu_button").performClick()
        composeTestRule.onNodeWithTag("select_recipes_fab_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("select_recipes_fab_button").performClick()

        composeTestRule.onNodeWithTag("multi_select_toolbar").assertIsDisplayed()
    }

    @Test
    fun recipeBookScreen_inspectRecipe_scalesServingsAndShowsSteps() {
        val pancakes = RecipeBookEngine.BUILT_IN_RECIPES.first { it.id == "fluffy_pancakes" }.copy(isFavorite = true)
        val repo = InMemoryRecipeBookRepository(initialSaved = listOf(pancakes))
        composeTestRule.setContent {
            ToolboxTheme {
                RecipeBookScreen(repository = repo)
            }
        }

        composeTestRule.onNodeWithTag("saved_recipe_card_fluffy_pancakes").performClick()
        composeTestRule.onNodeWithTag("recipe_detail_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("active_servings_text").assertIsDisplayed()

        // Increase servings from 4 to 5
        composeTestRule.onNodeWithTag("servings_increase_button").performClick()
        composeTestRule.onNodeWithText("5").assertIsDisplayed()

        composeTestRule.onNodeWithTag("close_recipe_detail_button").performClick()
    }

    @Test
    fun recipeBookScreen_longPressSavedRecipe_entersMultiSelectMode() {
        val pancakes = RecipeBookEngine.BUILT_IN_RECIPES.first { it.id == "fluffy_pancakes" }.copy(isFavorite = true)
        val repo = InMemoryRecipeBookRepository(initialSaved = listOf(pancakes))
        composeTestRule.setContent {
            ToolboxTheme {
                RecipeBookScreen(repository = repo)
            }
        }

        composeTestRule.onNodeWithTag("saved_recipe_card_fluffy_pancakes")
            .performTouchInput { longClick() }

        composeTestRule.onNodeWithTag("multi_select_toolbar").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 selected").assertIsDisplayed()
    }

    @Test
    fun duplicateRecipeFromContextMenu_opensAddRecipeModalPrefilledWithRecipe() {
        val pancakes = RecipeBookEngine.BUILT_IN_RECIPES.first { it.id == "fluffy_pancakes" }.copy(isFavorite = true)
        val repo = InMemoryRecipeBookRepository(initialSaved = listOf(pancakes))
        composeTestRule.setContent {
            ToolboxTheme {
                RecipeBookScreen(repository = repo)
            }
        }

        composeTestRule.onNodeWithTag("saved_recipe_overflow_fluffy_pancakes").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("duplicate_saved_recipe_fluffy_pancakes").performClick()
        composeTestRule.waitForIdle()

        // Opens the Add Recipe modal prefilled with the recipe
        composeTestRule.onNodeWithTag("recipe_editor_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("save_custom_recipe_button").assertIsDisplayed()

        // Since name is pre-selected, typing replaces it and confirming adds the new custom recipe
        composeTestRule.onNodeWithTag("custom_recipe_name_input").performTextInput("Blueberry Pancakes")
        composeTestRule.onNodeWithTag("save_custom_recipe_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Blueberry Pancakes").assertIsDisplayed()
        assertTrue(repo.getSavedRecipes().any { it.name == "Blueberry Pancakes" && it.isCustom && it.isFavorite })
    }

    @Test
    fun recipeBookScreen_showsMealTimeSuggestionsCardAndOnlyFavoritesInMainList() {
        val favPancakes = RecipeBookEngine.BUILT_IN_RECIPES.first { it.id == "fluffy_pancakes" }.copy(isFavorite = true)
        val nonFavSalmon = RecipeBookEngine.BUILT_IN_RECIPES.first { it.id == "pan_seared_salmon" }.copy(isFavorite = false)
        val repo = InMemoryRecipeBookRepository(initialSaved = listOf(favPancakes, nonFavSalmon))

        composeTestRule.setContent {
            ToolboxTheme {
                RecipeBookScreen(
                    repository = repo,
                    initialMealTime = MealTimeOfDay.BREAKFAST
                )
            }
        }

        // Top meal suggestions card is displayed and shows suggestions from favorites
        composeTestRule.onNodeWithTag("meal_time_suggestions_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Breakfast Suggestions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Suggested from Favorites").assertIsDisplayed()

        // Main list only contains favorites
        composeTestRule.onNodeWithTag("saved_recipe_card_fluffy_pancakes").assertIsDisplayed()
        composeTestRule.onNodeWithTag("saved_recipe_card_pan_seared_salmon").assertDoesNotExist()

        // Can cycle meal time to Lunch and Dinner
        composeTestRule.onNodeWithTag("meal_time_tab_LUNCH").performClick()
        composeTestRule.onNodeWithText("Lunch Suggestions").assertIsDisplayed()

        composeTestRule.onNodeWithTag("meal_time_tab_DINNER").performClick()
        composeTestRule.onNodeWithText("Dinner Suggestions").assertIsDisplayed()
    }
}
