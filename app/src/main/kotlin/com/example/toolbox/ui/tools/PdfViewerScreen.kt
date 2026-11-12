package com.example.toolbox.ui.tools

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomInMap
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val PDF_PEN_COLORS =
  listOf(
    Color(0xFFE53935), // Red
    Color(0xFF1E88E5), // Blue
    Color(0xFF43A047), // Green
    Color(0xFFFDD835), // Yellow
    Color(0xFFFB8C00), // Orange
    Color(0xFF8E24AA), // Purple
    Color(0xFF1A1A1A), // Black
    Color(0xFFFFFFFF), // White
  )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
  modifier: Modifier = Modifier,
  initialFile: File? = null,
  initialDirectory: File? = null,
  onNavigateBack: (() -> Unit)? = null,
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val scope = rememberCoroutineScope()

  var currentFile by remember(initialFile) { mutableStateOf(initialFile) }
  var pageCount by remember { mutableIntStateOf(0) }
  var currentPageIndex by remember { mutableIntStateOf(0) }
  var basePageBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var isLoadingPage by remember { mutableStateOf(false) }
  var isSavingPdf by remember { mutableStateOf(false) }

  var showFilePicker by remember { mutableStateOf(false) }
  var showSaveDialog by remember { mutableStateOf(false) }

  // Edit mode & history
  var editMode by remember { mutableStateOf(PdfEditMode.VIEW) }
  val editHistory = remember { PdfEditHistory() }
  var pageEditsMap by remember { mutableStateOf<Map<Int, PdfPageEdits>>(emptyMap()) }

  // View zoom & pan
  var zoomScale by remember { mutableFloatStateOf(1f) }
  var panOffset by remember { mutableStateOf(Offset.Zero) }

  // Draw controls state
  var brushType by remember { mutableStateOf(PdfBrushType.PEN) }
  var brushColor by remember { mutableStateOf(PDF_PEN_COLORS[0]) }
  var brushWidth by remember { mutableFloatStateOf(8f) }
  val activeStrokePoints = remember { mutableStateListOf<Offset>() }

  // Text annotation dialog state
  var showTextDialog by remember { mutableStateOf(false) }
  var editingTextItem by remember { mutableStateOf<PdfTextItem?>(null) }
  var pendingTextPosition by remember { mutableStateOf(Offset(0.5f, 0.5f)) }

  fun syncHistoryState() {
    pageEditsMap = editHistory.currentEdits
  }

  // Load PDF document when currentFile changes
  LaunchedEffect(currentFile) {
    val file = currentFile
    if (file == null || !file.exists()) {
      pageCount = 0
      currentPageIndex = 0
      basePageBitmap = null
      editHistory.reset()
      syncHistoryState()
      return@LaunchedEffect
    }
    isLoadingPage = true
    val count = withContext(Dispatchers.IO) { PdfViewerEngine.getPageCount(file) }
    pageCount = count
    currentPageIndex = 0
    zoomScale = 1f
    panOffset = Offset.Zero
    editHistory.reset()
    syncHistoryState()
    if (count > 0) {
      basePageBitmap =
        withContext(Dispatchers.IO) { PdfViewerEngine.renderPageToBitmap(file, 0) }
    } else {
      basePageBitmap = null
      Toast.makeText(context, "Unable to read PDF document", Toast.LENGTH_SHORT).show()
    }
    isLoadingPage = false
  }

  // Render active page when currentPageIndex changes
  LaunchedEffect(currentFile, currentPageIndex) {
    val file = currentFile ?: return@LaunchedEffect
    if (pageCount <= 0 || currentPageIndex !in 0 until pageCount) return@LaunchedEffect
    isLoadingPage = true
    activeStrokePoints.clear()
    zoomScale = 1f
    panOffset = Offset.Zero
    basePageBitmap =
      withContext(Dispatchers.IO) {
        PdfViewerEngine.renderPageToBitmap(file, currentPageIndex)
      }
    isLoadingPage = false
  }

  if (onNavigateBack != null) {
    BackHandler { onNavigateBack() }
  }

  val currentPageEdits = pageEditsMap[currentPageIndex] ?: PdfPageEdits()

  if (showFilePicker) {
    val defaultBrowseDir = remember(initialDirectory, currentFile) {
      initialDirectory
        ?: currentFile?.parentFile
        ?: Environment.getExternalStorageDirectory()?.takeIf { it.exists() && it.canRead() }
        ?: context.filesDir
    }
    StorageFilePickerDialog(
      initialDirectory = defaultBrowseDir,
      title = "Select PDF File",
      fileFilter = { file -> FileManagerEngine.isPdfFile(file) },
      onDismiss = { showFilePicker = false },
      onLaunchSystemPicker = { showFilePicker = false },
      onFileSelected = { selected: File ->
        showFilePicker = false
        currentFile = selected
      },
    )
  }

  if (showTextDialog) {
    PdfTextEditDialog(
      initialItem = editingTextItem,
      defaultColor = brushColor,
      onDismiss = {
        showTextDialog = false
        editingTextItem = null
      },
      onConfirm = { text, color, fontSizeSp, hasBg ->
        val existing = editingTextItem
        if (existing != null) {
          editHistory.updateText(
            currentPageIndex,
            existing.copy(
              text = text,
              color = color,
              fontSizeSp = fontSizeSp,
              hasBackground = hasBg,
            ),
          )
        } else {
          editHistory.addText(
            currentPageIndex,
            PdfTextItem(
              text = text,
              xRatio = pendingTextPosition.x.coerceIn(0.05f, 0.95f),
              yRatio = pendingTextPosition.y.coerceIn(0.05f, 0.95f),
              color = color,
              fontSizeSp = fontSizeSp,
              hasBackground = hasBg,
            ),
          )
        }
        syncHistoryState()
        showTextDialog = false
        editingTextItem = null
      },
    )
  }

  if (showSaveDialog && currentFile != null) {
    val srcFile = currentFile!!
    PdfSaveDialog(
      sourceFile = srcFile,
      hasEdits = editHistory.hasAnyEdits,
      isSaving = isSavingPdf,
      onDismiss = { if (!isSavingPdf) showSaveDialog = false },
      onSave = { outputFileName, overwriteOriginal ->
        scope.launch {
          isSavingPdf = true
          val targetFile =
            if (overwriteOriginal && srcFile.parentFile?.canWrite() == true) {
              srcFile
            } else {
              val baseDir =
                srcFile.parentFile?.takeIf { it.exists() && it.canWrite() }
                  ?: Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    ?.takeIf { it.exists() || it.mkdirs() }
                  ?: context.filesDir
              val cleanName =
                if (outputFileName.lowercase().endsWith(".pdf")) {
                  outputFileName
                } else {
                  "$outputFileName.pdf"
                }
              File(baseDir, cleanName)
            }

          val result =
            withContext(Dispatchers.IO) {
              PdfViewerEngine.saveEditedPdf(
                sourceFile = srcFile,
                outputFile = targetFile,
                pageEdits = editHistory.currentEdits,
              )
            }
          isSavingPdf = false
          showSaveDialog = false

          result
            .onSuccess { savedFile ->
              Toast.makeText(
                  context,
                  "Saved PDF to ${savedFile.name}",
                  Toast.LENGTH_SHORT,
                )
                .show()
              val savedPage = currentPageIndex
              currentFile = savedFile
              editHistory.reset()
              syncHistoryState()
              basePageBitmap =
                withContext(Dispatchers.IO) {
                  PdfViewerEngine.renderPageToBitmap(savedFile, savedPage)
                }
            }
            .onFailure { err ->
              Toast.makeText(
                  context,
                  "Failed to save PDF: ${err.message ?: "Unknown error"}",
                  Toast.LENGTH_SHORT,
                )
                .show()
            }
        }
      },
    )
  }

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .testTag("pdf_viewer_screen")
  ) {
    PdfViewerTopBar(
      fileName = currentFile?.name,
      hasDocument = currentFile != null && pageCount > 0,
      canUndo = editHistory.canUndo,
      canRedo = editHistory.canRedo,
      onNavigateBack = onNavigateBack,
      onUndo = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (editHistory.undo()) syncHistoryState()
      },
      onRedo = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (editHistory.redo()) syncHistoryState()
      },
      onShare = {
        val srcFile = currentFile ?: return@PdfViewerTopBar
        scope.launch {
          val fileToShare =
            if (editHistory.hasAnyEdits) {
              val exported = File(context.cacheDir, "annotated_${srcFile.name}")
              withContext(Dispatchers.IO) {
                PdfViewerEngine.saveEditedPdf(srcFile, exported, editHistory.currentEdits)
                  .getOrDefault(srcFile)
              }
            } else {
              srcFile
            }
          try {
            val uri: Uri =
              FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                fileToShare,
              )
            val intent =
              Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
              }
            context.startActivity(Intent.createChooser(intent, "Share PDF"))
          } catch (e: Exception) {
            Toast.makeText(context, "Unable to share PDF", Toast.LENGTH_SHORT).show()
          }
        }
      },
      onClose = {
        currentFile = null
        pageCount = 0
        basePageBitmap = null
        editHistory.reset()
        syncHistoryState()
      },
      onSave = { showSaveDialog = true },
    )

    if (currentFile == null || (pageCount == 0 && !isLoadingPage)) {
      OpenFileInitialView(
        onOpenFile = { showFilePicker = true },
        onUseSample = {
          scope.launch {
            isLoadingPage = true
            val sampleFile =
              withContext(Dispatchers.IO) {
                FileManagerEngine.getOrCreateSamplePdfFile(context)
              }
            currentFile = sampleFile
          }
        },
        sampleButtonText = "or try sample PDF",
        sampleButtonIcon = Icons.Default.PictureAsPdf,
        sampleButtonTestTag = "open_sample_pdf_button",
      )
    } else {
      // Page Navigation Strip
      PdfPageNavigationBar(
        currentPageIndex = currentPageIndex,
        pageCount = pageCount,
        pageEditsMap = pageEditsMap,
        onSelectPage = { newIndex ->
          if (newIndex in 0 until pageCount && newIndex != currentPageIndex) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            currentPageIndex = newIndex
          }
        },
      )

      // Main Interactive PDF Page Viewport
      Box(
        modifier =
          Modifier.weight(1f)
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(
              1.dp,
              MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
              RoundedCornerShape(16.dp),
            )
            .clipToBounds(),
        contentAlignment = Alignment.Center,
      ) {
        val pageBitmap = basePageBitmap
        if (isLoadingPage || pageBitmap == null) {
          CircularProgressIndicator()
        } else {
          PdfPageInteractiveCanvas(
            pageBitmap = pageBitmap,
            editMode = editMode,
            pageEdits = currentPageEdits,
            activeStrokePoints = activeStrokePoints,
            brushType = brushType,
            brushColor = brushColor,
            brushWidth = brushWidth,
            zoomScale = zoomScale,
            panOffset = panOffset,
            onZoomPanChange = { newZoom, newPan ->
              zoomScale = newZoom
              panOffset = newPan
            },
            onCommitStroke = { points ->
              if (points.isNotEmpty()) {
                editHistory.addStroke(
                  currentPageIndex,
                  PdfDrawStroke(
                    points = points,
                    color = brushColor,
                    strokeWidth =
                      if (brushType == PdfBrushType.HIGHLIGHTER) {
                        brushWidth * 2.2f
                      } else if (brushType == PdfBrushType.ERASER) {
                        brushWidth * 2.5f
                      } else {
                        brushWidth
                      },
                    alpha = brushType.defaultAlpha,
                    isEraser = brushType.isEraser,
                  ),
                )
                syncHistoryState()
              }
            },
            onTapAddTextAt = { normalizedOffset ->
              pendingTextPosition = normalizedOffset
              editingTextItem = null
              showTextDialog = true
            },
            onUpdateTextPosition = { itemId, newXRatio, newYRatio ->
              val currentItem =
                currentPageEdits.texts.find { it.id == itemId } ?: return@PdfPageInteractiveCanvas
              editHistory.updateText(
                currentPageIndex,
                currentItem.copy(
                  xRatio = newXRatio.coerceIn(0.04f, 0.96f),
                  yRatio = newYRatio.coerceIn(0.04f, 0.96f),
                ),
              )
              syncHistoryState()
            },
            onTapEditText = { item ->
              editingTextItem = item
              showTextDialog = true
            },
          )

          // Reset zoom pill when zoomed in
          if (zoomScale > 1.02f) {
            Surface(
              onClick = {
                zoomScale = 1f
                panOffset = Offset.Zero
              },
              shape = RoundedCornerShape(20.dp),
              color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
              tonalElevation = 4.dp,
              modifier =
                Modifier.align(Alignment.TopEnd)
                  .padding(12.dp)
                  .testTag("pdf_reset_zoom_button"),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Icon(
                  Icons.Default.ZoomInMap,
                  contentDescription = "Reset zoom",
                  modifier = Modifier.size(16.dp),
                )
                Text(
                  text = "${(zoomScale * 100).roundToInt()}% • Reset",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold,
                )
              }
            }
          }
        }
      }

      // Bottom Mode-Specific Controls + Mode Segmented Row
      Surface(
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
          when (editMode) {
            PdfEditMode.VIEW -> {
              PdfViewModeControls(
                zoomScale = zoomScale,
                currentPageEditsCount = currentPageEdits.editCount,
                onZoomChange = { targetZoom ->
                  zoomScale = targetZoom.coerceIn(1f, 4f)
                  if (zoomScale <= 1.01f) panOffset = Offset.Zero
                },
                onSwitchToDraw = { editMode = PdfEditMode.DRAW },
                onSwitchToText = { editMode = PdfEditMode.TEXT },
              )
            }
            PdfEditMode.DRAW -> {
              PdfDrawModeControls(
                brushType = brushType,
                onBrushTypeChange = { brushType = it },
                selectedColor = brushColor,
                onColorSelect = { brushColor = it },
                strokeWidth = brushWidth,
                onStrokeWidthChange = { brushWidth = it },
                hasPageStrokes = currentPageEdits.strokes.isNotEmpty(),
                onUndo = {
                  if (editHistory.undo()) syncHistoryState()
                },
                onClearPage = {
                  editHistory.clearPage(currentPageIndex)
                  syncHistoryState()
                },
              )
            }
            PdfEditMode.TEXT -> {
              PdfTextModeControls(
                texts = currentPageEdits.texts,
                onAddTextClick = {
                  pendingTextPosition = Offset(0.5f, 0.5f)
                  editingTextItem = null
                  showTextDialog = true
                },
                onEditTextClick = { item ->
                  editingTextItem = item
                  showTextDialog = true
                },
                onDeleteTextClick = { item ->
                  editHistory.removeText(currentPageIndex, item.id)
                  syncHistoryState()
                },
              )
            }
          }

          Spacer(Modifier.height(8.dp))

          SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            PdfEditMode.entries.forEachIndexed { index, mode ->
              SegmentedButton(
                selected = editMode == mode,
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  if (mode != PdfEditMode.VIEW) {
                    zoomScale = 1f
                    panOffset = Offset.Zero
                  }
                  editMode = mode
                },
                shape =
                  SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = PdfEditMode.entries.size,
                  ),
                icon = {
                  val modeIcon =
                    when (mode) {
                      PdfEditMode.VIEW -> Icons.Default.Visibility
                      PdfEditMode.DRAW -> Icons.Default.Brush
                      PdfEditMode.TEXT -> Icons.Default.TextFields
                    }
                  Icon(modeIcon, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                modifier = Modifier.testTag("pdf_mode_${mode.name.lowercase()}"),
              ) {
                Text(mode.label, maxLines = 1)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PdfViewerTopBar(
  fileName: String?,
  hasDocument: Boolean,
  canUndo: Boolean,
  canRedo: Boolean,
  onNavigateBack: (() -> Unit)?,
  onUndo: () -> Unit,
  onRedo: () -> Unit,
  onShare: () -> Unit,
  onClose: () -> Unit,
  onSave: () -> Unit,
) {
  Surface(
    tonalElevation = 1.dp,
    modifier = Modifier.fillMaxWidth().heightIn(min = 68.dp),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f, fill = false),
      ) {
        if (onNavigateBack != null) {
          FilledTonalIconButton(
            onClick = onNavigateBack,
            modifier =
              Modifier.size(36.dp)
                .testTag("action_navigate_back")
                .testTag("pdf_back_button"),
          ) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              modifier = Modifier.size(20.dp),
            )
          }
        } else {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(36.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                Icons.Default.PictureAsPdf,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp),
              )
            }
          }
        }
        Column {
          Text(
            "PDF Viewer",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = fileName ?: "View, draw & add text to PDFs",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      if (hasDocument) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(2.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(
            onClick = onUndo,
            enabled = canUndo,
            modifier = Modifier.size(36.dp).testTag("pdf_undo_button"),
          ) {
            Icon(
              Icons.AutoMirrored.Filled.Undo,
              contentDescription = "Undo",
              modifier = Modifier.size(20.dp),
            )
          }
          IconButton(
            onClick = onRedo,
            enabled = canRedo,
            modifier = Modifier.size(36.dp).testTag("pdf_redo_button"),
          ) {
            Icon(
              Icons.AutoMirrored.Filled.Redo,
              contentDescription = "Redo",
              modifier = Modifier.size(20.dp),
            )
          }
          IconButton(
            onClick = onShare,
            modifier = Modifier.size(36.dp).testTag("pdf_share_button"),
          ) {
            Icon(
              Icons.Default.Share,
              contentDescription = "Share PDF",
              modifier = Modifier.size(20.dp),
            )
          }
          IconButton(
            onClick = onClose,
            modifier = Modifier.size(36.dp).testTag("pdf_close_button"),
          ) {
            Icon(
              Icons.Default.Close,
              contentDescription = "Close PDF",
              modifier = Modifier.size(20.dp),
            )
          }
          Spacer(Modifier.width(4.dp))
          Button(
            onClick = onSave,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.testTag("pdf_save_button"),
          ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Save", fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }
  }
}

@Composable
private fun PdfPageNavigationBar(
  currentPageIndex: Int,
  pageCount: Int,
  pageEditsMap: Map<Int, PdfPageEdits>,
  onSelectPage: (Int) -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceContainer,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      IconButton(
        onClick = { onSelectPage(currentPageIndex - 1) },
        enabled = currentPageIndex > 0,
        modifier = Modifier.size(32.dp).testTag("pdf_prev_page_button"),
      ) {
        Icon(
          Icons.AutoMirrored.Filled.KeyboardArrowLeft,
          contentDescription = "Previous Page",
        )
      }

      Row(
        modifier =
          Modifier.weight(1f)
            .padding(horizontal = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        for (idx in 0 until pageCount) {
          val isSelected = idx == currentPageIndex
          val hasEdits = pageEditsMap[idx]?.isEmpty == false
          Surface(
            onClick = { onSelectPage(idx) },
            shape = RoundedCornerShape(8.dp),
            color =
              if (isSelected) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
              },
            border =
              if (!isSelected && hasEdits) {
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
              } else {
                null
              },
            modifier = Modifier.testTag("pdf_page_chip_${idx + 1}"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Text(
                text = "${idx + 1}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color =
                  if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                  } else {
                    MaterialTheme.colorScheme.onSurface
                  },
              )
              if (hasEdits) {
                Box(
                  modifier =
                    Modifier.size(6.dp)
                      .clip(CircleShape)
                      .background(
                        if (isSelected) {
                          MaterialTheme.colorScheme.onPrimary
                        } else {
                          MaterialTheme.colorScheme.primary
                        }
                      )
                )
              }
            }
          }
        }
      }

      Text(
        text = "Page ${currentPageIndex + 1} / $pageCount",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 6.dp).testTag("pdf_page_indicator"),
      )

      IconButton(
        onClick = { onSelectPage(currentPageIndex + 1) },
        enabled = currentPageIndex < pageCount - 1,
        modifier = Modifier.size(32.dp).testTag("pdf_next_page_button"),
      ) {
        Icon(
          Icons.AutoMirrored.Filled.KeyboardArrowRight,
          contentDescription = "Next Page",
        )
      }
    }
  }
}

@Composable
private fun PdfPageInteractiveCanvas(
  pageBitmap: Bitmap,
  editMode: PdfEditMode,
  pageEdits: PdfPageEdits,
  activeStrokePoints: MutableList<Offset>,
  brushType: PdfBrushType,
  brushColor: Color,
  brushWidth: Float,
  zoomScale: Float,
  panOffset: Offset,
  onZoomPanChange: (Float, Offset) -> Unit,
  onCommitStroke: (List<Offset>) -> Unit,
  onTapAddTextAt: (Offset) -> Unit,
  onUpdateTextPosition: (Long, Float, Float) -> Unit,
  onTapEditText: (PdfTextItem) -> Unit,
) {
  val density = LocalDensity.current
  val currentBrushType by rememberUpdatedState(brushType)
  val currentBrushColor by rememberUpdatedState(brushColor)
  val currentBrushWidth by rememberUpdatedState(brushWidth)
  val currentZoom by rememberUpdatedState(zoomScale)
  val currentPan by rememberUpdatedState(panOffset)
  val currentOnCommitStroke by rememberUpdatedState(onCommitStroke)
  val currentOnTapAddTextAt by rememberUpdatedState(onTapAddTextAt)

  BoxWithConstraints(
    modifier = Modifier.fillMaxSize().padding(8.dp),
    contentAlignment = Alignment.Center,
  ) {
    val containerW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
    val containerH = constraints.maxHeight.toFloat().coerceAtLeast(1f)
    val bmpRatio = pageBitmap.width.toFloat() / pageBitmap.height.toFloat().coerceAtLeast(1f)
    val containerRatio = containerW / containerH

    val pageDisplayW: Float
    val pageDisplayH: Float
    if (bmpRatio > containerRatio) {
      pageDisplayW = containerW
      pageDisplayH = containerW / bmpRatio
    } else {
      pageDisplayH = containerH
      pageDisplayW = containerH * bmpRatio
    }

    val pageWidthDp = with(density) { pageDisplayW.toDp() }
    val pageHeightDp = with(density) { pageDisplayH.toDp() }
    val scaleFactor = min(pageDisplayW, pageDisplayH) / 1000f

    Box(
      modifier =
        Modifier.size(pageWidthDp, pageHeightDp)
          .shadow(4.dp, RoundedCornerShape(6.dp))
          .clip(RoundedCornerShape(6.dp))
          .background(Color.White)
          .graphicsLayer {
            scaleX = zoomScale
            scaleY = zoomScale
            translationX = panOffset.x
            translationY = panOffset.y
          }
          .pointerInput(editMode, pageDisplayW, pageDisplayH) {
            when (editMode) {
              PdfEditMode.VIEW -> {
                detectTransformGestures { _, pan, zoom, _ ->
                  val newZoom = (currentZoom * zoom).coerceIn(1f, 4f)
                  val maxPanX = (pageDisplayW * (newZoom - 1f)) / 2f
                  val maxPanY = (pageDisplayH * (newZoom - 1f)) / 2f
                  val nextPan =
                    if (newZoom <= 1.01f) {
                      Offset.Zero
                    } else {
                      Offset(
                        x = (currentPan.x + pan.x).coerceIn(-maxPanX, maxPanX),
                        y = (currentPan.y + pan.y).coerceIn(-maxPanY, maxPanY),
                      )
                    }
                  onZoomPanChange(newZoom, nextPan)
                }
              }
              PdfEditMode.DRAW -> {
                detectDragGestures(
                  onDragStart = { startOffset ->
                    activeStrokePoints.clear()
                    activeStrokePoints.add(
                      Offset(
                        (startOffset.x / pageDisplayW).coerceIn(0f, 1f),
                        (startOffset.y / pageDisplayH).coerceIn(0f, 1f),
                      )
                    )
                  },
                  onDrag = { change, _ ->
                    change.consume()
                    activeStrokePoints.add(
                      Offset(
                        (change.position.x / pageDisplayW).coerceIn(0f, 1f),
                        (change.position.y / pageDisplayH).coerceIn(0f, 1f),
                      )
                    )
                  },
                  onDragEnd = {
                    val pointsCopy = activeStrokePoints.toList()
                    activeStrokePoints.clear()
                    currentOnCommitStroke(pointsCopy)
                  },
                  onDragCancel = { activeStrokePoints.clear() },
                )
              }
              PdfEditMode.TEXT -> {
                detectTapGestures { tapOffset ->
                  val normalized =
                    Offset(
                      (tapOffset.x / pageDisplayW).coerceIn(0.05f, 0.95f),
                      (tapOffset.y / pageDisplayH).coerceIn(0.05f, 0.95f),
                    )
                  currentOnTapAddTextAt(normalized)
                }
              }
            }
          }
          .testTag("pdf_page_canvas"),
    ) {
      // Base PDF Page Image
      Image(
        bitmap = pageBitmap.asImageBitmap(),
        contentDescription = "PDF Page",
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize(),
      )

      // Freehand Drawing + Eraser Overlay Layer (Offscreen compositing so BlendMode.Clear erases only strokes)
      Canvas(
        modifier =
          Modifier.fillMaxSize().graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
          }
      ) {
        val allStrokes =
          if (activeStrokePoints.isNotEmpty()) {
            val activeWidth =
              when (currentBrushType) {
                PdfBrushType.HIGHLIGHTER -> currentBrushWidth * 2.2f
                PdfBrushType.ERASER -> currentBrushWidth * 2.5f
                PdfBrushType.PEN -> currentBrushWidth
              }
            pageEdits.strokes +
              PdfDrawStroke(
                points = activeStrokePoints.toList(),
                color = currentBrushColor,
                strokeWidth = activeWidth,
                alpha = currentBrushType.defaultAlpha,
                isEraser = currentBrushType.isEraser,
              )
          } else {
            pageEdits.strokes
          }

        for (stroke in allStrokes) {
          if (stroke.points.isEmpty()) continue
          val effectiveWidth = (stroke.strokeWidth * scaleFactor).coerceAtLeast(2f)
          val drawColor =
            if (stroke.isEraser) {
              Color.Transparent
            } else {
              stroke.color.copy(alpha = stroke.alpha.coerceIn(0.05f, 1f))
            }
          val blendMode = if (stroke.isEraser) BlendMode.Clear else BlendMode.SrcOver

          if (stroke.points.size == 1) {
            val pt = stroke.points.first()
            drawCircle(
              color = drawColor,
              radius = effectiveWidth / 2f,
              center = Offset(pt.x * size.width, pt.y * size.height),
              blendMode = blendMode,
            )
          } else {
            val path =
              Path().apply {
                val first = stroke.points.first()
                moveTo(first.x * size.width, first.y * size.height)
                for (i in 1 until stroke.points.size) {
                  val pt = stroke.points[i]
                  lineTo(pt.x * size.width, pt.y * size.height)
                }
              }
            drawPath(
              path = path,
              color = drawColor,
              style =
                Stroke(
                  width = effectiveWidth,
                  cap = StrokeCap.Round,
                  join = StrokeJoin.Round,
                ),
              blendMode = blendMode,
            )
          }
        }
      }

      // Text Annotations Overlay
      pageEdits.texts.forEach { item ->
        var dragRatioX by remember(item.id, item.xRatio) { mutableFloatStateOf(item.xRatio) }
        var dragRatioY by remember(item.id, item.yRatio) { mutableFloatStateOf(item.yRatio) }

        val luminance =
          (0.299f * item.color.red) + (0.587f * item.color.green) + (0.114f * item.color.blue)
        val badgeBg =
          if (item.hasBackground) {
            if (luminance > 0.6f) Color(0xD818181C) else Color(0xE6FFFCEB)
          } else {
            Color.Transparent
          }

        Box(
          modifier =
            Modifier.offset {
                IntOffset(
                  x = ((dragRatioX * pageDisplayW) - 48.dp.toPx()).roundToInt(),
                  y = ((dragRatioY * pageDisplayH) - 18.dp.toPx()).roundToInt(),
                )
              }
              .clip(RoundedCornerShape(6.dp))
              .background(badgeBg)
              .then(
                if (editMode == PdfEditMode.TEXT) {
                  Modifier.border(
                      1.dp,
                      MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                      RoundedCornerShape(6.dp),
                    )
                    .clickable { onTapEditText(item) }
                    .pointerInput(item.id, pageDisplayW, pageDisplayH) {
                      detectDragGestures(
                        onDrag = { change, dragAmount ->
                          change.consume()
                          dragRatioX =
                            (dragRatioX + dragAmount.x / pageDisplayW).coerceIn(0.04f, 0.96f)
                          dragRatioY =
                            (dragRatioY + dragAmount.y / pageDisplayH).coerceIn(0.04f, 0.96f)
                        },
                        onDragEnd = {
                          onUpdateTextPosition(item.id, dragRatioX, dragRatioY)
                        },
                      )
                    }
                } else {
                  Modifier
                }
              )
              .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
          Text(
            text = item.text,
            color = item.color,
            fontSize = (item.fontSizeSp * 0.85f).coerceIn(10f, 42f).sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
          )
        }
      }
    }
  }
}

@Composable
private fun PdfViewModeControls(
  zoomScale: Float,
  currentPageEditsCount: Int,
  onZoomChange: (Float) -> Unit,
  onSwitchToDraw: () -> Unit,
  onSwitchToText: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      FilterChip(
        selected = zoomScale > 1.05f,
        onClick = {
          val nextZoom =
            when {
              zoomScale < 1.4f -> 1.5f
              zoomScale < 2.2f -> 2.5f
              else -> 1f
            }
          onZoomChange(nextZoom)
        },
        label = { Text("Zoom ${(zoomScale * 100).roundToInt()}%") },
        modifier = Modifier.testTag("pdf_zoom_chip"),
      )
      if (currentPageEditsCount > 0) {
        Text(
          text = "$currentPageEditsCount edit${if (currentPageEditsCount == 1) "" else "s"} on page",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      FilledTonalButton(
        onClick = onSwitchToDraw,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
      ) {
        Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text("Draw", style = MaterialTheme.typography.labelMedium)
      }
      FilledTonalButton(
        onClick = onSwitchToText,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
      ) {
        Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text("Add Text", style = MaterialTheme.typography.labelMedium)
      }
    }
  }
}

@Composable
private fun PdfDrawModeControls(
  brushType: PdfBrushType,
  onBrushTypeChange: (PdfBrushType) -> Unit,
  selectedColor: Color,
  onColorSelect: (Color) -> Unit,
  strokeWidth: Float,
  onStrokeWidthChange: (Float) -> Unit,
  hasPageStrokes: Boolean,
  onUndo: () -> Unit,
  onClearPage: () -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    // Brush type + width + clear row
    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      PdfBrushType.entries.forEach { type ->
        FilterChip(
          selected = brushType == type,
          onClick = { onBrushTypeChange(type) },
          label = { Text(type.label, style = MaterialTheme.typography.labelSmall) },
          modifier = Modifier.testTag("pdf_brush_${type.name.lowercase()}"),
        )
      }

      Spacer(Modifier.width(4.dp))

      val widths = listOf("Thin" to 4f, "Med" to 8f, "Thick" to 16f)
      widths.forEach { (label, w) ->
        FilterChip(
          selected = strokeWidth == w,
          onClick = { onStrokeWidthChange(w) },
          label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        )
      }

      if (hasPageStrokes) {
        OutlinedButton(
          onClick = onUndo,
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        ) {
          Icon(
            Icons.AutoMirrored.Filled.Undo,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
          )
          Spacer(Modifier.width(4.dp))
          Text("Undo", style = MaterialTheme.typography.labelSmall)
        }
        OutlinedButton(
          onClick = onClearPage,
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier.testTag("pdf_clear_page_button"),
        ) {
          Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(Modifier.width(4.dp))
          Text("Clear", style = MaterialTheme.typography.labelSmall)
        }
      }
    }

    // Color picker circles (when not using eraser)
    if (!brushType.isEraser) {
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        PDF_PEN_COLORS.forEachIndexed { idx, color ->
          val isSelected = selectedColor == color
          Box(
            modifier =
              Modifier.size(if (isSelected) 32.dp else 26.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                  width = if (isSelected) 2.5.dp else 1.dp,
                  color =
                    if (isSelected) {
                      MaterialTheme.colorScheme.primary
                    } else {
                      MaterialTheme.colorScheme.outlineVariant
                    },
                  shape = CircleShape,
                )
                .clickable { onColorSelect(color) }
                .testTag("pdf_color_$idx"),
            contentAlignment = Alignment.Center,
          ) {
            if (isSelected) {
              Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = if (color == Color.White || color == Color(0xFFFDD835)) Color.Black else Color.White,
                modifier = Modifier.size(14.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PdfTextModeControls(
  texts: List<PdfTextItem>,
  onAddTextClick: () -> Unit,
  onEditTextClick: (PdfTextItem) -> Unit,
  onDeleteTextClick: (PdfTextItem) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text =
          if (texts.isEmpty()) {
            "Tap anywhere on the page or press Add Text"
          } else {
            "Drag text on page to move • Tap text to edit"
          },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.weight(1f),
      )
      Spacer(Modifier.width(8.dp))
      Button(
        onClick = onAddTextClick,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("pdf_add_text_button"),
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text("Add Text", style = MaterialTheme.typography.labelMedium)
      }
    }

    if (texts.isNotEmpty()) {
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        texts.forEach { item ->
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
          ) {
            Row(
              modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Box(
                modifier =
                  Modifier.size(10.dp).clip(CircleShape).background(item.color)
              )
              Text(
                text = item.text,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { onEditTextClick(item) }.padding(horizontal = 4.dp),
              )
              IconButton(
                onClick = { onEditTextClick(item) },
                modifier = Modifier.size(24.dp),
              ) {
                Icon(
                  Icons.Default.Edit,
                  contentDescription = "Edit text",
                  modifier = Modifier.size(14.dp),
                )
              }
              IconButton(
                onClick = { onDeleteTextClick(item) },
                modifier = Modifier.size(24.dp).testTag("pdf_delete_text_button"),
              ) {
                Icon(
                  Icons.Default.Delete,
                  contentDescription = "Delete text",
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(14.dp),
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PdfTextEditDialog(
  initialItem: PdfTextItem?,
  defaultColor: Color,
  onDismiss: () -> Unit,
  onConfirm: (text: String, color: Color, fontSizeSp: Float, hasBackground: Boolean) -> Unit,
) {
  var textValue by remember(initialItem) { mutableStateOf(initialItem?.text ?: "") }
  var selectedColor by remember(initialItem) { mutableStateOf(initialItem?.color ?: defaultColor) }
  var fontSizeSp by remember(initialItem) { mutableFloatStateOf(initialItem?.fontSizeSp ?: 20f) }
  var hasBackground by remember(initialItem) {
    mutableStateOf(initialItem?.hasBackground ?: false)
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
      tonalElevation = 6.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                Icons.Default.TextFields,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp),
              )
            }
          }
          Column {
            Text(
              text = if (initialItem == null) "Add Text Annotation" else "Edit Text Annotation",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "Customize text, size, and color",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        OutlinedTextField(
          value = textValue,
          onValueChange = { textValue = it },
          label = { Text("Annotation text") },
          placeholder = { Text("Enter note or label...") },
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().testTag("pdf_text_input"),
          maxLines = 4,
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              "Font Size",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              "${fontSizeSp.roundToInt()} pt",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
            )
          }
          SlimDotSlider(
            value = fontSizeSp,
            onValueChange = { fontSizeSp = it },
            valueRange = 12f..48f,
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          PDF_PEN_COLORS.forEach { color ->
            val isSelected = selectedColor == color
            Box(
              modifier =
                Modifier.size(if (isSelected) 32.dp else 26.dp)
                  .clip(CircleShape)
                  .background(color)
                  .border(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color =
                      if (isSelected) {
                        MaterialTheme.colorScheme.primary
                      } else {
                        MaterialTheme.colorScheme.outlineVariant
                      },
                    shape = CircleShape,
                  )
                  .clickable { selectedColor = color },
              contentAlignment = Alignment.Center,
            ) {
              if (isSelected) {
                Icon(
                  Icons.Default.Check,
                  contentDescription = null,
                  tint =
                    if (color == Color.White || color == Color(0xFFFDD835)) Color.Black else Color.White,
                  modifier = Modifier.size(14.dp),
                )
              }
            }
          }
        }

        FilterChip(
          selected = hasBackground,
          onClick = { hasBackground = !hasBackground },
          label = { Text("Background callout pill") },
          leadingIcon =
            if (hasBackground) {
              { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
            } else {
              null
            },
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          FilledTonalButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f),
          ) {
            Text("Cancel")
          }
          Button(
            onClick = {
              if (textValue.isNotBlank()) {
                onConfirm(textValue.trim(), selectedColor, fontSizeSp, hasBackground)
              }
            },
            enabled = textValue.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f).testTag("pdf_text_confirm_button"),
          ) {
            Text(if (initialItem == null) "Add" else "Update", fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }
  }
}

@Composable
private fun PdfSaveDialog(
  sourceFile: File,
  hasEdits: Boolean,
  isSaving: Boolean,
  onDismiss: () -> Unit,
  onSave: (fileName: String, overwriteOriginal: Boolean) -> Unit,
) {
  val defaultNewName =
    remember(sourceFile) {
      val base = sourceFile.nameWithoutExtension
      "${base}_edited.pdf"
    }
  var fileName by remember(defaultNewName) { mutableStateOf(defaultNewName) }
  var overwriteOriginal by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
      tonalElevation = 6.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(40.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                Icons.Default.Save,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp),
              )
            }
          }
          Column {
            Text(
              text = "Save PDF Document",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text =
                if (hasEdits) {
                  "Export PDF with your drawings & text annotations"
                } else {
                  "Save a copy of this PDF document"
                },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        if (!overwriteOriginal) {
          OutlinedTextField(
            value = fileName,
            onValueChange = { fileName = it },
            label = { Text("Output file name") },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("pdf_save_filename_input"),
          )
        }

        if (sourceFile.parentFile?.canWrite() == true) {
          FilterChip(
            selected = overwriteOriginal,
            onClick = { overwriteOriginal = !overwriteOriginal },
            label = { Text("Overwrite original (${sourceFile.name})") },
            leadingIcon =
              if (overwriteOriginal) {
                {
                  Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                  )
                }
              } else {
                null
              },
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          FilledTonalButton(
            onClick = onDismiss,
            enabled = !isSaving,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f),
          ) {
            Text("Cancel")
          }
          Button(
            onClick = { onSave(fileName.trim().ifBlank { defaultNewName }, overwriteOriginal) },
            enabled = !isSaving && (overwriteOriginal || fileName.isNotBlank()),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(),
            modifier = Modifier.weight(1f).testTag("pdf_confirm_save_button"),
          ) {
            if (isSaving) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
              )
            } else {
              Text("Save PDF", fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun PdfViewerScreenPreview() {
  ToolboxTheme {
    PdfViewerScreen()
  }
}
