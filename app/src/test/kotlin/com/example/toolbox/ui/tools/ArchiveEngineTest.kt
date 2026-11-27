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

class ArchiveEngineTest {

  @get:Rule
  val tempFolder = TemporaryFolder()

  @Test
  fun detectFormat_recognizesZipTarTarGzAndGzip() {
    assertEquals(ArchiveFormat.ZIP, ArchiveEngine.detectFormat(File("bundle.zip")))
    assertEquals(ArchiveFormat.ZIP, ArchiveEngine.detectFormat(File("library.jar")))
    assertEquals(ArchiveFormat.ZIP, ArchiveEngine.detectFormat(File("app.apk")))
    assertEquals(ArchiveFormat.TAR, ArchiveEngine.detectFormat(File("backup.tar")))
    assertEquals(ArchiveFormat.TAR_GZ, ArchiveEngine.detectFormat(File("release.tar.gz")))
    assertEquals(ArchiveFormat.TAR_GZ, ArchiveEngine.detectFormat(File("release.tgz")))
    assertEquals(ArchiveFormat.GZIP, ArchiveEngine.detectFormat(File("data.json.gz")))
    assertNull(ArchiveEngine.detectFormat(File("notes.txt")))

    val dir = tempFolder.newFolder("NotAnArchive.zip")
    assertFalse(ArchiveEngine.isSupportedArchive(dir))
    assertTrue(ArchiveEngine.isSupportedArchive(File("bundle.zip")))
  }

  @Test
  fun createArchive_andReadArchive_synthesizesVirtualHierarchyAndComputesMetadata() {
    val root = tempFolder.root
    val srcDir = File(root, "SourceTree").apply { mkdirs() }
    val docsDir = File(srcDir, "Docs").apply { mkdirs() }
    val subDir = File(docsDir, "Specs").apply { mkdirs() }

    val readme = File(srcDir, "README.md").apply { writeText("# Hello Archive\nLine 2") }
    val specFile = File(subDir, "architecture.txt").apply { writeText("Detailed specification content") }
    val scriptFile = File(srcDir, "build.kt").apply { writeText("fun main() = println(\"ok\")") }

    val zipFile = File(root, "output.zip")
    val createResult = ArchiveEngine.createArchive(
      sourceFiles = listOf(readme, docsDir, scriptFile),
      outputFile = zipFile,
      format = ArchiveFormat.ZIP,
      compressionLevel = ArchiveCompressionLevel.BALANCED,
    )

    assertTrue(createResult.success)
    assertTrue(zipFile.exists())
    assertEquals(3, createResult.extractedFilesCount)

    val info = ArchiveEngine.readArchive(zipFile).getOrThrow()
    assertEquals(ArchiveFormat.ZIP, info.format)
    assertEquals(3, info.totalFileCount)
    assertEquals(2, info.totalFolderCount) // "Docs" and "Docs/Specs"

    // Root directory listing should contain Docs, README.md, build.kt
    val rootItems = ArchiveEngine.listVirtualDirectory(info, "")
    assertEquals(listOf("Docs", "build.kt", "README.md"), rootItems.map { it.name })
    val docsEntry = rootItems.first { it.name == "Docs" }
    assertTrue(docsEntry.isDirectory)
    assertEquals(1, docsEntry.childCount)
    assertEquals(specFile.length(), docsEntry.sizeBytes)

    // Navigate into "Docs" -> should contain "Specs"
    val docsChildren = ArchiveEngine.listVirtualDirectory(info, "Docs")
    assertEquals(1, docsChildren.size)
    assertEquals("Specs", docsChildren.first().name)
    assertTrue(docsChildren.first().isDirectory)

    // Navigate into "Docs/Specs" -> should contain "architecture.txt"
    val specsChildren = ArchiveEngine.listVirtualDirectory(info, "Docs/Specs")
    assertEquals(1, specsChildren.size)
    assertEquals("architecture.txt", specsChildren.first().name)
    assertFalse(specsChildren.first().isDirectory)

    // Verify reading text from nested entry
    val readText = ArchiveEngine.readEntryText(zipFile, "Docs/Specs/architecture.txt")
    assertEquals("Detailed specification content", readText)
  }

  @Test
  fun extractAll_andExtractSelectedEntries_extractsAccurately() {
    val root = tempFolder.root
    val sampleArchive = ArchiveEngine.generateSampleArchive(root, "sample.zip")
    assertTrue(sampleArchive.exists())

    // 1. Extract All
    val extractAllDir = File(root, "ExtractedAll")
    val allResult = ArchiveEngine.extractAll(sampleArchive, extractAllDir)
    assertTrue(allResult.success)
    assertEquals(6, allResult.extractedFilesCount)
    assertTrue(File(extractAllDir, "README.md").exists())
    assertTrue(File(extractAllDir, "Documents/release_notes.txt").exists())
    assertTrue(File(extractAllDir, "Documents/Reports/metrics_2026.csv").exists())
    assertTrue(File(extractAllDir, "Source/config.json").exists())
    assertTrue(File(extractAllDir, "Images/badge_preview.png").exists())

    // 2. Extract Selected Folder ("Documents") only
    val extractSelectedDir = File(root, "ExtractedSelected")
    val selResult = ArchiveEngine.extractEntries(
      archiveFile = sampleArchive,
      selectedArchivePaths = setOf("Documents"),
      destinationDir = extractSelectedDir,
      preservePaths = true,
    )
    assertTrue(selResult.success)
    assertEquals(2, selResult.extractedFilesCount)
    assertTrue(File(extractSelectedDir, "Documents/release_notes.txt").exists())
    assertTrue(File(extractSelectedDir, "Documents/Reports/metrics_2026.csv").exists())
    assertFalse(File(extractSelectedDir, "README.md").exists())
  }

  @Test
  fun addRenameCreateFolderAndDeleteEntries_modifiesArchiveInPlace() {
    val root = tempFolder.root
    val f1 = File(root, "alpha.txt").apply { writeText("Alpha content") }
    val zipFile = File(root, "editable.zip")

    assertTrue(ArchiveEngine.createArchive(listOf(f1), zipFile, ArchiveFormat.ZIP).success)

    // 1. Create a new folder inside the archive
    val folderRes = ArchiveEngine.createFolderInArchive(zipFile, "", "Extras")
    assertTrue(folderRes.success)

    // 2. Add a file into "Extras"
    val f2 = File(root, "beta.json").apply { writeText("{\"beta\":true}") }
    val addRes = ArchiveEngine.addFilesToArchive(zipFile, listOf(f2), targetVirtualDir = "Extras")
    assertTrue(addRes.success)

    var info = ArchiveEngine.readArchive(zipFile).getOrThrow()
    assertEquals("{\"beta\":true}", ArchiveEngine.readEntryText(zipFile, "Extras/beta.json"))

    // 3. Rename "Extras/beta.json" -> "Extras/gamma.json"
    val renameRes = ArchiveEngine.renameEntryInArchive(zipFile, "Extras/beta.json", "gamma.json")
    assertTrue(renameRes.success)
    info = ArchiveEngine.readArchive(zipFile).getOrThrow()
    assertNull(ArchiveEngine.readEntryText(zipFile, "Extras/beta.json"))
    assertEquals("{\"beta\":true}", ArchiveEngine.readEntryText(zipFile, "Extras/gamma.json"))

    // 4. Delete "Extras" folder and its contents
    val deleteRes = ArchiveEngine.deleteEntriesFromArchive(zipFile, setOf("Extras"))
    assertTrue(deleteRes.success)
    info = ArchiveEngine.readArchive(zipFile).getOrThrow()
    assertEquals(1, info.totalFileCount)
    assertEquals(0, info.totalFolderCount)
    assertEquals("Alpha content", ArchiveEngine.readEntryText(zipFile, "alpha.txt"))
  }

  @Test
  fun tarAndTarGz_roundTripCreateReadAndExtract() {
    val root = tempFolder.root
    val dir = File(root, "Data").apply { mkdirs() }
    val file1 = File(dir, "one.txt").apply { writeText("First entry") }
    val file2 = File(dir, "two.txt").apply { writeText("Second entry") }

    for (format in listOf(ArchiveFormat.TAR, ArchiveFormat.TAR_GZ)) {
      val archiveFile = File(root, "bundle.${format.extension}")
      val createRes = ArchiveEngine.createArchive(listOf(dir), archiveFile, format)
      assertTrue("Create failed for $format", createRes.success)

      val info = ArchiveEngine.readArchive(archiveFile).getOrThrow()
      assertEquals(format, info.format)
      assertEquals(2, info.totalFileCount)
      assertEquals("First entry", ArchiveEngine.readEntryText(archiveFile, "Data/one.txt"))
      assertEquals("Second entry", ArchiveEngine.readEntryText(archiveFile, "Data/two.txt"))

      val extractDir = File(root, "Out_${format.name}")
      val extractRes = ArchiveEngine.extractAll(archiveFile, extractDir)
      assertTrue(extractRes.success)
      assertEquals("First entry", File(extractDir, "Data/one.txt").readText())
      assertEquals("Second entry", File(extractDir, "Data/two.txt").readText())
    }
  }

  @Test
  fun groupAndSortEntries_andBreadcrumbs_matchExplorerBehavior() {
    val root = tempFolder.root
    val sample = ArchiveEngine.generateSampleArchive(root)
    val info = ArchiveEngine.readArchive(sample).getOrThrow()

    val rootEntries = ArchiveEngine.listVirtualDirectory(info, "")
    val grouped = ArchiveEngine.groupAndSortEntries(rootEntries, FileGroupOption.TYPE, FileSortOrder.NAME_ASC)
    assertEquals("Folders", grouped.first().title)
    assertEquals(listOf("Documents", "Images", "Source"), grouped.first().items.map { it.name })

    // Breadcrumbs & Up navigation
    val crumbs = ArchiveEngine.getBreadcrumbAncestors("sample.zip", "Documents/Reports")
    assertEquals(
      listOf(
        "sample.zip" to "",
        "Documents" to "Documents",
        "Reports" to "Documents/Reports",
      ),
      crumbs,
    )
    assertTrue(ArchiveEngine.canNavigateUp("Documents/Reports"))
    assertEquals("Documents", ArchiveEngine.navigateUpVirtualDirectory("Documents/Reports"))
    assertEquals("", ArchiveEngine.navigateUpVirtualDirectory("Documents"))
    assertFalse(ArchiveEngine.canNavigateUp(""))
    assertNull(ArchiveEngine.navigateUpVirtualDirectory(""))
  }

  @Test
  fun isSafeExtractTarget_blocksPathTraversalZipSlip() {
    val destDir = tempFolder.newFolder("SafeDest")
    assertTrue(ArchiveEngine.isSafeExtractTarget(destDir, "Documents/notes.txt"))
    assertFalse(ArchiveEngine.isSafeExtractTarget(destDir, "../outside.txt"))
    assertFalse(ArchiveEngine.isSafeExtractTarget(destDir, "Documents/../../evil.sh"))
  }

  @Test
  fun aes256EncryptedZip_createVerifyReadAndExtract_roundTripsAndRejectsWrongPassword() {
    val root = tempFolder.root
    val secretDir = File(root, "Vault").apply { mkdirs() }
    File(secretDir, "credentials.txt").writeText("api_key=super-secret-token-2026\n".repeat(12))
    File(secretDir, "empty.txt").writeBytes(ByteArray(0))
    val rawBinary = ByteArray(1024) { idx -> ((idx * 37 + 13) and 0xFF).toByte() }
    val binFile = File(root, "payload.bin").apply { writeBytes(rawBinary) }

    val encryptedZip = File(root, "vault_aes256.zip")
    val createRes = ArchiveEngine.createArchive(
      sourceFiles = listOf(secretDir, binFile),
      outputFile = encryptedZip,
      format = ArchiveFormat.ZIP,
      compressionLevel = ArchiveCompressionLevel.BALANCED,
      password = "Correct-Horse-Battery-Staple!🔒",
      encryptionMethod = ArchiveEncryptionMethod.AES_256,
    )
    assertTrue(createRes.success)
    assertTrue(encryptedZip.exists())

    // Metadata & hierarchy are readable without a password, flagging encryption accurately
    val info = ArchiveEngine.readArchive(encryptedZip).getOrThrow()
    assertTrue(info.isEncrypted)
    assertEquals(ArchiveEncryptionMethod.AES_256, info.encryptionMethod)
    assertEquals(3, info.totalFileCount)
    assertTrue(info.allEntries.filter { !it.isDirectory }.all { it.isEncrypted })

    // Password verification
    assertFalse(ArchiveEngine.verifyArchivePassword(encryptedZip, "").getOrThrow())
    assertFalse(ArchiveEngine.verifyArchivePassword(encryptedZip, "wrong-password").getOrThrow())
    assertTrue(ArchiveEngine.verifyArchivePassword(encryptedZip, "Correct-Horse-Battery-Staple!🔒").getOrThrow())

    // Missing or wrong password blocks reading and extracting
    assertNull(ArchiveEngine.readEntryText(encryptedZip, "Vault/credentials.txt"))
    assertNull(ArchiveEngine.readEntryText(encryptedZip, "Vault/credentials.txt", password = "bad"))

    val missingPwExtract = ArchiveEngine.extractAll(encryptedZip, File(root, "FailMissing"))
    assertFalse(missingPwExtract.success)
    assertTrue(missingPwExtract.passwordRequired)

    val wrongPwExtract = ArchiveEngine.extractAll(encryptedZip, File(root, "FailWrong"), password = "wrong")
    assertFalse(wrongPwExtract.success)
    assertTrue(wrongPwExtract.invalidPassword)

    // Correct password reads entries and extracts byte-for-byte
    assertEquals(
      "api_key=super-secret-token-2026\n".repeat(12),
      ArchiveEngine.readEntryText(encryptedZip, "Vault/credentials.txt", password = "Correct-Horse-Battery-Staple!🔒"),
    )
    val okDir = File(root, "UnlockedOut")
    val okExtract = ArchiveEngine.extractAll(
      archiveFile = encryptedZip,
      destinationDir = okDir,
      password = "Correct-Horse-Battery-Staple!🔒",
    )
    assertTrue(okExtract.success)
    assertEquals(3, okExtract.extractedFilesCount)
    assertEquals("", File(okDir, "Vault/empty.txt").readText())
    assertTrue(rawBinary.contentEquals(File(okDir, "payload.bin").readBytes()))
  }

  @Test
  fun zipCryptoEncryptedZip_storeAndDeflate_roundTripsAndRejectsWrongPassword() {
    val root = tempFolder.root
    val note = File(root, "legacy_note.txt").apply {
      writeText("Legacy ZipCrypto compatibility test payload ".repeat(20))
    }

    for (level in listOf(ArchiveCompressionLevel.STORE, ArchiveCompressionLevel.MAXIMUM)) {
      val zipFile = File(root, "legacy_${level.name}.zip")
      val res = ArchiveEngine.createArchive(
        sourceFiles = listOf(note),
        outputFile = zipFile,
        format = ArchiveFormat.ZIP,
        compressionLevel = level,
        password = "p@ssw0rd123",
        encryptionMethod = ArchiveEncryptionMethod.ZIP_CRYPTO,
      )
      assertTrue(res.success)

      val info = ArchiveEngine.readArchive(zipFile).getOrThrow()
      assertTrue(info.isEncrypted)
      assertEquals(ArchiveEncryptionMethod.ZIP_CRYPTO, info.encryptionMethod)

      assertFalse(ArchiveEngine.verifyArchivePassword(zipFile, "wrong").getOrThrow())
      assertTrue(ArchiveEngine.verifyArchivePassword(zipFile, "p@ssw0rd123").getOrThrow())
      assertEquals(
        note.readText(),
        ArchiveEngine.readEntryText(zipFile, "legacy_note.txt", password = "p@ssw0rd123"),
      )
    }
  }

  @Test
  fun encryptedZip_inPlaceModificationsAndPasswordUpdates_preserveEncryptionAndIntegrity() {
    val root = tempFolder.root
    val f1 = File(root, "one.txt").apply { writeText("Initial secret 1") }
    val zipFile = File(root, "mutable_encrypted.zip")

    // Start unencrypted, then encrypt in-place with AES-256
    assertTrue(ArchiveEngine.createArchive(listOf(f1), zipFile, ArchiveFormat.ZIP).success)
    assertFalse(ArchiveEngine.readArchive(zipFile).getOrThrow().isEncrypted)

    val encRes = ArchiveEngine.updateArchivePassword(
      archiveFile = zipFile,
      currentPassword = null,
      newPassword = "pw1",
      encryptionMethod = ArchiveEncryptionMethod.AES_256,
    )
    assertTrue(encRes.success)
    assertEquals(ArchiveEncryptionMethod.AES_256, ArchiveEngine.readArchive(zipFile).getOrThrow().encryptionMethod)

    // Without password, adding/modifying is rejected
    val f2 = File(root, "two.txt").apply { writeText("Added secret 2") }
    assertFalse(ArchiveEngine.addFilesToArchive(zipFile, listOf(f2)).success)

    // With password, create folder, add file, rename, and delete preserve AES-256 encryption
    assertTrue(ArchiveEngine.createFolderInArchive(zipFile, "", "Sub", password = "pw1").success)
    assertTrue(ArchiveEngine.addFilesToArchive(zipFile, listOf(f2), targetVirtualDir = "Sub", password = "pw1").success)
    assertTrue(ArchiveEngine.renameEntryInArchive(zipFile, "Sub/two.txt", "renamed.txt", password = "pw1").success)

    var info = ArchiveEngine.readArchive(zipFile).getOrThrow()
    assertTrue(info.isEncrypted)
    assertEquals(ArchiveEncryptionMethod.AES_256, info.encryptionMethod)
    assertEquals("Added secret 2", ArchiveEngine.readEntryText(zipFile, "Sub/renamed.txt", password = "pw1"))

    // Change password & switch method to ZipCrypto
    val rekeyRes = ArchiveEngine.updateArchivePassword(
      archiveFile = zipFile,
      currentPassword = "pw1",
      newPassword = "pw2",
      encryptionMethod = ArchiveEncryptionMethod.ZIP_CRYPTO,
    )
    assertTrue(rekeyRes.success)
    assertFalse(ArchiveEngine.verifyArchivePassword(zipFile, "pw1").getOrThrow())
    assertTrue(ArchiveEngine.verifyArchivePassword(zipFile, "pw2").getOrThrow())
    assertEquals(ArchiveEncryptionMethod.ZIP_CRYPTO, ArchiveEngine.readArchive(zipFile).getOrThrow().encryptionMethod)

    // Remove password protection completely
    val removePwRes = ArchiveEngine.updateArchivePassword(
      archiveFile = zipFile,
      currentPassword = "pw2",
      newPassword = null,
      encryptionMethod = ArchiveEncryptionMethod.NONE,
    )
    assertTrue(removePwRes.success)
    info = ArchiveEngine.readArchive(zipFile).getOrThrow()
    assertFalse(info.isEncrypted)
    assertEquals("Initial secret 1", ArchiveEngine.readEntryText(zipFile, "one.txt"))
    assertEquals("Added secret 2", ArchiveEngine.readEntryText(zipFile, "Sub/renamed.txt"))
  }
}
