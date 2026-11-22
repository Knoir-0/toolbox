package com.example.toolbox.ui.tools

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.os.Bundle
import android.widget.RemoteViews
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.example.toolbox.R
import com.example.toolbox.ui.main.BackgroundTexture
import com.example.toolbox.ui.main.DEFAULT_HEXAGON_ICON_BUTTONS
import com.example.toolbox.ui.main.DefaultNavigationPreferencesRepository
import com.example.toolbox.ui.main.NAVIGATION_PREFERENCES_NAME
import com.example.toolbox.ui.main.NavigationPreferencesBridge
import com.example.toolbox.ui.main.NavigationPreferencesRepository
import com.example.toolbox.ui.main.PREF_KEY_HEXAGON_ICON_BUTTONS
import com.example.toolbox.ui.theme.drawBackgroundTexture
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Snapshot of the Hexagonify setting, active background texture, widget dimensions, and Material 3
 * palette rendered by the Easter Egg Hexagonify Home Screen AppWidget ([HexagonifyWidgetProvider]).
 */
@Immutable
data class HexagonifyWidgetState(
  val isHexagon: Boolean,
  val backgroundTexture: BackgroundTexture,
  val widthDp: Float,
  val heightDp: Float,
  val palette: TodoWidgetPalette,
  val hexagonIconButtons: Boolean = isHexagon,
)

/**
 * Central state resolver, icon-free textured shape renderer, and RemoteViews builder for the
 * Easter Egg Hexagonify Home Screen AppWidget ([HexagonifyWidgetProvider]):
 * - Renders as a floating **Circle** when **Hexagonify** is off (`isHexagon == false`) and morphs
 *   into a floating vertical-sided **Hexagon** when **Hexagonify** is on (`isHexagon == true`).
 * - Features zero icons or text — instead, it renders a jewel-like surface that follows the user's
 *   active procedural **Background Texture** ([BackgroundTexture]) clipped to the circle or
 *   hexagon, layered with soft Material 3 tonal sheen, a subtle inner specular contour, and an
 *   outer border outline matching the other Toolbox widgets.
 * - Tapping anywhere on the widget toggles the app-wide **Hexagonify** setting, flips the widget
 *   between a circle and a hexagon, and immediately refreshes all pinned Toolbox widgets.
 */
object HexagonifyWidgets {

  const val ACTION_WIDGET_REFRESH = "com.example.toolbox.hexagonify.ACTION_WIDGET_REFRESH"
  const val ACTION_WIDGET_TOGGLE_HEXAGONIFY = "com.example.toolbox.hexagonify.ACTION_WIDGET_TOGGLE_HEXAGONIFY"

  const val DEFAULT_WIDGET_WIDTH_DP = 84f
  const val DEFAULT_WIDGET_HEIGHT_DP = 84f

  const val HEXAGON_CORNER_RADIUS_FRACTION = 0.08f

  private const val KEY_WIDGET_WIDTH_PREFIX = "hexagonify_widget_width_"
  private const val KEY_WIDGET_HEIGHT_PREFIX = "hexagonify_widget_height_"

  private const val REQUEST_CODE_TOGGLE_BASE = 9400

  private val inMemoryDimensions = mutableMapOf<Int, Pair<Float, Float>>()

  /**
   * Persists the latest resolved `(widthDp, heightDp)` for [appWidgetId].
   */
  fun saveWidgetDimensions(
    context: Context?,
    appWidgetId: Int,
    widthDp: Float,
    heightDp: Float,
  ) {
    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return
    val sanitizedW = widthDp.coerceAtLeast(48f)
    val sanitizedH = heightDp.coerceAtLeast(48f)
    synchronized(inMemoryDimensions) {
      inMemoryDimensions[appWidgetId] = sanitizedW to sanitizedH
    }
    if (context == null) return
    try {
      val prefs = context.getSharedPreferences(NAVIGATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
      prefs.edit()
        .putFloat("$KEY_WIDGET_WIDTH_PREFIX$appWidgetId", sanitizedW)
        .putFloat("$KEY_WIDGET_HEIGHT_PREFIX$appWidgetId", sanitizedH)
        .apply()
    } catch (_: Throwable) {}
  }

  /**
   * Loads the saved `(widthDp, heightDp)` for [appWidgetId], or `null` if none has been saved yet.
   */
  fun loadWidgetDimensions(
    context: Context?,
    appWidgetId: Int,
  ): Pair<Float, Float>? {
    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return null
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(NAVIGATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val wKey = "$KEY_WIDGET_WIDTH_PREFIX$appWidgetId"
        val hKey = "$KEY_WIDGET_HEIGHT_PREFIX$appWidgetId"
        if (prefs.contains(wKey) && prefs.contains(hKey)) {
          val w = prefs.getFloat(wKey, DEFAULT_WIDGET_WIDTH_DP)
          val h = prefs.getFloat(hKey, DEFAULT_WIDGET_HEIGHT_DP)
          if (w > 0f && h > 0f) {
            synchronized(inMemoryDimensions) {
              inMemoryDimensions[appWidgetId] = w to h
            }
            return w to h
          }
        }
      } catch (_: Throwable) {}
    }
    return synchronized(inMemoryDimensions) {
      inMemoryDimensions[appWidgetId]
    }
  }

  /**
   * Deletes stored widget dimensions when [appWidgetId] is removed from the home screen.
   */
  fun deleteWidgetDimensions(
    context: Context?,
    appWidgetId: Int,
  ) {
    synchronized(inMemoryDimensions) {
      inMemoryDimensions.remove(appWidgetId)
    }
    if (context == null) return
    try {
      val prefs = context.getSharedPreferences(NAVIGATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
      prefs.edit()
        .remove("$KEY_WIDGET_WIDTH_PREFIX$appWidgetId")
        .remove("$KEY_WIDGET_HEIGHT_PREFIX$appWidgetId")
        .apply()
    } catch (_: Throwable) {}
  }

  fun clearInMemoryStateForTesting() {
    synchronized(inMemoryDimensions) {
      inMemoryDimensions.clear()
    }
  }

  /**
   * Loads whether **Hexagonify** (`hexagonIconButtons`) is currently enabled.
   */
  fun loadHexagonifyEnabled(
    context: Context? = null,
    repository: NavigationPreferencesRepository? = null,
  ): Boolean {
    if (repository != null) {
      return repository.getHexagonIconButtons()
    }
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(NAVIGATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (prefs != null && prefs.contains(PREF_KEY_HEXAGON_ICON_BUTTONS)) {
          return prefs.getBoolean(PREF_KEY_HEXAGON_ICON_BUTTONS, DEFAULT_HEXAGON_ICON_BUTTONS)
        }
      } catch (_: Throwable) {}
    }
    return NavigationPreferencesBridge.activeRepository?.getHexagonIconButtons()
      ?: NavigationPreferencesBridge.hexagonIconButtons
  }

  /**
   * Sets **Hexagonify** (`hexagonIconButtons`) to [enabled] and refreshes all pinned Toolbox
   * Home Screen AppWidgets.
   */
  fun setHexagonifyEnabled(
    context: Context?,
    enabled: Boolean,
    repository: NavigationPreferencesRepository? = null,
    refreshWidgets: Boolean = true,
  ): Boolean {
    NavigationPreferencesBridge.hexagonIconButtons = enabled
    if (repository != null) {
      repository.setHexagonIconButtons(enabled)
    } else {
      NavigationPreferencesBridge.activeRepository?.setHexagonIconButtons(enabled)
    }
    if (context != null) {
      try {
        DefaultNavigationPreferencesRepository(context).setHexagonIconButtons(enabled)
      } catch (_: Throwable) {
        try {
          val prefs = context.getSharedPreferences(NAVIGATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
          prefs.edit().putBoolean(PREF_KEY_HEXAGON_ICON_BUTTONS, enabled).apply()
        } catch (_: Throwable) {}
      }
      if (refreshWidgets) {
        refreshAllToolboxWidgets(context)
      }
    }
    return enabled
  }

  /**
   * Toggles **Hexagonify** (`hexagonIconButtons`) between `false` (Circle) and `true` (Hexagon),
   * persists the new setting, refreshes all pinned Toolbox widgets, and returns the new state.
   */
  fun toggleHexagonify(
    context: Context?,
    repository: NavigationPreferencesRepository? = null,
    refreshWidgets: Boolean = true,
  ): Boolean {
    val current = loadHexagonifyEnabled(context = context, repository = repository)
    val next = !current
    return setHexagonifyEnabled(
      context = context,
      enabled = next,
      repository = repository,
      refreshWidgets = refreshWidgets,
    )
  }

  private fun refreshAllToolboxWidgets(context: Context) {
    updateAllWidgets(context)
    MusicPlayerWidgets.updateAllWidgets(context)
    TodoListWidgets.updateAllWidgets(context)
    NotesWidgets.updateAllWidgets(context)
    AudioRecorderWidgets.updateAllWidgets(context)
    PlantScheduleWidgets.updateAllWidgets(context)
    CameraWidgets.updateAllWidgets(context)
    DiceRollerWidgets.updateAllWidgets(context)
    BackgroundTextureWidgets.updateAllWidgets(context)
  }

  /**
   * Resolves the current [HexagonifyWidgetState] for [appWidgetId] and [options].
   */
  fun resolveWidgetState(
    context: Context? = null,
    appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
    options: Bundle? = null,
    repository: NavigationPreferencesRepository? = null,
    isHexagonOverride: Boolean? = null,
    activeTextureOverride: BackgroundTexture? = null,
  ): HexagonifyWidgetState {
    val savedDims = loadWidgetDimensions(context, appWidgetId)
    val fallbackW = savedDims?.first ?: DEFAULT_WIDGET_WIDTH_DP
    val fallbackH = savedDims?.second ?: DEFAULT_WIDGET_HEIGHT_DP

    val effectiveOptions = options ?: if (
      context != null &&
      appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
    ) {
      try {
        AppWidgetManager.getInstance(context)?.getAppWidgetOptions(appWidgetId)
      } catch (_: Throwable) {
        null
      }
    } else {
      null
    }

    val (resolvedWidthDp, resolvedHeightDp) = if (context != null) {
      MusicPlayerWidgets.resolveWidgetSizeDp(
        context = context,
        options = effectiveOptions,
        defaultWidthDp = fallbackW,
        defaultHeightDp = fallbackH,
      )
    } else if (effectiveOptions != null) {
      val minW = effectiveOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
      val maxH = effectiveOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0)
      val minH = effectiveOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
      val w = if (minW > 0) minW.toFloat() else fallbackW
      val h = when {
        maxH > 0 -> maxH.toFloat()
        minH > 0 -> minH.toFloat()
        else -> fallbackH
      }
      w to h
    } else {
      fallbackW to fallbackH
    }

    val widthDp = resolvedWidthDp.coerceAtLeast(48f)
    val heightDp = resolvedHeightDp.coerceAtLeast(48f)
    if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && effectiveOptions != null) {
      saveWidgetDimensions(context, appWidgetId, widthDp, heightDp)
    }

    val isHexagon = isHexagonOverride ?: loadHexagonifyEnabled(context, repository)
    val activeTexture = activeTextureOverride
      ?: repository?.getBackgroundTexture()
      ?: BackgroundTextureWidgets.loadActiveBackgroundTexture(context)
    val basePalette = TodoListWidgets.resolveWidgetPalette(context)
    val palette = basePalette.copy(
      hexagonIconButtons = isHexagon,
      backgroundTexture = activeTexture,
      iconButtonBackgroundRes = MusicPlayerWidgets.resolveIconButtonBackgroundRes(isHexagon),
    )

    return HexagonifyWidgetState(
      isHexagon = isHexagon,
      backgroundTexture = activeTexture,
      widthDp = widthDp,
      heightDp = heightDp,
      palette = palette,
      hexagonIconButtons = isHexagon,
    )
  }

  /**
   * Computes the 6 clockwise vertices of a vertical-sided hexagon centered at `(cx, cy)` with
   * circumradius [radius], where the left and right edges are strictly vertical (`x_topRight == x_bottomRight`
   * and `x_topLeft == x_bottomLeft`).
   */
  fun createHexagonVertices(
    cx: Float,
    cy: Float,
    radius: Float,
  ): List<Offset> {
    val r = radius.coerceAtLeast(1f)
    val halfWidth = r * (sqrt(3f) * 0.5f)
    val halfVerticalEdge = r * 0.5f
    return listOf(
      Offset(cx, cy - r),
      Offset(cx + halfWidth, cy - halfVerticalEdge),
      Offset(cx + halfWidth, cy + halfVerticalEdge),
      Offset(cx, cy + r),
      Offset(cx - halfWidth, cy + halfVerticalEdge),
      Offset(cx - halfWidth, cy - halfVerticalEdge),
    )
  }

  /**
   * Builds an [android.graphics.Path] for either a circle (`isHexagon == false`) or a
   * vertical-sided hexagon (`isHexagon == true`) centered at `(cx, cy)` with radius [radius].
   */
  fun buildAndroidShapePath(
    cx: Float,
    cy: Float,
    radius: Float,
    isHexagon: Boolean,
    cornerRadiusFraction: Float = HEXAGON_CORNER_RADIUS_FRACTION,
  ): Path {
    val path = Path()
    val r = radius.coerceAtLeast(1f)
    if (!isHexagon) {
      path.addCircle(cx, cy, r, Path.Direction.CW)
      return path
    }

    val vertices = createHexagonVertices(cx, cy, r)
    val cornerRadius = (r * 2f * cornerRadiusFraction).coerceAtLeast(0f)
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
      path.quadTo(curr.x, curr.y, endX, endY)
    }
    path.close()
    return path
  }

  /**
   * Builds a Compose [androidx.compose.ui.graphics.Path] for either a circle (`isHexagon == false`)
   * or a vertical-sided hexagon (`isHexagon == true`) centered at `(cx, cy)` with radius [radius].
   */
  fun buildComposeShapePath(
    cx: Float,
    cy: Float,
    radius: Float,
    isHexagon: Boolean,
    cornerRadiusFraction: Float = HEXAGON_CORNER_RADIUS_FRACTION,
  ): androidx.compose.ui.graphics.Path {
    val path = androidx.compose.ui.graphics.Path()
    val r = radius.coerceAtLeast(1f)
    if (!isHexagon) {
      path.addOval(Rect(cx - r, cy - r, cx + r, cy + r))
      return path
    }

    val vertices = createHexagonVertices(cx, cy, r)
    val cornerRadius = (r * 2f * cornerRadiusFraction).coerceAtLeast(0f)
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

  private fun blendArgb(baseArgb: Int, overlayArgb: Int, overlayFraction: Float): Int {
    val t = overlayFraction.coerceIn(0f, 1f)
    val inv = 1f - t
    val baseR = (baseArgb ushr 16) and 0xFF
    val baseG = (baseArgb ushr 8) and 0xFF
    val baseB = baseArgb and 0xFF
    val overR = (overlayArgb ushr 16) and 0xFF
    val overG = (overlayArgb ushr 8) and 0xFF
    val overB = overlayArgb and 0xFF
    val r = (baseR * inv + overR * t).roundToInt().coerceIn(0, 255)
    val g = (baseG * inv + overG * t).roundToInt().coerceIn(0, 255)
    val b = (baseB * inv + overB * t).roundToInt().coerceIn(0, 255)
    return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
  }

  private fun withAlpha(argb: Int, alphaFraction: Float): Int {
    val a = (alphaFraction.coerceIn(0f, 1f) * 255f).roundToInt().coerceIn(0, 255)
    return (argb and 0x00FFFFFF) or (a shl 24)
  }

  /**
   * Renders the icon-free Easter Egg widget [Bitmap] shaped as a **Circle** (`isHexagon == false`)
   * or a vertical-sided **Hexagon** (`isHexagon == true`):
   * - Completely transparent outside the shape so the widget floats on the home screen as a pure
   *   circle or hexagon.
   * - Filled with the user's Material 3 surface color plus a soft multi-stop radial ambient sheen.
   * - Layered with the user's active procedural [BackgroundTexture] (`palette.backgroundTexture`)
   *   strictly clipped to the circle or hexagon boundary.
   * - Finished with a delicate inner specular rim, subtle concentric geometric echo contours, and
   *   an outer border outline inset by `max(borderWidthPx * 1.25f, 1.5f)` matching the other
   *   Toolbox widgets.
   */
  fun renderHexagonifyShapeBitmap(
    isHexagon: Boolean,
    palette: TodoWidgetPalette,
    widthPx: Int = 240,
    heightPx: Int = 240,
    borderWidthPx: Float = 2f,
  ): Bitmap? {
    val safeW = widthPx.coerceIn(64, 1000)
    val safeH = heightPx.coerceIn(64, 1000)
    return try {
      val bitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      val w = safeW.toFloat()
      val h = safeH.toFloat()
      val cx = w * 0.5f
      val cy = h * 0.5f

      val effectiveBorderWidth = max(1f, borderWidthPx)
      val strokeInset = max(effectiveBorderWidth * 1.25f, 1.5f)
      val outerRadius = (min(w, h) * 0.5f - strokeInset).coerceAtLeast(10f)

      val outerAndroidPath = buildAndroidShapePath(
        cx = cx,
        cy = cy,
        radius = outerRadius,
        isHexagon = isHexagon,
      )

      // 1. Base surface fill
      val baseSurfaceArgb = if (isHexagon) {
        blendArgb(palette.surfaceArgb, palette.primaryContainerArgb, 0.14f)
      } else {
        palette.surfaceArgb
      }
      val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = baseSurfaceArgb
      }
      canvas.drawPath(outerAndroidPath, fillPaint)

      // 2. Soft Material 3 tonal sheen & ambient radial glow clipped inside the shape
      canvas.save()
      canvas.clipPath(outerAndroidPath)

      val topLeftGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        shader = RadialGradient(
          cx - outerRadius * 0.28f,
          cy - outerRadius * 0.32f,
          outerRadius * 1.35f,
          intArrayOf(
            withAlpha(palette.primaryContainerArgb, if (isHexagon) 0.44f else 0.30f),
            withAlpha(palette.primaryArgb, if (isHexagon) 0.16f else 0.10f),
            withAlpha(palette.surfaceArgb, 0f),
          ),
          floatArrayOf(0f, 0.52f, 1f),
          Shader.TileMode.CLAMP,
        )
      }
      canvas.drawRect(0f, 0f, w, h, topLeftGlowPaint)

      val bottomRightGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        shader = RadialGradient(
          cx + outerRadius * 0.32f,
          cy + outerRadius * 0.34f,
          outerRadius * 1.25f,
          intArrayOf(
            withAlpha(palette.tertiaryArgb, if (isHexagon) 0.24f else 0.16f),
            withAlpha(palette.secondaryArgb, 0.08f),
            withAlpha(palette.surfaceArgb, 0f),
          ),
          floatArrayOf(0f, 0.55f, 1f),
          Shader.TileMode.CLAMP,
        )
      }
      canvas.drawRect(0f, 0f, w, h, bottomRightGlowPaint)

      canvas.restore()

      // 3. Procedural Background Texture strictly clipped to the Circle or Hexagon
      if (palette.backgroundTexture != BackgroundTexture.NONE) {
        try {
          val composeCanvas = androidx.compose.ui.graphics.Canvas(bitmap.asImageBitmap())
          val drawScope = CanvasDrawScope()
          val composeClipPath = buildComposeShapePath(
            cx = cx,
            cy = cy,
            radius = outerRadius,
            isHexagon = isHexagon,
          )
          drawScope.draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = composeCanvas,
            size = Size(w, h),
          ) {
            clipPath(composeClipPath) {
              drawBackgroundTexture(
                texture = palette.backgroundTexture,
                primaryColor = Color(palette.primaryArgb),
                tertiaryColor = Color(palette.tertiaryArgb),
                secondaryColor = Color(palette.secondaryArgb),
                onSurfaceColor = Color(palette.onSurfaceArgb),
                isDarkTheme = palette.isDarkTheme,
                isPreview = min(w, h) <= 320f,
              )
            }
          }
        } catch (_: Throwable) {}
      }

      // 4. Subtle concentric inner geometric contours (no icons!) for jewel-like visual depth
      val echoStrokeWidth = max(1f, effectiveBorderWidth * 0.65f)
      val outerEchoRadius = outerRadius * 0.68f
      val innerEchoRadius = outerRadius * 0.38f
      if (outerEchoRadius > 8f) {
        val outerEchoPath = buildAndroidShapePath(
          cx = cx,
          cy = cy,
          radius = outerEchoRadius,
          isHexagon = isHexagon,
        )
        val outerEchoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.STROKE
          strokeWidth = echoStrokeWidth
          strokeJoin = Paint.Join.ROUND
          strokeCap = Paint.Cap.ROUND
          shader = LinearGradient(
            cx - outerEchoRadius,
            cy - outerEchoRadius,
            cx + outerEchoRadius,
            cy + outerEchoRadius,
            withAlpha(palette.primaryArgb, if (isHexagon) 0.24f else 0.16f),
            withAlpha(palette.tertiaryArgb, if (isHexagon) 0.18f else 0.11f),
            Shader.TileMode.CLAMP,
          )
        }
        canvas.drawPath(outerEchoPath, outerEchoPaint)
      }
      if (innerEchoRadius > 5f) {
        val innerEchoPath = buildAndroidShapePath(
          cx = cx,
          cy = cy,
          radius = innerEchoRadius,
          isHexagon = isHexagon,
        )
        val innerEchoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.STROKE
          strokeWidth = max(0.8f, echoStrokeWidth * 0.85f)
          strokeJoin = Paint.Join.ROUND
          strokeCap = Paint.Cap.ROUND
          color = withAlpha(
            if (isHexagon) palette.primaryArgb else palette.onSurfaceVariantArgb,
            if (isHexagon) 0.18f else 0.11f,
          )
        }
        canvas.drawPath(innerEchoPath, innerEchoPaint)
      }

      // 5. Delicate inner specular rim highlight just inside the outer border
      val innerRimRadius = (outerRadius - effectiveBorderWidth * 1.15f).coerceAtLeast(4f)
      if (innerRimRadius > 6f) {
        val innerRimPath = buildAndroidShapePath(
          cx = cx,
          cy = cy,
          radius = innerRimRadius,
          isHexagon = isHexagon,
        )
        val specularTopArgb = if (palette.isDarkTheme) {
          withAlpha(0xFFFFFFFF.toInt(), 0.14f)
        } else {
          withAlpha(0xFFFFFFFF.toInt(), 0.42f)
        }
        val specularBottomArgb = withAlpha(palette.primaryArgb, 0.04f)
        val innerRimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.STROKE
          strokeWidth = max(0.9f, effectiveBorderWidth * 0.6f)
          strokeJoin = Paint.Join.ROUND
          strokeCap = Paint.Cap.ROUND
          shader = LinearGradient(
            cx,
            cy - innerRimRadius,
            cx,
            cy + innerRimRadius,
            specularTopArgb,
            specularBottomArgb,
            Shader.TileMode.CLAMP,
          )
        }
        canvas.drawPath(innerRimPath, innerRimPaint)
      }

      // 6. Outer border outline matching the other Toolbox widgets
      val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = effectiveBorderWidth
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        color = palette.borderArgb
      }
      canvas.drawPath(outerAndroidPath, borderPaint)

      bitmap
    } catch (_: Throwable) {
      null
    }
  }

  fun buildToggleHexagonifyPendingIntent(
    context: Context,
    appWidgetId: Int,
  ): PendingIntent {
    val intent = Intent(context, HexagonifyWidgetProvider::class.java).apply {
      action = ACTION_WIDGET_TOGGLE_HEXAGONIFY
      putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      setPackage(context.packageName)
    }
    return PendingIntent.getBroadcast(
      context,
      REQUEST_CODE_TOGGLE_BASE + (appWidgetId and 0xFFFF),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  /**
   * Builds the full [RemoteViews] for a [HexagonifyWidgetProvider] instance.
   */
  fun buildWidgetRemoteViews(
    context: Context,
    appWidgetId: Int,
    state: HexagonifyWidgetState = resolveWidgetState(context, appWidgetId),
    options: Bundle? = null,
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_hexagonify)
    val palette = state.palette
    val density = try {
      context.resources.displayMetrics.density.coerceIn(1f, 4f)
    } catch (_: Throwable) {
      2f
    }

    val (widthDp, heightDp) = if (options != null) {
      MusicPlayerWidgets.resolveWidgetSizeDp(
        context = context,
        options = options,
        defaultWidthDp = state.widthDp,
        defaultHeightDp = state.heightDp,
      )
    } else {
      state.widthDp to state.heightDp
    }
    saveWidgetDimensions(context, appWidgetId, widthDp, heightDp)

    val rawWidthPx = (widthDp * density).coerceAtLeast(120f)
    val rawHeightPx = (heightDp * density).coerceAtLeast(120f)
    val maxDimPx = 900f
    val maxRaw = max(rawWidthPx, rawHeightPx)
    val scaleFactor = if (maxRaw > maxDimPx) maxDimPx / maxRaw else 1f
    val bmpWidthPx = (rawWidthPx * scaleFactor).roundToInt().coerceAtLeast(120)
    val bmpHeightPx = (rawHeightPx * scaleFactor).roundToInt().coerceAtLeast(120)

    renderHexagonifyShapeBitmap(
      isHexagon = state.isHexagon,
      palette = palette,
      widthPx = bmpWidthPx,
      heightPx = bmpHeightPx,
      borderWidthPx = max(1f, 1f * density * scaleFactor),
    )?.let { bmp ->
      views.setImageViewBitmap(R.id.widget_hexagonify_shape, bmp)
    }

    val contentDesc = if (state.isHexagon) {
      "Hexagonify active (Hexagon) — tap to switch to Circle"
    } else {
      "Hexagonify off (Circle) — tap to Hexagonify"
    }
    views.setContentDescription(R.id.widget_hexagonify_shape, contentDesc)

    val toggleIntent = buildToggleHexagonifyPendingIntent(context, appWidgetId)
    views.setOnClickPendingIntent(android.R.id.background, toggleIntent)
    views.setOnClickPendingIntent(R.id.widget_hexagonify_shape, toggleIntent)

    return views
  }

  /**
   * Handles a widget broadcast action for [HexagonifyWidgetProvider].
   */
  fun handleWidgetAction(
    context: Context?,
    intent: Intent?,
    repository: NavigationPreferencesRepository? = null,
  ): Boolean {
    val action = intent?.action ?: return false
    return when (action) {
      ACTION_WIDGET_REFRESH -> {
        if (context != null) updateAllWidgets(context)
        true
      }
      ACTION_WIDGET_TOGGLE_HEXAGONIFY -> {
        toggleHexagonify(
          context = context,
          repository = repository,
          refreshWidgets = true,
        )
        true
      }
      else -> false
    }
  }

  /**
   * Refreshes all pinned instances of [HexagonifyWidgetProvider].
   */
  fun updateAllWidgets(context: Context?) {
    val appCtx = context?.applicationContext ?: context ?: return
    try {
      val manager = AppWidgetManager.getInstance(appCtx) ?: return
      val component = ComponentName(appCtx, HexagonifyWidgetProvider::class.java)
      val widgetIds = try {
        manager.getAppWidgetIds(component) ?: IntArray(0)
      } catch (_: Throwable) {
        IntArray(0)
      }
      if (widgetIds.isEmpty()) return
      updateHexagonifyWidgets(appCtx, manager, widgetIds)
    } catch (_: Throwable) {}
  }

  fun updateHexagonifyWidgets(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
    optionsOverride: Bundle? = null,
  ) {
    for (appWidgetId in appWidgetIds) {
      try {
        val options = optionsOverride ?: try {
          appWidgetManager.getAppWidgetOptions(appWidgetId)
        } catch (_: Throwable) {
          null
        }
        val state = resolveWidgetState(
          context = context,
          appWidgetId = appWidgetId,
          options = options,
        )
        val views = buildWidgetRemoteViews(
          context = context,
          appWidgetId = appWidgetId,
          state = state,
          options = options,
        )
        appWidgetManager.updateAppWidget(appWidgetId, views)
      } catch (_: Throwable) {}
    }
  }
}

/**
 * Easter Egg Hexagonify Home Screen AppWidget that renders as an icon-free textured Circle when
 * **Hexagonify** is off and morphs into a vertical-sided Hexagon when **Hexagonify** is on.
 * Tapping the widget toggles **Hexagonify** across the entire app and all widgets.
 */
class HexagonifyWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    HexagonifyWidgets.updateHexagonifyWidgets(context, appWidgetManager, appWidgetIds)
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle,
  ) {
    HexagonifyWidgets.updateHexagonifyWidgets(
      context = context,
      appWidgetManager = appWidgetManager,
      appWidgetIds = intArrayOf(appWidgetId),
      optionsOverride = newOptions,
    )
  }

  override fun onDeleted(
    context: Context,
    appWidgetIds: IntArray,
  ) {
    super.onDeleted(context, appWidgetIds)
    for (appWidgetId in appWidgetIds) {
      HexagonifyWidgets.deleteWidgetDimensions(context, appWidgetId)
    }
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    val action = intent.action ?: return
    if (action.startsWith("com.example.toolbox.hexagonify.ACTION_")) {
      HexagonifyWidgets.handleWidgetAction(context, intent)
    }
  }
}
