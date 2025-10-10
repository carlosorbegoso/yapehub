package org.sysarp.project.ui.seller.screens.payments.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.payment.PaymentService

/**
 * Componente principal de pagos de vendedor
 * Refactorizado para ser más modular y mantenible
 */
@Composable
fun SellerPaymentsContent(
    payments: List<SellerPendingPayment>,
    isLoading: Boolean,
    errorMessage: String,
    onRefresh: () -> Unit,
    userProfile: UserProfile?,
    accessToken: String?,
    paymentService: PaymentService,
    onError: (String) -> Unit,
    isPendingTab: Boolean = true
) {
    when {
        isLoading -> SellerPaymentsLoadingCard()
        errorMessage.isNotEmpty() -> SellerPaymentsErrorCard(
            errorMessage = errorMessage,
            onRefresh = onRefresh
        )
        payments.isEmpty() -> SellerPaymentsEmptyState(
            title = if (isPendingTab) "No hay pagos pendientes" else "No hay pagos confirmados",
            subtitle = if (isPendingTab) 
                "Los pagos aparecerán aquí cuando los clientes realicen transacciones."
            else 
                "Los pagos confirmados aparecerán aquí.",
            icon = if (isPendingTab) 
                androidx.compose.material.icons.Icons.Filled.Schedule
            else 
                androidx.compose.material.icons.Icons.Filled.CheckCircle
        )
        else -> {
            Column {
                // Estadísticas rápidas
                SellerPaymentsStatsCard(
                    title = if (isPendingTab) "Pendientes" else "Confirmados",
                    count = payments.size,
                    totalAmount = payments.sumOf { it.amount },
                    icon = if (isPendingTab) 
                        androidx.compose.material.icons.Icons.Filled.Schedule 
                    else 
                        androidx.compose.material.icons.Icons.Filled.CheckCircle
                )

                // Lista de pagos
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(payments) { payment ->
                        if (isPendingTab) {
                            PendingPaymentCardWithActions(
                                payment = payment,
                                sellerId = userProfile?.sellerId?.toInt(),
                                accessToken = accessToken,
                                paymentService = paymentService,
                                onClaimPayment = onRefresh,
                                onError = onError
                            )
                        } else {
                            ConfirmedPaymentCard(payment = payment)
                        }
                    }

                    // Espacio adicional
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}
