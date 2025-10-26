package org.sysarp.project.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.FinancialAnalysisParams

/**
 * Diálogo de filtros para análisis financiero administrativo
 */
@Composable
fun AdminFinancialFilterDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onApply: (FinancialAnalysisParams) -> Unit
) {
    if (isVisible) {
        var include by remember { mutableStateOf("revenue,taxes,commissions") }
        var currency by remember { mutableStateOf("PEN") }
        var taxRate by remember { mutableStateOf(0.18f) }
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "💰 Análisis Financiero",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Configura los parámetros para el análisis financiero administrativo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Selector de inclusión
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Incluir en el análisis:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    onClick = { 
                                        include = if (include.contains("revenue")) {
                                            include.replace("revenue,", "").replace(",revenue", "").replace("revenue", "")
                                        } else {
                                            "$include,revenue".replace(",,", ",").trimStart(',')
                                        }
                                    },
                                    label = { Text("Ingresos") },
                                    selected = include.contains("revenue")
                                )
                                
                                FilterChip(
                                    onClick = { 
                                        include = if (include.contains("taxes")) {
                                            include.replace("taxes,", "").replace(",taxes", "").replace("taxes", "")
                                        } else {
                                            "$include,taxes".replace(",,", ",").trimStart(',')
                                        }
                                    },
                                    label = { Text("Impuestos") },
                                    selected = include.contains("taxes")
                                )
                                
                                FilterChip(
                                    onClick = { 
                                        include = if (include.contains("commissions")) {
                                            include.replace("commissions,", "").replace(",commissions", "").replace("commissions", "")
                                        } else {
                                            "$include,commissions".replace(",,", ",").trimStart(',')
                                        }
                                    },
                                    label = { Text("Comisiones") },
                                    selected = include.contains("commissions")
                                )
                            }
                        }
                    }
                    
                    // Selector de moneda
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Moneda:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    onClick = { currency = "PEN" },
                                    label = { Text("PEN (Soles)") },
                                    selected = currency == "PEN"
                                )
                                
                                FilterChip(
                                    onClick = { currency = "USD" },
                                    label = { Text("USD (Dólares)") },
                                    selected = currency == "USD"
                                )
                            }
                        }
                    }
                    
                    // Slider de tasa de impuesto
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Tasa de Impuesto: ${(taxRate * 100).toInt()}%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Slider(
                                value = taxRate,
                                onValueChange = { taxRate = it },
                                valueRange = 0f..0.5f,
                                steps = 9
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val params = FinancialAnalysisParams(
                            include = include,
                            currency = currency,
                            taxRate = taxRate.toDouble()
                        )
                        onApply(params)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Aplicar Filtros")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
