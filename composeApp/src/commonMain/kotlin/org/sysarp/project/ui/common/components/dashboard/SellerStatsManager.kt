package org.sysarp.project.ui.components.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.sysarp.project.service.stats.StatsService

/**
 * Manager para manejar la lógica de estadísticas del vendedor
 * Actualizado para usar solo el endpoint unificado /api/stats/summary
 */
class SellerStatsManager(
    private val statsService: StatsService
) {

}

/**
 * Composable para crear una instancia del SellerStatsManager
 */
@Composable
fun rememberSellerStatsManager(statsService: StatsService): SellerStatsManager {
    return remember { SellerStatsManager(statsService) }
}