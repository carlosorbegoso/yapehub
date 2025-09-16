package org.sysarp.project.ui.components.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Botones de acción profesionales del dashboard del vendedor
 */
@Composable
fun SellerDashboardActions(
    onNavigateToHistory: () -> Unit,
    onNavigateToPendingPayments: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDeactivationRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp), // Menos padding vertical
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0xFFF8F9FA),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), // Reducido de 20dp a 16dp
                verticalArrangement = Arrangement.spacedBy(12.dp) // Reducido de 16dp a 12dp
            ) {
                Text(
                    text = "Acciones Rápidas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Primera fila de botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfessionalActionButton(
                        title = "Historial",
                        subtitle = "Ver transacciones",
                        icon = Icons.Filled.History,
                        iconColor = Color(0xFF2196F3),
                        backgroundColor = Color(0xFFE3F2FD),
                        onClick = onNavigateToHistory,
                        modifier = Modifier.weight(1f)
                    )
                    
                    ProfessionalActionButton(
                        title = "Pagos",
                        subtitle = "Gestionar",
                        icon = Icons.Filled.Payment,
                        iconColor = Color(0xFF4CAF50),
                        backgroundColor = Color(0xFFE8F5E8),
                        onClick = onNavigateToPendingPayments,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Segunda fila de botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfessionalActionButton(
                        title = "Configuración",
                        subtitle = "Ajustes",
                        icon = Icons.Filled.Settings,
                        iconColor = Color(0xFF9C27B0),
                        backgroundColor = Color(0xFFF3E5F5),
                        onClick = onNavigateToSettings,
                        modifier = Modifier.weight(1f)
                    )
                    
                    ProfessionalActionButton(
                        title = "Desactivar",
                        subtitle = "Solicitar",
                        icon = Icons.Filled.ExitToApp,
                        iconColor = Color(0xFFF44336),
                        backgroundColor = Color(0xFFFFEBEE),
                        onClick = onNavigateToDeactivationRequest,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Botón de acción profesional con diseño mejorado
 */
@Composable
private fun ProfessionalActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "buttonScale"
    )
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                onClick = {
                    isPressed = true
                    onClick()
                }
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}