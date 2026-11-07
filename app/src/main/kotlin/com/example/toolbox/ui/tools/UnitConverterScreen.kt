package com.example.toolbox.ui.tools

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toolbox.ui.theme.ToolboxTheme
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.abs
import kotlinx.coroutines.launch

enum class UnitCategory(
  val label: String,
  val icon: ImageVector,
  val baseUnitSymbol: String,
) {
  LENGTH("Length", Icons.Default.Straighten, "m"),
  WEIGHT("Weight", Icons.Default.Scale, "kg"),
  TEMPERATURE("Temp", Icons.Default.Thermostat, "°C"),
  VOLUME("Volume", Icons.Default.LocalDrink, "L"),
  SPEED("Speed", Icons.Default.Speed, "m/s"),
}

data class MeasurementUnit(
  val id: String,
  val name: String,
  val symbol: String,
  val category: UnitCategory,
  val toBase: (Double) -> Double,
  val fromBase: (Double) -> Double,
)

object UnitCatalog {
  val lengthUnits = listOf(
    MeasurementUnit("mm", "Millimeter", "mm", UnitCategory.LENGTH, { it * 0.001 }, { it / 0.001 }),
    MeasurementUnit("cm", "Centimeter", "cm", UnitCategory.LENGTH, { it * 0.01 }, { it / 0.01 }),
    MeasurementUnit("m", "Meter", "m", UnitCategory.LENGTH, { it }, { it }),
    MeasurementUnit("km", "Kilometer", "km", UnitCategory.LENGTH, { it * 1000.0 }, { it / 1000.0 }),
    MeasurementUnit("in", "Inch", "in", UnitCategory.LENGTH, { it * 0.0254 }, { it / 0.0254 }),
    MeasurementUnit("ft", "Foot", "ft", UnitCategory.LENGTH, { it * 0.3048 }, { it / 0.3048 }),
    MeasurementUnit("yd", "Yard", "yd", UnitCategory.LENGTH, { it * 0.9144 }, { it / 0.9144 }),
    MeasurementUnit("mi", "Mile", "mi", UnitCategory.LENGTH, { it * 1609.344 }, { it / 1609.344 }),
  )

  val weightUnits = listOf(
    MeasurementUnit("mg", "Milligram", "mg", UnitCategory.WEIGHT, { it * 1e-6 }, { it / 1e-6 }),
    MeasurementUnit("g", "Gram", "g", UnitCategory.WEIGHT, { it * 0.001 }, { it / 0.001 }),
    MeasurementUnit("kg", "Kilogram", "kg", UnitCategory.WEIGHT, { it }, { it }),
    MeasurementUnit("t", "Metric Ton", "t", UnitCategory.WEIGHT, { it * 1000.0 }, { it / 1000.0 }),
    MeasurementUnit("oz", "Ounce", "oz", UnitCategory.WEIGHT, { it * 0.028349523125 }, { it / 0.028349523125 }),
    MeasurementUnit("lb", "Pound", "lb", UnitCategory.WEIGHT, { it * 0.45359237 }, { it / 0.45359237 }),
    MeasurementUnit("st", "Stone", "st", UnitCategory.WEIGHT, { it * 6.35029318 }, { it / 6.35029318 }),
  )

  val temperatureUnits = listOf(
    MeasurementUnit("c", "Celsius", "°C", UnitCategory.TEMPERATURE, { it }, { it }),
    MeasurementUnit(
      "f",
      "Fahrenheit",
      "°F",
      UnitCategory.TEMPERATURE,
      { (it - 32.0) * 5.0 / 9.0 },
      { (it * 9.0 / 5.0) + 32.0 },
    ),
    MeasurementUnit(
      "k",
      "Kelvin",
      "K",
      UnitCategory.TEMPERATURE,
      { it - 273.15 },
      { it + 273.15 },
    ),
  )

  val volumeUnits = listOf(
    MeasurementUnit("ml", "Milliliter", "mL", UnitCategory.VOLUME, { it * 0.001 }, { it / 0.001 }),
    MeasurementUnit("l", "Liter", "L", UnitCategory.VOLUME, { it }, { it }),
    MeasurementUnit("tsp", "Teaspoon (US)", "tsp", UnitCategory.VOLUME, { it * 0.00492892159 }, { it / 0.00492892159 }),
    MeasurementUnit("tbsp", "Tablespoon (US)", "tbsp", UnitCategory.VOLUME, { it * 0.0147867648 }, { it / 0.0147867648 }),
    MeasurementUnit("floz", "Fluid Ounce (US)", "fl oz", UnitCategory.VOLUME, { it * 0.0295735296 }, { it / 0.0295735296 }),
    MeasurementUnit("cup", "Cup (US)", "cup", UnitCategory.VOLUME, { it * 0.2365882365 }, { it / 0.2365882365 }),
    MeasurementUnit("pt", "Pint (US)", "pt", UnitCategory.VOLUME, { it * 0.473176473 }, { it / 0.473176473 }),
    MeasurementUnit("gal", "Gallon (US)", "gal", UnitCategory.VOLUME, { it * 3.785411784 }, { it / 3.785411784 }),
  )

  val speedUnits = listOf(
    MeasurementUnit("mps", "Meters / sec", "m/s", UnitCategory.SPEED, { it }, { it }),
    MeasurementUnit("kmh", "Kilometers / hr", "km/h", UnitCategory.SPEED, { it / 3.6 }, { it * 3.6 }),
    MeasurementUnit("mph", "Miles / hr", "mph", UnitCategory.SPEED, { it * 0.44704 }, { it / 0.44704 }),
    MeasurementUnit("kn", "Knot", "kn", UnitCategory.SPEED, { it * 0.514444 }, { it / 0.514444 }),
  )

  fun unitsFor(category: UnitCategory): List<MeasurementUnit> = when (category) {
    UnitCategory.LENGTH -> lengthUnits
    UnitCategory.WEIGHT -> weightUnits
    UnitCategory.TEMPERATURE -> temperatureUnits
    UnitCategory.VOLUME -> volumeUnits
    UnitCategory.SPEED -> speedUnits
  }

  fun convert(value: Double, from: MeasurementUnit, to: MeasurementUnit): Double {
    if (from.id == to.id) return value
    val baseValue = from.toBase(value)
    return to.fromBase(baseValue)
  }

  fun formatNumber(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "—"
    if (abs(value) < 1e-12) return "0"
    val absVal = abs(value)
    return if (absVal >= 1_000_000.0 || (absVal > 0 && absVal < 0.0001)) {
      String.format("%.4e", value)
    } else {
      val bd = BigDecimal(value, MathContext(7, RoundingMode.HALF_UP))
        .setScale(4, RoundingMode.HALF_UP)
        .stripTrailingZeros()
      bd.toPlainString()
    }
  }
}

@Immutable
private data class ConverterPalette(
  val screenBackground: Color,
  val heroCardSurface: Color,
  val heroCardBorder: Color,
  val heroGlow: Color,
  val accentPrimary: Color,
  val onAccentPrimary: Color,
  val accentSoftContainer: Color,
  val onAccentSoftContainer: Color,
  val accentBorder: Color,
  val fromWellSurface: Color,
  val fromWellBorder: Color,
  val toWellGradientStart: Color,
  val toWellGradientEnd: Color,
  val toWellBorder: Color,
  val pillSurface: Color,
  val pillBorder: Color,
  val targetPillSurface: Color,
  val targetPillBorder: Color,
  val rowSurface: Color,
  val rowBorder: Color,
  val rowSourceSurface: Color,
  val rowSourceBorder: Color,
  val rowTargetSurface: Color,
  val rowTargetBorder: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  val textMuted: Color,
  val symbolTileSurface: Color,
  val symbolTileContent: Color,
)

@Composable
private fun rememberConverterPalette(): ConverterPalette {
  val colorScheme = MaterialTheme.colorScheme
  return remember(colorScheme) {
    ConverterPalette(
      screenBackground = colorScheme.background,
      heroCardSurface = colorScheme.surfaceContainerHigh,
      heroCardBorder = colorScheme.outlineVariant.copy(alpha = 0.7f),
      heroGlow = colorScheme.primary.copy(alpha = 0.12f),
      accentPrimary = colorScheme.primary,
      onAccentPrimary = colorScheme.onPrimary,
      accentSoftContainer = colorScheme.primaryContainer,
      onAccentSoftContainer = colorScheme.onPrimaryContainer,
      accentBorder = colorScheme.primary.copy(alpha = 0.5f),
      fromWellSurface = colorScheme.surfaceContainerLow,
      fromWellBorder = colorScheme.outlineVariant.copy(alpha = 0.6f),
      toWellGradientStart = colorScheme.primaryContainer.copy(alpha = 0.55f),
      toWellGradientEnd = colorScheme.secondaryContainer.copy(alpha = 0.45f),
      toWellBorder = colorScheme.primary.copy(alpha = 0.6f),
      pillSurface = colorScheme.surfaceContainerHighest,
      pillBorder = colorScheme.outlineVariant,
      targetPillSurface = colorScheme.primaryContainer,
      targetPillBorder = colorScheme.primary,
      rowSurface = colorScheme.surfaceContainerLow,
      rowBorder = colorScheme.outlineVariant.copy(alpha = 0.5f),
      rowSourceSurface = colorScheme.secondaryContainer.copy(alpha = 0.55f),
      rowSourceBorder = colorScheme.secondary.copy(alpha = 0.6f),
      rowTargetSurface = colorScheme.primaryContainer.copy(alpha = 0.65f),
      rowTargetBorder = colorScheme.primary,
      textPrimary = colorScheme.onSurface,
      textSecondary = colorScheme.onSurfaceVariant,
      textMuted = colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
      symbolTileSurface = colorScheme.secondaryContainer,
      symbolTileContent = colorScheme.onSecondaryContainer,
    )
  }
}

@Composable
fun UnitConverterScreen(modifier: Modifier = Modifier) {
  var selectedCategory by rememberSaveable { mutableStateOf(UnitCategory.LENGTH) }
  var fromIndex by rememberSaveable { mutableIntStateOf(2) } // Default Meter
  var toIndex by rememberSaveable { mutableIntStateOf(5) }   // Default Foot
  var inputText by rememberSaveable { mutableStateOf("1") }

  val palette = rememberConverterPalette()

  val units = remember(selectedCategory) { UnitCatalog.unitsFor(selectedCategory) }
  val safeFromIndex = fromIndex.coerceIn(0, units.lastIndex)
  val safeToIndex = toIndex.coerceIn(0, units.lastIndex)
  val fromUnit = units[safeFromIndex]
  val toUnit = units[safeToIndex]

  val numericInput = inputText.toDoubleOrNull() ?: 0.0
  val convertedValue = remember(numericInput, fromUnit, toUnit) {
    UnitCatalog.convert(numericInput, fromUnit, toUnit)
  }
  val singleUnitRate = remember(fromUnit, toUnit) {
    UnitCatalog.convert(1.0, fromUnit, toUnit)
  }

  val swapRotation = remember { Animatable(0f) }
  val coroutineScope = rememberCoroutineScope()

  fun swapUnits() {
    val temp = safeFromIndex
    fromIndex = safeToIndex
    toIndex = temp
    coroutineScope.launch {
      swapRotation.snapTo(0f)
      swapRotation.animateTo(
        targetValue = 180f,
        animationSpec = spring(
          dampingRatio = Spring.DampingRatioMediumBouncy,
          stiffness = Spring.StiffnessMedium,
        ),
      )
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(palette.screenBackground),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // 1. Category Selector Row
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        UnitCategory.entries.forEach { category ->
          val isSelected = category == selectedCategory
          val containerColor by animateColorAsState(
            targetValue = if (isSelected) palette.accentPrimary else palette.rowSurface,
            animationSpec = tween(180),
            label = "CategoryBg",
          )
          val contentColor by animateColorAsState(
            targetValue = if (isSelected) palette.onAccentPrimary else palette.textSecondary,
            animationSpec = tween(180),
            label = "CategoryContent",
          )

          Surface(
            selected = isSelected,
            onClick = {
              selectedCategory = category
              fromIndex = 0
              toIndex = 1.coerceAtMost(UnitCatalog.unitsFor(category).lastIndex)
            },
            shape = RoundedCornerShape(50),
            color = containerColor,
            border = BorderStroke(
              width = 1.dp,
              color = if (isSelected) palette.accentPrimary else palette.rowBorder,
            ),
            shadowElevation = if (isSelected) 3.dp else 0.dp,
          ) {
            Row(
              modifier = Modifier.padding(start = 10.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(
                    if (isSelected) {
                      palette.onAccentPrimary.copy(alpha = 0.18f)
                    } else {
                      palette.symbolTileSurface
                    },
                  ),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = category.icon,
                  contentDescription = null,
                  tint = if (isSelected) palette.onAccentPrimary else palette.symbolTileContent,
                  modifier = Modifier.size(14.dp),
                )
              }
              Text(
                text = category.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = contentColor,
              )
            }
          }
        }
      }
    }

    // 2. Hero Conversion Card
    item {
      ConversionHeroCard(
        category = selectedCategory,
        units = units,
        fromUnit = fromUnit,
        toUnit = toUnit,
        onSelectFromUnit = { fromIndex = it },
        onSelectToUnit = { toIndex = it },
        inputText = inputText,
        onInputChange = { newText ->
          if (newText.isEmpty() || newText == "-" || newText == "." || newText == "-." || newText.toDoubleOrNull() != null) {
            inputText = newText
          }
        },
        convertedFormatted = UnitCatalog.formatNumber(convertedValue),
        rateFormatted = "1 ${fromUnit.symbol} = ${UnitCatalog.formatNumber(singleUnitRate)} ${toUnit.symbol}",
        swapRotation = swapRotation.value,
        onSwapUnits = ::swapUnits,
        palette = palette,
      )
    }

    // 3. Quick Value Presets
    item {
      QuickValuePresetsRow(
        category = selectedCategory,
        currentInput = inputText,
        onSelectPreset = { preset -> inputText = preset },
        palette = palette,
      )
    }

    // 4. Unified All-Units Comparison Card
    item {
      AllUnitsComparisonCard(
        category = selectedCategory,
        units = units,
        numericInput = numericInput,
        fromUnit = fromUnit,
        toUnit = toUnit,
        onSelectTargetUnit = { index -> toIndex = index },
        palette = palette,
      )
    }
  }
}

@Composable
private fun AllUnitsComparisonCard(
  category: UnitCategory,
  units: List<MeasurementUnit>,
  numericInput: Double,
  fromUnit: MeasurementUnit,
  toUnit: MeasurementUnit,
  onSelectTargetUnit: (Int) -> Unit,
  palette: ConverterPalette,
) {
  Card(
    shape = RoundedCornerShape(26.dp),
    colors = CardDefaults.cardColors(
      containerColor = palette.heroCardSurface,
    ),
    border = BorderStroke(1.dp, palette.heroCardBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Card Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "All ${category.label} Conversions",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.ExtraBold,
          color = palette.textPrimary,
        )
        Surface(
          shape = RoundedCornerShape(50),
          color = palette.accentSoftContainer,
          border = BorderStroke(1.dp, palette.accentBorder.copy(alpha = 0.45f)),
        ) {
          Text(
            text = "${units.size} units",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = palette.onAccentSoftContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      HorizontalDivider(color = palette.rowBorder)

      // Unit Rows inside the single card
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 6.dp),
      ) {
        units.forEachIndexed { index, targetUnit ->
          val valueInTarget = UnitCatalog.convert(numericInput, fromUnit, targetUnit)
          val isCurrentTarget = targetUnit.id == toUnit.id
          val isCurrentSource = targetUnit.id == fromUnit.id

          val rowBg = when {
            isCurrentTarget -> palette.rowTargetSurface
            isCurrentSource -> palette.rowSourceSurface
            else -> Color.Transparent
          }
          val rowBorder = when {
            isCurrentTarget -> BorderStroke(1.dp, palette.rowTargetBorder.copy(alpha = 0.65f))
            isCurrentSource -> BorderStroke(1.dp, palette.rowSourceBorder.copy(alpha = 0.55f))
            else -> null
          }

          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .clickable { onSelectTargetUnit(index) },
            shape = RoundedCornerShape(16.dp),
            color = rowBg,
            border = rowBorder,
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                // Monogram / Symbol Tile
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                      when {
                        isCurrentTarget -> palette.accentPrimary
                        isCurrentSource -> palette.accentSoftContainer
                        else -> palette.symbolTileSurface
                      },
                    ),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = targetUnit.symbol,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = when {
                      isCurrentTarget -> palette.onAccentPrimary
                      isCurrentSource -> palette.onAccentSoftContainer
                      else -> palette.symbolTileContent
                    },
                    maxLines = 1,
                  )
                }

                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Text(
                      text = targetUnit.name,
                      style = MaterialTheme.typography.bodyLarge,
                      fontWeight = if (isCurrentTarget || isCurrentSource) FontWeight.Bold else FontWeight.SemiBold,
                      color = palette.textPrimary,
                    )
                    if (isCurrentSource) {
                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = palette.textSecondary.copy(alpha = 0.14f),
                      ) {
                        Text(
                          text = "FROM",
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.ExtraBold,
                          color = palette.textSecondary,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                      }
                    } else if (isCurrentTarget) {
                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = palette.accentPrimary,
                      ) {
                        Text(
                          text = "TO",
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.ExtraBold,
                          color = palette.onAccentPrimary,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                      }
                    }
                  }
                  val ratePerOne = UnitCatalog.convert(1.0, fromUnit, targetUnit)
                  Text(
                    text = "1 ${fromUnit.symbol} = ${UnitCatalog.formatNumber(ratePerOne)} ${targetUnit.symbol}",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textMuted,
                  )
                }
              }

              val formattedVal = UnitCatalog.formatNumber(valueInTarget)
              Text(
                text = buildAnnotatedString {
                  withStyle(
                    SpanStyle(
                      fontWeight = FontWeight.ExtraBold,
                      color = if (isCurrentTarget) palette.accentPrimary else palette.textPrimary,
                    ),
                  ) {
                    append(formattedVal)
                  }
                  append(" ")
                  withStyle(
                    SpanStyle(
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 13.sp,
                      color = if (isCurrentTarget) {
                        palette.accentPrimary.copy(alpha = 0.85f)
                      } else {
                        palette.textSecondary
                      },
                    ),
                  ) {
                    append(targetUnit.symbol)
                  }
                },
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.End,
              )
            }
          }

          val nextUnit = units.getOrNull(index + 1)
          val nextIsHighlighted = nextUnit != null && (nextUnit.id == toUnit.id || nextUnit.id == fromUnit.id)
          if (index < units.lastIndex && !isCurrentTarget && !isCurrentSource && !nextIsHighlighted) {
            HorizontalDivider(
              modifier = Modifier.padding(start = 60.dp, end = 10.dp),
              color = palette.rowBorder.copy(alpha = 0.65f),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ConversionHeroCard(
  category: UnitCategory,
  units: List<MeasurementUnit>,
  fromUnit: MeasurementUnit,
  toUnit: MeasurementUnit,
  onSelectFromUnit: (Int) -> Unit,
  onSelectToUnit: (Int) -> Unit,
  inputText: String,
  onInputChange: (String) -> Unit,
  convertedFormatted: String,
  rateFormatted: String,
  swapRotation: Float,
  onSwapUnits: () -> Unit,
  palette: ConverterPalette,
) {
  Card(
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(
      containerColor = palette.heroCardSurface,
    ),
    border = BorderStroke(1.dp, palette.heroCardBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.radialGradient(
            colors = listOf(
              palette.heroGlow,
              Color.Transparent,
            ),
          ),
        ),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // Top Header with category badge & conversion formula rate
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
            shape = RoundedCornerShape(50),
            color = palette.accentSoftContainer,
            border = BorderStroke(1.dp, palette.accentBorder.copy(alpha = 0.45f)),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = palette.accentPrimary,
                modifier = Modifier.size(15.dp),
              )
              Text(
                text = "${fromUnit.symbol} → ${toUnit.symbol}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = palette.onAccentSoftContainer,
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(50),
            color = palette.fromWellSurface,
            border = BorderStroke(1.dp, palette.fromWellBorder),
          ) {
            Text(
              text = rateFormatted,
              style = MaterialTheme.typography.labelMedium,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.SemiBold,
              color = palette.textSecondary,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            )
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Seamless Input Field ("From") — unified value input + flush unit selector
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(22.dp),
          color = palette.fromWellSurface,
          border = BorderStroke(1.dp, palette.fromWellBorder),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = "From Unit (${fromUnit.name})",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = palette.textSecondary,
            )

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              BasicTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = MaterialTheme.typography.headlineLarge.copy(
                  fontWeight = FontWeight.Black,
                  color = palette.textPrimary,
                ),
                cursorBrush = SolidColor(palette.accentPrimary),
                decorationBox = { innerTextField ->
                  Box(contentAlignment = Alignment.CenterStart) {
                    if (inputText.isEmpty()) {
                      Text(
                        text = "0",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = palette.textMuted.copy(alpha = 0.45f),
                      )
                    }
                    innerTextField()
                  }
                },
              )

              if (inputText.isNotEmpty()) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  onClick = { onInputChange("") },
                  modifier = Modifier.size(24.dp),
                  shape = CircleShape,
                  color = palette.textMuted.copy(alpha = 0.16f),
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Default.Clear,
                      contentDescription = "Clear value",
                      tint = palette.textSecondary,
                      modifier = Modifier.size(14.dp),
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.width(10.dp))

              // Seamless vertical divider between text input and unit selector
              Box(
                modifier = Modifier
                  .height(28.dp)
                  .width(1.dp)
                  .background(palette.fromWellBorder),
              )

              InlineUnitSelector(
                selectedUnit = fromUnit,
                units = units,
                isTargetAccent = false,
                onSelectUnit = onSelectFromUnit,
                palette = palette,
              )
            }
          }
        }

        // Sleek Swap Bridge Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          HorizontalDivider(
            modifier = Modifier
              .weight(1f)
              .padding(start = 12.dp),
            color = palette.heroCardBorder,
          )
          Surface(
            onClick = onSwapUnits,
            modifier = Modifier
              .padding(horizontal = 14.dp)
              .size(40.dp)
              .graphicsLayer { rotationZ = swapRotation },
            shape = CircleShape,
            color = palette.accentPrimary,
            contentColor = palette.onAccentPrimary,
            border = BorderStroke(3.dp, palette.heroCardSurface),
            shadowElevation = 4.dp,
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.SwapVert,
                contentDescription = "Swap units",
                modifier = Modifier.size(20.dp),
              )
            }
          }
          HorizontalDivider(
            modifier = Modifier
              .weight(1f)
              .padding(end = 12.dp),
            color = palette.heroCardBorder,
          )
        }

        // Seamless Result Display Field ("To") — unified value readout + flush unit selector
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
              Brush.linearGradient(
                colors = listOf(
                  palette.toWellGradientStart,
                  palette.toWellGradientEnd,
                ),
              ),
            )
            .border(
              width = 1.25.dp,
              color = palette.toWellBorder,
              shape = RoundedCornerShape(22.dp),
            ),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = "To Unit (${toUnit.name})",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = palette.accentPrimary,
            )

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              AnimatedContent(
                targetState = convertedFormatted,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ConvertedValueAnimation",
                modifier = Modifier.weight(1f),
              ) { valStr ->
                Text(
                  text = valStr,
                  style = MaterialTheme.typography.headlineLarge,
                  fontWeight = FontWeight.Black,
                  color = palette.textPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }

              Spacer(modifier = Modifier.width(10.dp))

              // Seamless vertical divider between converted readout and unit selector
              Box(
                modifier = Modifier
                  .height(28.dp)
                  .width(1.dp)
                  .background(palette.toWellBorder.copy(alpha = 0.65f)),
              )

              InlineUnitSelector(
                selectedUnit = toUnit,
                units = units,
                isTargetAccent = true,
                onSelectUnit = onSelectToUnit,
                palette = palette,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun InlineUnitSelector(
  selectedUnit: MeasurementUnit,
  units: List<MeasurementUnit>,
  isTargetAccent: Boolean = false,
  onSelectUnit: (Int) -> Unit,
  palette: ConverterPalette,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    Row(
      modifier = Modifier
        .clip(RoundedCornerShape(12.dp))
        .clickable { expanded = true }
        .padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isTargetAccent) palette.accentPrimary else palette.accentSoftContainer,
      ) {
        Text(
          text = selectedUnit.symbol,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.ExtraBold,
          color = if (isTargetAccent) palette.onAccentPrimary else palette.onAccentSoftContainer,
          modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
        )
      }
      Text(
        text = selectedUnit.name,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = palette.textPrimary,
        maxLines = 1,
      )
      Icon(
        imageVector = Icons.Default.KeyboardArrowDown,
        contentDescription = "Select unit",
        tint = if (isTargetAccent) palette.accentPrimary else palette.textSecondary,
        modifier = Modifier.size(18.dp),
      )
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
    ) {
      units.forEachIndexed { index, unit ->
        val isSelected = unit.id == selectedUnit.id
        DropdownMenuItem(
          text = {
            Text(
              text = "${unit.symbol} · ${unit.name}",
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            )
          },
          onClick = {
            onSelectUnit(index)
            expanded = false
          },
          trailingIcon = if (isSelected) {
            {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = palette.accentPrimary,
                modifier = Modifier.size(18.dp),
              )
            }
          } else {
            null
          },
        )
      }
    }
  }
}

@Composable
private fun QuickValuePresetsRow(
  category: UnitCategory,
  currentInput: String,
  onSelectPreset: (String) -> Unit,
  palette: ConverterPalette,
) {
  val presets = remember(category) {
    when (category) {
      UnitCategory.TEMPERATURE -> listOf("-40", "0", "20", "37", "100", "212")
      else -> listOf("0.5", "1", "5", "10", "25", "100")
    }
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = "Quick values:",
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.SemiBold,
      color = palette.textSecondary,
      modifier = Modifier.padding(end = 2.dp),
    )
    presets.forEach { preset ->
      val isSelected = currentInput.trim() == preset
      Surface(
        onClick = { onSelectPreset(preset) },
        shape = RoundedCornerShape(50),
        color = if (isSelected) palette.accentSoftContainer else palette.rowSurface,
        border = BorderStroke(
          width = 1.dp,
          color = if (isSelected) palette.accentPrimary else palette.rowBorder,
        ),
        shadowElevation = if (isSelected) 1.dp else 0.dp,
      ) {
        Text(
          text = preset,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
          color = if (isSelected) palette.onAccentSoftContainer else palette.textPrimary,
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        )
      }
    }
  }
}

@Preview(name = "Light", showBackground = true)
@Preview(
  name = "Dark",
  showBackground = true,
  uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun UnitConverterScreenPreview() {
  ToolboxTheme {
    UnitConverterScreen()
  }
}
