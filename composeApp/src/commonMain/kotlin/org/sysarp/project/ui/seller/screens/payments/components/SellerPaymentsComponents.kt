package org.sysarp.project.ui.seller.screens.payments.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.sysarp.project.ui.seller.screens.payments.SellerPaymentsState


@Composable
fun SellerPaymentsComponents(
    state: SellerPaymentsState,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onRefresh: () -> Unit,
    onNavigateBack: () -> Unit,
    yapeCodeFilter: String,
    onYapeCodeFilterChanged: (String) -> Unit,
    onCalendarClick: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        SellerPaymentsTopBar(
            onNavigateBack = onNavigateBack,
            onRefresh = onRefresh,
            onCalendarClick = onCalendarClick
        )

        // Tabs con filtros
        SellerPaymentsTabs(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            yapeCodeFilter = yapeCodeFilter,
            onYapeCodeFilterChanged = onYapeCodeFilterChanged
        )

        // Contenido según tab seleccionado
        when (selectedTab) {
            0 -> renderPendingPaymentsTab(state, onRefresh)
            1 -> renderConfirmedPaymentsTab(state)
        }
    }
}

/**
 * Renderiza el tab de pagos pendientes
 */
@Composable
private fun renderPendingPaymentsTab(
    state: SellerPaymentsState,
    onRefresh: () -> Unit
) {
    SellerPaymentsContent(
        payments = state.filteredPendingPayments,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onRefresh = onRefresh,
        isPendingTab = true,
        paymentSummary = state.paymentSummary,
        isLoadingMore = state.isLoadingMore,
        hasMorePayments = state.hasMorePayments,
        onLoadMore = {
            state.loadMorePayments()
        },
        onClaimPayment = { paymentId ->
            state.claimPayment(paymentId)
        },
        onRejectPayment = { paymentId ->
            state.rejectPayment(paymentId)
        }
    )
}

/**
 * Renderiza el tab de pagos confirmados
 */
@Composable
private fun renderConfirmedPaymentsTab(state: SellerPaymentsState) {
    SellerPaymentsContent(
        payments = state.filteredConfirmedPayments,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onRefresh = {},
        isPendingTab = false,
        paymentSummary = state.paymentSummary,
        isLoadingMore = state.isLoadingMore,
        hasMorePayments = state.hasMorePayments,
        onLoadMore = {
            state.loadMorePayments()
        }
    )
}