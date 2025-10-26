package org.sysarp.project.ui.admin.screens.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.ui.common.screens.AnalyticsDashboardScreen

/**
 * Pantalla de Analytics del Admin usando la pantalla unificada
 */
@Composable
fun AdminAnalyticsScreen(
    authService: AuthService,
    statsService: StatsService,
    onNavigateBack: () -> Unit
) {
    // Obtener datos del usuario actual
    val userProfile by authService.userProfile.collectAsState()
    
    // Usar el dashboard de analytics con los parámetros del admin
    AnalyticsDashboardScreen(
        userType = "ADMIN",
        userId = userProfile?.adminId?.toInt() ?: 0,
        onNavigateBack = onNavigateBack,
        authService = authService,
        statsService = statsService
    )
}