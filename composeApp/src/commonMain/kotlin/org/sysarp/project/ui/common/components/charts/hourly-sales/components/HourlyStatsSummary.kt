package org.sysarp.project.ui.common.components.charts.hourly.sales.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.HourlySalesData
import org.sysarp.project.ui.common.components.charts.daily.sales.components.EnhancedMetricCard
import org.sysarp.project.ui.common.components.charts.hourly.sales.data.analyzeHourlySales
import org.sysarp.project.ui.common.components.charts.hourly.sales.utils.formatHour
import org.sysarp.project.utils.formatCurrency

/**
 * Componente de resumen de estadísticas para ventas por hora
 * Reutiliza EnhancedMetricCard del DailySalesChart
 */
@Composable
fun HourlyStatsSummary(
    hourlySales: List<HourlySalesData>,
    modifier: Modifier = Modifier
) {
    val analysis = analyzeHourlySales(hourlySales)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Hora Pico
        Box(modifier = Modifier.weight(1f)) {
            EnhancedMetricCard(
                icon = "🕐",
                label = "Hora Pico",
                value = analysis.peakHour?.hour?.formatHour() ?: "N/A",
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Promedio
        Box(modifier = Modifier.weight(1f)) {
            EnhancedMetricCard(
                icon = "📊",
                label = "Promedio",
                value = formatCurrency(analysis.averageSales),
                color = MaterialTheme.colorScheme.secondary
            )
        }
        
        // Horas Activas
        Box(modifier = Modifier.weight(1f)) {
            EnhancedMetricCard(
                icon = "⚡",
                label = "Activas",
                value = "${analysis.activeHours}/24",
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
