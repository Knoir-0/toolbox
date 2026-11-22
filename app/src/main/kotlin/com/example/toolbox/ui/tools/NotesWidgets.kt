package com.example.toolbox.ui.tools

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.toolbox.R
import com.example.toolbox.ui.main.NOTES_FILTER_MODE_ALL
import com.example.toolbox.ui.main.NOTES_FILTER_MODE_LABEL
import com.example.toolbox.ui.main.NOTES_FILTER_MODE_PINNED
import com.example.toolbox.ui.main.createNotesOpenIntent
import com.example.toolbox.ui.main.createNotesQuickCaptureIntent
import com.example.toolbox.ui.main.rememberNavigationPreferencesRepository
import com.example.toolbox.ui.theme.CircleShape
import com.example.toolbox.ui.theme.ToolboxTheme
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Filter option chosen when placing or reconfiguring a Notes Home Screen AppWidget:
 * - [ALL]: Display all active (non-archived, non-trashed) notes
 * - [PINNED]: Display only pinned notes
 * - [LABEL]: Display notes matching a specific label
 */
enum class NotesWidgetFilterMode(
  val storageValue: String,
  val displayTitle: String,
) {
  ALL(NOTES_FILTER_MODE_ALL, "All Notes"),
  PINNED(NOTES_FILTER_MODE_PINNED, "Pinned Notes"),
  LABEL(NOTES_FILTER_MODE_LABEL, "Label"),
  ;

  companion object {
    fun fromStorageValue(raw: String?): NotesWidgetFilterMode {
      if (raw.isNullOrBlank()) return ALL
      return entries.firstOrNull { it.storageValue.equals(raw.trim(), ignoreCase = true) }
        ?: entries.firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) }
        ?: ALL
    }
  }
}

/**
 * Per-widget configuration persisted for a [NotesWidgetProvider] instance.
 */
@Immutable
data class NotesWidgetConfig(
  val filterMode: NotesWidgetFilterMode = NotesWidgetFilterMode.ALL,
  val labelName: String? = null,
) {
  val normalizedLabelName: String?
    get() = labelName?.trim()?.takeIf { it.isNotEmpty() }

  val effectiveFilterMode: NotesWidgetFilterMode
    get() = if (filterMode == NotesWidgetFilterMode.LABEL && normalizedLabelName == null) {
      NotesWidgetFilterMode.ALL
    } else {
      filterMode
    }

  fun toNotesFilter(): NotesFilter = when (effectiveFilterMode) {
    NotesWidgetFilterMode.ALL -> NotesFilter.All
    NotesWidgetFilterMode.PINNED -> NotesFilter.Pinned
    NotesWidgetFilterMode.LABEL -> NotesFilter.Label(normalizedLabelName!!)
  }

  val headerTitle: String
    get() = when (effectiveFilterMode) {
      NotesWidgetFilterMode.ALL -> "Notes"
      NotesWidgetFilterMode.PINNED -> "Pinned Notes"
      NotesWidgetFilterMode.LABEL -> normalizedLabelName ?: "Notes"
    }

  val filterPillText: String
    get() = when (effectiveFilterMode) {
      NotesWidgetFilterMode.ALL -> "All"
      NotesWidgetFilterMode.PINNED -> "Pinned"
      NotesWidgetFilterMode.LABEL -> normalizedLabelName ?: "All"
    }

  val emptyStateTitle: String
    get() = when (effectiveFilterMode) {
      NotesWidgetFilterMode.ALL -> "No notes yet"
      NotesWidgetFilterMode.PINNED -> "No pinned notes"
      NotesWidgetFilterMode.LABEL -> "No notes in \"${normalizedLabelName ?: ""}\""
    }

  fun toJson(): JSONObject = JSONObject().apply {
    put("filterMode", effectiveFilterMode.storageValue)
    if (normalizedLabelName != null) {
      put("labelName", normalizedLabelName)
    }
  }

  companion object {
    fun fromJson(json: JSONObject?): NotesWidgetConfig {
      if (json == null) return NotesWidgetConfig()
      val mode = NotesWidgetFilterMode.fromStorageValue(json.optString("filterMode", NOTES_FILTER_MODE_ALL))
      val label = json.optString("labelName", "").trim().takeIf { it.isNotEmpty() }
      return NotesWidgetConfig(
        filterMode = if (mode == NotesWidgetFilterMode.LABEL && label == null) {
          NotesWidgetFilterMode.ALL
        } else {
          mode
        },
        labelName = if (mode == NotesWidgetFilterMode.LABEL) label else null,
      )
    }
  }
}

/**
 * Snapshot of the filtered notes list, header metadata, and theme palette rendered by the Notes
 * Home Screen AppWidget ([NotesWidgetProvider]).
 */
@Immutable
data class NotesWidgetState(
  val config: NotesWidgetConfig,
  val visibleNotes: List<NoteItem>,
  val totalVisibleCount: Int,
  val pinnedVisibleCount: Int,
  val headerTitle: String,
  val headerSubtitle: String,
  val filterPillText: String,
  val emptyStateTitle: String,
  val emptyStateSubtitle: String,
  val palette: TodoWidgetPalette,
  val hexagonIconButtons: Boolean = palette.hexagonIconButtons,
  val iconButtonBackgroundRes: Int = palette.iconButtonBackgroundRes,
)

/**
 * Central state resolver, per-widget filter configuration store, RemoteViews builder, and action
 * handler for the Notes Home Screen AppWidget ([NotesWidgetProvider]):
 * - Allows the user to choose between **All Notes**, **Pinned Notes**, and a **Specific Label**
 *   when placing or reconfiguring the widget on the home screen ([NotesWidgetConfigureActivity]).
 * - Displays the scrollable list of matching notes (titles, content or checklist previews,
 *   checklist completion badges, pin indicators, labels, and timestamps).
 * - Features the two floating circular FABs at the bottom-right: one for Plus (`+`) and one for
 *   Checklist, opening the Notes tool directly to taking a new note or a new checklist (pre-populated
 *   with the widget's pinned or label context).
 */
object NotesWidgets {

  const val ACTION_WIDGET_REFRESH = "com.example.toolbox.notes.ACTION_WIDGET_REFRESH"
  const val ACTION_WIDGET_ITEM_CLICK = "com.example.toolbox.notes.ACTION_WIDGET_ITEM_CLICK"
  const val ACTION_WIDGET_OPEN_NEW_NOTE = "com.example.toolbox.notes.ACTION_WIDGET_OPEN_NEW_NOTE"
  const val ACTION_WIDGET_OPEN_NEW_CHECKLIST = "com.example.toolbox.notes.ACTION_WIDGET_OPEN_NEW_CHECKLIST"

  const val EXTRA_NOTE_ID = "extra_notes_widget_note_id"
  const val EXTRA_FILTER_MODE = "extra_notes_widget_filter_mode"
  const val EXTRA_FILTER_LABEL = "extra_notes_widget_filter_label"

  const val KEY_WIDGET_CONFIG_PREFIX = "notes_widget_config_"
  const val WIDGET_CORNER_RADIUS_DP = 16f

  private const val REQUEST_CODE_OPEN_NOTES_BASE = 4100
  private const val REQUEST_CODE_ITEM_TEMPLATE_BASE = 4200
  private const val REQUEST_CODE_FAB_NEW_NOTE_BASE = 4300
  private const val REQUEST_CODE_FAB_NEW_CHECKLIST_BASE = 4400

  private val inMemoryConfigs = mutableMapOf<Int, NotesWidgetConfig>()

  /**
   * Saves the [NotesWidgetConfig] for [appWidgetId].
   */
  fun saveWidgetConfig(
    context: Context?,
    appWidgetId: Int,
    config: NotesWidgetConfig,
  ) {
    synchronized(inMemoryConfigs) {
      inMemoryConfigs[appWidgetId] = config
    }
    if (context == null) return
    try {
      val prefs = context.getSharedPreferences(
        SharedPreferencesNotesRepository.PREFS_NAME,
        Context.MODE_PRIVATE,
      )
      prefs.edit()
        .putString("$KEY_WIDGET_CONFIG_PREFIX$appWidgetId", config.toJson().toString())
        .apply()
    } catch (_: Throwable) {}
  }

  /**
   * Loads the [NotesWidgetConfig] for [appWidgetId], defaulting to [NotesWidgetFilterMode.ALL].
   */
  fun loadWidgetConfig(
    context: Context?,
    appWidgetId: Int,
  ): NotesWidgetConfig {
    if (context != null) {
      try {
        val prefs = context.getSharedPreferences(
          SharedPreferencesNotesRepository.PREFS_NAME,
          Context.MODE_PRIVATE,
        )
        val raw = prefs.getString("$KEY_WIDGET_CONFIG_PREFIX$appWidgetId", null)
        if (!raw.isNullOrBlank()) {
          val parsed = NotesWidgetConfig.fromJson(JSONObject(raw))
          synchronized(inMemoryConfigs) {
            inMemoryConfigs[appWidgetId] = parsed
          }
          return parsed
        }
      } catch (_: Throwable) {}
    }
    return synchronized(inMemoryConfigs) {
      inMemoryConfigs[appWidgetId] ?: NotesWidgetConfig()
    }
  }

  /**
   * Deletes the stored [NotesWidgetConfig] when a widget instance is removed from the home screen.
   */
  fun deleteWidgetConfig(
    context: Context?,
    appWidgetId: Int,
  ) {
    synchronized(inMemoryConfigs) {
      inMemoryConfigs.remove(appWidgetId)
    }
    if (context == null) return
    try {
      val prefs = context.getSharedPreferences(
        SharedPreferencesNotesRepository.PREFS_NAME,
        Context.MODE_PRIVATE,
      )
      prefs.edit()
        .remove("$KEY_WIDGET_CONFIG_PREFIX$appWidgetId")
        .apply()
    } catch (_: Throwable) {}
  }

  /**
   * Clears in-memory widget configurations (used in unit tests).
   */
  fun clearInMemoryConfigs() {
    synchronized(inMemoryConfigs) {
      inMemoryConfigs.clear()
    }
  }

  /**
   * Resolves the available labels from [repository] (combining saved labels and labels used on
   * non-trashed notes, sorted alphabetically).
   */
  fun resolveAvailableLabels(
    context: Context? = null,
    repository: NotesRepository? = context?.let { SharedPreferencesNotesRepository(it) },
  ): List<String> {
    val notes = repository?.loadNotes().orEmpty()
    val savedLabels = repository?.loadLabels().orEmpty()
    return NotesEngine.extractAllLabels(notes, savedLabels)
  }

  /**
   * Resolves the current [NotesWidgetState] for [config] (or [appWidgetId]) from [repository].
   */
  fun resolveWidgetState(
    context: Context? = null,
    appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID,
    config: NotesWidgetConfig = loadWidgetConfig(context, appWidgetId),
    repository: NotesRepository? = context?.let { SharedPreferencesNotesRepository(it) },
  ): NotesWidgetState {
    val allNotes = repository?.loadNotes().orEmpty().filter { !it.isArchived && !it.isTrashed }
    val filtered = NotesEngine.filterNotes(
      notes = allNotes,
      filter = config.toNotesFilter(),
    )
    val visibleNotes = filtered.allNotes
    val pinnedCount = filtered.pinned.size
    val totalCount = visibleNotes.size
    val noteWord = if (totalCount == 1) "note" else "notes"

    val headerSubtitle = when (config.effectiveFilterMode) {
      NotesWidgetFilterMode.ALL -> {
        if (pinnedCount > 0) {
          "$totalCount $noteWord • $pinnedCount pinned"
        } else {
          "$totalCount $noteWord"
        }
      }
      NotesWidgetFilterMode.PINNED -> {
        "$totalCount pinned $noteWord"
      }
      NotesWidgetFilterMode.LABEL -> {
        if (pinnedCount > 0) {
          "$totalCount $noteWord • $pinnedCount pinned"
        } else {
          "$totalCount $noteWord"
        }
      }
    }

    return NotesWidgetState(
      config = config,
      visibleNotes = visibleNotes,
      totalVisibleCount = totalCount,
      pinnedVisibleCount = pinnedCount,
      headerTitle = config.headerTitle,
      headerSubtitle = headerSubtitle,
      filterPillText = config.filterPillText,
      emptyStateTitle = config.emptyStateTitle,
      emptyStateSubtitle = "Tap + or checklist to take a note",
      palette = TodoListWidgets.resolveWidgetPalette(context),
    )
  }

  /**
   * Builds the display title for a [NoteItem] row in the widget.
   */
  fun buildNoteItemTitle(note: NoteItem): String {
    if (note.title.isNotBlank()) return note.title.trim()
    if (note.isChecklist) {
      val firstItem = note.uncheckedItems.firstOrNull() ?: note.checklistItems.firstOrNull()
      if (firstItem != null && firstItem.text.isNotBlank()) {
        return firstItem.text.trim()
      }
      return "Checklist"
    }
    val firstLine = note.content.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() }
    return firstLine ?: "Untitled note"
  }

  /**
   * Builds the body preview text for a [NoteItem] row in the widget.
   */
  fun buildNoteItemBodyPreview(note: NoteItem): String {
    if (note.isChecklist) {
      val itemsToPreview = if (note.title.isNotBlank()) {
        note.uncheckedItems.ifEmpty { note.checklistItems }
      } else {
        val all = note.uncheckedItems.ifEmpty { note.checklistItems }
        if (all.size > 1) all.drop(1) else emptyList()
      }
      if (itemsToPreview.isEmpty()) {
        val checkedCount = note.checkedItems.size
        return if (checkedCount > 0) {
          val word = if (checkedCount == 1) "checked item" else "checked items"
          "+ $checkedCount $word"
        } else {
          ""
        }
      }
      return itemsToPreview.take(4).joinToString("\n") { item ->
        val mark = if (item.isChecked) "✓" else "☐"
        "$mark ${item.text.trim()}"
      }
    } else {
      val trimmedContent = note.content.trim()
      if (trimmedContent.isEmpty()) return ""
      return if (note.title.isNotBlank()) {
        trimmedContent
      } else {
        val lines = trimmedContent.lines()
        if (lines.size > 1) lines.drop(1).joinToString("\n").trim() else ""
      }
    }
  }

  /**
   * Formats the checklist progress badge (e.g. `"2/5"`) for a checklist [NoteItem], or `null` when
   * the note is not a checklist or has no checklist items.
   */
  fun formatChecklistBadgeText(note: NoteItem): String? {
    if (!note.isChecklist || note.checklistItems.isEmpty()) return null
    val checked = note.checkedItems.size
    return "$checked/${note.checklistItems.size}"
  }

  /**
   * Builds the bottom metadata line (labels + formatted timestamp) for a [NoteItem] row in the widget.
   */
  fun buildNoteItemMetadata(
    note: NoteItem,
    nowMillis: Long = System.currentTimeMillis(),
  ): String {
    val parts = mutableListOf<String>()
    if (note.labels.isNotEmpty()) {
      parts.add(note.labels.joinToString(", "))
    }
    parts.add(NotesEngine.formatTimestamp(note.updatedAt, nowMillis))
    return parts.joinToString(" • ")
  }

  fun buildOpenNotesPendingIntent(
    context: Context,
    appWidgetId: Int,
    config: NotesWidgetConfig,
  ): PendingIntent {
    val intent = createNotesOpenIntent(
      context = context,
      filterMode = config.effectiveFilterMode.storageValue,
      label = config.normalizedLabelName,
    )
    return PendingIntent.getActivity(
      context,
      REQUEST_CODE_OPEN_NOTES_BASE + (appWidgetId and 0xFFFF),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  fun buildOpenNotesQuickCapturePendingIntent(
    context: Context,
    appWidgetId: Int,
    config: NotesWidgetConfig,
    isChecklist: Boolean,
  ): PendingIntent {
    val isPinned = config.effectiveFilterMode == NotesWidgetFilterMode.PINNED
    val label = if (config.effectiveFilterMode == NotesWidgetFilterMode.LABEL) {
      config.normalizedLabelName
    } else {
      null
    }
    val intent = createNotesQuickCaptureIntent(
      context = context,
      isChecklist = isChecklist,
      pinned = isPinned,
      label = label,
    )
    val baseCode = if (isChecklist) REQUEST_CODE_FAB_NEW_CHECKLIST_BASE else REQUEST_CODE_FAB_NEW_NOTE_BASE
    return PendingIntent.getActivity(
      context,
      baseCode + (appWidgetId and 0xFFFF),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  fun buildWidgetBroadcastPendingIntent(
    context: Context,
    action: String,
    requestCode: Int,
    mutable: Boolean = false,
  ): PendingIntent {
    val intent = Intent(context, NotesWidgetProvider::class.java).apply {
      this.action = action
      setPackage(context.packageName)
    }
    val mutabilityFlag = if (mutable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      PendingIntent.FLAG_MUTABLE
    } else if (mutable) {
      0
    } else {
      PendingIntent.FLAG_IMMUTABLE
    }
    return PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or mutabilityFlag,
    )
  }

  /**
   * Builds the [RemoteViews] for a single [NoteItem] card inside the widget's scrollable [ListView].
   */
  fun buildNoteItemRemoteViews(
    context: Context,
    note: NoteItem,
    config: NotesWidgetConfig = NotesWidgetConfig(),
    palette: TodoWidgetPalette = TodoListWidgets.resolveWidgetPalette(context),
    nowMillis: Long = System.currentTimeMillis(),
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_notes_item)
    val title = buildNoteItemTitle(note)
    views.setTextViewText(R.id.widget_notes_item_title, title)
    views.setTextColor(R.id.widget_notes_item_title, palette.onSurfaceArgb)

    if (note.isPinned) {
      views.setViewVisibility(R.id.widget_notes_item_pin_icon, View.VISIBLE)
      views.setInt(R.id.widget_notes_item_pin_icon, "setColorFilter", palette.primaryArgb)
    } else {
      views.setViewVisibility(R.id.widget_notes_item_pin_icon, View.GONE)
    }

    val checklistBadge = formatChecklistBadgeText(note)
    if (checklistBadge != null) {
      views.setViewVisibility(R.id.widget_notes_item_checklist_badge, View.VISIBLE)
      views.setTextViewText(R.id.widget_notes_item_checklist_badge, checklistBadge)
      views.setTextColor(R.id.widget_notes_item_checklist_badge, palette.onPrimaryContainerArgb)
    } else {
      views.setViewVisibility(R.id.widget_notes_item_checklist_badge, View.GONE)
    }

    val bodyPreview = buildNoteItemBodyPreview(note)
    if (bodyPreview.isNotBlank()) {
      views.setViewVisibility(R.id.widget_notes_item_body, View.VISIBLE)
      views.setTextViewText(R.id.widget_notes_item_body, bodyPreview)
      views.setTextColor(R.id.widget_notes_item_body, palette.onSurfaceVariantArgb)
    } else {
      views.setViewVisibility(R.id.widget_notes_item_body, View.GONE)
    }

    val metadata = buildNoteItemMetadata(note, nowMillis)
    views.setTextViewText(R.id.widget_notes_item_meta, metadata)
    views.setTextColor(R.id.widget_notes_item_meta, palette.outlineArgb)

    val openFillIn = Intent().apply {
      putExtra(EXTRA_NOTE_ID, note.id)
      putExtra(EXTRA_FILTER_MODE, config.effectiveFilterMode.storageValue)
      if (config.normalizedLabelName != null) {
        putExtra(EXTRA_FILTER_LABEL, config.normalizedLabelName)
      }
    }
    views.setOnClickFillInIntent(R.id.widget_notes_item_root, openFillIn)
    views.setOnClickFillInIntent(R.id.widget_notes_item_container, openFillIn)

    return views
  }

  /**
   * Builds the full [RemoteViews] for a [NotesWidgetProvider] instance.
   */
  fun buildWidgetRemoteViews(
    context: Context,
    appWidgetId: Int,
    state: NotesWidgetState = resolveWidgetState(context, appWidgetId),
    options: Bundle? = null,
  ): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_notes)
    val palette = state.palette
    val config = state.config
    val density = try {
      context.resources.displayMetrics.density.coerceIn(1f, 4f)
    } catch (_: Throwable) {
      2f
    }

    val (widthDp, heightDp) = MusicPlayerWidgets.resolveWidgetSizeDp(
      context = context,
      options = options,
      defaultWidthDp = 260f,
      defaultHeightDp = 260f,
    )
    val rawWidthPx = (widthDp * density).coerceAtLeast(120f)
    val rawHeightPx = (heightDp * density).coerceAtLeast(100f)
    val maxDimPx = 1100f
    val maxRaw = max(rawWidthPx, rawHeightPx)
    val scaleFactor = if (maxRaw > maxDimPx) maxDimPx / maxRaw else 1f
    val bgWidthPx = (rawWidthPx * scaleFactor).roundToInt().coerceAtLeast(120)
    val bgHeightPx = (rawHeightPx * scaleFactor).roundToInt().coerceAtLeast(100)

    TodoListWidgets.renderWidgetBackgroundBitmap(
      palette = palette,
      widthPx = bgWidthPx,
      heightPx = bgHeightPx,
      cornerRadiusPx = WIDGET_CORNER_RADIUS_DP * density * scaleFactor,
      borderWidthPx = max(1f, 1f * density * scaleFactor),
    )?.let { bgBmp ->
      views.setImageViewBitmap(R.id.widget_notes_background, bgBmp)
    }

    val headerIconRes = when (config.effectiveFilterMode) {
      NotesWidgetFilterMode.ALL -> R.drawable.ic_notes_widget_note
      NotesWidgetFilterMode.PINNED -> R.drawable.ic_notes_widget_pin
      NotesWidgetFilterMode.LABEL -> R.drawable.ic_notes_widget_label
    }

    // Header styling
    views.setImageViewResource(R.id.widget_notes_header_badge_bg, palette.iconButtonBackgroundRes)
    views.setInt(R.id.widget_notes_header_badge_bg, "setColorFilter", palette.primaryContainerArgb)
    views.setImageViewResource(R.id.widget_notes_header_badge_icon, headerIconRes)
    views.setInt(R.id.widget_notes_header_badge_icon, "setColorFilter", palette.primaryArgb)
    views.setTextViewText(R.id.widget_notes_title, state.headerTitle)
    views.setTextColor(R.id.widget_notes_title, palette.onSurfaceArgb)
    views.setTextViewText(R.id.widget_notes_subtext, state.headerSubtitle)
    views.setTextColor(R.id.widget_notes_subtext, palette.onSurfaceVariantArgb)
    views.setTextViewText(R.id.widget_notes_filter_pill, state.filterPillText)
    views.setTextColor(R.id.widget_notes_filter_pill, palette.onPrimaryContainerArgb)

    val openNotesIntent = buildOpenNotesPendingIntent(context, appWidgetId, config)
    views.setOnClickPendingIntent(R.id.widget_notes_header, openNotesIntent)
    views.setOnClickPendingIntent(R.id.widget_notes_empty_view, openNotesIntent)

    // Empty state styling
    views.setImageViewResource(R.id.widget_notes_empty_badge_bg, palette.iconButtonBackgroundRes)
    views.setInt(R.id.widget_notes_empty_badge_bg, "setColorFilter", palette.primaryContainerArgb)
    views.setImageViewResource(R.id.widget_notes_empty_badge_icon, headerIconRes)
    views.setInt(R.id.widget_notes_empty_badge_icon, "setColorFilter", palette.primaryArgb)
    views.setTextViewText(R.id.widget_notes_empty_title, state.emptyStateTitle)
    views.setTextColor(R.id.widget_notes_empty_title, palette.onSurfaceArgb)
    views.setTextViewText(R.id.widget_notes_empty_subtitle, state.emptyStateSubtitle)
    views.setTextColor(R.id.widget_notes_empty_subtitle, palette.onSurfaceVariantArgb)

    // Scrollable ListView adapter & empty view
    val serviceIntent = Intent(context, NotesWidgetRemoteViewsService::class.java).apply {
      putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
      putExtra(EXTRA_FILTER_MODE, config.effectiveFilterMode.storageValue)
      if (config.normalizedLabelName != null) {
        putExtra(EXTRA_FILTER_LABEL, config.normalizedLabelName)
      }
      data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
    }
    @Suppress("DEPRECATION")
    views.setRemoteAdapter(R.id.widget_notes_list_view, serviceIntent)
    views.setEmptyView(R.id.widget_notes_list_view, R.id.widget_notes_empty_view)

    val itemTemplatePendingIntent = buildWidgetBroadcastPendingIntent(
      context = context,
      action = ACTION_WIDGET_ITEM_CLICK,
      requestCode = REQUEST_CODE_ITEM_TEMPLATE_BASE + (appWidgetId and 0xFFFF),
      mutable = true,
    )
    views.setPendingIntentTemplate(R.id.widget_notes_list_view, itemTemplatePendingIntent)

    // Two Floating FABs at Bottom Right (removed from To-Do List widget and placed here):
    // 1. Checklist FAB -> opens Notes tool to take a new checklist
    views.setImageViewResource(
      R.id.widget_notes_fab_checklist_bg,
      palette.iconButtonBackgroundRes,
    )
    views.setInt(
      R.id.widget_notes_fab_checklist_bg,
      "setColorFilter",
      palette.secondaryContainerArgb,
    )
    views.setInt(
      R.id.widget_notes_fab_checklist,
      "setColorFilter",
      palette.onSecondaryContainerArgb,
    )
    views.setOnClickPendingIntent(
      R.id.widget_notes_fab_checklist,
      buildOpenNotesQuickCapturePendingIntent(context, appWidgetId, config, isChecklist = true),
    )

    // 2. Plus (+) FAB -> opens Notes tool to take a new note
    views.setImageViewResource(
      R.id.widget_notes_fab_plus_bg,
      palette.iconButtonBackgroundRes,
    )
    views.setInt(
      R.id.widget_notes_fab_plus_bg,
      "setColorFilter",
      palette.primaryContainerArgb,
    )
    views.setInt(
      R.id.widget_notes_fab_plus,
      "setColorFilter",
      palette.onPrimaryContainerArgb,
    )
    views.setOnClickPendingIntent(
      R.id.widget_notes_fab_plus,
      buildOpenNotesQuickCapturePendingIntent(context, appWidgetId, config, isChecklist = false),
    )

    return views
  }

  /**
   * Handles a widget broadcast action from [NotesWidgetProvider].
   */
  fun handleWidgetAction(
    context: Context?,
    intent: Intent?,
  ): Boolean {
    val action = intent?.action ?: return false
    val filterModeRaw = intent.getStringExtra(EXTRA_FILTER_MODE)
    val filterLabel = intent.getStringExtra(EXTRA_FILTER_LABEL)?.trim()?.takeIf { it.isNotEmpty() }
    val config = NotesWidgetConfig(
      filterMode = NotesWidgetFilterMode.fromStorageValue(filterModeRaw),
      labelName = filterLabel,
    )
    val isPinned = config.effectiveFilterMode == NotesWidgetFilterMode.PINNED
    val label = if (config.effectiveFilterMode == NotesWidgetFilterMode.LABEL) {
      config.normalizedLabelName
    } else {
      null
    }

    return when (action) {
      ACTION_WIDGET_REFRESH -> {
        if (context != null) updateAllWidgets(context)
        true
      }
      ACTION_WIDGET_ITEM_CLICK -> {
        val noteId = intent.getStringExtra(EXTRA_NOTE_ID)?.takeIf { it.isNotBlank() }
        NotesEngine.requestWidgetNavigation(
          filter = config.toNotesFilter(),
          openNoteId = noteId,
        )
        if (context != null) {
          try {
            context.startActivity(
              createNotesOpenIntent(
                context = context,
                filterMode = config.effectiveFilterMode.storageValue,
                label = config.normalizedLabelName,
                noteId = noteId,
              ),
            )
          } catch (_: Throwable) {}
        }
        true
      }
      ACTION_WIDGET_OPEN_NEW_NOTE -> {
        NotesEngine.requestQuickCapture(
          isChecklist = false,
          initialPinned = isPinned,
          initialLabel = label,
        )
        if (context != null) {
          try {
            context.startActivity(
              createNotesQuickCaptureIntent(
                context = context,
                isChecklist = false,
                pinned = isPinned,
                label = label,
              ),
            )
          } catch (_: Throwable) {}
        }
        true
      }
      ACTION_WIDGET_OPEN_NEW_CHECKLIST -> {
        NotesEngine.requestQuickCapture(
          isChecklist = true,
          initialPinned = isPinned,
          initialLabel = label,
        )
        if (context != null) {
          try {
            context.startActivity(
              createNotesQuickCaptureIntent(
                context = context,
                isChecklist = true,
                pinned = isPinned,
                label = label,
              ),
            )
          } catch (_: Throwable) {}
        }
        true
      }
      else -> false
    }
  }

  /**
   * Updates all pinned instances of [NotesWidgetProvider] on the user's home screen and notifies
   * their scrollable [ListView] adapters that note data changed.
   */
  fun updateAllWidgets(context: Context?) {
    val appCtx = context?.applicationContext ?: context ?: return
    try {
      val manager = AppWidgetManager.getInstance(appCtx) ?: return
      val component = ComponentName(appCtx, NotesWidgetProvider::class.java)
      val widgetIds = try {
        manager.getAppWidgetIds(component) ?: IntArray(0)
      } catch (_: Throwable) {
        IntArray(0)
      }
      if (widgetIds.isEmpty()) return
      updateNotesWidgets(appCtx, manager, widgetIds)
    } catch (_: Throwable) {}
  }

  /**
   * Updates the specified [appWidgetIds] for [NotesWidgetProvider].
   */
  fun updateNotesWidgets(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    val repo = SharedPreferencesNotesRepository(context)
    for (appWidgetId in appWidgetIds) {
      try {
        val options = try {
          appWidgetManager.getAppWidgetOptions(appWidgetId)
        } catch (_: Throwable) {
          null
        }
        val config = loadWidgetConfig(context, appWidgetId)
        val state = resolveWidgetState(
          context = context,
          appWidgetId = appWidgetId,
          config = config,
          repository = repo,
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
      appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_notes_list_view)
    } catch (_: Throwable) {}
  }
}

/**
 * Notes Home Screen AppWidget:
 * - Configurable when placed on the home screen (or reconfigured later) to show **All Notes**,
 *   **Pinned Notes**, or a **Specific Label** for notes ([NotesWidgetConfigureActivity]).
 * - Displays the scrollable list of matching notes with titles, body/checklist previews, checklist
 *   completion badges, pin indicators, labels, and timestamps.
 * - Features two floating FABs at the bottom-right (`+` and Checklist) that launch the Notes tool
 *   directly into taking a new text note or checklist.
 */
class NotesWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    NotesWidgets.updateNotesWidgets(context, appWidgetManager, appWidgetIds)
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle,
  ) {
    NotesWidgets.updateNotesWidgets(context, appWidgetManager, intArrayOf(appWidgetId))
  }

  override fun onDeleted(
    context: Context,
    appWidgetIds: IntArray,
  ) {
    super.onDeleted(context, appWidgetIds)
    for (appWidgetId in appWidgetIds) {
      NotesWidgets.deleteWidgetConfig(context, appWidgetId)
    }
  }

  override fun onReceive(context: Context, intent: Intent) {
    super.onReceive(context, intent)
    val action = intent.action ?: return
    if (action.startsWith("com.example.toolbox.notes.ACTION_")) {
      NotesWidgets.handleWidgetAction(context, intent)
    }
  }
}

/**
 * [RemoteViewsService] that powers the scrollable [ListView] inside [NotesWidgetProvider].
 */
class NotesWidgetRemoteViewsService : RemoteViewsService() {
  override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
    val appWidgetId = intent.getIntExtra(
      AppWidgetManager.EXTRA_APPWIDGET_ID,
      AppWidgetManager.INVALID_APPWIDGET_ID,
    )
    return NotesWidgetRemoteViewsFactory(applicationContext, appWidgetId)
  }
}

internal class NotesWidgetRemoteViewsFactory(
  private val context: Context,
  private val appWidgetId: Int,
) : RemoteViewsService.RemoteViewsFactory {

  private var notes: List<NoteItem> = emptyList()
  private var config: NotesWidgetConfig = NotesWidgetConfig()
  private var palette: TodoWidgetPalette = TodoListWidgets.resolveWidgetPalette(context)

  override fun onCreate() {
    reloadNotes()
  }

  override fun onDataSetChanged() {
    reloadNotes()
  }

  private fun reloadNotes() {
    try {
      val repo = SharedPreferencesNotesRepository(context)
      config = NotesWidgets.loadWidgetConfig(context, appWidgetId)
      val state = NotesWidgets.resolveWidgetState(
        context = context,
        appWidgetId = appWidgetId,
        config = config,
        repository = repo,
      )
      notes = state.visibleNotes
      palette = state.palette
    } catch (_: Throwable) {
      notes = emptyList()
    }
  }

  override fun onDestroy() {
    notes = emptyList()
  }

  override fun getCount(): Int = notes.size

  override fun getViewAt(position: Int): RemoteViews? {
    val item = notes.getOrNull(position) ?: return null
    return NotesWidgets.buildNoteItemRemoteViews(
      context = context,
      note = item,
      config = config,
      palette = palette,
    )
  }

  override fun getLoadingView(): RemoteViews? = null

  override fun getViewTypeCount(): Int = 1

  override fun getItemId(position: Int): Long {
    return notes.getOrNull(position)?.id?.hashCode()?.toLong() ?: position.toLong()
  }

  override fun hasStableIds(): Boolean = true
}

/**
 * Configuration Activity launched by the Android Home Screen launcher when placing or reconfiguring
 * a [NotesWidgetProvider] widget, allowing the user to choose between:
 * - **All Notes**
 * - **Pinned Notes**
 * - **Specific Label** (selecting an existing label or creating a new label)
 */
class NotesWidgetConfigureActivity : ComponentActivity() {

  private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    appWidgetId = intent?.extras?.getInt(
      AppWidgetManager.EXTRA_APPWIDGET_ID,
      AppWidgetManager.INVALID_APPWIDGET_ID,
    ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

    // Default result is RESULT_CANCELED in case the user backs out before confirming
    val cancelResult = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    setResult(RESULT_CANCELED, cancelResult)

    if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
      finish()
      return
    }

    val repository = SharedPreferencesNotesRepository(this)
    val initialConfig = NotesWidgets.loadWidgetConfig(this, appWidgetId)

    enableEdgeToEdge()
    setContent {
      val preferencesRepository = rememberNavigationPreferencesRepository()
      val themeMode by preferencesRepository.themeModeFlow.collectAsState()
      val themeColor by preferencesRepository.themeColorFlow.collectAsState()
      val musicPlayerThemeEnabled by preferencesRepository.musicPlayerThemeEnabledFlow.collectAsState()
      val musicPlayerColorOption by preferencesRepository.musicPlayerColorOptionFlow.collectAsState()
      val accentColor by preferencesRepository.accentColorFlow.collectAsState()
      val backgroundTexture by preferencesRepository.backgroundTextureFlow.collectAsState()
      val hexagonIconButtons by preferencesRepository.hexagonIconButtonsFlow.collectAsState()

      ToolboxTheme(
        themeMode = themeMode,
        themeColor = themeColor,
        accentColor = accentColor,
        musicPlayerThemeEnabled = musicPlayerThemeEnabled,
        musicPlayerColorOption = musicPlayerColorOption,
        backgroundTexture = backgroundTexture,
        hexagonIconButtons = hexagonIconButtons,
      ) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = Color.Black.copy(alpha = 0.45f),
        ) {
          NotesWidgetConfigureDialog(
            initialConfig = initialConfig,
            repository = repository,
            onConfirm = { chosenConfig, newLabelsToPersist ->
              if (newLabelsToPersist.isNotEmpty()) {
                val mergedLabels = (repository.loadLabels() + newLabelsToPersist)
                  .map { it.trim() }
                  .filter { it.isNotEmpty() }
                  .distinct()
                repository.saveLabels(mergedLabels)
              }
              NotesWidgets.saveWidgetConfig(
                context = this@NotesWidgetConfigureActivity,
                appWidgetId = appWidgetId,
                config = chosenConfig,
              )
              val appWidgetManager = AppWidgetManager.getInstance(this@NotesWidgetConfigureActivity)
              NotesWidgets.updateNotesWidgets(
                context = this@NotesWidgetConfigureActivity,
                appWidgetManager = appWidgetManager,
                appWidgetIds = intArrayOf(appWidgetId),
              )
              val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
              setResult(RESULT_OK, resultValue)
              finish()
            },
            onDismiss = {
              finish()
            },
          )
        }
      }
    }
  }
}

/**
 * Material 3 configuration dialog (`28.dp` corner radius, `44.dp` `primaryContainer` circular icon
 * badge, explanatory subtitle, preview context card, and `CircleShape` action buttons per
 * `DESIGN_LANGUAGE.md` Section 5.2) allowing the user to select **All Notes**, **Pinned Notes**, or
 * a **Specific Label** when placing the Notes widget on the home screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotesWidgetConfigureDialog(
  initialConfig: NotesWidgetConfig = NotesWidgetConfig(),
  repository: NotesRepository,
  onConfirm: (NotesWidgetConfig, List<String>) -> Unit,
  onDismiss: () -> Unit,
) {
  val haptic = LocalHapticFeedback.current
  val allNotes = remember(repository) { repository.loadNotes() }
  var createdLabels by remember { mutableStateOf(listOf<String>()) }
  val availableLabels = remember(allNotes, repository, createdLabels) {
    NotesEngine.extractAllLabels(allNotes, repository.loadLabels() + createdLabels)
  }

  var selectedMode by remember { mutableStateOf(initialConfig.effectiveFilterMode) }
  var selectedLabel by remember(availableLabels) {
    mutableStateOf(
      initialConfig.normalizedLabelName
        ?: availableLabels.firstOrNull()
        ?: "",
    )
  }
  var newLabelInput by remember { mutableStateOf("") }
  var showNewLabelField by remember {
    mutableStateOf(availableLabels.isEmpty())
  }
  val newLabelFocusRequester = remember { FocusRequester() }

  LaunchedEffect(selectedMode, showNewLabelField) {
    if (selectedMode == NotesWidgetFilterMode.LABEL && showNewLabelField) {
      try {
        newLabelFocusRequester.requestFocus()
      } catch (_: Throwable) {}
    }
  }

  val activeNotesCount = remember(allNotes) {
    allNotes.count { !it.isArchived && !it.isTrashed }
  }
  val pinnedNotesCount = remember(allNotes) {
    allNotes.count { it.isPinned && !it.isArchived && !it.isTrashed }
  }
  val notesCountByLabel = remember(allNotes, availableLabels) {
    availableLabels.associateWith { label ->
      allNotes.count { note ->
        !note.isArchived && !note.isTrashed && note.labels.any { it.equals(label, ignoreCase = true) }
      }
    }
  }

  val effectiveLabel = selectedLabel.trim().ifEmpty { newLabelInput.trim() }
  val canConfirm = when (selectedMode) {
    NotesWidgetFilterMode.ALL,
    NotesWidgetFilterMode.PINNED -> true
    NotesWidgetFilterMode.LABEL -> effectiveLabel.isNotEmpty()
  }

  fun commitNewLabelInput() {
    val trimmed = newLabelInput.trim()
    if (trimmed.isEmpty()) return
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val existing = availableLabels.firstOrNull { it.equals(trimmed, ignoreCase = true) }
    if (existing != null) {
      selectedLabel = existing
    } else {
      createdLabels = createdLabels + trimmed
      selectedLabel = trimmed
    }
    newLabelInput = ""
    showNewLabelField = false
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("notes_widget_configure_dialog"),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.StickyNote2,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Configure Notes Widget",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Choose which notes to display on your Home Screen",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // 1. All Notes option
        NotesWidgetFilterOptionCard(
          title = "All Notes",
          subtitle = if (activeNotesCount == 1) {
            "Show all active notes (1 note)"
          } else {
            "Show all active notes ($activeNotesCount notes)"
          },
          icon = Icons.AutoMirrored.Filled.StickyNote2,
          selected = selectedMode == NotesWidgetFilterMode.ALL,
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            selectedMode = NotesWidgetFilterMode.ALL
          },
          testTag = "notes_widget_option_all",
        )

        // 2. Pinned Notes option
        NotesWidgetFilterOptionCard(
          title = "Pinned Notes",
          subtitle = if (pinnedNotesCount == 1) {
            "Show only pinned notes (1 pinned)"
          } else {
            "Show only pinned notes ($pinnedNotesCount pinned)"
          },
          icon = Icons.Default.PushPin,
          selected = selectedMode == NotesWidgetFilterMode.PINNED,
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            selectedMode = NotesWidgetFilterMode.PINNED
          },
          testTag = "notes_widget_option_pinned",
        )

        // 3. Specific Label option
        NotesWidgetFilterOptionCard(
          title = "Specific Label",
          subtitle = if (selectedMode == NotesWidgetFilterMode.LABEL && effectiveLabel.isNotEmpty()) {
            val count = notesCountByLabel[effectiveLabel] ?: 0
            val word = if (count == 1) "note" else "notes"
            "Filter by label \"$effectiveLabel\" ($count $word)"
          } else {
            "Show notes tagged with a specific label"
          },
          icon = Icons.AutoMirrored.Filled.Label,
          selected = selectedMode == NotesWidgetFilterMode.LABEL,
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            selectedMode = NotesWidgetFilterMode.LABEL
            if (selectedLabel.isBlank() && availableLabels.isNotEmpty()) {
              selectedLabel = availableLabels.first()
            }
          },
          testTag = "notes_widget_option_label",
        )

        // Label picker section when Specific Label is selected
        if (selectedMode == NotesWidgetFilterMode.LABEL) {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ),
            border = BorderStroke(
              width = 1.dp,
              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("notes_widget_label_picker_card"),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "Select a label",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                if (!showNewLabelField) {
                  TextButton(
                    onClick = {
                      haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                      showNewLabelField = true
                    },
                    shape = CircleShape,
                    modifier = Modifier.testTag("notes_widget_new_label_toggle"),
                  ) {
                    Icon(
                      imageVector = Icons.Default.Add,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Label")
                  }
                }
              }

              if (availableLabels.isNotEmpty()) {
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp),
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  availableLabels.forEach { label ->
                    val isChipSelected = selectedLabel.equals(label, ignoreCase = true)
                    val count = notesCountByLabel[label] ?: 0
                    FilterChip(
                      selected = isChipSelected,
                      onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedLabel = label
                      },
                      shape = RoundedCornerShape(20.dp),
                      leadingIcon = {
                        Icon(
                          imageVector = if (isChipSelected) {
                            Icons.Default.Check
                          } else {
                            Icons.AutoMirrored.Filled.Label
                          },
                          contentDescription = null,
                          modifier = Modifier.size(16.dp),
                        )
                      },
                      label = {
                        Text(
                          text = if (count > 0) "$label ($count)" else label,
                          style = MaterialTheme.typography.labelMedium,
                          fontWeight = if (isChipSelected) FontWeight.Bold else FontWeight.Medium,
                        )
                      },
                      colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
                      ),
                      modifier = Modifier.testTag("notes_widget_label_chip_$label"),
                    )
                  }
                }
              }

              if (showNewLabelField || availableLabels.isEmpty()) {
                OutlinedTextField(
                  value = newLabelInput,
                  onValueChange = {
                    newLabelInput = it
                    if (it.trim().isNotEmpty()) {
                      selectedLabel = it.trim()
                    }
                  },
                  label = { Text("Label name") },
                  placeholder = { Text("e.g. Work, Groceries, Ideas") },
                  singleLine = true,
                  shape = RoundedCornerShape(14.dp),
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.Label,
                      contentDescription = null,
                    )
                  },
                  trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      if (newLabelInput.isNotEmpty()) {
                        IconButton(onClick = { newLabelInput = "" }) {
                          Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear label input",
                          )
                        }
                      }
                      if (newLabelInput.trim().isNotEmpty()) {
                        IconButton(
                          onClick = { commitNewLabelInput() },
                          modifier = Modifier.testTag("notes_widget_add_label_button"),
                        ) {
                          Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Use label",
                            tint = MaterialTheme.colorScheme.primary,
                          )
                        }
                      }
                    }
                  },
                  keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                  keyboardActions = KeyboardActions(
                    onDone = { commitNewLabelInput() },
                  ),
                  supportingText = if (effectiveLabel.isEmpty()) {
                    { Text("Enter or select a label to filter notes in this widget") }
                  } else {
                    null
                  },
                  modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(newLabelFocusRequester)
                    .testTag("notes_widget_new_label_input"),
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          val finalLabelsToSave = buildList {
            addAll(createdLabels)
            val typed = newLabelInput.trim()
            if (selectedMode == NotesWidgetFilterMode.LABEL && typed.isNotEmpty()) {
              add(typed)
            }
          }.distinct()
          val chosenConfig = NotesWidgetConfig(
            filterMode = selectedMode,
            labelName = if (selectedMode == NotesWidgetFilterMode.LABEL) effectiveLabel else null,
          )
          onConfirm(chosenConfig, finalLabelsToSave)
        },
        enabled = canConfirm,
        shape = CircleShape,
        modifier = Modifier.testTag("notes_widget_confirm_button"),
      ) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("Add Widget")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = CircleShape,
        modifier = Modifier.testTag("notes_widget_cancel_button"),
      ) {
        Text("Cancel")
      }
    },
  )
}

@Composable
private fun NotesWidgetFilterOptionCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  selected: Boolean,
  onClick: () -> Unit,
  testTag: String,
) {
  val containerColor = if (selected) {
    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
  } else {
    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
  }
  val borderColor = if (selected) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
  }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .testTag(testTag),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 56.dp)
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(
            if (selected) {
              MaterialTheme.colorScheme.primaryContainer
            } else {
              MaterialTheme.colorScheme.surfaceVariant
            },
          ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (selected) {
            MaterialTheme.colorScheme.primary
          } else {
            MaterialTheme.colorScheme.onSurfaceVariant
          },
          modifier = Modifier.size(18.dp),
        )
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Icon(
        imageVector = if (selected) {
          Icons.Default.RadioButtonChecked
        } else {
          Icons.Default.RadioButtonUnchecked
        },
        contentDescription = null,
        tint = if (selected) {
          MaterialTheme.colorScheme.primary
        } else {
          MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = Modifier.size(20.dp),
      )
    }
  }
}
