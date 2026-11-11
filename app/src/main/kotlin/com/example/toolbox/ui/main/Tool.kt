package com.example.toolbox.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Yard
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
 * Represents each tool available in the Toolbox pull-up navigation panel,
 * ordered by default from most to least frequently used (with [SETTINGS] always last).
 */
enum class Tool(
  val title: String,
  val icon: ImageVector,
  val category: ToolCategory,
) {
  CALCULATOR(
    title = "Calculator",
    icon = Icons.Default.Calculate,
    category = ToolCategory.MATH_AND_MEASUREMENT,
  ),
  NOTES(
    title = "Notes",
    icon = Icons.AutoMirrored.Filled.StickyNote2,
    category = ToolCategory.PRODUCTIVITY,
  ),
  TODO_LIST(
    title = "To-Do",
    icon = Icons.Default.Checklist,
    category = ToolCategory.PRODUCTIVITY,
  ),
  CAMERA(
    title = "Camera",
    icon = Icons.Default.PhotoCamera,
    category = ToolCategory.MEDIA_AND_CREATIVE,
  ),
  GALLERY(
    title = "Gallery",
    icon = Icons.Default.PhotoLibrary,
    category = ToolCategory.MEDIA_AND_CREATIVE,
  ),
  FILES(
    title = "Files",
    icon = Icons.Default.Folder,
    category = ToolCategory.PRODUCTIVITY,
  ),
  UNIT_CONVERTER(
    title = "Unit Converter",
    icon = Icons.Default.Straighten,
    category = ToolCategory.MATH_AND_MEASUREMENT,
  ),
  AUDIO_RECORDER(
    title = "Audio Recorder",
    icon = Icons.Default.Mic,
    category = ToolCategory.AUDIO_AND_MUSIC,
  ),
  BLUETOOTH_REMOTE(
    title = "TV Remote",
    icon = Icons.Default.SettingsRemote,
    category = ToolCategory.ENTERTAINMENT_AND_DEVICES,
  ),
  IMAGE_EDITOR(
    title = "Image Editor",
    icon = Icons.Default.Image,
    category = ToolCategory.MEDIA_AND_CREATIVE,
  ),
  FILE_CONVERTER(
    title = "File Converter",
    icon = Icons.Default.SwapHoriz,
    category = ToolCategory.MEDIA_AND_CREATIVE,
  ),
  RULER(
    title = "Ruler",
    icon = Icons.Default.SquareFoot,
    category = ToolCategory.MATH_AND_MEASUREMENT,
  ),
  VIDEO_EDITOR(
    title = "Video Editor",
    icon = Icons.Default.Movie,
    category = ToolCategory.MEDIA_AND_CREATIVE,
  ),
  AUDIO_EDITOR(
    title = "Audio Editor",
    icon = Icons.Default.GraphicEq,
    category = ToolCategory.AUDIO_AND_MUSIC,
  ),
  COLOR_PICKER(
    title = "Color Picker",
    icon = Icons.Default.Palette,
    category = ToolCategory.MEDIA_AND_CREATIVE,
  ),
  DICE_ROLLER(
    title = "Dice Roller",
    icon = Icons.Default.Casino,
    category = ToolCategory.ENTERTAINMENT_AND_DEVICES,
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
  CHORD_BOOK(
    title = "Chord Book",
    icon = Icons.Default.LibraryMusic,
    category = ToolCategory.AUDIO_AND_MUSIC,
  ),
  PLANT_SCHEDULE(
    title = "Plant Schedule",
    icon = Icons.Default.Yard,
    category = ToolCategory.PRODUCTIVITY,
  ),
  SETTINGS(
    title = "Settings",
    icon = Icons.Default.Settings,
    category = ToolCategory.SYSTEM,
  ),
}
