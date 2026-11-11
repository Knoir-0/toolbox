package com.example.toolbox.ui.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.Locale
import java.util.zip.CRC32
import java.util.zip.Deflater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

const val GALLERY_PREFS_NAME = "gallery_tool_preferences"
const val PREF_KEY_GALLERY_FOLDERS = "gallery_loaded_folders"
const val PREF_KEY_GALLERY_VIEW_MODE = "gallery_view_mode"
const val PREF_KEY_GALLERY_ALBUMS_PER_ROW = "gallery_albums_per_row"

const val MIN_ALBUMS_PER_ROW = 3
const val MAX_ALBUMS_PER_ROW = 6
const val DEFAULT_ALBUMS_PER_ROW = 3

const val MIN_PHOTO_ZOOM = 1f
const val MAX_PHOTO_ZOOM = 5f

val DEFAULT_GALLERY_FOLDER_NAMES: List<String> = listOf("DCIM", "Photos")

/**
 * Presentation modes for the Gallery tool:
 * - [TILE]: Default grid view (3 to 6 albums per row) with cover photo tile and folder label underneath.
 * - [LIST]: Secondary list view with album cover photo icon on the left and folder label to the right.
 */
enum class GalleryViewMode(val displayName: String) {
  TILE("Tile"),
  LIST("List"),
  ;

  companion object {
    val DEFAULT: GalleryViewMode = TILE

    fun fromNameOrDefault(name: String?): GalleryViewMode {
      if (name.isNullOrBlank()) return DEFAULT
      return entries.find { it.name.equals(name.trim(), ignoreCase = true) } ?: DEFAULT
    }
  }
}

/**
 * Represents an individual photo file inside an album.
 */
data class GalleryPhoto(
  val file: File,
  val name: String,
  val path: String,
  val sizeBytes: Long,
  val lastModifiedMs: Long,
  val extension: String,
) {
  val sizeFormatted: String get() = FileManagerEngine.formatFileSize(sizeBytes)
  val timestampFormatted: String get() = FileManagerEngine.formatTimestamp(lastModifiedMs)
}

/**
 * Represents an album (folder) in the Gallery, including its most recent photo and nested counts.
 * When multiple root folders (such as `DCIM` and `Photos`) are un-nested as if they were the same folder,
 * same-named subfolders across those roots are unified via [directories].
 */
data class GalleryAlbum(
  val directory: File,
  val name: String,
  val path: String,
  val mostRecentPhoto: File?,
  val directPhotoCount: Int,
  val totalPhotoCount: Int,
  val subAlbumCount: Int,
  val lastModifiedMs: Long,
  val directories: List<File> = listOf(directory),
) {
  val hasNestedAlbums: Boolean get() = subAlbumCount > 0

  val summaryText: String
    get() = when {
      subAlbumCount > 0 && totalPhotoCount > 0 -> {
        val photoLabel = if (totalPhotoCount == 1) "1 photo" else "$totalPhotoCount photos"
        val albumLabel = if (subAlbumCount == 1) "1 folder" else "$subAlbumCount folders"
        "$photoLabel • $albumLabel"
      }
      subAlbumCount > 0 -> {
        if (subAlbumCount == 1) "1 folder" else "$subAlbumCount folders"
      }
      else -> {
        if (totalPhotoCount == 1) "1 photo" else "$totalPhotoCount photos"
      }
    }
}

/**
 * Holds the contents of an opened album directory (or merged root directories):
 * its nested sub-albums and direct photos.
 */
data class AlbumContents(
  val directory: File,
  val nestedAlbums: List<GalleryAlbum>,
  val photos: List<GalleryPhoto>,
  val directories: List<File> = listOf(directory),
)

/**
 * Core engine for the Gallery tool:
 * - Resolves default gallery folders (`DCIM` and `Photos`)
 * - Scans albums and nested folders for the most recent photo cover image
 * - Supports hierarchical navigation into nested albums/folders
 * - Coerces and calculates pinch-to-zoom column density (`3..6` albums per row)
 */
object GalleryEngine {

  val SUPPORTED_IMAGE_EXTENSIONS: Set<String> = setOf(
    "jpg", "jpeg", "png", "webp", "gif", "bmp", "heic", "heif",
  )

  private const val MAX_THUMBNAIL_CACHE_ENTRIES = 96

  private val thumbnailCacheLock = Any()
  private val thumbnailCache = object : LinkedHashMap<String, Bitmap>(64, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Bitmap>?): Boolean {
      return size > MAX_THUMBNAIL_CACHE_ENTRIES
    }
  }

  private fun thumbnailCacheKey(file: File, reqSizePx: Int): String {
    return "${file.absolutePath}:${file.lastModified()}:$reqSizePx"
  }

  /**
   * Returns a previously decoded thumbnail [Bitmap] from the in-memory LRU cache if available,
   * or `null` if it has not been decoded yet. Safe to call on the main UI thread without blocking.
   */
  fun getCachedThumbnailBitmap(file: File, reqSizePx: Int = 360): Bitmap? {
    val key = thumbnailCacheKey(file, reqSizePx)
    synchronized(thumbnailCacheLock) {
      return thumbnailCache[key]
    }
  }

  /**
   * Clears the in-memory album art / thumbnail cache.
   */
  fun clearThumbnailCache() {
    synchronized(thumbnailCacheLock) {
      thumbnailCache.clear()
    }
  }

  /**
   * Coerces an albums-per-row count into the valid range [MIN_ALBUMS_PER_ROW]..[MAX_ALBUMS_PER_ROW] (3..6).
   */
  fun coerceAlbumsPerRow(count: Int): Int {
    return count.coerceIn(MIN_ALBUMS_PER_ROW, MAX_ALBUMS_PER_ROW)
  }

  /**
   * Calculates the updated zoom scale (`1f..5f`) for an individual photo in the viewer from a pinch
   * gesture [zoomChange].
   */
  fun calculatePhotoZoom(currentZoom: Float, zoomChange: Float): Float {
    val base = currentZoom.coerceIn(MIN_PHOTO_ZOOM, MAX_PHOTO_ZOOM)
    if (zoomChange <= 0f || zoomChange.isNaN()) return base
    return (base * zoomChange).coerceIn(MIN_PHOTO_ZOOM, MAX_PHOTO_ZOOM)
  }

  /**
   * Calculates the updated albums-per-row count (3..6) from a pinch gesture [zoomFactor].
   *
   * - Pinching in (`zoomFactor < 1f`, fingers moving together) shrinks tiles to show MORE albums per row (up to 6).
   * - Pinching out (`zoomFactor > 1f`, fingers moving apart) enlarges tiles to show FEWER albums per row (down to 3).
   */
  fun calculatePinchAlbumsPerRow(currentPerRow: Int, zoomFactor: Float): Int {
    val current = coerceAlbumsPerRow(currentPerRow)
    if (zoomFactor <= 0f || zoomFactor.isNaN()) return current

    val delta = when {
      zoomFactor <= 0.48f -> 3
      zoomFactor <= 0.65f -> 2
      zoomFactor <= 0.85f -> 1
      zoomFactor >= 1.85f -> -3
      zoomFactor >= 1.48f -> -2
      zoomFactor >= 1.18f -> -1
      else -> 0
    }
    return coerceAlbumsPerRow(current + delta)
  }

  /**
   * Checks whether [file] is a supported photo/image file.
   */
  fun isPhotoFile(file: File): Boolean {
    if (!file.exists() || !file.isFile) return false
    if (file.name.startsWith(".")) return false
    val ext = file.extension.lowercase(Locale.ROOT)
    return ext in SUPPORTED_IMAGE_EXTENSIONS || FileManagerEngine.isImageFile(file)
  }

  /**
   * Checks whether [file] is a visible subdirectory representing a nested album/folder.
   */
  fun isAlbumDirectory(file: File): Boolean {
    return file.exists() && file.isDirectory && !file.name.startsWith(".")
  }

  /**
   * Returns the default folders loaded into the gallery (`DCIM` and `Photos`) under [rootDirectory].
   */
  fun getDefaultGalleryFolders(
    rootDirectory: File = FileManagerEngine.getUserRootDirectory(),
  ): List<File> {
    return DEFAULT_GALLERY_FOLDER_NAMES.map { folderName ->
      File(rootDirectory, folderName)
    }
  }

  /**
   * Resolves the list of folders loaded into the gallery, starting with the default `DCIM` and `Photos`
   * folders and appending any user-added folder paths.
   */
  fun resolveGalleryFolders(
    rootDirectory: File = FileManagerEngine.getUserRootDirectory(),
    additionalPaths: List<String> = emptyList(),
  ): List<File> {
    val defaults = getDefaultGalleryFolders(rootDirectory)
    val extras = additionalPaths.mapNotNull { raw ->
      val trimmed = raw.trim()
      if (trimmed.isEmpty()) {
        null
      } else if (trimmed.startsWith("/")) {
        val candidate = File(trimmed)
        if (candidate.exists() || trimmed.startsWith(rootDirectory.absolutePath)) {
          candidate
        } else {
          File(rootDirectory, trimmed.trimStart('/'))
        }
      } else {
        File(rootDirectory, trimmed)
      }
    }
    val combined = mutableListOf<File>()
    for (folder in defaults + extras) {
      if (combined.none { it.absolutePath.equals(folder.absolutePath, ignoreCase = true) }) {
        combined.add(folder)
      }
    }
    return combined
  }

  /**
   * Finds the most recent photo inside [directory] (searching both direct photos and nested folders
   * up to [maxDepth]).
   *
   * Most recent is determined by `File.lastModified()` descending, with filename descending as a
   * deterministic tie-breaker.
   */
  fun findMostRecentPhoto(directory: File, maxDepth: Int = 8): File? {
    return findMostRecentPhoto(listOf(directory), maxDepth = maxDepth)
  }

  /**
   * Finds the most recent photo across all [directories] (searching both direct photos and nested
   * folders up to [maxDepth]).
   */
  fun findMostRecentPhoto(directories: List<File>, maxDepth: Int = 8): File? {
    var newestPhoto: File? = null
    var newestTime = Long.MIN_VALUE

    fun scan(currentDir: File, depth: Int) {
      if (depth > maxDepth) return
      val children = try {
        currentDir.listFiles()
      } catch (_: Throwable) {
        null
      } ?: return

      for (child in children) {
        if (child.name.startsWith(".")) continue
        if (child.isFile && isPhotoFile(child)) {
          val modified = child.lastModified()
          val currentBest = newestPhoto
          if (currentBest == null ||
            modified > newestTime ||
            (modified == newestTime && child.name.compareTo(currentBest.name, ignoreCase = true) > 0)
          ) {
            newestPhoto = child
            newestTime = modified
          }
        } else if (child.isDirectory && depth < maxDepth) {
          scan(child, depth + 1)
        }
      }
    }

    for (dir in directories) {
      if (dir.exists() && dir.isDirectory) {
        scan(dir, 0)
      }
    }
    return newestPhoto
  }

  /**
   * Counts all photos recursively inside [directory] up to [maxDepth].
   */
  fun countTotalPhotos(directory: File, maxDepth: Int = 8): Int {
    return countTotalPhotos(listOf(directory), maxDepth = maxDepth)
  }

  /**
   * Counts all photos recursively across all [directories] up to [maxDepth].
   */
  fun countTotalPhotos(directories: List<File>, maxDepth: Int = 8): Int {
    val seenPaths = mutableSetOf<String>()

    fun scan(currentDir: File, depth: Int) {
      if (depth > maxDepth) return
      val children = try {
        currentDir.listFiles()
      } catch (_: Throwable) {
        null
      } ?: return

      for (child in children) {
        if (child.name.startsWith(".")) continue
        if (child.isFile && isPhotoFile(child)) {
          seenPaths.add(child.absolutePath)
        } else if (child.isDirectory && depth < maxDepth) {
          scan(child, depth + 1)
        }
      }
    }

    for (dir in directories) {
      if (dir.exists() && dir.isDirectory) {
        scan(dir, 0)
      }
    }
    return seenPaths.size
  }

  /**
   * Builds a [GalleryAlbum] model for [directory], resolving its most recent photo, direct photo count,
   * total photo count (including nested folders), and nested folder count.
   *
   * When [deferAlbumArt] is `true`, deep recursive scanning for [GalleryAlbum.mostRecentPhoto] is
   * skipped during initial folder listing so it can be resolved asynchronously in the background via
   * [resolveAlbumArtAsync].
   */
  fun buildAlbum(
    directory: File,
    maxDepth: Int = 8,
    deferAlbumArt: Boolean = false,
  ): GalleryAlbum {
    return buildMergedAlbum(
      directories = listOf(directory),
      maxDepth = maxDepth,
      deferAlbumArt = deferAlbumArt,
    )
  }

  /**
   * Builds a unified [GalleryAlbum] model across one or more [directories] (e.g. when same-named
   * subfolders in `DCIM` and `Photos` are merged as if they were in the same folder).
   */
  fun buildMergedAlbum(
    directories: List<File>,
    maxDepth: Int = 8,
    deferAlbumArt: Boolean = false,
  ): GalleryAlbum {
    val distinctDirs = mutableListOf<File>()
    for (dir in directories) {
      if (distinctDirs.none { it.absolutePath.equals(dir.absolutePath, ignoreCase = true) }) {
        distinctDirs.add(dir)
      }
    }
    val primaryDir = distinctDirs.firstOrNull() ?: File("")

    val children = distinctDirs.flatMap { dir ->
      if (dir.exists() && dir.isDirectory) {
        try {
          dir.listFiles()?.toList().orEmpty()
        } catch (_: Throwable) {
          emptyList()
        }
      } else {
        emptyList()
      }
    }

    val directPhotoFiles = children
      .filter { isPhotoFile(it) }
      .distinctBy { it.absolutePath }
    val directPhotos = directPhotoFiles.size
    val subAlbums = children
      .filter { isAlbumDirectory(it) }
      .map { it.name.lowercase(Locale.ROOT) }
      .distinct()
      .size

    val mostRecent = when {
      deferAlbumArt -> null
      subAlbums == 0 -> {
        directPhotoFiles.maxWithOrNull(
          compareBy<File> { it.lastModified() }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
        )
      }
      else -> findMostRecentPhoto(distinctDirs, maxDepth = maxDepth)
    }

    val totalPhotos = if (subAlbums == 0 || deferAlbumArt) {
      directPhotos
    } else {
      countTotalPhotos(distinctDirs, maxDepth = maxDepth)
    }

    val lastMod = mostRecent?.lastModified()
      ?: distinctDirs.maxOfOrNull { if (it.exists()) it.lastModified() else 0L }
      ?: 0L

    return GalleryAlbum(
      directory = primaryDir,
      name = primaryDir.name.ifBlank { primaryDir.absolutePath },
      path = primaryDir.absolutePath,
      mostRecentPhoto = mostRecent,
      directPhotoCount = directPhotos,
      totalPhotoCount = totalPhotos,
      subAlbumCount = subAlbums,
      lastModifiedMs = lastMod,
      directories = distinctDirs,
    )
  }

  /**
   * Resolves an album's cover photo ([GalleryAlbum.mostRecentPhoto]), total photo count, and
   * pre-decodes its thumbnail bitmap on a background I/O thread ([Dispatchers.IO]).
   */
  suspend fun resolveAlbumArtAsync(
    album: GalleryAlbum,
    maxDepth: Int = 8,
    reqSizePx: Int = 360,
  ): GalleryAlbum = withContext(Dispatchers.IO) {
    ensureActive()
    val mostRecent = album.mostRecentPhoto ?: findMostRecentPhoto(album.directories, maxDepth = maxDepth)
    val totalPhotos = if (album.subAlbumCount == 0) {
      album.directPhotoCount
    } else if (album.totalPhotoCount > album.directPhotoCount) {
      album.totalPhotoCount
    } else {
      countTotalPhotos(album.directories, maxDepth = maxDepth)
    }
    ensureActive()
    if (mostRecent != null) {
      decodeThumbnailBitmap(mostRecent, reqSizePx = reqSizePx)
    }
    album.copy(
      mostRecentPhoto = mostRecent,
      totalPhotoCount = totalPhotos,
      lastModifiedMs = mostRecent?.lastModified() ?: album.lastModifiedMs,
    )
  }

  /**
   * Pre-decodes and caches album cover thumbnails in the background on [Dispatchers.IO] for [albums].
   */
  suspend fun prefetchAlbumArt(
    albums: List<GalleryAlbum>,
    reqSizePx: Int = 360,
  ) = withContext(Dispatchers.IO) {
    for (album in albums) {
      ensureActive()
      val coverFile = album.mostRecentPhoto ?: continue
      if (getCachedThumbnailBitmap(coverFile, reqSizePx) == null) {
        decodeThumbnailBitmap(coverFile, reqSizePx = reqSizePx)
      }
    }
  }

  /**
   * Loads the un-nested root contents of the Gallery by combining `DCIM` and `Photos`
   * (plus any [additionalPaths]) as if they were the same folder:
   * - [AlbumContents.nestedAlbums]: All subdirectories inside `DCIM` and `Photos` (merging same-named
   *   subfolders), sorted alphabetically.
   * - [AlbumContents.photos]: All direct photo files inside `DCIM` and `Photos`, sorted newest first.
   */
  fun loadRootContents(
    rootDirectory: File = FileManagerEngine.getUserRootDirectory(),
    folders: List<File>? = null,
    additionalPaths: List<String> = emptyList(),
  ): AlbumContents {
    val targetFolders = folders ?: resolveGalleryFolders(rootDirectory, additionalPaths)
    return loadMergedAlbumContents(targetFolders)
  }

  /**
   * Loads the root-level albums for the Gallery by un-nesting the contents of `DCIM` and `Photos`
   * (and any [additionalPaths]) as if they were the same folder.
   */
  fun loadRootAlbums(
    rootDirectory: File = FileManagerEngine.getUserRootDirectory(),
    folders: List<File>? = null,
    additionalPaths: List<String> = emptyList(),
  ): List<GalleryAlbum> {
    return loadRootContents(
      rootDirectory = rootDirectory,
      folders = folders,
      additionalPaths = additionalPaths,
    ).nestedAlbums
  }

  /**
   * Loads the contents of [album] across all of its backing [GalleryAlbum.directories].
   */
  fun loadAlbumContents(album: GalleryAlbum): AlbumContents {
    return loadMergedAlbumContents(album.directories)
  }

  /**
   * Loads the contents of [albumDirectory]:
   * - [AlbumContents.nestedAlbums]: Direct subdirectories inside [albumDirectory] (each with its own
   *   most recent photo and label), sorted alphabetically by folder name.
   * - [AlbumContents.photos]: Direct photo files inside [albumDirectory], sorted newest first.
   */
  fun loadAlbumContents(albumDirectory: File): AlbumContents {
    return loadMergedAlbumContents(listOf(albumDirectory))
  }

  /**
   * Loads and merges the contents of multiple [directories] as if they were the same folder:
   * - Subdirectories across [directories] are un-nested to the top level, merging any subdirectories
   *   that share the same name (case-insensitively) into a single [GalleryAlbum].
   * - Direct photo files across [directories] are combined and sorted newest first.
   */
  fun loadMergedAlbumContents(directories: List<File>): AlbumContents {
    val distinctDirs = mutableListOf<File>()
    for (dir in directories) {
      if (distinctDirs.none { it.absolutePath.equals(dir.absolutePath, ignoreCase = true) }) {
        distinctDirs.add(dir)
      }
    }
    val primaryDir = distinctDirs.firstOrNull() ?: File("")

    val children = distinctDirs.flatMap { dir ->
      if (dir.exists() && dir.isDirectory) {
        try {
          dir.listFiles()?.toList().orEmpty()
        } catch (_: Throwable) {
          emptyList()
        }
      } else {
        emptyList()
      }
    }

    val groupedSubDirs = LinkedHashMap<String, MutableList<File>>()
    for (child in children) {
      if (isAlbumDirectory(child)) {
        val key = child.name.lowercase(Locale.ROOT)
        groupedSubDirs.getOrPut(key) { mutableListOf() }.add(child)
      }
    }

    val nestedAlbums = groupedSubDirs.values
      .map { groupDirs -> buildMergedAlbum(groupDirs) }
      .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })

    val photos = children
      .filter { isPhotoFile(it) }
      .distinctBy { it.absolutePath }
      .sortedWith(
        compareByDescending<File> { it.lastModified() }
          .thenByDescending(String.CASE_INSENSITIVE_ORDER) { it.name }
      )
      .map { file ->
        GalleryPhoto(
          file = file,
          name = file.name,
          path = file.absolutePath,
          sizeBytes = file.length(),
          lastModifiedMs = file.lastModified(),
          extension = file.extension.lowercase(Locale.ROOT),
        )
      }

    return AlbumContents(
      directory = primaryDir,
      nestedAlbums = nestedAlbums,
      photos = photos,
      directories = distinctDirs,
    )
  }

  /**
   * Asynchronously decodes a downsampled thumbnail [Bitmap] from [file] on a background thread
   * ([Dispatchers.IO]), caching the result in memory for subsequent UI lookups.
   */
  suspend fun loadThumbnailBitmapAsync(file: File, reqSizePx: Int = 360): Bitmap? {
    getCachedThumbnailBitmap(file, reqSizePx)?.let { return it }
    return withContext(Dispatchers.IO) {
      ensureActive()
      decodeThumbnailBitmap(file, reqSizePx)
    }
  }

  /**
   * Decodes a thumbnail [Bitmap] from [file] with downsampling to prevent OOM on large camera photos,
   * storing the decoded bitmap in the in-memory LRU cache.
   */
  fun decodeThumbnailBitmap(file: File, reqSizePx: Int = 360): Bitmap? {
    if (!file.exists() || !file.isFile) return null
    getCachedThumbnailBitmap(file, reqSizePx)?.let { return it }

    return try {
      val boundsOptions = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
      }
      BitmapFactory.decodeFile(file.absolutePath, boundsOptions)
      val width = boundsOptions.outWidth
      val height = boundsOptions.outHeight
      val decoded = if (width <= 0 || height <= 0) {
        BitmapFactory.decodeFile(file.absolutePath)
      } else {
        var inSampleSize = 1
        if (height > reqSizePx || width > reqSizePx) {
          val halfHeight = height / 2
          val halfWidth = width / 2
          while ((halfHeight / inSampleSize) >= reqSizePx && (halfWidth / inSampleSize) >= reqSizePx) {
            inSampleSize *= 2
          }
        }
        val decodeOptions = BitmapFactory.Options().apply {
          this.inSampleSize = inSampleSize
        }
        BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
      }
      if (decoded != null) {
        val key = thumbnailCacheKey(file, reqSizePx)
        synchronized(thumbnailCacheLock) {
          thumbnailCache[key] = decoded
        }
      }
      decoded
    } catch (_: Throwable) {
      null
    }
  }

  /**
   * Writes a valid, spec-compliant RGB PNG image file to [outputFile] using pure standard library
   * streams (so it works identically in both JVM unit tests and Android runtime/instrumented tests).
   */
  fun writeSolidColorPng(
    outputFile: File,
    red: Int = 0x8E,
    green: Int = 0x66,
    blue: Int = 0xA6,
    width: Int = 32,
    height: Int = 32,
    lastModifiedMs: Long? = null,
  ): File {
    outputFile.parentFile?.mkdirs()
    val w = width.coerceIn(1, 512)
    val h = height.coerceIn(1, 512)

    val out = ByteArrayOutputStream()
    // PNG signature
    out.write(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))

    fun writeChunk(type: String, data: ByteArray) {
      val typeBytes = type.toByteArray(Charsets.US_ASCII)
      out.write(ByteBuffer.allocate(4).putInt(data.size).array())
      out.write(typeBytes)
      out.write(data)
      val crc = CRC32()
      crc.update(typeBytes)
      crc.update(data)
      out.write(ByteBuffer.allocate(4).putInt(crc.value.toInt()).array())
    }

    // IHDR chunk (13 bytes)
    val ihdr = ByteBuffer.allocate(13).apply {
      putInt(w)
      putInt(h)
      put(8.toByte()) // bit depth = 8
      put(2.toByte()) // color type = 2 (truecolor RGB)
      put(0.toByte()) // compression = 0
      put(0.toByte()) // filter = 0
      put(0.toByte()) // interlace = 0
    }.array()
    writeChunk("IHDR", ihdr)

    // Raw scanlines: 1 filter byte (0) + w * 3 RGB bytes per row
    val rowBytes = 1 + w * 3
    val rawPixels = ByteArray(rowBytes * h)
    val rByte = (red and 0xFF).toByte()
    val gByte = (green and 0xFF).toByte()
    val bByte = (blue and 0xFF).toByte()
    for (y in 0 until h) {
      val rowStart = y * rowBytes
      rawPixels[rowStart] = 0 // Filter type None
      for (x in 0 until w) {
        val pxOffset = rowStart + 1 + x * 3
        rawPixels[pxOffset] = rByte
        rawPixels[pxOffset + 1] = gByte
        rawPixels[pxOffset + 2] = bByte
      }
    }

    val deflater = Deflater()
    deflater.setInput(rawPixels)
    deflater.finish()
    val compressed = ByteArrayOutputStream()
    val buf = ByteArray(1024)
    while (!deflater.finished()) {
      val count = deflater.deflate(buf)
      if (count > 0) compressed.write(buf, 0, count)
    }
    deflater.end()
    writeChunk("IDAT", compressed.toByteArray())

    // IEND chunk
    writeChunk("IEND", ByteArray(0))

    FileOutputStream(outputFile).use { fos ->
      fos.write(out.toByteArray())
      fos.flush()
    }
    if (lastModifiedMs != null) {
      outputFile.setLastModified(lastModifiedMs)
    }
    return outputFile
  }
}
