package com.example.toolbox.ui.tools

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID

/**
 * Represents the 12 calendar months of the year (1 = January .. 12 = December).
 */
enum class CalendarMonth(
  val number: Int,
  val shortName: String,
  val narrowName: String,
  val fullName: String,
) {
  JANUARY(1, "Jan", "J", "January"),
  FEBRUARY(2, "Feb", "F", "February"),
  MARCH(3, "Mar", "M", "March"),
  APRIL(4, "Apr", "A", "April"),
  MAY(5, "May", "M", "May"),
  JUNE(6, "Jun", "J", "June"),
  JULY(7, "Jul", "J", "July"),
  AUGUST(8, "Aug", "A", "August"),
  SEPTEMBER(9, "Sep", "S", "September"),
  OCTOBER(10, "Oct", "O", "October"),
  NOVEMBER(11, "Nov", "N", "November"),
  DECEMBER(12, "Dec", "D", "December");

  val shortLabel: String
    get() = narrowName

  companion object {
    fun fromNumber(monthNumber: Int): CalendarMonth {
      return entries.firstOrNull { it.number == monthNumber } ?: JANUARY
    }

    fun currentMonthNumber(): Int {
      return Calendar.getInstance().get(Calendar.MONTH) + 1
    }
  }
}

/**
 * Categories of plants in the catalog and schedule.
 */
enum class PlantCategory(
  val displayName: String,
) {
  VEGETABLES("Vegetables"),
  LEAFY_GREENS("Leafy Greens"),
  ROOTS_AND_ALLIUMS("Roots & Alliums"),
  HERBS("Herbs"),
  FRUITS("Fruits & Berries"),
  FLOWERS("Flowers"),
}

/**
 * Represents the planting/harvesting state of a single month for a plant in the year view.
 * - [PLANTING] is highlighted using the active Theme color (`MaterialTheme.colorScheme.primary`).
 * - [HARVESTING] is highlighted using the active Accent color (`MaterialTheme.colorScheme.tertiary`).
 * - [BOTH] is highlighted using both Theme and Accent colors.
 */
enum class MonthActivityState {
  NONE,
  PLANTING,
  HARVESTING,
  BOTH,
}

/**
 * Represents a plant entry in the catalog or user's planting/harvest schedule.
 *
 * @property id Unique identifier for the plant.
 * @property name Common name of the plant (e.g., "Tomato").
 * @property scientificName Botanical/scientific name (e.g., "Solanum lycopersicum").
 * @property category Functional plant category.
 * @property plantingMonths Set of month numbers (1..12) suitable for planting/sowing.
 * @property harvestMonths Set of month numbers (1..12) suitable for harvesting/picking.
 * @property sunRequirement Sun exposure guidance (e.g., "Full Sun", "Partial Shade").
 * @property daysToMaturity Typical time from planting to harvest (e.g., "65–85 days").
 * @property spacing Recommended spacing between plants (e.g., "18–24 in").
 * @property notes Helpful growing and harvesting tips.
 * @property isCustom Whether this plant was created or customized by the user.
 */
data class PlantItem(
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  val scientificName: String = "",
  val category: PlantCategory = PlantCategory.VEGETABLES,
  val plantingMonths: Set<Int>,
  val harvestMonths: Set<Int>,
  val sunRequirement: String = "Full Sun",
  val daysToMaturity: String = "",
  val spacing: String = "",
  val notes: String = "",
  val isCustom: Boolean = false,
) {
  val normalizedPlantingMonths: Set<Int>
    get() = plantingMonths.filter { it in 1..12 }.toSortedSet()

  val normalizedHarvestMonths: Set<Int>
    get() = harvestMonths.filter { it in 1..12 }.toSortedSet()

  fun isPlantingInMonth(month: Int): Boolean = month in normalizedPlantingMonths

  fun isHarvestingInMonth(month: Int): Boolean = month in normalizedHarvestMonths

  fun plantingSummary(): String = PlantScheduleEngine.formatMonthRanges(normalizedPlantingMonths)

  fun harvestSummary(): String = PlantScheduleEngine.formatMonthRanges(normalizedHarvestMonths)

  fun activityForMonth(month: Int): MonthActivityState {
    val planting = isPlantingInMonth(month)
    val harvesting = isHarvestingInMonth(month)
    return when {
      planting && harvesting -> MonthActivityState.BOTH
      planting -> MonthActivityState.PLANTING
      harvesting -> MonthActivityState.HARVESTING
      else -> MonthActivityState.NONE
    }
  }

  fun toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("name", name)
    put("scientificName", scientificName)
    put("category", category.name)
    val plantArr = JSONArray()
    normalizedPlantingMonths.forEach { plantArr.put(it) }
    put("plantingMonths", plantArr)
    val harvestArr = JSONArray()
    normalizedHarvestMonths.forEach { harvestArr.put(it) }
    put("harvestMonths", harvestArr)
    put("sunRequirement", sunRequirement)
    put("daysToMaturity", daysToMaturity)
    put("spacing", spacing)
    put("notes", notes)
    put("isCustom", isCustom)
  }

  companion object {
    fun fromJson(json: JSONObject): PlantItem {
      val plantingSet = mutableSetOf<Int>()
      val plantArr = json.optJSONArray("plantingMonths")
      if (plantArr != null) {
        for (i in 0 until plantArr.length()) {
          val m = plantArr.optInt(i, -1)
          if (m in 1..12) plantingSet.add(m)
        }
      }

      val harvestSet = mutableSetOf<Int>()
      val harvestArr = json.optJSONArray("harvestMonths")
      if (harvestArr != null) {
        for (i in 0 until harvestArr.length()) {
          val m = harvestArr.optInt(i, -1)
          if (m in 1..12) harvestSet.add(m)
        }
      }

      val categoryName = json.optString("category", PlantCategory.VEGETABLES.name)
      val category = try {
        PlantCategory.valueOf(categoryName)
      } catch (_: IllegalArgumentException) {
        PlantCategory.VEGETABLES
      }

      return PlantItem(
        id = json.optString("id", UUID.randomUUID().toString()),
        name = json.optString("name", ""),
        scientificName = json.optString("scientificName", ""),
        category = category,
        plantingMonths = plantingSet,
        harvestMonths = harvestSet,
        sunRequirement = json.optString("sunRequirement", "Full Sun"),
        daysToMaturity = json.optString("daysToMaturity", ""),
        spacing = json.optString("spacing", ""),
        notes = json.optString("notes", ""),
        isCustom = json.optBoolean("isCustom", false),
      )
    }
  }
}

/**
 * Repository interface for persisting scheduled plants and custom catalog entries.
 */
interface PlantScheduleRepository {
  fun loadScheduledPlants(): List<PlantItem>
  fun saveScheduledPlants(plants: List<PlantItem>)
  fun loadCustomPlants(): List<PlantItem>
  fun saveCustomPlants(plants: List<PlantItem>)
}

/**
 * In-memory implementation of [PlantScheduleRepository] for unit tests and Compose previews.
 */
class InMemoryPlantScheduleRepository(
  initialScheduledPlants: List<PlantItem> = emptyList(),
  initialCustomPlants: List<PlantItem> = emptyList(),
) : PlantScheduleRepository {
  private val scheduled = initialScheduledPlants.toMutableList()
  private val custom = initialCustomPlants.toMutableList()

  override fun loadScheduledPlants(): List<PlantItem> = scheduled.toList()

  override fun saveScheduledPlants(plants: List<PlantItem>) {
    scheduled.clear()
    scheduled.addAll(plants)
  }

  override fun loadCustomPlants(): List<PlantItem> = custom.toList()

  override fun saveCustomPlants(plants: List<PlantItem>) {
    custom.clear()
    custom.addAll(plants)
  }
}

/**
 * Android [SharedPreferences]-backed implementation of [PlantScheduleRepository].
 */
class SharedPreferencesPlantScheduleRepository(
  private val sharedPreferences: SharedPreferences,
) : PlantScheduleRepository {

  companion object {
    const val PREFS_NAME = "plant_schedule_preferences"
    const val KEY_SCHEDULED_PLANTS_JSON = "scheduled_plants_json"
    const val KEY_CUSTOM_PLANTS_JSON = "custom_plants_json"
  }

  constructor(context: Context) : this(
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  )

  override fun loadScheduledPlants(): List<PlantItem> {
    val raw = sharedPreferences.getString(KEY_SCHEDULED_PLANTS_JSON, null) ?: return emptyList()
    return parsePlantListJson(raw)
  }

  override fun saveScheduledPlants(plants: List<PlantItem>) {
    val array = JSONArray()
    plants.forEach { array.put(it.toJson()) }
    sharedPreferences.edit().putString(KEY_SCHEDULED_PLANTS_JSON, array.toString()).apply()
  }

  override fun loadCustomPlants(): List<PlantItem> {
    val raw = sharedPreferences.getString(KEY_CUSTOM_PLANTS_JSON, null) ?: return emptyList()
    return parsePlantListJson(raw)
  }

  override fun saveCustomPlants(plants: List<PlantItem>) {
    val array = JSONArray()
    plants.forEach { array.put(it.toJson()) }
    sharedPreferences.edit().putString(KEY_CUSTOM_PLANTS_JSON, array.toString()).apply()
  }

  private fun parsePlantListJson(raw: String): List<PlantItem> {
    return try {
      val array = JSONArray(raw)
      val list = mutableListOf<PlantItem>()
      for (i in 0 until array.length()) {
        val obj = array.optJSONObject(i)
        if (obj != null) {
          val item = PlantItem.fromJson(obj)
          if (item.name.isNotBlank()) {
            list.add(item)
          }
        }
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }
}

/**
 * Engine containing the built-in plant database, search logic, schedule mutations, and formatting helpers.
 */
object PlantScheduleEngine {

  val BUILT_IN_PLANTS: List<PlantItem> = listOf(
    // Vegetables
    PlantItem(
      id = "tomato",
      name = "Tomato",
      scientificName = "Solanum lycopersicum",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "65–85 days",
      spacing = "24–36 in",
      notes = "Start seeds indoors 6–8 weeks before last frost; transplant into warm soil and stake or cage vines.",
    ),
    PlantItem(
      id = "peppers",
      name = "Peppers",
      scientificName = "Capsicum annuum",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "65–85 days",
      spacing = "18–24 in",
      notes = "Start indoors 8 weeks before last frost and transplant into warm, well-drained soil. Support heavy fruit loads with stakes.",
    ),
    PlantItem(
      id = "sweet_peppers",
      name = "Sweet Peppers",
      scientificName = "Capsicum annuum (Grossum Group)",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "65–80 days",
      spacing = "18–24 in",
      notes = "Includes sweet snacking, Marconi, and frying peppers. Allow fruit to fully color on the plant for peak sweetness and vitamin C.",
    ),
    PlantItem(
      id = "bell_pepper",
      name = "Bell Pepper",
      scientificName = "Capsicum annuum",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "70–85 days",
      spacing = "18–24 in",
      notes = "Warm-season crop. Harvest green or leave on the plant to ripen to red, yellow, or orange.",
    ),
    PlantItem(
      id = "banana_pepper",
      name = "Banana Pepper",
      scientificName = "Capsicum annuum 'Banana'",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "60–75 days",
      spacing = "18–24 in",
      notes = "Mild, tangy sweet pepper that produces heavily. Pick when pale yellow and 4–6 inches long.",
    ),
    PlantItem(
      id = "shishito_pepper",
      name = "Shishito Pepper",
      scientificName = "Capsicum annuum 'Shishito'",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "60–70 days",
      spacing = "18–24 in",
      notes = "Thin-walled East Asian sweet pepper ideal for blistering in a skillet; harvest green at 3–4 inches.",
    ),
    PlantItem(
      id = "poblano_pepper",
      name = "Poblano Pepper",
      scientificName = "Capsicum annuum 'Poblano'",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "75–85 days",
      spacing = "18–24 in",
      notes = "Rich, mild heart-shaped chili prized for roasting and stuffing; called ancho when dried.",
    ),
    PlantItem(
      id = "anaheim_pepper",
      name = "Anaheim Pepper",
      scientificName = "Capsicum annuum 'Anaheim'",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "75–80 days",
      spacing = "18–24 in",
      notes = "Mild-to-medium roasting pepper. Harvest firm green pods or let ripen to crimson.",
    ),
    PlantItem(
      id = "jalapeno_pepper",
      name = "Jalapeño Pepper",
      scientificName = "Capsicum annuum 'Jalapeño'",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "70–80 days",
      spacing = "14–18 in",
      notes = "Pick when firm and glossy dark green, or allow to turn red for sweeter heat.",
    ),
    PlantItem(
      id = "serrano_pepper",
      name = "Serrano Pepper",
      scientificName = "Capsicum annuum 'Serrano'",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "75–85 days",
      spacing = "14–18 in",
      notes = "Crisp, bright-flavored hot pepper great for fresh salsas and pickling.",
    ),
    PlantItem(
      id = "cayenne_pepper",
      name = "Cayenne Pepper",
      scientificName = "Capsicum annuum 'Cayenne'",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "70–85 days",
      spacing = "16–24 in",
      notes = "Slender, pungent chilies that dry easily for crushed red pepper flakes or powder.",
    ),
    PlantItem(
      id = "habanero_pepper",
      name = "Habanero Pepper",
      scientificName = "Capsicum chinense",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "90–100 days",
      spacing = "18–24 in",
      notes = "Fruity, citrus-scented chili with intense heat. Prefers long, warm summers.",
    ),
    PlantItem(
      id = "thai_chili_pepper",
      name = "Thai Chili Pepper",
      scientificName = "Capsicum frutescens",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "75–90 days",
      spacing = "14–18 in",
      notes = "Compact plants bearing upright clusters of small, fiery bird's eye chilies.",
    ),
    PlantItem(
      id = "cucumber",
      name = "Cucumber",
      scientificName = "Cucumis sativus",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(4, 5, 6),
      harvestMonths = setOf(7, 8, 9),
      sunRequirement = "Full Sun",
      daysToMaturity = "50–70 days",
      spacing = "12–36 in",
      notes = "Direct sow after frost danger passes. Frequent harvesting encourages continuous fruiting.",
    ),
    PlantItem(
      id = "zucchini",
      name = "Zucchini",
      scientificName = "Cucurbita pepo",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(4, 5, 6),
      harvestMonths = setOf(6, 7, 8, 9),
      sunRequirement = "Full Sun",
      daysToMaturity = "45–60 days",
      spacing = "24–36 in",
      notes = "Prolific summer squash. Harvest young at 6–8 inches for the most tender texture.",
    ),
    PlantItem(
      id = "pumpkin",
      name = "Pumpkin",
      scientificName = "Cucurbita maxima",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(5, 6),
      harvestMonths = setOf(9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "90–120 days",
      spacing = "36–60 in",
      notes = "Plant in late spring for autumn harvest. Cure in a warm, dry spot before storage.",
    ),
    PlantItem(
      id = "butternut_squash",
      name = "Butternut Squash",
      scientificName = "Cucurbita moschata",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(5, 6),
      harvestMonths = setOf(9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "85–110 days",
      spacing = "36–48 in",
      notes = "Harvest when rind is hard and tan before heavy frost; stores well for months.",
    ),
    PlantItem(
      id = "green_beans",
      name = "Green Beans",
      scientificName = "Phaseolus vulgaris",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(4, 5, 6, 7),
      harvestMonths = setOf(6, 7, 8, 9),
      sunRequirement = "Full Sun",
      daysToMaturity = "50–65 days",
      spacing = "4–6 in",
      notes = "Sow every 2–3 weeks for a continuous summer harvest. Fixes nitrogen in garden soil.",
    ),
    PlantItem(
      id = "snap_peas",
      name = "Sugar Snap Peas",
      scientificName = "Pisum sativum",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(2, 3, 4, 8),
      harvestMonths = setOf(5, 6, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "55–70 days",
      spacing = "2–4 in",
      notes = "Cool-weather favorite. Plant in early spring and again in late summer for a fall crop.",
    ),
    PlantItem(
      id = "sweet_corn",
      name = "Sweet Corn",
      scientificName = "Zea mays",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(4, 5, 6),
      harvestMonths = setOf(7, 8, 9),
      sunRequirement = "Full Sun",
      daysToMaturity = "65–90 days",
      spacing = "8–12 in",
      notes = "Plant in blocks of at least 4 rows for wind pollination. Harvest when silks brown and kernels are milky.",
    ),
    PlantItem(
      id = "eggplant",
      name = "Eggplant",
      scientificName = "Solanum melongena",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(4, 5),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "65–85 days",
      spacing = "18–24 in",
      notes = "Thrives in heat. Harvest when skin is glossy and springs back slightly when pressed.",
    ),
    PlantItem(
      id = "broccoli",
      name = "Broccoli",
      scientificName = "Brassica oleracea var. italica",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4, 7, 8),
      harvestMonths = setOf(5, 6, 9, 10, 11),
      sunRequirement = "Full Sun",
      daysToMaturity = "60–80 days",
      spacing = "18–24 in",
      notes = "Cut central head while buds are tight; side shoots will continue producing for weeks.",
    ),
    PlantItem(
      id = "cauliflower",
      name = "Cauliflower",
      scientificName = "Brassica oleracea var. botrytis",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4, 7, 8),
      harvestMonths = setOf(5, 6, 9, 10, 11),
      sunRequirement = "Full Sun",
      daysToMaturity = "65–85 days",
      spacing = "18–24 in",
      notes = "Best in cool spring and autumn weather. Blanch white varieties by tying outer leaves over the head.",
    ),
    PlantItem(
      id = "cabbage",
      name = "Cabbage",
      scientificName = "Brassica oleracea var. capitata",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(2, 3, 4, 7, 8),
      harvestMonths = setOf(5, 6, 7, 10, 11),
      sunRequirement = "Full Sun",
      daysToMaturity = "70–100 days",
      spacing = "12–18 in",
      notes = "Harvest when heads feel solid and firm.",
    ),
    PlantItem(
      id = "asparagus",
      name = "Asparagus",
      scientificName = "Asparagus officinalis",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4),
      harvestMonths = setOf(4, 5, 6),
      sunRequirement = "Full Sun",
      daysToMaturity = "2–3 years (crowns)",
      spacing = "12–18 in",
      notes = "Perennial crop. Snap spears at soil level in spring when 6–8 inches tall.",
    ),
    PlantItem(
      id = "cherry_tomato",
      name = "Cherry Tomato",
      scientificName = "Solanum lycopersicum var. cerasiforme",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(6, 7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "55–70 days",
      spacing = "24–36 in",
      notes = "Vigorous, bite-sized tomatoes that ripen earlier and fruit longer than slicing varieties.",
    ),
    PlantItem(
      id = "tomatillo",
      name = "Tomatillo",
      scientificName = "Physalis philadelphica",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(4, 5),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "65–75 days",
      spacing = "24–36 in",
      notes = "Plant at least two for cross-pollination. Harvest when fruit fills and splits the papery husk.",
    ),
    PlantItem(
      id = "okra",
      name = "Okra",
      scientificName = "Abelmoschus esculentus",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(5, 6),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "50–65 days",
      spacing = "12–18 in",
      notes = "Heat-loving southern staple. Pick pods every 2 days while 2–4 inches long and tender.",
    ),
    PlantItem(
      id = "brussels_sprouts",
      name = "Brussels Sprouts",
      scientificName = "Brassica oleracea var. gemmifera",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(4, 5, 6),
      harvestMonths = setOf(9, 10, 11, 12),
      sunRequirement = "Full Sun",
      daysToMaturity = "90–110 days",
      spacing = "18–24 in",
      notes = "Flavor sweetens dramatically after autumn frosts. Snap sprouts from the bottom of the stalk upward.",
    ),
    PlantItem(
      id = "celery",
      name = "Celery",
      scientificName = "Apium graveolens",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "85–110 days",
      spacing = "8–10 in",
      notes = "Requires rich soil and consistent moisture. Cut outer stalks as needed or harvest the whole head.",
    ),
    PlantItem(
      id = "artichoke",
      name = "Globe Artichoke",
      scientificName = "Cynara cardunculus var. scolymus",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4),
      harvestMonths = setOf(6, 7, 8),
      sunRequirement = "Full Sun",
      daysToMaturity = "85–100 days",
      spacing = "36–48 in",
      notes = "Architectural thistle-like plant. Harvest plump, tight flower buds before scales begin to open.",
    ),
    PlantItem(
      id = "edamame",
      name = "Edamame (Soybean)",
      scientificName = "Glycine max",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(5, 6),
      harvestMonths = setOf(8, 9),
      sunRequirement = "Full Sun",
      daysToMaturity = "75–90 days",
      spacing = "4–6 in",
      notes = "Pick pods when bright green and plump, before they begin to turn yellow.",
    ),
    PlantItem(
      id = "lima_beans",
      name = "Lima Beans",
      scientificName = "Phaseolus lunatus",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(5, 6),
      harvestMonths = setOf(8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "65–85 days",
      spacing = "4–6 in",
      notes = "Sow in thoroughly warmed soil. Shell fresh when pods feel plump and spongy.",
    ),
    PlantItem(
      id = "yellow_squash",
      name = "Yellow Summer Squash",
      scientificName = "Cucurbita pepo",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(4, 5, 6),
      harvestMonths = setOf(6, 7, 8, 9),
      sunRequirement = "Full Sun",
      daysToMaturity = "45–55 days",
      spacing = "24–36 in",
      notes = "Crookneck and straightneck varieties; harvest small with glossy, tender skin.",
    ),
    PlantItem(
      id = "acorn_squash",
      name = "Acorn Squash",
      scientificName = "Cucurbita pepo var. turbinata",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(5, 6),
      harvestMonths = setOf(9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "80–95 days",
      spacing = "36–48 in",
      notes = "Ribbed winter squash with sweet, nutty flesh. Ready when the ground spot turns deep orange.",
    ),
    PlantItem(
      id = "spaghetti_squash",
      name = "Spaghetti Squash",
      scientificName = "Cucurbita pepo subsp. pepo",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(5, 6),
      harvestMonths = setOf(8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "85–100 days",
      spacing = "36–48 in",
      notes = "Harvest when rind turns golden yellow and resists thumbnail pressure.",
    ),
    PlantItem(
      id = "kohlrabi",
      name = "Kohlrabi",
      scientificName = "Brassica oleracea var. gongylodes",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4, 7, 8),
      harvestMonths = setOf(5, 6, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "45–60 days",
      spacing = "4–6 in",
      notes = "Crisp, apple-sweet swollen stem. Harvest when bulbs are 2–3 inches across.",
    ),
    PlantItem(
      id = "fennel",
      name = "Florence Fennel",
      scientificName = "Foeniculum vulgare var. azoricum",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(4, 5, 7, 8),
      harvestMonths = setOf(6, 7, 9, 10, 11),
      sunRequirement = "Full Sun",
      daysToMaturity = "65–85 days",
      spacing = "8–12 in",
      notes = "Grown for its sweet, anise-scented bulb and feathery fronds. Keep soil evenly moist.",
    ),

    // Leafy Greens
    PlantItem(
      id = "lettuce",
      name = "Lettuce",
      scientificName = "Lactuca sativa",
      category = PlantCategory.LEAFY_GREENS,
      plantingMonths = setOf(3, 4, 5, 8, 9),
      harvestMonths = setOf(4, 5, 6, 9, 10, 11),
      sunRequirement = "Partial Shade",
      daysToMaturity = "30–60 days",
      spacing = "6–12 in",
      notes = "Fast-growing cool-season green. Harvest outer leaves continuously or cut whole heads.",
    ),
    PlantItem(
      id = "spinach",
      name = "Spinach",
      scientificName = "Spinacia oleracea",
      category = PlantCategory.LEAFY_GREENS,
      plantingMonths = setOf(2, 3, 4, 8, 9, 10),
      harvestMonths = setOf(4, 5, 6, 9, 10, 11),
      sunRequirement = "Partial Shade",
      daysToMaturity = "35–50 days",
      spacing = "3–6 in",
      notes = "Cold-hardy leafy green. Sow in early spring and again in late summer for autumn/winter harvest.",
    ),
    PlantItem(
      id = "kale",
      name = "Kale",
      scientificName = "Brassica oleracea var. sabellica",
      category = PlantCategory.LEAFY_GREENS,
      plantingMonths = setOf(3, 4, 7, 8, 9),
      harvestMonths = setOf(5, 6, 7, 9, 10, 11, 12),
      sunRequirement = "Full Sun",
      daysToMaturity = "50–65 days",
      spacing = "12–18 in",
      notes = "Leaves become sweeter after light autumn frosts. Pick lower leaves first.",
    ),
    PlantItem(
      id = "swiss_chard",
      name = "Swiss Chard",
      scientificName = "Beta vulgaris subsp. cicla",
      category = PlantCategory.LEAFY_GREENS,
      plantingMonths = setOf(3, 4, 5, 7, 8),
      harvestMonths = setOf(5, 6, 7, 8, 9, 10, 11),
      sunRequirement = "Full Sun",
      daysToMaturity = "50–60 days",
      spacing = "8–12 in",
      notes = "Tolerates both light frost and summer warmth. Cut outer stalks 1 inch above the crown.",
    ),
    PlantItem(
      id = "arugula",
      name = "Arugula",
      scientificName = "Eruca vesicaria",
      category = PlantCategory.LEAFY_GREENS,
      plantingMonths = setOf(3, 4, 5, 8, 9, 10),
      harvestMonths = setOf(4, 5, 6, 9, 10, 11),
      sunRequirement = "Partial Shade",
      daysToMaturity = "25–40 days",
      spacing = "4–6 in",
      notes = "Peppery salad green that germinates quickly in cool soil.",
    ),
    PlantItem(
      id = "bok_choy",
      name = "Bok Choy",
      scientificName = "Brassica rapa subsp. chinensis",
      category = PlantCategory.LEAFY_GREENS,
      plantingMonths = setOf(3, 4, 8, 9),
      harvestMonths = setOf(5, 6, 9, 10, 11),
      sunRequirement = "Partial Shade",
      daysToMaturity = "45–60 days",
      spacing = "6–10 in",
      notes = "Crisp stalks and tender leaves for stir-fries. Best planted in cool spring or late summer.",
    ),
    PlantItem(
      id = "collard_greens",
      name = "Collard Greens",
      scientificName = "Brassica oleracea var. viridis",
      category = PlantCategory.LEAFY_GREENS,
      plantingMonths = setOf(3, 4, 7, 8),
      harvestMonths = setOf(5, 6, 7, 9, 10, 11, 12),
      sunRequirement = "Full Sun",
      daysToMaturity = "60–75 days",
      spacing = "18–24 in",
      notes = "Tolerates both summer heat and winter frosts. Harvest broad lower leaves as the plant grows upward.",
    ),
    PlantItem(
      id = "mustard_greens",
      name = "Mustard Greens",
      scientificName = "Brassica juncea",
      category = PlantCategory.LEAFY_GREENS,
      plantingMonths = setOf(3, 4, 8, 9, 10),
      harvestMonths = setOf(4, 5, 6, 9, 10, 11),
      sunRequirement = "Full Sun",
      daysToMaturity = "30–45 days",
      spacing = "6–10 in",
      notes = "Zesty, fast-growing greens. Pick baby leaves for salads or larger leaves for braising.",
    ),
    PlantItem(
      id = "watercress",
      name = "Watercress",
      scientificName = "Nasturtium officinale",
      category = PlantCategory.LEAFY_GREENS,
      plantingMonths = setOf(3, 4, 5, 8, 9),
      harvestMonths = setOf(4, 5, 6, 9, 10, 11),
      sunRequirement = "Partial Shade",
      daysToMaturity = "40–50 days",
      spacing = "6–8 in",
      notes = "Thrives in consistently damp, rich soil or containers set in shallow water trays.",
    ),
    PlantItem(
      id = "endive",
      name = "Endive & Escarole",
      scientificName = "Cichorium endivia",
      category = PlantCategory.LEAFY_GREENS,
      plantingMonths = setOf(3, 4, 7, 8),
      harvestMonths = setOf(5, 6, 9, 10, 11),
      sunRequirement = "Full Sun",
      daysToMaturity = "60–85 days",
      spacing = "8–12 in",
      notes = "Frilly or broad-leaved chicory greens with a pleasant bitter-sweet crunch.",
    ),
    PlantItem(
      id = "radicchio",
      name = "Radicchio",
      scientificName = "Cichorium intybus",
      category = PlantCategory.LEAFY_GREENS,
      plantingMonths = setOf(3, 4, 7, 8),
      harvestMonths = setOf(6, 9, 10, 11),
      sunRequirement = "Full Sun",
      daysToMaturity = "65–85 days",
      spacing = "8–10 in",
      notes = "Cool night temperatures deepen its burgundy color and mellow its bold flavor.",
    ),
    PlantItem(
      id = "tatsoi",
      name = "Tatsoi",
      scientificName = "Brassica rapa var. rosularis",
      category = PlantCategory.LEAFY_GREENS,
      plantingMonths = setOf(3, 4, 8, 9, 10),
      harvestMonths = setOf(4, 5, 6, 9, 10, 11, 12),
      sunRequirement = "Partial Shade",
      daysToMaturity = "40–50 days",
      spacing = "6–8 in",
      notes = "Forms compact rosettes of glossy, spoon-shaped leaves that withstand cold weather.",
    ),

    // Roots & Alliums
    PlantItem(
      id = "carrot",
      name = "Carrot",
      scientificName = "Daucus carota",
      category = PlantCategory.ROOTS_AND_ALLIUMS,
      plantingMonths = setOf(3, 4, 5, 7, 8),
      harvestMonths = setOf(6, 7, 8, 9, 10, 11),
      sunRequirement = "Full Sun",
      daysToMaturity = "60–80 days",
      spacing = "2–3 in",
      notes = "Direct sow in loose, stone-free soil. Keep surface evenly moist during germination.",
    ),
    PlantItem(
      id = "radish",
      name = "Radish",
      scientificName = "Raphanus sativus",
      category = PlantCategory.ROOTS_AND_ALLIUMS,
      plantingMonths = setOf(3, 4, 5, 8, 9, 10),
      harvestMonths = setOf(4, 5, 6, 9, 10, 11),
      sunRequirement = "Full Sun",
      daysToMaturity = "22–30 days",
      spacing = "1–2 in",
      notes = "One of the fastest garden crops. Harvest promptly when roots reach 1 inch across.",
    ),
    PlantItem(
      id = "potato",
      name = "Potato",
      scientificName = "Solanum tuberosum",
      category = PlantCategory.ROOTS_AND_ALLIUMS,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "75–110 days",
      spacing = "12 in",
      notes = "Mound soil around stems as plants grow to protect tubers from sunlight.",
    ),
    PlantItem(
      id = "sweet_potato",
      name = "Sweet Potato",
      scientificName = "Ipomoea batatas",
      category = PlantCategory.ROOTS_AND_ALLIUMS,
      plantingMonths = setOf(5, 6),
      harvestMonths = setOf(9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "90–120 days",
      spacing = "12–18 in",
      notes = "Plant slips in warm soil. Dig roots before soil temperatures drop below 55°F.",
    ),
    PlantItem(
      id = "beet",
      name = "Beet",
      scientificName = "Beta vulgaris",
      category = PlantCategory.ROOTS_AND_ALLIUMS,
      plantingMonths = setOf(3, 4, 5, 7, 8),
      harvestMonths = setOf(5, 6, 7, 9, 10, 11),
      sunRequirement = "Full Sun",
      daysToMaturity = "50–70 days",
      spacing = "3–4 in",
      notes = "Both the sweet earthy roots and nutrient-rich greens are edible.",
    ),
    PlantItem(
      id = "onion",
      name = "Onion",
      scientificName = "Allium cepa",
      category = PlantCategory.ROOTS_AND_ALLIUMS,
      plantingMonths = setOf(3, 4),
      harvestMonths = setOf(7, 8, 9),
      sunRequirement = "Full Sun",
      daysToMaturity = "90–110 days",
      spacing = "4–6 in",
      notes = "Harvest when tops yellow and fall over; cure in a dry, shaded area before storing.",
    ),
    PlantItem(
      id = "garlic",
      name = "Garlic",
      scientificName = "Allium sativum",
      category = PlantCategory.ROOTS_AND_ALLIUMS,
      plantingMonths = setOf(10, 11),
      harvestMonths = setOf(6, 7),
      sunRequirement = "Full Sun",
      daysToMaturity = "240–270 days",
      spacing = "4–6 in",
      notes = "Plant individual cloves pointed-end up in autumn; harvest in mid-summer when lower leaves brown.",
    ),
    PlantItem(
      id = "scallion",
      name = "Green Onion (Scallion)",
      scientificName = "Allium fistulosum",
      category = PlantCategory.ROOTS_AND_ALLIUMS,
      plantingMonths = setOf(3, 4, 5, 6, 7, 8),
      harvestMonths = setOf(5, 6, 7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "50–65 days",
      spacing = "1–2 in",
      notes = "Bunching onions that do not form large bulbs; sow in succession for fresh stems all season.",
    ),
    PlantItem(
      id = "shallot",
      name = "Shallot",
      scientificName = "Allium cepa var. aggregatum",
      category = PlantCategory.ROOTS_AND_ALLIUMS,
      plantingMonths = setOf(3, 4, 10),
      harvestMonths = setOf(7, 8),
      sunRequirement = "Full Sun",
      daysToMaturity = "90–120 days",
      spacing = "6–8 in",
      notes = "Each planted bulb multiplies into a cluster of gourmet, mild-flavored shallots.",
    ),
    PlantItem(
      id = "leek",
      name = "Leek",
      scientificName = "Allium porrum",
      category = PlantCategory.ROOTS_AND_ALLIUMS,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(8, 9, 10, 11, 12),
      sunRequirement = "Full Sun",
      daysToMaturity = "100–120 days",
      spacing = "6 in",
      notes = "Hill soil around shafts as they grow to blanch the tender white stems.",
    ),
    PlantItem(
      id = "turnip",
      name = "Turnip",
      scientificName = "Brassica rapa subsp. rapa",
      category = PlantCategory.ROOTS_AND_ALLIUMS,
      plantingMonths = setOf(3, 4, 8, 9),
      harvestMonths = setOf(5, 6, 10, 11),
      sunRequirement = "Full Sun",
      daysToMaturity = "45–60 days",
      spacing = "3–4 in",
      notes = "Dual-purpose crop harvested for both tender young roots and flavorful greens.",
    ),
    PlantItem(
      id = "parsnip",
      name = "Parsnip",
      scientificName = "Pastinaca sativa",
      category = PlantCategory.ROOTS_AND_ALLIUMS,
      plantingMonths = setOf(4, 5),
      harvestMonths = setOf(10, 11, 12),
      sunRequirement = "Full Sun",
      daysToMaturity = "105–130 days",
      spacing = "3–4 in",
      notes = "Leave roots in the ground through autumn frosts to convert starches into rich, nutty sugars.",
    ),
    PlantItem(
      id = "rutabaga",
      name = "Rutabaga",
      scientificName = "Brassica napus var. napobrassica",
      category = PlantCategory.ROOTS_AND_ALLIUMS,
      plantingMonths = setOf(5, 6, 7),
      harvestMonths = setOf(9, 10, 11),
      sunRequirement = "Full Sun",
      daysToMaturity = "85–100 days",
      spacing = "6–8 in",
      notes = "Hardy autumn root vegetable with buttery yellow flesh that stores well through winter.",
    ),
    PlantItem(
      id = "daikon_radish",
      name = "Daikon Radish",
      scientificName = "Raphanus sativus var. longipinnatus",
      category = PlantCategory.ROOTS_AND_ALLIUMS,
      plantingMonths = setOf(7, 8, 9),
      harvestMonths = setOf(9, 10, 11),
      sunRequirement = "Full Sun",
      daysToMaturity = "50–70 days",
      spacing = "4–6 in",
      notes = "Crisp, mild winter radish with deep roots that naturally loosen compacted garden soil.",
    ),
    PlantItem(
      id = "ginger",
      name = "Ginger",
      scientificName = "Zingiber officinale",
      category = PlantCategory.ROOTS_AND_ALLIUMS,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(9, 10, 11),
      sunRequirement = "Partial Shade",
      daysToMaturity = "240–300 days",
      spacing = "8–12 in",
      notes = "Sprout plump rhizomes indoors in spring; harvest tender 'baby ginger' in autumn.",
    ),

    // Herbs
    PlantItem(
      id = "basil",
      name = "Basil",
      scientificName = "Ocimum basilicum",
      category = PlantCategory.HERBS,
      plantingMonths = setOf(4, 5, 6),
      harvestMonths = setOf(6, 7, 8, 9),
      sunRequirement = "Full Sun",
      daysToMaturity = "50–65 days",
      spacing = "10–12 in",
      notes = "Pinch stem tips regularly to encourage bushy growth and prevent early flowering.",
    ),
    PlantItem(
      id = "cilantro",
      name = "Cilantro",
      scientificName = "Coriandrum sativum",
      category = PlantCategory.HERBS,
      plantingMonths = setOf(3, 4, 5, 8, 9),
      harvestMonths = setOf(4, 5, 6, 9, 10, 11),
      sunRequirement = "Partial Shade",
      daysToMaturity = "45–70 days",
      spacing = "6–8 in",
      notes = "Prefers cool temperatures. Let some plants bolt in summer to harvest coriander seeds.",
    ),
    PlantItem(
      id = "parsley",
      name = "Parsley",
      scientificName = "Petroselinum crispum",
      category = PlantCategory.HERBS,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(5, 6, 7, 8, 9, 10, 11),
      sunRequirement = "Full Sun",
      daysToMaturity = "70–90 days",
      spacing = "8–10 in",
      notes = "Soak seeds overnight before planting to speed germination. Cut outer stems at the base.",
    ),
    PlantItem(
      id = "rosemary",
      name = "Rosemary",
      scientificName = "Salvia rosmarinus",
      category = PlantCategory.HERBS,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
      sunRequirement = "Full Sun",
      daysToMaturity = "80–100 days",
      spacing = "18–24 in",
      notes = "Evergreen Mediterranean perennial. Requires well-drained soil and plenty of sunlight.",
    ),
    PlantItem(
      id = "mint",
      name = "Mint",
      scientificName = "Mentha spicata",
      category = PlantCategory.HERBS,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(5, 6, 7, 8, 9, 10),
      sunRequirement = "Partial Shade",
      daysToMaturity = "60–90 days",
      spacing = "12–18 in",
      notes = "Grow in containers to contain vigorous runners. Harvest sprigs anytime before flowering.",
    ),
    PlantItem(
      id = "thyme",
      name = "Thyme",
      scientificName = "Thymus vulgaris",
      category = PlantCategory.HERBS,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(5, 6, 7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "70–90 days",
      spacing = "10–12 in",
      notes = "Drought-tolerant perennial herb. Harvest sprigs in the morning before flowers open.",
    ),
    PlantItem(
      id = "dill",
      name = "Dill",
      scientificName = "Anethum graveolens",
      category = PlantCategory.HERBS,
      plantingMonths = setOf(4, 5, 6, 7),
      harvestMonths = setOf(6, 7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "40–60 days",
      spacing = "10–12 in",
      notes = "Direct sow where it will grow. Great companion plant for cucumbers and cabbages.",
    ),
    PlantItem(
      id = "chives",
      name = "Chives",
      scientificName = "Allium schoenoprasum",
      category = PlantCategory.HERBS,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(4, 5, 6, 7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "60–80 days",
      spacing = "8–12 in",
      notes = "Hardy perennial with edible purple blossoms that attract pollinators.",
    ),
    PlantItem(
      id = "oregano",
      name = "Oregano",
      scientificName = "Origanum vulgare",
      category = PlantCategory.HERBS,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(5, 6, 7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "60–80 days",
      spacing = "12–18 in",
      notes = "Aromatic Mediterranean perennial. Leaves have the most intense flavor right before flower buds open.",
    ),
    PlantItem(
      id = "sage",
      name = "Sage",
      scientificName = "Salvia officinalis",
      category = PlantCategory.HERBS,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(5, 6, 7, 8, 9, 10, 11),
      sunRequirement = "Full Sun",
      daysToMaturity = "75–90 days",
      spacing = "18–24 in",
      notes = "Velvety gray-green leaves with savory warmth. Prune woody stems in spring to refresh growth.",
    ),
    PlantItem(
      id = "lavender",
      name = "Lavender",
      scientificName = "Lavandula angustifolia",
      category = PlantCategory.HERBS,
      plantingMonths = setOf(4, 5),
      harvestMonths = setOf(6, 7, 8),
      sunRequirement = "Full Sun",
      daysToMaturity = "90–120 days",
      spacing = "18–24 in",
      notes = "Thrives in sunny, sandy, alkaline soil. Cut flower spikes when half the buds have opened.",
    ),
    PlantItem(
      id = "chamomile",
      name = "Chamomile",
      scientificName = "Matricaria chamomilla",
      category = PlantCategory.HERBS,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(6, 7, 8),
      sunRequirement = "Full Sun",
      daysToMaturity = "60–70 days",
      spacing = "6–8 in",
      notes = "Daisy-like blossoms with a sweet apple scent; harvest flowers fully open for soothing herbal tea.",
    ),
    PlantItem(
      id = "lemongrass",
      name = "Lemongrass",
      scientificName = "Cymbopogon citratus",
      category = PlantCategory.HERBS,
      plantingMonths = setOf(4, 5, 6),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "75–100 days",
      spacing = "18–24 in",
      notes = "Tropical clumping grass with bright citrus aroma. Harvest plump stalks at ground level.",
    ),
    PlantItem(
      id = "tarragon",
      name = "French Tarragon",
      scientificName = "Artemisia dracunculus",
      category = PlantCategory.HERBS,
      plantingMonths = setOf(4, 5),
      harvestMonths = setOf(6, 7, 8, 9),
      sunRequirement = "Full Sun",
      daysToMaturity = "60–75 days",
      spacing = "18–24 in",
      notes = "Propagated from cuttings or divisions; prized in classic culinary sauces for its subtle anise flavor.",
    ),
    PlantItem(
      id = "lemon_balm",
      name = "Lemon Balm",
      scientificName = "Melissa officinalis",
      category = PlantCategory.HERBS,
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(5, 6, 7, 8, 9, 10),
      sunRequirement = "Partial Shade",
      daysToMaturity = "60–70 days",
      spacing = "12–18 in",
      notes = "Bright lemon-scented leaves for teas and salads; shear back mid-summer for fresh foliage.",
    ),
    PlantItem(
      id = "marjoram",
      name = "Sweet Marjoram",
      scientificName = "Origanum majorana",
      category = PlantCategory.HERBS,
      plantingMonths = setOf(4, 5),
      harvestMonths = setOf(6, 7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "60–75 days",
      spacing = "8–12 in",
      notes = "Sweeter and more delicate than oregano; harvest tender shoot tips throughout summer.",
    ),

    // Fruits & Berries
    PlantItem(
      id = "strawberry",
      name = "Strawberry",
      scientificName = "Fragaria × ananassa",
      category = PlantCategory.FRUITS,
      plantingMonths = setOf(3, 4, 9),
      harvestMonths = setOf(5, 6, 7),
      sunRequirement = "Full Sun",
      daysToMaturity = "60–90 days",
      spacing = "12–18 in",
      notes = "Mulch with straw to keep berries clean and retain moisture.",
    ),
    PlantItem(
      id = "watermelon",
      name = "Watermelon",
      scientificName = "Citrullus lanatus",
      category = PlantCategory.FRUITS,
      plantingMonths = setOf(4, 5, 6),
      harvestMonths = setOf(7, 8, 9),
      sunRequirement = "Full Sun",
      daysToMaturity = "75–95 days",
      spacing = "36–60 in",
      notes = "Ripe when the underside spot turns creamy yellow and the tendril nearest the fruit dries.",
    ),
    PlantItem(
      id = "cantaloupe",
      name = "Cantaloupe",
      scientificName = "Cucumis melo",
      category = PlantCategory.FRUITS,
      plantingMonths = setOf(4, 5, 6),
      harvestMonths = setOf(7, 8, 9),
      sunRequirement = "Full Sun",
      daysToMaturity = "75–90 days",
      spacing = "24–36 in",
      notes = "Ready to harvest when the rind netting turns tan, smells fragrant, and slips easily from the stem.",
    ),
    PlantItem(
      id = "blueberry",
      name = "Blueberry",
      scientificName = "Vaccinium corymbosum",
      category = PlantCategory.FRUITS,
      plantingMonths = setOf(3, 4, 10),
      harvestMonths = setOf(6, 7, 8),
      sunRequirement = "Full Sun",
      daysToMaturity = "1–2 years (bushes)",
      spacing = "48–60 in",
      notes = "Prefers acidic, well-drained soil (pH 4.5–5.5). Plant two varieties for best cross-pollination.",
    ),
    PlantItem(
      id = "raspberry",
      name = "Raspberry",
      scientificName = "Rubus idaeus",
      category = PlantCategory.FRUITS,
      plantingMonths = setOf(3, 4, 10),
      harvestMonths = setOf(6, 7, 8, 9),
      sunRequirement = "Full Sun",
      daysToMaturity = "1–2 years (canes)",
      spacing = "24–36 in",
      notes = "Summer-bearing and everbearing canes produce sweet berries that slip off the core when ripe.",
    ),
    PlantItem(
      id = "blackberry",
      name = "Blackberry",
      scientificName = "Rubus fruticosus",
      category = PlantCategory.FRUITS,
      plantingMonths = setOf(3, 4, 10),
      harvestMonths = setOf(7, 8, 9),
      sunRequirement = "Full Sun",
      daysToMaturity = "1–2 years (canes)",
      spacing = "36–48 in",
      notes = "Trellis canes for easy picking. Berries are sweetest when fully black with a dull sheen.",
    ),
    PlantItem(
      id = "honeydew_melon",
      name = "Honeydew Melon",
      scientificName = "Cucumis melo (Inodorus Group)",
      category = PlantCategory.FRUITS,
      plantingMonths = setOf(4, 5, 6),
      harvestMonths = setOf(8, 9),
      sunRequirement = "Full Sun",
      daysToMaturity = "80–100 days",
      spacing = "24–36 in",
      notes = "Smooth-skinned melon; ripe when rind turns creamy yellow and blossom end yields slightly.",
    ),
    PlantItem(
      id = "rhubarb",
      name = "Rhubarb",
      scientificName = "Rheum rhabarbarum",
      category = PlantCategory.FRUITS,
      plantingMonths = setOf(3, 4, 10),
      harvestMonths = setOf(4, 5, 6),
      sunRequirement = "Full Sun",
      daysToMaturity = "1–2 years (crowns)",
      spacing = "36–48 in",
      notes = "Long-lived perennial. Harvest tart crimson stalks by twisting and pulling; discard leaves.",
    ),
    PlantItem(
      id = "ground_cherry",
      name = "Ground Cherry",
      scientificName = "Physalis pruinosa",
      category = PlantCategory.FRUITS,
      plantingMonths = setOf(4, 5),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "65–75 days",
      spacing = "24 in",
      notes = "Golden berries enclosed in papery husks drop to the ground when ripe with pineapple-vanilla sweetness.",
    ),
    PlantItem(
      id = "fig",
      name = "Fig",
      scientificName = "Ficus carica",
      category = PlantCategory.FRUITS,
      plantingMonths = setOf(3, 4, 10),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "1–2 years (trees)",
      spacing = "72–120 in",
      notes = "Thrives in warm spots or large patio containers. Pick when fruit droops on the stem and feels soft.",
    ),
    PlantItem(
      id = "grape",
      name = "Grape",
      scientificName = "Vitis vinifera",
      category = PlantCategory.FRUITS,
      plantingMonths = setOf(3, 4),
      harvestMonths = setOf(8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "2–3 years (vines)",
      spacing = "72–96 in",
      notes = "Train vines along a sturdy sunny arbor or wire trellis; prune annually while dormant in late winter.",
    ),

    // Flowers
    PlantItem(
      id = "sunflower",
      name = "Sunflower",
      scientificName = "Helianthus annuus",
      category = PlantCategory.FLOWERS,
      plantingMonths = setOf(4, 5, 6),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "60–90 days",
      spacing = "12–24 in",
      notes = "Direct sow in sunny locations. Cut blooms when petals unfurl or leave heads to ripen seeds.",
    ),
    PlantItem(
      id = "marigold",
      name = "Marigold",
      scientificName = "Tagetes patula",
      category = PlantCategory.FLOWERS,
      plantingMonths = setOf(4, 5, 6),
      harvestMonths = setOf(6, 7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "45–60 days",
      spacing = "8–12 in",
      notes = "Classic vegetable garden companion that deters pests and blooms until frost.",
    ),
    PlantItem(
      id = "zinnia",
      name = "Zinnia",
      scientificName = "Zinnia elegans",
      category = PlantCategory.FLOWERS,
      plantingMonths = setOf(4, 5, 6),
      harvestMonths = setOf(6, 7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "55–70 days",
      spacing = "8–12 in",
      notes = "Vibrant cut flower; the more stems you cut, the more blooms the plant produces.",
    ),
    PlantItem(
      id = "nasturtium",
      name = "Nasturtium",
      scientificName = "Tropaeolum majus",
      category = PlantCategory.FLOWERS,
      plantingMonths = setOf(4, 5, 6),
      harvestMonths = setOf(6, 7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "50–65 days",
      spacing = "10–12 in",
      notes = "Both the jewel-toned flowers and round leaves are edible with a bright peppery flavor.",
    ),
    PlantItem(
      id = "cosmos",
      name = "Cosmos",
      scientificName = "Cosmos bipinnatus",
      category = PlantCategory.FLOWERS,
      plantingMonths = setOf(4, 5, 6),
      harvestMonths = setOf(6, 7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "60–75 days",
      spacing = "12–18 in",
      notes = "Airy, daisy-like blooms on feathery foliage that thrive in lean soil and attract butterflies.",
    ),
    PlantItem(
      id = "calendula",
      name = "Calendula (Pot Marigold)",
      scientificName = "Calendula officinalis",
      category = PlantCategory.FLOWERS,
      plantingMonths = setOf(3, 4, 5, 8),
      harvestMonths = setOf(5, 6, 7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "45–60 days",
      spacing = "8–12 in",
      notes = "Cool-tolerant edible gold and orange petals used in salads, teas, and skin-soothing salves.",
    ),
    PlantItem(
      id = "dahlia",
      name = "Dahlia",
      scientificName = "Dahlia pinnata",
      category = PlantCategory.FLOWERS,
      plantingMonths = setOf(4, 5),
      harvestMonths = setOf(7, 8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "75–100 days",
      spacing = "18–24 in",
      notes = "Plant tubers after last frost. Pinch central shoot when 12 inches tall for abundant late-summer blooms.",
    ),
    PlantItem(
      id = "sweet_pea_flower",
      name = "Sweet Pea",
      scientificName = "Lathyrus odoratus",
      category = PlantCategory.FLOWERS,
      plantingMonths = setOf(2, 3, 4),
      harvestMonths = setOf(5, 6, 7),
      sunRequirement = "Full Sun",
      daysToMaturity = "65–85 days",
      spacing = "6 in",
      notes = "Fragrant climbing flowers that love cool spring weather. Pick bouquets daily to prevent seed pods.",
    ),
    PlantItem(
      id = "snapdragon",
      name = "Snapdragon",
      scientificName = "Antirrhinum majus",
      category = PlantCategory.FLOWERS,
      plantingMonths = setOf(3, 4, 8, 9),
      harvestMonths = setOf(5, 6, 7, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "65–85 days",
      spacing = "6–12 in",
      notes = "Spiky vertical blooms in pastel and jewel tones that thrive in cool spring and fall gardens.",
    ),
    PlantItem(
      id = "borage",
      name = "Borage",
      scientificName = "Borago officinalis",
      category = PlantCategory.FLOWERS,
      plantingMonths = setOf(4, 5, 6),
      harvestMonths = setOf(6, 7, 8, 9),
      sunRequirement = "Full Sun",
      daysToMaturity = "50–60 days",
      spacing = "12–18 in",
      notes = "Star-shaped blue edible blossoms with a crisp cucumber taste; top pollinator magnet for tomatoes and squash.",
    ),
    PlantItem(
      id = "echinacea",
      name = "Coneflower (Echinacea)",
      scientificName = "Echinacea purpurea",
      category = PlantCategory.FLOWERS,
      plantingMonths = setOf(3, 4, 5, 9),
      harvestMonths = setOf(6, 7, 8, 9),
      sunRequirement = "Full Sun",
      daysToMaturity = "90–120 days",
      spacing = "18–24 in",
      notes = "Drought-hardy native perennial that feeds bees in summer and songbirds with seed heads in autumn.",
    ),
    PlantItem(
      id = "poppy",
      name = "California Poppy",
      scientificName = "Eschscholzia californica",
      category = PlantCategory.FLOWERS,
      plantingMonths = setOf(3, 4, 5, 9, 10),
      harvestMonths = setOf(5, 6, 7, 8),
      sunRequirement = "Full Sun",
      daysToMaturity = "55–70 days",
      spacing = "6–8 in",
      notes = "Silky cup-shaped blooms that open in sunlight and self-sow easily in dry, sunny borders.",
    ),
  )

  /**
   * Searches a catalog of plants by [query] and optional [category].
   * Matches plant name, scientific name, category name, notes, and active month names.
   */
  fun searchPlants(
    catalog: List<PlantItem> = BUILT_IN_PLANTS,
    query: String,
    category: PlantCategory? = null,
  ): List<PlantItem> {
    val categoryFiltered = if (category != null) {
      catalog.filter { it.category == category }
    } else {
      catalog
    }
    val trimmed = query.trim()
    if (trimmed.isEmpty()) {
      return categoryFiltered
    }
    val lowerQuery = trimmed.lowercase()

    val matchingMonthNumbers = CalendarMonth.entries
      .filter {
        it.fullName.lowercase().startsWith(lowerQuery) ||
          it.shortName.lowercase() == lowerQuery
      }
      .map { it.number }
      .toSet()

    return categoryFiltered
      .filter { plant ->
        plant.name.lowercase().contains(lowerQuery) ||
          plant.scientificName.lowercase().contains(lowerQuery) ||
          plant.category.displayName.lowercase().contains(lowerQuery) ||
          plant.notes.lowercase().contains(lowerQuery) ||
          (matchingMonthNumbers.isNotEmpty() && (
            plant.normalizedPlantingMonths.any { it in matchingMonthNumbers } ||
              plant.normalizedHarvestMonths.any { it in matchingMonthNumbers }
          ))
      }
      .sortedWith(
        compareBy<PlantItem> { plant ->
          val lowerName = plant.name.lowercase()
          when {
            lowerName == lowerQuery -> 0
            lowerName.startsWith(lowerQuery) -> 1
            lowerName.split(" ").any { it.startsWith(lowerQuery) } -> 2
            lowerName.contains(lowerQuery) -> 3
            else -> 4
          }
        }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      )
  }

  /**
   * Adds [plant] to the user's [schedule]. If a plant with the same [PlantItem.id] already exists,
   * replaces it in-place with the updated [plant]; otherwise appends it.
   */
  fun addPlantToSchedule(
    schedule: List<PlantItem>,
    plant: PlantItem,
  ): List<PlantItem> {
    val existingIndex = schedule.indexOfFirst { it.id == plant.id }
    return if (existingIndex >= 0) {
      schedule.toMutableList().apply { set(existingIndex, plant) }.toList()
    } else {
      schedule + plant
    }
  }

  /**
   * Removes the plant with [plantId] from [schedule].
   */
  fun removePlantFromSchedule(
    schedule: List<PlantItem>,
    plantId: String,
  ): List<PlantItem> {
    return schedule.filter { it.id != plantId }
  }

  /**
   * Toggles the presence of [plant] in [schedule].
   */
  fun togglePlantInSchedule(
    schedule: List<PlantItem>,
    plant: PlantItem,
  ): List<PlantItem> {
    return if (schedule.any { it.id == plant.id }) {
      removePlantFromSchedule(schedule, plant.id)
    } else {
      addPlantToSchedule(schedule, plant)
    }
  }

  /**
   * Updates an existing plant in [schedule] by matching [PlantItem.id].
   */
  fun updateScheduledPlant(
    schedule: List<PlantItem>,
    updatedPlant: PlantItem,
  ): List<PlantItem> {
    return schedule.map { if (it.id == updatedPlant.id) updatedPlant else it }
  }

  /**
   * Returns a pair of lists `(plantingInMonth, harvestingInMonth)` for the given [month] (1..12).
   */
  fun getPlantsForMonth(
    schedule: List<PlantItem>,
    month: Int,
  ): Pair<List<PlantItem>, List<PlantItem>> {
    val planting = schedule.filter { it.isPlantingInMonth(month) }
    val harvesting = schedule.filter { it.isHarvestingInMonth(month) }
    return planting to harvesting
  }

  fun plantsForPlantingInMonth(
    schedule: List<PlantItem>,
    month: Int,
  ): List<PlantItem> = schedule.filter { it.isPlantingInMonth(month) }

  fun plantsForHarvestInMonth(
    schedule: List<PlantItem>,
    month: Int,
  ): List<PlantItem> = schedule.filter { it.isHarvestingInMonth(month) }

  /**
   * Formats a set of month numbers (1..12) into compact human-readable ranges (e.g. "Mar–May" or "Mar–Apr, Aug–Sep").
   */
  fun formatMonthRanges(months: Set<Int>): String {
    val sorted = months.filter { it in 1..12 }.sorted()
    if (sorted.isEmpty()) return "None"
    if (sorted.size == 12) return "Year-round (Jan–Dec)"

    val ranges = mutableListOf<IntRange>()
    var rangeStart = sorted.first()
    var prev = rangeStart

    for (i in 1 until sorted.size) {
      val current = sorted[i]
      if (current == prev + 1) {
        prev = current
      } else {
        ranges.add(rangeStart..prev)
        rangeStart = current
        prev = current
      }
    }
    ranges.add(rangeStart..prev)

    return ranges.joinToString(", ") { range ->
      val startName = CalendarMonth.fromNumber(range.first).shortName
      val endName = CalendarMonth.fromNumber(range.last).shortName
      if (range.first == range.last) {
        startName
      } else {
        "$startName–$endName"
      }
    }
  }
}
