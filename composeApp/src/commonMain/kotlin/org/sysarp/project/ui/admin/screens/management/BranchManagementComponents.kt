package org.sysarp.project.ui.admin.screens.management

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onToggleFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header principal
        BranchManagementHeader()
        
        // Sección de estadísticas
        BranchStatsSection(state = state)
        
        // Sección de filtros
        BranchFiltersSection(
            state = state,
            onFilterChange = onFilterChange
        )
        
        // Sección de lista de sucursales
        BranchListSection(
            state = state,
            onEdit = onEditBranch,
            onViewSellers = onViewSellers,
            onToggleStatus = onToggleBranchStatus,
            onDelete = onDeleteBranch,
            onViewDetails = onViewBranchDetails
        )
        
        // Sección de paginación
        BranchPaginationSection(
            state = state,
            onPageChange = onPageChange
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
