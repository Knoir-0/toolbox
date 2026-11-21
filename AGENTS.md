# AI Agent Guidelines & Checklist

All AI agents implementing new tools, screens, dialogs, or UI enhancements for the **Toolbox** Android application must **always check and read [DESIGN_LANGUAGE.md](./DESIGN_LANGUAGE.md) before initiating work**, follow its specifications, adhere to the checklist below, and **update [README.md](./README.md) whenever significant changes or new features are made**.

## Dos and Don'ts Checklist for AI Agents

- [x] **DO** always check and read [DESIGN_LANGUAGE.md](./DESIGN_LANGUAGE.md) before initiating any work.
- [x] **DO** remember to update [README.md](./README.md) if significant changes have been made.
- [x] **DO** use `MaterialTheme.colorScheme.*` for all colors; verify dark and light mode contrast.
- [x] **DO** use standard corner radii: `28.dp` for dialogs/sheets, `14.dp` for fields/cards, `CircleShape` for FABs, icon badges, and slider dot handles.
- [x] **DO** use `ToolboxTopBar` (`68.dp` fixed height, `12.dp` horizontal padding, `40.dp` `primaryContainer` circular icon badge with `20.dp` `primary`-tinted icon, `titleLarge` Bold title + `bodySmall` `onSurfaceVariant` subtext) for all tool headers.
- [x] **DO** place the toolbar search icon (`Icons.Default.Search`) at the **very far right** of the top bar (after all other trailing action buttons, toggles, or pills) via `ToolboxTopBarSearchState`.
- [x] **DO** render header back/up navigation buttons (`Icons.AutoMirrored.Filled.ArrowBack`) as plain `36.dp` `IconButton`s with `MaterialTheme.colorScheme.onSurface` tint and no circular background container.
- [x] **DO** use `ToolboxTopBarSelectionState` (`ToolboxMultiSelectBarRow`) for all multi-selection toolbars so they share the `FilesScreen` layout (Close button, clickable Select All checkbox + count, `36.dp` action icons, red Delete, and `MoreVert` overflow menu).
- [x] **DO** use `ToolboxPillNavigationBar` and `ToolboxPillNavigationItem` (`DESIGN_LANGUAGE.md` Section 5.4) for all horizontal pill navigation/filter bars beneath `ToolboxTopBar` or inside catalog search overlays so padding (`12.dp` horizontal, `6.dp` vertical), spacing (`8.dp`), `RoundedCornerShape(20.dp)` geometry, typography (`labelMedium`, `Bold` when selected), and icon tints (`primary` leading icon when selected) remain identical across every tool.
- [x] **DO** use `SlimDotSlider` (slim `4.dp`–`6.dp` track with `16.dp`–`20.dp` circular dot thumb) for all sliders and trackbars.
- [x] **DO** use the integrated expanding toolbar search & catalog overlay pattern (`DESIGN_LANGUAGE.md` Section 5.7) with `ToolboxPillNavigationBar` / `ToolboxPillNavigationItem` filter chips and star favoriting for searchable tool catalogs and presets.
- [x] **DO** provide immediate haptic feedback (`LocalHapticFeedback`) on meaningful actions (duplicate/copy, create, delete, favorite, quick-switch).
- [x] **DO** support autofocus with `FocusRequester` and pre-select meaningful text in editing dialogs and expanding search bars.
- [x] **DO** provide clear inline error messages and disable confirm buttons when inputs are invalid or unchanged.
- [ ] **DON'T** use `heightIn(min = 68.dp)` with variable vertical padding on toolbars that causes the bar to resize when toggling search or multi-selection mode; always keep the top bar locked to `68.dp`.
- [ ] **DON'T** place the search icon to the left of view-mode toggles, sort buttons, or other toolbar actions.
- [ ] **DON'T** use raw drop shadows; use Material 3 `tonalElevation`.
- [ ] **DON'T** use default thick Material 3 sliders with tall vertical bar handles.
- [ ] **DON'T** use standalone dropdowns for large searchable preset catalogs when an integrated search toolbar and quick-switch list fits the tool workflow.
- [ ] **DON'T** use rectangular square buttons (`RectangleShape`) in dialogs or controls.
- [ ] **DON'T** hardcode raw string dimensions or colors in `@Composable` code.
- [ ] **DON'T** leave dialogs as bare text fields without an icon badge, explanatory subtitle, and item context.
