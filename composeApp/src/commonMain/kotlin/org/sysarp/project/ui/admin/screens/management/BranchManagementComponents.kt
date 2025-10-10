package org.sysarp.project.ui.admin.screens.management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.BranchInfo

/**
 * Componentes principales para BranchManagementScreen refactorizados
 * Usa componentes modulares para mejor mantenibilidad
 */

@Composable
fun BranchManagementComponents(
    state: BranchManagementComponentsState,
    onEditBranch: (BranchInfo) -> Unit,
    onViewSellers: (BranchInfo) -> Unit,
    onToggleBranchStatus: (BranchInfo) -> Unit,
    onDeleteBranch: (BranchInfo) -> Unit,
    onViewBranchDetails: (BranchInfo) -> Unit,
    onPageChange: (Int) -> Unit,
    onFilterChange: (String?) -> Unit,
    onToggleFilters: () -> Unit,
    onAddBranch: () -> Unit,
    onNavigateBack: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Usar LazyColumn como contenedor principal para evitar conflictos
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp) // Espacio para el FAB
        ) {
            // Header
            item {
                BranchManagementHeader(onNavigateBack = onNavigateBack)
            }
            
            // Sección de estadísticas
            item {
                BranchStatsSection(state = state)
            }
            
            // Sección de filtros
            item {
                BranchFiltersSection(
                    state = state,
                    onFilterChange = onFilterChange
                )
            }
            
            // Lista de sucursales (sin LazyColumn anidado)
            if (!state.hasBranches() && !state.isCurrentlyLoading()) {
                item {
                    EmptyBranchesCard()
                }
            } else {
                items(
                    items = state.getFilteredBranches(),
                    key = { branch -> branch.branchId }
                ) { branch ->
                    BranchCardV2(
                        branch = branch,
                        onEdit = { onEditBranch(branch) },
                        onViewSellers = { onViewSellers(branch) },
                        onToggleStatus = { onToggleBranchStatus(branch) },
                        onDelete = { onDeleteBranch(branch) },
                        onViewDetails = { onViewBranchDetails(branch) }
                    )
                }
            }
            
            // Sección de paginación
            item {
                BranchPaginationSection(
                    state = state,
                    onPageChange = onPageChange
                )
            }
        }
        
        // Botón flotante para agregar sucursal
        AddBranchFloatingButton(
            onClick = onAddBranch,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
    
    // Manejar acciones de la pantalla
    BranchManagementComponentsActions(
        state = state,
        onEditBranch = onEditBranch,
        onViewSellers = onViewSellers,
        onToggleBranchStatus = onToggleBranchStatus,
        onDeleteBranch = onDeleteBranch,
        onViewBranchDetails = onViewBranchDetails,
        onPageChange = onPageChange,
        onFilterChange = onFilterChange,
        onToggleFilters = onToggleFilters
    )
}
