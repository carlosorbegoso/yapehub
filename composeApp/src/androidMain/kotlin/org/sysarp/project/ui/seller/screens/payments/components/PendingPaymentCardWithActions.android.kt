package org.sysarp.project.ui.seller.screens.payments.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Implementación Android para formatear timestamps
 */
actual fun formatTimestamp(timestamp: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        formatter.format(parser.parse(timestamp) ?: Date())
    } catch (e: Exception) {
        timestamp // Return original if parsing fails
    }
}
