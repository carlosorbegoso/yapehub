package org.sysarp.project.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun ComposeSplashScreen(
    onSplashFinished: () -> Unit
) {
    val scaleAnimation = remember { Animatable(0.3f) }
    val alphaAnimation = remember { Animatable(0f) }
    
    LaunchedEffect(key1 = true) {
        // Animación de escala
        scaleAnimation.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = EaseOutBack
            )
        )
        
        // Animación de opacidad
        alphaAnimation.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(
                durationMillis = 800,
                easing = EaseOutCubic
            )
        )
        
        // Esperar antes de continuar
        delay(2000)
        
        // Finalizar splash
        onSplashFinished()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E88E5), // Azul corporativo
                        Color(0xFF673AB7), // Púrpura corporativo
                        Color(0xFF9C27B0)  // Magenta corporativo
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo principal - Red de vendedores
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scaleAnimation.value)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF4CAF50),
                                Color(0xFF2196F3)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌐",
                    fontSize = 80.sp,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Texto principal
            Text(
                text = "YAPE",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.scale(alphaAnimation.value)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subtítulo
            Text(
                text = "Sistema de Pagos",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.scale(alphaAnimation.value)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Indicadores de funcionalidad
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.scale(alphaAnimation.value)
            ) {
                // Vendedores
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "👥", fontSize = 24.sp)
                    Text(
                        text = "Vendedores",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
                
                // Pagos
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "💰", fontSize = 24.sp)
                    Text(
                        text = "Pagos",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
                
                // Notificaciones
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "📱", fontSize = 24.sp)
                    Text(
                        text = "Notificaciones",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Indicador de carga
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = Color.White.copy(alpha = 0.7f),
                strokeWidth = 3.dp
            )
        }
    }
}