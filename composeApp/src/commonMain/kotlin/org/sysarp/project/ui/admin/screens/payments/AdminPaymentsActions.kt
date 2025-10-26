package org.sysarp.project.ui.admin.screens.payments

import androidx.compose.runtime.Composable
import org.sysarp.project.data.UserProfile

/**
 * Acciones y handlers para AdminPaymentsScreen
 * Simplificado ya que la inicialización se maneja en AdminPaymentsScreen
 */

@Composable
fun AdminPaymentsActions(
    state: AdminPaymentsState,
    userProfile: UserProfile?,
    accessToken: String?,
    onNavigateBack: () -> Unit
) {
    // La inicialización ahora se maneja en AdminPaymentsScreen
    // Este componente se mantiene para futuras acciones específicas
}
