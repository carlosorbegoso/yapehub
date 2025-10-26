package org.sysarp.project.ui.admin.screens.management

import androidx.compose.runtime.Composable
import org.sysarp.project.data.BranchInfo

/**
 * Acciones y handlers para BranchManagementComponents
 */

@Composable
fun BranchManagementComponentsActions(
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
    // Manejar acciones de filtros
    val handleFilterChange = { status: String? ->
        state.applyFilter(status)
        onFilterChange(status)
    }
    
    // Manejar cambio de página
    val handlePageChange = { page: Int ->
        state.goToPage(page)
        onPageChange(page)
    }
    
    // Manejar alternar filtros
    val handleToggleFilters = {
        state.toggleFilters()
        onToggleFilters()
    }
    
    // Manejar edición de sucursal
    val handleEditBranch = { branch: BranchInfo ->
        state.updateSelectedBranch(branch)
        onEditBranch(branch)
    }
    
    // Manejar visualización de vendedores
    val handleViewSellers = { branch: BranchInfo ->
        state.updateSelectedBranch(branch)
        onViewSellers(branch)
    }
    
    // Manejar cambio de estado de sucursal
    val handleToggleBranchStatus = { branch: BranchInfo ->
        state.updateSelectedBranch(branch)
        onToggleBranchStatus(branch)
    }
    
    // Manejar eliminación de sucursal
    val handleDeleteBranch = { branch: BranchInfo ->
        state.updateSelectedBranch(branch)
        onDeleteBranch(branch)
    }
    
    // Manejar visualización de detalles
    val handleViewBranchDetails = { branch: BranchInfo ->
        state.updateSelectedBranch(branch)
        onViewBranchDetails(branch)
    }
    
    // Las acciones específicas se manejan en los componentes individuales
    // Este composable actúa como coordinador de acciones
}