package org.sysarp.project.ui.common.components.charts.daily.sales.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.DailySalesData

/**
 * Líneas de referencia para el gráfico de ventas diarias
 */
@Composable
fun DailySalesReferenceLines(
    dailySales: List<DailySalesData>
) {
    val maxValue = dailySales.maxOfOrNull { it.sales } ?: 1.0
    val minValue = dailySales.minOfOrNull { it.sales } ?: 0.0
    val valueRange = maxValue - minValue
    val averageSales = dailySales.map { it.sales }.average()
    val maxSales = dailySales.maxOfOrNull { it.sales } ?: 0.0
    
    val averageHeight = if (valueRange > 0) {
        ((averageSales - minValue) / valueRange * 0.9).coerceAtLeast(0.1)
    } else 0.15
    
    val maxHeight = if (valueRange > 0) {
        ((maxSales - minValue) / valueRange * 0.9).coerceAtLeast(0.1)
    } else 0.15
    
    // Línea de promedio
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                shape = RoundedCornerShape(0.5.dp)
            )
            .offset(y = (-averageHeight * 180).dp)
    )
    
    // Línea de máximo (solo si es significativamente diferente del promedio)
    if (maxSales > averageSales * 1.5) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(0.5.dp)
                )
                .offset(y = (-maxHeight * 180).dp)
        )
    }
    
    // Línea base
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            .offset(y = (-20).dp)
    )
}
