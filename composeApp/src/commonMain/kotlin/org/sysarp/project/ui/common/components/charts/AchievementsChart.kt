package org.sysarp.project.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.graphicsLayer
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
    showCard: Boolean = true,
    modifier: Modifier = Modifier
) {
    val chartContent = @Composable {
        if (sellerAchievements == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (showCard) 16.dp else 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "No hay datos de logros disponibles",
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
private fun StreakStats(
    achievements: SellerAchievementsData
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Primera fila: Racha Actual y Mejor Racha
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StreakItem(
                label = "Racha Actual",
                value = "${achievements.streakDays} días",
                color = MaterialTheme.colorScheme.primary,
                icon = "🔥",
                animationDelay = 0
            )
            StreakItem(
                label = "Mejor Racha",
                value = "${achievements.bestStreak} días",
                color = MaterialTheme.colorScheme.secondary,
                icon = "⭐",
                animationDelay = 200
            )
        }
        
        // Segunda fila: Total Rachas (centrado)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            StreakItem(
                label = "Total Rachas",
                value = "${achievements.totalStreaks}",
                color = MaterialTheme.colorScheme.tertiary,
                icon = "🎯",
                animationDelay = 400
            )
        }
    }
}

@Composable
private fun StreakItem(
    label: String,
    value: String,
    color: Color,
    icon: String,
    animationDelay: Int = 0
) {
    val scaleAnimation = remember { Animatable(0.8f) }
    
    LaunchedEffect(Unit) {
        scaleAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, delayMillis = animationDelay)
        )
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .graphicsLayer {
                    scaleX = scaleAnimation.value
                    scaleY = scaleAnimation.value
                }
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
                color = if (badge.earned) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.05f),
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
                        color = if (badge.earned) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
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
                color = if (badge.earned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
            color = MaterialTheme.colorScheme.onSurface
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            itemsIndexed(milestones) { index, milestone ->
                MilestoneItem(
                    milestone = milestone,
                    animationDelay = index * 150
                )
            }
        }
    }
}

@Composable
private fun MilestoneItem(
    milestone: MilestoneData,
    animationDelay: Int = 0
) {
    val scaleAnimation = remember { Animatable(0.8f) }
    val alphaAnimation = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        scaleAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, delayMillis = animationDelay)
        )
        alphaAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, delayMillis = animationDelay + 100)
        )
    }
    Box(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp)
            .graphicsLayer {
                scaleX = scaleAnimation.value
                scaleY = scaleAnimation.value
                alpha = alphaAnimation.value
            }
            .background(
                color = if (milestone.achieved) MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.05f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icono con fondo circular suave
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (milestone.achieved) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (milestone.achieved) "✅" else "⏳",
                    fontSize = 18.sp
                )
            }
            
            // Información del milestone
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = milestone.type.replace("_", " ").uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (milestone.achieved) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 2
                )
                
                // Barra de progreso (si no está logrado)
                if (!milestone.achieved) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(2.dp)
                            )
                    ) {
                        // Progreso simulado (puedes usar milestone.progress si existe)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.3f) // 30% de progreso simulado
                                .background(
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

