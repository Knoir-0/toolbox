package com.example.toolbox.ui.tools

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.toolbox.ui.main.DEFAULT_THEME_COLOR_SYSTEM
import com.example.toolbox.ui.main.FALLBACK_THEME_COLOR_HEX
import com.example.toolbox.ui.main.InMemoryNavigationPreferencesRepository
import com.example.toolbox.ui.main.MAX_TOOLS_PER_ROW
import com.example.toolbox.ui.main.MIN_TOOLS_PER_ROW
import com.example.toolbox.ui.main.NavigationPreferencesRepository
import com.example.toolbox.ui.main.ThemeMode
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.ToolSortingOption
import com.example.toolbox.ui.main.coerceToolsPerRow
import com.example.toolbox.ui.main.rememberNavigationPreferencesRepository
import com.example.toolbox.ui.theme.parseHexColor
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  modifier: Modifier = Modifier,
  preferencesRepository: NavigationPreferencesRepository = rememberNavigationPreferencesRepository(),
  initialVisibilityExpanded: Boolean = false,
) {
  val themeMode by preferencesRepository.themeModeFlow.collectAsState()
  val themeColor by preferencesRepository.themeColorFlow.collectAsState()
  val hiddenTools by preferencesRepository.hiddenToolsFlow.collectAsState()
  val toolSorting by preferencesRepository.toolSortingFlow.collectAsState()
  val toolUsageCounts by preferencesRepository.toolUsageCountsFlow.collectAsState()
  val toolsPerRow by preferencesRepository.toolsPerRowFlow.collectAsState()

  var customHexInput by rememberSaveable {
    mutableStateOf(if (themeColor != DEFAULT_THEME_COLOR_SYSTEM) themeColor else "")
  }

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
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
            Triple(ToolSortingOption.USAGE, "By usage", Icons.AutoMirrored.Filled.TrendingUp),
          )

          sortingOptions.forEachIndexed { index, (option, label, icon) ->
            SegmentedButton(
              selected = toolSorting == option,
              onClick = { preferencesRepository.setToolSorting(option) },
              shape = SegmentedButtonDefaults.itemShape(index = index, count = sortingOptions.size),
              icon = {
                Icon(
                  imageVector = icon,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
              },
              modifier = Modifier.testTag("sorting_option_${option.name.lowercase()}"),
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
              )
            }
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
              icon = {
                Icon(
                  imageVector = icon,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
              },
              modifier = Modifier.testTag("theme_mode_${mode.name.lowercase()}"),
            ) {
              Text(text = label, style = MaterialTheme.typography.labelMedium)
            }
          }
        }
      }
    }

    // Section 3: Color Customization
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
      ),
      shape = RoundedCornerShape(16.dp),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Palette,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
          )
          Text(
            text = "Theme Color",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
          )
        }

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

    // Section 4: Tool Visibility (Collapsible Panel)
    var isToolVisibilityExpanded by rememberSaveable { mutableStateOf(initialVisibilityExpanded) }
    val configurableTools = remember { Tool.entries.filter { it != Tool.SETTINGS } }
    val hiddenCount = configurableTools.count { it in hiddenTools }
    val visibleCount = configurableTools.size - hiddenCount

    Card(
      modifier = Modifier
        .fillMaxWidth()
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
            .clickable { isToolVisibilityExpanded = !isToolVisibilityExpanded }
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
              imageVector = Icons.Default.Visibility,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp),
            )
            Column {
              Text(
                text = "Tool Visibility",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
              )
              Text(
                text = if (hiddenCount == 0) "All tools visible" else "$hiddenCount hidden • $visibleCount visible",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          val rotationAngle by animateFloatAsState(
            targetValue = if (isToolVisibilityExpanded) 180f else 0f,
            label = "tool_visibility_chevron",
          )

          IconButton(
            onClick = { isToolVisibilityExpanded = !isToolVisibilityExpanded },
          ) {
            Icon(
              imageVector = Icons.Default.ExpandMore,
              contentDescription = if (isToolVisibilityExpanded) "Collapse tool visibility" else "Expand tool visibility",
              modifier = Modifier.rotate(rotationAngle),
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        AnimatedVisibility(
          visible = isToolVisibilityExpanded,
          enter = expandVertically() + fadeIn(),
          exit = shrinkVertically() + fadeOut(),
        ) {
          Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Text(
              text = "Select which tools appear in the navigation bar. Settings is always visible and positioned as the last tool.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Tool List (excluding Settings since it cannot be hidden)
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
              configurableTools.forEach { tool ->
                val isVisible = tool !in hiddenTools

                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                      val updated = if (isVisible) {
                        hiddenTools + tool
                      } else {
                        hiddenTools - tool
                      }
                      preferencesRepository.setHiddenTools(updated)
                    }
                    .padding(vertical = 2.dp, horizontal = 2.dp)
                    .testTag("tool_visibility_row_${tool.name}"),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Checkbox(
                    checked = isVisible,
                    onCheckedChange = { checked ->
                      val updated = if (checked) {
                        hiddenTools - tool
                      } else {
                        hiddenTools + tool
                      }
                      preferencesRepository.setHiddenTools(updated)
                    },
                    modifier = Modifier.testTag("tool_visibility_checkbox_${tool.name}"),
                    colors = CheckboxDefaults.colors(),
                  )

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

                    if (!isVisible) {
                      Text(
                        text = "Hidden from navigation",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                      )
                    }
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
                }
              }
            }
          }
        }
      }
    }

    // Reset to defaults
    OutlinedButton(
      onClick = {
        preferencesRepository.setThemeMode(ThemeMode.SYSTEM)
        preferencesRepository.setThemeColor(DEFAULT_THEME_COLOR_SYSTEM)
        preferencesRepository.setHiddenTools(emptySet())
        preferencesRepository.setToolOrder(Tool.entries)
        preferencesRepository.setToolSorting(ToolSortingOption.USER_CHOICE)
        preferencesRepository.resetToolUsage()
        customHexInput = ""
      },
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

    Spacer(modifier = Modifier.height(24.dp))
  }
}
