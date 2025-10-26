package org.sysarp.project.ui.components.seller_dashboard.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.UnifiedOverviewData
import org.sysarp.project.ui.components.seller_dashboard.cards.SellerStatCard
import org.sysarp.project.utils.formatCurrencyNoDecimals

/**
 * Sección de estadísticas del vendedor en el dashboard - Versión mejorada
 * Muestra información detallada usando los nuevos campos de la API
 */
@Composable
fun SellerStatsSection(
    confirmedPaymentsCount: Int,
    totalAmountCollected: Double,
    isLoadingStats: Boolean,
    modifier: Modifier = Modifier,
    // Nuevos parámetros para aprovechar la API actualizada
    overviewData: UnifiedOverviewData? = null
) {
    // Header de estadísticas
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Resumen de Ventas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (overviewData != null) {
                "Total: ${overviewData.totalTransactions} transacciones • Promedio: ${formatCurrencyNoDecimals(overviewData.averageTransactionValue)}"
            } else {
                "Estado actual de tus transacciones"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
    
    if (isLoadingStats) {
        // Estado de carga
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SellerStatCard(
                title = "Confirmados",
                value = "...",
                icon = Icons.Filled.CheckCircle,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            
            SellerStatCard(
                title = "Total",
                value = "...",
                icon = Icons.Filled.TrendingUp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }
    } else if (overviewData != null) {
        // Usar datos detallados de la nueva API - SOLO información permitida para vendedores
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Primera fila: Solo ventas confirmadas y promedio (NO ventas totales)
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SellerStatCard(
                    title = "Ventas Confirmadas",
                    value = formatCurrencyNoDecimals(overviewData.confirmedSales),
                    icon = Icons.Filled.CheckCircle,
                    color = Color(0xFF4CAF50), // Verde para confirmado
                    modifier = Modifier.weight(1f)
                )
                
                SellerStatCard(
                    title = "Promedio/Transacción",
                    value = formatCurrencyNoDecimals(overviewData.averageTransactionValue),
                    icon = Icons.Filled.TrendingUp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Segunda fila: Solo transacciones confirmadas y rechazadas (información permitida para vendedores)
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SellerStatCard(
                    title = "Confirmadas",
                    value = "${overviewData.confirmedTransactions}",
                    icon = Icons.Filled.CheckCircle,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                
                SellerStatCard(
                    title = "Rechazadas",
                    value = "${overviewData.rejectedTransactions}",
                    icon = Icons.Filled.Cancel,
                    color = Color(0xFFF44336), // Rojo para rechazado
                    modifier = Modifier.weight(1f)
                )
                
                // Mostrar total de transacciones como contexto
                SellerStatCard(
                    title = "Total",
                    value = "${overviewData.totalTransactions}",
                    icon = Icons.Filled.Assessment,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    } else {
        // Fallback a datos básicos
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SellerStatCard(
                title = "Confirmados",
                value = confirmedPaymentsCount.toString(),
                icon = Icons.Filled.CheckCircle,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            
            SellerStatCard(
                title = "Total Recaudado",
                value = formatCurrencyNoDecimals(totalAmountCollected),
                icon = Icons.Filled.CheckCircle,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
