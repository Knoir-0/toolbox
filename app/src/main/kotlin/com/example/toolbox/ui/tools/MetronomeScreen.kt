package com.example.toolbox.ui.tools

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TouchApp
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
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.toolbox.ui.theme.ToolboxTheme
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

enum class TimeSignature(val label: String, val beatsPerMeasure: Int, val noteValue: Int) {
  FOUR_FOUR("4/4", 4, 4),
  THREE_FOUR("3/4", 3, 4),
  TWO_FOUR("2/4", 2, 4),
  SIX_EIGHT("6/8", 6, 8),
  FIVE_FOUR("5/4", 5, 4),
  SEVEN_EIGHT("7/8", 7, 8),
}

private val CompactPillHeight = 28.dp

enum class BeatSubdivision(val label: String, val ticksPerBeat: Int) {
  QUARTER("♩ Quarter (1x)", 1),
  EIGHTH("♫ Eighth (2x)", 2),
  TRIPLET("3 Triplet (3x)", 3),
  SIXTEENTH("♬ Sixteenth (4x)", 4),
}

/**
 * Lightweight synthesizer for metronome clicks.
 * Safely catches audio initialization exceptions in headless test environments.
 */
private object MetronomeClickSynthesizer {
  suspend fun playMetronomeClick(frequencyHz: Double) = withContext(Dispatchers.Default) {
    try {
      val sampleRate = 22050
      val durationMs = 35
      val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
      val samples = ShortArray(numSamples)
      for (i in 0 until numSamples) {
        val t = i.toDouble() / sampleRate
        val envelope = exp(-95.0 * t)
        val wave = sin(2.0 * PI * frequencyHz * t)
        samples[i] = (wave * envelope * Short.MAX_VALUE * 0.8).toInt()
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

@Composable
fun MetronomeScreen(modifier: Modifier = Modifier) {
  var bpm by rememberSaveable { mutableIntStateOf(120) }
  var isMetronomePlaying by rememberSaveable { mutableStateOf(false) }
  var timeSignature by rememberSaveable { mutableStateOf(TimeSignature.FOUR_FOUR) }
  var subdivision by rememberSaveable { mutableStateOf(BeatSubdivision.QUARTER) }
  var accentFirstBeat by rememberSaveable { mutableStateOf(true) }
  var currentBeat by remember { mutableIntStateOf(0) }
  var currentSubTick by remember { mutableIntStateOf(0) }
  var beatPulseCounter by remember { mutableLongStateOf(0L) }
  val tapTimestamps = remember { mutableStateListOf<Long>() }

  LaunchedEffect(isMetronomePlaying, bpm, timeSignature, subdivision, accentFirstBeat) {
    if (!isMetronomePlaying) {
      currentBeat = 0
      currentSubTick = 0
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
      onSelectTimeSignature = {
        timeSignature = it
        currentBeat = 0
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
  onSelectTimeSignature: (TimeSignature) -> Unit,
  subdivision: BeatSubdivision,
  onSelectSubdivision: (BeatSubdivision) -> Unit,
  accentFirstBeat: Boolean,
  onToggleAccent: () -> Unit,
  currentBeat: Int,
  beatPulseCounter: Long,
  onTapTempo: () -> Unit,
) {
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
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            for (beatIndex in 0 until timeSignature.beatsPerMeasure) {
              val isCurrent = isPlaying && currentBeat == beatIndex
              val isDownbeat = beatIndex == 0 && accentFirstBeat
              val circleSize = if (isCurrent) 38.dp else 30.dp
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
                  style = MaterialTheme.typography.labelLarge,
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

          Slider(
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
              shape = RoundedCornerShape(16.dp),
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
              shape = RoundedCornerShape(16.dp),
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
              TimeSignature.entries.forEach { sig ->
                val selected = sig == timeSignature
                FilterChip(
                  selected = selected,
                  onClick = { onSelectTimeSignature(sig) },
                  label = {
                    Text(
                      text = sig.label,
                      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                  },
                  modifier = Modifier.height(CompactPillHeight),
                  shape = RoundedCornerShape(50),
                )
              }
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

@Preview(showBackground = true)
@Composable
private fun MetronomeScreenPreview() {
  ToolboxTheme {
    MetronomeScreen()
  }
}
