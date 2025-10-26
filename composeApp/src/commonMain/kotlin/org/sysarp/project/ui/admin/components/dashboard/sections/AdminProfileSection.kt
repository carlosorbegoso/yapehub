package org.sysarp.project.ui.admin.components.dashboard.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.websocket.WebSocketConnectionState

/**
 * Sección de perfil del administrador
 * Replica el estilo de SellerProfileSection pero con información específica de admin
 */
@Composable
fun AdminProfileSection(
    userProfile: UserProfile?,
    connectionState: WebSocketConnectionState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC) // Gris azulado muy suave y premium
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del administrador
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF2196F3).copy(alpha = 0.3f), // Azul moderno
                                Color(0xFF64B5F6).copy(alpha = 0.1f)  // Azul claro
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = Color(0xFF1976D2), // Azul profundo
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información del administrador
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = userProfile?.businessName ?: "Mi Negocio",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E) // Azul muy profundo para texto
                )
                
                Text(
                    text = userProfile?.name ?: "Administrador",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF3F51B5).copy(alpha = 0.8f) // Azul índigo
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Estado de conexión
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (connectionState) {
                                    WebSocketConnectionState.CONNECTED -> Color(0xFF4CAF50)
                                    WebSocketConnectionState.CONNECTING -> Color(0xFFFF9800)
                                    else -> Color(0xFFF44336)
                                }
                            )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = when (connectionState) {
                            WebSocketConnectionState.CONNECTED -> "Sistema Conectado"
                            WebSocketConnectionState.CONNECTING -> "Conectando..."
                            else -> "Sin Conexión"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Icono de administrador
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}