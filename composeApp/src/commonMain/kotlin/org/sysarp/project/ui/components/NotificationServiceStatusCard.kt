package org.sysarp.project.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.ServiceStatus
/*
@Composable
fun NotificationServiceStatusCard(
    serviceStatus: ServiceStatus?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (serviceStatus?.isRunning == true) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Título y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Servicio de Notificaciones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Indicador de estado
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = when {
                                    serviceStatus?.isRunning == true && serviceStatus.isCapturing -> Color.Green
                                    serviceStatus?.isRunning == true -> Color.Yellow
                                    else -> Color.Red
                                },
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                    Text(
                        text = when {
                            serviceStatus?.isRunning == true && serviceStatus.isCapturing -> "Activo"
                            serviceStatus?.isRunning == true -> "Iniciando"
                            else -> "Inactivo"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Información detallada
            serviceStatus?.let { status ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    StatusRow(
                        label = "Capturando",
                        value = if (status.isCapturing) "Sí" else "No",
                        valueColor = if (status.isCapturing) Color.Green else Color.Red
                    )
                    
                    StatusRow(
                        label = "Device Fingerprint",
                        value = status.deviceFingerprint,
                        valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    StatusRow(
                        label = "Pendientes",
                        value = status.pendingNotificationsCount.toString(),
                        valueColor = if (status.pendingNotificationsCount > 0) Color.Orange else Color.Green
                    )
                    
                    StatusRow(
                        label = "Procesadas",
                        value = status.totalNotificationsProcessed.toString(),
                        valueColor = Color.Green
                    )
                    
                    StatusRow(
                        label = "Fallidas",
                        value = status.totalNotificationsFailed.toString(),
                        valueColor = if (status.totalNotificationsFailed > 0) Color.Red else Color.Green
                    )
                    
                    status.lastError?.let { error ->
                        StatusRow(
                            label = "Último Error",
                            value = error.take(50) + if (error.length > 50) "..." else "",
                            valueColor = Color.Red
                        )
                    }
                }
            } ?: run {
                Text(
                    text = "Estado no disponible",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}
*/