package org.sysarp.project.ui.components.cards

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar con animación
                AnimatedAvatar(
                    connectionState = connectionState,
                    modifier = Modifier.size(80.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Nombre/Código del vendedor
                AnimatedText(
                    text = sellerName ?: "Vendedor #${sellerId ?: "N/A"}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Sucursal
                if (!branchName.isNullOrBlank()) {
                    AnimatedText(
                        text = branchName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Estado de conexión con animación
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
            WebSocketConnectionState.CONNECTED -> MaterialTheme.colorScheme.primary
            WebSocketConnectionState.CONNECTING -> MaterialTheme.colorScheme.secondary
            WebSocketConnectionState.RECONNECTING -> MaterialTheme.colorScheme.tertiary
            WebSocketConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.error
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
            WebSocketConnectionState.CONNECTED -> Color(0xFF4CAF50) // Verde
            WebSocketConnectionState.CONNECTING -> Color(0xFFFF9800) // Naranja
            WebSocketConnectionState.RECONNECTING -> Color(0xFF2196F3) // Azul
            WebSocketConnectionState.DISCONNECTED -> Color(0xFFF44336) // Rojo
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
