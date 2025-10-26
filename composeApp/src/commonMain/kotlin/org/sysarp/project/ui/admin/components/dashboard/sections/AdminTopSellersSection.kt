package org.sysarp.project.ui.admin.components.dashboard.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.UnifiedTopSellerData
import org.sysarp.project.utils.formatCurrencyNoDecimals

/**
 * Sección de top vendedores del administrador
 * Replica el estilo de las secciones del vendedor pero con información de rankings
 */
@Composable
fun AdminTopSellersSection(
    topSellers: List<UnifiedTopSellerData>,
    onNavigateToSellerManagement: () -> Unit,
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
                    text = "Top Vendedores",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Mejores ${topSellers.size} vendedores del período",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            TextButton(onClick = onNavigateToSellerManagement) {
                Text("Ver Todos")
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
    
    // Lista de top vendedores (estilo similar a las tarjetas del vendedor)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF) // Blanco limpio y profesional
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Sombra sutil
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            topSellers.take(3).forEach { seller ->
                TopSellerItem(
                    seller = seller,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (seller != topSellers.take(3).last()) {
                    Divider(
                        color = Color(0xFFE5E7EB), // Gris muy suave y moderno
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun TopSellerItem(
    seller: UnifiedTopSellerData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ranking badge mejorado con gradiente
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = when (seller.rank) {
                            1 -> Brush.radialGradient(
                                colors = listOf(Color(0xFFFFD700), Color(0xFFFFA000)) // Gradiente dorado
                            )
                            2 -> Brush.radialGradient(
                                colors = listOf(Color(0xFFE8E8E8), Color(0xFFBDBDBD)) // Gradiente plateado
                            )
                            3 -> Brush.radialGradient(
                                colors = listOf(Color(0xFFCD7F32), Color(0xFF8D5524)) // Gradiente bronce
                            )
                            else -> Brush.radialGradient(
                                colors = listOf(Color(0xFF64B5F6), Color(0xFF1976D2)) // Gradiente azul
                            )
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${seller.rank}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Información del vendedor
            Column {
                Text(
                    text = seller.sellerName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937) // Gris carbón moderno
                )
                Text(
                    text = seller.branchName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280) // Gris medio elegante
                )
            }
        }
        
        // Métricas del vendedor con diseño mejorado
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF10B981).copy(alpha = 0.1f)
                ),
                modifier = Modifier.padding(2.dp)
            ) {
                Text(
                    text = formatCurrencyNoDecimals(seller.totalSales),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF059669), // Verde esmeralda profundo
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Color(0xFF6B7280)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${seller.transactionCount} transacciones",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280) // Gris moderno
                )
            }
        }
    }
}