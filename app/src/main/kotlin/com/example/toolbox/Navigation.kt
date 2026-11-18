package com.example.toolbox

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.toolbox.ui.main.MainScreen
import com.example.toolbox.ui.main.Tool
import java.io.File

@Composable
fun MainNavigation(
  standaloneTool: Tool? = null,
  initialDirectory: File? = null,
  initialExpandMusicPlayer: Boolean = false,
) {
  val backStack = rememberNavBackStack(Main)
  val currentStandaloneTool by rememberUpdatedState(standaloneTool)
  val currentInitialDirectory by rememberUpdatedState(initialDirectory)
  val currentInitialExpandMusicPlayer by rememberUpdatedState(initialExpandMusicPlayer)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Main> {
          MainScreen(
            standaloneTool = currentStandaloneTool,
            initialDirectory = currentInitialDirectory,
            initialExpandMusicPlayer = currentInitialExpandMusicPlayer,
            onItemClick = { navKey -> backStack.add(navKey) },
          )
        }
      },
  )
}
