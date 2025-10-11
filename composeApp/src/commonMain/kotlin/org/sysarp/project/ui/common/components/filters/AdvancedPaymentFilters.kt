package org.sysarp.project.ui.common.components.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.PaymentFilterStatus
import org.sysarp.project.data.PaymentFilterStatusUtils
import org.sysarp.project.data.UserRole

/**
 * Componente de filtros avanzados para pagos
 * Combina filtros de fecha y estado
 */
@Composable
fun AdvancedPaymentFilters(
    selectedStatuses: List<PaymentFilterStatus>,
    startDate: String?,
    endDate: String?,
    userRole: UserRole,
    onStatusSelectionChanged: (List<PaymentFilterStatus>) -> Unit,
    onDateRangeChanged: (String?, String?) -> Unit,
    onApplyFilters: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Filtros Avanzados",
                style = MaterialTheme.typography.titleMedium
            )

            // Filtro de estados
            PaymentStatusFilter(
                selectedStatuses = selectedStatuses,
                userRole = userRole,
                onStatusSelectionChanged = onStatusSelectionChanged
            )

            // Filtro de fechas
            DateRangeFilter(
                startDate = startDate,
                endDate = endDate,
                onDateRangeChanged = onDateRangeChanged
            )

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onApplyFilters,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aplicar Filtros")
                }

                Button(
                    onClick = onClearFilters,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpiar")
                }
            }
        }
    }
}

/**
 * Hook para manejar el estado de los filtros
 */
@Composable
fun rememberPaymentFilters(
    initialStatuses: List<PaymentFilterStatus> = emptyList(),
    initialStartDate: String? = null,
    initialEndDate: String? = null
): PaymentFiltersState {
    var selectedStatuses by remember { mutableStateOf(initialStatuses) }
    var startDate by remember { mutableStateOf(initialStartDate) }
    var endDate by remember { mutableStateOf(initialEndDate) }

    return PaymentFiltersState(
        selectedStatuses = selectedStatuses,
        startDate = startDate,
        endDate = endDate,
        onStatusSelectionChanged = { selectedStatuses = it },
        onDateRangeChanged = { start, end ->
            startDate = start
            endDate = end
        },
        clearFilters = {
            selectedStatuses = emptyList()
            startDate = null
            endDate = null
        }
    )
}

/**
 * Estado de los filtros de pagos
 */
data class PaymentFiltersState(
    val selectedStatuses: List<PaymentFilterStatus>,
    val startDate: String?,
    val endDate: String?,
    val onStatusSelectionChanged: (List<PaymentFilterStatus>) -> Unit,
    val onDateRangeChanged: (String?, String?) -> Unit,
    val clearFilters: () -> Unit
) {
    /**
     * Obtiene el string de estados para la API
     */
    fun getStatusString(): String {
        return PaymentFilterStatusUtils.toCommaSeparatedString(selectedStatuses)
    }

    /**
     * Verifica si hay filtros activos
     */
    fun hasActiveFilters(): Boolean {
        return selectedStatuses.isNotEmpty() || startDate != null || endDate != null
    }

    /**
     * Obtiene una descripción de los filtros activos
     */
    fun getActiveFiltersDescription(): String {
        val parts = mutableListOf<String>()
        
        if (selectedStatuses.isNotEmpty()) {
            parts.add("Estados: ${selectedStatuses.joinToString(", ") { getStatusDisplayName(it) }}")
        }
        
        if (startDate != null) {
            parts.add("Desde: $startDate")
        }
        
        if (endDate != null) {
            parts.add("Hasta: $endDate")
        }
        
        return parts.joinToString(" | ")
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

/**
 * Componente simple para filtro de rango de fechas
 */
@Composable
private fun DateRangeFilter(
    startDate: String?,
    endDate: String?,
    onDateRangeChanged: (String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Rango de Fechas",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = startDate ?: "",
                onValueChange = { onDateRangeChanged(it.ifEmpty { null }, endDate) },
                label = { Text("Fecha Inicio") },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.weight(1f)
            )
            
            OutlinedTextField(
                value = endDate ?: "",
                onValueChange = { onDateRangeChanged(startDate, it.ifEmpty { null }) },
                label = { Text("Fecha Fin") },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
