# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#-keep class androidx.lifecycle.** { *; }
#-keep interface androidx.lifecycle.** { *; }

# Keep all classes and methods for debugging
-keep class * {
    *;
}

# Keep line numbers and source file names for debugging
-keepattributes SourceFile,LineNumberTable

# Keep annotations
-keepattributes *Annotation*

# Speicher-Optimierungen
-dontoptimize
-dontobfuscate
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# GSON
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# NEU Repository Parser
-keep class io.github.moulberry.repo.** { *; }
-keep class moe.nea.neurepoparser.** { *; }

# JGit
-keep class org.eclipse.jgit.** { *; }
-dontwarn org.eclipse.jgit.**

# Reflection
-keep class org.reflections.** { *; }
-dontwarn org.reflections.**

# Custom classes
-keep class de.hype.hypenotify.** { *; }

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Optimizations for memory
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively

# Remove unused resources
-dontwarn **
-ignorewarnings
