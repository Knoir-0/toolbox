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
import com.example.toolbox.ui.main.AUDIO_RECORDER_QUICK_ACTION_START_BOTH
import com.example.toolbox.ui.main.AUDIO_RECORDER_QUICK_ACTION_START_DEVICE
import com.example.toolbox.ui.main.AUDIO_RECORDER_QUICK_ACTION_START_MIC
import com.example.toolbox.ui.main.SCREEN_RECORDER_QUICK_ACTION_START_BOTH
import com.example.toolbox.ui.main.SCREEN_RECORDER_QUICK_ACTION_START_DEVICE
import com.example.toolbox.ui.main.SCREEN_RECORDER_QUICK_ACTION_START_MIC
import com.example.toolbox.ui.main.EXTRA_AUDIO_RECORDER_QUICK_ACTION
import com.example.toolbox.ui.main.EXTRA_CALENDAR_DATE_KEY
import com.example.toolbox.ui.main.EXTRA_CALENDAR_OPEN_ADD_EVENT
import com.example.toolbox.ui.main.EXTRA_CAMERA_CAPTURE_MODE
import com.example.toolbox.ui.main.EXTRA_ELEMENTS_ATOMIC_NUMBER
import com.example.toolbox.ui.main.EXTRA_ELEMENTS_OPEN_INSPECTOR
import com.example.toolbox.ui.main.EXTRA_ELEMENTS_OPEN_MOLAR_MASS
import com.example.toolbox.ui.main.EXTRA_ELEMENTS_OPEN_SEARCH
import com.example.toolbox.ui.main.EXTRA_EXPAND_MUSIC_PLAYER
import com.example.toolbox.ui.main.EXTRA_NOTES_INITIAL_FILTER_LABEL
import com.example.toolbox.ui.main.EXTRA_NOTES_INITIAL_FILTER_MODE
import com.example.toolbox.ui.main.EXTRA_NOTES_OPEN_NOTE_ID
import com.example.toolbox.ui.main.EXTRA_NOTES_QUICK_CAPTURE_MODE
import com.example.toolbox.ui.main.EXTRA_PLANT_SCHEDULE_MONTH
import com.example.toolbox.ui.main.EXTRA_PLANT_SCHEDULE_OPEN_SEARCH
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
import com.example.toolbox.ui.main.parseAudioRecorderQuickAction
import com.example.toolbox.ui.main.parseCalendarDateKey
import com.example.toolbox.ui.main.parseCalendarOpenAddEvent
import com.example.toolbox.ui.main.parseCalendarViewModeName
import com.example.toolbox.ui.main.parseCameraCaptureModeName
import com.example.toolbox.ui.main.parseElementsAtomicNumber
import com.example.toolbox.ui.main.parseElementsOpenInspector
import com.example.toolbox.ui.main.parseElementsOpenMolarMass
import com.example.toolbox.ui.main.parseElementsOpenSearch
import com.example.toolbox.ui.main.parseExpandMusicPlayer
import com.example.toolbox.ui.main.parseFolderShortcut
import com.example.toolbox.ui.main.parseNotesQuickCaptureMode
import com.example.toolbox.ui.main.parsePlantScheduleMonth
import com.example.toolbox.ui.main.parsePlantScheduleOpenSearch
import com.example.toolbox.ui.main.parseScreenRecorderQuickAction
import com.example.toolbox.ui.main.parseStandaloneTool
import com.example.toolbox.ui.main.parseTodoQuickActionMode
import com.example.toolbox.ui.main.rememberNavigationPreferencesRepository
import com.example.toolbox.ui.main.syncPinnedHomeScreenShortcuts
import com.example.toolbox.ui.main.syncStandaloneTaskDescription
import com.example.toolbox.ui.theme.ToolboxTheme
import com.example.toolbox.ui.tools.AudioCaptureSource
import com.example.toolbox.ui.tools.AudioRecorderEngine
import com.example.toolbox.ui.tools.CalendarDay
import com.example.toolbox.ui.tools.CalendarEngine
import com.example.toolbox.ui.tools.CalendarViewMode
import com.example.toolbox.ui.tools.CameraCaptureMode
import com.example.toolbox.ui.tools.CameraEngine
import com.example.toolbox.ui.tools.CameraWidgets
import com.example.toolbox.ui.tools.MusicPlayerEngine
import com.example.toolbox.ui.tools.NotesEngine
import com.example.toolbox.ui.tools.NotesFilter
import com.example.toolbox.ui.tools.PeriodicTableEngine
import com.example.toolbox.ui.tools.PlantScheduleEngine
import com.example.toolbox.ui.tools.ScreenAudioSource
import com.example.toolbox.ui.tools.ScreenRecorderEngine
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
    audioRecorderQuickAction: String? = parseAudioRecorderQuickAction(intent),
    screenRecorderQuickAction: String? = parseScreenRecorderQuickAction(intent),
    plantScheduleMonth: Int? = parsePlantScheduleMonth(intent),
    plantScheduleOpenSearch: Boolean = parsePlantScheduleOpenSearch(intent),
    cameraCaptureModeName: String? = parseCameraCaptureModeName(intent),
    calendarDateKey: String? = parseCalendarDateKey(intent),
    calendarOpenAddEvent: Boolean = parseCalendarOpenAddEvent(intent),
    calendarViewModeName: String? = parseCalendarViewModeName(intent),
    elementsAtomicNumber: Int? = parseElementsAtomicNumber(intent),
    elementsOpenInspector: Boolean = parseElementsOpenInspector(intent),
    elementsOpenSearch: Boolean = parseElementsOpenSearch(intent),
    elementsOpenMolarMass: Boolean = parseElementsOpenMolarMass(intent),
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
    if (audioRecorderQuickAction != null) {
      val source = when (audioRecorderQuickAction) {
        AUDIO_RECORDER_QUICK_ACTION_START_DEVICE -> AudioCaptureSource.DEVICE_AUDIO
        AUDIO_RECORDER_QUICK_ACTION_START_BOTH -> AudioCaptureSource.DEVICE_AND_MIC
        else -> AudioCaptureSource.MICROPHONE
      }
      AudioRecorderEngine.requestQuickRecord(startRecording = true, source = source)
    }
    if (screenRecorderQuickAction != null) {
      val source = when (screenRecorderQuickAction) {
        SCREEN_RECORDER_QUICK_ACTION_START_DEVICE -> ScreenAudioSource.DEVICE_AUDIO
        SCREEN_RECORDER_QUICK_ACTION_START_BOTH -> ScreenAudioSource.DEVICE_AND_MIC
        else -> ScreenAudioSource.MICROPHONE
      }
      ScreenRecorderEngine.requestQuickRecord(startRecording = true, source = source)
    }
    if (plantScheduleMonth != null || plantScheduleOpenSearch) {
      PlantScheduleEngine.requestWidgetNavigation(
        monthNumber = plantScheduleMonth,
        openCatalogSearch = plantScheduleOpenSearch,
      )
    }
    if (cameraCaptureModeName != null) {
      val mode = CameraCaptureMode.fromNameOrDefault(cameraCaptureModeName)
      CameraWidgets.saveActiveCaptureMode(this, mode)
      CameraEngine.requestCaptureMode(mode)
    }
    if (calendarDateKey != null || calendarOpenAddEvent || calendarViewModeName != null) {
      CalendarEngine.requestNavigation(
        targetDay = CalendarDay.fromDateKeyOrNull(calendarDateKey),
        openAddEvent = calendarOpenAddEvent,
        viewMode = calendarViewModeName?.let { CalendarViewMode.fromNameOrDefault(it) },
      )
    }
    if (elementsAtomicNumber != null || elementsOpenInspector || elementsOpenSearch || elementsOpenMolarMass) {
      PeriodicTableEngine.requestWidgetNavigation(
        atomicNumber = elementsAtomicNumber,
        openInspector = elementsOpenInspector,
        openSearch = elementsOpenSearch,
        openMolarMass = elementsOpenMolarMass,
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
    val audioRecorderQuickAction = parseAudioRecorderQuickAction(intent)
    val screenRecorderQuickAction = parseScreenRecorderQuickAction(intent)
    val plantScheduleMonth = parsePlantScheduleMonth(intent)
    val plantScheduleOpenSearch = parsePlantScheduleOpenSearch(intent)
    val cameraCaptureModeName = parseCameraCaptureModeName(intent)
    val calendarDateKey = parseCalendarDateKey(intent)
    val calendarOpenAddEvent = parseCalendarOpenAddEvent(intent)
    val calendarViewModeName = parseCalendarViewModeName(intent)
    val elementsAtomicNumber = parseElementsAtomicNumber(intent)
    val elementsOpenInspector = parseElementsOpenInspector(intent)
    val elementsOpenSearch = parseElementsOpenSearch(intent)
    val elementsOpenMolarMass = parseElementsOpenMolarMass(intent)
    val hasElementsNavigationExtras = elementsAtomicNumber != null ||
      elementsOpenInspector ||
      elementsOpenSearch ||
      elementsOpenMolarMass
    val hasNotesNavigationExtras = parseNotesFilterFromIntent(intent) != null ||
      !intent?.getStringExtra(EXTRA_NOTES_OPEN_NOTE_ID).isNullOrBlank()
    val resolvedTool = when {
      parsedFolder != null -> parsedTool ?: Tool.FILES
      expandMusicPlayer -> parsedTool ?: Tool.MUSIC
      notesQuickCaptureMode != null || hasNotesNavigationExtras -> parsedTool ?: Tool.NOTES
      todoQuickActionMode != null -> parsedTool ?: Tool.TODO_LIST
      audioRecorderQuickAction != null -> parsedTool ?: Tool.AUDIO_RECORDER
      screenRecorderQuickAction != null -> parsedTool ?: Tool.SCREEN_RECORDER
      plantScheduleMonth != null || plantScheduleOpenSearch -> parsedTool ?: Tool.PLANT_SCHEDULE
      cameraCaptureModeName != null -> parsedTool ?: Tool.CAMERA
      calendarDateKey != null || calendarOpenAddEvent || calendarViewModeName != null -> parsedTool ?: Tool.CALENDAR
      hasElementsNavigationExtras -> parsedTool ?: Tool.ELEMENTS
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
      val backgroundTexture by preferencesRepository.backgroundTextureFlow.collectAsState()
      val hexagonIconButtons by preferencesRepository.hexagonIconButtonsFlow.collectAsState()
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
        backgroundTexture = backgroundTexture,
        hexagonIconButtons = hexagonIconButtons,
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
    val audioRecorderQuickAction = parseAudioRecorderQuickAction(intent)
    val screenRecorderQuickAction = parseScreenRecorderQuickAction(intent)
    val plantScheduleMonth = parsePlantScheduleMonth(intent)
    val plantScheduleOpenSearch = parsePlantScheduleOpenSearch(intent)
    val cameraCaptureModeName = parseCameraCaptureModeName(intent)
    val calendarDateKey = parseCalendarDateKey(intent)
    val calendarOpenAddEvent = parseCalendarOpenAddEvent(intent)
    val calendarViewModeName = parseCalendarViewModeName(intent)
    val elementsAtomicNumber = parseElementsAtomicNumber(intent)
    val elementsOpenInspector = parseElementsOpenInspector(intent)
    val elementsOpenSearch = parseElementsOpenSearch(intent)
    val elementsOpenMolarMass = parseElementsOpenMolarMass(intent)
    val hasElementsNavigationExtras = elementsAtomicNumber != null ||
      elementsOpenInspector ||
      elementsOpenSearch ||
      elementsOpenMolarMass
    val hasNotesNavigationExtras = parseNotesFilterFromIntent(intent) != null ||
      !intent.getStringExtra(EXTRA_NOTES_OPEN_NOTE_ID).isNullOrBlank()
    val incomingTool = when {
      parsedFolder != null -> parsedTool ?: Tool.FILES
      expandMusicPlayer -> parsedTool ?: Tool.MUSIC
      notesQuickCaptureMode != null || hasNotesNavigationExtras -> parsedTool ?: Tool.NOTES
      todoQuickActionMode != null -> parsedTool ?: Tool.TODO_LIST
      audioRecorderQuickAction != null -> parsedTool ?: Tool.AUDIO_RECORDER
      screenRecorderQuickAction != null -> parsedTool ?: Tool.SCREEN_RECORDER
      plantScheduleMonth != null || plantScheduleOpenSearch -> parsedTool ?: Tool.PLANT_SCHEDULE
      cameraCaptureModeName != null -> parsedTool ?: Tool.CAMERA
      calendarDateKey != null || calendarOpenAddEvent || calendarViewModeName != null -> parsedTool ?: Tool.CALENDAR
      hasElementsNavigationExtras -> parsedTool ?: Tool.ELEMENTS
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
            if (audioRecorderQuickAction != null) {
              putExtra(EXTRA_AUDIO_RECORDER_QUICK_ACTION, audioRecorderQuickAction)
            }
            if (plantScheduleMonth != null) {
              putExtra(EXTRA_PLANT_SCHEDULE_MONTH, plantScheduleMonth)
            }
            if (plantScheduleOpenSearch) {
              putExtra(EXTRA_PLANT_SCHEDULE_OPEN_SEARCH, true)
            }
            if (cameraCaptureModeName != null) {
              putExtra(EXTRA_CAMERA_CAPTURE_MODE, cameraCaptureModeName)
            }
            if (calendarDateKey != null) {
              putExtra(EXTRA_CALENDAR_DATE_KEY, calendarDateKey)
            }
            if (calendarOpenAddEvent) {
              putExtra(EXTRA_CALENDAR_OPEN_ADD_EVENT, true)
            }
            if (elementsAtomicNumber != null) {
              putExtra(EXTRA_ELEMENTS_ATOMIC_NUMBER, elementsAtomicNumber)
            }
            if (elementsOpenInspector) {
              putExtra(EXTRA_ELEMENTS_OPEN_INSPECTOR, true)
            }
            if (elementsOpenSearch) {
              putExtra(EXTRA_ELEMENTS_OPEN_SEARCH, true)
            }
            if (elementsOpenMolarMass) {
              putExtra(EXTRA_ELEMENTS_OPEN_MOLAR_MASS, true)
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
    } else if (audioRecorderQuickAction != null) {
      initialFolderState.value = null
      standaloneToolState.value = parsedTool ?: Tool.AUDIO_RECORDER
    } else if (plantScheduleMonth != null || plantScheduleOpenSearch) {
      initialFolderState.value = null
      standaloneToolState.value = parsedTool ?: Tool.PLANT_SCHEDULE
    } else if (cameraCaptureModeName != null) {
      initialFolderState.value = null
      standaloneToolState.value = parsedTool ?: Tool.CAMERA
    } else if (calendarDateKey != null || calendarOpenAddEvent) {
      initialFolderState.value = null
      standaloneToolState.value = parsedTool ?: Tool.CALENDAR
    } else if (hasElementsNavigationExtras) {
      initialFolderState.value = null
      standaloneToolState.value = parsedTool ?: Tool.ELEMENTS
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
