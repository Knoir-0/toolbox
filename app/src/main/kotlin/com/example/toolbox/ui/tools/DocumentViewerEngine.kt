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
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class DocumentEditMode(val label: String) {
  VIEW("View"),
  DRAW("Draw"),
  TEXT("Text"),
}

typealias PdfEditMode = DocumentEditMode

enum class DocumentBrushType(
  val label: String,
  val defaultAlpha: Float,
  val isEraser: Boolean = false,
) {
  PEN("Pen", 1f, false),
  HIGHLIGHTER("Highlight", 0.38f, false),
  ERASER("Eraser", 1f, true),
}

typealias PdfBrushType = DocumentBrushType

data class DocumentDrawStroke(
  val points: List<Offset>,
  val color: Color,
  val strokeWidth: Float = 8f,
  val alpha: Float = 1f,
  val isEraser: Boolean = false,
)

typealias PdfDrawStroke = DocumentDrawStroke

data class DocumentTextItem(
  val id: Long = System.nanoTime(),
  val text: String,
  val xRatio: Float = 0.5f,
  val yRatio: Float = 0.5f,
  val color: Color = Color(0xFF1A1A1A),
  val fontSizeSp: Float = 20f,
  val hasBackground: Boolean = false,
)

typealias PdfTextItem = DocumentTextItem

data class DocumentPageEdits(
  val strokes: List<DocumentDrawStroke> = emptyList(),
  val texts: List<DocumentTextItem> = emptyList(),
) {
  val isEmpty: Boolean
    get() = strokes.isEmpty() && texts.isEmpty()

  val editCount: Int
    get() = strokes.size + texts.size
}

typealias PdfPageEdits = DocumentPageEdits

class DocumentEditHistory(initialEdits: Map<Int, DocumentPageEdits> = emptyMap()) {
  private val undoStack = ArrayDeque<Map<Int, DocumentPageEdits>>()
  private val redoStack = ArrayDeque<Map<Int, DocumentPageEdits>>()

  var currentEdits: Map<Int, DocumentPageEdits> = initialEdits
    private set

  val canUndo: Boolean
    get() = undoStack.isNotEmpty()

  val canRedo: Boolean
    get() = redoStack.isNotEmpty()

  val hasAnyEdits: Boolean
    get() = currentEdits.values.any { !it.isEmpty }

  fun pageEdits(pageIndex: Int): DocumentPageEdits = currentEdits[pageIndex] ?: DocumentPageEdits()

  fun updatePage(pageIndex: Int, transform: (DocumentPageEdits) -> DocumentPageEdits) {
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

  fun addStroke(pageIndex: Int, stroke: DocumentDrawStroke) {
    if (stroke.points.isEmpty()) return
    updatePage(pageIndex) { page -> page.copy(strokes = page.strokes + stroke) }
  }

  fun addText(pageIndex: Int, item: DocumentTextItem) {
    if (item.text.isBlank()) return
    updatePage(pageIndex) { page -> page.copy(texts = page.texts + item) }
  }

  fun updateText(pageIndex: Int, updatedItem: DocumentTextItem) {
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
    updatePage(pageIndex) { DocumentPageEdits() }
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

  fun reset(edits: Map<Int, DocumentPageEdits> = emptyMap()) {
    undoStack.clear()
    redoStack.clear()
    currentEdits = edits
  }

  companion object {
    private const val MAX_HISTORY = 30
  }
}

typealias PdfEditHistory = DocumentEditHistory

enum class DocumentFileCategory(val displayName: String) {
  PDF("PDF"),
  TEXT_MARKUP("Text & Markup"),
  OFFICE("Office Document"),
}

enum class DocumentFormat(
  val displayName: String,
  val badgeLabel: String,
  val primaryExtension: String,
  val extensions: Set<String>,
  val mimeType: String,
  val category: DocumentFileCategory,
  val isPresentationLandscape: Boolean = false,
) {
  PDF(
    displayName = "PDF Document",
    badgeLabel = "PDF",
    primaryExtension = "pdf",
    extensions = setOf("pdf"),
    mimeType = "application/pdf",
    category = DocumentFileCategory.PDF,
  ),
  TXT(
    displayName = "Plain Text",
    badgeLabel = "TXT",
    primaryExtension = "txt",
    extensions = setOf("txt", "text"),
    mimeType = "text/plain",
    category = DocumentFileCategory.TEXT_MARKUP,
  ),
  MD(
    displayName = "Markdown",
    badgeLabel = "MD",
    primaryExtension = "md",
    extensions = setOf("md", "markdown"),
    mimeType = "text/markdown",
    category = DocumentFileCategory.TEXT_MARKUP,
  ),
  RTF(
    displayName = "Rich Text Format",
    badgeLabel = "RTF",
    primaryExtension = "rtf",
    extensions = setOf("rtf"),
    mimeType = "application/rtf",
    category = DocumentFileCategory.TEXT_MARKUP,
  ),
  HTML(
    displayName = "HTML Document",
    badgeLabel = "HTML",
    primaryExtension = "html",
    extensions = setOf("html", "htm"),
    mimeType = "text/html",
    category = DocumentFileCategory.TEXT_MARKUP,
  ),
  LOG(
    displayName = "Log File",
    badgeLabel = "LOG",
    primaryExtension = "log",
    extensions = setOf("log"),
    mimeType = "text/plain",
    category = DocumentFileCategory.TEXT_MARKUP,
  ),
  DOCX(
    displayName = "Word Document",
    badgeLabel = "DOCX",
    primaryExtension = "docx",
    extensions = setOf("docx", "doc"),
    mimeType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    category = DocumentFileCategory.OFFICE,
  ),
  XLSX(
    displayName = "Excel Spreadsheet",
    badgeLabel = "XLSX",
    primaryExtension = "xlsx",
    extensions = setOf("xlsx", "xls"),
    mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    category = DocumentFileCategory.OFFICE,
  ),
  PPTX(
    displayName = "PowerPoint Presentation",
    badgeLabel = "PPTX",
    primaryExtension = "pptx",
    extensions = setOf("pptx", "ppt"),
    mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    category = DocumentFileCategory.OFFICE,
    isPresentationLandscape = true,
  ),
  ODT(
    displayName = "OpenDocument Text",
    badgeLabel = "ODT",
    primaryExtension = "odt",
    extensions = setOf("odt"),
    mimeType = "application/vnd.oasis.opendocument.text",
    category = DocumentFileCategory.OFFICE,
  ),
  ODS(
    displayName = "OpenDocument Spreadsheet",
    badgeLabel = "ODS",
    primaryExtension = "ods",
    extensions = setOf("ods"),
    mimeType = "application/vnd.oasis.opendocument.spreadsheet",
    category = DocumentFileCategory.OFFICE,
  ),
  ODP(
    displayName = "OpenDocument Presentation",
    badgeLabel = "ODP",
    primaryExtension = "odp",
    extensions = setOf("odp"),
    mimeType = "application/vnd.oasis.opendocument.presentation",
    category = DocumentFileCategory.OFFICE,
    isPresentationLandscape = true,
  ),
  ;

  companion object {
    val SUPPORTED_EXTENSIONS: Set<String> =
      entries.flatMap { it.extensions }.toSet()

    val SUPPORTED_PICKER_MIME_TYPES: Array<String> =
      arrayOf(
        "application/pdf",
        "text/plain",
        "text/markdown",
        "text/x-markdown",
        "text/html",
        "application/rtf",
        "text/rtf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "application/vnd.oasis.opendocument.text",
        "application/vnd.oasis.opendocument.spreadsheet",
        "application/vnd.oasis.opendocument.presentation",
      )

    fun fromExtension(extension: String?): DocumentFormat? {
      if (extension.isNullOrBlank()) return null
      val clean = extension.trim().removePrefix(".").lowercase(Locale.ROOT)
      if (clean.isEmpty()) return null
      return entries.firstOrNull { clean in it.extensions }
    }

    fun fromFile(file: File?): DocumentFormat? {
      if (file == null || file.isDirectory) return null
      return fromExtension(file.extension)
    }
  }
}

sealed interface DocumentBlock {
  data class Heading(val text: String, val level: Int = 1) : DocumentBlock
  data class Paragraph(
    val text: String,
    val isMonospace: Boolean = false,
    val isQuote: Boolean = false,
  ) : DocumentBlock
  data class BulletItem(val text: String, val marker: String = "•") : DocumentBlock
  data class CodeBlock(val lines: List<String>) : DocumentBlock
  data class Table(val headers: List<String>, val rows: List<List<String>>, val title: String? = null) : DocumentBlock
  data class SlideCard(
    val slideNumber: Int,
    val title: String,
    val bullets: List<String>,
    val notes: String? = null,
  ) : DocumentBlock
  data object Divider : DocumentBlock
  data object PageBreak : DocumentBlock
}

data class ParsedDocumentContent(
  val format: DocumentFormat,
  val title: String,
  val blocks: List<DocumentBlock>,
)

object DocumentViewerEngine {
  private const val BASE_REFERENCE_DIMENSION = 1000f
  private const val PORTRAIT_PAGE_WIDTH_PT = 595
  private const val PORTRAIT_PAGE_HEIGHT_PT = 842
  private const val LANDSCAPE_PAGE_WIDTH_PT = 842
  private const val LANDSCAPE_PAGE_HEIGHT_PT = 595

  val SUPPORTED_DOCUMENT_EXTENSIONS: Set<String>
    get() = DocumentFormat.SUPPORTED_EXTENSIONS

  fun detectFormat(file: File?): DocumentFormat? = DocumentFormat.fromFile(file)

  fun isSupportedDocumentFile(file: File?): Boolean {
    if (file == null || !file.isFile) return false
    return detectFormat(file) != null
  }

  fun isSupportedDocumentExtension(extension: String?): Boolean {
    return DocumentFormat.fromExtension(extension) != null
  }

  fun isPdfFile(file: File): Boolean {
    return file.isFile && file.extension.lowercase(Locale.ROOT) == "pdf"
  }

  fun getPageCount(file: File): Int {
    if (!file.exists() || !file.isFile) return 0
    val format = detectFormat(file) ?: return 0
    if (format == DocumentFormat.PDF) {
      return try {
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
          PdfRenderer(pfd).use { renderer -> renderer.pageCount }
        }
      } catch (_: Exception) {
        0
      }
    }
    return try {
      renderAllPagesToBitmaps(file, maxDimension = 800).size.also {
        // Count resolved from paginated render
      }
    } catch (_: Exception) {
      0
    }
  }

  fun calculateFitWidthPageSize(
    containerWidth: Float,
    bitmapWidth: Int,
    bitmapHeight: Int,
  ): Pair<Float, Float> {
    val safeContainerW = containerWidth.coerceAtLeast(1f)
    val safeBmpW = bitmapWidth.coerceAtLeast(1).toFloat()
    val safeBmpH = bitmapHeight.coerceAtLeast(1).toFloat()
    val aspectRatio = safeBmpW / safeBmpH
    val displayW = safeContainerW
    val displayH = (safeContainerW / aspectRatio).coerceAtLeast(1f)
    return displayW to displayH
  }

  fun resolveVisiblePageIndex(
    scrollOffsetPx: Int,
    viewportHeightPx: Float,
    pageHeightsPx: List<Float>,
    pageSpacingPx: Float = 0f,
  ): Int {
    if (pageHeightsPx.isEmpty()) return 0
    val viewportCenterY = scrollOffsetPx.toFloat() + (viewportHeightPx.coerceAtLeast(1f) / 2f)
    var accumulatedY = 0f
    for (i in pageHeightsPx.indices) {
      val pageH = pageHeightsPx[i].coerceAtLeast(1f)
      val pageBottom = accumulatedY + pageH
      if (viewportCenterY <= pageBottom + (pageSpacingPx / 2f)) {
        return i
      }
      accumulatedY = pageBottom + pageSpacingPx
    }
    return pageHeightsPx.lastIndex
  }

  fun renderAllPagesToBitmaps(
    file: File,
    maxDimension: Int = 1600,
  ): List<Bitmap> {
    if (!file.exists() || !file.isFile) return emptyList()
    val format = detectFormat(file) ?: DocumentFormat.TXT
    return if (format == DocumentFormat.PDF) {
      renderPdfPagesToBitmaps(file, maxDimension)
    } else {
      renderStructuredDocumentToBitmaps(file, format, maxDimension)
    }
  }

  private fun renderPdfPagesToBitmaps(
    file: File,
    maxDimension: Int = 1600,
  ): List<Bitmap> {
    return try {
      ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
        PdfRenderer(pfd).use { renderer ->
          val count = renderer.pageCount
          if (count <= 0) return emptyList()
          val effectiveMaxDim =
            when {
              count > 30 -> min(maxDimension, 960)
              count > 12 -> min(maxDimension, 1200)
              else -> maxDimension
            }
          val bitmaps = ArrayList<Bitmap>(count)
          for (pageIndex in 0 until count) {
            renderer.openPage(pageIndex).use { page ->
              val rawW = page.width.coerceAtLeast(1)
              val rawH = page.height.coerceAtLeast(1)
              val scale = effectiveMaxDim.toFloat() / max(rawW, rawH).toFloat()
              val targetW = (rawW * scale).roundToInt().coerceAtLeast(1)
              val targetH = (rawH * scale).roundToInt().coerceAtLeast(1)

              val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
              val canvas = Canvas(bitmap)
              canvas.drawColor(android.graphics.Color.WHITE)
              page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
              bitmaps.add(bitmap)
            }
          }
          bitmaps
        }
      }
    } catch (_: Exception) {
      emptyList()
    }
  }

  fun renderPageToBitmap(
    file: File,
    pageIndex: Int,
    maxDimension: Int = 1600,
  ): Bitmap? {
    if (!file.exists() || !file.isFile) return null
    val format = detectFormat(file) ?: DocumentFormat.TXT
    if (format != DocumentFormat.PDF) {
      val pages = renderStructuredDocumentToBitmaps(file, format, maxDimension)
      return pages.getOrNull(pageIndex)
    }
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

  // =========================================================================
  // Multi-Format Document Parsing (TXT, MD, RTF, HTML, LOG, DOCX, XLSX, PPTX, ODT, ODS, ODP)
  // =========================================================================

  fun parseDocumentContent(file: File): ParsedDocumentContent {
    val format = detectFormat(file) ?: DocumentFormat.TXT
    val title = file.nameWithoutExtension.ifBlank { file.name }
    if (!file.exists() || !file.isFile) {
      return ParsedDocumentContent(format = format, title = title, blocks = emptyList())
    }
    val blocks =
      try {
        when (format) {
          DocumentFormat.PDF -> listOf(DocumentBlock.Paragraph("PDF document (${file.name})"))
          DocumentFormat.TXT -> parsePlainTextBlocks(readTextSafe(file), isMonospace = false)
          DocumentFormat.LOG -> parseLogBlocks(readTextSafe(file))
          DocumentFormat.MD -> parseMarkdownBlocks(readTextSafe(file))
          DocumentFormat.RTF -> parseRtfBlocks(readTextSafe(file))
          DocumentFormat.HTML -> parseHtmlBlocks(readTextSafe(file))
          DocumentFormat.DOCX -> parseDocxBlocks(file)
          DocumentFormat.XLSX -> parseXlsxBlocks(file)
          DocumentFormat.PPTX -> parsePptxBlocks(file)
          DocumentFormat.ODT -> parseOdtBlocks(file)
          DocumentFormat.ODS -> parseOdsBlocks(file)
          DocumentFormat.ODP -> parseOdpBlocks(file)
        }
      } catch (e: Exception) {
        listOf(
          DocumentBlock.Heading(title, level = 1),
          DocumentBlock.Paragraph("Unable to parse full document structure: ${e.message ?: "Unknown error"}"),
        )
      }
    val nonEmptyBlocks =
      if (blocks.isEmpty()) {
        listOf(DocumentBlock.Paragraph("(Empty ${format.badgeLabel} document)"))
      } else {
        blocks
      }
    return ParsedDocumentContent(
      format = format,
      title = title,
      blocks = nonEmptyBlocks,
    )
  }

  private fun readTextSafe(file: File, maxBytes: Int = 512 * 1024): String {
    val length = file.length().coerceAtLeast(0L)
    if (length == 0L) return ""
    val bytesToRead = min(length, maxBytes.toLong()).toInt()
    val buffer = ByteArray(bytesToRead)
    FileInputStream(file).use { fis ->
      var offset = 0
      while (offset < bytesToRead) {
        val read = fis.read(buffer, offset, bytesToRead - offset)
        if (read <= 0) break
        offset += read
      }
    }
    return decodeBestEffortCharset(buffer)
  }

  private fun decodeBestEffortCharset(bytes: ByteArray): String {
    if (bytes.size >= 2) {
      val b0 = bytes[0].toInt() and 0xFF
      val b1 = bytes[1].toInt() and 0xFF
      if (b0 == 0xFE && b1 == 0xFF) {
        return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
      }
      if (b0 == 0xFF && b1 == 0xFE) {
        return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
      }
    }
    if (bytes.size >= 3) {
      val b0 = bytes[0].toInt() and 0xFF
      val b1 = bytes[1].toInt() and 0xFF
      val b2 = bytes[2].toInt() and 0xFF
      if (b0 == 0xEF && b1 == 0xBB && b2 == 0xBF) {
        return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
      }
    }
    return String(bytes, Charsets.UTF_8)
  }

  internal fun parsePlainTextBlocks(raw: String, isMonospace: Boolean = false): List<DocumentBlock> {
    val normalized = raw.replace("\r\n", "\n").replace('\r', '\n')
    val lines = normalized.lines()
    if (lines.all { it.isBlank() }) return emptyList()
    val blocks = mutableListOf<DocumentBlock>()
    for (line in lines) {
      val expanded = line.replace("\t", "    ")
      blocks.add(DocumentBlock.Paragraph(text = expanded, isMonospace = isMonospace))
    }
    return blocks
  }

  internal fun parseLogBlocks(raw: String): List<DocumentBlock> {
    val normalized = raw.replace("\r\n", "\n").replace('\r', '\n')
    val lines = normalized.lines()
    if (lines.all { it.isBlank() }) return emptyList()
    return lines.map { line ->
      DocumentBlock.Paragraph(text = line.replace("\t", "  "), isMonospace = true)
    }
  }

  internal fun parseMarkdownBlocks(raw: String): List<DocumentBlock> {
    val lines = raw.replace("\r\n", "\n").replace('\r', '\n').lines()
    val blocks = mutableListOf<DocumentBlock>()
    var i = 0
    while (i < lines.size) {
      val line = lines[i]
      val trimmed = line.trim()

      // Fenced code block
      if (trimmed.startsWith("```")) {
        val codeLines = mutableListOf<String>()
        i++
        while (i < lines.size && !lines[i].trim().startsWith("```")) {
          codeLines.add(lines[i].replace("\t", "  "))
          i++
        }
        blocks.add(DocumentBlock.CodeBlock(codeLines.ifEmpty { listOf("") }))
        i++
        continue
      }

      // Markdown table
      if (trimmed.startsWith("|") && trimmed.endsWith("|") && trimmed.count { it == '|' } >= 2) {
        val tableLines = mutableListOf<String>()
        while (i < lines.size) {
          val candidate = lines[i].trim()
          if (candidate.startsWith("|") && candidate.count { it == '|' } >= 2) {
            tableLines.add(candidate)
            i++
          } else {
            break
          }
        }
        val parsedRows =
          tableLines
            .map { rowStr ->
              rowStr.trim('|').split('|').map { cleanInlineMarkdown(it.trim()) }
            }
            .filterNot { rowCells ->
              rowCells.all { cell -> cell.all { ch -> ch == '-' || ch == ':' || ch == ' ' } && cell.contains('-') }
            }
        if (parsedRows.isNotEmpty()) {
          val headers = parsedRows.first()
          val rows = parsedRows.drop(1)
          blocks.add(DocumentBlock.Table(headers = headers, rows = rows))
        }
        continue
      }

      when {
        trimmed.isEmpty() -> {
          blocks.add(DocumentBlock.Paragraph(""))
        }
        trimmed.matches(Regex("^(-{3,}|\\*{3,}|_{3,})$")) -> {
          blocks.add(DocumentBlock.Divider)
        }
        trimmed.startsWith("### ") -> {
          blocks.add(DocumentBlock.Heading(cleanInlineMarkdown(trimmed.removePrefix("### ").trim()), level = 3))
        }
        trimmed.startsWith("## ") -> {
          blocks.add(DocumentBlock.Heading(cleanInlineMarkdown(trimmed.removePrefix("## ").trim()), level = 2))
        }
        trimmed.startsWith("# ") -> {
          blocks.add(DocumentBlock.Heading(cleanInlineMarkdown(trimmed.removePrefix("# ").trim()), level = 1))
        }
        trimmed.startsWith("- [x] ", ignoreCase = true) -> {
          blocks.add(
            DocumentBlock.BulletItem(
              text = cleanInlineMarkdown(trimmed.substring(6).trim()),
              marker = "☑",
            )
          )
        }
        trimmed.startsWith("- [ ] ") -> {
          blocks.add(
            DocumentBlock.BulletItem(
              text = cleanInlineMarkdown(trimmed.substring(6).trim()),
              marker = "☐",
            )
          )
        }
        trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("+ ") -> {
          blocks.add(
            DocumentBlock.BulletItem(
              text = cleanInlineMarkdown(trimmed.substring(2).trim()),
              marker = "•",
            )
          )
        }
        trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
          val dotIdx = trimmed.indexOf('.')
          val marker = trimmed.substring(0, dotIdx + 1)
          val content = trimmed.substring(dotIdx + 1).trim()
          blocks.add(DocumentBlock.BulletItem(text = cleanInlineMarkdown(content), marker = marker))
        }
        trimmed.startsWith(">") -> {
          blocks.add(
            DocumentBlock.Paragraph(
              text = cleanInlineMarkdown(trimmed.removePrefix(">").trim()),
              isQuote = true,
            )
          )
        }
        else -> {
          blocks.add(DocumentBlock.Paragraph(cleanInlineMarkdown(line)))
        }
      }
      i++
    }
    return blocks
  }

  private fun cleanInlineMarkdown(text: String): String {
    return text
      .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
      .replace(Regex("__(.+?)__"), "$1")
      .replace(Regex("`([^`]+)`"), "$1")
      .replace(Regex("\\[(.+?)]\\((.+?)\\)"), "$1 ($2)")
  }

  internal fun parseRtfBlocks(raw: String): List<DocumentBlock> {
    val plain = stripRtfToPlainText(raw)
    val lines = plain.replace("\r\n", "\n").replace('\r', '\n').lines()
    val blocks = mutableListOf<DocumentBlock>()
    for ((idx, line) in lines.withIndex()) {
      val trimmed = line.trim()
      if (idx == 0 && trimmed.isNotEmpty() && trimmed.length <= 80 && !trimmed.endsWith(".")) {
        blocks.add(DocumentBlock.Heading(trimmed, level = 1))
      } else if (trimmed.startsWith("• ") || trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
        blocks.add(DocumentBlock.BulletItem(trimmed.substring(2).trim(), marker = "•"))
      } else {
        blocks.add(DocumentBlock.Paragraph(line))
      }
    }
    return blocks
  }

  internal fun stripRtfToPlainText(rtf: String): String {
    val trimmed = rtf.trim()
    if (!trimmed.startsWith("{\\rtf")) {
      return rtf
    }
    val sb = StringBuilder()
    var i = 0
    val len = rtf.length
    var braceDepth = 0
    var skipGroupDepth = -1
    val ignoredDestinations =
      setOf(
        "fonttbl",
        "colortbl",
        "stylesheet",
        "info",
        "pict",
        "object",
        "header",
        "footer",
        "generator",
        "xmlnstbl",
      )

    while (i < len) {
      val ch = rtf[i]
      when (ch) {
        '{' -> {
          braceDepth++
          // Check if starts with \*
          if (i + 2 < len && rtf[i + 1] == '\\' && rtf[i + 2] == '*' && skipGroupDepth < 0) {
            skipGroupDepth = braceDepth
          }
          i++
        }
        '}' -> {
          if (braceDepth == skipGroupDepth) {
            skipGroupDepth = -1
          }
          braceDepth = (braceDepth - 1).coerceAtLeast(0)
          i++
        }
        '\\' -> {
          i++
          if (i >= len) break
          val next = rtf[i]
          when {
            next == '\\' || next == '{' || next == '}' -> {
              if (skipGroupDepth < 0) sb.append(next)
              i++
            }
            next == '~' -> {
              if (skipGroupDepth < 0) sb.append(' ')
              i++
            }
            next == '-' || next == '_' -> {
              if (skipGroupDepth < 0) sb.append('-')
              i++
            }
            next == '\n' || next == '\r' -> {
              if (skipGroupDepth < 0) sb.append('\n')
              i++
            }
            next == '\'' -> {
              // Hex char \'xx
              if (i + 2 < len) {
                val hex = rtf.substring(i + 1, i + 3)
                val code = hex.toIntOrNull(16)
                if (code != null && skipGroupDepth < 0) {
                  sb.append(code.toChar())
                }
                i += 3
              } else {
                i++
              }
            }
            next.isLetter() -> {
              val wordStart = i
              while (i < len && rtf[i].isLetter()) i++
              val word = rtf.substring(wordStart, i)
              val numStart = i
              if (i < len && (rtf[i] == '-' || rtf[i].isDigit())) {
                i++
                while (i < len && rtf[i].isDigit()) i++
              }
              val numStr = rtf.substring(numStart, i)
              // Optional single space delimiter consumed by control word
              if (i < len && rtf[i] == ' ') i++

              if (word in ignoredDestinations && skipGroupDepth < 0) {
                skipGroupDepth = braceDepth
              } else if (skipGroupDepth < 0) {
                when (word) {
                  "par", "line", "sect", "page", "row" -> sb.append('\n')
                  "tab", "cell" -> sb.append("    ")
                  "bullet" -> sb.append("• ")
                  "endash" -> sb.append("–")
                  "emdash" -> sb.append("—")
                  "lquote" -> sb.append("‘")
                  "rquote" -> sb.append("’")
                  "ldblquote" -> sb.append("“")
                  "rdblquote" -> sb.append("”")
                  "u" -> {
                    val codePoint = numStr.toIntOrNull()
                    if (codePoint != null) {
                      val unsigned = if (codePoint < 0) codePoint + 65536 else codePoint
                      sb.append(unsigned.toChar())
                      // Skip fallback char (often '?')
                      if (i < len && rtf[i] == '?') i++
                    }
                  }
                }
              }
            }
            else -> {
              i++
            }
          }
        }
        '\r', '\n' -> {
          i++
        }
        else -> {
          if (skipGroupDepth < 0) {
            sb.append(ch)
          }
          i++
        }
      }
    }
    return sb.toString().trim()
  }

  internal fun parseHtmlBlocks(rawHtml: String): List<DocumentBlock> {
    val cleaned =
      rawHtml
        .replace(Regex("<style[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("<script[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("<!--[\\s\\S]*?-->"), "")

    val blocks = mutableListOf<DocumentBlock>()

    // Check for title
    val titleMatch = Regex("<title[^>]*>([\\s\\S]*?)</title>", RegexOption.IGNORE_CASE).find(cleaned)
    val extractedTitle = titleMatch?.groupValues?.get(1)?.let { cleanHtmlInline(it) }?.takeIf { it.isNotBlank() }
    if (extractedTitle != null) {
      blocks.add(DocumentBlock.Heading(extractedTitle, level = 1))
    }

    val bodyContent =
      Regex("<body[^>]*>([\\s\\S]*?)</body>", RegexOption.IGNORE_CASE)
        .find(cleaned)
        ?.groupValues
        ?.get(1)
        ?: cleaned

    // Tokenize block-level elements in document order
    val tokenRegex =
      Regex(
        "<(h[1-6]|p|li|pre|blockquote|table|hr|div)([^>]*)>([\\s\\S]*?)</\\1>|<(hr|br)\\s*/?>",
        RegexOption.IGNORE_CASE,
      )
    val matches = tokenRegex.findAll(bodyContent).toList()
    if (matches.isEmpty()) {
      val plain = cleanHtmlInline(bodyContent)
      return blocks + parsePlainTextBlocks(plain)
    }

    for (match in matches) {
      val tag = match.groupValues[1].lowercase(Locale.ROOT)
      val selfClosingTag = match.groupValues[4].lowercase(Locale.ROOT)
      val inner = match.groupValues[3]

      when {
        selfClosingTag == "hr" || tag == "hr" -> blocks.add(DocumentBlock.Divider)
        selfClosingTag == "br" -> blocks.add(DocumentBlock.Paragraph(""))
        tag.startsWith("h") && tag.length == 2 -> {
          val level = tag[1].digitToIntOrNull()?.coerceIn(1, 3) ?: 2
          val headingText = cleanHtmlInline(inner)
          if (headingText.isNotBlank() && headingText != extractedTitle) {
            blocks.add(DocumentBlock.Heading(headingText, level = level))
          }
        }
        tag == "li" -> {
          val itemText = cleanHtmlInline(inner)
          if (itemText.isNotBlank()) {
            blocks.add(DocumentBlock.BulletItem(itemText, marker = "•"))
          }
        }
        tag == "pre" -> {
          val codeText = unescapeHtmlEntities(inner.replace(Regex("<[^>]+>"), ""))
          blocks.add(DocumentBlock.CodeBlock(codeText.replace("\r\n", "\n").lines()))
        }
        tag == "blockquote" -> {
          val quoteText = cleanHtmlInline(inner)
          if (quoteText.isNotBlank()) {
            blocks.add(DocumentBlock.Paragraph(quoteText, isQuote = true))
          }
        }
        tag == "table" -> {
          val rows = mutableListOf<List<String>>()
          val trRegex = Regex("<tr[^>]*>([\\s\\S]*?)</tr>", RegexOption.IGNORE_CASE)
          val cellRegex = Regex("<t[hd][^>]*>([\\s\\S]*?)</t[hd]>", RegexOption.IGNORE_CASE)
          for (tr in trRegex.findAll(inner)) {
            val cells =
              cellRegex
                .findAll(tr.groupValues[1])
                .map { cleanHtmlInline(it.groupValues[1]) }
                .toList()
            if (cells.isNotEmpty()) {
              rows.add(cells)
            }
          }
          if (rows.isNotEmpty()) {
            blocks.add(DocumentBlock.Table(headers = rows.first(), rows = rows.drop(1)))
          }
        }
        tag == "p" || tag == "div" -> {
          val pText = cleanHtmlInline(inner)
          if (pText.isNotBlank()) {
            for (line in pText.lines()) {
              blocks.add(DocumentBlock.Paragraph(line.trim()))
            }
          }
        }
      }
    }

    if (blocks.isEmpty()) {
      val fallback = cleanHtmlInline(bodyContent)
      blocks.addAll(parsePlainTextBlocks(fallback))
    }
    return blocks
  }

  private fun cleanHtmlInline(htmlFragment: String): String {
    val withBreaks =
      htmlFragment
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("</(p|div|li|tr|h[1-6])>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<[^>]+>"), "")
    return unescapeHtmlEntities(withBreaks)
      .lines()
      .map { it.replace(Regex("\\s+"), " ").trim() }
      .joinToString("\n")
      .trim()
  }

  private fun unescapeHtmlEntities(text: String): String {
    return text
      .replace("&nbsp;", " ")
      .replace("&amp;", "&")
      .replace("&lt;", "<")
      .replace("&gt;", ">")
      .replace("&quot;", "\"")
      .replace("&apos;", "'")
      .replace("&#39;", "'")
      .replace("&mdash;", "—")
      .replace("&ndash;", "–")
      .replace("&bull;", "•")
      .replace(Regex("&#(\\d+);")) { m ->
        val code = m.groupValues[1].toIntOrNull()
        if (code != null) code.toChar().toString() else m.value
      }
  }

  // =========================================================================
  // Office Open XML (DOCX, XLSX, PPTX) & OpenDocument (ODT, ODS, ODP) Parsers
  // =========================================================================

  private fun readZipXmlEntries(file: File): Map<String, String> {
    val entries = linkedMapOf<String, String>()
    try {
      ZipInputStream(BufferedInputStream(FileInputStream(file))).use { zis ->
        var entry = zis.nextEntry
        while (entry != null) {
          val name = entry.name.removePrefix("/")
          if (!entry.isDirectory &&
            (name.endsWith(".xml", ignoreCase = true) || name.endsWith(".rels", ignoreCase = true))
          ) {
            val bytes = zis.readBytes()
            entries[name] = String(bytes, Charsets.UTF_8)
          }
          zis.closeEntry()
          entry = zis.nextEntry
        }
      }
    } catch (_: Exception) {
      // Non-zip or legacy file fallback handled by caller
    }
    return entries
  }

  internal fun parseDocxBlocks(file: File): List<DocumentBlock> {
    val entries = readZipXmlEntries(file)
    val documentXml = entries["word/document.xml"]
    if (documentXml.isNullOrBlank()) {
      // Fallback for plain text or legacy .doc file with readable strings
      return parseLegacyOrPlainFallback(file, "Word Document")
    }

    val blocks = mutableListOf<DocumentBlock>()
    // Match paragraphs <w:p ...>...</w:p> and tables <w:tbl ...>...</w:tbl> in order
    val blockRegex = Regex("<w:(tbl|p)(?:\\s[^>]*)?>([\\s\\S]*?)</w:\\1>")
    for (match in blockRegex.findAll(documentXml)) {
      val type = match.groupValues[1]
      val inner = match.groupValues[2]
      if (type == "tbl") {
        val rows = mutableListOf<List<String>>()
        val trRegex = Regex("<w:tr(?:\\s[^>]*)?>([\\s\\S]*?)</w:tr>")
        val tcRegex = Regex("<w:tc(?:\\s[^>]*)?>([\\s\\S]*?)</w:tc>")
        for (tr in trRegex.findAll(inner)) {
          val cells =
            tcRegex
              .findAll(tr.groupValues[1])
              .map { extractWordRunsText(it.groupValues[1]).trim() }
              .toList()
          if (cells.isNotEmpty()) {
            rows.add(cells)
          }
        }
        if (rows.isNotEmpty()) {
          blocks.add(DocumentBlock.Table(headers = rows.first(), rows = rows.drop(1)))
        }
      } else {
        val text = extractWordRunsText(inner).trim()
        val styleId =
          Regex("<w:pStyle\\s+w:val=\"([^\"]+)\"")
            .find(inner)
            ?.groupValues
            ?.get(1)
            ?.lowercase(Locale.ROOT)
            ?: ""
        val isListParagraph = inner.contains("<w:numPr") || styleId.contains("list")
        if (text.isEmpty()) {
          blocks.add(DocumentBlock.Paragraph(""))
        } else if (styleId.startsWith("heading1") || styleId == "title" || styleId == "h1") {
          blocks.add(DocumentBlock.Heading(text, level = 1))
        } else if (styleId.startsWith("heading2") || styleId == "subtitle" || styleId == "h2") {
          blocks.add(DocumentBlock.Heading(text, level = 2))
        } else if (styleId.startsWith("heading")) {
          blocks.add(DocumentBlock.Heading(text, level = 3))
        } else if (isListParagraph) {
          blocks.add(DocumentBlock.BulletItem(text, marker = "•"))
        } else {
          blocks.add(DocumentBlock.Paragraph(text))
        }
      }
    }
    return blocks
  }

  private fun extractWordRunsText(xmlFragment: String): String {
    val sb = StringBuilder()
    val tokenRegex = Regex("<w:t(?:\\s[^>]*)?>([\\s\\S]*?)</w:t>|<w:(tab|br|cr)\\s*/?>")
    for (token in tokenRegex.findAll(xmlFragment)) {
      val tag = token.groupValues[2]
      when (tag) {
        "tab" -> sb.append("    ")
        "br", "cr" -> sb.append('\n')
        else -> sb.append(unescapeHtmlEntities(token.groupValues[1]))
      }
    }
    return sb.toString()
  }

  internal fun parseXlsxBlocks(file: File): List<DocumentBlock> {
    val entries = readZipXmlEntries(file)
    val sheetEntries =
      entries.keys
        .filter { it.startsWith("xl/worksheets/sheet") && it.endsWith(".xml") }
        .sorted()
    if (sheetEntries.isEmpty()) {
      return parseLegacyOrPlainFallback(file, "Spreadsheet")
    }

    // Parse shared strings table if present
    val sharedStrings = mutableListOf<String>()
    val sharedStringsXml = entries["xl/sharedStrings.xml"]
    if (!sharedStringsXml.isNullOrBlank()) {
      val siRegex = Regex("<si(?:\\s[^>]*)?>([\\s\\S]*?)</si>")
      val tRegex = Regex("<t(?:\\s[^>]*)?>([\\s\\S]*?)</t>")
      for (si in siRegex.findAll(sharedStringsXml)) {
        val combined =
          tRegex.findAll(si.groupValues[1]).joinToString("") { unescapeHtmlEntities(it.groupValues[1]) }
        sharedStrings.add(combined)
      }
    }

    // Extract sheet names from workbook.xml if available
    val sheetNames = mutableListOf<String>()
    val workbookXml = entries["xl/workbook.xml"]
    if (!workbookXml.isNullOrBlank()) {
      val sheetNameRegex = Regex("<sheet\\s+[^>]*name=\"([^\"]+)\"")
      for (m in sheetNameRegex.findAll(workbookXml)) {
        sheetNames.add(unescapeHtmlEntities(m.groupValues[1]))
      }
    }

    val blocks = mutableListOf<DocumentBlock>()
    sheetEntries.forEachIndexed { sheetIdx, entryName ->
      val sheetXml = entries[entryName] ?: return@forEachIndexed
      val sheetTitle = sheetNames.getOrNull(sheetIdx) ?: "Sheet ${sheetIdx + 1}"
      val rows = parseXlsxWorksheetRows(sheetXml, sharedStrings)
      if (rows.isNotEmpty()) {
        val maxCols = rows.maxOf { it.size }.coerceAtLeast(1)
        val normalizedRows = rows.map { r -> List(maxCols) { col -> r.getOrNull(col) ?: "" } }
        blocks.add(
          DocumentBlock.Table(
            headers = normalizedRows.first(),
            rows = normalizedRows.drop(1),
            title = sheetTitle,
          )
        )
      } else {
        blocks.add(DocumentBlock.Heading(sheetTitle, level = 2))
        blocks.add(DocumentBlock.Paragraph("(Empty worksheet)"))
      }
    }
    return blocks
  }

  private fun parseXlsxWorksheetRows(
    sheetXml: String,
    sharedStrings: List<String>,
  ): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    val rowRegex = Regex("<row(?:\\s[^>]*)?>([\\s\\S]*?)</row>")
    val cellRegex = Regex("<c(\\s[^>]*)?>([\\s\\S]*?)</c>")
    val attrRefRegex = Regex("r=\"([A-Z]+)\\d+\"")
    val attrTypeRegex = Regex("t=\"([^\"]+)\"")
    val vRegex = Regex("<v>([\\s\\S]*?)</v>")
    val isTextRegex = Regex("<t(?:\\s[^>]*)?>([\\s\\S]*?)</t>")

    for (rowMatch in rowRegex.findAll(sheetXml)) {
      val rowInner = rowMatch.groupValues[1]
      val rowCells = linkedMapOf<Int, String>()
      var nextColIdx = 0
      for (cellMatch in cellRegex.findAll(rowInner)) {
        val attrs = cellMatch.groupValues[1]
        val cellInner = cellMatch.groupValues[2]
        val colLetters = attrRefRegex.find(attrs)?.groupValues?.get(1)
        val colIndex = if (colLetters != null) excelColLettersToIndex(colLetters) else nextColIdx
        nextColIdx = colIndex + 1

        val cellType = attrTypeRegex.find(attrs)?.groupValues?.get(1) ?: ""
        val rawVal = vRegex.find(cellInner)?.groupValues?.get(1)?.trim()
        val value =
          when (cellType) {
            "s" -> {
              val ssIdx = rawVal?.toIntOrNull()
              if (ssIdx != null && ssIdx in sharedStrings.indices) sharedStrings[ssIdx] else (rawVal ?: "")
            }
            "inlineStr", "str" -> {
              isTextRegex.findAll(cellInner).joinToString("") { unescapeHtmlEntities(it.groupValues[1]) }
                .ifEmpty { unescapeHtmlEntities(rawVal ?: "") }
            }
            "b" -> if (rawVal == "1") "TRUE" else "FALSE"
            else -> unescapeHtmlEntities(rawVal ?: "")
          }
        rowCells[colIndex] = value
      }
      if (rowCells.isNotEmpty()) {
        val maxCol = (rowCells.keys.maxOrNull() ?: 0).coerceAtMost(24)
        val rowList = (0..maxCol).map { rowCells[it] ?: "" }
        if (rowList.any { it.isNotBlank() }) {
          rows.add(rowList)
        }
      }
    }
    return rows
  }

  private fun excelColLettersToIndex(letters: String): Int {
    var result = 0
    for (ch in letters.uppercase(Locale.ROOT)) {
      if (ch in 'A'..'Z') {
        result = result * 26 + (ch - 'A' + 1)
      }
    }
    return (result - 1).coerceAtLeast(0)
  }

  internal fun parsePptxBlocks(file: File): List<DocumentBlock> {
    val entries = readZipXmlEntries(file)
    val slideKeys =
      entries.keys
        .filter { it.matches(Regex("ppt/slides/slide\\d+\\.xml")) }
        .sortedBy { key ->
          Regex("slide(\\d+)\\.xml").find(key)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        }
    if (slideKeys.isEmpty()) {
      return parseLegacyOrPlainFallback(file, "Presentation")
    }

    val blocks = mutableListOf<DocumentBlock>()
    slideKeys.forEachIndexed { idx, key ->
      val slideXml = entries[key] ?: return@forEachIndexed
      val paragraphs = mutableListOf<String>()
      val pRegex = Regex("<a:p(?:\\s[^>]*)?>([\\s\\S]*?)</a:p>")
      val tRegex = Regex("<a:t(?:\\s[^>]*)?>([\\s\\S]*?)</a:t>")
      for (p in pRegex.findAll(slideXml)) {
        val line =
          tRegex
            .findAll(p.groupValues[1])
            .joinToString("") { unescapeHtmlEntities(it.groupValues[1]) }
            .trim()
        if (line.isNotEmpty()) {
          paragraphs.add(line)
        }
      }
      val slideTitle = paragraphs.firstOrNull() ?: "Slide ${idx + 1}"
      val slideBullets = paragraphs.drop(1)
      if (idx > 0) {
        blocks.add(DocumentBlock.PageBreak)
      }
      blocks.add(
        DocumentBlock.SlideCard(
          slideNumber = idx + 1,
          title = slideTitle,
          bullets = slideBullets,
        )
      )
    }
    return blocks
  }

  internal fun parseOdtBlocks(file: File): List<DocumentBlock> {
    val entries = readZipXmlEntries(file)
    val contentXml = entries["content.xml"] ?: return parseLegacyOrPlainFallback(file, "OpenDocument Text")
    val blocks = mutableListOf<DocumentBlock>()
    val elementRegex =
      Regex("<text:(h|p|list-item)(?:\\s([^>]*))?>([\\s\\S]*?)</text:\\1>|<table:table(?:\\s[^>]*)?>([\\s\\S]*?)</table:table>")
    for (match in elementRegex.findAll(contentXml)) {
      val tag = match.groupValues[1]
      val attrs = match.groupValues[2]
      val inner = match.groupValues[3]
      val tableInner = match.groupValues[4]

      if (tableInner.isNotEmpty()) {
        val rows = parseOdfTableRows(tableInner)
        if (rows.isNotEmpty()) {
          blocks.add(DocumentBlock.Table(headers = rows.first(), rows = rows.drop(1)))
        }
      } else {
        val text = cleanOdfInlineText(inner)
        when (tag) {
          "h" -> {
            val level =
              Regex("text:outline-level=\"(\\d+)\"")
                .find(attrs)
                ?.groupValues
                ?.get(1)
                ?.toIntOrNull()
                ?.coerceIn(1, 3)
                ?: 1
            if (text.isNotBlank()) blocks.add(DocumentBlock.Heading(text, level = level))
          }
          "list-item" -> {
            if (text.isNotBlank()) blocks.add(DocumentBlock.BulletItem(text, marker = "•"))
          }
          "p" -> {
            blocks.add(DocumentBlock.Paragraph(text))
          }
        }
      }
    }
    return blocks
  }

  internal fun parseOdsBlocks(file: File): List<DocumentBlock> {
    val entries = readZipXmlEntries(file)
    val contentXml = entries["content.xml"] ?: return parseLegacyOrPlainFallback(file, "OpenDocument Spreadsheet")
    val blocks = mutableListOf<DocumentBlock>()
    val tableRegex = Regex("<table:table(\\s[^>]*)?>([\\s\\S]*?)</table:table>")
    var sheetIdx = 1
    for (tableMatch in tableRegex.findAll(contentXml)) {
      val attrs = tableMatch.groupValues[1]
      val inner = tableMatch.groupValues[2]
      val tableName =
        Regex("table:name=\"([^\"]+)\"")
          .find(attrs)
          ?.groupValues
          ?.get(1)
          ?.let { unescapeHtmlEntities(it) }
          ?: "Sheet $sheetIdx"
      sheetIdx++
      val rows = parseOdfTableRows(inner)
      if (rows.isNotEmpty()) {
        val maxCols = rows.maxOf { it.size }.coerceAtLeast(1)
        val normalized = rows.map { r -> List(maxCols) { c -> r.getOrNull(c) ?: "" } }
        blocks.add(
          DocumentBlock.Table(
            headers = normalized.first(),
            rows = normalized.drop(1),
            title = tableName,
          )
        )
      }
    }
    return blocks
  }

  internal fun parseOdpBlocks(file: File): List<DocumentBlock> {
    val entries = readZipXmlEntries(file)
    val contentXml = entries["content.xml"] ?: return parseLegacyOrPlainFallback(file, "OpenDocument Presentation")
    val blocks = mutableListOf<DocumentBlock>()
    val pageRegex = Regex("<draw:page(\\s[^>]*)?>([\\s\\S]*?)</draw:page>")
    val pRegex = Regex("<text:p(?:\\s[^>]*)?>([\\s\\S]*?)</text:p>")
    var slideIdx = 0
    for (pageMatch in pageRegex.findAll(contentXml)) {
      val attrs = pageMatch.groupValues[1]
      val inner = pageMatch.groupValues[2]
      val pageName =
        Regex("draw:name=\"([^\"]+)\"")
          .find(attrs)
          ?.groupValues
          ?.get(1)
          ?.let { unescapeHtmlEntities(it) }
      val lines =
        pRegex
          .findAll(inner)
          .map { cleanOdfInlineText(it.groupValues[1]) }
          .filter { it.isNotBlank() }
          .toList()
      val title = lines.firstOrNull() ?: pageName ?: "Slide ${slideIdx + 1}"
      val bullets = if (lines.isNotEmpty()) lines.drop(1) else emptyList()
      if (slideIdx > 0) {
        blocks.add(DocumentBlock.PageBreak)
      }
      blocks.add(
        DocumentBlock.SlideCard(
          slideNumber = slideIdx + 1,
          title = title,
          bullets = bullets,
        )
      )
      slideIdx++
    }
    return blocks
  }

  private fun parseOdfTableRows(tableInnerXml: String): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    val rowRegex = Regex("<table:table-row(?:\\s[^>]*)?>([\\s\\S]*?)</table:table-row>")
    val cellRegex = Regex("<table:table-cell(\\s[^>]*)?>([\\s\\S]*?)</table:table-cell>|<table:table-cell(\\s[^>]*)?/>")
    for (rowMatch in rowRegex.findAll(tableInnerXml)) {
      val cells = mutableListOf<String>()
      for (cellMatch in cellRegex.findAll(rowMatch.groupValues[1])) {
        val attrs = cellMatch.groupValues[1].ifEmpty { cellMatch.groupValues[3] }
        val inner = cellMatch.groupValues[2]
        val repeat =
          Regex("table:number-columns-repeated=\"(\\d+)\"")
            .find(attrs)
            ?.groupValues
            ?.get(1)
            ?.toIntOrNull()
            ?.coerceIn(1, 16)
            ?: 1
        val text = cleanOdfInlineText(inner)
        repeat(repeat) {
          if (cells.size < 24) cells.add(text)
        }
      }
      while (cells.isNotEmpty() && cells.last().isBlank()) {
        cells.removeAt(cells.lastIndex)
      }
      if (cells.any { it.isNotBlank() }) {
        rows.add(cells)
      }
    }
    return rows
  }

  private fun cleanOdfInlineText(xmlFragment: String): String {
    val withSpaces =
      xmlFragment
        .replace(Regex("<text:tab\\s*/?>"), "    ")
        .replace(Regex("<text:line-break\\s*/?>"), "\n")
        .replace(Regex("<text:s\\s*/?>"), " ")
        .replace(Regex("<[^>]+>"), "")
    return unescapeHtmlEntities(withSpaces).trim()
  }

  private fun parseLegacyOrPlainFallback(file: File, label: String): List<DocumentBlock> {
    val rawText = readTextSafe(file)
    // Extract printable runs if binary or return plain text lines
    val hasBinaryNulls = rawText.count { it == '\u0000' } > 4
    if (!hasBinaryNulls && rawText.isNotBlank()) {
      return parsePlainTextBlocks(rawText)
    }
    val printableRuns =
      Regex("[\\x20-\\x7E]{4,}")
        .findAll(rawText)
        .map { it.value.trim() }
        .filter { it.isNotBlank() }
        .toList()
    if (printableRuns.isEmpty()) {
      return listOf(DocumentBlock.Paragraph("($label: ${file.name})"))
    }
    return printableRuns.map { DocumentBlock.Paragraph(it) }
  }

  // =========================================================================
  // Paginated Document Sheet Renderer (Unified Page Canvas)
  // =========================================================================

  private sealed interface PageDrawOp {
    data class TextLine(
      val text: String,
      val x: Float,
      val y: Float,
      val textSize: Float,
      val color: Int,
      val isBold: Boolean = false,
      val isItalic: Boolean = false,
      val isMonospace: Boolean = false,
    ) : PageDrawOp

    data class FilledRoundRect(
      val rect: RectF,
      val radius: Float,
      val fillColor: Int,
      val strokeColor: Int? = null,
      val strokeWidth: Float = 1f,
    ) : PageDrawOp

    data class Line(
      val x1: Float,
      val y1: Float,
      val x2: Float,
      val y2: Float,
      val color: Int,
      val strokeWidth: Float = 1f,
    ) : PageDrawOp
  }

  private data class PaginatedSheetLayout(
    val pageWidthPt: Int,
    val pageHeightPt: Int,
    val pages: List<List<PageDrawOp>>,
  )

  private fun renderStructuredDocumentToBitmaps(
    file: File,
    format: DocumentFormat,
    maxDimension: Int,
  ): List<Bitmap> {
    val parsed = parseDocumentContent(file)
    val layout = layoutPaginatedDocument(parsed, file.name)
    val count = layout.pages.size.coerceAtLeast(1)
    val effectiveMaxDim =
      when {
        count > 30 -> min(maxDimension, 960)
        count > 12 -> min(maxDimension, 1200)
        else -> maxDimension
      }
    val rawW = layout.pageWidthPt
    val rawH = layout.pageHeightPt
    val scale = effectiveMaxDim.toFloat() / max(rawW, rawH).toFloat()
    val targetW = (rawW * scale).roundToInt().coerceAtLeast(1)
    val targetH = (rawH * scale).roundToInt().coerceAtLeast(1)

    return layout.pages.mapIndexed { pageIdx, ops ->
      val bmp = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bmp)
      drawPaginatedSheetOnCanvas(
        canvas = canvas,
        scale = scale,
        pageWidthPt = rawW,
        pageHeightPt = rawH,
        pageIndex = pageIdx,
        pageCount = count,
        documentTitle = file.name,
        formatBadge = format.badgeLabel,
        ops = ops,
      )
      bmp
    }
  }

  private fun renderStructuredDocumentToBasePdf(
    sourceFile: File,
    format: DocumentFormat,
    outputPdfFile: File,
  ): Boolean {
    val parsed = parseDocumentContent(sourceFile)
    val layout = layoutPaginatedDocument(parsed, sourceFile.name)
    val pdf = PdfDocument()
    return try {
      val count = layout.pages.size.coerceAtLeast(1)
      layout.pages.forEachIndexed { idx, ops ->
        val pageInfo =
          PdfDocument.PageInfo.Builder(layout.pageWidthPt, layout.pageHeightPt, idx + 1).create()
        val page = pdf.startPage(pageInfo)
        drawPaginatedSheetOnCanvas(
          canvas = page.canvas,
          scale = 1f,
          pageWidthPt = layout.pageWidthPt,
          pageHeightPt = layout.pageHeightPt,
          pageIndex = idx,
          pageCount = count,
          documentTitle = sourceFile.name,
          formatBadge = format.badgeLabel,
          ops = ops,
        )
        pdf.finishPage(page)
      }
      outputPdfFile.parentFile?.mkdirs()
      FileOutputStream(outputPdfFile).use { fos ->
        pdf.writeTo(fos)
      }
      outputPdfFile.exists() && outputPdfFile.length() > 0L
    } catch (_: Exception) {
      false
    } finally {
      pdf.close()
    }
  }

  private fun drawPaginatedSheetOnCanvas(
    canvas: Canvas,
    scale: Float,
    pageWidthPt: Int,
    pageHeightPt: Int,
    pageIndex: Int,
    pageCount: Int,
    documentTitle: String,
    formatBadge: String,
    ops: List<PageDrawOp>,
  ) {
    canvas.save()
    canvas.scale(scale, scale)
    canvas.drawColor(android.graphics.Color.WHITE)

    // Top document header banner bar
    val headerLinePaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(225, 231, 240)
        strokeWidth = 1f
      }
    val badgePaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(235, 241, 252)
        style = Paint.Style.FILL
      }
    val badgeTextPaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(33, 64, 154)
        textSize = 9.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      }
    val headerTitlePaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(110, 120, 135)
        textSize = 9.5f
      }

    val badgeW = badgeTextPaint.measureText(formatBadge) + 14f
    val badgeRect = RectF(42f, 18f, 42f + badgeW, 34f)
    canvas.drawRoundRect(badgeRect, 5f, 5f, badgePaint)
    canvas.drawText(formatBadge, 49f, 29.5f, badgeTextPaint)
    canvas.drawText(documentTitle, 42f + badgeW + 8f, 29.5f, headerTitlePaint)
    canvas.drawLine(42f, 40f, pageWidthPt - 42f, 40f, headerLinePaint)

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    for (op in ops) {
      when (op) {
        is PageDrawOp.FilledRoundRect -> {
          fillPaint.color = op.fillColor
          canvas.drawRoundRect(op.rect, op.radius, op.radius, fillPaint)
          if (op.strokeColor != null) {
            strokePaint.color = op.strokeColor
            strokePaint.strokeWidth = op.strokeWidth
            canvas.drawRoundRect(op.rect, op.radius, op.radius, strokePaint)
          }
        }
        is PageDrawOp.Line -> {
          strokePaint.color = op.color
          strokePaint.strokeWidth = op.strokeWidth
          canvas.drawLine(op.x1, op.y1, op.x2, op.y2, strokePaint)
        }
        is PageDrawOp.TextLine -> {
          textPaint.color = op.color
          textPaint.textSize = op.textSize
          val baseFamily = if (op.isMonospace) Typeface.MONOSPACE else Typeface.DEFAULT
          val style =
            when {
              op.isBold && op.isItalic -> Typeface.BOLD_ITALIC
              op.isBold -> Typeface.BOLD
              op.isItalic -> Typeface.ITALIC
              else -> Typeface.NORMAL
            }
          textPaint.typeface = Typeface.create(baseFamily, style)
          canvas.drawText(op.text, op.x, op.y, textPaint)
        }
      }
    }

    // Bottom footer
    val footerPaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(135, 143, 156)
        textSize = 10f
      }
    canvas.drawLine(42f, pageHeightPt - 38f, pageWidthPt - 42f, pageHeightPt - 38f, headerLinePaint)
    canvas.drawText(
      "Toolbox Document Viewer • $formatBadge • Page ${pageIndex + 1} / $pageCount",
      42f,
      pageHeightPt - 22f,
      footerPaint,
    )
    canvas.restore()
  }

  private fun layoutPaginatedDocument(
    parsed: ParsedDocumentContent,
    fileName: String,
  ): PaginatedSheetLayout {
    val isLandscape = parsed.format.isPresentationLandscape
    val pageW = if (isLandscape) LANDSCAPE_PAGE_WIDTH_PT else PORTRAIT_PAGE_WIDTH_PT
    val pageH = if (isLandscape) LANDSCAPE_PAGE_HEIGHT_PT else PORTRAIT_PAGE_HEIGHT_PT

    val marginH = 44f
    val topStart = 58f
    val bottomLimit = pageH - 50f
    val usableW = pageW - marginH * 2

    val pages = mutableListOf<MutableList<PageDrawOp>>()
    var currentOps = mutableListOf<PageDrawOp>()
    pages.add(currentOps)
    var cursorY = topStart

    fun startNewPage() {
      if (currentOps.isNotEmpty()) {
        currentOps = mutableListOf()
        pages.add(currentOps)
      }
      cursorY = topStart
    }

    fun ensureSpace(neededHeight: Float) {
      if (cursorY + neededHeight > bottomLimit && currentOps.isNotEmpty()) {
        startNewPage()
      }
    }

    for (block in parsed.blocks) {
      when (block) {
        is DocumentBlock.PageBreak -> {
          startNewPage()
        }
        is DocumentBlock.Divider -> {
          ensureSpace(18f)
          val lineY = cursorY + 8f
          currentOps.add(
            PageDrawOp.Line(
              x1 = marginH,
              y1 = lineY,
              x2 = pageW - marginH,
              y2 = lineY,
              color = android.graphics.Color.rgb(205, 214, 228),
              strokeWidth = 1.5f,
            )
          )
          cursorY += 18f
        }
        is DocumentBlock.Heading -> {
          val fontSize =
            when (block.level) {
              1 -> 20f
              2 -> 16f
              else -> 13.5f
            }
          val lineH = fontSize * 1.35f
          val wrapped = wrapTextByApproxWidth(block.text, usableW, fontSize, isMonospace = false)
          cursorY += 6f
          ensureSpace(lineH * wrapped.size + 10f)
          for (line in wrapped) {
            currentOps.add(
              PageDrawOp.TextLine(
                text = line,
                x = marginH,
                y = cursorY + fontSize,
                textSize = fontSize,
                color = android.graphics.Color.rgb(24, 34, 56),
                isBold = true,
              )
            )
            cursorY += lineH
          }
          if (block.level == 1) {
            currentOps.add(
              PageDrawOp.Line(
                x1 = marginH,
                y1 = cursorY + 2f,
                x2 = pageW - marginH,
                y2 = cursorY + 2f,
                color = android.graphics.Color.rgb(218, 226, 238),
                strokeWidth = 1f,
              )
            )
            cursorY += 8f
          } else {
            cursorY += 4f
          }
        }
        is DocumentBlock.Paragraph -> {
          if (block.text.isBlank()) {
            cursorY += 9f
          } else {
            val fontSize = if (block.isMonospace) 10.5f else 12f
            val lineH = fontSize * 1.42f
            val indent = if (block.isQuote) 16f else 0f
            val wrapped =
              wrapTextByApproxWidth(
                text = block.text,
                maxWidthPt = usableW - indent,
                fontSizePt = fontSize,
                isMonospace = block.isMonospace,
              )
            for (line in wrapped) {
              ensureSpace(lineH)
              if (block.isQuote) {
                currentOps.add(
                  PageDrawOp.Line(
                    x1 = marginH + 4f,
                    y1 = cursorY + 2f,
                    x2 = marginH + 4f,
                    y2 = cursorY + lineH,
                    color = android.graphics.Color.rgb(100, 130, 190),
                    strokeWidth = 2.5f,
                  )
                )
              }
              currentOps.add(
                PageDrawOp.TextLine(
                  text = line,
                  x = marginH + indent,
                  y = cursorY + fontSize,
                  textSize = fontSize,
                  color =
                    if (block.isQuote) {
                      android.graphics.Color.rgb(75, 86, 105)
                    } else {
                      android.graphics.Color.rgb(42, 48, 60)
                    },
                  isItalic = block.isQuote,
                  isMonospace = block.isMonospace,
                )
              )
              cursorY += lineH
            }
            cursorY += 3f
          }
        }
        is DocumentBlock.BulletItem -> {
          val fontSize = 12f
          val lineH = fontSize * 1.4f
          val bulletIndent = 20f
          val wrapped =
            wrapTextByApproxWidth(
              text = block.text,
              maxWidthPt = usableW - bulletIndent,
              fontSizePt = fontSize,
              isMonospace = false,
            )
          wrapped.forEachIndexed { idx, line ->
            ensureSpace(lineH)
            if (idx == 0) {
              currentOps.add(
                PageDrawOp.TextLine(
                  text = block.marker,
                  x = marginH + 2f,
                  y = cursorY + fontSize,
                  textSize = fontSize,
                  color = android.graphics.Color.rgb(33, 64, 154),
                  isBold = true,
                )
              )
            }
            currentOps.add(
              PageDrawOp.TextLine(
                text = line,
                x = marginH + bulletIndent,
                y = cursorY + fontSize,
                textSize = fontSize,
                color = android.graphics.Color.rgb(42, 48, 60),
              )
            )
            cursorY += lineH
          }
          cursorY += 2f
        }
        is DocumentBlock.CodeBlock -> {
          val fontSize = 10f
          val lineH = 14.5f
          val wrappedLines =
            block.lines.flatMap { rawLine ->
              wrapTextByApproxWidth(
                text = rawLine.ifEmpty { " " },
                maxWidthPt = usableW - 20f,
                fontSizePt = fontSize,
                isMonospace = true,
              )
            }
          val chunkSize = ((bottomLimit - topStart - 24f) / lineH).toInt().coerceAtLeast(1)
          for (chunk in wrappedLines.chunked(chunkSize)) {
            val boxH = chunk.size * lineH + 14f
            ensureSpace(boxH + 6f)
            val boxRect = RectF(marginH, cursorY, pageW - marginH, cursorY + boxH)
            currentOps.add(
              PageDrawOp.FilledRoundRect(
                rect = boxRect,
                radius = 6f,
                fillColor = android.graphics.Color.rgb(244, 246, 250),
                strokeColor = android.graphics.Color.rgb(216, 224, 236),
                strokeWidth = 1f,
              )
            )
            var lineY = cursorY + 7f + fontSize
            for (codeLine in chunk) {
              currentOps.add(
                PageDrawOp.TextLine(
                  text = codeLine,
                  x = marginH + 10f,
                  y = lineY,
                  textSize = fontSize,
                  color = android.graphics.Color.rgb(32, 40, 54),
                  isMonospace = true,
                )
              )
              lineY += lineH
            }
            cursorY += boxH + 6f
          }
        }
        is DocumentBlock.Table -> {
          if (!block.title.isNullOrBlank()) {
            ensureSpace(26f)
            currentOps.add(
              PageDrawOp.TextLine(
                text = block.title,
                x = marginH,
                y = cursorY + 15f,
                textSize = 14.5f,
                color = android.graphics.Color.rgb(26, 48, 118),
                isBold = true,
              )
            )
            cursorY += 22f
          }
          val colCount =
            max(
              block.headers.size,
              block.rows.maxOfOrNull { it.size } ?: 1,
            ).coerceIn(1, 10)
          val colWidth = usableW / colCount
          val rowH = 22f
          val fontSize = 9.5f
          val maxCharsPerCell = ((colWidth - 10f) / (fontSize * 0.54f)).toInt().coerceAtLeast(3)

          fun drawTableRow(cells: List<String>, isHeader: Boolean) {
            ensureSpace(rowH + 2f)
            val rowRect = RectF(marginH, cursorY, pageW - marginH, cursorY + rowH)
            currentOps.add(
              PageDrawOp.FilledRoundRect(
                rect = rowRect,
                radius = 0f,
                fillColor =
                  if (isHeader) {
                    android.graphics.Color.rgb(235, 241, 252)
                  } else {
                    android.graphics.Color.WHITE
                  },
                strokeColor = android.graphics.Color.rgb(206, 216, 232),
                strokeWidth = 1f,
              )
            )
            for (c in 0 until colCount) {
              val cellX = marginH + c * colWidth
              if (c > 0) {
                currentOps.add(
                  PageDrawOp.Line(
                    x1 = cellX,
                    y1 = cursorY,
                    x2 = cellX,
                    y2 = cursorY + rowH,
                    color = android.graphics.Color.rgb(216, 224, 236),
                    strokeWidth = 1f,
                  )
                )
              }
              val rawCell = cells.getOrNull(c)?.replace('\n', ' ')?.trim() ?: ""
              val clipped =
                if (rawCell.length > maxCharsPerCell) {
                  rawCell.take((maxCharsPerCell - 1).coerceAtLeast(1)) + "…"
                } else {
                  rawCell
                }
              currentOps.add(
                PageDrawOp.TextLine(
                  text = clipped,
                  x = cellX + 5f,
                  y = cursorY + 14.5f,
                  textSize = fontSize,
                  color =
                    if (isHeader) {
                      android.graphics.Color.rgb(24, 42, 92)
                    } else {
                      android.graphics.Color.rgb(45, 53, 68)
                    },
                  isBold = isHeader,
                )
              )
            }
            cursorY += rowH
          }

          if (block.headers.isNotEmpty()) {
            drawTableRow(block.headers, isHeader = true)
          }
          for (row in block.rows) {
            drawTableRow(row, isHeader = false)
          }
          cursorY += 10f
        }
        is DocumentBlock.SlideCard -> {
          val cardTop = topStart + 4f
          val cardBottom = bottomLimit - 8f
          val cardRect = RectF(marginH, cardTop, pageW - marginH, cardBottom)
          currentOps.add(
            PageDrawOp.FilledRoundRect(
              rect = cardRect,
              radius = 14f,
              fillColor = android.graphics.Color.rgb(247, 249, 253),
              strokeColor = android.graphics.Color.rgb(194, 208, 232),
              strokeWidth = 2f,
            )
          )
          val headerBanner = RectF(marginH, cardTop, pageW - marginH, cardTop + 72f)
          currentOps.add(
            PageDrawOp.FilledRoundRect(
              rect = headerBanner,
              radius = 14f,
              fillColor = android.graphics.Color.rgb(33, 64, 154),
            )
          )
          currentOps.add(
            PageDrawOp.TextLine(
              text = "SLIDE ${block.slideNumber}",
              x = marginH + 24f,
              y = cardTop + 24f,
              textSize = 11f,
              color = android.graphics.Color.rgb(200, 218, 255),
              isBold = true,
            )
          )
          val titleLines = wrapTextByApproxWidth(block.title, usableW - 48f, 22f, isMonospace = false)
          currentOps.add(
            PageDrawOp.TextLine(
              text = titleLines.firstOrNull() ?: block.title,
              x = marginH + 24f,
              y = cardTop + 52f,
              textSize = 21f,
              color = android.graphics.Color.WHITE,
              isBold = true,
            )
          )
          var slideY = cardTop + 104f
          if (block.bullets.isEmpty()) {
            currentOps.add(
              PageDrawOp.TextLine(
                text = "(Slide ${block.slideNumber})",
                x = marginH + 28f,
                y = slideY,
                textSize = 14f,
                color = android.graphics.Color.rgb(110, 120, 136),
                isItalic = true,
              )
            )
          } else {
            for (bullet in block.bullets) {
              val wrappedBullet = wrapTextByApproxWidth(bullet, usableW - 72f, 15f, isMonospace = false)
              wrappedBullet.forEachIndexed { idx, line ->
                if (slideY + 22f <= cardBottom - 16f) {
                  if (idx == 0) {
                    currentOps.add(
                      PageDrawOp.TextLine(
                        text = "•",
                        x = marginH + 28f,
                        y = slideY,
                        textSize = 16f,
                        color = android.graphics.Color.rgb(33, 64, 154),
                        isBold = true,
                      )
                    )
                  }
                  currentOps.add(
                    PageDrawOp.TextLine(
                      text = line,
                      x = marginH + 48f,
                      y = slideY,
                      textSize = 15f,
                      color = android.graphics.Color.rgb(34, 42, 56),
                    )
                  )
                  slideY += 23f
                }
              }
              slideY += 6f
            }
          }
          cursorY = cardBottom
        }
      }
    }

    return PaginatedSheetLayout(
      pageWidthPt = pageW,
      pageHeightPt = pageH,
      pages = pages,
    )
  }

  private fun wrapTextByApproxWidth(
    text: String,
    maxWidthPt: Float,
    fontSizePt: Float,
    isMonospace: Boolean,
  ): List<String> {
    if (text.isEmpty()) return listOf("")
    val avgCharW = fontSizePt * (if (isMonospace) 0.6f else 0.52f)
    val maxChars = (maxWidthPt / avgCharW).toInt().coerceAtLeast(8)
    val result = mutableListOf<String>()
    for (rawLine in text.split('\n')) {
      if (rawLine.length <= maxChars) {
        result.add(rawLine)
        continue
      }
      val words = rawLine.split(' ')
      var current = StringBuilder()
      for (word in words) {
        if (word.length > maxChars) {
          if (current.isNotEmpty()) {
            result.add(current.toString())
            current = StringBuilder()
          }
          var start = 0
          while (start < word.length) {
            val end = min(start + maxChars, word.length)
            result.add(word.substring(start, end))
            start = end
          }
        } else if (current.isEmpty()) {
          current.append(word)
        } else if (current.length + 1 + word.length <= maxChars) {
          current.append(' ').append(word)
        } else {
          result.add(current.toString())
          current = StringBuilder(word)
        }
      }
      if (current.isNotEmpty()) {
        result.add(current.toString())
      }
    }
    return result.ifEmpty { listOf("") }
  }

  // =========================================================================
  // Page Annotation & Export Engine
  // =========================================================================

  fun renderEditedPageBitmap(
    basePageBitmap: Bitmap,
    edits: DocumentPageEdits,
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

  fun saveEditedDocumentAsPdf(
    sourceFile: File,
    outputFile: File,
    pageEdits: Map<Int, DocumentPageEdits>,
    renderScale: Float = 2f,
  ): Result<File> = saveEditedPdf(sourceFile, outputFile, pageEdits, renderScale)

  fun saveEditedPdf(
    sourceFile: File,
    outputFile: File,
    pageEdits: Map<Int, DocumentPageEdits>,
    renderScale: Float = 2f,
  ): Result<File> {
    if (!sourceFile.exists() || !sourceFile.isFile) {
      return Result.failure(IllegalArgumentException("Source document file does not exist"))
    }
    return try {
      outputFile.parentFile?.mkdirs()
      val format = detectFormat(sourceFile) ?: DocumentFormat.TXT
      var tempConvertedPdf: File? = null
      val effectivePdfSource =
        if (format == DocumentFormat.PDF) {
          sourceFile
        } else {
          val converted =
            File(
              outputFile.parentFile ?: sourceFile.parentFile,
              ".tmp_base_${System.currentTimeMillis()}.pdf",
            )
          if (!renderStructuredDocumentToBasePdf(sourceFile, format, converted)) {
            return Result.failure(IllegalStateException("Failed to paginate ${format.displayName} into PDF"))
          }
          tempConvertedPdf = converted
          converted
        }

      try {
        val tempOutput =
          if (effectivePdfSource.absolutePath == outputFile.absolutePath) {
            File(outputFile.parentFile, ".tmp_${System.currentTimeMillis()}_${outputFile.name}")
          } else {
            outputFile
          }

        ParcelFileDescriptor.open(effectivePdfSource, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
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

                  val editsForPage = pageEdits[pageIndex] ?: DocumentPageEdits()
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
      } finally {
        tempConvertedPdf?.delete()
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  // =========================================================================
  // Sample Multi-Format Document Generators
  // =========================================================================

  fun generateSampleDocumentFile(
    outputFile: File,
    format: DocumentFormat = DocumentFormat.fromFile(outputFile) ?: DocumentFormat.PDF,
  ): File {
    outputFile.parentFile?.mkdirs()
    return when (format) {
      DocumentFormat.PDF -> generateSamplePdfFile(outputFile)
      DocumentFormat.MD -> {
        outputFile.writeText(
          """
          # Toolbox Sample Markdown Document
          
          Welcome to the **Document Viewer**! Every supported filetype is rendered onto a unified paginated canvas so you can zoom, pan, draw with freehand ink or highlighter, and place draggable text annotations.
          
          ## Supported File Categories
          
          - [x] **PDF Documents** (`.pdf`)
          - [x] **Text & Markup** (`.txt`, `.md`, `.rtf`, `.html`, `.log`)
          - [x] **Office Documents** (`.docx`, `.xlsx`, `.pptx`, `.odt`, `.ods`, `.odp`)
          
          ## Format Capabilities Table
          
          | Format | Category | Layout Engine | PDF Export |
          | --- | --- | --- | --- |
          | PDF | Document | Android PdfRenderer | Native |
          | DOCX / ODT | Office Word | Paginated Typography | Supported |
          | XLSX / ODS | Office Sheet | Structured Grid Sheet | Supported |
          | PPTX / ODP | Presentation | Landscape Slide Deck | Supported |
          
          > Tip: Switch to Draw or Text mode in the bottom bar to annotate any page and export a flattened PDF!
          """.trimIndent()
        )
        outputFile
      }
      DocumentFormat.DOCX -> generateSampleDocxFile(outputFile)
      DocumentFormat.XLSX -> generateSampleXlsxFile(outputFile)
      DocumentFormat.PPTX -> generateSamplePptxFile(outputFile)
      DocumentFormat.HTML -> {
        outputFile.writeText(
          """
          <!DOCTYPE html>
          <html>
          <head><title>Toolbox HTML Report</title></head>
          <body>
            <h1>Toolbox HTML Document Preview</h1>
            <p>This HTML document is parsed and typeset onto clean A4 pages for offline reading and markup.</p>
            <h2>Key Highlights</h2>
            <ul>
              <li>Full freehand Pen, Highlighter, and Eraser support</li>
              <li>Draggable Text callouts with customizable colors</li>
              <li>One-tap export to annotated PDF</li>
            </ul>
          </body>
          </html>
          """.trimIndent()
        )
        outputFile
      }
      DocumentFormat.RTF -> {
        outputFile.writeText(
          """{\rtf1\ansi\deff0 {\fonttbl {\f0 Helvetica;}}
          \f0\fs28 Toolbox Rich Text Document\par
          \fs22 This RTF document is rendered on the unified page canvas.\par
          \bullet Switch to Draw mode to highlight key points\par
          \bullet Switch to Text mode to stamp review notes\par
          }""".trimIndent()
        )
        outputFile
      }
      else -> {
        outputFile.writeText(
          """
          Toolbox Sample Document (${format.badgeLabel})
          =========================================
          
          1. Overview
          This document is rendered into paginated sheets by DocumentViewerEngine.
          
          2. Annotation Tools
          - View Mode: Smooth vertical scrolling, pinch-to-zoom, and horizontal pan.
          - Draw Mode: Freehand pen, highlighter, and eraser strokes.
          - Text Mode: Tap anywhere to place or drag text annotations.
          """.trimIndent()
        )
        outputFile
      }
    }
  }

  fun generateSampleDocxFile(outputFile: File): File {
    outputFile.parentFile?.mkdirs()
    val documentXml =
      """
      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
        <w:body>
          <w:p>
            <w:pPr><w:pStyle w:val="Heading1"/></w:pPr>
            <w:r><w:t>Toolbox Word Document (DOCX)</w:t></w:r>
          </w:p>
          <w:p>
            <w:r><w:t>This sample DOCX file is parsed offline and rendered onto paginated document sheets ready for reading, freehand highlighting, and text annotations.</w:t></w:r>
          </w:p>
          <w:p>
            <w:pPr><w:pStyle w:val="Heading2"/></w:pPr>
            <w:r><w:t>1. Executive Summary</w:t></w:r>
          </w:p>
          <w:p>
            <w:pPr><w:numPr><w:ilvl w:val="0"/><w:numId w:val="1"/></w:numPr></w:pPr>
            <w:r><w:t>Supports Word (DOCX/ODT), Excel (XLSX/ODS), and PowerPoint (PPTX/ODP)</w:t></w:r>
          </w:p>
          <w:p>
            <w:pPr><w:numPr><w:ilvl w:val="0"/><w:numId w:val="1"/></w:numPr></w:pPr>
            <w:r><w:t>Supports Markdown (MD), Rich Text (RTF), HTML, Plain Text (TXT), and Logs (LOG)</w:t></w:r>
          </w:p>
          <w:tbl>
            <w:tr>
              <w:tc><w:p><w:r><w:t>Module</w:t></w:r></w:p></w:tc>
              <w:tc><w:p><w:r><w:t>Format</w:t></w:r></w:p></w:tc>
              <w:tc><w:p><w:r><w:t>Status</w:t></w:r></w:p></w:tc>
            </w:tr>
            <w:tr>
              <w:tc><w:p><w:r><w:t>Document Parser</w:t></w:r></w:p></w:tc>
              <w:tc><w:p><w:r><w:t>OOXML &amp; ODF</w:t></w:r></w:p></w:tc>
              <w:tc><w:p><w:r><w:t>Active</w:t></w:r></w:p></w:tc>
            </w:tr>
            <w:tr>
              <w:tc><w:p><w:r><w:t>Page Annotator</w:t></w:r></w:p></w:tc>
              <w:tc><w:p><w:r><w:t>Ink + Callouts</w:t></w:r></w:p></w:tc>
              <w:tc><w:p><w:r><w:t>Ready</w:t></w:r></w:p></w:tc>
            </w:tr>
          </w:tbl>
        </w:body>
      </w:document>
      """.trimIndent()
    ZipOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { zos ->
      zos.putNextEntry(ZipEntry("word/document.xml"))
      zos.write(documentXml.toByteArray(Charsets.UTF_8))
      zos.closeEntry()
    }
    return outputFile
  }

  fun generateSampleXlsxFile(outputFile: File): File {
    outputFile.parentFile?.mkdirs()
    val workbookXml =
      """
      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
        <sheets>
          <sheet name="Project Metrics" sheetId="1" r:id="rId1"/>
        </sheets>
      </workbook>
      """.trimIndent()
    val sharedStringsXml =
      """
      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      <sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="8" uniqueCount="8">
        <si><t>Task</t></si>
        <si><t>Owner</t></si>
        <si><t>Status</t></si>
        <si><t>PDF Renderer</t></si>
        <si><t>Core Team</t></si>
        <si><t>Complete</t></si>
        <si><t>Office &amp; Markup Support</t></si>
        <si><t>Verified</t></si>
      </sst>
      """.trimIndent()
    val sheet1Xml =
      """
      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
        <sheetData>
          <row r="1">
            <c r="A1" t="s"><v>0</v></c>
            <c r="B1" t="s"><v>1</v></c>
            <c r="C1" t="s"><v>2</v></c>
          </row>
          <row r="2">
            <c r="A2" t="s"><v>3</v></c>
            <c r="B2" t="s"><v>4</v></c>
            <c r="C2" t="s"><v>5</v></c>
          </row>
          <row r="3">
            <c r="A3" t="s"><v>6</v></c>
            <c r="B3" t="s"><v>4</v></c>
            <c r="C3" t="s"><v>7</v></c>
          </row>
        </sheetData>
      </worksheet>
      """.trimIndent()

    ZipOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { zos ->
      zos.putNextEntry(ZipEntry("xl/workbook.xml"))
      zos.write(workbookXml.toByteArray(Charsets.UTF_8))
      zos.closeEntry()
      zos.putNextEntry(ZipEntry("xl/sharedStrings.xml"))
      zos.write(sharedStringsXml.toByteArray(Charsets.UTF_8))
      zos.closeEntry()
      zos.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
      zos.write(sheet1Xml.toByteArray(Charsets.UTF_8))
      zos.closeEntry()
    }
    return outputFile
  }

  fun generateSamplePptxFile(outputFile: File): File {
    outputFile.parentFile?.mkdirs()
    val slide1Xml =
      """
      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      <p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
             xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
        <p:cSld><p:spTree>
          <p:sp><p:txBody>
            <a:p><a:r><a:t>Toolbox Document Viewer Deck</a:t></a:r></a:p>
            <a:p><a:r><a:t>Unified Paginated Canvas for PDF, Office &amp; Markup</a:t></a:r></a:p>
            <a:p><a:r><a:t>Annotate slides with freehand ink and text callouts</a:t></a:r></a:p>
          </p:txBody></p:sp>
        </p:spTree></p:cSld>
      </p:sld>
      """.trimIndent()
    val slide2Xml =
      """
      <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
      <p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
             xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
        <p:cSld><p:spTree>
          <p:sp><p:txBody>
            <a:p><a:r><a:t>Export &amp; Share Workflow</a:t></a:r></a:p>
            <a:p><a:r><a:t>1. Open any PPTX, DOCX, XLSX, MD, RTF, HTML, TXT, or PDF</a:t></a:r></a:p>
            <a:p><a:r><a:t>2. Highlight key metrics or add review text stamps</a:t></a:r></a:p>
            <a:p><a:r><a:t>3. Save or share as a flattened multi-page PDF document</a:t></a:r></a:p>
          </p:txBody></p:sp>
        </p:spTree></p:cSld>
      </p:sld>
      """.trimIndent()

    ZipOutputStream(BufferedOutputStream(FileOutputStream(outputFile))).use { zos ->
      zos.putNextEntry(ZipEntry("ppt/slides/slide1.xml"))
      zos.write(slide1Xml.toByteArray(Charsets.UTF_8))
      zos.closeEntry()
      zos.putNextEntry(ZipEntry("ppt/slides/slide2.xml"))
      zos.write(slide2Xml.toByteArray(Charsets.UTF_8))
      zos.closeEntry()
    }
    return outputFile
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
    canvas.drawText("Toolbox Sample Document", 64f, 88f, titlePaint)

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
        "This sample multi-page document is generated locally so you can preview,",
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
        "• PDF, Office (DOCX/XLSX/PPTX/ODF) & Markup —  Unified Canvas",
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
    canvas.drawText("Toolbox Document Viewer • Page 1 / 3", 48f, height - 36f, footerPaint)
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
    canvas.drawText("Toolbox Document Viewer • Page 2 / 3", 48f, height - 36f, footerPaint)
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
    canvas.drawText("Toolbox Document Viewer • Page 3 / 3", 48f, height - 36f, footerPaint)
  }
}

typealias PdfViewerEngine = DocumentViewerEngine
