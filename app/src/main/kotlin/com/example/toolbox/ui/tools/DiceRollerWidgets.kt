package com.example.toolbox.ui.tools

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Bundle
import android.widget.RemoteViews
import androidx.compose.runtime.Immutable
import com.example.toolbox.R
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.createStandaloneToolIntent
import com.example.toolbox.ui.theme.drawWidgetBackgroundTextureOverlay
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * Snapshot of the D20 hero card state, active [RollMode] (Disadvantage / Normal / Advantage),
 * latest D20 roll result, and Material 3 palette rendered by the D20 Roller Home Screen AppWidget
 * ([DiceRollerWidgetProvider]).
 */
@Immutable
data class DiceRollerWidgetState(
  val rollMode: RollMode,
  val latestRoll: DiceRollResult?,
  val rollCounter: Int,
  val notationText: String,
  val statusBadgeText: String,
  val breakdownText: String,
  val isNatural20: Boolean,
  val isNatural1: Boolean,
  val palette: TodoWidgetPalette,
  val errorArgb: Int,
  val onErrorArgb: Int,
  val errorContainerArgb: Int,
  val onErrorContainerArgb: Int,
  val hexagonIconButtons: Boolean = palette.hexagonIconButtons,
)

/**
 * Central state resolver, D20 roller engine, 3D polyhedral die Canvas renderer, and RemoteViews
 * builder for the D20 Roller Home Screen AppWidget ([DiceRollerWidgetProvider]):
 * - Modeled after `RollResultHero` in [DiceRollerScreen]: features the top notation pill,
 *   critical hit (`NAT 20!`) / critical miss (`NAT 1!`) / mode status badge, 3D beveled D20 with
 *   directional lighting and inscribed total, and kept/dropped roll breakdown.
 * - Includes an interactive 3-way **Disadvantage | Normal | Advantage** segmented selector at the
 *   bottom of the card.
 * - Tapping the central D20 rolls `1d20` right on the home screen; tapping any mode segment switches
 *   to that mode and rolls immediately; tapping the top-left notation pill opens the full
 *   **Dice Roller** tool.
 */
object DiceRollerWidgets {

  const val ACTION_WIDGET_REFRESH = "com.example.toolbox.dice.ACTION_WIDGET_REFRESH"
  const val ACTION_WIDGET_ROLL_D20 = "com.example.toolbox.dice.ACTION_WIDGET_ROLL_D20"
  const val ACTION_WIDGET_SET_ROLL_MODE = "com.example.toolbox.dice.ACTION_WIDGET_SET_ROLL_MODE"

  const val EXTRA_ROLL_MODE = "extra_dice_widget_roll_mode"
  const val EXTRA_ROLL_ON_MODE_CHANGE = "extra_dice_widget_roll_on_mode_change"

  const val KEY_WIDGET_ROLL_MODE = "dice_widget_roll_mode"
  const val KEY_WIDGET_ROLL_COUNTER = "dice_widget_roll_counter"
  const val KEY_WIDGET_LATEST_ROLL_JSON = "dice_widget_latest_d20_roll_json"

  const val WIDGET_CORNER_RADIUS_DP = 16f

  private const val REQUEST_CODE_OPEN_DICE_TOOL = 8100
  private const val REQUEST_CODE_ROLL_D20 = 8101
  private const val REQUEST_CODE_MODE_DISADVANTAGE = 8102
  private const val REQUEST_CODE_MODE_NORMAL = 8103
  private const val REQUEST_CODE_MODE_ADVANTAGE = 8104

  @Volatile
  private var inMemoryRollMode: RollMode = RollMode.NORMAL

  @Volatile
  private var inMemoryRollCounter: Int = 0

  @Volatile
  private var inMemoryLatestRoll: DiceRollResult? = null

  fun clearInMemoryStateForTesting() {
    inMemoryRollMode = RollMode.NORMAL
    inMemoryRollCounter = 0
    inMemoryLatestRoll = null
  }

  /**
   * Serializes a [DiceRollResult] to [JSONObject] for widget persistence.
   */
  fun rollResultToJson(roll: DiceRollResult): JSONObject = JSONObject().apply {
    put("id", roll.id)
    put("dieType", roll.dieType.name)
    put("diceCount", roll.diceCount)
    put("modifier", roll.modifier)
    put("rollMode", roll.rollMode.name)
    val keptArr = JSONArray()
    roll.keptRolls.forEach { keptArr.put(it) }
    put("keptRolls", keptArr)
    val droppedArr = JSONArray()
    roll.droppedRolls.forEach { droppedArr.put(it) }
    put("droppedRolls", droppedArr)
    put("total", roll.total)
    if (!roll.label.isNullOrBlank()) {
      put("label", roll.label)
    }
  }

  /**
   * Deserializes a [DiceRollResult] from [JSONObject], or returns `null` if invalid.
   */
  fun rollResultFromJson(json: JSONObject?): DiceRollResult? {
    if (json == null) return null
    val id = json.optLong("id", 1L)
    val dieType = try {
      DieType.valueOf(json.optString("dieType", DieType.D20.name))
    } catch (_: Throwable) {
      DieType.D20
    }
    val diceCount = json.optInt("diceCount", 1).coerceIn(1, 20)
    val modifier = json.optInt("modifier", 0).coerceIn(-20, 40)
    val rollMode = try {
      RollMode.valueOf(json.optString("rollMode", RollMode.NORMAL.name))
    } catch (_: Throwable) {
      RollMode.NORMAL
    }
    val keptRolls = mutableListOf<Int>()
    val keptArr = json.optJSONArray("keptRolls")
    if (keptArr != null) {
      for (i in 0 until keptArr.length()) {
        val v = keptArr.optInt(i, -1)
        if (v >= 1) keptRolls.add(v)
      }
    }
    if (keptRolls.isEmpty()) return null
    val droppedRolls = mutableListOf<Int>()
    val droppedArr = json.optJSONArray("droppedRolls")
    if (droppedArr != null) {
      for (i in 0 until droppedArr.length()) {
        val v = droppedArr.optInt(i, -1)
        if (v >= 1) droppedRolls.add(v)
      }
    }
    val total = json.optInt("total", keptRolls.sum() + modifier)
    val label = json.optString("label", "").takeIf { it.isNotBlank() }
    return DiceRollResult(
      id = id,
      dieType = dieType,
      diceCount = diceCount,
      modifier = modifier,
      rollMode = rollMode,
      keptRolls = keptRolls,
      droppedRolls = droppedRolls,
      total = total,
      label = label,
    )
  }

  fun loadRollMode(context: Context? = null): RollMode {
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(DiceRollerPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_WIDGET_ROLL_MODE, null)
        if (!raw.isNullOrBlank()) {
          val parsed = RollMode.entries.firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) }
          if (parsed != null) {
            inMemoryRollMode = parsed
            return parsed
          }
        }
      } catch (_: Throwable) {}
    }
    return inMemoryRollMode
  }

  fun saveRollMode(
    context: Context?,
    mode: RollMode,
  ) {
    inMemoryRollMode = mode
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(DiceRollerPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_WIDGET_ROLL_MODE, mode.name).apply()
      } catch (_: Throwable) {}
    }
  }

  fun loadLatestRoll(context: Context? = null): DiceRollResult? {
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(DiceRollerPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_WIDGET_LATEST_ROLL_JSON, null)
        if (!raw.isNullOrBlank()) {
          val parsed = rollResultFromJson(JSONObject(raw))
          if (parsed != null) {
            inMemoryLatestRoll = parsed
            inMemoryRollCounter = max(inMemoryRollCounter, parsed.id.toInt())
            return parsed
          }
        }
      } catch (_: Throwable) {}
    }
    return inMemoryLatestRoll
  }

  fun saveLatestRoll(
    context: Context?,
    roll: DiceRollResult?,
    counter: Int,
  ) {
    inMemoryLatestRoll = roll
    inMemoryRollCounter = counter
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(DiceRollerPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit().putInt(KEY_WIDGET_ROLL_COUNTER, counter)
        if (roll == null) {
          editor.remove(KEY_WIDGET_LATEST_ROLL_JSON)
        } else {
          editor.putString(KEY_WIDGET_LATEST_ROLL_JSON, rollResultToJson(roll).toString())
        }
        editor.apply()
      } catch (_: Throwable) {}
    }
  }

  /**
   * Performs a `1d20` roll using [mode] (Disadvantage, Normal, or Advantage), persists the result,
   * and refreshes any pinned D20 Roller widgets.
   */
  fun rollD20FromWidget(
    context: Context? = null,
    mode: RollMode = loadRollMode(context),
    random: Random = Random.Default,
    fixedFirstRoll: Int? = null,
    fixedSecondRoll: Int? = null,
  ): DiceRollResult {
    saveRollMode(context, mode)
    val persistedCounter = if (context != null) {
      try {
        context.getSharedPreferences(DiceRollerPreferences.PREFS_NAME, Context.MODE_PRIVATE)
          .getInt(KEY_WIDGET_ROLL_COUNTER, inMemoryRollCounter)
      } catch (_: Throwable) {
        inMemoryRollCounter
      }
    } else {
      inMemoryRollCounter
    }
    val nextCounter = max(persistedCounter, inMemoryRollCounter) + 1

    val r1 = (fixedFirstRoll ?: random.nextInt(1, DieType.D20.sides + 1)).coerceIn(1, 20)
    val kept: List<Int>
    val dropped: List<Int>
    when (mode) {
      RollMode.NORMAL -> {
        kept = listOf(r1)
        dropped = emptyList()
      }
      RollMode.ADVANTAGE -> {
        val r2 = (fixedSecondRoll ?: random.nextInt(1, DieType.D20.sides + 1)).coerceIn(1, 20)
        kept = listOf(maxOf(r1, r2))
        dropped = listOf(minOf(r1, r2))
      }
      RollMode.DISADVANTAGE -> {
        val r2 = (fixedSecondRoll ?: random.nextInt(1, DieType.D20.sides + 1)).coerceIn(1, 20)
        kept = listOf(minOf(r1, r2))
        dropped = listOf(maxOf(r1, r2))
      }
    }

    val result = DiceRollResult(
      id = nextCounter.toLong(),
      dieType = DieType.D20,
      diceCount = 1,
      modifier = 0,
      rollMode = mode,
      keptRolls = kept,
      droppedRolls = dropped,
      total = kept.sum(),
    )
    saveLatestRoll(context, result, nextCounter)
    if (context != null) {
      updateAllWidgets(context)
    }
    return result
  }

  /**
   * Formats the D20 notation pill text (`1d20`, `1d20 (Adv)`, or `1d20 (Dis)`).
   */
  fun formatD20Notation(mode: RollMode): String = when (mode) {
    RollMode.NORMAL -> "1d20"
    RollMode.ADVANTAGE -> "1d20 (Adv)"
    RollMode.DISADVANTAGE -> "1d20 (Dis)"
  }

  /**
   * Resolves the current [DiceRollerWidgetState].
   */
  fun resolveWidgetState(
    context: Context? = null,
    rollModeOverride: RollMode? = null,
    latestRollOverride: DiceRollResult? = null,
  ): DiceRollerWidgetState {
    val mode = rollModeOverride ?: loadRollMode(context)
    val latestRoll = latestRollOverride ?: loadLatestRoll(context)
    val palette = TodoListWidgets.resolveWidgetPalette(context)

    val isNat20 = latestRoll?.isNatural20 == true
    val isNat1 = latestRoll?.isNatural1 == true
    val notationText = formatD20Notation(mode)

    val statusBadgeText = when {
      isNat20 -> "★ CRITICAL HIT · NAT 20!"
      isNat1 -> "✦ CRITICAL MISS · NAT 1!"
      latestRoll != null && latestRoll.rollMode != RollMode.NORMAL ->
        latestRoll.rollMode.label.uppercase()
      latestRoll != null -> "ROLL #${latestRoll.id}"
      else -> "TAP TO ROLL"
    }

    val breakdownText = when {
      latestRoll == null -> "Tap die to roll $notationText"
      latestRoll.droppedRolls.isNotEmpty() ->
        "Kept ${latestRoll.keptRolls.first()} · Dropped ${latestRoll.droppedRolls.first()}"
      isNat20 -> "Natural 20 • Critical Success!"
      isNat1 -> "Natural 1 • Critical Failure!"
      else -> "Rolled ${latestRoll.total} on 1d20 • Range 1–20"
    }

    // Material 3 error tokens matching dark/light mode (using bitwise RGB extraction so it works in JVM unit tests)
    val surfR = (palette.surfaceArgb ushr 16) and 0xFF
    val surfG = (palette.surfaceArgb ushr 8) and 0xFF
    val surfB = palette.surfaceArgb and 0xFF
    val isDarkSurface = (surfR * 0.299f + surfG * 0.587f + surfB * 0.114f) < 128f
    val errorArgb = if (isDarkSurface) 0xFFFFB4AB.toInt() else 0xFFBA1A1A.toInt()
    val onErrorArgb = if (isDarkSurface) 0xFF690005.toInt() else 0xFFFFFFFF.toInt()
    val errorContainerArgb = if (isDarkSurface) 0xFF93000A.toInt() else 0xFFFFDAD6.toInt()
    val onErrorContainerArgb = if (isDarkSurface) 0xFFFFDAD6.toInt() else 0xFF410002.toInt()

    return DiceRollerWidgetState(
      rollMode = mode,
      latestRoll = latestRoll,
      rollCounter = latestRoll?.id?.toInt() ?: inMemoryRollCounter,
      notationText = notationText,
      statusBadgeText = statusBadgeText,
      breakdownText = breakdownText,
      isNatural20 = isNat20,
      isNatural1 = isNat1,
      palette = palette,
      errorArgb = errorArgb,
      onErrorArgb = onErrorArgb,
      errorContainerArgb = errorContainerArgb,
      onErrorContainerArgb = onErrorContainerArgb,
    )
  }

  private fun blendArgb(baseArgb: Int, overlayArgb: Int, fraction: Float): Int {
    val t = fraction.coerceIn(0f, 1f)
    val inv = 1f - t
    val baseR = (baseArgb ushr 16) and 0xFF
    val baseG = (baseArgb ushr 8) and 0xFF
    val baseB = baseArgb and 0xFF
    val overR = (overlayArgb ushr 16) and 0xFF
    val overG = (overlayArgb ushr 8) and 0xFF
    val overB = overlayArgb and 0xFF
    val r = (baseR * inv + overR * t).roundToInt().coerceIn(0, 255)
    val g = (baseG * inv + overG * t).roundToInt().coerceIn(0, 255)
    val b = (baseB * inv + overB * t).roundToInt().coerceIn(0, 255)
    return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
  }

  private fun withAlpha(argb: Int, alphaFraction: Float): Int {
    val a = (alphaFraction.coerceIn(0f, 1f) * 255f).roundToInt().coerceIn(0, 255)
    return (argb and 0x00FFFFFF) or (a shl 24)
  }

  /**
   * Renders the `16.dp` rounded hero card background [Bitmap] with radial glow and dynamic border
   * stroke (highlighting in `primary` on Natural 20 or `error` on Natural 1, matching `RollResultHero`).
   */
  fun renderHeroCardBackgroundBitmap(
    state: DiceRollerWidgetState,
    widthPx: Int = 440,
    heightPx: Int = 340,
    cornerRadiusPx: Float = 32f,
    borderWidthPx: Float = 2f,
  ): Bitmap? {
    val safeW = widthPx.coerceIn(140, 1200)
    val safeH = heightPx.coerceIn(110, 1200)
    return try {
      val palette = state.palette
      val bitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      val w = safeW.toFloat()
      val h = safeH.toFloat()
      val fillRect = RectF(0f, 0f, w, h)
      val fillRadius = min(cornerRadiusPx, min(w, h) * 0.5f)

      val baseContainerArgb = when {
        state.isNatural20 -> blendArgb(palette.surfaceArgb, palette.primaryContainerArgb, 0.42f)
        state.isNatural1 -> blendArgb(palette.surfaceArgb, state.errorContainerArgb, 0.48f)
        else -> palette.surfaceArgb
      }
      val accentArgb = when {
        state.isNatural20 -> palette.primaryArgb
        state.isNatural1 -> state.errorArgb
        else -> palette.primaryArgb
      }

      val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = baseContainerArgb
      }
      canvas.drawRoundRect(fillRect, fillRadius, fillRadius, fillPaint)

      // Subtle center radial glow matching RollResultHero
      val glowAlpha = if (state.isNatural20 || state.isNatural1) 0.24f else 0.11f
      val glowRadius = max(w, h) * 0.58f
      val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        shader = RadialGradient(
          w * 0.5f,
          h * 0.46f,
          glowRadius,
          intArrayOf(
            withAlpha(accentArgb, glowAlpha),
            withAlpha(accentArgb, 0.0f),
          ),
          floatArrayOf(0f, 1f),
          Shader.TileMode.CLAMP,
        )
      }
      canvas.drawRoundRect(fillRect, fillRadius, fillRadius, glowPaint)

      drawWidgetBackgroundTextureOverlay(
        bitmap = bitmap,
        cornerRadiusPx = fillRadius,
        texture = palette.backgroundTexture,
        primaryColor = androidx.compose.ui.graphics.Color(palette.primaryArgb),
        tertiaryColor = androidx.compose.ui.graphics.Color(palette.tertiaryArgb),
        secondaryColor = androidx.compose.ui.graphics.Color(palette.secondaryArgb),
        onSurfaceColor = androidx.compose.ui.graphics.Color(palette.onSurfaceArgb),
        isDarkTheme = palette.isDarkTheme,
      )

      val effectiveBorderWidth = if (state.isNatural20 || state.isNatural1) {
        max(borderWidthPx * 1.6f, 2.5f)
      } else {
        borderWidthPx
      }
      if (effectiveBorderWidth > 0f) {
        val strokeInset = max(effectiveBorderWidth * 1.25f, 1.5f)
        val borderRect = RectF(strokeInset, strokeInset, w - strokeInset, h - strokeInset)
        val borderRadius = max(
          0f,
          min(cornerRadiusPx + effectiveBorderWidth, min(borderRect.width(), borderRect.height()) * 0.5f),
        )
        val borderColor = when {
          state.isNatural20 -> palette.primaryArgb
          state.isNatural1 -> state.errorArgb
          else -> palette.borderArgb
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.STROKE
          strokeWidth = effectiveBorderWidth
          color = borderColor
        }
        canvas.drawRoundRect(borderRect, borderRadius, borderRadius, borderPaint)
      }

      bitmap
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Renders the 3D beveled polyhedral D20 centerpiece [Bitmap] with directional facet shading,
   * radial glow halo, and inscribed roll total (or `"D20"` before rolling), matching
   * `CentralInscribedDieCanvas` in [DiceRollerScreen].
   */
  fun renderD20HeroDieBitmap(
    state: DiceRollerWidgetState,
    sizePx: Int = 240,
  ): Bitmap? {
    val safeSize = sizePx.coerceIn(96, 640)
    return try {
      val palette = state.palette
      val bitmap = Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      val s = safeSize.toFloat()
      val cx = s * 0.5f
      val cy = s * 0.5f
      val outerRadius = s * 0.42f
      val innerRadius = outerRadius * 0.64f

      val isCritical = state.isNatural20 || state.isNatural1
      val hasRoll = state.latestRoll != null

      val dieFaceBaseArgb = when {
        state.isNatural20 -> palette.primaryArgb
        state.isNatural1 -> state.errorArgb
        hasRoll -> palette.primaryContainerArgb
        else -> palette.secondaryContainerArgb
      }
      val dieStrokeArgb = when {
        state.isNatural20 -> palette.primaryArgb
        state.isNatural1 -> state.errorArgb
        hasRoll -> palette.primaryArgb
        else -> withAlpha(palette.onSecondaryContainerArgb, 0.68f)
      }
      val inscribedTextArgb = when {
        state.isNatural20 -> palette.onPrimaryArgb
        state.isNatural1 -> state.onErrorArgb
        hasRoll -> palette.onPrimaryContainerArgb
        else -> palette.onSecondaryContainerArgb
      }
      val glowArgb = when {
        state.isNatural20 -> palette.primaryArgb
        state.isNatural1 -> state.errorArgb
        else -> palette.primaryArgb
      }

      // 1. Soft outer glow halo
      val haloRadius = s * 0.49f
      val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        shader = RadialGradient(
          cx,
          cy,
          haloRadius,
          intArrayOf(
            withAlpha(glowArgb, if (isCritical) 0.42f else 0.24f),
            withAlpha(glowArgb, 0.06f),
            Color.TRANSPARENT,
          ),
          floatArrayOf(0f, 0.72f, 1f),
          Shader.TileMode.CLAMP,
        )
      }
      canvas.drawCircle(cx, cy, haloRadius, haloPaint)

      // 2. D20 Hexagon outer & inner vertices (6 sides, top vertex at -PI/2)
      val sides = DieType.D20.polygonSides
      val rotationOffset = -Math.PI / 2.0
      val outerX = FloatArray(sides)
      val outerY = FloatArray(sides)
      val innerX = FloatArray(sides)
      val innerY = FloatArray(sides)

      for (i in 0 until sides) {
        val angle = rotationOffset + (2.0 * Math.PI * i / sides)
        val cosA = cos(angle).toFloat()
        val sinA = sin(angle).toFloat()
        outerX[i] = cx + outerRadius * cosA
        outerY[i] = cy + outerRadius * sinA
        innerX[i] = cx + innerRadius * cosA
        innerY[i] = cy + innerRadius * sinA
      }

      // 3. Perimeter bevel facets with directional lighting from top-left (-135 deg)
      val lightAngle = -3.0 * Math.PI / 4.0
      val facetFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
      }
      val facetStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = max(1f, s * 0.014f)
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        color = withAlpha(dieStrokeArgb, 0.45f)
      }

      for (i in 0 until sides) {
        val next = (i + 1) % sides
        val midX = (outerX[i] + outerX[next] + innerX[i] + innerX[next]) * 0.25f
        val midY = (outerY[i] + outerY[next] + innerY[i] + innerY[next]) * 0.25f
        val facetAngle = atan2((midY - cy).toDouble(), (midX - cx).toDouble())
        val alignment = cos(facetAngle - lightAngle).toFloat()

        val facetColor = if (alignment >= 0f) {
          blendArgb(dieFaceBaseArgb, Color.WHITE, 0.12f + 0.22f * alignment)
        } else {
          blendArgb(dieFaceBaseArgb, Color.BLACK, 0.10f + 0.26f * (-alignment))
        }

        val facetPath = Path().apply {
          moveTo(outerX[i], outerY[i])
          lineTo(outerX[next], outerY[next])
          lineTo(innerX[next], innerY[next])
          lineTo(innerX[i], innerY[i])
          close()
        }
        facetFillPaint.color = facetColor
        canvas.drawPath(facetPath, facetFillPaint)
        canvas.drawPath(facetPath, facetStrokePaint)
      }

      // 4. Central inscribed table face with radial depth gradient
      val innerPath = Path().apply {
        for (i in 0 until sides) {
          if (i == 0) moveTo(innerX[i], innerY[i]) else lineTo(innerX[i], innerY[i])
        }
        close()
      }
      val centerHighlight = blendArgb(dieFaceBaseArgb, Color.WHITE, 0.16f)
      val centerEdge = blendArgb(dieFaceBaseArgb, Color.BLACK, 0.12f)
      val innerFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        shader = RadialGradient(
          cx - innerRadius * 0.18f,
          cy - innerRadius * 0.18f,
          innerRadius * 1.15f,
          intArrayOf(centerHighlight, dieFaceBaseArgb, centerEdge),
          floatArrayOf(0f, 0.55f, 1f),
          Shader.TileMode.CLAMP,
        )
      }
      canvas.drawPath(innerPath, innerFillPaint)

      // 5. Inner table outline & outer silhouette stroke
      val innerStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = max(1.5f, s * 0.02f)
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        color = withAlpha(dieStrokeArgb, 0.72f)
      }
      canvas.drawPath(innerPath, innerStrokePaint)

      val outerPath = Path().apply {
        for (i in 0 until sides) {
          if (i == 0) moveTo(outerX[i], outerY[i]) else lineTo(outerX[i], outerY[i])
        }
        close()
      }
      val outerStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = max(2f, s * 0.032f)
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        color = dieStrokeArgb
      }
      canvas.drawPath(outerPath, outerStrokePaint)

      // 6. Inscribed Total or "D20" label in the center face
      val inscribedText = state.latestRoll?.total?.toString() ?: "D20"
      val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = inscribedTextArgb
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = if (state.latestRoll == null) s * 0.20f else s * 0.27f
      }
      val fontMetrics = textPaint.fontMetrics
      val textBaselineY = cy - (fontMetrics.ascent + fontMetrics.descent) * 0.5f
      canvas.drawText(inscribedText, cx, textBaselineY, textPaint)

      bitmap
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Renders a pill background [Bitmap] for the notation badge, status badge, or 3-way Advantage
   * selector segments (supporting horizontal hexagon shape when **Hexagonify** is enabled).
   */
  fun renderPillBitmap(
    fillArgb: Int,
    borderArgb: Int = Color.TRANSPARENT,
    widthPx: Int = 180,
    heightPx: Int = 64,
    borderWidthPx: Float = 0f,
    hexagonShape: Boolean = false,
  ): Bitmap? {
    val safeW = widthPx.coerceIn(48, 800)
    val safeH = heightPx.coerceIn(32, 300)
    return CameraWidgets.renderModePillBitmap(
      fillArgb = fillArgb,
      borderArgb = borderArgb,
      widthPx = safeW,
      heightPx = safeH,
      cornerRadiusPx = safeH * 0.5f,
      borderWidthPx = borderWidthPx,
      hexagonShape = hexagonShape,
      verticalHexagon = false,
    )
  }

  fun buildOpenDiceToolPendingIntent(context: Context): PendingIntent {
    val intent = createStandaloneToolIntent(context, Tool.DICE_ROLLER)
    return PendingIntent.getActivity(
      context,
      REQUEST_CODE_OPEN_DICE_TOOL,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  fun buildRollD20BroadcastPendingIntent(context: Context): PendingIntent {
    val intent = Intent(context, DiceRollerWidgetProvider::class.java).apply {
      action = ACTION_WIDGET_ROLL_D20
      setPackage(context.packageName)
    }
    return PendingIntent.getBroadcast(
      context,
      REQUEST_CODE_ROLL_D20,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  fun buildSetRollModeBroadcastPendingIntent(
    context: Context,
    mode: RollMode,
  ): PendingIntent {
    val intent = Intent(context, DiceRollerWidgetProvider::class.java).apply {
      action = ACTION_WIDGET_SET_ROLL_MODE
      putExtra(EXTRA_ROLL_MODE, mode.name)
      putExtra(EXTRA_ROLL_ON_MODE_CHANGE, true)
      setPackage(context.packageName)
    }
    val requestCode = when (mode) {
      RollMode.DISADVANTAGE -> REQUEST_CODE_MODE_DISADVANTAGE
      RollMode.NORMAL -> REQUEST_CODE_MODE_NORMAL
      RollMode.ADVANTAGE -> REQUEST_CODE_MODE_ADVANTAGE
    }
    return PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  /**
   * Builds the full [RemoteViews] for a [DiceRollerWidgetProvider] instance.
   */
  fun buildWidgetRemoteViews(
    context: Context,
    appWidgetId: Int,
    state: DiceRollerWidgetState = resolveWidgetState(context),
    options: Bundle? = null,
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_dice_roller)
    val palette = state.palette
    val density = try {
      context.resources.displayMetrics.density.coerceIn(1f, 4f)
    } catch (_: Throwable) {
      2f
    }

    val (widthDp, heightDp) = MusicPlayerWidgets.resolveWidgetSizeDp(
      context = context,
      options = options,
      defaultWidthDp = 220f,
      defaultHeightDp = 170f,
    )
    val rawWidthPx = (widthDp * density).coerceAtLeast(150f)
    val rawHeightPx = (heightDp * density).coerceAtLeast(120f)
    val maxDimPx = 1000f
    val maxRaw = max(rawWidthPx, rawHeightPx)
    val scaleFactor = if (maxRaw > maxDimPx) maxDimPx / maxRaw else 1f
    val bgWidthPx = (rawWidthPx * scaleFactor).roundToInt().coerceAtLeast(150)
    val bgHeightPx = (rawHeightPx * scaleFactor).roundToInt().coerceAtLeast(120)

    // 1. Hero Card Background (16dp corner radius + radial glow + crit/normal border)
    renderHeroCardBackgroundBitmap(
      state = state,
      widthPx = bgWidthPx,
      heightPx = bgHeightPx,
      cornerRadiusPx = WIDGET_CORNER_RADIUS_DP * density * scaleFactor,
      borderWidthPx = max(1f, 1f * density * scaleFactor),
    )?.let { bgBmp ->
      views.setImageViewBitmap(R.id.widget_dice_background, bgBmp)
    }

    // 2. Top-Start Notation Pill (e.g. "1d20", "1d20 (Adv)", "1d20 (Dis)")
    renderPillBitmap(
      fillArgb = palette.itemCardArgb,
      borderArgb = palette.borderArgb,
      widthPx = (96f * density).roundToInt(),
      heightPx = (26f * density).roundToInt(),
      borderWidthPx = max(1f, 1f * density),
      hexagonShape = state.hexagonIconButtons,
    )?.let { bmp ->
      views.setImageViewBitmap(R.id.widget_dice_notation_pill_bg, bmp)
    }
    val notationIconTint = when {
      state.isNatural20 -> palette.primaryArgb
      state.isNatural1 -> state.errorArgb
      else -> palette.primaryArgb
    }
    views.setInt(R.id.widget_dice_notation_icon, "setColorFilter", notationIconTint)
    views.setTextViewText(R.id.widget_dice_notation_text, state.notationText)
    views.setTextColor(R.id.widget_dice_notation_text, palette.onSurfaceArgb)

    // 3. Top-End Crit / Mode Status Badge
    val statusFillArgb = when {
      state.isNatural20 -> palette.primaryArgb
      state.isNatural1 -> state.errorArgb
      state.latestRoll != null && state.latestRoll.rollMode != RollMode.NORMAL ->
        palette.tertiaryContainerArgb
      else -> palette.itemCardArgb
    }
    val statusTextArgb = when {
      state.isNatural20 -> palette.onPrimaryArgb
      state.isNatural1 -> state.onErrorArgb
      state.latestRoll != null && state.latestRoll.rollMode != RollMode.NORMAL ->
        palette.onTertiaryContainerArgb
      else -> palette.onSurfaceVariantArgb
    }
    renderPillBitmap(
      fillArgb = statusFillArgb,
      borderArgb = if (state.isNatural20 || state.isNatural1) Color.TRANSPARENT else palette.borderArgb,
      widthPx = (140f * density).roundToInt(),
      heightPx = (24f * density).roundToInt(),
      borderWidthPx = if (state.isNatural20 || state.isNatural1) 0f else max(1f, 1f * density),
      hexagonShape = state.hexagonIconButtons,
    )?.let { bmp ->
      views.setImageViewBitmap(R.id.widget_dice_status_badge_bg, bmp)
    }
    views.setTextViewText(R.id.widget_dice_status_badge_text, state.statusBadgeText)
    views.setTextColor(R.id.widget_dice_status_badge_text, statusTextArgb)

    // 4. Centerpiece 3D Shaded Polyhedral D20 Die Bitmap & Breakdown
    val dieSizePx = (min(widthDp, heightDp - 64f).coerceIn(72f, 200f) * density)
      .roundToInt()
      .coerceIn(120, 420)
    renderD20HeroDieBitmap(
      state = state,
      sizePx = dieSizePx,
    )?.let { dieBmp ->
      views.setImageViewBitmap(R.id.widget_dice_hero_die_image, dieBmp)
    }
    views.setTextViewText(R.id.widget_dice_breakdown_text, state.breakdownText)
    views.setTextColor(
      R.id.widget_dice_breakdown_text,
      when {
        state.isNatural20 -> palette.primaryArgb
        state.isNatural1 -> state.errorArgb
        else -> palette.onSurfaceVariantArgb
      },
    )

    // 5. Bottom 3-Way Advantage Selector (Disadvantage | Normal | Advantage)
    val selectorTrackWidthPx = (bgWidthPx - (20f * density * scaleFactor)).roundToInt().coerceAtLeast(140)
    val selectorTrackHeightPx = (32f * density * scaleFactor).roundToInt().coerceAtLeast(48)
    renderPillBitmap(
      fillArgb = palette.itemCardArgb,
      borderArgb = palette.borderArgb,
      widthPx = selectorTrackWidthPx,
      heightPx = selectorTrackHeightPx,
      borderWidthPx = max(1f, 1f * density * scaleFactor),
      hexagonShape = state.hexagonIconButtons,
    )?.let { trackBmp ->
      views.setImageViewBitmap(R.id.widget_dice_mode_selector_bg, trackBmp)
    }

    val segWidthPx = (selectorTrackWidthPx / 3f).roundToInt().coerceAtLeast(48)
    val segHeightPx = (selectorTrackHeightPx - (4f * density * scaleFactor)).roundToInt().coerceAtLeast(36)
    val activeSegBmp = renderPillBitmap(
      fillArgb = palette.primaryContainerArgb,
      borderArgb = palette.primaryArgb,
      widthPx = segWidthPx,
      heightPx = segHeightPx,
      borderWidthPx = max(1f, 1f * density * scaleFactor),
      hexagonShape = state.hexagonIconButtons,
    )
    val inactiveSegBmp = renderPillBitmap(
      fillArgb = Color.TRANSPARENT,
      borderArgb = Color.TRANSPARENT,
      widthPx = segWidthPx,
      heightPx = segHeightPx,
      borderWidthPx = 0f,
      hexagonShape = state.hexagonIconButtons,
    )

    val useCompactModeLabels = widthDp < 210f
    val disLabel = if (useCompactModeLabels) "Dis" else "Disadvantage"
    val normLabel = "Normal"
    val advLabel = if (useCompactModeLabels) "Adv" else "Advantage"

    data class ModeSegmentSlot(
      val mode: RollMode,
      val label: String,
      val containerId: Int,
      val bgId: Int,
      val textId: Int,
    )

    val segments = listOf(
      ModeSegmentSlot(
        mode = RollMode.DISADVANTAGE,
        label = disLabel,
        containerId = R.id.widget_dice_mode_disadvantage,
        bgId = R.id.widget_dice_mode_disadvantage_bg,
        textId = R.id.widget_dice_mode_disadvantage_text,
      ),
      ModeSegmentSlot(
        mode = RollMode.NORMAL,
        label = normLabel,
        containerId = R.id.widget_dice_mode_normal,
        bgId = R.id.widget_dice_mode_normal_bg,
        textId = R.id.widget_dice_mode_normal_text,
      ),
      ModeSegmentSlot(
        mode = RollMode.ADVANTAGE,
        label = advLabel,
        containerId = R.id.widget_dice_mode_advantage,
        bgId = R.id.widget_dice_mode_advantage_bg,
        textId = R.id.widget_dice_mode_advantage_text,
      ),
    )

    for (seg in segments) {
      val isSelected = seg.mode == state.rollMode
      val segBmp = if (isSelected) activeSegBmp else inactiveSegBmp
      if (segBmp != null) {
        views.setImageViewBitmap(seg.bgId, segBmp)
      }
      views.setTextViewText(seg.textId, seg.label)
      views.setTextColor(
        seg.textId,
        if (isSelected) palette.onPrimaryContainerArgb else palette.onSurfaceVariantArgb,
      )
      views.setOnClickPendingIntent(
        seg.containerId,
        buildSetRollModeBroadcastPendingIntent(context, seg.mode),
      )
    }

    // Click listeners:
    // - Tapping the central D20 hero area or status badge rolls the D20 immediately on the home screen
    // - Tapping the top-left notation pill opens the full Dice Roller tool
    val rollPendingIntent = buildRollD20BroadcastPendingIntent(context)
    views.setOnClickPendingIntent(R.id.widget_dice_hero_tap_area, rollPendingIntent)
    views.setOnClickPendingIntent(R.id.widget_dice_hero_die_image, rollPendingIntent)
    views.setOnClickPendingIntent(R.id.widget_dice_status_badge, rollPendingIntent)
    views.setOnClickPendingIntent(
      R.id.widget_dice_notation_pill,
      buildOpenDiceToolPendingIntent(context),
    )

    return views
  }

  /**
   * Handles a widget broadcast action for [DiceRollerWidgetProvider].
   */
  fun handleWidgetAction(
    context: Context?,
    intent: Intent?,
  ): Boolean {
    val action = intent?.action ?: return false
    return when (action) {
      ACTION_WIDGET_REFRESH -> {
        if (context != null) updateAllWidgets(context)
        true
      }
      ACTION_WIDGET_ROLL_D20 -> {
        rollD20FromWidget(context = context)
        true
      }
      ACTION_WIDGET_SET_ROLL_MODE -> {
        val rawMode = intent.getStringExtra(EXTRA_ROLL_MODE) ?: return false
        val mode = RollMode.entries.firstOrNull { it.name.equals(rawMode.trim(), ignoreCase = true) }
          ?: return false
        val rollOnChange = intent.getBooleanExtra(EXTRA_ROLL_ON_MODE_CHANGE, true)
        saveRollMode(context, mode)
        if (rollOnChange) {
          rollD20FromWidget(context = context, mode = mode)
        } else if (context != null) {
          updateAllWidgets(context)
        }
        true
      }
      else -> false
    }
  }

  /**
   * Refreshes all pinned instances of [DiceRollerWidgetProvider].
   */
  fun updateAllWidgets(context: Context?) {
    val appCtx = context?.applicationContext ?: context ?: return
    try {
      val manager = AppWidgetManager.getInstance(appCtx) ?: return
      val component = ComponentName(appCtx, DiceRollerWidgetProvider::class.java)
      val widgetIds = try {
        manager.getAppWidgetIds(component) ?: IntArray(0)
      } catch (_: Throwable) {
        IntArray(0)
      }
      if (widgetIds.isEmpty()) return
      updateDiceRollerWidgets(appCtx, manager, widgetIds)
    } catch (_: Throwable) {}
  }

  fun updateDiceRollerWidgets(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
    state: DiceRollerWidgetState = resolveWidgetState(context),
  ) {
    for (appWidgetId in appWidgetIds) {
      try {
        val options = try {
          appWidgetManager.getAppWidgetOptions(appWidgetId)
        } catch (_: Throwable) {
          null
        }
        val views = buildWidgetRemoteViews(
          context = context,
          appWidgetId = appWidgetId,
          state = state,
          options = options,
        )
        appWidgetManager.updateAppWidget(appWidgetId, views)
      } catch (_: Throwable) {}
    }
  }
}

/**
 * Interactive D20 Roller Home Screen AppWidget modeled after `RollResultHero` in [DiceRollerScreen],
 * including the 3-way Disadvantage / Normal / Advantage selector.
 */
class DiceRollerWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    DiceRollerWidgets.updateDiceRollerWidgets(context, appWidgetManager, appWidgetIds)
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle,
  ) {
    DiceRollerWidgets.updateDiceRollerWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    val action = intent.action ?: return
    if (action.startsWith("com.example.toolbox.dice.ACTION_")) {
      DiceRollerWidgets.handleWidgetAction(context, intent)
    }
  }
}
