package org.sysarp.project.utils

/**
 * Extrae el código corto de Yape del código completo
 * Ejemplo: "YAPE_1757840346620_123456_751" -> "751"
 */
fun extractShortYapeCode(fullYapeCode: String): String {
    return try {
        val parts = fullYapeCode.split("_")
        if (parts.size >= 4) {
            parts.last() // Retorna la última parte (el número corto)
        } else {
            fullYapeCode // Si no se puede parsear, retorna el código completo
        }
    } catch (e: Exception) {
        fullYapeCode // En caso de error, retorna el código completo
    }
}
