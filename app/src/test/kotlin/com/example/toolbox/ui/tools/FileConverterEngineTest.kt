package com.example.toolbox.ui.tools

import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.ToolCategory
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class FileConverterEngineTest {

  private lateinit var tempDir: File

  @Before
  fun setUp() {
    tempDir = Files.createTempDirectory("file_converter_test_").toFile()
  }

  @After
  fun tearDown() {
    tempDir.deleteRecursively()
  }

  @Test
  fun toolEnum_includesFileConverterUnderFilesAndStorage() {
    assertEquals("Convert", Tool.FILE_CONVERTER.title)
    assertEquals(ToolCategory.FILES_AND_STORAGE, Tool.FILE_CONVERTER.category)
  }

  @Test
  fun detectFormat_recognizesPrimaryExtensionsAndAliases() {
    assertEquals(ConvertibleFormat.PNG, ConvertibleFormat.fromFileName("photo.png"))
    assertEquals(ConvertibleFormat.JPG, ConvertibleFormat.fromFileName("photo.jpg"))
    assertEquals(ConvertibleFormat.JPG, ConvertibleFormat.fromFileName("photo.JPEG"))
    assertEquals(ConvertibleFormat.WEBP, ConvertibleFormat.fromFileName("image.webp"))
    assertEquals(ConvertibleFormat.HEIC, ConvertibleFormat.fromFileName("apple.heif"))
    assertEquals(ConvertibleFormat.TIFF, ConvertibleFormat.fromFileName("scan.tif"))
    assertEquals(ConvertibleFormat.MP3, ConvertibleFormat.fromFileName("song.mp3"))
    assertEquals(ConvertibleFormat.WAV, ConvertibleFormat.fromFileName("voice.wav"))
    assertEquals(ConvertibleFormat.M4A, ConvertibleFormat.fromFileName("recording.m4a"))
    assertEquals(ConvertibleFormat.THREE_GPP, ConvertibleFormat.fromFileName("clip.amr"))
    assertEquals(ConvertibleFormat.MP4, ConvertibleFormat.fromFileName("movie.mp4"))
    assertEquals(ConvertibleFormat.MKV, ConvertibleFormat.fromFileName("video.mkv"))
    assertEquals(ConvertibleFormat.MPEG, ConvertibleFormat.fromFileName("clip.mpg"))
    assertEquals(ConvertibleFormat.PDF, ConvertibleFormat.fromFileName("report.pdf"))
    assertEquals(ConvertibleFormat.HTML, ConvertibleFormat.fromFileName("index.htm"))
    assertEquals(ConvertibleFormat.TAR_GZ, ConvertibleFormat.fromFileName("backup.tar.gz"))
    assertEquals(ConvertibleFormat.TAR_GZ, ConvertibleFormat.fromFileName("backup.tgz"))
    assertNull(ConvertibleFormat.fromFileName("unknown.xyz"))
    assertNull(ConvertibleFormat.fromFileName("no_extension"))
  }

  @Test
  fun extractFileStem_handlesSingleAndDoubleExtensions() {
    assertEquals("vacation_photo", FileConverterEngine.extractFileStem("vacation_photo.jpg"))
    assertEquals("project_backup", FileConverterEngine.extractFileStem("project_backup.tar.gz"))
    assertEquals("archive.v1", FileConverterEngine.extractFileStem("archive.v1.zip"))
  }

  @Test
  fun capabilityMap_coversAllFormatsAndPrefersNativeWhereSupported() {
    val map = FileConverterEngine.CONVERSION_CAPABILITY_MAP
    assertTrue("Should have over 200 total conversion routes", FileConverterEngine.totalConversionRoutesCount >= 200)

    for (format in ConvertibleFormat.entries) {
      val routes = FileConverterEngine.getAvailableRoutes(format)
      assertTrue("Format ${format.name} must have conversion targets", routes.isNotEmpty())
      assertFalse("Format ${format.name} should not convert to itself", routes.any { it.target == format })
    }

    // Native primary + FFmpeg backup for standard images
    val pngToJpg = FileConverterEngine.getRoute(ConvertibleFormat.PNG, ConvertibleFormat.JPG)
    assertNotNull(pngToJpg)
    assertEquals(ConverterBackend.NATIVE, pngToJpg!!.preferredBackend)
    assertEquals(ConverterBackend.FFMPEG, pngToJpg.fallbackBackend)

    // Native primary + FFmpeg backup for Image -> PDF
    val jpgToPdf = FileConverterEngine.getRoute(ConvertibleFormat.JPG, ConvertibleFormat.PDF)
    assertNotNull(jpgToPdf)
    assertEquals(ConverterBackend.NATIVE, jpgToPdf!!.preferredBackend)
    assertEquals(ConverterBackend.FFMPEG, jpgToPdf.fallbackBackend)

    // Native primary for PDF -> PNG
    val pdfToPng = FileConverterEngine.getRoute(ConvertibleFormat.PDF, ConvertibleFormat.PNG)
    assertNotNull(pdfToPng)
    assertEquals(ConverterBackend.NATIVE, pdfToPng!!.preferredBackend)

    // Native primary + FFmpeg backup for MP3 -> WAV and MP3 -> M4A
    val mp3ToWav = FileConverterEngine.getRoute(ConvertibleFormat.MP3, ConvertibleFormat.WAV)
    assertNotNull(mp3ToWav)
    assertEquals(ConverterBackend.NATIVE, mp3ToWav!!.preferredBackend)
    assertEquals(ConverterBackend.FFMPEG, mp3ToWav.fallbackBackend)

    // FFmpeg primary for WAV -> MP3 and WAV -> FLAC
    val wavToMp3 = FileConverterEngine.getRoute(ConvertibleFormat.WAV, ConvertibleFormat.MP3)
    assertNotNull(wavToMp3)
    assertEquals(ConverterBackend.FFMPEG, wavToMp3!!.preferredBackend)

    // Native Media3 primary + FFmpeg backup for MKV -> MP4
    val mkvToMp4 = FileConverterEngine.getRoute(ConvertibleFormat.MKV, ConvertibleFormat.MP4)
    assertNotNull(mkvToMp4)
    assertEquals(ConverterBackend.NATIVE, mkvToMp4!!.preferredBackend)
    assertEquals(ConverterBackend.FFMPEG, mkvToMp4.fallbackBackend)

    // FFmpeg primary for MP4 -> MKV and MP4 -> GIF
    val mp4ToMkv = FileConverterEngine.getRoute(ConvertibleFormat.MP4, ConvertibleFormat.MKV)
    assertNotNull(mp4ToMkv)
    assertEquals(ConverterBackend.FFMPEG, mp4ToMkv!!.preferredBackend)

    val mp4ToGif = FileConverterEngine.getRoute(ConvertibleFormat.MP4, ConvertibleFormat.GIF)
    assertNotNull(mp4ToGif)
    assertEquals(ConverterBackend.FFMPEG, mp4ToGif!!.preferredBackend)
    assertEquals(ConverterBackend.NATIVE, mp4ToGif.fallbackBackend)
  }

  @Test
  fun selectBackend_dynamicallySwitchesToFallbackWhenPrimaryUnavailable() {
    // PNG -> JPG: Native preferred, FFmpeg fallback
    assertEquals(
      ConverterBackend.NATIVE,
      FileConverterEngine.selectBackend(ConvertibleFormat.PNG, ConvertibleFormat.JPG, nativeAvailable = true, ffmpegAvailable = true),
    )
    assertEquals(
      ConverterBackend.FFMPEG,
      FileConverterEngine.selectBackend(ConvertibleFormat.PNG, ConvertibleFormat.JPG, nativeAvailable = false, ffmpegAvailable = true),
    )
    assertNull(
      FileConverterEngine.selectBackend(ConvertibleFormat.PNG, ConvertibleFormat.JPG, nativeAvailable = false, ffmpegAvailable = false),
    )

    // WAV -> MP3: FFmpeg only
    assertEquals(
      ConverterBackend.FFMPEG,
      FileConverterEngine.selectBackend(ConvertibleFormat.WAV, ConvertibleFormat.MP3, nativeAvailable = true, ffmpegAvailable = true),
    )
    assertNull(
      FileConverterEngine.selectBackend(ConvertibleFormat.WAV, ConvertibleFormat.MP3, nativeAvailable = true, ffmpegAvailable = false),
    )
  }

  @Test
  fun convertFile_structuredDataRoundTrip_csvJsonXmlTsvHtmlMd() = runTest {
    val csvFile = File(tempDir, "employees.csv")
    csvFile.writeText("name,role,score\nAlice,Engineer,95\nBob,Designer,88\n")

    // CSV -> JSON
    val toJson = FileConverterEngine.convertFile(
      context = null,
      sourceFile = csvFile,
      targetFormat = ConvertibleFormat.JSON,
      outputDir = tempDir,
    )
    assertTrue(toJson.success)
    assertEquals(ConverterBackend.NATIVE, toJson.backendUsed)
    assertFalse(toJson.usedFallback)
    val jsonFile = toJson.outputFile!!
    val jsonContent = jsonFile.readText()
    assertTrue(jsonContent.contains("\"Alice\""))
    assertTrue(jsonContent.contains("\"Engineer\""))

    // JSON -> XML
    val toXml = FileConverterEngine.convertFile(
      context = null,
      sourceFile = jsonFile,
      targetFormat = ConvertibleFormat.XML,
      outputDir = tempDir,
    )
    assertTrue(toXml.success)
    val xmlFile = toXml.outputFile!!
    val xmlContent = xmlFile.readText()
    assertTrue(xmlContent.contains("<name>Alice</name>"))

    // XML -> TSV
    val toTsv = FileConverterEngine.convertFile(
      context = null,
      sourceFile = xmlFile,
      targetFormat = ConvertibleFormat.TSV,
      outputDir = tempDir,
    )
    assertTrue(toTsv.success)
    val tsvContent = toTsv.outputFile!!.readText()
    assertTrue(tsvContent.contains("Alice"))
    assertTrue(tsvContent.contains("Engineer"))
    assertTrue(tsvContent.contains("95"))
    assertTrue(tsvContent.contains("\t"))

    // CSV -> Markdown Table
    val toMd = FileConverterEngine.convertFile(
      context = null,
      sourceFile = csvFile,
      targetFormat = ConvertibleFormat.MD,
      outputDir = tempDir,
    )
    assertTrue(toMd.success)
    assertTrue(toMd.outputFile!!.readText().contains("| Alice | Engineer | 95 |"))
  }

  @Test
  fun convertFile_markdownHtmlTxtConversions() = runTest {
    val mdFile = File(tempDir, "notes.md")
    mdFile.writeText("# Project Title\n\n- First item\n- Second item\n")

    val toHtml = FileConverterEngine.convertFile(
      context = null,
      sourceFile = mdFile,
      targetFormat = ConvertibleFormat.HTML,
      outputDir = tempDir,
    )
    assertTrue(toHtml.success)
    val htmlFile = toHtml.outputFile!!
    val htmlText = htmlFile.readText()
    assertTrue(htmlText.contains("<h1>Project Title</h1>"))
    assertTrue(htmlText.contains("<li>First item</li>"))

    val toTxt = FileConverterEngine.convertFile(
      context = null,
      sourceFile = htmlFile,
      targetFormat = ConvertibleFormat.TXT,
      outputDir = tempDir,
    )
    assertTrue(toTxt.success)
    val plainText = toTxt.outputFile!!.readText()
    assertTrue(plainText.contains("Project Title"))
    assertTrue(plainText.contains("First item"))
  }

  @Test
  fun convertFile_archiveRoundTrip_zipToTarToTarGzToZip() = runTest {
    val zipFile = File(tempDir, "bundle.zip")
    ZipOutputStream(zipFile.outputStream()).use { zos ->
      zos.putNextEntry(ZipEntry("hello.txt"))
      zos.write("Hello Archive World!".toByteArray(Charsets.UTF_8))
      zos.closeEntry()
      zos.putNextEntry(ZipEntry("nested/data.json"))
      zos.write("{\"ok\":true}".toByteArray(Charsets.UTF_8))
      zos.closeEntry()
    }

    // ZIP -> TAR
    val toTar = FileConverterEngine.convertFile(
      context = null,
      sourceFile = zipFile,
      targetFormat = ConvertibleFormat.TAR,
      outputDir = tempDir,
    )
    assertTrue(toTar.success)
    assertEquals(ConverterBackend.NATIVE, toTar.backendUsed)
    val tarFile = toTar.outputFile!!
    assertTrue(tarFile.exists())

    // TAR -> TAR.GZ
    val toTarGz = FileConverterEngine.convertFile(
      context = null,
      sourceFile = tarFile,
      targetFormat = ConvertibleFormat.TAR_GZ,
      outputDir = tempDir,
    )
    assertTrue(toTarGz.success)
    val tarGzFile = toTarGz.outputFile!!
    assertTrue(tarGzFile.name.endsWith(".tar.gz"))

    // TAR.GZ -> ZIP
    val backToZip = FileConverterEngine.convertFile(
      context = null,
      sourceFile = tarGzFile,
      targetFormat = ConvertibleFormat.ZIP,
      outputDir = tempDir,
      desiredStem = "restored_bundle",
    )
    assertTrue(backToZip.success)
    assertEquals("restored_bundle.zip", backToZip.outputFile!!.name)
  }

  @Test
  fun convertFile_whenNativeFails_automaticallyUsesFfmpegBackup() = runTest {
    // In JVM unit tests without Robolectric, BitmapFactory.decodeFile returns null so Native PNG->JPG fails;
    // verify that convertFile automatically falls back to FFmpeg and succeeds when FFmpeg runner produces output!
    val dummyPng = File(tempDir, "sample.png").apply {
      writeBytes(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47))
    }

    var capturedFfmpegArgs: List<String>? = null
    val result = FileConverterEngine.convertFile(
      context = null,
      sourceFile = dummyPng,
      targetFormat = ConvertibleFormat.JPG,
      outputDir = tempDir,
      ffmpegRunner = { args, outFile ->
        capturedFfmpegArgs = args
        outFile.writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte()))
        true
      },
    )

    assertTrue(result.success)
    assertEquals(ConverterBackend.FFMPEG, result.backendUsed)
    assertTrue("Should indicate that fallback backend was used", result.usedFallback)
    assertNotNull(capturedFfmpegArgs)
    assertTrue(capturedFfmpegArgs!!.contains("-i"))
    assertEquals("sample.jpg", result.outputFile?.name)
  }

  @Test
  fun isSupportedFile_returnsTrueForSupportedFiletypes_andFalseForDirectoriesOrUnsupported() {
    assertFalse(FileConverterEngine.isSupportedFile(null))
    assertFalse(FileConverterEngine.isSupportedFile(tempDir))

    val pngFile = File(tempDir, "photo.png")
    val mp4File = File(tempDir, "clip.mp4")
    val jsonFile = File(tempDir, "config.json")
    val tarGzFile = File(tempDir, "archive.tar.gz")
    val unsupportedFile = File(tempDir, "binary.unknownext")
    val noExtFile = File(tempDir, "README")

    assertTrue(FileConverterEngine.isSupportedFile(pngFile))
    assertTrue(FileConverterEngine.isSupportedFile(mp4File))
    assertTrue(FileConverterEngine.isSupportedFile(jsonFile))
    assertTrue(FileConverterEngine.isSupportedFile(tarGzFile))
    assertFalse(FileConverterEngine.isSupportedFile(unsupportedFile))
    assertFalse(FileConverterEngine.isSupportedFile(noExtFile))
  }

  @Test
  fun capabilityMap_includesOcrRoutesForImagesAndPdfsToTextMarkupAndStructuredData() {
    val ocrTargets = listOf(
      ConvertibleFormat.TXT,
      ConvertibleFormat.MD,
      ConvertibleFormat.HTML,
      ConvertibleFormat.JSON,
      ConvertibleFormat.CSV,
      ConvertibleFormat.TSV,
      ConvertibleFormat.XML,
    )

    // Standard decodable images use NATIVE preferred + FFMPEG fallback for OCR
    for (target in ocrTargets) {
      val pngRoute = FileConverterEngine.getRoute(ConvertibleFormat.PNG, target)
      assertNotNull("Expected PNG -> ${target.name} OCR route", pngRoute)
      assertEquals(ConverterBackend.NATIVE, pngRoute!!.preferredBackend)
      assertEquals(ConverterBackend.FFMPEG, pngRoute.fallbackBackend)

      val jpgRoute = FileConverterEngine.getRoute(ConvertibleFormat.JPG, target)
      assertNotNull("Expected JPG -> ${target.name} OCR route", jpgRoute)
      assertEquals(ConverterBackend.NATIVE, jpgRoute!!.preferredBackend)

      // TIFF uses FFMPEG decode + ML Kit OCR
      val tiffRoute = FileConverterEngine.getRoute(ConvertibleFormat.TIFF, target)
      assertNotNull("Expected TIFF -> ${target.name} OCR route", tiffRoute)
      assertEquals(ConverterBackend.FFMPEG, tiffRoute!!.preferredBackend)

      // PDF uses NATIVE PdfRenderer + ML Kit OCR
      val pdfRoute = FileConverterEngine.getRoute(ConvertibleFormat.PDF, target)
      assertNotNull("Expected PDF -> ${target.name} OCR route", pdfRoute)
      assertEquals(ConverterBackend.NATIVE, pdfRoute!!.preferredBackend)
    }
  }

  @Test
  fun convertFile_imageOcrToTextMarkupAndStructuredData() = runTest {
    val imageFile = File(tempDir, "receipt_scan.png").apply {
      writeBytes(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47))
    }

    val mockOcrDoc = CameraEngine.analyzeScannedDocumentText(
      rawText = "Grocery Receipt\nApples $3.50\nCoffee Beans $12.00\nTotal $15.50",
      blocks = listOf(
        CameraScannedTextBlock(
          text = "Grocery Receipt",
          lines = listOf("Grocery Receipt"),
        ),
        CameraScannedTextBlock(
          text = "Apples $3.50\nCoffee Beans $12.00\nTotal $15.50",
          lines = listOf("Apples $3.50", "Coffee Beans $12.00", "Total $15.50"),
        ),
      ),
      fallbackTitle = "receipt_scan",
    )

    val ocrRunner: suspend (File, ConvertibleFormat) -> List<CameraScannedDocument> = { _, _ ->
      listOf(mockOcrDoc)
    }

    // 1. Image -> TXT
    val toTxt = FileConverterEngine.convertFile(
      context = null,
      sourceFile = imageFile,
      targetFormat = ConvertibleFormat.TXT,
      outputDir = tempDir,
      ocrRunner = ocrRunner,
    )
    assertTrue(toTxt.success)
    val txtContent = toTxt.outputFile!!.readText()
    assertTrue(txtContent.contains("Grocery Receipt"))
    assertTrue(txtContent.contains("Coffee Beans $12.00"))

    // 2. Image -> MD
    val toMd = FileConverterEngine.convertFile(
      context = null,
      sourceFile = imageFile,
      targetFormat = ConvertibleFormat.MD,
      outputDir = tempDir,
      ocrRunner = ocrRunner,
    )
    assertTrue(toMd.success)
    val mdContent = toMd.outputFile!!.readText()
    assertTrue(mdContent.contains("# Grocery Receipt"))
    assertTrue(mdContent.contains("Total $15.50"))

    // 3. Image -> HTML
    val toHtml = FileConverterEngine.convertFile(
      context = null,
      sourceFile = imageFile,
      targetFormat = ConvertibleFormat.HTML,
      outputDir = tempDir,
      ocrRunner = ocrRunner,
    )
    assertTrue(toHtml.success)
    val htmlContent = toHtml.outputFile!!.readText()
    assertTrue(htmlContent.contains("<h1>Grocery Receipt</h1>"))
    assertTrue(htmlContent.contains("Apples $3.50"))

    // 4. Image -> JSON
    val toJson = FileConverterEngine.convertFile(
      context = null,
      sourceFile = imageFile,
      targetFormat = ConvertibleFormat.JSON,
      outputDir = tempDir,
      ocrRunner = ocrRunner,
    )
    assertTrue(toJson.success)
    val jsonContent = toJson.outputFile!!.readText()
    assertTrue(jsonContent.contains("\"Grocery Receipt\""))
    assertTrue(jsonContent.contains("\"Coffee Beans $12.00\""))

    // 5. Image -> CSV & TSV & XML
    for (structuredFormat in listOf(ConvertibleFormat.CSV, ConvertibleFormat.TSV, ConvertibleFormat.XML)) {
      val res = FileConverterEngine.convertFile(
        context = null,
        sourceFile = imageFile,
        targetFormat = structuredFormat,
        outputDir = tempDir,
        ocrRunner = ocrRunner,
      )
      assertTrue("Expected PNG -> ${structuredFormat.name} to succeed", res.success)
      val text = res.outputFile!!.readText()
      assertTrue(text.contains("Grocery Receipt"))
      assertTrue(text.contains("Total $15.50"))
    }
  }

  @Test
  fun convertFile_pdfOcrMultiPageToTextMarkupAndStructuredData() = runTest {
    val pdfFile = File(tempDir, "handbook.pdf").apply {
      writeBytes("%PDF-1.4 mock".toByteArray(Charsets.UTF_8))
    }

    val page1 = CameraEngine.analyzeScannedDocumentText(
      rawText = "Chapter 1: Getting Started\nWelcome to the handbook.",
      fallbackTitle = "Page 1",
    )
    val page2 = CameraEngine.analyzeScannedDocumentText(
      rawText = "Chapter 2: Advanced Usage\nConfigure settings and shortcuts.",
      fallbackTitle = "Page 2",
    )

    val ocrRunner: suspend (File, ConvertibleFormat) -> List<CameraScannedDocument> = { _, _ ->
      listOf(page1, page2)
    }

    // PDF -> TXT (multi-page markers)
    val toTxt = FileConverterEngine.convertFile(
      context = null,
      sourceFile = pdfFile,
      targetFormat = ConvertibleFormat.TXT,
      outputDir = tempDir,
      ocrRunner = ocrRunner,
    )
    assertTrue(toTxt.success)
    val txtContent = toTxt.outputFile!!.readText()
    assertTrue(txtContent.contains("--- Page 1 ---"))
    assertTrue(txtContent.contains("--- Page 2 ---"))
    assertTrue(txtContent.contains("Welcome to the handbook."))
    assertTrue(txtContent.contains("Configure settings and shortcuts."))

    // PDF -> MD (multi-page headings)
    val toMd = FileConverterEngine.convertFile(
      context = null,
      sourceFile = pdfFile,
      targetFormat = ConvertibleFormat.MD,
      outputDir = tempDir,
      ocrRunner = ocrRunner,
    )
    assertTrue(toMd.success)
    val mdContent = toMd.outputFile!!.readText()
    assertTrue(mdContent.contains("## Page 1"))
    assertTrue(mdContent.contains("## Page 2"))

    // PDF -> CSV (page column numbers)
    val toCsv = FileConverterEngine.convertFile(
      context = null,
      sourceFile = pdfFile,
      targetFormat = ConvertibleFormat.CSV,
      outputDir = tempDir,
      ocrRunner = ocrRunner,
    )
    assertTrue(toCsv.success)
    val csvContent = toCsv.outputFile!!.readText()
    assertTrue(csvContent.contains("page,block,line,title,text"))
    assertTrue(csvContent.contains("Welcome to the handbook."))
    assertTrue(csvContent.contains("Configure settings and shortcuts."))
  }
}
