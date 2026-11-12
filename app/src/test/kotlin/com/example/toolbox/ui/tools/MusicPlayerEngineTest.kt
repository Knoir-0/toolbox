package com.example.toolbox.ui.tools

import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicPlayerEngineTest {

  @Test
  fun defaultSampleTracks_populateArtistsAlbumsSongsAndGenres() {
    val tracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS
    assertTrue(tracks.size >= 8)

    val artists = MusicPlayerEngine.buildArtists(tracks)
    val albums = MusicPlayerEngine.buildAlbums(tracks)
    val genres = MusicPlayerEngine.buildGenres(tracks)

    assertTrue(artists.isNotEmpty())
    assertTrue(albums.isNotEmpty())
    assertTrue(genres.isNotEmpty())

    // Aurora Synth has 2 albums (Midnight Odyssey & Solaris Grid) and 3 songs
    val aurora = artists.find { it.name == "Aurora Synth" }
    assertNotNull(aurora)
    assertEquals(2, aurora!!.albumCount)
    assertEquals(3, aurora.songCount)
    assertEquals("2 albums • 3 songs", aurora.summaryText)

    // Midnight Odyssey has 2 tracks
    val odyssey = albums.find { it.name == "Midnight Odyssey" }
    assertNotNull(odyssey)
    assertEquals("Aurora Synth", odyssey!!.artist)
    assertEquals(2, odyssey.songCount)
    assertEquals("Aurora Synth • 2 songs", odyssey.summaryText)

    // Jazz genre has 2 tracks and 1 artist
    val jazz = genres.find { it.name == "Jazz" }
    assertNotNull(jazz)
    assertEquals(2, jazz!!.songCount)
    assertEquals(1, jazz.artistCount)
    assertEquals("2 songs • 1 artist", jazz.summaryText)
  }

  @Test
  fun viewModeAndLibraryTab_parseAndToggleCorrectly() {
    assertEquals(MusicViewMode.LIST, MusicViewMode.DEFAULT)
    assertEquals(MusicViewMode.CARD, MusicViewMode.LIST.toggle())
    assertEquals(MusicViewMode.LIST, MusicViewMode.CARD.toggle())
    assertEquals(MusicViewMode.CARD, MusicViewMode.fromNameOrDefault("card"))
    assertEquals(MusicViewMode.LIST, MusicViewMode.fromNameOrDefault("invalid"))

    assertEquals(MusicLibraryTab.ARTISTS, MusicLibraryTab.fromNameOrDefault("ARTISTS"))
    assertEquals(MusicLibraryTab.ALBUMS, MusicLibraryTab.fromNameOrDefault("albums"))
    assertEquals(MusicLibraryTab.SONGS, MusicLibraryTab.fromNameOrDefault("SONGS"))
    assertEquals(MusicLibraryTab.GENRES, MusicLibraryTab.fromNameOrDefault("genres"))
    assertEquals(MusicLibraryTab.PLAYLISTS, MusicLibraryTab.fromNameOrDefault("playlists"))
  }

  @Test
  fun playlists_buildFilterSerializeAndDeserializeCorrectly() {
    val tracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS
    val entries = MusicPlayerEngine.DEFAULT_SAMPLE_PLAYLIST_ENTRIES
    val playlists = MusicPlayerEngine.buildPlaylists(entries, tracks)

    assertEquals(2, playlists.size)
    assertEquals("Neon Night Drive", playlists.first().name)
    assertEquals(4, playlists.first().songCount)

    val filtered = MusicPlayerEngine.filterPlaylists(playlists, "Chill")
    assertEquals(1, filtered.size)
    assertEquals("Chill & Focus", filtered.first().name)

    val serialized = MusicPlayerEngine.serializePlaylists(entries)
    val deserialized = MusicPlayerEngine.deserializePlaylists(serialized)
    assertEquals(entries, deserialized)
  }

  @Test
  fun resolveTrackPalette_producesDistinctBackgroundColorsFollowingAlbumArt() {
    val synthTrack = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS.first { it.genre == "Synthwave" }
    val jazzTrack = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS.first { it.genre == "Jazz" }
    val rockTrack = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS.first { it.genre == "Rock" }

    val synthPalette = MusicPlayerEngine.resolveTrackPalette(synthTrack)
    val jazzPalette = MusicPlayerEngine.resolveTrackPalette(jazzTrack)
    val rockPalette = MusicPlayerEngine.resolveTrackPalette(rockTrack)

    assertEquals("#00B4D8", synthPalette.dominantHex)
    assertEquals("#D97706", jazzPalette.dominantHex)
    assertEquals("#DC2626", rockPalette.dominantHex)

    assertNotEquals(synthPalette.dominantColor, jazzPalette.dominantColor)
    assertNotEquals(jazzPalette.dominantColor, rockPalette.dominantColor)
    assertNotEquals(synthPalette.deepBackgroundColor, jazzPalette.deepBackgroundColor)
    assertNotEquals(synthPalette.darkSurfaceColor, rockPalette.darkSurfaceColor)
  }

  @Test
  fun filterAndSort_filtersAcrossArtistsAlbumsSongsAndGenres() {
    val tracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS
    val artists = MusicPlayerEngine.buildArtists(tracks)
    val albums = MusicPlayerEngine.buildAlbums(tracks)
    val genres = MusicPlayerEngine.buildGenres(tracks)

    assertEquals(2, MusicPlayerEngine.filterTracks(tracks, "Velvet").size)
    assertEquals(1, MusicPlayerEngine.filterArtists(artists, "Sierra").size)
    assertEquals(1, MusicPlayerEngine.filterAlbums(albums, "Nocturnes").size)
    assertEquals(1, MusicPlayerEngine.filterGenres(genres, "Synthwave").size)

    val sortedAsc = MusicPlayerEngine.sortTracks(tracks, MusicSortOrder.TITLE_ASC)
    val sortedDesc = MusicPlayerEngine.sortTracks(tracks, MusicSortOrder.TITLE_DESC)
    assertEquals(sortedAsc.first().id, sortedDesc.last().id)

    val sortedLongest = MusicPlayerEngine.sortTracks(tracks, MusicSortOrder.DURATION_DESC)
    assertTrue(sortedLongest.first().durationMs >= sortedLongest.last().durationMs)
  }

  @Test
  fun queueNavigation_respectsRepeatAndShuffleAndPreviousThreshold() {
    // Repeat OFF at end of queue returns null when track finishes naturally, wraps on user click
    assertNull(
      MusicPlayerEngine.resolveNextTrackIndex(
        currentIndex = 4,
        queueSize = 5,
        repeatMode = MusicRepeatMode.OFF,
        shuffleEnabled = false,
        userInitiated = false,
      )
    )
    assertEquals(
      0,
      MusicPlayerEngine.resolveNextTrackIndex(
        currentIndex = 4,
        queueSize = 5,
        repeatMode = MusicRepeatMode.OFF,
        shuffleEnabled = false,
        userInitiated = true,
      )
    )

    // Repeat ONE stays on current track when track finishes naturally
    assertEquals(
      2,
      MusicPlayerEngine.resolveNextTrackIndex(
        currentIndex = 2,
        queueSize = 5,
        repeatMode = MusicRepeatMode.REPEAT_ONE,
        shuffleEnabled = false,
        userInitiated = false,
      )
    )

    // Previous restarts track when > 3 seconds into playback, steps back when <= 3 seconds
    assertEquals(
      2,
      MusicPlayerEngine.resolvePreviousTrackIndex(
        currentIndex = 2,
        queueSize = 5,
        currentPositionMs = 4_500L,
      )
    )
    assertEquals(
      1,
      MusicPlayerEngine.resolvePreviousTrackIndex(
        currentIndex = 2,
        queueSize = 5,
        currentPositionMs = 1_200L,
      )
    )
  }

  @Test
  fun ensurePlayableSampleWavFile_andScanMusicDirectories_workEndToEnd() {
    val tempDir = Files.createTempDirectory("music_engine_test").toFile()
    try {
      val sample = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS.first()
      val wavFile = MusicPlayerEngine.ensurePlayableSampleWavFile(tempDir, sample)
      assertTrue(wavFile.exists())
      assertTrue(wavFile.length() > 44L)
      assertTrue(MusicPlayerEngine.isMusicFile(wavFile))

      val customFile = File(tempDir, "Daft Punk - Digital Love.wav")
      wavFile.copyTo(customFile, overwrite = true)

      val scanned = MusicPlayerEngine.scanMusicDirectories(
        directories = listOf(tempDir),
        inspectMetadataRetriever = false,
      )
      val parsedCustom = scanned.find { it.file?.name == "Daft Punk - Digital Love.wav" }
      assertNotNull(parsedCustom)
      assertEquals("Daft Punk", parsedCustom!!.artist)
      assertEquals("Digital Love", parsedCustom.title)
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun deferredAlbumArt_andMetadataAndLibraryCaching_workInBackground() = runBlocking {
    val tempDir = Files.createTempDirectory("music_cache_test").toFile()
    try {
      MusicPlayerEngine.clearArtworkCache()
      MusicPlayerEngine.clearTrackMetadataCache()
      MusicPlayerEngine.configureDiskCache(tempDir)

      val albumDir = File(tempDir, "Music/Discovery").apply { mkdirs() }
      val sample = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS.first()
      val baseWav = MusicPlayerEngine.ensurePlayableSampleWavFile(tempDir, sample)

      val trackFile1 = File(albumDir, "Daft Punk - One More Time.wav")
      val trackFile2 = File(albumDir, "Daft Punk - Aerodynamic.wav")
      baseWav.copyTo(trackFile1, overwrite = true)
      baseWav.copyTo(trackFile2, overwrite = true)

      assertNull(MusicPlayerEngine.getCachedTrackMetadata(trackFile1))
      assertNull(MusicPlayerEngine.getCachedLibraryTracks(rootDirectory = tempDir, initialDirectory = albumDir, includeSamples = false))

      // Deferred scan populates track metadata & library caches without decoding bitmaps synchronously
      val loaded = MusicPlayerEngine.loadLibraryTracksAsync(
        rootDirectory = tempDir,
        initialDirectory = albumDir,
        includeSamples = false,
        inspectMetadataRetriever = true,
        deferAlbumArt = true,
      )
      assertEquals(2, loaded.size)
      assertNotNull(MusicPlayerEngine.getCachedTrackMetadata(trackFile1))
      assertNotNull(MusicPlayerEngine.getCachedLibraryTracks(rootDirectory = tempDir, initialDirectory = albumDir, includeSamples = false))

      // Both tracks in the same album share a single album art identity key and have not decoded bitmaps yet
      val t1 = loaded[0]
      val t2 = loaded[1]
      assertEquals(MusicPlayerEngine.albumArtIdentityKey(t1), MusicPlayerEngine.albumArtIdentityKey(t2))
      assertNull(MusicPlayerEngine.getCachedAlbumArtBitmap(t1, 240))
    } finally {
      MusicPlayerEngine.configureDiskCache(null)
      MusicPlayerEngine.clearArtworkCache()
      MusicPlayerEngine.clearTrackMetadataCache()
      tempDir.deleteRecursively()
    }
  }
}
