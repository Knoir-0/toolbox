package com.example.toolbox.ui.tools

import android.content.Context
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.Calendar
import java.util.Locale
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Supported archive formats for viewing, extracting, and creating archives.
 */
enum class ArchiveFormat(
  val displayName: String,
  val extension: String,
  val mimeType: String,
  val aliases: Set<String>,
  val supportsMultiEntryWrite: Boolean,
) {
  ZIP(
    displayName = "ZIP",
    extension = "zip",
    mimeType = "application/zip",
    aliases = setOf("zip", "jar", "apk", "cbz"),
    supportsMultiEntryWrite = true,
  ),
  TAR(
    displayName = "TAR",
    extension = "tar",
    mimeType = "application/x-tar",
    aliases = setOf("tar"),
    supportsMultiEntryWrite = true,
  ),
  TAR_GZ(
    displayName = "TAR.GZ",
    extension = "tar.gz",
    mimeType = "application/gzip",
    aliases = setOf("tgz", "tar.gz"),
    supportsMultiEntryWrite = true,
  ),
  GZIP(
    displayName = "GZIP",
    extension = "gz",
    mimeType = "application/gzip",
    aliases = setOf("gz"),
    supportsMultiEntryWrite = false,
  ),
}

/**
 * Compression level options when creating or modifying ZIP archives.
 */
enum class ArchiveCompressionLevel(
  val displayName: String,
  val shortLabel: String,
  val zipLevel: Int,
) {
  STORE(displayName = "Store (No compression)", shortLabel = "Store", zipLevel = Deflater.NO_COMPRESSION),
  FAST(displayName = "Fast compression", shortLabel = "Fast", zipLevel = Deflater.BEST_SPEED),
  BALANCED(displayName = "Balanced (Default)", shortLabel = "Balanced", zipLevel = Deflater.DEFAULT_COMPRESSION),
  MAXIMUM(displayName = "Maximum compression", shortLabel = "Max", zipLevel = Deflater.BEST_COMPRESSION),
}

/**
 * Represents a single file or virtual folder entry inside an archive.
 */
data class ArchiveEntryItem(
  val name: String,
  val archivePath: String,
  val parentArchivePath: String,
  val isDirectory: Boolean,
  val sizeBytes: Long,
  val compressedSizeBytes: Long,
  val lastModifiedMs: Long,
  val crc32: Long = 0L,
  val extension: String = "",
  val mimeType: String = "application/octet-stream",
  val childCount: Int? = null,
) {
  val compressionRatioPercent: Int
    get() = if (sizeBytes > 0L && compressedSizeBytes in 1..sizeBytes) {
      (((sizeBytes - compressedSizeBytes) * 100L) / sizeBytes).toInt().coerceIn(0, 99)
    } else {
      0
    }
}

/**
 * Represents a group of [ArchiveEntryItem]s when grouped by Type, Date, Size, or Name.
 */
data class ArchiveEntryGroup(
  val title: String,
  val items: List<ArchiveEntryItem>,
)

/**
 * Metadata and complete entry hierarchy for an opened archive file.
 */
data class ArchiveInfo(
  val file: File,
  val format: ArchiveFormat,
  val allEntries: List<ArchiveEntryItem>,
  val totalFileCount: Int,
  val totalFolderCount: Int,
  val totalUncompressedBytes: Long,
  val totalCompressedBytes: Long,
  val archiveFileSizeBytes: Long,
) {
  val overallCompressionPercent: Int
    get() {
      val effectiveCompressed = if (totalCompressedBytes > 0L) totalCompressedBytes else archiveFileSizeBytes
      return if (totalUncompressedBytes > 0L && effectiveCompressed in 1..totalUncompressedBytes) {
        (((totalUncompressedBytes - effectiveCompressed) * 100L) / totalUncompressedBytes).toInt().coerceIn(0, 99)
      } else {
        0
      }
    }
}

/**
 * Summary information for a multi-selection of archive entries.
 */
data class ArchiveBulkInfo(
  val totalItems: Int,
  val folderCount: Int,
  val fileCount: Int,
  val totalUncompressedBytes: Long,
  val totalCompressedBytes: Long,
  val formattedUncompressedSize: String,
  val formattedCompressedSize: String,
)

/**
 * Result of an archive creation, extraction, or modification operation.
 */
data class ArchiveOperationResult(
  val success: Boolean,
  val outputFile: File? = null,
  val extractedFilesCount: Int = 0,
  val totalBytesProcessed: Long = 0L,
  val message: String,
)

/**
 * Core engine for reading, navigating, previewing, extracting, and creating/modifying archives.
 */
object ArchiveEngine {

  private data class RawEntryPayload(
    val archivePath: String,
    val isDirectory: Boolean,
    val bytes: ByteArray,
    val lastModifiedMs: Long,
    val crc32: Long = 0L,
  )

  /**
   * Detects the [ArchiveFormat] of a file based on its name/extension.
   */
  fun detectFormat(file: File): ArchiveFormat? {
    if (file.isDirectory) return null
    return detectFormatFromName(file.name)
  }

  /**
   * Detects the [ArchiveFormat] from a filename string, properly handling compound extensions
   * such as `.tar.gz`.
   */
  fun detectFormatFromName(fileName: String): ArchiveFormat? {
    val lower = fileName.lowercase(Locale.ROOT).trim()
    if (lower.endsWith(".tar.gz") || lower.endsWith(".tgz")) {
      return ArchiveFormat.TAR_GZ
    }
    val ext = lower.substringAfterLast('.', "")
    if (ext.isEmpty()) return null
    return ArchiveFormat.entries.firstOrNull { ext in it.aliases }
  }

  /**
   * Returns true if the given file is a supported archive that can be opened in the Archive tool.
   */
  fun isSupportedArchive(file: File): Boolean {
    return !file.isDirectory && detectFormat(file) != null
  }

  /**
   * Normalizes an internal archive path: converts backslashes to forward slashes,
   * strips leading/trailing slashes, collapses duplicate slashes, and removes `.` or `..` segments.
   */
  fun normalizeArchivePath(rawPath: String): String {
    val unified = rawPath.replace('\\', '/').trim()
    if (unified.isEmpty()) return ""
    val segments = unified.split('/')
      .map { it.trim() }
      .filter { it.isNotEmpty() && it != "." && it != ".." }
    return segments.joinToString("/")
  }

  /**
   * Returns the parent virtual path of a normalized archive path, or `""` if at root.
   */
  fun parentArchivePathOf(normalizedPath: String): String {
    val clean = normalizeArchivePath(normalizedPath)
    val lastSlash = clean.lastIndexOf('/')
    return if (lastSlash <= 0) "" else clean.substring(0, lastSlash)
  }

  /**
   * Returns the leaf entry name of a normalized archive path.
   */
  fun leafNameOf(normalizedPath: String): String {
    val clean = normalizeArchivePath(normalizedPath)
    val lastSlash = clean.lastIndexOf('/')
    return if (lastSlash < 0) clean else clean.substring(lastSlash + 1)
  }

  /**
   * Verifies that extracting [entryPath] into [destinationDir] does not escape [destinationDir] (Zip-Slip protection).
   */
  fun isSafeExtractTarget(destinationDir: File, entryPath: String): Boolean {
    val rawSegments = entryPath.replace('\\', '/').split('/')
    if (rawSegments.any { it == ".." }) return false
    val normalized = normalizeArchivePath(entryPath)
    if (normalized.isEmpty()) return false
    return try {
      val destCanonical = destinationDir.canonicalPath
      val targetFile = File(destinationDir, normalized)
      val targetCanonical = targetFile.canonicalPath
      targetCanonical == destCanonical || targetCanonical.startsWith("$destCanonical${File.separator}")
    } catch (_: Exception) {
      false
    }
  }

  /**
   * Reads an archive file and constructs its full [ArchiveInfo] including synthesized intermediate
   * virtual directories and aggregated folder sizes/counts.
   */
  fun readArchive(archiveFile: File): Result<ArchiveInfo> {
    if (!archiveFile.exists() || !archiveFile.isFile) {
      return Result.failure(IllegalArgumentException("Archive file does not exist: ${archiveFile.name}"))
    }
    val format = detectFormat(archiveFile)
      ?: return Result.failure(IllegalArgumentException("Unsupported archive format: ${archiveFile.name}"))

    return try {
      val rawEntries = when (format) {
        ArchiveFormat.ZIP -> readZipMetadataEntries(archiveFile)
        ArchiveFormat.TAR -> {
          BufferedInputStream(FileInputStream(archiveFile)).use { bis ->
            readTarMetadataEntries(bis)
          }
        }
        ArchiveFormat.TAR_GZ -> {
          GZIPInputStream(BufferedInputStream(FileInputStream(archiveFile))).use { gzis ->
            readTarMetadataEntries(gzis)
          }
        }
        ArchiveFormat.GZIP -> readGzipMetadataEntries(archiveFile)
      }

      val completeHierarchy = buildCompleteEntryHierarchy(rawEntries, archiveFile.lastModified())
      val files = completeHierarchy.filter { !it.isDirectory }
      val folders = completeHierarchy.filter { it.isDirectory }
      val totalUncompressed = files.sumOf { it.sizeBytes.coerceAtLeast(0L) }
      val totalCompressed = files.sumOf { it.compressedSizeBytes.coerceAtLeast(0L) }
        .takeIf { it > 0L } ?: archiveFile.length()

      Result.success(
        ArchiveInfo(
          file = archiveFile,
          format = format,
          allEntries = completeHierarchy,
          totalFileCount = files.size,
          totalFolderCount = folders.size,
          totalUncompressedBytes = totalUncompressed,
          totalCompressedBytes = totalCompressed,
          archiveFileSizeBytes = archiveFile.length(),
        )
      )
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Lists direct children of [virtualDirPath] (`""` for archive root) inside [archiveInfo].
   */
  fun listVirtualDirectory(archiveInfo: ArchiveInfo, virtualDirPath: String = ""): List<ArchiveEntryItem> {
    val cleanDir = normalizeArchivePath(virtualDirPath)
    return archiveInfo.allEntries.filter { it.parentArchivePath == cleanDir }
  }

  /**
   * Computes `(totalUncompressedBytes, itemCount)` for all files under [virtualDirPath].
   */
  fun getVirtualDirectorySummary(archiveInfo: ArchiveInfo, virtualDirPath: String = ""): Pair<Long, Int> {
    val cleanDir = normalizeArchivePath(virtualDirPath)
    val directChildren = listVirtualDirectory(archiveInfo, cleanDir)
    val descendantFiles = if (cleanDir.isEmpty()) {
      archiveInfo.allEntries.filter { !it.isDirectory }
    } else {
      val prefix = "$cleanDir/"
      archiveInfo.allEntries.filter { !it.isDirectory && it.archivePath.startsWith(prefix) }
    }
    return descendantFiles.sumOf { it.sizeBytes } to directChildren.size
  }

  /**
   * Returns breadcrumb ancestors from the archive root `("", archiveFileName)` down to [virtualDirPath].
   */
  fun getBreadcrumbAncestors(archiveFileName: String, virtualDirPath: String): List<Pair<String, String>> {
    val result = mutableListOf(archiveFileName to "")
    val clean = normalizeArchivePath(virtualDirPath)
    if (clean.isEmpty()) return result
    val parts = clean.split('/')
    var acc = ""
    for (part in parts) {
      acc = if (acc.isEmpty()) part else "$acc/$part"
      result.add(part to acc)
    }
    return result
  }

  /**
   * Returns true if [virtualDirPath] is inside a subfolder and can navigate up toward archive root `""`.
   */
  fun canNavigateUp(virtualDirPath: String): Boolean {
    return normalizeArchivePath(virtualDirPath).isNotEmpty()
  }

  /**
   * Returns the parent virtual folder path, or `null` if already at archive root `""`.
   */
  fun navigateUpVirtualDirectory(virtualDirPath: String): String? {
    val clean = normalizeArchivePath(virtualDirPath)
    if (clean.isEmpty()) return null
    return parentArchivePathOf(clean)
  }

  /**
   * Filters archive entries by search query (case-insensitive substring match on entry name or path).
   */
  fun filterEntries(entries: List<ArchiveEntryItem>, query: String): List<ArchiveEntryItem> {
    if (query.isBlank()) return entries
    val trimmed = query.trim().lowercase(Locale.ROOT)
    return entries.filter {
      it.name.lowercase(Locale.ROOT).contains(trimmed) ||
        it.archivePath.lowercase(Locale.ROOT).contains(trimmed)
    }
  }

  /**
   * Searches across all entries in [archiveInfo] under [currentVirtualDir] when a search query is active,
   * or returns direct children of [currentVirtualDir] when [query] is blank.
   */
  fun resolveVisibleEntries(
    archiveInfo: ArchiveInfo,
    currentVirtualDir: String,
    query: String,
  ): List<ArchiveEntryItem> {
    val cleanDir = normalizeArchivePath(currentVirtualDir)
    if (query.isBlank()) {
      return listVirtualDirectory(archiveInfo, cleanDir)
    }
    val scopeEntries = if (cleanDir.isEmpty()) {
      archiveInfo.allEntries
    } else {
      val prefix = "$cleanDir/"
      archiveInfo.allEntries.filter { it.archivePath.startsWith(prefix) }
    }
    return filterEntries(scopeEntries, query)
  }

  /**
   * Sorts archive entries according to [sortOrder], keeping directories first (matching [FileManagerEngine.sortFiles]).
   */
  fun sortEntries(entries: List<ArchiveEntryItem>, sortOrder: FileSortOrder): List<ArchiveEntryItem> {
    val comparator = when (sortOrder) {
      FileSortOrder.NAME_ASC -> compareBy<ArchiveEntryItem> { !it.isDirectory }
        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      FileSortOrder.NAME_DESC -> compareBy<ArchiveEntryItem> { !it.isDirectory }
        .thenByDescending(String.CASE_INSENSITIVE_ORDER) { it.name }
      FileSortOrder.DATE_DESC -> compareBy<ArchiveEntryItem> { !it.isDirectory }
        .thenByDescending { it.lastModifiedMs }
        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      FileSortOrder.DATE_ASC -> compareBy<ArchiveEntryItem> { !it.isDirectory }
        .thenBy { it.lastModifiedMs }
        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      FileSortOrder.SIZE_DESC -> compareBy<ArchiveEntryItem> { !it.isDirectory }
        .thenByDescending { it.sizeBytes }
        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      FileSortOrder.SIZE_ASC -> compareBy<ArchiveEntryItem> { !it.isDirectory }
        .thenBy { it.sizeBytes }
        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      FileSortOrder.TYPE_ASC -> compareBy<ArchiveEntryItem> { !it.isDirectory }
        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.extension }
        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
    }
    return entries.sortedWith(comparator)
  }

  /**
   * Groups and sorts archive entries according to [groupOption] and [sortOrder],
   * matching [FileManagerEngine.groupAndSortFiles].
   */
  fun groupAndSortEntries(
    entries: List<ArchiveEntryItem>,
    groupOption: FileGroupOption,
    sortOrder: FileSortOrder,
  ): List<ArchiveEntryGroup> {
    if (entries.isEmpty()) return emptyList()

    if (groupOption == FileGroupOption.NONE) {
      return listOf(ArchiveEntryGroup(title = "", items = sortEntries(entries, sortOrder)))
    }

    val groupedMap: Map<String, List<ArchiveEntryItem>> = when (groupOption) {
      FileGroupOption.TYPE -> {
        val typeOrder = listOf("Folders", "Images", "Videos", "Audio", "Documents", "Archives", "Code", "Other")
        val raw = entries.groupBy { item -> getTypeCategoryTitle(item) }
        typeOrder.mapNotNull { key -> raw[key]?.let { key to it } }.toMap()
      }
      FileGroupOption.DATE -> {
        val dateOrder = listOf("Today", "Yesterday", "This Week", "This Month", "Older")
        val raw = entries.groupBy { item -> getDateCategoryTitle(item.lastModifiedMs) }
        dateOrder.mapNotNull { key -> raw[key]?.let { key to it } }.toMap()
      }
      FileGroupOption.SIZE -> {
        val sizeOrder = listOf("Folders", "Huge (> 100 MB)", "Large (10 – 100 MB)", "Medium (1 – 10 MB)", "Small (< 1 MB)")
        val raw = entries.groupBy { item -> getSizeCategoryTitle(item) }
        sizeOrder.mapNotNull { key -> raw[key]?.let { key to it } }.toMap()
      }
      FileGroupOption.NAME -> {
        val raw = entries.groupBy { item ->
          val firstChar = item.name.firstOrNull()?.uppercaseChar() ?: '#'
          if (firstChar in 'A'..'Z') firstChar.toString() else "#"
        }
        val sortedKeys = raw.keys.sortedWith { a, b ->
          if (a == "#") 1 else if (b == "#") -1 else a.compareTo(b)
        }
        val orderedKeys = if (sortOrder == FileSortOrder.NAME_DESC) sortedKeys.reversed() else sortedKeys
        orderedKeys.mapNotNull { key -> raw[key]?.let { key to it } }.toMap()
      }
      FileGroupOption.NONE -> emptyMap()
    }

    return groupedMap.map { (title, groupItems) ->
      ArchiveEntryGroup(
        title = title,
        items = sortWithinGroup(groupItems, sortOrder),
      )
    }
  }

  /**
   * Computes bulk selection summary for selected [ArchiveEntryItem]s.
   */
  fun getBulkEntryInfo(items: List<ArchiveEntryItem>): ArchiveBulkInfo {
    var folders = 0
    var files = 0
    var uncompressed = 0L
    var compressed = 0L
    for (item in items) {
      if (item.isDirectory) {
        folders++
      } else {
        files++
      }
      uncompressed += item.sizeBytes.coerceAtLeast(0L)
      compressed += item.compressedSizeBytes.coerceAtLeast(0L)
    }
    return ArchiveBulkInfo(
      totalItems = items.size,
      folderCount = folders,
      fileCount = files,
      totalUncompressedBytes = uncompressed,
      totalCompressedBytes = compressed,
      formattedUncompressedSize = "${FileManagerEngine.formatFileSize(uncompressed)} ($uncompressed bytes)",
      formattedCompressedSize = "${FileManagerEngine.formatFileSize(compressed)} ($compressed bytes)",
    )
  }

  /**
   * Classification helpers for archive entries (mirroring [FileManagerEngine] classification).
   */
  fun isTextEntry(item: ArchiveEntryItem): Boolean {
    return !item.isDirectory && FileManagerEngine.isTextFile(File(item.name))
  }

  fun isImageEntry(item: ArchiveEntryItem): Boolean {
    return !item.isDirectory && FileManagerEngine.isImageFile(File(item.name))
  }

  fun isAudioEntry(item: ArchiveEntryItem): Boolean {
    return !item.isDirectory && FileManagerEngine.isAudioFile(File(item.name))
  }

  fun isVideoEntry(item: ArchiveEntryItem): Boolean {
    return !item.isDirectory && FileManagerEngine.isVideoFile(File(item.name))
  }

  fun isCodeEntry(item: ArchiveEntryItem): Boolean {
    return !item.isDirectory && FileManagerEngine.isCodeFile(File(item.name))
  }

  fun isArchiveEntry(item: ArchiveEntryItem): Boolean {
    return !item.isDirectory && FileManagerEngine.isArchiveFile(File(item.name))
  }

  /**
   * Reads the raw bytes of a single file entry from [archiveFile], up to [maxBytes].
   */
  fun readEntryBytes(
    archiveFile: File,
    archivePath: String,
    maxBytes: Int = 10 * 1024 * 1024,
  ): ByteArray? {
    if (!archiveFile.exists() || !archiveFile.isFile) return null
    val targetPath = normalizeArchivePath(archivePath)
    if (targetPath.isEmpty()) return null
    val format = detectFormat(archiveFile) ?: return null

    return try {
      when (format) {
        ArchiveFormat.ZIP -> {
          ZipInputStream(BufferedInputStream(FileInputStream(archiveFile))).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
              if (!entry.isDirectory && normalizeArchivePath(entry.name) == targetPath) {
                return readLimitedBytes(zis, maxBytes)
              }
              zis.closeEntry()
              entry = zis.nextEntry
            }
          }
          null
        }
        ArchiveFormat.TAR -> {
          BufferedInputStream(FileInputStream(archiveFile)).use { bis ->
            readSingleTarEntryBytes(bis, targetPath, maxBytes)
          }
        }
        ArchiveFormat.TAR_GZ -> {
          GZIPInputStream(BufferedInputStream(FileInputStream(archiveFile))).use { gzis ->
            readSingleTarEntryBytes(gzis, targetPath, maxBytes)
          }
        }
        ArchiveFormat.GZIP -> {
          val expectedName = deriveGzipInnerFileName(archiveFile.name)
          if (normalizeArchivePath(expectedName) == targetPath) {
            GZIPInputStream(BufferedInputStream(FileInputStream(archiveFile))).use { gzis ->
              readLimitedBytes(gzis, maxBytes)
            }
          } else {
            null
          }
        }
      }
    } catch (_: Exception) {
      null
    }
  }

  /**
   * Reads the UTF-8 text content of a single file entry inside [archiveFile].
   */
  fun readEntryText(
    archiveFile: File,
    archivePath: String,
    maxBytes: Int = 512 * 1024,
  ): String? {
    val bytes = readEntryBytes(archiveFile, archivePath, maxBytes) ?: return null
    return try {
      String(bytes, Charsets.UTF_8)
    } catch (_: Exception) {
      null
    }
  }

  /**
   * Extracts a single file entry to a temporary cache file for external opening or sharing.
   */
  fun extractEntryToTempFile(
    context: Context,
    archiveFile: File,
    entry: ArchiveEntryItem,
  ): File? {
    if (entry.isDirectory) return null
    val bytes = readEntryBytes(archiveFile, entry.archivePath) ?: return null
    return try {
      val cacheDir = File(context.cacheDir, "archive_preview").apply { mkdirs() }
      val safeName = entry.name.replace(Regex("[\\\\/:*?\"<>|]"), "_").ifEmpty { "entry" }
      val outFile = File(cacheDir, safeName)
      FileOutputStream(outFile).use { it.write(bytes) }
      outFile
    } catch (_: Exception) {
      null
    }
  }

  /**
   * Extracts all entries in [archiveFile] into [destinationDir] with Zip-Slip protection.
   */
  fun extractAll(
    archiveFile: File,
    destinationDir: File,
    overwrite: Boolean = true,
  ): ArchiveOperationResult {
    return extractEntries(
      archiveFile = archiveFile,
      selectedArchivePaths = emptySet(),
      destinationDir = destinationDir,
      preservePaths = true,
      overwrite = overwrite,
      extractEntireArchive = true,
    )
  }

  /**
   * Extracts the specified [selectedArchivePaths] (including recursive children of any selected
   * virtual directories) into [destinationDir] with Zip-Slip path traversal protection.
   */
  fun extractEntries(
    archiveFile: File,
    selectedArchivePaths: Set<String>,
    destinationDir: File,
    preservePaths: Boolean = true,
    overwrite: Boolean = true,
    extractEntireArchive: Boolean = false,
  ): ArchiveOperationResult {
    if (!archiveFile.exists() || !archiveFile.isFile) {
      return ArchiveOperationResult(
        success = false,
        message = "Archive file not found: ${archiveFile.name}",
      )
    }
    val format = detectFormat(archiveFile)
      ?: return ArchiveOperationResult(
        success = false,
        message = "Unsupported archive format: ${archiveFile.name}",
      )

    if (!destinationDir.exists() && !destinationDir.mkdirs()) {
      return ArchiveOperationResult(
        success = false,
        message = "Could not create destination folder: ${destinationDir.name}",
      )
    }

    val normalizedSelections = selectedArchivePaths
      .map { normalizeArchivePath(it) }
      .filter { it.isNotEmpty() }
      .toSet()

    if (!extractEntireArchive && normalizedSelections.isEmpty()) {
      return ArchiveOperationResult(
        success = false,
        message = "No archive entries selected for extraction",
      )
    }

    fun matchesSelection(entryPath: String): Boolean {
      if (extractEntireArchive) return true
      val clean = normalizeArchivePath(entryPath)
      return normalizedSelections.any { sel ->
        clean == sel || clean.startsWith("$sel/")
      }
    }

    return try {
      var extractedCount = 0
      var totalBytes = 0L

      val rawPayloads = readAllRawPayloads(archiveFile, format)
      for (payload in rawPayloads) {
        if (!isSafeExtractTarget(destinationDir, payload.archivePath)) {
          continue
        }
        val cleanPath = normalizeArchivePath(payload.archivePath)
        if (cleanPath.isEmpty() || !matchesSelection(cleanPath)) {
          continue
        }

        val relativeOutputPath = if (preservePaths) {
          cleanPath
        } else {
          leafNameOf(cleanPath)
        }
        if (!isSafeExtractTarget(destinationDir, relativeOutputPath)) {
          continue
        }

        val targetFile = File(destinationDir, relativeOutputPath)
        if (payload.isDirectory) {
          targetFile.mkdirs()
        } else {
          targetFile.parentFile?.mkdirs()
          val finalTarget = if (!overwrite && targetFile.exists()) {
            resolveUniqueDestinationFile(targetFile)
          } else {
            targetFile
          }
          FileOutputStream(finalTarget).use { fos ->
            fos.write(payload.bytes)
          }
          if (payload.lastModifiedMs > 0L) {
            finalTarget.setLastModified(payload.lastModifiedMs)
          }
          extractedCount++
          totalBytes += payload.bytes.size
        }
      }

      ArchiveOperationResult(
        success = true,
        outputFile = destinationDir,
        extractedFilesCount = extractedCount,
        totalBytesProcessed = totalBytes,
        message = "Extracted $extractedCount ${if (extractedCount == 1) "file" else "files"} (${FileManagerEngine.formatFileSize(totalBytes)}) to ${destinationDir.name}",
      )
    } catch (e: Exception) {
      ArchiveOperationResult(
        success = false,
        message = "Extraction failed: ${e.message ?: "Unknown error"}",
      )
    }
  }

  /**
   * Creates a new archive at [outputFile] from one or more [sourceFiles] (files and/or directories recursively).
   */
  fun createArchive(
    sourceFiles: List<File>,
    outputFile: File,
    format: ArchiveFormat = ArchiveFormat.ZIP,
    compressionLevel: ArchiveCompressionLevel = ArchiveCompressionLevel.BALANCED,
  ): ArchiveOperationResult {
    val validSources = sourceFiles.filter { it.exists() }
    if (validSources.isEmpty()) {
      return ArchiveOperationResult(
        success = false,
        message = "Please select at least one existing file or folder to compress",
      )
    }

    return try {
      outputFile.parentFile?.mkdirs()
      val payloads = mutableListOf<RawEntryPayload>()
      val usedPaths = mutableSetOf<String>()

      for (source in validSources) {
        collectFilePayloadsRecursively(
          file = source,
          parentArchivePrefix = "",
          payloads = payloads,
          usedPaths = usedPaths,
        )
      }

      if (payloads.isEmpty()) {
        return ArchiveOperationResult(
          success = false,
          message = "No files found to compress",
        )
      }

      if (format == ArchiveFormat.GZIP) {
        val firstFilePayload = payloads.firstOrNull { !it.isDirectory }
          ?: return ArchiveOperationResult(
            success = false,
            message = "GZIP format requires a file entry",
          )
        GZIPOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { gzos ->
          gzos.write(firstFilePayload.bytes)
        }
        return ArchiveOperationResult(
          success = true,
          outputFile = outputFile,
          extractedFilesCount = 1,
          totalBytesProcessed = firstFilePayload.bytes.size.toLong(),
          message = "Created ${outputFile.name} (${FileManagerEngine.formatFileSize(outputFile.length())})",
        )
      }

      writeRawPayloadsToFile(
        payloads = payloads,
        outputFile = outputFile,
        format = format,
        compressionLevel = compressionLevel,
      )

      val fileCount = payloads.count { !it.isDirectory }
      val totalUncompressed = payloads.filter { !it.isDirectory }.sumOf { it.bytes.size.toLong() }

      ArchiveOperationResult(
        success = true,
        outputFile = outputFile,
        extractedFilesCount = fileCount,
        totalBytesProcessed = totalUncompressed,
        message = "Created ${outputFile.name} with $fileCount ${if (fileCount == 1) "file" else "files"} (${FileManagerEngine.formatFileSize(outputFile.length())})",
      )
    } catch (e: Exception) {
      ArchiveOperationResult(
        success = false,
        message = "Failed to create archive: ${e.message ?: "Unknown error"}",
      )
    }
  }

  /**
   * Adds one or more [filesToAdd] (files or folders) into an existing writable archive ([ZIP], [TAR], [TAR_GZ])
   * under [targetVirtualDir].
   */
  fun addFilesToArchive(
    archiveFile: File,
    filesToAdd: List<File>,
    targetVirtualDir: String = "",
    compressionLevel: ArchiveCompressionLevel = ArchiveCompressionLevel.BALANCED,
  ): ArchiveOperationResult {
    if (!archiveFile.exists()) {
      return ArchiveOperationResult(success = false, message = "Archive does not exist")
    }
    val format = detectFormat(archiveFile)
      ?: return ArchiveOperationResult(success = false, message = "Unsupported archive format")
    if (!format.supportsMultiEntryWrite) {
      return ArchiveOperationResult(success = false, message = "${format.displayName} does not support adding multiple entries")
    }
    val validSources = filesToAdd.filter { it.exists() && it.canonicalPath != archiveFile.canonicalPath }
    if (validSources.isEmpty()) {
      return ArchiveOperationResult(success = false, message = "No valid files selected to add")
    }

    return try {
      val existingPayloads = readAllRawPayloads(archiveFile, format).toMutableList()
      val usedPaths = existingPayloads.map { normalizeArchivePath(it.archivePath) }.toMutableSet()
      val cleanPrefix = normalizeArchivePath(targetVirtualDir)

      val newPayloads = mutableListOf<RawEntryPayload>()
      for (src in validSources) {
        collectFilePayloadsRecursively(
          file = src,
          parentArchivePrefix = cleanPrefix,
          payloads = newPayloads,
          usedPaths = usedPaths,
        )
      }

      val combined = existingPayloads + newPayloads
      val tempFile = File(archiveFile.parentFile ?: archiveFile.absoluteFile.parentFile, ".${archiveFile.name}.tmp_${System.currentTimeMillis()}")
      writeRawPayloadsToFile(combined, tempFile, format, compressionLevel)
      if (archiveFile.exists()) archiveFile.delete()
      tempFile.renameTo(archiveFile)

      val addedFilesCount = newPayloads.count { !it.isDirectory }
      val addedBytes = newPayloads.filter { !it.isDirectory }.sumOf { it.bytes.size.toLong() }

      ArchiveOperationResult(
        success = true,
        outputFile = archiveFile,
        extractedFilesCount = addedFilesCount,
        totalBytesProcessed = addedBytes,
        message = "Added ${validSources.size} ${if (validSources.size == 1) "item" else "items"} to ${archiveFile.name}",
      )
    } catch (e: Exception) {
      ArchiveOperationResult(
        success = false,
        message = "Failed to add files to archive: ${e.message ?: "Unknown error"}",
      )
    }
  }

  /**
   * Creates a new virtual folder inside an existing writable archive ([ZIP], [TAR], [TAR_GZ]).
   */
  fun createFolderInArchive(
    archiveFile: File,
    parentVirtualDir: String,
    folderName: String,
  ): ArchiveOperationResult {
    val validationError = FileManagerEngine.validateFileName(folderName)
    if (validationError != null) {
      return ArchiveOperationResult(success = false, message = validationError)
    }
    val format = detectFormat(archiveFile)
      ?: return ArchiveOperationResult(success = false, message = "Unsupported archive format")
    if (!format.supportsMultiEntryWrite) {
      return ArchiveOperationResult(success = false, message = "${format.displayName} does not support folders")
    }

    val cleanParent = normalizeArchivePath(parentVirtualDir)
    val newFolderPath = if (cleanParent.isEmpty()) folderName.trim() else "$cleanParent/${folderName.trim()}"

    return try {
      val existingPayloads = readAllRawPayloads(archiveFile, format).toMutableList()
      if (existingPayloads.any { normalizeArchivePath(it.archivePath).equals(newFolderPath, ignoreCase = true) }) {
        return ArchiveOperationResult(success = false, message = "A folder or file with that name already exists")
      }
      existingPayloads.add(
        RawEntryPayload(
          archivePath = newFolderPath,
          isDirectory = true,
          bytes = ByteArray(0),
          lastModifiedMs = System.currentTimeMillis(),
        )
      )
      val tempFile = File(archiveFile.parentFile ?: archiveFile.absoluteFile.parentFile, ".${archiveFile.name}.tmp_${System.currentTimeMillis()}")
      writeRawPayloadsToFile(existingPayloads, tempFile, format, ArchiveCompressionLevel.BALANCED)
      if (archiveFile.exists()) archiveFile.delete()
      tempFile.renameTo(archiveFile)

      ArchiveOperationResult(
        success = true,
        outputFile = archiveFile,
        message = "Created folder \"${folderName.trim()}\" in ${archiveFile.name}",
      )
    } catch (e: Exception) {
      ArchiveOperationResult(
        success = false,
        message = "Failed to create folder: ${e.message ?: "Unknown error"}",
      )
    }
  }

  /**
   * Deletes the specified [pathsToRemove] (and any descendants of removed folders) from [archiveFile].
   */
  fun deleteEntriesFromArchive(
    archiveFile: File,
    pathsToRemove: Set<String>,
  ): ArchiveOperationResult {
    val format = detectFormat(archiveFile)
      ?: return ArchiveOperationResult(success = false, message = "Unsupported archive format")
    if (!format.supportsMultiEntryWrite) {
      return ArchiveOperationResult(success = false, message = "${format.displayName} does not support deleting individual entries")
    }

    val cleanTargets = pathsToRemove.map { normalizeArchivePath(it) }.filter { it.isNotEmpty() }.toSet()
    if (cleanTargets.isEmpty()) {
      return ArchiveOperationResult(success = false, message = "No entries selected to delete")
    }

    return try {
      val existingPayloads = readAllRawPayloads(archiveFile, format)
      val remainingPayloads = existingPayloads.filterNot { payload ->
        val clean = normalizeArchivePath(payload.archivePath)
        cleanTargets.any { target -> clean == target || clean.startsWith("$target/") }
      }

      val removedCount = existingPayloads.size - remainingPayloads.size
      val tempFile = File(archiveFile.parentFile ?: archiveFile.absoluteFile.parentFile, ".${archiveFile.name}.tmp_${System.currentTimeMillis()}")
      writeRawPayloadsToFile(remainingPayloads, tempFile, format, ArchiveCompressionLevel.BALANCED)
      if (archiveFile.exists()) archiveFile.delete()
      tempFile.renameTo(archiveFile)

      ArchiveOperationResult(
        success = true,
        outputFile = archiveFile,
        extractedFilesCount = removedCount,
        message = "Removed ${cleanTargets.size} ${if (cleanTargets.size == 1) "item" else "items"} from ${archiveFile.name}",
      )
    } catch (e: Exception) {
      ArchiveOperationResult(
        success = false,
        message = "Failed to delete entries: ${e.message ?: "Unknown error"}",
      )
    }
  }

  /**
   * Renames an entry (file or virtual directory and all its descendants) inside [archiveFile].
   */
  fun renameEntryInArchive(
    archiveFile: File,
    oldArchivePath: String,
    newLeafName: String,
  ): ArchiveOperationResult {
    val validationError = FileManagerEngine.validateFileName(newLeafName)
    if (validationError != null) {
      return ArchiveOperationResult(success = false, message = validationError)
    }
    val format = detectFormat(archiveFile)
      ?: return ArchiveOperationResult(success = false, message = "Unsupported archive format")
    if (!format.supportsMultiEntryWrite) {
      return ArchiveOperationResult(success = false, message = "${format.displayName} does not support renaming entries")
    }

    val cleanOld = normalizeArchivePath(oldArchivePath)
    if (cleanOld.isEmpty()) {
      return ArchiveOperationResult(success = false, message = "Invalid entry path")
    }
    val parent = parentArchivePathOf(cleanOld)
    val cleanNew = if (parent.isEmpty()) newLeafName.trim() else "$parent/${newLeafName.trim()}"
    if (cleanOld == cleanNew) {
      return ArchiveOperationResult(success = true, outputFile = archiveFile, message = "Entry unchanged")
    }

    return try {
      val existingPayloads = readAllRawPayloads(archiveFile, format)
      if (existingPayloads.any {
          val p = normalizeArchivePath(it.archivePath)
          p.equals(cleanNew, ignoreCase = true) || p.startsWith("$cleanNew/", ignoreCase = true)
        }
      ) {
        return ArchiveOperationResult(success = false, message = "An item with that name already exists")
      }

      var touched = false
      val updatedPayloads = existingPayloads.map { payload ->
        val clean = normalizeArchivePath(payload.archivePath)
        when {
          clean == cleanOld -> {
            touched = true
            payload.copy(archivePath = cleanNew)
          }
          clean.startsWith("$cleanOld/") -> {
            touched = true
            val suffix = clean.removePrefix("$cleanOld/")
            payload.copy(archivePath = "$cleanNew/$suffix")
          }
          else -> payload
        }
      }

      if (!touched) {
        return ArchiveOperationResult(success = false, message = "Entry not found in archive")
      }

      val tempFile = File(archiveFile.parentFile ?: archiveFile.absoluteFile.parentFile, ".${archiveFile.name}.tmp_${System.currentTimeMillis()}")
      writeRawPayloadsToFile(updatedPayloads, tempFile, format, ArchiveCompressionLevel.BALANCED)
      if (archiveFile.exists()) archiveFile.delete()
      tempFile.renameTo(archiveFile)

      ArchiveOperationResult(
        success = true,
        outputFile = archiveFile,
        message = "Renamed to \"${newLeafName.trim()}\"",
      )
    } catch (e: Exception) {
      ArchiveOperationResult(
        success = false,
        message = "Failed to rename entry: ${e.message ?: "Unknown error"}",
      )
    }
  }

  /**
   * Generates a rich, self-contained sample `.zip` archive with nested folders, documents, code,
   * and a valid PNG image so the user can test viewing, navigating, previewing, and extracting immediately.
   */
  fun generateSampleArchive(
    outputDir: File,
    fileName: String = "sample_project_bundle.zip",
  ): File {
    outputDir.mkdirs()
    val outFile = File(outputDir, fileName)
    val now = System.currentTimeMillis()

    val samplePngBytes = createMinimalSolidPngBytes(width = 64, height = 64, r = 0x8E, g = 0x66, b = 0xA6)

    val payloads = listOf(
      RawEntryPayload(
        archivePath = "README.md",
        isDirectory = false,
        bytes = """
          # Sample Project Bundle
          
          Welcome to the **Toolbox Archive Explorer**!
          
          ## Included Folders
          - `Documents/` — Project notes, checklists, and CSV reports
          - `Source/` — Sample Kotlin code and JSON configuration
          - `Images/` — Sample PNG badge preview
          
          Tap any folder to browse inside, tap files to preview or extract them, or long-press items for bulk actions.
        """.trimIndent().toByteArray(Charsets.UTF_8),
        lastModifiedMs = now - 300_000L,
      ),
      RawEntryPayload(
        archivePath = "Documents/release_notes.txt",
        isDirectory = false,
        bytes = """
          Version 2.4 Release Notes
          =========================
          - Added Archive Explorer with list & grid views
          - Added one-tap ZIP creation and extraction
          - Added in-archive text and image preview
        """.trimIndent().toByteArray(Charsets.UTF_8),
        lastModifiedMs = now - 600_000L,
      ),
      RawEntryPayload(
        archivePath = "Documents/Reports/metrics_2026.csv",
        isDirectory = false,
        bytes = """
          Module,Files,SizeKB,Status
          ArchiveEngine,12,48,Ready
          FilesExplorer,18,96,Ready
          AudioEditor,9,64,Ready
        """.trimIndent().toByteArray(Charsets.UTF_8),
        lastModifiedMs = now - 1_200_000L,
      ),
      RawEntryPayload(
        archivePath = "Source/config.json",
        isDirectory = false,
        bytes = """
          {
            "app": "Toolbox",
            "tool": "Archive",
            "supportedFormats": ["ZIP", "TAR", "TAR.GZ", "GZIP"],
            "defaultCompression": "BALANCED"
          }
        """.trimIndent().toByteArray(Charsets.UTF_8),
        lastModifiedMs = now - 1_800_000L,
      ),
      RawEntryPayload(
        archivePath = "Source/HelloArchive.kt",
        isDirectory = false,
        bytes = """
          package sample

          fun main() {
            println("Hello from inside sample_project_bundle.zip!")
          }
        """.trimIndent().toByteArray(Charsets.UTF_8),
        lastModifiedMs = now - 2_400_000L,
      ),
      RawEntryPayload(
        archivePath = "Images/badge_preview.png",
        isDirectory = false,
        bytes = samplePngBytes,
        lastModifiedMs = now - 3_600_000L,
      ),
    )

    writeRawPayloadsToFile(
      payloads = payloads,
      outputFile = outFile,
      format = ArchiveFormat.ZIP,
      compressionLevel = ArchiveCompressionLevel.BALANCED,
    )
    return outFile
  }

  // ============================================================================
  // INTERNAL STREAM & HIERARCHY HELPERS
  // ============================================================================

  private data class ParsedRawMetadata(
    val archivePath: String,
    val isDirectory: Boolean,
    val uncompressedSize: Long,
    val compressedSize: Long,
    val lastModifiedMs: Long,
    val crc32: Long,
  )

  private fun readZipMetadataEntries(archiveFile: File): List<ParsedRawMetadata> {
    val list = mutableListOf<ParsedRawMetadata>()
    try {
      ZipFile(archiveFile).use { zipFile ->
        val entries = zipFile.entries()
        while (entries.hasMoreElements()) {
          val entry = entries.nextElement()
          val rawName = entry.name ?: continue
          if (rawName.replace('\\', '/').split('/').any { it == ".." }) continue
          val isDir = entry.isDirectory || rawName.endsWith("/") || rawName.endsWith("\\")
          val normPath = normalizeArchivePath(rawName)
          if (normPath.isEmpty()) continue
          val size = entry.size.takeIf { it >= 0L } ?: 0L
          val compSize = entry.compressedSize.takeIf { it >= 0L } ?: size
          val time = entry.time.takeIf { it > 0L } ?: archiveFile.lastModified()
          val crc = entry.crc.takeIf { it >= 0L } ?: 0L
          list.add(
            ParsedRawMetadata(
              archivePath = normPath,
              isDirectory = isDir,
              uncompressedSize = if (isDir) 0L else size,
              compressedSize = if (isDir) 0L else compSize,
              lastModifiedMs = time,
              crc32 = crc,
            )
          )
        }
      }
      return list
    } catch (_: Exception) {
      // Fallback to streaming ZipInputStream if ZipFile central directory has non-standard headers
    }

    ZipInputStream(BufferedInputStream(FileInputStream(archiveFile))).use { zis ->
      var entry = zis.nextEntry
      while (entry != null) {
        val rawName = entry.name
        if (!rawName.replace('\\', '/').split('/').any { it == ".." }) {
          val isDir = entry.isDirectory || rawName.endsWith("/") || rawName.endsWith("\\")
          val normPath = normalizeArchivePath(rawName)
          if (normPath.isNotEmpty()) {
            val bytes = if (isDir) ByteArray(0) else zis.readBytes()
            val size = entry.size.takeIf { it >= 0L } ?: bytes.size.toLong()
            val compSize = entry.compressedSize.takeIf { it >= 0L } ?: (bytes.size.toLong())
            val crc = entry.crc.takeIf { it >= 0L } ?: computeCrc32(bytes)
            list.add(
              ParsedRawMetadata(
                archivePath = normPath,
                isDirectory = isDir,
                uncompressedSize = if (isDir) 0L else size,
                compressedSize = if (isDir) 0L else compSize,
                lastModifiedMs = entry.time.takeIf { it > 0L } ?: archiveFile.lastModified(),
                crc32 = crc,
              )
            )
          }
        }
        zis.closeEntry()
        entry = zis.nextEntry
      }
    }
    return list
  }

  private fun readTarMetadataEntries(input: InputStream): List<ParsedRawMetadata> {
    val entries = mutableListOf<ParsedRawMetadata>()
    val header = ByteArray(512)
    while (true) {
      val read = readFullyOrEof(input, header)
      if (read < 512 || header.all { it == 0.toByte() }) break
      val rawName = extractNullTerminatedString(header, 0, 100)
      val prefix = extractNullTerminatedString(header, 345, 155)
      val combinedName = if (prefix.isNotEmpty()) "$prefix/$rawName" else rawName
      val sizeOctal = extractNullTerminatedString(header, 124, 12).trim()
      val mtimeOctal = extractNullTerminatedString(header, 136, 12).trim()
      val typeFlag = header[156].toInt().toChar()

      val size = sizeOctal.toLongOrNull(8)?.coerceAtLeast(0L) ?: 0L
      val mtimeSec = mtimeOctal.toLongOrNull(8) ?: (System.currentTimeMillis() / 1000L)
      val isDir = typeFlag == '5' || combinedName.endsWith("/")

      var crcValue = 0L
      if (size > 0L) {
        val crc = CRC32()
        skipAndCrcStream(input, size, crc)
        crcValue = crc.value
        val remainder = (size % 512L).toInt()
        if (remainder != 0) {
          skipFullyOrEof(input, (512 - remainder).toLong())
        }
      }

      if (!combinedName.replace('\\', '/').split('/').any { it == ".." }) {
        val normPath = normalizeArchivePath(combinedName)
        if (normPath.isNotEmpty()) {
          entries.add(
            ParsedRawMetadata(
              archivePath = normPath,
              isDirectory = isDir,
              uncompressedSize = if (isDir) 0L else size,
              compressedSize = if (isDir) 0L else size,
              lastModifiedMs = mtimeSec * 1000L,
              crc32 = crcValue,
            )
          )
        }
      }
    }
    return entries
  }

  private fun readGzipMetadataEntries(archiveFile: File): List<ParsedRawMetadata> {
    val innerName = deriveGzipInnerFileName(archiveFile.name)
    val crc = CRC32()
    var uncompressedSize = 0L
    GZIPInputStream(BufferedInputStream(FileInputStream(archiveFile))).use { gzis ->
      val buf = ByteArray(8192)
      while (true) {
        val r = gzis.read(buf)
        if (r <= 0) break
        crc.update(buf, 0, r)
        uncompressedSize += r
      }
    }
    return listOf(
      ParsedRawMetadata(
        archivePath = normalizeArchivePath(innerName),
        isDirectory = false,
        uncompressedSize = uncompressedSize,
        compressedSize = archiveFile.length(),
        lastModifiedMs = archiveFile.lastModified(),
        crc32 = crc.value,
      )
    )
  }

  private fun buildCompleteEntryHierarchy(
    rawEntries: List<ParsedRawMetadata>,
    fallbackTimestampMs: Long,
  ): List<ArchiveEntryItem> {
    val fileMap = linkedMapOf<String, ParsedRawMetadata>()
    val directoryPaths = linkedSetOf<String>()
    val directoryTimestamps = mutableMapOf<String, Long>()

    for (raw in rawEntries) {
      val clean = normalizeArchivePath(raw.archivePath)
      if (clean.isEmpty()) continue

      // Ensure all ancestor directories exist in directoryPaths
      var ancestor = parentArchivePathOf(clean)
      while (ancestor.isNotEmpty()) {
        directoryPaths.add(ancestor)
        val existingTime = directoryTimestamps[ancestor] ?: 0L
        if (raw.lastModifiedMs > existingTime) {
          directoryTimestamps[ancestor] = raw.lastModifiedMs
        }
        ancestor = parentArchivePathOf(ancestor)
      }

      if (raw.isDirectory) {
        directoryPaths.add(clean)
        val existingTime = directoryTimestamps[clean] ?: 0L
        if (raw.lastModifiedMs > existingTime) {
          directoryTimestamps[clean] = raw.lastModifiedMs
        }
      } else {
        fileMap[clean] = raw
      }
    }

    val result = mutableListOf<ArchiveEntryItem>()

    // 1. Build file items
    val fileItems = fileMap.values.map { raw ->
      val name = leafNameOf(raw.archivePath)
      val parent = parentArchivePathOf(raw.archivePath)
      val dummyFile = File(name)
      val ext = dummyFile.extension.lowercase(Locale.ROOT)
      val mime = FileManagerEngine.getMimeType(dummyFile)
      ArchiveEntryItem(
        name = name,
        archivePath = raw.archivePath,
        parentArchivePath = parent,
        isDirectory = false,
        sizeBytes = raw.uncompressedSize,
        compressedSizeBytes = raw.compressedSize,
        lastModifiedMs = raw.lastModifiedMs.takeIf { it > 0L } ?: fallbackTimestampMs,
        crc32 = raw.crc32,
        extension = ext,
        mimeType = mime,
        childCount = null,
      )
    }

    // 2. Build directory items with aggregated descendant sizes & direct child counts
    val allKnownPaths = directoryPaths + fileMap.keys
    val directChildrenCountMap = mutableMapOf<String, Int>()
    for (p in allKnownPaths) {
      val parent = parentArchivePathOf(p)
      directChildrenCountMap[parent] = (directChildrenCountMap[parent] ?: 0) + 1
    }

    val dirItems = directoryPaths.map { dirPath ->
      val name = leafNameOf(dirPath)
      val parent = parentArchivePathOf(dirPath)
      val prefix = "$dirPath/"
      var sumUncompressed = 0L
      var sumCompressed = 0L
      for (f in fileItems) {
        if (f.archivePath.startsWith(prefix)) {
          sumUncompressed += f.sizeBytes
          sumCompressed += f.compressedSizeBytes
        }
      }
      ArchiveEntryItem(
        name = name,
        archivePath = dirPath,
        parentArchivePath = parent,
        isDirectory = true,
        sizeBytes = sumUncompressed,
        compressedSizeBytes = sumCompressed,
        lastModifiedMs = directoryTimestamps[dirPath]?.takeIf { it > 0L } ?: fallbackTimestampMs,
        crc32 = 0L,
        extension = "",
        mimeType = "inode/directory",
        childCount = directChildrenCountMap[dirPath] ?: 0,
      )
    }

    result.addAll(dirItems)
    result.addAll(fileItems)
    return sortEntries(result, FileSortOrder.NAME_ASC)
  }

  private fun readAllRawPayloads(
    archiveFile: File,
    format: ArchiveFormat,
  ): List<RawEntryPayload> {
    val list = mutableListOf<RawEntryPayload>()
    when (format) {
      ArchiveFormat.ZIP -> {
        ZipInputStream(BufferedInputStream(FileInputStream(archiveFile))).use { zis ->
          var entry = zis.nextEntry
          while (entry != null) {
            val rawName = entry.name
            if (!rawName.replace('\\', '/').split('/').any { it == ".." }) {
              val isDir = entry.isDirectory || rawName.endsWith("/") || rawName.endsWith("\\")
              val clean = normalizeArchivePath(rawName)
              if (clean.isNotEmpty()) {
                val bytes = if (isDir) ByteArray(0) else zis.readBytes()
                list.add(
                  RawEntryPayload(
                    archivePath = clean,
                    isDirectory = isDir,
                    bytes = bytes,
                    lastModifiedMs = entry.time.takeIf { it > 0L } ?: archiveFile.lastModified(),
                    crc32 = entry.crc.takeIf { it >= 0L } ?: computeCrc32(bytes),
                  )
                )
              }
            }
            zis.closeEntry()
            entry = zis.nextEntry
          }
        }
      }
      ArchiveFormat.TAR -> {
        BufferedInputStream(FileInputStream(archiveFile)).use { bis ->
          list.addAll(readTarRawPayloads(bis))
        }
      }
      ArchiveFormat.TAR_GZ -> {
        GZIPInputStream(BufferedInputStream(FileInputStream(archiveFile))).use { gzis ->
          list.addAll(readTarRawPayloads(gzis))
        }
      }
      ArchiveFormat.GZIP -> {
        val innerName = deriveGzipInnerFileName(archiveFile.name)
        GZIPInputStream(BufferedInputStream(FileInputStream(archiveFile))).use { gzis ->
          val bytes = gzis.readBytes()
          list.add(
            RawEntryPayload(
              archivePath = normalizeArchivePath(innerName),
              isDirectory = false,
              bytes = bytes,
              lastModifiedMs = archiveFile.lastModified(),
              crc32 = computeCrc32(bytes),
            )
          )
        }
      }
    }
    return list
  }

  private fun readTarRawPayloads(input: InputStream): List<RawEntryPayload> {
    val entries = mutableListOf<RawEntryPayload>()
    val header = ByteArray(512)
    while (true) {
      val read = readFullyOrEof(input, header)
      if (read < 512 || header.all { it == 0.toByte() }) break
      val rawName = extractNullTerminatedString(header, 0, 100)
      val prefix = extractNullTerminatedString(header, 345, 155)
      val combinedName = if (prefix.isNotEmpty()) "$prefix/$rawName" else rawName
      val sizeOctal = extractNullTerminatedString(header, 124, 12).trim()
      val mtimeOctal = extractNullTerminatedString(header, 136, 12).trim()
      val typeFlag = header[156].toInt().toChar()

      val size = sizeOctal.toLongOrNull(8)?.coerceAtLeast(0L) ?: 0L
      val mtimeSec = mtimeOctal.toLongOrNull(8) ?: (System.currentTimeMillis() / 1000L)
      val isDir = typeFlag == '5' || combinedName.endsWith("/")

      val payload = ByteArray(size.toInt())
      if (size > 0L) {
        readFullyOrEof(input, payload)
        val remainder = (size % 512L).toInt()
        if (remainder != 0) {
          val pad = ByteArray(512 - remainder)
          readFullyOrEof(input, pad)
        }
      }
      if (!combinedName.replace('\\', '/').split('/').any { it == ".." }) {
        val clean = normalizeArchivePath(combinedName)
        if (clean.isNotEmpty()) {
          entries.add(
            RawEntryPayload(
              archivePath = clean,
              isDirectory = isDir,
              bytes = if (isDir) ByteArray(0) else payload,
              lastModifiedMs = mtimeSec * 1000L,
              crc32 = if (isDir) 0L else computeCrc32(payload),
            )
          )
        }
      }
    }
    return entries
  }

  private fun readSingleTarEntryBytes(
    input: InputStream,
    targetArchivePath: String,
    maxBytes: Int,
  ): ByteArray? {
    val header = ByteArray(512)
    while (true) {
      val read = readFullyOrEof(input, header)
      if (read < 512 || header.all { it == 0.toByte() }) break
      val rawName = extractNullTerminatedString(header, 0, 100)
      val prefix = extractNullTerminatedString(header, 345, 155)
      val combinedName = if (prefix.isNotEmpty()) "$prefix/$rawName" else rawName
      val sizeOctal = extractNullTerminatedString(header, 124, 12).trim()
      val typeFlag = header[156].toInt().toChar()
      val size = sizeOctal.toLongOrNull(8)?.coerceAtLeast(0L) ?: 0L
      val isDir = typeFlag == '5' || combinedName.endsWith("/")
      val clean = normalizeArchivePath(combinedName)

      if (!isDir && clean == targetArchivePath) {
        val toRead = size.coerceAtMost(maxBytes.toLong()).toInt()
        val buf = ByteArray(toRead)
        readFullyOrEof(input, buf)
        return buf
      } else if (size > 0L) {
        skipFullyOrEof(input, size)
        val remainder = (size % 512L).toInt()
        if (remainder != 0) {
          skipFullyOrEof(input, (512 - remainder).toLong())
        }
      }
    }
    return null
  }

  private fun writeRawPayloadsToFile(
    payloads: List<RawEntryPayload>,
    outputFile: File,
    format: ArchiveFormat,
    compressionLevel: ArchiveCompressionLevel,
  ) {
    when (format) {
      ArchiveFormat.ZIP -> {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { zos ->
          zos.setLevel(compressionLevel.zipLevel)
          val seenEntries = mutableSetOf<String>()
          for (payload in payloads) {
            val clean = normalizeArchivePath(payload.archivePath)
            if (clean.isEmpty()) continue
            val entryName = if (payload.isDirectory) "$clean/" else clean
            if (!seenEntries.add(entryName)) continue

            val zipEntry = ZipEntry(entryName).apply {
              time = payload.lastModifiedMs.takeIf { it > 0L } ?: System.currentTimeMillis()
              if (payload.isDirectory || compressionLevel == ArchiveCompressionLevel.STORE) {
                method = ZipEntry.STORED
                size = payload.bytes.size.toLong()
                compressedSize = payload.bytes.size.toLong()
                crc = computeCrc32(payload.bytes)
              }
            }
            zos.putNextEntry(zipEntry)
            if (!payload.isDirectory && payload.bytes.isNotEmpty()) {
              zos.write(payload.bytes)
            }
            zos.closeEntry()
          }
        }
      }
      ArchiveFormat.TAR -> {
        BufferedOutputStream(FileOutputStream(outputFile)).use { bos ->
          writeTarPayloads(payloads, bos)
        }
      }
      ArchiveFormat.TAR_GZ -> {
        GZIPOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { gzos ->
          writeTarPayloads(payloads, gzos)
        }
      }
      ArchiveFormat.GZIP -> {
        val firstFile = payloads.firstOrNull { !it.isDirectory }
        GZIPOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { gzos ->
          if (firstFile != null) {
            gzos.write(firstFile.bytes)
          }
        }
      }
    }
  }

  private fun writeTarPayloads(payloads: List<RawEntryPayload>, out: OutputStream) {
    val seen = mutableSetOf<String>()
    for (entry in payloads) {
      val clean = normalizeArchivePath(entry.archivePath)
      if (clean.isEmpty()) continue
      val tarName = if (entry.isDirectory) "$clean/" else clean
      if (!seen.add(tarName)) continue

      val header = ByteArray(512)
      val nameBytes = tarName.take(99).toByteArray(Charsets.UTF_8)
      System.arraycopy(nameBytes, 0, header, 0, nameBytes.size)

      writeOctalBytes(if (entry.isDirectory) "0000755" else "0000644", header, 100, 8)
      writeOctalBytes("0000000", header, 108, 8)
      writeOctalBytes("0000000", header, 116, 8)
      val byteSize = if (entry.isDirectory) 0 else entry.bytes.size
      writeOctalBytes(byteSize.toString(8).padStart(11, '0'), header, 124, 12)
      val mtimeSec = (entry.lastModifiedMs.takeIf { it > 0L } ?: System.currentTimeMillis()) / 1000L
      writeOctalBytes(mtimeSec.toString(8).padStart(11, '0'), header, 136, 12)

      for (i in 148 until 156) header[i] = ' '.code.toByte()
      header[156] = (if (entry.isDirectory) '5' else '0').code.toByte()

      var checksum = 0
      for (b in header) checksum += (b.toInt() and 0xFF)
      val chkStr = checksum.toString(8).padStart(6, '0')
      val chkBytes = chkStr.toByteArray(Charsets.US_ASCII)
      System.arraycopy(chkBytes, 0, header, 148, chkBytes.size.coerceAtMost(6))
      header[154] = 0
      header[155] = ' '.code.toByte()

      out.write(header)
      if (!entry.isDirectory && entry.bytes.isNotEmpty()) {
        out.write(entry.bytes)
        val remainder = entry.bytes.size % 512
        if (remainder != 0) {
          out.write(ByteArray(512 - remainder))
        }
      }
    }
    out.write(ByteArray(1024))
    out.flush()
  }

  private fun collectFilePayloadsRecursively(
    file: File,
    parentArchivePrefix: String,
    payloads: MutableList<RawEntryPayload>,
    usedPaths: MutableSet<String>,
  ) {
    if (!file.exists()) return
    val basePath = if (parentArchivePrefix.isEmpty()) file.name else "$parentArchivePrefix/${file.name}"
    val uniqueArchivePath = deduplicateArchivePath(basePath, file.isDirectory, usedPaths)
    usedPaths.add(uniqueArchivePath)

    if (file.isDirectory) {
      val children = file.listFiles()?.sortedBy { it.name.lowercase(Locale.ROOT) } ?: emptyList()
      if (children.isEmpty()) {
        payloads.add(
          RawEntryPayload(
            archivePath = uniqueArchivePath,
            isDirectory = true,
            bytes = ByteArray(0),
            lastModifiedMs = file.lastModified(),
          )
        )
      } else {
        for (child in children) {
          collectFilePayloadsRecursively(
            file = child,
            parentArchivePrefix = uniqueArchivePath,
            payloads = payloads,
            usedPaths = usedPaths,
          )
        }
      }
    } else {
      val bytes = file.readBytes()
      payloads.add(
        RawEntryPayload(
          archivePath = uniqueArchivePath,
          isDirectory = false,
          bytes = bytes,
          lastModifiedMs = file.lastModified(),
          crc32 = computeCrc32(bytes),
        )
      )
    }
  }

  private fun deduplicateArchivePath(
    desiredPath: String,
    isDirectory: Boolean,
    usedPaths: Set<String>,
  ): String {
    val clean = normalizeArchivePath(desiredPath)
    if (clean !in usedPaths) return clean
    val parent = parentArchivePathOf(clean)
    val leaf = leafNameOf(clean)
    val dotIdx = if (!isDirectory) leaf.lastIndexOf('.') else -1
    val stem = if (dotIdx > 0) leaf.substring(0, dotIdx) else leaf
    val ext = if (dotIdx > 0) leaf.substring(dotIdx) else ""
    var counter = 2
    while (true) {
      val candidateLeaf = "$stem ($counter)$ext"
      val candidatePath = if (parent.isEmpty()) candidateLeaf else "$parent/$candidateLeaf"
      if (candidatePath !in usedPaths) return candidatePath
      counter++
    }
  }

  private fun resolveUniqueDestinationFile(target: File): File {
    val parent = target.parentFile ?: return target
    val name = target.name
    val dotIdx = name.lastIndexOf('.')
    val stem = if (dotIdx > 0) name.substring(0, dotIdx) else name
    val ext = if (dotIdx > 0) name.substring(dotIdx) else ""
    var counter = 2
    while (true) {
      val candidate = File(parent, "$stem ($counter)$ext")
      if (!candidate.exists()) return candidate
      counter++
    }
  }

  private fun deriveGzipInnerFileName(gzipFileName: String): String {
    return when {
      gzipFileName.endsWith(".gz", ignoreCase = true) && gzipFileName.length > 3 -> {
        gzipFileName.dropLast(3)
      }
      else -> "${gzipFileName}_decompressed"
    }
  }

  private fun computeCrc32(bytes: ByteArray): Long {
    val crc = CRC32()
    crc.update(bytes)
    return crc.value
  }

  private fun readLimitedBytes(input: InputStream, maxBytes: Int): ByteArray {
    val out = ByteArrayOutputStream()
    val buf = ByteArray(8192)
    var remaining = maxBytes
    while (remaining > 0) {
      val r = input.read(buf, 0, minOf(buf.size, remaining))
      if (r <= 0) break
      out.write(buf, 0, r)
      remaining -= r
    }
    return out.toByteArray()
  }

  private fun readFullyOrEof(input: InputStream, buffer: ByteArray): Int {
    var total = 0
    while (total < buffer.size) {
      val r = input.read(buffer, total, buffer.size - total)
      if (r <= 0) break
      total += r
    }
    return total
  }

  private fun skipFullyOrEof(input: InputStream, bytesToSkip: Long) {
    var remaining = bytesToSkip
    val scratch = ByteArray(4096)
    while (remaining > 0L) {
      val toRead = minOf(remaining, scratch.size.toLong()).toInt()
      val r = input.read(scratch, 0, toRead)
      if (r <= 0) break
      remaining -= r
    }
  }

  private fun skipAndCrcStream(input: InputStream, bytesToRead: Long, crc: CRC32) {
    var remaining = bytesToRead
    val scratch = ByteArray(8192)
    while (remaining > 0L) {
      val toRead = minOf(remaining, scratch.size.toLong()).toInt()
      val r = input.read(scratch, 0, toRead)
      if (r <= 0) break
      crc.update(scratch, 0, r)
      remaining -= r
    }
  }

  private fun extractNullTerminatedString(buf: ByteArray, offset: Int, length: Int): String {
    var end = offset
    val limit = (offset + length).coerceAtMost(buf.size)
    while (end < limit && buf[end] != 0.toByte()) {
      end++
    }
    return String(buf, offset, end - offset, Charsets.UTF_8)
  }

  private fun writeOctalBytes(octal: String, dest: ByteArray, offset: Int, length: Int) {
    val bytes = octal.toByteArray(Charsets.US_ASCII)
    val count = minOf(bytes.size, length - 1)
    System.arraycopy(bytes, 0, dest, offset, count)
  }

  private fun sortWithinGroup(
    items: List<ArchiveEntryItem>,
    sortOrder: FileSortOrder,
  ): List<ArchiveEntryItem> {
    val comparator = when (sortOrder) {
      FileSortOrder.NAME_ASC -> compareBy(String.CASE_INSENSITIVE_ORDER) { item: ArchiveEntryItem -> item.name }
      FileSortOrder.NAME_DESC -> compareByDescending(String.CASE_INSENSITIVE_ORDER) { item: ArchiveEntryItem -> item.name }
      FileSortOrder.DATE_DESC -> compareByDescending<ArchiveEntryItem> { it.lastModifiedMs }
        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      FileSortOrder.DATE_ASC -> compareBy<ArchiveEntryItem> { it.lastModifiedMs }
        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      FileSortOrder.SIZE_DESC -> compareByDescending<ArchiveEntryItem> { it.sizeBytes }
        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      FileSortOrder.SIZE_ASC -> compareBy<ArchiveEntryItem> { it.sizeBytes }
        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      FileSortOrder.TYPE_ASC -> compareBy<ArchiveEntryItem, String>(String.CASE_INSENSITIVE_ORDER) { it.extension }
        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
    }
    return items.sortedWith(comparator)
  }

  private fun getTypeCategoryTitle(item: ArchiveEntryItem): String {
    return when {
      item.isDirectory -> "Folders"
      isImageEntry(item) -> "Images"
      isVideoEntry(item) -> "Videos"
      isAudioEntry(item) -> "Audio"
      isArchiveEntry(item) -> "Archives"
      isCodeEntry(item) -> "Code"
      isTextEntry(item) -> "Documents"
      item.extension in setOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "epub", "rtf", "odt") -> "Documents"
      else -> "Other"
    }
  }

  private fun getDateCategoryTitle(timestampMs: Long): String {
    if (timestampMs <= 0L) return "Older"
    val now = Calendar.getInstance()
    val startOfToday = (now.clone() as Calendar).apply {
      set(Calendar.HOUR_OF_DAY, 0)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val oneDayMs = 24L * 60 * 60 * 1000
    val startOfYesterday = startOfToday - oneDayMs
    val startOfWeek = startOfToday - 6 * oneDayMs
    val startOfMonth = startOfToday - 29 * oneDayMs

    return when {
      timestampMs >= startOfToday -> "Today"
      timestampMs >= startOfYesterday -> "Yesterday"
      timestampMs >= startOfWeek -> "This Week"
      timestampMs >= startOfMonth -> "This Month"
      else -> "Older"
    }
  }

  private fun getSizeCategoryTitle(item: ArchiveEntryItem): String {
    if (item.isDirectory) return "Folders"
    val mb = 1024L * 1024L
    return when {
      item.sizeBytes > 100L * mb -> "Huge (> 100 MB)"
      item.sizeBytes >= 10L * mb -> "Large (10 – 100 MB)"
      item.sizeBytes >= 1L * mb -> "Medium (1 – 10 MB)"
      else -> "Small (< 1 MB)"
    }
  }

  /**
   * Pure Kotlin minimal valid 24-bit RGB PNG generator so sample archives work in both JVM tests
   * and Android runtime without native Bitmap dependencies.
   */
  internal fun createMinimalSolidPngBytes(width: Int, height: Int, r: Int, g: Int, b: Int): ByteArray {
    val out = ByteArrayOutputStream()
    // PNG Signature
    out.write(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))

    fun writeChunk(type: String, data: ByteArray) {
      val lenBytes = ByteBuffer.allocate(4).putInt(data.size).array()
      val typeBytes = type.toByteArray(Charsets.US_ASCII)
      out.write(lenBytes)
      out.write(typeBytes)
      out.write(data)
      val crc = CRC32()
      crc.update(typeBytes)
      crc.update(data)
      val crcBytes = ByteBuffer.allocate(4).putInt(crc.value.toInt()).array()
      out.write(crcBytes)
    }

    // IHDR chunk
    val ihdr = ByteBuffer.allocate(13)
      .putInt(width)
      .putInt(height)
      .put(8.toByte()) // bit depth 8
      .put(2.toByte()) // color type 2 (Truecolor RGB)
      .put(0.toByte()) // compression
      .put(0.toByte()) // filter
      .put(0.toByte()) // interlace
      .array()
    writeChunk("IHDR", ihdr)

    // Raw scanlines: each row has 1 filter byte (0) + width * 3 RGB bytes
    val rawScanlines = ByteArray(height * (1 + width * 3))
    var offset = 0
    for (y in 0 until height) {
      rawScanlines[offset++] = 0
      for (x in 0 until width) {
        val boost = if ((x + y) % 8 < 4) 18 else 0
        rawScanlines[offset++] = (r + boost).coerceIn(0, 255).toByte()
        rawScanlines[offset++] = (g + boost).coerceIn(0, 255).toByte()
        rawScanlines[offset++] = (b + boost).coerceIn(0, 255).toByte()
      }
    }

    val compressedData = ByteArrayOutputStream()
    val deflater = Deflater(Deflater.DEFAULT_COMPRESSION)
    try {
      deflater.setInput(rawScanlines)
      deflater.finish()
      val buf = ByteArray(1024)
      while (!deflater.finished()) {
        val count = deflater.deflate(buf)
        if (count > 0) compressedData.write(buf, 0, count)
      }
    } finally {
      deflater.end()
    }
    writeChunk("IDAT", compressedData.toByteArray())
    writeChunk("IEND", ByteArray(0))
    return out.toByteArray()
  }
}
