package org.sysarp.project.ui.admin.screens.management.branch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.ui.admin.screens.management.BranchManagementComponentsState

/**
 * Sección de estadísticas de sucursales
 * Refactorizado para ser más modular y mantenible
 */
@Composable
fun BranchStatsSection(
    state: BranchManagementComponentsState
) {
    if (state.isCurrentlyLoading()) {
        BranchLoadingCard()
    } else if (state.hasError()) {
        BranchErrorCard(
            title = "Error cargando sucursales",
            message = state.getCurrentErrorMessage() ?: "Error desconocido"
        )
    } else if (state.hasBranchStats()) {
        val branchStats = state.getCurrentBranchStats()!!
        
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Grid de estadísticas moderno
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total de sucursales
                ModernStatCard(
                    title = "Total",
                    value = branchStats.total.toString(),
                    icon = Icons.Filled.Business,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                
                // Sucursales activas
                ModernStatCard(
                    title = "Activas",
                    value = branchStats.active.toString(),
                    icon = Icons.Filled.CheckCircle,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Vendedores totales
                ModernStatCard(
                    title = "Vendedores",
                    value = branchStats.totalSellers.toString(),
                    icon = Icons.Filled.People,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                
                // Promedio por sucursal
                val avgSellers = if (branchStats.total > 0) 
                    (branchStats.totalSellers / branchStats.total) else 0
                ModernStatCard(
                    title = "Promedio",
                    value = avgSellers.toString(),
                    icon = Icons.Filled.People,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
