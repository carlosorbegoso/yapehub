package org.sysarp.project.utils

import kotlin.math.round

/**
 * Utilidades para formateo de monedas
 * Compatible con Kotlin Multiplatform
 */

/**
 * Formatea un número a string con decimales específicos
 */
private fun Double.formatToDecimalPlaces(decimalPlaces: Int): String {
    val multiplier = when (decimalPlaces) {
        0 -> 1.0
        1 -> 10.0
        2 -> 100.0
        else -> throw IllegalArgumentException("Solo se soportan 0-2 decimales")
    }

    val rounded = round(this * multiplier) / multiplier

    return when (decimalPlaces) {
        0 -> "${rounded.toInt()}"
        1 -> {
            val intPart = rounded.toInt()
            val decimalPart = ((rounded - intPart) * 10).toInt()
            "$intPart.$decimalPart"
        }
        2 -> {
            val intPart = rounded.toInt()
            val decimalPart = ((rounded - intPart) * 100).toInt()
            if (decimalPart < 10) {
                "$intPart.0$decimalPart"
            } else {
                "$intPart.$decimalPart"
            }
        }
        else -> rounded.toString()
    }
}

/**
 * Formatea un valor numérico como moneda en soles peruanos
 * @param value Valor numérico a formatear
 * @return String formateado como "S/ XX.XX"
 */
fun formatCurrency(value: Double): String {
    return "S/ ${value.formatToDecimalPlaces(2)}"
}

/**
 * Formatea un valor numérico como moneda en soles peruanos (versión con Int)
 * @param value Valor entero a formatear
 * @return String formateado como "S/ XX.XX"
 */
fun formatCurrency(value: Int): String {
    return "S/ ${value.toDouble().formatToDecimalPlaces(2)}"
}

/**
 * Formatea un valor numérico como moneda en soles peruanos (versión con Float)
 * @param value Valor flotante a formatear
 * @return String formateado como "S/ XX.XX"
 */
fun formatCurrency(value: Float): String {
    return "S/ ${value.toDouble().formatToDecimalPlaces(2)}"
}

/**
 * Formatea un valor numérico como moneda sin símbolo
 * @param value Valor numérico a formatear
 * @return String formateado como "XX.XX"
 */
fun formatAmount(value: Double): String {
    return value.formatToDecimalPlaces(2)
}




/**
 * Formatea un valor numérico como moneda con símbolo personalizado
 * @param value Valor numérico a formatear
 * @param symbol Símbolo de moneda (por defecto "S/")
 * @return String formateado como "symbol XX.XX"
 */
fun formatCurrency(value: Double, symbol: String = "S/"): String {
    return "$symbol ${value.formatToDecimalPlaces(2)}"
}

/**
 * Formatea un valor numérico como moneda con símbolo personalizado (versión con Int)
 * @param value Valor entero a formatear
 * @param symbol Símbolo de moneda (por defecto "S/")
 * @return String formateado como "symbol XX.XX"
 */
fun formatCurrency(value: Int, symbol: String = "S/"): String {
    return "$symbol ${value.toDouble().formatToDecimalPlaces(2)}"
}

/**
 * Formatea un valor numérico como moneda con símbolo personalizado (versión con Float)
 * @param value Valor flotante a formatear
 * @param symbol Símbolo de moneda (por defecto "S/")
 * @return String formateado como "symbol XX.XX"
 */
fun formatCurrency(value: Float, symbol: String = "S/"): String {
    return "$symbol ${value.toDouble().formatToDecimalPlaces(2)}"
}

/**
 * Formatea un valor numérico como moneda sin decimales (para enteros)
 * @param value Valor numérico a formatear
 * @return String formateado como "S/ XX"
 */
fun formatCurrencyNoDecimals(value: Double): String {
    return "S/ ${value.formatToDecimalPlaces(0)}"
}

/**
 * Formatea un valor numérico como moneda sin decimales (versión con Int)
 * @param value Valor entero a formatear
 * @return String formateado como "S/ XX"
 */
fun formatCurrencyNoDecimals(value: Int): String {
    return "S/ ${value.toDouble().formatToDecimalPlaces(0)}"
}

/**
 * Formatea un valor numérico como moneda sin decimales (versión con Float)
 * @param value Valor flotante a formatear
 * @return String formateado como "S/ XX"
 */
fun formatCurrencyNoDecimals(value: Float): String {
    return "S/ ${value.toDouble().formatToDecimalPlaces(0)}"
}

/**
 * Formatea un porcentaje con un decimal
 * @param value Valor numérico a formatear
 * @return String formateado como "XX.X%"
 */
fun formatPercentage(value: Double): String {
    return "${value.formatToDecimalPlaces(1)}%"
}

/**
 * Formatea un porcentaje con un decimal (versión con Float)
 * @param value Valor flotante a formatear
 * @return String formateado como "XX.X%"
 */
fun formatPercentage(value: Float): String {
    return "${value.toDouble().formatToDecimalPlaces(1)}%"
}

/**
 * Formatea un valor numérico con un decimal
 * @param value Valor numérico a formatear
 * @return String formateado como "XX.X"
 */
fun formatOneDecimal(value: Double): String {
    return value.formatToDecimalPlaces(1)
}

