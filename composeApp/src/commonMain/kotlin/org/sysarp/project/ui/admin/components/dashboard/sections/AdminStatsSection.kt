package org.sysarp.project.ui.admin.components.dashboard.sections

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.ui.components.seller_dashboard.cards.SellerStatCard
import org.sysarp.project.utils.formatCurrencyNoDecimals
import org.sysarp.project.viewmodel.admin.EnhancedAdminDashboardData

/**
 * Sección de estadísticas del administrador
 * Replica el estilo de SellerStatsSection pero con información completa para admin
 */
@Composable
fun AdminStatsSection(
    enhancedData: EnhancedAdminDashboardData?,
    isLoadingStats: Boolean,
    modifier: Modifier = Modifier
) {
    // Header de estadísticas (mismo estilo que SellerStatsSection)
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Resumen del Negocio",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (enhancedData != null) {
                "Total: ${enhancedData.overview.totalTransactions} transacciones • Tasa de éxito: ${
                    if (enhancedData.overview.totalTransactions > 0) 
                        String.format("%.1f", (enhancedData.overview.confirmedTransactions.toFloat() / enhancedData.overview.totalTransactions.toFloat()) * 100)
                    else "0"
                }%"
            } else {
                "Estado actual de tu negocio"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
    
    if (isLoadingStats) {
        // Estado de carga (mismo estilo que SellerStatsSection)
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SellerStatCard(
                title = "Cargando...",
                value = "...",
                icon = Icons.Filled.TrendingUp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            
            SellerStatCard(
                title = "Cargando...",
                value = "...",
                icon = Icons.Filled.CheckCircle,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }
    } else if (enhancedData != null) {
        // Datos reales del administrador (estructura similar a SellerStatsSection)
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Primera fila: Ventas confirmadas y totales (información completa para admin)
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SellerStatCard(
                    title = "Ventas Confirmadas",
                    value = formatCurrencyNoDecimals(enhancedData.overview.confirmedSales),
                    icon = Icons.Filled.CheckCircle,
                    color = Color(0xFF00C853), // Verde vibrante y moderno
                    modifier = Modifier.weight(1f)
                )
                
                SellerStatCard(
                    title = "Ventas Totales",
                    value = formatCurrencyNoDecimals(enhancedData.overview.allSales),
                    icon = Icons.Filled.TrendingUp,
                    color = Color(0xFF2196F3), // Azul moderno y profesional
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Segunda fila: Estado de transacciones (información detallada para admin)
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SellerStatCard(
                    title = "Confirmadas",
                    value = "${enhancedData.overview.confirmedTransactions}",
                    icon = Icons.Filled.CheckCircle,
                    color = Color(0xFF00BCD4), // Cian moderno
                    modifier = Modifier.weight(1f)
                )
                
                if (enhancedData.overview.pendingTransactions > 0) {
                    SellerStatCard(
                        title = "Pendientes",
                        value = "${enhancedData.overview.pendingTransactions}",
                        icon = Icons.Filled.Schedule,
                        color = Color(0xFFFFC107), // Amarillo dorado
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    SellerStatCard(
                        title = "Promedio",
                        value = formatCurrencyNoDecimals(enhancedData.overview.averageTransactionValue),
                        icon = Icons.Filled.Analytics,
                        color = Color(0xFF1976D2), // Azul profundo consistente
                        modifier = Modifier.weight(1f)
                    )
                }
                
                SellerStatCard(
                    title = "Rechazadas",
                    value = "${enhancedData.overview.rejectedTransactions}",
                    icon = Icons.Filled.Cancel,
                    color = Color(0xFFE91E63), // Rosa vibrante (menos agresivo que rojo)
                    modifier = Modifier.weight(1f)
                )
            }
        }
    } else {
        // Estado sin datos (mismo estilo que SellerStatsSection)
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SellerStatCard(
                title = "Sin Datos",
                value = "0",
                icon = Icons.Filled.Info,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.weight(1f)
            )
            
            SellerStatCard(
                title = "Sin Datos",
                value = "0",
                icon = Icons.Filled.Info,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.weight(1f)
            )
        }
    }
}