package org.sysarp.project.ui.admin.components.dashboard.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.viewmodel.admin.EnhancedAdminDashboardData

/**
 * Sección de acciones del administrador
 * Replica el estilo de SellerActionsSection pero con acciones específicas de admin
 */
@Composable
fun AdminActionsSection(
    enhancedData: EnhancedAdminDashboardData?,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSellerManagement: () -> Unit,
    onNavigateToPendingPayments: () -> Unit,
    onNavigateToBranchManagement: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDeactivationRequests: () -> Unit,
    onNavigateToBilling: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Header (mismo estilo que otras secciones)
    Column(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Acciones Rápidas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Gestión y administración del negocio",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
    
    // Acciones principales (estilo similar a las tarjetas del vendedor)
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Primera fila de acciones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminActionCard(
                title = "Analytics",
                subtitle = "Reportes detallados",
                icon = Icons.Default.Analytics,
                onClick = onNavigateToAnalytics,
                modifier = Modifier.weight(1f)
            )
            
            AdminActionCard(
                title = "Vendedores",
                subtitle = "${enhancedData?.topSellers?.size ?: 0} activos",
                icon = Icons.Default.People,
                onClick = onNavigateToSellerManagement,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Segunda fila de acciones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminActionCard(
                title = "Pendientes",
                subtitle = "${enhancedData?.performanceMetrics?.pendingPayments ?: 0} pagos",
                icon = Icons.Default.Schedule,
                onClick = onNavigateToPendingPayments,
                modifier = Modifier.weight(1f),
                showBadge = (enhancedData?.performanceMetrics?.pendingPayments ?: 0) > 0
            )
            
            AdminActionCard(
                title = "Sucursales",
                subtitle = "Gestionar ubicaciones",
                icon = Icons.Default.Business,
                onClick = onNavigateToBranchManagement,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Tercera fila de acciones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminActionCard(
                title = "Configuración",
                subtitle = "Ajustes del sistema",
                icon = Icons.Default.Settings,
                onClick = onNavigateToSettings,
                modifier = Modifier.weight(1f)
            )
            
            AdminActionCard(
                title = "Facturación",
                subtitle = "Suscripciones",
                icon = Icons.Default.AttachMoney,
                onClick = onNavigateToBilling,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Acción adicional para solicitudes de desactivación
        AdminActionCard(
            title = "Solicitudes de Desactivación",
            subtitle = "Revisar solicitudes pendientes",
            icon = Icons.Default.PersonRemove,
            onClick = onNavigateToDeactivationRequests,
            modifier = Modifier.fillMaxWidth(),
            isFullWidth = true
        )
    }
}

@Composable
private fun AdminActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBadge: Boolean = false,
    isFullWidth: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box {
            if (isFullWidth) {
                // Layout para tarjeta de ancho completo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                // Layout para tarjetas normales
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Badge para notificaciones
            if (showBadge) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = 8.dp),
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .padding(2.dp)
                    )
                }
            }
        }
    }
}