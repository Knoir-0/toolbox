package com.example.toolbox.ui.tools

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class PlantScheduleTest {

  @Test
  fun builtInPlants_haveValidMonthsAndCategories() {
    assertTrue(PlantScheduleEngine.BUILT_IN_PLANTS.size >= 80)
    val ids = PlantScheduleEngine.BUILT_IN_PLANTS.map { it.id }
    assertEquals(ids.size, ids.distinct().size)
    assertTrue(ids.contains("peppers"))
    assertTrue(ids.contains("sweet_peppers"))

    for (plant in PlantScheduleEngine.BUILT_IN_PLANTS) {
      assertTrue("Plant ${plant.id} should have a name", plant.name.isNotBlank())
      assertTrue(
        "Plant ${plant.name} should have planting months in 1..12",
        plant.plantingMonths.isNotEmpty() && plant.plantingMonths.all { it in 1..12 },
      )
      assertTrue(
        "Plant ${plant.name} should have harvest months in 1..12",
        plant.harvestMonths.isNotEmpty() && plant.harvestMonths.all { it in 1..12 },
      )
    }

    for (category in PlantCategory.entries) {
      val inCategory = PlantScheduleEngine.BUILT_IN_PLANTS.filter { it.category == category }
      assertTrue("Category $category should contain built-in plants", inCategory.isNotEmpty())
    }
  }

  @Test
  fun searchPlants_matchesNameScientificCategoryAndMonth() {
    // Search by common name
    val tomatoResults = PlantScheduleEngine.searchPlants(query = "tomato")
    assertTrue(tomatoResults.isNotEmpty())
    assertEquals("Tomato", tomatoResults.first().name)

    // Search for Peppers and Sweet Peppers
    val peppersResults = PlantScheduleEngine.searchPlants(query = "peppers")
    assertTrue(peppersResults.isNotEmpty())
    assertEquals("Peppers", peppersResults.first().name)
    assertTrue(peppersResults.any { it.id == "sweet_peppers" })

    val sweetPeppersResults = PlantScheduleEngine.searchPlants(query = "sweet peppers")
    assertTrue(sweetPeppersResults.isNotEmpty())
    assertEquals("Sweet Peppers", sweetPeppersResults.first().name)

    // Search by scientific name
    val basilResults = PlantScheduleEngine.searchPlants(query = "Ocimum")
    assertEquals(1, basilResults.size)
    assertEquals("Basil", basilResults.first().name)

    // Search by category filter + query
    val herbResults = PlantScheduleEngine.searchPlants(
      query = "mint",
      category = PlantCategory.HERBS,
    )
    assertEquals(1, herbResults.size)
    assertEquals("Mint", herbResults.first().name)

    // Search by month name
    val octoberPlanting = PlantScheduleEngine.searchPlants(query = "October")
    assertTrue(octoberPlanting.any { it.id == "garlic" })
  }

  @Test
  fun scheduleOperations_addRemoveToggleAndUpdate() {
    val tomato = PlantScheduleEngine.BUILT_IN_PLANTS.first { it.id == "tomato" }
    val basil = PlantScheduleEngine.BUILT_IN_PLANTS.first { it.id == "basil" }

    var schedule = emptyList<PlantItem>()
    schedule = PlantScheduleEngine.addPlantToSchedule(schedule, tomato)
    assertEquals(listOf(tomato), schedule)

    // Adding duplicate replaces in-place without duplicating
    schedule = PlantScheduleEngine.addPlantToSchedule(schedule, tomato)
    assertEquals(1, schedule.size)

    // Toggle adds when absent and removes when present
    schedule = PlantScheduleEngine.togglePlantInSchedule(schedule, basil)
    assertEquals(listOf(tomato, basil), schedule)

    schedule = PlantScheduleEngine.togglePlantInSchedule(schedule, tomato)
    assertEquals(listOf(basil), schedule)

    // Update scheduled plant months
    val updatedBasil = basil.copy(plantingMonths = setOf(5, 6), harvestMonths = setOf(7, 8, 9))
    schedule = PlantScheduleEngine.updateScheduledPlant(schedule, updatedBasil)
    assertEquals(setOf(5, 6), schedule.first().normalizedPlantingMonths)

    // Remove plant
    schedule = PlantScheduleEngine.removePlantFromSchedule(schedule, basil.id)
    assertTrue(schedule.isEmpty())
  }

  @Test
  fun activityForMonth_distinguishesPlantingHarvestingBothAndNone() {
    val plant = PlantItem(
      id = "test_lettuce",
      name = "Lettuce",
      plantingMonths = setOf(3, 4, 5),
      harvestMonths = setOf(5, 6, 7),
    )

    assertEquals(MonthActivityState.NONE, plant.activityForMonth(1))
    assertEquals(MonthActivityState.PLANTING, plant.activityForMonth(3))
    assertEquals(MonthActivityState.PLANTING, plant.activityForMonth(4))
    assertEquals(MonthActivityState.BOTH, plant.activityForMonth(5))
    assertEquals(MonthActivityState.HARVESTING, plant.activityForMonth(6))
    assertEquals(MonthActivityState.HARVESTING, plant.activityForMonth(7))
    assertEquals(MonthActivityState.NONE, plant.activityForMonth(12))
  }

  @Test
  fun formatMonthRanges_formatsContiguousAndDisjointMonths() {
    assertEquals("None", PlantScheduleEngine.formatMonthRanges(emptySet()))
    assertEquals("May", PlantScheduleEngine.formatMonthRanges(setOf(5)))
    assertEquals("Mar–May", PlantScheduleEngine.formatMonthRanges(setOf(3, 4, 5)))
    assertEquals("Mar–Apr, Aug–Sep", PlantScheduleEngine.formatMonthRanges(setOf(3, 4, 8, 9)))
    assertEquals("Year-round (Jan–Dec)", PlantScheduleEngine.formatMonthRanges((1..12).toSet()))
  }

  @Test
  fun getPlantsForMonth_returnsPlantingAndHarvestingLists() {
    val tomato = PlantScheduleEngine.BUILT_IN_PLANTS.first { it.id == "tomato" } // Plant: 3,4,5; Harvest: 7,8,9,10
    val garlic = PlantScheduleEngine.BUILT_IN_PLANTS.first { it.id == "garlic" } // Plant: 10,11; Harvest: 6,7
    val schedule = listOf(tomato, garlic)

    val (plantInApril, harvestInApril) = PlantScheduleEngine.getPlantsForMonth(schedule, 4)
    assertEquals(listOf(tomato), plantInApril)
    assertTrue(harvestInApril.isEmpty())

    val (plantInJuly, harvestInJuly) = PlantScheduleEngine.getPlantsForMonth(schedule, 7)
    assertTrue(plantInJuly.isEmpty())
    assertEquals(listOf(tomato, garlic), harvestInJuly)

    val (plantInOct, harvestInOct) = PlantScheduleEngine.getPlantsForMonth(schedule, 10)
    assertEquals(listOf(garlic), plantInOct)
    assertEquals(listOf(tomato), harvestInOct)

    assertEquals(listOf(tomato), PlantScheduleEngine.plantsForPlantingInMonth(schedule, 4))
    assertEquals(listOf(tomato, garlic), PlantScheduleEngine.plantsForHarvestInMonth(schedule, 7))
    assertEquals("Mar–May", tomato.plantingSummary())
    assertEquals("Jul–Oct", tomato.harvestSummary())
    assertEquals("J", CalendarMonth.JANUARY.shortLabel)
  }

  @Test
  fun plantItem_jsonRoundTripAndRepositoryPersistence() {
    val customPlant = PlantItem(
      id = "custom_pepper",
      name = "Heirloom Chili",
      scientificName = "Capsicum chinense",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(3, 4),
      harvestMonths = setOf(8, 9, 10),
      sunRequirement = "Full Sun",
      daysToMaturity = "90 days",
      spacing = "18 in",
      notes = "Start warm indoors",
      isCustom = true,
    )

    val json = customPlant.toJson()
    val restored = PlantItem.fromJson(json)
    assertEquals(customPlant, restored)
    assertTrue(restored.isPlantingInMonth(3))
    assertFalse(restored.isPlantingInMonth(8))
    assertTrue(restored.isHarvestingInMonth(8))

    val repo = InMemoryPlantScheduleRepository()
    repo.saveScheduledPlants(listOf(customPlant))
    repo.saveCustomPlants(listOf(customPlant))

    assertEquals(listOf(customPlant), repo.loadScheduledPlants())
    assertEquals(listOf(customPlant), repo.loadCustomPlants())
  }

  @Test
  fun plantIcons_matchPlantTypesAndCategories() {
    val byId = PlantScheduleEngine.BUILT_IN_PLANTS.associateBy { it.id }

    assertEquals(PlantIconType.TOMATO, byId.getValue("tomato").iconType)
    assertEquals(PlantIconType.BELL_PEPPER, byId.getValue("bell_pepper").iconType)
    assertEquals(PlantIconType.CHILI_PEPPER, byId.getValue("jalapeno_pepper").iconType)
    assertEquals(PlantIconType.CUCUMBER_SQUASH, byId.getValue("cucumber").iconType)
    assertEquals(PlantIconType.PUMPKIN, byId.getValue("pumpkin").iconType)
    assertEquals(PlantIconType.PEA_POD, byId.getValue("snap_peas").iconType)
    assertEquals(PlantIconType.CORN, byId.getValue("sweet_corn").iconType)
    assertEquals(PlantIconType.EGGPLANT, byId.getValue("eggplant").iconType)
    assertEquals(PlantIconType.BROCCOLI, byId.getValue("broccoli").iconType)
    assertEquals(PlantIconType.CABBAGE, byId.getValue("cabbage").iconType)
    assertEquals(PlantIconType.STALK, byId.getValue("asparagus").iconType)
    assertEquals(PlantIconType.ARTICHOKE_OKRA, byId.getValue("artichoke").iconType)
    assertEquals(PlantIconType.LETTUCE_HEAD, byId.getValue("lettuce").iconType)
    assertEquals(PlantIconType.LEAFY_GREEN, byId.getValue("spinach").iconType)
    assertEquals(PlantIconType.CARROT_ROOT, byId.getValue("carrot").iconType)
    assertEquals(PlantIconType.ROUND_ROOT, byId.getValue("radish").iconType)
    assertEquals(PlantIconType.POTATO_TUBER, byId.getValue("potato").iconType)
    assertEquals(PlantIconType.ALLIUM_BULB, byId.getValue("garlic").iconType)
    assertEquals(PlantIconType.SCALLION_LEEK, byId.getValue("scallion").iconType)
    assertEquals(PlantIconType.HERB_LEAF, byId.getValue("basil").iconType)
    assertEquals(PlantIconType.HERB_NEEDLE, byId.getValue("rosemary").iconType)
    assertEquals(PlantIconType.STRAWBERRY, byId.getValue("strawberry").iconType)
    assertEquals(PlantIconType.MELON, byId.getValue("watermelon").iconType)
    assertEquals(PlantIconType.BERRY, byId.getValue("blueberry").iconType)
    assertEquals(PlantIconType.GRAPE, byId.getValue("grape").iconType)
    assertEquals(PlantIconType.FLOWER_BLOOM, byId.getValue("sunflower").iconType)

    // Every PlantIconType produces a distinct ImageVector
    val vectors = PlantIconType.entries.map { it.imageVector() }
    assertEquals(PlantIconType.entries.size, vectors.map { it.name }.distinct().size)

    // Custom plants match by name keywords or fall back to category icon
    val customChili = PlantItem(
      id = "custom_1",
      name = "Ghost Chili Pepper",
      category = PlantCategory.VEGETABLES,
      plantingMonths = setOf(4),
      harvestMonths = setOf(8),
      isCustom = true,
    )
    assertEquals(PlantIconType.CHILI_PEPPER, customChili.iconType)

    val customFlower = PlantItem(
      id = "custom_2",
      name = "Moonbeam Blossom",
      category = PlantCategory.FLOWERS,
      plantingMonths = setOf(4),
      harvestMonths = setOf(7),
      isCustom = true,
    )
    assertEquals(PlantIconType.FLOWER_BLOOM, customFlower.iconType)
  }
}
