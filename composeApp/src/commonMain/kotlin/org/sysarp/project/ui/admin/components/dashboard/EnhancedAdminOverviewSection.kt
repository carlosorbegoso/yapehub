package org.sysarp.project.ui.admin.components.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.UnifiedOverviewData
import org.sysarp.project.data.UnifiedTopSellerData
import org.sysarp.project.utils.formatCurrencyNoDecimals

/**
 * Sección mejorada de resumen del administrador que aprovecha los nuevos campos de la API
 */
@Composable
fun EnhancedAdminOverviewSection(
    overviewData: UnifiedOverviewData,
    topSellers: List<UnifiedTopSellerData>? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Resumen principal
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Panel de Control",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Resumen completo del negocio",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.Dashboard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                
                // Métricas principales de ventas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AdminMetricItem(
                        title = "Ventas Confirmadas",
                        value = formatCurrencyNoDecimals(overviewData.confirmedSales),
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    
                    AdminMetricItem(
                        title = "Ventas Totales",
                        value = formatCurrencyNoDecimals(overviewData.allSales),
                        icon = Icons.Default.TrendingUp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Estado de transacciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AdminTransactionStatusItem(
                        label = "Confirmadas",
                        count = overviewData.confirmedTransactions,
                        total = overviewData.totalTransactions,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    
                    AdminTransactionStatusItem(
                        label = "Pendientes",
                        count = overviewData.pendingTransactions,
                        total = overviewData.totalTransactions,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                    
                    AdminTransactionStatusItem(
                        label = "Rechazadas",
                        count = overviewData.rejectedTransactions,
                        total = overviewData.totalTransactions,
                        color = Color(0xFFF44336),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Información adicional
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoItem(
                        label = "Promedio/Transacción",
                        value = formatCurrencyNoDecimals(overviewData.averageTransactionValue),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    InfoItem(
                        label = "Total Transacciones",
                        value = overviewData.totalTransactions.toString(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    val successRate = if (overviewData.totalTransactions > 0) {
                        (overviewData.confirmedTransactions.toFloat() / overviewData.totalTransactions.toFloat()) * 100
                    } else 0f
                    
                    InfoItem(
                        label = "Tasa de Éxito",
                        value = "${String.format("%.1f", successRate)}%",
                        color = if (successRate >= 80) Color(0xFF4CAF50) else if (successRate >= 60) Color(0xFFFF9800) else Color(0xFFF44336)
                    )
                }
            }
        }
        
        // Top Sellers (si están disponibles)
        topSellers?.takeIf { it.isNotEmpty() }?.let { sellers ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Top Vendedores",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    
                    sellers.take(3).forEach { seller ->
                        TopSellerItem(
                            seller = seller,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminMetricItem(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun AdminTransactionStatusItem(
    label: String,
    count: Int,
    total: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        if (total > 0) {
            val percentage = (count.toFloat() / total.toFloat()) * 100
            Text(
                text = "${String.format("%.0f", percentage)}%",
                style = MaterialTheme.typography.bodySmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun TopSellerItem(
    seller: UnifiedTopSellerData,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${seller.rank}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = seller.sellerName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = color
                )
                Text(
                    text = seller.branchName,
                    style = MaterialTheme.typography.bodySmall,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = formatCurrencyNoDecimals(seller.totalSales),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "${seller.transactionCount} transacciones",
                style = MaterialTheme.typography.bodySmall,
                color = color.copy(alpha = 0.7f)
            )
        }
    }
}