package com.example.toolbox.ui.tools

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.example.toolbox.ui.main.SyncToolScreenOrientation
import com.example.toolbox.ui.main.Tool
import java.io.File
import java.util.UUID

/**
 * Shared cross-tool action helpers and sub-screen/dialog host so any tool across Toolbox
 * can offer unified Files + cross-tool context menu actions.
 */
object CrossToolActions {

  /**
   * Computes the `ClipDescription` MIME types for a shared [file] so Android's Sharesheet
   * recognizes both the primary file MIME type and any thumbnail stream type (`image/jpeg` for
   * videos, `image/png` for SVGs) served by [ToolboxFileProvider].
   */
  fun resolveShareClipMimeTypes(file: File, primaryMimeOverride: String? = null): Array<String> {
    val primaryMime = primaryMimeOverride
      ?.takeIf { it.isNotBlank() && it != "*/*" }
      ?: FileManagerEngine.getMimeType(file)
    return ToolboxFileProvider.computeStreamTypesForFile(file, "*/*")?.let { streamTypes ->
      if (primaryMime in streamTypes) streamTypes else arrayOf(primaryMime, *streamTypes)
    } ?: arrayOf(primaryMime)
  }

  /**
   * Computes the aggregate MIME type for a multi-file `ACTION_SEND_MULTIPLE` share intent.
   */
  fun resolveAggregateShareMimeType(files: List<File>): String {
    if (files.isEmpty()) return "*/*"
    if (files.size == 1) return FileManagerEngine.getMimeType(files.first())
    val mimes = files.map { FileManagerEngine.getMimeType(it) }
    val allImages = files.indices.all { i ->
      FileManagerEngine.isImageFile(files[i]) || mimes[i].startsWith("image/")
    }
    val allVideos = files.indices.all { i ->
      FileManagerEngine.isVideoFile(files[i]) || mimes[i].startsWith("video/")
    }
    return when {
      allImages -> "image/*"
      allVideos -> "video/*"
      else -> "*/*"
    }
  }

  /**
   * Builds a [ClipData] containing all [uris] and their stream MIME types so that
   * `Intent.createChooser` grants the Android Sharesheet (`ChooserActivity` / `IntentResolver`)
   * `FLAG_GRANT_READ_URI_PERMISSION` to render image and video preview thumbnails.
   */
  fun buildShareClipData(
    label: String,
    files: List<File>,
    uris: List<Uri>,
    primaryMimeOverride: String? = null,
  ): ClipData? {
    if (uris.isEmpty()) return null
    val mimeSet = LinkedHashSet<String>()
    if (files.size == 1) {
      resolveShareClipMimeTypes(files.first(), primaryMimeOverride).forEach { mimeSet.add(it) }
    } else {
      for (file in files) {
        resolveShareClipMimeTypes(file).forEach { mimeSet.add(it) }
      }
    }
    if (mimeSet.isEmpty()) {
      mimeSet.add("*/*")
    }
    val description = ClipDescription(label, mimeSet.toTypedArray())
    val clipData = ClipData(description, ClipData.Item(uris.first()))
    for (i in 1 until uris.size) {
      clipData.addItem(ClipData.Item(uris[i]))
    }
    return clipData
  }

  fun shareFile(
    context: Context,
    file: File,
    mimeTypeOverride: String? = null,
    chooserTitle: String? = null,
  ) {
    try {
      val mime = mimeTypeOverride
        ?.takeIf { it.isNotBlank() && it != "*/*" }
        ?: FileManagerEngine.getMimeType(file)
      if (FileManagerEngine.isVideoFile(file) || mime.startsWith("video/")) {
        ToolboxFileProvider.getOrCreateVideoShareThumbnailFile(context, file)
      }
      val contentUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
      )
      val title = chooserTitle ?: "Share ${file.name}"
      val clip = buildShareClipData(
        label = file.name,
        files = listOf(file),
        uris = listOf(contentUri),
        primaryMimeOverride = mime,
      )
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, contentUri)
        putExtra(Intent.EXTRA_TITLE, file.name)
        clipData = clip
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      val chooser = Intent.createChooser(shareIntent, title).apply {
        clipData = clip
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(chooser)
    } catch (e: Exception) {
      Toast.makeText(context, "Could not share file: ${e.message}", Toast.LENGTH_SHORT).show()
    }
  }

  fun shareMultipleFiles(
    context: Context,
    filesToShare: List<File>,
    chooserTitle: String? = null,
  ) {
    val resolvedFiles = FileManagerEngine.collectShareableFiles(filesToShare)
    if (resolvedFiles.isEmpty()) {
      Toast.makeText(context, "No files to share", Toast.LENGTH_SHORT).show()
    } else if (resolvedFiles.size == 1) {
      shareFile(context, resolvedFiles.first(), chooserTitle = chooserTitle)
    } else {
      try {
        val uris = ArrayList<Uri>(resolvedFiles.size)
        for (f in resolvedFiles) {
          val mime = FileManagerEngine.getMimeType(f)
          if (FileManagerEngine.isVideoFile(f) || mime.startsWith("video/")) {
            ToolboxFileProvider.getOrCreateVideoShareThumbnailFile(context, f)
          }
          uris.add(
            FileProvider.getUriForFile(
              context,
              "${context.packageName}.fileprovider",
              f,
            )
          )
        }
        val title = chooserTitle ?: "Share ${resolvedFiles.size} files"
        val aggregateMime = resolveAggregateShareMimeType(resolvedFiles)
        val clip = buildShareClipData(
          label = title,
          files = resolvedFiles,
          uris = uris,
        )
        val allMedia = resolvedFiles.all { f ->
          val m = FileManagerEngine.getMimeType(f)
          FileManagerEngine.isImageFile(f) || FileManagerEngine.isVideoFile(f) ||
            m.startsWith("image/") || m.startsWith("video/")
        }
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
          type = aggregateMime
          if (aggregateMime == "*/*" && allMedia) {
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
          }
          putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
          putExtra(Intent.EXTRA_TITLE, title)
          clipData = clip
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, title).apply {
          clipData = clip
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
      } catch (e: Exception) {
        Toast.makeText(context, "Could not share files: ${e.message}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  fun shareText(context: Context, title: String, text: String) {
    try {
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      val chooser = Intent.createChooser(shareIntent, "Share $title").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(chooser)
    } catch (e: Exception) {
      Toast.makeText(context, "Could not share: ${e.message}", Toast.LENGTH_SHORT).show()
    }
  }

  fun copyTextToClipboard(context: Context, label: String, text: String, showToast: Boolean = true) {
    try {
      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
      clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
      if (showToast) {
        Toast.makeText(context, "Copied $label", Toast.LENGTH_SHORT).show()
      }
    } catch (_: Throwable) {}
  }

  fun saveTextToNotes(
    context: Context,
    title: String,
    content: String = "",
    label: String? = null,
    labels: List<String> = if (!label.isNullOrBlank()) listOf(label) else emptyList(),
    checklistItems: List<NoteChecklistItem> = emptyList(),
    isChecklist: Boolean = checklistItems.isNotEmpty(),
    showToast: Boolean = true,
  ): NoteItem {
    val repo = SharedPreferencesNotesRepository(context)
    val existing = repo.loadNotes().toMutableList()
    val now = System.currentTimeMillis()
    val resolvedLabels = if (labels.isNotEmpty()) labels else if (!label.isNullOrBlank()) listOf(label) else emptyList()
    val created = NoteItem(
      id = UUID.randomUUID().toString(),
      title = title.trim().ifEmpty { "Note" },
      content = content,
      isChecklist = isChecklist,
      checklistItems = checklistItems,
      labels = resolvedLabels,
      createdAt = now,
      updatedAt = now,
    )
    existing.add(0, created)
    repo.saveNotes(existing)
    if (resolvedLabels.isNotEmpty()) {
      val currentLabels = repo.loadLabels().toMutableList()
      var changed = false
      for (lbl in resolvedLabels) {
        if (currentLabels.none { it.equals(lbl, ignoreCase = true) }) {
          currentLabels.add(lbl)
          changed = true
        }
      }
      if (changed) {
        repo.saveLabels(currentLabels.sorted())
      }
    }
    NotesWidgets.updateAllWidgets(context)
    if (showToast) {
      Toast.makeText(context, "Saved \"${created.title}\" to Notes", Toast.LENGTH_SHORT).show()
    }
    return created
  }

  fun saveTaskToTodoList(
    context: Context,
    title: String,
    notes: String = "",
    subtasks: List<String> = emptyList(),
    scheduledAtMillis: Long? = null,
    showToast: Boolean = true,
  ): TodoItem {
    val repo = SharedPreferencesTodoListRepository(context)
    val existing = repo.loadTasks().toMutableList()
    val now = System.currentTimeMillis()
    val subItems = subtasks.map { text ->
      TodoSubTask(
        id = UUID.randomUUID().toString(),
        title = text.trim(),
        isCompleted = false,
      )
    }.filter { it.title.isNotEmpty() }
    val created = TodoItem(
      id = UUID.randomUUID().toString(),
      title = title.trim().ifEmpty { "Task" },
      notes = notes,
      subtasks = subItems,
      scheduledAtMillis = scheduledAtMillis,
      createdAt = now,
    )
    existing.add(0, created)
    repo.saveTasks(existing)
    TodoListWidgets.updateAllWidgets(context)
    if (showToast) {
      val subSuffix = if (subItems.isNotEmpty()) " (${subItems.size} subtasks)" else ""
      Toast.makeText(context, "Added \"${created.title}\"$subSuffix to To Do", Toast.LENGTH_SHORT).show()
    }
    return created
  }

  fun saveFileToNotes(context: Context, file: File, showToast: Boolean = true): NoteItem? {
    val repo = SharedPreferencesNotesRepository(context)
    val imported = FileManagerEngine.importFileToNotes(file, repo)
    if (imported != null) {
      NotesWidgets.updateAllWidgets(context)
    }
    if (showToast) {
      if (imported != null) {
        Toast.makeText(context, "Saved \"${imported.title}\" to Notes", Toast.LENGTH_SHORT).show()
      } else {
        Toast.makeText(context, "Could not import ${file.name} to Notes", Toast.LENGTH_SHORT).show()
      }
    }
    return imported
  }

  fun importFileToTodo(context: Context, file: File, showToast: Boolean = true): TodoItem? {
    val repo = SharedPreferencesTodoListRepository(context)
    val imported = FileManagerEngine.importFileToTodoList(file, repo)
    if (imported != null) {
      TodoListWidgets.updateAllWidgets(context)
    }
    if (showToast) {
      if (imported != null) {
        val subCount = imported.subtasks.size
        val detail = if (subCount > 0) " ($subCount items)" else ""
        Toast.makeText(context, "Imported \"${imported.title}\"$detail to To Do", Toast.LENGTH_SHORT).show()
      } else {
        Toast.makeText(context, "Could not import ${file.name} to To Do", Toast.LENGTH_SHORT).show()
      }
    }
    return imported
  }

  fun exportTextAsFile(
    context: Context,
    suggestedFileName: String,
    content: String,
    targetDir: File? = null,
    showToast: Boolean = true,
  ): File? {
    return try {
      val dir = targetDir ?: File(FileManagerEngine.getSamplesDirectory(context), "Documents").apply { mkdirs() }
      if (!dir.exists()) dir.mkdirs()
      val sanitized = suggestedFileName
        .replace(Regex("[\\\\/:*?\"<>|]"), "_")
        .trim()
        .ifEmpty { "note.text" }
      val finalName = if (sanitized.contains(".")) sanitized else "$sanitized.text"
      var candidate = File(dir, finalName)
      var counter = 1
      val stem = finalName.substringBeforeLast(".")
      val ext = finalName.substringAfterLast(".", "text")
      while (candidate.exists()) {
        candidate = File(dir, "$stem ($counter).$ext")
        counter++
      }
      candidate.writeText(content)
      if (showToast) {
        Toast.makeText(context, "Exported ${candidate.name}", Toast.LENGTH_SHORT).show()
      }
      candidate
    } catch (e: Exception) {
      if (showToast) {
        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
      }
      null
    }
  }
}

@Stable
class CrossToolFileHostState {
  var fileForConverter by mutableStateOf<File?>(null)
  var fileForImage by mutableStateOf<File?>(null)
  var fileForImageEditor by mutableStateOf<File?>(null)
  var fileForVideoEditor by mutableStateOf<File?>(null)
  var fileForAudioEditor by mutableStateOf<File?>(null)
  var fileForArchive by mutableStateOf<File?>(null)
  var filesForArchiveCompression by mutableStateOf<List<File>?>(null)
  var fileForPdfViewer by mutableStateOf<File?>(null)
  var fileForMusicPlayer by mutableStateOf<File?>(null)
  var fileForGallery by mutableStateOf<File?>(null)
  var colorHexForColorPicker by mutableStateOf<String?>(null)
  var directoryForFiles by mutableStateOf<File?>(null)

  var fileToRename by mutableStateOf<File?>(null)
  var fileToDelete by mutableStateOf<File?>(null)
  var fileForDetails by mutableStateOf<File?>(null)
  var fileForTextEdit by mutableStateOf<File?>(null)

  var fileForColorPicker: File?
    get() = null
    set(value) {
      if (value != null) {
        inspectImageColor(value)
      } else {
        colorHexForColorPicker = null
      }
    }

  val hasActiveSubScreen: Boolean
    get() = fileForConverter != null ||
      fileForImage != null ||
      fileForImageEditor != null ||
      fileForVideoEditor != null ||
      fileForAudioEditor != null ||
      fileForArchive != null ||
      !filesForArchiveCompression.isNullOrEmpty() ||
      fileForPdfViewer != null ||
      fileForMusicPlayer != null ||
      fileForGallery != null ||
      colorHexForColorPicker != null ||
      directoryForFiles != null

  fun inspectImageColor(file: File) {
    val hex = FileManagerEngine.extractDominantColorHex(file) ?: "#10B981"
    colorHexForColorPicker = hex
  }

  fun openInFiles(fileOrDir: File) {
    val dir = if (fileOrDir.isDirectory) fileOrDir else (fileOrDir.parentFile ?: fileOrDir)
    directoryForFiles = dir
  }
}

@Composable
fun rememberCrossToolFileHostState(): CrossToolFileHostState {
  return remember { CrossToolFileHostState() }
}

@Composable
fun CrossToolFileHostState.RenderSubScreen(
  modifier: Modifier = Modifier,
  defaultDirectory: File? = null,
  onReturnedFromSubScreen: () -> Unit = {},
  onRefreshNeeded: () -> Unit = {},
): Boolean {
  val notifyReturned = {
    onReturnedFromSubScreen()
    onRefreshNeeded()
  }
  val activeConverterFile = fileForConverter
  if (activeConverterFile != null) {
    SyncToolScreenOrientation(tool = Tool.FILE_CONVERTER, restorePreviousOnDispose = true)
    FileConverterScreen(
      modifier = modifier,
      initialFile = activeConverterFile,
      initialDirectory = activeConverterFile.parentFile ?: defaultDirectory,
      onNavigateBack = {
        fileForConverter = null
        notifyReturned()
      },
    )
    return true
  }

  val activeImageFile = fileForImage
  if (activeImageFile != null) {
    SyncToolScreenOrientation(tool = Tool.IMAGE, restorePreviousOnDispose = true)
    ImageScreen(
      modifier = modifier,
      initialFile = activeImageFile,
      initialDirectory = activeImageFile.parentFile ?: defaultDirectory,
      onNavigateBack = {
        fileForImage = null
        notifyReturned()
      },
    )
    return true
  }

  val activeImageEditorFile = fileForImageEditor
  if (activeImageEditorFile != null) {
    SyncToolScreenOrientation(tool = Tool.IMAGE_EDITOR, restorePreviousOnDispose = true)
    ImageEditorScreen(
      modifier = modifier,
      initialFile = activeImageEditorFile,
      onNavigateBack = {
        fileForImageEditor = null
        notifyReturned()
      },
    )
    return true
  }

  val activeVideoEditorFile = fileForVideoEditor
  if (activeVideoEditorFile != null) {
    SyncToolScreenOrientation(tool = Tool.VIDEO_EDITOR, restorePreviousOnDispose = true)
    VideoEditorScreen(
      initialFile = activeVideoEditorFile,
      initialDirectory = activeVideoEditorFile.parentFile ?: defaultDirectory,
      modifier = modifier,
      onNavigateBack = {
        fileForVideoEditor = null
        notifyReturned()
      },
    )
    return true
  }

  val activeAudioEditorFile = fileForAudioEditor
  if (activeAudioEditorFile != null) {
    SyncToolScreenOrientation(tool = Tool.AUDIO_EDITOR, restorePreviousOnDispose = true)
    AudioEditorScreen(
      modifier = modifier,
      initialFile = activeAudioEditorFile,
      initialDirectory = activeAudioEditorFile.parentFile ?: defaultDirectory,
      onNavigateBack = {
        fileForAudioEditor = null
        notifyReturned()
      },
    )
    return true
  }

  val activeArchiveFile = fileForArchive
  if (activeArchiveFile != null) {
    SyncToolScreenOrientation(tool = Tool.ARCHIVE, restorePreviousOnDispose = true)
    ArchiveScreen(
      modifier = modifier,
      initialFile = activeArchiveFile,
      initialDirectory = activeArchiveFile.parentFile ?: defaultDirectory,
      onNavigateBack = {
        fileForArchive = null
        notifyReturned()
      },
    )
    return true
  }

  val activeCompressionFiles = filesForArchiveCompression
  if (!activeCompressionFiles.isNullOrEmpty()) {
    SyncToolScreenOrientation(tool = Tool.ARCHIVE, restorePreviousOnDispose = true)
    ArchiveScreen(
      modifier = modifier,
      initialFilesToCompress = activeCompressionFiles,
      initialDirectory = activeCompressionFiles.first().parentFile ?: defaultDirectory,
      onNavigateBack = {
        filesForArchiveCompression = null
        notifyReturned()
      },
    )
    return true
  }

  val activePdfViewerFile = fileForPdfViewer
  if (activePdfViewerFile != null) {
    SyncToolScreenOrientation(tool = Tool.PDF_VIEWER, restorePreviousOnDispose = true)
    DocumentViewerScreen(
      modifier = modifier,
      initialFile = activePdfViewerFile,
      initialDirectory = activePdfViewerFile.parentFile ?: defaultDirectory,
      onNavigateBack = {
        fileForPdfViewer = null
        notifyReturned()
      },
    )
    return true
  }

  val activeMusicFile = fileForMusicPlayer
  if (activeMusicFile != null) {
    val isDir = activeMusicFile.isDirectory
    SyncToolScreenOrientation(tool = Tool.MUSIC, restorePreviousOnDispose = true)
    MusicPlayerScreen(
      modifier = modifier,
      initialFile = if (isDir) null else activeMusicFile,
      initialDirectory = if (isDir) activeMusicFile else (activeMusicFile.parentFile ?: defaultDirectory),
      initialTab = MusicLibraryTab.SONGS,
      onNavigateBack = {
        fileForMusicPlayer = null
        notifyReturned()
      },
    )
    return true
  }

  val activeGalleryTarget = fileForGallery
  if (activeGalleryTarget != null) {
    val isDir = activeGalleryTarget.isDirectory
    val targetAlbumDir = if (isDir) activeGalleryTarget else (activeGalleryTarget.parentFile ?: defaultDirectory ?: activeGalleryTarget)
    SyncToolScreenOrientation(tool = Tool.GALLERY, restorePreviousOnDispose = true)
    GalleryScreen(
      modifier = modifier,
      rootDirectory = targetAlbumDir,
      initialAlbumDirectory = targetAlbumDir,
      initialPhotoFile = if (isDir) null else activeGalleryTarget,
      onNavigateBack = {
        fileForGallery = null
        notifyReturned()
      },
    )
    return true
  }

  val activeColorHex = colorHexForColorPicker
  if (activeColorHex != null) {
    SyncToolScreenOrientation(tool = Tool.COLOR_PICKER, restorePreviousOnDispose = true)
    ColorPickerScreen(
      modifier = modifier,
      initialHexColor = activeColorHex,
      onNavigateBack = {
        colorHexForColorPicker = null
        notifyReturned()
      },
    )
    return true
  }

  val activeFilesDir = directoryForFiles
  if (activeFilesDir != null) {
    SyncToolScreenOrientation(tool = Tool.FILES, restorePreviousOnDispose = true)
    FilesScreen(
      modifier = modifier,
      initialDirectory = activeFilesDir,
      onNavigateBack = {
        directoryForFiles = null
        notifyReturned()
      },
    )
    return true
  }

  return false
}

@Composable
fun CrossToolFileHostState.RenderFileDialogs(
  onFileMutated: () -> Unit = {},
  onRefreshNeeded: () -> Unit = {},
  onFileRenamed: (oldFile: File, newFile: File) -> Unit = { _, _ -> },
  onFileDeleted: (deletedFile: File) -> Unit = {},
) {
  val context = LocalContext.current
  val notifyMutated = {
    onFileMutated()
    onRefreshNeeded()
  }

  fileToRename?.let { targetFile ->
    val item = remember(targetFile) { FileManagerEngine.toFileItem(targetFile) }
    RenameFileDialog(
      item = item,
      onDismiss = { fileToRename = null },
      onConfirm = { newName ->
        val result = FileManagerEngine.renameFile(targetFile, newName)
        if (result.isSuccess) {
          val renamed = result.getOrNull() ?: File(targetFile.parentFile, newName)
          fileToRename = null
          onFileRenamed(targetFile, renamed)
          notifyMutated()
        } else {
          Toast.makeText(context, result.exceptionOrNull()?.message ?: "Rename failed", Toast.LENGTH_SHORT).show()
        }
      },
    )
  }

  fileToDelete?.let { targetFile ->
    val item = remember(targetFile) { FileManagerEngine.toFileItem(targetFile) }
    DeleteConfirmDialog(
      item = item,
      onDismiss = { fileToDelete = null },
      onConfirm = {
        FileManagerEngine.deleteFileOrDirectory(targetFile)
        fileToDelete = null
        onFileDeleted(targetFile)
        notifyMutated()
      },
    )
  }

  fileForDetails?.let { targetFile ->
    val item = remember(targetFile) { FileManagerEngine.toFileItem(targetFile) }
    FileDetailsDialog(
      item = item,
      onDismiss = { fileForDetails = null },
      onOpen = { FileManagerEngine.openFile(context, targetFile, chooseApp = false) },
      onOpenWith = { FileManagerEngine.openFile(context, targetFile, chooseApp = true) },
      onShare = { CrossToolActions.shareFile(context, targetFile) },
    )
  }

  fileForTextEdit?.let { targetFile ->
    TextEditorDialog(
      file = targetFile,
      onDismiss = { fileForTextEdit = null },
      onSaved = {
        notifyMutated()
      },
    )
  }
}
