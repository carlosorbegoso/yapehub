package org.sysarp.project.ui.admin.screens.payments.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.AdminPayment
import org.sysarp.project.utils.calculatePreciseTimeElapsed
import org.sysarp.project.utils.extractShortYapeCode
import org.sysarp.project.utils.formatCurrency
import org.sysarp.project.utils.formatDateTime

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

    // Calcular tiempo transcurrido preciso con memoización
    val timeElapsed by remember(payment.createdAt) {
        derivedStateOf {
            calculatePreciseTimeElapsed(payment.createdAt)
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
                
                // Estado mejorado con indicadores visuales
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Badge de estado
                    StatusBadge(status = payment.status, statusColor = statusColor)
                    
                    // Indicador de tiempo transcurrido
                    TimeIndicator(timeElapsed = timeElapsed)
                }
            }
            
            // Información del pago mejorada
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Monto destacado mejorado
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
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
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        // Icono de moneda
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AttachMoney,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(8.dp)
                            )
                        }
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
                
                // Información de procesamiento (si aplica)
                ProcessingInfoSection(payment = payment)
            }
            
            // Acciones contextuales mejoradas
            ContextualActionsSection(
                payment = payment,
                onAction = onAction
            )
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
 * Sección de información de procesamiento
 */
@Composable
private fun ProcessingInfoSection(payment: AdminPayment) {
    when (payment.status) {
        "CONFIRMED" -> {
            if (payment.confirmedBy != null && payment.confirmedAt != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EnhancedInfoRow(
                        label = "Confirmado por",
                        value = "Admin #${payment.confirmedBy}",
                        icon = Icons.Filled.CheckCircle,
                        iconColor = MaterialTheme.colorScheme.primary
                    )
                    EnhancedInfoRow(
                        label = "Fecha de confirmación",
                        value = formatDateTime(payment.confirmedAt),
                        icon = Icons.Filled.Schedule,
                        iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        "REJECTED_BY_SELLER" -> {
            if (payment.rejectedBy != null && payment.rejectedAt != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EnhancedInfoRow(
                        label = "Rechazado por",
                        value = "Admin #${payment.rejectedBy}",
                        icon = Icons.Filled.Cancel,
                        iconColor = MaterialTheme.colorScheme.error
                    )
                    EnhancedInfoRow(
                        label = "Fecha de rechazo",
                        value = formatDateTime(payment.rejectedAt),
                        icon = Icons.Filled.Schedule,
                        iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (payment.rejectionReason != null) {
                        EnhancedInfoRow(
                            label = "Razón",
                            value = payment.rejectionReason,
                            icon = Icons.Filled.Info,
                            iconColor = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sección de acciones contextuales para ADMINISTRADORES
 * Los admins NO pueden confirmar/ pagos, solo supervisar
 */
@Composable
private fun ContextualActionsSection(
    payment: AdminPayment,
    onAction: (String) -> Unit
) {
    when (payment.status) {
        "PENDING" -> {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Información para admin
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Pago pendiente de confirmación por el vendedor",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Solo información, sin acciones redundantes
                    Text(
                        text = "Los administradores pueden supervisar pero no intervenir en pagos pendientes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        "CONFIRMED" -> {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Información de confirmación
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Pago confirmado por el vendedor",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Solo acción útil para administradores
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = { onAction("ADD_NOTE") },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Note,
                                contentDescription = "Agregar nota administrativa",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Agregar Nota", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
        "REJECTED_BY_SELLER" -> {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Información de rechazo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Pago rechazado por el vendedor",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Solo acción útil para administradores
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = { onAction("VIEW_REASON") },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "Ver razón de rechazo",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ver Razón", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Badge de estado mejorado
 */
@Composable
private fun StatusBadge(
    status: String,
    statusColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Punto indicador
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(statusColor, CircleShape)
            )
            
            Text(
                text = when (status) {
                    "PENDING" -> "Pendiente"
                    "CONFIRMED" -> "Confirmado"
                    "REJECTED_BY_SELLER" -> "Rechazado"
                    else -> status
                },
                color = statusColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

/**
 * Indicador de tiempo transcurrido
 */
@Composable
private fun TimeIndicator(timeElapsed: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = timeElapsed,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
