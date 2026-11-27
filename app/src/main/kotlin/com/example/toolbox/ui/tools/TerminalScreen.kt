package com.example.toolbox.ui.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toolbox.ui.theme.Button
import com.example.toolbox.ui.theme.CircleShape
import com.example.toolbox.ui.theme.FilledTonalButton
import com.example.toolbox.ui.theme.FilterChip
import com.example.toolbox.ui.theme.HorizontalHexagonShape
import com.example.toolbox.ui.theme.OutlinedButton
import com.example.toolbox.ui.theme.TextButton
import com.example.toolbox.ui.theme.currentHexagonIconButtons
import com.example.toolbox.ui.theme.toolboxScreenBackground
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/**
 * Terminal tool screen supporting:
 * - **Option 1 (Default)**: Local Shell (`/system/bin/sh` via `ProcessBuilder` with Toybox utilities,
 *   working directory tracking, environment variables, and ANSI color output).
 * - **Option 3 (Elevated Privileges)**: On-device Wireless ADB Loopback (`127.0.0.1:<port>`),
 *   connecting directly to Android's local `adbd` daemon for `uid=2000(shell)` privileges
 *   (`dumpsys`, `logcat`, `pm`, `wm`, `cmd`, etc.) with mDNS port auto-discovery and 6-digit pairing.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TerminalScreen(
  initialDirectory: File? = null,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val keyboardController = LocalSoftwareKeyboardController.current
  val controller = remember { TerminalController(context.applicationContext) }

  LaunchedEffect(initialDirectory) {
    if (initialDirectory != null && initialDirectory.exists() && initialDirectory.isDirectory) {
      controller.executeCommand("cd ${shellEscapeSingleQuoted(initialDirectory.absolutePath)}")
    }
  }

  DisposableEffect(controller) {
    onDispose {
      controller.stopAdbMdnsDiscovery()
    }
  }

  val backendMode by controller.backendMode.collectAsState()
  val workingDirectory by controller.workingDirectory.collectAsState()
  val isRunning by controller.isRunning.collectAsState()
  val activeCommandText by controller.activeCommandText.collectAsState()
  val adbConnectionState by controller.adbConnectionState.collectAsState()
  val adbStatusMessage by controller.adbStatusMessage.collectAsState()
  val adbHost by controller.adbHost.collectAsState()
  val adbConnectPort by controller.adbConnectPort.collectAsState()
  val adbPairPort by controller.adbPairPort.collectAsState()
  val discoveredEndpoints by controller.discoveredEndpoints.collectAsState()
  val favoriteCommandIds by controller.favoriteCommandIds.collectAsState()
  val customCommands by controller.customCommands.collectAsState()
  val commandHistory by controller.commandHistory.collectAsState()
  val fontSizeSp by controller.fontSizeSp.collectAsState()
  val outputEntries by controller.outputEntries.collectAsState()

  var commandInput by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
  var historyCursor by rememberSaveable { mutableIntStateOf(-1) }

  // Top Bar Search & Command Catalog overlay state (DESIGN_LANGUAGE.md Section 5.7)
  var isSearchActive by rememberSaveable { mutableStateOf(false) }
  var searchQuery by rememberSaveable { mutableStateOf("") }
  var selectedCatalogCategory by rememberSaveable { mutableStateOf(TerminalCommandCategory.ALL) }

  // Dialog states
  var showAdbSetupDialog by rememberSaveable { mutableStateOf(false) }
  var showFontSizeDialog by rememberSaveable { mutableStateOf(false) }
  var showAddCustomCommandDialog by rememberSaveable { mutableStateOf(false) }

  val listState = rememberLazyListState()
  val commandFocusRequester = remember { FocusRequester() }

  LaunchedEffect(outputEntries.size) {
    if (outputEntries.isNotEmpty()) {
      runCatching { listState.animateScrollToItem(outputEntries.lastIndex) }
    }
  }

  LaunchedEffect(showAdbSetupDialog) {
    if (showAdbSetupDialog) {
      controller.startAdbMdnsDiscovery()
    } else {
      controller.stopAdbMdnsDiscovery()
    }
  }

  fun closeSearch() {
    isSearchActive = false
    searchQuery = ""
    selectedCatalogCategory = TerminalCommandCategory.ALL
    keyboardController?.hide()
  }

  BackHandler(enabled = isSearchActive) {
    closeSearch()
  }

  val subtext = remember(backendMode, workingDirectory, adbConnectionState, isRunning, activeCommandText) {
    when {
      isRunning && !activeCommandText.isNullOrBlank() -> "Running: $activeCommandText"
      backendMode == TerminalBackendMode.LOCAL_SHELL -> "Local Shell (sh) • $workingDirectory"
      adbConnectionState == AdbConnectionState.CONNECTED -> "Wireless ADB (uid=2000) • $workingDirectory"
      else -> "Wireless ADB (${adbConnectionState.label}) • $workingDirectory"
    }
  }

  val filteredCatalogPresets = remember(
    searchQuery,
    selectedCatalogCategory,
    favoriteCommandIds,
    customCommands,
  ) {
    TerminalCatalog.filterPresets(
      query = searchQuery,
      category = selectedCatalogCategory,
      favoriteIds = favoriteCommandIds,
      customPresets = customCommands,
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .toolboxScreenBackground(MaterialTheme.colorScheme.background)
      .testTag("terminal_screen"),
  ) {
    // 1. Fixed 68.dp ToolboxTopBar (DESIGN_LANGUAGE.md Section 5.1 & 5.7)
    ToolboxTopBar(
      title = "Terminal",
      subtext = subtext,
      icon = Icons.Default.Terminal,
      titleTestTag = "terminal_top_bar_title",
      subtextTestTag = "terminal_top_bar_subtext",
      actions = {
        // Wireless ADB setup/connection action button
        IconButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showAdbSetupDialog = true
          },
          modifier = Modifier
            .size(36.dp)
            .testTag("action_open_adb_dialog"),
        ) {
          Icon(
            imageVector = if (adbConnectionState == AdbConnectionState.CONNECTED) {
              Icons.Default.AdminPanelSettings
            } else {
              Icons.Default.Wifi
            },
            contentDescription = "Wireless ADB Privileges",
            tint = if (adbConnectionState == AdbConnectionState.CONNECTED) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(20.dp),
          )
        }

        // Font size adjustment action button
        IconButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showFontSizeDialog = true
          },
          modifier = Modifier
            .size(36.dp)
            .testTag("action_terminal_font_size"),
        ) {
          Icon(
            imageVector = Icons.Default.FormatSize,
            contentDescription = "Terminal Font Size",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
          )
        }

        // Copy full transcript button
        IconButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val fullTranscript = outputEntries.joinToString("\n") { entry ->
              when (entry.kind) {
                TerminalLineKind.COMMAND ->
                  "${entry.workingDirectory ?: "/"} ${entry.backendMode.promptSymbol} ${entry.plainText}"
                else -> entry.plainText
              }
            }
            copyToClipboard(context, "Terminal Transcript", fullTranscript)
          },
          modifier = Modifier
            .size(36.dp)
            .testTag("action_copy_transcript"),
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy Transcript",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
          )
        }

        // Clear scrollback button
        IconButton(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            controller.clearScrollback()
          },
          modifier = Modifier
            .size(36.dp)
            .testTag("action_clear_terminal"),
        ) {
          Icon(
            imageVector = Icons.Default.DeleteSweep,
            contentDescription = "Clear Terminal",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
          )
        }
      },
      searchState = ToolboxTopBarSearchState(
        isActive = isSearchActive,
        query = searchQuery,
        onQueryChange = { searchQuery = it },
        onOpenSearch = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          isSearchActive = true
        },
        onCloseSearch = { closeSearch() },
        placeholder = "Search shell commands & presets...",
        toggleButtonTestTag = "action_toggle_terminal_search",
        inputTestTag = "terminal_search_input",
      ),
    )

    // 2. Backend Mode & Quick Action Pill Bar (DESIGN_LANGUAGE.md Section 5.4)
    ToolboxPillNavigationBar(
      floatingButtonsCount = 1,
      floatingControls = {
        val hexagonEnabled = currentHexagonIconButtons()
        Surface(
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showAddCustomCommandDialog = true
          },
          shape = if (hexagonEnabled) HorizontalHexagonShape else CircleShape,
          color = MaterialTheme.colorScheme.primaryContainer,
          contentColor = MaterialTheme.colorScheme.primary,
          modifier = Modifier
            .size(34.dp)
            .testTag("action_add_custom_command"),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Save Custom Command",
              modifier = Modifier.size(18.dp),
            )
          }
        }
      },
    ) {
      // Option 1 Pill: Local Shell (sh)
      ToolboxPillNavigationItem(
        selected = backendMode == TerminalBackendMode.LOCAL_SHELL,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          controller.selectBackendMode(TerminalBackendMode.LOCAL_SHELL)
        },
        label = "Local Shell (sh)",
        leadingIcon = Icons.Default.Terminal,
        modifier = Modifier.testTag("pill_backend_local_shell"),
      )

      // Option 3 Pill: Wireless ADB (UID 2000 shell)
      ToolboxPillNavigationItem(
        selected = backendMode == TerminalBackendMode.WIRELESS_ADB,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          controller.selectBackendMode(TerminalBackendMode.WIRELESS_ADB)
        },
        label = if (adbConnectionState == AdbConnectionState.CONNECTED) {
          "Wireless ADB (UID 2000)"
        } else {
          "Wireless ADB"
        },
        leadingIcon = Icons.Default.AdminPanelSettings,
        modifier = Modifier.testTag("pill_backend_wireless_adb"),
      )

      // Quick favorite command pills for one-tap execution
      val favoritePresets = remember(favoriteCommandIds, customCommands) {
        (customCommands + TerminalCatalog.BUILT_IN_PRESETS).filter { it.id in favoriteCommandIds }
      }
      favoritePresets.forEach { preset ->
        ToolboxPillNavigationItem(
          selected = false,
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            controller.executeCommand(preset.command)
          },
          label = preset.title,
          leadingIcon = Icons.Default.PlayArrow,
          modifier = Modifier.testTag("pill_quick_cmd_${preset.id}"),
        )
      }
    }

    // 3. Wireless ADB Connection Banner when Option 3 is selected
    AnimatedVisibility(
      visible = backendMode == TerminalBackendMode.WIRELESS_ADB,
      enter = expandVertically(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)),
      exit = shrinkVertically(animationSpec = tween(180)) + fadeOut(animationSpec = tween(180)),
    ) {
      WirelessAdbStatusBanner(
        connectionState = adbConnectionState,
        statusMessage = adbStatusMessage,
        host = adbHost,
        connectPort = adbConnectPort,
        onOpenSetup = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          showAdbSetupDialog = true
        },
        onQuickConnect = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          controller.connectWirelessAdb()
        },
        onSwitchToLocal = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          controller.selectBackendMode(TerminalBackendMode.LOCAL_SHELL)
        },
      )
    }

    // 4. Main Content Area + Search Catalog Overlay
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .swipeToSwitchPillView(
          items = TerminalBackendMode.entries,
          selectedItem = backendMode,
          enabled = !isSearchActive,
          onSelectItem = { mode ->
            controller.selectBackendMode(mode)
          },
        ),
    ) {
      // Console Scrollback List
      SelectionContainer(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
          state = listState,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier
            .fillMaxSize()
            .testTag("terminal_scrollback_list"),
        ) {
          items(
            items = outputEntries,
            key = { it.id },
          ) { entry ->
            TerminalOutputLineItem(
              entry = entry,
              fontSizeSp = fontSizeSp,
              onCopyText = { text ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                copyToClipboard(context, "Terminal Output", text)
              },
              onRerunCommand = { cmd ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                commandInput = TextFieldValue(
                  text = cmd,
                  selection = TextRange(cmd.length),
                )
              },
            )
          }
        }
      }

      // Top-Expanding Command Catalog Search Overlay (DESIGN_LANGUAGE.md Section 5.7)
      this@Column.AnimatedVisibility(
        visible = isSearchActive,
        enter = expandVertically(expandFrom = Alignment.Top, animationSpec = tween(220)) +
          fadeIn(animationSpec = tween(220)),
        exit = shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(180)) +
          fadeOut(animationSpec = tween(180)),
      ) {
        Surface(
          color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f),
          modifier = Modifier
            .fillMaxSize()
            .testTag("terminal_search_overlay"),
        ) {
          Column(modifier = Modifier.fillMaxSize()) {
            ToolboxPillNavigationBar {
              TerminalCommandCategory.entries.forEach { category ->
                ToolboxPillNavigationItem(
                  selected = selectedCatalogCategory == category,
                  onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectedCatalogCategory = category
                  },
                  label = category.label,
                  leadingIcon = if (category == TerminalCommandCategory.FAVORITES) {
                    Icons.Default.Star
                  } else {
                    null
                  },
                  modifier = Modifier.testTag("catalog_category_${category.name}"),
                )
              }
            }

            ToolboxPillHorizontalPager(
              items = TerminalCommandCategory.entries,
              selectedItem = selectedCatalogCategory,
              key = { _, cat -> cat.name },
              onSelectItem = { category ->
                selectedCatalogCategory = category
              },
              modifier = Modifier.fillMaxSize(),
            ) { pageCategory ->
              val pagePresets = if (pageCategory == selectedCatalogCategory) {
                filteredCatalogPresets
              } else {
                remember(searchQuery, pageCategory, favoriteCommandIds, customCommands) {
                  TerminalCatalog.filterPresets(
                    query = searchQuery,
                    category = pageCategory,
                    favoriteIds = favoriteCommandIds,
                    customPresets = customCommands,
                  )
                }
              }

              LazyColumn(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
              ) {
                if (pagePresets.isEmpty()) {
                  item {
                    Card(
                      shape = RoundedCornerShape(14.dp),
                      colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                      ),
                      modifier = Modifier.fillMaxWidth(),
                    ) {
                      Column(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                      ) {
                        Text(
                          text = "No matching shell commands",
                          style = MaterialTheme.typography.bodyLarge,
                          fontWeight = FontWeight.SemiBold,
                          color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                          text = "Try searching for logcat, dumpsys, getprop, ip, df, or save a custom command.",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                      }
                    }
                  }
                } else {
                  items(
                    items = pagePresets,
                    key = { it.id },
                  ) { preset ->
                    val isFav = preset.id in favoriteCommandIds
                    TerminalPresetCatalogCard(
                      preset = preset,
                      isFavorite = isFav,
                      onRunCommand = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        closeSearch()
                        controller.executeCommand(preset.command)
                      },
                      onInsertCommand = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        closeSearch()
                        commandInput = TextFieldValue(
                          text = preset.command,
                          selection = TextRange(preset.command.length),
                        )
                      },
                      onToggleFavorite = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        controller.toggleFavoriteCommand(preset.id)
                      },
                      onDeleteCustom = if (preset.isCustom) {
                        {
                          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                          controller.deleteCustomCommand(preset.id)
                        }
                      } else {
                        null
                      },
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // 5. Tactile Extra Keys Row + Docked Command Input Bar
    Surface(
      color = MaterialTheme.colorScheme.surfaceContainer,
      tonalElevation = 3.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 6.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        // Extra Keys Row (CTRL+C, TAB, UP, DOWN, CLS, |, >, &&, /, -, ~, $)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp)
            .testTag("terminal_extra_keys_row"),
          horizontalArrangement = Arrangement.spacedBy(3.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          val hexagonEnabled = currentHexagonIconButtons()
          TerminalExtraKey.entries.forEach { key ->
            val isCtrlC = key == TerminalExtraKey.CTRL_C
            val isCompactKey = key.label.length <= 2
            Surface(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                when (key) {
                  TerminalExtraKey.CTRL_C -> controller.cancelRunningCommand()
                  TerminalExtraKey.CLEAR -> controller.clearScrollback()
                  TerminalExtraKey.TAB -> {
                    val completed = controller.autocompleteInput(commandInput.text)
                    commandInput = TextFieldValue(
                      text = completed,
                      selection = TextRange(completed.length),
                    )
                  }
                  TerminalExtraKey.UP -> {
                    if (commandHistory.isNotEmpty()) {
                      val nextIdx = if (historyCursor < 0) {
                        commandHistory.lastIndex
                      } else {
                        (historyCursor - 1).coerceAtLeast(0)
                      }
                      historyCursor = nextIdx
                      val cmd = commandHistory[nextIdx]
                      commandInput = TextFieldValue(cmd, TextRange(cmd.length))
                    }
                  }
                  TerminalExtraKey.DOWN -> {
                    if (commandHistory.isNotEmpty() && historyCursor >= 0) {
                      val nextIdx = historyCursor + 1
                      if (nextIdx <= commandHistory.lastIndex) {
                        historyCursor = nextIdx
                        val cmd = commandHistory[nextIdx]
                        commandInput = TextFieldValue(cmd, TextRange(cmd.length))
                      } else {
                        historyCursor = -1
                        commandInput = TextFieldValue("")
                      }
                    }
                  }
                  else -> {
                    val insert = key.insertText ?: ""
                    val cur = commandInput.text
                    val selStart = commandInput.selection.min.coerceIn(0, cur.length)
                    val selEnd = commandInput.selection.max.coerceIn(0, cur.length)
                    val updated = cur.substring(0, selStart) + insert + cur.substring(selEnd)
                    val newCursor = selStart + insert.length
                    commandInput = TextFieldValue(
                      text = updated,
                      selection = TextRange(newCursor),
                    )
                  }
                }
              },
              shape = if (hexagonEnabled) HorizontalHexagonShape else RoundedCornerShape(20.dp),
              color = if (isCtrlC && isRunning) {
                MaterialTheme.colorScheme.errorContainer
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
              },
              contentColor = if (isCtrlC && isRunning) {
                MaterialTheme.colorScheme.onErrorContainer
              } else {
                MaterialTheme.colorScheme.onSurface
              },
              border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
              ),
              modifier = Modifier
                .height(32.dp)
                .widthIn(min = if (isCompactKey) 42.dp else 48.dp)
                .testTag("extra_key_${key.name}"),
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = if (isCompactKey) 14.dp else 11.dp),
              ) {
                Text(
                  text = key.label,
                  style = MaterialTheme.typography.labelSmall,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }
        }

        // Command Input Pill Bar (RoundedCornerShape(28.dp) or HorizontalHexagonShape)
        val hexagonEnabled = currentHexagonIconButtons()
        val inputShape = if (hexagonEnabled) HorizontalHexagonShape else RoundedCornerShape(28.dp)

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Surface(
            shape = inputShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
              1.dp,
              MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            ),
            modifier = Modifier.weight(1f),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              // Active backend prompt indicator ($ for Local Shell, # for Wireless ADB)
              Box(
                modifier = Modifier
                  .size(26.dp)
                  .clip(CircleShape)
                  .background(
                    if (backendMode == TerminalBackendMode.WIRELESS_ADB) {
                      MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                      MaterialTheme.colorScheme.primaryContainer
                    },
                  ),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = backendMode.promptSymbol,
                  style = MaterialTheme.typography.bodyMedium,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  color = if (backendMode == TerminalBackendMode.WIRELESS_ADB) {
                    MaterialTheme.colorScheme.onTertiaryContainer
                  } else {
                    MaterialTheme.colorScheme.primary
                  },
                )
              }

              TextField(
                value = commandInput,
                onValueChange = {
                  commandInput = it
                  historyCursor = -1
                },
                placeholder = {
                  Text(
                    text = if (backendMode == TerminalBackendMode.WIRELESS_ADB) {
                      "Enter ADB shell command (dumpsys, logcat, pm...)"
                    } else {
                      "Enter shell command (ls, ps, getprop, ping...)"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                  fontFamily = FontFamily.Monospace,
                  color = MaterialTheme.colorScheme.onSurface,
                ),
                keyboardOptions = KeyboardOptions(
                  capitalization = KeyboardCapitalization.None,
                  autoCorrectEnabled = false,
                  keyboardType = KeyboardType.Ascii,
                  imeAction = ImeAction.Send,
                ),
                keyboardActions = KeyboardActions(
                  onSend = {
                    val cmd = commandInput.text.trim()
                    if (cmd.isNotEmpty()) {
                      haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                      commandInput = TextFieldValue("")
                      historyCursor = -1
                      controller.executeCommand(cmd)
                    }
                  },
                ),
                colors = TextFieldDefaults.colors(
                  focusedContainerColor = Color.Transparent,
                  unfocusedContainerColor = Color.Transparent,
                  disabledContainerColor = Color.Transparent,
                  focusedIndicatorColor = Color.Transparent,
                  unfocusedIndicatorColor = Color.Transparent,
                  disabledIndicatorColor = Color.Transparent,
                ),
                trailingIcon = {
                  if (commandInput.text.isNotEmpty()) {
                    IconButton(
                      onClick = { commandInput = TextFieldValue("") },
                      modifier = Modifier.size(32.dp),
                    ) {
                      Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear command",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                      )
                    }
                  }
                },
                modifier = Modifier
                  .weight(1f)
                  .focusRequester(commandFocusRequester)
                  .testTag("terminal_command_input"),
              )
            }
          }

          // Run or Stop action button
          if (isRunning) {
            Surface(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                controller.cancelRunningCommand()
              },
              shape = CircleShape,
              color = MaterialTheme.colorScheme.errorContainer,
              contentColor = MaterialTheme.colorScheme.error,
              modifier = Modifier
                .size(46.dp)
                .testTag("action_stop_command"),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Stop,
                  contentDescription = "Stop Command",
                  modifier = Modifier.size(22.dp),
                )
              }
            }
          } else {
            val canSend = commandInput.text.isNotBlank()
            Surface(
              onClick = {
                val cmd = commandInput.text.trim()
                if (cmd.isNotEmpty()) {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  commandInput = TextFieldValue("")
                  historyCursor = -1
                  controller.executeCommand(cmd)
                }
              },
              enabled = canSend,
              shape = CircleShape,
              color = if (canSend) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.surfaceVariant
              },
              contentColor = if (canSend) {
                MaterialTheme.colorScheme.onPrimary
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
              },
              modifier = Modifier
                .size(46.dp)
                .testTag("action_execute_command"),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.Send,
                  contentDescription = "Execute Command",
                  modifier = Modifier.size(20.dp),
                )
              }
            }
          }
        }
      }
    }
  }

  // Dialog 1: Option 3 Wireless ADB Pairing & Loopback Connection Dialog (DESIGN_LANGUAGE.md Section 5.2)
  if (showAdbSetupDialog) {
    WirelessAdbSetupDialog(
      connectionState = adbConnectionState,
      statusMessage = adbStatusMessage,
      initialHost = adbHost,
      initialConnectPort = adbConnectPort,
      initialPairPort = adbPairPort,
      discoveredEndpoints = discoveredEndpoints,
      onConnect = { host, port ->
        controller.connectWirelessAdb(host = host, connectPort = port)
      },
      onPair = { host, pairPort, code ->
        controller.pairWirelessAdb(host = host, pairPort = pairPort, pairingCode = code)
      },
      onDisconnect = {
        controller.disconnectWirelessAdb()
      },
      onOpenDeveloperOptions = {
        openDeveloperOptionsSettings(context)
      },
      onDismiss = { showAdbSetupDialog = false },
    )
  }

  // Dialog 2: Font Size Adjustment Dialog with SlimDotSlider (DESIGN_LANGUAGE.md Section 5.2 & 5.6)
  if (showFontSizeDialog) {
    TerminalFontSizeDialog(
      currentSizeSp = fontSizeSp,
      onConfirm = { newSize ->
        controller.setFontSizeSp(newSize)
        showFontSizeDialog = false
      },
      onDismiss = { showFontSizeDialog = false },
    )
  }

  // Dialog 3: Save Custom Command Preset Dialog (DESIGN_LANGUAGE.md Section 5.2)
  if (showAddCustomCommandDialog) {
    SaveCustomCommandDialog(
      initialCommand = commandInput.text.trim(),
      initialRequiresAdb = backendMode == TerminalBackendMode.WIRELESS_ADB,
      onConfirm = { title, cmd, desc, requiresAdb ->
        controller.saveCustomCommand(
          title = title,
          command = cmd,
          description = desc,
          requiresAdbPrivilege = requiresAdb,
        )
        showAddCustomCommandDialog = false
      },
      onDismiss = { showAddCustomCommandDialog = false },
    )
  }
}

@Composable
private fun WirelessAdbStatusBanner(
  connectionState: AdbConnectionState,
  statusMessage: String,
  host: String,
  connectPort: Int,
  onOpenSetup: () -> Unit,
  onQuickConnect: () -> Unit,
  onSwitchToLocal: () -> Unit,
) {
  val isConnected = connectionState == AdbConnectionState.CONNECTED
  val isBusy = connectionState == AdbConnectionState.CONNECTING ||
    connectionState == AdbConnectionState.PAIRING

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isConnected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
      } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
      },
    ),
    border = BorderStroke(
      1.dp,
      if (isConnected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
      } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
      },
    ),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 4.dp)
      .testTag("wireless_adb_status_banner"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(
            if (isConnected) {
              MaterialTheme.colorScheme.primaryContainer
            } else {
              MaterialTheme.colorScheme.tertiaryContainer
            },
          ),
        contentAlignment = Alignment.Center,
      ) {
        if (isBusy) {
          CircularProgressIndicator(
            strokeWidth = 2.dp,
            modifier = Modifier.size(18.dp),
          )
        } else {
          Icon(
            imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.AdminPanelSettings,
            contentDescription = null,
            tint = if (isConnected) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.onTertiaryContainer
            },
            modifier = Modifier.size(18.dp),
          )
        }
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = if (isConnected) {
            "Wireless ADB Active • $host:$connectPort"
          } else {
            "Wireless ADB Loopback ($host:$connectPort)"
          },
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = statusMessage,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }

      if (!isConnected && !isBusy) {
        FilledTonalButton(
          onClick = onQuickConnect,
          shape = CircleShape,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier.height(32.dp),
        ) {
          Text(
            text = "Connect",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
          )
        }
      }

      OutlinedButton(
        onClick = onOpenSetup,
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier
          .height(32.dp)
          .testTag("action_banner_pair_setup"),
      ) {
        Text(
          text = if (isConnected) "Config" else "Pair / Setup",
          style = MaterialTheme.typography.labelMedium,
        )
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TerminalOutputLineItem(
  entry: TerminalOutputEntry,
  fontSizeSp: Float,
  onCopyText: (String) -> Unit,
  onRerunCommand: (String) -> Unit,
) {
  val colorScheme = MaterialTheme.colorScheme

  when (entry.kind) {
    TerminalLineKind.BANNER -> {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
          .fillMaxWidth()
          .combinedClickable(
            onClick = {},
            onLongClick = { onCopyText(entry.plainText) },
          ),
      ) {
        Text(
          text = buildAnsiAnnotatedString(entry.segments, colorScheme.onSurface),
          fontFamily = FontFamily.Monospace,
          fontSize = fontSizeSp.sp,
          lineHeight = (fontSizeSp * 1.38f).sp,
          modifier = Modifier.padding(12.dp),
        )
      }
    }

    TerminalLineKind.COMMAND -> {
      val dirShort = remember(entry.workingDirectory) {
        val raw = entry.workingDirectory ?: "/"
        if (raw.length > 28) "…/${raw.substringAfterLast('/')}" else raw
      }
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = colorScheme.primaryContainer.copy(alpha = 0.35f),
        modifier = Modifier
          .fillMaxWidth()
          .combinedClickable(
            onClick = { onRerunCommand(entry.plainText) },
            onLongClick = { onCopyText(entry.plainText) },
          ),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 7.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "$dirShort ${entry.backendMode.promptSymbol}",
            fontFamily = FontFamily.Monospace,
            fontSize = fontSizeSp.sp,
            fontWeight = FontWeight.Bold,
            color = if (entry.backendMode == TerminalBackendMode.WIRELESS_ADB) {
              colorScheme.tertiary
            } else {
              colorScheme.primary
            },
          )
          Text(
            text = entry.plainText,
            fontFamily = FontFamily.Monospace,
            fontSize = fontSizeSp.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurface,
            modifier = Modifier.weight(1f),
          )
          Text(
            text = entry.formattedTime,
            style = MaterialTheme.typography.labelSmall,
            color = colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    TerminalLineKind.STDERR -> {
      Text(
        text = buildAnsiAnnotatedString(entry.segments, colorScheme.error),
        fontFamily = FontFamily.Monospace,
        fontSize = fontSizeSp.sp,
        lineHeight = (fontSizeSp * 1.35f).sp,
        color = colorScheme.error,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 4.dp)
          .combinedClickable(
            onClick = {},
            onLongClick = { onCopyText(entry.plainText) },
          ),
      )
    }

    TerminalLineKind.STATUS -> {
      Text(
        text = buildAnsiAnnotatedString(entry.segments, colorScheme.tertiary),
        fontFamily = FontFamily.Monospace,
        fontSize = fontSizeSp.sp,
        lineHeight = (fontSizeSp * 1.35f).sp,
        color = colorScheme.tertiary,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 4.dp)
          .combinedClickable(
            onClick = {},
            onLongClick = { onCopyText(entry.plainText) },
          ),
      )
    }

    TerminalLineKind.STDOUT -> {
      Text(
        text = buildAnsiAnnotatedString(entry.segments, colorScheme.onSurface),
        fontFamily = FontFamily.Monospace,
        fontSize = fontSizeSp.sp,
        lineHeight = (fontSizeSp * 1.35f).sp,
        color = colorScheme.onSurface,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 4.dp)
          .combinedClickable(
            onClick = {},
            onLongClick = { onCopyText(entry.plainText) },
          ),
      )
    }
  }
}

@Composable
private fun buildAnsiAnnotatedString(
  segments: List<AnsiTextSegment>,
  defaultColor: Color,
): AnnotatedString {
  val scheme = MaterialTheme.colorScheme
  return buildAnnotatedString {
    segments.forEach { seg ->
      val resolvedFg = when (seg.fgAnsiCode) {
        30, 90 -> scheme.onSurfaceVariant
        31, 91 -> scheme.error
        32, 92 -> scheme.primary
        33, 93 -> scheme.tertiary
        34, 94 -> scheme.secondary
        35, 95 -> scheme.primary
        36, 96 -> scheme.tertiary
        37, 97 -> scheme.onSurface
        else -> defaultColor
      }
      withStyle(
        SpanStyle(
          color = resolvedFg,
          fontWeight = if (seg.isBold) FontWeight.Bold else FontWeight.Normal,
        ),
      ) {
        append(seg.text)
      }
    }
  }
}

@Composable
private fun TerminalPresetCatalogCard(
  preset: TerminalCommandPreset,
  isFavorite: Boolean,
  onRunCommand: () -> Unit,
  onInsertCommand: () -> Unit,
  onToggleFavorite: () -> Unit,
  onDeleteCustom: (() -> Unit)? = null,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(
      1.dp,
      MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onRunCommand)
      .testTag("catalog_preset_card_${preset.id}"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(
            if (preset.requiresAdbPrivilege) {
              MaterialTheme.colorScheme.tertiaryContainer
            } else {
              MaterialTheme.colorScheme.primaryContainer
            },
          ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = if (preset.requiresAdbPrivilege) {
            Icons.Default.AdminPanelSettings
          } else {
            Icons.Default.Terminal
          },
          contentDescription = null,
          tint = if (preset.requiresAdbPrivilege) {
            MaterialTheme.colorScheme.onTertiaryContainer
          } else {
            MaterialTheme.colorScheme.primary
          },
          modifier = Modifier.size(20.dp),
        )
      }

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(3.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = preset.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
          )
          if (preset.requiresAdbPrivilege) {
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
              Text(
                text = "ADB / UID 2000",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
              )
            }
          }
        }

        Text(
          text = preset.command,
          style = MaterialTheme.typography.bodyMedium,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.primary,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )

        Text(
          text = "${preset.category.label} • ${preset.description}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }

      IconButton(
        onClick = onInsertCommand,
        modifier = Modifier.size(36.dp),
      ) {
        Icon(
          imageVector = Icons.Default.Code,
          contentDescription = "Edit command in input bar",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp),
        )
      }

      if (onDeleteCustom != null) {
        IconButton(
          onClick = onDeleteCustom,
          modifier = Modifier.size(36.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete custom command",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp),
          )
        }
      }

      IconButton(
        onClick = onToggleFavorite,
        modifier = Modifier
          .size(36.dp)
          .testTag("toggle_fav_${preset.id}"),
      ) {
        Icon(
          imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
          contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
          tint = if (isFavorite) {
            MaterialTheme.colorScheme.primary
          } else {
            MaterialTheme.colorScheme.onSurfaceVariant
          },
          modifier = Modifier.size(20.dp),
        )
      }
    }
  }
}

/**
 * Dialog for configuring & pairing Option 3 (Wireless ADB Loopback) in compliance with
 * `DESIGN_LANGUAGE.md` Section 5.2 (28.dp corners, 44.dp icon badge, context card, 14.dp inputs, CircleShape buttons).
 */
@Composable
private fun WirelessAdbSetupDialog(
  connectionState: AdbConnectionState,
  statusMessage: String,
  initialHost: String,
  initialConnectPort: Int,
  initialPairPort: Int,
  discoveredEndpoints: List<AdbDiscoveredEndpoint>,
  onConnect: (host: String, port: Int) -> Unit,
  onPair: (host: String, pairPort: Int, code: String) -> Unit,
  onDisconnect: () -> Unit,
  onOpenDeveloperOptions: () -> Unit,
  onDismiss: () -> Unit,
) {
  var isPairingMode by rememberSaveable { mutableStateOf(false) }
  var hostField by remember {
    mutableStateOf(TextFieldValue(initialHost, TextRange(0, initialHost.length)))
  }
  var connectPortField by remember {
    val str = initialConnectPort.toString()
    mutableStateOf(TextFieldValue(str, TextRange(0, str.length)))
  }
  var pairPortField by remember {
    val str = initialPairPort.toString()
    mutableStateOf(TextFieldValue(str, TextRange(0, str.length)))
  }
  var pairingCodeField by remember {
    mutableStateOf(TextFieldValue(""))
  }

  val focusRequester = remember { FocusRequester() }
  LaunchedEffect(isPairingMode) {
    delay(80)
    runCatching { focusRequester.requestFocus() }
  }

  // Update port fields automatically when mDNS discovers active local Wireless Debugging services
  LaunchedEffect(discoveredEndpoints) {
    discoveredEndpoints.firstOrNull { !it.isPairingService }?.let { ep ->
      val p = ep.port.toString()
      connectPortField = TextFieldValue(p, TextRange(p.length))
    }
    discoveredEndpoints.firstOrNull { it.isPairingService }?.let { ep ->
      val p = ep.port.toString()
      pairPortField = TextFieldValue(p, TextRange(p.length))
    }
  }

  val parsedHost = hostField.text.trim()
  val parsedConnectPort = connectPortField.text.trim().toIntOrNull()
  val parsedPairPort = pairPortField.text.trim().toIntOrNull()
  val parsedPairingCode = pairingCodeField.text.trim()

  val isHostValid = parsedHost.isNotEmpty()
  val isConnectPortValid = parsedConnectPort != null && parsedConnectPort in 1..65535
  val isPairPortValid = parsedPairPort != null && parsedPairPort in 1..65535
  val isPairingCodeValid = parsedPairingCode.length in 6..16

  val canSubmit = if (isPairingMode) {
    isHostValid && isPairPortValid && isPairingCodeValid
  } else {
    isHostValid && isConnectPortValid
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
            imageVector = Icons.Default.AdminPanelSettings,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Wireless ADB Shell",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Loopback adbd (uid=2000)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Context / Status Preview Card
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
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(
                text = connectionState.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (connectionState == AdbConnectionState.CONNECTED) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.onSurface
                },
              )
              TextButton(
                onClick = onOpenDeveloperOptions,
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Settings,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Developer Options",
                  style = MaterialTheme.typography.labelSmall,
                )
              }
            }
            Text(
              text = statusMessage,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (discoveredEndpoints.isNotEmpty()) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                discoveredEndpoints.forEach { ep ->
                  FilterChip(
                    selected = false,
                    onClick = {
                      val p = ep.port.toString()
                      if (ep.isPairingService) {
                        isPairingMode = true
                        pairPortField = TextFieldValue(p, TextRange(p.length))
                      } else {
                        isPairingMode = false
                        connectPortField = TextFieldValue(p, TextRange(p.length))
                      }
                    },
                    label = {
                      Text(
                        text = if (ep.isPairingService) "Pair :${ep.port}" else "Connect :${ep.port}",
                        style = MaterialTheme.typography.labelSmall,
                      )
                    },
                    shape = RoundedCornerShape(20.dp),
                  )
                }
              }
            }
          }
        }

        // Mode switcher: Connect vs Pair with 6-Digit Code
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          FilterChip(
            selected = !isPairingMode,
            onClick = { isPairingMode = false },
            label = { Text("1. Connect Port", style = MaterialTheme.typography.labelMedium) },
            leadingIcon = {
              Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.weight(1f),
          )
          FilterChip(
            selected = isPairingMode,
            onClick = { isPairingMode = true },
            label = { Text("2. Pair Code", style = MaterialTheme.typography.labelMedium) },
            leadingIcon = {
              Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.weight(1f),
          )
        }

        OutlinedTextField(
          value = hostField,
          onValueChange = { hostField = it },
          label = { Text("Loopback Host") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          isError = !isHostValid,
          leadingIcon = {
            Icon(Icons.Default.Wifi, contentDescription = null)
          },
          trailingIcon = {
            if (hostField.text.isNotEmpty()) {
              IconButton(onClick = { hostField = TextFieldValue("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear host")
              }
            }
          },
          modifier = Modifier.fillMaxWidth(),
        )

        if (!isPairingMode) {
          OutlinedTextField(
            value = connectPortField,
            onValueChange = { connectPortField = it },
            label = { Text("Wireless Debugging Port (e.g. 5555 or 38xxx)") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            isError = !isConnectPortValid,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Number,
              imeAction = ImeAction.Done,
            ),
            leadingIcon = {
              Icon(Icons.Default.Terminal, contentDescription = null)
            },
            trailingIcon = {
              if (connectPortField.text.isNotEmpty()) {
                IconButton(onClick = { connectPortField = TextFieldValue("") }) {
                  Icon(Icons.Default.Clear, contentDescription = "Clear port")
                }
              }
            },
            supportingText = {
              Text(
                text = if (!isConnectPortValid) {
                  "Enter a valid TCP port between 1 and 65535"
                } else {
                  "Shown under Wireless Debugging → IP address & Port"
                },
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .focusRequester(focusRequester)
              .testTag("adb_connect_port_input"),
          )
        } else {
          OutlinedTextField(
            value = pairPortField,
            onValueChange = { pairPortField = it },
            label = { Text("Pairing Port") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            isError = !isPairPortValid,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Number,
              imeAction = ImeAction.Next,
            ),
            leadingIcon = {
              Icon(Icons.Default.Link, contentDescription = null)
            },
            modifier = Modifier
              .fillMaxWidth()
              .focusRequester(focusRequester),
          )

          OutlinedTextField(
            value = pairingCodeField,
            onValueChange = { pairingCodeField = it },
            label = { Text("6-Digit Wi-Fi Pairing Code") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            isError = pairingCodeField.text.isNotEmpty() && !isPairingCodeValid,
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Number,
              imeAction = ImeAction.Done,
            ),
            leadingIcon = {
              Icon(Icons.Default.Key, contentDescription = null)
            },
            trailingIcon = {
              if (pairingCodeField.text.isNotEmpty()) {
                IconButton(onClick = { pairingCodeField = TextFieldValue("") }) {
                  Icon(Icons.Default.Clear, contentDescription = "Clear pairing code")
                }
              }
            },
            supportingText = {
              Text("Tap 'Pair device with pairing code' in Wireless Debugging (split-screen works best)")
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("adb_pairing_code_input"),
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (isPairingMode) {
            onPair(parsedHost, parsedPairPort ?: DEFAULT_TERMINAL_PAIR_PORT, parsedPairingCode)
          } else {
            onConnect(parsedHost, parsedConnectPort ?: DEFAULT_TERMINAL_ADB_PORT)
          }
        },
        enabled = canSubmit,
        shape = CircleShape,
        modifier = Modifier.testTag("action_confirm_adb_dialog"),
      ) {
        Icon(
          imageVector = if (isPairingMode) Icons.Default.Key else Icons.Default.Check,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (isPairingMode) "Pair Device" else "Connect Shell")
      }
    },
    dismissButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (connectionState == AdbConnectionState.CONNECTED) {
          TextButton(
            onClick = {
              onDisconnect()
              onDismiss()
            },
            shape = CircleShape,
          ) {
            Text("Disconnect", color = MaterialTheme.colorScheme.error)
          }
        }
        TextButton(
          onClick = onDismiss,
          shape = CircleShape,
        ) {
          Text("Close")
        }
      }
    },
  )
}

/**
 * Dialog for adjusting the Terminal monospace font size (`10sp`–`20sp`) using `SlimDotSlider`
 * (`DESIGN_LANGUAGE.md` Section 5.2 & 5.6).
 */
@Composable
private fun TerminalFontSizeDialog(
  currentSizeSp: Float,
  onConfirm: (Float) -> Unit,
  onDismiss: () -> Unit,
) {
  var sliderValue by remember { mutableStateOf(currentSizeSp) }
  val isModified = sliderValue.roundToInt() != currentSizeSp.roundToInt()

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
            imageVector = Icons.Default.FormatSize,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Console Font Size",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Adjust monospace scrollback text scale",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
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
            Text(
              text = "/sdcard $ uname -a • ${sliderValue.roundToInt()} sp",
              fontFamily = FontFamily.Monospace,
              fontSize = sliderValue.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = "Linux localhost 6.1.75-android14-11 #1 SMP PREEMPT aarch64",
              fontFamily = FontFamily.Monospace,
              fontSize = sliderValue.sp,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }

        SlimDotSlider(
          value = sliderValue,
          onValueChange = { sliderValue = it.roundToInt().toFloat() },
          valueRange = MIN_TERMINAL_FONT_SIZE_SP..MAX_TERMINAL_FONT_SIZE_SP,
          steps = (MAX_TERMINAL_FONT_SIZE_SP - MIN_TERMINAL_FONT_SIZE_SP).roundToInt() - 1,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(sliderValue) },
        enabled = isModified,
        shape = CircleShape,
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Apply")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = CircleShape,
      ) {
        Text("Cancel")
      }
    },
  )
}

/**
 * Dialog for saving a custom reusable command to the Terminal catalog (`DESIGN_LANGUAGE.md` Section 5.2).
 */
@Composable
private fun SaveCustomCommandDialog(
  initialCommand: String,
  initialRequiresAdb: Boolean,
  onConfirm: (title: String, command: String, description: String, requiresAdb: Boolean) -> Unit,
  onDismiss: () -> Unit,
) {
  var titleField by remember { mutableStateOf(TextFieldValue("")) }
  var commandField by remember {
    mutableStateOf(TextFieldValue(initialCommand, TextRange(0, initialCommand.length)))
  }
  var descriptionField by remember { mutableStateOf(TextFieldValue("")) }
  var requiresAdb by rememberSaveable { mutableStateOf(initialRequiresAdb) }

  val focusRequester = remember { FocusRequester() }
  LaunchedEffect(Unit) {
    delay(80)
    runCatching { focusRequester.requestFocus() }
  }

  val isTitleValid = titleField.text.trim().isNotEmpty()
  val isCommandValid = commandField.text.trim().isNotEmpty()
  val canSave = isTitleValid && isCommandValid

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
            imageVector = Icons.Default.Code,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Save Custom Command",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Add a reusable shell command to your preset catalog",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        OutlinedTextField(
          value = titleField,
          onValueChange = { titleField = it },
          label = { Text("Command Title") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          leadingIcon = { Icon(Icons.Default.Terminal, contentDescription = null) },
          trailingIcon = {
            if (titleField.text.isNotEmpty()) {
              IconButton(onClick = { titleField = TextFieldValue("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear title")
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        )

        OutlinedTextField(
          value = commandField,
          onValueChange = { commandField = it },
          label = { Text("Shell Command") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) },
          trailingIcon = {
            if (commandField.text.isNotEmpty()) {
              IconButton(onClick = { commandField = TextFieldValue("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear command")
              }
            }
          },
          modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
          value = descriptionField,
          onValueChange = { descriptionField = it },
          label = { Text("Description (optional)") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth(),
        )

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Requires Wireless ADB (UID 2000)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "Badge command for elevated ADB shell execution",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Switch(
              checked = requiresAdb,
              onCheckedChange = { requiresAdb = it },
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onConfirm(
            titleField.text.trim(),
            commandField.text.trim(),
            descriptionField.text.trim(),
            requiresAdb,
          )
        },
        enabled = canSave,
        shape = CircleShape,
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Save Preset")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = CircleShape,
      ) {
        Text("Cancel")
      }
    },
  )
}

private fun copyToClipboard(context: Context, label: String, text: String) {
  runCatching {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
  }
}

private fun openDeveloperOptionsSettings(context: Context) {
  try {
    val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
  } catch (_: Throwable) {
    try {
      val fallback = Intent(Settings.ACTION_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(fallback)
    } catch (_: Throwable) {}
  }
}
