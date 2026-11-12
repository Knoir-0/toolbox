package com.example.toolbox.ui.tools

import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeBookTest {

    @Test
    fun builtInRecipes_coversAllCategoriesAndHasCompleteData() {
        val catalog = RecipeBookEngine.BUILT_IN_RECIPES
        assertTrue("Expected at least 30 built-in recipes", catalog.size >= 30)

        val uniqueIds = catalog.map { it.id }.toSet()
        assertEquals("All built-in recipe IDs must be unique", catalog.size, uniqueIds.size)

        for (category in RecipeCategory.entries) {
            val count = catalog.count { it.category == category }
            assertTrue("Category $category should have built-in recipes", count >= 3)
        }

        for (recipe in catalog) {
            assertTrue("Recipe ${recipe.id} name should not be blank", recipe.name.isNotBlank())
            assertTrue("Recipe ${recipe.id} subtitle should not be blank", recipe.subtitle.isNotBlank())
            assertTrue("Recipe ${recipe.id} should have ingredients", recipe.ingredients.isNotEmpty())
            assertTrue("Recipe ${recipe.id} should have instructions", recipe.instructions.isNotEmpty())
            assertTrue("Recipe ${recipe.id} servings should be >= 1", recipe.servings >= 1)
            assertTrue("Recipe ${recipe.id} total time should be >= 0", recipe.totalTimeMinutes >= 0)
        }
    }

    @Test
    fun searchRecipes_emptyQuery_returnsPreloadedCatalogAndFiltersByCategory() {
        val catalog = RecipeBookEngine.BUILT_IN_RECIPES

        val allResults = RecipeBookEngine.searchRecipes(catalog, query = "", category = null)
        assertEquals(catalog.size, allResults.size)

        val breakfastOnly = RecipeBookEngine.searchRecipes(
            catalog = catalog,
            query = "   ",
            category = RecipeCategory.BREAKFAST
        )
        assertTrue(breakfastOnly.isNotEmpty())
        assertTrue(breakfastOnly.all { it.category == RecipeCategory.BREAKFAST })
    }

    @Test
    fun searchRecipes_matchesNameIngredientTagAndRanksNameMatchesFirst() {
        val catalog = RecipeBookEngine.BUILT_IN_RECIPES

        val pancakeResults = RecipeBookEngine.searchRecipes(catalog, query = "Pancakes")
        assertTrue(pancakeResults.isNotEmpty())
        assertEquals("fluffy_pancakes", pancakeResults.first().id)

        // Ingredient search
        val tahiniResults = RecipeBookEngine.searchRecipes(catalog, query = "tahini")
        assertTrue(tahiniResults.any { it.id == "creamy_hummus" })

        // Tag search
        val mediterraneanResults = RecipeBookEngine.searchRecipes(catalog, query = "Mediterranean")
        assertTrue(mediterraneanResults.any { it.id == "shakshuka" })
        assertTrue(mediterraneanResults.any { it.id == "greek_village_salad" })

        // Combined query + category filter
        val pastaGarlic = RecipeBookEngine.searchRecipes(
            catalog = catalog,
            query = "garlic",
            category = RecipeCategory.PASTA_AND_GRAINS
        )
        assertTrue(pastaGarlic.isNotEmpty())
        assertTrue(pastaGarlic.all { it.category == RecipeCategory.PASTA_AND_GRAINS })
    }

    @Test
    fun bookManagement_addRemoveToggleFavoriteAndUpdate() {
        val pancakes = RecipeBookEngine.BUILT_IN_RECIPES.first { it.id == "fluffy_pancakes" }
        val salmon = RecipeBookEngine.BUILT_IN_RECIPES.first { it.id == "pan_seared_salmon" }

        var saved = emptyList<RecipeItem>()
        saved = RecipeBookEngine.addRecipeToBook(saved, pancakes)
        assertEquals(1, saved.size)

        // Adding duplicate ID is a no-op
        saved = RecipeBookEngine.addRecipeToBook(saved, pancakes)
        assertEquals(1, saved.size)

        // Toggle adds when absent and removes when present
        saved = RecipeBookEngine.toggleRecipeInBook(saved, salmon)
        assertEquals(2, saved.size)
        saved = RecipeBookEngine.toggleRecipeInBook(saved, pancakes)
        assertEquals(1, saved.size)
        assertEquals("pan_seared_salmon", saved.first().id)

        // Toggle favorite
        assertFalse(saved.first().isFavorite)
        saved = RecipeBookEngine.toggleFavoriteRecipe(saved, salmon.id)
        assertTrue(saved.first().isFavorite)

        // Update recipe
        val editedSalmon = saved.first().copy(servings = 6, notes = "Extra lemon")
        saved = RecipeBookEngine.updateSavedRecipe(saved, editedSalmon)
        assertEquals(6, saved.first().servings)
        assertEquals("Extra lemon", saved.first().notes)

        // Remove recipe
        saved = RecipeBookEngine.removeRecipeFromBook(saved, salmon.id)
        assertTrue(saved.isEmpty())
    }

    @Test
    fun scaleIngredientLine_scalesWholeFractionalAndMixedQuantities() {
        assertEquals(
            "4 cups all-purpose flour",
            RecipeBookEngine.scaleIngredientLine("2 cups all-purpose flour", 4, 8)
        )
        assertEquals(
            "1 cup whole milk",
            RecipeBookEngine.scaleIngredientLine("1/2 cup whole milk", 2, 4)
        )
        assertEquals(
            "3/4 tsp kosher salt",
            RecipeBookEngine.scaleIngredientLine("1 1/2 tsp kosher salt", 4, 2)
        )
        // Non-numeric leading line remains unchanged
        assertEquals(
            "Fresh mint sprigs and ice",
            RecipeBookEngine.scaleIngredientLine("Fresh mint sprigs and ice", 4, 8)
        )
    }

    @Test
    fun formatDurationMinutes_formatsHoursAndMinutesCleanly() {
        assertEquals("0 min", RecipeBookEngine.formatDurationMinutes(0))
        assertEquals("15 min", RecipeBookEngine.formatDurationMinutes(15))
        assertEquals("1h", RecipeBookEngine.formatDurationMinutes(60))
        assertEquals("2h 10m", RecipeBookEngine.formatDurationMinutes(130))
    }

    @Test
    fun jsonSerializationAndInMemoryRepository_roundTripsRecipeItems() {
        val custom = RecipeItem(
            id = "custom_1",
            name = "Grandma's Lasagna",
            subtitle = "Family Sunday Dinner",
            category = RecipeCategory.PASTA_AND_GRAINS,
            prepTimeMinutes = 30,
            cookTimeMinutes = 50,
            servings = 8,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf("12 lasagna noodles", "2 cups ricotta", "3 cups marinara"),
            instructions = listOf("Layer noodles and sauce.", "Bake at 375F for 50 minutes."),
            tags = setOf("Italian", "Family"),
            notes = "Rest 15 minutes before slicing.",
            isFavorite = true,
            isCustom = true
        )

        val json = custom.toJson()
        val restored = RecipeItem.fromJson(json)
        assertEquals(custom, restored)

        val arr = JSONArray().apply { put(json) }
        val parsedList = SharedPreferencesRecipeBookRepository.parseRecipeListJson(arr.toString())
        assertEquals(1, parsedList.size)
        assertEquals(custom, parsedList.first())

        val repo = InMemoryRecipeBookRepository()
        repo.saveSavedRecipes(listOf(custom))
        repo.saveCustomCatalogRecipes(listOf(custom))
        assertNotNull(repo.getSavedRecipes().firstOrNull())
        assertEquals("Grandma's Lasagna", repo.getCustomCatalogRecipes().first().name)
    }
}
