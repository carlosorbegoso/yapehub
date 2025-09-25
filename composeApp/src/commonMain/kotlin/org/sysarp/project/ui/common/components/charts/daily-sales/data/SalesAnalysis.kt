package org.sysarp.project.ui.common.components.charts.daily.sales.data

import org.sysarp.project.data.DailySalesData
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Enum para tipos de tendencia más robusto
 */
enum class TrendType(val displayName: String, val threshold: Double) {
    UP("up", 5.0),
    DOWN("down", -5.0),
    STABLE("stable", 0.0)
}

/**
 * Modelo de datos para análisis de ventas diarias mejorado
 * 
 * @param trend Tipo de tendencia calculada
 * @param trendPercentage Porcentaje de cambio en la tendencia
 * @param peakDay Día con las mayores ventas
 * @param valleyDay Día con las menores ventas
 * @param averageGrowth Crecimiento promedio diario
 * @param volatility Volatilidad de las ventas
 * @param confidence Nivel de confianza del análisis (0.0 - 1.0)
 * @param dataQuality Calidad de los datos analizados
 */
data class SalesAnalysis(
    val trend: TrendType,
    val trendPercentage: Double,
    val peakDay: DailySalesData?,
    val valleyDay: DailySalesData?,
    val averageGrowth: Double,
    val volatility: Double,
    val confidence: Double = 0.0,
    val dataQuality: DataQuality = DataQuality.GOOD
)

/**
 * Enum para calidad de datos
 */
enum class DataQuality(val displayName: String) {
    EXCELLENT("Excelente"),
    GOOD("Buena"),
    FAIR("Regular"),
    POOR("Pobre")
}

/**
 * Analiza los datos de ventas diarias y calcula métricas importantes mejoradas
 * 
 * @param dailySales Lista de datos de ventas diarias
 * @return SalesAnalysis con todas las métricas calculadas y validaciones
 */
fun analyzeSalesData(dailySales: List<DailySalesData>): SalesAnalysis {
    // Validación inicial
    if (dailySales.isEmpty()) {
        return SalesAnalysis(
            trend = TrendType.STABLE,
            trendPercentage = 0.0,
            peakDay = null,
            valleyDay = null,
            averageGrowth = 0.0,
            volatility = 0.0,
            confidence = 0.0,
            dataQuality = DataQuality.POOR
        )
    }
    
    // Validar calidad de datos
    val dataQuality = assessDataQuality(dailySales)
    
    // Optimizar: calcular sales una sola vez
    val sales = dailySales.map { it.sales }
    val peakDay = dailySales.maxByOrNull { it.sales }
    val valleyDay = dailySales.minByOrNull { it.sales }
    
    // Análisis de tendencia mejorado con regresión lineal simple
    val trendAnalysis = calculateTrendWithRegression(sales)
    
    // Calcular crecimiento promedio día a día (optimizado)
    val growthRates = sales.zipWithNext { current, next ->
        if (current > 0) ((next - current) / current) * 100 else 0.0
    }
    val averageGrowth = growthRates.average()
    
    // Calcular volatilidad (desviación estándar)
    val mean = sales.average()
    val variance = sales.map { (it - mean).pow(2) }.average()
    val volatility = sqrt(variance)
    
    // Calcular confianza basada en consistencia de datos
    val confidence = calculateConfidence(sales, volatility, dataQuality)
    
    return SalesAnalysis(
        trend = trendAnalysis.first,
        trendPercentage = abs(trendAnalysis.second),
        peakDay = peakDay,
        valleyDay = valleyDay,
        averageGrowth = averageGrowth,
        volatility = volatility,
        confidence = confidence,
        dataQuality = dataQuality
    )
}

/**
 * Evalúa la calidad de los datos de ventas
 */
private fun assessDataQuality(dailySales: List<DailySalesData>): DataQuality {
    val sales = dailySales.map { it.sales }
    val zeroCount = sales.count { it == 0.0 }
    val zeroPercentage = zeroCount.toDouble() / sales.size
    
    return when {
        zeroPercentage > 0.5 -> DataQuality.POOR
        zeroPercentage > 0.2 -> DataQuality.FAIR
        sales.size < 3 -> DataQuality.FAIR
        else -> DataQuality.GOOD
    }
}

/**
 * Calcula tendencia usando regresión lineal simple
 */
private fun calculateTrendWithRegression(sales: List<Double>): Pair<TrendType, Double> {
    if (sales.size < 2) return TrendType.STABLE to 0.0
    
    val n = sales.size
    val xValues = (0 until n).map { it.toDouble() }
    val yValues = sales
    
    // Calcular pendiente (slope) usando fórmula de regresión lineal
    val sumX = xValues.sum()
    val sumY = yValues.sum()
    val sumXY = xValues.zip(yValues) { x, y -> x * y }.sum()
    val sumXX = xValues.map { it * it }.sum()
    
    val slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX)
    val trendPercentage = slope * 100
    
    val trend = when {
        trendPercentage > TrendType.UP.threshold -> TrendType.UP
        trendPercentage < TrendType.DOWN.threshold -> TrendType.DOWN
        else -> TrendType.STABLE
    }
    
    return trend to trendPercentage
}

/**
 * Calcula el nivel de confianza del análisis
 */
private fun calculateConfidence(sales: List<Double>, volatility: Double, dataQuality: DataQuality): Double {
    val baseConfidence = when (dataQuality) {
        DataQuality.EXCELLENT -> 0.95
        DataQuality.GOOD -> 0.85
        DataQuality.FAIR -> 0.70
        DataQuality.POOR -> 0.50
    }
    
    // Reducir confianza si hay alta volatilidad
    val volatilityPenalty = minOf(volatility / sales.average(), 0.3)
    
    return maxOf(baseConfidence - volatilityPenalty, 0.1)
}
