package org.sysarp.project.ui.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.sysarp.project.utils.formatDateOnly

/**
 * Componente reutilizable para seleccionar rangos de fechas
 * 
 * @param onDateRangeSelected Callback cuando se selecciona un rango de fechas
 * @param modifier Modificador para el componente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeSelector(
    onDateRangeSelected: (startDate: String?, endDate: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    
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
            // Título
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Seleccionar Rango de Fechas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Selectores de fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fecha de inicio
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Fecha de inicio",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { showStartDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
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
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Fecha de fin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { showEndDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
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
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val startDateString = startDate?.let { 
                            Instant.fromEpochMilliseconds(it)
                                .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                        }
                        val endDateString = endDate?.let { 
                            Instant.fromEpochMilliseconds(it)
                                .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                        }
                        
                        // Validar que la fecha de inicio sea anterior a la de fin
                        if (startDateString != null && endDateString != null && startDateString <= endDateString) {
                            onDateRangeSelected(startDateString, endDateString)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = startDate != null && endDate != null && 
                             startDate != null && endDate != null &&
                             Instant.fromEpochMilliseconds(startDate!!)
                                 .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString() <=
                             Instant.fromEpochMilliseconds(endDate!!)
                                 .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
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
                        onDateRangeSelected(null, null)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpiar")
                }
            }
        }
    }
    
    // DatePicker para fecha de inicio
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate
        )
        
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startDate = datePickerState.selectedDateMillis
                        showStartDatePicker = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showStartDatePicker = false }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // DatePicker para fecha de fin
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate
        )
        
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endDate = datePickerState.selectedDateMillis
                        showEndDatePicker = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEndDatePicker = false }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Componente para seleccionar un día específico
 * 
 * @param onDateSelected Callback cuando se selecciona una fecha específica
 * @param modifier Modificador para el componente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecificDateSelector(
    onDateSelected: (date: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    
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
            // Título
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
                    text = "Seleccionar Día Específico",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Selector de fecha
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                    Text(
                        text = selectedDate?.let { 
                            val dateString = Instant.fromEpochMilliseconds(it)
                                .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                            formatDateOnly(dateString)
                        } ?: "Seleccionar fecha",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            // Información adicional
            if (selectedDate != null) {
                val selectedDateString = Instant.fromEpochMilliseconds(selectedDate!!)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                
                Text(
                    text = if (selectedDateString == today) {
                        "📅 Día seleccionado: Hoy"
                    } else {
                        "📅 Día seleccionado: ${formatDateOnly(Instant.fromEpochMilliseconds(selectedDate!!).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString())}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        selectedDate?.let { date ->
                            val dateString = Instant.fromEpochMilliseconds(date)
                                .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                            onDateSelected(dateString)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = selectedDate != null
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
                        selectedDate = null
                        onDateSelected(null)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpiar")
                }
            }
        }
    }
    
    // DatePicker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDate = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
