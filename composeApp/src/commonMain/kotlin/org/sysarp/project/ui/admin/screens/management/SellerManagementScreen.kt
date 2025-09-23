package org.sysarp.project.ui.admin.screens.management

import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import org.sysarp.project.service.SellerService
import org.sysarp.project.service.auth.AuthService

/**
 * Pantalla de gestión de vendedores refactorizada
 * Usa componentes modulares para mejor mantenibilidad
 */

@Composable
fun SellerManagementScreen(
    sellerService: SellerService,
    authService: AuthService,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Crear el estado de la pantalla
    val state = remember {
        SellerManagementState(
            sellerService = sellerService,
            authService = authService,
            coroutineScope = coroutineScope
        )
    }
    
    // Manejar acciones de la pantalla
    SellerManagementActions(
        state = state,
        authService = authService,
        onAddSeller = { sellerData ->
            state.addSeller(
                sellerData = sellerData,
                onSuccess = { },
                onFailure = { }
            )
        },
        onEditSeller = { sellerId, sellerData ->
            state.editSeller(
                sellerId = sellerId,
                sellerData = sellerData,
                onSuccess = { },
                onFailure = { }
            )
        },
        onDeleteSeller = { sellerId ->
            state.deleteSeller(
                sellerId = sellerId,
                onSuccess = { },
                onFailure = { }
            )
        },
        onRetry = {
            state.loadSellers(
                onSuccess = { },
                onFailure = { }
            )
        }
    )
    
    // Renderizar el contenido de la pantalla
    SellerManagementScreenContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onAddSeller = { state.openAddSellerDialog() },
        onEditSeller = { seller -> state.openEditSellerDialog(seller) },
        onDeleteSeller = { sellerId -> state.deleteSeller(sellerId, {}, {}) },
        onRetry = {
            state.loadSellers(
                onSuccess = { },
                onFailure = { }
            )
        }
    )
}
