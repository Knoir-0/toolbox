package com.example.toolbox.ui.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toolbox.ui.main.ACCENT_COLOR_MUSIC_PLAYER
import com.example.toolbox.ui.main.CategorySortingOption
import com.example.toolbox.ui.main.CategoryToolSortingOption
import com.example.toolbox.ui.main.DEFAULT_ACCENT_COLOR_AUTO
import com.example.toolbox.ui.main.DEFAULT_SETTINGS_BACKUP_FILENAME
import com.example.toolbox.ui.main.DEFAULT_THEME_COLOR_SYSTEM
import com.example.toolbox.ui.main.FALLBACK_THEME_COLOR_HEX
import com.example.toolbox.ui.main.InMemoryNavigationPreferencesRepository
import com.example.toolbox.ui.main.MAX_TOOLS_PER_ROW
import com.example.toolbox.ui.main.MIN_TOOLS_PER_ROW
import com.example.toolbox.ui.main.NavigationPreferencesRepository
import com.example.toolbox.ui.main.ThemeMode
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.ToolSortOrder
import com.example.toolbox.ui.main.ToolSortingOption
import com.example.toolbox.ui.main.coerceToolsPerRow
import com.example.toolbox.ui.main.exportSettingsToJson
import com.example.toolbox.ui.main.importSettingsFromJson
import com.example.toolbox.ui.main.rememberNavigationPreferencesRepository
import com.example.toolbox.ui.main.resetAllSettingsToDefaults
import com.example.toolbox.ui.theme.AccentHarmonyOption
import com.example.toolbox.ui.theme.MusicPlayerColorOption
import com.example.toolbox.ui.theme.buildMusicPlayerAccentPrefValue
import com.example.toolbox.ui.theme.calculateAccentHarmonyColor
import com.example.toolbox.ui.theme.calculateComplementaryColor
import com.example.toolbox.ui.theme.colorToHexString
import com.example.toolbox.ui.theme.isAutoAccentColorSetting
import com.example.toolbox.ui.theme.isMusicPlayerAccentColorSetting
import com.example.toolbox.ui.theme.parseHexColor
import com.example.toolbox.ui.theme.resolveMusicPlayerHarmonyOption
import kotlin.math.roundToInt

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
  } catch (_: Exception) {
    Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  modifier: Modifier = Modifier,
  preferencesRepository: NavigationPreferencesRepository = rememberNavigationPreferencesRepository(),
  initialVisibilityExpanded: Boolean = true,
  initialCollapsedCategories: Set<ToolCategory> = remember { ToolCategory.entries.toSet() },
  initialThemeColorExpanded: Boolean = false,
  initialAccentColorExpanded: Boolean = false,
  onOpenUrl: ((String) -> Unit)? = null,
) {
  val themeMode by preferencesRepository.themeModeFlow.collectAsState()
  val themeColor by preferencesRepository.themeColorFlow.collectAsState()
  val accentColor by preferencesRepository.accentColorFlow.collectAsState()
  val hiddenTools by preferencesRepository.hiddenToolsFlow.collectAsState()
  val toolSorting by preferencesRepository.toolSortingFlow.collectAsState()
  val toolSortOrder by preferencesRepository.toolSortOrderFlow.collectAsState()
  val categorySorting by preferencesRepository.categorySortingFlow.collectAsState()
  val categoryToolSorting by preferencesRepository.categoryToolSortingFlow.collectAsState()
  val toolUsageCounts by preferencesRepository.toolUsageCountsFlow.collectAsState()
  val toolsPerRow by preferencesRepository.toolsPerRowFlow.collectAsState()

  var isThemeColorExpanded by rememberSaveable { mutableStateOf(initialThemeColorExpanded) }
  var isAccentColorExpanded by rememberSaveable { mutableStateOf(initialAccentColorExpanded) }

  var customHexInput by rememberSaveable(themeColor) {
    mutableStateOf(if (themeColor != DEFAULT_THEME_COLOR_SYSTEM) themeColor else "")
  }

  var customAccentHexInput by rememberSaveable(accentColor) {
    mutableStateOf(
      if (!isAutoAccentColorSetting(accentColor) && !isMusicPlayerAccentColorSetting(accentColor)) {
        accentColor
      } else {
        ""
      }
    )
  }

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(scrollState)
      .padding(horizontal = 20.dp, vertical = 16.dp)
      .testTag("settings_screen"),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Surface(
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
          )
        }
      }
      Column {
        Text(
          text = "Settings",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "Customize theme, colors, and toolbox tools",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

    // Section 1: Toolbox Default Sorting
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
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Sort,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
          )
          Text(
            text = "Default Sorting",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
          )
        }

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
    val activeThemeColorPreset = PRESET_COLORS.find { it.hex.equals(themeColor, ignoreCase = true) }
    val themeColorSummary = when {
      themeColor == DEFAULT_THEME_COLOR_SYSTEM -> "System Preset"
      activeThemeColorPreset != null -> "${activeThemeColorPreset.name} (${activeThemeColorPreset.hex})"
      else -> "Custom ($themeColor)"
    }
    val activeThemeColorBadge = when {
      themeColor == DEFAULT_THEME_COLOR_SYSTEM -> {
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
            val isSystemSelected = themeColor == DEFAULT_THEME_COLOR_SYSTEM
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
                    .testTag("color_preset_${preset.hex.removePrefix("#").lowercase()}"),
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
    val autoComplementColor = remember(currentPrimary) {
      calculateComplementaryColor(currentPrimary)
    }
    val activeAutoHarmony = remember(accentColor) {
      AccentHarmonyOption.fromPrefValue(accentColor)
    }
    val activeMusicOption = remember(accentColor) {
      MusicPlayerColorOption.fromPrefValue(accentColor)
    }
    val activeMusicHarmony = remember(accentColor, activeMusicOption) {
      if (activeMusicOption != null) resolveMusicPlayerHarmonyOption(accentColor) else null
    }
    var selectedHarmonyOption by rememberSaveable {
      mutableStateOf(activeAutoHarmony ?: activeMusicHarmony ?: AccentHarmonyOption.COMPLEMENTARY)
    }
    val currentMusicPalette = MusicPlayerEngine.activeTrackPalette
    val currentMusicTrack = MusicPlayerEngine.activeTrack
    val activeMusicColor = remember(currentMusicPalette, activeMusicOption) {
      (activeMusicOption ?: MusicPlayerColorOption.DOMINANT).resolveColor(currentMusicPalette)
    }
    val activeMusicAccentColor = remember(activeMusicColor, activeMusicHarmony, selectedHarmonyOption) {
      calculateAccentHarmonyColor(activeMusicColor, activeMusicHarmony ?: selectedHarmonyOption)
    }
    var selectedHarmonyMode by rememberSaveable {
      mutableStateOf(
        (activeAutoHarmony ?: activeMusicHarmony)?.harmonyMode ?: HarmonyMode.COMPLEMENTARY
      )
    }
    LaunchedEffect(activeAutoHarmony, activeMusicHarmony) {
      val resolvedHarmony = activeAutoHarmony ?: activeMusicHarmony
      if (resolvedHarmony != null) {
        selectedHarmonyOption = resolvedHarmony
        if (resolvedHarmony != AccentHarmonyOption.MONOCHROMATIC) {
          selectedHarmonyMode = resolvedHarmony.harmonyMode
        }
      }
    }
    val activeAutoHarmonyColor = remember(currentPrimary, activeAutoHarmony, autoComplementColor) {
      if (activeAutoHarmony != null) {
        calculateAccentHarmonyColor(currentPrimary, activeAutoHarmony)
      } else {
        autoComplementColor
      }
    }
    val activeAccentPreset = ACCENT_PRESET_COLORS.find { it.hex.equals(accentColor, ignoreCase = true) }
    val accentColorSummary = when {
      activeAutoHarmony != null -> activeAutoHarmony.label
      activeMusicOption != null -> {
        val harmonyPart = (activeMusicHarmony ?: selectedHarmonyOption).shortLabel
        "${activeMusicOption.label} • $harmonyPart"
      }
      activeAccentPreset != null -> "${activeAccentPreset.name} (${activeAccentPreset.hex})"
      else -> "Custom ($accentColor)"
    }
    val activeAccentColorBadge = when {
      activeAutoHarmony != null -> activeAutoHarmonyColor
      activeMusicOption != null -> activeMusicAccentColor
      else -> parseHexColor(accentColor) ?: autoComplementColor
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
              text = "Accent color used for key tool highlights (action keys, tuning needle, metronome downbeat, calipers). Default complements the main theme color.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            val isAutoAccentSelected = activeAutoHarmony != null

            // Auto Option Card
            OutlinedCard(
              onClick = {
                selectedHarmonyMode = HarmonyMode.COMPLEMENTARY
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
                      text = activeAutoHarmony?.label ?: "Auto (Complement)",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                    )
                    Text(
                      text = activeAutoHarmony?.description ?: "Automatically complements your main theme color",
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

            val isMusicAccentSelected = activeMusicOption != null
            val harmonyBaseColor = if (isMusicAccentSelected) activeMusicColor else currentPrimary

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
                  text = if (isMusicAccentSelected) {
                    "Calculated off music player color"
                  } else {
                    "Tap harmony to auto-track theme"
                  },
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
                  val defaultOption = AccentHarmonyOption.defaultForMode(mode)
                  val isModeSelected = selectedHarmonyMode == mode && (isAutoAccentSelected || isMusicAccentSelected)
                  FilterChip(
                    selected = isModeSelected,
                    onClick = {
                      selectedHarmonyMode = mode
                      selectedHarmonyOption = defaultOption
                      if (activeMusicOption != null) {
                        preferencesRepository.setAccentColor(
                          buildMusicPlayerAccentPrefValue(
                            activeMusicOption,
                            defaultOption,
                          )
                        )
                      } else {
                        preferencesRepository.setAccentColor(defaultOption.prefValue)
                      }
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
                  val swatchColor = remember(harmonyBaseColor, swatchOption) {
                    calculateAccentHarmonyColor(harmonyBaseColor, swatchOption)
                  }
                  val swatchHex = remember(swatchColor) {
                    colorToHexString(swatchColor)
                  }
                  val isSwatchSelected = if (isMusicAccentSelected) {
                    (activeMusicHarmony ?: selectedHarmonyOption) == swatchOption
                  } else {
                    activeAutoHarmony == swatchOption
                  }

                  Surface(
                    onClick = {
                      selectedHarmonyOption = swatchOption
                      if (activeMusicOption != null) {
                        preferencesRepository.setAccentColor(
                          buildMusicPlayerAccentPrefValue(
                            activeMusicOption,
                            swatchOption,
                          )
                        )
                      } else {
                        preferencesRepository.setAccentColor(swatchOption.prefValue)
                      }
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

            // Music Player Colors Option Card & Swatches
            OutlinedCard(
              onClick = {
                preferencesRepository.setAccentColor(
                  buildMusicPlayerAccentPrefValue(
                    activeMusicOption ?: MusicPlayerColorOption.DOMINANT,
                    activeMusicHarmony ?: selectedHarmonyOption,
                  )
                )
                customAccentHexInput = ""
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("accent_preset_music_player"),
              shape = RoundedCornerShape(12.dp),
              border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                  if (isMusicAccentSelected) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.outlineVariant
                ),
              ),
              colors = CardDefaults.outlinedCardColors(
                containerColor = if (isMusicAccentSelected) {
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
                      .background(activeMusicAccentColor),
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      imageVector = if (isMusicAccentSelected) Icons.Default.Check else Icons.Default.MusicNote,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(15.dp),
                    )
                  }
                  Column {
                    Text(
                      text = activeMusicOption?.label ?: "Music Player Colors",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                    )
                    Text(
                      text = if (currentMusicTrack != null) {
                        val harmonyPart = (activeMusicHarmony ?: selectedHarmonyOption).shortLabel
                        "Follows ${currentMusicTrack.title} (${activeMusicOption?.swatchLabel ?: "Dominant"} • $harmonyPart)"
                      } else {
                        activeMusicOption?.description ?: "Matches app background to Music Player and calculates accent via harmony"
                      },
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }
                }

                if (isMusicAccentSelected) {
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

            Column(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("accent_music_player_swatches_section"),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "Music Player Palette",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = currentMusicTrack?.let { "${it.title} • ${it.artist}" } ?: "Tap color to track music player",
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
                  val isSwatchSelected = activeMusicOption == musicOption

                  Surface(
                    onClick = {
                      preferencesRepository.setAccentColor(
                        buildMusicPlayerAccentPrefValue(
                          musicOption,
                          activeMusicHarmony ?: selectedHarmonyOption,
                        )
                      )
                      customAccentHexInput = ""
                    },
                    modifier = Modifier
                      .weight(1f)
                      .testTag("accent_music_swatch_${musicOption.name.lowercase()}"),
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

    // Section 5: Tool Settings (Collapsible Panel with Collapsible Categories)
    var isToolSettingsExpanded by rememberSaveable { mutableStateOf(initialVisibilityExpanded) }
    val configurableTools = remember { Tool.entries.filter { it != Tool.SETTINGS } }
    val hiddenCount = configurableTools.count { it in hiddenTools }
    val visibleCount = configurableTools.size - hiddenCount
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
                            .then(
                              if (!isSettings) {
                                Modifier.clickable {
                                  val updated = if (isVisible) {
                                    hiddenTools + tool
                                  } else {
                                    hiddenTools - tool
                                  }
                                  preferencesRepository.setHiddenTools(updated)
                                }
                              } else {
                                Modifier
                              }
                            )
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
                            modifier = Modifier.size(32.dp),
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

                          Column(modifier = Modifier.weight(1f)) {
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
                              addToolToHomeScreen(context, tool, themeColor = themeColor)
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
          }
        }
      }
    }

    // Section 6: Backup & Restore Settings
    var backupRestoreStatus by rememberSaveable { mutableStateOf<String?>(null) }
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
        } catch (_: Exception) {
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
            customHexInput = if (restoredThemeColor != DEFAULT_THEME_COLOR_SYSTEM) restoredThemeColor else ""
            customAccentHexInput = if (!isAutoAccentColorSetting(restoredAccentColor)) restoredAccentColor else ""
            AccentHarmonyOption.fromPrefValue(restoredAccentColor)?.let { restoredHarmony ->
              selectedHarmonyMode = restoredHarmony.harmonyMode
            }
            backupRestoreStatus = "Settings restored successfully."
            Toast.makeText(context, "Settings restored from file", Toast.LENGTH_SHORT).show()
          } else {
            backupRestoreStatus = "Invalid or unrecognized settings backup file."
            Toast.makeText(context, "Invalid settings backup file", Toast.LENGTH_SHORT).show()
          }
        } catch (_: Exception) {
          backupRestoreStatus = "Failed to read settings backup file."
          Toast.makeText(context, "Failed to restore settings", Toast.LENGTH_SHORT).show()
        }
      }
    }

    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("settings_backup_restore_card"),
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
            imageVector = Icons.Default.SaveAlt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
          )
          Text(
            text = "Backup & Restore",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
          )
        }

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

    // Report a Bug & Request a Feature (side-by-side rounded buttons)
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
          imageVector = Icons.Default.BugReport,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
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
          imageVector = Icons.Default.Lightbulb,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Request a Feature",
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }

    if (showResetConfirmDialog) {
      AlertDialog(
        onDismissRequest = { showResetConfirmDialog = false },
        modifier = Modifier.testTag("reset_settings_confirm_dialog"),
        title = {
          Text(
            text = "Reset All Settings?",
            fontWeight = FontWeight.Bold,
          )
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
              selectedHarmonyMode = HarmonyMode.COMPLEMENTARY
              collapsedCategories = ToolCategory.entries.toSet()
              isToolSettingsExpanded = true
              isThemeColorExpanded = false
              isAccentColorExpanded = false
              backupRestoreStatus = null
              showResetConfirmDialog = false
            },
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
