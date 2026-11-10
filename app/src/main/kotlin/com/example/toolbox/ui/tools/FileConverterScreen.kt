package com.example.toolbox.ui.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.toolbox.ui.theme.ToolboxTheme
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FileConverterScreen(
  modifier: Modifier = Modifier,
  initialFile: File? = null,
  initialDirectory: File? = null,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current
  val coroutineScope = rememberCoroutineScope()

  // Selected source file & detected format
  var selectedFile by remember { mutableStateOf(initialFile) }
  var sourceFormat by remember(selectedFile) {
    mutableStateOf(FileConverterEngine.detectFormat(selectedFile))
  }

  // Available routes for the selected source format
  val availableRoutes = remember(sourceFormat) {
    sourceFormat?.let { FileConverterEngine.getAvailableRoutes(it) } ?: emptyList()
  }

  // Category filter when selecting target format
  val availableCategories = remember(availableRoutes) {
    availableRoutes.map { it.target.category }.distinct()
  }
  var selectedCategoryFilter by remember(sourceFormat, availableCategories) {
    mutableStateOf(
      sourceFormat?.category?.takeIf { it in availableCategories }
        ?: availableCategories.firstOrNull()
    )
  }

  val filteredRoutes = remember(availableRoutes, selectedCategoryFilter) {
    if (selectedCategoryFilter == null) {
      availableRoutes
    } else {
      availableRoutes.filter { it.target.category == selectedCategoryFilter }
    }
  }

  // Selected target format
  var selectedTargetFormat by remember(sourceFormat) {
    mutableStateOf(filteredRoutes.firstOrNull()?.target ?: availableRoutes.firstOrNull()?.target)
  }

  LaunchedEffect(filteredRoutes) {
    if (selectedTargetFormat == null || availableRoutes.none { it.target == selectedTargetFormat }) {
      selectedTargetFormat = filteredRoutes.firstOrNull()?.target ?: availableRoutes.firstOrNull()?.target
    }
  }

  val activeRoute = remember(sourceFormat, selectedTargetFormat) {
    val src = sourceFormat
    val dst = selectedTargetFormat
    if (src != null && dst != null) FileConverterEngine.getRoute(src, dst) else null
  }

  val fallbackConvertedDir = remember(context, isInspection, initialDirectory) {
    initialDirectory ?: if (isInspection) {
      File("/storage/emulated/0/Converted")
    } else {
      FileConverterEngine.getDefaultConvertedDirectory(context)
    }
  }

  // Conversion execution state
  var isConverting by remember { mutableStateOf(false) }
  var lastResult by remember { mutableStateOf<ConversionResult?>(null) }
  val recentConversions = remember { mutableStateListOf<ConvertedFileHistoryItem>() }

  // Dialog states
  var showFileBrowserDialog by remember { mutableStateOf(false) }
  var showCapabilityMapDialog by remember { mutableStateOf(false) }

  // System SAF document picker (accessible inside the file picker dialog)
  val documentPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri: Uri? ->
    if (uri != null) {
      val imported = copyUriToTempSourceFile(context, uri)
      if (imported != null) {
        selectedFile = imported
        sourceFormat = FileConverterEngine.detectFormat(imported)
        lastResult = null
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
      } else {
        Toast.makeText(context, "Could not read selected file", Toast.LENGTH_SHORT).show()
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("file_converter_screen"),
  ) {
    // Top Toolbar Header (matching DESIGN_LANGUAGE.md Section 5.1)
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
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "File Converter",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = if (selectedFile != null && sourceFormat != null) {
                  "${sourceFormat!!.displayName} • ${availableRoutes.size} target formats"
                } else {
                  "Offline Native & FFmpeg converter"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = { showCapabilityMapDialog = true },
              modifier = Modifier.testTag("open_capability_map_button"),
            ) {
              Icon(
                imageVector = Icons.Default.Hub,
                contentDescription = "Supported Conversions Map",
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }
      }
    }

    val currentFile = selectedFile
    if (currentFile == null) {
      // ======================================================================
      // EMPTY STATE: Clean centered hero file selector card
      // ======================================================================
      Box(
        modifier = Modifier
          .fillMaxSize()
          .weight(1f)
          .padding(24.dp),
        contentAlignment = Alignment.Center,
      ) {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { showFileBrowserDialog = true }
            .testTag("browse_storage_files_button"),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.UploadFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp),
              )
            }

            Text(
              text = "Select a File to Convert",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center,
            )

            Text(
              text = "Tap to browse images, audio, video, documents, or archives",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
            )
          }
        }
      }
    } else {
      // ======================================================================
      // ACTIVE STATE: Selected File Card + Target Format Grid + Convert Action
      // ======================================================================
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .weight(1f),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // 1. Unified Selected File Card (tapping the card changes the file)
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .clickable { showFileBrowserDialog = true }
              .testTag("selected_source_file_card"),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = categoryIcon(sourceFormat?.category),
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.size(22.dp),
                )
              }

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = currentFile.name,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                val sizeText = FileManagerEngine.formatFileSize(currentFile.length())
                val formatLabel = sourceFormat?.displayName ?: "Unsupported format"
                Text(
                  text = "$formatLabel • $sizeText • Tap to change",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }

              IconButton(
                onClick = {
                  selectedFile = null
                  sourceFormat = null
                  selectedTargetFormat = null
                  lastResult = null
                },
                modifier = Modifier.testTag("clear_selected_file_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Clear selected file",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }

          if (sourceFormat == null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.errorContainer,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.ErrorOutline,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onErrorContainer,
                  modifier = Modifier.size(18.dp),
                )
                Text(
                  text = "Unrecognized file extension. Tap the card above to pick another file.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onErrorContainer,
                )
              }
            }
          }
        }

        // 2. Target Format Selection (Clean format pills without redundant engine sub-badges)
        if (sourceFormat != null) {
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              ),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("converter_step_2_card"),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Text(
                  text = "Convert to",
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                )

                // Category filter chips when cross-category conversions exist (e.g., Video -> Video / Audio / Image)
                if (availableCategories.size > 1) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    for (category in availableCategories) {
                      val isSelected = selectedCategoryFilter == category
                      FilterChip(
                        selected = isSelected,
                        onClick = {
                          selectedCategoryFilter = category
                          val firstInCat = availableRoutes.firstOrNull { it.target.category == category }?.target
                          if (firstInCat != null) {
                            selectedTargetFormat = firstInCat
                          }
                        },
                        label = { Text(category.displayName) },
                        leadingIcon = {
                          Icon(
                            imageVector = categoryIcon(category),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                          )
                        },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("target_category_chip_${category.name}"),
                      )
                    }
                  }
                }

                // Clean uniform format chips showing only the format name
                FlowRow(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  for (route in filteredRoutes) {
                    val target = route.target
                    val isSelected = selectedTargetFormat == target

                    Surface(
                      shape = RoundedCornerShape(12.dp),
                      color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                      } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                      },
                      border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) {
                          MaterialTheme.colorScheme.primary
                        } else {
                          MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        },
                      ),
                      modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                          selectedTargetFormat = target
                          lastResult = null
                          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        .testTag("target_format_option_${target.name}"),
                    ) {
                      Text(
                        text = target.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) {
                          MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                          MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                      )
                    }
                  }
                }
              }
            }
          }

          // 3. Convert Action + Automatic Output Path Summary
          item {
            val dstFormat = selectedTargetFormat
            val stem = FileConverterEngine.extractFileStem(currentFile.name)
            val targetDir = currentFile.parentFile?.takeIf { it.exists() && it.canWrite() } ?: fallbackConvertedDir
            val previewOutputName = if (dstFormat != null) "$stem.${dstFormat.extension}" else currentFile.name

            Column(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("converter_step_3_card"),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              // Subtle one-line summary showing default save filename, folder, and active engine
              if (activeRoute != null && dstFormat != null) {
                val engineSummary = buildString {
                  append("Engine: ${activeRoute.preferredBackend.displayName}")
                  if (activeRoute.fallbackBackend != null) {
                    append(" (${activeRoute.fallbackBackend.displayName} backup)")
                  }
                }
                Text(
                  text = "Saves as $previewOutputName in ${targetDir.name.ifEmpty { "/" }} • $engineSummary",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .testTag("active_route_info_card"),
                )
              }

              if (isConverting) {
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  LinearProgressIndicator(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(8.dp)),
                  )
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    CircularProgressIndicator(
                      modifier = Modifier.size(16.dp),
                      strokeWidth = 2.dp,
                    )
                    Text(
                      text = "Converting to ${dstFormat?.displayName ?: ""}...",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
              }

              Button(
                onClick = {
                  val targetFmt = selectedTargetFormat ?: return@Button
                  isConverting = true
                  lastResult = null
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                  coroutineScope.launch {
                    val result = FileConverterEngine.convertFile(
                      context = context,
                      sourceFile = currentFile,
                      targetFormat = targetFmt,
                      outputDir = targetDir,
                      desiredStem = stem,
                      sourceFormatOverride = sourceFormat,
                    )
                    isConverting = false
                    lastResult = result
                    val outFile = result.outputFile
                    if (result.success && outFile != null && outFile.exists()) {
                      recentConversions.add(
                        0,
                        ConvertedFileHistoryItem(
                          sourceName = currentFile.name,
                          outputFile = outFile,
                          sourceFormat = result.sourceFormat,
                          targetFormat = result.targetFormat,
                          backendUsed = result.backendUsed,
                          usedFallback = result.usedFallback,
                          sizeBytes = outFile.length(),
                          durationMs = result.durationMs,
                          timestampMs = System.currentTimeMillis(),
                        )
                      )
                    }
                  }
                },
                enabled = dstFormat != null && !isConverting,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(50.dp)
                  .testTag("convert_and_save_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (dstFormat != null) {
                    "Convert to ${dstFormat.displayName} & Save"
                  } else {
                    "Convert & Save"
                  },
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Bold,
                )
              }

              // Result Feedback Banner
              AnimatedVisibility(
                visible = lastResult != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
              ) {
                val res = lastResult
                if (res != null) {
                  Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (res.success) {
                      MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
                    } else {
                      MaterialTheme.colorScheme.errorContainer
                    },
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("conversion_result_banner"),
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                      Icon(
                        imageVector = if (res.success) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = if (res.success) {
                          MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                          MaterialTheme.colorScheme.onErrorContainer
                        },
                        modifier = Modifier.size(24.dp),
                      )
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = res.message,
                          style = MaterialTheme.typography.bodyMedium,
                          fontWeight = FontWeight.SemiBold,
                          color = if (res.success) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                          } else {
                            MaterialTheme.colorScheme.onErrorContainer
                          },
                        )
                        val out = res.outputFile
                        if (res.success && out != null) {
                          Text(
                            text = "${out.name} • ${FileManagerEngine.formatFileSize(out.length())} • ${res.durationMs} ms",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                          )
                          Text(
                            text = out.absolutePath,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                          )
                        }
                      }
                      if (res.success && res.outputFile != null) {
                        IconButton(
                          onClick = { shareConvertedFile(context, res.outputFile, res.targetFormat.mimeType) },
                        ) {
                          Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share converted file",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                          )
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }

        // Recent conversions history (only shown when conversions have been performed)
        if (recentConversions.isNotEmpty()) {
          item {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Recent Conversions (${recentConversions.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              TextButton(onClick = { recentConversions.clear() }) {
                Text("Clear")
              }
            }
          }

          items(recentConversions, key = { it.outputFile.absolutePath + it.timestampMs }) { item ->
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              ),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("converted_history_item_${item.outputFile.name}"),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = categoryIcon(item.targetFormat.category),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp),
                  )
                }

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = item.outputFile.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  val backendLabel = if (item.usedFallback) {
                    "${item.backendUsed.displayName} (Backup)"
                  } else {
                    item.backendUsed.displayName
                  }
                  Text(
                    text = "${item.sourceFormat.displayName} → ${item.targetFormat.displayName} • $backendLabel • ${item.sizeFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }

                IconButton(
                  onClick = {
                    if (item.outputFile.exists()) {
                      selectedFile = item.outputFile
                      sourceFormat = item.targetFormat
                      lastResult = null
                    }
                  },
                ) {
                  Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Convert this file again",
                    tint = MaterialTheme.colorScheme.primary,
                  )
                }

                IconButton(
                  onClick = {
                    shareConvertedFile(context, item.outputFile, item.targetFormat.mimeType)
                  },
                ) {
                  Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }

                IconButton(
                  onClick = {
                    item.outputFile.delete()
                    recentConversions.remove(item)
                  },
                ) {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete converted file",
                    tint = MaterialTheme.colorScheme.error,
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  if (showFileBrowserDialog) {
    StorageFilePickerDialog(
      initialDirectory = selectedFile?.parentFile ?: FileManagerEngine.getUserRootDirectory(),
      onDismiss = { showFileBrowserDialog = false },
      onLaunchSystemPicker = {
        showFileBrowserDialog = false
        documentPickerLauncher.launch(arrayOf("*/*"))
      },
      onFileSelected = { pickedFile ->
        selectedFile = pickedFile
        sourceFormat = FileConverterEngine.detectFormat(pickedFile)
        lastResult = null
        showFileBrowserDialog = false
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
      },
    )
  }

  if (showCapabilityMapDialog) {
    ConversionCapabilityMapDialog(
      onDismiss = { showCapabilityMapDialog = false },
    )
  }
}

/**
 * In-app storage file browser dialog adhering to DESIGN_LANGUAGE.md Section 5.2,
 * with a secondary action to launch the Android System Picker.
 */
@Composable
private fun StorageFilePickerDialog(
  initialDirectory: File,
  onDismiss: () -> Unit,
  onLaunchSystemPicker: () -> Unit,
  onFileSelected: (File) -> Unit,
) {
  val rootDir = remember { FileManagerEngine.getUserRootDirectory() }
  var currentDir by remember {
    mutableStateOf(if (initialDirectory.exists() && initialDirectory.isDirectory) initialDirectory else rootDir)
  }
  var searchQuery by remember { mutableStateOf("") }

  val entries = remember(currentDir, searchQuery) {
    val rawList = currentDir.listFiles()?.toList() ?: emptyList()
    rawList
      .filter { !it.name.startsWith(".") }
      .filter {
        searchQuery.isBlank() || it.name.contains(searchQuery.trim(), ignoreCase = true)
      }
      .sortedWith(
        compareByDescending<File> { it.isDirectory }
          .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
      )
  }

  val quickFolders = remember(rootDir) {
    listOf(
      "Storage" to rootDir,
      "Download" to File(rootDir, "Download"),
      "Documents" to File(rootDir, "Documents"),
      "Pictures" to File(rootDir, "Pictures"),
      "Audio" to File(rootDir, "Audio"),
      "Movies" to File(rootDir, "Movies"),
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Select File to Convert",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = currentDir.absolutePath,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 420.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // Quick volume / directory pills
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          for ((label, folder) in quickFolders) {
            FilterChip(
              selected = currentDir.absolutePath == folder.absolutePath,
              onClick = {
                if (folder.exists() || folder.mkdirs()) {
                  currentDir = folder
                }
              },
              label = { Text(label) },
              shape = RoundedCornerShape(20.dp),
            )
          }
        }

        // Search & Up navigation row
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          val parent = currentDir.parentFile
          IconButton(
            onClick = { if (parent != null && parent.canRead()) currentDir = parent },
            enabled = parent != null && parent.canRead(),
          ) {
            Icon(Icons.Default.ArrowUpward, contentDescription = "Parent folder")
          }

          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter files...") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            trailingIcon = if (searchQuery.isNotEmpty()) {
              {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(Icons.Default.Clear, contentDescription = "Clear search")
                }
              }
            } else null,
            modifier = Modifier.weight(1f),
          )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        if (entries.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(160.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = "No files in this folder",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            items(entries, key = { it.absolutePath }) { item ->
              val detected = if (!item.isDirectory) FileConverterEngine.detectFormat(item) else null
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .clickable {
                    if (item.isDirectory) {
                      currentDir = item
                    } else {
                      onFileSelected(item)
                    }
                  },
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Icon(
                    imageVector = if (item.isDirectory) {
                      Icons.Default.Folder
                    } else {
                      categoryIcon(detected?.category)
                    },
                    contentDescription = null,
                    tint = if (item.isDirectory) {
                      MaterialTheme.colorScheme.primary
                    } else {
                      MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(22.dp),
                  )
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = item.name,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onSurface,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                    if (!item.isDirectory) {
                      val sizeStr = FileManagerEngine.formatFileSize(item.length())
                      val badgeStr = detected?.displayName ?: "Unsupported"
                      Text(
                        text = "$badgeStr • $sizeStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = onLaunchSystemPicker,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("system_file_picker_button"),
      ) {
        Icon(
          imageVector = Icons.Default.FileOpen,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("System Picker")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
      ) {
        Text("Close")
      }
    },
  )
}

/**
 * Dialog displaying the complete dynamic conversion capability map across all formats and backends.
 */
@Composable
private fun ConversionCapabilityMapDialog(
  onDismiss: () -> Unit,
) {
  var selectedCategory by remember { mutableStateOf<ConversionMediaCategory?>(null) }
  val formats = remember(selectedCategory) {
    if (selectedCategory == null) {
      ConvertibleFormat.entries
    } else {
      ConvertibleFormat.entries.filter { it.category == selectedCategory }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    tonalElevation = 6.dp,
    modifier = Modifier.testTag("capability_map_dialog"),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Hub,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Conversion Capability Map",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Native preferred • FFmpeg automatic backup",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight(0.75f),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          FilterChip(
            selected = selectedCategory == null,
            onClick = { selectedCategory = null },
            label = { Text("All (${ConvertibleFormat.entries.size})") },
            shape = RoundedCornerShape(20.dp),
          )
          for (cat in ConversionMediaCategory.entries) {
            FilterChip(
              selected = selectedCategory == cat,
              onClick = { selectedCategory = cat },
              label = { Text(cat.displayName) },
              shape = RoundedCornerShape(20.dp),
            )
          }
        }

        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          items(formats, key = { it.name }) { fmt ->
            val routes = FileConverterEngine.getAvailableRoutes(fmt)
            val nativeTargets = routes.filter { it.preferredBackend == ConverterBackend.NATIVE }.map { it.target.displayName }
            val ffmpegTargets = routes.filter { it.preferredBackend == ConverterBackend.FFMPEG }.map { it.target.displayName }

            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              ),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Text(
                    text = "${fmt.displayName} (.${fmt.extension})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = "${routes.size} targets",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                  )
                }
                if (nativeTargets.isNotEmpty()) {
                  Text(
                    text = "Native: ${nativeTargets.joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                if (ffmpegTargets.isNotEmpty()) {
                  Text(
                    text = "FFmpeg: ${ffmpegTargets.joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
      ) {
        Text("Done")
      }
    },
  )
}

private fun categoryIcon(category: ConversionMediaCategory?): ImageVector {
  return when (category) {
    ConversionMediaCategory.IMAGE -> Icons.Default.Image
    ConversionMediaCategory.AUDIO -> Icons.Default.AudioFile
    ConversionMediaCategory.VIDEO -> Icons.Default.VideoFile
    ConversionMediaCategory.DOCUMENT -> Icons.Default.Description
    ConversionMediaCategory.ARCHIVE -> Icons.Default.Archive
    null -> Icons.Default.Description
  }
}

private fun copyUriToTempSourceFile(context: Context, uri: Uri): File? {
  return try {
    val contentResolver = context.contentResolver
    var displayName: String? = null
    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
      if (cursor.moveToFirst()) {
        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (idx >= 0) displayName = cursor.getString(idx)
      }
    }
    val safeName = (displayName ?: uri.lastPathSegment ?: "imported_file")
      .substringAfterLast('/')
      .replace(Regex("[\\\\/:*?\"<>|]"), "_")
    val importDir = File(context.cacheDir, "converter_imports").apply { mkdirs() }
    val dstFile = File(importDir, safeName)
    contentResolver.openInputStream(uri)?.use { input ->
      FileOutputStream(dstFile).use { output ->
        input.copyTo(output)
      }
    } ?: return null
    dstFile.takeIf { it.exists() && it.length() > 0L }
  } catch (_: Exception) {
    null
  }
}

private fun shareConvertedFile(context: Context, file: File, mimeType: String) {
  try {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
      type = mimeType
      putExtra(Intent.EXTRA_STREAM, uri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share ${file.name}"))
  } catch (_: Exception) {
    Toast.makeText(context, "Saved at: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FileConverterScreenEmptyPreview() {
  ToolboxTheme {
    FileConverterScreen()
  }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun FileConverterScreenSelectedPreview() {
  ToolboxTheme {
    FileConverterScreen(
      initialFile = File("/storage/emulated/0/Pictures/sample_photo.png"),
    )
  }
}
