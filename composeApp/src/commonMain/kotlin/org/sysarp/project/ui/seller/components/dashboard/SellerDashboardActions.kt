package org.sysarp.project.ui.seller.components.dashboard

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val CARD_PADDING = 20
private const val CARD_CORNER_RADIUS = 12
private const val ICON_SIZE = 32
private const val ARROW_SIZE = 20
private const val SPACER_WIDTH = 16
private const val COLUMN_SPACING = 12
private const val HORIZONTAL_PADDING = 16

/**
 * Botones de acción profesionales del dashboard del vendedor
 */
@Composable
fun SellerDashboardActions(
    onNavigateToAnalytics: () -> Unit,
    onNavigateToPendingPayments: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDeactivationRequest: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = HORIZONTAL_PADDING.dp),
        verticalArrangement = Arrangement.spacedBy(COLUMN_SPACING.dp)
    ) {
        Text(
            text = "Acciones Principales",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 0.dp)
        )

        ActionCard(
            title = "Analytics",
            subtitle = "Ver estadísticas detalladas y métricas de rendimiento",
            icon = Icons.Filled.Analytics,
            onClick = onNavigateToAnalytics
        )

        ActionCard(
            title = "Gestionar Pagos",
            subtitle = "Confirmar y rechazar pagos pendientes",
            icon = Icons.Filled.Payment,
            onClick = onNavigateToPendingPayments
        )

        ActionCard(
            title = "Notificaciones",
            subtitle = "Ver todas las notificaciones recibidas",
            icon = Icons.Filled.Notifications,
            onClick = onNavigateToNotifications
        )

        ActionCard(
            title = "Configuración",
            subtitle = "Ajustes del perfil y sistema",
            icon = Icons.Filled.Settings,
            onClick = onNavigateToSettings
        )

        ActionCard(
            title = "Solicitar Desactivación",
            subtitle = "Dar de baja tu cuenta de vendedor",
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            onClick = onNavigateToDeactivationRequest,
            isDestructive = true
        )
    }
}

/**
 * Tarjeta de acción siguiendo el estilo del dashboard admin
 *
 * @param title Título de la acción
 * @param subtitle Descripción de la acción
 * @param icon Icono a mostrar
 * @param onClick Callback cuando se hace clic
 * @param isDestructive Si es true, se muestra en rojo para acciones peligrosas
 */
@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDestructive) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS.dp),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CARD_PADDING.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(ICON_SIZE.dp)
            )

            Spacer(modifier = Modifier.width(SPACER_WIDTH.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDestructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(ARROW_SIZE.dp)
            )
        }
    }
}