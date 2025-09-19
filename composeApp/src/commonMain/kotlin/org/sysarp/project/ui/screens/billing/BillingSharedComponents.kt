package org.sysarp.project.ui.screens.billing

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun SuccessAnimationOverlay(
    message: String,
    onAnimationComplete: () -> Unit
) {
    // Animaciones para el overlay de éxito
    var showOverlay by remember { mutableStateOf(true) }
    val scale by animateFloatAsState(
        targetValue = if (showOverlay) 1f else 0f,
        animationSpec = tween(300, easing = EaseOutBack),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (showOverlay) 1f else 0f,
        animationSpec = tween(300),
        label = "alpha"
    )
    
    // Animación del icono de éxito
    val infiniteTransition = rememberInfiniteTransition(label = "success")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )
    
    val iconRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "iconRotation"
    )
    
    // Auto-hide después de 3 segundos
    LaunchedEffect(Unit) {
        delay(3000)
        showOverlay = false
        delay(300) // Esperar a que termine la animación de salida
        onAnimationComplete()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = alpha * 0.8f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .scale(scale)
                .graphicsLayer { this.alpha = alpha },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icono de éxito animado
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(40.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .scale(iconScale)
                            .graphicsLayer { rotationZ = iconRotation },
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Mensaje de éxito
                Text(
                    text = message,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                // Efectos de confeti (partículas simples)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(5) { index ->
                        val particleScale by animateFloatAsState(
                            targetValue = if (showOverlay) 1f else 0f,
                            animationSpec = tween(
                                durationMillis = 500,
                                delayMillis = index * 100,
                                easing = EaseOutBounce
                            ),
                            label = "particle$index"
                        )
                        
                        val particleRotation by animateFloatAsState(
                            targetValue = if (showOverlay) 360f else 0f,
                            animationSpec = tween(
                                durationMillis = 1000,
                                delayMillis = index * 100,
                                easing = EaseOutCubic
                            ),
                            label = "particleRotation$index"
                        )
                        
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .scale(particleScale)
                                .graphicsLayer { rotationZ = particleRotation },
                            tint = when (index % 3) {
                                0 -> MaterialTheme.colorScheme.primary
                                1 -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.tertiary
                            }
                        )
                    }
                }
            }
        }
    }
}
