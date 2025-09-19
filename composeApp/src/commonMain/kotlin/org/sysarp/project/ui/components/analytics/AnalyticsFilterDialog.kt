package org.sysarp.project.ui.components.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.sysarp.project.data.*

/**
 * Dialog para configurar parámetros avanzados de analytics
 */
@Composable
fun AnalyticsFilterDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onApply: (AnalyticsParams) -> Unit,
    initialParams: AnalyticsParams = AnalyticsParams()
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🔧 Configuración Avanzada de Analytics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Personaliza los parámetros de análisis para obtener insights más específicos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Configuraciones predefinidas
                    Text(
                        text = "📊 Configuraciones Predefinidas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(listOf(
                            "Análisis Rápido" to AnalyticsConfigs.QUICK_ANALYSIS,
                            "Análisis Detallado" to AnalyticsConfigs.DETAILED_ANALYSIS,
                            "Análisis de Ventas" to AnalyticsConfigs.SALES_ANALYSIS,
                            "Análisis de Rendimiento" to AnalyticsConfigs.PERFORMANCE_ANALYSIS,
                            "Predicción Corto Plazo" to AnalyticsConfigs.SHORT_TERM_FORECAST,
                            "Predicción Largo Plazo" to AnalyticsConfigs.LONG_TERM_FORECAST
                        )) { (name, config) ->
                            ConfigPredefinedCard(
                                name = name,
                                config = config,
                                onClick = { onApply(config.toParams()) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            onClick = { onApply(initialParams) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Aplicar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigPredefinedCard(
    name: String,
    config: AnalyticsConfig,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
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
                text = name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "${config.period.value} • ${config.metric.value} • ${config.confidence.value}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
