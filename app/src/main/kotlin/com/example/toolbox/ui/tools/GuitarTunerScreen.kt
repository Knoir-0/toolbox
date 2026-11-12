package com.example.toolbox.ui.tools

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.toolbox.ui.theme.ToolboxTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GuitarStringSpec(
  val stringNumber: Int, // 6 (low) down to 1 (high)
  val noteName: String,
  val octave: Int,
  val frequencyHz: Double,
) {
  val fullLabel: String get() = noteName
}

/**
 * Categories of guitar tunings used for filter pills in the tuning list and catalog search overlay.
 */
enum class GuitarTuningCategory(val displayName: String) {
  STANDARD("Standard"),
  DROP("Drop"),
  OPEN("Open"),
  ALTERNATIVE("Alternative"),
}

/**
 * Builds a 6-string tuning (low string first) from scientific pitch notation, e.g. "E2", "Eb2", "C#3".
 * Frequencies are derived from 12-TET with A4 = 440 Hz.
 */
private fun tuning(vararg notes: String): List<GuitarStringSpec> {
  require(notes.size == 6) { "Guitar tunings must define exactly 6 strings" }
  return notes.mapIndexed { index, note ->
    val octave = note.takeLastWhile { it.isDigit() }.toInt()
    val name = note.dropLast(octave.toString().length)
    val base = when (name.first()) {
      'C' -> 0
      'D' -> 2
      'E' -> 4
      'F' -> 5
      'G' -> 7
      'A' -> 9
      'B' -> 11
      else -> error("Invalid note: $note")
    }
    val accidental = name.drop(1).sumOf { c ->
      when (c) {
        '#' -> 1
        'b' -> -1
        else -> error("Invalid accidental in note: $note")
      }.toInt()
    }
    val midi = (octave + 1) * 12 + base + accidental
    val frequency = 440.0 * 2.0.pow((midi - 69) / 12.0)
    GuitarStringSpec(
      stringNumber = 6 - index,
      noteName = name,
      octave = octave,
      frequencyHz = (frequency * 100.0).roundToInt() / 100.0,
    )
  }
}

enum class GuitarTuningPreset(
  val label: String,
  val strings: List<GuitarStringSpec>,
  /** Extra search terms (alternate names) used by the searchable tuning selector. */
  val keywords: String = "",
  val category: GuitarTuningCategory = GuitarTuningCategory.STANDARD,
  /** Corresponding ID in [ChordBookEngine.BUILT_IN_TUNINGS] for shared favorites across tools. */
  val chordTuningId: String = "",
) {
  // Standard & lowered standard tunings
  STANDARD(
    label = "Standard",
    strings = tuning("E2", "A2", "D3", "G3", "B3", "E4"),
    keywords = "E standard default",
    category = GuitarTuningCategory.STANDARD,
    chordTuningId = "guitar_std",
  ),
  HALF_STEP_DOWN(
    label = "Eb Standard",
    strings = tuning("Eb2", "Ab2", "Db3", "Gb3", "Bb3", "Eb4"),
    keywords = "half step down D# standard",
    category = GuitarTuningCategory.STANDARD,
    chordTuningId = "guitar_half_down",
  ),
  D_STANDARD(
    label = "D Standard",
    strings = tuning("D2", "G2", "C3", "F3", "A3", "D4"),
    keywords = "whole step down full step",
    category = GuitarTuningCategory.STANDARD,
    chordTuningId = "guitar_full_down",
  ),
  C_SHARP_STANDARD(
    label = "C# Standard",
    strings = tuning("Db2", "Gb2", "B2", "E3", "Ab3", "Db4"),
    keywords = "Db standard one and a half steps down",
    category = GuitarTuningCategory.STANDARD,
    chordTuningId = "guitar_csharp_std",
  ),
  C_STANDARD(
    label = "C Standard",
    strings = tuning("C2", "F2", "Bb2", "Eb3", "G3", "C4"),
    keywords = "two steps down",
    category = GuitarTuningCategory.STANDARD,
    chordTuningId = "guitar_c_std",
  ),
  B_STANDARD(
    label = "B Standard",
    strings = tuning("B1", "E2", "A2", "D3", "F#3", "B3"),
    keywords = "baritone",
    category = GuitarTuningCategory.STANDARD,
    chordTuningId = "guitar_b_std",
  ),

  // Drop tunings
  DROP_D(
    label = "Drop D",
    strings = tuning("D2", "A2", "D3", "G3", "B3", "E4"),
    category = GuitarTuningCategory.DROP,
    chordTuningId = "guitar_drop_d",
  ),
  DROP_C_SHARP(
    label = "Drop C#",
    strings = tuning("Db2", "Ab2", "Db3", "Gb3", "Bb3", "Eb4"),
    keywords = "drop Db",
    category = GuitarTuningCategory.DROP,
    chordTuningId = "guitar_drop_csharp",
  ),
  DROP_C(
    label = "Drop C",
    strings = tuning("C2", "G2", "C3", "F3", "A3", "D4"),
    category = GuitarTuningCategory.DROP,
    chordTuningId = "guitar_drop_c",
  ),
  DROP_B(
    label = "Drop B",
    strings = tuning("B1", "F#2", "B2", "E3", "G#3", "C#4"),
    category = GuitarTuningCategory.DROP,
    chordTuningId = "guitar_drop_b",
  ),
  DROP_A(
    label = "Drop A",
    strings = tuning("A1", "E2", "A2", "D3", "F#3", "B3"),
    category = GuitarTuningCategory.DROP,
    chordTuningId = "guitar_drop_a",
  ),
  DOUBLE_DROP_D(
    label = "Double Drop D",
    strings = tuning("D2", "A2", "D3", "G3", "B3", "D4"),
    category = GuitarTuningCategory.DROP,
    chordTuningId = "guitar_double_drop_d",
  ),

  // Open tunings
  OPEN_G(
    label = "Open G",
    strings = tuning("D2", "G2", "D3", "G3", "B3", "D4"),
    keywords = "slide blues",
    category = GuitarTuningCategory.OPEN,
    chordTuningId = "guitar_open_g",
  ),
  OPEN_D(
    label = "Open D",
    strings = tuning("D2", "A2", "D3", "F#3", "A3", "D4"),
    keywords = "slide",
    category = GuitarTuningCategory.OPEN,
    chordTuningId = "guitar_open_d",
  ),
  OPEN_E(
    label = "Open E",
    strings = tuning("E2", "B2", "E3", "G#3", "B3", "E4"),
    keywords = "slide",
    category = GuitarTuningCategory.OPEN,
    chordTuningId = "guitar_open_e",
  ),
  OPEN_A(
    label = "Open A",
    strings = tuning("E2", "A2", "E3", "A3", "C#4", "E4"),
    keywords = "slide",
    category = GuitarTuningCategory.OPEN,
    chordTuningId = "guitar_open_a",
  ),
  OPEN_C(
    label = "Open C",
    strings = tuning("C2", "G2", "C3", "G3", "C4", "E4"),
    category = GuitarTuningCategory.OPEN,
    chordTuningId = "guitar_open_c",
  ),
  OPEN_D_MINOR(
    label = "Open D Minor",
    strings = tuning("D2", "A2", "D3", "F3", "A3", "D4"),
    keywords = "Dm cross note",
    category = GuitarTuningCategory.OPEN,
    chordTuningId = "guitar_open_dm",
  ),

  // Modal & alternative tunings
  DADGAD(
    label = "DADGAD",
    strings = tuning("D2", "A2", "D3", "G3", "A3", "D4"),
    keywords = "Dsus4 celtic",
    category = GuitarTuningCategory.ALTERNATIVE,
    chordTuningId = "guitar_dadgad",
  ),
  ALL_FOURTHS(
    label = "All Fourths",
    strings = tuning("E2", "A2", "D3", "G3", "C4", "F4"),
    keywords = "P4",
    category = GuitarTuningCategory.ALTERNATIVE,
    chordTuningId = "guitar_all_fourths",
  ),
  NEW_STANDARD(
    label = "New Standard",
    strings = tuning("C2", "G2", "D3", "A3", "E4", "G4"),
    keywords = "NST Fripp",
    category = GuitarTuningCategory.ALTERNATIVE,
    chordTuningId = "guitar_nst",
  ),
  NASHVILLE(
    label = "Nashville",
    strings = tuning("E3", "A3", "D4", "G4", "B3", "E4"),
    keywords = "high strung",
    category = GuitarTuningCategory.ALTERNATIVE,
    chordTuningId = "guitar_nashville",
  ),
  ;

  /** Note names, low to high, e.g. "E A D G B E". */
  val subtitle: String get() = strings.joinToString(" ") { it.noteName }

  fun matches(query: String): Boolean {
    val terms = query.trim().lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (terms.isEmpty()) return true
    val haystack = "$label $subtitle ${subtitle.replace(" ", "")} $keywords ${category.displayName}".lowercase()
    return terms.all { haystack.contains(it) }
  }

  companion object {
    fun fromKey(key: String): GuitarTuningPreset? {
      return entries.firstOrNull { it.name == key || it.chordTuningId == key }
    }
  }
}

/**
 * Search, filtering, and favoriting engine for Guitar Tuner presets.
 */
object GuitarTunerEngine {
  fun searchTunings(
    catalog: List<GuitarTuningPreset> = GuitarTuningPreset.entries,
    query: String = "",
    category: GuitarTuningCategory? = null,
    favoritesOnly: Boolean = false,
    favorites: Set<GuitarTuningPreset> = emptySet(),
  ): List<GuitarTuningPreset> {
    return catalog.filter { preset ->
      (!favoritesOnly || preset in favorites) &&
        (category == null || preset.category == category) &&
        preset.matches(query)
    }
  }

  /**
   * Filters tunings for the main list below the tuner and places favorited tunings at the top
   * for quick access while preserving preset order within each group.
   */
  fun filterTuningsForList(
    catalog: List<GuitarTuningPreset> = GuitarTuningPreset.entries,
    favorites: Set<GuitarTuningPreset> = emptySet(),
    favoritesOnly: Boolean = false,
    category: GuitarTuningCategory? = null,
  ): List<GuitarTuningPreset> {
    val filtered = catalog.filter { preset ->
      (!favoritesOnly || preset in favorites) &&
        (category == null || preset.category == category)
    }
    return filtered.sortedByDescending { it in favorites }
  }

  fun toggleFavorite(
    currentFavorites: Set<GuitarTuningPreset>,
    preset: GuitarTuningPreset,
  ): Set<GuitarTuningPreset> {
    return if (preset in currentFavorites) {
      currentFavorites - preset
    } else {
      currentFavorites + preset
    }
  }
}

/**
 * Persistence interface for favorite tunings, shared between Guitar Tuner and Chord Book.
 */
interface GuitarTunerRepository {
  fun loadFavoritePresets(): Set<GuitarTuningPreset>
  fun saveFavoritePresets(favorites: Set<GuitarTuningPreset>)

  fun loadFavoriteTuningIds(): Set<String> {
    return loadFavoritePresets().map { it.chordTuningId }.toSet()
  }

  fun saveFavoriteTuningIds(ids: Set<String>) {
    val presets = ids.mapNotNull { GuitarTuningPreset.fromKey(it) }.toSet()
    saveFavoritePresets(presets)
  }
}

class InMemoryGuitarTunerRepository(
  initialFavorites: Set<GuitarTuningPreset> = emptySet(),
  initialFavoriteIds: Set<String> = emptySet(),
) : GuitarTunerRepository {
  private var favoriteIds: Set<String> =
    initialFavorites.map { it.chordTuningId }.toSet() +
      initialFavoriteIds.map { GuitarTuningPreset.fromKey(it)?.chordTuningId ?: it }.toSet()

  override fun loadFavoritePresets(): Set<GuitarTuningPreset> {
    return favoriteIds.mapNotNull { GuitarTuningPreset.fromKey(it) }.toSet()
  }

  override fun saveFavoritePresets(favorites: Set<GuitarTuningPreset>) {
    val nonGuitarIds = favoriteIds.filter { GuitarTuningPreset.fromKey(it) == null }.toSet()
    favoriteIds = nonGuitarIds + favorites.map { it.chordTuningId }.toSet()
  }

  override fun loadFavoriteTuningIds(): Set<String> = favoriteIds

  override fun saveFavoriteTuningIds(ids: Set<String>) {
    favoriteIds = ids.map { GuitarTuningPreset.fromKey(it)?.chordTuningId ?: it }.toSet()
  }
}

class SharedPreferencesGuitarTunerRepository(
  context: Context,
) : GuitarTunerRepository {
  private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  override fun loadFavoritePresets(): Set<GuitarTuningPreset> {
    val raw = prefs.getStringSet(KEY_FAVORITE_TUNINGS, emptySet()).orEmpty()
    return raw.mapNotNull { key -> GuitarTuningPreset.fromKey(key) }.toSet()
  }

  override fun saveFavoritePresets(favorites: Set<GuitarTuningPreset>) {
    val existingRaw = prefs.getStringSet(KEY_FAVORITE_TUNINGS, emptySet()).orEmpty()
    val nonGuitarIds = existingRaw.filter { GuitarTuningPreset.fromKey(it) == null }.toSet()
    val updated = nonGuitarIds + favorites.map { it.name }.toSet()
    prefs.edit()
      .putStringSet(KEY_FAVORITE_TUNINGS, updated)
      .apply()
  }

  override fun loadFavoriteTuningIds(): Set<String> {
    val raw = prefs.getStringSet(KEY_FAVORITE_TUNINGS, emptySet()).orEmpty()
    return raw.map { key -> GuitarTuningPreset.fromKey(key)?.chordTuningId ?: key }.toSet()
  }

  override fun saveFavoriteTuningIds(ids: Set<String>) {
    val normalized = ids.map { key ->
      GuitarTuningPreset.fromKey(key)?.name ?: key
    }.toSet()
    prefs.edit()
      .putStringSet(KEY_FAVORITE_TUNINGS, normalized)
      .apply()
  }

  companion object {
    const val PREFS_NAME = "guitar_tuner_prefs"
    const val KEY_FAVORITE_TUNINGS = "favorite_tunings"
  }
}

/**
 * Lightweight synthesizer for reference guitar string tones.
 * Safely catches audio initialization exceptions in headless test environments.
 */
private object SoundSynthesizer {
  suspend fun playPluckedTone(frequencyHz: Double, durationMs: Int = 900) = withContext(Dispatchers.Default) {
    try {
      val sampleRate = 22050
      val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
      val samples = ShortArray(numSamples)
      for (i in 0 until numSamples) {
        val t = i.toDouble() / sampleRate
        val envelope = exp(-3.5 * t) * (1.0 - exp(-120.0 * t))
        // Fundamental + warm guitar harmonics
        val wave = (
          0.65 * sin(2.0 * PI * frequencyHz * t) +
            0.22 * sin(2.0 * PI * (frequencyHz * 2.0) * t) +
            0.09 * sin(2.0 * PI * (frequencyHz * 3.0) * t) +
            0.04 * sin(2.0 * PI * (frequencyHz * 4.0) * t)
          )
        samples[i] = (wave * envelope * Short.MAX_VALUE * 0.75).toInt()
          .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
      }
      playPcmBuffer(samples, sampleRate)
    } catch (_: Throwable) {
      // Ignore in test / no-audio environments
    }
  }

  private fun playPcmBuffer(samples: ShortArray, sampleRate: Int) {
    val bufferSizeBytes = samples.size * 2
    val track = AudioTrack.Builder()
      .setAudioAttributes(
        AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
          .build(),
      )
      .setAudioFormat(
        AudioFormat.Builder()
          .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
          .setSampleRate(sampleRate)
          .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
          .build(),
      )
      .setBufferSizeInBytes(bufferSizeBytes.coerceAtLeast(AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
      )))
      .setTransferMode(AudioTrack.MODE_STATIC)
      .build()
    track.write(samples, 0, samples.size)
    track.play()
  }
}

data class PitchMatchResult(
  val stringIndex: Int,
  val stringSpec: GuitarStringSpec,
  val detectedFrequencyHz: Double,
  val centsOffset: Float,
)

/**
 * Real-time guitar pitch detector using normalized autocorrelation with parabolic interpolation
 * and closest-string matching in logarithmic (cents) space.
 */
object GuitarPitchDetector {
  fun centsBetween(frequencyHz: Double, targetFrequencyHz: Double): Double {
    if (frequencyHz <= 0.0 || targetFrequencyHz <= 0.0) return 0.0
    return 1200.0 * (ln(frequencyHz / targetFrequencyHz) / ln(2.0))
  }

  fun matchClosestString(
    frequencyHz: Double,
    strings: List<GuitarStringSpec>,
  ): PitchMatchResult {
    var bestIndex = 0
    var bestCents = Double.MAX_VALUE
    strings.forEachIndexed { index, spec ->
      val cents = centsBetween(frequencyHz, spec.frequencyHz)
      if (abs(cents) < abs(bestCents)) {
        bestCents = cents
        bestIndex = index
      }
    }
    return PitchMatchResult(
      stringIndex = bestIndex,
      stringSpec = strings[bestIndex],
      detectedFrequencyHz = frequencyHz,
      centsOffset = bestCents.toFloat().coerceIn(-50f, 50f),
    )
  }

  fun detectFundamentalFrequency(
    samples: ShortArray,
    sampleCount: Int,
    sampleRate: Int,
    minFreqHz: Double = 55.0,
    maxFreqHz: Double = 450.0,
  ): Double? {
    if (sampleCount < 256 || sampleRate <= 0) return null

    var sum = 0.0
    for (i in 0 until sampleCount) {
      sum += samples[i].toDouble()
    }
    val mean = sum / sampleCount

    val normalized = DoubleArray(sampleCount)
    var sumSquares = 0.0
    for (i in 0 until sampleCount) {
      val v = (samples[i].toDouble() - mean) / Short.MAX_VALUE
      normalized[i] = v
      sumSquares += v * v
    }
    val rms = sqrt(sumSquares / sampleCount)
    if (rms < 0.012) return null

    val minLag = (sampleRate / maxFreqHz).toInt().coerceAtLeast(2)
    val maxLag = (sampleRate / minFreqHz).toInt().coerceAtMost(sampleCount / 2 - 2)
    if (minLag >= maxLag) return null

    val corr = DoubleArray(maxLag + 2)
    for (lag in minLag - 1..maxLag + 1) {
      var num = 0.0
      var e1 = 0.0
      var e2 = 0.0
      val limit = sampleCount - lag
      for (i in 0 until limit) {
        val a = normalized[i]
        val b = normalized[i + lag]
        num += a * b
        e1 += a * a
        e2 += b * b
      }
      val denom = sqrt(e1 * e2)
      corr[lag] = if (denom > 1e-9) num / denom else 0.0
    }

    var maxCorr = 0.0
    for (lag in minLag..maxLag) {
      if (corr[lag] > maxCorr) {
        maxCorr = corr[lag]
      }
    }
    if (maxCorr < 0.65) return null

    val threshold = (maxCorr * 0.85).coerceAtLeast(0.65)
    var chosenLag = -1
    var lag = minLag
    // Skip any initial positive slope from lag 0 roll-off
    while (lag < maxLag && corr[lag] > corr[lag + 1]) {
      lag++
    }
    while (lag <= maxLag) {
      if (corr[lag] >= threshold && corr[lag] >= corr[lag - 1] && corr[lag] >= corr[lag + 1]) {
        chosenLag = lag
        break
      }
      lag++
    }
    if (chosenLag < minLag) return null

    val prev = corr[chosenLag - 1]
    val curr = corr[chosenLag]
    val next = corr[chosenLag + 1]
    val denom = prev - 2.0 * curr + next
    val delta = if (abs(denom) > 1e-9) {
      (0.5 * (prev - next) / denom).coerceIn(-0.5, 0.5)
    } else {
      0.0
    }
    val refinedLag = chosenLag + delta
    return sampleRate / refinedLag
  }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuitarTunerScreen(
  modifier: Modifier = Modifier,
  repository: GuitarTunerRepository? = null,
) {
  val context = LocalContext.current
  val isInspectionMode = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current
  val focusManager = LocalFocusManager.current
  val keyboardController = LocalSoftwareKeyboardController.current

  val repo = remember(repository, context, isInspectionMode) {
    repository ?: if (isInspectionMode) {
      InMemoryGuitarTunerRepository(
        initialFavorites = setOf(GuitarTuningPreset.STANDARD, GuitarTuningPreset.DROP_D),
      )
    } else {
      SharedPreferencesGuitarTunerRepository(context)
    }
  }

  var favoritePresets by remember { mutableStateOf(repo.loadFavoritePresets()) }
  var selectedPreset by rememberSaveable { mutableStateOf(GuitarTuningPreset.STANDARD) }
  var selectedStringIndex by rememberSaveable { mutableIntStateOf(0) } // 0 = 6th string (low E)
  var centsOffset by rememberSaveable { mutableFloatStateOf(0f) }
  var detectedFrequencyHz by rememberSaveable {
    mutableDoubleStateOf(GuitarTuningPreset.STANDARD.strings.first().frequencyHz)
  }
  var isSignalActive by remember { mutableStateOf(false) }

  // Integrated toolbar search state (Section 5.7 of DESIGN_LANGUAGE.md)
  var isSearchActive by rememberSaveable { mutableStateOf(false) }
  var searchQuery by rememberSaveable { mutableStateOf("") }
  var selectedSearchCategoryName by rememberSaveable { mutableStateOf<String?>(null) }
  var searchFavoritesOnly by rememberSaveable { mutableStateOf(false) }
  val selectedSearchCategory = remember(selectedSearchCategoryName) {
    selectedSearchCategoryName?.let { name ->
      GuitarTuningCategory.entries.firstOrNull { it.name == name }
    }
  }
  val searchFocusRequester = remember { FocusRequester() }

  // Main tuning list filter state (below tuner & string display)
  var filterFavoritesOnly by rememberSaveable { mutableStateOf(false) }
  var selectedListCategoryName by rememberSaveable { mutableStateOf<String?>(null) }
  val selectedListCategory = remember(selectedListCategoryName) {
    selectedListCategoryName?.let { name ->
      GuitarTuningCategory.entries.firstOrNull { it.name == name }
    }
  }

  fun closeSearch() {
    isSearchActive = false
    searchQuery = ""
    selectedSearchCategoryName = null
    searchFavoritesOnly = false
    focusManager.clearFocus()
  }

  LaunchedEffect(isSearchActive) {
    if (isSearchActive) {
      delay(100)
      try {
        searchFocusRequester.requestFocus()
        keyboardController?.show()
      } catch (_: Exception) {}
    } else {
      keyboardController?.hide()
    }
  }

  BackHandler(enabled = isSearchActive) {
    closeSearch()
  }

  fun toggleFavorite(preset: GuitarTuningPreset) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val updated = GuitarTunerEngine.toggleFavorite(favoritePresets, preset)
    favoritePresets = updated
    repo.saveFavoritePresets(updated)
  }

  fun selectPreset(preset: GuitarTuningPreset) {
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    selectedPreset = preset
    selectedStringIndex = 0
    centsOffset = 0f
    detectedFrequencyHz = preset.strings.first().frequencyHz
    isSignalActive = false
  }

  var hasMicPermission by remember {
    mutableStateOf(
      isInspectionMode ||
        ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED,
    )
  }
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { granted ->
    hasMicPermission = granted
  }

  // Live microphone pitch detection loop
  LaunchedEffect(hasMicPermission, selectedPreset, isInspectionMode) {
    if (!hasMicPermission || isInspectionMode) {
      isSignalActive = false
      return@LaunchedEffect
    }
    withContext(Dispatchers.Default) {
      val sampleRate = 22050
      val windowSize = 2048
      val minBufSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
      )
      if (minBufSize <= 0) return@withContext

      val bufferSizeInBytes = maxOf(minBufSize, windowSize * 2)
      var audioRecord: AudioRecord? = null
      try {
        audioRecord = AudioRecord(
          MediaRecorder.AudioSource.MIC,
          sampleRate,
          AudioFormat.CHANNEL_IN_MONO,
          AudioFormat.ENCODING_PCM_16BIT,
          bufferSizeInBytes,
        )
        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
          return@withContext
        }
        audioRecord.startRecording()
        val buffer = ShortArray(windowSize)
        var smoothedFreq = 0.0
        var silentFrames = 0

        while (isActive) {
          val read = audioRecord.read(buffer, 0, buffer.size)
          if (read > 0) {
            val rawFreq = GuitarPitchDetector.detectFundamentalFrequency(
              samples = buffer,
              sampleCount = read,
              sampleRate = sampleRate,
            )
            if (rawFreq != null) {
              silentFrames = 0
              smoothedFreq = if (smoothedFreq <= 0.0 || abs(rawFreq - smoothedFreq) / smoothedFreq > 0.08) {
                rawFreq
              } else {
                smoothedFreq * 0.65 + rawFreq * 0.35
              }
              val match = GuitarPitchDetector.matchClosestString(
                frequencyHz = smoothedFreq,
                strings = selectedPreset.strings,
              )
              selectedStringIndex = match.stringIndex
              centsOffset = match.centsOffset
              detectedFrequencyHz = match.detectedFrequencyHz
              isSignalActive = true
            } else {
              silentFrames++
              if (silentFrames > 12) {
                isSignalActive = false
                smoothedFreq = 0.0
              }
            }
          } else {
            delay(25)
          }
        }
      } catch (_: Throwable) {
        // Ignore audio hardware errors in test / emulator environments
      } finally {
        try {
          audioRecord?.stop()
        } catch (_: Throwable) {}
        try {
          audioRecord?.release()
        } catch (_: Throwable) {}
      }
    }
  }

  val coroutineScope = rememberCoroutineScope()
  val strings = selectedPreset.strings
  val safeIndex = selectedStringIndex.coerceIn(0, strings.lastIndex)
  val activeString = strings[safeIndex]

  val matchingSearchPresets = remember(searchQuery, selectedSearchCategory, searchFavoritesOnly, favoritePresets) {
    GuitarTunerEngine.searchTunings(
      query = searchQuery,
      category = selectedSearchCategory,
      favoritesOnly = searchFavoritesOnly,
      favorites = favoritePresets,
    )
  }

  val filteredListPresets = remember(favoritePresets, filterFavoritesOnly, selectedListCategory) {
    GuitarTunerEngine.filterTuningsForList(
      favorites = favoritePresets,
      favoritesOnly = filterFavoritesOnly,
      category = selectedListCategory,
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("guitar_tuner_screen"),
  ) {
    // Section 5.1 & 5.7: Standard Tool Header Bar with Integrated Search Toolbar
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 1.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 68.dp),
        contentAlignment = Alignment.CenterStart,
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Guitar Tuner",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              val subtitleText = if (favoritePresets.isNotEmpty()) {
                "${selectedPreset.label} (${selectedPreset.subtitle}) • ${favoritePresets.size} favorite${if (favoritePresets.size == 1) "" else "s"}"
              } else {
                "${selectedPreset.label} • ${selectedPreset.subtitle}"
              }
              Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            IconButton(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isSearchActive = true
              },
              modifier = Modifier.testTag("toggle_search_bar_button"),
            ) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search tunings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        // Animated Expanding Search Bar integrated into the Toolbar (Section 5.7)
        androidx.compose.animation.AnimatedVisibility(
          visible = isSearchActive,
          enter = fadeIn(animationSpec = tween(220)) + expandHorizontally(
            animationSpec = spring(
              dampingRatio = Spring.DampingRatioNoBouncy,
              stiffness = Spring.StiffnessMediumLow,
            ),
            expandFrom = Alignment.End,
          ),
          exit = fadeOut(animationSpec = tween(180)) + shrinkHorizontally(
            animationSpec = tween(200),
            shrinkTowards = Alignment.End,
          ),
          modifier = Modifier.matchParentSize(),
        ) {
          Surface(
            modifier = Modifier
              .fillMaxSize()
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
              )
              .testTag("toolbar_search_bar"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
          ) {
            Row(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              IconButton(
                onClick = { closeSearch() },
                modifier = Modifier.testTag("close_search_button"),
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Close search",
                  tint = MaterialTheme.colorScheme.onSurface,
                )
              }

              TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                  Text(
                    text = "Search tunings or notes (e.g. Drop D, DADGAD)...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                  color = MaterialTheme.colorScheme.onSurface,
                ),
                colors = TextFieldDefaults.colors(
                  focusedContainerColor = Color.Transparent,
                  unfocusedContainerColor = Color.Transparent,
                  disabledContainerColor = Color.Transparent,
                  focusedIndicatorColor = Color.Transparent,
                  unfocusedIndicatorColor = Color.Transparent,
                  disabledIndicatorColor = Color.Transparent,
                ),
                trailingIcon = {
                  if (searchQuery.isNotEmpty()) {
                    IconButton(
                      onClick = { searchQuery = "" },
                      modifier = Modifier.testTag("tuning_search_clear"),
                    ) {
                      Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                  }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier
                  .fillMaxWidth()
                  .focusRequester(searchFocusRequester)
                  .testTag("tuning_search_input"),
              )
            }
          }
        }
      }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

    // Content Area + Search Catalog Overlay
    Box(
      modifier = Modifier
        .fillMaxSize()
        .weight(1f),
    ) {
      GuitarTunerContent(
        selectedPreset = selectedPreset,
        onSelectPreset = ::selectPreset,
        favoritePresets = favoritePresets,
        onToggleFavorite = ::toggleFavorite,
        filteredPresets = filteredListPresets,
        filterFavoritesOnly = filterFavoritesOnly,
        selectedCategory = selectedListCategory,
        onToggleFavoritesFilter = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          filterFavoritesOnly = !filterFavoritesOnly
        },
        onSelectCategory = { category ->
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          selectedListCategoryName = if (selectedListCategory == category) null else category?.name
        },
        onOpenSearch = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          isSearchActive = true
        },
        activeString = activeString,
        selectedStringIndex = safeIndex,
        onPlayReferenceString = { idx ->
          val spec = selectedPreset.strings[idx]
          coroutineScope.launch {
            SoundSynthesizer.playPluckedTone(spec.frequencyHz)
          }
        },
        centsOffset = centsOffset,
        detectedFrequencyHz = if (isSignalActive) detectedFrequencyHz else activeString.frequencyHz,
        isSignalActive = isSignalActive,
        hasMicPermission = hasMicPermission,
        onRequestMicPermission = {
          permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        },
      )

      // Top-Expanding Tuning Search Overlay (matches PlantScheduleScreen & RecipeBookScreen)
      androidx.compose.animation.AnimatedVisibility(
        visible = isSearchActive,
        enter = fadeIn(animationSpec = tween(200)) + expandVertically(expandFrom = Alignment.Top),
        exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(shrinkTowards = Alignment.Top),
        modifier = Modifier.fillMaxSize(),
      ) {
        TuningCatalogSearchOverlay(
          allCatalogCount = GuitarTuningPreset.entries.size,
          query = searchQuery,
          selectedCategory = selectedSearchCategory,
          favoritesOnly = searchFavoritesOnly,
          favoritePresets = favoritePresets,
          selectedPreset = selectedPreset,
          matchingPresets = matchingSearchPresets,
          onSelectCategory = { category ->
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            selectedSearchCategoryName = category?.name
          },
          onToggleFavoritesOnly = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            searchFavoritesOnly = !searchFavoritesOnly
          },
          onSelectPreset = { preset ->
            selectPreset(preset)
            closeSearch()
          },
          onToggleFavorite = ::toggleFavorite,
        )
      }
    }
  }
}

@Composable
private fun GuitarTunerContent(
  selectedPreset: GuitarTuningPreset,
  onSelectPreset: (GuitarTuningPreset) -> Unit,
  favoritePresets: Set<GuitarTuningPreset>,
  onToggleFavorite: (GuitarTuningPreset) -> Unit,
  filteredPresets: List<GuitarTuningPreset>,
  filterFavoritesOnly: Boolean,
  selectedCategory: GuitarTuningCategory?,
  onToggleFavoritesFilter: () -> Unit,
  onSelectCategory: (GuitarTuningCategory?) -> Unit,
  onOpenSearch: () -> Unit,
  activeString: GuitarStringSpec,
  selectedStringIndex: Int,
  onPlayReferenceString: (Int) -> Unit,
  centsOffset: Float,
  detectedFrequencyHz: Double,
  isSignalActive: Boolean,
  hasMicPermission: Boolean,
  onRequestMicPermission: () -> Unit,
) {
  val isInTune = isSignalActive && abs(centsOffset) <= 3.0f
  val animatedCents by animateFloatAsState(
    targetValue = if (isSignalActive) centsOffset else 0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMediumLow,
    ),
    label = "TunerNeedleSpring",
  )

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("tuner_main_list"),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
  ) {
    if (!hasMicPermission) {
      item(key = "mic_permission_banner") {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
          ),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
              )
              Text(
                text = "Microphone access is required to auto-detect guitar strings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
              )
            }
            Button(
              onClick = onRequestMicPermission,
              modifier = Modifier.padding(start = 8.dp),
            ) {
              Text("Allow")
            }
          }
        }
      }
    }

    // 1. Hero Chromatic Gauge Card
    item(key = "tuner_gauge_card") {
      val statusText = when {
        !isSignalActive -> "LISTENING"
        isInTune -> "IN TUNE"
        centsOffset < 0f -> "FLAT (♭) · TUNE UP"
        else -> "SHARP (♯) · TUNE DOWN"
      }
      val statusColor = when {
        !isSignalActive -> MaterialTheme.colorScheme.surfaceVariant
        isInTune -> MaterialTheme.colorScheme.primary
        abs(centsOffset) <= 15f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
      }
      val statusContentColor = when {
        !isSignalActive -> MaterialTheme.colorScheme.onSurfaceVariant
        isInTune -> MaterialTheme.colorScheme.onPrimary
        abs(centsOffset) <= 15f -> MaterialTheme.colorScheme.onTertiary
        else -> MaterialTheme.colorScheme.onError
      }

      Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        border = BorderStroke(
          width = if (isInTune) 2.dp else 1.dp,
          color = if (isInTune) statusColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          // Header Row: String Badge + Tuning Status Badge
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Surface(
              shape = RoundedCornerShape(50),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.MusicNote,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp),
                )
                Text(
                  text = if (isSignalActive) {
                    "String ${activeString.stringNumber} · ${activeString.noteName}"
                  } else {
                    "No note detected"
                  },
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                )
              }
            }

            Badge(
              containerColor = statusColor,
              contentColor = statusContentColor,
            ) {
              Text(
                text = statusText,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
              )
            }
          }

          // Semicircular Tuner Gauge Canvas
          TunerGaugeCanvas(
            cents = animatedCents,
            isInTune = isInTune,
            modifier = Modifier
              .fillMaxWidth()
              .height(155.dp),
          )

          // Central Note & Frequency Readout
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "TARGET",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = String.format("%.2f Hz", activeString.frequencyHz),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              if (isSignalActive) {
                Text(
                  text = activeString.noteName,
                  modifier = Modifier.testTag("detected_note"),
                  style = MaterialTheme.typography.displaySmall,
                  fontWeight = FontWeight.Black,
                  color = if (isInTune) statusColor else MaterialTheme.colorScheme.onSurface,
                )
                val centsFormatted = if (centsOffset >= 0.05f) {
                  "+${centsOffset.roundToInt()} ¢"
                } else {
                  "${centsOffset.roundToInt()} ¢"
                }
                Text(
                  text = centsFormatted,
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  color = statusColor,
                )
              }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "CURRENT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = if (isSignalActive) String.format("%.2f Hz", detectedFrequencyHz) else "--",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    }

    // 2. Auto-Detected Guitar Strings Card (6th to 1st string)
    item(key = "guitar_strings_card") {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            selectedPreset.strings.forEachIndexed { idx, spec ->
              val isSelected = isSignalActive && idx == selectedStringIndex
              Surface(
                onClick = { onPlayReferenceString(idx) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) {
                  MaterialTheme.colorScheme.primaryContainer
                } else {
                  MaterialTheme.colorScheme.surfaceContainerHigh
                },
                border = BorderStroke(
                  width = if (isSelected) 2.dp else 1.dp,
                  color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                  },
                ),
              ) {
                Column(
                  modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                  Text(
                    text = "${spec.stringNumber}th".replace("1th", "1st").replace("2th", "2nd").replace("3th", "3rd"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                  Text(
                    text = spec.noteName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) {
                      MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                      MaterialTheme.colorScheme.onSurface
                    },
                  )
                  Text(
                    text = "${spec.frequencyHz.roundToInt()}Hz",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }
        }
      }
    }

    // 3. Tunings List Header & Filter Pills below the tuner and string display
    item(key = "tunings_section_header") {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = "Guitar Tunings",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Tap a tuning to switch • Tap ★ to favorite",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          TextButton(
            onClick = onOpenSearch,
            modifier = Modifier.testTag("open_tuning_search_text_button"),
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Search")
          }
        }

        // Filter Chips Row (All, Favorites, Categories)
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("tuning_filter_row"),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          item {
            FilterChip(
              selected = !filterFavoritesOnly && selectedCategory == null,
              onClick = {
                if (filterFavoritesOnly) onToggleFavoritesFilter()
                onSelectCategory(null)
              },
              label = { Text("All (${GuitarTuningPreset.entries.size})") },
              shape = RoundedCornerShape(20.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
              ),
              modifier = Modifier.testTag("tuning_filter_all"),
            )
          }
          item {
            FilterChip(
              selected = filterFavoritesOnly,
              onClick = onToggleFavoritesFilter,
              label = { Text("Favorites (${favoritePresets.size})") },
              leadingIcon = {
                Icon(
                  imageVector = if (filterFavoritesOnly) Icons.Default.Star else Icons.Default.StarBorder,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
              },
              shape = RoundedCornerShape(20.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
              ),
              modifier = Modifier.testTag("tuning_filter_favorites"),
            )
          }
          items(GuitarTuningCategory.entries) { category ->
            FilterChip(
              selected = selectedCategory == category,
              onClick = { onSelectCategory(category) },
              label = { Text(category.displayName) },
              shape = RoundedCornerShape(20.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
              ),
              modifier = Modifier.testTag("tuning_filter_category_${category.name}"),
            )
          }
        }
      }
    }

    // 4. Quick-Tap Tuning List Items
    if (filteredPresets.isEmpty()) {
      item(key = "empty_tunings_filter") {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("empty_tunings_filter_card"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = if (filterFavoritesOnly) {
                "No favorite tunings yet"
              } else {
                "No tunings match the selected filter"
              },
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Tap the ★ icon on any tuning to save it to your favorites for quick switching.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
            )
            TextButton(
              onClick = {
                if (filterFavoritesOnly) onToggleFavoritesFilter()
                onSelectCategory(null)
              },
              modifier = Modifier.testTag("show_all_tunings_button"),
            ) {
              Text("Show All Tunings")
            }
          }
        }
      }
    } else {
      items(
        items = filteredPresets,
        key = { "tuning_${it.name}" },
      ) { preset ->
        TuningPresetCard(
          preset = preset,
          isSelected = preset == selectedPreset,
          isFavorite = preset in favoritePresets,
          onSelect = { onSelectPreset(preset) },
          onToggleFavorite = { onToggleFavorite(preset) },
          cardTestTag = "tuning_card_${preset.name}",
          favoriteButtonTestTag = "favorite_tuning_button_${preset.name}",
        )
      }
    }
  }
}

@Composable
private fun TuningPresetCard(
  preset: GuitarTuningPreset,
  isSelected: Boolean,
  isFavorite: Boolean,
  onSelect: () -> Unit,
  onToggleFavorite: () -> Unit,
  cardTestTag: String,
  favoriteButtonTestTag: String,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
      } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
      },
    ),
    border = BorderStroke(
      width = if (isSelected) 2.dp else 1.dp,
      color = if (isSelected) {
        MaterialTheme.colorScheme.primary
      } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
      },
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onSelect)
      .testTag(cardTestTag),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f),
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
              if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
              } else {
                MaterialTheme.colorScheme.secondaryContainer
              },
            ),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = if (isSelected) Icons.Default.Check else Icons.Default.MusicNote,
            contentDescription = null,
            tint = if (isSelected) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.onSecondaryContainer
            },
            modifier = Modifier.size(20.dp),
          )
        }

        Column(modifier = Modifier.weight(1f)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = preset.label,
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
              Text(
                text = preset.category.displayName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
              )
            }
            if (isSelected) {
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
              ) {
                Text(
                  text = "Active",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
            }
          }

          Text(
            text = preset.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      IconButton(
        onClick = onToggleFavorite,
        modifier = Modifier
          .size(36.dp)
          .testTag(favoriteButtonTestTag),
      ) {
        Icon(
          imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
          contentDescription = if (isFavorite) {
            "Remove ${preset.label} from favorites"
          } else {
            "Add ${preset.label} to favorites"
          },
          tint = if (isFavorite) {
            MaterialTheme.colorScheme.primary
          } else {
            MaterialTheme.colorScheme.onSurfaceVariant
          },
        )
      }
    }
  }
}

@Composable
private fun TuningCatalogSearchOverlay(
  allCatalogCount: Int,
  query: String,
  selectedCategory: GuitarTuningCategory?,
  favoritesOnly: Boolean,
  favoritePresets: Set<GuitarTuningPreset>,
  selectedPreset: GuitarTuningPreset,
  matchingPresets: List<GuitarTuningPreset>,
  onSelectCategory: (GuitarTuningCategory?) -> Unit,
  onToggleFavoritesOnly: () -> Unit,
  onSelectPreset: (GuitarTuningPreset) -> Unit,
  onToggleFavorite: (GuitarTuningPreset) -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.background,
    modifier = Modifier
      .fillMaxSize()
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = {},
      )
      .testTag("catalog_search_overlay"),
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Category & Favorites Filter Chips Bar (Section 4 & 5.7: RoundedCornerShape(20.dp))
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
      ) {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("catalog_category_pills"),
        ) {
          item {
            FilterChip(
              selected = selectedCategory == null && !favoritesOnly,
              onClick = {
                if (favoritesOnly) onToggleFavoritesOnly()
                onSelectCategory(null)
              },
              label = {
                Text(
                  text = "All ($allCatalogCount)",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Medium,
                )
              },
              shape = RoundedCornerShape(20.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
              ),
              modifier = Modifier.testTag("category_chip_ALL"),
            )
          }
          item {
            FilterChip(
              selected = favoritesOnly,
              onClick = onToggleFavoritesOnly,
              leadingIcon = {
                Icon(
                  imageVector = if (favoritesOnly) Icons.Default.Star else Icons.Default.StarBorder,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
              },
              label = {
                Text(
                  text = "Favorites (${favoritePresets.size})",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Medium,
                )
              },
              shape = RoundedCornerShape(20.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
              ),
              modifier = Modifier.testTag("category_chip_FAVORITES"),
            )
          }
          items(GuitarTuningCategory.entries) { category ->
            val isSelected = selectedCategory == category
            FilterChip(
              selected = isSelected,
              onClick = {
                onSelectCategory(if (isSelected) null else category)
              },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.MusicNote,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
              },
              label = {
                Text(
                  text = category.displayName,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Medium,
                )
              },
              shape = RoundedCornerShape(20.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
              ),
              modifier = Modifier.testTag("category_chip_${category.name}"),
            )
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

      // Matching Tunings List
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .weight(1f)
          .testTag("catalog_results_section"),
        contentPadding = PaddingValues(
          start = 16.dp,
          top = 12.dp,
          end = 16.dp,
          bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        item(key = "catalog_header") {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column {
              Text(
                text = when {
                  query.isNotBlank() -> "Search Results (${matchingPresets.size})"
                  favoritesOnly -> "Favorite Tunings (${matchingPresets.size})"
                  selectedCategory != null -> "${selectedCategory.displayName} (${matchingPresets.size})"
                  else -> "Tuning Catalog (${matchingPresets.size})"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "Tap a tuning to switch • Tap ★ to favorite",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        if (matchingPresets.isEmpty()) {
          item(key = "catalog_empty") {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              ),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("catalog_empty_card"),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = if (query.isNotBlank()) {
                    "No tunings found matching \"$query\""
                  } else {
                    "No matching tunings"
                  },
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = "Try searching by tuning name (e.g. Drop D, Open G, DADGAD) or string notes.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center,
                )
              }
            }
          }
        } else {
          items(
            items = matchingPresets,
            key = { "catalog_${it.name}" },
          ) { preset ->
            TuningPresetCard(
              preset = preset,
              isSelected = preset == selectedPreset,
              isFavorite = preset in favoritePresets,
              onSelect = { onSelectPreset(preset) },
              onToggleFavorite = { onToggleFavorite(preset) },
              cardTestTag = "catalog_tuning_card_${preset.name}",
              favoriteButtonTestTag = "catalog_favorite_button_${preset.name}",
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TunerGaugeCanvas(
  cents: Float,
  isInTune: Boolean,
  modifier: Modifier = Modifier,
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
  val tertiaryColor = MaterialTheme.colorScheme.tertiary
  val errorColor = MaterialTheme.colorScheme.error
  val surfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
  val inTuneColor = primaryColor

  Canvas(modifier = modifier) {
    val cx = size.width / 2f
    val cy = size.height * 0.88f
    val radius = minOf(size.width * 0.44f, size.height * 0.78f)

    // Draw background arc (-160 deg to -20 deg, where -90 deg is top dead center)
    val startAngle = 195f
    val sweepAngle = 150f

    drawArc(
      color = surfaceVariantColor.copy(alpha = 0.16f),
      startAngle = startAngle,
      sweepAngle = sweepAngle,
      useCenter = false,
      topLeft = Offset(cx - radius, cy - radius),
      size = Size(radius * 2f, radius * 2f),
      style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round),
    )

    // Center "In Tune" sweet spot arc (-5 to +5 cents)
    val sweetSpotSweep = sweepAngle * (10f / 100f)
    drawArc(
      color = inTuneColor.copy(alpha = if (isInTune) 0.85f else 0.45f),
      startAngle = 270f - sweetSpotSweep / 2f,
      sweepAngle = sweetSpotSweep,
      useCenter = false,
      topLeft = Offset(cx - radius, cy - radius),
      size = Size(radius * 2f, radius * 2f),
      style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Butt),
    )

    // Tick marks from -50 to +50 cents every 5 cents
    for (c in -50..50 step 5) {
      val fraction = c / 50f // -1f .. +1f
      val angleDeg = 270f + fraction * (sweepAngle / 2f)
      val angleRad = angleDeg * (PI / 180.0)
      val isMajor = c % 25 == 0
      val innerR = radius * if (isMajor) 0.78f else 0.84f
      val outerR = radius * 0.92f

      val tickColor = when {
        c == 0 -> inTuneColor
        isMajor -> surfaceVariantColor.copy(alpha = 0.85f)
        else -> surfaceVariantColor.copy(alpha = 0.4f)
      }

      drawLine(
        color = tickColor,
        start = Offset(
          x = cx + (innerR * cos(angleRad)).toFloat(),
          y = cy + (innerR * sin(angleRad)).toFloat(),
        ),
        end = Offset(
          x = cx + (outerR * cos(angleRad)).toFloat(),
          y = cy + (outerR * sin(angleRad)).toFloat(),
        ),
        strokeWidth = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx(),
        cap = StrokeCap.Round,
      )
    }

    // Needle angle based on cents (-50..+50)
    val clampedCents = cents.coerceIn(-50f, 50f)
    val needleAngleDeg = 270f + (clampedCents / 50f) * (sweepAngle / 2f)
    val needleAngleRad = needleAngleDeg * (PI / 180.0)
    val needleColor = when {
      isInTune -> inTuneColor
      abs(clampedCents) <= 15f -> tertiaryColor
      else -> errorColor
    }

    // Soft glow behind needle tip
    val tipX = cx + (radius * 0.88f * cos(needleAngleRad)).toFloat()
    val tipY = cy + (radius * 0.88f * sin(needleAngleRad)).toFloat()
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(needleColor.copy(alpha = 0.35f), Color.Transparent),
        center = Offset(tipX, tipY),
        radius = 28.dp.toPx(),
      ),
      radius = 28.dp.toPx(),
      center = Offset(tipX, tipY),
    )

    // Needle shaft
    drawLine(
      color = needleColor,
      start = Offset(cx, cy),
      end = Offset(tipX, tipY),
      strokeWidth = 4.5.dp.toPx(),
      cap = StrokeCap.Round,
    )

    // Pivot cap
    drawCircle(
      color = needleColor,
      radius = 10.dp.toPx(),
      center = Offset(cx, cy),
    )
    drawCircle(
      color = onPrimaryColor,
      radius = 4.dp.toPx(),
      center = Offset(cx, cy),
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun GuitarTunerScreenPreview() {
  ToolboxTheme {
    GuitarTunerScreen()
  }
}
