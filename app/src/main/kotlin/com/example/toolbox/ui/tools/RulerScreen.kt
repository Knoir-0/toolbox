package com.example.toolbox.ui.tools

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.toolbox.ui.theme.ToolboxTheme
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class RulerMode(val label: String) {
  SINGLE("From Zero"),
  CALIPER("Caliper (A↔B)"),
}

enum class RulerUnitSystem(val label: String, val shortLabel: String) {
  METRIC("Metric (cm / mm)", "mm"),
  IMPERIAL("Imperial (in)", "in"),
}

enum class RulerAxis(val label: String) {
  VERTICAL("Left / Right Edge"),
  HORIZONTAL("Top / Bottom Edge"),
}

data class ReferenceObjectSpec(
  val name: String,
  val lengthMm: Float,
)

/**
 * Snapshot of the device's physical display metrics used to render true-to-scale
 * graduations along the horizontal (`xdpi`) and vertical (`ydpi`) edges of the screen.
 */
data class DeviceScreenMeasurements(
  val xdpi: Float,
  val ydpi: Float,
  val densityDpi: Int,
  val widthPx: Int,
  val heightPx: Int,
  val calibrationScale: Float = 1.0f,
) {
  val pxPerInchX: Float
    get() = (xdpi * calibrationScale).coerceAtLeast(50f)

  val pxPerInchY: Float
    get() = (ydpi * calibrationScale).coerceAtLeast(50f)

  val pxPerMmX: Float
    get() = (pxPerInchX / RulerMath.MM_PER_INCH.toFloat()).coerceAtLeast(2f)

  val pxPerMmY: Float
    get() = (pxPerInchY / RulerMath.MM_PER_INCH.toFloat()).coerceAtLeast(2f)

  val screenWidthMm: Float
    get() = widthPx / pxPerMmX

  val screenHeightMm: Float
    get() = heightPx / pxPerMmY

  val screenDiagonalInches: Double
    get() = hypot(
      (widthPx / pxPerInchX).toDouble(),
      (heightPx / pxPerInchY).toDouble(),
    )
}

object RulerMath {
  const val MM_PER_INCH = 25.4
  private const val MIN_VALID_HARDWARE_DPI = 72f
  private const val MAX_VALID_HARDWARE_DPI = 1000f

  val referenceObjects = listOf(
    ReferenceObjectSpec("US Dime", 17.91f),
    ReferenceObjectSpec("US Quarter", 24.26f),
    ReferenceObjectSpec("SD Card", 32.0f),
    ReferenceObjectSpec("AA Battery", 50.5f),
    ReferenceObjectSpec("Credit Card Width", 53.98f),
    ReferenceObjectSpec("Sticky Note", 76.2f),
    ReferenceObjectSpec("Credit Card Length", 85.60f),
  )

  /**
   * Resolves physical hardware DPI for an axis (`xdpi` or `ydpi`), falling back to
   * [fallbackDensityDpi] when the reported hardware value is missing or out of bounds.
   */
  fun resolveHardwareDpi(reportedAxisDpi: Float, fallbackDensityDpi: Int): Float {
    return if (reportedAxisDpi in MIN_VALID_HARDWARE_DPI..MAX_VALID_HARDWARE_DPI) {
      reportedAxisDpi
    } else {
      fallbackDensityDpi.toFloat().coerceIn(120f, 640f)
    }
  }

  /**
   * Converts DPI (dots/pixels per inch) and optional [calibrationScale] into physical pixels per millimeter.
   */
  fun dpiToPxPerMm(dpi: Float, calibrationScale: Float = 1.0f): Float {
    return ((dpi * calibrationScale) / MM_PER_INCH.toFloat()).coerceAtLeast(2f)
  }

  fun mmToPx(mm: Float, pxPerMm: Float): Float = mm * pxPerMm

  fun pxToMm(px: Float, pxPerMm: Float): Float = px / pxPerMm.coerceAtLeast(1e-3f)

  fun mmToInches(mm: Double): Double = mm / MM_PER_INCH

  fun inchesToMm(inches: Double): Double = inches * MM_PER_INCH

  fun mmToCm(mm: Double): Double = mm / 10.0

  fun caliperSpanMm(startMm: Float, endMm: Float): Float = abs(endMm - startMm)

  fun gcd(a: Int, b: Int): Int {
    var x = abs(a)
    var y = abs(b)
    while (y != 0) {
      val t = y
      y = x % y
      x = t
    }
    return if (x == 0) 1 else x
  }

  /**
   * Formats a length in inches to the nearest fraction (default 1/16"), e.g. `2 3/8"`.
   */
  fun formatImperialFraction(inches: Double, denominator: Int = 16): String {
    val safeInches = max(0.0, inches)
    val totalParts = (safeInches * denominator).roundToInt()
    val whole = totalParts / denominator
    val rem = totalParts % denominator
    if (rem == 0) {
      return "$whole\""
    }
    val divisor = gcd(rem, denominator)
    val num = rem / divisor
    val den = denominator / divisor
    return if (whole > 0) {
      "$whole $num/$den\""
    } else {
      "$num/$den\""
    }
  }

  fun closestReferenceObject(mm: Float): ReferenceObjectSpec {
    return referenceObjects.minByOrNull { abs(it.lengthMm - mm) } ?: referenceObjects.first()
  }

  /**
   * Resolves whether touch/drag interaction targets Caliper Bar A (0) or Bar B (1).
   *
   * When both bars are separated along the active measurement axis, the closer bar is chosen.
   * When bars are overlapping or touch is equidistant, directional touch disambiguates pulling
   * the jaws apart: touch on or before the minimum selects the start jaw, and touch after selects
   * the end jaw.
   */
  fun resolveCaliperTarget(
    touchCoordMm: Float,
    startMm: Float,
    endMm: Float,
  ): Int {
    val distStart = abs(touchCoordMm - startMm)
    val distEnd = abs(touchCoordMm - endMm)

    if (distStart < distEnd) {
      return 0
    } else if (distEnd < distStart) {
      return 1
    }

    // Directional fallback when equidistant or overlapping:
    // Touch on or before the minimum pulls the start jaw; touch after pulls the end jaw.
    val minMm = min(startMm, endMm)
    return if (touchCoordMm <= minMm) {
      if (startMm <= endMm) 0 else 1
    } else {
      if (startMm <= endMm) 1 else 0
    }
  }
}

@Composable
fun RulerScreen(modifier: Modifier = Modifier) {
  var rulerMode by rememberSaveable { mutableStateOf(RulerMode.SINGLE) }
  var primaryUnit by rememberSaveable { mutableStateOf(RulerUnitSystem.METRIC) }
  var activeAxis by rememberSaveable { mutableStateOf(RulerAxis.VERTICAL) }
  var isHudMinimized by rememberSaveable { mutableStateOf(false) }

  var cursorStartMm by rememberSaveable { mutableFloatStateOf(15.0f) }
  var cursorEndMm by rememberSaveable { mutableFloatStateOf(53.98f) }
  var calibrationScale by rememberSaveable { mutableFloatStateOf(1.0f) }
  var showCalibrationPanel by rememberSaveable { mutableStateOf(false) }

  val context = LocalContext.current
  val density = LocalDensity.current

  // Extract exact physical display metrics (xdpi, ydpi, widthPixels, heightPixels) from the device
  val deviceMeasurements = remember(context, density, calibrationScale) {
    val metrics = context.resources.displayMetrics
    val resolvedXdpi = RulerMath.resolveHardwareDpi(metrics.xdpi, metrics.densityDpi)
    val resolvedYdpi = RulerMath.resolveHardwareDpi(metrics.ydpi, metrics.densityDpi)
    DeviceScreenMeasurements(
      xdpi = resolvedXdpi,
      ydpi = resolvedYdpi,
      densityDpi = metrics.densityDpi,
      widthPx = metrics.widthPixels,
      heightPx = metrics.heightPixels,
      calibrationScale = calibrationScale,
    )
  }

  val measuredMm = remember(rulerMode, cursorStartMm, cursorEndMm) {
    when (rulerMode) {
      RulerMode.SINGLE -> cursorEndMm.coerceAtLeast(0f)
      RulerMode.CALIPER -> RulerMath.caliperSpanMm(cursorStartMm, cursorEndMm)
    }
  }

  val measuredCm = remember(measuredMm) { RulerMath.mmToCm(measuredMm.toDouble()) }
  val measuredInches = remember(measuredMm) { RulerMath.mmToInches(measuredMm.toDouble()) }
  val fractionInches = remember(measuredInches) { RulerMath.formatImperialFraction(measuredInches) }

  // Full-bleed Box so scales render directly flush against all 4 edges of the screen
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.surfaceContainerLowest),
  ) {
    // 1. Full-Bleed Screen-Edge Scale Canvas (Left, Right, Top, and Bottom edges)
    ScreenEdgeRulerCanvas(
      rulerMode = rulerMode,
      primaryUnit = primaryUnit,
      activeAxis = activeAxis,
      deviceMeasurements = deviceMeasurements,
      cursorStartMm = cursorStartMm,
      cursorEndMm = cursorEndMm,
      measuredMm = measuredMm,
      onUpdateStartMm = { cursorStartMm = it },
      onUpdateEndMm = { cursorEndMm = it },
      onSelectAxis = { activeAxis = it },
      modifier = Modifier
        .fillMaxSize()
        .testTag("screen_edge_ruler_canvas")
        .semantics {
          contentDescription = "Screen edge ruler scale canvas"
        },
    )

    // 2. Center Floating HUD (inset away from all 4 screen edges so edge scales stay unobstructed)
    if (isHudMinimized) {
      MinimizedRulerHudPill(
        primaryUnit = primaryUnit,
        measuredMm = measuredMm,
        measuredCm = measuredCm,
        measuredInches = measuredInches,
        fractionInches = fractionInches,
        onExpandHud = { isHudMinimized = false },
        modifier = Modifier
          .align(Alignment.Center)
          .padding(horizontal = 64.dp),
      )
    } else {
      Column(
        modifier = Modifier
          .align(Alignment.Center)
          .fillMaxWidth()
          .padding(
            start = 64.dp,
            end = 64.dp,
            top = 36.dp,
            bottom = 36.dp,
          ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        RulerHeroCard(
          rulerMode = rulerMode,
          onSelectMode = { mode ->
            rulerMode = mode
            if (mode == RulerMode.CALIPER && abs(cursorEndMm - cursorStartMm) < 2f) {
              cursorStartMm = 10f
              cursorEndMm = 60f
            }
          },
          primaryUnit = primaryUnit,
          onToggleUnit = {
            primaryUnit = if (primaryUnit == RulerUnitSystem.METRIC) {
              RulerUnitSystem.IMPERIAL
            } else {
              RulerUnitSystem.METRIC
            }
          },
          onMinimizeHud = { isHudMinimized = true },
          measuredMm = measuredMm,
          measuredCm = measuredCm,
          measuredInches = measuredInches,
          fractionInches = fractionInches,
          showCalibration = showCalibrationPanel,
          onToggleCalibration = { showCalibrationPanel = !showCalibrationPanel },
        )

        AnimatedVisibility(visible = showCalibrationPanel) {
          CalibrationPanelCard(
            calibrationScale = calibrationScale,
            onCalibrationChange = { calibrationScale = it.coerceIn(0.80f, 1.20f) },
            onResetCalibration = { calibrationScale = 1.0f },
          )
        }
      }
    }
  }
}

@Composable
private fun MinimizedRulerHudPill(
  primaryUnit: RulerUnitSystem,
  measuredMm: Float,
  measuredCm: Double,
  measuredInches: Double,
  fractionInches: String,
  onExpandHud: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    onClick = onExpandHud,
    modifier = modifier.testTag("minimized_ruler_hud"),
    shape = RoundedCornerShape(50),
    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
    shadowElevation = 6.dp,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector = Icons.Default.SquareFoot,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(18.dp),
      )
      Text(
        text = if (primaryUnit == RulerUnitSystem.METRIC) {
          String.format("%.2f cm (%.1f mm) · %s", measuredCm, measuredMm, fractionInches)
        } else {
          String.format("%.2f in (%s) · %.1f mm", measuredInches, fractionInches, measuredMm)
        },
        style = MaterialTheme.typography.titleSmall,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
      )
      Icon(
        imageVector = Icons.Default.FullscreenExit,
        contentDescription = "Expand Ruler Controls",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(18.dp),
      )
    }
  }
}

@Composable
private fun RulerHeroCard(
  rulerMode: RulerMode,
  onSelectMode: (RulerMode) -> Unit,
  primaryUnit: RulerUnitSystem,
  onToggleUnit: () -> Unit,
  onMinimizeHud: () -> Unit,
  measuredMm: Float,
  measuredCm: Double,
  measuredInches: Double,
  fractionInches: String,
  showCalibration: Boolean,
  onToggleCalibration: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.radialGradient(
            colors = listOf(
              MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
              Color.Transparent,
            ),
          ),
        ),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        // Top Header: Metric / Imperial Toggle + Calibrate + Minimize HUD
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
            onClick = onToggleUnit,
            modifier = Modifier.testTag("ruler_unit_toggle"),
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
          ) {
            Row(
              modifier = Modifier.padding(2.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
              RulerUnitSystem.entries.forEach { unit ->
                val isSelected = primaryUnit == unit
                Surface(
                  shape = RoundedCornerShape(50),
                  color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    Color.Transparent
                  },
                ) {
                  Text(
                    text = if (unit == RulerUnitSystem.METRIC) "Metric (cm)" else "Imperial (in)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                      MaterialTheme.colorScheme.onPrimary
                    } else {
                      MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  )
                }
              }
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            IconButton(
              onClick = onToggleCalibration,
              modifier = Modifier
                .size(28.dp)
                .testTag("calibrate_ruler_button"),
            ) {
              Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Calibrate Ruler",
                tint = if (showCalibration) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(18.dp),
              )
            }

            IconButton(
              onClick = onMinimizeHud,
              modifier = Modifier
                .size(28.dp)
                .testTag("minimize_ruler_hud_button"),
            ) {
              Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = "Minimize controls to view full screen ruler",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
              )
            }
          }
        }

        // Main Digital Readout
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          val headlineText = if (primaryUnit == RulerUnitSystem.METRIC) {
            String.format("%.2f cm", measuredCm)
          } else {
            String.format("%.2f in", measuredInches)
          }
          val subText = if (primaryUnit == RulerUnitSystem.METRIC) {
            String.format("%.1f mm  ·  %.2f in (%s)", measuredMm, measuredInches, fractionInches)
          } else {
            String.format("%s  ·  %.1f mm (%.2f cm)", fractionInches, measuredMm, measuredCm)
          }

          AnimatedContent(
            targetState = headlineText,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            contentAlignment = Alignment.Center,
            label = "RulerPrimaryReadout",
          ) { valueStr ->
            Text(
              text = valueStr,
              style = MaterialTheme.typography.headlineMedium,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Black,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
          Text(
            text = subText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        // Mode Segmented Row
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
          RulerMode.entries.forEachIndexed { index, mode ->
            SegmentedButton(
              selected = rulerMode == mode,
              onClick = { onSelectMode(mode) },
              shape = SegmentedButtonDefaults.itemShape(
                index = index,
                count = RulerMode.entries.size,
              ),
            ) {
              Text(
                text = mode.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CalibrationPanelCard(
  calibrationScale: Float,
  onCalibrationChange: (Float) -> Unit,
  onResetCalibration: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.95f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Ruler Scale Calibration (${(calibrationScale * 100).roundToInt()}%)",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = "Reset 100%",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.tertiary,
          modifier = Modifier.clickable(onClick = onResetCalibration),
        )
      }

      SlimDotSlider(
        value = calibrationScale,
        onValueChange = onCalibrationChange,
        valueRange = 0.80f..1.20f,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

/**
 * Full-bleed canvas that renders accurate physical scales flush against all four edges of the screen:
 * - **Left & Right Edges** use the device's vertical physical DPI (`ydpi`)
 * - **Top & Bottom Edges** use the device's horizontal physical DPI (`xdpi`)
 * - All four edges display in the currently selected [primaryUnit] (Metric `cm`/`mm` or Imperial `in`/`1/16"`).
 */
@Composable
private fun ScreenEdgeRulerCanvas(
  rulerMode: RulerMode,
  primaryUnit: RulerUnitSystem,
  activeAxis: RulerAxis,
  deviceMeasurements: DeviceScreenMeasurements,
  cursorStartMm: Float,
  cursorEndMm: Float,
  measuredMm: Float,
  onUpdateStartMm: (Float) -> Unit,
  onUpdateEndMm: (Float) -> Unit,
  onSelectAxis: (RulerAxis) -> Unit,
  modifier: Modifier = Modifier,
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val tertiaryColor = MaterialTheme.colorScheme.tertiary
  val onSurfaceColor = MaterialTheme.colorScheme.onSurface
  val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
  val surfaceHighColor = MaterialTheme.colorScheme.surfaceContainerHigh
  val outlineColor = MaterialTheme.colorScheme.outlineVariant

  val pxPerMmX = deviceMeasurements.pxPerMmX
  val pxPerMmY = deviceMeasurements.pxPerMmY
  val activePxPerMm = if (activeAxis == RulerAxis.VERTICAL) pxPerMmY else pxPerMmX

  var activeDragTarget by remember { mutableIntStateOf(1) } // 0 = Start (A), 1 = End (B)

  val currentStartMm by rememberUpdatedState(cursorStartMm)
  val currentEndMm by rememberUpdatedState(cursorEndMm)
  val currentRulerMode by rememberUpdatedState(rulerMode)
  val currentActiveAxis by rememberUpdatedState(activeAxis)
  val currentPxPerMmX by rememberUpdatedState(pxPerMmX)
  val currentPxPerMmY by rememberUpdatedState(pxPerMmY)
  val currentActivePxPerMm by rememberUpdatedState(activePxPerMm)
  val currentOnUpdateStartMm by rememberUpdatedState(onUpdateStartMm)
  val currentOnUpdateEndMm by rememberUpdatedState(onUpdateEndMm)
  val currentOnSelectAxis by rememberUpdatedState(onSelectAxis)

  Canvas(
    modifier = modifier
      .pointerInput(Unit) {
        detectTapGestures { offset ->
          val edgeStripPx = 42.dp.toPx()
          // Automatically switch active axis if the user taps directly on a Top/Bottom or Left/Right edge scale
          val targetAxis = when {
            offset.y <= edgeStripPx || offset.y >= size.height - edgeStripPx -> RulerAxis.HORIZONTAL
            offset.x <= edgeStripPx || offset.x >= size.width - edgeStripPx -> RulerAxis.VERTICAL
            else -> currentActiveAxis
          }
          if (targetAxis != currentActiveAxis) {
            currentOnSelectAxis(targetAxis)
          }
          val axisPxPerMm = if (targetAxis == RulerAxis.VERTICAL) currentPxPerMmY else currentPxPerMmX
          val maxAxisPx = if (targetAxis == RulerAxis.VERTICAL) size.height else size.width
          val rawCoordPx = if (targetAxis == RulerAxis.VERTICAL) offset.y else offset.x
          val maxMm = (maxAxisPx / axisPxPerMm).coerceAtLeast(10f)
          val tappedMm = (rawCoordPx / axisPxPerMm).coerceIn(0f, maxMm)

          if (currentRulerMode == RulerMode.SINGLE) {
            currentOnUpdateEndMm(tappedMm)
          } else {
            val target = RulerMath.resolveCaliperTarget(
              touchCoordMm = tappedMm,
              startMm = currentStartMm,
              endMm = currentEndMm,
            )
            if (target == 0) {
              currentOnUpdateStartMm(tappedMm)
            } else {
              currentOnUpdateEndMm(tappedMm)
            }
          }
        }
      }
      .pointerInput(Unit) {
        detectDragGestures(
          onDragStart = { startOffset ->
            val maxAxisPx = if (currentActiveAxis == RulerAxis.VERTICAL) size.height else size.width
            val rawCoordPx = if (currentActiveAxis == RulerAxis.VERTICAL) startOffset.y else startOffset.x
            val maxMm = (maxAxisPx / currentActivePxPerMm).coerceAtLeast(10f)
            val touchedMm = (rawCoordPx / currentActivePxPerMm).coerceIn(0f, maxMm)

            activeDragTarget = if (currentRulerMode == RulerMode.CALIPER) {
              RulerMath.resolveCaliperTarget(
                touchCoordMm = touchedMm,
                startMm = currentStartMm,
                endMm = currentEndMm,
              )
            } else {
              1
            }

            if (activeDragTarget == 0) {
              currentOnUpdateStartMm(touchedMm)
            } else {
              currentOnUpdateEndMm(touchedMm)
            }
          },
          onDrag = { change, _ ->
            change.consume()
            val maxAxisPx = if (currentActiveAxis == RulerAxis.VERTICAL) size.height else size.width
            val rawCoordPx = if (currentActiveAxis == RulerAxis.VERTICAL) change.position.y else change.position.x
            val maxMm = (maxAxisPx / currentActivePxPerMm).coerceAtLeast(10f)
            val currentMm = (rawCoordPx / currentActivePxPerMm).coerceIn(0f, maxMm)
            if (currentRulerMode == RulerMode.CALIPER && activeDragTarget == 0) {
              currentOnUpdateStartMm(currentMm)
            } else {
              currentOnUpdateEndMm(currentMm)
            }
          },
        )
      },
  ) {
    val width = size.width
    val height = size.height
    val edgeBandPx = 38.dp.toPx()

    // 1. Subtle Screen-Edge Scale Bands along all 4 physical borders of the screen
    // Left Edge Band
    drawRect(
      color = surfaceHighColor.copy(alpha = 0.65f),
      topLeft = Offset(0f, 0f),
      size = Size(edgeBandPx, height),
    )
    // Right Edge Band
    drawRect(
      color = surfaceHighColor.copy(alpha = 0.65f),
      topLeft = Offset(width - edgeBandPx, 0f),
      size = Size(edgeBandPx, height),
    )
    // Top Edge Band
    drawRect(
      color = surfaceHighColor.copy(alpha = 0.65f),
      topLeft = Offset(0f, 0f),
      size = Size(width, edgeBandPx * 0.85f),
    )
    // Bottom Edge Band
    drawRect(
      color = surfaceHighColor.copy(alpha = 0.65f),
      topLeft = Offset(0f, height - edgeBandPx * 0.85f),
      size = Size(width, edgeBandPx * 0.85f),
    )

    // 2. Highlighted Active Measurement Span Zone (starts at true 0.0 on the screen edge)
    val maxAxisMm = if (activeAxis == RulerAxis.VERTICAL) {
      (height / pxPerMmY).coerceAtLeast(10f)
    } else {
      (width / pxPerMmX).coerceAtLeast(10f)
    }
    val clampedStartMm = if (rulerMode == RulerMode.SINGLE) {
      0f
    } else {
      cursorStartMm.coerceIn(0f, maxAxisMm)
    }
    val clampedEndMm = cursorEndMm.coerceIn(0f, maxAxisMm)

    if (activeAxis == RulerAxis.VERTICAL) {
      val yStart = clampedStartMm * pxPerMmY
      val yEnd = clampedEndMm * pxPerMmY
      val yMin = min(yStart, yEnd)
      val yMax = max(yStart, yEnd)

      drawRect(
        brush = Brush.verticalGradient(
          colors = listOf(
            primaryColor.copy(alpha = 0.22f),
            primaryColor.copy(alpha = 0.10f),
            primaryColor.copy(alpha = 0.22f),
          ),
          startY = yMin,
          endY = yMax.coerceAtLeast(yMin + 1f),
        ),
        topLeft = Offset(0f, yMin),
        size = Size(width, (yMax - yMin).coerceAtLeast(2f)),
      )
    } else {
      val xStart = clampedStartMm * pxPerMmX
      val xEnd = clampedEndMm * pxPerMmX
      val xMin = min(xStart, xEnd)
      val xMax = max(xStart, xEnd)

      drawRect(
        brush = Brush.horizontalGradient(
          colors = listOf(
            primaryColor.copy(alpha = 0.22f),
            primaryColor.copy(alpha = 0.10f),
            primaryColor.copy(alpha = 0.22f),
          ),
          startX = xMin,
          endX = xMax.coerceAtLeast(xMin + 1f),
        ),
        topLeft = Offset(xMin, 0f),
        size = Size((xMax - xMin).coerceAtLeast(2f), height),
      )
    }

    // All four screen-edge rulers display in the selected unit system (Metric or Imperial).
    val isMetric = primaryUnit == RulerUnitSystem.METRIC

    // 3. Draw Left & Right Vertical Screen-Edge Scales (using ydpi -> pxPerMmY)
    drawVerticalEdgeScale(
      isLeftEdge = true,
      isMetric = isMetric,
      pxPerMm = pxPerMmY,
      onSurfaceColor = onSurfaceColor,
      onSurfaceVariantColor = onSurfaceVariantColor,
    )
    drawVerticalEdgeScale(
      isLeftEdge = false,
      isMetric = isMetric,
      pxPerMm = pxPerMmY,
      onSurfaceColor = onSurfaceColor,
      onSurfaceVariantColor = onSurfaceVariantColor,
    )

    // 4. Draw Top & Bottom Horizontal Screen-Edge Scales (using xdpi -> pxPerMmX)
    drawHorizontalEdgeScale(
      isTopEdge = true,
      isMetric = isMetric,
      pxPerMm = pxPerMmX,
      onSurfaceColor = onSurfaceColor,
      onSurfaceVariantColor = onSurfaceVariantColor,
    )
    drawHorizontalEdgeScale(
      isTopEdge = false,
      isMetric = isMetric,
      pxPerMm = pxPerMmX,
      onSurfaceColor = onSurfaceColor,
      onSurfaceVariantColor = onSurfaceVariantColor,
    )

    // 5. Draw Active Measurement Cursor Lines & Handle Badges reaching all the way to the screen edges
    val startBadgeLabel = if (isMetric) {
      String.format("A · %.1f mm", clampedStartMm)
    } else {
      String.format("A · %.2f in", clampedStartMm / 25.4f)
    }
    val endBadgeLabel = if (isMetric) {
      String.format(
        "%s%.1f mm",
        if (rulerMode == RulerMode.CALIPER) "B · Δ " else "",
        measuredMm,
      )
    } else {
      String.format(
        "%s%.2f in",
        if (rulerMode == RulerMode.CALIPER) "B · Δ " else "",
        measuredMm / 25.4f,
      )
    }

    if (activeAxis == RulerAxis.VERTICAL) {
      val yStart = clampedStartMm * pxPerMmY
      val yEnd = clampedEndMm * pxPerMmY
      val yMin = min(yStart, yEnd)
      val yMax = max(yStart, yEnd)
      val guideX = edgeBandPx + 8.dp.toPx()

      drawLine(
        color = outlineColor.copy(alpha = 0.45f),
        start = Offset(guideX, 0f),
        end = Offset(guideX, height),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f),
      )
      drawLine(
        color = primaryColor,
        start = Offset(guideX, yMin),
        end = Offset(guideX, yMax),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round,
      )

      val startLineColor = if (rulerMode == RulerMode.CALIPER) {
        tertiaryColor
      } else {
        onSurfaceColor.copy(alpha = 0.75f)
      }
      drawLine(
        color = startLineColor,
        start = Offset(0f, yStart),
        end = Offset(width, yStart),
        strokeWidth = if (rulerMode == RulerMode.CALIPER) 2.5.dp.toPx() else 1.8.dp.toPx(),
      )
      if (rulerMode == RulerMode.CALIPER) {
        drawCursorHandleBadge(
          center = Offset(width * 0.5f, yStart),
          label = startBadgeLabel,
          badgeColor = tertiaryColor,
        )
      }

      drawLine(
        color = primaryColor,
        start = Offset(0f, yEnd),
        end = Offset(width, yEnd),
        strokeWidth = 2.8.dp.toPx(),
      )
      drawCursorHandleBadge(
        center = Offset(width * 0.5f, yEnd),
        label = endBadgeLabel,
        badgeColor = primaryColor,
      )
    } else {
      val xStart = clampedStartMm * pxPerMmX
      val xEnd = clampedEndMm * pxPerMmX
      val xMin = min(xStart, xEnd)
      val xMax = max(xStart, xEnd)
      val guideY = edgeBandPx + 6.dp.toPx()

      drawLine(
        color = outlineColor.copy(alpha = 0.45f),
        start = Offset(0f, guideY),
        end = Offset(width, guideY),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f),
      )
      drawLine(
        color = primaryColor,
        start = Offset(xMin, guideY),
        end = Offset(xMax, guideY),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round,
      )

      val startLineColor = if (rulerMode == RulerMode.CALIPER) {
        tertiaryColor
      } else {
        onSurfaceColor.copy(alpha = 0.75f)
      }
      drawLine(
        color = startLineColor,
        start = Offset(xStart, 0f),
        end = Offset(xStart, height),
        strokeWidth = if (rulerMode == RulerMode.CALIPER) 2.5.dp.toPx() else 1.8.dp.toPx(),
      )
      if (rulerMode == RulerMode.CALIPER) {
        drawCursorHandleBadge(
          center = Offset(xStart, height * 0.5f),
          label = startBadgeLabel,
          badgeColor = tertiaryColor,
        )
      }

      drawLine(
        color = primaryColor,
        start = Offset(xEnd, 0f),
        end = Offset(xEnd, height),
        strokeWidth = 2.8.dp.toPx(),
      )
      drawCursorHandleBadge(
        center = Offset(xEnd, height * 0.5f),
        label = endBadgeLabel,
        badgeColor = primaryColor,
      )
    }
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawVerticalEdgeScale(
  isLeftEdge: Boolean,
  isMetric: Boolean,
  pxPerMm: Float,
  onSurfaceColor: Color,
  onSurfaceVariantColor: Color,
) {
  val width = size.width
  val height = size.height
  val edgeX = if (isLeftEdge) 0f else width
  val direction = if (isLeftEdge) 1f else -1f

  val labelPaint = android.graphics.Paint().apply {
    isAntiAlias = true
    color = onSurfaceColor.toArgb()
    textSize = 10.5.dp.toPx()
    textAlign = if (isLeftEdge) {
      android.graphics.Paint.Align.LEFT
    } else {
      android.graphics.Paint.Align.RIGHT
    }
    typeface = android.graphics.Typeface.create(
      android.graphics.Typeface.MONOSPACE,
      android.graphics.Typeface.BOLD,
    )
  }

  if (isMetric) {
    val totalMm = (height / pxPerMm).toInt()
    for (mm in 0..totalMm) {
      val y = mm * pxPerMm
      val isCm = mm % 10 == 0
      val isHalfCm = mm % 5 == 0 && !isCm

      val tickLength = when {
        isCm -> 26.dp.toPx()
        isHalfCm -> 17.dp.toPx()
        else -> 9.dp.toPx()
      }
      val strokeWidth = when {
        isCm -> 2.1.dp.toPx()
        isHalfCm -> 1.4.dp.toPx()
        else -> 0.9.dp.toPx()
      }
      val tickColor = when {
        isCm -> onSurfaceColor
        isHalfCm -> onSurfaceColor.copy(alpha = 0.80f)
        else -> onSurfaceVariantColor.copy(alpha = 0.55f)
      }

      drawLine(
        color = tickColor,
        start = Offset(edgeX, y),
        end = Offset(edgeX + direction * tickLength, y),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
      )

      if (isCm && mm > 0 && y < height - 14.dp.toPx()) {
        val cmNumber = mm / 10
        val textX = edgeX + direction * (tickLength + 3.dp.toPx())
        drawContext.canvas.nativeCanvas.drawText(
          "${cmNumber}cm",
          textX,
          y + 3.5.dp.toPx(),
          labelPaint,
        )
      }
    }
  } else {
    val pxPerInch = pxPerMm * 25.4f
    val pxPerSixteenth = pxPerInch / 16f
    val totalSixteenths = (height / pxPerSixteenth).toInt()

    for (sixteenth in 0..totalSixteenths) {
      val y = sixteenth * pxPerSixteenth
      val isWholeInch = sixteenth % 16 == 0
      val isHalfInch = sixteenth % 8 == 0 && !isWholeInch
      val isQuarterInch = sixteenth % 4 == 0 && !isWholeInch && !isHalfInch
      val isEighthInch = sixteenth % 2 == 0 && !isWholeInch && !isHalfInch && !isQuarterInch

      val tickLength = when {
        isWholeInch -> 28.dp.toPx()
        isHalfInch -> 20.dp.toPx()
        isQuarterInch -> 15.dp.toPx()
        isEighthInch -> 10.dp.toPx()
        else -> 6.5.dp.toPx()
      }
      val strokeWidth = when {
        isWholeInch -> 2.1.dp.toPx()
        isHalfInch -> 1.5.dp.toPx()
        isQuarterInch -> 1.2.dp.toPx()
        else -> 0.85.dp.toPx()
      }
      val tickColor = when {
        isWholeInch -> onSurfaceColor
        isHalfInch || isQuarterInch -> onSurfaceColor.copy(alpha = 0.80f)
        else -> onSurfaceVariantColor.copy(alpha = 0.50f)
      }

      drawLine(
        color = tickColor,
        start = Offset(edgeX, y),
        end = Offset(edgeX + direction * tickLength, y),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
      )

      if (isWholeInch && sixteenth > 0 && y < height - 14.dp.toPx()) {
        val inchNumber = sixteenth / 16
        val textX = edgeX + direction * (tickLength + 3.dp.toPx())
        drawContext.canvas.nativeCanvas.drawText(
          "$inchNumber\"",
          textX,
          y + 3.5.dp.toPx(),
          labelPaint,
        )
      }
    }
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHorizontalEdgeScale(
  isTopEdge: Boolean,
  isMetric: Boolean,
  pxPerMm: Float,
  onSurfaceColor: Color,
  onSurfaceVariantColor: Color,
) {
  val width = size.width
  val height = size.height
  val edgeY = if (isTopEdge) 0f else height
  val direction = if (isTopEdge) 1f else -1f

  val labelPaint = android.graphics.Paint().apply {
    isAntiAlias = true
    color = onSurfaceColor.toArgb()
    textSize = 9.5.dp.toPx()
    textAlign = android.graphics.Paint.Align.CENTER
    typeface = android.graphics.Typeface.create(
      android.graphics.Typeface.MONOSPACE,
      android.graphics.Typeface.BOLD,
    )
  }

  if (isMetric) {
    val totalMm = (width / pxPerMm).toInt()
    for (mm in 0..totalMm) {
      val x = mm * pxPerMm
      val isCm = mm % 10 == 0
      val isHalfCm = mm % 5 == 0 && !isCm

      val tickLength = when {
        isCm -> 18.dp.toPx()
        isHalfCm -> 12.dp.toPx()
        else -> 7.dp.toPx()
      }
      val strokeWidth = when {
        isCm -> 2.0.dp.toPx()
        isHalfCm -> 1.3.dp.toPx()
        else -> 0.85.dp.toPx()
      }
      val tickColor = when {
        isCm -> onSurfaceColor
        isHalfCm -> onSurfaceColor.copy(alpha = 0.78f)
        else -> onSurfaceVariantColor.copy(alpha = 0.50f)
      }

      drawLine(
        color = tickColor,
        start = Offset(x, edgeY),
        end = Offset(x, edgeY + direction * tickLength),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
      )

      if (isCm && mm > 0 && x in 28.dp.toPx()..(width - 28.dp.toPx())) {
        val cmNumber = mm / 10
        val textY = if (isTopEdge) {
          edgeY + tickLength + 10.dp.toPx()
        } else {
          edgeY - tickLength - 4.dp.toPx()
        }
        drawContext.canvas.nativeCanvas.drawText(
          "${cmNumber}cm",
          x,
          textY,
          labelPaint,
        )
      }
    }
  } else {
    val pxPerInch = pxPerMm * 25.4f
    val pxPerSixteenth = pxPerInch / 16f
    val totalSixteenths = (width / pxPerSixteenth).toInt()

    for (sixteenth in 0..totalSixteenths) {
      val x = sixteenth * pxPerSixteenth
      val isWholeInch = sixteenth % 16 == 0
      val isHalfInch = sixteenth % 8 == 0 && !isWholeInch
      val isQuarterInch = sixteenth % 4 == 0 && !isWholeInch && !isHalfInch
      val isEighthInch = sixteenth % 2 == 0 && !isWholeInch && !isHalfInch && !isQuarterInch

      val tickLength = when {
        isWholeInch -> 20.dp.toPx()
        isHalfInch -> 14.dp.toPx()
        isQuarterInch -> 10.dp.toPx()
        isEighthInch -> 7.dp.toPx()
        else -> 4.5.dp.toPx()
      }
      val strokeWidth = when {
        isWholeInch -> 2.0.dp.toPx()
        isHalfInch -> 1.4.dp.toPx()
        isQuarterInch -> 1.1.dp.toPx()
        else -> 0.8.dp.toPx()
      }
      val tickColor = when {
        isWholeInch -> onSurfaceColor
        isHalfInch || isQuarterInch -> onSurfaceColor.copy(alpha = 0.78f)
        else -> onSurfaceVariantColor.copy(alpha = 0.45f)
      }

      drawLine(
        color = tickColor,
        start = Offset(x, edgeY),
        end = Offset(x, edgeY + direction * tickLength),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
      )

      if (isWholeInch && sixteenth > 0 && x in 28.dp.toPx()..(width - 28.dp.toPx())) {
        val inchNumber = sixteenth / 16
        val textY = if (isTopEdge) {
          edgeY + tickLength + 10.dp.toPx()
        } else {
          edgeY - tickLength - 4.dp.toPx()
        }
        drawContext.canvas.nativeCanvas.drawText(
          "$inchNumber\"",
          x,
          textY,
          labelPaint,
        )
      }
    }
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCursorHandleBadge(
  center: Offset,
  label: String,
  badgeColor: Color,
) {
  val textPaint = android.graphics.Paint().apply {
    isAntiAlias = true
    color = android.graphics.Color.WHITE
    textSize = 11.dp.toPx()
    textAlign = android.graphics.Paint.Align.CENTER
    typeface = android.graphics.Typeface.create(
      android.graphics.Typeface.MONOSPACE,
      android.graphics.Typeface.BOLD,
    )
  }

  val textWidth = textPaint.measureText(label)
  val pillWidth = textWidth + 24.dp.toPx()
  val pillHeight = 24.dp.toPx()
  val safeX = center.x.coerceIn(pillWidth / 2f + 4f, size.width - pillWidth / 2f - 4f)
  val safeY = center.y.coerceIn(pillHeight / 2f + 4f, size.height - pillHeight / 2f - 4f)

  drawRoundRect(
    color = badgeColor,
    topLeft = Offset(safeX - pillWidth / 2f, safeY - pillHeight / 2f),
    size = Size(pillWidth, pillHeight),
    cornerRadius = CornerRadius(pillHeight / 2f, pillHeight / 2f),
  )

  drawContext.canvas.nativeCanvas.drawText(
    label,
    safeX,
    safeY + 4.dp.toPx(),
    textPaint,
  )
}

@Preview(showBackground = true)
@Composable
private fun RulerScreenPreview() {
  ToolboxTheme {
    RulerScreen()
  }
}
