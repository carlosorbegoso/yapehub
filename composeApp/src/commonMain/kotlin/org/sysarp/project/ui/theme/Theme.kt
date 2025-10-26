package org.sysarp.project.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de colores moderna y vibrante para YapeHub
val YapePrimary = Color(0xFF1976D2) // Azul vibrante y moderno
val YapeOnPrimary = Color(0xFFFFFFFF)
val YapePrimaryContainer = Color(0xFFE3F2FD)
val YapeOnPrimaryContainer = Color(0xFF0D47A1)

val YapeSecondary = Color(0xFFFFC107) // Amarillo dorado vibrante
val YapeOnSecondary = Color(0xFF000000)
val YapeSecondaryContainer = Color(0xFFFFF8E1)
val YapeOnSecondaryContainer = Color(0xFFE65100)

val YapeTertiary = Color(0xFF4CAF50) // Verde moderno para éxito
val YapeOnTertiary = Color(0xFFFFFFFF)
val YapeTertiaryContainer = Color(0xFFE8F5E8)
val YapeOnTertiaryContainer = Color(0xFF2E7D32)

val YapeAccent = Color(0xFFE91E63) // Rosa vibrante para destacar
val YapeOnAccent = Color(0xFFFFFFFF)
val YapeAccentContainer = Color(0xFFFCE4EC)
val YapeOnAccentContainer = Color(0xFFAD1457)

val YapeError = Color(0xFFD32F2F)
val YapeOnError = Color(0xFFFFFFFF)
val YapeErrorContainer = Color(0xFFFFEBEE)
val YapeOnErrorContainer = Color(0xFFB71C1C)

// Colores de fondo modernos y elegantes
val YapeBackground = Color(0xFFF8F9FA) // Blanco moderno con toque gris
val YapeOnBackground = Color(0xFF212529) // Negro moderno
val YapeSurface = Color(0xFFFFFFFF)
val YapeOnSurface = Color(0xFF212529)
val YapeSurfaceVariant = Color(0xFFF1F3F4) // Gris muy claro moderno
val YapeOnSurfaceVariant = Color(0xFF5F6368) // Gris medio moderno

val YapeOutline = Color(0xFFDADCE0) // Gris suave moderno
val YapeOutlineVariant = Color(0xFFE8EAED) // Gris muy claro moderno

// Colores adicionales para overlays y efectos modernos
val YapeOverlay = Color(0x80000000) // Overlay semi-transparente
val YapeGradientStart = Color(0xFF1976D2) // Azul vibrante
val YapeGradientEnd = Color(0xFFFFC107) // Amarillo dorado vibrante
val YapeCardElevation = Color(0x0A000000) // Sombra muy sutil
val YapeSuccess = Color(0xFF4CAF50) // Verde para éxito
val YapeWarning = Color(0xFFFF9800) // Naranja para advertencias
val YapeInfo = Color(0xFF2196F3) // Azul para información

// Tema claro
private val LightColorScheme = lightColorScheme(
    primary = YapePrimary,
    onPrimary = YapeOnPrimary,
    primaryContainer = YapePrimaryContainer,
    onPrimaryContainer = YapeOnPrimaryContainer,
    
    secondary = YapeSecondary,
    onSecondary = YapeOnSecondary,
    secondaryContainer = YapeSecondaryContainer,
    onSecondaryContainer = YapeOnSecondaryContainer,
    
    tertiary = YapeTertiary,
    onTertiary = YapeOnTertiary,
    tertiaryContainer = YapeTertiaryContainer,
    onTertiaryContainer = YapeOnTertiaryContainer,
    
    error = YapeError,
    onError = YapeOnError,
    errorContainer = YapeErrorContainer,
    onErrorContainer = YapeOnErrorContainer,
    
    background = YapeBackground,
    onBackground = YapeOnBackground,
    surface = YapeSurface,
    onSurface = YapeOnSurface,
    surfaceVariant = YapeSurfaceVariant,
    onSurfaceVariant = YapeOnSurfaceVariant,
    
    outline = YapeOutline,
    outlineVariant = YapeOutlineVariant
)

// Tema oscuro moderno y vibrante
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6), // Azul claro vibrante para modo oscuro
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF1976D2),
    onPrimaryContainer = Color(0xFFE3F2FD),
    
    secondary = Color(0xFFFFD54F), // Amarillo dorado vibrante para modo oscuro
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFFFFC107),
    onSecondaryContainer = Color(0xFFFFF8E1),
    
    tertiary = Color(0xFF81C784), // Verde claro vibrante para modo oscuro
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF4CAF50),
    onTertiaryContainer = Color(0xFFE8F5E8),
    
    error = Color(0xFFEF5350),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFFD32F2F),
    onErrorContainer = Color(0xFFFFEBEE),
    
    background = Color(0xFF0D1117), // Fondo oscuro moderno (GitHub style)
    onBackground = Color(0xFFE6EDF3), // Gris claro moderno para texto
    surface = Color(0xFF161B22), // Superficie oscura moderna
    onSurface = Color(0xFFE6EDF3),
    surfaceVariant = Color(0xFF21262D), // Variante de superficie moderna
    onSurfaceVariant = Color(0xFF8B949E), // Gris claro moderno para texto secundario
    
    outline = Color(0xFF30363D), // Contorno moderno
    outlineVariant = Color(0xFF21262D) // Variante de contorno moderna
)

@Composable
fun YapeHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = YapeTypography,
        content = content
    )
}
