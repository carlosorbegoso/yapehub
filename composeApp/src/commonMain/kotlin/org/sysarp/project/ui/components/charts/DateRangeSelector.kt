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
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título simplificado
            Text(
                text = "📅 Seleccionar Rango de Fechas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            // Instrucciones simplificadas
            Text(
                text = "Selecciona fechas de inicio y fin para el análisis",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            // Selectores de fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fecha de inicio mejorada
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "📅 Fecha de inicio",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showStartDatePicker = true },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = startDate?.let { 
                                    val dateString = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                    formatDateOnly(dateString)
                                } ?: "Seleccionar fecha",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (startDate != null) Color.Black else Color.Gray
                            )
                        }
                    }
                }
                
                // Fecha de fin mejorada
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "📅 Fecha de fin",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEndDatePicker = true },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = endDate?.let { 
                                    val dateString = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                    formatDateOnly(dateString)
                                } ?: "Seleccionar fecha",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (endDate != null) Color.Black else Color.Gray
                            )
                        }
                    }
                }
            }
            
            // Validación de fechas
            if (startDate != null && endDate != null) {
                val startDateString = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                val endDateString = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                
                if (startDateString > endDateString) {
                    Text(
                        text = "⚠️ La fecha de inicio debe ser anterior a la fecha de fin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            
            // Botones de acción mejorados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val startDateString = startDate?.let { 
                            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                        }
                        val endDateString = endDate?.let { 
                            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                        }
                        
                        // Validar que la fecha de inicio sea anterior a la de fin
                        if (startDateString != null && endDateString != null && startDateString <= endDateString) {
                            onDateRangeSelected(startDateString, endDateString)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = startDate != null && endDate != null && 
                             startDate != null && endDate != null &&
                             Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString() <=
                             Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Event,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "✅ Aplicar",
                        modifier = Modifier.padding(start = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                OutlinedButton(
                    onClick = {
                        startDate = null
                        endDate = null
                        onDateRangeSelected(null, null)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "❌ Cancelar",
                        fontWeight = FontWeight.Bold
                    )
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
