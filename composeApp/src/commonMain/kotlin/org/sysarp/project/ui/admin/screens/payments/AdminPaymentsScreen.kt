package org.sysarp.project.ui.admin.screens.payments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.ui.common.components.DateFilterComponent
import org.sysarp.project.ui.common.components.rememberDateFilterState
import org.sysarp.project.ui.common.components.topbar.TopBarComponent
import org.sysarp.project.ui.admin.screens.payments.components.AdminPaymentsContent

/**
 * Pantalla de gestión de pagos del administrador refactorizada
 * Usa componentes modulares y Koin para DI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPaymentsScreen(
    onNavigateBack: () -> Unit,
    // Dependencies are now injected by Koin
    authService: AuthService = koinInject(),
    paymentService: PaymentService = koinInject()
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

    // Inicializar el estado con los datos del usuario
    LaunchedEffect(userProfile, accessToken) {
        println("ADMIN_PAYMENTS: Inicializando con userProfile: $userProfile, accessToken: ${accessToken?.take(10)}...")
        
        state.updateUserProfile(userProfile)
        state.updateAccessToken(accessToken)
        
        println("ADMIN_PAYMENTS: canLoadPayments: ${state.canLoadPayments()}")
        
        if (state.canLoadPayments()) {
            println("ADMIN_PAYMENTS: Cargando pagos...")
            state.loadAdminPayments(
                onSuccess = { 
                    println("ADMIN_PAYMENTS: Pagos cargados exitosamente")
                },
                onFailure = { error ->
                    println("ADMIN_PAYMENTS: Error cargando pagos: $error")
                }
            )
        } else {
            println("ADMIN_PAYMENTS: No se pueden cargar pagos - userProfile: $userProfile, accessToken: ${accessToken != null}")
        }
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

                   AdminPaymentsContent(
                       state = state,
                       onLoadMore = {
                           state.loadMorePayments(
                               onSuccess = { },
                               onFailure = { }
                           )
                       },
                       onAdvancedFiltersChanged = { filters ->
                           state.updateAdvancedFilters(filters)
                       },
                       onPaymentAction = { paymentId, action ->
                           // Manejar acciones de pago
                       }
                   )
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
