package com.example.toolbox.ui.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.Locale
import java.util.zip.CRC32
import java.util.zip.Deflater
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

const val GALLERY_PREFS_NAME = "gallery_tool_preferences"
const val PREF_KEY_GALLERY_FOLDERS = "gallery_loaded_folders"
const val PREF_KEY_GALLERY_VIEW_MODE = "gallery_view_mode"
const val PREF_KEY_GALLERY_ALBUMS_PER_ROW = "gallery_albums_per_row"
const val PREF_KEY_GALLERY_SORT_ORDER = "gallery_sort_order"

const val MIN_ALBUMS_PER_ROW = 3
const val MAX_ALBUMS_PER_ROW = 6
const val DEFAULT_ALBUMS_PER_ROW = 3

const val MIN_PHOTO_ZOOM = 1f
const val MAX_PHOTO_ZOOM = 5f

val DEFAULT_GALLERY_FOLDER_NAMES: List<String> = listOf("Pictures", "DCIM")
const val DEFAULT_NEW_ALBUM_FOLDER_NAME: String = "Pictures"

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
 * Sorting options for albums and photos in the Gallery tool.
 */
enum class GallerySortOrder(val displayName: String) {
  DATE_DESC("Date (Newest first)"),
  DATE_ASC("Date (Oldest first)"),
  NAME_ASC("Name (A to Z)"),
  NAME_DESC("Name (Z to A)"),
  SIZE_DESC("Size (Largest first)"),
  SIZE_ASC("Size (Smallest first)"),
  ;

  companion object {
    val DEFAULT: GallerySortOrder = DATE_DESC

    fun fromNameOrDefault(name: String?): GallerySortOrder {
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
  val isVideo: Boolean get() = VideoEditorEngine.isVideoFile(file)
}

/**
 * Represents an album (folder) in the Gallery, including its most recent photo and nested counts.
 * When multiple root folders (such as `Pictures` and `DCIM`) are un-nested as if they were the same folder,
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
 * Aggregated info for a multi-selection of albums and photos in the Gallery.
 */
data class GalleryBulkSelectionInfo(
  val albumCount: Int,
  val photoCount: Int,
  val totalMediaFileCount: Int,
  val totalSizeBytes: Long,
  val formattedSize: String,
) {
  val totalItems: Int get() = albumCount + photoCount
}

/**
 * Clipboard operation for multi-selected albums and photos in the Gallery.
 */
enum class GalleryClipboardOperation {
  COPY,
  CUT,
}

/**
 * Holds albums and photos cut or copied from the Gallery for pasting into another album/folder.
 */
data class GalleryClipboardState(
  val operation: GalleryClipboardOperation,
  val albums: List<GalleryAlbum>,
  val photos: List<GalleryPhoto>,
) {
  val totalCount: Int get() = albums.size + photos.size
}

/**
 * Cached recognition metadata for a single photo/media item in the Gallery:
 * - [ocrText]: Recognized text extracted via ML Kit Text Recognition v2 (or embedded PNG text).
 * - [barcodePayloads]: Raw QR code and 1D/2D barcode payloads decoded via ZXing.
 * - [barcodeSummaries]: Human-readable summaries for each decoded code (e.g. `"QR Code: https://..."`).
 * - [autoTags]: Combined automatic tags from ML Kit Image Labeling, OCR document classification,
 *   QR/barcode payload classification, and file/folder metadata heuristics.
 */
data class GalleryPhotoIndexEntry(
  val path: String,
  val lastModifiedMs: Long,
  val sizeBytes: Long,
  val ocrText: String = "",
  val barcodePayloads: List<String> = emptyList(),
  val barcodeSummaries: List<String> = emptyList(),
  val autoTags: List<String> = emptyList(),
  val indexedAtMs: Long = System.currentTimeMillis(),
) {
  val hasOcrText: Boolean get() = ocrText.isNotBlank()
  val hasBarcodes: Boolean get() = barcodePayloads.isNotEmpty()

  fun matchesSignature(file: File): Boolean {
    return file.lastModified() == lastModifiedMs && file.length() == sizeBytes
  }

  fun toJson(): JSONObject {
    return JSONObject().apply {
      put("path", path)
      put("lastModifiedMs", lastModifiedMs)
      put("sizeBytes", sizeBytes)
      put("ocrText", ocrText)
      put("barcodePayloads", JSONArray(barcodePayloads))
      put("barcodeSummaries", JSONArray(barcodeSummaries))
      put("autoTags", JSONArray(autoTags))
      put("indexedAtMs", indexedAtMs)
    }
  }

  companion object {
    fun fromJson(obj: JSONObject): GalleryPhotoIndexEntry? {
      val path = obj.optString("path", "").trim()
      if (path.isEmpty()) return null
      val lastMod = obj.optLong("lastModifiedMs", 0L)
      val size = obj.optLong("sizeBytes", 0L)
      val ocr = obj.optString("ocrText", "")
      val payloadsArr = obj.optJSONArray("barcodePayloads")
      val payloads = mutableListOf<String>()
      if (payloadsArr != null) {
        for (i in 0 until payloadsArr.length()) {
          val v = payloadsArr.optString(i, "").trim()
          if (v.isNotEmpty()) payloads.add(v)
        }
      }
      val summariesArr = obj.optJSONArray("barcodeSummaries")
      val summaries = mutableListOf<String>()
      if (summariesArr != null) {
        for (i in 0 until summariesArr.length()) {
          val v = summariesArr.optString(i, "").trim()
          if (v.isNotEmpty()) summaries.add(v)
        }
      }
      val tagsArr = obj.optJSONArray("autoTags")
      val tags = mutableListOf<String>()
      if (tagsArr != null) {
        for (i in 0 until tagsArr.length()) {
          val v = tagsArr.optString(i, "").trim()
          if (v.isNotEmpty()) tags.add(v)
        }
      }
      val indexedAt = obj.optLong("indexedAtMs", 0L)
      return GalleryPhotoIndexEntry(
        path = path,
        lastModifiedMs = lastMod,
        sizeBytes = size,
        ocrText = ocr,
        barcodePayloads = payloads.distinct(),
        barcodeSummaries = summaries.distinct(),
        autoTags = tags.distinct(),
        indexedAtMs = indexedAt,
      )
    }
  }
}

/**
 * Results of searching and filtering Gallery albums and photos by query and/or smart tag.
 */
data class GallerySearchResult(
  val matchingAlbums: List<GalleryAlbum>,
  val matchingPhotos: List<GalleryPhoto>,
  val matchSnippetsByPath: Map<String, String> = emptyMap(),
) {
  val isEmpty: Boolean get() = matchingAlbums.isEmpty() && matchingPhotos.isEmpty()
}

/**
 * Core engine for the Gallery tool:
 * - Resolves default gallery folders (`Pictures` and `DCIM`)
 * - Scans albums and nested folders for the most recent photo cover image
 * - Supports hierarchical navigation into nested albums/folders
 * - Coerces and calculates pinch-to-zoom column density (`3..6` albums per row)
 */
object GalleryEngine {

  val SUPPORTED_IMAGE_EXTENSIONS: Set<String> = setOf(
    "jpg", "jpeg", "png", "webp", "gif", "bmp", "heic", "heif",
  )

  val SUPPORTED_VIDEO_EXTENSIONS: Set<String> = VideoEditorEngine.SUPPORTED_VIDEO_EXTENSIONS

  const val NO_MEDIA_FILE_NAME: String = ".nomedia"

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
   * Checks whether [file] is a supported photo/image or video media file in the gallery.
   */
  fun isPhotoFile(file: File): Boolean {
    if (!file.exists() || !file.isFile) return false
    if (file.name.startsWith(".")) return false
    val ext = file.extension.lowercase(Locale.ROOT)
    return ext in SUPPORTED_IMAGE_EXTENSIONS ||
      ext in SUPPORTED_VIDEO_EXTENSIONS ||
      FileManagerEngine.isImageFile(file) ||
      VideoEditorEngine.isVideoFile(file)
  }

  /**
   * Checks whether [file] is a supported video file in the gallery.
   */
  fun isVideoFile(file: File): Boolean {
    if (!file.exists() || !file.isFile) return false
    if (file.name.startsWith(".")) return false
    return VideoEditorEngine.isVideoFile(file)
  }

  /**
   * Checks whether [file] is a visible subdirectory representing a nested album/folder.
   */
  fun isAlbumDirectory(file: File): Boolean {
    return file.exists() && file.isDirectory && !file.name.startsWith(".") && !hasNoMediaFile(file)
  }

  /**
   * Checks whether [directory] directly contains a `.nomedia` marker file. Following Android
   * MediaStore conventions, such a directory (and its entire subtree) is excluded from the gallery.
   */
  fun hasNoMediaFile(directory: File): Boolean {
    return try {
      File(directory, NO_MEDIA_FILE_NAME).exists()
    } catch (_: Throwable) {
      false
    }
  }

  /**
   * Checks whether [directory] is hidden from the gallery because it, or any of its ancestor
   * directories, contains a `.nomedia` marker file.
   */
  fun isHiddenByNoMedia(directory: File): Boolean {
    var current: File? = directory.absoluteFile
    while (current != null) {
      if (hasNoMediaFile(current)) return true
      current = current.parentFile
    }
    return false
  }

  /**
   * Returns the default folders loaded into the gallery (`Pictures` and `DCIM`) under [rootDirectory].
   */
  fun getDefaultGalleryFolders(
    rootDirectory: File = FileManagerEngine.getUserRootDirectory(),
  ): List<File> {
    return DEFAULT_GALLERY_FOLDER_NAMES.map { folderName ->
      File(rootDirectory, folderName)
    }
  }

  /**
   * Returns the default location for creating a new top-level album (`Pictures`) under [rootDirectory].
   */
  fun getDefaultNewAlbumFolder(
    rootDirectory: File = FileManagerEngine.getUserRootDirectory(),
  ): File {
    return File(rootDirectory, DEFAULT_NEW_ALBUM_FOLDER_NAME)
  }

  /**
   * Resolves the list of folders loaded into the gallery, starting with the default `Pictures` and `DCIM`
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
      if (hasNoMediaFile(currentDir)) return
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
      if (hasNoMediaFile(currentDir)) return
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
      if (dir.exists() && dir.isDirectory && !hasNoMediaFile(dir)) {
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
   * Loads the un-nested root contents of the Gallery by combining `Pictures` and `DCIM`
   * (plus any [additionalPaths]) as if they were the same folder:
   * - [AlbumContents.nestedAlbums]: All subdirectories inside `Pictures` and `DCIM` (merging same-named
   *   subfolders), sorted alphabetically.
   * - [AlbumContents.photos]: All direct photo files inside `Pictures` and `DCIM`, sorted newest first.
   */
  fun loadRootContents(
    rootDirectory: File = FileManagerEngine.getUserRootDirectory(),
    folders: List<File>? = null,
    additionalPaths: List<String> = emptyList(),
  ): AlbumContents {
    val targetFolders = (folders ?: resolveGalleryFolders(rootDirectory, additionalPaths))
      .filterNot { isHiddenByNoMedia(it) }
    return loadMergedAlbumContents(targetFolders)
  }

  /**
   * Loads the root-level albums for the Gallery by un-nesting the contents of `Pictures` and `DCIM`
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
      if (dir.exists() && dir.isDirectory && !hasNoMediaFile(dir)) {
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
   * Sorts [albums] according to [sortOrder].
   */
  fun sortAlbums(
    albums: List<GalleryAlbum>,
    sortOrder: GallerySortOrder,
  ): List<GalleryAlbum> {
    val comparator: Comparator<GalleryAlbum> = when (sortOrder) {
      GallerySortOrder.DATE_DESC ->
        compareByDescending<GalleryAlbum> { it.lastModifiedMs }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      GallerySortOrder.DATE_ASC ->
        compareBy<GalleryAlbum> { it.lastModifiedMs }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      GallerySortOrder.NAME_ASC ->
        compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      GallerySortOrder.NAME_DESC ->
        compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name }
      GallerySortOrder.SIZE_DESC ->
        compareByDescending<GalleryAlbum> { it.totalPhotoCount }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      GallerySortOrder.SIZE_ASC ->
        compareBy<GalleryAlbum> { it.totalPhotoCount }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
    }
    return albums.sortedWith(comparator)
  }

  /**
   * Sorts [photos] according to [sortOrder].
   */
  fun sortPhotos(
    photos: List<GalleryPhoto>,
    sortOrder: GallerySortOrder,
  ): List<GalleryPhoto> {
    val comparator: Comparator<GalleryPhoto> = when (sortOrder) {
      GallerySortOrder.DATE_DESC ->
        compareByDescending<GalleryPhoto> { it.lastModifiedMs }
          .thenByDescending(String.CASE_INSENSITIVE_ORDER) { it.name }
      GallerySortOrder.DATE_ASC ->
        compareBy<GalleryPhoto> { it.lastModifiedMs }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      GallerySortOrder.NAME_ASC ->
        compareBy<GalleryPhoto, String>(String.CASE_INSENSITIVE_ORDER) { it.name }
          .thenByDescending { it.lastModifiedMs }
      GallerySortOrder.NAME_DESC ->
        compareByDescending<GalleryPhoto, String>(String.CASE_INSENSITIVE_ORDER) { it.name }
          .thenByDescending { it.lastModifiedMs }
      GallerySortOrder.SIZE_DESC ->
        compareByDescending<GalleryPhoto> { it.sizeBytes }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      GallerySortOrder.SIZE_ASC ->
        compareBy<GalleryPhoto> { it.sizeBytes }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
    }
    return photos.sortedWith(comparator)
  }

  /**
   * Collects all distinct media [File]s across the selected [albums] (recursively up to [maxDepth])
   * and selected [photos].
   */
  fun collectMediaFiles(
    albums: List<GalleryAlbum>,
    photos: List<GalleryPhoto>,
    maxDepth: Int = 8,
  ): List<File> {
    val result = LinkedHashMap<String, File>()

    fun scan(dir: File, depth: Int) {
      if (depth > maxDepth) return
      if (hasNoMediaFile(dir)) return
      val children = try {
        dir.listFiles()
      } catch (_: Throwable) {
        null
      } ?: return

      for (child in children) {
        if (child.name.startsWith(".")) continue
        if (child.isFile && isPhotoFile(child)) {
          result.putIfAbsent(child.absolutePath, child)
        } else if (child.isDirectory && depth < maxDepth) {
          scan(child, depth + 1)
        }
      }
    }

    for (album in albums) {
      for (dir in album.directories) {
        if (dir.exists() && dir.isDirectory) {
          scan(dir, 0)
        }
      }
    }
    for (photo in photos) {
      if (photo.file.exists() && photo.file.isFile) {
        result.putIfAbsent(photo.file.absolutePath, photo.file)
      }
    }
    return result.values.toList()
  }

  /**
   * Computes aggregated size and count metadata for a multi-selection of [albums] and [photos].
   */
  fun getBulkSelectionInfo(
    albums: List<GalleryAlbum>,
    photos: List<GalleryPhoto>,
    maxDepth: Int = 8,
  ): GalleryBulkSelectionInfo {
    val mediaFiles = collectMediaFiles(albums, photos, maxDepth = maxDepth)
    val totalBytes = mediaFiles.sumOf { file ->
      try {
        file.length()
      } catch (_: Throwable) {
        0L
      }
    }
    return GalleryBulkSelectionInfo(
      albumCount = albums.size,
      photoCount = photos.size,
      totalMediaFileCount = mediaFiles.size,
      totalSizeBytes = totalBytes,
      formattedSize = FileManagerEngine.formatFileSize(totalBytes),
    )
  }

  /**
   * Deletes all selected [albums] (including all backing [GalleryAlbum.directories]) and [photos].
   * Returns the total number of selected items (albums + photos) deleted.
   */
  fun deleteSelectedItems(
    albums: List<GalleryAlbum>,
    photos: List<GalleryPhoto>,
  ): Int {
    var deletedCount = 0
    for (album in albums) {
      var anyDeleted = false
      for (dir in album.directories) {
        if (FileManagerEngine.deleteFileOrDirectory(dir)) {
          anyDeleted = true
        }
      }
      if (anyDeleted) deletedCount++
    }
    for (photo in photos) {
      if (FileManagerEngine.deleteFileOrDirectory(photo.file)) {
        deletedCount++
      }
    }
    return deletedCount
  }

  /**
   * Copies all selected [albums] and [photos] into [destinationDir].
   * Returns the total number of selected items (albums + photos) copied.
   */
  fun copySelectedItems(
    albums: List<GalleryAlbum>,
    photos: List<GalleryPhoto>,
    destinationDir: File,
  ): Int {
    if (!destinationDir.exists()) {
      destinationDir.mkdirs()
    }
    if (!destinationDir.isDirectory) return 0

    var copiedCount = 0
    for (album in albums) {
      val validDirs = album.directories.filter { it.exists() && it.isDirectory }
      if (validDirs.isEmpty()) continue
      if (validDirs.size == 1) {
        copiedCount += FileManagerEngine.copyMultipleFiles(validDirs, destinationDir)
      } else {
        try {
          var targetAlbumDir = File(destinationDir, album.name)
          if (targetAlbumDir.exists() && validDirs.none { it.absolutePath.equals(targetAlbumDir.absolutePath, ignoreCase = true) }) {
            var i = 1
            do {
              targetAlbumDir = File(destinationDir, "${album.name} (copy $i)")
              i++
            } while (targetAlbumDir.exists())
          }
          targetAlbumDir.mkdirs()
          for (srcDir in validDirs) {
            if (srcDir.absolutePath.equals(targetAlbumDir.absolutePath, ignoreCase = true)) continue
            val children = srcDir.listFiles().orEmpty().toList()
            FileManagerEngine.copyMultipleFiles(children, targetAlbumDir)
          }
          copiedCount++
        } catch (_: Throwable) {}
      }
    }
    if (photos.isNotEmpty()) {
      copiedCount += FileManagerEngine.copyMultipleFiles(photos.map { it.file }, destinationDir)
    }
    return copiedCount
  }

  /**
   * Moves all selected [albums] and [photos] into [destinationDir].
   * Returns the total number of selected items (albums + photos) moved.
   */
  fun moveSelectedItems(
    albums: List<GalleryAlbum>,
    photos: List<GalleryPhoto>,
    destinationDir: File,
  ): Int {
    if (!destinationDir.exists()) {
      destinationDir.mkdirs()
    }
    if (!destinationDir.isDirectory) return 0

    var movedCount = 0
    for (album in albums) {
      val validDirs = album.directories.filter { it.exists() && it.isDirectory }
      if (validDirs.isEmpty()) continue
      if (validDirs.size == 1) {
        val singleDir = validDirs.first()
        if (singleDir.parentFile?.absolutePath.equals(destinationDir.absolutePath, ignoreCase = true)) {
          movedCount++
        } else {
          movedCount += FileManagerEngine.moveMultipleFiles(validDirs, destinationDir)
        }
      } else {
        try {
          val targetAlbumDir = File(destinationDir, album.name)
          targetAlbumDir.mkdirs()
          for (srcDir in validDirs) {
            if (srcDir.absolutePath.equals(targetAlbumDir.absolutePath, ignoreCase = true)) continue
            val children = srcDir.listFiles().orEmpty().toList()
            FileManagerEngine.moveMultipleFiles(children, targetAlbumDir)
            srcDir.deleteRecursively()
          }
          movedCount++
        } catch (_: Throwable) {}
      }
    }
    if (photos.isNotEmpty()) {
      movedCount += FileManagerEngine.moveMultipleFiles(photos.map { it.file }, destinationDir)
    }
    return movedCount
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

    if (VideoEditorEngine.isVideoFile(file)) {
      val retriever = MediaMetadataRetriever()
      return try {
        retriever.setDataSource(file.absolutePath)
        val frame = retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        if (frame != null) {
          val key = thumbnailCacheKey(file, reqSizePx)
          synchronized(thumbnailCacheLock) {
            thumbnailCache[key] = frame
          }
        }
        frame
      } catch (_: Throwable) {
        null
      } finally {
        try {
          retriever.release()
        } catch (_: Throwable) {}
      }
    }

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

  // =========================================================================
  // Smart Gallery Indexer: Background OCR, QR/Barcode & Automatic Tagging
  // =========================================================================

  const val GALLERY_INDEX_CACHE_FILE_NAME = "gallery_smart_index_v2.json"
  const val DEFAULT_ML_KIT_LABEL_CONFIDENCE_THRESHOLD = 0.80f
  private const val PNG_TEXT_KEYWORD_OCR = "OCR_TEXT"
  private const val PNG_TEXT_KEYWORD_LABELS = "IMAGE_LABELS"

  private val photoIndexCacheLock = Any()
  private val photoIndexCache = LinkedHashMap<String, GalleryPhotoIndexEntry>()

  /**
   * Returns the default persistent JSON cache file for Gallery OCR, barcode, and auto-tag indexing.
   */
  fun getDefaultIndexCacheFile(context: Context): File {
    return File(context.filesDir, GALLERY_INDEX_CACHE_FILE_NAME)
  }

  /**
   * Returns a cached [GalleryPhotoIndexEntry] for [file] if one exists in memory and its
   * `lastModified` + `sizeBytes` signature still matches [file].
   */
  fun getCachedPhotoIndex(file: File): GalleryPhotoIndexEntry? {
    val path = file.absolutePath
    synchronized(photoIndexCacheLock) {
      val cached = photoIndexCache[path] ?: return null
      return if (cached.matchesSignature(file)) cached else null
    }
  }

  /**
   * Returns a snapshot of all currently cached [GalleryPhotoIndexEntry] items keyed by file path.
   */
  fun getAllCachedPhotoIndices(): Map<String, GalleryPhotoIndexEntry> {
    synchronized(photoIndexCacheLock) {
      return LinkedHashMap(photoIndexCache)
    }
  }

  /**
   * Clears the in-memory photo recognition index cache.
   */
  fun clearPhotoIndexCache() {
    synchronized(photoIndexCacheLock) {
      photoIndexCache.clear()
    }
  }

  /**
   * Stores or updates [entry] in the in-memory photo recognition index cache.
   */
  fun putCachedPhotoIndex(entry: GalleryPhotoIndexEntry) {
    synchronized(photoIndexCacheLock) {
      photoIndexCache[entry.path] = entry
    }
  }

  /**
   * Loads persisted [GalleryPhotoIndexEntry] records from [cacheFile] on disk and merges them into
   * the in-memory cache, pruning entries whose files no longer exist or have modified signatures.
   */
  fun loadIndexCacheFromDisk(cacheFile: File?): Map<String, GalleryPhotoIndexEntry> {
    if (cacheFile == null || !cacheFile.exists() || !cacheFile.isFile) {
      return getAllCachedPhotoIndices()
    }
    try {
      val raw = cacheFile.readText(Charsets.UTF_8)
      if (raw.isNotBlank()) {
        val root = JSONObject(raw)
        val entriesArray = root.optJSONArray("entries")
        if (entriesArray != null) {
          synchronized(photoIndexCacheLock) {
            for (i in 0 until entriesArray.length()) {
              val obj = entriesArray.optJSONObject(i) ?: continue
              val parsed = GalleryPhotoIndexEntry.fromJson(obj) ?: continue
              val file = File(parsed.path)
              if (file.exists() && file.isFile && parsed.matchesSignature(file)) {
                photoIndexCache[parsed.path] = parsed
              }
            }
          }
        }
      }
    } catch (_: Throwable) {
      // Ignore corrupt cache file and rebuild on next pass
    }
    return getAllCachedPhotoIndices()
  }

  /**
   * Persists the provided [entries] map (or current in-memory index cache) to [cacheFile] on disk.
   */
  fun saveIndexCacheToDisk(
    cacheFile: File?,
    entries: Map<String, GalleryPhotoIndexEntry> = getAllCachedPhotoIndices(),
  ) {
    if (cacheFile == null) return
    try {
      cacheFile.parentFile?.mkdirs()
      val arr = JSONArray()
      for (entry in entries.values) {
        arr.put(entry.toJson())
      }
      val root = JSONObject().apply {
        put("version", 1)
        put("entries", arr)
      }
      cacheFile.writeText(root.toString(), Charsets.UTF_8)
    } catch (_: Throwable) {
      // Best-effort persistence
    }
  }

  /**
   * Writes a spec-compliant RGB PNG image file with embedded OCR text and/or visual labels in PNG
   * `tEXt` chunks (and renders the text onto the image when Android Canvas is available).
   * Useful for deterministic unit and instrumented testing as well as synthetic test documents.
   */
  fun writeTextDocumentPng(
    outputFile: File,
    ocrText: String,
    imageLabels: List<String> = emptyList(),
    red: Int = 0xFA,
    green: Int = 0xFA,
    blue: Int = 0xFC,
    width: Int = 64,
    height: Int = 64,
    lastModifiedMs: Long? = null,
  ): File {
    outputFile.parentFile?.mkdirs()
    val w = width.coerceIn(16, 512)
    val h = height.coerceIn(16, 512)

    val out = ByteArrayOutputStream()
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

    val ihdr = ByteBuffer.allocate(13).apply {
      putInt(w)
      putInt(h)
      put(8.toByte())
      put(2.toByte())
      put(0.toByte())
      put(0.toByte())
      put(0.toByte())
    }.array()
    writeChunk("IHDR", ihdr)

    if (ocrText.isNotBlank()) {
      val keyBytes = PNG_TEXT_KEYWORD_OCR.toByteArray(Charsets.ISO_8859_1)
      val valBytes = ocrText.toByteArray(Charsets.UTF_8)
      val textChunk = ByteArray(keyBytes.size + 1 + valBytes.size)
      System.arraycopy(keyBytes, 0, textChunk, 0, keyBytes.size)
      textChunk[keyBytes.size] = 0
      System.arraycopy(valBytes, 0, textChunk, keyBytes.size + 1, valBytes.size)
      writeChunk("tEXt", textChunk)
    }

    if (imageLabels.isNotEmpty()) {
      val keyBytes = PNG_TEXT_KEYWORD_LABELS.toByteArray(Charsets.ISO_8859_1)
      val valBytes = imageLabels.joinToString("|").toByteArray(Charsets.UTF_8)
      val labelChunk = ByteArray(keyBytes.size + 1 + valBytes.size)
      System.arraycopy(keyBytes, 0, labelChunk, 0, keyBytes.size)
      labelChunk[keyBytes.size] = 0
      System.arraycopy(valBytes, 0, labelChunk, keyBytes.size + 1, valBytes.size)
      writeChunk("tEXt", labelChunk)
    }

    val rowBytes = 1 + w * 3
    val rawPixels = ByteArray(rowBytes * h)
    val rByte = (red and 0xFF).toByte()
    val gByte = (green and 0xFF).toByte()
    val bByte = (blue and 0xFF).toByte()
    for (y in 0 until h) {
      val rowStart = y * rowBytes
      rawPixels[rowStart] = 0
      for (x in 0 until w) {
        val pxOffset = rowStart + 1 + x * 3
        // Draw dark horizontal text-like bands on every 6th row when ocrText is non-blank
        val isTextBand = ocrText.isNotBlank() && (y in 10..(h - 10)) && (y % 6 == 0) && (x in 6..(w - 6))
        if (isTextBand) {
          rawPixels[pxOffset] = 0x20.toByte()
          rawPixels[pxOffset + 1] = 0x20.toByte()
          rawPixels[pxOffset + 2] = 0x24.toByte()
        } else {
          rawPixels[pxOffset] = rByte
          rawPixels[pxOffset + 1] = gByte
          rawPixels[pxOffset + 2] = bByte
        }
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

  /**
   * Reads embedded `tEXt` metadata chunks (`OCR_TEXT` and `IMAGE_LABELS`) from a PNG file if present.
   */
  fun readEmbeddedPngMetadata(file: File): Pair<String, List<String>> {
    if (!file.exists() || !file.isFile || file.length() < 24L) {
      return "" to emptyList()
    }
    return try {
      val bytes = file.readBytes()
      val pngSig = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
      for (i in pngSig.indices) {
        if (bytes[i] != pngSig[i]) return "" to emptyList()
      }
      var offset = 8
      var ocrText = ""
      val labels = mutableListOf<String>()
      while (offset + 8 <= bytes.size) {
        val length = ByteBuffer.wrap(bytes, offset, 4).int
        val type = String(bytes, offset + 4, 4, Charsets.US_ASCII)
        val dataStart = offset + 8
        if (length < 0 || dataStart + length > bytes.size) break
        if (type == "tEXt" && length > 1) {
          var nullIdx = -1
          for (i in 0 until length) {
            if (bytes[dataStart + i].toInt() == 0) {
              nullIdx = i
              break
            }
          }
          if (nullIdx > 0) {
            val key = String(bytes, dataStart, nullIdx, Charsets.ISO_8859_1)
            val value = String(bytes, dataStart + nullIdx + 1, length - nullIdx - 1, Charsets.UTF_8)
            if (key == PNG_TEXT_KEYWORD_OCR && value.isNotBlank()) {
              ocrText = value.trim()
            } else if (key == PNG_TEXT_KEYWORD_LABELS && value.isNotBlank()) {
              value.split("|").map { it.trim() }.filter { it.isNotEmpty() }.forEach { labels.add(it) }
            }
          }
        } else if (type == "IEND") {
          break
        }
        offset = dataStart + length + 4
      }
      ocrText to labels.distinct()
    } catch (_: Throwable) {
      "" to emptyList()
    }
  }

  /**
   * Runs Google ML Kit on-device Image Labeling (`com.google.mlkit:image-labeling`) on [bitmap]
   * and returns the top confident visual scene/object labels (e.g. `"Food"`, `"Nature"`, `"Pet"`,
   * `"Plant"`, `"Sky"`, `"Building"`, `"Car"`).
   */
  suspend fun recognizeImageLabelsWithMlKit(
    bitmap: Bitmap,
    rotationDegrees: Int = 0,
    confidenceThreshold: Float = DEFAULT_ML_KIT_LABEL_CONFIDENCE_THRESHOLD,
    maxLabels: Int = 5,
  ): List<String> {
    if (bitmap.width <= 16 || bitmap.height <= 16) return emptyList()
    val effectiveThreshold = confidenceThreshold.coerceIn(0.3f, 0.98f)
    return try {
      val options = ImageLabelerOptions.Builder()
        .setConfidenceThreshold(effectiveThreshold)
        .build()
      val labeler = ImageLabeling.getClient(options)
      val inputImage = InputImage.fromBitmap(bitmap, rotationDegrees)
      val rawLabels = suspendCancellableCoroutine<List<String>> { continuation ->
        labeler.process(inputImage)
          .addOnSuccessListener { results ->
            labeler.close()
            val mapped = results
              .filter { it.confidence >= effectiveThreshold }
              .sortedByDescending { it.confidence }
              .mapNotNull { item ->
                val text = item.text.trim()
                if (text.isNotEmpty()) {
                  text.replaceFirstChar { ch ->
                    if (ch.isLowerCase()) ch.titlecase(Locale.US) else ch.toString()
                  }
                } else {
                  null
                }
              }
              .distinct()
              .take(maxLabels)
            if (continuation.isActive) {
              continuation.resume(mapped)
            }
          }
          .addOnFailureListener {
            labeler.close()
            if (continuation.isActive) {
              continuation.resume(emptyList())
            }
          }
      }
      rawLabels
    } catch (_: Throwable) {
      emptyList()
    }
  }

  /**
   * Derives automatic smart tags for a gallery media [file] by combining:
   * 1. ML Kit visual scene/object labels ([imageLabels])
   * 2. Decoded QR code and 1D/2D barcode classifications ([scannedCodes])
   * 3. Recognized OCR text content ([ocrText] — e.g. Receipts, Documents, Notes/Checklists, Links)
   * 4. File & folder metadata heuristics (Screenshots, Camera, Scanned, Videos, GIFs)
   */
  fun deriveAutoTagsForPhoto(
    file: File,
    ocrText: String,
    scannedCodes: List<CameraScannedCode>,
    imageLabels: List<String> = emptyList(),
  ): List<String> {
    val tags = LinkedHashSet<String>()
    val lowerName = file.name.lowercase(Locale.ROOT)
    val lowerParent = file.parentFile?.name?.lowercase(Locale.ROOT).orEmpty()

    // 1. File / folder metadata tags
    if (isVideoFile(file)) {
      tags.add("Video")
    }
    if (file.extension.equals("gif", ignoreCase = true)) {
      tags.add("GIF")
    }
    if (lowerName.contains("screenshot") || lowerParent.contains("screenshot")) {
      tags.add("Screenshot")
    }
    if (lowerName.startsWith("scan_") || lowerName.startsWith("ocr_") || lowerParent.contains("scan")) {
      tags.add("Scanned")
    }
    if (lowerParent == "camera" || lowerParent == "dcim" || lowerName.startsWith("img_") || lowerName.startsWith("pxl_")) {
      tags.add("Camera")
    }

    // 2. QR & Barcode tags
    for (code in scannedCodes) {
      if (code.format.is2D) {
        tags.add("QR Code")
      } else {
        tags.add("Barcode")
      }
      when (code.payloadType) {
        CameraScanPayloadType.URL -> tags.add("Link")
        CameraScanPayloadType.WIFI -> tags.add("Wi-Fi")
        CameraScanPayloadType.CONTACT,
        CameraScanPayloadType.EMAIL,
        CameraScanPayloadType.PHONE,
        CameraScanPayloadType.SMS -> tags.add("Contact")
        CameraScanPayloadType.PRODUCT,
        CameraScanPayloadType.ISBN -> tags.add("Product")
        CameraScanPayloadType.CALENDAR -> tags.add("Calendar")
        CameraScanPayloadType.GEO -> tags.add("Location")
        else -> {}
      }
    }

    // 3. OCR Text & Document classification tags
    val trimmedOcr = ocrText.trim()
    if (trimmedOcr.isNotEmpty()) {
      tags.add("Text")
      val lowerOcr = trimmedOcr.lowercase(Locale.ROOT)
      val lines = trimmedOcr.lines().map { it.trim() }.filter { it.isNotEmpty() }
      val wordCount = trimmedOcr.split(Regex("\\s+")).count { it.isNotEmpty() }

      val hasCurrencyAmount = Regex("""(\$|€|£)\s*\d+(\.\d{2})?""").containsMatchIn(trimmedOcr)
      val hasReceiptKeywords = listOf("receipt", "subtotal", "total", "tax", "invoice", "amount due", "total paid")
        .any { lowerOcr.contains(it) }
      if (hasReceiptKeywords || (hasCurrencyAmount && lines.size >= 2)) {
        tags.add("Receipt")
        tags.add("Document")
      } else if (lines.size >= 3 || wordCount >= 10) {
        tags.add("Document")
      }

      val hasBulletOrNumberedLines = lines.count { line ->
        line.startsWith("- ") ||
          line.startsWith("* ") ||
          line.startsWith("• ") ||
          line.startsWith("[ ]") ||
          line.startsWith("[x]", ignoreCase = true) ||
          Regex("""^\d+\.\s+.*""").matches(line)
      } >= 2
      if (hasBulletOrNumberedLines || lowerOcr.contains("notes") || lowerOcr.contains("checklist")) {
        tags.add("Notes")
      }

      if (lowerOcr.contains("http://") || lowerOcr.contains("https://") || lowerOcr.contains("www.")) {
        tags.add("Link")
      }
    }

    // 4. ML Kit Image Labeling visual scene & object tags
    for (rawLabel in imageLabels) {
      val clean = rawLabel.trim()
      if (clean.isNotEmpty()) {
        val formatted = clean.replaceFirstChar { ch ->
          if (ch.isLowerCase()) ch.titlecase(Locale.US) else ch.toString()
        }
        tags.add(formatted)
      }
    }

    return tags.toList()
  }

  /**
   * Indexes a single gallery media [file] on a background thread, extracting:
   * - QR codes & 1D/2D barcodes via ZXing
   * - Recognized text via Google ML Kit Text Recognition v2 (with embedded PNG metadata fallback)
   * - Visual scene & object labels via Google ML Kit Image Labeling
   * - Heuristic smart tags
   *
   * Results are cached in memory keyed by path + `lastModified` + `sizeBytes`.
   */
  suspend fun indexSinglePhoto(
    file: File,
    forceReindex: Boolean = false,
  ): GalleryPhotoIndexEntry = withContext(Dispatchers.Default) {
    if (!file.exists() || !file.isFile) {
      return@withContext GalleryPhotoIndexEntry(
        path = file.absolutePath,
        lastModifiedMs = 0L,
        sizeBytes = 0L,
      )
    }

    if (!forceReindex) {
      getCachedPhotoIndex(file)?.let { return@withContext it }
    }

    val lastMod = file.lastModified()
    val sizeBytes = file.length()

    if (isVideoFile(file)) {
      val videoTags = deriveAutoTagsForPhoto(
        file = file,
        ocrText = "",
        scannedCodes = emptyList(),
        imageLabels = emptyList(),
      )
      val entry = GalleryPhotoIndexEntry(
        path = file.absolutePath,
        lastModifiedMs = lastMod,
        sizeBytes = sizeBytes,
        ocrText = "",
        barcodePayloads = emptyList(),
        barcodeSummaries = emptyList(),
        autoTags = videoTags,
      )
      putCachedPhotoIndex(entry)
      return@withContext entry
    }

    ensureActive()

    // 1. Check embedded PNG metadata (fast & deterministic for synthetic/test images)
    val (embeddedOcr, embeddedLabels) = readEmbeddedPngMetadata(file)

    // 2. Decode bitmap downsampled to 1024px max edge for ML Kit & ZXing
    val bitmap: Bitmap? = try {
      val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      BitmapFactory.decodeFile(file.absolutePath, boundsOpts)
      var sampleSize = 1
      val maxDim = maxOf(boundsOpts.outWidth, boundsOpts.outHeight)
      while (maxDim / sampleSize > 1024) {
        sampleSize *= 2
      }
      val decodeOpts = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.ARGB_8888
      }
      BitmapFactory.decodeFile(file.absolutePath, decodeOpts)
    } catch (_: Throwable) {
      null
    }

    ensureActive()

    // 3. Decode QR codes & barcodes (handles both Android Bitmap and pure-JVM PNG fallback)
    val scannedCodes = if (bitmap != null && bitmap.width > 0 && bitmap.height > 0) {
      CameraEngine.decodeCodesFromBitmap(bitmap)
    } else {
      CameraEngine.decodeCodesFromImageFile(file)
    }

    ensureActive()

    // 4. Run ML Kit Text Recognition v2 (OCR) if bitmap is available
    val mlKitOcrDoc = if (bitmap != null && bitmap.width > 16 && bitmap.height > 16) {
      CameraEngine.recognizeTextFromBitmapWithMlKit(bitmap)
    } else {
      null
    }
    val mlKitRawText = mlKitOcrDoc?.rawText.orEmpty()
    val finalOcrText = when {
      mlKitRawText.isNotBlank() && embeddedOcr.isNotBlank() -> {
        if (mlKitRawText.contains(embeddedOcr, ignoreCase = true)) {
          mlKitRawText
        } else {
          "$mlKitRawText\n$embeddedOcr"
        }
      }
      mlKitRawText.isNotBlank() -> mlKitRawText
      else -> embeddedOcr
    }

    ensureActive()

    // 5. Run ML Kit Image Labeling for automatic visual scene/object tags
    val mlKitLabels = if (bitmap != null && bitmap.width > 16 && bitmap.height > 16) {
      recognizeImageLabelsWithMlKit(bitmap)
    } else {
      emptyList()
    }
    val combinedVisualLabels = (embeddedLabels + mlKitLabels).distinct()

    val barcodePayloads = scannedCodes.map { it.rawValue.trim() }.filter { it.isNotEmpty() }.distinct()
    val barcodeSummaries = scannedCodes.map { "${it.format.displayName}: ${it.title}" }.distinct()
    val autoTags = deriveAutoTagsForPhoto(
      file = file,
      ocrText = finalOcrText,
      scannedCodes = scannedCodes,
      imageLabels = combinedVisualLabels,
    )

    val entry = GalleryPhotoIndexEntry(
      path = file.absolutePath,
      lastModifiedMs = lastMod,
      sizeBytes = sizeBytes,
      ocrText = finalOcrText,
      barcodePayloads = barcodePayloads,
      barcodeSummaries = barcodeSummaries,
      autoTags = autoTags,
    )
    putCachedPhotoIndex(entry)
    entry
  }

  /**
   * Collects all distinct [GalleryPhoto] items across [rootContents] (including nested albums up to
   * [maxDepth]) so background indexing and global search cover the entire gallery.
   */
  fun collectAllGalleryPhotos(
    rootContents: AlbumContents,
    maxDepth: Int = 8,
  ): List<GalleryPhoto> {
    val allFiles = collectMediaFiles(
      albums = rootContents.nestedAlbums,
      photos = rootContents.photos,
      maxDepth = maxDepth,
    )
    return allFiles
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
  }

  /**
   * Indexes a list of [photos] in the background, skipping any photo whose signature is already
   * cached. Invokes [onProgress] as unindexed photos are processed and saves the updated cache to
   * [cacheFile].
   */
  suspend fun indexPhotosInBackground(
    photos: List<GalleryPhoto>,
    cacheFile: File? = null,
    onProgress: (indexedCount: Int, totalToIndex: Int, snapshot: Map<String, GalleryPhotoIndexEntry>) -> Unit = { _, _, _ -> },
  ): Map<String, GalleryPhotoIndexEntry> = withContext(Dispatchers.Default) {
    loadIndexCacheFromDisk(cacheFile)

    val unindexed = photos.filter { photo ->
      getCachedPhotoIndex(photo.file) == null
    }

    if (unindexed.isEmpty()) {
      val current = getAllCachedPhotoIndices()
      onProgress(0, 0, current)
      return@withContext current
    }

    val totalToIndex = unindexed.size
    onProgress(0, totalToIndex, getAllCachedPhotoIndices())

    unindexed.forEachIndexed { idx, photo ->
      ensureActive()
      indexSinglePhoto(photo.file)
      val completed = idx + 1
      val snapshot = getAllCachedPhotoIndices()
      onProgress(completed, totalToIndex, snapshot)
      if (completed % 4 == 0 || completed == totalToIndex) {
        saveIndexCacheToDisk(cacheFile, snapshot)
      }
    }

    getAllCachedPhotoIndices()
  }

  /**
   * Extracts all available automatic tags across [photos] along with their frequency counts,
   * ordered with high-utility recognition tags first followed by count descending.
   */
  fun extractAvailableAutoTags(
    photos: List<GalleryPhoto>,
    indexMap: Map<String, GalleryPhotoIndexEntry>,
  ): List<Pair<String, Int>> {
    val counts = LinkedHashMap<String, Int>()
    for (photo in photos) {
      val entry = indexMap[photo.path] ?: continue
      for (tag in entry.autoTags) {
        counts[tag] = (counts[tag] ?: 0) + 1
      }
    }
    val priorityOrder = listOf(
      "Text",
      "Document",
      "Receipt",
      "Notes",
      "QR Code",
      "Barcode",
      "Link",
      "Wi-Fi",
      "Product",
      "Screenshot",
      "Scanned",
      "Camera",
      "Video",
    )
    return counts.entries
      .sortedWith(
        compareBy<Map.Entry<String, Int>> { entry ->
          val priorityIdx = priorityOrder.indexOfFirst { it.equals(entry.key, ignoreCase = true) }
          if (priorityIdx >= 0) priorityIdx else 100
        }
          .thenByDescending { it.value }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.key }
      )
      .map { it.key to it.value }
  }

  /**
   * Filters and searches [albums] and [photos] by [searchQuery] (matching filenames, recognized OCR
   * text, decoded QR/barcode payloads, and automatic tags) and optional [selectedTag].
   *
   * When [searchQuery] or [selectedTag] is active and [allGalleryPhotos] is provided, searches across
   * all photos in the current scope (e.g., all gallery photos when at root, or all photos in the
   * current album subtree) and generates a concise match snippet for each matched photo.
   */
  fun searchGallery(
    albums: List<GalleryAlbum>,
    directPhotos: List<GalleryPhoto>,
    allScopePhotos: List<GalleryPhoto> = directPhotos,
    indexMap: Map<String, GalleryPhotoIndexEntry>,
    searchQuery: String,
    selectedTag: String? = null,
    sortOrder: GallerySortOrder = GallerySortOrder.DATE_DESC,
  ): GallerySearchResult {
    val query = searchQuery.trim()
    val tagFilter = selectedTag?.trim()?.takeIf { it.isNotEmpty() }

    if (query.isEmpty() && tagFilter == null) {
      return GallerySearchResult(
        matchingAlbums = sortAlbums(albums, sortOrder),
        matchingPhotos = sortPhotos(directPhotos, sortOrder),
        matchSnippetsByPath = emptyMap(),
      )
    }

    val matchingAlbums = if (tagFilter != null) {
      emptyList()
    } else {
      sortAlbums(
        albums.filter { album ->
          album.name.contains(query, ignoreCase = true)
        },
        sortOrder,
      )
    }

    val candidatePhotos = if (allScopePhotos.isNotEmpty()) allScopePhotos else directPhotos
    val snippets = LinkedHashMap<String, String>()
    val matchedPhotos = mutableListOf<GalleryPhoto>()

    for (photo in candidatePhotos) {
      val entry = indexMap[photo.path]

      // 1. Check tag filter if selected
      if (tagFilter != null) {
        val hasTag = entry?.autoTags?.any { it.equals(tagFilter, ignoreCase = true) } == true
        if (!hasTag) continue
      }

      // 2. Check text query if non-empty
      if (query.isEmpty()) {
        matchedPhotos.add(photo)
        val snippet = buildMatchSnippetForTag(entry, tagFilter!!)
        if (snippet.isNotEmpty()) {
          snippets[photo.path] = snippet
        }
        continue
      }

      val nameMatch = photo.name.contains(query, ignoreCase = true)
      val ocrMatchLine = entry?.ocrText
        ?.lineSequence()
        ?.map { it.trim() }
        ?.firstOrNull { it.contains(query, ignoreCase = true) }
      val barcodeMatch = entry?.barcodePayloads
        ?.firstOrNull { it.contains(query, ignoreCase = true) }
        ?: entry?.barcodeSummaries?.firstOrNull { it.contains(query, ignoreCase = true) }
      val tagMatch = entry?.autoTags
        ?.firstOrNull { it.contains(query, ignoreCase = true) }

      if (nameMatch || ocrMatchLine != null || barcodeMatch != null || tagMatch != null) {
        matchedPhotos.add(photo)
        val snippet = when {
          ocrMatchLine != null -> "OCR: ${ocrMatchLine.take(64)}"
          barcodeMatch != null -> "Code: ${barcodeMatch.take(64)}"
          tagMatch != null && !nameMatch -> "Tag: $tagMatch"
          tagFilter != null -> buildMatchSnippetForTag(entry, tagFilter)
          else -> ""
        }
        if (snippet.isNotEmpty()) {
          snippets[photo.path] = snippet
        }
      }
    }

    return GallerySearchResult(
      matchingAlbums = matchingAlbums,
      matchingPhotos = sortPhotos(matchedPhotos, sortOrder),
      matchSnippetsByPath = snippets,
    )
  }

  private fun buildMatchSnippetForTag(
    entry: GalleryPhotoIndexEntry?,
    tag: String,
  ): String {
    if (entry == null) return ""
    return when {
      (tag.equals("QR Code", ignoreCase = true) || tag.equals("Barcode", ignoreCase = true) || tag.equals("Link", ignoreCase = true)) &&
        entry.barcodePayloads.isNotEmpty() -> {
        "Code: ${entry.barcodePayloads.first().take(60)}"
      }
      entry.ocrText.isNotBlank() -> {
        val firstLine = entry.ocrText.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() }.orEmpty()
        if (firstLine.isNotEmpty()) "OCR: ${firstLine.take(60)}" else "Tag: $tag"
      }
      else -> "Tag: $tag"
    }
  }
}
