package com.example.toolbox.ui.tools

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

const val STARS_PREFS_NAME = "stars_tool_prefs"
const val PREF_KEY_STARS_PROFILES_JSON = "stars_profiles_json"
const val PREF_KEY_STARS_ACTIVE_PROFILE_ID = "stars_active_profile_id"
const val PREF_KEY_STARS_MATCH_PARTNER_ID = "stars_match_partner_id"
const val PREF_KEY_STARS_FAVORITE_IDS = "stars_favorite_ids"

/**
 * The four classical astrological elements (Triplicities).
 */
enum class ZodiacElement(
  val label: String,
  val motto: String,
  val traits: String,
  val colorHex: String,
) {
  FIRE(
    label = "Fire",
    motto = "I Create & Inspire",
    traits = "Passionate, dynamic, spontaneous, courageous, and radiant.",
    colorHex = "#E65100",
  ),
  EARTH(
    label = "Earth",
    motto = "I Build & Ground",
    traits = "Practical, loyal, sensual, patient, and materially grounded.",
    colorHex = "#2E7D32",
  ),
  AIR(
    label = "Air",
    motto = "I Connect & Think",
    traits = "Intellectual, communicative, curious, social, and visionary.",
    colorHex = "#0277BD",
  ),
  WATER(
    label = "Water",
    motto = "I Feel & Heal",
    traits = "Intuitive, empathetic, deeply emotional, imaginative, and nurturing.",
    colorHex = "#6A1B9A",
  ),
}

/**
 * The three astrological modalities (Quadruplicities).
 */
enum class ZodiacModality(
  val label: String,
  val role: String,
  val description: String,
) {
  CARDINAL(
    label = "Cardinal",
    role = "Initiator",
    description = "Begins each season with bold leadership, fresh momentum, and pioneering drive.",
  ),
  FIXED(
    label = "Fixed",
    role = "Sustainer",
    description = "Anchors the middle of each season with persistence, depth, loyalty, and focus.",
  ),
  MUTABLE(
    label = "Mutable",
    role = "Transformer",
    description = "Completes each season with adaptability, resourcefulness, wisdom, and synthesis.",
  ),
}

/**
 * All 12 tropical zodiac signs in ecliptic order (`0°` Aries to `360°` Pisces).
 */
enum class ZodiacSign(
  val displayName: String,
  val glyph: String,
  val symbolName: String,
  val element: ZodiacElement,
  val modality: ZodiacModality,
  val dateRange: String,
  val startLongitude: Int,
  val rulingPlanetName: String,
  val naturalHouse: Int,
  val motto: String,
  val keywords: List<String>,
  val powerColorName: String,
  val powerColorHex: String,
  val birthstone: String,
  val bodyPart: String,
  val strengths: String,
  val challenges: String,
  val loveStyle: String,
  val careerStyle: String,
  val summary: String,
) {
  ARIES(
    displayName = "Aries",
    glyph = "♈",
    symbolName = "The Ram",
    element = ZodiacElement.FIRE,
    modality = ZodiacModality.CARDINAL,
    dateRange = "Mar 21 – Apr 19",
    startLongitude = 0,
    rulingPlanetName = "Mars",
    naturalHouse = 1,
    motto = "I Am",
    keywords = listOf("Bold", "Pioneering", "Fearless", "Direct", "Passionate"),
    powerColorName = "Crimson Flame",
    powerColorHex = "#D32F2F",
    birthstone = "Diamond & Bloodstone",
    bodyPart = "Head & Adrenals",
    strengths = "Unstoppable initiative, honesty, courage under pressure, infectious enthusiasm.",
    challenges = "Impatience, acting before reflecting, quick temper when slowed down.",
    loveStyle = "Ardent, expressive, and exhilarating; thrives on chemistry, spontaneity, and mutual independence.",
    careerStyle = "Natural entrepreneur and trailblazer who excels at launching bold projects and leading from the front.",
    summary = "The spark of the zodiac—Aries channels cardinal fire into fearless beginnings, honest self-expression, and protective loyalty.",
  ),
  TAURUS(
    displayName = "Taurus",
    glyph = "♉",
    symbolName = "The Bull",
    element = ZodiacElement.EARTH,
    modality = ZodiacModality.FIXED,
    dateRange = "Apr 20 – May 20",
    startLongitude = 30,
    rulingPlanetName = "Venus",
    naturalHouse = 2,
    motto = "I Have",
    keywords = listOf("Grounded", "Sensual", "Devoted", "Artistic", "Steadfast"),
    powerColorName = "Emerald Moss",
    powerColorHex = "#2E7D32",
    birthstone = "Emerald & Rose Quartz",
    bodyPart = "Throat & Neck",
    strengths = "Unwavering reliability, aesthetic grace, calm endurance, appreciation of beauty and comfort.",
    challenges = "Stubbornness, resistance to sudden change, over-attachment to routine.",
    loveStyle = "Tender, tactile, and deeply loyal; builds romance through shared comfort, touch, and lasting trust.",
    careerStyle = "Patient builder who masters craft, finance, design, and sustainable long-term value.",
    summary = "Ruled by Venus, Taurus turns fixed earth into lush sanctuary, steadfast devotion, and timeless sensory beauty.",
  ),
  GEMINI(
    displayName = "Gemini",
    glyph = "♊",
    symbolName = "The Twins",
    element = ZodiacElement.AIR,
    modality = ZodiacModality.MUTABLE,
    dateRange = "May 21 – Jun 20",
    startLongitude = 60,
    rulingPlanetName = "Mercury",
    naturalHouse = 3,
    motto = "I Think",
    keywords = listOf("Curious", "Witty", "Adaptable", "Expressive", "Quick"),
    powerColorName = "Sunlit Amber",
    powerColorHex = "#F9A825",
    birthstone = "Pearl & Agate",
    bodyPart = "Hands, Lungs & Nervous System",
    strengths = "Brilliant versatility, sparkling conversation, rapid learning, bridging diverse ideas and people.",
    challenges = "Restlessness, scattered focus, overthinking emotional nuances.",
    loveStyle = "Playful, intellectually electric, and communicative; falls in love through wit, curiosity, and shared adventures.",
    careerStyle = "Thrives in storytelling, media, strategy, teaching, and fast-moving environments that reward agility.",
    summary = "The cosmic messenger—Gemini weaves mutable air into lively curiosity, quick wit, and kaleidoscopic perspective.",
  ),
  CANCER(
    displayName = "Cancer",
    glyph = "♋",
    symbolName = "The Crab",
    element = ZodiacElement.WATER,
    modality = ZodiacModality.CARDINAL,
    dateRange = "Jun 21 – Jul 22",
    startLongitude = 90,
    rulingPlanetName = "Moon",
    naturalHouse = 4,
    motto = "I Feel",
    keywords = listOf("Intuitive", "Nurturing", "Protective", "Soulful", "Devoted"),
    powerColorName = "Moonlit Silver",
    powerColorHex = "#5C6BC0",
    birthstone = "Ruby & Moonstone",
    bodyPart = "Chest & Stomach",
    strengths = "Profound emotional intelligence, fierce protection of loved ones, uncanny intuition, creating warmth.",
    challenges = "Retreating into a shell when hurt, mood fluctuations tied to lunar tides, holding onto the past.",
    loveStyle = "Deeply caring, soulful, and domestic; seeks emotional safety, tender devotion, and a true sense of home.",
    careerStyle = "Leads with empathy and foresight, excelling in caregiving, hospitality, arts, real estate, and community leadership.",
    summary = "Guided by the Moon, Cancer blends cardinal leadership with deep emotional waters to nurture, protect, and remember.",
  ),
  LEO(
    displayName = "Leo",
    glyph = "♌",
    symbolName = "The Lion",
    element = ZodiacElement.FIRE,
    modality = ZodiacModality.FIXED,
    dateRange = "Jul 23 – Aug 22",
    startLongitude = 120,
    rulingPlanetName = "Sun",
    naturalHouse = 5,
    motto = "I Will",
    keywords = listOf("Radiant", "Generous", "Warmhearted", "Creative", "Charismatic"),
    powerColorName = "Solar Gold",
    powerColorHex = "#EF6C00",
    birthstone = "Peridot & Onyx",
    bodyPart = "Heart & Upper Spine",
    strengths = "Big-hearted generosity, creative flair, natural warmth, loyalty that uplifts everyone nearby.",
    challenges = "Pride when unappreciated, dramatic reactions, difficulty sharing the spotlight.",
    loveStyle = "Grand, affectionate, and fiercely devoted; loves celebrating their partner and keeping romance vibrant.",
    careerStyle = "Shines in creative direction, leadership, performance, design, and any role calling for heart and vision.",
    summary = "Ruled by the Sun, Leo radiates fixed fire—bringing warmth, creative celebration, and noble loyalty to every room.",
  ),
  VIRGO(
    displayName = "Virgo",
    glyph = "♍",
    symbolName = "The Maiden",
    element = ZodiacElement.EARTH,
    modality = ZodiacModality.MUTABLE,
    dateRange = "Aug 23 – Sep 22",
    startLongitude = 150,
    rulingPlanetName = "Mercury",
    naturalHouse = 6,
    motto = "I Analyze",
    keywords = listOf("Thoughtful", "Precise", "Helpful", "Discerning", "Refined"),
    powerColorName = "Sage Olive",
    powerColorHex = "#558B2F",
    birthstone = "Sapphire & Carnelian",
    bodyPart = "Digestive System",
    strengths = "Graceful attention to detail, practical wisdom, quiet devotion through acts of service, clear problem-solving.",
    challenges = "Self-criticism, perfectionism, worrying over small imperfections.",
    loveStyle = "Attentive, sincere, and quietly romantic; shows love by remembering every small detail that makes life better.",
    careerStyle = "Master craftsperson, analyst, healer, editor, and strategist who turns chaos into elegant order.",
    summary = "Combining Mercury's intellect with mutable earth, Virgo refines daily life into purposeful craft, healing, and devotion.",
  ),
  LIBRA(
    displayName = "Libra",
    glyph = "♎",
    symbolName = "The Scales",
    element = ZodiacElement.AIR,
    modality = ZodiacModality.CARDINAL,
    dateRange = "Sep 23 – Oct 22",
    startLongitude = 180,
    rulingPlanetName = "Venus",
    naturalHouse = 7,
    motto = "I Balance",
    keywords = listOf("Harmonious", "Charming", "Diplomatic", "Aesthetic", "Fair"),
    powerColorName = "Rose Quartz",
    powerColorHex = "#D81B60",
    birthstone = "Opal & Lapis Lazuli",
    bodyPart = "Lower Back & Kidneys",
    strengths = "Effortless grace, fairness, artistic sophistication, ability to see every side and foster harmony.",
    challenges = "Indecisiveness when weighing options, avoiding conflict to keep the peace.",
    loveStyle = "Romantic, partnership-centered, and poetic; thrives on beauty, thoughtful gestures, and deep equality.",
    careerStyle = "Excels in law, design, diplomacy, curation, counseling, and collaborative leadership.",
    summary = "Ruled by Venus, Libra channels cardinal air to initiate harmony, justice, beauty, and sacred partnership.",
  ),
  SCORPIO(
    displayName = "Scorpio",
    glyph = "♏",
    symbolName = "The Scorpion",
    element = ZodiacElement.WATER,
    modality = ZodiacModality.FIXED,
    dateRange = "Oct 23 – Nov 21",
    startLongitude = 210,
    rulingPlanetName = "Pluto",
    naturalHouse = 8,
    motto = "I Transform",
    keywords = listOf("Magnetic", "Intense", "Perceptive", "Loyal", "Alchemical"),
    powerColorName = "Obsidian Wine",
    powerColorHex = "#8E24AA",
    birthstone = "Topaz & Malachite",
    bodyPart = "Reproductive & Regenerative System",
    strengths = "Fearless emotional depth, X-ray intuition, unbreakable loyalty, power to regenerate and transform.",
    challenges = "Secrecy, guarding vulnerability too tightly, all-or-nothing intensity.",
    loveStyle = "Soul-deep, magnetic, and fiercely Ride-or-Die; craves total honesty, intimacy, and transformative bond.",
    careerStyle = "Brilliantly suited for research, psychology, investigation, strategy, healing, and high-stakes leadership.",
    summary = "Fixed water at its deepest—Scorpio sees beneath surfaces to alchemize truth, intimacy, and rebirth.",
  ),
  SAGITTARIUS(
    displayName = "Sagittarius",
    glyph = "♐",
    symbolName = "The Archer",
    element = ZodiacElement.FIRE,
    modality = ZodiacModality.MUTABLE,
    dateRange = "Nov 22 – Dec 21",
    startLongitude = 240,
    rulingPlanetName = "Jupiter",
    naturalHouse = 9,
    motto = "I Explore",
    keywords = listOf("Adventurous", "Optimistic", "Philosophical", "Free-Spirited", "Honest"),
    powerColorName = "Royal Indigo",
    powerColorHex = "#3949AB",
    birthstone = "Turquoise & Tanzanite",
    bodyPart = "Hips & Thighs",
    strengths = "Boundless optimism, love of truth and exploration, humor, inspiring big-picture wisdom.",
    challenges = "Blunt delivery, restlessness with routine, over-promising in excitement.",
    loveStyle = "Joyful, candid, and expansive; seeks a best friend and co-adventurer to explore life's horizons together.",
    careerStyle = "Flourishes in travel, philosophy, higher education, publishing, coaching, and visionary ventures.",
    summary = "Ruled by Jupiter, Sagittarius aims mutable fire toward distant horizons, wisdom, laughter, and authentic freedom.",
  ),
  CAPRICORN(
    displayName = "Capricorn",
    glyph = "♑",
    symbolName = "The Sea-Goat",
    element = ZodiacElement.EARTH,
    modality = ZodiacModality.CARDINAL,
    dateRange = "Dec 22 – Jan 19",
    startLongitude = 270,
    rulingPlanetName = "Saturn",
    naturalHouse = 10,
    motto = "I Achieve",
    keywords = listOf("Ambitious", "Resilient", "Wise", "Dry-Witted", "Masterful"),
    powerColorName = "Slate Forest",
    powerColorHex = "#37474F",
    birthstone = "Garnet & Black Tourmaline",
    bodyPart = "Knees, Bones & Joints",
    strengths = "Quiet authority, long-range strategic mastery, dry humor, unwavering integrity when commitments matter.",
    challenges = "Carrying too much responsibility alone, workaholism, being overly stoic.",
    loveStyle = "Devoted, protective, and enduring; expresses love through rock-solid commitment and building a legacy together.",
    careerStyle = "Architect of lasting institutions—excels in executive leadership, engineering, finance, and mastery of craft.",
    summary = "Guided by Saturn, Capricorn climbs with cardinal earth determination while keeping the Sea-Goat's soulful depth.",
  ),
  AQUARIUS(
    displayName = "Aquarius",
    glyph = "♒",
    symbolName = "The Water-Bearer",
    element = ZodiacElement.AIR,
    modality = ZodiacModality.FIXED,
    dateRange = "Jan 20 – Feb 18",
    startLongitude = 300,
    rulingPlanetName = "Uranus",
    naturalHouse = 11,
    motto = "I Envision",
    keywords = listOf("Original", "Humanitarian", "Independent", "Visionary", "Inventive"),
    powerColorName = "Electric Cyan",
    powerColorHex = "#00838F",
    birthstone = "Amethyst & Aquamarine",
    bodyPart = "Ankles & Circulatory System",
    strengths = "Future-forward originality, egalitarian warmth, intellectual independence, loyalty to authentic community.",
    challenges = "Emotional detachment when overwhelmed, contrarian streak, living too far in the future.",
    loveStyle = "Unconventional, cerebral, and deeply respectful of individuality; bonds through friendship and shared ideals.",
    careerStyle = "Innovator in technology, science, social reform, design, and community networks.",
    summary = "Fixed air electrified by Uranus—Aquarius pours new consciousness, invention, and authentic freedom into the collective.",
  ),
  PISCES(
    displayName = "Pisces",
    glyph = "♓",
    symbolName = "The Fishes",
    element = ZodiacElement.WATER,
    modality = ZodiacModality.MUTABLE,
    dateRange = "Feb 19 – Mar 20",
    startLongitude = 330,
    rulingPlanetName = "Neptune",
    naturalHouse = 12,
    motto = "I Dream",
    keywords = listOf("Mystical", "Compassionate", "Poetic", "Imaginative", "Tender"),
    powerColorName = "Seafoam Violet",
    powerColorHex = "#7B1FA2",
    birthstone = "Aquamarine & Amethyst",
    bodyPart = "Feet & Lymphatic System",
    strengths = "Boundless compassion, artistic and spiritual depth, dreamlike intuition, healing presence.",
    challenges = "Absorbing others' emotions like a sponge, escapism, blurry boundaries.",
    loveStyle = "Soulmate-oriented, poetic, and enchanted; loves with unconditional tenderness and spiritual resonance.",
    careerStyle = "Thrives in music, visual arts, healing, psychology, cinema, spirituality, and compassionate service.",
    summary = "The final sign of the wheel—Pisces dissolves boundaries in mutable water, weaving empathy, art, and transcendent soul.",
  );

  val oppositeSign: ZodiacSign
    get() = entries[(ordinal + 6) % 12]

  companion object {
    fun fromLongitude(longitudeDegrees: Double): ZodiacSign {
      val norm = ((longitudeDegrees % 360.0) + 360.0) % 360.0
      val index = (norm / 30.0).toInt().coerceIn(0, 11)
      return entries[index]
    }

    fun fromMonthDay(month: Int, day: Int): ZodiacSign {
      return when (month) {
        1 -> if (day >= 20) AQUARIUS else CAPRICORN
        2 -> if (day >= 19) PISCES else AQUARIUS
        3 -> if (day >= 21) ARIES else PISCES
        4 -> if (day >= 20) TAURUS else ARIES
        5 -> if (day >= 21) GEMINI else TAURUS
        6 -> if (day >= 21) CANCER else GEMINI
        7 -> if (day >= 23) LEO else CANCER
        8 -> if (day >= 23) VIRGO else LEO
        9 -> if (day >= 23) LIBRA else VIRGO
        10 -> if (day >= 23) SCORPIO else LIBRA
        11 -> if (day >= 22) SAGITTARIUS else SCORPIO
        12 -> if (day >= 22) CAPRICORN else SAGITTARIUS
        else -> ARIES
      }
    }
  }
}

/**
 * Celestial bodies calculated in the Stars natal & transit ephemeris.
 */
enum class CelestialBody(
  val displayName: String,
  val glyph: String,
  val archetype: String,
  val domain: String,
  val orbitalPeriodLabel: String,
  val rulesSigns: List<ZodiacSign>,
  val canRetrograde: Boolean,
  val description: String,
) {
  SUN(
    displayName = "Sun",
    glyph = "☉",
    archetype = "Core Self & Vitality",
    domain = "Identity, conscious purpose, creative life force, and radiance",
    orbitalPeriodLabel = "365.25 days (~30 days per sign)",
    rulesSigns = listOf(ZodiacSign.LEO),
    canRetrograde = false,
    description = "Your Sun sign represents the heart of your identity—who you are becoming, what energizes your spirit, and how you shine.",
  ),
  MOON(
    displayName = "Moon",
    glyph = "☽",
    archetype = "Inner World & Emotions",
    domain = "Instincts, emotional needs, safety, memory, and private self",
    orbitalPeriodLabel = "27.3 days (~2.5 days per sign)",
    rulesSigns = listOf(ZodiacSign.CANCER),
    canRetrograde = false,
    description = "Your Moon sign reveals your emotional landscape—how you process feelings, what makes you feel safe and loved, and your intuitive reflexes.",
  ),
  MERCURY(
    displayName = "Mercury",
    glyph = "☿",
    archetype = "Mind & Communication",
    domain = "Voice, thought patterns, curiosity, humor, and daily connections",
    orbitalPeriodLabel = "88 days (Retrogrades 3–4×/year)",
    rulesSigns = listOf(ZodiacSign.GEMINI, ZodiacSign.VIRGO),
    canRetrograde = true,
    description = "Mercury governs how you think, speak, listen, and learn—shaping your communication style and mental rhythm.",
  ),
  VENUS(
    displayName = "Venus",
    glyph = "♀",
    archetype = "Love, Beauty & Harmony",
    domain = "Romance, affection, aesthetics, pleasure, and what you cherish",
    orbitalPeriodLabel = "225 days (Retrogrades every 18 months)",
    rulesSigns = listOf(ZodiacSign.TAURUS, ZodiacSign.LIBRA),
    canRetrograde = true,
    description = "Venus illuminates how you give and receive affection, what attracts your heart, and your relationship with beauty and pleasure.",
  ),
  MARS(
    displayName = "Mars",
    glyph = "♂",
    archetype = "Drive, Passion & Action",
    domain = "Desire, courage, ambition, physical energy, and how you pursue goals",
    orbitalPeriodLabel = "687 days (Retrogrades every 26 months)",
    rulesSigns = listOf(ZodiacSign.ARIES),
    canRetrograde = true,
    description = "Mars is your inner engine—showing how you assert boundaries, channel passion, and turn desire into bold action.",
  ),
  JUPITER(
    displayName = "Jupiter",
    glyph = "♃",
    archetype = "Abundance, Luck & Wisdom",
    domain = "Growth, optimism, generosity, higher meaning, and good fortune",
    orbitalPeriodLabel = "11.86 years (~1 year per sign)",
    rulesSigns = listOf(ZodiacSign.SAGITTARIUS),
    canRetrograde = true,
    description = "Jupiter shows where life expands for you—bringing synchronicity, mentorship, philosophical breadth, and natural confidence.",
  ),
  SATURN(
    displayName = "Saturn",
    glyph = "♄",
    archetype = "Mastery, Structure & Time",
    domain = "Discipline, life lessons, boundaries, maturity, and lasting legacy",
    orbitalPeriodLabel = "29.46 years (~2.5 years per sign)",
    rulesSigns = listOf(ZodiacSign.CAPRICORN),
    canRetrograde = true,
    description = "Saturn marks where you build deep mastery over time—turning patience and self-discipline into unshakable strength.",
  ),
  URANUS(
    displayName = "Uranus",
    glyph = "♅",
    archetype = "Awakening & Originality",
    domain = "Breakthroughs, innovation, liberation, and sudden insight",
    orbitalPeriodLabel = "84 years (~7 years per sign)",
    rulesSigns = listOf(ZodiacSign.AQUARIUS),
    canRetrograde = true,
    description = "Uranus sparks electricity and authenticity—highlighting where you break old molds and express your most original genius.",
  ),
  NEPTUNE(
    displayName = "Neptune",
    glyph = "♆",
    archetype = "Dreams, Soul & Imagination",
    domain = "Mysticism, art, compassion, enchantment, and spiritual unity",
    orbitalPeriodLabel = "164.8 years (~14 years per sign)",
    rulesSigns = listOf(ZodiacSign.PISCES),
    canRetrograde = true,
    description = "Neptune governs your poetic and spiritual sensitivity—where inspiration, dreams, and unconditional empathy flow.",
  ),
  PLUTO(
    displayName = "Pluto",
    glyph = "♇",
    archetype = "Alchemy & Rebirth",
    domain = "Deep transformation, shadow work, empowerment, and regeneration",
    orbitalPeriodLabel = "248 years (12–31 years per sign)",
    rulesSigns = listOf(ZodiacSign.SCORPIO),
    canRetrograde = true,
    description = "Pluto reveals your deepest reservoir of resilience—where you shed outgrown chapters and rise wiser and stronger.",
  ),
}

/**
 * The 12 Astrological Houses and their life domains.
 */
@Immutable
data class AstrologicalHouseInfo(
  val number: Int,
  val roman: String,
  val title: String,
  val latinName: String,
  val naturalSign: ZodiacSign,
  val theme: String,
  val description: String,
)

val ASTROLOGICAL_HOUSES: List<AstrologicalHouseInfo> = listOf(
  AstrologicalHouseInfo(
    number = 1,
    roman = "I",
    title = "1st House of Self & Ascendant",
    latinName = "Vita",
    naturalSign = ZodiacSign.ARIES,
    theme = "Identity, First Impressions, Presence & Physical Vitality",
    description = "The 1st House (the Ascendant or Rising sign) is the lens through which you greet the world—shaping your aura, instinctive demeanor, and personal beginnings.",
  ),
  AstrologicalHouseInfo(
    number = 2,
    roman = "II",
    title = "2nd House of Value & Resources",
    latinName = "Lucrum",
    naturalSign = ZodiacSign.TAURUS,
    theme = "Self-Worth, Finances, Possessions & Sensory Grounding",
    description = "The 2nd House governs what you value and build—your relationship with material security, personal talents, and everyday pleasures.",
  ),
  AstrologicalHouseInfo(
    number = 3,
    roman = "III",
    title = "3rd House of Mind & Voice",
    latinName = "Fratres",
    naturalSign = ZodiacSign.GEMINI,
    theme = "Communication, Learning, Siblings & Daily Environment",
    description = "The 3rd House illuminates your curious mind, conversational style, writing, local adventures, and kindred connections.",
  ),
  AstrologicalHouseInfo(
    number = 4,
    roman = "IV",
    title = "4th House of Home & Roots",
    latinName = "Genitor",
    naturalSign = ZodiacSign.CANCER,
    theme = "Sanctuary, Family, Ancestry & Emotional Foundation",
    description = "Anchored at the nadir of the chart (the IC), the 4th House represents your private sanctuary, roots, and what feels like true home.",
  ),
  AstrologicalHouseInfo(
    number = 5,
    roman = "V",
    title = "5th House of Joy & Romance",
    latinName = "Nati",
    naturalSign = ZodiacSign.LEO,
    theme = "Creativity, Romance, Play, Artistry & Heartfelt Expression",
    description = "The 5th House is where your heart plays—governing courtship, artistic creation, hobbies, celebration, and pure joy.",
  ),
  AstrologicalHouseInfo(
    number = 6,
    roman = "VI",
    title = "6th House of Ritual & Wellness",
    latinName = "Valetudo",
    naturalSign = ZodiacSign.VIRGO,
    theme = "Daily Rituals, Health, Craft, Devotion & Mind-Body Balance",
    description = "The 6th House shapes your daily rhythms—how you care for your body, refine your skills, and find meaning in helpful service.",
  ),
  AstrologicalHouseInfo(
    number = 7,
    roman = "VII",
    title = "7th House of Partnership",
    latinName = "Uxor",
    naturalSign = ZodiacSign.LIBRA,
    theme = "Committed Love, Marriage, Soul Mirrors & One-on-One Bonds",
    description = "Opening at the Descendant opposite your Rising sign, the 7th House reveals what you seek and attract in devoted partnership.",
  ),
  AstrologicalHouseInfo(
    number = 8,
    roman = "VIII",
    title = "8th House of Intimacy & Alchemy",
    latinName = "Mors",
    naturalSign = ZodiacSign.SCORPIO,
    theme = "Deep Merging, Shared Resources, Mystery & Emotional Rebirth",
    description = "The 8th House governs soul-level intimacy, trust, shared finances, psychological insight, and transformative renewal.",
  ),
  AstrologicalHouseInfo(
    number = 9,
    roman = "IX",
    title = "9th House of Horizons & Wisdom",
    latinName = "Iter",
    naturalSign = ZodiacSign.SAGITTARIUS,
    theme = "Travel, Philosophy, Higher Learning & Spiritual Expansion",
    description = "The 9th House calls you beyond the familiar—inspiring long-distance journeys, big-picture philosophy, and lifelong discovery.",
  ),
  AstrologicalHouseInfo(
    number = 10,
    roman = "X",
    title = "10th House of Purpose & Legacy",
    latinName = "Regnum",
    naturalSign = ZodiacSign.CAPRICORN,
    theme = "Career, Public Calling, Reputation & Long-Term Achievement",
    description = "Crowned by the Midheaven (MC), the 10th House illuminates your highest public calling, vocation, and the legacy you build.",
  ),
  AstrologicalHouseInfo(
    number = 11,
    roman = "XI",
    title = "11th House of Wishes & Community",
    latinName = "Benefacta",
    naturalSign = ZodiacSign.AQUARIUS,
    theme = "Friendships, Collective Vision, Hopes & Future Dreams",
    description = "The 11th House celebrates your chosen community, creative collaborations, humanitarian ideals, and boldest wishes for the future.",
  ),
  AstrologicalHouseInfo(
    number = 12,
    roman = "XII",
    title = "12th House of Dreams & the Unseen",
    latinName = "Carcer",
    naturalSign = ZodiacSign.PISCES,
    theme = "Intuition, Solitude, Subconscious Wisdom & Spiritual Healing",
    description = "The 12th House is the mystic sanctuary of the chart—where dreams, meditation, unspoken intuition, and deep soul healing reside.",
  ),
)

/**
 * Major Ptolemaic planetary aspects.
 */
enum class AspectType(
  val displayName: String,
  val glyph: String,
  val exactAngle: Double,
  val orbDegrees: Double,
  val nature: String,
  val harmonyWeight: Int,
  val summary: String,
) {
  CONJUNCTION(
    displayName = "Conjunction",
    glyph = "☌",
    exactAngle = 0.0,
    orbDegrees = 8.0,
    nature = "Unified Fusion",
    harmonyWeight = 85,
    summary = "Two planets occupy the same degree area of the sky, blending their archetypes into a single concentrated focal point.",
  ),
  SEXTILE(
    displayName = "Sextile",
    glyph = "⚹",
    exactAngle = 60.0,
    orbDegrees = 5.5,
    nature = "Creative Opportunity",
    harmonyWeight = 90,
    summary = "A supportive 60° angle between compatible elements (Fire–Air or Earth–Water) that sparks effortless synergy and creative flow.",
  ),
  SQUARE(
    displayName = "Square",
    glyph = "□",
    exactAngle = 90.0,
    orbDegrees = 6.5,
    nature = "Dynamic Catalyst",
    harmonyWeight = 62,
    summary = "A charged 90° angle of productive tension that pushes growth, decisive action, and magnetic chemistry.",
  ),
  TRINE(
    displayName = "Trine",
    glyph = "△",
    exactAngle = 120.0,
    orbDegrees = 7.5,
    nature = "Natural Harmony",
    harmonyWeight = 96,
    summary = "A golden 120° angle connecting signs of the same element, bringing natural grace, mutual understanding, and luck.",
  ),
  OPPOSITION(
    displayName = "Opposition",
    glyph = "☍",
    exactAngle = 180.0,
    orbDegrees = 7.5,
    nature = "Magnetic Polarity",
    harmonyWeight = 76,
    summary = "A 180° mirror across the zodiac wheel that creates undeniable attraction, perspective, and balance between complementary opposites.",
  ),
}

/**
 * Eight principal lunar phases across the 29.53-day synodic cycle.
 */
enum class LunarPhaseType(
  val displayName: String,
  val emoji: String,
  val guidance: String,
) {
  NEW_MOON(
    displayName = "New Moon",
    emoji = "🌑",
    guidance = "Plant fresh intentions, rest quietly, and envision what you want to grow over the next 28 days.",
  ),
  WAXING_CRESCENT(
    displayName = "Waxing Crescent",
    emoji = "🌒",
    guidance = "Take the first gentle steps toward your intention; gather curiosity, hope, and momentum.",
  ),
  FIRST_QUARTER(
    displayName = "First Quarter",
    emoji = "🌓",
    guidance = "Commit through action—clear small obstacles and make decisive choices that support your path.",
  ),
  WAXING_GIBBOUS(
    displayName = "Waxing Gibbous",
    emoji = "🌔",
    guidance = "Refine, polish, and trust the process as your efforts build toward full illumination.",
  ),
  FULL_MOON(
    displayName = "Full Moon",
    emoji = "🌕",
    guidance = "Culmination, clarity, and celebration—honor how far you've come and release what is complete.",
  ),
  WANING_GIBBOUS(
    displayName = "Waning Gibbous",
    emoji = "🌖",
    guidance = "Share gratitude, wisdom, and warmth with the people you love; integrate recent realizations.",
  ),
  LAST_QUARTER(
    displayName = "Last Quarter",
    emoji = "🌗",
    guidance = "Declutter your schedule and heart; forgive, let go of lingering tension, and restore balance.",
  ),
  WANING_CRESCENT(
    displayName = "Waning Crescent",
    emoji = "🌘",
    guidance = "Balsamic rest and dreamtime—slow down, journal, and recharge before the next New Moon cycle.",
  ),
}

/**
 * Snapshot of the Moon's phase, illumination, zodiac transit, and upcoming Full/New Moon countdowns.
 */
@Immutable
data class MoonPhaseSnapshot(
  val phaseType: LunarPhaseType,
  val phaseAngleDegrees: Double, // 0 = New, 90 = First Quarter, 180 = Full, 270 = Last Quarter
  val illuminationFraction: Double, // 0.0 .. 1.0
  val illuminationPercent: Int,
  val isWaxing: Boolean,
  val lunarAgeDays: Double,
  val moonSign: ZodiacSign,
  val moonDegreeInSign: Int,
  val daysUntilFullMoon: Double,
  val daysUntilNewMoon: Double,
)

/**
 * Calculated position of a single [CelestialBody] at a given instant.
 */
@Immutable
data class PlanetaryPlacement(
  val body: CelestialBody,
  val eclipticLongitude: Double, // 0.0 .. 360.0
  val sign: ZodiacSign,
  val degreeInSign: Int,
  val minuteInSign: Int,
  val house: Int, // 1..12
  val isRetrograde: Boolean,
  val dailySpeedDegrees: Double,
  val interpretation: String,
) {
  val formattedDegree: String
    get() = String.format(Locale.US, "%d°%02d'", degreeInSign, minuteInSign)

  val formattedPlacement: String
    get() = "${sign.glyph} ${sign.displayName} $formattedDegree"
}

/**
 * Calculated angular aspect between two celestial bodies.
 */
@Immutable
data class PlanetaryAspect(
  val firstBody: CelestialBody,
  val firstSign: ZodiacSign,
  val secondBody: CelestialBody,
  val secondSign: ZodiacSign,
  val aspectType: AspectType,
  val exactSeparationDegrees: Double,
  val orbDegrees: Double,
  val interpretation: String,
)

/**
 * Built-in offline birth city coordinate entry for rising/house calculations.
 */
@Immutable
data class BirthCityPreset(
  val id: String,
  val cityName: String,
  val regionOrCountry: String,
  val latitude: Double,
  val longitude: Double,
  val timeZoneId: String,
) {
  val displayLabel: String
    get() = "$cityName, $regionOrCountry"
}

/**
 * User-saved birth chart profile (e.g. "Her Stars", "Partner", friends, family).
 */
@Immutable
data class BirthProfile(
  val id: String,
  val name: String,
  val relationshipTag: String = "Me",
  val birthYear: Int,
  val birthMonth: Int, // 1..12
  val birthDay: Int, // 1..31
  val birthHour: Int = 12, // 0..23
  val birthMinute: Int = 0, // 0..59
  val hasExactBirthTime: Boolean = true,
  val cityName: String = "New York, USA",
  val latitude: Double = 40.7128,
  val longitude: Double = -74.0060,
  val timeZoneId: String = "America/New_York",
  val notes: String = "",
) {
  val formattedBirthDate: String
    get() = String.format(Locale.US, "%04d-%02d-%02d", birthYear, birthMonth, birthDay)

  val formattedBirthTime: String
    get() {
      if (!hasExactBirthTime) return "Time unknown (Noon chart)"
      val amPm = if (birthHour < 12) "AM" else "PM"
      val h12 = when {
        birthHour == 0 -> 12
        birthHour > 12 -> birthHour - 12
        else -> birthHour
      }
      return String.format(Locale.US, "%d:%02d %s", h12, birthMinute, amPm)
    }

  fun toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("name", name)
    put("relationshipTag", relationshipTag)
    put("birthYear", birthYear)
    put("birthMonth", birthMonth)
    put("birthDay", birthDay)
    put("birthHour", birthHour)
    put("birthMinute", birthMinute)
    put("hasExactBirthTime", hasExactBirthTime)
    put("cityName", cityName)
    put("latitude", latitude)
    put("longitude", longitude)
    put("timeZoneId", timeZoneId)
    put("notes", notes)
  }

  companion object {
    fun fromJson(json: JSONObject): BirthProfile {
      return BirthProfile(
        id = json.optString("id", "profile_default").ifBlank { "profile_default" },
        name = json.optString("name", "My Chart").ifBlank { "My Chart" },
        relationshipTag = json.optString("relationshipTag", "Me").ifBlank { "Me" },
        birthYear = json.optInt("birthYear", 1998).coerceIn(1900, 2100),
        birthMonth = json.optInt("birthMonth", 5).coerceIn(1, 12),
        birthDay = json.optInt("birthDay", 15).coerceIn(1, 31),
        birthHour = json.optInt("birthHour", 14).coerceIn(0, 23),
        birthMinute = json.optInt("birthMinute", 30).coerceIn(0, 59),
        hasExactBirthTime = json.optBoolean("hasExactBirthTime", true),
        cityName = json.optString("cityName", "New York, USA").ifBlank { "New York, USA" },
        latitude = json.optDouble("latitude", 40.7128).coerceIn(-89.0, 89.0),
        longitude = json.optDouble("longitude", -74.0060).coerceIn(-180.0, 180.0),
        timeZoneId = json.optString("timeZoneId", "America/New_York").ifBlank { "UTC" },
        notes = json.optString("notes", ""),
      )
    }
  }
}

/**
 * Complete calculated natal chart for a [BirthProfile] (or any moment in time).
 */
@Immutable
data class NatalChartCalculation(
  val profile: BirthProfile,
  val sunSign: ZodiacSign,
  val moonSign: ZodiacSign,
  val risingSign: ZodiacSign,
  val ascendantLongitude: Double,
  val midheavenLongitude: Double,
  val midheavenSign: ZodiacSign,
  val placements: List<PlanetaryPlacement>,
  val aspects: List<PlanetaryAspect>,
  val elementCounts: Map<ZodiacElement, Int>,
  val modalityCounts: Map<ZodiacModality, Int>,
  val dominantElement: ZodiacElement,
  val dominantModality: ZodiacModality,
  val chartRulerBody: CelestialBody,
  val bigThreeSummary: String,
)

/**
 * Daily horoscope and transit reading generated deterministically from real sky transits interacting
 * with a user's [NatalChartCalculation] (or selected [ZodiacSign]).
 */
@Immutable
data class DailyHoroscopeReading(
  val dateFormatted: String,
  val year: Int,
  val month: Int,
  val day: Int,
  val targetSign: ZodiacSign,
  val moonSnapshot: MoonPhaseSnapshot,
  val headlineMantra: String,
  val cosmicVibeWord: String,
  val keyTransitHeadline: String,
  val overviewReading: String,
  val loveReading: String,
  val careerReading: String,
  val wellnessReading: String,
  val dosList: List<String>,
  val dontsList: List<String>,
  val luckyNumber: Int,
  val luckyTimeWindow: String,
  val powerColorName: String,
  val powerColorHex: String,
  val bestMatchSign: ZodiacSign,
  val retrogradeBodies: List<CelestialBody>,
  val isMercuryRetrograde: Boolean,
  val skyPlacements: List<PlanetaryPlacement>,
)

/**
 * Synastry / compatibility analysis between two [NatalChartCalculation]s or two [ZodiacSign]s.
 */
@Immutable
data class SynastryCompatibilityReport(
  val firstLabel: String,
  val firstSun: ZodiacSign,
  val firstMoon: ZodiacSign,
  val firstRising: ZodiacSign,
  val secondLabel: String,
  val secondSun: ZodiacSign,
  val secondMoon: ZodiacSign,
  val secondRising: ZodiacSign,
  val overallScore: Int, // 0..100
  val romanceScore: Int,
  val communicationScore: Int,
  val emotionalScore: Int,
  val valuesScore: Int,
  val bondTitle: String,
  val elementalDynamics: String,
  val summary: String,
  val strengths: List<String>,
  val growthTips: List<String>,
  val crossAspects: List<PlanetaryAspect>,
)

/**
 * Searchable reference entry for the Stars Guide & Encyclopedia (and universal Search tool).
 */
enum class StarsGuideCategory(val label: String) {
  ALL("All"),
  FAVORITES("Favorites"),
  SIGNS("Zodiac Signs"),
  PLANETS("Planets"),
  HOUSES("12 Houses"),
  ASPECTS("Aspects"),
  PHASES("Moon & Elements"),
  CONSTELLATIONS("Constellations"),
}

@Immutable
data class StarsGuideEntry(
  val id: String,
  val title: String,
  val glyph: String,
  val category: StarsGuideCategory,
  val subtitle: String,
  val badgeLabel: String,
  val summary: String,
  val details: List<Pair<String, String>>,
  val powerColorHex: String? = null,
  val keywords: String = "",
)

/**
 * A single named star in a celestial constellation with J2000 equatorial coordinates:
 * Right Ascension ([raHours], `0.0..24.0` hours) and Declination ([decDegrees], `-90.0..+90.0` degrees).
 */
@Immutable
data class ConstellationStarNode(
  val id: String,
  val name: String,
  val raHours: Double,
  val decDegrees: Double,
  val magnitude: Float,
  val roleLabel: String = "",
)

/**
 * A celestial constellation (12 Zodiac constellations + iconic night-sky landmarks like Orion,
 * Ursa Major, Cassiopeia, and Cygnus) with star vertices and stick-figure line connections.
 */
@Immutable
data class SkyConstellation(
  val id: String,
  val name: String,
  val subtitle: String,
  val glyph: String,
  val zodiacSign: ZodiacSign? = null,
  val mythAndLore: String,
  val asterismNote: String,
  val stars: List<ConstellationStarNode>,
  val lines: List<Pair<String, String>>,
) {
  val centerRaHours: Double
    get() = if (stars.isEmpty()) 0.0 else stars.map { it.raHours }.average()

  val centerDecDegrees: Double
    get() = if (stars.isEmpty()) 0.0 else stars.map { it.decDegrees }.average()
}

/**
 * Horizontal sky coordinates (Altitude above horizon `-90°..+90°`, Azimuth compass bearing `0°..360°`
 * where `0° = North`, `90° = East`, `180° = South`, `270° = West`).
 */
@Immutable
data class SkyHorizontalPosition(
  val altitudeDegrees: Double,
  val azimuthDegrees: Double,
)

/**
 * 2D normalized camera viewport point (`normX, normY` in `0..1` when inside the camera frame)
 * projected from a 3D sky position relative to the camera's pointing orientation.
 */
@Immutable
data class ProjectedSkyPoint(
  val normX: Float,
  val normY: Float,
  val altitudeDegrees: Double,
  val azimuthDegrees: Double,
  val angularDistanceFromCenterDeg: Double,
  val isInFrontOfCamera: Boolean,
)

/**
 * A bright star-like point detected by on-device computer vision from a live camera frame.
 */
@Immutable
data class DetectedCameraStarPoint(
  val normX: Float,
  val normY: Float,
  val brightness: Float,
  val radiusScore: Float,
)

/**
 * Result of matching bright points in the live camera frame to a constellation or asterism
 * (such as Orion's Belt — Alnitak, Alnilam, Mintaka — or the active target constellation).
 */
@Immutable
data class StarVisionSnapMatch(
  val constellationId: String,
  val constellationName: String,
  val asterismLabel: String,
  val confidence: Float,
  val snappedStarPositions: Map<String, Pair<Float, Float>>,
  val matchedPoints: List<DetectedCameraStarPoint>,
)

/**
 * Built-in catalog of 16 constellations: all 12 Zodiac constellations plus Orion (with Orion's Belt),
 * Ursa Major (Big Dipper), Cassiopeia (The Queen's W), and Cygnus (Northern Cross).
 */
val SKY_CONSTELLATIONS: List<SkyConstellation> = listOf(
  SkyConstellation(
    id = "orion",
    name = "Orion",
    subtitle = "The Hunter & Orion's Belt",
    glyph = "✦",
    zodiacSign = null,
    mythAndLore = "Towering beside Taurus and Gemini, Orion the Hunter is the most recognizable beacon of the night sky—anchored by the supergiants Betelgeuse and Rigel and bisected by the iconic three-star row of Orion's Belt.",
    asterismNote = "Orion's Belt: Alnitak (ζ Ori), Alnilam (ε Ori), and Mintaka (δ Ori) form a nearly straight 2.7° diagonal line pointing southeast to Sirius and northwest to Aldebaran in Taurus.",
    stars = listOf(
      ConstellationStarNode("betelgeuse", "Betelgeuse", 5.9195, 7.4071, 0.42f, "Left Shoulder"),
      ConstellationStarNode("bellatrix", "Bellatrix", 5.4189, 6.3497, 1.64f, "Right Shoulder"),
      ConstellationStarNode("alnitak", "Alnitak", 5.6793, -1.9426, 1.77f, "Orion's Belt (East)"),
      ConstellationStarNode("alnilam", "Alnilam", 5.6036, -1.2019, 1.69f, "Orion's Belt (Center)"),
      ConstellationStarNode("mintaka", "Mintaka", 5.5334, -0.2991, 2.23f, "Orion's Belt (West)"),
      ConstellationStarNode("saiph", "Saiph", 5.7959, -9.6696, 2.09f, "Left Knee"),
      ConstellationStarNode("rigel", "Rigel", 5.2423, -8.2016, 0.13f, "Right Foot"),
      ConstellationStarNode("meissa", "Meissa", 5.5855, 9.9342, 3.39f, "Head"),
    ),
    lines = listOf(
      "alnitak" to "alnilam",
      "alnilam" to "mintaka",
      "betelgeuse" to "bellatrix",
      "betelgeuse" to "meissa",
      "bellatrix" to "meissa",
      "betelgeuse" to "alnitak",
      "bellatrix" to "mintaka",
      "alnitak" to "saiph",
      "mintaka" to "rigel",
      "saiph" to "rigel",
    ),
  ),
  SkyConstellation(
    id = "aries",
    name = "Aries",
    subtitle = "The Golden Ram",
    glyph = "♈",
    zodiacSign = ZodiacSign.ARIES,
    mythAndLore = "First constellation of the zodiac, led by the bright horn stars Hamal, Sheratan, and Mesarthim.",
    asterismNote = "Look for a crisp curved arc of three stars just west of the Pleiades.",
    stars = listOf(
      ConstellationStarNode("hamal", "Hamal", 2.1196, 23.4624, 2.00f, "Alpha Arietis"),
      ConstellationStarNode("sheratan", "Sheratan", 1.9107, 20.8080, 2.64f, "Beta Arietis"),
      ConstellationStarNode("mesarthim", "Mesarthim", 1.8927, 19.2940, 3.88f, "Gamma Arietis"),
      ConstellationStarNode("41_ari", "Bharani", 2.8331, 27.2605, 3.63f, "41 Arietis"),
    ),
    lines = listOf(
      "41_ari" to "hamal",
      "hamal" to "sheratan",
      "sheratan" to "mesarthim",
    ),
  ),
  SkyConstellation(
    id = "taurus",
    name = "Taurus",
    subtitle = "The Celestial Bull & Hyades V",
    glyph = "♉",
    zodiacSign = ZodiacSign.TAURUS,
    mythAndLore = "Anchored by the fiery orange eye of Aldebaran and crowned by the Pleiades (Seven Sisters), Taurus charges ahead of Orion.",
    asterismNote = "Follow Orion's Belt northwest to find Aldebaran and the V-shaped face of the Bull.",
    stars = listOf(
      ConstellationStarNode("aldebaran", "Aldebaran", 4.5987, 16.5093, 0.86f, "Bull's Fiery Eye"),
      ConstellationStarNode("elnath", "Elnath", 5.4382, 28.6075, 1.65f, "Northern Horn Tip"),
      ConstellationStarNode("tianguan", "Tianguan", 5.6274, 21.1425, 3.00f, "Southern Horn Tip"),
      ConstellationStarNode("prima_hyadum", "Hyadum I", 4.3299, 15.6276, 3.65f, "Snout"),
      ConstellationStarNode("ain", "Ain", 4.4769, 19.1804, 3.53f, "Right Eye"),
      ConstellationStarNode("alcyone", "Alcyone (Pleiades)", 3.7914, 24.1051, 2.87f, "Seven Sisters Cluster"),
    ),
    lines = listOf(
      "tianguan" to "aldebaran",
      "aldebaran" to "prima_hyadum",
      "prima_hyadum" to "ain",
      "ain" to "elnath",
      "ain" to "alcyone",
    ),
  ),
  SkyConstellation(
    id = "gemini",
    name = "Gemini",
    subtitle = "The Immortal Twins (Castor & Pollux)",
    glyph = "♊",
    zodiacSign = ZodiacSign.GEMINI,
    mythAndLore = "Crowned by the bright twin stars Castor and Pollux standing side-by-side northeast of Orion.",
    asterismNote = "Two parallel chains of stars descending from the bright twin heads Castor and Pollux.",
    stars = listOf(
      ConstellationStarNode("pollux", "Pollux", 7.7553, 28.0262, 1.14f, "Immortal Twin Head"),
      ConstellationStarNode("castor", "Castor", 7.5767, 31.8883, 1.58f, "Mortal Twin Head"),
      ConstellationStarNode("alhena", "Alhena", 6.6285, 16.3993, 1.93f, "Pollux's Foot"),
      ConstellationStarNode("tejat", "Tejat", 6.3826, 22.5136, 2.88f, "Castor's Foot"),
      ConstellationStarNode("mebsuta", "Mebsuta", 6.7322, 25.1311, 3.06f, "Castor's Knee"),
      ConstellationStarNode("wasat", "Wasat", 7.3355, 21.9823, 3.53f, "Pollux's Waist"),
    ),
    lines = listOf(
      "castor" to "pollux",
      "castor" to "mebsuta",
      "mebsuta" to "tejat",
      "pollux" to "wasat",
      "wasat" to "alhena",
    ),
  ),
  SkyConstellation(
    id = "cancer",
    name = "Cancer",
    subtitle = "The Crab & Beehive Cluster",
    glyph = "♋",
    zodiacSign = ZodiacSign.CANCER,
    mythAndLore = "Nestled between Gemini and Leo, Cancer shelters the glowing Praesepe (Beehive) star cluster at its heart.",
    asterismNote = "An inverted Y shape surrounding the misty Beehive Cluster (M44).",
    stars = listOf(
      ConstellationStarNode("altarf", "Al Tarf", 8.2752, 9.1855, 3.52f, "Southern Claw"),
      ConstellationStarNode("asellus_australis", "Asellus Australis", 8.7449, 18.1543, 3.94f, "Southern Donkey"),
      ConstellationStarNode("asellus_borealis", "Asellus Borealis", 8.7212, 21.4685, 4.66f, "Northern Donkey"),
      ConstellationStarNode("acubens", "Acubens", 8.9750, 11.8577, 4.25f, "Southeastern Claw"),
      ConstellationStarNode("iota_cnc", "Iota Cancri", 8.7782, 28.7599, 4.02f, "Northern Tip"),
    ),
    lines = listOf(
      "altarf" to "asellus_australis",
      "acubens" to "asellus_australis",
      "asellus_australis" to "asellus_borealis",
      "asellus_borealis" to "iota_cnc",
    ),
  ),
  SkyConstellation(
    id = "leo",
    name = "Leo",
    subtitle = "The Royal Lion & Sickle",
    glyph = "♌",
    zodiacSign = ZodiacSign.LEO,
    mythAndLore = "Anchored by Regulus ('Little King') on the ecliptic and the sweeping backwards question-mark of the Lion's Mane.",
    asterismNote = "The Sickle asterism forms the Lion's head and chest, while Denebola marks his tail.",
    stars = listOf(
      ConstellationStarNode("regulus", "Regulus", 10.1395, 11.9672, 1.35f, "Heart of the Lion"),
      ConstellationStarNode("algieba", "Algieba", 10.3329, 19.8415, 2.08f, "Lion's Mane"),
      ConstellationStarNode("adhAFERA", "Adhafera", 10.2782, 23.4173, 3.44f, "Upper Mane"),
      ConstellationStarNode("rasalas", "Rasalas", 9.8792, 26.0069, 3.88f, "Lion's Head"),
      ConstellationStarNode("zosma", "Zosma", 11.2351, 20.5237, 2.56f, "Lion's Hip"),
      ConstellationStarNode("denebola", "Denebola", 11.8177, 14.5721, 2.14f, "Lion's Tail"),
    ),
    lines = listOf(
      "regulus" to "algieba",
      "algieba" to "adhAFERA",
      "adhAFERA" to "rasalas",
      "algieba" to "zosma",
      "zosma" to "denebola",
      "denebola" to "regulus",
    ),
  ),
  SkyConstellation(
    id = "virgo",
    name = "Virgo",
    subtitle = "The Maiden & Spica",
    glyph = "♍",
    zodiacSign = ZodiacSign.VIRGO,
    mythAndLore = "The second-largest constellation in the sky, illuminated by the brilliant blue-white jewel Spica (the sheaf of wheat).",
    asterismNote = "Arc from the Big Dipper's handle through Arcturus and 'spike to Spica'.",
    stars = listOf(
      ConstellationStarNode("spica", "Spica", 13.4199, -11.1613, 0.97f, "Ear of Wheat"),
      ConstellationStarNode("porrima", "Porrima", 12.6943, -1.4494, 2.74f, "Goddess of Prophecy"),
      ConstellationStarNode("vindemiatrix", "Vindemiatrix", 13.0363, 10.9591, 2.83f, "Grape Gatherer"),
      ConstellationStarNode("auva", "Auva", 12.9266, 3.3975, 3.38f, "Delta Virginis"),
      ConstellationStarNode("ania", "Heze", 13.5782, -0.5959, 3.37f, "Zeta Virginis"),
    ),
    lines = listOf(
      "spica" to "porrima",
      "spica" to "ania",
      "ania" to "porrima",
      "porrima" to "auva",
      "auva" to "vindemiatrix",
    ),
  ),
  SkyConstellation(
    id = "libra",
    name = "Libra",
    subtitle = "The Golden Scales",
    glyph = "♎",
    zodiacSign = ZodiacSign.LIBRA,
    mythAndLore = "The only zodiac constellation representing an sacred instrument of balance rather than a living creature.",
    asterismNote = "A diamond balance beam formed by Zubeneschamali and Zubenelgenubi west of Scorpius.",
    stars = listOf(
      ConstellationStarNode("zubeneschamali", "Zubeneschamali", 15.2834, -9.3829, 2.61f, "Northern Scale"),
      ConstellationStarNode("zubenelgenubi", "Zubenelgenubi", 14.8479, -16.0418, 2.75f, "Southern Scale"),
      ConstellationStarNode("brachium", "Brachium", 15.0681, -25.2821, 3.29f, "Left Scale Pan"),
      ConstellationStarNode("upsilon_lib", "Upsilon Librae", 15.6173, -28.1351, 3.60f, "Right Scale Pan"),
    ),
    lines = listOf(
      "zubenelgenubi" to "zubeneschamali",
      "zubenelgenubi" to "brachium",
      "zubeneschamali" to "upsilon_lib",
      "brachium" to "upsilon_lib",
    ),
  ),
  SkyConstellation(
    id = "scorpius",
    name = "Scorpius",
    subtitle = "The Scorpion & Antares",
    glyph = "♏",
    zodiacSign = ZodiacSign.SCORPIO,
    mythAndLore = "Placed opposite Orion on the celestial sphere, Scorpius glows with the ruby supergiant heart Antares ('Rival of Mars') and a sweeping J-shaped stinger tail.",
    asterismNote = "Three claw stars flank red Antares before curving down to the stinger Shaula.",
    stars = listOf(
      ConstellationStarNode("antares", "Antares", 16.4901, -26.4320, 1.06f, "Heart of the Scorpion"),
      ConstellationStarNode("acrab", "Acrab", 16.0906, -19.8054, 2.62f, "Northern Claw"),
      ConstellationStarNode("dschubba", "Dschubba", 16.0056, -22.6217, 2.32f, "Forehead"),
      ConstellationStarNode("sargas", "Sargas", 17.6219, -42.9978, 1.87f, "Tail Curve"),
      ConstellationStarNode("shaula", "Shaula", 17.5601, -37.1038, 1.62f, "The Stinger"),
    ),
    lines = listOf(
      "acrab" to "dschubba",
      "dschubba" to "antares",
      "antares" to "sargas",
      "sargas" to "shaula",
    ),
  ),
  SkyConstellation(
    id = "sagittarius",
    name = "Sagittarius",
    subtitle = "The Archer & Teapot Asterism",
    glyph = "♐",
    zodiacSign = ZodiacSign.SAGITTARIUS,
    mythAndLore = "Aiming his arrow toward the galactic center of the Milky Way, Sagittarius is easily spotted by its famous Teapot asterism.",
    asterismNote = "The Milky Way rises like steam from the spout of the Sagittarius Teapot.",
    stars = listOf(
      ConstellationStarNode("kaus_australis", "Kaus Australis", 18.4029, -34.3846, 1.85f, "Southern Bow"),
      ConstellationStarNode("nunki", "Nunki", 18.9211, -26.2967, 2.05f, "Teapot Handle"),
      ConstellationStarNode("kaus_media", "Kaus Media", 18.3499, -29.8281, 2.70f, "Center Bow"),
      ConstellationStarNode("kaus_borealis", "Kaus Borealis", 18.4661, -25.4217, 2.81f, "Teapot Lid"),
      ConstellationStarNode("alnasl", "Alnasl", 18.0968, -30.4241, 2.99f, "Arrowhead / Spout"),
    ),
    lines = listOf(
      "alnasl" to "kaus_media",
      "kaus_media" to "kaus_australis",
      "kaus_media" to "kaus_borealis",
      "kaus_borealis" to "nunki",
      "nunki" to "kaus_australis",
    ),
  ),
  SkyConstellation(
    id = "capricornus",
    name = "Capricornus",
    subtitle = "The Mythic Sea-Goat",
    glyph = "♑",
    zodiacSign = ZodiacSign.CAPRICORN,
    mythAndLore = "One of the oldest constellations recorded in Mesopotamian star lore—a wide celestial triangle connecting Deneb Algedi and Algedi.",
    asterismNote = "A winged arrowhead or smile-shaped triangle in the autumn sky.",
    stars = listOf(
      ConstellationStarNode("deneb_algedi", "Deneb Algedi", 21.7840, -16.1273, 2.85f, "Tail of the Goat"),
      ConstellationStarNode("dabih", "Dabih", 20.3501, -14.7815, 3.08f, "Horn of the Goat"),
      ConstellationStarNode("algedi", "Algedi", 20.2946, -12.5085, 3.57f, "Alpha Capricorni"),
      ConstellationStarNode("omega_cap", "Omega Capricorni", 20.8622, -26.9191, 4.11f, "Keel"),
    ),
    lines = listOf(
      "algedi" to "dabih",
      "dabih" to "omega_cap",
      "omega_cap" to "deneb_algedi",
      "deneb_algedi" to "dabih",
    ),
  ),
  SkyConstellation(
    id = "aquarius",
    name = "Aquarius",
    subtitle = "The Water-Bearer & Water Jar",
    glyph = "♒",
    zodiacSign = ZodiacSign.AQUARIUS,
    mythAndLore = "Guided by the lucky stars Sadalmelik ('Luck of the King') and Sadalsuud ('Luckiest of the Lucky'), pouring the celestial stream toward Fomalhaut.",
    asterismNote = "Look for the Y-shaped Water Jar asterism just east of Sadalmelik.",
    stars = listOf(
      ConstellationStarNode("sadalsuud", "Sadalsuud", 21.5260, -5.5712, 2.90f, "Luckiest of the Lucky"),
      ConstellationStarNode("sadalmelik", "Sadalmelik", 22.0964, -0.3199, 2.95f, "Luck of the King"),
      ConstellationStarNode("skat", "Skat", 22.9108, -15.8208, 3.27f, "Water Stream"),
      ConstellationStarNode("sadachbia", "Sadachbia", 22.3609, -1.3874, 3.84f, "Water Jar"),
    ),
    lines = listOf(
      "sadalsuud" to "sadalmelik",
      "sadalmelik" to "sadachbia",
      "sadachbia" to "skat",
    ),
  ),
  SkyConstellation(
    id = "pisces",
    name = "Pisces",
    subtitle = "The Tied Fishes & Circlet",
    glyph = "♓",
    zodiacSign = ZodiacSign.PISCES,
    mythAndLore = "Two celestial fishes bound together at the knot star Alrescha, swimming across the vernal equinox point.",
    asterismNote = "Two long ribbons of stars meeting at a sharp V-shaped knot at Alrescha.",
    stars = listOf(
      ConstellationStarNode("alrescha", "Alrescha", 2.0341, 2.7637, 3.82f, "The Ribbon Knot"),
      ConstellationStarNode("alpherg", "Alpherg", 1.5247, 15.3458, 3.62f, "Northern Ribbon"),
      ConstellationStarNode("fumalsamakah", "Fumalsamakah", 23.0318, 3.8201, 4.48f, "Western Fish"),
      ConstellationStarNode("iota_psc", "Iota Piscium", 23.6655, 5.6263, 4.13f, "Circlet Ring"),
    ),
    lines = listOf(
      "alpherg" to "alrescha",
      "alrescha" to "iota_psc",
      "iota_psc" to "fumalsamakah",
    ),
  ),
  SkyConstellation(
    id = "ursa_major",
    name = "Ursa Major",
    subtitle = "The Great Bear & Big Dipper",
    glyph = "✦",
    zodiacSign = null,
    mythAndLore = "The timeless northern compass—the seven bright stars of the Big Dipper point the way to Polaris, the North Star.",
    asterismNote = "Follow the outer bowl pointers Merak and Dubhe 5× their distance north to Polaris.",
    stars = listOf(
      ConstellationStarNode("dubhe", "Dubhe", 11.0621, 61.7510, 1.79f, "Upper Pointer"),
      ConstellationStarNode("merak", "Merak", 11.0307, 56.3824, 2.37f, "Lower Pointer"),
      ConstellationStarNode("phecda", "Phecda", 11.8972, 53.6948, 2.44f, "Bowl Base"),
      ConstellationStarNode("megrez", "Megrez", 12.2570, 57.0326, 3.31f, "Bowl Join"),
      ConstellationStarNode("alioth", "Alioth", 12.9005, 55.9598, 1.77f, "Inner Handle"),
      ConstellationStarNode("mizar", "Mizar", 13.3987, 54.9254, 2.23f, "Middle Handle"),
      ConstellationStarNode("alkaid", "Alkaid", 13.7923, 49.3133, 1.86f, "Handle Tip"),
    ),
    lines = listOf(
      "dubhe" to "merak",
      "merak" to "phecda",
      "phecda" to "megrez",
      "megrez" to "dubhe",
      "megrez" to "alioth",
      "alioth" to "mizar",
      "mizar" to "alkaid",
    ),
  ),
  SkyConstellation(
    id = "cassiopeia",
    name = "Cassiopeia",
    subtitle = "The Queen's Celestial W",
    glyph = "✦",
    zodiacSign = null,
    mythAndLore = "Circumpolar across northern latitudes, Cassiopeia forms an unmistakable five-star W or M embedded in the rich Milky Way.",
    asterismNote = "Five bright stars of 2nd magnitude arranged in a crisp zigzag W.",
    stars = listOf(
      ConstellationStarNode("caph", "Caph", 0.1529, 59.1498, 2.28f, "West Tip"),
      ConstellationStarNode("schedar", "Schedar", 0.6751, 56.5373, 2.24f, "Queen's Breast"),
      ConstellationStarNode("gamma_cas", "Nave / Tsih", 0.9451, 60.7167, 2.15f, "Center Peak"),
      ConstellationStarNode("ruchbah", "Ruchbah", 1.4303, 60.2353, 2.68f, "Knee"),
      ConstellationStarNode("segin", "Segin", 1.9066, 63.6701, 3.35f, "East Tip"),
    ),
    lines = listOf(
      "caph" to "schedar",
      "schedar" to "gamma_cas",
      "gamma_cas" to "ruchbah",
      "ruchbah" to "segin",
    ),
  ),
  SkyConstellation(
    id = "cygnus",
    name = "Cygnus",
    subtitle = "The Swan & Northern Cross",
    glyph = "✦",
    zodiacSign = null,
    mythAndLore = "Soaring down the summer Milky Way, Cygnus is crowned by the supergiant Deneb (one vertex of the Summer Triangle) and the colorful double star Albireo at the Swan's beak.",
    asterismNote = "Forms the grand Northern Cross along the plane of the Milky Way.",
    stars = listOf(
      ConstellationStarNode("deneb", "Deneb", 20.6905, 45.2803, 1.25f, "Swan's Tail"),
      ConstellationStarNode("sadr", "Sadr", 20.3705, 40.2567, 2.23f, "Swan's Heart"),
      ConstellationStarNode("albireo", "Albireo", 19.5120, 27.9597, 3.05f, "Swan's Beak"),
      ConstellationStarNode("gienah", "Gienah", 20.7702, 33.9703, 2.48f, "East Wing"),
      ConstellationStarNode("fawaris", "Fawaris", 19.7496, 45.1308, 2.87f, "West Wing"),
    ),
    lines = listOf(
      "deneb" to "sadr",
      "sadr" to "albireo",
      "fawaris" to "sadr",
      "sadr" to "gienah",
    ),
  ),
)

/**
 * Offline city coordinates catalog covering major global timezones for Ascendant & Midheaven calculation.
 */
val BUILTIN_BIRTH_CITIES: List<BirthCityPreset> = listOf(
  BirthCityPreset("nyc", "New York", "NY, USA", 40.7128, -74.0060, "America/New_York"),
  BirthCityPreset("lax", "Los Angeles", "CA, USA", 34.0522, -118.2437, "America/Los_Angeles"),
  BirthCityPreset("chi", "Chicago", "IL, USA", 41.8781, -87.6298, "America/Chicago"),
  BirthCityPreset("aus", "Austin", "TX, USA", 30.2672, -97.7431, "America/Chicago"),
  BirthCityPreset("mia", "Miami", "FL, USA", 25.7617, -80.1918, "America/New_York"),
  BirthCityPreset("sea", "Seattle", "WA, USA", 47.6062, -122.3321, "America/Los_Angeles"),
  BirthCityPreset("sfo", "San Francisco", "CA, USA", 37.7749, -122.4194, "America/Los_Angeles"),
  BirthCityPreset("den", "Denver", "CO, USA", 39.7392, -104.9903, "America/Denver"),
  BirthCityPreset("phx", "Phoenix", "AZ, USA", 33.4484, -112.0740, "America/Phoenix"),
  BirthCityPreset("bos", "Boston", "MA, USA", 42.3601, -71.0589, "America/New_York"),
  BirthCityPreset("atl", "Atlanta", "GA, USA", 33.7490, -84.3880, "America/New_York"),
  BirthCityPreset("hnl", "Honolulu", "HI, USA", 21.3069, -157.8583, "Pacific/Honolulu"),
  BirthCityPreset("tor", "Toronto", "Canada", 43.6532, -79.3832, "America/Toronto"),
  BirthCityPreset("van", "Vancouver", "Canada", 49.2827, -123.1207, "America/Vancouver"),
  BirthCityPreset("mex", "Mexico City", "Mexico", 19.4326, -99.1332, "America/Mexico_City"),
  BirthCityPreset("lon", "London", "United Kingdom", 51.5074, -0.1278, "Europe/London"),
  BirthCityPreset("par", "Paris", "France", 48.8566, 2.3522, "Europe/Paris"),
  BirthCityPreset("ber", "Berlin", "Germany", 52.5200, 13.4050, "Europe/Berlin"),
  BirthCityPreset("rom", "Rome", "Italy", 41.9028, 12.4964, "Europe/Rome"),
  BirthCityPreset("mad", "Madrid", "Spain", 40.4168, -3.7038, "Europe/Madrid"),
  BirthCityPreset("ams", "Amsterdam", "Netherlands", 52.3676, 4.9041, "Europe/Amsterdam"),
  BirthCityPreset("dub", "Dublin", "Ireland", 53.3498, -6.2603, "Europe/Dublin"),
  BirthCityPreset("ath", "Athens", "Greece", 37.9838, 23.7275, "Europe/Athens"),
  BirthCityPreset("tok", "Tokyo", "Japan", 35.6762, 139.6503, "Asia/Tokyo"),
  BirthCityPreset("seo", "Seoul", "South Korea", 37.5665, 126.9780, "Asia/Seoul"),
  BirthCityPreset("syd", "Sydney", "Australia", -33.8688, 151.2093, "Australia/Sydney"),
  BirthCityPreset("mel", "Melbourne", "Australia", -37.8136, 144.9631, "Australia/Melbourne"),
  BirthCityPreset("akl", "Auckland", "New Zealand", -36.8485, 174.7633, "Pacific/Auckland"),
  BirthCityPreset("sin", "Singapore", "Singapore", 1.3521, 103.8198, "Asia/Singapore"),
  BirthCityPreset("bom", "Mumbai", "India", 19.0760, 72.8777, "Asia/Kolkata"),
  BirthCityPreset("dxb", "Dubai", "UAE", 25.2048, 55.2708, "Asia/Dubai"),
  BirthCityPreset("sao", "São Paulo", "Brazil", -23.5505, -46.6333, "America/Sao_Paulo"),
  BirthCityPreset("bue", "Buenos Aires", "Argentina", -34.6037, -58.3816, "America/Argentina/Buenos_Aires"),
  BirthCityPreset("cpt", "Cape Town", "South Africa", -33.9249, 18.4241, "Africa/Johannesburg"),
)

/**
 * Default starter profiles so the user and her partner can explore charts and compatibility immediately.
 */
val DEFAULT_BIRTH_PROFILES: List<BirthProfile> = listOf(
  BirthProfile(
    id = "profile_her_stars",
    name = "My Chart",
    relationshipTag = "Me",
    birthYear = 1999,
    birthMonth = 5,
    birthDay = 14,
    birthHour = 16,
    birthMinute = 20,
    hasExactBirthTime = true,
    cityName = "Chicago, IL, USA",
    latitude = 41.8781,
    longitude = -87.6298,
    timeZoneId = "America/Chicago",
    notes = "Primary natal chart profile",
  ),
  BirthProfile(
    id = "profile_partner_stars",
    name = "Partner",
    relationshipTag = "Partner",
    birthYear = 1998,
    birthMonth = 10,
    birthDay = 28,
    birthHour = 9,
    birthMinute = 45,
    hasExactBirthTime = true,
    cityName = "Chicago, IL, USA",
    latitude = 41.8781,
    longitude = -87.6298,
    timeZoneId = "America/Chicago",
    notes = "Partner synastry profile",
  ),
)

/**
 * Repository interface for persisting birth profiles, active chart selection, and favorite guide items.
 */
interface StarsRepository {
  fun loadProfiles(): List<BirthProfile>
  fun saveProfiles(profiles: List<BirthProfile>)
  fun getActiveProfileId(): String
  fun setActiveProfileId(profileId: String)
  fun getMatchPartnerProfileId(): String
  fun setMatchPartnerProfileId(profileId: String)
  fun loadFavoriteGuideIds(): Set<String>
  fun saveFavoriteGuideIds(ids: Set<String>)
  fun toggleFavoriteGuideId(id: String): Set<String>
}

class InMemoryStarsRepository(
  initialProfiles: List<BirthProfile> = DEFAULT_BIRTH_PROFILES,
  initialActiveId: String = initialProfiles.firstOrNull()?.id ?: "profile_her_stars",
  initialPartnerId: String = initialProfiles.getOrNull(1)?.id ?: initialActiveId,
  initialFavorites: Set<String> = setOf("sign_TAURUS", "sign_SCORPIO", "planet_MOON", "planet_VENUS"),
) : StarsRepository {
  private var profiles: List<BirthProfile> = initialProfiles.ifEmpty { DEFAULT_BIRTH_PROFILES }
  private var activeId: String = initialActiveId
  private var partnerId: String = initialPartnerId
  private var favorites: Set<String> = initialFavorites

  override fun loadProfiles(): List<BirthProfile> = profiles

  override fun saveProfiles(profiles: List<BirthProfile>) {
    this.profiles = profiles.ifEmpty { DEFAULT_BIRTH_PROFILES }
    if (this.profiles.none { it.id == activeId }) {
      activeId = this.profiles.first().id
    }
    val active = this.profiles.firstOrNull { it.id == activeId } ?: this.profiles.first()
    StarsEngine.updateActiveSign(StarsEngine.calculateNatalChart(active).sunSign)
  }

  override fun getActiveProfileId(): String {
    return profiles.firstOrNull { it.id == activeId }?.id ?: profiles.first().id
  }

  override fun setActiveProfileId(profileId: String) {
    activeId = profileId
    val active = profiles.firstOrNull { it.id == activeId } ?: profiles.first()
    StarsEngine.updateActiveSign(StarsEngine.calculateNatalChart(active).sunSign)
  }

  override fun getMatchPartnerProfileId(): String {
    return profiles.firstOrNull { it.id == partnerId }?.id
      ?: profiles.getOrNull(1)?.id
      ?: profiles.first().id
  }

  override fun setMatchPartnerProfileId(profileId: String) {
    partnerId = profileId
  }

  override fun loadFavoriteGuideIds(): Set<String> = favorites

  override fun saveFavoriteGuideIds(ids: Set<String>) {
    favorites = ids
  }

  override fun toggleFavoriteGuideId(id: String): Set<String> {
    favorites = if (id in favorites) favorites - id else favorites + id
    return favorites
  }
}

class SharedPreferencesStarsRepository(
  private val prefs: SharedPreferences,
) : StarsRepository {

  constructor(context: Context) : this(
    context.getSharedPreferences(STARS_PREFS_NAME, Context.MODE_PRIVATE)
  )

  override fun loadProfiles(): List<BirthProfile> {
    val raw = prefs.getString(PREF_KEY_STARS_PROFILES_JSON, null)
    if (raw.isNullOrBlank()) return DEFAULT_BIRTH_PROFILES
    return try {
      val arr = JSONArray(raw)
      val list = mutableListOf<BirthProfile>()
      for (i in 0 until arr.length()) {
        val obj = arr.optJSONObject(i) ?: continue
        list.add(BirthProfile.fromJson(obj))
      }
      list.ifEmpty { DEFAULT_BIRTH_PROFILES }
    } catch (_: Exception) {
      DEFAULT_BIRTH_PROFILES
    }
  }

  override fun saveProfiles(profiles: List<BirthProfile>) {
    val nonEmpty = profiles.ifEmpty { DEFAULT_BIRTH_PROFILES }
    val arr = JSONArray()
    nonEmpty.forEach { arr.put(it.toJson()) }
    prefs.edit().putString(PREF_KEY_STARS_PROFILES_JSON, arr.toString()).apply()
    val activeId = getActiveProfileId()
    val active = nonEmpty.firstOrNull { it.id == activeId } ?: nonEmpty.first()
    StarsEngine.updateActiveSign(StarsEngine.calculateNatalChart(active).sunSign)
  }

  override fun getActiveProfileId(): String {
    val all = loadProfiles()
    val saved = prefs.getString(PREF_KEY_STARS_ACTIVE_PROFILE_ID, null)
    return all.firstOrNull { it.id == saved }?.id ?: all.first().id
  }

  override fun setActiveProfileId(profileId: String) {
    prefs.edit().putString(PREF_KEY_STARS_ACTIVE_PROFILE_ID, profileId).apply()
    val all = loadProfiles()
    val active = all.firstOrNull { it.id == profileId } ?: all.first()
    StarsEngine.updateActiveSign(StarsEngine.calculateNatalChart(active).sunSign)
  }

  override fun getMatchPartnerProfileId(): String {
    val all = loadProfiles()
    val saved = prefs.getString(PREF_KEY_STARS_MATCH_PARTNER_ID, null)
    return all.firstOrNull { it.id == saved }?.id
      ?: all.getOrNull(1)?.id
      ?: all.first().id
  }

  override fun setMatchPartnerProfileId(profileId: String) {
    prefs.edit().putString(PREF_KEY_STARS_MATCH_PARTNER_ID, profileId).apply()
  }

  override fun loadFavoriteGuideIds(): Set<String> {
    val saved = prefs.getStringSet(PREF_KEY_STARS_FAVORITE_IDS, null)
    return saved?.toSet() ?: setOf("sign_TAURUS", "sign_SCORPIO", "planet_MOON", "planet_VENUS")
  }

  override fun saveFavoriteGuideIds(ids: Set<String>) {
    prefs.edit().putStringSet(PREF_KEY_STARS_FAVORITE_IDS, ids).apply()
  }

  override fun toggleFavoriteGuideId(id: String): Set<String> {
    val current = loadFavoriteGuideIds()
    val updated = if (id in current) current - id else current + id
    saveFavoriteGuideIds(updated)
    return updated
  }
}

/**
 * Core astronomical & astrological calculation engine.
 *
 * Computes geocentric ecliptic longitudes for the Sun, Moon, and Planets (Mercury through Pluto),
 * apparent retrograde motion, Local Sidereal Time (LST), Ascendant (Rising Sign), Midheaven (MC),
 * Whole-Sign / Equal house placements, Ptolemaic aspects, lunar phase illumination, synastry scores,
 * and deterministic transit-driven daily horoscopes—100% offline.
 */
object StarsEngine {

  private const val DEG_TO_RAD = PI / 180.0
  private const val RAD_TO_DEG = 180.0 / PI
  private const val SYNODIC_MONTH_DAYS = 29.530588853

  private val _pendingWidgetTabFlow = MutableStateFlow<String?>(null)
  val pendingWidgetTabFlow: StateFlow<String?> = _pendingWidgetTabFlow.asStateFlow()

  /**
   * Observable current sign from the Stars tool so [com.example.toolbox.ui.theme.ToolboxTheme]
   * and Settings can dynamically theme the application according to the active sign's power color.
   */
  var activeSignState by mutableStateOf(ZodiacSign.TAURUS)
    private set

  fun updateActiveSign(sign: ZodiacSign) {
    activeSignState = sign
  }

  /**
   * Resolves the current [ZodiacSign] from the Stars tool (checking any widget/today sign override
   * first, then the active birth profile's Sun sign, and updating [activeSignState]).
   */
  fun resolveCurrentSign(
    context: Context? = null,
    repository: StarsRepository? = null,
  ): ZodiacSign {
    val overrideSign = StarsWidgets.loadOverrideSign(context)
    if (overrideSign != null) {
      activeSignState = overrideSign
      return overrideSign
    }
    val repo = repository ?: context?.let {
      try {
        SharedPreferencesStarsRepository(it)
      } catch (_: Throwable) {
        null
      }
    }
    if (repo != null) {
      val profiles = repo.loadProfiles()
      val activeId = repo.getActiveProfileId()
      val active = profiles.firstOrNull { it.id == activeId } ?: profiles.firstOrNull()
      if (active != null) {
        val sign = calculateNatalChart(active).sunSign
        activeSignState = sign
        return sign
      }
    }
    return activeSignState
  }

  fun requestWidgetTab(tabName: String?) {
    _pendingWidgetTabFlow.value = tabName
  }

  fun consumePendingWidgetTab(): String? {
    val value = _pendingWidgetTabFlow.value
    _pendingWidgetTabFlow.value = null
    return value
  }

  fun normalizeDegrees(deg: Double): Double {
    val rem = deg % 360.0
    return if (rem < 0.0) rem + 360.0 else rem
  }

  fun angularSeparation(lon1: Double, lon2: Double): Double {
    val diff = abs(normalizeDegrees(lon1) - normalizeDegrees(lon2))
    return if (diff > 180.0) 360.0 - diff else diff
  }

  /**
   * Converts a calendar date/time in a specified [timeZoneId] to Julian Day (JD).
   */
  fun toJulianDay(
    year: Int,
    month: Int,
    day: Int,
    hour: Int = 12,
    minute: Int = 0,
    timeZoneId: String = "UTC",
  ): Double {
    val tz = try {
      TimeZone.getTimeZone(timeZoneId)
    } catch (_: Exception) {
      TimeZone.getTimeZone("UTC")
    }
    val cal = Calendar.getInstance(tz, Locale.US).apply {
      set(Calendar.YEAR, year)
      set(Calendar.MONTH, (month - 1).coerceIn(0, 11))
      set(Calendar.DAY_OF_MONTH, day.coerceIn(1, 31))
      set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
      set(Calendar.MINUTE, minute.coerceIn(0, 59))
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val utcMillis = cal.timeInMillis
    return (utcMillis / 86_400_000.0) + 2440587.5
  }

  /**
   * Computes geocentric ecliptic longitude (in degrees `0..360`) for [body] at Julian Day [jd]
   * using keplerian orbital elements referenced to J2000.0 (`JD 2451545.0`).
   */
  fun computeEclipticLongitude(body: CelestialBody, jd: Double): Double {
    val d = jd - 2451545.0 // Days since J2000.0
    val t = d / 36525.0 // Julian centuries since J2000.0

    // Sun's geocentric ecliptic longitude & Earth's heliocentric position
    val sunMeanLon = normalizeDegrees(280.46646 + 0.98564736 * d)
    val sunMeanAnom = normalizeDegrees(357.52911 + 0.98560028 * d)
    val sunMrad = sunMeanAnom * DEG_TO_RAD
    val sunEqCenter = (1.914602 - 0.004817 * t) * sin(sunMrad) +
      0.019993 * sin(2 * sunMrad) +
      0.000289 * sin(3 * sunMrad)
    val sunTrueLon = normalizeDegrees(sunMeanLon + sunEqCenter)
    val sunRadius = 1.00014 - 0.01671 * cos(sunMrad) - 0.00014 * cos(2 * sunMrad)

    if (body == CelestialBody.SUN) {
      return sunTrueLon
    }

    if (body == CelestialBody.MOON) {
      // High-precision lunar mean elements + principal perturbations (Evection, Variation, Annual Equation)
      val lPrime = normalizeDegrees(218.3164477 + 13.17639648 * d)
      val dElong = normalizeDegrees(297.8501921 + 12.19074912 * d)
      val mSun = sunMeanAnom
      val mMoon = normalizeDegrees(134.9633964 + 13.06499295 * d)
      val fArg = normalizeDegrees(93.2720950 + 13.22935024 * d)

      val lon = lPrime +
        6.288774 * sin(mMoon * DEG_TO_RAD) +
        1.274027 * sin((2 * dElong - mMoon) * DEG_TO_RAD) +
        0.658314 * sin(2 * dElong * DEG_TO_RAD) +
        0.213618 * sin(2 * mMoon * DEG_TO_RAD) -
        0.185116 * sin(mSun * DEG_TO_RAD) -
        0.114332 * sin(2 * fArg * DEG_TO_RAD) +
        0.058793 * sin((2 * dElong - 2 * mMoon) * DEG_TO_RAD) +
        0.057066 * sin((2 * dElong - mSun - mMoon) * DEG_TO_RAD) +
        0.053322 * sin((2 * dElong + mMoon) * DEG_TO_RAD)
      return normalizeDegrees(lon)
    }

    // Heliocentric Keplerian elements for planets: (L0, dL_per_day, a_AU, e, perihelion_w)
    data class OrbitalElements(
      val meanLon0: Double,
      val meanMotionPerDay: Double,
      val semiMajorAxis: Double,
      val eccentricity: Double,
      val perihelionLon: Double,
    )

    val elem = when (body) {
      CelestialBody.MERCURY -> OrbitalElements(252.2509, 4.09233445, 0.387098, 0.205635, 77.4561)
      CelestialBody.VENUS -> OrbitalElements(181.9798, 1.60213034, 0.723332, 0.006772, 131.5637)
      CelestialBody.MARS -> OrbitalElements(355.4330, 0.52403900, 1.523679, 0.093401, 336.0602)
      CelestialBody.JUPITER -> OrbitalElements(34.3515, 0.08308676, 5.20260, 0.048498, 14.3312)
      CelestialBody.SATURN -> OrbitalElements(50.0774, 0.03345965, 9.55491, 0.055546, 93.0572)
      CelestialBody.URANUS -> OrbitalElements(314.0550, 0.01173129, 19.2184, 0.046381, 173.0053)
      CelestialBody.NEPTUNE -> OrbitalElements(304.3487, 0.00598103, 30.1104, 0.009456, 48.1203)
      CelestialBody.PLUTO -> OrbitalElements(238.9290, 0.00396400, 39.4821, 0.248827, 224.0689)
      CelestialBody.SUN, CelestialBody.MOON -> OrbitalElements(280.4665, 0.98564736, 1.0, 0.01671, 102.9373)
    }

    val meanLon = normalizeDegrees(elem.meanLon0 + elem.meanMotionPerDay * d)
    val meanAnom = normalizeDegrees(meanLon - elem.perihelionLon)
    val mRad = meanAnom * DEG_TO_RAD
    val e = elem.eccentricity

    // Solve Kepler's equation E - e*sin(E) = M iteratively
    var eccAnom = mRad + e * sin(mRad) * (1.0 + e * cos(mRad))
    for (iter in 0 until 4) {
      val delta = (eccAnom - e * sin(eccAnom) - mRad) / (1.0 - e * cos(eccAnom))
      eccAnom -= delta
    }

    val xv = elem.semiMajorAxis * (cos(eccAnom) - e)
    val yv = elem.semiMajorAxis * ( kotlin.math.sqrt(1.0 - e * e) * sin(eccAnom) )
    val trueAnom = atan2(yv, xv) * RAD_TO_DEG
    val radius = kotlin.math.sqrt(xv * xv + yv * yv)
    val helioLon = normalizeDegrees(trueAnom + elem.perihelionLon)

    // Convert heliocentric planet position + geocentric Sun position to geocentric planet ecliptic longitude
    val helioRad = helioLon * DEG_TO_RAD
    val sunRad = sunTrueLon * DEG_TO_RAD
    val geoX = radius * cos(helioRad) + sunRadius * cos(sunRad)
    val geoY = radius * sin(helioRad) + sunRadius * sin(sunRad)

    return normalizeDegrees(atan2(geoY, geoX) * RAD_TO_DEG)
  }

  /**
   * Computes daily apparent angular speed (degrees/day). Negative values indicate apparent retrograde motion.
   */
  fun computeDailySpeedDegrees(body: CelestialBody, jd: Double): Double {
    val lonBefore = computeEclipticLongitude(body, jd - 0.5)
    val lonAfter = computeEclipticLongitude(body, jd + 0.5)
    var diff = lonAfter - lonBefore
    if (diff > 180.0) diff -= 360.0
    if (diff < -180.0) diff += 360.0
    return diff
  }

  /**
   * Computes the Ascendant (Rising Sign longitude) and Midheaven (MC longitude) for a given
   * Julian Day [jd] and geographic [latitude] / [longitude].
   */
  fun computeAngles(
    jd: Double,
    latitude: Double,
    longitude: Double,
  ): Pair<Double, Double> {
    val d = jd - 2451545.0
    val t = d / 36525.0
    // Greenwich Mean Sidereal Time in degrees
    val gmst = normalizeDegrees(
      280.46061837 + 360.98564736629 * d + 0.000387933 * t * t
    )
    // Local Sidereal Time (RAMC) in degrees
    val lst = normalizeDegrees(gmst + longitude)
    val lstRad = lst * DEG_TO_RAD
    val epsRad = (23.439291 - 0.0130042 * t) * DEG_TO_RAD
    val latRad = latitude.coerceIn(-66.0, 66.0) * DEG_TO_RAD

    // Midheaven (MC)
    val mcRad = atan2(sin(lstRad), cos(lstRad) * cos(epsRad))
    val mcLon = normalizeDegrees(mcRad * RAD_TO_DEG)

    // Ascendant
    val y = cos(lstRad)
    val x = -(sin(lstRad) * cos(epsRad) + tan(latRad) * sin(epsRad))
    val ascLon = normalizeDegrees(atan2(y, x) * RAD_TO_DEG)

    return ascLon to mcLon
  }

  /**
   * Computes the Moon Phase snapshot for a given Julian Day [jd].
   */
  fun computeMoonPhase(jd: Double): MoonPhaseSnapshot {
    val sunLon = computeEclipticLongitude(CelestialBody.SUN, jd)
    val moonLon = computeEclipticLongitude(CelestialBody.MOON, jd)
    val phaseAngle = normalizeDegrees(moonLon - sunLon) // 0..360
    val phaseRad = phaseAngle * DEG_TO_RAD
    val illuminationFraction = ((1.0 - cos(phaseRad)) / 2.0).coerceIn(0.0, 1.0)
    val illuminationPercent = (illuminationFraction * 100.0).roundToInt().coerceIn(0, 100)
    val isWaxing = phaseAngle < 180.0
    val lunarAgeDays = (phaseAngle / 360.0) * SYNODIC_MONTH_DAYS

    val phaseType = when {
      phaseAngle < 22.5 || phaseAngle >= 337.5 -> LunarPhaseType.NEW_MOON
      phaseAngle < 67.5 -> LunarPhaseType.WAXING_CRESCENT
      phaseAngle < 112.5 -> LunarPhaseType.FIRST_QUARTER
      phaseAngle < 157.5 -> LunarPhaseType.WAXING_GIBBOUS
      phaseAngle < 202.5 -> LunarPhaseType.FULL_MOON
      phaseAngle < 247.5 -> LunarPhaseType.WANING_GIBBOUS
      phaseAngle < 292.5 -> LunarPhaseType.LAST_QUARTER
      else -> LunarPhaseType.WANING_CRESCENT
    }

    val moonSign = ZodiacSign.fromLongitude(moonLon)
    val moonDegreeInSign = (moonLon % 30.0).toInt().coerceIn(0, 29)

    val degToFull = normalizeDegrees(180.0 - phaseAngle)
    val degToNew = normalizeDegrees(360.0 - phaseAngle)
    val daysUntilFull = (degToFull / 360.0) * SYNODIC_MONTH_DAYS
    val daysUntilNew = (degToNew / 360.0) * SYNODIC_MONTH_DAYS

    return MoonPhaseSnapshot(
      phaseType = phaseType,
      phaseAngleDegrees = phaseAngle,
      illuminationFraction = illuminationFraction,
      illuminationPercent = illuminationPercent,
      isWaxing = isWaxing,
      lunarAgeDays = lunarAgeDays,
      moonSign = moonSign,
      moonDegreeInSign = moonDegreeInSign,
      daysUntilFullMoon = daysUntilFull,
      daysUntilNewMoon = daysUntilNew,
    )
  }

  /**
   * Builds a rich human-readable interpretation for a [CelestialBody] placed in [sign] and [house].
   */
  fun buildPlacementInterpretation(
    body: CelestialBody,
    sign: ZodiacSign,
    house: Int,
    isRetrograde: Boolean,
  ): String {
    val houseInfo = ASTROLOGICAL_HOUSES.getOrNull((house - 1).coerceIn(0, 11)) ?: ASTROLOGICAL_HOUSES.first()
    val keywords = sign.keywords.take(2).joinToString(" & ").lowercase(Locale.US)
    val retroNote = if (isRetrograde) {
      " Because ${body.displayName} is retrograde here, this energy turns inward with deep reflection and originality."
    } else {
      ""
    }
    return "${body.displayName} in ${sign.displayName} infuses your ${body.archetype.lowercase(Locale.US)} with $keywords ${sign.element.label.lowercase(Locale.US)} energy. Placed in the ${houseInfo.title} (${houseInfo.theme}), it highlights how you express ${sign.motto.lowercase(Locale.US)} in this sphere of life.$retroNote"
  }

  /**
   * Calculates major Ptolemaic aspects across a list of [PlanetaryPlacement]s.
   */
  fun computeAspects(placements: List<PlanetaryPlacement>): List<PlanetaryAspect> {
    val results = mutableListOf<PlanetaryAspect>()
    for (i in placements.indices) {
      for (j in i + 1 until placements.size) {
        val p1 = placements[i]
        val p2 = placements[j]
        val sep = angularSeparation(p1.eclipticLongitude, p2.eclipticLongitude)
        val matchedAspect = AspectType.entries.firstOrNull { aspect ->
          abs(sep - aspect.exactAngle) <= aspect.orbDegrees
        } ?: continue
        val orb = abs(sep - matchedAspect.exactAngle)
        val interp = "${p1.body.displayName} in ${p1.sign.displayName} forms a ${matchedAspect.displayName.lowercase(Locale.US)} (${matchedAspect.glyph}) with ${p2.body.displayName} in ${p2.sign.displayName} (${String.format(Locale.US, "%.1f°", orb)} orb) — ${matchedAspect.nature.lowercase(Locale.US)} uniting ${p1.body.archetype.lowercase(Locale.US)} and ${p2.body.archetype.lowercase(Locale.US)}."
        results.add(
          PlanetaryAspect(
            firstBody = p1.body,
            firstSign = p1.sign,
            secondBody = p2.body,
            secondSign = p2.sign,
            aspectType = matchedAspect,
            exactSeparationDegrees = sep,
            orbDegrees = orb,
            interpretation = interp,
          )
        )
      }
    }
    return results.sortedBy { it.orbDegrees }
  }

  /**
   * Computes a full [NatalChartCalculation] for [profile].
   */
  fun calculateNatalChart(profile: BirthProfile): NatalChartCalculation {
    val effectiveHour = if (profile.hasExactBirthTime) profile.birthHour else 12
    val effectiveMinute = if (profile.hasExactBirthTime) profile.birthMinute else 0
    val jd = toJulianDay(
      year = profile.birthYear,
      month = profile.birthMonth,
      day = profile.birthDay,
      hour = effectiveHour,
      minute = effectiveMinute,
      timeZoneId = profile.timeZoneId,
    )

    val (ascLon, mcLon) = computeAngles(
      jd = jd,
      latitude = profile.latitude,
      longitude = profile.longitude,
    )
    val risingSign = ZodiacSign.fromLongitude(ascLon)
    val mcSign = ZodiacSign.fromLongitude(mcLon)

    val placements = CelestialBody.entries.map { body ->
      val lon = computeEclipticLongitude(body, jd)
      val sign = ZodiacSign.fromLongitude(lon)
      val inSignDegTotal = lon % 30.0
      val deg = inSignDegTotal.toInt().coerceIn(0, 29)
      val min = ((inSignDegTotal - deg) * 60.0).toInt().coerceIn(0, 59)
      val speed = computeDailySpeedDegrees(body, jd)
      val isRetro = body.canRetrograde && speed < 0.0
      // Whole-sign house relative to Rising sign (1..12)
      val house = ((sign.ordinal - risingSign.ordinal + 12) % 12) + 1
      PlanetaryPlacement(
        body = body,
        eclipticLongitude = lon,
        sign = sign,
        degreeInSign = deg,
        minuteInSign = min,
        house = house,
        isRetrograde = isRetro,
        dailySpeedDegrees = speed,
        interpretation = buildPlacementInterpretation(body, sign, house, isRetro),
      )
    }

    val sunSign = placements.first { it.body == CelestialBody.SUN }.sign
    val moonSign = placements.first { it.body == CelestialBody.MOON }.sign
    val aspects = computeAspects(placements)

    // Weighted element & modality balance (Personal bodies count 2x, outer bodies 1x)
    val elementCounts = ZodiacElement.entries.associateWith { 0 }.toMutableMap()
    val modalityCounts = ZodiacModality.entries.associateWith { 0 }.toMutableMap()
    placements.forEach { p ->
      val weight = when (p.body) {
        CelestialBody.SUN, CelestialBody.MOON, CelestialBody.MERCURY, CelestialBody.VENUS, CelestialBody.MARS -> 2
        else -> 1
      }
      elementCounts[p.sign.element] = (elementCounts[p.sign.element] ?: 0) + weight
      modalityCounts[p.sign.modality] = (modalityCounts[p.sign.modality] ?: 0) + weight
    }
    elementCounts[risingSign.element] = (elementCounts[risingSign.element] ?: 0) + 2
    modalityCounts[risingSign.modality] = (modalityCounts[risingSign.modality] ?: 0) + 2

    val dominantElement = elementCounts.maxByOrNull { it.value }?.key ?: sunSign.element
    val dominantModality = modalityCounts.maxByOrNull { it.value }?.key ?: sunSign.modality

    val chartRulerBody = CelestialBody.entries.firstOrNull { risingSign in it.rulesSigns }
      ?: CelestialBody.SUN

    val bigThreeSummary = "With a ${sunSign.displayName} Sun (${sunSign.glyph}), ${moonSign.displayName} Moon (${moonSign.glyph}), and ${risingSign.displayName} Rising (${risingSign.glyph}), ${profile.name} blends ${sunSign.keywords.first().lowercase(Locale.US)} ${sunSign.element.label.lowercase(Locale.US)} identity with a ${moonSign.keywords.first().lowercase(Locale.US)} emotional core and a ${risingSign.keywords.first().lowercase(Locale.US)}, magnetic first impression."

    return NatalChartCalculation(
      profile = profile,
      sunSign = sunSign,
      moonSign = moonSign,
      risingSign = risingSign,
      ascendantLongitude = ascLon,
      midheavenLongitude = mcLon,
      midheavenSign = mcSign,
      placements = placements,
      aspects = aspects,
      elementCounts = elementCounts,
      modalityCounts = modalityCounts,
      dominantElement = dominantElement,
      dominantModality = dominantModality,
      chartRulerBody = chartRulerBody,
      bigThreeSummary = bigThreeSummary,
    )
  }

  /**
   * Generates a deterministic, transit-backed [DailyHoroscopeReading] for [targetSign] (and optional
   * [natalChart]) on the given calendar date (`year-month-day`).
   */
  fun generateDailyHoroscope(
    year: Int,
    month: Int,
    day: Int,
    targetSign: ZodiacSign,
    natalChart: NatalChartCalculation? = null,
  ): DailyHoroscopeReading {
    val jd = toJulianDay(year, month, day, hour = 12, minute = 0, timeZoneId = "UTC")
    val moonSnapshot = computeMoonPhase(jd)

    val skyPlacements = CelestialBody.entries.map { body ->
      val lon = computeEclipticLongitude(body, jd)
      val sign = ZodiacSign.fromLongitude(lon)
      val inSignDegTotal = lon % 30.0
      val deg = inSignDegTotal.toInt().coerceIn(0, 29)
      val min = ((inSignDegTotal - deg) * 60.0).toInt().coerceIn(0, 59)
      val speed = computeDailySpeedDegrees(body, jd)
      val isRetro = body.canRetrograde && speed < 0.0
      val house = ((sign.ordinal - targetSign.ordinal + 12) % 12) + 1
      PlanetaryPlacement(
        body = body,
        eclipticLongitude = lon,
        sign = sign,
        degreeInSign = deg,
        minuteInSign = min,
        house = house,
        isRetrograde = isRetro,
        dailySpeedDegrees = speed,
        interpretation = buildPlacementInterpretation(body, sign, house, isRetro),
      )
    }

    val retrogradeBodies = skyPlacements.filter { it.isRetrograde }.map { it.body }
    val isMercuryRetro = CelestialBody.MERCURY in retrogradeBodies

    val transitSun = skyPlacements.first { it.body == CelestialBody.SUN }
    val transitMoon = skyPlacements.first { it.body == CelestialBody.MOON }
    val transitVenus = skyPlacements.first { it.body == CelestialBody.VENUS }
    val transitMars = skyPlacements.first { it.body == CelestialBody.MARS }
    val transitMercury = skyPlacements.first { it.body == CelestialBody.MERCURY }

    val moonHouse = ((transitMoon.sign.ordinal - targetSign.ordinal + 12) % 12) + 1
    val moonHouseInfo = ASTROLOGICAL_HOUSES[(moonHouse - 1).coerceIn(0, 11)]
    val venusHouse = ((transitVenus.sign.ordinal - targetSign.ordinal + 12) % 12) + 1
    val marsHouse = ((transitMars.sign.ordinal - targetSign.ordinal + 12) % 12) + 1

    val dateSeed = (year * 372 + month * 31 + day) + (targetSign.ordinal * 17)

    val mantras = listOf(
      "Trust the quiet pull of your intuition today.",
      "What is meant for your heart will meet you halfway.",
      "Lead with warmth; your presence shifts the whole room.",
      "Small rituals today create spacious magic tomorrow.",
      "Honor both your ambition and your need for sanctuary.",
      "Speak your truth gently—clarity is an act of love.",
      "Let beauty and curiosity guide your next decision.",
      "You don't have to rush what is rooted in truth.",
      "Release old static so fresh inspiration can land.",
      "Your authenticity is your strongest magnetism right now.",
      "Notice the synchronicities—the sky is winking at you.",
      "Protect your peace while keeping your heart wide open.",
    )
    val vibeWords = listOf(
      "Luminous",
      "Magnetic",
      "Grounded",
      "Enchanted",
      "Clear-Hearted",
      "Fearless",
      "Serene",
      "Inspired",
      "Radiant",
      "Soulful",
      "Aligned",
      "Playful",
    )

    val headlineMantra = mantras[abs(dateSeed) % mantras.size]
    val cosmicVibeWord = vibeWords[abs(dateSeed * 3 + 5) % vibeWords.size]

    val keyTransitHeadline = "${moonSnapshot.phaseType.emoji} ${moonSnapshot.phaseType.displayName} in ${transitMoon.sign.displayName} (${moonHouseInfo.roman} House) • Sun in ${transitSun.sign.displayName}"

    val natalMoonNote = if (natalChart != null && natalChart.sunSign == targetSign) {
      " With your natal ${natalChart.moonSign.displayName} Moon and ${natalChart.risingSign.displayName} Rising, this transit especially activates your ${ASTROLOGICAL_HOUSES[((transitMoon.sign.ordinal - natalChart.risingSign.ordinal + 12) % 12)].title}."
    } else {
      ""
    }

    val overviewReading = "As the ${moonSnapshot.phaseType.displayName} (${moonSnapshot.illuminationPercent}% illuminated) moves through ${transitMoon.sign.displayName} in your ${moonHouseInfo.title}, your focus turns toward ${moonHouseInfo.theme.lowercase(Locale.US)}. The Sun in ${transitSun.sign.displayName} harmonizes with your ${targetSign.element.label} nature, inviting you to lean into your ${targetSign.keywords.first().lowercase(Locale.US)} instincts.$natalMoonNote ${moonSnapshot.phaseType.guidance}"

    val loveReading = "Venus in ${transitVenus.sign.displayName} (${transitVenus.formattedDegree}) lights up your ${ASTROLOGICAL_HOUSES[venusHouse - 1].title}, bringing ${transitVenus.sign.keywords.first().lowercase(Locale.US)} warmth to your closest bonds. ${targetSign.loveStyle} Today favors heartfelt conversations, unhurried quality time, and showing affection in tangible ways."

    val mercuryClause = if (isMercuryRetro) {
      "Because Mercury is currently retrograde in ${transitMercury.sign.displayName}, double-check details, revisit a past creative idea, and give yourself grace with timing."
    } else {
      "Mercury direct in ${transitMercury.sign.displayName} sharpens your voice and helps your ideas land with persuasive clarity."
    }
    val careerReading = "Mars in ${transitMars.sign.displayName} energizes your ${ASTROLOGICAL_HOUSES[marsHouse - 1].title}, giving you ${transitMars.sign.keywords.first().lowercase(Locale.US)} momentum on meaningful goals. $mercuryClause"

    val wellnessReading = "With lunar energy flowing through ${transitMoon.sign.element.label.lowercase(Locale.US)}-ruled ${transitMoon.sign.displayName}, care for your ${targetSign.bodyPart.lowercase(Locale.US)} and balance active focus with restorative stillness."

    val dosPool = listOf(
      "Lean into ${transitMoon.sign.displayName}'s ${transitMoon.sign.keywords.first().lowercase(Locale.US)} energy in your ${moonHouseInfo.theme.substringBefore(",").lowercase(Locale.US)}",
      "Wear or surround yourself with ${targetSign.powerColorName} (${targetSign.powerColorHex}) for extra confidence",
      "Reach out to a ${transitVenus.sign.displayName} or ${targetSign.oppositeSign.displayName} soul who inspires you",
      "Write down one clear intention while the Moon is in its ${moonSnapshot.phaseType.displayName} phase",
      "Honor your ${targetSign.element.label} element with fresh air, music, or a favorite comfort ritual",
    )
    val dontsPool = listOf(
      if (isMercuryRetro) "Rush important messages without re-reading during Mercury Retrograde" else "Second-guess your first intuitive yes",
      "Let someone else's urgency pull you out of your natural rhythm",
      "Overpack your evening when your spirit is asking for breathing room",
      "Hold back a sincere compliment or affectionate gesture",
    )

    val luckyNumber = ((abs(dateSeed * 7) % 27) + 1)
    val luckyHours = listOf("9:30 AM – 11:00 AM", "1:15 PM – 3:00 PM", "4:45 PM – 6:30 PM", "7:30 PM – 9:15 PM", "10:00 PM – 11:30 PM")
    val luckyTimeWindow = luckyHours[abs(dateSeed) % luckyHours.size]

    // Best match sign today: trine (same element) or sextile sign
    val compatibleOffsets = listOf(4, 8, 2, 10, 6)
    val bestMatchOffset = compatibleOffsets[abs(dateSeed) % compatibleOffsets.size]
    val bestMatchSign = ZodiacSign.entries[(targetSign.ordinal + bestMatchOffset) % 12]

    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val mLabel = monthNames.getOrElse((month - 1).coerceIn(0, 11)) { "Jan" }
    val dateFormatted = "$mLabel $day, $year"

    return DailyHoroscopeReading(
      dateFormatted = dateFormatted,
      year = year,
      month = month,
      day = day,
      targetSign = targetSign,
      moonSnapshot = moonSnapshot,
      headlineMantra = headlineMantra,
      cosmicVibeWord = cosmicVibeWord,
      keyTransitHeadline = keyTransitHeadline,
      overviewReading = overviewReading,
      loveReading = loveReading,
      careerReading = careerReading,
      wellnessReading = wellnessReading,
      dosList = dosPool.take(3),
      dontsList = dontsPool.take(3),
      luckyNumber = luckyNumber,
      luckyTimeWindow = luckyTimeWindow,
      powerColorName = targetSign.powerColorName,
      powerColorHex = targetSign.powerColorHex,
      bestMatchSign = bestMatchSign,
      retrogradeBodies = retrogradeBodies,
      isMercuryRetrograde = isMercuryRetro,
      skyPlacements = skyPlacements,
    )
  }

  /**
   * Computes a comprehensive synastry compatibility report between two [NatalChartCalculation]s.
   */
  fun calculateSynastry(
    first: NatalChartCalculation,
    second: NatalChartCalculation,
  ): SynastryCompatibilityReport {
    fun signHarmony(s1: ZodiacSign, s2: ZodiacSign): Int {
      val diff = abs(s1.ordinal - s2.ordinal) % 12
      val shortest = if (diff > 6) 12 - diff else diff
      return when (shortest) {
        0 -> 92 // Conjunction (Same sign)
        4 -> 96 // Trine (Same element)
        2 -> 90 // Sextile (Fire-Air / Earth-Water)
        6 -> 88 // Opposition (Magnetic soulmate polarity)
        3 -> 76 // Square (High passion & catalyst growth)
        1, 5 -> 79 // Semi-sextile / Quincunx (Intriguing mystery)
        else -> 82
      }
    }

    val sunSun = signHarmony(first.sunSign, second.sunSign)
    val moonMoon = signHarmony(first.moonSign, second.moonSign)
    val sunMoonCross = (signHarmony(first.sunSign, second.moonSign) + signHarmony(first.moonSign, second.sunSign)) / 2
    val risingHarmony = signHarmony(first.risingSign, second.risingSign)

    val firstVenus = first.placements.first { it.body == CelestialBody.VENUS }.sign
    val secondVenus = second.placements.first { it.body == CelestialBody.VENUS }.sign
    val firstMars = first.placements.first { it.body == CelestialBody.MARS }.sign
    val secondMars = second.placements.first { it.body == CelestialBody.MARS }.sign
    val firstMercury = first.placements.first { it.body == CelestialBody.MERCURY }.sign
    val secondMercury = second.placements.first { it.body == CelestialBody.MERCURY }.sign

    val venusMarsCross = (signHarmony(firstVenus, secondMars) + signHarmony(firstMars, secondVenus) + signHarmony(firstVenus, secondVenus)) / 3
    val mercuryHarmony = (signHarmony(firstMercury, secondMercury) + sunSun) / 2

    val romanceScore = ((venusMarsCross * 0.6 + sunMoonCross * 0.4).roundToInt()).coerceIn(65, 99)
    val emotionalScore = ((moonMoon * 0.55 + sunMoonCross * 0.45).roundToInt()).coerceIn(65, 99)
    val communicationScore = ((mercuryHarmony * 0.65 + risingHarmony * 0.35).roundToInt()).coerceIn(65, 99)
    val valuesScore = ((signHarmony(firstVenus, secondVenus) * 0.5 + sunSun * 0.5).roundToInt()).coerceIn(65, 99)
    val overallScore = ((romanceScore * 0.30 + emotionalScore * 0.30 + communicationScore * 0.20 + valuesScore * 0.20).roundToInt()).coerceIn(68, 99)

    // Compute inter-chart cross aspects between core personal planets
    val coreBodies = setOf(
      CelestialBody.SUN,
      CelestialBody.MOON,
      CelestialBody.MERCURY,
      CelestialBody.VENUS,
      CelestialBody.MARS,
      CelestialBody.JUPITER,
    )
    val crossAspects = mutableListOf<PlanetaryAspect>()
    for (p1 in first.placements.filter { it.body in coreBodies }) {
      for (p2 in second.placements.filter { it.body in coreBodies }) {
        val sep = angularSeparation(p1.eclipticLongitude, p2.eclipticLongitude)
        val aspect = AspectType.entries.firstOrNull { abs(sep - it.exactAngle) <= it.orbDegrees } ?: continue
        val orb = abs(sep - aspect.exactAngle)
        val interp = "${first.profile.name}'s ${p1.body.displayName} (${p1.sign.displayName}) ${aspect.displayName.lowercase(Locale.US)} ${second.profile.name}'s ${p2.body.displayName} (${p2.sign.displayName}) — ${aspect.nature.lowercase(Locale.US)} connecting ${p1.body.archetype.lowercase(Locale.US)} with ${p2.body.archetype.lowercase(Locale.US)}."
        crossAspects.add(
          PlanetaryAspect(
            firstBody = p1.body,
            firstSign = p1.sign,
            secondBody = p2.body,
            secondSign = p2.sign,
            aspectType = aspect,
            exactSeparationDegrees = sep,
            orbDegrees = orb,
            interpretation = interp,
          )
        )
      }
    }

    val e1 = first.sunSign.element
    val e2 = second.sunSign.element
    val (bondTitle, elementalDynamics) = when {
      e1 == e2 -> "Kindred ${e1.label} Resonance" to "Sharing the ${e1.label} element creates instant recognition, shared intuition, and natural fluency in how you both move through the world."
      (e1 == ZodiacElement.FIRE && e2 == ZodiacElement.AIR) || (e1 == ZodiacElement.AIR && e2 == ZodiacElement.FIRE) ->
        "Spark & Breeze Alchemy" to "Air feeds Fire and Fire uplifts Air—your connection brims with playful wit, creative inspiration, and shared excitement for life."
      (e1 == ZodiacElement.EARTH && e2 == ZodiacElement.WATER) || (e1 == ZodiacElement.WATER && e2 == ZodiacElement.EARTH) ->
        "Garden & Rain Sanctuary" to "Water nourishes Earth while Earth holds a safe vessel for Water—creating a deeply tender, loyal, and grounding emotional sanctuary."
      (e1 == ZodiacElement.FIRE && e2 == ZodiacElement.WATER) || (e1 == ZodiacElement.WATER && e2 == ZodiacElement.FIRE) ->
        "Steam & Passion Alchemy" to "Fire brings warmth and courage while Water brings soulful depth and intuition—a passionate, protective, and deeply romantic contrast."
      (e1 == ZodiacElement.FIRE && e2 == ZodiacElement.EARTH) || (e1 == ZodiacElement.EARTH && e2 == ZodiacElement.FIRE) ->
        "Vision & Foundation Bond" to "Fire ignites bold dreams while Earth turns them into lasting reality—a powerhouse partnership of warmth, loyalty, and shared building."
      else ->
        "Mind & Soul Synthesis" to "Combining ${e1.label} and ${e2.label} blends intellectual curiosity with sensual or emotional depth, helping you see the world through each other's eyes."
    }

    val summary = "${first.profile.name} (${first.sunSign.glyph} ${first.sunSign.displayName}) and ${second.profile.name} (${second.sunSign.glyph} ${second.sunSign.displayName}) share a $overallScore% cosmic resonance. $elementalDynamics With ${first.profile.name}'s ${first.moonSign.displayName} Moon and ${second.profile.name}'s ${second.moonSign.displayName} Moon, your emotional bond thrives on ${first.moonSign.keywords.first().lowercase(Locale.US)} care and ${second.moonSign.keywords.first().lowercase(Locale.US)} devotion."

    val strengths = listOf(
      "Sun–Moon Synergy: ${first.sunSign.displayName} & ${second.moonSign.displayName} balance ${first.sunSign.keywords.first().lowercase(Locale.US)} vitality with ${second.moonSign.keywords.first().lowercase(Locale.US)} emotional intuition.",
      "Venus–Mars Chemistry: ${firstVenus.displayName} Venus and ${secondMars.displayName} Mars spark affectionate warmth, attraction, and shared appreciation for beauty.",
      "Communication Flow: Mercury in ${firstMercury.displayName} and ${secondMercury.displayName} keeps conversations honest, curious, and supportive.",
    )
    val growthTips = listOf(
      "Honor pacing differences between ${first.sunSign.modality.label} (${first.sunSign.modality.role}) and ${second.sunSign.modality.label} (${second.sunSign.modality.role}) rhythms.",
      "Nurture both ${first.moonSign.displayName} Moon's need for ${first.moonSign.keywords[1].lowercase(Locale.US)} safety and ${second.moonSign.displayName} Moon's need for ${second.moonSign.keywords[1].lowercase(Locale.US)} connection.",
    )

    return SynastryCompatibilityReport(
      firstLabel = first.profile.name,
      firstSun = first.sunSign,
      firstMoon = first.moonSign,
      firstRising = first.risingSign,
      secondLabel = second.profile.name,
      secondSun = second.sunSign,
      secondMoon = second.moonSign,
      secondRising = second.risingSign,
      overallScore = overallScore,
      romanceScore = romanceScore,
      communicationScore = communicationScore,
      emotionalScore = emotionalScore,
      valuesScore = valuesScore,
      bondTitle = bondTitle,
      elementalDynamics = elementalDynamics,
      summary = summary,
      strengths = strengths,
      growthTips = growthTips,
      crossAspects = crossAspects.sortedBy { it.orbDegrees }.take(8),
    )
  }

  /**
   * Builds a quick sign-to-sign [SynastryCompatibilityReport] when comparing any two [ZodiacSign]s directly.
   */
  fun calculateSignPairSynastry(
    firstSign: ZodiacSign,
    secondSign: ZodiacSign,
  ): SynastryCompatibilityReport {
    val p1 = BirthProfile(
      id = "sign_${firstSign.name}",
      name = firstSign.displayName,
      birthYear = 2000,
      birthMonth = ((firstSign.ordinal + 3) % 12) + 1,
      birthDay = 1,
      hasExactBirthTime = false,
    )
    val p2 = BirthProfile(
      id = "sign_${secondSign.name}",
      name = secondSign.displayName,
      birthYear = 2000,
      birthMonth = ((secondSign.ordinal + 3) % 12) + 1,
      birthDay = 1,
      hasExactBirthTime = false,
    )
    val c1 = calculateNatalChart(p1).copy(sunSign = firstSign, moonSign = firstSign, risingSign = firstSign)
    val c2 = calculateNatalChart(p2).copy(sunSign = secondSign, moonSign = secondSign, risingSign = secondSign)
    return calculateSynastry(c1, c2)
  }

  /**
   * Complete searchable catalog of Zodiac Signs, Planets, 12 Houses, Aspects, Lunar Phases, and Elements.
   */
  val GUIDE_CATALOG: List<StarsGuideEntry> by lazy {
    buildList {
      // 1. All 12 Zodiac Signs
      for (sign in ZodiacSign.entries) {
        add(
          StarsGuideEntry(
            id = "sign_${sign.name}",
            title = "${sign.displayName} (${sign.symbolName})",
            glyph = sign.glyph,
            category = StarsGuideCategory.SIGNS,
            subtitle = "${sign.dateRange} • ${sign.element.label} • ${sign.modality.label} • Ruled by ${sign.rulingPlanetName}",
            badgeLabel = "${sign.element.label} Sign",
            summary = sign.summary,
            powerColorHex = sign.powerColorHex,
            keywords = "${sign.displayName} ${sign.symbolName} ${sign.element.label} ${sign.modality.label} ${sign.rulingPlanetName} ${sign.keywords.joinToString(" ")} ${sign.birthstone} zodiac sign horoscope",
            details = listOf(
              "Glyph & Symbol" to "${sign.glyph} ${sign.symbolName}",
              "Season Dates" to sign.dateRange,
              "Element & Modality" to "${sign.element.label} • ${sign.modality.label} (${sign.modality.role})",
              "Planetary Ruler" to "${sign.rulingPlanetName} (Natural ${ASTROLOGICAL_HOUSES[sign.naturalHouse - 1].title})",
              "Core Motto" to "\"${sign.motto}\"",
              "Key Traits" to sign.keywords.joinToString(" • "),
              "Strengths" to sign.strengths,
              "Growth Edges" to sign.challenges,
              "Love & Romance" to sign.loveStyle,
              "Career & Calling" to sign.careerStyle,
              "Power Color" to "${sign.powerColorName} (${sign.powerColorHex})",
              "Birthstones" to sign.birthstone,
              "Opposite Sign" to "${sign.oppositeSign.glyph} ${sign.oppositeSign.displayName}",
            ),
          )
        )
      }

      // 2. All 10 Celestial Bodies
      for (body in CelestialBody.entries) {
        val ruled = body.rulesSigns.joinToString(", ") { "${it.glyph} ${it.displayName}" }
        add(
          StarsGuideEntry(
            id = "planet_${body.name}",
            title = "${body.displayName} — ${body.archetype}",
            glyph = body.glyph,
            category = StarsGuideCategory.PLANETS,
            subtitle = "${body.domain} • Orbit: ${body.orbitalPeriodLabel}",
            badgeLabel = "Celestial Body",
            summary = body.description,
            keywords = "${body.displayName} ${body.archetype} ${body.domain} $ruled planet astrology retrograde",
            details = listOf(
              "Planet & Glyph" to "${body.glyph} ${body.displayName}",
              "Archetype" to body.archetype,
              "Life Domain" to body.domain,
              "Rules Zodiac Sign(s)" to ruled,
              "Orbital Cycle" to body.orbitalPeriodLabel,
              "Retrograde Motion" to if (body.canRetrograde) "Yes — Periodically turns retrograde from Earth's perspective" else "Never retrogrades (Luminary)",
              "In Your Chart" to body.description,
            ),
          )
        )
      }

      // 3. All 12 Astrological Houses
      for (house in ASTROLOGICAL_HOUSES) {
        add(
          StarsGuideEntry(
            id = "house_${house.number}",
            title = house.title,
            glyph = house.roman,
            category = StarsGuideCategory.HOUSES,
            subtitle = "${house.theme} • Natural Sign: ${house.naturalSign.glyph} ${house.naturalSign.displayName}",
            badgeLabel = "House ${house.roman}",
            summary = house.description,
            keywords = "${house.title} ${house.theme} ${house.latinName} ${house.naturalSign.displayName} house chart",
            details = listOf(
              "House Number" to "${house.title} (${house.roman})",
              "Traditional Motto" to house.latinName,
              "Core Themes" to house.theme,
              "Natural Sign & Ruler" to "${house.naturalSign.glyph} ${house.naturalSign.displayName} (${house.naturalSign.rulingPlanetName})",
              "Meaning in Chart" to house.description,
            ),
          )
        )
      }

      // 4. All 5 Major Aspects
      for (aspect in AspectType.entries) {
        add(
          StarsGuideEntry(
            id = "aspect_${aspect.name}",
            title = "${aspect.displayName} (${aspect.exactAngle.roundToInt()}°)",
            glyph = aspect.glyph,
            category = StarsGuideCategory.ASPECTS,
            subtitle = "${aspect.nature} • Standard Orb: ±${aspect.orbDegrees}°",
            badgeLabel = "Aspect",
            summary = aspect.summary,
            keywords = "${aspect.displayName} ${aspect.nature} ${aspect.exactAngle.roundToInt()} degrees orb aspect synastry",
            details = listOf(
              "Aspect & Glyph" to "${aspect.glyph} ${aspect.displayName}",
              "Exact Angle" to "${aspect.exactAngle.roundToInt()}° (±${aspect.orbDegrees}° orb)",
              "Energetic Nature" to aspect.nature,
              "How It Feels" to aspect.summary,
            ),
          )
        )
      }

      // 5. Lunar Phases & 4 Elements
      for (phase in LunarPhaseType.entries) {
        add(
          StarsGuideEntry(
            id = "phase_${phase.name}",
            title = "${phase.displayName} Phase",
            glyph = phase.emoji,
            category = StarsGuideCategory.PHASES,
            subtitle = "Lunar Cycle Ritual & Emotional Guidance",
            badgeLabel = "Moon Phase",
            summary = phase.guidance,
            keywords = "${phase.displayName} moon phase lunar cycle ritual",
            details = listOf(
              "Lunar Phase" to "${phase.emoji} ${phase.displayName}",
              "Cosmic Guidance" to phase.guidance,
            ),
          )
        )
      }

      for (element in ZodiacElement.entries) {
        val elementSigns = ZodiacSign.entries.filter { it.element == element }
          .joinToString(", ") { "${it.glyph} ${it.displayName}" }
        add(
          StarsGuideEntry(
            id = "element_${element.name}",
            title = "${element.label} Element — ${element.motto}",
            glyph = element.label.first().toString(),
            category = StarsGuideCategory.PHASES,
            subtitle = "Signs: $elementSigns",
            badgeLabel = "Element",
            summary = element.traits,
            powerColorHex = element.colorHex,
            keywords = "${element.label} element ${element.traits} $elementSigns",
            details = listOf(
              "Element" to element.label,
              "Motto" to element.motto,
              "Signs" to elementSigns,
              "Core Qualities" to element.traits,
            ),
          )
        )
      }

      // 6. All 16 Sky Constellations (including Orion & Orion's Belt, Big Dipper, Cassiopeia, Cygnus & 12 Zodiac Constellations)
      for (constellation in SKY_CONSTELLATIONS) {
        val starNames = constellation.stars.joinToString(", ") { "${it.name} (${it.roleLabel})" }
        add(
          StarsGuideEntry(
            id = "constellation_${constellation.id}",
            title = "${constellation.name} — ${constellation.subtitle}",
            glyph = constellation.glyph,
            category = StarsGuideCategory.CONSTELLATIONS,
            subtitle = "${constellation.asterismNote}",
            badgeLabel = if (constellation.zodiacSign != null) "Zodiac Constellation" else "Sky Constellation",
            summary = constellation.mythAndLore,
            powerColorHex = constellation.zodiacSign?.powerColorHex,
            keywords = "${constellation.name} ${constellation.subtitle} ${constellation.asterismNote} $starNames constellation sky ar stars orions belt",
            details = listOf(
              "Constellation" to "${constellation.glyph} ${constellation.name} (${constellation.subtitle})",
              "Asterism & Star-Hopping" to constellation.asterismNote,
              "Principal Stars (${constellation.stars.size})" to starNames,
              "Coordinates (J2000)" to String.format(
                Locale.US,
                "RA %.1fh • Dec %+.1f°",
                constellation.centerRaHours,
                constellation.centerDecDegrees,
              ),
              "Myth & Star Lore" to constellation.mythAndLore,
            ),
          )
        )
      }
    }
  }

  /**
   * Converts Equatorial coordinates (Right Ascension [raHours] `0..24`, Declination [decDegrees] `-90..+90`)
   * into Horizontal sky coordinates ([SkyHorizontalPosition]: Altitude & Azimuth) for an observer at
   * ([latitude], [longitude]) at Julian Day [jd].
   */
  fun equatorialToHorizontal(
    raHours: Double,
    decDegrees: Double,
    jd: Double,
    latitude: Double,
    longitude: Double,
  ): SkyHorizontalPosition {
    val d = jd - 2451545.0
    val t = d / 36525.0
    val gmst = normalizeDegrees(280.46061837 + 360.98564736629 * d + 0.000387933 * t * t)
    val lstDeg = normalizeDegrees(gmst + longitude)
    val raDeg = normalizeDegrees(raHours * 15.0)
    val haRad = normalizeDegrees(lstDeg - raDeg) * DEG_TO_RAD
    val decRad = decDegrees.coerceIn(-89.99, 89.99) * DEG_TO_RAD
    val latRad = latitude.coerceIn(-89.5, 89.5) * DEG_TO_RAD

    val sinAlt = (sin(decRad) * sin(latRad) + cos(decRad) * cos(latRad) * cos(haRad)).coerceIn(-1.0, 1.0)
    val altRad = asin(sinAlt)
    val altDeg = altRad * RAD_TO_DEG

    val y = -sin(haRad) * cos(decRad)
    val x = sin(decRad) - sin(latRad) * sinAlt
    val azDeg = normalizeDegrees(atan2(y, x) * RAD_TO_DEG)

    return SkyHorizontalPosition(
      altitudeDegrees = altDeg,
      azimuthDegrees = azDeg,
    )
  }

  /**
   * Converts an Ecliptic longitude ([eclipticLongitudeDeg], with `beta = 0°`) into Equatorial
   * coordinates (`Pair(raHours, decDegrees)`), so the Sun, Moon, and Planets can be plotted on the
   * 3D Sky AR dome alongside the stars.
   */
  fun eclipticToEquatorial(
    eclipticLongitudeDeg: Double,
    jd: Double,
  ): Pair<Double, Double> {
    val d = jd - 2451545.0
    val t = d / 36525.0
    val epsRad = (23.439291 - 0.0130042 * t) * DEG_TO_RAD
    val lonRad = normalizeDegrees(eclipticLongitudeDeg) * DEG_TO_RAD

    val sinDec = (sin(epsRad) * sin(lonRad)).coerceIn(-1.0, 1.0)
    val decDeg = asin(sinDec) * RAD_TO_DEG
    val raDeg = normalizeDegrees(atan2(sin(lonRad) * cos(epsRad), cos(lonRad)) * RAD_TO_DEG)
    val raHours = raDeg / 15.0
    return raHours to decDeg
  }

  /**
   * Projects a sky object at ([targetAltitudeDeg], [targetAzimuthDeg]) onto the camera viewport
   * given the camera's pointing direction ([cameraAltitudeDeg], [cameraAzimuthDeg], [cameraRollDeg])
   * and fields of view ([fovHorizontalDeg], [fovVerticalDeg]).
   */
  fun projectSkyToCameraViewport(
    targetAltitudeDeg: Double,
    targetAzimuthDeg: Double,
    cameraAltitudeDeg: Double,
    cameraAzimuthDeg: Double,
    cameraRollDeg: Double = 0.0,
    fovHorizontalDeg: Double = 62.0,
    fovVerticalDeg: Double = 78.0,
  ): ProjectedSkyPoint {
    // Convert spherical (Alt, Az) to 3D East-North-Up (ENU) unit vectors:
    // East = sin(Az)*cos(Alt), North = cos(Az)*cos(Alt), Up = sin(Alt)
    fun toEnu(altDeg: Double, azDeg: Double): Triple<Double, Double, Double> {
      val altR = altDeg * DEG_TO_RAD
      val azR = azDeg * DEG_TO_RAD
      val cAlt = cos(altR)
      return Triple(
        sin(azR) * cAlt, // East (X)
        cos(azR) * cAlt, // North (Y)
        sin(altR),       // Up (Z)
      )
    }

    val (tx, ty, tz) = toEnu(targetAltitudeDeg, targetAzimuthDeg)
    val (fx, fy, fz) = toEnu(cameraAltitudeDeg, cameraAzimuthDeg)

    // Camera right vector = horizontal perpendicular to camera azimuth (Az + 90°)
    val rightAzRad = (cameraAzimuthDeg + 90.0) * DEG_TO_RAD
    val rx0 = sin(rightAzRad)
    val ry0 = cos(rightAzRad)
    val rz0 = 0.0

    // Camera up vector = Right x Forward
    val ux0 = ry0 * fz - rz0 * fy
    val uy0 = rz0 * fx - rx0 * fz
    val uz0 = rx0 * fy - ry0 * fx

    // Apply optional camera roll around forward axis
    val rollR = cameraRollDeg * DEG_TO_RAD
    val cRoll = cos(rollR)
    val sRoll = sin(rollR)
    val rx = rx0 * cRoll + ux0 * sRoll
    val ry = ry0 * cRoll + uy0 * sRoll
    val rz = rz0 * cRoll + uz0 * sRoll
    val ux = ux0 * cRoll - rx0 * sRoll
    val uy = uy0 * cRoll - ry0 * sRoll
    val uz = uz0 * cRoll - rz0 * sRoll

    val dotForward = (tx * fx + ty * fy + tz * fz).coerceIn(-1.0, 1.0)
    val dotRight = tx * rx + ty * ry + tz * rz
    val dotUp = tx * ux + ty * uy + tz * uz

    val angularDistDeg = kotlin.math.acos(dotForward) * RAD_TO_DEG
    val isInFront = dotForward > 0.08

    // Perspective / gnomonic tangent projection clamped smoothly for wide off-axis markers
    val denom = dotForward.coerceAtLeast(0.15)
    val tanHalfH = tan((fovHorizontalDeg.coerceIn(20.0, 120.0) * 0.5) * DEG_TO_RAD).coerceAtLeast(0.1)
    val tanHalfV = tan((fovVerticalDeg.coerceIn(20.0, 130.0) * 0.5) * DEG_TO_RAD).coerceAtLeast(0.1)

    val ndcX = (dotRight / denom) / tanHalfH
    val ndcY = (dotUp / denom) / tanHalfV

    val normX = (0.5 + 0.5 * ndcX).toFloat()
    val normY = (0.5 - 0.5 * ndcY).toFloat()

    return ProjectedSkyPoint(
      normX = normX,
      normY = normY,
      altitudeDegrees = targetAltitudeDeg,
      azimuthDegrees = targetAzimuthDeg,
      angularDistanceFromCenterDeg = angularDistDeg,
      isInFrontOfCamera = isInFront,
    )
  }

  /**
   * Detects bright point-like star sources from raw ARGB pixels (or a [Bitmap]) of a live camera frame.
   *
   * Divides the frame into a spatial grid, computes local background luminance and contrast, and
   * extracts sharp bright peaks (rejecting broad uniform glare) so pointing the camera at stars in
   * the night sky (or an image of Orion's Belt) locates the glowing star coordinates.
   */
  fun detectBrightStarPointsFromArgb(
    pixels: IntArray,
    width: Int,
    height: Int,
    maxStars: Int = 24,
  ): List<DetectedCameraStarPoint> {
    if (width < 16 || height < 16 || pixels.size < width * height) return emptyList()

    val lum = FloatArray(width * height)
    var sumLum = 0.0
    for (i in 0 until width * height) {
      val c = pixels[i]
      val r = (c shr 16) and 0xFF
      val g = (c shr 8) and 0xFF
      val b = c and 0xFF
      val l = (0.299f * r + 0.587f * g + 0.114f * b) / 255f
      lum[i] = l
      sumLum += l
    }
    val meanLum = (sumLum / (width * height)).toFloat()
    // Reject completely washed-out daylight frames unless there are very strong specular points
    val minPeakThreshold = (meanLum + 0.22f).coerceIn(0.32f, 0.92f)

    val candidates = mutableListOf<DetectedCameraStarPoint>()
    val step = maxOf(2, minOf(width, height) / 64)
    val ringOffset = (step * 2).coerceAtLeast(3)

    var y = ringOffset
    while (y < height - ringOffset) {
      var x = ringOffset
      while (x < width - ringOffset) {
        val centerLum = lum[y * width + x]
        if (centerLum >= minPeakThreshold) {
          // Verify local maximum in immediate neighborhood
          val nUp = lum[(y - step) * width + x]
          val nDown = lum[(y + step) * width + x]
          val nLeft = lum[y * width + (x - step)]
          val nRight = lum[y * width + (x + step)]
          if (centerLum >= nUp && centerLum >= nDown && centerLum >= nLeft && centerLum >= nRight) {
            // Check outer ring contrast so we pick compact star points rather than large flat walls
            val outerAvg = (
              lum[(y - ringOffset) * width + x] +
                lum[(y + ringOffset) * width + x] +
                lum[y * width + (x - ringOffset)] +
                lum[y * width + (x + ringOffset)]
              ) * 0.25f
            val contrast = centerLum - outerAvg
            if (contrast >= 0.14f) {
              candidates.add(
                DetectedCameraStarPoint(
                  normX = x.toFloat() / width.toFloat(),
                  normY = y.toFloat() / height.toFloat(),
                  brightness = centerLum.coerceIn(0f, 1f),
                  radiusScore = (contrast * 2.2f).coerceIn(0.3f, 1.5f),
                )
              )
            }
          }
        }
        x += step
      }
      y += step
    }

    // Non-maximum suppression (merge nearby detections within 3.5% of frame width)
    val sorted = candidates.sortedByDescending { it.brightness + it.radiusScore * 0.5f }
    val kept = mutableListOf<DetectedCameraStarPoint>()
    val minDist = 0.035f
    for (pt in sorted) {
      val tooClose = kept.any { existing ->
        hypot((pt.normX - existing.normX).toDouble(), (pt.normY - existing.normY).toDouble()) < minDist
      }
      if (!tooClose) {
        kept.add(pt)
        if (kept.size >= maxStars) break
      }
    }
    return kept
  }

  fun detectBrightStarPointsFromBitmap(
    bitmap: Bitmap,
    maxStars: Int = 24,
  ): List<DetectedCameraStarPoint> {
    val w = bitmap.width
    val h = bitmap.height
    if (w < 16 || h < 16) return emptyList()
    val pixels = IntArray(w * h)
    bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
    return detectBrightStarPointsFromArgb(pixels, w, h, maxStars)
  }

  /**
   * Matches detected camera star points against **Orion's Belt** (3 collinear, equally spaced bright stars:
   * Alnitak, Alnilam, Mintaka) and reconstructs the rest of Orion's constellation around the belt,
   * or snaps the currently aimed constellation's projected vertices onto nearby detected camera stars.
   */
  fun matchConstellationToDetectedStars(
    detectedPoints: List<DetectedCameraStarPoint>,
    aimedConstellation: SkyConstellation? = null,
    projectedStarCoords: Map<String, Pair<Float, Float>> = emptyMap(),
  ): StarVisionSnapMatch? {
    if (detectedPoints.size < 3) return null

    // 1. Check for a collinear, equally-spaced 3-star triplet (Orion's Belt signature!)
    val topPoints = detectedPoints.take(12)
    var bestBeltTriplet: Triple<DetectedCameraStarPoint, DetectedCameraStarPoint, DetectedCameraStarPoint>? = null
    var bestBeltScore = 0f

    for (i in topPoints.indices) {
      for (j in topPoints.indices) {
        if (j == i) continue
        for (k in topPoints.indices) {
          if (k == i || k == j) continue
          val p1 = topPoints[i]
          val p2 = topPoints[j] // middle candidate
          val p3 = topPoints[k]
          // Order left-to-right (or top-to-bottom) to avoid duplicate permutations
          if (p1.normX > p3.normX || (p1.normX == p3.normX && p1.normY > p3.normY)) continue

          val vx1 = (p2.normX - p1.normX).toDouble()
          val vy1 = (p2.normY - p1.normY).toDouble()
          val vx2 = (p3.normX - p2.normX).toDouble()
          val vy2 = (p3.normY - p2.normY).toDouble()
          val d1 = hypot(vx1, vy1)
          val d2 = hypot(vx2, vy2)
          val totalSpan = d1 + d2
          if (d1 < 0.035 || d2 < 0.035 || totalSpan > 0.65) continue

          val cosAngle = ((vx1 * vx2 + vy1 * vy2) / (d1 * d2)).coerceIn(-1.0, 1.0)
          // Collinear within ~16 degrees (cosAngle >= 0.96) and spacing ratio within 35%
          val spacingRatio = minOf(d1, d2) / maxOf(d1, d2)
          if (cosAngle >= 0.955 && spacingRatio >= 0.68) {
            val centerBias = 1.0 - (hypot(p2.normX - 0.5, p2.normY - 0.5) * 0.5).coerceIn(0.0, 0.5)
            val score = (cosAngle * 0.5 + spacingRatio * 0.35 + centerBias * 0.15).toFloat()
            if (score > bestBeltScore) {
              bestBeltScore = score
              bestBeltTriplet = Triple(p1, p2, p3)
            }
          }
        }
      }
    }

    if (bestBeltTriplet != null && (aimedConstellation == null || aimedConstellation.id == "orion" || bestBeltScore >= 0.86f)) {
      val (pLeft, pMid, pRight) = bestBeltTriplet
      // Vector along Orion's Belt from Alnitak (East/left) to Mintaka (West/right)
      val bx = pRight.normX - pLeft.normX
      val by = pRight.normY - pLeft.normY
      // Perpendicular vector pointing "up" toward Orion's shoulders (Betelgeuse & Bellatrix)
      var px = by
      var py = -bx
      if (py > 0f) {
        px = -px
        py = -py
      }

      fun snapOrSynthesize(targetX: Float, targetY: Float, maxSnapDist: Float = 0.11f): Pair<Float, Float> {
        val nearest = detectedPoints.minByOrNull {
          hypot((it.normX - targetX).toDouble(), (it.normY - targetY).toDouble())
        }
        if (nearest != null) {
          val dist = hypot((nearest.normX - targetX).toDouble(), (nearest.normY - targetY).toDouble()).toFloat()
          if (dist <= maxSnapDist) {
            return nearest.normX to nearest.normY
          }
        }
        return targetX.coerceIn(0.04f, 0.96f) to targetY.coerceIn(0.04f, 0.96f)
      }

      val snapped = mutableMapOf<String, Pair<Float, Float>>()
      snapped["alnitak"] = pLeft.normX to pLeft.normY
      snapped["alnilam"] = pMid.normX to pMid.normY
      snapped["mintaka"] = pRight.normX to pRight.normY
      snapped["betelgeuse"] = snapOrSynthesize(pLeft.normX - bx * 0.35f + px * 2.4f, pLeft.normY - by * 0.35f + py * 2.4f)
      snapped["bellatrix"] = snapOrSynthesize(pRight.normX + bx * 0.35f + px * 2.1f, pRight.normY + by * 0.35f + py * 2.1f)
      snapped["meissa"] = snapOrSynthesize(pMid.normX + px * 2.85f, pMid.normY + py * 2.85f)
      snapped["saiph"] = snapOrSynthesize(pLeft.normX - bx * 0.25f - px * 2.25f, pLeft.normY - by * 0.25f - py * 2.25f)
      snapped["rigel"] = snapOrSynthesize(pRight.normX + bx * 0.45f - px * 2.2f, pRight.normY + by * 0.45f - py * 2.2f)

      return StarVisionSnapMatch(
        constellationId = "orion",
        constellationName = "Orion",
        asterismLabel = "Orion's Belt Locked (Alnitak • Alnilam • Mintaka)",
        confidence = bestBeltScore.coerceIn(0.75f, 0.99f),
        snappedStarPositions = snapped,
        matchedPoints = listOf(pLeft, pMid, pRight),
      )
    }

    // 2. Otherwise, snap the aimed constellation's projected vertices onto nearby detected bright camera stars
    val targetConstellation = aimedConstellation ?: return null
    if (projectedStarCoords.isEmpty()) return null

    val snapped = mutableMapOf<String, Pair<Float, Float>>()
    val matchedPts = mutableListOf<DetectedCameraStarPoint>()
    var snappedCount = 0

    for (star in targetConstellation.stars) {
      val proj = projectedStarCoords[star.id] ?: continue
      val nearest = detectedPoints.minByOrNull {
        hypot((it.normX - proj.first).toDouble(), (it.normY - proj.second).toDouble())
      }
      if (nearest != null) {
        val dist = hypot((nearest.normX - proj.first).toDouble(), (nearest.normY - proj.second).toDouble()).toFloat()
        if (dist <= 0.14f) {
          snapped[star.id] = nearest.normX to nearest.normY
          matchedPts.add(nearest)
          snappedCount++
        } else {
          snapped[star.id] = proj
        }
      } else {
        snapped[star.id] = proj
      }
    }

    if (snappedCount < 2) return null
    val conf = (0.60f + 0.10f * snappedCount).coerceIn(0.65f, 0.96f)
    return StarVisionSnapMatch(
      constellationId = targetConstellation.id,
      constellationName = targetConstellation.name,
      asterismLabel = "${targetConstellation.name} (${snappedCount} stars locked)",
      confidence = conf,
      snappedStarPositions = snapped,
      matchedPoints = matchedPts.distinct(),
    )
  }
}
