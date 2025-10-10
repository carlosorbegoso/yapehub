package org.sysarp.project.ui.common.components.responsive

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Sistema responsive para diferentes tamaños de pantalla
 */

/**
 * Breakpoints para diferentes tamaños de pantalla
 */
object ScreenBreakpoints {
    val Mobile = 600.dp
    val Tablet = 840.dp
    val Desktop = 1200.dp
}

/**
 * Configuración responsive para diferentes componentes
 */
@Composable
fun ResponsiveConfig(
    mobile: Dp = 16.dp,
    tablet: Dp = 24.dp,
    desktop: Dp = 32.dp,
    screenWidth: Dp = 0.dp
): Dp {
    return when {
        screenWidth >= ScreenBreakpoints.Desktop -> desktop
        screenWidth >= ScreenBreakpoints.Tablet -> tablet
        else -> mobile
    }
}

/**
 * Padding responsive
 */
@Composable
fun ResponsivePadding(
    modifier: Modifier = Modifier,
    mobile: PaddingValues = PaddingValues(16.dp),
    tablet: PaddingValues = PaddingValues(24.dp),
    desktop: PaddingValues = PaddingValues(32.dp),
    screenWidth: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val padding = when {
        screenWidth >= ScreenBreakpoints.Desktop -> desktop
        screenWidth >= ScreenBreakpoints.Tablet -> tablet
        else -> mobile
    }
    
    Box(
        modifier = modifier.padding(padding)
    ) {
        content()
    }
}

/**
 * Column responsive que se adapta al tamaño de pantalla
 */
@Composable
fun ResponsiveColumn(
    modifier: Modifier = Modifier,
    mobileColumns: Int = 1,
    tabletColumns: Int = 2,
    desktopColumns: Int = 3,
    screenWidth: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val columns = when {
        screenWidth >= ScreenBreakpoints.Desktop -> desktopColumns
        screenWidth >= ScreenBreakpoints.Tablet -> tabletColumns
        else -> mobileColumns
    }
    
    if (columns == 1) {
        Column(modifier = modifier) {
            content()
        }
    } else {
        // Para múltiples columnas, usar LazyVerticalGrid sería mejor
        // pero por simplicidad usamos Column aquí
        Column(modifier = modifier) {
            content()
        }
    }
}

/**
 * Card responsive que se adapta al tamaño de pantalla
 */
@Composable
fun ResponsiveCard(
    modifier: Modifier = Modifier,
    mobileElevation: Dp = 2.dp,
    tabletElevation: Dp = 4.dp,
    desktopElevation: Dp = 6.dp,
    screenWidth: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val elevation = when {
        screenWidth >= ScreenBreakpoints.Desktop -> desktopElevation
        screenWidth >= ScreenBreakpoints.Tablet -> tabletElevation
        else -> mobileElevation
    }
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        content()
    }
}

/**
 * Texto responsive que se adapta al tamaño de pantalla
 */
@Composable
fun ResponsiveText(
    text: String,
    modifier: Modifier = Modifier,
    mobileStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    tabletStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    desktopStyle: TextStyle = MaterialTheme.typography.titleMedium,
    screenWidth: Dp = 0.dp
) {
    val style = when {
        screenWidth >= ScreenBreakpoints.Desktop -> desktopStyle
        screenWidth >= ScreenBreakpoints.Tablet -> tabletStyle
        else -> mobileStyle
    }
    
    Text(
        text = text,
        modifier = modifier,
        style = style
    )
}

/**
 * Botón responsive que se adapta al tamaño de pantalla
 */
@Composable
fun ResponsiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    mobileHeight: Dp = 40.dp,
    tabletHeight: Dp = 48.dp,
    desktopHeight: Dp = 56.dp,
    screenWidth: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val height = when {
        screenWidth >= ScreenBreakpoints.Desktop -> desktopHeight
        screenWidth >= ScreenBreakpoints.Tablet -> tabletHeight
        else -> mobileHeight
    }
    
    Button(
        onClick = onClick,
        modifier = modifier.height(height)
    ) {
        content()
    }
}

/**
 * Grid responsive para tablets y desktop
 */
@Composable
fun ResponsiveGrid(
    modifier: Modifier = Modifier,
    mobileColumns: Int = 1,
    tabletColumns: Int = 2,
    desktopColumns: Int = 3,
    screenWidth: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val columns = when {
        screenWidth >= ScreenBreakpoints.Desktop -> desktopColumns
        screenWidth >= ScreenBreakpoints.Tablet -> tabletColumns
        else -> mobileColumns
    }
    
    // Para implementación completa, usar LazyVerticalGrid
    // Por ahora usamos Column como placeholder
    Column(modifier = modifier) {
        content()
    }
}
