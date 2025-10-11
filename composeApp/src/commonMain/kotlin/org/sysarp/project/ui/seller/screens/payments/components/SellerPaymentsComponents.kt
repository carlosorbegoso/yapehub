package org.sysarp.project.ui.seller.screens.payments.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.payment.PaymentService

/**
 * Componente principal que orquesta toda la gestión de pagos de vendedor
 * Refactorizado para ser más modular y mantenible
 */
@Composable
fun SellerPaymentsComponents(
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
    userProfile: UserProfile?,
    accessToken: String?,
    paymentService: PaymentService,
    onError: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        SellerPaymentsTopBar(
            onNavigateBack = onNavigateBack,
            onRefresh = onRefresh
        )
        
        // Tabs
        SellerPaymentsTabs(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected
        )
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> {
                SellerPaymentsContent(
                    payments = pendingPayments,
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
                    payments = confirmedPayments,
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
