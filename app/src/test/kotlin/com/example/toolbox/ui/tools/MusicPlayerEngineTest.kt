package com.example.toolbox.ui.tools

import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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

    assertEquals(MusicLibraryTab.HOME, MusicLibraryTab.DEFAULT)
    assertEquals(MusicLibraryTab.HOME, MusicLibraryTab.fromNameOrDefault("HOME"))
    assertEquals(MusicLibraryTab.ARTISTS, MusicLibraryTab.fromNameOrDefault("ARTISTS"))
    assertEquals(MusicLibraryTab.ALBUMS, MusicLibraryTab.fromNameOrDefault("albums"))
    assertEquals(MusicLibraryTab.SONGS, MusicLibraryTab.fromNameOrDefault("SONGS"))
    assertEquals(MusicLibraryTab.GENRES, MusicLibraryTab.fromNameOrDefault("genres"))
    assertEquals(MusicLibraryTab.PLAYLISTS, MusicLibraryTab.fromNameOrDefault("playlists"))

    // Section tab order parsing, serialization, and reordering (HOME is fixed at the front of the pill list;
    // default order: Home, Playlists, Songs, Albums, Artists, Genres)
    assertEquals(
      listOf(
        MusicLibraryTab.HOME,
        MusicLibraryTab.PLAYLISTS,
        MusicLibraryTab.SONGS,
        MusicLibraryTab.ALBUMS,
        MusicLibraryTab.ARTISTS,
        MusicLibraryTab.GENRES,
      ),
      MusicPlayerEngine.parseTabOrder(null),
    )
    assertEquals(MusicLibraryTab.entries.toList(), MusicPlayerEngine.parseTabOrder(null))
    assertEquals(MusicLibraryTab.HOME, MusicPlayerEngine.parseTabOrder(null).first())
    assertEquals(
      MusicLibraryTab.HOME,
      MusicPlayerEngine.parseTabOrder("ARTISTS|ALBUMS|SONGS|GENRES|PLAYLISTS").first(),
    )
    assertEquals(
      MusicLibraryTab.HOME,
      MusicPlayerEngine.parseTabOrder("SONGS|HOME|PLAYLISTS|ALBUMS|ARTISTS|GENRES").first(),
    )
    // Attempting to move HOME (index 0) leaves the tab order unchanged
    assertEquals(
      MusicLibraryTab.entries.toList(),
      MusicPlayerEngine.reorderTabs(
        tabs = MusicLibraryTab.entries.toList(),
        fromIndex = 0,
        toIndex = 3,
      ),
    )
    // Moving SONGS (index 2) to the front (index 0) places it at index 1 right after HOME
    val reordered = MusicPlayerEngine.reorderTabs(
      tabs = MusicLibraryTab.entries.toList(),
      fromIndex = 2, // SONGS
      toIndex = 0,
    )
    assertEquals(
      listOf(
        MusicLibraryTab.HOME,
        MusicLibraryTab.SONGS,
        MusicLibraryTab.PLAYLISTS,
        MusicLibraryTab.ALBUMS,
        MusicLibraryTab.ARTISTS,
        MusicLibraryTab.GENRES,
      ),
      reordered,
    )
    val serializedOrder = MusicPlayerEngine.serializeTabOrder(reordered)
    assertEquals(reordered, MusicPlayerEngine.parseTabOrder(serializedOrder))
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

    // Built-in Recently Played playlist sorts by lastPlayedAtMs descending (trackA @ 500 > trackC @ 400 > trackB @ 300)
    val recentlyPlayed = MusicPlayerEngine.buildRecentlyPlayedPlaylist(stats, tracks)
    assertEquals(MusicPlayerEngine.RECENTLY_PLAYED_PLAYLIST_NAME, recentlyPlayed.name)
    assertTrue(recentlyPlayed.isRecentlyPlayed)
    assertTrue(recentlyPlayed.isBuiltIn)
    assertEquals(tracks.size, recentlyPlayed.songCount)
    assertEquals(listOf(trackA.id, trackC.id, trackB.id), recentlyPlayed.tracks.take(3).map { it.id })

    // Sort order RECENTLY_PLAYED produces the same ranking
    val sortedByRecentlyPlayed = MusicPlayerEngine.sortTracks(tracks, MusicSortOrder.RECENTLY_PLAYED, stats)
    assertEquals(recentlyPlayed.tracks.map { it.id }, sortedByRecentlyPlayed.map { it.id })

    // lastPlayedFormatted returns "Never" for unplayed stats and formatted date for played tracks
    assertEquals("Never", TrackPlayStats().lastPlayedFormatted)
    assertNotEquals("Never", stats.getValue(trackA.id).lastPlayedFormatted)

    // Light recency weight in Most Played: nudges a recently played track ahead of a nearly-tied older track,
    // without overpowering a track that has an additional play.
    val equalDurationTracks = listOf(
      trackA.copy(durationMs = 180_000L),
      trackB.copy(durationMs = 180_000L),
      trackC.copy(durationMs = 180_000L),
    )
    var recencyStats = emptyMap<String, TrackPlayStats>()
    val olderPlayedAt = 1_700_000_000_000L
    val recentPlayedAt = olderPlayedAt + 7 * 24 * 60 * 60_000L // 1 week later
    recencyStats = MusicPlayerEngine.recordPlayStat(
      current = recencyStats,
      trackId = trackA.id,
      incrementPlayCount = true,
      addedPlayTimeMs = 12_000L,
      timestampMs = olderPlayedAt,
    )
    recencyStats = MusicPlayerEngine.recordPlayStat(
      current = recencyStats,
      trackId = trackB.id,
      incrementPlayCount = true,
      addedPlayTimeMs = 10_000L,
      timestampMs = recentPlayedAt,
    )
    recencyStats = MusicPlayerEngine.recordPlayStat(
      current = recencyStats,
      trackId = trackC.id,
      incrementPlayCount = true,
      addedPlayTimeMs = 10_000L,
      timestampMs = olderPlayedAt,
    )
    recencyStats = MusicPlayerEngine.recordPlayStat(
      current = recencyStats,
      trackId = trackC.id,
      incrementPlayCount = true,
      addedPlayTimeMs = 10_000L,
      timestampMs = olderPlayedAt + 1_000L,
    )
    val weightedMostPlayed = MusicPlayerEngine.buildMostPlayedPlaylist(
      playStats = recencyStats,
      allTracks = equalDurationTracks,
    )
    // trackC (2 plays) > trackB (1 play, very recent) > trackA (1 play, 1 week older)
    assertEquals(listOf(trackC.id, trackB.id, trackA.id), weightedMostPlayed.tracks.map { it.id })

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

    assertEquals(MusicSortOrder.RANDOM, MusicSortOrder.fromNameOrDefault("RANDOM"))
    assertEquals("Random", MusicSortOrder.RANDOM.displayName)
    val sortedRandom = MusicPlayerEngine.sortTracks(tracks, MusicSortOrder.RANDOM, randomSeed = 42L)
    assertEquals(tracks.size, sortedRandom.size)
    assertEquals(tracks.map { it.id }.toSet(), sortedRandom.map { it.id }.toSet())
    assertNotEquals(tracks.map { it.id }, sortedRandom.map { it.id })
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

      // Default library loading scans Music, Audio, Recordings, and Download (plus plural Downloads folder)
      assertEquals(
        listOf("Music", "Audio", "Recordings", "Download"),
        DEFAULT_MUSIC_FOLDER_NAMES,
      )
      val musicDir = File(tempDir, "Music").apply { mkdirs() }
      val audioDir = File(tempDir, "Audio").apply { mkdirs() }
      val audioSubDir = File(audioDir, "Subfolder").apply { mkdirs() }
      val recordingsDir = File(tempDir, "Recordings").apply { mkdirs() }
      val recordingsSubDir = File(recordingsDir, "Meetings").apply { mkdirs() }
      val downloadsDir = File(tempDir, "Download").apply { mkdirs() }
      val ignoredDir = File(tempDir, "Documents").apply { mkdirs() }
      wavFile.copyTo(File(musicDir, "MusicSong.wav"), overwrite = true)
      wavFile.copyTo(File(audioDir, "Artist One - AudioRecording.wav"), overwrite = true)
      wavFile.copyTo(File(audioSubDir, "Artist Two - NestedAudioClip.wav"), overwrite = true)
      wavFile.copyTo(File(recordingsDir, "Speaker A - VoiceNote1.wav"), overwrite = true)
      wavFile.copyTo(File(recordingsSubDir, "Speaker B - VoiceNote2.wav"), overwrite = true)
      wavFile.copyTo(File(downloadsDir, "DownloadedClip.wav"), overwrite = true)
      wavFile.copyTo(File(ignoredDir, "IgnoredDocAudio.wav"), overwrite = true)

      val libraryTracks = MusicPlayerEngine.loadLibraryTracks(
        rootDirectory = tempDir,
        initialDirectory = null,
        includeSamples = false,
        inspectMetadataRetriever = false,
      )
      assertEquals(6, libraryTracks.size)
      assertEquals(
        setOf("MusicSong", "AudioRecording", "NestedAudioClip", "VoiceNote1", "VoiceNote2", "DownloadedClip"),
        libraryTracks.map { it.title }.toSet(),
      )

      // Audio and Recordings folders (including nested subfolders and mixed artists) are each treated as a single album
      val audioTracks = libraryTracks.filter { it.title in setOf("AudioRecording", "NestedAudioClip") }
      assertEquals(2, audioTracks.size)
      assertTrue(audioTracks.all { it.album == "Audio" })

      val recordingTracks = libraryTracks.filter { it.title in setOf("VoiceNote1", "VoiceNote2") }
      assertEquals(2, recordingTracks.size)
      assertTrue(recordingTracks.all { it.album == "Recordings" })

      val builtAlbums = MusicPlayerEngine.buildAlbums(libraryTracks)
      val audioAlbums = builtAlbums.filter { it.name == "Audio" }
      val recordingAlbums = builtAlbums.filter { it.name == "Recordings" }
      assertEquals(1, audioAlbums.size)
      assertEquals(2, audioAlbums.single().tracks.size)
      assertEquals("Various Artists", audioAlbums.single().artist)
      assertTrue(MusicPlayerEngine.matchesAlbumKey(audioAlbums.single(), "Audio|Artist One"))
      assertTrue(MusicPlayerEngine.matchesAlbumKey(audioAlbums.single(), "Audio|Various Artists"))

      assertEquals(1, recordingAlbums.size)
      assertEquals(2, recordingAlbums.single().tracks.size)
      assertEquals("Various Artists", recordingAlbums.single().artist)
      assertTrue(MusicPlayerEngine.matchesAlbumKey(recordingAlbums.single(), "Recordings|Speaker A"))
      assertTrue(MusicPlayerEngine.matchesAlbumKey(recordingAlbums.single(), "Recordings|Various Artists"))
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
      assertNull(session.currentTrack)

      // Start playing first track
      session.playTrackInContext(sampleTracks[0], sampleTracks)
      assertTrue(session.isPlaying)
      assertEquals(sampleTracks[0].id, session.currentTrack?.id)
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

  @Test
  fun resolveFocusedSectionAndQueueIndices_locatesCurrentlyPlayingTrackAcrossSectionsAndQueue() {
    val tracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS
    val sortedTracks = MusicPlayerEngine.sortTracks(tracks, MusicSortOrder.TITLE_ASC)
    val artists = MusicPlayerEngine.buildArtists(tracks)
    val albums = MusicPlayerEngine.buildAlbums(tracks)
    val genres = MusicPlayerEngine.buildGenres(tracks)

    val activeTrack = tracks.first { it.id == "sample_autumn_coffee" } // "Autumn Leaves & Coffee" by Velvet Trio

    val artistIndex = MusicPlayerEngine.resolveFocusedArtistIndex(
      artists = artists,
      currentTrack = activeTrack,
      currentArtistName = activeTrack.artist,
    )
    assertTrue(artistIndex >= 0)
    assertEquals("Velvet Trio", artists[artistIndex].name)

    val albumIndex = MusicPlayerEngine.resolveFocusedAlbumIndex(
      albums = albums,
      currentTrack = activeTrack,
      currentAlbumName = activeTrack.album,
      currentArtistName = activeTrack.artist,
    )
    assertTrue(albumIndex >= 0)
    assertEquals("Late Night Sessions", albums[albumIndex].name)

    val genreIndex = MusicPlayerEngine.resolveFocusedGenreIndex(
      genres = genres,
      currentTrack = activeTrack,
      currentGenreName = activeTrack.genre,
    )
    assertTrue(genreIndex >= 0)
    assertEquals("Jazz", genres[genreIndex].name)

    val songIndex = MusicPlayerEngine.resolveFocusedTrackIndex(
      tracks = sortedTracks,
      currentTrackId = activeTrack.id,
    )
    assertEquals(sortedTracks.indexOfFirst { it.id == activeTrack.id }, songIndex)
    assertEquals("sample_autumn_coffee", sortedTracks[songIndex].id)

    val favoritesPlaylist = MusicPlayerEngine.buildFavoritesPlaylist(setOf(activeTrack.id), tracks)
    val mostPlayedPlaylist = MusicPlayerEngine.buildMostPlayedPlaylist(emptyMap(), tracks)
    val userPlaylists = MusicPlayerEngine.buildPlaylists(
      entries = listOf(
        SavedPlaylistEntry(
          id = "custom_late_night",
          name = "Late Night",
          trackIds = listOf(activeTrack.id),
          createdAtMs = 1_700_000_000_000L,
        )
      ),
      allTracks = tracks,
    )
    val playlists = listOf(favoritesPlaylist, mostPlayedPlaylist) + userPlaylists

    // User playlist containing the track is prioritized over built-in playlists
    val playlistIndex = MusicPlayerEngine.resolveFocusedPlaylistIndex(
      playlists = playlists,
      currentTrackId = activeTrack.id,
    )
    assertEquals("custom_late_night", playlists[playlistIndex].id)

    // Queue focus resolves the active queue index and falls back to matching track ID if index is out of bounds
    val expectedQueueIdx = tracks.indexOfFirst { it.id == activeTrack.id }
    val queueIndex = MusicPlayerEngine.resolveFocusedQueueIndex(
      queue = tracks,
      currentQueueIndex = expectedQueueIdx,
      currentTrackId = activeTrack.id,
    )
    assertEquals(expectedQueueIdx, queueIndex)
    assertEquals(
      expectedQueueIdx,
      MusicPlayerEngine.resolveFocusedQueueIndex(
        queue = tracks,
        currentQueueIndex = -1,
        currentTrackId = activeTrack.id,
      )
    )
  }

  @Test
  fun playStats_calculatedFullPlaysAndSkips_integrateIntoMostPlayedSortWithLightSkipTieBreaker() {
    val shortTrack = MusicTrack(
      id = "short_track",
      title = "Short Anthem",
      artist = "Artist A",
      album = "Album A",
      genre = "Indie",
      durationMs = 30_000L, // 30s duration
    )
    val longTrack = MusicTrack(
      id = "long_track",
      title = "Long Epic",
      artist = "Artist B",
      album = "Album B",
      genre = "Indie",
      durationMs = 120_000L, // 2m duration
    )
    val tieTrackLowSkips = MusicTrack(
      id = "tie_low_skips",
      title = "Zebra Song",
      artist = "Artist C",
      album = "Album C",
      genre = "Indie",
      durationMs = 60_000L,
    )
    val tieTrackHighSkips = MusicTrack(
      id = "tie_high_skips",
      title = "Alpha Song",
      artist = "Artist C",
      album = "Album C",
      genre = "Indie",
      durationMs = 60_000L,
    )

    var stats = emptyMap<String, TrackPlayStats>()

    // Both shortTrack and longTrack have 2 plays and 60,000ms listen time:
    // shortTrack completed 2.0 full plays (60_000 / 30_000), while longTrack completed 0.5 full plays (60_000 / 120_000).
    stats = MusicPlayerEngine.recordPlayStat(
      current = stats,
      trackId = shortTrack.id,
      incrementPlayCount = true,
      addedPlayTimeMs = 30_000L,
      timestampMs = 100L,
    )
    stats = MusicPlayerEngine.recordPlayStat(
      current = stats,
      trackId = shortTrack.id,
      incrementPlayCount = true,
      addedPlayTimeMs = 30_000L,
      timestampMs = 200L,
    )

    stats = MusicPlayerEngine.recordPlayStat(
      current = stats,
      trackId = longTrack.id,
      incrementPlayCount = true,
      addedPlayTimeMs = 30_000L,
      timestampMs = 100L,
    )
    stats = MusicPlayerEngine.recordPlayStat(
      current = stats,
      trackId = longTrack.id,
      incrementPlayCount = true,
      addedPlayTimeMs = 30_000L,
      timestampMs = 200L,
    )

    val shortStat = stats.getValue(shortTrack.id)
    val longStat = stats.getValue(longTrack.id)
    assertEquals(2.0, shortStat.calculatedFullPlays(shortTrack.durationMs), 0.001)
    assertEquals("2", shortStat.formattedFullPlays(shortTrack.durationMs))
    assertEquals(0.5, longStat.calculatedFullPlays(longTrack.durationMs), 0.001)
    assertEquals("0.5", longStat.formattedFullPlays(longTrack.durationMs))

    // Even if shortTrack has several skips, its higher calculated full plays (2.0 vs 0.5) dominates
    repeat(3) {
      stats = MusicPlayerEngine.recordPlayStat(
        current = stats,
        trackId = shortTrack.id,
        incrementSkipCount = true,
        timestampMs = 250L,
      )
    }
    assertEquals(3, stats.getValue(shortTrack.id).skipCount)

    // Two tied tracks with identical play count (1), playtime (30_000ms), and duration (60_000ms => 0.5 full plays),
    // except tieTrackHighSkips was skipped twice and tieTrackLowSkips was skipped once.
    stats = MusicPlayerEngine.recordPlayStat(
      current = stats,
      trackId = tieTrackLowSkips.id,
      incrementPlayCount = true,
      addedPlayTimeMs = 30_000L,
      incrementSkipCount = true,
      timestampMs = 300L,
    )
    stats = MusicPlayerEngine.recordPlayStat(
      current = stats,
      trackId = tieTrackHighSkips.id,
      incrementPlayCount = true,
      addedPlayTimeMs = 30_000L,
      incrementSkipCount = true,
      timestampMs = 300L,
    )
    stats = MusicPlayerEngine.recordPlayStat(
      current = stats,
      trackId = tieTrackHighSkips.id,
      incrementSkipCount = true,
      timestampMs = 300L,
    )

    val mostPlayed = MusicPlayerEngine.buildMostPlayedPlaylist(
      playStats = stats,
      allTracks = listOf(longTrack, tieTrackHighSkips, shortTrack, tieTrackLowSkips),
    )
    // Order: shortTrack (2 full plays) > longTrack (2 plays, 0.5 full plays) > tieTrackLowSkips (1 skip) > tieTrackHighSkips (2 skips)
    assertEquals(
      listOf(shortTrack.id, longTrack.id, tieTrackLowSkips.id, tieTrackHighSkips.id),
      mostPlayed.tracks.map { it.id },
    )

    // Serialization preserves skipCount and remains backward-compatible with 4-column legacy format
    val serialized = MusicPlayerEngine.serializePlayStats(stats)
    val deserialized = MusicPlayerEngine.deserializePlayStats(serialized)
    assertEquals(stats, deserialized)

    val legacyLine = "legacy_id\t2\t45000\t1700000000"
    val legacyRestored = MusicPlayerEngine.deserializePlayStats(legacyLine)
    assertEquals(0, legacyRestored.getValue("legacy_id").skipCount)
    assertEquals(2, legacyRestored.getValue("legacy_id").playCount)
  }

  @Test
  fun shuffleQueue_andSmartShuffle_reorderQueueAndWeightMostPlayedTowardsTop() {
    assertEquals(MusicShuffleMode.SHUFFLE, MusicShuffleMode.OFF.next())
    assertEquals(MusicShuffleMode.SMART_SHUFFLE, MusicShuffleMode.SHUFFLE.next())
    assertEquals(MusicShuffleMode.OFF, MusicShuffleMode.SMART_SHUFFLE.next())

    val tracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS
    val activeTrack = tracks[3] // Index 3 in unshuffled queue
    val deterministicRandom = kotlin.random.Random(42)

    // 1. Standard SHUFFLE pins activeTrack at index 0 and shuffles the remaining tracks in the queue
    val (shuffledQueue, shuffledIdx) = MusicPlayerEngine.applyShuffleModeToQueue(
      baseQueue = tracks,
      currentQueue = tracks,
      anchorTrackId = activeTrack.id,
      mode = MusicShuffleMode.SHUFFLE,
      random = deterministicRandom,
    )
    assertEquals(0, shuffledIdx)
    assertEquals(activeTrack.id, shuffledQueue.first().id)
    assertEquals(tracks.size, shuffledQueue.size)
    assertEquals(tracks.map { it.id }.toSet(), shuffledQueue.map { it.id }.toSet())
    assertNotEquals(tracks.map { it.id }, shuffledQueue.map { it.id })

    // Next track progression steps sequentially through the shuffled queue (0 -> 1 -> 2)
    assertEquals(
      1,
      MusicPlayerEngine.resolveNextTrackIndex(
        currentIndex = 0,
        queueSize = shuffledQueue.size,
        repeatMode = MusicRepeatMode.OFF,
        shuffleEnabled = true,
        userInitiated = true,
      )
    )
    assertEquals(
      2,
      MusicPlayerEngine.resolveNextTrackIndex(
        currentIndex = 1,
        queueSize = shuffledQueue.size,
        repeatMode = MusicRepeatMode.OFF,
        shuffleEnabled = true,
        userInitiated = false,
      )
    )

    // 2. Restoring OFF returns the original unshuffled queue and restores activeTrack's original index (3)
    val (restoredQueue, restoredIdx) = MusicPlayerEngine.applyShuffleModeToQueue(
      baseQueue = tracks,
      currentQueue = shuffledQueue,
      anchorTrackId = activeTrack.id,
      mode = MusicShuffleMode.OFF,
    )
    assertEquals(3, restoredIdx)
    assertEquals(tracks.map { it.id }, restoredQueue.map { it.id })

    // 3. SMART_SHUFFLE weights the most played songs more towards the top of a shuffle
    val favoriteHeavyTrack = tracks.last()
    val mediumPlayedTrack = tracks[tracks.size - 2]
    var stats = emptyMap<String, TrackPlayStats>()
    repeat(15) {
      stats = MusicPlayerEngine.recordPlayStat(
        current = stats,
        trackId = favoriteHeavyTrack.id,
        incrementPlayCount = true,
        addedPlayTimeMs = favoriteHeavyTrack.durationMs,
        timestampMs = 1_000L + it,
      )
    }
    repeat(5) {
      stats = MusicPlayerEngine.recordPlayStat(
        current = stats,
        trackId = mediumPlayedTrack.id,
        incrementPlayCount = true,
        addedPlayTimeMs = mediumPlayedTrack.durationMs,
        timestampMs = 2_000L + it,
      )
    }

    val heavyWeight = MusicPlayerEngine.smartShuffleWeight(favoriteHeavyTrack, stats)
    val mediumWeight = MusicPlayerEngine.smartShuffleWeight(mediumPlayedTrack, stats)
    val unplayedWeight = MusicPlayerEngine.smartShuffleWeight(tracks.first(), stats)
    assertTrue(heavyWeight > mediumWeight)
    assertTrue(mediumWeight > unplayedWeight)

    val rng = kotlin.random.Random(12345)
    val trials = 200
    var heavyIndexSum = 0
    var mediumIndexSum = 0
    var unplayedIndexSum = 0
    val unplayedReference = tracks[1]

    repeat(trials) {
      val (smartQueue, smartIdx) = MusicPlayerEngine.applyShuffleModeToQueue(
        baseQueue = tracks,
        currentQueue = tracks,
        anchorTrackId = activeTrack.id,
        mode = MusicShuffleMode.SMART_SHUFFLE,
        playStats = stats,
        random = rng,
      )
      assertEquals(0, smartIdx)
      assertEquals(activeTrack.id, smartQueue.first().id)
      heavyIndexSum += smartQueue.indexOfFirst { it.id == favoriteHeavyTrack.id }
      mediumIndexSum += smartQueue.indexOfFirst { it.id == mediumPlayedTrack.id }
      unplayedIndexSum += smartQueue.indexOfFirst { it.id == unplayedReference.id }
    }

    val avgHeavyRank = heavyIndexSum.toDouble() / trials
    val avgMediumRank = mediumIndexSum.toDouble() / trials
    val avgUnplayedRank = unplayedIndexSum.toDouble() / trials
    // Heavily played track appears much closer to the top (smaller index) than medium and unplayed tracks
    assertTrue("Expected avgHeavyRank ($avgHeavyRank) < avgMediumRank ($avgMediumRank)", avgHeavyRank < avgMediumRank)
    assertTrue("Expected avgMediumRank ($avgMediumRank) < avgUnplayedRank ($avgUnplayedRank)", avgMediumRank < avgUnplayedRank)

    // 4. SleepTimerPreset cycles through Off -> 10 min -> 15 min -> 20 min -> 30 min -> 60 min -> Off
    assertEquals(
      listOf(
        SleepTimerPreset.OFF,
        SleepTimerPreset.TEN_MIN,
        SleepTimerPreset.FIFTEEN_MIN,
        SleepTimerPreset.TWENTY_MIN,
        SleepTimerPreset.THIRTY_MIN,
        SleepTimerPreset.SIXTY_MIN,
      ),
      SleepTimerPreset.entries,
    )
    assertEquals(10 * 60_000L, SleepTimerPreset.TEN_MIN.durationMs)
    assertEquals(15 * 60_000L, SleepTimerPreset.FIFTEEN_MIN.durationMs)
    assertEquals(20 * 60_000L, SleepTimerPreset.TWENTY_MIN.durationMs)
    assertEquals(30 * 60_000L, SleepTimerPreset.THIRTY_MIN.durationMs)
    assertEquals(60 * 60_000L, SleepTimerPreset.SIXTY_MIN.durationMs)
    assertEquals(SleepTimerPreset.TEN_MIN, SleepTimerPreset.OFF.next())
    assertEquals(SleepTimerPreset.FIFTEEN_MIN, SleepTimerPreset.TEN_MIN.next())
    assertEquals(SleepTimerPreset.TWENTY_MIN, SleepTimerPreset.FIFTEEN_MIN.next())
    assertEquals(SleepTimerPreset.THIRTY_MIN, SleepTimerPreset.TWENTY_MIN.next())
    assertEquals(SleepTimerPreset.SIXTY_MIN, SleepTimerPreset.THIRTY_MIN.next())
    assertEquals(SleepTimerPreset.OFF, SleepTimerPreset.SIXTY_MIN.next())
  }

  @Test
  fun favoriteArtistsAlbumsAndGenres_toggleAndSerialize_andDoNotModifyDefaultFavoritesPlaylist() {
    val tracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS
    val artists = MusicPlayerEngine.buildArtists(tracks)
    val albums = MusicPlayerEngine.buildAlbums(tracks)
    val genres = MusicPlayerEngine.buildGenres(tracks)

    val artist = artists.first()
    val album = albums.first()
    val genre = genres.first()

    // 1. Toggle artist favorite
    var favoriteArtists = emptySet<String>()
    assertFalse(MusicPlayerEngine.isArtistFavorite(artist, favoriteArtists))
    favoriteArtists = MusicPlayerEngine.toggleFavoriteArtist(favoriteArtists, artist)
    assertTrue(MusicPlayerEngine.isArtistFavorite(artist, favoriteArtists))
    assertTrue(MusicPlayerEngine.isArtistFavorite(artist.name, favoriteArtists))
    favoriteArtists = MusicPlayerEngine.toggleFavoriteArtist(favoriteArtists, artist.name)
    assertFalse(MusicPlayerEngine.isArtistFavorite(artist, favoriteArtists))

    // 2. Toggle album favorite (uses name|artist composite key)
    var favoriteAlbums = emptySet<String>()
    assertFalse(MusicPlayerEngine.isAlbumFavorite(album, favoriteAlbums))
    favoriteAlbums = MusicPlayerEngine.toggleFavoriteAlbum(favoriteAlbums, album)
    assertTrue(MusicPlayerEngine.isAlbumFavorite(album, favoriteAlbums))
    assertTrue(MusicPlayerEngine.isAlbumFavorite(album.name, album.artist, favoriteAlbums))
    assertEquals("${album.name}|${album.artist}", album.key)
    // Round-trip serialization preserves "|" inside album keys
    val serializedAlbums = MusicPlayerEngine.serializeFavoriteSet(favoriteAlbums)
    val deserializedAlbums = MusicPlayerEngine.deserializeFavoriteSet(serializedAlbums)
    assertEquals(favoriteAlbums, deserializedAlbums)
    favoriteAlbums = MusicPlayerEngine.toggleFavoriteAlbum(favoriteAlbums, album.name, album.artist)
    assertFalse(MusicPlayerEngine.isAlbumFavorite(album, favoriteAlbums))

    // 3. Toggle genre favorite
    var favoriteGenres = emptySet<String>()
    assertFalse(MusicPlayerEngine.isGenreFavorite(genre, favoriteGenres))
    favoriteGenres = MusicPlayerEngine.toggleFavoriteGenre(favoriteGenres, genre)
    assertTrue(MusicPlayerEngine.isGenreFavorite(genre, favoriteGenres))
    assertTrue(MusicPlayerEngine.isGenreFavorite(genre.name, favoriteGenres))
    favoriteGenres = MusicPlayerEngine.toggleFavoriteGenre(favoriteGenres, genre.name)
    assertFalse(MusicPlayerEngine.isGenreFavorite(genre, favoriteGenres))

    // 4. Favoriting artists, albums, and genres in MusicPlaybackSession does NOT add tracks to the default Favorites playlist
    val session = MusicPlaybackSession(context = null, initialTracks = tracks)
    session.toggleFavoriteArtist(artist.name)
    session.toggleFavoriteAlbum(album)
    session.toggleFavoriteGenre(genre.name)

    assertTrue(session.isArtistFavorite(artist.name))
    assertTrue(session.isAlbumFavorite(album))
    assertTrue(session.isGenreFavorite(genre.name))
    assertTrue(session.favoriteIds.isEmpty())

    val favoritesPlaylist = MusicPlayerEngine.buildFavoritesPlaylist(session.favoriteIds, tracks)
    assertTrue(favoritesPlaylist.tracks.isEmpty())

    // Only explicitly favorited songs appear in the default Favorites playlist
    val explicitSong = tracks.last()
    session.toggleFavorite(explicitSong.id)
    val updatedFavoritesPlaylist = MusicPlayerEngine.buildFavoritesPlaylist(session.favoriteIds, tracks)
    assertEquals(listOf(explicitSong.id), updatedFavoritesPlaylist.tracks.map { it.id })

    // 5. Per-tab Favorites filter state is independent across Artists, Albums, Songs, and Genres
    assertFalse(session.showFavoriteArtistsOnly)
    assertFalse(session.showFavoriteAlbumsOnly)
    assertFalse(session.showFavoriteSongsOnly)
    assertFalse(session.showFavoriteGenresOnly)

    session.toggleFavoritesOnly(MusicLibraryTab.ARTISTS)
    assertTrue(session.showFavoriteArtistsOnly)
    assertFalse(session.showFavoriteAlbumsOnly)
    assertFalse(session.showFavoriteSongsOnly)
    assertFalse(session.showFavoriteGenresOnly)

    session.toggleFavoritesOnly(MusicLibraryTab.ALBUMS)
    session.toggleFavoritesOnly(MusicLibraryTab.GENRES)
    assertTrue(session.showFavoriteArtistsOnly)
    assertTrue(session.showFavoriteAlbumsOnly)
    assertFalse(session.showFavoriteSongsOnly)
    assertTrue(session.showFavoriteGenresOnly)

    session.release()

    // 6. Favoriting a song, artist, album, or genre cancels out the skip statistic weighting in Most Played
    val trackLowSkips = MusicTrack(
      id = "low_skips",
      title = "Beta Track",
      artist = "Artist One",
      album = "Album One",
      genre = "Genre One",
      durationMs = 60_000L,
    )
    val trackHighSkips = MusicTrack(
      id = "high_skips",
      title = "Alpha Track",
      artist = "Artist Two",
      album = "Album Two",
      genre = "Genre Two",
      durationMs = 60_000L,
    )
    val playStats = mapOf(
      trackLowSkips.id to TrackPlayStats(playCount = 2, totalPlayTimeMs = 60_000L, lastPlayedAtMs = 1_000L, skipCount = 1),
      trackHighSkips.id to TrackPlayStats(playCount = 2, totalPlayTimeMs = 60_000L, lastPlayedAtMs = 1_000L, skipCount = 10),
    )

    // Without favorites, trackLowSkips ranks higher than trackHighSkips due to fewer skips
    val unfavoredOrder = MusicPlayerEngine.buildMostPlayedPlaylist(
      playStats = playStats,
      allTracks = listOf(trackLowSkips, trackHighSkips),
    ).tracks.map { it.id }
    assertEquals(listOf(trackLowSkips.id, trackHighSkips.id), unfavoredOrder)

    // When trackHighSkips is favorited by song, artist, album, or genre, its skip penalty is cancelled out
    assertEquals(
      listOf(trackHighSkips.id, trackLowSkips.id),
      MusicPlayerEngine.buildMostPlayedPlaylist(
        playStats = playStats,
        allTracks = listOf(trackLowSkips, trackHighSkips),
        favoriteIds = setOf(trackHighSkips.id),
      ).tracks.map { it.id },
    )
    assertEquals(
      listOf(trackHighSkips.id, trackLowSkips.id),
      MusicPlayerEngine.buildMostPlayedPlaylist(
        playStats = playStats,
        allTracks = listOf(trackLowSkips, trackHighSkips),
        favoriteArtists = setOf(trackHighSkips.artist),
      ).tracks.map { it.id },
    )
    assertEquals(
      listOf(trackHighSkips.id, trackLowSkips.id),
      MusicPlayerEngine.buildMostPlayedPlaylist(
        playStats = playStats,
        allTracks = listOf(trackLowSkips, trackHighSkips),
        favoriteAlbums = setOf("${trackHighSkips.album}|${trackHighSkips.artist}"),
      ).tracks.map { it.id },
    )
    assertEquals(
      listOf(trackHighSkips.id, trackLowSkips.id),
      MusicPlayerEngine.buildMostPlayedPlaylist(
        playStats = playStats,
        allTracks = listOf(trackLowSkips, trackHighSkips),
        favoriteGenres = setOf(trackHighSkips.genre),
      ).tracks.map { it.id },
    )
  }

  @Test
  fun playbackSession_contextMenuQueuePlaylistNavigationAndFileActions_workCorrectly() {
    val tracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS
    val session = MusicPlaybackSession(context = null, initialTracks = tracks)
    try {
      val t0 = tracks[0]
      val t1 = tracks[1]
      val t2 = tracks[2]
      val t3 = tracks[3]

      // Start playing t0..t1
      session.playTrackInContext(t0, listOf(t0, t1))
      assertEquals(listOf(t0.id, t1.id), session.playbackQueue.map { it.id })
      assertEquals(0, session.currentQueueIndex)

      // Play Next inserts t2 right after current track (index 1)
      session.playNext(listOf(t2))
      assertEquals(listOf(t0.id, t2.id, t1.id), session.playbackQueue.map { it.id })

      // Add to Queue appends t3 at the end
      session.addToQueue(listOf(t3))
      assertEquals(listOf(t0.id, t2.id, t1.id, t3.id), session.playbackQueue.map { it.id })

      // Create playlist, add tracks, remove track, rename playlist
      session.saveQueueAsPlaylist("Road Trip", listOf(t0, t1))
      val created = session.savedPlaylistEntries.firstOrNull { it.name == "Road Trip" }
      assertNotNull(created)
      val playlistId = created!!.id
      val addedOk = session.addTracksToPlaylist(playlistId, listOf(t1, t2))
      assertTrue(addedOk)
      assertEquals(listOf(t0.id, t1.id, t2.id), session.savedPlaylistEntries.first { it.id == playlistId }.trackIds)

      val removedOk = session.removeTrackFromPlaylist(playlistId, t1.id)
      assertTrue(removedOk)
      assertEquals(listOf(t0.id, t2.id), session.savedPlaylistEntries.first { it.id == playlistId }.trackIds)

      val renamedOk = session.renamePlaylist(playlistId, "Evening Drive")
      assertTrue(renamedOk)
      assertEquals("Evening Drive", session.savedPlaylistEntries.first { it.id == playlistId }.name)

      // Navigation helpers
      session.navigateToArtist(t0.artist)
      assertEquals(MusicLibraryTab.ARTISTS, session.selectedTab)
      assertEquals(t0.artist, session.selectedArtistName)

      val albumKey = "${t0.album}|${t0.artist}"
      session.navigateToAlbum(albumKey)
      assertEquals(MusicLibraryTab.ALBUMS, session.selectedTab)
      assertEquals(albumKey, session.selectedAlbumKey)

      // Duplicate, rename, and delete track
      val initialCount = session.allTracks.size
      val duplicated = session.duplicateTrack(t0)
      assertEquals(initialCount + 1, session.allTracks.size)
      assertTrue(duplicated.title.contains(t0.title))

      val renamedTrack = session.renameTrack(duplicated, "Neon Horizon Remix")
      assertNotNull(renamedTrack)
      assertEquals("Neon Horizon Remix", renamedTrack!!.title)
      assertTrue(session.allTracks.any { it.title == "Neon Horizon Remix" })

      session.deleteTrack(renamedTrack)
      assertEquals(initialCount, session.allTracks.size)
      assertFalse(session.allTracks.any { it.id == renamedTrack.id })

      // Cards per row is bounded to 2..3 (max 3 per row)
      assertEquals(2, MIN_MUSIC_CARDS_PER_ROW)
      assertEquals(3, MAX_MUSIC_CARDS_PER_ROW)
      assertEquals(2, session.cardsPerRow)
      session.updateCardsPerRow(3)
      assertEquals(3, session.cardsPerRow)
      session.updateCardsPerRow(4)
      assertEquals(3, session.cardsPerRow)
      session.updateCardsPerRow(1)
      assertEquals(2, session.cardsPerRow)

      // Home playlists generate 6 mixes and regenerate on refreshHomePlaylists()
      val initialHome = session.homePlaylists
      assertEquals(6, initialHome.size)
      assertTrue(initialHome.all { it.isGeneratedHomeMix && it.tracks.isNotEmpty() })
      val refreshedHome = session.refreshHomePlaylists()
      assertEquals(6, refreshedHome.size)
      assertEquals(1, session.homeRefreshGeneration)
      assertNotEquals(initialHome.map { it.name }, refreshedHome.map { it.name })
    } finally {
      session.release()
    }
  }

  @Test
  fun generateHomePlaylists_createsSixMixesUsingGenreMashupsPlayStatsAndFavorites_targetingOneHour() {
    // Build a 30-track library (~4 min per song = 2 hours total) across similar genres
    val genres = listOf("Synthwave", "Electronic", "Jazz", "Acoustic", "Rock", "Classical")
    val longTracks = (1..30).map { idx ->
      val genre = genres[(idx - 1) % genres.size]
      MusicTrack(
        id = "track_$idx",
        title = "Song $idx",
        artist = "Artist ${(idx - 1) % 6 + 1}",
        album = "Album ${(idx - 1) % 6 + 1}",
        genre = genre,
        durationMs = 240_000L, // 4 minutes each
        trackNumber = idx,
      )
    }

    var stats = emptyMap<String, TrackPlayStats>()
    stats = MusicPlayerEngine.recordPlayStat(stats, "track_1", incrementPlayCount = true, addedPlayTimeMs = 480_000L, timestampMs = 1_000L)
    stats = MusicPlayerEngine.recordPlayStat(stats, "track_2", incrementPlayCount = true, addedPlayTimeMs = 240_000L, timestampMs = 2_000L)
    stats = MusicPlayerEngine.recordPlayStat(stats, "track_7", incrementPlayCount = true, addedPlayTimeMs = 720_000L, timestampMs = 3_000L)

    val favSongs = setOf("track_3", "track_9")
    val favArtists = setOf("Artist 2")
    val favAlbums = setOf("Album 4|Artist 4")
    val favGenres = setOf("Synthwave")

    val mixesGen0 = MusicPlayerEngine.generateHomePlaylists(
      allTracks = longTracks,
      playStats = stats,
      favoriteIds = favSongs,
      favoriteArtists = favArtists,
      favoriteAlbums = favAlbums,
      favoriteGenres = favGenres,
      refreshGeneration = 0,
    )

    assertEquals(6, mixesGen0.size)
    // Verify each playlist targets ~1 hour (3,600,000 ms, within ~45..70 min)
    for (mix in mixesGen0) {
      assertTrue("Expected non-empty mix ${mix.name}", mix.tracks.isNotEmpty())
      assertTrue(
        "Expected ~1 hr duration for ${mix.name}, got ${mix.totalDurationMs}",
        mix.totalDurationMs in (45 * 60_000L)..(70 * 60_000L),
      )
    }

    // Mix 1 mashes similar genres together (e.g. Synthwave + Electronic), Mix 2 is Old Favorites
    assertTrue(MusicPlayerEngine.areGenresSimilar("Synthwave", "Electronic"))
    assertTrue(mixesGen0[0].subtitle.contains("Genre Mashup"))
    assertEquals("Old Favorites", mixesGen0[1].name)
    assertTrue(mixesGen0[1].subtitle.contains("Old Favorites"))
    // track_1 (in favorite genre Synthwave, played a lot at oldest timestamp 1_000L) and track_2 (by favorite Artist 2, played at 2_000L)
    // rank at the very top of Old Favorites ahead of recently-played or unplayed tracks
    assertEquals("track_1", mixesGen0[1].tracks.first().id)

    // Mix 3 uses play stats
    assertTrue(mixesGen0[2].subtitle.contains("Play Stats"))
    assertTrue(mixesGen0[2].tracks.any { it.id == "track_7" || it.id == "track_1" })

    // Mix 4 uses favorites (songs, artists, albums, genres)
    assertTrue(mixesGen0[3].subtitle.contains("Favorites"))
    assertTrue(mixesGen0[3].tracks.any { it.id in favSongs || it.artist in favArtists })

    // Refreshing (generation 1) generates a new set of 6 playlists
    val mixesGen1 = MusicPlayerEngine.generateHomePlaylists(
      allTracks = longTracks,
      playStats = stats,
      favoriteIds = favSongs,
      favoriteArtists = favArtists,
      favoriteAlbums = favAlbums,
      favoriteGenres = favGenres,
      refreshGeneration = 1,
    )
    assertEquals(6, mixesGen1.size)
    assertNotEquals(mixesGen0.map { it.name }, mixesGen1.map { it.name })
  }

  @Test
  fun playbackSession_renameAndDeleteArtistAlbumGenre_andProtectedPlaylists_workCorrectly() {
    val tracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS
    val session = MusicPlaybackSession(context = null, initialTracks = tracks)
    try {
      // 1. Default and Home playlists are protected from deletion and renaming
      assertTrue(MusicPlayerEngine.isProtectedPlaylistId(MusicPlayerEngine.FAVORITES_PLAYLIST_ID))
      assertTrue(MusicPlayerEngine.isProtectedPlaylistId(MusicPlayerEngine.MOST_PLAYED_PLAYLIST_ID))
      assertTrue(MusicPlayerEngine.isProtectedPlaylistId(MusicPlayerEngine.RECENTLY_PLAYED_PLAYLIST_ID))
      for (mix in session.homePlaylists) {
        assertTrue(MusicPlayerEngine.isProtectedPlaylistId(mix.id))
        assertFalse(session.deletePlaylist(mix.id))
        assertFalse(session.renamePlaylist(mix.id, "Custom Name"))
      }
      assertFalse(session.deletePlaylist(MusicPlayerEngine.FAVORITES_PLAYLIST_ID))
      assertFalse(session.deletePlaylist(MusicPlayerEngine.MOST_PLAYED_PLAYLIST_ID))
      assertFalse(session.deletePlaylist(MusicPlayerEngine.RECENTLY_PLAYED_PLAYLIST_ID))

      // 2. Rename entire Artist ("Aurora Synth" -> "Aurora Prime")
      session.toggleFavoriteArtist("Aurora Synth")
      assertTrue(session.isArtistFavorite("Aurora Synth"))
      val renamedArtistTracks = session.renameArtist("Aurora Synth", "Aurora Prime")
      assertEquals(3, renamedArtistTracks)
      assertFalse(session.allTracks.any { it.artist == "Aurora Synth" })
      assertEquals(3, session.allTracks.count { it.artist == "Aurora Prime" })
      assertTrue(session.isArtistFavorite("Aurora Prime"))
      assertFalse(session.isArtistFavorite("Aurora Synth"))

      // 3. Rename entire Album ("Midnight Odyssey" -> "Dawn Odyssey")
      session.toggleFavoriteAlbum("Midnight Odyssey", "Aurora Prime")
      assertTrue(session.isAlbumFavorite("Midnight Odyssey", "Aurora Prime"))
      val renamedAlbumTracks = session.renameAlbum("Midnight Odyssey", "Aurora Prime", "Dawn Odyssey")
      assertEquals(2, renamedAlbumTracks)
      assertFalse(session.allTracks.any { it.album == "Midnight Odyssey" })
      assertEquals(2, session.allTracks.count { it.album == "Dawn Odyssey" })
      assertTrue(session.isAlbumFavorite("Dawn Odyssey", "Aurora Prime"))

      // 4. Rename entire Genre ("Jazz" -> "Smooth Jazz")
      session.toggleFavoriteGenre("Jazz")
      assertTrue(session.isGenreFavorite("Jazz"))
      val renamedGenreTracks = session.renameGenre("Jazz", "Smooth Jazz")
      assertEquals(2, renamedGenreTracks)
      assertFalse(session.allTracks.any { it.genre == "Jazz" })
      assertEquals(2, session.allTracks.count { it.genre == "Smooth Jazz" })
      assertTrue(session.isGenreFavorite("Smooth Jazz"))

      // 5. Delete entire Album ("Dawn Odyssey" -> removes 2 tracks)
      val deletedAlbumTracks = session.deleteAlbum("Dawn Odyssey", "Aurora Prime")
      assertEquals(2, deletedAlbumTracks)
      assertFalse(session.allTracks.any { it.album == "Dawn Odyssey" })
      assertFalse(session.isAlbumFavorite("Dawn Odyssey", "Aurora Prime"))

      // 6. Delete entire Genre ("Smooth Jazz" -> removes 2 tracks)
      val deletedGenreTracks = session.deleteGenre("Smooth Jazz")
      assertEquals(2, deletedGenreTracks)
      assertFalse(session.allTracks.any { it.genre == "Smooth Jazz" })
      assertFalse(session.isGenreFavorite("Smooth Jazz"))

      // 7. Delete entire Artist ("Sierra Pines" -> removes 2 tracks)
      val deletedArtistTracks = session.deleteArtist("Sierra Pines")
      assertEquals(2, deletedArtistTracks)
      assertFalse(session.allTracks.any { it.artist == "Sierra Pines" })
    } finally {
      session.release()
    }
  }

  @Test
  fun diskIndexCache_andFrozenViewPlayStatsDuringPlayback_workCorrectly() {
    val tmpDir = Files.createTempDirectory("music_index_cache_test").toFile()
    val indexFile = File(tmpDir, MusicPlayerEngine.MUSIC_INDEX_CACHE_FILE_NAME)
    val musicDir = File(tmpDir, "Music").apply { mkdirs() }
    val baseSample = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS.first()
    val baseWav = MusicPlayerEngine.ensurePlayableSampleWavFile(tmpDir, baseSample)
    val wavFile = File(musicDir, "Aurora Synth - Cached Anthem.wav")
    baseWav.copyTo(wavFile, overwrite = true)
    try {
      MusicPlayerEngine.clearTrackMetadataCache()
      val customTrack = MusicPlayerEngine.buildTrackFromFile(
        file = wavFile,
        inspectMetadataRetriever = false,
      ).copy(
        title = "Cached Anthem Custom Title",
        artist = "Aurora Synth",
        album = "Cached Album",
        genre = "Synthwave",
        durationMs = 245_000L,
      )
      MusicPlayerEngine.putCachedTrackIndex(customTrack)
      MusicPlayerEngine.saveTrackIndexCacheToDisk(indexFile, listOf(customTrack))
      assertTrue(indexFile.exists())

      // Clear in-memory cache and reload from disk -> custom metadata is restored without re-parsing
      MusicPlayerEngine.clearTrackMetadataCache()
      assertNull(MusicPlayerEngine.getCachedTrackIndex(wavFile))
      val loadedMap = MusicPlayerEngine.loadTrackIndexCacheFromDisk(indexFile)
      assertEquals(1, loadedMap.size)
      val restored = MusicPlayerEngine.buildTrackFromFile(wavFile, inspectMetadataRetriever = true)
      assertEquals("Cached Anthem Custom Title", restored.title)
      assertEquals("Cached Album", restored.album)
      assertEquals(245_000L, restored.durationMs)

      // Verify viewPlayStats and homePlayStats remain frozen while music is actively playing
      val tracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS
      val session = MusicPlaybackSession(context = null, initialTracks = tracks)
      try {
        assertTrue(session.viewPlayStats.isEmpty())
        assertTrue(session.homePlayStats.isEmpty())
        val initialHomeMixTracks = session.homePlaylists.map { it.tracks.map { t -> t.id } }

        // Start playing track 0 -> playStats updates immediately, but viewPlayStats and homePlayStats stay frozen while playing
        session.playTrackInContext(tracks[0], tracks)
        assertTrue(session.isPlaying)
        assertEquals(1, session.playStats[tracks[0].id]?.playCount)
        assertNull(session.viewPlayStats[tracks[0].id])
        assertNull(session.homePlayStats[tracks[0].id])

        // Simulate playback ticks -> playStats accumulates time, viewPlayStats & homePlaylists remain unchanged
        session.recordTrackPlayStats(tracks[0].id, addedPlayTimeMs = 5_000L)
        assertEquals(5_000L, session.playStats[tracks[0].id]?.totalPlayTimeMs)
        assertNull(session.viewPlayStats[tracks[0].id])
        assertEquals(initialHomeMixTracks, session.homePlaylists.map { it.tracks.map { t -> t.id } })

        // Switching tabs or changing sort order snapshots the latest playStats into viewPlayStats
        session.updateTab(MusicLibraryTab.PLAYLISTS)
        assertEquals(1, session.viewPlayStats[tracks[0].id]?.playCount)
        assertEquals(5_000L, session.viewPlayStats[tracks[0].id]?.totalPlayTimeMs)
        // Home play stats remain frozen until refreshHomePlaylists() is called
        assertNull(session.homePlayStats[tracks[0].id])
        session.refreshHomePlaylists()
        assertEquals(1, session.homePlayStats[tracks[0].id]?.playCount)
      } finally {
        session.release()
      }
    } finally {
      MusicPlayerEngine.clearTrackMetadataCache()
      tmpDir.deleteRecursively()
    }
  }

  @Test
  fun bluetoothDisconnect_andSystemMediaControls_pauseAndControlPlaybackCorrectly() {
    // 1. Verify Bluetooth audio device type and broadcast disconnect classification
    assertTrue(MusicPlayerEngine.isBluetoothAudioDeviceType(android.media.AudioDeviceInfo.TYPE_BLUETOOTH_A2DP))
    assertTrue(MusicPlayerEngine.isBluetoothAudioDeviceType(android.media.AudioDeviceInfo.TYPE_BLUETOOTH_SCO))
    assertTrue(MusicPlayerEngine.isBluetoothAudioDeviceType(26)) // TYPE_BLE_HEADSET
    assertFalse(MusicPlayerEngine.isBluetoothAudioDeviceType(android.media.AudioDeviceInfo.TYPE_BUILTIN_SPEAKER))

    assertTrue(
      MusicPlayerEngine.shouldPauseOnAudioDisconnectBroadcast(
        action = android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY,
      )
    )
    assertTrue(
      MusicPlayerEngine.shouldPauseOnAudioDisconnectBroadcast(
        action = android.bluetooth.BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED,
        bluetoothState = android.bluetooth.BluetoothProfile.STATE_DISCONNECTED,
        previousBluetoothState = android.bluetooth.BluetoothProfile.STATE_CONNECTED,
      )
    )
    assertTrue(
      MusicPlayerEngine.shouldPauseOnAudioDisconnectBroadcast(
        action = android.bluetooth.BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED,
        bluetoothState = android.bluetooth.BluetoothProfile.STATE_DISCONNECTING,
        previousBluetoothState = android.bluetooth.BluetoothProfile.STATE_CONNECTED,
      )
    )
    assertTrue(
      MusicPlayerEngine.shouldPauseOnAudioDisconnectBroadcast(
        action = android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED,
      )
    )
    assertFalse(
      MusicPlayerEngine.shouldPauseOnAudioDisconnectBroadcast(
        action = android.bluetooth.BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED,
        bluetoothState = android.bluetooth.BluetoothProfile.STATE_CONNECTED,
        previousBluetoothState = android.bluetooth.BluetoothProfile.STATE_CONNECTING,
      )
    )

    val tracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS
    val session = MusicPlaybackSession(context = null, initialTracks = tracks)
    try {
      // Initially no track is loaded -> system media control state is inactive
      assertFalse(session.systemMediaControlState.isActive)
      assertFalse(session.onBluetoothHeadphonesDisconnected())
      assertEquals(0, session.bluetoothDisconnectPauseCount)

      // 2. System media Play action starts playback on first library track when queue is empty
      assertTrue(session.handleMediaControlAction(MusicPlayerEngine.ACTION_MEDIA_PLAY))
      assertTrue(session.isPlaying)
      assertEquals(tracks[0].id, session.currentTrack?.id)
      assertTrue(session.systemMediaControlState.isActive)
      assertTrue(session.systemMediaControlState.isPlaying)
      assertEquals(tracks[0].title, session.systemMediaControlState.title)
      assertEquals(tracks[0].artist, session.systemMediaControlState.artist)
      assertEquals(tracks[0].album, session.systemMediaControlState.album)

      // 3. Disconnecting Bluetooth headphones while playing pauses playback immediately
      assertTrue(session.onBluetoothHeadphonesDisconnected())
      assertFalse(session.isPlaying)
      assertFalse(session.systemMediaControlState.isPlaying)
      assertEquals(1, session.bluetoothDisconnectPauseCount)

      // A second disconnect while already paused is a no-op
      assertFalse(session.onBluetoothHeadphonesDisconnected())
      assertEquals(1, session.bluetoothDisconnectPauseCount)

      // 4. Hardware / Bluetooth AVRCP media key events and system media actions control playback
      assertTrue(session.handleMediaKeyEvent(android.view.KeyEvent.KEYCODE_MEDIA_PLAY))
      assertTrue(session.isPlaying)
      assertTrue(session.systemMediaControlState.isPlaying)

      assertTrue(session.handleMediaKeyEvent(android.view.KeyEvent.KEYCODE_MEDIA_NEXT))
      assertEquals(tracks[1].id, session.currentTrack?.id)
      assertEquals(tracks[1].title, session.systemMediaControlState.title)

      // Seek and playback speed updates propagate to system media state
      session.seekToPositionMs(6_500L)
      assertEquals(6_500L, session.currentPositionMs)
      assertEquals(6_500L, session.systemMediaControlState.positionMs)

      session.setPlaybackSpeedValue(1.5f)
      assertEquals(1.5f, session.playbackSpeed, 0.001f)
      assertEquals(1.5f, session.systemMediaControlState.playbackSpeed, 0.001f)

      // Toggle favorite via system media control custom action (strictly for the individual song, not artist/album)
      session.toggleFavoriteArtist(tracks[1].artist)
      session.toggleFavoriteAlbum(tracks[1].album, tracks[1].artist)
      assertFalse(session.systemMediaControlState.isFavorite)
      assertEquals(
        com.example.toolbox.R.drawable.ic_media_favorite_off,
        session.systemMediaControlState.favoriteIconRes,
      )

      assertTrue(session.handleMediaControlAction(MusicPlayerEngine.ACTION_MEDIA_TOGGLE_FAVORITE))
      assertTrue(tracks[1].id in session.favoriteIds)
      assertTrue(session.systemMediaControlState.isFavorite)
      assertEquals(
        com.example.toolbox.R.drawable.ic_media_favorite_on,
        session.systemMediaControlState.favoriteIconRes,
      )

      // Shuffle (left) and Star / Favorite (right) buttons flank playback controls and reflect state in icons
      assertEquals(
        listOf(
          MusicPlayerEngine.ACTION_MEDIA_CYCLE_SHUFFLE,
          MusicPlayerEngine.ACTION_MEDIA_TOGGLE_FAVORITE,
        ),
        session.systemMediaControlState.customActionIds,
      )
      assertEquals(MusicShuffleMode.OFF, session.systemMediaControlState.shuffleMode)
      assertEquals(
        com.example.toolbox.R.drawable.ic_media_shuffle_off,
        session.systemMediaControlState.shuffleIconRes,
      )
      assertTrue(session.handleMediaControlAction(MusicPlayerEngine.ACTION_MEDIA_CYCLE_SHUFFLE))
      assertEquals(MusicShuffleMode.SHUFFLE, session.systemMediaControlState.shuffleMode)
      assertEquals(
        com.example.toolbox.R.drawable.ic_media_shuffle_on,
        session.systemMediaControlState.shuffleIconRes,
      )
      assertTrue(session.handleMediaControlAction(MusicPlayerEngine.ACTION_MEDIA_CYCLE_SHUFFLE))
      assertEquals(MusicShuffleMode.SMART_SHUFFLE, session.systemMediaControlState.shuffleMode)
      assertEquals(
        com.example.toolbox.R.drawable.ic_media_shuffle_smart,
        session.systemMediaControlState.shuffleIconRes,
      )
      assertTrue(session.handleMediaControlAction(MusicPlayerEngine.ACTION_MEDIA_CYCLE_SHUFFLE))
      assertEquals(MusicShuffleMode.OFF, session.systemMediaControlState.shuffleMode)
      assertEquals(
        com.example.toolbox.R.drawable.ic_media_shuffle_off,
        session.systemMediaControlState.shuffleIconRes,
      )

      assertEquals(MusicRepeatMode.OFF, session.systemMediaControlState.repeatMode)
      assertEquals(
        com.example.toolbox.R.drawable.ic_media_repeat_off,
        session.systemMediaControlState.repeatIconRes,
      )
      assertTrue(session.handleMediaControlAction(MusicPlayerEngine.ACTION_MEDIA_CYCLE_REPEAT))
      assertEquals(MusicRepeatMode.REPEAT_ALL, session.systemMediaControlState.repeatMode)
      assertEquals(
        com.example.toolbox.R.drawable.ic_media_repeat_all,
        session.systemMediaControlState.repeatIconRes,
      )
      assertTrue(session.handleMediaControlAction(MusicPlayerEngine.ACTION_MEDIA_CYCLE_REPEAT))
      assertEquals(MusicRepeatMode.REPEAT_ONE, session.systemMediaControlState.repeatMode)
      assertEquals(
        com.example.toolbox.R.drawable.ic_media_repeat_one,
        session.systemMediaControlState.repeatIconRes,
      )
      assertTrue(session.handleMediaControlAction(MusicPlayerEngine.ACTION_MEDIA_CYCLE_REPEAT))
      assertEquals(MusicRepeatMode.OFF, session.systemMediaControlState.repeatMode)
      assertEquals(
        com.example.toolbox.R.drawable.ic_media_repeat_off,
        session.systemMediaControlState.repeatIconRes,
      )

      // Expanding full player from system media player launch keeps playback active and opens full player view
      assertFalse(session.isPlayerExpanded)
      session.expandFullPlayer()
      assertTrue(session.isPlayerExpanded)
      assertTrue(session.isPlaying)

      // Pause via media button
      assertTrue(session.handleMediaKeyEvent(android.view.KeyEvent.KEYCODE_MEDIA_PAUSE))
      assertFalse(session.isPlaying)
      assertFalse(session.systemMediaControlState.isPlaying)

      // 5. External app playing in Android system media player drives activeTrackPalette and isMusicPlaying
      val externalInfo = MusicPlayerEngine.updateExternalSystemMediaFromMetadata(
        title = "Midnight City",
        artist = "M83",
        album = "Hurry Up, We're Dreaming",
        sourcePackage = "com.spotify.music",
        sourceAppName = "Spotify",
        isPlaying = true,
        notificationColorRgb = 0x1DB954,
      )
      assertTrue(MusicPlayerEngine.isMusicPlaying)
      assertEquals(externalInfo.palette.dominantHex, MusicPlayerEngine.activeTrackPalette.dominantHex)
      assertEquals("#1DB954", MusicPlayerEngine.activeTrackPalette.dominantHex)
      assertEquals("Midnight City", MusicPlayerEngine.activeSystemMediaInfo?.title)
      assertEquals("Spotify", MusicPlayerEngine.activeSystemMediaInfo?.sourceAppName)
      assertTrue(MusicPlayerEngine.activeSystemMediaInfo?.isExternalApp == true)

      // Pausing external media stops isMusicPlaying, while activeTrackPalette and activeSystemMediaInfo
      // still reflect the external media session over an unplayed/paused internal track
      MusicPlayerEngine.updateExternalSystemMediaSession(externalInfo.copy(isPlaying = false))
      assertFalse(MusicPlayerEngine.isMusicPlaying)
      assertEquals("#1DB954", MusicPlayerEngine.activeTrackPalette.dominantHex)
      assertEquals("Midnight City", MusicPlayerEngine.activeSystemMediaInfo?.title)
      assertTrue(MusicPlayerEngine.activeSystemMediaInfo?.isExternalApp == true)

      // Active playback states (PLAYING, BUFFERING, SKIPPING_TO_NEXT, CONNECTING, etc.) are recognized
      assertTrue(MusicPlayerEngine.isPlaybackStateActive(android.media.session.PlaybackState.STATE_PLAYING))
      assertTrue(MusicPlayerEngine.isPlaybackStateActive(android.media.session.PlaybackState.STATE_BUFFERING))
      assertTrue(MusicPlayerEngine.isPlaybackStateActive(android.media.session.PlaybackState.STATE_SKIPPING_TO_NEXT))
      assertTrue(MusicPlayerEngine.isPlaybackStateActive(android.media.session.PlaybackState.STATE_CONNECTING))
      assertFalse(MusicPlayerEngine.isPlaybackStateActive(android.media.session.PlaybackState.STATE_PAUSED))
      assertFalse(MusicPlayerEngine.isPlaybackStateActive(android.media.session.PlaybackState.STATE_STOPPED))

      // 6. External system media player transport controls (Play/Pause, Seek, Next, Previous)
      val controllableInfo = MusicPlayerEngine.updateExternalSystemMediaFromMetadata(
        title = "Starboy",
        artist = "The Weeknd",
        album = "Starboy",
        genre = "Synth-Pop",
        sourcePackage = "com.spotify.music",
        sourceAppName = "Spotify",
        isPlaying = true,
        notificationColorRgb = 0xE50914,
        durationMs = 230_000L,
        positionMs = 45_000L,
        positionUpdateElapsedRealtimeMs = 10_000L,
      )
      assertEquals(230_000L, controllableInfo.durationMs)
      assertEquals(45_000L, controllableInfo.positionMs)
      assertTrue(controllableInfo.canSeek)
      assertEquals(50_000L, MusicPlayerEngine.currentExternalSystemMediaPositionMs(15_000L))

      // Toggle Play/Pause pauses external session
      assertTrue(MusicPlayerEngine.toggleExternalSystemMediaPlayPause())
      assertEquals("PAUSE", MusicPlayerEngine.externalSystemMediaLastDispatchedAction)
      assertFalse(MusicPlayerEngine.externalSystemMediaInfo?.isPlaying == true)

      // Toggle Play/Pause resumes external session
      assertTrue(MusicPlayerEngine.toggleExternalSystemMediaPlayPause())
      assertEquals("PLAY", MusicPlayerEngine.externalSystemMediaLastDispatchedAction)
      assertTrue(MusicPlayerEngine.externalSystemMediaInfo?.isPlaying == true)

      // Seek updates external position
      assertTrue(MusicPlayerEngine.seekExternalSystemMediaTo(120_000L))
      assertEquals("SEEK:120000", MusicPlayerEngine.externalSystemMediaLastDispatchedAction)
      assertEquals(120_000L, MusicPlayerEngine.externalSystemMediaInfo?.positionMs)

      // Skip Next and Skip Previous reset position and record action
      assertTrue(MusicPlayerEngine.skipExternalSystemMediaToNext())
      assertEquals("NEXT", MusicPlayerEngine.externalSystemMediaLastDispatchedAction)
      assertEquals(0L, MusicPlayerEngine.externalSystemMediaInfo?.positionMs)

      assertTrue(MusicPlayerEngine.skipExternalSystemMediaToPrevious())
      assertEquals("PREVIOUS", MusicPlayerEngine.externalSystemMediaLastDispatchedAction)

      // 7. Resolving external media source package (directly from sourcePackage or fallback from sourceAppName)
      assertEquals("com.spotify.music", MusicPlayerEngine.resolveExternalSystemMediaPackage())
      MusicPlayerEngine.updateExternalSystemMediaFromMetadata(
        title = "Veridis Quo",
        artist = "Daft Punk",
        album = "Discovery",
        genre = "French House",
        sourcePackage = "",
        sourceAppName = "YouTube Music",
        isPlaying = true,
      )
      assertEquals(
        "com.google.android.apps.youtube.music",
        MusicPlayerEngine.resolveExternalSystemMediaPackage(),
      )
    } finally {
      MusicPlayerEngine.updateExternalSystemMediaSession(null)
      session.release()
    }
  }

  @Test
  fun starringSongUpdatesFavoriteIdsImmediatelyAndRecalculatesPlaylistsInBackground() = kotlinx.coroutines.runBlocking {
    val tracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS
    val session = MusicPlaybackSession(
      context = null,
      isInspection = true,
      initialTracks = tracks,
    )
    try {
      val firstTrack = tracks[0]
      val secondTrack = tracks[1]
      assertFalse(firstTrack.id in session.favoriteIds)
      assertTrue(session.favoritesPlaylist.tracks.isEmpty())

      // Star first song: favoriteIds updates immediately, and background task updates playlists
      session.toggleFavorite(firstTrack.id)
      assertTrue(firstTrack.id in session.favoriteIds)
      session.awaitBackgroundRecalculations()

      assertEquals(listOf(firstTrack.id), session.favoritesPlaylist.tracks.map { it.id })
      assertTrue(session.favoritesPlaylist.containsTrack(firstTrack.id))
      assertFalse(session.favoritesPlaylist.containsTrack(secondTrack.id))
      assertEquals(
        listOf(firstTrack.id),
        session.basePlaylists.first { it.isFavorites }.tracks.map { it.id },
      )
      assertEquals(6, session.homePlaylists.size)

      // Star second song and unstar first song
      session.toggleFavorite(secondTrack.id)
      session.toggleFavorite(firstTrack.id)
      assertFalse(firstTrack.id in session.favoriteIds)
      assertTrue(secondTrack.id in session.favoriteIds)
      session.awaitBackgroundRecalculations()

      assertEquals(listOf(secondTrack.id), session.favoritesPlaylist.tracks.map { it.id })
      assertTrue(session.favoritesPlaylist.containsTrack(secondTrack.id))
      assertFalse(session.favoritesPlaylist.containsTrack(firstTrack.id))
    } finally {
      session.release()
    }
  }

  @Test
  fun preferredMusicApps_supportsInstalledStreamingCatalogCustomLinksAndSerialization() {
    try {
      // Without installed packages on a null context, only installed streaming apps are listed (empty)
      assertTrue(MusicPlayerEngine.discoverPopularStreamingApps(context = null).isEmpty())

      // When streaming apps are installed, only the installed ones are returned
      MusicPlayerEngine.installedStreamingAppsOverrideForTest =
        MusicPlayerEngine.POPULAR_STREAMING_MUSIC_APPS.filter { it.id in setOf("spotify", "youtube_music", "tidal") }

      val installedPopular = MusicPlayerEngine.discoverPopularStreamingApps(context = null)
      assertEquals(listOf("spotify", "youtube_music", "tidal"), installedPopular.map { it.id })
      assertTrue(installedPopular.all { it.isInstalled })

      val custom = MusicPlayerEngine.createCustomPreferredMusicAppLink(
        label = "Plexamp",
        packageOrUrl = "tv.plex.labs.plexamp",
      )
      assertTrue(custom.isCustom)
      assertEquals("Plexamp", custom.label)
      assertEquals("tv.plex.labs.plexamp", custom.packageName)

      val serialized = MusicPlayerEngine.serializePreferredMusicApps(
        listOf(installedPopular.first(), custom)
      )
      val deserialized = MusicPlayerEngine.deserializePreferredMusicApps(serialized)
      assertEquals(2, deserialized.size)
      assertEquals(installedPopular.first().id, deserialized[0].id)
      assertEquals(custom.id, deserialized[1].id)
      assertEquals("Plexamp", deserialized[1].label)
      assertTrue(deserialized[1].isCustom)
    } finally {
      MusicPlayerEngine.installedStreamingAppsOverrideForTest = null
    }
  }

  @Test
  fun scopedStorage_customFolders_andPerSectionSavedSortsWorkCorrectly() = kotlinx.coroutines.runBlocking {
    val tracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS
    val session = MusicPlaybackSession(
      context = null,
      isInspection = true,
      initialTracks = tracks,
      initialTab = MusicLibraryTab.HOME,
      initialGrantedFolderPaths = emptySet(),
    )
    try {
      // 1. Scoped storage initially locked when /Music permission is not granted
      assertFalse(session.isMusicFolderGranted)
      assertNull(session.pendingPermissionFolderPath)

      // Tapping a navigation tab while locked requests /Music permission prompt instead of switching tabs
      session.updateTab(MusicLibraryTab.SONGS)
      assertEquals(MusicLibraryTab.HOME, session.selectedTab)
      assertEquals("/Music", session.pendingPermissionFolderPath)

      // Granting /Music unlocks navigation
      session.grantFolderAccess(MusicPlayerEngine.DEFAULT_MUSIC_SCOPED_FOLDER)
      assertTrue(session.isMusicFolderGranted)
      assertNull(session.pendingPermissionFolderPath)

      // 2. Default sort on Home is RANDOM and "Save for home" is pre-checked
      assertEquals(MusicLibraryTab.HOME, session.selectedTab)
      assertEquals(MusicSortOrder.RANDOM, session.sortOrder)
      assertTrue(session.isSortSavedForCurrentScope)
      assertEquals("Save for home", session.currentSortSaveLabel)

      // Switching to Songs falls back to global default sort (TITLE_ASC) with "Save for songs" unchecked
      session.updateTab(MusicLibraryTab.SONGS)
      assertEquals(MusicLibraryTab.SONGS, session.selectedTab)
      assertEquals(MusicSortOrder.TITLE_ASC, session.sortOrder)
      assertFalse(session.isSortSavedForCurrentScope)
      assertEquals("Save for songs", session.currentSortSaveLabel)

      // Check "Save for songs" and set sort to DURATION_DESC
      session.setSaveSortForCurrentScope(true)
      assertTrue(session.isSortSavedForCurrentScope)
      session.updateSortOrder(MusicSortOrder.DURATION_DESC)
      assertEquals(MusicSortOrder.DURATION_DESC, session.sortOrder)

      // Check other section labels: playlists, albums, artists, genre
      session.updateTab(MusicLibraryTab.PLAYLISTS)
      assertEquals("Save for playlists", session.currentSortSaveLabel)
      assertEquals(MusicSortOrder.TITLE_ASC, session.sortOrder)

      session.updateTab(MusicLibraryTab.ALBUMS)
      assertEquals("Save for albums", session.currentSortSaveLabel)

      session.updateTab(MusicLibraryTab.ARTISTS)
      assertEquals("Save for artists", session.currentSortSaveLabel)

      session.updateTab(MusicLibraryTab.GENRES)
      assertEquals("Save for genre", session.currentSortSaveLabel)

      // Returning to Home restores saved RANDOM sort; returning to Songs restores saved DURATION_DESC sort
      session.updateTab(MusicLibraryTab.HOME)
      assertEquals(MusicSortOrder.RANDOM, session.sortOrder)
      assertTrue(session.isSortSavedForCurrentScope)

      session.updateTab(MusicLibraryTab.SONGS)
      assertEquals(MusicSortOrder.DURATION_DESC, session.sortOrder)
      assertTrue(session.isSortSavedForCurrentScope)

      // 3. Adding and removing a custom folder pill, and "Save for folder" sort option
      val customFolder = MusicFolder(
        folderName = "SynthwaveVault",
        displayPath = "/Music/SynthwaveVault",
        isCustom = true,
      )
      session.addCustomFolder(customFolder)
      assertEquals(1, session.customFolders.size)
      assertEquals(customFolder.folderPath, session.selectedCustomFolderPath)
      assertEquals("Save for folder", session.currentSortSaveLabel)
      assertFalse(session.isSortSavedForCurrentScope)

      session.setSaveSortForCurrentScope(true)
      session.updateSortOrder(MusicSortOrder.YEAR_DESC)
      assertTrue(session.isSortSavedForCurrentScope)
      assertEquals(MusicSortOrder.YEAR_DESC, session.sortOrder)

      // Removing the custom folder clears selection and restores active tab sort
      session.removeCustomFolder(customFolder)
      assertTrue(session.customFolders.isEmpty())
      assertNull(session.selectedCustomFolderPath)
      assertEquals(MusicLibraryTab.SONGS, session.selectedTab)
      assertEquals(MusicSortOrder.DURATION_DESC, session.sortOrder)
    } finally {
      session.release()
    }
  }

  @Test
  fun groupAndSort_supportsSongsGroupingByAlbumArtistGenre_andAlbumsGroupingByArtistGenre() {
    val tracks = MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS
    val albums = MusicPlayerEngine.buildAlbums(tracks)

    // 1. Available group options per section
    assertTrue(MusicPlayerEngine.availableGroupOptions(MusicLibraryTab.HOME).isEmpty())
    assertTrue(MusicPlayerEngine.availableGroupOptions(MusicLibraryTab.PLAYLISTS).isEmpty())
    assertTrue(MusicPlayerEngine.availableGroupOptions(MusicLibraryTab.ARTISTS).isEmpty())
    assertTrue(MusicPlayerEngine.availableGroupOptions(MusicLibraryTab.GENRES).isEmpty())

    assertEquals(
      listOf(
        MusicGroupOption.ALBUM,
        MusicGroupOption.ARTIST,
        MusicGroupOption.GENRE,
        MusicGroupOption.YEAR,
        MusicGroupOption.NONE,
      ),
      MusicPlayerEngine.availableGroupOptions(MusicLibraryTab.SONGS),
    )
    assertEquals(
      listOf(
        MusicGroupOption.ALBUM,
        MusicGroupOption.ARTIST,
        MusicGroupOption.GENRE,
        MusicGroupOption.YEAR,
        MusicGroupOption.NONE,
      ),
      MusicPlayerEngine.availableGroupOptions(
        selectedTab = MusicLibraryTab.HOME,
        selectedCustomFolderPath = "/Music/SynthwaveVault",
      ),
    )
    assertEquals(
      listOf(
        MusicGroupOption.ARTIST,
        MusicGroupOption.GENRE,
        MusicGroupOption.YEAR,
        MusicGroupOption.NONE,
      ),
      MusicPlayerEngine.availableGroupOptions(MusicLibraryTab.ALBUMS),
    )

    // 2. Grouping songs by None, Album, Artist, Genre, and Year
    val songsNone = MusicPlayerEngine.groupAndSortTracks(
      tracks = tracks,
      groupOption = MusicGroupOption.NONE,
      sortOrder = MusicSortOrder.TITLE_ASC,
    )
    assertEquals(1, songsNone.size)
    assertEquals("", songsNone.first().title)
    assertEquals(tracks.size, songsNone.first().tracks.size)

    val songsByAlbum = MusicPlayerEngine.groupAndSortTracks(
      tracks = tracks,
      groupOption = MusicGroupOption.ALBUM,
      sortOrder = MusicSortOrder.TITLE_ASC,
    )
    assertTrue(songsByAlbum.size > 1)
    assertEquals(
      songsByAlbum.map { it.title },
      songsByAlbum.map { it.title }.sortedWith(String.CASE_INSENSITIVE_ORDER),
    )
    assertEquals(tracks.size, songsByAlbum.sumOf { it.tracks.size })
    assertTrue(songsByAlbum.all { group -> group.tracks.all { it.album == group.title } })

    val songsByArtist = MusicPlayerEngine.groupAndSortTracks(
      tracks = tracks,
      groupOption = MusicGroupOption.ARTIST,
      sortOrder = MusicSortOrder.TITLE_ASC,
    )
    assertTrue(songsByArtist.size > 1)
    assertEquals(tracks.size, songsByArtist.sumOf { it.tracks.size })
    assertTrue(songsByArtist.all { group -> group.tracks.all { it.artist == group.title } })

    val songsByGenre = MusicPlayerEngine.groupAndSortTracks(
      tracks = tracks,
      groupOption = MusicGroupOption.GENRE,
      sortOrder = MusicSortOrder.TITLE_ASC,
    )
    assertTrue(songsByGenre.size > 1)
    assertEquals(tracks.size, songsByGenre.sumOf { it.tracks.size })
    assertTrue(songsByGenre.all { group -> group.tracks.all { it.genre == group.title } })

    val songsByYear = MusicPlayerEngine.groupAndSortTracks(
      tracks = tracks,
      groupOption = MusicGroupOption.YEAR,
      sortOrder = MusicSortOrder.TITLE_ASC,
    )
    assertTrue(songsByYear.size > 1)
    assertEquals(
      songsByYear.map { it.title.toInt() },
      songsByYear.map { it.title.toInt() }.sortedDescending(),
    )
    assertEquals(tracks.size, songsByYear.sumOf { it.tracks.size })
    assertTrue(songsByYear.all { group -> group.tracks.all { it.year.toString() == group.title } })

    // 3. Grouping albums by None, Artist, Genre, and Year
    val albumsNone = MusicPlayerEngine.groupAndSortAlbums(
      albums = albums,
      groupOption = MusicGroupOption.NONE,
      sortOrder = MusicSortOrder.TITLE_ASC,
    )
    assertEquals(1, albumsNone.size)
    assertEquals("", albumsNone.first().title)
    assertEquals(albums.size, albumsNone.first().albums.size)

    val albumsByArtist = MusicPlayerEngine.groupAndSortAlbums(
      albums = albums,
      groupOption = MusicGroupOption.ARTIST,
      sortOrder = MusicSortOrder.TITLE_ASC,
    )
    assertTrue(albumsByArtist.size > 1)
    assertEquals(albums.size, albumsByArtist.sumOf { it.albums.size })
    assertTrue(albumsByArtist.all { group -> group.albums.all { it.artist == group.title } })

    val albumsByGenre = MusicPlayerEngine.groupAndSortAlbums(
      albums = albums,
      groupOption = MusicGroupOption.GENRE,
      sortOrder = MusicSortOrder.TITLE_ASC,
    )
    assertTrue(albumsByGenre.size > 1)
    assertEquals(albums.size, albumsByGenre.sumOf { it.albums.size })
    assertTrue(albumsByGenre.all { group -> group.albums.all { it.genre == group.title } })

    val albumsByYear = MusicPlayerEngine.groupAndSortAlbums(
      albums = albums,
      groupOption = MusicGroupOption.YEAR,
      sortOrder = MusicSortOrder.TITLE_ASC,
    )
    assertTrue(albumsByYear.size > 1)
    assertEquals(
      albumsByYear.map { it.title.toInt() },
      albumsByYear.map { it.title.toInt() }.sortedDescending(),
    )
    assertEquals(albums.size, albumsByYear.sumOf { it.albums.size })
    assertTrue(albumsByYear.all { group -> group.albums.all { it.year.toString() == group.title } })

    // 4. Session per-section group & sort persistence and coercion across sections
    val session = MusicPlaybackSession(
      context = null,
      isInspection = true,
      initialTracks = tracks,
      initialTab = MusicLibraryTab.SONGS,
      initialGrantedFolderPaths = setOf("/Music"),
    )
    try {
      assertEquals(MusicGroupOption.NONE, session.groupOption)
      session.setSaveSortForCurrentScope(true)
      session.updateGroupOption(MusicGroupOption.ALBUM)
      session.updateSortOrder(MusicSortOrder.DURATION_DESC)
      assertEquals(MusicGroupOption.ALBUM, session.groupOption)
      assertEquals(MusicSortOrder.DURATION_DESC, session.sortOrder)

      // Switching to Albums tab resets group to NONE (or default) and allows grouping by Artist or Genre
      session.updateTab(MusicLibraryTab.ALBUMS)
      assertEquals(MusicGroupOption.NONE, session.groupOption)
      // Attempt to group by ALBUM in Albums view coerces to NONE
      session.updateGroupOption(MusicGroupOption.ALBUM)
      assertEquals(MusicGroupOption.NONE, session.groupOption)
      // Group by GENRE in Albums view succeeds
      session.setSaveSortForCurrentScope(true)
      session.updateGroupOption(MusicGroupOption.GENRE)
      assertEquals(MusicGroupOption.GENRE, session.groupOption)

      // Switching back to Songs restores saved ALBUM group option and DURATION_DESC sort
      session.updateTab(MusicLibraryTab.SONGS)
      assertEquals(MusicGroupOption.ALBUM, session.groupOption)
      assertEquals(MusicSortOrder.DURATION_DESC, session.sortOrder)

      // Switching back to Albums restores saved GENRE group option
      session.updateTab(MusicLibraryTab.ALBUMS)
      assertEquals(MusicGroupOption.GENRE, session.groupOption)
    } finally {
      session.release()
    }
  }

  @Test
  fun renameTrackArtistAlbumAndGenre_adjustsEmbeddedFileMetadataInBackgroundTask() = kotlinx.coroutines.runBlocking {
    val tempDir = java.nio.file.Files.createTempDirectory("music_rename_meta_test").toFile()
    try {
      // Create a WAV file and an MP3 file with initial metadata
      val wavBuffer = AudioEditorEngine.createSampleAudioBuffer(sampleRate = 8000, durationMs = 500L)
      val wavFile = File(tempDir, "Original Song.wav")
      assertTrue(
        AudioEditorEngine.writeWavFile(
          buffer = wavBuffer,
          outputFile = wavFile,
          metadata = AudioMetadataTags(
            title = "Original Song",
            artist = "Aurora Synth",
            album = "Midnight Odyssey",
            genre = "Synthwave",
            year = "2026",
            trackNumber = "1",
            comment = "Initial WAV tag",
          ),
        )
      )

      val mp3File = File(tempDir, "Second Track.mp3")
      mp3File.writeBytes(ByteArray(256) { (it and 0xFF).toByte() })
      assertTrue(
        AudioEditorEngine.writeAudioMetadata(
          file = mp3File,
          metadata = AudioMetadataTags(
            title = "Second Track",
            artist = "Aurora Synth",
            album = "Midnight Odyssey",
            genre = "Synthwave",
            year = "2026",
            trackNumber = "2",
            comment = "Initial MP3 tag",
          ),
        )
      )

      val track1 = MusicPlayerEngine.buildTrackFromFile(wavFile, inspectMetadataRetriever = false)
      val track2 = MusicPlayerEngine.buildTrackFromFile(mp3File, inspectMetadataRetriever = false)
      assertEquals("Original Song", track1.title)
      assertEquals("Aurora Synth", track1.artist)
      assertEquals("Midnight Odyssey", track1.album)
      assertEquals("Synthwave", track1.genre)
      assertEquals("Second Track", track2.title)
      assertEquals("Aurora Synth", track2.artist)
      assertEquals("Midnight Odyssey", track2.album)
      assertEquals("Synthwave", track2.genre)

      val session = MusicPlaybackSession(
        context = null,
        isInspection = true,
        initialTracks = listOf(track1, track2),
      )
      try {
        // 1. Rename track1 ("Original Song" -> "Crystal Horizon")
        val renamedTrack = session.renameTrack(track1, "Crystal Horizon")
        assertNotNull(renamedTrack)
        session.awaitPendingMetadataUpdates()

        val renamedWavFile = renamedTrack!!.file
        assertNotNull(renamedWavFile)
        assertTrue(renamedWavFile!!.exists())
        assertEquals("Crystal Horizon.wav", renamedWavFile.name)

        val wavMetaAfterTrackRename = AudioEditorEngine.readAudioMetadata(renamedWavFile)
        assertEquals("Crystal Horizon", wavMetaAfterTrackRename.title)
        assertEquals("Aurora Synth", wavMetaAfterTrackRename.artist)
        assertEquals("Midnight Odyssey", wavMetaAfterTrackRename.album)
        assertEquals("Synthwave", wavMetaAfterTrackRename.genre)
        assertEquals("Initial WAV tag", wavMetaAfterTrackRename.comment)

        // 2. Rename Artist ("Aurora Synth" -> "Aurora Prime") across both WAV and MP3 files
        val artistUpdatedCount = session.renameArtist("Aurora Synth", "Aurora Prime")
        assertEquals(2, artistUpdatedCount)
        session.awaitPendingMetadataUpdates()

        assertEquals("Aurora Prime", AudioEditorEngine.readAudioMetadata(renamedWavFile).artist)
        assertEquals("Aurora Prime", AudioEditorEngine.readAudioMetadata(mp3File).artist)

        // 3. Rename Album ("Midnight Odyssey" -> "Solaris") across both files
        val albumUpdatedCount = session.renameAlbum("Midnight Odyssey", "Aurora Prime", "Solaris")
        assertEquals(2, albumUpdatedCount)
        session.awaitPendingMetadataUpdates()

        assertEquals("Solaris", AudioEditorEngine.readAudioMetadata(renamedWavFile).album)
        assertEquals("Solaris", AudioEditorEngine.readAudioMetadata(mp3File).album)

        // 4. Rename Genre ("Synthwave" -> "Outrun") across both files
        val genreUpdatedCount = session.renameGenre("Synthwave", "Outrun")
        assertEquals(2, genreUpdatedCount)
        session.awaitPendingMetadataUpdates()

        val finalWavTags = AudioEditorEngine.readAudioMetadata(renamedWavFile)
        assertEquals("Crystal Horizon", finalWavTags.title)
        assertEquals("Aurora Prime", finalWavTags.artist)
        assertEquals("Solaris", finalWavTags.album)
        assertEquals("Outrun", finalWavTags.genre)

        val finalMp3Tags = AudioEditorEngine.readAudioMetadata(mp3File)
        assertEquals("Second Track", finalMp3Tags.title)
        assertEquals("Aurora Prime", finalMp3Tags.artist)
        assertEquals("Solaris", finalMp3Tags.album)
        assertEquals("Outrun", finalMp3Tags.genre)
        assertEquals("Initial MP3 tag", finalMp3Tags.comment)

        // Re-scanning files from disk reflects all updated metadata tags
        MusicPlayerEngine.clearTrackMetadataCache()
        val rescanned1 = MusicPlayerEngine.buildTrackFromFile(renamedWavFile, inspectMetadataRetriever = false)
        val rescanned2 = MusicPlayerEngine.buildTrackFromFile(mp3File, inspectMetadataRetriever = false)
        assertEquals("Crystal Horizon", rescanned1.title)
        assertEquals("Aurora Prime", rescanned1.artist)
        assertEquals("Solaris", rescanned1.album)
        assertEquals("Outrun", rescanned1.genre)
        assertEquals("Second Track", rescanned2.title)
        assertEquals("Aurora Prime", rescanned2.artist)
        assertEquals("Solaris", rescanned2.album)
        assertEquals("Outrun", rescanned2.genre)
      } finally {
        session.release()
      }
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun extractPaletteFromPixels_preservesDominantHueOnComplementarySplitAndDarkCovers() {
    // 1. Complementary split cover (60% warm crimson red #DC2626, 40% emerald green #16A34A):
    // Linear RGB averaging would blend red + green into muddy yellow/olive, whereas perceptual
    // OKLab clustering must select the true dominant crimson red and secondary emerald green.
    val width = 24
    val height = 24
    val pixels = IntArray(width * height)
    val crimsonArgb = (0xFF shl 24) or 0xDC2626
    val emeraldArgb = (0xFF shl 24) or 0x16A34A
    for (y in 0 until height) {
      for (x in 0 until width) {
        pixels[y * width + x] = if (y < 15) crimsonArgb else emeraldArgb
      }
    }

    val splitPalette = MusicPlayerEngine.extractPaletteFromPixels(pixels, width, height)
    assertTrue(
      "Dominant color should remain warm red (${splitPalette.dominantHex})",
      splitPalette.dominantColor.red > 0.75f &&
        splitPalette.dominantColor.green < 0.25f &&
        splitPalette.dominantColor.blue < 0.25f,
    )
    assertTrue(
      "Secondary color should remain emerald green (${splitPalette.secondaryHex})",
      splitPalette.secondaryColor.green > 0.55f &&
        splitPalette.secondaryColor.red < 0.20f,
    )

    // 2. Deep navy cover (#0F2547) with a vibrant gold logo (#F59E0B) occupying 10% of pixels:
    // Must not fall back to the old hardcoded purple (#5E35B1).
    val darkCoverPixels = IntArray(width * height) { (0xFF shl 24) or 0x0F2547 }
    for (i in 0 until 60) {
      darkCoverPixels[i] = (0xFF shl 24) or 0xF59E0B
    }
    val darkPalette = MusicPlayerEngine.extractPaletteFromPixels(darkCoverPixels, width, height)
    assertTrue(
      "Dominant color on deep navy cover should stay blue-dominant (${darkPalette.dominantHex})",
      darkPalette.dominantColor.blue > darkPalette.dominantColor.red,
    )
    assertTrue(
      "Accent color on deep navy cover with gold logo should pick gold (${darkPalette.accentHex})",
      darkPalette.accentColor.red > 0.80f &&
        darkPalette.accentColor.green > 0.50f &&
        darkPalette.accentColor.blue < 0.20f,
    )
  }

  @Test
  fun extractEmbeddedArtworkBytes_parsesId3v2ApicAndFlacMetadataBlockPicture() {
    val tempDir = java.nio.file.Files.createTempDirectory("music_embedded_art_parse_test").toFile()
    try {
      val fakeJpegBytes = byteArrayOf(
        0xFF.toByte(),
        0xD8.toByte(),
        0xFF.toByte(),
        0xE0.toByte(),
        0x00,
        0x10,
        'J'.code.toByte(),
        'F'.code.toByte(),
        'I'.code.toByte(),
        'F'.code.toByte(),
        0x00,
        0x01,
        0x02,
        0x03,
        0xFF.toByte(),
        0xD9.toByte(),
      )

      // 1. Build an MP3 file with an ID3v2.3 APIC frame
      val mp3File = File(tempDir, "tagged_cover.mp3")
      val mimeBytes = "image/jpeg".toByteArray(Charsets.US_ASCII)
      val apicPayload = java.io.ByteArrayOutputStream().apply {
        write(0) // ISO-8859-1 encoding
        write(mimeBytes)
        write(0) // null-terminated MIME
        write(3) // front cover picture type
        write(0) // empty description null-terminator
        write(fakeJpegBytes)
      }.toByteArray()
      val apicSize = apicPayload.size
      val id3TagBytes = java.io.ByteArrayOutputStream().apply {
        val totalTagSize = 10 + apicSize
        write(
          byteArrayOf(
            'I'.code.toByte(),
            'D'.code.toByte(),
            '3'.code.toByte(),
            3,
            0,
            0,
            ((totalTagSize shr 21) and 0x7F).toByte(),
            ((totalTagSize shr 14) and 0x7F).toByte(),
            ((totalTagSize shr 7) and 0x7F).toByte(),
            (totalTagSize and 0x7F).toByte(),
          ),
        )
        write("APIC".toByteArray(Charsets.US_ASCII))
        write(
          byteArrayOf(
            ((apicSize shr 24) and 0xFF).toByte(),
            ((apicSize shr 16) and 0xFF).toByte(),
            ((apicSize shr 8) and 0xFF).toByte(),
            (apicSize and 0xFF).toByte(),
            0,
            0,
          ),
        )
        write(apicPayload)
        write(ByteArray(32) { 0x55 })
      }.toByteArray()
      mp3File.writeBytes(id3TagBytes)

      val extractedId3Art = MusicPlayerEngine.extractEmbeddedArtworkBytes(mp3File)
      assertNotNull(extractedId3Art)
      assertTrue(fakeJpegBytes.contentEquals(extractedId3Art))

      // 2. Build a FLAC file with a METADATA_BLOCK_PICTURE (type 6)
      val flacFile = File(tempDir, "tagged_cover.flac")
      val flacPicBlock = java.nio.ByteBuffer.allocate(32 + mimeBytes.size + fakeJpegBytes.size)
        .order(java.nio.ByteOrder.BIG_ENDIAN)
        .apply {
          putInt(3) // front cover
          putInt(mimeBytes.size)
          put(mimeBytes)
          putInt(0) // empty description
          putInt(300) // width
          putInt(300) // height
          putInt(24) // depth
          putInt(0) // colors
          putInt(fakeJpegBytes.size)
          put(fakeJpegBytes)
        }.array()
      val flacBytes = java.io.ByteArrayOutputStream().apply {
        write("fLaC".toByteArray(Charsets.US_ASCII))
        val blockLen = flacPicBlock.size
        write(0x80 or 6) // isLast=true, blockType=6 (PICTURE)
        write((blockLen shr 16) and 0xFF)
        write((blockLen shr 8) and 0xFF)
        write(blockLen and 0xFF)
        write(flacPicBlock)
      }.toByteArray()
      flacFile.writeBytes(flacBytes)

      val extractedFlacArt = MusicPlayerEngine.extractEmbeddedArtworkBytes(flacFile)
      assertNotNull(extractedFlacArt)
      assertTrue(fakeJpegBytes.contentEquals(extractedFlacArt))
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun resolveTrackPalette_doesNotPoisonExtractedPaletteCacheWithDeterministicFallbackColors() {
    val tempDir = java.nio.file.Files.createTempDirectory("music_palette_cache_isolation_test").toFile()
    MusicPlayerEngine.clearArtworkCache()
    MusicPlayerEngine.clearTrackMetadataCache()
    try {
      val albumDir = File(tempDir, "Music/WarmSunsetAlbum").apply { mkdirs() }
      val trackFile1 = File(albumDir, "01 - Warm Track.mp3").apply { writeBytes(ByteArray(128) { 1 }) }
      val trackFile2 = File(albumDir, "02 - Second Warm Track.mp3").apply { writeBytes(ByteArray(128) { 2 }) }

      // Track initially scanned with deferAlbumArt=true gets a deterministic preset color (e.g. Cyan #00B4D8),
      // which is completely opposite the actual warm crimson/orange album cover (#E63946).
      val track1 = MusicTrack(
        id = "file_${trackFile1.absolutePath}",
        title = "Warm Track",
        artist = "Solar Artist",
        album = "WarmSunsetAlbum",
        genre = "Indie",
        durationMs = 180_000L,
        file = trackFile1,
        sizeBytes = trackFile1.length(),
        lastModifiedMs = trackFile1.lastModified(),
        artworkPrimaryRgb = 0x00B4D8, // Deterministic cyan fallback (opposite of warm red cover)
        artworkSecondaryRgb = 0x7209B7,
        artworkAccentRgb = 0x4CC9F0,
        isSampleTrack = false,
      )
      val track2 = track1.copy(
        id = "file_${trackFile2.absolutePath}",
        title = "Second Warm Track",
        file = trackFile2,
        sizeBytes = trackFile2.length(),
        lastModifiedMs = trackFile2.lastModified(),
      )

      // 1. Synchronous resolveTrackPalette before bitmap decoding returns the temporary fallback
      //    without polluting the extracted paletteCache.
      val initialFallback = MusicPlayerEngine.resolveTrackPalette(track1)
      assertEquals("#00B4D8", initialFallback.dominantHex)

      // 2. Simulate album art extraction from a warm crimson/orange cover (#E63946 / #9D0208 / #FFB703)
      val warmPixels = IntArray(32 * 32) { idx ->
        when {
          idx < 700 -> (0xFF shl 24) or 0xE63946
          idx < 920 -> (0xFF shl 24) or 0x9D0208
          else -> (0xFF shl 24) or 0xFFB703
        }
      }
      val extractedWarmPalette = MusicPlayerEngine.extractPaletteFromPixels(warmPixels, 32, 32)
      assertTrue(
        "Extracted palette must be warm red, not cyan (${extractedWarmPalette.dominantHex})",
        extractedWarmPalette.dominantColor.red > 0.75f && extractedWarmPalette.dominantColor.blue < 0.35f,
      )

      // Record extracted palette into cache via reflection or package-visible flow by caching it for track1
      val recordMethod = MusicPlayerEngine::class.java.getDeclaredMethod(
        "recordExtractedPaletteForTrack",
        MusicTrack::class.java,
        AlbumArtPalette::class.java,
      ).apply { isAccessible = true }
      recordMethod.invoke(MusicPlayerEngine, track1, extractedWarmPalette)

      // 3. Subsequent resolveTrackPalette calls for both track1 and sibling track2 on the same album
      //    now return the extracted warm red album art palette rather than the opposite cyan fallback!
      val resolved1 = MusicPlayerEngine.resolveTrackPalette(track1)
      val resolved2 = MusicPlayerEngine.resolveTrackPalette(track2)
      assertEquals(extractedWarmPalette.dominantHex, resolved1.dominantHex)
      assertEquals(extractedWarmPalette.dominantHex, resolved2.dominantHex)

      // 4. Re-building track metadata from file with deferAlbumArt=true also picks up the extracted warm colors
      val rebuiltTrack1 = MusicPlayerEngine.buildTrackFromFile(
        file = trackFile1,
        inspectMetadataRetriever = false,
        deferAlbumArt = true,
      )
      // Even if buildTrackFromFile derives a different album/artist in fast pass, filePaletteKey preserves the file's extracted palette!
      assertEquals(
        extractedWarmPalette.dominantHex,
        MusicPlayerEngine.formatRgbHex(rebuiltTrack1.artworkPrimaryRgb),
      )
    } finally {
      MusicPlayerEngine.clearArtworkCache()
      MusicPlayerEngine.clearTrackMetadataCache()
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun computeBoundedInSampleSize_boundsSquareAndRectangularHighResArtworkBelowBinderLimit() {
    // Square 3000x3000 cover targeting 240px for MediaMetadata / Notification IPC:
    // Decoded dimensions must never exceed 2 * targetSize (480px -> <= 921 KB ARGB_8888 before final 240px scale).
    val sampleSquare = MusicPlayerEngine.computeBoundedInSampleSize(3000, 3000, 240)
    assertTrue((3000 / sampleSquare) <= 480)

    // Borderline 718x718 cover (previously kept at inSampleSize=1 -> 2.06 MB ARGB_8888, exceeding 1 MB Binder limit):
    val sampleBorderline = MusicPlayerEngine.computeBoundedInSampleSize(718, 718, 240)
    assertTrue((718 / sampleBorderline) <= 480)

    // Wide rectangular 2560x680 banner cover (previously kept at inSampleSize=1 because halfHeight < 360 -> ~7 MB!):
    val sampleWide = MusicPlayerEngine.computeBoundedInSampleSize(2560, 680, 360)
    assertTrue(
      "Wide cover width after subsampling should be <= 720px, got ${2560 / sampleWide}",
      (2560 / sampleWide) <= 720,
    )
    assertTrue((680 / sampleWide) <= 720)
  }

  @Test
  fun recordExtractedPaletteForTrack_isSnapshotSafeAcrossBackgroundThreadsAndUpdatesActiveSession() = kotlinx.coroutines.runBlocking {
    MusicPlayerEngine.resetPlaybackSession()
    MusicPlayerEngine.clearArtworkCache()
    val tempDir = java.nio.file.Files.createTempDirectory("music_snapshot_thread_safety_test").toFile()
    try {
      val trackFile = File(tempDir, "01 - Horizon.mp3").apply { writeBytes(ByteArray(64) { 7 }) }
      val track = MusicTrack(
        id = "file_${trackFile.absolutePath}",
        title = "Horizon",
        artist = "Solaris",
        album = "Golden Hour",
        genre = "Electronic",
        durationMs = 200_000L,
        file = trackFile,
        sizeBytes = trackFile.length(),
        lastModifiedMs = trackFile.lastModified(),
        artworkPrimaryRgb = 0x00B4D8,
        artworkSecondaryRgb = 0x7209B7,
        artworkAccentRgb = 0x4CC9F0,
        isSampleTrack = false,
      )

      val session = MusicPlaybackSession(
        context = null,
        isInspection = true,
        initialTracks = listOf(track),
      )
      MusicPlayerEngine.activeSessionState = session
      try {
        session.playTrackInContext(track, listOf(track))
        assertTrue(session.systemMediaControlState.isActive)
        assertEquals(track.id, session.systemMediaControlState.trackId)

        val recordMethod = MusicPlayerEngine::class.java.getDeclaredMethod(
          "recordExtractedPaletteForTrack",
          MusicTrack::class.java,
          AlbumArtPalette::class.java,
        ).apply { isAccessible = true }

        val extractedPalette = MusicPlayerEngine.buildPaletteFromRgb(0xE63946, 0x9D0208, 0xFFB703)

        // Hammer concurrent background Dispatchers.IO palette updates while taking & applying
        // Compose snapshots on the foreground coroutine to verify zero Snapshot state crashes.
        val jobs = (1..8).map {
          launch(Dispatchers.IO) {
            repeat(15) {
              recordMethod.invoke(MusicPlayerEngine, track, extractedPalette)
            }
          }
        }
        repeat(20) {
          androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
            val currentHex = MusicPlayerEngine.activeTrackPalette.dominantHex
            assertTrue(currentHex.startsWith("#"))
          }
        }
        jobs.forEach { it.join() }

        assertEquals(extractedPalette.dominantHex, session.activePalette.dominantHex)
        assertEquals(extractedPalette.dominantHex, MusicPlayerEngine.activeTrackPalette.dominantHex)
        assertTrue(session.systemMediaControlState.isActive)
        assertTrue(session.systemMediaControlState.isPlaying)
      } finally {
        session.release()
        MusicPlayerEngine.resetPlaybackSession()
      }
    } finally {
      MusicPlayerEngine.clearArtworkCache()
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun applyExternalSystemMediaMetadata_syncsExternalPlaybackAndTransitionsStoppedStateCleanly() {
    MusicPlayerEngine.resetPlaybackSession()
    try {
      // 1. System-synced external session becomes active and drives isMusicPlaying + activeTrackPalette
      val syncedInfo = MusicPlayerEngine.applyExternalSystemMediaMetadata(
        title = "Midnight City",
        artist = "M83",
        album = "Hurry Up, We're Dreaming",
        sourcePackage = "com.spotify.music",
        sourceAppName = "Spotify",
        isPlaying = true,
        notificationColorRgb = 0x1DB954,
        durationMs = 243_000L,
        positionMs = 42_000L,
        fromSystemSync = true,
      )
      assertTrue(MusicPlayerEngine.isMusicPlaying)
      assertEquals("Midnight City", MusicPlayerEngine.externalSystemMediaInfo?.title)
      assertEquals("#1DB954", syncedInfo.palette.dominantHex)
      assertEquals("#1DB954", MusicPlayerEngine.activeTrackPalette.dominantHex)

      // 2. Repeating identical metadata does not corrupt state
      val repeatedInfo = MusicPlayerEngine.applyExternalSystemMediaMetadata(
        title = "Midnight City",
        artist = "M83",
        album = "Hurry Up, We're Dreaming",
        sourcePackage = "com.spotify.music",
        sourceAppName = "Spotify",
        isPlaying = false,
        notificationColorRgb = 0x1DB954,
        durationMs = 243_000L,
        positionMs = 45_000L,
        fromSystemSync = true,
      )
      assertEquals(false, repeatedInfo.isPlaying)
      assertEquals(false, MusicPlayerEngine.isMusicPlaying)
    } finally {
      MusicPlayerEngine.resetPlaybackSession()
    }
  }

  @Test
  fun externalSystemMedia_ignoresPhoneCallDialerAndTelecomSessions() {
    MusicPlayerEngine.resetPlaybackSession()
    try {
      // 1. Phone, Dialer, Telecom, and InCallUI packages, labels, audio modes, and usages are recognized as ignored
      assertTrue(MusicPlayerEngine.isIgnoredPhoneOrCallPackage(null, "com.google.android.dialer"))
      assertTrue(MusicPlayerEngine.isIgnoredPhoneOrCallPackage(null, "com.android.dialer"))
      assertTrue(MusicPlayerEngine.isIgnoredPhoneOrCallPackage(null, "com.android.phone"))
      assertTrue(MusicPlayerEngine.isIgnoredPhoneOrCallPackage(null, "com.android.server.telecom"))
      assertTrue(MusicPlayerEngine.isIgnoredPhoneOrCallPackage(null, "com.samsung.android.incallui"))
      assertFalse(MusicPlayerEngine.isIgnoredPhoneOrCallPackage(null, "com.spotify.music"))
      assertFalse(MusicPlayerEngine.isIgnoredPhoneOrCallPackage(null, "com.google.android.apps.youtube.music"))

      assertTrue(MusicPlayerEngine.isIgnoredPhoneOrCallAppName("Phone"))
      assertTrue(MusicPlayerEngine.isIgnoredPhoneOrCallAppName("Dialer"))
      assertTrue(MusicPlayerEngine.isIgnoredPhoneOrCallAppName("Call Management"))
      assertTrue(MusicPlayerEngine.isIgnoredPhoneOrCallAppName("Phone Services"))
      assertTrue(MusicPlayerEngine.isIgnoredPhoneOrCallAppName("Incoming call"))
      assertFalse(MusicPlayerEngine.isIgnoredPhoneOrCallAppName("Spotify"))
      assertFalse(MusicPlayerEngine.isIgnoredPhoneOrCallAppName("YouTube Music"))

      assertTrue(MusicPlayerEngine.isPhoneCallAudioMode(android.media.AudioManager.MODE_RINGTONE))
      assertTrue(MusicPlayerEngine.isPhoneCallAudioMode(android.media.AudioManager.MODE_IN_CALL))
      assertTrue(MusicPlayerEngine.isPhoneCallAudioMode(android.media.AudioManager.MODE_IN_COMMUNICATION))
      assertFalse(MusicPlayerEngine.isPhoneCallAudioMode(android.media.AudioManager.MODE_NORMAL))

      assertTrue(MusicPlayerEngine.isNonMediaCallOrRingtoneUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE))
      assertTrue(MusicPlayerEngine.isNonMediaCallOrRingtoneUsage(android.media.AudioAttributes.USAGE_VOICE_COMMUNICATION))
      assertFalse(MusicPlayerEngine.isNonMediaCallOrRingtoneUsage(android.media.AudioAttributes.USAGE_MEDIA))

      // 2. Receiving a phone call when no external media is active does NOT populate externalSystemMediaInfo
      MusicPlayerEngine.updateExternalSystemMediaFromMetadata(
        title = "Phone",
        sourcePackage = "com.google.android.dialer",
        sourceAppName = "Phone",
        isPlaying = true,
      )
      assertNull(MusicPlayerEngine.externalSystemMediaInfo)
      assertFalse(MusicPlayerEngine.isMusicPlaying)
      assertEquals(false, MusicPlayerEngine.activeSystemMediaInfo?.isExternalApp)

      MusicPlayerEngine.applyExternalSystemMediaMetadata(
        title = "Incoming call",
        artist = "(555) 019-2834",
        sourcePackage = "com.android.server.telecom",
        sourceAppName = "Call Management",
        isPlaying = true,
        fromSystemSync = true,
      )
      assertNull(MusicPlayerEngine.externalSystemMediaInfo)
      assertFalse(MusicPlayerEngine.isMusicPlaying)

      // 3. When a legitimate external music player (e.g. Spotify) is active and a phone call arrives,
      // the phone session is ignored and does not overwrite the music player's session with "Phone"
      MusicPlayerEngine.updateExternalSystemMediaFromMetadata(
        title = "Veridis Quo",
        artist = "Daft Punk",
        album = "Discovery",
        sourcePackage = "com.spotify.music",
        sourceAppName = "Spotify",
        isPlaying = false,
      )
      assertEquals("Veridis Quo", MusicPlayerEngine.externalSystemMediaInfo?.title)
      assertEquals("Spotify", MusicPlayerEngine.externalSystemMediaInfo?.sourceAppName)

      MusicPlayerEngine.applyExternalSystemMediaMetadata(
        title = "Phone",
        sourcePackage = "com.google.android.dialer",
        sourceAppName = "Phone",
        isPlaying = true,
        fromSystemSync = true,
      )
      assertEquals("Veridis Quo", MusicPlayerEngine.externalSystemMediaInfo?.title)
      assertEquals("Spotify", MusicPlayerEngine.externalSystemMediaInfo?.sourceAppName)
      assertFalse(MusicPlayerEngine.isMusicPlaying)
    } finally {
      MusicPlayerEngine.resetPlaybackSession()
    }
  }
}
