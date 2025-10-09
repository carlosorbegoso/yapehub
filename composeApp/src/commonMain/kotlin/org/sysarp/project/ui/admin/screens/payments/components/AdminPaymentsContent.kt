package org.sysarp.project.ui.admin.screens.payments.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.ui.admin.screens.payments.AdminPaymentsState

/**
 * Componente principal de gestión de pagos administrativos
 * Refactorizado para ser más modular y mantenible
 */
@Composable
fun AdminPaymentsContent(
    state: AdminPaymentsState,
    onLoadMore: () -> Unit = {},
    onStatusFilterChange: (String?) -> Unit = {},
    onPaymentAction: (Int, String) -> Unit = { _: Int, _: String -> } // paymentId, action
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Resumen de pagos
        if (state.paymentSummary != null) {
            PaymentSummaryCard(summary = state.paymentSummary!!)
        }
        
        // Filtros de estado
        StatusFilterCard(
            selectedStatus = state.selectedStatus,
            onStatusChange = onStatusFilterChange
        )
        
        // Lista de pagos o estados
        when {
            state.isLoading -> AdminPaymentsLoadingCard()
            state.errorMessage.isNotEmpty() -> AdminPaymentsErrorCard(errorMessage = state.errorMessage)
            state.payments.isEmpty() -> AdminPaymentsEmptyState(
                message = "No hay pagos disponibles"
            )
            else -> {
                // Lista de pagos
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.payments) { payment ->
                        AdminPaymentCard(
                            payment = payment,
                            onAction = { action -> onPaymentAction(payment.paymentId, action) }
                        )
                    }
                    
                    // Botón de cargar más
                    if (state.hasMorePayments) {
                        item {
                            AdminPaymentsLoadMoreButton(
                                isLoadingMore = state.isLoadingMore,
                                onLoadMore = onLoadMore
                            )
                        }
                    }
                }
            }
        }
    }
}
