package org.sysarp.project.ui.common.components.cards

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingUp
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

// ===== Constantes de Diseño =====

private const val CARD_SHAPE_RADIUS = 20
private const val ICON_BOX_SIZE = 56
private const val ICON_SIZE = 28
private const val PADDING_LARGE = 24
private const val PADDING_MEDIUM = 16
private const val PADDING_SMALL = 4
private const val ICON_ALPHA = 0.1f

private val CARD_ELEVATION_DEFAULT = 6.dp
private val CARD_ELEVATION_PRESSED = 12.dp
private val ANIMATION_DURATION = 300

/**
 * Tarjeta de estadísticas moderna con animaciones
 * Responsabilidades:
 * - Mostrar estadísticas con icono y valor
 * - Animar cambios de color
 * - Proporcionar diseño consistente
 */
@Composable
fun ModernStatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    subtitle: String? = null,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    modifier: Modifier = Modifier
) {
    val animatedIconColor by animateColorAsState(
        targetValue = iconColor,
        animationSpec = tween(ANIMATION_DURATION),
        label = "iconColor"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CARD_SHAPE_RADIUS.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = CARD_ELEVATION_DEFAULT,
            pressedElevation = CARD_ELEVATION_PRESSED
        ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(PADDING_LARGE.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            renderIconBox(animatedIconColor)
            Spacer(modifier = Modifier.width(PADDING_MEDIUM.dp))
            renderStatsContent(title, value, subtitle)
        }
    }
}

/**
 * Tarjeta de estadísticas de pagos con información específica
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

// ===== Componentes Privados =====

/**
 * Renderiza el box circular con icono
 */
@Composable
private fun renderIconBox(iconColor: Color) {
    Box(
        modifier = Modifier
            .size(ICON_BOX_SIZE.dp)
            .clip(CircleShape)
            .background(iconColor.copy(alpha = ICON_ALPHA)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(ICON_SIZE.dp)
        )
    }
}

/**
 * Renderiza el contenido de estadísticas (título, valor, subtítulo)
 */
@Composable
private fun renderStatsContent(
    title: String,
    value: String,
    subtitle: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        renderStatsTitle(title)
        Spacer(modifier = Modifier.height(PADDING_SMALL.dp))
        renderStatsValue(value)

        subtitle?.let {
            Spacer(modifier = Modifier.height(2.dp))
            renderStatsSubtitle(it)
        }
    }
}

/**
 * Renderiza el título de la estadística
 */
@Composable
private fun renderStatsTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium
    )
}

/**
 * Renderiza el valor principal de la estadística
 */
@Composable
private fun renderStatsValue(value: String) {
    Text(
        text = value,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * Renderiza el subtítulo de la estadística
 */
@Composable
private fun renderStatsSubtitle(subtitle: String) {
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}