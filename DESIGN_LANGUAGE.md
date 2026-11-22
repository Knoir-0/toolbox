# Toolbox UI Design Language & Guidelines 🎨

This document defines the core design language, visual standards, component specifications, and UI conventions for the **Toolbox** Android application. 

All AI agents and developers implementing new tools, screens, dialogs, or UI enhancements must follow these principles to maintain a cohesive, elegant, and tactile user experience across the application.

---

## 1. Core Design Philosophy

1. **Material Design 3 (Expressive & Modern)**: Built entirely on Material 3 (`androidx.compose.material3`), leveraging dynamic color palettes, tonal elevation, and smooth micro-interactions.
2. **Tactile & Responsive**: Every action produces immediate visual or haptic feedback (`HapticFeedbackType.LongPress`). Transitions and disclosures use spring or easing animations.
3. **Information Density with Breathing Room**: Generous touch targets (minimum 44×44 dp for interactive elements), balanced with tight, clean typographic hierarchy and clear metadata separation.
4. **Offline & Self-Contained**: Clear status indicators, zero ambiguous spinners or blocking loaders for local tasks.

---

## 2. Color System & Theming

### 2.1 Dynamic Material You, Color Tokens & Background Textures
Toolbox supports system dynamic theming (Android 12+ / API 31+) and custom user-selected themes in **Settings**, falling back to a curated purple-lavender palette (`#8E66A6`) on older systems. The **Accent Color** is also configurable in Settings: it defaults to **Auto (Analogous +30°)**, and supports automatic color harmonies (**Analogous**, **Tints & Shades**, **Complementary**, **Triadic**, and **Split-Complementary**) as well as preset swatches and custom hex codes. The **Background Texture** collapsible section in Settings defaults to **None** and allows users to apply procedural background textures (**Fuzzy**, **Fade**, **Concentric**, **Halftone**, **Topography**, **Aurora**, **Starfield**, **Sunburst**, **Honeycomb**, **Blueprint**, **Waves**, **Confetti**, and **Circuit**) across all tool screens via `Modifier.toolboxScreenBackground(...)`.

> **CRITICAL RULE**: Never hardcode hex values (e.g., `#FFFFFF`, `#1E1E1E`, `#000000`) for surfaces, text, or borders. Always reference `MaterialTheme.colorScheme` tokens so dark mode, light mode, and dynamic palette switches work seamlessly.

| Token | Usage |
| :--- | :--- |
| `colorScheme.background` | Overall screen scaffold background |
| `colorScheme.surface` | Top header bars, bottom sheets, cards |
| `colorScheme.surfaceContainer` | Floating menus, bottom drawers |
| `colorScheme.surfaceContainerHigh` | **Dialogs** (`AlertDialog`), popups, elevated panels |
| `colorScheme.surfaceVariant` | Unfocused text field backgrounds, item card backgrounds (`copy(alpha = 0.45f)`) |
| `colorScheme.primary` | Active icons, progress indicators, primary buttons, slider tracks |
| `colorScheme.primaryContainer` | Header icon badge containers, active pill containers, FAB containers |
| `colorScheme.onPrimaryContainer` | Icons/text rendered inside a `primaryContainer` |
| `colorScheme.onSurface` | Primary titles, primary item text, field labels |
| `colorScheme.onSurfaceVariant` | Subtitles, timestamps, item counts, secondary metadata, inactive icons |
| `colorScheme.outlineVariant` | Subtle separators, dividers (`copy(alpha = 0.5f)`), container outlines |
| `colorScheme.tertiary` / `tertiaryContainer` | Accent highlights & actions (Calculator action keys, Metronome downbeat, Guitar Tuner in-tune needle, Ruler calipers, Unit Converter target well, dice roll badges) |
| `colorScheme.error` / `errorContainer` | Validation errors, delete confirmation buttons, destructive actions |

### 2.2 Elevation & Depth
- **Tonal Elevation over Drop Shadows**: Material 3 replaces heavy drop shadows with tonal elevation:
  - Top Bars / Headers: `tonalElevation = 1.dp`
  - Quick-Add Bars / Action Bars: `tonalElevation = 3.dp`
  - Dialogs & Sheets: `tonalElevation = 6.dp`
- **Subtle Borders**: Use `BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))` for clean card definitions on low-contrast screens.

---

## 3. Typography & Hierarchy

Toolbox uses Material 3 typography with specific weight and color rules:

| Role | Style | Weight | Color Token | Common Use |
| :--- | :--- | :--- | :--- | :--- |
| **Screen / Tool Title** | `typography.titleLarge` | `FontWeight.Bold` | `colorScheme.onSurface` | Top bar title ("Files", "To-Do", "Calculator") |
| **Dialog Title** | `typography.titleLarge` | `FontWeight.Bold` | `colorScheme.onSurface` | Dialog header title |
| **Header Subtext** | `typography.bodySmall` | `FontWeight.Normal` | `colorScheme.onSurfaceVariant` | Device storage ("45.2 GB used of 128 GB"), task counter |
| **Card / Item Title** | `typography.bodyLarge` / `bodyMedium` | `FontWeight.SemiBold` | `colorScheme.onSurface` | File name, task title, tool name |
| **Metadata & Subtext** | `typography.bodySmall` / `labelSmall` | `FontWeight.Normal` | `colorScheme.onSurfaceVariant` | `"$childCount items • $sizeFormatted • $timestamp"` |
| **Badge / Pill Label** | `typography.labelSmall` / `labelMedium` | `FontWeight.Medium` | `colorScheme.onPrimaryContainer` / `onSurface` | Storage volume pills, extension restoration badges |
| **Numerical / Technical** | `typography.bodyMedium` | `FontFamily.Monospace` | `colorScheme.onSurface` | MD5 checksums, exact byte counts, hex codes |

---

## 4. Shapes & Geometry Standards

Consistent rounding gives Toolbox its signature modern, friendly, and cohesive aesthetic:

- **Dialogs**: `RoundedCornerShape(28.dp)` (`MaterialTheme.shapes.extraLarge`)
- **Bottom Sheets / Drawers**: `RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)`
- **Home Screen AppWidgets (All Widgets)**: `16.dp` corner radius (`16f * density` in rendered background bitmaps), with `android:id="@android:id/background"` and `android:clipToOutline="true"` on the full-bleed `16.dp`-rounded root `FrameLayout` (so Android 12+ launchers opt out of `28.dp` `system_app_widget_background_radius` clipping), and the `1.dp` border stroke inset by `strokeInset = max(borderWidthPx * 1.25f, 1.5f)` with corner radius `cornerRadiusPx + borderWidthPx` so the outline never appears cut off at the rounded corners
- **Device & Category Pills / Filter Chips**: `RoundedCornerShape(20.dp)` (enforced via `ToolboxPillNavigationItem`)
- **Item Cards (Lists & Grids)**: `RoundedCornerShape(12.dp)` to `RoundedCornerShape(14.dp)`
- **Text Input Fields (`OutlinedTextField`)**: `RoundedCornerShape(14.dp)` or `RoundedCornerShape(16.dp)`
- **Buttons (Dialog Confirm / Dismiss & Action Buttons)**: `CircleShape` (fully rounded pill shape, matching the rest of the app's buttons)
- **Floating Action Buttons (FAB & Mini-FAB)**: `CircleShape`
- **Header Icon Badges**: `CircleShape` (fixed `40.dp` container in `colorScheme.primaryContainer` with a `20.dp` icon tinted `colorScheme.primary`)
- **Sliders & Trackbars (`SlimDotSlider`)**: Slim track (`4.dp` to `6.dp` height) with a circular dot drag handle (`CircleShape`, `16.dp` to `20.dp`). Never use the default thick Material 3 vertical bar thumb.

---

## 5. Standard Component Patterns

### 5.1 Tool Header / Top Bar (`ToolboxTopBar`)
All tools with a top header bar (including **Settings**, **To Do**, **Calculator**, and **Unit Converter**, while self-contained full-canvas utilities like **Dice Roller**, **Metronome**, **Color Picker**, **Ruler**, and **Camera** intentionally omit a top bar) must use the shared `ToolboxTopBar` composable (`com.example.toolbox.ui.tools.ToolboxTopBar`) modeled after `FilesScreen`:

1. **Fixed `68.dp` Height (Never Resizes)**: The outer `Box` uses `Modifier.fillMaxWidth().height(68.dp)` (never `heightIn(min = 68.dp)` with vertical padding that grows or shrinks when actions or search bars toggle). This guarantees zero vertical layout shift when opening/closing the expanding search bar or entering/exiting multi-selection mode.
2. **Leading Icon Badge vs. Back Navigation**:
   - **Root Level**: A `40.dp` `CircleShape` badge (`colorScheme.primaryContainer`) containing a `20.dp` tool icon tinted with `colorScheme.primary`.
   - **Drill-Down / Back Navigation**: When `onNavigateBack != null`, the badge is replaced by a plain `36.dp` `IconButton` with `Icons.AutoMirrored.Filled.ArrowBack` tinted `colorScheme.onSurface` (never wrapped in a colored circular container).
3. **Title & Subtitle Hierarchy**:
   - **Title**: `MaterialTheme.typography.titleLarge`, `FontWeight.Bold`, `colorScheme.onSurface`, single-line with `TextOverflow.Ellipsis`.
   - **Subtext**: `MaterialTheme.typography.bodySmall`, `colorScheme.onSurfaceVariant`, single-line with `TextOverflow.Ellipsis`.
   - **Horizontal Padding & Spacing**: `12.dp` horizontal padding on the toolbar row, `10.dp` spacing between the leading badge/back button and the title column.
4. **Search Icon Always on the Far Right**:
   - Whenever a tool supports toolbar search (`searchState != null`), the search trigger `IconButton` (`Icons.Default.Search`, `36.dp` button with `20.dp` icon tinted `colorScheme.onSurfaceVariant`) is rendered **at the very far right** of the toolbar, after all other trailing action buttons, segmented view toggles, or filter capsules.

```kotlin
ToolboxTopBar(
  title = "Files",
  subtext = subtext,
  icon = Icons.Default.FolderOpen,
  onNavigateBack = if (canNavigateUp) ({ navigateUp() }) else null,
  backButtonTestTag = "action_navigate_up",
  selectionState = if (isSelectionMode) {
    ToolboxTopBarSelectionState(
      selectedCount = selectedPaths.size,
      totalCount = entries.size,
      onToggleSelectAll = { toggleSelectAll() },
      onCloseSelection = { clearSelection() },
      actions = {
        // 36.dp IconButtons (20.dp icons) + red Delete + MoreVert overflow DropdownMenu
      },
    )
  } else {
    null
  },
  actions = {
    // Tool-specific trailing controls (e.g. ToolboxViewModeCycleButton, Sort, Capsules)
    // Placed BEFORE the far-right Search icon button
  },
  searchState = ToolboxTopBarSearchState(
    isActive = isSearchActive,
    query = searchQuery,
    onQueryChange = { searchQuery = it },
    onOpenSearch = { isSearchActive = true },
    onCloseSearch = { closeSearch() },
    placeholder = "Search files & folders...",
  ),
)
```

---

### 5.2 Dialog Design Pattern (e.g. Rename, Create, Inspect, Confirm)
Dialogs must not be bare or plain. They must feature:
1. **28.dp rounded corners** and `surfaceContainerHigh` background (`tonalElevation = 6.dp`).
2. **Circular icon badge, title, and optional subtext** in the header (`44.dp` `CircleShape` in `primaryContainer` with `24.dp` `primary` tint, or `errorContainer` with `error` tint for destructive confirmations; `titleLarge` Bold title and optional `bodySmall` `onSurfaceVariant` subtext in a horizontal `Row` — never use `AlertDialog`'s centered `icon` slot).
3. **Context / Preview Card** showing the item being acted on when relevant so the user never loses context.
4. **Rounded text field** (`14.dp` shape) with leading icon, clear button (`Icons.Default.Clear`), auto-focus, and keyboard action support.
5. **Intelligent Selection**: When editing file names, pre-select the filename stem excluding the extension (`TextRange(0, stem.length)`).
6. **Helper Badges**: Warnings for missing file extensions with one-tap restore pills.
7. **Rounded Action Buttons**: Confirm and dismiss buttons using `CircleShape` (matching the rest of the app's buttons), enabled only when valid and modified.

```kotlin
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
          imageVector = Icons.Default.DriveFileRenameOutline,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp),
        )
      }
      Column {
        Text(
          text = "Rename Item",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "Enter a new name for this item",
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
      // Context Preview Card
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        modifier = Modifier.fillMaxWidth(),
      ) {
        // Item icon, current name, metadata
      }

      // Rounded Input Field
      OutlinedTextField(
        value = textFieldValue,
        onValueChange = { textFieldValue = it },
        label = { Text("New name") },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        // leadingIcon, trailingIcon, keyboardActions, supportingText
      )
    }
  },
  confirmButton = {
    Button(
      onClick = { onConfirm(...) },
      enabled = canConfirm,
      shape = CircleShape,
    ) {
      Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text("Confirm")
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
```

---

### 5.3 Floating Action Buttons (FABs)
- **Primary Action**: `FloatingActionButton(shape = CircleShape, containerColor = colorScheme.primaryContainer)`
- **Secondary / Creation Action**: `SmallFloatingActionButton(shape = CircleShape, containerColor = colorScheme.secondaryContainer)`
- Avoid verbose text in FABs where the icon is globally recognizable (e.g., NoteAdd, Add, CreateNewFolder).

---

### 5.4 Lists, Grid Cards, Shared Pill Navigation Bar (`ToolboxPillNavigationBar` & `ToolboxPillNavigationItem`) & View Mode / Column Controls (`ToolboxViewModeCycleButton` & `pinchToChangeColumns`)
- **Card Styling**: Use `CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))` with `RoundedCornerShape(12.dp)` to `14.dp`.
- **Metadata**: Separate secondary metadata items using bullet points (`•` with `colorScheme.outline`).
- **Unified Pill Navigation Bar (`ToolboxPillNavigationBar` & `ToolboxPillNavigationItem`)**:
  - Every tool with a horizontal pill/filter bar immediately below `ToolboxTopBar` or inside a catalog search overlay (**Files**, **Archive**, **Notes**, **To Do**, **Calculator**, **Unit Converter**, **Gallery**, **Audio Recorder**, **Music Player**, **Planting & Harvest**, **Recipes**, **Chord Book**, **Guitar Tuner**, **OBD2 Scanner**, and **Search**) must use the shared `ToolboxPillNavigationBar` and `ToolboxPillNavigationItem` composables (`com.example.toolbox.ui.tools.ToolboxPillNavigationBar` / `ToolboxPillNavigationItem`):
    - **Borderless & Seamless**: Never wrap the pill navigation bar in an extra `Surface(tonalElevation = 1.dp)` or sandwich it between top/bottom `HorizontalDivider` lines. It sits directly on the screen background immediately below `ToolboxTopBar`.
    - **Consistent Padding & Spacing**: `12.dp` start padding, `6.dp` top/bottom vertical padding, `8.dp` spacing between pills, and automatic right-edge padding (`12.dp` when no floating controls are present, or `(16 + floatingButtonsCount * 40).dp` when floating controls are pinned).
    - **Standardized Pill Styling (`ToolboxPillNavigationItem`)**: Every pill uses `FilterChip` with `RoundedCornerShape(20.dp)`, `16.dp` leading/trailing icons, `MaterialTheme.typography.labelMedium` (`FontWeight.Bold` when selected, `FontWeight.Medium` when unselected), and `FilterChipDefaults.filterChipColors(selectedContainerColor = colorScheme.primaryContainer, selectedLabelColor = colorScheme.onPrimaryContainer, selectedLeadingIconColor = colorScheme.primary, selectedTrailingIconColor = colorScheme.onPrimaryContainer)`.
- **Floating Right-Edge Controls**:
  - Tools with secondary pill-bar controls (**Calculator**, **Gallery**, **Files**, **Archive**, **Music Player**, **Audio Recorder**, **Notes**, and **To Do**) pass them via the `floatingControls` slot (with `floatingButtonsCount` set to the number of `34.dp` circular buttons). `ToolboxPillNavigationBar` pins them at `Alignment.CenterEnd` (`padding(end = 12.dp)`, `zIndex(10f)`, `6.dp` horizontal spacing).
  - **Faded Edge into Background (`Modifier.fadingPillRowUnderFloatingControls` & `Modifier.pillBarFloatingControlsBackdrop`)**: Never allow scrolling pills/chips to be visible directly underneath or between the floating buttons. `ToolboxPillNavigationBar` automatically applies `.fadingPillRowUnderFloatingControls(...)` (offscreen `BlendMode.DstIn` horizontal alpha mask) before `.horizontalScroll(...)` on the pill `Row` and `.pillBarFloatingControlsBackdrop(backgroundColor = ...)` on the floating controls `Row` so pills smoothly dissolve into the background color across a `32.dp` feather zone right before the floating controls.
- **Toolbar View & Column Count Cycle Button (`ToolboxViewModeCycleButton`)**:
  - In any tool with a list and grid view (**Gallery**, **Files**, **Archive**, **Music Player**, **Notes**), place `ToolboxViewModeCycleButton` in `ToolboxTopBar`'s `actions` slot (and do not render a separate items-per-row button in the pill bar). When in List view, it displays the list icon (`Icons.AutoMirrored.Filled.ViewList`); when in Grid/Card/Tile view, it displays the current column count (`2`, `3`, `4`, etc.) centered inside an underlaid `Icons.Default.Fullscreen` icon frame. Pressing the button cycles through `List -> 2 -> 3 -> ... -> maxColumns -> List`.
  - **Pinch-to-Zoom (`Modifier.pinchToChangeColumns`)**: Attach `.pinchToChangeColumns(...)` to the list/grid content container so two-finger pinch-out (fewer columns, larger tiles, and pinching out past `minColumns` transitions into List view) and pinch-in (pinching in from List view transitions into a `minColumns`-column Grid view, and further pinching in increases columns for smaller tiles) seamlessly transition between List view and Grid view and adjust `columnsPerRow` within the tool's range:
    - **Gallery** (default `3`-column Tile view): List view ↔ `2..5` per row.
    - **Files** (default List view), **Archive** (default List view): List view ↔ `2..6` per row.
    - **Music Player** (default List view): List view ↔ `2..3` per row (Card view default `2`).
    - **Notes** (default `2`-column Grid view): List view ↔ `2..3` per row.

---

### 5.5 Multi-Selection & Bulk Actions Pattern (`ToolboxTopBarSelectionState`)
- **Activation**: Long-pressing (`combinedClickable(onLongClick = ...)`) on an item enters multi-select mode and pre-selects that item.
- **Icon Replacement**: All item leading icon boxes are replaced with `Checkbox` components during multi-select mode.
- **Top Toolbar Transformation (`ToolboxMultiSelectBarRow`)**: Every multi-select tool (**Files**, **Notes**, **To Do**, **Gallery**, **Music Player**, **Archive**, **Planting & Harvest**, **Recipes**) uses `ToolboxTopBarSelectionState` inside `ToolboxTopBar` (maintaining the exact `68.dp` height and `surface` background with `1.dp` tonal elevation):
  - **Exit Selection Button**: A `36.dp` `IconButton` (`Icons.Default.Close`, `20.dp` icon).
  - **Select All Toggle Area**: A clickable `RoundedCornerShape(8.dp)` row containing a `32.dp` `Checkbox` (`allSelected`) and `"$selectedCount selected"` text (`typography.titleMedium`, `FontWeight.SemiBold`, `colorScheme.onSurface`). Tapping either the checkbox or the row toggles select all / deselect all.
  - **Compact Action Buttons**: `36.dp` `IconButton`s with `20.dp` icons (`2.dp` spacing) for primary bulk actions—such as Info (`Icons.Default.Info`), Share (`Icons.Default.Share`), Cut (`Icons.Default.ContentCut`), Copy (`Icons.Default.ContentCopy`), Extract (`Icons.Default.Unarchive`), and Delete in red (`Icons.Default.Delete`, `colorScheme.error`, enabled when selection > 0).
  - **Overflow `...` Menu**: A `36.dp` `IconButton` (`Icons.Default.MoreVert`, `20.dp` icon) opening a `DropdownMenu` with labeled text & leading-icon items for all available bulk actions.
- **Floating Paste Bar**: When items are cut or copied, display a floating pill above the bottom edge with paste destination action and cancel option.

---

### 5.6 Sliders & Trackbars (`SlimDotSlider`)
All continuous and stepped sliders across Toolbox must use a **slim track** (`4.dp` to `6.dp` height) paired with a **circular dot drag handle** (`16.dp` to `20.dp` `CircleShape`) rather than the default Material 3 tall vertical bar handle:

- **Continuous / Progress Sliders**: Use `SlimDotSlider` (`4.dp` track height, `16.dp` circular `CircleShape` thumb with `2.dp` elevation/shadow).
- **Stepped Stop Sliders** (e.g., Tools-Per-Row): Use a `6.dp` rounded Canvas track with `3.5.dp` radius stop dots and a `20.dp` `CircleShape` thumb.

```kotlin
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
```

---

### 5.7 Integrated Search Toolbar & Catalog Overlay Pattern (`ToolboxTopBarSearchState`)
Tools with searchable catalogs, presets, or collections (such as **Files**, **Music Player**, **Notes**, **Archive**, **Planting & Harvest**, **Recipes**, **Guitar Tuner**, **Chord Book**, and **OBD2 Scanner**) must use `ToolboxTopBarSearchState` on `ToolboxTopBar`:

1. **Far-Right Toolbar Search Trigger**: `ToolboxTopBar` automatically places the search `IconButton` (`Icons.Default.Search`, `36.dp` button, `20.dp` icon tinted `colorScheme.onSurfaceVariant`) at the **very far right** of the `68.dp` header, after any custom `actions` (such as view-mode segmented toggles, sort menus, or unit/label pills).
2. **Fixed-Height Animated Horizontal Search Bar**: Inside the `68.dp` header `Box`, `ToolboxTopBar` layers an `AnimatedVisibility` container (`enter = fadeIn(tween(220)) + expandHorizontally(expandFrom = Alignment.End)`, `exit = fadeOut(tween(180)) + shrinkHorizontally(shrinkTowards = Alignment.End)`) with `Modifier.matchParentSize()` and `Surface(color = colorScheme.surface, tonalElevation = 2.dp)`:
   - Because the outer `Box` is locked to `Modifier.height(68.dp)` and the search overlay uses `Modifier.matchParentSize()`, activating or dismissing search **never resizes the toolbar**.
   - Leading back button (`Icons.AutoMirrored.Filled.ArrowBack`) to exit search and reset filters.
   - Borderless `TextField` (`Color.Transparent` containers and indicators) that auto-focuses via `FocusRequester` and opens the software keyboard on entry.
   - Trailing clear button (`Icons.Default.Clear`) visible when the query is non-empty, plus optional `trailingContent` before the clear button (e.g. Archive's `Selected` filter chip).
   - `BackHandler(enabled = isSearchActive)` to dismiss search cleanly on system back navigation.
3. **Top-Expanding Search Overlay & Filter Pills**: Below the header `ToolboxTopBar` (or `ToolboxPillNavigationBar`), layer an `AnimatedVisibility` overlay (`expandVertically(expandFrom = Alignment.Top) + fadeIn()`) over the main content `Box`:
   - **Category & Favorites Filter Row**: Use `ToolboxPillNavigationBar` and `ToolboxPillNavigationItem` (`RoundedCornerShape(20.dp)` `FilterChip` pills for `All`, `Favorites`, and domain categories).
   - **Search Results List**: A `LazyColumn` of `RoundedCornerShape(14.dp)` cards (`surfaceVariant.copy(alpha = 0.45f)`) showing matching items, result counts, and an empty-state card when no items match.
4. **Favoriting & Quick-Switch Lists**:
   - Provide a trailing star toggle (`Icons.Default.Star` tinted with `colorScheme.primary` when favorited; `Icons.Default.StarBorder` tinted with `colorScheme.onSurfaceVariant` when unfavorited) on both catalog search cards and in-tool item/preset lists.
   - Related tools that operate on the same preset catalog (such as **Guitar Tuner** and **Chord Book**) share a unified favorite tunings repository so favoriting a tuning in one tool immediately surfaces it in the other.
   - In tools with an active preset or instrument configuration (e.g., **Guitar Tuner**), place the browsable/filterable preset list below the primary interactive display so quick-tapping any item immediately switches the active preset with haptic feedback, while favorited items can be filtered or surfaced at the top.

---

### 5.8 Home Screen AppWidgets Pattern
All Home Screen AppWidgets (including the unified responsive **Music Player** widget—which automatically switches between **Mini Bar**, **Medium Player**, and **Album Art** views based on widget dimensions—as well as **To Do List** and **Notes**) must adhere to the following visual and structural conventions:

1. **Standard `16.dp` Outer Corner Radius**:
   - Every widget root container and rendered background `Bitmap` must use a **`16.dp` corner radius** (`<corners android:radius="16dp" />` in the preview XML `<shape>` drawable and `16f * density` in any dynamically rendered `Canvas.drawRoundRect` background bitmap).
   - Edge-aligned media thumbnails inside a widget (such as the left-aligned album art in the **Mini Bar** and **Medium Player** views or the top album art in the **Album Art** view) must also use a `16.dp` corner radius so they sit flush with the widget corners.
2. **Non-Clipped `1.dp` Outer Outline (`@android:id/background` Launcher Opt-Out + Inset Stroke)**:
   - Give the widget card `FrameLayout` `android:id="@android:id/background"`, `android:background="@drawable/bg_..."` (`<corners android:radius="16dp" />`), and `android:clipToOutline="true"`. On Android 12+ launchers (`RoundedCornerEnforcement.hasAppWidgetOptedOut`), this opts the widget out of the launcher's `28.dp` `system_app_widget_background_radius` clip mask—which would otherwise slice off the 4 corners of a `16.dp` outline—and clips to the widget's own `16.dp` outline instead.
   - The unified **Music Player** widget (`MusicAlbumArtWidgetProvider`, `music_album_art_widget_info.xml`) switches views automatically based on widget height (`resolveWidgetViewStyleForSize`):
     - **Mini Bar view** (`heightDp < 100dp`, card height `72.dp` / `MINI_BAR_HEIGHT_DP = 72f`): Single-row bar with left-aligned square album art, song info, favorite star, and Previous / Play-Pause / Next controls.
     - **Medium Player view** (`100dp <= heightDp < 180dp`, default `4x2`, card height `112.dp` / `MEDIUM_WIDGET_HEIGHT_DP = 112f`): Two-row horizontal card with left-aligned square album art, song info & favorite star on the top row, and the full 5 playback controls below it.
     - **Album Art view** (`heightDp >= 180dp`): Vertical card with top square album art, bottom-right overlaid favorite star, song info, and the full 5 playback controls below it.
   - When drawing the `1.dp` border stroke in `Canvas.drawRoundRect`, inset the stroke rectangle by `strokeInset = max(borderWidthPx * 1.25f, 1.5f)` (`RectF(strokeInset, strokeInset, w - strokeInset, h - strokeInset)`) and use corner radius `cornerRadiusPx + borderWidthPx` so the entire stroke thickness stays strictly inside the `16.dp` `ViewOutlineProvider` curve even when `scaleType="fitXY"` slightly adjusts the bitmap's aspect ratio.
   - In the **Mini Bar** and **Medium Player** views, where the `1:1` square album art occupies the full height on the left (`x in 0..h`), apply a horizontal `LinearGradient` to the border stroke that smoothly fades the outline to transparent as it approaches the album art.
3. **Theme & Dynamic Palette Synchronization**:
   - Widgets must dynamically resolve their colors from the user's active theme settings (`ThemeMode`, Material You dynamic colors, custom seed/accent colors, or active Music Player album-art palette) and refresh immediately whenever underlying tool state changes.
4. **Inner Cards, Badges & Floating Controls**:
   - List items inside scrollable widget collections (such as task cards in **To Do List**) use `14.dp` rounded corners (`<corners android:radius="14dp" />`).
   - Header icon badges, task check circles, transport buttons, and floating action buttons (FABs) inside widgets use circular (`oval`) backgrounds matching the app's `CircleShape` tokens.


