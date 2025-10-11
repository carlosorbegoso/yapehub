package org.sysarp.project.ui.seller.screens.payments.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.data.UserProfile
import org.sysarp.project.data.PaymentFilterStatus
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.ui.common.components.cards.ModernPaymentCard
import org.sysarp.project.ui.common.components.cards.ModernPaymentCardWithActions
import org.sysarp.project.ui.common.components.cards.PaymentStatsCard
import org.sysarp.project.ui.common.components.states.ModernLoadingState
import org.sysarp.project.ui.common.components.states.PaymentEmptyState
import org.sysarp.project.ui.common.components.states.PaymentErrorState

/**
 * Componente principal de pagos de vendedor con UI/UX moderna y atractiva
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
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Contenido principal
        when {
            isLoading -> {
                ModernLoadingState(
                    message = if (isPendingTab) "Cargando pagos pendientes..." else "Cargando pagos confirmados...",
                    modifier = Modifier.fillMaxSize()
                )
            }
            errorMessage.isNotEmpty() -> {
                PaymentErrorState(
                    message = errorMessage,
                    onRetry = onRefresh,
                    modifier = Modifier.fillMaxSize()
                )
            }
            payments.isEmpty() -> {
                PaymentEmptyState(
                    isPendingTab = isPendingTab,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Estadísticas rápidas
                    item {
                        PaymentStatsCard(
                            title = if (isPendingTab) "Pendientes" else "Confirmados",
                            count = payments.size,
                            totalAmount = payments.sumOf { it.amount },
                            icon = if (isPendingTab)
                                androidx.compose.material.icons.Icons.Filled.Schedule
                            else
                                androidx.compose.material.icons.Icons.Filled.CheckCircle
                        )
                    }
                    
                    // Lista de pagos
                    items(payments) { payment ->
                        if (isPendingTab) {
                            ModernPaymentCardWithActions(
                                payment = payment,
                                onClaim = {
                                    // Lógica de confirmación
                                    onRefresh()
                                },
                                onReject = {
                                    // Lógica de rechazo
                                    onRefresh()
                                }
                            )
                        } else {
                            ModernPaymentCard(
                                payment = payment
                            )
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
