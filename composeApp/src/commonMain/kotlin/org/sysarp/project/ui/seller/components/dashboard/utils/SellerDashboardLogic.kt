package org.sysarp.project.ui.components.seller_dashboard.utils

import org.sysarp.project.data.SellerPendingPayment

/**
 * Utilidades y lógica para el dashboard del vendedor
 */
object SellerDashboardLogic {

    /**
     * Filtra pagos pendientes según criterios de búsqueda y filtros
     */
    fun filterPayments(
        payments: List<SellerPendingPayment>,
        searchQuery: String,
        selectedFilter: String
    ): List<SellerPendingPayment> {
        var filtered = payments
        
        // Filtrar por búsqueda
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter { payment ->
                payment.senderName.contains(searchQuery, ignoreCase = true) ||
                payment.yapeCode.contains(searchQuery, ignoreCase = true) ||
                payment.paymentId.toString().contains(searchQuery, ignoreCase = true)
            }
        }
        
        // Filtrar por estado
        when (selectedFilter) {
            "Pendientes" -> filtered = filtered.filter { it.status == "PENDING" }
            "Procesando" -> filtered = filtered.filter { 
                // Esta lógica se manejará en el componente padre
                true 
            }
        }
        
        return filtered
    }

}
