package com.example.toolbox.ui.tools

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Curated note card color options inspired by Google Keep.
 * Actual Compose surface colors are resolved dynamically in [NotesScreen] based on light/dark theme
 * and `MaterialTheme.colorScheme` tokens.
 */
enum class NoteColor(
  val displayName: String,
) {
  DEFAULT("Default"),
  CORAL("Coral"),
  PEACH("Peach"),
  SAND("Sand"),
  MINT("Mint"),
  SAGE("Sage"),
  FOG("Fog"),
  STORM("Storm"),
  DUSK("Dusk"),
  BLOSSOM("Blossom"),
  CLAY("Clay"),
}

/**
 * Represents a single interactive checkbox item inside a checklist-mode [NoteItem].
 */
data class NoteChecklistItem(
  val id: String = UUID.randomUUID().toString(),
  val text: String,
  val isChecked: Boolean = false,
) {
  fun toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("text", text)
    put("isChecked", isChecked)
  }

  companion object {
    fun fromJson(json: JSONObject): NoteChecklistItem = NoteChecklistItem(
      id = json.optString("id", UUID.randomUUID().toString()),
      text = json.optString("text", ""),
      isChecked = json.optBoolean("isChecked", false),
    )
  }
}

/**
 * Represents a single note in the Notes tool.
 * A note can be either a free-form text note ([isChecklist] = false) or an interactive
 * checklist ([isChecklist] = true).
 */
data class NoteItem(
  val id: String = UUID.randomUUID().toString(),
  val title: String = "",
  val content: String = "",
  val isChecklist: Boolean = false,
  val checklistItems: List<NoteChecklistItem> = emptyList(),
  val color: NoteColor = NoteColor.DEFAULT,
  val isPinned: Boolean = false,
  val isArchived: Boolean = false,
  val isTrashed: Boolean = false,
  val labels: List<String> = emptyList(),
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
) {
  val isEmpty: Boolean
    get() = title.isBlank() &&
      content.isBlank() &&
      checklistItems.all { it.text.isBlank() }

  val uncheckedItems: List<NoteChecklistItem>
    get() = checklistItems.filter { !it.isChecked }

  val checkedItems: List<NoteChecklistItem>
    get() = checklistItems.filter { it.isChecked }

  /**
   * Formats the note into plain text suitable for copying to clipboard.
   */
  fun toPlainText(): String {
    val sb = StringBuilder()
    if (title.isNotBlank()) {
      sb.append(title.trim())
    }
    if (isChecklist) {
      if (checklistItems.isNotEmpty()) {
        if (sb.isNotEmpty()) sb.append("\n\n")
        val lines = checklistItems.map { item ->
          val mark = if (item.isChecked) "[x]" else "[ ]"
          "$mark ${item.text}"
        }
        sb.append(lines.joinToString("\n"))
      }
    } else {
      if (content.isNotBlank()) {
        if (sb.isNotEmpty()) sb.append("\n\n")
        sb.append(content.trim())
      }
    }
    if (labels.isNotEmpty()) {
      if (sb.isNotEmpty()) sb.append("\n\n")
      sb.append(labels.joinToString(", "))
    }
    return sb.toString()
  }

  fun toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("title", title)
    put("content", content)
    put("isChecklist", isChecklist)
    val checklistArray = JSONArray()
    checklistItems.forEach { checklistArray.put(it.toJson()) }
    put("checklistItems", checklistArray)
    put("color", color.name)
    put("isPinned", isPinned)
    put("isArchived", isArchived)
    put("isTrashed", isTrashed)
    val labelsArray = JSONArray()
    labels.forEach { labelsArray.put(it) }
    put("labels", labelsArray)
    put("createdAt", createdAt)
    put("updatedAt", updatedAt)
  }

  companion object {
    fun fromJson(json: JSONObject): NoteItem {
      val itemsList = mutableListOf<NoteChecklistItem>()
      val checklistArray = json.optJSONArray("checklistItems")
      if (checklistArray != null) {
        for (i in 0 until checklistArray.length()) {
          val itemObj = checklistArray.optJSONObject(i)
          if (itemObj != null) {
            itemsList.add(NoteChecklistItem.fromJson(itemObj))
          }
        }
      }

      val labelsList = mutableListOf<String>()
      val labelsArray = json.optJSONArray("labels")
      if (labelsArray != null) {
        for (i in 0 until labelsArray.length()) {
          val label = labelsArray.optString(i, "").trim()
          if (label.isNotEmpty()) {
            labelsList.add(label)
          }
        }
      }

      val colorName = json.optString("color", NoteColor.DEFAULT.name)
      val parsedColor = try {
        NoteColor.valueOf(colorName)
      } catch (_: IllegalArgumentException) {
        NoteColor.DEFAULT
      }

      val now = System.currentTimeMillis()
      val created = json.optLong("createdAt", now)
      val updated = json.optLong("updatedAt", created)

      return NoteItem(
        id = json.optString("id", UUID.randomUUID().toString()),
        title = json.optString("title", ""),
        content = json.optString("content", ""),
        isChecklist = json.optBoolean("isChecklist", false),
        checklistItems = itemsList,
        color = parsedColor,
        isPinned = json.optBoolean("isPinned", false),
        isArchived = json.optBoolean("isArchived", false),
        isTrashed = json.optBoolean("isTrashed", false),
        labels = labelsList.distinct(),
        createdAt = created,
        updatedAt = updated,
      )
    }
  }
}

/**
 * Filter categories for browsing notes on the board.
 */
sealed interface NotesFilter {
  data object All : NotesFilter
  data object Pinned : NotesFilter
  data object Checklists : NotesFilter
  data class Label(val name: String) : NotesFilter
  data object Archived : NotesFilter
  data object Trash : NotesFilter
}

/**
 * Result of filtering and searching notes, partitioned into pinned and unpinned sections
 * when appropriate.
 */
data class FilteredNotesResult(
  val pinned: List<NoteItem>,
  val others: List<NoteItem>,
) {
  val totalCount: Int get() = pinned.size + others.size
  val isEmpty: Boolean get() = pinned.isEmpty() && others.isEmpty()
  val allNotes: List<NoteItem> get() = pinned + others
}

/**
 * Snapshot for Undo/Redo history in the Notes tool.
 */
data class NotesHistorySnapshot(
  val notes: List<NoteItem>,
  val labels: List<String> = emptyList(),
)

/**
 * Repository interface for persisting notes, custom labels, and layout preferences.
 */
interface NotesRepository {
  fun loadNotes(): List<NoteItem>
  fun saveNotes(notes: List<NoteItem>)
  fun loadLabels(): List<String>
  fun saveLabels(labels: List<String>)
  fun isGridView(): Boolean
  fun setGridView(isGrid: Boolean)
}

/**
 * In-memory implementation of [NotesRepository] for unit tests and Compose previews.
 */
class InMemoryNotesRepository(
  initialNotes: List<NoteItem> = emptyList(),
  initialLabels: List<String> = emptyList(),
  initialGridView: Boolean = true,
) : NotesRepository {
  private val notes = initialNotes.toMutableList()
  private val labels = initialLabels.toMutableList()
  private var gridView = initialGridView

  override fun loadNotes(): List<NoteItem> = notes.toList()

  override fun saveNotes(notes: List<NoteItem>) {
    this.notes.clear()
    this.notes.addAll(notes)
  }

  override fun loadLabels(): List<String> = labels.toList()

  override fun saveLabels(labels: List<String>) {
    this.labels.clear()
    this.labels.addAll(labels.map { it.trim() }.filter { it.isNotEmpty() }.distinct())
  }

  override fun isGridView(): Boolean = gridView

  override fun setGridView(isGrid: Boolean) {
    gridView = isGrid
  }
}

/**
 * Android [SharedPreferences]-backed implementation of [NotesRepository].
 */
class SharedPreferencesNotesRepository(
  private val sharedPreferences: SharedPreferences,
) : NotesRepository {

  companion object {
    const val PREFS_NAME = "notes_tool_preferences"
    const val KEY_NOTES_JSON = "notes_json"
    const val KEY_LABELS_JSON = "labels_json"
    const val KEY_IS_GRID_VIEW = "is_grid_view"
  }

  constructor(context: Context) : this(
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  )

  override fun loadNotes(): List<NoteItem> {
    val raw = sharedPreferences.getString(KEY_NOTES_JSON, null) ?: return emptyList()
    return try {
      val array = JSONArray(raw)
      val list = mutableListOf<NoteItem>()
      for (i in 0 until array.length()) {
        val obj = array.optJSONObject(i)
        if (obj != null) {
          list.add(NoteItem.fromJson(obj))
        }
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }

  override fun saveNotes(notes: List<NoteItem>) {
    val array = JSONArray()
    notes.forEach { array.put(it.toJson()) }
    sharedPreferences.edit().putString(KEY_NOTES_JSON, array.toString()).apply()
  }

  override fun loadLabels(): List<String> {
    val raw = sharedPreferences.getString(KEY_LABELS_JSON, null) ?: return emptyList()
    return try {
      val array = JSONArray(raw)
      val list = mutableListOf<String>()
      for (i in 0 until array.length()) {
        val label = array.optString(i, "").trim()
        if (label.isNotEmpty()) {
          list.add(label)
        }
      }
      list.distinct()
    } catch (_: Exception) {
      emptyList()
    }
  }

  override fun saveLabels(labels: List<String>) {
    val array = JSONArray()
    labels.map { it.trim() }.filter { it.isNotEmpty() }.distinct().forEach { array.put(it) }
    sharedPreferences.edit().putString(KEY_LABELS_JSON, array.toString()).apply()
  }

  override fun isGridView(): Boolean {
    return sharedPreferences.getBoolean(KEY_IS_GRID_VIEW, true)
  }

  override fun setGridView(isGrid: Boolean) {
    sharedPreferences.edit().putBoolean(KEY_IS_GRID_VIEW, isGrid).apply()
  }
}

/**
 * Pure business logic for creating, editing, converting, filtering, and organizing notes.
 */
object NotesEngine {

  /**
   * Formats a timestamp into a concise human-readable label (e.g. "Oct 14" or "3:45 PM" if today).
   */
  fun formatTimestamp(timestampMs: Long, nowMs: Long = System.currentTimeMillis()): String {
    val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val isSameDay = dayFormat.format(Date(timestampMs)) == dayFormat.format(Date(nowMs))
    val pattern = if (isSameDay) "h:mm a" else "MMM d"
    return SimpleDateFormat(pattern, Locale.US).format(Date(timestampMs))
  }

  /**
   * Adds or updates a note in [notes]. If the note is empty and is a newly created note, it is discarded
   * (matching Google Keep's "Empty note discarded" behavior).
   */
  fun saveOrDiscardNote(
    notes: List<NoteItem>,
    draft: NoteItem,
    nowMs: Long = System.currentTimeMillis(),
  ): List<NoteItem> {
    val cleanedItems = draft.checklistItems
      .map { it.copy(text = it.text.trim()) }
      .filter { it.text.isNotEmpty() }
    val sanitizedDraft = draft.copy(
      title = draft.title.trim(),
      content = if (draft.isChecklist) "" else draft.content.trimEnd(),
      checklistItems = if (draft.isChecklist) cleanedItems else emptyList(),
      labels = draft.labels.map { it.trim() }.filter { it.isNotEmpty() }.distinct(),
      updatedAt = nowMs,
    )

    val existingIndex = notes.indexOfFirst { it.id == sanitizedDraft.id }
    if (sanitizedDraft.isEmpty) {
      return if (existingIndex != -1) {
        notes.filterNot { it.id == sanitizedDraft.id }
      } else {
        notes
      }
    }

    return if (existingIndex != -1) {
      val mutable = notes.toMutableList()
      mutable[existingIndex] = sanitizedDraft
      mutable.toList()
    } else {
      listOf(sanitizedDraft.copy(createdAt = nowMs, updatedAt = nowMs)) + notes
    }
  }

  /**
   * Converts a [NoteItem] between plain text mode and interactive checklist mode.
   */
  fun toggleChecklistMode(note: NoteItem): NoteItem {
    return if (!note.isChecklist) {
      // Plain text -> Checklist: split non-blank lines into checklist items
      val lines = note.content
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
      val items = if (lines.isEmpty()) {
        emptyList()
      } else {
        lines.map { line ->
          when {
            line.startsWith("[x] ", ignoreCase = true) ->
              NoteChecklistItem(text = line.substring(4).trim(), isChecked = true)
            line.startsWith("[ ] ") ->
              NoteChecklistItem(text = line.substring(4).trim(), isChecked = false)
            line.startsWith("- ") || line.startsWith("* ") || line.startsWith("• ") ->
              NoteChecklistItem(text = line.substring(2).trim(), isChecked = false)
            else ->
              NoteChecklistItem(text = line, isChecked = false)
          }
        }
      }
      note.copy(
        isChecklist = true,
        checklistItems = items,
        content = "",
      )
    } else {
      // Checklist -> Plain text: join checklist items into lines
      val joinedContent = note.checklistItems
        .map { it.text.trim() }
        .filter { it.isNotEmpty() }
        .joinToString("\n")
      note.copy(
        isChecklist = false,
        content = joinedContent,
        checklistItems = emptyList(),
      )
    }
  }

  /**
   * Toggles the checked state of a specific checklist item within a note.
   */
  fun toggleChecklistItem(
    notes: List<NoteItem>,
    noteId: String,
    checklistItemId: String,
    nowMs: Long = System.currentTimeMillis(),
  ): List<NoteItem> {
    return notes.map { note ->
      if (note.id == noteId) {
        val updatedItems = note.checklistItems.map { item ->
          if (item.id == checklistItemId) item.copy(isChecked = !item.isChecked) else item
        }
        note.copy(checklistItems = updatedItems, updatedAt = nowMs)
      } else {
        note
      }
    }
  }

  /**
   * Unchecks all checklist items in [note].
   */
  fun uncheckAllItems(note: NoteItem): NoteItem {
    return note.copy(
      checklistItems = note.checklistItems.map { it.copy(isChecked = false) },
    )
  }

  /**
   * Deletes all checked items in [note].
   */
  fun deleteCheckedItems(note: NoteItem): NoteItem {
    return note.copy(
      checklistItems = note.checklistItems.filter { !it.isChecked },
    )
  }

  /**
   * Toggles pin status for [noteId]. Pinning an archived note automatically unarchives it.
   */
  fun togglePin(
    notes: List<NoteItem>,
    noteId: String,
    nowMs: Long = System.currentTimeMillis(),
  ): List<NoteItem> {
    return notes.map { note ->
      if (note.id == noteId) {
        val newPinned = !note.isPinned
        note.copy(
          isPinned = newPinned,
          isArchived = if (newPinned) false else note.isArchived,
          isTrashed = false,
          updatedAt = nowMs,
        )
      } else {
        note
      }
    }
  }

  /**
   * Sets pin status for a set of [noteIds].
   */
  fun setPinned(
    notes: List<NoteItem>,
    noteIds: Set<String>,
    pinned: Boolean,
    nowMs: Long = System.currentTimeMillis(),
  ): List<NoteItem> {
    return notes.map { note ->
      if (note.id in noteIds) {
        note.copy(
          isPinned = pinned,
          isArchived = if (pinned) false else note.isArchived,
          updatedAt = nowMs,
        )
      } else {
        note
      }
    }
  }

  /**
   * Toggles archive status for [noteId]. Archiving a pinned note automatically unpins it.
   */
  fun toggleArchive(
    notes: List<NoteItem>,
    noteId: String,
    nowMs: Long = System.currentTimeMillis(),
  ): List<NoteItem> {
    return notes.map { note ->
      if (note.id == noteId) {
        val newArchived = !note.isArchived
        note.copy(
          isArchived = newArchived,
          isPinned = if (newArchived) false else note.isPinned,
          updatedAt = nowMs,
        )
      } else {
        note
      }
    }
  }

  /**
   * Sets archive status for a set of [noteIds].
   */
  fun setArchived(
    notes: List<NoteItem>,
    noteIds: Set<String>,
    archived: Boolean,
    nowMs: Long = System.currentTimeMillis(),
  ): List<NoteItem> {
    return notes.map { note ->
      if (note.id in noteIds) {
        note.copy(
          isArchived = archived,
          isPinned = if (archived) false else note.isPinned,
          updatedAt = nowMs,
        )
      } else {
        note
      }
    }
  }

  /**
   * Moves the specified [noteIds] to the Trash (unpinning them).
   */
  fun moveToTrash(
    notes: List<NoteItem>,
    noteIds: Set<String>,
    nowMs: Long = System.currentTimeMillis(),
  ): List<NoteItem> {
    return notes.map { note ->
      if (note.id in noteIds) {
        note.copy(
          isTrashed = true,
          isPinned = false,
          isArchived = false,
          updatedAt = nowMs,
        )
      } else {
        note
      }
    }
  }

  /**
   * Restores the specified [noteIds] from Trash back to active notes.
   */
  fun restoreFromTrash(
    notes: List<NoteItem>,
    noteIds: Set<String>,
    nowMs: Long = System.currentTimeMillis(),
  ): List<NoteItem> {
    return notes.map { note ->
      if (note.id in noteIds) {
        note.copy(
          isTrashed = false,
          updatedAt = nowMs,
        )
      } else {
        note
      }
    }
  }

  /**
   * Permanently deletes the specified [noteIds].
   */
  fun deletePermanently(
    notes: List<NoteItem>,
    noteIds: Set<String>,
  ): List<NoteItem> {
    return notes.filterNot { it.id in noteIds }
  }

  /**
   * Permanently removes all trashed notes.
   */
  fun emptyTrash(notes: List<NoteItem>): List<NoteItem> {
    return notes.filterNot { it.isTrashed }
  }

  /**
   * Changes the card color for [noteIds].
   */
  fun setColor(
    notes: List<NoteItem>,
    noteIds: Set<String>,
    color: NoteColor,
    nowMs: Long = System.currentTimeMillis(),
  ): List<NoteItem> {
    return notes.map { note ->
      if (note.id in noteIds) {
        note.copy(color = color, updatedAt = nowMs)
      } else {
        note
      }
    }
  }

  /**
   * Creates a copy of [noteId] with a fresh ID and current timestamp, placed at the top of the list.
   */
  fun duplicateNote(
    notes: List<NoteItem>,
    noteId: String,
    nowMs: Long = System.currentTimeMillis(),
  ): List<NoteItem> {
    val source = notes.firstOrNull { it.id == noteId } ?: return notes
    val copy = source.copy(
      id = UUID.randomUUID().toString(),
      checklistItems = source.checklistItems.map { it.copy(id = UUID.randomUUID().toString()) },
      createdAt = nowMs,
      updatedAt = nowMs,
    )
    return listOf(copy) + notes
  }

  /**
   * Deletes a label globally from both custom labels and all notes.
   */
  fun deleteLabelGlobally(
    notes: List<NoteItem>,
    savedLabels: List<String>,
    labelToRemove: String,
  ): Pair<List<NoteItem>, List<String>> {
    val updatedNotes = notes.map { note ->
      if (note.labels.any { it.equals(labelToRemove, ignoreCase = true) }) {
        note.copy(labels = note.labels.filterNot { it.equals(labelToRemove, ignoreCase = true) })
      } else {
        note
      }
    }
    val updatedLabels = savedLabels.filterNot { it.equals(labelToRemove, ignoreCase = true) }
    return updatedNotes to updatedLabels
  }

  /**
   * Extracts all unique labels from both [savedLabels] and active/archived [notes], sorted alphabetically.
   */
  fun extractAllLabels(
    notes: List<NoteItem>,
    savedLabels: List<String> = emptyList(),
  ): List<String> {
    val fromNotes = notes
      .filterNot { it.isTrashed }
      .flatMap { it.labels }
    return (savedLabels + fromNotes)
      .map { it.trim() }
      .filter { it.isNotEmpty() }
      .distinctBy { it.lowercase(Locale.US) }
      .sortedWith(String.CASE_INSENSITIVE_ORDER)
  }

  /**
   * Moves the note with [fromNoteId] to the position of [toNoteId] in [notes].
   */
  fun reorderNotes(
    notes: List<NoteItem>,
    fromNoteId: String,
    toNoteId: String,
  ): List<NoteItem> {
    if (fromNoteId == toNoteId) return notes
    val fromIndex = notes.indexOfFirst { it.id == fromNoteId }
    val toIndex = notes.indexOfFirst { it.id == toNoteId }
    return reorderNotes(notes, fromIndex, toIndex)
  }

  /**
   * Moves a note from [fromIndex] to [toIndex] in [notes].
   */
  fun reorderNotes(
    notes: List<NoteItem>,
    fromIndex: Int,
    toIndex: Int,
  ): List<NoteItem> {
    if (fromIndex !in notes.indices || toIndex !in notes.indices || fromIndex == toIndex) {
      return notes
    }
    val mutable = notes.toMutableList()
    val moved = mutable.removeAt(fromIndex)
    mutable.add(toIndex, moved)
    return mutable.toList()
  }

  /**
   * Filters and searches [notes] according to [filter], [searchQuery], and [colorFilter],
   * preserving the arrangement order of [notes].
   */
  fun filterNotes(
    notes: List<NoteItem>,
    filter: NotesFilter = NotesFilter.All,
    searchQuery: String = "",
    colorFilter: NoteColor? = null,
  ): FilteredNotesResult {
    val query = searchQuery.trim()

    val matchesCategory = notes.filter { note ->
      when (filter) {
        NotesFilter.All -> !note.isArchived && !note.isTrashed
        NotesFilter.Pinned -> note.isPinned && !note.isArchived && !note.isTrashed
        NotesFilter.Checklists -> note.isChecklist && !note.isArchived && !note.isTrashed
        is NotesFilter.Label -> !note.isTrashed && note.labels.any { it.equals(filter.name, ignoreCase = true) }
        NotesFilter.Archived -> note.isArchived && !note.isTrashed
        NotesFilter.Trash -> note.isTrashed
      }
    }

    val matchesColor = if (colorFilter != null) {
      matchesCategory.filter { it.color == colorFilter }
    } else {
      matchesCategory
    }

    val matchesSearch = if (query.isEmpty()) {
      matchesColor
    } else {
      matchesColor.filter { note ->
        note.title.contains(query, ignoreCase = true) ||
          note.content.contains(query, ignoreCase = true) ||
          note.checklistItems.any { it.text.contains(query, ignoreCase = true) } ||
          note.labels.any { it.contains(query, ignoreCase = true) }
      }
    }

    // Partition into Pinned and Others when not viewing Archive or Trash or Pinned-only
    return when (filter) {
      NotesFilter.Archived, NotesFilter.Trash -> FilteredNotesResult(
        pinned = emptyList(),
        others = matchesSearch,
      )
      NotesFilter.Pinned -> FilteredNotesResult(
        pinned = matchesSearch,
        others = emptyList(),
      )
      else -> {
        val (pinned, others) = matchesSearch.partition { it.isPinned }
        FilteredNotesResult(pinned = pinned, others = others)
      }
    }
  }
}
