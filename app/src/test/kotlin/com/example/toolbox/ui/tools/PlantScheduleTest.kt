package com.example.toolbox.ui.tools

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class PlantScheduleTest {

  @Test
  fun builtInPlants_haveValidMonthsAndCategories() {
    assertTrue(PlantScheduleEngine.BUILT_IN_PLANTS.size >= 30)
    val ids = PlantScheduleEngine.BUILT_IN_PLANTS.map { it.id }
    assertEquals(ids.size, ids.distinct().size)

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
}
