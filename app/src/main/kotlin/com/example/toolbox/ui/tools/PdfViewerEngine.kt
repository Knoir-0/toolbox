package com.example.toolbox.ui.tools

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class PdfEditMode(val label: String) {
  VIEW("View"),
  DRAW("Draw"),
  TEXT("Text"),
}

enum class PdfBrushType(val label: String, val defaultAlpha: Float, val isEraser: Boolean = false) {
  PEN("Pen", 1f, false),
  HIGHLIGHTER("Highlight", 0.38f, false),
  ERASER("Eraser", 1f, true),
}

data class PdfDrawStroke(
  val points: List<Offset>,
  val color: Color,
  val strokeWidth: Float = 8f,
  val alpha: Float = 1f,
  val isEraser: Boolean = false,
)

data class PdfTextItem(
  val id: Long = System.nanoTime(),
  val text: String,
  val xRatio: Float = 0.5f,
  val yRatio: Float = 0.5f,
  val color: Color = Color(0xFF1A1A1A),
  val fontSizeSp: Float = 20f,
  val hasBackground: Boolean = false,
)

data class PdfPageEdits(
  val strokes: List<PdfDrawStroke> = emptyList(),
  val texts: List<PdfTextItem> = emptyList(),
) {
  val isEmpty: Boolean
    get() = strokes.isEmpty() && texts.isEmpty()

  val editCount: Int
    get() = strokes.size + texts.size
}

class PdfEditHistory(initialEdits: Map<Int, PdfPageEdits> = emptyMap()) {
  private val undoStack = ArrayDeque<Map<Int, PdfPageEdits>>()
  private val redoStack = ArrayDeque<Map<Int, PdfPageEdits>>()

  var currentEdits: Map<Int, PdfPageEdits> = initialEdits
    private set

  val canUndo: Boolean
    get() = undoStack.isNotEmpty()

  val canRedo: Boolean
    get() = redoStack.isNotEmpty()

  val hasAnyEdits: Boolean
    get() = currentEdits.values.any { !it.isEmpty }

  fun pageEdits(pageIndex: Int): PdfPageEdits = currentEdits[pageIndex] ?: PdfPageEdits()

  fun updatePage(pageIndex: Int, transform: (PdfPageEdits) -> PdfPageEdits) {
    val existing = pageEdits(pageIndex)
    val updated = transform(existing)
    if (updated == existing) return
    undoStack.addLast(currentEdits)
    if (undoStack.size > MAX_HISTORY) {
      undoStack.removeFirst()
    }
    redoStack.clear()
    val nextMap = currentEdits.toMutableMap()
    if (updated.isEmpty) {
      nextMap.remove(pageIndex)
    } else {
      nextMap[pageIndex] = updated
    }
    currentEdits = nextMap
  }

  fun addStroke(pageIndex: Int, stroke: PdfDrawStroke) {
    if (stroke.points.isEmpty()) return
    updatePage(pageIndex) { page -> page.copy(strokes = page.strokes + stroke) }
  }

  fun addText(pageIndex: Int, item: PdfTextItem) {
    if (item.text.isBlank()) return
    updatePage(pageIndex) { page -> page.copy(texts = page.texts + item) }
  }

  fun updateText(pageIndex: Int, updatedItem: PdfTextItem) {
    updatePage(pageIndex) { page ->
      page.copy(texts = page.texts.map { if (it.id == updatedItem.id) updatedItem else it })
    }
  }

  fun removeText(pageIndex: Int, textId: Long) {
    updatePage(pageIndex) { page ->
      page.copy(texts = page.texts.filterNot { it.id == textId })
    }
  }

  fun clearPage(pageIndex: Int) {
    updatePage(pageIndex) { PdfPageEdits() }
  }

  fun undo(): Boolean {
    if (undoStack.isEmpty()) return false
    redoStack.addLast(currentEdits)
    currentEdits = undoStack.removeLast()
    return true
  }

  fun redo(): Boolean {
    if (redoStack.isEmpty()) return false
    undoStack.addLast(currentEdits)
    currentEdits = redoStack.removeLast()
    return true
  }

  fun reset(edits: Map<Int, PdfPageEdits> = emptyMap()) {
    undoStack.clear()
    redoStack.clear()
    currentEdits = edits
  }

  companion object {
    private const val MAX_HISTORY = 30
  }
}

object PdfViewerEngine {
  private const val BASE_REFERENCE_DIMENSION = 1000f

  fun isPdfFile(file: File): Boolean {
    return file.isFile && file.extension.lowercase() == "pdf"
  }

  fun getPageCount(file: File): Int {
    if (!file.exists() || !file.isFile) return 0
    return try {
      ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
        PdfRenderer(pfd).use { renderer -> renderer.pageCount }
      }
    } catch (_: Exception) {
      0
    }
  }

  fun renderPageToBitmap(
    file: File,
    pageIndex: Int,
    maxDimension: Int = 1600,
  ): Bitmap? {
    if (!file.exists() || !file.isFile) return null
    return try {
      ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
        PdfRenderer(pfd).use { renderer ->
          if (pageIndex !in 0 until renderer.pageCount) return null
          renderer.openPage(pageIndex).use { page ->
            val rawW = page.width.coerceAtLeast(1)
            val rawH = page.height.coerceAtLeast(1)
            val scale = maxDimension.toFloat() / max(rawW, rawH).toFloat()
            val targetW = (rawW * scale).roundToInt().coerceAtLeast(1)
            val targetH = (rawH * scale).roundToInt().coerceAtLeast(1)

            val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmap
          }
        }
      }
    } catch (_: Exception) {
      null
    }
  }

  fun renderEditedPageBitmap(
    basePageBitmap: Bitmap,
    edits: PdfPageEdits,
  ): Bitmap {
    val result = basePageBitmap.copy(Bitmap.Config.ARGB_8888, true)
    if (edits.isEmpty) return result

    val width = result.width.toFloat()
    val height = result.height.toFloat()
    val dimensionScale = min(width, height) / BASE_REFERENCE_DIMENSION
    val resultCanvas = Canvas(result)

    if (edits.strokes.isNotEmpty()) {
      val strokeLayer =
        Bitmap.createBitmap(
          result.width.coerceAtLeast(1),
          result.height.coerceAtLeast(1),
          Bitmap.Config.ARGB_8888,
        )
      val strokeCanvas = Canvas(strokeLayer)

      for (stroke in edits.strokes) {
        if (stroke.points.isEmpty()) continue
        val paint =
          Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = (stroke.strokeWidth * dimensionScale).coerceAtLeast(2f)
            if (stroke.isEraser) {
              xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
              color = android.graphics.Color.TRANSPARENT
            } else {
              val effectiveColor = stroke.color.copy(alpha = stroke.alpha.coerceIn(0.05f, 1f))
              color = effectiveColor.toArgb()
            }
          }

        if (stroke.points.size == 1) {
          val pt = stroke.points.first()
          val cx = pt.x.coerceIn(0f, 1f) * width
          val cy = pt.y.coerceIn(0f, 1f) * height
          val dotPaint =
            Paint(paint).apply {
              style = Paint.Style.FILL
            }
          strokeCanvas.drawCircle(cx, cy, paint.strokeWidth / 2f, dotPaint)
        } else {
          val path = android.graphics.Path()
          val first = stroke.points.first()
          path.moveTo(first.x.coerceIn(0f, 1f) * width, first.y.coerceIn(0f, 1f) * height)
          for (i in 1 until stroke.points.size) {
            val pt = stroke.points[i]
            path.lineTo(pt.x.coerceIn(0f, 1f) * width, pt.y.coerceIn(0f, 1f) * height)
          }
          strokeCanvas.drawPath(path, paint)
        }
      }

      resultCanvas.drawBitmap(strokeLayer, 0f, 0f, null)
      strokeLayer.recycle()
    }

    if (edits.texts.isNotEmpty()) {
      for (item in edits.texts) {
        if (item.text.isBlank()) continue
        val scaledTextSize = (item.fontSizeSp * 1.6f * dimensionScale).coerceAtLeast(12f)
        val textPaint =
          Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = item.color.toArgb()
            textSize = scaledTextSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
          }

        val lines = item.text.split("\n")
        val fontMetrics = textPaint.fontMetrics
        val lineHeight = (fontMetrics.descent - fontMetrics.ascent) * 1.15f
        val totalHeight = lineHeight * lines.size
        val maxLineWidth = lines.maxOfOrNull { textPaint.measureText(it) } ?: 0f

        val centerX = item.xRatio.coerceIn(0.02f, 0.98f) * width
        val centerY = item.yRatio.coerceIn(0.02f, 0.98f) * height

        if (item.hasBackground) {
          val padH = scaledTextSize * 0.45f
          val padV = scaledTextSize * 0.3f
          val bgPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
              val luminance =
                (0.299f * item.color.red) +
                  (0.587f * item.color.green) +
                  (0.114f * item.color.blue)
              color =
                if (luminance > 0.6f) {
                  android.graphics.Color.argb(215, 24, 24, 28)
                } else {
                  android.graphics.Color.argb(230, 255, 252, 235)
                }
              style = Paint.Style.FILL
            }
          val rect =
            RectF(
              centerX - maxLineWidth / 2f - padH,
              centerY - totalHeight / 2f - padV,
              centerX + maxLineWidth / 2f + padH,
              centerY + totalHeight / 2f + padV,
            )
          resultCanvas.drawRoundRect(rect, scaledTextSize * 0.3f, scaledTextSize * 0.3f, bgPaint)
        }

        val startBaselineY =
          centerY - (totalHeight / 2f) - fontMetrics.ascent - ((lineHeight - (fontMetrics.descent - fontMetrics.ascent)) / 2f)
        lines.forEachIndexed { index, line ->
          resultCanvas.drawText(line, centerX, startBaselineY + index * lineHeight, textPaint)
        }
      }
    }

    return result
  }

  fun saveEditedPdf(
    sourceFile: File,
    outputFile: File,
    pageEdits: Map<Int, PdfPageEdits>,
    renderScale: Float = 2f,
  ): Result<File> {
    if (!sourceFile.exists() || !sourceFile.isFile) {
      return Result.failure(IllegalArgumentException("Source PDF file does not exist"))
    }
    return try {
      outputFile.parentFile?.mkdirs()
      val tempOutput =
        if (sourceFile.absolutePath == outputFile.absolutePath) {
          File(outputFile.parentFile, ".tmp_${System.currentTimeMillis()}_${outputFile.name}")
        } else {
          outputFile
        }

      ParcelFileDescriptor.open(sourceFile, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
        PdfRenderer(pfd).use { renderer ->
          val outDoc = PdfDocument()
          try {
            val count = renderer.pageCount
            for (pageIndex in 0 until count) {
              renderer.openPage(pageIndex).use { srcPage ->
                val pageWidth = srcPage.width.coerceAtLeast(1)
                val pageHeight = srcPage.height.coerceAtLeast(1)

                val bitmapW = (pageWidth * renderScale).roundToInt().coerceIn(1, 2400)
                val bitmapH = (pageHeight * renderScale).roundToInt().coerceIn(1, 2400)

                val rawBitmap = Bitmap.createBitmap(bitmapW, bitmapH, Bitmap.Config.ARGB_8888)
                val rawCanvas = Canvas(rawBitmap)
                rawCanvas.drawColor(android.graphics.Color.WHITE)
                srcPage.render(
                  rawBitmap,
                  null,
                  null,
                  PdfRenderer.Page.RENDER_MODE_FOR_PRINT,
                )

                val editsForPage = pageEdits[pageIndex] ?: PdfPageEdits()
                val finalBitmap =
                  if (editsForPage.isEmpty) {
                    rawBitmap
                  } else {
                    renderEditedPageBitmap(rawBitmap, editsForPage)
                  }

                val pageInfo =
                  PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
                val destPage = outDoc.startPage(pageInfo)
                val destRect = Rect(0, 0, pageWidth, pageHeight)
                val bitmapPaint = Paint(Paint.FILTER_BITMAP_FLAG)
                destPage.canvas.drawBitmap(finalBitmap, null, destRect, bitmapPaint)
                outDoc.finishPage(destPage)

                if (finalBitmap !== rawBitmap) {
                  finalBitmap.recycle()
                }
                rawBitmap.recycle()
              }
            }

            FileOutputStream(tempOutput).use { fos ->
              outDoc.writeTo(fos)
            }
          } finally {
            outDoc.close()
          }
        }
      }

      if (tempOutput != outputFile) {
        tempOutput.copyTo(outputFile, overwrite = true)
        tempOutput.delete()
      }
      Result.success(outputFile)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun generateSamplePdfFile(outputFile: File): File {
    outputFile.parentFile?.mkdirs()
    val document = PdfDocument()
    val pageWidth = 595
    val pageHeight = 842

    try {
      // Page 1: Project Overview & Annotation Sandbox
      val page1Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
      val page1 = document.startPage(page1Info)
      drawSamplePage1(page1.canvas, pageWidth, pageHeight)
      document.finishPage(page1)

      // Page 2: Checklist & Review Notes
      val page2Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
      val page2 = document.startPage(page2Info)
      drawSamplePage2(page2.canvas, pageWidth, pageHeight)
      document.finishPage(page2)

      // Page 3: Sketch Grid & Sign-off
      val page3Info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 3).create()
      val page3 = document.startPage(page3Info)
      drawSamplePage3(page3.canvas, pageWidth, pageHeight)
      document.finishPage(page3)

      FileOutputStream(outputFile).use { out ->
        document.writeTo(out)
      }
    } finally {
      document.close()
    }
    return outputFile
  }

  private fun drawSamplePage1(canvas: Canvas, width: Int, height: Int) {
    canvas.drawColor(android.graphics.Color.WHITE)

    val headerPaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(33, 64, 154)
        style = Paint.Style.FILL
      }
    canvas.drawRoundRect(RectF(40f, 40f, width - 40f, 145f), 16f, 16f, headerPaint)

    val titlePaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 26f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      }
    canvas.drawText("Toolbox Sample PDF Document", 64f, 88f, titlePaint)

    val subTitlePaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(215, 228, 255)
        textSize = 14f
      }
    canvas.drawText("Page 1 of 3 • Try switching between View, Draw, and Text modes", 64f, 118f, subTitlePaint)

    val sectionTitlePaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(28, 32, 42)
        textSize = 18f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      }
    val bodyPaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(65, 72, 86)
        textSize = 14f
      }

    canvas.drawText("1. Document Summary", 48f, 195f, sectionTitlePaint)
    val summaryLines =
      listOf(
        "This sample multi-page PDF is generated locally so you can preview,",
        "highlight, sketch freehand notes, and place text annotations anywhere.",
        "Use the bottom toolbar to switch modes and export your annotated PDF.",
      )
    summaryLines.forEachIndexed { idx, line ->
      canvas.drawText(line, 48f, 225f + idx * 24f, bodyPaint)
    }

    val cardPaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(244, 247, 252)
        style = Paint.Style.FILL
      }
    val borderPaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(200, 212, 232)
        style = Paint.Style.STROKE
        strokeWidth = 2f
      }
    val boxRect = RectF(48f, 320f, width - 48f, 540f)
    canvas.drawRoundRect(boxRect, 12f, 12f, cardPaint)
    canvas.drawRoundRect(boxRect, 12f, 12f, borderPaint)

    canvas.drawText("2. Key Milestones & Metrics", 68f, 356f, sectionTitlePaint)
    val metrics =
      listOf(
        "• Architecture & Offline Rendering Engine  —  Completed",
        "• Freehand Pen, Highlighter & Eraser Layer  —  Ready for Markup",
        "• Draggable Text Annotations & Export       —  Interactive",
        "• Multi-Page Navigation & Zoom Controls     —  Verified",
      )
    metrics.forEachIndexed { idx, line ->
      canvas.drawText(line, 68f, 392f + idx * 32f, bodyPaint)
    }

    val signBox = RectF(48f, 580f, width - 48f, 750f)
    canvas.drawRoundRect(signBox, 12f, 12f, borderPaint)
    canvas.drawText("3. Approval & Freehand Signature Box", 68f, 615f, sectionTitlePaint)
    canvas.drawText("Draw your signature or stamp text inside the area below:", 68f, 642f, bodyPaint)
    canvas.drawLine(68f, 715f, width - 68f, 715f, borderPaint)

    val footerPaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(130, 138, 150)
        textSize = 12f
      }
    canvas.drawText("Toolbox PDF Viewer • Page 1 / 3", 48f, height - 36f, footerPaint)
  }

  private fun drawSamplePage2(canvas: Canvas, width: Int, height: Int) {
    canvas.drawColor(android.graphics.Color.WHITE)

    val sectionTitlePaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(28, 32, 42)
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      }
    val bodyPaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(65, 72, 86)
        textSize = 15f
      }
    val boxPaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(90, 105, 135)
        style = Paint.Style.STROKE
        strokeWidth = 2f
      }

    canvas.drawText("Review Checklist (Page 2)", 48f, 72f, sectionTitlePaint)
    canvas.drawText("Check off items using the Draw tool or add comments with the Text tool:", 48f, 104f, bodyPaint)

    val checklistItems =
      listOf(
        "Verify layout dimensions and margins",
        "Highlight priority paragraphs in yellow",
        "Add reviewer initials and date stamp",
        "Confirm offline PDF export preserves all pages",
        "Share or save annotated copy to device storage",
      )

    checklistItems.forEachIndexed { idx, item ->
      val top = 145f + idx * 56f
      canvas.drawRoundRect(RectF(56f, top, 80f, top + 24f), 4f, 4f, boxPaint)
      canvas.drawText(item, 98f, top + 18f, bodyPaint)
    }

    val notesRect = RectF(48f, 450f, width - 48f, 750f)
    canvas.drawRoundRect(notesRect, 12f, 12f, boxPaint)
    canvas.drawText("Reviewer Notes", 68f, 488f, sectionTitlePaint)

    val linePaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(220, 226, 236)
        strokeWidth = 1.5f
      }
    for (lineIdx in 0..5) {
      val y = 530f + lineIdx * 34f
      canvas.drawLine(68f, y, width - 68f, y, linePaint)
    }

    val footerPaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(130, 138, 150)
        textSize = 12f
      }
    canvas.drawText("Toolbox PDF Viewer • Page 2 / 3", 48f, height - 36f, footerPaint)
  }

  private fun drawSamplePage3(canvas: Canvas, width: Int, height: Int) {
    canvas.drawColor(android.graphics.Color.WHITE)

    val titlePaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(28, 32, 42)
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      }
    val subPaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(80, 88, 102)
        textSize = 14f
      }
    canvas.drawText("Diagram & Sketch Canvas (Page 3)", 48f, 72f, titlePaint)
    canvas.drawText("Use the grid below for diagrams, callouts, and freehand annotations.", 48f, 100f, subPaint)

    val gridLeft = 48f
    val gridTop = 130f
    val gridRight = width - 48f
    val gridBottom = 740f

    val gridPaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(232, 236, 244)
        strokeWidth = 1f
      }
    val step = 32f
    var x = gridLeft
    while (x <= gridRight) {
      canvas.drawLine(x, gridTop, x, gridBottom, gridPaint)
      x += step
    }
    var y = gridTop
    while (y <= gridBottom) {
      canvas.drawLine(gridLeft, y, gridRight, y, gridPaint)
      y += step
    }

    val borderPaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(180, 192, 212)
        style = Paint.Style.STROKE
        strokeWidth = 2f
      }
    canvas.drawRoundRect(RectF(gridLeft, gridTop, gridRight, gridBottom), 8f, 8f, borderPaint)

    val footerPaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(130, 138, 150)
        textSize = 12f
      }
    canvas.drawText("Toolbox PDF Viewer • Page 3 / 3", 48f, height - 36f, footerPaint)
  }
}
