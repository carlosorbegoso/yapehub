package org.sysarp.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.service.websocket.WebSocketConnectionState

/**
 * Indicador de estado de conexión WebSocket
 */
@Composable
fun WebSocketStatusIndicator(
    connectionState: WebSocketConnectionState,
    sellerId: Int?,
    onReconnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (connectionState) {
                WebSocketConnectionState.CONNECTED -> MaterialTheme.colorScheme.primaryContainer
                WebSocketConnectionState.CONNECTING -> MaterialTheme.colorScheme.secondaryContainer
                WebSocketConnectionState.RECONNECTING -> MaterialTheme.colorScheme.tertiaryContainer
                WebSocketConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.errorContainer
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicador de estado
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = when (connectionState) {
                                WebSocketConnectionState.CONNECTED -> Color.Green
                                WebSocketConnectionState.CONNECTING -> Color.Yellow
                                WebSocketConnectionState.RECONNECTING -> Color(0xFFFF9800)
                                WebSocketConnectionState.DISCONNECTED -> Color.Red
                            },
                            shape = CircleShape
                        )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column {
                    Text(
                        text = getConnectionStatusText(connectionState),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = when (connectionState) {
                            WebSocketConnectionState.CONNECTED -> MaterialTheme.colorScheme.onPrimaryContainer
                            WebSocketConnectionState.CONNECTING -> MaterialTheme.colorScheme.onSecondaryContainer
                            WebSocketConnectionState.RECONNECTING -> MaterialTheme.colorScheme.onTertiaryContainer
                            WebSocketConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    
                    if (sellerId != null) {
                        Text(
                            text = "Vendedor: $sellerId",
                            style = MaterialTheme.typography.bodySmall,
                            color = when (connectionState) {
                                WebSocketConnectionState.CONNECTED -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                WebSocketConnectionState.CONNECTING -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                WebSocketConnectionState.RECONNECTING -> MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                WebSocketConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            }
                        )
                    }
                }
            }
            
            // Botón de reconexión si está desconectado
            if (connectionState == WebSocketConnectionState.DISCONNECTED) {
                IconButton(
                    onClick = onReconnectClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Reconectar",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Indicador compacto de estado de conexión
 */
@Composable
fun WebSocketStatusCompact(
    connectionState: WebSocketConnectionState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = when (connectionState) {
                        WebSocketConnectionState.CONNECTED -> Color.Green
                        WebSocketConnectionState.CONNECTING -> Color.Yellow
                        WebSocketConnectionState.RECONNECTING -> Color(0xFFFF9800)
                        WebSocketConnectionState.DISCONNECTED -> Color.Red
                    },
                    shape = CircleShape
                )
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = getConnectionStatusText(connectionState),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Obtiene el texto del estado de conexión
 */
private fun getConnectionStatusText(state: WebSocketConnectionState): String {
    return when (state) {
        WebSocketConnectionState.CONNECTED -> "Conectado"
        WebSocketConnectionState.CONNECTING -> "Conectando..."
        WebSocketConnectionState.RECONNECTING -> "Reconectando..."
        WebSocketConnectionState.DISCONNECTED -> "Desconectado"
    }
}
