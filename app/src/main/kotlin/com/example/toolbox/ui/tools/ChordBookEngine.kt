package com.example.toolbox.ui.tools

import android.content.Context
import android.content.SharedPreferences
import kotlin.math.abs
import org.json.JSONArray
import org.json.JSONObject

const val CHORD_BOOK_PREFS_NAME = "chord_book_preferences"

/**
 * Enharmonic accidental spelling preference.
 */
enum class EnharmonicPreference(val label: String) {
  AUTO("Auto"),
  SHARPS("♯ Sharps"),
  FLATS("♭ Flats"),
}

/**
 * Controls what text is drawn inside the dots on chord diagrams and fretboard views.
 */
enum class DotLabelMode(val label: String) {
  INTERVALS("Intervals"),
  NOTES("Notes"),
  FINGERS("Fingers"),
}

/**
 * Filter for neck position when browsing chord voicings.
 */
enum class NeckPositionFilter(val label: String, val minFret: Int, val maxFret: Int) {
  ALL("All Frets", 0, 15),
  OPEN_LOW("Open (0–4)", 0, 4),
  MID_NECK("Mid (4–8)", 4, 8),
  HIGH_NECK("High (8–15)", 8, 15),
}

/**
 * Supported stringed instrument families.
 */
enum class InstrumentFamily(
  val displayName: String,
  val shortName: String,
  val stringCount: Int,
) {
  GUITAR_6("6-String Guitar", "Guitar (6)", 6),
  GUITAR_7("7-String Guitar", "Guitar (7)", 7),
  BASS_4("4-String Bass", "Bass (4)", 4),
  BASS_5("5-String Bass", "Bass (5)", 5),
  UKULELE("Ukulele", "Ukulele", 4),
  BANJO_5("5-String Banjo", "Banjo (5)", 5),
  MANDOLIN("Mandolin", "Mandolin", 4),
}

/**
 * Represents a tuning preset or custom tuning for a stringed instrument.
 * [openStringMidi] is ordered from lowest-index string (0 = leftmost on vertical chord chart,
 * typically lowest pitch) to highest-index string (rightmost on vertical chord chart).
 */
data class ChordTuning(
  val id: String,
  val name: String,
  val instrument: InstrumentFamily,
  val openStringMidi: List<Int>,
  val isCustom: Boolean = false,
) {
  val stringCount: Int get() = openStringMidi.size

  val matchingTunerPreset: GuitarTuningPreset?
    get() = GuitarTuningPreset.fromKey(id)

  val displayLabel: String
    get() = matchingTunerPreset?.label ?: name.substringBefore(" (").trim()

  val tuningCategory: GuitarTuningCategory?
    get() = matchingTunerPreset?.category

  fun effectiveMidi(stringIndex: Int, capoFret: Int = 0): Int {
    return openStringMidi[stringIndex] + capoFret.coerceIn(0, 12)
  }

  fun formattedNotes(
    capoFret: Int = 0,
    preference: EnharmonicPreference = EnharmonicPreference.SHARPS,
  ): String {
    return openStringMidi.indices.joinToString(" ") { idx ->
      val midi = effectiveMidi(idx, capoFret)
      ChordBookEngine.formatPitchClass(midi.mod(12), preference)
    }
  }

  fun formattedNotesWithOctaves(
    capoFret: Int = 0,
    preference: EnharmonicPreference = EnharmonicPreference.SHARPS,
  ): List<String> {
    return openStringMidi.indices.map { idx ->
      val midi = effectiveMidi(idx, capoFret)
      ChordBookEngine.formatMidiNote(midi, preference)
    }
  }

  fun matches(
    query: String,
    preference: EnharmonicPreference = EnharmonicPreference.AUTO,
  ): Boolean {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return true
    if (matchingTunerPreset?.matches(trimmed) == true) return true
    val terms = trimmed.lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
    val notesSpace = formattedNotes(0, preference)
    val notesCompact = notesSpace.replace(" ", "")
    val notesOctaves = formattedNotesWithOctaves(0, preference).joinToString(" ")
    val catName = tuningCategory?.displayName.orEmpty()
    val haystack =
      "$name $displayLabel ${instrument.displayName} ${instrument.shortName} $notesSpace $notesCompact $notesOctaves $catName"
        .lowercase()
    return terms.all { haystack.contains(it) }
  }

  fun toJson(): JSONObject {
    return JSONObject().apply {
      put("id", id)
      put("name", name)
      put("instrument", instrument.name)
      put("openStringMidi", JSONArray(openStringMidi))
      put("isCustom", isCustom)
    }
  }

  companion object {
    fun fromJson(obj: JSONObject): ChordTuning? {
      val id = obj.optString("id", "").takeIf { it.isNotBlank() } ?: return null
      val name = obj.optString("name", "").takeIf { it.isNotBlank() } ?: return null
      val instName = obj.optString("instrument", InstrumentFamily.GUITAR_6.name)
      val instrument = try {
        InstrumentFamily.valueOf(instName)
      } catch (_: IllegalArgumentException) {
        InstrumentFamily.GUITAR_6
      }
      val arr = obj.optJSONArray("openStringMidi") ?: return null
      val midiList = mutableListOf<Int>()
      for (i in 0 until arr.length()) {
        midiList.add(arr.optInt(i, 40).coerceIn(20, 96))
      }
      if (midiList.size < 3) return null
      return ChordTuning(
        id = id,
        name = name,
        instrument = instrument,
        openStringMidi = midiList,
        isCustom = obj.optBoolean("isCustom", true),
      )
    }
  }
}

/**
 * Functional categories of chord qualities.
 */
enum class ChordCategory(val title: String) {
  TRIADS_AND_BASIC("Basic & Triads"),
  SEVENTHS("Sevenths"),
  SIXTHS_AND_ADD("6th & Add"),
  EXTENDED("Extended"),
  ALTERED("Altered & Jazz"),
}

/**
 * Defines a chord formula by its semitone [intervals] from the root, plus any
 * [optionalIntervals] that may be omitted on instruments with limited strings or compact shapes
 * (such as omitting the perfect 5th in 7th/9th/11th/13th voicings).
 */
enum class ChordQuality(
  val symbol: String,
  val fullName: String,
  val category: ChordCategory,
  val intervals: List<Int>,
  val optionalIntervals: Set<Int> = emptySet(),
) {
  // Basic & Triads
  MAJOR("", "Major", ChordCategory.TRIADS_AND_BASIC, listOf(0, 4, 7)),
  MINOR("m", "Minor", ChordCategory.TRIADS_AND_BASIC, listOf(0, 3, 7)),
  POWER_5("5", "Power (5th)", ChordCategory.TRIADS_AND_BASIC, listOf(0, 7)),
  SUS2("sus2", "Suspended 2nd", ChordCategory.TRIADS_AND_BASIC, listOf(0, 2, 7)),
  SUS4("sus4", "Suspended 4th", ChordCategory.TRIADS_AND_BASIC, listOf(0, 5, 7)),
  DIMINISHED("dim", "Diminished", ChordCategory.TRIADS_AND_BASIC, listOf(0, 3, 6)),
  AUGMENTED("aug", "Augmented", ChordCategory.TRIADS_AND_BASIC, listOf(0, 4, 8)),

  // Sevenths
  DOMINANT_7("7", "Dominant 7th", ChordCategory.SEVENTHS, listOf(0, 4, 7, 10), setOf(7)),
  MAJOR_7("maj7", "Major 7th", ChordCategory.SEVENTHS, listOf(0, 4, 7, 11), setOf(7)),
  MINOR_7("m7", "Minor 7th", ChordCategory.SEVENTHS, listOf(0, 3, 7, 10), setOf(7)),
  MINOR_MAJOR_7("m(maj7)", "Minor-Major 7th", ChordCategory.SEVENTHS, listOf(0, 3, 7, 11), setOf(7)),
  HALF_DIMINISHED("m7b5", "Half-Diminished (m7♭5)", ChordCategory.SEVENTHS, listOf(0, 3, 6, 10)),
  DIMINISHED_7("dim7", "Diminished 7th", ChordCategory.SEVENTHS, listOf(0, 3, 6, 9)),
  DOMINANT_7_SUS4("7sus4", "7th Suspended 4th", ChordCategory.SEVENTHS, listOf(0, 5, 7, 10), setOf(7)),
  DOMINANT_7_SUS2("7sus2", "7th Suspended 2nd", ChordCategory.SEVENTHS, listOf(0, 2, 7, 10), setOf(7)),
  DOMINANT_7_FLAT_5("7b5", "Dominant 7♭5", ChordCategory.SEVENTHS, listOf(0, 4, 6, 10)),
  DOMINANT_7_SHARP_5("7#5", "Augmented 7th (7♯5)", ChordCategory.SEVENTHS, listOf(0, 4, 8, 10)),
  MAJOR_7_SHARP_5("maj7#5", "Major 7th ♯5", ChordCategory.SEVENTHS, listOf(0, 4, 8, 11)),

  // 6th & Add Chords
  MAJOR_6("6", "Major 6th", ChordCategory.SIXTHS_AND_ADD, listOf(0, 4, 7, 9), setOf(7)),
  MINOR_6("m6", "Minor 6th", ChordCategory.SIXTHS_AND_ADD, listOf(0, 3, 7, 9), setOf(7)),
  SIX_NINE("6/9", "Six-Nine (6/9)", ChordCategory.SIXTHS_AND_ADD, listOf(0, 4, 7, 9, 2), setOf(7)),
  ADD_9("add9", "Major Add 9", ChordCategory.SIXTHS_AND_ADD, listOf(0, 4, 7, 2), setOf(7)),
  MINOR_ADD_9("m(add9)", "Minor Add 9", ChordCategory.SIXTHS_AND_ADD, listOf(0, 3, 7, 2), setOf(7)),
  ADD_11("add11", "Major Add 11", ChordCategory.SIXTHS_AND_ADD, listOf(0, 4, 7, 5), setOf(7)),

  // Extended (9ths, 11ths, 13ths)
  DOMINANT_9("9", "Dominant 9th", ChordCategory.EXTENDED, listOf(0, 4, 7, 10, 2), setOf(7)),
  MAJOR_9("maj9", "Major 9th", ChordCategory.EXTENDED, listOf(0, 4, 7, 11, 2), setOf(7)),
  MINOR_9("m9", "Minor 9th", ChordCategory.EXTENDED, listOf(0, 3, 7, 10, 2), setOf(7)),
  DOMINANT_11("11", "Dominant 11th", ChordCategory.EXTENDED, listOf(0, 4, 7, 10, 2, 5), setOf(4, 7, 2)),
  MINOR_11("m11", "Minor 11th", ChordCategory.EXTENDED, listOf(0, 3, 7, 10, 2, 5), setOf(7, 2)),
  DOMINANT_13("13", "Dominant 13th", ChordCategory.EXTENDED, listOf(0, 4, 7, 10, 2, 9), setOf(7, 2)),
  MAJOR_13("maj13", "Major 13th", ChordCategory.EXTENDED, listOf(0, 4, 7, 11, 2, 9), setOf(7, 2)),
  MINOR_13("m13", "Minor 13th", ChordCategory.EXTENDED, listOf(0, 3, 7, 10, 2, 9), setOf(7, 2)),

  // Altered & Jazz
  DOMINANT_7_FLAT_9("7b9", "Dominant 7♭9", ChordCategory.ALTERED, listOf(0, 4, 7, 10, 1), setOf(7)),
  DOMINANT_7_SHARP_9("7#9", "Dominant 7♯9", ChordCategory.ALTERED, listOf(0, 4, 7, 10, 3), setOf(7)),
  DOMINANT_7_SHARP_11("7#11", "Lydian Dominant (7♯11)", ChordCategory.ALTERED, listOf(0, 4, 7, 10, 6), setOf(7)),
  DOMINANT_7_FLAT_13("7b13", "Dominant 7♭13", ChordCategory.ALTERED, listOf(0, 4, 7, 10, 8), setOf(7)),
  MAJOR_7_SHARP_11("maj7#11", "Major 7th ♯11", ChordCategory.ALTERED, listOf(0, 4, 7, 11, 6), setOf(7)),
}

/**
 * Represents a finger barre across multiple strings on a single fret.
 */
data class ChordBarre(
  val fret: Int,
  val fromString: Int,
  val toString: Int,
  val finger: Int = 1,
)

/**
 * Represents a playable chord voicing and its anatomical fingering on a given tuning.
 *
 * [frets] has one entry per string (`-1` = muted `X`, `0` = open `O` relative to capo,
 * `1..15` = fret number relative to capo).
 * [fingers] has one entry per string (`0` = muted or open, `1` = index, `2` = middle,
 * `3` = ring, `4` = pinky).
 */
data class ChordVoicing(
  val rootPitchClass: Int,
  val quality: ChordQuality,
  val bassPitchClass: Int,
  val frets: List<Int>,
  val fingers: List<Int>,
  val barres: List<ChordBarre>,
  val baseFret: Int,
  val span: Int,
  val score: Int,
) {
  val isSlashOrInversion: Boolean get() = bassPitchClass != rootPitchClass

  val compactNotation: String
    get() = frets.joinToString(
      separator = if (frets.any { it >= 10 }) "-" else "",
    ) { fret ->
      when {
        fret < 0 -> "x"
        else -> fret.toString()
      }
    }
}

/**
 * Result of reverse chord analysis from notes selected on the virtual fretboard.
 */
data class ReverseChordMatch(
  val rootPitchClass: Int,
  val quality: ChordQuality,
  val bassPitchClass: Int,
  val isExactMatch: Boolean,
  val isRootBass: Boolean,
  val matchedIntervals: List<Int>,
  val missingIntervals: List<Int>,
  val score: Int,
) {
  fun formatChordName(preference: EnharmonicPreference = EnharmonicPreference.AUTO): String {
    return ChordBookEngine.formatChordSymbol(
      rootPitchClass = rootPitchClass,
      quality = quality,
      bassPitchClass = if (bassPitchClass != rootPitchClass) bassPitchClass else null,
      preference = preference,
    )
  }
}

/**
 * Categories of scales and modes.
 */
enum class ScaleCategory(val title: String) {
  MAJOR_AND_MODES("Major & Modes"),
  MINOR_VARIANTS("Minor Variants"),
  PENTATONIC_AND_BLUES("Pentatonic & Blues"),
  SYMMETRIC_AND_EXOTIC("Symmetric & Exotic"),
}

/**
 * Defines a musical scale or mode by its semitone [intervals] from the root.
 */
enum class ScaleType(
  val displayName: String,
  val category: ScaleCategory,
  val intervals: List<Int>,
  val formulaLabels: List<String>,
) {
  // Major & Modes
  MAJOR(
    displayName = "Major (Ionian)",
    category = ScaleCategory.MAJOR_AND_MODES,
    intervals = listOf(0, 2, 4, 5, 7, 9, 11),
    formulaLabels = listOf("R", "2", "3", "4", "5", "6", "7"),
  ),
  DORIAN(
    displayName = "Dorian",
    category = ScaleCategory.MAJOR_AND_MODES,
    intervals = listOf(0, 2, 3, 5, 7, 9, 10),
    formulaLabels = listOf("R", "2", "♭3", "4", "5", "6", "♭7"),
  ),
  PHRYGIAN(
    displayName = "Phrygian",
    category = ScaleCategory.MAJOR_AND_MODES,
    intervals = listOf(0, 1, 3, 5, 7, 8, 10),
    formulaLabels = listOf("R", "♭2", "♭3", "4", "5", "♭6", "♭7"),
  ),
  LYDIAN(
    displayName = "Lydian",
    category = ScaleCategory.MAJOR_AND_MODES,
    intervals = listOf(0, 2, 4, 6, 7, 9, 11),
    formulaLabels = listOf("R", "2", "3", "♯4", "5", "6", "7"),
  ),
  MIXOLYDIAN(
    displayName = "Mixolydian",
    category = ScaleCategory.MAJOR_AND_MODES,
    intervals = listOf(0, 2, 4, 5, 7, 9, 10),
    formulaLabels = listOf("R", "2", "3", "4", "5", "6", "♭7"),
  ),
  NATURAL_MINOR(
    displayName = "Natural Minor (Aeolian)",
    category = ScaleCategory.MAJOR_AND_MODES,
    intervals = listOf(0, 2, 3, 5, 7, 8, 10),
    formulaLabels = listOf("R", "2", "♭3", "4", "5", "♭6", "♭7"),
  ),
  LOCRIAN(
    displayName = "Locrian",
    category = ScaleCategory.MAJOR_AND_MODES,
    intervals = listOf(0, 1, 3, 5, 6, 8, 10),
    formulaLabels = listOf("R", "♭2", "♭3", "4", "♭5", "♭6", "♭7"),
  ),

  // Minor Variants
  HARMONIC_MINOR(
    displayName = "Harmonic Minor",
    category = ScaleCategory.MINOR_VARIANTS,
    intervals = listOf(0, 2, 3, 5, 7, 8, 11),
    formulaLabels = listOf("R", "2", "♭3", "4", "5", "♭6", "7"),
  ),
  MELODIC_MINOR(
    displayName = "Melodic Minor",
    category = ScaleCategory.MINOR_VARIANTS,
    intervals = listOf(0, 2, 3, 5, 7, 9, 11),
    formulaLabels = listOf("R", "2", "♭3", "4", "5", "6", "7"),
  ),
  PHRYGIAN_DOMINANT(
    displayName = "Phrygian Dominant",
    category = ScaleCategory.MINOR_VARIANTS,
    intervals = listOf(0, 1, 4, 5, 7, 8, 10),
    formulaLabels = listOf("R", "♭2", "3", "4", "5", "♭6", "♭7"),
  ),
  LYDIAN_DOMINANT(
    displayName = "Lydian Dominant",
    category = ScaleCategory.MINOR_VARIANTS,
    intervals = listOf(0, 2, 4, 6, 7, 9, 10),
    formulaLabels = listOf("R", "2", "3", "♯4", "5", "6", "♭7"),
  ),

  // Pentatonic & Blues
  MAJOR_PENTATONIC(
    displayName = "Major Pentatonic",
    category = ScaleCategory.PENTATONIC_AND_BLUES,
    intervals = listOf(0, 2, 4, 7, 9),
    formulaLabels = listOf("R", "2", "3", "5", "6"),
  ),
  MINOR_PENTATONIC(
    displayName = "Minor Pentatonic",
    category = ScaleCategory.PENTATONIC_AND_BLUES,
    intervals = listOf(0, 3, 5, 7, 10),
    formulaLabels = listOf("R", "♭3", "4", "5", "♭7"),
  ),
  BLUES(
    displayName = "Minor Blues",
    category = ScaleCategory.PENTATONIC_AND_BLUES,
    intervals = listOf(0, 3, 5, 6, 7, 10),
    formulaLabels = listOf("R", "♭3", "4", "♭5", "5", "♭7"),
  ),
  MAJOR_BLUES(
    displayName = "Major Blues",
    category = ScaleCategory.PENTATONIC_AND_BLUES,
    intervals = listOf(0, 2, 3, 4, 7, 9),
    formulaLabels = listOf("R", "2", "♭3", "3", "5", "6"),
  ),

  // Symmetric & Exotic
  WHOLE_TONE(
    displayName = "Whole Tone",
    category = ScaleCategory.SYMMETRIC_AND_EXOTIC,
    intervals = listOf(0, 2, 4, 6, 8, 10),
    formulaLabels = listOf("R", "2", "3", "♯4", "♯5", "♭7"),
  ),
  DIMINISHED_HALF_WHOLE(
    displayName = "Diminished (Half-Whole)",
    category = ScaleCategory.SYMMETRIC_AND_EXOTIC,
    intervals = listOf(0, 1, 3, 4, 6, 7, 9, 10),
    formulaLabels = listOf("R", "♭2", "♯2", "3", "♯4", "5", "6", "♭7"),
  ),
  DIMINISHED_WHOLE_HALF(
    displayName = "Diminished (Whole-Half)",
    category = ScaleCategory.SYMMETRIC_AND_EXOTIC,
    intervals = listOf(0, 2, 3, 5, 6, 8, 9, 11),
    formulaLabels = listOf("R", "2", "♭3", "4", "♭5", "♭6", "6", "7"),
  ),
  BEBOP_DOMINANT(
    displayName = "Bebop Dominant",
    category = ScaleCategory.SYMMETRIC_AND_EXOTIC,
    intervals = listOf(0, 2, 4, 5, 7, 9, 10, 11),
    formulaLabels = listOf("R", "2", "3", "4", "5", "6", "♭7", "7"),
  ),
  HUNGARIAN_MINOR(
    displayName = "Hungarian Minor",
    category = ScaleCategory.SYMMETRIC_AND_EXOTIC,
    intervals = listOf(0, 2, 3, 6, 7, 8, 11),
    formulaLabels = listOf("R", "2", "♭3", "♯4", "5", "♭6", "7"),
  ),
}

/**
 * Represents a positional scale box along the fretboard (e.g., CAGED / 5-Box system).
 */
data class ScaleBoxPosition(
  val boxNumber: Int,
  val label: String,
  val startFret: Int,
  val endFret: Int,
)

/**
 * Represents a harmonized diatonic chord built on a degree of a parent scale.
 */
data class DiatonicChord(
  val degreeNumber: Int,
  val romanNumeral: String,
  val rootPitchClass: Int,
  val triadQuality: ChordQuality,
  val seventhQuality: ChordQuality,
  val intervalLabel: String,
)

/**
 * Result of scale matching (from Reverse Search or Progression Scale Extraction).
 */
data class ScaleMatch(
  val rootPitchClass: Int,
  val scaleType: ScaleType,
  val matchedPitchClasses: Set<Int>,
  val totalInputPitchClasses: Int,
  val coverageRatio: Float,
  val score: Int,
  val extraScalePitchClasses: Set<Int> = emptySet(),
  val outsideInputPitchClasses: Set<Int> = emptySet(),
) {
  val scalePitchClasses: List<Int>
    get() = scaleType.intervals.map { (rootPitchClass + it).mod(12) }

  val isExactScale: Boolean
    get() = matchedPitchClasses.size == totalInputPitchClasses &&
      extraScalePitchClasses.isEmpty() &&
      outsideInputPitchClasses.isEmpty()

  val isFullCoverage: Boolean
    get() = matchedPitchClasses.size == totalInputPitchClasses && outsideInputPitchClasses.isEmpty()

  fun formatScaleName(preference: EnharmonicPreference = EnharmonicPreference.AUTO): String {
    val rootName = ChordBookEngine.formatPitchClass(rootPitchClass, preference, rootPitchClass)
    return "$rootName ${scaleType.displayName}"
  }
}

/**
 * A chord entry pinned into a user's progression / songbook.
 */
data class SavedProgressionChord(
  val id: String,
  val rootPitchClass: Int,
  val quality: ChordQuality,
  val bassPitchClass: Int = rootPitchClass,
  val preferredFrets: List<Int>? = null,
) {
  fun formatName(preference: EnharmonicPreference = EnharmonicPreference.AUTO): String {
    return ChordBookEngine.formatChordSymbol(
      rootPitchClass = rootPitchClass,
      quality = quality,
      bassPitchClass = if (bassPitchClass != rootPitchClass) bassPitchClass else null,
      preference = preference,
    )
  }

  fun transpose(semitones: Int): SavedProgressionChord {
    val newRoot = (rootPitchClass + semitones).mod(12)
    val newBass = (bassPitchClass + semitones).mod(12)
    return copy(
      rootPitchClass = newRoot,
      bassPitchClass = newBass,
      preferredFrets = null, // Recompute optimal voicing in the new key
    )
  }

  fun toJson(): JSONObject {
    return JSONObject().apply {
      put("id", id)
      put("rootPitchClass", rootPitchClass)
      put("quality", quality.name)
      put("bassPitchClass", bassPitchClass)
      if (preferredFrets != null) {
        put("preferredFrets", JSONArray(preferredFrets))
      }
    }
  }

  companion object {
    fun fromJson(obj: JSONObject): SavedProgressionChord? {
      val id = obj.optString("id", "").takeIf { it.isNotBlank() } ?: return null
      val root = obj.optInt("rootPitchClass", 0).mod(12)
      val qualityName = obj.optString("quality", ChordQuality.MAJOR.name)
      val quality = try {
        ChordQuality.valueOf(qualityName)
      } catch (_: IllegalArgumentException) {
        ChordQuality.MAJOR
      }
      val bass = obj.optInt("bassPitchClass", root).mod(12)
      val fretsArr = obj.optJSONArray("preferredFrets")
      val preferredFrets = if (fretsArr != null) {
        List(fretsArr.length()) { idx -> fretsArr.optInt(idx, -1) }
      } else {
        null
      }
      return SavedProgressionChord(
        id = id,
        rootPitchClass = root,
        quality = quality,
        bassPitchClass = bass,
        preferredFrets = preferredFrets,
      )
    }
  }
}

/**
 * A named chord progression / song saved in the user's songbook.
 */
data class ChordProgression(
  val id: String,
  val name: String,
  val chords: List<SavedProgressionChord>,
) {
  fun toJson(): JSONObject {
    return JSONObject().apply {
      put("id", id)
      put("name", name)
      val arr = JSONArray()
      chords.forEach { arr.put(it.toJson()) }
      put("chords", arr)
    }
  }

  companion object {
    fun fromJson(obj: JSONObject): ChordProgression? {
      val id = obj.optString("id", "").takeIf { it.isNotBlank() } ?: return null
      val name = obj.optString("name", "Untitled Progression")
      val arr = obj.optJSONArray("chords") ?: JSONArray()
      val chords = mutableListOf<SavedProgressionChord>()
      for (i in 0 until arr.length()) {
        val chordObj = arr.optJSONObject(i) ?: continue
        SavedProgressionChord.fromJson(chordObj)?.let { chords.add(it) }
      }
      return ChordProgression(id = id, name = name, chords = chords)
    }
  }
}

/**
 * Core music theory, combinatorial voicing solver, reverse chord/scale identifier,
 * and scale harmonization engine inspired by Chord!
 */
object ChordBookEngine {

  private val SHARP_NAMES = arrayOf("C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B")
  private val FLAT_NAMES = arrayOf("C", "D♭", "D", "E♭", "E", "F", "G♭", "G", "A♭", "A", "B♭", "B")

  // Pitch classes that conventionally prefer flat key signatures (F=5, Bb=10, Eb=3, Ab=8, Db=1, Gb=6)
  private val FLAT_PREFERRED_ROOTS = setOf(1, 3, 5, 8, 10)

  val BUILT_IN_TUNINGS: List<ChordTuning> = listOf(
    // 6-String Guitar — Standard & lowered standard tunings (matching GuitarTuningPreset)
    ChordTuning("guitar_std", "Standard (E A D G B E)", InstrumentFamily.GUITAR_6, listOf(40, 45, 50, 55, 59, 64)),
    ChordTuning("guitar_half_down", "Eb Standard (E♭ A♭ D♭ G♭ B♭ E♭)", InstrumentFamily.GUITAR_6, listOf(39, 44, 49, 54, 58, 63)),
    ChordTuning("guitar_full_down", "D Standard (D G C F A D)", InstrumentFamily.GUITAR_6, listOf(38, 43, 48, 53, 57, 62)),
    ChordTuning("guitar_csharp_std", "C# Standard (D♭ G♭ B E A♭ D♭)", InstrumentFamily.GUITAR_6, listOf(37, 42, 47, 52, 56, 61)),
    ChordTuning("guitar_c_std", "C Standard (C F B♭ E♭ G C)", InstrumentFamily.GUITAR_6, listOf(36, 41, 46, 51, 55, 60)),
    ChordTuning("guitar_b_std", "B Standard (B E A D F♯ B)", InstrumentFamily.GUITAR_6, listOf(35, 40, 45, 50, 54, 59)),

    // 6-String Guitar — Drop tunings
    ChordTuning("guitar_drop_d", "Drop D (D A D G B E)", InstrumentFamily.GUITAR_6, listOf(38, 45, 50, 55, 59, 64)),
    ChordTuning("guitar_drop_csharp", "Drop C# (D♭ A♭ D♭ G♭ B♭ E♭)", InstrumentFamily.GUITAR_6, listOf(37, 44, 49, 54, 58, 63)),
    ChordTuning("guitar_drop_c", "Drop C (C G C F A D)", InstrumentFamily.GUITAR_6, listOf(36, 43, 48, 53, 57, 62)),
    ChordTuning("guitar_drop_b", "Drop B (B F♯ B E G♯ C♯)", InstrumentFamily.GUITAR_6, listOf(35, 42, 47, 52, 56, 61)),
    ChordTuning("guitar_drop_a", "Drop A (A E A D F♯ B)", InstrumentFamily.GUITAR_6, listOf(33, 40, 45, 50, 54, 59)),
    ChordTuning("guitar_double_drop_d", "Double Drop D (D A D G B D)", InstrumentFamily.GUITAR_6, listOf(38, 45, 50, 55, 59, 62)),

    // 6-String Guitar — Open tunings
    ChordTuning("guitar_open_g", "Open G (D G D G B D)", InstrumentFamily.GUITAR_6, listOf(38, 43, 50, 55, 59, 62)),
    ChordTuning("guitar_open_d", "Open D (D A D F♯ A D)", InstrumentFamily.GUITAR_6, listOf(38, 45, 50, 54, 57, 62)),
    ChordTuning("guitar_open_e", "Open E (E B E G♯ B E)", InstrumentFamily.GUITAR_6, listOf(40, 47, 52, 56, 59, 64)),
    ChordTuning("guitar_open_a", "Open A (E A E A C♯ E)", InstrumentFamily.GUITAR_6, listOf(40, 45, 52, 57, 61, 64)),
    ChordTuning("guitar_open_c", "Open C (C G C G C E)", InstrumentFamily.GUITAR_6, listOf(36, 43, 48, 55, 60, 64)),
    ChordTuning("guitar_open_dm", "Open D Minor (D A D F A D)", InstrumentFamily.GUITAR_6, listOf(38, 45, 50, 53, 57, 62)),

    // 6-String Guitar — Modal & alternative tunings
    ChordTuning("guitar_dadgad", "DADGAD (D A D G A D)", InstrumentFamily.GUITAR_6, listOf(38, 45, 50, 55, 57, 62)),
    ChordTuning("guitar_all_fourths", "All Fourths (E A D G C F)", InstrumentFamily.GUITAR_6, listOf(40, 45, 50, 55, 60, 65)),
    ChordTuning("guitar_nst", "New Standard (C G D A E G)", InstrumentFamily.GUITAR_6, listOf(36, 43, 50, 57, 64, 67)),
    ChordTuning("guitar_nashville", "Nashville (E A D G B E)", InstrumentFamily.GUITAR_6, listOf(52, 57, 62, 67, 59, 64)),

    // 7-String Guitar
    ChordTuning("guitar7_std", "7-String Standard (B E A D G B E)", InstrumentFamily.GUITAR_7, listOf(35, 40, 45, 50, 55, 59, 64)),
    ChordTuning("guitar7_drop_a", "7-String Drop A (A E A D G B E)", InstrumentFamily.GUITAR_7, listOf(33, 40, 45, 50, 55, 59, 64)),

    // 4-String Bass
    ChordTuning("bass4_std", "4-String Standard (E A D G)", InstrumentFamily.BASS_4, listOf(28, 33, 38, 43)),
    ChordTuning("bass4_drop_d", "4-String Drop D (D A D G)", InstrumentFamily.BASS_4, listOf(26, 33, 38, 43)),
    ChordTuning("bass4_half_down", "4-String Half Step Down (E♭ A♭ D♭ G♭)", InstrumentFamily.BASS_4, listOf(27, 32, 37, 42)),

    // 5-String Bass
    ChordTuning("bass5_std", "5-String Standard (B E A D G)", InstrumentFamily.BASS_5, listOf(23, 28, 33, 38, 43)),
    ChordTuning("bass5_tenor", "5-String High C (E A D G C)", InstrumentFamily.BASS_5, listOf(28, 33, 38, 43, 48)),

    // Ukulele
    ChordTuning("uke_std", "Standard C6 High-G (G C E A)", InstrumentFamily.UKULELE, listOf(67, 60, 64, 69)),
    ChordTuning("uke_low_g", "Low-G Tuning (G3 C4 E4 A4)", InstrumentFamily.UKULELE, listOf(55, 60, 64, 69)),
    ChordTuning("uke_baritone", "Baritone (D G B E)", InstrumentFamily.UKULELE, listOf(50, 55, 59, 64)),
    ChordTuning("uke_d_tuning", "Traditional D6 (A D F♯ B)", InstrumentFamily.UKULELE, listOf(69, 62, 66, 71)),

    // 5-String Banjo
    ChordTuning("banjo_open_g", "Open G (g D G B D)", InstrumentFamily.BANJO_5, listOf(67, 50, 55, 59, 62)),
    ChordTuning("banjo_double_c", "Double C (g C G C D)", InstrumentFamily.BANJO_5, listOf(67, 48, 55, 60, 62)),
    ChordTuning("banjo_sawmill", "Sawmill / Mountain Modal (g D G C D)", InstrumentFamily.BANJO_5, listOf(67, 50, 55, 60, 62)),

    // Mandolin
    ChordTuning("mandolin_std", "Standard Fifths (G D A E)", InstrumentFamily.MANDOLIN, listOf(55, 62, 69, 76)),
    ChordTuning("mandolin_gdad", "GDAD Celtic (G D A D)", InstrumentFamily.MANDOLIN, listOf(55, 62, 69, 74)),
  )

  fun isTuningFavorite(
    tuning: ChordTuning,
    favoriteIds: Set<String>,
  ): Boolean {
    val preset = tuning.matchingTunerPreset
    return tuning.id in favoriteIds || (preset != null && (preset.name in favoriteIds || preset.chordTuningId in favoriteIds))
  }

  fun toggleFavoriteTuning(
    currentFavoriteIds: Set<String>,
    tuning: ChordTuning,
  ): Set<String> {
    val canonicalId = tuning.matchingTunerPreset?.chordTuningId ?: tuning.id
    val presetName = tuning.matchingTunerPreset?.name
    val isCurrentlyFav = isTuningFavorite(tuning, currentFavoriteIds)
    val cleaned = currentFavoriteIds.filterNot {
      it == tuning.id || it == canonicalId || (presetName != null && it == presetName)
    }.toSet()
    return if (isCurrentlyFav) cleaned else cleaned + canonicalId
  }

  fun searchTunings(
    catalog: List<ChordTuning> = BUILT_IN_TUNINGS,
    query: String = "",
    instrument: InstrumentFamily? = null,
    category: GuitarTuningCategory? = null,
    favoritesOnly: Boolean = false,
    favoriteIds: Set<String> = emptySet(),
    preference: EnharmonicPreference = EnharmonicPreference.AUTO,
  ): List<ChordTuning> {
    val filtered = catalog.filter { tuning ->
      (!favoritesOnly || isTuningFavorite(tuning, favoriteIds)) &&
        (instrument == null || tuning.instrument == instrument) &&
        (category == null || tuning.tuningCategory == category) &&
        tuning.matches(query, preference)
    }
    return filtered.sortedByDescending { isTuningFavorite(it, favoriteIds) }
  }

  fun defaultProgressions(): List<ChordProgression> = listOf(
    ChordProgression(
      id = "prog_axis",
      name = "Axis Progression",
      chords = listOf(
        SavedProgressionChord("ax1", 0, ChordQuality.MAJOR),
        SavedProgressionChord("ax2", 7, ChordQuality.MAJOR),
        SavedProgressionChord("ax3", 9, ChordQuality.MINOR),
        SavedProgressionChord("ax4", 5, ChordQuality.MAJOR),
      ),
    ),
    ChordProgression(
      id = "prog_royal_road",
      name = "Royal Road Progression",
      chords = listOf(
        SavedProgressionChord("rr1", 5, ChordQuality.MAJOR_7),
        SavedProgressionChord("rr2", 7, ChordQuality.DOMINANT_7),
        SavedProgressionChord("rr3", 4, ChordQuality.MINOR_7),
        SavedProgressionChord("rr4", 9, ChordQuality.MINOR_7),
      ),
    ),
    ChordProgression(
      id = "prog_flamenco",
      name = "Flamenco Progression",
      chords = listOf(
        SavedProgressionChord("fl1", 9, ChordQuality.MINOR),
        SavedProgressionChord("fl2", 7, ChordQuality.MAJOR),
        SavedProgressionChord("fl3", 5, ChordQuality.MAJOR),
        SavedProgressionChord("fl4", 4, ChordQuality.MAJOR),
      ),
    ),
  )

  /**
   * Formats a pitch class (`0..11`) according to [preference] and optional [contextRoot].
   */
  fun formatPitchClass(
    pitchClass: Int,
    preference: EnharmonicPreference = EnharmonicPreference.AUTO,
    contextRoot: Int = pitchClass,
  ): String {
    val pc = pitchClass.mod(12)
    val useFlats = when (preference) {
      EnharmonicPreference.SHARPS -> false
      EnharmonicPreference.FLATS -> true
      EnharmonicPreference.AUTO -> contextRoot.mod(12) in FLAT_PREFERRED_ROOTS
    }
    return if (useFlats) FLAT_NAMES[pc] else SHARP_NAMES[pc]
  }

  /**
   * Formats a MIDI note number (e.g. `40` -> `"E2"`, `60` -> `"C4"`).
   */
  fun formatMidiNote(
    midi: Int,
    preference: EnharmonicPreference = EnharmonicPreference.SHARPS,
  ): String {
    val pc = midi.mod(12)
    val octave = (midi / 12) - 1
    return "${formatPitchClass(pc, preference)}$octave"
  }

  /**
   * Formats a full chord symbol (e.g., `Cmaj7`, `F♯m7♭5`, `D/F♯`).
   */
  fun formatChordSymbol(
    rootPitchClass: Int,
    quality: ChordQuality,
    bassPitchClass: Int? = null,
    preference: EnharmonicPreference = EnharmonicPreference.AUTO,
  ): String {
    val rootStr = formatPitchClass(rootPitchClass, preference, rootPitchClass)
    val symbolDisplay = quality.symbol
      .replace("b", "♭")
      .replace("#", "♯")
    val base = "$rootStr$symbolDisplay"
    val cleanBass = bassPitchClass?.mod(12)
    return if (cleanBass != null && cleanBass != rootPitchClass.mod(12)) {
      val bassStr = formatPitchClass(cleanBass, preference, rootPitchClass)
      "$base/$bassStr"
    } else {
      base
    }
  }

  /**
   * Returns a human-readable interval label (e.g., `"R"`, `"3"`, `"♭3"`, `"5"`, `"♭7"`, `"9"`)
   * for [semitoneFromRoot] within the context of an optional [quality].
   */
  fun intervalName(semitoneFromRoot: Int, quality: ChordQuality? = null): String {
    val semi = semitoneFromRoot.mod(12)
    if (quality != null) {
      // Context-sensitive compound interval names (9, 11, 13) when chord quality uses extensions
      when (semi) {
        1 -> if (1 in quality.intervals) return "♭9"
        2 -> if (quality.category == ChordCategory.EXTENDED || quality == ChordQuality.ADD_9 ||
          quality == ChordQuality.MINOR_ADD_9 || quality == ChordQuality.SIX_NINE
        ) {
          return "9"
        }
        3 -> if (quality == ChordQuality.DOMINANT_7_SHARP_9) return "♯9"
        5 -> if (quality == ChordQuality.DOMINANT_11 || quality == ChordQuality.MINOR_11 ||
          quality == ChordQuality.ADD_11
        ) {
          return "11"
        }
        6 -> if (quality == ChordQuality.DOMINANT_7_SHARP_11 || quality == ChordQuality.MAJOR_7_SHARP_11) {
          return "♯11"
        }
        8 -> if (quality == ChordQuality.DOMINANT_7_FLAT_13) return "♭13"
        9 -> if (quality == ChordQuality.DOMINANT_13 || quality == ChordQuality.MAJOR_13 ||
          quality == ChordQuality.MINOR_13
        ) {
          return "13"
        }
      }
    }
    return when (semi) {
      0 -> "R"
      1 -> "♭2"
      2 -> "2"
      3 -> "♭3"
      4 -> "3"
      5 -> "4"
      6 -> "♭5"
      7 -> "5"
      8 -> "♯5"
      9 -> "6"
      10 -> "♭7"
      11 -> "7"
      else -> "R"
    }
  }

  /**
   * Computes the pitch classes belonging to `(rootPitchClass, quality)`.
   */
  fun chordPitchClasses(rootPitchClass: Int, quality: ChordQuality): List<Int> {
    val root = rootPitchClass.mod(12)
    return quality.intervals.map { (root + it).mod(12) }
  }

  /**
   * Dynamically generates and scores playable chord voicings and fingerings for any
   * [tuning], [capoFret], [rootPitchClass], [quality], and optional [slashBassPitchClass].
   */
  fun generateChordVoicings(
    rootPitchClass: Int,
    quality: ChordQuality,
    tuning: ChordTuning = BUILT_IN_TUNINGS.first(),
    capoFret: Int = 0,
    slashBassPitchClass: Int? = null,
    positionFilter: NeckPositionFilter = NeckPositionFilter.ALL,
    allowInversions: Boolean = false,
    allowMutedInside: Boolean = false,
    maxResults: Int = 36,
  ): List<ChordVoicing> {
    val root = rootPitchClass.mod(12)
    val targetBass = slashBassPitchClass?.mod(12)
    val stringCount = tuning.stringCount
    val effectiveOpenMidi = IntArray(stringCount) { idx -> tuning.effectiveMidi(idx, capoFret) }

    val allIntervals = quality.intervals.map { it.mod(12) }.distinct()
    val requiredIntervals = allIntervals.filter { it !in quality.optionalIntervals.map { o -> o.mod(12) } }
      .toMutableSet()
    if (targetBass != null) {
      val bassInterval = (targetBass - root).mod(12)
      requiredIntervals.add(bassInterval)
    }

    // If instrument has fewer strings than required intervals (e.g., 4-string bass/ukulele with a 5-note chord),
    // keep the root and the most defining guide tones (3rd, 7th, extension) up to stringCount.
    val effectiveRequiredIntervals: Set<Int> = if (requiredIntervals.size <= stringCount) {
      requiredIntervals
    } else {
      val prioritized = mutableListOf(0)
      if (targetBass != null) prioritized.add((targetBass - root).mod(12))
      // Guide tones & extensions first (everything except 0 and 7)
      for (iv in requiredIntervals) {
        if (iv != 0 && iv != 7 && iv !in prioritized) prioritized.add(iv)
      }
      for (iv in requiredIntervals) {
        if (iv !in prioritized) prioritized.add(iv)
      }
      prioritized.take(stringCount).toSet()
    }

    val allowedIntervals = allIntervals.toMutableSet()
    if (targetBass != null) {
      allowedIntervals.add((targetBass - root).mod(12))
    }

    val minSoundedStrings = when {
      quality == ChordQuality.POWER_5 -> 2
      stringCount <= 4 -> minOf(3, stringCount)
      else -> minOf(4, effectiveRequiredIntervals.size.coerceAtLeast(3))
    }

    val deduplicatedVoicings = LinkedHashMap<List<Int>, ChordVoicing>()
    val currentFrets = IntArray(stringCount)

    // Search fret windows from baseFret = 0..12
    for (windowBase in 0..12) {
      val minWindowFret = if (windowBase == 0) 0 else windowBase
      val maxWindowFret = if (windowBase == 0) 4 else (windowBase + 3).coerceAtMost(15)

      if (maxWindowFret < positionFilter.minFret || minWindowFret > positionFilter.maxFret) {
        continue
      }

      // Precompute valid candidate frets for each string in this window
      val candidatesPerString = Array(stringCount) { s ->
        val openPc = effectiveOpenMidi[s].mod(12)
        val candidates = mutableListOf(-1) // -1 = muted
        // Allow open string in low windows (0..2) if open string is a valid chord tone
        if (windowBase <= 2) {
          val openInterval = (openPc - root).mod(12)
          if (openInterval in allowedIntervals) {
            candidates.add(0)
          }
        }
        val startF = maxOf(1, minWindowFret)
        for (f in startF..maxWindowFret) {
          val interval = (openPc + f - root).mod(12)
          if (interval in allowedIntervals) {
            candidates.add(f)
          }
        }
        candidates.toIntArray()
      }

      fun searchString(s: Int) {
        if (s == stringCount) {
          evaluateCandidate(
            frets = currentFrets,
            effectiveOpenMidi = effectiveOpenMidi,
            root = root,
            quality = quality,
            targetBass = targetBass,
            effectiveRequiredIntervals = effectiveRequiredIntervals,
            allIntervals = allIntervals,
            minSoundedStrings = minSoundedStrings,
            allowInversions = allowInversions,
            allowMutedInside = allowMutedInside,
            positionFilter = positionFilter,
            deduplicatedVoicings = deduplicatedVoicings,
          )
          return
        }
        for (cand in candidatesPerString[s]) {
          currentFrets[s] = cand
          searchString(s + 1)
        }
      }

      searchString(0)
    }

    return deduplicatedVoicings.values
      .sortedWith(
        compareByDescending<ChordVoicing> { it.score }
          .thenBy { it.baseFret }
          .thenBy { it.span },
      )
      .take(maxResults)
  }

  private fun evaluateCandidate(
    frets: IntArray,
    effectiveOpenMidi: IntArray,
    root: Int,
    quality: ChordQuality,
    targetBass: Int?,
    effectiveRequiredIntervals: Set<Int>,
    allIntervals: List<Int>,
    minSoundedStrings: Int,
    allowInversions: Boolean,
    allowMutedInside: Boolean,
    positionFilter: NeckPositionFilter,
    deduplicatedVoicings: LinkedHashMap<List<Int>, ChordVoicing>,
  ) {
    val stringCount = frets.size
    var soundedCount = 0
    var minFret = Int.MAX_VALUE
    var maxFret = 0
    var lowestMidi = Int.MAX_VALUE
    var bassPitchClass = -1
    var lowestSoundedStringIndex = -1
    var firstSoundedString = -1
    var lastSoundedString = -1
    var presentIntervalMask = 0

    for (s in 0 until stringCount) {
      val f = frets[s]
      if (f >= 0) {
        soundedCount++
        if (firstSoundedString == -1) firstSoundedString = s
        lastSoundedString = s
        if (f > 0) {
          if (f < minFret) minFret = f
          if (f > maxFret) maxFret = f
        }
        val midi = effectiveOpenMidi[s] + f
        val pc = midi.mod(12)
        val interval = (pc - root).mod(12)
        presentIntervalMask = presentIntervalMask or (1 shl interval)
        if (midi < lowestMidi) {
          lowestMidi = midi
          bassPitchClass = pc
          lowestSoundedStringIndex = s
        }
      }
    }

    if (soundedCount < minSoundedStrings) return

    // Check position filter against the voicing's effective base fret
    val effectiveBaseFret = if (minFret == Int.MAX_VALUE) 0 else minFret
    if (effectiveBaseFret < positionFilter.minFret || effectiveBaseFret > positionFilter.maxFret) {
      return
    }

    // Check required intervals
    for (req in effectiveRequiredIntervals) {
      if ((presentIntervalMask and (1 shl req)) == 0) return
    }

    // Check bass note requirement
    if (targetBass != null) {
      if (bassPitchClass != targetBass) return
    } else if (!allowInversions) {
      if (bassPitchClass != root) return
    }

    // Check interior muted strings between firstSoundedString and lastSoundedString
    var interiorMuteCount = 0
    for (s in (firstSoundedString + 1) until lastSoundedString) {
      if (frets[s] < 0) interiorMuteCount++
    }
    if (interiorMuteCount > 0 && !allowMutedInside) return
    if (interiorMuteCount > 1) return

    // Span check
    val span = if (minFret == Int.MAX_VALUE) 0 else (maxFret - minFret)
    if (span > 3) return

    val fretsList = frets.toList()
    if (deduplicatedVoicings.containsKey(fretsList)) return

    // Solve anatomical fingering & barres
    val fingeringResult = solveFingeringAndBarres(frets, minFret, maxFret) ?: return

    // Score voicing for ergonomic playability & musical completeness
    var score = 100

    // Reward more sounded strings
    score += soundedCount * 14

    // Reward having all optional intervals present (e.g., full 5th when playable)
    var optionalPresent = 0
    for (iv in allIntervals) {
      if ((presentIntervalMask and (1 shl iv)) != 0) optionalPresent++
    }
    score += optionalPresent * 10

    // Reward root in the bass (or exact target slash bass)
    if (targetBass != null && bassPitchClass == targetBass) {
      score += 35
    } else if (bassPitchClass == root) {
      score += 30
    }

    // Reward bass note on lower strings (strings 0, 1, 2)
    if (lowestSoundedStringIndex in 0..2) {
      score += (3 - lowestSoundedStringIndex) * 8
    }

    // Reward open strings in low position
    val openCount = frets.count { it == 0 }
    if (effectiveBaseFret <= 3) {
      score += openCount * 6
    }

    // Reward compact fret span and lower fret positions
    score -= span * 7
    score -= effectiveBaseFret * 3

    // Penalize interior muted strings
    score -= interiorMuteCount * 28

    // Penalize skipping the highest melody strings when bass strings are also muted
    val trailingMutes = (stringCount - 1) - lastSoundedString
    score -= trailingMutes * 6

    val displayBaseFret = if (maxFret <= 4) 1 else minFret

    deduplicatedVoicings[fretsList] = ChordVoicing(
      rootPitchClass = root,
      quality = quality,
      bassPitchClass = bassPitchClass,
      frets = fretsList,
      fingers = fingeringResult.first,
      barres = fingeringResult.second,
      baseFret = displayBaseFret,
      span = span,
      score = score,
    )
  }

  /**
   * Assigns anatomical fingers (`1..4`) and detects index-finger barres (`ChordBarre`).
   * Returns `null` if the shape cannot be physically fretted by 4 fingers.
   */
  internal fun solveFingeringAndBarres(
    frets: IntArray,
    minFret: Int,
    maxFret: Int,
  ): Pair<List<Int>, List<ChordBarre>>? {
    val stringCount = frets.size
    val fingers = IntArray(stringCount) { 0 }
    if (minFret == Int.MAX_VALUE) {
      // All strings are open or muted
      return fingers.toList() to emptyList()
    }

    val frettedStrings = (0 until stringCount).filter { frets[it] > 0 }
    val stringsAtMinFret = frettedStrings.filter { frets[it] == minFret }
    val stringsAboveMinFret = frettedStrings.filter { frets[it] > minFret }

    // Determine whether an index-finger barre at minFret is required or idiomatic
    val needsBarre = frettedStrings.size > 4 ||
      (stringsAtMinFret.size >= 2 && (stringsAboveMinFret.isNotEmpty() || stringsAtMinFret.size >= 3))

    if (needsBarre && stringsAtMinFret.size >= 2) {
      val barreStart = stringsAtMinFret.first()
      val barreEnd = stringsAtMinFret.last()

      // A barre from barreStart..barreEnd is only valid if NO string in between is open (0)
      // or fretted lower than minFret.
      val canBarre = (barreStart..barreEnd).all { s ->
        frets[s] < 0 || frets[s] >= minFret
      }

      if (canBarre && stringsAboveMinFret.size <= 3) {
        // Assign finger 1 to all strings fretted at minFret
        for (s in stringsAtMinFret) {
          fingers[s] = 1
        }
        // Sort remaining fretted strings by (fret ASC, stringIndex ASC) and assign fingers 2, 3, 4
        val sortedRemaining = stringsAboveMinFret.sortedWith(
          compareBy<Int> { frets[it] }.thenBy { it },
        )
        var nextFinger = 2
        for (s in sortedRemaining) {
          // Leave a finger gap if fret jumps by >= 2 frets from minFret and we have spare fingers
          if (nextFinger == 2 && frets[s] - minFret >= 2 && sortedRemaining.size <= 2) {
            nextFinger = 3
          }
          if (nextFinger > 4) return null
          fingers[s] = nextFinger
          nextFinger++
        }
        val barre = ChordBarre(
          fret = minFret,
          fromString = barreStart,
          toString = barreEnd,
          finger = 1,
        )
        return fingers.toList() to listOf(barre)
      }
    }

    // Without a barre, at most 4 strings can be fretted
    if (frettedStrings.size > 4) return null

    val sortedFretted = frettedStrings.sortedWith(
      compareBy<Int> { frets[it] }.thenBy { it },
    )
    for (i in sortedFretted.indices) {
      val s = sortedFretted[i]
      fingers[s] = i + 1
    }
    return fingers.toList() to emptyList()
  }

  /**
   * Identifies chords matching a set of MIDI notes or pitch classes from the interactive
   * Reverse Search fretboard. Supports both exact matches and inferred/implied matches
   * (such as omitted 5th shell voicings or slash inversions).
   */
  fun reverseIdentifyChords(
    selectedMidiNotes: List<Int>,
    maxResults: Int = 18,
  ): List<ReverseChordMatch> {
    if (selectedMidiNotes.isEmpty()) return emptyList()

    val pitchClasses = selectedMidiNotes.map { it.mod(12) }.toSet()
    if (pitchClasses.size < 2) return emptyList()

    val lowestMidi = selectedMidiNotes.minOrNull() ?: return emptyList()
    val bassPc = lowestMidi.mod(12)

    val matches = mutableListOf<ReverseChordMatch>()

    for (root in 0..11) {
      val inputIntervals = pitchClasses.map { (it - root).mod(12) }.toSet()

      for (quality in ChordQuality.entries) {
        val chordIntervals = quality.intervals.map { it.mod(12) }.toSet()
        // Every note played by the user must belong to the candidate chord
        if (!chordIntervals.containsAll(inputIntervals)) continue

        // Root should be present in the played notes (or at least for non-rootless inference)
        if (0 !in inputIntervals) continue

        val missing = chordIntervals - inputIntervals
        val requiredIntervals = chordIntervals - quality.optionalIntervals.map { it.mod(12) }.toSet()
        val missingRequired = requiredIntervals - inputIntervals

        // Allow at most 1 missing required interval (for inferring chords from partial 2-3 note shapes)
        if (missingRequired.size > 1) continue
        if (missing.size > 2) continue

        val isExact = missing.isEmpty()
        val isCompleteRequired = missingRequired.isEmpty()
        val isRootBass = bassPc == root

        var score = 100
        if (isExact) score += 60
        else if (isCompleteRequired) score += 35 // Only optional 5th/9th omitted
        else score -= missingRequired.size * 25

        if (isRootBass) {
          score += 30
        } else {
          score += 5 // Valid slash/inversion
        }

        // Prefer simpler, fundamental chord qualities when tied
        score += when (quality.category) {
          ChordCategory.TRIADS_AND_BASIC -> 15
          ChordCategory.SEVENTHS -> 12
          ChordCategory.SIXTHS_AND_ADD -> 10
          ChordCategory.EXTENDED -> 8
          ChordCategory.ALTERED -> 6
        }

        score -= missing.size * 10

        matches.add(
          ReverseChordMatch(
            rootPitchClass = root,
            quality = quality,
            bassPitchClass = bassPc,
            isExactMatch = isExact,
            isRootBass = isRootBass,
            matchedIntervals = quality.intervals.filter { (it.mod(12)) in inputIntervals },
            missingIntervals = quality.intervals.filter { (it.mod(12)) in missing },
            score = score,
          ),
        )
      }
    }

    return matches
      .sortedWith(
        compareByDescending<ReverseChordMatch> { it.score }
          .thenByDescending { it.isExactMatch }
          .thenByDescending { it.isRootBass },
      )
      .take(maxResults)
  }

  /**
   * Identifies scales and modes compatible with the notes selected on the fretboard
   * or pitch-class selector in Reverse Scale Search.
   */
  fun reverseIdentifyScales(
    selectedPitchClasses: Set<Int>,
    preferredRoot: Int? = null,
    filterRoot: Int? = null,
    filterCategory: ScaleCategory? = null,
    requireAllNotesMatch: Boolean = false,
    maxResults: Int = 16,
  ): List<ScaleMatch> {
    val cleanInput = selectedPitchClasses.map { it.mod(12) }.toSet()
    if (cleanInput.isEmpty()) return emptyList()

    val normalizedFilterRoot = filterRoot?.mod(12)
    val results = mutableListOf<ScaleMatch>()
    for (root in 0..11) {
      if (normalizedFilterRoot != null && root != normalizedFilterRoot) continue

      for (scale in ScaleType.entries) {
        if (filterCategory != null && scale.category != filterCategory) continue

        val scalePcs = scale.intervals.map { (root + it).mod(12) }.toSet()
        val matched = cleanInput.intersect(scalePcs)
        val outsideInput = cleanInput - scalePcs
        val extraScale = scalePcs - cleanInput
        val coverage = matched.size.toFloat() / cleanInput.size.toFloat()

        if (requireAllNotesMatch && outsideInput.isNotEmpty()) continue

        // Require at least 80% coverage (or 100% when <= 4 notes are selected)
        if (cleanInput.size <= 4 && matched.size < cleanInput.size) continue
        if (coverage < 0.8f) continue

        var score = (coverage * 100f).toInt()
        if (matched.size == cleanInput.size) score += 40
        if (matched.size == cleanInput.size && extraScale.isEmpty()) {
          // Exact 1-to-1 scale match (every scale note selected and no outside notes)
          score += 35
        } else {
          // Prefer tighter-fitting scales with fewer unplayed scale degrees
          score -= extraScale.size * 4
        }
        if (preferredRoot != null && root == preferredRoot.mod(12)) score += 25
        if (normalizedFilterRoot != null && root == normalizedFilterRoot) score += 30
        if (root in cleanInput) score += 15

        // Prefer core scales slightly over 8-note symmetric scales
        score += when (scale.category) {
          ScaleCategory.MAJOR_AND_MODES -> 12
          ScaleCategory.PENTATONIC_AND_BLUES -> 14
          ScaleCategory.MINOR_VARIANTS -> 8
          ScaleCategory.SYMMETRIC_AND_EXOTIC -> 4
        }

        results.add(
          ScaleMatch(
            rootPitchClass = root,
            scaleType = scale,
            matchedPitchClasses = matched,
            totalInputPitchClasses = cleanInput.size,
            coverageRatio = coverage,
            score = score,
            extraScalePitchClasses = extraScale,
            outsideInputPitchClasses = outsideInput,
          ),
        )
      }
    }

    return results
      .sortedWith(
        compareByDescending<ScaleMatch> { it.score }
          .thenByDescending { it.isExactScale }
          .thenByDescending { it.coverageRatio }
          .thenBy { it.extraScalePitchClasses.size },
      )
      .take(maxResults)
  }

  /**
   * Extracts compatible scales for an entire chord progression (Songbook / Progression Harmonizer).
   */
  fun extractScalesForProgression(
    chords: List<SavedProgressionChord>,
    maxResults: Int = 12,
  ): List<ScaleMatch> {
    if (chords.isEmpty()) return emptyList()

    val allPitchClasses = mutableSetOf<Int>()
    val chordRoots = chords.map { it.rootPitchClass.mod(12) }.toSet()
    val firstRoot = chords.first().rootPitchClass.mod(12)

    for (chord in chords) {
      // Include required chord intervals so optional 5ths don't over-constrain
      val required = chord.quality.intervals.filter { it !in chord.quality.optionalIntervals }
      for (iv in required) {
        allPitchClasses.add((chord.rootPitchClass + iv).mod(12))
      }
      allPitchClasses.add(chord.bassPitchClass.mod(12))
    }

    val results = mutableListOf<ScaleMatch>()
    for (root in 0..11) {
      for (scale in ScaleType.entries) {
        val scalePcs = scale.intervals.map { (root + it).mod(12) }.toSet()
        val matched = allPitchClasses.intersect(scalePcs)
        val outsideInput = allPitchClasses - scalePcs
        val extraScale = scalePcs - allPitchClasses
        val coverage = matched.size.toFloat() / allPitchClasses.size.toFloat()
        if (coverage < 0.7f) continue

        val rootsInScale = chordRoots.count { it in scalePcs }
        var score = (coverage * 100f).toInt() + rootsInScale * 12
        if (root == firstRoot) score += 25
        if (matched.size == allPitchClasses.size) score += 35

        score += when (scale) {
          ScaleType.MAJOR, ScaleType.NATURAL_MINOR -> 18
          ScaleType.DORIAN, ScaleType.MIXOLYDIAN, ScaleType.MINOR_PENTATONIC, ScaleType.MAJOR_PENTATONIC -> 14
          else -> 6
        }

        results.add(
          ScaleMatch(
            rootPitchClass = root,
            scaleType = scale,
            matchedPitchClasses = matched,
            totalInputPitchClasses = allPitchClasses.size,
            coverageRatio = coverage,
            score = score,
            extraScalePitchClasses = extraScale,
            outsideInputPitchClasses = outsideInput,
          ),
        )
      }
    }

    return results
      .sortedByDescending { it.score }
      .take(maxResults)
  }

  /**
   * Computes 5 overlapping positional boxes (CAGED / 5-Position system) along the neck
   * for practicing a scale in manageable 4–5 fret spans.
   */
  fun computeScaleBoxes(rootPitchClass: Int, tuning: ChordTuning, capoFret: Int = 0): List<ScaleBoxPosition> {
    val lowestOpenPc = tuning.effectiveMidi(0, capoFret).mod(12)
    val rootFretOnLowest = (rootPitchClass.mod(12) - lowestOpenPc).mod(12)
    val offsets = listOf(0, 2, 4, 7, 9)
    return offsets.mapIndexed { idx, offset ->
      val rawStart = (rootFretOnLowest + offset).mod(12)
      val start = rawStart.coerceIn(0, 11)
      val end = (start + 4).coerceAtMost(15)
      ScaleBoxPosition(
        boxNumber = idx + 1,
        label = "Box ${idx + 1} (Frets $start–$end)",
        startFret = start,
        endFret = end,
      )
    }
  }

  /**
   * Harmonizes a [scaleType] rooted at [rootPitchClass] into diatonic triads and 7th chords
   * for each scale degree, complete with Roman numeral analysis.
   */
  fun harmonizeScale(
    rootPitchClass: Int,
    scaleType: ScaleType,
  ): List<DiatonicChord> {
    val root = rootPitchClass.mod(12)
    val scalePcs = scaleType.intervals.map { (root + it).mod(12) }
    val degreeCount = scalePcs.size
    if (degreeCount < 5) return emptyList()

    val romanBase = arrayOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII")

    return scalePcs.indices.map { idx ->
      val degreeRoot = scalePcs[idx]
      val thirdPc = scalePcs[(idx + 2) % degreeCount]
      val fifthPc = scalePcs[(idx + 4) % degreeCount]
      val seventhPc = scalePcs[(idx + 6) % degreeCount]

      val thirdSemi = (thirdPc - degreeRoot).mod(12)
      val fifthSemi = (fifthPc - degreeRoot).mod(12)
      val seventhSemi = (seventhPc - degreeRoot).mod(12)

      val triadQuality = when {
        thirdSemi == 4 && fifthSemi == 7 -> ChordQuality.MAJOR
        thirdSemi == 3 && fifthSemi == 7 -> ChordQuality.MINOR
        thirdSemi == 3 && fifthSemi == 6 -> ChordQuality.DIMINISHED
        thirdSemi == 4 && fifthSemi == 8 -> ChordQuality.AUGMENTED
        thirdSemi == 5 && fifthSemi == 7 -> ChordQuality.SUS4
        thirdSemi == 2 && fifthSemi == 7 -> ChordQuality.SUS2
        else -> if (thirdSemi == 3) ChordQuality.MINOR else ChordQuality.MAJOR
      }

      val seventhQuality = when {
        triadQuality == ChordQuality.MAJOR && seventhSemi == 11 -> ChordQuality.MAJOR_7
        triadQuality == ChordQuality.MAJOR && seventhSemi == 10 -> ChordQuality.DOMINANT_7
        triadQuality == ChordQuality.MINOR && seventhSemi == 10 -> ChordQuality.MINOR_7
        triadQuality == ChordQuality.MINOR && seventhSemi == 11 -> ChordQuality.MINOR_MAJOR_7
        triadQuality == ChordQuality.DIMINISHED && seventhSemi == 10 -> ChordQuality.HALF_DIMINISHED
        triadQuality == ChordQuality.DIMINISHED && seventhSemi == 9 -> ChordQuality.DIMINISHED_7
        triadQuality == ChordQuality.AUGMENTED && seventhSemi == 11 -> ChordQuality.MAJOR_7_SHARP_5
        triadQuality == ChordQuality.AUGMENTED && seventhSemi == 10 -> ChordQuality.DOMINANT_7_SHARP_5
        else -> triadQuality
      }

      val numeralBase = romanBase.getOrElse(idx) { "${idx + 1}" }
      val roman = when (triadQuality) {
        ChordQuality.MAJOR -> numeralBase
        ChordQuality.MINOR -> numeralBase.lowercase()
        ChordQuality.DIMINISHED -> "${numeralBase.lowercase()}°"
        ChordQuality.AUGMENTED -> "$numeralBase+"
        else -> numeralBase
      }

      val intervalLabel = scaleType.formulaLabels.getOrElse(idx) {
        intervalName((degreeRoot - root).mod(12))
      }

      DiatonicChord(
        degreeNumber = idx + 1,
        romanNumeral = roman,
        rootPitchClass = degreeRoot,
        triadQuality = triadQuality,
        seventhQuality = seventhQuality,
        intervalLabel = intervalLabel,
      )
    }
  }
}

/**
 * Persistence helper for saving custom tunings and user chord progressions in SharedPreferences.
 */
class ChordBookPreferencesRepository(
  private val sharedPreferences: SharedPreferences,
) {
  companion object {
    const val KEY_CUSTOM_TUNINGS = "custom_tunings_json"
    const val KEY_PROGRESSIONS = "progressions_json"
    const val KEY_SELECTED_TUNING_ID = "selected_tuning_id"
    const val KEY_CAPO_FRET = "capo_fret"
    const val KEY_ENHARMONIC = "enharmonic_preference"
    const val KEY_DOT_LABEL_MODE = "dot_label_mode"
  }

  constructor(context: Context) : this(
    context.getSharedPreferences(CHORD_BOOK_PREFS_NAME, Context.MODE_PRIVATE),
  )

  fun loadCustomTunings(): List<ChordTuning> {
    val raw = sharedPreferences.getString(KEY_CUSTOM_TUNINGS, null) ?: return emptyList()
    return try {
      val arr = JSONArray(raw)
      val list = mutableListOf<ChordTuning>()
      for (i in 0 until arr.length()) {
        val obj = arr.optJSONObject(i) ?: continue
        ChordTuning.fromJson(obj)?.let { list.add(it) }
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }

  fun saveCustomTunings(tunings: List<ChordTuning>) {
    val arr = JSONArray()
    tunings.forEach { arr.put(it.toJson()) }
    sharedPreferences.edit().putString(KEY_CUSTOM_TUNINGS, arr.toString()).apply()
  }

  fun loadProgressions(): List<ChordProgression> {
    val raw = sharedPreferences.getString(KEY_PROGRESSIONS, null)
      ?: return ChordBookEngine.defaultProgressions()
    return try {
      val arr = JSONArray(raw)
      val list = mutableListOf<ChordProgression>()
      for (i in 0 until arr.length()) {
        val obj = arr.optJSONObject(i) ?: continue
        ChordProgression.fromJson(obj)?.let { list.add(it) }
      }
      list.ifEmpty { ChordBookEngine.defaultProgressions() }
    } catch (_: Exception) {
      ChordBookEngine.defaultProgressions()
    }
  }

  fun saveProgressions(progressions: List<ChordProgression>) {
    val arr = JSONArray()
    progressions.forEach { arr.put(it.toJson()) }
    sharedPreferences.edit().putString(KEY_PROGRESSIONS, arr.toString()).apply()
  }

  fun getSelectedTuningId(): String {
    return sharedPreferences.getString(KEY_SELECTED_TUNING_ID, null)
      ?: ChordBookEngine.BUILT_IN_TUNINGS.first().id
  }

  fun setSelectedTuningId(id: String) {
    sharedPreferences.edit().putString(KEY_SELECTED_TUNING_ID, id).apply()
  }

  fun getCapoFret(): Int {
    return sharedPreferences.getInt(KEY_CAPO_FRET, 0).coerceIn(0, 12)
  }

  fun setCapoFret(capo: Int) {
    sharedPreferences.edit().putInt(KEY_CAPO_FRET, capo.coerceIn(0, 12)).apply()
  }

  fun getEnharmonicPreference(): EnharmonicPreference {
    val raw = sharedPreferences.getString(KEY_ENHARMONIC, EnharmonicPreference.AUTO.name)
    return try {
      EnharmonicPreference.valueOf(raw ?: EnharmonicPreference.AUTO.name)
    } catch (_: IllegalArgumentException) {
      EnharmonicPreference.AUTO
    }
  }

  fun setEnharmonicPreference(pref: EnharmonicPreference) {
    sharedPreferences.edit().putString(KEY_ENHARMONIC, pref.name).apply()
  }

  fun getDotLabelMode(): DotLabelMode {
    val raw = sharedPreferences.getString(KEY_DOT_LABEL_MODE, DotLabelMode.INTERVALS.name)
    return try {
      DotLabelMode.valueOf(raw ?: DotLabelMode.INTERVALS.name)
    } catch (_: IllegalArgumentException) {
      DotLabelMode.INTERVALS
    }
  }

  fun setDotLabelMode(mode: DotLabelMode) {
    sharedPreferences.edit().putString(KEY_DOT_LABEL_MODE, mode.name).apply()
  }
}
