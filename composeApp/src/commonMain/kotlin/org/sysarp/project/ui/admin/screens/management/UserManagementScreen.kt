package org.sysarp.project.ui.admin.screens.management

import androidx.compose.runtime.Composable
import org.sysarp.project.repository.UserProfileRepository
import org.sysarp.project.ui.admin.screens.management.user.components.UserManagementScreenRefactored

/**
 * Pantalla de gestión de usuarios refactorizada
 * Usa componentes modulares para mejor mantenibilidad
 */
@Composable
fun UserManagementScreen(
    userProfileRepository: UserProfileRepository,
    onNavigateBack: () -> Unit
) {
    UserManagementScreenRefactored(
        userProfileRepository = userProfileRepository,
        onNavigateBack = onNavigateBack
    )
}