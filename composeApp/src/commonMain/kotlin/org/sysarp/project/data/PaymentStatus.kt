package org.sysarp.project.data

/**
 * Estados de pagos disponibles para filtros
 */
enum class PaymentFilterStatus(val value: String) {
    PENDING("PENDING"),
    CLAIMED("CLAIMED"),
    REJECTED("REJECTED"),
    ALL("ALL")
}

/**
 * Utilidades para manejar estados de pagos
 */
object PaymentFilterStatusUtils {
    /**
     * Convierte una lista de estados a string separado por comas
     */
    fun toCommaSeparatedString(statuses: List<PaymentFilterStatus>): String {
        return statuses.joinToString(",") { it.value }
    }

    /**
     * Convierte un string separado por comas a lista de estados
     */
    fun fromCommaSeparatedString(statusString: String): List<PaymentFilterStatus> {
        return statusString.split(",")
            .map { it.trim() }
            .mapNotNull { statusValue ->
                PaymentFilterStatus.values().find { it.value == statusValue }
            }
    }

    /**
     * Obtiene todos los estados excepto ALL
     */
    fun getAllSpecificStatuses(): List<PaymentFilterStatus> {
        return listOf(PaymentFilterStatus.PENDING, PaymentFilterStatus.CLAIMED, PaymentFilterStatus.REJECTED)
    }

    /**
     * Obtiene estados disponibles para vendedores
     */
    fun getSellerAvailableStatuses(): List<PaymentFilterStatus> {
        return getAllSpecificStatuses()
    }

    /**
     * Obtiene estados disponibles para administradores
     */
    fun getAdminAvailableStatuses(): List<PaymentFilterStatus> {
        return getAllSpecificStatuses() + PaymentFilterStatus.ALL
    }
}
