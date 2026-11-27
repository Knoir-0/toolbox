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
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Calendar
import java.util.Locale
import java.util.zip.CRC32
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.Inflater
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Supported archive formats for viewing, extracting, and creating archives.
 */
enum class ArchiveFormat(
  val displayName: String,
  val extension: String,
  val mimeType: String,
  val aliases: Set<String>,
  val supportsMultiEntryWrite: Boolean,
  val supportsPasswordEncryption: Boolean = false,
) {
  ZIP(
    displayName = "ZIP",
    extension = "zip",
    mimeType = "application/zip",
    aliases = setOf("zip", "jar", "apk", "cbz"),
    supportsMultiEntryWrite = true,
    supportsPasswordEncryption = true,
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
 * Supported password encryption methods for `.zip` archives.
 */
enum class ArchiveEncryptionMethod(
  val displayName: String,
  val shortLabel: String,
  val description: String,
  val aesStrengthByte: Int = 0,
  val saltLength: Int = 0,
  val keyLength: Int = 0,
) {
  NONE(
    displayName = "None",
    shortLabel = "None",
    description = "Unencrypted standard archive",
  ),
  AES_256(
    displayName = "AES-256 (WinZip AE-2)",
    shortLabel = "AES-256",
    description = "Strong 256-bit AES encryption with HMAC-SHA1 verification",
    aesStrengthByte = 3,
    saltLength = 16,
    keyLength = 32,
  ),
  AES_192(
    displayName = "AES-192 (WinZip AE-2)",
    shortLabel = "AES-192",
    description = "192-bit AES encryption with HMAC-SHA1 verification",
    aesStrengthByte = 2,
    saltLength = 12,
    keyLength = 24,
  ),
  AES_128(
    displayName = "AES-128 (WinZip AE-2)",
    shortLabel = "AES-128",
    description = "128-bit AES encryption with HMAC-SHA1 verification",
    aesStrengthByte = 1,
    saltLength = 8,
    keyLength = 16,
  ),
  ZIP_CRYPTO(
    displayName = "ZipCrypto (Legacy)",
    shortLabel = "ZipCrypto",
    description = "Traditional PKWARE password protection for legacy extractors",
  );

  val isAes: Boolean
    get() = aesStrengthByte in 1..3

  companion object {
    val creatableMethods: List<ArchiveEncryptionMethod> = listOf(AES_256, ZIP_CRYPTO)

    fun fromAesStrength(strength: Int): ArchiveEncryptionMethod = when (strength) {
      1 -> AES_128
      2 -> AES_192
      3 -> AES_256
      else -> AES_256
    }
  }
}

/**
 * Thrown when an encrypted archive entry is accessed without a password or with an incorrect password.
 */
class InvalidArchivePasswordException(
  message: String = "Incorrect password",
  val isMissingPassword: Boolean = false,
) : Exception(message)

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
  val isEncrypted: Boolean = false,
  val encryptionMethod: ArchiveEncryptionMethod = ArchiveEncryptionMethod.NONE,
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
  val isEncrypted: Boolean = false,
  val encryptionMethod: ArchiveEncryptionMethod = ArchiveEncryptionMethod.NONE,
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
  val passwordRequired: Boolean = false,
  val invalidPassword: Boolean = false,
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
      val rawEntries = try {
        when (format) {
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
      } catch (_: Exception) {
        emptyList()
      }

      val completeHierarchy = buildCompleteEntryHierarchy(rawEntries, archiveFile.lastModified())
      val files = completeHierarchy.filter { !it.isDirectory }
      val folders = completeHierarchy.filter { it.isDirectory }
      val totalUncompressed = files.sumOf { it.sizeBytes.coerceAtLeast(0L) }
      val totalCompressed = files.sumOf { it.compressedSizeBytes.coerceAtLeast(0L) }
        .takeIf { it > 0L } ?: archiveFile.length()
      val firstEncryptedFile = files.firstOrNull { it.isEncrypted }

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
          isEncrypted = firstEncryptedFile != null,
          encryptionMethod = firstEncryptedFile?.encryptionMethod ?: ArchiveEncryptionMethod.NONE,
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
   * Verifies whether [password] can unlock the encrypted entries in [archiveFile].
   * Returns `Result.success(true)` when valid (or when the archive is not encrypted),
   * `Result.success(false)` when [password] is incorrect, or `Result.failure` on I/O errors.
   */
  fun verifyArchivePassword(
    archiveFile: File,
    password: String,
  ): Result<Boolean> {
    if (!archiveFile.exists() || !archiveFile.isFile) {
      return Result.failure(IllegalArgumentException("Archive file does not exist"))
    }
    val format = detectFormat(archiveFile)
      ?: return Result.failure(IllegalArgumentException("Unsupported archive format"))
    if (format != ArchiveFormat.ZIP) {
      return Result.success(true)
    }

    return try {
      val cdEntries = parseZipCentralDirectory(archiveFile)
      val encryptedEntry = cdEntries.firstOrNull { !it.isDirectory && it.isEncrypted }
        ?: return Result.success(true)
      if (password.isEmpty()) {
        return Result.success(false)
      }
      RandomAccessFile(archiveFile, "r").use { raf ->
        readAndDecodeZipEntryBytes(
          raf = raf,
          entry = encryptedEntry,
          maxBytes = Int.MAX_VALUE,
          password = password,
        )
      }
      Result.success(true)
    } catch (_: InvalidArchivePasswordException) {
      Result.success(false)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Reads the raw bytes of a single file entry from [archiveFile], up to [maxBytes],
   * returning a [Result] that distinguishes [InvalidArchivePasswordException] from other errors.
   */
  fun readEntryBytesResult(
    archiveFile: File,
    archivePath: String,
    maxBytes: Int = 10 * 1024 * 1024,
    password: String? = null,
  ): Result<ByteArray> {
    if (!archiveFile.exists() || !archiveFile.isFile) {
      return Result.failure(IllegalArgumentException("Archive file not found"))
    }
    val targetPath = normalizeArchivePath(archivePath)
    if (targetPath.isEmpty()) {
      return Result.failure(IllegalArgumentException("Invalid archive entry path"))
    }
    val format = detectFormat(archiveFile)
      ?: return Result.failure(IllegalArgumentException("Unsupported archive format"))

    return try {
      val bytes: ByteArray? = when (format) {
        ArchiveFormat.ZIP -> {
          val cdEntries = parseZipCentralDirectory(archiveFile)
          if (cdEntries.isNotEmpty()) {
            val match = cdEntries.firstOrNull { !it.isDirectory && normalizeArchivePath(it.name) == targetPath }
            if (match != null) {
              RandomAccessFile(archiveFile, "r").use { raf ->
                readAndDecodeZipEntryBytes(raf, match, maxBytes, password)
              }
            } else {
              null
            }
          } else {
            ZipInputStream(BufferedInputStream(FileInputStream(archiveFile))).use { zis ->
              var found: ByteArray? = null
              var entry = zis.nextEntry
              while (entry != null) {
                if (!entry.isDirectory && normalizeArchivePath(entry.name) == targetPath) {
                  found = readLimitedBytes(zis, maxBytes)
                  break
                }
                zis.closeEntry()
                entry = zis.nextEntry
              }
              found
            }
          }
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

      if (bytes != null) {
        Result.success(bytes)
      } else {
        Result.failure(NoSuchElementException("Entry not found: $targetPath"))
      }
    } catch (e: InvalidArchivePasswordException) {
      Result.failure(e)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Reads the raw bytes of a single file entry from [archiveFile], up to [maxBytes].
   */
  fun readEntryBytes(
    archiveFile: File,
    archivePath: String,
    maxBytes: Int = 10 * 1024 * 1024,
    password: String? = null,
  ): ByteArray? {
    return readEntryBytesResult(archiveFile, archivePath, maxBytes, password).getOrNull()
  }

  /**
   * Reads the UTF-8 text content of a single file entry inside [archiveFile].
   */
  fun readEntryText(
    archiveFile: File,
    archivePath: String,
    maxBytes: Int = 512 * 1024,
    password: String? = null,
  ): String? {
    val bytes = readEntryBytes(archiveFile, archivePath, maxBytes, password) ?: return null
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
    password: String? = null,
  ): File? {
    if (entry.isDirectory) return null
    val bytes = readEntryBytes(archiveFile, entry.archivePath, password = password) ?: return null
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
    password: String? = null,
  ): ArchiveOperationResult {
    return extractEntries(
      archiveFile = archiveFile,
      selectedArchivePaths = emptySet(),
      destinationDir = destinationDir,
      preservePaths = true,
      overwrite = overwrite,
      extractEntireArchive = true,
      password = password,
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
    password: String? = null,
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

      val rawPayloads = readAllRawPayloads(
        archiveFile = archiveFile,
        format = format,
        password = password,
        filterPredicate = ::matchesSelection,
      )
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
    } catch (e: InvalidArchivePasswordException) {
      ArchiveOperationResult(
        success = false,
        message = if (e.isMissingPassword) {
          "Password required to extract encrypted archive"
        } else {
          "Incorrect password for ${archiveFile.name}"
        },
        passwordRequired = e.isMissingPassword,
        invalidPassword = !e.isMissingPassword,
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
   * When [password] is non-empty and [format] is [ArchiveFormat.ZIP], file entries are encrypted using
   * [encryptionMethod] (defaults to [ArchiveEncryptionMethod.AES_256]).
   */
  fun createArchive(
    sourceFiles: List<File>,
    outputFile: File,
    format: ArchiveFormat = ArchiveFormat.ZIP,
    compressionLevel: ArchiveCompressionLevel = ArchiveCompressionLevel.BALANCED,
    password: String? = null,
    encryptionMethod: ArchiveEncryptionMethod = ArchiveEncryptionMethod.AES_256,
  ): ArchiveOperationResult {
    val validSources = sourceFiles.filter { it.exists() }
    if (validSources.isEmpty()) {
      return ArchiveOperationResult(
        success = false,
        message = "Please select at least one existing file or folder to compress",
      )
    }

    val effectivePassword = password?.takeIf { it.isNotEmpty() }
    if (effectivePassword != null && !format.supportsPasswordEncryption) {
      return ArchiveOperationResult(
        success = false,
        message = "${format.displayName} does not support password encryption — please choose ZIP",
      )
    }
    val effectiveEncryption = if (effectivePassword != null) {
      if (encryptionMethod == ArchiveEncryptionMethod.NONE) ArchiveEncryptionMethod.AES_256 else encryptionMethod
    } else {
      ArchiveEncryptionMethod.NONE
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
        password = effectivePassword,
        encryptionMethod = effectiveEncryption,
      )

      val fileCount = payloads.count { !it.isDirectory }
      val totalUncompressed = payloads.filter { !it.isDirectory }.sumOf { it.bytes.size.toLong() }
      val encBadge = if (effectivePassword != null) " • ${effectiveEncryption.shortLabel} encrypted" else ""

      ArchiveOperationResult(
        success = true,
        outputFile = outputFile,
        extractedFilesCount = fileCount,
        totalBytesProcessed = totalUncompressed,
        message = "Created ${outputFile.name} with $fileCount ${if (fileCount == 1) "file" else "files"} (${FileManagerEngine.formatFileSize(outputFile.length())}$encBadge)",
      )
    } catch (e: Exception) {
      ArchiveOperationResult(
        success = false,
        message = "Failed to create archive: ${e.message ?: "Unknown error"}",
      )
    }
  }

  /**
   * Encrypts an existing `.zip` archive with [newPassword], updates its password/encryption method,
   * or removes password protection when [newPassword] is `null` or empty.
   */
  fun updateArchivePassword(
    archiveFile: File,
    currentPassword: String? = null,
    newPassword: String? = null,
    encryptionMethod: ArchiveEncryptionMethod = ArchiveEncryptionMethod.AES_256,
    compressionLevel: ArchiveCompressionLevel = ArchiveCompressionLevel.BALANCED,
  ): ArchiveOperationResult {
    if (!archiveFile.exists() || !archiveFile.isFile) {
      return ArchiveOperationResult(success = false, message = "Archive does not exist")
    }
    val format = detectFormat(archiveFile)
      ?: return ArchiveOperationResult(success = false, message = "Unsupported archive format")
    if (!format.supportsPasswordEncryption) {
      return ArchiveOperationResult(
        success = false,
        message = "${format.displayName} archives do not support password encryption",
      )
    }

    val targetPassword = newPassword?.takeIf { it.isNotEmpty() }
    val targetMethod = if (targetPassword != null) {
      if (encryptionMethod == ArchiveEncryptionMethod.NONE) ArchiveEncryptionMethod.AES_256 else encryptionMethod
    } else {
      ArchiveEncryptionMethod.NONE
    }

    return try {
      val existingPayloads = readAllRawPayloads(archiveFile, format, password = currentPassword)
      val tempFile = File(
        archiveFile.parentFile ?: archiveFile.absoluteFile.parentFile,
        ".${archiveFile.name}.tmp_${System.currentTimeMillis()}",
      )
      writeRawPayloadsToFile(
        payloads = existingPayloads,
        outputFile = tempFile,
        format = format,
        compressionLevel = compressionLevel,
        password = targetPassword,
        encryptionMethod = targetMethod,
      )
      if (archiveFile.exists()) archiveFile.delete()
      tempFile.renameTo(archiveFile)

      val msg = if (targetPassword != null) {
        "Encrypted ${archiveFile.name} with ${targetMethod.shortLabel} password protection"
      } else {
        "Removed password protection from ${archiveFile.name}"
      }
      ArchiveOperationResult(
        success = true,
        outputFile = archiveFile,
        extractedFilesCount = existingPayloads.count { !it.isDirectory },
        message = msg,
      )
    } catch (e: InvalidArchivePasswordException) {
      ArchiveOperationResult(
        success = false,
        message = if (e.isMissingPassword) {
          "Current password required to modify encrypted archive"
        } else {
          "Incorrect current password"
        },
        passwordRequired = e.isMissingPassword,
        invalidPassword = !e.isMissingPassword,
      )
    } catch (e: Exception) {
      ArchiveOperationResult(
        success = false,
        message = "Failed to update archive encryption: ${e.message ?: "Unknown error"}",
      )
    }
  }

  /**
   * Adds one or more [filesToAdd] (files or folders) into an existing writable archive ([ZIP], [TAR], [TAR_GZ])
   * under [targetVirtualDir], automatically preserving existing ZIP password encryption when present.
   */
  fun addFilesToArchive(
    archiveFile: File,
    filesToAdd: List<File>,
    targetVirtualDir: String = "",
    compressionLevel: ArchiveCompressionLevel = ArchiveCompressionLevel.BALANCED,
    password: String? = null,
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
      val existingEncryption = detectExistingZipEncryptionMethod(archiveFile, format)
      val existingPayloads = readAllRawPayloads(archiveFile, format, password = password).toMutableList()
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
      writeRawPayloadsToFile(
        payloads = combined,
        outputFile = tempFile,
        format = format,
        compressionLevel = compressionLevel,
        password = if (existingEncryption != ArchiveEncryptionMethod.NONE) password else null,
        encryptionMethod = existingEncryption,
      )
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
    } catch (e: InvalidArchivePasswordException) {
      ArchiveOperationResult(
        success = false,
        message = if (e.isMissingPassword) "Unlock archive with password before adding files" else "Incorrect archive password",
        passwordRequired = e.isMissingPassword,
        invalidPassword = !e.isMissingPassword,
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
    password: String? = null,
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
      val existingEncryption = detectExistingZipEncryptionMethod(archiveFile, format)
      val existingPayloads = readAllRawPayloads(archiveFile, format, password = password).toMutableList()
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
      writeRawPayloadsToFile(
        payloads = existingPayloads,
        outputFile = tempFile,
        format = format,
        compressionLevel = ArchiveCompressionLevel.BALANCED,
        password = if (existingEncryption != ArchiveEncryptionMethod.NONE) password else null,
        encryptionMethod = existingEncryption,
      )
      if (archiveFile.exists()) archiveFile.delete()
      tempFile.renameTo(archiveFile)

      ArchiveOperationResult(
        success = true,
        outputFile = archiveFile,
        message = "Created folder \"${folderName.trim()}\" in ${archiveFile.name}",
      )
    } catch (e: InvalidArchivePasswordException) {
      ArchiveOperationResult(
        success = false,
        message = if (e.isMissingPassword) "Unlock archive with password before creating folders" else "Incorrect archive password",
        passwordRequired = e.isMissingPassword,
        invalidPassword = !e.isMissingPassword,
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
    password: String? = null,
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
      val existingEncryption = detectExistingZipEncryptionMethod(archiveFile, format)
      val existingPayloads = readAllRawPayloads(archiveFile, format, password = password)
      val remainingPayloads = existingPayloads.filterNot { payload ->
        val clean = normalizeArchivePath(payload.archivePath)
        cleanTargets.any { target -> clean == target || clean.startsWith("$target/") }
      }

      val removedCount = existingPayloads.size - remainingPayloads.size
      val tempFile = File(archiveFile.parentFile ?: archiveFile.absoluteFile.parentFile, ".${archiveFile.name}.tmp_${System.currentTimeMillis()}")
      writeRawPayloadsToFile(
        payloads = remainingPayloads,
        outputFile = tempFile,
        format = format,
        compressionLevel = ArchiveCompressionLevel.BALANCED,
        password = if (existingEncryption != ArchiveEncryptionMethod.NONE) password else null,
        encryptionMethod = existingEncryption,
      )
      if (archiveFile.exists()) archiveFile.delete()
      tempFile.renameTo(archiveFile)

      ArchiveOperationResult(
        success = true,
        outputFile = archiveFile,
        extractedFilesCount = removedCount,
        message = "Removed ${cleanTargets.size} ${if (cleanTargets.size == 1) "item" else "items"} from ${archiveFile.name}",
      )
    } catch (e: InvalidArchivePasswordException) {
      ArchiveOperationResult(
        success = false,
        message = if (e.isMissingPassword) "Unlock archive with password before deleting entries" else "Incorrect archive password",
        passwordRequired = e.isMissingPassword,
        invalidPassword = !e.isMissingPassword,
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
    password: String? = null,
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
      val existingEncryption = detectExistingZipEncryptionMethod(archiveFile, format)
      val existingPayloads = readAllRawPayloads(archiveFile, format, password = password)
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
      writeRawPayloadsToFile(
        payloads = updatedPayloads,
        outputFile = tempFile,
        format = format,
        compressionLevel = ArchiveCompressionLevel.BALANCED,
        password = if (existingEncryption != ArchiveEncryptionMethod.NONE) password else null,
        encryptionMethod = existingEncryption,
      )
      if (archiveFile.exists()) archiveFile.delete()
      tempFile.renameTo(archiveFile)

      ArchiveOperationResult(
        success = true,
        outputFile = archiveFile,
        message = "Renamed to \"${newLeafName.trim()}\"",
      )
    } catch (e: InvalidArchivePasswordException) {
      ArchiveOperationResult(
        success = false,
        message = if (e.isMissingPassword) "Unlock archive with password before renaming entries" else "Incorrect archive password",
        passwordRequired = e.isMissingPassword,
        invalidPassword = !e.isMissingPassword,
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
    val isEncrypted: Boolean = false,
    val encryptionMethod: ArchiveEncryptionMethod = ArchiveEncryptionMethod.NONE,
  )

  private data class ZipCentralDirEntry(
    val name: String,
    val isDirectory: Boolean,
    val gpFlag: Int,
    val compressionMethod: Int,
    val actualCompressionMethod: Int,
    val dosTime: Int,
    val dosDate: Int,
    val lastModifiedMs: Long,
    val crc32: Long,
    val compressedSize: Long,
    val uncompressedSize: Long,
    val localHeaderOffset: Long,
    val isEncrypted: Boolean,
    val encryptionMethod: ArchiveEncryptionMethod,
    val aesVendorVersion: Int,
  )

  private fun detectExistingZipEncryptionMethod(
    archiveFile: File,
    format: ArchiveFormat,
  ): ArchiveEncryptionMethod {
    if (format != ArchiveFormat.ZIP || !archiveFile.exists()) return ArchiveEncryptionMethod.NONE
    val entries = parseZipCentralDirectory(archiveFile)
    return entries.firstOrNull { !it.isDirectory && it.isEncrypted }?.encryptionMethod
      ?: ArchiveEncryptionMethod.NONE
  }

  private fun readZipMetadataEntries(archiveFile: File): List<ParsedRawMetadata> {
    val cdEntries = parseZipCentralDirectory(archiveFile)
    if (cdEntries.isNotEmpty()) {
      val list = mutableListOf<ParsedRawMetadata>()
      for (entry in cdEntries) {
        if (entry.name.replace('\\', '/').split('/').any { it == ".." }) continue
        val normPath = normalizeArchivePath(entry.name)
        if (normPath.isEmpty()) continue
        list.add(
          ParsedRawMetadata(
            archivePath = normPath,
            isDirectory = entry.isDirectory,
            uncompressedSize = if (entry.isDirectory) 0L else entry.uncompressedSize.coerceAtLeast(0L),
            compressedSize = if (entry.isDirectory) 0L else entry.compressedSize.coerceAtLeast(0L),
            lastModifiedMs = entry.lastModifiedMs.takeIf { it > 0L } ?: archiveFile.lastModified(),
            crc32 = entry.crc32.coerceAtLeast(0L),
            isEncrypted = !entry.isDirectory && entry.isEncrypted,
            encryptionMethod = if (!entry.isDirectory && entry.isEncrypted) entry.encryptionMethod else ArchiveEncryptionMethod.NONE,
          )
        )
      }
      return list
    }

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
        isEncrypted = raw.isEncrypted,
        encryptionMethod = raw.encryptionMethod,
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
    password: String? = null,
    filterPredicate: ((String) -> Boolean)? = null,
  ): List<RawEntryPayload> {
    val list = mutableListOf<RawEntryPayload>()
    when (format) {
      ArchiveFormat.ZIP -> {
        val cdEntries = parseZipCentralDirectory(archiveFile)
        if (cdEntries.isNotEmpty()) {
          RandomAccessFile(archiveFile, "r").use { raf ->
            for (entry in cdEntries) {
              if (entry.name.replace('\\', '/').split('/').any { it == ".." }) continue
              val clean = normalizeArchivePath(entry.name)
              if (clean.isEmpty()) continue
              if (filterPredicate != null && !filterPredicate(clean)) continue
              val bytes = if (entry.isDirectory) {
                ByteArray(0)
              } else {
                readAndDecodeZipEntryBytes(raf, entry, Int.MAX_VALUE, password)
              }
              list.add(
                RawEntryPayload(
                  archivePath = clean,
                  isDirectory = entry.isDirectory,
                  bytes = bytes,
                  lastModifiedMs = entry.lastModifiedMs.takeIf { it > 0L } ?: archiveFile.lastModified(),
                  crc32 = if (entry.isDirectory) 0L else computeCrc32(bytes),
                )
              )
            }
          }
        } else {
          ZipInputStream(BufferedInputStream(FileInputStream(archiveFile))).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
              val rawName = entry.name
              if (!rawName.replace('\\', '/').split('/').any { it == ".." }) {
                val isDir = entry.isDirectory || rawName.endsWith("/") || rawName.endsWith("\\")
                val clean = normalizeArchivePath(rawName)
                if (clean.isNotEmpty() && (filterPredicate == null || filterPredicate(clean))) {
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
    password: String? = null,
    encryptionMethod: ArchiveEncryptionMethod = ArchiveEncryptionMethod.NONE,
  ) {
    when (format) {
      ArchiveFormat.ZIP -> {
        if (!password.isNullOrEmpty() && encryptionMethod != ArchiveEncryptionMethod.NONE) {
          writeEncryptedZipPayloads(
            payloads = payloads,
            outputFile = outputFile,
            compressionLevel = compressionLevel,
            password = password,
            encryptionMethod = encryptionMethod,
          )
          return
        }
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

  // ============================================================================
  // ZIP CENTRAL DIRECTORY PARSER & AES-256 / ZIPCRYPTO ENCRYPTION CODECS
  // ============================================================================

  private const val ZIP_SIG_LOCAL_FILE_HEADER = 0x04034b50
  private const val ZIP_SIG_CENTRAL_FILE_HEADER = 0x02014b50
  private const val ZIP_SIG_EOCD = 0x06054b50
  private const val ZIP_METHOD_STORED = 0
  private const val ZIP_METHOD_DEFLATED = 8
  private const val ZIP_METHOD_WINZIP_AES = 99
  private const val ZIP_EXTRA_ZIP64 = 0x0001
  private const val ZIP_EXTRA_WINZIP_AES = 0x9901
  private const val WINZIP_PBKDF2_ITERATIONS = 1000
  private const val WINZIP_AUTH_CODE_BYTES = 10

  private val ZIP_CRC32_TABLE = IntArray(256) { n ->
    var c = n
    repeat(8) {
      c = if ((c and 1) != 0) (c ushr 1) xor 0xEDB88320.toInt() else (c ushr 1)
    }
    c
  }

  private class ZipCryptoState(passwordBytes: ByteArray) {
    private var key0 = 0x12345678
    private var key1 = 0x23456789
    private var key2 = 0x34567890

    init {
      for (b in passwordBytes) {
        updateKeys(b.toInt() and 0xFF)
      }
    }

    private fun crc32Step(crc: Int, byteVal: Int): Int {
      return (crc ushr 8) xor ZIP_CRC32_TABLE[(crc xor byteVal) and 0xFF]
    }

    private fun updateKeys(byteVal: Int) {
      key0 = crc32Step(key0, byteVal and 0xFF)
      key1 = (key1 + (key0 and 0xFF)) * 134775813 + 1
      key2 = crc32Step(key2, (key1 ushr 24) and 0xFF)
    }

    private fun streamByte(): Int {
      val temp = (key2 or 2) and 0xFFFF
      return ((temp * (temp xor 1)) ushr 8) and 0xFF
    }

    fun encryptBytes(plain: ByteArray): ByteArray {
      val out = ByteArray(plain.size)
      for (i in plain.indices) {
        val p = plain[i].toInt() and 0xFF
        val c = p xor streamByte()
        updateKeys(p)
        out[i] = c.toByte()
      }
      return out
    }

    fun decryptBytes(cipher: ByteArray, offset: Int = 0, length: Int = cipher.size - offset): ByteArray {
      val out = ByteArray(length)
      for (i in 0 until length) {
        val c = cipher[offset + i].toInt() and 0xFF
        val p = (c xor streamByte()) and 0xFF
        updateKeys(p)
        out[i] = p.toByte()
      }
      return out
    }
  }

  private class CountingOutputStream(private val delegate: OutputStream) : OutputStream() {
    var bytesWritten: Long = 0L
      private set

    override fun write(b: Int) {
      delegate.write(b)
      bytesWritten++
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
      delegate.write(b, off, len)
      bytesWritten += len
    }

    override fun flush() = delegate.flush()
    override fun close() = delegate.close()
  }

  private data class WrittenCentralDirRecord(
    val versionMadeBy: Int,
    val versionNeeded: Int,
    val gpFlag: Int,
    val compressionMethod: Int,
    val dosTime: Int,
    val dosDate: Int,
    val crc32: Long,
    val compressedSize: Long,
    val uncompressedSize: Long,
    val nameBytes: ByteArray,
    val extraBytes: ByteArray,
    val externalAttrs: Long,
    val localHeaderOffset: Long,
  )

  private fun parseZipCentralDirectory(archiveFile: File): List<ZipCentralDirEntry> {
    if (!archiveFile.exists() || archiveFile.length() < 22L) return emptyList()
    return try {
      RandomAccessFile(archiveFile, "r").use { raf ->
        val fileLen = raf.length()
        val tailLen = minOf(fileLen, 65_557L).toInt()
        val tailOffset = fileLen - tailLen
        val tailBuf = ByteArray(tailLen)
        raf.seek(tailOffset)
        raf.readFully(tailBuf)

        var eocdPos = -1
        for (i in tailLen - 22 downTo 0) {
          if (readLeInt32(tailBuf, i) == ZIP_SIG_EOCD) {
            val commentLen = readLeUInt16(tailBuf, i + 20)
            if (i + 22 + commentLen <= tailLen) {
              eocdPos = i
              break
            }
          }
        }
        if (eocdPos < 0) return emptyList()

        val cdSize = readLeUInt32(tailBuf, eocdPos + 12)
        val cdOffset = readLeUInt32(tailBuf, eocdPos + 16)
        if (cdOffset < 0L || cdSize <= 0L || cdOffset + cdSize > fileLen || cdSize > 64L * 1024L * 1024L) {
          return emptyList()
        }

        val cdBuf = ByteArray(cdSize.toInt())
        raf.seek(cdOffset)
        raf.readFully(cdBuf)

        val entries = mutableListOf<ZipCentralDirEntry>()
        var cursor = 0
        while (cursor + 46 <= cdBuf.size) {
          if (readLeInt32(cdBuf, cursor) != ZIP_SIG_CENTRAL_FILE_HEADER) break
          val gpFlag = readLeUInt16(cdBuf, cursor + 8)
          val method = readLeUInt16(cdBuf, cursor + 10)
          val dosTime = readLeUInt16(cdBuf, cursor + 12)
          val dosDate = readLeUInt16(cdBuf, cursor + 14)
          val crc32 = readLeUInt32(cdBuf, cursor + 16)
          var compSize = readLeUInt32(cdBuf, cursor + 20)
          var uncompSize = readLeUInt32(cdBuf, cursor + 24)
          val nameLen = readLeUInt16(cdBuf, cursor + 28)
          val extraLen = readLeUInt16(cdBuf, cursor + 30)
          val commentLen = readLeUInt16(cdBuf, cursor + 32)
          var localOffset = readLeUInt32(cdBuf, cursor + 42)

          val headerTotal = 46 + nameLen + extraLen + commentLen
          if (cursor + headerTotal > cdBuf.size) break

          val name = String(cdBuf, cursor + 46, nameLen, Charsets.UTF_8)
          val extraStart = cursor + 46 + nameLen
          val extraEnd = extraStart + extraLen

          var actualMethod = method
          var aesVendorVersion = 2
          var detectedEncMethod = if ((gpFlag and 0x0001) != 0) {
            if (method == ZIP_METHOD_WINZIP_AES) ArchiveEncryptionMethod.AES_256 else ArchiveEncryptionMethod.ZIP_CRYPTO
          } else {
            ArchiveEncryptionMethod.NONE
          }

          var extPos = extraStart
          while (extPos + 4 <= extraEnd) {
            val extId = readLeUInt16(cdBuf, extPos)
            val extSize = readLeUInt16(cdBuf, extPos + 2)
            if (extPos + 4 + extSize > extraEnd) break
            val dataPos = extPos + 4

            when (extId) {
              ZIP_EXTRA_ZIP64 -> {
                var zPos = dataPos
                if (uncompSize == 0xFFFFFFFFL && zPos + 8 <= dataPos + extSize) {
                  uncompSize = readLeInt64(cdBuf, zPos)
                  zPos += 8
                }
                if (compSize == 0xFFFFFFFFL && zPos + 8 <= dataPos + extSize) {
                  compSize = readLeInt64(cdBuf, zPos)
                  zPos += 8
                }
                if (localOffset == 0xFFFFFFFFL && zPos + 8 <= dataPos + extSize) {
                  localOffset = readLeInt64(cdBuf, zPos)
                }
              }
              ZIP_EXTRA_WINZIP_AES -> {
                if (extSize >= 7) {
                  aesVendorVersion = readLeUInt16(cdBuf, dataPos)
                  val strength = cdBuf[dataPos + 4].toInt() and 0xFF
                  actualMethod = readLeUInt16(cdBuf, dataPos + 5)
                  detectedEncMethod = ArchiveEncryptionMethod.fromAesStrength(strength)
                }
              }
            }
            extPos += 4 + extSize
          }

          val isDir = name.endsWith("/") || name.endsWith("\\")
          val isEncrypted = !isDir && ((gpFlag and 0x0001) != 0 || method == ZIP_METHOD_WINZIP_AES)

          entries.add(
            ZipCentralDirEntry(
              name = name,
              isDirectory = isDir,
              gpFlag = gpFlag,
              compressionMethod = method,
              actualCompressionMethod = actualMethod,
              dosTime = dosTime,
              dosDate = dosDate,
              lastModifiedMs = dosDateTimeToEpochMs(dosTime, dosDate),
              crc32 = crc32,
              compressedSize = compSize,
              uncompressedSize = uncompSize,
              localHeaderOffset = localOffset,
              isEncrypted = isEncrypted,
              encryptionMethod = if (isEncrypted) detectedEncMethod else ArchiveEncryptionMethod.NONE,
              aesVendorVersion = aesVendorVersion,
            )
          )

          cursor += headerTotal
        }
        entries
      }
    } catch (_: Exception) {
      emptyList()
    }
  }

  private fun readAndDecodeZipEntryBytes(
    raf: RandomAccessFile,
    entry: ZipCentralDirEntry,
    maxBytes: Int,
    password: String?,
  ): ByteArray {
    if (entry.isDirectory) return ByteArray(0)
    if (entry.localHeaderOffset < 0L || entry.localHeaderOffset + 30L > raf.length()) {
      throw IllegalArgumentException("Corrupt ZIP local header offset")
    }

    raf.seek(entry.localHeaderOffset)
    val localHeader = ByteArray(30)
    raf.readFully(localHeader)
    if (readLeInt32(localHeader, 0) != ZIP_SIG_LOCAL_FILE_HEADER) {
      throw IllegalArgumentException("Invalid ZIP local file header signature")
    }

    val localNameLen = readLeUInt16(localHeader, 26)
    val localExtraLen = readLeUInt16(localHeader, 28)

    var effectiveEncMethod = entry.encryptionMethod
    var effectiveActualMethod = entry.actualCompressionMethod
    var effectiveVendorVer = entry.aesVendorVersion

    if (localExtraLen > 0) {
      val localExtra = ByteArray(localExtraLen)
      raf.seek(entry.localHeaderOffset + 30L + localNameLen)
      raf.readFully(localExtra)
      var extPos = 0
      while (extPos + 4 <= localExtra.size) {
        val extId = readLeUInt16(localExtra, extPos)
        val extSize = readLeUInt16(localExtra, extPos + 2)
        if (extPos + 4 + extSize > localExtra.size) break
        if (extId == ZIP_EXTRA_WINZIP_AES && extSize >= 7) {
          effectiveVendorVer = readLeUInt16(localExtra, extPos + 4)
          val strength = localExtra[extPos + 8].toInt() and 0xFF
          effectiveActualMethod = readLeUInt16(localExtra, extPos + 9)
          effectiveEncMethod = ArchiveEncryptionMethod.fromAesStrength(strength)
        }
        extPos += 4 + extSize
      }
    }

    val dataOffset = entry.localHeaderOffset + 30L + localNameLen + localExtraLen
    val compSize = entry.compressedSize.coerceAtLeast(0L)
    if (dataOffset + compSize > raf.length()) {
      throw IllegalArgumentException("Truncated ZIP entry payload")
    }

    raf.seek(dataOffset)
    val rawCompressed = ByteArray(compSize.toInt())
    raf.readFully(rawCompressed)

    if (!entry.isEncrypted) {
      val decoded = decompressZipPayload(rawCompressed, effectiveActualMethod, maxBytes)
      return if (decoded.size > maxBytes) decoded.copyOf(maxBytes) else decoded
    }

    if (password.isNullOrEmpty()) {
      throw InvalidArchivePasswordException(
        message = "Password required to unlock encrypted archive",
        isMissingPassword = true,
      )
    }

    val passwordEncodings = buildList {
      add(password.toByteArray(Charsets.UTF_8))
      if (password.any { it.code > 127 }) {
        add(password.toByteArray(Charsets.ISO_8859_1))
      }
    }

    if (effectiveEncMethod.isAes || entry.compressionMethod == ZIP_METHOD_WINZIP_AES) {
      val aesMethod = if (effectiveEncMethod.isAes) effectiveEncMethod else ArchiveEncryptionMethod.AES_256
      val saltLen = aesMethod.saltLength
      val keyLen = aesMethod.keyLength
      val minOverhead = saltLen + 2 + WINZIP_AUTH_CODE_BYTES
      if (rawCompressed.size < minOverhead) {
        throw IllegalArgumentException("Truncated AES-encrypted ZIP entry")
      }

      val salt = rawCompressed.copyOfRange(0, saltLen)
      val pvExpected = rawCompressed.copyOfRange(saltLen, saltLen + 2)
      val cipherLen = rawCompressed.size - minOverhead
      val ciphertext = rawCompressed.copyOfRange(saltLen + 2, saltLen + 2 + cipherLen)
      val macExpected = rawCompressed.copyOfRange(saltLen + 2 + cipherLen, rawCompressed.size)

      for (pwBytes in passwordEncodings) {
        val derived = pbkdf2HmacSha1(
          passwordBytes = pwBytes,
          salt = salt,
          iterations = WINZIP_PBKDF2_ITERATIONS,
          derivedLength = 2 * keyLen + 2,
        )
        val pv0 = derived[2 * keyLen]
        val pv1 = derived[2 * keyLen + 1]
        if (pv0 != pvExpected[0] || pv1 != pvExpected[1]) continue

        val hmacKey = derived.copyOfRange(keyLen, 2 * keyLen)
        val computedMac10 = hmacSha1First10(hmacKey, ciphertext)
        if (!MessageDigest.isEqual(computedMac10, macExpected)) continue

        val aesKey = derived.copyOfRange(0, keyLen)
        val decryptedCompressed = aesCtrWinZip(aesKey, ciphertext)
        val plaintext = decompressZipPayload(decryptedCompressed, effectiveActualMethod, maxBytes)

        if (effectiveVendorVer == 1 && entry.crc32 != 0L && plaintext.size.toLong() == entry.uncompressedSize) {
          if (computeCrc32(plaintext) != entry.crc32) {
            throw InvalidArchivePasswordException("CRC-32 mismatch after AES-AE1 decryption")
          }
        }
        return if (plaintext.size > maxBytes) plaintext.copyOf(maxBytes) else plaintext
      }
      throw InvalidArchivePasswordException("Incorrect password")
    } else {
      // PKWARE Traditional ZipCrypto
      if (rawCompressed.size < 12) {
        throw IllegalArgumentException("Truncated ZipCrypto entry")
      }
      val expectedCrcCheck = ((entry.crc32 ushr 24) and 0xFFL).toInt()
      val expectedTimeCheck = (entry.dosTime ushr 8) and 0xFF

      for (pwBytes in passwordEncodings) {
        val crypto = ZipCryptoState(pwBytes)
        val decHeader = crypto.decryptBytes(rawCompressed, 0, 12)
        val checkByte = decHeader[11].toInt() and 0xFF
        val hasDataDescriptor = (entry.gpFlag and 0x0008) != 0
        val headerMatches = if (hasDataDescriptor) {
          checkByte == expectedTimeCheck || checkByte == expectedCrcCheck
        } else {
          checkByte == expectedCrcCheck || checkByte == expectedTimeCheck
        }
        if (!headerMatches) continue

        val decryptedCompressed = crypto.decryptBytes(rawCompressed, 12, rawCompressed.size - 12)
        val fullPlaintext = try {
          decompressZipPayload(decryptedCompressed, effectiveActualMethod, Int.MAX_VALUE)
        } catch (_: Exception) {
          continue
        }

        if (fullPlaintext.size.toLong() != entry.uncompressedSize) continue
        if (computeCrc32(fullPlaintext) != entry.crc32) continue

        return if (fullPlaintext.size > maxBytes) fullPlaintext.copyOf(maxBytes) else fullPlaintext
      }
      throw InvalidArchivePasswordException("Incorrect password")
    }
  }

  private fun writeEncryptedZipPayloads(
    payloads: List<RawEntryPayload>,
    outputFile: File,
    compressionLevel: ArchiveCompressionLevel,
    password: String,
    encryptionMethod: ArchiveEncryptionMethod,
  ) {
    val secureRandom = SecureRandom()
    val passwordBytes = password.toByteArray(Charsets.UTF_8)
    val cdRecords = mutableListOf<WrittenCentralDirRecord>()
    val seenEntries = mutableSetOf<String>()

    CountingOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { out ->
      for (payload in payloads) {
        val clean = normalizeArchivePath(payload.archivePath)
        if (clean.isEmpty()) continue
        val entryName = if (payload.isDirectory) "$clean/" else clean
        if (!seenEntries.add(entryName)) continue

        val nameBytes = entryName.toByteArray(Charsets.UTF_8)
        val ts = payload.lastModifiedMs.takeIf { it > 0L } ?: System.currentTimeMillis()
        val (dosTime, dosDate) = epochMsToDosDateTime(ts)
        val localOffset = out.bytesWritten

        if (payload.isDirectory) {
          val gpFlag = 0x0800 // UTF-8
          writeLocalFileHeader(
            out = out,
            versionNeeded = 20,
            gpFlag = gpFlag,
            compressionMethod = ZIP_METHOD_STORED,
            dosTime = dosTime,
            dosDate = dosDate,
            crc32 = 0L,
            compressedSize = 0L,
            uncompressedSize = 0L,
            nameBytes = nameBytes,
            extraBytes = ByteArray(0),
          )
          cdRecords.add(
            WrittenCentralDirRecord(
              versionMadeBy = 20,
              versionNeeded = 20,
              gpFlag = gpFlag,
              compressionMethod = ZIP_METHOD_STORED,
              dosTime = dosTime,
              dosDate = dosDate,
              crc32 = 0L,
              compressedSize = 0L,
              uncompressedSize = 0L,
              nameBytes = nameBytes,
              extraBytes = ByteArray(0),
              externalAttrs = 0x10L, // MS-DOS directory attribute
              localHeaderOffset = localOffset,
            )
          )
          continue
        }

        val plainBytes = payload.bytes
        val plainCrc32 = computeCrc32(plainBytes)
        val useDeflate = compressionLevel != ArchiveCompressionLevel.STORE && plainBytes.isNotEmpty()
        val actualMethod = if (useDeflate) ZIP_METHOD_DEFLATED else ZIP_METHOD_STORED
        val compressedRaw = if (useDeflate) {
          deflateRaw(plainBytes, compressionLevel.zipLevel)
        } else {
          plainBytes
        }

        if (encryptionMethod.isAes) {
          val aesMethod = encryptionMethod
          val salt = ByteArray(aesMethod.saltLength).also { secureRandom.nextBytes(it) }
          val derived = pbkdf2HmacSha1(
            passwordBytes = passwordBytes,
            salt = salt,
            iterations = WINZIP_PBKDF2_ITERATIONS,
            derivedLength = 2 * aesMethod.keyLength + 2,
          )
          val aesKey = derived.copyOfRange(0, aesMethod.keyLength)
          val hmacKey = derived.copyOfRange(aesMethod.keyLength, 2 * aesMethod.keyLength)
          val pvBytes = derived.copyOfRange(2 * aesMethod.keyLength, 2 * aesMethod.keyLength + 2)

          val ciphertext = aesCtrWinZip(aesKey, compressedRaw)
          val authCode10 = hmacSha1First10(hmacKey, ciphertext)
          val encryptedSize = (salt.size + 2 + ciphertext.size + WINZIP_AUTH_CODE_BYTES).toLong()

          val aesExtra = ByteArray(11)
          writeLeUInt16(aesExtra, 0, ZIP_EXTRA_WINZIP_AES)
          writeLeUInt16(aesExtra, 2, 7)
          writeLeUInt16(aesExtra, 4, 2) // AE-2
          writeLeUInt16(aesExtra, 6, 0x4541) // "AE"
          aesExtra[8] = aesMethod.aesStrengthByte.toByte()
          writeLeUInt16(aesExtra, 9, actualMethod)

          val gpFlag = 0x0001 or 0x0800 // Encrypted + UTF-8
          writeLocalFileHeader(
            out = out,
            versionNeeded = 51,
            gpFlag = gpFlag,
            compressionMethod = ZIP_METHOD_WINZIP_AES,
            dosTime = dosTime,
            dosDate = dosDate,
            crc32 = 0L, // AE-2 omits plaintext CRC-32 from header
            compressedSize = encryptedSize,
            uncompressedSize = plainBytes.size.toLong(),
            nameBytes = nameBytes,
            extraBytes = aesExtra,
          )
          out.write(salt)
          out.write(pvBytes)
          if (ciphertext.isNotEmpty()) out.write(ciphertext)
          out.write(authCode10)

          cdRecords.add(
            WrittenCentralDirRecord(
              versionMadeBy = 51,
              versionNeeded = 51,
              gpFlag = gpFlag,
              compressionMethod = ZIP_METHOD_WINZIP_AES,
              dosTime = dosTime,
              dosDate = dosDate,
              crc32 = 0L,
              compressedSize = encryptedSize,
              uncompressedSize = plainBytes.size.toLong(),
              nameBytes = nameBytes,
              extraBytes = aesExtra,
              externalAttrs = 0L,
              localHeaderOffset = localOffset,
            )
          )
        } else {
          // ZipCrypto (PKWARE traditional encryption)
          val crypto = ZipCryptoState(passwordBytes)
          val headerPlain = ByteArray(12).also { secureRandom.nextBytes(it) }
          headerPlain[11] = ((plainCrc32 ushr 24) and 0xFFL).toByte()
          val encryptedHeader = crypto.encryptBytes(headerPlain)
          val encryptedBody = crypto.encryptBytes(compressedRaw)
          val encryptedSize = (12 + encryptedBody.size).toLong()

          val gpFlag = 0x0001 or 0x0800 // Encrypted + UTF-8
          writeLocalFileHeader(
            out = out,
            versionNeeded = 20,
            gpFlag = gpFlag,
            compressionMethod = actualMethod,
            dosTime = dosTime,
            dosDate = dosDate,
            crc32 = plainCrc32,
            compressedSize = encryptedSize,
            uncompressedSize = plainBytes.size.toLong(),
            nameBytes = nameBytes,
            extraBytes = ByteArray(0),
          )
          out.write(encryptedHeader)
          if (encryptedBody.isNotEmpty()) out.write(encryptedBody)

          cdRecords.add(
            WrittenCentralDirRecord(
              versionMadeBy = 20,
              versionNeeded = 20,
              gpFlag = gpFlag,
              compressionMethod = actualMethod,
              dosTime = dosTime,
              dosDate = dosDate,
              crc32 = plainCrc32,
              compressedSize = encryptedSize,
              uncompressedSize = plainBytes.size.toLong(),
              nameBytes = nameBytes,
              extraBytes = ByteArray(0),
              externalAttrs = 0L,
              localHeaderOffset = localOffset,
            )
          )
        }
      }

      val cdOffset = out.bytesWritten
      for (rec in cdRecords) {
        val header = ByteArray(46)
        writeLeInt32(header, 0, ZIP_SIG_CENTRAL_FILE_HEADER)
        writeLeUInt16(header, 4, rec.versionMadeBy)
        writeLeUInt16(header, 6, rec.versionNeeded)
        writeLeUInt16(header, 8, rec.gpFlag)
        writeLeUInt16(header, 10, rec.compressionMethod)
        writeLeUInt16(header, 12, rec.dosTime)
        writeLeUInt16(header, 14, rec.dosDate)
        writeLeUInt32(header, 16, rec.crc32)
        writeLeUInt32(header, 20, rec.compressedSize)
        writeLeUInt32(header, 24, rec.uncompressedSize)
        writeLeUInt16(header, 28, rec.nameBytes.size)
        writeLeUInt16(header, 30, rec.extraBytes.size)
        writeLeUInt16(header, 32, 0) // comment len
        writeLeUInt16(header, 34, 0) // disk start
        writeLeUInt16(header, 36, 0) // internal attrs
        writeLeUInt32(header, 38, rec.externalAttrs)
        writeLeUInt32(header, 42, rec.localHeaderOffset)

        out.write(header)
        out.write(rec.nameBytes)
        if (rec.extraBytes.isNotEmpty()) out.write(rec.extraBytes)
      }

      val cdSize = out.bytesWritten - cdOffset
      val eocd = ByteArray(22)
      writeLeInt32(eocd, 0, ZIP_SIG_EOCD)
      writeLeUInt16(eocd, 4, 0)
      writeLeUInt16(eocd, 6, 0)
      writeLeUInt16(eocd, 8, cdRecords.size)
      writeLeUInt16(eocd, 10, cdRecords.size)
      writeLeUInt32(eocd, 12, cdSize)
      writeLeUInt32(eocd, 16, cdOffset)
      writeLeUInt16(eocd, 20, 0)
      out.write(eocd)
      out.flush()
    }
  }

  private fun writeLocalFileHeader(
    out: OutputStream,
    versionNeeded: Int,
    gpFlag: Int,
    compressionMethod: Int,
    dosTime: Int,
    dosDate: Int,
    crc32: Long,
    compressedSize: Long,
    uncompressedSize: Long,
    nameBytes: ByteArray,
    extraBytes: ByteArray,
  ) {
    val header = ByteArray(30)
    writeLeInt32(header, 0, ZIP_SIG_LOCAL_FILE_HEADER)
    writeLeUInt16(header, 4, versionNeeded)
    writeLeUInt16(header, 6, gpFlag)
    writeLeUInt16(header, 8, compressionMethod)
    writeLeUInt16(header, 10, dosTime)
    writeLeUInt16(header, 12, dosDate)
    writeLeUInt32(header, 14, crc32)
    writeLeUInt32(header, 18, compressedSize)
    writeLeUInt32(header, 22, uncompressedSize)
    writeLeUInt16(header, 26, nameBytes.size)
    writeLeUInt16(header, 28, extraBytes.size)
    out.write(header)
    out.write(nameBytes)
    if (extraBytes.isNotEmpty()) out.write(extraBytes)
  }

  private fun pbkdf2HmacSha1(
    passwordBytes: ByteArray,
    salt: ByteArray,
    iterations: Int,
    derivedLength: Int,
  ): ByteArray {
    val mac = Mac.getInstance("HmacSHA1")
    val keyBytes = if (passwordBytes.isEmpty()) ByteArray(1) else passwordBytes
    mac.init(SecretKeySpec(keyBytes, "HmacSHA1"))
    val hLen = 20
    val blocks = (derivedLength + hLen - 1) / hLen
    val derived = ByteArray(blocks * hLen)
    val saltWithBlock = ByteArray(salt.size + 4)
    System.arraycopy(salt, 0, saltWithBlock, 0, salt.size)

    for (blockIndex in 1..blocks) {
      saltWithBlock[salt.size] = ((blockIndex ushr 24) and 0xFF).toByte()
      saltWithBlock[salt.size + 1] = ((blockIndex ushr 16) and 0xFF).toByte()
      saltWithBlock[salt.size + 2] = ((blockIndex ushr 8) and 0xFF).toByte()
      saltWithBlock[salt.size + 3] = (blockIndex and 0xFF).toByte()

      var u = mac.doFinal(saltWithBlock)
      val t = u.copyOf()
      for (iter in 2..iterations) {
        u = mac.doFinal(u)
        for (j in 0 until hLen) {
          t[j] = (t[j].toInt() xor u[j].toInt()).toByte()
        }
      }
      System.arraycopy(t, 0, derived, (blockIndex - 1) * hLen, hLen)
    }
    return if (derived.size == derivedLength) derived else derived.copyOf(derivedLength)
  }

  private fun aesCtrWinZip(aesKey: ByteArray, input: ByteArray): ByteArray {
    if (input.isEmpty()) return ByteArray(0)
    val cipher = Cipher.getInstance("AES/ECB/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(aesKey, "AES"))
    val counter = ByteArray(16)
    val keystreamBlock = ByteArray(16)
    val out = ByteArray(input.size)

    var offset = 0
    while (offset < input.size) {
      // WinZip AES uses a 128-bit little-endian block counter starting at 1
      for (i in 0 until 16) {
        val next = (counter[i].toInt() and 0xFF) + 1
        counter[i] = next.toByte()
        if (next <= 0xFF) break
      }
      cipher.update(counter, 0, 16, keystreamBlock, 0)
      val chunk = minOf(16, input.size - offset)
      for (j in 0 until chunk) {
        out[offset + j] = (input[offset + j].toInt() xor keystreamBlock[j].toInt()).toByte()
      }
      offset += chunk
    }
    return out
  }

  private fun hmacSha1First10(hmacKey: ByteArray, data: ByteArray): ByteArray {
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(SecretKeySpec(hmacKey, "HmacSHA1"))
    return mac.doFinal(data).copyOf(WINZIP_AUTH_CODE_BYTES)
  }

  private fun deflateRaw(input: ByteArray, level: Int): ByteArray {
    if (input.isEmpty()) return ByteArray(0)
    val deflater = Deflater(level, true)
    return try {
      deflater.setInput(input)
      deflater.finish()
      val out = ByteArrayOutputStream(minOf(input.size + 64, 65_536))
      val buf = ByteArray(8192)
      while (!deflater.finished()) {
        val count = deflater.deflate(buf)
        if (count > 0) {
          out.write(buf, 0, count)
        } else if (deflater.needsInput()) {
          break
        }
      }
      out.toByteArray()
    } finally {
      deflater.end()
    }
  }

  private fun decompressZipPayload(
    compressed: ByteArray,
    method: Int,
    maxBytes: Int,
  ): ByteArray {
    return when (method) {
      ZIP_METHOD_STORED -> {
        if (compressed.size > maxBytes) compressed.copyOf(maxBytes) else compressed
      }
      ZIP_METHOD_DEFLATED -> inflateRaw(compressed, maxBytes)
      else -> throw IllegalArgumentException("Unsupported ZIP compression method: $method")
    }
  }

  private fun inflateRaw(compressed: ByteArray, maxBytes: Int): ByteArray {
    if (compressed.isEmpty() || maxBytes <= 0) return ByteArray(0)
    val inflater = Inflater(true)
    return try {
      // Append 1 dummy byte per java.util.zip.Inflater(nowrap=true) contract
      val padded = ByteArray(compressed.size + 1)
      System.arraycopy(compressed, 0, padded, 0, compressed.size)
      inflater.setInput(padded)

      val out = ByteArrayOutputStream()
      val buf = ByteArray(8192)
      var remaining = maxBytes
      while (!inflater.finished() && remaining > 0) {
        val count = try {
          inflater.inflate(buf, 0, minOf(buf.size, remaining))
        } catch (e: DataFormatException) {
          throw IllegalArgumentException("Corrupt deflated stream", e)
        }
        if (count > 0) {
          out.write(buf, 0, count)
          remaining -= count
        } else if (inflater.needsInput() || inflater.needsDictionary()) {
          break
        }
      }
      out.toByteArray()
    } finally {
      inflater.end()
    }
  }

  private fun dosDateTimeToEpochMs(dosTime: Int, dosDate: Int): Long {
    if (dosDate == 0 && dosTime == 0) return 0L
    val year = ((dosDate ushr 9) and 0x7F) + 1980
    val month = (((dosDate ushr 5) and 0x0F) - 1).coerceIn(0, 11)
    val day = (dosDate and 0x1F).coerceIn(1, 31)
    val hour = ((dosTime ushr 11) and 0x1F).coerceIn(0, 23)
    val minute = ((dosTime ushr 5) and 0x3F).coerceIn(0, 59)
    val second = ((dosTime and 0x1F) * 2).coerceIn(0, 59)
    return Calendar.getInstance().apply {
      set(year, month, day, hour, minute, second)
      set(Calendar.MILLISECOND, 0)
    }.timeInMillis
  }

  private fun epochMsToDosDateTime(epochMs: Long): Pair<Int, Int> {
    val cal = Calendar.getInstance().apply { timeInMillis = epochMs }
    val year = cal.get(Calendar.YEAR)
    if (year < 1980) {
      return 0 to ((1 shl 5) or 1)
    }
    val dosDate = (((year - 1980).coerceAtMost(127) shl 9) or
      ((cal.get(Calendar.MONTH) + 1) shl 5) or
      cal.get(Calendar.DAY_OF_MONTH))
    val dosTime = ((cal.get(Calendar.HOUR_OF_DAY) shl 11) or
      (cal.get(Calendar.MINUTE) shl 5) or
      (cal.get(Calendar.SECOND) / 2))
    return dosTime to dosDate
  }

  private fun readLeUInt16(buf: ByteArray, offset: Int): Int {
    return (buf[offset].toInt() and 0xFF) or
      ((buf[offset + 1].toInt() and 0xFF) shl 8)
  }

  private fun readLeInt32(buf: ByteArray, offset: Int): Int {
    return (buf[offset].toInt() and 0xFF) or
      ((buf[offset + 1].toInt() and 0xFF) shl 8) or
      ((buf[offset + 2].toInt() and 0xFF) shl 16) or
      ((buf[offset + 3].toInt() and 0xFF) shl 24)
  }

  private fun readLeUInt32(buf: ByteArray, offset: Int): Long {
    return readLeInt32(buf, offset).toLong() and 0xFFFFFFFFL
  }

  private fun readLeInt64(buf: ByteArray, offset: Int): Long {
    val lo = readLeUInt32(buf, offset)
    val hi = readLeUInt32(buf, offset + 4)
    return lo or (hi shl 32)
  }

  private fun writeLeUInt16(buf: ByteArray, offset: Int, value: Int) {
    buf[offset] = (value and 0xFF).toByte()
    buf[offset + 1] = ((value ushr 8) and 0xFF).toByte()
  }

  private fun writeLeInt32(buf: ByteArray, offset: Int, value: Int) {
    buf[offset] = (value and 0xFF).toByte()
    buf[offset + 1] = ((value ushr 8) and 0xFF).toByte()
    buf[offset + 2] = ((value ushr 16) and 0xFF).toByte()
    buf[offset + 3] = ((value ushr 24) and 0xFF).toByte()
  }

  private fun writeLeUInt32(buf: ByteArray, offset: Int, value: Long) {
    writeLeInt32(buf, offset, (value and 0xFFFFFFFFL).toInt())
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
