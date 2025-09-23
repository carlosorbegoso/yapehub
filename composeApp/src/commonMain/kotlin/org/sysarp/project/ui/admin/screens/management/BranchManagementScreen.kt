package org.sysarp.project.ui.screens.admin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import org.sysarp.project.data.BranchInfo
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
    
    // Crear el estado del management
    val state = remember {
        BranchManagementState(
            branchService = branchService,
            adminId = adminId,
            accessToken = accessToken,
            coroutineScope = coroutineScope
        )
    }
    
    Scaffold(
        topBar = {
            BranchManagementTopBar(
                state = state,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        BranchManagementContent(
            state = state,
            onEdit = { branch -> state.showEditDialog(branch) },
            onViewSellers = { branch -> state.showSellersDialog(branch) },
            onToggleStatus = { branch -> 
                // TODO: Implementar toggle de estado
            },
            onDelete = { branch -> state.showDeleteDialog(branch) },
            onViewDetails = { branch -> state.showDetailsDialog(branch) }
        )
    }
    
    // Manejar acciones y efectos secundarios
    BranchManagementActions(
        state = state,
        onBackClick = onBackClick
    )
    
    // Manejar diálogos
    BranchManagementDialogs(
        state = state,
        branchService = branchService,
        adminId = adminId,
        accessToken = accessToken
    )
}
