package org.sysarp.project.ui.admin.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.sysarp.project.data.QRCodeData
import org.sysarp.project.service.auth.AuthService

/**
 * Acciones y handlers para AdminProfileScreen
 */

@Composable
fun AdminProfileActions(
    state: AdminProfileState,
    authService: AuthService,
    onNavigateToQR: (QRCodeData) -> Unit,
    onEditProfile: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveProfile: () -> Unit
) {
    // Sincronizar con el servicio de autenticación
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()
    
    // Actualizar el estado con los datos de autenticación
    LaunchedEffect(userProfile, accessToken) {
        state.updateUserProfile(userProfile)
        state.updateAccessToken(accessToken)
        
        // Cargar perfil cuando tengamos los datos necesarios
        if (state.canLoadProfile()) {
            state.loadProfile(
                onSuccess = { },
                onFailure = { }
            )
        }
    }
    
    // Manejar acciones de la pantalla
    LaunchedEffect(Unit) {
        // Configurar callbacks para las acciones
        // Las acciones específicas se manejan en los componentes individuales
    }
}
