package com.example.toolbox.ui.tools

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.RemoteViews
import androidx.compose.runtime.Immutable
import com.example.toolbox.R
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.createStandaloneToolIntent
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Snapshot of the Good tool state and Material 3 palette rendered by the compact Good Home Screen
 * AppWidget ([GoodWidgetProvider]).
 */
@Immutable
data class GoodWidgetState(
  val currentMessage: String,
  val palette: TodoWidgetPalette,
  val hexagonIconButtons: Boolean = palette.hexagonIconButtons,
  val iconButtonBackgroundRes: Int = palette.iconButtonBackgroundRes,
)

/**
 * Central state resolver, RemoteViews builder, and action handler for the compact Good Home Screen
 * AppWidget ([GoodWidgetProvider]):
 * - Displays a simple bar with the current positive message on the left and a refresh button on the right.
 * - Tapping the refresh button on the right picks a new positive message, saves it to [GoodRepository],
 *   and updates all Good widgets (and any active [GoodScreen]).
 * - Tapping the message or bar body on the left opens the **Good** tool ([Tool.GOOD]).
 */
object GoodWidgets {

  const val ACTION_WIDGET_REFRESH = "com.example.toolbox.good.ACTION_WIDGET_REFRESH"
  const val ACTION_WIDGET_NEXT_MESSAGE = "com.example.toolbox.good.ACTION_WIDGET_NEXT_MESSAGE"

  const val WIDGET_CORNER_RADIUS_DP = 16f

  private const val REQUEST_CODE_OPEN_GOOD = 5700
  private const val REQUEST_CODE_NEXT_MESSAGE = 5701

  private var inMemoryFallbackMessage: String = DEFAULT_GOOD_MESSAGE

  fun clearInMemoryStateForTesting() {
    inMemoryFallbackMessage = DEFAULT_GOOD_MESSAGE
  }

  /**
   * Resolves the current [GoodWidgetState] from persisted Good preferences (or [repository]) and
   * the user's active Material 3 theme palette.
   */
  fun resolveWidgetState(
    context: Context? = null,
    repository: GoodRepository? = null,
  ): GoodWidgetState {
    val currentMessage = when {
      repository != null -> repository.getCurrentMessage()
      context != null -> {
        try {
          SharedPreferencesGoodRepository(context).getCurrentMessage()
        } catch (_: Throwable) {
          inMemoryFallbackMessage
        }
      }
      else -> inMemoryFallbackMessage
    }
    val palette = TodoListWidgets.resolveWidgetPalette(context)
    return GoodWidgetState(
      currentMessage = currentMessage,
      palette = palette,
    )
  }

  /**
   * Advances to a new positive message, persists it, and refreshes all Good widgets.
   */
  fun refreshGoodMessage(
    context: Context? = null,
    repository: GoodRepository? = null,
    random: Random = Random.Default,
    refreshWidgets: Boolean = true,
  ): String {
    val nextMessage = when {
      repository != null -> repository.refreshMessage(random)
      context != null -> {
        try {
          SharedPreferencesGoodRepository(context).refreshMessage(random)
        } catch (_: Throwable) {
          val next = pickNextGoodMessage(currentMessage = inMemoryFallbackMessage, random = random)
          inMemoryFallbackMessage = next
          next
        }
      }
      else -> {
        val next = pickNextGoodMessage(currentMessage = inMemoryFallbackMessage, random = random)
        inMemoryFallbackMessage = next
        next
      }
    }
    inMemoryFallbackMessage = nextMessage
    if (refreshWidgets && context != null) {
      updateAllWidgets(context)
    }
    return nextMessage
  }

  fun renderWidgetBackgroundBitmap(
    palette: TodoWidgetPalette,
    widthPx: Int = 440,
    heightPx: Int = 144,
    cornerRadiusPx: Float = 32f,
    borderWidthPx: Float = 2f,
  ): Bitmap? {
    return TodoListWidgets.renderWidgetBackgroundBitmap(
      palette = palette,
      widthPx = widthPx,
      heightPx = heightPx,
      cornerRadiusPx = cornerRadiusPx,
      borderWidthPx = borderWidthPx,
    )
  }

  fun buildOpenGoodToolPendingIntent(context: Context): PendingIntent {
    val intent = createStandaloneToolIntent(context, Tool.GOOD)
    return PendingIntent.getActivity(
      context,
      REQUEST_CODE_OPEN_GOOD,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  fun buildNextMessagePendingIntent(context: Context): PendingIntent {
    val intent = Intent(context, GoodWidgetProvider::class.java).apply {
      action = ACTION_WIDGET_NEXT_MESSAGE
    }
    return PendingIntent.getBroadcast(
      context,
      REQUEST_CODE_NEXT_MESSAGE,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  /**
   * Builds the [RemoteViews] for a [GoodWidgetProvider] instance.
   */
  fun buildWidgetRemoteViews(
    context: Context,
    appWidgetId: Int,
    state: GoodWidgetState = resolveWidgetState(context),
    options: Bundle? = null,
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_good)
    val palette = state.palette
    val density = try {
      context.resources.displayMetrics.density.coerceIn(1f, 4f)
    } catch (_: Throwable) {
      2f
    }

    val (widthDp, heightDp) = MusicPlayerWidgets.resolveWidgetSizeDp(
      context = context,
      options = options,
      defaultWidthDp = 240f,
      defaultHeightDp = 72f,
    )
    val rawWidthPx = (widthDp * density).coerceAtLeast(140f)
    val rawHeightPx = (heightDp * density).coerceAtLeast(100f)
    val maxDimPx = 900f
    val maxRaw = max(rawWidthPx, rawHeightPx)
    val scaleFactor = if (maxRaw > maxDimPx) maxDimPx / maxRaw else 1f
    val bgWidthPx = (rawWidthPx * scaleFactor).roundToInt().coerceAtLeast(140)
    val bgHeightPx = (rawHeightPx * scaleFactor).roundToInt().coerceAtLeast(100)

    renderWidgetBackgroundBitmap(
      palette = palette,
      widthPx = bgWidthPx,
      heightPx = bgHeightPx,
      cornerRadiusPx = WIDGET_CORNER_RADIUS_DP * density * scaleFactor,
      borderWidthPx = max(1f, 1f * density * scaleFactor),
    )?.let { bgBmp ->
      views.setImageViewBitmap(R.id.widget_good_background, bgBmp)
    }

    // Left: Good positive message
    views.setTextViewText(R.id.widget_good_message_text, state.currentMessage)
    views.setTextColor(R.id.widget_good_message_text, palette.onSurfaceArgb)

    // Right: Refresh button (circle or vertical-sided hexagon when Hexagonify is enabled)
    views.setImageViewResource(R.id.widget_good_refresh_button_bg, state.iconButtonBackgroundRes)
    views.setInt(R.id.widget_good_refresh_button_bg, "setColorFilter", palette.primaryContainerArgb)
    views.setInt(R.id.widget_good_refresh_button_icon, "setColorFilter", palette.primaryArgb)

    val openIntent = buildOpenGoodToolPendingIntent(context)
    val nextMessageIntent = buildNextMessagePendingIntent(context)

    views.setOnClickPendingIntent(R.id.widget_good_root, openIntent)
    views.setOnClickPendingIntent(R.id.widget_good_message_text, openIntent)
    views.setOnClickPendingIntent(R.id.widget_good_refresh_button, nextMessageIntent)
    views.setOnClickPendingIntent(R.id.widget_good_refresh_button_icon, nextMessageIntent)

    return views
  }

  /**
   * Handles a widget broadcast action for [GoodWidgetProvider].
   */
  fun handleWidgetAction(
    context: Context?,
    intent: Intent?,
    repository: GoodRepository? = null,
    random: Random = Random.Default,
  ): Boolean = handleWidgetAction(
    context = context,
    action = intent?.action,
    repository = repository,
    random = random,
  )

  fun handleWidgetAction(
    context: Context?,
    action: String?,
    repository: GoodRepository? = null,
    random: Random = Random.Default,
  ): Boolean {
    if (action.isNullOrBlank()) return false
    return when (action) {
      ACTION_WIDGET_REFRESH -> {
        if (context != null) updateAllWidgets(context)
        true
      }
      ACTION_WIDGET_NEXT_MESSAGE -> {
        refreshGoodMessage(
          context = context,
          repository = repository,
          random = random,
          refreshWidgets = true,
        )
        true
      }
      else -> false
    }
  }

  /**
   * Refreshes all pinned instances of [GoodWidgetProvider].
   */
  fun updateAllWidgets(context: Context?) {
    val appCtx = context?.applicationContext ?: context ?: return
    try {
      val manager = AppWidgetManager.getInstance(appCtx) ?: return
      val component = ComponentName(appCtx, GoodWidgetProvider::class.java)
      val widgetIds = try {
        manager.getAppWidgetIds(component) ?: IntArray(0)
      } catch (_: Throwable) {
        IntArray(0)
      }
      if (widgetIds.isEmpty()) return
      updateGoodWidgets(appCtx, manager, widgetIds)
    } catch (_: Throwable) {}
  }

  fun updateGoodWidgets(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
    state: GoodWidgetState = resolveWidgetState(context),
  ) {
    for (appWidgetId in appWidgetIds) {
      try {
        val options = try {
          appWidgetManager.getAppWidgetOptions(appWidgetId)
        } catch (_: Throwable) {
          null
        }
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
 * Compact Good Home Screen AppWidget displaying the current positive message on the left and a
 * refresh button on the right.
 */
class GoodWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    GoodWidgets.updateGoodWidgets(context, appWidgetManager, appWidgetIds)
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle,
  ) {
    GoodWidgets.updateGoodWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
  }

  override fun onReceive(context: Context, intent: Intent) {
    if (GoodWidgets.handleWidgetAction(context, intent)) {
      return
    }
    super.onReceive(context, intent)
  }
}
