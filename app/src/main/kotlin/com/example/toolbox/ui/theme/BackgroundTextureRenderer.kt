package com.example.toolbox.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.example.toolbox.ui.main.BackgroundTexture
import com.example.toolbox.ui.main.NavigationPreferencesBridge
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * CompositionLocal holding the active [BackgroundTexture] selected in Settings.
 * When `null`, falls back to [NavigationPreferencesBridge.backgroundTexture].
 */
val LocalBackgroundTexture = compositionLocalOf<BackgroundTexture?> { null }

/**
 * CompositionLocal holding whether circular icon buttons are hexagonified with vertical left and right sides.
 * When `null`, falls back to [NavigationPreferencesBridge.hexagonIconButtons].
 */
val LocalHexagonIconButtons = compositionLocalOf<Boolean?> { null }

/**
 * Resolves the active [BackgroundTexture] from [LocalBackgroundTexture] or [NavigationPreferencesBridge].
 */
@Composable
fun currentBackgroundTexture(): BackgroundTexture =
  LocalBackgroundTexture.current ?: NavigationPreferencesBridge.backgroundTexture

/**
 * Resolves whether icon buttons should be hexagonified from [LocalHexagonIconButtons] or [NavigationPreferencesBridge].
 */
@Composable
fun currentHexagonIconButtons(): Boolean =
  LocalHexagonIconButtons.current ?: NavigationPreferencesBridge.hexagonIconButtons

/**
 * Computes the 6 clockwise vertices of a pointy-topped/bottomed hexagon for [size] where
 * the left and right sides are strictly vertical (`x_topRight == x_bottomRight` and `x_topLeft == x_bottomLeft`).
 *
 * Vertex order (clockwise):
 * - `0`: Top point `(cx, cy - r)`
 * - `1`: Top-right corner `(cx + halfWidth, cy - halfVerticalEdge)`
 * - `2`: Bottom-right corner `(cx + halfWidth, cy + halfVerticalEdge)` (Right side is vertical)
 * - `3`: Bottom point `(cx, cy + r)`
 * - `4`: Bottom-left corner `(cx - halfWidth, cy + halfVerticalEdge)`
 * - `5`: Top-left corner `(cx - halfWidth, cy - halfVerticalEdge)` (Left side is vertical)
 */
fun createVerticalSidedHexagonVertices(size: Size): List<Offset> {
  val w = size.width
  val h = size.height
  val cx = w * 0.5f
  val cy = h * 0.5f
  val isSquareish = w <= h * 1.25f && h <= w * 1.25f
  return if (isSquareish) {
    val r = min(w, h) * 0.5f
    val halfWidth = r * (sqrt(3f) * 0.5f)
    val halfVerticalEdge = r * 0.5f
    listOf(
      Offset(cx, cy - r),
      Offset(cx + halfWidth, cy - halfVerticalEdge),
      Offset(cx + halfWidth, cy + halfVerticalEdge),
      Offset(cx, cy + r),
      Offset(cx - halfWidth, cy + halfVerticalEdge),
      Offset(cx - halfWidth, cy - halfVerticalEdge),
    )
  } else {
    val halfW = w * 0.5f
    val halfH = h * 0.5f
    val halfVerticalEdge = halfH * 0.5f
    listOf(
      Offset(cx, 0f),
      Offset(cx + halfW, cy - halfVerticalEdge),
      Offset(cx + halfW, cy + halfVerticalEdge),
      Offset(cx, h),
      Offset(cx - halfW, cy + halfVerticalEdge),
      Offset(cx - halfW, cy - halfVerticalEdge),
    )
  }
}

/**
 * Builds a [Path] for a hexagon with vertical left and right sides and subtly rounded vertices.
 */
fun createVerticalSidedHexagonPath(
  size: Size,
  cornerRadiusFraction: Float = 0.08f,
): Path {
  val vertices = createVerticalSidedHexagonVertices(size)
  val path = Path()
  if (size.width <= 0f || size.height <= 0f) return path

  val cornerRadius = (min(size.width, size.height) * cornerRadiusFraction).coerceAtLeast(0f)
  if (cornerRadius <= 0.01f) {
    vertices.forEachIndexed { idx, pt ->
      if (idx == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
    }
    path.close()
    return path
  }

  for (i in 0 until 6) {
    val prev = vertices[(i + 5) % 6]
    val curr = vertices[i]
    val next = vertices[(i + 1) % 6]

    val dx1 = prev.x - curr.x
    val dy1 = prev.y - curr.y
    val len1 = hypot(dx1, dy1).coerceAtLeast(0.001f)

    val dx2 = next.x - curr.x
    val dy2 = next.y - curr.y
    val len2 = hypot(dx2, dy2).coerceAtLeast(0.001f)

    val d = minOf(cornerRadius, len1 * 0.35f, len2 * 0.35f)
    val startX = curr.x + (dx1 / len1) * d
    val startY = curr.y + (dy1 / len1) * d
    val endX = curr.x + (dx2 / len2) * d
    val endY = curr.y + (dy2 / len2) * d

    if (i == 0) {
      path.moveTo(startX, startY)
    } else {
      path.lineTo(startX, startY)
    }
    path.quadraticTo(curr.x, curr.y, endX, endY)
  }
  path.close()
  return path
}

/**
 * Hexagon [Shape] with strictly vertical left and right sides and top/bottom points.
 */
object HexagonShape : Shape {
  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density,
  ): Outline = Outline.Generic(createVerticalSidedHexagonPath(size))
}

/**
 * Returns `true` when [size] is a circular (`1:1`) icon button, badge, or FAB container
 * that should be rendered as a vertical-sided hexagon when [hexagonEnabled] is `true`.
 */
fun shouldUseHexagonIconOutline(
  size: Size,
  hexagonEnabled: Boolean = NavigationPreferencesBridge.hexagonIconButtons,
): Boolean {
  return hexagonEnabled &&
    size.width > 0f &&
    size.height > 0f &&
    max(size.width, size.height) <= min(size.width, size.height) * 1.25f
}

/**
 * Adaptive icon button [Shape] that renders a vertical-sided [HexagonShape] for circular (`1:1`)
 * icon buttons, badges, and FABs when [NavigationPreferencesBridge.hexagonIconButtons] is enabled,
 * and delegates to standard [androidx.compose.foundation.shape.CircleShape] otherwise (or for wide text pills).
 */
object ToolboxIconShape : Shape {
  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density,
  ): Outline {
    if (shouldUseHexagonIconOutline(size)) {
      return HexagonShape.createOutline(size, layoutDirection, density)
    }
    return androidx.compose.foundation.shape.CircleShape.createOutline(size, layoutDirection, density)
  }
}

/**
 * Drop-in [CircleShape] token for Toolbox UI screens that automatically hexagonifies circular (`1:1`)
 * action icon buttons, icon badges, and FABs with vertical left and right sides when the user enables
 * "Hexagonify Icon Buttons" in Settings.
 */
val CircleShape: Shape = ToolboxIconShape

@Composable
fun FilledIconButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = CircleShape,
  colors: IconButtonColors = IconButtonDefaults.filledIconButtonColors(),
  interactionSource: MutableInteractionSource? = null,
  content: @Composable () -> Unit,
) {
  androidx.compose.material3.FilledIconButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = shape,
    colors = colors,
    interactionSource = interactionSource,
    content = content,
  )
}

@Composable
fun FilledTonalIconButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = CircleShape,
  colors: IconButtonColors = IconButtonDefaults.filledTonalIconButtonColors(),
  interactionSource: MutableInteractionSource? = null,
  content: @Composable () -> Unit,
) {
  androidx.compose.material3.FilledTonalIconButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = shape,
    colors = colors,
    interactionSource = interactionSource,
    content = content,
  )
}

@Composable
fun OutlinedIconButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = CircleShape,
  colors: IconButtonColors = IconButtonDefaults.outlinedIconButtonColors(),
  border: BorderStroke? = IconButtonDefaults.outlinedIconButtonBorder(enabled),
  interactionSource: MutableInteractionSource? = null,
  content: @Composable () -> Unit,
) {
  androidx.compose.material3.OutlinedIconButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = shape,
    colors = colors,
    border = border,
    interactionSource = interactionSource,
    content = content,
  )
}

@Composable
fun FloatingActionButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  shape: Shape = CircleShape,
  containerColor: Color = FloatingActionButtonDefaults.containerColor,
  contentColor: Color = contentColorFor(containerColor),
  elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
  interactionSource: MutableInteractionSource? = null,
  content: @Composable () -> Unit,
) {
  androidx.compose.material3.FloatingActionButton(
    onClick = onClick,
    modifier = modifier,
    shape = shape,
    containerColor = containerColor,
    contentColor = contentColor,
    elevation = elevation,
    interactionSource = interactionSource,
    content = content,
  )
}

@Composable
fun SmallFloatingActionButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  shape: Shape = CircleShape,
  containerColor: Color = FloatingActionButtonDefaults.containerColor,
  contentColor: Color = contentColorFor(containerColor),
  elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
  interactionSource: MutableInteractionSource? = null,
  content: @Composable () -> Unit,
) {
  androidx.compose.material3.SmallFloatingActionButton(
    onClick = onClick,
    modifier = modifier,
    shape = shape,
    containerColor = containerColor,
    contentColor = contentColor,
    elevation = elevation,
    interactionSource = interactionSource,
    content = content,
  )
}

/**
 * Draws the solid [baseColor] background followed by the active procedural [BackgroundTexture]
 * across the container's canvas before any child content is rendered.
 */
@Composable
fun Modifier.toolboxScreenBackground(
  baseColor: Color = MaterialTheme.colorScheme.background,
  texture: BackgroundTexture = currentBackgroundTexture(),
): Modifier {
  if (texture == BackgroundTexture.NONE) {
    return this.background(baseColor)
  }
  val colorScheme = MaterialTheme.colorScheme
  val primaryColor = colorScheme.primary
  val tertiaryColor = colorScheme.tertiary
  val secondaryColor = colorScheme.secondary
  val onSurfaceColor = colorScheme.onSurface
  val isDark = baseColor.luminance() < 0.5f

  return this.drawBehind {
    drawRect(color = baseColor)
    drawBackgroundTexture(
      texture = texture,
      primaryColor = primaryColor,
      tertiaryColor = tertiaryColor,
      secondaryColor = secondaryColor,
      onSurfaceColor = onSurfaceColor,
      isDarkTheme = isDark,
      isPreview = false,
    )
  }
}

/**
 * Draws the [brush] background followed by the active procedural [BackgroundTexture]
 * across the container's canvas before any child content is rendered.
 */
@Composable
fun Modifier.toolboxScreenBackground(
  brush: Brush,
  texture: BackgroundTexture = currentBackgroundTexture(),
): Modifier {
  if (texture == BackgroundTexture.NONE) {
    return this.background(brush)
  }
  val colorScheme = MaterialTheme.colorScheme
  val primaryColor = colorScheme.primary
  val tertiaryColor = colorScheme.tertiary
  val secondaryColor = colorScheme.secondary
  val onSurfaceColor = colorScheme.onSurface
  val isDark = colorScheme.background.luminance() < 0.5f

  return this.drawBehind {
    drawRect(brush = brush)
    drawBackgroundTexture(
      texture = texture,
      primaryColor = primaryColor,
      tertiaryColor = tertiaryColor,
      secondaryColor = secondaryColor,
      onSurfaceColor = onSurfaceColor,
      isDarkTheme = isDark,
      isPreview = false,
    )
  }
}

/**
 * Renders a live miniature preview swatch of [texture] for Settings cards and badges.
 */
@Composable
fun BackgroundTexturePreviewBox(
  texture: BackgroundTexture,
  modifier: Modifier = Modifier,
  baseColor: Color = MaterialTheme.colorScheme.background,
) {
  val colorScheme = MaterialTheme.colorScheme
  val primaryColor = colorScheme.primary
  val tertiaryColor = colorScheme.tertiary
  val secondaryColor = colorScheme.secondary
  val onSurfaceColor = colorScheme.onSurface
  val isDark = baseColor.luminance() < 0.5f

  if (texture == BackgroundTexture.NONE) {
    Box(modifier = modifier.background(baseColor))
  } else {
    Canvas(modifier = modifier) {
      drawRect(color = baseColor)
      drawBackgroundTexture(
        texture = texture,
        primaryColor = primaryColor,
        tertiaryColor = tertiaryColor,
        secondaryColor = secondaryColor,
        onSurfaceColor = onSurfaceColor,
        isDarkTheme = isDark,
        isPreview = true,
      )
    }
  }
}

/**
 * Deterministic 32-bit integer hash for procedural texture generation without allocations.
 */
internal fun proceduralHash(seed: Int): Int {
  var x = seed
  x = ((x ushr 16) xor x) * 0x45d9f3b
  x = ((x ushr 16) xor x) * 0x45d9f3b
  x = (x ushr 16) xor x
  return x and 0x7FFFFFFF
}

internal fun proceduralFloat(seed: Int): Float =
  (proceduralHash(seed) % 10_000) / 10_000f

/**
 * Draws the procedural [texture] directly onto this [DrawScope].
 */
fun DrawScope.drawBackgroundTexture(
  texture: BackgroundTexture,
  primaryColor: Color,
  tertiaryColor: Color,
  secondaryColor: Color,
  onSurfaceColor: Color,
  isDarkTheme: Boolean,
  isPreview: Boolean = false,
) {
  val w = size.width
  val h = size.height
  if (w <= 0f || h <= 0f || texture == BackgroundTexture.NONE) return

  val minDim = min(w, h)
  val maxDim = max(w, h)
  val alphaBoost = if (isPreview) 2.25f else 1.0f
  val baseAlpha = ((if (isDarkTheme) 0.16f else 0.13f) * alphaBoost).coerceIn(0.02f, 0.72f)

  when (texture) {
    BackgroundTexture.NONE -> Unit
    BackgroundTexture.FUZZY -> drawFuzzyTexture(
      w = w,
      h = h,
      maxDim = maxDim,
      primaryColor = primaryColor,
      tertiaryColor = tertiaryColor,
      secondaryColor = secondaryColor,
      onSurfaceColor = onSurfaceColor,
      baseAlpha = baseAlpha,
      isPreview = isPreview,
    )
    BackgroundTexture.FADE -> drawFadeTexture(
      w = w,
      h = h,
      maxDim = maxDim,
      primaryColor = primaryColor,
      tertiaryColor = tertiaryColor,
      secondaryColor = secondaryColor,
      baseAlpha = baseAlpha,
    )
    BackgroundTexture.CONCENTRIC -> drawConcentricTexture(
      w = w,
      h = h,
      maxDim = maxDim,
      primaryColor = primaryColor,
      tertiaryColor = tertiaryColor,
      secondaryColor = secondaryColor,
      baseAlpha = baseAlpha,
      isPreview = isPreview,
    )
    BackgroundTexture.HALFTONE -> drawHalftoneTexture(
      w = w,
      h = h,
      primaryColor = primaryColor,
      tertiaryColor = tertiaryColor,
      baseAlpha = baseAlpha,
      isPreview = isPreview,
    )
    BackgroundTexture.TOPOGRAPHY -> drawTopographyTexture(
      w = w,
      h = h,
      primaryColor = primaryColor,
      tertiaryColor = tertiaryColor,
      secondaryColor = secondaryColor,
      baseAlpha = baseAlpha,
      isPreview = isPreview,
    )
    BackgroundTexture.AURORA -> drawAuroraTexture(
      w = w,
      h = h,
      primaryColor = primaryColor,
      tertiaryColor = tertiaryColor,
      secondaryColor = secondaryColor,
      baseAlpha = baseAlpha,
    )
    BackgroundTexture.STARFIELD -> drawStarfieldTexture(
      w = w,
      h = h,
      maxDim = maxDim,
      primaryColor = primaryColor,
      tertiaryColor = tertiaryColor,
      onSurfaceColor = onSurfaceColor,
      baseAlpha = baseAlpha,
      isPreview = isPreview,
    )
    BackgroundTexture.SUNBURST -> drawSunburstTexture(
      w = w,
      h = h,
      maxDim = maxDim,
      primaryColor = primaryColor,
      tertiaryColor = tertiaryColor,
      baseAlpha = baseAlpha,
    )
    BackgroundTexture.HEXAGONS -> drawHexagonsTexture(
      w = w,
      h = h,
      minDim = minDim,
      primaryColor = primaryColor,
      tertiaryColor = tertiaryColor,
      baseAlpha = baseAlpha,
      isPreview = isPreview,
    )
    BackgroundTexture.BLUEPRINT -> drawBlueprintTexture(
      w = w,
      h = h,
      minDim = minDim,
      primaryColor = primaryColor,
      tertiaryColor = tertiaryColor,
      baseAlpha = baseAlpha,
      isPreview = isPreview,
    )
    BackgroundTexture.WAVES -> drawWavesTexture(
      w = w,
      h = h,
      primaryColor = primaryColor,
      tertiaryColor = tertiaryColor,
      secondaryColor = secondaryColor,
      baseAlpha = baseAlpha,
      isPreview = isPreview,
    )
    BackgroundTexture.CONFETTI -> drawConfettiTexture(
      w = w,
      h = h,
      minDim = minDim,
      primaryColor = primaryColor,
      tertiaryColor = tertiaryColor,
      secondaryColor = secondaryColor,
      baseAlpha = baseAlpha,
      isPreview = isPreview,
    )
    BackgroundTexture.CIRCUIT -> drawCircuitTexture(
      w = w,
      h = h,
      minDim = minDim,
      primaryColor = primaryColor,
      tertiaryColor = tertiaryColor,
      baseAlpha = baseAlpha,
      isPreview = isPreview,
    )
  }
}

private fun DrawScope.drawFuzzyTexture(
  w: Float,
  h: Float,
  maxDim: Float,
  primaryColor: Color,
  tertiaryColor: Color,
  secondaryColor: Color,
  onSurfaceColor: Color,
  baseAlpha: Float,
  isPreview: Boolean,
) {
  // Subtle ambient dual glow under the grain
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(
        primaryColor.copy(alpha = (baseAlpha * 0.7f).coerceIn(0f, 1f)),
        Color.Transparent,
      ),
      center = Offset(w * 0.22f, h * 0.18f),
      radius = maxDim * 0.65f,
    ),
    radius = maxDim * 0.65f,
    center = Offset(w * 0.22f, h * 0.18f),
  )
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(
        tertiaryColor.copy(alpha = (baseAlpha * 0.65f).coerceIn(0f, 1f)),
        Color.Transparent,
      ),
      center = Offset(w * 0.8f, h * 0.82f),
      radius = maxDim * 0.65f,
    ),
    radius = maxDim * 0.65f,
    center = Offset(w * 0.8f, h * 0.82f),
  )

  val particleCount = if (isPreview) 120 else 440
  val baseDotRadius = if (isPreview) 1.4f else 2.6f
  val baseFiberLen = if (isPreview) 4.5f else 9.5f

  for (i in 0 until particleCount) {
    val px = proceduralFloat(i * 4 + 1) * w
    val py = proceduralFloat(i * 4 + 2) * h
    val scale = 0.45f + proceduralFloat(i * 4 + 3) * 1.15f
    val colorChoice = when (i % 4) {
      0 -> primaryColor
      1 -> tertiaryColor
      2 -> secondaryColor
      else -> onSurfaceColor
    }
    val particleAlpha = (baseAlpha * (0.55f + (i % 5) * 0.16f)).coerceIn(0.02f, 0.8f)

    if (i % 3 == 0) {
      // Micro fuzzy fiber segment
      val angle = proceduralFloat(i * 4 + 4) * (PI.toFloat() * 2f)
      val halfLen = baseFiberLen * scale * 0.5f
      val dx = cos(angle) * halfLen
      val dy = sin(angle) * halfLen
      drawLine(
        color = colorChoice.copy(alpha = particleAlpha),
        start = Offset(px - dx, py - dy),
        end = Offset(px + dx, py + dy),
        strokeWidth = (baseDotRadius * 0.75f).coerceAtLeast(1f),
        cap = StrokeCap.Round,
      )
    } else {
      // Stipple grain dot
      drawCircle(
        color = colorChoice.copy(alpha = particleAlpha),
        radius = (baseDotRadius * scale).coerceAtLeast(0.8f),
        center = Offset(px, py),
      )
    }
  }
}

private fun DrawScope.drawFadeTexture(
  w: Float,
  h: Float,
  maxDim: Float,
  primaryColor: Color,
  tertiaryColor: Color,
  secondaryColor: Color,
  baseAlpha: Float,
) {
  val topAlpha = (baseAlpha * 1.55f).coerceIn(0f, 0.85f)
  val midAlpha = (baseAlpha * 0.85f).coerceIn(0f, 0.65f)
  val bottomAlpha = (baseAlpha * 1.55f).coerceIn(0f, 0.85f)

  drawRect(
    brush = Brush.linearGradient(
      colors = listOf(
        primaryColor.copy(alpha = topAlpha),
        secondaryColor.copy(alpha = midAlpha),
        tertiaryColor.copy(alpha = bottomAlpha),
      ),
      start = Offset(0f, 0f),
      end = Offset(w, h),
    ),
  )

  val bloomRadius = maxDim * 0.75f
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(
        primaryColor.copy(alpha = (baseAlpha * 1.15f).coerceIn(0f, 0.75f)),
        Color.Transparent,
      ),
      center = Offset(w * 0.14f, h * 0.12f),
      radius = bloomRadius,
    ),
    radius = bloomRadius,
    center = Offset(w * 0.14f, h * 0.12f),
  )
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(
        tertiaryColor.copy(alpha = (baseAlpha * 1.15f).coerceIn(0f, 0.75f)),
        Color.Transparent,
      ),
      center = Offset(w * 0.88f, h * 0.86f),
      radius = bloomRadius,
    ),
    radius = bloomRadius,
    center = Offset(w * 0.88f, h * 0.86f),
  )
}

private fun DrawScope.drawConcentricTexture(
  w: Float,
  h: Float,
  maxDim: Float,
  primaryColor: Color,
  tertiaryColor: Color,
  secondaryColor: Color,
  baseAlpha: Float,
  isPreview: Boolean,
) {
  val c1 = Offset(w * 0.78f, h * 0.22f)
  val c2 = Offset(w * 0.18f, h * 0.82f)
  val ringCount = if (isPreview) 7 else 14
  val step = (maxDim * 1.1f) / ringCount
  val baseStroke = if (isPreview) 1.4f else 2.2f

  // Soft core halos
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(
        primaryColor.copy(alpha = (baseAlpha * 0.85f).coerceIn(0f, 0.7f)),
        Color.Transparent,
      ),
      center = c1,
      radius = maxDim * 0.45f,
    ),
    radius = maxDim * 0.45f,
    center = c1,
  )
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(
        tertiaryColor.copy(alpha = (baseAlpha * 0.75f).coerceIn(0f, 0.7f)),
        Color.Transparent,
      ),
      center = c2,
      radius = maxDim * 0.42f,
    ),
    radius = maxDim * 0.42f,
    center = c2,
  )

  for (i in 1..ringCount) {
    val radius = i * step
    val falloff = 1f - (i - 1).toFloat() / (ringCount * 1.25f)
    val ringColor = if (i % 2 == 0) tertiaryColor else primaryColor
    val alpha = (baseAlpha * (0.75f + 0.45f * falloff)).coerceIn(0.02f, 0.85f)
    val strokeW = if (i % 3 == 0) baseStroke * 1.45f else baseStroke
    drawCircle(
      color = ringColor.copy(alpha = alpha),
      radius = radius,
      center = c1,
      style = Stroke(width = strokeW),
    )
  }

  val secondaryCount = if (isPreview) 5 else 10
  for (i in 1..secondaryCount) {
    val radius = i * (step * 1.15f)
    val ringColor = if (i % 2 == 0) secondaryColor else tertiaryColor
    val alpha = (baseAlpha * 0.72f).coerceIn(0.02f, 0.75f)
    drawCircle(
      color = ringColor.copy(alpha = alpha),
      radius = radius,
      center = c2,
      style = Stroke(width = baseStroke * 0.9f),
    )
  }
}

private fun DrawScope.drawHalftoneTexture(
  w: Float,
  h: Float,
  primaryColor: Color,
  tertiaryColor: Color,
  baseAlpha: Float,
  isPreview: Boolean,
) {
  val cols = if (isPreview) 9 else 16
  val spacing = (w / cols).coerceAtLeast(6f)
  val rows = (h / (spacing * 0.866f)).toInt() + 2

  for (r in 0..rows) {
    val y = r * spacing * 0.866f
    val rowOffset = if (r % 2 == 1) spacing * 0.5f else 0f
    for (c in 0..cols) {
      val x = c * spacing + rowOffset
      if (x < -spacing || x > w + spacing || y < -spacing || y > h + spacing) continue

      val wave = (sin(c * 0.48f + r * 0.36f) + cos(c * 0.25f - r * 0.42f)) * 0.25f + 0.5f
      val clampedWave = wave.coerceIn(0f, 1f)
      val dotRadius = spacing * (0.09f + 0.26f * clampedWave)
      val dotColor = lerp(primaryColor, tertiaryColor, clampedWave)
      val dotAlpha = (baseAlpha * (0.6f + 0.75f * clampedWave)).coerceIn(0.02f, 0.85f)

      drawCircle(
        color = dotColor.copy(alpha = dotAlpha),
        radius = dotRadius,
        center = Offset(x, y),
      )
    }
  }
}

private fun DrawScope.drawTopographyTexture(
  w: Float,
  h: Float,
  primaryColor: Color,
  tertiaryColor: Color,
  secondaryColor: Color,
  baseAlpha: Float,
  isPreview: Boolean,
) {
  val lineCount = if (isPreview) 7 else 15
  val segments = if (isPreview) 16 else 36
  val amp1 = h * 0.045f
  val amp2 = h * 0.025f
  val baseStroke = if (isPreview) 1.3f else 2.0f
  val path = Path()

  for (i in 0 until lineCount) {
    val fraction = (i + 0.5f) / lineCount.toFloat()
    val baseY = h * fraction
    val isIndexContour = i % 3 == 0
    val lineColor = when {
      isIndexContour -> primaryColor
      i % 2 == 0 -> tertiaryColor
      else -> secondaryColor
    }
    val lineAlpha = (baseAlpha * if (isIndexContour) 1.25f else 0.78f).coerceIn(0.02f, 0.85f)
    val strokeWidth = if (isIndexContour) baseStroke * 1.5f else baseStroke * 0.9f

    path.reset()
    for (s in 0..segments) {
      val t = s.toFloat() / segments.toFloat()
      val x = w * t
      val y = baseY +
        amp1 * sin(t * PI.toFloat() * 2.2f + i * 0.62f) +
        amp2 * cos(t * PI.toFloat() * 3.8f - i * 0.45f)
      if (s == 0) {
        path.moveTo(x, y)
      } else {
        path.lineTo(x, y)
      }
    }

    drawPath(
      path = path,
      color = lineColor.copy(alpha = lineAlpha),
      style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
  }
}

private fun DrawScope.drawAuroraTexture(
  w: Float,
  h: Float,
  primaryColor: Color,
  tertiaryColor: Color,
  secondaryColor: Color,
  baseAlpha: Float,
) {
  val ribbonColors = listOf(
    primaryColor to tertiaryColor,
    tertiaryColor to secondaryColor,
    secondaryColor to primaryColor,
  )
  val path = Path()
  val crestPath = Path()
  val steps = 24

  ribbonColors.forEachIndexed { index, (startColor, endColor) ->
    val centerY = h * (0.24f + index * 0.26f)
    val ribbonThickness = h * 0.22f
    val amp = h * (0.06f + index * 0.015f)
    val phase = index * 1.35f

    path.reset()
    crestPath.reset()
    for (s in 0..steps) {
      val t = s.toFloat() / steps.toFloat()
      val x = w * t
      val yTop = centerY + amp * sin(t * PI.toFloat() * 2.0f + phase) +
        (amp * 0.45f) * cos(t * PI.toFloat() * 3.2f - phase)
      if (s == 0) {
        path.moveTo(x, yTop)
        crestPath.moveTo(x, yTop)
      } else {
        path.lineTo(x, yTop)
        crestPath.lineTo(x, yTop)
      }
    }
    for (s in steps downTo 0) {
      val t = s.toFloat() / steps.toFloat()
      val x = w * t
      val yBottom = centerY + ribbonThickness +
        (amp * 0.7f) * sin(t * PI.toFloat() * 1.8f + phase + 0.6f)
      path.lineTo(x, yBottom)
    }
    path.close()

    drawPath(
      path = path,
      brush = Brush.verticalGradient(
        colors = listOf(
          startColor.copy(alpha = (baseAlpha * 1.15f).coerceIn(0f, 0.75f)),
          endColor.copy(alpha = (baseAlpha * 0.55f).coerceIn(0f, 0.5f)),
          Color.Transparent,
        ),
        startY = centerY - amp,
        endY = centerY + ribbonThickness + amp,
      ),
    )
    drawPath(
      path = crestPath,
      brush = Brush.horizontalGradient(
        colors = listOf(
          startColor.copy(alpha = (baseAlpha * 1.35f).coerceIn(0f, 0.85f)),
          endColor.copy(alpha = (baseAlpha * 1.35f).coerceIn(0f, 0.85f)),
        ),
      ),
      style = Stroke(width = 2.2f, cap = StrokeCap.Round),
    )
  }
}

private fun DrawScope.drawStarfieldTexture(
  w: Float,
  h: Float,
  maxDim: Float,
  primaryColor: Color,
  tertiaryColor: Color,
  onSurfaceColor: Color,
  baseAlpha: Float,
  isPreview: Boolean,
) {
  // Nebula radial clouds
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(
        primaryColor.copy(alpha = (baseAlpha * 0.95f).coerceIn(0f, 0.7f)),
        Color.Transparent,
      ),
      center = Offset(w * 0.28f, h * 0.3f),
      radius = maxDim * 0.6f,
    ),
    radius = maxDim * 0.6f,
    center = Offset(w * 0.28f, h * 0.3f),
  )
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(
        tertiaryColor.copy(alpha = (baseAlpha * 0.85f).coerceIn(0f, 0.7f)),
        Color.Transparent,
      ),
      center = Offset(w * 0.75f, h * 0.68f),
      radius = maxDim * 0.55f,
    ),
    radius = maxDim * 0.55f,
    center = Offset(w * 0.75f, h * 0.68f),
  )

  val starCount = if (isPreview) 26 else 88
  val baseStarSize = if (isPreview) 1.5f else 2.5f

  for (i in 0 until starCount) {
    val sx = proceduralFloat(i * 5 + 11) * w
    val sy = proceduralFloat(i * 5 + 12) * h
    val brightness = 0.5f + proceduralFloat(i * 5 + 13) * 0.8f
    val starColor = when {
      i % 5 == 0 -> primaryColor
      i % 3 == 0 -> tertiaryColor
      else -> onSurfaceColor
    }
    val starAlpha = (baseAlpha * (0.7f + brightness * 0.65f)).coerceIn(0.04f, 0.9f)

    if (i % 6 == 0) {
      // Twinkling 4-point sparkle star
      val arm = baseStarSize * (if (isPreview) 2.6f else 3.4f) * brightness
      drawCircle(
        color = starColor.copy(alpha = (starAlpha * 0.45f).coerceIn(0f, 0.6f)),
        radius = arm * 0.75f,
        center = Offset(sx, sy),
      )
      drawLine(
        color = starColor.copy(alpha = starAlpha),
        start = Offset(sx - arm, sy),
        end = Offset(sx + arm, sy),
        strokeWidth = 1.5f,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = starColor.copy(alpha = starAlpha),
        start = Offset(sx, sy - arm),
        end = Offset(sx, sy + arm),
        strokeWidth = 1.5f,
        cap = StrokeCap.Round,
      )
    } else {
      drawCircle(
        color = starColor.copy(alpha = starAlpha),
        radius = (baseStarSize * brightness).coerceAtLeast(1f),
        center = Offset(sx, sy),
      )
    }
  }
}

private fun DrawScope.drawSunburstTexture(
  w: Float,
  h: Float,
  maxDim: Float,
  primaryColor: Color,
  tertiaryColor: Color,
  baseAlpha: Float,
) {
  val origin = Offset(w * 0.82f, h * 0.16f)
  val rayCount = 18
  val reach = maxDim * 1.45f
  val angleStep = (PI * 2.0) / rayCount
  val rayPath = Path()

  for (i in 0 until rayCount step 2) {
    val a1 = i * angleStep
    val a2 = a1 + angleStep * 0.55
    val p1 = Offset(
      x = origin.x + (cos(a1) * reach).toFloat(),
      y = origin.y + (sin(a1) * reach).toFloat(),
    )
    val p2 = Offset(
      x = origin.x + (cos(a2) * reach).toFloat(),
      y = origin.y + (sin(a2) * reach).toFloat(),
    )
    rayPath.reset()
    rayPath.moveTo(origin.x, origin.y)
    rayPath.lineTo(p1.x, p1.y)
    rayPath.lineTo(p2.x, p2.y)
    rayPath.close()

    val rayColor = if ((i / 2) % 2 == 0) primaryColor else tertiaryColor
    drawPath(
      path = rayPath,
      brush = Brush.radialGradient(
        colors = listOf(
          rayColor.copy(alpha = (baseAlpha * 1.15f).coerceIn(0f, 0.78f)),
          rayColor.copy(alpha = (baseAlpha * 0.45f).coerceIn(0f, 0.45f)),
          Color.Transparent,
        ),
        center = origin,
        radius = reach * 0.9f,
      ),
    )
  }

  // Sun core & halo
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(
        tertiaryColor.copy(alpha = (baseAlpha * 1.4f).coerceIn(0f, 0.85f)),
        primaryColor.copy(alpha = (baseAlpha * 0.6f).coerceIn(0f, 0.5f)),
        Color.Transparent,
      ),
      center = origin,
      radius = maxDim * 0.28f,
    ),
    radius = maxDim * 0.28f,
    center = origin,
  )
}

private fun DrawScope.drawHexagonsTexture(
  w: Float,
  h: Float,
  minDim: Float,
  primaryColor: Color,
  tertiaryColor: Color,
  baseAlpha: Float,
  isPreview: Boolean,
) {
  val hexRadius = (if (isPreview) minDim * 0.24f else minDim * 0.085f).coerceAtLeast(10f)
  val horizStep = hexRadius * 1.5f
  val vertStep = hexRadius * sqrt(3f)
  val cols = (w / horizStep).toInt() + 2
  val rows = (h / vertStep).toInt() + 2
  val strokeW = if (isPreview) 1.2f else 1.6f
  val hexPath = Path()

  for (col in -1..cols) {
    val cx = col * horizStep
    val yOffset = if (col % 2 != 0) vertStep * 0.5f else 0f
    for (row in -1..rows) {
      val cy = row * vertStep + yOffset
      if (cx < -hexRadius * 2 || cx > w + hexRadius * 2 || cy < -hexRadius * 2 || cy > h + hexRadius * 2) {
        continue
      }

      hexPath.reset()
      for (v in 0 until 6) {
        val angleRad = (PI / 3.0) * v
        val vx = cx + (hexRadius * cos(angleRad)).toFloat()
        val vy = cy + (hexRadius * sin(angleRad)).toFloat()
        if (v == 0) hexPath.moveTo(vx, vy) else hexPath.lineTo(vx, vy)
      }
      hexPath.close()

      val cellHash = proceduralHash(col * 73 + row * 151)
      val cellColor = if ((col + row) % 2 == 0) primaryColor else tertiaryColor

      if (cellHash % 5 == 0) {
        drawPath(
          path = hexPath,
          color = cellColor.copy(alpha = (baseAlpha * 0.48f).coerceIn(0.01f, 0.5f)),
        )
      }
      drawPath(
        path = hexPath,
        color = cellColor.copy(alpha = (baseAlpha * 0.85f).coerceIn(0.02f, 0.8f)),
        style = Stroke(width = strokeW),
      )
    }
  }
}

private fun DrawScope.drawBlueprintTexture(
  w: Float,
  h: Float,
  minDim: Float,
  primaryColor: Color,
  tertiaryColor: Color,
  baseAlpha: Float,
  isPreview: Boolean,
) {
  val cell = (if (isPreview) minDim * 0.2f else minDim * 0.055f).coerceAtLeast(8f)
  val cols = (w / cell).toInt() + 1
  val rows = (h / cell).toInt() + 1
  val minorAlpha = (baseAlpha * 0.45f).coerceIn(0.01f, 0.5f)
  val majorAlpha = (baseAlpha * 0.95f).coerceIn(0.02f, 0.85f)
  val crossAlpha = (baseAlpha * 1.25f).coerceIn(0.04f, 0.9f)

  for (c in 0..cols) {
    val x = c * cell
    val isMajor = c % 4 == 0
    drawLine(
      color = primaryColor.copy(alpha = if (isMajor) majorAlpha else minorAlpha),
      start = Offset(x, 0f),
      end = Offset(x, h),
      strokeWidth = if (isMajor) 1.6f else 1f,
    )
  }

  for (r in 0..rows) {
    val y = r * cell
    val isMajor = r % 4 == 0
    drawLine(
      color = primaryColor.copy(alpha = if (isMajor) majorAlpha else minorAlpha),
      start = Offset(0f, y),
      end = Offset(w, y),
      strokeWidth = if (isMajor) 1.6f else 1f,
    )
  }

  val tick = (cell * 0.22f).coerceAtLeast(2.5f)
  for (c in 0..cols step 4) {
    val x = c * cell
    for (r in 0..rows step 4) {
      val y = r * cell
      drawLine(
        color = tertiaryColor.copy(alpha = crossAlpha),
        start = Offset(x - tick, y),
        end = Offset(x + tick, y),
        strokeWidth = 1.6f,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = tertiaryColor.copy(alpha = crossAlpha),
        start = Offset(x, y - tick),
        end = Offset(x, y + tick),
        strokeWidth = 1.6f,
        cap = StrokeCap.Round,
      )
    }
  }
}

private fun DrawScope.drawWavesTexture(
  w: Float,
  h: Float,
  primaryColor: Color,
  tertiaryColor: Color,
  secondaryColor: Color,
  baseAlpha: Float,
  isPreview: Boolean,
) {
  val waveCount = if (isPreview) 4 else 6
  val steps = if (isPreview) 18 else 36
  val fillPath = Path()
  val crestPath = Path()

  for (i in 0 until waveCount) {
    val fraction = (i + 1).toFloat() / (waveCount + 1).toFloat()
    val baseY = h * fraction
    val amp = h * (0.032f + (i % 3) * 0.012f)
    val freq = 1.6f + (i % 2) * 0.6f
    val phase = i * 1.1f
    val waveColor = when (i % 3) {
      0 -> primaryColor
      1 -> tertiaryColor
      else -> secondaryColor
    }

    fillPath.reset()
    crestPath.reset()
    for (s in 0..steps) {
      val t = s.toFloat() / steps.toFloat()
      val x = w * t
      val y = baseY + amp * sin(t * PI.toFloat() * 2f * freq + phase)
      if (s == 0) {
        fillPath.moveTo(x, y)
        crestPath.moveTo(x, y)
      } else {
        fillPath.lineTo(x, y)
        crestPath.lineTo(x, y)
      }
    }
    fillPath.lineTo(w, baseY + amp * 3.2f)
    fillPath.lineTo(0f, baseY + amp * 3.2f)
    fillPath.close()

    drawPath(
      path = fillPath,
      brush = Brush.verticalGradient(
        colors = listOf(
          waveColor.copy(alpha = (baseAlpha * 0.65f).coerceIn(0f, 0.55f)),
          Color.Transparent,
        ),
        startY = baseY - amp,
        endY = baseY + amp * 3.2f,
      ),
    )
    drawPath(
      path = crestPath,
      color = waveColor.copy(alpha = (baseAlpha * 1.15f).coerceIn(0.02f, 0.85f)),
      style = Stroke(width = if (isPreview) 1.5f else 2.2f, cap = StrokeCap.Round),
    )
  }
}

private fun DrawScope.drawConfettiTexture(
  w: Float,
  h: Float,
  minDim: Float,
  primaryColor: Color,
  tertiaryColor: Color,
  secondaryColor: Color,
  baseAlpha: Float,
  isPreview: Boolean,
) {
  val count = if (isPreview) 20 else 58
  val baseSize = (if (isPreview) minDim * 0.09f else minDim * 0.028f).coerceAtLeast(3.5f)
  val shapePath = Path()

  for (i in 0 until count) {
    val cx = proceduralFloat(i * 6 + 21) * w
    val cy = proceduralFloat(i * 6 + 22) * h
    val scale = 0.65f + proceduralFloat(i * 6 + 23) * 0.9f
    val angle = proceduralFloat(i * 6 + 24) * (PI.toFloat() * 2f)
    val color = when (i % 3) {
      0 -> primaryColor
      1 -> tertiaryColor
      else -> secondaryColor
    }
    val alpha = (baseAlpha * (0.75f + (i % 4) * 0.15f)).coerceIn(0.03f, 0.85f)
    val r = baseSize * scale

    when (i % 4) {
      0 -> {
        // Filled circle
        drawCircle(
          color = color.copy(alpha = alpha),
          radius = r * 0.7f,
          center = Offset(cx, cy),
        )
      }
      1 -> {
        // Hollow ring
        drawCircle(
          color = color.copy(alpha = alpha),
          radius = r * 0.85f,
          center = Offset(cx, cy),
          style = Stroke(width = (r * 0.32f).coerceAtLeast(1.4f)),
        )
      }
      2 -> {
        // Tilted capsule bar
        val dx = cos(angle) * r * 1.1f
        val dy = sin(angle) * r * 1.1f
        drawLine(
          color = color.copy(alpha = alpha),
          start = Offset(cx - dx, cy - dy),
          end = Offset(cx + dx, cy + dy),
          strokeWidth = (r * 0.55f).coerceAtLeast(2f),
          cap = StrokeCap.Round,
        )
      }
      else -> {
        // Playful triangle
        shapePath.reset()
        for (v in 0 until 3) {
          val a = angle + v * (PI.toFloat() * 2f / 3f)
          val vx = cx + cos(a) * r
          val vy = cy + sin(a) * r
          if (v == 0) shapePath.moveTo(vx, vy) else shapePath.lineTo(vx, vy)
        }
        shapePath.close()
        drawPath(
          path = shapePath,
          color = color.copy(alpha = alpha),
        )
      }
    }
  }
}

private fun DrawScope.drawCircuitTexture(
  w: Float,
  h: Float,
  minDim: Float,
  primaryColor: Color,
  tertiaryColor: Color,
  baseAlpha: Float,
  isPreview: Boolean,
) {
  val gridStep = (if (isPreview) minDim * 0.16f else minDim * 0.055f).coerceAtLeast(8f)
  val cols = max(3, (w / gridStep).toInt())
  val rows = max(3, (h / gridStep).toInt())
  val traceCount = if (isPreview) 10 else 28
  val strokeW = if (isPreview) 1.4f else 2.0f
  val padRadius = strokeW * 1.85f
  val tracePath = Path()

  for (i in 0 until traceCount) {
    val c0 = proceduralHash(i * 7 + 31) % cols
    val r0 = proceduralHash(i * 7 + 32) % rows
    val dirX = if (proceduralHash(i * 7 + 33) % 2 == 0) 1 else -1
    val dirY = if (proceduralHash(i * 7 + 34) % 2 == 0) 1 else -1
    val run1 = 1 + (proceduralHash(i * 7 + 35) % 3)
    val diag = 1 + (proceduralHash(i * 7 + 36) % 2)
    val run2 = 1 + (proceduralHash(i * 7 + 37) % 3)
    val horizontalFirst = i % 2 == 0

    val x0 = c0 * gridStep
    val y0 = r0 * gridStep
    val x1 = if (horizontalFirst) x0 + dirX * run1 * gridStep else x0
    val y1 = if (horizontalFirst) y0 else y0 + dirY * run1 * gridStep
    val x2 = x1 + dirX * diag * gridStep
    val y2 = y1 + dirY * diag * gridStep
    val x3 = if (horizontalFirst) x2 + dirX * run2 * gridStep else x2
    val y3 = if (horizontalFirst) y2 else y2 + dirY * run2 * gridStep

    val traceColor = if (i % 3 == 0) tertiaryColor else primaryColor
    val alpha = (baseAlpha * (0.8f + (i % 3) * 0.15f)).coerceIn(0.03f, 0.85f)

    tracePath.reset()
    tracePath.moveTo(x0, y0)
    tracePath.lineTo(x1, y1)
    tracePath.lineTo(x2, y2)
    tracePath.lineTo(x3, y3)

    drawPath(
      path = tracePath,
      color = traceColor.copy(alpha = alpha),
      style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )

    // Solder via pads at start and end nodes
    drawCircle(
      color = traceColor.copy(alpha = alpha),
      radius = padRadius,
      center = Offset(x0, y0),
      style = Stroke(width = strokeW * 0.85f),
    )
    drawCircle(
      color = traceColor.copy(alpha = alpha),
      radius = padRadius * 0.85f,
      center = Offset(x3, y3),
    )
  }
}
