package org.sysarp.project.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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

/**
 * Gráfico circular para mostrar el progreso de objetivos del vendedor
 * Muestra objetivos diarios, semanales, mensuales y anuales con progreso circular
 */
@Composable
fun GoalsProgressChart(
    sellerGoals: SellerGoalsData?,
    showCard: Boolean = true,
    modifier: Modifier = Modifier
) {
    val chartContent = @Composable {
        if (sellerGoals == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (showCard) 16.dp else 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "No hay datos de objetivos disponibles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (showCard) 16.dp else 0.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

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

    // Renderizar con o sin Card según el parámetro
    if (showCard) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            chartContent()
        }
    } else {
        Box(modifier = modifier.fillMaxWidth()) {
            chartContent()
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
