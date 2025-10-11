package org.sysarp.project.ui.seller.screens.payments

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.ui.common.components.rememberDateFilterState
import org.sysarp.project.ui.seller.screens.payments.components.SellerPaymentsComponents

/**
 * Pantalla de pagos del vendedor refactorizada
 * Usa componentes modulares para mejor mantenibilidad
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerPaymentsScreen(
    authService: AuthService,
    paymentService: PaymentService,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()
    
    // Crear el estado del screen
    val state = remember {
        SellerPaymentsState(
            paymentService = paymentService,
            coroutineScope = coroutineScope
        )
    }
    
    // Estado del filtro de fechas
    val dateFilterState = rememberDateFilterState()
    
    // Usar el componente refactorizado
    SellerPaymentsComponents(
        pendingPayments = state.pendingPayments,
        confirmedPayments = state.confirmedPayments,
        isLoadingPending = state.isLoading,
        isLoadingConfirmed = state.isLoading,
        errorMessagePending = state.errorMessage,
        errorMessageConfirmed = state.errorMessage,
        selectedTab = state.selectedTab,
        onTabSelected = { tabIndex ->
            state.changeSelectedTab(tabIndex)
        },
        onRefresh = {
            state.refreshAllPayments(
                onSuccess = { },
                onFailure = { }
            )
        },
        onNavigateBack = onNavigateBack,
        userProfile = userProfile,
        accessToken = accessToken,
        paymentService = paymentService,
        onError = { error ->
            state.updateErrorMessage(error)
        }
    )
}
