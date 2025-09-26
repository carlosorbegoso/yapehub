package org.sysarp.project.ui.common.components.charts.hourly.sales.data

import org.sysarp.project.data.HourlySalesData

/**
 * Análisis inteligente de datos de ventas por hora
 */
data class HourlyAnalysis(
    val peakHour: HourlySalesData?,
    val totalSales: Double,
    val activeHours: Int,
    val averageSales: Double,
    val maxSales: Double,
    val peakHourSales: Double,
    val inactiveHours: Int,
    val salesDistribution: SalesDistribution
)

data class SalesDistribution(
    val morning: Double,    // 6-12
    val afternoon: Double, // 12-18
    val evening: Double,   // 18-24
    val night: Double      // 0-6
)

/**
 * Analiza los datos de ventas por hora y genera métricas inteligentes
 */
fun analyzeHourlySales(hourlySales: List<HourlySalesData>): HourlyAnalysis {
    if (hourlySales.isEmpty()) {
        return HourlyAnalysis(
            peakHour = null,
            totalSales = 0.0,
            activeHours = 0,
            averageSales = 0.0,
            maxSales = 0.0,
            peakHourSales = 0.0,
            inactiveHours = 24,
            salesDistribution = SalesDistribution(0.0, 0.0, 0.0, 0.0)
        )
    }

    val peakHour = hourlySales.maxByOrNull { it.sales }
    val totalSales = hourlySales.sumOf { it.sales }
    val activeHours = hourlySales.count { it.sales > 0 }
    val averageSales = if (activeHours > 0) totalSales / activeHours else 0.0
    val maxSales = hourlySales.maxOfOrNull { it.sales } ?: 0.0
    val inactiveHours = 24 - activeHours

    // Distribución por períodos del día
    val morningSales = hourlySales.filter { 
        val hour = it.hour.substring(0, 2).toIntOrNull() ?: 0
        hour in 6..11 
    }.sumOf { it.sales }
    
    val afternoonSales = hourlySales.filter { 
        val hour = it.hour.substring(0, 2).toIntOrNull() ?: 0
        hour in 12..17 
    }.sumOf { it.sales }
    
    val eveningSales = hourlySales.filter { 
        val hour = it.hour.substring(0, 2).toIntOrNull() ?: 0
        hour in 18..23 
    }.sumOf { it.sales }
    
    val nightSales = hourlySales.filter { 
        val hour = it.hour.substring(0, 2).toIntOrNull() ?: 0
        hour in 0..5 
    }.sumOf { it.sales }

    return HourlyAnalysis(
        peakHour = peakHour,
        totalSales = totalSales,
        activeHours = activeHours,
        averageSales = averageSales,
        maxSales = maxSales,
        peakHourSales = peakHour?.sales ?: 0.0,
        inactiveHours = inactiveHours,
        salesDistribution = SalesDistribution(
            morning = morningSales,
            afternoon = afternoonSales,
            evening = eveningSales,
            night = nightSales
        )
    )
}

/**
 * Calcula el color de la barra basado en la intensidad de ventas
 */
fun calculateBarColor(sales: Double, maxSales: Double, primaryColor: androidx.compose.ui.graphics.Color): androidx.compose.ui.graphics.Color {
    val intensity = if (maxSales > 0) sales / maxSales else 0.0
    
    return when {
        intensity > 0.8 -> primaryColor
        intensity > 0.6 -> androidx.compose.ui.graphics.Color(0xFF1976D2)
        intensity > 0.4 -> androidx.compose.ui.graphics.Color(0xFF42A5F5)
        intensity > 0.2 -> androidx.compose.ui.graphics.Color(0xFF81C784)
        intensity > 0.0 -> androidx.compose.ui.graphics.Color(0xFFFFB74D)
        else -> androidx.compose.ui.graphics.Color(0xFFE0E0E0)
    }
}

/**
 * Determina el período del día basado en la hora
 */
fun getTimeOfDay(hour: String): String {
    val hourInt = hour.substring(0, 2).toIntOrNull() ?: 0
    return when (hourInt) {
        in 6..11 -> "Mañana"
        in 12..17 -> "Tarde"
        in 18..23 -> "Noche"
        else -> "Madrugada"
    }
}

/**
 * Calcula métricas de rendimiento por hora
 */
fun calculateHourlyPerformance(hourlySales: List<HourlySalesData>): Map<String, Double> {
    val analysis = analyzeHourlySales(hourlySales)
    
    return mapOf(
        "eficiencia" to if (analysis.maxSales > 0) (analysis.averageSales / analysis.maxSales) else 0.0,
        "actividad" to (analysis.activeHours / 24.0),
        "consistencia" to calculateConsistency(hourlySales),
        "crecimiento" to calculateGrowth(hourlySales)
    )
}

private fun calculateConsistency(hourlySales: List<HourlySalesData>): Double {
    val sales = hourlySales.map { it.sales }
    val mean = sales.average()
    val variance = sales.map { (it - mean) * (it - mean) }.average()
    val stdDev = kotlin.math.sqrt(variance)
    return if (mean > 0) 1.0 - (stdDev / mean) else 0.0
}

private fun calculateGrowth(hourlySales: List<HourlySalesData>): Double {
    if (hourlySales.size < 2) return 0.0
    
    val firstHalf = hourlySales.take(12).sumOf { it.sales }
    val secondHalf = hourlySales.drop(12).sumOf { it.sales }
    
    return if (firstHalf > 0) (secondHalf - firstHalf) / firstHalf else 0.0
}
