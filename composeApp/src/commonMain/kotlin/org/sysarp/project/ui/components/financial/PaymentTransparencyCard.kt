package org.sysarp.project.ui.components.financial

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.PaymentTransparencyData
import org.sysarp.project.utils.formatCurrency
import org.sysarp.project.utils.formatPercentage

@Composable
fun PaymentTransparencyCard(
    data: PaymentTransparencyData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con score de transparencia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔍 Transparencia de Pagos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Score de transparencia
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (data.transparencyScore >= 90) 
                            MaterialTheme.colorScheme.primaryContainer
                        else if (data.transparencyScore >= 70)
                            MaterialTheme.colorScheme.secondaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${data.transparencyScore.toInt()}%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Período y última actualización
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Período: ${data.period.start} - ${data.period.end}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Actualizado: ${data.lastUpdated.take(10)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Métricas principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ingresos totales
                TransparencyMetricItem(
                    title = "Ingresos Totales",
                    value = formatCurrency(data.totalRevenue),
                    modifier = Modifier.weight(1f)
                )
                
                // Transacciones
                TransparencyMetricItem(
                    title = "Transacciones",
                    value = "${data.totalTransactions}",
                    subtitle = "${data.confirmedTransactions} confirmadas",
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Comisiones y tasas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Comisión de vendedor
                TransparencyMetricItem(
                    title = "Comisión Vendedor",
                    value = formatCurrency(data.sellerCommissionAmount),
                    subtitle = "${(data.sellerCommissionRate * 100).toInt()}%",
                    modifier = Modifier.weight(1f)
                )
                
                // Tasa de impuesto
                TransparencyMetricItem(
                    title = "Impuestos",
                    value = formatCurrency(data.taxAmount),
                    subtitle = "${(data.taxRate * 100).toInt()}%",
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Tarifas de procesamiento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tarifas de procesamiento
                TransparencyMetricItem(
                    title = "Tarifas Procesamiento",
                    value = formatCurrency(data.processingFees),
                    modifier = Modifier.weight(1f)
                )
                
                // Tarifas de plataforma
                TransparencyMetricItem(
                    title = "Tarifas Plataforma",
                    value = formatCurrency(data.platformFees),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TransparencyMetricItem(
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
