package com.example.toolbox.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Yard
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Categories for grouping tools in the Toolbox navigation panel.
 */
enum class ToolCategory(
  val title: String,
) {
  MATH_AND_MEASUREMENT(title = "Math & Measurement"),
  PRODUCTIVITY(title = "Productivity"),
  FILES_AND_STORAGE(title = "Files"),
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
    title = "Calculate",
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
    icon = Icons.Default.Camera,
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
  AUDIO_RECORDER(
    title = "Record",
    icon = Icons.Default.Mic,
    category = ToolCategory.AUDIO_AND_SOUND,
  ),
  MUSIC(
    title = "Music",
    icon = Icons.Default.Headphones,
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
    icon = TuningForkIcon,
    category = ToolCategory.MUSIC_AND_INSTRUMENTS,
  ),
  CHORD_BOOK(
    title = "Chords",
    icon = Icons.Default.LibraryMusic,
    category = ToolCategory.MUSIC_AND_INSTRUMENTS,
  ),
  RECIPES(
    title = "Recipes",
    icon = Icons.Default.RestaurantMenu,
    category = ToolCategory.MISCELLANEOUS,
  ),
  PLANT_SCHEDULE(
    title = "Plants",
    icon = Icons.Default.Yard,
    category = ToolCategory.MISCELLANEOUS,
  ),
  DICE_ROLLER(
    title = "Dice",
    icon = Icons.Default.Casino,
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
    title = "Convert",
    icon = Icons.Default.SwapHoriz,
    category = ToolCategory.FILES_AND_STORAGE,
  ),
  PDF_VIEWER(
    title = "PDF Viewer",
    icon = Icons.Default.PictureAsPdf,
    category = ToolCategory.FILES_AND_STORAGE,
  ),
  ARCHIVE(
    title = "Archives",
    icon = Icons.Default.FolderZip,
    category = ToolCategory.FILES_AND_STORAGE,
  ),
  DECIBEL_METER(
    title = "Decibels",
    icon = Icons.Default.Speed,
    category = ToolCategory.AUDIO_AND_SOUND,
  ),
  METRONOME(
    title = "Metronome",
    icon = MetronomeIcon,
    category = ToolCategory.MUSIC_AND_INSTRUMENTS,
  ),
  COLOR_PICKER(
    title = "Colors",
    icon = Icons.Default.Palette,
    category = ToolCategory.MISCELLANEOUS,
  ),
  OBD2_SCANNER(
    title = "OBD2",
    icon = Icons.Default.DirectionsCar,
    category = ToolCategory.REMOTES_AND_DEVICES,
  ),
  SETTINGS(
    title = "Settings",
    icon = Icons.Default.Settings,
    category = ToolCategory.SYSTEM,
  ),
  ;

  companion object {
    val ARCHIVES: Tool get() = ARCHIVE
    val MUSIC_PLAYER: Tool get() = MUSIC
    val RECIPE_BOOK: Tool get() = RECIPES
    val OBD2: Tool get() = OBD2_SCANNER
  }
}

/**
 * Custom vector icon representing a musical tuning fork (two parallel prongs, U-yoke, stem, and ball base).
 */
val TuningForkIcon: ImageVector by lazy {
  ImageVector.Builder(
    name = "TuningFork",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
  ).apply {
    path(fill = SolidColor(Color.Black)) {
      moveTo(7f, 3f)
      curveTo(7f, 2.45f, 7.45f, 2f, 8f, 2f)
      reflectiveCurveTo(9f, 2.45f, 9f, 3f)
      verticalLineTo(9.5f)
      curveTo(9f, 11.16f, 10.34f, 12.5f, 12f, 12.5f)
      reflectiveCurveTo(15f, 11.16f, 15f, 9.5f)
      verticalLineTo(3f)
      curveTo(15f, 2.45f, 15.45f, 2f, 16f, 2f)
      reflectiveCurveTo(17f, 2.45f, 17f, 3f)
      verticalLineTo(9.5f)
      curveTo(17f, 12.01f, 15.14f, 14.09f, 12.73f, 14.44f)
      verticalLineTo(18.65f)
      curveTo(13.61f, 18.95f, 14.25f, 19.78f, 14.25f, 20.75f)
      curveTo(14.25f, 21.99f, 13.24f, 23f, 12f, 23f)
      reflectiveCurveTo(9.75f, 21.99f, 9.75f, 20.75f)
      curveTo(9.75f, 19.78f, 10.39f, 18.95f, 11.27f, 18.65f)
      verticalLineTo(14.44f)
      curveTo(8.86f, 14.09f, 7f, 12.01f, 7f, 9.5f)
      close()
    }
  }.build()
}

/**
 * Custom vector icon representing a classic mechanical metronome (A-frame body with angled pendulum rod and weight).
 */
val MetronomeIcon: ImageVector by lazy {
  ImageVector.Builder(
    name = "Metronome",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
  ).apply {
    // A-frame body with inner window cutout
    path(
      fill = SolidColor(Color.Black),
      pathFillType = PathFillType.EvenOdd,
    ) {
      moveTo(8.85f, 3f)
      curveTo(8.02f, 3f, 7.31f, 3.58f, 7.13f, 4.39f)
      lineTo(4.04f, 18.39f)
      curveTo(3.79f, 19.52f, 4.65f, 20.6f, 5.81f, 20.6f)
      horizontalLineTo(6.5f)
      verticalLineTo(22f)
      horizontalLineTo(8.5f)
      verticalLineTo(20.6f)
      horizontalLineTo(15.5f)
      verticalLineTo(22f)
      horizontalLineTo(17.5f)
      verticalLineTo(20.6f)
      horizontalLineTo(18.19f)
      curveTo(19.35f, 20.6f, 20.21f, 19.52f, 19.96f, 18.39f)
      lineTo(16.87f, 4.39f)
      curveTo(16.69f, 3.58f, 15.98f, 3f, 15.15f, 3f)
      close()
      // Inner recessed window
      moveTo(9.28f, 5f)
      horizontalLineTo(14.72f)
      lineTo(17.15f, 16f)
      horizontalLineTo(6.85f)
      close()
      // Lower horizontal trim slot
      moveTo(6.4f, 17.5f)
      horizontalLineTo(17.6f)
      verticalLineTo(18.8f)
      horizontalLineTo(6.4f)
      close()
    }
    // Angled pendulum arm and sliding counterweight
    path(fill = SolidColor(Color.Black)) {
      moveTo(11.15f, 16.2f)
      lineTo(17.95f, 4.4f)
      lineTo(19.68f, 5.4f)
      lineTo(17.68f, 8.86f)
      lineTo(18.55f, 9.36f)
      lineTo(17.3f, 11.53f)
      lineTo(16.43f, 11.03f)
      lineTo(13.45f, 16.2f)
      close()
    }
  }.build()
}
