package org.sysarp.project.ui.admin.screens.management

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import org.sysarp.project.data.BranchInfo
import org.sysarp.project.service.branch.BranchService
import org.sysarp.project.ui.components.branch.CreateBranchDialog
import org.sysarp.project.ui.components.branch.EditBranchDialog

@Composable
fun BranchManagementScreen(
    branchService: BranchService,
    adminId: Int,
    accessToken: String,
    onBackClick: () -> Unit,
    onNavigateToBranchDetails: (BranchInfo) -> Unit = {},
    onNavigateToSellers: (BranchInfo) -> Unit = {},
    onNavigateToEditBranch: (BranchInfo) -> Unit = {},
    onNavigateToAddBranch: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Estados para diálogos y acciones
    var showDeleteDialog by remember { mutableStateOf<BranchInfo?>(null) }
    var showAddBranchDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<BranchInfo?>(null) }
    
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
    
    // Usar el nuevo componente modular directamente
    BranchManagementComponents(
        state = componentsState,
        onEditBranch = { branch -> 
            showEditDialog = branch
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
            showAddBranchDialog = true
        },
        onNavigateBack = onBackClick
    )
    
    // Diálogo de creación de sucursal
    if (showAddBranchDialog) {
        CreateBranchDialog(
            onDismiss = {
                showAddBranchDialog = false
            },
            onCreate = { name, code, address ->
                state.createBranch(name, code, address)
                showAddBranchDialog = false
            }
        )
    }
    
    // Diálogo de edición de sucursal
    showEditDialog?.let { branch ->
        EditBranchDialog(
            branch = branch,
            onDismiss = {
                showEditDialog = null
            },
            onUpdate = { name, code, address, isActive ->
                state.updateBranch(
                    branchId = branch.branchId,
                    name = name,
                    code = code,
                    address = address,
                    isActive = isActive
                )
                showEditDialog = null
            }
        )
    }
    
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