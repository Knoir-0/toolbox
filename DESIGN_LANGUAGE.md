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

### 2.1 Dynamic Material You & Color Tokens
Toolbox supports system dynamic theming (Android 12+ / API 31+) and custom user-selected themes in **Settings**, falling back to a curated purple-lavender palette (`#8E66A6`) on older systems.

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
- **Device & Category Pills / Filter Chips**: `RoundedCornerShape(20.dp)` or `CircleShape`
- **Item Cards (Lists & Grids)**: `RoundedCornerShape(12.dp)` to `RoundedCornerShape(14.dp)`
- **Text Input Fields (`OutlinedTextField`)**: `RoundedCornerShape(14.dp)` or `RoundedCornerShape(16.dp)`
- **Buttons (Dialog Confirm / Dismiss)**: `RoundedCornerShape(12.dp)` to `RoundedCornerShape(14.dp)`
- **Floating Action Buttons (FAB & Mini-FAB)**: `CircleShape`
- **Header Icon Badges**: `CircleShape` (size: 36.dp to 44.dp)

---

## 5. Standard Component Patterns

### 5.1 Tool Header / Top Bar
Every tool toolbar adheres to a consistent height and layout matching `TodoListScreen` and `FilesScreen`:

```kotlin
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
        // 36dp - 40dp Circular Icon Badge
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
          )
        }

        // Title + Subtext Column
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = subtext,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      // Action Icon Buttons (Search, Sort, ViewMode, etc.)
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Trailing icon buttons
      }
    }
  }
}
```

---

### 5.2 Dialog Design Pattern (e.g. Rename, Create, Inspect)
Dialogs must not be bare or plain. They must feature:
1. **28.dp rounded corners** and `surfaceContainerHigh` background.
2. **Circular icon badge** in the header (`primaryContainer` with primary tint).
3. **Context / Preview Card** showing the item being acted on so the user never loses context.
4. **Rounded text field** (`14.dp` shape) with leading icon, clear button (`Icons.Default.Clear`), auto-focus, and keyboard action support.
5. **Intelligent Selection**: When editing file names, pre-select the filename stem excluding the extension (`TextRange(0, stem.length)`).
6. **Helper Badges**: Warnings for missing file extensions with one-tap restore pills.
7. **Rounded Buttons**: Confirm button with icon (`Icons.Default.Check`) and label, enabled only when valid and modified.

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
```

---

### 5.3 Floating Action Buttons (FABs)
- **Primary Action**: `FloatingActionButton(shape = CircleShape, containerColor = colorScheme.primaryContainer)`
- **Secondary / Creation Action**: `SmallFloatingActionButton(shape = CircleShape, containerColor = colorScheme.secondaryContainer)`
- Avoid verbose text in FABs where the icon is globally recognizable (e.g., NoteAdd, Add, CreateNewFolder).

---

### 5.4 Lists & Grid Cards
- Use `CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))` with `RoundedCornerShape(12.dp)` to `14.dp`.
- Separate secondary metadata items using bullet points (`•` with `colorScheme.outline`).
- Provide action menus via `IconButton` with `Icons.Default.MoreVert`.

---

### 5.5 Multi-Selection & Bulk Actions Pattern
- **Activation**: Long-pressing (`combinedClickable(onLongClick = ...)`) on an item enters multi-select mode and pre-selects that item.
- **Icon Replacement**: All item leading icon boxes are replaced with `Checkbox` components during multi-select mode.
- **Top Toolbar Transformation**: Replaces title and leading icon with:
  - An exit selection button (`Icons.Default.Close`).
  - A "Select All" checkbox paired with `# selected` text (`titleMedium`, `SemiBold`). Tapping toggles select all / deselect all.
  - Action buttons: Info (`Icons.Default.Info`), Cut (`Icons.Default.ContentCut`), Copy (`Icons.Default.ContentCopy`), and Delete in red (`Icons.Default.Delete`, `colorScheme.error`, enabled when selection > 0).
  - Overflow `...` menu (`MoreVert`) containing the labeled text options for all actions.
- **Floating Paste Bar**: When items are cut or copied, display a floating pill above the bottom edge with paste destination action and cancel option.

---

## 6. Dos and Don'ts Checklist for AI Agents

- [x] **DO** use `MaterialTheme.colorScheme.*` for all colors; verify dark and light mode contrast.
- [x] **DO** use standard corner radii: `28.dp` for dialogs/sheets, `14.dp` for fields/cards, `CircleShape` for FABs and icon badges.
- [x] **DO** provide immediate haptic feedback (`LocalHapticFeedback`) on meaningful actions (duplicate/copy, create, delete).
- [x] **DO** support autofocus with `FocusRequester` and pre-select meaningful text in editing dialogs.
- [x] **DO** provide clear inline error messages and disable confirm buttons when inputs are invalid or unchanged.
- [ ] **DON'T** use raw drop shadows; use Material 3 `tonalElevation`.
- [ ] **DON'T** use rectangular square buttons (`RectangleShape`) in dialogs or controls.
- [ ] **DON'T** hardcode raw string dimensions or colors in `@Composable` code.
- [ ] **DON'T** leave dialogs as bare text fields without an icon badge, explanatory subtitle, and item context.
