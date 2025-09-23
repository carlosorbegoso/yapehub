package org.sysarp.project.ui.admin.screens.analytics

import androidx.compose.runtime.Composable

/**
 * Acciones y handlers para AdminAnalyticsControls
 */

@Composable
fun AdminAnalyticsControlsActions(
    state: AdminAnalyticsControlsState,
    onPeriodChange: (String) -> Unit,
    onLoadAnalytics: (String?, String?) -> Unit,
    onShowFiltersDialog: () -> Unit,
    onShowFinancialFiltersDialog: () -> Unit,
    onShowTransparencyFiltersDialog: () -> Unit,
    onShowPeriodMenu: () -> Unit
) {
    // Manejar la lógica de acciones de los controles
    // Las acciones específicas se manejan en los componentes individuales
}

@Composable
fun AdminAnalyticsControlsContent(
    state: AdminAnalyticsControlsState,
    onPeriodChange: (String) -> Unit,
    onLoadAnalytics: (String?, String?) -> Unit,
    onShowFiltersDialog: () -> Unit,
    onShowFinancialFiltersDialog: () -> Unit,
    onShowTransparencyFiltersDialog: () -> Unit,
    onShowPeriodMenu: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    AdminAnalyticsControlsContainer(
        state = state,
        onPeriodChange = onPeriodChange,
        onLoadAnalytics = onLoadAnalytics,
        onShowFiltersDialog = onShowFiltersDialog,
        onShowFinancialFiltersDialog = onShowFinancialFiltersDialog,
        onShowTransparencyFiltersDialog = onShowTransparencyFiltersDialog,
        onShowPeriodMenu = onShowPeriodMenu,
        modifier = modifier
    )
}
