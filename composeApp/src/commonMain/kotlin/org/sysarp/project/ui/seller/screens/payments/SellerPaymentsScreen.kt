package org.sysarp.project.ui.screens.seller

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.ui.seller.screens.payments.*

/**
 * Pantalla de pagos del vendedor refactorizada
 * Usa componentes modulares para mejor mantenibilidad
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerPaymentsScreen(
    authService: AuthService,
    paymentService: PaymentService,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()
    
    // Crear el estado del screen
    val state = remember {
        SellerPaymentsState(
            authService = authService,
            paymentService = paymentService,
            coroutineScope = coroutineScope
        )
    }
    
    Scaffold(
        topBar = {
            SellerPaymentsTopBar(
                onNavigateBack = onNavigateBack,
                onRefresh = {
                    state.refreshAllPayments(
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
            // Tabs
            SellerPaymentsTabs(
                selectedTab = state.selectedTab,
                onTabSelected = { tabIndex ->
                    state.changeSelectedTab(tabIndex)
                }
            )

            // Contenido según tab seleccionado
            SellerPaymentsContent(
                state = state,
                onNavigateBack = onNavigateBack
            )
        }
    }
    
    // Manejar acciones del screen
    SellerPaymentsActions(
        state = state,
        userProfile = userProfile,
        accessToken = accessToken,
        onNavigateBack = onNavigateBack
    )
}
