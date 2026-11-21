# Toolbox 🧰

A modern, offline-first multi-tool utility application for Android built entirely with **Jetpack Compose**, **Material 3**, and **Kotlin**.

Toolbox consolidates essential daily tools—from musical utilities to measurement, productivity, and device control aids—into a single, high-performance application with an intuitive, customizable navigation drawer.

---

## 🛠️ Included Tools

The application includes 31 specialized tools grouped into 9 intuitive functional categories:

| Tool | Category | Description | Key Capabilities |
| :--- | :--- | :--- | :--- |
| **🔢 Calculate** | Math & Measure | High-precision scientific & standard calculator | Live result evaluation, smart auto-matching parentheses `( )`, trigonometry (DEG/RAD), powers, roots, logarithms, percentages, factorials, calculation history tape, memory registers (MC/MR/M+/M-), and clipboard integration. |
| **🗒️ Notes** | Productivity | Simplified offline Google Keep-inspired note and checklist board | 2-column staggered card grid & list view toggle, adaptive light/dark card color palettes, pinned & unpinned sections, seamless plain-text and interactive checklist conversion, custom `#labels` filtering, search by keyword & color, multi-select bulk actions, archive, trash with restore, cross-tool file export/import, and undo/redo history. |
| **📝 To Do** | Productivity | Streamlined offline task manager with scheduling, recurring tasks, and alarm reminders | Ergonomic pill-shaped quick-add bar with instant keyboard entry, hierarchical subtasks with expand/collapse support, due date & time scheduling (24-hour main horizon + scheduled view), Google Calendar-style recurring rules (minutes, hours, days, weekdays, weeks, months, years), exact alarm notifications with reboot persistence, reusable task templates, swipe-to-complete (right) and swipe-to-delete (left), full undo/redo action history, and daily completion counter with automatic midnight reset. |
| **📷 Camera** | Photo & Video | Versatile photo and video camera with QR/barcode scanner, OCR document note scanner, and live ML Kit vision | **Photo** (Standard, Night Mode, Panorama in JPEG/PNG), **Video** (MP4), **Scan** (2D QR Code, Data Matrix, Aztec, PDF417 and 1D EAN-13/ISBN, EAN-8, UPC-A, UPC-E, Code 128, Code 39, Code 93, ITF, Codabar with structured Wi-Fi/URL/vCard/GS1 parsing, scan history, and QR/barcode generator), **Note** (ML Kit OCR document text extraction with direct save to Notes), and **Vision** (real-time ML Kit Object Detection & Image Labeling with confidence threshold filter and bounding boxes); velocity-continuous ML Kit Smart Object Autofocus with 5s tap-to-focus manual override; front/back/multi-lens switching with selfie mirroring, flash/torch, timer (Off, 3s, 5s, 10s), aspect ratios (4:3, 16:9, 1:1, Full), composition grids, pinch & preset zoom (`1x`–`8x`), exposure (`-2.0` to `+2.0 EV`), live color filters, and direct link to Gallery. |
| **🏞️ Gallery** | Photo & Video | Photo and video album organizer with ML Kit smart tag search, inline video previews, and pinch-to-zoom density | Default `Camera` (`/DCIM`) and `Pictures` (`/Pictures`) folders (plus `/Download` with full storage access and custom scoped folders), pinchable grid density (2 to 5 columns) with toolbar view/column cycling, inline muted video previews while scrolling, background ML Kit image labeling & smart tag overlays with disk index caching, call/voice-recording media filtering, multi-attribute sorting & grouping, bulk share/move/copy, trash bin with restore, and one-tap handoff to **Image**, **Image Editor**, or **Video Editor**. |
| **📁 Files** | Files | Built-in offline file and storage manager with cross-tool handoff | Directory browsing, breadcrumb path navigation, quick storage volumes (Internal Storage, Documents, Downloads, Cache), list and grid view modes, multi-attribute sorting (Name, Date, Size, Type), file & folder creation, duplication, safe renaming with extension preservation, recursive deletion, in-app plain text editor, metadata & MD5 checksum inspector, home screen folder shortcuts, and direct cross-tool handoff to **Image**, **Image Editor**, **Video Editor**, **Audio Editor**, **Music**, **PDF Viewer**, **Archive**, **Convert**, **Notes**, and **To Do**. |
| **📐 Units** | Math & Measure | Fast, multidirectional unit conversion utility | Comprehensive conversion engine across 45 units in **Length**, **Weight**, **Temperature**, **Volume**, **Speed**, and **Digital Storage / Data** (bits, bytes, KB, MB, GB, TB, PB, KiB, MiB, GiB, TiB, kb, Mb, Gb, Tb) with instant two-way calculations, quick value presets, and one-tap unit swapping. Hidden by default. |
| **📏 Ruler** | Math & Measure | Precision on-screen ruler, caliper, pitch gauge, protractor, and spirit level | Metric (mm, cm) and Imperial (inches with fractional notation like 5/16" or 2 3/8"), dual-thumb A↔B caliper span, thread & line pitch gauge, 0°–180° angle protractor, 2-axis spirit bubble level, hardware DPI auto-detection, manual calibration slider, and real-world reference comparisons (coins, cards). |
| **🎙️ Record** | Audio | Voice and audio recorder with live waveform, search, and miniature player | Central floating microphone FAB with smooth transition to Stop button, live amplitude waveform, timestamped & searchable recording library, selectable formats (M4A default, WAV, AAC, OGG, 3GP), configurable `/Audio` recordings directory (plus `/Download` with full storage access), miniature playback modal with play/pause and seek slider, and direct handoff to **Audio Editor** or **Music**. |
| **🎧 Music** | Audio | Offline background music player with home screen widgets, system media integration, and dynamic album-art theming | Dedicated **Home** (auto-generated ~1-hour mixes & preferred streaming app links), **Playlists**, **Songs**, **Albums**, **Artists**, and **Genres** tabs with reorderable tabs and **List / Card** view toggle (2–3 cards per row); background playback with Android `MediaSession`, external system media controller syncing & custom transport actions, **Mini Bar** & **Album Art** Home Screen AppWidgets; collapsible mini-player and full-screen player sheet with waveform/seek scrubber, shuffle (`Off`/`Shuffle`/`Smart Shuffle`), repeat (`Off`/`All`/`One`), favorites across songs/artists/albums/genres, custom playlists, track metadata editor, sleep timer, playback speed (`0.5x`–`2.0x`), and dynamic palettes that theme both the player and the entire app. |
| **📺 Remote** | Devices | Bluetooth HID remote for Android TV, PCs, and media devices | Bluetooth device discovery with RSSI signal indicators, pairing and connection lifecycle with inline status and cancel/error feedback, directional D-Pad, media playback and volume controls, live wireless keyboard input synchronization, and touchpad / gyroscope air-mouse cursor navigation. |
| **🎮 Gamepad** | Devices | Bluetooth HID wireless controller for games and emulators | Full dual-thumbstick or D-Pad + ABXY layout in landscape mode, L1/R1/L2/R2 shoulder triggers, Start/Select/Home system buttons, Bluetooth HID Gamepad descriptor with low-latency input reporting, and haptic feedback. |
| **🎸 Tuner** | Instruments | Real-time chromatic pitch detector and instrument tuner | Digital signal processing via autocorrelation, target frequency matching, cents offset needle gauge, synthesized reference tone generator, A4 calibration (415–466 Hz), and 16+ searchable presets for Guitar (6/7-string), Bass (4/5-string), and Ukulele across Standard, Drop D, DADGAD, Open tunings, and more. |
| **🎼 Chords** | Instruments | Algorithmic chord voicing generator, reverse finder, scale explorer, and songbook | Dynamic combinatorial voicing & anatomical fingering solver across 7 instrument families (6/7-string Guitar, 4/5-string Bass, Ukulele, Banjo, Mandolin) and 31 chord qualities, custom tunings & capo, interactive reverse chord/scale search, 20 scale & mode fretboard diagrams with positional boxes and diatonic key harmonization, acoustic strum synthesis, and transposable progression songbook. |
| **🍽️ Recipes** | Lifestyle | Personal recipe book with preloaded recipe catalog search | Preloaded catalog of 38+ curated recipes across 7 categories (Breakfast, Mains & Dinner, Pasta & Grains, Soups & Salads, Sides & Snacks, Baking & Desserts, Sauces & Drinks); animated toolbar search with category filter chips; interactive recipe viewer with dynamic serving scaler, ingredient checklist, and step-by-step cooking tracker; favorites filter; and custom recipe creation & editing. |
| **🌱 Plants** | Lifestyle | Interactive planting and harvest year schedule with care alarms | Built-in catalog of 48+ vegetables, leafy greens, roots/alliums, herbs, fruits, and flowers; search by name, category, or month; 12-month year view highlighting planting months in the active theme color and harvesting months in the active accent color; sunlight, spacing, and watering care guides with reminder notifications; and custom plant creation & schedule customization. |
| **🎲 Dice** | Lifestyle | Versatile dice rolling system for tabletop RPGs and board games | Supports standard polyhedral dice (D4, D6, D8, D10, D12, D20, D100), custom dice pools, modifiers, Advantage/Disadvantage modes, saved RPG presets, roll history log with statistics breakdown & export, fluid 3D roll physics/animations, and responsive layouts adapting to compact and wide displays. |
| **🖼️ Image** | Photo & Video | Standalone full-screen image & animated GIF/WebP viewer with OCR, QR/barcode, and color inspection | Full black-canvas viewer with pinch-to-zoom (`50%`–`500%`), swipeable horizontal paging across folder images with bottom thumbnail strip, animated GIF/WebP playback controls, ML Kit OCR text / QR & barcode / smart tag recognition inspector, interactive auto-focus color picker reticle with hex pill link to **Colors**, and one-tap handoff to **Image Editor**. Hidden by default. |
| **🖌️ Image Editor** | Photo & Video | Non-destructive photo editing, filters, adjustments, and annotation | Aspect-ratio crop presets (1:1, 4:3, 16:9, Free) & straighten, 90° rotation & flip, color adjustments (brightness, contrast, saturation, warmth, vignette), curated filter presets, freehand drawing with variable stroke widths, dedicated eraser tool, text overlay annotations with custom colors, undo/redo history, and high-resolution PNG/JPEG export. Hidden by default. |
| **🎬 Video Editor** | Photo & Video | Fast offline video trimmer and editor powered by Media3 & FFmpeg | Interactive filmstrip timeline with draggable trim handles, lossless instant stream-copy (`-c copy`) trimming & muting, hardware-accelerated `h264_mediacodec` + FFmpeg filtergraph pipeline with automatic software fallback, aspect-ratio crop, 90°/180° rotation & flip, `0.25x`–`3.0x` playback speed, audio volume & fade-in/out, real-time color filters & adjustments, frame snapshot capture, undo/redo history, and export to MP4, GIF, and M4A. Hidden by default. |
| **🎚️ Audio Editor** | Audio | Waveform audio editor with trim, cut, fade, speed, and multi-format export | Interactive waveform canvas with draggable selection handles, real-time `AudioTrack` preview & loop playback, trim (keep selection) & cut (splice out selection), volume gain & peak normalization, fade-in/fade-out envelopes, speed/pitch resampling (`0.5x`–`2.0x`), audio reversal, metadata tag editing, multi-step undo/redo history, and export to WAV, M4A, MP3, AAC, OGG, and FLAC. Hidden by default. |
| **🔄 Convert** | Files | Universal offline file converter powered by Native Android APIs + FFmpeg backup | Dynamic conversion capability map across **34 formats** (Image, Audio, Video, Document/Data, and Archive), preferring hardware-accelerated Native Android/Media3 encoders (`Bitmap`, `PdfRenderer`, `PdfDocument`, `MediaCodec`, `Media3 Transformer`, `Zip`/`Tar`) with 16KB-aligned FFmpeg (`ffmpeg-kit-16kb`) as automatic backup and multi-codec engine. Hidden by default. |
| **📄 PDF Viewer** | Files | Offline PDF viewer with freehand drawing and text annotation | Multi-page PDF rendering via `PdfRenderer`, pinch-to-zoom & pan in View mode, freehand drawing (Pen, Highlighter, Eraser) with customizable colors & stroke widths, draggable text annotations with customizable font size & background pills, per-page undo/redo history, built-in sample PDF generator, and full multi-page annotated PDF export (`PdfDocument`). Hidden by default. |
| **🗜️ Archive** | Files | Offline archive explorer, extractor, and compressor | Browse, inspect, extract, and create `.zip`, `.tar`, `.tar.gz` (`.tgz`), and `.gz` archives with recursive directory packing, configurable compression levels, entry size/compression ratios, and direct integration from **Files**. Hidden by default. |
| **🔊 Decibels** | Math & Measure | Real-time sound pressure level (dB SPL) meter | Semicircular dB SPL gauge (0–120 dB), live min/avg/max statistics, 40-point rolling level history bar graph, calibration offset slider (`-20 dB` to `+20 dB`), 5 environmental noise level classifications, and pause/reset controls. Hidden by default. |
| **⏱️ Metronome** | Instruments | Low-latency audio-visual tempo keeper | `AudioTrack` PCM pulse synthesis, animated mechanical pendulum, adjustable BPM (10–300) with tap tempo, customizable time signatures, rhythm subdivisions (quarter, eighth, triplet, 16th), accented beats, haptic pulse option, and synchronized visual flash. Hidden by default. |
| **🎨 Colors** | Photo & Video | Comprehensive color palette, live camera sampler, and accessibility suite | Interactive 2D HSV saturation-value color canvas, live camera color sampling, HEX/RGB/HSV/HSL/CMYK conversion, WCAG 2.1 contrast ratio and accessibility rating (AA/AAA for normal & large text), named color swatches, and 5 harmony generators (complementary, analogous, triadic, split-complementary, shades). Hidden by default. |
| **🚗 OBD2** | Devices | Bluetooth Low Energy (BLE) ELM327 vehicle diagnostics scanner | BLE GATT discovery & auto-characteristic binding (`FFF0`, `FFE0`, Nordic UART) plus built-in Demo Vehicle simulator, live Mode 01 semicircular RPM & Speed gauges (`km/h`•`°C` / `mph`•`°F`), Coolant Temp, Battery Voltage (`AT RV`), Engine Load, Throttle, Short/Long-Term Fuel Trim, Intake Air Temp, and MAF telemetry cards, Stored (`Mode 03`) & Pending (`Mode 07`) trouble code scanner with Mode 04 MIL reset, searchable offline SAE J2012 DTC dictionary, Mode 09 VIN decoder, and Mode 01 PID 01 I/M emissions readiness monitors. Hidden by default. |
| **🧪 Elements** | Math & Measure | Interactive 118-element IUPAC Periodic Table, Bohr shell diagrams, and molar mass calculator | 18-group interactive Periodic Table of all 118 elements with Lanthanide/Actinide f-block series, pinch-to-zoom & pan table controls, color-coding by chemical category, phase of matter, or orbital block (`s`/`p`/`d`/`f`), dynamic temperature phase simulator (`0 K`–`6000 K` in `K`/`°C`/`°F`), 2D Bohr electron shell canvas, searchable element catalog with favorites, and chemical formula molar mass & mass-percentage calculator. Hidden by default. |
| **🔍 Search** | System | Universal search and instant evaluator across all tools | Search and launch all 31 tools (including hidden tools), evaluate live math expressions, convert units across 6 categories, analyze hex colors & harmonies, evaluate RPG dice formulas, and search across user Notes, To-Do tasks, Recipes, Plant schedules, Guitar/Instrument tunings, Chords, Scales, Progressions, OBD2 SAE J2012 trouble codes, 118 chemical elements, 34 file conversion formats, and app Settings. |
| **⚙️ Settings** | System | Application preferences, theme, colors, permissions, error logs, backup, and tool visibility | Theme mode (System, Light, Dark), dynamic Material You colors with custom hex color picker, automatic accent color harmonies (Complementary, Analogous, Triadic, Split-Complementary, Shades), and live Music Player album-art dynamic theming; navigation drawer density slider (3 to 6 tools per row) and 4 sorting modes (**Manual**, **Tool categories**, **Usage**, and **Alphabetical**); per-tool visibility toggles & standalone Home Screen shortcut pinning; granular **App Permissions** manager with per-tool usage counters and last-used timestamps; **Error Logs** inspector (retaining up to 100 recent diagnostic entries with copy/clear); and full JSON settings & user data backup/restore. Always positioned as the final tool. |

---

## ✨ Interface & Experience

- **Customizable Pull-Up Drawer**: Fluid pull-up bottom sheet navigation with customizable grid density (3 to 6 tools per row), edge-drag page switching, and persistent last-opened tool memory.
- **Four Navigation Organization Modes**:
  - **Manual**: Personalize your drawer layout with intuitive drag-and-drop tool reordering.
  - **Tool Categories**: Group tools logically into categorized pages with clear category headers, swipeable page indicators, and secondary pill sorting for both categories and tools within categories by Manual, Alpha, or Usage.
  - **Usage**: Automatically sort tools based on real usage frequency, keeping your most frequented utilities within immediate reach.
  - **Alphabetical**: Sort tools by name with selectable ascending (A-Z) or descending (Z-A) ordering.
- **Material Design 3, Accent Harmonies & Live Music Theming**: Full Material You dynamic theming, custom hex seed colors, automatic accent harmonies (Complementary, Analogous, Triadic, Split-Complementary, Shades), live album-art theme synchronization from the built-in Music Player or active Android system media sessions, tonal elevation, and complete dark / light mode support.
- **Standalone Home Screen Shortcuts & AppWidgets**: Pin any of the 31 tools (or specific storage folders from Files) to the Android Home Screen as standalone document tasks with dedicated adaptive & monochrome icons in the Recent Apps switcher, plus resizable **Music Mini Bar** and **Music Album Art** Home Screen widgets.
- **Seamless Cross-Tool Handoffs**: Unified context actions allow instant file and content handoffs across tools (e.g., opening files from **Files** or **Gallery** directly into **Image**, **Image Editor**, **Video Editor**, **Audio Editor**, **Music**, **PDF Viewer**, **Archive**, or **Convert**, or sending OCR scans and text to **Notes** and **To Do**).
- **Dock & Background Seamlessness**: Coordinated background color transitions ensure docked action bars (such as in To Do and Ruler) blend seamlessly with the bottom navigation drawer.
- **Edge-to-Edge & Gesture Navigation**: Modern edge-to-edge layout supporting system gesture navigation, window insets, and predictive back animations.
- **Offline & Completely Private**: Zero analytics, telemetry, or external network requests. All computations, on-device ML Kit vision, DSP, audio synthesis, and file operations happen entirely on-device.

---

## 🎨 Design Guidelines

For architectural and visual specifications, refer to [DESIGN_LANGUAGE.md](DESIGN_LANGUAGE.md) and [AGENTS.md](AGENTS.md). They outline:
- Material 3 color tokens and surface elevation standards
- Typography scale and formatting rules
- Standard shapes (`28.dp` dialogs/sheets, `14.dp` cards/fields, `CircleShape` badges/FABs) and touch target guidelines
- Component patterns for `ToolboxTopBar`, `ToolboxPillNavigationBar`, `SlimDotSlider`, expanding search bars, multi-select toolbars, dialogs, and animations

---

## 🏗️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/) 2.3 (JVM Toolchain 17)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 (`androidx.compose.material3`)
- **Navigation**: [AndroidX Navigation 3](https://developer.android.com/guide/navigation) (`androidx.navigation3`)
- **Architecture**: MVVM with Kotlin Coroutines and `StateFlow`
- **Build System**: Gradle 9.x with Android Gradle Plugin (AGP) 9.x, Version Catalogs (`libs.versions.toml`)
- **Media, Conversion & Transcoding**: AndroidX Media3 (`media3-transformer`, `media3-effect`), `PdfRenderer` / `PdfDocument`, `MediaCodec`, and 16KB-page-aligned FFmpeg (`ffmpeg-kit-16kb`)
- **Computer Vision & Scanning**: Google ML Kit On-Device Text Recognition (OCR), Image Labeling, and Object Detection (Smart AF & live Vision overlays), plus ZXing (`com.google.zxing:core`) for 1D/2D barcode scanning and generation
- **Audio & Signal Processing**: Low-latency `AudioTrack` PCM synthesis and custom autocorrelation DSP for real-time fundamental frequency pitch tracking and dB SPL metering
- **Bluetooth**: Android Bluetooth HID Peripheral profile (Remote & Gamepad with gyroscope air-mouse cursor tracking), Bluetooth Low Energy (BLE) GATT client for ELM327 OBD2 vehicle diagnostics, and Bluetooth A2DP/Headset audio route monitoring

---

## 📁 Repository Structure

```text
toolbox/
├── app/
│   ├── build.gradle.kts           # Application build script & dependencies
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── kotlin/com/example/toolbox/
│       │   │   ├── MainActivity.kt               # Entry-point activity & standalone shortcut router
│       │   │   ├── Navigation.kt                 # Navigation3 backstack host
│       │   │   ├── NavigationKeys.kt             # Navigation destinations
│       │   │   ├── data/
│       │   │   │   └── DataRepository.kt
│       │   │   └── ui/
│       │   │       ├── main/                     # MainScreen, BottomSheet drawer, Tool definitions,
│       │   │       │                             # NavigationPreferences, HomeScreenShortcuts
│       │   │       ├── theme/                    # Material 3 theme, dynamic colors, accent harmonies, typography
│       │   │       └── tools/                    # Tool screens, engines & shared components:
│       │   │                                     #  - ToolboxTopBar.kt & CrossToolActions.kt
│       │   │                                     #  - ArchiveScreen.kt & ArchiveEngine.kt
│       │   │                                     #  - AudioEditorScreen.kt & AudioEditorEngine.kt
│       │   │                                     #  - AudioRecorderScreen.kt & AudioRecorderEngine.kt
│       │   │                                     #  - BluetoothGamepadScreen.kt
│       │   │                                     #  - BluetoothRemoteScreen.kt & BluetoothRemoteViewModel.kt
│       │   │                                     #  - BluetoothHidProtocol.kt & BluetoothHidSession.kt
│       │   │                                     #  - CalculatorScreen.kt & CalculatorEngine.kt
│       │   │                                     #  - CameraScreen.kt & CameraEngine.kt
│       │   │                                     #  - ChordBookScreen.kt & ChordBookEngine.kt
│       │   │                                     #  - ColorPickerScreen.kt
│       │   │                                     #  - DecibelMeterScreen.kt
│       │   │                                     #  - DiceRollerScreen.kt
│       │   │                                     #  - ElementsScreen.kt & ElementsEngine.kt
│       │   │                                     #  - FileConverterScreen.kt & FileConverterEngine.kt
│       │   │                                     #  - FilesScreen.kt & FileManagerEngine.kt
│       │   │                                     #  - GalleryScreen.kt & GalleryEngine.kt
│       │   │                                     #  - GuitarTunerScreen.kt
│       │   │                                     #  - ImageScreen.kt
│       │   │                                     #  - ImageEditorScreen.kt & ImageEditorEngine.kt
│       │   │                                     #  - MetronomeScreen.kt
│       │   │                                     #  - MusicPlayerScreen.kt, MusicPlayerEngine.kt & MusicPlayerWidgets.kt
│       │   │                                     #  - NotesScreen.kt & NotesModel.kt
│       │   │                                     #  - Obd2ScannerScreen.kt & Obd2ScannerEngine.kt
│       │   │                                     #  - PdfViewerScreen.kt & PdfViewerEngine.kt
│       │   │                                     #  - PlantScheduleScreen.kt, PlantScheduleModel.kt & PlantIcons.kt
│       │   │                                     #  - RecipeBookScreen.kt & RecipeBookModel.kt
│       │   │                                     #  - RulerScreen.kt
│       │   │                                     #  - SearchScreen.kt & SearchEngine.kt
│       │   │                                     #  - SettingsScreen.kt
│       │   │                                     #  - TodoListScreen.kt, TodoListModel.kt & TodoNotificationReceiver.kt
│       │   │                                     #  - UnitConverterScreen.kt
│       │   │                                     #  - VideoEditorScreen.kt & VideoEditorEngine.kt
│       │   └── res/                              # Vector drawables, adaptive tool mipmaps, AppWidget layouts, XML configs
│       ├── test/                                 # Unit tests for tools, math algorithms, engines, and ViewModels
│       └── androidTest/                          # Instrumented UI tests for Compose screens & drawer interactions
├── gradle/
│   ├── libs.versions.toml         # Dependency & version catalog
│   └── wrapper/                   # Gradle wrapper binaries & properties
├── build.gradle.kts               # Root build configuration
├── settings.gradle.kts            # Project settings & plugin repositories
├── DESIGN_LANGUAGE.md             # UI design language, tokens, and component guidelines
├── AGENTS.md                      # AI agent guidelines & UI implementation checklist
├── LICENSE                        # GNU General Public License v3.0
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or newer recommended
- **JDK**: Java Development Kit 17
- **Android SDK**:
  - `compileSdk`: **36**
  - `minSdk`: **24** (Android 7.0 Nougat)
  - `targetSdk`: **36**

### Building & Running

You can build and test the application directly using the Gradle wrapper:

```bash
# Build the debug APK
./gradlew assembleDebug

# Build the debug apk when shit's fucked
./gradlew assembleDebug --rerun-tasks --no-build-cache --no-configuration-cache

# Run local unit tests
./gradlew test

# Run Android instrumented tests on an attached device or emulator
./gradlew connectedAndroidTest

# Build, install, and launch directly on connected device/emulator
./gradlew runDebug
```

---

## 📋 Permissions

Toolbox is strictly offline-first. All permissions requested are strictly utilized for local hardware, notifications, and file management features, and can be audited with per-tool usage counters and timestamps in **Settings → App Permissions**:

- **`android.permission.CAMERA`**: Required by **Camera** (to capture photos, record videos, scan QR codes/barcodes, extract OCR document notes, and run ML Kit Vision detection) and **Colors** (live camera color sampling).
- **`android.permission.RECORD_AUDIO`**: Required by **Tuner** (real-time fundamental frequency pitch analysis), **Decibels** (real-time sound pressure level measurement), **Record** (voice and audio recording), and **Camera** (audio capture during video recording). Audio streams for the Tuner and Decibels are analyzed transiently in memory and are never saved or transmitted.
- **`android.permission.MANAGE_EXTERNAL_STORAGE` / `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE`**: Required by **Files** to browse and manage all device storage volumes, and unlocks full `/Download` folder access and cross-folder operations in **Gallery**, **Record**, **Music**, and local media tools (**Image**, **Image Editor**, **Video Editor**, **Audio Editor**, **PDF Viewer**, **Archive**, and **Convert**).
- **`android.permission.BLUETOOTH` / `BLUETOOTH_ADMIN` / `ACCESS_FINE_LOCATION` (Android 11 and below) & `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` / `BLUETOOTH_ADVERTISE` (Android 12+)**: Required by **Remote** and **Gamepad** (Bluetooth HID peripheral registration and low-latency input transmission), **OBD2** (BLE GATT ELM327 vehicle diagnostics), and **Music** (Bluetooth audio route detection). `neverForLocation` is asserted on Android 12+.
- **`android.permission.POST_NOTIFICATIONS`**: Required by **To Do** (scheduled task alarm notifications), **Plants** (plant care and watering reminders), and **Music** (media playback notifications).
- **`android.permission.SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`**: Required by **To Do** and **Plants** to trigger scheduled task and plant care reminders at exact due times.
- **`android.permission.RECEIVE_BOOT_COMPLETED`**: Required by **To Do** and **Plants** to automatically reschedule active task and plant care alarms after a device reboot.
- **`android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`**: Optional system access used by **Music** and **Settings** (`ToolboxMediaListenerService`) to observe active Android media sessions for dynamic album-art theming and external media transport controls.
- **`android.permission.VIBRATE`**: Used by **Metronome**, **Dice**, **Remote**, **Gamepad**, and **To Do** for tactile feedback, beat pulses, and alert vibrations.
- **`android.permission.REQUEST_INSTALL_PACKAGES`**: Used by **Files** to launch the system package installer when opening local `.apk` packages.

---

## 📄 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.
