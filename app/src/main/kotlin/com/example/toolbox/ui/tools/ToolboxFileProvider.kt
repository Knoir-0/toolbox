package com.example.toolbox.ui.tools

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.RectF
import android.graphics.Shader
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Custom [FileProvider] that enables rich visual thumbnails in the Android Sharesheet
 * (`Intent.createChooser` / `ContentResolver.loadThumbnail`) for both **images** and **videos**:
 *
 * 1. Overrides [getType] to use [FileManagerEngine.getMimeType] so all image and video extensions
 *    (regardless of casing) resolve to their exact `image/...` or `video/...` MIME type.
 * 2. Overrides [getStreamTypes] to advertise `"image/jpeg"` thumbnail stream availability for
 *    video URIs (`video/...`) and `"image/png"` raster stream availability for SVG URIs
 *    (`image/svg+xml`). Android's Sharesheet (`PreviewDataProvider`) queries `getStreamTypes`
 *    for image stream availability to decide whether to show the visual media preview card.
 * 3. Overrides [query] to include [DocumentsContract.Document.COLUMN_FLAGS] with
 *    [DocumentsContract.Document.FLAG_SUPPORTS_THUMBNAIL] for image and video files as well as
 *    `mime_type` and `title`.
 * 4. Overrides [openTypedAssetFile] so that when the Android Sharesheet calls
 *    [ContentResolver.loadThumbnail], video files dynamically extract and return a cached JPEG
 *    video frame thumbnail (and SVG files return a rasterized PNG thumbnail), while standard
 *    file opens (`openFile`, `openInputStream`, or video/generic typed opens) continue to stream
 *    the original unmodified file.
 */
class ToolboxFileProvider : FileProvider() {

  override fun getType(uri: Uri): String {
    val ctx = context
    if (ctx != null) {
      val file = resolveFileFromUri(ctx, uri)
      if (file != null) {
        val mime = FileManagerEngine.getMimeType(file)
        if (mime != "application/octet-stream") {
          return mime
        }
      }
    }
    return super.getType(uri) ?: "application/octet-stream"
  }

  override fun getStreamTypes(uri: Uri, mimeTypeFilter: String): Array<String>? {
    val ctx = context
    if (ctx != null) {
      val file = resolveFileFromUri(ctx, uri)
      if (file != null) {
        return computeStreamTypesForFile(file, mimeTypeFilter)
      }
    }
    val primaryMime = super.getType(uri) ?: return null
    return if (matchesMimeTypeFilter(primaryMime, mimeTypeFilter)) {
      arrayOf(primaryMime)
    } else {
      null
    }
  }

  override fun query(
    uri: Uri,
    projection: Array<out String>?,
    selection: String?,
    selectionArgs: Array<out String>?,
    sortOrder: String?,
  ): Cursor {
    val ctx = context
    val file = if (ctx != null) resolveFileFromUri(ctx, uri) else null
    if (file == null) {
      return super.query(uri, projection, selection, selectionArgs, sortOrder)
    }

    val cols = projection ?: DEFAULT_QUERY_COLUMNS
    val mime = FileManagerEngine.getMimeType(file)
    val supportsThumb = isPreviewableMediaFile(file, mime)
    val displayName = uri.getQueryParameter("displayName") ?: file.name

    val outNames = ArrayList<String>(cols.size)
    val outValues = ArrayList<Any?>(cols.size)
    for (col in cols) {
      when (col) {
        OpenableColumns.DISPLAY_NAME -> {
          outNames.add(OpenableColumns.DISPLAY_NAME)
          outValues.add(displayName)
        }
        OpenableColumns.SIZE -> {
          outNames.add(OpenableColumns.SIZE)
          outValues.add(file.length())
        }
        DocumentsContract.Document.COLUMN_MIME_TYPE -> {
          outNames.add(DocumentsContract.Document.COLUMN_MIME_TYPE)
          outValues.add(mime)
        }
        DocumentsContract.Document.COLUMN_FLAGS -> {
          outNames.add(DocumentsContract.Document.COLUMN_FLAGS)
          outValues.add(
            if (supportsThumb) DocumentsContract.Document.FLAG_SUPPORTS_THUMBNAIL else 0
          )
        }
        DocumentsContract.Document.COLUMN_LAST_MODIFIED -> {
          outNames.add(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
          outValues.add(file.lastModified())
        }
        "title" -> {
          outNames.add("title")
          outValues.add(displayName)
        }
      }
    }

    val cursor = MatrixCursor(outNames.toTypedArray(), 1)
    cursor.addRow(outValues)
    return cursor
  }

  override fun openTypedAssetFile(
    uri: Uri,
    mimeTypeFilter: String,
    opts: Bundle?,
  ): AssetFileDescriptor {
    return openTypedAssetFile(uri, mimeTypeFilter, opts, null)
  }

  override fun openTypedAssetFile(
    uri: Uri,
    mimeTypeFilter: String,
    opts: Bundle?,
    signal: CancellationSignal?,
  ): AssetFileDescriptor {
    val ctx = context
    if (ctx != null) {
      val file = resolveFileFromUri(ctx, uri)
      if (file != null && file.exists() && file.isFile) {
        val primaryMime = FileManagerEngine.getMimeType(file)
        val targetMaxDim = extractRequestedMaxDimension(opts)

        // 1. Video file requested as an image/* thumbnail stream (e.g. by ContentResolver.loadThumbnail in Android Sharesheet)
        val isVideo = FileManagerEngine.isVideoFile(file) || primaryMime.startsWith("video/")
        if (isVideo && shouldServeVideoThumbnailStream(primaryMime, mimeTypeFilter, opts)) {
          val thumbFile = getOrCreateVideoShareThumbnailFile(ctx, file, targetMaxDim)
          if (thumbFile != null && thumbFile.exists() && thumbFile.length() > 0L) {
            val pfd = ParcelFileDescriptor.open(thumbFile, ParcelFileDescriptor.MODE_READ_ONLY)
            return AssetFileDescriptor(pfd, 0L, thumbFile.length())
          }
        }

        // 2. SVG image requested as a raster image thumbnail by ContentResolver.loadThumbnail
        val isSvg = file.extension.equals("svg", ignoreCase = true) || primaryMime == "image/svg+xml"
        if (isSvg && shouldServeSvgRasterStream(primaryMime, mimeTypeFilter, opts)) {
          val thumbFile = getOrCreateSvgShareThumbnailFile(ctx, file, targetMaxDim)
          if (thumbFile != null && thumbFile.exists() && thumbFile.length() > 0L) {
            val pfd = ParcelFileDescriptor.open(thumbFile, ParcelFileDescriptor.MODE_READ_ONLY)
            return AssetFileDescriptor(pfd, 0L, thumbFile.length())
          }
        }

        // 3. Direct image or file stream when mimeTypeFilter matches primaryMime
        if (matchesMimeTypeFilter(primaryMime, mimeTypeFilter)) {
          val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
          return AssetFileDescriptor(pfd, 0L, file.length())
        }
      }
    }
    return super.openTypedAssetFile(uri, mimeTypeFilter, opts, signal)
      ?: throw FileNotFoundException("Could not open typed asset file for $uri ($mimeTypeFilter)")
  }

  companion object {
    private const val SHARE_THUMBNAIL_CACHE_DIR = "share_thumbnails"
    private const val DEFAULT_THUMBNAIL_MAX_DIM_PX = 512

    private val DEFAULT_QUERY_COLUMNS = arrayOf(
      OpenableColumns.DISPLAY_NAME,
      OpenableColumns.SIZE,
      DocumentsContract.Document.COLUMN_MIME_TYPE,
      DocumentsContract.Document.COLUMN_FLAGS,
    )

    /**
     * Pure MIME filter matching that works identically in Android runtime and JVM unit tests.
     */
    fun matchesMimeTypeFilter(concreteMimeType: String, mimeTypeFilter: String): Boolean {
      if (concreteMimeType.isBlank() || mimeTypeFilter.isBlank()) return false
      if (mimeTypeFilter == "*/*" || mimeTypeFilter == "*") return true
      val cNorm = concreteMimeType.lowercase(Locale.ROOT).trim()
      val fNorm = mimeTypeFilter.lowercase(Locale.ROOT).trim()
      if (cNorm == fNorm) return true

      val cSlash = cNorm.indexOf('/')
      val fSlash = fNorm.indexOf('/')
      if (cSlash <= 0 || fSlash <= 0) return false

      val cType = cNorm.substring(0, cSlash)
      val cSub = cNorm.substring(cSlash + 1)
      val fType = fNorm.substring(0, fSlash)
      val fSub = fNorm.substring(fSlash + 1)

      if (fType == "*" && (fSub == "*" || fSub == cSub)) return true
      if (cType == fType && (fSub == "*" || cSub == "*")) return true
      return false
    }

    fun isPreviewableMediaFile(file: File, mimeType: String = FileManagerEngine.getMimeType(file)): Boolean {
      return FileManagerEngine.isImageFile(file) ||
        FileManagerEngine.isVideoFile(file) ||
        mimeType.startsWith("image/") ||
        mimeType.startsWith("video/")
    }

    /**
     * Computes the stream MIME types offered by [ToolboxFileProvider] for [file] under [mimeTypeFilter].
     * For video files, advertises `"image/jpeg"` in addition to the video MIME type so Android's
     * Sharesheet (`PreviewDataProvider`) treats video URIs as visual media with thumbnails.
     */
    fun computeStreamTypesForFile(file: File, mimeTypeFilter: String): Array<String>? {
      val primaryMime = FileManagerEngine.getMimeType(file)
      val types = ArrayList<String>(2)
      if (matchesMimeTypeFilter(primaryMime, mimeTypeFilter)) {
        types.add(primaryMime)
      }

      val isVideo = FileManagerEngine.isVideoFile(file) || primaryMime.startsWith("video/")
      val isSvg = file.extension.equals("svg", ignoreCase = true) || primaryMime == "image/svg+xml"
      val isImage = FileManagerEngine.isImageFile(file) || primaryMime.startsWith("image/")

      when {
        isVideo -> {
          if (matchesMimeTypeFilter("image/jpeg", mimeTypeFilter) && "image/jpeg" !in types) {
            types.add("image/jpeg")
          }
        }
        isSvg -> {
          if (matchesMimeTypeFilter("image/png", mimeTypeFilter) && "image/png" !in types) {
            types.add("image/png")
          }
        }
        isImage -> {
          if (types.isEmpty() && matchesMimeTypeFilter("image/jpeg", mimeTypeFilter)) {
            types.add(primaryMime)
          }
        }
      }

      return if (types.isEmpty()) null else types.toTypedArray()
    }

    /**
     * Returns `true` when `openTypedAssetFile` on a video URI should return a JPEG frame thumbnail
     * instead of the raw video bytes.
     */
    fun shouldServeVideoThumbnailStream(
      primaryMime: String,
      mimeTypeFilter: String,
      opts: Bundle? = null,
    ): Boolean {
      val requestsImage = matchesMimeTypeFilter("image/jpeg", mimeTypeFilter)
      val matchesVideoDirectly = matchesMimeTypeFilter(primaryMime, mimeTypeFilter)
      val hasThumbnailSizeOpt = opts?.containsKey(ContentResolver.EXTRA_SIZE) == true
      // Serve the JPEG thumbnail whenever the caller asks for an image/* stream (and not video/*)
      // or explicitly passes ContentResolver.EXTRA_SIZE for thumbnail loading.
      return (requestsImage && !matchesVideoDirectly) || (requestsImage && hasThumbnailSizeOpt)
    }

    fun shouldServeSvgRasterStream(
      primaryMime: String,
      mimeTypeFilter: String,
      opts: Bundle? = null,
    ): Boolean {
      val hasThumbnailSizeOpt = opts?.containsKey(ContentResolver.EXTRA_SIZE) == true
      val requestsPngOrJpeg =
        mimeTypeFilter.equals("image/png", ignoreCase = true) ||
          mimeTypeFilter.equals("image/jpeg", ignoreCase = true)
      val requestsGenericImage = mimeTypeFilter.equals("image/*", ignoreCase = true)
      return requestsPngOrJpeg || (requestsGenericImage && hasThumbnailSizeOpt) ||
        (matchesMimeTypeFilter("image/png", mimeTypeFilter) && !matchesMimeTypeFilter(primaryMime, mimeTypeFilter))
    }

    /**
     * Resolves a `content://<authority>/<tag>/<relativePath>` URI created by [FileProvider]
     * back to its underlying [File] on disk, matching `res/xml/file_paths.xml`.
     */
    fun resolveFileFromUri(context: Context, uri: Uri): File? {
      val encodedPath = uri.encodedPath ?: return null
      val splitIndex = encodedPath.indexOf('/', 1)
      if (splitIndex <= 1) return null
      val tag = Uri.decode(encodedPath.substring(1, splitIndex))
      val relPath = Uri.decode(encodedPath.substring(splitIndex + 1))
      return resolveFileFromTagAndRelativePath(context, tag, relPath)
    }

    fun resolveFileFromTagAndRelativePath(
      context: Context,
      tag: String,
      relativePath: String,
    ): File? {
      val baseDir: File = when (tag) {
        "shared_images" -> File(context.cacheDir, "images")
        "cache_files" -> context.cacheDir
        "internal_files" -> context.filesDir
        "external_files" -> context.getExternalFilesDir(null) ?: context.filesDir
        "external_storage" -> Environment.getExternalStorageDirectory()
        "device_root" -> File("/")
        else -> return null
      }
      val candidate = File(baseDir, relativePath)
      return try {
        candidate.canonicalFile
      } catch (_: Throwable) {
        candidate
      }
    }

    private fun extractRequestedMaxDimension(opts: Bundle?): Int {
      if (opts == null) return DEFAULT_THUMBNAIL_MAX_DIM_PX
      return try {
        val point: Point? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          opts.getParcelable(ContentResolver.EXTRA_SIZE, Point::class.java)
        } else {
          @Suppress("DEPRECATION")
          opts.getParcelable(ContentResolver.EXTRA_SIZE)
        }
        val maxSide = maxOf(point?.x ?: 0, point?.y ?: 0)
        if (maxSide > 0) maxSide.coerceIn(128, 1024) else DEFAULT_THUMBNAIL_MAX_DIM_PX
      } catch (_: Throwable) {
        DEFAULT_THUMBNAIL_MAX_DIM_PX
      }
    }

    /**
     * Returns or creates a cached JPEG video frame thumbnail in `context.cacheDir/share_thumbnails`
     * so the Android Sharesheet can display a crisp preview of any shared video file.
     */
    fun getOrCreateVideoShareThumbnailFile(
      context: Context,
      videoFile: File,
      targetMaxDimPx: Int = DEFAULT_THUMBNAIL_MAX_DIM_PX,
    ): File? {
      if (!videoFile.exists() || !videoFile.isFile) return null
      return try {
        val cacheDir = File(context.cacheDir, SHARE_THUMBNAIL_CACHE_DIR)
        if (!cacheDir.exists()) {
          cacheDir.mkdirs()
        }
        val pathHash = videoFile.absolutePath.hashCode().toUInt().toString(16)
        val thumbFile = File(
          cacheDir,
          "vid_thumb_${pathHash}_${videoFile.lastModified()}_${videoFile.length()}.jpg",
        )
        if (thumbFile.exists() && thumbFile.length() > 0L) {
          return thumbFile
        }

        val extracted = extractVideoFrameBitmap(videoFile, targetMaxDimPx)
          ?: createFallbackVideoPreviewBitmap(videoFile, targetMaxDimPx)

        if (extracted != null) {
          FileOutputStream(thumbFile).use { out ->
            extracted.compress(Bitmap.CompressFormat.JPEG, 88, out)
            out.flush()
          }
          if (thumbFile.exists() && thumbFile.length() > 0L) {
            return thumbFile
          }
        }
        null
      } catch (_: Throwable) {
        null
      }
    }

    private fun getOrCreateSvgShareThumbnailFile(
      context: Context,
      svgFile: File,
      targetMaxDimPx: Int = DEFAULT_THUMBNAIL_MAX_DIM_PX,
    ): File? {
      if (!svgFile.exists() || !svgFile.isFile) return null
      return try {
        val cacheDir = File(context.cacheDir, SHARE_THUMBNAIL_CACHE_DIR)
        if (!cacheDir.exists()) {
          cacheDir.mkdirs()
        }
        val pathHash = svgFile.absolutePath.hashCode().toUInt().toString(16)
        val thumbFile = File(
          cacheDir,
          "svg_thumb_${pathHash}_${svgFile.lastModified()}_${svgFile.length()}.png",
        )
        if (thumbFile.exists() && thumbFile.length() > 0L) {
          return thumbFile
        }
        val rendered = GalleryEngine.decodeThumbnailBitmap(
          file = svgFile,
          reqSizePx = targetMaxDimPx,
        ) ?: return null
        FileOutputStream(thumbFile).use { out ->
          rendered.compress(Bitmap.CompressFormat.PNG, 100, out)
          out.flush()
        }
        thumbFile.takeIf { it.exists() && it.length() > 0L }
      } catch (_: Throwable) {
        null
      }
    }

    private fun extractVideoFrameBitmap(videoFile: File, targetMaxDimPx: Int): Bitmap? {
      // 1. Reuse in-memory GalleryEngine thumbnail cache when already decoded
      GalleryEngine.getCachedThumbnailBitmap(videoFile)?.let { cached ->
        if (!cached.isRecycled && cached.width > 0 && cached.height > 0) {
          return scaleBitmapDownIfNeeded(cached, targetMaxDimPx)
        }
      }

      // 2. Extract frame via MediaMetadataRetriever
      val retriever = MediaMetadataRetriever()
      try {
        retriever.setDataSource(videoFile.absolutePath)
        val rawW = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
        val rawH = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
        val rot = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
        val dispW = if (rot == 90 || rot == 270) rawH else rawW
        val dispH = if (rot == 90 || rot == 270) rawW else rawH

        val frame: Bitmap? = if (
          Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 &&
          dispW > 0 &&
          dispH > 0
        ) {
          val maxSide = maxOf(dispW, dispH)
          val scale = if (maxSide > targetMaxDimPx) targetMaxDimPx.toFloat() / maxSide.toFloat() else 1f
          val dstW = (dispW * scale).roundToInt().coerceAtLeast(1)
          val dstH = (dispH * scale).roundToInt().coerceAtLeast(1)
          retriever.getScaledFrameAtTime(
            0L,
            MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
            dstW,
            dstH,
          ) ?: retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } else {
          retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            ?: retriever.frameAtTime
        } ?: retriever.embeddedPicture?.let { bytes ->
          BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

        if (frame != null && frame.width > 0 && frame.height > 0) {
          return scaleBitmapDownIfNeeded(frame, targetMaxDimPx)
        }
      } catch (_: Throwable) {
        // Fall through to ThumbnailUtils
      } finally {
        try {
          retriever.release()
        } catch (_: Throwable) {}
      }

      // 3. Fallback to ThumbnailUtils
      return try {
        @Suppress("DEPRECATION")
        val fallback = ThumbnailUtils.createVideoThumbnail(
          videoFile.absolutePath,
          MediaStore.Images.Thumbnails.MINI_KIND,
        )
        fallback?.let { scaleBitmapDownIfNeeded(it, targetMaxDimPx) }
      } catch (_: Throwable) {
        null
      }
    }

    private fun scaleBitmapDownIfNeeded(bitmap: Bitmap, targetMaxDimPx: Int): Bitmap {
      val w = bitmap.width
      val h = bitmap.height
      val maxSide = maxOf(w, h)
      if (maxSide <= targetMaxDimPx || targetMaxDimPx <= 0) return bitmap
      val scale = targetMaxDimPx.toFloat() / maxSide.toFloat()
      val dstW = (w * scale).roundToInt().coerceAtLeast(1)
      val dstH = (h * scale).roundToInt().coerceAtLeast(1)
      return try {
        Bitmap.createScaledBitmap(bitmap, dstW, dstH, true)
      } catch (_: Throwable) {
        bitmap
      }
    }

    /**
     * Renders a clean 16:9 cinema-styled video thumbnail fallback bitmap when the underlying
     * video codec cannot be decoded by [MediaMetadataRetriever] on the current device.
     */
    private fun createFallbackVideoPreviewBitmap(videoFile: File, targetMaxDimPx: Int): Bitmap? {
      return try {
        val width = targetMaxDimPx.coerceIn(256, 640)
        val height = (width * 9f / 16f).roundToInt().coerceAtLeast(144)
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          shader = LinearGradient(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            Color.rgb(34, 30, 48),
            Color.rgb(64, 48, 92),
            Shader.TileMode.CLAMP,
          )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val cx = width / 2f
        val cy = height / 2f
        val circleRadius = height * 0.22f
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = Color.argb(215, 255, 255, 255)
          style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, circleRadius, circlePaint)

        val playPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = Color.rgb(64, 48, 92)
          style = Paint.Style.FILL
        }
        val triHalf = circleRadius * 0.42f
        val playPath = Path().apply {
          moveTo(cx - triHalf * 0.7f, cy - triHalf)
          lineTo(cx + triHalf * 1.1f, cy)
          lineTo(cx - triHalf * 0.7f, cy + triHalf)
          close()
        }
        canvas.drawPath(playPath, playPaint)

        val extLabel = videoFile.extension.uppercase(Locale.ROOT).ifBlank { "VIDEO" }
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = Color.argb(160, 0, 0, 0)
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = Color.WHITE
          textSize = height * 0.085f
          isFakeBoldText = true
          textAlign = Paint.Align.CENTER
        }
        val pillW = (textPaint.measureText(extLabel) + height * 0.14f).coerceAtLeast(height * 0.28f)
        val pillH = height * 0.15f
        val pillRect = RectF(
          width - pillW - height * 0.06f,
          height - pillH - height * 0.06f,
          width - height * 0.06f,
          height - height * 0.06f,
        )
        canvas.drawRoundRect(pillRect, pillH / 2f, pillH / 2f, pillPaint)
        val textY = pillRect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(extLabel, pillRect.centerX(), textY, textPaint)

        bmp
      } catch (_: Throwable) {
        null
      }
    }
  }
}
