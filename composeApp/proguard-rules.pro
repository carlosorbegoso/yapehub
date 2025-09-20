# ProGuard rules para YapeHub
# Mantener clases de Compose
-keep class androidx.compose.** { *; }
-keep class kotlinx.compose.** { *; }

# Mantener clases de Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Mantener clases de Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Mantener clases de ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Mantener clases de CameraX
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# Reglas para SLF4J (logging)
-dontwarn org.slf4j.**
-dontwarn org.slf4j.impl.**
-keep class org.slf4j.** { *; }

# Reglas para Timber
-keep class timber.log.** { *; }
-dontwarn timber.log.**

# Reglas generales de Android
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Mantener clases nativas
-keepclasseswithmembernames class * {
    native <methods>;
}

# Reglas para Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**

# Reglas para Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Optimizaciones
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify
