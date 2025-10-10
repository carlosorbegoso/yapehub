package org.sysarp.project.ui.admin.screens.payments.components

import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.AdminPayment
import org.sysarp.project.ui.admin.screens.payments.AdminPaymentsState

/**
 * Componente principal de gestión de pagos administrativos mejorado
 * Incluye diálogo de confirmación y mejor UX
 */
@Composable
fun AdminPaymentsContent(
    state: AdminPaymentsState,
    onLoadMore: () -> Unit = {},
    onPaymentAction: (Int, String) -> Unit = { _: Int, _: String -> }, // paymentId, action
    onAdvancedFiltersChanged: (AdvancedFilters) -> Unit = {}
) {
    // Estado para el diálogo de confirmación
    var selectedPayment by remember { mutableStateOf<AdminPayment?>(null) }
    var selectedAction by remember { mutableStateOf<String?>(null) }
    var isDialogVisible by remember { mutableStateOf(false) }
    var isProcessingAction by remember { mutableStateOf(false) }

    // Debug: Mostrar estado actual
    LaunchedEffect(state.isLoading, state.errorMessage, state.payments.size, state.paymentSummary) {
        println("ADMIN_PAYMENTS_CONTENT: Estado actual - isLoading: ${state.isLoading}, error: '${state.errorMessage}', payments: ${state.payments.size}, summary: ${state.paymentSummary != null}")
    }
    
    // Lista desplazable con todos los componentes
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Resumen de pagos interactivo
        if (state.paymentSummary != null) {
            item {
                PaymentSummaryCard(
                    summary = state.paymentSummary!!
                )
            }
        }
        
        // Filtros avanzados (incluye filtro por estado)
        item {
            AdvancedFiltersComponent(
                payments = state.payments,
                onFiltersChanged = onAdvancedFiltersChanged
            )
        }
        
        // Estados de carga, error o vacío
        when {
            state.isLoading -> {
                item {
                    AdminPaymentsLoadingCard()
                }
            }
            state.errorMessage.isNotEmpty() -> {
                item {
                    AdminPaymentsErrorCard(errorMessage = state.errorMessage)
                }
            }
            state.getDisplayPayments().isEmpty() -> {
                item {
                    AdminPaymentsEmptyState(
                        message = state.getEmptyStateMessage()
                    )
                }
            }
            else -> {
                // Lista de pagos con keys estables para mejor performance
                items(
                    items = state.getDisplayPayments(),
                    key = { payment -> payment.paymentId }
                ) { payment ->
                    AdminPaymentCard(
                        payment = payment,
                        onAction = { action ->
                            selectedPayment = payment
                            selectedAction = action
                            isDialogVisible = true
                        }
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

    // Diálogo de acción para administradores
    AdminPaymentActionDialog(
        payment = selectedPayment,
        action = selectedAction,
        isVisible = isDialogVisible,
        isLoading = isProcessingAction,
        onConfirm = {
            isProcessingAction = true
            selectedPayment?.let { payment ->
                selectedAction?.let { action ->
                    onPaymentAction(payment.paymentId, action)
                    // Simular procesamiento (en una implementación real, esto sería una llamada a la API)
                    kotlinx.coroutines.GlobalScope.launch {
                        kotlinx.coroutines.delay(1000) // Simular delay de API
                        isProcessingAction = false
                        isDialogVisible = false
                        selectedPayment = null
                        selectedAction = null
                    }
                }
            }
        },
        onDismiss = {
            isDialogVisible = false
            selectedPayment = null
            selectedAction = null
            isProcessingAction = false
        }
    )
}
