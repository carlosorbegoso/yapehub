package org.sysarp.project.ui.admin.screens.management.branch

import androidx.compose.runtime.Composable
import org.sysarp.project.ui.common.components.topbar.TopBarComponent

/**
 * Header de la pantalla de gestión de sucursales
 * Refactorizado para ser más modular y mantenible
 */
@Composable
fun BranchManagementHeader(
    onNavigateBack: (() -> Unit)? = null
) {
    TopBarComponent(
        title = "🏢 Gestión de Sucursales",
        subtitle = "Administra las sucursales de tu negocio",
        onNavigateBack = onNavigateBack
    )
}
