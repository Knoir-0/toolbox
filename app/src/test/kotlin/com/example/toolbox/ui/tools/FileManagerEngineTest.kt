package com.example.toolbox.ui.tools

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FileManagerEngineTest {

  @get:Rule
  val tempFolder = TemporaryFolder()

  @Test
  fun formatFileSize_formatsCorrectlyAcrossUnits() {
    assertEquals("0 B", FileManagerEngine.formatFileSize(-5))
    assertEquals("0 B", FileManagerEngine.formatFileSize(0))
    assertEquals("512 B", FileManagerEngine.formatFileSize(512))
    assertEquals("1.0 KB", FileManagerEngine.formatFileSize(1024))
    assertEquals("1.5 KB", FileManagerEngine.formatFileSize(1536))
    assertEquals("1.0 MB", FileManagerEngine.formatFileSize(1024 * 1024))
    assertEquals("2.5 MB", FileManagerEngine.formatFileSize((2.5 * 1024 * 1024).toLong()))
    assertEquals("1.00 GB", FileManagerEngine.formatFileSize(1024L * 1024L * 1024L))
    assertEquals("2.50 GB", FileManagerEngine.formatFileSize((2.5 * 1024L * 1024L * 1024L).toLong()))
  }

  @Test
  fun formatTimestamp_formatsValidTimestampAndHandlesInvalid() {
    assertEquals("Unknown", FileManagerEngine.formatTimestamp(0))
    assertEquals("Unknown", FileManagerEngine.formatTimestamp(-100))

    val epoch = 1700000000000L
    val formatted = FileManagerEngine.formatTimestamp(epoch)
    assertNotNull(formatted)
    assertTrue(formatted.length > 5)
  }

  @Test
  fun fileClassification_detectsFileTypesCorrectly() {
    val txtFile = File("notes.txt")
    val mdFile = File("readme.md")
    val jsonFile = File("config.json")
    val pngFile = File("photo.png")
    val mp3File = File("song.mp3")
    val mp4File = File("movie.mp4")
    val zipFile = File("archive.zip")
    val ktFile = File("Main.kt")
    val unknownFile = File("data.bin")

    assertTrue(FileManagerEngine.isTextFile(txtFile))
    assertTrue(FileManagerEngine.isTextFile(mdFile))
    assertTrue(FileManagerEngine.isTextFile(jsonFile))
    assertTrue(FileManagerEngine.isTextFile(ktFile))
    assertFalse(FileManagerEngine.isTextFile(pngFile))
    assertFalse(FileManagerEngine.isTextFile(unknownFile))

    assertTrue(FileManagerEngine.isImageFile(pngFile))
    assertFalse(FileManagerEngine.isImageFile(txtFile))

    assertTrue(FileManagerEngine.isAudioFile(mp3File))
    assertFalse(FileManagerEngine.isAudioFile(mp4File))

    assertTrue(FileManagerEngine.isVideoFile(mp4File))
    assertFalse(FileManagerEngine.isVideoFile(mp3File))

    assertTrue(FileManagerEngine.isArchiveFile(zipFile))
    assertFalse(FileManagerEngine.isArchiveFile(pngFile))

    assertTrue(FileManagerEngine.isCodeFile(jsonFile))
    assertTrue(FileManagerEngine.isCodeFile(ktFile))
    assertFalse(FileManagerEngine.isCodeFile(txtFile))
  }

  @Test
  fun validateFileName_enforcesValidityRules() {
    assertNotNull(FileManagerEngine.validateFileName(""))
    assertNotNull(FileManagerEngine.validateFileName("   "))
    assertNotNull(FileManagerEngine.validateFileName("."))
    assertNotNull(FileManagerEngine.validateFileName(".."))
    assertNotNull(FileManagerEngine.validateFileName("a/b"))
    assertNotNull(FileManagerEngine.validateFileName("file:name"))
    assertNotNull(FileManagerEngine.validateFileName("bad*name"))
    assertNotNull(FileManagerEngine.validateFileName("a".repeat(256)))

    assertNull(FileManagerEngine.validateFileName("normal_name.txt"))
    assertNull(FileManagerEngine.validateFileName("My Folder 123"))

    // Duplicate check in parent folder
    val root = tempFolder.root
    val existing = File(root, "existing.txt")
    existing.writeText("hello")

    assertNotNull(FileManagerEngine.validateFileName("existing.txt", root))
    assertNull(FileManagerEngine.validateFileName("non_existing.txt", root))
  }

  @Test
  fun fileAndFolderCreation_createsSuccessfully() {
    val root = tempFolder.root

    val folderResult = FileManagerEngine.createFolder(root, "SubFolder")
    assertTrue(folderResult.isSuccess)
    val folder = folderResult.getOrThrow()
    assertTrue(folder.exists())
    assertTrue(folder.isDirectory)

    val fileResult = FileManagerEngine.createFile(root, "test.txt", "Initial Content")
    assertTrue(fileResult.isSuccess)
    val file = fileResult.getOrThrow()
    assertTrue(file.exists())
    assertTrue(file.isFile)
    assertEquals("Initial Content", file.readText())
  }

  @Test
  fun renameFile_renamesFileSuccessfully() {
    val root = tempFolder.root
    val file = File(root, "old_name.txt")
    file.writeText("Hello")

    val result = FileManagerEngine.renameFile(file, "new_name.txt")
    assertTrue(result.isSuccess)
    val renamed = result.getOrThrow()
    assertFalse(file.exists())
    assertTrue(renamed.exists())
    assertEquals("new_name.txt", renamed.name)
    assertEquals("Hello", renamed.readText())
  }

  @Test
  fun duplicateFile_createsIncrementedCopies() {
    val root = tempFolder.root
    val file = File(root, "document.txt")
    file.writeText("Copy me")

    val copy1Result = FileManagerEngine.duplicateFile(file)
    assertTrue(copy1Result.isSuccess)
    val copy1 = copy1Result.getOrThrow()
    assertEquals("document (copy).txt", copy1.name)
    assertEquals("Copy me", copy1.readText())

    val copy2Result = FileManagerEngine.duplicateFile(file)
    assertTrue(copy2Result.isSuccess)
    val copy2 = copy2Result.getOrThrow()
    assertEquals("document (copy 2).txt", copy2.name)
    assertEquals("Copy me", copy2.readText())
  }

  @Test
  fun deleteRecursively_removesFoldersAndFiles() {
    val root = tempFolder.root
    val folder = File(root, "ToDelete")
    folder.mkdirs()
    File(folder, "child1.txt").writeText("1")
    File(folder, "child2.txt").writeText("2")

    assertTrue(folder.exists())
    assertTrue(FileManagerEngine.deleteFileOrDirectory(folder))
    assertFalse(folder.exists())
  }

  @Test
  fun readAndWriteTextContent_worksCorrectly() {
    val root = tempFolder.root
    val file = File(root, "editable.txt")
    file.writeText("Original")

    assertEquals("Original", FileManagerEngine.readTextContent(file))

    assertTrue(FileManagerEngine.writeTextContent(file, "Updated text"))
    assertEquals("Updated text", FileManagerEngine.readTextContent(file))
  }

  @Test
  fun calculateMd5_producesAccurateHash() {
    val root = tempFolder.root
    val file = File(root, "md5_test.txt")
    file.writeText("hello world")

    // Standard MD5 of "hello world" (without trailing newline)
    val expectedMd5 = "5eb63bbbe01eeed093cb22bb8f5acdc3"
    assertEquals(expectedMd5, FileManagerEngine.calculateMd5(file))
  }

  @Test
  fun getFileDetails_populatesAllMetadata() {
    val root = tempFolder.root
    val file = File(root, "metadata.txt")
    file.writeText("Line 1\nLine 2\nLine 3")

    val details = FileManagerEngine.getFileDetails(file)
    assertEquals("metadata.txt", details.name)
    assertFalse(details.isDirectory)
    assertEquals("text/plain", details.mimeType)
    assertEquals(3, details.lineCount)
    assertTrue(details.canRead)
    assertTrue(details.canWrite)
    assertNotNull(details.md5Checksum)
  }

  @Test
  fun sortingAndFiltering_organizesFilesProperly() {
    val root = tempFolder.root
    val dirB = File(root, "DirB").apply { mkdirs() }
    val dirA = File(root, "DirA").apply { mkdirs() }
    val fileZ = File(root, "z.txt").apply { writeText("Small") }
    val fileA = File(root, "a.txt").apply { writeText("Large content here!") }

    val items = listOf(
      FileManagerEngine.toFileItem(fileZ),
      FileManagerEngine.toFileItem(dirB),
      FileManagerEngine.toFileItem(fileA),
      FileManagerEngine.toFileItem(dirA),
    )

    // Name Ascending: Dirs first (DirA, DirB), then regular files (a.txt, z.txt)
    val sortedAsc = FileManagerEngine.sortFiles(items, FileSortOrder.NAME_ASC)
    assertEquals(listOf("DirA", "DirB", "a.txt", "z.txt"), sortedAsc.map { it.name })

    // Name Descending: Dirs first (DirB, DirA), then files (z.txt, a.txt)
    val sortedDesc = FileManagerEngine.sortFiles(items, FileSortOrder.NAME_DESC)
    assertEquals(listOf("DirB", "DirA", "z.txt", "a.txt"), sortedDesc.map { it.name })

    // Filter by query "dir"
    val filtered = FileManagerEngine.filterFiles(items, "dir")
    assertEquals(2, filtered.size)
    assertTrue(filtered.all { it.name.contains("Dir") })
  }

  @Test
  fun populateDefaultSampleFiles_createsInitialFiles() {
    val root = tempFolder.newFolder("SampleRoot")
    assertTrue(root.listFiles()!!.isEmpty())

    FileManagerEngine.populateDefaultSampleFiles(root)
    val created = root.listFiles()!!
    assertTrue(created.isNotEmpty())
    assertTrue(created.any { it.name == "Welcome.txt" })
    assertTrue(created.any { it.name == "Documents" && it.isDirectory })
    assertTrue(created.any { it.name == "Notes" && it.isDirectory })
    assertTrue(created.any { it.name == "Images" && it.isDirectory })
    assertTrue(created.any { it.name == "Audio" && it.isDirectory })
    assertTrue(created.any { it.name == "Music" && it.isDirectory })
    assertTrue(created.any { it.name == "Archives" && it.isDirectory })
    assertTrue(created.any { it.name == "Videos" && it.isDirectory })

    assertTrue(java.io.File(root, "Documents/sample_document.pdf").exists())
    assertTrue(java.io.File(root, "Images/sample_image.png").exists())
    assertTrue(java.io.File(root, "Audio/sample_chord_arpeggio.wav").exists())
    assertTrue(java.io.File(root, "Archives/sample_project_bundle.zip").exists())
    assertTrue(java.io.File(root, "Music").listFiles()!!.isNotEmpty())
  }

  @Test
  fun storageVolumes_defaultsToStorageEmulatedZeroRoot() {
    val rootDir = FileManagerEngine.getUserRootDirectory()
    assertNotNull(rootDir)
    assertEquals("storage/emulated/0", rootDir.path.trimStart('/'))

    val volumes = FileManagerEngine.getStorageVolumes(null)
    assertTrue(volumes.size >= 2)
    val primary = volumes.first()
    assertEquals("primary_root", primary.id)
    assertEquals("Home", primary.name)
    assertTrue(primary.isPrimary)
    assertFalse(primary.isRemovable)

    val samples = volumes[1]
    assertEquals(FileManagerEngine.SAMPLES_VOLUME_ID, samples.id)
    assertEquals(FileManagerEngine.SAMPLES_FOLDER_NAME, samples.name)
    assertFalse(samples.isPrimary)
    assertFalse(samples.isRemovable)
  }

  @Test
  fun storageSpaceInfo_computesFormattedAndPercentage() {
    val total = 100L * 1024L * 1024L * 1024L
    val used = 40L * 1024L * 1024L * 1024L
    val free = 60L * 1024L * 1024L * 1024L
    val info = StorageSpaceInfo(totalBytes = total, freeBytes = free, usedBytes = used)

    assertEquals(40, info.percentageUsed)
    assertEquals("40.00 GB used of 100.00 GB", info.displayText)
  }

  @Test
  fun computeFolderSize_andToFileItem_computesDirectorySize() {
    val root = tempFolder.root
    val folder = File(root, "Docs").apply { mkdirs() }
    val f1 = File(folder, "doc1.txt").apply { writeText("Hello") } // 5 bytes
    val f2 = File(folder, "doc2.txt").apply { writeText("World!") } // 6 bytes

    val size = FileManagerEngine.computeFolderSize(folder)
    assertEquals(11L, size)

    val item = FileManagerEngine.toFileItem(folder)
    assertTrue(item.isDirectory)
    assertEquals(2, item.childCount)
    assertEquals(11L, item.sizeBytes)

    val details = FileManagerEngine.getFileDetails(folder)
    assertTrue(details.isDirectory)
    assertEquals(2, details.childCount)
    assertEquals(11L, details.sizeBytes)
    assertTrue(details.sizeFormatted.contains("2 items"))
    assertTrue(details.sizeFormatted.contains("11 B"))
  }

  @Test
  fun bulkOperations_computesStorageAndHandlesCopyMoveDelete() {
    val root = tempFolder.root
    val folderA = File(root, "FolderA").apply { mkdirs() }
    val f1 = File(folderA, "test1.txt").apply { writeText("12345") } // 5 bytes
    val f2 = File(folderA, "test2.txt").apply { writeText("abcdef") } // 6 bytes
    val subFolder = File(folderA, "Sub").apply { mkdirs() }
    File(subFolder, "nested.txt").writeText("xyz") // 3 bytes

    val items = listOf(
      FileManagerEngine.toFileItem(f1),
      FileManagerEngine.toFileItem(f2),
      FileManagerEngine.toFileItem(subFolder),
    )

    val bulkInfo = FileManagerEngine.getBulkStorageInfo(items)
    assertEquals(3, bulkInfo.totalItems)
    assertEquals(1, bulkInfo.folderCount)
    assertEquals(2, bulkInfo.fileCount)
    assertEquals(14L, bulkInfo.totalSizeBytes)
    assertTrue(bulkInfo.formattedSize.contains("14 B"))

    // Copy multiple files to dest
    val destDir = File(root, "Dest").apply { mkdirs() }
    val copiedCount = FileManagerEngine.copyMultipleFiles(listOf(f1, f2, subFolder), destDir)
    assertEquals(3, copiedCount)
    assertTrue(File(destDir, "test1.txt").exists())
    assertTrue(File(destDir, "test2.txt").exists())
    assertTrue(File(destDir, "Sub/nested.txt").exists())

    // Move multiple files to dest2
    val dest2 = File(root, "Dest2").apply { mkdirs() }
    val movedCount = FileManagerEngine.moveMultipleFiles(listOf(f1, f2), dest2)
    assertEquals(2, movedCount)
    assertFalse(f1.exists())
    assertFalse(f2.exists())
    assertTrue(File(dest2, "test1.txt").exists())
    assertTrue(File(dest2, "test2.txt").exists())

    // Delete multiple files
    val deletedCount = FileManagerEngine.deleteMultipleFiles(listOf(destDir, dest2))
    assertEquals(2, deletedCount)
    assertFalse(destDir.exists())
    assertFalse(dest2.exists())
  }

  @Test
  fun getStorageSummary_computesTotalBytesAndCountCorrectly() = runTest {
    val root = tempFolder.root
    val folder = File(root, "SummaryTest").apply { mkdirs() }
    File(folder, "f1.txt").writeText("12345") // 5 bytes
    File(folder, "f2.txt").writeText("67890") // 5 bytes
    val sub = File(folder, "SubDir").apply { mkdirs() }
    File(sub, "f3.txt").writeText("abc") // 3 bytes

    val (totalBytes, count) = FileManagerEngine.getStorageSummary(folder)
    assertEquals(13L, totalBytes)
    assertEquals(3, count)

    val (blockingBytes, blockingCount) = FileManagerEngine.getStorageSummaryBlocking(folder)
    assertEquals(13L, blockingBytes)
    assertEquals(3, blockingCount)
  }

  @Test
  fun getStorageSummary_cancelsWhenCoroutineIsCancelled() = runTest {
    val root = tempFolder.root
    val folder = File(root, "CancelTest").apply { mkdirs() }
    // Create several nested files
    for (i in 0 until 50) {
      File(folder, "file_$i.txt").writeText("Content $i")
    }

    var wasCancelled = false
    val job = launch {
      try {
        FileManagerEngine.getStorageSummary(folder)
      } catch (_: CancellationException) {
        wasCancelled = true
        throw CancellationException("Cancelled")
      }
    }
    job.cancelAndJoin()
    assertTrue(wasCancelled || job.isCancelled)
  }

  @Test
  fun groupAndSortFiles_groupsByTypeCorrectly() {
    val items = listOf(
      FileItem(File("photo.png"), "photo.png", "/photo.png", false, 5000, 1000, "png", "image/png"),
      FileItem(File("Docs"), "Docs", "/Docs", true, 0, 2000, "", "", 1),
      FileItem(File("notes.txt"), "notes.txt", "/notes.txt", false, 100, 3000, "txt", "text/plain"),
      FileItem(File("song.mp3"), "song.mp3", "/song.mp3", false, 3000, 4000, "mp3", "audio/mpeg"),
      FileItem(File("video.mp4"), "video.mp4", "/video.mp4", false, 8000, 5000, "mp4", "video/mp4"),
      FileItem(File("archive.zip"), "archive.zip", "/archive.zip", false, 6000, 6000, "zip", "application/zip"),
      FileItem(File("script.kt"), "script.kt", "/script.kt", false, 200, 7000, "kt", "text/x-kotlin"),
      FileItem(File("random.dat"), "random.dat", "/random.dat", false, 50, 8000, "dat", "application/octet-stream"),
    )

    val groups = FileManagerEngine.groupAndSortFiles(items, FileGroupOption.TYPE, FileSortOrder.NAME_ASC)
    assertEquals(8, groups.size)
    assertEquals("Folders", groups[0].title)
    assertEquals("Docs", groups[0].items[0].name)
    assertEquals("Images", groups[1].title)
    assertEquals("photo.png", groups[1].items[0].name)
    assertEquals("Videos", groups[2].title)
    assertEquals("video.mp4", groups[2].items[0].name)
    assertEquals("Audio", groups[3].title)
    assertEquals("song.mp3", groups[3].items[0].name)
    assertEquals("Documents", groups[4].title)
    assertEquals(listOf("notes.txt"), groups[4].items.map { it.name })
    assertEquals("Archives", groups[5].title)
    assertEquals("archive.zip", groups[5].items[0].name)
    assertEquals("Code", groups[6].title)
    assertEquals(listOf("script.kt"), groups[6].items.map { it.name })
    assertEquals("Other", groups[7].title)
    assertEquals("random.dat", groups[7].items[0].name)

    // Ensure no file appears in more than one group (which would cause duplicate keys in LazyColumn)
    val allGroupedPaths = groups.flatMap { it.items.map { item -> item.path } }
    assertEquals(items.size, allGroupedPaths.size)
    assertEquals(allGroupedPaths.distinct().size, allGroupedPaths.size)
  }

  @Test
  fun groupAndSortFiles_sampleDocumentsFolder_hasUniqueItemKeysAcrossGroups() {
    val root = tempFolder.newFolder("SampleRoot")
    FileManagerEngine.populateDefaultSampleFiles(root)
    val docsDir = File(root, "Documents")
    val items = FileManagerEngine.listDirectoryContents(docsDir)

    val groups = FileManagerEngine.groupAndSortFiles(items, FileGroupOption.TYPE, FileSortOrder.NAME_ASC)
    val allGroupedPaths = groups.flatMap { it.items.map { item -> item.path } }
    assertEquals(items.size, allGroupedPaths.size)
    assertEquals(allGroupedPaths.distinct().size, allGroupedPaths.size)
  }

  @Test
  fun groupAndSortFiles_groupBySizeAndSortsWithinGroups() {
    val items = listOf(
      FileItem(File("small1.txt"), "small1.txt", "/small1.txt", false, 100, 1000, "txt", "text/plain"),
      FileItem(File("small2.txt"), "small2.txt", "/small2.txt", false, 200, 2000, "txt", "text/plain"),
      FileItem(File("medium.bin"), "medium.bin", "/medium.bin", false, 5L * 1024 * 1024, 3000, "bin", "application/octet-stream"),
      FileItem(File("huge.iso"), "huge.iso", "/huge.iso", false, 150L * 1024 * 1024, 4000, "iso", "application/octet-stream"),
    )

    val groupsAsc = FileManagerEngine.groupAndSortFiles(items, FileGroupOption.SIZE, FileSortOrder.NAME_ASC)
    assertEquals(3, groupsAsc.size)
    assertEquals("Huge (> 100 MB)", groupsAsc[0].title)
    assertEquals("huge.iso", groupsAsc[0].items[0].name)
    assertEquals("Medium (1 – 10 MB)", groupsAsc[1].title)
    assertEquals("medium.bin", groupsAsc[1].items[0].name)
    assertEquals("Small (< 1 MB)", groupsAsc[2].title)
    assertEquals(2, groupsAsc[2].items.size)
    assertEquals("small1.txt", groupsAsc[2].items[0].name)
    assertEquals("small2.txt", groupsAsc[2].items[1].name)

    // Sort descending by name within groups
    val groupsDesc = FileManagerEngine.groupAndSortFiles(items, FileGroupOption.SIZE, FileSortOrder.NAME_DESC)
    assertEquals("small2.txt", groupsDesc[2].items[0].name)
    assertEquals("small1.txt", groupsDesc[2].items[1].name)
  }

  @Test
  fun groupAndSortFiles_noneReturnsSingleEmptyTitleGroup() {
    val items = listOf(
      FileItem(File("z.txt"), "z.txt", "/z.txt", false, 100, 1000, "txt", "text/plain"),
      FileItem(File("a.txt"), "a.txt", "/a.txt", false, 200, 2000, "txt", "text/plain"),
    )

    val groups = FileManagerEngine.groupAndSortFiles(items, FileGroupOption.NONE, FileSortOrder.NAME_ASC)
    assertEquals(1, groups.size)
    assertEquals("", groups[0].title)
    assertEquals("a.txt", groups[0].items[0].name)
    assertEquals("z.txt", groups[0].items[1].name)
  }

  @Test
  fun resolveOpenMimeType_resolvesSpecificMimeAndWildcardForUnknownOnChooseApp() {
    val pdfFile = File("report.pdf")
    val apkFile = File("installer.apk")
    val unknownFile = File("custom.unknownext")

    assertEquals("application/pdf", FileManagerEngine.resolveOpenMimeType(pdfFile, chooseApp = false))
    assertEquals("application/pdf", FileManagerEngine.resolveOpenMimeType(pdfFile, chooseApp = true))
    assertEquals(
      "application/vnd.android.package-archive",
      FileManagerEngine.resolveOpenMimeType(apkFile, chooseApp = false),
    )
    assertEquals(
      "application/octet-stream",
      FileManagerEngine.resolveOpenMimeType(unknownFile, chooseApp = false),
    )
    assertEquals(
      "*/*",
      FileManagerEngine.resolveOpenMimeType(unknownFile, chooseApp = true),
    )
  }

  @Test
  fun folderSortGroupConfig_savesAndResolvesPerFolderAndDefaultConfigs() {
    FileManagerEngine.clearAllSortGroupConfigs()

    val folderA = File("/storage/emulated/0/Downloads")
    val folderB = File("/storage/emulated/0/Pictures")

    // Initially uses default (TYPE, NAME_ASC) and is not saved for specific folder
    val (initialA, isSavedA) = FileManagerEngine.resolveSortGroupConfig(null, folderA)
    assertFalse(isSavedA)
    assertEquals(FileGroupOption.TYPE, initialA.groupOption)
    assertEquals(FileSortOrder.NAME_ASC, initialA.sortOrder)

    // Save specific config for folderA
    val customConfigA = FolderSortGroupConfig(
      groupOption = FileGroupOption.DATE,
      sortOrder = FileSortOrder.DATE_DESC,
    )
    FileManagerEngine.setFolderSortGroupConfig(null, folderA, customConfigA)

    assertTrue(FileManagerEngine.hasFolderSortGroupConfig(null, folderA))
    assertFalse(FileManagerEngine.hasFolderSortGroupConfig(null, folderB))

    val (resolvedA, resolvedSavedA) = FileManagerEngine.resolveSortGroupConfig(null, folderA)
    assertTrue(resolvedSavedA)
    assertEquals(customConfigA, resolvedA)

    // folderB still resolves to the global default
    val (resolvedB, resolvedSavedB) = FileManagerEngine.resolveSortGroupConfig(null, folderB)
    assertFalse(resolvedSavedB)
    assertEquals(FolderSortGroupConfig(FileGroupOption.TYPE, FileSortOrder.NAME_ASC), resolvedB)

    // Changing global default affects folderB but not folderA
    val newDefault = FolderSortGroupConfig(
      groupOption = FileGroupOption.SIZE,
      sortOrder = FileSortOrder.SIZE_DESC,
    )
    FileManagerEngine.setDefaultSortGroupConfig(null, newDefault)
    assertEquals(customConfigA, FileManagerEngine.resolveSortGroupConfig(null, folderA).first)
    assertEquals(newDefault, FileManagerEngine.resolveSortGroupConfig(null, folderB).first)

    // Removing folderA override reverts folderA back to global default
    FileManagerEngine.removeFolderSortGroupConfig(null, folderA)
    assertFalse(FileManagerEngine.hasFolderSortGroupConfig(null, folderA))
    assertEquals(newDefault, FileManagerEngine.resolveSortGroupConfig(null, folderA).first)

    FileManagerEngine.clearAllSortGroupConfigs()
  }

  @Test
  fun isConvertibleFile_detectsSupportedFilesAndRejectsUnsupportedOrDirectories() {
    val dir = File(tempFolder.root, "SubFolder").apply { mkdirs() }
    val png = File(tempFolder.root, "image.png")
    val mp3 = File(tempFolder.root, "track.mp3")
    val md = File(tempFolder.root, "notes.md")
    val zip = File(tempFolder.root, "bundle.zip")
    val apk = File(tempFolder.root, "installer.apk")

    assertFalse(FileManagerEngine.isConvertibleFile(dir))
    assertTrue(FileManagerEngine.isConvertibleFile(png))
    assertTrue(FileManagerEngine.isConvertibleFile(mp3))
    assertTrue(FileManagerEngine.isConvertibleFile(md))
    assertTrue(FileManagerEngine.isConvertibleFile(zip))
    assertFalse(FileManagerEngine.isConvertibleFile(apk))

    val tar = File(tempFolder.root, "backup.tar")
    val tarGz = File(tempFolder.root, "release.tar.gz")
    val tgz = File(tempFolder.root, "bundle.tgz")
    val gz = File(tempFolder.root, "log.gz")
    assertFalse(FileManagerEngine.isSupportedArchiveFile(dir))
    assertTrue(FileManagerEngine.isSupportedArchiveFile(zip))
    assertTrue(FileManagerEngine.isSupportedArchiveFile(tar))
    assertTrue(FileManagerEngine.isSupportedArchiveFile(tarGz))
    assertTrue(FileManagerEngine.isSupportedArchiveFile(tgz))
    assertTrue(FileManagerEngine.isSupportedArchiveFile(gz))
    assertTrue(FileManagerEngine.isSupportedArchiveFile(apk))
    assertFalse(FileManagerEngine.isSupportedArchiveFile(png))
    assertFalse(FileManagerEngine.isSupportedArchiveFile(md))
  }

  @Test
  fun isImageFile_andIsVideoFile_detectSupportedEditorFormats() {
    val dir = File(tempFolder.root, "MediaFolder").apply { mkdirs() }
    val png = File(tempFolder.root, "photo.png")
    val heic = File(tempFolder.root, "photo.heic")
    val mp4 = File(tempFolder.root, "clip.mp4")
    val mov = File(tempFolder.root, "clip.mov")
    val txt = File(tempFolder.root, "readme.txt")

    assertFalse(FileManagerEngine.isImageFile(dir))
    assertTrue(FileManagerEngine.isImageFile(png))
    assertTrue(FileManagerEngine.isImageFile(heic))
    assertFalse(FileManagerEngine.isImageFile(mp4))
    assertFalse(FileManagerEngine.isImageFile(txt))

    assertFalse(FileManagerEngine.isVideoFile(dir))
    assertTrue(FileManagerEngine.isVideoFile(mp4))
    assertTrue(FileManagerEngine.isVideoFile(mov))
    assertFalse(FileManagerEngine.isVideoFile(png))
    assertFalse(FileManagerEngine.isVideoFile(txt))

    val m4a = File(tempFolder.root, "voice.m4a")
    val wav = File(tempFolder.root, "sample.wav")
    assertFalse(FileManagerEngine.isAudioFile(dir))
    assertTrue(FileManagerEngine.isAudioFile(m4a))
    assertTrue(FileManagerEngine.isAudioFile(wav))
    assertTrue(AudioEditorEngine.isSupportedAudioFile(m4a))
    assertTrue(AudioEditorEngine.isSupportedAudioFile(wav))
    assertFalse(AudioEditorEngine.isSupportedAudioFile(dir))
    assertFalse(AudioEditorEngine.isSupportedAudioFile(png))
    assertFalse(AudioEditorEngine.isSupportedAudioFile(txt))
  }

  @Test
  fun backNavigation_goesUpAFolderUnlessAtRoot_andStopsAtRoot() {
    val root = File("/storage/emulated/0")
    val docs = File("/storage/emulated/0/Documents")
    val projects = File("/storage/emulated/0/Documents/Projects")
    val subProject = File("/storage/emulated/0/Documents/Projects/2026")

    assertTrue(FileManagerEngine.isSameOrDescendant(root, root))
    assertTrue(FileManagerEngine.isSameOrDescendant(docs, root))
    assertTrue(FileManagerEngine.isSameOrDescendant(subProject, root))
    assertFalse(FileManagerEngine.isSameOrDescendant(File("/storage/1234-5678"), root))

    // At nested folder: not at root, can navigate up step by step
    assertFalse(FileManagerEngine.isAtRootDirectory(subProject, root))
    assertTrue(FileManagerEngine.canNavigateUp(subProject, root))
    assertEquals(projects, FileManagerEngine.navigateUpDirectory(subProject, root))

    assertFalse(FileManagerEngine.isAtRootDirectory(projects, root))
    assertTrue(FileManagerEngine.canNavigateUp(projects, root))
    assertEquals(docs, FileManagerEngine.navigateUpDirectory(projects, root))

    assertFalse(FileManagerEngine.isAtRootDirectory(docs, root))
    assertTrue(FileManagerEngine.canNavigateUp(docs, root))
    assertEquals(root, FileManagerEngine.navigateUpDirectory(docs, root))

    // Once at root: isAtRootDirectory is true, canNavigateUp is false, navigateUpDirectory returns null
    assertTrue(FileManagerEngine.isAtRootDirectory(root, root))
    assertFalse(FileManagerEngine.canNavigateUp(root, root))
    assertNull(FileManagerEngine.navigateUpDirectory(root, root))
  }
}
