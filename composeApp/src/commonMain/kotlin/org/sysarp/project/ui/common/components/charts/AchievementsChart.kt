package org.sysarp.project.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sysarp.project.data.BadgeData
import org.sysarp.project.data.MilestoneData
import org.sysarp.project.data.SellerAchievementsData

/**
 * Gráfico para mostrar logros, badges y milestones del vendedor
 * Muestra una vista horizontal de badges ganados y milestones alcanzados
 */
@Composable
fun AchievementsChart(
    sellerAchievements: SellerAchievementsData?,
    modifier: Modifier = Modifier
) {
    if (sellerAchievements == null) {
        EmptyChartCard(
            title = "🏆 Logros y Badges",
            subtitle = "No hay datos de logros disponibles",
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
                title = "🏆 Logros y Badges",
                subtitle = "Tu progreso y reconocimientos"
            )

            // Estadísticas de rachas
            StreakStats(achievements = sellerAchievements)

            // Badges ganados
            if (sellerAchievements.badges.isNotEmpty()) {
                BadgesSection(badges = sellerAchievements.badges)
            }

            // Milestones alcanzados
            if (sellerAchievements.milestones.isNotEmpty()) {
                MilestonesSection(milestones = sellerAchievements.milestones)
            }
        }
    }
}

@Composable
private fun StreakStats(
    achievements: SellerAchievementsData
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StreakItem(
            label = "Racha Actual",
            value = "${achievements.streakDays} días",
            color = Color(0xFF4CAF50),
            icon = "🔥"
        )
        StreakItem(
            label = "Mejor Racha",
            value = "${achievements.bestStreak} días",
            color = Color(0xFF2196F3),
            icon = "⭐"
        )
        StreakItem(
            label = "Total Rachas",
            value = "${achievements.totalStreaks}",
            color = Color(0xFFFF9800),
            icon = "🎯"
        )
    }
}

@Composable
private fun StreakItem(
    label: String,
    value: String,
    color: Color,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    color = color.copy(alpha = 0.15f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 22.sp
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
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun BadgesSection(
    badges: List<BadgeData>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🏅 Badges Ganados",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(badges) { badge ->
                BadgeItem(badge = badge)
            }
        }
    }
}

@Composable
private fun BadgeItem(
    badge: BadgeData
) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(badge) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Box(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp)
            .background(
                color = if (badge.earned) Color(0xFF4CAF50).copy(alpha = 0.05f) else Color.Gray.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Icono con fondo circular suave
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (badge.earned) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge.icon,
                    fontSize = 20.sp
                )
            }
            
            Text(
                text = badge.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (badge.earned) Color(0xFF4CAF50) else Color.Gray,
                fontSize = 11.sp
            )
            
            Text(
                text = badge.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontSize = 9.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun MilestonesSection(
    milestones: List<MilestoneData>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🎯 Milestones Alcanzados",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(milestones) { milestone ->
                MilestoneItem(milestone = milestone)
            }
        }
    }
}

@Composable
private fun MilestoneItem(
    milestone: MilestoneData
) {
    Box(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp)
            .background(
                color = if (milestone.achieved) Color(0xFF2196F3).copy(alpha = 0.05f) else Color.Gray.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Icono con fondo circular suave
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = if (milestone.achieved) Color(0xFF2196F3).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (milestone.achieved) "✅" else "⏳",
                    fontSize = 16.sp
                )
            }
            
            Text(
                text = milestone.type.replace("_", " ").uppercase(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (milestone.achieved) Color(0xFF2196F3) else Color.Gray,
                fontSize = 10.sp,
                maxLines = 2
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
