package com.example.toolbox.ui.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import com.example.toolbox.ui.theme.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AddToHomeScreen
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Texture
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.toolbox.ui.main.ToolCategory
import com.example.toolbox.ui.main.addToolToHomeScreen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toolbox.ui.main.ACCENT_COLOR_MUSIC_PLAYER
import com.example.toolbox.ui.main.AppPermission
import com.example.toolbox.ui.main.BackgroundTexture
import com.example.toolbox.ui.main.CategorySortingOption
import com.example.toolbox.ui.main.CategoryToolSortingOption
import com.example.toolbox.ui.main.DEFAULT_ACCENT_COLOR_AUTO
import com.example.toolbox.ui.main.DEFAULT_BACKGROUND_TEXTURE
import com.example.toolbox.ui.main.DEFAULT_DATA_BACKUP_FILENAME
import com.example.toolbox.ui.main.DEFAULT_SETTINGS_BACKUP_FILENAME
import com.example.toolbox.ui.main.DEFAULT_THEME_COLOR_SYSTEM
import com.example.toolbox.ui.main.ErrorLogEntry
import com.example.toolbox.ui.main.ErrorLogManager
import com.example.toolbox.ui.main.FALLBACK_THEME_COLOR_HEX
import com.example.toolbox.ui.main.InMemoryNavigationPreferencesRepository
import com.example.toolbox.ui.main.MAX_ERROR_LOG_ENTRIES
import com.example.toolbox.ui.main.MAX_TOOLS_PER_ROW
import com.example.toolbox.ui.main.MIN_TOOLS_PER_ROW
import com.example.toolbox.ui.main.NavigationPreferencesRepository
import com.example.toolbox.ui.main.PermissionToolUsageStat
import com.example.toolbox.ui.main.ThemeMode
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.ToolSortOrder
import com.example.toolbox.ui.main.ToolSortingOption
import com.example.toolbox.ui.main.coerceToolsPerRow
import com.example.toolbox.ui.main.exportDataToJson
import com.example.toolbox.ui.main.exportSettingsToJson
import com.example.toolbox.ui.main.formatPermissionUsageTimestamp
import com.example.toolbox.ui.main.importDataFromJson
import com.example.toolbox.ui.main.importSettingsFromJson
import com.example.toolbox.ui.main.rememberNavigationPreferencesRepository
import com.example.toolbox.ui.main.resetAllSettingsToDefaults
import com.example.toolbox.ui.theme.AccentHarmonyOption
import com.example.toolbox.ui.theme.BackgroundTexturePreviewBox
import com.example.toolbox.ui.theme.HexagonShape
import com.example.toolbox.ui.theme.MusicPlayerColorOption
import com.example.toolbox.ui.theme.calculateAccentHarmonyColor
import com.example.toolbox.ui.theme.colorToHexString
import com.example.toolbox.ui.theme.isAutoAccentColorSetting
import com.example.toolbox.ui.theme.isMusicPlayerThemeColorSetting
import com.example.toolbox.ui.theme.parseHexColor
import com.example.toolbox.ui.theme.toolboxScreenBackground
import kotlin.math.roundToInt
import kotlin.random.Random

const val FEEDBACK_DEVIL_ANGEL_EASTER_EGG_CHANCE = 0.01f

val DevilFeedbackIcon: ImageVector by lazy {
  ImageVector.Builder(
    name = "SettingsDevilFeedback",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
  ).apply {
    // Left horn
    path(fill = SolidColor(Color.Black)) {
      moveTo(7.4f, 8.2f)
      curveTo(5.6f, 6.2f, 4.4f, 3.8f, 4.8f, 2.2f)
      curveTo(6.6f, 3.0f, 8.4f, 4.8f, 9.6f, 6.8f)
      close()
    }
    // Right horn
    path(fill = SolidColor(Color.Black)) {
      moveTo(16.6f, 8.2f)
      curveTo(18.4f, 6.2f, 19.6f, 3.8f, 19.2f, 2.2f)
      curveTo(17.4f, 3.0f, 15.6f, 4.8f, 14.4f, 6.8f)
      close()
    }
    // Devil head with slanted eyes and mischievous grin cutouts
    path(
      fill = SolidColor(Color.Black),
      pathFillType = PathFillType.EvenOdd,
    ) {
      moveTo(12f, 6f)
      curveTo(16.42f, 6f, 20f, 9.58f, 20f, 14f)
      curveTo(20f, 18.42f, 16.42f, 22f, 12f, 22f)
      curveTo(7.58f, 22f, 4f, 18.42f, 4f, 14f)
      curveTo(4f, 9.58f, 7.58f, 6f, 12f, 6f)
      close()
      // Left slanted eye
      moveTo(7.6f, 11.4f)
      lineTo(10.8f, 12.8f)
      lineTo(10.0f, 14.2f)
      lineTo(7.2f, 13.0f)
      close()
      // Right slanted eye
      moveTo(16.4f, 11.4f)
      lineTo(16.8f, 13.0f)
      lineTo(14.0f, 14.2f)
      lineTo(13.2f, 12.8f)
      close()
      // Mischievous grin
      moveTo(8.2f, 16.4f)
      curveTo(9.4f, 18.2f, 14.6f, 18.2f, 15.8f, 16.4f)
      lineTo(16.8f, 17.4f)
      curveTo(15.0f, 19.8f, 9.0f, 19.8f, 7.2f, 17.4f)
      close()
    }
  }.build()
}

val AngelFeedbackIcon: ImageVector by lazy {
  ImageVector.Builder(
    name = "SettingsAngelFeedback",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
  ).apply {
    // Halo ring above head
    path(
      fill = SolidColor(Color.Black),
      pathFillType = PathFillType.EvenOdd,
    ) {
      moveTo(12f, 1.5f)
      curveTo(16.4f, 1.5f, 19.5f, 2.5f, 19.5f, 3.8f)
      curveTo(19.5f, 5.1f, 16.4f, 6.1f, 12f, 6.1f)
      curveTo(7.6f, 6.1f, 4.5f, 5.1f, 4.5f, 3.8f)
      curveTo(4.5f, 2.5f, 7.6f, 1.5f, 12f, 1.5f)
      close()
      moveTo(12f, 2.9f)
      curveTo(9.0f, 2.9f, 6.8f, 3.3f, 6.8f, 3.8f)
      curveTo(6.8f, 4.3f, 9.0f, 4.7f, 12f, 4.7f)
      curveTo(15.0f, 4.7f, 17.2f, 4.3f, 17.2f, 3.8f)
      curveTo(17.2f, 3.3f, 15.0f, 2.9f, 12f, 2.9f)
      close()
    }
    // Angel head with gentle eyes and smile cutouts
    path(
      fill = SolidColor(Color.Black),
      pathFillType = PathFillType.EvenOdd,
    ) {
      moveTo(12f, 7f)
      curveTo(16.14f, 7f, 19.5f, 10.36f, 19.5f, 14.5f)
      curveTo(19.5f, 18.64f, 16.14f, 22f, 12f, 22f)
      curveTo(7.86f, 22f, 4.5f, 18.64f, 4.5f, 14.5f)
      curveTo(4.5f, 10.36f, 7.86f, 7f, 12f, 7f)
      close()
      // Left eye
      moveTo(8.0f, 13.2f)
      arcToRelative(1.2f, 1.2f, 0f, true, true, 2.4f, 0f)
      arcToRelative(1.2f, 1.2f, 0f, true, true, -2.4f, 0f)
      close()
      // Right eye
      moveTo(13.6f, 13.2f)
      arcToRelative(1.2f, 1.2f, 0f, true, true, 2.4f, 0f)
      arcToRelative(1.2f, 1.2f, 0f, true, true, -2.4f, 0f)
      close()
      // Gentle smile
      moveTo(8.6f, 16.6f)
      curveTo(9.8f, 18.2f, 14.2f, 18.2f, 15.4f, 16.6f)
      lineTo(16.5f, 17.5f)
      curveTo(14.8f, 19.6f, 9.2f, 19.6f, 7.5f, 17.5f)
      close()
    }
  }.build()
}

fun shouldUseDevilAndAngelFeedbackIcons(
  random: Random = Random.Default,
): Boolean = random.nextFloat() < FEEDBACK_DEVIL_ANGEL_EASTER_EGG_CHANCE

fun resolveReportBugButtonIcon(useDevilAndAngelIcons: Boolean): ImageVector =
  if (useDevilAndAngelIcons) DevilFeedbackIcon else Icons.Default.BugReport

fun resolveRequestFeatureButtonIcon(useDevilAndAngelIcons: Boolean): ImageVector =
  if (useDevilAndAngelIcons) AngelFeedbackIcon else Icons.Default.Lightbulb

data class ColorPreset(
  val name: String,
  val hex: String,
  val color: Color,
  val isFallbackDefault: Boolean = false,
)

val PRESET_COLORS = listOf(
  ColorPreset("Purple (Default)", FALLBACK_THEME_COLOR_HEX, Color(0xFF8E66A6), isFallbackDefault = true),
  ColorPreset("Ocean Blue", "#00668B", Color(0xFF00668B)),
  ColorPreset("Forest Green", "#386A20", Color(0xFF386A20)),
  ColorPreset("Amber Orange", "#E65100", Color(0xFFE65100)),
  ColorPreset("Crimson Red", "#B3261E", Color(0xFFB3261E)),
  ColorPreset("Teal", "#006A60", Color(0xFF006A60)),
  ColorPreset("Rose Pink", "#984061", Color(0xFF984061)),
  ColorPreset("Indigo", "#3F51B5", Color(0xFF3F51B5)),
)

val ACCENT_PRESET_COLORS = listOf(
  ColorPreset("Amber Orange", "#E65100", Color(0xFFE65100)),
  ColorPreset("Ocean Blue", "#00668B", Color(0xFF00668B)),
  ColorPreset("Forest Green", "#386A20", Color(0xFF386A20)),
  ColorPreset("Crimson Red", "#B3261E", Color(0xFFB3261E)),
  ColorPreset("Teal", "#006A60", Color(0xFF006A60)),
  ColorPreset("Rose Pink", "#984061", Color(0xFF984061)),
  ColorPreset("Purple", FALLBACK_THEME_COLOR_HEX, Color(0xFF8E66A6)),
  ColorPreset("Indigo", "#3F51B5", Color(0xFF3F51B5)),
  ColorPreset("Coral", "#FF6B57", Color(0xFFFF6B57)),
  ColorPreset("Gold", "#FFA000", Color(0xFFFFA000)),
)

const val GITHUB_NEW_ISSUE_URL = "https://github.com/Knoir-0/toolbox/issues/new"
const val GITHUB_REPORT_BUG_URL = GITHUB_NEW_ISSUE_URL
const val GITHUB_REQUEST_FEATURE_URL = GITHUB_NEW_ISSUE_URL

fun openExternalUrl(context: Context, url: String) {
  try {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
  } catch (e: Exception) {
    ErrorLogManager.logError(
      throwable = e,
      source = "Settings",
      customMessage = "Unable to open external link: $url",
      context = context,
    )
    Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
  }
}

fun openAppPermissionSystemSettings(context: Context, permission: AppPermission) {
  try {
    when (permission) {
      AppPermission.NOTIFICATION_LISTENER -> {
        MusicPlayerEngine.openSystemMediaListenerSettings(context)
        return
      }
      AppPermission.STORAGE -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
          context.startActivity(intent)
          return
        }
      }
      AppPermission.SCHEDULE_EXACT_ALARM -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
          context.startActivity(intent)
          return
        }
      }
      else -> {}
    }
    val appDetailsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
      data = Uri.parse("package:${context.packageName}")
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(appDetailsIntent)
  } catch (e: Throwable) {
    ErrorLogManager.logError(
      throwable = e,
      source = "Settings",
      customMessage = "Unable to open system permission settings for ${permission.title}",
      context = context,
    )
    Toast.makeText(context, "Unable to open system permission settings", Toast.LENGTH_SHORT).show()
  }
}

fun filterErrorLogs(
  logs: List<ErrorLogEntry>,
  query: String,
): List<ErrorLogEntry> {
  val trimmed = query.trim()
  if (trimmed.isEmpty()) return logs
  return logs.filter { entry ->
    entry.message.contains(trimmed, ignoreCase = true) ||
      entry.source.contains(trimmed, ignoreCase = true) ||
      entry.details.contains(trimmed, ignoreCase = true) ||
      entry.formattedTimestamp.contains(trimmed, ignoreCase = true)
  }
}

fun highlightErrorLogText(
  text: String,
  query: String,
  highlightBackground: Color,
  highlightContent: Color,
): AnnotatedString {
  val trimmed = query.trim()
  if (trimmed.isEmpty() || text.isEmpty()) {
    return AnnotatedString(text)
  }
  return buildAnnotatedString {
    append(text)
    var searchStart = 0
    while (searchStart < text.length) {
      val matchIndex = text.indexOf(trimmed, startIndex = searchStart, ignoreCase = true)
      if (matchIndex < 0) break
      val matchEnd = (matchIndex + trimmed.length).coerceAtMost(text.length)
      addStyle(
        style = SpanStyle(
          background = highlightBackground,
          color = highlightContent,
          fontWeight = FontWeight.Bold,
        ),
        start = matchIndex,
        end = matchEnd,
      )
      searchStart = matchEnd
    }
  }
}

fun copyErrorLogEntryToClipboard(context: Context, entry: ErrorLogEntry): String {
  val formatted = entry.toFormattedText()
  try {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("Toolbox Error Log", formatted))
    Toast.makeText(context, "Error copied to clipboard", Toast.LENGTH_SHORT).show()
  } catch (_: Exception) {}
  return formatted
}

@Composable
fun ErrorLogsFullScreenView(
  errorLogs: List<ErrorLogEntry>,
  onNavigateBack: () -> Unit,
  onClearErrorLogs: () -> Unit,
  modifier: Modifier = Modifier,
  onCopyEntry: ((ErrorLogEntry, String) -> Unit)? = null,
) {
  val context = LocalContext.current
  var isSearchActive by rememberSaveable { mutableStateOf(false) }
  var searchQuery by rememberSaveable { mutableStateOf("") }

  val recentErrorLogs = remember(errorLogs) {
    errorLogs.takeLast(MAX_ERROR_LOG_ENTRIES).asReversed()
  }
  val filteredErrorLogs = remember(recentErrorLogs, searchQuery) {
    filterErrorLogs(recentErrorLogs, searchQuery)
  }

  BackHandler {
    if (isSearchActive) {
      isSearchActive = false
      searchQuery = ""
    } else {
      onNavigateBack()
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .toolboxScreenBackground(MaterialTheme.colorScheme.background)
      .testTag("error_logs_screen"),
  ) {
    ToolboxTopBar(
      title = "Error Logs",
      subtext = if (searchQuery.isNotBlank()) {
        "${filteredErrorLogs.size} of ${recentErrorLogs.size} errors matching"
      } else {
        "${recentErrorLogs.size} of $MAX_ERROR_LOG_ENTRIES errors recorded"
      },
      icon = Icons.Default.BugReport,
      onNavigateBack = {
        isSearchActive = false
        searchQuery = ""
        onNavigateBack()
      },
      backContentDescription = "Back",
      backButtonTestTag = "action_navigate_back",
      additionalBackButtonTestTag = "error_logs_back_button",
      titleTestTag = "error_logs_title",
      subtextTestTag = "error_logs_count_text",
      searchState = ToolboxTopBarSearchState(
        isActive = isSearchActive,
        query = searchQuery,
        onQueryChange = { searchQuery = it },
        onOpenSearch = { isSearchActive = true },
        onCloseSearch = {
          isSearchActive = false
          searchQuery = ""
        },
        onClearQuery = { searchQuery = "" },
        placeholder = "Search error logs...",
      ),
    )

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 16.dp)
        .testTag("error_logs_list"),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (filteredErrorLogs.isEmpty()) {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("error_logs_empty_state"),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text(
              text = if (recentErrorLogs.isEmpty()) "No errors logged" else "No matching errors",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = if (recentErrorLogs.isEmpty()) {
                "Up to the last $MAX_ERROR_LOG_ENTRIES errors encountered in Toolbox will appear here."
              } else {
                "No error logs match \"${searchQuery.trim()}\"."
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      } else {
        val highlightBackground = MaterialTheme.colorScheme.primaryContainer
        val highlightContent = MaterialTheme.colorScheme.onPrimaryContainer

        filteredErrorLogs.forEachIndexed { index, entry ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("error_log_card_$index"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
            border = BorderStroke(
              1.dp,
              MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .testTag("error_log_entry_$index"),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                ) {
                  Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                  ) {
                    Text(
                      text = entry.source,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.SemiBold,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                  }
                  Text(
                    text = entry.formattedTimestamp,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }

                OutlinedButton(
                  onClick = {
                    val copiedText = copyErrorLogEntryToClipboard(context, entry)
                    onCopyEntry?.invoke(entry, copiedText)
                  },
                  shape = CircleShape,
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier
                    .height(30.dp)
                    .testTag("copy_error_log_button_$index"),
                ) {
                  Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy error",
                    modifier = Modifier.size(14.dp),
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Copy",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                  )
                }
              }

              SelectionContainer(
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("error_log_selectable_text_$index"),
              ) {
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  Text(
                    text = highlightErrorLogText(
                      text = entry.message,
                      query = searchQuery,
                      highlightBackground = highlightBackground,
                      highlightContent = highlightContent,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("error_log_message_$index"),
                  )
                  if (entry.details.isNotBlank()) {
                    Text(
                      text = highlightErrorLogText(
                        text = entry.details,
                        query = searchQuery,
                        highlightBackground = highlightBackground,
                        highlightContent = highlightContent,
                      ),
                      style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                      fontFamily = FontFamily.Monospace,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.testTag("error_log_details_$index"),
                    )
                  }
                }
              }
            }
          }
        }
      }

      if (recentErrorLogs.isNotEmpty()) {
        OutlinedButton(
          onClick = onClearErrorLogs,
          shape = CircleShape,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("clear_error_logs_button"),
        ) {
          Icon(
            imageVector = Icons.Default.RestartAlt,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("Clear Error Logs")
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

const val TOOLBOX_LICENSE_TITLE = "GNU General Public License v3.0"

const val TOOLBOX_LICENSE_TEXT = """                    GNU GENERAL PUBLIC LICENSE
                       Version 3, 29 June 2007

 Copyright (C) 2007 Free Software Foundation, Inc. <https://fsf.org/>
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.

                            Preamble

  The GNU General Public License is a free, copyleft license for
software and other kinds of works.

  The licenses for most software and other practical works are designed
to take away your freedom to share and change the works.  By contrast,
the GNU General Public License is intended to guarantee your freedom to
share and change all versions of a program--to make sure it remains free
software for all its users.  We, the Free Software Foundation, use the
GNU General Public License for most of our software; it applies also to
any other work released this way by its authors.  You can apply it to
your programs, too.

  When we speak of free software, we are referring to freedom, not
price.  Our General Public Licenses are designed to make sure that you
have the freedom to distribute copies of free software (and charge for
them if you wish), that you receive source code or can get it if you
want it, that you can change the software or use pieces of it in new
free programs, and that you know you can do these things.

  To protect your rights, we need to prevent others from denying you
these rights or asking you to surrender the rights.  Therefore, you have
certain responsibilities if you distribute copies of the software, or if
you modify it: responsibilities to respect the freedom of others.

  For example, if you distribute copies of such a program, whether
gratis or for a fee, you must pass on to the recipients the same
freedoms that you received.  You must make sure that they, too, receive
or can get the source code.  And you must show them these terms so they
know their rights.

  Developers that use the GNU GPL protect your rights with two steps:
(1) assert copyright on the software, and (2) offer you this License
giving you legal permission to copy, distribute and/or modify it.

  For the developers' and authors' protection, the GPL clearly explains
that there is no warranty for this free software.  For both users' and
authors' sake, the GPL requires that modified versions be marked as
changed, so that their problems will not be attributed erroneously to
authors of previous versions.

  Some devices are designed to deny users access to install or run
modified versions of the software inside them, although the manufacturer
can do so.  This is fundamentally incompatible with the aim of
protecting users' freedom to change the software.  The systematic
pattern of such abuse occurs in the area of products for individuals to
use, which is precisely where it is most unacceptable.  Therefore, we
have designed this version of the GPL to prohibit the practice for those
products.  If such problems arise substantially in other domains, we
stand ready to extend this provision to those domains in future versions
of the GPL, as needed to protect the freedom of users.

  Finally, every program is threatened constantly by software patents.
States should not allow patents to restrict development and use of
software on general-purpose computers, but in those that do, we wish to
avoid the special danger that patents applied to a free program could
make it effectively proprietary.  To prevent this, the GPL assures that
patents cannot be used to render the program non-free.

  The precise terms and conditions for copying, distribution and
modification follow.

                       TERMS AND CONDITIONS

  0. Definitions.

  "This License" refers to version 3 of the GNU General Public License.

  "Copyright" also means copyright-like laws that apply to other kinds of
works, such as semiconductor masks.

  "The Program" refers to any copyrightable work licensed under this
License.  Each licensee is addressed as "you".  "Licensees" and
"recipients" may be individuals or organizations.

  To "modify" a work means to copy from or adapt all or part of the work
in a fashion requiring copyright permission, other than the making of an
exact copy.  The resulting work is called a "modified version" of the
earlier work or a work "based on" the earlier work.

  A "covered work" means either the unmodified Program or a work based
on the Program.

  To "propagate" a work means to do anything with it that, without
permission, would make you directly or secondarily liable for
infringement under applicable copyright law, except executing it on a
computer or modifying a private copy.  Propagation includes copying,
distribution (with or without modification), making available to the
public, and in some countries other activities as well.

  To "convey" a work means any kind of propagation that enables other
parties to make or receive copies.  Mere interaction with a user through
a computer network, with no transfer of a copy, is not conveying.

  An interactive user interface displays "Appropriate Legal Notices"
to the extent that it includes a convenient and prominently visible
feature that (1) displays an appropriate copyright notice, and (2)
tells the user that there is no warranty for the work (except to the
extent that warranties are provided), that licensees may convey the
work under this License, and how to view a copy of this License.  If
the interface presents a list of user commands or options, such as a
menu, a prominent item in the list meets this criterion.

  1. Source Code.

  The "source code" for a work means the preferred form of the work
for making modifications to it.  "Object code" means any non-source
form of a work.

  A "Standard Interface" means an interface that either is an official
standard defined by a recognized standards body, or, in the case of
interfaces specified for a particular programming language, one that
is widely used among developers working in that language.

  The "System Libraries" of an executable work include anything, other
than the work as a whole, that (a) is included in the normal form of
packaging a Major Component, but which is not part of that Major
Component, and (b) serves only to enable use of the work with that
Major Component, or to implement a Standard Interface for which an
implementation is available to the public in source code form.  A
"Major Component", in this context, means a major essential component
(kernel, window system, and so on) of the specific operating system
(if any) on which the executable work runs, or a compiler used to
produce the work, or an object code interpreter used to run it.

  The "Corresponding Source" for a work in object code form means all
the source code needed to generate, install, and (for an executable
work) run the object code and to modify the work, including scripts to
control those activities.  However, it does not include the work's
System Libraries, or general-purpose tools or generally available free
programs which are used unmodified in performing those activities but
which are not part of the work.  For example, Corresponding Source
includes interface definition files associated with source files for
the work, and the source code for shared libraries and dynamically
linked subprograms that the work is specifically designed to require,
such as by intimate data communication or control flow between those
subprograms and other parts of the work.

  The Corresponding Source need not include anything that users
can regenerate automatically from other parts of the Corresponding
Source.

  The Corresponding Source for a work in source code form is that
same work.

  2. Basic Permissions.

  All rights granted under this License are granted for the term of
copyright on the Program, and are irrevocable provided the stated
conditions are met.  This License explicitly affirms your unlimited
permission to run the unmodified Program.  The output from running a
covered work is covered by this License only if the output, given its
content, constitutes a covered work.  This License acknowledges your
rights of fair use or other equivalent, as provided by copyright law.

  You may make, run and propagate covered works that you do not
convey, without conditions so long as your license otherwise remains
in force.  You may convey covered works to others for the sole purpose
of having them make modifications exclusively for you, or provide you
with facilities for running those works, provided that you comply with
the terms of this License in conveying all material for which you do
not control copyright.  Those thus making or running the covered works
for you must do so exclusively on your behalf, under your direction
and control, on terms that prohibit them from making any copies of
your copyrighted material outside their relationship with you.

  Conveying under any other circumstances is permitted solely under
the conditions stated below.  Sublicensing is not allowed; section 10
makes it unnecessary.

  3. Protecting Users' Legal Rights From Anti-Circumvention Law.

  No covered work shall be deemed part of an effective technological
measure under any applicable law fulfilling obligations under article
11 of the WIPO copyright treaty adopted on 20 December 1996, or
similar laws prohibiting or restricting circumvention of such
measures.

  When you convey a covered work, you waive any legal power to forbid
circumvention of technological measures to the extent such circumvention
is effected by exercising rights under this License with respect to
the covered work, and you disclaim any intention to limit operation or
modification of the work as a means of enforcing, against the work's
users, your or third parties' legal rights to forbid circumvention of
technological measures.

  4. Conveying Verbatim Copies.

  You may convey verbatim copies of the Program's source code as you
receive it, in any medium, provided that you conspicuously and
appropriately publish on each copy an appropriate copyright notice;
keep intact all notices stating that this License and any
non-permissive terms added in accord with section 7 apply to the code;
keep intact all notices of the absence of any warranty; and give all
recipients a copy of this License along with the Program.

  You may charge any price or no price for each copy that you convey,
and you may offer support or warranty protection for a fee.

  5. Conveying Modified Source Versions.

  You may convey a work based on the Program, or the modifications to
produce it from the Program, in the form of source code under the
terms of section 4, provided that you also meet all of these conditions:

    a) The work must carry prominent notices stating that you modified
    it, and giving a relevant date.

    b) The work must carry prominent notices stating that it is
    released under this License and any conditions added under section
    7.  This requirement modifies the requirement in section 4 to
    "keep intact all notices".

    c) You must license the entire work, as a whole, under this
    License to anyone who comes into possession of a copy.  This
    License will therefore apply, along with any applicable section 7
    additional terms, to the whole of the work, and all its parts,
    regardless of how they are packaged.  This License gives no
    permission to license the work in any other way, but it does not
    invalidate such permission if you have separately received it.

    d) If the work has interactive user interfaces, each must display
    Appropriate Legal Notices; however, if the Program has interactive
    interfaces that do not display Appropriate Legal Notices, your
    work need not make them do so.

  A compilation of a covered work with other separate and independent
works, which are not by their nature extensions of the covered work,
and which are not combined with it such as to form a larger program,
in or on a volume of a storage or distribution medium, is called an
"aggregate" if the compilation and its resulting copyright are not
used to limit the access or legal rights of the compilation's users
beyond what the individual works permit.  Inclusion of a covered work
in an aggregate does not cause this License to apply to the other
parts of the aggregate.

  6. Conveying Non-Source Forms.

  You may convey a covered work in object code form under the terms
of sections 4 and 5, provided that you also convey the
machine-readable Corresponding Source under the terms of this License,
in one of these ways:

    a) Convey the object code in, or embodied in, a physical product
    (including a physical distribution medium), accompanied by the
    Corresponding Source fixed on a durable physical medium
    customarily used for software interchange.

    b) Convey the object code in, or embodied in, a physical product
    (including a physical distribution medium), accompanied by a
    written offer, valid for at least three years and valid for as
    long as you offer spare parts or customer support for that product
    model, to give anyone who possesses the object code either (1) a
    copy of the Corresponding Source for all the software in the
    product that is covered by this License, on a durable physical
    medium customarily used for software interchange, for a price no
    more than your reasonable cost of physically performing this
    conveying of source, or (2) access to copy the
    Corresponding Source from a network server at no charge.

    c) Convey individual copies of the object code with a copy of the
    written offer to provide the Corresponding Source.  This
    alternative is allowed only occasionally and noncommercially, and
    only if you received the object code with such an offer, in accord
    with subsection 6b.

    d) Convey the object code by offering access from a designated
    place (gratis or for a charge), and offer equivalent access to the
    Corresponding Source in the same way through the same place at no
    further charge.  You need not require recipients to copy the
    Corresponding Source along with the object code.  If the place to
    copy the object code is a network server, the Corresponding Source
    may be on a different server (operated by you or a third party)
    that supports equivalent copying facilities, provided you maintain
    clear directions next to the object code saying where to find the
    Corresponding Source.  Regardless of what server hosts the
    Corresponding Source, you remain obligated to ensure that it is
    available for as long as needed to satisfy these requirements.

    e) Convey the object code using peer-to-peer transmission, provided
    you inform other peers where the object code and Corresponding
    Source of the work are being offered to the general public at no
    charge under subsection 6d.

  A separable portion of the object code, whose source code is excluded
from the Corresponding Source as a System Library, need not be
included in conveying the object code work.

  A "User Product" is either (1) a "consumer product", which means any
tangible personal property which is normally used for personal, family,
or household purposes, or (2) anything designed or sold for incorporation
into a dwelling.  In determining whether a product is a consumer product,
doubtful cases shall be resolved in favor of coverage.  For a particular
product received by a particular user, "normally used" refers to a
typical or common use of that class of product, regardless of the status
of the particular user or of the way in which the particular user
actually uses, or expects or is expected to use, the product.  A product
is a consumer product regardless of whether the product has substantial
commercial, industrial or non-consumer uses, unless such uses represent
the only significant mode of use of the product.

  "Installation Information" for a User Product means any methods,
procedures, authorization keys, or other information required to install
and execute modified versions of a covered work in that User Product from
a modified version of its Corresponding Source.  The information must
suffice to ensure that the continued functioning of the modified object
code is in no case prevented or interfered with solely because
modification has been made.

  If you convey an object code work under this section in, or with, or
specifically for use in, a User Product, and the conveying occurs as
part of a transaction in which the right of possession and use of the
User Product is transferred to the recipient in perpetuity or for a
fixed term (regardless of how the transaction is characterized), the
Corresponding Source conveyed under this section must be accompanied
by the Installation Information.  But this requirement does not apply
if neither you nor any third party retains the ability to install
modified object code on the User Product (for example, the work has
been installed in ROM).

  The requirement to provide Installation Information does not include a
requirement to continue to provide support service, warranty, or updates
for a work that has been modified or installed by the recipient, or for
the User Product in which it has been modified or installed.  Access to a
network may be denied when the modification itself materially and
adversely affects the operation of the network or violates the rules and
protocols for communication across the network.

  Corresponding Source conveyed, and Installation Information provided,
in accord with this section must be in a format that is publicly
documented (and with an implementation available to the public in
source code form), and must require no special password or key for
unpacking, reading or copying.

  7. Additional Terms.

  "Additional permissions" are terms that supplement the terms of this
License by making exceptions from one or more of its conditions.
Additional permissions that are applicable to the entire Program shall
be treated as though they were included in this License, to the extent
that they are valid under applicable law.  If additional permissions
apply only to part of the Program, that part may be used separately
under those permissions, but the entire Program remains governed by
this License without regard to the additional permissions.

  When you convey a copy of a covered work, you may at your option
remove any additional permissions from that copy, or from any part of
it.  (Additional permissions may be written to require their own
removal in certain cases when you modify the work.)  You may place
additional permissions on material, added by you to a covered work,
for which you have or can give appropriate copyright permission.

  Notwithstanding any other provision of this License, for material you
add to a covered work, you may (if authorized by the copyright holders of
that material) supplement the terms of this License with terms:

    a) Disclaiming warranty or limiting liability differently from the
    terms of sections 15 and 16 of this License; or

    b) Requiring preservation of specified reasonable legal notices or
    author attributions in that material or in the Appropriate Legal
    Notices displayed by works containing it; or

    c) Prohibiting misrepresentation of the origin of that material, or
    requiring that modified versions of such material be marked in
    reasonable ways as different from the original version; or

    d) Limiting the use for publicity purposes of names of licensors or
    authors of the material; or

    e) Declining to grant rights under trademark law for use of some
    trade names, trademarks, or service marks; or

    f) Requiring indemnification of licensors and authors of that
    material by anyone who conveys the material (or modified versions of
    it) with contractual assumptions of liability to the recipient, for
    any liability that these contractual assumptions directly impose on
    those licensors and authors.

  All other non-permissive additional terms are considered "further
restrictions" within the meaning of section 10.  If the Program as you
received it, or any part of it, contains a notice stating that it is
governed by this License along with a term that is a further
restriction, you may remove that term.  If a license document contains
a further restriction but permits relicensing or conveying under this
License, you may add to a covered work material governed by the terms
of that license document, provided that the further restriction does
not survive such relicensing or conveying.

  If you add terms to a covered work in accord with this section, you
must place, in the relevant source files, a statement of the
additional terms that apply to those files, or a notice indicating
where to find the applicable terms.

  Additional terms, permissive or non-permissive, may be stated in the
form of a separately written license, or stated as exceptions;
the above requirements apply either way.

  8. Termination.

  You may not propagate or modify a covered work except as expressly
provided under this License.  Any attempt otherwise to propagate or
modify it is void, and will automatically terminate your rights under
this License (including any patent licenses granted under the third
paragraph of section 11).

  However, if you cease all violation of this License, then your
license from a particular copyright holder is reinstated (a)
provisionally, unless and until the copyright holder explicitly and
finally terminates your license, and (b) permanently, if the copyright
holder fails to notify you of the violation by some reasonable means
prior to 60 days after the cessation.

  Moreover, your license from a particular copyright holder is
reinstated permanently if the copyright holder notifies you of the
violation by some reasonable means, this is the first time you have
received notice of violation of this License (for any work) from that
copyright holder, and you cure the violation prior to 30 days after
your receipt of the notice.

  Termination of your rights under this section does not terminate the
licenses of parties who have received copies or rights from you under
this License.  If your rights have been terminated and not permanently
reinstated, you do not qualify to receive new licenses for the same
material under section 10.

  9. Acceptance Not Required for Having Copies.

  You are not required to accept this License in order to receive or
run a copy of the Program.  Ancillary propagation of a covered work
occurring solely as a consequence of using peer-to-peer transmission
to receive a copy likewise does not require acceptance.  However,
nothing other than this License grants you permission to propagate or
modify any covered work.  These actions infringe copyright if you do
not accept this License.  Therefore, by modifying or propagating a
covered work, you indicate your acceptance of this License to do so.

  10. Automatic Licensing of Downstream Recipients.

  Each time you convey a covered work, the recipient automatically
receives a license from the original licensors, to run, modify and
propagate that work, subject to this License.  You are not responsible
for enforcing compliance by third parties with this License.

  An "entity transaction" is a transaction transferring control of an
organization, or substantially all assets of one, or subdividing an
organization, or merging organizations.  If propagation of a covered
work results from an entity transaction, each party to that
transaction who receives a copy of the work also receives whatever
licenses to the work the party's predecessor in interest had or could
give under the previous paragraph, plus a right to possession of the
Corresponding Source of the work from the predecessor in interest, if
the predecessor has it or can get it with reasonable efforts.

  You may not impose any further restrictions on the exercise of the
rights granted or affirmed under this License.  For example, you may
not impose a license fee, royalty, or other charge for exercise of
rights granted under this License, and you may not initiate litigation
(including a cross-claim or counterclaim in a lawsuit) alleging that
any patent claim is infringed by making, using, selling, offering for
sale, or importing the Program or any portion of it.

  11. Patents.

  A "contributor" is a copyright holder who authorizes use under this
License of the Program or a work on which the Program is based.  The
work thus licensed is called the contributor's "contributor version".

  A contributor's "essential patent claims" are all patent claims
owned or controlled by the contributor, whether already acquired or
hereafter acquired, that would be infringed by some manner, permitted
by this License, of making, using, or selling its contributor version,
but do not include claims that would be infringed only as a
consequence of further modification of the contributor version.  For
purposes of this definition, "control" includes the right to grant
patent sublicenses in a manner consistent with the requirements of
this License.

  Each contributor grants you a non-exclusive, worldwide, royalty-free
patent license under the contributor's essential patent claims, to
make, use, sell, offer for sale, import and otherwise run, modify and
propagate the contents of its contributor version.

  In the following three paragraphs, a "patent license" is any express
agreement or commitment, however denominated, not to enforce a patent
(such as an express permission to practice a patent or covenant not to
sue for patent infringement).  To "grant" such a patent license to a
party means to make such an agreement or commitment not to enforce a
patent against the party.

  If you convey a covered work, knowingly relying on a patent license,
and the Corresponding Source of the work is not available for anyone
to copy, free of charge and under the terms of this License, through a
publicly available network server or other readily accessible means,
then you must either (1) cause the Corresponding Source to be so
available, or (2) arrange to deprive yourself of the benefit of the
patent license for this particular work, or (3) arrange, in a manner
consistent with the requirements of this License, to extend the patent
license to downstream recipients.  "Knowingly relying" means you have
actual knowledge that, but for the patent license, your conveying the
covered work in a country, or your recipient's use of the covered work
in a country, would infringe one or more identifiable patents in that
country that you have reason to believe are valid.

  If, pursuant to or in connection with a single transaction or
arrangement, you convey, or propagate by procuring conveyance of, a
covered work, and grant a patent license to some of the parties
receiving the covered work authorizing them to use, propagate, modify
or convey a specific copy of the covered work, then the patent license
you grant is automatically extended to all recipients of the covered
work and works based on it.

  A patent license is "discriminatory" if it does not include within
the scope of its coverage, prohibits the exercise of, or is
conditioned on the non-exercise of one or more of the rights that are
specifically granted under this License.  You may not convey a covered
work if you are a party to an arrangement with a third party that is
in the business of distributing software, under which you make payment
to the third party based on the extent of your activity of conveying
the work, and under which the third party grants, to any of the
parties who would receive the covered work from you, a discriminatory
patent license (a) in connection with copies of the covered work
conveyed by you (or copies made from those copies), or (b) primarily
for and in connection with specific products or compilations that
contain the covered work, unless you entered into that arrangement,
or that patent license was granted, prior to 28 March 2007.

  Nothing in this License shall be construed as excluding or limiting
any implied license or other defenses to infringement that may
otherwise be available to you under applicable patent law.

  12. No Surrender of Others' Freedom.

  If conditions are imposed on you (whether by court order, agreement or
otherwise) that contradict the conditions of this License, they do not
excuse you from the conditions of this License.  If you cannot convey a
covered work so as to satisfy simultaneously your obligations under this
License and any other pertinent obligations, then as a consequence you may
not convey it at all.  For example, if you agree to terms that obligate you
to collect a royalty for further conveying from those to whom you convey
the Program, the only way you could satisfy both those terms and this
License would be to refrain entirely from conveying the Program.

  13. Use with the GNU Affero General Public License.

  Notwithstanding any other provision of this License, you have
permission to link or combine any covered work with a work licensed
under version 3 of the GNU Affero General Public License into a single
combined work, and to convey the resulting work.  The terms of this
License will continue to apply to the part which is the covered work,
but the special requirements of the GNU Affero General Public License,
section 13, concerning interaction through a network will apply to the
combination as such.

  14. Revised Versions of this License.

  The Free Software Foundation may publish revised and/or new versions of
the GNU General Public License from time to time.  Such new versions will
be similar in spirit to the present version, but may differ in detail to
address new problems or concerns.

  Each version is given a distinguishing version number.  If the
Program specifies that a certain numbered version of the GNU General
Public License "or any later version" applies to it, you have the
option of following the terms and conditions either of that numbered
version or of any later version published by the Free Software
Foundation.  If the Program does not specify a version number of the
GNU General Public License, you may choose any version ever published
by the Free Software Foundation.

  If the Program specifies that a proxy can decide which future
versions of the GNU General Public License can be used, that proxy's
public statement of acceptance of a version permanently authorizes you
to choose that version for the Program.

  Later license versions may give you additional or different
permissions.  However, no additional obligations are imposed on any
author or copyright holder as a result of your choosing to follow a
later version.

  15. Disclaimer of Warranty.

  THERE IS NO WARRANTY FOR THE PROGRAM, TO THE EXTENT PERMITTED BY
APPLICABLE LAW.  EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT
HOLDERS AND/OR OTHER PARTIES PROVIDE THE PROGRAM "AS IS" WITHOUT WARRANTY
OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE.  THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE PROGRAM
IS WITH YOU.  SHOULD THE PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF
ALL NECESSARY SERVICING, REPAIR OR CORRECTION.

  16. Limitation of Liability.

  IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING
WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MODIFIES AND/OR CONVEYS
THE PROGRAM AS PERMITTED ABOVE, BE LIABLE TO YOU FOR DAMAGES, INCLUDING ANY
GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE
USE OR INABILITY TO USE THE PROGRAM (INCLUDING BUT NOT LIMITED TO LOSS OF
DATA OR DATA BEING RENDERED INACCURATE OR LOSSES SUSTAINED BY YOU OR THIRD
PARTIES OR A FAILURE OF THE PROGRAM TO OPERATE WITH ANY OTHER PROGRAMS),
EVEN IF SUCH HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGES.

  17. Interpretation of Sections 15 and 16.

  If the disclaimer of warranty and limitation of liability provided
above cannot be given local legal effect according to their terms,
reviewing courts shall apply local law that most closely approximates
an absolute waiver of all civil liability in connection with the
Program, unless a warranty or assumption of liability accompanies a
copy of the Program in return for a fee.

                     END OF TERMS AND CONDITIONS

            How to Apply These Terms to Your New Programs

  If you develop a new program, and you want it to be of the greatest
possible use to the public, the best way to achieve this is to make it
free software which everyone can redistribute and change under these terms.

  To do so, attach the following notices to the program.  It is safest
to attach them to the start of each source file to most effectively
state the exclusion of warranty; and each file should have at least
the "copyright" line and a pointer to where the full notice is found.

    <one line to give the program's name and a brief idea of what it does.>
    Copyright (C) <year>  <name of author>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

Also add information on how to contact you by electronic and paper mail.

  If the program does terminal interaction, make it output a short
notice like this when it starts in an interactive mode:

    <program>  Copyright (C) <year>  <name of author>
    This program comes with ABSOLUTELY NO WARRANTY; for details type `show w'.
    This is free software, and you are welcome to redistribute it
    under certain conditions; type `show c' for details.

The hypothetical commands `show w' and `show c' should show the appropriate
parts of the General Public License.  Of course, your program's commands
might be different; for a GUI interface, you would use an "about box".

  You should also get your employer (if you work as a programmer) or school,
if any, to sign a "copyright disclaimer" for the program, if necessary.
For more information on this, and how to apply and follow the GNU GPL, see
<https://www.gnu.org/licenses/>.

  The GNU General Public License does not permit incorporating your program
into proprietary programs.  If your program is a subroutine library, you
may consider it more useful to permit linking proprietary applications with
the library.  If this is what you want to do, use the GNU Lesser General
Public License instead of this License.  But first, please read
<https://www.gnu.org/licenses/why-not-lgpl.html>."""

fun copyLicenseToClipboard(
  context: Context,
  licenseText: String = TOOLBOX_LICENSE_TEXT,
): String {
  try {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("Toolbox License", licenseText))
    Toast.makeText(context, "License copied to clipboard", Toast.LENGTH_SHORT).show()
  } catch (_: Exception) {}
  return licenseText
}

@Composable
fun LicenseFullScreenView(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  licenseText: String = TOOLBOX_LICENSE_TEXT,
  onCopyLicense: ((String) -> Unit)? = null,
) {
  val context = LocalContext.current
  var isSearchActive by rememberSaveable { mutableStateOf(false) }
  var searchQuery by rememberSaveable { mutableStateOf("") }

  BackHandler {
    if (isSearchActive) {
      isSearchActive = false
      searchQuery = ""
    } else {
      onNavigateBack()
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .toolboxScreenBackground(MaterialTheme.colorScheme.background)
      .testTag("license_screen"),
  ) {
    ToolboxTopBar(
      title = "License",
      subtext = TOOLBOX_LICENSE_TITLE,
      icon = Icons.Default.Gavel,
      onNavigateBack = {
        isSearchActive = false
        searchQuery = ""
        onNavigateBack()
      },
      backContentDescription = "Back",
      backButtonTestTag = "action_navigate_back",
      additionalBackButtonTestTag = "license_back_button",
      titleTestTag = "license_title",
      subtextTestTag = "license_subtext",
      searchState = ToolboxTopBarSearchState(
        isActive = isSearchActive,
        query = searchQuery,
        onQueryChange = { searchQuery = it },
        onOpenSearch = { isSearchActive = true },
        onCloseSearch = {
          isSearchActive = false
          searchQuery = ""
        },
        onClearQuery = { searchQuery = "" },
        placeholder = "Search license...",
      ),
    )

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 16.dp)
        .testTag("license_content"),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      val highlightBackground = MaterialTheme.colorScheme.primaryContainer
      val highlightContent = MaterialTheme.colorScheme.onPrimaryContainer

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("license_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(
          1.dp,
          MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        ),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            ) {
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
              ) {
                Text(
                  text = "GPL-3.0",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
              Text(
                text = "Version 3, 29 June 2007",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }

            OutlinedButton(
              onClick = {
                val copiedText = copyLicenseToClipboard(context, licenseText)
                onCopyLicense?.invoke(copiedText)
              },
              shape = CircleShape,
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier
                .height(30.dp)
                .testTag("copy_license_button"),
            ) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy license",
                modifier = Modifier.size(14.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Copy",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

          SelectionContainer(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("license_selectable_text"),
          ) {
            Text(
              text = highlightErrorLogText(
                text = licenseText,
                query = searchQuery,
                highlightBackground = highlightBackground,
                highlightContent = highlightContent,
              ),
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.5.sp,
                lineHeight = 16.sp,
              ),
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("license_text"),
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  modifier: Modifier = Modifier,
  preferencesRepository: NavigationPreferencesRepository = rememberNavigationPreferencesRepository(),
  initialSortingExpanded: Boolean = false,
  initialVisibilityExpanded: Boolean = false,
  initialCollapsedCategories: Set<ToolCategory> = remember { ToolCategory.entries.toSet() },
  initialThemeColorExpanded: Boolean = false,
  initialAccentColorExpanded: Boolean = false,
  initialBackgroundTextureExpanded: Boolean = false,
  initialPermissionsExpanded: Boolean = false,
  initialBackupRestoreExpanded: Boolean = false,
  initialErrorLogsDialogVisible: Boolean = false,
  initialLicenseDialogVisible: Boolean = false,
  feedbackIconsRandom: Random = Random.Default,
  onOpenUrl: ((String) -> Unit)? = null,
  onSelectTool: ((Tool) -> Unit)? = null,
) {
  val themeMode by preferencesRepository.themeModeFlow.collectAsState()
  val themeColor by preferencesRepository.themeColorFlow.collectAsState()
  val musicPlayerThemeEnabled by preferencesRepository.musicPlayerThemeEnabledFlow.collectAsState()
  val musicPlayerColorOption by preferencesRepository.musicPlayerColorOptionFlow.collectAsState()
  val accentColor by preferencesRepository.accentColorFlow.collectAsState()
  val backgroundTexture by preferencesRepository.backgroundTextureFlow.collectAsState()
  val hexagonIconButtons by preferencesRepository.hexagonIconButtonsFlow.collectAsState()
  val hiddenTools by preferencesRepository.hiddenToolsFlow.collectAsState()
  val toolSorting by preferencesRepository.toolSortingFlow.collectAsState()
  val toolSortOrder by preferencesRepository.toolSortOrderFlow.collectAsState()
  val categorySorting by preferencesRepository.categorySortingFlow.collectAsState()
  val categoryToolSorting by preferencesRepository.categoryToolSortingFlow.collectAsState()
  val toolUsageCounts by preferencesRepository.toolUsageCountsFlow.collectAsState()
  val toolsPerRow by preferencesRepository.toolsPerRowFlow.collectAsState()
  val permissionGrantStates by preferencesRepository.permissionGrantStatesFlow.collectAsState()
  val permissionUsageStats by preferencesRepository.permissionUsageStatsFlow.collectAsState()
  val errorLogs by preferencesRepository.errorLogsFlow.collectAsState()

  var isSortingExpanded by rememberSaveable { mutableStateOf(initialSortingExpanded) }
  var isThemeColorExpanded by rememberSaveable { mutableStateOf(initialThemeColorExpanded) }
  var isAccentColorExpanded by rememberSaveable { mutableStateOf(initialAccentColorExpanded) }
  var isBackgroundTextureExpanded by rememberSaveable { mutableStateOf(initialBackgroundTextureExpanded) }
  var isPermissionsExpanded by rememberSaveable { mutableStateOf(initialPermissionsExpanded) }
  var isBackupRestoreExpanded by rememberSaveable { mutableStateOf(initialBackupRestoreExpanded) }
  var showErrorLogsDialog by rememberSaveable { mutableStateOf(initialErrorLogsDialogVisible) }
  var showLicenseDialog by rememberSaveable { mutableStateOf(initialLicenseDialogVisible) }
  val haptic = LocalHapticFeedback.current

  if (showErrorLogsDialog) {
    ErrorLogsFullScreenView(
      errorLogs = errorLogs,
      onNavigateBack = { showErrorLogsDialog = false },
      onClearErrorLogs = { preferencesRepository.clearErrorLogs() },
      modifier = modifier,
    )
    return
  }

  if (showLicenseDialog) {
    LicenseFullScreenView(
      onNavigateBack = { showLicenseDialog = false },
      modifier = modifier,
    )
    return
  }

  var customHexInput by rememberSaveable(themeColor) {
    mutableStateOf(
      if (themeColor != DEFAULT_THEME_COLOR_SYSTEM && !isMusicPlayerThemeColorSetting(themeColor)) {
        themeColor
      } else {
        ""
      }
    )
  }

  var customAccentHexInput by rememberSaveable(accentColor) {
    mutableStateOf(if (!isAutoAccentColorSetting(accentColor)) accentColor else "")
  }

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .toolboxScreenBackground(MaterialTheme.colorScheme.background, texture = backgroundTexture),
  ) {
    // Standard Tool Header Bar (DESIGN_LANGUAGE.md Section 5.1)
    ToolboxTopBar(
      title = "Settings",
      subtext = "Customize theme, colors, and toolbox tools",
      icon = Icons.Default.Settings,
    )

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 16.dp)
        .testTag("settings_screen"),
      verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      // Section 1: Toolbox Default Sorting (Collapsible)
      val sortingSummary = remember(toolSorting, toolSortOrder, categorySorting, categoryToolSorting, toolsPerRow) {
        val sortModeLabel = when (toolSorting) {
          ToolSortingOption.USER_CHOICE -> "Manual"
          ToolSortingOption.TOOL_CATEGORIES -> {
            val catOrder = when (categorySorting) {
              CategorySortingOption.MANUAL -> "Manual"
              CategorySortingOption.ALPHABETICAL -> "Alpha"
              CategorySortingOption.USAGE -> "Usage"
            }
            "Categories ($catOrder)"
          }
          ToolSortingOption.USAGE -> "Usage"
          ToolSortingOption.ALPHABETICAL -> {
            val dir = if (toolSortOrder == ToolSortOrder.ASCENDING) "A–Z" else "Z–A"
            "Alphabetical ($dir)"
          }
        }
        "$sortModeLabel • $toolsPerRow per row"
      }

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("toolbox_sorting_card"),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        shape = RoundedCornerShape(16.dp),
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          // Collapsible Header Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .clickable { isSortingExpanded = !isSortingExpanded }
              .testTag("toolbox_sorting_toggle")
              .testTag("default_sorting_toggle"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
              )
              Column {
                Text(
                  text = "Default Sorting",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                )
                Text(
                  text = sortingSummary,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.testTag("default_sorting_summary"),
                )
              }
            }

            val sortingRotationAngle by animateFloatAsState(
              targetValue = if (isSortingExpanded) 180f else 0f,
              label = "default_sorting_chevron",
            )

            IconButton(
              onClick = { isSortingExpanded = !isSortingExpanded },
            ) {
              Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = if (isSortingExpanded) "Collapse default sorting" else "Expand default sorting",
                modifier = Modifier.rotate(sortingRotationAngle),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          AnimatedVisibility(
            visible = isSortingExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
          ) {
            Column(
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Text(
                text = "Choose how tools are ordered and paginated in the navigation panel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )

              SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth(),
              ) {
                val sortingOptions = listOf(
                  Triple(ToolSortingOption.USER_CHOICE, "Manual", Icons.Default.TouchApp),
                  Triple(ToolSortingOption.TOOL_CATEGORIES, "Categories", Icons.Default.Category),
                  Triple(ToolSortingOption.USAGE, "Usage", Icons.AutoMirrored.Filled.TrendingUp),
                  Triple(ToolSortingOption.ALPHABETICAL, "Alpha", Icons.Default.SortByAlpha),
                )

                sortingOptions.forEachIndexed { index, (option, label, icon) ->
                  SegmentedButton(
                    selected = toolSorting == option,
                    onClick = { preferencesRepository.setToolSorting(option) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = sortingOptions.size),
                    icon = {},
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                    modifier = Modifier
                      .testTag("sorting_option_${option.name.lowercase()}")
                      .then(if (option == ToolSortingOption.ALPHABETICAL) Modifier.testTag("sorting_option_alpha") else Modifier),
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                      Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                      )
                      Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        maxLines = 1,
                      )
                    }
                  }
                }
              }

              AnimatedVisibility(
                visible = toolSorting == ToolSortingOption.TOOL_CATEGORIES,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
              ) {
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  // Category Order Pill Selector
                  Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Text(
                      text = "Category order",
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    SingleChoiceSegmentedButtonRow(
                      modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_sorting_selector"),
                    ) {
                      val categoryOptions = listOf(
                        Triple(CategorySortingOption.MANUAL, "Manual", Icons.Default.TouchApp),
                        Triple(CategorySortingOption.ALPHABETICAL, "Alpha", Icons.Default.SortByAlpha),
                        Triple(CategorySortingOption.USAGE, "Usage", Icons.AutoMirrored.Filled.TrendingUp),
                      )

                      categoryOptions.forEachIndexed { index, (option, label, icon) ->
                        SegmentedButton(
                          selected = categorySorting == option,
                          onClick = { preferencesRepository.setCategorySorting(option) },
                          shape = SegmentedButtonDefaults.itemShape(index = index, count = categoryOptions.size),
                          icon = {},
                          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                          modifier = Modifier
                            .testTag("category_sorting_${option.name.lowercase()}")
                            .then(if (option == CategorySortingOption.ALPHABETICAL) Modifier.testTag("category_sorting_alpha") else Modifier),
                        ) {
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                          ) {
                            Icon(
                              imageVector = icon,
                              contentDescription = null,
                              modifier = Modifier.size(14.dp),
                            )
                            Text(
                              text = label,
                              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                              maxLines = 1,
                            )
                          }
                        }
                      }
                    }

                    Text(
                      text = when (categorySorting) {
                        CategorySortingOption.MANUAL -> "Categories appear in standard predefined order."
                        CategorySortingOption.ALPHABETICAL -> "Categories appear alphabetically from A to Z."
                        CategorySortingOption.USAGE -> "Categories appear with your most frequently used categories first."
                      },
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.primary,
                    )
                  }

                  // Tools Within Categories Order Pill Selector
                  Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Text(
                      text = "Tools within categories",
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    SingleChoiceSegmentedButtonRow(
                      modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_tool_sorting_selector"),
                    ) {
                      val toolOptions = listOf(
                        Triple(CategoryToolSortingOption.MANUAL, "Manual", Icons.Default.TouchApp),
                        Triple(CategoryToolSortingOption.ALPHABETICAL, "Alpha", Icons.Default.SortByAlpha),
                        Triple(CategoryToolSortingOption.USAGE, "Usage", Icons.AutoMirrored.Filled.TrendingUp),
                      )

                      toolOptions.forEachIndexed { index, (option, label, icon) ->
                        SegmentedButton(
                          selected = categoryToolSorting == option,
                          onClick = { preferencesRepository.setCategoryToolSorting(option) },
                          shape = SegmentedButtonDefaults.itemShape(index = index, count = toolOptions.size),
                          icon = {},
                          contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                          modifier = Modifier
                            .testTag("category_tool_sorting_${option.name.lowercase()}")
                            .then(if (option == CategoryToolSortingOption.ALPHABETICAL) Modifier.testTag("category_tool_sorting_alpha") else Modifier),
                        ) {
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                          ) {
                            Icon(
                              imageVector = icon,
                              contentDescription = null,
                              modifier = Modifier.size(14.dp),
                            )
                            Text(
                              text = label,
                              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                              maxLines = 1,
                            )
                          }
                        }
                      }
                    }

                    Text(
                      text = when (categoryToolSorting) {
                        CategoryToolSortingOption.MANUAL -> "Tools within each category appear in manual order. Touch and hold a tool to drag and drop it within its category."
                        CategoryToolSortingOption.ALPHABETICAL -> "Tools within each category appear alphabetically from A to Z."
                        CategoryToolSortingOption.USAGE -> "Tools within each category appear with your most used tools first."
                      },
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.primary,
                    )
                  }
                }
              }

              AnimatedVisibility(
                visible = toolSorting == ToolSortingOption.ALPHABETICAL,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
              ) {
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("alphabetical_sort_order_selector"),
                  ) {
                    val sortOrders = listOf(
                      Triple(ToolSortOrder.ASCENDING, "Ascending", Icons.Default.ArrowUpward),
                      Triple(ToolSortOrder.DESCENDING, "Descending", Icons.Default.ArrowDownward),
                    )

                    sortOrders.forEachIndexed { index, (order, label, icon) ->
                      SegmentedButton(
                        selected = toolSortOrder == order,
                        onClick = { preferencesRepository.setToolSortOrder(order) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = sortOrders.size),
                        icon = {},
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("sort_order_${order.name.lowercase()}"),
                      ) {
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                          Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                          )
                          Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            maxLines = 1,
                          )
                        }
                      }
                    }
                  }

                  Text(
                    text = if (toolSortOrder == ToolSortOrder.ASCENDING) {
                      "Tools will appear alphabetically from A to Z."
                    } else {
                      "Tools will appear reverse alphabetically from Z to A."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                  )
                }
              }

              if (toolSorting == ToolSortingOption.USAGE) {
                Text(
                  text = "Tools will appear with your most frequently used tools first.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.primary,
                )
              }

              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                  )
                  Text(
                    text = "Tools per row",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                  )
                }
                Text(
                  text = "$toolsPerRow",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.ExtraBold,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.testTag("settings_tools_per_row_count"),
                )
              }

              Text(
                text = if (toolSorting == ToolSortingOption.TOOL_CATEGORIES) {
                  "Small categories will share a single row when their combined tool count fits within this setting."
                } else {
                  "Number of tool icons displayed per row in the toolbox panel."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )

              val coercedToolsPerRow = coerceToolsPerRow(toolsPerRow)
              val stops = (MIN_TOOLS_PER_ROW..MAX_TOOLS_PER_ROW).toList()
              val activeColor = MaterialTheme.colorScheme.primary
              val inactiveColor = MaterialTheme.colorScheme.surfaceVariant
              val activeDotColor = MaterialTheme.colorScheme.onPrimary
              val inactiveDotColor = MaterialTheme.colorScheme.onSurfaceVariant

              Slider(
                value = coercedToolsPerRow.toFloat(),
                onValueChange = { newValue ->
                  preferencesRepository.setToolsPerRow(coerceToolsPerRow(newValue.roundToInt()))
                },
                valueRange = MIN_TOOLS_PER_ROW.toFloat()..MAX_TOOLS_PER_ROW.toFloat(),
                steps = MAX_TOOLS_PER_ROW - MIN_TOOLS_PER_ROW - 1,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(32.dp)
                  .testTag("settings_tools_per_row_slider"),
                thumb = {
                  Surface(
                    modifier = Modifier.size(20.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 3.dp,
                  ) {}
                },
                track = { sliderState ->
                  Canvas(
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(12.dp),
                  ) {
                    val trackStroke = 6.dp.toPx()
                    val dotRadius = 3.5.dp.toPx()
                    val centerY = size.height / 2f
                    val fraction = (sliderState.value - MIN_TOOLS_PER_ROW) /
                      (MAX_TOOLS_PER_ROW - MIN_TOOLS_PER_ROW).toFloat()
                    val activeEndX = size.width * fraction.coerceIn(0f, 1f)

                    // Inactive track
                    drawLine(
                      color = inactiveColor,
                      start = Offset(0f, centerY),
                      end = Offset(size.width, centerY),
                      strokeWidth = trackStroke,
                      cap = StrokeCap.Round,
                    )
                    // Active track
                    drawLine(
                      color = activeColor,
                      start = Offset(0f, centerY),
                      end = Offset(activeEndX, centerY),
                      strokeWidth = trackStroke,
                      cap = StrokeCap.Round,
                    )
                    // Dots for 3, 4, 5, 6
                    stops.forEachIndexed { index, stop ->
                      val stopFraction = index.toFloat() / (stops.size - 1).toFloat()
                      val dotX = size.width * stopFraction
                      val isReached = stop <= coercedToolsPerRow
                      drawCircle(
                        color = if (isReached) activeDotColor else inactiveDotColor,
                        radius = dotRadius,
                        center = Offset(dotX, centerY),
                      )
                    }
                  }
                },
                colors = SliderDefaults.colors(
                  thumbColor = MaterialTheme.colorScheme.primary,
                  activeTrackColor = MaterialTheme.colorScheme.primary,
                  inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
              )
            }
          }
        }
      }

    // Section 2: Theme Mode
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
      ),
      shape = RoundedCornerShape(16.dp),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = when (themeMode) {
              ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
              ThemeMode.LIGHT -> Icons.Default.LightMode
              ThemeMode.DARK -> Icons.Default.DarkMode
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
          )
          Text(
            text = "Theme Mode",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
          )
        }

        Text(
          text = "Choose light, dark, or follow system theme",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SingleChoiceSegmentedButtonRow(
          modifier = Modifier.fillMaxWidth(),
        ) {
          val modes = listOf(
            Triple(ThemeMode.SYSTEM, "System", Icons.Default.BrightnessAuto),
            Triple(ThemeMode.LIGHT, "Light", Icons.Default.LightMode),
            Triple(ThemeMode.DARK, "Dark", Icons.Default.DarkMode),
          )

          modes.forEachIndexed { index, (mode, label, icon) ->
            SegmentedButton(
              selected = themeMode == mode,
              onClick = { preferencesRepository.setThemeMode(mode) },
              shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
              icon = {},
              modifier = Modifier.testTag("theme_mode_${mode.name.lowercase()}"),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Icon(
                  imageVector = icon,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Text(text = label, style = MaterialTheme.typography.labelMedium)
              }
            }
          }
        }
      }
    }

    // Section 3: Theme Color (Collapsible)
    val localContext = LocalContext.current
    val isInspection = LocalInspectionMode.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isSystemMediaListenerEnabled by remember(localContext) {
      mutableStateOf(
        if (isInspection) true else MusicPlayerEngine.isSystemMediaListenerEnabled(localContext)
      )
    }
    val currentMusicPalette = MusicPlayerEngine.activeTrackPalette
    val currentMusicTrack = MusicPlayerEngine.activeTrack
    val activeSystemMediaInfo = MusicPlayerEngine.activeSystemMediaInfo
    val isMusicPlaying = MusicPlayerEngine.isMusicPlaying
    LaunchedEffect(localContext, isThemeColorExpanded, musicPlayerThemeEnabled) {
      if (!isInspection) {
        isSystemMediaListenerEnabled = MusicPlayerEngine.isSystemMediaListenerEnabled(localContext)
        MusicPlayerEngine.syncExternalSystemMediaSessions(localContext)
      }
    }
    DisposableEffect(lifecycleOwner, localContext, isInspection) {
      if (isInspection) {
        onDispose {}
      } else {
        val observer = LifecycleEventObserver { _, event ->
          if (event == Lifecycle.Event.ON_RESUME) {
            isSystemMediaListenerEnabled = MusicPlayerEngine.isSystemMediaListenerEnabled(localContext)
            MusicPlayerEngine.syncExternalSystemMediaSessions(localContext)
          }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
          lifecycleOwner.lifecycle.removeObserver(observer)
        }
      }
    }
    val selectedMusicColorOption = remember(themeColor, musicPlayerColorOption) {
      MusicPlayerColorOption.fromPrefValue(themeColor)
        ?: MusicPlayerColorOption.fromPrefValue(musicPlayerColorOption)
        ?: MusicPlayerColorOption.DOMINANT
    }
    val activeMusicThemeColor = remember(currentMusicPalette, selectedMusicColorOption) {
      selectedMusicColorOption.resolveColor(currentMusicPalette)
    }
    val isMusicThemeCurrentlyActive = musicPlayerThemeEnabled && isMusicPlaying
    val activeThemeColorPreset = PRESET_COLORS.find { it.hex.equals(themeColor, ignoreCase = true) }
    val fallbackThemeColorSummary = when {
      themeColor == DEFAULT_THEME_COLOR_SYSTEM || isMusicPlayerThemeColorSetting(themeColor) -> "System Preset"
      activeThemeColorPreset != null -> "${activeThemeColorPreset.name} (${activeThemeColorPreset.hex})"
      else -> "Custom ($themeColor)"
    }
    val themeColorSummary = if (isMusicThemeCurrentlyActive) {
      "${selectedMusicColorOption.label} • Fallback: $fallbackThemeColorSummary"
    } else {
      fallbackThemeColorSummary
    }
    val activeThemeColorBadge = when {
      isMusicThemeCurrentlyActive -> activeMusicThemeColor
      themeColor == DEFAULT_THEME_COLOR_SYSTEM || isMusicPlayerThemeColorSetting(themeColor) -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          MaterialTheme.colorScheme.primary
        } else {
          Color(0xFF8E66A6)
        }
      }
      else -> parseHexColor(themeColor) ?: MaterialTheme.colorScheme.primary
    }

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("theme_color_card"),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
      ),
      shape = RoundedCornerShape(16.dp),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Collapsible Header Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { isThemeColorExpanded = !isThemeColorExpanded }
            .testTag("theme_color_toggle"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
          ) {
            Icon(
              imageVector = Icons.Default.Palette,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp),
            )
            Column {
              Text(
                text = "Theme Color",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
              )
              Text(
                text = themeColorSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Box(
              modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(activeThemeColorBadge)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            )

            val rotationAngle by animateFloatAsState(
              targetValue = if (isThemeColorExpanded) 180f else 0f,
              label = "theme_color_chevron",
            )

            IconButton(
              onClick = { isThemeColorExpanded = !isThemeColorExpanded },
            ) {
              Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = if (isThemeColorExpanded) "Collapse theme color" else "Expand theme color",
                modifier = Modifier.rotate(rotationAngle),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        AnimatedVisibility(
          visible = isThemeColorExpanded,
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut(),
        ) {
          Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Text(
              text = "Default uses system preset (Material You on Android 12+, #8E66A6 fallback on older Android versions).",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // System Preset Option Card
            val isSystemSelected =
              themeColor == DEFAULT_THEME_COLOR_SYSTEM || isMusicPlayerThemeColorSetting(themeColor)
            OutlinedCard(
              onClick = {
                preferencesRepository.setThemeColor(DEFAULT_THEME_COLOR_SYSTEM)
                customHexInput = ""
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("color_preset_system"),
              shape = RoundedCornerShape(12.dp),
              border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                  if (isSystemSelected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.outlineVariant
                ),
              ),
              colors = CardDefaults.outlinedCardColors(
                containerColor = if (isSystemSelected) {
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                } else {
                  Color.Transparent
                },
              ),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Box(
                    modifier = Modifier
                      .size(24.dp)
                      .clip(CircleShape)
                      .background(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                          MaterialTheme.colorScheme.primary
                        } else {
                          Color(0xFF8E66A6)
                        }
                      ),
                    contentAlignment = Alignment.Center,
                  ) {
                    if (isSystemSelected) {
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                      )
                    }
                  }
                  Column {
                    Text(
                      text = "System Preset",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                    )
                    Text(
                      text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        "Material You dynamic colors"
                      } else {
                        "Toolbox Purple (#8E66A6) fallback"
                      },
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }

                if (isSystemSelected) {
                  Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                  ) {
                    Text(
                      text = "Active",
                      style = MaterialTheme.typography.labelSmall,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                  }
                }
              }
            }

            // Music Player Colors Option Card (Checkbox, checked by default) & Swatches
            val isMusicThemeSelected = musicPlayerThemeEnabled
            OutlinedCard(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .toggleable(
                  value = isMusicThemeSelected,
                  role = Role.Checkbox,
                  onValueChange = { checked ->
                    preferencesRepository.setMusicPlayerThemeEnabled(checked)
                    if (checked && !isInspection) {
                      isSystemMediaListenerEnabled = MusicPlayerEngine.isSystemMediaListenerEnabled(localContext)
                      MusicPlayerEngine.syncExternalSystemMediaSessions(localContext)
                    }
                  },
                )
                .testTag("color_preset_music_player"),
              shape = RoundedCornerShape(12.dp),
              border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                  if (isMusicThemeSelected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.outlineVariant
                ),
              ),
              colors = CardDefaults.outlinedCardColors(
                containerColor = if (isMusicThemeSelected) {
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                } else {
                  Color.Transparent
                },
              ),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                  modifier = Modifier.weight(1f),
                ) {
                  Checkbox(
                    checked = isMusicThemeSelected,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(
                      checkedColor = MaterialTheme.colorScheme.primary,
                      uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                  )
                  Box(
                    modifier = Modifier
                      .size(24.dp)
                      .clip(CircleShape)
                      .background(activeMusicThemeColor),
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      imageVector = Icons.Default.MusicNote,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(15.dp),
                    )
                  }
                  Column {
                    Text(
                      text = if (isMusicThemeSelected) {
                        selectedMusicColorOption.label
                      } else {
                        "System Media Colors"
                      },
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                    )
                    Text(
                      text = when {
                        !isMusicThemeSelected ->
                          "When checked, themes app from the active Android system media player (falls back to selected theme color)"
                        isMusicPlaying && activeSystemMediaInfo != null && activeSystemMediaInfo.isExternalApp -> {
                          val sourceSuffix = activeSystemMediaInfo.sourceAppName.takeIf { it.isNotBlank() }?.let { " • $it" }.orEmpty()
                          "Playing ${activeSystemMediaInfo.title}$sourceSuffix (${selectedMusicColorOption.swatchLabel})"
                        }
                        isMusicPlaying && currentMusicTrack != null ->
                          "Playing ${currentMusicTrack.title} (${selectedMusicColorOption.swatchLabel})"
                        isMusicPlaying ->
                          "Active while system media is playing (${selectedMusicColorOption.swatchLabel})"
                        else ->
                          "Follows Android system media player • Fallback: $fallbackThemeColorSummary"
                      },
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
                }

                if (isMusicThemeCurrentlyActive) {
                  Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                  ) {
                    Text(
                      text = "Playing",
                      style = MaterialTheme.typography.labelSmall,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                  }
                }
              }
            }

            Column(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("theme_music_player_swatches_section"),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "System Media Palette",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = when {
                    activeSystemMediaInfo != null && activeSystemMediaInfo.isExternalApp -> {
                      val artistOrApp = activeSystemMediaInfo.artist.ifBlank { activeSystemMediaInfo.sourceAppName }
                      if (artistOrApp.isNotBlank()) "${activeSystemMediaInfo.title} • $artistOrApp" else activeSystemMediaInfo.title
                    }
                    currentMusicTrack != null -> "${currentMusicTrack.title} • ${currentMusicTrack.artist}"
                    else -> "Follows Android system media player"
                  },
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                MusicPlayerColorOption.entries.forEach { musicOption ->
                  val swatchColor = musicOption.resolveColor(currentMusicPalette)
                  val swatchHex = musicOption.resolveHex(currentMusicPalette)
                  val isSwatchSelected = isMusicThemeSelected && selectedMusicColorOption == musicOption

                  Surface(
                    onClick = {
                      preferencesRepository.setMusicPlayerColorOption(musicOption.prefValue)
                      preferencesRepository.setMusicPlayerThemeEnabled(true)
                      if (!isInspection) {
                        isSystemMediaListenerEnabled = MusicPlayerEngine.isSystemMediaListenerEnabled(localContext)
                        MusicPlayerEngine.syncExternalSystemMediaSessions(localContext)
                      }
                    },
                    modifier = Modifier
                      .weight(1f)
                      .testTag("theme_music_swatch_${musicOption.name.lowercase()}"),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSwatchSelected) {
                      MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    } else {
                      MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    border = BorderStroke(
                      width = if (isSwatchSelected) 2.dp else 1.dp,
                      color = if (isSwatchSelected) {
                        MaterialTheme.colorScheme.primary
                      } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                      },
                    ),
                  ) {
                    Column(
                      modifier = Modifier.padding(8.dp),
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                      Box(
                        modifier = Modifier
                          .fillMaxWidth()
                          .height(40.dp)
                          .background(swatchColor, RoundedCornerShape(10.dp))
                          .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp),
                          ),
                        contentAlignment = Alignment.Center,
                      ) {
                        if (isSwatchSelected) {
                          Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected ${musicOption.label}",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                          )
                        }
                      }
                      Text(
                        text = musicOption.swatchLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSwatchSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSwatchSelected) {
                          MaterialTheme.colorScheme.primary
                        } else {
                          MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                      )
                      Text(
                        text = swatchHex,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                      )
                    }
                  }
                }
              }

              if (!isSystemMediaListenerEnabled && activeSystemMediaInfo?.isExternalApp != true) {
                Surface(
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("system_media_listener_permission_banner"),
                  shape = RoundedCornerShape(12.dp),
                  color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                  border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                  ),
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                  ) {
                    Text(
                      text = "Grant Notification Access so Toolbox can read artwork & track colors from external media apps.",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSecondaryContainer,
                      modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    )
                    OutlinedButton(
                      onClick = {
                        MusicPlayerEngine.openSystemMediaListenerSettings(localContext)
                      },
                      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                      modifier = Modifier.testTag("grant_system_media_access_button"),
                    ) {
                      Text(
                        text = "Grant Access",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                      )
                    }
                  }
                }
              }
            }

            // Color Swatches
            Text(
              text = "Preset Colors",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              PRESET_COLORS.forEach { preset ->
                val isSelected = themeColor.equals(preset.hex, ignoreCase = true)
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(4.dp),
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                      preferencesRepository.setThemeColor(preset.hex)
                      customHexInput = preset.hex
                    }
                    .padding(4.dp)
                    .testTag("color_swatch_${preset.hex}"),
                ) {
                  Box(
                    modifier = Modifier
                      .size(40.dp)
                      .clip(CircleShape)
                      .background(preset.color)
                      .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape,
                      ),
                    contentAlignment = Alignment.Center,
                  ) {
                    if (isSelected) {
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected ${preset.name}",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                      )
                    }
                  }
                  Text(
                    text = if (preset.isFallbackDefault) "#8E66A6" else preset.name.split(" ").first(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  )
                }
              }
            }

            // Custom Hex Color Input
            Text(
              text = "Custom Color",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )

            val parsedCustomColor = remember(customHexInput) { parseHexColor(customHexInput) }

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              OutlinedTextField(
                value = customHexInput,
                onValueChange = { input ->
                  val sanitized = if (input.startsWith("#")) input else "#$input"
                  customHexInput = sanitized
                  val parsed = parseHexColor(sanitized)
                  if (parsed != null) {
                    preferencesRepository.setThemeColor(sanitized.uppercase())
                  }
                },
                label = { Text("Hex Color (e.g. #8E66A6)") },
                placeholder = { Text("#8E66A6") },
                singleLine = true,
                leadingIcon = {
                  Box(
                    modifier = Modifier
                      .size(24.dp)
                      .clip(CircleShape)
                      .background(parsedCustomColor ?: Color.Gray)
                      .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                  )
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("custom_color_input"),
                colors = OutlinedTextFieldDefaults.colors(),
              )

              if (parsedCustomColor != null) {
                Button(
                  onClick = {
                    preferencesRepository.setThemeColor(customHexInput.uppercase())
                  },
                  modifier = Modifier.testTag("apply_custom_color_button"),
                ) {
                  Text("Apply")
                }
              }
            }
          }
        }
      }
    }

    // Section 4: Accent Color (Collapsible)
    val currentPrimary = MaterialTheme.colorScheme.primary
    val themeBackgroundIsDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val isDarkTheme = when (themeMode) {
      ThemeMode.LIGHT -> false
      ThemeMode.DARK -> true
      ThemeMode.SYSTEM -> themeBackgroundIsDark
    }
    val defaultAutoHarmony = remember(isDarkTheme) {
      AccentHarmonyOption.defaultForTheme(isDarkTheme)
    }
    val defaultAutoAccentColor = remember(currentPrimary, defaultAutoHarmony) {
      calculateAccentHarmonyColor(currentPrimary, defaultAutoHarmony)
    }
    val activeAutoHarmony = remember(accentColor, isDarkTheme) {
      AccentHarmonyOption.fromPrefValue(accentColor, darkTheme = isDarkTheme)
    }
    var selectedHarmonyMode by rememberSaveable {
      mutableStateOf(activeAutoHarmony?.harmonyMode ?: HarmonyMode.ANALOGOUS)
    }
    LaunchedEffect(activeAutoHarmony) {
      if (activeAutoHarmony != null && activeAutoHarmony != AccentHarmonyOption.MONOCHROMATIC) {
        selectedHarmonyMode = activeAutoHarmony.harmonyMode
      }
    }
    val activeAutoHarmonyColor = remember(currentPrimary, activeAutoHarmony, defaultAutoAccentColor) {
      if (activeAutoHarmony != null) {
        calculateAccentHarmonyColor(currentPrimary, activeAutoHarmony)
      } else {
        defaultAutoAccentColor
      }
    }
    val activeAccentPreset = ACCENT_PRESET_COLORS.find { it.hex.equals(accentColor, ignoreCase = true) }
    val accentColorSummary = when {
      activeAutoHarmony != null -> activeAutoHarmony.label
      activeAccentPreset != null -> "${activeAccentPreset.name} (${activeAccentPreset.hex})"
      else -> "Custom ($accentColor)"
    }
    val activeAccentColorBadge = when {
      activeAutoHarmony != null -> activeAutoHarmonyColor
      else -> parseHexColor(accentColor) ?: defaultAutoAccentColor
    }

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("accent_color_card"),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
      ),
      shape = RoundedCornerShape(16.dp),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Collapsible Header Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { isAccentColorExpanded = !isAccentColorExpanded }
            .testTag("accent_color_toggle"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
          ) {
            Icon(
              imageVector = Icons.Default.Brush,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.tertiary,
              modifier = Modifier.size(20.dp),
            )
            Column {
              Text(
                text = "Accent Color",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
              )
              Text(
                text = accentColorSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Box(
              modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(activeAccentColorBadge)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            )

            val rotationAngle by animateFloatAsState(
              targetValue = if (isAccentColorExpanded) 180f else 0f,
              label = "accent_color_chevron",
            )

            IconButton(
              onClick = { isAccentColorExpanded = !isAccentColorExpanded },
            ) {
              Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = if (isAccentColorExpanded) "Collapse accent color" else "Expand accent color",
                modifier = Modifier.rotate(rotationAngle),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        AnimatedVisibility(
          visible = isAccentColorExpanded,
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut(),
        ) {
          Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Text(
              text = "Accent color used for key tool highlights (action keys, tuning needle, metronome downbeat, calipers). Defaults to Analogous (+30°) of the main theme color.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            val isAutoAccentSelected = activeAutoHarmony != null

            // Auto Option Card
            OutlinedCard(
              onClick = {
                selectedHarmonyMode = HarmonyMode.ANALOGOUS
                preferencesRepository.setAccentColor(DEFAULT_ACCENT_COLOR_AUTO)
                customAccentHexInput = ""
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("accent_preset_auto"),
              shape = RoundedCornerShape(12.dp),
              border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                  if (isAutoAccentSelected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.outlineVariant
                ),
              ),
              colors = CardDefaults.outlinedCardColors(
                containerColor = if (isAutoAccentSelected) {
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                } else {
                  Color.Transparent
                },
              ),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                  modifier = Modifier.weight(1f),
                ) {
                  Box(
                    modifier = Modifier
                      .size(24.dp)
                      .clip(CircleShape)
                      .background(activeAutoHarmonyColor),
                    contentAlignment = Alignment.Center,
                  ) {
                    if (isAutoAccentSelected) {
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                      )
                    }
                  }
                  Column {
                    Text(
                      text = (activeAutoHarmony ?: defaultAutoHarmony).label,
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                    )
                    Text(
                      text = (activeAutoHarmony ?: defaultAutoHarmony).description,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }

                if (isAutoAccentSelected) {
                  Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                  ) {
                    Text(
                      text = "Active",
                      style = MaterialTheme.typography.labelSmall,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                  }
                }
              }
            }

            // Color Harmonies (Auto) Section inspired by Colors tool
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("accent_color_harmonies_section"),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "Color Harmonies (Auto)",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = "Tap harmony to auto-track theme",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                HarmonyMode.entries.forEach { mode ->
                  val defaultOption = AccentHarmonyOption.defaultForMode(mode, isDarkTheme)
                  val isModeSelected = selectedHarmonyMode == mode && isAutoAccentSelected
                  FilterChip(
                    selected = isModeSelected,
                    onClick = {
                      selectedHarmonyMode = mode
                      preferencesRepository.setAccentColor(defaultOption.prefValue)
                      customAccentHexInput = ""
                    },
                    shape = CircleShape,
                    label = {
                      Text(
                        text = mode.label,
                        fontWeight = if (isModeSelected) FontWeight.Bold else FontWeight.Medium,
                      )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                      selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    modifier = Modifier.testTag("accent_harmony_mode_${mode.name.lowercase()}"),
                  )
                }
              }

              val harmonySwatches = remember(selectedHarmonyMode) {
                AccentHarmonyOption.swatchesForMode(selectedHarmonyMode)
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                harmonySwatches.forEach { swatchOption ->
                  val swatchColor = remember(currentPrimary, swatchOption) {
                    calculateAccentHarmonyColor(currentPrimary, swatchOption)
                  }
                  val swatchHex = remember(swatchColor) {
                    colorToHexString(swatchColor)
                  }
                  val isSwatchSelected = activeAutoHarmony == swatchOption

                  Surface(
                    onClick = {
                      preferencesRepository.setAccentColor(swatchOption.prefValue)
                      customAccentHexInput = ""
                    },
                    modifier = Modifier
                      .weight(1f)
                      .testTag("accent_harmony_swatch_${swatchOption.name.lowercase()}"),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSwatchSelected) {
                      MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    } else {
                      MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    border = BorderStroke(
                      width = if (isSwatchSelected) 2.dp else 1.dp,
                      color = if (isSwatchSelected) {
                        MaterialTheme.colorScheme.primary
                      } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                      },
                    ),
                  ) {
                    Column(
                      modifier = Modifier.padding(8.dp),
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                      Box(
                        modifier = Modifier
                          .fillMaxWidth()
                          .height(40.dp)
                          .background(swatchColor, RoundedCornerShape(10.dp))
                          .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp),
                          ),
                        contentAlignment = Alignment.Center,
                      ) {
                        if (isSwatchSelected) {
                          Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected ${swatchOption.label}",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                          )
                        }
                      }
                      Text(
                        text = swatchOption.swatchLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSwatchSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSwatchSelected) {
                          MaterialTheme.colorScheme.primary
                        } else {
                          MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                      )
                      Text(
                        text = swatchHex,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                      )
                    }
                  }
                }
              }
            }

            // Preset Accent Swatches
            Text(
              text = "Preset Accent Colors",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              ACCENT_PRESET_COLORS.forEach { preset ->
                val isSelected = accentColor.equals(preset.hex, ignoreCase = true)
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(4.dp),
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                      preferencesRepository.setAccentColor(preset.hex)
                      customAccentHexInput = preset.hex
                    }
                    .padding(4.dp)
                    .testTag("accent_preset_${preset.hex.removePrefix("#").lowercase()}"),
                ) {
                  Box(
                    modifier = Modifier
                      .size(40.dp)
                      .clip(CircleShape)
                      .background(preset.color)
                      .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape,
                      )
                      .testTag("accent_swatch_${preset.hex}"),
                    contentAlignment = Alignment.Center,
                  ) {
                    if (isSelected) {
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected ${preset.name}",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                      )
                    }
                  }
                  Text(
                    text = preset.name.split(" ").first(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  )
                }
              }
            }

            // Custom Hex Accent Input
            Text(
              text = "Custom Accent Color",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )

            val parsedCustomAccent = remember(customAccentHexInput) { parseHexColor(customAccentHexInput) }

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              OutlinedTextField(
                value = customAccentHexInput,
                onValueChange = { input ->
                  val sanitized = if (input.startsWith("#")) input else "#$input"
                  customAccentHexInput = sanitized
                  val parsed = parseHexColor(sanitized)
                  if (parsed != null) {
                    preferencesRepository.setAccentColor(sanitized.uppercase())
                  }
                },
                label = { Text("Accent Hex (e.g. #E65100)") },
                placeholder = { Text("#E65100") },
                singleLine = true,
                leadingIcon = {
                  Box(
                    modifier = Modifier
                      .size(24.dp)
                      .clip(CircleShape)
                      .background(parsedCustomAccent ?: Color.Gray)
                      .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                  )
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("custom_accent_color_input"),
                colors = OutlinedTextFieldDefaults.colors(),
              )

              if (parsedCustomAccent != null) {
                Button(
                  onClick = {
                    preferencesRepository.setAccentColor(customAccentHexInput.uppercase())
                  },
                  modifier = Modifier.testTag("apply_custom_accent_color_button"),
                ) {
                  Text("Apply")
                }
              }
            }
          }
        }
      }
    }

    // Section 5: Background Texture (Collapsible)
    val backgroundTextureSummary = remember(backgroundTexture, hexagonIconButtons) {
      val base = if (backgroundTexture == BackgroundTexture.NONE) {
        "None (Default)"
      } else {
        "${backgroundTexture.displayName} • ${backgroundTexture.subtitle}"
      }
      if (hexagonIconButtons) {
        "$base • Hexagon icons"
      } else {
        base
      }
    }

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("background_texture_card"),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
      ),
      shape = RoundedCornerShape(16.dp),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Collapsible Header Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { isBackgroundTextureExpanded = !isBackgroundTextureExpanded }
            .testTag("background_texture_toggle"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
          ) {
            Icon(
              imageVector = Icons.Default.Texture,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp),
            )
            Column {
              Text(
                text = "Background Texture",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
              )
              Text(
                text = backgroundTextureSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("background_texture_summary"),
              )
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            BackgroundTexturePreviewBox(
              texture = backgroundTexture,
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                .testTag("background_texture_badge"),
            )

            val textureRotationAngle by animateFloatAsState(
              targetValue = if (isBackgroundTextureExpanded) 180f else 0f,
              label = "background_texture_chevron",
            )

            IconButton(
              onClick = { isBackgroundTextureExpanded = !isBackgroundTextureExpanded },
            ) {
              Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = if (isBackgroundTextureExpanded) {
                  "Collapse background texture"
                } else {
                  "Expand background texture"
                },
                modifier = Modifier.rotate(textureRotationAngle),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        AnimatedVisibility(
          visible = isBackgroundTextureExpanded,
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut(),
        ) {
          Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Text(
              text = "Apply a procedural background texture across all tool screens. Uses your active theme and accent colors.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Default "None" Option Card
            val isNoneTextureSelected = backgroundTexture == BackgroundTexture.NONE
            OutlinedCard(
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                preferencesRepository.setBackgroundTexture(BackgroundTexture.NONE)
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("background_texture_option_none"),
              shape = RoundedCornerShape(12.dp),
              border = CardDefaults.outlinedCardBorder().copy(
                brush = SolidColor(
                  if (isNoneTextureSelected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.outlineVariant
                ),
              ),
              colors = CardDefaults.outlinedCardColors(
                containerColor = if (isNoneTextureSelected) {
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                } else {
                  Color.Transparent
                },
              ),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                  modifier = Modifier.weight(1f),
                ) {
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.background)
                      .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center,
                  ) {
                    if (isNoneTextureSelected) {
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                      )
                    }
                  }
                  Column {
                    Text(
                      text = "None (Default)",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                    )
                    Text(
                      text = BackgroundTexture.NONE.subtitle,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }

                if (isNoneTextureSelected) {
                  Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                  ) {
                    Text(
                      text = "Active",
                      style = MaterialTheme.typography.labelSmall,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                  }
                }
              }
            }

            // Hexagonify Icon Buttons Toggle Card (Vertical left and right sides)
            OutlinedCard(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .toggleable(
                  value = hexagonIconButtons,
                  role = Role.Checkbox,
                  onValueChange = { checked ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    preferencesRepository.setHexagonIconButtons(checked)
                  },
                )
                .testTag("hexagon_icon_buttons_toggle"),
              shape = RoundedCornerShape(12.dp),
              border = CardDefaults.outlinedCardBorder().copy(
                brush = SolidColor(
                  if (hexagonIconButtons) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.outlineVariant
                ),
              ),
              colors = CardDefaults.outlinedCardColors(
                containerColor = if (hexagonIconButtons) {
                  MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                } else {
                  Color.Transparent
                },
              ),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                  modifier = Modifier.weight(1f),
                ) {
                  Checkbox(
                    checked = hexagonIconButtons,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(
                      checkedColor = MaterialTheme.colorScheme.primary,
                      uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                  )
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(HexagonShape)
                      .background(MaterialTheme.colorScheme.primaryContainer)
                      .border(1.dp, MaterialTheme.colorScheme.primary, HexagonShape),
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      imageVector = if (hexagonIconButtons) Icons.Default.Check else Icons.Default.Texture,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(16.dp),
                    )
                  }
                  Column {
                    Text(
                      text = "Hexagonify Icon Buttons",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                    )
                    Text(
                      text = "Transform circular action icon buttons, badges, and FABs into vertical-sided hexagons",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }

                if (hexagonIconButtons) {
                  Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                  ) {
                    Text(
                      text = "Active",
                      style = MaterialTheme.typography.labelSmall,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                  }
                }
              }
            }

            // Fun Presets Header
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Texture Presets",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "${BackgroundTexture.presets.size} presets",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            // 2-column grid of live texture preset cards
            val presetRows = remember { BackgroundTexture.presets.chunked(2) }
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              presetRows.forEach { rowPresets ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  rowPresets.forEach { preset ->
                    val isPresetSelected = backgroundTexture == preset
                    Surface(
                      onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        preferencesRepository.setBackgroundTexture(preset)
                      },
                      modifier = Modifier
                        .weight(1f)
                        .testTag("background_texture_option_${preset.name.lowercase()}"),
                      shape = RoundedCornerShape(14.dp),
                      color = if (isPresetSelected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                      } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                      },
                      border = BorderStroke(
                        width = if (isPresetSelected) 2.dp else 1.dp,
                        color = if (isPresetSelected) {
                          MaterialTheme.colorScheme.primary
                        } else {
                          MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                        },
                      ),
                    ) {
                      Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                      ) {
                        Box(
                          modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                              width = 1.dp,
                              color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                              shape = RoundedCornerShape(10.dp),
                            ),
                        ) {
                          BackgroundTexturePreviewBox(
                            texture = preset,
                            modifier = Modifier.fillMaxSize(),
                          )

                          if (isPresetSelected) {
                            Box(
                              modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                              contentAlignment = Alignment.Center,
                            ) {
                              Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected ${preset.displayName}",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp),
                              )
                            }
                          }
                        }

                        Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceBetween,
                          verticalAlignment = Alignment.CenterVertically,
                        ) {
                          Text(
                            text = preset.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isPresetSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isPresetSelected) {
                              MaterialTheme.colorScheme.primary
                            } else {
                              MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                          )
                          if (isPresetSelected) {
                            Surface(
                              shape = CircleShape,
                              color = MaterialTheme.colorScheme.primary,
                              contentColor = MaterialTheme.colorScheme.onPrimary,
                            ) {
                              Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                              )
                            }
                          }
                        }

                        Text(
                          text = preset.subtitle,
                          style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                          maxLines = 2,
                          minLines = 2,
                          overflow = TextOverflow.Ellipsis,
                        )
                      }
                    }
                  }
                  if (rowPresets.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                  }
                }
              }
            }
          }
        }
      }
    }

    // Section 6: Tool Settings (Collapsible Panel with Collapsible Categories)
    var isToolSettingsExpanded by rememberSaveable { mutableStateOf(initialVisibilityExpanded) }
    val configurableTools = remember { Tool.entries.filter { it != Tool.SETTINGS } }
    val hiddenCount = configurableTools.count { it in hiddenTools }
    val visibleCount = configurableTools.size - hiddenCount
    val totalToolUses = toolUsageCounts.values.sum()
    val context = LocalContext.current
    val sortedCategories = remember { ToolCategory.entries.sortedBy { it.title } }
    var collapsedCategories by rememberSaveable { mutableStateOf(initialCollapsedCategories) }

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("tool_settings_card")
        .testTag("tool_visibility_card"),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
      ),
      shape = RoundedCornerShape(16.dp),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Collapsible Header Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { isToolSettingsExpanded = !isToolSettingsExpanded }
            .testTag("tool_settings_toggle")
            .testTag("tool_visibility_toggle"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
          ) {
            Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp),
            )
            Column {
              Text(
                text = "Tool Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
              )
              Text(
                text = if (hiddenCount == 0) "All tools visible • Add to Home screen" else "$hiddenCount hidden • $visibleCount visible • Add to Home screen",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          val rotationAngle by animateFloatAsState(
            targetValue = if (isToolSettingsExpanded) 180f else 0f,
            label = "tool_settings_chevron",
          )

          IconButton(
            onClick = { isToolSettingsExpanded = !isToolSettingsExpanded },
          ) {
            Icon(
              imageVector = Icons.Default.ExpandMore,
              contentDescription = if (isToolSettingsExpanded) "Collapse tool settings" else "Expand tool settings",
              modifier = Modifier.rotate(rotationAngle),
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        AnimatedVisibility(
          visible = isToolSettingsExpanded,
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut(),
        ) {
          Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Text(
              text = "Manage tool visibility in navigation and add standalone shortcuts to your Home screen. Settings is always visible in navigation.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Tool List grouped by category (alphabetical) with collapsible categories
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              sortedCategories.forEach { category ->
                val categoryTools = remember(category) {
                  Tool.entries.filter { it.category == category }.sortedBy { it.title }
                }
                val categoryHiddenCount = categoryTools.count { it != Tool.SETTINGS && it in hiddenTools }
                val isCatCollapsed = category in collapsedCategories
                val catRotationAngle by animateFloatAsState(
                  targetValue = if (isCatCollapsed) 0f else 180f,
                  label = "category_${category.name}_chevron",
                )

                Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  // Collapsible Category Header
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(8.dp))
                      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                      .clickable {
                        collapsedCategories = if (isCatCollapsed) {
                          collapsedCategories - category
                        } else {
                          collapsedCategories + category
                        }
                      }
                      .padding(horizontal = 12.dp, vertical = 8.dp)
                      .testTag("category_header_${category.name.lowercase()}")
                      .testTag("tool_settings_category_${category.name.lowercase()}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                      Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                      )
                      Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.testTag("category_tool_count_${category.name.lowercase()}"),
                      ) {
                        Text(
                          text = "${categoryTools.size}",
                          style = MaterialTheme.typography.labelSmall,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                      }
                      Surface(
                        shape = CircleShape,
                        color = if (categoryHiddenCount > 0) {
                          MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                          MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = if (categoryHiddenCount > 0) {
                          MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                          MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.testTag("category_hidden_count_${category.name.lowercase()}"),
                      ) {
                        Text(
                          text = "$categoryHiddenCount hidden",
                          style = MaterialTheme.typography.labelSmall,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                      }
                    }

                    Icon(
                      imageVector = Icons.Default.ExpandMore,
                      contentDescription = if (isCatCollapsed) "Expand ${category.title}" else "Collapse ${category.title}",
                      modifier = Modifier
                        .size(20.dp)
                        .rotate(catRotationAngle),
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }

                  // Tools in Category
                  AnimatedVisibility(
                    visible = !isCatCollapsed,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                  ) {
                    Column(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 2.dp, bottom = 6.dp),
                      verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                      categoryTools.forEach { tool ->
                        val isSettings = tool == Tool.SETTINGS
                        val isVisible = tool !in hiddenTools || isSettings

                        Row(
                          modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .padding(vertical = 2.dp, horizontal = 2.dp)
                            .testTag("tool_visibility_row_${tool.name}")
                            .testTag("tool_settings_row_${tool.name}"),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                          if (isSettings) {
                            Spacer(modifier = Modifier.size(48.dp))
                          } else {
                            val eyeColor by animateColorAsState(
                              targetValue = if (isVisible) {
                                MaterialTheme.colorScheme.primary
                              } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                              },
                              animationSpec = tween(200),
                              label = "eye_color_${tool.name}",
                            )

                            IconButton(
                              onClick = {
                                val updated = if (isVisible) {
                                  hiddenTools + tool
                                } else {
                                  hiddenTools - tool
                                }
                                preferencesRepository.setHiddenTools(updated)
                              },
                              modifier = Modifier
                                .testTag("tool_visibility_checkbox_${tool.name}")
                                .testTag("tool_settings_checkbox_${tool.name}")
                                .testTag("tool_visibility_eye_${tool.name}"),
                            ) {
                              AnimatedContent(
                                targetState = isVisible,
                                transitionSpec = {
                                  (expandVertically(
                                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                                    expandFrom = Alignment.CenterVertically,
                                  ) + fadeIn(animationSpec = tween(220)) + scaleIn(
                                    initialScale = 0.8f,
                                    animationSpec = tween(220),
                                  )).togetherWith(
                                    shrinkVertically(
                                      animationSpec = tween(180, easing = FastOutSlowInEasing),
                                      shrinkTowards = Alignment.CenterVertically,
                                    ) + fadeOut(animationSpec = tween(180)) + scaleOut(
                                      targetScale = 0.8f,
                                      animationSpec = tween(180),
                                    )
                                  )
                                },
                                label = "eye_toggle_${tool.name}",
                              ) { visible ->
                                Icon(
                                  imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                  contentDescription = if (visible) {
                                    "Hide ${tool.title} from navigation"
                                  } else {
                                    "Show ${tool.title} in navigation"
                                  },
                                  tint = eyeColor,
                                  modifier = Modifier.size(24.dp),
                                )
                              }
                            }
                          }

                          Surface(
                            modifier = Modifier
                              .size(32.dp)
                              .clip(CircleShape)
                              .clickable { onSelectTool?.invoke(tool) }
                              .testTag("tool_settings_icon_${tool.name}"),
                            shape = CircleShape,
                            color = if (isVisible) {
                              MaterialTheme.colorScheme.primaryContainer
                            } else {
                              MaterialTheme.colorScheme.surfaceVariant
                            },
                            contentColor = if (isVisible) {
                              MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                              MaterialTheme.colorScheme.onSurfaceVariant
                            },
                          ) {
                            Box(contentAlignment = Alignment.Center) {
                              Icon(
                                imageVector = tool.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                              )
                            }
                          }

                          Column(
                            modifier = Modifier
                              .weight(1f)
                              .clip(RoundedCornerShape(8.dp))
                              .clickable { onSelectTool?.invoke(tool) }
                              .padding(vertical = 6.dp, horizontal = 4.dp)
                              .testTag("tool_settings_text_${tool.name}"),
                          ) {
                            Text(
                              text = tool.title,
                              style = MaterialTheme.typography.bodyMedium,
                              fontWeight = if (isVisible) FontWeight.Medium else FontWeight.Normal,
                              color = if (isVisible) {
                                MaterialTheme.colorScheme.onSurface
                              } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                              },
                            )
                          }

                          val usage = toolUsageCounts[tool] ?: 0
                          if (usage > 0) {
                            Surface(
                              shape = CircleShape,
                              color = MaterialTheme.colorScheme.secondaryContainer,
                              contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ) {
                              Text(
                                text = "$usage ${if (usage == 1) "use" else "uses"}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                              )
                            }
                          }

                          IconButton(
                            onClick = {
                              addToolToHomeScreen(context, tool)
                            },
                            modifier = Modifier.testTag("add_to_home_${tool.name}"),
                          ) {
                            Icon(
                              imageVector = Icons.AutoMirrored.Filled.AddToHomeScreen,
                              contentDescription = "Add ${tool.title} to Home screen",
                              tint = MaterialTheme.colorScheme.primary,
                              modifier = Modifier.size(20.dp),
                            )
                          }
                        }
                      }
                    }
                  }
                }
              }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = "Total tool uses: $totalToolUses",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("tool_settings_total_uses_text"),
              )

              OutlinedButton(
                onClick = { preferencesRepository.resetToolUsage() },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                  .testTag("tool_settings_reset_stats_button")
                  .testTag("reset_tool_usage_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.RestartAlt,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Reset Stats",
                  style = MaterialTheme.typography.labelMedium,
                )
              }
            }
          }
        }
      }
    }

    // Section 6: App Permissions & Tool Usage Statistics
    val totalPermissionsCount = AppPermission.entries.size
    val grantedPermissionsCount = AppPermission.entries.count { perm ->
      permissionGrantStates[perm] != false
    }
    val totalPermissionUseEvents = AppPermission.entries.sumOf { perm ->
      permissionUsageStats[perm]?.values?.sumOf { it.useCount } ?: 0
    }

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("permissions_card")
        .testTag("settings_permissions_card"),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
      ),
      shape = RoundedCornerShape(16.dp),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Collapsible Header Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { isPermissionsExpanded = !isPermissionsExpanded }
            .testTag("permissions_toggle")
            .testTag("settings_permissions_toggle"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp),
            )
            Column {
              Text(
                text = "Permissions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
              )
              Text(
                text = "$grantedPermissionsCount of $totalPermissionsCount granted • $totalPermissionUseEvents total uses",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("permissions_header_summary"),
              )
            }
          }

          val permissionsChevronAngle by animateFloatAsState(
            targetValue = if (isPermissionsExpanded) 180f else 0f,
            label = "permissions_chevron",
          )

          IconButton(
            onClick = { isPermissionsExpanded = !isPermissionsExpanded },
          ) {
            Icon(
              imageVector = Icons.Default.ExpandMore,
              contentDescription = if (isPermissionsExpanded) "Collapse permissions" else "Expand permissions",
              modifier = Modifier.rotate(permissionsChevronAngle),
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        AnimatedVisibility(
          visible = isPermissionsExpanded,
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut(),
        ) {
          Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Text(
              text = "All permissions used by Toolbox, with per-tool usage counts, last used timestamps, and controls to grant or revoke each permission.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              OutlinedButton(
                onClick = { preferencesRepository.grantAllPermissions() },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("permissions_grant_all_button"),
              ) {
                Text(
                  text = "Grant All",
                  style = MaterialTheme.typography.labelMedium,
                )
              }
              OutlinedButton(
                onClick = { preferencesRepository.revokeAllPermissions() },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("permissions_revoke_all_button"),
              ) {
                Text(
                  text = "Revoke All",
                  style = MaterialTheme.typography.labelMedium,
                )
              }
              OutlinedButton(
                onClick = { preferencesRepository.resetPermissionUsage() },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("permissions_reset_stats_button"),
              ) {
                Text(
                  text = "Reset Stats",
                  style = MaterialTheme.typography.labelMedium,
                )
              }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              AppPermission.entries.forEach { permission ->
                val isGranted = permissionGrantStates[permission] != false
                val permToolStatsMap = permissionUsageStats[permission].orEmpty()
                val totalPermUses = permToolStatsMap.values.sumOf { it.useCount }
                val lastPermTimestampMs = permToolStatsMap.values.maxOfOrNull { it.lastUsedTimestampMs } ?: 0L
                val lastPermFormatted = formatPermissionUsageTimestamp(lastPermTimestampMs)
                val permKey = permission.name.lowercase()

                OutlinedCard(
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("permission_card_$permKey"),
                  shape = RoundedCornerShape(12.dp),
                  border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                      if (isGranted) {
                        MaterialTheme.colorScheme.outlineVariant
                      } else {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                      }
                    ),
                  ),
                  colors = CardDefaults.outlinedCardColors(
                    containerColor = if (isGranted) {
                      MaterialTheme.colorScheme.surfaceContainerLowest
                    } else {
                      MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.14f)
                    },
                  ),
                ) {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                      Column(
                        modifier = Modifier
                          .weight(1f)
                          .padding(end = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                      ) {
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                          Text(
                            text = permission.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                          )
                          Surface(
                            shape = CircleShape,
                            color = if (isGranted) {
                              MaterialTheme.colorScheme.primaryContainer
                            } else {
                              MaterialTheme.colorScheme.errorContainer
                            },
                            contentColor = if (isGranted) {
                              MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                              MaterialTheme.colorScheme.onErrorContainer
                            },
                            modifier = Modifier.testTag("permission_status_$permKey"),
                          ) {
                            Text(
                              text = if (isGranted) "Granted" else "Revoked",
                              style = MaterialTheme.typography.labelSmall,
                              fontWeight = FontWeight.SemiBold,
                              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                          }
                        }

                        Text(
                          text = permission.manifestPermission,
                          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                          fontFamily = FontFamily.Monospace,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis,
                        )
                      }

                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                      ) {
                        IconButton(
                          onClick = {
                            if (!isInspection) {
                              openAppPermissionSystemSettings(context, permission)
                            }
                          },
                          modifier = Modifier
                            .size(32.dp)
                            .testTag("permission_system_settings_$permKey"),
                        ) {
                          Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open system settings for ${permission.title}",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                          )
                        }

                        if (isGranted) {
                          OutlinedButton(
                            onClick = {
                              preferencesRepository.revokePermission(permission)
                              if (!isInspection && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                try {
                                  context.revokeSelfPermissionsOnKill(permission.manifestPermissions)
                                } catch (_: Throwable) {}
                              }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                              .testTag("permission_toggle_$permKey")
                              .testTag("permission_revoke_$permKey"),
                          ) {
                            Text(
                              text = "Revoke",
                              style = MaterialTheme.typography.labelSmall,
                              fontWeight = FontWeight.SemiBold,
                            )
                          }
                        } else {
                          Button(
                            onClick = {
                              preferencesRepository.grantPermission(permission)
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                              .testTag("permission_toggle_$permKey")
                              .testTag("permission_grant_$permKey"),
                          ) {
                            Text(
                              text = "Grant",
                              style = MaterialTheme.typography.labelSmall,
                              fontWeight = FontWeight.SemiBold,
                            )
                          }
                        }
                      }
                    }

                    Text(
                      text = permission.description,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Text(
                      text = "Total uses: $totalPermUses • Last used: $lastPermFormatted",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.testTag("permission_summary_$permKey"),
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    Column(
                      modifier = Modifier.fillMaxWidth(),
                      verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                      permission.toolsUsingPermission.forEach { tool ->
                        val toolStat = permToolStatsMap[tool]
                          ?: PermissionToolUsageStat(permission = permission, tool = tool)
                        val toolKey = tool.name.lowercase()

                        Row(
                          modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onSelectTool?.invoke(tool) }
                            .padding(vertical = 3.dp, horizontal = 4.dp)
                            .testTag("permission_tool_row_${permKey}_$toolKey"),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f),
                          ) {
                            Icon(
                              imageVector = tool.icon,
                              contentDescription = null,
                              tint = MaterialTheme.colorScheme.primary,
                              modifier = Modifier.size(15.dp),
                            )
                            Text(
                              text = tool.title,
                              style = MaterialTheme.typography.bodySmall,
                              fontWeight = FontWeight.Medium,
                              color = MaterialTheme.colorScheme.onSurface,
                              maxLines = 1,
                              overflow = TextOverflow.Ellipsis,
                            )
                          }

                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                          ) {
                            Surface(
                              shape = CircleShape,
                              color = if (toolStat.useCount > 0) {
                                MaterialTheme.colorScheme.secondaryContainer
                              } else {
                                MaterialTheme.colorScheme.surfaceVariant
                              },
                              contentColor = if (toolStat.useCount > 0) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                              } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                              },
                            ) {
                              Text(
                                text = "${toolStat.useCount} ${if (toolStat.useCount == 1) "use" else "uses"}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                  .padding(horizontal = 6.dp, vertical = 1.dp)
                                  .testTag("permission_tool_count_${permKey}_$toolKey"),
                              )
                            }

                            Text(
                              text = "Last: ${toolStat.lastUsedFormatted}",
                              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                              fontFamily = FontFamily.Monospace,
                              color = MaterialTheme.colorScheme.onSurfaceVariant,
                              modifier = Modifier.testTag("permission_tool_timestamp_${permKey}_$toolKey"),
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
        }
      }
    }

    // Section 7: Backup & Restore Settings and Data
    var backupRestoreStatus by rememberSaveable { mutableStateOf<String?>(null) }
    var dataBackupRestoreStatus by rememberSaveable { mutableStateOf<String?>(null) }
    var showResetConfirmDialog by rememberSaveable { mutableStateOf(false) }

    val backupDocumentLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
      if (uri != null) {
        try {
          val jsonContent = exportSettingsToJson(preferencesRepository)
          context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(jsonContent.toByteArray(Charsets.UTF_8))
          }
          backupRestoreStatus = "Settings backed up successfully."
          Toast.makeText(context, "Settings backed up to file", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
          preferencesRepository.logError(
            throwable = e,
            source = "Settings",
            customMessage = "Failed to export settings to file.",
          )
          backupRestoreStatus = "Failed to export settings to file."
          Toast.makeText(context, "Failed to backup settings", Toast.LENGTH_SHORT).show()
        }
      }
    }

    val restoreDocumentLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
      if (uri != null) {
        try {
          val jsonContent = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader(Charsets.UTF_8).readText()
          } ?: ""
          val imported = importSettingsFromJson(jsonContent, preferencesRepository)
          if (imported) {
            val restoredThemeColor = preferencesRepository.getThemeColor()
            val restoredAccentColor = preferencesRepository.getAccentColor()
            customHexInput = if (
              restoredThemeColor != DEFAULT_THEME_COLOR_SYSTEM &&
              !isMusicPlayerThemeColorSetting(restoredThemeColor)
            ) {
              restoredThemeColor
            } else {
              ""
            }
            customAccentHexInput = if (!isAutoAccentColorSetting(restoredAccentColor)) restoredAccentColor else ""
            AccentHarmonyOption.fromPrefValue(restoredAccentColor, darkTheme = isDarkTheme)?.let { restoredHarmony ->
              selectedHarmonyMode = restoredHarmony.harmonyMode
            }
            backupRestoreStatus = "Settings restored successfully."
            Toast.makeText(context, "Settings restored from file", Toast.LENGTH_SHORT).show()
          } else {
            preferencesRepository.logError(
              message = "Invalid or unrecognized settings backup file.",
              source = "Settings",
            )
            backupRestoreStatus = "Invalid or unrecognized settings backup file."
            Toast.makeText(context, "Invalid settings backup file", Toast.LENGTH_SHORT).show()
          }
        } catch (e: Exception) {
          preferencesRepository.logError(
            throwable = e,
            source = "Settings",
            customMessage = "Failed to read settings backup file.",
          )
          backupRestoreStatus = "Failed to read settings backup file."
          Toast.makeText(context, "Failed to restore settings", Toast.LENGTH_SHORT).show()
        }
      }
    }

    val backupDataDocumentLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
      if (uri != null) {
        try {
          val jsonContent = exportDataToJson(context)
          context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(jsonContent.toByteArray(Charsets.UTF_8))
          }
          dataBackupRestoreStatus = "Data backed up successfully."
          Toast.makeText(context, "Data backed up to file", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
          preferencesRepository.logError(
            throwable = e,
            source = "Settings",
            customMessage = "Failed to export data to file.",
          )
          dataBackupRestoreStatus = "Failed to export data to file."
          Toast.makeText(context, "Failed to backup data", Toast.LENGTH_SHORT).show()
        }
      }
    }

    val restoreDataDocumentLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
      if (uri != null) {
        try {
          val jsonContent = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader(Charsets.UTF_8).readText()
          } ?: ""
          val imported = importDataFromJson(jsonContent, context)
          if (imported) {
            dataBackupRestoreStatus = "Data restored successfully."
            Toast.makeText(context, "Data restored from file", Toast.LENGTH_SHORT).show()
          } else {
            preferencesRepository.logError(
              message = "Invalid or unrecognized data backup file.",
              source = "Settings",
            )
            dataBackupRestoreStatus = "Invalid or unrecognized data backup file."
            Toast.makeText(context, "Invalid data backup file", Toast.LENGTH_SHORT).show()
          }
        } catch (e: Exception) {
          preferencesRepository.logError(
            throwable = e,
            source = "Settings",
            customMessage = "Failed to read data backup file.",
          )
          dataBackupRestoreStatus = "Failed to read data backup file."
          Toast.makeText(context, "Failed to restore data", Toast.LENGTH_SHORT).show()
        }
      }
    }

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("settings_backup_restore_card")
        .testTag("backup_restore_card"),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
      ),
      shape = RoundedCornerShape(16.dp),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Collapsible Header Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { isBackupRestoreExpanded = !isBackupRestoreExpanded }
            .testTag("settings_backup_restore_toggle")
            .testTag("backup_restore_toggle"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
          ) {
            Icon(
              imageVector = Icons.Default.SaveAlt,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp),
            )
            Column {
              Text(
                text = "Backup & Restore",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
              )
              Text(
                text = "Export or import settings and tool data as JSON",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("backup_restore_summary"),
              )
            }
          }

          val backupRestoreRotationAngle by animateFloatAsState(
            targetValue = if (isBackupRestoreExpanded) 180f else 0f,
            label = "backup_restore_chevron",
          )

          IconButton(
            onClick = { isBackupRestoreExpanded = !isBackupRestoreExpanded },
          ) {
            Icon(
              imageVector = Icons.Default.ExpandMore,
              contentDescription = if (isBackupRestoreExpanded) "Collapse backup and restore" else "Expand backup and restore",
              modifier = Modifier.rotate(backupRestoreRotationAngle),
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        AnimatedVisibility(
          visible = isBackupRestoreExpanded,
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut(),
        ) {
          Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            // Settings Backup & Restore
            Text(
              text = "Settings",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
              text = "Save your current settings to a JSON file or restore settings from a previous backup.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              OutlinedButton(
                onClick = {
                  backupDocumentLauncher.launch(DEFAULT_SETTINGS_BACKUP_FILENAME)
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("backup_settings_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.FileUpload,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Backup")
              }

              OutlinedButton(
                onClick = {
                  restoreDocumentLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("restore_settings_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.FileDownload,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Restore")
              }
            }

            backupRestoreStatus?.let { statusMessage ->
              Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("backup_restore_status"),
              )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Data Backup & Restore
            Text(
              text = "Data",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
              text = "Save your tool data (notes, tasks, custom recipes, plant schedule, saved dice rolls, custom tunings, progressions, and music playlists/favorites/stats) to a JSON file or restore data from a previous backup.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              OutlinedButton(
                onClick = {
                  backupDataDocumentLauncher.launch(DEFAULT_DATA_BACKUP_FILENAME)
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("backup_data_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.FileUpload,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Backup")
              }

              OutlinedButton(
                onClick = {
                  restoreDataDocumentLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("restore_data_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.FileDownload,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Restore")
              }
            }

            dataBackupRestoreStatus?.let { statusMessage ->
              Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("data_backup_restore_status"),
              )
            }
          }
        }
      }
    }

    // Reset to defaults
    OutlinedButton(
      onClick = {
        showResetConfirmDialog = true
      },
      shape = CircleShape,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("reset_defaults_button"),
    ) {
      Icon(
        imageVector = Icons.Default.RestartAlt,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text("Reset All Settings to Defaults")
    }

    // Report a Bug & Request a Feature (side-by-side rounded buttons, 1% chance for devil & angel icons)
    val useDevilAndAngelIcons = rememberSaveable {
      shouldUseDevilAndAngelFeedbackIcons(feedbackIconsRandom)
    }
    val reportBugIcon = remember(useDevilAndAngelIcons) {
      resolveReportBugButtonIcon(useDevilAndAngelIcons)
    }
    val requestFeatureIcon = remember(useDevilAndAngelIcons) {
      resolveRequestFeatureButtonIcon(useDevilAndAngelIcons)
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("github_feedback_buttons_row"),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      OutlinedButton(
        onClick = {
          onOpenUrl?.invoke(GITHUB_REPORT_BUG_URL) ?: openExternalUrl(context, GITHUB_REPORT_BUG_URL)
        },
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        modifier = Modifier
          .weight(1f)
          .testTag("report_bug_button"),
      ) {
        Icon(
          imageVector = reportBugIcon,
          contentDescription = null,
          modifier = Modifier
            .size(18.dp)
            .testTag(if (useDevilAndAngelIcons) "report_bug_devil_icon" else "report_bug_default_icon"),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Report a Bug",
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      OutlinedButton(
        onClick = {
          onOpenUrl?.invoke(GITHUB_REQUEST_FEATURE_URL) ?: openExternalUrl(context, GITHUB_REQUEST_FEATURE_URL)
        },
        shape = CircleShape,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        modifier = Modifier
          .weight(1f)
          .testTag("request_feature_button"),
      ) {
        Icon(
          imageVector = requestFeatureIcon,
          contentDescription = null,
          modifier = Modifier
            .size(18.dp)
            .testTag(if (useDevilAndAngelIcons) "request_feature_angel_icon" else "request_feature_default_icon"),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Request a Feature",
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }

    // Error Logs button
    OutlinedButton(
      onClick = {
        showErrorLogsDialog = true
      },
      shape = CircleShape,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("error_logs_button"),
    ) {
      Icon(
        imageVector = Icons.Default.BugReport,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text("Error Logs")
    }

    // License button below Error Logs
    OutlinedButton(
      onClick = {
        showLicenseDialog = true
      },
      shape = CircleShape,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("license_button"),
    ) {
      Icon(
        imageVector = Icons.Default.Gavel,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text("License")
    }

    if (showResetConfirmDialog) {
      AlertDialog(
        onDismissRequest = { showResetConfirmDialog = false },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp,
        modifier = Modifier.testTag("reset_settings_confirm_dialog"),
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
                .background(MaterialTheme.colorScheme.errorContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.RestartAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp),
              )
            }
            Column {
              Text(
                text = "Reset All Settings?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "Restore defaults across all tools",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        },
        text = {
          Text(
            text = "Are you sure you want to reset all settings (theme, colors, tool order, visibility, and usage stats) back to their defaults?",
          )
        },
        confirmButton = {
          Button(
            onClick = {
              resetAllSettingsToDefaults(preferencesRepository)
              customHexInput = ""
              customAccentHexInput = ""
              selectedHarmonyMode = HarmonyMode.ANALOGOUS
              collapsedCategories = ToolCategory.entries.toSet()
              isSortingExpanded = false
              isToolSettingsExpanded = false
              isPermissionsExpanded = false
              isThemeColorExpanded = false
              isAccentColorExpanded = false
              isBackgroundTextureExpanded = false
              isBackupRestoreExpanded = false
              backupRestoreStatus = null
              dataBackupRestoreStatus = null
              showResetConfirmDialog = false
            },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.error,
              contentColor = MaterialTheme.colorScheme.onError,
            ),
            modifier = Modifier.testTag("confirm_reset_settings_button"),
          ) {
            Text("Reset")
          }
        },
        dismissButton = {
          TextButton(
            onClick = { showResetConfirmDialog = false },
            shape = CircleShape,
            modifier = Modifier.testTag("cancel_reset_settings_button"),
          ) {
            Text("Cancel")
          }
        },
      )
    }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
