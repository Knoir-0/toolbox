# Toolbox 🧰

A modern, offline-first multi-tool utility application for Android built entirely with **Jetpack Compose**, **Material 3**, and **Kotlin**.

Toolbox consolidates essential daily tools—from musical utilities to measurement, productivity, and device control aids—into a single, high-performance application with an intuitive, customizable navigation drawer.

---

## 🛠️ Included Tools

The application includes 36 specialized tools grouped into 11 intuitive functional categories:

| Tool | Category | Description | Key Capabilities |
| :--- | :--- | :--- | :--- |
| **🔢 Calculate** | Math | High-precision scientific, standard & graphing calculator | Live result evaluation (with playful easter egg subtexts for famous mathematical, physical, and pop-culture constants like `42`, `π`, `τ`, `e`, `φ`, `√2`, `c`, `g`, `-273.15`, `88`, `1.21`, `1337`, `80085`, `5318008`, `0.7734`, `404`, `69`, `1729`, `137`, and `Nₐ`), smart auto-matching parentheses `( )`, trigonometry (DEG/RAD), powers, roots, logarithms, percentages, factorials, calculation history tape, memory registers (MC/MR/M+/M-), clipboard integration, and an interactive 2D **Graphing** mode supporting up to 6 simultaneous `f(x)` curves, preset function library, pan & pinch-to-zoom viewport with auto-fit Y, discontinuity/asymptote splitting, interactive curve tracing with `SlimDotSlider` scrubber, tangent slope (`dy/dx`) line overlay, automatic root/extremum/intersection detection, table of values, and definite integral (`∫ f(x) dx`) area shading. |
| **🗒️ Notes** | Productivity | Simplified offline Google Keep-inspired note and checklist board backed by `.text` files in `/Notes`, with a configurable Home Screen AppWidget | Scoped storage access to `/Notes` in root storage saving notes in the Markdown-based `.text` format (`# Title`, Markdown body text, `- item` unchecked / `+ item` checked lists with tab `\t` indentation for nested checklist items, and a trailing `#Label Name 1, #Label Name 2, ...` CSV line for labels with the internal `#` hidden in the UI), 2–3 column staggered card grid & list view toggle, adaptive light/dark card color palettes, pinned & unpinned sections, seamless plain-text and interactive checklist conversion, drag-and-drop note and checklist item reordering with automatic edge scrolling, custom labels filtering, search by keyword & color, multi-select bulk actions, archive, trash with restore, cross-tool `.text` file export/import, undo/redo history, and a resizable **Notes** Home Screen AppWidget (configurable on placement to show **All Notes**, **Pinned Notes**, or a **Specific Label**, with note cards styled in each note's selected color, a top-right view mode selector cycling between List view and a 2-column Grid view, scrollable notes list, and floating Plus & Checklist FABs that open **Notes** to create a new note or checklist). |
| **📝 To Do** | Productivity | Streamlined offline task manager backed by a single `to-do.text` file in `/ToDo` with tab-indented nested subtask lists, scheduling, recurring tasks, alarm reminders, and Home Screen AppWidget | Scoped storage access to `/ToDo` in root storage saving the entire To Do list (active tasks, completed tasks, and multi-level hierarchical subtasks) as a single `/ToDo/to-do.text` file in the custom `.text` format using tab (`\t`) nested list indentation (`-` for active/uncompleted items and `+` for completed items), ergonomic pill-shaped quick-add bar with instant keyboard entry and inline autocomplete suggestions from the Saved Tasks list while typing, hierarchical subtasks with expand/collapse support, drag-and-drop task and subtask reordering with automatic edge scrolling, due date & time scheduling (24-hour main horizon + scheduled view), Google Calendar-style recurring rules (minutes, hours, days, weekdays, weeks, months, years), exact alarm notifications with reboot persistence, reusable task templates, swipe-to-complete (right) and swipe-to-delete (left), full undo/redo action history, daily completion counter with automatic midnight reset, and a resizable **To Do List** Home Screen AppWidget (scrollable task list with check circles, top-right Undo & Redo buttons, and floating Plus & Schedule FABs that open **To Do** with "Add a task..." focused or the schedule view opened). |
| **📅 Calendar** | Productivity | Offline Google Calendar-inspired schedule, month, week & day planner with reminders, `.ics` export, and 2 Home Screen AppWidgets | 4 view modes (**Schedule** showing upcoming events without past events, **Month** 6×7 Sunday-aligned grid with miniature event chips and selected-day agenda list, **Week**, and **Day**), 24-hour vertical timeline with sticky day headers, all-day event strip, and live current-time indicator line & dot, **Today** date badge jump, 11 official Google Calendar event color swatches (**Tomato**, **Flamingo**, **Tangerine**, **Banana**, **Sage**, **Basil**, **Peacock**, **Blueberry**, **Lavender**, **Grape**, **Graphite**) with adaptive light/dark container & text tokens and hex pill link to **Colors**, 7 filterable calendar categories (**Personal**, **Work**, **Birthdays**, **Holidays**, **Health & Fitness**, **Travel**, and live projected **To Do Tasks**), built-in annual holidays & seasonal observances, recurring rules (**Daily**, **Weekly**, **Biweekly**, **Weekdays Mon–Fri**, **Monthly**, **Yearly**), exact alarm notification reminders with reboot persistence, RFC 5545 `.ics` iCalendar export/import, cross-tool handoff to **Notes** and **To Do**, and 2 **Calendar** Home Screen AppWidgets: **Calendar Schedule** (matching the Calendar tool's Schedule view with a scrollable list of upcoming days' weekday/date badges and color-coded event cards excluding past events, Today badge reset, star favorite toggling, and `+` one-tap event creation) and **Calendar Month** (displaying the 6×7 monthly grid with weekday headers, Today highlight, colored event dots, `<` / `>` month navigation, Today badge reset, and `+` one-tap event creation). |
| **📷 Camera** | Media | Versatile photo and video camera with QR/barcode scanner, OCR document note scanner, live ML Kit vision, and compact mode pill bar Home Screen AppWidget | **Photo** (Standard, Night Mode, Panorama in JPEG/PNG), **Video** (MP4), **Scan** (2D QR Code, Data Matrix, Aztec, PDF417 and 1D EAN-13/ISBN, EAN-8, UPC-A, UPC-E, Code 128, Code 39, Code 93, ITF, Codabar with structured Wi-Fi/URL/vCard/GS1 parsing, scan history, and QR/barcode generator), **Note** (aspect-aligned ML Kit OCR document text extraction with multi-frame motion-blur stabilization and direct save to Notes), and **Vision** (real-time ML Kit `STREAM_MODE` Object Detection & Tracking + contextual aspect-balanced Image Labeling with confidence hysteresis, EMA bounding-box stabilization, dropout coasting, and shutter photo capture saved directly to Gallery); center-prioritized velocity-continuous ML Kit Smart Object Autofocus across all modes including Vision even when no high-confidence labels are present (strongly prioritizing objects detected closer to the center of the view, targeting the center midpoint between both objects when multiple objects are detected near the center, with persistent `STREAM_MODE` object tracking, elliptical cornering arcs, straight-line target tracking, capped velocity addition, calm maximum reticle speed, smooth arrival deceleration, and 7s tap-to-focus manual override); automatic device default photo directory detection (`DCIM/Camera`, OEM/DCF folders like `DCIM/100ANDRO` or `DCIM/100MEDIA`, `Pictures/Camera`, or direct `DCIM`), front/back/multi-lens switching with selfie mirroring, flash/torch, timer (Off, 3s, 5s, 10s), aspect ratios (4:3, 16:9, 1:1, Full), composition grids, pinch & toggleable zoom slider (`1x`–`8x`), exposure (`-2.0` to `+2.0 EV`), live color filters, direct link to Gallery, and a compact **Camera Modes** pill bar Home Screen AppWidget (`Photo`, `Video`, `Scan`, `Note`, `Vision`). |
| **🏞️ Gallery** | Media | Photo and video album organizer with ML Kit smart tag search, inline video previews, and pinch-to-zoom density | Default `Camera` (`/DCIM`) and `Pictures` (`/Pictures`) folders (plus `/Download` with full storage access and custom scoped folders), pinchable grid density (2 to 5 columns) with toolbar view/column cycling, inline muted video previews while scrolling, background ML Kit image labeling & smart tag overlays with disk index caching, call/voice-recording media filtering, multi-attribute sorting & grouping, bulk share/move/copy, trash bin with restore, and one-tap handoff to **Viewer**, **Image Editor**, or **Video Editor**. |
| **📁 Files** | Files | Built-in offline file and storage manager with cross-tool handoff | Directory browsing, breadcrumb path navigation, quick storage volumes (Internal Storage, Documents, Downloads, Cache), list and grid view modes, multi-attribute sorting (Name, Date, Size, Type), file & folder creation, duplication, safe renaming with extension preservation, recursive deletion, in-app plain text editor, metadata & MD5 checksum inspector, home screen folder shortcuts, and direct cross-tool handoff to **Viewer**, **Image Editor**, **Video Editor**, **Audio Editor**, **Music**, **Document Viewer**, **Archive**, **Convert**, **Notes**, and **To Do**. |
| **🎧 Music** | Audio | Offline background music player with home screen widgets, system media integration, and dynamic album-art theming | Dedicated **Home** (auto-generated ~1-hour mixes—including a text-similarity Genre Mashup that groups genres with similar text and picks a few random genres from a matched group—and preferred streaming app links), **Playlists**, **Songs**, **Albums**, **Artists**, and **Genres** tabs with reorderable tabs and **List / Card** view toggle (2–3 cards per row); background playback with Android `MediaSession`, external system media controller syncing & custom transport actions, unified size-responsive **Music Player** Home Screen AppWidget (automatically switching between **Mini Bar**, **Medium Player**, and **Album Art** views as you resize it); collapsible mini-player and full-screen player sheet with an integrated bottom queue drag handle that reveals the queue inside the same shared album-art gradient background while pushing the controls up and hiding the album art, waveform/seek scrubber, shuffle (`Off`/`Shuffle`/`Smart Shuffle`), repeat (`Off`/`All`/`One`), favorites across songs/artists/albums/genres, custom playlists, track metadata editor, sleep timer, playback speed (`0.5x`–`2.0x`), and dynamic palettes that theme both the player and the entire app. |
| **🎙️ Record** | Audio | Voice and audio recorder with live waveform, search, miniature player, and compact Home Screen AppWidget | Central floating microphone FAB with smooth transition to Stop button, live amplitude waveform, timestamped & searchable recording library, selectable formats (M4A default, WAV, AAC, OGG, 3GP), configurable `/Audio` recordings directory (plus `/Download` with full storage access), miniature playback modal with play/pause and seek slider, direct handoff to **Audio Editor** or **Music**, and a compact **Record** Home Screen AppWidget with one-tap instant voice memo recording. |
| **📏 Ruler** | Math | Precision on-screen ruler, caliper, pitch gauge, protractor, and spirit level | Metric (mm, cm) and Imperial (inches with fractional notation like 5/16" or 2 3/8"), dual-thumb A↔B caliper span, thread & line pitch gauge, 0°–180° angle protractor, 2-axis spirit bubble level, hardware DPI auto-detection, manual calibration slider, and real-world reference comparisons (coins, cards). |
| **🍽️ Recipes** | Lifestyle | Personal recipe book with preloaded recipe catalog search | Preloaded catalog of 38+ curated recipes across 7 categories (Breakfast, Mains & Dinner, Pasta & Grains, Soups & Salads, Sides & Snacks, Baking & Desserts, Sauces & Drinks); time-of-day meal suggestion card (Breakfast, Lunch, Dinner) prioritizing favorites and falling back to the recipe book; favorites-focused main recipe list with category filter pills and animated toolbar search; interactive recipe viewer with dynamic serving scaler, ingredient checklist, and step-by-step cooking tracker; and custom recipe creation & editing. |
| **🌱 Plants** | Lifestyle | Interactive planting and harvest year schedule with care alarms and monthly Home Screen AppWidget | Built-in catalog of 48+ vegetables, leafy greens, roots/alliums, herbs, fruits, and flowers; search by name, category, or month; 12-month year view highlighting planting months in the active theme color and harvesting months in the active accent color; sunlight, spacing, and watering care guides with reminder notifications; custom plant creation & schedule customization; and a **Planting Schedule** Home Screen AppWidget surfacing the monthly planting and harvest columns with `<` / `>` month navigation and one-tap catalog add. |
| **🎸 Tuner** | Instruments | Real-time chromatic pitch detector and instrument tuner | Digital signal processing via autocorrelation, target frequency matching, cents offset needle gauge, synthesized reference tone generator, A4 calibration (415–466 Hz), and 16+ searchable presets across Standard, Drop D, DADGAD, Open tunings, and more (with **Standard** and **Drop D** favorited by default and shared with **Chords**). |
| **🎼 Chords** | Instruments | Algorithmic chord voicing generator, reverse finder, scale explorer, and songbook | Dynamic combinatorial voicing & anatomical fingering solver across 7 instrument families (6/7-string Guitar, 4/5-string Bass, Ukulele, Banjo, Mandolin) and 31 chord qualities, custom tunings & capo (with **Standard** and **Drop D** favorited by default and shared with **Tuner**), interactive reverse chord/scale search, 20 scale & mode fretboard diagrams with positional boxes and diatonic key harmonization, acoustic strum synthesis, and transposable progression songbook. |
| **📺 Remote** | Devices | Bluetooth HID remote for Android TV, PCs, and media devices | Bluetooth device discovery with RSSI signal indicators, pairing and connection lifecycle with inline status and cancel/error feedback, directional D-Pad, media playback and volume controls, live wireless keyboard input synchronization, and touchpad / gyroscope air-mouse cursor navigation. |
| **🎲 Dice** | Miscellaneous | Versatile dice rolling system for tabletop RPGs and board games with interactive D20 Roller Home Screen AppWidget | Supports standard polyhedral dice (D4, D6, D8, D10, D12, D20, D100), custom dice pools, modifiers, Advantage/Disadvantage modes, **Dice Jail** easter egg (`🔒 SENT TO DICE JAIL · 2× NAT 1!` when rolling two consecutive Natural 1s on a D20), searchable **SRD 5.2.1** catalog of all 339 official 5E spells (with full descriptions, casting time, range, components, and duration) and 38 weapons (with damage dice, properties, mastery definitions, weight, and cost) plus CC-BY-4.0 attribution modal, saved RPG presets, roll history log with statistics breakdown, fluid 3D roll physics/animations, responsive layouts adapting to compact and wide displays, and an interactive **D20 Roller** Home Screen AppWidget modeled after the hero card with a 3D faceted D20, critical hit/miss/Dice Jail badges, and a 3-way **Disadvantage / Normal / Advantage** selector. |
| **😊 Good** | Miscellaneous | Minimalist positive message & daily encouragement canvas with compact Home Screen AppWidget | Full-canvas positive message display centered on screen with one-tap refresh (blacklisting words from the current message 80% of the time so consecutive messages feel more random), persistent active-message state memory across launches, and a compact **Good** Home Screen AppWidget bar displaying the positive message on the left and a one-tap refresh button on the right. |
| **🔍 Search** | System | Universal search and instant evaluator across all tools | Search and launch all 36 tools (including hidden tools), evaluate live math expressions, convert units across 6 categories, analyze hex colors & harmonies, evaluate RPG dice formulas, and search across user Notes, To-Do tasks, Calendar events & holidays, Recipes, Plant schedules, Guitar/Instrument tunings, Chords, Scales, Progressions, OBD2 SAE J2012 trouble codes, 118 chemical elements, 67 astrology signs/planets/houses/aspects/constellations, 35 file conversion formats, and app Settings. |
| **📐 Units** | Math | Fast, multidirectional unit conversion utility | Comprehensive conversion engine across 45 units in **Length**, **Weight**, **Temperature**, **Volume**, **Speed**, and **Digital Storage / Data** (bits, bytes, KB, MB, GB, TB, PB, KiB, MiB, GiB, TiB, kb, Mb, Gb, Tb) with instant two-way calculations, quick value presets, and one-tap unit swapping. Hidden by default. |
| **🔊 Decibels** | Audio | Real-time sound pressure level (dB SPL) meter | Semicircular dB SPL gauge (0–120 dB), live min/avg/max statistics, 40-point rolling level history bar graph, calibration offset slider (`-20 dB` to `+20 dB`), 5 environmental noise level classifications, and pause/reset controls. Hidden by default. |
| **🧪 Elements** | Math | Interactive 118-element IUPAC Periodic Table, Bohr shell diagrams, molar mass calculator, and Periodic Table Home Screen AppWidget | 18-group interactive Periodic Table of all 118 elements with Lanthanide/Actinide f-block series, pinch-to-zoom & pan table controls, color-coding by chemical category, phase of matter, or orbital block (`s`/`p`/`d`/`f`), dynamic temperature phase simulator (`0 K`–`6000 K` in `K`/`°C`/`°F`), full-screen element inspector with 2D Bohr electron shell canvas & previous/next element navigation, searchable element catalog with favorites, chemical formula molar mass & mass-percentage calculator, and an interactive **Periodic Table** Home Screen AppWidget whose main view is the full 18-column IUPAC Periodic Table where tapping any element switches to that element's information view (featuring the element tile badge, atomic/physical properties, suborbital electron configuration & Hund's-rule spin-pair orbital boxes `[⇅]`/`[↑]`, an Element Description & Info card on the left, a 2D Bohr electron shell diagram on the right, `←` back-to-table navigation, and `<` / `>` / `★` / `⟳` / `🔍` controls). Hidden by default. |
| **🔄 Convert** | Files | Universal offline file converter powered by Native Android APIs, FFmpeg, ML Kit OCR & Whisper Speech-to-Text | Dynamic conversion capability map across **35 formats** (Image, Audio, Video, Document/Data/Subtitle, and Archive), preferring hardware-accelerated Native Android/Media3 encoders (`Bitmap`, `PdfRenderer`, `PdfDocument`, `MediaCodec`, `Media3 Transformer`, `Zip`/`Tar`) with 16KB-aligned FFmpeg (`ffmpeg-kit-16kb`) as automatic backup, on-device ML Kit OCR for Image/PDF → Text & Structured Data, and bundled offline **Whisper** (`ggml-tiny-q5_1.bin`) Speech-to-Text for transcribing any Audio or Video file into `.txt`, `.srt` subtitles, `.md`, `.html`, `.json`, `.csv`, `.tsv`, or `.xml`. Hidden by default. |
| **📄 Document Viewer** | Files | Offline multi-format document viewer with unified paginated canvas, continuous fit-width scrolling, freehand drawing, and text annotation | Unified paginated sheet rendering across **PDF** (`.pdf`), **Text & Markup** (`.txt`, `.md`, `.rtf`, `.html`, `.log`), and **Office Documents** (`.docx`, `.xlsx`, `.pptx`, `.odt`, `.ods`, `.odp`, plus legacy `.doc`/`.xls`/`.ppt` fallbacks) that fits the screen width on initial load, continuous vertical scrolling across pages in View mode (automatically locked while drawing or adding/moving text annotations), pinch-to-zoom & horizontal pan, freehand drawing (Pen, Highlighter, Eraser) with customizable colors & stroke widths, draggable text annotations with customizable font size & background pills, per-page undo/redo history, built-in sample document generator, and full multi-page annotated PDF export (`PdfDocument`). Hidden by default. |
| **🗜️ Archive** | Files | Offline archive explorer, extractor, and compressor | Browse, inspect, extract, and create `.zip`, `.tar`, `.tar.gz` (`.tgz`), and `.gz` archives with recursive directory packing, configurable compression levels, entry size/compression ratios, and direct integration from **Files**. Hidden by default. |
| **🖼️ Viewer** | Design | Standalone full-screen image & animated GIF/WebP viewer with OCR, QR/barcode, and color inspection | Full black-canvas viewer with pinch-to-zoom (`50%`–`500%`), swipeable horizontal paging across folder images with bottom thumbnail strip, animated GIF/WebP playback controls, ML Kit OCR text / QR & barcode / smart tag recognition inspector, interactive auto-focus color picker reticle with hex pill link to **Colors**, and one-tap handoff to **Image Editor**. Hidden by default. |
| **🖌️ Image Editor** | Design | Non-destructive photo editing, filters, adjustments, and annotation | Aspect-ratio crop presets (1:1, 4:3, 16:9, Free) & straighten, 90° rotation & flip, color adjustments (brightness, contrast, saturation, warmth, vignette), curated filter presets, freehand drawing with variable stroke widths, dedicated eraser tool, text overlay annotations with custom colors, undo/redo history, and high-resolution PNG/JPEG export. Hidden by default. |
| **🎬 Video Editor** | Design | Fast offline video trimmer and editor powered by Media3 & FFmpeg | Interactive filmstrip timeline with draggable trim handles, lossless instant stream-copy (`-c copy`) trimming & muting, hardware-accelerated `h264_mediacodec` + FFmpeg filtergraph pipeline with automatic software fallback, aspect-ratio crop, 90°/180° rotation & flip, `0.25x`–`3.0x` playback speed, audio volume & fade-in/out, real-time color filters & adjustments, frame snapshot capture, undo/redo history, and export to MP4, GIF, and M4A. Hidden by default. |
| **🖥️ Screen Recorder** | Media | Simple one-tap screen & single-app recorder with Mic/Device/Combined audio buttons, Video Editor handoff, and compact Home Screen AppWidget | Minimalist editor-style initial screen featuring the 3 audio capture buttons from **Record** (smaller **Device + Mic** button on the left, larger primary **Microphone** button in the center, and smaller **Device Audio** button on the right) above `"tap to start recording"`, bottom option to select a single app to record (`"record a single app"` opening a searchable installed-app target picker, or full screen), Android 14+ `MediaProjectionConfig` single-app / full-display support and foreground recording notification with Stop action, 1080p/720p/480p H.264 MP4 presets, saved recordings history dialog, one-tap handoff to **Video Editor**, and a compact **Screen Recorder** Home Screen AppWidget with one-tap **Device + Mic**, **Microphone**, and **Device Audio** screen recording buttons. Hidden by default. |
| **🎨 Colors** | Design | Comprehensive color palette, live camera sampler, and accessibility suite | Interactive 2D HSV saturation-value color canvas, live camera color sampling, HEX/RGB/HSV/HSL/CMYK conversion, WCAG 2.1 contrast ratio and accessibility rating (AA/AAA for normal & large text), named color swatches, and 5 harmony generators (complementary, analogous, triadic, split-complementary, shades). Hidden by default. |
| **🎚️ Audio Editor** | Audio | Waveform audio editor with trim, cut, fade, speed, and multi-format export | Interactive waveform canvas with draggable selection handles, real-time `AudioTrack` preview & loop playback, trim (keep selection) & cut (splice out selection), volume gain & peak normalization, fade-in/fade-out envelopes, speed/pitch resampling (`0.5x`–`2.0x`), audio reversal, metadata tag editing, multi-step undo/redo history, and export to WAV, M4A, MP3, AAC, OGG, and FLAC. Hidden by default. |
| **⏱️ Metronome** | Instruments | Low-latency audio-visual tempo keeper | `AudioTrack` PCM pulse synthesis, animated mechanical pendulum, adjustable BPM (10–300) with tap tempo, customizable time signatures, rhythm subdivisions (quarter, eighth, triplet, 16th), accented beats, haptic pulse option, and synchronized visual flash. Hidden by default. |
| **🎮 Gamepad** | Devices | Bluetooth HID wireless controller for games and emulators | Full dual-thumbstick or D-Pad + ABXY layout in landscape mode, L1/R1/L2/R2 shoulder triggers, Start/Select/Home system buttons, Bluetooth HID Gamepad descriptor with low-latency input reporting, and haptic feedback. Hidden by default. |
| **🚗 OBD2** | Devices | Bluetooth Low Energy (BLE) ELM327 vehicle diagnostics scanner | BLE GATT discovery & auto-characteristic binding (`FFF0`, `FFE0`, Nordic UART) plus built-in Demo Vehicle simulator (including a `P0088 — Flux Capacitor: 1.21 Gigawatts Required` temporal displacement easter egg at 88 mph), live Mode 01 semicircular RPM & Speed gauges (`km/h`•`°C` / `mph`•`°F`), Coolant Temp, Battery Voltage (`AT RV`), Engine Load, Throttle, Short/Long-Term Fuel Trim, Intake Air Temp, and MAF telemetry cards, Stored (`Mode 03`) & Pending (`Mode 07`) trouble code scanner with Mode 04 MIL reset, searchable offline SAE J2012 DTC dictionary, Mode 09 VIN decoder, and Mode 01 PID 01 I/M emissions readiness monitors. Hidden by default. |
| **🌙 Stars** | Lifestyle | Offline astrology, daily horoscope, Big Three natal chart calculator, Night Sky live camera constellation overlay, synastry compatibility matcher, lunar phase tracker, and Home Screen AppWidget | 5-tab layout (**Today**, **Chart**, **Night Sky**, **Match**, **Guide**): deterministic transit-backed daily horoscopes across **Overview**, **Love**, **Career & Vibe**, and **Do's & Don'ts** with date scrubber (Yesterday, Today, Tomorrow), lucky number, compatible sign, and power color pill linking directly to **Colors**; **Big Three** (**Sun**, **Moon**, **Rising / Ascendant**) and 10-planet natal ephemeris, interactive 2D **Natal Chart Wheel & Aspect Grid** canvas, whole-sign houses (`I`–`XII`), and user-created birth profiles (**Me**, **Partner**, **Friends**, **Family**—starting empty by default and automatically defaulting to the current birth sign when no profiles are filled out); **Night Sky** live camera constellation viewfinder combining 3D gyroscope/compass celestial projection (`RA/Dec → Altitude/Azimuth`) for 16 constellations (all 12 Zodiac constellations plus **Orion & Orion's Belt**, **Ursa Major / Big Dipper**, **Cassiopeia**, and **Cygnus**) and live Sun/Moon/Planets with on-device **Star-Snap** computer vision that detects bright stars in the camera feed (such as the 3 collinear stars of Orion's Belt) and snaps glowing constellation lines directly onto the real stars on screen, plus one-tap **AR Sky Snapshot** export; live **Moon Phase** terminator canvas with illumination %, lunar age, countdowns to next Full/New Moon, and **Mercury Retrograde** watch; **Synastry Compatibility** analyzer comparing any two saved birth charts or any two zodiac signs across Romance, Emotional Sanctuary, Communication, and Shared Values; searchable 67-entry **Astrology & Constellation Guide** with star favoriting (starting with no default favorites); cross-tool save to **Notes**; and a **Stars** Home Screen AppWidget displaying the active sign glyph badge, daily cosmic mantra, Moon phase, Big Three summary, and one-tap sign cycling. Hidden by default. |
| **💻 Terminal** | System | Android local shell (`/system/bin/sh`) & on-device Wireless ADB loopback (`uid=2000`) console | **Option 1 (Default)**: Zero-setup Local Shell (`/system/bin/sh` via `ProcessBuilder` with Toybox utilities, persistent working directory tracking, environment variable `export`/`unset`, and ANSI VT100/xterm SGR color parsing); **Option 3 (Elevated Privileges)**: Switchable on-device **Wireless ADB Loopback** client (`127.0.0.1:<port>`) implementing the ADB wire protocol (`CNXN`, `STLS` TLS 1.3 upgrade, 2048-bit RSA `AUTH_SIGNATURE` / `AUTH_RSAPUBLICKEY`, `OPEN`, `WRTE`, `OKAY`, `CLSE`), mDNS (`NsdManager`) automatic Wireless Debugging port discovery (`_adb-tls-connect._tcp` & `_adb-tls-pairing._tcp`), and 6-digit Wi-Fi pairing code flow for `uid=2000(shell)` privileges (`dumpsys`, system-wide `logcat`, `pm`, `wm`, `cmd`); tactile **Extra Keys** row (`CTRL+C`, `TAB` path autocomplete, `↑`/`↓` command history, `CLS`, `|`, `>`, `&&`, `/`, `-`, `~`, `$`); adjustable monospace font size (`10sp`–`20sp`) via `SlimDotSlider`; classic hidden shell easter egg commands (`sudo make me a sandwich`, `make me a sandwich`, `cowsay`, `fortune`, `good`, `sl`, `matrix`, `neofetch`, `42`, `flux`); and a searchable catalog of 24+ built-in Android shell presets plus custom user commands. Hidden by default. |
| **⚙️ Settings** | System | Application preferences, theme, colors, background textures, permissions, error logs, license, backup, and tool visibility | Theme mode (System, Light, Dark), dynamic Material You colors with **Current Star Sign** theme color (dynamically coloring the application with the power color of the current calendar star sign) and custom hex color picker, automatic accent color harmonies (Complementary, Analogous, Triadic, Split-Complementary, Shades), live Music Player album-art dynamic theming, **Style** section with procedural background texture presets (**None** default, **Fuzzy**, **Fade**, **Concentric**, **Halftone**, **Topography**, **Aurora**, **Starfield**, **Sunburst**, **Honeycomb**, **Blueprint**, **Waves**, **Confetti**, and **Circuit**) applied across all tools and **Hexagonify** toggle (`"Hexagons are the bestagons"`, transforming circular action icon buttons—including artwork-overlaid favorite star, context menu `...`, and play/playing buttons in Music Player—badges, FABs, and Home Screen widget icons into vertical-sided hexagons, and all navigation pills, filter chips, artwork-overlaid time/duration badges, camera/editor/ruler/settings pill toggle selectors, remote playback & volume control bars, quick-capture/inline pill inputs, action buttons, and modal action buttons across the app into horizontal-aligned hexagons), plus a resizable **Style** Home Screen AppWidget (vertically scrollable style and background texture selector that places an unlabeled textured circle/hexagon shape toggle as its first item followed by **None** and all procedural background texture options, automatically adapting from 1 to 4 columns per row as resized horizontally); navigation drawer density slider (3 to 6 tools per row) and 4 sorting modes (**Manual**, **Tool categories**, **Usage**, and **Alphabetical**); per-tool visibility toggles & standalone Home Screen shortcut pinning; granular **App Permissions** manager with per-tool usage counters and last-used timestamps; **Error Logs** inspector (retaining up to 100 recent diagnostic entries with copy/clear); **License** viewer (displaying the full GNU General Public License v3.0 text with search and copy); full JSON settings & user data backup/restore; **Report a Bug** & **Request a Feature** links (with a 20% easter egg chance to swap in devil & angel icons on a 3-hour cooldown); and a **Donate** button mockup at the bottom of Settings (with a 5% easter egg chance to move to the top of Settings, a 20% chance after 2 seconds in view to briefly flip for 2 seconds with a 0.5-hour cooldown to a message from either the Donate message list or the **Good** tool's positive message list with a 50/50 split, and a temporary `404 Not Found` `¯\_(ツ)_/¯` view when tapped). Always positioned as the final tool. |

---

## ✨ Interface & Experience

- **Customizable Pull-Up Drawer**: Fluid pull-up navigation drawer permanently anchored to the physical bottom of the device regardless of screen rotation, with customizable grid density (3 to 6 tools per row), edge-drag page switching, and persistent last-opened tool memory.
- **Four Navigation Organization Modes**:
  - **Manual**: Personalize your drawer layout with intuitive drag-and-drop tool reordering.
  - **Tool Categories**: Group tools logically into categorized pages with clear category headers, swipeable page indicators, and secondary pill sorting for both categories and tools within categories by Manual, Alpha, or Usage.
  - **Usage**: Automatically sort tools based on real usage frequency, keeping your most frequented utilities within immediate reach.
  - **Alphabetical**: Sort tools by name with selectable ascending (A-Z) or descending (Z-A) ordering.
- **Material Design 3, Accent Harmonies, Background Textures & Live Music Theming**: Full Material You dynamic theming, custom hex seed colors, automatic accent harmonies (Complementary, Analogous, Triadic, Split-Complementary, Shades), procedural background textures across all tools (Fuzzy, Fade, Concentric, Halftone, Topography, Aurora, Starfield, Sunburst, Honeycomb, Blueprint, Waves, Confetti, Circuit), live album-art theme synchronization from the built-in Music Player or active Android system media sessions (automatically propagating theme, accent, Star Sign, dark/light mode, and media palette changes across all pinned Home Screen AppWidgets in real time), tonal elevation, and complete dark / light mode support.
- **Standalone Home Screen Shortcuts & AppWidgets**: Pin any of the 35 tools (or specific storage folders from Files) to the Android Home Screen as standalone document tasks with dedicated adaptive & monochrome icons in the Recent Apps switcher, plus 13 Home Screen AppWidgets: resizable **To Do List**, configurable **Notes** (**All Notes**, **Pinned Notes**, or a **Specific Label**), size-responsive **Music Player** (automatically switching between **Mini Bar**, **Medium Player**, and **Album Art** layouts), compact **Record** (one-tap voice memo recording), **Planting Schedule** (monthly planting & harvest breakdown with month switcher), compact **Camera Modes** pill bar (**Photo**, **Video**, **Scan**, **Note**, **Vision**), interactive **D20 Roller** (3D faceted D20 hero card with critical hit/miss badges and **Disadvantage / Normal / Advantage** selector), vertically scrollable **Style** selector (with an unlabeled textured circle/hexagon shape toggle as the first item followed by **None** and all 13 procedural background textures, adapting from 1 to 4 columns as resized horizontally), compact **Good** bar (positive message on the left with a one-tap refresh button on the right), **Stars** (daily horoscope mantra, Moon phase & sign, Big Three natal summary, and one-tap zodiac sign switcher), **Calendar Schedule** (scrollable Google Calendar Schedule view with left weekday/date badges and right color-coded event cards for upcoming events, Today badge jump, and one-tap event creation), **Calendar Month** (6×7 monthly grid with weekday headers, Today highlight, colored event dots, `<` / `>` month navigation, Today badge jump, and one-tap event creation), and **Periodic Table** (interactive 18-column IUPAC Periodic Table main view where tapping any of the 118 elements switches to that element's information view with its suborbital electron configuration, Hund's-rule orbital spin-box diagram, 2D Bohr electron shell diagram, `←` back-to-table button, and `<` / `>` / `★` / `⟳` / `🔍` controls).
- **Seamless Cross-Tool Handoffs, Standalone "Open with..." Apps & Rich Sharesheet Previews**: Unified context actions allow instant file and content handoffs across tools (e.g., opening files from **Files** or **Gallery** directly into **Viewer**, **Image Editor**, **Video Editor**, **Audio Editor**, **Music**, **Document Viewer**, **Archive**, or **Convert**, or sending OCR scans and text to **Notes** and **To Do**). Every file-handling tool (**Document Viewer**, **Viewer**, **Gallery**, **Image Editor**, **Colors**, **Video Editor**, **Music**, **Audio Editor**, **Archive**, **Convert**, **Notes**, and **Files**) also registers its dedicated `<activity-alias>` icon and label with Android's system **"Open with..."** picker (`ACTION_VIEW` / `ACTION_EDIT`), launching directly as a standalone app with the selected file loaded. Single and multi-file sharing attaches `ClipData` and uses a custom `ToolboxFileProvider` (`getStreamTypes` + `openTypedAssetFile` video frame extraction) so the Android system Sharesheet renders rich visual thumbnail previews for both images and videos.
- **Dock & Background Seamlessness**: Coordinated background color transitions ensure docked action bars (such as in To Do and Ruler) blend seamlessly with the bottom navigation drawer.
- **Smart Per-Tool Device Rotation**: Each tool enforces its ideal screen orientation policy (`Tool.screenOrientation`)—locking handheld utilities, editors, and file managers (**Calculate**, **To Do**, **Camera**, **Files**, **Units**, **Ruler**, **Record**, **Music**, **Remote**, **Tuner**, **Recipes**, **Plants**, **Dice**, **Image Editor**, **Video Editor**, **Screen Recorder**, **Audio Editor**, **Convert**, **Document Viewer**, **Archive**, **Decibels**, **Metronome**, **Colors**, **OBD2**, **Good**, **Stars**, and **Settings**) to **Portrait**, locking **Gamepad** to **Sensor Landscape**, and allowing free rotation on wide-canvas and reading/typing tools (**Notes**, **Gallery**, **Chords**, **Viewer**, **Elements**, **Calendar**, **Search**, and **Terminal**) while automatically restoring parent orientation across inline cross-tool handoffs.
- **Edge-to-Edge & Gesture Navigation**: Modern edge-to-edge layout supporting system gesture navigation, window insets, predictive back animations, two-finger pinch-to-zoom grid density (`Modifier.pinchToChangeColumns`), and horizontal swipe navigation between pill-bar views (`Modifier.swipeToSwitchPillView`) with automatic pill bar scroll-into-view.
- **Offline & Completely Private**: Zero analytics, telemetry, or external network requests. All computations, on-device ML Kit vision, DSP, audio synthesis, and file operations happen entirely on-device.

---

## 🎨 Design Guidelines

For architectural and visual specifications, refer to [DESIGN_LANGUAGE.md](DESIGN_LANGUAGE.md) and [AGENTS.md](AGENTS.md). They outline:
- Material 3 color tokens and surface elevation standards
- Typography scale and formatting rules
- Standard shapes (`28.dp` dialogs/sheets, `14.dp` cards/fields, `CircleShape` badges/FABs) and touch target guidelines
- **Component patterns** for `ToolboxTopBar`, `ToolboxPillNavigationBar`, `SlimDotSlider`, expanding search bars, multi-select toolbars, dialogs, and animations

---

## 📄 Custom `.text` File Format Specification

Both **Notes** and **To Do** persist their data as human-readable UTF-8 `.text` files (`NOTES_FILE_EXTENSION = "text"`): **Notes** requests scoped storage access to `/Notes` and saves each note as an individual `.text` file, while **To Do** requests scoped storage access to `/ToDo` and saves the entire To Do list (active tasks, completed tasks, and nested subtasks) as a single `/ToDo/to-do.text` file using nested lists. The `.text` format is a clean superset of Markdown designed for plain-text notes, nested checklists, and tagged organization:

1. **Title Heading (First Non-Blank Line)**:
   - Prefixed with `# ` followed by the note or list title (e.g., `# Camping Checklist`).
2. **Body Content (Markdown Text or Interactive Checklist)**:
   - **Text Notes**: Standard multi-line Markdown prose separated from the title by a blank line.
   - **Checklists & Tasks**: Each item is placed on its own line:
     - `- <item text>` denotes an **unchecked / active** item.
     - `+ <item text>` denotes a **checked / completed** item (internally replacing `-` with `+` when checked).
   - **Checklist Nesting (Tab Indentation)**:
     - Hierarchical nesting of checklist items and subtasks is denoted by leading tab characters (`\t`), with one `\t` per nesting level (`indentLevel = 0` at root, `\t` for level 1, `\t\t` for level 2, etc.).
3. **Labels Footer (Last Non-Blank Line)**:
   - Notes with labels store them on the final line as a comma-separated list where each label is prefixed with `#`:
     ```text
     #Label Name 1, #Label Name 2, #Label Name 3
     ```
   - The leading `#` on each label token is an internal `.text` format marker and is stripped automatically when parsed so `#` is never displayed in the UI.

### Example `.text` File

```text
# Camping Checklist

- Tent
	- Rainfly & stakes
		+ Titanium pegs
+ Sleeping bag
- Water filter

#outdoors, #weekend trip
```

---

## 🏗️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/) 2.3 (JVM Toolchain 17)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 (`androidx.compose.material3`)
- **Navigation**: [AndroidX Navigation 3](https://developer.android.com/guide/navigation) (`androidx.navigation3`)
- **Architecture**: MVVM with Kotlin Coroutines and `StateFlow`
- **Build System**: Gradle 9.x with Android Gradle Plugin (AGP) 9.x, Version Catalogs (`libs.versions.toml`)
- **Media, Conversion, Transcoding & Speech-to-Text**: AndroidX Media3 (`media3-transformer`, `media3-effect`), `PdfRenderer` / `PdfDocument`, `MediaCodec`, 16KB-page-aligned FFmpeg (`ffmpeg-kit-16kb`), and offline Whisper Speech-to-Text (`whisper-android` + bundled quantized `ggml-tiny-q5_1.bin`)
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
│       │   │                                     #  - AudioRecorderScreen.kt, AudioRecorderEngine.kt & AudioRecorderWidgets.kt
│       │   │                                     #  - BluetoothGamepadScreen.kt
│       │   │                                     #  - BluetoothRemoteScreen.kt & BluetoothRemoteViewModel.kt
│       │   │                                     #  - BluetoothHidProtocol.kt & BluetoothHidSession.kt
│       │   │                                     #  - CalculatorScreen.kt & CalculatorEngine.kt
│       │   │                                     #  - CalendarScreen.kt, CalendarEngine.kt & CalendarWidgets.kt
│       │   │                                     #  - CameraScreen.kt, CameraEngine.kt & CameraWidgets.kt
│       │   │                                     #  - ChordBookScreen.kt & ChordBookEngine.kt
│       │   │                                     #  - ColorPickerScreen.kt
│       │   │                                     #  - DecibelMeterScreen.kt
│       │   │                                     #  - DiceRollerScreen.kt, DndSrdCatalog.kt & DiceRollerWidgets.kt
│       │   │                                     #  - ElementsScreen.kt, ElementsEngine.kt & ElementsWidgets.kt
│       │   │                                     #  - FileConverterScreen.kt & FileConverterEngine.kt
│       │   │                                     #  - FilesScreen.kt & FileManagerEngine.kt
│       │   │                                     #  - GalleryScreen.kt & GalleryEngine.kt
│       │   │                                     #  - GoodScreen.kt & GoodWidgets.kt
│       │   │                                     #  - GuitarTunerScreen.kt
│       │   │                                     #  - ImageScreen.kt
│       │   │                                     #  - ImageEditorScreen.kt & ImageEditorEngine.kt
│       │   │                                     #  - MetronomeScreen.kt
│       │   │                                     #  - MusicPlayerScreen.kt, MusicPlayerEngine.kt & MusicPlayerWidgets.kt
│       │   │                                     #  - NotesScreen.kt, NotesModel.kt & NotesWidgets.kt
│       │   │                                     #  - Obd2ScannerScreen.kt & Obd2ScannerEngine.kt
│       │   │                                     #  - DocumentViewerScreen.kt & DocumentViewerEngine.kt
│       │   │                                     #  - PlantScheduleScreen.kt, PlantScheduleModel.kt, PlantIcons.kt & PlantScheduleWidgets.kt
│       │   │                                     #  - RecipeBookScreen.kt & RecipeBookModel.kt
│       │   │                                     #  - RulerScreen.kt
│       │   │                                     #  - ScreenRecorderScreen.kt, ScreenRecorderEngine.kt & ScreenRecorderWidgets.kt
│       │   │                                     #  - SearchScreen.kt & SearchEngine.kt
│       │   │                                     #  - SettingsScreen.kt & BackgroundTextureWidgets.kt
│       │   │                                     #  - StarsScreen.kt, StarsEngine.kt & StarsWidgets.kt
│       │   │                                     #  - TerminalScreen.kt & TerminalEngine.kt
│       │   │                                     #  - TodoListScreen.kt, TodoListModel.kt, TodoListWidgets.kt & TodoNotificationReceiver.kt
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

- **`android.permission.CAMERA`**: Required by **Camera** (to capture photos, record videos, scan QR codes/barcodes, extract OCR document notes, and run ML Kit Vision detection), **Colors** (live camera color sampling), and **Stars** (live **Night Sky** constellation camera overlay and Star-Snap detection).
- **`android.permission.RECORD_AUDIO`**: Required by **Tuner** (real-time fundamental frequency pitch analysis), **Decibels** (real-time sound pressure level measurement), **Record** (voice and audio recording), and **Camera** (audio capture during video recording). Audio streams for the Tuner and Decibels are analyzed transiently in memory and are never saved or transmitted.
- **`android.permission.MANAGE_EXTERNAL_STORAGE` / `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE`**: Required by **Files** to browse and manage all device storage volumes, and unlocks full `/Download` folder access and cross-folder operations in **Gallery**, **Record**, **Music**, and local media tools (**Viewer**, **Image Editor**, **Video Editor**, **Audio Editor**, **Document Viewer**, **Archive**, and **Convert**).
- **`android.permission.BLUETOOTH` / `BLUETOOTH_ADMIN` / `ACCESS_FINE_LOCATION` (Android 11 and below) & `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` / `BLUETOOTH_ADVERTISE` (Android 12+)**: Required by **Remote** and **Gamepad** (Bluetooth HID peripheral registration and low-latency input transmission), **OBD2** (BLE GATT ELM327 vehicle diagnostics), and **Music** (Bluetooth audio route detection). `neverForLocation` is asserted on Android 12+.
- **`android.permission.POST_NOTIFICATIONS`**: Required by **To Do** (scheduled task alarm notifications), **Calendar** (scheduled event reminder notifications), **Music** (media playback notifications), and **Screen Recorder** (ongoing foreground recording status and stop control notification).
- **`android.permission.SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`**: Required by **To Do** and **Calendar** to trigger scheduled task and event reminders at exact due times.
- **`android.permission.RECEIVE_BOOT_COMPLETED`**: Required by **To Do** and **Calendar** to automatically reschedule active task and event reminder alarms after a device reboot.
- **`android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`**: Optional system access used by **Music** and **Settings** (`ToolboxMediaListenerService`) to observe active Android media sessions for dynamic album-art theming and external media transport controls.
- **`android.permission.VIBRATE`**: Used by **Metronome**, **Dice**, **Remote**, **Gamepad**, **To Do**, and **Calendar** for tactile feedback, beat pulses, and alert vibrations.
- **`android.permission.REQUEST_INSTALL_PACKAGES`**: Used by **Files** to launch the system package installer when opening local `.apk` packages.

---

## 📄 License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.
