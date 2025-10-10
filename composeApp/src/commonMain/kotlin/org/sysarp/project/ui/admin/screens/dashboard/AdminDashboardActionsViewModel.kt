package org.sysarp.project.ui.admin.screens.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sysarp.project.data.UserProfile
import org.sysarp.project.viewmodel.admin.AdminDashboardViewModel

/**
 * Acciones y handlers para AdminDashboardScreen usando ViewModel
 */
@Composable
fun AdminDashboardActions(
    viewModel: AdminDashboardViewModel,
    userProfile: UserProfile?,
    accessToken: String?,
    onLogout: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Manejo de sesión y token refresh
    LaunchedEffect(Unit) {
        // Verificar sesión válida
        val isValid = viewModel.isSessionValid()
        if (!isValid) {
            viewModel.logout()
            onLogout()
        }
    }
    
    // Refresh automático de token
    LaunchedEffect(Unit) {
        while (true) {
            delay(120_000) // 2 minutos
            try {
                val isValid = viewModel.isSessionValid()
                if (!isValid) {
                    viewModel.logout()
                    onLogout()
                    break
                }
            } catch (e: Exception) {
                // Error silencioso en refresh
            }
        }
    }
    
    // Cargar datos cuando cambie el perfil o token
    LaunchedEffect(userProfile?.adminId, accessToken) {
        if (userProfile?.adminId != null && accessToken != null) {
            viewModel.refreshDashboard()
        }
    }
    
    // Limpiar errores cuando cambien
    LaunchedEffect(userProfile) {
        viewModel.clearError()
    }
}

/**
 * Crea el menú de la top bar
 */
@Composable
fun createTopBarMenuItems(
    onShowAffiliationDialog: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
): List<org.sysarp.project.ui.common.components.topbar.TopBarMenuItem> {
    return listOf(
        org.sysarp.project.ui.common.components.topbar.TopBarMenuItem(
            title = "Generar código de afiliación",
            icon = Icons.Filled.QrCode,
            onClick = onShowAffiliationDialog
        ),
        org.sysarp.project.ui.common.components.topbar.TopBarMenuItem(
            title = "Mi Perfil",
            icon = Icons.Filled.Person,
            onClick = onNavigateToProfile
        ),
        org.sysarp.project.ui.common.components.topbar.TopBarMenuItem(
            title = "Configuración",
            icon = Icons.Filled.Settings,
            onClick = onNavigateToSettings
        ),
        org.sysarp.project.ui.common.components.topbar.TopBarMenuItem(
            title = "Cerrar sesión",
            icon = Icons.Filled.Logout,
            onClick = onLogout,
            iconColor = MaterialTheme.colorScheme.error
        )
    )
}

/**
 * Handler para logout usando ViewModel
 */
fun handleLogout(
    viewModel: AdminDashboardViewModel,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onLogout: () -> Unit
) {
    coroutineScope.launch {
        viewModel.logout()
        onLogout()
    }
}
