package org.sysarp.project.ui.common.components.charts.hourly.sales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sysarp.project.data.HourlySalesData
import org.sysarp.project.ui.common.components.charts.hourly.sales.components.HourLabelsOverlay
import org.sysarp.project.ui.common.components.charts.hourly.sales.components.HourlyBarsChart
import org.sysarp.project.ui.common.components.charts.hourly.sales.components.HourlyStatsSummary
import org.sysarp.project.ui.common.components.charts.hourly.sales.dialogs.HourDetailsDialog

/**
 * Gráfico de barras para mostrar ventas por hora del día
 * Refactorizado en componentes modulares y reutilizables
 * 
 * @param hourlySales Lista de datos de ventas por hora
 * @param showCard Si mostrar el componente dentro de un Card
 * @param modifier Modificador para el componente
 */
@Composable
fun HourlySalesChart(
    hourlySales: List<HourlySalesData>?,
    showCard: Boolean = true,
    modifier: Modifier = Modifier
) {
    var selectedHour by remember { mutableStateOf<HourlySalesData?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    val chartContent = @Composable {
        if (hourlySales.isNullOrEmpty()) {
            EmptyState()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (showCard) 16.dp else 0.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Gráfico de barras interactivo con etiquetas
                Box {
                    HourlyBarsChart(
                        hourlySales = hourlySales,
                        onHourSelected = { hourData ->
                            selectedHour = hourData
                            showDetailsDialog = true
                        }
                    )
                    
                    // Etiquetas de horas superpuestas
                    HourLabelsOverlay(hourlySales = hourlySales)
                }

                // Estadísticas resumidas usando componentes reutilizables
                HourlyStatsSummary(hourlySales = hourlySales)
            }
        }
    }

    // Renderizar con o sin Card según el parámetro
    if (showCard) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            chartContent()
        }
    } else {
        Box(modifier = modifier.fillMaxWidth()) {
            chartContent()
        }
    }

    // Diálogo de detalles
    if (showDetailsDialog && selectedHour != null) {
        HourDetailsDialog(
            hourData = selectedHour!!,
            onDismiss = {
                showDetailsDialog = false
                selectedHour = null
            }
        )
    }
}

/**
 * Estado vacío mejorado
 */
@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "📊",
            fontSize = 48.sp
        )
        
        Text(
            text = "No hay datos de ventas por hora",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "Los datos aparecerán aquí cuando estén disponibles",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
