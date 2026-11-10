# Toolbox 🧰

A modern, offline-first multi-tool utility application for Android built entirely with **Jetpack Compose**, **Material 3**, and **Kotlin**.

Toolbox consolidates essential daily tools—from musical utilities to measurement, productivity, and device control aids—into a single, high-performance application with an intuitive, customizable navigation drawer.

---

## 🛠️ Included Tools

The application includes 12 specialized tools grouped into intuitive functional categories:

| Tool | Category | Description | Key Capabilities |
| :--- | :--- | :--- | :--- |
| **🎲 Dice Roller** | Entertainment & Devices | Versatile dice rolling system for tabletop RPGs and board games | Supports standard polyhedral dice (D4, D6, D8, D10, D12, D20, D100), custom dice, modifiers, roll history log with clear/export, fluid 3D roll physics/animations, and responsive layouts adapting to compact and wide displays. |
| **📐 Unit Converter** | Math & Measurement | Fast, multidirectional unit conversion utility | Comprehensive conversion engine across **Length**, **Weight**, **Temperature**, **Volume**, **Speed**, and **Digital Storage / Data** (bits, bytes, KB, MB, GB, TB, PB, KiB, MiB, GiB, TiB, kb, Mb, Gb, Tb) with instant two-way calculations, quick value presets, and one-tap unit swapping. |
| **🎸 Guitar Tuner** | Audio & Music | Real-time chromatic pitch detector and instrument tuner | Digital signal processing via autocorrelation, target frequency matching, cents offset needle gauge, and presets for Standard, Drop D, DADGAD, Open tunings, Half Step Down, and more. |
| **⏱️ Metronome** | Audio & Music | Low-latency audio-visual tempo keeper | AudioTrack pulse synthesis, adjustable BPM (10–300) with tap tempo, customizable time signatures, rhythm subdivisions, accented beats, and synchronized visual flash. |
| **🎨 Color Picker** | Media & Creative | Comprehensive color palette and accessibility suite | Interactive color spectrum canvas, RGB, HSV, and HEX color inspection, WCAG 2.1 contrast ratio and accessibility rating (AA/AAA for normal & large text), and harmony generators (complementary, analogous, triadic, tetradic, monochromatic). |
| **📏 Ruler & Calipers** | Math & Measurement | Precision on-screen measurement tool | Metric (mm, cm) and Imperial (inches with fractional notation like 5/16" or 2 3/8"), dual-thumb caliper span, hardware DPI auto-detection, manual calibration slider, and real-world reference comparisons (coins, cards). |
| **📺 TV Remote** | Entertainment & Devices | Bluetooth HID remote for Android TV and media devices | Bluetooth device discovery with RSSI signal indicators, pairing and connection lifecycle with inline status and cancel/error feedback, directional D-Pad, media playback and volume controls, live keyboard input synchronization, and air-mouse / gyroscope cursor navigation. |
| **🔢 Calculator** | Math & Measurement | High-precision scientific & standard calculator | Live result evaluation, smart auto-matching parentheses `( )`, trigonometry (DEG/RAD), powers, roots, logarithms, percentages, factorials, calculation history tape, memory registers (MC/MR/M+/M-), and clipboard integration. |
| **🖼️ Image Editor** | Media & Creative | Photo editing, filters, adjustments, and annotation | Aspect-ratio crop presets (1:1, 4:3, 16:9, Free), rotation & flip, color adjustments (brightness, contrast, saturation), artistic filters, freehand drawing with variable stroke widths (Thin, Med, Thick), dedicated eraser tool, text overlay annotations with custom colors, undo/redo history, and high-resolution export. |
| **📝 To-Do** | Productivity | Streamlined offline task manager with gesture actions | Ergonomic pill-shaped quick-add bar with instant keyboard entry, hierarchical subtasks with expand/collapse support, swipe-to-complete (right) and swipe-to-delete (left), full undo/redo action history, daily completion counter with automatic midnight reset, completed task archive with restoration, and spring micro-interactions. |
| **📁 Files** | Productivity | Built-in offline file and storage manager | Directory browsing, breadcrumb path navigation, quick storage volumes (Internal Storage, Documents, Downloads, Cache), list and grid view modes, multi-attribute sorting (Name, Date, Size, Type), file & folder creation, duplication, safe renaming with file extension preservation, recursive deletion with safety prompts, in-app plain text editor, image previewer, metadata & MD5 checksum inspector, file sharing, and external file import. |
| **⚙️ Settings** | System | Application preferences, theme, colors, sorting, and tool visibility | Theme mode (System, Light, Dark), dynamic Material You colors with custom hex color picker and preset themes (fallback to `#8E66A6`), navigation drawer density slider (3 to 6 tools per row), tool sorting modes (**Manual**, **Tool categories**, and **By usage** with tracked activity counters and reset option), per-tool visibility toggles, and reset to defaults. Always positioned as the final tool. |

---

## ✨ Interface & Experience

- **Customizable Pull-Up Drawer**: Fluid pull-up bottom sheet navigation with customizable grid density (3 to 6 tools per row).
- **Three Navigation Organization Modes**:
  - **Manual**: Personalize your drawer layout with intuitive drag-and-drop tool reordering.
  - **Tool Categories**: Group tools logically into categorized pages with clear category headers and swipeable page indicators.
  - **By Usage**: Automatically sort tools based on real usage frequency, keeping your most frequented utilities within immediate reach.
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

- **`android.permission.RECORD_AUDIO`**: Required by the **Guitar Tuner** to capture acoustic instrument audio for real-time fundamental frequency pitch analysis. Audio samples are analyzed transiently in memory and are never saved or transmitted.
- **`android.permission.BLUETOOTH` / `BLUETOOTH_ADMIN` / `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` / `ACCESS_FINE_LOCATION`**: Required by the **TV Remote** to discover, pair with, and transmit HID keyboard/mouse/media input events to Bluetooth TV and streaming devices.
- **`android.permission.MANAGE_EXTERNAL_STORAGE` / `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE`**: Required by the **Files** manager to view, open, edit, organize, and manage files and directories across local storage volumes.

---

## 📄 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.
