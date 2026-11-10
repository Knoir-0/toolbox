package com.example.toolbox.ui.tools

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
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
  }

  @Test
  fun storageVolumes_defaultsToStorageEmulatedZeroRoot() {
    val rootDir = FileManagerEngine.getUserRootDirectory()
    assertNotNull(rootDir)
    assertEquals("storage/emulated/0", rootDir.path.trimStart('/'))

    val volumes = FileManagerEngine.getStorageVolumes(null)
    assertTrue(volumes.isNotEmpty())
    val primary = volumes.first()
    assertEquals("primary_root", primary.id)
    assertEquals("Home", primary.name)
    assertTrue(primary.isPrimary)
    assertFalse(primary.isRemovable)
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
}
