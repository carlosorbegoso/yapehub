package org.sysarp.project.ui.common.components.charts.daily.sales.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.ui.common.components.charts.daily.sales.data.TrendType

/**
 * Indicador de tendencia mejorado con icono y porcentaje
 * Muestra la dirección de la tendencia con mejor tipado y validación
 * 
 * @param trend Tipo de tendencia usando enum TrendType
 * @param percentage Porcentaje de cambio en la tendencia
 */
@Composable
fun TrendIndicator(
    trend: TrendType,
    percentage: Double
) {
    val (icon, color) = when (trend) {
        TrendType.UP -> Icons.AutoMirrored.Filled.TrendingUp to MaterialTheme.colorScheme.primary
        TrendType.DOWN -> Icons.AutoMirrored.Filled.TrendingDown to MaterialTheme.colorScheme.error
        TrendType.STABLE -> Icons.Filled.Analytics to MaterialTheme.colorScheme.secondary
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "${percentage.toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
