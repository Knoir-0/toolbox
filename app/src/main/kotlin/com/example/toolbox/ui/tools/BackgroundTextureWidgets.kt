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
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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
import com.example.toolbox.ui.main.DEFAULT_HEXAGON_ICON_BUTTONS
import com.example.toolbox.ui.main.DefaultNavigationPreferencesRepository
import com.example.toolbox.ui.main.NAVIGATION_PREFERENCES_NAME
import com.example.toolbox.ui.main.NavigationPreferencesBridge
import com.example.toolbox.ui.main.NavigationPreferencesRepository
import com.example.toolbox.ui.main.PREF_KEY_BACKGROUND_TEXTURE
import com.example.toolbox.ui.main.PREF_KEY_HEXAGON_ICON_BUTTONS
import com.example.toolbox.ui.theme.drawBackgroundTexture
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Represents an entry in the Style Home Screen AppWidget ([BackgroundTextureWidgetProvider]):
 * - [HexagonifyShape]: The unlabeled mystery shape view placed as the very first item in the
 *   widget grid, rendering the floating circle (`isHexagon == false`) or vertical-sided hexagon
 *   (`isHexagon == true`) that toggles Hexagonify on tap.
 * - [TextureOption]: A procedural [BackgroundTexture] option card (`None` and all 13 presets).
 */
@Immutable
sealed interface StyleWidgetItem {
  val key: String

  @Immutable
  data object HexagonifyShape : StyleWidgetItem {
    override val key: String = "HEXAGONIFY_SHAPE"
  }

  @Immutable
  data class TextureOption(val texture: BackgroundTexture) : StyleWidgetItem {
    override val key: String = "TEXTURE_${texture.name}"
  }
}

/**
 * Snapshot of the Style options, active texture selection, Hexagonify state, responsive column
 * count, row partitioning, and Material 3 palette rendered by the Style Home Screen AppWidget
 * ([BackgroundTextureWidgetProvider]).
 */
@Immutable
data class BackgroundTextureWidgetState(
  val activeTexture: BackgroundTexture,
  val isHexagon: Boolean,
  val textures: List<BackgroundTexture>,
  val items: List<StyleWidgetItem>,
  val columnCount: Int,
  val itemRows: List<List<StyleWidgetItem>>,
  val textureRows: List<List<BackgroundTexture>>,
  val widthDp: Float,
  val heightDp: Float,
  val columnWidthDp: Float,
  val rowHeightDp: Float,
  val palette: TodoWidgetPalette,
  val hexagonIconButtons: Boolean = isHexagon,
)

/**
 * Central state resolver, multi-column row partitioner, live style & texture preview renderer, and
 * RemoteViews builder for the Style Home Screen AppWidget ([BackgroundTextureWidgetProvider]):
 * - Displays a vertically scrollable list where the very first item is the unlabeled mystery
 *   circle/hexagon shape view ([StyleWidgetItem.HexagonifyShape]), followed by **None**
 *   ([BackgroundTexture.NONE]) and all 13 procedural background texture presets
 *   ([BackgroundTexture.entries]).
 * - Automatically adapts its number of columns per row (`1..4`) as the widget is resized
 *   horizontally on the home screen ([resolveColumnCountForWidth]):
 *   - `1` column when `widthDp < 130dp` (e.g., `1x2`, `1x3`)
 *   - `2` columns when `130dp <= widthDp < 210dp` (e.g., `2x2`, `2x3`)
 *   - `3` columns when `210dp <= widthDp < 290dp` (e.g., `3x2`, `3x3`)
 *   - `4` columns when `widthDp >= 290dp` (e.g., `4x1`, `4x2+`)
 * - Tapping the first item toggles **Hexagonify** across the entire app and all widgets; tapping
 *   any texture card immediately updates the app's background texture setting directly from the
 *   home screen and refreshes all pinned Toolbox widgets.
 */
object BackgroundTextureWidgets {

  const val ACTION_WIDGET_REFRESH = "com.example.toolbox.texture.ACTION_WIDGET_REFRESH"
  const val ACTION_WIDGET_SELECT_TEXTURE = "com.example.toolbox.texture.ACTION_WIDGET_SELECT_TEXTURE"
  const val ACTION_WIDGET_TOGGLE_HEXAGONIFY = "com.example.toolbox.texture.ACTION_WIDGET_TOGGLE_HEXAGONIFY"
  const val EXTRA_BACKGROUND_TEXTURE = "extra_background_texture_widget_texture"
  const val EXTRA_TOGGLE_HEXAGONIFY = "extra_background_texture_widget_toggle_hexagonify"

  const val MIN_WIDGET_COLUMNS = 1
  const val MAX_WIDGET_COLUMNS = 4

  const val DEFAULT_WIDGET_WIDTH_DP = 180f
  const val DEFAULT_WIDGET_HEIGHT_DP = 160f
  const val DEFAULT_COLUMN_WIDTH_DP = 86f
  const val DEFAULT_ROW_HEIGHT_DP = 76f

  const val TWO_COLUMNS_WIDTH_THRESHOLD_DP = 130f
  const val THREE_COLUMNS_WIDTH_THRESHOLD_DP = 210f
  const val FOUR_COLUMNS_WIDTH_THRESHOLD_DP = 290f

  const val TWO_ROWS_VISIBLE_HEIGHT_THRESHOLD_DP = 118f

  const val WIDGET_CORNER_RADIUS_DP = 16f
  const val CARD_CORNER_RADIUS_DP = 14f
  const val SWATCH_CORNER_RADIUS_DP = 10f
  const val HEXAGON_CORNER_RADIUS_FRACTION = 0.08f

  private const val KEY_WIDGET_WIDTH_PREFIX = "background_texture_widget_width_"
  private const val KEY_WIDGET_HEIGHT_PREFIX = "background_texture_widget_height_"

  private const val REQUEST_CODE_ITEM_TEMPLATE_BASE = 9100

  private val inMemoryDimensions = mutableMapOf<Int, Pair<Float, Float>>()

  /**
   * Builds the ordered list of [StyleWidgetItem]s displayed in the widget: the mystery
   * circle/hexagon view first ([StyleWidgetItem.HexagonifyShape]), followed by all [textures]
   * (`None` + 13 procedural textures).
   */
  fun buildStyleItems(
    textures: List<BackgroundTexture> = BackgroundTexture.entries,
  ): List<StyleWidgetItem> {
    val result = ArrayList<StyleWidgetItem>(textures.size + 1)
    result.add(StyleWidgetItem.HexagonifyShape)
    textures.forEach { texture ->
      result.add(StyleWidgetItem.TextureOption(texture))
    }
    return result
  }

  /**
   * Resolves the number of horizontal columns (`1..4`) to display inside each vertically scrollable
   * row for a widget of [widthDp].
   */
  fun resolveColumnCountForWidth(widthDp: Float): Int {
    return when {
      widthDp < TWO_COLUMNS_WIDTH_THRESHOLD_DP -> 1
      widthDp < THREE_COLUMNS_WIDTH_THRESHOLD_DP -> 2
      widthDp < FOUR_COLUMNS_WIDTH_THRESHOLD_DP -> 3
      else -> MAX_WIDGET_COLUMNS
    }
  }

  /**
   * Partitions [items] (the first [StyleWidgetItem.HexagonifyShape] item followed by `None` and all
   * procedural texture options) into horizontal rows of at most [columnCount] items per row for
   * the vertically scrollable list.
   */
  fun buildStyleItemRows(
    items: List<StyleWidgetItem> = buildStyleItems(),
    columnCount: Int,
  ): List<List<StyleWidgetItem>> {
    if (items.isEmpty()) return emptyList()
    val coercedColumns = columnCount.coerceIn(MIN_WIDGET_COLUMNS, MAX_WIDGET_COLUMNS)
    return items.chunked(coercedColumns)
  }

  /**
   * Partitions [textures] (`None` + all procedural texture options) into horizontal rows of at most
   * [columnCount] items per row.
   */
  fun buildTextureRows(
    textures: List<BackgroundTexture> = BackgroundTexture.entries,
    columnCount: Int,
  ): List<List<BackgroundTexture>> {
    if (textures.isEmpty()) return emptyList()
    val coercedColumns = columnCount.coerceIn(MIN_WIDGET_COLUMNS, MAX_WIDGET_COLUMNS)
    return textures.chunked(coercedColumns)
  }

  /**
   * Resolves the width in `dp` of each column slot inside a row of the vertically scrollable list.
   */
  fun resolveColumnWidthDp(
    listAreaWidthDp: Float,
    columnCount: Int = resolveColumnCountForWidth(listAreaWidthDp),
  ): Float {
    if (listAreaWidthDp <= 0f) return DEFAULT_COLUMN_WIDTH_DP
    val coercedColumns = columnCount.coerceIn(MIN_WIDGET_COLUMNS, MAX_WIDGET_COLUMNS)
    return ((listAreaWidthDp - 8f) / coercedColumns.toFloat()).coerceIn(56f, 240f)
  }

  /**
   * Resolves the height in `dp` of each row in the vertically scrollable list so a single-row-tall
   * widget fits 1 full row cleanly while taller widgets show 2+ rows with smooth vertical scrolling.
   */
  fun resolveRowHeightDp(heightDp: Float): Float {
    if (heightDp <= 0f) return DEFAULT_ROW_HEIGHT_DP
    return if (heightDp < TWO_ROWS_VISIBLE_HEIGHT_THRESHOLD_DP) {
      (heightDp - 8f).coerceIn(52f, 84f)
    } else {
      DEFAULT_ROW_HEIGHT_DP
    }
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
    val sanitizedW = widthDp.coerceAtLeast(56f)
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
  fun loadActiveBackgroundTexture(
    context: Context? = null,
    repository: NavigationPreferencesRepository? = null,
  ): BackgroundTexture {
    if (repository != null) {
      return repository.getBackgroundTexture()
    }
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
    repository: NavigationPreferencesRepository? = null,
    activeTextureOverride: BackgroundTexture? = null,
    isHexagonOverride: Boolean? = null,
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

    val widthDp = resolvedWidthDp.coerceAtLeast(56f)
    val heightDp = resolvedHeightDp.coerceAtLeast(48f)
    if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && effectiveOptions != null) {
      saveWidgetDimensions(context, appWidgetId, widthDp, heightDp)
    }

    val activeTexture = activeTextureOverride ?: loadActiveBackgroundTexture(context, repository)
    val isHexagon = isHexagonOverride ?: loadHexagonifyEnabled(context, repository)
    val allTextures = BackgroundTexture.entries
    val allItems = buildStyleItems(allTextures)
    val columnCount = resolveColumnCountForWidth(widthDp)
    val itemRows = buildStyleItemRows(allItems, columnCount)
    val textureRows = buildTextureRows(allTextures, columnCount)
    val columnWidthDp = resolveColumnWidthDp(widthDp, columnCount)
    val rowHeightDp = resolveRowHeightDp(heightDp)
    val basePalette = TodoListWidgets.resolveWidgetPalette(context)
    val palette = basePalette.copy(
      hexagonIconButtons = isHexagon,
      backgroundTexture = activeTexture,
      iconButtonBackgroundRes = MusicPlayerWidgets.resolveIconButtonBackgroundRes(isHexagon),
    )

    return BackgroundTextureWidgetState(
      activeTexture = activeTexture,
      isHexagon = isHexagon,
      textures = allTextures,
      items = allItems,
      columnCount = columnCount,
      itemRows = itemRows,
      textureRows = textureRows,
      widthDp = widthDp,
      heightDp = heightDp,
      columnWidthDp = columnWidthDp,
      rowHeightDp = rowHeightDp,
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
   * Renders the unlabeled first-item shape [Bitmap] as a floating **Circle** (`isHexagon == false`)
   * or vertical-sided **Hexagon** (`isHexagon == true`) clipped to the active procedural
   * [BackgroundTexture], layered with soft Material 3 radial sheen, subtle concentric geometric
   * contours, and an outer border outline.
   */
  fun renderHexagonifyShapeBitmap(
    isHexagon: Boolean,
    palette: TodoWidgetPalette,
    widthPx: Int = 180,
    heightPx: Int = 140,
    borderWidthPx: Float = 2f,
  ): Bitmap? {
    val safeW = widthPx.coerceIn(64, 1000)
    val safeH = heightPx.coerceIn(52, 1000)
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

      // 4. Subtle concentric inner geometric contours (no icons or labels!)
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
   * Builds the [RemoteViews] for a single horizontal row of [rowItems] (containing `1..4`
   * side-by-side items depending on [columnCount]) inside the vertically scrollable [ListView].
   */
  fun buildStyleRowRemoteViews(
    context: Context,
    rowItems: List<StyleWidgetItem>,
    activeTexture: BackgroundTexture,
    isHexagon: Boolean,
    columnCount: Int,
    columnWidthDp: Float = DEFAULT_COLUMN_WIDTH_DP,
    rowHeightDp: Float = DEFAULT_ROW_HEIGHT_DP,
    palette: TodoWidgetPalette = TodoListWidgets.resolveWidgetPalette(context),
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_background_texture_column_item)
    val coercedColumnCount = columnCount.coerceIn(MIN_WIDGET_COLUMNS, MAX_WIDGET_COLUMNS)
    val safeColWidthDp = columnWidthDp.coerceIn(52f, 260f)
    val safeRowHeightDp = rowHeightDp.coerceIn(48f, 160f)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      try {
        views.setViewLayoutHeight(
          R.id.widget_texture_column_outer,
          safeRowHeightDp,
          TypedValue.COMPLEX_UNIT_DIP,
        )
        views.setViewLayoutHeight(
          R.id.widget_texture_column_inner,
          safeRowHeightDp,
          TypedValue.COMPLEX_UNIT_DIP,
        )
      } catch (_: Throwable) {}
    }

    val density = try {
      context.resources.displayMetrics.density.coerceIn(1f, 3.5f)
    } catch (_: Throwable) {
      2f
    }

    val cardWidthPx = ((safeColWidthDp - 4f) * density).roundToInt().coerceIn(72, 420)
    val cardHeightPx = ((safeRowHeightDp - 6f) * density).roundToInt().coerceIn(52, 320)

    ROW_SLOTS.forEachIndexed { colIndex, slot ->
      if (colIndex >= coercedColumnCount) {
        views.setViewVisibility(slot.containerId, View.GONE)
      } else {
        val item = rowItems.getOrNull(colIndex)
        if (item == null) {
          views.setViewVisibility(slot.containerId, View.INVISIBLE)
        } else {
          views.setViewVisibility(slot.containerId, View.VISIBLE)
          when (item) {
            is StyleWidgetItem.HexagonifyShape -> {
              // Unlabeled mystery shape view — no text or explanation
              views.setViewVisibility(slot.labelId, View.GONE)
              views.setTextViewText(slot.labelId, "")
              renderHexagonifyShapeBitmap(
                isHexagon = isHexagon,
                palette = palette,
                widthPx = cardWidthPx,
                heightPx = cardHeightPx,
                borderWidthPx = max(1f, 1.2f * density),
              )?.let { bmp ->
                views.setImageViewBitmap(slot.bgId, bmp)
              }
              views.setContentDescription(slot.containerId, null)

              val fillInIntent = Intent().apply {
                putExtra(EXTRA_TOGGLE_HEXAGONIFY, true)
              }
              views.setOnClickFillInIntent(slot.containerId, fillInIntent)
              views.setOnClickFillInIntent(slot.bgId, fillInIntent)
            }
            is StyleWidgetItem.TextureOption -> {
              val texture = item.texture
              val isSelected = texture == activeTexture
              views.setViewVisibility(slot.labelId, View.VISIBLE)
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
                  "${texture.displayName} style (Active)"
                } else {
                  "Select ${texture.displayName} style"
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
      }
    }

    return views
  }

  /**
   * Builds the [RemoteViews] for a single horizontal row of [rowTextures] inside the vertically
   * scrollable [ListView].
   */
  fun buildTextureRowRemoteViews(
    context: Context,
    rowTextures: List<BackgroundTexture>,
    activeTexture: BackgroundTexture,
    columnCount: Int,
    columnWidthDp: Float = DEFAULT_COLUMN_WIDTH_DP,
    rowHeightDp: Float = DEFAULT_ROW_HEIGHT_DP,
    palette: TodoWidgetPalette = TodoListWidgets.resolveWidgetPalette(context),
  ): RemoteViews {
    return buildStyleRowRemoteViews(
      context = context,
      rowItems = rowTextures.map { StyleWidgetItem.TextureOption(it) },
      activeTexture = activeTexture,
      isHexagon = palette.hexagonIconButtons,
      columnCount = columnCount,
      columnWidthDp = columnWidthDp,
      rowHeightDp = rowHeightDp,
      palette = palette,
    )
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

    val rawWidthPx = (widthDp * density).coerceAtLeast(120f)
    val rawHeightPx = (heightDp * density).coerceAtLeast(80f)
    val maxDimPx = 1000f
    val maxRaw = max(rawWidthPx, rawHeightPx)
    val scaleFactor = if (maxRaw > maxDimPx) maxDimPx / maxRaw else 1f
    val bgWidthPx = (rawWidthPx * scaleFactor).roundToInt().coerceAtLeast(120)
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
      ACTION_WIDGET_TOGGLE_HEXAGONIFY -> {
        toggleHexagonify(
          context = context,
          repository = repository,
          refreshWidgets = true,
        )
        true
      }
      ACTION_WIDGET_SELECT_TEXTURE -> {
        if (intent.getBooleanExtra(EXTRA_TOGGLE_HEXAGONIFY, false)) {
          toggleHexagonify(
            context = context,
            repository = repository,
            refreshWidgets = true,
          )
        } else {
          val rawTexture = intent.getStringExtra(EXTRA_BACKGROUND_TEXTURE)
          val texture = BackgroundTexture.fromPrefValue(rawTexture)
          selectBackgroundTexture(
            context = context,
            texture = texture,
            repository = repository,
            refreshWidgets = true,
          )
        }
        true
      }
      else -> false
    }
  }

  /**
   * Refreshes all pinned instances of [BackgroundTextureWidgetProvider] and notifies their
   * vertically scrollable [ListView] adapters that data changed.
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
 * Vertically scrollable Style Home Screen AppWidget that displays the unlabeled shape view first
 * followed by **None** and all procedural background texture presets, automatically adapting its
 * number of columns per row as the widget is resized horizontally.
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
 * [RemoteViewsService] that powers the vertically scrollable multi-column [ListView] inside
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

  override fun getCount(): Int = state.itemRows.size

  override fun getViewAt(position: Int): RemoteViews? {
    val currentState = state
    val rowItems = currentState.itemRows.getOrNull(position) ?: return null
    return BackgroundTextureWidgets.buildStyleRowRemoteViews(
      context = context,
      rowItems = rowItems,
      activeTexture = currentState.activeTexture,
      isHexagon = currentState.isHexagon,
      columnCount = currentState.columnCount,
      columnWidthDp = currentState.columnWidthDp,
      rowHeightDp = currentState.rowHeightDp,
      palette = currentState.palette,
    )
  }

  override fun getLoadingView(): RemoteViews? = null

  override fun getViewTypeCount(): Int = 1

  override fun getItemId(position: Int): Long {
    val row = state.itemRows.getOrNull(position) ?: return position.toLong()
    return row.joinToString("|") { it.key }.hashCode().toLong()
  }

  override fun hasStableIds(): Boolean = true
}
