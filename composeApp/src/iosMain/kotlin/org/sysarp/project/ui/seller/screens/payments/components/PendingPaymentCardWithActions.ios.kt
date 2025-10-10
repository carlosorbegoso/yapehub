package org.sysarp.project.ui.seller.screens.payments.components

import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale

/**
 * Implementación iOS para formatear timestamps
 */
actual fun formatTimestamp(timestamp: String): String {
    return try {
        val inputFormatter = NSDateFormatter()
        inputFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
        
        val outputFormatter = NSDateFormatter()
        outputFormatter.dateFormat = "dd/MM/yyyy HH:mm"
        outputFormatter.locale = NSLocale.currentLocale
        
        val date = inputFormatter.dateFromString(timestamp)
        if (date != null) {
            outputFormatter.stringFromDate(date)
        } else {
            timestamp
        }
    } catch (e: Exception) {
        timestamp // Return original if parsing fails
    }
}
