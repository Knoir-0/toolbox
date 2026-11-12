package com.example.toolbox.ui.tools

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.toolbox.ui.theme.ToolboxTheme
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

const val MIN_DECIBEL_VALUE = 0f
const val MAX_DECIBEL_VALUE = 120f
const val MIN_CALIBRATION_OFFSET_DB = -20f
const val MAX_CALIBRATION_OFFSET_DB = 20f
private const val HISTORY_MAX_POINTS = 40

/**
 * Environmental noise level classifications for a given decibel (dB SPL) reading.
 */
enum class NoiseLevelCategory(
  val label: String,
  val description: String,
  val rangeText: String,
  val minDb: Float,
  val maxDb: Float,
) {
  QUIET(
    label = "QUIET",
    description = "Whisper • Quiet Library",
    rangeText = "0–40 dB",
    minDb = 0f,
    maxDb = 40f,
  ),
  MODERATE(
    label = "MODERATE",
    description = "Quiet Room • Normal Conversation",
    rangeText = "40–65 dB",
    minDb = 40f,
    maxDb = 65f,
  ),
  LOUD(
    label = "LOUD",
    description = "Busy Traffic • Vacuum Cleaner",
    rangeText = "65–85 dB",
    minDb = 65f,
    maxDb = 85f,
  ),
  VERY_LOUD(
    label = "VERY LOUD",
    description = "Lawn Mower • Power Tools",
    rangeText = "85–100 dB",
    minDb = 85f,
    maxDb = 100f,
  ),
  EXTREME(
    label = "EXTREME",
    description = "Concert • Hearing Risk Threshold",
    rangeText = "100–120 dB",
    minDb = 100f,
    maxDb = 120f,
  ),
}

/**
 * Pure signal-processing and formatting helpers for the Decibel Meter tool.
 */
object DecibelMeterMath {

  /**
   * Computes normalized root-mean-square (RMS) amplitude in `0.0..1.0` from 16-bit PCM samples,
   * removing DC offset first.
   */
  fun calculateNormalizedRms(samples: ShortArray, sampleCount: Int): Double {
    val count = sampleCount.coerceAtMost(samples.size)
    if (count <= 0) return 0.0

    var sum = 0.0
    for (i in 0 until count) {
      sum += samples[i].toDouble()
    }
    val mean = sum / count

    var sumSquares = 0.0
    for (i in 0 until count) {
      val centered = (samples[i].toDouble() - mean) / Short.MAX_VALUE
      sumSquares += centered * centered
    }
    return sqrt(sumSquares / count).coerceIn(0.0, 1.0)
  }

  /**
   * Converts a normalized RMS amplitude (`0.0..1.0`) into an estimated decibel SPL reading
   * (`0f..120f`), applying [calibrationOffsetDb].
   *
   * Full-scale sine/square waves (`rms = 1.0`) map to [fullScaleDbSpl] (default `95.0` dB SPL)
   * before calibration, and very quiet floor signals clamp cleanly to `0f`.
   */
  fun rmsToDecibels(
    normalizedRms: Double,
    calibrationOffsetDb: Float = 0f,
    fullScaleDbSpl: Double = 95.0,
  ): Float {
    if (normalizedRms <= 1e-5) return MIN_DECIBEL_VALUE
    val dbfs = 20.0 * log10(normalizedRms.coerceIn(1e-5, 1.0))
    val estimatedSpl = (fullScaleDbSpl + dbfs + calibrationOffsetDb).toFloat()
    return estimatedSpl.coerceIn(MIN_DECIBEL_VALUE, MAX_DECIBEL_VALUE)
  }

  /**
   * Exponential moving average smoothing for live decibel readings.
   */
  fun smoothDecibels(
    previousDb: Float,
    targetDb: Float,
    alpha: Float = 0.32f,
  ): Float {
    val clampedAlpha = alpha.coerceIn(0.01f, 1f)
    if (previousDb <= 0.01f) return targetDb.coerceIn(MIN_DECIBEL_VALUE, MAX_DECIBEL_VALUE)
    val blended = previousDb * (1f - clampedAlpha) + targetDb * clampedAlpha
    return blended.coerceIn(MIN_DECIBEL_VALUE, MAX_DECIBEL_VALUE)
  }

  /**
   * Classifies a decibel reading into a [NoiseLevelCategory].
   */
  fun classifyNoiseLevel(db: Float): NoiseLevelCategory {
    val clamped = db.coerceIn(MIN_DECIBEL_VALUE, MAX_DECIBEL_VALUE)
    return when {
      clamped < 40f -> NoiseLevelCategory.QUIET
      clamped < 65f -> NoiseLevelCategory.MODERATE
      clamped < 85f -> NoiseLevelCategory.LOUD
      clamped < 100f -> NoiseLevelCategory.VERY_LOUD
      else -> NoiseLevelCategory.EXTREME
    }
  }

  /**
   * Formats a decibel value to one decimal place (e.g., `"42.5 dB"`).
   */
  fun formatDb(db: Float, includeUnit: Boolean = true): String {
    val clamped = db.coerceIn(MIN_DECIBEL_VALUE, MAX_DECIBEL_VALUE)
    val number = String.format(Locale.US, "%.1f", clamped)
    return if (includeUnit) "$number dB" else number
  }

  /**
   * Formats a signed calibration offset (e.g., `"+2.5 dB"` or `"-1.0 dB"`).
   */
  fun formatCalibrationOffset(offsetDb: Float): String {
    val rounded = (offsetDb * 2f).roundToInt() / 2f
    val sign = if (rounded >= 0f) "+" else ""
    return String.format(Locale.US, "%s%.1f dB", sign, rounded)
  }
}

@SuppressLint("MissingPermission")
@Composable
fun DecibelMeterScreen(
  modifier: Modifier = Modifier,
  initialDecibels: Float = 0f,
) {
  val context = LocalContext.current
  val isInspectionMode = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current

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

  var isPaused by rememberSaveable { mutableStateOf(false) }
  var calibrationOffsetDb by rememberSaveable { mutableFloatStateOf(0f) }

  var currentDb by rememberSaveable {
    mutableFloatStateOf(if (isInspectionMode && initialDecibels == 0f) 46.5f else initialDecibels)
  }
  var minDb by rememberSaveable {
    mutableFloatStateOf(if (isInspectionMode && initialDecibels == 0f) 34.2f else initialDecibels)
  }
  var maxDb by rememberSaveable {
    mutableFloatStateOf(if (isInspectionMode && initialDecibels == 0f) 68.4f else initialDecibels)
  }
  var sumDb by rememberSaveable {
    mutableFloatStateOf(if (isInspectionMode && initialDecibels == 0f) 46.5f else initialDecibels)
  }
  var sampleCount by rememberSaveable {
    mutableIntStateOf(if (isInspectionMode || initialDecibels > 0f) 1 else 0)
  }

  val history = remember {
    mutableStateListOf<Float>().apply {
      if (isInspectionMode) {
        addAll(listOf(36f, 38f, 42f, 45f, 51f, 48f, 44f, 46.5f))
      } else if (initialDecibels > 0f) {
        add(initialDecibels)
      }
    }
  }

  fun resetStats() {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    currentDb = 0f
    minDb = 0f
    maxDb = 0f
    sumDb = 0f
    sampleCount = 0
    history.clear()
  }

  // Live microphone audio capture and dB SPL calculation loop
  LaunchedEffect(hasMicPermission, isPaused, calibrationOffsetDb, isInspectionMode) {
    if (!hasMicPermission || isPaused || isInspectionMode) {
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
        var smoothed = currentDb

        while (isActive) {
          val read = audioRecord.read(buffer, 0, buffer.size)
          if (read > 0) {
            val rms = DecibelMeterMath.calculateNormalizedRms(buffer, read)
            val rawDb = DecibelMeterMath.rmsToDecibels(
              normalizedRms = rms,
              calibrationOffsetDb = calibrationOffsetDb,
            )
            smoothed = DecibelMeterMath.smoothDecibels(smoothed, rawDb)
            withContext(Dispatchers.Main) {
              currentDb = smoothed
              if (sampleCount == 0) {
                minDb = smoothed
                maxDb = smoothed
                sumDb = smoothed
                sampleCount = 1
              } else {
                if (smoothed < minDb) minDb = smoothed
                if (smoothed > maxDb) maxDb = smoothed
                sumDb += smoothed
                sampleCount++
              }
              if (history.size >= HISTORY_MAX_POINTS) {
                history.removeAt(0)
              }
              history.add(smoothed)
            }
            delay(80L)
          } else {
            delay(40L)
          }
        }
      } catch (_: Throwable) {
        // Ignore audio hardware errors in test / headless environments
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

  val avgDb = if (sampleCount > 0) (sumDb / sampleCount).coerceIn(MIN_DECIBEL_VALUE, MAX_DECIBEL_VALUE) else 0f
  val category = DecibelMeterMath.classifyNoiseLevel(currentDb)

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("decibel_meter_screen"),
  ) {
    // 1. Standard Tool Header / Top Bar (DESIGN_LANGUAGE.md 5.1)
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
            .padding(horizontal = 12.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Decibel Meter",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = when {
                  !hasMicPermission -> "Microphone permission required"
                  isPaused -> "Paused • ${DecibelMeterMath.formatCalibrationOffset(calibrationOffsetDb)} offset"
                  else -> "Listening • ${DecibelMeterMath.formatCalibrationOffset(calibrationOffsetDb)} offset"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            IconButton(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isPaused = !isPaused
              },
              modifier = Modifier.testTag("decibel_pause_resume_button"),
            ) {
              Icon(
                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (isPaused) "Resume meter" else "Pause meter",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            IconButton(
              onClick = { resetStats() },
              modifier = Modifier.testTag("decibel_reset_button"),
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset statistics",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp),
    ) {
      // Optional Microphone Permission Banner
      if (!hasMicPermission) {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("mic_permission_banner"),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
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
                  text = "Microphone access is required to measure ambient sound levels.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
              }
              Button(
                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .padding(start = 8.dp)
                  .testTag("allow_mic_permission_button"),
              ) {
                Text("Allow")
              }
            }
          }
        }
      }

      // 2. Hero Semicircular Decibel Gauge Card
      item {
        val statusColor = when (category) {
          NoiseLevelCategory.QUIET, NoiseLevelCategory.MODERATE -> MaterialTheme.colorScheme.primary
          NoiseLevelCategory.LOUD -> MaterialTheme.colorScheme.tertiary
          NoiseLevelCategory.VERY_LOUD, NoiseLevelCategory.EXTREME -> MaterialTheme.colorScheme.error
        }
        val statusContentColor = when (category) {
          NoiseLevelCategory.QUIET, NoiseLevelCategory.MODERATE -> MaterialTheme.colorScheme.onPrimary
          NoiseLevelCategory.LOUD -> MaterialTheme.colorScheme.onTertiary
          NoiseLevelCategory.VERY_LOUD, NoiseLevelCategory.EXTREME -> MaterialTheme.colorScheme.onError
        }

        val animatedDb by animateFloatAsState(
          targetValue = currentDb,
          animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
          ),
          label = "DecibelGaugeSpring",
        )

        Card(
          shape = RoundedCornerShape(28.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
          ),
          border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("decibel_gauge_card"),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
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
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp),
                  )
                  Text(
                    text = if (isPaused) "PAUSED" else "LIVE dB SPL",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                }
              }

              Badge(
                containerColor = statusColor,
                contentColor = statusContentColor,
                modifier = Modifier.testTag("decibel_category_badge"),
              ) {
                Text(
                  text = category.label,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.ExtraBold,
                )
              }
            }

            DecibelGaugeCanvas(
              decibels = animatedDb,
              accentColor = statusColor,
              modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
                .testTag("decibel_gauge_canvas"),
            )

            Row(
              verticalAlignment = Alignment.Bottom,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Text(
                text = DecibelMeterMath.formatDb(currentDb, includeUnit = false),
                style = MaterialTheme.typography.displayMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("decibel_current_value"),
              )
              Text(
                text = "dB",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
              )
            }

            Text(
              text = category.description,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
              modifier = Modifier.testTag("decibel_category_description"),
            )
          }
        }
      }

      // 3. Min / Avg / Max Statistics Card
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          DecibelStatCard(
            label = "MIN",
            valueText = DecibelMeterMath.formatDb(minDb),
            testTag = "decibel_min_value",
            modifier = Modifier.weight(1f),
          )
          DecibelStatCard(
            label = "AVG",
            valueText = DecibelMeterMath.formatDb(avgDb),
            testTag = "decibel_avg_value",
            modifier = Modifier.weight(1f),
          )
          DecibelStatCard(
            label = "MAX",
            valueText = DecibelMeterMath.formatDb(maxDb),
            testTag = "decibel_max_value",
            modifier = Modifier.weight(1f),
          )
        }
      }

      // 4. Recent Level History Bar Graph Card
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Level History",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "0 – 120 dB",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            DecibelHistoryCanvas(
              history = history,
              modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .testTag("decibel_history_canvas"),
            )
          }
        }
      }

      // 5. Calibration Offset Card with SlimDotSlider (DESIGN_LANGUAGE.md 5.6)
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Tune,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp),
                )
                Text(
                  text = "Calibration Offset",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = DecibelMeterMath.formatCalibrationOffset(calibrationOffsetDb),
                  style = MaterialTheme.typography.bodyMedium,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.testTag("decibel_calibration_value"),
                )
                if (calibrationOffsetDb != 0f) {
                  Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                      .clip(RoundedCornerShape(20.dp))
                      .clickable {
                        calibrationOffsetDb = 0f
                      }
                      .testTag("decibel_calibration_reset"),
                  ) {
                    Text(
                      text = "Reset",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Medium,
                      color = MaterialTheme.colorScheme.onPrimaryContainer,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                  }
                }
              }
            }

            DecibelSlimDotSlider(
              value = calibrationOffsetDb,
              onValueChange = { raw ->
                calibrationOffsetDb = ((raw * 2f).roundToInt() / 2f)
                  .coerceIn(MIN_CALIBRATION_OFFSET_DB, MAX_CALIBRATION_OFFSET_DB)
              },
              valueRange = MIN_CALIBRATION_OFFSET_DB..MAX_CALIBRATION_OFFSET_DB,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("decibel_calibration_slider"),
            )
          }
        }
      }

      // 6. Common Sound Reference Levels Card
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = "Reference Levels",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )

            NoiseLevelCategory.entries.forEach { level ->
              val isCurrentLevel = level == category
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isCurrentLevel) {
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                } else {
                  Color.Transparent
                },
                modifier = Modifier.fillMaxWidth(),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = level.label,
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = if (isCurrentLevel) FontWeight.Bold else FontWeight.SemiBold,
                      color = if (isCurrentLevel) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                      } else {
                        MaterialTheme.colorScheme.onSurface
                      },
                    )
                    Text(
                      text = level.description,
                      style = MaterialTheme.typography.bodySmall,
                      color = if (isCurrentLevel) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                      } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                      },
                    )
                  }
                  Text(
                    text = level.rangeText,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (isCurrentLevel) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrentLevel) {
                      MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                      MaterialTheme.colorScheme.onSurfaceVariant
                    },
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
private fun DecibelStatCard(
  label: String,
  valueText: String,
  testTag: String,
  modifier: Modifier = Modifier,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 10.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = valueText,
        style = MaterialTheme.typography.bodyMedium,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        modifier = Modifier.testTag(testTag),
      )
    }
  }
}

@Composable
private fun DecibelGaugeCanvas(
  decibels: Float,
  accentColor: Color,
  modifier: Modifier = Modifier,
) {
  val trackColor = MaterialTheme.colorScheme.surfaceVariant
  val tickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
  val needleColor = MaterialTheme.colorScheme.onSurface

  Canvas(modifier = modifier) {
    val strokeWidth = 14.dp.toPx()
    val diameter = minOf(size.width - strokeWidth * 2f, (size.height - strokeWidth) * 1.85f)
      .coerceAtLeast(10f)
    val arcLeft = (size.width - diameter) / 2f
    val arcTop = size.height - diameter / 2f - 10.dp.toPx()
    val arcSize = Size(diameter, diameter)
    val center = Offset(size.width / 2f, arcTop + diameter / 2f)
    val radius = diameter / 2f

    // Background arc (180 degrees from left to right)
    drawArc(
      color = trackColor,
      startAngle = 180f,
      sweepAngle = 180f,
      useCenter = false,
      topLeft = Offset(arcLeft, arcTop),
      size = arcSize,
      style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
    )

    val fraction = ((decibels - MIN_DECIBEL_VALUE) / (MAX_DECIBEL_VALUE - MIN_DECIBEL_VALUE))
      .coerceIn(0f, 1f)

    // Active progress arc
    if (fraction > 0.005f) {
      drawArc(
        color = accentColor,
        startAngle = 180f,
        sweepAngle = 180f * fraction,
        useCenter = false,
        topLeft = Offset(arcLeft, arcTop),
        size = arcSize,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
      )
    }

    // Scale tick marks every 20 dB (0, 20, 40, 60, 80, 100, 120)
    val tickCount = 6
    for (i in 0..tickCount) {
      val tickFraction = i.toFloat() / tickCount
      val angleRad = PI * (1.0 - tickFraction)
      val innerR = radius - strokeWidth * 0.95f
      val outerR = radius - strokeWidth * 0.55f
      val start = Offset(
        x = center.x + (innerR * cos(angleRad)).toFloat(),
        y = center.y - (innerR * sin(angleRad)).toFloat(),
      )
      val end = Offset(
        x = center.x + (outerR * cos(angleRad)).toFloat(),
        y = center.y - (outerR * sin(angleRad)).toFloat(),
      )
      drawLine(
        color = tickColor,
        start = start,
        end = end,
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round,
      )
    }

    // Needle pointer
    val needleAngleRad = PI * (1.0 - fraction)
    val needleLength = radius - strokeWidth * 0.7f
    val needleTip = Offset(
      x = center.x + (needleLength * cos(needleAngleRad)).toFloat(),
      y = center.y - (needleLength * sin(needleAngleRad)).toFloat(),
    )
    drawLine(
      color = needleColor,
      start = center,
      end = needleTip,
      strokeWidth = 3.5.dp.toPx(),
      cap = StrokeCap.Round,
    )
    drawCircle(
      color = accentColor,
      radius = 7.dp.toPx(),
      center = center,
    )
    drawCircle(
      color = needleColor,
      radius = 3.5.dp.toPx(),
      center = center,
    )
  }
}

@Composable
private fun DecibelHistoryCanvas(
  history: List<Float>,
  modifier: Modifier = Modifier,
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val tertiaryColor = MaterialTheme.colorScheme.tertiary
  val errorColor = MaterialTheme.colorScheme.error
  val emptyBarColor = MaterialTheme.colorScheme.surfaceVariant

  Canvas(modifier = modifier) {
    val barCount = HISTORY_MAX_POINTS
    val spacing = 3.dp.toPx()
    val totalSpacing = spacing * (barCount - 1)
    val barWidth = ((size.width - totalSpacing) / barCount).coerceAtLeast(2f)

    for (i in 0 until barCount) {
      val x = i * (barWidth + spacing)
      val historyIndex = i - (barCount - history.size)
      if (historyIndex in history.indices) {
        val db = history[historyIndex].coerceIn(MIN_DECIBEL_VALUE, MAX_DECIBEL_VALUE)
        val fraction = (db / MAX_DECIBEL_VALUE).coerceIn(0.06f, 1f)
        val barHeight = size.height * fraction
        val barColor = when {
          db < 65f -> primaryColor
          db < 85f -> tertiaryColor
          else -> errorColor
        }
        drawRoundRect(
          color = barColor,
          topLeft = Offset(x, size.height - barHeight),
          size = Size(barWidth, barHeight),
          cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
        )
      } else {
        val minBarHeight = size.height * 0.08f
        drawRoundRect(
          color = emptyBarColor,
          topLeft = Offset(x, size.height - minBarHeight),
          size = Size(barWidth, minBarHeight),
          cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DecibelSlimDotSlider(
  value: Float,
  onValueChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
  valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
  accentColor: Color = MaterialTheme.colorScheme.primary,
) {
  val sliderColors = SliderDefaults.colors(
    thumbColor = accentColor,
    activeTrackColor = accentColor,
    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
  )
  Slider(
    value = value,
    onValueChange = onValueChange,
    modifier = modifier,
    valueRange = valueRange,
    colors = sliderColors,
    thumb = {
      Box(
        modifier = Modifier
          .size(16.dp)
          .shadow(2.dp, CircleShape)
          .background(accentColor, CircleShape),
      )
    },
    track = { sliderState ->
      SliderDefaults.Track(
        sliderState = sliderState,
        modifier = Modifier.height(4.dp),
        colors = sliderColors,
      )
    },
  )
}

@Preview(showBackground = true)
@Composable
private fun DecibelMeterScreenPreview() {
  ToolboxTheme {
    DecibelMeterScreen()
  }
}
