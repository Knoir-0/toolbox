package com.example.toolbox.ui.main

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.toolbox.MainActivity
import com.example.toolbox.R
import java.io.File

const val EXTRA_STANDALONE_TOOL = "extra_standalone_tool"
const val EXTRA_FOLDER_PATH = "extra_folder_path"

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
}

/**
 * Parses the folder path requested in an incoming [Intent] if launched via a folder home screen shortcut.
 */
fun parseFolderShortcut(intent: Intent?): String? {
  return intent?.getStringExtra(EXTRA_FOLDER_PATH)
    ?: intent?.data?.getQueryParameter("folder")
}

/**
 * Creates the [Intent] used to launch [tool] in a standalone instance without navigation.
 */
fun createStandaloneToolIntent(context: Context, tool: Tool): Intent {
  return Intent(context, MainActivity::class.java).apply {
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
    action = Intent.ACTION_VIEW
    putExtra(EXTRA_STANDALONE_TOOL, Tool.FILES.name)
    putExtra(EXTRA_FOLDER_PATH, folder.absolutePath)
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK or Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
  }
}

/**
 * Creates an [IconCompat] for a given [Tool] shortcut.
 */
fun createToolShortcutIcon(context: Context, tool: Tool): IconCompat {
  return try {
    IconCompat.createWithResource(context, R.mipmap.ic_launcher)
  } catch (_: Exception) {
    IconCompat.createWithResource(context, android.R.drawable.sym_def_app_icon)
  }
}

/**
 * Creates an [IconCompat] for a folder shortcut.
 */
fun createFolderShortcutIcon(context: Context): IconCompat {
  return try {
    IconCompat.createWithResource(context, R.mipmap.ic_launcher)
  } catch (_: Exception) {
    IconCompat.createWithResource(context, android.R.drawable.sym_def_app_icon)
  }
}

/**
 * Requests pinning a home screen shortcut to launch [tool] in a standalone instance.
 */
fun addToolToHomeScreen(context: Context, tool: Tool): Boolean {
  val intent = createStandaloneToolIntent(context, tool)

  return try {
    if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
      val pinShortcutInfo = ShortcutInfoCompat.Builder(context, "shortcut_tool_${tool.name}")
        .setShortLabel(tool.title)
        .setLongLabel(tool.title)
        .setIcon(createToolShortcutIcon(context, tool))
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
fun addFolderToHomeScreen(context: Context, folder: File): Boolean {
  val folderName = folder.name.ifBlank { "Files" }
  val intent = createFolderShortcutIntent(context, folder)

  return try {
    if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
      val shortcutId = "shortcut_folder_${folder.absolutePath.hashCode()}"
      val pinShortcutInfo = ShortcutInfoCompat.Builder(context, shortcutId)
        .setShortLabel(folderName)
        .setLongLabel(folderName)
        .setIcon(createFolderShortcutIcon(context))
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
