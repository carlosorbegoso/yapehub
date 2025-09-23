package org.sysarp.project.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.ui.components.topbar.TopBarComponent
import org.sysarp.project.ui.admin.screens.payments.*

/**
 * Pantalla de gestión de pagos del administrador refactorizada
 * Usa componentes modulares para mejor mantenibilidad
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPaymentsScreen(
    authService: AuthService,
    paymentService: PaymentService,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()
    
    // Crear el estado del screen
    val state = remember {
        AdminPaymentsState(
            authService = authService,
            paymentService = paymentService,
            coroutineScope = coroutineScope
        )
    }
    
    Scaffold(
        topBar = {
            TopBarComponent(
                title = "Gestión de Pagos",
                subtitle = "Administra y supervisa todos los pagos del sistema",
                onNavigateBack = onNavigateBack,
                onRefresh = {
                    state.loadAdminPayments(
                        onSuccess = { },
                        onFailure = { }
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AdminPaymentsContentHandler(state = state)
        }
    }
    
    // Manejar acciones del screen
    AdminPaymentsActions(
        state = state,
        userProfile = userProfile,
        accessToken = accessToken,
        onNavigateBack = onNavigateBack
    )
}
