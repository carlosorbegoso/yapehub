package org.sysarp.project.ui.components.cards

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Tarjeta de estadística reutilizable con animaciones
 */
@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    backgroundColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    modifier: Modifier = Modifier
) {
    // Animación de entrada escalonada
    var animatedVisibility by remember {
        mutableStateOf(false)
    }
    
    LaunchedEffect(Unit) {
        animatedVisibility = true
    }
    
    AnimatedVisibility(
        visible = animatedVisibility,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(
            animationSpec = tween(1000)
        ),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icono animado
                AnimatedIcon(
                    icon = icon,
                    iconColor = iconColor,
                    modifier = Modifier.size(32.dp)
                )
                
                // Valor animado
                AnimatedValue(
                    value = value,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Título
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Icono animado
 */
@Composable
private fun AnimatedIcon(
    icon: ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )
    
    val animatedRotation by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(1000),
        label = "iconRotation"
    )
    
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconColor,
        modifier = modifier
            .scale(animatedScale)
            .rotate(animatedRotation)
    )
}

/**
 * Valor animado con efecto de conteo
 */
@Composable
private fun AnimatedValue(
    value: String,
    modifier: Modifier = Modifier
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200),
        label = "valueAlpha"
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "valueScale"
    )
    
    Text(
        text = value,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = animatedAlpha),
        modifier = modifier.scale(animatedScale)
    )
}

/**
 * Tarjeta de estadística con animación de pulso para valores importantes
 */
@Composable
fun PulsingStatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    backgroundColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    isImportant: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Animación de pulso para valores importantes
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isImportant) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    var animatedVisibility by remember {
        mutableStateOf(false)
    }
    
    LaunchedEffect(Unit) {
        animatedVisibility = true
    }
    
    AnimatedVisibility(
        visible = animatedVisibility,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(
            animationSpec = tween(1000)
        ),
        modifier = modifier.scale(pulseScale)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isImportant) 8.dp else 4.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icono animado
                AnimatedIcon(
                    icon = icon,
                    iconColor = iconColor,
                    modifier = Modifier.size(32.dp)
                )
                
                // Valor animado
                AnimatedValue(
                    value = value,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Título
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
