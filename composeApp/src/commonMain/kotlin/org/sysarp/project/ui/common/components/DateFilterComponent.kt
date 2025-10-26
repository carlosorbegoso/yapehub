package org.sysarp.project.ui.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.sysarp.project.ui.common.components.calendar.SmartCalendar
import kotlin.time.ExperimentalTime

/**
 * Componente reutilizable para filtros de fechas
 * Proporciona una interfaz consistente para seleccionar rangos de fechas
 */
@OptIn(ExperimentalTime::class)
@Composable
fun DateFilterComponent(
    selectedDateRange: String,
    onDateRangeSelected: (String) -> Unit,
    showCalendar: Boolean,
    onShowCalendar: () -> Unit,
    onDismissCalendar: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Filtrar por fecha",
    description: String = "Selecciona un período para filtrar los datos"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Título del filtro
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Rango seleccionado actual
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Período actual:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = selectedDateRange,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(
                    onClick = onShowCalendar,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = "Abrir calendario para cambiar período",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        
        // Información adicional
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
    
    // Calendario inteligente
    SmartCalendar(
        selectedPeriod = selectedDateRange,
        onPeriodSelected = onDateRangeSelected,
        expanded = showCalendar,
        onDismiss = onDismissCalendar
    )
}

/**
 * Versión compacta del componente de filtro de fechas
 * Para usar en espacios más pequeños como toolbars o cards
 */
@Composable
fun CompactDateFilterComponent(
    selectedDateRange: String,
    onDateRangeSelected: (String) -> Unit,
    showCalendar: Boolean,
    onShowCalendar: () -> Unit,
    onDismissCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Rango actual
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Período:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = selectedDateRange,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Botón compacto
        IconButton(
            onClick = onShowCalendar,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = "Cambiar período",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
    
    // Calendario inteligente
    SmartCalendar(
        selectedPeriod = selectedDateRange,
        onPeriodSelected = onDateRangeSelected,
        expanded = showCalendar,
        onDismiss = onDismissCalendar
    )
}

/**
 * Hook para manejar el estado del filtro de fechas
 * Proporciona lógica común para todos los componentes que usan filtros de fechas
 */
@OptIn(ExperimentalTime::class)
@Composable
fun rememberDateFilterState(
    initialStartDate: String? = null,
    initialEndDate: String? = null,
    initialSelectedRange: String = "📅 30 días"
): DateFilterState {
    val now = kotlin.time.Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val lastMonth = today.minus(30, DateTimeUnit.DAY)
    
    var startDate by remember { mutableStateOf(initialStartDate ?: lastMonth.toString()) }
    var endDate by remember { mutableStateOf(initialEndDate ?: today.toString()) }
    var selectedDateRange by remember { mutableStateOf(initialSelectedRange) }
    var showCalendarDialog by remember { mutableStateOf(false) }
    
    return DateFilterState(
        startDate = startDate,
        endDate = endDate,
        selectedDateRange = selectedDateRange,
        showCalendarDialog = showCalendarDialog,
        onStartDateChange = { startDate = it },
        onEndDateChange = { endDate = it },
        onSelectedRangeChange = { selectedDateRange = it },
        onShowCalendar = { showCalendarDialog = true },
        onDismissCalendar = { showCalendarDialog = false },
        onDateRangeSelected = { period ->
            selectedDateRange = period
            calculateDateRange(period) { newStart, newEnd ->
                startDate = newStart
                endDate = newEnd
            }
        }
    )
}

/**
 * Estado del filtro de fechas
 */
data class DateFilterState(
    val startDate: String,
    val endDate: String,
    val selectedDateRange: String,
    val showCalendarDialog: Boolean,
    val onStartDateChange: (String) -> Unit,
    val onEndDateChange: (String) -> Unit,
    val onSelectedRangeChange: (String) -> Unit,
    val onShowCalendar: () -> Unit,
    val onDismissCalendar: () -> Unit,
    val onDateRangeSelected: (String) -> Unit
)

/**
 * Calcula las fechas de inicio y fin basado en el período seleccionado
 */
@OptIn(ExperimentalTime::class)
private fun calculateDateRange(
    period: String,
    onDatesCalculated: (startDate: String, endDate: String) -> Unit
) {
    val now = kotlin.time.Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    
    when (period) {
        "📅 Hoy" -> {
            onDatesCalculated(today.toString(), today.toString())
        }
        "📅 7 días" -> {
            val startDate = today.minus(7, DateTimeUnit.DAY)
            onDatesCalculated(startDate.toString(), today.toString())
        }
        "📅 30 días" -> {
            val startDate = today.minus(30, DateTimeUnit.DAY)
            onDatesCalculated(startDate.toString(), today.toString())
        }
        "📅 90 días" -> {
            val startDate = today.minus(90, DateTimeUnit.DAY)
            onDatesCalculated(startDate.toString(), today.toString())
        }
        else -> {
            // Para rangos personalizados, parsear las fechas
            if (period.contains(" - ")) {
                val parts = period.split(" - ")
                if (parts.size == 2) {
                    onDatesCalculated(parts[0].trim(), parts[1].trim())
                }
            } else {
                // Fecha específica
                onDatesCalculated(period, period)
            }
        }
    }
}
