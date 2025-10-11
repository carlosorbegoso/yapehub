package org.sysarp.project.ui.seller.screens.payments.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.data.UserProfile
import org.sysarp.project.data.PaymentFilterStatus
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.ui.seller.screens.payments.SellerPaymentsState

/**
 * Componente principal mejorado que orquesta toda la gestión de pagos de vendedor
 * Con filtros dinámicos y mejor UX
 */
@Composable
fun SellerPaymentsComponents(
    state: SellerPaymentsState,
    pendingPayments: List<SellerPendingPayment>,
    confirmedPayments: List<SellerPendingPayment>,
    isLoadingPending: Boolean,
    isLoadingConfirmed: Boolean,
    errorMessagePending: String,
    errorMessageConfirmed: String,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onRefresh: () -> Unit,
    onNavigateBack: () -> Unit,
    yapeCodeFilter: String,
    onYapeCodeFilterChanged: (String) -> Unit,
    onCalendarClick: (() -> Unit)? = null,
    userProfile: UserProfile?,
    accessToken: String?,
    paymentService: PaymentService,
    onError: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar mejorado
        SellerPaymentsTopBar(
            onNavigateBack = onNavigateBack,
            onRefresh = onRefresh,
            onCalendarClick = onCalendarClick
        )
        
        // Tabs mejorados con filtros
        SellerPaymentsTabs(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            yapeCodeFilter = yapeCodeFilter,
            onYapeCodeFilterChanged = onYapeCodeFilterChanged
        )
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> {
                SellerPaymentsContent(
                    payments = state.filteredPendingPayments,
                    isLoading = isLoadingPending,
                    errorMessage = errorMessagePending,
                    onRefresh = onRefresh,
                    userProfile = userProfile,
                    accessToken = accessToken,
                    paymentService = paymentService,
                    onError = onError,
                    isPendingTab = true
                )
            }
            1 -> {
                SellerPaymentsContent(
                    payments = state.filteredConfirmedPayments,
                    isLoading = isLoadingConfirmed,
                    errorMessage = errorMessageConfirmed,
                    onRefresh = onRefresh,
                    userProfile = userProfile,
                    accessToken = accessToken,
                    paymentService = paymentService,
                    onError = onError,
                    isPendingTab = false
                )
            }
        }
    }
}
