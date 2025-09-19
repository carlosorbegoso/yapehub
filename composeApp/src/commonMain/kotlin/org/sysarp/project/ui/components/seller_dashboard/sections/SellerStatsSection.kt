package org.sysarp.project.ui.components.seller_dashboard.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.ui.components.seller_dashboard.cards.SellerStatCard
import org.sysarp.project.utils.formatCurrencyNoDecimals

/**
 * Sección de estadísticas del vendedor en el dashboard
 * Muestra resumen de ventas confirmadas y total recaudado
 */
@Composable
fun SellerStatsSection(
    confirmedPaymentsCount: Int,
    totalAmountCollected: Double,
    isLoadingStats: Boolean,
    onViewAnalytics: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Header de estadísticas con botón de analytics
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
                    text = "Resumen de Ventas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Estado actual de tus transacciones",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            OutlinedButton(
                onClick = onViewAnalytics,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Analytics,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Analytics")
            }
        }
    }
    
    // Tarjetas de estadísticas
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isLoadingStats) {
            // Estado de carga
            SellerStatCard(
                title = "Confirmados",
                value = "...",
                icon = Icons.Filled.CheckCircle,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            
            SellerStatCard(
                title = "Total Recaudado",
                value = "...",
                icon = Icons.Filled.CheckCircle,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        } else {
            // Datos reales
            SellerStatCard(
                title = "Confirmados",
                value = confirmedPaymentsCount.toString(),
                icon = Icons.Filled.CheckCircle,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            
            SellerStatCard(
                title = "Total Recaudado",
                value = formatCurrencyNoDecimals(totalAmountCollected),
                icon = Icons.Filled.CheckCircle,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
