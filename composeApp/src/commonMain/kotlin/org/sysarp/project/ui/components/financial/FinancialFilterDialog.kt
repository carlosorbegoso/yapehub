package org.sysarp.project.ui.components.financial

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
 * Dialog para configurar parámetros de análisis financiero
 */
@Composable
fun FinancialFilterDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onApplyFinancial: (FinancialAnalysisParams) -> Unit,
    onApplyTransparency: (PaymentTransparencyParams) -> Unit,
    initialFinancialParams: FinancialAnalysisParams = FinancialAnalysisParams(),
    initialTransparencyParams: PaymentTransparencyParams = PaymentTransparencyParams()
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
                        text = "💰 Configuración Financiera",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Configura los parámetros para análisis financiero y transparencia de pagos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Configuraciones predefinidas para análisis financiero
                    Text(
                        text = "📊 Análisis Financiero",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(150.dp)
                    ) {
                        items(listOf(
                            "Perú Estándar" to FinancialConfigs.PERU_STANDARD,
                            "Solo Ingresos" to FinancialConfigs.REVENUE_ONLY,
                            "Solo Impuestos" to FinancialConfigs.TAXES_ONLY,
                            "Solo Comisiones" to FinancialConfigs.COMMISSIONS_ONLY
                        )) { (name, config) ->
                            FinancialConfigCard(
                                name = name,
                                config = config,
                                onClick = { onApplyFinancial(config.toParams()) }
                            )
                        }
                    }

                    // Configuraciones predefinidas para transparencia
                    Text(
                        text = "🔍 Transparencia de Pagos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(150.dp)
                    ) {
                        items(listOf(
                            "Transparencia Completa" to TransparencyConfigs.FULL_TRANSPARENCY,
                            "Solo Tarifas" to TransparencyConfigs.FEES_ONLY,
                            "Solo Impuestos" to TransparencyConfigs.TAXES_ONLY,
                            "Solo Comisiones" to TransparencyConfigs.COMMISSIONS_ONLY
                        )) { (name, config) ->
                            TransparencyConfigCard(
                                name = name,
                                config = config,
                                onClick = { onApplyTransparency(config.toParams()) }
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
                            onClick = { 
                                onApplyFinancial(initialFinancialParams)
                                onApplyTransparency(initialTransparencyParams)
                            },
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
private fun FinancialConfigCard(
    name: String,
    config: FinancialAnalysisConfig,
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
                text = "${config.currency.value} • ${config.taxRate.value} • ${config.include.value}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TransparencyConfigCard(
    name: String,
    config: PaymentTransparencyConfig,
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
            
            val features = mutableListOf<String>()
            if (config.includeFees) features.add("Tarifas")
            if (config.includeTaxes) features.add("Impuestos")
            if (config.includeCommissions) features.add("Comisiones")
            
            Text(
                text = features.joinToString(" • "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
