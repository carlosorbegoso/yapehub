package org.sysarp.project.ui.admin.screens.management

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.sysarp.project.service.auth.AuthService

/**
 * Acciones y handlers para SellerManagementScreen
 */

@Composable
fun SellerManagementActions(
    state: SellerManagementState,
    authService: AuthService,
    onAddSeller: (SellerData) -> Unit,
    onEditSeller: (Int, SellerData) -> Unit,
    onDeleteSeller: (Int) -> Unit,
    onRetry: () -> Unit
) {
    // Sincronizar con el servicio de autenticación
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()
    
    // Actualizar el estado con los datos de autenticación
    LaunchedEffect(userProfile, accessToken) {
        state.updateUserProfile(userProfile)
        state.updateAccessToken(accessToken)
        
        // Cargar vendedores cuando tengamos los datos necesarios
        if (state.canLoadSellers()) {
            state.loadSellers(
                onSuccess = { },
                onFailure = { }
            )
        }
    }
    
    // Manejar acciones de los diálogos
    if (state.isAddDialogVisible()) {
        AddSellerDialog(
            onDismiss = { state.closeAddSellerDialog() },
            onConfirm = { sellerData ->
                onAddSeller(sellerData)
            }
        )
    }
    
    if (state.isEditDialogVisible()) {
        state.getCurrentSelectedSeller()?.let { seller ->
            EditSellerDialog(
                seller = seller,
                onDismiss = { state.closeEditSellerDialog() },
                onConfirm = { sellerData ->
                    onEditSeller(seller.sellerId, sellerData)
                }
            )
        }
    }
}
