package org.sysarp.project.ui.common.components.cards

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.utils.formatCurrency

/**
 * Tarjeta de estadísticas moderna con animaciones
 */
@Composable
fun ModernStatsCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300),
        label = "cardScale"
    )
    
    val animatedIconColor by animateColorAsState(
        targetValue = iconColor,
        animationSpec = tween(300),
        label = "iconColor"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 12.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono con fondo circular
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(animatedIconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = animatedIconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Contenido
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                subtitle?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de estadísticas de pagos específica
 */
@Composable
fun PaymentStatsCard(
    title: String,
    count: Int,
    totalAmount: Double,
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    ModernStatsCard(
        title = title,
        value = formatCurrency(totalAmount),
        subtitle = "$count pagos",
        icon = icon,
        iconColor = iconColor,
        modifier = modifier
    )
}

/**
 * Tarjeta de estadísticas con tendencia
 */
@Composable
fun TrendingStatsCard(
    title: String,
    value: String,
    trend: String,
    trendUp: Boolean = true,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val trendColor = if (trendUp) {
        Color(0xFF4CAF50) // Green
    } else {
        Color(0xFFF44336) // Red
    }
    
    val trendIcon = if (trendUp) {
        Icons.Filled.TrendingUp
    } else {
        Icons.Filled.TrendingUp // Puedes cambiar por TrendingDown si está disponible
    }

    ModernStatsCard(
        title = title,
        value = value,
        subtitle = trend,
        icon = icon,
        iconColor = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}
