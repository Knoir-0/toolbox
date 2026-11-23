package com.example.toolbox.ui.tools

import com.example.toolbox.ui.theme.toolboxScreenBackground

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import com.example.toolbox.ui.theme.CircleShape
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
import com.example.toolbox.ui.theme.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.toolbox.ui.theme.FilledTonalButton
import com.example.toolbox.ui.theme.FilledTonalIconButton
import com.example.toolbox.ui.theme.FilterChip
import com.example.toolbox.ui.theme.HorizontalHexagonShape
import com.example.toolbox.ui.theme.currentHexagonIconButtons
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.example.toolbox.ui.theme.OutlinedButton
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
import androidx.compose.ui.graphics.TransformOrigin
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
  var pageBitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
  val pageCount = pageBitmaps.size
  var currentPageIndex by remember { mutableIntStateOf(0) }
  var visibleCenterYRatio by remember { mutableFloatStateOf(0.5f) }
  val documentScrollState = rememberScrollState()
  var isLoadingPage by remember { mutableStateOf(false) }
  var isSavingPdf by remember { mutableStateOf(false) }

  var showSaveDialog by remember { mutableStateOf(false) }

  val pdfPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
  ) { uri: Uri? ->
    if (uri != null) {
      scope.launch {
        isLoadingPage = true
        val copied = withContext(Dispatchers.IO) {
          copyUriToTempSourceFile(context, uri)
        }
        isLoadingPage = false
        if (copied != null) {
          currentFile = copied
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } else {
          Toast.makeText(context, "Unable to read selected PDF file", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  // Edit mode & history
  var editMode by remember { mutableStateOf(PdfEditMode.VIEW) }
  val editHistory = remember { PdfEditHistory() }
  var pageEditsMap by remember { mutableStateOf<Map<Int, PdfPageEdits>>(emptyMap()) }

  // View zoom & horizontal pan
  var zoomScale by remember { mutableFloatStateOf(1f) }
  var panOffsetX by remember { mutableFloatStateOf(0f) }

  // Draw controls state
  var brushType by remember { mutableStateOf(PdfBrushType.PEN) }
  var brushColor by remember { mutableStateOf(PDF_PEN_COLORS[0]) }
  var brushWidth by remember { mutableFloatStateOf(8f) }
  var activeStrokePageIndex by remember { mutableIntStateOf(0) }
  val activeStrokePoints = remember { mutableStateListOf<Offset>() }

  // Text annotation dialog state
  var showTextDialog by remember { mutableStateOf(false) }
  var editingTextPageIndex by remember { mutableIntStateOf(0) }
  var editingTextItem by remember { mutableStateOf<PdfTextItem?>(null) }
  var pendingTextPosition by remember { mutableStateOf(Offset(0.5f, 0.5f)) }

  fun syncHistoryState() {
    pageEditsMap = editHistory.currentEdits
  }

  // Load all PDF pages when currentFile changes
  LaunchedEffect(currentFile) {
    val file = currentFile
    if (file == null || !file.exists()) {
      pageBitmaps = emptyList()
      currentPageIndex = 0
      visibleCenterYRatio = 0.5f
      editHistory.reset()
      syncHistoryState()
      return@LaunchedEffect
    }
    isLoadingPage = true
    activeStrokePoints.clear()
    zoomScale = 1f
    panOffsetX = 0f
    editHistory.reset()
    syncHistoryState()
    val renderedPages =
      withContext(Dispatchers.IO) { PdfViewerEngine.renderAllPagesToBitmaps(file) }
    pageBitmaps = renderedPages
    currentPageIndex = 0
    visibleCenterYRatio = 0.5f
    documentScrollState.scrollTo(0)
    if (renderedPages.isEmpty()) {
      Toast.makeText(context, "Unable to read PDF document", Toast.LENGTH_SHORT).show()
    }
    isLoadingPage = false
  }

  if (onNavigateBack != null) {
    BackHandler { onNavigateBack() }
  }

  val safeCurrentPageIndex =
    if (pageCount > 0) currentPageIndex.coerceIn(0, pageCount - 1) else 0
  val currentPageEdits = pageEditsMap[safeCurrentPageIndex] ?: PdfPageEdits()

  if (showTextDialog) {
    PdfTextEditDialog(
      initialItem = editingTextItem,
      defaultColor = brushColor,
      onDismiss = {
        showTextDialog = false
        editingTextItem = null
      },
      onConfirm = { text, color, fontSizeSp, hasBg ->
        val targetPage =
          if (pageCount > 0) editingTextPageIndex.coerceIn(0, pageCount - 1) else 0
        val existing = editingTextItem
        if (existing != null) {
          editHistory.updateText(
            targetPage,
            existing.copy(
              text = text,
              color = color,
              fontSizeSp = fontSizeSp,
              hasBackground = hasBg,
            ),
          )
        } else {
          editHistory.addText(
            targetPage,
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
              val savedScroll = documentScrollState.value
              val savedPage = safeCurrentPageIndex
              currentFile = savedFile
              editHistory.reset()
              syncHistoryState()
              pageBitmaps =
                withContext(Dispatchers.IO) {
                  PdfViewerEngine.renderAllPagesToBitmaps(savedFile)
                }
              currentPageIndex = savedPage
              documentScrollState.scrollTo(savedScroll)
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
        .toolboxScreenBackground(MaterialTheme.colorScheme.background)
        .testTag("pdf_viewer_screen")
  ) {
    PdfViewerTopBar(
      fileName = currentFile?.name,
      currentPageIndex = safeCurrentPageIndex,
      pageCount = pageCount,
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
        pageBitmaps = emptyList()
        currentPageIndex = 0
        editHistory.reset()
        syncHistoryState()
      },
      onSave = { showSaveDialog = true },
    )

    if (currentFile == null || (pageCount == 0 && !isLoadingPage)) {
      OpenFileInitialView(
        onOpenFile = { pdfPickerLauncher.launch("application/pdf") },
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
      // Main Scrollable PDF Document Viewport (fits screen width when first loaded)
      Box(
        modifier =
          Modifier.weight(1f)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .clipToBounds(),
        contentAlignment = Alignment.Center,
      ) {
        if (isLoadingPage || pageBitmaps.isEmpty()) {
          CircularProgressIndicator()
        } else {
          PdfDocumentScrollableViewport(
            pageBitmaps = pageBitmaps,
            scrollState = documentScrollState,
            editMode = editMode,
            pageEditsMap = pageEditsMap,
            activeStrokePageIndex = activeStrokePageIndex,
            activeStrokePoints = activeStrokePoints,
            brushType = brushType,
            brushColor = brushColor,
            brushWidth = brushWidth,
            zoomScale = zoomScale,
            panOffsetX = panOffsetX,
            onZoomPanChange = { newZoom, newPanX ->
              zoomScale = newZoom
              panOffsetX = newPanX
            },
            onVisiblePageChange = { visiblePageIdx, centerRatioY ->
              if (visiblePageIdx in 0 until pageCount) {
                currentPageIndex = visiblePageIdx
                visibleCenterYRatio = centerRatioY
              }
            },
            onStartStrokeOnPage = { pageIdx ->
              activeStrokePageIndex = pageIdx
              currentPageIndex = pageIdx
            },
            onCommitStroke = { pageIdx, points ->
              if (points.isNotEmpty()) {
                currentPageIndex = pageIdx
                editHistory.addStroke(
                  pageIdx,
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
            onTapAddTextAt = { pageIdx, normalizedOffset ->
              currentPageIndex = pageIdx
              editingTextPageIndex = pageIdx
              pendingTextPosition = normalizedOffset
              editingTextItem = null
              showTextDialog = true
            },
            onUpdateTextPosition = { pageIdx, itemId, newXRatio, newYRatio ->
              currentPageIndex = pageIdx
              val pageEditsForTarget = pageEditsMap[pageIdx] ?: PdfPageEdits()
              val currentItem =
                pageEditsForTarget.texts.find { it.id == itemId }
                  ?: return@PdfDocumentScrollableViewport
              editHistory.updateText(
                pageIdx,
                currentItem.copy(
                  xRatio = newXRatio.coerceIn(0.04f, 0.96f),
                  yRatio = newYRatio.coerceIn(0.04f, 0.96f),
                ),
              )
              syncHistoryState()
            },
            onTapEditText = { pageIdx, item ->
              currentPageIndex = pageIdx
              editingTextPageIndex = pageIdx
              editingTextItem = item
              showTextDialog = true
            },
          )

          // Floating current page indicator pill
          Surface(
            shape =
              if (currentHexagonIconButtons()) {
                HorizontalHexagonShape
              } else {
                RoundedCornerShape(20.dp)
              },
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f),
            tonalElevation = 3.dp,
            modifier =
              Modifier.align(Alignment.TopStart)
                .padding(12.dp)
                .testTag("pdf_page_indicator"),
          ) {
            Text(
              text = "Page ${safeCurrentPageIndex + 1} / $pageCount",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
          }

          // Reset zoom pill when zoomed in
          if (zoomScale > 1.02f) {
            Surface(
              onClick = {
                zoomScale = 1f
                panOffsetX = 0f
              },
              shape =
                if (currentHexagonIconButtons()) {
                  HorizontalHexagonShape
                } else {
                  RoundedCornerShape(20.dp)
                },
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
                  if (zoomScale <= 1.01f) panOffsetX = 0f
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
                  editHistory.clearPage(safeCurrentPageIndex)
                  syncHistoryState()
                },
              )
            }
            PdfEditMode.TEXT -> {
              PdfTextModeControls(
                texts = currentPageEdits.texts,
                onAddTextClick = {
                  editingTextPageIndex = safeCurrentPageIndex
                  pendingTextPosition = Offset(0.5f, visibleCenterYRatio.coerceIn(0.1f, 0.9f))
                  editingTextItem = null
                  showTextDialog = true
                },
                onEditTextClick = { item ->
                  editingTextPageIndex = safeCurrentPageIndex
                  editingTextItem = item
                  showTextDialog = true
                },
                onDeleteTextClick = { item ->
                  editHistory.removeText(safeCurrentPageIndex, item.id)
                  syncHistoryState()
                },
              )
            }
          }

          Spacer(Modifier.height(8.dp))

          val hexagonEnabled = currentHexagonIconButtons()
          SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            PdfEditMode.entries.forEachIndexed { index, mode ->
              SegmentedButton(
                selected = editMode == mode,
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                  editMode = mode
                },
                shape = if (hexagonEnabled) {
                  HorizontalHexagonShape
                } else {
                  SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = PdfEditMode.entries.size,
                  )
                },
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
  currentPageIndex: Int,
  pageCount: Int,
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
  val subtext =
    if (fileName != null && pageCount > 0) {
      "$fileName • Page ${currentPageIndex + 1} of $pageCount"
    } else {
      fileName ?: "View, draw & add text to PDFs"
    }
  ToolboxTopBar(
    title = "PDF Viewer",
    subtext = subtext,
    icon = Icons.Default.PictureAsPdf,
    onNavigateBack = onNavigateBack,
    backButtonTestTag = "action_navigate_back",
    additionalBackButtonTestTag = "pdf_back_button",
    actions = {
      if (hasDocument) {
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
    },
  )
}

@Composable
private fun PdfDocumentScrollableViewport(
  pageBitmaps: List<Bitmap>,
  scrollState: ScrollState,
  editMode: PdfEditMode,
  pageEditsMap: Map<Int, PdfPageEdits>,
  activeStrokePageIndex: Int,
  activeStrokePoints: MutableList<Offset>,
  brushType: PdfBrushType,
  brushColor: Color,
  brushWidth: Float,
  zoomScale: Float,
  panOffsetX: Float,
  onZoomPanChange: (Float, Float) -> Unit,
  onVisiblePageChange: (Int, Float) -> Unit,
  onStartStrokeOnPage: (Int) -> Unit,
  onCommitStroke: (Int, List<Offset>) -> Unit,
  onTapAddTextAt: (Int, Offset) -> Unit,
  onUpdateTextPosition: (Int, Long, Float, Float) -> Unit,
  onTapEditText: (Int, PdfTextItem) -> Unit,
) {
  val density = LocalDensity.current
  val currentZoom by rememberUpdatedState(zoomScale)
  val currentPanX by rememberUpdatedState(panOffsetX)
  val currentOnZoomPanChange by rememberUpdatedState(onZoomPanChange)
  val currentOnVisiblePageChange by rememberUpdatedState(onVisiblePageChange)

  val pageSpacingDp = 12.dp
  val pageSpacingPx = with(density) { pageSpacingDp.toPx() }

  BoxWithConstraints(
    modifier = Modifier.fillMaxSize().testTag("pdf_document_scroll_container"),
    contentAlignment = Alignment.TopCenter,
  ) {
    val containerW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
    val containerH = constraints.maxHeight.toFloat().coerceAtLeast(1f)

    // Each page fits the full width of the screen when loaded (zoomScale == 1f)
    val basePageSizes =
      remember(pageBitmaps, containerW) {
        pageBitmaps.map { bmp ->
          PdfViewerEngine.calculateFitWidthPageSize(
            containerWidth = containerW,
            bitmapWidth = bmp.width,
            bitmapHeight = bmp.height,
          )
        }
      }
    val scaledPageHeights =
      remember(basePageSizes, zoomScale) {
        basePageSizes.map { (_, h) -> h * zoomScale }
      }

    // Track active page and visible center ratio as the user scrolls
    LaunchedEffect(scrollState.value, containerH, scaledPageHeights, pageSpacingPx) {
      if (scaledPageHeights.isNotEmpty()) {
        val visibleIdx =
          PdfViewerEngine.resolveVisiblePageIndex(
            scrollOffsetPx = scrollState.value,
            viewportHeightPx = containerH,
            pageHeightsPx = scaledPageHeights,
            pageSpacingPx = pageSpacingPx,
          )
        var pageTopY = 0f
        for (i in 0 until visibleIdx) {
          pageTopY += scaledPageHeights[i] + pageSpacingPx
        }
        val pageH = scaledPageHeights[visibleIdx].coerceAtLeast(1f)
        val viewportCenterY = scrollState.value.toFloat() + (containerH / 2f)
        val centerRatio = ((viewportCenterY - pageTopY) / pageH).coerceIn(0.1f, 0.9f)
        currentOnVisiblePageChange(visibleIdx, centerRatio)
      }
    }

    val horizontalDragState = rememberDraggableState { deltaX ->
      if (currentZoom > 1.01f) {
        val maxPanX = (containerW * (currentZoom - 1f)) / 2f
        val nextPanX = (currentPanX + deltaX).coerceIn(-maxPanX, maxPanX)
        currentOnZoomPanChange(currentZoom, nextPanX)
      }
    }

    val isScrollEnabled = editMode == PdfEditMode.VIEW

    Column(
      modifier =
        Modifier.fillMaxSize()
          .pointerInput(editMode, containerW) {
            if (editMode == PdfEditMode.VIEW) {
              awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                do {
                  val event = awaitPointerEvent()
                  val pressedCount = event.changes.count { it.pressed }
                  if (pressedCount >= 2) {
                    val zoomChange = event.calculateZoom()
                    val panChange = event.calculatePan()
                    if (zoomChange != 1f || panChange != Offset.Zero) {
                      val newZoom = (currentZoom * zoomChange).coerceIn(1f, 4f)
                      val maxPanX = (containerW * (newZoom - 1f)) / 2f
                      val nextPanX =
                        if (newZoom <= 1.01f) {
                          0f
                        } else {
                          (currentPanX + panChange.x).coerceIn(-maxPanX, maxPanX)
                        }
                      currentOnZoomPanChange(newZoom, nextPanX)
                      if (panChange.y != 0f) {
                        scrollState.dispatchRawDelta(-panChange.y)
                      }
                      event.changes.forEach { it.consume() }
                    }
                  }
                } while (event.changes.any { it.pressed })
              }
            }
          }
          .draggable(
            state = horizontalDragState,
            orientation = Orientation.Horizontal,
            enabled = isScrollEnabled && zoomScale > 1.01f,
          )
          .verticalScroll(
            state = scrollState,
            enabled = isScrollEnabled,
          )
          .testTag("pdf_pages_column"),
      verticalArrangement = Arrangement.spacedBy(pageSpacingDp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      pageBitmaps.forEachIndexed { pageIndex, pageBitmap ->
        val (pageDisplayW, pageDisplayH) = basePageSizes[pageIndex]
        val pageEdits = pageEditsMap[pageIndex] ?: PdfPageEdits()
        val isActiveStrokePage = activeStrokePageIndex == pageIndex

        PdfSinglePageInteractiveSurface(
          pageIndex = pageIndex,
          pageBitmap = pageBitmap,
          pageDisplayW = pageDisplayW,
          pageDisplayH = pageDisplayH,
          editMode = editMode,
          pageEdits = pageEdits,
          isActiveStrokePage = isActiveStrokePage,
          activeStrokePoints = activeStrokePoints,
          brushType = brushType,
          brushColor = brushColor,
          brushWidth = brushWidth,
          zoomScale = zoomScale,
          panOffsetX = panOffsetX,
          onStartStroke = { onStartStrokeOnPage(pageIndex) },
          onCommitStroke = { points -> onCommitStroke(pageIndex, points) },
          onTapAddTextAt = { normalizedOffset -> onTapAddTextAt(pageIndex, normalizedOffset) },
          onUpdateTextPosition = { itemId, newX, newY ->
            onUpdateTextPosition(pageIndex, itemId, newX, newY)
          },
          onTapEditText = { item -> onTapEditText(pageIndex, item) },
        )
      }
    }
  }
}

@Composable
private fun PdfSinglePageInteractiveSurface(
  pageIndex: Int,
  pageBitmap: Bitmap,
  pageDisplayW: Float,
  pageDisplayH: Float,
  editMode: PdfEditMode,
  pageEdits: PdfPageEdits,
  isActiveStrokePage: Boolean,
  activeStrokePoints: MutableList<Offset>,
  brushType: PdfBrushType,
  brushColor: Color,
  brushWidth: Float,
  zoomScale: Float,
  panOffsetX: Float,
  onStartStroke: () -> Unit,
  onCommitStroke: (List<Offset>) -> Unit,
  onTapAddTextAt: (Offset) -> Unit,
  onUpdateTextPosition: (Long, Float, Float) -> Unit,
  onTapEditText: (PdfTextItem) -> Unit,
) {
  val density = LocalDensity.current
  val currentBrushType by rememberUpdatedState(brushType)
  val currentBrushColor by rememberUpdatedState(brushColor)
  val currentBrushWidth by rememberUpdatedState(brushWidth)
  val currentOnStartStroke by rememberUpdatedState(onStartStroke)
  val currentOnCommitStroke by rememberUpdatedState(onCommitStroke)
  val currentOnTapAddTextAt by rememberUpdatedState(onTapAddTextAt)

  val pageWidthDp = with(density) { pageDisplayW.toDp() }
  val pageHeightDp = with(density) { pageDisplayH.toDp() }
  val scaledPageHeightDp = with(density) { (pageDisplayH * zoomScale).toDp() }
  val scaleFactor = min(pageDisplayW, pageDisplayH) / 1000f

  Box(
    modifier =
      Modifier.width(pageWidthDp)
        .height(scaledPageHeightDp)
        .clipToBounds()
        .testTag("pdf_page_slot_${pageIndex + 1}"),
    contentAlignment = Alignment.TopCenter,
  ) {
    Box(
      modifier =
        Modifier.size(pageWidthDp, pageHeightDp)
          .background(Color.White)
          .graphicsLayer {
            transformOrigin = TransformOrigin(0.5f, 0f)
            scaleX = zoomScale
            scaleY = zoomScale
            translationX = panOffsetX
          }
          .pointerInput(editMode, pageIndex, pageDisplayW, pageDisplayH) {
            when (editMode) {
              PdfEditMode.VIEW -> {
                // Handled by parent PdfDocumentScrollableViewport (vertical scroll, pinch zoom, horizontal pan)
              }
              PdfEditMode.DRAW -> {
                detectDragGestures(
                  onDragStart = { startOffset ->
                    currentOnStartStroke()
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
          .testTag(if (pageIndex == 0) "pdf_page_canvas" else "pdf_page_canvas_$pageIndex"),
    ) {
      // Base PDF Page Image
      Image(
        bitmap = pageBitmap.asImageBitmap(),
        contentDescription = "PDF Page ${pageIndex + 1}",
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
          if (isActiveStrokePage && activeStrokePoints.isNotEmpty()) {
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
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              Icons.Default.TextFields,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp),
            )
          }
          Column {
            Text(
              text = if (initialItem == null) "Add Text Annotation" else "Edit Text Annotation",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
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
            shape = CircleShape,
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
            shape = CircleShape,
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
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              Icons.Default.Save,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp),
            )
          }
          Column {
            Text(
              text = "Save PDF Document",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
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
            shape = CircleShape,
            modifier = Modifier.weight(1f),
          ) {
            Text("Cancel")
          }
          Button(
            onClick = { onSave(fileName.trim().ifBlank { defaultNewName }, overwriteOriginal) },
            enabled = !isSaving && (overwriteOriginal || fileName.isNotBlank()),
            shape = CircleShape,
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
