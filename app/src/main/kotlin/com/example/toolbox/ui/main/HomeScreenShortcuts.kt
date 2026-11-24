package com.example.toolbox.ui.main

import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.toPath
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.toolbox.MainActivity
import com.example.toolbox.R
import com.example.toolbox.ui.theme.parseHexColor
import java.io.File

const val EXTRA_STANDALONE_TOOL = "extra_standalone_tool"
const val EXTRA_FOLDER_PATH = "extra_folder_path"
const val EXTRA_EXPAND_MUSIC_PLAYER = "extra_expand_music_player"
const val EXTRA_NOTES_QUICK_CAPTURE_MODE = "extra_notes_quick_capture_mode"
const val NOTES_QUICK_CAPTURE_NOTE = "note"
const val NOTES_QUICK_CAPTURE_CHECKLIST = "checklist"
const val EXTRA_NOTES_INITIAL_FILTER_MODE = "extra_notes_initial_filter_mode"
const val EXTRA_NOTES_INITIAL_FILTER_LABEL = "extra_notes_initial_filter_label"
const val EXTRA_NOTES_OPEN_NOTE_ID = "extra_notes_open_note_id"
const val NOTES_FILTER_MODE_ALL = "all"
const val NOTES_FILTER_MODE_PINNED = "pinned"
const val NOTES_FILTER_MODE_LABEL = "label"
const val EXTRA_TODO_QUICK_ACTION_MODE = "extra_todo_quick_action_mode"
const val TODO_QUICK_ACTION_ADD_TASK = "add_task"
const val TODO_QUICK_ACTION_SCHEDULE = "schedule"
const val EXTRA_AUDIO_RECORDER_QUICK_ACTION = "extra_audio_recorder_quick_action"
const val AUDIO_RECORDER_QUICK_ACTION_START_MIC = "start_mic"
const val AUDIO_RECORDER_QUICK_ACTION_START_DEVICE = "start_device"
const val AUDIO_RECORDER_QUICK_ACTION_START_BOTH = "start_both"
const val EXTRA_SCREEN_RECORDER_QUICK_ACTION = "extra_screen_recorder_quick_action"
const val SCREEN_RECORDER_QUICK_ACTION_START_MIC = "start_mic"
const val SCREEN_RECORDER_QUICK_ACTION_START_DEVICE = "start_device"
const val SCREEN_RECORDER_QUICK_ACTION_START_BOTH = "start_both"
const val EXTRA_PLANT_SCHEDULE_MONTH = "extra_plant_schedule_month"
const val EXTRA_PLANT_SCHEDULE_OPEN_SEARCH = "extra_plant_schedule_open_search"
const val EXTRA_CAMERA_CAPTURE_MODE = "extra_camera_capture_mode"
const val EXTRA_CALENDAR_DATE_KEY = "extra_calendar_date_key"
const val EXTRA_CALENDAR_OPEN_ADD_EVENT = "extra_calendar_open_add_event"
const val EXTRA_ELEMENTS_ATOMIC_NUMBER = "extra_elements_atomic_number"
const val EXTRA_ELEMENTS_OPEN_INSPECTOR = "extra_elements_open_inspector"
const val EXTRA_ELEMENTS_OPEN_SEARCH = "extra_elements_open_search"
const val EXTRA_ELEMENTS_OPEN_MOLAR_MASS = "extra_elements_open_molar_mass"
const val EXTRA_CALENDAR_VIEW_MODE = "extra_calendar_view_mode"

const val STANDALONE_SHORTCUT_INTENT_FLAGS =
  Intent.FLAG_ACTIVITY_NEW_TASK or
    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
    Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS

/**
 * Normalizes a raw folder path to its canonical absolute normalized form so shortcuts and task
 * matchers treat equivalent paths (such as trailing slashes or relative segments) as the same
 * folder location.
 */
fun normalizeShortcutFolderPath(folderPath: String?): String? {
  if (folderPath.isNullOrBlank()) return null
  return File(folderPath).absoluteFile.normalize().path
}

/**
 * Builds the canonical `toolbox://standalone/<tool>` [Uri] (including optional `folder` query parameter)
 * used as `Intent.data` so Android's document task matcher opens each distinct tool or folder
 * location in its own app instance while reusing an existing standalone instance of the same tool
 * or folder location.
 */
fun resolveStandaloneDocumentUri(
  tool: Tool,
  folderPath: String? = null,
): android.net.Uri {
  val normalizedFolder = normalizeShortcutFolderPath(folderPath)
  val builder = android.net.Uri.Builder()
    .scheme("toolbox")
    .authority("standalone")
    .appendPath(tool.name)
  if (!normalizedFolder.isNullOrBlank()) {
    builder.appendQueryParameter("folder", normalizedFolder)
  }
  return builder.build()
}

private const val SHORTCUT_ICON_SIZE_PX = 216
private const val SHORTCUT_ADAPTIVE_VIEWPORT_DP = 108f
private const val SHORTCUT_FOREGROUND_ICON_DP = 50f

/**
 * Resolves the manifest `<activity-alias>` class name dedicated to launching [tool] as a standalone
 * shortcut so the Recent Apps switcher displays the tool's icon and title.
 */
fun resolveToolShortcutAliasClassName(tool: Tool): String {
  return when (tool) {
    Tool.CALCULATOR -> "com.example.toolbox.MainActivityToolCalculator"
    Tool.NOTES -> "com.example.toolbox.MainActivityToolNotes"
    Tool.TODO_LIST -> "com.example.toolbox.MainActivityToolTodoList"
    Tool.CAMERA -> "com.example.toolbox.MainActivityToolCamera"
    Tool.GALLERY -> "com.example.toolbox.MainActivityToolGallery"
    Tool.FILES -> "com.example.toolbox.MainActivityToolFiles"
    Tool.UNIT_CONVERTER -> "com.example.toolbox.MainActivityToolUnitConverter"
    Tool.AUDIO_RECORDER -> "com.example.toolbox.MainActivityToolAudioRecorder"
    Tool.BLUETOOTH_REMOTE -> "com.example.toolbox.MainActivityToolBluetoothRemote"
    Tool.IMAGE -> "com.example.toolbox.MainActivityToolImage"
    Tool.IMAGE_EDITOR -> "com.example.toolbox.MainActivityToolImageEditor"
    Tool.FILE_CONVERTER -> "com.example.toolbox.MainActivityToolFileConverter"
    Tool.RULER -> "com.example.toolbox.MainActivityToolRuler"
    Tool.VIDEO_EDITOR -> "com.example.toolbox.MainActivityToolVideoEditor"
    Tool.SCREEN_RECORDER -> "com.example.toolbox.MainActivityToolScreenRecorder"
    Tool.AUDIO_EDITOR -> "com.example.toolbox.MainActivityToolAudioEditor"
    Tool.COLOR_PICKER -> "com.example.toolbox.MainActivityToolColorPicker"
    Tool.DICE_ROLLER -> "com.example.toolbox.MainActivityToolDiceRoller"
    Tool.GUITAR_TUNER -> "com.example.toolbox.MainActivityToolGuitarTuner"
    Tool.METRONOME -> "com.example.toolbox.MainActivityToolMetronome"
    Tool.CHORD_BOOK -> "com.example.toolbox.MainActivityToolChordBook"
    Tool.PLANT_SCHEDULE -> "com.example.toolbox.MainActivityToolPlantSchedule"
    Tool.BLUETOOTH_GAMEPAD -> "com.example.toolbox.MainActivityToolBluetoothGamepad"
    Tool.DECIBEL_METER -> "com.example.toolbox.MainActivityToolDecibelMeter"
    Tool.ARCHIVE -> "com.example.toolbox.MainActivityToolArchive"
    Tool.MUSIC -> "com.example.toolbox.MainActivityToolMusicPlayer"
    Tool.PDF_VIEWER -> "com.example.toolbox.MainActivityToolPdfViewer"
    Tool.RECIPES -> "com.example.toolbox.MainActivityToolRecipeBook"
    Tool.OBD2_SCANNER -> "com.example.toolbox.MainActivityToolObd2Scanner"
    Tool.ELEMENTS -> "com.example.toolbox.MainActivityToolElements"
    Tool.GOOD -> "com.example.toolbox.MainActivityToolGood"
    Tool.STARS -> "com.example.toolbox.MainActivityToolStars"
    Tool.CALENDAR -> "com.example.toolbox.MainActivityToolCalendar"
    Tool.SEARCH -> "com.example.toolbox.MainActivityToolSearch"
    Tool.TERMINAL -> "com.example.toolbox.MainActivityToolTerminal"
    Tool.SETTINGS -> "com.example.toolbox.MainActivityToolSettings"
  }
}

/**
 * Resolves the `<adaptive-icon>` mipmap resource ID for [tool], sharing the system preset
 * background (`@drawable/ic_launcher_background`) and providing a `<monochrome>` layer for
 * Android 13+ themed home screen icons.
 */
@DrawableRes
fun resolveToolShortcutMipmapResId(tool: Tool): Int {
  return when (tool) {
    Tool.CALCULATOR -> R.mipmap.ic_tool_calculator
    Tool.NOTES -> R.mipmap.ic_tool_notes
    Tool.TODO_LIST -> R.mipmap.ic_tool_todo_list
    Tool.CAMERA -> R.mipmap.ic_tool_camera
    Tool.GALLERY -> R.mipmap.ic_tool_gallery
    Tool.FILES -> R.mipmap.ic_tool_files
    Tool.UNIT_CONVERTER -> R.mipmap.ic_tool_unit_converter
    Tool.AUDIO_RECORDER -> R.mipmap.ic_tool_audio_recorder
    Tool.BLUETOOTH_REMOTE -> R.mipmap.ic_tool_bluetooth_remote
    Tool.IMAGE -> R.mipmap.ic_tool_image
    Tool.IMAGE_EDITOR -> R.mipmap.ic_tool_image_editor
    Tool.FILE_CONVERTER -> R.mipmap.ic_tool_file_converter
    Tool.RULER -> R.mipmap.ic_tool_ruler
    Tool.VIDEO_EDITOR -> R.mipmap.ic_tool_video_editor
    Tool.SCREEN_RECORDER -> R.mipmap.ic_tool_screen_recorder
    Tool.AUDIO_EDITOR -> R.mipmap.ic_tool_audio_editor
    Tool.COLOR_PICKER -> R.mipmap.ic_tool_color_picker
    Tool.DICE_ROLLER -> R.mipmap.ic_tool_dice_roller
    Tool.GUITAR_TUNER -> R.mipmap.ic_tool_guitar_tuner
    Tool.METRONOME -> R.mipmap.ic_tool_metronome
    Tool.CHORD_BOOK -> R.mipmap.ic_tool_chord_book
    Tool.PLANT_SCHEDULE -> R.mipmap.ic_tool_plant_schedule
    Tool.BLUETOOTH_GAMEPAD -> R.mipmap.ic_tool_bluetooth_gamepad
    Tool.DECIBEL_METER -> R.mipmap.ic_tool_decibel_meter
    Tool.ARCHIVE -> R.mipmap.ic_tool_archive
    Tool.MUSIC -> R.mipmap.ic_tool_music_player
    Tool.PDF_VIEWER -> R.mipmap.ic_tool_pdf_viewer
    Tool.RECIPES -> R.mipmap.ic_tool_recipe_book
    Tool.OBD2_SCANNER -> R.mipmap.ic_tool_obd2_scanner
    Tool.ELEMENTS -> R.mipmap.ic_tool_elements
    Tool.GOOD -> R.mipmap.ic_tool_good
    Tool.STARS -> R.mipmap.ic_tool_stars
    Tool.CALENDAR -> R.mipmap.ic_tool_calendar
    Tool.SEARCH -> R.mipmap.ic_tool_search
    Tool.TERMINAL -> R.mipmap.ic_tool_terminal
    Tool.SETTINGS -> R.mipmap.ic_tool_settings
  }
}

/**
 * Parses a [Tool] from the target `<activity-alias>` component class name, or `null` if it does not
 * match a tool shortcut alias.
 */
fun parseStandaloneToolFromComponent(className: String?): Tool? {
  if (className.isNullOrBlank()) return null
  return Tool.entries.find { tool ->
    val alias = resolveToolShortcutAliasClassName(tool)
    className == alias || className.endsWith(".${alias.substringAfterLast('.')}")
  }
}

/**
 * Parses the standalone tool enum value from a raw string name.
 */
fun parseStandaloneToolName(name: String?): Tool? {
  return name?.let {
    try {
      Tool.valueOf(it)
    } catch (_: IllegalArgumentException) {
      null
    }
  }
}

/**
 * Parses the standalone tool requested in an incoming [Intent] if launched via home screen shortcut.
 */
fun parseStandaloneTool(intent: Intent?): Tool? {
  val name = intent?.getStringExtra(EXTRA_STANDALONE_TOOL)
    ?: intent?.data?.getQueryParameter("tool")
    ?: intent?.data?.lastPathSegment
  return parseStandaloneToolName(name)
    ?: parseStandaloneToolFromComponent(intent?.component?.className)
}

/**
 * Parses the normalized folder path requested in an incoming [Intent] if launched via a folder home screen shortcut.
 */
fun parseFolderShortcut(intent: Intent?): String? {
  val raw = intent?.getStringExtra(EXTRA_FOLDER_PATH)
    ?: intent?.data?.getQueryParameter("folder")
  return normalizeShortcutFolderPath(raw)
}

/**
 * Returns `true` if two `(tool, folderPath)` pairs refer to the same standalone app instance target.
 */
fun isSameStandaloneTarget(
  firstTool: Tool?,
  firstFolder: String?,
  secondTool: Tool?,
  secondFolder: String?,
): Boolean {
  val normFirstFolder = normalizeShortcutFolderPath(firstFolder)
  val normSecondFolder = normalizeShortcutFolderPath(secondFolder)
  val effectiveFirstTool = if (normFirstFolder != null) (firstTool ?: Tool.FILES) else firstTool
  val effectiveSecondTool = if (normSecondFolder != null) (secondTool ?: Tool.FILES) else secondTool
  return effectiveFirstTool == effectiveSecondTool && normFirstFolder == normSecondFolder
}

/**
 * Searches [ActivityManager.getAppTasks] for an already-open standalone task matching
 * ([targetTool], [targetFolder]) that is distinct from [excludeTaskId].
 */
fun findExistingStandaloneAppTask(
  context: Context,
  targetTool: Tool?,
  targetFolder: String?,
  excludeTaskId: Int? = null,
): ActivityManager.AppTask? {
  val normalizedTargetFolder = normalizeShortcutFolderPath(targetFolder)
  val effectiveTargetTool = if (normalizedTargetFolder != null) (targetTool ?: Tool.FILES) else targetTool
  if (effectiveTargetTool == null && normalizedTargetFolder == null) return null
  return try {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
      ?: return null
    activityManager.appTasks.firstOrNull { appTask ->
      val taskInfo = try {
        appTask.taskInfo
      } catch (_: Exception) {
        null
      } ?: return@firstOrNull false
      @Suppress("DEPRECATION")
      val taskId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) taskInfo.taskId else taskInfo.id
      if (taskId == -1 || (excludeTaskId != null && taskId == excludeTaskId)) {
        return@firstOrNull false
      }
      @Suppress("DEPRECATION")
      val isAlive = taskInfo.numActivities > 0 || taskInfo.topActivity != null
      if (!isAlive) {
        return@firstOrNull false
      }
      val baseIntent = taskInfo.baseIntent
      val taskFolder = parseFolderShortcut(baseIntent)
      val taskTool = parseStandaloneTool(baseIntent) ?: if (taskFolder != null) Tool.FILES else null
      isSameStandaloneTarget(taskTool, taskFolder, effectiveTargetTool, normalizedTargetFolder)
    }
  } catch (_: Exception) {
    null
  }
}

/**
 * Returns `true` if the incoming [Intent] requests opening the Music Player directly into the
 * expanded full player view (for example, when launched by tapping the Android system media player).
 */
fun parseExpandMusicPlayer(intent: Intent?): Boolean {
  return intent?.getBooleanExtra(EXTRA_EXPAND_MUSIC_PLAYER, false) == true ||
    intent?.data?.getQueryParameter("expand_player")?.equals("true", ignoreCase = true) == true
}

/**
 * Parses the requested Notes quick-capture mode ([NOTES_QUICK_CAPTURE_NOTE] or
 * [NOTES_QUICK_CAPTURE_CHECKLIST]) from an incoming [Intent], or `null` if none was requested.
 */
fun parseNotesQuickCaptureMode(intent: Intent?): String? {
  val raw = intent?.getStringExtra(EXTRA_NOTES_QUICK_CAPTURE_MODE)
    ?: intent?.data?.getQueryParameter("notes_mode")
    ?: return null
  return when {
    raw.equals(NOTES_QUICK_CAPTURE_NOTE, ignoreCase = true) -> NOTES_QUICK_CAPTURE_NOTE
    raw.equals(NOTES_QUICK_CAPTURE_CHECKLIST, ignoreCase = true) -> NOTES_QUICK_CAPTURE_CHECKLIST
    else -> null
  }
}

/**
 * Creates an [Intent] that launches [Tool.NOTES] directly into creating a new text note
 * ([isChecklist] = `false`) or a new checklist note ([isChecklist] = `true`), optionally
 * pre-populating [pinned] or [label] according to the widget's filter configuration.
 */
fun createNotesQuickCaptureIntent(
  context: Context,
  isChecklist: Boolean,
  pinned: Boolean = false,
  label: String? = null,
): Intent {
  val mode = if (isChecklist) NOTES_QUICK_CAPTURE_CHECKLIST else NOTES_QUICK_CAPTURE_NOTE
  return createStandaloneToolIntent(context, Tool.NOTES).apply {
    putExtra(EXTRA_NOTES_QUICK_CAPTURE_MODE, mode)
    when {
      !label.isNullOrBlank() -> {
        putExtra(EXTRA_NOTES_INITIAL_FILTER_MODE, NOTES_FILTER_MODE_LABEL)
        putExtra(EXTRA_NOTES_INITIAL_FILTER_LABEL, label)
      }
      pinned -> {
        putExtra(EXTRA_NOTES_INITIAL_FILTER_MODE, NOTES_FILTER_MODE_PINNED)
      }
    }
  }
}

/**
 * Creates an [Intent] that launches [Tool.NOTES] filtered to [filterMode] ([NOTES_FILTER_MODE_ALL],
 * [NOTES_FILTER_MODE_PINNED], or [NOTES_FILTER_MODE_LABEL] with [label]) and optionally opens a
 * specific [noteId] in the single-note editor.
 */
fun createNotesOpenIntent(
  context: Context,
  filterMode: String = NOTES_FILTER_MODE_ALL,
  label: String? = null,
  noteId: String? = null,
): Intent {
  return createStandaloneToolIntent(context, Tool.NOTES).apply {
    putExtra(EXTRA_NOTES_INITIAL_FILTER_MODE, filterMode)
    if (!label.isNullOrBlank()) {
      putExtra(EXTRA_NOTES_INITIAL_FILTER_LABEL, label)
    }
    if (!noteId.isNullOrBlank()) {
      putExtra(EXTRA_NOTES_OPEN_NOTE_ID, noteId)
    }
  }
}

/**
 * Parses the requested To-Do quick-action mode ([TODO_QUICK_ACTION_ADD_TASK] or
 * [TODO_QUICK_ACTION_SCHEDULE]) from an incoming [Intent], or `null` if none was requested.
 */
fun parseTodoQuickActionMode(intent: Intent?): String? {
  val raw = intent?.getStringExtra(EXTRA_TODO_QUICK_ACTION_MODE)
    ?: intent?.data?.getQueryParameter("todo_action")
    ?: return null
  return when {
    raw.equals(TODO_QUICK_ACTION_ADD_TASK, ignoreCase = true) -> TODO_QUICK_ACTION_ADD_TASK
    raw.equals(TODO_QUICK_ACTION_SCHEDULE, ignoreCase = true) -> TODO_QUICK_ACTION_SCHEDULE
    else -> null
  }
}

/**
 * Creates an [Intent] that launches [Tool.TODO_LIST] with either the "Add a task..." input focused
 * ([openSchedule] = `false`) or the schedule view opened ([openSchedule] = `true`).
 */
fun createTodoQuickActionIntent(
  context: Context,
  openSchedule: Boolean,
): Intent {
  val mode = if (openSchedule) TODO_QUICK_ACTION_SCHEDULE else TODO_QUICK_ACTION_ADD_TASK
  return createStandaloneToolIntent(context, Tool.TODO_LIST).apply {
    putExtra(EXTRA_TODO_QUICK_ACTION_MODE, mode)
  }
}

/**
 * Parses the requested Audio Recorder quick-action mode ([AUDIO_RECORDER_QUICK_ACTION_START_MIC],
 * [AUDIO_RECORDER_QUICK_ACTION_START_DEVICE], or [AUDIO_RECORDER_QUICK_ACTION_START_BOTH])
 * from an incoming [Intent], or `null` if none was requested.
 */
fun parseAudioRecorderQuickAction(intent: Intent?): String? {
  val raw = intent?.getStringExtra(EXTRA_AUDIO_RECORDER_QUICK_ACTION)
    ?: intent?.data?.getQueryParameter("audio_recorder_action")
    ?: return null
  return when {
    raw.equals(AUDIO_RECORDER_QUICK_ACTION_START_MIC, ignoreCase = true) ->
      AUDIO_RECORDER_QUICK_ACTION_START_MIC
    raw.equals(AUDIO_RECORDER_QUICK_ACTION_START_DEVICE, ignoreCase = true) ->
      AUDIO_RECORDER_QUICK_ACTION_START_DEVICE
    raw.equals(AUDIO_RECORDER_QUICK_ACTION_START_BOTH, ignoreCase = true) ->
      AUDIO_RECORDER_QUICK_ACTION_START_BOTH
    else -> null
  }
}

/**
 * Creates an [Intent] that launches [Tool.AUDIO_RECORDER] and optionally immediately begins
 * recording with the requested [quickActionMode] ([AUDIO_RECORDER_QUICK_ACTION_START_MIC],
 * [AUDIO_RECORDER_QUICK_ACTION_START_DEVICE], or [AUDIO_RECORDER_QUICK_ACTION_START_BOTH]).
 */
fun createAudioRecorderQuickActionIntent(
  context: Context,
  startRecording: Boolean = true,
  quickActionMode: String = AUDIO_RECORDER_QUICK_ACTION_START_MIC,
): Intent {
  return createStandaloneToolIntent(context, Tool.AUDIO_RECORDER).apply {
    if (startRecording) {
      putExtra(EXTRA_AUDIO_RECORDER_QUICK_ACTION, quickActionMode)
    }
  }
}

/**
 * Parses the requested Screen Recorder quick-action mode ([SCREEN_RECORDER_QUICK_ACTION_START_MIC],
 * [SCREEN_RECORDER_QUICK_ACTION_START_DEVICE], or [SCREEN_RECORDER_QUICK_ACTION_START_BOTH])
 * from an incoming [Intent], or `null` if none was requested.
 */
fun parseScreenRecorderQuickAction(intent: Intent?): String? {
  val raw = intent?.getStringExtra(EXTRA_SCREEN_RECORDER_QUICK_ACTION)
    ?: intent?.data?.getQueryParameter("screen_recorder_action")
    ?: return null
  return when {
    raw.equals(SCREEN_RECORDER_QUICK_ACTION_START_MIC, ignoreCase = true) ->
      SCREEN_RECORDER_QUICK_ACTION_START_MIC
    raw.equals(SCREEN_RECORDER_QUICK_ACTION_START_DEVICE, ignoreCase = true) ->
      SCREEN_RECORDER_QUICK_ACTION_START_DEVICE
    raw.equals(SCREEN_RECORDER_QUICK_ACTION_START_BOTH, ignoreCase = true) ->
      SCREEN_RECORDER_QUICK_ACTION_START_BOTH
    else -> null
  }
}

/**
 * Creates an [Intent] that launches [Tool.SCREEN_RECORDER] and optionally immediately begins
 * recording with the requested [quickActionMode] ([SCREEN_RECORDER_QUICK_ACTION_START_MIC],
 * [SCREEN_RECORDER_QUICK_ACTION_START_DEVICE], or [SCREEN_RECORDER_QUICK_ACTION_START_BOTH]).
 */
fun createScreenRecorderQuickActionIntent(
  context: Context,
  startRecording: Boolean = true,
  quickActionMode: String = SCREEN_RECORDER_QUICK_ACTION_START_MIC,
): Intent {
  return createStandaloneToolIntent(context, Tool.SCREEN_RECORDER).apply {
    if (startRecording) {
      putExtra(EXTRA_SCREEN_RECORDER_QUICK_ACTION, quickActionMode)
    }
  }
}

/**
 * Parses the requested focus month (`1..12`) for [Tool.PLANT_SCHEDULE] from an incoming [Intent],
 * or `null` if none was specified.
 */
fun parsePlantScheduleMonth(intent: Intent?): Int? {
  val fromExtra = intent?.getIntExtra(EXTRA_PLANT_SCHEDULE_MONTH, -1)?.takeIf { it in 1..12 }
  if (fromExtra != null) return fromExtra
  val fromQuery = intent?.data?.getQueryParameter("plant_month")?.toIntOrNull()?.takeIf { it in 1..12 }
  return fromQuery
}

/**
 * Returns `true` if the incoming [Intent] requests opening [Tool.PLANT_SCHEDULE] with the
 * plant catalog search overlay expanded.
 */
fun parsePlantScheduleOpenSearch(intent: Intent?): Boolean {
  return intent?.getBooleanExtra(EXTRA_PLANT_SCHEDULE_OPEN_SEARCH, false) == true ||
    intent?.data?.getQueryParameter("plant_search")?.equals("true", ignoreCase = true) == true
}

/**
 * Creates an [Intent] that launches [Tool.PLANT_SCHEDULE] focused on [monthNumber] (`1..12`)
 * and optionally opens the plant catalog search overlay ([openCatalogSearch] = `true`).
 */
fun createPlantScheduleWidgetIntent(
  context: Context,
  monthNumber: Int? = null,
  openCatalogSearch: Boolean = false,
): Intent {
  return createStandaloneToolIntent(context, Tool.PLANT_SCHEDULE).apply {
    if (monthNumber != null && monthNumber in 1..12) {
      putExtra(EXTRA_PLANT_SCHEDULE_MONTH, monthNumber)
    }
    if (openCatalogSearch) {
      putExtra(EXTRA_PLANT_SCHEDULE_OPEN_SEARCH, true)
    }
  }
}

/**
 * Parses the requested Camera capture mode name from an incoming [Intent], or `null` if none was requested.
 */
fun parseCameraCaptureModeName(intent: Intent?): String? {
  val raw = intent?.getStringExtra(EXTRA_CAMERA_CAPTURE_MODE)
    ?: intent?.data?.getQueryParameter("camera_mode")
    ?: return null
  return raw.trim().takeIf { it.isNotEmpty() }
}

/**
 * Creates an [Intent] that launches [Tool.CAMERA] directly into the specified capture [modeName].
 */
fun createCameraModeQuickActionIntent(
  context: Context,
  modeName: String,
): Intent {
  return createStandaloneToolIntent(context, Tool.CAMERA).apply {
    putExtra(EXTRA_CAMERA_CAPTURE_MODE, modeName)
  }
}

/**
 * Parses the requested focus date key (`yyyy-MM-dd`) for [Tool.CALENDAR] from an incoming [Intent],
 * or `null` if none was specified.
 */
fun parseCalendarDateKey(intent: Intent?): String? {
  val fromExtra = intent?.getStringExtra(EXTRA_CALENDAR_DATE_KEY)?.trim()?.takeIf { it.isNotEmpty() }
  if (fromExtra != null) return fromExtra
  return intent?.data?.getQueryParameter("calendar_date")?.trim()?.takeIf { it.isNotEmpty() }
}

/**
 * Returns `true` if the incoming [Intent] requests opening [Tool.CALENDAR] with the
 * New Calendar Event dialog open.
 */
fun parseCalendarOpenAddEvent(intent: Intent?): Boolean {
  return intent?.getBooleanExtra(EXTRA_CALENDAR_OPEN_ADD_EVENT, false) == true ||
    intent?.data?.getQueryParameter("calendar_add")?.equals("true", ignoreCase = true) == true
}

/**
 * Parses the requested [com.example.toolbox.ui.tools.CalendarViewMode] name (`SCHEDULE`, `MONTH`, `WEEK`, `DAY`)
 * for [Tool.CALENDAR] from an incoming [Intent], or `null` if none was specified.
 */
fun parseCalendarViewModeName(intent: Intent?): String? {
  val fromExtra = intent?.getStringExtra(EXTRA_CALENDAR_VIEW_MODE)?.trim()?.takeIf { it.isNotEmpty() }
  if (fromExtra != null) return fromExtra
  return intent?.data?.getQueryParameter("calendar_view")?.trim()?.takeIf { it.isNotEmpty() }
}

/**
 * Creates an [Intent] that launches [Tool.CALENDAR] focused on [dateKey] (`yyyy-MM-dd`),
 * optionally switches to [viewModeName], and optionally opens the New Calendar Event dialog ([openAddEvent] = `true`).
 */
fun createCalendarWidgetIntent(
  context: Context,
  dateKey: String? = null,
  openAddEvent: Boolean = false,
  viewModeName: String? = null,
): Intent {
  return createStandaloneToolIntent(context, Tool.CALENDAR).apply {
    if (!dateKey.isNullOrBlank()) {
      putExtra(EXTRA_CALENDAR_DATE_KEY, dateKey)
    }
    if (openAddEvent) {
      putExtra(EXTRA_CALENDAR_OPEN_ADD_EVENT, true)
    }
    if (!viewModeName.isNullOrBlank()) {
      putExtra(EXTRA_CALENDAR_VIEW_MODE, viewModeName)
    }
  }
}

/**
 * Parses the requested element atomic number (`1..118`) for [Tool.ELEMENTS] from an incoming [Intent],
 * or `null` if none was specified.
 */
fun parseElementsAtomicNumber(intent: Intent?): Int? {
  val fromExtra = intent?.getIntExtra(EXTRA_ELEMENTS_ATOMIC_NUMBER, -1)?.takeIf { it in 1..118 }
  if (fromExtra != null) return fromExtra
  return intent?.data?.getQueryParameter("element_z")?.toIntOrNull()?.takeIf { it in 1..118 }
}

/**
 * Returns `true` if the incoming [Intent] requests opening [Tool.ELEMENTS] directly in the
 * full-screen Element Inspector view.
 */
fun parseElementsOpenInspector(intent: Intent?): Boolean {
  return intent?.getBooleanExtra(EXTRA_ELEMENTS_OPEN_INSPECTOR, false) == true ||
    intent?.data?.getQueryParameter("element_inspect")?.equals("true", ignoreCase = true) == true
}

/**
 * Returns `true` if the incoming [Intent] requests opening [Tool.ELEMENTS] with the search
 * catalog overlay active.
 */
fun parseElementsOpenSearch(intent: Intent?): Boolean {
  return intent?.getBooleanExtra(EXTRA_ELEMENTS_OPEN_SEARCH, false) == true ||
    intent?.data?.getQueryParameter("element_search")?.equals("true", ignoreCase = true) == true
}

/**
 * Returns `true` if the incoming [Intent] requests opening [Tool.ELEMENTS] in the Molar Mass
 * calculator tab.
 */
fun parseElementsOpenMolarMass(intent: Intent?): Boolean {
  return intent?.getBooleanExtra(EXTRA_ELEMENTS_OPEN_MOLAR_MASS, false) == true ||
    intent?.data?.getQueryParameter("element_molar_mass")?.equals("true", ignoreCase = true) == true
}

/**
 * Creates an [Intent] that launches [Tool.ELEMENTS] focused on [atomicNumber] (`1..118`) and
 * optionally opens the full-screen Element Inspector ([openInspector] = `true`), the search
 * overlay ([openSearch] = `true`), or the Molar Mass Calculator tab ([openMolarMass] = `true`).
 */
fun createElementsWidgetIntent(
  context: Context,
  atomicNumber: Int? = null,
  openInspector: Boolean = false,
  openSearch: Boolean = false,
  openMolarMass: Boolean = false,
): Intent {
  return createStandaloneToolIntent(context, Tool.ELEMENTS).apply {
    if (atomicNumber != null && atomicNumber in 1..118) {
      putExtra(EXTRA_ELEMENTS_ATOMIC_NUMBER, atomicNumber)
    }
    if (openInspector) {
      putExtra(EXTRA_ELEMENTS_OPEN_INSPECTOR, true)
    }
    if (openSearch) {
      putExtra(EXTRA_ELEMENTS_OPEN_SEARCH, true)
    }
    if (openMolarMass) {
      putExtra(EXTRA_ELEMENTS_OPEN_MOLAR_MASS, true)
    }
  }
}

/**
 * Resolves the label to display in the Recent Apps switcher for a standalone tool or folder shortcut,
 * or `null` when running the main Toolbox application.
 */
fun resolveStandaloneTaskLabel(
  standaloneTool: Tool?,
  initialFolder: String? = null,
): String? {
  if (initialFolder != null) {
    val folderName = File(initialFolder).name
    return folderName.ifBlank { standaloneTool?.title ?: Tool.FILES.title }
  }
  return standaloneTool?.title
}

/**
 * Resolves the [ImageVector] icon to display in the Recent Apps switcher for a standalone tool or
 * folder shortcut, or `null` when running the main Toolbox application.
 */
fun resolveStandaloneTaskIconVector(
  standaloneTool: Tool?,
  initialFolder: String? = null,
): ImageVector? {
  if (initialFolder != null) {
    return FOLDER_SHORTCUT_ICON
  }
  return standaloneTool?.icon
}

/**
 * Updates [activity]'s [ActivityManager.TaskDescription] so the Recent Apps switcher displays the
 * matching tool (or folder) icon, title, and system preset background color when opened from a shortcut.
 */
fun syncStandaloneTaskDescription(
  activity: Activity,
  standaloneTool: Tool?,
  initialFolder: String? = null,
) {
  val label = resolveStandaloneTaskLabel(standaloneTool, initialFolder) ?: return
  val iconVector = resolveStandaloneTaskIconVector(standaloneTool, initialFolder) ?: return
  try {
    val backgroundColor = resolveShortcutBackgroundColor(context = activity)
    val foregroundColor = resolveShortcutForegroundColor(backgroundColor)
    val iconBitmap = createToolShortcutBitmap(
      imageVector = iconVector,
      backgroundColor = backgroundColor,
      foregroundColor = foregroundColor,
    )
    @Suppress("DEPRECATION")
    val taskDescription = ActivityManager.TaskDescription(
      label,
      iconBitmap,
      backgroundColor.toArgb(),
    )
    activity.setTaskDescription(taskDescription)
  } catch (_: Exception) {
    // Ignore when Bitmap/TaskDescription is unavailable in unit tests
  }
}

/**
 * Creates the [Intent] used to launch [tool] in a standalone instance without navigation, reusing
 * an existing standalone task for [tool] if one is already open.
 */
fun createStandaloneToolIntent(context: Context, tool: Tool): Intent {
  return Intent(context, MainActivity::class.java).apply {
    component = ComponentName(context, resolveToolShortcutAliasClassName(tool))
    action = Intent.ACTION_VIEW
    data = resolveStandaloneDocumentUri(tool)
    putExtra(EXTRA_STANDALONE_TOOL, tool.name)
    addFlags(STANDALONE_SHORTCUT_INTENT_FLAGS)
  }
}

/**
 * Creates the [Intent] used to launch [Tool.FILES] at [folder] from a home screen shortcut, reusing
 * an existing standalone task for [folder] if one is already open.
 */
fun createFolderShortcutIntent(context: Context, folder: File): Intent {
  val normalizedPath = normalizeShortcutFolderPath(folder.path) ?: folder.absolutePath
  return Intent(context, MainActivity::class.java).apply {
    component = ComponentName(context, resolveToolShortcutAliasClassName(Tool.FILES))
    action = Intent.ACTION_VIEW
    data = resolveStandaloneDocumentUri(Tool.FILES, normalizedPath)
    putExtra(EXTRA_STANDALONE_TOOL, Tool.FILES.name)
    putExtra(EXTRA_FOLDER_PATH, normalizedPath)
    addFlags(STANDALONE_SHORTCUT_INTENT_FLAGS)
  }
}

/**
 * Resolves the background [Color] for shortcut icons according to [themeColor].
 *
 * When [themeColor] is a valid hex string (preset or custom), returns the parsed color.
 * When [themeColor] is [DEFAULT_THEME_COLOR_SYSTEM] (or null/invalid), uses Toolbox Purple (`#8E66A6`)
 * to match the default app launcher icon (`@drawable/ic_launcher_background`), while in-app
 * theme colors continue to follow the user's selection in Settings.
 */
fun resolveShortcutBackgroundColor(
  themeColor: String? = null,
  context: Context? = null,
): Color {
  val effectiveThemeColor = themeColor ?: run {
    if (context != null) {
      try {
        DefaultNavigationPreferencesRepository(context.applicationContext ?: context).getThemeColor()
      } catch (_: Exception) {
        DEFAULT_THEME_COLOR_SYSTEM
      }
    } else {
      DEFAULT_THEME_COLOR_SYSTEM
    }
  }

  if (!effectiveThemeColor.equals(DEFAULT_THEME_COLOR_SYSTEM, ignoreCase = true)) {
    parseHexColor(effectiveThemeColor)?.let { return it }
  }

  return parseHexColor(FALLBACK_THEME_COLOR_HEX) ?: Color(0xFF8E66A6)
}

/**
 * Resolves a contrasting foreground [Color] for drawing a tool's icon on top of [backgroundColor].
 */
fun resolveShortcutForegroundColor(backgroundColor: Color): Color {
  return if (backgroundColor.luminance() > 0.5f) Color.Black else Color.White
}

/**
 * Renders an adaptive shortcut icon [Bitmap] displaying [imageVector] centered over [backgroundColor].
 */
fun createToolShortcutBitmap(
  imageVector: ImageVector,
  backgroundColor: Color,
  foregroundColor: Color = resolveShortcutForegroundColor(backgroundColor),
  sizePx: Int = SHORTCUT_ICON_SIZE_PX,
): Bitmap {
  val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(bitmap.asImageBitmap())

  // Draw background
  val bgPaint = Paint().apply {
    isAntiAlias = true
    style = PaintingStyle.Fill
    color = backgroundColor
  }
  canvas.drawRect(Rect(0f, 0f, sizePx.toFloat(), sizePx.toFloat()), bgPaint)

  // Scale and center the tool's ImageVector within the adaptive icon safe zone
  val viewportWidth = imageVector.viewportWidth.takeIf { it > 0f } ?: 24f
  val viewportHeight = imageVector.viewportHeight.takeIf { it > 0f } ?: 24f
  val targetIconSizePx = sizePx * (SHORTCUT_FOREGROUND_ICON_DP / SHORTCUT_ADAPTIVE_VIEWPORT_DP)
  val scaleX = targetIconSizePx / viewportWidth
  val scaleY = targetIconSizePx / viewportHeight
  val scale = minOf(scaleX, scaleY)
  val dx = (sizePx - viewportWidth * scale) / 2f
  val dy = (sizePx - viewportHeight * scale) / 2f

  canvas.save()
  canvas.translate(dx, dy)
  canvas.scale(scale, scale)
  drawVectorGroup(canvas, imageVector.root, foregroundColor)
  canvas.restore()

  return bitmap
}

private fun drawVectorGroup(
  canvas: Canvas,
  group: VectorGroup,
  tintColor: Color,
) {
  canvas.save()
  canvas.translate(group.translationX, group.translationY)
  if (group.rotation != 0f) {
    canvas.translate(group.pivotX, group.pivotY)
    canvas.rotate(group.rotation)
    canvas.translate(-group.pivotX, -group.pivotY)
  }
  if (group.scaleX != 1f || group.scaleY != 1f) {
    canvas.translate(group.pivotX, group.pivotY)
    canvas.scale(group.scaleX, group.scaleY)
    canvas.translate(-group.pivotX, -group.pivotY)
  }
  if (group.clipPathData.isNotEmpty()) {
    val clipPath = group.clipPathData.toPath()
    canvas.clipPath(clipPath)
  }
  for (node in group) {
    when (node) {
      is VectorPath -> {
        val path = node.pathData.toPath().apply {
          fillType = node.pathFillType
        }
        if (node.fill != null) {
          val paint = Paint().apply {
            isAntiAlias = true
            style = PaintingStyle.Fill
            color = tintColor.copy(alpha = (tintColor.alpha * node.fillAlpha).coerceIn(0f, 1f))
          }
          canvas.drawPath(path, paint)
        }
        if (node.stroke != null && node.strokeLineWidth > 0f) {
          val paint = Paint().apply {
            isAntiAlias = true
            style = PaintingStyle.Stroke
            strokeWidth = node.strokeLineWidth
            strokeCap = node.strokeLineCap
            strokeJoin = node.strokeLineJoin
            strokeMiterLimit = node.strokeLineMiter
            color = tintColor.copy(alpha = (tintColor.alpha * node.strokeAlpha).coerceIn(0f, 1f))
          }
          canvas.drawPath(path, paint)
        }
      }
      is VectorGroup -> {
        drawVectorGroup(canvas, node, tintColor)
      }
    }
  }
  canvas.restore()
}

/**
 * Creates an [IconCompat] for a given [Tool] shortcut using its system-preset `<adaptive-icon>` resource.
 */
fun createToolShortcutIcon(
  context: Context,
  tool: Tool,
): IconCompat {
  return try {
    IconCompat.createWithResource(context, resolveToolShortcutMipmapResId(tool))
  } catch (_: Exception) {
    try {
      IconCompat.createWithResource(context, R.mipmap.ic_launcher)
    } catch (_: Exception) {
      IconCompat.createWithResource(context, android.R.drawable.sym_def_app_icon)
    }
  }
}

/**
 * The folder [ImageVector] used for folders in the Files tool and for folder Home screen shortcuts.
 */
val FOLDER_SHORTCUT_ICON: ImageVector = Icons.Default.Folder

/**
 * Creates an [IconCompat] for a folder shortcut using the system-preset Files `<adaptive-icon>` resource.
 */
fun createFolderShortcutIcon(
  context: Context,
): IconCompat {
  return try {
    IconCompat.createWithResource(context, R.mipmap.ic_tool_files)
  } catch (_: Exception) {
    try {
      IconCompat.createWithResource(context, R.mipmap.ic_launcher)
    } catch (_: Exception) {
      IconCompat.createWithResource(context, android.R.drawable.sym_def_app_icon)
    }
  }
}

/**
 * Synchronizes already-pinned home screen shortcuts so their launch [Intent]s use the latest
 * standalone instance-reuse flags (`FLAG_ACTIVITY_NEW_DOCUMENT` without `FLAG_ACTIVITY_MULTIPLE_TASK`)
 * and canonical document URI.
 */
fun syncPinnedHomeScreenShortcuts(context: Context) {
  try {
    val existingPinned = ShortcutManagerCompat.getShortcuts(
      context,
      ShortcutManagerCompat.FLAG_MATCH_PINNED,
    )
    if (existingPinned.isEmpty()) return

    val updatedShortcuts = existingPinned.mapNotNull { shortcut ->
      val shortcutId = shortcut.id
      when {
        shortcutId.startsWith("shortcut_tool_") -> {
          val tool = parseStandaloneToolName(shortcutId.removePrefix("shortcut_tool_"))
            ?: parseStandaloneTool(shortcut.intent)
            ?: return@mapNotNull null
          ShortcutInfoCompat.Builder(context, shortcutId)
            .setShortLabel(tool.title)
            .setLongLabel(tool.title)
            .setIcon(createToolShortcutIcon(context, tool))
            .setIntent(createStandaloneToolIntent(context, tool))
            .setAlwaysBadged()
            .build()
        }
        shortcutId.startsWith("shortcut_folder_") -> {
          val folderPath = parseFolderShortcut(shortcut.intent) ?: return@mapNotNull null
          val folder = File(folderPath)
          val folderName = folder.name.ifBlank { "Files" }
          ShortcutInfoCompat.Builder(context, shortcutId)
            .setShortLabel(folderName)
            .setLongLabel(folderName)
            .setIcon(createFolderShortcutIcon(context))
            .setIntent(createFolderShortcutIntent(context, folder))
            .setAlwaysBadged()
            .build()
        }
        else -> null
      }
    }
    if (updatedShortcuts.isNotEmpty()) {
      ShortcutManagerCompat.updateShortcuts(context, updatedShortcuts)
    }
  } catch (_: Exception) {
    // Ignore when ShortcutManager is unavailable in unit tests
  }
}

/**
 * Requests pinning a home screen shortcut to launch [tool] in a standalone instance.
 */
fun addToolToHomeScreen(
  context: Context,
  tool: Tool,
): Boolean {
  val intent = createStandaloneToolIntent(context, tool)

  return try {
    if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
      val pinShortcutInfo = ShortcutInfoCompat.Builder(context, "shortcut_tool_${tool.name}")
        .setShortLabel(tool.title)
        .setLongLabel(tool.title)
        .setIcon(createToolShortcutIcon(context, tool))
        .setIntent(intent)
        .setAlwaysBadged()
        .build()

      ShortcutManagerCompat.updateShortcuts(context, listOf(pinShortcutInfo))
      val pinned = ShortcutManagerCompat.requestPinShortcut(context, pinShortcutInfo, null)
      Toast.makeText(context, "Adding ${tool.title} to Home screen...", Toast.LENGTH_SHORT).show()
      pinned
    } else {
      Toast.makeText(context, "Home screen shortcuts not supported by launcher", Toast.LENGTH_SHORT).show()
      false
    }
  } catch (e: Exception) {
    Toast.makeText(context, "Could not add ${tool.title} to Home screen", Toast.LENGTH_SHORT).show()
    false
  }
}

/**
 * Requests pinning a home screen shortcut to launch [Tool.FILES] navigated directly to [folder].
 */
fun addFolderToHomeScreen(
  context: Context,
  folder: File,
): Boolean {
  val normalizedFolder = folder.absoluteFile.normalize()
  val folderName = normalizedFolder.name.ifBlank { "Files" }
  val intent = createFolderShortcutIntent(context, normalizedFolder)

  return try {
    if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
      val shortcutId = "shortcut_folder_${normalizedFolder.path.hashCode()}"
      val pinShortcutInfo = ShortcutInfoCompat.Builder(context, shortcutId)
        .setShortLabel(folderName)
        .setLongLabel(folderName)
        .setIcon(createFolderShortcutIcon(context))
        .setIntent(intent)
        .setAlwaysBadged()
        .build()

      ShortcutManagerCompat.updateShortcuts(context, listOf(pinShortcutInfo))
      val pinned = ShortcutManagerCompat.requestPinShortcut(context, pinShortcutInfo, null)
      Toast.makeText(context, "Adding $folderName to Home screen...", Toast.LENGTH_SHORT).show()
      pinned
    } else {
      Toast.makeText(context, "Home screen shortcuts not supported by launcher", Toast.LENGTH_SHORT).show()
      false
    }
  } catch (e: Exception) {
    Toast.makeText(context, "Could not add $folderName to Home screen", Toast.LENGTH_SHORT).show()
    false
  }
}
