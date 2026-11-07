package com.example.toolbox.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents each tool available in the Toolbox pull-up navigation panel.
 */
enum class Tool(
  val title: String,
  val icon: ImageVector,
) {
  DICE_ROLLER(
    title = "Dice Roller",
    icon = Icons.Default.Casino,
  ),
  UNIT_CONVERTER(
    title = "Unit Converter",
    icon = Icons.Default.Straighten,
  ),
  GUITAR_TUNER(
    title = "Guitar Tuner",
    icon = Icons.Default.MusicNote,
  ),
  METRONOME(
    title = "Metronome",
    icon = Icons.Default.Timer,
  ),
  COLOR_PICKER(
    title = "Color Picker",
    icon = Icons.Default.Palette,
  ),
  RULER(
    title = "Ruler",
    icon = Icons.Default.SquareFoot,
  ),
}
