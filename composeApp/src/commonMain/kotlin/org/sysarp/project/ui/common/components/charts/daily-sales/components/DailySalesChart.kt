package org.sysarp.project.ui.common.components.charts.daily.sales.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.DailySalesData
import org.sysarp.project.ui.common.components.charts.daily.sales.data.analyzeSalesData
import org.sysarp.project.ui.common.components.charts.daily.sales.dialogs.DayDetailsDialog

/**
 * Componente principal del gráfico de ventas diarias
 * Refactorizado para ser más modular y mantenible
 */
@Composable
fun DailySalesChart(
    dailySales: List<DailySalesData>,
    title: String = "Ventas Diarias",
    showCard: Boolean = true,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableStateOf<DailySalesData?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    val chartContent = @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (showCard) 16.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título del gráfico
            if (title != "Ventas Diarias") {
                DailySalesChartTitle(title = title)
            }

            // Análisis de datos
            val analysis = remember(dailySales) { analyzeSalesData(dailySales) }
            val periodLabel = calculatePeriodLabel(dailySales.size)
            
            // Métricas principales
            DailySalesMetricsSection(
                dailySales = dailySales,
                analysis = analysis,
                periodLabel = periodLabel
            )
            
            // Área principal del gráfico
            DailySalesChartArea(
                dailySales = dailySales,
                onDaySelected = { dayData ->
                    selectedDay = dayData
                    showDetailsDialog = true
                }
            )
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
    if (showDetailsDialog && selectedDay != null) {
        DayDetailsDialog(
            dayData = selectedDay!!,
            onDismiss = {
                showDetailsDialog = false
                selectedDay = null
            }
        )
    }
}

@Composable
private fun DailySalesChartTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Analytics,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun calculatePeriodLabel(totalDays: Int): String {
    return when {
        totalDays <= 7 -> "Semanal"
        totalDays <= 14 -> "Quincenal"
        totalDays <= 30 -> "Mensual"
        totalDays <= 60 -> "Bimestral"
        else -> "Período Extendido"
    }
}
