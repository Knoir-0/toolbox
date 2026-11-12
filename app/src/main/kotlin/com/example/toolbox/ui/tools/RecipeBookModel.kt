package com.example.toolbox.ui.tools

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

enum class RecipeCategory(val displayName: String) {
    BREAKFAST("Breakfast"),
    MAINS("Mains & Dinner"),
    PASTA_AND_GRAINS("Pasta & Grains"),
    SOUPS_AND_SALADS("Soups & Salads"),
    SIDES_AND_SNACKS("Sides & Snacks"),
    BAKING_AND_DESSERTS("Baking & Desserts"),
    SAUCES_AND_DRINKS("Sauces & Drinks")
}

enum class RecipeDifficulty(val displayName: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard")
}

data class RecipeItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val category: RecipeCategory,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val servings: Int,
    val difficulty: RecipeDifficulty = RecipeDifficulty.EASY,
    val ingredients: List<String>,
    val instructions: List<String>,
    val tags: Set<String> = emptySet(),
    val notes: String = "",
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false
) {
    val totalTimeMinutes: Int
        get() = prepTimeMinutes.coerceAtLeast(0) + cookTimeMinutes.coerceAtLeast(0)

    fun formattedPrepTime(): String = RecipeBookEngine.formatDurationMinutes(prepTimeMinutes)

    fun formattedCookTime(): String = RecipeBookEngine.formatDurationMinutes(cookTimeMinutes)

    fun formattedTotalTime(): String = RecipeBookEngine.formatDurationMinutes(totalTimeMinutes)

    fun scaledIngredients(targetServings: Int): List<String> {
        val safeTarget = targetServings.coerceIn(1, 100)
        val safeBase = servings.coerceAtLeast(1)
        if (safeTarget == safeBase) return ingredients
        return ingredients.map { line ->
            RecipeBookEngine.scaleIngredientLine(line, safeBase, safeTarget)
        }
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("subtitle", subtitle)
        put("category", category.name)
        put("prepTimeMinutes", prepTimeMinutes)
        put("cookTimeMinutes", cookTimeMinutes)
        put("servings", servings)
        put("difficulty", difficulty.name)
        put("ingredients", JSONArray(ingredients))
        put("instructions", JSONArray(instructions))
        put("tags", JSONArray(tags.toList()))
        put("notes", notes)
        put("isFavorite", isFavorite)
        put("isCustom", isCustom)
    }

    companion object {
        fun fromJson(obj: JSONObject): RecipeItem {
            val categoryStr = obj.optString("category", RecipeCategory.MAINS.name)
            val category = try {
                RecipeCategory.valueOf(categoryStr)
            } catch (_: IllegalArgumentException) {
                RecipeCategory.MAINS
            }
            val difficultyStr = obj.optString("difficulty", RecipeDifficulty.EASY.name)
            val difficulty = try {
                RecipeDifficulty.valueOf(difficultyStr)
            } catch (_: IllegalArgumentException) {
                RecipeDifficulty.EASY
            }
            val ingredientsArray = obj.optJSONArray("ingredients")
            val ingredients = mutableListOf<String>()
            if (ingredientsArray != null) {
                for (i in 0 until ingredientsArray.length()) {
                    val item = ingredientsArray.optString(i).trim()
                    if (item.isNotEmpty()) ingredients.add(item)
                }
            }
            val instructionsArray = obj.optJSONArray("instructions")
            val instructions = mutableListOf<String>()
            if (instructionsArray != null) {
                for (i in 0 until instructionsArray.length()) {
                    val step = instructionsArray.optString(i).trim()
                    if (step.isNotEmpty()) instructions.add(step)
                }
            }
            val tagsArray = obj.optJSONArray("tags")
            val tags = mutableSetOf<String>()
            if (tagsArray != null) {
                for (i in 0 until tagsArray.length()) {
                    val tag = tagsArray.optString(i).trim()
                    if (tag.isNotEmpty()) tags.add(tag)
                }
            }
            return RecipeItem(
                id = obj.optString("id", "recipe_${System.currentTimeMillis()}"),
                name = obj.optString("name", "Untitled Recipe"),
                subtitle = obj.optString("subtitle", "Homemade"),
                category = category,
                prepTimeMinutes = obj.optInt("prepTimeMinutes", 10).coerceAtLeast(0),
                cookTimeMinutes = obj.optInt("cookTimeMinutes", 15).coerceAtLeast(0),
                servings = obj.optInt("servings", 4).coerceAtLeast(1),
                difficulty = difficulty,
                ingredients = ingredients,
                instructions = instructions,
                tags = tags,
                notes = obj.optString("notes", ""),
                isFavorite = obj.optBoolean("isFavorite", false),
                isCustom = obj.optBoolean("isCustom", false)
            )
        }
    }
}

interface RecipeBookRepository {
    fun getSavedRecipes(): List<RecipeItem>
    fun saveSavedRecipes(recipes: List<RecipeItem>)
    fun getCustomCatalogRecipes(): List<RecipeItem>
    fun saveCustomCatalogRecipes(recipes: List<RecipeItem>)
}

class InMemoryRecipeBookRepository(
    initialSaved: List<RecipeItem> = emptyList(),
    initialCustomCatalog: List<RecipeItem> = emptyList()
) : RecipeBookRepository {
    private var savedRecipes: List<RecipeItem> = initialSaved
    private var customCatalog: List<RecipeItem> = initialCustomCatalog

    override fun getSavedRecipes(): List<RecipeItem> = savedRecipes

    override fun saveSavedRecipes(recipes: List<RecipeItem>) {
        savedRecipes = recipes
    }

    override fun getCustomCatalogRecipes(): List<RecipeItem> = customCatalog

    override fun saveCustomCatalogRecipes(recipes: List<RecipeItem>) {
        customCatalog = recipes
    }
}

class SharedPreferencesRecipeBookRepository(context: Context) : RecipeBookRepository {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getSavedRecipes(): List<RecipeItem> {
        val raw = prefs.getString(KEY_SAVED_RECIPES, null) ?: return emptyList()
        return parseRecipeListJson(raw)
    }

    override fun saveSavedRecipes(recipes: List<RecipeItem>) {
        val arr = JSONArray()
        recipes.forEach { arr.put(it.toJson()) }
        prefs.edit().putString(KEY_SAVED_RECIPES, arr.toString()).apply()
    }

    override fun getCustomCatalogRecipes(): List<RecipeItem> {
        val raw = prefs.getString(KEY_CUSTOM_RECIPES, null) ?: return emptyList()
        return parseRecipeListJson(raw)
    }

    override fun saveCustomCatalogRecipes(recipes: List<RecipeItem>) {
        val arr = JSONArray()
        recipes.forEach { arr.put(it.toJson()) }
        prefs.edit().putString(KEY_CUSTOM_RECIPES, arr.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "recipe_book_prefs"
        private const val KEY_SAVED_RECIPES = "saved_recipes_json"
        private const val KEY_CUSTOM_RECIPES = "custom_recipes_json"

        fun parseRecipeListJson(raw: String): List<RecipeItem> {
            return try {
                val arr = JSONArray(raw)
                buildList {
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        add(RecipeItem.fromJson(obj))
                    }
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}

object RecipeBookEngine {

    val BUILT_IN_RECIPES: List<RecipeItem> = listOf(
        // ==================== BREAKFAST ====================
        RecipeItem(
            id = "fluffy_pancakes",
            name = "Fluffy Buttermilk Pancakes",
            subtitle = "Classic American Breakfast",
            category = RecipeCategory.BREAKFAST,
            prepTimeMinutes = 10,
            cookTimeMinutes = 15,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "2 cups all-purpose flour",
                "2 tbsp granulated sugar",
                "2 tsp baking powder",
                "1 tsp baking soda",
                "1/2 tsp kosher salt",
                "2 cups buttermilk",
                "2 large eggs",
                "1/4 cup unsalted butter, melted",
                "1 tsp vanilla extract"
            ),
            instructions = listOf(
                "Whisk flour, sugar, baking powder, baking soda, and salt together in a large bowl.",
                "In a separate bowl, whisk buttermilk, eggs, melted butter, and vanilla until smooth.",
                "Fold the wet ingredients into the dry ingredients gently; leave a few small lumps for extra fluffiness.",
                "Heat a lightly buttered griddle or nonstick skillet over medium-low heat.",
                "Pour 1/4 cup batter per pancake onto the griddle and cook for 2-3 minutes until bubbles form on the surface.",
                "Flip and cook for 1-2 minutes more until golden brown. Serve warm with maple syrup."
            ),
            tags = setOf("Vegetarian", "Classic", "Sweet", "Kid-Friendly"),
            notes = "Avoid overmixing the batter—lumps disappear during cooking and keep the pancakes tender."
        ),
        RecipeItem(
            id = "classic_french_toast",
            name = "Brioche French Toast",
            subtitle = "Cinnamon Vanilla Custard",
            category = RecipeCategory.BREAKFAST,
            prepTimeMinutes = 10,
            cookTimeMinutes = 10,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "8 thick slices brioche or challah bread",
                "4 large eggs",
                "1 cup whole milk or half-and-half",
                "1 tbsp brown sugar",
                "1 tsp ground cinnamon",
                "1 tsp vanilla extract",
                "2 tbsp unsalted butter"
            ),
            instructions = listOf(
                "Whisk eggs, milk, brown sugar, cinnamon, and vanilla in a shallow baking dish.",
                "Melt 1 tbsp butter in a large skillet or griddle over medium heat.",
                "Dip each bread slice into the custard for 15 seconds per side so it absorbs without getting soggy.",
                "Cook slices for 2-3 minutes per side until deeply golden brown and caramelized.",
                "Serve immediately topped with butter, powdered sugar, berries, and warm maple syrup."
            ),
            tags = setOf("Vegetarian", "Quick", "Sweet", "Brunch"),
            notes = "Day-old thick-sliced bread holds up best to the rich egg custard."
        ),
        RecipeItem(
            id = "creamy_scrambled_eggs",
            name = "Soft Scrambled Eggs",
            subtitle = "French Bistro Style",
            category = RecipeCategory.BREAKFAST,
            prepTimeMinutes = 3,
            cookTimeMinutes = 5,
            servings = 2,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "4 large eggs",
                "1 tbsp unsalted butter",
                "1 tbsp heavy cream or creme fraiche",
                "1/4 tsp kosher salt",
                "1 tbsp fresh chives, finely chopped",
                "1 pinch freshly ground black pepper"
            ),
            instructions = listOf(
                "Crack eggs into a bowl with salt and whisk thoroughly until completely uniform.",
                "Melt butter in a nonstick skillet over medium-low heat until foaming.",
                "Pour in the eggs and stir slowly and continuously with a silicone spatula, scraping the bottom and sides.",
                "Remove pan from heat when the eggs are just set but still glossy and slightly loose.",
                "Fold in heavy cream, black pepper, and chopped chives before serving on toasted sourdough."
            ),
            tags = setOf("Vegetarian", "Gluten-Free", "High-Protein", "Quick"),
            notes = "Removing the skillet from the heat a few seconds early prevents overcooking from residual heat."
        ),
        RecipeItem(
            id = "avocado_egg_toast",
            name = "Everything Avocado Toast",
            subtitle = "With Jammy Egg & Chili Flakes",
            category = RecipeCategory.BREAKFAST,
            prepTimeMinutes = 5,
            cookTimeMinutes = 7,
            servings = 2,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "2 thick slices sourdough bread",
                "1 large ripe Hass avocado",
                "2 large eggs",
                "1 tsp fresh lemon juice",
                "1 tsp everything bagel seasoning",
                "1/4 tsp red pepper flakes",
                "1/4 tsp flaky sea salt"
            ),
            instructions = listOf(
                "Bring a small pot of water to a gentle boil, lower in the eggs, and simmer for 6.5 minutes for jammy yolks.",
                "Toast the sourdough slices until golden and crisp around the edges.",
                "Halve and pit the avocado, then mash the flesh in a bowl with lemon juice and a pinch of salt.",
                "Spread the mashed avocado generously onto the warm toast.",
                "Peel and halve the soft-boiled eggs, place on top, and sprinkle with everything seasoning and chili flakes."
            ),
            tags = setOf("Vegetarian", "Quick", "Healthy", "High-Protein"),
            notes = "An ice bath right after boiling makes peeling soft-boiled eggs effortless."
        ),
        RecipeItem(
            id = "overnight_oats",
            name = "Berry Chia Overnight Oats",
            subtitle = "No-Cook Meal Prep Breakfast",
            category = RecipeCategory.BREAKFAST,
            prepTimeMinutes = 5,
            cookTimeMinutes = 0,
            servings = 2,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "1 cup rolled old-fashioned oats",
                "1 cup milk or almond milk",
                "1/2 cup Greek yogurt",
                "2 tbsp chia seeds",
                "1 tbsp honey or maple syrup",
                "1/2 tsp vanilla extract",
                "1 cup mixed fresh berries"
            ),
            instructions = listOf(
                "Combine oats, milk, Greek yogurt, chia seeds, honey, and vanilla in a jar or airtight container.",
                "Stir well so no chia seed clumps remain at the bottom.",
                "Cover and refrigerate for at least 4 hours or overnight.",
                "Stir in a splash of milk if needed to loosen the texture, then top with fresh berries before serving."
            ),
            tags = setOf("Vegetarian", "No-Cook", "Meal-Prep", "Healthy"),
            notes = "Keeps in the fridge for up to 4 days—make a batch on Sunday for weekday mornings."
        ),
        RecipeItem(
            id = "shakshuka",
            name = "Classic Shakshuka",
            subtitle = "Eggs Poached in Spiced Tomato Sauce",
            category = RecipeCategory.BREAKFAST,
            prepTimeMinutes = 10,
            cookTimeMinutes = 20,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "2 tbsp olive oil",
                "1 medium yellow onion, diced",
                "1 red bell pepper, diced",
                "3 cloves garlic, minced",
                "1 tsp ground cumin",
                "1 tsp smoked paprika",
                "1 can (28 oz) crushed tomatoes",
                "6 large eggs",
                "1/2 cup crumbled feta cheese",
                "1/4 cup fresh cilantro or parsley, chopped"
            ),
            instructions = listOf(
                "Heat olive oil in a wide skillet over medium heat. Sauté onion and red bell pepper for 6-8 minutes until soft.",
                "Stir in garlic, cumin, and smoked paprika; cook for 1 minute until fragrant.",
                "Pour in crushed tomatoes, season with salt and pepper, and simmer for 8-10 minutes until thickened.",
                "Use a spoon to make 6 small wells in the sauce and crack an egg into each well.",
                "Cover the skillet and cook on low heat for 5-7 minutes until egg whites are set and yolks remain runny.",
                "Scatter crumbled feta and fresh herbs on top and serve with warm pita or crusty bread."
            ),
            tags = setOf("Vegetarian", "One-Pan", "Mediterranean", "Brunch"),
            notes = "Keep the lid on while poaching the eggs so the tops of the whites cook evenly."
        ),
        RecipeItem(
            id = "spinach_feta_omelette",
            name = "Spinach & Feta Omelette",
            subtitle = "Diner-Style Folded Eggs",
            category = RecipeCategory.BREAKFAST,
            prepTimeMinutes = 5,
            cookTimeMinutes = 6,
            servings = 1,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "3 large eggs",
                "1 tbsp water or milk",
                "1 tbsp unsalted butter",
                "1 cup fresh baby spinach",
                "1/4 cup crumbled feta cheese",
                "1/4 tsp kosher salt",
                "1 pinch black pepper"
            ),
            instructions = listOf(
                "Whisk eggs, water, salt, and pepper until lightly frothy.",
                "Melt half the butter in an 8-inch nonstick skillet over medium heat and wilt the spinach for 1 minute; transfer to a plate.",
                "Melt remaining butter in the skillet, pour in the beaten eggs, and gently push cooked edges toward the center while tilting the pan.",
                "When the top is mostly set, scatter the wilted spinach and feta over one half of the omelette.",
                "Fold the empty half over the filling and slide onto a warm plate."
            ),
            tags = setOf("Vegetarian", "Gluten-Free", "High-Protein", "Quick"),
            notes = "Use an 8-inch nonstick pan for the ideal thickness and clean fold."
        ),
        RecipeItem(
            id = "chilaquiles_rojos",
            name = "Chilaquiles Rojos with Fried Eggs",
            subtitle = "Crisp Tortillas in Guajillo Tomato Salsa",
            category = RecipeCategory.BREAKFAST,
            prepTimeMinutes = 10,
            cookTimeMinutes = 15,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "8 cups thick-cut corn tortilla chips",
                "2 cups red enchilada or guajillo tomato salsa",
                "4 large eggs",
                "1/2 cup queso fresco or cotija cheese, crumbled",
                "1/3 cup Mexican crema or sour cream",
                "1/4 cup red onion, finely sliced",
                "1 ripe avocado, sliced",
                "1/4 cup fresh cilantro leaves"
            ),
            instructions = listOf(
                "Simmer the red salsa in a wide skillet over medium-low heat for 3-4 minutes until fragrant and slightly thickened.",
                "In a separate nonstick skillet, fry the eggs sunny-side up or over-easy in a drizzle of oil until edges are crisp.",
                "Add the corn tortilla chips directly into the simmering salsa and fold gently for 1 minute so every chip is coated while staying crisp.",
                "Divide the saucy tortillas onto warm plates and top each serving with a fried egg.",
                "Garnish generously with crumbled queso fresco, drizzled crema, sliced avocado, red onion, and fresh cilantro."
            ),
            tags = setOf("Mexican", "Hispanic", "Vegetarian", "Gluten-Free", "Brunch"),
            notes = "Toss the tortilla chips in the warm salsa right before serving so they keep a satisfying crunch underneath the sauce."
        ),

        // ==================== MAINS & DINNER ====================
        RecipeItem(
            id = "pan_seared_salmon",
            name = "Crispy Skin Lemon Garlic Salmon",
            subtitle = "20-Minute Weeknight Dinner",
            category = RecipeCategory.MAINS,
            prepTimeMinutes = 5,
            cookTimeMinutes = 10,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "4 skin-on salmon fillets (6 oz each)",
                "1 tbsp olive oil",
                "2 tbsp unsalted butter",
                "3 cloves garlic, smashed",
                "1/2 lemon, juiced",
                "1 tbsp fresh dill or parsley, chopped",
                "1 tsp kosher salt",
                "1/2 tsp freshly ground black pepper"
            ),
            instructions = listOf(
                "Pat salmon fillets thoroughly dry with paper towels and season both sides with salt and pepper.",
                "Heat olive oil in a large skillet over medium-high heat until shimmering.",
                "Place salmon skin-side down and press firmly with a spatula for 10 seconds to prevent curling.",
                "Cook undisturbed for 5-6 minutes until the skin is crisp and 70% of the fillet is opaque up the sides.",
                "Flip fillets, add butter and smashed garlic, and baste for 2 minutes until medium-rare inside.",
                "Squeeze fresh lemon juice and scatter chopped dill on top before serving."
            ),
            tags = setOf("Seafood", "Gluten-Free", "Quick", "High-Protein", "Healthy"),
            notes = "Dry skin is the secret to restaurant-quality crispy salmon skin."
        ),
        RecipeItem(
            id = "garlic_herb_roast_chicken",
            name = "Herb Butter Roast Chicken",
            subtitle = "Sunday Family Roast",
            category = RecipeCategory.MAINS,
            prepTimeMinutes = 15,
            cookTimeMinutes = 70,
            servings = 4,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "1 whole chicken (4 lb), giblets removed",
                "4 tbsp unsalted butter, softened",
                "4 cloves garlic, minced",
                "1 tbsp fresh rosemary and thyme, finely chopped",
                "1 whole lemon, halved",
                "1 tbsp kosher salt",
                "1 tsp freshly ground black pepper"
            ),
            instructions = listOf(
                "Preheat oven to 425°F (220°C). Pat chicken completely dry inside and out with paper towels.",
                "Mix softened butter, minced garlic, chopped herbs, salt, and pepper into a paste.",
                "Loosen the skin over the breast and thighs gently and rub half the herb butter underneath and the rest over the outside.",
                "Stuff the cavity with the lemon halves and truss the legs with kitchen twine.",
                "Roast in a cast-iron skillet or roasting pan for 65-75 minutes until the thickest part of the thigh registers 165°F.",
                "Rest for 15 minutes before carving and spooning the pan juices over the slices."
            ),
            tags = setOf("Poultry", "Gluten-Free", "Classic", "Dinner"),
            notes = "Resting the chicken for 15 minutes lets the juices redistribute throughout the breast meat."
        ),
        RecipeItem(
            id = "chicken_stir_fry",
            name = "Ginger Garlic Chicken Stir-Fry",
            subtitle = "Crisp Vegetables & Savory Glaze",
            category = RecipeCategory.MAINS,
            prepTimeMinutes = 15,
            cookTimeMinutes = 10,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "1.5 lb boneless skinless chicken breast, thinly sliced",
                "2 cups broccoli florets",
                "1 red bell pepper, sliced",
                "1 cup snap peas",
                "3 tbsp low-sodium soy sauce",
                "1 tbsp sesame oil",
                "1 tbsp honey",
                "1 tbsp cornstarch mixed with 1/4 cup water",
                "1 tbsp fresh ginger, grated",
                "3 cloves garlic, minced",
                "2 tbsp vegetable oil"
            ),
            instructions = listOf(
                "Whisk soy sauce, sesame oil, honey, and cornstarch slurry in a small bowl.",
                "Heat 1 tbsp vegetable oil in a wok or large skillet over high heat. Sear chicken slices for 4-5 minutes until browned; transfer to a plate.",
                "Add remaining oil along with broccoli, bell pepper, and snap peas; stir-fry for 3-4 minutes until crisp-tender.",
                "Add garlic and ginger and toss for 30 seconds until fragrant.",
                "Return chicken to the pan, pour in the sauce, and toss for 1-2 minutes until glossy and thickened."
            ),
            tags = setOf("Poultry", "Quick", "One-Pan", "High-Protein"),
            notes = "Have all ingredients chopped and measured before heating the wok—stir-frying moves fast."
        ),
        RecipeItem(
            id = "street_beef_tacos",
            name = "Spiced Ground Beef Tacos",
            subtitle = "Weeknight Taco Night",
            category = RecipeCategory.MAINS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 15,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "1 lb lean ground beef",
                "1 small yellow onion, finely diced",
                "2 cloves garlic, minced",
                "1 tbsp chili powder",
                "1.5 tsp ground cumin",
                "1 tsp smoked paprika",
                "1/2 tsp dried oregano",
                "1/2 cup tomato sauce or beef broth",
                "8 small corn or flour tortillas, warmed",
                "1 cup shredded cheddar, diced tomatoes, and cilantro for serving"
            ),
            instructions = listOf(
                "Brown ground beef and diced onion in a skillet over medium-high heat for 6-8 minutes, breaking the meat up finely.",
                "Drain excess fat, then stir in garlic, chili powder, cumin, smoked paprika, oregano, salt, and pepper.",
                "Pour in tomato sauce or broth and simmer for 5 minutes until saucy and flavorful.",
                "Warm tortillas in a dry skillet for 30 seconds per side.",
                "Fill tortillas with seasoned beef and top with cheese, tomatoes, cilantro, and a squeeze of lime."
            ),
            tags = setOf("Beef", "Quick", "Kid-Friendly", "Mexican"),
            notes = "Simmering the spices with a splash of tomato sauce coats the beef evenly so it stays juicy."
        ),
        RecipeItem(
            id = "chicken_tikka_masala",
            name = "Chicken Tikka Masala",
            subtitle = "Creamy Spiced Tomato Curry",
            category = RecipeCategory.MAINS,
            prepTimeMinutes = 20,
            cookTimeMinutes = 25,
            servings = 4,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "1.5 lb boneless chicken thighs, cut into bite-size pieces",
                "1/2 cup plain yogurt",
                "2 tbsp garam masala, divided",
                "1 tsp ground turmeric",
                "1 tsp ground cumin",
                "2 tbsp unsalted butter",
                "1 large onion, finely chopped",
                "4 cloves garlic, minced",
                "1 tbsp fresh ginger, grated",
                "1 can (15 oz) tomato puree",
                "3/4 cup heavy cream"
            ),
            instructions = listOf(
                "Toss chicken with yogurt, 1 tbsp garam masala, turmeric, and 1/2 tsp salt; marinate for at least 15 minutes.",
                "Sear chicken pieces in a hot skillet in batches until charred at the edges; set aside.",
                "Melt butter in the same skillet over medium heat and sauté onion for 7 minutes until golden.",
                "Add garlic, ginger, cumin, and remaining 1 tbsp garam masala; cook for 1 minute.",
                "Stir in tomato puree and simmer for 10 minutes until darkened and thick.",
                "Return chicken to the pan with heavy cream and simmer gently for 5 minutes. Serve over basmati rice with warm naan."
            ),
            tags = setOf("Poultry", "Curry", "Indian", "Gluten-Free"),
            notes = "Chicken thighs stay extra tender when seared at high heat and simmered in the masala sauce."
        ),
        RecipeItem(
            id = "smash_burgers",
            name = "Crispy Edge Smash Burgers",
            subtitle = "Diner-Style Double Cheeseburgers",
            category = RecipeCategory.MAINS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 8,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "1.5 lb 80/20 ground chuck beef, divided into 8 balls (3 oz each)",
                "8 slices American or cheddar cheese",
                "4 brioche burger buns, buttered and toasted",
                "1 tsp kosher salt",
                "1 tsp freshly ground black pepper",
                "1/4 cup burger sauce (mayo, ketchup, relish, vinegar)",
                "12 dill pickle chips and shredded iceberg lettuce"
            ),
            instructions = listOf(
                "Heat a cast-iron skillet or griddle over high heat until smoking hot.",
                "Place beef balls on the dry hot surface and smash flat immediately using a stiff metal spatula.",
                "Season generously with salt and pepper and cook undisturbed for 2 minutes until deep brown lace forms on the edges.",
                "Scrape underneath and flip each patty, top immediately with a slice of cheese, and cook 1 minute more.",
                "Stack two cheesy patties per toasted bun with burger sauce, pickles, and shredded lettuce."
            ),
            tags = setOf("Beef", "Quick", "American", "Classic"),
            notes = "Keep the ground beef cold right until it hits the hot cast iron for maximum crust."
        ),
        RecipeItem(
            id = "classic_beef_stew",
            name = "Old-Fashioned Beef Stew",
            subtitle = "Slow-Braised Chuck, Potatoes & Carrots",
            category = RecipeCategory.MAINS,
            prepTimeMinutes = 20,
            cookTimeMinutes = 110,
            servings = 6,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "2 lb beef chuck roast, cut into 1.5-inch cubes",
                "3 tbsp all-purpose flour",
                "2 tbsp olive oil",
                "1 large yellow onion, chopped",
                "3 cloves garlic, minced",
                "2 tbsp tomato paste",
                "4 cups beef broth",
                "1 tbsp Worcestershire sauce",
                "4 medium carrots, cut into chunks",
                "1 lb baby Yukon Gold potatoes, halved",
                "1 cup frozen peas"
            ),
            instructions = listOf(
                "Pat beef cubes dry, season with salt and pepper, and toss with flour.",
                "Sear beef in batches in hot olive oil in a Dutch oven until deeply browned on all sides; transfer to a plate.",
                "Sauté onion in the pot for 5 minutes, then stir in garlic and tomato paste for 1 minute.",
                "Pour in beef broth and Worcestershire sauce, scraping up all browned bits from the bottom.",
                "Return beef to the pot, cover, and simmer on low for 1 hour.",
                "Add carrots and potatoes, simmer covered for 40 minutes until tender, then stir in peas for the final 5 minutes."
            ),
            tags = setOf("Beef", "One-Pot", "Comfort-Food", "Dinner"),
            notes = "Browning the beef in uncrowded batches builds the deep, savory foundation of the gravy."
        ),
        RecipeItem(
            id = "sheet_pan_fajitas",
            name = "Sheet Pan Chicken Fajitas",
            subtitle = "Oven-Roasted Peppers & Onions",
            category = RecipeCategory.MAINS,
            prepTimeMinutes = 15,
            cookTimeMinutes = 20,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "1.5 lb boneless skinless chicken breast or thighs, sliced into strips",
                "3 bell peppers (mixed colors), sliced",
                "1 large red onion, sliced into wedges",
                "2 tbsp olive oil",
                "2 tsp chili powder",
                "1.5 tsp ground cumin",
                "1 tsp garlic powder",
                "1 tsp kosher salt",
                "1 lime, juiced",
                "8 warm flour tortillas"
            ),
            instructions = listOf(
                "Preheat oven to 425°F (220°C). Line a large rimmed baking sheet with foil or parchment.",
                "Spread sliced chicken, bell peppers, and onion across the baking sheet.",
                "Drizzle with olive oil and sprinkle with chili powder, cumin, garlic powder, and salt; toss to coat evenly.",
                "Roast for 18-20 minutes, broiling for the final 2 minutes for lightly charred edges.",
                "Squeeze fresh lime juice over the hot pan and serve immediately in warm tortillas."
            ),
            tags = setOf("Poultry", "One-Pan", "Mexican", "Healthy", "Quick"),
            notes = "Slicing the chicken and peppers to similar thicknesses ensures everything finishes roasting at the same time."
        ),
        RecipeItem(
            id = "homemade_sushi_rolls",
            name = "Salmon Avocado & California Sushi Rolls",
            subtitle = "Homemade Seasoned Sushi Rice & Nori Maki",
            category = RecipeCategory.MAINS,
            prepTimeMinutes = 30,
            cookTimeMinutes = 20,
            servings = 4,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "2 cups Japanese short-grain sushi rice, rinsed until water runs clear",
                "2.25 cups water",
                "1/4 cup seasoned rice vinegar (or 3 tbsp rice vinegar + 1 tbsp sugar + 1 tsp salt)",
                "6 sheets roasted nori (seaweed)",
                "8 oz sushi-grade fresh salmon or imitation crab sticks, sliced into strips",
                "1 ripe Hass avocado, thinly sliced",
                "1 English cucumber, julienned into thin matchsticks",
                "2 tbsp toasted sesame seeds",
                "Soy sauce, pickled ginger, and wasabi for serving"
            ),
            instructions = listOf(
                "Cook rinsed sushi rice with water in a rice cooker or covered pot (simmer 15 minutes on low, then steam 10 minutes off heat).",
                "Transfer hot rice to a wide bowl, gently fold in the seasoned rice vinegar with a spatula using slicing motions, and fan until room temperature.",
                "Wrap a bamboo sushi mat (makisu) in plastic wrap and place a half or full sheet of nori shiny-side down on the mat.",
                "With damp hands, spread a thin, even layer of sushi rice over the nori and sprinkle with toasted sesame seeds.",
                "Arrange strips of salmon (or crab), avocado, and cucumber horizontally across the lower third of the sheet.",
                "Lift the edge of the bamboo mat and roll forward firmly into a tight cylinder, then slice with a wet, sharp knife into 8 pieces."
            ),
            tags = setOf("Japanese", "Sushi", "Seafood", "Healthy", "Dairy-Free"),
            notes = "Wipe your knife blade with a damp towel between every cut so the sushi rice slices cleanly without sticking."
        ),
        RecipeItem(
            id = "spicy_tuna_sushi_rolls",
            name = "Spicy Tuna Crispy Cucumber Sushi Rolls",
            subtitle = "Inside-Out Uramaki with Sriracha Mayo",
            category = RecipeCategory.MAINS,
            prepTimeMinutes = 25,
            cookTimeMinutes = 20,
            servings = 4,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "2 cups cooked and seasoned sushi rice",
                "8 oz sushi-grade ahi tuna, finely diced",
                "2 tbsp Japanese mayonnaise (Kewpie)",
                "1 tbsp sriracha chili sauce",
                "1 tsp toasted sesame oil",
                "2 green onions, finely chopped",
                "4 sheets roasted nori",
                "1/2 English cucumber, julienned",
                "2 tbsp toasted white and black sesame seeds"
            ),
            instructions = listOf(
                "Gently fold diced sushi-grade tuna with Japanese mayo, sriracha, toasted sesame oil, and chopped green onions in a chilled bowl.",
                "Place a sheet of nori on a plastic-wrapped bamboo rolling mat and spread a thin layer of seasoned sushi rice evenly across the surface.",
                "Sprinkle sesame seeds over the rice, then flip the sheet over so the rice faces down against the plastic wrap (for an inside-out roll).",
                "Spoon a line of the spicy tuna mixture and cucumber matchsticks across the center of the bare nori.",
                "Roll tightly using the bamboo mat to shape and compress the roll, then slice into 8 bite-size pieces and drizzle with extra spicy mayo."
            ),
            tags = setOf("Japanese", "Sushi", "Seafood", "Spicy", "Dairy-Free"),
            notes = "Keep the diced tuna cold in the refrigerator right up until assembling the rolls."
        ),
        RecipeItem(
            id = "chicken_katsu_curry",
            name = "Japanese Chicken Katsu Curry",
            subtitle = "Crispy Panko Cutlet & Savory Golden Curry",
            category = RecipeCategory.MAINS,
            prepTimeMinutes = 20,
            cookTimeMinutes = 25,
            servings = 4,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "2 large boneless skinless chicken breasts, butterflied into 4 cutlets",
                "1/2 cup all-purpose flour",
                "2 large eggs, beaten",
                "1.5 cups Japanese panko breadcrumbs",
                "1 large yellow onion, thinly sliced",
                "2 carrots and 1 Yukon Gold potato, diced into bite-size pieces",
                "3 cups chicken broth",
                "4 oz Japanese curry roux blocks (or 2 tbsp curry powder + 2 tbsp butter + 2 tbsp flour)",
                "1 tbsp soy sauce and 1 tsp honey",
                "4 cups steamed short-grain Japanese rice",
                "1/2 cup vegetable oil for shallow frying"
            ),
            instructions = listOf(
                "Sauté sliced onion, carrots, and diced potato in 1 tbsp oil in a pot for 5 minutes until softened, then pour in chicken broth and simmer covered for 12 minutes until tender.",
                "Turn heat to low, stir in the Japanese curry roux blocks, soy sauce, and honey until melted into a thick, glossy gravy.",
                "Season chicken cutlets with salt and pepper, dredge in flour, dip in beaten egg, and press firmly into panko breadcrumbs.",
                "Shallow-fry the breaded cutlets in 350°F vegetable oil for 3-4 minutes per side until deep golden brown and crisp; drain on a wire rack.",
                "Slice the crispy chicken katsu into strips, arrange over warm steamed rice, and ladle the hot Japanese curry alongside."
            ),
            tags = setOf("Japanese", "Poultry", "Curry", "Comfort-Food"),
            notes = "Mist the panko breadcrumbs lightly with water before frying for an even lighter, flakier crunch."
        ),
        RecipeItem(
            id = "chicken_teriyaki",
            name = "Glazed Skillet Chicken Teriyaki",
            subtitle = "Authentic Soy, Mirin & Ginger Tare Glaze",
            category = RecipeCategory.MAINS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 15,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "1.5 lb boneless skinless chicken thighs",
                "1/3 cup soy sauce",
                "1/3 cup mirin (Japanese sweet rice wine)",
                "2 tbsp sake or water",
                "2 tbsp granulated sugar",
                "1 tsp fresh ginger, grated",
                "1 tbsp vegetable oil",
                "2 green onions, thinly sliced, and 1 tsp toasted sesame seeds"
            ),
            instructions = listOf(
                "Whisk soy sauce, mirin, sake, sugar, and grated ginger together in a small bowl until sugar dissolves.",
                "Pat chicken thighs dry and sear smooth-side down in a lightly oiled skillet over medium-high heat for 6 minutes until golden.",
                "Flip the chicken thighs and cook for 3 minutes more, then blot any excess oil from the pan with a paper towel.",
                "Pour in the teriyaki sauce mixture and simmer briskly for 3-4 minutes, spooning the bubbling glaze over the chicken until thick and lacquered.",
                "Rest for 3 minutes, slice into strips, spoon pan glaze on top, and garnish with sesame seeds and scallions."
            ),
            tags = setOf("Japanese", "Poultry", "Quick", "One-Pan", "Dairy-Free"),
            notes = "Wiping excess fat from the skillet before pouring in the sauce helps the glaze cling evenly to the chicken."
        ),
        RecipeItem(
            id = "chicken_enchiladas_verdes",
            name = "Chicken Enchiladas Verdes",
            subtitle = "Baked Tortillas in Tangy Tomatillo Green Salsa",
            category = RecipeCategory.MAINS,
            prepTimeMinutes = 20,
            cookTimeMinutes = 25,
            servings = 6,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "3 cups cooked shredded chicken",
                "3 cups salsa verde (roasted tomatillo green salsa)",
                "1/2 cup sour cream or Mexican crema",
                "2.5 cups Monterey Jack or Oaxaca cheese, shredded, divided",
                "12 corn tortillas, warmed",
                "1/2 white onion, finely diced",
                "1/3 cup fresh cilantro, chopped"
            ),
            instructions = listOf(
                "Preheat oven to 375°F (190°C). Whisk salsa verde and sour cream together in a bowl until creamy.",
                "Toss shredded chicken with 3/4 cup of the creamy salsa verde and 1 cup of shredded cheese.",
                "Spread 1/2 cup of the sauce across the bottom of a 9x13-inch baking dish.",
                "Warm corn tortillas briefly so they roll without cracking, fill each with the chicken mixture, and arrange seam-side down in the dish.",
                "Pour remaining sauce over the tortillas, scatter remaining cheese on top, and bake for 20-25 minutes until bubbly and lightly browned.",
                "Top with diced white onion and fresh cilantro before serving."
            ),
            tags = setOf("Mexican", "Hispanic", "Poultry", "Gluten-Free", "Comfort-Food"),
            notes = "Warming the corn tortillas in a dry skillet for 15 seconds per side keeps them pliable and prevents tearing."
        ),
        RecipeItem(
            id = "pork_carnitas",
            name = "Crispy Slow-Braised Pork Carnitas",
            subtitle = "Citrus, Garlic & Oregano Mexican Pulled Pork",
            category = RecipeCategory.MAINS,
            prepTimeMinutes = 15,
            cookTimeMinutes = 150,
            servings = 6,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "3 lb boneless pork shoulder (pork butt), cut into 2-inch chunks",
                "1 large orange, juiced (plus spent halves)",
                "2 limes, juiced",
                "1 white onion, quartered",
                "6 cloves garlic, smashed",
                "2 tsp ground cumin",
                "2 tsp dried Mexican oregano",
                "1.5 tsp kosher salt and 1 tsp black pepper",
                "1/2 cup water or chicken broth"
            ),
            instructions = listOf(
                "Season pork chunks generously with salt, pepper, cumin, and oregano and place in a heavy Dutch oven.",
                "Tuck in the quartered onion, smashed garlic, orange juice, spent orange halves, lime juice, and water.",
                "Bring to a simmer, cover tightly, and braise on low heat (or in a 300°F oven) for 2 hours until fork-tender.",
                "Uncover and simmer over medium-high heat for 15 minutes until the braising liquid reduces into a rich glaze.",
                "Shred the pork into large chunks, spread onto a baking sheet, ladle reduced juices over top, and broil for 4-5 minutes until the edges are crisp and caramelized."
            ),
            tags = setOf("Mexican", "Hispanic", "Pork", "Gluten-Free", "Dairy-Free"),
            notes = "Crisping the shredded pork under the broiler right before serving gives carnitas their signature contrast of tender meat and crunchy edges."
        ),
        RecipeItem(
            id = "arroz_con_pollo",
            name = "Classic Arroz con Pollo",
            subtitle = "One-Pot Latin Saffron & Sofrito Chicken Rice",
            category = RecipeCategory.MAINS,
            prepTimeMinutes = 15,
            cookTimeMinutes = 35,
            servings = 6,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "6 bone-in or boneless chicken thighs",
                "1.5 cups long-grain white rice",
                "2 tbsp olive oil",
                "1 yellow onion and 1 red bell pepper, finely diced",
                "4 cloves garlic, minced",
                "1/2 cup tomato sauce",
                "2.5 cups warm chicken broth",
                "1 tsp ground cumin, 1 tsp smoked paprika, and 1 pinch saffron or sazon",
                "1 cup frozen peas and 1/3 cup sliced pimiento-stuffed green olives",
                "1/4 cup fresh cilantro, chopped"
            ),
            instructions = listOf(
                "Season chicken thighs with salt, pepper, cumin, and paprika. Sear in hot olive oil in a wide Dutch oven for 5 minutes per side until golden; transfer to a plate.",
                "Sauté diced onion and bell pepper in the pot for 5 minutes until softened, then stir in garlic and tomato sauce for 1 minute.",
                "Add the rice and toast for 2 minutes, stirring to coat the grains in the savory sofrito oil.",
                "Pour in warm chicken broth and saffron, nestle the chicken thighs back on top, cover tightly, and simmer on low for 20 minutes.",
                "Scatter peas and green olives over the rice, cover off the heat for 5 minutes, and garnish with fresh cilantro."
            ),
            tags = setOf("Hispanic", "Poultry", "Rice", "One-Pot", "Gluten-Free"),
            notes = "Toasting the dry rice in the sofrito before adding broth keeps the grains separate and deeply flavorful."
        ),
        RecipeItem(
            id = "chicken_parmigiana",
            name = "Classic Italian Chicken Parmigiana",
            subtitle = "Crispy Breaded Cutlets with Marinara & Mozzarella",
            category = RecipeCategory.MAINS,
            prepTimeMinutes = 20,
            cookTimeMinutes = 20,
            servings = 4,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "2 large chicken breasts, halved horizontally into 4 thin cutlets",
                "1/2 cup all-purpose flour",
                "2 large eggs, beaten",
                "1 cup Italian seasoned breadcrumbs mixed with 1/2 cup grated Parmesan",
                "2 cups marinara sauce",
                "8 oz fresh mozzarella cheese, sliced or shredded",
                "1/3 cup olive oil for pan-frying",
                "1/4 cup fresh basil leaves, torn"
            ),
            instructions = listOf(
                "Preheat oven or broiler to 425°F (220°C). Pound chicken cutlets to an even 1/3-inch thickness and season with salt and pepper.",
                "Dredge each cutlet in flour, dip in beaten egg, and press into the Parmesan-breadcrumb mixture.",
                "Pan-fry cutlets in shimmering olive oil in a large skillet over medium-high heat for 3 minutes per side until golden and crisp.",
                "Arrange crispy cutlets in a baking dish, spoon marinara sauce over each, and top with mozzarella and extra Parmesan.",
                "Bake for 8-10 minutes until the cheese is melted, bubbling, and lightly browned, then scatter with fresh basil."
            ),
            tags = setOf("Italian", "Poultry", "Classic", "Comfort-Food"),
            notes = "Spoon marinara over the center of each cutlet leaving the edges uncovered so the breadcrumb crust stays crisp."
        ),
        RecipeItem(
            id = "layered_meat_lasagna",
            name = "Traditional Bolognese & Herbed Ricotta Lasagna",
            subtitle = "Baked Italian Layered Pasta",
            category = RecipeCategory.MAINS,
            prepTimeMinutes = 30,
            cookTimeMinutes = 50,
            servings = 8,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "12 lasagna noodles, boiled until al dente",
                "4 cups rich meat marinara or Bolognese sauce",
                "15 oz whole-milk ricotta cheese",
                "1 large egg, lightly beaten",
                "3 cups low-moisture mozzarella cheese, shredded",
                "1 cup freshly grated Parmesan cheese",
                "1/4 cup fresh parsley and basil, finely chopped"
            ),
            instructions = listOf(
                "Preheat oven to 375°F (190°C). Stir ricotta, beaten egg, half the Parmesan, chopped herbs, salt, and black pepper together in a bowl.",
                "Spread 1 cup of meat sauce evenly across the bottom of a deep 9x13-inch baking dish.",
                "Arrange 4 lasagna noodles over the sauce, spread with one-third of the ricotta mixture, 1 cup meat sauce, and 1 cup shredded mozzarella.",
                "Repeat the layers twice more, finishing the top layer with remaining sauce, mozzarella, and Parmesan.",
                "Bake covered with foil for 30 minutes, then uncover and bake 20 minutes more until golden and bubbling. Rest 15 minutes before slicing."
            ),
            tags = setOf("Italian", "Pasta", "Beef", "Comfort-Food", "Make-Ahead"),
            notes = "Letting the baked lasagna rest for 15 minutes before cutting ensures clean, structured slices."
        ),
        RecipeItem(
            id = "chicken_souvlaki_tzatziki",
            name = "Greek Chicken Souvlaki with Tzatziki",
            subtitle = "Lemon Oregano Skewers & Cool Cucumber Yogurt",
            category = RecipeCategory.MAINS,
            prepTimeMinutes = 20,
            cookTimeMinutes = 12,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "1.5 lb boneless skinless chicken breast or thighs, cubed",
                "3 tbsp extra-virgin olive oil",
                "1 large lemon, juiced",
                "4 cloves garlic, minced, divided",
                "1 tbsp dried Greek oregano",
                "1 cup thick Greek yogurt",
                "1/2 English cucumber, grated and squeezed dry",
                "2 tbsp fresh dill, chopped",
                "4 warm pita breads, sliced tomatoes, and red onion for serving"
            ),
            instructions = listOf(
                "Whisk olive oil, lemon juice, 3 cloves minced garlic, oregano, 1 tsp salt, and black pepper; toss with cubed chicken and marinate for 15-30 minutes.",
                "Meanwhile, stir Greek yogurt, squeezed grated cucumber, remaining 1 clove garlic, fresh dill, and a pinch of salt to make the tzatziki sauce.",
                "Thread marinated chicken onto skewers and sear on a hot grill pan or cast-iron skillet for 10-12 minutes, turning until charred and cooked through.",
                "Serve the hot skewers over warm pita bread with generous spoonfuls of cool tzatziki, tomatoes, and red onion."
            ),
            tags = setOf("Greek", "Mediterranean", "Poultry", "High-Protein", "Healthy"),
            notes = "Squeezing excess water from the grated cucumber in a clean kitchen towel keeps the tzatziki thick and creamy."
        ),
        RecipeItem(
            id = "traditional_greek_moussaka",
            name = "Traditional Greek Moussaka",
            subtitle = "Roasted Eggplant, Cinnamon Spiced Meat & Béchamel",
            category = RecipeCategory.MAINS,
            prepTimeMinutes = 35,
            cookTimeMinutes = 45,
            servings = 6,
            difficulty = RecipeDifficulty.HARD,
            ingredients = listOf(
                "2 medium eggplants, sliced into 1/3-inch rounds",
                "1 lb ground lamb or beef",
                "1 medium yellow onion, finely diced, and 3 cloves garlic, minced",
                "1 can (15 oz) crushed tomatoes",
                "1/2 tsp ground cinnamon and 1 tsp dried oregano",
                "4 tbsp unsalted butter and 1/4 cup all-purpose flour",
                "2 cups warm whole milk",
                "1/2 cup grated Parmesan or Kefalotyri cheese",
                "1 egg yolk and 1 pinch freshly grated nutmeg"
            ),
            instructions = listOf(
                "Preheat oven to 400°F (200°C). Brush eggplant slices with olive oil, season with salt, and roast on baking sheets for 20 minutes until tender.",
                "Brown ground meat and onion in a skillet for 8 minutes, then stir in garlic, crushed tomatoes, cinnamon, oregano, salt, and pepper; simmer 15 minutes.",
                "For the béchamel, melt butter in a saucepan, whisk in flour for 1 minute, gradually whisk in warm milk until thick, then stir in cheese, nutmeg, and egg yolk off the heat.",
                "Layer half the roasted eggplant in a 9x13-inch baking dish, spread the spiced meat sauce on top, add remaining eggplant, and pour the creamy béchamel over the top.",
                "Bake for 35-40 minutes until the béchamel crust is puffed and deep golden brown. Rest 20 minutes before serving."
            ),
            tags = setOf("Greek", "Mediterranean", "Beef", "Comfort-Food", "Dinner"),
            notes = "A warm touch of cinnamon in the tomato meat sauce gives Greek moussaka its unmistakable aromatic depth."
        ),

        // ==================== PASTA & GRAINS ====================
        RecipeItem(
            id = "spaghetti_bolognese",
            name = "Classic Spaghetti Bolognese",
            subtitle = "Rich Italian Meat Ragù",
            category = RecipeCategory.PASTA_AND_GRAINS,
            prepTimeMinutes = 15,
            cookTimeMinutes = 45,
            servings = 6,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "1 lb spaghetti or tagliatelle",
                "1 lb ground beef (or half beef, half pork)",
                "2 tbsp olive oil",
                "1 medium onion, finely diced",
                "1 carrot, finely diced",
                "1 celery stalk, finely diced",
                "3 cloves garlic, minced",
                "2 tbsp tomato paste",
                "1 can (28 oz) crushed San Marzano tomatoes",
                "1/2 cup whole milk",
                "1/2 cup freshly grated Parmesan cheese"
            ),
            instructions = listOf(
                "Heat olive oil in a Dutch oven over medium heat. Cook onion, carrot, and celery (soffritto) for 8 minutes until soft.",
                "Add ground meat and cook for 8 minutes, crumbling finely until browned.",
                "Stir in garlic and tomato paste and cook for 2 minutes until caramelized.",
                "Pour in milk and simmer for 3 minutes until absorbed, then add crushed tomatoes, salt, and pepper.",
                "Simmer gently on low heat for 30 minutes, stirring occasionally until thick and rich.",
                "Toss al dente pasta directly in the sauce with a splash of pasta water and top with grated Parmesan."
            ),
            tags = setOf("Pasta", "Italian", "Beef", "Comfort-Food"),
            notes = "Adding milk before the tomatoes tenderizes the meat and balances the acidity of the tomatoes."
        ),
        RecipeItem(
            id = "spaghetti_carbonara",
            name = "Roman Spaghetti Carbonara",
            subtitle = "Pancetta, Pecorino & Silky Egg Sauce",
            category = RecipeCategory.PASTA_AND_GRAINS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 15,
            servings = 4,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "12 oz spaghetti or rigatoni",
                "6 oz guanciale or pancetta, diced",
                "3 large whole eggs plus 2 egg yolks",
                "1 cup finely grated Pecorino Romano or Parmesan",
                "1 tsp freshly cracked black pepper"
            ),
            instructions = listOf(
                "Bring a pot of salted water to a boil and cook pasta until al dente, reserving 1 cup of starchy pasta water.",
                "While pasta cooks, crisp the guanciale or pancetta in a large skillet over medium heat for 6-8 minutes; turn off heat.",
                "Whisk whole eggs, egg yolks, grated cheese, and cracked black pepper into a thick paste in a bowl.",
                "Transfer hot drained pasta into the warm (off-heat) skillet and toss with the rendered fat.",
                "Pour in the egg-cheese mixture along with 1/3 cup warm pasta water, tossing vigorously for 1 minute until a glossy emulsion coats every strand."
            ),
            tags = setOf("Pasta", "Italian", "Quick", "Classic"),
            notes = "Always take the pan off the heat before adding the egg mixture so the eggs emulsify into silk instead of scrambling."
        ),
        RecipeItem(
            id = "fettuccine_alfredo",
            name = "Creamy Fettuccine Alfredo",
            subtitle = "Parmesan, Garlic & Butter Sauce",
            category = RecipeCategory.PASTA_AND_GRAINS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 15,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "12 oz fettuccine pasta",
                "6 tbsp unsalted butter",
                "2 cloves garlic, minced",
                "1.5 cups heavy cream",
                "1.5 cups freshly grated Parmesan cheese",
                "1/4 tsp freshly grated nutmeg",
                "2 tbsp fresh parsley, chopped"
            ),
            instructions = listOf(
                "Cook fettuccine in salted boiling water until al dente; reserve 1/2 cup pasta water before draining.",
                "Melt butter in a large skillet over medium-low heat and sauté garlic for 1 minute without browning.",
                "Pour in heavy cream and simmer gently for 3-4 minutes until slightly reduced.",
                "Whisk in freshly grated Parmesan a handful at a time along with nutmeg, salt, and pepper until smooth.",
                "Toss cooked fettuccine in the sauce, adding a splash of pasta water as needed, and garnish with parsley."
            ),
            tags = setOf("Pasta", "Vegetarian", "Italian", "Comfort-Food", "Quick"),
            notes = "Grate Parmesan fresh from a block—pre-shredded cheese contains anti-caking starch that prevents smooth melting."
        ),
        RecipeItem(
            id = "baked_mac_and_cheese",
            name = "Baked Three-Cheese Macaroni",
            subtitle = "Sharp Cheddar, Gruyère & Crispy Crumb Topping",
            category = RecipeCategory.PASTA_AND_GRAINS,
            prepTimeMinutes = 15,
            cookTimeMinutes = 25,
            servings = 6,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "1 lb elbow macaroni or cavatappi",
                "4 tbsp unsalted butter",
                "1/4 cup all-purpose flour",
                "3 cups whole milk, warmed",
                "2.5 cups sharp cheddar cheese, shredded",
                "1 cup Gruyère or Monterey Jack cheese, shredded",
                "1/2 tsp Dijon mustard",
                "1/2 tsp smoked paprika",
                "3/4 cup panko breadcrumbs tossed with 1 tbsp melted butter"
            ),
            instructions = listOf(
                "Preheat oven to 375°F (190°C). Boil pasta 2 minutes shy of al dente and drain.",
                "Melt butter in a large saucepan over medium heat, whisk in flour, and cook for 1 minute.",
                "Gradually whisk in warm milk and simmer for 4-5 minutes until thickened into a smooth béchamel.",
                "Remove from heat and stir in mustard, paprika, salt, pepper, and three-quarters of the shredded cheeses until melted.",
                "Fold in pasta, transfer to a greased baking dish, top with remaining cheese and buttered panko, and bake for 20 minutes until bubbling and golden."
            ),
            tags = setOf("Pasta", "Vegetarian", "Comfort-Food", "Kid-Friendly"),
            notes = "Undercooking the pasta slightly ensures it stays firm after baking in the cheese sauce."
        ),
        RecipeItem(
            id = "vegetable_fried_rice",
            name = "Takeout-Style Egg Fried Rice",
            subtitle = "Crisp Veggies, Sesame & Scallions",
            category = RecipeCategory.PASTA_AND_GRAINS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 10,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "4 cups cold day-old cooked jasmine rice",
                "3 large eggs, lightly beaten",
                "1 cup frozen peas and diced carrots, thawed",
                "3 cloves garlic, minced",
                "4 green onions, thinly sliced",
                "3 tbsp soy sauce",
                "1 tbsp toasted sesame oil",
                "2 tbsp vegetable oil"
            ),
            instructions = listOf(
                "Heat 1 tbsp vegetable oil in a large wok or skillet over high heat. Scramble the eggs quickly and transfer to a plate.",
                "Heat remaining oil, add garlic, peas, and carrots, and stir-fry for 2 minutes.",
                "Add cold rice, breaking up any clumps, and toss over high heat for 3-4 minutes until rice grains toast slightly.",
                "Drizzle soy sauce and sesame oil around the perimeter of the wok and toss thoroughly.",
                "Fold in scrambled eggs and sliced green onions and serve hot."
            ),
            tags = setOf("Rice", "Vegetarian", "Quick", "One-Pan"),
            notes = "Cold day-old rice has less surface moisture so every grain fries cleanly without clumping."
        ),
        RecipeItem(
            id = "chicken_pad_thai",
            name = "Street-Style Chicken Pad Thai",
            subtitle = "Rice Noodles in Tangy Tamarind Sauce",
            category = RecipeCategory.PASTA_AND_GRAINS,
            prepTimeMinutes = 20,
            cookTimeMinutes = 10,
            servings = 4,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "8 oz flat rice noodles",
                "12 oz chicken breast or shrimp, sliced",
                "3 tbsp tamarind paste (or rice vinegar + lime juice)",
                "3 tbsp fish sauce (or soy sauce)",
                "2 tbsp brown sugar",
                "2 large eggs",
                "2 cups bean sprouts",
                "3 green onions, cut into 1-inch pieces",
                "1/3 cup roasted peanuts, chopped",
                "1 lime, cut into wedges"
            ),
            instructions = listOf(
                "Soak rice noodles in warm water for 25 minutes until pliable but still firm; drain well.",
                "Whisk tamarind paste, fish sauce, and brown sugar together in a small bowl.",
                "Sear sliced chicken in 2 tbsp oil in a hot wok for 4 minutes until cooked.",
                "Push chicken to one side, crack eggs into the empty side, scramble briefly, then add drained noodles and sauce.",
                "Toss over high heat for 2-3 minutes until noodles absorb the sauce, then fold in bean sprouts, green onions, and peanuts."
            ),
            tags = setOf("Noodles", "Thai", "Quick", "Dairy-Free"),
            notes = "Soak rice noodles in warm water rather than boiling them so they don't overcook in the wok."
        ),
        RecipeItem(
            id = "mushroom_risotto",
            name = "Creamy Garlic Mushroom Risotto",
            subtitle = "Arborio Rice, Parmesan & Thyme",
            category = RecipeCategory.PASTA_AND_GRAINS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 30,
            servings = 4,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "1.5 cups Arborio rice",
                "12 oz cremini or wild mushrooms, sliced",
                "5 cups warm vegetable or chicken stock",
                "1 small shallot or onion, finely diced",
                "3 cloves garlic, minced",
                "1/2 cup dry white wine (or extra stock)",
                "3 tbsp unsalted butter",
                "3/4 cup freshly grated Parmesan cheese",
                "1 tbsp fresh thyme leaves"
            ),
            instructions = listOf(
                "Sauté sliced mushrooms with 1 tbsp butter and thyme in a wide pan until golden brown; remove and set aside.",
                "In the same pan, melt 1 tbsp butter and cook diced shallot and garlic for 3 minutes until translucent.",
                "Add Arborio rice and stir for 2 minutes until the edges of the grains look translucent.",
                "Pour in white wine and stir until absorbed, then add warm stock one ladle at a time, stirring until each addition is mostly absorbed (about 20 minutes).",
                "Fold in sautéed mushrooms, remaining 1 tbsp butter, and grated Parmesan off the heat until velvety."
            ),
            tags = setOf("Rice", "Vegetarian", "Gluten-Free", "Italian"),
            notes = "Keep your pot of stock warm on a back burner so each ladle doesn't interrupt the simmer of the rice."
        ),
        RecipeItem(
            id = "penne_alla_vodka",
            name = "Creamy Penne alla Vodka",
            subtitle = "Tomato, Garlic & Calabrian Chili Cream Sauce",
            category = RecipeCategory.PASTA_AND_GRAINS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 20,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "1 lb penne rigate or rigatoni",
                "2 tbsp extra-virgin olive oil and 2 tbsp unsalted butter",
                "1 small shallot, finely minced, and 4 cloves garlic, minced",
                "1/2 cup double-concentrated tomato paste",
                "1/4 cup vodka (or chicken/vegetable broth with a squeeze of lemon)",
                "3/4 cup heavy cream",
                "1/2 tsp crushed red pepper flakes",
                "3/4 cup freshly grated Parmesan cheese",
                "1/4 cup fresh basil leaves, torn"
            ),
            instructions = listOf(
                "Heat olive oil and 1 tbsp butter in a wide skillet over medium heat. Cook minced shallot and garlic for 3 minutes until soft.",
                "Add tomato paste and crushed red pepper flakes; caramelize for 4-5 minutes, stirring constantly until darkened to a deep brick red.",
                "Deglaze with vodka and simmer for 2 minutes until mostly reduced, then reduce heat to low and whisk in heavy cream until smooth.",
                "Meanwhile, boil penne in generously salted water until al dente, reserving 1 cup of starchy pasta water.",
                "Transfer hot pasta directly into the sauce with remaining 1 tbsp butter, grated Parmesan, and a splash of pasta water, tossing until glossy."
            ),
            tags = setOf("Pasta", "Italian", "Vegetarian", "Comfort-Food", "Quick"),
            notes = "Caramelizing the tomato paste thoroughly before adding the cream gives penne alla vodka its signature rich, savory-sweet depth."
        ),
        RecipeItem(
            id = "cacio_e_pepe",
            name = "Roman Tonnarelli Cacio e Pepe",
            subtitle = "Pecorino Romano & Toasted Black Pepper",
            category = RecipeCategory.PASTA_AND_GRAINS,
            prepTimeMinutes = 5,
            cookTimeMinutes = 15,
            servings = 4,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "12 oz spaghetti, tonnarelli, or bucatini",
                "2 tsp whole black peppercorns, coarsely crushed",
                "1.5 cups finely grated aged Pecorino Romano cheese",
                "1/2 cup finely grated Parmigiano-Reggiano",
                "1 tbsp unsalted butter (optional for extra gloss)"
            ),
            instructions = listOf(
                "Boil pasta in a pot with half the usual amount of salted water (so the water becomes extra starchy) until 2 minutes shy of al dente.",
                "Toast crushed black peppercorns in a wide dry skillet over medium-low heat for 1 minute until aromatic, then ladle in 1/2 cup starchy pasta water and simmer.",
                "Whisk the finely grated Pecorino and Parmigiano with 1/4 cup warm (not boiling) pasta water in a bowl into a smooth, thick paste.",
                "Transfer pasta into the pepper skillet and toss for 1 minute until al dente, then remove the pan from the heat for 30 seconds.",
                "Add the cheese paste and toss vigorously, splashing in extra pasta water as needed, until a creamy emulsion coats the noodles."
            ),
            tags = setOf("Pasta", "Italian", "Vegetarian", "Classic", "Quick"),
            notes = "Grating the Pecorino on the finest microplane side prevents clumping when emulsified with warm pasta water."
        ),
        RecipeItem(
            id = "tonkotsu_shoyu_ramen",
            name = "Japanese Chashu & Ajitama Shoyu Ramen",
            subtitle = "Savory Garlic Soy Broth, Noodles & Jammy Egg",
            category = RecipeCategory.PASTA_AND_GRAINS,
            prepTimeMinutes = 15,
            cookTimeMinutes = 20,
            servings = 4,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "4 packs fresh or dried Japanese ramen noodles",
                "6 cups rich pork or chicken bone broth",
                "2 cups dashi stock (or water steep with kombu and dried shiitake)",
                "1/3 cup soy sauce and 2 tbsp mirin",
                "1 tbsp toasted sesame oil",
                "4 cloves garlic, grated, and 1 tbsp fresh ginger, grated",
                "8 slices braised pork belly chashu or seared pork tenderloin",
                "4 soft-boiled marinated eggs (ajitama), halved",
                "4 green onions, thinly sliced, plus nori sheets and bamboo shoots"
            ),
            instructions = listOf(
                "Warm sesame oil in a soup pot over medium heat and sauté grated garlic and ginger for 1 minute until fragrant.",
                "Pour in the bone broth, dashi stock, soy sauce, and mirin; simmer gently for 12-15 minutes to meld the flavors.",
                "Meanwhile, boil the ramen noodles in a separate pot of unsalted water for 2-3 minutes until springy, then drain well.",
                "Divide the hot noodles among 4 deep warmed ramen bowls and ladle the steaming aromatic broth over top.",
                "Arrange seared chashu slices, halved jammy eggs, sliced scallions, bamboo shoots, and crisp nori around each bowl."
            ),
            tags = setOf("Japanese", "Noodles", "Soup", "Comfort-Food"),
            notes = "Always cook ramen noodles in a separate pot of water rather than directly in the broth so excess starch doesn't cloud the soup."
        ),

        // ==================== SOUPS & SALADS ====================
        RecipeItem(
            id = "chicken_noodle_soup",
            name = "Homestyle Chicken Noodle Soup",
            subtitle = "Tender Chicken, Egg Noodles & Herbs",
            category = RecipeCategory.SOUPS_AND_SALADS,
            prepTimeMinutes = 15,
            cookTimeMinutes = 25,
            servings = 6,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "2 tbsp unsalted butter or olive oil",
                "1 medium yellow onion, diced",
                "3 carrots, peeled and sliced",
                "3 celery stalks, sliced",
                "3 cloves garlic, minced",
                "8 cups chicken broth",
                "2.5 cups cooked shredded chicken",
                "2.5 cups wide egg noodles",
                "1/4 cup fresh parsley and dill, chopped"
            ),
            instructions = listOf(
                "Melt butter in a large pot over medium heat. Cook onion, carrots, and celery for 7-8 minutes until softened.",
                "Stir in garlic and cook for 1 minute until fragrant.",
                "Pour in chicken broth, bring to a boil, and add wide egg noodles.",
                "Simmer for 7-8 minutes until noodles and vegetables are tender.",
                "Stir in shredded chicken and fresh herbs, season with salt and black pepper, and heat through for 2 minutes."
            ),
            tags = setOf("Soup", "Poultry", "Comfort-Food", "One-Pot"),
            notes = "Rotisserie chicken makes this comforting classic come together in under 30 minutes."
        ),
        RecipeItem(
            id = "roasted_tomato_basil_soup",
            name = "Creamy Tomato Basil Soup",
            subtitle = "Perfect Companion for Grilled Cheese",
            category = RecipeCategory.SOUPS_AND_SALADS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 25,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "2 tbsp unsalted butter",
                "1 tbsp olive oil",
                "1 medium yellow onion, chopped",
                "4 cloves garlic, minced",
                "2 cans (28 oz each) whole peeled tomatoes with juices",
                "1 cup vegetable or chicken broth",
                "1/2 cup fresh basil leaves, packed",
                "1/2 cup heavy cream",
                "1 tsp sugar"
            ),
            instructions = listOf(
                "Heat butter and olive oil in a Dutch oven over medium heat. Cook onion for 7 minutes until soft and sweet.",
                "Add garlic and cook for 1 minute, then pour in canned tomatoes, broth, sugar, salt, and pepper.",
                "Simmer uncovered for 15-20 minutes, breaking up the tomatoes with a wooden spoon.",
                "Add fresh basil leaves and blend until smooth using an immersion blender.",
                "Stir in heavy cream, warm gently without boiling, and serve."
            ),
            tags = setOf("Soup", "Vegetarian", "Gluten-Free", "Classic"),
            notes = "A pinch of sugar rounds out the natural acidity of canned tomatoes."
        ),
        RecipeItem(
            id = "classic_caesar_salad",
            name = "Crisp Romaine Caesar Salad",
            subtitle = "Homemade Garlic Lemon Dressing & Croutons",
            category = RecipeCategory.SOUPS_AND_SALADS,
            prepTimeMinutes = 15,
            cookTimeMinutes = 10,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "2 heads romaine lettuce, chopped, washed, and spun dry",
                "2 cups cubed sourdough bread tossed with 2 tbsp olive oil",
                "1/3 cup mayonnaise or Greek yogurt",
                "2 tbsp fresh lemon juice",
                "1 tsp Dijon mustard",
                "1 tsp Worcestershire sauce",
                "1 clove garlic, finely grated",
                "3/4 cup shaved Parmesan cheese",
                "1/4 cup extra-virgin olive oil"
            ),
            instructions = listOf(
                "Bake oiled sourdough cubes at 375°F (190°C) for 10 minutes until golden and crisp; cool completely.",
                "Whisk mayonnaise, lemon juice, Dijon mustard, Worcestershire sauce, grated garlic, and half the Parmesan in a bowl.",
                "Slowly whisk in extra-virgin olive oil along with salt and black pepper until creamy.",
                "Toss crisp romaine leaves with the dressing in a large bowl, then top with homemade croutons and remaining shaved Parmesan."
            ),
            tags = setOf("Salad", "Classic", "Quick", "Lunch"),
            notes = "Make sure the romaine leaves are completely dry so the creamy dressing clings to every leaf."
        ),
        RecipeItem(
            id = "greek_village_salad",
            name = "Mediterranean Greek Salad",
            subtitle = "Cucumber, Tomatoes, Olives & Feta",
            category = RecipeCategory.SOUPS_AND_SALADS,
            prepTimeMinutes = 15,
            cookTimeMinutes = 0,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "4 ripe Roma tomatoes, cut into wedges",
                "1 English cucumber, halved and sliced thick",
                "1/2 red onion, thinly sliced",
                "1 green bell pepper, sliced into rings",
                "3/4 cup Kalamata olives",
                "6 oz block sheep's milk feta cheese",
                "1/4 cup extra-virgin olive oil",
                "2 tbsp red wine vinegar",
                "1 tsp dried oregano"
            ),
            instructions = listOf(
                "Combine tomato wedges, cucumber slices, red onion, green bell pepper, and Kalamata olives in a wide shallow bowl.",
                "Whisk extra-virgin olive oil, red wine vinegar, dried oregano, and a pinch of salt and pepper.",
                "Pour the dressing over the vegetables and toss gently.",
                "Place slabs or large crumbles of feta cheese on top and finish with an extra sprinkle of oregano."
            ),
            tags = setOf("Salad", "Vegetarian", "Gluten-Free", "Mediterranean", "No-Cook", "Healthy"),
            notes = "Soaking sliced red onion in cold water for 5 minutes mellows any sharp bite."
        ),
        RecipeItem(
            id = "hearty_beef_chili",
            name = "Smoky Three-Bean Beef Chili",
            subtitle = "Rich Tex-Mex One-Pot Chili",
            category = RecipeCategory.SOUPS_AND_SALADS,
            prepTimeMinutes = 15,
            cookTimeMinutes = 35,
            servings = 6,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "1.5 lb ground beef",
                "1 large yellow onion, diced",
                "1 red bell pepper, diced",
                "3 cloves garlic, minced",
                "2 tbsp chili powder",
                "1 tbsp ground cumin",
                "1 tsp smoked paprika",
                "1 can (28 oz) crushed tomatoes",
                "1 can (15 oz) kidney beans, rinsed",
                "1 can (15 oz) black beans, rinsed",
                "1 cup beef broth"
            ),
            instructions = listOf(
                "Brown ground beef, onion, and bell pepper in a large pot over medium-high heat for 8 minutes.",
                "Stir in garlic, chili powder, cumin, smoked paprika, salt, and pepper; toast spices for 1 minute.",
                "Add crushed tomatoes, kidney beans, black beans, and beef broth.",
                "Bring to a boil, then reduce heat to low and simmer uncovered for 25-30 minutes until thick.",
                "Ladle into bowls and top with sour cream, shredded cheddar, and sliced jalapeños."
            ),
            tags = setOf("Soup", "Beef", "One-Pot", "Gluten-Free", "High-Protein"),
            notes = "Chili tastes even better the next day after the spices meld in the fridge."
        ),
        RecipeItem(
            id = "minestrone_soup",
            name = "Italian Vegetable Minestrone",
            subtitle = "Garden Veggies, White Beans & Ditalini",
            category = RecipeCategory.SOUPS_AND_SALADS,
            prepTimeMinutes = 15,
            cookTimeMinutes = 25,
            servings = 6,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "2 tbsp olive oil",
                "1 onion, 2 carrots, and 2 celery stalks, diced",
                "1 medium zucchini, diced",
                "3 cloves garlic, minced",
                "1 can (15 oz) diced tomatoes",
                "1 can (15 oz) cannellini beans, rinsed",
                "5 cups vegetable broth",
                "1 tsp Italian seasoning",
                "3/4 cup small pasta (ditalini or shells)",
                "2 cups baby spinach"
            ),
            instructions = listOf(
                "Heat olive oil in a soup pot over medium heat. Sauté onion, carrots, celery, and zucchini for 7 minutes.",
                "Add garlic and Italian seasoning and cook for 1 minute.",
                "Pour in diced tomatoes, cannellini beans, and vegetable broth; bring to a simmer.",
                "Stir in small pasta and cook for 9-10 minutes until tender.",
                "Fold in baby spinach until wilted and serve topped with grated Parmesan."
            ),
            tags = setOf("Soup", "Vegetarian", "Italian", "Healthy", "One-Pot"),
            notes = "Toss a leftover Parmesan rind into the pot while simmering for extra savory depth."
        ),
        RecipeItem(
            id = "chicken_tortilla_soup",
            name = "Mexican Chicken Tortilla Soup",
            subtitle = "Smoky Tomato Chili Broth, Avocado & Crisp Strips",
            category = RecipeCategory.SOUPS_AND_SALADS,
            prepTimeMinutes = 15,
            cookTimeMinutes = 25,
            servings = 6,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "2.5 cups cooked shredded chicken",
                "1 medium yellow onion, diced, and 1 jalapeño, seeded and minced",
                "3 cloves garlic, minced",
                "1.5 tsp ground cumin and 1 tsp chili powder",
                "1 can (15 oz) fire-roasted crushed tomatoes",
                "6 cups chicken broth",
                "1 can (15 oz) black beans, rinsed, and 1 cup sweet corn kernels",
                "1 lime, juiced, and 1/3 cup fresh cilantro, chopped",
                "Crisp corn tortilla strips, diced avocado, and queso fresco for topping"
            ),
            instructions = listOf(
                "Heat 1 tbsp oil in a Dutch oven over medium heat and sauté diced onion and jalapeño for 5 minutes until softened.",
                "Stir in garlic, cumin, and chili powder; cook for 1 minute until toasted and fragrant.",
                "Pour in fire-roasted tomatoes, chicken broth, black beans, and corn; bring to a gentle boil and simmer for 15 minutes.",
                "Stir in shredded chicken, fresh lime juice, and chopped cilantro; heat through for 3 minutes.",
                "Ladle into warm bowls and pile high with crisp tortilla strips, diced avocado, and crumbled queso fresco."
            ),
            tags = setOf("Soup", "Mexican", "Hispanic", "Poultry", "Gluten-Free", "One-Pot"),
            notes = "Fry thin strips of corn tortillas in a splash of oil with a pinch of salt for fresh, shatteringly crisp soup toppers."
        ),
        RecipeItem(
            id = "avgolemono_soup",
            name = "Greek Lemon Chicken Soup (Avgolemono)",
            subtitle = "Velvety Egg-Lemon Broth with Orzo & Fresh Dill",
            category = RecipeCategory.SOUPS_AND_SALADS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 20,
            servings = 6,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "8 cups rich chicken broth",
                "3/4 cup orzo pasta (or arborio rice)",
                "2.5 cups cooked shredded chicken breast",
                "3 large eggs, at room temperature",
                "1/2 cup fresh lemon juice (about 2-3 juicy lemons)",
                "1/4 cup fresh dill, finely chopped",
                "1 tsp kosher salt and freshly ground black pepper"
            ),
            instructions = listOf(
                "Bring chicken broth to a boil in a large pot, stir in the orzo, and simmer for 8-9 minutes until tender.",
                "Reduce heat to low and stir in the shredded chicken.",
                "In a medium bowl, whisk the whole eggs and fresh lemon juice vigorously for 1 minute until pale and frothy.",
                "Slowly ladle 1.5 cups of the hot soup broth in a thin stream into the egg-lemon mixture while whisking constantly to temper the eggs.",
                "Turn off the burner, pour the tempered egg-lemon mixture back into the pot while stirring, and garnish with fresh dill and black pepper."
            ),
            tags = setOf("Soup", "Greek", "Mediterranean", "Poultry", "Comfort-Food"),
            notes = "Tempering the eggs slowly and keeping the soup below a boil after adding them creates a silky, cream-free velvet broth."
        ),
        RecipeItem(
            id = "japanese_miso_soup",
            name = "Traditional Japanese Dashi Miso Soup",
            subtitle = "Silken Tofu, Wakame Seaweed & Scallions",
            category = RecipeCategory.SOUPS_AND_SALADS,
            prepTimeMinutes = 5,
            cookTimeMinutes = 10,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "4 cups Japanese dashi stock (4 cups water + 1 piece kombu + 1 cup bonito flakes, or 2 tsp dashi granules)",
                "3 to 4 tbsp white or red miso paste",
                "6 oz silken or soft tofu, cut into 1/2-inch cubes",
                "1 tbsp dried wakame seaweed",
                "2 green onions, thinly sliced"
            ),
            instructions = listOf(
                "Bring dashi stock to a gentle simmer in a saucepan over medium heat.",
                "Add the cubed silken tofu and dried wakame seaweed; warm gently for 2 minutes until the wakame expands and softens.",
                "Turn off the heat completely so the delicate aroma of the miso is preserved.",
                "Place miso paste in a fine-mesh strainer or ladle submerged in the hot broth and stir until smoothly dissolved into the soup.",
                "Ladle immediately into bowls and scatter thinly sliced green onions on top."
            ),
            tags = setOf("Soup", "Japanese", "Quick", "Healthy", "Gluten-Free"),
            notes = "Never boil miso soup after whisking in the miso paste—high heat drives off its savory fermented aroma and causes it to separate."
        ),
        RecipeItem(
            id = "french_onion_soup",
            name = "Caramelized French Onion Soup",
            subtitle = "Slow-Sweetened Onions & Toasted Gruyère Crouton",
            category = RecipeCategory.SOUPS_AND_SALADS,
            prepTimeMinutes = 15,
            cookTimeMinutes = 50,
            servings = 4,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "4 large yellow onions, thinly sliced",
                "4 tbsp unsalted butter and 1 tbsp olive oil",
                "1/2 tsp sugar and 1 tsp kosher salt",
                "2 cloves garlic, minced",
                "1 tbsp all-purpose flour",
                "1/2 cup dry white wine or dry sherry",
                "6 cups rich beef broth",
                "4 sprigs fresh thyme and 1 bay leaf",
                "8 slices toasted French baguette and 2 cups shredded Gruyère cheese"
            ),
            instructions = listOf(
                "Melt butter with olive oil in a Dutch oven over medium-low heat. Add sliced onions, salt, and sugar; cook slowly for 35-40 minutes, stirring often until deep mahogany brown.",
                "Stir in minced garlic and flour and cook for 1 minute.",
                "Pour in white wine, scraping up the browned fond from the bottom of the pot, and simmer until reduced by half.",
                "Add beef broth, thyme sprigs, and bay leaf; simmer partially covered for 15 minutes, then discard herbs.",
                "Ladle soup into oven-safe crocks, float toasted baguette slices on top, pile generously with shredded Gruyère, and broil for 2-3 minutes until bubbling and golden."
            ),
            tags = setOf("Soup", "Comfort-Food", "Classic", "Dinner"),
            notes = "Patience while caramelizing the onions is everything—their natural sugars turn deep golden and savory-sweet."
        ),
        RecipeItem(
            id = "broccoli_cheddar_soup",
            name = "Velvety Broccoli Cheddar Soup",
            subtitle = "Sharp Cheddar, Tender Broccoli Florets & Carrots",
            category = RecipeCategory.SOUPS_AND_SALADS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 25,
            servings = 6,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "4 cups fresh broccoli florets and peeled stems, chopped small",
                "1 large carrot, julienned or coarsely grated",
                "1 small yellow onion, finely diced, and 2 cloves garlic, minced",
                "4 tbsp unsalted butter",
                "1/4 cup all-purpose flour",
                "3 cups chicken or vegetable broth",
                "1.5 cups half-and-half or whole milk",
                "2.5 cups sharp cheddar cheese, freshly shredded",
                "1/2 tsp Dijon mustard and 1 pinch nutmeg"
            ),
            instructions = listOf(
                "Melt butter in a Dutch oven over medium heat and sauté diced onion and garlic for 4 minutes until translucent.",
                "Whisk in flour and cook for 1 minute, then slowly whisk in broth and half-and-half until smooth.",
                "Add chopped broccoli, grated carrot, Dijon mustard, nutmeg, salt, and black pepper; simmer on medium-low for 15 minutes until broccoli is tender.",
                "Remove the pot from the heat and stir in freshly shredded sharp cheddar one handful at a time until velvety and melted.",
                "Ladle into warm bowls or crusty sourdough bread bowls."
            ),
            tags = setOf("Soup", "Vegetarian", "Comfort-Food", "One-Pot", "Kid-Friendly"),
            notes = "Take the pot off the burner before stirring in the cheddar so the cheese melts into a smooth cream without turning grainy."
        ),
        RecipeItem(
            id = "potato_leek_soup",
            name = "Creamy Potato Leek Soup",
            subtitle = "Silky Yukon Golds, Buttery Leeks & Fresh Chives",
            category = RecipeCategory.SOUPS_AND_SALADS,
            prepTimeMinutes = 15,
            cookTimeMinutes = 25,
            servings = 6,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "3 large leeks (white and light green parts only), washed and thinly sliced",
                "2 lb Yukon Gold potatoes, peeled and diced",
                "3 tbsp unsalted butter",
                "3 cloves garlic, smashed",
                "4.5 cups chicken or vegetable broth",
                "1 bay leaf and 3 sprigs fresh thyme",
                "1/2 cup heavy cream",
                "2 tbsp fresh chives, finely chopped"
            ),
            instructions = listOf(
                "Melt butter in a heavy soup pot over medium-low heat. Add sliced leeks, garlic, and a pinch of salt; sweat gently for 8-10 minutes without browning.",
                "Add diced potatoes, broth, bay leaf, and thyme sprigs; bring to a boil, then simmer covered for 15 minutes until potatoes are fork-tender.",
                "Remove bay leaf and thyme stems, then puree the soup until velvety smooth using an immersion blender.",
                "Stir in heavy cream, adjust salt and white or black pepper to taste, and serve topped with fresh chopped chives."
            ),
            tags = setOf("Soup", "Vegetarian", "Gluten-Free", "Comfort-Food", "One-Pot"),
            notes = "Rinse sliced leeks in a bowl of cold water and lift them out so any hidden sand sinks to the bottom."
        ),
        RecipeItem(
            id = "insalata_caprese",
            name = "Classic Italian Insalata Caprese",
            subtitle = "Vine-Ripened Tomatoes, Fresh Mozzarella & Basil",
            category = RecipeCategory.SOUPS_AND_SALADS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 0,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "3 large ripe heirloom or vine tomatoes, sliced 1/4-inch thick",
                "12 oz fresh mozzarella di bufala or fior di latte, sliced",
                "1/2 cup fresh sweet basil leaves",
                "3 tbsp extra-virgin olive oil",
                "1 tbsp aged balsamic glaze (optional)",
                "1/2 tsp flaky sea salt and freshly cracked black pepper"
            ),
            instructions = listOf(
                "Arrange alternating overlapping slices of ripe tomato and fresh mozzarella on a wide serving platter.",
                "Tuck whole fresh basil leaves between the tomato and mozzarella slices.",
                "Drizzle generously with fruity extra-virgin olive oil and aged balsamic glaze if desired.",
                "Season right before serving with flaky sea salt and freshly cracked black pepper."
            ),
            tags = setOf("Salad", "Italian", "Vegetarian", "Gluten-Free", "No-Cook", "Quick"),
            notes = "Serve tomatoes and fresh mozzarella at room temperature rather than straight from the fridge for peak flavor."
        ),

        // ==================== SIDES & SNACKS ====================
        RecipeItem(
            id = "fresh_guacamole",
            name = "Fresh Lime & Cilantro Guacamole",
            subtitle = "Chunky Restaurant-Style Dip",
            category = RecipeCategory.SIDES_AND_SNACKS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 0,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "3 ripe Hass avocados",
                "1/3 cup finely diced red onion",
                "1 Roma tomato, seeded and diced",
                "1/3 cup fresh cilantro, chopped",
                "1 jalapeño, seeded and minced",
                "2 tbsp fresh lime juice",
                "3/4 tsp kosher salt"
            ),
            instructions = listOf(
                "Halve and pit the avocados, then scoop the flesh into a mixing bowl.",
                "Mash coarsely with a fork so the texture stays slightly chunky.",
                "Fold in red onion, tomato, cilantro, minced jalapeño, lime juice, and salt.",
                "Taste and adjust lime juice or salt, then serve immediately with tortilla chips."
            ),
            tags = setOf("Vegan", "Gluten-Free", "No-Cook", "Quick", "Mexican"),
            notes = "Press plastic wrap directly against the surface of leftover guacamole to prevent browning."
        ),
        RecipeItem(
            id = "crispy_garlic_bread",
            name = "Bakery Garlic Herb Bread",
            subtitle = "Crisp Edges & Soft Buttery Center",
            category = RecipeCategory.SIDES_AND_SNACKS,
            prepTimeMinutes = 5,
            cookTimeMinutes = 12,
            servings = 6,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "1 loaf French or Italian bread, halved lengthwise",
                "1/2 cup (1 stick) unsalted butter, softened",
                "4 cloves garlic, finely grated or minced",
                "2 tbsp fresh parsley, finely chopped",
                "1/4 cup grated Parmesan cheese",
                "1/4 tsp kosher salt"
            ),
            instructions = listOf(
                "Preheat oven to 400°F (200°C).",
                "Mix softened butter, grated garlic, parsley, Parmesan, and salt in a small bowl.",
                "Spread the garlic butter evenly across the cut sides of the bread loaf.",
                "Bake cut-side up on a baking sheet for 10-12 minutes until the edges are golden and crisp.",
                "Slice into thick pieces and serve warm."
            ),
            tags = setOf("Vegetarian", "Side", "Italian", "Quick"),
            notes = "For a cheesy variation, sprinkle shredded mozzarella on top for the last 3 minutes of baking."
        ),
        RecipeItem(
            id = "creamy_mashed_potatoes",
            name = "Creamy Garlic Mashed Potatoes",
            subtitle = "Yukon Gold Potatoes with Butter & Cream",
            category = RecipeCategory.SIDES_AND_SNACKS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 20,
            servings = 6,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "2.5 lb Yukon Gold potatoes, peeled and cut into quarters",
                "4 cloves garlic, peeled",
                "6 tbsp unsalted butter, melted",
                "3/4 cup warm whole milk or heavy cream",
                "1.5 tsp kosher salt",
                "1/4 tsp freshly ground black pepper",
                "2 tbsp chopped fresh chives"
            ),
            instructions = listOf(
                "Place cut potatoes and peeled garlic cloves in a large pot and cover with cold salted water by 1 inch.",
                "Bring to a boil and simmer for 15-18 minutes until potatoes are easily pierced with a fork.",
                "Drain well and return potatoes to the warm dry pot for 1 minute to let excess steam evaporate.",
                "Mash potatoes and garlic with a potato masher, then fold in melted butter before gently stirring in warm milk.",
                "Season with salt and pepper and top with fresh chives."
            ),
            tags = setOf("Vegetarian", "Gluten-Free", "Side", "Comfort-Food"),
            notes = "Adding butter before the milk coats the potato starch molecules and keeps the mash fluffy instead of gluey."
        ),
        RecipeItem(
            id = "crispy_roasted_potatoes",
            name = "Crispy Rosemary Roasted Potatoes",
            subtitle = "Shatteringly Crisp Outside, Fluffy Inside",
            category = RecipeCategory.SIDES_AND_SNACKS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 35,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "2 lb Yukon Gold or russet potatoes, cut into 1.5-inch chunks",
                "3 tbsp olive oil",
                "1/2 tsp baking soda",
                "1 tbsp fresh rosemary, finely chopped",
                "3 cloves garlic, minced",
                "1 tsp kosher salt",
                "1/2 tsp black pepper"
            ),
            instructions = listOf(
                "Preheat oven to 425°F (220°C). Parboil potato chunks in salted water with baking soda for 8 minutes; drain.",
                "Return drained potatoes to the pot and shake vigorously for 10 seconds to rough up the outer edges.",
                "Toss potatoes on a baking sheet with olive oil, rosemary, garlic, salt, and pepper.",
                "Roast for 30-35 minutes, flipping halfway through, until deeply golden and crisp on all sides."
            ),
            tags = setOf("Vegan", "Gluten-Free", "Side"),
            notes = "Shaking the parboiled potatoes creates a thin starchy slurry on the surface that bakes into an extra-crunchy crust."
        ),
        RecipeItem(
            id = "creamy_hummus",
            name = "Ultra-Smooth Lemon Tahini Hummus",
            subtitle = "Whipped Chickpea Dip",
            category = RecipeCategory.SIDES_AND_SNACKS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 0,
            servings = 6,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "1 can (15 oz) chickpeas, drained and rinsed",
                "1/3 cup well-stirred tahini",
                "3 tbsp fresh lemon juice",
                "1 small clove garlic, minced",
                "2 tbsp extra-virgin olive oil, plus more for drizzling",
                "1/2 tsp ground cumin",
                "2 to 3 tbsp ice-cold water",
                "3/4 tsp kosher salt"
            ),
            instructions = listOf(
                "Blend tahini and lemon juice in a food processor for 1 minute until whipped and pale.",
                "Add olive oil, minced garlic, cumin, and salt; blend for 30 seconds.",
                "Add drained chickpeas and process for 2 minutes, scraping down the sides of the bowl.",
                "With the motor running, drizzle in ice-cold water 1 tbsp at a time until silky and airy.",
                "Swirl into a shallow bowl, drizzle with olive oil and paprika, and serve with warm pita and veggies."
            ),
            tags = setOf("Vegan", "Gluten-Free", "No-Cook", "Healthy", "Mediterranean"),
            notes = "Whipping the tahini and lemon juice first—plus a splash of ice water—creates a cloud-like texture."
        ),
        RecipeItem(
            id = "tomato_bruschetta",
            name = "Classic Tomato Basil Bruschetta",
            subtitle = "Toasted Crostini with Garlic & Balsamic",
            category = RecipeCategory.SIDES_AND_SNACKS,
            prepTimeMinutes = 15,
            cookTimeMinutes = 5,
            servings = 6,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "5 ripe Roma tomatoes, seeded and finely diced",
                "1/3 cup fresh basil leaves, chiffonade",
                "2 tbsp extra-virgin olive oil",
                "1 tbsp balsamic vinegar or glaze",
                "1 baguette, sliced into 1/2-inch rounds",
                "1 whole clove garlic, peeled and halved",
                "1/2 tsp flaky sea salt"
            ),
            instructions = listOf(
                "Toss diced tomatoes, basil, olive oil, balsamic vinegar, and sea salt in a bowl; let marinate for 10 minutes.",
                "Brush baguette slices lightly with olive oil and toast under the broiler or in a skillet until golden.",
                "Rub the warm toasted bread slices lightly with the cut side of the raw garlic clove.",
                "Spoon the marinated tomato mixture onto the crostini right before serving."
            ),
            tags = setOf("Vegan", "Appetizer", "Italian", "Quick"),
            notes = "Rubbing a raw garlic clove over warm toast infuses gentle garlic aroma without overpowering the fresh tomatoes."
        ),
        RecipeItem(
            id = "crispy_spanakopita",
            name = "Greek Spinach & Feta Spanakopita",
            subtitle = "Flaky Golden Phyllo Triangles with Fresh Herbs",
            category = RecipeCategory.SIDES_AND_SNACKS,
            prepTimeMinutes = 25,
            cookTimeMinutes = 25,
            servings = 8,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "1 lb fresh baby spinach, wilted and squeezed bone-dry, chopped",
                "8 oz Greek feta cheese, crumbled",
                "1/2 cup ricotta or cottage cheese",
                "4 green onions, finely chopped, and 1/3 cup fresh dill and parsley",
                "2 large eggs, lightly beaten",
                "12 sheets thawed phyllo pastry",
                "1/2 cup melted unsalted butter mixed with 2 tbsp olive oil"
            ),
            instructions = listOf(
                "Preheat oven to 375°F (190°C) and line two baking sheets with parchment paper.",
                "Mix squeezed chopped spinach, crumbled feta, ricotta, green onions, fresh dill, parsley, beaten eggs, and black pepper in a bowl.",
                "Lay 1 sheet of phyllo on a work surface, brush lightly with the butter-oil mixture, top with a second sheet, brush again, and cut lengthwise into 3 strips.",
                "Spoon 1 heaping tablespoon of filling at the bottom of each strip and fold diagonally like a flag into a neat triangle; repeat with remaining phyllo.",
                "Brush the tops with melted butter and bake for 22-25 minutes until puffed, flaky, and golden brown."
            ),
            tags = setOf("Greek", "Mediterranean", "Vegetarian", "Appetizer", "Baking"),
            notes = "Keep unused phyllo sheets covered with a slightly damp kitchen towel while assembling so they don't dry out."
        ),
        RecipeItem(
            id = "mexican_street_corn_elote",
            name = "Mexican Street Corn (Elote)",
            subtitle = "Charred Sweet Corn with Crema, Cotija, Lime & Chili",
            category = RecipeCategory.SIDES_AND_SNACKS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 10,
            servings = 4,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "4 ears fresh sweet corn, husked (or 4 cups corn kernels for esquites)",
                "1/4 cup mayonnaise",
                "1/4 cup Mexican crema or sour cream",
                "1/2 cup cotija or queso fresco cheese, finely crumbled",
                "1/2 cup fresh cilantro, finely chopped",
                "1 clove garlic, finely grated",
                "1 lime, cut into wedges, and 1 tsp chili powder or Tajín"
            ),
            instructions = listOf(
                "Whisk mayonnaise, Mexican crema, grated garlic, and a squeeze of lime juice together in a small bowl.",
                "Grill the husked corn over high heat (or char kernels in a dry hot cast-iron skillet) for 8-10 minutes, turning until blistered and golden.",
                "Brush the hot charred corn generously all over with the creamy garlic-lime sauce.",
                "Sprinkle heavily with crumbled cotija cheese, chopped cilantro, and chili powder or Tajín, and serve warm with lime wedges."
            ),
            tags = setOf("Mexican", "Hispanic", "Vegetarian", "Gluten-Free", "Side", "Quick"),
            notes = "For easy bowl-style serving (esquites), slice the charred kernels off the cob and toss everything together in a warm bowl."
        ),
        RecipeItem(
            id = "pan_fried_pork_gyoza",
            name = "Crispy Pan-Fried Japanese Pork Gyoza",
            subtitle = "Lacy Bottom Dumplings with Soy-Vinegar Dip",
            category = RecipeCategory.SIDES_AND_SNACKS,
            prepTimeMinutes = 25,
            cookTimeMinutes = 10,
            servings = 4,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "24 round gyoza or wonton wrappers",
                "1/2 lb ground pork",
                "1.5 cups napa cabbage, finely minced and squeezed dry",
                "3 green onions, minced, plus 2 cloves garlic and 1 tsp ginger, grated",
                "1 tbsp soy sauce and 1 tbsp toasted sesame oil",
                "1 tbsp vegetable oil and 1/3 cup water for steaming",
                "Dipping sauce: 2 tbsp soy sauce, 2 tbsp rice vinegar, and 1 tsp chili oil"
            ),
            instructions = listOf(
                "Mix ground pork, squeezed napa cabbage, green onions, garlic, ginger, soy sauce, and sesame oil thoroughly in a bowl.",
                "Place 1 rounded teaspoon of filling in the center of each gyoza wrapper, moisten the edge with water, and fold in half, pleating one side to seal.",
                "Heat vegetable oil in a large nonstick skillet over medium-high heat, arrange gyoza flat-side down, and sear for 2-3 minutes until golden brown on the bottoms.",
                "Carefully pour in 1/3 cup water, cover immediately with a tight lid, and steam for 4 minutes until wrappers are translucent and pork is cooked through.",
                "Uncover and cook 1 minute more until all water evaporates and bottoms are crisp again. Serve with soy-vinegar dipping sauce."
            ),
            tags = setOf("Japanese", "Pork", "Appetizer", "Dairy-Free"),
            notes = "Squeezing the salted minced cabbage dry keeps the gyoza filling juicy without making the wrappers soggy."
        ),

        // ==================== BAKING & DESSERTS ====================
        RecipeItem(
            id = "chocolate_chip_cookies",
            name = "Chewy Bakery Chocolate Chip Cookies",
            subtitle = "Crisp Edges & Gooey Centers",
            category = RecipeCategory.BAKING_AND_DESSERTS,
            prepTimeMinutes = 15,
            cookTimeMinutes = 11,
            servings = 16,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "2.25 cups all-purpose flour",
                "1 tsp baking soda",
                "1/2 tsp kosher salt",
                "3/4 cup (1.5 sticks) unsalted butter, melted and cooled",
                "1 cup packed brown sugar",
                "1/2 cup granulated sugar",
                "1 large egg plus 1 egg yolk",
                "2 tsp vanilla extract",
                "1.5 cups semisweet chocolate chips or chunks"
            ),
            instructions = listOf(
                "Preheat oven to 350°F (175°C) and line two baking sheets with parchment paper.",
                "Whisk flour, baking soda, and salt together in a medium bowl.",
                "Whisk melted butter, brown sugar, and granulated sugar in a large bowl for 1 minute until smooth.",
                "Beat in the whole egg, egg yolk, and vanilla extract, then fold in the dry ingredients and chocolate chips.",
                "Scoop 2-tablespoon balls of dough onto the baking sheets 2 inches apart.",
                "Bake for 10-12 minutes until the edges are golden brown but the centers still look soft. Cool on the pan for 5 minutes."
            ),
            tags = setOf("Dessert", "Baking", "Classic", "Sweet"),
            notes = "Using melted butter and an extra egg yolk gives these cookies rich chewiness with zero chill time required."
        ),
        RecipeItem(
            id = "fudgy_brownies",
            name = "Crinkle-Top Fudgy Brownies",
            subtitle = "One-Bowl Double Chocolate Squares",
            category = RecipeCategory.BAKING_AND_DESSERTS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 25,
            servings = 12,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "1/2 cup (1 stick) unsalted butter, melted",
                "1 cup granulated sugar",
                "2 large eggs",
                "1 tsp vanilla extract",
                "1/2 cup unsweetened cocoa powder",
                "1/2 cup all-purpose flour",
                "1/4 tsp kosher salt",
                "1/2 cup semisweet chocolate chips"
            ),
            instructions = listOf(
                "Preheat oven to 350°F (175°C). Line an 8x8-inch baking pan with parchment paper overhang.",
                "Whisk hot melted butter and sugar together vigorously for 1 minute.",
                "Add eggs and vanilla and whisk for another full minute until pale and glossy (this creates the shiny crinkle top).",
                "Fold in cocoa powder, flour, salt, and chocolate chips with a spatula just until combined.",
                "Spread into the pan and bake for 22-26 minutes until a toothpick inserted in the center comes out with moist crumbs."
            ),
            tags = setOf("Dessert", "Baking", "Chocolate", "One-Bowl"),
            notes = "Whisking the eggs and sugar thoroughly dissolves the sugar crystals so they rise to form a paper-thin glossy crust."
        ),
        RecipeItem(
            id = "moist_banana_bread",
            name = "One-Bowl Classic Banana Bread",
            subtitle = "Sweet Ripe Bananas & Warm Cinnamon",
            category = RecipeCategory.BAKING_AND_DESSERTS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 55,
            servings = 8,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "3 large overripe speckled bananas, mashed",
                "1/3 cup unsalted butter, melted",
                "3/4 cup brown sugar",
                "1 large egg, beaten",
                "1 tsp vanilla extract",
                "1 tsp baking soda",
                "1 tsp ground cinnamon",
                "1/4 tsp salt",
                "1.5 cups all-purpose flour"
            ),
            instructions = listOf(
                "Preheat oven to 350°F (175°C) and grease a 9x5-inch loaf pan.",
                "Mash the ripe bananas in a large bowl with a fork until mostly smooth, then stir in the melted butter.",
                "Mix in brown sugar, beaten egg, vanilla, baking soda, cinnamon, and salt.",
                "Fold in the flour gently until just incorporated; optional: fold in 1/2 cup chopped walnuts or chocolate chips.",
                "Bake for 50-60 minutes until a toothpick inserted in the center comes out clean. Cool 10 minutes before slicing."
            ),
            tags = setOf("Baking", "Sweet", "Breakfast", "One-Bowl"),
            notes = "The darker and more spotted your bananas are, the sweeter and more fragrant the loaf will be."
        ),
        RecipeItem(
            id = "classic_apple_crisp",
            name = "Cinnamon Oat Apple Crisp",
            subtitle = "Warm Baked Apples with Streusel Topping",
            category = RecipeCategory.BAKING_AND_DESSERTS,
            prepTimeMinutes = 15,
            cookTimeMinutes = 40,
            servings = 6,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "6 medium Granny Smith or Honeycrisp apples, peeled and sliced",
                "2 tbsp granulated sugar",
                "1 tbsp lemon juice",
                "1.5 tsp ground cinnamon, divided",
                "1 cup old-fashioned rolled oats",
                "3/4 cup all-purpose flour",
                "3/4 cup packed brown sugar",
                "1/2 cup cold unsalted butter, cubed"
            ),
            instructions = listOf(
                "Preheat oven to 350°F (175°C) and butter an 8x8-inch or 9-inch baking dish.",
                "Toss sliced apples with granulated sugar, lemon juice, and 1/2 tsp cinnamon; arrange evenly in the baking dish.",
                "Combine oats, flour, brown sugar, remaining 1 tsp cinnamon, and a pinch of salt in a bowl.",
                "Cut in the cold butter cubes with your fingertips or a fork until coarse clumps form, then scatter over the apples.",
                "Bake for 40-45 minutes until the fruit is bubbling and the oat topping is crisp and golden. Serve warm with vanilla ice cream."
            ),
            tags = setOf("Dessert", "Baking", "Fruit", "Vegetarian"),
            notes = "Combining tart Granny Smith with sweet Honeycrisp apples gives the filling balanced flavor and texture."
        ),
        RecipeItem(
            id = "classic_tiramisu",
            name = "Classic Italian Tiramisu",
            subtitle = "Espresso-Soaked Ladyfingers & Mascarpone Cream",
            category = RecipeCategory.BAKING_AND_DESSERTS,
            prepTimeMinutes = 25,
            cookTimeMinutes = 0,
            servings = 8,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "24 savoiardi (Italian ladyfinger) cookies",
                "1.5 cups strong brewed espresso, cooled",
                "16 oz (2 cups) mascarpone cheese, softened",
                "1 cup heavy whipping cream, cold",
                "1/2 cup granulated sugar",
                "1 tsp vanilla extract",
                "3 tbsp unsweetened Dutch-process cocoa powder"
            ),
            instructions = listOf(
                "Beat cold heavy cream, sugar, and vanilla with a mixer until medium peaks form.",
                "Gently fold the softened mascarpone into the whipped cream until smooth and billowy.",
                "Dip each ladyfinger briefly (1 second per side) into cooled espresso and line the bottom of an 8x8-inch dish.",
                "Spread half of the mascarpone cream evenly over the first layer of ladyfingers.",
                "Repeat with a second layer of dipped ladyfingers and remaining mascarpone cream.",
                "Chill covered for at least 4 hours, then dust generously with cocoa powder before serving."
            ),
            tags = setOf("Dessert", "No-Cook", "Italian", "Make-Ahead"),
            notes = "Dip ladyfingers for just a quick second—they soften further as the tiramisu chills."
        ),
        RecipeItem(
            id = "cinnamon_churros",
            name = "Crispy Cinnamon Sugar Churros",
            subtitle = "Piped Choux Pastry with Warm Chocolate Sauce",
            category = RecipeCategory.BAKING_AND_DESSERTS,
            prepTimeMinutes = 15,
            cookTimeMinutes = 15,
            servings = 6,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "1 cup water",
                "2.5 tbsp unsalted butter",
                "2 tbsp granulated sugar plus 1/2 cup for coating",
                "1/4 tsp kosher salt",
                "1 cup all-purpose flour",
                "1 large egg and 1 tsp vanilla extract",
                "1.5 tsp ground cinnamon",
                "4 oz dark chocolate melted with 1/2 cup warm heavy cream",
                "Vegetable oil for frying"
            ),
            instructions = listOf(
                "Combine 1/2 cup sugar and ground cinnamon in a shallow dish and set aside.",
                "Bring water, butter, 2 tbsp sugar, and salt to a rolling boil in a saucepan, then remove from heat and stir in flour all at once until a smooth dough ball forms.",
                "Cool dough for 5 minutes, then beat in the egg and vanilla until glossy and pipeable; transfer to a piping bag fitted with a large star tip.",
                "Pipe 5-inch strips into 360°F vegetable oil, snipping the ends with kitchen shears, and fry for 2 minutes per side until deep golden and crisp.",
                "Drain briefly on paper towels, roll immediately in cinnamon sugar, and serve warm with dark chocolate dipping sauce."
            ),
            tags = setOf("Dessert", "Hispanic", "Mexican", "Sweet"),
            notes = "Using a star piping tip creates ridges that maximize crispy surface area and prevent steam pockets while frying."
        ),
        RecipeItem(
            id = "honey_walnut_baklava",
            name = "Greek Honey & Walnut Baklava",
            subtitle = "Crisp Layered Phyllo with Lemon Cinnamon Syrup",
            category = RecipeCategory.BAKING_AND_DESSERTS,
            prepTimeMinutes = 30,
            cookTimeMinutes = 45,
            servings = 12,
            difficulty = RecipeDifficulty.MEDIUM,
            ingredients = listOf(
                "16 oz thawed phyllo dough sheets",
                "3 cups walnuts and pistachios, finely chopped",
                "1.5 tsp ground cinnamon",
                "1 cup (2 sticks) unsalted butter, melted",
                "1 cup granulated sugar and 3/4 cup water",
                "3/4 cup Greek wildflower honey",
                "1 tbsp fresh lemon juice and 1 strip lemon peel"
            ),
            instructions = listOf(
                "Simmer sugar, water, honey, lemon juice, and lemon peel in a small saucepan for 10 minutes, then cool the syrup completely to room temperature.",
                "Preheat oven to 350°F (175°C). Toss finely chopped nuts with ground cinnamon.",
                "Layer 8 buttered sheets of phyllo in the bottom of a buttered 9x13-inch pan, scatter a third of the spiced nuts on top, and repeat with 6 sheets and nuts twice more.",
                "Top with 8 final buttered phyllo sheets and use a sharp knife to score the pastry into diamond shapes all the way through.",
                "Bake for 45-50 minutes until crisp and golden, then immediately ladle the cooled honey syrup over the hot baklava so it sizzles and absorbs without getting soggy."
            ),
            tags = setOf("Dessert", "Greek", "Mediterranean", "Baking", "Sweet"),
            notes = "Always pour cold syrup over hot-from-the-oven baklava so the phyllo layers stay shatteringly crisp."
        ),

        // ==================== SAUCES & DRINKS ====================
        RecipeItem(
            id = "homemade_marinara",
            name = "20-Minute San Marzano Marinara",
            subtitle = "Sweet Garlic, Olive Oil & Fresh Basil",
            category = RecipeCategory.SAUCES_AND_DRINKS,
            prepTimeMinutes = 5,
            cookTimeMinutes = 20,
            servings = 6,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "1 can (28 oz) whole peeled San Marzano tomatoes",
                "1/4 cup extra-virgin olive oil",
                "5 cloves garlic, thinly sliced",
                "1/4 tsp red pepper flakes",
                "1 large sprig fresh basil",
                "3/4 tsp kosher salt"
            ),
            instructions = listOf(
                "Crush whole tomatoes by hand in a bowl along with their juices.",
                "Warm olive oil, sliced garlic, and red pepper flakes in a wide saucepan over medium-low heat until garlic is lightly golden.",
                "Carefully pour in crushed tomatoes, salt, and the whole basil sprig.",
                "Simmer uncovered for 15-20 minutes until thickened and fragrant, then discard basil sprig before serving."
            ),
            tags = setOf("Sauce", "Vegan", "Gluten-Free", "Italian", "Quick"),
            notes = "Starting the sliced garlic in cold olive oil infuses the oil evenly without scorching."
        ),
        RecipeItem(
            id = "fresh_basil_pesto",
            name = "Genovese Fresh Basil Pesto",
            subtitle = "Pine Nuts, Parmesan & Extra-Virgin Olive Oil",
            category = RecipeCategory.SAUCES_AND_DRINKS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 0,
            servings = 6,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "2 cups packed fresh basil leaves",
                "1/3 cup toasted pine nuts (or walnuts)",
                "2 cloves garlic",
                "1/2 cup freshly grated Parmesan cheese",
                "1/2 cup extra-virgin olive oil",
                "1 tsp fresh lemon juice",
                "1/2 tsp kosher salt"
            ),
            instructions = listOf(
                "Pulse pine nuts and garlic in a food processor until finely chopped.",
                "Add basil leaves, lemon juice, and salt; pulse 8-10 times until coarsely minced.",
                "With the processor on low, drizzle in olive oil slowly until emulsified.",
                "Stir in grated Parmesan by hand so the sauce keeps a vibrant green color and delicate texture."
            ),
            tags = setOf("Sauce", "Vegetarian", "Gluten-Free", "No-Cook", "Italian"),
            notes = "A squeeze of lemon juice keeps fresh basil pesto bright emerald green."
        ),
        RecipeItem(
            id = "berry_banana_smoothie",
            name = "Triple Berry Protein Smoothie",
            subtitle = "Creamy Yogurt, Banana & Mixed Berries",
            category = RecipeCategory.SAUCES_AND_DRINKS,
            prepTimeMinutes = 5,
            cookTimeMinutes = 0,
            servings = 2,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "1.5 cups frozen mixed berries (strawberries, blueberries, raspberries)",
                "1 ripe banana",
                "3/4 cup Greek yogurt",
                "1 cup milk or oat milk",
                "1 tbsp honey or maple syrup",
                "1 tbsp chia seeds or ground flaxseed"
            ),
            instructions = listOf(
                "Pour milk and Greek yogurt into the blender first so the blades spin smoothly.",
                "Add banana, frozen berries, honey, and chia seeds.",
                "Blend on high for 45-60 seconds until completely smooth and creamy, then pour into chilled glasses."
            ),
            tags = setOf("Drink", "Vegetarian", "Gluten-Free", "No-Cook", "Healthy", "Quick"),
            notes = "Using frozen berries creates a thick milkshake-like texture without diluting the flavor with ice."
        ),
        RecipeItem(
            id = "fresh_squeezed_lemonade",
            name = "Classic Fresh-Squeezed Lemonade",
            subtitle = "With Lemon Zest Simple Syrup",
            category = RecipeCategory.SAUCES_AND_DRINKS,
            prepTimeMinutes = 10,
            cookTimeMinutes = 5,
            servings = 6,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "6 large lemons (about 1 cup fresh lemon juice)",
                "3/4 cup granulated sugar",
                "1 cup warm water (for dissolving sugar)",
                "4 cups cold water",
                "1 lemon, thinly sliced for garnish",
                "Fresh mint sprigs and ice"
            ),
            instructions = listOf(
                "Peel 3 strips of zest from one lemon and dissolve sugar in 1 cup warm water with the zest to make a fragrant syrup; cool.",
                "Roll the lemons firmly on the counter, halve them, and juice to yield 1 cup fresh juice.",
                "Strain the cooled syrup into a pitcher along with fresh lemon juice and 4 cups cold water.",
                "Stir well and serve over ice garnished with lemon wheels and mint."
            ),
            tags = setOf("Drink", "Vegan", "Gluten-Free", "Refreshing"),
            notes = "Steeping lemon peel in the sugar syrup extracts natural citrus oils for brighter flavor."
        ),
        RecipeItem(
            id = "classic_vinaigrette",
            name = "Everyday Dijon Shallot Vinaigrette",
            subtitle = "Balanced Salad Dressing in a Jar",
            category = RecipeCategory.SAUCES_AND_DRINKS,
            prepTimeMinutes = 5,
            cookTimeMinutes = 0,
            servings = 6,
            difficulty = RecipeDifficulty.EASY,
            ingredients = listOf(
                "1/2 cup extra-virgin olive oil",
                "3 tbsp red wine vinegar or champagne vinegar",
                "1 tbsp Dijon mustard",
                "1 tsp honey",
                "1 small shallot, finely minced",
                "1/2 tsp kosher salt",
                "1/4 tsp freshly ground black pepper"
            ),
            instructions = listOf(
                "Place minced shallot, vinegar, Dijon mustard, honey, salt, and pepper in a mason jar.",
                "Pour in extra-virgin olive oil, seal the lid tightly, and shake vigorously for 15 seconds until emulsified.",
                "Drizzle over mixed greens or roasted vegetables; store leftovers in the fridge for up to 1 week."
            ),
            tags = setOf("Sauce", "Vegetarian", "Gluten-Free", "No-Cook", "Quick"),
            notes = "Dijon mustard acts as a natural emulsifier that keeps the oil and vinegar smoothly combined."
        )
    )

    fun formatDurationMinutes(minutes: Int): String {
        val safeMinutes = minutes.coerceAtLeast(0)
        if (safeMinutes == 0) return "0 min"
        val hours = safeMinutes / 60
        val rem = safeMinutes % 60
        return when {
            hours == 0 -> "$rem min"
            rem == 0 -> "${hours}h"
            else -> "${hours}h ${rem}m"
        }
    }

    /**
     * Searches the recipe catalog by query and optional category filter.
     * Matches recipe name, subtitle/cuisine, category displayName, ingredients, tags, difficulty, and notes.
     * Ranks exact name matches first, name prefix matches second, word-prefix matches third,
     * name substring matches fourth, and remaining matches alphabetically by name.
     */
    fun searchRecipes(
        catalog: List<RecipeItem>,
        query: String,
        category: RecipeCategory? = null
    ): List<RecipeItem> {
        val q = query.trim().lowercase(Locale.US)
        val filteredByCategory = if (category != null) {
            catalog.filter { it.category == category }
        } else {
            catalog
        }
        if (q.isEmpty()) return filteredByCategory

        return filteredByCategory.filter { recipe ->
            recipe.name.lowercase(Locale.US).contains(q) ||
                recipe.subtitle.lowercase(Locale.US).contains(q) ||
                recipe.category.displayName.lowercase(Locale.US).contains(q) ||
                recipe.difficulty.displayName.lowercase(Locale.US).contains(q) ||
                recipe.ingredients.any { it.lowercase(Locale.US).contains(q) } ||
                recipe.tags.any { it.lowercase(Locale.US).contains(q) } ||
                recipe.notes.lowercase(Locale.US).contains(q)
        }.sortedWith(
            compareBy<RecipeItem> { recipe ->
                val lowerName = recipe.name.lowercase(Locale.US)
                val lowerSubtitle = recipe.subtitle.lowercase(Locale.US)
                when {
                    lowerName == q -> 0
                    lowerName.startsWith(q) -> 1
                    lowerName.split(" ").any { it.startsWith(q) } -> 2
                    lowerName.contains(q) -> 3
                    lowerSubtitle.contains(q) -> 4
                    else -> 5
                }
            }.thenBy { it.name }
        )
    }

    fun addRecipeToBook(
        currentSaved: List<RecipeItem>,
        recipe: RecipeItem
    ): List<RecipeItem> {
        if (currentSaved.any { it.id == recipe.id }) return currentSaved
        return currentSaved + recipe
    }

    fun removeRecipeFromBook(
        currentSaved: List<RecipeItem>,
        recipeId: String
    ): List<RecipeItem> {
        return currentSaved.filterNot { it.id == recipeId }
    }

    fun toggleRecipeInBook(
        currentSaved: List<RecipeItem>,
        recipe: RecipeItem
    ): List<RecipeItem> {
        return if (currentSaved.any { it.id == recipe.id }) {
            removeRecipeFromBook(currentSaved, recipe.id)
        } else {
            addRecipeToBook(currentSaved, recipe)
        }
    }

    fun updateSavedRecipe(
        currentSaved: List<RecipeItem>,
        updatedRecipe: RecipeItem
    ): List<RecipeItem> {
        return currentSaved.map { if (it.id == updatedRecipe.id) updatedRecipe else it }
    }

    fun toggleFavoriteRecipe(
        currentSaved: List<RecipeItem>,
        recipeId: String
    ): List<RecipeItem> {
        return currentSaved.map {
            if (it.id == recipeId) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    private val LEADING_QUANTITY_REGEX =
        Regex("""^(\d+\s+\d+/\d+|\d+/\d+|\d+(?:\.\d+)?)(\s+.*)$""")

    /**
     * Scales a leading numeric or fractional quantity in an ingredient line proportionally
     * from [originalServings] to [targetServings].
     */
    fun scaleIngredientLine(
        ingredient: String,
        originalServings: Int,
        targetServings: Int
    ): String {
        val trimmed = ingredient.trim()
        val safeOriginal = originalServings.coerceAtLeast(1)
        val safeTarget = targetServings.coerceAtLeast(1)
        if (safeOriginal == safeTarget) return trimmed

        val match = LEADING_QUANTITY_REGEX.matchEntire(trimmed) ?: return trimmed
        val rawQty = match.groupValues[1]
        val rest = match.groupValues[2]
        val baseValue = parseQuantity(rawQty) ?: return trimmed
        val scaledValue = baseValue * safeTarget.toDouble() / safeOriginal.toDouble()
        return "${formatQuantity(scaledValue)}$rest"
    }

    fun parseQuantity(raw: String): Double? {
        val text = raw.trim()
        if (text.contains(" ")) {
            val parts = text.split(Regex("\\s+"))
            if (parts.size == 2) {
                val whole = parts[0].toDoubleOrNull() ?: return null
                val frac = parseFraction(parts[1]) ?: return null
                return whole + frac
            }
        }
        if (text.contains("/")) {
            return parseFraction(text)
        }
        return text.toDoubleOrNull()
    }

    private fun parseFraction(text: String): Double? {
        val slash = text.split("/")
        if (slash.size != 2) return null
        val num = slash[0].toDoubleOrNull() ?: return null
        val den = slash[1].toDoubleOrNull() ?: return null
        if (den == 0.0) return null
        return num / den
    }

    fun formatQuantity(value: Double): String {
        if (value <= 0.0) return "0"
        val whole = value.toInt()
        val fraction = value - whole
        val commonFractions = listOf(
            0.0 to "",
            (1.0 / 4.0) to "1/4",
            (1.0 / 3.0) to "1/3",
            (1.0 / 2.0) to "1/2",
            (2.0 / 3.0) to "2/3",
            (3.0 / 4.0) to "3/4",
            1.0 to "1"
        )
        val closest = commonFractions.minByOrNull { abs(it.first - fraction) }
        if (closest != null && abs(closest.first - fraction) < 0.06) {
            return when {
                closest.first == 1.0 -> (whole + 1).toString()
                closest.second.isEmpty() -> whole.toString()
                whole == 0 -> closest.second
                else -> "$whole ${closest.second}"
            }
        }
        val rounded = (value * 10.0).roundToInt() / 10.0
        return if (rounded == rounded.toInt().toDouble()) {
            rounded.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", rounded)
        }
    }
}
