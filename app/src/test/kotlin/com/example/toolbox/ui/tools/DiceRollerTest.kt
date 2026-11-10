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
  }
}
