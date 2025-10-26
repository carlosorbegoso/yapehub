package org.sysarp.project.ui.admin.components.dashboard.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.viewmodel.admin.EnhancedAdminDashboardData

/**
 * Sección de métricas de rendimiento del administrador
 * Replica el estilo de SellerPaymentsSection pero con métricas de rendimiento
 */
@Composable
fun AdminPerformanceSection(
    enhancedData: EnhancedAdminDashboardData?,
    onNavigateToPendingPayments: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Header (mismo estilo que otras secciones)
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Métricas de Rendimiento",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Análisis de eficiencia del sistema",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            if (enhancedData?.overview?.pendingTransactions ?: 0 > 0) {
                TextButton(onClick = onNavigateToPendingPayments) {
                    Text("Ver Pendientes")
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
    
    if (enhancedData != null) {
        // Tarjetas de métricas (estilo similar a las tarjetas del vendedor)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF0F9FF) // Azul hielo muy suave y elegante
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Primera fila de métricas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PerformanceMetricItem(
                        title = "Tiempo Promedio",
                        value = "${String.format("%.1f", enhancedData.performanceMetrics.averageConfirmationTime)} min",
                        icon = Icons.Default.Schedule,
                        color = Color(0xFF3B82F6), // Azul brillante
                        modifier = Modifier.weight(1f)
                    )
                    
                    PerformanceMetricItem(
                        title = "Tasa de Reclamo",
                        value = "${String.format("%.1f", enhancedData.performanceMetrics.claimRate)}%",
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFF10B981), // Verde esmeralda
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Divider(
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
                
                // Segunda fila de métricas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PerformanceMetricItem(
                        title = "Tasa de Rechazo",
                        value = "${String.format("%.1f", enhancedData.performanceMetrics.rejectionRate)}%",
                        icon = Icons.Default.Cancel,
                        color = if (enhancedData.performanceMetrics.rejectionRate > 10) Color(0xFFEF4444) else Color(0xFFF59E0B), // Rojo y amarillo modernos
                        modifier = Modifier.weight(1f)
                    )
                    
                    PerformanceMetricItem(
                        title = "Pagos Pendientes",
                        value = "${enhancedData.performanceMetrics.pendingPayments}",
                        icon = Icons.Default.Pending,
                        color = if (enhancedData.performanceMetrics.pendingPayments > 0) Color(0xFFF59E0B) else Color(0xFF10B981), // Amarillo y verde modernos
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Resumen de estado
                if (enhancedData.performanceMetrics.pendingPayments > 0 || enhancedData.performanceMetrics.rejectionRate > 5) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (enhancedData.performanceMetrics.rejectionRate > 10) 
                                MaterialTheme.colorScheme.errorContainer 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (enhancedData.performanceMetrics.rejectionRate > 10) 
                                    Icons.Default.Warning 
                                else 
                                    Icons.Default.Info,
                                contentDescription = null,
                                tint = if (enhancedData.performanceMetrics.rejectionRate > 10) 
                                    MaterialTheme.colorScheme.onErrorContainer 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = when {
                                    enhancedData.performanceMetrics.rejectionRate > 10 -> "Alta tasa de rechazo - Revisar procesos"
                                    enhancedData.performanceMetrics.pendingPayments > 5 -> "Varios pagos pendientes - Requiere atención"
                                    else -> "Sistema funcionando correctamente"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (enhancedData.performanceMetrics.rejectionRate > 10) 
                                    MaterialTheme.colorScheme.onErrorContainer 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceMetricItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
        )
    }
}