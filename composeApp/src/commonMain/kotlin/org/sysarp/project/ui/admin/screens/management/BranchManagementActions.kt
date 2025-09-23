package org.sysarp.project.ui.screens.admin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.ui.components.topbar.TopBarComponent

/**
 * Acciones y handlers para BranchManagementScreen
 */

@Composable
fun BranchManagementActions(
    state: BranchManagementState,
    onBackClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Cargar datos iniciales
    LaunchedEffect(state.currentPage, state.filterStatus) {
        state.loadBranches()
    }
}

@Composable
fun BranchManagementTopBar(
    state: BranchManagementState,
    onBackClick: () -> Unit
) {
    TopBarComponent(
        title = "Gestión de Sucursales",
        subtitle = "Administra tus sucursales y equipos",
        onNavigateBack = onBackClick,
        onRefresh = { state.refresh() },
        actions = {
            IconButton(onClick = { state.toggleFilters() }) {
                Icon(
                    imageVector = if (state.showFilters) Icons.Filled.FilterListOff else Icons.Filled.FilterList,
                    contentDescription = "Filtros"
                )
            }
            
            IconButton(onClick = { state.showCreateDialog() }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Crear sucursal"
                )
            }
        }
    )
}

@Composable
fun BranchManagementContent(
    state: BranchManagementState,
    onEdit: (org.sysarp.project.data.BranchInfo) -> Unit,
    onViewSellers: (org.sysarp.project.data.BranchInfo) -> Unit,
    onToggleStatus: (org.sysarp.project.data.BranchInfo) -> Unit,
    onDelete: (org.sysarp.project.data.BranchInfo) -> Unit,
    onViewDetails: (org.sysarp.project.data.BranchInfo) -> Unit
) {
    val branches = state.branchesData?.branches ?: emptyList()
    val branchStats = state.getBranchStats()
    
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
    ) {
        // Header con resumen
        item {
            BranchManagementHeader()
        }
        
        // Estadísticas principales
        item {
            BranchStatsSection(
                branchStats = branchStats,
                isLoading = state.isLoading,
                errorMessage = state.errorMessage
            )
        }
        
        // Filtros
        item {
            BranchFiltersSection(
                showFilters = state.showFilters,
                filterStatus = state.filterStatus,
                onFilterChange = { status -> state.changeFilterStatus(status) }
            )
        }
        
        // Lista de sucursales
        item {
            BranchListSection(
                branches = branches,
                isLoading = state.isLoading,
                onEdit = onEdit,
                onViewSellers = onViewSellers,
                onToggleStatus = onToggleStatus,
                onDelete = onDelete,
                onViewDetails = onViewDetails
            )
        }
        
        // Paginación
        item {
            BranchPaginationSection(
                currentPage = state.currentPage,
                totalPages = state.branchesData?.pagination?.totalPages ?: 1,
                onPageChange = { page -> state.changeCurrentPage(page) }
            )
        }
    }
}
