package org.sysarp.project.ui.admin.screens.payments.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.sysarp.project.data.AdminPayment
import org.sysarp.project.utils.formatCurrency
import org.sysarp.project.utils.formatDateTime

/**
 * Diálogo de confirmación para acciones de pago
 * Proporciona feedback visual y confirmación para acciones críticas
 */
@Composable
fun PaymentActionDialog(
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
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icono de acción
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (action) {
                                "CONFIRM" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                "REJECT" -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            imageVector = when (action) {
                                "CONFIRM" -> Icons.Filled.CheckCircle
                                "REJECT" -> Icons.Filled.Cancel
                                else -> Icons.Filled.Help
                            },
                            contentDescription = when (action) {
                                "CONFIRM" -> "Icono de confirmación"
                                "REJECT" -> "Icono de rechazo"
                                else -> "Icono de ayuda"
                            },
                            tint = when (action) {
                                "CONFIRM" -> MaterialTheme.colorScheme.primary
                                "REJECT" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .padding(12.dp)
                        )
                    }

                    // Título
                    Text(
                        text = when (action) {
                            "CONFIRM" -> "Confirmar Pago"
                            "REJECT" -> "Rechazar Pago"
                            else -> "Acción de Pago"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    // Información del pago
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PaymentInfoRow(
                                label = "ID",
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
                                label = "Vendedor",
                                value = payment.sellerName,
                                icon = Icons.Filled.Store
                            )
                            PaymentInfoRow(
                                label = "Fecha",
                                value = formatDateTime(payment.createdAt),
                                icon = Icons.Filled.Schedule
                            )
                        }
                    }

                    // Mensaje de confirmación
                    Text(
                        text = when (action) {
                            "CONFIRM" -> "¿Estás seguro de que deseas confirmar este pago? Esta acción no se puede deshacer."
                            "REJECT" -> "¿Estás seguro de que deseas rechazar este pago? Esta acción no se puede deshacer."
                            else -> "¿Estás seguro de realizar esta acción?"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (action) {
                                    "CONFIRM" -> MaterialTheme.colorScheme.primary
                                    "REJECT" -> MaterialTheme.colorScheme.error
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
                                    "CONFIRM" -> "Confirmar"
                                    "REJECT" -> "Rechazar"
                                    else -> "Confirmar"
                                }
                            )
                        }
                    }
                }
            }
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
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
