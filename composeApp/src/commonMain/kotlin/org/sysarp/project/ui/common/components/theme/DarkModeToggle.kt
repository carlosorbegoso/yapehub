package org.sysarp.project.ui.common.components.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Toggle para cambiar entre modo claro y oscuro
 */
@Composable
fun DarkModeToggle(
    isDarkMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animationDuration = 300
    
    val scale by animateFloatAsState(
        targetValue = if (isDarkMode) 1f else 0.8f,
        animationSpec = tween(animationDuration),
        label = "scale"
    )
    
    val backgroundColor by animateFloatAsState(
        targetValue = if (isDarkMode) 1f else 0f,
        animationSpec = tween(animationDuration),
        label = "background"
    )
    
    Card(
        modifier = modifier
            .size(56.dp)
            .scale(scale)
            .clickable { onToggle() },
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Fondo animado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(
                            alpha = backgroundColor * 0.2f
                        ),
                        shape = CircleShape
                    )
            )
            
            // Icono animado
            Icon(
                imageVector = if (isDarkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                contentDescription = if (isDarkMode) "Modo oscuro" else "Modo claro",
                tint = if (isDarkMode) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Toggle más simple para usar en barras de navegación
 */
@Composable
fun SimpleDarkModeToggle(
    isDarkMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isDarkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode,
            contentDescription = if (isDarkMode) "Cambiar a modo claro" else "Cambiar a modo oscuro",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Card con información sobre el tema actual
 */
@Composable
fun ThemeInfoCard(
    isDarkMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isDarkMode) "Modo Oscuro" else "Modo Claro",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isDarkMode) 
                        "Tema oscuro activado" 
                    else 
                        "Tema claro activado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            DarkModeToggle(
                isDarkMode = isDarkMode,
                onToggle = onToggle
            )
        }
    }
}
