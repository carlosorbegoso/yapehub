package org.sysarp.project.utils

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.Duration

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
 * Formatea un timestamp ISO a formato HH:mm
 * @param timestamp Timestamp en formato ISO (ej: "2024-01-15T14:30:00.000Z")
 * @return Hora formateada como "14:30" o "N/A" si falla
 */
fun formatTimeOnly(timestamp: String): String {
    return try {
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
        try {
            dateTimeString.substring(0, 16)
        } catch (e2: Exception) {
            dateTimeString
        }
    }
}

/**
 * Calcula el tiempo transcurrido de forma precisa usando kotlinx-datetime
 * @param createdAt Timestamp en formato ISO (ej: "2024-01-15T14:30:00.000Z")
 * @return Tiempo relativo como "hace 2 horas" o "hace 3 días" o "desconocido" si falla
 */
@OptIn(ExperimentalTime::class)
fun calculatePreciseTimeElapsed(createdAt: String): String {
    return try {
        val createdInstant = Instant.parse(createdAt)
        val now = Clock.System.now()
        val duration = now - createdInstant

        when {
            duration.inWholeSeconds < 60 -> "Ahora mismo"
            duration.inWholeMinutes < 60 -> "Hace ${duration.inWholeMinutes} min"
            duration.inWholeHours < 24 -> "Hace ${duration.inWholeHours} h"
            duration.inWholeDays < 7 -> "Hace ${duration.inWholeDays} días"
            duration.inWholeDays < 30 -> "Hace ${duration.inWholeDays / 7} sem"
            duration.inWholeDays < 365 -> "Hace ${duration.inWholeDays / 30} mes"
            else -> "Hace ${duration.inWholeDays / 365} año"
        }
    } catch (e: Exception) {
        try {
            // Fallback simple usando solo strings
            val createdDate = createdAt.substring(0, 10)
            val now = Clock.System.now()
            val nowString = now.toString()
            val currentDate = nowString.substring(0, 10)

            if (createdDate == currentDate) {
                "Hoy"
            } else {
                "Reciente"
            }
        } catch (e2: Exception) {
            "Desconocido"
        }
    }
}


fun getCurrentTimestampMs(): Long {
    return Clock.System.now().toEpochMilliseconds()
}

fun calculateDurationMs(startTime: Long, endTime: Long? = null): Long {
    val end = endTime ?: getCurrentTimestampMs()
    return end - startTime
}

fun getFormattedTimestamp(): String {
    return try {
        val now = Clock.System.now()
        val nowString = now.toString()
        val dateTime = nowString.substring(0, 19).replace("T", " ")
        "$dateTime.000"
    } catch (e: Exception) {
        "N/A"
    }
}

/**
 * Convierte un período del calendario en fechas específicas para la API
 * @param period Período seleccionado (ej: "📅 Hoy", "📅 7 días", "📅 30 días", "📅 90 días")
 * @return Par de fechas (startDate, endDate) en formato "yyyy-MM-dd" o null si no hay filtro
 */
fun convertPeriodToDates(period: String): Pair<String?, String?> {
    return try {
        val now = Clock.System.now()
        val todayStr = now.toString().substring(0, 10) // yyyy-MM-dd
        
        when (period) {
            "📅 Hoy" -> Pair(todayStr, todayStr)
            "📅 7 días" -> {
                val sevenDaysAgoMs = now.toEpochMilliseconds() - (7 * 24 * 60 * 60 * 1000L)
                val sevenDaysAgo = Instant.fromEpochMilliseconds(sevenDaysAgoMs)
                val sevenDaysAgoStr = sevenDaysAgo.toString().substring(0, 10)
                Pair(sevenDaysAgoStr, todayStr)
            }
            "📅 30 días" -> {
                val thirtyDaysAgoMs = now.toEpochMilliseconds() - (30 * 24 * 60 * 60 * 1000L)
                val thirtyDaysAgo = Instant.fromEpochMilliseconds(thirtyDaysAgoMs)
                val thirtyDaysAgoStr = thirtyDaysAgo.toString().substring(0, 10)
                Pair(thirtyDaysAgoStr, todayStr)
            }
            "📅 90 días" -> {
                val ninetyDaysAgoMs = now.toEpochMilliseconds() - (90 * 24 * 60 * 60 * 1000L)
                val ninetyDaysAgo = Instant.fromEpochMilliseconds(ninetyDaysAgoMs)
                val ninetyDaysAgoStr = ninetyDaysAgo.toString().substring(0, 10)
                Pair(ninetyDaysAgoStr, todayStr)
            }
            else -> {
                // Verificar si es un rango de fechas personalizado (formato: "2025-10-09 - 2025-10-10")
                if (period.contains(" - ")) {
                    val parts = period.split(" - ")
                    if (parts.size == 2) {
                        val startDate = parts[0].trim()
                        val endDate = parts[1].trim()
                        Pair(startDate, endDate)
                    } else {
                        Pair(null, null)
                    }
                } else {
                    // Verificar si es una fecha individual (formato: "2025-10-09")
                    val dateRegex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
                    if (dateRegex.matches(period.trim())) {
                        val singleDate = period.trim()
                        Pair(singleDate, singleDate) // Usar la misma fecha como inicio y fin
                    } else {
                        Pair(null, null) // Sin filtro de fecha
                    }
                }
            }
        }
    } catch (e: Exception) {
        // Fallback simple usando fechas fijas
        val todayStr = Clock.System.now().toString().substring(0, 10)
        when (period) {
            "📅 Hoy" -> Pair(todayStr, todayStr)
            "📅 7 días" -> Pair("2024-01-01", todayStr) // Fallback
            "📅 30 días" -> Pair("2024-01-01", todayStr) // Fallback
            "📅 90 días" -> Pair("2024-01-01", todayStr) // Fallback
            else -> {
                // Verificar si es una fecha individual en el fallback
                val dateRegex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
                if (dateRegex.matches(period.trim())) {
                    val singleDate = period.trim()
                    Pair(singleDate, singleDate)
                } else {
                    Pair(null, null)
                }
            }
        }
    }
}

/**
 * Obtiene la fecha actual en formato yyyy-MM-dd
 */
fun getCurrentDateString(): String {
    return try {
        Clock.System.now().toString().substring(0, 10)
    } catch (e: Exception) {
        "2024-01-01" // Fallback
    }
}


