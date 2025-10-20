package org.sysarp.project.ui.seller.screens.payments.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.PaymentSummary
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.ui.common.components.cards.ModernPaymentCard
import org.sysarp.project.ui.common.components.cards.ModernPaymentCardWithActions
import org.sysarp.project.ui.common.components.cards.PaymentStatsCard
import org.sysarp.project.ui.common.components.states.ModernLoadingState
import org.sysarp.project.ui.common.components.states.PaymentEmptyState
import org.sysarp.project.ui.common.components.states.PaymentErrorState

/**
 * Componente que renderiza la lista de pagos con estados de carga, error y vacío
 * Responsabilidades:
 * - Mostrar estado de carga
 * - Mostrar estado de error
 * - Mostrar estado vacío
 * - Renderizar lista de pagos
 * - Manejar scroll infinito
 */
@Composable
fun SellerPaymentsContent(
    payments: List<SellerPendingPayment>,
    isLoading: Boolean,
    errorMessage: String,
    onRefresh: () -> Unit,
    isPendingTab: Boolean = true,
    onClaimPayment: (Int) -> Unit = {},
    onRejectPayment: (Int) -> Unit = {},
    paymentSummary: PaymentSummary? = null,
    isLoadingMore: Boolean = false,
    hasMorePayments: Boolean = false,
    onLoadMore: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> renderLoadingState(isPendingTab)
            errorMessage.isNotEmpty() -> renderErrorState(errorMessage, onRefresh)
            payments.isEmpty() -> renderEmptyState(isPendingTab, onRefresh)
            else -> renderPaymentsList(
                payments = payments,
                isPendingTab = isPendingTab,
                paymentSummary = paymentSummary,
                isLoadingMore = isLoadingMore,
                hasMorePayments = hasMorePayments,
                onClaimPayment = onClaimPayment,
                onRejectPayment = onRejectPayment,
                onLoadMore = onLoadMore
            )
        }
    }
}

/**
 * Renderiza el estado de carga
 */
@Composable
private fun renderLoadingState(isPendingTab: Boolean) {
    ModernLoadingState(
        message = if (isPendingTab)
            "Cargando pagos pendientes..."
        else
            "Cargando pagos confirmados...",
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Renderiza el estado de error
 */
@Composable
private fun renderErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    PaymentErrorState(
        message = errorMessage,
        onRetry = onRetry,
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Renderiza el estado vacío
 */
@Composable
private fun renderEmptyState(
    isPendingTab: Boolean,
    onRefresh: () -> Unit
) {
    PaymentEmptyState(
        isPendingTab = isPendingTab,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Renderiza la lista de pagos
 */
@Composable
private fun renderPaymentsList(
    payments: List<SellerPendingPayment>,
    isPendingTab: Boolean,
    paymentSummary: PaymentSummary?,
    isLoadingMore: Boolean,
    hasMorePayments: Boolean,
    onClaimPayment: (Int) -> Unit,
    onRejectPayment: (Int) -> Unit,
    onLoadMore: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Estadísticas rápidas
        item {
            renderPaymentStats(
                isPendingTab = isPendingTab,
                payments = payments,
                paymentSummary = paymentSummary
            )
        }

        // Lista de pagos
        items(
            items = payments,
            key = { it.paymentId }
        ) { payment ->
            renderPaymentItem(
                payment = payment,
                isPendingTab = isPendingTab,
                onClaimPayment = onClaimPayment,
                onRejectPayment = onRejectPayment
            )
        }

        // Indicador de carga más pagos
        if (isLoadingMore) {
            item {
                renderLoadingMoreIndicator()
            }
        }

        // Trigger para cargar más
        if (hasMorePayments && !isLoadingMore) {
            item {
                LaunchedEffect(Unit) {
                    onLoadMore()
                }
            }
        }

        // Espacio final
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Renderiza la tarjeta de estadísticas de pagos
 */
@Composable
private fun renderPaymentStats(
    isPendingTab: Boolean,
    payments: List<SellerPendingPayment>,
    paymentSummary: PaymentSummary?
) {
    val count = paymentSummary?.let {
        if (isPendingTab) it.pendingCount else it.confirmedCount
    } ?: payments.size

    val totalAmount = paymentSummary?.let {
        if (isPendingTab) it.pendingAmount else it.confirmedAmount
    } ?: payments.sumOf { it.amount }

    PaymentStatsCard(
        title = if (isPendingTab) "Pendientes" else "Confirmados",
        count = count,
        totalAmount = totalAmount,
        icon = if (isPendingTab)
            Icons.Filled.Schedule
        else
            Icons.Filled.CheckCircle
    )
}

/**
 * Renderiza un item de pago
 */
@Composable
private fun renderPaymentItem(
    payment: SellerPendingPayment,
    isPendingTab: Boolean,
    onClaimPayment: (Int) -> Unit,
    onRejectPayment: (Int) -> Unit
) {
    if (isPendingTab) {
        ModernPaymentCardWithActions(
            payment = payment,
            onClaim = { onClaimPayment(payment.paymentId) },
            onReject = { onRejectPayment(payment.paymentId) }
        )
    } else {
        ModernPaymentCard(payment = payment)
    }
}

/**
 * Renderiza el indicador de carga infinita
 */
@Composable
private fun renderLoadingMoreIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}