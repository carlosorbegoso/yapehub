package org.sysarp.project.ui.screens.seller

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.ui.seller.screens.payments.SellerPaymentsActions
import org.sysarp.project.ui.seller.screens.payments.SellerPaymentsContent
import org.sysarp.project.ui.seller.screens.payments.SellerPaymentsState
import org.sysarp.project.ui.seller.screens.payments.SellerPaymentsTabs
import org.sysarp.project.ui.seller.screens.payments.SellerPaymentsTopBar

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
