package com.example.toolbox.ui.tools

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SharePreviewFileProviderTest {

  @Test
  fun matchesMimeTypeFilter_handlesExactAndWildcardPatterns() {
    assertTrue(ToolboxFileProvider.matchesMimeTypeFilter("image/jpeg", "image/*"))
    assertTrue(ToolboxFileProvider.matchesMimeTypeFilter("image/png", "image/png"))
    assertTrue(ToolboxFileProvider.matchesMimeTypeFilter("video/mp4", "*/*"))
    assertTrue(ToolboxFileProvider.matchesMimeTypeFilter("video/mp4", "video/*"))
    assertFalse(ToolboxFileProvider.matchesMimeTypeFilter("video/mp4", "image/*"))
    assertFalse(ToolboxFileProvider.matchesMimeTypeFilter("application/pdf", "image/*"))
  }

  @Test
  fun computeStreamTypesForFile_advertisesImageJpegThumbnailStreamForVideos() {
    val videoFile = File("/tmp/sample_recording.mp4")

    // When Android Sharesheet queries getStreamTypes(videoUri, "image/*"), it must return ["image/jpeg"]
    val imageStreams = ToolboxFileProvider.computeStreamTypesForFile(videoFile, "image/*")
    assertArrayEquals(arrayOf("image/jpeg"), imageStreams)

    // When queried with "*/*", it returns both the primary video MIME type and "image/jpeg"
    val allStreams = ToolboxFileProvider.computeStreamTypesForFile(videoFile, "*/*")
    assertArrayEquals(arrayOf("video/mp4", "image/jpeg"), allStreams)

    // When queried with "video/*", it returns only the video MIME type
    val videoStreams = ToolboxFileProvider.computeStreamTypesForFile(videoFile, "video/*")
    assertArrayEquals(arrayOf("video/mp4"), videoStreams)
  }

  @Test
  fun computeStreamTypesForFile_advertisesImageStreamForImagesAndNullForNonMedia() {
    val jpegFile = File("/tmp/photo.jpg")
    assertArrayEquals(
      arrayOf("image/jpeg"),
      ToolboxFileProvider.computeStreamTypesForFile(jpegFile, "image/*"),
    )

    val svgFile = File("/tmp/diagram.svg")
    assertArrayEquals(
      arrayOf("image/svg+xml", "image/png"),
      ToolboxFileProvider.computeStreamTypesForFile(svgFile, "image/*"),
    )

    val pdfFile = File("/tmp/document.pdf")
    assertNull(ToolboxFileProvider.computeStreamTypesForFile(pdfFile, "image/*"))
  }

  @Test
  fun shouldServeVideoThumbnailStream_distinguishesThumbnailRequestsFromVideoReads() {
    // ContentResolver.loadThumbnail requests "image/*" on a "video/mp4" URI -> serve thumbnail
    assertTrue(
      ToolboxFileProvider.shouldServeVideoThumbnailStream(
        primaryMime = "video/mp4",
        mimeTypeFilter = "image/*",
      )
    )
    assertTrue(
      ToolboxFileProvider.shouldServeVideoThumbnailStream(
        primaryMime = "video/x-matroska",
        mimeTypeFilter = "image/jpeg",
      )
    )

    // Target app opening the video as "video/*" or "*/*" without EXTRA_SIZE -> serve raw video
    assertFalse(
      ToolboxFileProvider.shouldServeVideoThumbnailStream(
        primaryMime = "video/mp4",
        mimeTypeFilter = "video/*",
      )
    )
    assertFalse(
      ToolboxFileProvider.shouldServeVideoThumbnailStream(
        primaryMime = "video/mp4",
        mimeTypeFilter = "*/*",
      )
    )
  }

  @Test
  fun resolveShareClipMimeTypes_and_resolveAggregateShareMimeType_coverImagesAndVideos() {
    val img1 = File("/tmp/one.png")
    val img2 = File("/tmp/two.jpg")
    val vid1 = File("/tmp/clip.mp4")
    val vid2 = File("/tmp/movie.mkv")

    assertArrayEquals(
      arrayOf("image/png"),
      CrossToolActions.resolveShareClipMimeTypes(img1),
    )
    assertArrayEquals(
      arrayOf("video/mp4", "image/jpeg"),
      CrossToolActions.resolveShareClipMimeTypes(vid1),
    )

    assertEquals("image/*", CrossToolActions.resolveAggregateShareMimeType(listOf(img1, img2)))
    assertEquals("video/*", CrossToolActions.resolveAggregateShareMimeType(listOf(vid1, vid2)))
    assertEquals("*/*", CrossToolActions.resolveAggregateShareMimeType(listOf(img1, vid1)))
  }
}
