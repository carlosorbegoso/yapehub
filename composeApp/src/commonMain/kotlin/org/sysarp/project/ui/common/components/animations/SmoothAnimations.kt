package org.sysarp.project.ui.common.components.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

/**
 * Animaciones suaves para componentes de la aplicación
 */

/**
 * Animación de fade in para contenido
 */
@Composable
fun FadeInContent(
    modifier: Modifier = Modifier,
    durationMillis: Int = 500,
    content: @Composable () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis),
        label = "fade_in"
    )
    
    Box(
        modifier = modifier.alpha(alpha)
    ) {
        content()
    }
}

/**
 * Animación de escala para botones y cards
 */
@Composable
fun ScaleInContent(
    modifier: Modifier = Modifier,
    durationMillis: Int = 300,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis),
        label = "scale_in"
    )
    
    Box(
        modifier = modifier.scale(scale)
    ) {
        content()
    }
}

/**
 * Animación de slide in desde abajo
 */
@Composable
fun SlideInFromBottom(
    modifier: Modifier = Modifier,
    durationMillis: Int = 400,
    content: @Composable () -> Unit
) {
    val offsetY by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(durationMillis),
        label = "slide_in"
    )
    
    Box(
        modifier = modifier.offset(y = offsetY.dp)
    ) {
        content()
    }
}

/**
 * Animación de pulse para elementos importantes
 */
@Composable
fun PulseAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    Box(
        modifier = modifier.scale(scale)
    ) {
        content()
    }
}

/**
 * Animación de shimmer para loading states
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    
    Box(
        modifier = modifier.alpha(alpha)
    ) {
        content()
    }
}

/**
 * Animación de bounce para elementos interactivos
 */
@Composable
fun BounceAnimation(
    modifier: Modifier = Modifier,
    isPressed: Boolean = false,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounce"
    )
    
    Box(
        modifier = modifier.scale(scale)
    ) {
        content()
    }
}

/**
 * Animación de rotación para iconos de carga
 */
@Composable
fun RotatingIcon(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = modifier.rotate(rotation)
    ) {
        content()
    }
}