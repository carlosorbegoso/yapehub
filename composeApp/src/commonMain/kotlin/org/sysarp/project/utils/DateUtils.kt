package org.sysarp.project.utils

/**
 * Utilidades para formateo de fechas y timestamps
 * Compatible con Kotlin Multiplatform
 */

/**
 * Formatea un timestamp ISO a formato dd/MM/yyyy HH:mm
 * @param timestamp Timestamp en formato ISO (ej: "2024-01-15T14:30:00.000Z")
 * @return Fecha formateada como "15/01/2024 14:30" o el timestamp original si falla
 */
fun formatTimestamp(timestamp: String): String {
    return try {
        // Formatear timestamp ISO a formato dd/MM/yyyy HH:mm
        val datePart = timestamp.substringBefore("T")
        val timePart = timestamp.substringAfter("T").substringBefore(".")
        
        val dateComponents = datePart.split("-")
        val timeComponents = timePart.split(":")
        
        if (dateComponents.size >= 3 && timeComponents.size >= 2) {
            "${dateComponents[2]}/${dateComponents[1]}/${dateComponents[0]} ${timeComponents[0]}:${timeComponents[1]}"
        } else {
            timestamp
        }
    } catch (e: Exception) {
        timestamp
    }
}

/**
 * Formatea un timestamp ISO a formato dd/MM/yyyy
 * @param timestamp Timestamp en formato ISO (ej: "2024-01-15T14:30:00.000Z")
 * @return Fecha formateada como "15/01/2024" o "N/A" si falla
 */
fun formatDateOnly(timestamp: String): String {
    return try {
        // Formatear fecha ISO a formato dd/MM/yyyy
        val datePart = timestamp.substringBefore("T")
        val dateComponents = datePart.split("-")
        
        if (dateComponents.size >= 3) {
            "${dateComponents[2]}/${dateComponents[1]}/${dateComponents[0]}"
        } else {
            "N/A"
        }
    } catch (e: Exception) {
        "N/A"
    }
}

/**
 * Formatea un timestamp ISO a formato dd/MM
 * @param timestamp Timestamp en formato ISO (ej: "2024-01-15T14:30:00.000Z")
 * @return Fecha formateada como "15/01" o "N/A" si falla
 */
fun formatShortDate(timestamp: String): String {
    return try {
        // Formatear fecha ISO a formato dd/MM
        val datePart = timestamp.substringBefore("T")
        val dateComponents = datePart.split("-")
        
        if (dateComponents.size >= 3) {
            "${dateComponents[2]}/${dateComponents[1]}"
        } else {
            "N/A"
        }
    } catch (e: Exception) {
        "N/A"
    }
}

/**
 * Formatea un timestamp ISO a formato HH:mm
 * @param timestamp Timestamp en formato ISO (ej: "2024-01-15T14:30:00.000Z")
 * @return Hora formateada como "14:30" o "N/A" si falla
 */
fun formatTimeOnly(timestamp: String): String {
    return try {
        // Formatear timestamp para mostrar solo la hora
        val timePart = timestamp.substringAfter("T").substringBefore(".")
        val timeComponents = timePart.split(":")
        
        if (timeComponents.size >= 2) {
            "${timeComponents[0]}:${timeComponents[1]}"
        } else {
            "N/A"
        }
    } catch (e: Exception) {
        "N/A"
    }
}

/**
 * Formatea un timestamp ISO a formato amigable de tiempo relativo
 * @param timestamp Timestamp en formato ISO (ej: "2024-01-15T14:30:00.000Z")
 * @return Tiempo relativo como "hace 2 horas" o "desconocido" si falla
 */
fun formatRelativeTime(timestamp: String): String {
    return try {
        // Formatear la fecha para mostrar de manera más amigable
        // Extraer solo la parte de la fecha del timestamp ISO
        val datePart = timestamp.substringBefore("T")
        val timePart = timestamp.substringAfter("T").substringBefore(".")
        
        // Formato simple: dd/MM/yyyy HH:mm
        val dateComponents = datePart.split("-")
        val timeComponents = timePart.split(":")
        
        if (dateComponents.size >= 3 && timeComponents.size >= 2) {
            "${dateComponents[2]}/${dateComponents[1]}/${dateComponents[0]} ${timeComponents[0]}:${timeComponents[1]}"
        } else {
            timestamp
        }
    } catch (e: Exception) {
        "desconocido"
    }
}

/**
 * Formatea un timestamp con formato específico usando Clock
 * @param dateTimeString Timestamp en formato "yyyy-MM-dd'T'HH:mm:ss.SSSSSS" (ej: "2024-01-15T14:30:00.123456")
 * @return Fecha formateada como "15/01/2024 14:30" o fallback si falla
 */
fun formatDateTime(dateTimeString: String): String {
    return try {
        // Formatear timestamp con formato específico usando parsing manual
        // Formato esperado: "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
        val datePart = dateTimeString.substringBefore("T")
        val timePart = dateTimeString.substringAfter("T").substringBefore(".")
        
        val dateComponents = datePart.split("-")
        val timeComponents = timePart.split(":")
        
        if (dateComponents.size >= 3 && timeComponents.size >= 2) {
            // Formato dd/MM/yyyy HH:mm
            "${dateComponents[2]}/${dateComponents[1]}/${dateComponents[0]} ${timeComponents[0]}:${timeComponents[1]}"
        } else {
            dateTimeString
        }
    } catch (e: Exception) {
        // Fallback: extraer solo los primeros 16 caracteres (yyyy-MM-ddTHH:mm)
        try {
            dateTimeString.substring(0, 16)
        } catch (e2: Exception) {
            dateTimeString
        }
    }
}
