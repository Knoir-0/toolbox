# Toolbox 🧰

A modern, offline-first multi-tool utility application for Android built entirely with **Jetpack Compose**, **Material 3**, and **Kotlin**.

Toolbox consolidates essential daily tools—from musical utilities to measurement and gaming aids—into a single, high-performance application with an intuitive, customizable navigation drawer.

---

## 🛠️ Included Tools

| Tool | Description | Key Capabilities |
| :--- | :--- | :--- |
| **🎲 Dice Roller** | Versatile dice rolling system for tabletop RPGs and board games | Supports standard polyhedral dice (D4, D6, D8, D10, D12, D20, D100), custom dice, modifiers, roll history log, and fluid animations. |
| **🎸 Guitar Tuner** | Real-time chromatic pitch detector and instrument tuner | Digital signal processing via autocorrelation, target frequency matching, cents offset gauge, and presets for Standard, Drop D, DADGAD, Open tunings, etc. |
| **🎨 Color Picker** | Comprehensive color palette and accessibility suite | RGB, HSV, and HEX color inspection, WCAG contrast ratio and accessibility rating (AA/AAA), and harmony generators (complementary, analogous, triadic, etc.). |
| **📏 Ruler & Calipers** | Precision on-screen measurement tool | Metric (mm, cm) and Imperial (inches with fractional notation like 5/16" or 2 3/8"), dual-thumb caliper span, hardware DPI auto-detection, manual calibration, and real-world reference comparisons. |
| **📐 Unit Converter** | Fast, flexible unit conversion utility | Comprehensive conversion engine across length, temperature, and standard unit dimensions with instant two-way calculations. |
| **⏱️ Metronome** | Low-latency audio-visual tempo keeper | AudioTrack pulse synthesis, adjustable BPM with tap tempo, customizable time signatures, accent beats, and synchronized visual flash. |

---

## ✨ Interface & Experience

- **Customizable Bottom Navigation**: Fluid pull-up bottom sheet drawer with customizable density (3 to 6 tools per row) and drag-and-drop tool reordering.
- **Material 3 & Dynamic Theming**: Full Material You dynamic theming with complete dark mode and light mode support.
- **Edge-to-Edge**: Modern edge-to-edge rendering supporting predictive back and gesture navigation.
- **Offline & Private**: All computation, pitch detection, and synthesis happen entirely on-device without network telemetry.

---

## 🏗️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/) (JVM Toolchain 17)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 (`androidx.compose.material3`)
- **Navigation**: [AndroidX Navigation 3](https://developer.android.com/guide/navigation) (`androidx.navigation3`)
- **Architecture**: MVVM with Kotlin Coroutines and `StateFlow`
- **Build System**: Gradle 9.x with Android Gradle Plugin (AGP) 9.x, Version Catalogs (`libs.versions.toml`)
- **Audio & Signal Processing**: Low-latency `AudioTrack` pulse generation and custom autocorrelation DSP for real-time fundamental frequency pitch tracking.

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
│       │   │   ├── data/                     # Data sources and repositories
│       │   │   └── ui/
│       │   │       ├── main/                 # MainScreen, BottomSheet drawer, Tool definitions
│       │   │       ├── theme/                # Material 3 theme, colors, typography
│       │   │       └── tools/                # Screens: Dice, Tuner, Ruler, ColorPicker, Metronome, Converter
│       │   └── res/                          # Drawables, mipmaps, XML configurations
│       └── test/                             # Unit tests for tools, math algorithms, and ViewModels
├── gradle/
│   ├── libs.versions.toml         # Dependency & version catalog
│   └── wrapper/                      # Gradle wrapper binaries & properties
├── build.gradle.kts               # Root build configuration
├── settings.gradle.kts            # Project settings & plugin repositories
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

- `android.permission.RECORD_AUDIO`: Required by the **Guitar Tuner** to capture real-time acoustic instrument audio for pitch analysis. Audio is processed transiently in memory and is never recorded or stored.

---

## 📄 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.
