package org.sysarp.project.ui.admin.components.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import org.sysarp.project.utils.formatCurrencyNoDecimals
import org.sysarp.project.viewmodel.admin.EnhancedAdminDashboardData

/**
 * Sección mejorada del dashboard del administrador que aprovecha todos los datos de la nueva API
 */
@Composable
fun EnhancedAdminDashboardSection(
    enhancedData: EnhancedAdminDashboardData,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSellerManagement: () -> Unit,
    onNavigateToPendingPayments: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Resumen ejecutivo principal
        ExecutiveSummaryCard(enhancedData)
        
        // Métricas de ventas detalladas
        SalesMetricsCard(enhancedData)
        
        // Estado de transacciones
        TransactionStatusCard(enhancedData, onNavigateToPendingPayments)
        
        // Top vendedores
        if (enhancedData.topSellers.isNotEmpty()) {
            TopSellersCard(enhancedData, onNavigateToSellerManagement)
        }
        
        // Métricas de rendimiento
        PerformanceMetricsCard(enhancedData)
        
        // Acciones rápidas
        QuickActionsCard(
            onNavigateToAnalytics = onNavigateToAnalytics,
            onNavigateToSellerManagement = onNavigateToSellerManagement,
            onNavigateToPendingPayments = onNavigateToPendingPayments
        )
    }
}

@Composable
private fun ExecutiveSummaryCard(data: EnhancedAdminDashboardData) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Resumen Ejecutivo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Vista general del negocio",
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
            
            // Métricas principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ExecutiveMetricItem(
                    title = "Ingresos Totales",
                    value = formatCurrencyNoDecimals(data.overview.allSales),
                    subtitle = "Confirmados: ${formatCurrencyNoDecimals(data.overview.confirmedSales)}",
                    icon = Icons.Default.AttachMoney,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                
                ExecutiveMetricItem(
                    title = "Transacciones",
                    value = "${data.overview.totalTransactions}",
                    subtitle = "${data.overview.confirmedTransactions} confirmadas",
                    icon = Icons.Default.Receipt,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Tasa de éxito
            val successRate = if (data.overview.totalTransactions > 0) {
                (data.overview.confirmedTransactions.toFloat() / data.overview.totalTransactions.toFloat()) * 100
            } else 0f
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tasa de Éxito del Negocio:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${String.format("%.1f", successRate)}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (successRate >= 80) Color(0xFF4CAF50) else if (successRate >= 60) Color(0xFFFF9800) else Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
private fun SalesMetricsCard(data: EnhancedAdminDashboardData) {
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
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Métricas de Ventas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SalesMetricItem(
                    label = "Promedio/Transacción",
                    value = formatCurrencyNoDecimals(data.overview.averageTransactionValue),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                
                SalesMetricItem(
                    label = "Crecimiento",
                    value = "${String.format("%.1f", data.overview.salesGrowth)}%",
                    color = if (data.overview.salesGrowth >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
                
                val pendingAmount = data.overview.allSales - data.overview.confirmedSales
                SalesMetricItem(
                    label = "Pendiente",
                    value = formatCurrencyNoDecimals(pendingAmount),
                    color = if (pendingAmount > 0) Color(0xFFFF9800) else MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TransactionStatusCard(
    data: EnhancedAdminDashboardData,
    onNavigateToPendingPayments: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Estado de Transacciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                
                if (data.overview.pendingTransactions > 0) {
                    TextButton(onClick = onNavigateToPendingPayments) {
                        Text("Ver Pendientes")
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TransactionStatusItem(
                    label = "Confirmadas",
                    count = data.overview.confirmedTransactions,
                    total = data.overview.totalTransactions,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                
                TransactionStatusItem(
                    label = "Pendientes",
                    count = data.overview.pendingTransactions,
                    total = data.overview.totalTransactions,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
                
                TransactionStatusItem(
                    label = "Rechazadas",
                    count = data.overview.rejectedTransactions,
                    total = data.overview.totalTransactions,
                    color = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TopSellersCard(
    data: EnhancedAdminDashboardData,
    onNavigateToSellerManagement: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFD700), // Gold
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Top Vendedores",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                TextButton(onClick = onNavigateToSellerManagement) {
                    Text("Ver Todos")
                }
            }
            
            data.topSellers.take(3).forEach { seller ->
                TopSellerItem(
                    seller = seller,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PerformanceMetricsCard(data: EnhancedAdminDashboardData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Métricas de Rendimiento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PerformanceMetricItem(
                    label = "Tiempo Promedio",
                    value = "${String.format("%.1f", data.performanceMetrics.averageConfirmationTime)} min",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                PerformanceMetricItem(
                    label = "Tasa de Reclamo",
                    value = "${String.format("%.1f", data.performanceMetrics.claimRate)}%",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                
                PerformanceMetricItem(
                    label = "Tasa de Rechazo",
                    value = "${String.format("%.1f", data.performanceMetrics.rejectionRate)}%",
                    color = if (data.performanceMetrics.rejectionRate > 10) Color(0xFFF44336) else Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionsCard(
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSellerManagement: () -> Unit,
    onNavigateToPendingPayments: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Acciones Rápidas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton(
                    text = "Analytics",
                    icon = Icons.Default.Analytics,
                    onClick = onNavigateToAnalytics,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                QuickActionButton(
                    text = "Vendedores",
                    icon = Icons.Default.People,
                    onClick = onNavigateToSellerManagement,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                QuickActionButton(
                    text = "Pendientes",
                    icon = Icons.Default.Schedule,
                    onClick = onNavigateToPendingPayments,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// Componentes auxiliares
@Composable
private fun ExecutiveMetricItem(
    title: String,
    value: String,
    subtitle: String,
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
            modifier = Modifier.size(32.dp)
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
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun SalesMetricItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun TransactionStatusItem(
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
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
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
private fun TopSellerItem(
    seller: org.sysarp.project.data.UnifiedTopSellerData,
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
                color = Color(0xFFFFD700)
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
                color = Color(0xFF4CAF50)
            )
            Text(
                text = "${seller.transactionCount} transacciones",
                style = MaterialTheme.typography.bodySmall,
                color = color.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun PerformanceMetricItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
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
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun QuickActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}