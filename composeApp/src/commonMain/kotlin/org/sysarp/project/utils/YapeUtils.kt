package org.sysarp.project.utils


fun extractShortYapeCode(fullYapeCode: String): String {
    return try {
        // Si contiene guiones bajos, extraer la última parte
        if (fullYapeCode.contains("_")) {
            val parts = fullYapeCode.split("_")
            parts.last() // Retorna la última parte (el número corto)
        } else {
            // Si no contiene guiones bajos, buscar números al final
            val regex = Regex("\\d+$")
            val match = regex.find(fullYapeCode)
            match?.value ?: fullYapeCode
        }
    } catch (e: Exception) {
        fullYapeCode // En caso de error, retorna el código completo
    }
}
