package org.sysarp.project.ui.admin.screens.profile

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import org.sysarp.project.data.QRCodeData
import org.sysarp.project.service.admin.AdminService
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.http.AdminProfileApiClient

/**
 * Pantalla de perfil de administrador refactorizada
 * Usa componentes modulares para mejor mantenibilidad
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    authService: AuthService,
    onNavigateBack: () -> Unit,
    onNavigateToQR: (QRCodeData) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Crear el servicio de administrador
    val adminService = remember { 
        AdminService(
            AdminProfileApiClient()
        )
    }
    
    // Crear el estado de la pantalla
    val state = remember {
        AdminProfileState(
            adminService = adminService,
            authService = authService,
            coroutineScope = coroutineScope
        )
    }
    
    // Manejar acciones de la pantalla
    AdminProfileActions(
        state = state,
        authService = authService,
        onNavigateToQR = onNavigateToQR,
        onEditProfile = { state.startEditing() },
        onCancelEdit = { state.cancelEditing() },
        onSaveProfile = { 
            state.saveProfile(
                onSuccess = { },
                onFailure = { }
            )
        }
    )
    
    // Renderizar el contenido de la pantalla
    AdminProfileScreenContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onNavigateToQR = onNavigateToQR,
        onEditProfile = { state.startEditing() },
        onCancelEdit = { state.cancelEditing() },
        onSaveProfile = { 
            state.saveProfile(
                onSuccess = { },
                onFailure = { }
            )
        }
    )
}
