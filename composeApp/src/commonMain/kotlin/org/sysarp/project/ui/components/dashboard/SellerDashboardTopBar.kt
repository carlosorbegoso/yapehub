package org.sysarp.project.ui.components.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sysarp.project.service.auth.AuthService

/**
 * Barra superior del dashboard del vendedor con notificaciones y acciones
 */
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
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            // Indicador de nuevos pagos
            if (newPaymentsCount > 0) {
                Box {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notificaciones"
                        )
                    }
                    Badge(
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = newPaymentsCount.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            IconButton(onClick = onNavigateToAnalytics) {
                Icon(
                    imageVector = Icons.Filled.Analytics,
                    contentDescription = "Analytics"
                )
            }
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Configuración"
                )
            }
            IconButton(onClick = {
                coroutineScope.launch {
                    authService.logout()
                    onLogout()
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Cerrar Sesión"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface // Mismo estilo que admin
        ),
        modifier = modifier.padding(horizontal = 2.dp) // Ultra compacto
    )
}
