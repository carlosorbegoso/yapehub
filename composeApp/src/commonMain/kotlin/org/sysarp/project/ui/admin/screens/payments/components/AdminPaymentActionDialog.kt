package org.sysarp.project.ui.admin.screens.payments.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.sysarp.project.data.AdminPayment
import org.sysarp.project.utils.extractShortYapeCode
import org.sysarp.project.utils.formatCurrency
import org.sysarp.project.utils.formatDateTime

/**
 * Diálogo específico para acciones de administradores
 * Maneja acciones de supervisión como ver detalles, contactar seller, agregar notas
 */
@Composable
fun AdminPaymentActionDialog(
    payment: AdminPayment?,
    action: String?,
    isVisible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false
) {
    if (isVisible && payment != null && action != null) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header con icono y título
                    AdminActionHeader(action = action)

                    // Contenido específico según la acción
                    when (action) {
                        "ADD_NOTE" -> {
                            AddNoteContent(payment = payment)
                        }
                        "VIEW_REASON" -> {
                            ViewReasonContent(payment = payment)
                        }
                        else -> {
                            DefaultActionContent(payment = payment, action = action)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Botones de acción
                    AdminActionButtons(
                        action = action,
                        isLoading = isLoading,
                        onConfirm = onConfirm,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminActionHeader(action: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (action) {
                "VIEW_DETAILS" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                "CONTACT_SELLER" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                "ADD_NOTE" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                "VIEW_REASON" -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(50)
    ) {
        Icon(
            imageVector = when (action) {
                "ADD_NOTE" -> Icons.Filled.Note
                "VIEW_REASON" -> Icons.Filled.Info
                else -> Icons.Filled.Help
            },
            contentDescription = null,
            tint = when (action) {
                "ADD_NOTE" -> MaterialTheme.colorScheme.tertiary
                "VIEW_REASON" -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier
                .size(48.dp)
                .padding(12.dp)
        )
    }

    Text(
        text = when (action) {
            "ADD_NOTE" -> "Agregar Nota Administrativa"
            "VIEW_REASON" -> "Razón de Rechazo"
            else -> "Acción de Administrador"
        },
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}


@Composable
private fun AddNoteContent(payment: AdminPayment) {
    var noteText by remember { mutableStateOf("") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Nota Administrativa",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            PaymentInfoRow(
                label = "Pago",
                value = "#${payment.paymentId} - ${formatCurrency(payment.amount)}",
                icon = Icons.Filled.Tag
            )
            PaymentInfoRow(
                label = "Cliente",
                value = payment.senderName,
                icon = Icons.Filled.Person
            )
            PaymentInfoRow(
                label = "Estado",
                value = when (payment.status) {
                    "PENDING" -> "Pendiente"
                    "CONFIRMED" -> "Confirmado"
                    "REJECTED_BY_SELLER" -> "Rechazado por Vendedor"
                    else -> payment.status
                },
                icon = Icons.Filled.Info
            )
            
            // Mostrar vendedor solo si el pago ya fue procesado
            if (payment.status != "PENDING") {
                PaymentInfoRow(
                    label = "Vendedor",
                    value = payment.sellerName,
                    icon = Icons.Filled.Store
                )
            }
            
            HorizontalDivider()
            
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Escribe tu nota aquí...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            
            Text(
                text = "Esta nota será visible solo para administradores y se asociará a este pago.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ViewReasonContent(payment: AdminPayment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Detalles del Rechazo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            
            PaymentInfoRow(
                label = "Pago",
                value = "#${payment.paymentId} - ${formatCurrency(payment.amount)}",
                icon = Icons.Filled.Tag
            )
            PaymentInfoRow(
                label = "Cliente",
                value = payment.senderName,
                icon = Icons.Filled.Person
            )
            PaymentInfoRow(
                label = "Estado",
                value = when (payment.status) {
                    "PENDING" -> "Pendiente"
                    "CONFIRMED" -> "Confirmado"
                    "REJECTED_BY_SELLER" -> "Rechazado por Vendedor"
                    else -> payment.status
                },
                icon = Icons.Filled.Info
            )
            
            // Mostrar vendedor solo si el pago ya fue procesado
            if (payment.status != "PENDING") {
                PaymentInfoRow(
                    label = "Vendedor",
                    value = payment.sellerName,
                    icon = Icons.Filled.Store
                )
            }
            
            HorizontalDivider()
            
            payment.rejectedBy?.let { rejectedBy ->
                PaymentInfoRow(
                    label = "Rechazado por",
                    value = "Admin #$rejectedBy",
                    icon = Icons.Filled.Cancel
                )
            }
            
            payment.rejectedAt?.let { rejectedAt ->
                PaymentInfoRow(
                    label = "Fecha de Rechazo",
                    value = formatDateTime(rejectedAt),
                    icon = Icons.Filled.Schedule
                )
            }
            
            payment.rejectionReason?.let { reason ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Razón del Rechazo:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } ?: run {
                Text(
                    text = "No se proporcionó razón específica para el rechazo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DefaultActionContent(payment: AdminPayment, action: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Acción: $action",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            PaymentInfoRow(
                label = "Pago",
                value = "#${payment.paymentId}",
                icon = Icons.Filled.Tag
            )
            PaymentInfoRow(
                label = "Monto",
                value = formatCurrency(payment.amount),
                icon = Icons.Filled.AttachMoney
            )
            PaymentInfoRow(
                label = "Cliente",
                value = payment.senderName,
                icon = Icons.Filled.Person
            )
            PaymentInfoRow(
                label = "Estado",
                value = when (payment.status) {
                    "PENDING" -> "Pendiente"
                    "CONFIRMED" -> "Confirmado"
                    "REJECTED_BY_SELLER" -> "Rechazado por Vendedor"
                    else -> payment.status
                },
                icon = Icons.Filled.Info
            )
            
            // Mostrar vendedor solo si el pago ya fue procesado
            if (payment.status != "PENDING") {
                PaymentInfoRow(
                    label = "Vendedor",
                    value = payment.sellerName,
                    icon = Icons.Filled.Store
                )
            }
            
            Text(
                text = "Esta acción será procesada por el sistema.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AdminActionButtons(
    action: String,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f),
            enabled = !isLoading
        ) {
            Text("Cerrar")
        }

        Button(
            onClick = onConfirm,
            modifier = Modifier.weight(1f),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = when (action) {
                    "ADD_NOTE" -> MaterialTheme.colorScheme.tertiary
                    "VIEW_REASON" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                }
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = when (action) {
                    "ADD_NOTE" -> "Guardar Nota"
                    "VIEW_REASON" -> "Entendido"
                    else -> "Procesar"
                }
            )
        }
    }
}

@Composable
private fun PaymentInfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}
