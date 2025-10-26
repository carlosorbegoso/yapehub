package org.sysarp.project.ui.common.components.charts.daily.sales.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Card mejorado para métricas con gradiente suave y sin bordes gruesos
 * 
 * @param icon Emoji o icono para la métrica
 * @param label Etiqueta descriptiva
 * @param value Valor principal de la métrica
 * @param color Color del tema para la métrica
 */
@Composable
fun EnhancedMetricCard(
    icon: String,
    label: String,
    value: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.08f),
                        color.copy(alpha = 0.03f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icono con fondo circular suave
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 18.sp
                )
            }
            
            // Valor principal
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            // Etiqueta
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Card de análisis con diseño mejorado (sin bordes gruesos)
 * Muestra información sobre días destacados (mejor día, día bajo)
 * 
 * @param icon Emoji representativo
 * @param label Tipo de análisis (ej: "Mejor Día")
 * @param value Valor principal (ej: nombre del día)
 * @param detail Detalle adicional (ej: monto de ventas)
 * @param color Color del tema
 */
@Composable
fun AnalysisCard(
    icon: String,
    label: String,
    value: String,
    detail: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.08f),
                        color.copy(alpha = 0.03f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Icono con fondo circular suave
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 14.sp
                )
            }
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}
