package org.sysarp.project.ui.admin.screens.payments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.ui.common.components.DateFilterComponent
import org.sysarp.project.ui.common.components.rememberDateFilterState
import org.sysarp.project.ui.common.components.topbar.TopBarComponent
import org.sysarp.project.ui.admin.screens.payments.components.AdminPaymentsContent
import org.sysarp.project.ui.common.components.animations.FadeInContent
import org.sysarp.project.ui.common.components.animations.ScaleInContent
import org.sysarp.project.ui.common.components.theme.DarkModeToggle
import org.sysarp.project.ui.common.components.responsive.ResponsivePadding
import org.sysarp.project.ui.common.components.responsive.ResponsiveText

/**
 * Pantalla de gestión de pagos del administrador con mejoras de UI/UX
 * Incluye Dark Mode, animaciones suaves y diseño responsive
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPaymentsScreenEnhanced(
    onNavigateBack: () -> Unit,
    // Dependencies are now injected by Koin
    authService: AuthService = koinInject(),
    paymentService: PaymentService = koinInject()
) {
    val coroutineScope = rememberCoroutineScope()
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()

    // Estado para Dark Mode
    var isDarkMode by remember { mutableStateOf(false) }

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

    // Aplicar el tema con Dark Mode
    org.sysarp.project.ui.theme.YapeHubTheme(darkTheme = isDarkMode) {
        Scaffold(
            topBar = {
                ScaleInContent {
                    TopBarComponent(
                        title = "Pagos Pendientes",
                        subtitle = "Administra los pagos de tus vendedores",
                        onNavigateBack = onNavigateBack,
                        actions = {
                            // Toggle de Dark Mode en la barra superior
                            DarkModeToggle(
                                isDarkMode = isDarkMode,
                                onToggle = { isDarkMode = !isDarkMode }
                            )
                        }
                    )
                }
            }
        ) { paddingValues ->
            FadeInContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Información del tema actual
                    ResponsivePadding(
                        mobile = PaddingValues(16.dp),
                        tablet = PaddingValues(24.dp),
                        desktop = PaddingValues(32.dp)
                    ) {
                        ResponsiveText(
                            text = if (isDarkMode) "Modo Oscuro Activado" else "Modo Claro Activado",
                            mobileStyle = MaterialTheme.typography.bodySmall,
                            tabletStyle = MaterialTheme.typography.bodyMedium,
                            desktopStyle = MaterialTheme.typography.titleSmall
                        )
                    }

                    // Filtro de fechas con animación
                    ScaleInContent {
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
                    }

                    // Contenido principal con animación
                    FadeInContent {
                        AdminPaymentsContent(
                            state = state,
                            onLoadMore = {
                                state.loadMorePayments(
                                    onSuccess = { },
                                    onFailure = { }
                                )
                            },
                            onStatusFilterChange = { status ->
                                state.filterByStatus(
                                    status = status,
                                    onSuccess = { },
                                    onFailure = { }
                                )
                            },
                            onPaymentAction = { paymentId, action ->
                                // Manejar acciones de pago
                            }
                        )
                    }
                }
            }
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
