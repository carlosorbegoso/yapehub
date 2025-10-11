package org.sysarp.project.ui.admin.screens.payments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import org.sysarp.project.data.PaymentFilterStatus
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.ui.admin.screens.payments.components.AdminPaymentsContent
import org.sysarp.project.ui.admin.screens.payments.components.AdminPaymentsTabs
import org.sysarp.project.ui.components.calendar.SmartCalendar
import org.sysarp.project.ui.common.components.topbar.TopBarComponent

/**
 * Pantalla de gestión de pagos del administrador mejorada con filtros dinámicos
 * Usa componentes modulares y Koin para DI con mejor UX
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

    // Estados para filtros
    var yapeCodeFilter by remember { mutableStateOf("") }
    var showCalendar by remember { mutableStateOf(false) }
    var selectedDateRange by remember { mutableStateOf("📅 30 días") } // Coincide con el estado por defecto

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
        
        // Inicializar filtros por defecto
        state.initializeDefaultFilters()

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
                       },
                       onCalendarClick = {
                           showCalendar = true
                       }
                   )
               }
           ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs mejorados con filtros
            AdminPaymentsTabs(
                selectedTab = 0, // Por defecto "Todos"
                onTabSelected = { tabIndex ->
                    // Mapear tabs a estados
                    val statuses = when (tabIndex) {
                        0 -> listOf(PaymentFilterStatus.ALL) // Todos
                        1 -> listOf(PaymentFilterStatus.PENDING) // Pendientes
                        2 -> listOf(PaymentFilterStatus.CLAIMED) // Confirmados
                        3 -> listOf(PaymentFilterStatus.REJECTED) // Rechazados
                        else -> listOf(PaymentFilterStatus.ALL)
                    }
                    state.updateSelectedStatuses(statuses)
                    state.loadAdminPayments(
                        onSuccess = { },
                        onFailure = { }
                    )
                },
                yapeCodeFilter = state.yapeCodeFilter,
                onYapeCodeFilterChanged = { newFilter ->
                    state.updateYapeCodeFilter(newFilter)
                }
            )

            // Contenido principal simplificado
            AdminPaymentsContent(
                state = state,
                onLoadMore = {
                    state.loadMorePayments(
                        onSuccess = { },
                        onFailure = { }
                    )
                },
                onPaymentAction = { paymentId, action ->
                    // Manejar acciones de pago
                    println("ADMIN_PAYMENTS: Acción $action en pago $paymentId")
                }
            )
        }
        
        // Calendario inteligente
        SmartCalendar(
            selectedPeriod = selectedDateRange,
            onPeriodSelected = { newPeriod ->
                selectedDateRange = newPeriod
                state.updateDateRangeFilter(newPeriod)
                showCalendar = false
                
                // Recargar datos con los nuevos filtros de fecha
                state.loadAdminPayments(
                    onSuccess = { },
                    onFailure = { }
                )
            },
            expanded = showCalendar,
            onDismiss = { showCalendar = false }
        )
    }
}