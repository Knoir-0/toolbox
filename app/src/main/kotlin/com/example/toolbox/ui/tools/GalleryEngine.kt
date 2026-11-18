package com.example.toolbox.ui.tools

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URLDecoder
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.Inflater
import kotlin.coroutines.resume
import kotlin.math.roundToInt
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
const val PREF_KEY_GALLERY_GROUP_OPTION = "gallery_group_option"
const val PREF_KEY_GALLERY_DEFAULT_SORT_GROUP = "gallery_default_sort_group"
const val PREF_PREFIX_GALLERY_FOLDER_SORT_GROUP = "gallery_folder_sort_group_"
const val PREF_KEY_GRANTED_GALLERY_FOLDERS = "granted_gallery_folders"
const val PREF_KEY_GRANTED_GALLERY_FOLDER_PATHS = "granted_gallery_folder_paths"
const val PREF_KEY_GALLERY_FOLDER_PILLS = "gallery_folder_pills"
const val GALLERY_ROOT_FOLDER_KEY = "__gallery_root__"
const val GALLERY_ALBUMS_PILL_NAME = "Albums"

const val DEFAULT_DCIM_FOLDER_NAME = "DCIM"
const val DEFAULT_DCIM_PILL_LABEL = "Camera"
const val DEFAULT_PICTURES_FOLDER_NAME = "Pictures"
const val DEFAULT_GALLERY_DOWNLOADS_FOLDER_NAME = "Download"

const val DEFAULT_DCIM_FOLDER_DISPLAY = "/DCIM"
const val DEFAULT_PICTURES_FOLDER_DISPLAY = "/Pictures"
const val DEFAULT_GALLERY_DOWNLOADS_FOLDER_DISPLAY = "/Download"

const val MIN_ALBUMS_PER_ROW = 3
const val MAX_ALBUMS_PER_ROW = 6
const val DEFAULT_ALBUMS_PER_ROW = 3

const val MIN_PHOTO_ZOOM = 1f
const val MAX_PHOTO_ZOOM = 5f

const val DEFAULT_VIDEO_PREVIEW_PLAY_DURATION_MS = 4_500L
const val DEFAULT_VIDEO_PREVIEW_CYCLE_PAUSE_DURATION_MS = 7_000L
const val DEFAULT_VIDEO_PREVIEW_SCROLL_OUT_COOLDOWN_MS = 2_500L

/**
 * Playback phases for inline muted video previews while browsing/scrolling the Gallery:
 * - [IDLE]: No videos currently in view or inline preview disabled.
 * - [PLAYING]: Playing a muted preview of the current active video in view (for 3–6s).
 * - [CYCLE_PAUSE]: All videos currently in view have finished their preview; pausing (for 6–8s)
 *   before starting again with the first video in view.
 * - [SCROLL_COOLDOWN]: The video that was playing was scrolled out of view; pausing (for 2–3s
 *   after the user stops scrolling) before starting again with the first video currently in view.
 */
enum class GalleryVideoPreviewPhase {
  IDLE,
  PLAYING,
  CYCLE_PAUSE,
  SCROLL_COOLDOWN,
}

/**
 * Immutable state snapshot for the inline muted video preview scheduler in the Gallery.
 */
data class GalleryVideoPreviewState(
  val phase: GalleryVideoPreviewPhase = GalleryVideoPreviewPhase.IDLE,
  val activeVideoPath: String? = null,
  val playedInCurrentCycle: Set<String> = emptySet(),
  val playSessionToken: Int = 0,
)

val DEFAULT_GALLERY_FOLDER_NAMES: List<String> = listOf(
  DEFAULT_DCIM_FOLDER_NAME,
  DEFAULT_PICTURES_FOLDER_NAME,
  DEFAULT_GALLERY_DOWNLOADS_FOLDER_NAME,
)

val DEFAULT_GALLERY_FOLDER_DISPLAY_PATHS: List<String> = listOf(
  DEFAULT_DCIM_FOLDER_DISPLAY,
  DEFAULT_PICTURES_FOLDER_DISPLAY,
  DEFAULT_GALLERY_DOWNLOADS_FOLDER_DISPLAY,
)

const val DEFAULT_NEW_ALBUM_FOLDER_NAME: String = "Pictures"

/**
 * Default folders accessed by the Gallery tool (`DCIM`, `Pictures`, `Download`).
 */
enum class GalleryDefaultFolder(
  val folderName: String,
  val displayPath: String,
  val pillLabel: String = folderName,
) {
  DCIM(
    folderName = DEFAULT_DCIM_FOLDER_NAME,
    displayPath = DEFAULT_DCIM_FOLDER_DISPLAY,
    pillLabel = DEFAULT_DCIM_PILL_LABEL,
  ),
  PICTURES(
    folderName = DEFAULT_PICTURES_FOLDER_NAME,
    displayPath = DEFAULT_PICTURES_FOLDER_DISPLAY,
  ),
  DOWNLOADS(
    folderName = DEFAULT_GALLERY_DOWNLOADS_FOLDER_NAME,
    displayPath = DEFAULT_GALLERY_DOWNLOADS_FOLDER_DISPLAY,
  ),
  ;

  companion object {
    val DEFAULT: GalleryDefaultFolder = DCIM

    fun fromPathOrName(pathOrName: String?): GalleryDefaultFolder? {
      if (pathOrName.isNullOrBlank()) return null
      val decoded = try {
        URLDecoder.decode(pathOrName, "UTF-8")
      } catch (_: Throwable) {
        pathOrName
      }
      val cleaned = decoded
        .trim()
        .trim('/')
        .substringAfterLast('/')
        .substringAfterLast(':')
        .trim()
      return when {
        cleaned.equals(DEFAULT_DCIM_FOLDER_NAME, ignoreCase = true) ||
          cleaned.equals(DCIM.name, ignoreCase = true) -> DCIM
        cleaned.equals(DEFAULT_PICTURES_FOLDER_NAME, ignoreCase = true) ||
          cleaned.equals(PICTURES.name, ignoreCase = true) -> PICTURES
        cleaned.equals(DEFAULT_GALLERY_DOWNLOADS_FOLDER_NAME, ignoreCase = true) ||
          cleaned.equals("Downloads", ignoreCase = true) ||
          cleaned.equals(DOWNLOADS.name, ignoreCase = true) -> DOWNLOADS
        else -> null
      }
    }
  }

  fun toGalleryFolder(): GalleryFolder = GalleryFolder(
    folderName = folderName,
    displayPath = displayPath,
  )
}

/**
 * Represents a folder navigation pill in the Gallery tool (either one of the default folders
 * `DCIM`, `Pictures`, `Download` or a user-added custom folder via Android's scoped storage access).
 */
data class GalleryFolder(
  val folderName: String,
  val displayPath: String,
  val treeUri: String? = null,
  val customAbsolutePath: String? = null,
) {
  val defaultFolder: GalleryDefaultFolder?
    get() = if (customAbsolutePath == null && displayPath.count { it == '/' } <= 1) {
      GalleryDefaultFolder.fromPathOrName(displayPath)
    } else {
      null
    }

  val isDefault: Boolean
    get() = defaultFolder != null

  val pillLabel: String
    get() = defaultFolder?.pillLabel ?: folderName
}

val DEFAULT_GALLERY_FOLDERS: List<GalleryFolder> =
  GalleryDefaultFolder.entries.map { it.toGalleryFolder() }

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
 * Grouping options for organizing media in the Gallery tool:
 * - [FOLDERS]: Groups albums and photos by the default root folders (`DCIM` and `Pictures`).
 * - [DATE]: Groups photos & videos chronologically (Today, Yesterday, This Week, Month & Year).
 * - [MEDIA_TYPE]: Groups items into Photos, Videos, GIFs, and Screenshots.
 * - [SMART_TAGS]: Groups items by detected smart tags (Receipts, Documents, QR Codes, visual labels, Untagged).
 * - [NONE]: Un-nested view (default) combining `Pictures` and `DCIM` without group headers.
 */
enum class GalleryGroupOption(val displayName: String) {
  FOLDERS("Folders"),
  DATE("Date"),
  MEDIA_TYPE("Media Type"),
  SMART_TAGS("Smart Tags"),
  NONE("None"),
  ;

  companion object {
    val DEFAULT: GalleryGroupOption = NONE

    fun fromNameOrDefault(name: String?): GalleryGroupOption {
      if (name.isNullOrBlank()) return DEFAULT
      return entries.find { it.name.equals(name.trim(), ignoreCase = true) } ?: DEFAULT
    }
  }
}

/**
 * Holds a combined group-by and sort-by configuration for a gallery folder/album or default preference.
 */
data class GallerySortGroupConfig(
  val groupOption: GalleryGroupOption = GalleryGroupOption.DEFAULT,
  val sortOrder: GallerySortOrder = GallerySortOrder.DEFAULT,
)

/**
 * Represents a grouped section of [GalleryPhoto] items under a common [title] header.
 */
data class GalleryPhotoGroup(
  val title: String,
  val photos: List<GalleryPhoto>,
)

/**
 * Represents a root-folder group (such as `"DCIM"` or `"Pictures"`) containing that root folder's
 * [albums] and direct [photos].
 */
data class GalleryFolderGroup(
  val title: String,
  val albums: List<GalleryAlbum>,
  val photos: List<GalleryPhoto>,
) {
  val totalCount: Int get() = albums.size + photos.size
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
  val isAnimatedImage: Boolean get() = GalleryEngine.isAnimatedImageFile(file)
  val supportsInlinePreview: Boolean get() = isVideo || isAnimatedImage
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
   * Updates [state] when the ordered list of [visibleVideoPaths] in the Gallery viewport changes
   * or when inline previews are [enabled]/disabled:
   * - If disabled, resets to [GalleryVideoPreviewPhase.IDLE].
   * - If [GalleryVideoPreviewPhase.PLAYING] and the currently playing video is scrolled out of view
   *   (`activeVideoPath !in visibleVideoPaths`), immediately pauses playback and transitions to
   *   [GalleryVideoPreviewPhase.SCROLL_COOLDOWN].
   * - If [GalleryVideoPreviewPhase.IDLE] and at least one video is in view, begins playing the
   *   first video in view.
   */
  fun onVisibleVideosChanged(
    state: GalleryVideoPreviewState,
    visibleVideoPaths: List<String>,
    enabled: Boolean = true,
  ): GalleryVideoPreviewState {
    if (!enabled) {
      return GalleryVideoPreviewState()
    }

    val playingPath = state.activeVideoPath
    if (state.phase == GalleryVideoPreviewPhase.PLAYING && playingPath != null) {
      if (playingPath !in visibleVideoPaths) {
        return state.copy(
          phase = GalleryVideoPreviewPhase.SCROLL_COOLDOWN,
          activeVideoPath = null,
          playedInCurrentCycle = emptySet(),
        )
      }
      return state
    }

    if (visibleVideoPaths.isEmpty()) {
      return if (state.phase == GalleryVideoPreviewPhase.SCROLL_COOLDOWN) {
        state.copy(
          activeVideoPath = null,
          playedInCurrentCycle = emptySet(),
        )
      } else {
        GalleryVideoPreviewState()
      }
    }

    if (state.phase == GalleryVideoPreviewPhase.IDLE) {
      val first = visibleVideoPaths.first()
      return state.copy(
        phase = GalleryVideoPreviewPhase.PLAYING,
        activeVideoPath = first,
        playedInCurrentCycle = emptySet(),
        playSessionToken = state.playSessionToken + 1,
      )
    }

    return state
  }

  /**
   * Advances [state] when the currently playing video finishes its preview slice (3–6s):
   * - Marks the video as played in [GalleryVideoPreviewState.playedInCurrentCycle].
   * - Moves on to the next unplayed video in [visibleVideoPaths].
   * - If all videos in [visibleVideoPaths] have been played, pauses and transitions to
   *   [GalleryVideoPreviewPhase.CYCLE_PAUSE] (6–8s).
   */
  fun onActiveVideoPreviewCompleted(
    state: GalleryVideoPreviewState,
    visibleVideoPaths: List<String>,
  ): GalleryVideoPreviewState {
    val playingPath = state.activeVideoPath ?: return state
    if (state.phase != GalleryVideoPreviewPhase.PLAYING) return state
    if (playingPath !in visibleVideoPaths) {
      return state.copy(
        phase = GalleryVideoPreviewPhase.SCROLL_COOLDOWN,
        activeVideoPath = null,
        playedInCurrentCycle = emptySet(),
      )
    }

    val updatedPlayed = state.playedInCurrentCycle + playingPath
    val nextVideo = visibleVideoPaths.firstOrNull { it !in updatedPlayed }
    return if (nextVideo != null) {
      state.copy(
        phase = GalleryVideoPreviewPhase.PLAYING,
        activeVideoPath = nextVideo,
        playedInCurrentCycle = updatedPlayed,
        playSessionToken = state.playSessionToken + 1,
      )
    } else {
      state.copy(
        phase = GalleryVideoPreviewPhase.CYCLE_PAUSE,
        activeVideoPath = null,
        playedInCurrentCycle = updatedPlayed,
      )
    }
  }

  /**
   * Resumes preview playback from the first video currently in [visibleVideoPaths] after either
   * [GalleryVideoPreviewPhase.CYCLE_PAUSE] (6–8s) or [GalleryVideoPreviewPhase.SCROLL_COOLDOWN]
   * (2–3s after scrolling stops) completes.
   */
  fun onPreviewPauseOrCooldownCompleted(
    state: GalleryVideoPreviewState,
    visibleVideoPaths: List<String>,
  ): GalleryVideoPreviewState {
    val first = visibleVideoPaths.firstOrNull()
    return if (first != null) {
      state.copy(
        phase = GalleryVideoPreviewPhase.PLAYING,
        activeVideoPath = first,
        playedInCurrentCycle = emptySet(),
        playSessionToken = state.playSessionToken + 1,
      )
    } else {
      GalleryVideoPreviewState()
    }
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
   * Checks whether [file] is an animated image file (such as a `.gif` or an animated `.webp`).
   */
  fun isAnimatedImageFile(file: File): Boolean {
    if (!file.exists() || !file.isFile) return false
    if (file.name.startsWith(".")) return false
    val ext = file.extension.lowercase(Locale.ROOT)
    if (ext == "gif") return true
    if (ext == "webp") {
      return try {
        val header = ByteArray(64)
        val readCount = file.inputStream().use { it.read(header) }
        if (readCount >= 21 &&
          header[0] == 'R'.code.toByte() &&
          header[1] == 'I'.code.toByte() &&
          header[2] == 'F'.code.toByte() &&
          header[3] == 'F'.code.toByte() &&
          header[8] == 'W'.code.toByte() &&
          header[9] == 'E'.code.toByte() &&
          header[10] == 'B'.code.toByte() &&
          header[11] == 'P'.code.toByte()
        ) {
          val isVp8x = header[12] == 'V'.code.toByte() &&
            header[13] == 'P'.code.toByte() &&
            header[14] == '8'.code.toByte() &&
            header[15] == 'X'.code.toByte()
          val hasAnimFlag = isVp8x && ((header[20].toInt() and 0x02) != 0)
          val headerStr = String(header, 0, readCount, Charsets.US_ASCII)
          hasAnimFlag || headerStr.contains("ANIM")
        } else {
          false
        }
      } catch (_: Throwable) {
        false
      }
    }
    return false
  }

  /**
   * Checks whether [file] supports inline auto-playing previews while browsing the Gallery
   * (either a video file or an animated image file).
   */
  fun isInlinePreviewMediaFile(file: File): Boolean {
    return isVideoFile(file) || isAnimatedImageFile(file)
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
   * Resolves the list of folders loaded into the gallery, starting with the default `DCIM`, `Pictures`,
   * and `Download` folders and appending any user-added folder paths.
   */
  fun resolveGalleryFolders(
    rootDirectory: File = FileManagerEngine.getUserRootDirectory(),
    additionalPaths: List<String> = emptyList(),
  ): List<File> {
    val defaults = getDefaultGalleryFolders(rootDirectory).toMutableList()
    // Also include plural "Downloads" folder when present on storage
    val downloadsDir = File(rootDirectory, "Downloads")
    if (downloadsDir.exists() && downloadsDir.isDirectory) {
      defaults.add(downloadsDir)
    }
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
   * Resolves a user-friendly display path for [directory] (e.g. `/DCIM` when inside [userRoot]).
   */
  fun formatDirectoryDisplayPath(
    directory: File,
    userRoot: File = FileManagerEngine.getUserRootDirectory(),
  ): String {
    val absPath = directory.absolutePath
    val rootPath = userRoot.absolutePath.trimEnd('/')
    if (absPath == rootPath) return "/"
    if (absPath.startsWith("$rootPath/")) {
      return absPath.removePrefix(rootPath)
    }
    GalleryDefaultFolder.fromPathOrName(directory.name)?.let { defaultFolder ->
      return defaultFolder.displayPath
    }
    return absPath
  }

  /**
   * Resolves the concrete [File] directory on disk for a [GalleryFolder] pill.
   */
  fun resolveDirectoryForFolder(
    folder: GalleryFolder,
    rootDirectory: File = FileManagerEngine.getUserRootDirectory(),
  ): File {
    if (!folder.customAbsolutePath.isNullOrBlank()) {
      return File(folder.customAbsolutePath)
    }
    val defaultFolder = folder.defaultFolder
    if (defaultFolder != null) {
      return File(rootDirectory, defaultFolder.folderName)
    }
    val subPath = folder.displayPath.trim().trimStart('/')
    return if (subPath.isEmpty()) rootDirectory else File(rootDirectory, subPath)
  }

  /**
   * Parses a comma-separated string of folder names or enum names into a set of [GalleryDefaultFolder]s
   * that have been granted storage access. Returns an empty set when [rawString] is null or blank.
   */
  fun parseGrantedFolders(rawString: String?): Set<GalleryDefaultFolder> {
    if (rawString.isNullOrBlank()) return emptySet()
    return rawString
      .split(",")
      .mapNotNull { token -> GalleryDefaultFolder.fromPathOrName(token) }
      .toSet()
  }

  /**
   * Serializes a set of granted [GalleryDefaultFolder]s into a comma-separated string.
   */
  fun serializeGrantedFolders(folders: Set<GalleryDefaultFolder>): String {
    return GalleryDefaultFolder.entries
      .filter { it in folders }
      .joinToString(",") { it.name }
  }

  /**
   * Returns the set of [GalleryDefaultFolder]s that have been granted scoped storage access.
   */
  fun getGrantedFolders(context: Context): Set<GalleryDefaultFolder> {
    val prefs = context.getSharedPreferences(GALLERY_PREFS_NAME, Context.MODE_PRIVATE)
    return parseGrantedFolders(prefs.getString(PREF_KEY_GRANTED_GALLERY_FOLDERS, null))
  }

  /**
   * Persists the set of [GalleryDefaultFolder]s that have been granted scoped storage access.
   */
  fun setGrantedFolders(context: Context, folders: Set<GalleryDefaultFolder>) {
    val prefs = context.getSharedPreferences(GALLERY_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(PREF_KEY_GRANTED_GALLERY_FOLDERS, serializeGrantedFolders(folders)).apply()
  }

  /**
   * Returns whether [folder] has been granted scoped storage access.
   */
  fun isFolderAccessGranted(context: Context, folder: GalleryDefaultFolder): Boolean {
    return folder in getGrantedFolders(context)
  }

  /**
   * Updates the granted state of [folder] in preferences and returns the updated set of granted folders.
   */
  fun setFolderAccessGranted(
    context: Context,
    folder: GalleryDefaultFolder,
    granted: Boolean = true,
  ): Set<GalleryDefaultFolder> {
    val current = getGrantedFolders(context).toMutableSet()
    if (granted) {
      current.add(folder)
    } else {
      current.remove(folder)
    }
    val updated = GalleryDefaultFolder.entries.filter { it in current }.toSet()
    setGrantedFolders(context, updated)
    return updated
  }

  /**
   * Builds the external storage document ID for [folder] (e.g. `primary:DCIM`, `primary:Pictures`, `primary:Download`).
   */
  fun buildFolderDocumentId(folder: GalleryDefaultFolder): String {
    return "primary:${folder.folderName}"
  }

  /**
   * Builds the initial [Uri] targeting [folder] for `Intent.ACTION_OPEN_DOCUMENT_TREE`.
   */
  fun buildInitialDocumentUriForFolder(folder: GalleryDefaultFolder): Uri {
    val docId = buildFolderDocumentId(folder)
    return try {
      DocumentsContract.buildDocumentUri(EXTERNAL_STORAGE_DOCUMENTS_AUTHORITY, docId)
    } catch (_: Throwable) {
      Uri.parse("content://$EXTERNAL_STORAGE_DOCUMENTS_AUTHORITY/document/primary%3A${folder.folderName}")
    }
  }

  /**
   * Resolves a [GalleryDefaultFolder] from a SAF tree or document [Uri] if it matches one of the default folders.
   */
  fun resolveFolderFromTreeUri(uri: Uri?): GalleryDefaultFolder? {
    if (uri == null) return null
    val docId = try {
      DocumentsContract.getTreeDocumentId(uri)
    } catch (_: Throwable) {
      null
    }
    if (!docId.isNullOrBlank()) {
      GalleryDefaultFolder.fromPathOrName(docId)?.let { return it }
    }
    return GalleryDefaultFolder.fromPathOrName(uri.toString())
  }

  /**
   * Creates the Android system prompt [Intent] (`Intent.ACTION_OPEN_DOCUMENT_TREE`) scoped to [folder]
   * so the user can grant access to files in that location.
   */
  fun createFolderAccessIntent(
    context: Context? = null,
    folder: GalleryDefaultFolder,
    userRoot: File = FileManagerEngine.getUserRootDirectory(),
  ): Intent {
    try {
      val targetDir = File(userRoot, folder.folderName)
      if (!targetDir.exists()) {
        targetDir.mkdirs()
      }
    } catch (_: Throwable) {}

    val initialUri = buildInitialDocumentUriForFolder(folder)
    val baseIntent = if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      try {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
        storageManager?.primaryStorageVolume?.createOpenDocumentTreeIntent()
      } catch (_: Throwable) {
        null
      }
    } else {
      null
    } ?: Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)

    return baseIntent.apply {
      action = Intent.ACTION_OPEN_DOCUMENT_TREE
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
      }
      addFlags(
        Intent.FLAG_GRANT_READ_URI_PERMISSION or
          Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
          Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
          Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
      )
    }
  }

  /**
   * Creates the Android system prompt [Intent] (`Intent.ACTION_OPEN_DOCUMENT_TREE`) scoped to a
   * [GalleryFolder] (default or custom) so the user can grant access to files in that location.
   */
  fun createFolderAccessIntent(
    context: Context? = null,
    folder: GalleryFolder,
    userRoot: File = FileManagerEngine.getUserRootDirectory(),
  ): Intent {
    folder.defaultFolder?.let { defaultFolder ->
      return createFolderAccessIntent(context = context, folder = defaultFolder, userRoot = userRoot)
    }
    val relativePath = folder.displayPath.trim().trim('/')
    try {
      val targetDir = if (!folder.customAbsolutePath.isNullOrBlank()) {
        File(folder.customAbsolutePath)
      } else {
        File(userRoot, relativePath)
      }
      if (!targetDir.exists()) {
        targetDir.mkdirs()
      }
    } catch (_: Throwable) {}

    val docId = "primary:$relativePath"
    val initialUri = try {
      DocumentsContract.buildDocumentUri(EXTERNAL_STORAGE_DOCUMENTS_AUTHORITY, docId)
    } catch (_: Throwable) {
      Uri.parse("content://$EXTERNAL_STORAGE_DOCUMENTS_AUTHORITY/document/primary%3A$relativePath")
    }
    val baseIntent = if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      try {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
        storageManager?.primaryStorageVolume?.createOpenDocumentTreeIntent()
      } catch (_: Throwable) {
        null
      }
    } else {
      null
    } ?: Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)

    return baseIntent.apply {
      action = Intent.ACTION_OPEN_DOCUMENT_TREE
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
      }
      addFlags(
        Intent.FLAG_GRANT_READ_URI_PERMISSION or
          Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
          Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
          Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
      )
    }
  }

  /**
   * Builds the Android system prompt [Intent] (`Intent.ACTION_OPEN_DOCUMENT_TREE`) for adding a
   * custom storage location via Android's scoped storage access.
   */
  fun createAddCustomFolderIntent(
    context: Context? = null,
    userRoot: File = FileManagerEngine.getUserRootDirectory(),
  ): Intent {
    return AudioRecorderEngine.createAddCustomFolderIntent(context = context, userRoot = userRoot)
  }

  /**
   * Resolves a custom or default [GalleryFolder] from a SAF tree/document [Uri] or file path.
   */
  fun resolveCustomFolderFromTreeUri(
    uri: Uri?,
    userRoot: File = FileManagerEngine.getUserRootDirectory(),
  ): GalleryFolder? {
    if (uri == null) return null
    if (uri.scheme.equals("file", ignoreCase = true)) {
      val path = uri.path ?: return null
      val file = File(path)
      val displayPath = formatDirectoryDisplayPath(file, userRoot)
      val name = file.name.ifBlank { displayPath.trim('/').substringAfterLast('/').ifBlank { "Custom" } }
      return GalleryFolder(
        folderName = name,
        displayPath = if (displayPath.startsWith("/")) displayPath else "/$displayPath",
        treeUri = uri.toString(),
        customAbsolutePath = file.absolutePath,
      )
    }

    val rawDocId = try {
      DocumentsContract.getTreeDocumentId(uri)
    } catch (_: Throwable) {
      null
    } ?: try {
      DocumentsContract.getDocumentId(uri)
    } catch (_: Throwable) {
      null
    } ?: run {
      val decoded = try {
        URLDecoder.decode(uri.toString(), "UTF-8")
      } catch (_: Throwable) {
        uri.toString()
      }
      decoded.substringAfterLast("/tree/", "").ifEmpty {
        decoded.substringAfterLast("/document/", "")
      }.ifEmpty {
        decoded
      }
    }

    val cleanedSubpath = rawDocId
      .substringAfter(':', rawDocId)
      .trim()
      .trim('/')
    if (cleanedSubpath.isEmpty()) {
      return GalleryDefaultFolder.DEFAULT.toGalleryFolder().copy(treeUri = uri.toString())
    }
    if (!cleanedSubpath.contains('/')) {
      GalleryDefaultFolder.fromPathOrName(cleanedSubpath)?.let { defaultFolder ->
        return defaultFolder.toGalleryFolder().copy(treeUri = uri.toString())
      }
    }
    val folderName = cleanedSubpath.substringAfterLast('/').ifBlank { cleanedSubpath }
    val displayPath = "/$cleanedSubpath"
    val resolvedFile = File(userRoot, cleanedSubpath)
    return GalleryFolder(
      folderName = folderName,
      displayPath = displayPath,
      treeUri = uri.toString(),
      customAbsolutePath = resolvedFile.absolutePath,
    )
  }

  /**
   * Moves the folder pill at [fromIndex] to [toIndex] in [folders], returning the new order.
   */
  fun reorderFolders(
    folders: List<GalleryFolder>,
    fromIndex: Int,
    toIndex: Int,
  ): List<GalleryFolder> {
    val base = if (folders.isEmpty()) DEFAULT_GALLERY_FOLDERS else folders
    if (fromIndex !in base.indices || toIndex !in base.indices || fromIndex == toIndex) {
      return base
    }
    val mutable = base.toMutableList()
    val moved = mutable.removeAt(fromIndex)
    mutable.add(toIndex, moved)
    return mutable.toList()
  }

  /**
   * Reorders the full Gallery navigation pill list (where the `"Albums"` pill is always fixed at index `0`
   * at the front of the list). Attempting to move index `0` returns the list unchanged, and moving any
   * folder pill to index `<= 0` places it at index `1` right after `"Albums"`.
   */
  fun reorderNavigationPills(
    pills: List<String>,
    fromIndex: Int,
    toIndex: Int,
  ): List<String> {
    val folderNames = pills.filterNot { it.equals(GALLERY_ALBUMS_PILL_NAME, ignoreCase = true) }
    val normalized = listOf(GALLERY_ALBUMS_PILL_NAME) +
      (if (folderNames.isEmpty()) DEFAULT_GALLERY_FOLDER_NAMES else folderNames)
    if (fromIndex <= 0 || fromIndex !in normalized.indices) {
      return normalized
    }
    val clampedTarget = toIndex.coerceIn(1, normalized.lastIndex)
    if (fromIndex == clampedTarget) return normalized
    val mutable = normalized.toMutableList()
    val moved = mutable.removeAt(fromIndex)
    mutable.add(clampedTarget, moved)
    return mutable.toList()
  }

  /**
   * Serializes an ordered list of [GalleryFolder] pills into a persistent string.
   */
  fun serializeFolderPills(folders: List<GalleryFolder>): String {
    val seenPaths = mutableSetOf<String>()
    val deduplicated = mutableListOf<GalleryFolder>()
    for (folder in folders) {
      val normKey = folder.displayPath.trim().lowercase(Locale.ROOT)
      if (normKey.isNotEmpty() && seenPaths.add(normKey)) {
        deduplicated.add(folder)
      }
    }
    for (defaultFolder in DEFAULT_GALLERY_FOLDERS) {
      val normKey = defaultFolder.displayPath.lowercase(Locale.ROOT)
      if (seenPaths.add(normKey)) {
        deduplicated.add(defaultFolder)
      }
    }
    return deduplicated.joinToString("\n") { folder ->
      listOf(
        folder.folderName.replace("\t", " ").replace("\n", " "),
        folder.displayPath.replace("\t", " ").replace("\n", " "),
        (folder.treeUri ?: "").replace("\t", " ").replace("\n", " "),
        (folder.customAbsolutePath ?: "").replace("\t", " ").replace("\n", " "),
      ).joinToString("\t")
    }
  }

  /**
   * Parses a serialized folder pills string into an ordered list of [GalleryFolder]s,
   * preserving the saved order and ensuring all default folders (`DCIM`, `Pictures`, `Download`) are included.
   */
  fun parseFolderPills(rawString: String?): List<GalleryFolder> {
    if (rawString.isNullOrBlank()) return DEFAULT_GALLERY_FOLDERS
    val seenPaths = mutableSetOf<String>()
    val parsed = mutableListOf<GalleryFolder>()
    val lines = if (rawString.contains("\n") || rawString.contains("\t")) {
      rawString.lines()
    } else {
      rawString.split("|", ",")
    }
    for (line in lines) {
      val trimmed = line.trim()
      if (trimmed.isEmpty() || trimmed.equals(GALLERY_ALBUMS_PILL_NAME, ignoreCase = true)) continue
      val folder = if (trimmed.contains("\t")) {
        val parts = trimmed.split("\t")
        val name = parts.getOrNull(0)?.trim().orEmpty()
        val display = parts.getOrNull(1)?.trim().orEmpty()
        val uri = parts.getOrNull(2)?.trim()?.takeIf { it.isNotEmpty() }
        val absPath = parts.getOrNull(3)?.trim()?.takeIf { it.isNotEmpty() }
        if (name.isNotEmpty() && display.isNotEmpty()) {
          GalleryFolder(
            folderName = name,
            displayPath = if (display.startsWith("/")) display else "/$display",
            treeUri = uri,
            customAbsolutePath = absPath,
          )
        } else {
          null
        }
      } else {
        GalleryDefaultFolder.fromPathOrName(trimmed)?.toGalleryFolder() ?: run {
          val cleanPath = "/" + trimmed.trim('/')
          val name = cleanPath.substringAfterLast('/')
          if (name.isNotEmpty()) GalleryFolder(folderName = name, displayPath = cleanPath) else null
        }
      }
      if (folder != null) {
        val normKey = folder.displayPath.lowercase(Locale.ROOT)
        if (seenPaths.add(normKey)) {
          parsed.add(folder)
        }
      }
    }
    for (defaultFolder in DEFAULT_GALLERY_FOLDERS) {
      val normKey = defaultFolder.displayPath.lowercase(Locale.ROOT)
      if (seenPaths.add(normKey)) {
        parsed.add(defaultFolder)
      }
    }
    return if (parsed.isEmpty()) DEFAULT_GALLERY_FOLDERS else parsed
  }

  fun getFolderPills(context: Context): List<GalleryFolder> {
    val prefs = context.getSharedPreferences(GALLERY_PREFS_NAME, Context.MODE_PRIVATE)
    return parseFolderPills(prefs.getString(PREF_KEY_GALLERY_FOLDER_PILLS, null))
  }

  fun setFolderPills(context: Context, folders: List<GalleryFolder>) {
    val prefs = context.getSharedPreferences(GALLERY_PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(PREF_KEY_GALLERY_FOLDER_PILLS, serializeFolderPills(folders)).apply()
  }

  /**
   * Parses a serialized string of granted folder display paths (and/or default folder names) into
   * a canonical set of display paths (e.g. `"/DCIM"`, `"/Pictures"`, `"/Download"`, `"/Vacation"`).
   */
  fun parseGrantedFolderPaths(rawString: String?): Set<String> {
    if (rawString.isNullOrBlank()) return emptySet()
    return rawString
      .split(",", "|", "\n")
      .mapNotNull { token ->
        val trimmed = token.trim()
        if (trimmed.isEmpty()) {
          null
        } else {
          val defaultMatch = if (trimmed.count { it == '/' } <= 1) {
            GalleryDefaultFolder.fromPathOrName(trimmed)
          } else {
            null
          }
          defaultMatch?.displayPath ?: ("/" + trimmed.trim('/'))
        }
      }
      .toSet()
  }

  fun serializeGrantedFolderPaths(paths: Set<String>): String {
    return paths
      .map { path ->
        val trimmed = path.trim()
        val defaultMatch = if (trimmed.count { it == '/' } <= 1) {
          GalleryDefaultFolder.fromPathOrName(trimmed)
        } else {
          null
        }
        defaultMatch?.displayPath ?: ("/" + trimmed.trim('/'))
      }
      .distinct()
      .joinToString("|")
  }

  fun getGrantedFolderPaths(context: Context): Set<String> {
    val prefs = context.getSharedPreferences(GALLERY_PREFS_NAME, Context.MODE_PRIVATE)
    val fromPaths = parseGrantedFolderPaths(prefs.getString(PREF_KEY_GRANTED_GALLERY_FOLDER_PATHS, null))
    val fromDefaults = getGrantedFolders(context).map { it.displayPath }.toSet()
    return fromPaths + fromDefaults
  }

  fun setGrantedFolderPaths(context: Context, paths: Set<String>) {
    val prefs = context.getSharedPreferences(GALLERY_PREFS_NAME, Context.MODE_PRIVATE)
    val defaultSet = paths.mapNotNull { path ->
      if (path.count { it == '/' } <= 1) GalleryDefaultFolder.fromPathOrName(path) else null
    }.toSet()
    prefs.edit()
      .putString(PREF_KEY_GRANTED_GALLERY_FOLDER_PATHS, serializeGrantedFolderPaths(paths))
      .putString(PREF_KEY_GRANTED_GALLERY_FOLDERS, serializeGrantedFolders(defaultSet))
      .apply()
  }

  fun setFolderPathAccessGranted(
    context: Context,
    displayPath: String,
    granted: Boolean = true,
  ): Set<String> {
    val normalized = GalleryDefaultFolder.fromPathOrName(displayPath)
      ?.takeIf { displayPath.count { ch -> ch == '/' } <= 1 }
      ?.displayPath
      ?: ("/" + displayPath.trim().trim('/'))
    val current = getGrantedFolderPaths(context).toMutableSet()
    if (granted) {
      current.add(normalized)
    } else {
      current.removeAll { it.equals(normalized, ignoreCase = true) }
    }
    setGrantedFolderPaths(context, current)
    return current.toSet()
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
   * Checks whether [directory] is the `Downloads` (or Android `Download`) folder, which is treated
   * as a single album in the gallery rather than having its contents un-nested into the root view.
   */
  fun isDownloadsDirectory(directory: File): Boolean {
    val name = directory.name
    return name.equals("Downloads", ignoreCase = true) || name.equals("Download", ignoreCase = true)
  }

  /**
   * Collects all supported photo/video files recursively inside [directories] up to [maxDepth],
   * skipping dot-prefixed entries and `.nomedia` directories.
   */
  private fun collectMediaFilesInDirectories(
    directories: List<File>,
    maxDepth: Int = 8,
  ): List<File> {
    val result = LinkedHashMap<String, File>()

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
          result.putIfAbsent(child.absolutePath, child)
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
    return result.values.toList()
  }

  /**
   * Builds a single unified `"Download"` [GalleryAlbum] across [directories], flattening all media
   * inside `Download` into a single album (`subAlbumCount = 0`).
   */
  fun buildDownloadsAlbum(
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
    val primaryDir = distinctDirs.firstOrNull() ?: File("Download")
    val mediaFiles = collectMediaFilesInDirectories(distinctDirs, maxDepth = maxDepth)
    val totalPhotos = mediaFiles.size
    val mostRecent = if (deferAlbumArt) {
      null
    } else {
      mediaFiles.maxWithOrNull(
        compareBy<File> { it.lastModified() }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      )
    }
    val lastMod = mostRecent?.lastModified()
      ?: distinctDirs.maxOfOrNull { if (it.exists()) it.lastModified() else 0L }
      ?: 0L

    return GalleryAlbum(
      directory = primaryDir,
      name = "Download",
      path = primaryDir.absolutePath,
      mostRecentPhoto = mostRecent,
      directPhotoCount = totalPhotos,
      totalPhotoCount = totalPhotos,
      subAlbumCount = 0,
      lastModifiedMs = lastMod,
      directories = distinctDirs,
    )
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
   * subfolders in `DCIM`, `Pictures`, and `Photos` are merged as if they were in the same folder).
   * If all [directories] are `Downloads` folders, builds a single flat `"Downloads"` album.
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
    if (distinctDirs.isNotEmpty() && distinctDirs.all { isDownloadsDirectory(it) }) {
      return buildDownloadsAlbum(
        directories = distinctDirs,
        maxDepth = maxDepth,
        deferAlbumArt = deferAlbumArt,
      )
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
   * Loads the root contents of the Gallery from the default folders (`DCIM`, `Pictures`, `Photos`,
   * and `Downloads`, plus any [additionalPaths]):
   * - `DCIM`, `Pictures`, and `Photos` (and non-Downloads folders) have their subdirectories un-nested
   *   as top-level albums (merging same-named subfolders) and their direct photo files combined at root.
   * - `Downloads` (including Android's `Download` folder) is treated as a single `"Downloads"` album
   *   in [AlbumContents.nestedAlbums].
   */
  fun loadRootContents(
    rootDirectory: File = FileManagerEngine.getUserRootDirectory(),
    folders: List<File>? = null,
    additionalPaths: List<String> = emptyList(),
  ): AlbumContents {
    val targetFolders = (folders ?: resolveGalleryFolders(rootDirectory, additionalPaths))
      .filterNot { isHiddenByNoMedia(it) }
    val downloadsFolders = targetFolders.filter { isDownloadsDirectory(it) && isAlbumDirectory(it) }
    val unNestedFolders = targetFolders.filterNot { isDownloadsDirectory(it) }

    val baseContents = loadMergedAlbumContents(unNestedFolders)
    val mergedNestedAlbums = if (downloadsFolders.isNotEmpty()) {
      val existingDownloadsAlbum = baseContents.nestedAlbums.find {
        it.name.equals("Downloads", ignoreCase = true) || it.name.equals("Download", ignoreCase = true)
      }
      val allDownloadsDirs = if (existingDownloadsAlbum != null) {
        downloadsFolders + existingDownloadsAlbum.directories
      } else {
        downloadsFolders
      }
      val downloadsAlbum = buildDownloadsAlbum(allDownloadsDirs)
      (
        baseContents.nestedAlbums.filterNot {
          it.name.equals("Downloads", ignoreCase = true) || it.name.equals("Download", ignoreCase = true)
        } + downloadsAlbum
      ).sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    } else {
      baseContents.nestedAlbums
    }

    return baseContents.copy(
      directory = targetFolders.firstOrNull() ?: baseContents.directory,
      nestedAlbums = mergedNestedAlbums,
      directories = targetFolders,
    )
  }

  /**
   * Loads the root-level albums for the Gallery (`DCIM`, `Pictures`, and `Photos` un-nested, plus
   * `Downloads` treated as a single album).
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
   * - When [directories] represent the `Downloads` folder, treats `Downloads` as a single flat album
   *   with no nested sub-albums and all media files inside `Downloads` collected together.
   * - Otherwise, subdirectories across [directories] are un-nested to the top level, merging any
   *   subdirectories that share the same name (case-insensitively) into a single [GalleryAlbum],
   *   and direct photo files across [directories] are combined and sorted newest first.
   */
  fun loadMergedAlbumContents(directories: List<File>): AlbumContents {
    val distinctDirs = mutableListOf<File>()
    for (dir in directories) {
      if (distinctDirs.none { it.absolutePath.equals(dir.absolutePath, ignoreCase = true) }) {
        distinctDirs.add(dir)
      }
    }
    val primaryDir = distinctDirs.firstOrNull() ?: File("")

    if (distinctDirs.isNotEmpty() && distinctDirs.all { isDownloadsDirectory(it) }) {
      val photos = collectMediaFilesInDirectories(distinctDirs, maxDepth = 8)
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
        nestedAlbums = emptyList(),
        photos = photos,
        directories = distinctDirs,
      )
    }

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
   * Resolves the root folder group title (`"DCIM"`, `"Pictures"`, `"Download"`, etc.) for [file].
   */
  fun resolveDefaultFolderGroupTitle(file: File): String {
    var current: File? = file.absoluteFile
    while (current != null) {
      val name = current.name
      if (name.equals("DCIM", ignoreCase = true)) return "DCIM"
      if (name.equals("Pictures", ignoreCase = true)) return "Pictures"
      if (name.equals("Downloads", ignoreCase = true) || name.equals("Download", ignoreCase = true)) {
        return "Download"
      }
      current = current.parentFile
    }
    return "Pictures"
  }

  /**
   * Groups albums and photos by their root folders (`"DCIM"`, `"Pictures"`, and `"Download"`,
   * plus any custom loaded root folders) when [GalleryGroupOption.FOLDERS] is selected.
   */
  fun groupByDefaultFolders(
    rootDirectories: List<File>,
    currentAlbum: GalleryAlbum? = null,
    sortOrder: GallerySortOrder = GallerySortOrder.DATE_DESC,
    searchQuery: String = "",
    selectedTag: String? = null,
    indexMap: Map<String, GalleryPhotoIndexEntry> = emptyMap(),
  ): List<GalleryFolderGroup> {
    val sourceDirs = if (currentAlbum != null) {
      currentAlbum.directories
    } else {
      rootDirectories.filterNot { isHiddenByNoMedia(it) }
    }

    val dirsByGroupTitle = LinkedHashMap<String, MutableList<File>>()
    for (dir in sourceDirs) {
      val title = when {
        dir.name.equals("DCIM", ignoreCase = true) -> "DCIM"
        dir.name.equals("Pictures", ignoreCase = true) -> "Pictures"
        dir.name.equals("Downloads", ignoreCase = true) || dir.name.equals("Download", ignoreCase = true) -> "Download"
        currentAlbum == null && dir.name.isNotBlank() -> dir.name
        else -> resolveDefaultFolderGroupTitle(dir)
      }
      dirsByGroupTitle.getOrPut(title) { mutableListOf() }.add(dir)
    }

    val hasSearch = searchQuery.isNotBlank() || !selectedTag.isNullOrBlank()
    val groups = mutableListOf<GalleryFolderGroup>()

    for ((title, groupDirs) in dirsByGroupTitle) {
      val isRootDownloadsGroup = currentAlbum == null && (title.equals("Download", ignoreCase = true) || title.equals("Downloads", ignoreCase = true))
      val contents = if (isRootDownloadsGroup) {
        val validDownloadDirs = groupDirs.filter { isAlbumDirectory(it) }
        val downloadsAlbum = if (validDownloadDirs.isNotEmpty()) {
          buildDownloadsAlbum(validDownloadDirs)
        } else {
          null
        }
        AlbumContents(
          directory = groupDirs.firstOrNull() ?: File("Download"),
          nestedAlbums = if (downloadsAlbum != null && downloadsAlbum.totalPhotoCount > 0) {
            listOf(downloadsAlbum)
          } else {
            emptyList()
          },
          photos = emptyList(),
          directories = groupDirs,
        )
      } else {
        loadMergedAlbumContents(groupDirs)
      }
      if (hasSearch) {
        val scopeFiles = collectMediaFiles(contents.nestedAlbums, contents.photos)
        val scopePhotos = scopeFiles.map { file ->
          GalleryPhoto(
            file = file,
            name = file.name,
            path = file.absolutePath,
            sizeBytes = file.length(),
            lastModifiedMs = file.lastModified(),
            extension = file.extension.lowercase(Locale.ROOT),
          )
        }
        val searchRes = searchGallery(
          albums = contents.nestedAlbums,
          directPhotos = contents.photos,
          allScopePhotos = scopePhotos,
          indexMap = indexMap,
          searchQuery = searchQuery,
          selectedTag = selectedTag,
          sortOrder = sortOrder,
        )
        if (searchRes.matchingAlbums.isNotEmpty() || searchRes.matchingPhotos.isNotEmpty()) {
          groups.add(
            GalleryFolderGroup(
              title = title,
              albums = searchRes.matchingAlbums,
              photos = searchRes.matchingPhotos,
            )
          )
        }
      } else {
        val sortedAlbums = sortAlbums(contents.nestedAlbums, sortOrder)
        val sortedPhotos = sortPhotos(contents.photos, sortOrder)
        if (sortedAlbums.isNotEmpty() || sortedPhotos.isNotEmpty()) {
          groups.add(
            GalleryFolderGroup(
              title = title,
              albums = sortedAlbums,
              photos = sortedPhotos,
            )
          )
        }
      }
    }

    // Order standard default folders ("DCIM", "Pictures", "Download") consistently
    val folderPriority = listOf("DCIM", "Pictures", "Download")
    return groups.sortedWith(
      compareBy<GalleryFolderGroup> { group ->
        val idx = folderPriority.indexOfFirst { it.equals(group.title, ignoreCase = true) }
        if (idx >= 0) idx else 100
      }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
    )
  }

  private var inMemoryDefaultSortGroupConfig = GallerySortGroupConfig()
  private val inMemoryFolderSortGroupMap = mutableMapOf<String, GallerySortGroupConfig>()

  fun clearSortGroupConfigs() {
    inMemoryDefaultSortGroupConfig = GallerySortGroupConfig()
    inMemoryFolderSortGroupMap.clear()
  }

  fun serializeSortGroupConfig(config: GallerySortGroupConfig): String {
    return "${config.groupOption.name}:${config.sortOrder.name}"
  }

  fun parseSortGroupConfig(raw: String?): GallerySortGroupConfig? {
    if (raw.isNullOrBlank()) return null
    val parts = raw.split(":")
    if (parts.size != 2) return null
    return try {
      val group = GalleryGroupOption.valueOf(parts[0].trim())
      val sort = GallerySortOrder.valueOf(parts[1].trim())
      GallerySortGroupConfig(groupOption = group, sortOrder = sort)
    } catch (_: IllegalArgumentException) {
      null
    }
  }

  fun getDefaultSortGroupConfig(context: Context? = null): GallerySortGroupConfig {
    if (context == null) return inMemoryDefaultSortGroupConfig
    return try {
      val prefs = context.getSharedPreferences(GALLERY_PREFS_NAME, Context.MODE_PRIVATE)
      val combined = parseSortGroupConfig(prefs.getString(PREF_KEY_GALLERY_DEFAULT_SORT_GROUP, null))
      if (combined != null) {
        combined
      } else {
        val savedGroup = prefs.getString(PREF_KEY_GALLERY_GROUP_OPTION, null)
        val savedSort = prefs.getString(PREF_KEY_GALLERY_SORT_ORDER, null)
        if (savedGroup != null || savedSort != null) {
          GallerySortGroupConfig(
            groupOption = GalleryGroupOption.fromNameOrDefault(savedGroup),
            sortOrder = GallerySortOrder.fromNameOrDefault(savedSort),
          )
        } else {
          GallerySortGroupConfig()
        }
      }
    } catch (_: Throwable) {
      inMemoryDefaultSortGroupConfig
    }
  }

  fun setDefaultSortGroupConfig(
    context: Context? = null,
    config: GallerySortGroupConfig,
  ) {
    inMemoryDefaultSortGroupConfig = config
    if (context == null) return
    try {
      val prefs = context.getSharedPreferences(GALLERY_PREFS_NAME, Context.MODE_PRIVATE)
      prefs.edit()
        .putString(PREF_KEY_GALLERY_DEFAULT_SORT_GROUP, serializeSortGroupConfig(config))
        .putString(PREF_KEY_GALLERY_GROUP_OPTION, config.groupOption.name)
        .putString(PREF_KEY_GALLERY_SORT_ORDER, config.sortOrder.name)
        .apply()
    } catch (_: Throwable) {}
  }

  fun getFolderSortGroupConfig(
    context: Context? = null,
    folderKey: String,
  ): GallerySortGroupConfig? {
    if (context == null) return inMemoryFolderSortGroupMap[folderKey]
    return try {
      val prefs = context.getSharedPreferences(GALLERY_PREFS_NAME, Context.MODE_PRIVATE)
      parseSortGroupConfig(prefs.getString(PREF_PREFIX_GALLERY_FOLDER_SORT_GROUP + folderKey, null))
    } catch (_: Throwable) {
      inMemoryFolderSortGroupMap[folderKey]
    }
  }

  fun getFolderSortGroupConfig(
    context: Context? = null,
    directory: File,
  ): GallerySortGroupConfig? = getFolderSortGroupConfig(context, directory.absolutePath)

  fun hasFolderSortGroupConfig(
    context: Context? = null,
    folderKey: String,
  ): Boolean {
    return getFolderSortGroupConfig(context, folderKey) != null
  }

  fun hasFolderSortGroupConfig(
    context: Context? = null,
    directory: File,
  ): Boolean = hasFolderSortGroupConfig(context, directory.absolutePath)

  fun setFolderSortGroupConfig(
    context: Context? = null,
    folderKey: String,
    config: GallerySortGroupConfig,
  ) {
    inMemoryFolderSortGroupMap[folderKey] = config
    if (context == null) return
    try {
      val prefs = context.getSharedPreferences(GALLERY_PREFS_NAME, Context.MODE_PRIVATE)
      prefs.edit()
        .putString(PREF_PREFIX_GALLERY_FOLDER_SORT_GROUP + folderKey, serializeSortGroupConfig(config))
        .apply()
    } catch (_: Throwable) {}
  }

  fun setFolderSortGroupConfig(
    context: Context? = null,
    directory: File,
    config: GallerySortGroupConfig,
  ) = setFolderSortGroupConfig(context, directory.absolutePath, config)

  fun removeFolderSortGroupConfig(
    context: Context? = null,
    folderKey: String,
  ) {
    inMemoryFolderSortGroupMap.remove(folderKey)
    if (context == null) return
    try {
      val prefs = context.getSharedPreferences(GALLERY_PREFS_NAME, Context.MODE_PRIVATE)
      prefs.edit()
        .remove(PREF_PREFIX_GALLERY_FOLDER_SORT_GROUP + folderKey)
        .apply()
    } catch (_: Throwable) {}
  }

  fun removeFolderSortGroupConfig(
    context: Context? = null,
    directory: File,
  ) = removeFolderSortGroupConfig(context, directory.absolutePath)

  fun resolveSortGroupConfig(
    context: Context? = null,
    folderKey: String,
  ): Pair<GallerySortGroupConfig, Boolean> {
    val folderConfig = getFolderSortGroupConfig(context, folderKey)
    return if (folderConfig != null) {
      folderConfig to true
    } else {
      getDefaultSortGroupConfig(context) to false
    }
  }

  /**
   * Groups [photos] according to [groupOption] and sorts items within each group according to [sortOrder].
   */
  fun groupPhotos(
    photos: List<GalleryPhoto>,
    groupOption: GalleryGroupOption,
    sortOrder: GallerySortOrder = GallerySortOrder.DATE_DESC,
    indexMap: Map<String, GalleryPhotoIndexEntry> = emptyMap(),
    nowMs: Long = System.currentTimeMillis(),
  ): List<GalleryPhotoGroup> {
    val sorted = sortPhotos(photos.distinctBy { it.path }, sortOrder)
    if (sorted.isEmpty()) return emptyList()

    return when (groupOption) {
      GalleryGroupOption.FOLDERS,
      GalleryGroupOption.NONE -> {
        listOf(GalleryPhotoGroup(title = "", photos = sorted))
      }

      GalleryGroupOption.DATE -> {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
        val todayCal = Calendar.getInstance().apply {
          timeInMillis = nowMs
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
        val startOfTodayMs = todayCal.timeInMillis
        val startOfYesterdayMs = startOfTodayMs - 24L * 60L * 60L * 1000L
        val startOfWeekMs = startOfTodayMs - 6L * 24L * 60L * 60L * 1000L

        val buckets = LinkedHashMap<String, MutableList<GalleryPhoto>>()
        val dateOrderedPhotos = if (sortOrder == GallerySortOrder.DATE_ASC) {
          sorted.sortedWith(
            compareBy<GalleryPhoto> { it.lastModifiedMs }
              .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
          )
        } else {
          sorted.sortedWith(
            compareByDescending<GalleryPhoto> { it.lastModifiedMs }
              .thenByDescending(String.CASE_INSENSITIVE_ORDER) { it.name }
          )
        }

        for (photo in dateOrderedPhotos) {
          val ts = photo.lastModifiedMs
          val label = when {
            ts >= startOfTodayMs -> "Today"
            ts >= startOfYesterdayMs -> "Yesterday"
            ts >= startOfWeekMs -> "This Week"
            else -> monthFormat.format(Date(ts))
          }
          buckets.getOrPut(label) { mutableListOf() }.add(photo)
        }

        buckets.map { (title, groupItems) ->
          GalleryPhotoGroup(
            title = title,
            photos = sortPhotos(groupItems, sortOrder),
          )
        }
      }

      GalleryGroupOption.MEDIA_TYPE -> {
        val photosBucket = mutableListOf<GalleryPhoto>()
        val videosBucket = mutableListOf<GalleryPhoto>()
        val gifsBucket = mutableListOf<GalleryPhoto>()
        val screenshotsBucket = mutableListOf<GalleryPhoto>()

        for (photo in sorted) {
          val entry = indexMap[photo.path]
          val lowerName = photo.name.lowercase(Locale.ROOT)
          val lowerParent = photo.file.parentFile?.name?.lowercase(Locale.ROOT).orEmpty()
          val isScreenshot = lowerName.contains("screenshot") ||
            lowerParent.contains("screenshot") ||
            entry?.autoTags?.any { it.equals("Screenshot", ignoreCase = true) } == true
          when {
            photo.isVideo -> videosBucket.add(photo)
            photo.extension.equals("gif", ignoreCase = true) -> gifsBucket.add(photo)
            isScreenshot -> screenshotsBucket.add(photo)
            else -> photosBucket.add(photo)
          }
        }

        buildList {
          if (photosBucket.isNotEmpty()) add(GalleryPhotoGroup("Photos", photosBucket))
          if (videosBucket.isNotEmpty()) add(GalleryPhotoGroup("Videos", videosBucket))
          if (gifsBucket.isNotEmpty()) add(GalleryPhotoGroup("GIFs", gifsBucket))
          if (screenshotsBucket.isNotEmpty()) add(GalleryPhotoGroup("Screenshots", screenshotsBucket))
        }
      }

      GalleryGroupOption.SMART_TAGS -> {
        val orderedTags = extractAvailableAutoTags(sorted, indexMap).map { it.first }
        val groups = mutableListOf<GalleryPhotoGroup>()
        for (tag in orderedTags) {
          val matching = sorted.filter { photo ->
            indexMap[photo.path]?.autoTags?.any { it.equals(tag, ignoreCase = true) } == true
          }
          if (matching.isNotEmpty()) {
            groups.add(GalleryPhotoGroup(title = tag, photos = matching))
          }
        }
        val untagged = sorted.filter { photo ->
          indexMap[photo.path]?.autoTags.isNullOrEmpty()
        }
        if (untagged.isNotEmpty()) {
          groups.add(GalleryPhotoGroup(title = "Untagged", photos = untagged))
        }
        groups
      }
    }
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
   * Writes a valid 2-frame looping GIF89a animated image file to [outputFile] for deterministic
   * unit and instrumented testing.
   */
  fun writeSampleAnimatedGif(
    outputFile: File,
    lastModifiedMs: Long? = null,
  ): File {
    outputFile.parentFile?.mkdirs()
    // Valid 1x1 2-frame looping GIF89a (frame 1 = red, frame 2 = blue, 200ms delay per frame)
    val gifBytes = byteArrayOf(
      0x47, 0x49, 0x46, 0x38, 0x39, 0x61, // GIF89a
      0x01, 0x00, 0x01, 0x00,             // Logical screen 1x1
      0x80.toByte(), 0x00, 0x00,          // Global color table (2 colors)
      0xFF.toByte(), 0x00, 0x00,          // Color 0: Red
      0x00, 0x00, 0xFF.toByte(),          // Color 1: Blue
      // Netscape looping application extension
      0x21, 0xFF.toByte(), 0x0B,
      0x4E, 0x45, 0x54, 0x53, 0x43, 0x41, 0x50, 0x45, 0x32, 0x2E, 0x30,
      0x03, 0x01, 0x00, 0x00, 0x00,
      // Frame 1 GCE (200ms) + Image Descriptor + LZW data
      0x21, 0xF9.toByte(), 0x04, 0x00, 0x14, 0x00, 0x00, 0x00,
      0x2C, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00,
      0x02, 0x02, 0x44, 0x01, 0x00,
      // Frame 2 GCE (200ms) + Image Descriptor + LZW data
      0x21, 0xF9.toByte(), 0x04, 0x00, 0x14, 0x00, 0x00, 0x00,
      0x2C, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00,
      0x02, 0x02, 0x4C, 0x01, 0x00,
      0x3B, // Trailer
    )
    FileOutputStream(outputFile).use { fos ->
      fos.write(gifBytes)
      fos.flush()
    }
    if (lastModifiedMs != null) {
      outputFile.setLastModified(lastModifiedMs)
    }
    return outputFile
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

  /**
   * Maps a viewport touch position ([viewportX], [viewportY]) inside a container of size
   * ([containerWidth] x [containerHeight]) to normalized `(0f..1f, 0f..1f)` coordinates within
   * an image of size ([imageWidth] x [imageHeight]) rendered with `ContentScale.Fit`,
   * [zoomScale], and translation ([panOffsetX], [panOffsetY]).
   */
  fun mapViewportPointToImageNormalized(
    viewportX: Float,
    viewportY: Float,
    containerWidth: Float,
    containerHeight: Float,
    imageWidth: Int,
    imageHeight: Int,
    zoomScale: Float = 1f,
    panOffsetX: Float = 0f,
    panOffsetY: Float = 0f,
  ): Pair<Float, Float> {
    val cw = containerWidth.coerceAtLeast(1f)
    val ch = containerHeight.coerceAtLeast(1f)
    val safeZoom = zoomScale.coerceAtLeast(0.1f)
    val centerX = cw / 2f
    val centerY = ch / 2f
    val unzoomedX = centerX + (viewportX - centerX - panOffsetX) / safeZoom
    val unzoomedY = centerY + (viewportY - centerY - panOffsetY) / safeZoom

    if (imageWidth <= 0 || imageHeight <= 0) {
      return (unzoomedX / cw).coerceIn(0f, 1f) to (unzoomedY / ch).coerceIn(0f, 1f)
    }

    val imgAspect = imageWidth.toFloat() / imageHeight.toFloat()
    val containerAspect = cw / ch
    val (fitW, fitH) = if (containerAspect > imgAspect) {
      (ch * imgAspect) to ch
    } else {
      cw to (cw / imgAspect)
    }
    val imgLeft = (cw - fitW) / 2f
    val imgTop = (ch - fitH) / 2f
    val normX = ((unzoomedX - imgLeft) / fitW.coerceAtLeast(1f)).coerceIn(0f, 1f)
    val normY = ((unzoomedY - imgTop) / fitH.coerceAtLeast(1f)).coerceIn(0f, 1f)
    return normX to normY
  }

  /**
   * Samples the RGB color at normalized image coordinates ([normX], [normY]) in `0f..1f` from
   * [bitmap] (or directly from a PNG [file] when [bitmap] is unavailable, such as in JVM unit tests)
   * and returns an uppercase `#RRGGBB` hex string.
   */
  fun samplePhotoColorHex(
    file: File? = null,
    bitmap: Bitmap? = null,
    normX: Float = 0.5f,
    normY: Float = 0.5f,
    radiusPx: Int = 4,
  ): String {
    if (bitmap != null && bitmap.width > 0 && bitmap.height > 0) {
      return CameraEngine.sampleCenterRadiusColorHexFromBitmap(
        bitmap = bitmap,
        normX = normX,
        normY = normY,
        radiusPx = radiusPx,
      )
    }
    if (file != null && file.exists() && file.isFile) {
      val pngHex = samplePngFileColorHex(file, normX, normY, radiusPx)
      if (pngHex != null) return pngHex
      val decoded = try {
        BitmapFactory.decodeFile(file.absolutePath)
      } catch (_: Throwable) {
        null
      }
      if (decoded != null && decoded.width > 0 && decoded.height > 0) {
        return CameraEngine.sampleCenterRadiusColorHexFromBitmap(
          bitmap = decoded,
          normX = normX,
          normY = normY,
          radiusPx = radiusPx,
        )
      }
    }
    return CameraEngine.sampleSyntheticFocusColorHex(normX, normY)
  }

  private fun samplePngFileColorHex(
    file: File,
    normX: Float,
    normY: Float,
    radiusPx: Int,
  ): String? {
    return try {
      val bytes = file.readBytes()
      if (bytes.size < 33) return null
      val pngSig = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
      for (i in pngSig.indices) {
        if (bytes[i] != pngSig[i]) return null
      }
      var offset = 8
      var width = 0
      var height = 0
      var bitDepth = 0
      var colorType = 0
      val idatOut = ByteArrayOutputStream()

      while (offset + 8 <= bytes.size) {
        val length = ByteBuffer.wrap(bytes, offset, 4).int
        val type = String(bytes, offset + 4, 4, Charsets.US_ASCII)
        val dataStart = offset + 8
        if (length < 0 || dataStart + length > bytes.size) break
        when (type) {
          "IHDR" -> {
            if (length >= 13) {
              val buf = ByteBuffer.wrap(bytes, dataStart, 13)
              width = buf.int
              height = buf.int
              bitDepth = buf.get().toInt() and 0xFF
              colorType = buf.get().toInt() and 0xFF
            }
          }
          "IDAT" -> {
            idatOut.write(bytes, dataStart, length)
          }
          "IEND" -> break
        }
        offset = dataStart + length + 4
      }

      if (width <= 0 || height <= 0 || bitDepth != 8 || (colorType != 2 && colorType != 6)) {
        return null
      }
      val channels = if (colorType == 6) 4 else 3
      val rowBytes = 1 + width * channels
      val expectedRawSize = rowBytes * height
      val inflater = Inflater()
      inflater.setInput(idatOut.toByteArray())
      val raw = ByteArray(expectedRawSize)
      var totalInflated = 0
      while (!inflater.finished() && totalInflated < expectedRawSize) {
        val count = inflater.inflate(raw, totalInflated, expectedRawSize - totalInflated)
        if (count <= 0) break
        totalInflated += count
      }
      inflater.end()
      if (totalInflated < expectedRawSize) return null

      // Only handle filter type 0 (None) directly here; other filter types fall back to BitmapFactory on Android
      for (y in 0 until height) {
        if ((raw[y * rowBytes].toInt() and 0xFF) != 0) return null
      }

      val pixels = IntArray(width * height)
      for (y in 0 until height) {
        val rowStart = y * rowBytes + 1
        val pixRow = y * width
        for (x in 0 until width) {
          val pOff = rowStart + x * channels
          val r = raw[pOff].toInt() and 0xFF
          val g = raw[pOff + 1].toInt() and 0xFF
          val b = raw[pOff + 2].toInt() and 0xFF
          pixels[pixRow + x] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }
      }
      CameraEngine.sampleCenterRadiusColorHexFromArgbPixels(
        pixels = pixels,
        width = width,
        height = height,
        normX = normX,
        normY = normY,
        radiusPx = radiusPx,
      )
    } catch (_: Throwable) {
      null
    }
  }
}
