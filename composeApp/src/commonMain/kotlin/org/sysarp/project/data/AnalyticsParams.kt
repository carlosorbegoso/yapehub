package org.sysarp.project.data

/**
 * Parámetros específicos para Analytics APIs
 * Proporciona tipos seguros y valores predefinidos para los parámetros de analytics
 */

// Tipos de inclusión de datos
enum class AnalyticsInclude(val value: String) {
    TRENDS("trends"),
    FORECAST("forecast"),
    TRENDS_AND_FORECAST("trends,forecast"),
    ALL("all")
}

// Períodos de análisis
enum class AnalyticsPeriod(val value: String) {
    DAILY("daily"),
    WEEKLY("weekly"),
    MONTHLY("monthly"),
    YEARLY("yearly")
}

// Granularidad de datos
enum class AnalyticsGranularity(val value: String) {
    DAILY("daily"),
    HOURLY("hourly"),
    WEEKLY("weekly"),
    MONTHLY("monthly")
}

// Métricas específicas
enum class AnalyticsMetric(val value: String) {
    SALES("sales"),
    TRANSACTIONS("transactions"),
    PERFORMANCE("performance"),
    ALL("all")
}

// Niveles de confianza para predicciones
enum class AnalyticsConfidence(val value: Double) {
    LOW(0.90),
    MEDIUM(0.95),
    HIGH(0.99)
}

// Configuración predefinida para diferentes tipos de análisis
object AnalyticsConfigs {
    
    // Configuración para análisis rápido (dashboard)
    val QUICK_ANALYSIS = AnalyticsConfig(
        include = AnalyticsInclude.TRENDS,
        period = AnalyticsPeriod.WEEKLY,
        metric = AnalyticsMetric.ALL,
        confidence = AnalyticsConfidence.MEDIUM,
        days = 7
    )
    
    // Configuración para análisis detallado
    val DETAILED_ANALYSIS = AnalyticsConfig(
        include = AnalyticsInclude.TRENDS_AND_FORECAST,
        period = AnalyticsPeriod.MONTHLY,
        metric = AnalyticsMetric.ALL,
        confidence = AnalyticsConfidence.HIGH,
        days = 30
    )
    
    // Configuración para análisis de ventas
    val SALES_ANALYSIS = AnalyticsConfig(
        include = AnalyticsInclude.TRENDS_AND_FORECAST,
        period = AnalyticsPeriod.MONTHLY,
        metric = AnalyticsMetric.SALES,
        confidence = AnalyticsConfidence.MEDIUM,
        days = 30
    )
    
    // Configuración para análisis de rendimiento
    val PERFORMANCE_ANALYSIS = AnalyticsConfig(
        include = AnalyticsInclude.TRENDS,
        period = AnalyticsPeriod.WEEKLY,
        metric = AnalyticsMetric.PERFORMANCE,
        confidence = AnalyticsConfidence.MEDIUM,
        days = 7
    )
    
    // Configuración para predicciones a corto plazo
    val SHORT_TERM_FORECAST = AnalyticsConfig(
        include = AnalyticsInclude.FORECAST,
        period = AnalyticsPeriod.DAILY,
        metric = AnalyticsMetric.ALL,
        confidence = AnalyticsConfidence.LOW,
        days = 7
    )
    
    // Configuración para predicciones a largo plazo
    val LONG_TERM_FORECAST = AnalyticsConfig(
        include = AnalyticsInclude.FORECAST,
        period = AnalyticsPeriod.MONTHLY,
        metric = AnalyticsMetric.ALL,
        confidence = AnalyticsConfidence.HIGH,
        days = 90
    )
    
    // Configuración específica para el endpoint actual
    val SPECIFIC_INCLUDE = AnalyticsConfig(
        include = AnalyticsInclude.TRENDS, // Para dailySales,hourlySales,sellerGoals
        period = AnalyticsPeriod.WEEKLY,
        metric = AnalyticsMetric.SALES,
        confidence = AnalyticsConfidence.MEDIUM,
        days = 62,
        granularity = AnalyticsGranularity.DAILY
    )
}

// Clase de configuración para analytics
data class AnalyticsConfig(
    val include: AnalyticsInclude,
    val period: AnalyticsPeriod,
    val metric: AnalyticsMetric,
    val confidence: AnalyticsConfidence,
    val days: Int,
    val granularity: AnalyticsGranularity = AnalyticsGranularity.DAILY
) {
    fun toParams(): AnalyticsParams {
        return AnalyticsParams(
            include = include.value,
            period = period.value,
            metric = metric.value,
            confidence = confidence.value,
            days = days,
            granularity = granularity.value
        )
    }
}

// Clase para parámetros de analytics
data class AnalyticsParams(
    val include: String? = null,
    val period: String? = null,
    val metric: String? = null,
    val confidence: Double? = null,
    val days: Int? = null,
    val granularity: String? = null // "daily", "hourly", "weekly", "monthly"
)
