package org.sysarp.project.ui.common.components.charts.daily.sales.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sysarp.project.data.DailySalesData
import org.sysarp.project.utils.formatCurrency

/**
 * Tooltip flotante mejorado con animaciones y diseño elegante
 * Aparece cuando el usuario pasa el mouse sobre una barra del gráfico
 * 
 * @param dayData Datos del día seleccionado
 * @param trendDirection Dirección de la tendencia: "up", "down", "stable"
 * @param modifier Modificador para posicionamiento
 */
@Composable
fun FloatingTooltip(
    dayData: DailySalesData,
    trendDirection: String,
    modifier: Modifier = Modifier
) {
    val scaleAnimation = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        scaleAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(200)
        )
    }
    
    Card(
        modifier = modifier.scale(scaleAnimation.value),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header con icono del día
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📅",
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = dayData.dayName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Monto de ventas destacado
            Text(
                text = formatCurrency(dayData.sales),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Información adicional en fila
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Transacciones
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "🔄",
                        fontSize = 10.sp
                    )
                    Text(
                        text = "${dayData.transactions}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Indicador de tendencia mejorado
                val (trendIcon, trendColor) = when (trendDirection) {
                    "up" -> "↗️" to MaterialTheme.colorScheme.primary
                    "down" -> "↘️" to MaterialTheme.colorScheme.error
                    else -> "→" to MaterialTheme.colorScheme.secondary
                }
                
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = trendColor.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = trendIcon,
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}
