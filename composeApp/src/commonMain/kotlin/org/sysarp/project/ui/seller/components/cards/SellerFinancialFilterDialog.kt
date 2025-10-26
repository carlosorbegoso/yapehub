package org.sysarp.project.ui.components.financial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.sysarp.project.data.SellerFinancialAnalysisConfig
import org.sysarp.project.data.SellerFinancialAnalysisParams
import org.sysarp.project.data.SellerFinancialConfigs

/**
 * Dialog para configurar parámetros de análisis financiero de vendedores
 */
@Composable
fun SellerFinancialFilterDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onApply: (SellerFinancialAnalysisParams) -> Unit,
    initialParams: SellerFinancialAnalysisParams = SellerFinancialAnalysisParams()
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
                        text = "Configura los parámetros para tu análisis financiero personal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Configuraciones predefinidas
                    Text(
                        text = "📊 Opciones de Análisis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(listOf(
                            "Perú Estándar" to SellerFinancialConfigs.PERU_STANDARD,
                            "Solo Ganancias" to SellerFinancialConfigs.EARNINGS_ONLY,
                            "Solo Comisiones" to SellerFinancialConfigs.COMMISSIONS_ONLY,
                            "Comisión Alta" to SellerFinancialConfigs.HIGH_COMMISSION,
                            "Estándar USA" to SellerFinancialConfigs.USA_STANDARD
                        )) { (name, config) ->
                            SellerFinancialConfigCard(
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
private fun SellerFinancialConfigCard(
    name: String,
    config: SellerFinancialAnalysisConfig,
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
                text = "${config.currency.value} • ${(config.commissionRate * 100).toInt()}% • ${config.include.value}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
