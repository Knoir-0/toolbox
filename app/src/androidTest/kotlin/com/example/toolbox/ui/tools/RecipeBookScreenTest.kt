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
        val pancakes = RecipeBookEngine.BUILT_IN_RECIPES.first { it.id == "fluffy_pancakes" }
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
        val pancakes = RecipeBookEngine.BUILT_IN_RECIPES.first { it.id == "fluffy_pancakes" }
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
}
