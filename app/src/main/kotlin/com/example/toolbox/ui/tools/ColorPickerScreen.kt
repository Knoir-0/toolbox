package com.example.toolbox.ui.tools

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.toolbox.ui.theme.ToolboxTheme
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

enum class ColorSliderMode(val label: String) {
  HSV("HSV Sliders"),
  RGB("RGB Sliders"),
}

enum class HarmonyMode(val label: String) {
  COMPLEMENTARY("Complementary"),
  ANALOGOUS("Analogous"),
  TRIADIC("Triadic"),
  SPLIT_COMPLEMENTARY("Split-Comp"),
  SHADES("Tints & Shades"),
}

data class HsvColor(
  val hue: Float,        // 0f..360f
  val saturation: Float, // 0f..1f
  val value: Float,      // 0f..1f
) {
  val rgb: Triple<Int, Int, Int>
    get() = ColorMath.hsvToRgb(hue, saturation, value)

  val composeColor: Color
    get() {
      val (r, g, b) = rgb
      return Color(r, g, b)
    }

  val hexString: String
    get() {
      val (r, g, b) = rgb
      return ColorMath.toHex(r, g, b)
    }
}

data class HslColor(
  val hue: Int,               // 0..360
  val saturationPercent: Int, // 0..100
  val lightnessPercent: Int,  // 0..100
)

data class CmykColor(
  val c: Int, // 0..100
  val m: Int, // 0..100
  val y: Int, // 0..100
  val k: Int, // 0..100
)

data class NamedColorSwatch(
  val name: String,
  val hex: String,
  val r: Int,
  val g: Int,
  val b: Int,
)

object ColorMath {
  private val presetSwatches = listOf(
    NamedColorSwatch("Ocean Blue", "#23759B", 35, 117, 155),
    NamedColorSwatch("Crimson", "#DC143C", 220, 20, 60),
    NamedColorSwatch("Coral", "#FF6B57", 255, 107, 87),
    NamedColorSwatch("Amber", "#FFBF00", 255, 191, 0),
    NamedColorSwatch("Emerald", "#10B981", 16, 185, 129),
    NamedColorSwatch("Teal", "#0D9488", 13, 148, 136),
    NamedColorSwatch("Sky Blue", "#0EA5E9", 14, 165, 233),
    NamedColorSwatch("Royal Blue", "#2563EB", 37, 99, 235),
    NamedColorSwatch("Indigo", "#4F46E5", 79, 70, 229),
    NamedColorSwatch("Amethyst", "#8B5CF6", 139, 92, 246),
    NamedColorSwatch("Magenta", "#D946EF", 217, 70, 239),
    NamedColorSwatch("Rose", "#F43F5E", 244, 63, 94),
  )

  private val namedReferenceCatalog = presetSwatches + listOf(
    NamedColorSwatch("Pure Red", "#FF0000", 255, 0, 0),
    NamedColorSwatch("Sunset Orange", "#F97316", 249, 115, 22),
    NamedColorSwatch("Gold", "#EAB308", 234, 179, 8),
    NamedColorSwatch("Lime Green", "#84CC16", 132, 204, 22),
    NamedColorSwatch("Forest Green", "#15803D", 21, 128, 61),
    NamedColorSwatch("Mint", "#6EE7B7", 110, 231, 183),
    NamedColorSwatch("Cyan", "#06B6D4", 6, 182, 212),
    NamedColorSwatch("Navy", "#1E3A8A", 30, 58, 138),
    NamedColorSwatch("Lavender", "#C4B5FD", 196, 181, 253),
    NamedColorSwatch("Plum", "#701A75", 112, 26, 117),
    NamedColorSwatch("Peach", "#FDBA74", 253, 186, 116),
    NamedColorSwatch("Terracotta", "#9A3412", 154, 52, 18),
    NamedColorSwatch("Slate", "#64748B", 100, 116, 139),
    NamedColorSwatch("Charcoal", "#1E293B", 30, 41, 59),
    NamedColorSwatch("Silver", "#CBD5E1", 203, 213, 225),
    NamedColorSwatch("Snow White", "#FFFFFF", 255, 255, 255),
    NamedColorSwatch("Jet Black", "#000000", 0, 0, 0),
  )

  fun hsvToRgb(hue: Float, saturation: Float, value: Float): Triple<Int, Int, Int> {
    val h = ((hue % 360f) + 360f) % 360f
    val s = saturation.coerceIn(0f, 1f)
    val v = value.coerceIn(0f, 1f)

    val c = v * s
    val x = c * (1f - abs(((h / 60f) % 2f) - 1f))
    val m = v - c

    val (rPrime, gPrime, bPrime) = when {
      h < 60f -> Triple(c, x, 0f)
      h < 120f -> Triple(x, c, 0f)
      h < 180f -> Triple(0f, c, x)
      h < 240f -> Triple(0f, x, c)
      h < 300f -> Triple(x, 0f, c)
      else -> Triple(c, 0f, x)
    }

    val r = ((rPrime + m) * 255f).roundToInt().coerceIn(0, 255)
    val g = ((gPrime + m) * 255f).roundToInt().coerceIn(0, 255)
    val b = ((bPrime + m) * 255f).roundToInt().coerceIn(0, 255)
    return Triple(r, g, b)
  }

  fun rgbToHsv(r: Int, g: Int, b: Int): HsvColor {
    val rf = r.coerceIn(0, 255) / 255f
    val gf = g.coerceIn(0, 255) / 255f
    val bf = b.coerceIn(0, 255) / 255f

    val maxC = max(rf, max(gf, bf))
    val minC = min(rf, min(gf, bf))
    val delta = maxC - minC

    val hue = when {
      delta == 0f -> 0f
      maxC == rf -> 60f * (((gf - bf) / delta) % 6f)
      maxC == gf -> 60f * (((bf - rf) / delta) + 2f)
      else -> 60f * (((rf - gf) / delta) + 4f)
    }.let { if (it < 0f) it + 360f else it }

    val saturation = if (maxC == 0f) 0f else (delta / maxC)
    return HsvColor(
      hue = hue.coerceIn(0f, 360f),
      saturation = saturation.coerceIn(0f, 1f),
      value = maxC.coerceIn(0f, 1f),
    )
  }

  fun rgbToHsl(r: Int, g: Int, b: Int): HslColor {
    val rf = r.coerceIn(0, 255) / 255f
    val gf = g.coerceIn(0, 255) / 255f
    val bf = b.coerceIn(0, 255) / 255f

    val maxC = max(rf, max(gf, bf))
    val minC = min(rf, min(gf, bf))
    val delta = maxC - minC
    val l = (maxC + minC) / 2f

    val h = when {
      delta == 0f -> 0f
      maxC == rf -> 60f * (((gf - bf) / delta) % 6f)
      maxC == gf -> 60f * (((bf - rf) / delta) + 2f)
      else -> 60f * (((rf - gf) / delta) + 4f)
    }.let { if (it < 0f) it + 360f else it }

    val s = if (delta == 0f) {
      0f
    } else {
      delta / (1f - abs(2f * l - 1f))
    }

    return HslColor(
      hue = h.roundToInt().coerceIn(0, 360),
      saturationPercent = (s * 100f).roundToInt().coerceIn(0, 100),
      lightnessPercent = (l * 100f).roundToInt().coerceIn(0, 100),
    )
  }

  fun rgbToCmyk(r: Int, g: Int, b: Int): CmykColor {
    val rf = r.coerceIn(0, 255) / 255f
    val gf = g.coerceIn(0, 255) / 255f
    val bf = b.coerceIn(0, 255) / 255f

    val k = 1f - max(rf, max(gf, bf))
    if (k >= 0.9999f) {
      return CmykColor(0, 0, 0, 100)
    }
    val c = ((1f - rf - k) / (1f - k) * 100f).roundToInt().coerceIn(0, 100)
    val m = ((1f - gf - k) / (1f - k) * 100f).roundToInt().coerceIn(0, 100)
    val y = ((1f - bf - k) / (1f - k) * 100f).roundToInt().coerceIn(0, 100)
    val kPct = (k * 100f).roundToInt().coerceIn(0, 100)
    return CmykColor(c, m, y, kPct)
  }

  fun toHex(r: Int, g: Int, b: Int): String =
    String.format("#%02X%02X%02X", r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))

  fun parseHex(input: String): Triple<Int, Int, Int>? {
    val cleaned = input.trim().removePrefix("#")
    return when (cleaned.length) {
      3 -> {
        val r = cleaned.substring(0, 1).repeat(2).toIntOrNull(16) ?: return null
        val g = cleaned.substring(1, 2).repeat(2).toIntOrNull(16) ?: return null
        val b = cleaned.substring(2, 3).repeat(2).toIntOrNull(16) ?: return null
        Triple(r, g, b)
      }
      6 -> {
        val r = cleaned.substring(0, 2).toIntOrNull(16) ?: return null
        val g = cleaned.substring(2, 4).toIntOrNull(16) ?: return null
        val b = cleaned.substring(4, 6).toIntOrNull(16) ?: return null
        Triple(r, g, b)
      }
      else -> null
    }
  }

  fun relativeLuminance(r: Int, g: Int, b: Int): Double {
    fun channelLum(c: Int): Double {
      val srgb = c.coerceIn(0, 255) / 255.0
      return if (srgb <= 0.03928) srgb / 12.92 else ((srgb + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * channelLum(r) + 0.7152 * channelLum(g) + 0.0722 * channelLum(b)
  }

  fun contrastRatio(lum1: Double, lum2: Double): Double {
    val lighter = max(lum1, lum2)
    val darker = min(lum1, lum2)
    return (lighter + 0.05) / (darker + 0.05)
  }

  fun wcagRating(contrast: Double): String = when {
    contrast >= 7.0 -> "AAA"
    contrast >= 4.5 -> "AA"
    contrast >= 3.0 -> "AA Large"
    else -> "Low"
  }

  fun closestColorName(r: Int, g: Int, b: Int): String {
    return namedReferenceCatalog.minByOrNull { swatch ->
      val dr = r - swatch.r
      val dg = g - swatch.g
      val db = b - swatch.b
      dr * dr + dg * dg + db * db
    }?.name ?: "Custom Color"
  }

  fun generateHarmony(base: HsvColor, mode: HarmonyMode): List<HsvColor> {
    fun wrapHue(h: Float) = ((h % 360f) + 360f) % 360f
    return when (mode) {
      HarmonyMode.COMPLEMENTARY -> listOf(
        base,
        base.copy(hue = wrapHue(base.hue + 180f)),
      )
      HarmonyMode.ANALOGOUS -> listOf(
        base.copy(hue = wrapHue(base.hue - 30f)),
        base,
        base.copy(hue = wrapHue(base.hue + 30f)),
      )
      HarmonyMode.TRIADIC -> listOf(
        base,
        base.copy(hue = wrapHue(base.hue + 120f)),
        base.copy(hue = wrapHue(base.hue + 240f)),
      )
      HarmonyMode.SPLIT_COMPLEMENTARY -> listOf(
        base,
        base.copy(hue = wrapHue(base.hue + 150f)),
        base.copy(hue = wrapHue(base.hue + 210f)),
      )
      HarmonyMode.SHADES -> listOf(
        base.copy(saturation = (base.saturation * 0.45f).coerceIn(0f, 1f), value = 0.96f),
        base.copy(saturation = (base.saturation * 0.75f).coerceIn(0f, 1f), value = 0.88f),
        base,
        base.copy(value = (base.value * 0.70f).coerceIn(0.12f, 1f)),
        base.copy(value = (base.value * 0.42f).coerceIn(0.08f, 1f)),
      )
    }
  }
}

/**
 * The accent color the user picked in system settings (Wallpaper & style).
 *
 * On Android 12+ this reads the system's mid-tone accent ([android.R.color.system_accent1_500]),
 * which is the closest public representation of the user's chosen seed color and is identical in
 * light and dark mode. Older versions have no user-selectable accent, so the app theme's primary
 * color is used instead.
 */
@SuppressLint("LocalContextGetResourceValueCall")
@Composable
private fun rememberSystemAccentColor(): Color {
  val context = LocalContext.current
  val configuration = LocalConfiguration.current
  val fallback = MaterialTheme.colorScheme.primary
  return remember(context, configuration, fallback) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      Color(context.getColor(android.R.color.system_accent1_500))
    } else {
      fallback
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerScreen(modifier: Modifier = Modifier) {
  val systemAccentColor = rememberSystemAccentColor()
  val initialRgb = remember(systemAccentColor) {
    val argb = systemAccentColor.toArgb()
    Triple(
      (argb shr 16) and 0xFF,
      (argb shr 8) and 0xFF,
      argb and 0xFF,
    )
  }
  val initialHsv = remember(initialRgb) {
    ColorMath.rgbToHsv(initialRgb.first, initialRgb.second, initialRgb.third)
  }
  val initialHex = remember(initialRgb) {
    ColorMath.toHex(initialRgb.first, initialRgb.second, initialRgb.third)
  }

  var hue by rememberSaveable(initialHex) { mutableFloatStateOf(initialHsv.hue) }
  var saturation by rememberSaveable(initialHex) { mutableFloatStateOf(initialHsv.saturation) }
  var value by rememberSaveable(initialHex) { mutableFloatStateOf(initialHsv.value) }

  var sliderMode by rememberSaveable { mutableStateOf(ColorSliderMode.HSV) }
  var harmonyMode by rememberSaveable { mutableStateOf(HarmonyMode.COMPLEMENTARY) }
  var hexInput by rememberSaveable(initialHex) { mutableStateOf(initialHex) }
  var lastCopiedLabel by rememberSaveable { mutableStateOf<String?>(null) }

  val savedPalette = remember(initialHex) {
    mutableStateListOf(
      *listOf(
        initialHex,
        "#10B981",
        "#F43F5E",
        "#8B5CF6",
        "#FFBF00",
      ).distinct().toTypedArray(),
    )
  }

  val currentHsv = remember(hue, saturation, value) { HsvColor(hue, saturation, value) }
  val (r, g, b) = currentHsv.rgb
  val hexCode = currentHsv.hexString
  val hsl = remember(r, g, b) { ColorMath.rgbToHsl(r, g, b) }
  val cmyk = remember(r, g, b) { ColorMath.rgbToCmyk(r, g, b) }
  val colorName = remember(r, g, b) { ColorMath.closestColorName(r, g, b) }
  val harmonies = remember(currentHsv, harmonyMode) {
    ColorMath.generateHarmony(currentHsv, harmonyMode)
  }

  val context = LocalContext.current

  fun updateFromHsv(newH: Float, newS: Float, newV: Float) {
    hue = newH.coerceIn(0f, 360f)
    saturation = newS.coerceIn(0f, 1f)
    value = newV.coerceIn(0f, 1f)
    val (nr, ng, nb) = ColorMath.hsvToRgb(hue, saturation, value)
    hexInput = ColorMath.toHex(nr, ng, nb)
  }

  fun updateFromRgb(newR: Int, newG: Int, newB: Int) {
    val converted = ColorMath.rgbToHsv(newR, newG, newB)
    hue = converted.hue
    saturation = converted.saturation
    value = converted.value
    hexInput = ColorMath.toHex(newR, newG, newB)
  }

  fun copyToClipboard(label: String, text: String) {
    try {
      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
      clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    } catch (_: Throwable) {
      // Safe fallback in test environments
    }
    lastCopiedLabel = "$label ($text)"
  }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // 1. Hero Color Preview & WCAG Contrast Card
    item {
      ColorHeroCard(
        hsvColor = currentHsv,
        r = r,
        g = g,
        b = b,
        hexCode = hexCode,
        colorName = colorName,
        lastCopiedLabel = lastCopiedLabel,
        isSaved = savedPalette.contains(hexCode),
        onCopyHex = { copyToClipboard("HEX", hexCode) },
        onRandomize = {
          updateFromHsv(
            newH = Random.nextFloat() * 360f,
            newS = Random.nextFloat() * 0.65f + 0.35f,
            newV = Random.nextFloat() * 0.55f + 0.45f,
          )
        },
        onSaveSwatch = {
          if (!savedPalette.contains(hexCode)) {
            savedPalette.add(0, hexCode)
          }
        },
      )
    }

    // 2. Interactive 2D Saturation/Value Canvas & Hue Spectrum Bar
    item {
      InteractiveCanvasPickerCard(
        hue = hue,
        saturation = saturation,
        value = value,
        onSaturationValueChange = { newS, newV ->
          updateFromHsv(hue, newS, newV)
        },
        onHueChange = { newH ->
          updateFromHsv(newH, saturation, value)
        },
      )
    }

    // 3. Precision Sliders Card (HSV / RGB)
    item {
      PrecisionSlidersCard(
        sliderMode = sliderMode,
        onSelectSliderMode = { sliderMode = it },
        hue = hue,
        saturation = saturation,
        value = value,
        r = r,
        g = g,
        b = b,
        onUpdateHsv = ::updateFromHsv,
        onUpdateRgb = ::updateFromRgb,
      )
    }

    // 4. Direct HEX Input & Multi-Format Code Table (HEX, RGB, HSL, HSV, CMYK)
    item {
      ColorFormatsCard(
        hexInput = hexInput,
        currentHex = hexCode,
        r = r,
        g = g,
        b = b,
        hsv = currentHsv,
        hsl = hsl,
        cmyk = cmyk,
        onHexInputChange = { newText ->
          val filtered = newText.uppercase().take(7)
          hexInput = filtered
          ColorMath.parseHex(filtered)?.let { (pr, pg, pb) ->
            if (filtered.removePrefix("#").length == 6) {
              val converted = ColorMath.rgbToHsv(pr, pg, pb)
              hue = converted.hue
              saturation = converted.saturation
              value = converted.value
            }
          }
        },
        onApplyHex = {
          ColorMath.parseHex(hexInput)?.let { (pr, pg, pb) ->
            updateFromRgb(pr, pg, pb)
          }
        },
        onCopyFormat = ::copyToClipboard,
      )
    }

    // 5. Color Harmonies Generator Card
    item {
      ColorHarmoniesCard(
        harmonyMode = harmonyMode,
        onSelectHarmonyMode = { harmonyMode = it },
        harmonies = harmonies,
        activeHex = hexCode,
        onSelectHarmonyColor = { selected ->
          updateFromHsv(selected.hue, selected.saturation, selected.value)
        },
      )
    }

    // 6. Saved Palette Card
    item {
      SavedPaletteCard(
        savedPalette = savedPalette,
        activeHex = hexCode,
        onSelectHex = { selectedHex ->
          ColorMath.parseHex(selectedHex)?.let { (pr, pg, pb) ->
            updateFromRgb(pr, pg, pb)
          }
        },
        onSaveCurrent = {
          if (!savedPalette.contains(hexCode)) {
            savedPalette.add(0, hexCode)
          }
        },
        onClearSaved = { savedPalette.clear() },
      )
    }
  }
}

@Composable
private fun ColorHeroCard(
  hsvColor: HsvColor,
  r: Int,
  g: Int,
  b: Int,
  hexCode: String,
  colorName: String,
  lastCopiedLabel: String?,
  isSaved: Boolean,
  onCopyHex: () -> Unit,
  onRandomize: () -> Unit,
  onSaveSwatch: () -> Unit,
) {
  val lum = remember(r, g, b) { ColorMath.relativeLuminance(r, g, b) }
  val whiteContrast = remember(lum) { ColorMath.contrastRatio(lum, 1.0) }
  val blackContrast = remember(lum) { ColorMath.contrastRatio(lum, 0.0) }
  val foregroundColor = if (whiteContrast >= blackContrast) Color.White else Color(0xFF101820)

  Card(
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Large Color Swatch Banner
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .height(152.dp),
        shape = RoundedCornerShape(22.dp),
        color = hsvColor.composeColor,
        border = BorderStroke(1.5.dp, foregroundColor.copy(alpha = 0.22f)),
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
          verticalArrangement = Arrangement.SpaceBetween,
        ) {
          // Top Row: Closest Color Name Pill + Copy Status Badge
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Surface(
              shape = RoundedCornerShape(50),
              color = foregroundColor.copy(alpha = 0.16f),
              border = BorderStroke(1.dp, foregroundColor.copy(alpha = 0.28f)),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Palette,
                  contentDescription = null,
                  tint = foregroundColor,
                  modifier = Modifier.size(15.dp),
                )
                Text(
                  text = colorName,
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  color = foregroundColor,
                )
              }
            }

            if (lastCopiedLabel != null) {
              Badge(
                containerColor = foregroundColor.copy(alpha = 0.22f),
                contentColor = foregroundColor,
              ) {
                Text(
                  text = "Copied $lastCopiedLabel",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }

          // Bottom Row: Large HEX Readout + WCAG Contrast Badges
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
          ) {
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
              AnimatedContent(
                targetState = hexCode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "HeroHexAnimation",
              ) { hex ->
                Text(
                  text = hex,
                  style = MaterialTheme.typography.headlineLarge,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Black,
                  color = foregroundColor,
                )
              }
              Text(
                text = "RGB($r, $g, $b) · ${hsvColor.hue.roundToInt()}°",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = foregroundColor.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
              horizontalAlignment = Alignment.End,
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              ContrastBadgePill(
                sampleLabel = "White",
                ratio = whiteContrast,
                bgColor = Color.White.copy(alpha = 0.9f),
                textColor = Color(0xFF101820),
              )
              ContrastBadgePill(
                sampleLabel = "Black",
                ratio = blackContrast,
                bgColor = Color(0xFF101820).copy(alpha = 0.85f),
                textColor = Color.White,
              )
            }
          }
        }
      }

      // Quick Actions Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Button(
          onClick = onCopyHex,
          modifier = Modifier.weight(1.2f),
          shape = RoundedCornerShape(14.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = null,
            modifier = Modifier
              .padding(end = 6.dp)
              .size(18.dp),
          )
          Text(
            text = "Copy HEX",
            fontWeight = FontWeight.Bold,
            maxLines = 1,
          )
        }

        FilledTonalButton(
          onClick = onRandomize,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(14.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Shuffle,
            contentDescription = null,
            modifier = Modifier
              .padding(end = 6.dp)
              .size(18.dp),
          )
          Text(
            text = "Random",
            fontWeight = FontWeight.Bold,
            maxLines = 1,
          )
        }

        OutlinedButton(
          onClick = onSaveSwatch,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(14.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
        ) {
          Icon(
            imageVector = if (isSaved) Icons.Default.Check else Icons.Default.BookmarkAdd,
            contentDescription = null,
            modifier = Modifier
              .padding(end = 6.dp)
              .size(18.dp),
          )
          Text(
            text = if (isSaved) "Saved" else "Save",
            fontWeight = FontWeight.Bold,
            maxLines = 1,
          )
        }
      }
    }
  }
}

@Composable
private fun ContrastBadgePill(
  sampleLabel: String,
  ratio: Double,
  bgColor: Color,
  textColor: Color,
) {
  val rating = ColorMath.wcagRating(ratio)
  Surface(
    shape = RoundedCornerShape(50),
    color = bgColor,
  ) {
    Text(
      text = String.format("%s %.1f:1 (%s)", sampleLabel, ratio, rating),
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = textColor,
    )
  }
}

@Composable
private fun InteractiveCanvasPickerCard(
  hue: Float,
  saturation: Float,
  value: Float,
  onSaturationValueChange: (Float, Float) -> Unit,
  onHueChange: (Float) -> Unit,
) {
  val pureHueColor = remember(hue) {
    val (hr, hg, hb) = ColorMath.hsvToRgb(hue, 1f, 1f)
    Color(hr, hg, hb)
  }

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
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "2D Color Canvas",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = "Tap or drag to pick saturation & brightness",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      // 2D Saturation-Value Picker Box
      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(170.dp)
          .clip(RoundedCornerShape(18.dp))
          .pointerInput(Unit) {
            detectTapGestures { offset ->
              val s = (offset.x / size.width).coerceIn(0f, 1f)
              val v = (1f - (offset.y / size.height)).coerceIn(0f, 1f)
              onSaturationValueChange(s, v)
            }
          }
          .pointerInput(Unit) {
            detectDragGestures { change, _ ->
              change.consume()
              val s = (change.position.x / size.width).coerceIn(0f, 1f)
              val v = (1f - (change.position.y / size.height)).coerceIn(0f, 1f)
              onSaturationValueChange(s, v)
            }
          },
      ) {
        // Horizontal gradient: White -> Pure Hue
        drawRoundRect(
          brush = Brush.horizontalGradient(
            colors = listOf(Color.White, pureHueColor),
          ),
          cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
        )
        // Vertical gradient overlay: Transparent -> Black
        drawRoundRect(
          brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Black),
          ),
          cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
        )

        // Thumb indicator
        val thumbX = saturation.coerceIn(0f, 1f) * size.width
        val thumbY = (1f - value.coerceIn(0f, 1f)) * size.height
        val (cr, cg, cb) = ColorMath.hsvToRgb(hue, saturation, value)
        val activeColor = Color(cr, cg, cb)

        drawCircle(
          color = Color.Black.copy(alpha = 0.45f),
          radius = 13.dp.toPx(),
          center = Offset(thumbX, thumbY),
          style = Stroke(width = 3.dp.toPx()),
        )
        drawCircle(
          color = Color.White,
          radius = 11.dp.toPx(),
          center = Offset(thumbX, thumbY),
          style = Stroke(width = 3.dp.toPx()),
        )
        drawCircle(
          color = activeColor,
          radius = 8.dp.toPx(),
          center = Offset(thumbX, thumbY),
        )
      }

      // Hue Spectrum Ribbon
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = "Hue Spectrum",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = "${hue.roundToInt()}°",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
          )
        }

        val rainbowColors = remember {
          listOf(
            Color(0xFFFF0000),
            Color(0xFFFFFF00),
            Color(0xFF00FF00),
            Color(0xFF00FFFF),
            Color(0xFF0000FF),
            Color(0xFFFF00FF),
            Color(0xFFFF0000),
          )
        }

        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(50))
            .pointerInput(Unit) {
              detectTapGestures { offset ->
                val newH = (offset.x / size.width).coerceIn(0f, 1f) * 360f
                onHueChange(newH)
              }
            }
            .pointerInput(Unit) {
              detectDragGestures { change, _ ->
                change.consume()
                val newH = (change.position.x / size.width).coerceIn(0f, 1f) * 360f
                onHueChange(newH)
              }
            },
        ) {
          drawRoundRect(
            brush = Brush.horizontalGradient(rainbowColors),
            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
          )
          val thumbX = (hue.coerceIn(0f, 360f) / 360f) * size.width
          val thumbY = size.height / 2f
          drawCircle(
            color = Color.Black.copy(alpha = 0.45f),
            radius = 12.dp.toPx(),
            center = Offset(thumbX, thumbY),
            style = Stroke(width = 3.dp.toPx()),
          )
          drawCircle(
            color = Color.White,
            radius = 10.dp.toPx(),
            center = Offset(thumbX, thumbY),
            style = Stroke(width = 3.dp.toPx()),
          )
          drawCircle(
            color = pureHueColor,
            radius = 7.dp.toPx(),
            center = Offset(thumbX, thumbY),
          )
        }
      }
    }
  }
}

@Composable
private fun PrecisionSlidersCard(
  sliderMode: ColorSliderMode,
  onSelectSliderMode: (ColorSliderMode) -> Unit,
  hue: Float,
  saturation: Float,
  value: Float,
  r: Int,
  g: Int,
  b: Int,
  onUpdateHsv: (Float, Float, Float) -> Unit,
  onUpdateRgb: (Int, Int, Int) -> Unit,
) {
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
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
          )
          Text(
            text = "Channel Controls",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
        }
      }

      SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        ColorSliderMode.entries.forEachIndexed { index, mode ->
          SegmentedButton(
            selected = sliderMode == mode,
            onClick = { onSelectSliderMode(mode) },
            shape = SegmentedButtonDefaults.itemShape(
              index = index,
              count = ColorSliderMode.entries.size,
            ),
          ) {
            Text(
              text = mode.label,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }
      }

      when (sliderMode) {
        ColorSliderMode.HSV -> {
          ChannelSliderRow(
            label = "Hue",
            valueDisplay = "${hue.roundToInt()}°",
            value = hue,
            valueRange = 0f..360f,
            accentColor = MaterialTheme.colorScheme.primary,
            onValueChange = { onUpdateHsv(it, saturation, value) },
            onStep = { delta -> onUpdateHsv((hue + delta).coerceIn(0f, 360f), saturation, value) },
          )
          ChannelSliderRow(
            label = "Saturation",
            valueDisplay = "${(saturation * 100).roundToInt()}%",
            value = saturation * 100f,
            valueRange = 0f..100f,
            accentColor = MaterialTheme.colorScheme.secondary,
            onValueChange = { onUpdateHsv(hue, it / 100f, value) },
            onStep = { delta -> onUpdateHsv(hue, ((saturation * 100f + delta) / 100f).coerceIn(0f, 1f), value) },
          )
          ChannelSliderRow(
            label = "Brightness",
            valueDisplay = "${(value * 100).roundToInt()}%",
            value = value * 100f,
            valueRange = 0f..100f,
            accentColor = MaterialTheme.colorScheme.tertiary,
            onValueChange = { onUpdateHsv(hue, saturation, it / 100f) },
            onStep = { delta -> onUpdateHsv(hue, saturation, ((value * 100f + delta) / 100f).coerceIn(0f, 1f)) },
          )
        }

        ColorSliderMode.RGB -> {
          ChannelSliderRow(
            label = "Red",
            valueDisplay = "$r",
            value = r.toFloat(),
            valueRange = 0f..255f,
            accentColor = MaterialTheme.colorScheme.primary,
            onValueChange = { onUpdateRgb(it.roundToInt(), g, b) },
            onStep = { delta -> onUpdateRgb((r + delta.roundToInt()).coerceIn(0, 255), g, b) },
          )
          ChannelSliderRow(
            label = "Green",
            valueDisplay = "$g",
            value = g.toFloat(),
            valueRange = 0f..255f,
            accentColor = MaterialTheme.colorScheme.secondary,
            onValueChange = { onUpdateRgb(r, it.roundToInt(), b) },
            onStep = { delta -> onUpdateRgb(r, (g + delta.roundToInt()).coerceIn(0, 255), b) },
          )
          ChannelSliderRow(
            label = "Blue",
            valueDisplay = "$b",
            value = b.toFloat(),
            valueRange = 0f..255f,
            accentColor = MaterialTheme.colorScheme.tertiary,
            onValueChange = { onUpdateRgb(r, g, it.roundToInt()) },
            onStep = { delta -> onUpdateRgb(r, g, (b + delta.roundToInt()).coerceIn(0, 255)) },
          )
        }
      }
    }
  }
}

@Composable
private fun ChannelSliderRow(
  label: String,
  valueDisplay: String,
  value: Float,
  valueRange: ClosedFloatingPointRange<Float>,
  accentColor: Color,
  onValueChange: (Float) -> Unit,
  onStep: (Float) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        text = valueDisplay,
        style = MaterialTheme.typography.labelLarge,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = accentColor,
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      FilledTonalIconButton(
        onClick = { onStep(-1f) },
        modifier = Modifier.size(32.dp),
      ) {
        Icon(
          imageVector = Icons.Default.Remove,
          contentDescription = "Decrease $label",
          modifier = Modifier.size(16.dp),
        )
      }

      SlimDotSlider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        accentColor = accentColor,
        modifier = Modifier.weight(1f),
      )

      FilledTonalIconButton(
        onClick = { onStep(1f) },
        modifier = Modifier.size(32.dp),
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Increase $label",
          modifier = Modifier.size(16.dp),
        )
      }
    }
  }
}

@Composable
private fun ColorFormatsCard(
  hexInput: String,
  currentHex: String,
  r: Int,
  g: Int,
  b: Int,
  hsv: HsvColor,
  hsl: HslColor,
  cmyk: CmykColor,
  onHexInputChange: (String) -> Unit,
  onApplyHex: () -> Unit,
  onCopyFormat: (String, String) -> Unit,
) {
  val isHexValid = remember(hexInput) { ColorMath.parseHex(hexInput) != null }

  val formats = remember(currentHex, r, g, b, hsv, hsl, cmyk) {
    listOf(
      "HEX" to currentHex,
      "RGB" to "rgb($r, $g, $b)",
      "HSL" to "hsl(${hsl.hue}, ${hsl.saturationPercent}%, ${hsl.lightnessPercent}%)",
      "HSV" to "hsv(${hsv.hue.roundToInt()}°, ${(hsv.saturation * 100).roundToInt()}%, ${(hsv.value * 100).roundToInt()}%)",
      "CMYK" to "cmyk(${cmyk.c}%, ${cmyk.m}%, ${cmyk.y}%, ${cmyk.k}%)",
    )
  }

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
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = "HEX Code & Color Formats",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedTextField(
          value = hexInput,
          onValueChange = onHexInputChange,
          modifier = Modifier.weight(1f),
          label = { Text("Enter HEX (#RRGGBB)") },
          singleLine = true,
          isError = hexInput.isNotEmpty() && !isHexValid,
          textStyle = MaterialTheme.typography.titleMedium.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
          ),
          shape = RoundedCornerShape(16.dp),
          trailingIcon = {
            if (hexInput.isNotEmpty()) {
              IconButton(onClick = { onHexInputChange("") }) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Clear HEX",
                )
              }
            }
          },
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
          ),
        )

        FilledTonalButton(
          onClick = onApplyHex,
          enabled = isHexValid,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.height(54.dp),
        ) {
          Text("Apply", fontWeight = FontWeight.Bold)
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        formats.forEach { (formatName, formattedValue) ->
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onCopyFormat(formatName, formattedValue) },
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Badge(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
              ) {
                Text(
                  text = formatName,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  fontWeight = FontWeight.Bold,
                )
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = formattedValue,
                  style = MaterialTheme.typography.bodyMedium,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                )
                Icon(
                  imageVector = Icons.Default.ContentCopy,
                  contentDescription = "Copy $formatName",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp),
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
private fun ColorHarmoniesCard(
  harmonyMode: HarmonyMode,
  onSelectHarmonyMode: (HarmonyMode) -> Unit,
  harmonies: List<HsvColor>,
  activeHex: String,
  onSelectHarmonyColor: (HsvColor) -> Unit,
) {
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
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Color Harmonies",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = "Tap swatch to apply",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        HarmonyMode.entries.forEach { mode ->
          val isSelected = mode == harmonyMode
          FilterChip(
            selected = isSelected,
            onClick = { onSelectHarmonyMode(mode) },
            label = {
              Text(
                text = mode.label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        harmonies.forEach { swatchHsv ->
          val swatchHex = swatchHsv.hexString
          val isCurrent = swatchHex == activeHex
          Surface(
            onClick = { onSelectHarmonyColor(swatchHsv) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(
              width = if (isCurrent) 2.dp else 1.dp,
              color = if (isCurrent) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
              },
            ),
          ) {
            Column(
              modifier = Modifier.padding(8.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .background(swatchHsv.composeColor, RoundedCornerShape(10.dp))
                  .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(10.dp),
                  ),
              )
              Text(
                text = swatchHex,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SavedPaletteCard(
  savedPalette: List<String>,
  activeHex: String,
  onSelectHex: (String) -> Unit,
  onSaveCurrent: () -> Unit,
  onClearSaved: () -> Unit,
) {
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
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Saved Palette Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Saved Palette (${savedPalette.size})",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          FilledTonalButton(
            onClick = onSaveCurrent,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = null,
              modifier = Modifier
                .padding(end = 4.dp)
                .size(15.dp),
            )
            Text("Add Current", style = MaterialTheme.typography.labelMedium)
          }
          if (savedPalette.isNotEmpty()) {
            IconButton(
              onClick = onClearSaved,
              modifier = Modifier.size(32.dp),
            ) {
              Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = "Clear Saved Palette",
                modifier = Modifier.size(18.dp),
              )
            }
          }
        }
      }

      if (savedPalette.isEmpty()) {
        Text(
          text = "No saved swatches yet. Tap 'Save' or 'Add Current' to pin colors here.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      } else {
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          savedPalette.forEach { hex ->
            val rgb = ColorMath.parseHex(hex) ?: Triple(0, 0, 0)
            val swatchColor = Color(rgb.first, rgb.second, rgb.third)
            val isSelected = hex.equals(activeHex, ignoreCase = true)

            Surface(
              onClick = { onSelectHex(hex) },
              shape = RoundedCornerShape(50),
              color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
              } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
              },
              border = BorderStroke(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                },
              ),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Box(
                  modifier = Modifier
                    .size(16.dp)
                    .background(swatchColor, CircleShape)
                    .border(1.dp, Color.Black.copy(alpha = 0.25f), CircleShape),
                )
                Text(
                  text = hex,
                  style = MaterialTheme.typography.labelMedium,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun ColorPickerScreenPreview() {
  ToolboxTheme {
    ColorPickerScreen()
  }
}
