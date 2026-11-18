package com.example.toolbox

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.parseExpandMusicPlayer
import com.example.toolbox.ui.main.parseFolderShortcut
import com.example.toolbox.ui.main.parseStandaloneTool
import com.example.toolbox.ui.main.rememberNavigationPreferencesRepository
import com.example.toolbox.ui.main.syncStandaloneTaskDescription
import com.example.toolbox.ui.theme.ToolboxTheme
import com.example.toolbox.ui.tools.MusicPlayerEngine
import java.io.File

class MainActivity : ComponentActivity() {
  private val standaloneToolState = mutableStateOf<Tool?>(null)
  private val initialFolderState = mutableStateOf<String?>(null)
  private val expandMusicPlayerState = mutableStateOf(false)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    MusicPlayerEngine.ensureSystemMediaMonitoring(this)
    MusicPlayerEngine.syncExternalSystemMediaSessions(this)
    val parsedTool = parseStandaloneTool(intent)
    val parsedFolder = parseFolderShortcut(intent)
    val expandMusicPlayer = parseExpandMusicPlayer(intent)
    val resolvedTool = when {
      parsedFolder != null -> parsedTool ?: Tool.FILES
      expandMusicPlayer -> parsedTool ?: Tool.MUSIC
      else -> parsedTool
    }
    standaloneToolState.value = resolvedTool
    initialFolderState.value = parsedFolder
    expandMusicPlayerState.value = expandMusicPlayer
    if (expandMusicPlayer) {
      MusicPlayerEngine.requestExpandFullPlayer()
    }
    syncStandaloneTaskDescription(
      activity = this,
      standaloneTool = resolvedTool,
      initialFolder = parsedFolder,
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
    setIntent(intent)
    val parsedFolder = parseFolderShortcut(intent)
    val parsedTool = parseStandaloneTool(intent)
    val expandMusicPlayer = parseExpandMusicPlayer(intent)
    if (parsedFolder != null) {
      initialFolderState.value = parsedFolder
      standaloneToolState.value = parsedTool ?: Tool.FILES
    } else if (expandMusicPlayer) {
      standaloneToolState.value = parsedTool ?: Tool.MUSIC
      expandMusicPlayerState.value = true
      MusicPlayerEngine.requestExpandFullPlayer()
    } else if (parsedTool != null) {
      standaloneToolState.value = parsedTool
    }
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
