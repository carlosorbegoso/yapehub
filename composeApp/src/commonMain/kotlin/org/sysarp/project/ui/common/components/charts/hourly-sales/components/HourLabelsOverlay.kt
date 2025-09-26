package org.sysarp.project.ui.common.components.charts.hourly.sales.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sysarp.project.data.HourlySalesData

/**
 * Componente para mostrar etiquetas de horas superpuestas en el gráfico
 */
@Composable
fun HourLabelsOverlay(
    hourlySales: List<HourlySalesData>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        hourlySales.forEachIndexed { index, hourData ->
            if (index % 3 == 0) { // Mostrar cada 3 horas
                Text(
                    text = hourData.hour.substring(0, 2),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            } else {
                // Espacio vacío para mantener alineación
                Spacer(modifier = Modifier.width(0.dp))
            }
        }
    }
}
