package com.example.toolbox.ui.tools

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PdfViewerEngineTest {

  @Test
  fun isPdfFile_recognizesValidPdfFilesAndRejectsOthers() {
    val tempDir = Files.createTempDirectory("pdf_viewer_test").toFile()
    try {
      val pdfFile = File(tempDir, "sample.PDF").apply { writeText("%PDF-1.4") }
      val txtFile = File(tempDir, "notes.txt").apply { writeText("hello") }
      val subDir = File(tempDir, "folder.pdf").apply { mkdirs() }

      assertTrue(PdfViewerEngine.isPdfFile(pdfFile))
      assertTrue(FileManagerEngine.isPdfFile(pdfFile))
      assertFalse(PdfViewerEngine.isPdfFile(txtFile))
      assertFalse(FileManagerEngine.isPdfFile(txtFile))
      assertFalse(PdfViewerEngine.isPdfFile(subDir))
      assertFalse(FileManagerEngine.isPdfFile(subDir))
    } finally {
      tempDir.deleteRecursively()
    }
  }

  @Test
  fun pdfEditHistory_tracksStrokesAndTextsPerPageWithUndoAndRedo() {
    val history = PdfEditHistory()
    assertFalse(history.canUndo)
    assertFalse(history.canRedo)
    assertFalse(history.hasAnyEdits)
    assertTrue(history.pageEdits(0).isEmpty)

    // Add a drawing stroke on page 0
    val stroke1 =
      PdfDrawStroke(
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
      PdfTextItem(
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
  fun pdfEditHistory_ignoresEmptyStrokeOrBlankText() {
    val history = PdfEditHistory()
    history.addStroke(0, PdfDrawStroke(points = emptyList(), color = Color.Black))
    history.addText(0, PdfTextItem(text = "   "))

    assertFalse(history.canUndo)
    assertFalse(history.hasAnyEdits)
    assertTrue(history.pageEdits(0).isEmpty)
  }

  @Test
  fun calculateFitWidthPageSize_fitsContainerWidthAndPreservesAspectRatio() {
    // Portrait A4 page (595 x 842) inside a 1080px wide container
    val (portraitW, portraitH) =
      PdfViewerEngine.calculateFitWidthPageSize(
        containerWidth = 1080f,
        bitmapWidth = 595,
        bitmapHeight = 842,
      )
    assertEquals(1080f, portraitW, 0.01f)
    assertEquals(1080f * (842f / 595f), portraitH, 0.1f)
    assertTrue("Portrait page height should exceed width when fitting width", portraitH > portraitW)

    // Landscape page (1200 x 800) inside a 900px wide container
    val (landscapeW, landscapeH) =
      PdfViewerEngine.calculateFitWidthPageSize(
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
      PdfViewerEngine.resolveVisiblePageIndex(
        scrollOffsetPx = 0,
        viewportHeightPx = viewportHeight,
        pageHeightsPx = pageHeights,
        pageSpacingPx = spacing,
      ),
    )

    // Scrolled to middle of Page 1 (page 1 spans 1424..2824, scroll = 1500 -> center = 2000)
    assertEquals(
      1,
      PdfViewerEngine.resolveVisiblePageIndex(
        scrollOffsetPx = 1500,
        viewportHeightPx = viewportHeight,
        pageHeightsPx = pageHeights,
        pageSpacingPx = spacing,
      ),
    )

    // Scrolled to Page 2 (page 2 spans 2848..4248, scroll = 3000 -> center = 3500)
    assertEquals(
      2,
      PdfViewerEngine.resolveVisiblePageIndex(
        scrollOffsetPx = 3000,
        viewportHeightPx = viewportHeight,
        pageHeightsPx = pageHeights,
        pageSpacingPx = spacing,
      ),
    )
  }
}
