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
import com.example.toolbox.ui.main.parseFolderShortcut
import com.example.toolbox.ui.main.parseStandaloneTool
import com.example.toolbox.ui.main.rememberNavigationPreferencesRepository
import com.example.toolbox.ui.main.syncAppLauncherIcon
import com.example.toolbox.ui.main.syncStandaloneTaskDescription
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File

class MainActivity : ComponentActivity() {
  private val standaloneToolState = mutableStateOf<Tool?>(null)
  private val initialFolderState = mutableStateOf<String?>(null)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val parsedTool = parseStandaloneTool(intent)
    val parsedFolder = parseFolderShortcut(intent)
    val resolvedTool = if (parsedFolder != null) (parsedTool ?: Tool.FILES) else parsedTool
    standaloneToolState.value = resolvedTool
    initialFolderState.value = parsedFolder
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
      val accentColor by preferencesRepository.accentColorFlow.collectAsState()
      val standaloneTool by standaloneToolState
      val initialFolder by initialFolderState

      LaunchedEffect(themeColor) {
        syncAppLauncherIcon(this@MainActivity, themeColor)
      }

      LaunchedEffect(standaloneTool, initialFolder, themeColor) {
        syncStandaloneTaskDescription(
          activity = this@MainActivity,
          standaloneTool = standaloneTool,
          initialFolder = initialFolder,
          themeColor = themeColor,
        )
      }

      ToolboxTheme(
        themeMode = themeMode,
        themeColor = themeColor,
        accentColor = accentColor,
      ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          MainNavigation(
            standaloneTool = standaloneTool,
            initialDirectory = initialFolder?.let { File(it) },
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
    if (parsedFolder != null) {
      initialFolderState.value = parsedFolder
      standaloneToolState.value = parsedTool ?: Tool.FILES
    } else if (parsedTool != null) {
      standaloneToolState.value = parsedTool
    }
    syncStandaloneTaskDescription(
      activity = this,
      standaloneTool = standaloneToolState.value,
      initialFolder = initialFolderState.value,
    )
  }
}
