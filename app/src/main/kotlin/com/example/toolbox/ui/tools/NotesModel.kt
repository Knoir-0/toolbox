package com.example.toolbox.ui.tools

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

const val NOTES_FILE_EXTENSION = "text"
const val NOTES_FILE_DOT_EXTENSION = ".text"
const val NOTES_FILE_FORMAT_NAME = "Text"
const val NOTES_ROOT_FOLDER_NAME = "Notes"
const val NOTES_ROOT_DISPLAY_PATH = "/Notes"
private const val NOTES_EXTERNAL_STORAGE_DOCUMENTS_AUTHORITY = "com.android.externalstorage.documents"

/**
 * Represents a scoped storage folder used by the Notes tool (defaulting to `/Notes` in root storage).
 */
data class NotesFolder(
  val folderName: String = NOTES_ROOT_FOLDER_NAME,
  val displayPath: String = NOTES_ROOT_DISPLAY_PATH,
  val treeUri: String? = null,
  val customAbsolutePath: String? = null,
) {
  val folderPath: String get() = displayPath
  val pillLabel: String get() = folderName
}

val DEFAULT_NOTES_SCOPED_FOLDER = NotesFolder(
  folderName = NOTES_ROOT_FOLDER_NAME,
  displayPath = NOTES_ROOT_DISPLAY_PATH,
)


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
 * In `.text` files, unchecked items use `- <text>` and checked items replace the dash with `+`
 * internally (`+ <text>`), with nesting denoted by leading tab indentation (`"\t".repeat(indentLevel)`).
 */
data class NoteChecklistItem(
  val id: String = UUID.randomUUID().toString(),
  val text: String,
  val isChecked: Boolean = false,
  val indentLevel: Int = 0,
) {
  /**
   * Internal `.text` list marker: `-` when unchecked, `+` when checked.
   */
  val textPrefix: Char
    get() = if (isChecked) '+' else '-'

  /**
   * Formats this item as a `.text` checklist line (`- <text>` for unchecked, `+ <text>` for checked),
   * indented by 1 tab (`\t`) per [indentLevel] to denote checklist nesting.
   */
  fun toTextLine(): String {
    val indent = "\t".repeat(indentLevel.coerceAtLeast(0))
    return "$indent$textPrefix ${text.trim()}"
  }

  fun toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("text", text)
    put("isChecked", isChecked)
    if (indentLevel > 0) {
      put("indentLevel", indentLevel)
    }
    put("textLine", toTextLine())
  }

  companion object {
    fun fromTextLine(
      rawLine: String,
      id: String = UUID.randomUUID().toString(),
    ): NoteChecklistItem? {
      val indentLevel = NotesEngine.parseTextIndentLevel(rawLine)
      val trimmed = rawLine.trim()
      if (trimmed.isEmpty()) return null
      return when {
        trimmed.startsWith("+ ") -> {
          val itemText = trimmed.substring(2).trim()
          if (itemText.isNotEmpty()) {
            NoteChecklistItem(id = id, text = itemText, isChecked = true, indentLevel = indentLevel)
          } else null
        }
        trimmed.startsWith("- ") -> {
          val itemText = trimmed.substring(2).trim()
          if (itemText.isNotEmpty()) {
            NoteChecklistItem(id = id, text = itemText, isChecked = false, indentLevel = indentLevel)
          } else null
        }
        else -> null
      }
    }

    fun fromJson(json: JSONObject): NoteChecklistItem {
      val itemId = json.optString("id", UUID.randomUUID().toString())
      val rawTextLine = json.optString("textLine", "")
      if (rawTextLine.isNotBlank() && !json.has("text")) {
        fromTextLine(rawTextLine, id = itemId)?.let { return it }
      }
      return NoteChecklistItem(
        id = itemId,
        text = json.optString("text", ""),
        isChecked = json.optBoolean("isChecked", false),
        indentLevel = json.optInt("indentLevel", 0).coerceAtLeast(0),
      )
    }
  }
}

/**
 * Represents a single note in the Notes tool.
 * A note can be either a free-form Markdown text note ([isChecklist] = false) or an interactive
 * `.text` checklist ([isChecklist] = true).
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
   * Formats the note into `.text` file content:
   * - Markdown `# <title>` heading when [title] is non-blank.
   * - For checklists ([isChecklist] = `true`), `- <item>` for unchecked items (matching `.yaml` lists)
   *   and `+ <item>` for checked items (dash replaced with `+` internally).
   * - For text notes ([isChecklist] = `false`), Markdown body text.
   * - Labels on the last line as a `#Label Name 1, #Label Name 2, ...` CSV list when [labels] is non-empty.
   */
  fun toText(): String = NotesEngine.formatNoteToText(this)

  /**
   * Formats the note into `.text`-based plain text suitable for copying to clipboard or sharing,
   * without the internal `#` prefixes on labels.
   */
  fun toPlainText(): String {
    val sb = StringBuilder()
    val textBody = NotesEngine.formatNoteToText(this, includeLabels = false)
    if (textBody.isNotEmpty()) {
      sb.append(textBody)
    }
    val cleanedLabels = labels
      .map { NotesEngine.normalizeLabelName(it) }
      .filter { it.isNotEmpty() }
      .distinct()
    if (cleanedLabels.isNotEmpty()) {
      if (sb.isNotEmpty()) sb.append("\n\n")
      sb.append(cleanedLabels.joinToString(", "))
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
    put("text", toText())
    put("format", NOTES_FILE_EXTENSION)
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
    fun fromText(
      rawText: String,
      fallbackTitle: String = "",
      id: String = UUID.randomUUID().toString(),
      nowMs: Long = System.currentTimeMillis(),
      forceChecklist: Boolean? = null,
    ): NoteItem = NotesEngine.parseNoteFromText(
      rawText = rawText,
      fallbackTitle = fallbackTitle,
      id = id,
      nowMs = nowMs,
      forceChecklist = forceChecklist,
    )

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
          val label = NotesEngine.normalizeLabelName(labelsArray.optString(i, ""))
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
      val id = json.optString("id", UUID.randomUUID().toString())
      val hasExplicitFields = json.has("title") || json.has("content") || checklistArray != null
      val rawText = json.optString("text", "")
      if (!hasExplicitFields && rawText.isNotEmpty()) {
        val parsed = fromText(
          rawText = rawText,
          id = id,
          nowMs = updated,
          forceChecklist = if (json.has("isChecklist")) json.optBoolean("isChecklist") else null,
        )
        return parsed.copy(
          color = parsedColor,
          isPinned = json.optBoolean("isPinned", false),
          isArchived = json.optBoolean("isArchived", false),
          isTrashed = json.optBoolean("isTrashed", false),
          labels = (labelsList + parsed.labels).distinct(),
          createdAt = created,
          updatedAt = updated,
        )
      }

      return NoteItem(
        id = id,
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

const val MIN_NOTES_GRID_COLUMNS = 2
const val MAX_NOTES_GRID_COLUMNS = 3
const val DEFAULT_NOTES_GRID_COLUMNS = 2

/**
 * Repository interface for persisting `.text` notes in `/Notes`, custom labels, scoped storage state,
 * and layout preferences.
 */
interface NotesRepository {
  fun loadNotes(): List<NoteItem>
  fun saveNotes(notes: List<NoteItem>)
  fun loadLabels(): List<String>
  fun saveLabels(labels: List<String>)
  fun isGridView(): Boolean
  fun setGridView(isGrid: Boolean)
  fun getGridColumnsPerRow(): Int = DEFAULT_NOTES_GRID_COLUMNS
  fun setGridColumnsPerRow(columns: Int) {}
  fun isNotesFolderAccessGranted(): Boolean = true
  fun setNotesFolderAccessGranted(granted: Boolean) {}
  fun getGrantedFolderPaths(): Set<String> =
    if (isNotesFolderAccessGranted()) setOf(DEFAULT_NOTES_SCOPED_FOLDER.displayPath) else emptySet()
  fun setGrantedFolderPaths(paths: Set<String>) {
    setNotesFolderAccessGranted(NotesEngine.isNotesFolderAccessGranted(paths))
  }
  fun getNotesDirectory(): File? = null
}

/**
 * In-memory implementation of [NotesRepository] for unit tests and Compose previews.
 * When [notesDirectory] is provided, `.text` files are also synced to and from [notesDirectory].
 */
class InMemoryNotesRepository(
  initialNotes: List<NoteItem> = emptyList(),
  initialLabels: List<String> = emptyList(),
  initialGridView: Boolean = true,
  initialGridColumnsPerRow: Int = DEFAULT_NOTES_GRID_COLUMNS,
  initialNotesFolderGranted: Boolean = true,
  private val notesDirectory: File? = null,
) : NotesRepository {
  private val notes = initialNotes.toMutableList()
  private val labels = initialLabels.toMutableList()
  private var gridView = initialGridView
  private var gridColumnsPerRow = initialGridColumnsPerRow.coerceIn(MIN_NOTES_GRID_COLUMNS, MAX_NOTES_GRID_COLUMNS)
  private var notesFolderGranted = initialNotesFolderGranted
  private var fileNamesByNoteId = mutableMapOf<String, String>()

  init {
    if (notesDirectory != null) {
      if (initialNotes.isNotEmpty()) {
        fileNamesByNoteId.putAll(NotesEngine.saveNotesToDirectory(initialNotes, notesDirectory))
      } else if (notesDirectory.exists()) {
        val loaded = NotesEngine.loadNotesFromDirectory(notesDirectory)
        notes.addAll(loaded)
        fileNamesByNoteId.putAll(NotesEngine.assignTextFileNames(loaded))
      }
    }
  }

  override fun loadNotes(): List<NoteItem> {
    val dir = notesDirectory
    if (dir != null && dir.exists()) {
      val textFiles = dir.listFiles { f -> NotesEngine.isNoteTextFile(f) }
      if (textFiles != null && (textFiles.isNotEmpty() || notes.isEmpty())) {
        val synced = NotesEngine.loadNotesFromDirectory(
          directory = dir,
          metadataNotes = notes.toList(),
          fileNameByNoteId = fileNamesByNoteId,
        )
        notes.clear()
        notes.addAll(synced)
      }
    }
    return notes.toList()
  }

  override fun saveNotes(notes: List<NoteItem>) {
    this.notes.clear()
    this.notes.addAll(notes)
    val dir = notesDirectory
    if (dir != null) {
      fileNamesByNoteId.clear()
      fileNamesByNoteId.putAll(NotesEngine.saveNotesToDirectory(notes, dir))
    }
  }

  override fun loadLabels(): List<String> = labels.map { NotesEngine.normalizeLabelName(it) }.filter { it.isNotEmpty() }.distinct()

  override fun saveLabels(labels: List<String>) {
    this.labels.clear()
    this.labels.addAll(labels.map { NotesEngine.normalizeLabelName(it) }.filter { it.isNotEmpty() }.distinct())
  }

  override fun isGridView(): Boolean = gridView

  override fun setGridView(isGrid: Boolean) {
    gridView = isGrid
  }

  override fun getGridColumnsPerRow(): Int = gridColumnsPerRow

  override fun setGridColumnsPerRow(columns: Int) {
    gridColumnsPerRow = columns.coerceIn(MIN_NOTES_GRID_COLUMNS, MAX_NOTES_GRID_COLUMNS)
  }

  override fun isNotesFolderAccessGranted(): Boolean = notesFolderGranted

  override fun setNotesFolderAccessGranted(granted: Boolean) {
    notesFolderGranted = granted
  }

  override fun getNotesDirectory(): File? = notesDirectory
}

/**
 * Android [SharedPreferences] and `/Notes` `.text` file-backed implementation of [NotesRepository].
 */
class SharedPreferencesNotesRepository(
  private val sharedPreferences: SharedPreferences,
  private val notesDirectory: File? = null,
  private val hasExplicitNotesDirectory: Boolean = notesDirectory != null,
) : NotesRepository {

  companion object {
    const val PREFS_NAME = "notes_tool_preferences"
    const val KEY_NOTES_JSON = "notes_json"
    const val KEY_LABELS_JSON = "labels_json"
    const val KEY_IS_GRID_VIEW = "is_grid_view"
    const val KEY_GRID_COLUMNS_PER_ROW = "grid_columns_per_row"
    const val KEY_NOTES_FOLDER_GRANTED = "notes_folder_granted"
    const val KEY_GRANTED_FOLDER_PATHS = "notes_granted_folder_paths"
  }

  constructor(context: Context) : this(
    sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
    notesDirectory = NotesEngine.getDefaultNotesDirectory(context),
    hasExplicitNotesDirectory = false,
  )

  constructor(context: Context, notesDirectory: File) : this(
    sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
    notesDirectory = notesDirectory,
    hasExplicitNotesDirectory = true,
  )

  override fun loadNotes(): List<NoteItem> {
    val raw = sharedPreferences.getString(KEY_NOTES_JSON, null)
    val dir = notesDirectory
    if (raw == null) {
      if (dir != null) {
        if (hasExplicitNotesDirectory || isNotesFolderAccessGranted()) {
          if (dir.exists()) {
            return NotesEngine.loadNotesFromDirectory(dir)
          }
        } else {
          // SharedPreferences was cleared and /Notes access is not granted: remove stale managed .text files
          try {
            dir.listFiles { f -> NotesEngine.isNoteTextFile(f) }?.forEach { it.delete() }
          } catch (_: Throwable) {}
        }
      }
      return emptyList()
    }

    val metadataNotes = mutableListOf<NoteItem>()
    val fileNameByNoteId = mutableMapOf<String, String>()
    try {
      val array = JSONArray(raw)
      for (i in 0 until array.length()) {
        val obj = array.optJSONObject(i)
        if (obj != null) {
          val note = NoteItem.fromJson(obj)
          metadataNotes.add(note)
          val fileName = obj.optString("fileName", "").trim()
          if (fileName.isNotEmpty()) {
            fileNameByNoteId[note.id] = fileName
          }
        }
      }
    } catch (_: Exception) {
      return emptyList()
    }

    if (dir != null) {
      try {
        val existingTextFiles = if (dir.exists()) {
          dir.listFiles { f -> NotesEngine.isNoteTextFile(f) }?.toList().orEmpty()
        } else {
          emptyList()
        }

        return if (existingTextFiles.isEmpty()) {
          if (metadataNotes.isNotEmpty()) {
            NotesEngine.saveNotesToDirectory(metadataNotes, dir)
          }
          metadataNotes
        } else {
          NotesEngine.loadNotesFromDirectory(
            directory = dir,
            metadataNotes = metadataNotes,
            fileNameByNoteId = fileNameByNoteId,
          )
        }
      } catch (_: Throwable) {
        return metadataNotes
      }
    }

    return metadataNotes
  }

  override fun saveNotes(notes: List<NoteItem>) {
    val assignedFileNames = NotesEngine.assignTextFileNames(notes)
    val dir = notesDirectory
    if (dir != null) {
      try {
        NotesEngine.saveNotesToDirectory(notes, dir, assignedFileNames)
      } catch (_: Throwable) {}
    }
    val array = JSONArray()
    notes.forEach { note ->
      val json = note.toJson()
      assignedFileNames[note.id]?.let { json.put("fileName", it) }
      array.put(json)
    }
    sharedPreferences.edit().putString(KEY_NOTES_JSON, array.toString()).apply()
  }

  override fun loadLabels(): List<String> {
    val raw = sharedPreferences.getString(KEY_LABELS_JSON, null) ?: return emptyList()
    return try {
      val array = JSONArray(raw)
      val list = mutableListOf<String>()
      for (i in 0 until array.length()) {
        val label = NotesEngine.normalizeLabelName(array.optString(i, ""))
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
    labels.map { NotesEngine.normalizeLabelName(it) }.filter { it.isNotEmpty() }.distinct().forEach { array.put(it) }
    sharedPreferences.edit().putString(KEY_LABELS_JSON, array.toString()).apply()
  }

  override fun isGridView(): Boolean {
    return sharedPreferences.getBoolean(KEY_IS_GRID_VIEW, true)
  }

  override fun setGridView(isGrid: Boolean) {
    sharedPreferences.edit().putBoolean(KEY_IS_GRID_VIEW, isGrid).apply()
  }

  override fun getGridColumnsPerRow(): Int {
    return sharedPreferences
      .getInt(KEY_GRID_COLUMNS_PER_ROW, DEFAULT_NOTES_GRID_COLUMNS)
      .coerceIn(MIN_NOTES_GRID_COLUMNS, MAX_NOTES_GRID_COLUMNS)
  }

  override fun setGridColumnsPerRow(columns: Int) {
    val coerced = columns.coerceIn(MIN_NOTES_GRID_COLUMNS, MAX_NOTES_GRID_COLUMNS)
    sharedPreferences.edit().putInt(KEY_GRID_COLUMNS_PER_ROW, coerced).apply()
  }

  override fun isNotesFolderAccessGranted(): Boolean {
    if (sharedPreferences.getBoolean(KEY_NOTES_FOLDER_GRANTED, false)) {
      return true
    }
    val rawPaths = sharedPreferences.getString(KEY_GRANTED_FOLDER_PATHS, null)
    return NotesEngine.isNotesFolderAccessGranted(NotesEngine.parseGrantedFolderPaths(rawPaths))
  }

  override fun setNotesFolderAccessGranted(granted: Boolean) {
    val updatedPaths = if (granted) {
      getGrantedFolderPaths() + DEFAULT_NOTES_SCOPED_FOLDER.displayPath
    } else {
      emptySet()
    }
    sharedPreferences.edit()
      .putBoolean(KEY_NOTES_FOLDER_GRANTED, granted)
      .putString(KEY_GRANTED_FOLDER_PATHS, NotesEngine.serializeGrantedFolderPaths(updatedPaths))
      .apply()
  }

  override fun getGrantedFolderPaths(): Set<String> {
    val parsed = NotesEngine.parseGrantedFolderPaths(
      sharedPreferences.getString(KEY_GRANTED_FOLDER_PATHS, null)
    )
    return if (sharedPreferences.getBoolean(KEY_NOTES_FOLDER_GRANTED, false)) {
      parsed + DEFAULT_NOTES_SCOPED_FOLDER.displayPath
    } else {
      parsed
    }
  }

  override fun setGrantedFolderPaths(paths: Set<String>) {
    val granted = NotesEngine.isNotesFolderAccessGranted(paths)
    sharedPreferences.edit()
      .putBoolean(KEY_NOTES_FOLDER_GRANTED, granted)
      .putString(KEY_GRANTED_FOLDER_PATHS, NotesEngine.serializeGrantedFolderPaths(paths))
      .apply()
  }

  override fun getNotesDirectory(): File? = notesDirectory
}

/**
 * Pure business logic for creating, editing, converting, filtering, and organizing `.text` notes.
 */
object NotesEngine {

  val DEFAULT_NOTES_FOLDER: NotesFolder = DEFAULT_NOTES_SCOPED_FOLDER

  private val RESERVED_TODO_TEXT_FILES = setOf(
    TODO_LIST_TEXT_FILE_NAME.lowercase(Locale.ROOT),
    "todo_list.text",
    TODO_COMPLETED_TEXT_FILE_NAME.lowercase(Locale.ROOT),
    TODO_SAVED_TEXT_FILE_NAME.lowercase(Locale.ROOT),
  )

  fun isNoteTextFile(file: File): Boolean {
    return file.isFile &&
      file.extension.equals(NOTES_FILE_EXTENSION, ignoreCase = true) &&
      file.name.lowercase(Locale.ROOT) !in RESERVED_TODO_TEXT_FILES
  }

  private val TEXT_UNCHECKED_LINE_REGEX = Regex("""^([ \t]*)-\s+(.+)$""")
  private val TEXT_CHECKED_LINE_REGEX = Regex("""^([ \t]*)\+\s+(.+)$""")

  data class PendingQuickCaptureRequest(
    val isChecklist: Boolean,
    val initialPinned: Boolean = false,
    val initialLabel: String? = null,
    val requestId: Long = System.nanoTime(),
  )

  var pendingQuickCaptureRequest by mutableStateOf<PendingQuickCaptureRequest?>(null)
    private set

  /**
   * Requests that [NotesScreen] immediately open its new-note creation panel in either text note
   * mode ([isChecklist] = `false`) or checklist mode ([isChecklist] = `true`), optionally
   * pre-populating [initialPinned] or [initialLabel].
   */
  fun requestQuickCapture(
    isChecklist: Boolean,
    initialPinned: Boolean = false,
    initialLabel: String? = null,
  ) {
    pendingQuickCaptureRequest = PendingQuickCaptureRequest(
      isChecklist = isChecklist,
      initialPinned = initialPinned,
      initialLabel = initialLabel?.trim()?.takeIf { it.isNotEmpty() },
    )
  }

  /**
   * Consumes any pending quick-capture request after [NotesScreen] opens the creation panel.
   */
  fun consumePendingQuickCaptureRequest(): PendingQuickCaptureRequest? {
    val current = pendingQuickCaptureRequest
    pendingQuickCaptureRequest = null
    return current
  }

  data class PendingWidgetNavigationRequest(
    val filter: NotesFilter? = null,
    val openNoteId: String? = null,
    val requestId: Long = System.nanoTime(),
  )

  var pendingWidgetNavigationRequest by mutableStateOf<PendingWidgetNavigationRequest?>(null)
    private set

  /**
   * Requests that [NotesScreen] switch to [filter] and/or open [openNoteId] in the single-note view.
   */
  fun requestWidgetNavigation(
    filter: NotesFilter? = null,
    openNoteId: String? = null,
  ) {
    pendingWidgetNavigationRequest = PendingWidgetNavigationRequest(
      filter = filter,
      openNoteId = openNoteId?.takeIf { it.isNotBlank() },
    )
  }

  /**
   * Consumes any pending widget navigation request after [NotesScreen] handles it.
   */
  fun consumePendingWidgetNavigationRequest(): PendingWidgetNavigationRequest? {
    val current = pendingWidgetNavigationRequest
    pendingWidgetNavigationRequest = null
    return current
  }

  /**
   * Computes the `.text` checklist nesting depth (`indentLevel`) from leading whitespace on [rawLine].
   * Each leading tab character (`\t`) denotes 1 nesting level (with every 2 leading spaces also
   * accepted as 1 nesting level for backwards compatibility).
   */
  fun parseTextIndentLevel(rawLine: String): Int {
    val leadingWhitespace = rawLine.takeWhile { it == ' ' || it == '\t' }
    val tabCount = leadingWhitespace.count { it == '\t' }
    val spaceCount = leadingWhitespace.count { it == ' ' }
    return tabCount + (spaceCount / 2)
  }

  /**
   * Normalizes a label name so the internal `.text` `#` prefix is never stored on [NoteItem.labels]
   * or displayed in the UI.
   */
  fun normalizeLabelName(raw: String): String {
    return raw.trim().trimStart('#').trim()
  }

  /**
   * Formats a list of labels into the internal `.text` CSV footer line:
   * `#Label Name 1, #Label Name 2, ...`
   */
  fun formatLabelsToTextLine(labels: List<String>): String {
    return labels
      .map { normalizeLabelName(it) }
      .filter { it.isNotEmpty() }
      .distinct()
      .joinToString(", ") { "#$it" }
  }

  /**
   * Parses a `.text` label CSV line (`#Label Name 1, #Label Name 2, ...`) into clean label names
   * without the internal `#`, or returns `null` if [rawLine] is not a `.text` label line.
   *
   * Note: Markdown headings use `# <Title>` (with a space after `#`), whereas `.text` label tokens
   * start directly with `#<LabelName>` (no space immediately following `#`) and are comma-separated.
   */
  fun parseLabelsFromTextLine(rawLine: String): List<String>? {
    val trimmed = rawLine.trim()
    if (!trimmed.startsWith("#")) return null
    val tokens = trimmed.split(',').map { it.trim() }
    if (tokens.isEmpty()) return null
    val parsedLabels = mutableListOf<String>()
    for (token in tokens) {
      if (!token.startsWith("#") || token.length < 2) return null
      val secondChar = token[1]
      if (secondChar.isWhitespace() || secondChar == '#') return null
      val labelName = normalizeLabelName(token)
      if (labelName.isEmpty()) return null
      parsedLabels.add(labelName)
    }
    return parsedLabels.distinct()
  }

  /**
   * Formats a [NoteItem] into `.text` content:
   * - Markdown `# <title>` heading when [NoteItem.title] is non-blank.
   * - For checklists ([NoteItem.isChecklist] = `true`), `- <item>` for unchecked items (matching
   *   `.yaml` list syntax) and `+ <item>` for checked items (dash replaced with `+` internally).
   * - For text notes ([NoteItem.isChecklist] = `false`), Markdown body text.
   * - Labels on the last line as a `#Label Name 1, #Label Name 2, ...` CSV list when [includeLabels]
   *   is `true` and [NoteItem.labels] is non-empty.
   */
  fun formatNoteToText(
    note: NoteItem,
    includeLabels: Boolean = true,
  ): String {
    val sb = StringBuilder()
    if (note.title.isNotBlank()) {
      sb.append("# ").append(note.title.trim())
    }
    if (note.isChecklist) {
      val validItems = note.checklistItems.filter { it.text.isNotBlank() }
      if (validItems.isNotEmpty()) {
        if (sb.isNotEmpty()) sb.append("\n\n")
        val lines = validItems.map { it.toTextLine() }
        sb.append(lines.joinToString("\n"))
      }
    } else {
      if (note.content.isNotBlank()) {
        if (sb.isNotEmpty()) sb.append("\n\n")
        sb.append(note.content.trimEnd())
      }
    }
    if (includeLabels) {
      val labelsLine = formatLabelsToTextLine(note.labels)
      if (labelsLine.isNotEmpty()) {
        if (sb.isNotEmpty()) sb.append("\n\n")
        sb.append(labelsLine)
      }
    }
    return sb.toString()
  }

  /**
   * Parses `.text` content into a [NoteItem].
   * - Extracts a `# Heading` title if present on the first non-blank line; otherwise uses [fallbackTitle].
   * - Extracts the trailing `#Label Name 1, #Label Name 2, ...` CSV label list from the last non-blank line
   *   (stripping the internal `#` prefixes so they are never shown in the UI).
   * - Recognizes `.text` checklists where unchecked items use `- <item>` and checked items use `+ <item>`.
   */
  fun parseNoteFromText(
    rawText: String,
    fallbackTitle: String = "",
    id: String = UUID.randomUUID().toString(),
    nowMs: Long = System.currentTimeMillis(),
    forceChecklist: Boolean? = null,
    existingChecklistItems: List<NoteChecklistItem> = emptyList(),
  ): NoteItem {
    val normalized = rawText.replace("\r\n", "\n").replace('\r', '\n')
    val lines = normalized.split('\n')
    val firstNonBlankIdx = lines.indexOfFirst { it.isNotBlank() }
    if (firstNonBlankIdx == -1) {
      return NoteItem(
        id = id,
        title = fallbackTitle,
        isChecklist = forceChecklist ?: false,
        createdAt = nowMs,
        updatedAt = nowMs,
      )
    }

    val lastNonBlankIdx = lines.indexOfLast { it.isNotBlank() }
    val trailingLabels = parseLabelsFromTextLine(lines[lastNonBlankIdx])
    val contentLines = if (trailingLabels != null) {
      lines.subList(0, lastNonBlankIdx)
    } else {
      lines
    }

    val firstContentNonBlankIdx = contentLines.indexOfFirst { it.isNotBlank() }
    if (firstContentNonBlankIdx == -1) {
      return NoteItem(
        id = id,
        title = fallbackTitle,
        isChecklist = forceChecklist ?: false,
        labels = trailingLabels.orEmpty(),
        createdAt = nowMs,
        updatedAt = nowMs,
      )
    }

    val firstTrimmed = contentLines[firstContentNonBlankIdx].trim()
    val title: String
    val rawBodyLines: List<String>
    if (firstTrimmed.startsWith("# ") || firstTrimmed == "#") {
      title = firstTrimmed.removePrefix("#").trim().ifBlank { fallbackTitle }
      rawBodyLines = contentLines.drop(firstContentNonBlankIdx + 1)
    } else {
      title = fallbackTitle
      rawBodyLines = contentLines.drop(firstContentNonBlankIdx)
    }

    val firstBodyNonBlank = rawBodyLines.indexOfFirst { it.isNotBlank() }
    val lastBodyNonBlank = rawBodyLines.indexOfLast { it.isNotBlank() }
    val trimmedBodyLines = if (firstBodyNonBlank >= 0 && lastBodyNonBlank >= firstBodyNonBlank) {
      rawBodyLines.subList(firstBodyNonBlank, lastBodyNonBlank + 1)
    } else {
      emptyList()
    }

    val nonBlankBodyLines = trimmedBodyLines.filter { it.isNotBlank() }
    val allTextChecklistLines = nonBlankBodyLines.isNotEmpty() && nonBlankBodyLines.all { line ->
      TEXT_UNCHECKED_LINE_REGEX.matches(line) || TEXT_CHECKED_LINE_REGEX.matches(line)
    }

    val isChecklist = when (forceChecklist) {
      true -> true
      false -> false
      null -> allTextChecklistLines
    }

    return if (isChecklist) {
      val remainingExisting = existingChecklistItems.toMutableList()
      val items = nonBlankBodyLines.mapNotNull { rawLine ->
        val checkedMatch = TEXT_CHECKED_LINE_REGEX.matchEntire(rawLine)
        val uncheckedMatch = if (checkedMatch == null) TEXT_UNCHECKED_LINE_REGEX.matchEntire(rawLine) else null
        val indentLevel = parseTextIndentLevel(rawLine)
        val (itemText, isChecked) = when {
          checkedMatch != null -> checkedMatch.groupValues[2].trim() to true
          uncheckedMatch != null -> uncheckedMatch.groupValues[2].trim() to false
          else -> {
            val parsed = parseChecklistItems(rawLine).firstOrNull()
            if (parsed != null) parsed.text to parsed.isChecked else rawLine.trim() to false
          }
        }
        if (itemText.isEmpty()) {
          null
        } else {
          val matchIdx = remainingExisting.indexOfFirst {
            it.text == itemText && it.isChecked == isChecked && it.indentLevel == indentLevel
          }.takeIf { it >= 0 }
            ?: remainingExisting.indexOfFirst { it.text == itemText && it.isChecked == isChecked }.takeIf { it >= 0 }
            ?: remainingExisting.indexOfFirst { it.text == itemText }.takeIf { it >= 0 }
          val matchedId = if (matchIdx != null) {
            remainingExisting.removeAt(matchIdx).id
          } else {
            UUID.randomUUID().toString()
          }
          NoteChecklistItem(
            id = matchedId,
            text = itemText,
            isChecked = isChecked,
            indentLevel = indentLevel,
          )
        }
      }
      NoteItem(
        id = id,
        title = title,
        content = "",
        isChecklist = true,
        checklistItems = items,
        labels = trailingLabels.orEmpty(),
        createdAt = nowMs,
        updatedAt = nowMs,
      )
    } else {
      NoteItem(
        id = id,
        title = title,
        content = trimmedBodyLines.joinToString("\n").trimEnd(),
        isChecklist = false,
        checklistItems = emptyList(),
        labels = trailingLabels.orEmpty(),
        createdAt = nowMs,
        updatedAt = nowMs,
      )
    }
  }

  /**
   * Builds a unique, filesystem-safe `.text` filename for [note].
   */
  fun buildTextFileName(
    note: NoteItem,
    usedFileNamesLower: MutableSet<String> = mutableSetOf(),
  ): String {
    val shortId = note.id
      .replace(Regex("[^A-Za-z0-9_-]"), "")
      .take(8)
      .ifEmpty { "note" }
    val rawStem = if (note.title.isNotBlank()) {
      note.title.trim()
    } else {
      "Untitled_$shortId"
    }
    val sanitizedStem = rawStem
      .replace(Regex("[\\\\/:*?\"<>|\\r\\n\\t]"), "_")
      .replace(Regex("\\s+"), " ")
      .trim()
      .trim('.')
      .take(80)
      .ifEmpty { "Untitled_$shortId" }

    var candidate = "$sanitizedStem$NOTES_FILE_DOT_EXTENSION"
    var counter = 2
    while (!usedFileNamesLower.add(candidate.lowercase(Locale.ROOT))) {
      candidate = "$sanitizedStem ($counter)$NOTES_FILE_DOT_EXTENSION"
      counter++
    }
    return candidate
  }

  /**
   * Assigns deterministic, collision-free `.text` filenames for a list of [notes],
   * returning a map of `note.id -> fileName`.
   */
  fun assignTextFileNames(notes: List<NoteItem>): Map<String, String> {
    val usedLower = mutableSetOf<String>()
    val result = LinkedHashMap<String, String>(notes.size)
    for (note in notes) {
      result[note.id] = buildTextFileName(note, usedLower)
    }
    return result
  }

  /**
   * Writes all [notes] into [directory] as `.text` files and deletes any stale `.text` files
   * in [directory] that are no longer part of [notes].
   */
  fun saveNotesToDirectory(
    notes: List<NoteItem>,
    directory: File,
    fileNamesByNoteId: Map<String, String> = assignTextFileNames(notes),
  ): Map<String, String> {
    if (!directory.exists()) {
      directory.mkdirs()
    }
    val activeNamesLower = mutableSetOf<String>()
    for (note in notes) {
      val fileName = fileNamesByNoteId[note.id] ?: buildTextFileName(note, activeNamesLower)
      activeNamesLower.add(fileName.lowercase(Locale.ROOT))
      val targetFile = File(directory, fileName)
      targetFile.writeText(formatNoteToText(note))
      try {
        if (note.updatedAt > 0L) {
          targetFile.setLastModified(note.updatedAt)
        }
      } catch (_: Throwable) {}
    }

    directory.listFiles { f -> isNoteTextFile(f) }?.forEach { existingFile ->
      if (existingFile.name.lowercase(Locale.ROOT) !in activeNamesLower) {
        try {
          existingFile.delete()
        } catch (_: Throwable) {}
      }
    }
    return fileNamesByNoteId
  }

  /**
   * Loads all `.text` note files from [directory], merging with optional [metadataNotes]
   * and [fileNameByNoteId] to preserve note IDs, colors, pin/archive/trash flags, labels,
   * timestamps, and manual card arrangement.
   */
  fun loadNotesFromDirectory(
    directory: File,
    metadataNotes: List<NoteItem> = emptyList(),
    fileNameByNoteId: Map<String, String> = emptyMap(),
  ): List<NoteItem> {
    if (!directory.exists() || !directory.isDirectory) {
      return metadataNotes
    }
    val textFiles = directory.listFiles { f -> isNoteTextFile(f) }?.toList().orEmpty()

    if (textFiles.isEmpty()) {
      return emptyList()
    }

    val effectiveFileNames = if (fileNameByNoteId.isNotEmpty()) {
      fileNameByNoteId
    } else if (metadataNotes.isNotEmpty()) {
      assignTextFileNames(metadataNotes)
    } else {
      emptyMap()
    }

    val metaById = metadataNotes.associateBy { it.id }
    val noteIdByFileNameLower = effectiveFileNames.entries.associate { (id, fName) ->
      fName.lowercase(Locale.ROOT) to id
    }
    val matchedNotesById = LinkedHashMap<String, NoteItem>()
    val unmatchedExternalNotes = mutableListOf<NoteItem>()

    for (file in textFiles.sortedByDescending { it.lastModified() }) {
      val rawText = try {
        file.readText()
      } catch (_: Throwable) {
        continue
      }
      val fileNameLower = file.name.lowercase(Locale.ROOT)
      val matchedId = noteIdByFileNameLower[fileNameLower]
        ?: metadataNotes.firstOrNull { meta ->
          meta.id !in matchedNotesById &&
            meta.title.isNotBlank() &&
            meta.title.equals(file.nameWithoutExtension, ignoreCase = true)
        }?.id
      val meta = matchedId?.let { metaById[it] }

      val isAutoUntitledFile = file.nameWithoutExtension.startsWith("Untitled_", ignoreCase = true) ||
        file.nameWithoutExtension.startsWith("Note_", ignoreCase = true)
      val fallbackTitle = when {
        meta != null -> meta.title
        isAutoUntitledFile -> ""
        else -> file.nameWithoutExtension
      }
      val fileTimestamp = file.lastModified().takeIf { it > 0L } ?: System.currentTimeMillis()
      val parsed = parseNoteFromText(
        rawText = rawText,
        fallbackTitle = fallbackTitle,
        id = meta?.id ?: "text_${file.nameWithoutExtension.hashCode()}_$fileTimestamp",
        nowMs = meta?.updatedAt ?: fileTimestamp,
        forceChecklist = if (rawText.trim().let { !it.contains('\n') && it.startsWith("#") } && meta != null) {
          meta.isChecklist
        } else {
          null
        },
        existingChecklistItems = meta?.checklistItems.orEmpty(),
      )

      if (meta != null) {
        matchedNotesById[meta.id] = parsed.copy(
          id = meta.id,
          isChecklist = if (parsed.content.isEmpty() && parsed.checklistItems.isEmpty()) {
            meta.isChecklist
          } else {
            parsed.isChecklist
          },
          color = meta.color,
          isPinned = meta.isPinned,
          isArchived = meta.isArchived,
          isTrashed = meta.isTrashed,
          labels = parsed.labels.ifEmpty { meta.labels },
          createdAt = meta.createdAt,
          updatedAt = meta.updatedAt,
        )
      } else if (!parsed.isEmpty) {
        unmatchedExternalNotes.add(parsed)
      }
    }

    val orderedMatched = metadataNotes.mapNotNull { matchedNotesById[it.id] }
    return unmatchedExternalNotes + orderedMatched
  }

  /**
   * Resolves the `/Notes` directory in the root storage ([userRoot]), falling back to app storage
   * `/Notes` in restricted/test environments where external root storage isn't directly writable via [File].
   */
  fun getDefaultNotesDirectory(
    context: Context? = null,
    userRoot: File = FileManagerEngine.getUserRootDirectory(),
  ): File {
    val primaryNotesDir = File(userRoot, NOTES_ROOT_FOLDER_NAME)
    try {
      if ((primaryNotesDir.exists() && primaryNotesDir.canWrite()) || primaryNotesDir.mkdirs()) {
        return primaryNotesDir
      }
    } catch (_: Throwable) {}

    if (context != null) {
      val fallbackParent = context.getExternalFilesDir(null) ?: context.filesDir
      val fallbackNotesDir = File(fallbackParent, NOTES_ROOT_FOLDER_NAME)
      fallbackNotesDir.mkdirs()
      return fallbackNotesDir
    }
    return primaryNotesDir
  }

  /**
   * Creates the Android system prompt [Intent] (`Intent.ACTION_OPEN_DOCUMENT_TREE`) scoped to `/Notes`
   * in the root directory so the user can grant scoped storage access to use the Notes tool.
   */
  fun createFolderAccessIntent(
    context: Context? = null,
    folder: NotesFolder = DEFAULT_NOTES_SCOPED_FOLDER,
    userRoot: File = FileManagerEngine.getUserRootDirectory(),
  ): Intent {
    val relativePath = folder.displayPath.trim().trim('/').ifEmpty { NOTES_ROOT_FOLDER_NAME }
    try {
      val targetDir = if (!folder.customAbsolutePath.isNullOrBlank()) {
        File(folder.customAbsolutePath)
      } else {
        File(userRoot, relativePath)
      }
      if (!targetDir.exists()) {
        targetDir.mkdirs()
      }
    } catch (_: Throwable) {}

    val docId = "primary:$relativePath"
    val initialUri = try {
      DocumentsContract.buildDocumentUri(
        NOTES_EXTERNAL_STORAGE_DOCUMENTS_AUTHORITY,
        docId,
      )
    } catch (_: Throwable) {
      Uri.parse("content://$NOTES_EXTERNAL_STORAGE_DOCUMENTS_AUTHORITY/document/primary%3A$relativePath")
    }
    val baseIntent = if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      try {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
        storageManager?.primaryStorageVolume?.createOpenDocumentTreeIntent()
      } catch (_: Throwable) {
        null
      }
    } else {
      null
    } ?: Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)

    return baseIntent.apply {
      action = Intent.ACTION_OPEN_DOCUMENT_TREE
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
      }
      addFlags(
        Intent.FLAG_GRANT_READ_URI_PERMISSION or
          Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
          Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
          Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
      )
    }
  }

  fun isNotesFolderAccessGranted(grantedFolderPaths: Set<String>): Boolean {
    return grantedFolderPaths.any { path ->
      val clean = path.trim().trim('/')
      clean.equals(NOTES_ROOT_FOLDER_NAME, ignoreCase = true)
    }
  }

  fun parseGrantedFolderPaths(raw: String?): Set<String> {
    if (raw.isNullOrBlank()) return emptySet()
    return raw.split('\n')
      .map { it.trim() }
      .filter { it.isNotEmpty() }
      .map { if (it.startsWith("/")) it else "/$it" }
      .toSet()
  }

  fun serializeGrantedFolderPaths(paths: Set<String>): String {
    return paths
      .map { it.trim() }
      .filter { it.isNotEmpty() }
      .map { if (it.startsWith("/")) it else "/$it" }
      .distinct()
      .joinToString("\n")
  }

  fun getGrantedFolderPaths(context: Context): Set<String> {
    return SharedPreferencesNotesRepository(context).getGrantedFolderPaths()
  }

  fun setFolderPathAccessGranted(
    context: Context,
    folderPath: String = DEFAULT_NOTES_SCOPED_FOLDER.displayPath,
    granted: Boolean = true,
  ): Set<String> {
    val repo = SharedPreferencesNotesRepository(context)
    val normalized = if (folderPath.startsWith("/")) folderPath else "/$folderPath"
    val updated = if (granted) {
      repo.getGrantedFolderPaths() + normalized
    } else {
      repo.getGrantedFolderPaths().filterNot { it.equals(normalized, ignoreCase = true) }.toSet()
    }
    repo.setGrantedFolderPaths(updated)
    return updated
  }

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
      labels = draft.labels.map { normalizeLabelName(it) }.filter { it.isNotEmpty() }.distinct(),
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
   * Parses [rawText] into one or more [NoteChecklistItem]s by splitting on return/newline
   * characters (`\r\n`, `\n`, or `\r`), extracting leading tab indentation (`\t`) for nesting
   * [NoteChecklistItem.indentLevel], trimming optional list/checkbox prefixes, and filtering out blank lines.
   */
  fun parseChecklistItems(rawText: String): List<NoteChecklistItem> {
    return rawText
      .split(Regex("\\r\\n|\\n|\\r"))
      .mapNotNull { rawLine ->
        val indentLevel = parseTextIndentLevel(rawLine)
        val line = rawLine.trim()
        if (line.isEmpty()) {
          null
        } else {
          when {
            line.startsWith("+ ") -> {
              val itemText = line.substring(2).trim()
              if (itemText.isNotEmpty()) NoteChecklistItem(text = itemText, isChecked = true, indentLevel = indentLevel) else null
            }
            line.startsWith("- [x] ", ignoreCase = true) || line.startsWith("* [x] ", ignoreCase = true) -> {
              val itemText = line.substring(6).trim()
              if (itemText.isNotEmpty()) NoteChecklistItem(text = itemText, isChecked = true, indentLevel = indentLevel) else null
            }
            line.startsWith("- [ ] ") || line.startsWith("* [ ] ") -> {
              val itemText = line.substring(6).trim()
              if (itemText.isNotEmpty()) NoteChecklistItem(text = itemText, isChecked = false, indentLevel = indentLevel) else null
            }
            line.startsWith("[x] ", ignoreCase = true) -> {
              val itemText = line.substring(4).trim()
              if (itemText.isNotEmpty()) NoteChecklistItem(text = itemText, isChecked = true, indentLevel = indentLevel) else null
            }
            line.startsWith("[ ] ") -> {
              val itemText = line.substring(4).trim()
              if (itemText.isNotEmpty()) NoteChecklistItem(text = itemText, isChecked = false, indentLevel = indentLevel) else null
            }
            line.startsWith("- ") || line.startsWith("* ") || line.startsWith("• ") -> {
              val itemText = line.substring(2).trim()
              if (itemText.isNotEmpty()) NoteChecklistItem(text = itemText, isChecked = false, indentLevel = indentLevel) else null
            }
            else -> {
              val numberedMatch = Regex("""^\d+[.)]\s+(.+)$""").matchEntire(line)
              if (numberedMatch != null) {
                val itemText = numberedMatch.groupValues[1].trim()
                if (itemText.isNotEmpty()) NoteChecklistItem(text = itemText, isChecked = false, indentLevel = indentLevel) else null
              } else {
                NoteChecklistItem(text = line, isChecked = false, indentLevel = indentLevel)
              }
            }
          }
        }
      }
  }

  /**
   * Updates the text of the checklist item with [itemId] in [note]. If [updatedText] contains
   * return/newline characters from a multi-line paste, splits it into multiple checklist items in place.
   */
  fun updateChecklistItemText(
    note: NoteItem,
    itemId: String,
    updatedText: String,
  ): NoteItem {
    if (!updatedText.contains('\n') && !updatedText.contains('\r')) {
      return note.copy(
        checklistItems = note.checklistItems.map { item ->
          if (item.id == itemId) {
            if (updatedText.startsWith("\t")) {
              val addedTabs = updatedText.takeWhile { it == '\t' }.length
              item.copy(
                text = updatedText.drop(addedTabs),
                indentLevel = item.indentLevel + addedTabs,
              )
            } else {
              item.copy(text = updatedText)
            }
          } else {
            item
          }
        },
      )
    }

    val parsed = parseChecklistItems(updatedText)
    val updatedList = mutableListOf<NoteChecklistItem>()
    for (item in note.checklistItems) {
      if (item.id == itemId) {
        if (parsed.isEmpty()) {
          updatedList.add(item.copy(text = ""))
        } else {
          val firstParsed = parsed.first()
          updatedList.add(
            item.copy(
              text = firstParsed.text,
              indentLevel = if (firstParsed.indentLevel > 0) firstParsed.indentLevel else item.indentLevel,
            ),
          )
          parsed.drop(1).forEach { extra ->
            updatedList.add(
              extra.copy(
                isChecked = item.isChecked,
                indentLevel = if (extra.indentLevel > 0) extra.indentLevel else item.indentLevel,
              ),
            )
          }
        }
      } else {
        updatedList.add(item)
      }
    }
    return note.copy(checklistItems = updatedList)
  }

  /**
   * Adjusts the nesting [NoteChecklistItem.indentLevel] of the checklist item with [itemId] by [delta]
   * (clamped to `>= 0`).
   */
  fun adjustChecklistItemIndent(
    note: NoteItem,
    itemId: String,
    delta: Int,
  ): NoteItem {
    if (delta == 0) return note
    return note.copy(
      checklistItems = note.checklistItems.map { item ->
        if (item.id == itemId) {
          item.copy(indentLevel = (item.indentLevel + delta).coerceAtLeast(0))
        } else {
          item
        }
      },
    )
  }

  /**
   * Converts a [NoteItem] between plain text mode and interactive checklist mode.
   */
  fun toggleChecklistMode(note: NoteItem): NoteItem {
    return if (!note.isChecklist) {
      // Plain text -> Checklist: split non-blank lines into checklist items (preserving tab indentation)
      val items = parseChecklistItems(note.content)
      note.copy(
        isChecklist = true,
        checklistItems = items,
        content = "",
      )
    } else {
      // Checklist -> Plain text: join checklist items into lines (preserving tab indentation)
      val joinedContent = note.checklistItems
        .mapNotNull { item ->
          val clean = item.text.trim()
          if (clean.isEmpty()) null else "${"\t".repeat(item.indentLevel.coerceAtLeast(0))}$clean"
        }
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
   * Returns checked items in [note] whose text matches [query] (case-insensitive),
   * prioritizing prefix matches followed by substring matches.
   */
  fun getCheckedItemSuggestions(
    note: NoteItem,
    query: String,
  ): List<NoteChecklistItem> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return emptyList()
    val matching = note.checkedItems.filter { item ->
      val itemText = item.text.trim()
      itemText.isNotEmpty() && itemText.contains(trimmed, ignoreCase = true)
    }.distinctBy { it.text.trim().lowercase(Locale.US) }

    val (prefixMatches, containsMatches) = matching.partition { item ->
      item.text.trim().startsWith(trimmed, ignoreCase = true)
    }
    return prefixMatches + containsMatches
  }

  /**
   * Restores (unchecks) a checked checklist item with [checkedItemId] back to the unchecked list.
   * When [replacingUncheckedItemId] is provided, replaces that unchecked item's text with the
   * suggested checked item's text and removes the checked item from the checked list.
   */
  fun applyCheckedItemSuggestion(
    note: NoteItem,
    checkedItemId: String,
    replacingUncheckedItemId: String? = null,
  ): NoteItem {
    val target = note.checkedItems.firstOrNull { it.id == checkedItemId } ?: return note
    val updatedItems = if (replacingUncheckedItemId != null) {
      note.checklistItems
        .filterNot { it.id == checkedItemId }
        .map { item ->
          if (item.id == replacingUncheckedItemId) {
            item.copy(text = target.text, isChecked = false)
          } else {
            item
          }
        }
    } else {
      note.checklistItems.map { item ->
        if (item.id == checkedItemId) item.copy(isChecked = false) else item
      }
    }
    return note.copy(checklistItems = updatedItems)
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
   * Returns all active/archived (non-trashed) notes that currently use [label].
   */
  fun getNotesUsingLabel(
    notes: List<NoteItem>,
    label: String,
  ): List<NoteItem> {
    return notes.filter { note ->
      !note.isTrashed && note.labels.any { it.equals(label, ignoreCase = true) }
    }
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
      .map { normalizeLabelName(it) }
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
