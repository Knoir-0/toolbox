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
import androidx.compose.material.icons.filled.SportsEsports
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
  PRODUCTIVITY(title = "Productivity"),
  FILES_AND_STORAGE(title = "Files & Storage"),
  PHOTO_AND_VIDEO(title = "Photo & Video"),
  AUDIO_AND_SOUND(title = "Audio & Sound"),
  MUSIC_AND_INSTRUMENTS(title = "Music & Instruments"),
  REMOTES_AND_DEVICES(title = "Remotes & Devices"),
  MISCELLANEOUS(title = "Miscellaneous"),
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
    title = "To Do",
    icon = Icons.Default.Checklist,
    category = ToolCategory.PRODUCTIVITY,
  ),
  CAMERA(
    title = "Camera",
    icon = Icons.Default.PhotoCamera,
    category = ToolCategory.PHOTO_AND_VIDEO,
  ),
  GALLERY(
    title = "Gallery",
    icon = Icons.Default.PhotoLibrary,
    category = ToolCategory.PHOTO_AND_VIDEO,
  ),
  FILES(
    title = "Files",
    icon = Icons.Default.Folder,
    category = ToolCategory.FILES_AND_STORAGE,
  ),
  UNIT_CONVERTER(
    title = "Units",
    icon = Icons.Default.Straighten,
    category = ToolCategory.MATH_AND_MEASUREMENT,
  ),
  RULER(
    title = "Ruler",
    icon = Icons.Default.SquareFoot,
    category = ToolCategory.MATH_AND_MEASUREMENT,
  ),
  COLOR_PICKER(
    title = "Colors",
    icon = Icons.Default.Palette,
    category = ToolCategory.MISCELLANEOUS,
  ),
  AUDIO_RECORDER(
    title = "Recorder",
    icon = Icons.Default.Mic,
    category = ToolCategory.AUDIO_AND_SOUND,
  ),
  BLUETOOTH_REMOTE(
    title = "Remote",
    icon = Icons.Default.SettingsRemote,
    category = ToolCategory.REMOTES_AND_DEVICES,
  ),
  BLUETOOTH_GAMEPAD(
    title = "Gamepad",
    icon = Icons.Default.SportsEsports,
    category = ToolCategory.REMOTES_AND_DEVICES,
  ),
  GUITAR_TUNER(
    title = "Tuner",
    icon = Icons.Default.MusicNote,
    category = ToolCategory.MUSIC_AND_INSTRUMENTS,
  ),
  METRONOME(
    title = "Metronome",
    icon = Icons.Default.Timer,
    category = ToolCategory.MUSIC_AND_INSTRUMENTS,
  ),
  CHORD_BOOK(
    title = "Chords",
    icon = Icons.Default.LibraryMusic,
    category = ToolCategory.MUSIC_AND_INSTRUMENTS,
  ),
  DICE_ROLLER(
    title = "Dice",
    icon = Icons.Default.Casino,
    category = ToolCategory.MISCELLANEOUS,
  ),
  PLANT_SCHEDULE(
    title = "Plants",
    icon = Icons.Default.Yard,
    category = ToolCategory.MISCELLANEOUS,
  ),
  IMAGE_EDITOR(
    title = "Image Editor",
    icon = Icons.Default.Image,
    category = ToolCategory.PHOTO_AND_VIDEO,
  ),
  VIDEO_EDITOR(
    title = "Video Editor",
    icon = Icons.Default.Movie,
    category = ToolCategory.PHOTO_AND_VIDEO,
  ),
  AUDIO_EDITOR(
    title = "Audio Editor",
    icon = Icons.Default.GraphicEq,
    category = ToolCategory.AUDIO_AND_SOUND,
  ),
  FILE_CONVERTER(
    title = "File Converter",
    icon = Icons.Default.SwapHoriz,
    category = ToolCategory.FILES_AND_STORAGE,
  ),
  SETTINGS(
    title = "Settings",
    icon = Icons.Default.Settings,
    category = ToolCategory.SYSTEM,
  ),
}
