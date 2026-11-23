package com.example.toolbox.ui.tools

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
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
 * Active view mode of the Periodic Table of Elements Home Screen AppWidget ([ElementsWidgetProvider]):
 * - [TABLE]: Main view displaying the full 18-column IUPAC Periodic Table where every element tile
 *   (`1..118`) is individually tappable.
 * - [ELEMENT_INFO]: Detailed Element Information view shown after tapping an element on the
 *   Periodic Table, displaying that element's identity badge, atomic/physical properties,
 *   full suborbital electron configuration & Hund's-rule orbital spin boxes (`⇅` / `↑`),
 *   and 2D Bohr electron shell diagram.
 */
enum class ElementsWidgetViewMode {
  TABLE,
  ELEMENT_INFO,
}

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
 * Snapshot of the active [ElementsWidgetViewMode], focused [ChemicalElement], suborbital breakdown,
 * favorite elements set, and Material 3 palette rendered by the Periodic Table of Elements
 * Home Screen AppWidget ([ElementsWidgetProvider]).
 */
@Immutable
data class ElementsWidgetState(
  val viewMode: ElementsWidgetViewMode,
  val element: ChemicalElement,
  val isFavorite: Boolean,
  val favoriteAtomicNumbers: Set<Int>,
  val titleText: String,
  val categoryText: String,
  val subtitleText: String,
  val configText: String,
  val suborbitalsText: String,
  val fullSuborbitalsText: String,
  val suborbitals: List<ElementSuborbital>,
  val valenceSuborbitals: List<ElementSuborbital>,
  val categoryColors: ElementWidgetCategoryColors,
  val palette: TodoWidgetPalette,
  val hexagonIconButtons: Boolean = palette.hexagonIconButtons,
  val iconButtonBackgroundRes: Int = palette.iconButtonBackgroundRes,
)

/**
 * Central state resolver, interactive 18-column IUPAC Periodic Table main view renderer,
 * Suborbital Spin-Box Diagram Canvas renderer, 2D Bohr Electron Shell Diagram Canvas renderer,
 * and RemoteViews builder for the Periodic Table of Elements Home Screen AppWidget
 * ([ElementsWidgetProvider]):
 * - **Main View ([ElementsWidgetViewMode.TABLE])**: Displays the complete 18-column IUPAC Periodic
 *   Table (Periods 1–7 + Lanthanides & Actinides f-block rows) with category color-coding and
 *   individual tap targets for all 118 elements. Tapping any element switches the widget to
 *   [ElementsWidgetViewMode.ELEMENT_INFO] for that element.
 * - **Element Information View ([ElementsWidgetViewMode.ELEMENT_INFO])**: Displays the tapped
 *   element's tile badge (`#Z`, phase badge, symbol, atomic mass), element name & category/orbital
 *   block pill, atomic mass/phase/period/group summary, suborbital electron configuration and
 *   Hund's-rule spin-pair orbital boxes (`⇅` / `↑`), visual Suborbitals Diagram card, 2D Bohr
 *   electron shell diagram, and a `←` button to return to the Periodic Table main view (alongside
 *   `<` Previous, `>` Next, `★` Favorite, `⟳` Random, and `🔍` Search controls).
 * - Strictly follows `DESIGN_LANGUAGE.md` Section 5.8 (`16.dp` outer corner radius,
 *   `@android:id/background` + `clipToOutline="true"` launcher opt-out, inset `1.dp` border stroke,
 *   and automatic Hexagonify badge support).
 */
object ElementsWidgets {

  const val ACTION_WIDGET_REFRESH = "com.example.toolbox.elements.ACTION_WIDGET_REFRESH"
  const val ACTION_WIDGET_SELECT_ELEMENT = "com.example.toolbox.elements.ACTION_WIDGET_SELECT_ELEMENT"
  const val ACTION_WIDGET_SHOW_TABLE = "com.example.toolbox.elements.ACTION_WIDGET_SHOW_TABLE"
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
  private const val REQUEST_CODE_SHOW_TABLE_BASE = 9550
  private const val REQUEST_CODE_PREV_ELEMENT_BASE = 9560
  private const val REQUEST_CODE_NEXT_ELEMENT_BASE = 9570
  private const val REQUEST_CODE_RANDOM_ELEMENT_BASE = 9580
  private const val REQUEST_CODE_TOGGLE_FAVORITE_BASE = 9590
  private const val REQUEST_CODE_SELECT_ELEMENT_BASE = 20000

  /**
   * Maps atomic number `Z` (`1..118`) to its clickable cell view ID in `R.layout.widget_elements`.
   */
  val ELEMENT_CELL_VIEW_IDS: IntArray = intArrayOf(
    0,
    R.id.widget_element_cell_1,
    R.id.widget_element_cell_2,
    R.id.widget_element_cell_3,
    R.id.widget_element_cell_4,
    R.id.widget_element_cell_5,
    R.id.widget_element_cell_6,
    R.id.widget_element_cell_7,
    R.id.widget_element_cell_8,
    R.id.widget_element_cell_9,
    R.id.widget_element_cell_10,
    R.id.widget_element_cell_11,
    R.id.widget_element_cell_12,
    R.id.widget_element_cell_13,
    R.id.widget_element_cell_14,
    R.id.widget_element_cell_15,
    R.id.widget_element_cell_16,
    R.id.widget_element_cell_17,
    R.id.widget_element_cell_18,
    R.id.widget_element_cell_19,
    R.id.widget_element_cell_20,
    R.id.widget_element_cell_21,
    R.id.widget_element_cell_22,
    R.id.widget_element_cell_23,
    R.id.widget_element_cell_24,
    R.id.widget_element_cell_25,
    R.id.widget_element_cell_26,
    R.id.widget_element_cell_27,
    R.id.widget_element_cell_28,
    R.id.widget_element_cell_29,
    R.id.widget_element_cell_30,
    R.id.widget_element_cell_31,
    R.id.widget_element_cell_32,
    R.id.widget_element_cell_33,
    R.id.widget_element_cell_34,
    R.id.widget_element_cell_35,
    R.id.widget_element_cell_36,
    R.id.widget_element_cell_37,
    R.id.widget_element_cell_38,
    R.id.widget_element_cell_39,
    R.id.widget_element_cell_40,
    R.id.widget_element_cell_41,
    R.id.widget_element_cell_42,
    R.id.widget_element_cell_43,
    R.id.widget_element_cell_44,
    R.id.widget_element_cell_45,
    R.id.widget_element_cell_46,
    R.id.widget_element_cell_47,
    R.id.widget_element_cell_48,
    R.id.widget_element_cell_49,
    R.id.widget_element_cell_50,
    R.id.widget_element_cell_51,
    R.id.widget_element_cell_52,
    R.id.widget_element_cell_53,
    R.id.widget_element_cell_54,
    R.id.widget_element_cell_55,
    R.id.widget_element_cell_56,
    R.id.widget_element_cell_57,
    R.id.widget_element_cell_58,
    R.id.widget_element_cell_59,
    R.id.widget_element_cell_60,
    R.id.widget_element_cell_61,
    R.id.widget_element_cell_62,
    R.id.widget_element_cell_63,
    R.id.widget_element_cell_64,
    R.id.widget_element_cell_65,
    R.id.widget_element_cell_66,
    R.id.widget_element_cell_67,
    R.id.widget_element_cell_68,
    R.id.widget_element_cell_69,
    R.id.widget_element_cell_70,
    R.id.widget_element_cell_71,
    R.id.widget_element_cell_72,
    R.id.widget_element_cell_73,
    R.id.widget_element_cell_74,
    R.id.widget_element_cell_75,
    R.id.widget_element_cell_76,
    R.id.widget_element_cell_77,
    R.id.widget_element_cell_78,
    R.id.widget_element_cell_79,
    R.id.widget_element_cell_80,
    R.id.widget_element_cell_81,
    R.id.widget_element_cell_82,
    R.id.widget_element_cell_83,
    R.id.widget_element_cell_84,
    R.id.widget_element_cell_85,
    R.id.widget_element_cell_86,
    R.id.widget_element_cell_87,
    R.id.widget_element_cell_88,
    R.id.widget_element_cell_89,
    R.id.widget_element_cell_90,
    R.id.widget_element_cell_91,
    R.id.widget_element_cell_92,
    R.id.widget_element_cell_93,
    R.id.widget_element_cell_94,
    R.id.widget_element_cell_95,
    R.id.widget_element_cell_96,
    R.id.widget_element_cell_97,
    R.id.widget_element_cell_98,
    R.id.widget_element_cell_99,
    R.id.widget_element_cell_100,
    R.id.widget_element_cell_101,
    R.id.widget_element_cell_102,
    R.id.widget_element_cell_103,
    R.id.widget_element_cell_104,
    R.id.widget_element_cell_105,
    R.id.widget_element_cell_106,
    R.id.widget_element_cell_107,
    R.id.widget_element_cell_108,
    R.id.widget_element_cell_109,
    R.id.widget_element_cell_110,
    R.id.widget_element_cell_111,
    R.id.widget_element_cell_112,
    R.id.widget_element_cell_113,
    R.id.widget_element_cell_114,
    R.id.widget_element_cell_115,
    R.id.widget_element_cell_116,
    R.id.widget_element_cell_117,
    R.id.widget_element_cell_118,
  )

  fun cellViewIdForAtomicNumber(atomicNumber: Int): Int {
    return ELEMENT_CELL_VIEW_IDS.getOrElse(atomicNumber.coerceIn(1, 118)) { R.id.widget_element_cell_1 }
  }

  @Volatile
  private var inMemoryAtomicNumber: Int = 1

  @Volatile
  private var inMemoryViewMode: ElementsWidgetViewMode = ElementsWidgetViewMode.TABLE

  fun clearInMemoryStateForTesting() {
    inMemoryAtomicNumber = 1
    inMemoryViewMode = ElementsWidgetViewMode.TABLE
  }

  /**
   * Loads the active [ElementsWidgetViewMode] for the Periodic Table widget, defaulting to
   * [ElementsWidgetViewMode.TABLE] so the Periodic Table is the main view.
   */
  fun loadWidgetViewMode(context: Context? = null): ElementsWidgetViewMode {
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(ELEMENTS_PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(PREF_KEY_WIDGET_VIEW_MODE, null)
        if (!raw.isNullOrBlank()) {
          val mode = ElementsWidgetViewMode.entries.firstOrNull {
            it.name.equals(raw.trim(), ignoreCase = true)
          }
          if (mode != null) {
            inMemoryViewMode = mode
            return mode
          }
        }
      } catch (_: Throwable) {}
    }
    return inMemoryViewMode
  }

  /**
   * Persists the active [ElementsWidgetViewMode] (`TABLE` or `ELEMENT_INFO`) for the Periodic Table widget.
   */
  fun saveWidgetViewMode(
    context: Context?,
    viewMode: ElementsWidgetViewMode,
  ): ElementsWidgetViewMode {
    inMemoryViewMode = viewMode
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(ELEMENTS_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_KEY_WIDGET_VIEW_MODE, viewMode.name).apply()
      } catch (_: Throwable) {}
    }
    return viewMode
  }

  /**
   * Loads the currently focused element atomic number (`1..118`) for the Periodic Table widget,
   * defaulting to `1` (Hydrogen) if no selection has been saved.
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
   * Selects [atomicNumber] (`1..118`) from a tap on the Periodic Table widget and switches the
   * widget view mode to [ElementsWidgetViewMode.ELEMENT_INFO].
   */
  fun selectElementInWidget(
    context: Context?,
    atomicNumber: Int,
    refreshWidgets: Boolean = true,
  ): ChemicalElement {
    val coerced = saveFocusedAtomicNumber(context, atomicNumber)
    saveWidgetViewMode(context, ElementsWidgetViewMode.ELEMENT_INFO)
    if (refreshWidgets && context != null) {
      updateAllWidgets(context)
    }
    return PeriodicTableEngine.byAtomicNumber(coerced) ?: PeriodicTableEngine.ALL_ELEMENTS.first()
  }

  /**
   * Switches the Periodic Table widget back to its main [ElementsWidgetViewMode.TABLE] view.
   */
  fun showPeriodicTableInWidget(
    context: Context?,
    refreshWidgets: Boolean = true,
  ): ElementsWidgetViewMode {
    saveWidgetViewMode(context, ElementsWidgetViewMode.TABLE)
    if (refreshWidgets && context != null) {
      updateAllWidgets(context)
    }
    return ElementsWidgetViewMode.TABLE
  }

  /**
   * Steps the focused element backward (`delta = -1`) or forward (`delta = +1`) with wrap-around
   * between Hydrogen (`#1`) and Oganesson (`#118`), and ensures [ElementsWidgetViewMode.ELEMENT_INFO]
   * is shown.
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
    saveWidgetViewMode(context, ElementsWidgetViewMode.ELEMENT_INFO)
    if (refreshWidgets && context != null) {
      updateAllWidgets(context)
    }
    return PeriodicTableEngine.byAtomicNumber(nextZ) ?: PeriodicTableEngine.ALL_ELEMENTS.first()
  }

  /**
   * Picks a random element (`1..118`) distinct from the current element, switches to
   * [ElementsWidgetViewMode.ELEMENT_INFO], and refreshes all Periodic Table widgets.
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
    saveWidgetViewMode(context, ElementsWidgetViewMode.ELEMENT_INFO)
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
   * Resolves Material 3 orbital block colors (`s`, `p`, `d`, `f`) from [palette].
   */
  fun resolveBlockColors(
    block: ElementBlock,
    palette: TodoWidgetPalette,
  ): ElementWidgetCategoryColors {
    val errorContainerArgb = if (palette.isDarkTheme) 0xFF93000A.toInt() else 0xFFFFDAD6.toInt()
    val onErrorContainerArgb = if (palette.isDarkTheme) 0xFFFFDAD6.toInt() else 0xFF410002.toInt()
    val errorArgb = if (palette.isDarkTheme) 0xFFFFB4AB.toInt() else 0xFFBA1A1A.toInt()

    val (container, onContainer, accent) = when (block) {
      ElementBlock.S -> Triple(
        palette.tertiaryContainerArgb,
        palette.onTertiaryContainerArgb,
        palette.tertiaryArgb,
      )
      ElementBlock.P -> Triple(
        palette.secondaryContainerArgb,
        palette.onSecondaryContainerArgb,
        palette.secondaryArgb,
      )
      ElementBlock.D -> Triple(
        palette.primaryContainerArgb,
        palette.onPrimaryContainerArgb,
        palette.primaryArgb,
      )
      ElementBlock.F -> Triple(
        blendArgb(errorContainerArgb, palette.primaryContainerArgb, 0.45f),
        onErrorContainerArgb,
        errorArgb,
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
    viewModeOverride: ElementsWidgetViewMode? = null,
  ): ElementsWidgetState {
    val repo = repository ?: context?.let {
      try {
        SharedPreferencesElementsRepository(it)
      } catch (_: Throwable) {
        null
      }
    } ?: InMemoryElementsRepository()

    val viewMode = viewModeOverride ?: loadWidgetViewMode(context)
    val favorites = repo.loadFavoriteAtomicNumbers()
    val focusedZ = (atomicNumberOverride ?: loadFocusedAtomicNumber(context, repo)).coerceIn(1, 118)
    val element = PeriodicTableEngine.byAtomicNumber(focusedZ) ?: PeriodicTableEngine.ALL_ELEMENTS.first()
    val isFavorite = element.atomicNumber in favorites
    val palette = TodoListWidgets.resolveWidgetPalette(context)
    val categoryColors = resolveCategoryColors(element.category, palette)

    val favStarPrefix = if (isFavorite) "★ " else ""
    val titleText = "$favStarPrefix${element.name} (#${element.atomicNumber})"
    val categoryText = "${element.category.shortLabel} • ${element.block.label}"
    val groupShort = element.group?.let { "Group $it" } ?: element.block.label
    val subtitleText = "${element.formattedAtomicMass} • ${element.standardPhase.label} (${element.standardPhase.badge}) • P${element.period} • $groupShort"
    val enSuffix = element.electronegativity?.let { " • EN $it" }.orEmpty()
    val configText = "${element.electronConfiguration} • Shells: ${element.shellsFormatted}$enSuffix"

    val suborbitals = element.suborbitals
    val valenceSuborbitals = element.valenceSuborbitals
    val suborbitalsText = "Suborbitals: ${element.valenceSuborbitalsSpinSummary}"
    val fullSuborbitalsText = element.fullElectronConfiguration

    return ElementsWidgetState(
      viewMode = viewMode,
      element = element,
      isFavorite = isFavorite,
      favoriteAtomicNumbers = favorites,
      titleText = titleText,
      categoryText = categoryText,
      subtitleText = subtitleText,
      configText = configText,
      suborbitalsText = suborbitalsText,
      fullSuborbitalsText = fullSuborbitalsText,
      suborbitals = suborbitals,
      valenceSuborbitals = valenceSuborbitals,
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
   * Renders the full 18-column IUPAC Periodic Table [Bitmap] (Periods 1–7 plus Lanthanides &
   * Actinides f-block rows) aligned 1:1 with the 18-column × 9.35-row proportional hit grid in
   * `R.layout.widget_elements` (`@+id/widget_elements_table_hit_grid`) so tapping any element
   * tile selects that exact element and transitions to its information & suborbitals view.
   */
  fun renderPeriodicTableMapBitmap(
    state: ElementsWidgetState,
    widthPx: Int = 520,
    heightPx: Int = 260,
    cornerRadiusPx: Float = 28f,
  ): Bitmap? {
    val safeW = widthPx.coerceIn(140, 1200)
    val safeH = heightPx.coerceIn(72, 900)
    return try {
      val palette = state.palette
      val bitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      val w = safeW.toFloat()
      val h = safeH.toFloat()

      val cols = 18
      val totalRowUnits = 9.35f // 7 main rows + 0.35 gap + 2 f-block rows
      val slotW = w / cols.toFloat()
      val slotH = h / totalRowUnits
      val gapPx = max(1f, min(slotW, slotH) * 0.08f)
      val halfGap = gapPx * 0.5f

      val cellW = (slotW - gapPx).coerceAtLeast(2f)
      val cellH = (slotH - gapPx).coerceAtLeast(2f)
      val cellRadius = min(cellW, cellH) * 0.20f

      val cellFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
      }
      val cellStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = max(0.8f, min(cellW, cellH) * 0.075f)
      }
      val symPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = (min(cellW, cellH) * 0.48f).coerceAtLeast(4.5f)
      }
      val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textSize = (min(cellW, cellH) * 0.23f).coerceAtLeast(3.5f)
      }
      val showAtomicNumbers = cellH >= 22f && cellW >= 18f

      val categoryColorMap = ElementCategory.entries.associateWith { cat ->
        resolveCategoryColors(cat, palette)
      }

      fun slotRect(gridRow: Int, gridCol: Int): RectF {
        val rowUnitTop = if (gridRow <= 7) {
          (gridRow - 1).toFloat()
        } else {
          // gridRow 9 -> Lanthanides (7.35), gridRow 10 -> Actinides (8.35)
          7.35f + (gridRow - 9).toFloat()
        }
        val left = (gridCol - 1) * slotW + halfGap
        val top = rowUnitTop * slotH + halfGap
        return RectF(left, top, left + cellW, top + cellH)
      }

      // 1. Top-center header legend in the open area between Group 2 and Group 13 (Periods 1–3)
      val bannerLeft = 2f * slotW + slotW * 0.25f
      val bannerRight = 12f * slotW - slotW * 0.25f
      val bannerTop = 0.15f * slotH
      val bannerBottom = 2.85f * slotH
      if (bannerRight > bannerLeft + 40f && bannerBottom > bannerTop + 16f) {
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = palette.onSurfaceArgb
          textAlign = Paint.Align.CENTER
          typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          textSize = (slotH * 0.48f).coerceIn(8f, 22f)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = palette.onSurfaceVariantArgb
          textAlign = Paint.Align.CENTER
          typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
          textSize = (slotH * 0.34f).coerceIn(6.5f, 15f)
        }
        val centerX = (bannerLeft + bannerRight) * 0.5f
        canvas.drawText("Periodic Table of Elements", centerX, bannerTop + titlePaint.textSize * 1.05f, titlePaint)
        canvas.drawText(
          "Tap any element for details & suborbitals",
          centerX,
          bannerTop + titlePaint.textSize * 1.25f + subPaint.textSize * 1.15f,
          subPaint,
        )

        // Orbital block legend pills (s-block, p-block, d-block, f-block) in Period 2-3 gap
        val blockEntries = ElementBlock.entries
        val pillAreaY = bannerTop + titlePaint.textSize * 1.35f + subPaint.textSize * 1.65f
        val pillH = (slotH * 0.56f).coerceIn(10f, 26f)
        if (pillAreaY + pillH <= bannerBottom) {
          val pillGap = max(3f, slotW * 0.14f)
          val totalBannerW = bannerRight - bannerLeft
          val pillW = ((totalBannerW - pillGap * (blockEntries.size - 1)) / blockEntries.size).coerceAtMost(slotW * 2.1f)
          val totalPillsW = pillW * blockEntries.size + pillGap * (blockEntries.size - 1)
          var pillX = centerX - totalPillsW * 0.5f
          val pillTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = (pillH * 0.54f).coerceIn(6f, 14f)
          }
          for (blk in blockEntries) {
            val blkColors = resolveBlockColors(blk, palette)
            val pRect = RectF(pillX, pillAreaY, pillX + pillW, pillAreaY + pillH)
            cellFillPaint.color = withAlpha(blkColors.containerArgb, 0.88f)
            canvas.drawRoundRect(pRect, pillH * 0.5f, pillH * 0.5f, cellFillPaint)
            cellStrokePaint.color = blkColors.borderArgb
            canvas.drawRoundRect(pRect, pillH * 0.5f, pillH * 0.5f, cellStrokePaint)
            pillTextPaint.color = blkColors.onContainerArgb
            val fm = pillTextPaint.fontMetrics
            val bl = pRect.centerY() - (fm.ascent + fm.descent) * 0.5f
            canvas.drawText(blk.label, pRect.centerX(), bl, pillTextPaint)
            pillX += pillW + pillGap
          }
        }
      }

      // 2. Lanthanide (57-71) & Actinide (89-103) placeholder cells at (6, 3) and (7, 3)
      val placeholders = listOf(
        Triple(6, ElementCategory.LANTHANIDE, "57-71"),
        Triple(7, ElementCategory.ACTINIDE, "89-103"),
      )
      val placeholderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textSize = (min(cellW, cellH) * 0.28f).coerceAtLeast(3.5f)
      }
      for ((pRow, pCat, pLabel) in placeholders) {
        val pRect = slotRect(pRow, 3)
        val pColors = categoryColorMap[pCat] ?: state.categoryColors
        cellFillPaint.color = withAlpha(pColors.containerArgb, 0.55f)
        canvas.drawRoundRect(pRect, cellRadius, cellRadius, cellFillPaint)
        cellStrokePaint.color = withAlpha(pColors.borderArgb, 0.65f)
        canvas.drawRoundRect(pRect, cellRadius, cellRadius, cellStrokePaint)
        placeholderPaint.color = pColors.onContainerArgb
        val fm = placeholderPaint.fontMetrics
        val bl = pRect.centerY() - (fm.ascent + fm.descent) * 0.5f
        canvas.drawText(pLabel, pRect.centerX(), bl, placeholderPaint)
      }

      // 3. Draw all 118 element tiles
      var selectedRect: RectF? = null
      for (el in PeriodicTableEngine.ALL_ELEMENTS) {
        val rect = slotRect(el.gridRow, el.gridCol)
        val catColors = categoryColorMap[el.category] ?: state.categoryColors
        val isSelected = el.atomicNumber == state.element.atomicNumber
        val isFav = el.atomicNumber in state.favoriteAtomicNumbers

        if (isSelected) {
          selectedRect = rect
        }

        cellFillPaint.color = withAlpha(catColors.containerArgb, if (isFav) 0.96f else 0.88f)
        canvas.drawRoundRect(rect, cellRadius, cellRadius, cellFillPaint)

        cellStrokePaint.color = if (isFav) {
          palette.primaryArgb
        } else {
          withAlpha(catColors.borderArgb, 0.65f)
        }
        canvas.drawRoundRect(rect, cellRadius, cellRadius, cellStrokePaint)

        if (showAtomicNumbers) {
          numPaint.color = withAlpha(catColors.onContainerArgb, 0.78f)
          canvas.drawText(
            el.atomicNumber.toString(),
            rect.left + cellW * 0.10f,
            rect.top + numPaint.textSize * 1.02f,
            numPaint,
          )
        }

        symPaint.color = catColors.onContainerArgb
        val fm = symPaint.fontMetrics
        val centerYOffset = if (showAtomicNumbers) rect.centerY() + cellH * 0.10f else rect.centerY()
        val baseline = centerYOffset - (fm.ascent + fm.descent) * 0.5f
        canvas.drawText(el.symbol, rect.centerX(), baseline, symPaint)
      }

      // 4. Subtle highlight ring around the last-selected element tile
      if (selectedRect != null) {
        val hiBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.STROKE
          strokeWidth = max(1.5f, min(cellW, cellH) * 0.11f)
          color = palette.primaryArgb
        }
        canvas.drawRoundRect(selectedRect, cellRadius, cellRadius, hiBorderPaint)
      }

      bitmap
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Renders the visual Suborbitals & Orbital Spin-Box Diagram [Bitmap] for [state.element]
   * inside a `14.dp` rounded inner card:
   * - Top header showing the active orbital block (`s-block`, `p-block`, `d-block`, `f-block`) and
   *   condensed suborbital configuration (`[Ar] 3d⁶ 4s²`).
   * - Middle visual row rendering Hund's-rule orbital spin-pair boxes (`⇅` paired / `↑` unpaired)
   *   for each outer/valence suborbital (`3d⁶`, `4s²`, etc.) color-coded by orbital block.
   * - Bottom subshell progression strip showing all populated suborbitals (`1s² 2s² 2p⁶ ...`) with
   *   proportional fill bars and oxidation states / melting & boiling point metadata.
   */
  fun renderSuborbitalsDiagramBitmap(
    state: ElementsWidgetState,
    widthPx: Int = 380,
    heightPx: Int = 180,
    cornerRadiusPx: Float = 28f,
  ): Bitmap? {
    val safeW = widthPx.coerceIn(140, 1000)
    val safeH = heightPx.coerceIn(72, 700)
    return try {
      val palette = state.palette
      val element = state.element
      val bitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      val w = safeW.toFloat()
      val h = safeH.toFloat()
      val cardRadius = min(cornerRadiusPx, min(w, h) * 0.35f)

      // 1. Inner Card Fill & Border
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

      val padX = w * 0.04f
      val padY = h * 0.08f
      val contentW = (w - padX * 2f).coerceAtLeast(60f)

      // 2. Top Header: "SUBORBITALS • d-block" on left, shells on right
      val headerTextSize = (h * 0.115f).coerceIn(9f, 20f)
      val headerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = state.categoryColors.accentArgb
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = headerTextSize
      }
      val headerRightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = palette.onSurfaceVariantArgb
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textSize = (headerTextSize * 0.88f).coerceIn(8f, 18f)
      }
      val headerBaseline = padY + headerTextSize * 0.88f
      canvas.drawText(
        "SUBORBITALS (${element.block.label.uppercase(Locale.ROOT)})",
        padX,
        headerBaseline,
        headerTitlePaint,
      )
      canvas.drawText(
        "Shells: ${element.shellsFormatted}",
        w - padX,
        headerBaseline,
        headerRightPaint,
      )

      // 3. Middle Section: Hund's-Rule Valence Suborbital Spin Boxes (⇅ / ↑)
      val valenceList = state.valenceSuborbitals
      val spinAreaTop = headerBaseline + h * 0.06f
      val spinAreaHeight = (h * 0.42f).coerceAtLeast(24f)
      val spinBoxH = (spinAreaHeight * 0.62f).coerceIn(14f, 44f)
      val spinBoxW = (spinBoxH * 0.92f).coerceIn(12f, 40f)
      val boxGap = max(2f, spinBoxW * 0.12f)
      val groupGap = max(6f, spinBoxW * 0.38f)

      val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
      val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = max(1f, spinBoxH * 0.06f)
      }
      val subLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textSize = (spinAreaHeight * 0.27f).coerceIn(8f, 17f)
      }
      val spinArrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textSize = (spinBoxH * 0.62f).coerceIn(8f, 24f)
      }

      // Compute scale if there are many orbital boxes (e.g. 4f¹⁴ + 5d¹⁰ + 6s² + 6p⁶)
      val totalBoxes = valenceList.sumOf { it.orbitalBoxCount }.coerceAtLeast(1)
      val rawTotalW = totalBoxes * spinBoxW +
        (totalBoxes - valenceList.size).coerceAtLeast(0) * boxGap +
        (valenceList.size - 1).coerceAtLeast(0) * groupGap
      val scale = if (rawTotalW > contentW) (contentW / rawTotalW).coerceAtLeast(0.42f) else 1f
      val effBoxW = spinBoxW * scale
      val effBoxH = spinBoxH * max(0.75f, scale)
      val effBoxGap = boxGap * scale
      val effGroupGap = groupGap * scale
      spinArrowPaint.textSize = (effBoxH * 0.62f).coerceIn(7f, 24f)

      var curX = padX
      val labelY = spinAreaTop + subLabelPaint.textSize * 0.92f
      val boxTopY = labelY + h * 0.035f
      val boxRadius = min(effBoxW, effBoxH) * 0.24f

      for (sub in valenceList) {
        val blkColors = resolveBlockColors(sub.block, palette)
        val groupW = sub.orbitalBoxCount * effBoxW + (sub.orbitalBoxCount - 1).coerceAtLeast(0) * effBoxGap
        if (curX + groupW > w - padX + 2f && curX > padX) break

        subLabelPaint.color = blkColors.accentArgb
        canvas.drawText(
          "${sub.formattedNotation} (${sub.electrons}/${sub.maxElectrons}e⁻)",
          curX,
          labelY,
          subLabelPaint,
        )

        var boxX = curX
        for (occ in sub.orbitalBoxOccupancies) {
          val bRect = RectF(boxX, boxTopY, boxX + effBoxW, boxTopY + effBoxH)
          fillPaint.color = if (occ > 0) {
            withAlpha(blkColors.containerArgb, if (occ == 2) 0.95f else 0.72f)
          } else {
            withAlpha(palette.surfaceArgb, 0.45f)
          }
          canvas.drawRoundRect(bRect, boxRadius, boxRadius, fillPaint)

          strokePaint.color = if (occ > 0) blkColors.borderArgb else palette.borderArgb
          canvas.drawRoundRect(bRect, boxRadius, boxRadius, strokePaint)

          if (occ > 0) {
            spinArrowPaint.color = blkColors.onContainerArgb
            val arrowText = if (occ == 2) "⇅" else "↑"
            val fm = spinArrowPaint.fontMetrics
            val bl = bRect.centerY() - (fm.ascent + fm.descent) * 0.5f
            canvas.drawText(arrowText, bRect.centerX(), bl, spinArrowPaint)
          }
          boxX += effBoxW + effBoxGap
        }

        curX += groupW + effGroupGap
      }

      // 4. Bottom Section: All Populated Suborbitals (1s -> outermost) Chips + Physical Metadata
      val allSubs = state.suborbitals
      val chipsTopY = (boxTopY + effBoxH + h * 0.06f).coerceAtMost(h - padY - h * 0.24f)
      val chipH = (h * 0.16f).coerceIn(12f, 28f)
      val chipGap = max(3f, w * 0.01f)
      val maxChipsToShow = 12
      val visibleSubs = if (allSubs.size > maxChipsToShow) {
        allSubs.takeLast(maxChipsToShow)
      } else {
        allSubs
      }
      if (visibleSubs.isNotEmpty()) {
        val chipW = ((contentW - chipGap * (visibleSubs.size - 1)) / visibleSubs.size.toFloat())
          .coerceIn(18f, w * 0.18f)
        val chipTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          textAlign = Paint.Align.CENTER
          typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
          textSize = (chipH * 0.56f).coerceIn(6.5f, 14f)
        }
        var chipX = padX
        for (sub in visibleSubs) {
          if (chipX + chipW > w - padX + 2f) break
          val blkColors = resolveBlockColors(sub.block, palette)
          val cRect = RectF(chipX, chipsTopY, chipX + chipW, chipsTopY + chipH)
          val cRadius = chipH * 0.35f

          fillPaint.color = withAlpha(blkColors.containerArgb, if (sub.isCore) 0.58f else 0.96f)
          canvas.drawRoundRect(cRect, cRadius, cRadius, fillPaint)

          strokePaint.color = if (sub.isCore) withAlpha(blkColors.borderArgb, 0.45f) else blkColors.accentArgb
          canvas.drawRoundRect(cRect, cRadius, cRadius, strokePaint)

          chipTextPaint.color = blkColors.onContainerArgb
          val fm = chipTextPaint.fontMetrics
          val bl = cRect.centerY() - (fm.ascent + fm.descent) * 0.5f
          canvas.drawText(sub.formattedNotation, cRect.centerX(), bl, chipTextPaint)

          chipX += chipW + chipGap
        }
      }

      // 5. Footer Metadata Line: Oxidation states, Melting point, Boiling point, Discovery
      val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = palette.onSurfaceVariantArgb
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textSize = (h * 0.095f).coerceIn(8f, 15f)
      }
      val meltStr = ElementTempUnit.CELSIUS.formatKelvin(element.meltingPointK)
      val boilStr = ElementTempUnit.CELSIUS.formatKelvin(element.boilingPointK)
      val footerText = "Oxidation: ${element.oxidationStates} • Melt: $meltStr • Boil: $boilStr"
      canvas.drawText(footerText, padX, h - padY * 0.55f, footerPaint)

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

  fun buildSelectElementBroadcastPendingIntent(
    context: Context,
    appWidgetId: Int,
    atomicNumber: Int,
  ): PendingIntent {
    val coercedZ = atomicNumber.coerceIn(1, 118)
    val intent = Intent(context, ElementsWidgetProvider::class.java).apply {
      action = ACTION_WIDGET_SELECT_ELEMENT
      putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      putExtra(EXTRA_ATOMIC_NUMBER, coercedZ)
      setPackage(context.packageName)
    }
    val requestCode = REQUEST_CODE_SELECT_ELEMENT_BASE + ((appWidgetId and 0x3F) * 128) + coercedZ
    return PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  /**
   * Builds the [RemoteViews] for an [ElementsWidgetProvider] instance:
   * - In [ElementsWidgetViewMode.TABLE] (main view): displays the full-bleed 18-column IUPAC
   *   Periodic Table (`R.id.widget_elements_table_view`) and wires all 118 element cells to
   *   [ACTION_WIDGET_SELECT_ELEMENT] so tapping any element transitions the widget to that
   *   element's information & suborbitals view.
   * - In [ElementsWidgetViewMode.ELEMENT_INFO]: displays the tapped element's details,
   *   Hund's-rule suborbital spin-box configuration, visual Suborbitals Diagram card, 2D Bohr
   *   electron shell diagram, and a `←` Back button (`R.id.widget_elements_back_to_table_button`)
   *   to return to the Periodic Table main view.
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

    // 2. Toggle between Main View (Periodic Table) and Element Information View
    val isTableView = state.viewMode == ElementsWidgetViewMode.TABLE
    views.setViewVisibility(
      R.id.widget_elements_table_view,
      if (isTableView) View.VISIBLE else View.GONE,
    )
    views.setViewVisibility(
      R.id.widget_elements_detail_view,
      if (isTableView) View.GONE else View.VISIBLE,
    )

    val innerCornerPx = INNER_CARD_CORNER_RADIUS_DP * density * scaleFactor

    // 3. Render Main Periodic Table Bitmap & Wire Per-Element Tap Listeners (1..118)
    val tableMapW = (bgWidthPx - (12f * density * scaleFactor).roundToInt()).coerceIn(140, 1000)
    val tableMapH = (bgHeightPx - (12f * density * scaleFactor).roundToInt()).coerceIn(72, 700)
    renderPeriodicTableMapBitmap(
      state = state,
      widthPx = tableMapW,
      heightPx = tableMapH,
      cornerRadiusPx = innerCornerPx,
    )?.let { mapBmp ->
      views.setImageViewBitmap(R.id.widget_elements_periodic_table_image, mapBmp)
    }

    for (z in 1..118) {
      val cellId = cellViewIdForAtomicNumber(z)
      val selectIntent = buildSelectElementBroadcastPendingIntent(
        context = context,
        appWidgetId = appWidgetId,
        atomicNumber = z,
      )
      views.setOnClickPendingIntent(cellId, selectIntent)
    }
    views.setOnClickPendingIntent(
      R.id.widget_element_cell_lanthanides,
      buildSelectElementBroadcastPendingIntent(context, appWidgetId, 57),
    )
    views.setOnClickPendingIntent(
      R.id.widget_element_cell_actinides,
      buildSelectElementBroadcastPendingIntent(context, appWidgetId, 89),
    )

    // 4. Populate Element Information & Suborbitals View
    val tileBadgeSizePx = (54f * density).roundToInt().coerceIn(72, 240)
    renderElementTileBadgeBitmap(
      state = state,
      sizePx = tileBadgeSizePx,
      cornerRadiusPx = INNER_CARD_CORNER_RADIUS_DP * density,
    )?.let { tileBmp ->
      views.setImageViewBitmap(R.id.widget_elements_tile_image, tileBmp)
    }

    views.setImageViewResource(R.id.widget_elements_back_to_table_bg, state.iconButtonBackgroundRes)
    views.setInt(R.id.widget_elements_back_to_table_bg, "setColorFilter", palette.primaryContainerArgb)
    views.setInt(R.id.widget_elements_back_to_table_icon, "setColorFilter", palette.primaryArgb)

    views.setTextViewText(R.id.widget_elements_title, state.titleText)
    views.setTextColor(R.id.widget_elements_title, palette.onSurfaceArgb)

    views.setTextViewText(R.id.widget_elements_category_pill, state.categoryText)
    views.setTextColor(R.id.widget_elements_category_pill, state.categoryColors.accentArgb)

    views.setTextViewText(R.id.widget_elements_subtitle, state.subtitleText)
    views.setTextColor(R.id.widget_elements_subtitle, palette.onSurfaceVariantArgb)

    views.setTextViewText(R.id.widget_elements_config_text, state.configText)
    views.setTextColor(R.id.widget_elements_config_text, palette.onSurfaceArgb)

    views.setTextViewText(R.id.widget_elements_suborbitals_text, state.suborbitalsText)
    views.setTextColor(R.id.widget_elements_suborbitals_text, state.categoryColors.accentArgb)

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

    // 5. Bottom Visual Section in Element Info View: Suborbitals Diagram Card + 2D Bohr Shell Diagram
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

      val visualHeightDp = (heightDp - 82f).coerceAtLeast(56f)
      val bohrSizeDp = if (showBohr) min(88f, visualHeightDp) else 0f
      val suborbitalsWidthDp = (widthDp - 20f - if (showBohr) (bohrSizeDp + 6f) else 0f).coerceAtLeast(120f)

      val suborbitalsWidthPx = (suborbitalsWidthDp * density * scaleFactor).roundToInt().coerceIn(140, 900)
      val suborbitalsHeightPx = (visualHeightDp * density * scaleFactor).roundToInt().coerceIn(72, 600)

      renderSuborbitalsDiagramBitmap(
        state = state,
        widthPx = suborbitalsWidthPx,
        heightPx = suborbitalsHeightPx,
        cornerRadiusPx = innerCornerPx,
      )?.let { subBmp ->
        views.setImageViewBitmap(R.id.widget_elements_suborbitals_image, subBmp)
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

    // 6. Click Listeners for Element Info View & Table Header
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
    val showTableIntent = buildActionBroadcastPendingIntent(
      context = context,
      appWidgetId = appWidgetId,
      action = ACTION_WIDGET_SHOW_TABLE,
      requestCodeBase = REQUEST_CODE_SHOW_TABLE_BASE,
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

    views.setOnClickPendingIntent(R.id.widget_elements_table_header_banner, openTableIntent)
    views.setOnClickPendingIntent(R.id.widget_elements_back_to_table_button, showTableIntent)
    views.setOnClickPendingIntent(R.id.widget_elements_back_to_table_icon, showTableIntent)
    views.setOnClickPendingIntent(R.id.widget_elements_tile_image, openInspectorIntent)
    views.setOnClickPendingIntent(R.id.widget_elements_text_container, openInspectorIntent)
    views.setOnClickPendingIntent(R.id.widget_elements_suborbitals_image, openInspectorIntent)
    views.setOnClickPendingIntent(R.id.widget_elements_bohr_image, openInspectorIntent)
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
      ACTION_WIDGET_SHOW_TABLE -> {
        showPeriodicTableInWidget(context = context, refreshWidgets = true)
        true
      }
      ACTION_WIDGET_SELECT_ELEMENT,
      ACTION_WIDGET_SET_ELEMENT -> {
        val targetZ = intent.getIntExtra(EXTRA_ATOMIC_NUMBER, -1)
        if (targetZ in 1..118) {
          selectElementInWidget(context = context, atomicNumber = targetZ, refreshWidgets = true)
          true
        } else {
          false
        }
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
 * Periodic Table of Elements Home Screen AppWidget whose main view is the interactive 18-column
 * IUPAC Periodic Table, and where tapping any element transitions the widget to that element's
 * information, suborbital configuration & spin-box diagram, and 2D Bohr electron shell diagram.
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
