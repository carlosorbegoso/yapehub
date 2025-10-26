package org.sysarp.project.ui.admin.components.dashboard.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onShowAffiliationDialog: () -> Unit,
    onLogout: () -> Unit,
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
                title = "Código QR",
                subtitle = "Generar afiliación",
                icon = Icons.Default.QrCode,
                onClick = onShowAffiliationDialog,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Tercera fila de acciones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminActionCard(
                title = "Sucursales",
                subtitle = "Gestionar ubicaciones",
                icon = Icons.Default.Business,
                onClick = onNavigateToBranchManagement,
                modifier = Modifier.weight(1f)
            )
            
            AdminActionCard(
                title = "Configuración",
                subtitle = "Ajustes del sistema",
                icon = Icons.Default.Settings,
                onClick = onNavigateToSettings,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Cuarta fila de acciones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminActionCard(
                title = "Facturación",
                subtitle = "Suscripciones",
                icon = Icons.Default.AttachMoney,
                onClick = onNavigateToBilling,
                modifier = Modifier.weight(1f)
            )
            
            // Espacio vacío para mantener el balance visual
            Spacer(modifier = Modifier.weight(1f))
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
        
        // Botón de cerrar sesión más prominente
        AdminActionCard(
            title = "Cerrar Sesión",
            subtitle = "Salir del sistema de administración",
            icon = Icons.AutoMirrored.Filled.Logout,
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            isFullWidth = true,
            isLogoutButton = true
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
    isFullWidth: Boolean = false,
    isLogoutButton: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF) // Blanco limpio y profesional
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // Sombra muy sutil
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
                        tint = if (isLogoutButton) MaterialTheme.colorScheme.error else Color(0xFF2196F3), // Rojo para logout, azul para otros
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
                            color = Color(0xFF1F2937) // Gris carbón moderno
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280) // Gris medio elegante
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
                        tint = if (isLogoutButton) MaterialTheme.colorScheme.error else Color(0xFF1976D2), // Rojo para logout, azul para otros
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1F2937) // Gris carbón moderno
                    )
                    
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280) // Gris medio elegante
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