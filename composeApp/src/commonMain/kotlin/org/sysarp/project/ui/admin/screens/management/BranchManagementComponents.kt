package org.sysarp.project.ui.admin.screens.management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header usando TopBarComponent
            BranchManagementHeader(onNavigateBack = onNavigateBack)
            
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
            
            // Espacio adicional para el botón flotante
            Spacer(modifier = Modifier.height(80.dp))
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
