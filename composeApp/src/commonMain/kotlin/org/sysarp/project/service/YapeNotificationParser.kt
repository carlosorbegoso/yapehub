package org.sysarp.project.service

import org.sysarp.project.data.TransactionType
import org.sysarp.project.data.YapeTransaction
import kotlinx.datetime.Clock

/**
 * Parser para notificaciones reales de Yape
 */
object YapeNotificationParser {
    

    fun parseYapeNotification(
        notificationText: String,
        businessName: String? = null
    ): YapeTransaction? {
        return try {
            val cleanedText = notificationText.trim()
            DebugLogger.info("🔍 [PARSER] Iniciando parseo de: $cleanedText")
            
            // Verificar primero si es una notificación de Yape válida
            if (!isYapeNotification(cleanedText)) {
                DebugLogger.warn("❌ [PARSER] No es una notificación de Yape válida")
                return null
            }
            
            DebugLogger.info("🔍 [PARSER] Procesando notificación de Yape válida")
            
            // Extraer código de seguridad (3 o más dígitos) - buscar cualquier número de 3+ dígitos
            val securityCodePattern = Regex("([0-9]{3,})", RegexOption.IGNORE_CASE)
            val securityCodeMatch = securityCodePattern.find(cleanedText)
            val securityCode = securityCodeMatch?.groupValues?.get(1)
            
            DebugLogger.info("🔍 [PARSER] Código de seguridad encontrado: $securityCode")
            if (securityCode == null) {
                DebugLogger.warn("❌ [PARSER] No se encontró código de seguridad")
                return null
            }
            
            // Extraer monto (S/ X.XX, S/ X,XX, S/ X,XXX.XX)
            val amountPattern = Regex("S/\\s*([0-9,]+(?:\\.[0-9]{1,2})?)", RegexOption.IGNORE_CASE)
            val amountMatch = amountPattern.find(cleanedText)
            val amountString = amountMatch?.groupValues?.get(1)
            
            DebugLogger.info("🔍 [PARSER] Monto encontrado: $amountString")
            if (amountString == null) {
                DebugLogger.warn("❌ [PARSER] No se encontró monto")
                return null
            }
            
            val amount = parseAmount(amountString)
            DebugLogger.info("🔍 [PARSER] Monto parseado: $amount")
            
            // Extraer nombre del remitente - manejar tanto recibidos como enviados
            val senderName = when {
                // Para transacciones recibidas: "Carlos te envió"
                cleanedText.contains("te envió") -> {
                    val senderPattern = Regex("(?:Confirmación de Pago\\s+)?([A-Za-zÁÉÍÓÚáéíóúñÑ\\s.]+?)\\s+te envió", RegexOption.IGNORE_CASE)
                    val senderMatch = senderPattern.find(cleanedText)
                    senderMatch?.groupValues?.get(1)?.trim() ?: "Usuario"
                }
                // Para transacciones enviadas: "Enviaste S/ X.XX a María"
                cleanedText.contains("Enviaste") -> {
                    val senderPattern = Regex("Enviaste\\s+S/\\s*[0-9]+(?:\\.[0-9]{1,2})?\\s+a\\s+([A-Za-zÁÉÍÓÚáéíóúñÑ\\s.]+?)(?:\\s+El|$)", RegexOption.IGNORE_CASE)
                    val senderMatch = senderPattern.find(cleanedText)
                    senderMatch?.groupValues?.get(1)?.trim() ?: "Usuario"
                }
                else -> "Usuario"
            }
            
            val transaction = YapeTransaction(
                id = 0, // Dejar que SQLite auto-genere el ID
                transactionId = "TEMP_ID", // El repositorio generará el ID único
                amount = amount,
                currency = "PEN",
                senderName = senderName,
                senderPhone = null,
                message = cleanedText,
                transactionType = TransactionType.RECEIVED,
                businessName = businessName ?: "Negocio Principal",
                createdAt = Clock.System.now(),
                processedAt = null,
                isProcessed = false,
                rawNotification = notificationText,
                securityCode = securityCode
            )
            
            DebugLogger.info("✅ [PARSER] Transacción creada exitosamente: ${transaction.amount} PEN de ${transaction.senderName}")
            return transaction
        } catch (e: Exception) {
            DebugLogger.error("❌ [PARSER] Error parseando notificación de Yape: ${e.message}")
            null
        }
    }
    
    /**
     * Convierte un string de cantidad a Double
     */
    private fun parseAmount(amountString: String): Double {
        return amountString
            .replace(",", "")
            .toDoubleOrNull() ?: 0.0
    }
    
    /**
     * Verifica si una notificación es de Yape
     * Maneja múltiples indicadores de notificaciones de Yape
     */
    fun isYapeNotification(notificationText: String): Boolean {
        // Verificar que tenga los elementos esenciales
        val hasAmount = notificationText.contains("S/", ignoreCase = true)
        val hasSecurityCode = Regex("\\d{3,}").containsMatchIn(notificationText)
        val hasPaymentIndicator = Regex("\\b(pago|envió|enviaste)\\b", RegexOption.IGNORE_CASE).containsMatchIn(notificationText)
        val hasYapeIndicator = Regex("\\b(yape|confirmación|confirmacion)\\b", RegexOption.IGNORE_CASE).containsMatchIn(notificationText)
        
        // Si tiene indicadores de Yape, es válida si tiene monto y código
        // Si no tiene indicadores de Yape, debe tener monto, código y indicador de pago
        val isYape = if (hasYapeIndicator) {
            hasAmount && hasSecurityCode
        } else {
            hasAmount && hasSecurityCode && hasPaymentIndicator
        }
        
        DebugLogger.debug("🔍 Verificando si es notificación de Yape: $isYape")
        DebugLogger.debug("🔍 - Monto: $hasAmount, Código: $hasSecurityCode, Pago: $hasPaymentIndicator, Yape: $hasYapeIndicator")
        if (isYape) {
            DebugLogger.debug("📱 Notificación de Yape válida: $notificationText")
        }
        return isYape
    }

}
