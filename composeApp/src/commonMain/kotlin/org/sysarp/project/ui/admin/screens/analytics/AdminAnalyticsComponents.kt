package org.sysarp.project.ui.screens.admin

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.AnalyticsOverview
import org.sysarp.project.utils.formatCurrency

/**
 * Componente para mostrar las métricas principales del administrador
 */
@Composable
fun AdminPrimaryMetricsSection(
    data: AnalyticsOverview,
    modifier: Modifier = Modifier
) {
    // Animación simple para el componente principal
    val scaleAnimation = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        scaleAnimation.animateTo(1f, tween(800))
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card principal de ventas - Diseño limpio y moderno
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(
                    scaleX = 0.9f + (0.1f * scaleAnimation.value),
                    scaleY = 0.9f + (0.1f * scaleAnimation.value),
                    alpha = scaleAnimation.value
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header simplificado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ventas Totales",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Badge de estado simplificado
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Creciendo",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // Valor principal centrado
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatCurrency(data.totalSales),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "${data.totalTransactions} transacciones",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

    }
}

/**
 * Componente para mostrar métricas adicionales del administrador
 */
@Composable
fun AdminAdditionalMetricsCard(
    data: AnalyticsOverview,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📈 Métricas Administrativas Adicionales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Grid de métricas adicionales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Métrica 1: Transacciones confirmadas
                AdminMetricItem(
                    title = "Confirmadas",
                    value = "${data.totalTransactions}",
                    subtitle = "total",
                    modifier = Modifier.weight(1f)
                )
                
                // Métrica 2: Tasa de éxito
                AdminMetricItem(
                    title = "Tasa de Éxito",
                    value = "95%",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Métrica 3: Tiempo promedio
                AdminMetricItem(
                    title = "Tiempo Promedio",
                    value = "2.5min",
                    modifier = Modifier.weight(1f)
                )
                
                // Métrica 4: Satisfacción
                AdminMetricItem(
                    title = "Satisfacción",
                    value = "4.8/5",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Métrica 5: Vendedores activos
                AdminMetricItem(
                    title = "Vendedores Activos",
                    value = "12",
                    modifier = Modifier.weight(1f)
                )
                
                // Métrica 6: Sucursales
                AdminMetricItem(
                    title = "Sucursales",
                    value = "3",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AdminMetricItem(
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    animationProgress: Float = 1f
) {
    Card(
        modifier = modifier.graphicsLayer(
            scaleX = 0.8f + (0.2f * animationProgress),
            scaleY = 0.8f + (0.2f * animationProgress),
            alpha = animationProgress
        ),
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
