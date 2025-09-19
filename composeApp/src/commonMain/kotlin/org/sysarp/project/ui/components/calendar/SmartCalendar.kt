package org.sysarp.project.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Componente de calendario inteligente y reutilizable
 * Permite selección rápida de períodos predefinidos y opciones avanzadas
 */
@Composable
fun SmartCalendar(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit,
    onCustomRangeSelected: () -> Unit,
    onSpecificDateSelected: () -> Unit,
    expanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Opciones de período específicas para analytics
    val periodOptions = listOf(
        "🕐 Hoy" to 1,      // Hoy
        "📅 7 días" to 7,      // Última semana  
        "📆 30 días" to 30,     // Último mes
        "🗓️ 3 meses" to 90,     // Últimos 3 meses
        "📊 1 año" to 365,    // Último año
        "🎯" to -2,     // Día específico
        "⚙️" to -1      // Rango personalizado
    )
    
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
            .background(Color.White)
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        // Título del calendario con período actual
        Column(
            modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 8.dp)
        ) {
            Text(
                text = "📅 Calendario Inteligente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Período actual: ${getPeriodDateRange(selectedPeriod)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Opciones rápidas (períodos predefinidos) - Layout horizontal compacto
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            periodOptions.filter { it.second > 0 }.take(4).forEach { (period, _) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                color = if (selectedPeriod == period) 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                onPeriodSelected(period)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = period,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = when (period) {
                            "🕐 Hoy" -> "Hoy"
                            "📅 7 días" -> "7 días"
                            "📆 30 días" -> "30 días"
                            "🗓️ 3 meses" -> "3 meses"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
        }
        
        // Más opciones
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            periodOptions.filter { it.second > 0 }.drop(4).forEach { (period, _) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                color = if (selectedPeriod == period) 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                onPeriodSelected(period)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = period,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "1 año",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
        }
        
        // Separador visual elegante
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Gray.copy(alpha = 0.2f))
                .padding(horizontal = 16.dp)
        )
        
        // Opciones avanzadas del calendario
        Text(
            text = "Selección Avanzada",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 4.dp)
        )
        
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Rango personalizado
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            onCustomRangeSelected()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚙️",
                        fontSize = 22.sp,
                        color = Color(0xFF4CAF50)
                    )
                }
                Text(
                    text = "Rango",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
            
            // Día específico
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            color = Color(0xFF2196F3).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            onSpecificDateSelected()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎯",
                        fontSize = 22.sp,
                        color = Color(0xFF2196F3)
                    )
                }
                Text(
                    text = "Día",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 10.sp
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
        else -> "Período no definido"
    }
}
