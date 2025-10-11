package org.sysarp.project.ui.common.components.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.PaymentFilterStatus
import org.sysarp.project.data.PaymentFilterStatusUtils
import org.sysarp.project.data.UserRole

/**
 * Componente para filtros de estados de pagos
 * Soporta selección múltiple para vendedores y selección única para administradores
 */
@Composable
fun PaymentStatusFilter(
    selectedStatuses: List<PaymentFilterStatus>,
    userRole: UserRole,
    onStatusSelectionChanged: (List<PaymentFilterStatus>) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Filtrar por Estado",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            when (userRole) {
                UserRole.VENDOR -> {
                    // Selección múltiple para vendedores
                    VendorStatusFilter(
                        selectedStatuses = selectedStatuses,
                        onStatusSelectionChanged = onStatusSelectionChanged
                    )
                }
                UserRole.ADMIN -> {
                    // Selección única para administradores
                    AdminStatusFilter(
                        selectedStatuses = selectedStatuses,
                        onStatusSelectionChanged = onStatusSelectionChanged
                    )
                }
            }
        }
    }
}

@Composable
private fun VendorStatusFilter(
    selectedStatuses: List<PaymentFilterStatus>,
    onStatusSelectionChanged: (List<PaymentFilterStatus>) -> Unit
) {
    Column(
        modifier = Modifier.selectableGroup()
    ) {
        PaymentFilterStatusUtils.getSellerAvailableStatuses().forEach { status ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedStatuses.contains(status),
                        onClick = {
                            val newSelection = if (selectedStatuses.contains(status)) {
                                selectedStatuses - status
                            } else {
                                selectedStatuses + status
                            }
                            onStatusSelectionChanged(newSelection)
                        },
                        role = Role.Checkbox
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedStatuses.contains(status),
                    onCheckedChange = null // Handled by selectable
                )
                Text(
                    text = getStatusDisplayName(status),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun AdminStatusFilter(
    selectedStatuses: List<PaymentFilterStatus>,
    onStatusSelectionChanged: (List<PaymentFilterStatus>) -> Unit
) {
    Column(
        modifier = Modifier.selectableGroup()
    ) {
        PaymentFilterStatusUtils.getAdminAvailableStatuses().forEach { status ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedStatuses.contains(status),
                        onClick = {
                            onStatusSelectionChanged(listOf(status))
                        },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedStatuses.contains(status),
                    onClick = null // Handled by selectable
                )
                Text(
                    text = getStatusDisplayName(status),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

private fun getStatusDisplayName(status: PaymentFilterStatus): String {
    return when (status) {
        PaymentFilterStatus.PENDING -> "Pendientes"
        PaymentFilterStatus.CLAIMED -> "Confirmados"
        PaymentFilterStatus.REJECTED -> "Rechazados"
        PaymentFilterStatus.ALL -> "Todos"
    }
}
