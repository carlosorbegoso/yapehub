package org.sysarp.project.ui.admin.screens.payments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.AdminPayment
import org.sysarp.project.data.PaymentSummary
import org.sysarp.project.utils.extractShortYapeCode
import org.sysarp.project.utils.formatCurrency
import org.sysarp.project.utils.formatDateTime

/**
 * Componentes UI para AdminPaymentsScreen
 */

@Composable
fun AdminPaymentsContent(
    state: AdminPaymentsState,
    onLoadMore: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Resumen de estadísticas
        state.paymentSummary?.let { summary ->
            item {
                PaymentSummaryCard(summary = summary)
            }
        }

        // Filtros por estado
        item {
            StatusFilterCard(
                selectedStatus = state.selectedStatus,
                onStatusSelected = { status ->
                    state.filterByStatus(
                        status = status,
                        onSuccess = { },
                        onFailure = { }
                    )
                }
            )
        }

        if (state.isLoading) {
            item {
                AdminPaymentsLoadingCard()
            }
        } else if (state.errorMessage.isNotEmpty()) {
            item {
                AdminPaymentsErrorCard(
                    errorMessage = state.errorMessage,
                    onRetry = {
                        state.loadAdminPayments(
                            onSuccess = { },
                            onFailure = { }
                        )
                    }
                )
            }
        } else if (!state.hasPayments()) {
            item {
                AdminPaymentsEmptyState(
                    message = state.getEmptyStateMessage()
                )
            }
        } else {
            items(state.payments) { payment ->
                AdminPaymentCard(payment = payment)
            }
            
            // Botón "Cargar más" si hay más pagos disponibles
            if (state.canLoadMorePayments()) {
                item {
                    AdminPaymentsLoadMoreButton(
                        isLoadingMore = state.isLoadingMore,
                        onLoadMore = onLoadMore
                    )
                }
            }
        }
    }
}

@Composable
fun AdminPaymentsLoadingCard() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun AdminPaymentsErrorCard(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
fun AdminPaymentsEmptyState(
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay pagos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AdminPaymentsLoadMoreButton(
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit
) {
    Button(
        onClick = onLoadMore,
        enabled = !isLoadingMore,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isLoadingMore) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(if (isLoadingMore) "Cargando..." else "Cargar más pagos")
    }
}

@Composable
fun PaymentSummaryCard(summary: PaymentSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Resumen de Pagos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Estadísticas principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AdminStatItem(
                    value = "${summary.totalPayments}",
                    label = "Total",
                    icon = Icons.Filled.Payment,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                AdminStatItem(
                    value = "${summary.pendingCount}",
                    label = "Pendientes",
                    icon = Icons.Filled.Schedule,
                    color = MaterialTheme.colorScheme.error
                )
                
                AdminStatItem(
                    value = "${summary.confirmedCount}",
                    label = "Confirmados",
                    icon = Icons.Filled.CheckCircle,
                    color = MaterialTheme.colorScheme.primary
                )
                
                AdminStatItem(
                    value = "${summary.rejectedCount}",
                    label = "Rechazados",
                    icon = Icons.Filled.Cancel,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Montos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Monto Total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatCurrency(summary.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Column {
                    Text(
                        text = "Pendiente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatCurrency(summary.pendingAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun AdminStatItem(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun StatusFilterCard(
    selectedStatus: String?,
    onStatusSelected: (String?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Filtrar por Estado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    onClick = { onStatusSelected(null) },
                    label = { 
                        Text(
                            "Todos",
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                    },
                    selected = selectedStatus == null,
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.List,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                
                FilterChip(
                    onClick = { onStatusSelected("PENDING") },
                    label = { 
                        Text(
                            "Pendientes",
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                    },
                    selected = selectedStatus == "PENDING",
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Segunda fila - Confirmados y Rechazados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    onClick = { onStatusSelected("CONFIRMED") },
                    label = { 
                        Text(
                            "Confirmados",
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                    },
                    selected = selectedStatus == "CONFIRMED",
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                
                FilterChip(
                    onClick = { onStatusSelected("REJECTED_BY_SELLER") },
                    label = { 
                        Text(
                            "Rechazados",
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                    },
                    selected = selectedStatus == "REJECTED_BY_SELLER",
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun AdminPaymentCard(payment: AdminPayment) {
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header con estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pago #${payment.paymentId}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = statusColor.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = payment.status,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Información del pago
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(
                    label = "Monto",
                    value = formatCurrency(payment.amount),
                    icon = Icons.Filled.AttachMoney,
                    valueColor = MaterialTheme.colorScheme.primary
                )
                
                InfoRow(
                    label = "De",
                    value = payment.senderName,
                    icon = Icons.Filled.Person
                )
                
                InfoRow(
                    label = "Código Yape",
                    value = extractShortYapeCode(payment.yapeCode),
                    icon = Icons.Filled.QrCode,
                    valueColor = MaterialTheme.colorScheme.primary
                )
                
                InfoRow(
                    label = "Vendedor",
                    value = payment.sellerName,
                    icon = Icons.Filled.Store
                )
                
                InfoRow(
                    label = "Sucursal",
                    value = payment.branchName,
                    icon = Icons.Filled.Business
                )
                
                InfoRow(
                    label = "Fecha",
                    value = formatDateTime(payment.createdAt),
                    icon = Icons.Filled.Schedule
                )
                
                // Información adicional según el estado
                when (payment.status) {
                    "CONFIRMED" -> {
                        payment.confirmedAt?.let { confirmedAt ->
                            InfoRow(
                                label = "Confirmado",
                                value = formatDateTime(confirmedAt),
                                icon = Icons.Filled.CheckCircle,
                                valueColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    "REJECTED_BY_SELLER" -> {
                        payment.rejectedAt?.let { rejectedAt ->
                            InfoRow(
                                label = "Rechazado",
                                value = formatDateTime(rejectedAt),
                                icon = Icons.Filled.Cancel,
                                valueColor = MaterialTheme.colorScheme.error
                            )
                        }
                        payment.rejectionReason?.let { reason ->
                            InfoRow(
                                label = "Motivo",
                                value = reason,
                                icon = Icons.Filled.Warning,
                                valueColor = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    icon: ImageVector,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
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
