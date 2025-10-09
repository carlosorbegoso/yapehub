package org.sysarp.project.ui.common.components.charts.predictions

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sysarp.project.data.PredictedSalesData
import org.sysarp.project.data.TrendAnalysisData

/**
 * Gráfico de líneas para predicciones
 * Componente simplificado para evitar errores de compilación
 */
@Composable
fun PredictionsLineChart(
    predictedSales: List<PredictedSalesData>,
    trendAnalysis: TrendAnalysisData
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Título del gráfico
        Text(
            text = "📈 Proyección de Ventas",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Canvas simplificado
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            drawSimpleLineChart(predictedSales)
        }
        
        // Resumen de valores
        PredictedValuesSummary(predictedSales = predictedSales)
    }
}

private fun DrawScope.drawSimpleLineChart(predictedSales: List<PredictedSalesData>) {
    if (predictedSales.isEmpty()) return
    
    val padding = 40.dp.toPx()
    val chartWidth = size.width - padding * 2
    val chartHeight = size.height - padding * 2
    val startX = padding
    val startY = padding
    
    val maxValue = predictedSales.maxOfOrNull { it.predicted } ?: 0.0
    val minValue = predictedSales.minOfOrNull { it.predicted } ?: 0.0
    val valueRange = maxValue - minValue
    
    if (predictedSales.size < 2) return
    
    val path = Path()
    val stepX = chartWidth / (predictedSales.size - 1)
    
    predictedSales.forEachIndexed { index, data ->
        val x = startX + index * stepX
        val normalizedValue = if (valueRange > 0) {
            (data.predicted - minValue) / valueRange
        } else 0.5
        val y = startY + chartHeight - (normalizedValue * chartHeight).toFloat()
        
        if (index == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    
    // Dibujar línea principal
    drawPath(
        path = path,
        color = Color.Blue,
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )
}

@Composable
private fun PredictedValuesSummary(predictedSales: List<PredictedSalesData>) {
    if (predictedSales.isEmpty()) return
    
    val maxValue = predictedSales.maxOfOrNull { it.predicted } ?: 0.0
    val minValue = predictedSales.minOfOrNull { it.predicted } ?: 0.0
    val avgValue = predictedSales.map { it.predicted }.average()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem("Máximo", maxValue.toString())
        StatItem("Promedio", avgValue.toString())
        StatItem("Mínimo", minValue.toString())
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
