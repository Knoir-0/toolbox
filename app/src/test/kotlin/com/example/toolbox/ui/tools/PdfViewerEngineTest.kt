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
}
