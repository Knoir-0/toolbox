package com.example.toolbox.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Categories for grouping tools in the Toolbox navigation panel.
 */
enum class ToolCategory(
  val title: String,
) {
  MATH_AND_MEASUREMENT(title = "Math & Measurement"),
  AUDIO_AND_MUSIC(title = "Audio & Music"),
  MEDIA_AND_CREATIVE(title = "Media & Creative"),
  PRODUCTIVITY(title = "Productivity"),
  ENTERTAINMENT_AND_DEVICES(title = "Entertainment & Devices"),
  SYSTEM(title = "System"),
}

/**
 * Represents each tool available in the Toolbox pull-up navigation panel.
 */
enum class Tool(
  val title: String,
  val icon: ImageVector,
  val category: ToolCategory,
) {
  DICE_ROLLER(
    title = "Dice Roller",
    icon = Icons.Default.Casino,
    category = ToolCategory.ENTERTAINMENT_AND_DEVICES,
  ),
  UNIT_CONVERTER(
    title = "Unit Converter",
    icon = Icons.Default.Straighten,
    category = ToolCategory.MATH_AND_MEASUREMENT,
  ),
  GUITAR_TUNER(
    title = "Guitar Tuner",
    icon = Icons.Default.MusicNote,
    category = ToolCategory.AUDIO_AND_MUSIC,
  ),
  METRONOME(
    title = "Metronome",
    icon = Icons.Default.Timer,
    category = ToolCategory.AUDIO_AND_MUSIC,
  ),
  COLOR_PICKER(
    title = "Color Picker",
    icon = Icons.Default.Palette,
    category = ToolCategory.MEDIA_AND_CREATIVE,
  ),
  RULER(
    title = "Ruler",
    icon = Icons.Default.SquareFoot,
    category = ToolCategory.MATH_AND_MEASUREMENT,
  ),
  BLUETOOTH_REMOTE(
    title = "TV Remote",
    icon = Icons.Default.SettingsRemote,
    category = ToolCategory.ENTERTAINMENT_AND_DEVICES,
  ),
  CALCULATOR(
    title = "Calculator",
    icon = Icons.Default.Calculate,
    category = ToolCategory.MATH_AND_MEASUREMENT,
  ),
  IMAGE_EDITOR(
    title = "Image Editor",
    icon = Icons.Default.Image,
    category = ToolCategory.MEDIA_AND_CREATIVE,
  ),
  TODO_LIST(
    title = "To-Do",
    icon = Icons.Default.Checklist,
    category = ToolCategory.PRODUCTIVITY,
  ),
  FILES(
    title = "Files",
    icon = Icons.Default.Folder,
    category = ToolCategory.PRODUCTIVITY,
  ),
  SETTINGS(
    title = "Settings",
    icon = Icons.Default.Settings,
    category = ToolCategory.SYSTEM,
  ),
}
