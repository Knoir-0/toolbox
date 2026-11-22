package com.example.toolbox

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.example.toolbox.ui.main.EXTRA_EXPAND_MUSIC_PLAYER
import com.example.toolbox.ui.main.EXTRA_NOTES_INITIAL_FILTER_LABEL
import com.example.toolbox.ui.main.EXTRA_NOTES_INITIAL_FILTER_MODE
import com.example.toolbox.ui.main.EXTRA_NOTES_OPEN_NOTE_ID
import com.example.toolbox.ui.main.EXTRA_NOTES_QUICK_CAPTURE_MODE
import com.example.toolbox.ui.main.EXTRA_TODO_QUICK_ACTION_MODE
import com.example.toolbox.ui.main.NOTES_FILTER_MODE_ALL
import com.example.toolbox.ui.main.NOTES_FILTER_MODE_LABEL
import com.example.toolbox.ui.main.NOTES_FILTER_MODE_PINNED
import com.example.toolbox.ui.main.NOTES_QUICK_CAPTURE_CHECKLIST
import com.example.toolbox.ui.main.TODO_QUICK_ACTION_SCHEDULE
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.createFolderShortcutIntent
import com.example.toolbox.ui.main.createStandaloneToolIntent
import com.example.toolbox.ui.main.findExistingStandaloneAppTask
import com.example.toolbox.ui.main.isSameStandaloneTarget
import com.example.toolbox.ui.main.parseExpandMusicPlayer
import com.example.toolbox.ui.main.parseFolderShortcut
import com.example.toolbox.ui.main.parseNotesQuickCaptureMode
import com.example.toolbox.ui.main.parseStandaloneTool
import com.example.toolbox.ui.main.parseTodoQuickActionMode
import com.example.toolbox.ui.main.rememberNavigationPreferencesRepository
import com.example.toolbox.ui.main.syncPinnedHomeScreenShortcuts
import com.example.toolbox.ui.main.syncStandaloneTaskDescription
import com.example.toolbox.ui.theme.ToolboxTheme
import com.example.toolbox.ui.tools.MusicPlayerEngine
import com.example.toolbox.ui.tools.NotesEngine
import com.example.toolbox.ui.tools.NotesFilter
import com.example.toolbox.ui.tools.TodoListEngine
import java.io.File

class MainActivity : ComponentActivity() {
  private val standaloneToolState = mutableStateOf<Tool?>(null)
  private val initialFolderState = mutableStateOf<String?>(null)
  private val expandMusicPlayerState = mutableStateOf(false)

  private fun parseNotesFilterFromIntent(intent: Intent?): NotesFilter? {
    val mode = intent?.getStringExtra(EXTRA_NOTES_INITIAL_FILTER_MODE)?.trim() ?: return null
    val label = intent.getStringExtra(EXTRA_NOTES_INITIAL_FILTER_LABEL)?.trim()
    return when {
      mode.equals(NOTES_FILTER_MODE_PINNED, ignoreCase = true) -> NotesFilter.Pinned
      mode.equals(NOTES_FILTER_MODE_LABEL, ignoreCase = true) && !label.isNullOrBlank() ->
        NotesFilter.Label(label)
      mode.equals(NOTES_FILTER_MODE_ALL, ignoreCase = true) -> NotesFilter.All
      else -> null
    }
  }

  private fun applyIncomingToolActions(
    intent: Intent?,
    expandMusicPlayer: Boolean,
    notesQuickCaptureMode: String?,
    todoQuickActionMode: String?,
  ) {
    if (expandMusicPlayer) {
      MusicPlayerEngine.requestExpandFullPlayer()
    }
    val notesFilter = parseNotesFilterFromIntent(intent)
    val openNoteId = intent?.getStringExtra(EXTRA_NOTES_OPEN_NOTE_ID)?.takeIf { it.isNotBlank() }
    val filterLabel = (notesFilter as? NotesFilter.Label)?.name
    val filterPinned = notesFilter == NotesFilter.Pinned
    if (notesQuickCaptureMode != null) {
      NotesEngine.requestQuickCapture(
        isChecklist = notesQuickCaptureMode == NOTES_QUICK_CAPTURE_CHECKLIST,
        initialPinned = filterPinned,
        initialLabel = filterLabel,
      )
    } else if (notesFilter != null || openNoteId != null) {
      NotesEngine.requestWidgetNavigation(
        filter = notesFilter,
        openNoteId = openNoteId,
      )
    }
    if (todoQuickActionMode != null) {
      TodoListEngine.requestQuickAction(
        openSchedule = todoQuickActionMode == TODO_QUICK_ACTION_SCHEDULE,
      )
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    syncPinnedHomeScreenShortcuts(this)
    MusicPlayerEngine.ensureSystemMediaMonitoring(this)
    MusicPlayerEngine.syncExternalSystemMediaSessions(this)
    val parsedTool = parseStandaloneTool(intent)
    val parsedFolder = parseFolderShortcut(intent)
    val expandMusicPlayer = parseExpandMusicPlayer(intent)
    val notesQuickCaptureMode = parseNotesQuickCaptureMode(intent)
    val todoQuickActionMode = parseTodoQuickActionMode(intent)
    val hasNotesNavigationExtras = parseNotesFilterFromIntent(intent) != null ||
      !intent?.getStringExtra(EXTRA_NOTES_OPEN_NOTE_ID).isNullOrBlank()
    val resolvedTool = when {
      parsedFolder != null -> parsedTool ?: Tool.FILES
      expandMusicPlayer -> parsedTool ?: Tool.MUSIC
      notesQuickCaptureMode != null || hasNotesNavigationExtras -> parsedTool ?: Tool.NOTES
      todoQuickActionMode != null -> parsedTool ?: Tool.TODO_LIST
      else -> parsedTool
    }
    if (resolvedTool != null || parsedFolder != null) {
      val existingTask = findExistingStandaloneAppTask(
        context = this,
        targetTool = resolvedTool,
        targetFolder = parsedFolder,
        excludeTaskId = taskId,
      )
      if (existingTask != null) {
        applyIncomingToolActions(
          intent = intent,
          expandMusicPlayer = expandMusicPlayer,
          notesQuickCaptureMode = notesQuickCaptureMode,
          todoQuickActionMode = todoQuickActionMode,
        )
        try {
          existingTask.moveToFront()
          finishAndRemoveTask()
          return
        } catch (_: Exception) {
          // Fall through if moving existing task fails
        }
      }
    }
    standaloneToolState.value = resolvedTool
    initialFolderState.value = parsedFolder
    expandMusicPlayerState.value = expandMusicPlayer
    applyIncomingToolActions(
      intent = intent,
      expandMusicPlayer = expandMusicPlayer,
      notesQuickCaptureMode = notesQuickCaptureMode,
      todoQuickActionMode = todoQuickActionMode,
    )
    syncStandaloneTaskDescription(
      activity = this,
      standaloneTool = resolvedTool,
      initialFolder = parsedFolder,
    )

    onBackPressedDispatcher.addCallback(
      this,
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          if (standaloneToolState.value != null && isTaskRoot) {
            moveTaskToBack(true)
          } else {
            isEnabled = false
            onBackPressedDispatcher.onBackPressed()
            isEnabled = true
          }
        }
      },
    )

    enableEdgeToEdge()
    setContent {
      val preferencesRepository = rememberNavigationPreferencesRepository()
      val themeMode by preferencesRepository.themeModeFlow.collectAsState()
      val themeColor by preferencesRepository.themeColorFlow.collectAsState()
      val musicPlayerThemeEnabled by preferencesRepository.musicPlayerThemeEnabledFlow.collectAsState()
      val musicPlayerColorOption by preferencesRepository.musicPlayerColorOptionFlow.collectAsState()
      val accentColor by preferencesRepository.accentColorFlow.collectAsState()
      val standaloneTool by standaloneToolState
      val initialFolder by initialFolderState
      val expandMusicPlayerInitial by expandMusicPlayerState

      LaunchedEffect(standaloneTool, initialFolder) {
        syncStandaloneTaskDescription(
          activity = this@MainActivity,
          standaloneTool = standaloneTool,
          initialFolder = initialFolder,
        )
      }

      ToolboxTheme(
        themeMode = themeMode,
        themeColor = themeColor,
        accentColor = accentColor,
        musicPlayerThemeEnabled = musicPlayerThemeEnabled,
        musicPlayerColorOption = musicPlayerColorOption,
      ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          MainNavigation(
            standaloneTool = standaloneTool,
            initialDirectory = initialFolder?.let { File(it) },
            initialExpandMusicPlayer = expandMusicPlayerInitial,
          )
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    val parsedFolder = parseFolderShortcut(intent)
    val parsedTool = parseStandaloneTool(intent)
    val expandMusicPlayer = parseExpandMusicPlayer(intent)
    val notesQuickCaptureMode = parseNotesQuickCaptureMode(intent)
    val todoQuickActionMode = parseTodoQuickActionMode(intent)
    val hasNotesNavigationExtras = parseNotesFilterFromIntent(intent) != null ||
      !intent.getStringExtra(EXTRA_NOTES_OPEN_NOTE_ID).isNullOrBlank()
    val incomingTool = when {
      parsedFolder != null -> parsedTool ?: Tool.FILES
      expandMusicPlayer -> parsedTool ?: Tool.MUSIC
      notesQuickCaptureMode != null || hasNotesNavigationExtras -> parsedTool ?: Tool.NOTES
      todoQuickActionMode != null -> parsedTool ?: Tool.TODO_LIST
      else -> parsedTool
    }
    if ((incomingTool != null || parsedFolder != null) &&
      !isSameStandaloneTarget(
        firstTool = standaloneToolState.value,
        firstFolder = initialFolderState.value,
        secondTool = incomingTool,
        secondFolder = parsedFolder,
      )
    ) {
      applyIncomingToolActions(
        intent = intent,
        expandMusicPlayer = expandMusicPlayer,
        notesQuickCaptureMode = notesQuickCaptureMode,
        todoQuickActionMode = todoQuickActionMode,
      )
      val existingTask = findExistingStandaloneAppTask(
        context = this,
        targetTool = incomingTool,
        targetFolder = parsedFolder,
        excludeTaskId = taskId,
      )
      if (existingTask != null) {
        try {
          existingTask.moveToFront()
          return
        } catch (_: Exception) {
          // Fall through to startActivity below
        }
      }
      try {
        val launchIntent = if (parsedFolder != null) {
          createFolderShortcutIntent(this, File(parsedFolder))
        } else {
          createStandaloneToolIntent(this, incomingTool!!).apply {
            if (expandMusicPlayer) {
              putExtra(EXTRA_EXPAND_MUSIC_PLAYER, true)
            }
            if (notesQuickCaptureMode != null) {
              putExtra(EXTRA_NOTES_QUICK_CAPTURE_MODE, notesQuickCaptureMode)
            }
            intent.getStringExtra(EXTRA_NOTES_INITIAL_FILTER_MODE)?.let {
              putExtra(EXTRA_NOTES_INITIAL_FILTER_MODE, it)
            }
            intent.getStringExtra(EXTRA_NOTES_INITIAL_FILTER_LABEL)?.let {
              putExtra(EXTRA_NOTES_INITIAL_FILTER_LABEL, it)
            }
            intent.getStringExtra(EXTRA_NOTES_OPEN_NOTE_ID)?.let {
              putExtra(EXTRA_NOTES_OPEN_NOTE_ID, it)
            }
            if (todoQuickActionMode != null) {
              putExtra(EXTRA_TODO_QUICK_ACTION_MODE, todoQuickActionMode)
            }
          }
        }
        startActivity(launchIntent)
        return
      } catch (_: Exception) {
        // Fall through if starting separate task fails
      }
    }
    setIntent(intent)
    if (parsedFolder != null) {
      initialFolderState.value = parsedFolder
      standaloneToolState.value = parsedTool ?: Tool.FILES
    } else if (expandMusicPlayer) {
      initialFolderState.value = null
      standaloneToolState.value = parsedTool ?: Tool.MUSIC
      expandMusicPlayerState.value = true
    } else if (notesQuickCaptureMode != null || hasNotesNavigationExtras) {
      initialFolderState.value = null
      standaloneToolState.value = parsedTool ?: Tool.NOTES
    } else if (todoQuickActionMode != null) {
      initialFolderState.value = null
      standaloneToolState.value = parsedTool ?: Tool.TODO_LIST
    } else if (parsedTool != null) {
      initialFolderState.value = null
      standaloneToolState.value = parsedTool
    }
    applyIncomingToolActions(
      intent = intent,
      expandMusicPlayer = expandMusicPlayer,
      notesQuickCaptureMode = notesQuickCaptureMode,
      todoQuickActionMode = todoQuickActionMode,
    )
    syncStandaloneTaskDescription(
      activity = this,
      standaloneTool = standaloneToolState.value,
      initialFolder = initialFolderState.value,
    )
  }

  override fun onResume() {
    super.onResume()
    MusicPlayerEngine.syncExternalSystemMediaSessions(this)
  }

  override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    if (hasFocus) {
      MusicPlayerEngine.syncExternalSystemMediaSessions(this)
    }
  }
}
