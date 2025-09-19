package org.sysarp.project.ui.components.charts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.sysarp.project.utils.formatDateOnly

/**
 * Componente avanzado y reutilizable para selección de fechas
 * Combina selector de rango y día específico en una interfaz unificada
 * 
 * @param onDateRangeSelected Callback cuando se selecciona un rango de fechas
 * @param onSpecificDateSelected Callback cuando se selecciona un día específico
 * @param modifier Modificador para el componente
 */
@Composable
fun AdvancedDateSelector(
    onDateRangeSelected: (startDate: String?, endDate: String?) -> Unit,
    onSpecificDateSelected: (date: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf<DateSelectorMode>(DateSelectorMode.RANGE) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var specificDate by remember { mutableStateOf<Long?>(null) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título y selector de modo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Selector de Fechas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Selector de modo
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { selectedMode = DateSelectorMode.RANGE },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMode == DateSelectorMode.RANGE) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.size(height = 32.dp, width = 80.dp)
                    ) {
                        Text(
                            text = "Rango",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Button(
                        onClick = { selectedMode = DateSelectorMode.SPECIFIC },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMode == DateSelectorMode.SPECIFIC) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.size(height = 32.dp, width = 80.dp)
                    ) {
                        Text(
                            text = "Día",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // Contenido según el modo seleccionado
            when (selectedMode) {
                DateSelectorMode.RANGE -> {
                    // Selector de rango de fechas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Fecha de inicio
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fecha de inicio",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = { /* TODO: Implementar DatePicker */ },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = startDate?.let { 
                                        val dateString = Instant.fromEpochMilliseconds(it)
                                            .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                        formatDateOnly(dateString)
                                    } ?: "Seleccionar",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                        
                        // Fecha de fin
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fecha de fin",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = { /* TODO: Implementar DatePicker */ },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = endDate?.let { 
                                        val dateString = Instant.fromEpochMilliseconds(it)
                                            .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                        formatDateOnly(dateString)
                                    } ?: "Seleccionar",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                    
                    // Validación de fechas
                    if (startDate != null && endDate != null) {
                        val startDateString = Instant.fromEpochMilliseconds(startDate!!)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                        val endDateString = Instant.fromEpochMilliseconds(endDate!!)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                        
                        if (startDateString > endDateString) {
                            Text(
                                text = "⚠️ La fecha de inicio debe ser anterior a la fecha de fin",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                DateSelectorMode.SPECIFIC -> {
                    // Selector de día específico
                    Column {
                        Text(
                            text = "Seleccionar día",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { /* TODO: Implementar DatePicker */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = specificDate?.let { 
                                    val dateString = Instant.fromEpochMilliseconds(it)
                                        .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                    formatDateOnly(dateString)
                                } ?: "Seleccionar fecha",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        
                        // Información del día seleccionado
                        if (specificDate != null) {
                            val selectedDateString = Instant.fromEpochMilliseconds(specificDate!!)
                                .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                            
                            Text(
                                text = if (selectedDateString == today) {
                                    "📅 Día seleccionado: Hoy"
                                } else {
                                    "📅 Día seleccionado: ${formatDateOnly(Instant.fromEpochMilliseconds(specificDate!!).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString())}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        when (selectedMode) {
                            DateSelectorMode.RANGE -> {
                                val startDateString = startDate?.let { 
                                    Instant.fromEpochMilliseconds(it)
                                        .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                }
                                val endDateString = endDate?.let { 
                                    Instant.fromEpochMilliseconds(it)
                                        .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                }
                                
                                if (startDateString != null && endDateString != null && startDateString <= endDateString) {
                                    onDateRangeSelected(startDateString, endDateString)
                                }
                            }
                            DateSelectorMode.SPECIFIC -> {
                                specificDate?.let { date ->
                                    val dateString = Instant.fromEpochMilliseconds(date)
                                        .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                    onSpecificDateSelected(dateString)
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = when (selectedMode) {
                        DateSelectorMode.RANGE -> startDate != null && endDate != null
                        DateSelectorMode.SPECIFIC -> specificDate != null
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Event,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Aplicar Filtro",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                OutlinedButton(
                    onClick = {
                        startDate = null
                        endDate = null
                        specificDate = null
                        onDateRangeSelected(null, null)
                        onSpecificDateSelected(null)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpiar")
                }
            }
        }
    }
}

/**
 * Modos de selección de fechas
 */
enum class DateSelectorMode {
    RANGE,    // Selección de rango de fechas
    SPECIFIC  // Selección de día específico
}
