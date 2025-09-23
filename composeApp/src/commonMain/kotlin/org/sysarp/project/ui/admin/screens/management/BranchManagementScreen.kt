package org.sysarp.project.ui.admin.screens.management

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import org.sysarp.project.service.branch.BranchService
import org.sysarp.project.ui.common.components.layout.GlobalAppLayout
import org.sysarp.project.ui.common.components.navigation.GlobalNavItem

/**
 * Pantalla de gestión de sucursales
 * Refactorizada para usar componentes modulares
 */
@Composable
fun BranchManagementScreen(
    branchService: BranchService,
    adminId: Int,
    accessToken: String,
    onBackClick: () -> Unit,
    onNavigate: (GlobalNavItem) -> Unit
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
    
    GlobalAppLayout(
        currentScreen = GlobalNavItem.Management,
        title = "Gestión de Sucursales",
        subtitle = "Administra tus sucursales y equipos",
        onNavigate = onNavigate,
        onBackClick = onBackClick
    ) {
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
                state.toggleBranchStatus(branch)
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