# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

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

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have problems with serialization.
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.atomicfu.**
-dontwarn io.netty.**
-dontwarn com.typesafe.**
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**
-keep class java.lang.management.** { *; }

# Koin
-keep class org.koin.** { *; }
-keep class * extends org.koin.core.module.Module
-keep class * extends org.koin.core.component.KoinComponent

# Keep data classes used for serialization
-keep @kotlinx.serialization.Serializable class ** {
    *;
}

# Keep your project's data classes
-keep class org.sysarp.project.data.** { *; }
-keep class org.sysarp.project.model.** { *; }
-keep class org.sysarp.project.service.** { *; }

# Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**