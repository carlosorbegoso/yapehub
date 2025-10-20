package org.sysarp.project.ui.seller.screens.payments

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.ui.components.calendar.SmartCalendar
import org.sysarp.project.ui.seller.screens.payments.components.SellerPaymentsComponents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerPaymentsScreen(
    authService: AuthService,
    paymentService: PaymentService,
    statsService: StatsService,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()

    // ===== Estados Locales =====
    var showCalendar by remember { mutableStateOf(false) }
    var selectedDateRange by remember { mutableStateOf("📅 30 días") }

    // ===== Estado Centralizado =====
    val state = remember {
        SellerPaymentsState(
            paymentService = paymentService,
            statsService = statsService,
            coroutineScope = coroutineScope
        )
    }

    // ===== Inicialización =====

    LaunchedEffect(userProfile, accessToken) {
        if (userProfile == null || accessToken == null) return@LaunchedEffect

        initializeScreen(state, userProfile, accessToken)
    }

    // ===== Handlers =====

    fun handleDateRangeSelected(newPeriod: String) {
        selectedDateRange = newPeriod
        state.updateDateRangeFilter(newPeriod)
        showCalendar = false
        state.loadPaymentsWithFilters()
    }

    fun handleTabSelected(tabIndex: Int) {
        state.changeSelectedTab(tabIndex)
    }

    fun handleRefresh() {
        state.refreshAllPayments()
    }

    fun handleYapeCodeFilterChanged(newFilter: String) {
        state.updateYapeCodeFilter(newFilter)
    }

    fun handleCalendarClick() {
        showCalendar = true
    }

    fun handleCalendarDismiss() {
        showCalendar = false
    }

    // ===== UI =====

    SellerPaymentsComponents(
        state = state,
        selectedTab = state.selectedTab,
        onTabSelected = ::handleTabSelected,
        onRefresh = ::handleRefresh,
        onNavigateBack = onNavigateBack,
        yapeCodeFilter = state.yapeCodeFilter,
        onYapeCodeFilterChanged = ::handleYapeCodeFilterChanged,
        onCalendarClick = ::handleCalendarClick
    )

    SmartCalendar(
        selectedPeriod = selectedDateRange,
        onPeriodSelected = ::handleDateRangeSelected,
        expanded = showCalendar,
        onDismiss = ::handleCalendarDismiss
    )
}

/**
 * Inicializa el screen configurando el estado y cargando datos iniciales
 */
private fun initializeScreen(
    state: SellerPaymentsState,
    userProfile: UserProfile?,
    accessToken: String?
) {
    state.updateUserProfile(userProfile)
    state.updateAccessToken(accessToken)
    state.initializeDefaultFilters()
    state.loadPaymentsWithFilters()
    state.loadSellerStats()
}