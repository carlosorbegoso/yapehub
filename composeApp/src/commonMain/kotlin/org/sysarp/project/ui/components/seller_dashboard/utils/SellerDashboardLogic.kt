package org.sysarp.project.ui.components.seller_dashboard.utils

import org.sysarp.project.data.SellerPendingPayment

/**
 * Utilidades y lógica para el dashboard del vendedor
 */
object SellerDashboardLogic {
    
    /**
     * Mejora mensajes de error para ser más comprensibles
     */
    fun getImprovedErrorMessage(error: String): String {
        return when {
            error.contains("ya fue procesado") -> "Este pago ya fue procesado anteriormente"
            error.contains("Invalid paymentId") -> "El pago no es válido o ya fue procesado"
            error.contains("INVALID_FIELD") -> "Error en los datos del pago"
            error.contains("El pago ya fue procesado") -> "Este pago ya fue procesado anteriormente"
            else -> "Error al procesar el pago: $error"
        }
    }
    
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
    
    /**
     * Genera mensaje de éxito para confirmación de pago
     */
    fun getSuccessMessage(amount: Double, isConfirmed: Boolean): String {
        val action = if (isConfirmed) "✅ Pago confirmado exitosamente" else "❌ Pago rechazado exitosamente"
        return "$action - S/ ${String.format("%.2f", amount)}"
    }
}
