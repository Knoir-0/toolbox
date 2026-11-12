package com.example.toolbox.ui.tools

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toolbox.ui.theme.ToolboxTheme
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TimeSignature(
  val label: String,
  val beatsPerMeasure: Int,
  val noteValue: Int,
  val isCustom: Boolean = false,
) {
  companion object {
    val FOUR_FOUR = TimeSignature("4/4", 4, 4)
    val THREE_FOUR = TimeSignature("3/4", 3, 4)
    val TWO_FOUR = TimeSignature("2/4", 2, 4)
    val FIVE_FOUR = TimeSignature("5/4", 5, 4)
    val SIX_EIGHT = TimeSignature("6/8", 6, 8)
    val FIVE_EIGHT = TimeSignature("5/8", 5, 8)

    val defaultSignatures = listOf(
      FOUR_FOUR,
      THREE_FOUR,
      TWO_FOUR,
      FIVE_FOUR,
      SIX_EIGHT,
      FIVE_EIGHT,
    )

    fun fromBeatsAndNote(beats: Int, noteValue: Int, isCustom: Boolean = false): TimeSignature {
      return TimeSignature(
        label = "$beats/$noteValue",
        beatsPerMeasure = beats,
        noteValue = noteValue,
        isCustom = isCustom,
      )
    }

    fun parse(label: String, isCustom: Boolean = false): TimeSignature? {
      val parts = label.split("/")
      if (parts.size != 2) return null
      val beats = parts[0].toIntOrNull() ?: return null
      val note = parts[1].toIntOrNull() ?: return null
      if (beats <= 0 || note <= 0) return null
      return fromBeatsAndNote(beats, note, isCustom)
    }
  }
}

val TimeSignatureSaver = Saver<TimeSignature, String>(
  save = { "${it.beatsPerMeasure}/${it.noteValue}:${it.isCustom}" },
  restore = { saved ->
    val parts = saved.split(":")
    val label = parts.getOrNull(0) ?: "4/4"
    val isCustom = parts.getOrNull(1)?.toBooleanStrictOrNull() ?: false
    TimeSignature.parse(label, isCustom) ?: TimeSignature.FOUR_FOUR
  },
)

private val CompactPillHeight = 28.dp

enum class BeatSubdivision(val label: String, val ticksPerBeat: Int) {
  QUARTER("Quarter", 1),
  EIGHTH("Eighth", 2),
  TRIPLET("Triplet", 3),
  SIXTEENTH("Sixteenth", 4),
}

/**
 * Lightweight synthesizer for metronome clicks.
 * Caches synthesized PCM buffers per frequency and deterministically releases
 * [AudioTrack] instances after each click so native AudioFlinger tracks are never leaked
 * during long-running metronome sessions.
 * Safely catches audio initialization exceptions in headless test environments.
 */
internal object MetronomeClickSynthesizer {
  internal const val SAMPLE_RATE = 22050
  internal const val CLICK_DURATION_MS = 35
  private const val TRACK_RELEASE_DELAY_MS = 100L
  private const val MAX_ACTIVE_TRACKS = 6

  private val pcmCache = ConcurrentHashMap<Double, ShortArray>()
  private val activeTracksLock = Any()
  private val activeTracks = ArrayDeque<AudioTrack>()

  internal fun synthesizeClickPcm(
    frequencyHz: Double,
    sampleRate: Int = SAMPLE_RATE,
    durationMs: Int = CLICK_DURATION_MS,
    minBufferBytes: Int = 0,
  ): ShortArray {
    val activeSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
    val minSamples = ((minBufferBytes + 1) / 2).coerceAtLeast(activeSamples)
    val samples = ShortArray(minSamples)
    for (i in 0 until activeSamples) {
      val t = i.toDouble() / sampleRate
      val envelope = exp(-95.0 * t)
      val wave = sin(2.0 * PI * frequencyHz * t)
      samples[i] = (wave * envelope * Short.MAX_VALUE * 0.8).toInt()
        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
    return samples
  }

  suspend fun playMetronomeClick(frequencyHz: Double) = withContext(Dispatchers.Default) {
    var track: AudioTrack? = null
    try {
      val minBufferBytes = try {
        AudioTrack.getMinBufferSize(
          SAMPLE_RATE,
          AudioFormat.CHANNEL_OUT_MONO,
          AudioFormat.ENCODING_PCM_16BIT,
        ).coerceAtLeast(0)
      } catch (_: Throwable) {
        0
      }
      val samples = pcmCache.getOrPut(frequencyHz) {
        synthesizeClickPcm(
          frequencyHz = frequencyHz,
          sampleRate = SAMPLE_RATE,
          durationMs = CLICK_DURATION_MS,
          minBufferBytes = minBufferBytes,
        )
      }
      track = startPcmTrack(samples, SAMPLE_RATE)
      delay(TRACK_RELEASE_DELAY_MS)
    } catch (_: Throwable) {
      // Ignore in test / no-audio environments
    } finally {
      if (track != null) {
        synchronized(activeTracksLock) {
          activeTracks.remove(track)
        }
        releaseTrackQuietly(track)
      }
    }
  }

  fun releaseAll() {
    val tracksToRelease = synchronized(activeTracksLock) {
      val copy = activeTracks.toList()
      activeTracks.clear()
      copy
    }
    tracksToRelease.forEach { releaseTrackQuietly(it) }
  }

  private fun startPcmTrack(samples: ShortArray, sampleRate: Int): AudioTrack {
    val evicted = mutableListOf<AudioTrack>()
    synchronized(activeTracksLock) {
      while (activeTracks.size >= MAX_ACTIVE_TRACKS) {
        evicted.add(activeTracks.removeFirst())
      }
    }
    evicted.forEach { releaseTrackQuietly(it) }

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
      .setBufferSizeInBytes(bufferSizeBytes)
      .setTransferMode(AudioTrack.MODE_STATIC)
      .build()

    try {
      track.write(samples, 0, samples.size)
      track.play()
      synchronized(activeTracksLock) {
        activeTracks.addLast(track)
      }
      return track
    } catch (t: Throwable) {
      releaseTrackQuietly(track)
      throw t
    }
  }

  private fun releaseTrackQuietly(track: AudioTrack?) {
    if (track == null) return
    try {
      if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
        track.stop()
      }
    } catch (_: Throwable) {
    }
    try {
      track.release()
    } catch (_: Throwable) {
    }
  }
}

@Composable
fun MetronomeScreen(modifier: Modifier = Modifier) {
  var bpm by rememberSaveable { mutableIntStateOf(120) }
  var isMetronomePlaying by rememberSaveable { mutableStateOf(false) }
  var timeSignature by rememberSaveable(stateSaver = TimeSignatureSaver) { mutableStateOf(TimeSignature.FOUR_FOUR) }
  val customTimeSignatures = rememberSaveable { mutableStateListOf<String>() }
  var subdivision by rememberSaveable { mutableStateOf(BeatSubdivision.QUARTER) }
  var accentFirstBeat by rememberSaveable { mutableStateOf(true) }
  var currentBeat by remember { mutableIntStateOf(0) }
  var currentSubTick by remember { mutableIntStateOf(0) }
  var beatPulseCounter by remember { mutableLongStateOf(0L) }
  val tapTimestamps = remember { mutableStateListOf<Long>() }

  val allTimeSignatures = remember(customTimeSignatures.toList()) {
    val customs = customTimeSignatures.mapNotNull { TimeSignature.parse(it, isCustom = true) }
    TimeSignature.defaultSignatures + customs
  }

  LaunchedEffect(isMetronomePlaying, bpm, timeSignature, subdivision, accentFirstBeat) {
    if (!isMetronomePlaying) {
      currentBeat = 0
      currentSubTick = 0
      MetronomeClickSynthesizer.releaseAll()
      return@LaunchedEffect
    }
    var beatIdx = 0
    var subIdx = 0
    while (isActive && isMetronomePlaying) {
      currentBeat = beatIdx
      currentSubTick = subIdx
      beatPulseCounter++

      val clickFreq = when {
        subIdx > 0 -> 540.0
        beatIdx == 0 && accentFirstBeat -> 1080.0
        else -> 760.0
      }
      launch { MetronomeClickSynthesizer.playMetronomeClick(clickFreq) }

      val beatIntervalMs = (60_000.0 / bpm.coerceIn(30, 240)).toLong()
      val tickIntervalMs = (beatIntervalMs / subdivision.ticksPerBeat).coerceAtLeast(25L)
      delay(tickIntervalMs)

      subIdx++
      if (subIdx >= subdivision.ticksPerBeat) {
        subIdx = 0
        beatIdx = (beatIdx + 1) % timeSignature.beatsPerMeasure
      }
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      isMetronomePlaying = false
      MetronomeClickSynthesizer.releaseAll()
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    MetronomeContent(
      bpm = bpm,
      onBpmChange = { bpm = it.coerceIn(30, 240) },
      isPlaying = isMetronomePlaying,
      onTogglePlay = { isMetronomePlaying = !isMetronomePlaying },
      timeSignature = timeSignature,
      allTimeSignatures = allTimeSignatures,
      onSelectTimeSignature = {
        timeSignature = it
        currentBeat = 0
      },
      onAddCustomTimeSignature = { newSig ->
        if (!customTimeSignatures.contains(newSig.label) && TimeSignature.defaultSignatures.none { it.label == newSig.label }) {
          customTimeSignatures.add(newSig.label)
        }
        timeSignature = newSig
        currentBeat = 0
      },
      onRemoveCustomTimeSignature = { remSig ->
        customTimeSignatures.remove(remSig.label)
        if (timeSignature.label == remSig.label) {
          timeSignature = TimeSignature.FOUR_FOUR
          currentBeat = 0
        }
      },
      subdivision = subdivision,
      onSelectSubdivision = { subdivision = it },
      accentFirstBeat = accentFirstBeat,
      onToggleAccent = { accentFirstBeat = !accentFirstBeat },
      currentBeat = currentBeat,
      beatPulseCounter = beatPulseCounter,
      onTapTempo = {
        val now = System.currentTimeMillis()
        if (tapTimestamps.isNotEmpty() && now - tapTimestamps.last() > 2500L) {
          tapTimestamps.clear()
        }
        tapTimestamps.add(now)
        if (tapTimestamps.size > 5) {
          tapTimestamps.removeAt(0)
        }
        if (tapTimestamps.size >= 2) {
          val intervals = tapTimestamps.zipWithNext { a, b -> b - a }
          val avgMs = intervals.average()
          if (avgMs > 0) {
            bpm = (60_000.0 / avgMs).roundToInt().coerceIn(30, 240)
          }
        }
      },
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MetronomeContent(
  bpm: Int,
  onBpmChange: (Int) -> Unit,
  isPlaying: Boolean,
  onTogglePlay: () -> Unit,
  timeSignature: TimeSignature,
  allTimeSignatures: List<TimeSignature>,
  onSelectTimeSignature: (TimeSignature) -> Unit,
  onAddCustomTimeSignature: (TimeSignature) -> Unit,
  onRemoveCustomTimeSignature: (TimeSignature) -> Unit,
  subdivision: BeatSubdivision,
  onSelectSubdivision: (BeatSubdivision) -> Unit,
  accentFirstBeat: Boolean,
  onToggleAccent: () -> Unit,
  currentBeat: Int,
  beatPulseCounter: Long,
  onTapTempo: () -> Unit,
) {
  var showAddCustomDialog by remember { mutableStateOf(false) }

  val tempoMarking = remember(bpm) {
    when {
      bpm < 45 -> "Grave"
      bpm < 60 -> "Largo"
      bpm < 66 -> "Larghetto"
      bpm < 76 -> "Adagio"
      bpm < 108 -> "Andante"
      bpm < 120 -> "Moderato"
      bpm < 156 -> "Allegro"
      bpm < 176 -> "Vivace"
      bpm < 200 -> "Presto"
      else -> "Prestissimo"
    }
  }

  val pendulumSwing by animateFloatAsState(
    targetValue = if (!isPlaying) {
      0f
    } else if (currentBeat % 2 == 0) {
      -28f
    } else {
      28f
    }
    ,
    animationSpec = tween(
      durationMillis = (60_000 / bpm.coerceIn(30, 240)).coerceIn(200, 1500),
    ),
    label = "PendulumSwingAnimation",
  )

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    contentPadding = PaddingValues(bottom = 12.dp),
  ) {
    // 1. Hero Metronome Visualizer & BPM Card
    item {
      Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        border = BorderStroke(
          width = if (isPlaying) 2.dp else 1.dp,
          color = if (isPlaying) {
            MaterialTheme.colorScheme.primary
          } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
          },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          // Header: Tempo Marking & Time Signature Badge
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Badge(
              containerColor = MaterialTheme.colorScheme.primaryContainer,
              contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
              Text(
                text = tempoMarking.uppercase(),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
              )
            }

            Text(
              text = "${timeSignature.label} · ${subdivision.label}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          // Visual Beat Dots Row
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
              if (timeSignature.beatsPerMeasure > 8) 6.dp else 10.dp,
              Alignment.CenterHorizontally,
            ),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            val totalBeats = timeSignature.beatsPerMeasure
            val isCompact = totalBeats > 12
            val isMedium = totalBeats in 7..12
            val normalSize = when {
              isCompact -> 22.dp
              isMedium -> 26.dp
              else -> 30.dp
            }
            val activeSize = when {
              isCompact -> 26.dp
              isMedium -> 32.dp
              else -> 38.dp
            }

            for (beatIndex in 0 until totalBeats) {
              val isCurrent = isPlaying && currentBeat == beatIndex
              val isDownbeat = beatIndex == 0 && accentFirstBeat
              val circleSize = if (isCurrent) activeSize else normalSize
              val bgColor = when {
                isCurrent && isDownbeat -> MaterialTheme.colorScheme.tertiary
                isCurrent -> MaterialTheme.colorScheme.primary
                isDownbeat -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceContainerHighest
              }
              val textColor = when {
                isCurrent && isDownbeat -> MaterialTheme.colorScheme.onTertiary
                isCurrent -> MaterialTheme.colorScheme.onPrimary
                isDownbeat -> MaterialTheme.colorScheme.onTertiaryContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
              }

              Box(
                modifier = Modifier
                  .size(circleSize)
                  .background(bgColor, CircleShape),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = "${beatIndex + 1}",
                  style = if (isCompact) {
                    MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)
                  } else if (isMedium) {
                    MaterialTheme.typography.labelMedium
                  } else {
                    MaterialTheme.typography.labelLarge
                  },
                  fontWeight = FontWeight.Black,
                  color = textColor,
                )
              }
            }
          }

          // Pendulum & BPM Readout
          MetronomePendulumCanvas(
            swingAngleDeg = pendulumSwing,
            isPlaying = isPlaying,
            isAccentBeat = isPlaying && currentBeat == 0 && accentFirstBeat,
            modifier = Modifier
              .fillMaxWidth()
              .height(110.dp),
          )

          // BPM Stepper Row (-5, -1, BPM, +1, +5)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            FilledTonalButton(
              onClick = { onBpmChange(bpm - 5) },
              contentPadding = PaddingValues(horizontal = 10.dp),
            ) {
              Text("-5", fontWeight = FontWeight.Bold)
            }

            FilledTonalIconButton(onClick = { onBpmChange(bpm - 1) }) {
              Icon(Icons.Default.Remove, contentDescription = "Decrease BPM")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "$bpm",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
              )
              Text(
                text = "BPM",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            FilledTonalIconButton(onClick = { onBpmChange(bpm + 1) }) {
              Icon(Icons.Default.Add, contentDescription = "Increase BPM")
            }

            FilledTonalButton(
              onClick = { onBpmChange(bpm + 5) },
              contentPadding = PaddingValues(horizontal = 10.dp),
            ) {
              Text("+5", fontWeight = FontWeight.Bold)
            }
          }

          SlimDotSlider(
            value = bpm.toFloat(),
            onValueChange = { onBpmChange(it.roundToInt()) },
            valueRange = 30f..240f,
          )

          // Play / Pause + Tap Tempo Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            OutlinedButton(
              onClick = onTapTempo,
              modifier = Modifier
                .weight(1f)
                .height(52.dp),
              shape = CircleShape,
            ) {
              Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                modifier = Modifier
                  .padding(end = 6.dp)
                  .size(20.dp),
              )
              Text(
                text = "Tap Tempo",
                fontWeight = FontWeight.Bold,
              )
            }

            Button(
              onClick = onTogglePlay,
              modifier = Modifier
                .weight(1f)
                .height(52.dp),
              shape = CircleShape,
              colors = if (isPlaying) {
                ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.error,
                  contentColor = MaterialTheme.colorScheme.onError,
                )
              } else {
                ButtonDefaults.buttonColors()
              },
            ) {
              Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier
                  .padding(end = 6.dp)
                  .size(22.dp),
              )
              Text(
                text = if (isPlaying) "Pause" else "Play",
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    }

    // 2. Time Signature & Subdivision Settings Card
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
      ) {
        // Drop the 48dp minimum touch-target padding so the pills sit compactly
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Time Signature",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
              )
              FilterChip(
                selected = accentFirstBeat,
                onClick = onToggleAccent,
                label = { Text("Accent Beat 1") },
                leadingIcon = if (accentFirstBeat) {
                  {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp),
                    )
                  }
                } else {
                  null
                },
                modifier = Modifier.height(CompactPillHeight),
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                  selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                  selectedLeadingIconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ),
              )
            }

            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              allTimeSignatures.forEach { sig ->
                val selected = sig.label == timeSignature.label
                FilterChip(
                  selected = selected,
                  onClick = { onSelectTimeSignature(sig) },
                  label = {
                    Text(
                      text = sig.label,
                      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                  },
                  trailingIcon = if (sig.isCustom) {
                    {
                      Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove ${sig.label}",
                        modifier = Modifier
                          .size(14.dp)
                          .clickable { onRemoveCustomTimeSignature(sig) },
                      )
                    }
                  } else {
                    null
                  },
                  modifier = Modifier.height(CompactPillHeight),
                  shape = RoundedCornerShape(50),
                )
              }

              FilterChip(
                selected = false,
                onClick = { showAddCustomDialog = true },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                  )
                },
                label = {
                  Text(
                    text = "Custom",
                    fontWeight = FontWeight.SemiBold,
                  )
                },
                modifier = Modifier.height(CompactPillHeight),
                shape = RoundedCornerShape(50),
              )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Text(
              text = "Subdivision",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
            )

            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              BeatSubdivision.entries.forEach { sub ->
                val selected = sub == subdivision
                FilterChip(
                  selected = selected,
                  onClick = { onSelectSubdivision(sub) },
                  leadingIcon = {
                    SubdivisionIcon(
                      subdivision = sub,
                      modifier = Modifier.size(16.dp),
                    )
                  },
                  label = {
                    Text(
                      text = sub.label,
                      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                  },
                  modifier = Modifier.height(CompactPillHeight),
                  shape = RoundedCornerShape(50),
                )
              }
            }
          }
        }
      }
    }
  }

  if (showAddCustomDialog) {
    AddCustomTimeSignatureDialog(
      onDismiss = { showAddCustomDialog = false },
      onAdd = { newSig ->
        onAddCustomTimeSignature(newSig)
        showAddCustomDialog = false
      },
    )
  }
}

@Composable
private fun AddCustomTimeSignatureDialog(
  onDismiss: () -> Unit,
  onAdd: (TimeSignature) -> Unit,
) {
  var beatsInput by remember { mutableStateOf("7") }
  var selectedNoteValue by remember { mutableIntStateOf(4) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    text = {
      CustomTimeSignatureDialogContent(
        beatsInput = beatsInput,
        onBeatsInputChange = {
          beatsInput = it
          errorMessage = null
        },
        selectedNoteValue = selectedNoteValue,
        onSelectNoteValue = { selectedNoteValue = it },
        errorMessage = errorMessage,
      )
    },
    confirmButton = {
      Button(
        onClick = {
          val beats = beatsInput.toIntOrNull()
          if (beats == null || beats !in 1..64) {
            errorMessage = "Enter a valid number of beats (1–64)"
          } else {
            onAdd(TimeSignature.fromBeatsAndNote(beats, selectedNoteValue, isCustom = true))
          }
        },
      ) {
        Text("Add")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    },
  )
}

@Composable
private fun CustomTimeSignatureDialogContent(
  beatsInput: String,
  onBeatsInputChange: (String) -> Unit,
  selectedNoteValue: Int,
  onSelectNoteValue: (Int) -> Unit,
  errorMessage: String?,
  modifier: Modifier = Modifier,
) {
  val beatsInt = beatsInput.toIntOrNull() ?: 0
  val previewLabel = if (beatsInt in 1..64) "$beatsInt/$selectedNoteValue" else "?/$selectedNoteValue"

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(16.dp))
        .padding(vertical = 12.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = previewLabel,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.primary,
      )
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(
        text = "Beats per measure (1–64)",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        FilledTonalIconButton(
          onClick = {
            val current = beatsInput.toIntOrNull() ?: 4
            if (current > 1) onBeatsInputChange((current - 1).toString())
          },
        ) {
          Icon(Icons.Default.Remove, contentDescription = "Decrease beats")
        }

        OutlinedTextField(
          value = beatsInput,
          onValueChange = { input ->
            val filtered = input.filter { it.isDigit() }.take(2)
            onBeatsInputChange(filtered)
          },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(16.dp),
          singleLine = true,
          textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )

        FilledTonalIconButton(
          onClick = {
            val current = beatsInput.toIntOrNull() ?: 4
            if (current < 64) onBeatsInputChange((current + 1).toString())
          },
        ) {
          Icon(Icons.Default.Add, contentDescription = "Increase beats")
        }
      }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(
        text = "Beat unit (note value)",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        listOf(2, 4, 8, 16).forEach { noteVal ->
          val isSelected = selectedNoteValue == noteVal
          FilterChip(
            selected = isSelected,
            onClick = { onSelectNoteValue(noteVal) },
            label = {
              Text(
                text = "/$noteVal",
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
              )
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(50),
          )
        }
      }
    }

    if (errorMessage != null) {
      Text(
        text = errorMessage,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
      )
    }
  }
}

@Composable
private fun MetronomePendulumCanvas(
  swingAngleDeg: Float,
  isPlaying: Boolean,
  isAccentBeat: Boolean,
  modifier: Modifier = Modifier,
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val tertiaryColor = MaterialTheme.colorScheme.tertiary
  val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)

  Canvas(modifier = modifier) {
    val cx = size.width / 2f
    val pivotY = size.height * 0.9f
    val armLength = size.height * 0.76f

    // Swing guide arc
    drawArc(
      color = trackColor,
      startAngle = 240f,
      sweepAngle = 60f,
      useCenter = false,
      topLeft = Offset(cx - armLength, pivotY - armLength),
      size = Size(armLength * 2f, armLength * 2f),
      style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
    )

    val angleRad = (270f + swingAngleDeg) * (PI / 180.0)
    val bobX = cx + (armLength * cos(angleRad)).toFloat()
    val bobY = pivotY + (armLength * sin(angleRad)).toFloat()
    val activeColor = if (isAccentBeat) tertiaryColor else primaryColor

    // Pendulum rod
    drawLine(
      color = activeColor,
      start = Offset(cx, pivotY),
      end = Offset(bobX, bobY),
      strokeWidth = 4.dp.toPx(),
      cap = StrokeCap.Round,
    )

    // Pendulum weight bob
    if (isPlaying) {
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(activeColor.copy(alpha = 0.4f), Color.Transparent),
          center = Offset(bobX, bobY),
          radius = 24.dp.toPx(),
        ),
        radius = 24.dp.toPx(),
        center = Offset(bobX, bobY),
      )
    }
    drawCircle(
      color = activeColor,
      radius = 11.dp.toPx(),
      center = Offset(bobX, bobY),
    )
    drawCircle(
      color = activeColor,
      radius = 6.dp.toPx(),
      center = Offset(cx, pivotY),
    )
  }
}

@Composable
fun SubdivisionIcon(
  subdivision: BeatSubdivision,
  modifier: Modifier = Modifier,
  tint: Color = LocalContentColor.current,
) {
  val textMeasurer = rememberTextMeasurer()
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    when (subdivision) {
      BeatSubdivision.QUARTER -> {
        val headWidth = w * 0.44f
        val headHeight = h * 0.32f
        val headCenterX = w * 0.40f
        val headCenterY = h * 0.74f

        rotate(degrees = -25f, pivot = Offset(headCenterX, headCenterY)) {
          drawOval(
            color = tint,
            topLeft = Offset(headCenterX - headWidth / 2f, headCenterY - headHeight / 2f),
            size = Size(headWidth, headHeight),
          )
        }

        val stemX = headCenterX + headWidth * 0.36f
        val stemTopY = h * 0.12f
        val stemBottomY = headCenterY
        val stemWidth = 1.6.dp.toPx()

        drawLine(
          color = tint,
          start = Offset(stemX, stemBottomY),
          end = Offset(stemX, stemTopY),
          strokeWidth = stemWidth,
          cap = StrokeCap.Round,
        )
      }

      BeatSubdivision.EIGHTH -> {
        val headWidth = w * 0.34f
        val headHeight = h * 0.24f
        val head1CenterX = w * 0.22f
        val head1CenterY = h * 0.77f
        val head2CenterX = w * 0.68f
        val head2CenterY = h * 0.77f

        rotate(degrees = -25f, pivot = Offset(head1CenterX, head1CenterY)) {
          drawOval(
            color = tint,
            topLeft = Offset(head1CenterX - headWidth / 2f, head1CenterY - headHeight / 2f),
            size = Size(headWidth, headHeight),
          )
        }
        rotate(degrees = -25f, pivot = Offset(head2CenterX, head2CenterY)) {
          drawOval(
            color = tint,
            topLeft = Offset(head2CenterX - headWidth / 2f, head2CenterY - headHeight / 2f),
            size = Size(headWidth, headHeight),
          )
        }

        val stem1X = head1CenterX + headWidth * 0.36f
        val stem2X = head2CenterX + headWidth * 0.36f
        val stemTopY = h * 0.16f
        val stemBottomY = head1CenterY
        val stemWidth = 1.5.dp.toPx()

        drawLine(
          color = tint,
          start = Offset(stem1X, stemBottomY),
          end = Offset(stem1X, stemTopY),
          strokeWidth = stemWidth,
          cap = StrokeCap.Square,
        )
        drawLine(
          color = tint,
          start = Offset(stem2X, stemBottomY),
          end = Offset(stem2X, stemTopY),
          strokeWidth = stemWidth,
          cap = StrokeCap.Square,
        )

        val beamThickness = 2.2.dp.toPx()
        val beamCenterY = stemTopY + beamThickness / 2f
        drawLine(
          color = tint,
          start = Offset(stem1X - stemWidth / 2f, beamCenterY),
          end = Offset(stem2X + stemWidth / 2f, beamCenterY),
          strokeWidth = beamThickness,
          cap = StrokeCap.Square,
        )
      }

      BeatSubdivision.TRIPLET -> {
        val textResult = textMeasurer.measure(
          text = "3",
          style = TextStyle(
            fontSize = 7.5.sp,
            fontWeight = FontWeight.Black,
            color = tint,
          ),
        )

        val headWidth = w * 0.28f
        val headHeight = h * 0.20f
        val head1CenterX = w * 0.14f
        val head1CenterY = h * 0.80f
        val head2CenterX = w * 0.48f
        val head2CenterY = h * 0.80f
        val head3CenterX = w * 0.82f
        val head3CenterY = h * 0.80f

        rotate(degrees = -25f, pivot = Offset(head1CenterX, head1CenterY)) {
          drawOval(
            color = tint,
            topLeft = Offset(head1CenterX - headWidth / 2f, head1CenterY - headHeight / 2f),
            size = Size(headWidth, headHeight),
          )
        }
        rotate(degrees = -25f, pivot = Offset(head2CenterX, head2CenterY)) {
          drawOval(
            color = tint,
            topLeft = Offset(head2CenterX - headWidth / 2f, head2CenterY - headHeight / 2f),
            size = Size(headWidth, headHeight),
          )
        }
        rotate(degrees = -25f, pivot = Offset(head3CenterX, head3CenterY)) {
          drawOval(
            color = tint,
            topLeft = Offset(head3CenterX - headWidth / 2f, head3CenterY - headHeight / 2f),
            size = Size(headWidth, headHeight),
          )
        }

        val stem1X = head1CenterX + headWidth * 0.36f
        val stem2X = head2CenterX + headWidth * 0.36f
        val stem3X = head3CenterX + headWidth * 0.36f
        val stemTopY = h * 0.36f
        val stemBottomY = head1CenterY
        val stemWidth = 1.3.dp.toPx()

        drawLine(
          color = tint,
          start = Offset(stem1X, stemBottomY),
          end = Offset(stem1X, stemTopY),
          strokeWidth = stemWidth,
          cap = StrokeCap.Square,
        )
        drawLine(
          color = tint,
          start = Offset(stem2X, stemBottomY),
          end = Offset(stem2X, stemTopY),
          strokeWidth = stemWidth,
          cap = StrokeCap.Square,
        )
        drawLine(
          color = tint,
          start = Offset(stem3X, stemBottomY),
          end = Offset(stem3X, stemTopY),
          strokeWidth = stemWidth,
          cap = StrokeCap.Square,
        )

        val beamThickness = 1.8.dp.toPx()
        val beamCenterY = stemTopY + beamThickness / 2f
        drawLine(
          color = tint,
          start = Offset(stem1X - stemWidth / 2f, beamCenterY),
          end = Offset(stem3X + stemWidth / 2f, beamCenterY),
          strokeWidth = beamThickness,
          cap = StrokeCap.Square,
        )

        val beamCenterX = (stem1X + stem3X) / 2f
        drawText(
          textLayoutResult = textResult,
          topLeft = Offset(
            x = beamCenterX - textResult.size.width / 2f,
            y = (stemTopY - textResult.size.height) / 2f,
          ),
        )
      }

      BeatSubdivision.SIXTEENTH -> {
        val headWidth = w * 0.34f
        val headHeight = h * 0.24f
        val head1CenterX = w * 0.22f
        val head1CenterY = h * 0.77f
        val head2CenterX = w * 0.68f
        val head2CenterY = h * 0.77f

        rotate(degrees = -25f, pivot = Offset(head1CenterX, head1CenterY)) {
          drawOval(
            color = tint,
            topLeft = Offset(head1CenterX - headWidth / 2f, head1CenterY - headHeight / 2f),
            size = Size(headWidth, headHeight),
          )
        }
        rotate(degrees = -25f, pivot = Offset(head2CenterX, head2CenterY)) {
          drawOval(
            color = tint,
            topLeft = Offset(head2CenterX - headWidth / 2f, head2CenterY - headHeight / 2f),
            size = Size(headWidth, headHeight),
          )
        }

        val stem1X = head1CenterX + headWidth * 0.36f
        val stem2X = head2CenterX + headWidth * 0.36f
        val stemTopY = h * 0.14f
        val stemBottomY = head1CenterY
        val stemWidth = 1.5.dp.toPx()

        drawLine(
          color = tint,
          start = Offset(stem1X, stemBottomY),
          end = Offset(stem1X, stemTopY),
          strokeWidth = stemWidth,
          cap = StrokeCap.Square,
        )
        drawLine(
          color = tint,
          start = Offset(stem2X, stemBottomY),
          end = Offset(stem2X, stemTopY),
          strokeWidth = stemWidth,
          cap = StrokeCap.Square,
        )

        val beamThickness = 1.8.dp.toPx()
        val beam1Y = stemTopY + beamThickness / 2f
        drawLine(
          color = tint,
          start = Offset(stem1X - stemWidth / 2f, beam1Y),
          end = Offset(stem2X + stemWidth / 2f, beam1Y),
          strokeWidth = beamThickness,
          cap = StrokeCap.Square,
        )

        val beamGap = 1.5.dp.toPx()
        val beam2Y = beam1Y + beamThickness / 2f + beamGap + beamThickness / 2f
        drawLine(
          color = tint,
          start = Offset(stem1X - stemWidth / 2f, beam2Y),
          end = Offset(stem2X + stemWidth / 2f, beam2Y),
          strokeWidth = beamThickness,
          cap = StrokeCap.Square,
        )
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun MetronomeScreenPreview() {
  ToolboxTheme {
    MetronomeScreen()
  }
}

@Preview(showBackground = true)
@Composable
private fun CustomTimeSignatureDialogPreview() {
  ToolboxTheme {
    androidx.compose.material3.Surface(
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
      modifier = Modifier.padding(16.dp),
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        CustomTimeSignatureDialogContent(
          beatsInput = "7",
          onBeatsInputChange = {},
          selectedNoteValue = 4,
          onSelectNoteValue = {},
          errorMessage = null,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          TextButton(onClick = {}) { Text("Cancel") }
          Button(onClick = {}) { Text("Add") }
        }
      }
    }
  }
}
