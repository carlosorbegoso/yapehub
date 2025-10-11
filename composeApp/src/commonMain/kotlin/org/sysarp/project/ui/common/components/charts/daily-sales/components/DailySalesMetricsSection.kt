package org.sysarp.project.ui.common.components.charts.daily.sales.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.DailySalesData
import org.sysarp.project.ui.common.components.charts.daily.sales.data.SalesAnalysis
import org.sysarp.project.utils.formatCurrency

/**
 * Sección de métricas principales del gráfico de ventas diarias
 */
@Composable
fun DailySalesMetricsSection(
    dailySales: List<DailySalesData>,
    analysis: SalesAnalysis,
    periodLabel: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título principal con indicador de tendencia
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Total $periodLabel: ${formatCurrency(dailySales.sumOf { it.sales })}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            TrendIndicator(
                trend = analysis.trend,
                percentage = analysis.trendPercentage
            )
        }
        
        // Métricas principales mejoradas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EnhancedMetricCard(
                icon = "📊",
                label = "Promedio Diario",
                value = formatCurrency(dailySales.map { it.sales }.average()),
                color = MaterialTheme.colorScheme.secondary
            )
            
            EnhancedMetricCard(
                icon = "🔄",
                label = "Total Transacciones",
                value = "${dailySales.sumOf { it.transactions }}",
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        
        // Análisis inteligente de días destacados
        if (analysis.peakDay != null || analysis.valleyDay != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (analysis.peakDay != null) {
                    AnalysisCard(
                        icon = "📈",
                        label = "Mejor Día",
                        value = analysis.peakDay.dayName,
                        detail = formatCurrency(analysis.peakDay.sales),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (analysis.valleyDay != null) {
                    AnalysisCard(
                        icon = "📉",
                        label = "Día Bajo",
                        value = analysis.valleyDay.dayName,
                        detail = formatCurrency(analysis.valleyDay.sales),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        // Información adicional para períodos largos
        if (dailySales.size > 7) {
            Text(
                text = "Período: ${dailySales.size} días",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
