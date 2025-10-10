package org.sysarp.project.ui.admin.screens.payments.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.sysarp.project.data.AdminPayment
import org.sysarp.project.utils.extractShortYapeCode
import org.sysarp.project.utils.formatCurrency
import org.sysarp.project.utils.formatDateTime
import kotlin.time.ExperimentalTime

/**
 * Tarjeta individual de pago administrativo mejorada
 * Incluye información adicional, tiempo transcurrido y mejor UX
 */
@Composable
fun AdminPaymentCard(
    payment: AdminPayment,
    onAction: (String) -> Unit = {}
) {
    val statusColor = when (payment.status) {
        "PENDING" -> MaterialTheme.colorScheme.error
        "CONFIRMED" -> MaterialTheme.colorScheme.primary
        "REJECTED_BY_SELLER" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val statusIcon = when (payment.status) {
        "PENDING" -> Icons.Filled.Schedule
        "CONFIRMED" -> Icons.Filled.CheckCircle
        "REJECTED_BY_SELLER" -> Icons.Filled.Cancel
        else -> Icons.Filled.Payment
    }

    // Calcular tiempo transcurrido con memoización
    val timeElapsed by remember(payment.createdAt) {
        derivedStateOf {
            calculateTimeElapsed(payment.createdAt)
        }
    }

    // Animación de entrada
    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "card_alpha"
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con estado y tiempo transcurrido mejorado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Icono de estado con fondo
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = statusColor.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier
                                .size(28.dp)
                                .padding(8.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Pago #${payment.paymentId}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = timeElapsed,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Estado mejorado
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = statusColor.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = payment.status,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            
            // Información del pago mejorada
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Monto destacado simplificado
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Monto del Pago",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatCurrency(payment.amount),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Información en dos columnas con mejor diseño
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EnhancedInfoRow(
                            label = "Cliente",
                            value = payment.senderName,
                            icon = Icons.Filled.Person,
                            iconColor = MaterialTheme.colorScheme.primary
                        )
                        
                        EnhancedInfoRow(
                            label = "Código Yape",
                            value = extractShortYapeCode(payment.yapeCode),
                            icon = Icons.Filled.QrCode,
                            iconColor = MaterialTheme.colorScheme.secondary
                        )
                    }
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EnhancedInfoRow(
                            label = "Vendedor",
                            value = payment.sellerName,
                            icon = Icons.Filled.Store,
                            iconColor = MaterialTheme.colorScheme.tertiary
                        )
                        
                        EnhancedInfoRow(
                            label = "Sucursal",
                            value = payment.branchName ?: "Sin sucursal",
                            icon = Icons.Filled.Business,
                            iconColor = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                
                // Fecha con mejor diseño
                EnhancedInfoRow(
                    label = "Fecha y Hora",
                    value = formatDateTime(payment.createdAt),
                    icon = Icons.Filled.Schedule,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Botones de acción mejorados (si el pago está pendiente)
            if (payment.status == "PENDING") {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onAction("CONFIRM") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Confirmar pago ${payment.paymentId}",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Confirmar",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        OutlinedButton(
                            onClick = { onAction("REJECT") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Cancel,
                                contentDescription = "Rechazar pago ${payment.paymentId}",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Rechazar",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente mejorado para mostrar información en filas
 */
@Composable
private fun EnhancedInfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icono con fondo circular
        Card(
            colors = CardDefaults.cardColors(
                containerColor = iconColor.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .size(20.dp)
                    .padding(6.dp)
            )
        }
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Calcula el tiempo transcurrido desde la creación del pago
 */
private fun calculateTimeElapsed(createdAt: String): String {
    return try {
        // Parsear la fecha de creación
        val createdDate = createdAt.substring(0, 10) // Obtener solo la fecha
        val currentDate = java.time.LocalDate.now().toString()

        if (createdDate == currentDate) {
            "Hoy"
        } else {
            "Reciente"
        }
    } catch (e: Exception) {
        "Desconocido"
    }
}
