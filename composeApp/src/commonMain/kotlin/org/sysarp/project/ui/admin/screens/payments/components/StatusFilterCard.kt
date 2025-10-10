package org.sysarp.project.ui.admin.screens.payments.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Componente de filtros de estado para pagos administrativos
 * Refactorizado para ser más modular
 */
@Composable
fun StatusFilterCard(
    selectedStatus: String?,
    onStatusChange: (String?) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Filtrar por estado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    onClick = { onStatusChange(null) },
                    label = { Text("Todos") },
                    selected = selectedStatus == null
                )
                
                FilterChip(
                    onClick = { onStatusChange("PENDING") },
                    label = { Text("Pendientes") },
                    selected = selectedStatus == "PENDING"
                )
                
                FilterChip(
                    onClick = { onStatusChange("CONFIRMED") },
                    label = { Text("Confirmados") },
                    selected = selectedStatus == "CONFIRMED"
                )
                
                FilterChip(
                    onClick = { onStatusChange("REJECTED") },
                    label = { Text("Rechazados") },
                    selected = selectedStatus == "REJECTED"
                )
            }
        }
    }
}