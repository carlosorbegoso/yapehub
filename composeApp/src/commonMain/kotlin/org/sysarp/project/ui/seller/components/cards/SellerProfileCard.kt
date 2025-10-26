package org.sysarp.project.ui.components.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.service.websocket.WebSocketConnectionState

/**
 * Tarjeta de perfil del vendedor con animaciones y estado de conexión
 */
@Composable
fun SellerProfileCard(
    sellerId: Int?,
    sellerName: String?,
    branchName: String?,
    branchCode: String? = null,
    affiliationCode: String? = null,
    connectionState: WebSocketConnectionState,
    modifier: Modifier = Modifier
) {
    // Animación de entrada
    var animatedVisibility by remember {
        mutableStateOf(false)
    }
    
    LaunchedEffect(Unit) {
        animatedVisibility = true
    }
    
    AnimatedVisibility(
        visible = animatedVisibility,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(
            animationSpec = tween(800)
        ),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar con animación - más pequeño
                AnimatedAvatar(
                    connectionState = connectionState,
                    modifier = Modifier.size(56.dp)
                )
                
                // Información del vendedor en columna compacta
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp) // Más espacio para mejor legibilidad
                ) {
                    // Nombre del vendedor
                    AnimatedText(
                        text = sellerName ?: "Vendedor",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Código del vendedor
                    AnimatedText(
                        text = "Código: #${sellerId ?: "N/A"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Información de sucursal
                    if (!branchName.isNullOrBlank()) {
                        Column {
                            AnimatedText(
                                text = branchName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            // Código de sucursal si está disponible
                            if (!branchCode.isNullOrBlank()) {
                                AnimatedText(
                                    text = "Código: $branchCode",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            
                            // Código de afiliación si está disponible
                            if (!affiliationCode.isNullOrBlank()) {
                                AnimatedText(
                                    text = "Afiliación: $affiliationCode",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
                
                // Estado de conexión con animación - al lado derecho
                ConnectionStatusIndicator(
                    connectionState = connectionState
                )
            }
        }
    }
}

/**
 * Avatar animado que cambia según el estado de conexión
 */
@Composable
private fun AnimatedAvatar(
    connectionState: WebSocketConnectionState,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = when (connectionState) {
            WebSocketConnectionState.CONNECTED -> Color(0xFF4CAF50) // Verde profesional para "en línea"
            WebSocketConnectionState.CONNECTING -> Color(0xFF2196F3) // Azul para conectando
            WebSocketConnectionState.RECONNECTING -> Color(0xFF2196F3) // Azul para reconectando
            WebSocketConnectionState.DISCONNECTED -> Color(0xFFE53935) // Rojo profesional para desconectado
        },
        animationSpec = tween(1000),
        label = "avatarColor"
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = when (connectionState) {
            WebSocketConnectionState.CONNECTED -> 1.0f
            WebSocketConnectionState.CONNECTING -> 0.9f
            WebSocketConnectionState.RECONNECTING -> 0.95f
            WebSocketConnectionState.DISCONNECTED -> 0.8f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "avatarScale"
    )
    
    Box(
        modifier = modifier
            .scale(animatedScale)
            .clip(CircleShape)
            .background(animatedColor.copy(alpha = 0.1f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = "Avatar del vendedor",
            tint = animatedColor,
            modifier = Modifier.size(48.dp)
        )
    }
}

/**
 * Texto animado
 */
@Composable
private fun AnimatedText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    fontWeight: FontWeight? = null,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200),
        label = "textAlpha"
    )
    
    Text(
        text = text,
        style = style,
        fontWeight = fontWeight,
        color = color.copy(alpha = animatedAlpha),
        modifier = modifier
    )
}

/**
 * Indicador de estado de conexión con animación
 */
@Composable
private fun ConnectionStatusIndicator(
    connectionState: WebSocketConnectionState,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = when (connectionState) {
            WebSocketConnectionState.CONNECTED -> Color(0xFF4CAF50) // Verde profesional para "en línea"
            WebSocketConnectionState.CONNECTING -> Color(0xFF2196F3) // Azul para conectando
            WebSocketConnectionState.RECONNECTING -> Color(0xFF2196F3) // Azul para reconectando
            WebSocketConnectionState.DISCONNECTED -> Color(0xFFE53935) // Rojo profesional para desconectado
        },
        animationSpec = tween(500),
        label = "statusColor"
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "statusScale"
    )
    
    Row(
        modifier = modifier.scale(animatedScale),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Punto de estado animado
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(animatedColor)
        )
        
        Text(
            text = when (connectionState) {
                WebSocketConnectionState.CONNECTED -> "En línea"
                WebSocketConnectionState.CONNECTING -> "Conectando..."
                WebSocketConnectionState.RECONNECTING -> "Reconectando..."
                WebSocketConnectionState.DISCONNECTED -> "Desconectado"
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = animatedColor
        )
    }
}
