package org.sysarp.project.ui.admin.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.ConnectedSellersData
import org.sysarp.project.data.PerformanceMetricsData
import org.sysarp.project.data.QuickSummaryData
import org.sysarp.project.ui.common.components.charts.PerformanceMetricsPieChart
import org.sysarp.project.utils.formatTimeOnly

/**
 * Componentes UI principales para AdminDashboardScreen
 */

@Composable
fun QuickStatsSection(
    quickSummaryData: QuickSummaryData?,
    isLoadingStats: Boolean,
    statsError: String
) {
    // Título de la sección
    Text(
        text = "📊 Resumen",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    
    // Contenido de estadísticas
    if (isLoadingStats) {
        LoadingCard()
    } else if (statsError.isNotEmpty()) {
        ErrorCard(
            title = "Error cargando estadísticas",
            message = statsError
        )
    } else if (quickSummaryData != null) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(6) { index ->
                when (index) {
                    0 -> StatCard(
                        title = "Ventas Confirmadas",
                        value = org.sysarp.project.utils.formatCurrencyNoDecimals(quickSummaryData.confirmedSales),
                        icon = Icons.Default.CheckCircle,
                        color = androidx.compose.ui.graphics.Color(0xFF00C853) // Verde vibrante
                    )
                    1 -> StatCard(
                        title = "Ventas Totales",
                        value = org.sysarp.project.utils.formatCurrencyNoDecimals(quickSummaryData.allSales),
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        color = androidx.compose.ui.graphics.Color(0xFF2196F3) // Azul moderno
                    )
                    2 -> StatCard(
                        title = "Transacciones",
                        value = quickSummaryData.totalTransactions.toString(),
                        icon = Icons.Default.Receipt,
                        color = androidx.compose.ui.graphics.Color(0xFF1565C0) // Azul profundo
                    )
                    3 -> StatCard(
                        title = "Promedio",
                        value = org.sysarp.project.utils.formatCurrencyNoDecimals(quickSummaryData.averageTransactionValue),
                        icon = Icons.Default.Analytics,
                        color = androidx.compose.ui.graphics.Color(0xFFFF5722) // Naranja vibrante
                    )
                    4 -> StatCard(
                        title = "Confirmadas",
                        value = "${quickSummaryData.confirmedTransactions} (${if (quickSummaryData.totalTransactions > 0) String.format("%.0f", (quickSummaryData.confirmedTransactions.toFloat() / quickSummaryData.totalTransactions.toFloat()) * 100) else "0"}%)",
                        icon = Icons.Default.CheckCircle,
                        color = androidx.compose.ui.graphics.Color(0xFF00BCD4) // Cian moderno
                    )
                    5 -> StatCard(
                        title = "Pendientes",
                        value = "${quickSummaryData.pendingTransactions}",
                        icon = Icons.Default.Schedule,
                        color = androidx.compose.ui.graphics.Color(0xFFFFC107) // Amarillo dorado
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentStatusSection(
    quickSummaryData: QuickSummaryData?
) {
    if (quickSummaryData != null) {
        // Título de la sección
        Text(
            text = "📊 Estado de Pagos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        // Gráfico de estado de pagos
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PerformanceMetricsPieChart(
                    performanceMetrics = PerformanceMetricsData(
                        confirmedPayments = quickSummaryData.confirmedPayments,
                        rejectedPayments = quickSummaryData.rejectedPayments,
                        pendingPayments = quickSummaryData.pendingPayments,
                        averageConfirmationTime = quickSummaryData.averageConfirmationTime,
                        claimRate = quickSummaryData.claimRate,
                        rejectionRate = 0.0 // Calcular si es necesario
                    ),
                    showCard = false // ← Evitar duplicación de Card
                )
            }
        }
    }
}

@Composable
fun MainActionsSection(
    onNavigateToBranchManagement: () -> Unit,
    onNavigateToSellerManagement: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToPendingPayments: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToBilling: () -> Unit
) {
    // Título de la sección
    Text(
        text = "Acciones Principales",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    
    // Tarjetas de acciones
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionCard(
            title = "Gestionar Sucursales",
            subtitle = "Crear, editar y administrar sucursales",
            icon = Icons.Filled.Business,
            onClick = onNavigateToBranchManagement
        )
        
        ActionCard(
            title = "Gestión de Vendedores",
            subtitle = "Administrar vendedores y equipos",
            icon = Icons.Filled.People,
            onClick = onNavigateToSellerManagement
        )
        
        ActionCard(
            title = "Ver Analytics",
            subtitle = "Reportes y estadísticas detalladas",
            icon = Icons.Filled.Analytics,
            onClick = onNavigateToAnalytics
        )
        
        ActionCard(
            title = "Pagos Pendientes",
            subtitle = "Revisar y confirmar pagos",
            icon = Icons.Filled.Payment,
            onClick = onNavigateToPendingPayments
        )
        
        ActionCard(
            title = "Configuración",
            subtitle = "Ajustes del sistema y perfil",
            icon = Icons.Filled.Settings,
            onClick = onNavigateToSettings
        )
        
        ActionCard(
            title = "Facturación",
            subtitle = "Gestión de suscripciones y tokens",
            icon = Icons.Filled.AttachMoney,
            onClick = onNavigateToBilling
        )
    }
}

@Composable
fun ConnectedSellersSection(
    connectedSellersData: ConnectedSellersData?,
    isLoadingSellers: Boolean,
    sellersError: String
) {
    // Título de la sección
    Text(
        text = "Vendedores Conectados",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    
    // Contenido de vendedores
    if (isLoadingSellers) {
        LoadingCard()
    } else if (sellersError.isNotEmpty()) {
        ErrorCard(
            title = "Error cargando vendedores",
            message = sellersError
        )
    } else {
        val sellersData = connectedSellersData
        if (sellersData != null) {
            // Estadísticas de conexión
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ConnectionStatItem(
                        label = "Conectados", 
                        value = sellersData.totalConnected.toString(), 
                        icon = Icons.Filled.CheckCircle
                    )
                    ConnectionStatItem(
                        label = "Total", 
                        value = sellersData.connectedSellers.size.toString(), 
                        icon = Icons.Filled.People
                    )
                    ConnectionStatItem(
                        label = "Última actualización", 
                        value = formatTimeOnly(sellersData.timestamp), 
                        icon = Icons.Filled.Schedule
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lista de vendedores
            if (sellersData.connectedSellers.isEmpty()) {
                EmptySellersCard()
            } else {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sellersData.connectedSellers.forEach { seller ->
                        ConnectedSellerCard(sellerInfo = seller)
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No hay datos de vendedores",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Composable
fun DashboardContent(
    quickSummaryData: QuickSummaryData?,
    isLoadingStats: Boolean,
    statsError: String,
    connectedSellersData: ConnectedSellersData?,
    isLoadingSellers: Boolean,
    sellersError: String,
    pendingRequestsCount: Int,
    onNavigateToBranchManagement: () -> Unit,
    onNavigateToSellerManagement: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToPendingPayments: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDeactivationRequests: () -> Unit,
    onNavigateToBilling: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Resumen rápido
        item {
            QuickStatsSection(
                quickSummaryData = quickSummaryData,
                isLoadingStats = isLoadingStats,
                statsError = statsError
            )
        }
        
        // Estado de pagos
        item {
            PaymentStatusSection(quickSummaryData = quickSummaryData)
        }
        
        // Solicitudes de baja pendientes
        item {
            PendingRequestsCard(
                pendingRequestsCount = pendingRequestsCount,
                onNavigateToDeactivationRequests = onNavigateToDeactivationRequests
            )
        }
        
        // Acciones principales
        item {
            MainActionsSection(
                onNavigateToBranchManagement = onNavigateToBranchManagement,
                onNavigateToSellerManagement = onNavigateToSellerManagement,
                onNavigateToAnalytics = onNavigateToAnalytics,
                onNavigateToPendingPayments = onNavigateToPendingPayments,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToBilling = onNavigateToBilling
            )
        }
        
        
        // Vendedores conectados
        item {
            ConnectedSellersSection(
                connectedSellersData = connectedSellersData,
                isLoadingSellers = isLoadingSellers,
                sellersError = sellersError
            )
        }
        
        // Espacio adicional
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
