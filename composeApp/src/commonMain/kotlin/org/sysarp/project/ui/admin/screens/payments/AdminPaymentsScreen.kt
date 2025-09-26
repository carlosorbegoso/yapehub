package org.sysarp.project.ui.screens.admin

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
import org.sysarp.project.ui.admin.screens.payments.AdminPaymentsActions
import org.sysarp.project.ui.admin.screens.payments.AdminPaymentsContentHandler
import org.sysarp.project.ui.admin.screens.payments.AdminPaymentsState
import org.sysarp.project.ui.common.components.topbar.TopBarComponent
import org.sysarp.project.ui.common.components.DateFilterComponent
import org.sysarp.project.ui.common.components.rememberDateFilterState

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
    
    // Estado del filtro de fechas
    val dateFilterState = rememberDateFilterState()
    
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
            // Filtro de fechas
            DateFilterComponent(
                selectedDateRange = dateFilterState.selectedDateRange,
                onDateRangeSelected = { period ->
                    dateFilterState.onDateRangeSelected(period)
                    state.filterByDateRange(
                        startDate = dateFilterState.startDate,
                        endDate = dateFilterState.endDate,
                        onSuccess = { },
                        onFailure = { }
                    )
                },
                showCalendar = dateFilterState.showCalendarDialog,
                onShowCalendar = dateFilterState.onShowCalendar,
                onDismissCalendar = dateFilterState.onDismissCalendar,
                title = "Filtrar pagos por fecha",
                description = "Selecciona un período para filtrar los pagos del sistema"
            )
            
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
