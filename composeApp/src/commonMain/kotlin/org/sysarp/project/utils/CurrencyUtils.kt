package org.sysarp.project.utils

/**
 * Utilidades para formateo de monedas
 * Compatible con Kotlin Multiplatform
 */

/**
 * Formatea un valor numérico como moneda en soles peruanos
 * @param value Valor numérico a formatear
 * @return String formateado como "S/ XX.XX"
 */
fun formatCurrency(value: Double): String {
    return formatCurrency(value)
}

/**
 * Formatea un valor numérico como moneda en soles peruanos (versión con Int)
 * @param value Valor entero a formatear
 * @return String formateado como "S/ XX.XX"
 */
fun formatCurrency(value: Int): String {
    return "S/ ${"%.2f".format(value.toDouble())}"
}

/**
 * Formatea un valor numérico como moneda en soles peruanos (versión con Float)
 * @param value Valor flotante a formatear
 * @return String formateado como "S/ XX.XX"
 */
fun formatCurrency(value: Float): String {
    return "S/ ${"%.2f".format(value.toDouble())}"
}

/**
 * Formatea un valor numérico como moneda sin símbolo
 * @param value Valor numérico a formatear
 * @return String formateado como "XX.XX"
 */
fun formatAmount(value: Double): String {
    return "%.2f".format(value)
}

/**
 * Formatea un valor numérico como moneda sin símbolo (versión con Int)
 * @param value Valor entero a formatear
 * @return String formateado como "XX.XX"
 */
fun formatAmount(value: Int): String {
    return "%.2f".format(value.toDouble())
}

/**
 * Formatea un valor numérico como moneda sin símbolo (versión con Float)
 * @param value Valor flotante a formatear
 * @return String formateado como "XX.XX"
 */
fun formatAmount(value: Float): String {
    return "%.2f".format(value.toDouble())
}

/**
 * Formatea un valor numérico como moneda con símbolo personalizado
 * @param value Valor numérico a formatear
 * @param symbol Símbolo de moneda (por defecto "S/")
 * @return String formateado como "symbol XX.XX"
 */
fun formatCurrency(value: Double, symbol: String = "S/"): String {
    return "$symbol ${"%.2f".format(value)}"
}

/**
 * Formatea un valor numérico como moneda con símbolo personalizado (versión con Int)
 * @param value Valor entero a formatear
 * @param symbol Símbolo de moneda (por defecto "S/")
 * @return String formateado como "symbol XX.XX"
 */
fun formatCurrency(value: Int, symbol: String = "S/"): String {
    return "$symbol ${"%.2f".format(value.toDouble())}"
}

/**
 * Formatea un valor numérico como moneda con símbolo personalizado (versión con Float)
 * @param value Valor flotante a formatear
 * @param symbol Símbolo de moneda (por defecto "S/")
 * @return String formateado como "symbol XX.XX"
 */
fun formatCurrency(value: Float, symbol: String = "S/"): String {
    return "$symbol ${"%.2f".format(value.toDouble())}"
}
