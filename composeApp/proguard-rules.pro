# Reglas ProGuard para YapeHub

# Mantener clases de datos serializables
-keep class org.sysarp.project.data.** { *; }

# Mantener clases de Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Mantener clases de Compose
-keep class androidx.compose.** { *; }
-keep class org.jetbrains.compose.** { *; }

# Mantener clases de Koin
-keep class org.koin.** { *; }

# Mantener clases de Ktor
-keep class io.ktor.** { *; }

# Mantener ViewModels
-keep class org.sysarp.project.viewmodel.** { *; }

# Mantener servicios
-keep class org.sysarp.project.service.** { *; }

# Reglas generales
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses