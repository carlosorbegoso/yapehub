package org.sysarp.project.ui.components.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Componente de calendario inteligente y elegante
 * Permite selección visual directa de rangos de fechas con navegación por meses
 */
@Composable
fun SmartCalendar(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit,
    expanded: Boolean,
    onDismiss: () -> Unit
) {
    var selectedStartDate by remember(expanded) { mutableStateOf<LocalDate?>(null) }
    var selectedEndDate by remember(expanded) { mutableStateOf<LocalDate?>(null) }
    var currentMonth by remember(expanded) { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date) }
    
    // Resetear estado cuando se abre el calendario
    LaunchedEffect(expanded) {
        if (expanded) {
            selectedStartDate = null
            selectedEndDate = null
            currentMonth = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        }
    }
    
    AnimatedVisibility(
        visible = expanded,
        enter = scaleIn(
            animationSpec = tween(300),
            initialScale = 0.8f
        ) + fadeIn(animationSpec = tween(300)),
        exit = scaleOut(
            animationSpec = tween(200),
            targetScale = 0.8f
        ) + fadeOut(animationSpec = tween(200))
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            // Calendario visual directo como diálogo - sin contenedores extra
            VisualCalendarSection(
                currentMonth = currentMonth,
                onMonthChange = { currentMonth = it },
                selectedStartDate = selectedStartDate,
                selectedEndDate = selectedEndDate,
                onDateSelected = { date ->
                    when {
                        selectedStartDate == null -> {
                            selectedStartDate = date
                            selectedEndDate = null
                        }
                        selectedEndDate == null -> {
                            if (date >= selectedStartDate!!) {
                                selectedEndDate = date
                            } else {
                                selectedStartDate = date
                                selectedEndDate = null
                            }
                        }
                        else -> {
                            selectedStartDate = date
                            selectedEndDate = null
                        }
                    }
                },
                onApplyRange = {
                    // Permitir aplicar con una sola fecha o con rango
                    if (selectedStartDate != null) {
                        val periodText = if (selectedEndDate != null) {
                            "${selectedStartDate!!} - ${selectedEndDate!!}"
                        } else {
                            selectedStartDate!!.toString()
                        }
                        onPeriodSelected(periodText)
                        // Cerrar el diálogo después de aplicar
                        onDismiss()
                    }
                },
                onPeriodSelected = onPeriodSelected,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun VisualCalendarSection(
    currentMonth: LocalDate,
    onMonthChange: (LocalDate) -> Unit,
    selectedStartDate: LocalDate?,
    selectedEndDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onApplyRange: () -> Unit,
    onPeriodSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botones de período rápido
            QuickPeriodButtons(
                onPeriodSelected = { period ->
                    onPeriodSelected(period)
                    // Cerrar el diálogo después de seleccionar período rápido
                    onDismiss()
                }
            )
            
            // Header del mes
            MonthHeader(
                currentMonth = currentMonth,
                onMonthChange = onMonthChange
            )
            
            // Calendario visual
            VisualCalendarGrid(
                currentMonth = currentMonth,
                selectedStartDate = selectedStartDate,
                selectedEndDate = selectedEndDate,
                onDateSelected = onDateSelected
            )
            
            // Botón aplicar - se activa con una fecha o con rango
            AnimatedVisibility(
                visible = selectedStartDate != null,
                enter = slideInVertically(
                    animationSpec = tween(300),
                    initialOffsetY = { it }
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    animationSpec = tween(200),
                    targetOffsetY = { it }
                ) + fadeOut(animationSpec = tween(200))
            ) {
                Button(
                    onClick = onApplyRange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedEndDate != null) "Aplicar Rango" else "Aplicar Fecha")
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(
    currentMonth: LocalDate,
    onMonthChange: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onMonthChange(currentMonth.minus(1, DateTimeUnit.MONTH)) }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Mes anterior",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Text(
            text = "${currentMonth.monthNumber}/${currentMonth.year}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        IconButton(
            onClick = { onMonthChange(currentMonth.plus(1, DateTimeUnit.MONTH)) }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Mes siguiente",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun VisualCalendarGrid(
    currentMonth: LocalDate,
    selectedStartDate: LocalDate?,
    selectedEndDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val firstDayOfMonth = LocalDate(currentMonth.year, currentMonth.monthNumber, 1)
    val lastDayOfMonth = currentMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal
    val daysInMonth = lastDayOfMonth.dayOfMonth
    
    Column {
        // Días de la semana
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("L", "M", "X", "J", "V", "S", "D").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.size(32.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Días del mes
        var dayCounter = 1
        repeat(6) { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(7) { dayOfWeek ->
                    if (week == 0 && dayOfWeek < firstDayOfWeek) {
                        // Espacios vacíos antes del primer día del mes
                        Spacer(modifier = Modifier.size(32.dp))
                    } else if (dayCounter <= daysInMonth) {
                        val date = LocalDate(currentMonth.year, currentMonth.monthNumber, dayCounter)
                        val isSelected = date == selectedStartDate || date == selectedEndDate
                        val isInRange = selectedStartDate != null && selectedEndDate != null && 
                                       date > selectedStartDate && date < selectedEndDate
                        val isToday = date == today
                        
                        CalendarDay(
                            day = dayCounter.toString(),
                            isSelected = isSelected,
                            isInRange = isInRange,
                            isToday = isToday,
                            onClick = { onDateSelected(date) }
                        )
                        dayCounter++
                    } else {
                        Spacer(modifier = Modifier.size(32.dp))
                    }
                }
            }
            if (week < 5) Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun CalendarDay(
    day: String,
    isSelected: Boolean,
    isInRange: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isInRange -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else -> Color.Transparent
    }
    
    val textColor = when {
        isSelected -> Color.White
        isToday -> MaterialTheme.colorScheme.primary
        else -> Color.Black
    }
    
    // Animación de escala para la selección
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = tween(200),
        label = "dayScale"
    )
    
    Box(
        modifier = Modifier
            .size(32.dp)
            .scale(scale)
            .background(
                backgroundColor,
                CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}
@Composable
private fun QuickPeriodButtons(
    onPeriodSelected: (String) -> Unit
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Períodos rápidos",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Botón Hoy
            Button(
                onClick = {
                    onPeriodSelected("📅 Hoy")
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                contentPadding = PaddingValues(8.dp)
            ) {
                Text(
                    text = "📅 Hoy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Botón 7 días
            Button(
                onClick = {
                    onPeriodSelected("📅 7 días")
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                contentPadding = PaddingValues(8.dp)
            ) {
                Text(
                    text = "📅 7 días",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Botón 30 días
            Button(
                onClick = {
                    onPeriodSelected("📅 30 días")
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                contentPadding = PaddingValues(8.dp)
            ) {
                Text(
                    text = "📅 30 días",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Botón 90 días
            Button(
                onClick = {
                    onPeriodSelected("📅 90 días")
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                contentPadding = PaddingValues(8.dp)
            ) {
                Text(
                    text = "📅 90 días",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Función para obtener el rango de fechas del período seleccionado
 */
private fun getPeriodDateRange(period: String): String {
    val now = Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    
    return when (period) {
        "🕐 Hoy" -> {
            val todayStr = today.toString()
            "Hoy: $todayStr"
        }
        "📅 7 días" -> {
            val endDate = today.toString()
            "Últimos 7 días hasta $endDate"
        }
        "📆 30 días" -> {
            val endDate = today.toString()
            "Últimos 30 días hasta $endDate"
        }
        "🗓️ 3 meses" -> {
            val endDate = today.toString()
            "Últimos 3 meses hasta $endDate"
        }
        "📊 1 año" -> {
            val endDate = today.toString()
            "Último año hasta $endDate"
        }
        "⚙️ Rango personalizado" -> "Selecciona fechas específicas"
        "🎯 Día específico" -> "Selecciona un día específico"
        else -> {
            if (period.contains(" - ")) {
                "Rango: $period"
            } else {
                "Período no definido"
            }
        }
    }
}
