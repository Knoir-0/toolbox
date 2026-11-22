package com.example.toolbox.ui.tools

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.example.toolbox.R
import com.example.toolbox.ui.main.BackgroundTexture
import com.example.toolbox.ui.main.DefaultNavigationPreferencesRepository
import com.example.toolbox.ui.main.NAVIGATION_PREFERENCES_NAME
import com.example.toolbox.ui.main.NavigationPreferencesBridge
import com.example.toolbox.ui.main.NavigationPreferencesRepository
import com.example.toolbox.ui.main.PREF_KEY_BACKGROUND_TEXTURE
import com.example.toolbox.ui.theme.drawBackgroundTexture
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Snapshot of the Background Texture options, active texture selection, responsive row count,
 * column partitioning, and Material 3 palette rendered by the Background Texture Home Screen
 * AppWidget ([BackgroundTextureWidgetProvider]).
 */
@Immutable
data class BackgroundTextureWidgetState(
  val activeTexture: BackgroundTexture,
  val textures: List<BackgroundTexture>,
  val rowCount: Int,
  val textureColumns: List<List<BackgroundTexture>>,
  val widthDp: Float,
  val heightDp: Float,
  val columnWidthDp: Float,
  val palette: TodoWidgetPalette,
  val hexagonIconButtons: Boolean = palette.hexagonIconButtons,
)

/**
 * Central state resolver, multi-row column partitioner, live texture preview swatch renderer, and
 * RemoteViews builder for the Background Texture Home Screen AppWidget ([BackgroundTextureWidgetProvider]):
 * - Displays a horizontally scrollable list of **None** ([BackgroundTexture.NONE]) and all 13
 *   procedural background texture presets ([BackgroundTexture.entries]).
 * - Automatically adapts its number of rows (`1..4`) as the widget is resized vertically on the
 *   home screen ([resolveRowCountForHeight]):
 *   - `1` row when `heightDp < 118dp` (e.g., `4x1`)
 *   - `2` rows when `118dp <= heightDp < 198dp` (e.g., `4x2`)
 *   - `3` rows when `198dp <= heightDp < 278dp` (e.g., `4x3`)
 *   - `4` rows when `heightDp >= 278dp` (e.g., `4x4+`)
 * - Tapping any texture card immediately updates the app's background texture setting directly
 *   from the home screen and refreshes all pinned Toolbox widgets.
 */
object BackgroundTextureWidgets {

  const val ACTION_WIDGET_REFRESH = "com.example.toolbox.texture.ACTION_WIDGET_REFRESH"
  const val ACTION_WIDGET_SELECT_TEXTURE = "com.example.toolbox.texture.ACTION_WIDGET_SELECT_TEXTURE"
  const val EXTRA_BACKGROUND_TEXTURE = "extra_background_texture_widget_texture"

  const val MIN_WIDGET_ROWS = 1
  const val MAX_WIDGET_ROWS = 4

  const val DEFAULT_WIDGET_WIDTH_DP = 340f
  const val DEFAULT_WIDGET_HEIGHT_DP = 84f
  const val DEFAULT_COLUMN_WIDTH_DP = 96f

  const val TWO_ROWS_HEIGHT_THRESHOLD_DP = 118f
  const val THREE_ROWS_HEIGHT_THRESHOLD_DP = 198f
  const val FOUR_ROWS_HEIGHT_THRESHOLD_DP = 278f

  const val WIDGET_CORNER_RADIUS_DP = 16f
  const val CARD_CORNER_RADIUS_DP = 14f
  const val SWATCH_CORNER_RADIUS_DP = 10f

  private const val KEY_WIDGET_WIDTH_PREFIX = "background_texture_widget_width_"
  private const val KEY_WIDGET_HEIGHT_PREFIX = "background_texture_widget_height_"

  private const val REQUEST_CODE_ITEM_TEMPLATE_BASE = 9100

  private val inMemoryDimensions = mutableMapOf<Int, Pair<Float, Float>>()

  /**
   * Resolves the number of vertical rows (`1..4`) to display inside each horizontally scrollable
   * column for a widget of [heightDp].
   */
  fun resolveRowCountForHeight(heightDp: Float): Int {
    return when {
      heightDp < TWO_ROWS_HEIGHT_THRESHOLD_DP -> 1
      heightDp < THREE_ROWS_HEIGHT_THRESHOLD_DP -> 2
      heightDp < FOUR_ROWS_HEIGHT_THRESHOLD_DP -> 3
      else -> MAX_WIDGET_ROWS
    }
  }

  /**
   * Partitions [textures] (`None` + all procedural texture options) into columns of at most
   * [rowCount] vertical items per column for the horizontally scrollable list.
   */
  fun buildTextureColumns(
    textures: List<BackgroundTexture> = BackgroundTexture.entries,
    rowCount: Int,
  ): List<List<BackgroundTexture>> {
    if (textures.isEmpty()) return emptyList()
    val coercedRows = rowCount.coerceIn(MIN_WIDGET_ROWS, MAX_WIDGET_ROWS)
    return textures.chunked(coercedRows)
  }

  /**
   * Resolves the width in `dp` of each column in the horizontally scrollable list so ~3.35 columns
   * are visible in the viewport with the next column peeking in from the right edge.
   */
  fun resolveColumnWidthDp(listAreaWidthDp: Float): Float {
    if (listAreaWidthDp <= 0f) return DEFAULT_COLUMN_WIDTH_DP
    return (listAreaWidthDp / 3.35f).coerceIn(84f, 118f)
  }

  /**
   * Persists the latest resolved `(widthDp, heightDp)` for [appWidgetId] so
   * [BackgroundTextureWidgetRemoteViewsFactory] always has accurate dimensions.
   */
  fun saveWidgetDimensions(
    context: Context?,
    appWidgetId: Int,
    widthDp: Float,
    heightDp: Float,
  ) {
    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return
    val sanitizedW = widthDp.coerceAtLeast(120f)
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
   * Loads the currently active [BackgroundTexture] from [SharedPreferences] or
   * [NavigationPreferencesBridge].
   */
  fun loadActiveBackgroundTexture(context: Context? = null): BackgroundTexture {
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(NAVIGATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(PREF_KEY_BACKGROUND_TEXTURE, null)
        if (!raw.isNullOrBlank()) {
          return BackgroundTexture.fromPrefValue(raw)
        }
      } catch (_: Throwable) {}
    }
    return NavigationPreferencesBridge.activeRepository?.getBackgroundTexture()
      ?: NavigationPreferencesBridge.backgroundTexture
  }

  /**
   * Applies [texture] as the active background texture in Settings and refreshes all pinned
   * Toolbox Home Screen AppWidgets.
   */
  fun selectBackgroundTexture(
    context: Context?,
    texture: BackgroundTexture,
    repository: NavigationPreferencesRepository? = null,
    refreshWidgets: Boolean = true,
  ) {
    NavigationPreferencesBridge.backgroundTexture = texture
    if (repository != null) {
      repository.setBackgroundTexture(texture)
    } else {
      NavigationPreferencesBridge.activeRepository?.setBackgroundTexture(texture)
    }
    if (context != null) {
      try {
        DefaultNavigationPreferencesRepository(context).setBackgroundTexture(texture)
      } catch (_: Throwable) {
        try {
          val prefs = context.getSharedPreferences(NAVIGATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
          prefs.edit().putString(PREF_KEY_BACKGROUND_TEXTURE, texture.name).apply()
        } catch (_: Throwable) {}
      }
      if (refreshWidgets) {
        updateAllWidgets(context)
        HexagonifyWidgets.updateAllWidgets(context)
      }
    }
  }

  /**
   * Resolves the current [BackgroundTextureWidgetState] for [appWidgetId] and [options].
   */
  fun resolveWidgetState(
    context: Context? = null,
    appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
    options: Bundle? = null,
    activeTextureOverride: BackgroundTexture? = null,
  ): BackgroundTextureWidgetState {
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

    val widthDp = resolvedWidthDp.coerceAtLeast(120f)
    val heightDp = resolvedHeightDp.coerceAtLeast(48f)
    if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && effectiveOptions != null) {
      saveWidgetDimensions(context, appWidgetId, widthDp, heightDp)
    }

    val activeTexture = activeTextureOverride ?: loadActiveBackgroundTexture(context)
    val allTextures = BackgroundTexture.entries
    val rowCount = resolveRowCountForHeight(heightDp)
    val columns = buildTextureColumns(allTextures, rowCount)
    val columnWidthDp = resolveColumnWidthDp(widthDp)
    val palette = TodoListWidgets.resolveWidgetPalette(context)

    return BackgroundTextureWidgetState(
      activeTexture = activeTexture,
      textures = allTextures,
      rowCount = rowCount,
      textureColumns = columns,
      widthDp = widthDp,
      heightDp = heightDp,
      columnWidthDp = columnWidthDp,
      palette = palette,
    )
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
   * Renders a `14.dp` rounded option card [Bitmap] for [texture] containing:
   * - Tonal card background (`primaryContainer` blend with `2.dp` `primary` border when selected;
   *   `itemCardArgb` with `1.dp` `borderArgb` border when unselected)
   * - Upper rounded live procedural texture preview swatch rendered via [drawBackgroundTexture]
   *   (`isPreview = true`) with the user's active theme & accent colors
   * - Top-right active selection checkmark badge (`CircleShape` or vertical-sided hexagon when
   *   **Hexagonify** is enabled) when [isSelected] is `true`.
   */
  fun renderTextureOptionCardBitmap(
    texture: BackgroundTexture,
    isSelected: Boolean,
    palette: TodoWidgetPalette,
    widthPx: Int = 180,
    heightPx: Int = 140,
    density: Float = 2f,
  ): Bitmap? {
    val safeW = widthPx.coerceIn(72, 600)
    val safeH = heightPx.coerceIn(52, 500)
    return try {
      val bitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      val w = safeW.toFloat()
      val h = safeH.toFloat()

      val cardRadius = min(CARD_CORNER_RADIUS_DP * density, min(w, h) * 0.45f)
      val cardFillArgb = if (isSelected) {
        blendArgb(palette.surfaceArgb, palette.primaryContainerArgb, 0.46f)
      } else {
        palette.itemCardArgb
      }
      val cardBorderArgb = if (isSelected) {
        palette.primaryArgb
      } else {
        palette.borderArgb
      }
      val cardBorderWidth = if (isSelected) {
        max(2f, 1.8f * density)
      } else {
        max(1f, 1f * density)
      }

      // 1. Card background fill
      val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = cardFillArgb
      }
      canvas.drawRoundRect(RectF(0f, 0f, w, h), cardRadius, cardRadius, fillPaint)

      // 2. Inner Texture Preview Swatch above the bottom label band
      val insetPad = (5f * density).coerceAtMost(min(w, h) * 0.12f)
      val labelBandPx = (20f * density).coerceAtMost(h * 0.36f)
      val swatchLeft = insetPad
      val swatchTop = insetPad
      val swatchRight = (w - insetPad).coerceAtLeast(swatchLeft + 16f)
      val swatchBottom = (h - labelBandPx).coerceAtLeast(swatchTop + 14f)
      val swatchW = (swatchRight - swatchLeft).roundToInt().coerceAtLeast(16)
      val swatchH = (swatchBottom - swatchTop).roundToInt().coerceAtLeast(14)
      val swatchRadius = min(SWATCH_CORNER_RADIUS_DP * density, min(swatchW, swatchH) * 0.45f)

      val swatchBmp = Bitmap.createBitmap(swatchW, swatchH, Bitmap.Config.ARGB_8888)
      val swatchCanvas = Canvas(swatchBmp)
      val sw = swatchW.toFloat()
      val sh = swatchH.toFloat()

      val swatchBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = palette.surfaceArgb
      }
      swatchCanvas.drawRoundRect(RectF(0f, 0f, sw, sh), swatchRadius, swatchRadius, swatchBgPaint)

      if (texture != BackgroundTexture.NONE) {
        val composeCanvas = androidx.compose.ui.graphics.Canvas(swatchBmp.asImageBitmap())
        val drawScope = CanvasDrawScope()
        drawScope.draw(
          density = Density(1f),
          layoutDirection = LayoutDirection.Ltr,
          canvas = composeCanvas,
          size = Size(sw, sh),
        ) {
          val clipPath = androidx.compose.ui.graphics.Path().apply {
            addRoundRect(
              RoundRect(
                left = 0f,
                top = 0f,
                right = sw,
                bottom = sh,
                cornerRadius = CornerRadius(swatchRadius, swatchRadius),
              )
            )
          }
          clipPath(clipPath) {
            drawBackgroundTexture(
              texture = texture,
              primaryColor = Color(palette.primaryArgb),
              tertiaryColor = Color(palette.tertiaryArgb),
              secondaryColor = Color(palette.secondaryArgb),
              onSurfaceColor = Color(palette.onSurfaceArgb),
              isDarkTheme = palette.isDarkTheme,
              isPreview = true,
            )
          }
        }
      } else {
        // Subtle "None" clean-surface circle & slash indicator in the center of the swatch
        val cx = sw * 0.5f
        val cy = sh * 0.5f
        val r = (min(sw, sh) * 0.24f).coerceAtLeast(5f)
        val noneIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.STROKE
          strokeWidth = max(1.2f, 1.1f * density)
          strokeCap = Paint.Cap.ROUND
          color = withAlpha(
            if (isSelected) palette.primaryArgb else palette.onSurfaceVariantArgb,
            if (isSelected) 0.55f else 0.42f,
          )
        }
        swatchCanvas.drawCircle(cx, cy, r, noneIconPaint)
        val diag = r * 0.707f
        swatchCanvas.drawLine(cx - diag, cy + diag, cx + diag, cy - diag, noneIconPaint)
      }

      // Swatch border outline
      val swatchBorderW = max(1f, 0.9f * density)
      val swatchBorderInset = swatchBorderW * 0.5f
      val swatchBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = swatchBorderW
        color = if (isSelected) {
          withAlpha(palette.primaryArgb, 0.55f)
        } else {
          palette.borderArgb
        }
      }
      swatchCanvas.drawRoundRect(
        RectF(swatchBorderInset, swatchBorderInset, sw - swatchBorderInset, sh - swatchBorderInset),
        max(0f, swatchRadius - swatchBorderInset),
        max(0f, swatchRadius - swatchBorderInset),
        swatchBorderPaint,
      )

      canvas.drawBitmap(swatchBmp, swatchLeft, swatchTop, null)

      // 3. Top-right active selection check badge (matching SettingsScreen)
      if (isSelected) {
        val badgeSize = (16f * density).coerceIn(14f, min(sw, sh) * 0.72f)
        val badgeMargin = (3.5f * density).coerceAtLeast(2f)
        val badgeCx = swatchRight - badgeMargin - badgeSize * 0.5f
        val badgeCy = swatchTop + badgeMargin + badgeSize * 0.5f
        val badgeRadius = badgeSize * 0.5f

        val badgeFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.FILL
          color = palette.primaryArgb
        }
        if (palette.hexagonIconButtons) {
          val halfW = badgeRadius * 0.866f
          val halfVert = badgeRadius * 0.5f
          val hexPath = Path().apply {
            moveTo(badgeCx, badgeCy - badgeRadius)
            lineTo(badgeCx + halfW, badgeCy - halfVert)
            lineTo(badgeCx + halfW, badgeCy + halfVert)
            lineTo(badgeCx, badgeCy + badgeRadius)
            lineTo(badgeCx - halfW, badgeCy + halfVert)
            lineTo(badgeCx - halfW, badgeCy - halfVert)
            close()
          }
          canvas.drawPath(hexPath, badgeFillPaint)
        } else {
          canvas.drawCircle(badgeCx, badgeCy, badgeRadius, badgeFillPaint)
        }

        // Checkmark icon inside badge
        val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          style = Paint.Style.STROKE
          strokeWidth = max(1.6f, badgeSize * 0.13f)
          strokeCap = Paint.Cap.ROUND
          strokeJoin = Paint.Join.ROUND
          color = palette.onPrimaryArgb
        }
        val checkPath = Path().apply {
          moveTo(badgeCx - badgeRadius * 0.42f, badgeCy + badgeRadius * 0.02f)
          lineTo(badgeCx - badgeRadius * 0.10f, badgeCy + badgeRadius * 0.34f)
          lineTo(badgeCx + badgeRadius * 0.46f, badgeCy - badgeRadius * 0.28f)
        }
        canvas.drawPath(checkPath, checkPaint)
      }

      // 4. Outer Card Border
      val strokeInset = max(cardBorderWidth * 0.75f, 1f)
      val borderRect = RectF(strokeInset, strokeInset, w - strokeInset, h - strokeInset)
      val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = cardBorderWidth
        color = cardBorderArgb
      }
      canvas.drawRoundRect(
        borderRect,
        max(0f, cardRadius - strokeInset * 0.5f),
        max(0f, cardRadius - strokeInset * 0.5f),
        borderPaint,
      )

      bitmap
    } catch (_: Throwable) {
      null
    }
  }

  private data class TextureRowSlotIds(
    val containerId: Int,
    val bgId: Int,
    val labelId: Int,
  )

  private val ROW_SLOTS = listOf(
    TextureRowSlotIds(
      containerId = R.id.widget_texture_slot_1,
      bgId = R.id.widget_texture_slot_1_bg,
      labelId = R.id.widget_texture_slot_1_label,
    ),
    TextureRowSlotIds(
      containerId = R.id.widget_texture_slot_2,
      bgId = R.id.widget_texture_slot_2_bg,
      labelId = R.id.widget_texture_slot_2_label,
    ),
    TextureRowSlotIds(
      containerId = R.id.widget_texture_slot_3,
      bgId = R.id.widget_texture_slot_3_bg,
      labelId = R.id.widget_texture_slot_3_label,
    ),
    TextureRowSlotIds(
      containerId = R.id.widget_texture_slot_4,
      bgId = R.id.widget_texture_slot_4_bg,
      labelId = R.id.widget_texture_slot_4_label,
    ),
  )

  /**
   * Builds the [RemoteViews] for a single vertical column of [columnTextures] (containing `1..4`
   * vertically stacked texture cards depending on [rowCount]) inside the horizontally scrollable
   * [ListView].
   */
  fun buildTextureColumnRemoteViews(
    context: Context,
    columnTextures: List<BackgroundTexture>,
    activeTexture: BackgroundTexture,
    rowCount: Int,
    columnWidthDp: Float = DEFAULT_COLUMN_WIDTH_DP,
    columnHeightDp: Float = DEFAULT_WIDGET_HEIGHT_DP,
    palette: TodoWidgetPalette = TodoListWidgets.resolveWidgetPalette(context),
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_background_texture_column_item)
    val coercedRowCount = rowCount.coerceIn(MIN_WIDGET_ROWS, MAX_WIDGET_ROWS)
    val safeColWidthDp = columnWidthDp.coerceIn(72f, 160f)
    val safeColHeightDp = columnHeightDp.coerceIn(48f, 520f)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      try {
        views.setViewLayoutHeight(
          R.id.widget_texture_column_outer,
          safeColWidthDp,
          TypedValue.COMPLEX_UNIT_DIP,
        )
        views.setViewLayoutWidth(
          R.id.widget_texture_column_inner,
          safeColWidthDp,
          TypedValue.COMPLEX_UNIT_DIP,
        )
        views.setViewLayoutHeight(
          R.id.widget_texture_column_inner,
          safeColHeightDp,
          TypedValue.COMPLEX_UNIT_DIP,
        )
      } catch (_: Throwable) {}
    }

    val density = try {
      context.resources.displayMetrics.density.coerceIn(1f, 3.5f)
    } catch (_: Throwable) {
      2f
    }

    val cardWidthPx = ((safeColWidthDp - 6f) * density).roundToInt().coerceIn(72, 360)
    val slotHeightDp = ((safeColHeightDp - 10f) / coercedRowCount.toFloat()) - 4f
    val cardHeightPx = (slotHeightDp.coerceAtLeast(36f) * density).roundToInt().coerceIn(52, 320)

    ROW_SLOTS.forEachIndexed { rowIndex, slot ->
      if (rowIndex >= coercedRowCount) {
        views.setViewVisibility(slot.containerId, View.GONE)
      } else {
        val texture = columnTextures.getOrNull(rowIndex)
        if (texture == null) {
          views.setViewVisibility(slot.containerId, View.INVISIBLE)
        } else {
          views.setViewVisibility(slot.containerId, View.VISIBLE)
          val isSelected = texture == activeTexture
          renderTextureOptionCardBitmap(
            texture = texture,
            isSelected = isSelected,
            palette = palette,
            widthPx = cardWidthPx,
            heightPx = cardHeightPx,
            density = density,
          )?.let { bmp ->
            views.setImageViewBitmap(slot.bgId, bmp)
          }

          views.setTextViewText(slot.labelId, texture.displayName)
          views.setTextColor(
            slot.labelId,
            if (isSelected) palette.primaryArgb else palette.onSurfaceArgb,
          )
          views.setContentDescription(
            slot.containerId,
            if (isSelected) {
              "${texture.displayName} background texture (Active)"
            } else {
              "Select ${texture.displayName} background texture"
            },
          )

          val fillInIntent = Intent().apply {
            putExtra(EXTRA_BACKGROUND_TEXTURE, texture.name)
          }
          views.setOnClickFillInIntent(slot.containerId, fillInIntent)
          views.setOnClickFillInIntent(slot.bgId, fillInIntent)
          views.setOnClickFillInIntent(slot.labelId, fillInIntent)
        }
      }
    }

    return views
  }

  fun buildSelectTextureTemplatePendingIntent(
    context: Context,
    appWidgetId: Int,
  ): PendingIntent {
    val intent = Intent(context, BackgroundTextureWidgetProvider::class.java).apply {
      action = ACTION_WIDGET_SELECT_TEXTURE
      putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      setPackage(context.packageName)
    }
    val mutabilityFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      PendingIntent.FLAG_MUTABLE
    } else {
      0
    }
    return PendingIntent.getBroadcast(
      context,
      REQUEST_CODE_ITEM_TEMPLATE_BASE + (appWidgetId and 0xFFFF),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or mutabilityFlag,
    )
  }

  /**
   * Builds the full [RemoteViews] for a [BackgroundTextureWidgetProvider] instance.
   */
  fun buildWidgetRemoteViews(
    context: Context,
    appWidgetId: Int,
    state: BackgroundTextureWidgetState = resolveWidgetState(context, appWidgetId),
    options: Bundle? = null,
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_background_texture)
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

    val rawWidthPx = (widthDp * density).coerceAtLeast(160f)
    val rawHeightPx = (heightDp * density).coerceAtLeast(80f)
    val maxDimPx = 1000f
    val maxRaw = max(rawWidthPx, rawHeightPx)
    val scaleFactor = if (maxRaw > maxDimPx) maxDimPx / maxRaw else 1f
    val bgWidthPx = (rawWidthPx * scaleFactor).roundToInt().coerceAtLeast(160)
    val bgHeightPx = (rawHeightPx * scaleFactor).roundToInt().coerceAtLeast(80)

    TodoListWidgets.renderWidgetBackgroundBitmap(
      palette = palette,
      widthPx = bgWidthPx,
      heightPx = bgHeightPx,
      cornerRadiusPx = WIDGET_CORNER_RADIUS_DP * density * scaleFactor,
      borderWidthPx = max(1f, 1f * density * scaleFactor),
    )?.let { bgBmp ->
      views.setImageViewBitmap(R.id.widget_background_texture_background, bgBmp)
    }

    // Because the ListView is rotated -90deg around its center:
    // - its local width is its vertical height on screen (heightDp)
    // - its local height is its horizontal width on screen (widthDp)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      try {
        views.setViewLayoutWidth(
          R.id.widget_background_texture_list_view,
          heightDp.coerceAtLeast(48f),
          TypedValue.COMPLEX_UNIT_DIP,
        )
        views.setViewLayoutHeight(
          R.id.widget_background_texture_list_view,
          widthDp.coerceAtLeast(120f),
          TypedValue.COMPLEX_UNIT_DIP,
        )
      } catch (_: Throwable) {}
    }

    val serviceIntent = Intent(context, BackgroundTextureWidgetRemoteViewsService::class.java).apply {
      putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
    }
    @Suppress("DEPRECATION")
    views.setRemoteAdapter(R.id.widget_background_texture_list_view, serviceIntent)
    views.setEmptyView(
      R.id.widget_background_texture_list_view,
      R.id.widget_background_texture_empty_view,
    )

    views.setPendingIntentTemplate(
      R.id.widget_background_texture_list_view,
      buildSelectTextureTemplatePendingIntent(context, appWidgetId),
    )

    return views
  }

  /**
   * Handles a widget broadcast action for [BackgroundTextureWidgetProvider].
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
      ACTION_WIDGET_SELECT_TEXTURE -> {
        val rawTexture = intent.getStringExtra(EXTRA_BACKGROUND_TEXTURE)
        val texture = BackgroundTexture.fromPrefValue(rawTexture)
        selectBackgroundTexture(
          context = context,
          texture = texture,
          repository = repository,
          refreshWidgets = true,
        )
        true
      }
      else -> false
    }
  }

  /**
   * Refreshes all pinned instances of [BackgroundTextureWidgetProvider] and notifies their
   * horizontally scrollable [ListView] adapters that data changed.
   */
  fun updateAllWidgets(context: Context?) {
    val appCtx = context?.applicationContext ?: context ?: return
    try {
      val manager = AppWidgetManager.getInstance(appCtx) ?: return
      val component = ComponentName(appCtx, BackgroundTextureWidgetProvider::class.java)
      val widgetIds = try {
        manager.getAppWidgetIds(component) ?: IntArray(0)
      } catch (_: Throwable) {
        IntArray(0)
      }
      if (widgetIds.isEmpty()) return
      updateBackgroundTextureWidgets(appCtx, manager, widgetIds)
    } catch (_: Throwable) {}
  }

  fun updateBackgroundTextureWidgets(
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
    try {
      @Suppress("DEPRECATION")
      appWidgetManager.notifyAppWidgetViewDataChanged(
        appWidgetIds,
        R.id.widget_background_texture_list_view,
      )
    } catch (_: Throwable) {}
  }
}

/**
 * Horizontally scrollable Background Texture Home Screen AppWidget that displays **None** and all
 * procedural background texture presets, automatically expanding to multiple rows as the widget is
 * resized vertically.
 */
class BackgroundTextureWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    BackgroundTextureWidgets.updateBackgroundTextureWidgets(context, appWidgetManager, appWidgetIds)
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle,
  ) {
    BackgroundTextureWidgets.updateBackgroundTextureWidgets(
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
      BackgroundTextureWidgets.deleteWidgetDimensions(context, appWidgetId)
    }
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    val action = intent.action ?: return
    if (action.startsWith("com.example.toolbox.texture.ACTION_")) {
      BackgroundTextureWidgets.handleWidgetAction(context, intent)
    }
  }
}

/**
 * [RemoteViewsService] that powers the horizontally scrollable multi-row [ListView] inside
 * [BackgroundTextureWidgetProvider].
 */
class BackgroundTextureWidgetRemoteViewsService : RemoteViewsService() {
  override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
    val appWidgetId = intent.getIntExtra(
      AppWidgetManager.EXTRA_APPWIDGET_ID,
      AppWidgetManager.INVALID_APPWIDGET_ID,
    )
    return BackgroundTextureWidgetRemoteViewsFactory(applicationContext, appWidgetId)
  }
}

internal class BackgroundTextureWidgetRemoteViewsFactory(
  private val context: Context,
  private val appWidgetId: Int,
) : RemoteViewsService.RemoteViewsFactory {

  private var state: BackgroundTextureWidgetState =
    BackgroundTextureWidgets.resolveWidgetState(context, appWidgetId)

  override fun onCreate() {
    reloadState()
  }

  override fun onDataSetChanged() {
    reloadState()
  }

  private fun reloadState() {
    try {
      state = BackgroundTextureWidgets.resolveWidgetState(
        context = context,
        appWidgetId = appWidgetId,
      )
    } catch (_: Throwable) {}
  }

  override fun onDestroy() {}

  override fun getCount(): Int = state.textureColumns.size

  override fun getViewAt(position: Int): RemoteViews? {
    val currentState = state
    val columnTextures = currentState.textureColumns.getOrNull(position) ?: return null
    return BackgroundTextureWidgets.buildTextureColumnRemoteViews(
      context = context,
      columnTextures = columnTextures,
      activeTexture = currentState.activeTexture,
      rowCount = currentState.rowCount,
      columnWidthDp = currentState.columnWidthDp,
      columnHeightDp = currentState.heightDp,
      palette = currentState.palette,
    )
  }

  override fun getLoadingView(): RemoteViews? = null

  override fun getViewTypeCount(): Int = 1

  override fun getItemId(position: Int): Long {
    val col = state.textureColumns.getOrNull(position) ?: return position.toLong()
    return col.joinToString("|") { it.name }.hashCode().toLong()
  }

  override fun hasStableIds(): Boolean = true
}
