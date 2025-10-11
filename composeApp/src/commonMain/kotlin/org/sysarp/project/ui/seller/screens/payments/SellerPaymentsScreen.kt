package org.sysarp.project.ui.seller.screens.payments

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.data.PaymentFilterStatus
import org.sysarp.project.ui.components.calendar.SmartCalendar
import org.sysarp.project.ui.seller.screens.payments.components.SellerPaymentsComponents

/**
 * Pantalla de pagos del vendedor mejorada con filtros dinámicos
 * Usa componentes modulares para mejor mantenibilidad y UX
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerPaymentsScreen(
    authService: AuthService,
    paymentService: PaymentService,
    statsService: org.sysarp.project.service.stats.StatsService,
    onNavigateBack: () -> Unit
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
        SellerPaymentsState(
            paymentService = paymentService,
            statsService = statsService,
            coroutineScope = coroutineScope
        )
    }
    
    // Cargar datos iniciales cuando el usuario esté disponible
    androidx.compose.runtime.LaunchedEffect(userProfile, accessToken) {
        if (userProfile != null && accessToken != null) {
            println("SELLER_PAYMENTS_SCREEN: Iniciando carga de datos para sellerId: ${userProfile?.sellerId}")
            
            // Actualizar el estado con los datos del usuario
            state.updateUserProfile(userProfile)
            state.updateAccessToken(accessToken)
            
            // Inicializar filtros por defecto
            state.initializeDefaultFilters()

            // Cargar pagos con filtros actuales
            state.loadPaymentsWithFilters(
                onSuccess = { },
                onFailure = { }
            )
            
            // Cargar estadísticas del seller
            println("SELLER_PAYMENTS_SCREEN: Llamando a loadSellerStats...")
            state.loadSellerStats(
                onSuccess = { println("SELLER_PAYMENTS_SCREEN: Estadísticas cargadas exitosamente") },
                onFailure = { error -> println("SELLER_PAYMENTS_SCREEN: Error cargando estadísticas: $error") }
            )
        } else {
            println("SELLER_PAYMENTS_SCREEN: No se pueden cargar datos - userProfile: ${userProfile != null}, accessToken: ${accessToken != null}")
        }
    }
    
    // Usar el componente mejorado con filtros
    SellerPaymentsComponents(
        state = state,
        pendingPayments = state.pendingPayments,
        confirmedPayments = state.confirmedPayments,
        isLoadingPending = state.isLoading,
        isLoadingConfirmed = state.isLoading,
        errorMessagePending = state.errorMessage,
        errorMessageConfirmed = state.errorMessage,
        selectedTab = state.selectedTab,
        onTabSelected = { tabIndex ->
            state.changeSelectedTab(tabIndex)
        },
        onRefresh = {
            state.refreshAllPayments(
                onSuccess = { },
                onFailure = { }
            )
        },
        onNavigateBack = onNavigateBack,
        yapeCodeFilter = state.yapeCodeFilter,
        onYapeCodeFilterChanged = { newFilter ->
            state.updateYapeCodeFilter(newFilter)
        },
        onCalendarClick = {
            showCalendar = true
        },
        userProfile = userProfile,
        accessToken = accessToken,
        paymentService = paymentService,
        onError = { error ->
            state.updateErrorMessage(error)
        }
    )
    
    // Calendario inteligente
    SmartCalendar(
        selectedPeriod = selectedDateRange,
        onPeriodSelected = { newPeriod ->
            selectedDateRange = newPeriod
            state.updateDateRangeFilter(newPeriod)
            showCalendar = false
            
            // Recargar datos con los nuevos filtros de fecha
            state.loadPaymentsWithFilters(
                onSuccess = { },
                onFailure = { }
            )
        },
        expanded = showCalendar,
        onDismiss = { showCalendar = false }
    )
}
