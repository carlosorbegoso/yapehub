package org.sysarp.project.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.BranchData
import org.sysarp.project.data.BranchInfo
import org.sysarp.project.service.branch.BranchService
import org.sysarp.project.ui.components.branch.BranchSellersDialog
import org.sysarp.project.ui.components.branch.CreateBranchDialog
import org.sysarp.project.ui.components.branch.EditBranchDialog

/**
 * Diálogos para BranchManagementScreen
 */

@Composable
fun BranchManagementDialogs(
    state: BranchManagementState,
    branchService: BranchService,
    adminId: Int,
    accessToken: String
) {
    // Diálogo de creación
    if (state.showCreateDialog) {
        CreateBranchDialog(
            onDismiss = { state.dismissCreateDialog() },
            onCreate = { name, code, address ->
                state.createBranch(name, code, address)
            }
        )
    }
    
    // Diálogo de edición
    state.selectedBranch?.let { branch ->
        if (state.showEditDialog) {
            EditBranchDialog(
                branch = branch,
                onDismiss = { state.dismissEditDialog() },
                onUpdate = { name, code, address, isActive ->
                    state.updateBranch(
                        branchId = branch.branchId,
                        name = name,
                        code = code,
                        address = address,
                        isActive = isActive
                    )
                }
            )
        }
        
        // Diálogo de vendedores
        if (state.showSellersDialog) {
            BranchSellersDialog(
                branch = branch,
                branchService = branchService,
                adminId = adminId,
                accessToken = accessToken,
                onDismiss = { state.dismissSellersDialog() }
            )
        }
    }
    
    // Diálogo de confirmación de eliminación
    state.selectedBranch?.let { branch ->
        if (state.showDeleteDialog) {
            BranchDeleteConfirmationDialog(
                branch = branch,
                onConfirm = {
                    state.deleteBranch(branch.branchId)
                },
                onDismiss = { state.dismissDeleteDialog() }
            )
        }
    }
    
    // Diálogo de detalles de sucursal
    state.branchDetails?.let { details ->
        if (state.showDetailsDialog) {
            BranchDetailsDialog(
                details = details,
                onDismiss = { state.dismissDetailsDialog() }
            )
        }
    }
}

@Composable
fun BranchDeleteConfirmationDialog(
    branch: BranchInfo,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Eliminar Sucursal")
        },
        text = {
            Text("¿Estás seguro de que deseas eliminar la sucursal \"${branch.name}\"? Esta acción no se puede deshacer.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun BranchDetailsDialog(
    details: BranchData,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Detalles de la Sucursal")
        },
        text = {
            Column {
                DetailRow("Nombre", details.name)
                DetailRow("Código", details.code)
                DetailRow("Dirección", details.address)
                DetailRow("Estado", if (details.isActive) "Activa" else "Inactiva")
                DetailRow("Vendedores", details.sellersCount.toString())
                DetailRow("Creada", details.createdAt)
                DetailRow("Actualizada", details.updatedAt)
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = MaterialTheme.typography.bodyMedium.fontWeight
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
