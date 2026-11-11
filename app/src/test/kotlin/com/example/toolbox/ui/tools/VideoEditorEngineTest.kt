package com.example.toolbox.ui.tools

import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.ToolCategory
import java.io.File
import java.nio.file.Files
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

class VideoEditorEngineTest {

  @Test
  fun videoEditorTool_isRegisteredInMediaAndCreativeBeforeSettings() {
    assertEquals("Video Editor", Tool.VIDEO_EDITOR.title)
    assertEquals(ToolCategory.MEDIA_AND_CREATIVE, Tool.VIDEO_EDITOR.category)
    assertEquals(Tool.SETTINGS, Tool.entries.last())
    assertTrue(Tool.entries.indexOf(Tool.VIDEO_EDITOR) < Tool.entries.indexOf(Tool.SETTINGS))
  }

  @Test
  fun isVideoFile_recognizesSupportedVideoExtensionsAndRejectsNonVideos() {
    val tempDir = Files.createTempDirectory("video_ext_test").toFile()
    try {
      assertTrue(VideoEditorEngine.isVideoFile(File(tempDir, "clip.mp4")))
      assertTrue(VideoEditorEngine.isVideoFile(File(tempDir, "CLIP.MOV")))
      assertTrue(VideoEditorEngine.isVideoFile(File(tempDir, "movie.mkv")))
      assertTrue(VideoEditorEngine.isVideoFile(File(tempDir, "recording.webm")))
      assertTrue(VideoEditorEngine.isVideoFile(File(tempDir, "cam.3gp")))

      assertFalse(VideoEditorEngine.isVideoFile(tempDir))
      assertFalse(VideoEditorEngine.isVideoFile(File(tempDir, "photo.png")))
      assertFalse(VideoEditorEngine.isVideoFile(File(tempDir, "song.mp3")))
      assertFalse(VideoEditorEngine.isVideoFile(File(tempDir, "notes.txt")))
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun formatTimecode_andFormatFfmpegSeconds_formatAccurately() {
    assertEquals("00:00.0", VideoEditorEngine.formatTimecode(0L))
    assertEquals("00:04.2", VideoEditorEngine.formatTimecode(4_250L))
    assertEquals("01:05.9", VideoEditorEngine.formatTimecode(65_910L))
    assertEquals("01:05", VideoEditorEngine.formatTimecode(65_910L, includeTenths = false))
    assertEquals("1:02:03.4", VideoEditorEngine.formatTimecode(3_723_400L))
    assertEquals("1:02:03", VideoEditorEngine.formatTimecode(3_723_400L, includeTenths = false))

    assertEquals("0.000", VideoEditorEngine.formatFfmpegSeconds(0L))
    assertEquals("1.250", VideoEditorEngine.formatFfmpegSeconds(1_250L))
    assertEquals("14.080", VideoEditorEngine.formatFfmpegSeconds(14_080L))
  }

  @Test
  fun extractFileStem_andResolveOutputFile_avoidCollisionsAndSourceOverwrite() {
    assertEquals("sunset_clip", VideoEditorEngine.extractFileStem("sunset_clip.mp4"))
    assertEquals("my.vacation.2026", VideoEditorEngine.extractFileStem("my.vacation.2026.mov"))
    assertEquals("edited_video", VideoEditorEngine.extractFileStem("   "))

    val tempDir = Files.createTempDirectory("video_resolve_test").toFile()
    try {
      val src = File(tempDir, "clip.mp4").apply { writeText("source") }
      // Same name as sourceFile must auto-increment so source is never overwritten
      val resolvedSameAsSource = VideoEditorEngine.resolveOutputFile(
        outputDir = tempDir,
        desiredStem = "clip",
        extension = "mp4",
        sourceFile = src,
      )
      assertEquals("clip_1.mp4", resolvedSameAsSource.name)

      File(tempDir, "edited.mp4").writeText("existing")
      val resolvedCollision = VideoEditorEngine.resolveOutputFile(
        outputDir = tempDir,
        desiredStem = "edited",
        extension = "mp4",
      )
      assertEquals("edited_1.mp4", resolvedCollision.name)
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun videoEditState_detectsChangesAndCoercesBoundsSafely() {
    val initial = VideoEditState.initialForDuration(10_000L)
    assertFalse(initial.hasAnyChanges)
    assertFalse(initial.hasTrimChanges)
    assertFalse(initial.hasVisualReencodeChanges)
    assertEquals(10_000L, initial.estimatedOutputDurationMs)

    val trimmedAndFast = initial.copy(
      trimStartMs = 2_000L,
      trimEndMs = 8_000L,
      playbackSpeed = 2.0f,
    ).coerceForDuration(10_000L)

    assertTrue(trimmedAndFast.hasTrimChanges)
    assertTrue(trimmedAndFast.hasSpeedChanges)
    assertTrue(trimmedAndFast.hasVisualReencodeChanges)
    assertEquals(6_000L, trimmedAndFast.trimmedSourceDurationMs)
    assertEquals(3_000L, trimmedAndFast.estimatedOutputDurationMs)

    // Coerce inverted or out-of-range trim bounds to preserve MIN_VIDEO_TRIM_DURATION_MS
    val coerced = initial.copy(
      trimStartMs = 9_950L,
      trimEndMs = 1_000L,
      rotationDegrees = -90,
    ).coerceForDuration(10_000L)
    assertEquals(9_800L, coerced.trimStartMs)
    assertEquals(10_000L, coerced.trimEndMs)
    assertEquals(270, coerced.normalizedRotation)
  }

  @Test
  fun determineStrategy_selectsStreamCopyForPureTrimOrMute_andHardwareWhenReencoding() {
    val mp4Meta = VideoMetadata(
      file = File("/tmp/sample.mp4"),
      durationMs = 12_000L,
      width = 1920,
      height = 1080,
    )
    val mkvMeta = mp4Meta.copy(file = File("/tmp/sample.mkv"))

    // 1. Pure trim + mute on MP4 at Original resolution -> STREAM_COPY
    val trimMuteState = VideoEditState.initialForDuration(12_000L).copy(
      trimStartMs = 1_000L,
      trimEndMs = 9_000L,
      isMuted = true,
    )
    assertEquals(
      VideoProcessingStrategy.STREAM_COPY,
      VideoEditorEngine.determineStrategy(mp4Meta, trimMuteState, VideoExportOptions()),
    )

    // 2. MKV source -> MP4 requires container/codec transcode -> HARDWARE_FFMPEG
    assertEquals(
      VideoProcessingStrategy.HARDWARE_FFMPEG,
      VideoEditorEngine.determineStrategy(mkvMeta, trimMuteState, VideoExportOptions()),
    )

    // 3. Resolution downscale -> HARDWARE_FFMPEG
    assertEquals(
      VideoProcessingStrategy.HARDWARE_FFMPEG,
      VideoEditorEngine.determineStrategy(
        mp4Meta,
        trimMuteState,
        VideoExportOptions(resolution = VideoResolutionPreset.P720),
      ),
    )

    // 4. Visual filter or crop -> HARDWARE_FFMPEG with SOFTWARE_FFMPEG fallback
    val filteredState = trimMuteState.copy(filterPreset = VideoFilterPreset.WARM)
    val chain = VideoEditorEngine.buildStrategyFallbackChain(mp4Meta, filteredState, VideoExportOptions())
    assertEquals(
      listOf(VideoProcessingStrategy.HARDWARE_FFMPEG, VideoProcessingStrategy.SOFTWARE_FFMPEG),
      chain,
    )

    // 5. GIF -> SOFTWARE_FFMPEG
    assertEquals(
      VideoProcessingStrategy.SOFTWARE_FFMPEG,
      VideoEditorEngine.determineStrategy(
        mp4Meta,
        trimMuteState,
        VideoExportOptions(format = VideoExportFormat.GIF),
      ),
    )
  }

  @Test
  fun computeEvenCropPixels_alwaysProducesEvenDimensionsWithinFrame() {
    val rect = NormalizedRect(left = 0.133f, top = 0.077f, right = 0.891f, bottom = 0.923f)
    val px = VideoEditorEngine.computeEvenCropPixels(rect, frameWidth = 1920, frameHeight = 1080)

    assertEquals(0, px.x % 2)
    assertEquals(0, px.y % 2)
    assertEquals(0, px.width % 2)
    assertEquals(0, px.height % 2)
    assertTrue(px.width >= 2)
    assertTrue(px.height >= 2)
    assertTrue(px.x + px.width <= 1920)
    assertTrue(px.y + px.height <= 1080)
  }

  @Test
  fun buildFilterStages_usesOnlyLgplVerifiedFilters_andChainsExtremeAtempo() {
    val meta = VideoMetadata(
      file = File("/tmp/clip.mp4"),
      durationMs = 10_000L,
      width = 1920,
      height = 1080,
    )
    val state = VideoEditState(
      durationMs = 10_000L,
      trimStartMs = 1_000L,
      trimEndMs = 9_000L,
      cropRect = NormalizedRect(0.1f, 0.1f, 0.9f, 0.9f),
      rotationDegrees = 90,
      flipHorizontal = true,
      playbackSpeed = 0.25f,
      volume = 1.5f,
      fadeInOut = true,
      brightness = 20f,
      contrast = 1.2f,
      saturation = 1.3f,
      filterPreset = VideoFilterPreset.VINTAGE,
    )

    val vf = VideoEditorEngine.buildVideoFilterStages(
      metadata = meta,
      editState = state,
      options = VideoExportOptions(resolution = VideoResolutionPreset.P720),
    )
    val vfJoined = vf.joinToString(",")

    assertTrue(vfJoined.contains("transpose=1"))
    assertTrue(vfJoined.contains("hflip"))
    assertTrue(vfJoined.contains("crop="))
    assertTrue(vfJoined.contains("scale="))
    assertTrue(vfJoined.contains("colorchannelmixer="))
    assertTrue(vfJoined.contains("vignette="))
    assertTrue(vfJoined.contains("hue=s=1.30"))
    assertTrue(vfJoined.contains("lutyuv="))
    assertTrue(vfJoined.contains("setpts=4.0000*(PTS-STARTPTS)"))
    assertTrue(vfJoined.contains("fade=t=in"))
    assertTrue(vfJoined.contains("fade=t=out"))
    // Never emit GPL-only eq filter
    assertFalse(vfJoined.contains("eq="))

    val afSlow = VideoEditorEngine.buildAudioFilterStages(state)
    // 0.25x requires two chained atempo=0.5 stages
    assertEquals(2, afSlow.count { it.startsWith("atempo=0.5") })
    assertTrue(afSlow.any { it == "volume=1.50" })
    assertTrue(afSlow.any { it.startsWith("afade=t=in") })

    val afFast = VideoEditorEngine.buildAudioFilterStages(state.copy(playbackSpeed = 3.0f))
    // 3.0x decomposes into atempo=2.0 followed by atempo=1.5000
    assertTrue(afFast.contains("atempo=2.0"))
    assertTrue(afFast.contains("atempo=1.5000"))
  }

  @Test
  fun buildFfmpegArguments_neverUsesMissingLibx264_andUsesStreamCopyOrHardwareEncoder() {
    val meta = VideoMetadata(
      file = File("/tmp/clip.mp4"),
      durationMs = 8_000L,
      width = 1280,
      height = 720,
    )
    val trimOnly = VideoEditState.initialForDuration(8_000L).copy(
      trimStartMs = 1_500L,
      trimEndMs = 5_500L,
    )

    val copyArgs = VideoEditorEngine.buildFfmpegArguments(
      inputPath = "/tmp/clip.mp4",
      outputPath = "/tmp/out.mp4",
      metadata = meta,
      editState = trimOnly,
      options = VideoExportOptions(),
      strategy = VideoProcessingStrategy.STREAM_COPY,
    )
    assertTrue(copyArgs.containsAll(listOf("-ss", "1.500", "-t", "4.000", "-c:v", "copy", "-c:a", "copy")))
    assertFalse(copyArgs.contains("libx264"))

    val hwArgs = VideoEditorEngine.buildFfmpegArguments(
      inputPath = "/tmp/clip.mp4",
      outputPath = "/tmp/out.mp4",
      metadata = meta,
      editState = trimOnly.copy(rotationDegrees = 180),
      options = VideoExportOptions(),
      strategy = VideoProcessingStrategy.HARDWARE_FFMPEG,
    )
    assertTrue(hwArgs.contains("h264_mediacodec"))
    assertFalse(hwArgs.contains("libx264"))
  }

  @Test
  fun exportVideo_fallsBackAutomaticallyWhenPrimaryStrategyFails_andBlocksMutedM4a() = runBlocking {
    val tempDir = Files.createTempDirectory("video_export_test").toFile()
    try {
      val src = File(tempDir, "source.mp4").apply { writeBytes(ByteArray(256) { 1 }) }
      val outDir = File(tempDir, "exports")
      val meta = VideoMetadata(file = src, durationMs = 5_000L, width = 1280, height = 720)
      val state = VideoEditState.initialForDuration(5_000L).copy(filterPreset = VideoFilterPreset.BW)

      val attemptedStrategies = mutableListOf<VideoProcessingStrategy>()
      val result = VideoEditorEngine.exportVideo(
        context = null,
        sourceFile = src,
        outputDir = outDir,
        desiredStem = "bw_export",
        metadata = meta,
        editState = state,
        options = VideoExportOptions(format = VideoExportFormat.MP4),
        ffmpegRunner = { _, targetFile, strategy ->
          attemptedStrategies.add(strategy)
          if (strategy == VideoProcessingStrategy.HARDWARE_FFMPEG) {
            // Simulate hardware codec busy/unavailable on first attempt
            false
          } else {
            targetFile.writeBytes(ByteArray(512) { 2 })
            true
          }
        },
      )

      assertTrue(result.success)
      assertTrue(result.usedFallback)
      assertEquals(VideoProcessingStrategy.SOFTWARE_FFMPEG, result.strategyUsed)
      assertEquals(
        listOf(VideoProcessingStrategy.HARDWARE_FFMPEG, VideoProcessingStrategy.SOFTWARE_FFMPEG),
        attemptedStrategies,
      )
      assertNotNull(result.outputFile)
      assertTrue(result.outputFile!!.exists())

      // Muted clip + Audio-only M4A export should fail fast with clear message
      val mutedM4a = VideoEditorEngine.exportVideo(
        context = null,
        sourceFile = src,
        outputDir = outDir,
        desiredStem = "muted_audio",
        metadata = meta,
        editState = state.copy(isMuted = true),
        options = VideoExportOptions(format = VideoExportFormat.M4A),
      )
      assertFalse(mutedM4a.success)
      assertNull(mutedM4a.outputFile)
      assertTrue(mutedM4a.message.contains("Mute Audio"))
    } finally {
      tempDir.deleteRecursively()
    }
  }
}
