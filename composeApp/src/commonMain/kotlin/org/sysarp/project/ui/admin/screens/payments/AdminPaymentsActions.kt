package org.sysarp.project.ui.admin.screens.payments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.sysarp.project.data.UserProfile

/**
 * Acciones y handlers para AdminPaymentsScreen
 */

@Composable
fun AdminPaymentsActions(
    state: AdminPaymentsState,
    userProfile: UserProfile?,
    accessToken: String?,
    onNavigateBack: () -> Unit
) {
    // Actualizar el estado con los valores del usuario
    LaunchedEffect(userProfile, accessToken) {
        state.updateUserProfile(userProfile)
        state.updateAccessToken(accessToken)
        
        if (state.canLoadPayments()) {
            state.loadAdminPayments(
                onSuccess = { },
                onFailure = { }
            )
        }
    }
}

@Composable
fun AdminPaymentsContentHandler(
    state: AdminPaymentsState
) {
    AdminPaymentsContent(
        state = state,
        onLoadMore = {
            state.loadMorePayments(
                onSuccess = { },
                onFailure = { }
            )
        }
    )
}
