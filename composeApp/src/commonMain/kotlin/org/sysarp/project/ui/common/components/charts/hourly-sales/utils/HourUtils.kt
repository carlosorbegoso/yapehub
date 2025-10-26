package org.sysarp.project.ui.common.components.charts.hourly.sales.utils

/**
 * Utilidades para el manejo de horas en componentes de gráficos
 */

/**
 * Convierte una hora string a Int de forma segura
 */
fun String.parseHour(): Int {
    return try {
        this.toInt()
    } catch (e: Exception) {
        0 // Devolver 0 si hay error
    }
}

/**
 * Formatea una hora string con ceros a la izquierda
 */
fun String.formatHour(): String {
    return try {
        val hourInt = this.toInt()
        if (hourInt < 10) "0$hourInt" else hourInt.toString()
    } catch (e: Exception) {
        this // Devolver el original si hay error
    }
}

/**
 * Determina el período del día basado en la hora
 */
fun getTimeOfDay(hour: String): String {
    val hourInt = hour.parseHour()
    return when (hourInt) {
        in 6..11 -> "Mañana"
        in 12..17 -> "Tarde"
        in 18..23 -> "Noche"
        else -> "Madrugada"
    }
}
