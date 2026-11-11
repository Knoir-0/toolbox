package com.example.toolbox.ui.main

import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.toPath
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.toolbox.MainActivity
import com.example.toolbox.R
import com.example.toolbox.ui.theme.parseHexColor
import java.io.File

const val EXTRA_STANDALONE_TOOL = "extra_standalone_tool"
const val EXTRA_FOLDER_PATH = "extra_folder_path"

private const val SHORTCUT_ICON_SIZE_PX = 216
private const val SHORTCUT_ADAPTIVE_VIEWPORT_DP = 108f
private const val SHORTCUT_FOREGROUND_ICON_DP = 50f

/**
 * Represents the launcher `<activity-alias>` variants in `AndroidManifest.xml`, each configured
 * with a themed adaptive icon background matching the corresponding theme color.
 */
enum class LauncherIconVariant(
  val aliasClassName: String,
  val hexColor: String?,
  val color: Color,
) {
  SYSTEM(
    aliasClassName = "com.example.toolbox.MainActivityLauncherSystem",
    hexColor = null,
    color = Color(0xFF8E66A6),
  ),
  PURPLE(
    aliasClassName = "com.example.toolbox.MainActivityLauncherPurple",
    hexColor = FALLBACK_THEME_COLOR_HEX,
    color = Color(0xFF8E66A6),
  ),
  OCEAN_BLUE(
    aliasClassName = "com.example.toolbox.MainActivityLauncherOceanBlue",
    hexColor = "#00668B",
    color = Color(0xFF00668B),
  ),
  FOREST_GREEN(
    aliasClassName = "com.example.toolbox.MainActivityLauncherForestGreen",
    hexColor = "#386A20",
    color = Color(0xFF386A20),
  ),
  AMBER_ORANGE(
    aliasClassName = "com.example.toolbox.MainActivityLauncherAmberOrange",
    hexColor = "#E65100",
    color = Color(0xFFE65100),
  ),
  CRIMSON_RED(
    aliasClassName = "com.example.toolbox.MainActivityLauncherCrimsonRed",
    hexColor = "#B3261E",
    color = Color(0xFFB3261E),
  ),
  TEAL(
    aliasClassName = "com.example.toolbox.MainActivityLauncherTeal",
    hexColor = "#006A60",
    color = Color(0xFF006A60),
  ),
  ROSE_PINK(
    aliasClassName = "com.example.toolbox.MainActivityLauncherRosePink",
    hexColor = "#984061",
    color = Color(0xFF984061),
  ),
  INDIGO(
    aliasClassName = "com.example.toolbox.MainActivityLauncherIndigo",
    hexColor = "#3F51B5",
    color = Color(0xFF3F51B5),
  ),
}

/**
 * Resolves the [LauncherIconVariant] whose background matches (or is closest to) [themeColor].
 */
fun resolveLauncherIconVariant(themeColor: String?): LauncherIconVariant {
  if (themeColor.isNullOrBlank() || themeColor.equals(DEFAULT_THEME_COLOR_SYSTEM, ignoreCase = true)) {
    return LauncherIconVariant.SYSTEM
  }
  val exactMatch = LauncherIconVariant.entries.find {
    it.hexColor != null && it.hexColor.equals(themeColor, ignoreCase = true)
  }
  if (exactMatch != null) return exactMatch

  val parsedColor = parseHexColor(themeColor) ?: return LauncherIconVariant.SYSTEM
  return LauncherIconVariant.entries
    .filter { it != LauncherIconVariant.SYSTEM }
    .minByOrNull { variant ->
      val dr = parsedColor.red - variant.color.red
      val dg = parsedColor.green - variant.color.green
      val db = parsedColor.blue - variant.color.blue
      dr * dr + dg * dg + db * db
    } ?: LauncherIconVariant.PURPLE
}

/**
 * Synchronizes the enabled launcher `<activity-alias>` so the application icon background on the
 * Home screen matches the active [themeColor].
 */
fun syncAppLauncherIcon(
  context: Context,
  themeColor: String? = null,
) {
  try {
    val appContext = context.applicationContext ?: context
    val effectiveThemeColor = themeColor
      ?: DefaultNavigationPreferencesRepository(appContext).getThemeColor()
    val targetVariant = resolveLauncherIconVariant(effectiveThemeColor)
    val pm = appContext.packageManager
    val packageName = appContext.packageName

    val targetComponent = ComponentName(packageName, targetVariant.aliasClassName)
    if (pm.getComponentEnabledSetting(targetComponent) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
      pm.setComponentEnabledSetting(
        targetComponent,
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP,
      )
    }

    for (variant in LauncherIconVariant.entries) {
      if (variant != targetVariant) {
        val component = ComponentName(packageName, variant.aliasClassName)
        val currentState = pm.getComponentEnabledSetting(component)
        val isEnabledByDefault = variant == LauncherIconVariant.SYSTEM
        if (currentState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
          (isEnabledByDefault && currentState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
        ) {
          pm.setComponentEnabledSetting(
            component,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP,
          )
        }
      }
    }
  } catch (_: Exception) {
    // Ignore when PackageManager is unavailable in unit tests
  }
}

/**
 * Resolves the manifest `<activity-alias>` class name dedicated to launching [tool] as a standalone
 * shortcut so the Recent Apps switcher displays the tool's icon and title.
 */
fun resolveToolShortcutAliasClassName(tool: Tool): String {
  return when (tool) {
    Tool.CALCULATOR -> "com.example.toolbox.MainActivityToolCalculator"
    Tool.NOTES -> "com.example.toolbox.MainActivityToolNotes"
    Tool.TODO_LIST -> "com.example.toolbox.MainActivityToolTodoList"
    Tool.CAMERA -> "com.example.toolbox.MainActivityToolCamera"
    Tool.GALLERY -> "com.example.toolbox.MainActivityToolGallery"
    Tool.FILES -> "com.example.toolbox.MainActivityToolFiles"
    Tool.UNIT_CONVERTER -> "com.example.toolbox.MainActivityToolUnitConverter"
    Tool.AUDIO_RECORDER -> "com.example.toolbox.MainActivityToolAudioRecorder"
    Tool.BLUETOOTH_REMOTE -> "com.example.toolbox.MainActivityToolBluetoothRemote"
    Tool.IMAGE_EDITOR -> "com.example.toolbox.MainActivityToolImageEditor"
    Tool.FILE_CONVERTER -> "com.example.toolbox.MainActivityToolFileConverter"
    Tool.RULER -> "com.example.toolbox.MainActivityToolRuler"
    Tool.VIDEO_EDITOR -> "com.example.toolbox.MainActivityToolVideoEditor"
    Tool.AUDIO_EDITOR -> "com.example.toolbox.MainActivityToolAudioEditor"
    Tool.COLOR_PICKER -> "com.example.toolbox.MainActivityToolColorPicker"
    Tool.DICE_ROLLER -> "com.example.toolbox.MainActivityToolDiceRoller"
    Tool.GUITAR_TUNER -> "com.example.toolbox.MainActivityToolGuitarTuner"
    Tool.METRONOME -> "com.example.toolbox.MainActivityToolMetronome"
    Tool.CHORD_BOOK -> "com.example.toolbox.MainActivityToolChordBook"
    Tool.PLANT_SCHEDULE -> "com.example.toolbox.MainActivityToolPlantSchedule"
    Tool.SETTINGS -> "com.example.toolbox.MainActivityToolSettings"
  }
}

/**
 * Parses a [Tool] from the target `<activity-alias>` component class name, or `null` if it does not
 * match a tool shortcut alias.
 */
fun parseStandaloneToolFromComponent(className: String?): Tool? {
  if (className.isNullOrBlank()) return null
  return Tool.entries.find { tool ->
    val alias = resolveToolShortcutAliasClassName(tool)
    className == alias || className.endsWith(".${alias.substringAfterLast('.')}")
  }
}

/**
 * Parses the standalone tool enum value from a raw string name.
 */
fun parseStandaloneToolName(name: String?): Tool? {
  return name?.let {
    try {
      Tool.valueOf(it)
    } catch (_: IllegalArgumentException) {
      null
    }
  }
}

/**
 * Parses the standalone tool requested in an incoming [Intent] if launched via home screen shortcut.
 */
fun parseStandaloneTool(intent: Intent?): Tool? {
  val name = intent?.getStringExtra(EXTRA_STANDALONE_TOOL)
    ?: intent?.data?.getQueryParameter("tool")
    ?: intent?.data?.lastPathSegment
  return parseStandaloneToolName(name)
    ?: parseStandaloneToolFromComponent(intent?.component?.className)
}

/**
 * Parses the folder path requested in an incoming [Intent] if launched via a folder home screen shortcut.
 */
fun parseFolderShortcut(intent: Intent?): String? {
  return intent?.getStringExtra(EXTRA_FOLDER_PATH)
    ?: intent?.data?.getQueryParameter("folder")
}

/**
 * Resolves the label to display in the Recent Apps switcher for a standalone tool or folder shortcut,
 * or `null` when running the main Toolbox application.
 */
fun resolveStandaloneTaskLabel(
  standaloneTool: Tool?,
  initialFolder: String? = null,
): String? {
  if (initialFolder != null) {
    val folderName = File(initialFolder).name
    return folderName.ifBlank { standaloneTool?.title ?: Tool.FILES.title }
  }
  return standaloneTool?.title
}

/**
 * Resolves the [ImageVector] icon to display in the Recent Apps switcher for a standalone tool or
 * folder shortcut, or `null` when running the main Toolbox application.
 */
fun resolveStandaloneTaskIconVector(
  standaloneTool: Tool?,
  initialFolder: String? = null,
): ImageVector? {
  if (initialFolder != null) {
    return FOLDER_SHORTCUT_ICON
  }
  return standaloneTool?.icon
}

/**
 * Updates [activity]'s [ActivityManager.TaskDescription] so the Recent Apps switcher displays the
 * matching tool (or folder) icon, title, and themed background color when opened from a shortcut.
 */
fun syncStandaloneTaskDescription(
  activity: Activity,
  standaloneTool: Tool?,
  initialFolder: String? = null,
  themeColor: String? = null,
) {
  val label = resolveStandaloneTaskLabel(standaloneTool, initialFolder) ?: return
  val iconVector = resolveStandaloneTaskIconVector(standaloneTool, initialFolder) ?: return
  try {
    val backgroundColor = resolveShortcutBackgroundColor(themeColor = themeColor, context = activity)
    val foregroundColor = resolveShortcutForegroundColor(backgroundColor)
    val iconBitmap = createToolShortcutBitmap(
      imageVector = iconVector,
      backgroundColor = backgroundColor,
      foregroundColor = foregroundColor,
    )
    @Suppress("DEPRECATION")
    val taskDescription = ActivityManager.TaskDescription(
      label,
      iconBitmap,
      backgroundColor.toArgb(),
    )
    activity.setTaskDescription(taskDescription)
  } catch (_: Exception) {
    // Ignore when Bitmap/TaskDescription is unavailable in unit tests
  }
}

/**
 * Creates the [Intent] used to launch [tool] in a standalone instance without navigation.
 */
fun createStandaloneToolIntent(context: Context, tool: Tool): Intent {
  return Intent(context, MainActivity::class.java).apply {
    component = ComponentName(context, resolveToolShortcutAliasClassName(tool))
    action = Intent.ACTION_VIEW
    putExtra(EXTRA_STANDALONE_TOOL, tool.name)
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
  }
}

/**
 * Creates the [Intent] used to launch [Tool.FILES] at [folder] from a home screen shortcut.
 */
fun createFolderShortcutIntent(context: Context, folder: File): Intent {
  return Intent(context, MainActivity::class.java).apply {
    component = ComponentName(context, resolveToolShortcutAliasClassName(Tool.FILES))
    action = Intent.ACTION_VIEW
    putExtra(EXTRA_STANDALONE_TOOL, Tool.FILES.name)
    putExtra(EXTRA_FOLDER_PATH, folder.absolutePath)
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
  }
}

/**
 * Resolves the background [Color] for a home screen shortcut icon according to [themeColor].
 *
 * When [themeColor] is a valid hex string (preset or custom), returns the parsed color.
 * When [themeColor] is [DEFAULT_THEME_COLOR_SYSTEM] (or null/invalid), uses the Android 12+
 * dynamic primary color when [context] is available, falling back to Toolbox Purple (`#8E66A6`).
 */
fun resolveShortcutBackgroundColor(
  themeColor: String?,
  context: Context? = null,
): Color {
  val effectiveThemeColor = themeColor ?: run {
    if (context != null) {
      try {
        DefaultNavigationPreferencesRepository(context.applicationContext).getThemeColor()
      } catch (_: Exception) {
        DEFAULT_THEME_COLOR_SYSTEM
      }
    } else {
      DEFAULT_THEME_COLOR_SYSTEM
    }
  }

  if (!effectiveThemeColor.equals(DEFAULT_THEME_COLOR_SYSTEM, ignoreCase = true)) {
    parseHexColor(effectiveThemeColor)?.let { return it }
  }

  if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    try {
      return dynamicLightColorScheme(context).primary
    } catch (_: Exception) {
      // Fall back to default theme color below
    }
  }

  return parseHexColor(FALLBACK_THEME_COLOR_HEX) ?: Color(0xFF8E66A6)
}

/**
 * Resolves a contrasting foreground [Color] for drawing a tool's icon on top of [backgroundColor].
 */
fun resolveShortcutForegroundColor(backgroundColor: Color): Color {
  return if (backgroundColor.luminance() > 0.5f) Color.Black else Color.White
}

/**
 * Renders an adaptive shortcut icon [Bitmap] displaying [imageVector] centered over [backgroundColor].
 */
fun createToolShortcutBitmap(
  imageVector: ImageVector,
  backgroundColor: Color,
  foregroundColor: Color = resolveShortcutForegroundColor(backgroundColor),
  sizePx: Int = SHORTCUT_ICON_SIZE_PX,
): Bitmap {
  val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(bitmap.asImageBitmap())

  // Draw themed background
  val bgPaint = Paint().apply {
    isAntiAlias = true
    style = PaintingStyle.Fill
    color = backgroundColor
  }
  canvas.drawRect(Rect(0f, 0f, sizePx.toFloat(), sizePx.toFloat()), bgPaint)

  // Scale and center the tool's ImageVector within the adaptive icon safe zone
  val viewportWidth = imageVector.viewportWidth.takeIf { it > 0f } ?: 24f
  val viewportHeight = imageVector.viewportHeight.takeIf { it > 0f } ?: 24f
  val targetIconSizePx = sizePx * (SHORTCUT_FOREGROUND_ICON_DP / SHORTCUT_ADAPTIVE_VIEWPORT_DP)
  val scaleX = targetIconSizePx / viewportWidth
  val scaleY = targetIconSizePx / viewportHeight
  val scale = minOf(scaleX, scaleY)
  val dx = (sizePx - viewportWidth * scale) / 2f
  val dy = (sizePx - viewportHeight * scale) / 2f

  canvas.save()
  canvas.translate(dx, dy)
  canvas.scale(scale, scale)
  drawVectorGroup(canvas, imageVector.root, foregroundColor)
  canvas.restore()

  return bitmap
}

private fun drawVectorGroup(
  canvas: Canvas,
  group: VectorGroup,
  tintColor: Color,
) {
  canvas.save()
  canvas.translate(group.translationX, group.translationY)
  if (group.rotation != 0f) {
    canvas.translate(group.pivotX, group.pivotY)
    canvas.rotate(group.rotation)
    canvas.translate(-group.pivotX, -group.pivotY)
  }
  if (group.scaleX != 1f || group.scaleY != 1f) {
    canvas.translate(group.pivotX, group.pivotY)
    canvas.scale(group.scaleX, group.scaleY)
    canvas.translate(-group.pivotX, -group.pivotY)
  }
  if (group.clipPathData.isNotEmpty()) {
    val clipPath = group.clipPathData.toPath()
    canvas.clipPath(clipPath)
  }
  for (node in group) {
    when (node) {
      is VectorPath -> {
        val path = node.pathData.toPath().apply {
          fillType = node.pathFillType
        }
        if (node.fill != null) {
          val paint = Paint().apply {
            isAntiAlias = true
            style = PaintingStyle.Fill
            color = tintColor.copy(alpha = (tintColor.alpha * node.fillAlpha).coerceIn(0f, 1f))
          }
          canvas.drawPath(path, paint)
        }
        if (node.stroke != null && node.strokeLineWidth > 0f) {
          val paint = Paint().apply {
            isAntiAlias = true
            style = PaintingStyle.Stroke
            strokeWidth = node.strokeLineWidth
            strokeCap = node.strokeLineCap
            strokeJoin = node.strokeLineJoin
            strokeMiterLimit = node.strokeLineMiter
            color = tintColor.copy(alpha = (tintColor.alpha * node.strokeAlpha).coerceIn(0f, 1f))
          }
          canvas.drawPath(path, paint)
        }
      }
      is VectorGroup -> {
        drawVectorGroup(canvas, node, tintColor)
      }
    }
  }
  canvas.restore()
}

/**
 * Creates an [IconCompat] for a given [Tool] shortcut with the tool's icon and themed background color.
 */
fun createToolShortcutIcon(
  context: Context,
  tool: Tool,
  themeColor: String? = null,
): IconCompat {
  return try {
    val backgroundColor = resolveShortcutBackgroundColor(themeColor = themeColor, context = context)
    val foregroundColor = resolveShortcutForegroundColor(backgroundColor)
    val bitmap = createToolShortcutBitmap(
      imageVector = tool.icon,
      backgroundColor = backgroundColor,
      foregroundColor = foregroundColor,
    )
    IconCompat.createWithAdaptiveBitmap(bitmap)
  } catch (_: Exception) {
    try {
      IconCompat.createWithResource(context, R.mipmap.ic_launcher)
    } catch (_: Exception) {
      IconCompat.createWithResource(context, android.R.drawable.sym_def_app_icon)
    }
  }
}

/**
 * The folder [ImageVector] used for folders in the Files tool and for folder Home screen shortcuts.
 */
val FOLDER_SHORTCUT_ICON: ImageVector = Icons.Default.Folder

/**
 * Renders an adaptive shortcut icon [Bitmap] displaying the app's folder icon ([FOLDER_SHORTCUT_ICON])
 * centered over the themed [backgroundColor].
 */
fun createFolderShortcutBitmap(
  backgroundColor: Color,
  foregroundColor: Color = resolveShortcutForegroundColor(backgroundColor),
  sizePx: Int = SHORTCUT_ICON_SIZE_PX,
): Bitmap {
  return createToolShortcutBitmap(
    imageVector = FOLDER_SHORTCUT_ICON,
    backgroundColor = backgroundColor,
    foregroundColor = foregroundColor,
    sizePx = sizePx,
  )
}

/**
 * Creates an [IconCompat] for a folder shortcut using the same folder icon as the Files tool
 * and a background color themed according to [themeColor].
 */
fun createFolderShortcutIcon(
  context: Context,
  themeColor: String? = null,
): IconCompat {
  return try {
    val backgroundColor = resolveShortcutBackgroundColor(themeColor = themeColor, context = context)
    val foregroundColor = resolveShortcutForegroundColor(backgroundColor)
    val bitmap = createFolderShortcutBitmap(
      backgroundColor = backgroundColor,
      foregroundColor = foregroundColor,
    )
    IconCompat.createWithAdaptiveBitmap(bitmap)
  } catch (_: Exception) {
    try {
      IconCompat.createWithResource(context, R.mipmap.ic_launcher)
    } catch (_: Exception) {
      IconCompat.createWithResource(context, android.R.drawable.sym_def_app_icon)
    }
  }
}

/**
 * Requests pinning a home screen shortcut to launch [tool] in a standalone instance.
 */
fun addToolToHomeScreen(
  context: Context,
  tool: Tool,
  themeColor: String? = null,
): Boolean {
  val intent = createStandaloneToolIntent(context, tool)

  return try {
    if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
      val effectiveThemeColor = themeColor ?: run {
        try {
          DefaultNavigationPreferencesRepository(context.applicationContext ?: context).getThemeColor()
        } catch (_: Exception) {
          DEFAULT_THEME_COLOR_SYSTEM
        }
      }
      syncAppLauncherIcon(context, effectiveThemeColor)
      val activeVariant = resolveLauncherIconVariant(effectiveThemeColor)
      val pinShortcutInfo = ShortcutInfoCompat.Builder(context, "shortcut_tool_${tool.name}")
        .setShortLabel(tool.title)
        .setLongLabel(tool.title)
        .setIcon(createToolShortcutIcon(context, tool, effectiveThemeColor))
        .setActivity(ComponentName(context.packageName, activeVariant.aliasClassName))
        .setIntent(intent)
        .setAlwaysBadged()
        .build()

      val pinned = ShortcutManagerCompat.requestPinShortcut(context, pinShortcutInfo, null)
      Toast.makeText(context, "Adding ${tool.title} to Home screen...", Toast.LENGTH_SHORT).show()
      pinned
    } else {
      Toast.makeText(context, "Home screen shortcuts not supported by launcher", Toast.LENGTH_SHORT).show()
      false
    }
  } catch (e: Exception) {
    Toast.makeText(context, "Could not add ${tool.title} to Home screen", Toast.LENGTH_SHORT).show()
    false
  }
}

/**
 * Requests pinning a home screen shortcut to launch [Tool.FILES] navigated directly to [folder].
 */
fun addFolderToHomeScreen(
  context: Context,
  folder: File,
  themeColor: String? = null,
): Boolean {
  val folderName = folder.name.ifBlank { "Files" }
  val intent = createFolderShortcutIntent(context, folder)

  return try {
    if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
      val effectiveThemeColor = themeColor ?: run {
        try {
          DefaultNavigationPreferencesRepository(context.applicationContext ?: context).getThemeColor()
        } catch (_: Exception) {
          DEFAULT_THEME_COLOR_SYSTEM
        }
      }
      syncAppLauncherIcon(context, effectiveThemeColor)
      val activeVariant = resolveLauncherIconVariant(effectiveThemeColor)
      val shortcutId = "shortcut_folder_${folder.absolutePath.hashCode()}"
      val pinShortcutInfo = ShortcutInfoCompat.Builder(context, shortcutId)
        .setShortLabel(folderName)
        .setLongLabel(folderName)
        .setIcon(createFolderShortcutIcon(context, effectiveThemeColor))
        .setActivity(ComponentName(context.packageName, activeVariant.aliasClassName))
        .setIntent(intent)
        .setAlwaysBadged()
        .build()

      val pinned = ShortcutManagerCompat.requestPinShortcut(context, pinShortcutInfo, null)
      Toast.makeText(context, "Adding $folderName to Home screen...", Toast.LENGTH_SHORT).show()
      pinned
    } else {
      Toast.makeText(context, "Home screen shortcuts not supported by launcher", Toast.LENGTH_SHORT).show()
      false
    }
  } catch (e: Exception) {
    Toast.makeText(context, "Could not add $folderName to Home screen", Toast.LENGTH_SHORT).show()
    false
  }
}
