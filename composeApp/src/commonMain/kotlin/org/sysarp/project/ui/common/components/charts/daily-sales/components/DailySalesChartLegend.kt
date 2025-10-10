package org.sysarp.project.ui.common.components.charts.daily.sales.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Leyenda para el gráfico de ventas diarias
 */
@Composable
fun DailySalesChartLegend(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Título de la leyenda
        Text(
            text = "📊 Leyenda del Gráfico",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
        
        // Colores de rendimiento
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(
                color = MaterialTheme.colorScheme.primary,
                label = "Excelente",
                icon = "🔥"
            )
            LegendItem(
                color = MaterialTheme.colorScheme.secondary,
                label = "Bueno",
                icon = "📈"
            )
            LegendItem(
                color = MaterialTheme.colorScheme.tertiary,
                label = "Regular",
                icon = "📊"
            )
            LegendItem(
                color = MaterialTheme.colorScheme.error,
                label = "Bajo",
                icon = "📉"
            )
        }
        
        // Símbolos explicativos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SymbolItem(
                symbol = "🔥",
                description = "Top 10%"
            )
            SymbolItem(
                symbol = "📈",
                description = "Sobre promedio"
            )
            SymbolItem(
                symbol = "📊",
                description = "Promedio"
            )
            SymbolItem(
                symbol = "📉",
                description = "Bajo promedio"
            )
        }
    }
}

/**
 * Item individual de la leyenda de colores
 */
@Composable
private fun LegendItem(
    color: Color,
    label: String,
    icon: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = color.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(2.dp)
                )
        )
        Text(
            text = icon,
            fontSize = 10.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp
        )
    }
}

/**
 * Item individual de la leyenda de símbolos
 */
@Composable
private fun SymbolItem(
    symbol: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = symbol,
            fontSize = 12.sp
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp
        )
    }
}
