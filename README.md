# Toolbox 🧰

A modern, offline-first multi-tool utility application for Android built entirely with **Jetpack Compose**, **Material 3**, and **Kotlin**.

Toolbox consolidates essential daily tools—from musical utilities to measurement, productivity, and device control aids—into a single, high-performance application with an intuitive, customizable navigation drawer.

---

## 🛠️ Included Tools

The application includes 27 specialized tools grouped into intuitive functional categories:

| Tool | Category | Description | Key Capabilities |
| :--- | :--- | :--- | :--- |
| **🔢 Calculate** | Math & Measurement | High-precision scientific & standard calculator | Live result evaluation, smart auto-matching parentheses `( )`, trigonometry (DEG/RAD), powers, roots, logarithms, percentages, factorials, calculation history tape, memory registers (MC/MR/M+/M-), and clipboard integration. |
| **🗒️ Notes** | Productivity | Simplified offline Google Keep-inspired note and checklist board | 2-column staggered card grid & list view toggle, adaptive light/dark card color palettes, pinned & unpinned sections, seamless plain-text and interactive checklist conversion, custom `#labels` filtering, search by keyword & color, multi-select bulk actions, archive, trash with restore, and undo/redo history. |
| **📝 To Do** | Productivity | Streamlined offline task manager with gesture actions | Ergonomic pill-shaped quick-add bar with instant keyboard entry, hierarchical subtasks with expand/collapse support, swipe-to-complete (right) and swipe-to-delete (left), full undo/redo action history, daily completion counter with automatic midnight reset, completed task archive with restoration, and spring micro-interactions. |
| **📷 Camera** | Photo & Video | Versatile photo and video camera with QR code reader, barcode scanner, pro overlays, and live filters | Photo (JPEG/PNG), Video (MP4), and **QR & Barcode Scanner** modes (supporting 2D QR Code, Data Matrix, Aztec, PDF417 and 1D EAN-13/ISBN, EAN-8, UPC-A, UPC-E, Code 128, Code 39, Code 93, ITF, and Codabar with live frame detection, structured Wi-Fi/URL/vCard/GS1 parsing, scan history, and built-in QR/barcode generator), front/back lens switching with horizontal selfie mirroring, flash/torch modes (Off, Auto, On, Torch), self-timer countdown (Off, 3s, 5s, 10s), aspect ratio framing (4:3, 16:9, 1:1, Full), composition grids (3×3 Rule of Thirds, Golden Ratio, Crosshair), tap-to-focus reticle, pinch & preset zoom (`1x`–`8x`), exposure compensation (`-2.0` to `+2.0 EV`), live color filters (Normal, Mono, Warm, Cool, Sepia, Vivid), configurable `/DCIM/Camera` storage, and direct one-tap link to the Gallery tool's Camera folder. |
| **🏞️ Gallery** | Photo & Video | Photo and album organizer with pinch-to-zoom density | Default `Pictures` and `DCIM` loading with contents un-nested as a unified folder (defaulting new album creation to `Pictures`), default Tile View (3 albums per row, pinchable from 3 up to 6 albums per row) displaying the most recent photo per album with folder label underneath, secondary List View with album cover icon on the left and label on the right, full nested albums/folders navigation with breadcrumbs, and full-screen photo viewer. |
| **📁 Files** | Files | Built-in offline file and storage manager | Directory browsing, breadcrumb path navigation, quick storage volumes (Internal Storage, Documents, Downloads, Cache), list and grid view modes, multi-attribute sorting (Name, Date, Size, Type), file & folder creation, duplication, safe renaming with file extension preservation, recursive deletion with safety prompts, in-app plain text editor, image previewer, metadata & MD5 checksum inspector, file sharing, and external file import. |
| **📐 Units** | Math & Measurement | Fast, multidirectional unit conversion utility | Comprehensive conversion engine across **Length**, **Weight**, **Temperature**, **Volume**, **Speed**, and **Digital Storage / Data** (bits, bytes, KB, MB, GB, TB, PB, KiB, MiB, GiB, TiB, kb, Mb, Gb, Tb) with instant two-way calculations, quick value presets, and one-tap unit swapping. |
| **📏 Ruler & Calipers** | Math & Measurement | Precision on-screen measurement tool | Metric (mm, cm) and Imperial (inches with fractional notation like 5/16" or 2 3/8"), dual-thumb caliper span, hardware DPI auto-detection, manual calibration slider, and real-world reference comparisons (coins, cards). |
| **🎙️ Record** | Audio & Sound | Simple voice and audio recorder with miniature player | Central floating microphone FAB with smooth transition to Stop button, timestamped recording files, selectable formats (M4A default, WAV, AAC, OGG, 3GP), configurable `/Audio` recordings directory, and miniature playback modal with play/pause toggle and tracking slider. |
| **🎧 Music** | Audio & Sound | Offline music library and collapsible player with dynamic album-art theming | Dedicated **Artists**, **Albums**, **Songs**, and **Genres** views with instant **List / Card** view toggle, artist/album/genre drill-down pages, collapsible mini-player bar and full-screen player sheet with seek scrubber, shuffle, repeat (`Off`/`All`/`One`), ±10s skip, volume & mute, playback speed (`0.5x`–`2.0x`), sleep timer, playback queue, and dynamic background gradients that follow the active song's album art. |
| **📺 Remote** | Remotes & Devices | Bluetooth HID remote for Android TV and media devices | Bluetooth device discovery with RSSI signal indicators, pairing and connection lifecycle with inline status and cancel/error feedback, directional D-Pad, media playback and volume controls, live keyboard input synchronization, and air-mouse / gyroscope cursor navigation. |
| **🎮 Gamepad** | Remotes & Devices | Bluetooth HID wireless controller for games and emulators | Full dual-thumbstick or D-Pad + ABXY layout in landscape mode, L1/R1/L2/R2 shoulder triggers, Start/Select/Home system buttons, Bluetooth HID Gamepad descriptor with low-latency input reporting, and haptic feedback. |
| **🎸 Tuner** | Music & Instruments | Real-time chromatic pitch detector and instrument tuner | Digital signal processing via autocorrelation, target frequency matching, cents offset needle gauge, and presets for Standard, Drop D, DADGAD, Open tunings, Half Step Down, and more. |
| **🎼 Chords** | Music & Instruments | Algorithmic chord voicing generator, reverse finder, scale explorer, and songbook | Dynamic combinatorial voicing & anatomical fingering solver across 7 instrument families (6/7-string Guitar, 4/5-string Bass, Ukulele, Banjo, Mandolin), custom tunings & capo, interactive reverse chord/scale search, 20 scale & mode fretboard diagrams with positional boxes and diatonic key harmonization, and transposable progression songbook with scale extraction. |
| **🍽️ Recipes** | Miscellaneous | Personal recipe book with preloaded recipe catalog search | Preloaded catalog of 36+ common recipes across 7 categories (Breakfast, Mains & Dinner, Pasta & Grains, Soups & Salads, Sides & Snacks, Baking & Desserts, Sauces & Drinks); animated toolbar search with category filter chips matching the Plants tool; interactive recipe viewer with serving scaler, ingredient checklist, and step-by-step cooking tracker; favorites filter; and custom recipe creation & editing. |
| **🌱 Plants** | Miscellaneous | Interactive planting and harvest year schedule | Built-in catalog of 36+ vegetables, leafy greens, roots/alliums, herbs, fruits, and flowers; search by name, category, or month; 12-month year view highlighting planting months in the active theme color and harvesting months in the active accent color; monthly planting/harvest breakdown; and custom plant creation & schedule customization. |
| **🎲 Dice** | Miscellaneous | Versatile dice rolling system for tabletop RPGs and board games | Supports standard polyhedral dice (D4, D6, D8, D10, D12, D20, D100), custom dice, modifiers, roll history log with clear/export, fluid 3D roll physics/animations, and responsive layouts adapting to compact and wide displays. |
| **🖼️ Image Editor** | Photo & Video | Photo editing, filters, adjustments, and annotation | Aspect-ratio crop presets (1:1, 4:3, 16:9, Free), rotation & flip, color adjustments (brightness, contrast, saturation), artistic filters, freehand drawing with variable stroke widths (Thin, Med, Thick), dedicated eraser tool, text overlay annotations with custom colors, undo/redo history, and high-resolution export. Hidden by default. |
| **🎬 Video Editor** | Photo & Video | Fast offline video trimmer and editor powered by FFmpeg | Interactive filmstrip timeline with draggable trim handles, lossless instant stream-copy (`-c copy`) trimming & muting, hardware-accelerated `h264_mediacodec` + FFmpeg filtergraph pipeline with automatic software fallback, aspect-ratio crop, 90°/180° rotation & flip, `0.25x`–`3.0x` playback speed, audio volume & fade-in/out, real-time GPU color filters & adjustments, frame snapshot capture, undo/redo history, and export to MP4, GIF, and M4A. Hidden by default. |
| **🎚️ Audio Editor** | Audio & Sound | Waveform audio editor with trim, cut, fade, speed, and multi-format export | Interactive waveform canvas with draggable selection handles, real-time `AudioTrack` preview & loop playback, trim (keep selection) & cut (splice out selection), volume gain & peak normalization, fade-in/fade-out envelopes, speed/pitch resampling (`0.5x`–`2.0x`), audio reversal, multi-step undo/redo history, and export to WAV, M4A, MP3, AAC, OGG, and FLAC. Hidden by default. |
| **🔄 File Converter** | Files | Universal offline file converter powered by Native Android APIs + FFmpeg backup | Dynamic conversion capability map across **34 formats** (Image, Audio, Video, Document/Data, and Archive), preferring hardware-accelerated Native Android/Media3 encoders (`Bitmap`, `PdfRenderer`, `PdfDocument`, `MediaCodec`, `Media3 Transformer`, `Zip`/`Tar`) with 16KB-aligned FFmpeg (`ffmpeg-kit`) as automatic backup and multi-codec engine. Hidden by default. |
| **📄 PDF Viewer** | Files | Offline PDF viewer with freehand drawing and text annotation | Multi-page PDF rendering via `PdfRenderer`, pinch-to-zoom & pan in View mode, freehand drawing (Pen, Highlighter, Eraser) with customizable colors & stroke widths, draggable text annotations with customizable font size & background pills, per-page undo/redo history, built-in sample PDF generator, and full multi-page PDF export (`PdfDocument`). Hidden by default. |
| **🗜️ Archives** | Files | Offline archive explorer, extractor, and compressor | Browse, inspect, extract, and create `.zip`, `.tar`, `.tar.gz`, and `.gz` archives with recursive directory packing, entry size/compression ratios, and direct integration from Files. Hidden by default. |
| **🔊 Decibels** | Audio & Sound | Real-time sound pressure level (dB SPL) meter | Semicircular dB SPL gauge (0–120 dB), live min/avg/max statistics, rolling level history bar graph, calibration offset slider (`-20 dB` to `+20 dB`), environmental noise level classifications, and pause/reset controls. Hidden by default. |
| **⏱️ Metronome** | Music & Instruments | Low-latency audio-visual tempo keeper | AudioTrack pulse synthesis, adjustable BPM (10–300) with tap tempo, customizable time signatures, rhythm subdivisions, accented beats, and synchronized visual flash. Hidden by default. |
| **🎨 Colors** | Miscellaneous | Comprehensive color palette and accessibility suite | Interactive color spectrum canvas, RGB, HSV, and HEX color inspection, WCAG 2.1 contrast ratio and accessibility rating (AA/AAA for normal & large text), and harmony generators (complementary, analogous, triadic, tetradic, monochromatic). Hidden by default. |
| **⚙️ Settings** | System | Application preferences, theme, colors, sorting, and tool visibility | Theme mode (System, Light, Dark), dynamic Material You colors with custom hex color picker and preset themes (fallback to `#8E66A6`), navigation drawer density slider (3 to 6 tools per row), tool sorting modes (**Manual**, **Tool categories** with configurable sub-sorting for categories and tools within categories by **Manual**, **Alpha**, or **Usage**, **Usage** with tracked activity counters and reset option, and **Alphabetical** with ascending / descending toggle), per-tool visibility toggles, and reset to defaults. Always positioned as the final tool. |

---

## ✨ Interface & Experience

- **Customizable Pull-Up Drawer**: Fluid pull-up bottom sheet navigation with customizable grid density (3 to 6 tools per row).
- **Four Navigation Organization Modes**:
  - **Manual**: Personalize your drawer layout with intuitive drag-and-drop tool reordering.
  - **Tool Categories**: Group tools logically into categorized pages with clear category headers, swipeable page indicators, and secondary pill sorting for both categories and tools within categories by Manual, Alpha, or Usage.
  - **Usage**: Automatically sort tools based on real usage frequency, keeping your most frequented utilities within immediate reach.
  - **Alphabetical**: Sort tools by name with selectable ascending (A-Z) or descending (Z-A) ordering.
- **Material Design 3 & Expressive Theming**: Full Material You dynamic theming, custom color palettes, tonal elevation, and complete dark mode / light mode support.
- **Dock & Background Seamlessness**: Coordinated background color transitions ensure docked action bars (such as in To-Do and Ruler) blend seamlessly with the bottom navigation drawer.
- **Edge-to-Edge & Gesture Navigation**: Modern edge-to-edge layout supporting system gesture navigation, window insets, and predictive back animations.
- **Offline & Completely Private**: Zero analytics, telemetry, or external network requests. All computations, DSP, audio synthesis, and file operations happen entirely on-device.

---

## 🎨 Design Guidelines

For architectural and visual specifications, refer to [DESIGN_LANGUAGE.md](DESIGN_LANGUAGE.md). It outlines:
- Material 3 color tokens and surface elevation standards
- Typography scale and formatting rules
- Standard shapes and touch target guidelines
- Component patterns for action bars, dialogs, cards, and animations

---

## 🏗️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/) (JVM Toolchain 17)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 (`androidx.compose.material3`)
- **Navigation**: [AndroidX Navigation 3](https://developer.android.com/guide/navigation) (`androidx.navigation3`)
- **Architecture**: MVVM with Kotlin Coroutines and `StateFlow`
- **Build System**: Gradle 9.x with Android Gradle Plugin (AGP) 9.x, Version Catalogs (`libs.versions.toml`)
- **Audio & Signal Processing**: Low-latency `AudioTrack` pulse generation and custom autocorrelation DSP for real-time fundamental frequency pitch tracking.
- **Bluetooth**: Android Bluetooth HID Host profile integration with gyroscope-based motion cursor tracking.

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
│       │   │   ├── MainActivity.kt           # Entry-point activity
│       │   │   ├── Navigation.kt             # Navigation3 backstack host
│       │   │   ├── NavigationKeys.kt         # Navigation destinations
│       │   │   └── ui/
│       │   │       ├── main/                 # MainScreen, BottomSheet drawer, Tool definitions, Preferences
│       │   │       ├── theme/                # Material 3 theme, dynamic colors, typography
│       │   │       └── tools/                # Tool screens & engines:
│       │   │                                 #  - DiceRollerScreen.kt
│       │   │                                 #  - UnitConverterScreen.kt
│       │   │                                 #  - GuitarTunerScreen.kt
│       │   │                                 #  - MetronomeScreen.kt
│       │   │                                 #  - ColorPickerScreen.kt
│       │   │                                 #  - RulerScreen.kt
│       │   │                                 #  - BluetoothRemoteScreen.kt
│       │   │                                 #  - CalculatorScreen.kt & CalculatorEngine.kt
│       │   │                                 #  - ImageEditorScreen.kt & ImageEditorEngine.kt
│       │   │                                 #  - TodoListScreen.kt & TodoListModel.kt
│       │   │                                 #  - FilesScreen.kt & FileManagerEngine.kt
│       │   │                                 #  - AudioRecorderScreen.kt & AudioRecorderEngine.kt
│       │   │                                 #  - AudioEditorScreen.kt & AudioEditorEngine.kt
│       │   │                                 #  - DecibelMeterScreen.kt
│       │   │                                 #  - MusicPlayerScreen.kt & MusicPlayerEngine.kt
│       │   │                                 #  - FileConverterScreen.kt & FileConverterEngine.kt
│       │   │                                 #  - GalleryScreen.kt & GalleryEngine.kt
│       │   │                                 #  - CameraScreen.kt & CameraEngine.kt
│       │   │                                 #  - VideoEditorScreen.kt & VideoEditorEngine.kt
│       │   │                                 #  - NotesScreen.kt & NotesModel.kt
│       │   │                                 #  - PlantScheduleScreen.kt & PlantScheduleModel.kt
│       │   │                                 #  - SettingsScreen.kt
│       │   └── res/                          # Vector drawables, mipmaps, XML configurations
│       ├── test/                             # Unit tests for tools, math algorithms, engines, and ViewModels
│       └── androidTest/                      # Instrumented UI tests for Compose screens & drawer interactions
├── gradle/
│   ├── libs.versions.toml         # Dependency & version catalog
│   └── wrapper/                      # Gradle wrapper binaries & properties
├── build.gradle.kts               # Root build configuration
├── settings.gradle.kts            # Project settings & plugin repositories
├── DESIGN_LANGUAGE.md             # UI design language, tokens, and component guidelines
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

# Run local unit tests
./gradlew test

# Run Android instrumented tests on an attached device or emulator
./gradlew connectedAndroidTest

# Build, install, and launch directly on connected device/emulator
./gradlew runDebug
```

---

## 📋 Permissions

Toolbox is strictly offline-first. All permissions requested are strictly utilized for local hardware and file management features:

- **`android.permission.CAMERA`**: Required by the **Camera** to capture photos, record videos, and scan QR codes and barcodes in real time.
- **`android.permission.RECORD_AUDIO`**: Required by the **Tuner** (real-time fundamental frequency pitch analysis), **Decibels** (real-time sound pressure level measurement), **Record** (voice and audio recording), and **Camera** (audio capture during video recording). Audio streams for the Tuner and Decibels are analyzed transiently in memory and are never saved or transmitted.
- **`android.permission.BLUETOOTH` / `BLUETOOTH_ADMIN` / `ACCESS_FINE_LOCATION` (Android 11 and below) & `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` / `BLUETOOTH_ADVERTISE` (Android 12+)**: Required by the **Remote** and **Gamepad** to discover, pair with, register as a Bluetooth HID peripheral, and transmit low-latency keyboard, mouse, media, and controller input events to target devices (`neverForLocation` is asserted on Android 12+).
- **`android.permission.MANAGE_EXTERNAL_STORAGE` / `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE`**: Required by **Files**, **Gallery**, and local media tools (**Camera**, **Record**, **Image Editor**, **Video Editor**, **Audio Editor**, and **File Converter**) to browse, organize, preview, edit, convert, and save files across local storage volumes.

---

## 📄 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.
