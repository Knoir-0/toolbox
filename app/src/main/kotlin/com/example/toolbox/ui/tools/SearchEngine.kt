package com.example.toolbox.ui.tools

import android.content.Context
import com.example.toolbox.ui.main.Tool
import com.example.toolbox.ui.main.ToolCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Category filter chips for narrowing universal search results and tools directory,
 * aligned directly with [ToolCategory].
 */
enum class SearchCategoryFilter(
  val label: String,
  val toolCategory: ToolCategory? = null,
) {
  ALL("All", null),
  MATH_AND_MEASUREMENT(ToolCategory.MATH_AND_MEASUREMENT.title, ToolCategory.MATH_AND_MEASUREMENT),
  PRODUCTIVITY(ToolCategory.PRODUCTIVITY.title, ToolCategory.PRODUCTIVITY),
  FILES_AND_STORAGE(ToolCategory.FILES_AND_STORAGE.title, ToolCategory.FILES_AND_STORAGE),
  PHOTO_AND_VIDEO(ToolCategory.PHOTO_AND_VIDEO.title, ToolCategory.PHOTO_AND_VIDEO),
  AUDIO_AND_SOUND(ToolCategory.AUDIO_AND_SOUND.title, ToolCategory.AUDIO_AND_SOUND),
  MUSIC_AND_INSTRUMENTS(ToolCategory.MUSIC_AND_INSTRUMENTS.title, ToolCategory.MUSIC_AND_INSTRUMENTS),
  REMOTES_AND_DEVICES(ToolCategory.REMOTES_AND_DEVICES.title, ToolCategory.REMOTES_AND_DEVICES),
  MISCELLANEOUS(ToolCategory.MISCELLANEOUS.title, ToolCategory.MISCELLANEOUS),
  SYSTEM(ToolCategory.SYSTEM.title, ToolCategory.SYSTEM),
}

/**
 * Specific kind of search result surfaced by [SearchEngine].
 */
enum class SearchResultType(val label: String) {
  TOOL("Tool"),
  LIVE_CALCULATION("Calculation"),
  LIVE_CONVERSION("Unit Conversion"),
  LIVE_COLOR("Color Analysis"),
  LIVE_DICE_ROLL("Dice Formula"),
  NOTE("Note"),
  TODO_TASK("Task"),
  RECIPE("Recipe"),
  PLANT("Plant"),
  TUNING("Tuning"),
  CHORD("Chord"),
  SCALE("Scale"),
  PROGRESSION("Progression"),
  TEMPO("Tempo & Meter"),
  UNIT("Unit"),
  RULER_REFERENCE("Reference Object"),
  DECIBEL_LEVEL("Noise Level"),
  COLOR_SWATCH("Color Swatch"),
  OBD2_DTC("Trouble Code"),
  FILE_FORMAT("File Format"),
  MUSIC_TRACK("Music Track"),
  AUDIO_RECORDING("Audio Format"),
  CAMERA_MODE("Camera & Scanner"),
  DEVICE_CONTROL("Remote & Gamepad"),
  MEDIA_CAPABILITY("Media & Files"),
  SETTING("Setting"),
}

/**
 * Structured key-value row displayed inside the Search Result inspection dialog.
 */
data class SearchResultDetailRow(
  val label: String,
  val value: String,
)

/**
 * Rich metadata describing a [Tool] for directory browsing and keyword matching.
 */
data class ToolSearchMetadata(
  val tool: Tool,
  val summary: String,
  val description: String,
  val capabilities: List<String>,
  val keywords: List<String>,
  val sampleQueries: List<String>,
)

/**
 * Suggested discovery query chip displayed on the Search home view.
 */
data class SearchDiscoverySuggestion(
  val query: String,
  val label: String,
  val tool: Tool,
)

/**
 * A single unified search result item pointing to a [Tool] and optional deep content details.
 */
data class SearchResultItem(
  val id: String,
  val title: String,
  val subtitle: String,
  val snippet: String,
  val badgeLabel: String,
  val tool: Tool,
  val resultType: SearchResultType,
  val categoryFilter: SearchCategoryFilter = SearchEngine.categoryFilterForTool(tool),
  val score: Int,
  val copyableValue: String? = null,
  val colorHex: String? = null,
  val detailRows: List<SearchResultDetailRow> = emptyList(),
)

/**
 * Pluggable data sources for user-created content across tools.
 * Can be backed by Android [android.content.SharedPreferences] or in-memory repositories in tests.
 */
data class SearchDataSources(
  val notesRepository: NotesRepository? = null,
  val todoListRepository: TodoListRepository? = null,
  val recipeBookRepository: RecipeBookRepository? = null,
  val plantScheduleRepository: PlantScheduleRepository? = null,
  val hiddenTools: Set<Tool> = emptySet(),
  val usageCounts: Map<Tool, Int> = emptyMap(),
)

/**
 * Universal Search Engine indexing all tools, live evaluators, persisted user data, and built-in catalogs.
 */
object SearchEngine {

  val DISCOVERY_SUGGESTIONS: List<SearchDiscoverySuggestion> = listOf(
    SearchDiscoverySuggestion("24 * 15 + 40", "24 × 15 + 40", Tool.CALCULATOR),
    SearchDiscoverySuggestion("100 km to mi", "100 km → mi", Tool.UNIT_CONVERTER),
    SearchDiscoverySuggestion("72 f to c", "72 °F → °C", Tool.UNIT_CONVERTER),
    SearchDiscoverySuggestion("#8E66A6", "Color #8E66A6", Tool.COLOR_PICKER),
    SearchDiscoverySuggestion("2d20+5", "Roll 2d20+5", Tool.DICE_ROLLER),
    SearchDiscoverySuggestion("P0420", "DTC P0420", Tool.OBD2_SCANNER),
    SearchDiscoverySuggestion("Pancakes", "Pancakes Recipe", Tool.RECIPES),
    SearchDiscoverySuggestion("Tomato", "Tomato Schedule", Tool.PLANT_SCHEDULE),
    SearchDiscoverySuggestion("Drop D", "Drop D Tuning", Tool.GUITAR_TUNER),
    SearchDiscoverySuggestion("Cmaj7", "Cmaj7 Chord", Tool.CHORD_BOOK),
    SearchDiscoverySuggestion("Dorian", "Dorian Scale", Tool.CHORD_BOOK),
    SearchDiscoverySuggestion("FLAC", "FLAC Format", Tool.FILE_CONVERTER),
  )

  val TOOL_METADATA: Map<Tool, ToolSearchMetadata> = mapOf(
    Tool.CALCULATOR to ToolSearchMetadata(
      tool = Tool.CALCULATOR,
      summary = "Scientific calculator with live expression evaluation & history",
      description = "Evaluate arithmetic, trigonometry (sin, cos, tan), logarithms (ln, log), factorials, powers, roots, percentages, and memory registers.",
      capabilities = listOf("Arithmetic & Parentheses", "Trigonometry (DEG/RAD)", "Logarithms & Powers", "Memory Registers (M+, M-, MR, MC)", "Calculation History"),
      keywords = listOf("calculate", "calculator", "math", "scientific", "algebra", "trig", "sin", "cos", "tan", "sqrt", "logarithm", "factorial", "percent", "equation", "formula"),
      sampleQueries = listOf("24 * 15 + 40", "sqrt(144) + 18", "sin(30)"),
    ),
    Tool.NOTES to ToolSearchMetadata(
      tool = Tool.NOTES,
      summary = "Rich text notes, interactive checklists, #labels & color cards",
      description = "Create and organize rich-formatted notes, checklists, custom #labels, pinned cards, archive storage, and color-coded note themes.",
      capabilities = listOf("Rich Text & Markdown Styling", "Interactive Checklists", "Custom #Labels & Filtering", "11 Color Swatch Themes", "Pin & Archive Management"),
      keywords = listOf("notes", "notepad", "memo", "checklist", "journal", "write", "text", "label", "pin", "archive", "markdown"),
      sampleQueries = listOf("Notes", "Checklist", "Coral"),
    ),
    Tool.TODO_LIST to ToolSearchMetadata(
      tool = Tool.TODO_LIST,
      summary = "Task manager with subtasks, due date scheduling & alarm reminders",
      description = "Track daily tasks, nested subtasks, scheduled due dates and times, alarm notifications, and reusable saved task templates.",
      capabilities = listOf("Nested Subtasks & Progress", "Date & Time Scheduling", "Alarm Notifications", "Saved Task Templates", "Completed Task History"),
      keywords = listOf("todo", "to do", "tasks", "checklist", "reminder", "schedule", "alarm", "subtask", "due date", "productivity"),
      sampleQueries = listOf("To Do", "Reminder", "Subtasks"),
    ),
    Tool.CAMERA to ToolSearchMetadata(
      tool = Tool.CAMERA,
      summary = "Photo/video camera, QR/barcode scanner & OCR document note scanner",
      description = "Capture photos and videos, scan QR codes and product barcodes (EAN, UPC, Code 128), and extract text from documents with OCR.",
      capabilities = listOf("Photo & Video Capture", "QR & Barcode Scanner", "OCR Document Text Extraction", "Flash, Grid & Aspect Ratio Controls", "Direct Save to Notes"),
      keywords = listOf("camera", "photo", "video", "qr", "barcode", "scanner", "ocr", "document scan", "lens", "flash", "selfie"),
      sampleQueries = listOf("QR Scanner", "OCR", "Barcode"),
    ),
    Tool.GALLERY to ToolSearchMetadata(
      tool = Tool.GALLERY,
      summary = "Photo & video album browser with EXIF inspector, trash & bulk actions",
      description = "Browse device photo and video albums, inspect EXIF metadata, manage trash bin, and launch Image or Video Editor directly.",
      capabilities = listOf("Album & Media Grid", "EXIF & Resolution Inspector", "Bulk Share, Move & Copy", "Trash Bin & Restore", "One-Tap Image/Video Editing"),
      keywords = listOf("gallery", "photos", "pictures", "videos", "albums", "media", "exif", "trash", "viewer", "slideshow"),
      sampleQueries = listOf("Gallery", "Albums", "EXIF"),
    ),
    Tool.FILES to ToolSearchMetadata(
      tool = Tool.FILES,
      summary = "File & storage explorer with multi-select, copy, move & tool handoff",
      description = "Browse internal storage volumes, search files and folders, sort by size/date/type, and open archives, PDFs, or media editors.",
      capabilities = listOf("Storage Volume Browser", "Multi-Select Copy, Move & Rename", "Folder Creation & File Search", "Direct Archive & PDF Handoff", "Storage Capacity Breakdown"),
      keywords = listOf("files", "file manager", "folder", "storage", "directory", "explorer", "download", "documents", "dcim", "copy", "move", "rename"),
      sampleQueries = listOf("Files", "Download", "Documents"),
    ),
    Tool.AUDIO_RECORDER to ToolSearchMetadata(
      tool = Tool.AUDIO_RECORDER,
      summary = "Voice & audio recorder with live waveform, M4A/WAV/OGG & mic selection",
      description = "Record voice memos and studio audio in M4A (AAC), WAV (16-bit PCM), or OGG (Opus) with live amplitude waveform and playback.",
      capabilities = listOf("M4A, WAV, AAC, OGG & 3GP Recording", "Microphone & Device Audio Input", "Live Amplitude Waveform", "Built-in Playback & Seek", "Direct Audio Editor Handoff"),
      keywords = listOf("record", "audio recorder", "voice memo", "microphone", "wav", "m4a", "ogg", "pcm", "dictaphone", "sound recording"),
      sampleQueries = listOf("WAV", "Microphone", "Voice Memo"),
    ),
    Tool.MUSIC to ToolSearchMetadata(
      tool = Tool.MUSIC,
      summary = "Background music player with playlists, favorites & dynamic album theming",
      description = "Play local audio tracks and built-in synth tracks with queue control, shuffle/repeat, custom playlists, favorites, and dynamic album art palettes.",
      capabilities = listOf("Background Playback & Mini-Player", "Custom Playlists & Favorites", "Dynamic Album Art Palette Extraction", "Track Search & Sorting", "Waveform & Progress Scrubbing"),
      keywords = listOf("music", "music player", "audio player", "songs", "tracks", "playlist", "album", "artist", "mp3", "favorites", "equalizer"),
      sampleQueries = listOf("Midnight Horizon", "Solaris Drift", "Music"),
    ),
    Tool.UNIT_CONVERTER to ToolSearchMetadata(
      tool = Tool.UNIT_CONVERTER,
      summary = "Multi-category unit converter for Length, Weight, Temp, Volume, Speed & Data",
      description = "Convert instantaneously across 45 metric, imperial, and digital units in 6 measurement categories with full conversion tables.",
      capabilities = listOf("Length (mm, cm, m, km, in, ft, yd, mi)", "Weight (mg, g, kg, t, oz, lb, st)", "Temperature (°C, °F, K)", "Volume, Speed & Digital Data (B to TiB)"),
      keywords = listOf("units", "unit converter", "convert", "length", "weight", "mass", "temperature", "volume", "speed", "data", "metric", "imperial", "celsius", "fahrenheit", "kilometer", "mile"),
      sampleQueries = listOf("100 km to mi", "72 f to c", "50 kg to lb"),
    ),
    Tool.RULER to ToolSearchMetadata(
      tool = Tool.RULER,
      summary = "Screen-edge ruler, dual caliper, pitch gauge, protractor & bubble level",
      description = "Measure real-world objects using display DPI calibration, interactive A↔B caliper, thread pitch gauge, 180° protractor, and 2-axis spirit level.",
      capabilities = listOf("Interactive A↔B Caliper", "Metric (cm/mm) & Imperial (in) Scales", "Thread & Line Pitch Gauge", "0°–180° Angle Protractor", "2-Axis Spirit Bubble Level & Reference Objects"),
      keywords = listOf("ruler", "measure", "caliper", "protractor", "angle", "bubble level", "spirit level", "pitch gauge", "millimeter", "inch", "centimeter"),
      sampleQueries = listOf("Caliper", "Credit Card", "Protractor"),
    ),
    Tool.GUITAR_TUNER to ToolSearchMetadata(
      tool = Tool.GUITAR_TUNER,
      summary = "Chromatic pitch tuner with 16+ guitar, bass & ukulele tuning presets",
      description = "Tune acoustic/electric guitar, 7-string, bass, and ukulele using real-time pitch detection, cents gauge, and reference tone synthesis.",
      capabilities = listOf("Real-Time Chromatic Pitch Detection", "16+ Alternate, Drop & Open Tunings", "Auto & Manual String Lock", "Synthesized Reference Tone Generator", "A4 Calibration (415–466 Hz)"),
      keywords = listOf("tuner", "guitar tuner", "pitch", "frequency", "hz", "cents", "drop d", "dadgad", "open g", "ukulele", "bass tuner", "chromatic"),
      sampleQueries = listOf("Drop D", "DADGAD", "Open G"),
    ),
    Tool.CHORD_BOOK to ToolSearchMetadata(
      tool = Tool.CHORD_BOOK,
      summary = "Interactive chord diagrams, 20 musical scales, progressions & 7 instruments",
      description = "Explore playable voicings across 31 chord qualities, 20 scales/modes, chord progressions, and 7 stringed instruments with acoustic synthesis.",
      capabilities = listOf("31 Chord Qualities & Voicing Diagrams", "20 Scales & Modes Explorer", "Interactive Fretboard & Strum Synthesizer", "Chord Progression Builder & Player", "7 Instruments (Guitar, Bass, Ukulele, Banjo, Mandolin)"),
      keywords = listOf("chords", "chord book", "scales", "fretboard", "guitar chords", "ukulele chords", "banjo", "mandolin", "progression", "pentatonic", "dorian", "arpeggio", "voicing"),
      sampleQueries = listOf("Cmaj7", "Dorian", "Royal Road"),
    ),
    Tool.BLUETOOTH_REMOTE to ToolSearchMetadata(
      tool = Tool.BLUETOOTH_REMOTE,
      summary = "Bluetooth HID TV/media remote with D-Pad, Air Mouse trackpad & keyboard",
      description = "Control smart TVs, PCs, and streaming boxes over Bluetooth HID with a directional pad, touchpad air mouse, media transport keys, and keyboard input.",
      capabilities = listOf("Bluetooth HID Remote Profile", "D-Pad & OK Navigation", "Touchpad & Gyro Air Mouse", "Media Transport & Volume Controls", "Wireless Keyboard Input"),
      keywords = listOf("remote", "bluetooth remote", "tv remote", "hid", "air mouse", "trackpad", "d-pad", "media control", "streaming"),
      sampleQueries = listOf("Remote", "Air Mouse", "D-Pad"),
    ),
    Tool.BLUETOOTH_GAMEPAD to ToolSearchMetadata(
      tool = Tool.BLUETOOTH_GAMEPAD,
      summary = "Landscape Bluetooth HID gamepad controller with dual sticks, ABXY & triggers",
      description = "Use your phone as a wireless Bluetooth HID game controller featuring dual thumbsticks, D-Pad, ABXY action buttons, and L1/R1/L2/R2 shoulder triggers.",
      capabilities = listOf("Dual Analog Thumbsticks", "ABXY Action Buttons & 8-Way D-Pad", "L1/R1 & L2/R2 Shoulder Triggers", "Haptic Force Feedback", "Bluetooth HID Gamepad Profile"),
      keywords = listOf("gamepad", "controller", "joystick", "bluetooth gamepad", "gaming", "abxy", "triggers", "thumbstick", "dpad"),
      sampleQueries = listOf("Gamepad", "Joystick", "ABXY"),
    ),
    Tool.RECIPES to ToolSearchMetadata(
      tool = Tool.RECIPES,
      summary = "Recipe book with 38+ built-in recipes, serving scaler & step checklist",
      description = "Browse 38+ curated recipes across 7 culinary categories, dynamically scale ingredient servings, track cooking steps, and save custom recipes.",
      capabilities = listOf("38+ Built-In Recipes Across 7 Categories", "Dynamic Serving Multiplier", "Interactive Ingredient & Step Checklists", "Custom Recipe Creator", "Prep Time, Cook Time & Difficulty Filters"),
      keywords = listOf("recipes", "recipe book", "cooking", "baking", "food", "ingredients", "meal", "dinner", "breakfast", "dessert", "kitchen"),
      sampleQueries = listOf("Pancakes", "Ramen", "Tiramisu"),
    ),
    Tool.PLANT_SCHEDULE to ToolSearchMetadata(
      tool = Tool.PLANT_SCHEDULE,
      summary = "Annual garden planting & harvest calendar with care & spacing guides",
      description = "Plan planting and harvest windows across 48+ vegetables, leafy greens, herbs, fruits, and flowers with seasonal month timelines.",
      capabilities = listOf("48+ Built-In Vegetables, Herbs, Fruits & Flowers", "12-Month Visual Planting & Harvest Timeline", "Sunlight, Spacing & Maturity Guides", "Current Month Activity Filter", "Custom Plant Entries"),
      keywords = listOf("plants", "plant schedule", "garden", "gardening", "vegetable", "herb", "fruit", "flower", "harvest", "sow", "transplant", "watering", "sunlight"),
      sampleQueries = listOf("Tomato", "Basil", "Strawberry"),
    ),
    Tool.DICE_ROLLER to ToolSearchMetadata(
      tool = Tool.DICE_ROLLER,
      summary = "Polyhedral RPG dice roller (d4–d100) with Advantage, modifiers & presets",
      description = "Roll d4, d6, d8, d10, d12, d20, and d100 polyhedral dice with custom modifiers, Advantage/Disadvantage, saved presets, and roll history.",
      capabilities = listOf("Polyhedral Dice (d4, d6, d8, d10, d12, d20, d100)", "Advantage & Disadvantage Modes", "Custom Modifiers & Multi-Dice Pools", "Saved RPG Presets", "Roll Breakdown & Statistics History"),
      keywords = listOf("dice", "dice roller", "rpg", "dnd", "d20", "d6", "d100", "polyhedral", "advantage", "disadvantage", "random", "tabletop"),
      sampleQueries = listOf("2d20+5", "4d6", "d20"),
    ),
    Tool.IMAGE_EDITOR to ToolSearchMetadata(
      tool = Tool.IMAGE_EDITOR,
      summary = "Non-destructive image editor with crop, rotate, color adjustments & filters",
      description = "Crop to standard aspect ratios, rotate and flip photos, tune brightness/contrast/saturation/warmth/vignette, apply filters, and export PNG/JPEG.",
      capabilities = listOf("Aspect-Ratio Crop & Straighten", "90° Rotate & Horizontal/Vertical Flip", "Brightness, Contrast, Saturation & Warmth", "Curated Color Filter Presets", "High-Resolution PNG & JPEG Export"),
      keywords = listOf("image editor", "photo editor", "crop", "rotate", "flip", "filter", "brightness", "contrast", "saturation", "warmth", "vignette"),
      sampleQueries = listOf("Image Editor", "Crop Photo", "Image Filter"),
    ),
    Tool.VIDEO_EDITOR to ToolSearchMetadata(
      tool = Tool.VIDEO_EDITOR,
      summary = "Timeline video editor with trim, playback speed, filters, mute & export",
      description = "Trim video start and end points, adjust playback speed (0.25x–2.0x), rotate orientation, mute audio, apply color grading, and export.",
      capabilities = listOf("Precision Start/End Timeline Trim", "Playback Speed Control (0.25x–2.0x)", "Video Rotation & Audio Track Mute", "Color Filter Grading & Resolution Scaling", "Still Frame Capture"),
      keywords = listOf("video editor", "trim video", "cut video", "playback speed", "slow motion", "mute video", "rotate video", "mp4"),
      sampleQueries = listOf("Video Editor", "Trim Video", "Slow Motion"),
    ),
    Tool.AUDIO_EDITOR to ToolSearchMetadata(
      tool = Tool.AUDIO_EDITOR,
      summary = "Waveform audio editor with trim, gain, fade in/out, normalize & speed",
      description = "Inspect audio waveforms, trim clip boundaries, apply gain boost/cut, shape fade-in and fade-out envelopes, peak-normalize, and export WAV.",
      capabilities = listOf("Visual Waveform Trim Handles", "Gain Boost & Attenuation (dB)", "Fade-In & Fade-Out Envelopes", "One-Tap Peak Normalization", "Tempo/Speed Adjustment & WAV Export"),
      keywords = listOf("audio editor", "sound editor", "waveform", "trim audio", "fade in", "fade out", "normalize", "gain", "wav export"),
      sampleQueries = listOf("Audio Editor", "Normalize Audio", "Fade In"),
    ),
    Tool.FILE_CONVERTER to ToolSearchMetadata(
      tool = Tool.FILE_CONVERTER,
      summary = "Offline file converter supporting 34 image, audio, video & document formats",
      description = "Batch convert between 34 file formats across Images (PNG, JPG, WEBP, SVG, AVIF, HEIC, PDF), Audio (WAV, MP3, FLAC, OGG, AAC, OPUS), Video, and Data/Archives.",
      capabilities = listOf("Image Formats (PNG, JPG, WEBP, SVG, HEIC, AVIF, TIFF, BMP, GIF, ICO)", "Audio Formats (WAV, MP3, FLAC, AAC, M4A, OGG, OPUS, AIFF, AMR)", "Video Containers (MP4, MKV, WEBM, MOV, AVI, 3GP)", "Document, Data & Archive Formats (PDF, JSON, XML, CSV, TSV, MD, HTML, TXT, ZIP, TAR)"),
      keywords = listOf("convert", "file converter", "format", "transcode", "png", "jpg", "webp", "svg", "mp3", "flac", "wav", "mp4", "mkv", "json", "csv", "pdf"),
      sampleQueries = listOf("FLAC", "WEBP", "JSON"),
    ),
    Tool.PDF_VIEWER to ToolSearchMetadata(
      tool = Tool.PDF_VIEWER,
      summary = "PDF reader & annotator with freehand ink, highlighter, text notes & export",
      description = "Open multi-page PDF documents, zoom and pan smoothly, draw with pen and highlighter tools, attach page text notes, and export annotated PDFs.",
      capabilities = listOf("Multi-Page PDF Rendering & Zoom", "Freehand Pen & Highlighter Ink", "Page Text Annotations", "Sample PDF Generator", "Flattened Annotated PDF Export"),
      keywords = listOf("pdf", "pdf viewer", "document", "reader", "annotate", "highlighter", "pen", "ebook", "markup"),
      sampleQueries = listOf("PDF Viewer", "Annotate PDF", "Highlighter"),
    ),
    Tool.ARCHIVE to ToolSearchMetadata(
      tool = Tool.ARCHIVE,
      summary = "Archive manager to inspect, extract & create ZIP, TAR, TAR.GZ & GZ bundles",
      description = "Browse archive contents, extract files to storage, and compress files or folders into .zip, .tar, .tar.gz (.tgz), and .gz archives.",
      capabilities = listOf("ZIP, TAR, TAR.GZ & GZIP Support", "In-Archive Directory Tree Explorer", "Selective or Full Extraction", "Configurable Compression Level", "Archive Integrity & Compression Ratio Stats"),
      keywords = listOf("archive", "archives", "zip", "unzip", "tar", "gzip", "tgz", "compress", "extract", "bundle"),
      sampleQueries = listOf("Archives", "ZIP", "TAR.GZ"),
    ),
    Tool.DECIBEL_METER to ToolSearchMetadata(
      tool = Tool.DECIBEL_METER,
      summary = "Real-time sound level meter (dB SPL) with peak/min/avg & calibration offset",
      description = "Measure environmental noise in decibels (0–120 dB SPL) with an animated arc gauge, rolling history graph, noise classification, and ±20 dB calibration.",
      capabilities = listOf("Real-Time dB SPL Gauge (0–120 dB)", "Min, Avg & Peak Tracking", "40-Point Rolling History Graph", "5 Environmental Noise Classifications", "±20 dB Microphone Calibration Offset"),
      keywords = listOf("decibels", "decibel meter", "sound meter", "noise", "db", "spl", "loudness", "acoustics", "microphone level"),
      sampleQueries = listOf("85 dB", "Decibels", "Quiet"),
    ),
    Tool.METRONOME to ToolSearchMetadata(
      tool = Tool.METRONOME,
      summary = "Precision audio/visual metronome (20–240 BPM) with tap tempo & subdivisions",
      description = "Keep time with an animated mechanical pendulum, custom time signatures, beat accent toggles, subdivisions (quarter, eighth, triplet, 16th), and tap tempo.",
      capabilities = listOf("20–240 BPM Range & Tap Tempo", "Animated Pendulum & Beat Indicators", "Standard & Custom Time Signatures (4/4, 3/4, 5/4, 6/8)", "Quarter, Eighth, Triplet & 16th Subdivisions", "Low-Latency PCM Click Synthesis"),
      keywords = listOf("metronome", "bpm", "tempo", "beat", "rhythm", "time signature", "subdivision", "triplet", "tap tempo", "allegro", "andante"),
      sampleQueries = listOf("120 BPM", "Allegro", "6/8"),
    ),
    Tool.COLOR_PICKER to ToolSearchMetadata(
      tool = Tool.COLOR_PICKER,
      summary = "2D HSV color picker, RGB/HSL/CMYK converter, harmonies & WCAG contrast",
      description = "Pick colors on a 2D saturation-value canvas, convert between HEX, RGB, HSV, HSL, and CMYK, generate 5 color harmonies, and verify WCAG contrast.",
      capabilities = listOf("2D Saturation-Value Canvas & Hue Bar", "HEX, RGB, HSV, HSL & CMYK Conversion", "5 Harmony Generators (Complementary, Analogous, Triadic, Split-Comp, Shades)", "WCAG AA/AAA Contrast Ratio Checker", "Named Color Swatches & Saved Palette"),
      keywords = listOf("colors", "color picker", "hex", "rgb", "hsv", "hsl", "cmyk", "palette", "swatch", "harmony", "complementary", "triadic", "contrast", "wcag"),
      sampleQueries = listOf("#8E66A6", "Emerald", "Triadic"),
    ),
    Tool.OBD2_SCANNER to ToolSearchMetadata(
      tool = Tool.OBD2_SCANNER,
      summary = "BLE ELM327 automotive scanner with live PID gauges, DTC lookup & VIN decoder",
      description = "Diagnose vehicles via Bluetooth Low Energy ELM327 adapters or simulation mode: monitor live RPM/speed/coolant/fuel trims, decode SAE J2012 DTCs, and check emissions readiness.",
      capabilities = listOf("Live Telemetry Gauges (RPM, Speed, Coolant, Load, Fuel Trim, O2)", "Comprehensive SAE J2012 DTC Trouble Code Catalog & Decoder", "MIL Status & I/M Emissions Readiness Monitors", "Freeze Frame Snapshot & Mode 04 Clear Codes", "VIN & Calibration ID Reader"),
      keywords = listOf("obd2", "obd", "car", "automotive", "dtc", "check engine", "trouble code", "elm327", "rpm", "coolant", "vin", "emissions", "p0420", "p0300"),
      sampleQueries = listOf("P0420", "P0300", "OBD2"),
    ),
    Tool.SEARCH to ToolSearchMetadata(
      tool = Tool.SEARCH,
      summary = "Universal search across all tools, calculators, converters, user data & catalogs",
      description = "Search and launch any tool, evaluate live math and unit conversions, inspect notes, tasks, recipes, plants, chords, scales, tunings, DTC trouble codes, file formats, and settings.",
      capabilities = listOf("Instant Tool Launcher (Visible & Hidden Tools)", "Live Calculator, Unit Converter, Color & Dice Evaluators", "Full-Text Search Across Notes, To-Do, Recipes & Plants", "Reference Search Across Chords, Scales, Tunings, OBD2 DTCs & 34 Formats", "Category & Per-Tool Filtering"),
      keywords = listOf("search", "find", "lookup", "universal search", "spotlight", "directory", "all tools", "query", "index"),
      sampleQueries = listOf("Calculate", "100 km to mi", "P0420"),
    ),
    Tool.SETTINGS to ToolSearchMetadata(
      tool = Tool.SETTINGS,
      summary = "App theme mode, dynamic colors, accent harmonies, tool sorting & backup",
      description = "Customize light/dark/system theme, wallpaper dynamic colors, automatic accent harmonies, music player theming, tool visibility, grid order, and full JSON backup/restore.",
      capabilities = listOf("Theme Mode (System, Light, Dark)", "Seed Theme Color & Music Player Dynamic Theme", "Automatic Accent Harmonies (Complementary, Analogous, Triadic, Split-Comp)", "Tool Visibility & Grid Sorting (Manual, Category, Usage)", "Full App JSON Backup & Restore"),
      keywords = listOf("settings", "preferences", "theme", "dark mode", "light mode", "accent color", "hidden tools", "visibility", "sorting", "backup", "restore", "reset"),
      sampleQueries = listOf("Dark Mode", "Backup", "Hidden Tools"),
    ),
  )

  /**
   * Builds [SearchDataSources] wired to Android [SharedPreferences] for the given [context].
   */
  fun createDataSources(
    context: Context,
    hiddenTools: Set<Tool> = emptySet(),
    usageCounts: Map<Tool, Int> = emptyMap(),
  ): SearchDataSources {
    return SearchDataSources(
      notesRepository = SharedPreferencesNotesRepository(context),
      todoListRepository = SharedPreferencesTodoListRepository(context),
      recipeBookRepository = SharedPreferencesRecipeBookRepository(context),
      plantScheduleRepository = SharedPreferencesPlantScheduleRepository(context),
      hiddenTools = hiddenTools,
      usageCounts = usageCounts,
    )
  }

  /**
   * Maps a [Tool] to its corresponding [SearchCategoryFilter] based on [Tool.category].
   */
  fun categoryFilterForTool(tool: Tool): SearchCategoryFilter {
    return when (tool.category) {
      ToolCategory.MATH_AND_MEASUREMENT -> SearchCategoryFilter.MATH_AND_MEASUREMENT
      ToolCategory.PRODUCTIVITY -> SearchCategoryFilter.PRODUCTIVITY
      ToolCategory.FILES_AND_STORAGE -> SearchCategoryFilter.FILES_AND_STORAGE
      ToolCategory.PHOTO_AND_VIDEO -> SearchCategoryFilter.PHOTO_AND_VIDEO
      ToolCategory.AUDIO_AND_SOUND -> SearchCategoryFilter.AUDIO_AND_SOUND
      ToolCategory.MUSIC_AND_INSTRUMENTS -> SearchCategoryFilter.MUSIC_AND_INSTRUMENTS
      ToolCategory.REMOTES_AND_DEVICES -> SearchCategoryFilter.REMOTES_AND_DEVICES
      ToolCategory.MISCELLANEOUS -> SearchCategoryFilter.MISCELLANEOUS
      ToolCategory.SYSTEM -> SearchCategoryFilter.SYSTEM
    }
  }

  /**
   * Executes a universal search across all tools, live evaluators, user repositories, and built-in catalogs.
   */
  fun search(
    query: String,
    categoryFilter: SearchCategoryFilter = SearchCategoryFilter.ALL,
    toolFilter: Tool? = null,
    dataSources: SearchDataSources = SearchDataSources(),
    maxResults: Int = 120,
  ): List<SearchResultItem> {
    val rawTrimmed = query.trim()
    if (rawTrimmed.isEmpty()) return emptyList()

    val normalizedQuery = rawTrimmed.lowercase(Locale.ROOT)
    val tokens = normalizedQuery
      .split(Regex("\\s+"))
      .map { it.trim() }
      .filter { it.isNotEmpty() }

    val allResults = mutableListOf<SearchResultItem>()

    // 1. Search all Tools themselves
    allResults.addAll(searchToolsCatalog(normalizedQuery, tokens, dataSources))

    // 2. Live Smart Evaluators (Calculator, Unit Converter, Color Hex, Dice Roll, Chord, BPM, dB)
    allResults.addAll(evaluateLiveCalculator(rawTrimmed))
    allResults.addAll(evaluateLiveUnitConversion(rawTrimmed))
    allResults.addAll(evaluateLiveColorHex(rawTrimmed))
    allResults.addAll(evaluateLiveDiceNotation(rawTrimmed))
    allResults.addAll(evaluateLiveChordLookup(rawTrimmed))
    allResults.addAll(evaluateLiveBpmAndDecibels(rawTrimmed))

    // 3. Persisted User Data (Notes, To-Do Tasks, Saved/Custom Recipes, Scheduled/Custom Plants)
    allResults.addAll(searchUserNotes(normalizedQuery, tokens, dataSources.notesRepository))
    allResults.addAll(searchUserTodos(normalizedQuery, tokens, dataSources.todoListRepository))

    // 4. Built-in + User Catalogs across all tools
    allResults.addAll(searchRecipes(normalizedQuery, tokens, dataSources.recipeBookRepository))
    allResults.addAll(searchPlants(normalizedQuery, tokens, dataSources.plantScheduleRepository))
    allResults.addAll(searchGuitarTunings(normalizedQuery, tokens))
    allResults.addAll(searchChordBookCatalog(normalizedQuery, tokens))
    allResults.addAll(searchMetronomeCatalog(normalizedQuery, tokens))
    allResults.addAll(searchUnitCatalog(normalizedQuery, tokens))
    allResults.addAll(searchRulerCatalog(normalizedQuery, tokens))
    allResults.addAll(searchDecibelCatalog(normalizedQuery, tokens))
    allResults.addAll(searchColorSwatchesAndHarmonies(normalizedQuery, tokens))
    allResults.addAll(searchObd2Catalog(rawTrimmed, normalizedQuery, tokens))
    allResults.addAll(searchFileConverterFormats(normalizedQuery, tokens))
    allResults.addAll(searchMusicTracks(normalizedQuery, tokens))
    allResults.addAll(searchAudioRecorderCatalog(normalizedQuery, tokens))
    allResults.addAll(searchCameraCatalog(normalizedQuery, tokens))
    allResults.addAll(searchDiceCatalog(normalizedQuery, tokens))
    allResults.addAll(searchRemotesAndMediaCapabilities(normalizedQuery, tokens))
    allResults.addAll(searchSettingsCatalog(normalizedQuery, tokens))

    return allResults
      .asSequence()
      .filter { item ->
        val matchesCategory = categoryFilter.toolCategory == null || item.tool.category == categoryFilter.toolCategory
        val matchesTool = toolFilter == null || item.tool == toolFilter
        matchesCategory && matchesTool
      }
      .sortedWith(
        compareByDescending<SearchResultItem> { it.score }
          .thenBy { if (it.resultType == SearchResultType.TOOL) 0 else 1 }
          .thenBy { it.tool.ordinal }
          .thenBy { it.title.lowercase(Locale.ROOT) }
      )
      .distinctBy { it.id }
      .take(maxResults)
      .toList()
  }

  /**
   * Computes the number of matching results per [Tool] for a given [query] (ignoring toolFilter)
   * so UI filter chips can display live per-tool result counts.
   */
  fun countResultsByTool(
    query: String,
    dataSources: SearchDataSources = SearchDataSources(),
  ): Map<Tool, Int> {
    if (query.isBlank()) return emptyMap()
    val results = search(
      query = query,
      categoryFilter = SearchCategoryFilter.ALL,
      toolFilter = null,
      dataSources = dataSources,
      maxResults = 500,
    )
    return results.groupingBy { it.tool }.eachCount()
  }

  private fun scoreTextMatch(
    normalizedQuery: String,
    tokens: List<String>,
    primaryText: String,
    secondaryText: String = "",
    keywordsText: String = "",
    baseWeight: Int = 100,
  ): Int {
    if (normalizedQuery.isEmpty()) return 0
    val primaryLower = primaryText.lowercase(Locale.ROOT)
    val secondaryLower = secondaryText.lowercase(Locale.ROOT)
    val keywordsLower = keywordsText.lowercase(Locale.ROOT)
    val combined = "$primaryLower $secondaryLower $keywordsLower"

    // Every query token must appear somewhere in the combined searchable text
    if (tokens.any { token -> !combined.contains(token) }) {
      return 0
    }

    var score = baseWeight
    when {
      primaryLower == normalizedQuery -> score += 260
      primaryLower.startsWith(normalizedQuery) -> score += 190
      primaryLower.split(Regex("[\\s•·/()_-]+")).any { it == normalizedQuery } -> score += 165
      primaryLower.contains(normalizedQuery) -> score += 130
      secondaryLower.contains(normalizedQuery) -> score += 85
      keywordsLower.contains(normalizedQuery) -> score += 60
    }

    for (token in tokens) {
      if (primaryLower.startsWith(token)) score += 25
      else if (primaryLower.contains(token)) score += 15
      else if (secondaryLower.contains(token)) score += 8
      else if (keywordsLower.contains(token)) score += 4
    }
    return score
  }

  private fun searchToolsCatalog(
    normalizedQuery: String,
    tokens: List<String>,
    dataSources: SearchDataSources,
  ): List<SearchResultItem> {
    return Tool.entries.mapNotNull { tool ->
      val meta = TOOL_METADATA[tool]
      val title = tool.title
      val categoryTitle = tool.category.title
      val summary = meta?.summary.orEmpty()
      val description = meta?.description.orEmpty()
      val capabilities = meta?.capabilities?.joinToString(" • ").orEmpty()
      val keywords = buildString {
        append(tool.name.lowercase(Locale.ROOT).replace('_', ' '))
        append(' ')
        append(categoryTitle)
        append(' ')
        append(meta?.keywords?.joinToString(" ").orEmpty())
        append(' ')
        append(capabilities)
        append(' ')
        append(description)
      }

      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = title,
        secondaryText = "$categoryTitle $summary",
        keywordsText = keywords,
        baseWeight = 180,
      )
      if (score <= 0) return@mapNotNull null

      val isHidden = tool in dataSources.hiddenTools
      val usageCount = dataSources.usageCounts[tool] ?: 0
      val visibilityLabel = if (isHidden) "Hidden in bar (tap to open)" else "Visible in navigation bar"

      SearchResultItem(
        id = "tool_${tool.name}",
        title = title,
        subtitle = "$categoryTitle • $summary",
        snippet = description,
        badgeLabel = "Tool",
        tool = tool,
        resultType = SearchResultType.TOOL,
        categoryFilter = categoryFilterForTool(tool),
        score = score + (usageCount.coerceAtMost(20)),
        detailRows = buildList {
          add(SearchResultDetailRow("Tool Name", title))
          add(SearchResultDetailRow("Category", categoryTitle))
          add(SearchResultDetailRow("Navigation Status", visibilityLabel))
          if (usageCount > 0) {
            add(SearchResultDetailRow("Times Opened", "$usageCount"))
          }
          if (meta != null && meta.capabilities.isNotEmpty()) {
            add(SearchResultDetailRow("Key Capabilities", meta.capabilities.joinToString("\n• ", prefix = "• ")))
          }
          if (meta != null && meta.sampleQueries.isNotEmpty()) {
            add(SearchResultDetailRow("Example Searches", meta.sampleQueries.joinToString(", ")))
          }
        },
      )
    }
  }

  private fun evaluateLiveCalculator(rawQuery: String): List<SearchResultItem> {
    val trimmed = rawQuery.trim()
    val hasMathOperatorOrFunction = trimmed.any { it in "+-*/×÷^%()!" } ||
      Regex("(?i)\\b(sin|cos|tan|sqrt|ln|log|pi)\\b").containsMatchIn(trimmed)
    val hasDigitOrConstant = trimmed.any { it.isDigit() } ||
      Regex("(?i)\\b(pi|e)\\b").containsMatchIn(trimmed)
    if (!hasMathOperatorOrFunction || !hasDigitOrConstant) return emptyList()

    val evalSuccess = CalculatorEngine.evaluateSafe(trimmed) as? EvaluationResult.Success ?: return emptyList()
    val evaluated = evalSuccess.formatted
    if (evaluated == trimmed) return emptyList()

    return listOf(
      SearchResultItem(
        id = "live_calc_$trimmed",
        title = "$trimmed = $evaluated",
        subtitle = "Instant Scientific Calculation • Tap to copy or open in Calculate",
        snippet = "Evaluated expression using CalculatorEngine (Double precision).",
        badgeLabel = "Instant Math",
        tool = Tool.CALCULATOR,
        resultType = SearchResultType.LIVE_CALCULATION,
        categoryFilter = SearchCategoryFilter.MATH_AND_MEASUREMENT,
        score = 520,
        copyableValue = evaluated,
        detailRows = listOf(
          SearchResultDetailRow("Expression", trimmed),
          SearchResultDetailRow("Result", evaluated),
          SearchResultDetailRow("Angle Mode", "Degrees (DEG)"),
          SearchResultDetailRow("Tool", Tool.CALCULATOR.title),
        ),
      )
    )
  }

  private val UNIT_CONVERSION_REGEX = Regex(
    pattern = "^\\s*([+-]?\\d+(?:\\.\\d+)?)\\s*([a-zA-Z°/]+)(?:\\s+(?:to|in|as|->|→)\\s+([a-zA-Z°/]+))?\\s*$",
    option = RegexOption.IGNORE_CASE,
  )

  private fun findMeasurementUnit(token: String): MeasurementUnit? {
    val cleaned = token.trim().lowercase(Locale.ROOT).removePrefix("°")
    if (cleaned.isEmpty()) return null
    val allUnits = UnitCategory.entries.flatMap { cat -> UnitCatalog.unitsFor(cat) }

    allUnits.firstOrNull { u ->
      u.symbol.lowercase(Locale.ROOT).removePrefix("°") == cleaned ||
        u.name.lowercase(Locale.ROOT) == cleaned ||
        "${u.name.lowercase(Locale.ROOT)}s" == cleaned
    }?.let { return it }

    val aliasMap = mapOf(
      "c" to "°C",
      "celsius" to "°C",
      "f" to "°F",
      "fahrenheit" to "°F",
      "k" to "K",
      "kelvin" to "K",
      "kms" to "km",
      "kilometer" to "km",
      "kilometers" to "km",
      "kilometre" to "km",
      "meter" to "m",
      "meters" to "m",
      "centimeter" to "cm",
      "centimeters" to "cm",
      "millimeter" to "mm",
      "millimeters" to "mm",
      "inch" to "in",
      "inches" to "in",
      "foot" to "ft",
      "feet" to "ft",
      "yard" to "yd",
      "yards" to "yd",
      "mile" to "mi",
      "miles" to "mi",
      "lbs" to "lb",
      "pound" to "lb",
      "pounds" to "lb",
      "ounce" to "oz",
      "ounces" to "oz",
      "gram" to "g",
      "grams" to "g",
      "kilogram" to "kg",
      "kilograms" to "kg",
      "liter" to "L",
      "liters" to "L",
      "milliliter" to "mL",
      "milliliters" to "mL",
      "gallon" to "gal",
      "gallons" to "gal",
    )
    val mappedSymbol = aliasMap[cleaned] ?: return null
    return allUnits.firstOrNull { it.symbol.equals(mappedSymbol, ignoreCase = true) }
  }

  private fun evaluateLiveUnitConversion(rawQuery: String): List<SearchResultItem> {
    val match = UNIT_CONVERSION_REGEX.matchEntire(rawQuery) ?: return emptyList()
    val amount = match.groupValues[1].toDoubleOrNull() ?: return emptyList()
    val fromToken = match.groupValues[2]
    val toToken = match.groupValues[3]

    val fromUnit = findMeasurementUnit(fromToken) ?: return emptyList()
    val categoryUnits = UnitCatalog.unitsFor(fromUnit.category)
    val amountFormatted = UnitCatalog.formatNumber(amount)

    if (toToken.isNotBlank()) {
      val toUnit = findMeasurementUnit(toToken)
      if (toUnit != null && toUnit.category == fromUnit.category) {
        val converted = UnitCatalog.convert(amount, fromUnit, toUnit)
        val convertedFormatted = UnitCatalog.formatNumber(converted)
        val allPeers = categoryUnits
          .filter { it != fromUnit }
          .map { peer ->
            SearchResultDetailRow(
              label = "${peer.name} (${peer.symbol})",
              value = "${UnitCatalog.formatNumber(UnitCatalog.convert(amount, fromUnit, peer))} ${peer.symbol}",
            )
          }
        return listOf(
          SearchResultItem(
            id = "live_unit_${fromUnit.symbol}_${toUnit.symbol}_$amountFormatted",
            title = "$amountFormatted ${fromUnit.symbol} = $convertedFormatted ${toUnit.symbol}",
            subtitle = "${fromUnit.category.label} Conversion • ${fromUnit.name} to ${toUnit.name}",
            snippet = "Converted using ${Tool.UNIT_CONVERTER.title} (${fromUnit.category.label}).",
            badgeLabel = "Instant Conversion",
            tool = Tool.UNIT_CONVERTER,
            resultType = SearchResultType.LIVE_CONVERSION,
            categoryFilter = SearchCategoryFilter.MATH_AND_MEASUREMENT,
            score = 515,
            copyableValue = "$convertedFormatted ${toUnit.symbol}",
            detailRows = listOf(
              SearchResultDetailRow("Input", "$amountFormatted ${fromUnit.name} (${fromUnit.symbol})"),
              SearchResultDetailRow("Converted", "$convertedFormatted ${toUnit.name} (${toUnit.symbol})"),
              SearchResultDetailRow("Category", fromUnit.category.label),
            ) + allPeers,
          )
        )
      }
    }

    val peers = categoryUnits.filter { it != fromUnit }
    if (peers.isEmpty()) return emptyList()
    val primaryPeer = peers.first()
    val primaryConverted = UnitCatalog.formatNumber(UnitCatalog.convert(amount, fromUnit, primaryPeer))
    val summaryPeers = peers.take(4).joinToString(" • ") { peer ->
      "${UnitCatalog.formatNumber(UnitCatalog.convert(amount, fromUnit, peer))} ${peer.symbol}"
    }

    return listOf(
      SearchResultItem(
        id = "live_unit_multi_${fromUnit.symbol}_$amountFormatted",
        title = "$amountFormatted ${fromUnit.symbol} = $primaryConverted ${primaryPeer.symbol}",
        subtitle = "${fromUnit.category.label} (${fromUnit.name}) • $summaryPeers",
        snippet = "Tap to view all ${peers.size} converted ${fromUnit.category.label.lowercase(Locale.ROOT)} units.",
        badgeLabel = "Unit Table",
        tool = Tool.UNIT_CONVERTER,
        resultType = SearchResultType.LIVE_CONVERSION,
        categoryFilter = SearchCategoryFilter.MATH_AND_MEASUREMENT,
        score = 490,
        copyableValue = "$primaryConverted ${primaryPeer.symbol}",
        detailRows = listOf(
          SearchResultDetailRow("Input Value", "$amountFormatted ${fromUnit.name} (${fromUnit.symbol})"),
          SearchResultDetailRow("Category", fromUnit.category.label),
        ) + peers.map { peer ->
          SearchResultDetailRow(
            label = "${peer.name} (${peer.symbol})",
            value = "${UnitCatalog.formatNumber(UnitCatalog.convert(amount, fromUnit, peer))} ${peer.symbol}",
          )
        },
      )
    )
  }

  private fun evaluateLiveColorHex(rawQuery: String): List<SearchResultItem> {
    val trimmed = rawQuery.trim()
    val isExplicitHex = trimmed.startsWith("#") && (trimmed.length == 4 || trimmed.length == 7)
    val isSixCharHex = trimmed.length == 6 && trimmed.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' } &&
      trimmed.any { it.isDigit() }
    if (!isExplicitHex && !isSixCharHex) return emptyList()

    val rgb = ColorMath.parseHex(trimmed) ?: return emptyList()
    val (r, g, b) = rgb
    val canonicalHex = ColorMath.toHex(r, g, b)
    val hsv = ColorMath.rgbToHsv(r, g, b)
    val hsl = ColorMath.rgbToHsl(r, g, b)
    val cmyk = ColorMath.rgbToCmyk(r, g, b)
    val closestName = ColorMath.closestColorName(r, g, b)
    val lum = ColorMath.relativeLuminance(r, g, b)
    val contrastWhite = ColorMath.contrastRatio(lum, 1.0)
    val contrastBlack = ColorMath.contrastRatio(lum, 0.0)
    val complementaryHex = ColorMath.generateHarmony(hsv, HarmonyMode.COMPLEMENTARY)
      .drop(1)
      .firstOrNull()
      ?.hexString
      ?: canonicalHex

    return listOf(
      SearchResultItem(
        id = "live_color_$canonicalHex",
        title = "$canonicalHex ($closestName)",
        subtitle = "RGB($r, $g, $b) • HSL(${hsl.hue}°, ${hsl.saturationPercent}%, ${hsl.lightnessPercent}%)",
        snippet = "CMYK(${cmyk.c}%, ${cmyk.m}%, ${cmyk.y}%, ${cmyk.k}%) • Complementary: $complementaryHex",
        badgeLabel = "Live Color",
        tool = Tool.COLOR_PICKER,
        resultType = SearchResultType.LIVE_COLOR,
        score = 510,
        copyableValue = canonicalHex,
        colorHex = canonicalHex,
        detailRows = listOf(
          SearchResultDetailRow("HEX", canonicalHex),
          SearchResultDetailRow("Closest Named Swatch", closestName),
          SearchResultDetailRow("RGB", "$r, $g, $b"),
          SearchResultDetailRow("HSV", "${hsv.hue.roundToInt()}°, ${(hsv.saturation * 100).roundToInt()}%, ${(hsv.value * 100).roundToInt()}%"),
          SearchResultDetailRow("HSL", "${hsl.hue}°, ${hsl.saturationPercent}%, ${hsl.lightnessPercent}%"),
          SearchResultDetailRow("CMYK", "${cmyk.c}%, ${cmyk.m}%, ${cmyk.y}%, ${cmyk.k}%"),
          SearchResultDetailRow("Contrast vs White", String.format(Locale.US, "%.2f:1 (%s)", contrastWhite, ColorMath.wcagRating(contrastWhite))),
          SearchResultDetailRow("Contrast vs Black", String.format(Locale.US, "%.2f:1 (%s)", contrastBlack, ColorMath.wcagRating(contrastBlack))),
          SearchResultDetailRow("Complementary Color", complementaryHex),
        ),
      )
    )
  }

  private val DICE_NOTATION_REGEX = Regex(
    pattern = "^\\s*(\\d{0,2})d(4|6|8|10|12|20|100)(?:\\s*([+-])\\s*(\\d+))?\\s*$",
    option = RegexOption.IGNORE_CASE,
  )

  private fun evaluateLiveDiceNotation(rawQuery: String): List<SearchResultItem> {
    val match = DICE_NOTATION_REGEX.matchEntire(rawQuery) ?: return emptyList()
    val count = match.groupValues[1].ifEmpty { "1" }.toIntOrNull()?.coerceIn(1, 50) ?: 1
    val sides = match.groupValues[2].toIntOrNull() ?: return emptyList()
    val sign = match.groupValues[3]
    val modMag = match.groupValues[4].toIntOrNull() ?: 0
    val modifier = when (sign) {
      "+" -> modMag
      "-" -> -modMag
      else -> 0
    }
    val dieType = DieType.entries.firstOrNull { it.sides == sides } ?: return emptyList()
    val minTotal = count * 1 + modifier
    val maxTotal = count * sides + modifier
    val averageTotal = count * ((sides + 1) / 2.0) + modifier
    val modSuffix = when {
      modifier > 0 -> "+$modifier"
      modifier < 0 -> "$modifier"
      else -> ""
    }
    val notation = "${count}${dieType.label}$modSuffix"

    return listOf(
      SearchResultItem(
        id = "live_dice_$notation",
        title = "Roll $notation (Range $minTotal–$maxTotal)",
        subtitle = String.format(Locale.US, "Average: %.1f • %d × %s%s", averageTotal, count, dieType.label.uppercase(Locale.ROOT), if (modifier != 0) " ($modSuffix)" else ""),
        snippet = "Polyhedral RPG dice formula for ${Tool.DICE_ROLLER.title}.",
        badgeLabel = "Dice Formula",
        tool = Tool.DICE_ROLLER,
        resultType = SearchResultType.LIVE_DICE_ROLL,
        score = 505,
        copyableValue = notation,
        detailRows = listOf(
          SearchResultDetailRow("Notation", notation),
          SearchResultDetailRow("Die Type", "${dieType.label.uppercase(Locale.ROOT)} ($sides-sided)"),
          SearchResultDetailRow("Dice Count", "$count"),
          SearchResultDetailRow("Modifier", if (modifier >= 0) "+$modifier" else "$modifier"),
          SearchResultDetailRow("Minimum Roll", "$minTotal"),
          SearchResultDetailRow("Maximum Roll", "$maxTotal"),
          SearchResultDetailRow("Expected Average", String.format(Locale.US, "%.2f", averageTotal)),
        ),
      )
    )
  }

  private fun evaluateLiveChordLookup(rawQuery: String): List<SearchResultItem> {
    val trimmed = rawQuery.trim()
    if (trimmed.isEmpty() || trimmed.length > 14) return emptyList()

    val rootSpellings = listOf(
      "C#" to 1, "Db" to 1, "D#" to 3, "Eb" to 3, "F#" to 6, "Gb" to 6,
      "G#" to 8, "Ab" to 8, "A#" to 10, "Bb" to 10,
      "C" to 0, "D" to 2, "E" to 4, "F" to 5, "G" to 7, "A" to 9, "B" to 11,
    )

    for ((rootName, pitchClass) in rootSpellings) {
      if (trimmed.startsWith(rootName, ignoreCase = true)) {
        val remainder = trimmed.substring(rootName.length).trim()
        val quality = ChordQuality.entries.firstOrNull { q ->
          q.symbol.equals(remainder, ignoreCase = true) ||
            q.fullName.equals(remainder, ignoreCase = true) ||
            (remainder.equals("major", ignoreCase = true) && q == ChordQuality.MAJOR) ||
            (remainder.equals("minor", ignoreCase = true) && q == ChordQuality.MINOR) ||
            (remainder.equals("min", ignoreCase = true) && q == ChordQuality.MINOR) ||
            (remainder.equals("maj", ignoreCase = true) && q == ChordQuality.MAJOR)
        }
        if (quality != null && remainder.isNotEmpty()) {
          val displayRoot = ChordBookEngine.formatPitchClass(pitchClass, EnharmonicPreference.AUTO, pitchClass)
          val chordNotes = quality.intervals.map { semitones ->
            ChordBookEngine.formatPitchClass((pitchClass + semitones).mod(12), EnharmonicPreference.AUTO, pitchClass)
          }
          val intervalLabels = quality.intervals.map { semitones ->
            ChordBookEngine.intervalName(semitones, quality)
          }
          val chordSymbol = ChordBookEngine.formatChordSymbol(pitchClass, quality)
          val chordTitle = "$chordSymbol ($displayRoot ${quality.fullName})"
          return listOf(
            SearchResultItem(
              id = "live_chord_${pitchClass}_${quality.name}",
              title = chordTitle,
              subtitle = "Notes: ${chordNotes.joinToString(" – ")} • Intervals: ${intervalLabels.joinToString(" – ")}",
              snippet = "Playable across Guitar, Bass, Ukulele, Banjo & Mandolin in ${Tool.CHORD_BOOK.title}.",
              badgeLabel = "Chord Voicing",
              tool = Tool.CHORD_BOOK,
              resultType = SearchResultType.CHORD,
              score = 495,
              copyableValue = chordNotes.joinToString(" "),
              detailRows = listOf(
                SearchResultDetailRow("Chord", chordTitle),
                SearchResultDetailRow("Root Note", displayRoot),
                SearchResultDetailRow("Quality", quality.fullName),
                SearchResultDetailRow("Chord Tones", chordNotes.joinToString(" – ")),
                SearchResultDetailRow("Interval Formula", intervalLabels.joinToString(" – ")),
                SearchResultDetailRow("Semitone Steps", quality.intervals.joinToString(", ")),
              ),
            )
          )
        }
      }
    }
    return emptyList()
  }

  private val BPM_QUERY_REGEX = Regex("^\\s*(\\d{2,3})\\s*bpm\\s*$", RegexOption.IGNORE_CASE)
  private val DB_QUERY_REGEX = Regex("^\\s*(\\d{1,3}(?:\\.\\d+)?)\\s*db(?:\\s*spl)?\\s*$", RegexOption.IGNORE_CASE)

  private fun evaluateLiveBpmAndDecibels(rawQuery: String): List<SearchResultItem> {
    val results = mutableListOf<SearchResultItem>()

    BPM_QUERY_REGEX.matchEntire(rawQuery)?.let { match ->
      val bpm = match.groupValues[1].toIntOrNull()?.coerceIn(20, 240)
      if (bpm != null) {
        val marking = tempoMarkingForBpm(bpm)
        val msPerBeat = (60_000.0 / bpm).roundToInt()
        results.add(
          SearchResultItem(
            id = "live_bpm_$bpm",
            title = "$bpm BPM ($marking)",
            subtitle = "Beat Interval: $msPerBeat ms/beat • Sixteenth: ${(msPerBeat / 4.0).roundToInt()} ms",
            snippet = "Open ${Tool.METRONOME.title} to start click synthesizer at $bpm BPM.",
            badgeLabel = "Metronome Tempo",
            tool = Tool.METRONOME,
            resultType = SearchResultType.TEMPO,
            score = 500,
            copyableValue = "$bpm BPM",
            detailRows = listOf(
              SearchResultDetailRow("Tempo", "$bpm BPM"),
              SearchResultDetailRow("Classical Marking", marking),
              SearchResultDetailRow("Quarter Note Interval", "$msPerBeat ms"),
              SearchResultDetailRow("Eighth Note Interval", "${(msPerBeat / 2.0).roundToInt()} ms"),
              SearchResultDetailRow("Triplet Interval", "${(msPerBeat / 3.0).roundToInt()} ms"),
              SearchResultDetailRow("Sixteenth Note Interval", "${(msPerBeat / 4.0).roundToInt()} ms"),
            ),
          )
        )
      }
    }

    DB_QUERY_REGEX.matchEntire(rawQuery)?.let { match ->
      val db = match.groupValues[1].toFloatOrNull()?.coerceIn(0f, 120f)
      if (db != null) {
        val category = NoiseLevelCategory.entries.firstOrNull { db >= it.minDb && db <= it.maxDb }
          ?: NoiseLevelCategory.EXTREME
        results.add(
          SearchResultItem(
            id = "live_db_${db.roundToInt()}",
            title = "${String.format(Locale.US, "%.1f", db)} dB SPL — ${category.label}",
            subtitle = "${category.description} (${category.rangeText})",
            snippet = "Environmental sound pressure level classification in ${Tool.DECIBEL_METER.title}.",
            badgeLabel = "Sound Level",
            tool = Tool.DECIBEL_METER,
            resultType = SearchResultType.DECIBEL_LEVEL,
            categoryFilter = SearchCategoryFilter.MATH_AND_MEASUREMENT,
            score = 500,
            copyableValue = "${String.format(Locale.US, "%.1f", db)} dB",
            detailRows = listOf(
              SearchResultDetailRow("Sound Level", "${String.format(Locale.US, "%.1f", db)} dB SPL"),
              SearchResultDetailRow("Classification", category.label),
              SearchResultDetailRow("Typical Environment", category.description),
              SearchResultDetailRow("Category Range", category.rangeText),
            ),
          )
        )
      }
    }

    return results
  }

  private fun tempoMarkingForBpm(bpm: Int): String = when {
    bpm < 40 -> "Grave"
    bpm < 60 -> "Largo"
    bpm < 66 -> "Larghetto"
    bpm < 76 -> "Adagio"
    bpm < 108 -> "Andante"
    bpm < 120 -> "Moderato"
    bpm < 156 -> "Allegro"
    bpm < 176 -> "Vivace"
    bpm < 200 -> "Presto"
    else -> "Prestissimo"
  }

  private fun searchUserNotes(
    normalizedQuery: String,
    tokens: List<String>,
    notesRepository: NotesRepository?,
  ): List<SearchResultItem> {
    val results = mutableListOf<SearchResultItem>()
    val notes = notesRepository?.loadNotes().orEmpty()
    for (note in notes) {
      val checklistText = note.checklistItems.joinToString(" ") { it.text }
      val labelsText = note.labels.joinToString(" ") { "#$it" }
      val title = note.title.ifBlank { "Untitled Note" }
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = title,
        secondaryText = "${note.content} $checklistText",
        keywordsText = "$labelsText ${note.color.displayName} note notes checklist",
        baseWeight = 160,
      )
      if (score > 0) {
        val statusParts = buildList {
          if (note.isPinned) add("Pinned")
          if (note.isArchived) add("Archived")
          if (note.isChecklist) add("${note.checklistItems.count { it.isChecked }}/${note.checklistItems.size} checked")
          if (note.labels.isNotEmpty()) add(labelsText)
        }
        val subtitle = statusParts.joinToString(" • ").ifEmpty { "Note (${note.color.displayName})" }
        val preview = if (note.isChecklist && note.checklistItems.isNotEmpty()) {
          note.checklistItems.joinToString(", ") { (if (it.isChecked) "☑ " else "☐ ") + it.text }
        } else {
          note.content.ifBlank { "Empty note" }
        }
        results.add(
          SearchResultItem(
            id = "user_note_${note.id}",
            title = title,
            subtitle = subtitle,
            snippet = preview.take(180),
            badgeLabel = if (note.isPinned) "Pinned Note" else "Note",
            tool = Tool.NOTES,
            resultType = SearchResultType.NOTE,
            categoryFilter = SearchCategoryFilter.PRODUCTIVITY,
            score = score + (if (note.isPinned) 20 else 0),
            copyableValue = preview,
            detailRows = buildList {
              add(SearchResultDetailRow("Title", title))
              add(SearchResultDetailRow("Color Theme", note.color.displayName))
              if (note.labels.isNotEmpty()) {
                add(SearchResultDetailRow("Labels", labelsText))
              }
              add(SearchResultDetailRow("Content", preview))
            },
          )
        )
      }
    }

    for (color in NoteColor.entries) {
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = "${color.displayName} Note Theme",
        secondaryText = "Notes color card swatch",
        keywordsText = "note color ${color.displayName}",
        baseWeight = 95,
      )
      if (score > 0) {
        results.add(
          SearchResultItem(
            id = "note_color_${color.name}",
            title = "${color.displayName} Note Card Color",
            subtitle = "Notes Tool • Card background color theme",
            snippet = "Organize and filter notes by the ${color.displayName} card swatch in ${Tool.NOTES.title}.",
            badgeLabel = "Note Theme",
            tool = Tool.NOTES,
            resultType = SearchResultType.NOTE,
            categoryFilter = SearchCategoryFilter.PRODUCTIVITY,
            score = score,
          )
        )
      }
    }
    return results
  }

  private fun searchUserTodos(
    normalizedQuery: String,
    tokens: List<String>,
    todoRepository: TodoListRepository?,
  ): List<SearchResultItem> {
    if (todoRepository == null) return emptyList()
    val results = mutableListOf<SearchResultItem>()
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.US)

    val activeTasks = todoRepository.loadTasks().map { it to "Active Task" }
    val completedTasks = todoRepository.loadCompletedTasks().map { it to "Completed Task" }
    val savedTemplates = todoRepository.loadSavedTasks().map { it to "Saved Template" }

    for ((task, bucketLabel) in (activeTasks + completedTasks + savedTemplates)) {
      val subTasksText = task.subtasks.joinToString(" ") { it.title }
      val scheduleText = task.scheduledAtMillis?.let { dateFormat.format(Date(it)) }.orEmpty()
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = task.title,
        secondaryText = "${task.notes} $subTasksText",
        keywordsText = "$bucketLabel $scheduleText todo task checklist",
        baseWeight = 160,
      )
      if (score > 0) {
        val subtitleParts = buildList {
          add(bucketLabel)
          if (task.subtasks.isNotEmpty()) {
            add("${task.subtasks.count { it.isCompleted }}/${task.subtasks.size} subtasks")
          }
          if (scheduleText.isNotBlank()) {
            add(scheduleText)
          }
        }
        results.add(
          SearchResultItem(
            id = "user_todo_${bucketLabel}_${task.id}",
            title = task.title,
            subtitle = subtitleParts.joinToString(" • "),
            snippet = task.notes.ifBlank {
              if (task.subtasks.isNotEmpty()) {
                "Subtasks: " + task.subtasks.joinToString(", ") { it.title }
              } else {
                "$bucketLabel in ${Tool.TODO_LIST.title}"
              }
            },
            badgeLabel = bucketLabel,
            tool = Tool.TODO_LIST,
            resultType = SearchResultType.TODO_TASK,
            categoryFilter = SearchCategoryFilter.PRODUCTIVITY,
            score = score,
            copyableValue = task.title,
            detailRows = buildList {
              add(SearchResultDetailRow("Task", task.title))
              add(SearchResultDetailRow("Status", bucketLabel))
              if (scheduleText.isNotBlank()) {
                add(SearchResultDetailRow("Scheduled", scheduleText))
              }
              if (task.notes.isNotBlank()) {
                add(SearchResultDetailRow("Notes", task.notes))
              }
              if (task.subtasks.isNotEmpty()) {
                add(
                  SearchResultDetailRow(
                    "Subtasks",
                    task.subtasks.joinToString("\n") { (if (it.isCompleted) "☑ " else "☐ ") + it.title },
                  )
                )
              }
            },
          )
        )
      }
    }
    return results
  }

  private fun searchRecipes(
    normalizedQuery: String,
    tokens: List<String>,
    recipeRepository: RecipeBookRepository?,
  ): List<SearchResultItem> {
    val saved = recipeRepository?.getSavedRecipes().orEmpty()
    val customCatalog = recipeRepository?.getCustomCatalogRecipes().orEmpty()
    val allRecipes = (saved + customCatalog + RecipeBookEngine.BUILT_IN_RECIPES)
      .distinctBy { it.id }

    return allRecipes.mapNotNull { recipe ->
      val ingredientsText = recipe.ingredients.joinToString(", ")
      val tagsText = recipe.tags.joinToString(" ")
      val stepsText = recipe.instructions.joinToString(" ")
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = recipe.name,
        secondaryText = "${recipe.subtitle} ${recipe.category.displayName} $ingredientsText",
        keywordsText = "$tagsText ${recipe.difficulty.displayName} $stepsText recipe cooking",
        baseWeight = 135,
      )
      if (score <= 0) return@mapNotNull null

      SearchResultItem(
        id = "recipe_${recipe.id}",
        title = recipe.name,
        subtitle = "${recipe.category.displayName} • ${recipe.totalTimeMinutes} min • ${recipe.difficulty.displayName}",
        snippet = "${recipe.subtitle} — Ingredients: ${recipe.ingredients.take(4).joinToString(", ")}",
        badgeLabel = if (recipe.isCustom) "Custom Recipe" else "Recipe",
        tool = Tool.RECIPES,
        resultType = SearchResultType.RECIPE,
        score = score,
        detailRows = listOf(
          SearchResultDetailRow("Recipe", recipe.name),
          SearchResultDetailRow("Category", recipe.category.displayName),
          SearchResultDetailRow("Difficulty", recipe.difficulty.displayName),
          SearchResultDetailRow("Prep / Cook Time", "${recipe.prepTimeMinutes}m prep • ${recipe.cookTimeMinutes}m cook (${recipe.totalTimeMinutes}m total)"),
          SearchResultDetailRow("Default Servings", "${recipe.servings}"),
          SearchResultDetailRow(
            "Ingredients (${recipe.ingredients.size})",
            recipe.ingredients.joinToString("\n") { "• $it" },
          ),
          SearchResultDetailRow(
            "Instructions (${recipe.instructions.size} steps)",
            recipe.instructions.mapIndexed { idx, step -> "${idx + 1}. $step" }.joinToString("\n"),
          ),
        ),
      )
    }
  }

  private fun searchPlants(
    normalizedQuery: String,
    tokens: List<String>,
    plantRepository: PlantScheduleRepository?,
  ): List<SearchResultItem> {
    val scheduled = plantRepository?.loadScheduledPlants().orEmpty()
    val custom = plantRepository?.loadCustomPlants().orEmpty()
    val allPlants = (scheduled + custom + PlantScheduleEngine.BUILT_IN_PLANTS)
      .distinctBy { it.id }

    return allPlants.mapNotNull { plant ->
      val plantingSummary = plant.plantingSummary()
      val harvestSummary = plant.harvestSummary()

      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = plant.name,
        secondaryText = "${plant.scientificName} ${plant.category.displayName} ${plant.sunRequirement}",
        keywordsText = "${plant.notes} plant garden $plantingSummary $harvestSummary",
        baseWeight = 135,
      )
      if (score <= 0) return@mapNotNull null

      SearchResultItem(
        id = "plant_${plant.id}",
        title = "${plant.name} (${plant.scientificName})",
        subtitle = "${plant.category.displayName} • ${plant.sunRequirement} • Maturity: ${plant.daysToMaturity}",
        snippet = "Planting: $plantingSummary • Harvest: $harvestSummary • Spacing: ${plant.spacing}",
        badgeLabel = "Plant Guide",
        tool = Tool.PLANT_SCHEDULE,
        resultType = SearchResultType.PLANT,
        score = score,
        detailRows = listOf(
          SearchResultDetailRow("Plant Name", plant.name),
          SearchResultDetailRow("Scientific Name", plant.scientificName),
          SearchResultDetailRow("Category", plant.category.displayName),
          SearchResultDetailRow("Sunlight", plant.sunRequirement),
          SearchResultDetailRow("Days to Maturity", plant.daysToMaturity),
          SearchResultDetailRow("Spacing", plant.spacing),
          SearchResultDetailRow("Planting Window", plantingSummary),
          SearchResultDetailRow("Harvest Window", harvestSummary),
          SearchResultDetailRow("Care Notes", plant.notes),
        ),
      )
    }
  }

  private fun searchGuitarTunings(
    normalizedQuery: String,
    tokens: List<String>,
  ): List<SearchResultItem> {
    return GuitarTuningPreset.entries.mapNotNull { preset ->
      val notesText = preset.strings.joinToString(" ") { "${it.noteName}${it.octave}" }
      val plainNotes = preset.strings.joinToString("-") { it.noteName }
      val freqsText = preset.strings.joinToString(", ") { "${String.format(Locale.US, "%.1f", it.frequencyHz)} Hz" }
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = preset.label,
        secondaryText = "${preset.category.displayName} $notesText $plainNotes",
        keywordsText = "guitar tuner tuning ${preset.keywords} ${preset.name.replace('_', ' ')}",
        baseWeight = 130,
      )
      if (score <= 0) return@mapNotNull null

      SearchResultItem(
        id = "guitar_tuning_${preset.name}",
        title = "${preset.label} Tuning",
        subtitle = "${preset.category.displayName} • Strings: $notesText",
        snippet = "Frequencies: $freqsText",
        badgeLabel = "Tuner Preset",
        tool = Tool.GUITAR_TUNER,
        resultType = SearchResultType.TUNING,
        score = score,
        copyableValue = notesText,
        detailRows = listOf(
          SearchResultDetailRow("Tuning Preset", preset.label),
          SearchResultDetailRow("Category", preset.category.displayName),
          SearchResultDetailRow("String Notes", notesText),
          SearchResultDetailRow("Target Frequencies", freqsText),
        ),
      )
    }
  }

  private fun searchChordBookCatalog(
    normalizedQuery: String,
    tokens: List<String>,
  ): List<SearchResultItem> {
    val results = mutableListOf<SearchResultItem>()

    // 1. Chord Qualities
    for (quality in ChordQuality.entries) {
      val cNotes = quality.intervals.map { semitones ->
        ChordBookEngine.formatPitchClass(semitones.mod(12), EnharmonicPreference.SHARPS)
      }
      val intervalLabels = quality.intervals.map { semitones ->
        ChordBookEngine.intervalName(semitones, quality)
      }
      val displaySymbol = if (quality.symbol.isEmpty()) "Major" else quality.symbol
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = "${quality.fullName} Chord ($displaySymbol)",
        secondaryText = "Intervals: ${intervalLabels.joinToString(" ")} • C${quality.symbol}: ${cNotes.joinToString(" ")}",
        keywordsText = "chord quality voicing triad seventh ${quality.name}",
        baseWeight = 125,
      )
      if (score > 0) {
        results.add(
          SearchResultItem(
            id = "chord_quality_${quality.name}",
            title = "${quality.fullName} Chord ($displaySymbol)",
            subtitle = "Formula: ${intervalLabels.joinToString(" – ")} • Example (C${quality.symbol}): ${cNotes.joinToString(" – ")}",
            snippet = "Interactive fretboard diagrams & voice synthesis in ${Tool.CHORD_BOOK.title}.",
            badgeLabel = "Chord Type",
            tool = Tool.CHORD_BOOK,
            resultType = SearchResultType.CHORD,
            score = score,
            detailRows = listOf(
              SearchResultDetailRow("Chord Quality", quality.fullName),
              SearchResultDetailRow("Symbol", displaySymbol),
              SearchResultDetailRow("Interval Formula", intervalLabels.joinToString(" – ")),
              SearchResultDetailRow("Semitone Offsets", quality.intervals.joinToString(", ")),
              SearchResultDetailRow("In Key of C", cNotes.joinToString(" – ")),
            ),
          )
        )
      }
    }

    // 2. Musical Scales & Modes
    for (scale in ScaleType.entries) {
      val cScaleNotes = scale.intervals.map { semitones ->
        ChordBookEngine.formatPitchClass(semitones.mod(12), EnharmonicPreference.SHARPS)
      }
      val formulaStr = scale.formulaLabels.joinToString(" – ")
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = "${scale.displayName} Scale",
        secondaryText = "Formula: $formulaStr • Notes in C: ${cScaleNotes.joinToString(" ")}",
        keywordsText = "scale mode fretboard ${scale.category.title} ${scale.name.replace('_', ' ')}",
        baseWeight = 130,
      )
      if (score > 0) {
        results.add(
          SearchResultItem(
            id = "scale_${scale.name}",
            title = "${scale.displayName} Scale",
            subtitle = "Degrees: $formulaStr (${scale.intervals.size} notes)",
            snippet = "In C: ${cScaleNotes.joinToString(" – ")} • Full neck pattern in ${Tool.CHORD_BOOK.title}.",
            badgeLabel = "Musical Scale",
            tool = Tool.CHORD_BOOK,
            resultType = SearchResultType.SCALE,
            score = score,
            copyableValue = cScaleNotes.joinToString(" "),
            detailRows = listOf(
              SearchResultDetailRow("Scale / Mode", scale.displayName),
              SearchResultDetailRow("Category", scale.category.title),
              SearchResultDetailRow("Degree Formula", formulaStr),
              SearchResultDetailRow("Semitone Steps", scale.intervals.joinToString(", ")),
              SearchResultDetailRow("Notes in C", cScaleNotes.joinToString(" – ")),
            ),
          )
        )
      }
    }

    // 3. Chord Progressions
    for (prog in ChordBookEngine.defaultProgressions()) {
      val chordsSummary = prog.chords.joinToString(" → ") { it.formatName() }
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = prog.name,
        secondaryText = chordsSummary,
        keywordsText = "chord progression cadence harmonic songbook",
        baseWeight = 125,
      )
      if (score > 0) {
        results.add(
          SearchResultItem(
            id = "progression_${prog.id}",
            title = prog.name,
            subtitle = "Chords: $chordsSummary",
            snippet = "Playable chord progression sequence in ${Tool.CHORD_BOOK.title}.",
            badgeLabel = "Progression",
            tool = Tool.CHORD_BOOK,
            resultType = SearchResultType.PROGRESSION,
            score = score,
            copyableValue = chordsSummary,
            detailRows = listOf(
              SearchResultDetailRow("Progression Name", prog.name),
              SearchResultDetailRow("Chords", chordsSummary),
              SearchResultDetailRow("Chord Count", "${prog.chords.size}"),
            ),
          )
        )
      }
    }

    // 4. Instrument Tunings in Chord Book
    for (tuning in ChordBookEngine.BUILT_IN_TUNINGS) {
      val notesLabel = tuning.formattedNotes()
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = "${tuning.name} (${tuning.instrument.displayName})",
        secondaryText = "$notesLabel • ${tuning.stringCount} strings",
        keywordsText = "instrument tuning fretboard ${tuning.instrument.shortName}",
        baseWeight = 120,
      )
      if (score > 0) {
        results.add(
          SearchResultItem(
            id = "chordbook_tuning_${tuning.id}",
            title = "${tuning.instrument.displayName} — ${tuning.name}",
            subtitle = "Strings (${tuning.stringCount}): $notesLabel",
            snippet = "Fretboard voicing layout for ${tuning.instrument.displayName} in ${Tool.CHORD_BOOK.title}.",
            badgeLabel = tuning.instrument.shortName,
            tool = Tool.CHORD_BOOK,
            resultType = SearchResultType.TUNING,
            score = score,
            detailRows = listOf(
              SearchResultDetailRow("Instrument", tuning.instrument.displayName),
              SearchResultDetailRow("Tuning Name", tuning.name),
              SearchResultDetailRow("String Count", "${tuning.stringCount}"),
              SearchResultDetailRow("Open String Pitches", notesLabel),
            ),
          )
        )
      }
    }

    return results
  }

  private data class ClassicalTempoInfo(
    val name: String,
    val bpmRange: String,
    val defaultBpm: Int,
    val description: String,
  )

  private val CLASSICAL_TEMPOS = listOf(
    ClassicalTempoInfo("Grave", "20–40 BPM", 32, "Very slow, solemn, and weighty tempo"),
    ClassicalTempoInfo("Largo", "40–60 BPM", 50, "Broad, dignified, and stately tempo"),
    ClassicalTempoInfo("Adagio", "66–76 BPM", 72, "Slow, lyrical, and expressive tempo"),
    ClassicalTempoInfo("Andante", "76–108 BPM", 92, "Walking pace, flowing and moderate"),
    ClassicalTempoInfo("Moderato", "108–120 BPM", 112, "Moderate, balanced everyday tempo"),
    ClassicalTempoInfo("Allegro", "120–156 BPM", 136, "Fast, bright, and lively tempo"),
    ClassicalTempoInfo("Vivace", "156–176 BPM", 164, "Lively, brisk, and vibrant tempo"),
    ClassicalTempoInfo("Presto", "176–200 BPM", 184, "Very fast and energetic tempo"),
    ClassicalTempoInfo("Prestissimo", "200–240 BPM", 216, "Extremely fast, maximum metronome tempo"),
  )

  private fun searchMetronomeCatalog(
    normalizedQuery: String,
    tokens: List<String>,
  ): List<SearchResultItem> {
    val results = mutableListOf<SearchResultItem>()

    for (tempo in CLASSICAL_TEMPOS) {
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = "${tempo.name} (${tempo.bpmRange})",
        secondaryText = tempo.description,
        keywordsText = "metronome tempo bpm classical ${tempo.defaultBpm}",
        baseWeight = 120,
      )
      if (score > 0) {
        results.add(
          SearchResultItem(
            id = "metronome_tempo_${tempo.name.lowercase(Locale.ROOT)}",
            title = "${tempo.name} — ${tempo.bpmRange}",
            subtitle = "${tempo.description} (Default: ${tempo.defaultBpm} BPM)",
            snippet = "Classical tempo marking in ${Tool.METRONOME.title}.",
            badgeLabel = "Tempo Marking",
            tool = Tool.METRONOME,
            resultType = SearchResultType.TEMPO,
            score = score,
            detailRows = listOf(
              SearchResultDetailRow("Tempo Marking", tempo.name),
              SearchResultDetailRow("BPM Range", tempo.bpmRange),
              SearchResultDetailRow("Typical BPM", "${tempo.defaultBpm} BPM"),
              SearchResultDetailRow("Character", tempo.description),
            ),
          )
        )
      }
    }

    for (sig in TimeSignature.defaultSignatures) {
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = "${sig.label} Time Signature",
        secondaryText = "${sig.beatsPerMeasure} beats per measure, 1/${sig.noteValue} note gets the beat",
        keywordsText = "metronome meter time signature rhythm",
        baseWeight = 115,
      )
      if (score > 0) {
        results.add(
          SearchResultItem(
            id = "metronome_sig_${sig.label}",
            title = "${sig.label} Time Signature",
            subtitle = "${sig.beatsPerMeasure} beats per measure • Note value: 1/${sig.noteValue}",
            snippet = "Configurable beat accents and subdivisions in ${Tool.METRONOME.title}.",
            badgeLabel = "Time Signature",
            tool = Tool.METRONOME,
            resultType = SearchResultType.TEMPO,
            score = score,
          )
        )
      }
    }

    return results
  }

  private fun searchUnitCatalog(
    normalizedQuery: String,
    tokens: List<String>,
  ): List<SearchResultItem> {
    val results = mutableListOf<SearchResultItem>()
    for (category in UnitCategory.entries) {
      val units = UnitCatalog.unitsFor(category)
      for (unit in units) {
        val score = scoreTextMatch(
          normalizedQuery = normalizedQuery,
          tokens = tokens,
          primaryText = "${unit.name} (${unit.symbol})",
          secondaryText = "${category.label} unit converter",
          keywordsText = "unit convert measurement ${category.name}",
          baseWeight = 118,
        )
        if (score > 0) {
          val sampleConversions = units
            .filter { it != unit }
            .take(4)
            .joinToString(" • ") { peer ->
              "1 ${unit.symbol} = ${UnitCatalog.formatNumber(UnitCatalog.convert(1.0, unit, peer))} ${peer.symbol}"
            }
          results.add(
            SearchResultItem(
              id = "unit_catalog_${category.name}_${unit.symbol}",
              title = "${unit.name} (${unit.symbol})",
              subtitle = "${category.label} Unit • $sampleConversions",
              snippet = "Supported in ${Tool.UNIT_CONVERTER.title} (${category.label}).",
              badgeLabel = "${category.label} Unit",
              tool = Tool.UNIT_CONVERTER,
              resultType = SearchResultType.UNIT,
              categoryFilter = SearchCategoryFilter.MATH_AND_MEASUREMENT,
              score = score,
              detailRows = listOf(
                SearchResultDetailRow("Unit Name", unit.name),
                SearchResultDetailRow("Symbol", unit.symbol),
                SearchResultDetailRow("Category", category.label),
              ) + units.filter { it != unit }.map { peer ->
                SearchResultDetailRow(
                  label = "1 ${unit.symbol} in ${peer.name}",
                  value = "${UnitCatalog.formatNumber(UnitCatalog.convert(1.0, unit, peer))} ${peer.symbol}",
                )
              },
            )
          )
        }
      }
    }
    return results
  }

  private fun searchRulerCatalog(
    normalizedQuery: String,
    tokens: List<String>,
  ): List<SearchResultItem> {
    val results = mutableListOf<SearchResultItem>()
    for (ref in RulerMath.referenceObjects) {
      val inches = RulerMath.mmToInches(ref.lengthMm.toDouble())
      val mmText = String.format(Locale.US, "%.2f mm (%.2f cm)", ref.lengthMm, ref.lengthMm / 10f)
      val inText = String.format(Locale.US, "%.3f in", inches)
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = ref.name,
        secondaryText = "$mmText • $inText",
        keywordsText = "ruler caliper reference object size dimension measure",
        baseWeight = 125,
      )
      if (score > 0) {
        results.add(
          SearchResultItem(
            id = "ruler_ref_${ref.name.lowercase(Locale.ROOT).replace(' ', '_')}",
            title = "${ref.name} — $mmText",
            subtitle = "Imperial: $inText • Ruler Caliper Reference Preset",
            snippet = "Snap caliper markers A↔B to ${ref.name} in ${Tool.RULER.title}.",
            badgeLabel = "Ruler Reference",
            tool = Tool.RULER,
            resultType = SearchResultType.RULER_REFERENCE,
            categoryFilter = SearchCategoryFilter.MATH_AND_MEASUREMENT,
            score = score,
            copyableValue = mmText,
            detailRows = listOf(
              SearchResultDetailRow("Reference Object", ref.name),
              SearchResultDetailRow("Millimeters", String.format(Locale.US, "%.2f mm", ref.lengthMm)),
              SearchResultDetailRow("Centimeters", String.format(Locale.US, "%.2f cm", ref.lengthMm / 10f)),
              SearchResultDetailRow("Inches", inText),
            ),
          )
        )
      }
    }

    for (mode in RulerMode.entries) {
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = "Ruler ${mode.label} Mode",
        secondaryText = "Measurement instrument in Ruler tool",
        keywordsText = "ruler caliper pitch protractor level angle bubble",
        baseWeight = 115,
      )
      if (score > 0) {
        results.add(
          SearchResultItem(
            id = "ruler_mode_${mode.name}",
            title = "Ruler — ${mode.label}",
            subtitle = "Interactive measurement mode in ${Tool.RULER.title}",
            snippet = "Switch to ${mode.label} inside the ${Tool.RULER.title} tool.",
            badgeLabel = "Ruler Mode",
            tool = Tool.RULER,
            resultType = SearchResultType.RULER_REFERENCE,
            categoryFilter = SearchCategoryFilter.MATH_AND_MEASUREMENT,
            score = score,
          )
        )
      }
    }
    return results
  }

  private fun searchDecibelCatalog(
    normalizedQuery: String,
    tokens: List<String>,
  ): List<SearchResultItem> {
    return NoiseLevelCategory.entries.mapNotNull { category ->
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = "${category.label} Noise Level (${category.rangeText})",
        secondaryText = category.description,
        keywordsText = "decibels db spl noise sound level meter",
        baseWeight = 120,
      )
      if (score <= 0) return@mapNotNull null

      SearchResultItem(
        id = "decibel_cat_${category.name}",
        title = "${category.label} (${category.rangeText})",
        subtitle = category.description,
        snippet = "Acoustic classification in ${Tool.DECIBEL_METER.title}.",
        badgeLabel = "dB Classification",
        tool = Tool.DECIBEL_METER,
        resultType = SearchResultType.DECIBEL_LEVEL,
        categoryFilter = SearchCategoryFilter.MATH_AND_MEASUREMENT,
        score = score,
        detailRows = listOf(
          SearchResultDetailRow("Noise Category", category.label),
          SearchResultDetailRow("dB SPL Range", category.rangeText),
          SearchResultDetailRow("Typical Sources", category.description),
        ),
      )
    }
  }

  private fun searchColorSwatchesAndHarmonies(
    normalizedQuery: String,
    tokens: List<String>,
  ): List<SearchResultItem> {
    val results = mutableListOf<SearchResultItem>()
    for (swatch in ColorMath.namedReferenceCatalog) {
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = "${swatch.name} (${swatch.hex})",
        secondaryText = "RGB(${swatch.r}, ${swatch.g}, ${swatch.b})",
        keywordsText = "color swatch palette hex rgb",
        baseWeight = 125,
      )
      if (score > 0) {
        val hsl = ColorMath.rgbToHsl(swatch.r, swatch.g, swatch.b)
        val cmyk = ColorMath.rgbToCmyk(swatch.r, swatch.g, swatch.b)
        results.add(
          SearchResultItem(
            id = "color_swatch_${swatch.hex}",
            title = "${swatch.name} (${swatch.hex})",
            subtitle = "RGB(${swatch.r}, ${swatch.g}, ${swatch.b}) • HSL(${hsl.hue}°, ${hsl.saturationPercent}%, ${hsl.lightnessPercent}%)",
            snippet = "CMYK(${cmyk.c}%, ${cmyk.m}%, ${cmyk.y}%, ${cmyk.k}%) • Tap to open in ${Tool.COLOR_PICKER.title}.",
            badgeLabel = "Color Swatch",
            tool = Tool.COLOR_PICKER,
            resultType = SearchResultType.COLOR_SWATCH,
            score = score,
            copyableValue = swatch.hex,
            colorHex = swatch.hex,
            detailRows = listOf(
              SearchResultDetailRow("Swatch Name", swatch.name),
              SearchResultDetailRow("HEX", swatch.hex),
              SearchResultDetailRow("RGB", "${swatch.r}, ${swatch.g}, ${swatch.b}"),
              SearchResultDetailRow("HSL", "${hsl.hue}°, ${hsl.saturationPercent}%, ${hsl.lightnessPercent}%"),
              SearchResultDetailRow("CMYK", "${cmyk.c}%, ${cmyk.m}%, ${cmyk.y}%, ${cmyk.k}%"),
            ),
          )
        )
      }
    }

    for (harmony in HarmonyMode.entries) {
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = "${harmony.label} Color Harmony",
        secondaryText = "Color wheel harmony generator in Colors & Settings",
        keywordsText = "color harmony palette wheel complementary analogous triadic split shades",
        baseWeight = 115,
      )
      if (score > 0) {
        results.add(
          SearchResultItem(
            id = "color_harmony_${harmony.name}",
            title = "${harmony.label} Harmony",
            subtitle = "Color palette generator in ${Tool.COLOR_PICKER.title}",
            snippet = "Generate ${harmony.label} color palettes and WCAG contrast ratings.",
            badgeLabel = "Color Harmony",
            tool = Tool.COLOR_PICKER,
            resultType = SearchResultType.COLOR_SWATCH,
            score = score,
          )
        )
      }
    }
    return results
  }

  private val DTC_CODE_REGEX = Regex("^\\s*([PCBU][0-3][0-9A-F]{3})\\s*$", RegexOption.IGNORE_CASE)

  private fun searchObd2Catalog(
    rawQuery: String,
    normalizedQuery: String,
    tokens: List<String>,
  ): List<SearchResultItem> {
    val results = mutableListOf<SearchResultItem>()
    val matchedCodes = mutableSetOf<String>()

    for (entry in Obd2DtcCatalog.ALL_ENTRIES) {
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = "${entry.code} — ${entry.description}",
        secondaryText = "${entry.system} • ${entry.commonCauses}",
        keywordsText = "obd2 dtc trouble code check engine ${entry.severity.label}",
        baseWeight = 135,
      )
      if (score > 0) {
        matchedCodes.add(entry.code.uppercase(Locale.ROOT))
        results.add(
          SearchResultItem(
            id = "obd2_dtc_${entry.code}",
            title = "${entry.code} — ${entry.description}",
            subtitle = "${entry.system} • Severity: ${entry.severity.label}",
            snippet = "Common causes: ${entry.commonCauses}",
            badgeLabel = "OBD2 DTC",
            tool = Tool.OBD2_SCANNER,
            resultType = SearchResultType.OBD2_DTC,
            score = score,
            copyableValue = "${entry.code}: ${entry.description}",
            detailRows = listOf(
              SearchResultDetailRow("Trouble Code", entry.code),
              SearchResultDetailRow("Description", entry.description),
              SearchResultDetailRow("System", entry.system),
              SearchResultDetailRow("Severity", entry.severity.label),
              SearchResultDetailRow("Common Causes", entry.commonCauses),
            ),
          )
        )
      }
    }

    // Dynamic SAE J2012 decoder for any 5-character DTC code not already in ALL_ENTRIES
    DTC_CODE_REGEX.matchEntire(rawQuery)?.let { match ->
      val code = match.groupValues[1].uppercase(Locale.ROOT)
      if (code !in matchedCodes) {
        val dynamicEntry = Obd2DtcCatalog.lookup(code)
        results.add(
          SearchResultItem(
            id = "obd2_dtc_dynamic_${dynamicEntry.code}",
            title = "${dynamicEntry.code} — ${dynamicEntry.description}",
            subtitle = "${dynamicEntry.system} • Severity: ${dynamicEntry.severity.label}",
            snippet = "Common causes: ${dynamicEntry.commonCauses}",
            badgeLabel = "SAE J2012 DTC",
            tool = Tool.OBD2_SCANNER,
            resultType = SearchResultType.OBD2_DTC,
            score = 480,
            copyableValue = "${dynamicEntry.code}: ${dynamicEntry.description}",
            detailRows = listOf(
              SearchResultDetailRow("Trouble Code", dynamicEntry.code),
              SearchResultDetailRow("Description", dynamicEntry.description),
              SearchResultDetailRow("System", dynamicEntry.system),
              SearchResultDetailRow("Severity", dynamicEntry.severity.label),
              SearchResultDetailRow("Common Causes", dynamicEntry.commonCauses),
            ),
          )
        )
      }
    }

    return results
  }

  private fun searchFileConverterFormats(
    normalizedQuery: String,
    tokens: List<String>,
  ): List<SearchResultItem> {
    return ConvertibleFormat.entries.mapNotNull { format ->
      val aliasesText = format.aliases.joinToString(" ")
      val targets = FileConverterEngine.CONVERSION_CAPABILITY_MAP[format]?.keys.orEmpty()
      val targetsText = targets.joinToString(", ") { it.displayName }

      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = "${format.displayName} (.${format.extension})",
        secondaryText = "${format.category.displayName} • ${format.description} • ${format.mimeType}",
        keywordsText = "$aliasesText convert file format transcode",
        baseWeight = 125,
      )
      if (score <= 0) return@mapNotNull null

      SearchResultItem(
        id = "format_${format.name}",
        title = "${format.displayName} (.${format.extension}) — ${format.category.displayName}",
        subtitle = "${format.description} • MIME: ${format.mimeType}",
        snippet = if (targetsText.isNotEmpty()) "Converts to: $targetsText" else format.description,
        badgeLabel = "${format.category.displayName} Format",
        tool = Tool.FILE_CONVERTER,
        resultType = SearchResultType.FILE_FORMAT,
        score = score,
        detailRows = listOf(
          SearchResultDetailRow("Format", format.displayName),
          SearchResultDetailRow("Extension", ".${format.extension}"),
          SearchResultDetailRow("Media Category", format.category.displayName),
          SearchResultDetailRow("MIME Type", format.mimeType),
          SearchResultDetailRow("Description", format.description),
          SearchResultDetailRow("Supported Output Targets (${targets.size})", targetsText.ifEmpty { "None" }),
        ),
      )
    }
  }

  private fun searchMusicTracks(
    normalizedQuery: String,
    tokens: List<String>,
  ): List<SearchResultItem> {
    return MusicPlayerEngine.DEFAULT_SAMPLE_TRACKS.mapNotNull { track ->
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = track.title,
        secondaryText = "${track.artist} • ${track.album}",
        keywordsText = "music song track audio player synth",
        baseWeight = 125,
      )
      if (score <= 0) return@mapNotNull null

      val durationSec = (track.durationMs / 1000L).toInt()
      val durationText = String.format(Locale.US, "%d:%02d", durationSec / 60, durationSec % 60)

      SearchResultItem(
        id = "music_track_${track.id}",
        title = track.title,
        subtitle = "${track.artist} • ${track.album} ($durationText)",
        snippet = "Built-in synthesizer track in ${Tool.MUSIC.title}.",
        badgeLabel = "Music Track",
        tool = Tool.MUSIC,
        resultType = SearchResultType.MUSIC_TRACK,
        score = score,
        detailRows = listOf(
          SearchResultDetailRow("Track Title", track.title),
          SearchResultDetailRow("Artist", track.artist),
          SearchResultDetailRow("Album", track.album),
          SearchResultDetailRow("Duration", durationText),
        ),
      )
    }
  }

  private fun searchAudioRecorderCatalog(
    normalizedQuery: String,
    tokens: List<String>,
  ): List<SearchResultItem> {
    val results = mutableListOf<SearchResultItem>()
    for (format in AudioRecordingFormat.entries) {
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = "${format.displayName} Audio Recording (.${format.extension})",
        secondaryText = "${format.description} • ${format.mimeType}",
        keywordsText = "audio recorder voice memo format microphone",
        baseWeight = 115,
      )
      if (score > 0) {
        results.add(
          SearchResultItem(
            id = "recorder_format_${format.name}",
            title = "${format.displayName} Recording Format",
            subtitle = "Extension: .${format.extension} • MIME: ${format.mimeType}",
            snippet = "${format.description} in ${Tool.AUDIO_RECORDER.title}.",
            badgeLabel = "Recorder Format",
            tool = Tool.AUDIO_RECORDER,
            resultType = SearchResultType.AUDIO_RECORDING,
            score = score,
          )
        )
      }
    }

    for (source in AudioCaptureSource.entries) {
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = "${source.displayName} Audio Input",
        secondaryText = source.description,
        keywordsText = "audio recorder microphone input source device",
        baseWeight = 115,
      )
      if (score > 0) {
        results.add(
          SearchResultItem(
            id = "recorder_source_${source.name}",
            title = "${source.displayName} Audio Input",
            subtitle = source.description,
            snippet = "Select ${source.displayName} routing in ${Tool.AUDIO_RECORDER.title}.",
            badgeLabel = "Mic Source",
            tool = Tool.AUDIO_RECORDER,
            resultType = SearchResultType.AUDIO_RECORDING,
            score = score,
          )
        )
      }
    }
    return results
  }

  private fun searchCameraCatalog(
    normalizedQuery: String,
    tokens: List<String>,
  ): List<SearchResultItem> {
    val results = mutableListOf<SearchResultItem>()
    for (mode in CameraCaptureMode.entries) {
      val description = when (mode) {
        CameraCaptureMode.PHOTO -> "High-resolution still photography with grid overlays, flash, and aspect ratios"
        CameraCaptureMode.VIDEO -> "Video recording with live timer, torch control, and gallery integration"
        CameraCaptureMode.SCANNER -> "Real-time QR code, Aztec, Data Matrix, EAN-13, UPC-A, and Code 128 barcode scanner"
        CameraCaptureMode.NOTE -> "Optical Character Recognition (OCR) document scanner with contrast enhancement and direct save to Notes"
        CameraCaptureMode.VISION -> "Live on-device ML Kit image labeling and visual scene tagging accuracy inspector"
      }
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = "Camera ${mode.displayName} Mode",
        secondaryText = description,
        keywordsText = "camera photo video qr barcode scanner ocr note document vision ml kit tagging labels eye",
        baseWeight = 120,
      )
      if (score > 0) {
        results.add(
          SearchResultItem(
            id = "camera_mode_${mode.name}",
            title = "Camera — ${mode.displayName} Mode",
            subtitle = description,
            snippet = "Available in ${Tool.CAMERA.title}.",
            badgeLabel = "Camera Mode",
            tool = Tool.CAMERA,
            resultType = SearchResultType.CAMERA_MODE,
            score = score,
          )
        )
      }
    }

    for (preset in CameraNoteSamplePreset.entries) {
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = "OCR Preset: ${preset.label}",
        secondaryText = preset.defaultTitle,
        keywordsText = "camera ocr document scan note receipt checklist",
        baseWeight = 110,
      )
      if (score > 0) {
        results.add(
          SearchResultItem(
            id = "camera_ocr_${preset.name}",
            title = "OCR Document Scanner — ${preset.label}",
            subtitle = preset.defaultTitle,
            snippet = preset.sampleText.lines().firstOrNull().orEmpty(),
            badgeLabel = "OCR Scanner",
            tool = Tool.CAMERA,
            resultType = SearchResultType.CAMERA_MODE,
            score = score,
          )
        )
      }
    }
    return results
  }

  private fun searchDiceCatalog(
    normalizedQuery: String,
    tokens: List<String>,
  ): List<SearchResultItem> {
    return DieType.entries.mapNotNull { die ->
      val avg = (die.sides + 1) / 2.0
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = "${die.label.uppercase(Locale.ROOT)} (${die.sides}-Sided Die)",
        secondaryText = "Polyhedral RPG die • Range 1–${die.sides} • Average $avg",
        keywordsText = "dice roller polyhedral rpg dnd tabletop ${die.label}",
        baseWeight = 120,
      )
      if (score <= 0) return@mapNotNull null

      SearchResultItem(
        id = "die_type_${die.name}",
        title = "${die.label.uppercase(Locale.ROOT)} — ${die.sides}-Sided Polyhedral Die",
        subtitle = "Range: 1–${die.sides} • Expected Average: $avg",
        snippet = "Roll single or multi-die ${die.label} pools with modifiers and Advantage in ${Tool.DICE_ROLLER.title}.",
        badgeLabel = "Polyhedral Die",
        tool = Tool.DICE_ROLLER,
        resultType = SearchResultType.LIVE_DICE_ROLL,
        score = score,
      )
    }
  }

  private data class FeatureCatalogItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val snippet: String,
    val badgeLabel: String,
    val tool: Tool,
    val resultType: SearchResultType,
    val keywords: String,
  )

  private val FEATURE_CATALOG_ITEMS = listOf(
    FeatureCatalogItem(
      id = "feature_bt_remote_air_mouse",
      title = "Air Mouse & Touchpad Trackpad",
      subtitle = "Bluetooth HID Pointer & Scroll Control",
      snippet = "Control cursor movement, left/right clicks, and scrolling on TVs or PCs via Bluetooth HID.",
      badgeLabel = "Remote Feature",
      tool = Tool.BLUETOOTH_REMOTE,
      resultType = SearchResultType.DEVICE_CONTROL,
      keywords = "air mouse trackpad touchpad cursor pointer bluetooth remote tv",
    ),
    FeatureCatalogItem(
      id = "feature_bt_gamepad_layout",
      title = "Dual Thumbstick & ABXY Controller Layout",
      subtitle = "Landscape Bluetooth HID Gamepad with L1/R1/L2/R2 Triggers",
      snippet = "Full wireless controller layout for Android, PC, and console Bluetooth HID gaming.",
      badgeLabel = "Gamepad Feature",
      tool = Tool.BLUETOOTH_GAMEPAD,
      resultType = SearchResultType.DEVICE_CONTROL,
      keywords = "gamepad controller joystick thumbstick abxy triggers l1 r1 l2 r2 dpad",
    ),
    FeatureCatalogItem(
      id = "feature_archive_formats",
      title = "ZIP, TAR, TAR.GZ & GZIP Extraction & Compression",
      subtitle = "Browse archive trees, extract files, and create compressed bundles",
      snippet = "Supports .zip, .tar, .tar.gz, .tgz, and .gz archives with configurable compression levels.",
      badgeLabel = "Archive Feature",
      tool = Tool.ARCHIVE,
      resultType = SearchResultType.MEDIA_CAPABILITY,
      keywords = "zip unzip tar targz tgz gzip compress extract archive bundle",
    ),
    FeatureCatalogItem(
      id = "feature_pdf_annotations",
      title = "PDF Freehand Ink, Highlighter & Text Annotations",
      subtitle = "Draw on PDF pages and export flattened annotated documents",
      snippet = "Annotate multi-page PDFs with customizable pen colors, stroke widths, highlighters, and page notes.",
      badgeLabel = "PDF Feature",
      tool = Tool.PDF_VIEWER,
      resultType = SearchResultType.MEDIA_CAPABILITY,
      keywords = "pdf viewer annotate pen highlighter draw signature markup export",
    ),
    FeatureCatalogItem(
      id = "feature_image_editor_adjustments",
      title = "Photo Crop, 90° Rotate, Flip & Color Filters",
      subtitle = "Brightness, Contrast, Saturation, Warmth & Vignette Sliders",
      snippet = "Edit images non-destructively and export high-resolution PNG or JPEG files.",
      badgeLabel = "Image Editing",
      tool = Tool.IMAGE_EDITOR,
      resultType = SearchResultType.MEDIA_CAPABILITY,
      keywords = "image editor crop rotate flip brightness contrast saturation warmth vignette filter",
    ),
    FeatureCatalogItem(
      id = "feature_video_editor_timeline",
      title = "Video Timeline Trim, Playback Speed (0.25x–2x) & Mute",
      subtitle = "Cut video clips, adjust speed, rotate orientation & capture frames",
      snippet = "Trim video start/end points, apply color presets, scale resolution, and export MP4.",
      badgeLabel = "Video Editing",
      tool = Tool.VIDEO_EDITOR,
      resultType = SearchResultType.MEDIA_CAPABILITY,
      keywords = "video editor trim cut speed slow motion fast forward mute rotate frame capture",
    ),
    FeatureCatalogItem(
      id = "feature_audio_editor_waveform",
      title = "Audio Waveform Trim, Gain, Fade In/Out & Peak Normalize",
      subtitle = "Precision envelope shaping and lossless WAV export",
      snippet = "Trim audio recordings, boost or attenuate dB gain, apply smooth fades, and normalize peak levels.",
      badgeLabel = "Audio Editing",
      tool = Tool.AUDIO_EDITOR,
      resultType = SearchResultType.MEDIA_CAPABILITY,
      keywords = "audio editor waveform trim fade in fade out normalize gain db wav",
    ),
  )

  private fun searchRemotesAndMediaCapabilities(
    normalizedQuery: String,
    tokens: List<String>,
  ): List<SearchResultItem> {
    return FEATURE_CATALOG_ITEMS.mapNotNull { item ->
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = item.title,
        secondaryText = item.subtitle,
        keywordsText = "${item.keywords} ${item.snippet}",
        baseWeight = 115,
      )
      if (score <= 0) return@mapNotNull null

      SearchResultItem(
        id = item.id,
        title = item.title,
        subtitle = "${item.tool.title} • ${item.subtitle}",
        snippet = item.snippet,
        badgeLabel = item.badgeLabel,
        tool = item.tool,
        resultType = item.resultType,
        categoryFilter = categoryFilterForTool(item.tool),
        score = score,
      )
    }
  }

  private data class SettingCatalogEntry(
    val id: String,
    val title: String,
    val subtitle: String,
    val snippet: String,
    val keywords: String,
  )

  private val SETTINGS_CATALOG = listOf(
    SettingCatalogEntry(
      id = "setting_theme_mode",
      title = "Theme Mode (System, Light, Dark)",
      subtitle = "Appearance • Switch between System Default, Light Mode, and Dark Mode",
      snippet = "Configure application-wide Material 3 light or dark appearance in Settings.",
      keywords = "settings theme mode dark mode light mode system appearance night",
    ),
    SettingCatalogEntry(
      id = "setting_theme_color",
      title = "Main Theme Color & Wallpaper Dynamic Color",
      subtitle = "Appearance • System Material You, Music Player Album Palette, or Custom Hex",
      snippet = "Choose your seed color or sync the app theme dynamically with the active Music Player track.",
      keywords = "settings theme color wallpaper material you dynamic color music player palette seed",
    ),
    SettingCatalogEntry(
      id = "setting_accent_harmony",
      title = "Automatic Accent Color Harmonies",
      subtitle = "Complementary, Analogous, Triadic, Split-Complementary & Tints/Shades",
      snippet = "Automatically derive secondary and accent UI tokens from the main theme color.",
      keywords = "settings accent color harmony complementary analogous triadic split monochromatic",
    ),
    SettingCatalogEntry(
      id = "setting_tool_sorting",
      title = "Navigation Panel Tool Sorting & Tools Per Row",
      subtitle = "Organize tools by Manual Order, Tool Categories, or Usage Frequency (3–6 per row)",
      snippet = "Customize how tools are grouped, ordered, and paginated in the pull-up navigation bar.",
      keywords = "settings tool sorting order manual categories usage frequency tools per row grid",
    ),
    SettingCatalogEntry(
      id = "setting_tool_visibility",
      title = "Tool Visibility & Hidden Tools Manager",
      subtitle = "Show or hide individual tools in the bottom navigation panel",
      snippet = "Toggle visibility for any tool or tap any tool icon in Settings to open it directly.",
      keywords = "settings tool visibility hidden tools show hide navigation bar",
    ),
    SettingCatalogEntry(
      id = "setting_backup_restore",
      title = "Full App Backup, Restore & Reset Preferences",
      subtitle = "Export or import all tool data & settings as a portable JSON bundle",
      snippet = "Back up notes, tasks, recipes, plant schedules, custom tunings, and preferences.",
      keywords = "settings backup restore export import json reset preferences data",
    ),
  )

  private fun searchSettingsCatalog(
    normalizedQuery: String,
    tokens: List<String>,
  ): List<SearchResultItem> {
    return SETTINGS_CATALOG.mapNotNull { entry ->
      val score = scoreTextMatch(
        normalizedQuery = normalizedQuery,
        tokens = tokens,
        primaryText = entry.title,
        secondaryText = entry.subtitle,
        keywordsText = "${entry.keywords} ${entry.snippet}",
        baseWeight = 120,
      )
      if (score <= 0) return@mapNotNull null

      SearchResultItem(
        id = entry.id,
        title = entry.title,
        subtitle = entry.subtitle,
        snippet = entry.snippet,
        badgeLabel = "App Setting",
        tool = Tool.SETTINGS,
        resultType = SearchResultType.SETTING,
        score = score,
      )
    }
  }
}
