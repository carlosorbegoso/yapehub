package org.sysarp.project.ui.admin.screens.management.branch

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.sysarp.project.ui.admin.screens.management.BranchManagementComponentsState

/**
 * Componente principal de gestión de sucursales
 * Refactorizado para usar componentes modulares
 */
@Composable
fun BranchManagementComponents(
    state: BranchManagementComponentsState,
    onNavigateBack: (() -> Unit)? = null,
    onAddBranch: () -> Unit = {},
    onEditBranch: (String) -> Unit = {},
    onDeleteBranch: (String) -> Unit = {},
    onFilterChange: (String) -> Unit = {},
    onStatusFilterChange: (String?) -> Unit = {},
    onPageChange: (Int) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        BranchManagementHeader(onNavigateBack = onNavigateBack)
        
        // Estadísticas
        BranchStatsSection(state = state)
        
        // Filtros (si están implementados)
        // BranchFiltersSection(state = state, onFilterChange = onFilterChange, onStatusFilterChange = onStatusFilterChange)
        
        // Lista de sucursales
        // BranchListSection(state = state, onEditBranch = onEditBranch, onDeleteBranch = onDeleteBranch)
        
        // Paginación (si está implementada)
        // BranchPaginationSection(state = state, onPageChange = onPageChange)
        
        // Botón flotante para agregar sucursal
        // AddBranchFloatingButton(onClick = onAddBranch)
    }
}
