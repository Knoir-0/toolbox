package com.example.toolbox.ui.tools

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentViewerEngineTest {

  @Test
  fun isPdfFile_recognizesValidPdfFilesAndRejectsOthers() {
    val tempDir = Files.createTempDirectory("doc_viewer_pdf_test").toFile()
    try {
      val pdfFile = File(tempDir, "sample.PDF").apply { writeText("%PDF-1.4") }
      val txtFile = File(tempDir, "notes.txt").apply { writeText("hello") }
      val subDir = File(tempDir, "folder.pdf").apply { mkdirs() }

      assertTrue(DocumentViewerEngine.isPdfFile(pdfFile))
      assertTrue(FileManagerEngine.isPdfFile(pdfFile))
      assertFalse(DocumentViewerEngine.isPdfFile(txtFile))
      assertFalse(FileManagerEngine.isPdfFile(txtFile))
      assertFalse(DocumentViewerEngine.isPdfFile(subDir))
      assertFalse(FileManagerEngine.isPdfFile(subDir))
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun isSupportedDocumentFile_recognizesPdfTextMarkupAndOfficeFormats() {
    val tempDir = Files.createTempDirectory("doc_viewer_formats_test").toFile()
    try {
      val supportedExtensions = listOf(
        "pdf" to DocumentFormat.PDF,
        "txt" to DocumentFormat.TXT,
        "md" to DocumentFormat.MD,
        "markdown" to DocumentFormat.MD,
        "rtf" to DocumentFormat.RTF,
        "html" to DocumentFormat.HTML,
        "htm" to DocumentFormat.HTML,
        "log" to DocumentFormat.LOG,
        "docx" to DocumentFormat.DOCX,
        "doc" to DocumentFormat.DOCX,
        "xlsx" to DocumentFormat.XLSX,
        "xls" to DocumentFormat.XLSX,
        "pptx" to DocumentFormat.PPTX,
        "ppt" to DocumentFormat.PPTX,
        "odt" to DocumentFormat.ODT,
        "ods" to DocumentFormat.ODS,
        "odp" to DocumentFormat.ODP,
      )

      for ((ext, expectedFormat) in supportedExtensions) {
        val file = File(tempDir, "sample.$ext").apply { writeText("content") }
        assertTrue("Expected .$ext to be supported", DocumentViewerEngine.isSupportedDocumentFile(file))
        assertTrue("Expected FileManagerEngine to recognize .$ext", FileManagerEngine.isDocumentViewerFile(file))
        assertEquals(expectedFormat, DocumentViewerEngine.detectFormat(file))
      }

      val unsupportedFile = File(tempDir, "song.mp3").apply { writeText("audio") }
      val dirWithDocExt = File(tempDir, "folder.docx").apply { mkdirs() }
      assertFalse(DocumentViewerEngine.isSupportedDocumentFile(unsupportedFile))
      assertFalse(FileManagerEngine.isDocumentViewerFile(unsupportedFile))
      assertNull(DocumentViewerEngine.detectFormat(unsupportedFile))
      assertFalse(DocumentViewerEngine.isSupportedDocumentFile(dirWithDocExt))
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun parseDocumentContent_parsesMarkdownHeadingsChecklistsTablesAndCode() {
    val tempDir = Files.createTempDirectory("doc_viewer_md_test").toFile()
    try {
      val mdFile = File(tempDir, "guide.md")
      DocumentViewerEngine.generateSampleDocumentFile(mdFile, DocumentFormat.MD)

      val parsed = DocumentViewerEngine.parseDocumentContent(mdFile)
      assertEquals(DocumentFormat.MD, parsed.format)
      assertTrue(parsed.blocks.any { it is DocumentBlock.Heading && it.level == 1 })
      assertTrue(parsed.blocks.any { it is DocumentBlock.BulletItem && it.marker == "☑" })
      assertTrue(parsed.blocks.any { it is DocumentBlock.Table })
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun parseDocumentContent_parsesRtfAndHtmlIntoStructuredBlocks() {
    val tempDir = Files.createTempDirectory("doc_viewer_rtf_html_test").toFile()
    try {
      val rtfFile = File(tempDir, "memo.rtf")
      DocumentViewerEngine.generateSampleDocumentFile(rtfFile, DocumentFormat.RTF)
      val parsedRtf = DocumentViewerEngine.parseDocumentContent(rtfFile)
      assertEquals(DocumentFormat.RTF, parsedRtf.format)
      val rtfText = parsedRtf.blocks.joinToString("\n") { block ->
        when (block) {
          is DocumentBlock.Heading -> block.text
          is DocumentBlock.Paragraph -> block.text
          is DocumentBlock.BulletItem -> block.text
          else -> ""
        }
      }
      assertTrue(rtfText.contains("Toolbox Rich Text Document"))

      val htmlFile = File(tempDir, "report.html")
      DocumentViewerEngine.generateSampleDocumentFile(htmlFile, DocumentFormat.HTML)
      val parsedHtml = DocumentViewerEngine.parseDocumentContent(htmlFile)
      assertEquals(DocumentFormat.HTML, parsedHtml.format)
      assertTrue(parsedHtml.blocks.any { it is DocumentBlock.Heading })
      assertTrue(parsedHtml.blocks.any { it is DocumentBlock.BulletItem })
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun parseDocumentContent_parsesOfficeOpenXmlDocxXlsxAndPptx() {
    val tempDir = Files.createTempDirectory("doc_viewer_office_test").toFile()
    try {
      // 1. DOCX
      val docxFile = File(tempDir, "spec.docx")
      DocumentViewerEngine.generateSampleDocxFile(docxFile)
      val parsedDocx = DocumentViewerEngine.parseDocumentContent(docxFile)
      assertEquals(DocumentFormat.DOCX, parsedDocx.format)
      assertTrue(
        parsedDocx.blocks.any {
          it is DocumentBlock.Heading && it.text.contains("Toolbox Word Document")
        }
      )
      assertTrue(parsedDocx.blocks.any { it is DocumentBlock.Table })

      // 2. XLSX
      val xlsxFile = File(tempDir, "metrics.xlsx")
      DocumentViewerEngine.generateSampleXlsxFile(xlsxFile)
      val parsedXlsx = DocumentViewerEngine.parseDocumentContent(xlsxFile)
      assertEquals(DocumentFormat.XLSX, parsedXlsx.format)
      val table = parsedXlsx.blocks.filterIsInstance<DocumentBlock.Table>().firstOrNull()
      assertNotNull(table)
      assertEquals("Project Metrics", table!!.title)
      assertEquals(listOf("Task", "Owner", "Status"), table.headers)
      assertEquals(2, table.rows.size)

      // 3. PPTX
      val pptxFile = File(tempDir, "deck.pptx")
      DocumentViewerEngine.generateSamplePptxFile(pptxFile)
      val parsedPptx = DocumentViewerEngine.parseDocumentContent(pptxFile)
      assertEquals(DocumentFormat.PPTX, parsedPptx.format)
      val slides = parsedPptx.blocks.filterIsInstance<DocumentBlock.SlideCard>()
      assertEquals(2, slides.size)
      assertEquals("Toolbox Document Viewer Deck", slides[0].title)
      assertEquals("Export & Share Workflow", slides[1].title)
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun documentEditHistory_tracksStrokesAndTextsPerPageWithUndoAndRedo() {
    val history = DocumentEditHistory()
    assertFalse(history.canUndo)
    assertFalse(history.canRedo)
    assertFalse(history.hasAnyEdits)
    assertTrue(history.pageEdits(0).isEmpty)

    // Add a drawing stroke on page 0
    val stroke1 =
      DocumentDrawStroke(
        points = listOf(Offset(0.1f, 0.1f), Offset(0.5f, 0.5f)),
        color = Color.Red,
        strokeWidth = 8f,
      )
    history.addStroke(0, stroke1)
    assertTrue(history.canUndo)
    assertFalse(history.canRedo)
    assertTrue(history.hasAnyEdits)
    assertEquals(1, history.pageEdits(0).strokes.size)
    assertEquals(1, history.pageEdits(0).editCount)

    // Add a text annotation on page 1
    val text1 =
      DocumentTextItem(
        id = 100L,
        text = "Approved",
        xRatio = 0.4f,
        yRatio = 0.6f,
        color = Color.Blue,
        fontSizeSp = 24f,
        hasBackground = true,
      )
    history.addText(1, text1)
    assertEquals(1, history.pageEdits(1).texts.size)
    assertEquals("Approved", history.pageEdits(1).texts.first().text)

    // Update text annotation on page 1
    val updatedText1 = text1.copy(text = "Approved & Signed", xRatio = 0.7f)
    history.updateText(1, updatedText1)
    assertEquals("Approved & Signed", history.pageEdits(1).texts.first().text)
    assertEquals(0.7f, history.pageEdits(1).texts.first().xRatio, 0.001f)

    // Undo text update -> reverts to "Approved"
    assertTrue(history.undo())
    assertEquals("Approved", history.pageEdits(1).texts.first().text)
    assertTrue(history.canRedo)

    // Redo text update -> restores "Approved & Signed"
    assertTrue(history.redo())
    assertEquals("Approved & Signed", history.pageEdits(1).texts.first().text)

    // Remove text annotation from page 1
    history.removeText(1, 100L)
    assertTrue(history.pageEdits(1).isEmpty)
    assertFalse(history.pageEdits(0).isEmpty)

    // Clear page 0
    history.clearPage(0)
    assertTrue(history.pageEdits(0).isEmpty)
    assertFalse(history.hasAnyEdits)
  }

  @Test
  fun documentEditHistory_ignoresEmptyStrokeOrBlankText() {
    val history = DocumentEditHistory()
    history.addStroke(0, DocumentDrawStroke(points = emptyList(), color = Color.Black))
    history.addText(0, DocumentTextItem(text = "   "))

    assertFalse(history.canUndo)
    assertFalse(history.hasAnyEdits)
    assertTrue(history.pageEdits(0).isEmpty)
  }

  @Test
  fun calculateFitWidthPageSize_fitsContainerWidthAndPreservesAspectRatio() {
    // Portrait A4 page (595 x 842) inside a 1080px wide container
    val (portraitW, portraitH) =
      DocumentViewerEngine.calculateFitWidthPageSize(
        containerWidth = 1080f,
        bitmapWidth = 595,
        bitmapHeight = 842,
      )
    assertEquals(1080f, portraitW, 0.01f)
    assertEquals(1080f * (842f / 595f), portraitH, 0.1f)
    assertTrue("Portrait page height should exceed width when fitting width", portraitH > portraitW)

    // Landscape page (1200 x 800) inside a 900px wide container
    val (landscapeW, landscapeH) =
      DocumentViewerEngine.calculateFitWidthPageSize(
        containerWidth = 900f,
        bitmapWidth = 1200,
        bitmapHeight = 800,
      )
    assertEquals(900f, landscapeW, 0.01f)
    assertEquals(600f, landscapeH, 0.01f)
  }

  @Test
  fun resolveVisiblePageIndex_tracksCurrentPageAcrossVerticalScroll() {
    val pageHeights = listOf(1400f, 1400f, 1400f)
    val spacing = 24f
    val viewportHeight = 1000f

    // At initial load (scroll = 0), viewport center is 500 -> Page 0
    assertEquals(
      0,
      DocumentViewerEngine.resolveVisiblePageIndex(
        scrollOffsetPx = 0,
        viewportHeightPx = viewportHeight,
        pageHeightsPx = pageHeights,
        pageSpacingPx = spacing,
      ),
    )

    // Scrolled to middle of Page 1 (page 1 spans 1424..2824, scroll = 1500 -> center = 2000)
    assertEquals(
      1,
      DocumentViewerEngine.resolveVisiblePageIndex(
        scrollOffsetPx = 1500,
        viewportHeightPx = viewportHeight,
        pageHeightsPx = pageHeights,
        pageSpacingPx = spacing,
      ),
    )

    // Scrolled to Page 2 (page 2 spans 2848..4248, scroll = 3000 -> center = 3500)
    assertEquals(
      2,
      DocumentViewerEngine.resolveVisiblePageIndex(
        scrollOffsetPx = 3000,
        viewportHeightPx = viewportHeight,
        pageHeightsPx = pageHeights,
        pageSpacingPx = spacing,
      ),
    )
  }
}
