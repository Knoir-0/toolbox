package com.example.toolbox.ui.tools

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.json.JSONObject
import org.junit.Test
import java.util.UUID

class DiceRollerTest {

  @Test
  fun savedDiceRoll_notationFormatsCorrectly() {
    val normalRoll = SavedDiceRoll(
      name = "Longsword",
      dieType = DieType.D8,
      diceCount = 1,
      modifier = 3,
      rollMode = RollMode.NORMAL,
    )
    assertEquals("1d8 + 3", normalRoll.notation)

    val advRoll = SavedDiceRoll(
      name = "Initiative",
      dieType = DieType.D20,
      diceCount = 1,
      modifier = 2,
      rollMode = RollMode.ADVANTAGE,
    )
    assertEquals("1d20 (Adv) + 2", advRoll.notation)

    val disRoll = SavedDiceRoll(
      name = "Stealth Check",
      dieType = DieType.D20,
      diceCount = 1,
      modifier = -1,
      rollMode = RollMode.DISADVANTAGE,
    )
    assertEquals("1d20 (Dis) - 1", disRoll.notation)

    val plainRoll = SavedDiceRoll(
      name = "Fireball",
      dieType = DieType.D6,
      diceCount = 8,
      modifier = 0,
      rollMode = RollMode.NORMAL,
    )
    assertEquals("8d6", plainRoll.notation)
  }

  @Test
  fun savedDiceRoll_jsonSerializationRoundTrip() {
    val original = SavedDiceRoll(
      id = UUID.randomUUID().toString(),
      name = "Greatsword Smash",
      dieType = DieType.D6,
      diceCount = 2,
      modifier = 4,
      rollMode = RollMode.NORMAL,
    )

    val json = original.toJson()
    val restored = SavedDiceRoll.fromJson(json)

    assertEquals(original.id, restored.id)
    assertEquals(original.name, restored.name)
    assertEquals(original.dieType, restored.dieType)
    assertEquals(original.diceCount, restored.diceCount)
    assertEquals(original.modifier, restored.modifier)
    assertEquals(original.rollMode, restored.rollMode)
    assertEquals(original.notation, restored.notation)
  }

  @Test
  fun savedDiceRoll_fromJsonHandlesInvalidOrCorruptData() {
    val emptyObj = JSONObject()
    val restored = SavedDiceRoll.fromJson(emptyObj)

    assertEquals("Custom Roll", restored.name)
    assertEquals(DieType.D20, restored.dieType)
    assertEquals(1, restored.diceCount)
    assertEquals(0, restored.modifier)
    assertEquals(RollMode.NORMAL, restored.rollMode)

    val corruptObj = JSONObject().apply {
      put("name", "Corrupt Test")
      put("dieType", "NON_EXISTENT_DIE")
      put("diceCount", 999) // Should be coerced to 20
      put("modifier", -50) // Should be coerced to -20
      put("rollMode", "INVALID_MODE")
    }
    val restoredCorrupt = SavedDiceRoll.fromJson(corruptObj)
    assertEquals("Corrupt Test", restoredCorrupt.name)
    assertEquals(DieType.D20, restoredCorrupt.dieType)
    assertEquals(20, restoredCorrupt.diceCount)
    assertEquals(-20, restoredCorrupt.modifier)
    assertEquals(RollMode.NORMAL, restoredCorrupt.rollMode)
    val highModObj = JSONObject().apply {
      put("name", "High Mod Test")
      put("dieType", "D20")
      put("diceCount", 1)
      put("modifier", 50) // Should be coerced to 40
      put("rollMode", "NORMAL")
    }
    val restoredHighMod = SavedDiceRoll.fromJson(highModObj)
    assertEquals(40, restoredHighMod.modifier)
  }

  @Test
  fun dndSpellCatalog_searchesSrdSpellsWeaponsAndAttribution() {
    val allEntries = DndSpellCatalog.searchSpells()
    // 339 SRD 5.2.1 spells + 38 SRD 5.2.1 weapons = 377 entries
    assertEquals(377, allEntries.size)

    // Old non-SRD spells like Toll the Dead must not be present
    assertTrue(allEntries.none { it.name.equals("Toll the Dead", ignoreCase = true) })

    // Verify first and last alphabetical SRD 5.2.1 spells are present with full descriptions
    val acidArrow = DndSpellCatalog.searchSpells(query = "Acid Arrow").first()
    assertEquals("Acid Arrow", acidArrow.name)
    assertEquals(2, acidArrow.level)
    assertEquals(DieType.D4, acidArrow.dieType)
    assertEquals(4, acidArrow.diceCount)
    assertTrue(acidArrow.description.contains("powdered rhubarb leaf"))
    assertTrue(acidArrow.description.contains("4d4 Acid damage"))

    val zoneOfTruth = DndSpellCatalog.searchSpells(query = "Zone of Truth").first()
    assertEquals("Zone of Truth", zoneOfTruth.name)
    assertEquals(2, zoneOfTruth.level)
    assertTrue(zoneOfTruth.description.contains("Charisma saving throw"))

    // Verify Fireball is ranked first when searching "Fireball"
    val fireballResults = DndSpellCatalog.searchSpells(query = "Fireball")
    assertTrue(fireballResults.isNotEmpty())
    val fireball = fireballResults.first()
    assertEquals("Fireball", fireball.name)
    assertEquals(DieType.D6, fireball.dieType)
    assertEquals(8, fireball.diceCount)
    assertEquals(0, fireball.modifier)
    assertEquals("8d6", fireball.notation)

    // Verify Healing spells
    val healingSpells = DndSpellCatalog.searchSpells(category = DndSpellCategory.HEALING)
    assertTrue(healingSpells.any { it.name == "Cure Wounds" })
    assertTrue(healingSpells.all { it.category == DndSpellCategory.HEALING })

    // Verify SRD 5.2.1 Weapons are included and filterable
    val weapons = DndSpellCatalog.searchSpells(category = DndSpellCategory.WEAPONS)
    assertEquals(38, weapons.size)
    assertTrue(weapons.all { it.category == DndSpellCategory.WEAPONS && it.levelLabel == "Weapon" })

    val greatsword = DndSpellCatalog.searchSpells(query = "Greatsword").first()
    assertEquals("Greatsword", greatsword.name)
    assertEquals(DndSpellCategory.WEAPONS, greatsword.category)
    assertEquals(DieType.D6, greatsword.dieType)
    assertEquals(2, greatsword.diceCount)
    assertEquals("2d6", greatsword.notation)
    assertTrue(greatsword.description.contains("Graze"))

    val longsword = DndSpellCatalog.searchSpells(query = "Longsword").first()
    assertEquals("Longsword", longsword.name)
    assertEquals("1d8", longsword.notation)
    assertTrue(longsword.description.contains("Versatile (1d10)"))

    // Verify CC-BY-4.0 attribution text
    assertTrue(DndSpellCatalog.SRD_ATTRIBUTION_TEXT.contains("System Reference Document 5.2.1"))
    assertTrue(DndSpellCatalog.SRD_ATTRIBUTION_TEXT.contains("Wizards of the Coast LLC"))
    assertTrue(DndSpellCatalog.SRD_ATTRIBUTION_TEXT.contains("Creative Commons Attribution 4.0 International License"))
  }

  @Test
  fun diceRollerWidget_rollsD20WithNormalAdvantageAndDisadvantage() {
    DiceRollerWidgets.clearInMemoryStateForTesting()

    // Initial unrolled state
    val initial = DiceRollerWidgets.resolveWidgetState(context = null)
    assertEquals(RollMode.NORMAL, initial.rollMode)
    assertEquals(null, initial.latestRoll)
    assertEquals("1d20", initial.notationText)
    assertEquals("TAP TO ROLL", initial.statusBadgeText)

    // Roll with Advantage (e.g., 7 and 19 -> keeps 19, drops 7)
    val advRoll = DiceRollerWidgets.rollD20FromWidget(
      context = null,
      mode = RollMode.ADVANTAGE,
      fixedFirstRoll = 7,
      fixedSecondRoll = 19,
    )
    assertEquals(19, advRoll.total)
    assertEquals(listOf(19), advRoll.keptRolls)
    assertEquals(listOf(7), advRoll.droppedRolls)
    val advState = DiceRollerWidgets.resolveWidgetState(context = null)
    assertEquals(RollMode.ADVANTAGE, advState.rollMode)
    assertEquals("1d20 (Adv)", advState.notationText)
    assertEquals("ADVANTAGE", advState.statusBadgeText)
    assertEquals("Kept 19 · Dropped 7", advState.breakdownText)

    // Roll with Disadvantage (e.g., 18 and 4 -> keeps 4, drops 18)
    val disRoll = DiceRollerWidgets.rollD20FromWidget(
      context = null,
      mode = RollMode.DISADVANTAGE,
      fixedFirstRoll = 18,
      fixedSecondRoll = 4,
    )
    assertEquals(4, disRoll.total)
    assertEquals(listOf(4), disRoll.keptRolls)
    assertEquals(listOf(18), disRoll.droppedRolls)
    val disState = DiceRollerWidgets.resolveWidgetState(context = null)
    assertEquals(RollMode.DISADVANTAGE, disState.rollMode)
    assertEquals("1d20 (Dis)", disState.notationText)
    assertEquals("DISADVANTAGE", disState.statusBadgeText)
    assertEquals("Kept 4 · Dropped 18", disState.breakdownText)

    // Critical Hit (Natural 20) & Critical Miss (Natural 1)
    DiceRollerWidgets.rollD20FromWidget(
      context = null,
      mode = RollMode.NORMAL,
      fixedFirstRoll = 20,
    )
    val nat20State = DiceRollerWidgets.resolveWidgetState(context = null)
    assertTrue(nat20State.isNatural20)
    assertEquals("★ CRITICAL HIT · NAT 20!", nat20State.statusBadgeText)

    val nat1Roll = DiceRollerWidgets.rollD20FromWidget(
      context = null,
      mode = RollMode.NORMAL,
      fixedFirstRoll = 1,
    )
    val nat1State = DiceRollerWidgets.resolveWidgetState(context = null)
    assertTrue(nat1State.isNatural1)
    assertEquals("✦ CRITICAL MISS · NAT 1!", nat1State.statusBadgeText)

    // JSON round-trip
    val json = DiceRollerWidgets.rollResultToJson(advRoll)
    val restored = DiceRollerWidgets.rollResultFromJson(json)
    assertEquals(advRoll, restored)
    assertEquals(1, nat1Roll.total)
  }

  @Test
  fun diceJailEasterEgg_triggersOnTwoConsecutiveNatural1s() {
    DiceRollerWidgets.clearInMemoryStateForTesting()
    assertEquals(2, DICE_JAIL_CONSECUTIVE_NAT_1_THRESHOLD)

    val nat1First = DiceRollResult(
      id = 1L,
      dieType = DieType.D20,
      diceCount = 1,
      modifier = 0,
      rollMode = RollMode.NORMAL,
      keptRolls = listOf(1),
      total = 1,
    )
    val nat1Second = nat1First.copy(id = 2L)
    val normalRoll = nat1First.copy(id = 3L, keptRolls = listOf(14), total = 14)

    // Single Nat 1 does not trigger Dice Jail
    assertEquals(1, countConsecutiveNatural1s(listOf(nat1First)))
    assertTrue(!isInDiceJail(listOf(nat1First)))

    // Two consecutive Nat 1s at head of history triggers Dice Jail
    val twoNat1History = listOf(nat1Second, nat1First)
    assertEquals(2, countConsecutiveNatural1s(twoNat1History))
    assertTrue(isInDiceJail(twoNat1History))

    // Rolling a non-Nat 1 breaks the streak and releases from Dice Jail
    val brokenStreakHistory = listOf(normalRoll, nat1Second, nat1First)
    assertEquals(0, countConsecutiveNatural1s(brokenStreakHistory))
    assertTrue(!isInDiceJail(brokenStreakHistory))

    // Widget also tracks 2 consecutive Nat 1s for Dice Jail badge
    DiceRollerWidgets.rollD20FromWidget(context = null, mode = RollMode.NORMAL, fixedFirstRoll = 1)
    val afterOneNat1 = DiceRollerWidgets.resolveWidgetState(context = null)
    assertTrue(afterOneNat1.isNatural1)
    assertTrue(!afterOneNat1.isInDiceJail)
    assertEquals("✦ CRITICAL MISS · NAT 1!", afterOneNat1.statusBadgeText)

    DiceRollerWidgets.rollD20FromWidget(context = null, mode = RollMode.NORMAL, fixedFirstRoll = 1)
    val afterTwoNat1s = DiceRollerWidgets.resolveWidgetState(context = null)
    assertTrue(afterTwoNat1s.isNatural1)
    assertTrue(afterTwoNat1s.isInDiceJail)
    assertEquals(DICE_JAIL_BADGE_TEXT, afterTwoNat1s.statusBadgeText)
  }
}
