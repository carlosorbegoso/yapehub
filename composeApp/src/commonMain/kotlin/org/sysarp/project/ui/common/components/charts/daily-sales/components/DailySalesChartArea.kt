package org.sysarp.project.ui.common.components.charts.daily.sales.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sysarp.project.data.DailySalesData

/**
 * Área principal del gráfico de ventas diarias
 */
@Composable
fun DailySalesChartArea(
    dailySales: List<DailySalesData>,
    onDaySelected: (DailySalesData) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Contenedor del gráfico con fondo suave
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            // Gráfico de barras
            DailySalesBarsChart(
                dailySales = dailySales,
                onDaySelected = onDaySelected
            )
            
            // Líneas de referencia
            DailySalesReferenceLines(
                dailySales = dailySales
            )
        }
        
        // Leyenda de colores y símbolos
        DailySalesChartLegend(
            modifier = Modifier.padding(top = 8.dp)
        )
        
        // Información adicional del gráfico
        DailySalesChartInfo(
            dailySales = dailySales
        )
    }
}

@Composable
private fun DailySalesChartInfo(
    dailySales: List<DailySalesData>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "💡 Toca una barra para ver detalles",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        
        Text(
            text = "📊 ${dailySales.size} días",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}
