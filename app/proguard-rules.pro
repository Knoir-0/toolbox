# Toolbox R8 Tree-Shaking Rules (Debug & Release)
# Shrinks unused library classes (Guava, Play Services, Media3, Compose internals)
# while preserving readable class/method names, line numbers, JNI bindings, and reflection.

-dontobfuscate
-dontoptimize
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep all Toolbox application classes and members intact so reflection, serialization,
# Compose previews, and stack traces always work identically to unminified builds.
-keep class com.example.toolbox.** { *; }

# Keep FFmpegKit and Whisper JNI classes and native methods
-keep class com.arthenica.ffmpegkit.** { *; }
-keep class dev.ffmpegkit.whisper.** { *; }

# Keep ML Kit & Google Play Services native/reflective entry points
-keep @com.google.android.gms.common.annotation.KeepName class * { *; }
-keep @androidx.annotation.Keep class * { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**
-dontwarn com.google.common.**
