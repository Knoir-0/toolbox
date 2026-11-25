import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
}

android {
  namespace = "com.example.toolbox"
  compileSdk = 36
  defaultConfig {
    applicationId = "com.example.toolbox"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"
    ndk {
      abiFilters += listOf("arm64-v8a")
    }
  }

  buildTypes {
    debug {
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    release {
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  buildFeatures {
    compose = true
    aidl = false
    buildConfig = false
    shaders = false
  }

  packaging {
    jniLibs {
      useLegacyPackaging = true
      pickFirsts += "**/libc++_shared.so"
    }
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

kotlin {
  jvmToolchain(17)
}

val materialIconsExtendedConfiguration: Configuration by configurations.creating {
  isCanBeConsumed = false
  isCanBeResolved = true
  attributes {
    attribute(
      com.android.build.api.attributes.BuildTypeAttr.ATTRIBUTE,
      objects.named(com.android.build.api.attributes.BuildTypeAttr::class.java, "release")
    )
  }
}

abstract class StripUnusedMaterialIconsTask : DefaultTask() {
  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val sourceFiles: ConfigurableFileCollection

  @get:InputFiles
  @get:PathSensitive(PathSensitivity.NONE)
  abstract val iconArtifacts: ConfigurableFileCollection

  @get:OutputFile
  abstract val outputJar: RegularFileProperty

  @TaskAction
  fun strip() {
    val usedClasses = mutableSetOf<String>()
    val importRegex = Regex("""import\s+androidx\.compose\.material\.icons\.([a-z.]+)\.([A-Za-z0-9_]+)""")
    val usageRegex = Regex("""Icons\.((?:AutoMirrored\.)?(?:Filled|Outlined|Rounded|Sharp|TwoTone|Default))\.([A-Za-z0-9_]+)""")

    sourceFiles.files.filter { it.isFile && it.extension == "kt" }.forEach { file ->
      val text = file.readText()
      importRegex.findAll(text).forEach { match ->
        val pkg = match.groupValues[1].replace('.', '/')
        val name = match.groupValues[2]
        usedClasses.add("androidx/compose/material/icons/$pkg/${name}Kt.class")
      }
      usageRegex.findAll(text).forEach { match ->
        val pkg = match.groupValues[1].lowercase().replace("default", "filled").replace('.', '/')
        val name = match.groupValues[2]
        usedClasses.add("androidx/compose/material/icons/$pkg/${name}Kt.class")
      }
    }

    val extendedAar = iconArtifacts.files.firstOrNull {
      it.name.contains("material-icons-extended") && (it.extension == "aar" || it.extension == "jar")
    } ?: error("Could not locate material-icons-extended artifact in ${iconArtifacts.files}")

    val outFile = outputJar.get().asFile
    outFile.parentFile.mkdirs()

    fun copyFilteredClasses(classesInputZip: ZipFile, zos: ZipOutputStream) {
      val entries = classesInputZip.entries()
      while (entries.hasMoreElements()) {
        val entry = entries.nextElement()
        val name = entry.name
        val keep = !name.endsWith("Kt.class") || name in usedClasses
        if (keep && !entry.isDirectory) {
          val newEntry = ZipEntry(name)
          newEntry.time = 347155260000L
          zos.putNextEntry(newEntry)
          classesInputZip.getInputStream(entry).use { it.copyTo(zos) }
          zos.closeEntry()
        }
      }
    }

    ZipOutputStream(outFile.outputStream().buffered()).use { zos ->
      if (extendedAar.extension == "aar") {
        val tempJar = File.createTempFile("material-icons-ext-classes", ".jar")
        try {
          ZipFile(extendedAar).use { aarZip ->
            val classesEntry = aarZip.getEntry("classes.jar") ?: error("No classes.jar in $extendedAar")
            aarZip.getInputStream(classesEntry).use { input ->
              tempJar.outputStream().use { output -> input.copyTo(output) }
            }
          }
          ZipFile(tempJar).use { jarZip -> copyFilteredClasses(jarZip, zos) }
        } finally {
          tempJar.delete()
        }
      } else {
        ZipFile(extendedAar).use { jarZip -> copyFilteredClasses(jarZip, zos) }
      }
    }
  }
}

val stripUnusedMaterialIcons = tasks.register<StripUnusedMaterialIconsTask>("stripUnusedMaterialIcons") {
  group = "build"
  description = "Extracts only the Material Extended icons referenced in the project into a slim JAR."
  sourceFiles.from(fileTree("src") { include("**/*.kt") })
  iconArtifacts.from(materialIconsExtendedConfiguration)
  outputJar.set(layout.buildDirectory.file("generated/material-icons/material-icons-extended-trimmed.jar"))
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)
  materialIconsExtendedConfiguration(composeBom)
  materialIconsExtendedConfiguration(libs.androidx.compose.material.icons.extended)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(files(stripUnusedMaterialIcons.flatMap { it.outputJar }))
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation("org.json:json:20240303")

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Media3, FFmpeg & Whisper Speech-to-Text (File Converter)
  implementation(libs.androidx.media3.transformer)
  implementation(libs.androidx.media3.effect)
  implementation(libs.androidx.media3.common)
  implementation(libs.ffmpeg.kit.android)
  implementation(libs.whisper.android)

  // ZXing (QR Code Reader & Barcode Scanner)
  implementation(libs.zxing.core)

  // Google ML Kit Text Recognition v2 (On-device OCR for Document / Note Scanner & Gallery Search)
  implementation(libs.mlkit.text.recognition)
  // Google ML Kit Image Labeling (On-device automatic image tagging for Gallery)
  implementation(libs.mlkit.image.labeling)
  // Google ML Kit Object Detection & Tracking (On-device localized object bounding boxes)
  implementation(libs.mlkit.objectDetection)
}

abstract class SyncRootApkTask : DefaultTask() {
  @get:InputFile
  @get:PathSensitive(PathSensitivity.NONE)
  abstract val sourceApk: RegularFileProperty

  @get:OutputFile
  abstract val destinationApk: RegularFileProperty

  @TaskAction
  fun sync() {
    sourceApk.get().asFile.copyTo(destinationApk.get().asFile, overwrite = true)
  }
}

val syncRootDebugApk = tasks.register<SyncRootApkTask>("syncRootDebugApk") {
  group = "build"
  description = "Copies the built debug APK to the workspace root app-debug.apk."
  dependsOn("packageDebug")
  sourceApk.set(layout.buildDirectory.file("outputs/apk/debug/app-debug.apk"))
  destinationApk.set(rootProject.layout.projectDirectory.file("app-debug.apk"))
}

tasks.matching { it.name == "assembleDebug" || it.name == "packageDebug" }.configureEach {
  finalizedBy(syncRootDebugApk)
}

tasks.register<Exec>("runDebug") {
  group = "application"
  description = "Builds, delta-installs, and launches the debug APK on the connected emulator or device."
  dependsOn("assembleDebug")
  val apkFile = layout.buildDirectory.file("outputs/apk/debug/app-debug.apk")
  inputs.file(apkFile)
  commandLine("android", "run", "--apks=${apkFile.get().asFile.absolutePath}")
}
