package org.sysarp.project.ui.components.financial

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.FinancialAnalysisData
import org.sysarp.project.utils.formatCurrency

@Composable
fun FinancialAnalysisCard(
    data: FinancialAnalysisData,
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💰 Análisis Financiero",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = data.currency,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Período
            Text(
                text = "Período: ${data.period.start} - ${data.period.end}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Métricas principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ingresos totales
                FinancialMetricItem(
                    title = "Ingresos Totales",
                    value = formatCurrency(data.totalRevenue),
                    modifier = Modifier.weight(1f)
                )
                
                // Ingresos netos
                FinancialMetricItem(
                    title = "Ingresos Netos",
                    value = formatCurrency(data.netRevenue),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Métricas secundarias
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Transacciones
                FinancialMetricItem(
                    title = "Transacciones",
                    value = "${data.transactions}",
                    subtitle = "${data.confirmedTransactions} confirmadas",
                    modifier = Modifier.weight(1f)
                )
                
                // Valor promedio
                FinancialMetricItem(
                    title = "Valor Promedio",
                    value = formatCurrency(data.averageTransactionValue),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Impuestos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tasa de impuesto
                FinancialMetricItem(
                    title = "Tasa de Impuesto",
                    value = "${(data.taxRate * 100).toInt()}%",
                    modifier = Modifier.weight(1f)
                )
                
                // Monto de impuesto
                FinancialMetricItem(
                    title = "Impuestos",
                    value = formatCurrency(data.taxAmount),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FinancialMetricItem(
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
