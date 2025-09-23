package org.sysarp.project.ui.seller.screens.payments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Acciones y handlers para SellerPaymentsScreen
 */

@Composable
fun SellerPaymentsActions(
    state: SellerPaymentsState,
    userProfile: org.sysarp.project.data.UserProfile?,
    accessToken: String?,
    onNavigateBack: () -> Unit
) {
    // Actualizar el estado con los valores del usuario
    LaunchedEffect(userProfile, accessToken) {
        state.updateUserProfile(userProfile)
        state.updateAccessToken(accessToken)
        
        if (state.canLoadPayments()) {
            state.loadPendingPayments(
                onSuccess = { },
                onFailure = { }
            )
            state.loadConfirmedPayments(
                onSuccess = { },
                onFailure = { }
            )
        }
    }
}

@Composable
fun SellerPaymentsContent(
    state: SellerPaymentsState,
    onNavigateBack: () -> Unit
) {
    when (state.selectedTab) {
        0 -> PendingPaymentsContent(
            payments = state.pendingPayments,
            isLoading = state.isLoading,
            errorMessage = state.errorMessage,
            onRefresh = {
                state.loadPendingPayments(
                    onSuccess = { },
                    onFailure = { }
                )
            },
            userProfile = state.userProfile,
            accessToken = state.accessToken,
            paymentService = state.paymentService,
            onError = { error ->
                state.updateErrorMessage(error)
            }
        )
        1 -> ConfirmedPaymentsContent(
            payments = state.confirmedPayments,
            isLoading = state.isLoading,
            errorMessage = state.errorMessage,
            onRefresh = {
                state.loadConfirmedPayments(
                    onSuccess = { },
                    onFailure = { }
                )
            }
        )
    }
}

@Composable
fun SellerPaymentsRefreshHandler(
    state: SellerPaymentsState
) {
    // Manejar la lógica de actualización
    // Las acciones específicas se manejan en los componentes individuales
}
