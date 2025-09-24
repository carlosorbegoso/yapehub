package org.sysarp.project.ui.screens.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.sysarp.project.ui.admin.screens.analytics.AdminAnalyticsControlsContainer
import org.sysarp.project.ui.admin.screens.analytics.AdminAnalyticsControlsState

/**
 * Componente de controles para AdminAnalyticsScreen refactorizado
 * Usa componentes modulares para mejor mantenibilidad
 */
@Composable
fun AdminAnalyticsControls(
    state: AdminAnalyticsState,
    onPeriodChange: (String) -> Unit,
    onLoadAnalytics: (String?, String?) -> Unit,
    onShowFiltersDialog: () -> Unit,
    onShowFinancialFiltersDialog: () -> Unit,
    onShowTransparencyFiltersDialog: () -> Unit,
    onShowPeriodMenu: () -> Unit,
    onClosePeriodMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Crear el estado de los controles
    val controlsState = remember {
        AdminAnalyticsControlsState()
    }
    
    // Sincronizar el estado externo con el estado interno
    LaunchedEffect(state.selectedPeriod, state.showPeriodMenu) {
        controlsState.updateSelectedPeriod(state.selectedPeriod)
        controlsState.updateShowPeriodMenu(state.showPeriodMenu)
    }
    
    // Renderizar el contenido de los controles
    AdminAnalyticsControlsContainer(
        state = controlsState,
        onPeriodChange = onPeriodChange,
        onLoadAnalytics = onLoadAnalytics,
        onShowFiltersDialog = onShowFiltersDialog,
        onShowFinancialFiltersDialog = onShowFinancialFiltersDialog,
        onShowTransparencyFiltersDialog = onShowTransparencyFiltersDialog,
        onShowPeriodMenu = onShowPeriodMenu,
        onClosePeriodMenu = onClosePeriodMenu,
        modifier = modifier
    )
}
