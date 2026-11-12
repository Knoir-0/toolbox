package com.example.toolbox.ui.tools

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * Data model representing a file or directory for display and management.
 */
data class FileItem(
  val file: File,
  val name: String,
  val path: String,
  val isDirectory: Boolean,
  val sizeBytes: Long,
  val lastModifiedMs: Long,
  val extension: String,
  val mimeType: String,
  val childCount: Int? = null,
)

/**
 * Available sorting orders for file listing.
 */
enum class FileSortOrder(val displayName: String) {
  NAME_ASC("Name (A to Z)"),
  NAME_DESC("Name (Z to A)"),
  DATE_DESC("Date (Newest first)"),
  DATE_ASC("Date (Oldest first)"),
  SIZE_DESC("Size (Largest first)"),
  SIZE_ASC("Size (Smallest first)"),
  TYPE_ASC("Type"),
}

/**
 * Available grouping options for file presentation.
 */
enum class FileGroupOption(val displayName: String) {
  TYPE("Type"),
  DATE("Date Modified"),
  SIZE("Size"),
  NAME("Alphabetical"),
  NONE("None"),
}

/**
 * Holds a combined group-by and sort-by configuration for a folder or default preference.
 */
data class FolderSortGroupConfig(
  val groupOption: FileGroupOption = FileGroupOption.TYPE,
  val sortOrder: FileSortOrder = FileSortOrder.NAME_ASC,
)

/**
 * Represents a group of files under a common header/section.
 */
data class FileGroup(
  val title: String,
  val items: List<FileItem>,
)

/**
 * View presentation mode: List or Grid.
 */
enum class FileViewMode {
  LIST,
  GRID,
}

/**
 * Represents a storage volume or device (Root / Internal Storage, SD Card, USB OTG, etc.).
 */
data class StorageVolumeInfo(
  val id: String,
  val name: String,
  val file: File,
  val isPrimary: Boolean,
  val isRemovable: Boolean = false,
)

/**
 * Holds device storage space statistics (total, free, used).
 */
data class StorageSpaceInfo(
  val totalBytes: Long,
  val freeBytes: Long,
  val usedBytes: Long,
) {
  val usedFormatted: String get() = FileManagerEngine.formatFileSize(usedBytes)
  val totalFormatted: String get() = FileManagerEngine.formatFileSize(totalBytes)
  val freeFormatted: String get() = FileManagerEngine.formatFileSize(freeBytes)
  val percentageUsed: Int get() = if (totalBytes > 0) ((usedBytes * 100) / totalBytes).toInt() else 0
  val displayText: String get() = "$usedFormatted used of $totalFormatted"
}

/**
 * Full file details for the inspection dialog.
 */
data class FileDetails(
  val name: String,
  val fullPath: String,
  val parentPath: String,
  val isDirectory: Boolean,
  val sizeFormatted: String,
  val sizeBytes: Long,
  val lastModifiedFormatted: String,
  val lastModifiedMs: Long,
  val mimeType: String,
  val extension: String,
  val canRead: Boolean,
  val canWrite: Boolean,
  val isHidden: Boolean,
  val childCount: Int? = null,
  val lineCount: Int? = null,
  val md5Checksum: String? = null,
)

/**
 * Summary information for a collection of selected files.
 */
data class BulkStorageInfo(
  val totalItems: Int,
  val folderCount: Int,
  val fileCount: Int,
  val totalSizeBytes: Long,
  val formattedSize: String,
)

enum class ClipboardOperation {
  COPY, CUT
}

data class ClipboardState(
  val operation: ClipboardOperation,
  val items: List<FileItem>,
)

object FileManagerEngine {

  private val MIME_MAP = mapOf(
    "txt" to "text/plain",
    "md" to "text/markdown",
    "json" to "application/json",
    "xml" to "application/xml",
    "csv" to "text/csv",
    "html" to "text/html",
    "htm" to "text/html",
    "css" to "text/css",
    "js" to "application/javascript",
    "ts" to "application/typescript",
    "kt" to "text/x-kotlin",
    "java" to "text/x-java-source",
    "py" to "text/x-python",
    "c" to "text/x-c",
    "cpp" to "text/x-c++",
    "h" to "text/x-c",
    "sh" to "application/x-sh",
    "log" to "text/plain",
    "properties" to "text/plain",
    "yaml" to "text/yaml",
    "yml" to "text/yaml",
    "toml" to "text/plain",
    "ini" to "text/plain",
    "pdf" to "application/pdf",
    "png" to "image/png",
    "jpg" to "image/jpeg",
    "jpeg" to "image/jpeg",
    "webp" to "image/webp",
    "gif" to "image/gif",
    "bmp" to "image/bmp",
    "svg" to "image/svg+xml",
    "mp3" to "audio/mpeg",
    "wav" to "audio/wav",
    "ogg" to "audio/ogg",
    "m4a" to "audio/mp4",
    "flac" to "audio/flac",
    "aac" to "audio/aac",
    "mp4" to "video/mp4",
    "mkv" to "video/x-matroska",
    "webm" to "video/webm",
    "avi" to "video/x-msvideo",
    "mov" to "video/quicktime",
    "zip" to "application/zip",
    "tar" to "application/x-tar",
    "gz" to "application/gzip",
    "7z" to "application/x-7z-compressed",
    "rar" to "application/vnd.rar",
    "apk" to "application/vnd.android.package-archive",
    "doc" to "application/msword",
    "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "xls" to "application/vnd.ms-excel",
    "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "ppt" to "application/vnd.ms-powerpoint",
    "pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "epub" to "application/epub+zip",
    "rtf" to "application/rtf",
  )

  /**
   * Formats a byte count into a human-readable string.
   */
  fun formatFileSize(bytes: Long): String {
    if (bytes < 0) return "0 B"
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format(Locale.US, "%.1f MB", mb)
    val gb = mb / 1024.0
    return String.format(Locale.US, "%.2f GB", gb)
  }

  /**
   * Formats an epoch millisecond timestamp to a human-readable date.
   */
  fun formatTimestamp(epochMs: Long): String {
    if (epochMs <= 0) return "Unknown"
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(epochMs))
  }

  /**
   * Resolves the MIME type of a file by extension with built-in fallbacks.
   */
  fun getMimeType(file: File): String {
    if (file.isDirectory) return "inode/directory"
    val ext = file.extension.lowercase(Locale.ROOT)
    MIME_MAP[ext]?.let { return it }

    try {
      val mimeMapClass = Class.forName("android.webkit.MimeTypeMap")
      val getSingleton = mimeMapClass.getMethod("getSingleton")
      val instance = getSingleton.invoke(null)
      val getMimeTypeFromExtension =
        mimeMapClass.getMethod("getMimeTypeFromExtension", String::class.java)
      val res = getMimeTypeFromExtension.invoke(instance, ext) as? String
      if (!res.isNullOrBlank()) return res
    } catch (_: Throwable) {
      // Fallback if Android framework is not initialized
    }
    return "application/octet-stream"
  }

  fun isTextFile(file: File): Boolean {
    if (file.isDirectory) return false
    val ext = file.extension.lowercase(Locale.ROOT)
    val mime = getMimeType(file)
    if (mime.startsWith("text/")) return true
    val textExts = setOf(
      "txt", "md", "json", "xml", "csv", "html", "htm", "css",
      "js", "ts", "kt", "java", "py", "c", "cpp", "h", "sh",
      "log", "properties", "yaml", "yml", "toml", "ini", "svg", "sql", "gradle", "kts",
    )
    return ext in textExts
  }

  fun isImageFile(file: File): Boolean {
    if (file.isDirectory) return false
    val ext = file.extension.lowercase(Locale.ROOT)
    return ext in setOf("png", "jpg", "jpeg", "webp", "gif", "bmp", "heic", "heif")
  }

  fun isAudioFile(file: File): Boolean {
    if (file.isDirectory) return false
    val ext = file.extension.lowercase(Locale.ROOT)
    return ext in AudioRecorderEngine.SUPPORTED_AUDIO_EXTENSIONS
  }

  fun isVideoFile(file: File): Boolean {
    if (file.isDirectory) return false
    val ext = file.extension.lowercase(Locale.ROOT)
    return ext in setOf(
      "mp4", "m4v", "mkv", "mov", "qt", "webm", "avi",
      "3gp", "3gpp", "flv", "mpeg", "mpg", "ts", "mts", "m2ts",
    )
  }

  fun isArchiveFile(file: File): Boolean {
    if (file.isDirectory) return false
    val ext = file.extension.lowercase(Locale.ROOT)
    return ext in setOf("zip", "tar", "gz", "7z", "rar", "jar", "apk")
  }

  fun isCodeFile(file: File): Boolean {
    if (file.isDirectory) return false
    val ext = file.extension.lowercase(Locale.ROOT)
    return ext in setOf(
      "json", "xml", "html", "htm", "css", "js", "ts", "kt",
      "java", "py", "c", "cpp", "h", "sh", "sql", "kts", "gradle",
    )
  }

  fun isConvertibleFile(file: File): Boolean {
    return FileConverterEngine.isSupportedFile(file)
  }

  /**
   * Computes total size in bytes of files within [directory].
   * Traverses up to [maxDepth] and [maxFiles] to ensure smooth performance.
   */
  fun computeFolderSize(directory: File, maxDepth: Int = 4, maxFiles: Int = 1000): Long {
    if (!directory.exists() || !directory.isDirectory) return 0L
    var totalBytes = 0L
    var count = 0
    try {
      directory.walkTopDown()
        .maxDepth(maxDepth)
        .onEnter { count < maxFiles }
        .forEach { file ->
          if (!file.isDirectory) {
            totalBytes += file.length()
            count++
            if (count >= maxFiles) return totalBytes
          }
        }
    } catch (_: Exception) {
      // Ignore permission or IO errors
    }
    return totalBytes
  }

  /**
   * Converts a [File] into a UI-friendly [FileItem].
   */
  fun toFileItem(file: File): FileItem {
    val isDir = file.isDirectory
    val childCount = if (isDir) {
      file.listFiles()?.size ?: 0
    } else null
    val sizeBytes = if (isDir) {
      computeFolderSize(file)
    } else {
      file.length()
    }

    return FileItem(
      file = file,
      name = file.name,
      path = file.absolutePath,
      isDirectory = isDir,
      sizeBytes = sizeBytes,
      lastModifiedMs = file.lastModified(),
      extension = file.extension,
      mimeType = getMimeType(file),
      childCount = childCount,
    )
  }

  /**
   * Lists the contents of a directory converted to [FileItem]s.
   */
  fun listDirectoryContents(directory: File): List<FileItem> {
    if (!directory.exists() || !directory.isDirectory) return emptyList()
    val files = directory.listFiles() ?: return emptyList()
    return files.map { toFileItem(it) }
  }

  /**
   * Sorts a list of [FileItem]s according to [sortOrder], maintaining directories first.
   */
  fun sortFiles(items: List<FileItem>, sortOrder: FileSortOrder): List<FileItem> {
    val dirs = items.filter { it.isDirectory }
    val regularFiles = items.filter { !it.isDirectory }

    val comparator: Comparator<FileItem> = when (sortOrder) {
      FileSortOrder.NAME_ASC -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      FileSortOrder.NAME_DESC -> compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name }
      FileSortOrder.DATE_DESC -> compareByDescending<FileItem> { it.lastModifiedMs }.thenBy { it.name }
      FileSortOrder.DATE_ASC -> compareBy<FileItem> { it.lastModifiedMs }.thenBy { it.name }
      FileSortOrder.SIZE_DESC -> compareByDescending<FileItem> { it.sizeBytes }.thenBy { it.name }
      FileSortOrder.SIZE_ASC -> compareBy<FileItem> { it.sizeBytes }.thenBy { it.name }
      FileSortOrder.TYPE_ASC -> compareBy<FileItem> { it.extension.lowercase(Locale.ROOT) }.thenBy { it.name }
    }

    return dirs.sortedWith(comparator) + regularFiles.sortedWith(comparator)
  }

  /**
   * Groups and sorts a list of [FileItem]s according to [groupOption] and [sortOrder].
   * Returns a list of [FileGroup]s, where empty groups are omitted.
   */
  fun groupAndSortFiles(
    items: List<FileItem>,
    groupOption: FileGroupOption,
    sortOrder: FileSortOrder,
  ): List<FileGroup> {
    if (items.isEmpty()) return emptyList()

    val comparator: Comparator<FileItem> = when (sortOrder) {
      FileSortOrder.NAME_ASC -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      FileSortOrder.NAME_DESC -> compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name }
      FileSortOrder.DATE_DESC -> compareByDescending<FileItem> { it.lastModifiedMs }.thenBy { it.name }
      FileSortOrder.DATE_ASC -> compareBy<FileItem> { it.lastModifiedMs }.thenBy { it.name }
      FileSortOrder.SIZE_DESC -> compareByDescending<FileItem> { it.sizeBytes }.thenBy { it.name }
      FileSortOrder.SIZE_ASC -> compareBy<FileItem> { it.sizeBytes }.thenBy { it.name }
      FileSortOrder.TYPE_ASC -> compareBy<FileItem> { it.extension.lowercase(Locale.ROOT) }.thenBy { it.name }
    }

    return when (groupOption) {
      FileGroupOption.NONE -> {
        listOf(FileGroup(title = "", items = sortFiles(items, sortOrder)))
      }
      FileGroupOption.TYPE -> {
        val groups = mutableListOf<FileGroup>()
        val dirs = items.filter { it.isDirectory }.sortedWith(comparator)
        if (dirs.isNotEmpty()) groups.add(FileGroup("Folders", dirs))

        val regularFiles = items.filter { !it.isDirectory }
        val images = regularFiles.filter { isImageFile(it.file) }.sortedWith(comparator)
        if (images.isNotEmpty()) groups.add(FileGroup("Images", images))

        val videos = regularFiles.filter { isVideoFile(it.file) }.sortedWith(comparator)
        if (videos.isNotEmpty()) groups.add(FileGroup("Videos", videos))

        val audio = regularFiles.filter { isAudioFile(it.file) }.sortedWith(comparator)
        if (audio.isNotEmpty()) groups.add(FileGroup("Audio", audio))

        val docs = regularFiles.filter { isTextFile(it.file) }.sortedWith(comparator)
        if (docs.isNotEmpty()) groups.add(FileGroup("Documents", docs))

        val archives = regularFiles.filter { isArchiveFile(it.file) }.sortedWith(comparator)
        if (archives.isNotEmpty()) groups.add(FileGroup("Archives", archives))

        val code = regularFiles.filter { isCodeFile(it.file) }.sortedWith(comparator)
        if (code.isNotEmpty()) groups.add(FileGroup("Code", code))

        val handled = (images + videos + audio + docs + archives + code).toSet()
        val other = regularFiles.filter { it !in handled }.sortedWith(comparator)
        if (other.isNotEmpty()) groups.add(FileGroup("Other", other))

        groups
      }
      FileGroupOption.DATE -> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis
        val yesterdayStart = todayStart - 24L * 60 * 60 * 1000
        val sevenDaysAgo = todayStart - 6L * 24 * 60 * 60 * 1000
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val monthStart = calendar.timeInMillis

        val today = mutableListOf<FileItem>()
        val yesterday = mutableListOf<FileItem>()
        val last7Days = mutableListOf<FileItem>()
        val thisMonth = mutableListOf<FileItem>()
        val older = mutableListOf<FileItem>()

        items.forEach { item ->
          val time = item.lastModifiedMs
          when {
            time >= todayStart -> today.add(item)
            time >= yesterdayStart -> yesterday.add(item)
            time >= sevenDaysAgo -> last7Days.add(item)
            time >= monthStart -> thisMonth.add(item)
            else -> older.add(item)
          }
        }

        val groups = mutableListOf<FileGroup>()
        if (today.isNotEmpty()) groups.add(FileGroup("Today", today.sortedWith(comparator)))
        if (yesterday.isNotEmpty()) groups.add(FileGroup("Yesterday", yesterday.sortedWith(comparator)))
        if (last7Days.isNotEmpty()) groups.add(FileGroup("Last 7 Days", last7Days.sortedWith(comparator)))
        if (thisMonth.isNotEmpty()) groups.add(FileGroup("This Month", thisMonth.sortedWith(comparator)))
        if (older.isNotEmpty()) groups.add(FileGroup("Older", older.sortedWith(comparator)))
        groups
      }
      FileGroupOption.SIZE -> {
        val groups = mutableListOf<FileGroup>()
        val dirs = items.filter { it.isDirectory }.sortedWith(comparator)
        if (dirs.isNotEmpty()) groups.add(FileGroup("Folders", dirs))

        val regularFiles = items.filter { !it.isDirectory }
        val huge = regularFiles.filter { it.sizeBytes >= 100L * 1024 * 1024 }.sortedWith(comparator)
        val large = regularFiles.filter { it.sizeBytes in (10L * 1024 * 1024) until (100L * 1024 * 1024) }.sortedWith(comparator)
        val medium = regularFiles.filter { it.sizeBytes in (1024L * 1024) until (10L * 1024 * 1024) }.sortedWith(comparator)
        val small = regularFiles.filter { it.sizeBytes < 1024L * 1024 }.sortedWith(comparator)

        if (huge.isNotEmpty()) groups.add(FileGroup("Huge (> 100 MB)", huge))
        if (large.isNotEmpty()) groups.add(FileGroup("Large (10 – 100 MB)", large))
        if (medium.isNotEmpty()) groups.add(FileGroup("Medium (1 – 10 MB)", medium))
        if (small.isNotEmpty()) groups.add(FileGroup("Small (< 1 MB)", small))
        groups
      }
      FileGroupOption.NAME -> {
        val groups = mutableListOf<FileGroup>()
        val dirs = items.filter { it.isDirectory }.sortedWith(comparator)
        if (dirs.isNotEmpty()) groups.add(FileGroup("Folders", dirs))

        val regularFiles = items.filter { !it.isDirectory }
        val aToF = mutableListOf<FileItem>()
        val gToL = mutableListOf<FileItem>()
        val mToR = mutableListOf<FileItem>()
        val sToZ = mutableListOf<FileItem>()
        val other = mutableListOf<FileItem>()

        regularFiles.forEach { item ->
          val firstChar = item.name.firstOrNull()?.uppercaseChar() ?: ' '
          when (firstChar) {
            in 'A'..'F' -> aToF.add(item)
            in 'G'..'L' -> gToL.add(item)
            in 'M'..'R' -> mToR.add(item)
            in 'S'..'Z' -> sToZ.add(item)
            else -> other.add(item)
          }
        }

        if (aToF.isNotEmpty()) groups.add(FileGroup("A – F", aToF.sortedWith(comparator)))
        if (gToL.isNotEmpty()) groups.add(FileGroup("G – L", gToL.sortedWith(comparator)))
        if (mToR.isNotEmpty()) groups.add(FileGroup("M – R", mToR.sortedWith(comparator)))
        if (sToZ.isNotEmpty()) groups.add(FileGroup("S – Z", sToZ.sortedWith(comparator)))
        if (other.isNotEmpty()) groups.add(FileGroup("# (Other)", other.sortedWith(comparator)))
        groups
      }
    }
  }

  /**
   * Filters a list of [FileItem]s by a search query string.
   */
  fun filterFiles(items: List<FileItem>, query: String): List<FileItem> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return items
    return items.filter { it.name.contains(trimmed, ignoreCase = true) }
  }

  /**
   * Validates a candidate file or folder name.
   * Returns an error description string, or null if valid.
   */
  fun validateFileName(name: String, parentDir: File? = null): String? {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) {
      return "Name cannot be empty"
    }
    if (trimmed.length > 255) {
      return "Name cannot exceed 255 characters"
    }
    val invalidChars = listOf('/', '\\', ':', '*', '?', '"', '<', '>', '|', '\u0000')
    if (trimmed.any { it in invalidChars }) {
      return "Name contains invalid characters: / \\ : * ? \" < > |"
    }
    if (trimmed == "." || trimmed == "..") {
      return "Reserved file name"
    }
    if (parentDir != null && File(parentDir, trimmed).exists()) {
      return "An item with this name already exists"
    }
    return null
  }

  /**
   * Creates a new directory in [parentDir] with [name].
   */
  fun createFolder(parentDir: File, name: String): Result<File> {
    val error = validateFileName(name, parentDir)
    if (error != null) {
      return Result.failure(IllegalArgumentException(error))
    }
    val newDir = File(parentDir, name.trim())
    return if (newDir.mkdirs() || newDir.isDirectory) {
      Result.success(newDir)
    } else {
      Result.failure(IllegalStateException("Failed to create folder"))
    }
  }

  /**
   * Creates a new file in [parentDir] with [name] and optional [initialContent].
   */
  fun createFile(parentDir: File, name: String, initialContent: String = ""): Result<File> {
    val error = validateFileName(name, parentDir)
    if (error != null) {
      return Result.failure(IllegalArgumentException(error))
    }
    val newFile = File(parentDir, name.trim())
    return try {
      parentDir.mkdirs()
      if (newFile.createNewFile()) {
        if (initialContent.isNotEmpty()) {
          newFile.writeText(initialContent)
        }
        Result.success(newFile)
      } else {
        Result.failure(IllegalStateException("File could not be created"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Renames [file] to [newName].
   */
  fun renameFile(file: File, newName: String): Result<File> {
    val parent = file.parentFile ?: return Result.failure(IllegalArgumentException("File has no parent directory"))
    val error = validateFileName(newName, parent)
    if (error != null) {
      return Result.failure(IllegalArgumentException(error))
    }
    val destination = File(parent, newName.trim())
    return if (file.renameTo(destination)) {
      Result.success(destination)
    } else {
      Result.failure(IllegalStateException("Failed to rename file"))
    }
  }

  /**
   * Duplicates [file] in the same directory with an incremented copy suffix.
   */
  fun duplicateFile(file: File): Result<File> {
    if (!file.exists()) return Result.failure(IllegalArgumentException("File does not exist"))
    val parent = file.parentFile ?: return Result.failure(IllegalArgumentException("File has no parent directory"))

    val baseName = if (file.isDirectory) file.name else file.nameWithoutExtension
    val ext = if (file.isDirectory || file.extension.isEmpty()) "" else ".${file.extension}"

    var counter = 1
    var candidate: File
    do {
      val suffix = if (counter == 1) " (copy)" else " (copy $counter)"
      candidate = File(parent, "$baseName$suffix$ext")
      counter++
    } while (candidate.exists())

    return try {
      if (file.isDirectory) {
        file.copyRecursively(candidate, overwrite = false)
      } else {
        file.copyTo(candidate, overwrite = false)
      }
      Result.success(candidate)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Recursively deletes a file or directory.
   */
  fun deleteFileOrDirectory(file: File): Boolean {
    if (!file.exists()) return true
    return file.deleteRecursively()
  }

  /**
   * Recursively deletes multiple files or directories. Returns count of successfully deleted items.
   */
  fun deleteMultipleFiles(files: List<File>): Int {
    var count = 0
    for (f in files) {
      if (deleteFileOrDirectory(f)) {
        count++
      }
    }
    return count
  }

  /**
   * Copies multiple files/folders into [destinationDir].
   */
  fun copyMultipleFiles(sources: List<File>, destinationDir: File): Int {
    if (!destinationDir.exists() || !destinationDir.isDirectory) return 0
    var count = 0
    for (src in sources) {
      try {
        var dest = File(destinationDir, src.name)
        if (dest.exists()) {
          val base = if (src.isDirectory) src.name else src.nameWithoutExtension
          val ext = if (src.isDirectory || src.extension.isEmpty()) "" else ".${src.extension}"
          var i = 1
          do {
            dest = File(destinationDir, "$base (copy $i)$ext")
            i++
          } while (dest.exists())
        }
        if (src.isDirectory) {
          src.copyRecursively(dest, overwrite = false)
        } else {
          src.copyTo(dest, overwrite = false)
        }
        count++
      } catch (_: Exception) {}
    }
    return count
  }

  /**
   * Moves multiple files/folders into [destinationDir].
   */
  fun moveMultipleFiles(sources: List<File>, destinationDir: File): Int {
    if (!destinationDir.exists() || !destinationDir.isDirectory) return 0
    var count = 0
    for (src in sources) {
      try {
        var dest = File(destinationDir, src.name)
        if (dest.exists()) {
          val base = if (src.isDirectory) src.name else src.nameWithoutExtension
          val ext = if (src.isDirectory || src.extension.isEmpty()) "" else ".${src.extension}"
          var i = 1
          do {
            dest = File(destinationDir, "$base (moved $i)$ext")
            i++
          } while (dest.exists())
        }
        if (src.renameTo(dest)) {
          count++
        } else {
          if (src.isDirectory) {
            src.copyRecursively(dest, overwrite = true)
          } else {
            src.copyTo(dest, overwrite = true)
          }
          src.deleteRecursively()
          count++
        }
      } catch (_: Exception) {}
    }
    return count
  }

  /**
   * Gathers aggregated storage and type information for a collection of items.
   */
  fun getBulkStorageInfo(items: List<FileItem>): BulkStorageInfo {
    val folderCount = items.count { it.isDirectory }
    val fileCount = items.count { !it.isDirectory }
    val totalBytes = items.sumOf { it.sizeBytes }
    return BulkStorageInfo(
      totalItems = items.size,
      folderCount = folderCount,
      fileCount = fileCount,
      totalSizeBytes = totalBytes,
      formattedSize = formatFileSize(totalBytes),
    )
  }

  /**
   * Reads text content from [file] up to [maxBytes] to avoid OOM.
   */
  fun readTextContent(file: File, maxBytes: Long = 2 * 1024 * 1024): String? {
    if (!file.exists() || file.isDirectory) return null
    if (file.length() > maxBytes) {
      // Read first chunk
      val bytes = ByteArray(maxBytes.toInt())
      FileInputStream(file).use { input ->
        val read = input.read(bytes)
        return String(bytes, 0, read) + "\n\n... [Truncated: File exceeds 2MB preview limit]"
      }
    }
    return try {
      file.readText()
    } catch (_: Exception) {
      null
    }
  }

  /**
   * Writes text content into [file].
   */
  fun writeTextContent(file: File, content: String): Boolean {
    return try {
      file.writeText(content)
      true
    } catch (_: Exception) {
      false
    }
  }

  /**
   * Computes the MD5 hex string of [file], or null on error.
   */
  fun calculateMd5(file: File): String? {
    if (!file.exists() || file.isDirectory) return null
    return try {
      val digest = MessageDigest.getInstance("MD5")
      FileInputStream(file).use { input ->
        val buffer = ByteArray(8192)
        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
          digest.update(buffer, 0, read)
        }
      }
      digest.digest().joinToString("") { "%02x".format(it) }
    } catch (_: Exception) {
      null
    }
  }

  /**
   * Retrieves full details for a file item.
   */
  fun getFileDetails(file: File): FileDetails {
    val isDir = file.isDirectory
    val childCount = if (isDir) file.listFiles()?.size ?: 0 else null
    val sizeBytes = if (isDir) computeFolderSize(file) else file.length()
    val lineCount = if (!isDir && isTextFile(file) && file.length() < 2 * 1024 * 1024) {
      try {
        file.useLines { it.count() }
      } catch (_: Exception) {
        null
      }
    } else null

    val md5 = if (!isDir && file.length() < 50 * 1024 * 1024) {
      calculateMd5(file)
    } else null

    return FileDetails(
      name = file.name,
      fullPath = file.absolutePath,
      parentPath = file.parentFile?.absolutePath ?: "/",
      isDirectory = isDir,
      sizeFormatted = if (isDir) "$childCount items • ${formatFileSize(sizeBytes)}" else formatFileSize(sizeBytes),
      sizeBytes = sizeBytes,
      lastModifiedFormatted = formatTimestamp(file.lastModified()),
      lastModifiedMs = file.lastModified(),
      mimeType = getMimeType(file),
      extension = file.extension,
      canRead = file.canRead(),
      canWrite = file.canWrite(),
      isHidden = file.isHidden,
      childCount = childCount,
      lineCount = lineCount,
      md5Checksum = md5,
    )
  }

  /**
   * Calculates total size and item count in [directory].
   * Runs in [Dispatchers.IO] and supports cooperative cancellation when the coroutine is cancelled.
   */
  suspend fun getStorageSummary(directory: File): Pair<Long, Int> = withContext(Dispatchers.IO) {
    if (!directory.exists() || !directory.isDirectory) return@withContext 0L to 0
    var totalBytes = 0L
    var count = 0
    try {
      directory.walkTopDown()
        .onEnter { dir ->
          coroutineContext.ensureActive()
          dir.canRead()
        }
        .forEach { file ->
          coroutineContext.ensureActive()
          if (!file.isDirectory) {
            totalBytes += file.length()
            count++
          }
        }
    } catch (e: CancellationException) {
      throw e
    } catch (_: Exception) {
      // Ignore permission or IO errors during traversal
    }
    totalBytes to count
  }

  /**
   * Synchronous / blocking version of [getStorageSummary] for testing or non-coroutine callers.
   */
  fun getStorageSummaryBlocking(directory: File): Pair<Long, Int> {
    if (!directory.exists() || !directory.isDirectory) return 0L to 0
    var totalBytes = 0L
    var count = 0
    try {
      directory.walkTopDown()
        .onEnter { dir -> dir.canRead() }
        .forEach { file ->
          if (!file.isDirectory) {
            totalBytes += file.length()
            count++
          }
        }
    } catch (_: Exception) {
      // Ignore permission or IO errors during traversal
    }
    return totalBytes to count
  }

  /**
   * Resolves the primary root directory for user storage (storage/emulated/0).
   */
  fun getUserRootDirectory(): File {
    val emulatedZero = File("/storage/emulated/0")
    if (emulatedZero.exists()) return emulatedZero
    return try {
      val ext = android.os.Environment.getExternalStorageDirectory()
      if (ext != null && ext.exists()) ext else emulatedZero
    } catch (_: Throwable) {
      emulatedZero
    }
  }

  /**
   * Returns true if [file] is the same directory as [rootDir] or a descendant inside [rootDir].
   */
  fun isSameOrDescendant(file: File, rootDir: File): Boolean {
    val filePath = file.absoluteFile.normalize().path
    val rootPath = rootDir.absoluteFile.normalize().path
    if (filePath == rootPath) return true
    val prefix = if (rootPath.endsWith(File.separator)) rootPath else rootPath + File.separator
    return filePath.startsWith(prefix)
  }

  /**
   * Returns true if [currentDirectory] is at (or above) [rootDirectory], meaning it is at the root
   * and cannot navigate up a folder.
   */
  fun isAtRootDirectory(currentDirectory: File, rootDirectory: File): Boolean {
    val currentNormalized = currentDirectory.absoluteFile.normalize()
    val rootNormalized = rootDirectory.absoluteFile.normalize()
    if (currentNormalized.path == rootNormalized.path) return true
    if (currentNormalized.parentFile == null) return true
    val currentPrefix = if (currentNormalized.path.endsWith(File.separator)) {
      currentNormalized.path
    } else {
      currentNormalized.path + File.separator
    }
    if (rootNormalized.path.startsWith(currentPrefix)) return true
    return false
  }

  /**
   * Returns true if the file manager can navigate up a folder from [currentDirectory] towards [rootDirectory].
   */
  fun canNavigateUp(currentDirectory: File, rootDirectory: File): Boolean {
    return !isAtRootDirectory(currentDirectory, rootDirectory)
  }

  /**
   * Returns the parent directory to navigate up to from [currentDirectory], or null if already at [rootDirectory].
   */
  fun navigateUpDirectory(currentDirectory: File, rootDirectory: File): File? {
    if (!canNavigateUp(currentDirectory, rootDirectory)) return null
    return currentDirectory.absoluteFile.normalize().parentFile
  }

  /**
   * Discovers all available storage volumes on the device:
   * 1. Primary root folder (storage/emulated/0)
   * 2. Connected external / removable storage devices (SD cards, USB OTG drives).
   */
  fun getStorageVolumes(context: android.content.Context?): List<StorageVolumeInfo> {
    val primaryRoot = getUserRootDirectory()
    val volumes = mutableListOf(
      StorageVolumeInfo(
        id = "primary_root",
        name = "Home",
        file = primaryRoot,
        isPrimary = true,
        isRemovable = false,
      )
    )

    if (context == null) return volumes

    try {
      // 1. Android StorageManager StorageVolumes (API 24+)
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        val storageManager = context.getSystemService(android.content.Context.STORAGE_SERVICE) as? android.os.storage.StorageManager
        val sysVolumes = storageManager?.storageVolumes ?: emptyList()
        for (vol in sysVolumes) {
          if (vol.isPrimary) continue
          val desc = vol.getDescription(context)
          val name = if (!desc.isNullOrBlank()) desc else "SD Card"
          val dir: File? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            vol.directory
          } else {
            try {
              val getPath = vol.javaClass.getMethod("getPath")
              (getPath.invoke(vol) as? String)?.let { File(it) }
            } catch (_: Throwable) {
              null
            }
          }
          if (dir != null && dir.exists()) {
            if (volumes.none { it.file.absolutePath == dir.absolutePath }) {
              volumes.add(
                StorageVolumeInfo(
                  id = dir.absolutePath,
                  name = name,
                  file = dir,
                  isPrimary = false,
                  isRemovable = true,
                )
              )
            }
          }
        }
      }

      // 2. Secondary directories from getExternalFilesDirs (SD Cards / Removable USB)
      val extDirs = context.getExternalFilesDirs(null)
      if (extDirs != null) {
        for (i in 1 until extDirs.size) {
          val extDir = extDirs[i] ?: continue
          if (!extDir.exists()) continue
          val segments = extDir.absolutePath.split("/").filter { it.isNotEmpty() }
          if (segments.size >= 2 && segments[0] == "storage" && segments[1] != "emulated" && segments[1] != "self") {
            val volRoot = File("/storage/${segments[1]}")
            if (volRoot.exists() && volumes.none { it.file.absolutePath == volRoot.absolutePath }) {
              volumes.add(
                StorageVolumeInfo(
                  id = volRoot.absolutePath,
                  name = "SD Card (${segments[1]})",
                  file = volRoot,
                  isPrimary = false,
                  isRemovable = true,
                )
              )
            }
          }
        }
      }

      // 3. Scan /storage directory for additional mounted volumes (USB drives, etc.)
      val storageDir = File("/storage")
      if (storageDir.exists() && storageDir.isDirectory) {
        storageDir.listFiles()?.forEach { file ->
          if (file.isDirectory && file.name != "emulated" && file.name != "self" && file.name != "knox") {
            if (volumes.none { it.file.absolutePath == file.absolutePath }) {
              val deviceName = if (file.name.matches(Regex("[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}"))) {
                "SD Card (${file.name})"
              } else {
                file.name
              }
              volumes.add(
                StorageVolumeInfo(
                  id = file.absolutePath,
                  name = deviceName,
                  file = file,
                  isPrimary = false,
                  isRemovable = true,
                )
              )
            }
          }
        }
      }
    } catch (_: Throwable) {
      // In tests or headless environments, return the primary volume
    }

    return volumes
  }

  /**
   * Checks whether the app has storage access permissions for browsing external directories.
   */
  fun hasStoragePermission(context: android.content.Context): Boolean {
    return try {
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        android.os.Environment.isExternalStorageManager()
      } else {
        androidx.core.content.ContextCompat.checkSelfPermission(
          context,
          android.Manifest.permission.READ_EXTERNAL_STORAGE,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
      }
    } catch (_: Throwable) {
      true
    }
  }

  /**
   * Retrieves storage space information (total, free, used bytes) for the given file or volume root.
   */
  fun getDeviceStorageSpace(file: File?): StorageSpaceInfo? {
    val target = file ?: getUserRootDirectory()
    return try {
      val stat = android.os.StatFs(target.absolutePath)
      val total = stat.totalBytes
      val free = stat.availableBytes
      val used = (total - free).coerceAtLeast(0L)
      if (total > 0) {
        StorageSpaceInfo(totalBytes = total, freeBytes = free, usedBytes = used)
      } else {
        null
      }
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Resolves the MIME type to use when launching an `ACTION_VIEW` intent for [file].
   * When [chooseApp] is true and the resolved MIME type is generic (`application/octet-stream`),
   * returns a wildcard MIME type so that the system app chooser presents all available handler apps.
   */
  fun resolveOpenMimeType(file: File, chooseApp: Boolean = false): String {
    val mime = getMimeType(file)
    return if (chooseApp && mime == "application/octet-stream") "*/*" else mime
  }

  /**
   * Opens [file] using an Android `ACTION_VIEW` intent backed by `FileProvider`.
   * When [chooseApp] is `false`, opens the file directly with the default app (or system resolver).
   * When [chooseApp] is `true`, wraps the intent in `Intent.createChooser` so the user can choose
   * a specific app to open the file with.
   */
  fun openFile(
    context: android.content.Context,
    file: File,
    chooseApp: Boolean = false,
  ): Boolean {
    if (file.isDirectory || !file.exists()) return false
    return try {
      val contentUri = androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
      )
      val mime = resolveOpenMimeType(file, chooseApp)
      val baseFlags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
        if (context !is android.app.Activity) android.content.Intent.FLAG_ACTIVITY_NEW_TASK else 0

      val viewIntent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
        setDataAndType(contentUri, mime)
        addFlags(baseFlags)
      }

      if (chooseApp) {
        val chooserIntent = android.content.Intent.createChooser(
          viewIntent,
          "Open ${file.name} with",
        ).apply {
          if (context !is android.app.Activity) {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
          }
        }
        context.startActivity(chooserIntent)
        true
      } else {
        try {
          context.startActivity(viewIntent)
          true
        } catch (_: android.content.ActivityNotFoundException) {
          val fallbackIntent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "*/*")
            addFlags(baseFlags)
          }
          val chooserIntent = android.content.Intent.createChooser(
            fallbackIntent,
            "Open ${file.name} with",
          ).apply {
            if (context !is android.app.Activity) {
              addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
          }
          context.startActivity(chooserIntent)
          true
        }
      }
    } catch (e: Exception) {
      android.widget.Toast.makeText(
        context,
        "No app found to open ${file.name}",
        android.widget.Toast.LENGTH_SHORT,
      ).show()
      false
    }
  }

  private const val FILES_PREFS_NAME = "files_tool_preferences"
  private const val PREF_KEY_DEFAULT_SORT_GROUP = "default_sort_group"
  private const val PREF_PREFIX_FOLDER_SORT_GROUP = "folder_sort_group_"

  private var inMemoryDefaultSortGroupConfig = FolderSortGroupConfig()
  private val inMemoryFolderSortGroupMap = mutableMapOf<String, FolderSortGroupConfig>()

  /**
   * Serializes a [FolderSortGroupConfig] into `"GROUP:SORT"` format.
   */
  fun serializeSortGroupConfig(config: FolderSortGroupConfig): String {
    return "${config.groupOption.name}:${config.sortOrder.name}"
  }

  /**
   * Parses a `"GROUP:SORT"` string into a [FolderSortGroupConfig], or returns null if invalid.
   */
  fun parseSortGroupConfig(raw: String?): FolderSortGroupConfig? {
    if (raw.isNullOrBlank()) return null
    val parts = raw.split(":")
    if (parts.size != 2) return null
    return try {
      val group = FileGroupOption.valueOf(parts[0].trim())
      val sort = FileSortOrder.valueOf(parts[1].trim())
      FolderSortGroupConfig(groupOption = group, sortOrder = sort)
    } catch (_: IllegalArgumentException) {
      null
    }
  }

  /**
   * Returns the global/default [FolderSortGroupConfig] used for folders without a custom override.
   */
  fun getDefaultSortGroupConfig(context: android.content.Context? = null): FolderSortGroupConfig {
    if (context == null) return inMemoryDefaultSortGroupConfig
    return try {
      val prefs = context.getSharedPreferences(FILES_PREFS_NAME, android.content.Context.MODE_PRIVATE)
      parseSortGroupConfig(prefs.getString(PREF_KEY_DEFAULT_SORT_GROUP, null))
        ?: inMemoryDefaultSortGroupConfig
    } catch (_: Throwable) {
      inMemoryDefaultSortGroupConfig
    }
  }

  /**
   * Persists the global/default [FolderSortGroupConfig] used for folders without a custom override.
   */
  fun setDefaultSortGroupConfig(
    context: android.content.Context? = null,
    config: FolderSortGroupConfig,
  ) {
    inMemoryDefaultSortGroupConfig = config
    if (context == null) return
    try {
      val prefs = context.getSharedPreferences(FILES_PREFS_NAME, android.content.Context.MODE_PRIVATE)
      prefs.edit().putString(PREF_KEY_DEFAULT_SORT_GROUP, serializeSortGroupConfig(config)).apply()
    } catch (_: Throwable) {}
  }

  /**
   * Returns the saved [FolderSortGroupConfig] for [directory], or null if none is saved for that folder.
   */
  fun getFolderSortGroupConfig(
    context: android.content.Context? = null,
    directory: File,
  ): FolderSortGroupConfig? {
    val pathKey = directory.absolutePath
    if (context == null) return inMemoryFolderSortGroupMap[pathKey]
    return try {
      val prefs = context.getSharedPreferences(FILES_PREFS_NAME, android.content.Context.MODE_PRIVATE)
      parseSortGroupConfig(prefs.getString(PREF_PREFIX_FOLDER_SORT_GROUP + pathKey, null))
        ?: inMemoryFolderSortGroupMap[pathKey]
    } catch (_: Throwable) {
      inMemoryFolderSortGroupMap[pathKey]
    }
  }

  /**
   * Returns whether [directory] has a saved folder-specific sort/group configuration.
   */
  fun hasFolderSortGroupConfig(
    context: android.content.Context? = null,
    directory: File,
  ): Boolean {
    return getFolderSortGroupConfig(context, directory) != null
  }

  /**
   * Saves a folder-specific [FolderSortGroupConfig] for [directory].
   */
  fun setFolderSortGroupConfig(
    context: android.content.Context? = null,
    directory: File,
    config: FolderSortGroupConfig,
  ) {
    val pathKey = directory.absolutePath
    inMemoryFolderSortGroupMap[pathKey] = config
    if (context == null) return
    try {
      val prefs = context.getSharedPreferences(FILES_PREFS_NAME, android.content.Context.MODE_PRIVATE)
      prefs.edit()
        .putString(PREF_PREFIX_FOLDER_SORT_GROUP + pathKey, serializeSortGroupConfig(config))
        .apply()
    } catch (_: Throwable) {}
  }

  /**
   * Removes any saved folder-specific [FolderSortGroupConfig] for [directory].
   */
  fun removeFolderSortGroupConfig(
    context: android.content.Context? = null,
    directory: File,
  ) {
    val pathKey = directory.absolutePath
    inMemoryFolderSortGroupMap.remove(pathKey)
    if (context == null) return
    try {
      val prefs = context.getSharedPreferences(FILES_PREFS_NAME, android.content.Context.MODE_PRIVATE)
      prefs.edit().remove(PREF_PREFIX_FOLDER_SORT_GROUP + pathKey).apply()
    } catch (_: Throwable) {}
  }

  /**
   * Resolves the active [FolderSortGroupConfig] for [directory] along with a boolean flag
   * indicating whether the configuration is specifically saved for [directory].
   */
  fun resolveSortGroupConfig(
    context: android.content.Context? = null,
    directory: File,
  ): Pair<FolderSortGroupConfig, Boolean> {
    val folderSpecific = getFolderSortGroupConfig(context, directory)
    return if (folderSpecific != null) {
      folderSpecific to true
    } else {
      getDefaultSortGroupConfig(context) to false
    }
  }

  /**
   * Clears all in-memory and persisted folder sort/group configurations and resets the default.
   */
  fun clearAllSortGroupConfigs(context: android.content.Context? = null) {
    inMemoryDefaultSortGroupConfig = FolderSortGroupConfig()
    inMemoryFolderSortGroupMap.clear()
    if (context == null) return
    try {
      val prefs = context.getSharedPreferences(FILES_PREFS_NAME, android.content.Context.MODE_PRIVATE)
      prefs.edit().clear().apply()
    } catch (_: Throwable) {}
  }

  /**
   * Seeds default sample files and directories in [baseDir] if it is empty.
   */
  fun populateDefaultSampleFiles(baseDir: File) {
    if (!baseDir.exists()) baseDir.mkdirs()
    val existing = baseDir.listFiles()
    if (existing != null && existing.isNotEmpty()) return

    val welcomeFile = File(baseDir, "Welcome.txt")
    welcomeFile.writeText(
      """
      Welcome to the Toolbox Files Manager!
      =====================================
      
      Here you can:
      - Browse, create, and organize folders
      - Create and edit text and markdown files
      - Preview images and photos
      - Inspect file metadata and MD5 checksums
      - Copy, rename, share, and delete files
      - Import files from your device storage
      
      Enjoy exploring!
      """.trimIndent()
    )

    val docsDir = File(baseDir, "Documents")
    docsDir.mkdirs()
    File(docsDir, "QuickNotes.md").writeText(
      """
      # Quick Notes
      
      - [x] Integrate Files tool into Toolbox
      - [x] Support text viewing and editing
      - [x] Add image preview and MD5 calculation
      - [ ] Add more cool tools to Toolbox!
      """.trimIndent()
    )
    File(docsDir, "Config.json").writeText(
      """
      {
        "appName": "Toolbox",
        "version": "1.0",
        "features": ["Calculator", "Unit Converter", "Dice Roller", "Files"]
      }
      """.trimIndent()
    )

    val notesDir = File(baseDir, "Notes")
    notesDir.mkdirs()
    File(notesDir, "ShoppingList.txt").writeText(
      """
      - Apples
      - Coffee beans
      - Notebook
      - USB-C cable
      """.trimIndent()
    )
  }
}
