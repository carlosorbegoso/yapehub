package org.sysarp.project.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.PaymentNotificationData
import org.sysarp.project.data.PaymentResultData
import org.sysarp.project.utils.extractShortYapeCode

/**
 * Tarjeta de notificación de nuevo pago
 */
@Composable
fun PaymentNotificationCard(
    notification: PaymentNotificationData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header con icono y botón de cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Payment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nuevo Pago Recibido",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Información del pago
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoRow(
                        label = "Monto",
                        value = "S/ ${String.format("%.2f", notification.amount)}",
                        icon = Icons.Filled.AttachMoney,
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                    
                    InfoRow(
                        label = "De",
                        value = notification.senderName,
                        icon = Icons.Filled.Person
                    )
                    
                    InfoRow(
                        label = "Código",
                        value = extractShortYapeCode(notification.yapeCode),
                        icon = Icons.Filled.QrCode,
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                    
                    InfoRow(
                        label = "ID",
                        value = "#${notification.paymentId}",
                        icon = Icons.Filled.Tag
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Mensaje
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * Tarjeta de notificación de resultado de pago
 */
@Composable
fun PaymentResultNotificationCard(
    result: PaymentResultData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (result.status) {
                    "CONFIRMED" -> MaterialTheme.colorScheme.primaryContainer
                    "REJECTED_BY_SELLER" -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header con icono y botón de cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (result.status) {
                                "CONFIRMED" -> Icons.Filled.CheckCircle
                                "REJECTED_BY_SELLER" -> Icons.Filled.Cancel
                                else -> Icons.Filled.Info
                            },
                            contentDescription = null,
                            tint = when (result.status) {
                                "CONFIRMED" -> MaterialTheme.colorScheme.primary
                                "REJECTED_BY_SELLER" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (result.status) {
                                "CONFIRMED" -> "Pago Confirmado"
                                "REJECTED_BY_SELLER" -> "Pago Rechazado"
                                else -> "Estado Actualizado"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (result.status) {
                                "CONFIRMED" -> MaterialTheme.colorScheme.onPrimaryContainer
                                "REJECTED_BY_SELLER" -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            tint = when (result.status) {
                                "CONFIRMED" -> MaterialTheme.colorScheme.onPrimaryContainer
                                "REJECTED_BY_SELLER" -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Información del resultado
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoRow(
                        label = "Pago ID",
                        value = "#${result.paymentId}",
                        icon = Icons.Filled.Tag
                    )
                    
                    InfoRow(
                        label = "Vendedor",
                        value = result.sellerName,
                        icon = Icons.Filled.Person
                    )
                    
                    InfoRow(
                        label = "Estado",
                        value = when (result.status) {
                            "CONFIRMED" -> "Confirmado"
                            "REJECTED_BY_SELLER" -> "Rechazado"
                            else -> result.status
                        },
                        icon = when (result.status) {
                            "CONFIRMED" -> Icons.Filled.CheckCircle
                            "REJECTED_BY_SELLER" -> Icons.Filled.Cancel
                            else -> Icons.Filled.Info
                        },
                        valueColor = when (result.status) {
                            "CONFIRMED" -> MaterialTheme.colorScheme.primary
                            "REJECTED_BY_SELLER" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Mensaje
                Text(
                    text = result.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (result.status) {
                        "CONFIRMED" -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        "REJECTED_BY_SELLER" -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    }
                )
            }
        }
    }
}

/**
 * Componente para mostrar información en filas
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor,
            modifier = Modifier.weight(1f)
        )
    }
}
