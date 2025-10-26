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
            containerColor = Color(0xFFFEF3C7) // Amarillo dorado muy suave y premium
        )
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
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                        thickness = 1.dp
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
            // Ranking badge
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (seller.rank) {
                        1 -> Color(0xFFFFD700) // Oro brillante
                        2 -> Color(0xFFE8E8E8) // Plata moderna
                        3 -> Color(0xFFCD7F32) // Bronce elegante
                        else -> Color(0xFF2196F3) // Azul moderno
                    }
                ),
                modifier = Modifier.size(32.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "#${seller.rank}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Información del vendedor
            Column {
                Text(
                    text = seller.sellerName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = seller.branchName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
        
        // Métricas del vendedor
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = formatCurrencyNoDecimals(seller.totalSales),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
            Text(
                text = "${seller.transactionCount} transacciones",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}