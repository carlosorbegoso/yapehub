package org.sysarp.project.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sqrt

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Animaciones múltiples
    val infiniteTransition = rememberInfiniteTransition(label = "splash_animations")
    
    // Escala pulsante del logo (más suave)
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Opacidad de los iconos secundarios (más elegante)
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    // Animación de pulso para los iconos secundarios (más elegante)
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Animación de entrada del texto
    val textScale = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    
    // Efecto de carga que se ejecuta una vez
    LaunchedEffect(Unit) {
        // Animar entrada del texto
        textScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = EaseOutBack)
        )
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(600, easing = EaseOutCubic)
        )
        
        // Esperar 3 segundos total
        delay(3000)
        
        // Navegar al login
        onSplashFinished()
    }
    
    // Fondo con gradiente corporativo animado
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0D1B2A), // Azul oscuro profundo
                        Color(0xFF1E88E5), // Azul corporativo
                        Color(0xFF9C27B0), // Púrpura
                        Color(0xFF673AB7)  // Púrpura oscuro
                    ),
                    radius = 1000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Constelaciones de estrellas animadas
        ConstellationsBackground()
        
        // Contenido principal
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo principal con animaciones
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                // Icono central con animación de pulso
                Icon(
                    imageVector = Icons.Filled.Circle,
                    contentDescription = "YapeHub Logo",
                    tint = Color.White,
                    modifier = Modifier
                        .size(80.dp)
                        .scale(scale * 0.8f)
                )
                
                // Letra Y central
                Text(
                    text = "Y",
                    color = Color(0xFF1E88E5),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .scale(scale * 0.8f)
                )
                
                // Iconos secundarios con animaciones elegantes
                // Top - Money con círculo
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .offset(y = (-70).dp)
                        .alpha(alpha)
                        .scale(pulse)
                        .background(
                            Color(0xFF4CAF50),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AttachMoney,
                        contentDescription = "Money",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Right - Store con círculo
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .offset(x = 70.dp)
                        .alpha(alpha)
                        .scale(pulse)
                        .background(
                            Color(0xFFFF9800),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Store,
                        contentDescription = "Store",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Bottom - Payment con círculo
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .offset(y = 70.dp)
                        .alpha(alpha)
                        .scale(pulse)
                        .background(
                            Color(0xFFF44336),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Payment,
                        contentDescription = "Payment",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Left - People con círculo
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .offset(x = (-70).dp)
                        .alpha(alpha)
                        .scale(pulse)
                        .background(
                            Color(0xFF9C27B0),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.People,
                        contentDescription = "People",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Líneas de conexión animadas
                Canvas(
                    modifier = Modifier.size(200.dp)
                ) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val radius = 70f
                    
                    // Líneas de conexión con opacidad animada
                    drawLine(
                        color = Color.White.copy(alpha = 0.3f * alpha),
                        start = Offset(centerX, centerY),
                        end = Offset(centerX, centerY - radius),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.3f * alpha),
                        start = Offset(centerX, centerY),
                        end = Offset(centerX + radius, centerY),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.3f * alpha),
                        start = Offset(centerX, centerY),
                        end = Offset(centerX, centerY + radius),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.3f * alpha),
                        start = Offset(centerX, centerY),
                        end = Offset(centerX - radius, centerY),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
            
            // Texto de la app con animación
            Text(
                text = "YapeHub",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 32.dp)
                    .scale(textScale.value)
                    .alpha(textAlpha.value)
            )
            
            // Texto descriptivo con animación
            Text(
                text = "Conectando vendedores y pagos",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .scale(textScale.value)
                    .alpha(textAlpha.value)
            )
            
            // Indicador de carga
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 24.dp)
                    .alpha(textAlpha.value)
            )
        }
    }
}

@Composable
fun ConstellationsBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "constellations")
    
    // Animación de parpadeo para las estrellas
    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )
    
    // Posiciones de las estrellas (constelaciones)
    val starPositions = listOf(
        Pair(0.1f, 0.2f), Pair(0.2f, 0.1f), Pair(0.3f, 0.3f),
        Pair(0.4f, 0.1f), Pair(0.5f, 0.4f), Pair(0.6f, 0.2f),
        Pair(0.7f, 0.1f), Pair(0.8f, 0.3f), Pair(0.9f, 0.2f),
        Pair(0.15f, 0.4f), Pair(0.25f, 0.6f), Pair(0.35f, 0.5f),
        Pair(0.45f, 0.7f), Pair(0.55f, 0.6f), Pair(0.65f, 0.8f),
        Pair(0.75f, 0.5f), Pair(0.85f, 0.7f), Pair(0.95f, 0.6f),
        Pair(0.1f, 0.8f), Pair(0.2f, 0.9f), Pair(0.3f, 0.85f),
        Pair(0.4f, 0.9f), Pair(0.5f, 0.8f), Pair(0.6f, 0.9f),
        Pair(0.7f, 0.85f), Pair(0.8f, 0.9f), Pair(0.9f, 0.8f)
    )
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Dibujar estrellas
        starPositions.forEach { (x, y) ->
            val randomDelay = (x * 1000).toInt()
            val starTwinkle by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500 + randomDelay, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "star_$x"
            )
            
            Box(
                modifier = Modifier
                    .offset(
                        x = (x * 400).dp - 2.dp,
                        y = (y * 800).dp - 2.dp
                    )
                    .size(4.dp)
                    .alpha(starTwinkle)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Star",
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Líneas de conexión entre estrellas (constelaciones)
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            // Dibujar líneas de conexión
            for (i in 0 until starPositions.size - 1) {
                val (x1, y1) = starPositions[i]
                val (x2, y2) = starPositions[i + 1]
                
                // Solo conectar estrellas cercanas
                val distance = sqrt(
                    ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)).toDouble()
                )
                
                if (distance < 0.3) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.3f * twinkle),
                        start = Offset(
                            x1 * canvasWidth,
                            y1 * canvasHeight
                        ),
                        end = Offset(
                            x2 * canvasWidth,
                            y2 * canvasHeight
                        ),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }
    }
}