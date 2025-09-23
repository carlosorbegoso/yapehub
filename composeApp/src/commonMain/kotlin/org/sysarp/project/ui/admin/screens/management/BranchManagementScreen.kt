package org.sysarp.project.ui.admin.screens.management

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import org.sysarp.project.service.branch.BranchService

/**
 * Pantalla de gestión de sucursales
 * Refactorizada para usar componentes modulares
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchManagementScreen(
    branchService: BranchService,
    adminId: Int,
    accessToken: String,
    onBackClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Crear el estado principal de gestión de sucursales
    val state = remember {
        BranchManagementState(
            branchService = branchService,
            adminId = adminId,
            accessToken = accessToken,
            coroutineScope = coroutineScope
        )
    }
    
    // Crear el estado de los componentes
    val componentsState = remember {
        BranchManagementComponentsState()
    }
    
    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        state.loadBranches()
    }
    
    // Sincronizar datos entre estados
    LaunchedEffect(state.branchesData) {
        state.branchesData?.let { branchesData ->
            componentsState.updateBranches(branchesData.branches)
            componentsState.updateLoading(false)
        }
    }
    
    LaunchedEffect(state.isLoading) {
        componentsState.updateLoading(state.isLoading)
    }
    
    Scaffold(
        topBar = {
            // TODO: Implementar TopBar
        }
    ) { paddingValues ->
        // Usar el nuevo componente modular
        BranchManagementComponents(
            state = componentsState,
            onEditBranch = { branch -> 
                // TODO: Implementar edición
            },
            onViewSellers = { branch -> 
                // TODO: Implementar visualización de vendedores
            },
            onToggleBranchStatus = { branch -> 
                // TODO: Implementar toggle de estado
            },
            onDeleteBranch = { branch -> 
                // TODO: Implementar eliminación
            },
            onViewBranchDetails = { branch -> 
                // TODO: Implementar visualización de detalles
            },
            onPageChange = { page -> 
                // TODO: Implementar cambio de página
            },
            onFilterChange = { status -> 
                // TODO: Implementar cambio de filtro
            },
            onToggleFilters = { 
                // TODO: Implementar toggle de filtros
            }
        )
    }
}