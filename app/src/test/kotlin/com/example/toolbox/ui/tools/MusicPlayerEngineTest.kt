package com.example.toolbox.ui.tools

import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
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
    val entries = listOf(
      SavedPlaylistEntry(
        id = "playlist_a",
        name = "Neon Night Drive",
        trackIds = listOf("sample_neon_horizon", "sample_starlight_drive", "sample_cybernetic_pulse", "sample_crimson_velocity"),
        createdAtMs = 1_700_000_000_000L,
      ),
      SavedPlaylistEntry(
        id = "playlist_b",
        name = "Chill & Focus",
        trackIds = listOf("sample_autumn_coffee", "sample_blue_note_stroll"),
        createdAtMs = 1_700_000_100_000L,
      ),
    )
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
  fun favoritesPlaylist_isAlwaysBuiltAndFollowsFavoriteIds() {
    val tracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS
    val empty = MusicPlayerEngine.buildFavoritesPlaylist(emptySet(), tracks)
    assertEquals(MusicPlayerEngine.FAVORITES_PLAYLIST_NAME, empty.name)
    assertEquals(0, empty.songCount)
    assertTrue(empty.isFavorites)

    val favs = MusicPlayerEngine.buildFavoritesPlaylist(setOf(tracks[1].id, tracks[0].id), tracks)
    assertEquals(listOf(tracks[0].id, tracks[1].id), favs.tracks.map { it.id })

    assertTrue(MusicPlayerEngine.buildFavoritesPlaylist(emptySet(), emptyList()).tracks.isEmpty())
  }

  @Test
  fun mostPlayedPlaylist_andPlayStats_recordSerializeAndSortByCombinedPlaysAndPlaytime() {
    val tracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS
    val trackA = tracks[0] // Neon Horizon
    val trackB = tracks[1] // Starlight Drive
    val trackC = tracks[2] // Cybernetic Pulse

    var stats = emptyMap<String, TrackPlayStats>()
    // Track B played 3 times with 30s total listen time => score = 3 * 60_000 + 30_000 = 210_000
    stats = MusicPlayerEngine.recordPlayStat(stats, trackB.id, incrementPlayCount = true, addedPlayTimeMs = 10_000L, timestampMs = 100L)
    stats = MusicPlayerEngine.recordPlayStat(stats, trackB.id, incrementPlayCount = true, addedPlayTimeMs = 10_000L, timestampMs = 200L)
    stats = MusicPlayerEngine.recordPlayStat(stats, trackB.id, incrementPlayCount = true, addedPlayTimeMs = 10_000L, timestampMs = 300L)

    // Track C played 1 time but listened for 4 minutes (240_000 ms) => score = 1 * 60_000 + 240_000 = 300_000
    stats = MusicPlayerEngine.recordPlayStat(stats, trackC.id, incrementPlayCount = true, addedPlayTimeMs = 240_000L, timestampMs = 400L)

    // Track A played 1 time for 5s => score = 65_000
    stats = MusicPlayerEngine.recordPlayStat(stats, trackA.id, incrementPlayCount = true, addedPlayTimeMs = 5_000L, timestampMs = 500L)

    val mostPlayed = MusicPlayerEngine.buildMostPlayedPlaylist(stats, tracks)
    assertEquals(MusicPlayerEngine.MOST_PLAYED_PLAYLIST_NAME, mostPlayed.name)
    assertTrue(mostPlayed.isMostPlayed)
    assertTrue(mostPlayed.isBuiltIn)
    assertEquals(tracks.size, mostPlayed.songCount)
    assertEquals(listOf(trackC.id, trackB.id, trackA.id), mostPlayed.tracks.take(3).map { it.id })

    // Sort order MOST_PLAYED produces the same ranking
    val sortedByMostPlayed = MusicPlayerEngine.sortTracks(tracks, MusicSortOrder.MOST_PLAYED, stats)
    assertEquals(mostPlayed.tracks.map { it.id }, sortedByMostPlayed.map { it.id })

    // Serialization & deserialization round-trip
    val raw = MusicPlayerEngine.serializePlayStats(stats)
    val restored = MusicPlayerEngine.deserializePlayStats(raw)
    assertEquals(stats, restored)
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

    // Reordering queue items updates track positions and keeps currentQueueIndex pointed at the active track
    val sampleQueue = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS.take(4)
    val activeTrackId = sampleQueue[1].id
    val (reordered1, newCur1) = MusicPlayerEngine.reorderQueue(
      queue = sampleQueue,
      fromIndex = 1,
      toIndex = 3,
      currentQueueIndex = 1,
    )
    assertEquals(3, newCur1)
    assertEquals(activeTrackId, reordered1[newCur1].id)

    val (reordered2, newCur2) = MusicPlayerEngine.reorderQueue(
      queue = sampleQueue,
      fromIndex = 0,
      toIndex = 2,
      currentQueueIndex = 1,
    )
    assertEquals(0, newCur2)
    assertEquals(activeTrackId, reordered2[newCur2].id)
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

      // Default library loading scans the Music and Audio folders (ignoring Download)
      val musicDir = File(tempDir, "Music").apply { mkdirs() }
      val audioDir = File(tempDir, "Audio").apply { mkdirs() }
      val downloadDir = File(tempDir, "Download").apply { mkdirs() }
      wavFile.copyTo(File(musicDir, "MusicSong.wav"), overwrite = true)
      wavFile.copyTo(File(audioDir, "AudioRecording.wav"), overwrite = true)
      wavFile.copyTo(File(downloadDir, "DownloadedClip.wav"), overwrite = true)

      val libraryTracks = MusicPlayerEngine.loadLibraryTracks(
        rootDirectory = tempDir,
        initialDirectory = null,
        includeSamples = false,
        inspectMetadataRetriever = false,
      )
      assertEquals(2, libraryTracks.size)
      assertEquals(setOf("MusicSong", "AudioRecording"), libraryTracks.map { it.title }.toSet())
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

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun playbackSession_continuesPlayingAndAdvancesQueueInBackground() = runTest {
    val testDispatcher = StandardTestDispatcher(testScheduler)
    val sampleTracks = listOf(
      MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS[0].copy(durationMs = 2_000L),
      MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS[1].copy(durationMs = 2_000L),
    )
    val session = MusicPlaybackSession(
      context = null,
      initialTracks = sampleTracks,
      isInspection = false,
      coroutineContext = testDispatcher,
    )
    try {
      assertFalse(session.isPlaying)
      assertEquals(sampleTracks[0].id, session.currentTrack?.id)

      // Start playing first track
      session.playTrackInContext(sampleTracks[0], sampleTracks)
      assertTrue(session.isPlaying)
      assertEquals(0, session.currentQueueIndex)
      assertEquals(0L, session.currentPositionMs)
      assertEquals(1, session.playStats[sampleTracks[0].id]?.playCount)

      // Advance time in background (simulating switching away from Music tool)
      advanceTimeBy(1_000L)
      assertTrue(session.isPlaying)
      assertTrue(session.currentPositionMs > 0L)
      assertTrue((session.playStats[sampleTracks[0].id]?.totalPlayTimeMs ?: 0L) >= 800L)

      // Advance past the end of track 0 -> automatically steps to track 1 in background and keeps playing
      advanceTimeBy(1_500L)
      assertTrue(session.isPlaying)
      assertEquals(1, session.currentQueueIndex)
      assertEquals(sampleTracks[1].id, session.currentTrack?.id)
      assertEquals(1, session.playStats[sampleTracks[1].id]?.playCount)
    } finally {
      session.release()
    }
  }
}
