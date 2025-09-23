package org.sysarp.project.ui.admin.screens.management

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import org.sysarp.project.data.BranchInfo
import org.sysarp.project.service.branch.BranchService
import org.sysarp.project.ui.common.components.layout.GlobalAppLayout
import org.sysarp.project.ui.common.components.navigation.GlobalNavItem

@Composable
fun BranchManagementScreen(
    branchService: BranchService,
    adminId: Int,
    accessToken: String,
    onBackClick: () -> Unit,
    onNavigate: (GlobalNavItem) -> Unit,
    onNavigateToBranchDetails: (BranchInfo) -> Unit = {},
    onNavigateToSellers: (BranchInfo) -> Unit = {},
    onNavigateToEditBranch: (BranchInfo) -> Unit = {},
    onNavigateToAddBranch: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Estados para diálogos y acciones
    var showDeleteDialog by remember { mutableStateOf<BranchInfo?>(null) }
    var showAddBranchDialog by remember { mutableStateOf(false) }
    
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
        showTopBar = false, // Sin TopBar, solo navegación inferior
        onNavigate = onNavigate,
        onBackClick = onBackClick
    ) {
        // Usar el nuevo componente modular
        BranchManagementComponents(
            state = componentsState,
            onEditBranch = { branch -> 
                onNavigateToEditBranch(branch)
            },
            onViewSellers = { branch -> 
                onNavigateToSellers(branch)
            },
            onToggleBranchStatus = { branch -> 
                state.toggleBranchStatus(branch)
            },
            onDeleteBranch = { branch -> 
                showDeleteDialog = branch
            },
            onViewBranchDetails = { branch -> 
                onNavigateToBranchDetails(branch)
            },
            onPageChange = { page -> 
                componentsState.changeCurrentPage(page)
                state.loadBranches()
            },
            onFilterChange = { status -> 
                componentsState.changeFilterStatus(status)
            },
            onToggleFilters = { 
                componentsState.toggleFilters()
            },
            onAddBranch = {
                onNavigateToAddBranch()
            }
        )
        
        // Diálogo de confirmación para eliminar sucursal
        showDeleteDialog?.let { branch ->
            BranchDeleteConfirmationDialog(
                branch = branch,
                onConfirm = {
                    state.deleteBranch(branch)
                    showDeleteDialog = null
                },
                onDismiss = {
                    showDeleteDialog = null
                }
            )
        }
    }
}