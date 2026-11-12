package com.example.toolbox.ui.tools

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChordBookEngineTest {

  @Test
  fun formatPitchClass_respectsEnharmonicPreferences() {
    // C# / Db = 1, Eb / D# = 3, Bb / A# = 10
    assertEquals("C♯", ChordBookEngine.formatPitchClass(1, EnharmonicPreference.SHARPS))
    assertEquals("D♭", ChordBookEngine.formatPitchClass(1, EnharmonicPreference.FLATS))

    // Auto uses flats for flat roots (F=5, Bb=10, Eb=3, Ab=8, Db=1) and sharps for G/D/A/E/B
    assertEquals("B♭", ChordBookEngine.formatPitchClass(10, EnharmonicPreference.AUTO, contextRoot = 5))
    assertEquals("F♯", ChordBookEngine.formatPitchClass(6, EnharmonicPreference.AUTO, contextRoot = 7))
  }

  @Test
  fun formatChordSymbol_formatsStandardAndSlashChords() {
    assertEquals(
      "Cmaj7",
      ChordBookEngine.formatChordSymbol(0, ChordQuality.MAJOR_7),
    )
    assertEquals(
      "F♯m7♭5",
      ChordBookEngine.formatChordSymbol(6, ChordQuality.HALF_DIMINISHED, preference = EnharmonicPreference.SHARPS),
    )
    assertEquals(
      "D/F♯",
      ChordBookEngine.formatChordSymbol(2, ChordQuality.MAJOR, bassPitchClass = 6, preference = EnharmonicPreference.SHARPS),
    )
    // When bass equals root, no slash is appended
    assertEquals(
      "G7",
      ChordBookEngine.formatChordSymbol(7, ChordQuality.DOMINANT_7, bassPitchClass = 7),
    )
  }

  @Test
  fun generateChordVoicings_standardGuitar_producesCanonicalOpenChords() {
    val stdGuitar = ChordBookEngine.BUILT_IN_TUNINGS.first { it.id == "guitar_std" }

    // C Major open chord: x-3-2-0-1-0
    val cMajorVoicings = ChordBookEngine.generateChordVoicings(
      rootPitchClass = 0,
      quality = ChordQuality.MAJOR,
      tuning = stdGuitar,
    )
    assertTrue("Should generate playable C major voicings", cMajorVoicings.isNotEmpty())
    assertTrue(
      "Should include standard open C shape x-3-2-0-1-0",
      cMajorVoicings.any { it.frets == listOf(-1, 3, 2, 0, 1, 0) },
    )

    // E Major open chord: 0-2-2-1-0-0
    val eMajorVoicings = ChordBookEngine.generateChordVoicings(
      rootPitchClass = 4,
      quality = ChordQuality.MAJOR,
      tuning = stdGuitar,
    )
    assertTrue(
      "Should include standard open E shape 0-2-2-1-0-0",
      eMajorVoicings.any { it.frets == listOf(0, 2, 2, 1, 0, 0) },
    )

    // F Major barre chord: 1-3-3-2-1-1 with barre on fret 1
    val fMajorVoicings = ChordBookEngine.generateChordVoicings(
      rootPitchClass = 5,
      quality = ChordQuality.MAJOR,
      tuning = stdGuitar,
    )
    val fullFBarre = fMajorVoicings.find { it.frets == listOf(1, 3, 3, 2, 1, 1) }
    assertNotNull("Should include full E-shape F barre 1-3-3-2-1-1", fullFBarre)
    assertTrue("Full F barre should have a barre at fret 1", fullFBarre!!.barres.any { it.fret == 1 })
  }

  @Test
  fun generateChordVoicings_worksAcrossAlternateTuningsAndInstruments() {
    val dropD = ChordBookEngine.BUILT_IN_TUNINGS.first { it.id == "guitar_drop_d" }
    val dPowerOrMajor = ChordBookEngine.generateChordVoicings(
      rootPitchClass = 2, // D
      quality = ChordQuality.MAJOR,
      tuning = dropD,
    )
    assertTrue("Drop D should generate D Major voicings", dPowerOrMajor.isNotEmpty())
    // In Drop D, open 6th string is D2 (fret 0), so 0-0-0-2-3-2 is a playable D major with open low D
    assertTrue(
      "Drop D should include open low-D voicing 0-0-0-2-3-2",
      dPowerOrMajor.any { it.frets == listOf(0, 0, 0, 2, 3, 2) },
    )

    // Ukulele C Major: 0-0-0-3 (G4 C4 E4 C5)
    val uke = ChordBookEngine.BUILT_IN_TUNINGS.first { it.id == "uke_std" }
    val ukeCMajor = ChordBookEngine.generateChordVoicings(
      rootPitchClass = 0,
      quality = ChordQuality.MAJOR,
      tuning = uke,
      allowInversions = true,
    )
    assertTrue("Ukulele should generate C major voicings", ukeCMajor.isNotEmpty())
    assertTrue(
      "Ukulele should include classic 0-0-0-3 C major shape",
      ukeCMajor.any { it.frets == listOf(0, 0, 0, 3) },
    )
  }

  @Test
  fun generateChordVoicings_slashBassAndExtendedChords_arePlayable() {
    val stdGuitar = ChordBookEngine.BUILT_IN_TUNINGS.first { it.id == "guitar_std" }

    // D/F# (root = D=2, slashBass = F#=6): 2-0-0-2-3-2
    val dSlashFSharp = ChordBookEngine.generateChordVoicings(
      rootPitchClass = 2,
      quality = ChordQuality.MAJOR,
      tuning = stdGuitar,
      slashBassPitchClass = 6,
    )
    assertTrue("D/F# should produce voicings with F# in bass", dSlashFSharp.isNotEmpty())
    assertTrue(
      "All D/F# voicings must have bassPitchClass == 6 (F#)",
      dSlashFSharp.all { it.bassPitchClass == 6 },
    )

    // Extended chords (Dominant 13th, 9th, 7#9)
    val c13Voicings = ChordBookEngine.generateChordVoicings(
      rootPitchClass = 0,
      quality = ChordQuality.DOMINANT_13,
      tuning = stdGuitar,
    )
    assertTrue("Should generate playable C13 voicings (omitting optional 5th/9th when needed)", c13Voicings.isNotEmpty())
    for (v in c13Voicings) {
      assertTrue("Fingers assigned must be between 0 and 4", v.fingers.all { it in 0..4 })
    }
  }

  @Test
  fun reverseIdentifyChords_identifiesExactAndSlashAndImpliedChords() {
    // Standard open C major MIDI notes: A2+3=C3(48), D3+2=E3(52), G3=55, B3+1=C4(60), E4=64
    val cMajorMidi = listOf(48, 52, 55, 60, 64)
    val cMatches = ChordBookEngine.reverseIdentifyChords(cMajorMidi)
    assertTrue(cMatches.isNotEmpty())
    val topMatch = cMatches.first()
    assertEquals(0, topMatch.rootPitchClass)
    assertEquals(ChordQuality.MAJOR, topMatch.quality)
    assertTrue(topMatch.isExactMatch)
    assertTrue(topMatch.isRootBass)

    // Slash chord D/F#: F#2(42), A2(45), D3(50), A3(57), D4(62), F#4(66)
    val dSlashFSharpMidi = listOf(42, 45, 50, 57, 62, 66)
    val slashMatches = ChordBookEngine.reverseIdentifyChords(dSlashFSharpMidi)
    val dMatch = slashMatches.find { it.rootPitchClass == 2 && it.quality == ChordQuality.MAJOR }
    assertNotNull(dMatch)
    assertEquals(6, dMatch!!.bassPitchClass)
    assertFalse(dMatch.isRootBass)
    assertEquals("D/F♯", dMatch.formatChordName(EnharmonicPreference.SHARPS))

    // Shell voicing Cmaj7 without 5th (C3=48, E3=52, B3=59)
    val cMaj7Shell = listOf(48, 52, 59)
    val shellMatches = ChordBookEngine.reverseIdentifyChords(cMaj7Shell)
    assertTrue(
      "Should infer Cmaj7 when 5th (G) is omitted",
      shellMatches.any { it.rootPitchClass == 0 && it.quality == ChordQuality.MAJOR_7 },
    )
  }

  @Test
  fun reverseIdentifyScales_identifiesExactPentatonicAndModesAndAppliesFilters() {
    // C Major Pentatonic pitch classes: C(0), D(2), E(4), G(7), A(9)
    val cPentatonicPcs = setOf(0, 2, 4, 7, 9)
    val matches = ChordBookEngine.reverseIdentifyScales(
      selectedPitchClasses = cPentatonicPcs,
      preferredRoot = 0,
    )
    assertTrue(matches.isNotEmpty())
    val top = matches.first()
    assertEquals(0, top.rootPitchClass)
    assertEquals(ScaleType.MAJOR_PENTATONIC, top.scaleType)
    assertTrue(top.isExactScale)
    assertTrue(top.extraScalePitchClasses.isEmpty())
    assertTrue(top.outsideInputPitchClasses.isEmpty())

    // C Major / D Dorian 7-note pitch classes filtered by D(2) root and MAJOR_AND_MODES category
    val whiteKeyPcs = setOf(0, 2, 4, 5, 7, 9, 11)
    val dRootMatches = ChordBookEngine.reverseIdentifyScales(
      selectedPitchClasses = whiteKeyPcs,
      filterRoot = 2,
      filterCategory = ScaleCategory.MAJOR_AND_MODES,
      requireAllNotesMatch = true,
    )
    assertTrue(dRootMatches.isNotEmpty())
    assertTrue(dRootMatches.all { it.rootPitchClass == 2 })
    val dorianMatch = dRootMatches.first()
    assertEquals(ScaleType.DORIAN, dorianMatch.scaleType)
    assertTrue(dorianMatch.isExactScale)
  }

  @Test
  fun harmonizeScale_generatesCorrectDiatonicDegreesInCMajor() {
    val harmonized = ChordBookEngine.harmonizeScale(0, ScaleType.MAJOR)
    assertEquals(7, harmonized.size)

    // I = Cmaj7, ii = Dm7, iii = Em7, IV = Fmaj7, V = G7, vi = Am7, vii° = Bm7b5
    assertEquals("I", harmonized[0].romanNumeral)
    assertEquals(ChordQuality.MAJOR, harmonized[0].triadQuality)
    assertEquals(ChordQuality.MAJOR_7, harmonized[0].seventhQuality)

    assertEquals("ii", harmonized[1].romanNumeral)
    assertEquals(ChordQuality.MINOR, harmonized[1].triadQuality)
    assertEquals(ChordQuality.MINOR_7, harmonized[1].seventhQuality)

    assertEquals("V", harmonized[4].romanNumeral)
    assertEquals(ChordQuality.MAJOR, harmonized[4].triadQuality)
    assertEquals(ChordQuality.DOMINANT_7, harmonized[4].seventhQuality)

    assertEquals("vii°", harmonized[6].romanNumeral)
    assertEquals(ChordQuality.DIMINISHED, harmonized[6].triadQuality)
    assertEquals(ChordQuality.HALF_DIMINISHED, harmonized[6].seventhQuality)
  }

  @Test
  fun extractScalesForProgression_identifiesParentKey() {
    val cPopProgression = listOf(
      SavedProgressionChord("1", 0, ChordQuality.MAJOR), // C
      SavedProgressionChord("2", 7, ChordQuality.MAJOR), // G
      SavedProgressionChord("3", 9, ChordQuality.MINOR), // Am
      SavedProgressionChord("4", 5, ChordQuality.MAJOR), // F
    )
    val scales = ChordBookEngine.extractScalesForProgression(cPopProgression)
    assertTrue(scales.isNotEmpty())
    val top = scales.first()
    assertEquals(0, top.rootPitchClass)
    assertEquals(ScaleType.MAJOR, top.scaleType)
    assertEquals(1.0f, top.coverageRatio, 0.001f)
  }

  @Test
  fun defaultProgressions_containsAxisRoyalRoadAndFlamenco() {
    val defaults = ChordBookEngine.defaultProgressions()
    assertEquals(3, defaults.size)
    assertEquals(
      listOf("Axis Progression", "Royal Road Progression", "Flamenco Progression"),
      defaults.map { it.name },
    )
    assertEquals(
      listOf("C", "G", "Am", "F"),
      defaults[0].chords.map { it.formatName() },
    )
    assertEquals(
      listOf("Fmaj7", "G7", "Em7", "Am7"),
      defaults[1].chords.map { it.formatName() },
    )
    assertEquals(
      listOf("Am", "G", "F", "E"),
      defaults[2].chords.map { it.formatName() },
    )
  }

  @Test
  fun progressionTransposeAndJsonRoundTrip_preservesData() {
    val chord = SavedProgressionChord(
      id = "ch1",
      rootPitchClass = 0, // C
      quality = ChordQuality.MINOR_7,
      bassPitchClass = 7, // G
      preferredFrets = listOf(3, 3, 5, 3, 4, 3),
    )
    val transposedUp2 = chord.transpose(2)
    assertEquals(2, transposedUp2.rootPitchClass) // D
    assertEquals(9, transposedUp2.bassPitchClass) // A

    val prog = ChordProgression("p1", "My Song", listOf(chord))
    val restored = ChordProgression.fromJson(prog.toJson())
    assertNotNull(restored)
    assertEquals("My Song", restored!!.name)
    assertEquals(1, restored.chords.size)
    assertEquals(listOf(3, 3, 5, 3, 4, 3), restored.chords.first().preferredFrets)
  }

  @Test
  fun builtInGuitarTunings_matchGuitarTunerPresets() {
    val guitar6Tunings = ChordBookEngine.BUILT_IN_TUNINGS.filter { it.instrument == InstrumentFamily.GUITAR_6 }
    val tunerPresets = GuitarTuningPreset.entries
    assertEquals(tunerPresets.size, guitar6Tunings.size)

    tunerPresets.zip(guitar6Tunings).forEach { (preset, chordTuning) ->
      assertEquals(preset.chordTuningId, chordTuning.id)
      assertEquals(preset, chordTuning.matchingTunerPreset)
      assertTrue(
        "Chord tuning '${chordTuning.name}' should start with preset label '${preset.label}'",
        chordTuning.name.startsWith("${preset.label} ("),
      )
      assertEquals(6, chordTuning.openStringMidi.size)
      preset.strings.zip(chordTuning.openStringMidi).forEach { (stringSpec, midi) ->
        val expectedFreq = ((440.0 * Math.pow(2.0, (midi - 69) / 12.0)) * 100.0).let { Math.round(it) / 100.0 }
        assertEquals(expectedFreq, stringSpec.frequencyHz, 0.02)
      }
    }
  }

  @Test
  fun chordBookTunings_searchAndShareFavoritesWithGuitarTuner() {
    val sharedRepo = InMemoryGuitarTunerRepository(
      initialFavorites = setOf(GuitarTuningPreset.DROP_D),
    )

    // Favorite set in Guitar Tuner is immediately recognized in Chord Book
    val initialFavIds = sharedRepo.loadFavoriteTuningIds()
    val dropDChordTuning = ChordBookEngine.BUILT_IN_TUNINGS.first { it.id == "guitar_drop_d" }
    assertTrue(ChordBookEngine.isTuningFavorite(dropDChordTuning, initialFavIds))

    // Favoriting DADGAD in Chord Book updates shared repository for Guitar Tuner
    val dadgadChordTuning = ChordBookEngine.BUILT_IN_TUNINGS.first { it.id == "guitar_dadgad" }
    val updatedFavIds = ChordBookEngine.toggleFavoriteTuning(initialFavIds, dadgadChordTuning)
    sharedRepo.saveFavoriteTuningIds(updatedFavIds)

    assertEquals(
      setOf(GuitarTuningPreset.DROP_D, GuitarTuningPreset.DADGAD),
      sharedRepo.loadFavoritePresets(),
    )

    // Search filters by query, category, and favorites and sorts favorites first
    val favResults = ChordBookEngine.searchTunings(
      favoritesOnly = true,
      favoriteIds = sharedRepo.loadFavoriteTuningIds(),
    )
    assertEquals(listOf(dropDChordTuning, dadgadChordTuning), favResults)

    val queryResults = ChordBookEngine.searchTunings(query = "DADGAD")
    assertEquals(listOf(dadgadChordTuning), queryResults)
  }

  @Test
  fun chordBookTab_orderAndTitles_matchChordsScalesReverseSongbook() {
    assertEquals(
      listOf(
        ChordBookTab.CHORDS,
        ChordBookTab.SCALES,
        ChordBookTab.REVERSE,
        ChordBookTab.SONGBOOK,
      ),
      ChordBookTab.entries,
    )
    assertEquals(
      listOf("Chords", "Scales", "Reverse", "Songbook"),
      ChordBookTab.entries.map { it.title },
    )
  }
}
