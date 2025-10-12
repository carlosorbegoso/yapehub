package org.sysarp.project.ui.admin.screens.analytics

import androidx.compose.runtime.*
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.ui.common.screens.UnifiedAnalyticsScreen

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
    
    // Usar la pantalla unificada con los parámetros del admin
    UnifiedAnalyticsScreen(
        userType = "ADMIN",
        userId = userProfile?.adminId?.toInt() ?: 0,
        onNavigateBack = onNavigateBack,
        authService = authService,
        statsService = statsService
    )
}