package com.example.toolbox.ui.tools

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

const val ELEMENTS_PREFS_NAME = "elements_tool_preferences"
private const val PREF_KEY_FAVORITE_ATOMIC_NUMBERS = "favorite_atomic_numbers"

/**
 * Primary chemical categories of the Periodic Table of Elements.
 */
enum class ElementCategory(
  val label: String,
  val shortLabel: String,
  val description: String,
) {
  ALKALI_METAL(
    label = "Alkali Metal",
    shortLabel = "Alkali",
    description = "Group 1 highly reactive, soft, low-density metals with a single valence electron (ns¹).",
  ),
  ALKALINE_EARTH_METAL(
    label = "Alkaline Earth Metal",
    shortLabel = "Alkaline Earth",
    description = "Group 2 silvery-white, reactive metals with two valence electrons (ns²).",
  ),
  TRANSITION_METAL(
    label = "Transition Metal",
    shortLabel = "Transition",
    description = "d-block metals (Groups 3–12) characterized by variable oxidation states, high conductivity, and catalytic activity.",
  ),
  POST_TRANSITION_METAL(
    label = "Post-Transition Metal",
    shortLabel = "Post-Transition",
    description = "p-block metallic elements located between the transition metals and metalloids, typically softer with lower melting points.",
  ),
  METALLOID(
    label = "Metalloid",
    shortLabel = "Metalloid",
    description = "Semiconducting elements along the diagonal step line exhibiting intermediate properties between metals and nonmetals.",
  ),
  REACTIVE_NONMETAL(
    label = "Reactive Nonmetal",
    shortLabel = "Nonmetal",
    description = "Electronegative elements that form covalent compounds and acidic oxides, essential to organic and atmospheric chemistry.",
  ),
  HALOGEN(
    label = "Halogen",
    shortLabel = "Halogen",
    description = "Group 17 salt-forming nonmetals with seven valence electrons (ns² np⁵) that readily form −1 halide ions.",
  ),
  NOBLE_GAS(
    label = "Noble Gas",
    shortLabel = "Noble Gas",
    description = "Group 18 monatomic, odorless, colorless gases with filled valence shells and very low chemical reactivity.",
  ),
  LANTHANIDE(
    label = "Lanthanide",
    shortLabel = "Lanthanide",
    description = "4f-block rare-earth metals (Z = 57–71) prized for high-strength permanent magnets, lasers, and phosphors.",
  ),
  ACTINIDE(
    label = "Actinide",
    shortLabel = "Actinide",
    description = "5f-block radioactive heavy metals (Z = 89–103), including nuclear fuels uranium and plutonium.",
  ),
}

/**
 * Physical phase of an element at standard temperature and pressure (STP / 298.15 K, 1 atm),
 * or dynamically computed at any arbitrary temperature in Kelvin.
 */
enum class MatterPhase(
  val label: String,
  val badge: String,
) {
  SOLID("Solid", "s"),
  LIQUID("Liquid", "l"),
  GAS("Gas", "g"),
  UNKNOWN("Synthetic / Unknown", "?"),
}

/**
 * Orbital subshell block of the Periodic Table.
 */
enum class ElementBlock(
  val symbol: String,
  val label: String,
) {
  S("s", "s-block"),
  P("p", "p-block"),
  D("d", "d-block"),
  F("f", "f-block"),
}

/**
 * Temperature display unit for melting/boiling points and the interactive phase simulator.
 */
enum class ElementTempUnit(
  val symbol: String,
  val label: String,
) {
  KELVIN("K", "Kelvin"),
  CELSIUS("°C", "Celsius"),
  FAHRENHEIT("°F", "Fahrenheit"),
  ;

  fun next(): ElementTempUnit = when (this) {
    KELVIN -> CELSIUS
    CELSIUS -> FAHRENHEIT
    FAHRENHEIT -> KELVIN
  }

  fun fromKelvin(kelvin: Double): Double = when (this) {
    KELVIN -> kelvin
    CELSIUS -> kelvin - 273.15
    FAHRENHEIT -> (kelvin - 273.15) * 9.0 / 5.0 + 32.0
  }

  fun formatKelvin(kelvin: Double?): String {
    if (kelvin == null) return "—"
    val converted = fromKelvin(kelvin)
    return if (abs(converted - converted.toLong()) < 0.05) {
      String.format(Locale.US, "%d %s", converted.toLong(), symbol)
    } else {
      String.format(Locale.US, "%.2f %s", converted, symbol)
    }
  }
}

/**
 * Comprehensive data model for a single chemical element (Z = 1..118).
 *
 * Grid coordinates:
 * - [gridRow] (1..7 for main table periods 1–7; 9 for Lanthanides 57–71; 10 for Actinides 89–103)
 * - [gridCol] (1..18)
 */
data class ChemicalElement(
  val atomicNumber: Int,
  val symbol: String,
  val name: String,
  val atomicMass: Double,
  val category: ElementCategory,
  val group: Int?, // 1..18 (null for f-block lanthanides/actinides 58-71 & 90-103)
  val period: Int, // 1..7
  val block: ElementBlock,
  val gridRow: Int,
  val gridCol: Int,
  val electronConfiguration: String,
  val electronShells: List<Int>,
  val electronegativity: Double?, // Pauling scale
  val density: Double?, // g/cm³ at STP (or g/L for gases noted in formatted output)
  val meltingPointK: Double?, // Kelvin
  val boilingPointK: Double?, // Kelvin
  val atomicRadiusPm: Int?, // Picometers (empirical/calculated)
  val ionizationEnergyKjMol: Double?, // First ionization energy in kJ/mol
  val oxidationStates: String,
  val standardPhase: MatterPhase,
  val discoveryYear: String,
  val discoveredBy: String,
  val summary: String,
) {
  val isRadioactive: Boolean
    get() = atomicNumber == 43 || atomicNumber == 61 || atomicNumber >= 84

  val formattedAtomicMass: String
    get() = if (atomicMass == atomicMass.toLong().toDouble()) {
      "[${atomicMass.toLong()}] u"
    } else {
      String.format(Locale.US, "%.4f u", atomicMass).trimEnd('0').trimEnd('.').let {
        if (it.endsWith(" u")) it else "$it u"
      }
    }

  val compactAtomicMass: String
    get() = if (atomicMass == atomicMass.toLong().toDouble()) {
      "[${atomicMass.toLong()}]"
    } else {
      String.format(Locale.US, "%.3f", atomicMass).trimEnd('0').trimEnd('.')
    }

  val shellsFormatted: String
    get() = electronShells.joinToString(" · ")

  val groupLabel: String
    get() = group?.let { "Group $it" } ?: when (category) {
      ElementCategory.LANTHANIDE -> "Lanthanides (f-block)"
      ElementCategory.ACTINIDE -> "Actinides (f-block)"
      else -> "f-block"
    }

  /**
   * Computes the physical phase of this element at [temperatureK] Kelvin.
   */
  fun phaseAtTemperature(temperatureK: Double): MatterPhase {
    if (standardPhase == MatterPhase.UNKNOWN) return MatterPhase.UNKNOWN
    val melt = meltingPointK
    val boil = boilingPointK
    if (melt == null && boil == null) return standardPhase
    if (boil != null && temperatureK >= boil) return MatterPhase.GAS
    if (melt != null && temperatureK >= melt) return MatterPhase.LIQUID
    if (melt != null && temperatureK < melt) return MatterPhase.SOLID
    return standardPhase
  }
}

/**
 * Breakdown of a single element's contribution inside a parsed chemical formula (e.g. H₂O, C₆H₁₂O₆, Ca(OH)₂).
 */
data class MolarMassElementComponent(
  val element: ChemicalElement,
  val count: Int,
  val subtotalMass: Double,
  val massPercentage: Double,
)

/**
 * Result of parsing and evaluating a chemical formula for molar mass & elemental composition.
 */
sealed class MolarMassResult {
  data class Success(
    val formula: String,
    val totalMolarMass: Double,
    val totalAtoms: Int,
    val components: List<MolarMassElementComponent>,
  ) : MolarMassResult() {
    val formattedMolarMass: String
      get() = String.format(Locale.US, "%.4f g/mol", totalMolarMass)
  }

  data class Error(val message: String) : MolarMassResult()
}

/**
 * Persistence interface for user-favorited elements.
 */
interface ElementsRepository {
  fun loadFavoriteAtomicNumbers(): Set<Int>
  fun saveFavoriteAtomicNumbers(favorites: Set<Int>)
  fun toggleFavorite(atomicNumber: Int): Set<Int> {
    val current = loadFavoriteAtomicNumbers().toMutableSet()
    if (atomicNumber in current) {
      current.remove(atomicNumber)
    } else {
      current.add(atomicNumber)
    }
    saveFavoriteAtomicNumbers(current)
    return current
  }
}

class InMemoryElementsRepository(
  initialFavorites: Set<Int> = emptySet(),
) : ElementsRepository {
  private var favorites: Set<Int> = initialFavorites.toSet()

  override fun loadFavoriteAtomicNumbers(): Set<Int> = favorites

  override fun saveFavoriteAtomicNumbers(favorites: Set<Int>) {
    this.favorites = favorites.filter { it in 1..118 }.toSet()
  }
}

class SharedPreferencesElementsRepository(
  private val prefs: SharedPreferences,
) : ElementsRepository {
  constructor(context: Context) : this(
    context.getSharedPreferences(ELEMENTS_PREFS_NAME, Context.MODE_PRIVATE),
  )

  override fun loadFavoriteAtomicNumbers(): Set<Int> {
    val raw = prefs.getString(PREF_KEY_FAVORITE_ATOMIC_NUMBERS, null) ?: return emptySet()
    if (raw.isBlank()) return emptySet()
    return raw.split(",")
      .mapNotNull { it.trim().toIntOrNull() }
      .filter { it in 1..118 }
      .toSet()
  }

  override fun saveFavoriteAtomicNumbers(favorites: Set<Int>) {
    val cleaned = favorites.filter { it in 1..118 }.sorted().joinToString(",")
    prefs.edit().putString(PREF_KEY_FAVORITE_ATOMIC_NUMBERS, cleaned).apply()
  }
}

/**
 * Computed layout metrics (in pixels) for the 18-column Periodic Table grid at a given
 * viewport width and zoom multiplier.
 *
 * At [PeriodicTableEngine.DEFAULT_TABLE_ZOOM] (`1.0f`), the full 18-group table + period badge
 * column fits within [viewportWidthPx] across the full device width.
 */
data class PeriodicTableGridMetrics(
  val viewportWidthPx: Int,
  val tableWidthPx: Int,
  val contentWidthPx: Int,
  val horizontalPaddingPx: Int,
  val periodColumnWidthPx: Int,
  val tileWidthPx: Int,
  val tileHeightPx: Int,
  val tileSpacingPx: Int,
)

/**
 * Core engine and complete IUPAC 118-element database for the Periodic Table of Elements tool.
 */
object PeriodicTableEngine {

  const val STP_TEMPERATURE_K = 298.15
  const val MIN_SIMULATOR_TEMP_K = 0f
  const val MAX_SIMULATOR_TEMP_K = 6000f

  const val DEFAULT_TABLE_ZOOM = 1.0f
  const val MIN_TABLE_ZOOM = 1.0f
  const val MAX_TABLE_ZOOM = 4.0f

  const val BASE_TILE_WIDTH_DP = 52f
  const val BASE_TILE_HEIGHT_DP = 58f
  const val BASE_TILE_SPACING_DP = 3f
  const val BASE_PERIOD_COLUMN_WIDTH_DP = 22f
  const val BASE_TABLE_CONTENT_WIDTH_DP =
    BASE_PERIOD_COLUMN_WIDTH_DP + (18f * BASE_TILE_WIDTH_DP) + (18f * BASE_TILE_SPACING_DP) // 1012f

  val ALL_ELEMENTS: List<ChemicalElement> = listOf(
    // ==================== PERIOD 1 ====================
    ChemicalElement(
      atomicNumber = 1, symbol = "H", name = "Hydrogen", atomicMass = 1.008,
      category = ElementCategory.REACTIVE_NONMETAL, group = 1, period = 1, block = ElementBlock.S,
      gridRow = 1, gridCol = 1, electronConfiguration = "1s¹", electronShells = listOf(1),
      electronegativity = 2.20, density = 0.00008988, meltingPointK = 13.99, boilingPointK = 20.271,
      atomicRadiusPm = 53, ionizationEnergyKjMol = 1312.0, oxidationStates = "-1, +1",
      standardPhase = MatterPhase.GAS, discoveryYear = "1766", discoveredBy = "Henry Cavendish",
      summary = "The lightest and most abundant chemical element in the universe (~75% of normal matter by mass), powering stellar fusion and forming water and all organic compounds.",
    ),
    ChemicalElement(
      atomicNumber = 2, symbol = "He", name = "Helium", atomicMass = 4.0026,
      category = ElementCategory.NOBLE_GAS, group = 18, period = 1, block = ElementBlock.S,
      gridRow = 1, gridCol = 18, electronConfiguration = "1s²", electronShells = listOf(2),
      electronegativity = null, density = 0.0001785, meltingPointK = 0.95, boilingPointK = 4.222,
      atomicRadiusPm = 31, ionizationEnergyKjMol = 2372.3, oxidationStates = "0",
      standardPhase = MatterPhase.GAS, discoveryYear = "1868", discoveredBy = "Pierre Janssen & Norman Lockyer",
      summary = "A colorless, inert noble gas with the lowest boiling point of all elements; widely used in cryogenics, MRI superconducting magnets, and deep-sea breathing mixtures.",
    ),

    // ==================== PERIOD 2 ====================
    ChemicalElement(
      atomicNumber = 3, symbol = "Li", name = "Lithium", atomicMass = 6.94,
      category = ElementCategory.ALKALI_METAL, group = 1, period = 2, block = ElementBlock.S,
      gridRow = 2, gridCol = 1, electronConfiguration = "[He] 2s¹", electronShells = listOf(2, 1),
      electronegativity = 0.98, density = 0.534, meltingPointK = 453.65, boilingPointK = 1603.0,
      atomicRadiusPm = 167, ionizationEnergyKjMol = 520.2, oxidationStates = "+1",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1817", discoveredBy = "Johan August Arfwedson",
      summary = "The least dense solid element and softest alkali metal, foundational to high-energy-density rechargeable lithium-ion batteries and heat-resistant ceramics.",
    ),
    ChemicalElement(
      atomicNumber = 4, symbol = "Be", name = "Beryllium", atomicMass = 9.0122,
      category = ElementCategory.ALKALINE_EARTH_METAL, group = 2, period = 2, block = ElementBlock.S,
      gridRow = 2, gridCol = 2, electronConfiguration = "[He] 2s²", electronShells = listOf(2, 2),
      electronegativity = 1.57, density = 1.85, meltingPointK = 1560.0, boilingPointK = 2742.0,
      atomicRadiusPm = 112, ionizationEnergyKjMol = 899.5, oxidationStates = "+2",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1798", discoveredBy = "Louis Nicolas Vauquelin",
      summary = "A lightweight, extraordinarily stiff alkaline earth metal transparent to X-rays and used in aerospace mirrors (such as the James Webb Space Telescope) and non-sparking alloys.",
    ),
    ChemicalElement(
      atomicNumber = 5, symbol = "B", name = "Boron", atomicMass = 10.81,
      category = ElementCategory.METALLOID, group = 13, period = 2, block = ElementBlock.P,
      gridRow = 2, gridCol = 13, electronConfiguration = "[He] 2s² 2p¹", electronShells = listOf(2, 3),
      electronegativity = 2.04, density = 2.34, meltingPointK = 2349.0, boilingPointK = 4200.0,
      atomicRadiusPm = 87, ionizationEnergyKjMol = 800.6, oxidationStates = "+3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1808", discoveredBy = "Joseph Louis Gay-Lussac & Louis Jacques Thénard",
      summary = "A hard refractory metalloid essential to borosilicate laboratory glass (Pyrex), fiberglass, neodymium magnets, and p-type silicon semiconductor doping.",
    ),
    ChemicalElement(
      atomicNumber = 6, symbol = "C", name = "Carbon", atomicMass = 12.011,
      category = ElementCategory.REACTIVE_NONMETAL, group = 14, period = 2, block = ElementBlock.P,
      gridRow = 2, gridCol = 14, electronConfiguration = "[He] 2s² 2p²", electronShells = listOf(2, 4),
      electronegativity = 2.55, density = 2.267, meltingPointK = 3800.0, boilingPointK = 4300.0,
      atomicRadiusPm = 67, ionizationEnergyKjMol = 1086.5, oxidationStates = "-4, -3, -2, -1, 0, +1, +2, +3, +4",
      standardPhase = MatterPhase.SOLID, discoveryYear = "Ancient", discoveredBy = "Known since antiquity",
      summary = "The tetravalent chemical backbone of all known organic life, forming diverse allotropes including diamond, graphite, graphene, and fullerenes.",
    ),
    ChemicalElement(
      atomicNumber = 7, symbol = "N", name = "Nitrogen", atomicMass = 14.007,
      category = ElementCategory.REACTIVE_NONMETAL, group = 15, period = 2, block = ElementBlock.P,
      gridRow = 2, gridCol = 15, electronConfiguration = "[He] 2s² 2p³", electronShells = listOf(2, 5),
      electronegativity = 3.04, density = 0.0012506, meltingPointK = 63.15, boilingPointK = 77.355,
      atomicRadiusPm = 56, ionizationEnergyKjMol = 1402.3, oxidationStates = "-3, -2, -1, +1, +2, +3, +4, +5",
      standardPhase = MatterPhase.GAS, discoveryYear = "1772", discoveredBy = "Daniel Rutherford",
      summary = "Constitutes 78% of Earth's atmosphere as a strongly triple-bonded diatomic gas (N₂) and is indispensable for amino acids, nucleic acids, and agricultural fertilizers.",
    ),
    ChemicalElement(
      atomicNumber = 8, symbol = "O", name = "Oxygen", atomicMass = 15.999,
      category = ElementCategory.REACTIVE_NONMETAL, group = 16, period = 2, block = ElementBlock.P,
      gridRow = 2, gridCol = 16, electronConfiguration = "[He] 2s² 2p⁴", electronShells = listOf(2, 6),
      electronegativity = 3.44, density = 0.001429, meltingPointK = 54.36, boilingPointK = 90.188,
      atomicRadiusPm = 48, ionizationEnergyKjMol = 1313.9, oxidationStates = "-2, -1, +1, +2",
      standardPhase = MatterPhase.GAS, discoveryYear = "1774", discoveredBy = "Carl Wilhelm Scheele & Joseph Priestley",
      summary = "A highly electronegative nonmetal making up 21% of Earth's atmosphere and nearly half of Earth's crust by mass; essential for aerobic respiration and combustion.",
    ),
    ChemicalElement(
      atomicNumber = 9, symbol = "F", name = "Fluorine", atomicMass = 18.998,
      category = ElementCategory.HALOGEN, group = 17, period = 2, block = ElementBlock.P,
      gridRow = 2, gridCol = 17, electronConfiguration = "[He] 2s² 2p⁵", electronShells = listOf(2, 7),
      electronegativity = 3.98, density = 0.001696, meltingPointK = 53.48, boilingPointK = 85.03,
      atomicRadiusPm = 42, ionizationEnergyKjMol = 1681.0, oxidationStates = "-1",
      standardPhase = MatterPhase.GAS, discoveryYear = "1886", discoveredBy = "Henri Moissan",
      summary = "The most electronegative and chemically reactive of all elements, used in fluoropolymers (PTFE/Teflon), dental fluoride, and uranium enrichment.",
    ),
    ChemicalElement(
      atomicNumber = 10, symbol = "Ne", name = "Neon", atomicMass = 20.180,
      category = ElementCategory.NOBLE_GAS, group = 18, period = 2, block = ElementBlock.P,
      gridRow = 2, gridCol = 18, electronConfiguration = "[He] 2s² 2p⁶", electronShells = listOf(2, 8),
      electronegativity = null, density = 0.0008999, meltingPointK = 24.56, boilingPointK = 27.104,
      atomicRadiusPm = 38, ionizationEnergyKjMol = 2080.7, oxidationStates = "0",
      standardPhase = MatterPhase.GAS, discoveryYear = "1898", discoveredBy = "William Ramsay & Morris Travers",
      summary = "A monatomic noble gas that emits a brilliant reddish-orange glow in high-voltage discharge tubes, high-vacuum indicators, and helium-neon lasers.",
    ),

    // ==================== PERIOD 3 ====================
    ChemicalElement(
      atomicNumber = 11, symbol = "Na", name = "Sodium", atomicMass = 22.990,
      category = ElementCategory.ALKALI_METAL, group = 1, period = 3, block = ElementBlock.S,
      gridRow = 3, gridCol = 1, electronConfiguration = "[Ne] 3s¹", electronShells = listOf(2, 8, 1),
      electronegativity = 0.93, density = 0.968, meltingPointK = 370.944, boilingPointK = 1156.09,
      atomicRadiusPm = 190, ionizationEnergyKjMol = 495.8, oxidationStates = "-1, +1",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1807", discoveredBy = "Humphry Davy",
      summary = "A soft, silvery alkali metal that reacts vigorously with water; its ion (Na⁺) is essential for nerve impulse transmission, cellular osmoregulation, and table salt (NaCl).",
    ),
    ChemicalElement(
      atomicNumber = 12, symbol = "Mg", name = "Magnesium", atomicMass = 24.305,
      category = ElementCategory.ALKALINE_EARTH_METAL, group = 2, period = 3, block = ElementBlock.S,
      gridRow = 3, gridCol = 2, electronConfiguration = "[Ne] 3s²", electronShells = listOf(2, 8, 2),
      electronegativity = 1.31, density = 1.738, meltingPointK = 923.0, boilingPointK = 1363.0,
      atomicRadiusPm = 145, ionizationEnergyKjMol = 737.7, oxidationStates = "+2",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1755", discoveredBy = "Joseph Black",
      summary = "A lightweight structural metal that burns with a brilliant white light and sits at the center of chlorophyll molecules in photosynthetic plants.",
    ),
    ChemicalElement(
      atomicNumber = 13, symbol = "Al", name = "Aluminum", atomicMass = 26.982,
      category = ElementCategory.POST_TRANSITION_METAL, group = 13, period = 3, block = ElementBlock.P,
      gridRow = 3, gridCol = 13, electronConfiguration = "[Ne] 3s² 3p¹", electronShells = listOf(2, 8, 3),
      electronegativity = 1.61, density = 2.70, meltingPointK = 933.47, boilingPointK = 2743.0,
      atomicRadiusPm = 118, ionizationEnergyKjMol = 577.5, oxidationStates = "+3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1825", discoveredBy = "Hans Christian Ørsted",
      summary = "The most abundant metal in Earth's crust, valued for its low density, corrosion-resistant oxide passivation layer, and recyclability in aerospace and packaging.",
    ),
    ChemicalElement(
      atomicNumber = 14, symbol = "Si", name = "Silicon", atomicMass = 28.085,
      category = ElementCategory.METALLOID, group = 14, period = 3, block = ElementBlock.P,
      gridRow = 3, gridCol = 14, electronConfiguration = "[Ne] 3s² 3p²", electronShells = listOf(2, 8, 4),
      electronegativity = 1.90, density = 2.329, meltingPointK = 1687.0, boilingPointK = 3538.0,
      atomicRadiusPm = 111, ionizationEnergyKjMol = 786.5, oxidationStates = "-4, +2, +4",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1824", discoveredBy = "Jöns Jacob Berzelius",
      summary = "A tetravalent metalloid and the second most abundant element in Earth's crust, forming the semiconductor foundation of integrated circuits, CPUs, and solar cells.",
    ),
    ChemicalElement(
      atomicNumber = 15, symbol = "P", name = "Phosphorus", atomicMass = 30.974,
      category = ElementCategory.REACTIVE_NONMETAL, group = 15, period = 3, block = ElementBlock.P,
      gridRow = 3, gridCol = 15, electronConfiguration = "[Ne] 3s² 3p³", electronShells = listOf(2, 8, 5),
      electronegativity = 2.19, density = 1.823, meltingPointK = 317.3, boilingPointK = 553.7,
      atomicRadiusPm = 98, ionizationEnergyKjMol = 1011.8, oxidationStates = "-3, +3, +5",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1669", discoveredBy = "Hennig Brand",
      summary = "A multivalent nonmetal existing as white, red, and black allotropes; essential to ATP cellular energy transfer, DNA/RNA backbones, and bone hydroxyapatite.",
    ),
    ChemicalElement(
      atomicNumber = 16, symbol = "S", name = "Sulfur", atomicMass = 32.06,
      category = ElementCategory.REACTIVE_NONMETAL, group = 16, period = 3, block = ElementBlock.P,
      gridRow = 3, gridCol = 16, electronConfiguration = "[Ne] 3s² 3p⁴", electronShells = listOf(2, 8, 6),
      electronegativity = 2.58, density = 2.07, meltingPointK = 388.36, boilingPointK = 717.8,
      atomicRadiusPm = 88, ionizationEnergyKjMol = 999.6, oxidationStates = "-2, +2, +4, +6",
      standardPhase = MatterPhase.SOLID, discoveryYear = "Ancient", discoveredBy = "Known since antiquity",
      summary = "A bright yellow crystalline nonmetal occurring near volcanic vents; used to produce sulfuric acid, vulcanize rubber, and form disulfide bridges in proteins.",
    ),
    ChemicalElement(
      atomicNumber = 17, symbol = "Cl", name = "Chlorine", atomicMass = 35.45,
      category = ElementCategory.HALOGEN, group = 17, period = 3, block = ElementBlock.P,
      gridRow = 3, gridCol = 17, electronConfiguration = "[Ne] 3s² 3p⁵", electronShells = listOf(2, 8, 7),
      electronegativity = 3.16, density = 0.0032, meltingPointK = 171.6, boilingPointK = 239.11,
      atomicRadiusPm = 79, ionizationEnergyKjMol = 1251.2, oxidationStates = "-1, +1, +3, +5, +7",
      standardPhase = MatterPhase.GAS, discoveryYear = "1774", discoveredBy = "Carl Wilhelm Scheele",
      summary = "A yellow-green halogen gas and powerful oxidizing agent critical for drinking water sanitation, PVC plastics, and biological chloride electrolytes.",
    ),
    ChemicalElement(
      atomicNumber = 18, symbol = "Ar", name = "Argon", atomicMass = 39.948,
      category = ElementCategory.NOBLE_GAS, group = 18, period = 3, block = ElementBlock.P,
      gridRow = 3, gridCol = 18, electronConfiguration = "[Ne] 3s² 3p⁶", electronShells = listOf(2, 8, 8),
      electronegativity = null, density = 0.001784, meltingPointK = 83.81, boilingPointK = 87.302,
      atomicRadiusPm = 71, ionizationEnergyKjMol = 1520.6, oxidationStates = "0",
      standardPhase = MatterPhase.GAS, discoveryYear = "1894", discoveredBy = "Lord Rayleigh & William Ramsay",
      summary = "The third most abundant gas in Earth's atmosphere (0.93%), widely used as an inert shielding gas in TIG/MIG arc welding and insulated double-pane windows.",
    ),

    // ==================== PERIOD 4 ====================
    ChemicalElement(
      atomicNumber = 19, symbol = "K", name = "Potassium", atomicMass = 39.098,
      category = ElementCategory.ALKALI_METAL, group = 1, period = 4, block = ElementBlock.S,
      gridRow = 4, gridCol = 1, electronConfiguration = "[Ar] 4s¹", electronShells = listOf(2, 8, 8, 1),
      electronegativity = 0.82, density = 0.862, meltingPointK = 336.7, boilingPointK = 1032.0,
      atomicRadiusPm = 243, ionizationEnergyKjMol = 418.8, oxidationStates = "+1",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1807", discoveredBy = "Humphry Davy",
      summary = "A reactive alkali metal that burns with a lilac flame; intracellular K⁺ ions are vital for cardiac rhythm, neuronal action potentials, and plant nutrition.",
    ),
    ChemicalElement(
      atomicNumber = 20, symbol = "Ca", name = "Calcium", atomicMass = 40.078,
      category = ElementCategory.ALKALINE_EARTH_METAL, group = 2, period = 4, block = ElementBlock.S,
      gridRow = 4, gridCol = 2, electronConfiguration = "[Ar] 4s²", electronShells = listOf(2, 8, 8, 2),
      electronegativity = 1.00, density = 1.55, meltingPointK = 1115.0, boilingPointK = 1757.0,
      atomicRadiusPm = 194, ionizationEnergyKjMol = 589.8, oxidationStates = "+2",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1808", discoveredBy = "Humphry Davy",
      summary = "The fifth most abundant element in Earth's crust and the most abundant metal in the human body, forming bones, teeth, shells, limestone, and Portland cement.",
    ),
    ChemicalElement(
      atomicNumber = 21, symbol = "Sc", name = "Scandium", atomicMass = 44.956,
      category = ElementCategory.TRANSITION_METAL, group = 3, period = 4, block = ElementBlock.D,
      gridRow = 4, gridCol = 3, electronConfiguration = "[Ar] 3d¹ 4s²", electronShells = listOf(2, 8, 9, 2),
      electronegativity = 1.36, density = 2.985, meltingPointK = 1814.0, boilingPointK = 3109.0,
      atomicRadiusPm = 184, ionizationEnergyKjMol = 633.1, oxidationStates = "+3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1879", discoveredBy = "Lars Fredrik Nilson",
      summary = "The first d-block transition metal, alloyed with aluminum to create high-strength, weldable aerospace frames and stadium metal-halide lighting.",
    ),
    ChemicalElement(
      atomicNumber = 22, symbol = "Ti", name = "Titanium", atomicMass = 47.867,
      category = ElementCategory.TRANSITION_METAL, group = 4, period = 4, block = ElementBlock.D,
      gridRow = 4, gridCol = 4, electronConfiguration = "[Ar] 3d² 4s²", electronShells = listOf(2, 8, 10, 2),
      electronegativity = 1.54, density = 4.506, meltingPointK = 1941.0, boilingPointK = 3560.0,
      atomicRadiusPm = 176, ionizationEnergyKjMol = 658.8, oxidationStates = "+2, +3, +4",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1791", discoveredBy = "William Gregor",
      summary = "Renowned for having the highest strength-to-density ratio of any metallic element and outstanding corrosion resistance, used in jet turbines, biocompatible implants, and TiO₂ white pigment.",
    ),
    ChemicalElement(
      atomicNumber = 23, symbol = "V", name = "Vanadium", atomicMass = 50.942,
      category = ElementCategory.TRANSITION_METAL, group = 5, period = 4, block = ElementBlock.D,
      gridRow = 4, gridCol = 5, electronConfiguration = "[Ar] 3d³ 4s²", electronShells = listOf(2, 8, 11, 2),
      electronegativity = 1.63, density = 6.11, meltingPointK = 2183.0, boilingPointK = 3680.0,
      atomicRadiusPm = 171, ionizationEnergyKjMol = 650.9, oxidationStates = "+2, +3, +4, +5",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1801", discoveredBy = "Andrés Manuel del Río",
      summary = "A hard, ductile transition metal added to tool steels (chrome-vanadium) for shock resistance and utilized in grid-scale vanadium redox flow batteries.",
    ),
    ChemicalElement(
      atomicNumber = 24, symbol = "Cr", name = "Chromium", atomicMass = 51.996,
      category = ElementCategory.TRANSITION_METAL, group = 6, period = 4, block = ElementBlock.D,
      gridRow = 4, gridCol = 6, electronConfiguration = "[Ar] 3d⁵ 4s¹", electronShells = listOf(2, 8, 13, 1),
      electronegativity = 1.66, density = 7.15, meltingPointK = 2180.0, boilingPointK = 2944.0,
      atomicRadiusPm = 166, ionizationEnergyKjMol = 652.9, oxidationStates = "+2, +3, +6",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1797", discoveredBy = "Louis Nicolas Vauquelin",
      summary = "A lustrous, hard metal that gives stainless steel its corrosion resistance and imparts deep red and emerald green colors to rubies and emeralds.",
    ),
    ChemicalElement(
      atomicNumber = 25, symbol = "Mn", name = "Manganese", atomicMass = 54.938,
      category = ElementCategory.TRANSITION_METAL, group = 7, period = 4, block = ElementBlock.D,
      gridRow = 4, gridCol = 7, electronConfiguration = "[Ar] 3d⁵ 4s²", electronShells = listOf(2, 8, 13, 2),
      electronegativity = 1.55, density = 7.21, meltingPointK = 1519.0, boilingPointK = 2334.0,
      atomicRadiusPm = 161, ionizationEnergyKjMol = 717.3, oxidationStates = "+2, +3, +4, +6, +7",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1774", discoveredBy = "Johan Gottlieb Gahn",
      summary = "An essential deoxidizer and desulfurizer in steelmaking, a cathode component in alkaline and lithium-ion batteries, and a cofactor in photosynthetic water splitting.",
    ),
    ChemicalElement(
      atomicNumber = 26, symbol = "Fe", name = "Iron", atomicMass = 55.845,
      category = ElementCategory.TRANSITION_METAL, group = 8, period = 4, block = ElementBlock.D,
      gridRow = 4, gridCol = 8, electronConfiguration = "[Ar] 3d⁶ 4s²", electronShells = listOf(2, 8, 14, 2),
      electronegativity = 1.83, density = 7.874, meltingPointK = 1811.0, boilingPointK = 3134.0,
      atomicRadiusPm = 156, ionizationEnergyKjMol = 762.5, oxidationStates = "+2, +3, +6",
      standardPhase = MatterPhase.SOLID, discoveryYear = "Ancient", discoveredBy = "Known since antiquity",
      summary = "The most mass-abundant element on Earth (dominating Earth's core), the primary constituent of steel, and the oxygen-binding center of hemoglobin in red blood cells.",
    ),
    ChemicalElement(
      atomicNumber = 27, symbol = "Co", name = "Cobalt", atomicMass = 58.933,
      category = ElementCategory.TRANSITION_METAL, group = 9, period = 4, block = ElementBlock.D,
      gridRow = 4, gridCol = 9, electronConfiguration = "[Ar] 3d⁷ 4s²", electronShells = listOf(2, 8, 15, 2),
      electronegativity = 1.88, density = 8.90, meltingPointK = 1768.0, boilingPointK = 3200.0,
      atomicRadiusPm = 152, ionizationEnergyKjMol = 760.4, oxidationStates = "+2, +3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1735", discoveredBy = "Georg Brandt",
      summary = "A ferromagnetic transition metal famous for deep cobalt-blue glass pigments, high-temperature superalloys, samarium-cobalt magnets, and Vitamin B₁₂ (cobalamin).",
    ),
    ChemicalElement(
      atomicNumber = 28, symbol = "Ni", name = "Nickel", atomicMass = 58.693,
      category = ElementCategory.TRANSITION_METAL, group = 10, period = 4, block = ElementBlock.D,
      gridRow = 4, gridCol = 10, electronConfiguration = "[Ar] 3d⁸ 4s²", electronShells = listOf(2, 8, 16, 2),
      electronegativity = 1.91, density = 8.908, meltingPointK = 1728.0, boilingPointK = 3186.0,
      atomicRadiusPm = 149, ionizationEnergyKjMol = 737.1, oxidationStates = "+2, +3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1751", discoveredBy = "Axel Fredrik Cronstedt",
      summary = "A corrosion-resistant ferromagnetic metal used extensively in austenitic stainless steel, nichrome heating elements, coinage, and high-capacity EV battery cathodes.",
    ),
    ChemicalElement(
      atomicNumber = 29, symbol = "Cu", name = "Copper", atomicMass = 63.546,
      category = ElementCategory.TRANSITION_METAL, group = 11, period = 4, block = ElementBlock.D,
      gridRow = 4, gridCol = 11, electronConfiguration = "[Ar] 3d¹⁰ 4s¹", electronShells = listOf(2, 8, 18, 1),
      electronegativity = 1.90, density = 8.96, meltingPointK = 1357.77, boilingPointK = 2835.0,
      atomicRadiusPm = 145, ionizationEnergyKjMol = 745.5, oxidationStates = "+1, +2",
      standardPhase = MatterPhase.SOLID, discoveryYear = "Ancient", discoveredBy = "Known since antiquity",
      summary = "A reddish-orange malleable metal with exceptional electrical and thermal conductivity, foundational to electrical wiring, motors, bronze, and brass.",
    ),
    ChemicalElement(
      atomicNumber = 30, symbol = "Zn", name = "Zinc", atomicMass = 65.38,
      category = ElementCategory.TRANSITION_METAL, group = 12, period = 4, block = ElementBlock.D,
      gridRow = 4, gridCol = 12, electronConfiguration = "[Ar] 3d¹⁰ 4s²", electronShells = listOf(2, 8, 18, 2),
      electronegativity = 1.65, density = 7.14, meltingPointK = 692.68, boilingPointK = 1180.0,
      atomicRadiusPm = 142, ionizationEnergyKjMol = 906.4, oxidationStates = "+2",
      standardPhase = MatterPhase.SOLID, discoveryYear = "Ancient", discoveredBy = "Indian metallurgists & Andreas Marggraf",
      summary = "A bluish-silver metal used to galvanize steel against rust, alloyed with copper to make brass, and required as a catalytic cofactor in over 300 human enzymes.",
    ),
    ChemicalElement(
      atomicNumber = 31, symbol = "Ga", name = "Gallium", atomicMass = 69.723,
      category = ElementCategory.POST_TRANSITION_METAL, group = 13, period = 4, block = ElementBlock.P,
      gridRow = 4, gridCol = 13, electronConfiguration = "[Ar] 3d¹⁰ 4s² 4p¹", electronShells = listOf(2, 8, 18, 3),
      electronegativity = 1.81, density = 5.91, meltingPointK = 302.9146, boilingPointK = 2673.0,
      atomicRadiusPm = 136, ionizationEnergyKjMol = 578.8, oxidationStates = "+3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1875", discoveredBy = "Lecoq de Boisbaudran",
      summary = "A soft metal that melts just above room temperature (29.76 °C) in the palm of a hand; vital for gallium nitride (GaN) blue/white LEDs and high-frequency power electronics.",
    ),
    ChemicalElement(
      atomicNumber = 32, symbol = "Ge", name = "Germanium", atomicMass = 72.630,
      category = ElementCategory.METALLOID, group = 14, period = 4, block = ElementBlock.P,
      gridRow = 4, gridCol = 14, electronConfiguration = "[Ar] 3d¹⁰ 4s² 4p²", electronShells = listOf(2, 8, 18, 4),
      electronegativity = 2.01, density = 5.323, meltingPointK = 1211.4, boilingPointK = 3106.0,
      atomicRadiusPm = 125, ionizationEnergyKjMol = 762.0, oxidationStates = "-4, +2, +4",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1886", discoveredBy = "Clemens Winkler",
      summary = "A lustrous semiconductor metalloid predicted by Mendeleev as 'ekasilicon', used in fiber-optic cores, infrared night-vision optics, and SiGe high-speed transistors.",
    ),
    ChemicalElement(
      atomicNumber = 33, symbol = "As", name = "Arsenic", atomicMass = 74.922,
      category = ElementCategory.METALLOID, group = 15, period = 4, block = ElementBlock.P,
      gridRow = 4, gridCol = 15, electronConfiguration = "[Ar] 3d¹⁰ 4s² 4p³", electronShells = listOf(2, 8, 18, 5),
      electronegativity = 2.18, density = 5.727, meltingPointK = 1090.0, boilingPointK = 887.0,
      atomicRadiusPm = 114, ionizationEnergyKjMol = 947.0, oxidationStates = "-3, +3, +5",
      standardPhase = MatterPhase.SOLID, discoveryYear = "Ancient", discoveredBy = "Albertus Magnus",
      summary = "A toxic metalloid used in gallium arsenide (GaAs) high-efficiency solar cells, laser diodes, and lead-acid battery grid alloys.",
    ),
    ChemicalElement(
      atomicNumber = 34, symbol = "Se", name = "Selenium", atomicMass = 78.971,
      category = ElementCategory.REACTIVE_NONMETAL, group = 16, period = 4, block = ElementBlock.P,
      gridRow = 4, gridCol = 16, electronConfiguration = "[Ar] 3d¹⁰ 4s² 4p⁴", electronShells = listOf(2, 8, 18, 6),
      electronegativity = 2.55, density = 4.81, meltingPointK = 494.0, boilingPointK = 958.0,
      atomicRadiusPm = 103, ionizationEnergyKjMol = 941.0, oxidationStates = "-2, +2, +4, +6",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1817", discoveredBy = "Jöns Jacob Berzelius",
      summary = "A photoconductive nonmetal used in glass decolorizing, solar cells, X-ray detectors, and required as a trace antioxidant micronutrient in selenoproteins.",
    ),
    ChemicalElement(
      atomicNumber = 35, symbol = "Br", name = "Bromine", atomicMass = 79.904,
      category = ElementCategory.HALOGEN, group = 17, period = 4, block = ElementBlock.P,
      gridRow = 4, gridCol = 17, electronConfiguration = "[Ar] 3d¹⁰ 4s² 4p⁵", electronShells = listOf(2, 8, 18, 7),
      electronegativity = 2.96, density = 3.1028, meltingPointK = 265.8, boilingPointK = 332.0,
      atomicRadiusPm = 94, ionizationEnergyKjMol = 1139.9, oxidationStates = "-1, +1, +3, +5, +7",
      standardPhase = MatterPhase.LIQUID, discoveryYear = "1826", discoveredBy = "Antoine Jérôme Balard & Carl Jacob Löwig",
      summary = "One of only two elements that are liquid at room temperature (alongside mercury); a dense reddish-brown halogen used in flame retardants and chemical synthesis.",
    ),
    ChemicalElement(
      atomicNumber = 36, symbol = "Kr", name = "Krypton", atomicMass = 83.798,
      category = ElementCategory.NOBLE_GAS, group = 18, period = 4, block = ElementBlock.P,
      gridRow = 4, gridCol = 18, electronConfiguration = "[Ar] 3d¹⁰ 4s² 4p⁶", electronShells = listOf(2, 8, 18, 8),
      electronegativity = 3.00, density = 0.003749, meltingPointK = 115.78, boilingPointK = 119.93,
      atomicRadiusPm = 88, ionizationEnergyKjMol = 1350.8, oxidationStates = "0, +2",
      standardPhase = MatterPhase.GAS, discoveryYear = "1898", discoveredBy = "William Ramsay & Morris Travers",
      summary = "A noble gas used in high-intensity airport runway strobes, excimer photolithography lasers, and energy-saving insulated glazing.",
    ),

    // ==================== PERIOD 5 ====================
    ChemicalElement(
      atomicNumber = 37, symbol = "Rb", name = "Rubidium", atomicMass = 85.468,
      category = ElementCategory.ALKALI_METAL, group = 1, period = 5, block = ElementBlock.S,
      gridRow = 5, gridCol = 1, electronConfiguration = "[Kr] 5s¹", electronShells = listOf(2, 8, 18, 8, 1),
      electronegativity = 0.82, density = 1.532, meltingPointK = 312.45, boilingPointK = 961.0,
      atomicRadiusPm = 265, ionizationEnergyKjMol = 403.0, oxidationStates = "+1",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1861", discoveredBy = "Robert Bunsen & Gustav Kirchhoff",
      summary = "A very soft, highly reactive alkali metal used in vapor-cell atomic clocks, Bose-Einstein condensate physics experiments, and purple fireworks.",
    ),
    ChemicalElement(
      atomicNumber = 38, symbol = "Sr", name = "Strontium", atomicMass = 87.62,
      category = ElementCategory.ALKALINE_EARTH_METAL, group = 2, period = 5, block = ElementBlock.S,
      gridRow = 5, gridCol = 2, electronConfiguration = "[Kr] 5s²", electronShells = listOf(2, 8, 18, 8, 2),
      electronegativity = 0.95, density = 2.64, meltingPointK = 1050.0, boilingPointK = 1650.0,
      atomicRadiusPm = 219, ionizationEnergyKjMol = 549.5, oxidationStates = "+2",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1790", discoveredBy = "Adair Crawford",
      summary = "An alkaline earth metal whose salts produce brilliant crimson-red flames in pyrotechnics and emergency flares, and power ultra-precise optical lattice atomic clocks.",
    ),
    ChemicalElement(
      atomicNumber = 39, symbol = "Y", name = "Yttrium", atomicMass = 88.906,
      category = ElementCategory.TRANSITION_METAL, group = 3, period = 5, block = ElementBlock.D,
      gridRow = 5, gridCol = 3, electronConfiguration = "[Kr] 4d¹ 5s²", electronShells = listOf(2, 8, 18, 9, 2),
      electronegativity = 1.22, density = 4.472, meltingPointK = 1799.0, boilingPointK = 3203.0,
      atomicRadiusPm = 212, ionizationEnergyKjMol = 600.0, oxidationStates = "+3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1794", discoveredBy = "Johan Gadolin",
      summary = "A rare-earth transition metal used in Nd:YAG solid-state lasers, YBCO high-temperature superconductors, and yttria-stabilized zirconia (YSZ) thermal coatings.",
    ),
    ChemicalElement(
      atomicNumber = 40, symbol = "Zr", name = "Zirconium", atomicMass = 91.224,
      category = ElementCategory.TRANSITION_METAL, group = 4, period = 5, block = ElementBlock.D,
      gridRow = 5, gridCol = 4, electronConfiguration = "[Kr] 4d² 5s²", electronShells = listOf(2, 8, 18, 10, 2),
      electronegativity = 1.33, density = 6.52, meltingPointK = 2128.0, boilingPointK = 4650.0,
      atomicRadiusPm = 206, ionizationEnergyKjMol = 640.1, oxidationStates = "+4",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1789", discoveredBy = "Martin Heinrich Klaproth",
      summary = "A corrosion-resistant metal with very low thermal-neutron absorption cross-section, making it the standard cladding material for nuclear reactor fuel rods.",
    ),
    ChemicalElement(
      atomicNumber = 41, symbol = "Nb", name = "Niobium", atomicMass = 92.906,
      category = ElementCategory.TRANSITION_METAL, group = 5, period = 5, block = ElementBlock.D,
      gridRow = 5, gridCol = 5, electronConfiguration = "[Kr] 4d⁴ 5s¹", electronShells = listOf(2, 8, 18, 12, 1),
      electronegativity = 1.60, density = 8.57, meltingPointK = 2750.0, boilingPointK = 5017.0,
      atomicRadiusPm = 198, ionizationEnergyKjMol = 652.1, oxidationStates = "+3, +5",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1801", discoveredBy = "Charles Hatchett",
      summary = "A ductile refractory metal used in high-strength pipeline microalloyed steels, rocket thruster nozzles, and NbTi/Nb₃Sn superconducting magnets for particle accelerators.",
    ),
    ChemicalElement(
      atomicNumber = 42, symbol = "Mo", name = "Molybdenum", atomicMass = 95.95,
      category = ElementCategory.TRANSITION_METAL, group = 6, period = 5, block = ElementBlock.D,
      gridRow = 5, gridCol = 6, electronConfiguration = "[Kr] 4d⁵ 5s¹", electronShells = listOf(2, 8, 18, 13, 1),
      electronegativity = 2.16, density = 10.28, meltingPointK = 2896.0, boilingPointK = 4912.0,
      atomicRadiusPm = 190, ionizationEnergyKjMol = 684.3, oxidationStates = "+2, +3, +4, +5, +6",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1778", discoveredBy = "Carl Wilhelm Scheele",
      summary = "A high-melting refractory metal used to strengthen chromoly steels, formulate MoS₂ dry lubricants, and catalyze biological nitrogen fixation in nitrogenase enzymes.",
    ),
    ChemicalElement(
      atomicNumber = 43, symbol = "Tc", name = "Technetium", atomicMass = 98.0,
      category = ElementCategory.TRANSITION_METAL, group = 7, period = 5, block = ElementBlock.D,
      gridRow = 5, gridCol = 7, electronConfiguration = "[Kr] 4d⁵ 5s²", electronShells = listOf(2, 8, 18, 13, 2),
      electronegativity = 1.90, density = 11.0, meltingPointK = 2430.0, boilingPointK = 4538.0,
      atomicRadiusPm = 183, ionizationEnergyKjMol = 702.0, oxidationStates = "+4, +7",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1937", discoveredBy = "Carlo Perrier & Emilio Segrè",
      summary = "The lightest element with no stable isotopes and the first artificially produced element; metastable ⁹⁹ᵐTc is the world's most widely used medical radioisotope tracer.",
    ),
    ChemicalElement(
      atomicNumber = 44, symbol = "Ru", name = "Ruthenium", atomicMass = 101.07,
      category = ElementCategory.TRANSITION_METAL, group = 8, period = 5, block = ElementBlock.D,
      gridRow = 5, gridCol = 8, electronConfiguration = "[Kr] 4d⁷ 5s¹", electronShells = listOf(2, 8, 18, 15, 1),
      electronegativity = 2.20, density = 12.45, meltingPointK = 2607.0, boilingPointK = 4423.0,
      atomicRadiusPm = 178, ionizationEnergyKjMol = 710.2, oxidationStates = "+2, +3, +4, +8",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1844", discoveredBy = "Karl Ernst Claus",
      summary = "A hard platinum-group metal used in wear-resistant electrical contacts, thick-film chip resistors, and Grubbs olefin metathesis catalysts.",
    ),
    ChemicalElement(
      atomicNumber = 45, symbol = "Rh", name = "Rhodium", atomicMass = 102.91,
      category = ElementCategory.TRANSITION_METAL, group = 9, period = 5, block = ElementBlock.D,
      gridRow = 5, gridCol = 9, electronConfiguration = "[Kr] 4d⁸ 5s¹", electronShells = listOf(2, 8, 18, 16, 1),
      electronegativity = 2.28, density = 12.41, meltingPointK = 2237.0, boilingPointK = 3968.0,
      atomicRadiusPm = 173, ionizationEnergyKjMol = 719.7, oxidationStates = "+3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1803", discoveredBy = "William Hyde Wollaston",
      summary = "An exceedingly rare, highly reflective platinum-group metal critical for three-way automotive catalytic converters that reduce NOₓ emissions and fine jewelry plating.",
    ),
    ChemicalElement(
      atomicNumber = 46, symbol = "Pd", name = "Palladium", atomicMass = 106.42,
      category = ElementCategory.TRANSITION_METAL, group = 10, period = 5, block = ElementBlock.D,
      gridRow = 5, gridCol = 10, electronConfiguration = "[Kr] 4d¹⁰", electronShells = listOf(2, 8, 18, 18),
      electronegativity = 2.20, density = 12.023, meltingPointK = 1828.05, boilingPointK = 3236.0,
      atomicRadiusPm = 169, ionizationEnergyKjMol = 804.4, oxidationStates = "+2, +4",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1803", discoveredBy = "William Hyde Wollaston",
      summary = "A platinum-group metal capable of absorbing up to 900 times its own volume of hydrogen gas and prized for catalytic converters and cross-coupling organic synthesis.",
    ),
    ChemicalElement(
      atomicNumber = 47, symbol = "Ag", name = "Silver", atomicMass = 107.87,
      category = ElementCategory.TRANSITION_METAL, group = 11, period = 5, block = ElementBlock.D,
      gridRow = 5, gridCol = 11, electronConfiguration = "[Kr] 4d¹⁰ 5s¹", electronShells = listOf(2, 8, 18, 18, 1),
      electronegativity = 1.93, density = 10.49, meltingPointK = 1234.93, boilingPointK = 2435.0,
      atomicRadiusPm = 165, ionizationEnergyKjMol = 731.0, oxidationStates = "+1",
      standardPhase = MatterPhase.SOLID, discoveryYear = "Ancient", discoveredBy = "Known since antiquity",
      summary = "Possesses the highest electrical conductivity, thermal conductivity, and optical reflectivity of any metal; used in solar cell paste, electronics, mirrors, and bullion.",
    ),
    ChemicalElement(
      atomicNumber = 48, symbol = "Cd", name = "Cadmium", atomicMass = 112.41,
      category = ElementCategory.TRANSITION_METAL, group = 12, period = 5, block = ElementBlock.D,
      gridRow = 5, gridCol = 12, electronConfiguration = "[Kr] 4d¹⁰ 5s²", electronShells = listOf(2, 8, 18, 18, 2),
      electronegativity = 1.69, density = 8.65, meltingPointK = 594.22, boilingPointK = 1040.0,
      atomicRadiusPm = 161, ionizationEnergyKjMol = 867.8, oxidationStates = "+2",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1817", discoveredBy = "Karl Samuel Leberecht Hermann & Friedrich Stromeyer",
      summary = "A soft bluish-white metal used in cadmium telluride (CdTe) thin-film solar panels, nuclear reactor control rods, and QLED quantum dot displays.",
    ),
    ChemicalElement(
      atomicNumber = 49, symbol = "In", name = "Indium", atomicMass = 114.82,
      category = ElementCategory.POST_TRANSITION_METAL, group = 13, period = 5, block = ElementBlock.P,
      gridRow = 5, gridCol = 13, electronConfiguration = "[Kr] 4d¹⁰ 5s² 5p¹", electronShells = listOf(2, 8, 18, 18, 3),
      electronegativity = 1.78, density = 7.31, meltingPointK = 429.7485, boilingPointK = 2345.0,
      atomicRadiusPm = 156, ionizationEnergyKjMol = 558.3, oxidationStates = "+3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1863", discoveredBy = "Ferdinand Reich & Hieronymous Theodor Richter",
      summary = "A very soft, malleable metal essential for indium tin oxide (ITO) transparent conductive touchscreen coatings and low-melting solders.",
    ),
    ChemicalElement(
      atomicNumber = 50, symbol = "Sn", name = "Tin", atomicMass = 118.71,
      category = ElementCategory.POST_TRANSITION_METAL, group = 14, period = 5, block = ElementBlock.P,
      gridRow = 5, gridCol = 14, electronConfiguration = "[Kr] 4d¹⁰ 5s² 5p²", electronShells = listOf(2, 8, 18, 18, 4),
      electronegativity = 1.96, density = 7.265, meltingPointK = 505.08, boilingPointK = 2875.0,
      atomicRadiusPm = 145, ionizationEnergyKjMol = 708.6, oxidationStates = "+2, +4",
      standardPhase = MatterPhase.SOLID, discoveryYear = "Ancient", discoveredBy = "Known since antiquity",
      summary = "A corrosion-resistant metal that enabled the Bronze Age when alloyed with copper and remains the primary component of lead-free electronic solder and float-glass manufacturing.",
    ),
    ChemicalElement(
      atomicNumber = 51, symbol = "Sb", name = "Antimony", atomicMass = 121.76,
      category = ElementCategory.METALLOID, group = 15, period = 5, block = ElementBlock.P,
      gridRow = 5, gridCol = 15, electronConfiguration = "[Kr] 4d¹⁰ 5s² 5p³", electronShells = listOf(2, 8, 18, 18, 5),
      electronegativity = 2.05, density = 6.697, meltingPointK = 903.78, boilingPointK = 1860.0,
      atomicRadiusPm = 133, ionizationEnergyKjMol = 834.0, oxidationStates = "-3, +3, +5",
      standardPhase = MatterPhase.SOLID, discoveryYear = "Ancient", discoveredBy = "Known since antiquity",
      summary = "A lustrous gray metalloid used in flame-retardant formulations (Sb₂O₃), lead-acid battery hardening alloys, and infrared semiconductor detectors.",
    ),
    ChemicalElement(
      atomicNumber = 52, symbol = "Te", name = "Tellurium", atomicMass = 127.60,
      category = ElementCategory.METALLOID, group = 16, period = 5, block = ElementBlock.P,
      gridRow = 5, gridCol = 16, electronConfiguration = "[Kr] 4d¹⁰ 5s² 5p⁴", electronShells = listOf(2, 8, 18, 18, 6),
      electronegativity = 2.10, density = 6.24, meltingPointK = 722.66, boilingPointK = 1261.0,
      atomicRadiusPm = 123, ionizationEnergyKjMol = 869.3, oxidationStates = "-2, +2, +4, +6",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1782", discoveredBy = "Franz-Joseph Müller von Reichenstein",
      summary = "A rare, brittle semiconductor metalloid used in high-efficiency CdTe solar panels, bismuth telluride thermoelectric Peltier coolers, and rewritable optical discs.",
    ),
    ChemicalElement(
      atomicNumber = 53, symbol = "I", name = "Iodine", atomicMass = 126.90,
      category = ElementCategory.HALOGEN, group = 17, period = 5, block = ElementBlock.P,
      gridRow = 5, gridCol = 17, electronConfiguration = "[Kr] 4d¹⁰ 5s² 5p⁵", electronShells = listOf(2, 8, 18, 18, 7),
      electronegativity = 2.66, density = 4.933, meltingPointK = 386.85, boilingPointK = 457.4,
      atomicRadiusPm = 115, ionizationEnergyKjMol = 1008.4, oxidationStates = "-1, +1, +3, +5, +7",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1811", discoveredBy = "Bernard Courtois",
      summary = "A lustrous purple-black halogen solid that sublimes into a violet vapor; essential for human thyroid hormone synthesis, medical disinfectants, and X-ray contrast media.",
    ),
    ChemicalElement(
      atomicNumber = 54, symbol = "Xe", name = "Xenon", atomicMass = 131.29,
      category = ElementCategory.NOBLE_GAS, group = 18, period = 5, block = ElementBlock.P,
      gridRow = 5, gridCol = 18, electronConfiguration = "[Kr] 4d¹⁰ 5s² 5p⁶", electronShells = listOf(2, 8, 18, 18, 8),
      electronegativity = 2.60, density = 0.005894, meltingPointK = 161.4, boilingPointK = 165.051,
      atomicRadiusPm = 108, ionizationEnergyKjMol = 1170.4, oxidationStates = "0, +2, +4, +6, +8",
      standardPhase = MatterPhase.GAS, discoveryYear = "1898", discoveredBy = "William Ramsay & Morris Travers",
      summary = "A heavy, dense noble gas used in spacecraft Hall-effect ion thrusters, high-intensity cinema projection lamps, camera flash tubes, and general anesthesia.",
    ),

    // ==================== PERIOD 6 (55-56, 57-71 LANTHANIDES, 72-86) ====================
    ChemicalElement(
      atomicNumber = 55, symbol = "Cs", name = "Cesium", atomicMass = 132.91,
      category = ElementCategory.ALKALI_METAL, group = 1, period = 6, block = ElementBlock.S,
      gridRow = 6, gridCol = 1, electronConfiguration = "[Xe] 6s¹", electronShells = listOf(2, 8, 18, 18, 8, 1),
      electronegativity = 0.79, density = 1.93, meltingPointK = 301.7, boilingPointK = 944.0,
      atomicRadiusPm = 298, ionizationEnergyKjMol = 375.7, oxidationStates = "+1",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1860", discoveredBy = "Robert Bunsen & Gustav Kirchhoff",
      summary = "A soft silvery-gold alkali metal that melts at 28.5 °C; the hyperfine transition of ¹³³Cs defines the SI second in atomic clocks.",
    ),
    ChemicalElement(
      atomicNumber = 56, symbol = "Ba", name = "Barium", atomicMass = 137.33,
      category = ElementCategory.ALKALINE_EARTH_METAL, group = 2, period = 6, block = ElementBlock.S,
      gridRow = 6, gridCol = 2, electronConfiguration = "[Xe] 6s²", electronShells = listOf(2, 8, 18, 18, 8, 2),
      electronegativity = 0.89, density = 3.51, meltingPointK = 1000.0, boilingPointK = 2170.0,
      atomicRadiusPm = 253, ionizationEnergyKjMol = 502.9, oxidationStates = "+2",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1808", discoveredBy = "Humphry Davy",
      summary = "A heavy alkaline earth metal used as insoluble barium sulfate radiocontrast media for gastrointestinal X-ray imaging, drilling fluids, and green fireworks.",
    ),

    // -------------------- LANTHANIDES (57..71 -> gridRow = 9, gridCol = 3..17) --------------------
    ChemicalElement(
      atomicNumber = 57, symbol = "La", name = "Lanthanum", atomicMass = 138.91,
      category = ElementCategory.LANTHANIDE, group = 3, period = 6, block = ElementBlock.F,
      gridRow = 9, gridCol = 3, electronConfiguration = "[Xe] 5d¹ 6s²", electronShells = listOf(2, 8, 18, 18, 9, 2),
      electronegativity = 1.10, density = 6.162, meltingPointK = 1193.0, boilingPointK = 3737.0,
      atomicRadiusPm = 195, ionizationEnergyKjMol = 538.1, oxidationStates = "+3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1839", discoveredBy = "Carl Gustaf Mosander",
      summary = "The eponymous first lanthanide, used in high-refractive-index camera and telescope lenses, NiMH hybrid vehicle battery anodes, and petroleum cracking catalysts.",
    ),
    ChemicalElement(
      atomicNumber = 58, symbol = "Ce", name = "Cerium", atomicMass = 140.12,
      category = ElementCategory.LANTHANIDE, group = null, period = 6, block = ElementBlock.F,
      gridRow = 9, gridCol = 4, electronConfiguration = "[Xe] 4f¹ 5d¹ 6s²", electronShells = listOf(2, 8, 18, 19, 9, 2),
      electronegativity = 1.12, density = 6.77, meltingPointK = 1068.0, boilingPointK = 3716.0,
      atomicRadiusPm = 185, ionizationEnergyKjMol = 534.4, oxidationStates = "+3, +4",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1803", discoveredBy = "Jöns Jacob Berzelius, Wilhelm Hisinger & Martin Klaproth",
      summary = "The most abundant of the rare-earth lanthanides, used in ferrocerium lighter flints, precision optical glass polishing compounds (CeO₂), and catalytic converters.",
    ),
    ChemicalElement(
      atomicNumber = 59, symbol = "Pr", name = "Praseodymium", atomicMass = 140.91,
      category = ElementCategory.LANTHANIDE, group = null, period = 6, block = ElementBlock.F,
      gridRow = 9, gridCol = 5, electronConfiguration = "[Xe] 4f³ 6s²", electronShells = listOf(2, 8, 18, 21, 8, 2),
      electronegativity = 1.13, density = 6.77, meltingPointK = 1208.0, boilingPointK = 3793.0,
      atomicRadiusPm = 247, ionizationEnergyKjMol = 527.0, oxidationStates = "+3, +4",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1885", discoveredBy = "Carl Auer von Welsbach",
      summary = "A soft silvery lanthanide alloyed with neodymium in high-strength permanent magnets and used in didymium glassblowers' protective goggles.",
    ),
    ChemicalElement(
      atomicNumber = 60, symbol = "Nd", name = "Neodymium", atomicMass = 144.24,
      category = ElementCategory.LANTHANIDE, group = null, period = 6, block = ElementBlock.F,
      gridRow = 9, gridCol = 6, electronConfiguration = "[Xe] 4f⁴ 6s²", electronShells = listOf(2, 8, 18, 22, 8, 2),
      electronegativity = 1.14, density = 7.01, meltingPointK = 1297.0, boilingPointK = 3347.0,
      atomicRadiusPm = 206, ionizationEnergyKjMol = 533.1, oxidationStates = "+3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1885", discoveredBy = "Carl Auer von Welsbach",
      summary = "Forms Nd₂Fe₁₄B, the strongest known permanent magnets, powering electric vehicle traction motors, wind turbine generators, headphones, and Nd:YAG lasers.",
    ),
    ChemicalElement(
      atomicNumber = 61, symbol = "Pm", name = "Promethium", atomicMass = 145.0,
      category = ElementCategory.LANTHANIDE, group = null, period = 6, block = ElementBlock.F,
      gridRow = 9, gridCol = 7, electronConfiguration = "[Xe] 4f⁵ 6s²", electronShells = listOf(2, 8, 18, 23, 8, 2),
      electronegativity = 1.13, density = 7.26, meltingPointK = 1315.0, boilingPointK = 3273.0,
      atomicRadiusPm = 205, ionizationEnergyKjMol = 540.0, oxidationStates = "+3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1945", discoveredBy = "Jacob A. Marinsky, Lawrence E. Glendenin & Charles D. Coryell",
      summary = "The only exclusively radioactive lanthanide, emitting pure beta radiation used in long-life nuclear microbatteries and luminous aerospace instrument dials.",
    ),
    ChemicalElement(
      atomicNumber = 62, symbol = "Sm", name = "Samarium", atomicMass = 150.36,
      category = ElementCategory.LANTHANIDE, group = null, period = 6, block = ElementBlock.F,
      gridRow = 9, gridCol = 8, electronConfiguration = "[Xe] 4f⁶ 6s²", electronShells = listOf(2, 8, 18, 24, 8, 2),
      electronegativity = 1.17, density = 7.52, meltingPointK = 1345.0, boilingPointK = 2067.0,
      atomicRadiusPm = 238, ionizationEnergyKjMol = 544.5, oxidationStates = "+2, +3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1879", discoveredBy = "Lecoq de Boisbaudran",
      summary = "A rare-earth metal used in samarium-cobalt (SmCo) permanent magnets that retain magnetism at high temperatures up to 700 °C.",
    ),
    ChemicalElement(
      atomicNumber = 63, symbol = "Eu", name = "Europium", atomicMass = 151.96,
      category = ElementCategory.LANTHANIDE, group = null, period = 6, block = ElementBlock.F,
      gridRow = 9, gridCol = 9, electronConfiguration = "[Xe] 4f⁷ 6s²", electronShells = listOf(2, 8, 18, 25, 8, 2),
      electronegativity = 1.20, density = 5.244, meltingPointK = 1099.0, boilingPointK = 1802.0,
      atomicRadiusPm = 231, ionizationEnergyKjMol = 547.1, oxidationStates = "+2, +3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1901", discoveredBy = "Eugène-Anatole Demarçay",
      summary = "The most reactive lanthanide, prized for intense red and blue photoluminescent phosphors in LED displays and anti-counterfeiting Euro banknote inks.",
    ),
    ChemicalElement(
      atomicNumber = 64, symbol = "Gd", name = "Gadolinium", atomicMass = 157.25,
      category = ElementCategory.LANTHANIDE, group = null, period = 6, block = ElementBlock.F,
      gridRow = 9, gridCol = 10, electronConfiguration = "[Xe] 4f⁷ 5d¹ 6s²", electronShells = listOf(2, 8, 18, 25, 9, 2),
      electronegativity = 1.20, density = 7.90, meltingPointK = 1585.0, boilingPointK = 3546.0,
      atomicRadiusPm = 233, ionizationEnergyKjMol = 593.4, oxidationStates = "+3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1880", discoveredBy = "Jean Charles Galissard de Marignac",
      summary = "Possesses seven unpaired 4f electrons giving it strong paramagnetism for intravenous MRI contrast agents and the highest thermal-neutron capture cross-section of any natural element.",
    ),
    ChemicalElement(
      atomicNumber = 65, symbol = "Tb", name = "Terbium", atomicMass = 158.93,
      category = ElementCategory.LANTHANIDE, group = null, period = 6, block = ElementBlock.F,
      gridRow = 9, gridCol = 11, electronConfiguration = "[Xe] 4f⁹ 6s²", electronShells = listOf(2, 8, 18, 27, 8, 2),
      electronegativity = 1.10, density = 8.23, meltingPointK = 1629.0, boilingPointK = 3503.0,
      atomicRadiusPm = 225, ionizationEnergyKjMol = 565.8, oxidationStates = "+3, +4",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1843", discoveredBy = "Carl Gustaf Mosander",
      summary = "A silvery lanthanide used in green display phosphors, high-temperature neodymium magnet stabilizers, and Terfenol-D magnetostrictive sonar actuators.",
    ),
    ChemicalElement(
      atomicNumber = 66, symbol = "Dy", name = "Dysprosium", atomicMass = 162.50,
      category = ElementCategory.LANTHANIDE, group = null, period = 6, block = ElementBlock.F,
      gridRow = 9, gridCol = 12, electronConfiguration = "[Xe] 4f¹⁰ 6s²", electronShells = listOf(2, 8, 18, 28, 8, 2),
      electronegativity = 1.22, density = 8.54, meltingPointK = 1680.0, boilingPointK = 2840.0,
      atomicRadiusPm = 228, ionizationEnergyKjMol = 573.0, oxidationStates = "+3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1886", discoveredBy = "Lecoq de Boisbaudran",
      summary = "Has one of the highest magnetic strengths of all elements and is added to EV neodymium magnets to prevent demagnetization at high operating temperatures.",
    ),
    ChemicalElement(
      atomicNumber = 67, symbol = "Ho", name = "Holmium", atomicMass = 164.93,
      category = ElementCategory.LANTHANIDE, group = null, period = 6, block = ElementBlock.F,
      gridRow = 9, gridCol = 13, electronConfiguration = "[Xe] 4f¹¹ 6s²", electronShells = listOf(2, 8, 18, 29, 8, 2),
      electronegativity = 1.23, density = 8.79, meltingPointK = 1734.0, boilingPointK = 2993.0,
      atomicRadiusPm = 226, ionizationEnergyKjMol = 581.0, oxidationStates = "+3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1878", discoveredBy = "Marc Delafontaine, Jacques-Louis Soret & Per Teodor Cleve",
      summary = "Possesses the highest magnetic moment (10.6 μB) of any naturally occurring element, used as magnetic flux concentrators and in medical Ho:YAG surgical lasers.",
    ),
    ChemicalElement(
      atomicNumber = 68, symbol = "Er", name = "Erbium", atomicMass = 167.26,
      category = ElementCategory.LANTHANIDE, group = null, period = 6, block = ElementBlock.F,
      gridRow = 9, gridCol = 14, electronConfiguration = "[Xe] 4f¹² 6s²", electronShells = listOf(2, 8, 18, 30, 8, 2),
      electronegativity = 1.24, density = 9.066, meltingPointK = 1802.0, boilingPointK = 3141.0,
      atomicRadiusPm = 226, ionizationEnergyKjMol = 589.3, oxidationStates = "+3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1843", discoveredBy = "Carl Gustaf Mosander",
      summary = "Doped into undersea fiber-optic cables (erbium-doped fiber amplifiers, EDFAs) to optically amplify 1550 nm internet telecommunications signals without electronic conversion.",
    ),
    ChemicalElement(
      atomicNumber = 69, symbol = "Tm", name = "Thulium", atomicMass = 168.93,
      category = ElementCategory.LANTHANIDE, group = null, period = 6, block = ElementBlock.F,
      gridRow = 9, gridCol = 15, electronConfiguration = "[Xe] 4f¹³ 6s²", electronShells = listOf(2, 8, 18, 31, 8, 2),
      electronegativity = 1.25, density = 9.32, meltingPointK = 1818.0, boilingPointK = 2223.0,
      atomicRadiusPm = 222, ionizationEnergyKjMol = 596.7, oxidationStates = "+2, +3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1879", discoveredBy = "Per Teodor Cleve",
      summary = "The least abundant naturally occurring stable lanthanide, used in portable medical X-ray sources (¹⁷⁰Tm) and high-efficiency surgical fiber lasers.",
    ),
    ChemicalElement(
      atomicNumber = 70, symbol = "Yb", name = "Ytterbium", atomicMass = 173.05,
      category = ElementCategory.LANTHANIDE, group = null, period = 6, block = ElementBlock.F,
      gridRow = 9, gridCol = 16, electronConfiguration = "[Xe] 4f¹⁴ 6s²", electronShells = listOf(2, 8, 18, 32, 8, 2),
      electronegativity = 1.10, density = 6.90, meltingPointK = 1097.0, boilingPointK = 1469.0,
      atomicRadiusPm = 222, ionizationEnergyKjMol = 603.4, oxidationStates = "+2, +3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1878", discoveredBy = "Jean Charles Galissard de Marignac",
      summary = "A closed-4f-shell lanthanide used in high-power ytterbium fiber lasers for industrial cutting and in the world's most stable optical lattice atomic clocks.",
    ),
    ChemicalElement(
      atomicNumber = 71, symbol = "Lu", name = "Lutetium", atomicMass = 174.97,
      category = ElementCategory.LANTHANIDE, group = null, period = 6, block = ElementBlock.D,
      gridRow = 9, gridCol = 17, electronConfiguration = "[Xe] 4f¹⁴ 5d¹ 6s²", electronShells = listOf(2, 8, 18, 32, 9, 2),
      electronegativity = 1.27, density = 9.841, meltingPointK = 1925.0, boilingPointK = 3675.0,
      atomicRadiusPm = 217, ionizationEnergyKjMol = 523.5, oxidationStates = "+3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1907", discoveredBy = "Georges Urbain, Carl Auer von Welsbach & Charles James",
      summary = "The heaviest, hardest, and densest of the lanthanide series, used in lutetium oxyorthosilicate (LSO) scintillator crystals for medical PET scanners and targeted cancer radiotherapy (¹⁷⁷Lu).",
    ),

    // -------------------- PERIOD 6 CONTINUED (72..86) --------------------
    ChemicalElement(
      atomicNumber = 72, symbol = "Hf", name = "Hafnium", atomicMass = 178.49,
      category = ElementCategory.TRANSITION_METAL, group = 4, period = 6, block = ElementBlock.D,
      gridRow = 6, gridCol = 4, electronConfiguration = "[Xe] 4f¹⁴ 5d² 6s²", electronShells = listOf(2, 8, 18, 32, 10, 2),
      electronegativity = 1.30, density = 13.31, meltingPointK = 2506.0, boilingPointK = 4876.0,
      atomicRadiusPm = 208, ionizationEnergyKjMol = 658.5, oxidationStates = "+4",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1923", discoveredBy = "Dirk Coster & George de Hevesy",
      summary = "Chemically almost identical to zirconium due to the lanthanide contraction, but absorbs neutrons 600× more strongly; HfO₂ is the high-κ gate dielectric in modern microprocessors.",
    ),
    ChemicalElement(
      atomicNumber = 73, symbol = "Ta", name = "Tantalum", atomicMass = 180.95,
      category = ElementCategory.TRANSITION_METAL, group = 5, period = 6, block = ElementBlock.D,
      gridRow = 6, gridCol = 5, electronConfiguration = "[Xe] 4f¹⁴ 5d³ 6s²", electronShells = listOf(2, 8, 18, 32, 11, 2),
      electronegativity = 1.50, density = 16.69, meltingPointK = 3290.0, boilingPointK = 5731.0,
      atomicRadiusPm = 200, ionizationEnergyKjMol = 761.0, oxidationStates = "+5",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1802", discoveredBy = "Anders Gustaf Ekeberg",
      summary = "An extraordinarily corrosion-resistant refractory metal immune to nearly all acids and completely biocompatible, used in compact high-capacitance smartphone capacitors and surgical implants.",
    ),
    ChemicalElement(
      atomicNumber = 74, symbol = "W", name = "Tungsten", atomicMass = 183.84,
      category = ElementCategory.TRANSITION_METAL, group = 6, period = 6, block = ElementBlock.D,
      gridRow = 6, gridCol = 6, electronConfiguration = "[Xe] 4f¹⁴ 5d⁴ 6s²", electronShells = listOf(2, 8, 18, 32, 12, 2),
      electronegativity = 2.36, density = 19.25, meltingPointK = 3695.0, boilingPointK = 5828.0,
      atomicRadiusPm = 193, ionizationEnergyKjMol = 770.0, oxidationStates = "+2, +3, +4, +5, +6",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1783", discoveredBy = "Juan José & Fausto Elhuyar",
      summary = "Possesses the highest melting point (3422 °C) and highest tensile strength of all pure metals; used in TIG welding electrodes, X-ray targets, and ultra-hard tungsten carbide tooling.",
    ),
    ChemicalElement(
      atomicNumber = 75, symbol = "Re", name = "Rhenium", atomicMass = 186.21,
      category = ElementCategory.TRANSITION_METAL, group = 7, period = 6, block = ElementBlock.D,
      gridRow = 6, gridCol = 7, electronConfiguration = "[Xe] 4f¹⁴ 5d⁵ 6s²", electronShells = listOf(2, 8, 18, 32, 13, 2),
      electronegativity = 1.90, density = 21.02, meltingPointK = 3459.0, boilingPointK = 5869.0,
      atomicRadiusPm = 188, ionizationEnergyKjMol = 760.0, oxidationStates = "-1, +2, +4, +6, +7",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1925", discoveredBy = "Walter Noddack, Ida Tacke & Otto Berg",
      summary = "One of the rarest elements in Earth's crust with the highest boiling point of any element, essential for single-crystal nickel-superalloy jet engine turbine blades.",
    ),
    ChemicalElement(
      atomicNumber = 76, symbol = "Os", name = "Osmium", atomicMass = 190.23,
      category = ElementCategory.TRANSITION_METAL, group = 8, period = 6, block = ElementBlock.D,
      gridRow = 6, gridCol = 8, electronConfiguration = "[Xe] 4f¹⁴ 5d⁶ 6s²", electronShells = listOf(2, 8, 18, 32, 14, 2),
      electronegativity = 2.20, density = 22.59, meltingPointK = 3306.0, boilingPointK = 5285.0,
      atomicRadiusPm = 185, ionizationEnergyKjMol = 840.0, oxidationStates = "+2, +3, +4, +8",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1803", discoveredBy = "Smithson Tennant",
      summary = "The densest naturally occurring element on Earth (22.59 g/cm³), hard and bluish-white, alloyed with platinum and iridium for fountain pen nibs and electrical pivots.",
    ),
    ChemicalElement(
      atomicNumber = 77, symbol = "Ir", name = "Iridium", atomicMass = 192.22,
      category = ElementCategory.TRANSITION_METAL, group = 9, period = 6, block = ElementBlock.D,
      gridRow = 6, gridCol = 9, electronConfiguration = "[Xe] 4f¹⁴ 5d⁷ 6s²", electronShells = listOf(2, 8, 18, 32, 15, 2),
      electronegativity = 2.20, density = 22.56, meltingPointK = 2719.0, boilingPointK = 4701.0,
      atomicRadiusPm = 180, ionizationEnergyKjMol = 880.0, oxidationStates = "+1, +3, +4, +6",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1803", discoveredBy = "Smithson Tennant",
      summary = "The most corrosion-resistant metal known (even resisting aqua regia at high temperatures); an iridium-rich clay layer marks the 66-million-year-old K-Pg asteroid impact boundary.",
    ),
    ChemicalElement(
      atomicNumber = 78, symbol = "Pt", name = "Platinum", atomicMass = 195.08,
      category = ElementCategory.TRANSITION_METAL, group = 10, period = 6, block = ElementBlock.D,
      gridRow = 6, gridCol = 10, electronConfiguration = "[Xe] 4f¹⁴ 5d⁹ 6s¹", electronShells = listOf(2, 8, 18, 32, 17, 1),
      electronegativity = 2.28, density = 21.45, meltingPointK = 2041.4, boilingPointK = 4098.0,
      atomicRadiusPm = 177, ionizationEnergyKjMol = 870.0, oxidationStates = "+2, +4",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1735", discoveredBy = "Antonio de Ulloa",
      summary = "A dense, malleable noble metal prized for hydrogen fuel-cell electrodes, catalytic converters, laboratory crucibles, cisplatin chemotherapy drugs, and fine jewelry.",
    ),
    ChemicalElement(
      atomicNumber = 79, symbol = "Au", name = "Gold", atomicMass = 196.97,
      category = ElementCategory.TRANSITION_METAL, group = 11, period = 6, block = ElementBlock.D,
      gridRow = 6, gridCol = 11, electronConfiguration = "[Xe] 4f¹⁴ 5d¹⁰ 6s¹", electronShells = listOf(2, 8, 18, 32, 18, 1),
      electronegativity = 2.54, density = 19.30, meltingPointK = 1337.33, boilingPointK = 3129.0,
      atomicRadiusPm = 174, ionizationEnergyKjMol = 890.1, oxidationStates = "+1, +3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "Ancient", discoveredBy = "Known since antiquity",
      summary = "The most malleable and ductile of all metals, completely resistant to atmospheric tarnish; relativistic 5d/6s orbital effects give gold its characteristic warm yellow color.",
    ),
    ChemicalElement(
      atomicNumber = 80, symbol = "Hg", name = "Mercury", atomicMass = 200.59,
      category = ElementCategory.TRANSITION_METAL, group = 12, period = 6, block = ElementBlock.D,
      gridRow = 6, gridCol = 12, electronConfiguration = "[Xe] 4f¹⁴ 5d¹⁰ 6s²", electronShells = listOf(2, 8, 18, 32, 18, 2),
      electronegativity = 2.00, density = 13.534, meltingPointK = 234.321, boilingPointK = 629.88,
      atomicRadiusPm = 171, ionizationEnergyKjMol = 1007.1, oxidationStates = "+1, +2",
      standardPhase = MatterPhase.LIQUID, discoveryYear = "Ancient", discoveredBy = "Known since antiquity",
      summary = "The only metallic element that is liquid at standard room temperature (melting at −38.83 °C), historically known as quicksilver and used in barometers, fluorescent lamps, and amalgams.",
    ),
    ChemicalElement(
      atomicNumber = 81, symbol = "Tl", name = "Thallium", atomicMass = 204.38,
      category = ElementCategory.POST_TRANSITION_METAL, group = 13, period = 6, block = ElementBlock.P,
      gridRow = 6, gridCol = 13, electronConfiguration = "[Xe] 4f¹⁴ 5d¹⁰ 6s² 6p¹", electronShells = listOf(2, 8, 18, 32, 18, 3),
      electronegativity = 1.62, density = 11.85, meltingPointK = 577.0, boilingPointK = 1746.0,
      atomicRadiusPm = 156, ionizationEnergyKjMol = 589.4, oxidationStates = "+1, +3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1861", discoveredBy = "William Crookes",
      summary = "A soft gray post-transition metal named for its bright green spectral emission line, used in infrared optical crystals and sodium-iodide gamma-ray scintillators.",
    ),
    ChemicalElement(
      atomicNumber = 82, symbol = "Pb", name = "Lead", atomicMass = 207.2,
      category = ElementCategory.POST_TRANSITION_METAL, group = 14, period = 6, block = ElementBlock.P,
      gridRow = 6, gridCol = 14, electronConfiguration = "[Xe] 4f¹⁴ 5d¹⁰ 6s² 6p²", electronShells = listOf(2, 8, 18, 32, 18, 4),
      electronegativity = 2.33, density = 11.34, meltingPointK = 600.61, boilingPointK = 2022.0,
      atomicRadiusPm = 154, ionizationEnergyKjMol = 715.6, oxidationStates = "+2, +4",
      standardPhase = MatterPhase.SOLID, discoveryYear = "Ancient", discoveredBy = "Known since antiquity",
      summary = "The heaviest element with stable isotopes (magic proton number Z = 82), widely used in automotive lead-acid starter batteries and X-ray/gamma radiation shielding.",
    ),
    ChemicalElement(
      atomicNumber = 83, symbol = "Bi", name = "Bismuth", atomicMass = 208.98,
      category = ElementCategory.POST_TRANSITION_METAL, group = 15, period = 6, block = ElementBlock.P,
      gridRow = 6, gridCol = 15, electronConfiguration = "[Xe] 4f¹⁴ 5d¹⁰ 6s² 6p³", electronShells = listOf(2, 8, 18, 32, 18, 5),
      electronegativity = 2.02, density = 9.78, meltingPointK = 544.7, boilingPointK = 1837.0,
      atomicRadiusPm = 143, ionizationEnergyKjMol = 703.0, oxidationStates = "+3, +5",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1753", discoveredBy = "Claude François Geoffroy",
      summary = "Forms iridescent stair-stepped hopper crystals, expands upon freezing like water, exhibits strong diamagnetism, and is the active ingredient in bismuth subsalicylate.",
    ),
    ChemicalElement(
      atomicNumber = 84, symbol = "Po", name = "Polonium", atomicMass = 209.0,
      category = ElementCategory.POST_TRANSITION_METAL, group = 16, period = 6, block = ElementBlock.P,
      gridRow = 6, gridCol = 16, electronConfiguration = "[Xe] 4f¹⁴ 5d¹⁰ 6s² 6p⁴", electronShells = listOf(2, 8, 18, 32, 18, 6),
      electronegativity = 2.00, density = 9.196, meltingPointK = 527.0, boilingPointK = 1235.0,
      atomicRadiusPm = 135, ionizationEnergyKjMol = 812.1, oxidationStates = "-2, +2, +4, +6",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1898", discoveredBy = "Marie & Pierre Curie",
      summary = "A rare, intensely radioactive alpha-emitting chalcogen discovered in pitchblende ore and named after Marie Curie's native Poland; crystallizes in a simple cubic lattice.",
    ),
    ChemicalElement(
      atomicNumber = 85, symbol = "At", name = "Astatine", atomicMass = 210.0,
      category = ElementCategory.HALOGEN, group = 17, period = 6, block = ElementBlock.P,
      gridRow = 6, gridCol = 17, electronConfiguration = "[Xe] 4f¹⁴ 5d¹⁰ 6s² 6p⁵", electronShells = listOf(2, 8, 18, 32, 18, 7),
      electronegativity = 2.20, density = 7.0, meltingPointK = 575.0, boilingPointK = 610.0,
      atomicRadiusPm = 127, ionizationEnergyKjMol = 899.0, oxidationStates = "-1, +1, +3, +5, +7",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1940", discoveredBy = "Dale R. Corson, Kenneth Ross MacKenzie & Emilio Segrè",
      summary = "The rarest naturally occurring element in Earth's crust (less than 1 gram at any time), a short-lived radioactive halogen researched for targeted alpha-particle cancer therapy (²¹¹At).",
    ),
    ChemicalElement(
      atomicNumber = 86, symbol = "Rn", name = "Radon", atomicMass = 222.0,
      category = ElementCategory.NOBLE_GAS, group = 18, period = 6, block = ElementBlock.P,
      gridRow = 6, gridCol = 18, electronConfiguration = "[Xe] 4f¹⁴ 5d¹⁰ 6s² 6p⁶", electronShells = listOf(2, 8, 18, 32, 18, 8),
      electronegativity = 2.20, density = 0.00973, meltingPointK = 202.0, boilingPointK = 211.5,
      atomicRadiusPm = 120, ionizationEnergyKjMol = 1037.0, oxidationStates = "0, +2",
      standardPhase = MatterPhase.GAS, discoveryYear = "1899", discoveredBy = "Ernest Rutherford & Robert B. Owens",
      summary = "A dense, colorless radioactive noble gas formed by the natural decay of radium and uranium in granitic bedrock and soil.",
    ),

    // ==================== PERIOD 7 (87-88, 89-103 ACTINIDES, 104-118) ====================
    ChemicalElement(
      atomicNumber = 87, symbol = "Fr", name = "Francium", atomicMass = 223.0,
      category = ElementCategory.ALKALI_METAL, group = 1, period = 7, block = ElementBlock.S,
      gridRow = 7, gridCol = 1, electronConfiguration = "[Rn] 7s¹", electronShells = listOf(2, 8, 18, 32, 18, 8, 1),
      electronegativity = 0.70, density = 1.87, meltingPointK = 300.0, boilingPointK = 950.0,
      atomicRadiusPm = 348, ionizationEnergyKjMol = 380.0, oxidationStates = "+1",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1939", discoveredBy = "Marguerite Perey",
      summary = "An extremely unstable, short-lived radioactive alkali metal (²²³Fr half-life 22 minutes) with the largest atomic radius of any element.",
    ),
    ChemicalElement(
      atomicNumber = 88, symbol = "Ra", name = "Radium", atomicMass = 226.0,
      category = ElementCategory.ALKALINE_EARTH_METAL, group = 2, period = 7, block = ElementBlock.S,
      gridRow = 7, gridCol = 2, electronConfiguration = "[Rn] 7s²", electronShells = listOf(2, 8, 18, 32, 18, 8, 2),
      electronegativity = 0.90, density = 5.5, meltingPointK = 973.0, boilingPointK = 2010.0,
      atomicRadiusPm = 283, ionizationEnergyKjMol = 509.3, oxidationStates = "+2",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1898", discoveredBy = "Marie & Pierre Curie",
      summary = "A radioactive alkaline earth metal isolated by Marie and Pierre Curie from uraninite that glows faint blue via radioluminescence.",
    ),

    // -------------------- ACTINIDES (89..103 -> gridRow = 10, gridCol = 3..17) --------------------
    ChemicalElement(
      atomicNumber = 89, symbol = "Ac", name = "Actinium", atomicMass = 227.0,
      category = ElementCategory.ACTINIDE, group = 3, period = 7, block = ElementBlock.F,
      gridRow = 10, gridCol = 3, electronConfiguration = "[Rn] 6d¹ 7s²", electronShells = listOf(2, 8, 18, 32, 18, 9, 2),
      electronegativity = 1.10, density = 10.07, meltingPointK = 1323.0, boilingPointK = 3471.0,
      atomicRadiusPm = 195, ionizationEnergyKjMol = 499.0, oxidationStates = "+3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1899", discoveredBy = "André-Louis Debierne",
      summary = "The eponymous first actinide element, a silvery radioactive metal whose isotope ²²⁵Ac is a leading alpha-emitter in targeted cancer immunotherapy.",
    ),
    ChemicalElement(
      atomicNumber = 90, symbol = "Th", name = "Thorium", atomicMass = 232.04,
      category = ElementCategory.ACTINIDE, group = null, period = 7, block = ElementBlock.F,
      gridRow = 10, gridCol = 4, electronConfiguration = "[Rn] 6d² 7s²", electronShells = listOf(2, 8, 18, 32, 18, 10, 2),
      electronegativity = 1.30, density = 11.72, meltingPointK = 2023.0, boilingPointK = 5061.0,
      atomicRadiusPm = 180, ionizationEnergyKjMol = 587.0, oxidationStates = "+4",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1829", discoveredBy = "Jöns Jacob Berzelius",
      summary = "A weakly radioactive fertile actinide (²³²Th half-life 14 billion years) three times more abundant than uranium and researched as a molten-salt nuclear reactor fuel.",
    ),
    ChemicalElement(
      atomicNumber = 91, symbol = "Pa", name = "Protactinium", atomicMass = 231.04,
      category = ElementCategory.ACTINIDE, group = null, period = 7, block = ElementBlock.F,
      gridRow = 10, gridCol = 5, electronConfiguration = "[Rn] 5f² 6d¹ 7s²", electronShells = listOf(2, 8, 18, 32, 20, 9, 2),
      electronegativity = 1.50, density = 15.37, meltingPointK = 1841.0, boilingPointK = 4300.0,
      atomicRadiusPm = 180, ionizationEnergyKjMol = 568.0, oxidationStates = "+4, +5",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1913", discoveredBy = "Kasimir Fajans & Oswald Helmuth Göhring",
      summary = "A dense, silvery-gray radioactive actinide occurring in trace quantities in uranium ores as the decay precursor to actinium.",
    ),
    ChemicalElement(
      atomicNumber = 92, symbol = "U", name = "Uranium", atomicMass = 238.03,
      category = ElementCategory.ACTINIDE, group = null, period = 7, block = ElementBlock.F,
      gridRow = 10, gridCol = 6, electronConfiguration = "[Rn] 5f³ 6d¹ 7s²", electronShells = listOf(2, 8, 18, 32, 21, 9, 2),
      electronegativity = 1.38, density = 19.1, meltingPointK = 1405.3, boilingPointK = 4404.0,
      atomicRadiusPm = 175, ionizationEnergyKjMol = 597.6, oxidationStates = "+3, +4, +5, +6",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1789", discoveredBy = "Martin Heinrich Klaproth",
      summary = "A heavy, weakly radioactive actinide metal whose fissile isotope ²³⁵U sustains chain reactions in commercial nuclear power reactors across the globe.",
    ),
    ChemicalElement(
      atomicNumber = 93, symbol = "Np", name = "Neptunium", atomicMass = 237.0,
      category = ElementCategory.ACTINIDE, group = null, period = 7, block = ElementBlock.F,
      gridRow = 10, gridCol = 7, electronConfiguration = "[Rn] 5f⁴ 6d¹ 7s²", electronShells = listOf(2, 8, 18, 32, 22, 9, 2),
      electronegativity = 1.36, density = 20.45, meltingPointK = 917.0, boilingPointK = 4273.0,
      atomicRadiusPm = 175, ionizationEnergyKjMol = 604.5, oxidationStates = "+3, +4, +5, +6, +7",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1940", discoveredBy = "Edwin McMillan & Philip H. Abelson",
      summary = "The first synthetic transuranium element, named after the planet Neptune and used as a precursor to produce ²³⁸Pu for deep-space spacecraft power generators.",
    ),
    ChemicalElement(
      atomicNumber = 94, symbol = "Pu", name = "Plutonium", atomicMass = 244.0,
      category = ElementCategory.ACTINIDE, group = null, period = 7, block = ElementBlock.F,
      gridRow = 10, gridCol = 8, electronConfiguration = "[Rn] 5f⁶ 7s²", electronShells = listOf(2, 8, 18, 32, 24, 8, 2),
      electronegativity = 1.28, density = 19.85, meltingPointK = 912.5, boilingPointK = 3501.0,
      atomicRadiusPm = 175, ionizationEnergyKjMol = 584.7, oxidationStates = "+3, +4, +5, +6, +7",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1940", discoveredBy = "Glenn T. Seaborg, Arthur Wahl, Joseph W. Kennedy & Edwin McMillan",
      summary = "A complex actinide with six solid allotropes; ²³⁹Pu is a fissile nuclear fuel while ²³⁸Pu powers radioisotope thermoelectric generators (RTGs) on Voyager, Cassini, and Mars rovers.",
    ),
    ChemicalElement(
      atomicNumber = 95, symbol = "Am", name = "Americium", atomicMass = 243.0,
      category = ElementCategory.ACTINIDE, group = null, period = 7, block = ElementBlock.F,
      gridRow = 10, gridCol = 9, electronConfiguration = "[Rn] 5f⁷ 7s²", electronShells = listOf(2, 8, 18, 32, 25, 8, 2),
      electronegativity = 1.13, density = 12.0, meltingPointK = 1449.0, boilingPointK = 2880.0,
      atomicRadiusPm = 175, ionizationEnergyKjMol = 578.0, oxidationStates = "+2, +3, +4, +5, +6",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1944", discoveredBy = "Glenn T. Seaborg, Ralph A. James, Leon O. Morgan & Albert Ghiorso",
      summary = "A synthetic transuranic actinide found in everyday household products: a tiny microgram source of ²⁴¹Am ionizes air inside ionization smoke detectors.",
    ),
    ChemicalElement(
      atomicNumber = 96, symbol = "Cm", name = "Curium", atomicMass = 247.0,
      category = ElementCategory.ACTINIDE, group = null, period = 7, block = ElementBlock.F,
      gridRow = 10, gridCol = 10, electronConfiguration = "[Rn] 5f⁷ 6d¹ 7s²", electronShells = listOf(2, 8, 18, 32, 25, 9, 2),
      electronegativity = 1.28, density = 13.51, meltingPointK = 1613.0, boilingPointK = 3383.0,
      atomicRadiusPm = 176, ionizationEnergyKjMol = 581.0, oxidationStates = "+3, +4",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1944", discoveredBy = "Glenn T. Seaborg, Ralph A. James & Albert Ghiorso",
      summary = "Named in honor of Marie and Pierre Curie, curium-244 is used as the alpha-particle emitter in Alpha Particle X-ray Spectrometers (APXS) aboard Mars rovers.",
    ),
    ChemicalElement(
      atomicNumber = 97, symbol = "Bk", name = "Berkelium", atomicMass = 247.0,
      category = ElementCategory.ACTINIDE, group = null, period = 7, block = ElementBlock.F,
      gridRow = 10, gridCol = 11, electronConfiguration = "[Rn] 5f⁹ 7s²", electronShells = listOf(2, 8, 18, 32, 27, 8, 2),
      electronegativity = 1.30, density = 14.78, meltingPointK = 1259.0, boilingPointK = 2900.0,
      atomicRadiusPm = 170, ionizationEnergyKjMol = 601.0, oxidationStates = "+3, +4",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1949", discoveredBy = "Lawrence Berkeley National Laboratory",
      summary = "A synthetic radioactive actinide named after Berkeley, California, used as an essential target material to synthesize superheavy elements such as tennessine (Z = 117).",
    ),
    ChemicalElement(
      atomicNumber = 98, symbol = "Cf", name = "Californium", atomicMass = 251.0,
      category = ElementCategory.ACTINIDE, group = null, period = 7, block = ElementBlock.F,
      gridRow = 10, gridCol = 12, electronConfiguration = "[Rn] 5f¹⁰ 7s²", electronShells = listOf(2, 8, 18, 32, 28, 8, 2),
      electronegativity = 1.30, density = 15.1, meltingPointK = 1173.0, boilingPointK = 1743.0,
      atomicRadiusPm = null, ionizationEnergyKjMol = 608.0, oxidationStates = "+2, +3, +4",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1950", discoveredBy = "Lawrence Berkeley National Laboratory",
      summary = "A powerful neutron emitter (one microgram of ²⁵²Cf emits 2.3 million neutrons per second), used to start up nuclear reactors and perform neutron moisture and weld radiography.",
    ),
    ChemicalElement(
      atomicNumber = 99, symbol = "Es", name = "Einsteinium", atomicMass = 252.0,
      category = ElementCategory.ACTINIDE, group = null, period = 7, block = ElementBlock.F,
      gridRow = 10, gridCol = 13, electronConfiguration = "[Rn] 5f¹¹ 7s²", electronShells = listOf(2, 8, 18, 32, 29, 8, 2),
      electronegativity = 1.30, density = 8.84, meltingPointK = 1133.0, boilingPointK = 1269.0,
      atomicRadiusPm = null, ionizationEnergyKjMol = 619.0, oxidationStates = "+2, +3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1952", discoveredBy = "Albert Ghiorso & team (Ivy Mike fallout)",
      summary = "Discovered in the debris of the first thermonuclear test in 1952 and named after Albert Einstein; the heaviest element ever prepared in macroscopic visible quantities.",
    ),
    ChemicalElement(
      atomicNumber = 100, symbol = "Fm", name = "Fermium", atomicMass = 257.0,
      category = ElementCategory.ACTINIDE, group = null, period = 7, block = ElementBlock.F,
      gridRow = 10, gridCol = 14, electronConfiguration = "[Rn] 5f¹² 7s²", electronShells = listOf(2, 8, 18, 32, 30, 8, 2),
      electronegativity = 1.30, density = 9.7, meltingPointK = 1800.0, boilingPointK = null,
      atomicRadiusPm = null, ionizationEnergyKjMol = 627.0, oxidationStates = "+2, +3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1952", discoveredBy = "Albert Ghiorso & team",
      summary = "Named after nuclear physicist Enrico Fermi, fermium is the heaviest element that can be synthesized by neutron bombardment of lighter elements.",
    ),
    ChemicalElement(
      atomicNumber = 101, symbol = "Md", name = "Mendelevium", atomicMass = 258.0,
      category = ElementCategory.ACTINIDE, group = null, period = 7, block = ElementBlock.F,
      gridRow = 10, gridCol = 15, electronConfiguration = "[Rn] 5f¹³ 7s²", electronShells = listOf(2, 8, 18, 32, 31, 8, 2),
      electronegativity = 1.30, density = 10.3, meltingPointK = 1100.0, boilingPointK = null,
      atomicRadiusPm = null, ionizationEnergyKjMol = 635.0, oxidationStates = "+2, +3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1955", discoveredBy = "Albert Ghiorso, Bernard Harvey, Gregory Choppin, Stanley Thompson & Glenn Seaborg",
      summary = "Named in honor of Dmitri Mendeleev, creator of the Periodic Table; first synthesized one atom at a time by bombarding einsteinium with alpha particles.",
    ),
    ChemicalElement(
      atomicNumber = 102, symbol = "No", name = "Nobelium", atomicMass = 259.0,
      category = ElementCategory.ACTINIDE, group = null, period = 7, block = ElementBlock.F,
      gridRow = 10, gridCol = 16, electronConfiguration = "[Rn] 5f¹⁴ 7s²", electronShells = listOf(2, 8, 18, 32, 32, 8, 2),
      electronegativity = 1.30, density = 9.9, meltingPointK = 1100.0, boilingPointK = null,
      atomicRadiusPm = null, ionizationEnergyKjMol = 642.0, oxidationStates = "+2, +3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1966", discoveredBy = "Joint Institute for Nuclear Research (Dubna)",
      summary = "A synthetic closed-5f-shell actinide named after Alfred Nobel that uniquely favors the divalent (+2) oxidation state in aqueous solution.",
    ),
    ChemicalElement(
      atomicNumber = 103, symbol = "Lr", name = "Lawrencium", atomicMass = 266.0,
      category = ElementCategory.ACTINIDE, group = null, period = 7, block = ElementBlock.D,
      gridRow = 10, gridCol = 17, electronConfiguration = "[Rn] 5f¹⁴ 7s² 7p¹", electronShells = listOf(2, 8, 18, 32, 32, 8, 3),
      electronegativity = 1.30, density = 14.4, meltingPointK = 1900.0, boilingPointK = null,
      atomicRadiusPm = null, ionizationEnergyKjMol = 470.0, oxidationStates = "+3",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1961", discoveredBy = "Albert Ghiorso, Torbjørn Sikkeland, Almon Larsh & Robert Latimer",
      summary = "The final member of the actinide series, named after cyclotron inventor Ernest O. Lawrence, with relativistic stabilization of its 7p¹ valence electron.",
    ),

    // -------------------- PERIOD 7 CONTINUED (104..118) --------------------
    ChemicalElement(
      atomicNumber = 104, symbol = "Rf", name = "Rutherfordium", atomicMass = 267.0,
      category = ElementCategory.TRANSITION_METAL, group = 4, period = 7, block = ElementBlock.D,
      gridRow = 7, gridCol = 4, electronConfiguration = "[Rn] 5f¹⁴ 6d² 7s²", electronShells = listOf(2, 8, 18, 32, 32, 10, 2),
      electronegativity = null, density = 17.0, meltingPointK = 2400.0, boilingPointK = 5800.0,
      atomicRadiusPm = null, ionizationEnergyKjMol = 580.0, oxidationStates = "+4",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1964", discoveredBy = "JINR Dubna & Lawrence Berkeley National Laboratory",
      summary = "The first transactinide (superheavy) element, named after Ernest Rutherford and confirmed to behave chemically as a Group 4 homolog of hafnium.",
    ),
    ChemicalElement(
      atomicNumber = 105, symbol = "Db", name = "Dubnium", atomicMass = 268.0,
      category = ElementCategory.TRANSITION_METAL, group = 5, period = 7, block = ElementBlock.D,
      gridRow = 7, gridCol = 5, electronConfiguration = "[Rn] 5f¹⁴ 6d³ 7s²", electronShells = listOf(2, 8, 18, 32, 32, 11, 2),
      electronegativity = null, density = 21.6, meltingPointK = null, boilingPointK = null,
      atomicRadiusPm = null, ionizationEnergyKjMol = 665.0, oxidationStates = "+5",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1968", discoveredBy = "JINR Dubna & Lawrence Berkeley National Laboratory",
      summary = "A synthetic Group 5 superheavy transition metal named after the nuclear research town of Dubna, Russia.",
    ),
    ChemicalElement(
      atomicNumber = 106, symbol = "Sg", name = "Seaborgium", atomicMass = 269.0,
      category = ElementCategory.TRANSITION_METAL, group = 6, period = 7, block = ElementBlock.D,
      gridRow = 7, gridCol = 6, electronConfiguration = "[Rn] 5f¹⁴ 6d⁴ 7s²", electronShells = listOf(2, 8, 18, 32, 32, 12, 2),
      electronegativity = null, density = 23.0, meltingPointK = null, boilingPointK = null,
      atomicRadiusPm = null, ionizationEnergyKjMol = 757.0, oxidationStates = "+6",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1974", discoveredBy = "Lawrence Berkeley National Laboratory",
      summary = "A synthetic Group 6 transactinide named after nuclear chemist Glenn T. Seaborg; forms volatile hexacarbonyl Sg(CO)₆ analogous to tungsten.",
    ),
    ChemicalElement(
      atomicNumber = 107, symbol = "Bh", name = "Bohrium", atomicMass = 270.0,
      category = ElementCategory.TRANSITION_METAL, group = 7, period = 7, block = ElementBlock.D,
      gridRow = 7, gridCol = 7, electronConfiguration = "[Rn] 5f¹⁴ 6d⁵ 7s²", electronShells = listOf(2, 8, 18, 32, 32, 13, 2),
      electronegativity = null, density = 26.0, meltingPointK = null, boilingPointK = null,
      atomicRadiusPm = null, ionizationEnergyKjMol = 740.0, oxidationStates = "+7",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1981", discoveredBy = "Peter Armbruster & Gottfried Münzenberg (GSI Darmstadt)",
      summary = "A synthetic Group 7 superheavy element named after quantum physicist Niels Bohr, forming a volatile oxychloride BhO₃Cl similar to rhenium.",
    ),
    ChemicalElement(
      atomicNumber = 108, symbol = "Hs", name = "Hassium", atomicMass = 277.0,
      category = ElementCategory.TRANSITION_METAL, group = 8, period = 7, block = ElementBlock.D,
      gridRow = 7, gridCol = 8, electronConfiguration = "[Rn] 5f¹⁴ 6d⁶ 7s²", electronShells = listOf(2, 8, 18, 32, 32, 14, 2),
      electronegativity = null, density = 27.0, meltingPointK = null, boilingPointK = null,
      atomicRadiusPm = null, ionizationEnergyKjMol = 730.0, oxidationStates = "+8",
      standardPhase = MatterPhase.SOLID, discoveryYear = "1984", discoveredBy = "Peter Armbruster & Gottfried Münzenberg (GSI Darmstadt)",
      summary = "Named after the German state of Hesse, hassium is a Group 8 transactinide experimentally verified to form a volatile tetroxide (HsO₄) like osmium.",
    ),
    ChemicalElement(
      atomicNumber = 109, symbol = "Mt", name = "Meitnerium", atomicMass = 278.0,
      category = ElementCategory.TRANSITION_METAL, group = 9, period = 7, block = ElementBlock.D,
      gridRow = 7, gridCol = 9, electronConfiguration = "[Rn] 5f¹⁴ 6d⁷ 7s²", electronShells = listOf(2, 8, 18, 32, 32, 15, 2),
      electronegativity = null, density = 27.0, meltingPointK = null, boilingPointK = null,
      atomicRadiusPm = null, ionizationEnergyKjMol = 800.0, oxidationStates = "+1, +3, +6",
      standardPhase = MatterPhase.UNKNOWN, discoveryYear = "1982", discoveredBy = "Peter Armbruster & Gottfried Münzenberg (GSI Darmstadt)",
      summary = "A synthetic Group 9 superheavy element named in honor of physicist Lise Meitner, co-discoverer of nuclear fission.",
    ),
    ChemicalElement(
      atomicNumber = 110, symbol = "Ds", name = "Darmstadtium", atomicMass = 281.0,
      category = ElementCategory.TRANSITION_METAL, group = 10, period = 7, block = ElementBlock.D,
      gridRow = 7, gridCol = 10, electronConfiguration = "[Rn] 5f¹⁴ 6d⁸ 7s²", electronShells = listOf(2, 8, 18, 32, 32, 16, 2),
      electronegativity = null, density = 26.0, meltingPointK = null, boilingPointK = null,
      atomicRadiusPm = null, ionizationEnergyKjMol = 960.0, oxidationStates = "0, +2, +4, +6",
      standardPhase = MatterPhase.UNKNOWN, discoveryYear = "1994", discoveredBy = "Sigurd Hofmann & team (GSI Darmstadt)",
      summary = "A synthetic Group 10 superheavy element named after Darmstadt, Germany, where it was synthesized via cold fusion of lead-208 and nickel-62.",
    ),
    ChemicalElement(
      atomicNumber = 111, symbol = "Rg", name = "Roentgenium", atomicMass = 282.0,
      category = ElementCategory.TRANSITION_METAL, group = 11, period = 7, block = ElementBlock.D,
      gridRow = 7, gridCol = 11, electronConfiguration = "[Rn] 5f¹⁴ 6d⁹ 7s²", electronShells = listOf(2, 8, 18, 32, 32, 17, 2),
      electronegativity = null, density = 22.0, meltingPointK = null, boilingPointK = null,
      atomicRadiusPm = null, ionizationEnergyKjMol = 1020.0, oxidationStates = "-1, +1, +3, +5",
      standardPhase = MatterPhase.UNKNOWN, discoveryYear = "1994", discoveredBy = "Sigurd Hofmann & team (GSI Darmstadt)",
      summary = "A synthetic Group 11 coin-group superheavy element named after Wilhelm Conrad Röntgen, discoverer of X-rays.",
    ),
    ChemicalElement(
      atomicNumber = 112, symbol = "Cn", name = "Copernicium", atomicMass = 285.0,
      category = ElementCategory.TRANSITION_METAL, group = 12, period = 7, block = ElementBlock.D,
      gridRow = 7, gridCol = 12, electronConfiguration = "[Rn] 5f¹⁴ 6d¹⁰ 7s²", electronShells = listOf(2, 8, 18, 32, 32, 18, 2),
      electronegativity = null, density = 14.0, meltingPointK = 283.0, boilingPointK = 340.0,
      atomicRadiusPm = null, ionizationEnergyKjMol = 1155.0, oxidationStates = "0, +2",
      standardPhase = MatterPhase.UNKNOWN, discoveryYear = "1996", discoveredBy = "Sigurd Hofmann & team (GSI Darmstadt)",
      summary = "A Group 12 superheavy element named after astronomer Nicolaus Copernicus; strong relativistic contraction of the 7s² shell makes it highly volatile.",
    ),
    ChemicalElement(
      atomicNumber = 113, symbol = "Nh", name = "Nihonium", atomicMass = 286.0,
      category = ElementCategory.POST_TRANSITION_METAL, group = 13, period = 7, block = ElementBlock.P,
      gridRow = 7, gridCol = 13, electronConfiguration = "[Rn] 5f¹⁴ 6d¹⁰ 7s² 7p¹", electronShells = listOf(2, 8, 18, 32, 32, 18, 3),
      electronegativity = null, density = 16.0, meltingPointK = 700.0, boilingPointK = 1400.0,
      atomicRadiusPm = null, ionizationEnergyKjMol = 704.9, oxidationStates = "+1, +3",
      standardPhase = MatterPhase.UNKNOWN, discoveryYear = "2004", discoveredBy = "Kosuke Morita & RIKEN team (Japan)",
      summary = "The first chemical element discovered in Asia, named after 'Nihon' (Japan) where it was synthesized at the RIKEN Nishina Center.",
    ),
    ChemicalElement(
      atomicNumber = 114, symbol = "Fl", name = "Flerovium", atomicMass = 289.0,
      category = ElementCategory.POST_TRANSITION_METAL, group = 14, period = 7, block = ElementBlock.P,
      gridRow = 7, gridCol = 14, electronConfiguration = "[Rn] 5f¹⁴ 6d¹⁰ 7s² 7p²", electronShells = listOf(2, 8, 18, 32, 32, 18, 4),
      electronegativity = null, density = 11.4, meltingPointK = 200.0, boilingPointK = 380.0,
      atomicRadiusPm = null, ionizationEnergyKjMol = 823.9, oxidationStates = "0, +2, +4",
      standardPhase = MatterPhase.UNKNOWN, discoveryYear = "1999", discoveredBy = "JINR Dubna & Lawrence Livermore National Laboratory",
      summary = "A superheavy Group 14 element near the theoretical 'island of stability' (Z = 114, N = 184), named after the Flerov Laboratory of Nuclear Reactions.",
    ),
    ChemicalElement(
      atomicNumber = 115, symbol = "Mc", name = "Moscovium", atomicMass = 290.0,
      category = ElementCategory.POST_TRANSITION_METAL, group = 15, period = 7, block = ElementBlock.P,
      gridRow = 7, gridCol = 15, electronConfiguration = "[Rn] 5f¹⁴ 6d¹⁰ 7s² 7p³", electronShells = listOf(2, 8, 18, 32, 32, 18, 5),
      electronegativity = null, density = 13.5, meltingPointK = 670.0, boilingPointK = 1400.0,
      atomicRadiusPm = null, ionizationEnergyKjMol = 538.4, oxidationStates = "+1, +3",
      standardPhase = MatterPhase.UNKNOWN, discoveryYear = "2003", discoveredBy = "JINR Dubna & Lawrence Livermore National Laboratory",
      summary = "A synthetic Group 15 pnictogen superheavy element named after the Moscow Oblast region where the Joint Institute for Nuclear Research is located.",
    ),
    ChemicalElement(
      atomicNumber = 116, symbol = "Lv", name = "Livermorium", atomicMass = 293.0,
      category = ElementCategory.POST_TRANSITION_METAL, group = 16, period = 7, block = ElementBlock.P,
      gridRow = 7, gridCol = 16, electronConfiguration = "[Rn] 5f¹⁴ 6d¹⁰ 7s² 7p⁴", electronShells = listOf(2, 8, 18, 32, 32, 18, 6),
      electronegativity = null, density = 12.9, meltingPointK = 700.0, boilingPointK = 1100.0,
      atomicRadiusPm = null, ionizationEnergyKjMol = 663.9, oxidationStates = "+2, +4",
      standardPhase = MatterPhase.UNKNOWN, discoveryYear = "2000", discoveredBy = "JINR Dubna & Lawrence Livermore National Laboratory",
      summary = "A synthetic Group 16 superheavy chalcogen named in honor of the Lawrence Livermore National Laboratory and the city of Livermore, California.",
    ),
    ChemicalElement(
      atomicNumber = 117, symbol = "Ts", name = "Tennessine", atomicMass = 294.0,
      category = ElementCategory.HALOGEN, group = 17, period = 7, block = ElementBlock.P,
      gridRow = 7, gridCol = 17, electronConfiguration = "[Rn] 5f¹⁴ 6d¹⁰ 7s² 7p⁵", electronShells = listOf(2, 8, 18, 32, 32, 18, 7),
      electronegativity = null, density = 7.2, meltingPointK = 723.0, boilingPointK = 883.0,
      atomicRadiusPm = null, ionizationEnergyKjMol = 743.0, oxidationStates = "-1, +1, +3, +5",
      standardPhase = MatterPhase.UNKNOWN, discoveryYear = "2010", discoveredBy = "JINR Dubna, Oak Ridge National Laboratory & Vanderbilt University",
      summary = "A superheavy Group 17 element named after the state of Tennessee (home to Oak Ridge National Laboratory, which supplied the berkelium-249 target).",
    ),
    ChemicalElement(
      atomicNumber = 118, symbol = "Og", name = "Oganesson", atomicMass = 294.0,
      category = ElementCategory.NOBLE_GAS, group = 18, period = 7, block = ElementBlock.P,
      gridRow = 7, gridCol = 18, electronConfiguration = "[Rn] 5f¹⁴ 6d¹⁰ 7s² 7p⁶", electronShells = listOf(2, 8, 18, 32, 32, 18, 8),
      electronegativity = null, density = 7.0, meltingPointK = 325.0, boilingPointK = 450.0,
      atomicRadiusPm = null, ionizationEnergyKjMol = 860.0, oxidationStates = "0, +2, +4",
      standardPhase = MatterPhase.UNKNOWN, discoveryYear = "2002", discoveredBy = "Yuri Oganessian & JINR/LLNL team",
      summary = "The heaviest element on the Periodic Table (Z = 118), completing Period 7 and named in honor of nuclear physicist Yuri Oganessian.",
    ),
  )

  private val BY_ATOMIC_NUMBER: Map<Int, ChemicalElement> =
    ALL_ELEMENTS.associateBy { it.atomicNumber }

  private val BY_SYMBOL_CASE_SENSITIVE: Map<String, ChemicalElement> =
    ALL_ELEMENTS.associateBy { it.symbol }

  private val BY_SYMBOL_LOWER: Map<String, ChemicalElement> =
    ALL_ELEMENTS.associateBy { it.symbol.lowercase(Locale.ROOT) }

  private val BY_GRID_CELL: Map<Pair<Int, Int>, ChemicalElement> =
    ALL_ELEMENTS.associateBy { it.gridRow to it.gridCol }

  fun byAtomicNumber(atomicNumber: Int): ChemicalElement? = BY_ATOMIC_NUMBER[atomicNumber]

  fun bySymbol(symbol: String): ChemicalElement? =
    BY_SYMBOL_LOWER[symbol.trim().lowercase(Locale.ROOT)]

  fun byGridPosition(row: Int, col: Int): ChemicalElement? = BY_GRID_CELL[row to col]

  /**
   * Filters and sorts elements according to optional search query, category filter, phase filter,
   * block filter, and favorites-only flag.
   */
  fun filterElements(
    query: String = "",
    category: ElementCategory? = null,
    phase: MatterPhase? = null,
    block: ElementBlock? = null,
    favoritesOnly: Boolean = false,
    favoriteAtomicNumbers: Set<Int> = emptySet(),
    temperatureK: Double? = null,
  ): List<ChemicalElement> {
    val trimmed = query.trim()
    val queryLower = trimmed.lowercase(Locale.ROOT)
    val queryAtomicNum = trimmed.toIntOrNull()

    return ALL_ELEMENTS.filter { element ->
      if (favoritesOnly && element.atomicNumber !in favoriteAtomicNumbers) return@filter false
      if (category != null && element.category != category) return@filter false
      if (block != null && element.block != block) return@filter false
      if (phase != null) {
        val activePhase = if (temperatureK != null) {
          element.phaseAtTemperature(temperatureK)
        } else {
          element.standardPhase
        }
        if (activePhase != phase) return@filter false
      }
      if (queryLower.isEmpty()) return@filter true

      if (queryAtomicNum != null && element.atomicNumber == queryAtomicNum) return@filter true
      element.symbol.lowercase(Locale.ROOT) == queryLower ||
        element.name.lowercase(Locale.ROOT).contains(queryLower) ||
        element.symbol.lowercase(Locale.ROOT).contains(queryLower) ||
        element.atomicNumber.toString().startsWith(queryLower) ||
        element.category.label.lowercase(Locale.ROOT).contains(queryLower) ||
        element.electronConfiguration.lowercase(Locale.ROOT).contains(queryLower) ||
        element.discoveredBy.lowercase(Locale.ROOT).contains(queryLower) ||
        element.discoveryYear.lowercase(Locale.ROOT).contains(queryLower) ||
        element.summary.lowercase(Locale.ROOT).contains(queryLower)
    }.sortedWith(
      compareBy<ChemicalElement> { element ->
        if (queryLower.isEmpty()) {
          0
        } else {
          when {
            element.symbol.equals(trimmed, ignoreCase = true) -> 0
            queryAtomicNum != null && element.atomicNumber == queryAtomicNum -> 1
            element.name.equals(trimmed, ignoreCase = true) -> 2
            element.name.lowercase(Locale.ROOT).startsWith(queryLower) -> 3
            element.symbol.lowercase(Locale.ROOT).startsWith(queryLower) -> 4
            else -> 5
          }
        }
      }.thenBy { it.atomicNumber }
    )
  }

  /**
   * Parses a chemical formula (supporting nested parentheses/brackets, subscript digits `₀–₉`,
   * and hydrate middle dots `·` / `*`, e.g., `"H2O"`, `"C6H12O6"`, `"Ca(OH)2"`, `"CuSO4·5H2O"`)
   * and computes its total molar mass and per-element mass percentages.
   */
  fun calculateMolarMass(rawFormula: String): MolarMassResult {
    val normalized = normalizeChemicalFormula(rawFormula)
    if (normalized.isEmpty()) {
      return MolarMassResult.Error("Enter a chemical formula (e.g. H2O, C6H12O6, Ca(OH)2)")
    }

    // Support hydrates separated by '.' or '·' (e.g., CuSO4.5H2O)
    val parts = normalized.split('.', '·', '*')
      .map { it.trim() }
      .filter { it.isNotEmpty() }
    if (parts.isEmpty()) {
      return MolarMassResult.Error("Invalid chemical formula")
    }

    val totalCounts = linkedMapOf<ChemicalElement, Int>()
    for (part in parts) {
      var idx = 0
      var leadingMultiplier = 1
      if (idx < part.length && part[idx].isDigit()) {
        val start = idx
        while (idx < part.length && part[idx].isDigit()) idx++
        leadingMultiplier = part.substring(start, idx).toIntOrNull()
          ?: return MolarMassResult.Error("Multiplier too large in '$part'")
        if (leadingMultiplier <= 0) {
          return MolarMassResult.Error("Formula multiplier must be positive")
        }
      }
      val subFormula = part.substring(idx)
      if (subFormula.isEmpty()) {
        return MolarMassResult.Error("Missing elements after multiplier in '$part'")
      }

      when (val subResult = parseSingleFormulaTerm(subFormula)) {
        is FormulaTermParseResult.Error -> return MolarMassResult.Error(subResult.message)
        is FormulaTermParseResult.Success -> {
          for ((el, count) in subResult.counts) {
            totalCounts[el] = (totalCounts[el] ?: 0) + count * leadingMultiplier
          }
        }
      }
    }

    if (totalCounts.isEmpty()) {
      return MolarMassResult.Error("No valid chemical elements found in '$rawFormula'")
    }

    val totalMass = totalCounts.entries.sumOf { (el, count) -> el.atomicMass * count }
    val totalAtoms = totalCounts.values.sum()
    val components = totalCounts.entries
      .sortedBy { it.key.atomicNumber }
      .map { (el, count) ->
        val sub = el.atomicMass * count
        val pct = if (totalMass > 0.0) (sub / totalMass) * 100.0 else 0.0
        MolarMassElementComponent(
          element = el,
          count = count,
          subtotalMass = sub,
          massPercentage = pct,
        )
      }

    return MolarMassResult.Success(
      formula = normalized,
      totalMolarMass = totalMass,
      totalAtoms = totalAtoms,
      components = components,
    )
  }

  private fun normalizeChemicalFormula(raw: String): String {
    val sb = StringBuilder()
    for (ch in raw.trim()) {
      when (ch) {
        '₀' -> sb.append('0')
        '₁' -> sb.append('1')
        '₂' -> sb.append('2')
        '₃' -> sb.append('3')
        '₄' -> sb.append('4')
        '₅' -> sb.append('5')
        '₆' -> sb.append('6')
        '₇' -> sb.append('7')
        '₈' -> sb.append('8')
        '₉' -> sb.append('9')
        '[', '{' -> sb.append('(')
        ']', '}' -> sb.append(')')
        ' ' -> {}
        else -> sb.append(ch)
      }
    }
    return sb.toString()
  }

  private sealed class FormulaTermParseResult {
    data class Success(val counts: Map<ChemicalElement, Int>) : FormulaTermParseResult()
    data class Error(val message: String) : FormulaTermParseResult()
  }

  private fun parseSingleFormulaTerm(formula: String): FormulaTermParseResult {
    val stack = ArrayDeque<MutableMap<ChemicalElement, Int>>()
    stack.addLast(linkedMapOf())
    var i = 0

    while (i < formula.length) {
      val ch = formula[i]
      when {
        ch == '(' -> {
          stack.addLast(linkedMapOf())
          i++
        }
        ch == ')' -> {
          if (stack.size <= 1) {
            return FormulaTermParseResult.Error("Unmatched closing parenthesis ')'")
          }
          i++
          val numStart = i
          while (i < formula.length && formula[i].isDigit()) i++
          val groupMultiplier = if (i > numStart) {
            formula.substring(numStart, i).toIntOrNull()
              ?: return FormulaTermParseResult.Error("Subscript number too large")
          } else {
            1
          }
          if (groupMultiplier <= 0) {
            return FormulaTermParseResult.Error("Subscript must be at least 1")
          }
          val popped = stack.removeLast()
          if (popped.isEmpty()) {
            return FormulaTermParseResult.Error("Empty parentheses '()' in formula")
          }
          val top = stack.last()
          for ((el, count) in popped) {
            top[el] = (top[el] ?: 0) + count * groupMultiplier
          }
        }
        ch.isUpperCase() -> {
          val symStart = i
          i++
          while (i < formula.length && formula[i].isLowerCase()) i++
          val symbol = formula.substring(symStart, i)
          val element = BY_SYMBOL_CASE_SENSITIVE[symbol]
            ?: return FormulaTermParseResult.Error("Unknown element symbol '$symbol'")

          val numStart = i
          while (i < formula.length && formula[i].isDigit()) i++
          val count = if (i > numStart) {
            formula.substring(numStart, i).toIntOrNull()
              ?: return FormulaTermResultTooLarge()
          } else {
            1
          }
          if (count <= 0) {
            return FormulaTermParseResult.Error("Element count must be at least 1")
          }
          val top = stack.last()
          top[element] = (top[element] ?: 0) + count
        }
        else -> {
          return FormulaTermParseResult.Error("Unexpected character '$ch' in formula")
        }
      }
    }

    if (stack.size != 1) {
      return FormulaTermParseResult.Error("Unclosed opening parenthesis '('")
    }
    return FormulaTermParseResult.Success(stack.last())
  }

  private fun FormulaTermResultTooLarge(): FormulaTermParseResult =
    FormulaTermParseResult.Error("Subscript count is too large")

  /**
   * Common sample formulas for quick one-tap testing in the Molar Mass Calculator.
   */
  val SAMPLE_FORMULAS: List<Pair<String, String>> = listOf(
    "H2O" to "Water",
    "NaCl" to "Sodium Chloride",
    "C6H12O6" to "Glucose",
    "Ca(OH)2" to "Calcium Hydroxide",
    "H2SO4" to "Sulfuric Acid",
    "C8H10N4O2" to "Caffeine",
    "Fe2O3" to "Iron(III) Oxide",
    "CuSO4·5H2O" to "Copper(II) Sulfate Pentahydrate",
  )

  /**
   * Applies a multiplicative pinch [zoomChange] to [currentZoom], clamped within
   * [MIN_TABLE_ZOOM] (`1.0f`, full device width) and [MAX_TABLE_ZOOM] (`4.0f`).
   */
  fun calculateTableZoom(currentZoom: Float, zoomChange: Float): Float {
    val base = currentZoom.coerceIn(MIN_TABLE_ZOOM, MAX_TABLE_ZOOM)
    if (zoomChange <= 0f || zoomChange.isNaN()) return base
    return (base * zoomChange).coerceIn(MIN_TABLE_ZOOM, MAX_TABLE_ZOOM)
  }

  /**
   * Computes pixel dimensions for the 18-column Periodic Table grid so that at
   * [DEFAULT_TABLE_ZOOM] (`1.0f`) the entire 18-column table + period column spans the full
   * [viewportWidthPx] without overflowing, and scales monotonically as [zoom] increases.
   */
  fun calculateTableGridMetrics(
    viewportWidthPx: Int,
    zoom: Float = DEFAULT_TABLE_ZOOM,
    horizontalPaddingPx: Int = 0,
  ): PeriodicTableGridMetrics {
    val clampedZoom = zoom.coerceIn(MIN_TABLE_ZOOM, MAX_TABLE_ZOOM)
    val safeViewportWidthPx = viewportWidthPx.coerceAtLeast(19)
    val safePaddingPx = horizontalPaddingPx.coerceAtLeast(0).coerceAtMost(safeViewportWidthPx / 8)
    val baseContentWidthPx = (safeViewportWidthPx - 2 * safePaddingPx).coerceAtLeast(19)

    val baseSpacingPx =
      ((baseContentWidthPx * BASE_TILE_SPACING_DP) / BASE_TABLE_CONTENT_WIDTH_DP)
        .roundToInt()
        .coerceAtLeast(1)
    val basePeriodWidthPx =
      ((baseContentWidthPx * BASE_PERIOD_COLUMN_WIDTH_DP) / BASE_TABLE_CONTENT_WIDTH_DP)
        .roundToInt()
        .coerceAtLeast(1)
    val baseTileWidthPx =
      ((baseContentWidthPx - basePeriodWidthPx - 18 * baseSpacingPx) / 18)
        .coerceAtLeast(1)
    val baseTileHeightPx =
      ((baseTileWidthPx * BASE_TILE_HEIGHT_DP) / BASE_TILE_WIDTH_DP)
        .roundToInt()
        .coerceAtLeast(1)

    val tileWidthPx = (baseTileWidthPx * clampedZoom).roundToInt().coerceAtLeast(1)
    val tileHeightPx = (baseTileHeightPx * clampedZoom).roundToInt().coerceAtLeast(1)
    val periodColumnWidthPx = (basePeriodWidthPx * clampedZoom).roundToInt().coerceAtLeast(1)
    val tileSpacingPx = (baseSpacingPx * clampedZoom).roundToInt().coerceAtLeast(1)

    val contentWidthPx = periodColumnWidthPx + (18 * tileWidthPx) + (18 * tileSpacingPx)
    val tableWidthPx = maxOf(safeViewportWidthPx, contentWidthPx + (2 * safePaddingPx))

    return PeriodicTableGridMetrics(
      viewportWidthPx = safeViewportWidthPx,
      tableWidthPx = tableWidthPx,
      contentWidthPx = contentWidthPx,
      horizontalPaddingPx = safePaddingPx,
      periodColumnWidthPx = periodColumnWidthPx,
      tileWidthPx = tileWidthPx,
      tileHeightPx = tileHeightPx,
      tileSpacingPx = tileSpacingPx,
    )
  }
}
