package com.example.toolbox.ui.tools

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import android.graphics.SurfaceTexture
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.os.Looper
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.toolbox.ui.main.AppPermission
import com.example.toolbox.ui.main.NavigationPreferencesBridge
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.theme.Button
import com.example.toolbox.ui.theme.CircleShape
import com.example.toolbox.ui.theme.FilledTonalButton
import com.example.toolbox.ui.theme.FilterChip
import com.example.toolbox.ui.theme.OutlinedButton
import com.example.toolbox.ui.theme.TextButton
import com.example.toolbox.ui.theme.ToolboxTheme
import com.example.toolbox.ui.theme.parseHexColor
import com.example.toolbox.ui.theme.toolboxScreenBackground
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Primary navigation tabs for the Stars astrology tool.
 */
enum class StarsTab(
  val label: String,
) {
  TODAY("Today"),
  CHART("Chart"),
  SKY_AR("Sky AR"),
  MATCH("Match"),
  GUIDE("Guide"),
}

/**
 * Reading sub-sections on the Today tab.
 */
enum class DailyReadingSection(val label: String) {
  OVERVIEW("Overview"),
  LOVE("Love"),
  CAREER("Career & Vibe"),
  DOS_DONTS("Do's & Don'ts"),
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StarsScreen(
  modifier: Modifier = Modifier,
  repository: StarsRepository? = null,
  onOpenColorPicker: ((String) -> Unit)? = null,
  onOpenNotes: (() -> Unit)? = null,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptics = LocalHapticFeedback.current
  val starsRepo = remember(repository, isInspection) {
    repository ?: if (isInspection) InMemoryStarsRepository() else SharedPreferencesStarsRepository(context)
  }

  var profiles by remember { mutableStateOf(starsRepo.loadProfiles()) }
  var activeProfileId by remember { mutableStateOf(starsRepo.getActiveProfileId()) }
  var partnerProfileId by remember { mutableStateOf(starsRepo.getMatchPartnerProfileId()) }
  var favoriteGuideIds by remember { mutableStateOf(starsRepo.loadFavoriteGuideIds()) }

  val activeProfile = remember(profiles, activeProfileId) {
    profiles.firstOrNull { it.id == activeProfileId } ?: profiles.first()
  }
  val partnerProfile = remember(profiles, partnerProfileId, activeProfile) {
    profiles.firstOrNull { it.id == partnerProfileId }
      ?: profiles.firstOrNull { it.id != activeProfile.id }
      ?: activeProfile
  }

  val activeNatalChart = remember(activeProfile) {
    StarsEngine.calculateNatalChart(activeProfile)
  }
  val partnerNatalChart = remember(partnerProfile) {
    StarsEngine.calculateNatalChart(partnerProfile)
  }

  var selectedTab by rememberSaveable { mutableStateOf(StarsTab.TODAY) }
  var dayOffset by rememberSaveable { mutableIntStateOf(0) } // -1 Yesterday, 0 Today, +1 Tomorrow, etc.
  var overrideSignName by rememberSaveable { mutableStateOf<String?>(null) }
  val selectedDailySign = remember(overrideSignName, activeNatalChart) {
    overrideSignName?.let { name -> ZodiacSign.entries.firstOrNull { it.name == name } }
      ?: activeNatalChart.sunSign
  }

  LaunchedEffect(selectedDailySign) {
    StarsEngine.updateActiveSign(selectedDailySign)
  }

  val targetCalendar = remember(dayOffset) {
    Calendar.getInstance().apply {
      add(Calendar.DAY_OF_YEAR, dayOffset)
    }
  }
  val dailyHoroscope = remember(dayOffset, selectedDailySign, activeNatalChart) {
    StarsEngine.generateDailyHoroscope(
      year = targetCalendar.get(Calendar.YEAR),
      month = targetCalendar.get(Calendar.MONTH) + 1,
      day = targetCalendar.get(Calendar.DAY_OF_MONTH),
      targetSign = selectedDailySign,
      natalChart = activeNatalChart,
    )
  }

  // Handle widget tab launch requests
  val pendingWidgetTab by StarsEngine.pendingWidgetTabFlow.collectAsState()
  LaunchedEffect(pendingWidgetTab) {
    val requested = StarsEngine.consumePendingWidgetTab() ?: return@LaunchedEffect
    StarsTab.entries.firstOrNull { it.name.equals(requested, ignoreCase = true) }?.let {
      selectedTab = it
    }
  }

  // Integrated toolbar search & catalog overlay state
  var isSearchActive by rememberSaveable { mutableStateOf(false) }
  var searchQuery by rememberSaveable { mutableStateOf("") }
  var searchCategory by rememberSaveable { mutableStateOf(StarsGuideCategory.ALL) }

  // Dialog states
  var inspectedGuideEntry by remember { mutableStateOf<StarsGuideEntry?>(null) }
  var inspectedPlacement by remember { mutableStateOf<PlanetaryPlacement?>(null) }
  var profileToEdit by remember { mutableStateOf<BirthProfile?>(null) }
  var isCreatingNewProfile by remember { mutableStateOf(false) }
  var showProfileSwitcherDialog by remember { mutableStateOf(false) }

  BackHandler(enabled = isSearchActive) {
    isSearchActive = false
    searchQuery = ""
  }

  val filteredCatalogEntries = remember(searchQuery, searchCategory, favoriteGuideIds) {
    val q = searchQuery.trim().lowercase(Locale.US)
    val tokens = q.split(Regex("\\s+")).filter { it.isNotEmpty() }
    StarsEngine.GUIDE_CATALOG.filter { entry ->
      val matchesCat = when (searchCategory) {
        StarsGuideCategory.ALL -> true
        StarsGuideCategory.FAVORITES -> entry.id in favoriteGuideIds
        else -> entry.category == searchCategory
      }
      if (!matchesCat) return@filter false
      if (tokens.isEmpty()) return@filter true
      val haystack = "${entry.title} ${entry.subtitle} ${entry.summary} ${entry.keywords}".lowercase(Locale.US)
      tokens.all { haystack.contains(it) }
    }
  }

  val topBarSubtext = remember(activeProfile, activeNatalChart, dailyHoroscope) {
    "${activeProfile.name} • ☉ ${activeNatalChart.sunSign.displayName} • ☽ ${activeNatalChart.moonSign.displayName} • ↑ ${activeNatalChart.risingSign.displayName}"
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .toolboxScreenBackground(MaterialTheme.colorScheme.background)
      .testTag("stars_screen"),
  ) {
    ToolboxTopBar(
      title = Tool.STARS.title,
      subtext = topBarSubtext,
      icon = Tool.STARS.icon,
      titleTestTag = "stars_topbar_title",
      subtextTestTag = "stars_topbar_subtext",
      actions = {
        IconButton(
          onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            showProfileSwitcherDialog = true
          },
          modifier = Modifier.testTag("action_switch_profile"),
        ) {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Birth Profiles",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      },
      searchState = ToolboxTopBarSearchState(
        isActive = isSearchActive,
        query = searchQuery,
        onQueryChange = { searchQuery = it },
        onOpenSearch = {
          haptics.performHapticFeedback(HapticFeedbackType.LongPress)
          isSearchActive = true
        },
        onCloseSearch = {
          isSearchActive = false
          searchQuery = ""
        },
        placeholder = "Search signs, planets, houses, aspects...",
      ),
    )

    // Primary 4-tab pill navigation bar
    ToolboxPillNavigationBar(
      barTestTag = "stars_pill_nav_bar",
    ) {
      StarsTab.entries.forEach { tab ->
        val isSelected = selectedTab == tab && !isSearchActive
        val tabIcon = when (tab) {
          StarsTab.TODAY -> Icons.Default.WbSunny
          StarsTab.CHART -> Icons.Default.Public
          StarsTab.SKY_AR -> Icons.Default.CameraAlt
          StarsTab.MATCH -> Icons.Default.Favorite
          StarsTab.GUIDE -> Icons.AutoMirrored.Filled.MenuBook
        }
        ToolboxPillNavigationItem(
          selected = isSelected,
          onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            selectedTab = tab
            if (isSearchActive) {
              isSearchActive = false
              searchQuery = ""
            }
          },
          label = tab.label,
          leadingIcon = tabIcon,
          testTag = "stars_tab_${tab.name.lowercase(Locale.US)}",
        )
      }
    }

    Box(modifier = Modifier.fillMaxSize()) {
      // Main tab content
      AnimatedContent(
        targetState = selectedTab,
        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
        label = "stars_tab_content",
      ) { tab ->
        when (tab) {
          StarsTab.TODAY -> StarsTodayTabContent(
            reading = dailyHoroscope,
            natalChart = activeNatalChart,
            dayOffset = dayOffset,
            onChangeDayOffset = { newOffset ->
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              dayOffset = newOffset
            },
            onSelectSign = { sign ->
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              overrideSignName = sign.name
              StarsWidgets.saveOverrideSign(context, sign)
              StarsWidgets.updateAllWidgets(context)
            },
            onInspectPlacement = { placement ->
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              inspectedPlacement = placement
            },
            onOpenColorPicker = onOpenColorPicker,
            onSaveToNotes = {
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              val noteBody = buildString {
                appendLine("Date: ${dailyHoroscope.dateFormatted}")
                appendLine("Sign: ${dailyHoroscope.targetSign.glyph} ${dailyHoroscope.targetSign.displayName}")
                appendLine("Mantra: \"${dailyHoroscope.headlineMantra}\"")
                appendLine("Sky: ${dailyHoroscope.keyTransitHeadline}")
                appendLine()
                appendLine("## Overview")
                appendLine(dailyHoroscope.overviewReading)
                appendLine()
                appendLine("## Love & Relationships")
                appendLine(dailyHoroscope.loveReading)
                appendLine()
                appendLine("## Career & Vibe")
                appendLine(dailyHoroscope.careerReading)
                appendLine()
                appendLine("Lucky Number: ${dailyHoroscope.luckyNumber} • Power Color: ${dailyHoroscope.powerColorName} (${dailyHoroscope.powerColorHex}) • Compatible Sign: ${dailyHoroscope.bestMatchSign.displayName}")
              }
              CrossToolActions.saveTextToNotes(
                context = context,
                title = "${dailyHoroscope.targetSign.displayName} Horoscope (${dailyHoroscope.dateFormatted})",
                content = noteBody.trim(),
                labels = listOf("stars", "horoscope"),
              )
              onOpenNotes?.invoke()
            },
          )

          StarsTab.CHART -> StarsChartTabContent(
            profiles = profiles,
            activeProfile = activeProfile,
            natalChart = activeNatalChart,
            onSelectProfile = { profile ->
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              activeProfileId = profile.id
              starsRepo.setActiveProfileId(profile.id)
              overrideSignName = null
              StarsWidgets.saveOverrideSign(context, null)
              StarsWidgets.updateAllWidgets(context)
            },
            onEditProfile = { profile ->
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              isCreatingNewProfile = false
              profileToEdit = profile
            },
            onAddProfile = {
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              isCreatingNewProfile = true
              profileToEdit = BirthProfile(
                id = "profile_${UUID.randomUUID().toString().take(8)}",
                name = "",
                relationshipTag = "Friend",
                birthYear = 1999,
                birthMonth = 6,
                birthDay = 15,
                birthHour = 12,
                birthMinute = 0,
                hasExactBirthTime = true,
                cityName = "New York, NY, USA",
                latitude = 40.7128,
                longitude = -74.0060,
                timeZoneId = "America/New_York",
              )
            },
            onInspectPlacement = { placement ->
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              inspectedPlacement = placement
            },
            onSaveChartToNotes = {
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              val body = buildString {
                appendLine("Profile: ${activeProfile.name} (${activeProfile.formattedBirthDate} • ${activeProfile.formattedBirthTime} • ${activeProfile.cityName})")
                appendLine("Big Three: ☉ ${activeNatalChart.sunSign.displayName} Sun • ☽ ${activeNatalChart.moonSign.displayName} Moon • ↑ ${activeNatalChart.risingSign.displayName} Rising")
                appendLine()
                appendLine(activeNatalChart.bigThreeSummary)
                appendLine()
                appendLine("## Planetary Placements")
                activeNatalChart.placements.forEach { p ->
                  val rx = if (p.isRetrograde) " (Rx)" else ""
                  appendLine("- ${p.body.glyph} ${p.body.displayName}: ${p.sign.glyph} ${p.sign.displayName} ${p.formattedDegree}$rx • House ${p.house}")
                }
              }
              CrossToolActions.saveTextToNotes(
                context = context,
                title = "${activeProfile.name} — Natal Birth Chart",
                content = body.trim(),
                labels = listOf("stars", "birth chart"),
              )
              onOpenNotes?.invoke()
            },
          )

          StarsTab.SKY_AR -> StarsSkyArCameraTabContent(
            activeProfile = activeProfile,
            dailyHoroscope = dailyHoroscope,
            onInspectConstellation = { constellation ->
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              StarsEngine.GUIDE_CATALOG.firstOrNull { it.id == "constellation_${constellation.id}" }?.let {
                inspectedGuideEntry = it
              }
            },
          )

          StarsTab.MATCH -> StarsMatchTabContent(
            profiles = profiles,
            firstChart = activeNatalChart,
            secondChart = partnerNatalChart,
            onSelectFirstProfile = { p ->
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              activeProfileId = p.id
              starsRepo.setActiveProfileId(p.id)
              StarsWidgets.updateAllWidgets(context)
            },
            onSelectSecondProfile = { p ->
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              partnerProfileId = p.id
              starsRepo.setMatchPartnerProfileId(p.id)
            },
            onAddProfile = {
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              isCreatingNewProfile = true
              profileToEdit = BirthProfile(
                id = "profile_${UUID.randomUUID().toString().take(8)}",
                name = "",
                relationshipTag = "Partner",
                birthYear = 1998,
                birthMonth = 10,
                birthDay = 28,
                birthHour = 12,
                birthMinute = 0,
                hasExactBirthTime = true,
                cityName = "Chicago, IL, USA",
                latitude = 41.8781,
                longitude = -87.6298,
                timeZoneId = "America/Chicago",
              )
            },
          )

          StarsTab.GUIDE -> StarsGuideTabContent(
            entries = filteredCatalogEntries,
            selectedCategory = searchCategory,
            favoriteIds = favoriteGuideIds,
            onSelectCategory = { cat ->
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              searchCategory = cat
            },
            onToggleFavorite = { entry ->
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              favoriteGuideIds = starsRepo.toggleFavoriteGuideId(entry.id)
            },
            onInspectEntry = { entry ->
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              inspectedGuideEntry = entry
            },
          )
        }
      }

      // Integrated top-expanding search catalog overlay (DESIGN_LANGUAGE.md Section 5.7)
      this@Column.AnimatedVisibility(
        visible = isSearchActive,
        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(tween(180)),
        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(tween(150)),
      ) {
        Surface(
          modifier = Modifier
            .fillMaxSize()
            .testTag("stars_search_overlay"),
          color = MaterialTheme.colorScheme.background,
        ) {
          Column(modifier = Modifier.fillMaxSize()) {
            ToolboxPillNavigationBar(
              barTestTag = "stars_search_filter_bar",
            ) {
              StarsGuideCategory.entries.forEach { category ->
                ToolboxPillNavigationItem(
                  selected = searchCategory == category,
                  onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    searchCategory = category
                  },
                  label = category.label,
                  leadingIcon = if (category == StarsGuideCategory.FAVORITES) Icons.Default.Star else null,
                  testTag = "stars_search_cat_${category.name.lowercase(Locale.US)}",
                )
              }
            }

            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              item {
                Text(
                  text = "${filteredCatalogEntries.size} astrological reference entries",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                )
              }

              if (filteredCatalogEntries.isEmpty()) {
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
                        .padding(24.dp),
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                      Text(
                        text = "No matching stars or placements",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                      )
                      Text(
                        text = "Try searching for a sign (Taurus, Scorpio), planet (Venus, Moon), house (7th House), or aspect (Trine).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                      )
                    }
                  }
                }
              } else {
                items(filteredCatalogEntries, key = { it.id }) { entry ->
                  StarsGuideEntryCard(
                    entry = entry,
                    isFavorite = entry.id in favoriteGuideIds,
                    onToggleFavorite = {
                      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                      favoriteGuideIds = starsRepo.toggleFavoriteGuideId(entry.id)
                    },
                    onClick = {
                      haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                      inspectedGuideEntry = entry
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

  // Dialogs
  inspectedGuideEntry?.let { entry ->
    StarsGuideDetailDialog(
      entry = entry,
      isFavorite = entry.id in favoriteGuideIds,
      onToggleFavorite = {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        favoriteGuideIds = starsRepo.toggleFavoriteGuideId(entry.id)
      },
      onOpenColorPicker = onOpenColorPicker,
      onDismiss = { inspectedGuideEntry = null },
    )
  }

  inspectedPlacement?.let { placement ->
    PlanetaryPlacementDetailDialog(
      placement = placement,
      onDismiss = { inspectedPlacement = null },
    )
  }

  profileToEdit?.let { editingProfile ->
    BirthProfileEditDialog(
      initialProfile = editingProfile,
      isNewProfile = isCreatingNewProfile,
      canDelete = !isCreatingNewProfile && profiles.size > 1,
      onDismiss = { profileToEdit = null },
      onDelete = {
        val updated = profiles.filterNot { it.id == editingProfile.id }.ifEmpty { DEFAULT_BIRTH_PROFILES }
        profiles = updated
        starsRepo.saveProfiles(updated)
        activeProfileId = starsRepo.getActiveProfileId()
        partnerProfileId = starsRepo.getMatchPartnerProfileId()
        profileToEdit = null
        StarsWidgets.updateAllWidgets(context)
      },
      onConfirm = { savedProfile ->
        val updated = if (profiles.any { it.id == savedProfile.id }) {
          profiles.map { if (it.id == savedProfile.id) savedProfile else it }
        } else {
          profiles + savedProfile
        }
        profiles = updated
        starsRepo.saveProfiles(updated)
        activeProfileId = savedProfile.id
        starsRepo.setActiveProfileId(savedProfile.id)
        overrideSignName = null
        profileToEdit = null
        StarsWidgets.updateAllWidgets(context)
      },
    )
  }

  if (showProfileSwitcherDialog) {
    BirthProfileSwitcherDialog(
      profiles = profiles,
      activeProfileId = activeProfile.id,
      onSelectProfile = { selected ->
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        activeProfileId = selected.id
        starsRepo.setActiveProfileId(selected.id)
        overrideSignName = null
        showProfileSwitcherDialog = false
        StarsWidgets.updateAllWidgets(context)
      },
      onEditProfile = { target ->
        showProfileSwitcherDialog = false
        isCreatingNewProfile = false
        profileToEdit = target
      },
      onAddProfile = {
        showProfileSwitcherDialog = false
        isCreatingNewProfile = true
        profileToEdit = BirthProfile(
          id = "profile_${UUID.randomUUID().toString().take(8)}",
          name = "",
          relationshipTag = "Friend",
          birthYear = 1999,
          birthMonth = 5,
          birthDay = 15,
          birthHour = 12,
          birthMinute = 0,
          hasExactBirthTime = true,
          cityName = "New York, NY, USA",
          latitude = 40.7128,
          longitude = -74.0060,
          timeZoneId = "America/New_York",
        )
      },
      onDismiss = { showProfileSwitcherDialog = false },
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StarsTodayTabContent(
  reading: DailyHoroscopeReading,
  natalChart: NatalChartCalculation,
  dayOffset: Int,
  onChangeDayOffset: (Int) -> Unit,
  onSelectSign: (ZodiacSign) -> Unit,
  onInspectPlacement: (PlanetaryPlacement) -> Unit,
  onOpenColorPicker: ((String) -> Unit)?,
  onSaveToNotes: () -> Unit,
) {
  val context = LocalContext.current
  val haptics = LocalHapticFeedback.current
  var activeSection by rememberSaveable { mutableStateOf(DailyReadingSection.OVERVIEW) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("stars_today_list"),
    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // 1. Horizontal Zodiac Sign Selector Row (defaults to active profile's Sun sign)
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        ZodiacSign.entries.forEach { sign ->
          val isSelected = sign == reading.targetSign
          val isNatalSun = sign == natalChart.sunSign
          FilterChip(
            selected = isSelected,
            onClick = { onSelectSign(sign) },
            shape = RoundedCornerShape(20.dp),
            label = {
              Text(
                text = "${sign.glyph} ${sign.displayName}" + if (isNatalSun) " ★" else "",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
          )
        }
      }
    }

    // 2. Date Scrubber (Yesterday / Today / Tomorrow)
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(
            onClick = { onChangeDayOffset(dayOffset - 1) },
            modifier = Modifier.size(36.dp),
          ) {
            Icon(
              imageVector = Icons.Default.ChevronLeft,
              contentDescription = "Previous day",
              tint = MaterialTheme.colorScheme.onSurface,
            )
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val horizonLabel = when (dayOffset) {
              -1 -> "Yesterday"
              0 -> "Today's Cosmic Weather"
              1 -> "Tomorrow"
              else -> if (dayOffset > 0) "+$dayOffset Days Ahead" else "${dayOffset} Days Ago"
            }
            Text(
              text = horizonLabel,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = reading.dateFormatted,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            if (dayOffset != 0) {
              TextButton(
                onClick = { onChangeDayOffset(0) },
                shape = CircleShape,
              ) {
                Text("Today", style = MaterialTheme.typography.labelSmall)
              }
            }
            IconButton(
              onClick = { onChangeDayOffset(dayOffset + 1) },
              modifier = Modifier.size(36.dp),
            ) {
              Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next day",
                tint = MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        }
      }
    }

    // 3. Daily Horoscope Hero Card
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("stars_daily_hero_card"),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.weight(1f),
            ) {
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = reading.targetSign.glyph,
                  fontSize = 24.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
              }
              Column {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Text(
                    text = reading.targetSign.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                  ) {
                    Text(
                      text = reading.cosmicVibeWord,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onTertiaryContainer,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                  }
                }
                Text(
                  text = "${reading.targetSign.dateRange} • ${reading.targetSign.element.label} • Ruled by ${reading.targetSign.rulingPlanetName}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
              IconButton(
                onClick = onSaveToNotes,
                modifier = Modifier.size(36.dp),
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                  contentDescription = "Save horoscope to Notes",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp),
                )
              }
              IconButton(
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                  CrossToolActions.shareText(
                    context = context,
                    title = "${reading.targetSign.displayName} Horoscope (${reading.dateFormatted})",
                    text = "${reading.targetSign.glyph} ${reading.targetSign.displayName} (${reading.dateFormatted})\n\"${reading.headlineMantra}\"\n\n${reading.overviewReading}",
                  )
                },
                modifier = Modifier.size(36.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Share,
                  contentDescription = "Share horoscope",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(20.dp),
                )
              }
            }
          }

          // Daily Mantra Callout
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Text(
                text = "“${reading.headlineMantra}”",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
              Text(
                text = reading.keyTransitHeadline,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
              )
            }
          }

          // Sub-section pills: Overview | Love | Career & Vibe | Do's & Don'ts
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            DailyReadingSection.entries.forEach { section ->
              val selected = activeSection == section
              FilterChip(
                selected = selected,
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                  activeSection = section
                },
                shape = RoundedCornerShape(20.dp),
                label = {
                  Text(
                    text = section.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
              )
            }
          }

          when (activeSection) {
            DailyReadingSection.OVERVIEW -> {
              Text(
                text = reading.overviewReading,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = reading.wellnessReading,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            DailyReadingSection.LOVE -> {
              Text(
                text = reading.loveReading,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
            DailyReadingSection.CAREER -> {
              Text(
                text = reading.careerReading,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
            DailyReadingSection.DOS_DONTS -> {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                  text = "Cosmic Do's",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                )
                reading.dosList.forEach { item ->
                  Text(
                    text = "✦ $item",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Cosmic Don'ts",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.tertiary,
                )
                reading.dontsList.forEach { item ->
                  Text(
                    text = "✧ $item",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

          // Quick Daily Stats Row (Power Color links to Colors tool!)
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            ) {
              Text(
                text = "Lucky #${reading.luckyNumber}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              )
            }

            Surface(
              shape = RoundedCornerShape(20.dp),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
              modifier = Modifier.clickable {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onSelectSign(reading.bestMatchSign)
              },
            ) {
              Text(
                text = "Match: ${reading.bestMatchSign.glyph} ${reading.bestMatchSign.displayName}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              )
            }

            val parsedPowerColor = parseHexColor(reading.powerColorHex) ?: MaterialTheme.colorScheme.primary
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
              modifier = Modifier.clickable {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                if (onOpenColorPicker != null) {
                  onOpenColorPicker(reading.powerColorHex)
                } else {
                  CrossToolActions.copyTextToClipboard(context, "Power Color", reading.powerColorHex)
                }
              },
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Box(
                  modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(parsedPowerColor),
                )
                Text(
                  text = "${reading.powerColorName} (${reading.powerColorHex})",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(20.dp),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            ) {
              Text(
                text = "Peak: ${reading.luckyTimeWindow}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              )
            }
          }
        }
      }
    }

    // 4. Live Moon Phase & Lunar Canvas Card
    item {
      MoonPhaseCard(moonSnapshot = reading.moonSnapshot)
    }

    // 5. Retrograde Watch & Current Sky Transits Card
    item {
      RetrogradeAndSkyCard(
        reading = reading,
        onInspectPlacement = onInspectPlacement,
      )
    }
  }
}

@Composable
private fun MoonPhaseCard(
  moonSnapshot: MoonPhaseSnapshot,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("stars_moon_phase_card"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Custom Lunar Phase Visual Canvas
      LunarPhaseCanvas(
        phaseAngleDegrees = moonSnapshot.phaseAngleDegrees,
        modifier = Modifier.size(76.dp),
      )

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "${moonSnapshot.phaseType.emoji} ${moonSnapshot.phaseType.displayName}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
          ) {
            Text(
              text = "${moonSnapshot.illuminationPercent}%",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
          }
        }

        Text(
          text = "Moon in ${moonSnapshot.moonSign.glyph} ${moonSnapshot.moonSign.displayName} (${moonSnapshot.moonDegreeInSign}°) • Lunar Day ${String.format(Locale.US, "%.1f", moonSnapshot.lunarAgeDays)}",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.primary,
        )

        Text(
          text = moonSnapshot.phaseType.guidance,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
          text = String.format(
            Locale.US,
            "Next Full Moon in %.1fd • Next New Moon in %.1fd",
            moonSnapshot.daysUntilFullMoon,
            moonSnapshot.daysUntilNewMoon,
          ),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
fun LunarPhaseCanvas(
  phaseAngleDegrees: Double,
  modifier: Modifier = Modifier,
) {
  val litColor = MaterialTheme.colorScheme.primary
  val shadowColor = MaterialTheme.colorScheme.surfaceContainerHighest
  val outlineColor = MaterialTheme.colorScheme.outlineVariant

  Canvas(modifier = modifier) {
    val radius = size.minDimension / 2f - 4f
    val center = Offset(size.width / 2f, size.height / 2f)

    // Draw dark lunar disc
    drawCircle(
      color = shadowColor,
      radius = radius,
      center = center,
    )

    // Draw illuminated portion using terminator scanlines
    val normPhase = ((phaseAngleDegrees % 360.0) + 360.0) % 360.0
    val isWaxing = normPhase <= 180.0
    val cosPhase = cos(normPhase * PI / 180.0).toFloat()

    val steps = 48
    for (i in -steps..steps) {
      val yFrac = i.toFloat() / steps.toFloat()
      val y = center.y + yFrac * radius
      val halfChord = kotlin.math.sqrt((1f - yFrac * yFrac).coerceAtLeast(0f)) * radius
      val terminatorX = -cosPhase * halfChord
      val startX = if (isWaxing) center.x + terminatorX else center.x - halfChord
      val endX = if (isWaxing) center.x + halfChord else center.x - terminatorX
      if (endX > startX) {
        drawLine(
          color = litColor,
          start = Offset(startX, y),
          end = Offset(endX, y),
          strokeWidth = (radius / steps) * 1.45f,
          cap = StrokeCap.Round,
        )
      }
    }

    drawCircle(
      color = outlineColor,
      radius = radius,
      center = center,
      style = Stroke(width = 2f),
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RetrogradeAndSkyCard(
  reading: DailyHoroscopeReading,
  onInspectPlacement: (PlanetaryPlacement) -> Unit,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "Retrograde Watch & Live Sky",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          val mercuryStatus = if (reading.isMercuryRetrograde) {
            "Mercury is currently IN RETROGRADE (Rx)"
          } else {
            "Mercury is Direct — clear communications"
          }
          Text(
            text = mercuryStatus,
            style = MaterialTheme.typography.bodySmall,
            color = if (reading.isMercuryRetrograde) {
              MaterialTheme.colorScheme.tertiary
            } else {
              MaterialTheme.colorScheme.onSurfaceVariant
            },
          )
        }

        Surface(
          shape = RoundedCornerShape(20.dp),
          color = if (reading.isMercuryRetrograde) {
            MaterialTheme.colorScheme.tertiaryContainer
          } else {
            MaterialTheme.colorScheme.primaryContainer
          },
        ) {
          Text(
            text = "${reading.retrogradeBodies.size} Rx",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (reading.isMercuryRetrograde) {
              MaterialTheme.colorScheme.onTertiaryContainer
            } else {
              MaterialTheme.colorScheme.onPrimaryContainer
            },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        reading.skyPlacements.forEach { placement ->
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.clickable { onInspectPlacement(placement) },
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = placement.body.glyph,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
              )
              Text(
                text = "${placement.body.displayName}: ${placement.sign.glyph} ${placement.sign.displayName} ${placement.degreeInSign}°",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
              )
              if (placement.isRetrograde) {
                Text(
                  text = "Rx",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.tertiary,
                )
              }
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StarsChartTabContent(
  profiles: List<BirthProfile>,
  activeProfile: BirthProfile,
  natalChart: NatalChartCalculation,
  onSelectProfile: (BirthProfile) -> Unit,
  onEditProfile: (BirthProfile) -> Unit,
  onAddProfile: () -> Unit,
  onInspectPlacement: (PlanetaryPlacement) -> Unit,
  onSaveChartToNotes: () -> Unit,
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("stars_chart_list"),
    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // 1. Saved Birth Profiles Strip
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        profiles.forEach { profile ->
          val isSelected = profile.id == activeProfile.id
          FilterChip(
            selected = isSelected,
            onClick = { onSelectProfile(profile) },
            shape = RoundedCornerShape(20.dp),
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
            },
            label = {
              Text(
                text = "${profile.name} (${profile.relationshipTag})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
              selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
            ),
          )
        }

        OutlinedButton(
          onClick = onAddProfile,
          shape = CircleShape,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("New Profile", style = MaterialTheme.typography.labelMedium)
        }
      }
    }

    // 2. Profile Metadata & "Big Three" Hero Card
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${activeProfile.name}'s Natal Chart",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "${activeProfile.formattedBirthDate} • ${activeProfile.formattedBirthTime} • ${activeProfile.cityName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Row {
              IconButton(
                onClick = onSaveChartToNotes,
                modifier = Modifier.size(36.dp),
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                  contentDescription = "Save chart to Notes",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp),
                )
              }
              IconButton(
                onClick = { onEditProfile(activeProfile) },
                modifier = Modifier
                  .size(36.dp)
                  .testTag("action_edit_birth_profile"),
              ) {
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "Edit birth details",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(20.dp),
                )
              }
            }
          }

          // The Big Three Badges (Sun, Moon, Rising)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            BigThreeBadgeCard(
              label = "☉ Sun Sign",
              role = "Core Identity",
              sign = natalChart.sunSign,
              modifier = Modifier.weight(1f),
            )
            BigThreeBadgeCard(
              label = "☽ Moon Sign",
              role = "Emotional Soul",
              sign = natalChart.moonSign,
              modifier = Modifier.weight(1f),
            )
            BigThreeBadgeCard(
              label = "↑ Rising",
              role = "1st House Aura",
              sign = natalChart.risingSign,
              modifier = Modifier.weight(1f),
            )
          }

          Text(
            text = natalChart.bigThreeSummary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = MaterialTheme.colorScheme.primaryContainer,
            ) {
              Text(
                text = "Dominant: ${natalChart.dominantElement.label} & ${natalChart.dominantModality.label}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              )
            }
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
              Text(
                text = "Chart Ruler: ${natalChart.chartRulerBody.glyph} ${natalChart.chartRulerBody.displayName}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              )
            }
          }
        }
      }
    }

    // 3. Interactive 2D Natal Wheel & Aspect Grid Canvas
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = "Natal Chart Wheel & Aspects",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "ASC ${natalChart.risingSign.glyph} ${natalChart.risingSign.displayName} • MC ${natalChart.midheavenSign.glyph} ${natalChart.midheavenSign.displayName} • ${natalChart.aspects.size} major aspects",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          NatalWheelCanvas(
            natalChart = natalChart,
            modifier = Modifier
              .size(270.dp)
              .padding(4.dp),
          )
        }
      }
    }

    // 4. Full Planetary Placements Table
    item {
      Text(
        text = "Planetary Placements (Tap for meaning)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 4.dp),
      )
    }

    items(natalChart.placements, key = { it.body.name }) { placement ->
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onInspectPlacement(placement) },
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = placement.body.glyph,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
            }
            Column {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                  text = "${placement.body.displayName} in ${placement.sign.glyph} ${placement.sign.displayName}",
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                if (placement.isRetrograde) {
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                  ) {
                    Text(
                      text = "Rx",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onTertiaryContainer,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                    )
                  }
                }
              }
              Text(
                text = "${placement.body.archetype} • ${ASTROLOGICAL_HOUSES[(placement.house - 1).coerceIn(0, 11)].title}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = placement.formattedDegree,
              style = MaterialTheme.typography.bodyMedium,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = "House ${ASTROLOGICAL_HOUSES[(placement.house - 1).coerceIn(0, 11)].roman}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }

    // 5. Key Natal Aspects List
    if (natalChart.aspects.isNotEmpty()) {
      item {
        Text(
          text = "Major Natal Aspects",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        )
      }

      items(natalChart.aspects.take(10), key = { "${it.firstBody.name}_${it.secondBody.name}" }) { aspect ->
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
              .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "${aspect.firstBody.glyph} ${aspect.firstBody.displayName} ${aspect.aspectType.glyph} ${aspect.aspectType.displayName} ${aspect.secondBody.glyph} ${aspect.secondBody.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = String.format(Locale.US, "Orb %.1f°", aspect.orbDegrees),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary,
              )
            }
            Text(
              text = aspect.interpretation,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun BigThreeBadgeCard(
  label: String,
  role: String,
  sign: ZodiacSign,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = "${sign.glyph} ${sign.displayName}",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = role,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
      )
    }
  }
}

@Composable
private fun NatalWheelCanvas(
  natalChart: NatalChartCalculation,
  modifier: Modifier = Modifier,
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val tertiaryColor = MaterialTheme.colorScheme.tertiary
  val onSurfaceColor = MaterialTheme.colorScheme.onSurface
  val outlineColor = MaterialTheme.colorScheme.outlineVariant
  val surfaceColor = MaterialTheme.colorScheme.surface

  Canvas(modifier = modifier) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val outerRadius = min(size.width, size.height) / 2f - 6f
    val signInnerRadius = outerRadius * 0.78f
    val houseInnerRadius = outerRadius * 0.42f
    val planetTrackRadius = (signInnerRadius + houseInnerRadius) / 2f

    // Base wheel disc
    drawCircle(
      color = surfaceColor,
      radius = outerRadius,
      center = Offset(cx, cy),
    )
    drawCircle(
      color = outlineColor,
      radius = outerRadius,
      center = Offset(cx, cy),
      style = Stroke(width = 2.5f),
    )
    drawCircle(
      color = outlineColor,
      radius = signInnerRadius,
      center = Offset(cx, cy),
      style = Stroke(width = 1.8f),
    )
    drawCircle(
      color = outlineColor,
      radius = houseInnerRadius,
      center = Offset(cx, cy),
      style = Stroke(width = 1.5f),
    )

    // Rotate wheel so Ascendant sits at left horizon (180° screen angle)
    val ascBase = natalChart.risingSign.startLongitude.toDouble()
    fun eclipticToCanvasRadians(lonDeg: Double): Double {
      val screenDeg = 180.0 - (lonDeg - ascBase)
      return screenDeg * PI / 180.0
    }

    val glyphPaint = AndroidPaint().apply {
      isAntiAlias = true
      textAlign = AndroidPaint.Align.CENTER
      textSize = outerRadius * 0.11f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      color = onSurfaceColor.toArgb()
    }
    val planetPaint = AndroidPaint().apply {
      isAntiAlias = true
      textAlign = AndroidPaint.Align.CENTER
      textSize = outerRadius * 0.105f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      color = primaryColor.toArgb()
    }

    // 12 Zodiac Sign sectors and boundary spokes
    for (sign in ZodiacSign.entries) {
      val boundaryRad = eclipticToCanvasRadians(sign.startLongitude.toDouble())
      val bx1 = cx + (houseInnerRadius * cos(boundaryRad)).toFloat()
      val by1 = cy + (houseInnerRadius * sin(boundaryRad)).toFloat()
      val bx2 = cx + (outerRadius * cos(boundaryRad)).toFloat()
      val by2 = cy + (outerRadius * sin(boundaryRad)).toFloat()
      drawLine(
        color = outlineColor,
        start = Offset(bx1, by1),
        end = Offset(bx2, by2),
        strokeWidth = 1.5f,
      )

      val midRad = eclipticToCanvasRadians(sign.startLongitude + 15.0)
      val ringMid = (outerRadius + signInnerRadius) / 2f
      val gx = cx + (ringMid * cos(midRad)).toFloat()
      val gy = cy + (ringMid * sin(midRad)).toFloat() + (glyphPaint.textSize * 0.36f)
      drawContext.canvas.nativeCanvas.drawText(sign.glyph, gx, gy, glyphPaint)
    }

    // Draw aspect chords inside the inner circle
    natalChart.aspects.take(12).forEach { aspect ->
      val p1 = natalChart.placements.firstOrNull { it.body == aspect.firstBody } ?: return@forEach
      val p2 = natalChart.placements.firstOrNull { it.body == aspect.secondBody } ?: return@forEach
      val r1 = eclipticToCanvasRadians(p1.eclipticLongitude)
      val r2 = eclipticToCanvasRadians(p2.eclipticLongitude)
      val x1 = cx + (houseInnerRadius * 0.92f * cos(r1)).toFloat()
      val y1 = cy + (houseInnerRadius * 0.92f * sin(r1)).toFloat()
      val x2 = cx + (houseInnerRadius * 0.92f * cos(r2)).toFloat()
      val y2 = cy + (houseInnerRadius * 0.92f * sin(r2)).toFloat()

      val chordColor = when (aspect.aspectType) {
        AspectType.TRINE, AspectType.SEXTILE -> primaryColor.copy(alpha = 0.65f)
        AspectType.SQUARE, AspectType.OPPOSITION -> tertiaryColor.copy(alpha = 0.65f)
        AspectType.CONJUNCTION -> onSurfaceColor.copy(alpha = 0.65f)
      }
      drawLine(
        color = chordColor,
        start = Offset(x1, y1),
        end = Offset(x2, y2),
        strokeWidth = 2f,
      )
    }

    // Plot planetary glyphs along the planet track
    natalChart.placements.forEachIndexed { idx, placement ->
      val rad = eclipticToCanvasRadians(placement.eclipticLongitude)
      val radialStagger = if (idx % 2 == 0) planetTrackRadius * 1.04f else planetTrackRadius * 0.92f
      val px = cx + (radialStagger * cos(rad)).toFloat()
      val py = cy + (radialStagger * sin(rad)).toFloat()
      drawCircle(
        color = surfaceColor,
        radius = outerRadius * 0.068f,
        center = Offset(px, py),
      )
      drawContext.canvas.nativeCanvas.drawText(
        placement.body.glyph,
        px,
        py + (planetPaint.textSize * 0.36f),
        planetPaint,
      )
    }
  }
}

@Composable
private fun StarsMatchTabContent(
  profiles: List<BirthProfile>,
  firstChart: NatalChartCalculation,
  secondChart: NatalChartCalculation,
  onSelectFirstProfile: (BirthProfile) -> Unit,
  onSelectSecondProfile: (BirthProfile) -> Unit,
  onAddProfile: () -> Unit,
) {
  val context = LocalContext.current
  val haptics = LocalHapticFeedback.current

  var compareBySignsOnly by rememberSaveable { mutableStateOf(false) }
  var customSignA by rememberSaveable { mutableStateOf(firstChart.sunSign) }
  var customSignB by rememberSaveable { mutableStateOf(secondChart.sunSign) }

  val report = remember(compareBySignsOnly, firstChart, secondChart, customSignA, customSignB) {
    if (compareBySignsOnly) {
      StarsEngine.calculateSignPairSynastry(customSignA, customSignB)
    } else {
      StarsEngine.calculateSynastry(firstChart, secondChart)
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("stars_match_list"),
    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Mode toggle: Birth Profiles vs Quick Any-Two-Signs Matcher
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        FilterChip(
          selected = !compareBySignsOnly,
          onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            compareBySignsOnly = false
          },
          shape = RoundedCornerShape(20.dp),
          label = {
            Text(
              text = "Saved Birth Charts",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (!compareBySignsOnly) FontWeight.Bold else FontWeight.Medium,
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
          ),
        )
        FilterChip(
          selected = compareBySignsOnly,
          onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            compareBySignsOnly = true
          },
          shape = RoundedCornerShape(20.dp),
          label = {
            Text(
              text = "Quick Sign Matcher",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (compareBySignsOnly) FontWeight.Bold else FontWeight.Medium,
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
          ),
        )
      }
    }

    // Selector cards
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          if (!compareBySignsOnly) {
            Text(
              text = "Person 1",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              profiles.forEach { p ->
                val selected = p.id == firstChart.profile.id
                FilterChip(
                  selected = selected,
                  onClick = { onSelectFirstProfile(p) },
                  shape = RoundedCornerShape(20.dp),
                  label = { Text(p.name) },
                )
              }
            }

            Text(
              text = "Person 2",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              profiles.forEach { p ->
                val selected = p.id == secondChart.profile.id
                FilterChip(
                  selected = selected,
                  onClick = { onSelectSecondProfile(p) },
                  shape = RoundedCornerShape(20.dp),
                  label = { Text(p.name) },
                )
              }
              OutlinedButton(
                onClick = onAddProfile,
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Partner/Friend", style = MaterialTheme.typography.labelSmall)
              }
            }
          } else {
            Text(
              text = "First Sign",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              ZodiacSign.entries.forEach { sign ->
                FilterChip(
                  selected = customSignA == sign,
                  onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    customSignA = sign
                  },
                  shape = RoundedCornerShape(20.dp),
                  label = { Text("${sign.glyph} ${sign.displayName}") },
                )
              }
            }

            Text(
              text = "Second Sign",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              ZodiacSign.entries.forEach { sign ->
                FilterChip(
                  selected = customSignB == sign,
                  onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    customSignB = sign
                  },
                  shape = RoundedCornerShape(20.dp),
                  label = { Text("${sign.glyph} ${sign.displayName}") },
                )
              }
            }
          }
        }
      }
    }

    // Synastry Hero Score Card
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("stars_synastry_hero_card"),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${report.firstLabel} (${report.firstSun.glyph}) ♥ ${report.secondLabel} (${report.secondSun.glyph})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = report.bondTitle,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
              )
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
              ) {
                Text(
                  text = "${report.overallScore}%",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
              }
              IconButton(
                onClick = {
                  haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                  CrossToolActions.shareText(
                    context = context,
                    title = "${report.firstLabel} & ${report.secondLabel} Compatibility",
                    text = "${report.firstLabel} (${report.firstSun.glyph}) & ${report.secondLabel} (${report.secondSun.glyph}) — ${report.overallScore}% (${report.bondTitle})\n\n${report.summary}",
                  )
                },
                modifier = Modifier.size(36.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Share,
                  contentDescription = "Share compatibility report",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(20.dp),
                )
              }
            }
          }

          Text(
            text = report.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

          CompatibilityDimensionRow("Romance & Chemistry (Venus–Mars)", report.romanceScore)
          CompatibilityDimensionRow("Emotional Sanctuary (Sun–Moon)", report.emotionalScore)
          CompatibilityDimensionRow("Communication & Wit (Mercury)", report.communicationScore)
          CompatibilityDimensionRow("Shared Values & Loyalty", report.valuesScore)
        }
      }
    }

    // Strengths & Growth Tips
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
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "Superpowers Together",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
          )
          report.strengths.forEach { s ->
            Text(
              text = "✦ $s",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "How to Nurture Your Bond",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary,
          )
          report.growthTips.forEach { tip ->
            Text(
              text = "✧ $tip",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }

    // Cross-Aspects between the two charts
    if (report.crossAspects.isNotEmpty()) {
      item {
        Text(
          text = "Synastry Cross-Chart Aspects",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(horizontal = 4.dp),
        )
      }

      items(
        report.crossAspects,
        key = { "syn_${it.firstBody.name}_${it.secondBody.name}_${it.aspectType.name}" },
      ) { aspect ->
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
              .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "${aspect.firstBody.glyph} ${aspect.firstBody.displayName} ${aspect.aspectType.glyph} ${aspect.aspectType.displayName} ${aspect.secondBody.glyph} ${aspect.secondBody.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = aspect.aspectType.nature,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
              )
            }
            Text(
              text = aspect.interpretation,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun CompatibilityDimensionRow(
  label: String,
  score: Int,
) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = "$score%",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
      )
    }
    LinearProgressIndicator(
      progress = { (score / 100f).coerceIn(0f, 1f) },
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp)
        .clip(CircleShape),
      color = MaterialTheme.colorScheme.primary,
      trackColor = MaterialTheme.colorScheme.surface,
    )
  }
}

@Composable
private fun StarsGuideTabContent(
  entries: List<StarsGuideEntry>,
  selectedCategory: StarsGuideCategory,
  favoriteIds: Set<String>,
  onSelectCategory: (StarsGuideCategory) -> Unit,
  onToggleFavorite: (StarsGuideEntry) -> Unit,
  onInspectEntry: (StarsGuideEntry) -> Unit,
) {
  Column(modifier = Modifier.fillMaxSize()) {
    ToolboxPillNavigationBar(
      barTestTag = "stars_guide_category_bar",
    ) {
      StarsGuideCategory.entries.forEach { category ->
        ToolboxPillNavigationItem(
          selected = selectedCategory == category,
          onClick = { onSelectCategory(category) },
          label = category.label,
          leadingIcon = if (category == StarsGuideCategory.FAVORITES) Icons.Default.Star else null,
          testTag = "stars_guide_cat_${category.name.lowercase(Locale.US)}",
        )
      }
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("stars_guide_list"),
      contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      items(entries, key = { it.id }) { entry ->
        StarsGuideEntryCard(
          entry = entry,
          isFavorite = entry.id in favoriteIds,
          onToggleFavorite = { onToggleFavorite(entry) },
          onClick = { onInspectEntry(entry) },
        )
      }
    }
  }
}

@Composable
private fun StarsGuideEntryCard(
  entry: StarsGuideEntry,
  isFavorite: Boolean,
  onToggleFavorite: () -> Unit,
  onClick: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("stars_guide_card_${entry.id}"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(
        modifier = Modifier.weight(1f),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = entry.glyph,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = entry.title,
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
              Text(
                text = entry.badgeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
              )
            }
          }
          Text(
            text = entry.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = entry.summary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      IconButton(
        onClick = onToggleFavorite,
        modifier = Modifier.size(36.dp),
      ) {
        Icon(
          imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
          contentDescription = if (isFavorite) "Remove favorite" else "Favorite",
          tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun StarsGuideDetailDialog(
  entry: StarsGuideEntry,
  isFavorite: Boolean,
  onToggleFavorite: () -> Unit,
  onOpenColorPicker: ((String) -> Unit)?,
  onDismiss: () -> Unit,
) {
  val context = LocalContext.current
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
          Text(
            text = entry.glyph,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = entry.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = entry.badgeLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        IconButton(onClick = onToggleFavorite) {
          Icon(
            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = "Toggle favorite",
            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 420.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = entry.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(12.dp),
          )
        }

        entry.details.forEach { (k, v) ->
          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
              text = k,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = v,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }

        if (entry.powerColorHex != null && onOpenColorPicker != null) {
          OutlinedButton(
            onClick = {
              onDismiss()
              onOpenColorPicker(entry.powerColorHex)
            },
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Inspect Power Color (${entry.powerColorHex}) in Colors")
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val text = buildString {
            appendLine("${entry.glyph} ${entry.title}")
            appendLine(entry.summary)
            appendLine()
            entry.details.forEach { (k, v) -> appendLine("$k: $v") }
          }
          CrossToolActions.copyTextToClipboard(context, entry.title, text.trim())
          onDismiss()
        },
        shape = CircleShape,
      ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Copy")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = CircleShape,
      ) {
        Text("Close")
      }
    },
  )
}

@Composable
private fun PlanetaryPlacementDetailDialog(
  placement: PlanetaryPlacement,
  onDismiss: () -> Unit,
) {
  val context = LocalContext.current
  val houseInfo = ASTROLOGICAL_HOUSES[(placement.house - 1).coerceIn(0, 11)]

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
          Text(
            text = placement.body.glyph,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
        Column {
          Text(
            text = "${placement.body.displayName} in ${placement.sign.displayName}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${placement.formattedPlacement} • ${houseInfo.title}",
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
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = placement.interpretation,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(12.dp),
          )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "${placement.body.glyph} ${placement.body.displayName} (${placement.body.archetype})",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = placement.body.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "${placement.sign.glyph} ${placement.sign.displayName} (${placement.sign.element.label} • ${placement.sign.modality.label})",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = placement.sign.summary,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "${houseInfo.title} (${houseInfo.theme})",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = houseInfo.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          CrossToolActions.copyTextToClipboard(
            context = context,
            label = "${placement.body.displayName} in ${placement.sign.displayName}",
            text = "${placement.body.glyph} ${placement.body.displayName} in ${placement.sign.glyph} ${placement.sign.displayName} (${placement.formattedDegree}, ${houseInfo.title})\n${placement.interpretation}",
          )
          onDismiss()
        },
        shape = CircleShape,
      ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Copy")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = CircleShape,
      ) {
        Text("Close")
      }
    },
  )
}

@Composable
private fun BirthProfileSwitcherDialog(
  profiles: List<BirthProfile>,
  activeProfileId: String,
  onSelectProfile: (BirthProfile) -> Unit,
  onEditProfile: (BirthProfile) -> Unit,
  onAddProfile: () -> Unit,
  onDismiss: () -> Unit,
) {
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
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Saved Birth Charts",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Switch active chart or add a partner/friend",
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
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        profiles.forEach { profile ->
          val chart = remember(profile) { StarsEngine.calculateNatalChart(profile) }
          val isSelected = profile.id == activeProfileId
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
              },
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectProfile(profile) },
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "${profile.name} (${profile.relationshipTag})",
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                  } else {
                    MaterialTheme.colorScheme.onSurface
                  },
                )
                Text(
                  text = "☉ ${chart.sunSign.displayName} • ☽ ${chart.moonSign.displayName} • ↑ ${chart.risingSign.displayName}",
                  style = MaterialTheme.typography.bodySmall,
                  color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  },
                )
                Text(
                  text = "${profile.formattedBirthDate} • ${profile.cityName}",
                  style = MaterialTheme.typography.labelSmall,
                  color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  },
                )
              }

              IconButton(
                onClick = { onEditProfile(profile) },
                modifier = Modifier.size(36.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Edit,
                  contentDescription = "Edit ${profile.name}",
                  tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  },
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onAddProfile,
        shape = CircleShape,
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Add Profile")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = CircleShape,
      ) {
        Text("Close")
      }
    },
  )
}

@Composable
private fun BirthProfileEditDialog(
  initialProfile: BirthProfile,
  isNewProfile: Boolean,
  canDelete: Boolean,
  onDismiss: () -> Unit,
  onDelete: () -> Unit,
  onConfirm: (BirthProfile) -> Unit,
) {
  val focusRequester = remember { FocusRequester() }
  var nameField by remember {
    mutableStateOf(
      TextFieldValue(
        text = initialProfile.name,
        selection = TextRange(0, initialProfile.name.length),
      )
    )
  }
  var relationshipTag by remember { mutableStateOf(initialProfile.relationshipTag) }
  var yearText by remember { mutableStateOf(initialProfile.birthYear.toString()) }
  var monthText by remember { mutableStateOf(initialProfile.birthMonth.toString()) }
  var dayText by remember { mutableStateOf(initialProfile.birthDay.toString()) }
  var hasExactTime by remember { mutableStateOf(initialProfile.hasExactBirthTime) }
  var hourText by remember { mutableStateOf(initialProfile.birthHour.toString()) }
  var minuteText by remember { mutableStateOf(initialProfile.birthMinute.toString()) }

  var selectedCity by remember {
    mutableStateOf(
      BUILTIN_BIRTH_CITIES.firstOrNull {
        it.displayLabel.equals(initialProfile.cityName, ignoreCase = true) ||
          initialProfile.cityName.contains(it.cityName, ignoreCase = true)
      } ?: BUILTIN_BIRTH_CITIES.first()
    )
  }
  var cityFilterQuery by remember { mutableStateOf("") }

  LaunchedEffect(Unit) {
    delay(60)
    runCatching { focusRequester.requestFocus() }
  }

  val parsedYear = yearText.trim().toIntOrNull()
  val parsedMonth = monthText.trim().toIntOrNull()
  val parsedDay = dayText.trim().toIntOrNull()
  val parsedHour = if (hasExactTime) hourText.trim().toIntOrNull() else 12
  val parsedMinute = if (hasExactTime) minuteText.trim().toIntOrNull() else 0

  val nameError = if (nameField.text.trim().isEmpty()) "Enter a profile name" else null
  val dateError = when {
    parsedYear == null || parsedYear !in 1900..2100 -> "Year must be between 1900 and 2100"
    parsedMonth == null || parsedMonth !in 1..12 -> "Month must be 1–12"
    parsedDay == null || parsedDay !in 1..31 -> "Day must be 1–31"
    else -> null
  }
  val timeError = when {
    !hasExactTime -> null
    parsedHour == null || parsedHour !in 0..23 -> "Hour must be 0–23 (24-hour)"
    parsedMinute == null || parsedMinute !in 0..59 -> "Minute must be 0–59"
    else -> null
  }

  val canSave = nameError == null && dateError == null && timeError == null

  val previewProfile = remember(
    nameField.text,
    relationshipTag,
    parsedYear,
    parsedMonth,
    parsedDay,
    hasExactTime,
    parsedHour,
    parsedMinute,
    selectedCity,
  ) {
    BirthProfile(
      id = initialProfile.id,
      name = nameField.text.trim().ifEmpty { "Preview" },
      relationshipTag = relationshipTag,
      birthYear = (parsedYear ?: 1999).coerceIn(1900, 2100),
      birthMonth = (parsedMonth ?: 5).coerceIn(1, 12),
      birthDay = (parsedDay ?: 15).coerceIn(1, 31),
      birthHour = (parsedHour ?: 12).coerceIn(0, 23),
      birthMinute = (parsedMinute ?: 0).coerceIn(0, 59),
      hasExactBirthTime = hasExactTime,
      cityName = selectedCity.displayLabel,
      latitude = selectedCity.latitude,
      longitude = selectedCity.longitude,
      timeZoneId = selectedCity.timeZoneId,
    )
  }
  val previewChart = remember(previewProfile) {
    StarsEngine.calculateNatalChart(previewProfile)
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
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (isNewProfile) "Add Birth Profile" else "Edit Birth Profile",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Enter birth date, time & city for Big Three calculation",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        if (canDelete) {
          IconButton(onClick = onDelete) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete profile",
              tint = MaterialTheme.colorScheme.error,
            )
          }
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 440.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Live Context Preview Card (DESIGN_LANGUAGE.md Section 5.2)
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = "Live Big Three Preview",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "☉ ${previewChart.sunSign.glyph} ${previewChart.sunSign.displayName} Sun • ☽ ${previewChart.moonSign.glyph} ${previewChart.moonSign.displayName} Moon • ↑ ${previewChart.risingSign.glyph} ${previewChart.risingSign.displayName} Rising",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }

        // Name Field
        OutlinedTextField(
          value = nameField,
          onValueChange = { nameField = it },
          label = { Text("Name") },
          singleLine = true,
          isError = nameError != null,
          supportingText = nameError?.let { err -> { Text(err, color = MaterialTheme.colorScheme.error) } },
          shape = RoundedCornerShape(14.dp),
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
          trailingIcon = {
            if (nameField.text.isNotEmpty()) {
              IconButton(onClick = { nameField = TextFieldValue("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear")
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        )

        // Relationship Tag Chips
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          listOf("Me", "Partner", "Crush", "Bestie", "Friend", "Family").forEach { tag ->
            FilterChip(
              selected = relationshipTag.equals(tag, ignoreCase = true),
              onClick = { relationshipTag = tag },
              shape = RoundedCornerShape(20.dp),
              label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
            )
          }
        }

        // Birth Date Row (Year / Month / Day)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedTextField(
            value = yearText,
            onValueChange = { yearText = it.filter { ch -> ch.isDigit() }.take(4) },
            label = { Text("Year") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1.3f),
          )
          OutlinedTextField(
            value = monthText,
            onValueChange = { monthText = it.filter { ch -> ch.isDigit() }.take(2) },
            label = { Text("Month") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
          )
          OutlinedTextField(
            value = dayText,
            onValueChange = { dayText = it.filter { ch -> ch.isDigit() }.take(2) },
            label = { Text("Day") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
          )
        }
        if (dateError != null) {
          Text(
            text = dateError,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
          )
        }

        // Exact Birth Time Toggle + Hour/Min
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .clickable { hasExactTime = !hasExactTime },
        ) {
          Checkbox(
            checked = hasExactTime,
            onCheckedChange = { hasExactTime = it },
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "I know the exact birth time (for Rising sign & Houses)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }

        if (hasExactTime) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            OutlinedTextField(
              value = hourText,
              onValueChange = { hourText = it.filter { ch -> ch.isDigit() }.take(2) },
              label = { Text("Hour (0–23)") },
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
              value = minuteText,
              onValueChange = { minuteText = it.filter { ch -> ch.isDigit() }.take(2) },
              label = { Text("Minute (0–59)") },
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.weight(1f),
            )
          }
          if (timeError != null) {
            Text(
              text = timeError,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error,
            )
          }
        }

        // Birth City Selector
        OutlinedTextField(
          value = cityFilterQuery,
          onValueChange = { cityFilterQuery = it },
          label = { Text("Birth City (${selectedCity.displayLabel})") },
          placeholder = { Text("Filter 34 global cities...") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
          trailingIcon = {
            if (cityFilterQuery.isNotEmpty()) {
              IconButton(onClick = { cityFilterQuery = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear")
              }
            }
          },
          modifier = Modifier.fillMaxWidth(),
        )

        val matchingCities = remember(cityFilterQuery) {
          val q = cityFilterQuery.trim().lowercase(Locale.US)
          if (q.isEmpty()) BUILTIN_BIRTH_CITIES else {
            BUILTIN_BIRTH_CITIES.filter {
              it.displayLabel.lowercase(Locale.US).contains(q) ||
                it.timeZoneId.lowercase(Locale.US).contains(q)
            }
          }
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          matchingCities.forEach { city ->
            FilterChip(
              selected = city.id == selectedCity.id,
              onClick = { selectedCity = city },
              shape = RoundedCornerShape(20.dp),
              label = { Text(city.displayLabel, style = MaterialTheme.typography.labelSmall) },
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(previewProfile) },
        enabled = canSave,
        shape = CircleShape,
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Save Chart")
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
 * Live Sky AR Constellation Finder & Night Sky Camera Viewfinder (`StarsTab.SKY_AR`).
 *
 * Combines:
 * 1. Live back-camera preview (`TextureView` via Camera2 API) when Camera permission is granted.
 * 2. Real-time 3D device orientation sensors (`TYPE_ROTATION_VECTOR` or `TYPE_ACCELEROMETER` +
 *    `TYPE_MAGNETIC_FIELD`) + manual drag/pan override to aim anywhere across the 360° celestial dome.
 * 3. Astronomical RA/Dec -> Altitude/Azimuth projection for all 16 constellations (12 Zodiac signs +
 *    Orion & Orion's Belt, Big Dipper, Cassiopeia, Cygnus) and live Sun/Moon/Planets.
 * 4. Computer Vision **Star-Snap** (`StarsEngine.detectBrightStarPointsFromBitmap` +
 *    `StarsEngine.matchConstellationToDetectedStars`) that detects bright stars in the camera feed
 *    (such as Orion's Belt: Alnitak, Alnilam, Mintaka) and locks the glowing constellation lines
 *    directly onto the real stars on screen.
 * 5. One-tap **AR Sky Snapshot** that composites the live camera image and the glowing constellation
 *    overlay into a high-resolution PNG/JPEG in `DCIM/Camera`.
 */
@SuppressLint("MissingPermission")
@Composable
private fun StarsSkyArCameraTabContent(
  activeProfile: BirthProfile,
  dailyHoroscope: DailyHoroscopeReading,
  onInspectConstellation: (SkyConstellation) -> Unit,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptics = LocalHapticFeedback.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val coroutineScope = rememberCoroutineScope()

  val appCameraEnabled = NavigationPreferencesBridge.isPermissionGranted(AppPermission.CAMERA)
  var hasSystemCameraPermission by remember {
    mutableStateOf(
      isInspection ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    )
  }
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { granted ->
    hasSystemCameraPermission = granted
    if (granted) {
      NavigationPreferencesBridge.activeRepository?.recordPermissionUsage(
        permission = AppPermission.CAMERA,
        tool = Tool.STARS,
      )
    }
  }

  val isCameraActive = appCameraEnabled && hasSystemCameraPermission

  // Live sensor orientation state (Azimuth 0..360, Altitude -90..+90, Roll)
  var sensorAzimuthDeg by remember { mutableFloatStateOf(165f) }
  var sensorAltitudeDeg by remember { mutableFloatStateOf(32f) }
  var sensorRollDeg by remember { mutableFloatStateOf(0f) }
  var hasLiveSensorFix by remember { mutableStateOf(false) }

  // Manual touch-drag pan or "Lock to Constellation" override
  var isManualPanMode by rememberSaveable { mutableStateOf(false) }
  var manualAzimuthDeg by rememberSaveable { mutableFloatStateOf(165f) }
  var manualAltitudeDeg by rememberSaveable { mutableFloatStateOf(32f) }

  // Pinned / highlighted target constellation (defaults to Orion so Orion's Belt is ready immediately)
  var selectedConstellationId by rememberSaveable { mutableStateOf("orion") }
  var starSnapVisionEnabled by rememberSaveable { mutableStateOf(true) }
  var showAllConstellations by rememberSaveable { mutableStateOf(true) }
  var showPlanetMarkers by rememberSaveable { mutableStateOf(true) }

  // Detected bright stars from live camera frame & Star-Snap match state
  var detectedCameraStars by remember { mutableStateOf<List<DetectedCameraStarPoint>>(emptyList()) }
  var activeVisionSnapMatch by remember { mutableStateOf<StarVisionSnapMatch?>(null) }
  var activeTextureView by remember { mutableStateOf<TextureView?>(null) }
  var isLifecycleResumed by remember { mutableStateOf(true) }
  var surfaceReadyTrigger by remember { mutableIntStateOf(0) }

  val nowJd = remember(activeProfile) {
    val cal = Calendar.getInstance()
    StarsEngine.toJulianDay(
      year = cal.get(Calendar.YEAR),
      month = cal.get(Calendar.MONTH) + 1,
      day = cal.get(Calendar.DAY_OF_MONTH),
      hour = cal.get(Calendar.HOUR_OF_DAY),
      minute = cal.get(Calendar.MINUTE),
      timeZoneId = cal.timeZone.id,
    )
  }

  // Compute current horizontal (Alt/Az) positions for all 16 constellations
  val constellationHorizontalMap = remember(nowJd, activeProfile.latitude, activeProfile.longitude) {
    SKY_CONSTELLATIONS.associate { c ->
      c.id to c.stars.associate { star ->
        star.id to StarsEngine.equatorialToHorizontal(
          raHours = star.raHours,
          decDegrees = star.decDegrees,
          jd = nowJd,
          latitude = activeProfile.latitude,
          longitude = activeProfile.longitude,
        )
      }
    }
  }

  val constellationCenterHorizontalMap = remember(constellationHorizontalMap) {
    SKY_CONSTELLATIONS.associate { c ->
      val starPositions = constellationHorizontalMap[c.id]?.values.orEmpty()
      if (starPositions.isEmpty()) {
        c.id to SkyHorizontalPosition(30.0, 180.0)
      } else {
        val avgAlt = starPositions.map { it.altitudeDegrees }.average()
        val sinAz = starPositions.sumOf { sin(it.azimuthDegrees * PI / 180.0) }
        val cosAz = starPositions.sumOf { cos(it.azimuthDegrees * PI / 180.0) }
        val avgAz = StarsEngine.normalizeDegrees(kotlin.math.atan2(sinAz, cosAz) * 180.0 / PI)
        c.id to SkyHorizontalPosition(avgAlt, avgAz)
      }
    }
  }

  // Initialize camera aim toward Orion on first launch so the user sees Orion's Belt right away if sensors aren't moving
  LaunchedEffect(Unit) {
    constellationCenterHorizontalMap["orion"]?.let { orionPos ->
      manualAzimuthDeg = orionPos.azimuthDegrees.toFloat()
      manualAltitudeDeg = orionPos.altitudeDegrees.toFloat()
      if (!hasLiveSensorFix) {
        sensorAzimuthDeg = orionPos.azimuthDegrees.toFloat()
        sensorAltitudeDeg = orionPos.altitudeDegrees.toFloat()
      }
    }
  }

  // Register device Rotation Vector / Compass + Accelerometer sensors for 3D Sky AR pointing
  DisposableEffect(context, isInspection) {
    if (isInspection) {
      return@DisposableEffect onDispose {}
    }
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
      ?: return@DisposableEffect onDispose {}

    val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    val rotationMatrix = FloatArray(9)
    val remappedMatrix = FloatArray(9)
    val orientationAngles = FloatArray(3)
    val accelValues = FloatArray(3)
    val magValues = FloatArray(3)
    var hasAccel = false
    var hasMag = false

    fun updateFromRotationMatrix() {
      // Remap coordinate system for a phone held upright pointing the rear camera forward/up at the sky:
      // Device X -> World X (East), Device Z -> World Y (North/Camera axis)
      SensorManager.remapCoordinateSystem(
        rotationMatrix,
        SensorManager.AXIS_X,
        SensorManager.AXIS_Z,
        remappedMatrix,
      )
      SensorManager.getOrientation(remappedMatrix, orientationAngles)
      val rawAz = ((Math.toDegrees(orientationAngles[0].toDouble()) + 360.0) % 360.0).toFloat()
      val rawPitch = (-Math.toDegrees(orientationAngles[1].toDouble())).coerceIn(-89.0, 89.0).toFloat()
      val rawRoll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

      // Smooth EMA low-pass filter to eliminate compass jitter
      val alpha = 0.16f
      var azDiff = rawAz - sensorAzimuthDeg
      if (azDiff > 180f) azDiff -= 360f
      if (azDiff < -180f) azDiff += 360f
      sensorAzimuthDeg = ((sensorAzimuthDeg + alpha * azDiff) + 360f) % 360f
      sensorAltitudeDeg = (sensorAltitudeDeg + alpha * (rawPitch - sensorAltitudeDeg)).coerceIn(-88f, 88f)
      sensorRollDeg = (sensorRollDeg + alpha * (rawRoll - sensorRollDeg)) * 0.25f
      hasLiveSensorFix = true
    }

    val listener = object : SensorEventListener {
      override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
          Sensor.TYPE_ROTATION_VECTOR -> {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            updateFromRotationMatrix()
          }
          Sensor.TYPE_ACCELEROMETER -> {
            System.arraycopy(event.values, 0, accelValues, 0, minOf(3, event.values.size))
            hasAccel = true
            if (rotationSensor == null && hasMag) {
              if (SensorManager.getRotationMatrix(rotationMatrix, null, accelValues, magValues)) {
                updateFromRotationMatrix()
              }
            }
          }
          Sensor.TYPE_MAGNETIC_FIELD -> {
            System.arraycopy(event.values, 0, magValues, 0, minOf(3, event.values.size))
            hasMag = true
            if (rotationSensor == null && hasAccel) {
              if (SensorManager.getRotationMatrix(rotationMatrix, null, accelValues, magValues)) {
                updateFromRotationMatrix()
              }
            }
          }
        }
      }

      override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    try {
      if (rotationSensor != null) {
        sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
      } else {
        if (accelSensor != null) {
          sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_GAME)
        }
        if (magSensor != null) {
          sensorManager.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_GAME)
        }
      }
    } catch (_: Throwable) {}

    onDispose {
      try {
        sensorManager.unregisterListener(listener)
      } catch (_: Throwable) {}
    }
  }

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> isLifecycleResumed = true
        Lifecycle.Event.ON_PAUSE -> isLifecycleResumed = false
        else -> {}
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  // Open rear Camera2 preview stream onto activeTextureView when Camera permission is granted
  DisposableEffect(isCameraActive, isLifecycleResumed, surfaceReadyTrigger) {
    val tv = activeTextureView
    if (isInspection || !isCameraActive || !isLifecycleResumed || tv == null || !tv.isAvailable) {
      return@DisposableEffect onDispose {}
    }

    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
      ?: return@DisposableEffect onDispose {}
    val backCameraId = try {
      cameraManager.cameraIdList.firstOrNull { id ->
        cameraManager.getCameraCharacteristics(id)
          .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
      } ?: cameraManager.cameraIdList.firstOrNull()
    } catch (_: Throwable) {
      null
    } ?: return@DisposableEffect onDispose {}

    val mainHandler = Handler(Looper.getMainLooper())
    var isDisposed = false
    var openedDevice: CameraDevice? = null
    var openedSession: CameraCaptureSession? = null
    var previewSurface: Surface? = null

    try {
      NavigationPreferencesBridge.activeRepository?.recordPermissionUsage(
        permission = AppPermission.CAMERA,
        tool = Tool.STARS,
      )
      cameraManager.openCamera(
        backCameraId,
        object : CameraDevice.StateCallback() {
          override fun onOpened(device: CameraDevice) {
            if (isDisposed) {
              runCatching { device.close() }
              return
            }
            openedDevice = device
            try {
              val st = tv.surfaceTexture ?: return
              st.setDefaultBufferSize(1280, 960)
              val surface = Surface(st)
              previewSurface = surface
              val builder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                addTarget(surface)
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
              }
              @Suppress("DEPRECATION")
              device.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                  override fun onConfigured(session: CameraCaptureSession) {
                    if (isDisposed) {
                      runCatching { session.close() }
                      return
                    }
                    openedSession = session
                    runCatching {
                      session.setRepeatingRequest(builder.build(), null, mainHandler)
                    }
                  }

                  override fun onConfigureFailed(session: CameraCaptureSession) {}
                },
                mainHandler,
              )
            } catch (_: Throwable) {}
          }

          override fun onDisconnected(device: CameraDevice) {
            runCatching { device.close() }
            openedDevice = null
          }

          override fun onError(device: CameraDevice, error: Int) {
            runCatching { device.close() }
            openedDevice = null
          }
        },
        mainHandler,
      )
    } catch (_: Throwable) {}

    onDispose {
      isDisposed = true
      runCatching { openedSession?.close() }
      runCatching { openedDevice?.close() }
      runCatching { previewSurface?.release() }
    }
  }

  val activeCameraAzimuth = if (isManualPanMode || !hasLiveSensorFix) manualAzimuthDeg.toDouble() else sensorAzimuthDeg.toDouble()
  val activeCameraAltitude = if (isManualPanMode || !hasLiveSensorFix) manualAltitudeDeg.toDouble() else sensorAltitudeDeg.toDouble()
  val activeCameraRoll = if (isManualPanMode) 0.0 else sensorRollDeg.toDouble()

  // Project all constellations into the current camera viewport
  val projectedConstellations = remember(
    constellationHorizontalMap,
    activeCameraAzimuth,
    activeCameraAltitude,
    activeCameraRoll,
  ) {
    SKY_CONSTELLATIONS.associate { c ->
      val starHoriz = constellationHorizontalMap[c.id].orEmpty()
      c.id to c.stars.associate { star ->
        val horiz = starHoriz[star.id] ?: SkyHorizontalPosition(30.0, 180.0)
        star.id to StarsEngine.projectSkyToCameraViewport(
          targetAltitudeDeg = horiz.altitudeDegrees,
          targetAzimuthDeg = horiz.azimuthDegrees,
          cameraAltitudeDeg = activeCameraAltitude,
          cameraAzimuthDeg = activeCameraAzimuth,
          cameraRollDeg = activeCameraRoll,
        )
      }
    }
  }

  // Determine which constellation is closest to the center of the camera reticle
  val aimedConstellation = remember(
    constellationCenterHorizontalMap,
    activeCameraAzimuth,
    activeCameraAltitude,
    selectedConstellationId,
  ) {
    SKY_CONSTELLATIONS.minByOrNull { c ->
      val center = constellationCenterHorizontalMap[c.id] ?: return@minByOrNull 999.0
      val proj = StarsEngine.projectSkyToCameraViewport(
        targetAltitudeDeg = center.altitudeDegrees,
        targetAzimuthDeg = center.azimuthDegrees,
        cameraAltitudeDeg = activeCameraAltitude,
        cameraAzimuthDeg = activeCameraAzimuth,
      )
      proj.angularDistanceFromCenterDeg
    } ?: SKY_CONSTELLATIONS.first()
  }

  // Live Computer Vision Star-Spotter loop: grabs a lightweight frame from TextureView and matches
  // bright star points against Orion's Belt or the aimed constellation!
  LaunchedEffect(
    isCameraActive,
    starSnapVisionEnabled,
    activeTextureView,
    aimedConstellation.id,
    projectedConstellations,
  ) {
    if (isInspection || !isCameraActive || !starSnapVisionEnabled) {
      detectedCameraStars = emptyList()
      activeVisionSnapMatch = null
      return@LaunchedEffect
    }
    while (isActive && isCameraActive && starSnapVisionEnabled) {
      val tv = activeTextureView
      val frame = if (tv != null && tv.isAvailable && tv.width > 32 && tv.height > 32) {
        runCatching { tv.getBitmap(320, 240) }.getOrNull()
      } else {
        null
      }
      if (frame != null) {
        val aimedProjCoords = projectedConstellations[aimedConstellation.id]
          ?.filterValues { it.isInFrontOfCamera }
          ?.mapValues { (_, pt) -> pt.normX to pt.normY }
          .orEmpty()

        val (points, match) = withContext(Dispatchers.Default) {
          val pts = StarsEngine.detectBrightStarPointsFromBitmap(frame, maxStars = 20)
          val snapped = StarsEngine.matchConstellationToDetectedStars(
            detectedPoints = pts,
            aimedConstellation = aimedConstellation,
            projectedStarCoords = aimedProjCoords,
          )
          pts to snapped
        }
        detectedCameraStars = points
        activeVisionSnapMatch = match
      }
      delay(400L)
    }
  }

  // Project live Sun, Moon, and Planets onto the sky dome
  val projectedPlanets = remember(
    dailyHoroscope.skyPlacements,
    nowJd,
    activeProfile.latitude,
    activeProfile.longitude,
    activeCameraAzimuth,
    activeCameraAltitude,
    activeCameraRoll,
  ) {
    dailyHoroscope.skyPlacements.map { placement ->
      val (raH, decD) = StarsEngine.eclipticToEquatorial(placement.eclipticLongitude, nowJd)
      val horiz = StarsEngine.equatorialToHorizontal(
        raHours = raH,
        decDegrees = decD,
        jd = nowJd,
        latitude = activeProfile.latitude,
        longitude = activeProfile.longitude,
      )
      val proj = StarsEngine.projectSkyToCameraViewport(
        targetAltitudeDeg = horiz.altitudeDegrees,
        targetAzimuthDeg = horiz.azimuthDegrees,
        cameraAltitudeDeg = activeCameraAltitude,
        cameraAzimuthDeg = activeCameraAzimuth,
        cameraRollDeg = activeCameraRoll,
      )
      placement to proj
    }
  }

  val primaryColor = MaterialTheme.colorScheme.primary
  val tertiaryColor = MaterialTheme.colorScheme.tertiary

  fun captureArSkyPhoto() {
    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    coroutineScope.launch {
      val savedFile = withContext(Dispatchers.IO) {
        try {
          val outW = 1080
          val outH = 1440
          val composite = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
          val canvas = AndroidCanvas(composite)

          // 1. Draw live camera frame or deep-space starfield backdrop
          val tv = activeTextureView
          val cameraBmp = if (tv != null && tv.isAvailable) {
            runCatching { tv.getBitmap(outW, outH) }.getOrNull()
          } else {
            null
          }
          if (cameraBmp != null) {
            canvas.drawBitmap(cameraBmp, 0f, 0f, null)
          } else {
            canvas.drawColor(android.graphics.Color.parseColor("#0B0E1A"))
          }

          // 2. Bake glowing constellation stick lines & star names onto the photo
          val linePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = primaryColor.toArgb()
            strokeWidth = 5f
            style = AndroidPaint.Style.STROKE
            strokeCap = AndroidPaint.Cap.ROUND
          }
          val starPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = AndroidPaint.Style.FILL
          }
          val textPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          }

          SKY_CONSTELLATIONS.forEach { c ->
            if (!showAllConstellations && c.id != aimedConstellation.id && c.id != selectedConstellationId) {
              return@forEach
            }
            val starMap = projectedConstellations[c.id].orEmpty()
            val snapMap = if (activeVisionSnapMatch?.constellationId == c.id) {
              activeVisionSnapMatch?.snappedStarPositions.orEmpty()
            } else {
              emptyMap()
            }

            fun resolveStarPixel(starId: String): Pair<Float, Float>? {
              val snapped = snapMap[starId]
              if (snapped != null) {
                return (snapped.first * outW) to (snapped.second * outH)
              }
              val p = starMap[starId] ?: return null
              if (!p.isInFrontOfCamera) return null
              return (p.normX * outW) to (p.normY * outH)
            }

            c.lines.forEach { (idA, idB) ->
              val ptA = resolveStarPixel(idA) ?: return@forEach
              val ptB = resolveStarPixel(idB) ?: return@forEach
              canvas.drawLine(ptA.first, ptA.second, ptB.first, ptB.second, linePaint)
            }

            c.stars.forEach { star ->
              val pt = resolveStarPixel(star.id) ?: return@forEach
              val r = (10f - star.magnitude * 1.6f).coerceIn(4f, 11f)
              canvas.drawCircle(pt.first, pt.second, r, starPaint)
              canvas.drawText(star.name, pt.first + 12f, pt.second - 8f, textPaint)
            }
          }

          // Footer watermark banner
          val titlePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = primaryColor.toArgb()
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          }
          canvas.drawText(
            "Stars Sky AR • ${aimedConstellation.glyph} ${aimedConstellation.name} (${aimedConstellation.subtitle})",
            36f,
            outH - 48f,
            titlePaint,
          )

          val dcimDir = CameraEngine.detectDefaultCameraDirectory(
            context = context,
            userRoot = FileManagerEngine.getUserRootDirectory(),
            fallbackBaseDir = context.getExternalFilesDir(null) ?: context.filesDir,
          )
          if (!dcimDir.exists()) dcimDir.mkdirs()
          val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
          val outFile = File(dcimDir, "STARS_AR_$stamp.jpg")
          FileOutputStream(outFile).use { fos ->
            composite.compress(Bitmap.CompressFormat.JPEG, 94, fos)
          }
          outFile
        } catch (_: Throwable) {
          null
        }
      }
      if (savedFile != null) {
        Toast.makeText(context, "Saved AR Sky Snapshot: ${savedFile.name}", Toast.LENGTH_SHORT).show()
      } else {
        Toast.makeText(context, "Could not save AR snapshot", Toast.LENGTH_SHORT).show()
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp, vertical = 8.dp)
      .testTag("stars_sky_ar_tab"),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    // 1. Quick Target Constellation Jump Strip (Orion's Belt first, followed by Zodiac & Northern Landmarks)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      SKY_CONSTELLATIONS.forEach { constellation ->
        val isAimedOrSelected = constellation.id == aimedConstellation.id
        FilterChip(
          selected = isAimedOrSelected,
          onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            selectedConstellationId = constellation.id
            constellationCenterHorizontalMap[constellation.id]?.let { pos ->
              isManualPanMode = true
              manualAzimuthDeg = pos.azimuthDegrees.toFloat()
              manualAltitudeDeg = pos.altitudeDegrees.toFloat()
            }
          },
          shape = RoundedCornerShape(20.dp),
          label = {
            Text(
              text = "${constellation.glyph} ${constellation.name}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (isAimedOrSelected) FontWeight.Bold else FontWeight.Medium,
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
          ),
        )
      }
    }

    // 2. Main AR Camera Viewfinder Card with Live Constellation Stick-Lines & Star-Spotter Overlay
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(
        containerColor = Color(0xFF0A0D18),
      ),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .testTag("stars_sky_ar_viewfinder"),
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
              change.consume()
              if (!isManualPanMode) {
                isManualPanMode = true
                manualAzimuthDeg = activeCameraAzimuth.toFloat()
                manualAltitudeDeg = activeCameraAltitude.toFloat()
              }
              val degPerPx = 0.14f
              manualAzimuthDeg = ((manualAzimuthDeg - dragAmount.x * degPerPx) % 360f + 360f) % 360f
              manualAltitudeDeg = (manualAltitudeDeg + dragAmount.y * degPerPx).coerceIn(-85f, 85f)
            }
          },
      ) {
        // Live hardware back-camera TextureView preview
        if (isCameraActive && !isInspection) {
          AndroidView(
            factory = { ctx ->
              TextureView(ctx).apply {
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                  override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                    activeTextureView = this@apply
                    surfaceReadyTrigger++
                  }

                  override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

                  override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                    if (activeTextureView === this@apply) {
                      activeTextureView = null
                    }
                    return true
                  }

                  override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                }
              }
            },
            modifier = Modifier.fillMaxSize(),
          )
        }

        // Translucent night-sky contrast veil + AR Constellation & Planet Canvas
        Canvas(
          modifier = Modifier
            .fillMaxSize()
            .testTag("stars_sky_ar_canvas"),
        ) {
          val w = size.width
          val h = size.height

          // Subtle dark sky vignette so glowing constellation lines remain crisp even indoors
          drawRect(
            color = Color(0xFF080B14).copy(alpha = if (isCameraActive) 0.32f else 0.94f),
            size = size,
          )

          // Draw subtle background starfield when camera is off or in simulated mode
          if (!isCameraActive || isInspection) {
            for (s in 0 until 45) {
              val sx = ((s * 73 + (activeCameraAzimuth.roundToInt() * 3)) % 100) / 100f * w
              val sy = ((s * 41 + (activeCameraAltitude.roundToInt() * 5) + 100) % 100) / 100f * h
              drawCircle(
                color = Color.White.copy(alpha = 0.25f + (s % 4) * 0.12f),
                radius = 1.5f + (s % 3),
                center = Offset(sx, sy),
              )
            }
          }

          // Draw Horizon line & Cardinal Compass markers (N, NE, E, SE, S, SW, W, NW)
          val cardinals = listOf(
            0.0 to "N",
            45.0 to "NE",
            90.0 to "E",
            135.0 to "SE",
            180.0 to "S",
            225.0 to "SW",
            270.0 to "W",
            315.0 to "NW",
          )
          val cardinalPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = tertiaryColor.toArgb()
            textSize = 13.dp.toPx()
            textAlign = AndroidPaint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          }
          var prevHorizonPt: Offset? = null
          for (azStep in 0..360 step 15) {
            val hProj = StarsEngine.projectSkyToCameraViewport(
              targetAltitudeDeg = 0.0,
              targetAzimuthDeg = azStep.toDouble(),
              cameraAltitudeDeg = activeCameraAltitude,
              cameraAzimuthDeg = activeCameraAzimuth,
              cameraRollDeg = activeCameraRoll,
            )
            if (hProj.isInFrontOfCamera && hProj.normX in -0.3f..1.3f && hProj.normY in -0.3f..1.3f) {
              val curPt = Offset(hProj.normX * w, hProj.normY * h)
              prevHorizonPt?.let { prev ->
                if (hypot((curPt.x - prev.x).toDouble(), (curPt.y - prev.y).toDouble()) < w * 0.45f) {
                  drawLine(
                    color = tertiaryColor.copy(alpha = 0.35f),
                    start = prev,
                    end = curPt,
                    strokeWidth = 1.5.dp.toPx(),
                  )
                }
              }
              prevHorizonPt = curPt
            } else {
              prevHorizonPt = null
            }
          }
          cardinals.forEach { (az, label) ->
            val cProj = StarsEngine.projectSkyToCameraViewport(
              targetAltitudeDeg = 0.0,
              targetAzimuthDeg = az,
              cameraAltitudeDeg = activeCameraAltitude,
              cameraAzimuthDeg = activeCameraAzimuth,
              cameraRollDeg = activeCameraRoll,
            )
            if (cProj.isInFrontOfCamera && cProj.normX in 0.02f..0.98f && cProj.normY in 0.02f..0.98f) {
              drawContext.canvas.nativeCanvas.drawText(
                label,
                cProj.normX * w,
                cProj.normY * h - 6.dp.toPx(),
                cardinalPaint,
              )
            }
          }

          // Highlight detected camera bright star points from live computer vision
          if (starSnapVisionEnabled) {
            detectedCameraStars.forEach { pt ->
              drawCircle(
                color = tertiaryColor.copy(alpha = 0.55f),
                radius = 10.dp.toPx(),
                center = Offset(pt.normX * w, pt.normY * h),
                style = Stroke(width = 1.5.dp.toPx()),
              )
            }
          }

          val starLabelPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 11.dp.toPx()
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          }
          val constTitlePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            color = primaryColor.toArgb()
            textSize = 14.dp.toPx()
            textAlign = AndroidPaint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          }

          // Draw each Constellation's glowing stick lines, star nodes, and labels
          SKY_CONSTELLATIONS.forEach { constellation ->
            val isAimed = constellation.id == aimedConstellation.id
            if (!showAllConstellations && !isAimed && constellation.id != selectedConstellationId) {
              return@forEach
            }

            val starProjections = projectedConstellations[constellation.id].orEmpty()
            val snappedCoords = if (activeVisionSnapMatch?.constellationId == constellation.id) {
              activeVisionSnapMatch?.snappedStarPositions.orEmpty()
            } else {
              emptyMap()
            }

            fun resolveScreenOffset(starId: String): Offset? {
              val snapped = snappedCoords[starId]
              if (snapped != null) {
                return Offset(snapped.first * w, snapped.second * h)
              }
              val proj = starProjections[starId] ?: return null
              if (!proj.isInFrontOfCamera) return null
              if (proj.normX !in -0.25f..1.25f || proj.normY !in -0.25f..1.25f) return null
              return Offset(proj.normX * w, proj.normY * h)
            }

            val lineAlpha = if (isAimed) 0.95f else 0.52f
            val lineColor = if (snappedCoords.isNotEmpty()) tertiaryColor else primaryColor

            // Outer glow + inner crisp stick line for each constellation segment
            constellation.lines.forEach { (startId, endId) ->
              val pA = resolveScreenOffset(startId) ?: return@forEach
              val pB = resolveScreenOffset(endId) ?: return@forEach
              val isOrionBeltSegment = constellation.id == "orion" &&
                ((startId == "alnitak" && endId == "alnilam") || (startId == "alnilam" && endId == "mintaka"))

              drawLine(
                color = (if (isOrionBeltSegment) tertiaryColor else lineColor).copy(alpha = lineAlpha * 0.35f),
                start = pA,
                end = pB,
                strokeWidth = if (isOrionBeltSegment || isAimed) 7.dp.toPx() else 4.dp.toPx(),
                cap = StrokeCap.Round,
              )
              drawLine(
                color = (if (isOrionBeltSegment) tertiaryColor else lineColor).copy(alpha = lineAlpha),
                start = pA,
                end = pB,
                strokeWidth = if (isOrionBeltSegment || isAimed) 2.6.dp.toPx() else 1.6.dp.toPx(),
                cap = StrokeCap.Round,
              )
            }

            // Star vertices + magnitude-scaled halos + star names
            var sumX = 0f
            var sumY = 0f
            var visibleCount = 0
            constellation.stars.forEach { star ->
              val pt = resolveScreenOffset(star.id) ?: return@forEach
              sumX += pt.x
              sumY += pt.y
              visibleCount++

              val starRadius = ((4.6f - star.magnitude * 0.7f).coerceIn(2.2f, 5.5f)).dp.toPx()
              drawCircle(
                color = lineColor.copy(alpha = 0.35f),
                radius = starRadius * 2.1f,
                center = pt,
              )
              drawCircle(
                color = Color.White,
                radius = starRadius,
                center = pt,
              )

              if (isAimed || star.magnitude <= 1.85f) {
                drawContext.canvas.nativeCanvas.drawText(
                  star.name,
                  pt.x + starRadius + 4.dp.toPx(),
                  pt.y + 3.dp.toPx(),
                  starLabelPaint,
                )
              }
            }

            // Constellation Title Banner above its centroid
            if (visibleCount > 0) {
              val cx = sumX / visibleCount
              val cy = (sumY / visibleCount) - 18.dp.toPx()
              if (cx in 16f..(w - 16f) && cy in 16f..(h - 16f)) {
                drawContext.canvas.nativeCanvas.drawText(
                  "${constellation.glyph} ${constellation.name.uppercase(Locale.US)}",
                  cx,
                  cy,
                  constTitlePaint,
                )
              }
            }
          }

          // Draw live Sun, Moon & Planets on the sky dome
          if (showPlanetMarkers) {
            val planetPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
              color = tertiaryColor.toArgb()
              textSize = 12.dp.toPx()
              typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            projectedPlanets.forEach { (placement, proj) ->
              if (proj.isInFrontOfCamera && proj.normX in 0.02f..0.98f && proj.normY in 0.02f..0.98f) {
                val px = proj.normX * w
                val py = proj.normY * h
                drawCircle(
                  color = tertiaryColor.copy(alpha = 0.28f),
                  radius = 12.dp.toPx(),
                  center = Offset(px, py),
                )
                drawCircle(
                  color = tertiaryColor,
                  radius = 5.dp.toPx(),
                  center = Offset(px, py),
                )
                drawContext.canvas.nativeCanvas.drawText(
                  "${placement.body.glyph} ${placement.body.displayName}",
                  px + 10.dp.toPx(),
                  py + 4.dp.toPx(),
                  planetPaint,
                )
              }
            }
          }

          // Center Target Reticle
          val cx = w / 2f
          val cy = h / 2f
          val reticleRadius = 26.dp.toPx()
          drawCircle(
            color = Color.White.copy(alpha = 0.45f),
            radius = reticleRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 1.5.dp.toPx()),
          )
          drawCircle(
            color = primaryColor,
            radius = 3.dp.toPx(),
            center = Offset(cx, cy),
          )
        }

        // Top HUD Overlay: Azimuth / Altitude Compass Pill + Sensor/Manual Recenter + Star-Snap Status
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Explore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = String.format(
                  Locale.US,
                  "Az %03d° • Alt %+02d°",
                  activeCameraAzimuth.roundToInt(),
                  activeCameraAltitude.roundToInt(),
                ),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (isManualPanMode) {
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.clickable {
                  haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                  isManualPanMode = false
                },
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Recenter live gyro sensor",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(14.dp),
                  )
                  Text(
                    text = "Live Gyro",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                  )
                }
              }
            }

            Surface(
              shape = RoundedCornerShape(20.dp),
              color = if (starSnapVisionEnabled) {
                MaterialTheme.colorScheme.tertiaryContainer
              } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
              },
              modifier = Modifier.clickable {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                starSnapVisionEnabled = !starSnapVisionEnabled
              },
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.CenterFocusStrong,
                  contentDescription = "Toggle Star-Snap CV",
                  tint = if (starSnapVisionEnabled) {
                    MaterialTheme.colorScheme.onTertiaryContainer
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  },
                  modifier = Modifier.size(14.dp),
                )
                Text(
                  text = if (activeVisionSnapMatch != null) {
                    "Star-Snap Locked"
                  } else if (starSnapVisionEnabled) {
                    "Star-Snap CV"
                  } else {
                    "Snap Off"
                  },
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (starSnapVisionEnabled) {
                    MaterialTheme.colorScheme.onTertiaryContainer
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  },
                )
              }
            }
          }
        }

        // Camera Permission Prompt Banner if Camera permission is not yet granted
        if (!hasSystemCameraPermission) {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(12.dp)
              .fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Live Sky Camera Overlay",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = "Grant Camera access to draw constellations directly over your live night-sky camera feed.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Button(
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                shape = CircleShape,
              ) {
                Text("Enable Camera", style = MaterialTheme.typography.labelSmall)
              }
            }
          }
        }
      }
    }

    // 3. Aimed Constellation Info & AR Snapshot Action Card
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
      ),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("stars_sky_ar_info_card"),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(
            modifier = Modifier
              .weight(1f)
              .clickable { onInspectConstellation(aimedConstellation) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = aimedConstellation.glyph,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
            }
            Column {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                  text = "${aimedConstellation.name} — ${aimedConstellation.subtitle}",
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
              val snapSubtitle = activeVisionSnapMatch?.asterismLabel ?: aimedConstellation.asterismNote
              Text(
                text = snapSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (activeVisionSnapMatch != null) {
                  MaterialTheme.colorScheme.tertiary
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          Button(
            onClick = { captureArSkyPhoto() },
            shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            modifier = Modifier.testTag("stars_sky_ar_snapshot_button"),
          ) {
            Icon(
              imageVector = Icons.Default.PhotoCamera,
              contentDescription = "Save AR sky snapshot",
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Snap AR", style = MaterialTheme.typography.labelMedium)
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          FilterChip(
            selected = showAllConstellations,
            onClick = {
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              showAllConstellations = !showAllConstellations
            },
            shape = RoundedCornerShape(20.dp),
            label = {
              Text(
                text = if (showAllConstellations) "All 16 Constellations" else "Focused Constellation Only",
                style = MaterialTheme.typography.labelSmall,
              )
            },
          )
          FilterChip(
            selected = showPlanetMarkers,
            onClick = {
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              showPlanetMarkers = !showPlanetMarkers
            },
            shape = RoundedCornerShape(20.dp),
            label = {
              Text(
                text = "Sun, Moon & Planets",
                style = MaterialTheme.typography.labelSmall,
              )
            },
          )
          TextButton(
            onClick = { onInspectConstellation(aimedConstellation) },
            shape = CircleShape,
          ) {
            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Star Lore", style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun StarsScreenPreview() {
  ToolboxTheme {
    StarsScreen(repository = InMemoryStarsRepository())
  }
}
