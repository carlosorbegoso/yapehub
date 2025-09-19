package org.sysarp.project.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sysarp.project.data.SalesDistributionData
import org.sysarp.project.utils.formatCurrency
import org.sysarp.project.utils.formatPercentage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Gráfico de distribución de ventas por tiempo del día y día de la semana
 * Muestra patrones de ventas en diferentes períodos
 */
@Composable
fun SalesDistributionChart(
    salesDistribution: SalesDistributionData?,
    modifier: Modifier = Modifier
) {
    if (salesDistribution == null) {
        EmptyChartCard(
            title = "📊 Distribución de Ventas",
            subtitle = "No hay datos de distribución disponibles",
            modifier = modifier
        )
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título y resumen
            ChartHeader(
                title = "📊 Distribución de Ventas",
                subtitle = "Patrones de ventas por tiempo"
            )

            // Gráfico circular de distribución
            DistributionPieChart(salesDistribution = salesDistribution)

            // Estadísticas detalladas
            DistributionStats(salesDistribution = salesDistribution)
        }
    }
}

@Composable
private fun DistributionPieChart(
    salesDistribution: SalesDistributionData
) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(salesDistribution) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200)
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🥧 Distribución por Tiempo del Día",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Canvas(
            modifier = Modifier.size(200.dp)
        ) {
            drawDistributionPie(
                salesDistribution = salesDistribution,
                animationProgress = animationProgress.value,
                size = size
            )
        }
    }
}

private fun DrawScope.drawDistributionPie(
    salesDistribution: SalesDistributionData,
    animationProgress: Float,
    size: Size
) {
    val center = Offset(size.width / 2, size.height / 2)
    val radius = (size.minDimension - 40.dp.toPx()) / 2
    
    // Datos para el gráfico circular
    val data = listOf(
        Triple("Mañana", salesDistribution.morning, Color(0xFF4CAF50)),
        Triple("Tarde", salesDistribution.afternoon, Color(0xFF2196F3)),
        Triple("Noche", salesDistribution.evening, Color(0xFFFF9800))
    )
    
    val total = data.sumOf { it.second }
    if (total <= 0) return
    
    var startAngle = -90f
    
    data.forEach { (label, value, color) ->
        val sweepAngle = (value / total * 360f * animationProgress).toFloat()
        
        if (sweepAngle > 0) {
            // Dibujar arco
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
            
            // Dibujar borde
            drawArc(
                color = Color.White,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 2.dp.toPx())
            )
            
            startAngle += sweepAngle
        }
    }
}

@Composable
private fun DistributionStats(
    salesDistribution: SalesDistributionData
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📈 Estadísticas Detalladas",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        // Distribución por tiempo del día
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DistributionItem(
                label = "Mañana",
                value = formatCurrency(salesDistribution.morning),
                percentage = if (salesDistribution.morning > 0) "🌅" else "—",
                color = Color(0xFF4CAF50)
            )
            DistributionItem(
                label = "Tarde",
                value = formatCurrency(salesDistribution.afternoon),
                percentage = if (salesDistribution.afternoon > 0) "☀️" else "—",
                color = Color(0xFF2196F3)
            )
            DistributionItem(
                label = "Noche",
                value = formatCurrency(salesDistribution.evening),
                percentage = if (salesDistribution.evening > 0) "🌙" else "—",
                color = Color(0xFFFF9800)
            )
        }

        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.Gray.copy(alpha = 0.3f)
        )

        // Distribución por día de la semana
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DistributionItem(
                label = "Días Laborales",
                value = formatCurrency(salesDistribution.weekday),
                percentage = if (salesDistribution.weekday > 0) "📅" else "—",
                color = Color(0xFF9C27B0)
            )
            DistributionItem(
                label = "Fin de Semana",
                value = formatCurrency(salesDistribution.weekend),
                percentage = if (salesDistribution.weekend > 0) "🎉" else "—",
                color = Color(0xFFE91E63)
            )
        }
    }
}

@Composable
private fun DistributionItem(
    label: String,
    value: String,
    percentage: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = percentage,
                fontSize = 20.sp
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ChartHeader(
    title: String,
    subtitle: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
private fun EmptyChartCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}
