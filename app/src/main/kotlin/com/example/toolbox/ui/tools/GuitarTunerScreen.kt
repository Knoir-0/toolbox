package com.example.toolbox.ui.tools

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
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
) {
  // Standard & lowered standard tunings
  STANDARD("Standard", tuning("E2", "A2", "D3", "G3", "B3", "E4"), "E standard default"),
  HALF_STEP_DOWN("Eb Standard", tuning("Eb2", "Ab2", "Db3", "Gb3", "Bb3", "Eb4"), "half step down D# standard"),
  D_STANDARD("D Standard", tuning("D2", "G2", "C3", "F3", "A3", "D4"), "whole step down full step"),
  C_SHARP_STANDARD("C# Standard", tuning("Db2", "Gb2", "B2", "E3", "Ab3", "Db4"), "Db standard one and a half steps down"),
  C_STANDARD("C Standard", tuning("C2", "F2", "Bb2", "Eb3", "G3", "C4"), "two steps down"),
  B_STANDARD("B Standard", tuning("B1", "E2", "A2", "D3", "F#3", "B3"), "baritone"),

  // Drop tunings
  DROP_D("Drop D", tuning("D2", "A2", "D3", "G3", "B3", "E4")),
  DROP_C_SHARP("Drop C#", tuning("Db2", "Ab2", "Db3", "Gb3", "Bb3", "Eb4"), "drop Db"),
  DROP_C("Drop C", tuning("C2", "G2", "C3", "F3", "A3", "D4")),
  DROP_B("Drop B", tuning("B1", "F#2", "B2", "E3", "G#3", "C#4")),
  DROP_A("Drop A", tuning("A1", "E2", "A2", "D3", "F#3", "B3")),
  DOUBLE_DROP_D("Double Drop D", tuning("D2", "A2", "D3", "G3", "B3", "D4")),

  // Open tunings
  OPEN_G("Open G", tuning("D2", "G2", "D3", "G3", "B3", "D4"), "slide blues"),
  OPEN_D("Open D", tuning("D2", "A2", "D3", "F#3", "A3", "D4"), "slide"),
  OPEN_E("Open E", tuning("E2", "B2", "E3", "G#3", "B3", "E4"), "slide"),
  OPEN_A("Open A", tuning("E2", "A2", "E3", "A3", "C#4", "E4"), "slide"),
  OPEN_C("Open C", tuning("C2", "G2", "C3", "G3", "C4", "E4")),
  OPEN_D_MINOR("Open D Minor", tuning("D2", "A2", "D3", "F3", "A3", "D4"), "Dm cross note"),

  // Modal & alternative tunings
  DADGAD("DADGAD", tuning("D2", "A2", "D3", "G3", "A3", "D4"), "Dsus4 celtic"),
  ALL_FOURTHS("All Fourths", tuning("E2", "A2", "D3", "G3", "C4", "F4"), "P4"),
  NEW_STANDARD("New Standard", tuning("C2", "G2", "D3", "A3", "E4", "G4"), "NST Fripp"),
  NASHVILLE("Nashville", tuning("E3", "A3", "D4", "G4", "B3", "E4"), "high strung"),
  ;

  /** Note names, low to high, e.g. "E A D G B E". */
  val subtitle: String get() = strings.joinToString(" ") { it.noteName }

  fun matches(query: String): Boolean {
    val terms = query.trim().lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (terms.isEmpty()) return true
    val haystack = "$label $subtitle ${subtitle.replace(" ", "")} $keywords".lowercase()
    return terms.all { haystack.contains(it) }
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
@Composable
fun GuitarTunerScreen(modifier: Modifier = Modifier) {
  var selectedPreset by rememberSaveable { mutableStateOf(GuitarTuningPreset.STANDARD) }
  var selectedStringIndex by rememberSaveable { mutableIntStateOf(0) } // 0 = 6th string (low E)
  var centsOffset by rememberSaveable { mutableFloatStateOf(0f) }
  var detectedFrequencyHz by rememberSaveable {
    mutableDoubleStateOf(GuitarTuningPreset.STANDARD.strings.first().frequencyHz)
  }
  var isSignalActive by remember { mutableStateOf(false) }

  val context = LocalContext.current
  val isInspectionMode = LocalInspectionMode.current
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

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    GuitarTunerContent(
      selectedPreset = selectedPreset,
      onSelectPreset = {
        selectedPreset = it
        selectedStringIndex = 0
        centsOffset = 0f
        detectedFrequencyHz = it.strings.first().frequencyHz
        isSignalActive = false
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
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TuningPresetSelector(
  selectedPreset: GuitarTuningPreset,
  onSelectPreset: (GuitarTuningPreset) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  var query by remember { mutableStateOf("") }
  val focusManager = LocalFocusManager.current
  val filteredPresets = remember(query) {
    GuitarTuningPreset.entries.filter { it.matches(query) }
  }

  fun close() {
    expanded = false
    query = ""
    focusManager.clearFocus()
  }

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { if (it) expanded = true else close() },
  ) {
    OutlinedTextField(
      value = if (expanded) query else selectedPreset.label,
      onValueChange = {
        query = it
        expanded = true
      },
      modifier = Modifier
        .fillMaxWidth()
        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
        .testTag("tuning_selector"),
      label = { Text("Tuning") },
      placeholder = { Text("Search tunings or notes") },
      leadingIcon = {
        Icon(
          imageVector = if (expanded) Icons.Default.Search else Icons.Default.MusicNote,
          contentDescription = null,
        )
      },
      suffix = if (!expanded) {
        {
          Text(
            text = selectedPreset.subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      } else {
        null
      },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      singleLine = true,
      shape = RoundedCornerShape(16.dp),
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
      keyboardActions = KeyboardActions(
        onDone = {
          filteredPresets.firstOrNull()?.let(onSelectPreset)
          close()
        },
      ),
    )
    ExposedDropdownMenu(
      expanded = expanded,
      onDismissRequest = { close() },
    ) {
      if (filteredPresets.isEmpty()) {
        DropdownMenuItem(
          text = { Text("No matching tunings") },
          onClick = {},
          enabled = false,
          contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
        )
      }
      filteredPresets.forEach { preset ->
        val selected = preset == selectedPreset
        DropdownMenuItem(
          text = {
            Column {
              Text(
                text = preset.label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
              )
              Text(
                text = preset.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          },
          onClick = {
            onSelectPreset(preset)
            close()
          },
          trailingIcon = if (selected) {
            {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          } else {
            null
          },
          contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
        )
      }
    }
  }
}

@Composable
private fun GuitarTunerContent(
  selectedPreset: GuitarTuningPreset,
  onSelectPreset: (GuitarTuningPreset) -> Unit,
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
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    contentPadding = PaddingValues(bottom = 12.dp),
  ) {
    if (!hasMicPermission) {
      item {
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

    // 1. Searchable Tuning Preset Selector
    item {
      TuningPresetSelector(
        selectedPreset = selectedPreset,
        onSelectPreset = onSelectPreset,
      )
    }

    // 2. Hero Chromatic Gauge Card
    item {
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

    // 3. Auto-Detected Guitar Strings Card (6th to 1st string)
    item {
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
