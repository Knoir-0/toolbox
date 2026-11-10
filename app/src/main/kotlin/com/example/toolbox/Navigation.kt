package com.example.toolbox

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
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
) {
  val backStack = rememberNavBackStack(Main)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Main> {
          MainScreen(
            standaloneTool = standaloneTool,
            initialDirectory = initialDirectory,
            onItemClick = { navKey -> backStack.add(navKey) },
          )
        }
      },
  )
}
