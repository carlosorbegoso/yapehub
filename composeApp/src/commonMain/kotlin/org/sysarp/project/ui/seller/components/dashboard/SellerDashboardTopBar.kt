package org.sysarp.project.ui.components.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sysarp.project.service.auth.AuthService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardTopBar(
    newPaymentsCount: Int,
    authService: AuthService,
    coroutineScope: CoroutineScope,
    onNotificationsClick: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                "Dashboard Vendedor",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        actions = {
            // Notificaciones con badge
            if (newPaymentsCount > 0) {
                Box {
                    val interactionSource = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = onNotificationsClick,
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notificaciones",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                    ) {
                        Text(
                            text = newPaymentsCount.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            } else {
                val interactionSource = remember { MutableInteractionSource() }
                IconButton(
                    onClick = onNotificationsClick,
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notificaciones",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Iconos de acciones
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de Analytics
                val analyticsInteraction = remember { MutableInteractionSource() }
                IconButton(
                    onClick = onNavigateToAnalytics,
                    interactionSource = analyticsInteraction,
                    modifier = Modifier
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Analytics,
                        contentDescription = "Analytics",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Botón de Configuración
                val settingsInteraction = remember { MutableInteractionSource() }
                IconButton(
                    onClick = onNavigateToSettings,
                    interactionSource = settingsInteraction,
                    modifier = Modifier
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Configuración",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Botón de Logout
                val logoutInteraction = remember { MutableInteractionSource() }
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            authService.logout()
                            onLogout()
                        }
                    },
                    interactionSource = logoutInteraction,
                    modifier = Modifier
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Cerrar Sesión",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
            .padding(horizontal = 8.dp)
            .shadow(elevation = 2.dp, shape = RectangleShape)
    )
}