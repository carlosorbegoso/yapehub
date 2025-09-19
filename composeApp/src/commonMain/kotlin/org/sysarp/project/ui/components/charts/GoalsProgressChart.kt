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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sysarp.project.data.SellerGoalsData
import org.sysarp.project.utils.formatCurrency
import org.sysarp.project.utils.formatPercentage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Gráfico circular para mostrar el progreso de objetivos del vendedor
 * Muestra objetivos diarios, semanales, mensuales y anuales con progreso circular
 */
@Composable
fun GoalsProgressChart(
    sellerGoals: SellerGoalsData?,
    modifier: Modifier = Modifier
) {
    if (sellerGoals == null) {
        EmptyChartCard(
            title = "🎯 Progreso de Objetivos",
            subtitle = "No hay datos de objetivos disponibles",
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
                title = "🎯 Progreso de Objetivos",
                subtitle = "Tu progreso hacia las metas establecidas"
            )

            // Gráficos circulares de progreso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CircularProgressItem(
                    label = "Diario",
                    target = sellerGoals.dailyTarget,
                    progress = sellerGoals.dailyProgress,
                    color = Color(0xFF4CAF50)
                )
                CircularProgressItem(
                    label = "Semanal",
                    target = sellerGoals.weeklyTarget,
                    progress = sellerGoals.weeklyProgress,
                    color = Color(0xFF2196F3)
                )
                CircularProgressItem(
                    label = "Mensual",
                    target = sellerGoals.monthlyTarget,
                    progress = sellerGoals.monthlyProgress,
                    color = Color(0xFFFF9800)
                )
            }

            // Resumen de logros
            GoalsSummary(sellerGoals = sellerGoals)
        }
    }
}

@Composable
private fun CircularProgressItem(
    label: String,
    target: Double,
    progress: Double,
    color: Color
) {
    val animationProgress = remember { Animatable(0f) }
    val progressPercentage = (progress * 100).coerceIn(0.0, 100.0)

    LaunchedEffect(progress) {
        animationProgress.animateTo(
            targetValue = progressPercentage.toFloat() / 100f,
            animationSpec = tween(durationMillis = 1500)
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawCircularProgress(
                    progress = animationProgress.value,
                    color = color,
                    size = size
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${(progressPercentage * animationProgress.value).toInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = formatCurrency(target),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 8.sp
                )
            }
        }
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontSize = 10.sp
        )
    }
}

private fun DrawScope.drawCircularProgress(
    progress: Float,
    color: Color,
    size: Size
) {
    val strokeWidth = 8.dp.toPx()
    val radius = (size.minDimension - strokeWidth) / 2
    val center = Offset(size.width / 2, size.height / 2)
    
    // Fondo del círculo
    drawCircle(
        color = Color.Gray.copy(alpha = 0.2f),
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
    
    // Progreso
    val sweepAngle = progress * 360f
    drawArc(
        color = color,
        startAngle = -90f,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}

@Composable
private fun GoalsSummary(
    sellerGoals: SellerGoalsData
) {
    val achievementRate = sellerGoals.achievementRate * 100
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        GoalStatItem(
            label = "Tasa de Logro",
            value = formatPercentage(achievementRate),
            color = if (achievementRate >= 50) Color(0xFF4CAF50) else Color(0xFFFF9800),
            icon = if (achievementRate >= 50) "🎯" else "📈"
        )
        GoalStatItem(
            label = "Objetivo Anual",
            value = formatCurrency(sellerGoals.yearlyTarget),
            color = Color(0xFF2196F3),
            icon = "📅"
        )
    }
}

@Composable
private fun GoalStatItem(
    label: String,
    value: String,
    color: Color,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                text = icon,
                fontSize = 20.sp
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
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