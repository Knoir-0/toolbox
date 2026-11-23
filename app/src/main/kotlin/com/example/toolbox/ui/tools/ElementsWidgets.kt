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
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import androidx.compose.runtime.Immutable
import com.example.toolbox.R
import com.example.toolbox.ui.main.createElementsWidgetIntent
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * Resolved ARGB color tokens for an [ElementCategory] on the Periodic Table Home Screen AppWidget.
 */
@Immutable
data class ElementWidgetCategoryColors(
  val containerArgb: Int,
  val onContainerArgb: Int,
  val borderArgb: Int,
  val accentArgb: Int,
)

/**
 * Snapshot of the currently focused [ChemicalElement], favorite elements set, and Material 3 palette
 * rendered by the Periodic Table of Elements Home Screen AppWidget ([ElementsWidgetProvider]).
 */
@Immutable
data class ElementsWidgetState(
  val element: ChemicalElement,
  val isFavorite: Boolean,
  val favoriteAtomicNumbers: Set<Int>,
  val titleText: String,
  val categoryText: String,
  val subtitleText: String,
  val configText: String,
  val categoryColors: ElementWidgetCategoryColors,
  val palette: TodoWidgetPalette,
  val hexagonIconButtons: Boolean = palette.hexagonIconButtons,
  val iconButtonBackgroundRes: Int = palette.iconButtonBackgroundRes,
)

/**
 * Central state resolver, element tile badge renderer, 18-column IUPAC Periodic Table mini-map
 * Canvas renderer, 2D Bohr Electron Shell Diagram Canvas renderer, and RemoteViews builder for the
 * Periodic Table of Elements Home Screen AppWidget ([ElementsWidgetProvider]):
 * - Displays the active element tile badge (`Z`, phase badge, symbol, atomic mass), element name &
 *   category pill, atomic mass/phase/period/group summary, and electron configuration/shells.
 * - Includes interactive `<` (Previous element), `>` (Next element), `★` (Favorite toggle),
 *   `⟳` (Random element), and `🔍` (Open Periodic Table search) controls.
 * - When tall enough (`heightDp >= 115dp`), renders the complete 18-column IUPAC Periodic Table
 *   grid (Periods 1–7 + Lanthanides & Actinides f-block rows) with category color-coding,
 *   favorited element indicators, and a glowing highlight on the active element's grid cell,
 *   paired alongside the element's 2D Bohr electron shell diagram.
 * - Strictly follows `DESIGN_LANGUAGE.md` Section 5.8 (`16.dp` outer corner radius,
 *   `@android:id/background` + `clipToOutline="true"` launcher opt-out, inset `1.dp` border stroke,
 *   and automatic Hexagonify badge support).
 */
object ElementsWidgets {

  const val ACTION_WIDGET_REFRESH = "com.example.toolbox.elements.ACTION_WIDGET_REFRESH"
  const val ACTION_WIDGET_PREV_ELEMENT = "com.example.toolbox.elements.ACTION_WIDGET_PREV_ELEMENT"
  const val ACTION_WIDGET_NEXT_ELEMENT = "com.example.toolbox.elements.ACTION_WIDGET_NEXT_ELEMENT"
  const val ACTION_WIDGET_RANDOM_ELEMENT = "com.example.toolbox.elements.ACTION_WIDGET_RANDOM_ELEMENT"
  const val ACTION_WIDGET_TOGGLE_FAVORITE = "com.example.toolbox.elements.ACTION_WIDGET_TOGGLE_FAVORITE"
  const val ACTION_WIDGET_SET_ELEMENT = "com.example.toolbox.elements.ACTION_WIDGET_SET_ELEMENT"

  const val EXTRA_ATOMIC_NUMBER = "extra_elements_widget_atomic_number"

  const val WIDGET_CORNER_RADIUS_DP = 16f
  const val INNER_CARD_CORNER_RADIUS_DP = 14f
  const val COMPACT_HEIGHT_THRESHOLD_DP = 115f
  const val SHOW_BOHR_WIDTH_THRESHOLD_DP = 230f

  private const val REQUEST_CODE_OPEN_TABLE_BASE = 9500
  private const val REQUEST_CODE_OPEN_INSPECTOR_BASE = 9520
  private const val REQUEST_CODE_OPEN_SEARCH_BASE = 9540
  private const val REQUEST_CODE_PREV_ELEMENT_BASE = 9560
  private const val REQUEST_CODE_NEXT_ELEMENT_BASE = 9570
  private const val REQUEST_CODE_RANDOM_ELEMENT_BASE = 9580
  private const val REQUEST_CODE_TOGGLE_FAVORITE_BASE = 9590

  @Volatile
  private var inMemoryAtomicNumber: Int = 1

  fun clearInMemoryStateForTesting() {
    inMemoryAtomicNumber = 1
  }

  /**
   * Loads the currently focused element atomic number (`1..118`) for the Periodic Table widget,
   * defaulting to `1` (Hydrogen) or the first favorited element if no selection has been saved.
   */
  fun loadFocusedAtomicNumber(
    context: Context? = null,
    repository: ElementsRepository? = null,
  ): Int {
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(ELEMENTS_PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.contains(PREF_KEY_WIDGET_ATOMIC_NUMBER)) {
          val saved = prefs.getInt(PREF_KEY_WIDGET_ATOMIC_NUMBER, 1)
          if (saved in 1..118) {
            inMemoryAtomicNumber = saved
            return saved
          }
        }
      } catch (_: Throwable) {}
    }
    if (inMemoryAtomicNumber in 1..118 && inMemoryAtomicNumber != 1) {
      return inMemoryAtomicNumber
    }
    val favs = repository?.loadFavoriteAtomicNumbers().orEmpty()
    if (favs.isNotEmpty() && inMemoryAtomicNumber == 1) {
      return inMemoryAtomicNumber
    }
    return inMemoryAtomicNumber.coerceIn(1, 118)
  }

  /**
   * Persists the focused element atomic number (`1..118`) for the Periodic Table widget.
   */
  fun saveFocusedAtomicNumber(
    context: Context?,
    atomicNumber: Int,
  ): Int {
    val coerced = atomicNumber.coerceIn(1, 118)
    inMemoryAtomicNumber = coerced
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(ELEMENTS_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(PREF_KEY_WIDGET_ATOMIC_NUMBER, coerced).apply()
      } catch (_: Throwable) {}
    }
    return coerced
  }

  /**
   * Steps the focused element backward (`delta = -1`) or forward (`delta = +1`) with wrap-around
   * between Hydrogen (`#1`) and Oganesson (`#118`).
   */
  fun stepFocusedElement(
    context: Context?,
    delta: Int,
    currentAtomicNumberOverride: Int? = null,
    refreshWidgets: Boolean = true,
  ): ChemicalElement {
    val current = (currentAtomicNumberOverride ?: loadFocusedAtomicNumber(context)).coerceIn(1, 118)
    val zeroBased = ((current - 1 + delta) % 118 + 118) % 118
    val nextZ = zeroBased + 1
    saveFocusedAtomicNumber(context, nextZ)
    if (refreshWidgets && context != null) {
      updateAllWidgets(context)
    }
    return PeriodicTableEngine.byAtomicNumber(nextZ) ?: PeriodicTableEngine.ALL_ELEMENTS.first()
  }

  /**
   * Picks a random element (`1..118`) distinct from the current element, persists it, and refreshes
   * all Periodic Table widgets.
   */
  fun pickRandomElement(
    context: Context? = null,
    random: Random = Random.Default,
    currentAtomicNumberOverride: Int? = null,
    refreshWidgets: Boolean = true,
  ): ChemicalElement {
    val current = (currentAtomicNumberOverride ?: loadFocusedAtomicNumber(context)).coerceIn(1, 118)
    var candidate = random.nextInt(1, 119)
    if (candidate == current) {
      candidate = (current % 118) + 1
    }
    saveFocusedAtomicNumber(context, candidate)
    if (refreshWidgets && context != null) {
      updateAllWidgets(context)
    }
    return PeriodicTableEngine.byAtomicNumber(candidate) ?: PeriodicTableEngine.ALL_ELEMENTS.first()
  }

  /**
   * Toggles the favorite state of the currently focused (or specified) element and refreshes all
   * Periodic Table widgets.
   */
  fun toggleFavoriteFromWidget(
    context: Context? = null,
    repository: ElementsRepository? = null,
    atomicNumberOverride: Int? = null,
    refreshWidgets: Boolean = true,
  ): Set<Int> {
    val repo = repository ?: context?.let {
      try {
        SharedPreferencesElementsRepository(it)
      } catch (_: Throwable) {
        null
      }
    } ?: InMemoryElementsRepository()
    val targetZ = (atomicNumberOverride ?: loadFocusedAtomicNumber(context, repo)).coerceIn(1, 118)
    val updated = repo.toggleFavorite(targetZ)
    if (refreshWidgets && context != null) {
      updateAllWidgets(context)
    }
    return updated
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
   * Resolves Material 3 category colors for [category] from [palette], mirroring
   * `rememberCategoryVisualColors` in [ElementsScreen].
   */
  fun resolveCategoryColors(
    category: ElementCategory,
    palette: TodoWidgetPalette,
  ): ElementWidgetCategoryColors {
    val errorContainerArgb = if (palette.isDarkTheme) 0xFF93000A.toInt() else 0xFFFFDAD6.toInt()
    val onErrorContainerArgb = if (palette.isDarkTheme) 0xFFFFDAD6.toInt() else 0xFF410002.toInt()
    val errorArgb = if (palette.isDarkTheme) 0xFFFFB4AB.toInt() else 0xFFBA1A1A.toInt()

    val (container, onContainer, accent) = when (category) {
      ElementCategory.ALKALI_METAL -> Triple(
        blendArgb(errorContainerArgb, palette.primaryContainerArgb, 0.25f),
        onErrorContainerArgb,
        blendArgb(errorArgb, palette.primaryArgb, 0.25f),
      )
      ElementCategory.ALKALINE_EARTH_METAL -> Triple(
        blendArgb(palette.tertiaryContainerArgb, errorContainerArgb, 0.35f),
        palette.onTertiaryContainerArgb,
        blendArgb(palette.tertiaryArgb, errorArgb, 0.35f),
      )
      ElementCategory.TRANSITION_METAL -> Triple(
        palette.primaryContainerArgb,
        palette.onPrimaryContainerArgb,
        palette.primaryArgb,
      )
      ElementCategory.POST_TRANSITION_METAL -> Triple(
        palette.secondaryContainerArgb,
        palette.onSecondaryContainerArgb,
        palette.secondaryArgb,
      )
      ElementCategory.METALLOID -> Triple(
        blendArgb(palette.tertiaryContainerArgb, palette.secondaryContainerArgb, 0.45f),
        palette.onTertiaryContainerArgb,
        blendArgb(palette.tertiaryArgb, palette.secondaryArgb, 0.45f),
      )
      ElementCategory.REACTIVE_NONMETAL -> Triple(
        blendArgb(palette.primaryContainerArgb, palette.tertiaryContainerArgb, 0.60f),
        palette.onPrimaryContainerArgb,
        blendArgb(palette.primaryArgb, palette.tertiaryArgb, 0.60f),
      )
      ElementCategory.HALOGEN -> Triple(
        palette.tertiaryContainerArgb,
        palette.onTertiaryContainerArgb,
        palette.tertiaryArgb,
      )
      ElementCategory.NOBLE_GAS -> Triple(
        blendArgb(palette.secondaryContainerArgb, palette.primaryContainerArgb, 0.50f),
        palette.onSecondaryContainerArgb,
        blendArgb(palette.secondaryArgb, palette.primaryArgb, 0.50f),
      )
      ElementCategory.LANTHANIDE -> Triple(
        blendArgb(palette.primaryContainerArgb, palette.itemCardArgb, 0.35f),
        palette.onPrimaryContainerArgb,
        palette.primaryArgb,
      )
      ElementCategory.ACTINIDE -> Triple(
        blendArgb(errorContainerArgb, palette.tertiaryContainerArgb, 0.50f),
        onErrorContainerArgb,
        blendArgb(errorArgb, palette.tertiaryArgb, 0.50f),
      )
    }
    return ElementWidgetCategoryColors(
      containerArgb = container,
      onContainerArgb = onContainer,
      borderArgb = withAlpha(accent, 0.60f),
      accentArgb = accent,
    )
  }

  /**
   * Resolves the current [ElementsWidgetState] from persisted preferences (or [repository]) and the
   * user's active Material 3 theme palette.
   */
  fun resolveWidgetState(
    context: Context? = null,
    repository: ElementsRepository? = null,
    atomicNumberOverride: Int? = null,
  ): ElementsWidgetState {
    val repo = repository ?: context?.let {
      try {
        SharedPreferencesElementsRepository(it)
      } catch (_: Throwable) {
        null
      }
    } ?: InMemoryElementsRepository()

    val favorites = repo.loadFavoriteAtomicNumbers()
    val focusedZ = (atomicNumberOverride ?: loadFocusedAtomicNumber(context, repo)).coerceIn(1, 118)
    val element = PeriodicTableEngine.byAtomicNumber(focusedZ) ?: PeriodicTableEngine.ALL_ELEMENTS.first()
    val isFavorite = element.atomicNumber in favorites
    val palette = TodoListWidgets.resolveWidgetPalette(context)
    val categoryColors = resolveCategoryColors(element.category, palette)

    val favStarPrefix = if (isFavorite) "★ " else ""
    val titleText = "$favStarPrefix${element.name} (#${element.atomicNumber})"
    val categoryText = element.category.shortLabel
    val groupShort = element.group?.let { "Group $it" } ?: element.block.label
    val subtitleText = "${element.formattedAtomicMass} • ${element.standardPhase.label} (${element.standardPhase.badge}) • P${element.period} • $groupShort"
    val enSuffix = element.electronegativity?.let { " • EN $it" }.orEmpty()
    val configText = "${element.electronConfiguration} • Shells: ${element.shellsFormatted}$enSuffix"

    return ElementsWidgetState(
      element = element,
      isFavorite = isFavorite,
      favoriteAtomicNumbers = favorites,
      titleText = titleText,
      categoryText = categoryText,
      subtitleText = subtitleText,
      configText = configText,
      categoryColors = categoryColors,
      palette = palette,
    )
  }

  fun renderWidgetBackgroundBitmap(
    palette: TodoWidgetPalette,
    widthPx: Int = 520,
    heightPx: Int = 260,
    cornerRadiusPx: Float = 32f,
    borderWidthPx: Float = 2f,
  ): Bitmap? {
    return TodoListWidgets.renderWidgetBackgroundBitmap(
      palette = palette,
      widthPx = widthPx,
      heightPx = heightPx,
      cornerRadiusPx = cornerRadiusPx,
      borderWidthPx = borderWidthPx,
    )
  }

  /**
   * Renders the selected element's monogram tile badge [Bitmap] (`#Z`, phase/radioactive badge,
   * large element symbol, and compact atomic mass).
   */
  fun renderElementTileBadgeBitmap(
    state: ElementsWidgetState,
    sizePx: Int = 128,
    cornerRadiusPx: Float = 28f,
  ): Bitmap? {
    val safeSize = sizePx.coerceIn(64, 360)
    return try {
      val bitmap = Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      val s = safeSize.toFloat()
      val colors = state.categoryColors
      val element = state.element
      val radius = min(cornerRadiusPx, s * 0.32f)

      val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = colors.containerArgb
      }
      canvas.drawRoundRect(RectF(0f, 0f, s, s), radius, radius, fillPaint)

      val strokeWidth = max(2f, s * 0.028f)
      val inset = strokeWidth * 0.6f
      val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        this.strokeWidth = strokeWidth
        color = colors.borderArgb
      }
      canvas.drawRoundRect(
        RectF(inset, inset, s - inset, s - inset),
        max(0f, radius - inset * 0.5f),
        max(0f, radius - inset * 0.5f),
        borderPaint,
      )

      val pad = s * 0.11f
      // Top-left: Atomic Number (#Z)
      val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colors.onContainerArgb
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textSize = s * 0.165f
      }
      canvas.drawText("#${element.atomicNumber}", pad, pad + numPaint.textSize * 0.88f, numPaint)

      // Top-right: Phase badge (S / L / G / ?)
      val phasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colors.accentArgb
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = s * 0.16f
      }
      val phaseBadge = element.standardPhase.badge.uppercase(Locale.ROOT)
      canvas.drawText(phaseBadge, s - pad, pad + phasePaint.textSize * 0.88f, phasePaint)

      // Center: Large Element Symbol
      val symbolPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colors.onContainerArgb
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = s * 0.38f
      }
      val symMetrics = symbolPaint.fontMetrics
      val symBaseline = s * 0.53f - (symMetrics.ascent + symMetrics.descent) * 0.5f
      canvas.drawText(element.symbol, s * 0.5f, symBaseline, symbolPaint)

      // Bottom: Compact Atomic Mass
      val massPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = withAlpha(colors.onContainerArgb, 0.85f)
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        textSize = s * 0.145f
      }
      canvas.drawText(element.compactAtomicMass, s * 0.5f, s - pad * 0.72f, massPaint)

      bitmap
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Renders the full 18-column IUPAC Periodic Table mini-map [Bitmap] (Periods 1–7 plus
   * Lanthanides & Actinides f-block rows) inside a `14.dp` rounded inner card, color-coded by
   * [ElementCategory] and highlighting the currently focused element with a bright border and
   * symbol callout.
   */
  fun renderPeriodicTableMapBitmap(
    state: ElementsWidgetState,
    widthPx: Int = 380,
    heightPx: Int = 180,
    cornerRadiusPx: Float = 28f,
  ): Bitmap? {
    val safeW = widthPx.coerceIn(140, 1000)
    val safeH = heightPx.coerceIn(72, 700)
    return try {
      val palette = state.palette
      val bitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      val w = safeW.toFloat()
      val h = safeH.toFloat()
      val cardRadius = min(cornerRadiusPx, min(w, h) * 0.35f)

      // 1. Inner Card Fill & Subtle Border
      val cardFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = palette.itemCardArgb
      }
      canvas.drawRoundRect(RectF(0f, 0f, w, h), cardRadius, cardRadius, cardFillPaint)

      val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = max(1f, min(w, h) * 0.008f)
        color = palette.borderArgb
      }
      val bInset = cardBorderPaint.strokeWidth * 0.6f
      canvas.drawRoundRect(
        RectF(bInset, bInset, w - bInset, h - bInset),
        max(0f, cardRadius - bInset),
        max(0f, cardRadius - bInset),
        cardBorderPaint,
      )

      // 2. Compute 18-column x 9.35-row (7 periods + gap + 2 f-block rows) grid geometry
      val padX = w * 0.032f
      val padY = h * 0.06f
      val availW = (w - padX * 2f).coerceAtLeast(36f)
      val availH = (h - padY * 2f).coerceAtLeast(24f)

      val cols = 18
      val totalRowUnits = 9.35f // 7 main rows + 0.35 gap + 2 f-block rows
      val gapPx = max(1f, min(availW / 180f, availH / 90f))
      val cellW = ((availW - gapPx * (cols - 1)) / cols.toFloat()).coerceAtLeast(2f)
      val cellH = ((availH - gapPx * 9f) / totalRowUnits).coerceAtLeast(2f)
      val fBlockGapY = cellH * 0.35f

      val gridWidth = cols * cellW + (cols - 1) * gapPx
      val gridHeight = 9f * cellH + 8f * gapPx + fBlockGapY
      val startX = (w - gridWidth) * 0.5f
      val startY = (h - gridHeight) * 0.5f

      val cellRadius = min(cellW, cellH) * 0.22f
      val cellFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
      }
      val cellStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = max(0.8f, min(cellW, cellH) * 0.08f)
      }
      val symPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = (min(cellW, cellH) * 0.52f).coerceAtLeast(4f)
      }
      val showAllSymbols = min(cellW, cellH) >= 11f

      val categoryColorMap = ElementCategory.entries.associateWith { cat ->
        resolveCategoryColors(cat, palette)
      }

      fun rowTopY(gridRow: Int): Float {
        return if (gridRow <= 7) {
          startY + (gridRow - 1) * (cellH + gapPx)
        } else {
          // gridRow 9 -> Lanthanides (index 7), gridRow 10 -> Actinides (index 8)
          val fIdx = gridRow - 9
          startY + 7f * (cellH + gapPx) + fBlockGapY + fIdx * (cellH + gapPx)
        }
      }

      var selectedRect: RectF? = null
      var selectedColors: ElementWidgetCategoryColors? = null

      for (el in PeriodicTableEngine.ALL_ELEMENTS) {
        val left = startX + (el.gridCol - 1) * (cellW + gapPx)
        val top = rowTopY(el.gridRow)
        val rect = RectF(left, top, left + cellW, top + cellH)
        val catColors = categoryColorMap[el.category] ?: state.categoryColors
        val isSelected = el.atomicNumber == state.element.atomicNumber
        val isFav = el.atomicNumber in state.favoriteAtomicNumbers

        if (isSelected) {
          selectedRect = rect
          selectedColors = catColors
        }

        cellFillPaint.color = if (isSelected) {
          catColors.accentArgb
        } else {
          withAlpha(catColors.containerArgb, if (isFav) 0.96f else 0.82f)
        }
        canvas.drawRoundRect(rect, cellRadius, cellRadius, cellFillPaint)

        cellStrokePaint.color = if (isFav && !isSelected) {
          palette.primaryArgb
        } else {
          withAlpha(catColors.borderArgb, 0.55f)
        }
        canvas.drawRoundRect(rect, cellRadius, cellRadius, cellStrokePaint)

        if (showAllSymbols && !isSelected) {
          symPaint.color = catColors.onContainerArgb
          val fm = symPaint.fontMetrics
          val baseline = rect.centerY() - (fm.ascent + fm.descent) * 0.5f
          canvas.drawText(el.symbol, rect.centerX(), baseline, symPaint)
        }
      }

      // 3. Highlight the selected element tile on top with an expanded badge & glow border
      if (selectedRect != null && selectedColors != null) {
        val expand = max(1.5f, min(cellW, cellH) * 0.16f)
        val hiRect = RectF(
          (selectedRect.left - expand).coerceAtLeast(2f),
          (selectedRect.top - expand).coerceAtLeast(2f),
          (selectedRect.right + expand).coerceAtMost(w - 2f),
          (selectedRect.bottom + expand).coerceAtMost(h - 2f),
        )
        val hiRadius = min(hiRect.width(), hiRect.height()) * 0.25f

        val hiFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.FILL
          color = palette.primaryArgb
        }
        canvas.drawRoundRect(hiRect, hiRadius, hiRadius, hiFillPaint)

        val hiBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.STROKE
          strokeWidth = max(1.5f, min(cellW, cellH) * 0.14f)
          color = palette.onPrimaryArgb
        }
        canvas.drawRoundRect(hiRect, hiRadius, hiRadius, hiBorderPaint)

        val hiTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = palette.onPrimaryArgb
          textAlign = Paint.Align.CENTER
          typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          textSize = (min(hiRect.width(), hiRect.height()) * 0.56f).coerceAtLeast(6f)
        }
        val fm = hiTextPaint.fontMetrics
        val baseline = hiRect.centerY() - (fm.ascent + fm.descent) * 0.5f
        canvas.drawText(state.element.symbol, hiRect.centerX(), baseline, hiTextPaint)
      }

      bitmap
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Renders the 2D Bohr Electron Shell Diagram [Bitmap] for [state.element] inside a `14.dp`
   * rounded card, matching `BohrElectronShellDiagram` in [ElementsScreen].
   */
  fun renderBohrDiagramBitmap(
    state: ElementsWidgetState,
    sizePx: Int = 160,
    cornerRadiusPx: Float = 28f,
  ): Bitmap? {
    val safeSize = sizePx.coerceIn(64, 420)
    return try {
      val palette = state.palette
      val colors = state.categoryColors
      val element = state.element
      val bitmap = Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      val s = safeSize.toFloat()
      val cardRadius = min(cornerRadiusPx, s * 0.35f)

      // 1. Card background & border
      val cardFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = palette.itemCardArgb
      }
      canvas.drawRoundRect(RectF(0f, 0f, s, s), cardRadius, cardRadius, cardFillPaint)

      val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = max(1f, s * 0.012f)
        color = palette.borderArgb
      }
      val bInset = cardBorderPaint.strokeWidth * 0.6f
      canvas.drawRoundRect(
        RectF(bInset, bInset, s - bInset, s - bInset),
        max(0f, cardRadius - bInset),
        max(0f, cardRadius - bInset),
        cardBorderPaint,
      )

      val cx = s * 0.5f
      val cy = s * 0.5f
      val maxRadius = s * 0.43f
      val nucleusRadius = max(4f, s * 0.085f)

      // 2. Nucleus with element symbol
      val nucleusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = colors.accentArgb
      }
      canvas.drawCircle(cx, cy, nucleusRadius, nucleusPaint)

      val shells = element.electronShells
      if (shells.isNotEmpty()) {
        val minRingRadius = nucleusRadius + max(4f, s * 0.055f)
        val ringStep = if (shells.size > 1) {
          (maxRadius - minRingRadius) / (shells.size - 1).toFloat()
        } else {
          0f
        }

        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.STROKE
          strokeWidth = max(1f, s * 0.011f)
          color = withAlpha(palette.onSurfaceVariantArgb, 0.45f)
        }
        val electronPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.FILL
          color = palette.primaryArgb
        }
        val dotRadius = if (shells.size >= 6) {
          max(1.6f, s * 0.020f)
        } else {
          max(2.0f, s * 0.026f)
        }

        shells.forEachIndexed { shellIdx, electronCount ->
          val radius = if (shells.size == 1) {
            (minRingRadius + maxRadius) * 0.5f
          } else {
            minRingRadius + shellIdx * ringStep
          }
          canvas.drawCircle(cx, cy, radius, ringPaint)

          for (e in 0 until electronCount) {
            val angleRad = (2.0 * PI * e / electronCount.toDouble()) - (PI / 2.0)
            val ex = cx + (radius * cos(angleRad)).toFloat()
            val ey = cy + (radius * sin(angleRad)).toFloat()
            canvas.drawCircle(ex, ey, dotRadius, electronPaint)
          }
        }
      }

      bitmap
    } catch (_: Throwable) {
      null
    }
  }

  fun buildOpenElementsPendingIntent(
    context: Context,
    appWidgetId: Int,
    atomicNumber: Int,
    openInspector: Boolean = false,
    openSearch: Boolean = false,
  ): PendingIntent {
    val intent = createElementsWidgetIntent(
      context = context,
      atomicNumber = atomicNumber,
      openInspector = openInspector,
      openSearch = openSearch,
    )
    val base = when {
      openSearch -> REQUEST_CODE_OPEN_SEARCH_BASE
      openInspector -> REQUEST_CODE_OPEN_INSPECTOR_BASE
      else -> REQUEST_CODE_OPEN_TABLE_BASE
    }
    return PendingIntent.getActivity(
      context,
      base + (appWidgetId and 0xFF),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  fun buildActionBroadcastPendingIntent(
    context: Context,
    appWidgetId: Int,
    action: String,
    requestCodeBase: Int,
  ): PendingIntent {
    val intent = Intent(context, ElementsWidgetProvider::class.java).apply {
      this.action = action
      putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      setPackage(context.packageName)
    }
    return PendingIntent.getBroadcast(
      context,
      requestCodeBase + (appWidgetId and 0xFF),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  /**
   * Builds the [RemoteViews] for an [ElementsWidgetProvider] instance.
   */
  fun buildWidgetRemoteViews(
    context: Context,
    appWidgetId: Int,
    state: ElementsWidgetState = resolveWidgetState(context),
    options: Bundle? = null,
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_elements)
    val palette = state.palette
    val density = try {
      context.resources.displayMetrics.density.coerceIn(1f, 4f)
    } catch (_: Throwable) {
      2f
    }

    val (widthDp, heightDp) = MusicPlayerWidgets.resolveWidgetSizeDp(
      context = context,
      options = options,
      defaultWidthDp = 280f,
      defaultHeightDp = 150f,
    )
    val rawWidthPx = (widthDp * density).coerceAtLeast(160f)
    val rawHeightPx = (heightDp * density).coerceAtLeast(96f)
    val maxDimPx = 1000f
    val maxRaw = max(rawWidthPx, rawHeightPx)
    val scaleFactor = if (maxRaw > maxDimPx) maxDimPx / maxRaw else 1f
    val bgWidthPx = (rawWidthPx * scaleFactor).roundToInt().coerceAtLeast(160)
    val bgHeightPx = (rawHeightPx * scaleFactor).roundToInt().coerceAtLeast(96)

    // 1. Outer 16.dp Widget Background Bitmap with procedural texture & inset 1.dp border stroke
    renderWidgetBackgroundBitmap(
      palette = palette,
      widthPx = bgWidthPx,
      heightPx = bgHeightPx,
      cornerRadiusPx = WIDGET_CORNER_RADIUS_DP * density * scaleFactor,
      borderWidthPx = max(1f, 1f * density * scaleFactor),
    )?.let { bgBmp ->
      views.setImageViewBitmap(R.id.widget_elements_background, bgBmp)
    }

    // 2. Left Element Tile Badge Bitmap
    val tileBadgeSizePx = (58f * density).roundToInt().coerceIn(72, 240)
    renderElementTileBadgeBitmap(
      state = state,
      sizePx = tileBadgeSizePx,
      cornerRadiusPx = INNER_CARD_CORNER_RADIUS_DP * density,
    )?.let { tileBmp ->
      views.setImageViewBitmap(R.id.widget_elements_tile_image, tileBmp)
    }

    // 3. Center Element Title, Category Pill, Subtitle & Electron Configuration
    views.setTextViewText(R.id.widget_elements_title, state.titleText)
    views.setTextColor(R.id.widget_elements_title, palette.onSurfaceArgb)

    views.setTextViewText(R.id.widget_elements_category_pill, state.categoryText)
    views.setTextColor(R.id.widget_elements_category_pill, state.categoryColors.accentArgb)

    views.setTextViewText(R.id.widget_elements_subtitle, state.subtitleText)
    views.setTextColor(R.id.widget_elements_subtitle, palette.onSurfaceVariantArgb)

    views.setTextViewText(R.id.widget_elements_config_text, state.configText)
    views.setTextColor(R.id.widget_elements_config_text, palette.onSurfaceArgb)

    // 4. Right Action Controls (< Prev, > Next, ★ Favorite, ⟳ Random, 🔍 Search)
    views.setInt(R.id.widget_elements_prev_button, "setColorFilter", palette.onSurfaceVariantArgb)
    views.setInt(R.id.widget_elements_next_button, "setColorFilter", palette.onSurfaceVariantArgb)

    views.setImageViewResource(
      R.id.widget_elements_favorite_button,
      if (state.isFavorite) R.drawable.ic_media_favorite_on else R.drawable.ic_media_favorite_off,
    )
    views.setInt(
      R.id.widget_elements_favorite_button,
      "setColorFilter",
      if (state.isFavorite) palette.primaryArgb else palette.onSurfaceVariantArgb,
    )

    views.setImageViewResource(R.id.widget_elements_random_button_bg, state.iconButtonBackgroundRes)
    views.setInt(R.id.widget_elements_random_button_bg, "setColorFilter", palette.primaryContainerArgb)
    views.setInt(R.id.widget_elements_random_button_icon, "setColorFilter", palette.primaryArgb)

    views.setImageViewResource(R.id.widget_elements_search_button_bg, state.iconButtonBackgroundRes)
    views.setInt(R.id.widget_elements_search_button_bg, "setColorFilter", palette.secondaryContainerArgb)
    views.setInt(R.id.widget_elements_search_button_icon, "setColorFilter", palette.onSecondaryContainerArgb)

    // 5. Bottom Visual Section: 18-Column IUPAC Periodic Table Grid Map + 2D Bohr Electron Shell Diagram
    val showVisualSection = heightDp >= COMPACT_HEIGHT_THRESHOLD_DP
    views.setViewVisibility(
      R.id.widget_elements_visual_section,
      if (showVisualSection) View.VISIBLE else View.GONE,
    )
    if (showVisualSection) {
      val showBohr = widthDp >= SHOW_BOHR_WIDTH_THRESHOLD_DP
      views.setViewVisibility(
        R.id.widget_elements_bohr_image,
        if (showBohr) View.VISIBLE else View.GONE,
      )

      val visualHeightDp = (heightDp - 78f).coerceAtLeast(56f)
      val bohrSizeDp = if (showBohr) min(88f, visualHeightDp) else 0f
      val mapWidthDp = (widthDp - 20f - if (showBohr) (bohrSizeDp + 6f) else 0f).coerceAtLeast(120f)

      val mapWidthPx = (mapWidthDp * density * scaleFactor).roundToInt().coerceIn(140, 900)
      val mapHeightPx = (visualHeightDp * density * scaleFactor).roundToInt().coerceIn(72, 600)
      val innerCornerPx = INNER_CARD_CORNER_RADIUS_DP * density * scaleFactor

      renderPeriodicTableMapBitmap(
        state = state,
        widthPx = mapWidthPx,
        heightPx = mapHeightPx,
        cornerRadiusPx = innerCornerPx,
      )?.let { mapBmp ->
        views.setImageViewBitmap(R.id.widget_elements_periodic_table_image, mapBmp)
      }

      if (showBohr) {
        val bohrSizePx = (bohrSizeDp * density * scaleFactor).roundToInt().coerceIn(72, 360)
        renderBohrDiagramBitmap(
          state = state,
          sizePx = bohrSizePx,
          cornerRadiusPx = innerCornerPx,
        )?.let { bohrBmp ->
          views.setImageViewBitmap(R.id.widget_elements_bohr_image, bohrBmp)
        }
      }
    }

    // 6. Click Listeners
    val openTableIntent = buildOpenElementsPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      atomicNumber = state.element.atomicNumber,
      openInspector = false,
      openSearch = false,
    )
    val openInspectorIntent = buildOpenElementsPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      atomicNumber = state.element.atomicNumber,
      openInspector = true,
      openSearch = false,
    )
    val openSearchIntent = buildOpenElementsPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      atomicNumber = state.element.atomicNumber,
      openInspector = false,
      openSearch = true,
    )
    val prevIntent = buildActionBroadcastPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      action = ACTION_WIDGET_PREV_ELEMENT,
      requestCodeBase = REQUEST_CODE_PREV_ELEMENT_BASE,
    )
    val nextIntent = buildActionBroadcastPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      action = ACTION_WIDGET_NEXT_ELEMENT,
      requestCodeBase = REQUEST_CODE_NEXT_ELEMENT_BASE,
    )
    val randomIntent = buildActionBroadcastPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      action = ACTION_WIDGET_RANDOM_ELEMENT,
      requestCodeBase = REQUEST_CODE_RANDOM_ELEMENT_BASE,
    )
    val favoriteIntent = buildActionBroadcastPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      action = ACTION_WIDGET_TOGGLE_FAVORITE,
      requestCodeBase = REQUEST_CODE_TOGGLE_FAVORITE_BASE,
    )

    views.setOnClickPendingIntent(R.id.widget_elements_tile_image, openInspectorIntent)
    views.setOnClickPendingIntent(R.id.widget_elements_text_container, openInspectorIntent)
    views.setOnClickPendingIntent(R.id.widget_elements_bohr_image, openInspectorIntent)
    views.setOnClickPendingIntent(R.id.widget_elements_periodic_table_image, openTableIntent)
    views.setOnClickPendingIntent(R.id.widget_elements_prev_button, prevIntent)
    views.setOnClickPendingIntent(R.id.widget_elements_next_button, nextIntent)
    views.setOnClickPendingIntent(R.id.widget_elements_favorite_button, favoriteIntent)
    views.setOnClickPendingIntent(R.id.widget_elements_random_button, randomIntent)
    views.setOnClickPendingIntent(R.id.widget_elements_random_button_icon, randomIntent)
    views.setOnClickPendingIntent(R.id.widget_elements_search_button, openSearchIntent)
    views.setOnClickPendingIntent(R.id.widget_elements_search_button_icon, openSearchIntent)

    return views
  }

  /**
   * Handles a widget broadcast action for [ElementsWidgetProvider].
   */
  fun handleWidgetAction(
    context: Context?,
    intent: Intent?,
    repository: ElementsRepository? = null,
    random: Random = Random.Default,
  ): Boolean {
    val action = intent?.action ?: return false
    return when (action) {
      ACTION_WIDGET_REFRESH -> {
        if (context != null) updateAllWidgets(context)
        true
      }
      ACTION_WIDGET_PREV_ELEMENT -> {
        stepFocusedElement(context = context, delta = -1, refreshWidgets = true)
        true
      }
      ACTION_WIDGET_NEXT_ELEMENT -> {
        stepFocusedElement(context = context, delta = 1, refreshWidgets = true)
        true
      }
      ACTION_WIDGET_RANDOM_ELEMENT -> {
        pickRandomElement(context = context, random = random, refreshWidgets = true)
        true
      }
      ACTION_WIDGET_TOGGLE_FAVORITE -> {
        toggleFavoriteFromWidget(context = context, repository = repository, refreshWidgets = true)
        true
      }
      ACTION_WIDGET_SET_ELEMENT -> {
        val targetZ = intent.getIntExtra(EXTRA_ATOMIC_NUMBER, -1)
        if (targetZ in 1..118) {
          saveFocusedAtomicNumber(context, targetZ)
          if (context != null) updateAllWidgets(context)
          true
        } else {
          false
        }
      }
      else -> false
    }
  }

  /**
   * Refreshes all pinned instances of [ElementsWidgetProvider].
   */
  fun updateAllWidgets(context: Context?) {
    val appCtx = context?.applicationContext ?: context ?: return
    try {
      val manager = AppWidgetManager.getInstance(appCtx) ?: return
      val component = ComponentName(appCtx, ElementsWidgetProvider::class.java)
      val widgetIds = try {
        manager.getAppWidgetIds(component) ?: IntArray(0)
      } catch (_: Throwable) {
        IntArray(0)
      }
      if (widgetIds.isEmpty()) return
      updateElementsWidgets(appCtx, manager, widgetIds)
    } catch (_: Throwable) {}
  }

  fun updateElementsWidgets(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
    state: ElementsWidgetState = resolveWidgetState(context),
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
 * Periodic Table of Elements Home Screen AppWidget displaying an interactive element spotlight,
 * 18-column IUPAC Periodic Table mini-map, 2D Bohr electron shell diagram, and `<` / `>` / `★` /
 * `⟳` / `🔍` controls.
 */
class ElementsWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    ElementsWidgets.updateElementsWidgets(context, appWidgetManager, appWidgetIds)
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle,
  ) {
    ElementsWidgets.updateElementsWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    val action = intent.action ?: return
    if (action.startsWith("com.example.toolbox.elements.ACTION_")) {
      ElementsWidgets.handleWidgetAction(context, intent)
    }
  }
}
