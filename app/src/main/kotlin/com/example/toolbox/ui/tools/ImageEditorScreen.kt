package com.example.toolbox.ui.tools

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.RectF
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.res.painterResource
import com.example.toolbox.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ZoomInMap
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

enum class ImageEditorMode(val label: String) {
  CROP("Crop"),
  ROTATE("Rotate"),
  TILT("Tilt"),
  ADJUST("Adjust"),
  DRAW("Draw"),
}

@Composable
fun ImageEditorScreen(
  modifier: Modifier = Modifier,
  initialBitmap: Bitmap? = null,
  initialFile: File? = null,
  onNavigateBack: (() -> Unit)? = null,
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current

  val resolvedInitialBitmap = remember(initialBitmap, initialFile?.absolutePath) {
    initialBitmap ?: initialFile?.let { file ->
      decodeBitmapFromFile(file) ?: if (file.exists()) {
        Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
      } else {
        null
      }
    }
  }

  var currentBitmap by remember(resolvedInitialBitmap) { mutableStateOf<Bitmap?>(resolvedInitialBitmap) }
  var originalBitmap by remember(resolvedInitialBitmap) { mutableStateOf<Bitmap?>(resolvedInitialBitmap) }

  val history = remember { ImageEditorHistory<Bitmap>() }
  var activeMode by rememberSaveable { mutableStateOf(ImageEditorMode.CROP) }

  // Unsaved changes confirmation dialog state
  var showDiscardDialog by remember { mutableStateOf(false) }
  var pendingDiscardAction by remember { mutableStateOf<(() -> Unit)?>(null) }

  fun performActionWithDiscardCheck(action: () -> Unit) {
    if (history.canUndo) {
      pendingDiscardAction = action
      showDiscardDialog = true
    } else {
      action()
    }
  }

  BackHandler(enabled = onNavigateBack != null) {
    performActionWithDiscardCheck {
      onNavigateBack?.invoke()
    }
  }

  // File picker launcher
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
  ) { uri: Uri? ->
    if (uri != null) {
      val loaded = decodeBitmapFromUri(context, uri)
      if (loaded != null) {
        originalBitmap = loaded
        currentBitmap = loaded
        history.clear()
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
      } else {
        Toast.makeText(context, "Failed to load selected image", Toast.LENGTH_SHORT).show()
      }
    }
  }

  // Unsaved Changes Confirmation Dialog
  if (showDiscardDialog) {
    AlertDialog(
      onDismissRequest = { showDiscardDialog = false },
      title = { Text("Discard Unsaved Changes?") },
      text = { Text("You have uncommitted modifications that will be lost.") },
      confirmButton = {
        Button(
          onClick = {
            showDiscardDialog = false
            pendingDiscardAction?.invoke()
            pendingDiscardAction = null
          },
        ) {
          Text("Discard")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showDiscardDialog = false }) {
          Text("Keep Editing")
        }
      },
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("image_editor_screen"),
  ) {
    // Top Toolbar Header (matching DESIGN_LANGUAGE.md Section 5.1)
    ImageEditorTopBar(
      bitmap = currentBitmap,
      activeMode = activeMode,
      canUndo = history.canUndo,
      canRedo = history.canRedo,
      onUndo = {
        currentBitmap?.let { bmp ->
          history.undo(bmp)?.let { prev ->
            currentBitmap = prev
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          }
        }
      },
      onRedo = {
        currentBitmap?.let { bmp ->
          history.redo(bmp)?.let { next ->
            currentBitmap = next
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          }
        }
      },
      onCloseImage = {
        performActionWithDiscardCheck {
          currentBitmap = null
          originalBitmap = null
          history.clear()
        }
      },
      onSave = {
        currentBitmap?.let { bmp ->
          val uri = saveBitmapToGallery(context, bmp)
          if (uri != null) {
            Toast.makeText(context, "Image saved to Photos!", Toast.LENGTH_SHORT).show()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          } else {
            Toast.makeText(context, "Saved image to storage", Toast.LENGTH_SHORT).show()
          }
        }
      },
      onNavigateBack = onNavigateBack?.let { navBack ->
        {
          performActionWithDiscardCheck {
            navBack()
          }
        }
      },
    )

    if (currentBitmap == null) {
      // First View: Simple + icon with text "tap to open file"
      OpenFileInitialView(
        onOpenFile = { filePickerLauncher.launch("image/*") },
        onUseSample = {
          val sample = BitmapOperations.createSampleBitmap()
          originalBitmap = sample
          currentBitmap = sample
          history.clear()
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        },
        modifier = Modifier.weight(1f),
      )
    } else {
      // Image Editor UI
      val bmp = currentBitmap!!
      ImageEditorContent(
        bitmap = bmp,
        activeMode = activeMode,
        onModeSelected = {
          activeMode = it
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        },
        onApplyBitmapChange = { newBmp ->
          history.push(bmp)
          currentBitmap = newBmp
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun ImageEditorTopBar(
  bitmap: Bitmap?,
  activeMode: ImageEditorMode,
  canUndo: Boolean,
  canRedo: Boolean,
  onUndo: () -> Unit,
  onRedo: () -> Unit,
  onCloseImage: () -> Unit,
  onSave: () -> Unit,
  onNavigateBack: (() -> Unit)? = null,
) {
  Surface(
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 68.dp),
      contentAlignment = Alignment.CenterStart,
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          if (onNavigateBack != null) {
            IconButton(
              onClick = onNavigateBack,
              modifier = Modifier
                .size(36.dp)
                .testTag("action_navigate_back"),
            ) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
          } else {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
              )
            }
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Image Editor",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = if (bitmap != null) {
                "${bitmap.width}×${bitmap.height} px • ${activeMode.label}"
              } else {
                "Crop, rotate, tilt, adjust & draw"
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        if (bitmap != null) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            IconButton(
              onClick = onUndo,
              enabled = canUndo,
              modifier = Modifier
                .size(38.dp)
                .testTag("editor_undo_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo",
                modifier = Modifier.size(20.dp),
              )
            }
            IconButton(
              onClick = onRedo,
              enabled = canRedo,
              modifier = Modifier
                .size(38.dp)
                .testTag("editor_redo_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Redo,
                contentDescription = "Redo",
                modifier = Modifier.size(20.dp),
              )
            }
            IconButton(
              onClick = onCloseImage,
              modifier = Modifier
                .size(38.dp)
                .testTag("editor_close_button"),
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close image",
                modifier = Modifier.size(20.dp),
              )
            }
            Spacer(modifier = Modifier.width(4.dp))
            FilledIconButton(
              onClick = onSave,
              shape = CircleShape,
              modifier = Modifier
                .size(36.dp)
                .testTag("editor_save_button"),
            ) {
              Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Save image",
                modifier = Modifier.size(18.dp),
              )
            }
          }
        }
      }
    }
  }
}

/**
 * The initial view displayed before a media file is opened:
 * simply a + icon with the text "tap to open file" and a bottom sample button.
 */
@Composable
fun OpenFileInitialView(
  onOpenFile: () -> Unit,
  onUseSample: () -> Unit,
  modifier: Modifier = Modifier,
  sampleButtonText: String = "or try sample image",
  sampleButtonIcon: ImageVector = Icons.Default.Image,
  sampleButtonTestTag: String = "open_sample_image_button",
  openButtonTestTag: String = "open_file_button",
  isLoading: Boolean = false,
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .clickable(enabled = !isLoading, onClick = onOpenFile)
      .testTag(openButtonTestTag)
      .padding(24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier.align(Alignment.Center),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Surface(
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shadowElevation = 4.dp,
      ) {
        Box(contentAlignment = Alignment.Center) {
          if (isLoading) {
            CircularProgressIndicator(
              modifier = Modifier.size(32.dp),
              strokeWidth = 3.dp,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
          } else {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Open file",
              modifier = Modifier.size(40.dp),
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(20.dp))
      Text(
        text = "tap to open file",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("tap_to_open_file_text"),
      )
    }

    // Convenient sample option for testing without requiring local files
    TextButton(
      onClick = onUseSample,
      enabled = !isLoading,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 16.dp)
        .testTag(sampleButtonTestTag),
    ) {
      Icon(
        imageVector = sampleButtonIcon,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(sampleButtonText)
    }
  }
}

/**
 * Main editor content displaying the image canvas and mode controls.
 */
@Composable
fun ImageEditorContent(
  bitmap: Bitmap,
  activeMode: ImageEditorMode,
  onModeSelected: (ImageEditorMode) -> Unit,
  onApplyBitmapChange: (Bitmap) -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current

  // Crop state
  var cropRect by remember(bitmap) { mutableStateOf(NormalizedRect(0f, 0f, 1f, 1f)) }
  var selectedAspect by remember { mutableStateOf(AspectRatioPreset.FREE) }
  var squareOrientationVertical by remember(bitmap) { mutableStateOf(false) }

  // Rotate state
  var rotateDegrees by remember { mutableFloatStateOf(0f) }
  var flipHorizontal by remember { mutableStateOf(false) }
  var flipVertical by remember { mutableStateOf(false) }

  // Tilt state
  var tiltAngle by remember { mutableFloatStateOf(0f) }
  var horizontalTilt by remember { mutableFloatStateOf(0f) }
  var verticalTilt by remember { mutableFloatStateOf(0f) }
  var autoCropBorders by remember { mutableStateOf(true) }

  // Color adjustments state
  var adjustments by remember { mutableStateOf(ColorAdjustments()) }

  // Drawing state
  val strokes = remember { mutableStateListOf<ImageDrawingStroke>() }
  val textAnnotations = remember { mutableStateListOf<ImageTextAnnotation>() }
  var currentColor by remember { mutableStateOf(Color.Red) }
  var currentStrokeWidth by remember { mutableFloatStateOf(10f) }
  var isEraser by remember { mutableStateOf(false) }

  // Text annotation input dialog
  var showAddTextDialog by remember { mutableStateOf(false) }
  var newAnnotationText by remember { mutableStateOf("") }

  // Pinch-to-zoom & Pan state
  var zoomScale by remember { mutableFloatStateOf(1f) }
  var panOffset by remember { mutableStateOf(Offset.Zero) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 4.dp),
  ) {
    // Center Image Viewport / Canvas
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(horizontal = 2.dp, vertical = 2.dp)
        .testTag("image_canvas_container"),
      contentAlignment = Alignment.Center,
    ) {
      val boxWidth = constraints.maxWidth.toFloat()
      val boxHeight = constraints.maxHeight.toFloat()
      val bmpWidth = bitmap.width.toFloat()
      val bmpHeight = bitmap.height.toFloat()

      val density = LocalContext.current.resources.displayMetrics.density
      val marginPx = 8.dp.value * density
      val availWidth = (boxWidth - marginPx).coerceAtLeast(10f)
      val availHeight = (boxHeight - marginPx).coerceAtLeast(10f)

      if (availWidth > 0f && availHeight > 0f && bmpWidth > 0f && bmpHeight > 0f) {
        val bmpAspect = bmpWidth / bmpHeight
        val boxAspect = availWidth / availHeight

        val displayWidth: Float
        val displayHeight: Float
        if (bmpAspect > boxAspect) {
          displayWidth = availWidth
          displayHeight = availWidth / bmpAspect
        } else {
          displayHeight = availHeight
          displayWidth = availHeight * bmpAspect
        }

        // Render Image with overlays for current mode
        Box(
          modifier = Modifier
            .size(
              width = (displayWidth / density).dp,
              height = (displayHeight / density).dp,
            )
            .graphicsLayer {
              scaleX = zoomScale
              scaleY = zoomScale
              translationX = panOffset.x
              translationY = panOffset.y
            }
            .pointerInput(Unit) {
              detectTransformGestures { _, pan, zoom, _ ->
                zoomScale = (zoomScale * zoom).coerceIn(1f, 4f)
                val maxPanX = (displayWidth * (zoomScale - 1f)) / 2f
                val maxPanY = (displayHeight * (zoomScale - 1f)) / 2f
                panOffset = Offset(
                  x = (panOffset.x + pan.x).coerceIn(-maxPanX, maxPanX),
                  y = (panOffset.y + pan.y).coerceIn(-maxPanY, maxPanY),
                )
              }
            },
        ) {
          val composeBitmap = remember(bitmap) { bitmap.asImageBitmap() }

          // Transformation preview for Rotate & Tilt modes
          val previewRotation = if (activeMode == ImageEditorMode.ROTATE) rotateDegrees else 0f
          val previewTiltAngle = if (activeMode == ImageEditorMode.TILT) tiltAngle else 0f
          val previewHorizTilt = if (activeMode == ImageEditorMode.TILT) horizontalTilt else 0f
          val previewVertTilt = if (activeMode == ImageEditorMode.TILT) verticalTilt else 0f
          val previewScaleX = if (activeMode == ImageEditorMode.ROTATE && flipHorizontal) -1f else 1f
          val previewScaleY = if (activeMode == ImageEditorMode.ROTATE && flipVertical) -1f else 1f

          // Scale factor to eliminate empty border triangles when auto-cropping in Tilt mode
          val previewTiltScale = if (activeMode == ImageEditorMode.TILT && autoCropBorders) {
            val aspect = if (bitmap.height > 0) bitmap.width.toFloat() / bitmap.height.toFloat() else 1f
            calculateTiltScaleFactor(
              tiltAngle = tiltAngle,
              horizontalTilt = horizontalTilt,
              verticalTilt = verticalTilt,
              aspectRatio = aspect,
            )
          } else {
            1f
          }

          // Color filter preview for Adjust mode
          val colorFilter = if (activeMode == ImageEditorMode.ADJUST) {
            remember(adjustments) { ColorMatrixColorFilter(buildColorMatrix(adjustments)) }
          } else null

          Canvas(
            modifier = Modifier
              .fillMaxSize()
              .clipToBounds()
              .graphicsLayer {
                rotationX = previewVertTilt
                rotationY = previewHorizTilt
                rotationZ = previewRotation + previewTiltAngle
                scaleX = previewScaleX * previewTiltScale
                scaleY = previewScaleY * previewTiltScale
                cameraDistance = 16f * density
              },
          ) {
            val paint = android.graphics.Paint().apply {
              isAntiAlias = true
              isDither = true
              if (colorFilter != null) {
                this.colorFilter = colorFilter
              }
            }
            drawContext.canvas.nativeCanvas.drawBitmap(
              bitmap,
              android.graphics.Rect(0, 0, bitmap.width, bitmap.height),
              android.graphics.Rect(0, 0, size.width.roundToInt(), size.height.roundToInt()),
              paint,
            )
          }

          // Active Mode Specific Overlays
          when (activeMode) {
            ImageEditorMode.CROP -> {
              CropOverlay(
                cropRect = cropRect,
                onCropRectChange = { cropRect = it },
                containerSize = Size(displayWidth, displayHeight),
                lockedAspectRatio = if (selectedAspect == AspectRatioPreset.FREE) null else selectedAspect.ratio,
                imageWidth = bitmap.width.toFloat(),
                imageHeight = bitmap.height.toFloat(),
              )
            }
            ImageEditorMode.TILT -> {
              TiltAlignmentGridOverlay()
            }
            ImageEditorMode.DRAW -> {
              DrawingCanvasOverlay(
                strokes = strokes,
                textAnnotations = textAnnotations,
                currentColor = currentColor,
                currentStrokeWidth = currentStrokeWidth,
                isEraser = isEraser,
                bitmapWidth = bitmap.width,
                bitmapHeight = bitmap.height,
              )
            }
            ImageEditorMode.ADJUST -> {
              // Real-time matrix filter is drawn on Canvas
            }
            ImageEditorMode.ROTATE -> {
              // Rotation preview handled by graphicsLayer
            }
          }
        }

        // Floating Zoom Reset badge when zoomed in
        if (zoomScale > 1.05f) {
          Surface(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(8.dp)
              .clip(CircleShape)
              .clickable {
                zoomScale = 1f
                panOffset = Offset.Zero
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              },
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shadowElevation = 4.dp,
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Default.ZoomInMap,
                contentDescription = "Reset zoom",
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${"%.1f".format(zoomScale)}x (Reset)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Mode Specific Controls Panel
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp)),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp),
      ) {
        when (activeMode) {
          ImageEditorMode.CROP -> {
            CropControls(
              selectedAspect = selectedAspect,
              isVertical = isVerticalAspectRatio(
                preset = selectedAspect,
                cropRect = cropRect,
                imageWidth = bitmap.width.toFloat(),
                imageHeight = bitmap.height.toFloat(),
                squareIsVertical = squareOrientationVertical,
              ),
              onAspectSelected = { preset ->
                selectedAspect = preset
                squareOrientationVertical = false
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (preset.ratio != null) {
                  cropRect = computeCropForAspectRatio(
                    targetRatio = preset.ratio,
                    imageWidth = bitmap.width.toFloat(),
                    imageHeight = bitmap.height.toFloat(),
                  )
                } else {
                  cropRect = NormalizedRect(0f, 0f, 1f, 1f)
                }
              },
              onSwapAspectRatio = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (selectedAspect == AspectRatioPreset.SQUARE) {
                  squareOrientationVertical = !squareOrientationVertical
                } else {
                  val (newPreset, newRect) = swapCropAspectRatio(
                    preset = selectedAspect,
                    cropRect = cropRect,
                    imageWidth = bitmap.width.toFloat(),
                    imageHeight = bitmap.height.toFloat(),
                  )
                  selectedAspect = newPreset
                  cropRect = newRect
                }
              },
              onApplyCrop = {
                val cropped = BitmapOperations.crop(bitmap, cropRect)
                onApplyBitmapChange(cropped)
                cropRect = NormalizedRect(0f, 0f, 1f, 1f)
                selectedAspect = AspectRatioPreset.FREE
                squareOrientationVertical = false
              },
              onResetCrop = {
                cropRect = NormalizedRect(0f, 0f, 1f, 1f)
                selectedAspect = AspectRatioPreset.FREE
                squareOrientationVertical = false
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              },
            )
          }
          ImageEditorMode.ROTATE -> {
            RotateControls(
              degrees = rotateDegrees,
              onDegreesChange = { rotateDegrees = it },
              onRotateBy = { delta ->
                rotateDegrees = (rotateDegrees + delta) % 360f
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              },
              onToggleFlipH = {
                flipHorizontal = !flipHorizontal
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              },
              onToggleFlipV = {
                flipVertical = !flipVertical
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              },
              onApplyRotation = {
                val rotated = BitmapOperations.rotate(
                  source = bitmap,
                  degrees = rotateDegrees,
                  flipHorizontal = flipHorizontal,
                  flipVertical = flipVertical,
                )
                onApplyBitmapChange(rotated)
                rotateDegrees = 0f
                flipHorizontal = false
                flipVertical = false
              },
              onResetRotation = {
                rotateDegrees = 0f
                flipHorizontal = false
                flipVertical = false
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              },
            )
          }
          ImageEditorMode.TILT -> {
            TiltControls(
              tiltAngle = tiltAngle,
              horizontalTilt = horizontalTilt,
              verticalTilt = verticalTilt,
              autoCropBorders = autoCropBorders,
              onTiltAngleChange = { tiltAngle = it },
              onHorizontalTiltChange = { horizontalTilt = it },
              onVerticalTiltChange = { verticalTilt = it },
              onAutoCropBordersChange = { autoCropBorders = it },
              onApplyTilt = {
                val tilted = BitmapOperations.tilt(
                  source = bitmap,
                  tiltAngle = tiltAngle,
                  horizontalTilt = horizontalTilt,
                  verticalTilt = verticalTilt,
                  cropBorders = autoCropBorders,
                )
                onApplyBitmapChange(tilted)
                tiltAngle = 0f
                horizontalTilt = 0f
                verticalTilt = 0f
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              },
              onResetTilt = {
                tiltAngle = 0f
                horizontalTilt = 0f
                verticalTilt = 0f
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              },
            )
          }
          ImageEditorMode.ADJUST -> {
            AdjustControls(
              adjustments = adjustments,
              onAdjustmentsChange = { adjustments = it },
              onApplyAdjustments = {
                val adjusted = BitmapOperations.applyColorAdjustments(bitmap, adjustments)
                onApplyBitmapChange(adjusted)
                adjustments = ColorAdjustments()
              },
              onResetAdjustments = {
                adjustments = ColorAdjustments()
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              },
            )
          }
          ImageEditorMode.DRAW -> {
            DrawControls(
              strokesCount = strokes.size,
              textCount = textAnnotations.size,
              currentColor = currentColor,
              currentStrokeWidth = currentStrokeWidth,
              isEraser = isEraser,
              onColorSelected = {
                currentColor = it
                isEraser = false
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              },
              onStrokeWidthSelected = {
                currentStrokeWidth = it
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              },
              onToggleEraser = {
                isEraser = !isEraser
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              },
              onAddTextClicked = {
                newAnnotationText = ""
                showAddTextDialog = true
              },
              onUndoStroke = {
                if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex)
                else if (textAnnotations.isNotEmpty()) textAnnotations.removeAt(textAnnotations.lastIndex)
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              },
              onClearStrokes = {
                strokes.clear()
                textAnnotations.clear()
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              },
              onApplyDrawing = {
                var composited = bitmap
                if (strokes.isNotEmpty()) {
                  composited = BitmapOperations.renderStrokes(composited, strokes)
                  strokes.clear()
                }
                if (textAnnotations.isNotEmpty()) {
                  composited = BitmapOperations.renderAnnotations(composited, textAnnotations)
                  textAnnotations.clear()
                }
                onApplyBitmapChange(composited)
              },
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Bottom Mode Selector: Crop, Rotate, Tilt, Adjust, Draw
    ModeSelectorRow(
      activeMode = activeMode,
      onModeSelected = onModeSelected,
    )
  }

  // Dialog to add text annotation
  if (showAddTextDialog) {
    AlertDialog(
      onDismissRequest = { showAddTextDialog = false },
      title = { Text("Add Text Caption") },
      text = {
        Column {
          OutlinedTextField(
            value = newAnnotationText,
            onValueChange = { newAnnotationText = it },
            label = { Text("Text") },
            placeholder = { Text("Enter caption...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newAnnotationText.isNotBlank()) {
              textAnnotations.add(
                ImageTextAnnotation(
                  text = newAnnotationText.trim(),
                  color = currentColor,
                  xRatio = 0.5f,
                  yRatio = 0.5f,
                ),
              )
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            showAddTextDialog = false
          },
        ) {
          Text("Add")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showAddTextDialog = false }) {
          Text("Cancel")
        }
      },
    )
  }
}

/**
 * Bottom segmented row to toggle between Crop, Rotate, Tilt, Adjust, Draw.
 */
@Composable
fun ModeSelectorRow(
  activeMode: ImageEditorMode,
  onModeSelected: (ImageEditorMode) -> Unit,
  modifier: Modifier = Modifier,
) {
  val modes = ImageEditorMode.entries
  SingleChoiceSegmentedButtonRow(
    modifier = modifier
      .fillMaxWidth()
      .padding(bottom = 6.dp)
      .testTag("mode_selector_row"),
  ) {
    modes.forEachIndexed { index, mode ->
      SegmentedButton(
        shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
        onClick = { onModeSelected(mode) },
        selected = activeMode == mode,
        icon = {},
        label = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
          ) {
            val icon = when (mode) {
              ImageEditorMode.CROP -> Icons.Default.Crop
              ImageEditorMode.ROTATE -> Icons.AutoMirrored.Filled.RotateRight
              ImageEditorMode.TILT -> Icons.Default.Straighten
              ImageEditorMode.ADJUST -> Icons.Default.Tune
              ImageEditorMode.DRAW -> Icons.Default.Brush
            }
            Icon(
              imageVector = icon,
              contentDescription = null,
              modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(mode.label, style = MaterialTheme.typography.labelSmall)
          }
        },
      )
    }
  }
}

// -------------------------------------------------------------
// Crop Overlay & Controls
// -------------------------------------------------------------

@Composable
fun CropOverlay(
  cropRect: NormalizedRect,
  onCropRectChange: (NormalizedRect) -> Unit,
  containerSize: Size,
  lockedAspectRatio: Float? = null,
  imageWidth: Float = 1f,
  imageHeight: Float = 1f,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current
  var activeHandle by remember { mutableStateOf<CropHandle?>(null) }
  val primaryColor = MaterialTheme.colorScheme.primary

  val currentCropRect by rememberUpdatedState(cropRect)
  val currentOnCropChange by rememberUpdatedState(onCropRectChange)

  Box(
    modifier = modifier
      .fillMaxSize()
      .pointerInput(containerSize.width, containerSize.height, lockedAspectRatio) {
        var dragStartRect = NormalizedRect()
        var accumulatedDeltaX = 0f
        var accumulatedDeltaY = 0f

        detectDragGestures(
          onDragStart = { offset ->
            if (containerSize.width > 0f && containerSize.height > 0f) {
              dragStartRect = currentCropRect
              accumulatedDeltaX = 0f
              accumulatedDeltaY = 0f
              activeHandle = hitTestCropHandle(
                touchX = offset.x,
                touchY = offset.y,
                cropRect = currentCropRect,
                containerWidth = containerSize.width,
                containerHeight = containerSize.height,
                cornerThresholdPx = 64.dp.toPx(),
                edgeThresholdPx = 48.dp.toPx(),
              )
              if (activeHandle != null) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              }
            }
          },
          onDrag = { change, dragAmount ->
            change.consume()
            val handle = activeHandle ?: return@detectDragGestures
            if (containerSize.width > 0f && containerSize.height > 0f) {
              accumulatedDeltaX += dragAmount.x / containerSize.width
              accumulatedDeltaY += dragAmount.y / containerSize.height
              val updated = updateCropWithHandle(
                rect = dragStartRect,
                handle = handle,
                deltaX = accumulatedDeltaX,
                deltaY = accumulatedDeltaY,
                lockedAspectRatio = lockedAspectRatio,
                imageWidth = imageWidth,
                imageHeight = imageHeight,
              )
              currentOnCropChange(updated)
            }
          },
          onDragEnd = { activeHandle = null },
          onDragCancel = { activeHandle = null },
        )
      },
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      val left = cropRect.left * w
      val top = cropRect.top * h
      val right = cropRect.right * w
      val bottom = cropRect.bottom * h

      // Darkened scrim around crop area
      val scrim = Color.Black.copy(alpha = 0.55f)
      drawRect(scrim, Offset.Zero, Size(w, top))
      drawRect(scrim, Offset(0f, bottom), Size(w, h - bottom))
      drawRect(scrim, Offset(0f, top), Size(left, bottom - top))
      drawRect(scrim, Offset(right, top), Size(w - right, bottom - top))

      // Crop rectangle border (highlighted if active)
      val isBorderActive = activeHandle != null
      drawRect(
        color = if (isBorderActive) primaryColor else primaryColor.copy(alpha = 0.85f),
        topLeft = Offset(left, top),
        size = Size(right - left, bottom - top),
        style = Stroke(width = if (isBorderActive) 3.5.dp.toPx() else 2.5.dp.toPx()),
      )

      // Rule of thirds grid lines
      val stepX = (right - left) / 3f
      val stepY = (bottom - top) / 3f
      val gridColor = Color.White.copy(alpha = if (isBorderActive) 0.65f else 0.4f)
      val gridStroke = Stroke(width = 1.dp.toPx())

      drawLine(gridColor, Offset(left + stepX, top), Offset(left + stepX, bottom), gridStroke.width)
      drawLine(gridColor, Offset(left + 2 * stepX, top), Offset(left + 2 * stepX, bottom), gridStroke.width)
      drawLine(gridColor, Offset(left, top + stepY), Offset(right, top + stepY), gridStroke.width)
      drawLine(gridColor, Offset(left, top + 2 * stepY), Offset(right, top + 2 * stepY), gridStroke.width)

      // Corner L-brackets
      val handleLen = 24.dp.toPx()
      val cornerStrokeWidth = 4.5.dp.toPx()

      fun drawCorner(x: Float, y: Float, dx: Float, dy: Float, isHighlighted: Boolean) {
        val color = if (isHighlighted) Color.White else primaryColor
        val stroke = if (isHighlighted) cornerStrokeWidth + 1.5.dp.toPx() else cornerStrokeWidth
        val path = Path().apply {
          moveTo(x + dx, y)
          lineTo(x, y)
          lineTo(x, y + dy)
        }
        drawPath(
          path = path,
          color = color,
          style = Stroke(
            width = stroke,
            cap = StrokeCap.Round,
            join = StrokeJoin.Miter,
          ),
        )
      }

      drawCorner(left, top, handleLen, handleLen, activeHandle == CropHandle.TOP_LEFT)
      drawCorner(right, top, -handleLen, handleLen, activeHandle == CropHandle.TOP_RIGHT)
      drawCorner(left, bottom, handleLen, -handleLen, activeHandle == CropHandle.BOTTOM_LEFT)
      drawCorner(right, bottom, -handleLen, -handleLen, activeHandle == CropHandle.BOTTOM_RIGHT)

      // Edge grab handles (pills) for Top, Bottom, Left, Right
      val pillLength = 36.dp.toPx()
      val pillThickness = 5.5.dp.toPx()

      fun drawEdgePill(
        centerX: Float,
        centerY: Float,
        isVertical: Boolean,
        isHighlighted: Boolean,
      ) {
        val pillColor = if (isHighlighted) Color.White else primaryColor
        val length = if (isHighlighted) pillLength + 8.dp.toPx() else pillLength
        val thickness = if (isHighlighted) pillThickness + 2.dp.toPx() else pillThickness
        val radius = CornerRadius(thickness / 2f, thickness / 2f)

        val pillOffset = if (isVertical) {
          Offset(centerX - thickness / 2f, centerY - length / 2f)
        } else {
          Offset(centerX - length / 2f, centerY - thickness / 2f)
        }
        val pillSize = if (isVertical) Size(thickness, length) else Size(length, thickness)

        drawRoundRect(
          color = Color.Black.copy(alpha = 0.4f),
          topLeft = pillOffset + Offset(0.5.dp.toPx(), 0.5.dp.toPx()),
          size = pillSize,
          cornerRadius = radius,
        )
        drawRoundRect(
          color = pillColor,
          topLeft = pillOffset,
          size = pillSize,
          cornerRadius = radius,
        )
      }

      drawEdgePill((left + right) / 2f, top, isVertical = false, isHighlighted = activeHandle == CropHandle.TOP)
      drawEdgePill((left + right) / 2f, bottom, isVertical = false, isHighlighted = activeHandle == CropHandle.BOTTOM)
      drawEdgePill(left, (top + bottom) / 2f, isVertical = true, isHighlighted = activeHandle == CropHandle.LEFT)
      drawEdgePill(right, (top + bottom) / 2f, isVertical = true, isHighlighted = activeHandle == CropHandle.RIGHT)
    }
  }
}

@Composable
fun CropControls(
  selectedAspect: AspectRatioPreset,
  isVertical: Boolean = false,
  onAspectSelected: (AspectRatioPreset) -> Unit,
  onSwapAspectRatio: () -> Unit = {},
  onApplyCrop: () -> Unit,
  onResetCrop: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      AspectRatioPreset.entries.forEach { preset ->
        FilterChip(
          selected = selectedAspect == preset,
          onClick = { onAspectSelected(preset) },
          label = { Text(preset.label, style = MaterialTheme.typography.bodySmall) },
          shape = CircleShape,
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      FilledTonalIconToggleButton(
        checked = isVertical,
        onCheckedChange = { onSwapAspectRatio() },
        modifier = Modifier
          .size(40.dp)
          .testTag("crop_aspect_swap_toggle"),
      ) {
        Icon(
          imageVector = if (isVertical) Icons.Default.CropPortrait else Icons.Default.CropLandscape,
          contentDescription = if (isVertical) "Swap to horizontal aspect ratio" else "Swap to vertical aspect ratio",
          modifier = Modifier.size(20.dp),
        )
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        OutlinedButton(
          onClick = onResetCrop,
          shape = CircleShape,
          modifier = Modifier.testTag("crop_reset_button"),
        ) {
          Text("Reset")
        }
        Button(
          onClick = onApplyCrop,
          shape = CircleShape,
          modifier = Modifier.testTag("crop_apply_button"),
        ) {
          Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Apply Crop")
        }
      }
    }
  }
}

// -------------------------------------------------------------
// Slim Dot Slider
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlimDotSlider(
  value: Float,
  onValueChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
  onValueChangeFinished: (() -> Unit)? = null,
  valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
  steps: Int = 0,
  enabled: Boolean = true,
  accentColor: Color = MaterialTheme.colorScheme.primary,
) {
  val sliderColors = SliderDefaults.colors(
    thumbColor = accentColor,
    activeTrackColor = accentColor,
    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
  )
  Slider(
    value = value,
    onValueChange = onValueChange,
    onValueChangeFinished = onValueChangeFinished,
    modifier = modifier,
    valueRange = valueRange,
    steps = steps,
    enabled = enabled,
    colors = sliderColors,
    thumb = {
      Box(
        modifier = Modifier
          .size(16.dp)
          .shadow(2.dp, CircleShape)
          .background(accentColor, CircleShape),
      )
    },
    track = { sliderState ->
      SliderDefaults.Track(
        sliderState = sliderState,
        modifier = Modifier.height(4.dp),
        colors = sliderColors,
      )
    },
  )
}

// -------------------------------------------------------------
// Rotate Controls
// -------------------------------------------------------------

@Composable
fun RotateControls(
  degrees: Float,
  onDegreesChange: (Float) -> Unit,
  onRotateBy: (Float) -> Unit,
  onToggleFlipH: () -> Unit,
  onToggleFlipV: () -> Unit,
  onApplyRotation: () -> Unit,
  onResetRotation: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      FilledTonalIconButton(
        onClick = { onRotateBy(-90f) },
        modifier = Modifier.testTag("rotate_ccw_button"),
      ) {
        Icon(Icons.AutoMirrored.Filled.RotateLeft, contentDescription = "Rotate 90 CCW")
      }
      FilledTonalIconButton(
        onClick = { onRotateBy(90f) },
        modifier = Modifier.testTag("rotate_cw_button"),
      ) {
        Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate 90 CW")
      }
      FilledTonalIconButton(
        onClick = { onRotateBy(180f) },
        modifier = Modifier.testTag("rotate_180_button"),
      ) {
        Icon(Icons.Default.Refresh, contentDescription = "Rotate 180")
      }
      FilledTonalIconButton(
        onClick = onToggleFlipH,
        modifier = Modifier.testTag("flip_h_button"),
      ) {
        Icon(Icons.Default.Flip, contentDescription = "Flip Horizontally")
      }
      FilledTonalIconButton(
        onClick = onToggleFlipV,
        modifier = Modifier.testTag("flip_v_button"),
      ) {
        Icon(Icons.Default.SwapVert, contentDescription = "Flip Vertically")
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "${degrees.roundToInt()}°",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.width(48.dp),
      )
      SlimDotSlider(
        value = degrees,
        onValueChange = onDegreesChange,
        valueRange = -180f..180f,
        modifier = Modifier
          .weight(1f)
          .testTag("rotate_slider"),
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.End,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      OutlinedButton(onClick = onResetRotation) {
        Text("Reset")
      }
      Spacer(modifier = Modifier.width(8.dp))
      Button(
        onClick = onApplyRotation,
        modifier = Modifier.testTag("rotate_apply_button"),
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Apply Rotation")
      }
    }
  }
}

// -------------------------------------------------------------
// Tilt Controls & Grid Overlay
// -------------------------------------------------------------

@Composable
fun TiltAlignmentGridOverlay(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier.fillMaxSize()) {
    val stepX = size.width / 4f
    val stepY = size.height / 4f
    val gridColor = Color.White.copy(alpha = 0.3f)
    for (i in 1..3) {
      drawLine(gridColor, Offset(stepX * i, 0f), Offset(stepX * i, size.height), 1.dp.toPx())
      drawLine(gridColor, Offset(0f, stepY * i), Offset(size.width, stepY * i), 1.dp.toPx())
    }
  }
}

@Composable
fun TiltControls(
  tiltAngle: Float,
  horizontalTilt: Float,
  verticalTilt: Float,
  autoCropBorders: Boolean,
  onTiltAngleChange: (Float) -> Unit,
  onHorizontalTiltChange: (Float) -> Unit,
  onVerticalTiltChange: (Float) -> Unit,
  onAutoCropBordersChange: (Boolean) -> Unit,
  onApplyTilt: () -> Unit,
  onResetTilt: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current

  Column(modifier = modifier.fillMaxWidth()) {
    // 1. Straighten Angle
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Angle: ${"%.1f".format(tiltAngle)}°",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.width(96.dp),
      )
      SlimDotSlider(
        value = tiltAngle,
        onValueChange = onTiltAngleChange,
        valueRange = -45f..45f,
        modifier = Modifier
          .weight(1f)
          .testTag("tilt_angle_slider"),
      )
      if (tiltAngle != 0f) {
        IconButton(
          onClick = {
            onTiltAngleChange(0f)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          },
          modifier = Modifier.size(28.dp),
        ) {
          Icon(Icons.Default.Close, contentDescription = "Zero angle", modifier = Modifier.size(16.dp))
        }
      }
    }

    // 2. Horizontal Perspective Tilt
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "H-Tilt: ${"%.1f".format(horizontalTilt)}°",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.width(96.dp),
      )
      SlimDotSlider(
        value = horizontalTilt,
        onValueChange = onHorizontalTiltChange,
        valueRange = -30f..30f,
        modifier = Modifier
          .weight(1f)
          .testTag("tilt_skew_x_slider"),
      )
      if (horizontalTilt != 0f) {
        IconButton(
          onClick = {
            onHorizontalTiltChange(0f)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          },
          modifier = Modifier.size(28.dp),
        ) {
          Icon(Icons.Default.Close, contentDescription = "Zero horizontal tilt", modifier = Modifier.size(16.dp))
        }
      }
    }

    // 3. Vertical Perspective Tilt
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "V-Tilt: ${"%.1f".format(verticalTilt)}°",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.width(96.dp),
      )
      SlimDotSlider(
        value = verticalTilt,
        onValueChange = onVerticalTiltChange,
        valueRange = -30f..30f,
        modifier = Modifier
          .weight(1f)
          .testTag("tilt_skew_y_slider"),
      )
      if (verticalTilt != 0f) {
        IconButton(
          onClick = {
            onVerticalTiltChange(0f)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          },
          modifier = Modifier.size(28.dp),
        ) {
          Icon(Icons.Default.Close, contentDescription = "Zero vertical tilt", modifier = Modifier.size(16.dp))
        }
      }
    }

    // 4. Auto-crop toggle and Action Buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onAutoCropBordersChange(!autoCropBorders) },
      ) {
        Checkbox(
          checked = autoCropBorders,
          onCheckedChange = onAutoCropBordersChange,
          modifier = Modifier.size(32.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("Auto-crop borders", style = MaterialTheme.typography.bodySmall)
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = onResetTilt) {
          Text("Reset")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = onApplyTilt,
          modifier = Modifier.testTag("tilt_apply_button"),
        ) {
          Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Apply Tilt")
        }
      }
    }
  }
}

// -------------------------------------------------------------
// Color Adjustments Controls
// -------------------------------------------------------------

@Composable
fun AdjustControls(
  adjustments: ColorAdjustments,
  onAdjustmentsChange: (ColorAdjustments) -> Unit,
  onApplyAdjustments: () -> Unit,
  onResetAdjustments: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    // Quick filter presets
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      FilterPreset.entries.forEach { preset ->
        FilterChip(
          selected = adjustments.filterPreset == preset,
          onClick = { onAdjustmentsChange(adjustments.copy(filterPreset = preset)) },
          label = { Text(preset.label, style = MaterialTheme.typography.bodySmall) },
          shape = CircleShape,
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Brightness
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = "Brightness: ${adjustments.brightness.roundToInt()}",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.width(110.dp),
      )
      SlimDotSlider(
        value = adjustments.brightness,
        onValueChange = { onAdjustmentsChange(adjustments.copy(brightness = it)) },
        valueRange = -80f..80f,
        modifier = Modifier.weight(1f),
      )
    }

    // Contrast
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = "Contrast: ${"%.2f".format(adjustments.contrast)}",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.width(110.dp),
      )
      SlimDotSlider(
        value = adjustments.contrast,
        onValueChange = { onAdjustmentsChange(adjustments.copy(contrast = it)) },
        valueRange = 0.5f..1.5f,
        modifier = Modifier.weight(1f),
      )
    }

    // Saturation
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = "Saturation: ${"%.2f".format(adjustments.saturation)}",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.width(110.dp),
      )
      SlimDotSlider(
        value = adjustments.saturation,
        onValueChange = { onAdjustmentsChange(adjustments.copy(saturation = it)) },
        valueRange = 0f..2f,
        modifier = Modifier.weight(1f),
      )
    }

    // Actions
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.End,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      OutlinedButton(
        onClick = onResetAdjustments,
        shape = CircleShape,
      ) {
        Text("Reset")
      }
      Spacer(modifier = Modifier.width(8.dp))
      Button(
        onClick = onApplyAdjustments,
        shape = CircleShape,
        modifier = Modifier.testTag("adjust_apply_button"),
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Apply Filter")
      }
    }
  }
}

// -------------------------------------------------------------
// Drawing Canvas Overlay & Controls
// -------------------------------------------------------------

@Composable
fun DrawingCanvasOverlay(
  strokes: MutableList<ImageDrawingStroke>,
  textAnnotations: MutableList<ImageTextAnnotation>,
  currentColor: Color,
  currentStrokeWidth: Float,
  isEraser: Boolean,
  bitmapWidth: Int,
  bitmapHeight: Int,
  modifier: Modifier = Modifier,
) {
  var inProgressPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
  var draggingAnnotationIndex by remember { mutableStateOf<Int?>(null) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .pointerInput(bitmapWidth, bitmapHeight, currentColor, currentStrokeWidth, isEraser) {
        detectDragGestures(
          onDragStart = { offset ->
            if (size.width > 0 && size.height > 0) {
              val normX = offset.x / size.width
              val normY = offset.y / size.height

              // Check if user is grabbing an existing text annotation
              val touchedTextIdx = textAnnotations.indexOfLast { ann ->
                kotlin.math.abs(ann.xRatio - normX) < 0.12f && kotlin.math.abs(ann.yRatio - normY) < 0.08f
              }

              if (touchedTextIdx != -1) {
                draggingAnnotationIndex = touchedTextIdx
              } else {
                val bmX = normX * bitmapWidth
                val bmY = normY * bitmapHeight
                inProgressPoints = listOf(Offset(bmX, bmY))
              }
            }
          },
          onDrag = { change, _ ->
            change.consume()
            if (size.width > 0 && size.height > 0) {
              val normX = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
              val normY = (change.position.y / size.height).coerceIn(0.05f, 0.95f)

              val annIdx = draggingAnnotationIndex
              if (annIdx != null && annIdx in textAnnotations.indices) {
                val current = textAnnotations[annIdx]
                textAnnotations[annIdx] = current.copy(xRatio = normX, yRatio = normY)
              } else {
                val bmX = normX * bitmapWidth
                val bmY = normY * bitmapHeight
                inProgressPoints = inProgressPoints + Offset(bmX, bmY)
              }
            }
          },
          onDragEnd = {
            draggingAnnotationIndex = null
            if (inProgressPoints.isNotEmpty()) {
              strokes.add(
                ImageDrawingStroke(
                  points = inProgressPoints,
                  color = currentColor,
                  strokeWidth = currentStrokeWidth,
                  isEraser = isEraser,
                ),
              )
              inProgressPoints = emptyList()
            }
          },
          onDragCancel = {
            draggingAnnotationIndex = null
            inProgressPoints = emptyList()
          },
        )
      },
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      if (bitmapWidth <= 0 || bitmapHeight <= 0) return@Canvas
      val scaleX = size.width / bitmapWidth.toFloat()
      val scaleY = size.height / bitmapHeight.toFloat()

      fun drawStrokeList(strokeList: List<ImageDrawingStroke>) {
        strokeList.forEach { stroke ->
          if (stroke.points.size < 2) {
            stroke.points.firstOrNull()?.let { p ->
              drawCircle(
                color = if (stroke.isEraser) Color.Black.copy(alpha = 0.5f) else stroke.color,
                radius = (stroke.strokeWidth * scaleX) / 2f,
                center = Offset(p.x * scaleX, p.y * scaleY),
              )
            }
            return@forEach
          }

          val path = Path()
          path.moveTo(stroke.points[0].x * scaleX, stroke.points[0].y * scaleY)
          for (i in 1 until stroke.points.size) {
            val curr = stroke.points[i]
            path.lineTo(curr.x * scaleX, curr.y * scaleY)
          }

          drawPath(
            path = path,
            color = if (stroke.isEraser) Color.White.copy(alpha = 0.5f) else stroke.color,
            style = Stroke(
              width = stroke.strokeWidth * scaleX,
              cap = StrokeCap.Round,
              join = androidx.compose.ui.graphics.StrokeJoin.Round,
            ),
          )
        }
      }

      drawStrokeList(strokes)
      if (inProgressPoints.isNotEmpty()) {
        drawStrokeList(
          listOf(
            ImageDrawingStroke(
              points = inProgressPoints,
              color = currentColor,
              strokeWidth = currentStrokeWidth,
              isEraser = isEraser,
            ),
          ),
        )
      }

      // Draw text annotations on canvas
      textAnnotations.forEach { ann ->
        val textPaint = android.graphics.Paint().apply {
          isAntiAlias = true
          color = ann.color.toArgb()
          textSize = ann.fontSizeSp * (size.height / 600f)
          textAlign = android.graphics.Paint.Align.CENTER
          isFakeBoldText = true
        }

        val posX = ann.xRatio * size.width
        val posY = ann.yRatio * size.height

        if (ann.hasBackground) {
          val bounds = android.graphics.Rect()
          textPaint.getTextBounds(ann.text, 0, ann.text.length, bounds)
          val pad = 12f * (size.height / 600f)
          val bgPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.argb(160, 0, 0, 0)
          }
          val bgRect = RectF(
            posX - bounds.width() / 2f - pad,
            posY - bounds.height() - pad,
            posX + bounds.width() / 2f + pad,
            posY + pad,
          )
          drawContext.canvas.nativeCanvas.drawRoundRect(bgRect, pad, pad, bgPaint)
        }

        drawContext.canvas.nativeCanvas.drawText(ann.text, posX, posY, textPaint)
      }
    }
  }
}

@Composable
fun DrawControls(
  strokesCount: Int,
  textCount: Int,
  currentColor: Color,
  currentStrokeWidth: Float,
  isEraser: Boolean,
  onColorSelected: (Color) -> Unit,
  onStrokeWidthSelected: (Float) -> Unit,
  onToggleEraser: () -> Unit,
  onAddTextClicked: () -> Unit,
  onUndoStroke: () -> Unit,
  onClearStrokes: () -> Unit,
  onApplyDrawing: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = listOf(
    Color.Red,
    Color(0xFFFF9800),
    Color(0xFFFFEB3B),
    Color(0xFF4CAF50),
    Color(0xFF00BCD4),
    Color(0xFF2196F3),
    Color(0xFF9C27B0),
    Color.Black,
    Color.White,
  )

  Column(modifier = modifier.fillMaxWidth()) {
    // Bar 1: Color selection
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      colors.forEach { color ->
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color)
            .border(
              width = if (currentColor == color && !isEraser) 3.dp else 1.dp,
              color = if (currentColor == color && !isEraser) {
                MaterialTheme.colorScheme.primary
              } else {
                Color.Gray.copy(alpha = 0.5f)
              },
              shape = CircleShape,
            )
            .clickable { onColorSelected(color) },
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Bar 2: Line width & tool selection
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      val strokeOptions = listOf(4f to "Thin", 12f to "Med", 24f to "Thick")
      val currentOption = strokeOptions.firstOrNull { it.first == currentStrokeWidth } ?: strokeOptions[1]

      FilterChip(
        selected = !isEraser,
        onClick = {
          val currentIndex = strokeOptions.indexOfFirst { it.first == currentStrokeWidth }
          val nextIndex = if (currentIndex == -1) 0 else (currentIndex + 1) % strokeOptions.size
          onStrokeWidthSelected(strokeOptions[nextIndex].first)
        },
        label = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier.size(12.dp),
              contentAlignment = Alignment.Center,
            ) {
              Box(
                modifier = Modifier
                  .size(
                    when (currentOption.first) {
                      4f -> 4.dp
                      24f -> 10.dp
                      else -> 7.dp
                    },
                  )
                  .background(
                    color = if (!isEraser) {
                      MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                      MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    shape = CircleShape,
                  ),
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier.width(36.dp),
              contentAlignment = Alignment.CenterStart,
            ) {
              Text(
                currentOption.second,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
              )
            }
          }
        },
        shape = CircleShape,
        modifier = Modifier.testTag("draw_stroke_width_toggle"),
      )

      FilterChip(
        selected = isEraser,
        onClick = onToggleEraser,
        label = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              painter = painterResource(id = R.drawable.ic_eraser),
              contentDescription = null,
              modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Eraser", style = MaterialTheme.typography.labelSmall)
          }
        },
        shape = CircleShape,
      )

      FilterChip(
        selected = false,
        onClick = onAddTextClicked,
        label = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Text", style = MaterialTheme.typography.labelSmall)
          }
        },
        shape = CircleShape,
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row {
        IconButton(
          onClick = onUndoStroke,
          enabled = strokesCount > 0 || textCount > 0,
        ) {
          Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo stroke or text")
        }
        IconButton(
          onClick = onClearStrokes,
          enabled = strokesCount > 0 || textCount > 0,
        ) {
          Icon(Icons.Default.Clear, contentDescription = "Clear all strokes and text")
        }
      }

      Button(
        onClick = onApplyDrawing,
        enabled = strokesCount > 0 || textCount > 0,
        shape = CircleShape,
        modifier = Modifier.testTag("draw_apply_button"),
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Apply Drawing")
      }
    }
  }
}

// -------------------------------------------------------------
// Image Loading, Saving & Sharing Helpers
// -------------------------------------------------------------

fun decodeBitmapFromFile(file: File, maxDimension: Int = 2048): Bitmap? {
  if (!file.exists() || !file.isFile) return null
  return try {
    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, boundsOptions)

    val sampleSize = calculateInSampleSize(
      boundsOptions.outWidth,
      boundsOptions.outHeight,
      maxDimension,
    )

    val decodeOptions = BitmapFactory.Options().apply {
      inSampleSize = sampleSize
      inPreferredConfig = Bitmap.Config.ARGB_8888
    }

    val original = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return null

    var orientation = ExifInterface.ORIENTATION_NORMAL
    try {
      val exif = ExifInterface(file.absolutePath)
      orientation = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL,
      )
    } catch (_: Exception) {}

    val rotation = when (orientation) {
      ExifInterface.ORIENTATION_ROTATE_90 -> 90f
      ExifInterface.ORIENTATION_ROTATE_180 -> 180f
      ExifInterface.ORIENTATION_ROTATE_270 -> 270f
      else -> 0f
    }

    if (rotation != 0f) {
      val matrix = Matrix().apply { postRotate(rotation) }
      Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
    } else {
      original
    }
  } catch (_: Exception) {
    null
  }
}

fun decodeBitmapFromUri(context: Context, uri: Uri, maxDimension: Int = 2048): Bitmap? {
  return try {
    // 1. Safe downsampling: read dimensions first
    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { stream ->
      BitmapFactory.decodeStream(stream, null, boundsOptions)
    }

    val sampleSize = calculateInSampleSize(
      boundsOptions.outWidth,
      boundsOptions.outHeight,
      maxDimension,
    )

    // 2. Decode with sample size to prevent OOM
    val decodeOptions = BitmapFactory.Options().apply {
      inSampleSize = sampleSize
      inPreferredConfig = Bitmap.Config.ARGB_8888
    }

    val original = context.contentResolver.openInputStream(uri)?.use { stream ->
      BitmapFactory.decodeStream(stream, null, decodeOptions)
    } ?: return null

    // 3. Determine and correct orientation from EXIF
    var orientation = ExifInterface.ORIENTATION_NORMAL
    try {
      context.contentResolver.openInputStream(uri)?.use { stream ->
        val exif = ExifInterface(stream)
        orientation = exif.getAttributeInt(
          ExifInterface.TAG_ORIENTATION,
          ExifInterface.ORIENTATION_NORMAL,
        )
      }
    } catch (_: Exception) {}

    val rotation = when (orientation) {
      ExifInterface.ORIENTATION_ROTATE_90 -> 90f
      ExifInterface.ORIENTATION_ROTATE_180 -> 180f
      ExifInterface.ORIENTATION_ROTATE_270 -> 270f
      else -> 0f
    }

    if (rotation != 0f) {
      val matrix = Matrix().apply { postRotate(rotation) }
      Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
    } else {
      original
    }
  } catch (e: Exception) {
    e.printStackTrace()
    null
  }
}

fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Uri? {
  val filename = "Toolbox_${System.currentTimeMillis()}.png"
  val values = ContentValues().apply {
    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      put(MediaStore.Images.Media.IS_PENDING, 1)
      put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Toolbox")
    }
  }

  val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
  if (uri != null) {
    try {
      context.contentResolver.openOutputStream(uri)?.use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        context.contentResolver.update(uri, values, null, null)
      }
      return uri
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
  return null
}

fun shareBitmap(context: Context, bitmap: Bitmap) {
  try {
    val cachePath = File(context.cacheDir, "images")
    cachePath.mkdirs()
    val file = File(cachePath, "shared_image.png")
    FileOutputStream(file).use { out ->
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    val contentUri = FileProvider.getUriForFile(
      context,
      "${context.packageName}.fileprovider",
      file,
    )
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
      type = "image/png"
      putExtra(Intent.EXTRA_STREAM, contentUri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
  } catch (e: Exception) {
    e.printStackTrace()
    Toast.makeText(context, "Could not share image", Toast.LENGTH_SHORT).show()
  }
}

@Preview(showBackground = true)
@Composable
fun OpenFileInitialViewPreview() {
  ToolboxTheme {
    OpenFileInitialView(
      onOpenFile = {},
      onUseSample = {},
    )
  }
}

@Preview(showBackground = true)
@Composable
fun CropControlsPreview() {
  ToolboxTheme {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ) {
      Box(modifier = Modifier.padding(8.dp)) {
        CropControls(
          selectedAspect = AspectRatioPreset.RATIO_4_3,
          isVertical = false,
          onAspectSelected = {},
          onSwapAspectRatio = {},
          onApplyCrop = {},
          onResetCrop = {},
        )
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun CropControlsVerticalPreview() {
  ToolboxTheme {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ) {
      Box(modifier = Modifier.padding(8.dp)) {
        CropControls(
          selectedAspect = AspectRatioPreset.RATIO_3_4,
          isVertical = true,
          onAspectSelected = {},
          onSwapAspectRatio = {},
          onApplyCrop = {},
          onResetCrop = {},
        )
      }
    }
  }
}
