package org.sysarp.project.ui.components.charts

/**
 * Archivo de índice para todos los componentes de gráficos reutilizables
 * 
 * Este archivo proporciona un punto de acceso centralizado para todos los
 * componentes de gráficos disponibles en la aplicación.
 * 
 * Componentes disponibles:
 * 
 * Gráficos Básicos:
 * - DailySalesBarChart: Gráfico de barras para ventas diarias
 * - PerformanceMetricsPieChart: Gráfico circular para métricas de rendimiento
 * - SalesTrendLineChart: Gráfico de líneas para tendencias de ventas
 * 
 * Gráficos Avanzados:
 * (Los gráficos avanzados están disponibles como plantillas para futuras implementaciones)
 * 
 * Selectores de Fecha:
 * - DateRangeSelector: Selector de rango de fechas con calendario
 * - SpecificDateSelector: Selector de día específico con calendario
 * - AdvancedDateSelector: Selector avanzado unificado de fechas
 * 
 * Uso:
 * ```kotlin
 * // Gráficos básicos
 * import org.sysarp.project.ui.components.charts.DailySalesBarChart
 * import org.sysarp.project.ui.components.charts.PerformanceMetricsPieChart
 * import org.sysarp.project.ui.components.charts.SalesTrendLineChart
 * 
 * // Gráficos avanzados implementados
 * import org.sysarp.project.ui.components.charts.HourlySalesChart
 * import org.sysarp.project.ui.components.charts.GoalsProgressChart
 * import org.sysarp.project.ui.components.charts.AchievementsChart
 * import org.sysarp.project.ui.components.charts.PredictionsChart
 * import org.sysarp.project.ui.components.charts.SalesDistributionChart
 * import org.sysarp.project.ui.components.charts.ComparisonsChart
 * import org.sysarp.project.ui.components.charts.CombinedMetricsChart
 * 
 * // Gráficos avanzados adicionales (disponibles como plantillas)
 * // import org.sysarp.project.ui.components.charts.PerformanceMetricsChart
 * // import org.sysarp.project.ui.components.charts.TemporalComparisonChart
 * 
 * // Selectores de fecha
 * import org.sysarp.project.ui.components.charts.DateRangeSelector
 * import org.sysarp.project.ui.components.charts.SpecificDateSelector
 * import org.sysarp.project.ui.components.charts.AdvancedDateSelector
 * ```
 */

// Los componentes se importan automáticamente desde sus archivos individuales
// No es necesario re-exportarlos aquí ya que Kotlin permite importar directamente
