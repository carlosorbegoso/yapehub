package org.sysarp.project.ui.seller.components.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.service.notification.HybridNotificationManager
import org.sysarp.project.service.websocket.PaymentWebSocketService
import org.sysarp.project.service.websocket.WebSocketConnectionState

@Composable
fun HybridNotificationStatus(
    hybridNotificationManager: HybridNotificationManager,
    webSocketService: PaymentWebSocketService,
    modifier: Modifier = Modifier
) {
    var connectionState by remember { mutableStateOf(WebSocketConnectionState.DISCONNECTED) }
    var hybridStatus by remember { mutableStateOf("Iniciando...") }
    
    LaunchedEffect(Unit) {
        // Observar estado del WebSocket
        webSocketService.connectionState.collect { state ->
            connectionState = state
        }
    }
    
    LaunchedEffect(Unit) {
        // Actualizar estado híbrido cada 5 segundos
        while (true) {
            hybridStatus = hybridNotificationManager.getStatus()
            kotlinx.coroutines.delay(5000)
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (connectionState) {
                WebSocketConnectionState.CONNECTED -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                WebSocketConnectionState.CONNECTING -> Color(0xFFFF9800).copy(alpha = 0.1f)
                WebSocketConnectionState.RECONNECTING -> Color(0xFFFF9800).copy(alpha = 0.1f)
                WebSocketConnectionState.DISCONNECTED -> Color(0xFFF44336).copy(alpha = 0.1f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Estado de Notificaciones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = when (connectionState) {
                        WebSocketConnectionState.CONNECTED -> "🟢 Conectado"
                        WebSocketConnectionState.CONNECTING -> "🟡 Conectando"
                        WebSocketConnectionState.RECONNECTING -> "🟡 Reconectando"
                        WebSocketConnectionState.DISCONNECTED -> "🔴 Desconectado"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (connectionState) {
                        WebSocketConnectionState.CONNECTED -> Color(0xFF4CAF50)
                        WebSocketConnectionState.CONNECTING -> Color(0xFFFF9800)
                        WebSocketConnectionState.RECONNECTING -> Color(0xFFFF9800)
                        WebSocketConnectionState.DISCONNECTED -> Color(0xFFF44336)
                    }
                )
            }
            
            Text(
                text = "Sistema Híbrido: WebSocket + Polling",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Text(
                text = hybridStatus,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Text(
                text = "• WebSocket: Tiempo real para confirmaciones\n• Polling: Fallback para notificaciones de Yape",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

