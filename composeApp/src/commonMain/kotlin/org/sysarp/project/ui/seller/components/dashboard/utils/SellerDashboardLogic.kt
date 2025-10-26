package org.sysarp.project.ui.seller.components.dashboard.utils

import org.sysarp.project.data.SellerPendingPayment

object SellerDashboardLogic {


    fun filterPayments(
        payments: List<SellerPendingPayment>,
        searchQuery: String,
        selectedFilter: String
    ): List<SellerPendingPayment> {
        var filtered = payments

        // Aplicar filtro de búsqueda
        filtered = applySearchFilter(filtered, searchQuery)

        // Aplicar filtro de estado
        filtered = applyStatusFilter(filtered, selectedFilter)

        return filtered
    }


    private fun applySearchFilter(
        payments: List<SellerPendingPayment>,
        searchQuery: String
    ): List<SellerPendingPayment> {
        if (searchQuery.isEmpty()) return payments

        return payments.filter { payment ->
            matchesSearchQuery(payment, searchQuery)
        }
    }


    private fun matchesSearchQuery(
        payment: SellerPendingPayment,
        query: String
    ): Boolean {
        val lowerQuery = query.lowercase()

        return payment.senderName.lowercase().contains(lowerQuery) ||
                payment.yapeCode.lowercase().contains(lowerQuery) ||
                payment.paymentId.toString().contains(lowerQuery)
    }


    private fun applyStatusFilter(
        payments: List<SellerPendingPayment>,
        selectedFilter: String
    ): List<SellerPendingPayment> {
        return when (selectedFilter) {
            "Todos" -> payments
            "Pendientes" -> payments.filter { it.status == "PENDING" }
            "Confirmados" -> payments.filter { it.status == "CLAIMED" }
            "Rechazados" -> payments.filter { it.status == "REJECTED" }
            else -> payments
        }
    }

}

