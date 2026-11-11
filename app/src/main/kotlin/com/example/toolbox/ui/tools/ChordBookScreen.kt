package com.example.toolbox.ui.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toolbox.ui.theme.ToolboxTheme
import java.util.UUID
import kotlin.math.roundToInt

/**
 * Primary navigation tabs inside the Chord Book tool.
 */
enum class ChordBookTab(val title: String) {
  CHORDS("Chords"),
  REVERSE("Reverse"),
  SCALES("Scales & Key"),
  SONGBOOK("Songbook"),
}

/**
 * Search mode inside the Reverse tab (Reverse Chord Search vs Reverse Scale Search).
 */
enum class ReverseSearchMode(val title: String) {
  CHORDS("Reverse Chord"),
  SCALES("Reverse Scale"),
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChordBookScreen(
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val haptic = LocalHapticFeedback.current

  val repository = remember(context, isInspection) {
    if (isInspection) null else ChordBookPreferencesRepository(context)
  }

  var customTunings by remember {
    mutableStateOf(repository?.loadCustomTunings() ?: emptyList())
  }
  val allTunings = remember(customTunings) {
    ChordBookEngine.BUILT_IN_TUNINGS + customTunings
  }

  var selectedTuningId by rememberSaveable {
    mutableStateOf(repository?.getSelectedTuningId() ?: ChordBookEngine.BUILT_IN_TUNINGS.first().id)
  }
  val activeTuning = remember(allTunings, selectedTuningId) {
    allTunings.find { it.id == selectedTuningId } ?: ChordBookEngine.BUILT_IN_TUNINGS.first()
  }

  var capoFret by rememberSaveable {
    mutableIntStateOf(repository?.getCapoFret() ?: 0)
  }
  var enharmonicPref by rememberSaveable {
    mutableStateOf(repository?.getEnharmonicPreference() ?: EnharmonicPreference.AUTO)
  }
  var dotLabelMode by rememberSaveable {
    mutableStateOf(repository?.getDotLabelMode() ?: DotLabelMode.INTERVALS)
  }

  var activeTab by rememberSaveable { mutableStateOf(ChordBookTab.CHORDS) }

  // --- Tab 1: Chords Explorer State ---
  var selectedRoot by rememberSaveable { mutableIntStateOf(0) } // C = 0
  var selectedQuality by rememberSaveable { mutableStateOf(ChordQuality.MAJOR) }
  var selectedChordCategory by rememberSaveable { mutableStateOf(ChordCategory.TRIADS_AND_BASIC) }
  var slashBassPitchClass by rememberSaveable { mutableStateOf<Int?>(null) }
  var showSlashBassRow by rememberSaveable { mutableStateOf(false) }
  var neckPositionFilter by rememberSaveable { mutableStateOf(NeckPositionFilter.ALL) }
  var allowInversions by rememberSaveable { mutableStateOf(false) }
  var allowMutedInside by rememberSaveable { mutableStateOf(false) }
  var inspectedVoicing by remember { mutableStateOf<ChordVoicing?>(null) }

  // --- Tab 2: Reverse Finder State ---
  var reverseSearchMode by rememberSaveable { mutableStateOf(ReverseSearchMode.CHORDS) }
  var reverseMultiNoteMode by rememberSaveable { mutableStateOf(false) }
  var reverseScaleRootFilter by rememberSaveable { mutableStateOf<Int?>(null) }
  var reverseScaleCategoryFilter by rememberSaveable { mutableStateOf<ScaleCategory?>(null) }
  var reverseScaleRequireAllNotes by rememberSaveable { mutableStateOf(false) }
  // Store selected (stringIndex, fret) pairs as "stringIndex:fret" strings for saveable state
  val reverseSelections = remember {
    mutableStateListOf("0:0", "1:3", "2:2", "3:0", "4:1", "5:0") // Default C major shape on 6-string
  }
  LaunchedEffect(activeTuning.stringCount) {
    reverseSelections.removeAll { entry ->
      val s = entry.substringBefore(":").toIntOrNull() ?: return@removeAll true
      s >= activeTuning.stringCount
    }
  }

  // --- Tab 3: Scales & Key Harmonizer State ---
  var scaleRoot by rememberSaveable { mutableIntStateOf(0) } // C = 0
  var selectedScaleCategory by rememberSaveable { mutableStateOf(ScaleCategory.MAJOR_AND_MODES) }
  var selectedScaleType by rememberSaveable { mutableStateOf(ScaleType.MAJOR) }
  var selectedBoxNumber by rememberSaveable { mutableIntStateOf(0) } // 0 = Full Neck, 1..5 = Box
  var harmonizeAsSevenths by rememberSaveable { mutableStateOf(true) }

  // --- Tab 4: Songbook / Progression Builder State ---
  var progressions by remember {
    mutableStateOf(repository?.loadProgressions() ?: ChordBookEngine.defaultProgressions())
  }
  var activeProgressionId by rememberSaveable {
    mutableStateOf(progressions.firstOrNull()?.id ?: "prog_axis")
  }
  val activeProgression = remember(progressions, activeProgressionId) {
    progressions.find { it.id == activeProgressionId } ?: progressions.firstOrNull()
  }

  // Dialog states
  var showTuningDialog by rememberSaveable { mutableStateOf(false) }
  var showNewProgressionDialog by rememberSaveable { mutableStateOf(false) }
  var showRenameProgressionDialog by rememberSaveable { mutableStateOf(false) }

  fun updateProgressions(updated: List<ChordProgression>) {
    progressions = updated
    repository?.saveProgressions(updated)
  }

  fun pinChordToActiveProgression(
    root: Int,
    quality: ChordQuality,
    bass: Int = root,
    preferredFrets: List<Int>? = null,
  ) {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    val currentList = progressions.ifEmpty { ChordBookEngine.defaultProgressions() }
    val targetProg = currentList.find { it.id == activeProgressionId } ?: currentList.first()
    val newChord = SavedProgressionChord(
      id = UUID.randomUUID().toString().take(8),
      rootPitchClass = root.mod(12),
      quality = quality,
      bassPitchClass = bass.mod(12),
      preferredFrets = preferredFrets,
    )
    val updatedProg = targetProg.copy(chords = targetProg.chords + newChord)
    val updatedList = currentList.map { if (it.id == targetProg.id) updatedProg else it }
    updateProgressions(updatedList)
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("chord_book_screen"),
  ) {
    // 1. Standard Tool Header
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
                imageVector = Icons.Default.LibraryMusic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Chord Book",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              val capoText = if (capoFret > 0) " • Capo $capoFret" else ""
              Text(
                text = "${activeTuning.instrument.shortName} • ${activeTuning.formattedNotes(capoFret, enharmonicPref)}$capoText",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("chord_book_header_subtext"),
              )
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            // Dot label mode pill cycles Intervals -> Notes -> Fingers
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = MaterialTheme.colorScheme.secondaryContainer,
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  val next = when (dotLabelMode) {
                    DotLabelMode.INTERVALS -> DotLabelMode.NOTES
                    DotLabelMode.NOTES -> DotLabelMode.FINGERS
                    DotLabelMode.FINGERS -> DotLabelMode.INTERVALS
                  }
                  dotLabelMode = next
                  repository?.setDotLabelMode(next)
                }
                .testTag("chord_book_dot_label_toggle"),
            ) {
              Text(
                text = dotLabelMode.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              )
            }

            // Enharmonic accidental toggle (Auto / # / b)
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  val next = when (enharmonicPref) {
                    EnharmonicPreference.AUTO -> EnharmonicPreference.SHARPS
                    EnharmonicPreference.SHARPS -> EnharmonicPreference.FLATS
                    EnharmonicPreference.FLATS -> EnharmonicPreference.AUTO
                  }
                  enharmonicPref = next
                  repository?.setEnharmonicPreference(next)
                }
                .testTag("chord_book_enharmonic_toggle"),
            ) {
              Text(
                text = when (enharmonicPref) {
                  EnharmonicPreference.AUTO -> "♯/♭"
                  EnharmonicPreference.SHARPS -> "♯"
                  EnharmonicPreference.FLATS -> "♭"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              )
            }

            // Instrument, Tuning & Capo configuration button
            FilledTonalIconButton(
              onClick = { showTuningDialog = true },
              modifier = Modifier
                .size(36.dp)
                .testTag("chord_book_tuning_button"),
            ) {
              Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Instrument, Tuning & Capo",
                modifier = Modifier.size(18.dp),
              )
            }
          }
        }
      }
    }

    // 2. Mode Switcher Pill Bar
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 1.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        ChordBookTab.entries.forEach { tab ->
          val isSelected = activeTab == tab
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isSelected) {
              MaterialTheme.colorScheme.primaryContainer
            } else {
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(20.dp))
              .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                activeTab = tab
              }
              .testTag("chord_book_tab_${tab.name}"),
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            ) {
              Text(
                text = if (tab == ChordBookTab.SONGBOOK && activeProgression != null) {
                  "Songbook (${activeProgression.chords.size})"
                } else {
                  tab.title
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) {
                  MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

    // 3. Active Tab Content
    when (activeTab) {
      ChordBookTab.CHORDS -> {
        ChordsExplorerTabContent(
          activeTuning = activeTuning,
          capoFret = capoFret,
          enharmonicPref = enharmonicPref,
          dotLabelMode = dotLabelMode,
          selectedRoot = selectedRoot,
          onSelectRoot = {
            selectedRoot = it
            if (slashBassPitchClass == it) slashBassPitchClass = null
          },
          selectedCategory = selectedChordCategory,
          onSelectCategory = { cat ->
            selectedChordCategory = cat
            val firstInCat = ChordQuality.entries.firstOrNull { it.category == cat }
            if (firstInCat != null && selectedQuality.category != cat) {
              selectedQuality = firstInCat
            }
          },
          selectedQuality = selectedQuality,
          onSelectQuality = { selectedQuality = it },
          slashBassPitchClass = slashBassPitchClass,
          onSelectSlashBass = { slashBassPitchClass = it },
          showSlashBassRow = showSlashBassRow,
          onToggleSlashBassRow = { showSlashBassRow = !showSlashBassRow },
          neckPositionFilter = neckPositionFilter,
          onSelectNeckPositionFilter = { neckPositionFilter = it },
          allowInversions = allowInversions,
          onToggleAllowInversions = { allowInversions = !allowInversions },
          allowMutedInside = allowMutedInside,
          onToggleAllowMutedInside = { allowMutedInside = !allowMutedInside },
          onInspectVoicing = { inspectedVoicing = it },
          onPinChord = { preferredFrets ->
            pinChordToActiveProgression(
              root = selectedRoot,
              quality = selectedQuality,
              bass = slashBassPitchClass ?: selectedRoot,
              preferredFrets = preferredFrets,
            )
          },
          modifier = Modifier.weight(1f),
        )
      }

      ChordBookTab.REVERSE -> {
        ReverseFinderTabContent(
          activeTuning = activeTuning,
          capoFret = capoFret,
          enharmonicPref = enharmonicPref,
          dotLabelMode = dotLabelMode,
          searchMode = reverseSearchMode,
          onSelectSearchMode = { mode ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            reverseSearchMode = mode
            if (mode == ReverseSearchMode.SCALES) {
              reverseMultiNoteMode = true
            }
          },
          multiNoteMode = reverseMultiNoteMode,
          onToggleMultiNoteMode = {
            reverseMultiNoteMode = !reverseMultiNoteMode
            if (!reverseMultiNoteMode) {
              // Keep at most 1 note per string
              val seenStrings = mutableSetOf<Int>()
              val filtered = reverseSelections.filter { entry ->
                val s = entry.substringBefore(":").toIntOrNull() ?: return@filter false
                seenStrings.add(s)
              }
              reverseSelections.clear()
              reverseSelections.addAll(filtered)
            }
          },
          scaleRootFilter = reverseScaleRootFilter,
          onSelectScaleRootFilter = { rootFilter ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            reverseScaleRootFilter = rootFilter
          },
          scaleCategoryFilter = reverseScaleCategoryFilter,
          onSelectScaleCategoryFilter = { catFilter ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            reverseScaleCategoryFilter = catFilter
          },
          requireAllScaleNotes = reverseScaleRequireAllNotes,
          onToggleRequireAllScaleNotes = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            reverseScaleRequireAllNotes = !reverseScaleRequireAllNotes
          },
          selections = reverseSelections,
          onToggleFret = { stringIdx, fret ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val key = "$stringIdx:$fret"
            if (reverseSelections.contains(key)) {
              reverseSelections.remove(key)
            } else {
              if (!reverseMultiNoteMode && reverseSearchMode == ReverseSearchMode.CHORDS) {
                reverseSelections.removeAll { it.startsWith("$stringIdx:") }
              }
              reverseSelections.add(key)
            }
          },
          onTogglePitchClass = { targetPc ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val targetNormalized = targetPc.mod(12)
            val matchingKeys = reverseSelections.filter { entry ->
              val parts = entry.split(":")
              if (parts.size != 2) return@filter false
              val s = parts[0].toIntOrNull() ?: return@filter false
              val f = parts[1].toIntOrNull() ?: return@filter false
              if (s !in 0 until activeTuning.stringCount) return@filter false
              (activeTuning.effectiveMidi(s, capoFret) + f).mod(12) == targetNormalized
            }
            if (matchingKeys.isNotEmpty()) {
              reverseSelections.removeAll(matchingKeys.toSet())
            } else {
              // Find a comfortable low fret (0..11) on the fretboard that produces targetNormalized
              var bestString = 0
              var bestFret = (targetNormalized - activeTuning.effectiveMidi(0, capoFret).mod(12)).mod(12)
              for (s in 0 until activeTuning.stringCount) {
                val openPc = activeTuning.effectiveMidi(s, capoFret).mod(12)
                val f = (targetNormalized - openPc).mod(12)
                if (f < bestFret) {
                  bestFret = f
                  bestString = s
                }
              }
              reverseSelections.add("$bestString:$bestFret")
            }
          },
          onClearAll = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            reverseSelections.clear()
          },
          onJumpToChord = { match ->
            selectedRoot = match.rootPitchClass
            selectedChordCategory = match.quality.category
            selectedQuality = match.quality
            slashBassPitchClass = if (match.bassPitchClass != match.rootPitchClass) {
              match.bassPitchClass
            } else {
              null
            }
            showSlashBassRow = slashBassPitchClass != null
            activeTab = ChordBookTab.CHORDS
          },
          onPinMatch = { match ->
            pinChordToActiveProgression(
              root = match.rootPitchClass,
              quality = match.quality,
              bass = match.bassPitchClass,
            )
          },
          onJumpToScale = { scaleMatch ->
            scaleRoot = scaleMatch.rootPitchClass
            selectedScaleCategory = scaleMatch.scaleType.category
            selectedScaleType = scaleMatch.scaleType
            selectedBoxNumber = 0
            activeTab = ChordBookTab.SCALES
          },
          modifier = Modifier.weight(1f),
        )
      }

      ChordBookTab.SCALES -> {
        ScalesAndKeyTabContent(
          activeTuning = activeTuning,
          capoFret = capoFret,
          enharmonicPref = enharmonicPref,
          dotLabelMode = dotLabelMode,
          scaleRoot = scaleRoot,
          onSelectScaleRoot = { scaleRoot = it },
          selectedScaleCategory = selectedScaleCategory,
          onSelectScaleCategory = { cat ->
            selectedScaleCategory = cat
            val firstInCat = ScaleType.entries.firstOrNull { it.category == cat }
            if (firstInCat != null && selectedScaleType.category != cat) {
              selectedScaleType = firstInCat
            }
          },
          selectedScaleType = selectedScaleType,
          onSelectScaleType = { selectedScaleType = it },
          selectedBoxNumber = selectedBoxNumber,
          onSelectBoxNumber = { selectedBoxNumber = it },
          harmonizeAsSevenths = harmonizeAsSevenths,
          onToggleHarmonizeSevenths = { harmonizeAsSevenths = !harmonizeAsSevenths },
          onOpenReverseScaleSearch = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            reverseSearchMode = ReverseSearchMode.SCALES
            reverseMultiNoteMode = true
            activeTab = ChordBookTab.REVERSE
          },
          onJumpToDiatonicChord = { root, quality ->
            selectedRoot = root
            selectedChordCategory = quality.category
            selectedQuality = quality
            slashBassPitchClass = null
            showSlashBassRow = false
            activeTab = ChordBookTab.CHORDS
          },
          onPinDiatonicChord = { root, quality ->
            pinChordToActiveProgression(root = root, quality = quality)
          },
          modifier = Modifier.weight(1f),
        )
      }

      ChordBookTab.SONGBOOK -> {
        SongbookTabContent(
          activeTuning = activeTuning,
          capoFret = capoFret,
          enharmonicPref = enharmonicPref,
          dotLabelMode = dotLabelMode,
          progressions = progressions,
          activeProgression = activeProgression,
          onSelectProgression = { activeProgressionId = it.id },
          onRequestNewProgression = { showNewProgressionDialog = true },
          onRequestRenameProgression = { showRenameProgressionDialog = true },
          onDeleteProgression = { progToDelete ->
            if (progressions.size > 1) {
              val remaining = progressions.filter { it.id != progToDelete.id }
              updateProgressions(remaining)
              activeProgressionId = remaining.first().id
            }
          },
          onTransposeProgression = { semitones ->
            val current = activeProgression ?: return@SongbookTabContent
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val transposed = current.copy(
              chords = current.chords.map { it.transpose(semitones) },
            )
            updateProgressions(progressions.map { if (it.id == current.id) transposed else it })
          },
          onMoveChord = { index, delta ->
            val current = activeProgression ?: return@SongbookTabContent
            val targetIdx = index + delta
            if (index in current.chords.indices && targetIdx in current.chords.indices) {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              val mutable = current.chords.toMutableList()
              val item = mutable.removeAt(index)
              mutable.add(targetIdx, item)
              val updated = current.copy(chords = mutable)
              updateProgressions(progressions.map { if (it.id == current.id) updated else it })
            }
          },
          onRemoveChord = { chordId ->
            val current = activeProgression ?: return@SongbookTabContent
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val updated = current.copy(chords = current.chords.filter { it.id != chordId })
            updateProgressions(progressions.map { if (it.id == current.id) updated else it })
          },
          onJumpToChordVoicings = { savedChord ->
            selectedRoot = savedChord.rootPitchClass
            selectedChordCategory = savedChord.quality.category
            selectedQuality = savedChord.quality
            slashBassPitchClass = if (savedChord.bassPitchClass != savedChord.rootPitchClass) {
              savedChord.bassPitchClass
            } else {
              null
            }
            showSlashBassRow = slashBassPitchClass != null
            activeTab = ChordBookTab.CHORDS
          },
          onJumpToScale = { scaleMatch ->
            scaleRoot = scaleMatch.rootPitchClass
            selectedScaleCategory = scaleMatch.scaleType.category
            selectedScaleType = scaleMatch.scaleType
            selectedBoxNumber = 0
            activeTab = ChordBookTab.SCALES
          },
          modifier = Modifier.weight(1f),
        )
      }
    }
  }

  // --- Dialogs ---

  if (showTuningDialog) {
    InstrumentAndTuningDialog(
      allTunings = allTunings,
      activeTuning = activeTuning,
      capoFret = capoFret,
      enharmonicPref = enharmonicPref,
      onSelectTuning = { tuning ->
        selectedTuningId = tuning.id
        repository?.setSelectedTuningId(tuning.id)
      },
      onCapoChange = { newCapo ->
        capoFret = newCapo
        repository?.setCapoFret(newCapo)
      },
      onSaveCustomTuning = { newCustom ->
        val updatedCustom = customTunings.filter { it.id != newCustom.id } + newCustom
        customTunings = updatedCustom
        repository?.saveCustomTunings(updatedCustom)
        selectedTuningId = newCustom.id
        repository?.setSelectedTuningId(newCustom.id)
      },
      onDeleteCustomTuning = { toDelete ->
        val updatedCustom = customTunings.filter { it.id != toDelete.id }
        customTunings = updatedCustom
        repository?.saveCustomTunings(updatedCustom)
        if (selectedTuningId == toDelete.id) {
          selectedTuningId = ChordBookEngine.BUILT_IN_TUNINGS.first().id
          repository?.setSelectedTuningId(selectedTuningId)
        }
      },
      onDismiss = { showTuningDialog = false },
    )
  }

  inspectedVoicing?.let { voicing ->
    VoicingInspectorDialog(
      voicing = voicing,
      tuning = activeTuning,
      capoFret = capoFret,
      enharmonicPref = enharmonicPref,
      dotLabelMode = dotLabelMode,
      onPinVoicing = {
        pinChordToActiveProgression(
          root = voicing.rootPitchClass,
          quality = voicing.quality,
          bass = voicing.bassPitchClass,
          preferredFrets = voicing.frets,
        )
        inspectedVoicing = null
      },
      onDismiss = { inspectedVoicing = null },
    )
  }

  if (showNewProgressionDialog) {
    ProgressionNameDialog(
      title = "New Progression",
      subtitle = "Create a new songbook progression",
      initialName = "Progression ${progressions.size + 1}",
      contextLabel = "Starts with an empty chord sequence ready for pinning chords",
      onConfirm = { newName ->
        val newProg = ChordProgression(
          id = "prog_${UUID.randomUUID().toString().take(8)}",
          name = newName,
          chords = emptyList(),
        )
        val updated = progressions + newProg
        updateProgressions(updated)
        activeProgressionId = newProg.id
        showNewProgressionDialog = false
      },
      onDismiss = { showNewProgressionDialog = false },
    )
  }

  if (showRenameProgressionDialog && activeProgression != null) {
    ProgressionNameDialog(
      title = "Rename Progression",
      subtitle = "Enter a new title for this progression",
      initialName = activeProgression.name,
      contextLabel = "${activeProgression.chords.size} chords in progression",
      onConfirm = { newName ->
        val updated = progressions.map {
          if (it.id == activeProgression.id) it.copy(name = newName) else it
        }
        updateProgressions(updated)
        showRenameProgressionDialog = false
      },
      onDismiss = { showRenameProgressionDialog = false },
    )
  }
}

// ============================================================================
// TAB 1: CHORDS EXPLORER & DYNAMIC VOICING GENERATOR
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChordsExplorerTabContent(
  activeTuning: ChordTuning,
  capoFret: Int,
  enharmonicPref: EnharmonicPreference,
  dotLabelMode: DotLabelMode,
  selectedRoot: Int,
  onSelectRoot: (Int) -> Unit,
  selectedCategory: ChordCategory,
  onSelectCategory: (ChordCategory) -> Unit,
  selectedQuality: ChordQuality,
  onSelectQuality: (ChordQuality) -> Unit,
  slashBassPitchClass: Int?,
  onSelectSlashBass: (Int?) -> Unit,
  showSlashBassRow: Boolean,
  onToggleSlashBassRow: () -> Unit,
  neckPositionFilter: NeckPositionFilter,
  onSelectNeckPositionFilter: (NeckPositionFilter) -> Unit,
  allowInversions: Boolean,
  onToggleAllowInversions: () -> Unit,
  allowMutedInside: Boolean,
  onToggleAllowMutedInside: () -> Unit,
  onInspectVoicing: (ChordVoicing) -> Unit,
  onPinChord: (List<Int>?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val voicings = remember(
    selectedRoot,
    selectedQuality,
    activeTuning,
    capoFret,
    slashBassPitchClass,
    neckPositionFilter,
    allowInversions,
    allowMutedInside,
  ) {
    ChordBookEngine.generateChordVoicings(
      rootPitchClass = selectedRoot,
      quality = selectedQuality,
      tuning = activeTuning,
      capoFret = capoFret,
      slashBassPitchClass = slashBassPitchClass,
      positionFilter = neckPositionFilter,
      allowInversions = allowInversions,
      allowMutedInside = allowMutedInside,
      maxResults = 36,
    )
  }

  val chordSymbol = ChordBookEngine.formatChordSymbol(
    rootPitchClass = selectedRoot,
    quality = selectedQuality,
    bassPitchClass = slashBassPitchClass,
    preference = enharmonicPref,
  )
  val chordPcs = remember(selectedRoot, selectedQuality) {
    ChordBookEngine.chordPitchClasses(selectedRoot, selectedQuality)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("chord_book_chords_list"),
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    // 1. Root & Quality Selection Card
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
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          // Root Pitch Selector Row + Slash Bass Toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Root Note",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = if (slashBassPitchClass != null || showSlashBassRow) {
                MaterialTheme.colorScheme.tertiaryContainer
              } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
              },
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { onToggleSlashBassRow() }
                .testTag("slash_bass_toggle"),
            ) {
              val bassLabel = if (slashBassPitchClass != null) {
                "/${ChordBookEngine.formatPitchClass(slashBassPitchClass, enharmonicPref, selectedRoot)} Bass"
              } else {
                "/ Slash Bass"
              }
              Text(
                text = bassLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (slashBassPitchClass != null || showSlashBassRow) {
                  MaterialTheme.colorScheme.onTertiaryContainer
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              )
            }
          }

          PitchClassSelectorBar(
            selectedPitchClass = selectedRoot,
            enharmonicPref = enharmonicPref,
            onSelectPitchClass = onSelectRoot,
            tagPrefix = "chord_root",
          )

          // Expandable Slash Bass Selector
          AnimatedVisibility(
            visible = showSlashBassRow,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "Slash Bass Note (Inversion / Pedal)",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (slashBassPitchClass != null) {
                  Text(
                    text = "Clear",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onSelectSlashBass(null) },
                  )
                }
              }
              LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                  FilterChip(
                    selected = slashBassPitchClass == null,
                    onClick = { onSelectSlashBass(null) },
                    label = { Text("None") },
                    shape = RoundedCornerShape(20.dp),
                  )
                }
                items(12) { pc ->
                  if (pc != selectedRoot) {
                    val label = "/${ChordBookEngine.formatPitchClass(pc, enharmonicPref, selectedRoot)}"
                    FilterChip(
                      selected = slashBassPitchClass == pc,
                      onClick = { onSelectSlashBass(pc) },
                      label = { Text(label) },
                      shape = RoundedCornerShape(20.dp),
                      modifier = Modifier.testTag("slash_bass_chip_$pc"),
                    )
                  }
                }
              }
            }
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

          // Chord Quality Category Selector
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(ChordCategory.entries) { cat ->
              val isSelected = selectedCategory == cat
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.surfaceContainerHigh
                },
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .clickable { onSelectCategory(cat) }
                  .testTag("chord_category_${cat.name}"),
              ) {
                Text(
                  text = cat.title,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  },
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
              }
            }
          }

          // Chord Qualities within selected category
          val qualitiesInCategory = remember(selectedCategory) {
            ChordQuality.entries.filter { it.category == selectedCategory }
          }
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(qualitiesInCategory) { q ->
              val isSelected = selectedQuality == q
              val previewSymbol = ChordBookEngine.formatChordSymbol(
                rootPitchClass = selectedRoot,
                quality = q,
                preference = enharmonicPref,
              )
              FilterChip(
                selected = isSelected,
                onClick = { onSelectQuality(q) },
                label = {
                  Text(
                    text = "$previewSymbol (${q.fullName})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  )
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("chord_quality_${q.name}"),
              )
            }
          }
        }
      }
    }

    // 2. Active Chord Formula & Quick Pin Banner
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        ),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = chordSymbol,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.testTag("active_chord_symbol"),
              )
              Text(
                text = selectedQuality.fullName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
              )
            }

            // Intervals + Notes summary
            val formulaText = selectedQuality.intervals.joinToString(" – ") { iv ->
              ChordBookEngine.intervalName(iv, selectedQuality)
            }
            val notesText = chordPcs.joinToString(" – ") { pc ->
              ChordBookEngine.formatPitchClass(pc, enharmonicPref, selectedRoot)
            }
            Text(
              text = "Formula: $formulaText  •  Notes: $notesText",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.testTag("active_chord_formula"),
            )
          }

          FilledTonalButton(
            onClick = { onPinChord(voicings.firstOrNull()?.frets) },
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.testTag("pin_active_chord_button"),
          ) {
            Icon(
              imageVector = Icons.Default.BookmarkAdd,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Pin", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // 3. Position & Voicing Filters Row
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        NeckPositionFilter.entries.forEach { filter ->
          FilterChip(
            selected = neckPositionFilter == filter,
            onClick = { onSelectNeckPositionFilter(filter) },
            label = { Text(filter.label, style = MaterialTheme.typography.labelSmall) },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("position_filter_${filter.name}"),
          )
        }
        if (slashBassPitchClass == null) {
          FilterChip(
            selected = allowInversions,
            onClick = onToggleAllowInversions,
            label = { Text("Inversions", style = MaterialTheme.typography.labelSmall) },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("toggle_allow_inversions"),
          )
        }
        FilterChip(
          selected = allowMutedInside,
          onClick = onToggleAllowMutedInside,
          label = { Text("Interior Mutes", style = MaterialTheme.typography.labelSmall) },
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier.testTag("toggle_allow_muted_inside"),
        )
      }
    }

    // 4. Voicings Count Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "${voicings.size} Playable Voicings Computed",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.testTag("voicings_count_text"),
        )
        Text(
          text = "Tap any diagram to inspect or pin",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    // 5. Voicings Grid (2 per row)
    if (voicings.isEmpty()) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(
              text = "No voicings match the current filter constraints",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Try enabling Inversions, Interior Mutes, or switching to All Frets.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
            )
          }
        }
      }
    } else {
      val chunked = voicings.chunked(2)
      itemsIndexed(chunked) { rowIdx, pair ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          pair.forEachIndexed { colIdx, voicing ->
            val index = rowIdx * 2 + colIdx
            VoicingDiagramCard(
              voicing = voicing,
              tuning = activeTuning,
              capoFret = capoFret,
              enharmonicPref = enharmonicPref,
              dotLabelMode = dotLabelMode,
              index = index,
              onClick = { onInspectVoicing(voicing) },
              modifier = Modifier
                .weight(1f)
                .testTag("voicing_card_$index"),
            )
          }
          if (pair.size == 1) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }
  }
}

// ============================================================================
// TAB 2: REVERSE CHORD & SCALE FINDER
// ============================================================================

@Composable
private fun ReverseFinderTabContent(
  activeTuning: ChordTuning,
  capoFret: Int,
  enharmonicPref: EnharmonicPreference,
  dotLabelMode: DotLabelMode,
  searchMode: ReverseSearchMode,
  onSelectSearchMode: (ReverseSearchMode) -> Unit,
  multiNoteMode: Boolean,
  onToggleMultiNoteMode: () -> Unit,
  scaleRootFilter: Int?,
  onSelectScaleRootFilter: (Int?) -> Unit,
  scaleCategoryFilter: ScaleCategory?,
  onSelectScaleCategoryFilter: (ScaleCategory?) -> Unit,
  requireAllScaleNotes: Boolean,
  onToggleRequireAllScaleNotes: () -> Unit,
  selections: List<String>,
  onToggleFret: (stringIdx: Int, fret: Int) -> Unit,
  onTogglePitchClass: (pitchClass: Int) -> Unit,
  onClearAll: () -> Unit,
  onJumpToChord: (ReverseChordMatch) -> Unit,
  onPinMatch: (ReverseChordMatch) -> Unit,
  onJumpToScale: (ScaleMatch) -> Unit,
  modifier: Modifier = Modifier,
) {
  val selectedPairs = remember(selections.toList(), activeTuning) {
    selections.mapNotNull { entry ->
      val parts = entry.split(":")
      if (parts.size != 2) return@mapNotNull null
      val s = parts[0].toIntOrNull() ?: return@mapNotNull null
      val f = parts[1].toIntOrNull() ?: return@mapNotNull null
      if (s in 0 until activeTuning.stringCount && f in 0..15) s to f else null
    }
  }

  val selectedMidiNotes = remember(selectedPairs, activeTuning, capoFret) {
    selectedPairs.map { (s, f) -> activeTuning.effectiveMidi(s, capoFret) + f }
  }
  val selectedPitchClasses = remember(selectedMidiNotes) {
    selectedMidiNotes.map { it.mod(12) }.toSet()
  }

  val chordMatches = remember(selectedMidiNotes) {
    ChordBookEngine.reverseIdentifyChords(selectedMidiNotes, maxResults = 16)
  }
  val lowestPc = remember(selectedMidiNotes) {
    selectedMidiNotes.minOrNull()?.mod(12)
  }
  val scaleMatches = remember(
    selectedPitchClasses,
    lowestPc,
    scaleRootFilter,
    scaleCategoryFilter,
    requireAllScaleNotes,
  ) {
    ChordBookEngine.reverseIdentifyScales(
      selectedPitchClasses = selectedPitchClasses,
      preferredRoot = scaleRootFilter ?: lowestPc,
      filterRoot = scaleRootFilter,
      filterCategory = scaleCategoryFilter,
      requireAllNotesMatch = requireAllScaleNotes,
      maxResults = 18,
    )
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("chord_book_reverse_list"),
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // 0. Reverse Mode Switcher (Reverse Chord Search vs Reverse Scale Search)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        ReverseSearchMode.entries.forEach { mode ->
          val isSelected = searchMode == mode
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isSelected) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            },
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(20.dp))
              .clickable { onSelectSearchMode(mode) }
              .testTag(
                if (mode == ReverseSearchMode.CHORDS) {
                  "reverse_mode_chords"
                } else {
                  "reverse_mode_scales"
                },
              ),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = if (mode == ReverseSearchMode.CHORDS) {
                  Icons.Default.MusicNote
                } else {
                  Icons.Default.Search
                },
                contentDescription = null,
                tint = if (isSelected) {
                  MaterialTheme.colorScheme.onPrimary
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (mode == ReverseSearchMode.CHORDS) {
                  "Reverse Chord Search"
                } else {
                  "Reverse Scale Search"
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) {
                  MaterialTheme.colorScheme.onPrimary
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }
    }

    // 1. Controls & Note Input Card
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
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (searchMode == ReverseSearchMode.SCALES) {
                  "Reverse Scale Search"
                } else {
                  "Interactive Fretboard Search"
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("reverse_search_mode_title"),
              )
              Text(
                text = if (searchMode == ReverseSearchMode.SCALES) {
                  "Select notes below or tap frets on the neck to identify matching scales and modes."
                } else {
                  "Tap strings and frets below to identify chords, inversions, and compatible scales."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            OutlinedButton(
              onClick = onClearAll,
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              modifier = Modifier.testTag("reverse_clear_button"),
            ) {
              Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Clear", style = MaterialTheme.typography.labelMedium)
            }
          }

          if (searchMode == ReverseSearchMode.CHORDS) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                  selected = !multiNoteMode,
                  onClick = { if (multiNoteMode) onToggleMultiNoteMode() },
                  label = { Text("Chord Shape (1/string)", style = MaterialTheme.typography.labelSmall) },
                  shape = RoundedCornerShape(20.dp),
                  modifier = Modifier.testTag("reverse_single_mode_chip"),
                )
                FilterChip(
                  selected = multiNoteMode,
                  onClick = { if (!multiNoteMode) onToggleMultiNoteMode() },
                  label = { Text("Multi-Note / Scale", style = MaterialTheme.typography.labelSmall) },
                  shape = RoundedCornerShape(20.dp),
                  modifier = Modifier.testTag("reverse_multi_mode_chip"),
                )
              }
            }
          } else {
            // Reverse Scale Search: 12-Note Pitch Class Multi-Select Bar + Root & Category Filters
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Tap Pitch Classes (${selectedPitchClasses.size} selected)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              if (scaleRootFilter != null || scaleCategoryFilter != null || requireAllScaleNotes) {
                Text(
                  text = "Reset Filters",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier
                    .clickable {
                      onSelectScaleRootFilter(null)
                      onSelectScaleCategoryFilter(null)
                      if (requireAllScaleNotes) onToggleRequireAllScaleNotes()
                    }
                    .testTag("reverse_scale_reset_filters"),
                )
              }
            }

            PitchClassMultiSelectorBar(
              selectedPitchClasses = selectedPitchClasses,
              highlightedRootPitchClass = scaleRootFilter ?: lowestPc,
              enharmonicPref = enharmonicPref,
              onTogglePitchClass = onTogglePitchClass,
              tagPrefix = "reverse_scale_pitch",
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            // Optional Root / Tonic Filter Row
            Text(
              text = "Root / Tonic Filter",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              item {
                FilterChip(
                  selected = scaleRootFilter == null,
                  onClick = { onSelectScaleRootFilter(null) },
                  label = { Text("Any Root", style = MaterialTheme.typography.labelSmall) },
                  shape = RoundedCornerShape(20.dp),
                  modifier = Modifier.testTag("reverse_scale_root_any"),
                )
              }
              items(12) { pc ->
                val rootLabel = ChordBookEngine.formatPitchClass(pc, enharmonicPref, pc)
                FilterChip(
                  selected = scaleRootFilter == pc,
                  onClick = {
                    onSelectScaleRootFilter(if (scaleRootFilter == pc) null else pc)
                  },
                  label = { Text("$rootLabel Root", style = MaterialTheme.typography.labelSmall) },
                  shape = RoundedCornerShape(20.dp),
                  modifier = Modifier.testTag("reverse_scale_root_filter_$pc"),
                )
              }
            }

            // Scale Category & Exactness Filter Row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              item {
                FilterChip(
                  selected = requireAllScaleNotes,
                  onClick = onToggleRequireAllScaleNotes,
                  label = { Text("100% Fit Only", style = MaterialTheme.typography.labelSmall) },
                  shape = RoundedCornerShape(20.dp),
                  modifier = Modifier.testTag("reverse_scale_exact_only_chip"),
                )
              }
              item {
                FilterChip(
                  selected = scaleCategoryFilter == null,
                  onClick = { onSelectScaleCategoryFilter(null) },
                  label = { Text("All Categories", style = MaterialTheme.typography.labelSmall) },
                  shape = RoundedCornerShape(20.dp),
                  modifier = Modifier.testTag("reverse_scale_cat_all"),
                )
              }
              items(ScaleCategory.entries) { cat ->
                FilterChip(
                  selected = scaleCategoryFilter == cat,
                  onClick = {
                    onSelectScaleCategoryFilter(if (scaleCategoryFilter == cat) null else cat)
                  },
                  label = { Text(cat.title, style = MaterialTheme.typography.labelSmall) },
                  shape = RoundedCornerShape(20.dp),
                  modifier = Modifier.testTag("reverse_scale_cat_${cat.name}"),
                )
              }
            }
          }

          // Active notes summary pills
          if (selectedPairs.isNotEmpty()) {
            val notesSummary = if (searchMode == ReverseSearchMode.SCALES) {
              val sortedPcs = selectedPitchClasses.sorted()
              sortedPcs.joinToString(" – ") { pc ->
                ChordBookEngine.formatPitchClass(pc, enharmonicPref, scaleRootFilter ?: lowestPc ?: pc)
              }
            } else {
              selectedPairs
                .sortedBy { it.first }
                .joinToString(" • ") { (s, f) ->
                  val midi = activeTuning.effectiveMidi(s, capoFret) + f
                  "${ChordBookEngine.formatMidiNote(midi, enharmonicPref)} (Fret $f)"
                }
            }
            Text(
              text = "Selected: $notesSummary",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.testTag("reverse_selected_notes_text"),
            )
          } else {
            Text(
              text = if (searchMode == ReverseSearchMode.SCALES) {
                "No notes selected — tap pitch classes above or frets on the neck below"
              } else {
                "No notes selected — tap any fret on the neck below"
              },
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }

    // 2. Interactive Horizontal Fretboard Canvas (Frets 0..12)
    item {
      InteractiveReverseFretboard(
        tuning = activeTuning,
        capoFret = capoFret,
        enharmonicPref = enharmonicPref,
        selectedPairs = selectedPairs.toSet(),
        selectedPitchClasses = selectedPitchClasses,
        highlightAllMatchingPitchClasses = searchMode == ReverseSearchMode.SCALES,
        lowestPitchClass = scaleRootFilter ?: lowestPc,
        onToggleFret = onToggleFret,
      )
    }

    if (searchMode == ReverseSearchMode.SCALES) {
      // 3A. Primary Results in Reverse Scale Search: Identified Scales & Modes
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Identified Scales & Modes (${scaleMatches.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag("reverse_scale_matches_header"),
          )
          Text(
            text = "Tap any scale to open diagram & chords",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      if (scaleMatches.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = if (selectedPitchClasses.isEmpty()) {
                "Select notes on the pitch selector or fretboard above to search matching scales and modes."
              } else {
                "No scale matches the active notes and filters. Try clearing Root / Category filters or disabling 100% Fit Only."
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(16.dp),
            )
          }
        }
      } else {
        itemsIndexed(scaleMatches) { idx, scaleMatch ->
          ReverseScaleMatchCard(
            idx = idx,
            scaleMatch = scaleMatch,
            enharmonicPref = enharmonicPref,
            onJumpToScale = { onJumpToScale(scaleMatch) },
          )
        }
      }
    }

    // 3B. Identified Chords Section (Primary in Chord mode, Secondary in Scale mode)
    item {
      Text(
        text = "Identified Chords (${chordMatches.size})",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag("reverse_chord_matches_header"),
      )
    }

    if (chordMatches.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = if (selectedPitchClasses.size < 2) {
              "Select at least 2 distinct notes on the fretboard to identify chords."
            } else {
              "No standard chord matches all selected notes simultaneously. Try removing passing tones or using Reverse Scale Search."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
          )
        }
      }
    } else {
      itemsIndexed(chordMatches) { idx, match ->
        val chordName = match.formatChordName(enharmonicPref)
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (idx == 0 && match.isExactMatch) {
              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            },
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("reverse_chord_match_$idx"),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = chordName,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Surface(
                  shape = RoundedCornerShape(20.dp),
                  color = when {
                    match.isExactMatch && match.isRootBass -> MaterialTheme.colorScheme.primaryContainer
                    match.isExactMatch -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.secondaryContainer
                  },
                ) {
                  val badgeText = when {
                    match.isExactMatch && match.isRootBass -> "Exact"
                    match.isExactMatch -> "Slash / Inversion"
                    else -> {
                      val omitted = match.missingIntervals.joinToString(",") {
                        ChordBookEngine.intervalName(it, match.quality)
                      }
                      "Implied (no $omitted)"
                    }
                  }
                  Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                      match.isExactMatch && match.isRootBass -> MaterialTheme.colorScheme.onPrimaryContainer
                      match.isExactMatch -> MaterialTheme.colorScheme.onTertiaryContainer
                      else -> MaterialTheme.colorScheme.onSecondaryContainer
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  )
                }
              }

              val presentLabels = match.matchedIntervals.joinToString(" – ") {
                ChordBookEngine.intervalName(it, match.quality)
              }
              Text(
                text = "${match.quality.fullName} • Tones: $presentLabels",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              FilledTonalIconButton(
                onClick = { onPinMatch(match) },
                modifier = Modifier.size(34.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.BookmarkAdd,
                  contentDescription = "Pin to Songbook",
                  modifier = Modifier.size(16.dp),
                )
              }
              FilledTonalButton(
                onClick = { onJumpToChord(match) },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              ) {
                Text("Voicings", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    if (searchMode == ReverseSearchMode.CHORDS) {
      // 4. Compatible Scales Section (when in Chord Search mode)
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Compatible Scales & Modes (${scaleMatches.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag("reverse_scale_matches_header"),
          )
          Text(
            text = "Reverse Scale Search →",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
              .clickable { onSelectSearchMode(ReverseSearchMode.SCALES) }
              .testTag("jump_to_reverse_scale_search"),
          )
        }
      }

      itemsIndexed(scaleMatches) { idx, scaleMatch ->
        ReverseScaleMatchCard(
          idx = idx,
          scaleMatch = scaleMatch,
          enharmonicPref = enharmonicPref,
          onJumpToScale = { onJumpToScale(scaleMatch) },
        )
      }
    }
  }
}

@Composable
private fun ReverseScaleMatchCard(
  idx: Int,
  scaleMatch: ScaleMatch,
  enharmonicPref: EnharmonicPreference,
  onJumpToScale: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (idx == 0 && scaleMatch.isFullCoverage) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
      } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
      },
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onJumpToScale() }
      .testTag("reverse_scale_match_$idx"),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(3.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = scaleMatch.formatScaleName(enharmonicPref),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = when {
              scaleMatch.isExactScale -> MaterialTheme.colorScheme.primaryContainer
              scaleMatch.isFullCoverage -> MaterialTheme.colorScheme.tertiaryContainer
              else -> MaterialTheme.colorScheme.secondaryContainer
            },
          ) {
            val fitText = when {
              scaleMatch.isExactScale -> "Exact Scale"
              scaleMatch.isFullCoverage -> {
                "100% Fit (${scaleMatch.matchedPitchClasses.size}/${scaleMatch.scaleType.intervals.size})"
              }
              else -> "${(scaleMatch.coverageRatio * 100).roundToInt()}% Fit"
            }
            Text(
              text = fitText,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = when {
                scaleMatch.isExactScale -> MaterialTheme.colorScheme.onPrimaryContainer
                scaleMatch.isFullCoverage -> MaterialTheme.colorScheme.onTertiaryContainer
                else -> MaterialTheme.colorScheme.onSecondaryContainer
              },
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
          }
        }

        val scaleNotes = scaleMatch.scaleType.intervals.mapIndexed { degreeIdx, iv ->
          val pc = (scaleMatch.rootPitchClass + iv).mod(12)
          val noteName = ChordBookEngine.formatPitchClass(
            pc,
            enharmonicPref,
            scaleMatch.rootPitchClass,
          )
          val degLabel = scaleMatch.scaleType.formulaLabels.getOrElse(degreeIdx) {
            ChordBookEngine.intervalName(iv)
          }
          "$noteName ($degLabel)"
        }.joinToString(" – ")

        Text(
          text = scaleNotes,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        val detailLine = when {
          scaleMatch.isExactScale -> {
            "${scaleMatch.scaleType.category.title} • All ${scaleMatch.scaleType.intervals.size} scale tones matched"
          }
          scaleMatch.extraScalePitchClasses.isNotEmpty() && scaleMatch.outsideInputPitchClasses.isEmpty() -> {
            val extraStr = scaleMatch.scalePitchClasses
              .filter { it in scaleMatch.extraScalePitchClasses }
              .joinToString(", ") { pc ->
                ChordBookEngine.formatPitchClass(pc, enharmonicPref, scaleMatch.rootPitchClass)
              }
            "${scaleMatch.scaleType.category.title} • Additional scale tones: $extraStr"
          }
          scaleMatch.outsideInputPitchClasses.isNotEmpty() -> {
            val outsideStr = scaleMatch.outsideInputPitchClasses.sorted().joinToString(", ") { pc ->
              ChordBookEngine.formatPitchClass(pc, enharmonicPref, scaleMatch.rootPitchClass)
            }
            "${scaleMatch.scaleType.category.title} • Outside notes: $outsideStr"
          }
          else -> scaleMatch.scaleType.category.title
        }
        Text(
          text = detailLine,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      FilledTonalButton(
        onClick = onJumpToScale,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        modifier = Modifier.testTag("reverse_scale_view_button_$idx"),
      ) {
        Text("Scale", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
      }
    }
  }
}

// ============================================================================
// TAB 3: SCALES, POSITIONAL BOXES & KEY HARMONIZER
// ============================================================================

@Composable
private fun ScalesAndKeyTabContent(
  activeTuning: ChordTuning,
  capoFret: Int,
  enharmonicPref: EnharmonicPreference,
  dotLabelMode: DotLabelMode,
  scaleRoot: Int,
  onSelectScaleRoot: (Int) -> Unit,
  selectedScaleCategory: ScaleCategory,
  onSelectScaleCategory: (ScaleCategory) -> Unit,
  selectedScaleType: ScaleType,
  onSelectScaleType: (ScaleType) -> Unit,
  selectedBoxNumber: Int,
  onSelectBoxNumber: (Int) -> Unit,
  harmonizeAsSevenths: Boolean,
  onToggleHarmonizeSevenths: () -> Unit,
  onOpenReverseScaleSearch: () -> Unit,
  onJumpToDiatonicChord: (Int, ChordQuality) -> Unit,
  onPinDiatonicChord: (Int, ChordQuality) -> Unit,
  modifier: Modifier = Modifier,
) {
  val scaleBoxes = remember(scaleRoot, activeTuning, capoFret) {
    ChordBookEngine.computeScaleBoxes(scaleRoot, activeTuning, capoFret)
  }
  val activeBox = remember(scaleBoxes, selectedBoxNumber) {
    scaleBoxes.find { it.boxNumber == selectedBoxNumber }
  }
  val diatonicChords = remember(scaleRoot, selectedScaleType) {
    ChordBookEngine.harmonizeScale(scaleRoot, selectedScaleType)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("chord_book_scales_list"),
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // 1. Scale Root & Scale Type Selector Card
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
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Key / Scale Root",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Surface(
              shape = RoundedCornerShape(20.dp),
              color = MaterialTheme.colorScheme.secondaryContainer,
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { onOpenReverseScaleSearch() }
                .testTag("scales_reverse_search_button"),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSecondaryContainer,
                  modifier = Modifier.size(14.dp),
                )
                Text(
                  text = "Reverse Scale Search",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
              }
            }
          }

          PitchClassSelectorBar(
            selectedPitchClass = scaleRoot,
            enharmonicPref = enharmonicPref,
            onSelectPitchClass = onSelectScaleRoot,
            tagPrefix = "scale_root",
          )

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(ScaleCategory.entries) { cat ->
              val isSelected = selectedScaleCategory == cat
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.surfaceContainerHigh
                },
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .clickable { onSelectScaleCategory(cat) }
                  .testTag("scale_category_${cat.name}"),
              ) {
                Text(
                  text = cat.title,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  },
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
              }
            }
          }

          val scalesInCat = remember(selectedScaleCategory) {
            ScaleType.entries.filter { it.category == selectedScaleCategory }
          }
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(scalesInCat) { scale ->
              FilterChip(
                selected = selectedScaleType == scale,
                onClick = { onSelectScaleType(scale) },
                label = { Text(scale.displayName) },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("scale_type_${scale.name}"),
              )
            }
          }
        }
      }
    }

    // 2. Scale Formula & Positional Box Filter
    item {
      val rootName = ChordBookEngine.formatPitchClass(scaleRoot, enharmonicPref, scaleRoot)
      val scaleNotes = selectedScaleType.intervals.joinToString(" – ") { iv ->
        ChordBookEngine.formatPitchClass((scaleRoot + iv).mod(12), enharmonicPref, scaleRoot)
      }
      val formulaLabels = selectedScaleType.formulaLabels.joinToString(" – ")

      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        ),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = "$rootName ${selectedScaleType.displayName}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.testTag("active_scale_title"),
          )
          Text(
            text = "Degrees: $formulaLabels  •  Notes: $scaleNotes",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
      }
    }

    // Positional Box Chips (Full Neck vs Box 1..5)
    item {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        item {
          FilterChip(
            selected = selectedBoxNumber == 0,
            onClick = { onSelectBoxNumber(0) },
            label = { Text("Full Neck (0–15)") },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("scale_box_0"),
          )
        }
        items(scaleBoxes) { box ->
          FilterChip(
            selected = selectedBoxNumber == box.boxNumber,
            onClick = { onSelectBoxNumber(box.boxNumber) },
            label = { Text(box.label) },
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("scale_box_${box.boxNumber}"),
          )
        }
      }
    }

    // 3. Full-Neck Scale Fretboard Diagram
    item {
      ScaleFretboardDiagram(
        tuning = activeTuning,
        capoFret = capoFret,
        rootPitchClass = scaleRoot,
        scaleType = selectedScaleType,
        activeBox = activeBox,
        enharmonicPref = enharmonicPref,
        dotLabelMode = dotLabelMode,
      )
    }

    // 4. Key Harmonization ("Diatonic Chords in Scale")
    if (diatonicChords.isNotEmpty()) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = "Diatonic Harmonization",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Chords built on each scale degree — tap to view voicings or pin",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .clickable { onToggleHarmonizeSevenths() }
              .testTag("harmonize_sevenths_toggle"),
          ) {
            Text(
              text = if (harmonizeAsSevenths) "7th Chords" else "Triads",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
          }
        }
      }

      itemsIndexed(diatonicChords) { idx, diatonic ->
        val quality = if (harmonizeAsSevenths) diatonic.seventhQuality else diatonic.triadQuality
        val symbol = ChordBookEngine.formatChordSymbol(
          rootPitchClass = diatonic.rootPitchClass,
          quality = quality,
          preference = enharmonicPref,
        )
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("diatonic_chord_card_$idx"),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.weight(1f),
            ) {
              // Degree Roman Numeral Badge
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = diatonic.romanNumeral,
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
              }

              Column {
                Text(
                  text = symbol,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = "Degree ${diatonic.degreeNumber} (${diatonic.intervalLabel}) • ${quality.fullName}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              FilledTonalIconButton(
                onClick = { onPinDiatonicChord(diatonic.rootPitchClass, quality) },
                modifier = Modifier
                  .size(34.dp)
                  .testTag("pin_diatonic_chord_$idx"),
              ) {
                Icon(
                  imageVector = Icons.Default.BookmarkAdd,
                  contentDescription = "Pin to Songbook",
                  modifier = Modifier.size(16.dp),
                )
              }
              FilledTonalButton(
                onClick = { onJumpToDiatonicChord(diatonic.rootPitchClass, quality) },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("view_diatonic_chord_$idx"),
              ) {
                Text("Voicings", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}

// ============================================================================
// TAB 4: SONGBOOK / PROGRESSION CREATOR & SCALE EXTRACTOR
// ============================================================================

@Composable
private fun SongbookTabContent(
  activeTuning: ChordTuning,
  capoFret: Int,
  enharmonicPref: EnharmonicPreference,
  dotLabelMode: DotLabelMode,
  progressions: List<ChordProgression>,
  activeProgression: ChordProgression?,
  onSelectProgression: (ChordProgression) -> Unit,
  onRequestNewProgression: () -> Unit,
  onRequestRenameProgression: () -> Unit,
  onDeleteProgression: (ChordProgression) -> Unit,
  onTransposeProgression: (Int) -> Unit,
  onMoveChord: (index: Int, delta: Int) -> Unit,
  onRemoveChord: (String) -> Unit,
  onJumpToChordVoicings: (SavedProgressionChord) -> Unit,
  onJumpToScale: (ScaleMatch) -> Unit,
  modifier: Modifier = Modifier,
) {
  val chords = activeProgression?.chords ?: emptyList()
  val extractedScales = remember(chords) {
    ChordBookEngine.extractScalesForProgression(chords, maxResults = 8)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("chord_book_songbook_list"),
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // 1. Progression Selector & Management Bar
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
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Saved Progressions",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              FilledTonalIconButton(
                onClick = onRequestRenameProgression,
                modifier = Modifier
                  .size(32.dp)
                  .testTag("rename_progression_button"),
              ) {
                Icon(Icons.Default.Edit, contentDescription = "Rename Progression", modifier = Modifier.size(16.dp))
              }
              if (progressions.size > 1 && activeProgression != null) {
                FilledTonalIconButton(
                  onClick = { onDeleteProgression(activeProgression) },
                  modifier = Modifier
                    .size(32.dp)
                    .testTag("delete_progression_button"),
                ) {
                  Icon(Icons.Default.Delete, contentDescription = "Delete Progression", modifier = Modifier.size(16.dp))
                }
              }
              FilledTonalButton(
                onClick = onRequestNewProgression,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("new_progression_button"),
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
              }
            }
          }

          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(progressions) { prog ->
              FilterChip(
                selected = prog.id == activeProgression?.id,
                onClick = { onSelectProgression(prog) },
                label = { Text("${prog.name} (${prog.chords.size})") },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("progression_chip_${prog.id}"),
              )
            }
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

          // Key Transposition Controls
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = "Transpose Progression Key",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "Shifts all chords and updates voicings automatically",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              FilledTonalButton(
                onClick = { onTransposeProgression(-1) },
                enabled = chords.isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("transpose_down_button"),
              ) {
                Text("−1 Semi", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
              }
              FilledTonalButton(
                onClick = { onTransposeProgression(1) },
                enabled = chords.isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("transpose_up_button"),
              ) {
                Text("+1 Semi", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    // 2. Chords in Active Progression
    if (chords.isEmpty()) {
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
              text = "No chords pinned to this progression yet",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Use the Pin button in Chords, Reverse Finder, or Scales & Key to build your progression.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center,
            )
          }
        }
      }
    } else {
      val chunked = chords.chunked(2)
      itemsIndexed(chunked) { rowIdx, pair ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          pair.forEachIndexed { colIdx, savedChord ->
            val chordIdx = rowIdx * 2 + colIdx
            val resolvedVoicing = remember(savedChord, activeTuning, capoFret) {
              val generated = ChordBookEngine.generateChordVoicings(
                rootPitchClass = savedChord.rootPitchClass,
                quality = savedChord.quality,
                tuning = activeTuning,
                capoFret = capoFret,
                slashBassPitchClass = if (savedChord.bassPitchClass != savedChord.rootPitchClass) {
                  savedChord.bassPitchClass
                } else {
                  null
                },
                allowInversions = true,
                maxResults = 24,
              )
              if (savedChord.preferredFrets != null && savedChord.preferredFrets.size == activeTuning.stringCount) {
                generated.find { it.frets == savedChord.preferredFrets } ?: generated.firstOrNull()
              } else {
                generated.firstOrNull()
              }
            }

            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              ),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
              modifier = Modifier
                .weight(1f)
                .testTag("progression_chord_card_$chordIdx"),
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = "${chordIdx + 1}. ${savedChord.formatName(enharmonicPref)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  IconButton(
                    onClick = { onRemoveChord(savedChord.id) },
                    modifier = Modifier.size(26.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Default.Clear,
                      contentDescription = "Remove chord",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(16.dp),
                    )
                  }
                }

                if (resolvedVoicing != null) {
                  ChordDiagramCanvas(
                    voicing = resolvedVoicing,
                    tuning = activeTuning,
                    capoFret = capoFret,
                    enharmonicPref = enharmonicPref,
                    dotLabelMode = dotLabelMode,
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(150.dp)
                      .clickable { onJumpToChordVoicings(savedChord) },
                  )
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  IconButton(
                    onClick = { onMoveChord(chordIdx, -1) },
                    enabled = chordIdx > 0,
                    modifier = Modifier.size(28.dp),
                  ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Move earlier", modifier = Modifier.size(16.dp))
                  }
                  TextButton(
                    onClick = { onJumpToChordVoicings(savedChord) },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                  ) {
                    Text("Voicings", style = MaterialTheme.typography.labelSmall)
                  }
                  IconButton(
                    onClick = { onMoveChord(chordIdx, 1) },
                    enabled = chordIdx < chords.lastIndex,
                    modifier = Modifier.size(28.dp),
                  ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Move later", modifier = Modifier.size(16.dp))
                  }
                }
              }
            }
          }
          if (pair.size == 1) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }

    // 3. Extracted Scales for Improvisation / Soloing over Progression
    if (extractedScales.isNotEmpty()) {
      item {
        Column(
          modifier = Modifier.padding(top = 6.dp),
          verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          Text(
            text = "Scales Compatible with Progression",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag("progression_scales_header"),
          )
          Text(
            text = "Best parent keys and modes extracted from your chord sequence",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      itemsIndexed(extractedScales) { idx, scaleMatch ->
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          ),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onJumpToScale(scaleMatch) }
            .testTag("progression_scale_match_$idx"),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = scaleMatch.formatScaleName(enharmonicPref),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              val notes = scaleMatch.scaleType.intervals.joinToString(" – ") { iv ->
                ChordBookEngine.formatPitchClass(
                  (scaleMatch.rootPitchClass + iv).mod(12),
                  enharmonicPref,
                  scaleMatch.rootPitchClass,
                )
              }
              Text(
                text = notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            Surface(
              shape = RoundedCornerShape(20.dp),
              color = if (scaleMatch.coverageRatio >= 0.99f) {
                MaterialTheme.colorScheme.primaryContainer
              } else {
                MaterialTheme.colorScheme.secondaryContainer
              },
            ) {
              Text(
                text = "${(scaleMatch.coverageRatio * 100).roundToInt()}% Match",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (scaleMatch.coverageRatio >= 0.99f) {
                  MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                  MaterialTheme.colorScheme.onSecondaryContainer
                },
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              )
            }
          }
        }
      }
    }
  }
}

// ============================================================================
// SHARED VISUAL COMPONENTS (DIAGRAMS, FRETBOARDS, PITCH SELECTOR)
// ============================================================================

@Composable
private fun PitchClassSelectorBar(
  selectedPitchClass: Int,
  enharmonicPref: EnharmonicPreference,
  onSelectPitchClass: (Int) -> Unit,
  tagPrefix: String,
) {
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    for (row in 0..1) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        for (col in 0..5) {
          val pc = row * 6 + col
          val isSelected = selectedPitchClass.mod(12) == pc
          val label = ChordBookEngine.formatPitchClass(pc, enharmonicPref, pc)
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.surfaceContainerHigh
            },
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .clickable { onSelectPitchClass(pc) }
              .testTag("${tagPrefix}_$pc"),
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.padding(vertical = 8.dp),
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) {
                  MaterialTheme.colorScheme.onPrimary
                } else {
                  MaterialTheme.colorScheme.onSurface
                },
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PitchClassMultiSelectorBar(
  selectedPitchClasses: Set<Int>,
  highlightedRootPitchClass: Int?,
  enharmonicPref: EnharmonicPreference,
  onTogglePitchClass: (Int) -> Unit,
  tagPrefix: String,
) {
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    for (row in 0..1) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        for (col in 0..5) {
          val pc = row * 6 + col
          val isSelected = pc in selectedPitchClasses
          val isRoot = highlightedRootPitchClass != null && highlightedRootPitchClass.mod(12) == pc
          val label = ChordBookEngine.formatPitchClass(pc, enharmonicPref, pc)
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = when {
              isSelected && isRoot -> MaterialTheme.colorScheme.primary
              isSelected -> MaterialTheme.colorScheme.secondary
              else -> MaterialTheme.colorScheme.surfaceContainerHigh
            },
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .clickable { onTogglePitchClass(pc) }
              .testTag("${tagPrefix}_$pc"),
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier.padding(vertical = 8.dp),
            ) {
              Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = when {
                  isSelected && isRoot -> MaterialTheme.colorScheme.onPrimary
                  isSelected -> MaterialTheme.colorScheme.onSecondary
                  else -> MaterialTheme.colorScheme.onSurface
                },
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun VoicingDiagramCard(
  voicing: ChordVoicing,
  tuning: ChordTuning,
  capoFret: Int,
  enharmonicPref: EnharmonicPreference,
  dotLabelMode: DotLabelMode,
  index: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        val posLabel = if (voicing.baseFret <= 1) "Open / Pos 1" else "Fret ${voicing.baseFret}"
        Text(
          text = "#${index + 1} • $posLabel",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (voicing.isSlashOrInversion) {
          val bassNote = ChordBookEngine.formatPitchClass(
            voicing.bassPitchClass,
            enharmonicPref,
            voicing.rootPitchClass,
          )
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer,
          ) {
            Text(
              text = "/$bassNote",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onTertiaryContainer,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
            )
          }
        }
      }

      ChordDiagramCanvas(
        voicing = voicing,
        tuning = tuning,
        capoFret = capoFret,
        enharmonicPref = enharmonicPref,
        dotLabelMode = dotLabelMode,
        modifier = Modifier
          .fillMaxWidth()
          .height(160.dp),
      )

      Text(
        text = voicing.compactNotation,
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

/**
 * Renders a vertical chord box diagram with nut/baseFret indicator, open/muted markers,
 * curved barre pills, and labeled note dots.
 */
@Composable
fun ChordDiagramCanvas(
  voicing: ChordVoicing,
  tuning: ChordTuning,
  capoFret: Int,
  enharmonicPref: EnharmonicPreference,
  dotLabelMode: DotLabelMode,
  modifier: Modifier = Modifier,
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
  val secondaryColor = MaterialTheme.colorScheme.secondary
  val onSecondaryColor = MaterialTheme.colorScheme.onSecondary
  val onSurfaceColor = MaterialTheme.colorScheme.onSurface
  val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
  val outlineColor = MaterialTheme.colorScheme.outlineVariant

  Canvas(modifier = modifier) {
    val stringCount = tuning.stringCount
    val numFretsShown = 4
    val leftPadding = 28.dp.toPx()
    val rightPadding = 16.dp.toPx()
    val topPadding = 24.dp.toPx()
    val bottomPadding = 18.dp.toPx()

    val gridWidth = (size.width - leftPadding - rightPadding).coerceAtLeast(40f)
    val gridHeight = (size.height - topPadding - bottomPadding).coerceAtLeast(40f)
    val stringSpacing = if (stringCount > 1) gridWidth / (stringCount - 1) else gridWidth
    val fretSpacing = gridHeight / numFretsShown

    // 1. Draw Nut (thick line when baseFret == 1) or baseFret label on the left
    if (voicing.baseFret <= 1) {
      drawLine(
        color = onSurfaceColor,
        start = Offset(leftPadding, topPadding),
        end = Offset(leftPadding + gridWidth, topPadding),
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round,
      )
    } else {
      val labelPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = onSurfaceVariantColor.toArgb()
        textSize = 11.sp.toPx()
        textAlign = android.graphics.Paint.Align.RIGHT
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
      }
      drawContext.canvas.nativeCanvas.drawText(
        "${voicing.baseFret}fr",
        leftPadding - 6.dp.toPx(),
        topPadding + fretSpacing * 0.65f,
        labelPaint,
      )
    }

    // 2. Draw horizontal fret wires
    for (f in 0..numFretsShown) {
      val y = topPadding + f * fretSpacing
      drawLine(
        color = outlineColor,
        start = Offset(leftPadding, y),
        end = Offset(leftPadding + gridWidth, y),
        strokeWidth = 1.2.dp.toPx(),
      )
    }

    // 3. Draw vertical string lines
    for (s in 0 until stringCount) {
      val x = leftPadding + s * stringSpacing
      drawLine(
        color = onSurfaceVariantColor.copy(alpha = 0.65f),
        start = Offset(x, topPadding),
        end = Offset(x, topPadding + gridHeight),
        strokeWidth = (1.8f - (s.toFloat() / stringCount) * 0.7f).dp.toPx(),
      )
    }

    // 4. Draw Open (O) and Muted (X) indicators above nut
    val headerY = topPadding - 10.dp.toPx()
    val markerRadius = 4.5.dp.toPx()
    for (s in 0 until stringCount) {
      val x = leftPadding + s * stringSpacing
      val fret = voicing.frets.getOrElse(s) { -1 }
      if (fret == 0) {
        drawCircle(
          color = primaryColor,
          radius = markerRadius,
          center = Offset(x, headerY),
          style = Stroke(width = 1.6.dp.toPx()),
        )
      } else if (fret < 0) {
        val d = markerRadius * 0.85f
        drawLine(
          color = onSurfaceVariantColor,
          start = Offset(x - d, headerY - d),
          end = Offset(x + d, headerY + d),
          strokeWidth = 1.6.dp.toPx(),
          cap = StrokeCap.Round,
        )
        drawLine(
          color = onSurfaceVariantColor,
          start = Offset(x - d, headerY + d),
          end = Offset(x + d, headerY - d),
          strokeWidth = 1.6.dp.toPx(),
          cap = StrokeCap.Round,
        )
      }
    }

    // 5. Draw Barre pills
    val dotRadius = minOf(stringSpacing * 0.38f, fretSpacing * 0.36f).coerceAtMost(11.dp.toPx())
    for (barre in voicing.barres) {
      val relFret = barre.fret - voicing.baseFret + 1
      if (relFret in 1..numFretsShown) {
        val startX = leftPadding + barre.fromString * stringSpacing
        val endX = leftPadding + barre.toString * stringSpacing
        val centerY = topPadding + (relFret - 0.5f) * fretSpacing
        val barHeight = dotRadius * 1.45f
        drawRoundRect(
          color = secondaryColor.copy(alpha = 0.85f),
          topLeft = Offset(startX - dotRadius, centerY - barHeight / 2f),
          size = Size((endX - startX) + dotRadius * 2f, barHeight),
          cornerRadius = CornerRadius(barHeight / 2f, barHeight / 2f),
        )
      }
    }

    // 6. Draw Fretted Note Dots and bottom labels
    val dotTextPaint = android.graphics.Paint().apply {
      isAntiAlias = true
      textSize = (dotRadius * 1.05f).coerceAtLeast(8.sp.toPx())
      textAlign = android.graphics.Paint.Align.CENTER
      typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    val bottomTextPaint = android.graphics.Paint().apply {
      isAntiAlias = true
      color = onSurfaceVariantColor.toArgb()
      textSize = 9.sp.toPx()
      textAlign = android.graphics.Paint.Align.CENTER
    }

    for (s in 0 until stringCount) {
      val x = leftPadding + s * stringSpacing
      val fret = voicing.frets.getOrElse(s) { -1 }
      val finger = voicing.fingers.getOrElse(s) { 0 }

      if (fret >= 0) {
        val midi = tuning.effectiveMidi(s, capoFret) + fret
        val pc = midi.mod(12)
        val isRoot = pc == voicing.rootPitchClass.mod(12)
        val intervalStr = ChordBookEngine.intervalName(
          (pc - voicing.rootPitchClass).mod(12),
          voicing.quality,
        )
        val noteStr = ChordBookEngine.formatPitchClass(pc, enharmonicPref, voicing.rootPitchClass)

        // Bottom note/interval caption under each sounded string
        val bottomCaption = if (dotLabelMode == DotLabelMode.NOTES) intervalStr else noteStr
        drawContext.canvas.nativeCanvas.drawText(
          bottomCaption,
          x,
          topPadding + gridHeight + 13.dp.toPx(),
          bottomTextPaint,
        )

        if (fret > 0) {
          val relFret = fret - voicing.baseFret + 1
          if (relFret in 1..numFretsShown) {
            val centerY = topPadding + (relFret - 0.5f) * fretSpacing
            val circleColor = if (isRoot) primaryColor else secondaryColor
            val textColor = if (isRoot) onPrimaryColor else onSecondaryColor

            drawCircle(
              color = circleColor,
              radius = dotRadius,
              center = Offset(x, centerY),
            )

            val dotLabel = when (dotLabelMode) {
              DotLabelMode.FINGERS -> if (finger > 0) finger.toString() else intervalStr
              DotLabelMode.NOTES -> noteStr
              DotLabelMode.INTERVALS -> intervalStr
            }
            dotTextPaint.color = textColor.toArgb()
            val textY = centerY - (dotTextPaint.descent() + dotTextPaint.ascent()) / 2f
            drawContext.canvas.nativeCanvas.drawText(dotLabel, x, textY, dotTextPaint)
          }
        }
      }
    }
  }
}

/**
 * Interactive fretboard for the Reverse Chord & Scale Finder tab.
 */
@Composable
private fun InteractiveReverseFretboard(
  tuning: ChordTuning,
  capoFret: Int,
  enharmonicPref: EnharmonicPreference,
  selectedPairs: Set<Pair<Int, Int>>,
  selectedPitchClasses: Set<Int> = emptySet(),
  highlightAllMatchingPitchClasses: Boolean = false,
  lowestPitchClass: Int?,
  onToggleFret: (stringIdx: Int, fret: Int) -> Unit,
) {
  val maxFret = 12
  val primaryColor = MaterialTheme.colorScheme.primary
  val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
  val secondaryColor = MaterialTheme.colorScheme.secondary
  val onSecondaryColor = MaterialTheme.colorScheme.onSecondary
  val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
  val onSurfaceColor = MaterialTheme.colorScheme.onSurface
  val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
  val outlineColor = MaterialTheme.colorScheme.outlineVariant

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = surfaceVariantColor.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.5f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      val scrollState = rememberScrollState()
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(scrollState),
      ) {
        val stringCount = tuning.stringCount
        val canvasWidth = 620.dp
        val canvasHeight = (stringCount * 32 + 34).dp

        Canvas(
          modifier = Modifier
            .width(canvasWidth)
            .height(canvasHeight)
            .testTag("reverse_fretboard_canvas")
            .pointerInput(stringCount, maxFret) {
              detectTapGestures { offset ->
                val leftPad = 36.dp.toPx()
                val topPad = 24.dp.toPx()
                val bottomPad = 12.dp.toPx()
                val usableHeight = size.height - topPad - bottomPad
                val stringStep = if (stringCount > 1) usableHeight / (stringCount - 1) else usableHeight
                val fretSlotWidth = (size.width - leftPad) / (maxFret + 1)

                // Display high string at top (row 0 = stringCount - 1) down to low string at bottom
                val tappedRow = ((offset.y - topPad + stringStep / 2f) / stringStep).toInt()
                  .coerceIn(0, stringCount - 1)
                val stringIdx = (stringCount - 1) - tappedRow
                val fret = ((offset.x - leftPad) / fretSlotWidth).toInt().coerceIn(0, maxFret)
                onToggleFret(stringIdx, fret)
              }
            },
        ) {
          val leftPad = 36.dp.toPx()
          val topPad = 24.dp.toPx()
          val bottomPad = 12.dp.toPx()
          val usableHeight = size.height - topPad - bottomPad
          val stringStep = if (stringCount > 1) usableHeight / (stringCount - 1) else usableHeight
          val fretSlotWidth = (size.width - leftPad) / (maxFret + 1)

          val headerPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = onSurfaceVariantColor.toArgb()
            textSize = 10.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
          }

          // Fret numbers & inlay markers
          val inlayFrets = setOf(3, 5, 7, 9, 12)
          for (f in 0..maxFret) {
            val centerX = leftPad + (f + 0.5f) * fretSlotWidth
            drawContext.canvas.nativeCanvas.drawText(
              if (f == 0) "Open" else f.toString(),
              centerX,
              14.dp.toPx(),
              headerPaint,
            )

            // Fret wire at right edge of slot
            val wireX = leftPad + (f + 1) * fretSlotWidth
            drawLine(
              color = if (f == 0) onSurfaceColor else outlineColor,
              start = Offset(wireX, topPad - 4.dp.toPx()),
              end = Offset(wireX, topPad + usableHeight + 4.dp.toPx()),
              strokeWidth = if (f == 0) 3.5.dp.toPx() else 1.2.dp.toPx(),
            )

            if (f in inlayFrets) {
              val midY = topPad + usableHeight / 2f
              if (f == 12) {
                drawCircle(
                  color = outlineColor.copy(alpha = 0.45f),
                  radius = 4.dp.toPx(),
                  center = Offset(centerX, midY - stringStep),
                )
                drawCircle(
                  color = outlineColor.copy(alpha = 0.45f),
                  radius = 4.dp.toPx(),
                  center = Offset(centerX, midY + stringStep),
                )
              } else {
                drawCircle(
                  color = outlineColor.copy(alpha = 0.45f),
                  radius = 4.dp.toPx(),
                  center = Offset(centerX, midY),
                )
              }
            }
          }

          val dotTextPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = 10.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
          }

          // Strings (high string on top row, low string on bottom row)
          for (row in 0 until stringCount) {
            val s = (stringCount - 1) - row
            val y = topPad + row * stringStep
            val openMidi = tuning.effectiveMidi(s, capoFret)
            val openLabel = ChordBookEngine.formatPitchClass(openMidi.mod(12), enharmonicPref)

            drawContext.canvas.nativeCanvas.drawText(
              openLabel,
              18.dp.toPx(),
              y + 4.dp.toPx(),
              headerPaint,
            )

            drawLine(
              color = onSurfaceVariantColor.copy(alpha = 0.65f),
              start = Offset(leftPad, y),
              end = Offset(size.width, y),
              strokeWidth = (1.2f + (row.toFloat() / stringCount) * 1.0f).dp.toPx(),
            )

            for (f in 0..maxFret) {
              val isExplicitlySelected = (s to f) in selectedPairs
              val midi = openMidi + f
              val pc = midi.mod(12)
              val isPitchClassMatch = highlightAllMatchingPitchClasses && pc in selectedPitchClasses

              if (isExplicitlySelected || isPitchClassMatch) {
                val centerX = leftPad + (f + 0.5f) * fretSlotWidth
                val isBass = lowestPitchClass != null && pc == lowestPitchClass
                val noteStr = ChordBookEngine.formatPitchClass(pc, enharmonicPref)
                val alpha = if (isExplicitlySelected) 1f else 0.42f

                drawCircle(
                  color = (if (isBass) primaryColor else secondaryColor).copy(alpha = alpha),
                  radius = if (isExplicitlySelected) 11.dp.toPx() else 9.5.dp.toPx(),
                  center = Offset(centerX, y),
                )
                dotTextPaint.color = (if (isBass) onPrimaryColor else onSecondaryColor)
                  .copy(alpha = if (isExplicitlySelected) 1f else 0.9f)
                  .toArgb()
                val textY = y - (dotTextPaint.descent() + dotTextPaint.ascent()) / 2f
                drawContext.canvas.nativeCanvas.drawText(noteStr, centerX, textY, dotTextPaint)
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Full-neck scale diagram for the Scales & Key Harmonizer tab.
 */
@Composable
private fun ScaleFretboardDiagram(
  tuning: ChordTuning,
  capoFret: Int,
  rootPitchClass: Int,
  scaleType: ScaleType,
  activeBox: ScaleBoxPosition?,
  enharmonicPref: EnharmonicPreference,
  dotLabelMode: DotLabelMode,
) {
  val maxFret = 15
  val primaryColor = MaterialTheme.colorScheme.primary
  val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
  val tertiaryColor = MaterialTheme.colorScheme.tertiary
  val onTertiaryColor = MaterialTheme.colorScheme.onTertiary
  val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer
  val onSecondaryContainerColor = MaterialTheme.colorScheme.onSecondaryContainer
  val onSurfaceColor = MaterialTheme.colorScheme.onSurface
  val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
  val outlineColor = MaterialTheme.colorScheme.outlineVariant

  val scaleIntervals = remember(scaleType) { scaleType.intervals.map { it.mod(12) }.toSet() }
  val chordToneIntervals = remember { setOf(0, 3, 4, 7, 10, 11) }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ),
    border = BorderStroke(1.dp, outlineColor.copy(alpha = 0.5f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    val scrollState = rememberScrollState()
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp)
        .horizontalScroll(scrollState),
    ) {
      val stringCount = tuning.stringCount
      val canvasWidth = 740.dp
      val canvasHeight = (stringCount * 30 + 36).dp

      Canvas(
        modifier = Modifier
          .width(canvasWidth)
          .height(canvasHeight)
          .testTag("scale_fretboard_canvas"),
      ) {
        val leftPad = 34.dp.toPx()
        val topPad = 24.dp.toPx()
        val bottomPad = 12.dp.toPx()
        val usableHeight = size.height - topPad - bottomPad
        val stringStep = if (stringCount > 1) usableHeight / (stringCount - 1) else usableHeight
        val fretSlotWidth = (size.width - leftPad) / (maxFret + 1)

        // Highlight active positional box if selected
        if (activeBox != null) {
          val boxStartX = leftPad + activeBox.startFret * fretSlotWidth
          val boxWidth = (activeBox.endFret - activeBox.startFret + 1) * fretSlotWidth
          drawRoundRect(
            color = primaryColor.copy(alpha = 0.10f),
            topLeft = Offset(boxStartX, topPad - 8.dp.toPx()),
            size = Size(boxWidth, usableHeight + 16.dp.toPx()),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
          )
        }

        val headerPaint = android.graphics.Paint().apply {
          isAntiAlias = true
          color = onSurfaceVariantColor.toArgb()
          textSize = 10.sp.toPx()
          textAlign = android.graphics.Paint.Align.CENTER
          typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }

        val inlayFrets = setOf(3, 5, 7, 9, 12, 15)
        for (f in 0..maxFret) {
          val centerX = leftPad + (f + 0.5f) * fretSlotWidth
          drawContext.canvas.nativeCanvas.drawText(
            if (f == 0) "0" else f.toString(),
            centerX,
            14.dp.toPx(),
            headerPaint,
          )
          val wireX = leftPad + (f + 1) * fretSlotWidth
          drawLine(
            color = if (f == 0) onSurfaceColor else outlineColor,
            start = Offset(wireX, topPad - 4.dp.toPx()),
            end = Offset(wireX, topPad + usableHeight + 4.dp.toPx()),
            strokeWidth = if (f == 0) 3.5.dp.toPx() else 1.2.dp.toPx(),
          )
          if (f in inlayFrets) {
            val midY = topPad + usableHeight / 2f
            drawCircle(
              color = outlineColor.copy(alpha = 0.4f),
              radius = 3.5.dp.toPx(),
              center = Offset(centerX, midY),
            )
          }
        }

        val dotTextPaint = android.graphics.Paint().apply {
          isAntiAlias = true
          textSize = 9.5.sp.toPx()
          textAlign = android.graphics.Paint.Align.CENTER
          typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }

        for (row in 0 until stringCount) {
          val s = (stringCount - 1) - row
          val y = topPad + row * stringStep
          val openMidi = tuning.effectiveMidi(s, capoFret)
          val openLabel = ChordBookEngine.formatPitchClass(openMidi.mod(12), enharmonicPref, rootPitchClass)

          drawContext.canvas.nativeCanvas.drawText(openLabel, 16.dp.toPx(), y + 4.dp.toPx(), headerPaint)

          drawLine(
            color = onSurfaceVariantColor.copy(alpha = 0.6f),
            start = Offset(leftPad, y),
            end = Offset(size.width, y),
            strokeWidth = (1.2f + (row.toFloat() / stringCount) * 1.0f).dp.toPx(),
          )

          for (f in 0..maxFret) {
            val inBox = activeBox == null || f in activeBox.startFret..activeBox.endFret
            val midi = openMidi + f
            val pc = midi.mod(12)
            val semi = (pc - rootPitchClass).mod(12)
            if (semi in scaleIntervals) {
              val centerX = leftPad + (f + 0.5f) * fretSlotWidth
              val isRoot = semi == 0
              val isChordTone = semi in chordToneIntervals
              val alpha = if (inBox) 1f else 0.25f

              val bgColor = when {
                isRoot -> primaryColor.copy(alpha = alpha)
                isChordTone -> tertiaryColor.copy(alpha = alpha)
                else -> secondaryContainerColor.copy(alpha = alpha)
              }
              val fgColor = when {
                isRoot -> onPrimaryColor.copy(alpha = alpha)
                isChordTone -> onTertiaryColor.copy(alpha = alpha)
                else -> onSecondaryContainerColor.copy(alpha = alpha)
              }

              drawCircle(
                color = bgColor,
                radius = if (isRoot) 11.dp.toPx() else 9.5.dp.toPx(),
                center = Offset(centerX, y),
              )

              val degreeIdx = scaleType.intervals.indexOf(semi)
              val label = if (dotLabelMode == DotLabelMode.NOTES) {
                ChordBookEngine.formatPitchClass(pc, enharmonicPref, rootPitchClass)
              } else {
                scaleType.formulaLabels.getOrElse(degreeIdx) { ChordBookEngine.intervalName(semi) }
              }

              dotTextPaint.color = fgColor.toArgb()
              val textY = y - (dotTextPaint.descent() + dotTextPaint.ascent()) / 2f
              drawContext.canvas.nativeCanvas.drawText(label, centerX, textY, dotTextPaint)
            }
          }
        }
      }
    }
  }
}

// ============================================================================
// DIALOGS (DESIGN_LANGUAGE.md COMPLIANT: 28.dp, ICON BADGE, CONTEXT CARD, SLIM SLIDER)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InstrumentAndTuningDialog(
  allTunings: List<ChordTuning>,
  activeTuning: ChordTuning,
  capoFret: Int,
  enharmonicPref: EnharmonicPreference,
  onSelectTuning: (ChordTuning) -> Unit,
  onCapoChange: (Int) -> Unit,
  onSaveCustomTuning: (ChordTuning) -> Unit,
  onDeleteCustomTuning: (ChordTuning) -> Unit,
  onDismiss: () -> Unit,
) {
  var selectedInstrument by remember { mutableStateOf(activeTuning.instrument) }
  var editingCustom by remember { mutableStateOf(false) }
  var customName by remember { mutableStateOf("${activeTuning.instrument.shortName} Custom") }
  val customMidi = remember(activeTuning) {
    mutableStateListOf<Int>().apply { addAll(activeTuning.openStringMidi) }
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
            imageVector = Icons.Default.Tune,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "Instrument, Tuning & Capo",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Configure strings, alternate tunings, and capo position",
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
        // Context Preview Card
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
              text = "${activeTuning.instrument.displayName} • ${activeTuning.name}",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            val notesOctaves = activeTuning.formattedNotesWithOctaves(capoFret, enharmonicPref)
              .joinToString(" – ")
            Text(
              text = "Effective Pitch: $notesOctaves" + if (capoFret > 0) " (Capo $capoFret)" else "",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        // Capo SlimDotSlider (0..12)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = if (capoFret == 0) "Capo: None (Open Nut)" else "Capo: Fret $capoFret",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            if (capoFret > 0) {
              Text(
                text = "Reset",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onCapoChange(0) },
              )
            }
          }

          val sliderColors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
          )
          Slider(
            value = capoFret.toFloat(),
            onValueChange = { onCapoChange(it.roundToInt().coerceIn(0, 12)) },
            valueRange = 0f..12f,
            steps = 11,
            colors = sliderColors,
            modifier = Modifier.testTag("capo_fret_slider"),
            thumb = {
              Box(
                modifier = Modifier
                  .size(16.dp)
                  .shadow(2.dp, CircleShape)
                  .background(MaterialTheme.colorScheme.primary, CircleShape),
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

        // Instrument Family Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          items(InstrumentFamily.entries) { family ->
            FilterChip(
              selected = selectedInstrument == family,
              onClick = {
                selectedInstrument = family
                val firstForInst = allTunings.firstOrNull { it.instrument == family }
                if (firstForInst != null) {
                  onSelectTuning(firstForInst)
                  customMidi.clear()
                  customMidi.addAll(firstForInst.openStringMidi)
                }
              },
              label = { Text(family.shortName) },
              shape = RoundedCornerShape(20.dp),
              modifier = Modifier.testTag("instrument_family_${family.name}"),
            )
          }
        }

        // Toggle between Preset List and Custom Tuning Builder
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = if (editingCustom) "Custom String Pitches" else "Tunings for ${selectedInstrument.shortName}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          TextButton(
            onClick = {
              editingCustom = !editingCustom
              if (editingCustom) {
                customMidi.clear()
                customMidi.addAll(activeTuning.openStringMidi)
                customName = "${activeTuning.instrument.shortName} Custom"
              }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("toggle_custom_tuning_editor"),
          ) {
            Text(if (editingCustom) "Back to Presets" else "+ Custom Tuning")
          }
        }

        if (editingCustom) {
          OutlinedTextField(
            value = customName,
            onValueChange = { customName = it },
            label = { Text("Custom tuning name") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
          )
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            customMidi.indices.forEach { sIdx ->
              val midiVal = customMidi[sIdx]
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "String ${customMidi.size - sIdx}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  FilledTonalIconButton(
                    onClick = { customMidi[sIdx] = (midiVal - 1).coerceAtLeast(20) },
                    modifier = Modifier.size(28.dp),
                  ) {
                    Icon(Icons.Default.Remove, contentDescription = "Lower semitone", modifier = Modifier.size(14.dp))
                  }
                  Text(
                    text = ChordBookEngine.formatMidiNote(midiVal, enharmonicPref),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  FilledTonalIconButton(
                    onClick = { customMidi[sIdx] = (midiVal + 1).coerceAtMost(88) },
                    modifier = Modifier.size(28.dp),
                  ) {
                    Icon(Icons.Default.Add, contentDescription = "Raise semitone", modifier = Modifier.size(14.dp))
                  }
                }
              }
            }
          }
          Button(
            onClick = {
              val created = ChordTuning(
                id = "custom_${UUID.randomUUID().toString().take(8)}",
                name = customName.trim().ifBlank { "Custom Tuning" },
                instrument = selectedInstrument,
                openStringMidi = customMidi.toList(),
                isCustom = true,
              )
              onSaveCustomTuning(created)
              editingCustom = false
            },
            enabled = customName.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save & Activate Custom Tuning")
          }
        } else {
          val filteredTunings = remember(allTunings, selectedInstrument) {
            allTunings.filter { it.instrument == selectedInstrument }
          }
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 210.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            items(filteredTunings) { tuning ->
              val isSelected = tuning.id == activeTuning.id
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                  } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                  },
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { onSelectTuning(tuning) }
                  .testTag("tuning_option_${tuning.id}"),
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = tuning.name,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                      color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                      } else {
                        MaterialTheme.colorScheme.onSurface
                      },
                    )
                    Text(
                      text = tuning.formattedNotesWithOctaves(0, enharmonicPref).joinToString(" "),
                      style = MaterialTheme.typography.labelSmall,
                      color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                      } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                      },
                    )
                  }
                  if (tuning.isCustom) {
                    IconButton(
                      onClick = { onDeleteCustomTuning(tuning) },
                      modifier = Modifier.size(28.dp),
                    ) {
                      Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete custom tuning",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp),
                      )
                    }
                  } else if (isSelected) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.onPrimaryContainer,
                      modifier = Modifier.size(18.dp),
                    )
                  }
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
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Done")
      }
    },
  )
}

@Composable
private fun VoicingInspectorDialog(
  voicing: ChordVoicing,
  tuning: ChordTuning,
  capoFret: Int,
  enharmonicPref: EnharmonicPreference,
  dotLabelMode: DotLabelMode,
  onPinVoicing: () -> Unit,
  onDismiss: () -> Unit,
) {
  val chordTitle = ChordBookEngine.formatChordSymbol(
    rootPitchClass = voicing.rootPitchClass,
    quality = voicing.quality,
    bassPitchClass = if (voicing.isSlashOrInversion) voicing.bassPitchClass else null,
    preference = enharmonicPref,
  )

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
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = "$chordTitle Voicing",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${voicing.quality.fullName} • Shape ${voicing.compactNotation}",
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
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          ChordDiagramCanvas(
            voicing = voicing,
            tuning = tuning,
            capoFret = capoFret,
            enharmonicPref = enharmonicPref,
            dotLabelMode = dotLabelMode,
            modifier = Modifier
              .fillMaxWidth()
              .height(190.dp)
              .padding(12.dp),
          )
        }

        // String-by-string anatomical breakdown
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "String-by-String Breakdown",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          for (s in 0 until tuning.stringCount) {
            val fret = voicing.frets.getOrElse(s) { -1 }
            val finger = voicing.fingers.getOrElse(s) { 0 }
            val stringNumber = tuning.stringCount - s
            val openNote = ChordBookEngine.formatMidiNote(tuning.effectiveMidi(s, capoFret), enharmonicPref)

            val detail = if (fret < 0) {
              "Muted (X)"
            } else {
              val midi = tuning.effectiveMidi(s, capoFret) + fret
              val noteName = ChordBookEngine.formatMidiNote(midi, enharmonicPref)
              val interval = ChordBookEngine.intervalName(
                (midi.mod(12) - voicing.rootPitchClass).mod(12),
                voicing.quality,
              )
              val fingerStr = if (fret == 0) "Open" else "Fret $fret (Finger $finger)"
              "$noteName • Interval $interval • $fingerStr"
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = "Str $stringNumber ($openNote)",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (fret >= 0) FontWeight.SemiBold else FontWeight.Normal,
                color = if (fret >= 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onPinVoicing,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("inspector_pin_voicing_button"),
      ) {
        Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Pin Shape to Songbook")
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

@Composable
private fun ProgressionNameDialog(
  title: String,
  subtitle: String,
  initialName: String,
  contextLabel: String,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  var textFieldValue by remember {
    mutableStateOf(
      TextFieldValue(
        text = initialName,
        selection = TextRange(0, initialName.length),
      ),
    )
  }
  val focusRequester = remember { FocusRequester() }
  val canConfirm = textFieldValue.text.trim().isNotEmpty()

  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
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
            imageVector = Icons.Default.LibraryMusic,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
          )
        }
        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = subtitle,
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
          Text(
            text = contextLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(12.dp),
          )
        }

        OutlinedTextField(
          value = textFieldValue,
          onValueChange = { textFieldValue = it },
          label = { Text("Progression title") },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          trailingIcon = {
            if (textFieldValue.text.isNotEmpty()) {
              IconButton(onClick = { textFieldValue = TextFieldValue("") }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear")
              }
            }
          },
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(
            onDone = {
              if (canConfirm) onConfirm(textFieldValue.text.trim())
            },
          ),
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(textFieldValue.text.trim()) },
        enabled = canConfirm,
        shape = RoundedCornerShape(12.dp),
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Confirm")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(12.dp),
      ) {
        Text("Cancel")
      }
    },
  )
}

@Preview(showBackground = true)
@Composable
private fun ChordBookScreenPreview() {
  ToolboxTheme {
    ChordBookScreen()
  }
}
