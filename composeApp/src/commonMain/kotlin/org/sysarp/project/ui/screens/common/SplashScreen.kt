package org.sysarp.project.ui.screens.common

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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

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
    
        // Fondo profesional con gradiente corporativo
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                    Brush.radialGradient(
                    colors = listOf(
                            Color(0xFF0A0E27), // Azul marino profundo
                            Color(0xFF1A237E), // Azul corporativo oscuro
                            Color(0xFF283593), // Azul medio
                            Color(0xFF3949AB)  // Azul claro
                        ),
                        radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
            // Fondo profesional con patrones geométricos
            ProfessionalBackground()
        
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
fun ProfessionalBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "professional_bg")
    
    // Animación de pulso para los elementos del fondo
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Animación de rotación lenta para elementos decorativos
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // Círculos decorativos profesionales
        val circles = listOf(
            // Círculo grande superior izquierda
            CircleData(
                center = Offset(canvasWidth * 0.2f, canvasHeight * 0.2f),
                radius = 80f * pulse,
                color = Color.White.copy(alpha = 0.05f)
            ),
            // Círculo mediano inferior derecha
            CircleData(
                center = Offset(canvasWidth * 0.8f, canvasHeight * 0.8f),
                radius = 60f * pulse,
                color = Color.White.copy(alpha = 0.08f)
            ),
            // Círculo pequeño centro derecha
            CircleData(
                center = Offset(canvasWidth * 0.85f, canvasHeight * 0.3f),
                radius = 40f * pulse,
                color = Color.White.copy(alpha = 0.06f)
            ),
            // Círculo pequeño inferior izquierda
            CircleData(
                center = Offset(canvasWidth * 0.15f, canvasHeight * 0.85f),
                radius = 35f * pulse,
                color = Color.White.copy(alpha = 0.07f)
            )
        )
        
        // Dibujar círculos decorativos
        circles.forEach { circle ->
            drawCircle(
                color = circle.color,
                radius = circle.radius,
                center = circle.center
            )
        }
        
        // Líneas geométricas profesionales
        val lineColor = Color.White.copy(alpha = 0.1f)
        
        // Líneas horizontales
        for (i in 1..4) {
            val y = canvasHeight * (i * 0.2f)
            drawLine(
                color = lineColor,
                start = Offset(0f, y),
                end = Offset(canvasWidth, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        // Líneas verticales
        for (i in 1..4) {
            val x = canvasWidth * (i * 0.2f)
            drawLine(
                color = lineColor,
                start = Offset(x, 0f),
                end = Offset(x, canvasHeight),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        // Patrón de puntos profesionales
        val dotColor = Color.White.copy(alpha = 0.15f)
        val dotSpacing = 60f
        
        var x = dotSpacing
        while (x <= canvasWidth) {
            var y = dotSpacing
            while (y <= canvasHeight) {
                drawCircle(
                    color = dotColor,
                    radius = 2f,
                    center = Offset(x, y)
                )
                y += dotSpacing
            }
            x += dotSpacing
        }
        
        // Elemento decorativo central rotatorio
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2
        
        // Dibujar hexágono decorativo
        val hexRadius = 100f * pulse
        val hexPoints = (0..5).map { i ->
            val angle = (i * 60f + rotation) * (Math.PI / 180f)
            Offset(
                centerX + hexRadius * cos(angle).toFloat(),
                centerY + hexRadius * sin(angle).toFloat()
            )
        }
        
        // Dibujar hexágono
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(hexPoints[0].x, hexPoints[0].y)
                hexPoints.drop(1).forEach { point ->
                    lineTo(point.x, point.y)
                }
                close()
            },
            color = Color.White.copy(alpha = 0.08f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.dp.toPx()
            )
        )
    }
}

// Clase de datos para los círculos decorativos
data class CircleData(
    val center: Offset,
    val radius: Float,
    val color: Color
)