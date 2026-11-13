package com.example.toolbox.ui.tools

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.toolbox.ui.theme.ToolboxTheme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

enum class DieType(val sides: Int, val label: String, val polygonSides: Int) {
  D4(4, "d4", 3),
  D6(6, "d6", 4),
  D8(8, "d8", 4),
  D10(10, "d10", 5),
  D12(12, "d12", 10),
  D20(20, "d20", 6),
  D100(100, "d100", 8),
}

enum class RollMode(val label: String) {
  DISADVANTAGE("Disadvantage"),
  NORMAL("Normal"),
  ADVANTAGE("Advantage"),
}

enum class HistorySortOrder(val label: String) {
  NEWEST_FIRST("Newest"),
  OLDEST_FIRST("Oldest"),
  HIGHEST_TOTAL("Highest"),
  LOWEST_TOTAL("Lowest"),
}

data class DiceRollResult(
  val id: Long,
  val dieType: DieType,
  val diceCount: Int,
  val modifier: Int,
  val rollMode: RollMode,
  val keptRolls: List<Int>,
  val droppedRolls: List<Int> = emptyList(),
  val total: Int,
) {
  val notation: String
    get() {
      val base = "${diceCount}${dieType.label}"
      val modeSuffix = when (rollMode) {
        RollMode.NORMAL -> ""
        RollMode.ADVANTAGE -> " (Adv)"
        RollMode.DISADVANTAGE -> " (Dis)"
      }
      val modSuffix = when {
        modifier > 0 -> " + $modifier"
        modifier < 0 -> " - ${-modifier}"
        else -> ""
      }
      return "$base$modeSuffix$modSuffix"
    }

  val breakdown: String
    get() {
      val rollsPart = if (droppedRolls.isNotEmpty()) {
        "Kept ${keptRolls.joinToString(", ")} · Dropped ${droppedRolls.joinToString(", ")}"
      } else {
        "Rolls: ${keptRolls.joinToString(", ")}"
      }
      val modPart = when {
        modifier > 0 -> " (+$modifier)"
        modifier < 0 -> " ($modifier)"
        else -> ""
      }
      return "$rollsPart$modPart"
    }

  val isNatural20: Boolean
    get() = dieType == DieType.D20 && keptRolls.size == 1 && keptRolls.first() == 20

  val isNatural1: Boolean
    get() = dieType == DieType.D20 && keptRolls.size == 1 && keptRolls.first() == 1
}

data class SavedDiceRoll(
  val id: String = java.util.UUID.randomUUID().toString(),
  val name: String,
  val dieType: DieType,
  val diceCount: Int,
  val modifier: Int,
  val rollMode: RollMode = RollMode.NORMAL,
) {
  val notation: String
    get() {
      val base = "${diceCount}${dieType.label}"
      val modeSuffix = when (rollMode) {
        RollMode.NORMAL -> ""
        RollMode.ADVANTAGE -> " (Adv)"
        RollMode.DISADVANTAGE -> " (Dis)"
      }
      val modSuffix = when {
        modifier > 0 -> " + $modifier"
        modifier < 0 -> " - ${-modifier}"
        else -> ""
      }
      return "$base$modeSuffix$modSuffix"
    }

  fun toJson(): JSONObject {
    val obj = JSONObject()
    obj.put("id", id)
    obj.put("name", name)
    obj.put("dieType", dieType.name)
    obj.put("diceCount", diceCount)
    obj.put("modifier", modifier)
    obj.put("rollMode", rollMode.name)
    return obj
  }

  companion object {
    fun fromJson(obj: JSONObject): SavedDiceRoll {
      val id = obj.optString("id", java.util.UUID.randomUUID().toString())
      val name = obj.optString("name", "Custom Roll")
      val dieType = try {
        DieType.valueOf(obj.optString("dieType", DieType.D20.name))
      } catch (_: Exception) {
        DieType.D20
      }
      val diceCount = obj.optInt("diceCount", 1).coerceIn(1, 20)
      val modifier = obj.optInt("modifier", 0).coerceIn(-20, 20)
      val rollMode = try {
        RollMode.valueOf(obj.optString("rollMode", RollMode.NORMAL.name))
      } catch (_: Exception) {
        RollMode.NORMAL
      }
      return SavedDiceRoll(
        id = id,
        name = name,
        dieType = dieType,
        diceCount = diceCount,
        modifier = modifier,
        rollMode = rollMode,
      )
    }
  }
}

class DiceRollerPreferences(private val sharedPreferences: SharedPreferences) {
  companion object {
    const val PREFS_NAME = "dice_roller_preferences"
    const val KEY_SAVED_ROLLS_JSON = "saved_rolls_json"
  }

  constructor(context: Context) : this(
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  )

  fun loadSavedRolls(): List<SavedDiceRoll> {
    val jsonString = sharedPreferences.getString(KEY_SAVED_ROLLS_JSON, null) ?: return emptyList()
    return try {
      val array = JSONArray(jsonString)
      val list = mutableListOf<SavedDiceRoll>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(SavedDiceRoll.fromJson(obj))
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }

  fun saveSavedRolls(savedRolls: List<SavedDiceRoll>) {
    val array = JSONArray()
    savedRolls.forEach { array.put(it.toJson()) }
    sharedPreferences.edit().putString(KEY_SAVED_ROLLS_JSON, array.toString()).apply()
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceRollerScreen(modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val preferences = remember(context, isInspection) {
    if (isInspection) null else runCatching { DiceRollerPreferences(context) }.getOrNull()
  }
  val initialSavedRolls = remember(preferences) {
    preferences?.loadSavedRolls() ?: emptyList()
  }

  DiceRollerScreen(
    modifier = modifier,
    initialSheetValue = SheetValue.PartiallyExpanded,
    initialHistory = emptyList(),
    initialSavedRolls = initialSavedRolls,
    onSaveRolls = { rolls ->
      preferences?.saveSavedRolls(rolls)
    },
  )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun DiceRollerScreen(
  modifier: Modifier = Modifier,
  initialSheetValue: SheetValue,
  initialHistory: List<DiceRollResult> = emptyList(),
  initialSavedRolls: List<SavedDiceRoll> = emptyList(),
  onSaveRolls: (List<SavedDiceRoll>) -> Unit = {},
) {
  var selectedDie by rememberSaveable { mutableStateOf(DieType.D20) }
  var diceCount by rememberSaveable { mutableIntStateOf(1) }
  var modifierValue by rememberSaveable { mutableIntStateOf(0) }
  var rollMode by rememberSaveable { mutableStateOf(RollMode.NORMAL) }
  var rollCounter by rememberSaveable { mutableIntStateOf(0) }
  var showSaveDialog by rememberSaveable { mutableStateOf(false) }
  var showSavedRollsSheet by rememberSaveable { mutableStateOf(false) }

  val savedRolls = remember {
    mutableStateListOf<SavedDiceRoll>().apply {
      addAll(initialSavedRolls)
    }
  }

  val history = remember {
    mutableStateListOf<DiceRollResult>().apply {
      addAll(initialHistory)
    }
  }

  val effectiveRollMode = if (selectedDie == DieType.D20 && diceCount == 1) rollMode else RollMode.NORMAL

  fun performRoll() {
    rollCounter++
    val kept: List<Int>
    val dropped: List<Int>

    if (effectiveRollMode != RollMode.NORMAL) {
      val r1 = Random.nextInt(1, selectedDie.sides + 1)
      val r2 = Random.nextInt(1, selectedDie.sides + 1)
      if (effectiveRollMode == RollMode.ADVANTAGE) {
        kept = listOf(maxOf(r1, r2))
        dropped = listOf(minOf(r1, r2))
      } else {
        kept = listOf(minOf(r1, r2))
        dropped = listOf(maxOf(r1, r2))
      }
    } else {
      kept = List(diceCount) { Random.nextInt(1, selectedDie.sides + 1) }
      dropped = emptyList()
    }

    val total = kept.sum() + modifierValue
    history.add(
      0,
      DiceRollResult(
        id = rollCounter.toLong(),
        dieType = selectedDie,
        diceCount = diceCount,
        modifier = modifierValue,
        rollMode = effectiveRollMode,
        keptRolls = kept,
        droppedRolls = dropped,
        total = total,
      ),
    )
  }

  fun rollPreset(savedRoll: SavedDiceRoll) {
    selectedDie = savedRoll.dieType
    diceCount = savedRoll.diceCount
    modifierValue = savedRoll.modifier
    rollMode = savedRoll.rollMode

    rollCounter++
    val kept: List<Int>
    val dropped: List<Int>

    val effectiveMode = if (savedRoll.dieType == DieType.D20 && savedRoll.diceCount == 1) savedRoll.rollMode else RollMode.NORMAL

    if (effectiveMode != RollMode.NORMAL) {
      val r1 = Random.nextInt(1, savedRoll.dieType.sides + 1)
      val r2 = Random.nextInt(1, savedRoll.dieType.sides + 1)
      if (effectiveMode == RollMode.ADVANTAGE) {
        kept = listOf(maxOf(r1, r2))
        dropped = listOf(minOf(r1, r2))
      } else {
        kept = listOf(minOf(r1, r2))
        dropped = listOf(maxOf(r1, r2))
      }
    } else {
      kept = List(savedRoll.diceCount) { Random.nextInt(1, savedRoll.dieType.sides + 1) }
      dropped = emptyList()
    }

    val total = kept.sum() + savedRoll.modifier
    history.add(
      0,
      DiceRollResult(
        id = rollCounter.toLong(),
        dieType = savedRoll.dieType,
        diceCount = savedRoll.diceCount,
        modifier = savedRoll.modifier,
        rollMode = effectiveMode,
        keptRolls = kept,
        droppedRolls = dropped,
        total = total,
      ),
    )
  }

  val currentNotation = remember(selectedDie, diceCount, modifierValue, effectiveRollMode) {
    val base = "${diceCount}${selectedDie.label}"
    val modeSuffix = when (effectiveRollMode) {
      RollMode.NORMAL -> ""
      RollMode.ADVANTAGE -> " (Adv)"
      RollMode.DISADVANTAGE -> " (Dis)"
    }
    val modSuffix = when {
      modifierValue > 0 -> " + $modifierValue"
      modifierValue < 0 -> " - ${-modifierValue}"
      else -> ""
    }
    "$base$modeSuffix$modSuffix"
  }

  val sheetState = rememberStandardBottomSheetState(
    initialValue = initialSheetValue,
    skipHiddenState = true,
  )
  val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
  val coroutineScope = rememberCoroutineScope()

  val sheetPeekHeight = 32.dp
  val isSheetExpanded = sheetState.currentValue == SheetValue.Expanded ||
    sheetState.targetValue == SheetValue.Expanded

  BottomSheetScaffold(
    modifier = modifier.fillMaxSize(),
    scaffoldState = scaffoldState,
    sheetPeekHeight = sheetPeekHeight,
    sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    sheetTonalElevation = 6.dp,
    sheetShadowElevation = 12.dp,
    sheetDragHandle = null,
    containerColor = MaterialTheme.colorScheme.background,
    sheetContent = {
      RollHistorySheetContent(
        history = history,
        isExpanded = isSheetExpanded,
        peekHeight = sheetPeekHeight,
        onToggleSheet = {
          coroutineScope.launch {
            if (sheetState.currentValue == SheetValue.Expanded) {
              sheetState.partialExpand()
            } else {
              sheetState.expand()
            }
          }
        },
        onClearHistory = { history.clear() },
      )
    },
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        // Upper Area: Central Result Display
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 16.dp, vertical = 6.dp),
          contentAlignment = Alignment.Center,
        ) {
          val latestRoll = history.firstOrNull()
          RollResultHero(
            latestRoll = latestRoll,
            selectedDie = selectedDie,
            diceCount = diceCount,
            modifierValue = modifierValue,
            currentNotation = currentNotation,
            onTapToRoll = ::performRoll,
            modifier = Modifier.fillMaxWidth(),
          )
        }

        // Bottom Fixed Controls: Roll Mode, Die Selector, Dice Count, Modifier, and Roll Button right above the History Panel
        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = MaterialTheme.colorScheme.surfaceContainerLow,
          tonalElevation = 2.dp,
          shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(
                start = 16.dp,
                end = 16.dp,
                top = 10.dp,
                bottom = 10.dp + sheetPeekHeight,
              ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            AnimatedVisibility(visible = selectedDie == DieType.D20 && diceCount == 1) {
              SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                RollMode.entries.forEachIndexed { index, mode ->
                  SegmentedButton(
                    selected = rollMode == mode,
                    onClick = { rollMode = mode },
                    shape = SegmentedButtonDefaults.itemShape(
                      index = index,
                      count = RollMode.entries.size,
                    ),
                  ) {
                    Text(mode.label)
                  }
                }
              }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
              val totalDice = DieType.entries.size
              val targetSpacing = if (maxWidth < 320.dp) 2.dp else 4.dp
              val maxButtonSize = 40.dp
              val calculatedSize = (maxWidth - (targetSpacing * (totalDice - 1))) / totalDice
              val buttonSize = minOf(maxButtonSize, calculatedSize)
              val spacing = if (totalDice > 1) {
                minOf(targetSpacing, (maxWidth - (buttonSize * totalDice)) / (totalDice - 1))
              } else {
                targetSpacing
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                DieType.entries.forEach { die ->
                  DieSelectionButton(
                    die = die,
                    isSelected = die == selectedDie,
                    onClick = { selectedDie = die },
                    size = buttonSize,
                  )
                }
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              StepperCard(
                label = "Dice Count",
                inlinePrefix = "Count",
                valueText = "${diceCount}${selectedDie.label}",
                onDecrement = { if (diceCount > 1) diceCount-- },
                onIncrement = { if (diceCount < 20) diceCount++ },
                modifier = Modifier.weight(1f),
              )
              StepperCard(
                label = "Modifier",
                inlinePrefix = "Mod",
                valueText = if (modifierValue >= 0) "+$modifierValue" else "$modifierValue",
                onDecrement = { if (modifierValue > -20) modifierValue-- },
                onIncrement = { if (modifierValue < 20) modifierValue++ },
                modifier = Modifier.weight(1f),
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              // Load button: load (folder open) icon, rounded corners, no text
              FilledTonalIconButton(
                onClick = { showSavedRollsSheet = true },
                modifier = Modifier
                  .size(56.dp)
                  .semantics { contentDescription = "Load saved rolls" }
                  .testTag("dice_roller_load_button"),
                shape = CircleShape,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                  containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                  contentColor = MaterialTheme.colorScheme.primary,
                ),
              ) {
                Icon(
                  imageVector = Icons.Default.FolderOpen,
                  contentDescription = "Load saved rolls",
                  modifier = Modifier.size(24.dp),
                )
              }

              // Roll button: rounded corners
              Button(
                onClick = ::performRoll,
                modifier = Modifier
                  .weight(1f)
                  .height(56.dp)
                  .testTag("dice_roller_roll_button"),
                shape = CircleShape,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
              ) {
                PolyhedralDieIcon(
                  dieType = selectedDie,
                  color = LocalContentColor.current,
                  modifier = Modifier
                    .padding(end = 8.dp)
                    .size(22.dp),
                )
                Text(
                  text = "Roll $currentNotation",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }

              // Save button: floppy disk icon, rounded corners, no text
              FilledTonalIconButton(
                onClick = { showSaveDialog = true },
                modifier = Modifier
                  .size(56.dp)
                  .semantics { contentDescription = "Save roll" }
                  .testTag("dice_roller_save_button"),
                shape = CircleShape,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                  containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                  contentColor = MaterialTheme.colorScheme.primary,
                ),
              ) {
                Icon(
                  imageVector = Icons.Default.Save,
                  contentDescription = "Save roll",
                  modifier = Modifier.size(24.dp),
                )
              }
            }
          }
        }
      }

      // Scrim overlay behind history sheet when expanded
      AnimatedVisibility(
        visible = isSheetExpanded,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(200)),
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .pointerInput(Unit) {
              detectTapGestures {
                coroutineScope.launch {
                  sheetState.partialExpand()
                }
              }
            },
        )
      }
    }
  }

  if (showSaveDialog) {
    SaveRollDialog(
      dieType = selectedDie,
      diceCount = diceCount,
      modifierValue = modifierValue,
      rollMode = effectiveRollMode,
      currentNotation = currentNotation,
      onDismiss = { showSaveDialog = false },
      onSave = { name ->
        val newSavedRoll = SavedDiceRoll(
          id = java.util.UUID.randomUUID().toString(),
          name = name,
          dieType = selectedDie,
          diceCount = diceCount,
          modifier = modifierValue,
          rollMode = effectiveRollMode,
        )
        savedRolls.add(0, newSavedRoll)
        onSaveRolls(savedRolls.toList())
        showSaveDialog = false
      },
    )
  }

  if (showSavedRollsSheet) {
    SavedRollsSheet(
      savedRolls = savedRolls,
      onRollSaved = { savedRoll ->
        showSavedRollsSheet = false
        rollPreset(savedRoll)
      },
      onDeleteSaved = { id ->
        savedRolls.removeAll { it.id == id }
        onSaveRolls(savedRolls.toList())
      },
      onDismiss = { showSavedRollsSheet = false },
    )
  }
}

@Composable
private fun SaveRollDialog(
  dieType: DieType,
  diceCount: Int,
  modifierValue: Int,
  rollMode: RollMode,
  currentNotation: String,
  onDismiss: () -> Unit,
  onSave: (name: String) -> Unit,
) {
  var rollName by rememberSaveable { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Save,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Save Roll",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Enter a name to save this roll preset",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Context Preview Card
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              PolyhedralDieIcon(
                dieType = dieType,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = currentNotation,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              val detailText = buildString {
                append("${diceCount}x ${dieType.label.uppercase()}")
                if (modifierValue != 0) {
                  append(if (modifierValue > 0) " (+$modifierValue)" else " ($modifierValue)")
                }
                if (rollMode != RollMode.NORMAL) {
                  append(" • ${rollMode.label}")
                }
              }
              Text(
                text = detailText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        // Input Field
        OutlinedTextField(
          value = rollName,
          onValueChange = { rollName = it },
          label = { Text("Roll Name") },
          placeholder = { Text("e.g., Attack Roll, Sneak Attack, Fireball") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Save,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          trailingIcon = {
            if (rollName.isNotEmpty()) {
              IconButton(onClick = { rollName = "" }) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Clear text",
                )
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("save_roll_name_input"),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(rollName.trim()) },
        enabled = rollName.trim().isNotEmpty(),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("save_roll_confirm_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("Save")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("save_roll_dismiss_button"),
      ) {
        Text("Cancel")
      }
    },
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedRollsSheet(
  savedRolls: List<SavedDiceRoll>,
  onRollSaved: (SavedDiceRoll) -> Unit,
  onDeleteSaved: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    dragHandle = {
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
      ) {
        BottomSheetDefaults.DragHandle()
      }
    },
    modifier = Modifier.testTag("saved_rolls_sheet"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .padding(bottom = 32.dp),
    ) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Saved Rolls",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Tap a roll to quickly roll it",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(32.dp)
            .testTag("saved_rolls_close_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
          )
        }
      }

      HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(bottom = 12.dp),
      )

      if (savedRolls.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp),
          contentAlignment = Alignment.Center,
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Icon(
              imageVector = Icons.Default.FolderOpen,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
              modifier = Modifier.size(48.dp),
            )
            Text(
              text = "No saved rolls yet",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Configure your dice and tap the Save button to save frequent rolls for fast access.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 24.dp),
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_rolls_list"),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          items(savedRolls, key = { it.id }) { savedRoll ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onRollSaved(savedRoll) }
                .testTag("saved_roll_item_${savedRoll.id}"),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              ),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                  contentAlignment = Alignment.Center,
                ) {
                  PolyhedralDieIcon(
                    dieType = savedRoll.dieType,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = savedRoll.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = savedRoll.notation,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                  )
                }

                Button(
                  onClick = { onRollSaved(savedRoll) },
                  shape = RoundedCornerShape(10.dp),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                  modifier = Modifier
                    .height(36.dp)
                    .testTag("saved_roll_action_${savedRoll.id}"),
                ) {
                  Text(
                    text = "Roll",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                  )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                  onClick = { onDeleteSaved(savedRoll.id) },
                  modifier = Modifier
                    .size(36.dp)
                    .testTag("saved_roll_delete_${savedRoll.id}"),
                ) {
                  Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete ${savedRoll.name}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp),
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RollResultHero(
  latestRoll: DiceRollResult?,
  selectedDie: DieType,
  diceCount: Int = 1,
  modifierValue: Int = 0,
  currentNotation: String,
  onTapToRoll: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isNat20 = latestRoll?.isNatural20 == true
  val isNat1 = latestRoll?.isNatural1 == true

  val containerColor = when {
    isNat20 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    isNat1 -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
    else -> MaterialTheme.colorScheme.surfaceContainerHigh
  }
  val accentColor = when {
    isNat20 -> MaterialTheme.colorScheme.primary
    isNat1 -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.tertiary
  }
  val dieFaceBaseColor = when {
    isNat20 -> MaterialTheme.colorScheme.primary
    isNat1 -> MaterialTheme.colorScheme.error
    latestRoll != null -> MaterialTheme.colorScheme.primaryContainer
    else -> MaterialTheme.colorScheme.secondaryContainer
  }
  val dieStrokeColor = when {
    isNat20 -> MaterialTheme.colorScheme.primary
    isNat1 -> MaterialTheme.colorScheme.error
    latestRoll != null -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.65f)
  }
  val inscribedTextColor = when {
    isNat20 -> MaterialTheme.colorScheme.onPrimary
    isNat1 -> MaterialTheme.colorScheme.onError
    latestRoll != null -> MaterialTheme.colorScheme.onPrimaryContainer
    else -> MaterialTheme.colorScheme.onSecondaryContainer
  }

  // Animated tumble & bounce when a new roll arrives
  val rotationAnim = remember { Animatable(0f) }
  val scaleAnim = remember { Animatable(1f) }
  var previousRollId by remember { mutableStateOf(latestRoll?.id) }

  LaunchedEffect(latestRoll?.id) {
    if (latestRoll != null && latestRoll.id != previousRollId) {
      previousRollId = latestRoll.id
      launch {
        rotationAnim.snapTo(-180f)
        rotationAnim.animateTo(
          targetValue = 0f,
          animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow,
          ),
        )
      }
      launch {
        scaleAnim.snapTo(0.68f)
        scaleAnim.animateTo(
          targetValue = 1f,
          animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
          ),
        )
      }
    }
  }

  Card(
    onClick = onTapToRoll,
    modifier = modifier.padding(vertical = 2.dp),
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = BorderStroke(
      width = if (isNat20 || isNat1) 2.dp else 1.dp,
      color = if (isNat20 || isNat1) {
        accentColor
      } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
      },
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
  ) {
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.radialGradient(
            colors = listOf(
              accentColor.copy(alpha = if (isNat20 || isNat1) 0.22f else 0.10f),
              Color.Transparent,
            ),
          ),
        ),
    ) {
      val hasMiniDiceRow = latestRoll != null
      val totalDiceShown = if (latestRoll != null) {
        latestRoll.keptRolls.size + latestRoll.droppedRolls.size
      } else {
        0
      }
      val miniRowEstimate = when {
        !hasMiniDiceRow -> 0.dp
        totalDiceShown > 7 -> 66.dp
        else -> 42.dp
      }
      val nonDieChromeEstimate = 112.dp + miniRowEstimate
      val dieBoxSize = if (maxHeight != Dp.Infinity) {
        (maxHeight - nonDieChromeEstimate).coerceIn(72.dp, 136.dp)
      } else {
        136.dp
      }
      val isCompactDie = dieBoxSize < 104.dp

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = if (isCompactDie) 10.dp else 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (isCompactDie) 6.dp else 8.dp),
      ) {
        // Top Header: Notation Pill + Mode / Crit Badges
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              PolyhedralDieIcon(
                dieType = latestRoll?.dieType ?: selectedDie,
                color = accentColor,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = latestRoll?.notation ?: currentNotation,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
          }

          when {
            isNat20 -> {
              Badge(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
              ) {
                Text(
                  text = "★ CRITICAL HIT · NAT 20!",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.ExtraBold,
                )
              }
            }
            isNat1 -> {
              Badge(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
              ) {
                Text(
                  text = "✦ CRITICAL MISS · NAT 1!",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.ExtraBold,
                )
              }
            }
            latestRoll != null && latestRoll.rollMode != RollMode.NORMAL -> {
              Badge(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
              ) {
                Text(
                  text = latestRoll.rollMode.label.uppercase(),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
            else -> {
              Text(
                text = if (latestRoll == null) "TAP TO ROLL" else "ROLL #${latestRoll.id}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        // Centerpiece: Large Shaded Polyhedral Die with Inscribed Total
        val activeDie = latestRoll?.dieType ?: selectedDie
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.size(dieBoxSize),
        ) {
          CentralInscribedDieCanvas(
            dieType = activeDie,
            baseColor = dieFaceBaseColor,
            strokeColor = dieStrokeColor,
            glowColor = accentColor,
            isCritical = isNat20 || isNat1,
            modifier = Modifier
              .fillMaxSize()
              .graphicsLayer {
                rotationZ = rotationAnim.value
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
              },
          )

          AnimatedContent(
            targetState = latestRoll,
            transitionSpec = {
              (scaleIn(
                initialScale = 0.55f,
                animationSpec = spring(
                  dampingRatio = Spring.DampingRatioMediumBouncy,
                  stiffness = Spring.StiffnessMedium,
                ),
              ) + fadeIn(tween(180))) togetherWith (scaleOut(targetScale = 0.8f) + fadeOut(tween(120)))
            },
            label = "InscribedDieResultAnimation",
          ) { roll ->
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.offset(
                y = if (activeDie == DieType.D4) (dieBoxSize * 0.045f) else 0.dp,
              ),
            ) {
              if (roll == null) {
                Text(
                  text = selectedDie.label.uppercase(),
                  style = if (isCompactDie) {
                    MaterialTheme.typography.headlineSmall
                  } else {
                    MaterialTheme.typography.headlineMedium
                  },
                  fontWeight = FontWeight.Black,
                  color = inscribedTextColor,
                  textAlign = TextAlign.Center,
                )
              } else {
                val isWideNumber = roll.total >= 100 || roll.total <= -10
                Text(
                  text = roll.total.toString(),
                  style = when {
                    isCompactDie && isWideNumber -> MaterialTheme.typography.headlineSmall
                    isCompactDie -> MaterialTheme.typography.headlineLarge
                    isWideNumber -> MaterialTheme.typography.headlineLarge
                    else -> MaterialTheme.typography.displayMedium
                  },
                  fontWeight = FontWeight.Black,
                  color = inscribedTextColor,
                  textAlign = TextAlign.Center,
                )
              }
            }
          }
        }

        // Breakdown Section: Mini Dice Tokens + Modifier OR Ready Range Indicator
        if (latestRoll == null) {
          val minPossible = diceCount * 1 + modifierValue
          val maxPossible = diceCount * selectedDie.sides + modifierValue
          val avgPossible = diceCount * (selectedDie.sides + 1) / 2.0 + modifierValue
          RollRangeIndicator(
            minTotal = minPossible,
            maxTotal = maxPossible,
            avgTotal = avgPossible,
            currentTotal = null,
            accentColor = accentColor,
          )
        } else {
          RollMiniDiceRow(roll = latestRoll)

          val minPossible = latestRoll.diceCount * 1 + latestRoll.modifier
          val maxPossible = latestRoll.diceCount * latestRoll.dieType.sides + latestRoll.modifier
          val avgPossible = latestRoll.diceCount * (latestRoll.dieType.sides + 1) / 2.0 + latestRoll.modifier
          RollRangeIndicator(
            minTotal = minPossible,
            maxTotal = maxPossible,
            avgTotal = avgPossible,
            currentTotal = latestRoll.total,
            accentColor = accentColor,
          )
        }
      }
    }
  }
}

@Composable
private fun CentralInscribedDieCanvas(
  dieType: DieType,
  baseColor: Color,
  strokeColor: Color,
  glowColor: Color,
  isCritical: Boolean,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val cx = size.width / 2f
    val cy = when (dieType) {
      DieType.D4 -> size.height * 0.56f
      else -> size.height / 2f
    }
    val outerRadius = when (dieType) {
      DieType.D4, DieType.D6 -> size.minDimension * 0.44f
      else -> size.minDimension * 0.42f
    }

    // Soft outer glow halo
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(
          glowColor.copy(alpha = if (isCritical) 0.40f else 0.24f),
          glowColor.copy(alpha = 0.06f),
          Color.Transparent,
        ),
        center = Offset(cx, cy),
        radius = size.minDimension * 0.50f,
      ),
      radius = size.minDimension * 0.50f,
      center = Offset(cx, cy),
    )

    val sides = dieType.polygonSides
    val rotationOffset = when (dieType) {
      DieType.D6 -> Math.PI / 4.0
      else -> -Math.PI / 2.0
    }

    val outerVertices = List(sides) { i ->
      val angle = rotationOffset + (2.0 * Math.PI * i / sides)
      Offset(
        x = cx + (outerRadius * cos(angle)).toFloat(),
        y = cy + (outerRadius * sin(angle)).toFloat(),
      )
    }

    // Inner face vertices create 3D beveled perimeter facets and a spacious central face for the number
    val innerRadius = outerRadius * when (dieType) {
      DieType.D4 -> 0.66f
      DieType.D8 -> 0.68f
      DieType.D6 -> 0.66f
      else -> 0.64f
    }
    val innerVertices = List(sides) { i ->
      val angle = rotationOffset + (2.0 * Math.PI * i / sides)
      Offset(
        x = cx + (innerRadius * cos(angle)).toFloat(),
        y = cy + (innerRadius * sin(angle)).toFloat(),
      )
    }

    // Light direction from top-left (-135 degrees) for 3D facet shading
    val lightAngle = -3.0 * Math.PI / 4.0

    // Draw each perimeter bevel facet with directional lighting
    for (i in 0 until sides) {
      val next = (i + 1) % sides
      val o1 = outerVertices[i]
      val o2 = outerVertices[next]
      val in2 = innerVertices[next]
      val in1 = innerVertices[i]

      val midX = (o1.x + o2.x + in1.x + in2.x) / 4f
      val midY = (o1.y + o2.y + in1.y + in2.y) / 4f
      val facetAngle = atan2((midY - cy).toDouble(), (midX - cx).toDouble())
      val alignment = cos(facetAngle - lightAngle).toFloat() // -1f (shadow) to +1f (highlight)

      val facetColor = if (alignment >= 0f) {
        lerp(baseColor, Color.White, 0.12f + 0.22f * alignment)
      } else {
        lerp(baseColor, Color.Black, 0.10f + 0.26f * (-alignment))
      }

      val facetPath = Path().apply {
        moveTo(o1.x, o1.y)
        lineTo(o2.x, o2.y)
        lineTo(in2.x, in2.y)
        lineTo(in1.x, in1.y)
        close()
      }
      drawPath(path = facetPath, color = facetColor, style = Fill)
      drawPath(
        path = facetPath,
        color = strokeColor.copy(alpha = 0.45f),
        style = Stroke(
          width = size.minDimension * 0.014f,
          join = StrokeJoin.Round,
        ),
      )
    }

    // Central inscribed table face with radial depth gradient
    val innerPath = Path().apply {
      innerVertices.forEachIndexed { index, pt ->
        if (index == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
      }
      close()
    }
    val centerHighlight = lerp(baseColor, Color.White, 0.16f)
    val centerEdge = lerp(baseColor, Color.Black, 0.12f)
    drawPath(
      path = innerPath,
      brush = Brush.radialGradient(
        colors = listOf(centerHighlight, baseColor, centerEdge),
        center = Offset(cx - innerRadius * 0.18f, cy - innerRadius * 0.18f),
        radius = innerRadius * 1.15f,
      ),
      style = Fill,
    )

    // Inner table outline
    drawPath(
      path = innerPath,
      color = strokeColor.copy(alpha = 0.7f),
      style = Stroke(
        width = size.minDimension * 0.02f,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
      ),
    )

    // Outer silhouette stroke
    val outerPath = Path().apply {
      outerVertices.forEachIndexed { index, pt ->
        if (index == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
      }
      close()
    }
    drawPath(
      path = outerPath,
      color = strokeColor,
      style = Stroke(
        width = size.minDimension * 0.032f,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
      ),
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RollMiniDiceRow(
  roll: DiceRollResult,
  modifier: Modifier = Modifier,
) {
  val totalDiceShown = roll.keptRolls.size + roll.droppedRolls.size
  val tokenSize = if (totalDiceShown > 7) 30.dp else 36.dp

  FlowRow(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    roll.keptRolls.forEach { dieValue ->
      MiniDieToken(
        dieType = roll.dieType,
        value = dieValue,
        isDropped = false,
        isMaxFace = dieValue == roll.dieType.sides,
        isMinFace = dieValue == 1 && roll.dieType == DieType.D20,
        size = tokenSize,
      )
    }

    roll.droppedRolls.forEach { droppedValue ->
      MiniDieToken(
        dieType = roll.dieType,
        value = droppedValue,
        isDropped = true,
        isMaxFace = false,
        isMinFace = false,
        size = tokenSize,
      )
    }

    if (roll.modifier != 0) {
      val modText = if (roll.modifier > 0) "+${roll.modifier}" else "${roll.modifier}"
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)),
        modifier = Modifier.height(tokenSize),
      ) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.padding(horizontal = 10.dp),
        ) {
          Text(
            text = modText,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
          )
        }
      }
    }
  }
}

@Composable
private fun MiniDieToken(
  dieType: DieType,
  value: Int,
  isDropped: Boolean,
  isMaxFace: Boolean,
  isMinFace: Boolean,
  size: Dp,
  modifier: Modifier = Modifier,
) {
  val fillColor = when {
    isDropped -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
    isMaxFace -> MaterialTheme.colorScheme.primaryContainer
    isMinFace -> MaterialTheme.colorScheme.errorContainer
    else -> MaterialTheme.colorScheme.secondaryContainer
  }
  val strokeColor = when {
    isDropped -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    isMaxFace -> MaterialTheme.colorScheme.primary
    isMinFace -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.65f)
  }
  val textColor = when {
    isDropped -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    isMaxFace -> MaterialTheme.colorScheme.onPrimaryContainer
    isMinFace -> MaterialTheme.colorScheme.onErrorContainer
    else -> MaterialTheme.colorScheme.onSecondaryContainer
  }

  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
      .size(size)
      .alpha(if (isDropped) 0.6f else 1f),
  ) {
    PolyhedralDieIcon(
      dieType = dieType,
      color = strokeColor,
      fillColor = fillColor,
      facetColor = strokeColor.copy(alpha = 0.2f),
      strokeWidthFraction = if (isMaxFace || isMinFace) 0.085f else 0.065f,
      modifier = Modifier.fillMaxSize(),
    )
    Text(
      text = value.toString(),
      style = if (value >= 100) {
        MaterialTheme.typography.labelSmall
      } else {
        MaterialTheme.typography.labelMedium
      },
      fontWeight = FontWeight.ExtraBold,
      color = textColor,
      textDecoration = if (isDropped) TextDecoration.LineThrough else TextDecoration.None,
      modifier = when (dieType) {
        DieType.D4 -> Modifier.offset(y = 2.dp)
        DieType.D10 -> Modifier.offset(y = 1.dp)
        else -> Modifier
      },
    )
    if (isDropped) {
      val strikeColor = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
      Canvas(modifier = Modifier.fillMaxSize()) {
        val pad = this.size.minDimension * 0.22f
        drawLine(
          color = strikeColor,
          start = Offset(pad, this.size.height - pad),
          end = Offset(this.size.width - pad, pad),
          strokeWidth = this.size.minDimension * 0.07f,
          cap = StrokeCap.Round,
        )
      }
    }
  }
}

@Composable
private fun RollRangeIndicator(
  minTotal: Int,
  maxTotal: Int,
  avgTotal: Double,
  currentTotal: Int?,
  accentColor: Color,
  modifier: Modifier = Modifier,
) {
  val span = (maxTotal - minTotal).coerceAtLeast(1)
  val rawFraction = if (currentTotal != null) {
    ((currentTotal - minTotal).toFloat() / span.toFloat()).coerceIn(0f, 1f)
  } else {
    0.5f
  }
  val animatedFraction by animateFloatAsState(
    targetValue = rawFraction,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioNoBouncy,
      stiffness = Spring.StiffnessMediumLow,
    ),
    label = "RangeFractionAnimation",
  )

  val avgFormatted = if (avgTotal % 1.0 == 0.0) {
    avgTotal.toInt().toString()
  } else {
    String.format(java.util.Locale.US, "%.1f", avgTotal)
  }

  val percentileText = if (currentTotal != null) {
    val pct = (rawFraction * 100f).roundToInt()
    val tier = when {
      currentTotal == maxTotal -> "MAX!"
      currentTotal == minTotal -> "MIN"
      pct >= 75 -> "HIGH"
      pct <= 25 -> "LOW"
      else -> "AVG"
    }
    "$pct% · $tier"
  } else {
    null
  }

  val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
  val tickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
  val thumbCenterColor = MaterialTheme.colorScheme.surface

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
      Box(
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          text = "Min $minTotal",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.align(Alignment.CenterStart),
        )
        Text(
          text = "Avg $avgFormatted",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.align(Alignment.Center),
        )
        Row(
          modifier = Modifier.align(Alignment.CenterEnd),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          if (percentileText != null) {
            Surface(
              shape = RoundedCornerShape(50),
              color = accentColor.copy(alpha = 0.15f),
            ) {
              Text(
                text = percentileText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
              )
            }
          }
          Text(
            text = "Max $maxTotal",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      // Custom Range Bar with Midpoint Tick and Glowing Marker
      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp),
      ) {
        val barHeight = size.height * 0.65f
        val barTop = (size.height - barHeight) / 2f
        val cornerRadius = CornerRadius(barHeight / 2f, barHeight / 2f)

        // Full track
        drawRoundRect(
          color = trackColor,
          topLeft = Offset(0f, barTop),
          size = Size(size.width, barHeight),
          cornerRadius = cornerRadius,
        )

        if (currentTotal != null) {
          val fillWidth = (size.width * animatedFraction).coerceAtLeast(barHeight)
          drawRoundRect(
            brush = Brush.horizontalGradient(
              colors = listOf(
                accentColor.copy(alpha = 0.45f),
                accentColor,
              ),
              startX = 0f,
              endX = fillWidth,
            ),
            topLeft = Offset(0f, barTop),
            size = Size(fillWidth, barHeight),
            cornerRadius = cornerRadius,
          )
        }

        // Midpoint (Average) tick mark
        val midX = size.width * 0.5f
        drawLine(
          color = tickColor,
          start = Offset(midX, 0f),
          end = Offset(midX, size.height),
          strokeWidth = 2.dp.toPx(),
          cap = StrokeCap.Round,
        )

        // Thumb indicator for current roll
        if (currentTotal != null) {
          val thumbRadius = size.height * 0.6f
          val thumbX = (size.width * animatedFraction).coerceIn(thumbRadius, size.width - thumbRadius)
          drawCircle(
            color = accentColor.copy(alpha = 0.3f),
            radius = thumbRadius * 1.4f,
            center = Offset(thumbX, size.height / 2f),
          )
          drawCircle(
            color = accentColor,
            radius = thumbRadius,
            center = Offset(thumbX, size.height / 2f),
          )
          drawCircle(
            color = thumbCenterColor,
            radius = thumbRadius * 0.42f,
            center = Offset(thumbX, size.height / 2f),
          )
        }
      }
    }
  }
}

@Composable
private fun RollHistorySheetContent(
  history: List<DiceRollResult>,
  isExpanded: Boolean,
  peekHeight: Dp = 32.dp,
  onToggleSheet: () -> Unit,
  onClearHistory: () -> Unit,
) {
  var sortOrder by rememberSaveable { mutableStateOf(HistorySortOrder.NEWEST_FIRST) }
  var filterDie by rememberSaveable { mutableStateOf<DieType?>(null) }
  var sortMenuExpanded by remember { mutableStateOf(false) }
  var filterMenuExpanded by remember { mutableStateOf(false) }

  val availableDice = remember(history.toList()) {
    DieType.entries.filter { die -> history.any { it.dieType == die } }
  }

  val activeFilterDie = filterDie?.takeIf { it in availableDice }

  val displayedHistory = remember(history.toList(), sortOrder, activeFilterDie) {
    val filtered = if (activeFilterDie != null) {
      history.filter { it.dieType == activeFilterDie }
    } else {
      history
    }
    when (sortOrder) {
      HistorySortOrder.NEWEST_FIRST -> filtered.sortedByDescending { it.id }
      HistorySortOrder.OLDEST_FIRST -> filtered.sortedBy { it.id }
      HistorySortOrder.HIGHEST_TOTAL -> filtered.sortedByDescending { it.total }
      HistorySortOrder.LOWEST_TOTAL -> filtered.sortedBy { it.total }
    }
  }

  val arrowRotation by animateFloatAsState(
    targetValue = if (isExpanded) 180f else 0f,
    label = "HistoryChevronRotation",
  )

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight(0.60f),
  ) {
    // Peek Header Bar (Drag or Tap to Pull Up Over the Roller)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .height(peekHeight)
        .clickable(onClick = onToggleSheet)
        .testTag("dice_history_panel_toggle")
        .padding(horizontal = 20.dp, vertical = 3.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      // Drag handle pill
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
      ) {
        Box(
          modifier = Modifier
            .width(28.dp)
            .height(2.5.dp)
            .background(
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
              shape = CircleShape,
            ),
        )
      }

      Spacer(modifier = Modifier.height(2.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(15.dp),
          )
          Text(
            text = "Roll History",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
          if (history.isNotEmpty()) {
            Badge(
              containerColor = MaterialTheme.colorScheme.primaryContainer,
              contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
              Text(
                text = history.size.toString(),
                modifier = Modifier.padding(horizontal = 4.dp),
              )
            }
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          if (!isExpanded && history.isNotEmpty()) {
            Text(
              text = "Last: ${history.first().total}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = "Toggle history panel",
            modifier = Modifier
              .size(18.dp)
              .rotate(arrowRotation),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

    // Expanded History Content
    if (history.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(24.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "No rolls yet — roll some dice to build your adventure log!",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
        )
      }
    } else {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // Sort Button
          Box {
            FilterChip(
              selected = sortOrder != HistorySortOrder.NEWEST_FIRST,
              onClick = { sortMenuExpanded = true },
              label = { Text(text = sortOrder.label) },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.SwapVert,
                  contentDescription = "Sort rolls",
                  modifier = Modifier.size(16.dp),
                )
              },
              trailingIcon = {
                Icon(
                  imageVector = Icons.Default.ArrowDropDown,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
              },
            )
            DropdownMenu(
              expanded = sortMenuExpanded,
              onDismissRequest = { sortMenuExpanded = false },
            ) {
              HistorySortOrder.entries.forEach { option ->
                DropdownMenuItem(
                  text = { Text(option.label) },
                  onClick = {
                    sortOrder = option
                    sortMenuExpanded = false
                  },
                  trailingIcon = if (sortOrder == option) {
                    {
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                      )
                    }
                  } else {
                    null
                  },
                )
              }
            }
          }

          // Filter Button
          Box {
            FilterChip(
              selected = activeFilterDie != null,
              onClick = { filterMenuExpanded = true },
              label = { Text(text = activeFilterDie?.label ?: "All Dice") },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.FilterList,
                  contentDescription = "Filter rolls by die",
                  modifier = Modifier.size(16.dp),
                )
              },
              trailingIcon = {
                Icon(
                  imageVector = Icons.Default.ArrowDropDown,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
              },
            )
            DropdownMenu(
              expanded = filterMenuExpanded,
              onDismissRequest = { filterMenuExpanded = false },
            ) {
              DropdownMenuItem(
                text = { Text("All Dice") },
                onClick = {
                  filterDie = null
                  filterMenuExpanded = false
                },
                trailingIcon = if (activeFilterDie == null) {
                  {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      modifier = Modifier.size(18.dp),
                    )
                  }
                } else {
                  null
                },
              )
              availableDice.forEach { die ->
                DropdownMenuItem(
                  text = { Text(die.label) },
                  onClick = {
                    filterDie = die
                    filterMenuExpanded = false
                  },
                  trailingIcon = if (activeFilterDie == die) {
                    {
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                      )
                    }
                  } else {
                    null
                  },
                )
              }
            }
          }
        }

        IconButton(
          onClick = {
            filterDie = null
            onClearHistory()
          },
        ) {
          Icon(
            imageVector = Icons.Default.DeleteSweep,
            contentDescription = "Clear roll history",
            tint = MaterialTheme.colorScheme.error,
          )
        }
      }

      if (displayedHistory.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(24.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = "No ${activeFilterDie?.label ?: ""} rolls in history.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          items(displayedHistory, key = { it.id }) { item ->
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surfaceContainer,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                  Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(36.dp),
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(
                        text = "#${item.id}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                      )
                    }
                  }
                  Column {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                      Text(
                        text = item.notation,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                      )
                      if (item.isNatural20) {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                          Text("NAT 20")
                        }
                      } else if (item.isNatural1) {
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                          Text("NAT 1")
                        }
                      }
                    }
                    Text(
                      text = item.breakdown,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
                Text(
                  text = item.total.toString(),
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.ExtraBold,
                  color = when {
                    item.isNatural20 -> MaterialTheme.colorScheme.primary
                    item.isNatural1 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                  },
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StepperCard(
  label: String,
  inlinePrefix: String,
  valueText: String,
  onDecrement: () -> Unit,
  onIncrement: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
      FilledIconButton(
        onClick = onDecrement,
        modifier = Modifier.size(32.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
          containerColor = MaterialTheme.colorScheme.secondaryContainer,
          contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
      ) {
        Icon(
          imageVector = Icons.Default.Remove,
          contentDescription = "Decrease $label",
          modifier = Modifier.size(16.dp),
        )
      }
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = inlinePrefix,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
        )
        Text(
          text = valueText,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
        )
      }
      FilledIconButton(
        onClick = onIncrement,
        modifier = Modifier.size(32.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
          containerColor = MaterialTheme.colorScheme.secondaryContainer,
          contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Increase $label",
          modifier = Modifier.size(16.dp),
        )
      }
    }
  }
}

@Composable
private fun DieSelectionButton(
  die: DieType,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  size: Dp = 40.dp,
) {
  val fillColor = if (isSelected) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.surfaceContainerHigh
  }
  val strokeColor = if (isSelected) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.onSurfaceVariant
  }
  val textColor = if (isSelected) {
    MaterialTheme.colorScheme.onPrimary
  } else {
    MaterialTheme.colorScheme.onSurface
  }
  val facetColor = if (isSelected) {
    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
  } else {
     strokeColor.copy(alpha = 0.15f)
  }

  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .clickable(
        role = Role.Button,
        onClickLabel = "Select ${die.label}",
        onClick = onClick,
      )
      .semantics {
        contentDescription = die.label
        selected = isSelected
      },
    contentAlignment = Alignment.Center,
  ) {
    PolyhedralDieIcon(
      dieType = die,
      color = strokeColor,
      fillColor = fillColor,
      facetColor = facetColor,
      strokeWidthFraction = if (isSelected) 0.075f else 0.055f,
      modifier = Modifier.fillMaxSize(),
    )
    val textStyle = when {
      die == DieType.D100 -> MaterialTheme.typography.labelSmall
      size < 36.dp -> MaterialTheme.typography.labelSmall
      else -> MaterialTheme.typography.labelMedium
    }
    Text(
      text = die.sides.toString(),
      style = textStyle,
      fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
      color = textColor,
      textAlign = TextAlign.Center,
      modifier = when (die) {
        DieType.D4 -> Modifier.offset(y = (size * 0.05f).coerceAtLeast(1.dp))
        DieType.D10 -> Modifier.offset(y = (size * 0.02f).coerceAtLeast(0.5.dp))
        else -> Modifier
      },
    )
  }
}

@Composable
private fun PolyhedralDieIcon(
  dieType: DieType,
  color: Color,
  modifier: Modifier = Modifier,
  fillColor: Color = Color.Transparent,
  facetColor: Color = color.copy(alpha = color.alpha * 0.65f),
  strokeWidthFraction: Float = 0.08f,
) {
  Canvas(modifier = modifier) {
    val cx = size.width / 2f
    val cy = when (dieType) {
      DieType.D4 -> size.height * 0.58f
      else -> size.height / 2f
    }
    val radius = when (dieType) {
      DieType.D4, DieType.D6 -> size.minDimension * 0.47f
      else -> size.minDimension * 0.44f
    }
    val sides = dieType.polygonSides
    val rotationOffset = when (dieType) {
      DieType.D6 -> Math.PI / 4.0
      else -> -Math.PI / 2.0
    }

    val vertices = List(sides) { i ->
      val angle = rotationOffset + (2.0 * Math.PI * i / sides)
      Offset(
        x = cx + (radius * cos(angle)).toFloat(),
        y = cy + (radius * sin(angle)).toFloat(),
      )
    }

    val outlinePath = Path().apply {
      vertices.forEachIndexed { index, pt ->
        if (index == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
      }
      close()
    }

    if (fillColor != Color.Transparent) {
      drawPath(
        path = outlinePath,
        color = fillColor,
        style = Fill,
      )
    }

    // Draw subtle inner facet lines for d8, d12, d20
    if (dieType == DieType.D20 || dieType == DieType.D8 || dieType == DieType.D12) {
      val innerRadius = radius * 0.52f
      val innerVertices = List(3) { i ->
        val angle = -Math.PI / 2.0 + (2.0 * Math.PI * i / 3.0)
        Offset(
          x = cx + (innerRadius * cos(angle)).toFloat(),
          y = cy + (innerRadius * sin(angle)).toFloat(),
        )
      }
      val innerPath = Path().apply {
        innerVertices.forEachIndexed { index, pt ->
          if (index == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
        }
        close()
      }
      drawPath(
        path = innerPath,
        color = facetColor,
        style = Stroke(
          width = size.minDimension * 0.05f,
          cap = StrokeCap.Round,
          join = StrokeJoin.Round,
        ),
      )
    }

    drawPath(
      path = outlinePath,
      color = color,
      style = Stroke(
        width = size.minDimension * strokeWidthFraction,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
      ),
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DiceRollerScreenPreview() {
  ToolboxTheme {
    DiceRollerScreen()
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, widthDp = 320, name = "Dice Roller - Narrow Screen 320dp")
@Composable
fun DiceRollerScreenNarrowPreview() {
  ToolboxTheme {
    DiceRollerScreen()
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Dice Roller - History Sheet Expanded")
@Composable
fun DiceRollerScreenExpandedPreview() {
  ToolboxTheme {
    DiceRollerScreen(
      initialSheetValue = SheetValue.Expanded,
      initialHistory = listOf(
        DiceRollResult(
          id = 3L,
          dieType = DieType.D20,
          diceCount = 1,
          modifier = 3,
          rollMode = RollMode.NORMAL,
          keptRolls = listOf(17),
          droppedRolls = emptyList(),
          total = 20,
        ),
        DiceRollResult(
          id = 2L,
          dieType = DieType.D6,
          diceCount = 2,
          modifier = 0,
          rollMode = RollMode.NORMAL,
          keptRolls = listOf(4, 5),
          droppedRolls = emptyList(),
          total = 9,
        ),
        DiceRollResult(
          id = 1L,
          dieType = DieType.D20,
          diceCount = 1,
          modifier = 0,
          rollMode = RollMode.ADVANTAGE,
          keptRolls = listOf(20),
          droppedRolls = listOf(7),
          total = 20,
        ),
      ),
    )
  }
}

@Preview(showBackground = true, name = "Roll Result - Empty Ready State")
@Composable
fun RollResultHeroEmptyPreview() {
  ToolboxTheme {
    Surface(modifier = Modifier.padding(16.dp)) {
      RollResultHero(
        latestRoll = null,
        selectedDie = DieType.D20,
        diceCount = 1,
        modifierValue = 3,
        currentNotation = "1d20 + 3",
        onTapToRoll = {},
      )
    }
  }
}

@Preview(showBackground = true, name = "Roll Result - Multi-Die Roll")
@Composable
fun RollResultHeroMultiDiePreview() {
  ToolboxTheme {
    Surface(modifier = Modifier.padding(16.dp)) {
      RollResultHero(
        latestRoll = DiceRollResult(
          id = 4L,
          dieType = DieType.D8,
          diceCount = 4,
          modifier = 4,
          rollMode = RollMode.NORMAL,
          keptRolls = listOf(8, 6, 5, 3),
          droppedRolls = emptyList(),
          total = 26,
        ),
        selectedDie = DieType.D8,
        diceCount = 4,
        modifierValue = 4,
        currentNotation = "4d8 + 4",
        onTapToRoll = {},
      )
    }
  }
}

@Preview(showBackground = true, name = "Roll Result - Natural 20 Advantage")
@Composable
fun RollResultHeroNat20Preview() {
  ToolboxTheme {
    Surface(modifier = Modifier.padding(16.dp)) {
      RollResultHero(
        latestRoll = DiceRollResult(
          id = 7L,
          dieType = DieType.D20,
          diceCount = 1,
          modifier = 5,
          rollMode = RollMode.ADVANTAGE,
          keptRolls = listOf(20),
          droppedRolls = listOf(8),
          total = 25,
        ),
        selectedDie = DieType.D20,
        diceCount = 1,
        modifierValue = 5,
        currentNotation = "1d20 (Adv) + 5",
        onTapToRoll = {},
      )
    }
  }
}

@Preview(showBackground = true, name = "Roll Result - Natural 1 Disadvantage")
@Composable
fun RollResultHeroNat1Preview() {
  ToolboxTheme {
    Surface(modifier = Modifier.padding(16.dp)) {
      RollResultHero(
        latestRoll = DiceRollResult(
          id = 8L,
          dieType = DieType.D20,
          diceCount = 1,
          modifier = -1,
          rollMode = RollMode.DISADVANTAGE,
          keptRolls = listOf(1),
          droppedRolls = listOf(16),
          total = 0,
        ),
        selectedDie = DieType.D20,
        diceCount = 1,
        modifierValue = -1,
        currentNotation = "1d20 (Dis) - 1",
        onTapToRoll = {},
      )
    }
  }
}
