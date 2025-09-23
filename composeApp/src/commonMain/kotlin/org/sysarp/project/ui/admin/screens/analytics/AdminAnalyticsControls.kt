package org.sysarp.project.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.sysarp.project.ui.components.calendar.SmartCalendar
import kotlin.time.Duration.Companion.days

/**
 * Componente de controles para AdminAnalyticsScreen
 */
@Composable
fun AdminAnalyticsControls(
    state: AdminAnalyticsState,
    onPeriodChange: (String) -> Unit,
    onLoadAnalytics: (String?, String?) -> Unit,
    onShowFiltersDialog: () -> Unit,
    onShowFinancialFiltersDialog: () -> Unit,
    onShowTransparencyFiltersDialog: () -> Unit,
    onShowPeriodMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Período de análisis",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = state.selectedPeriod,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón de filtros con icono centrado
                IconButton(
                    onClick = onShowFiltersDialog,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = "Filtros",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Botón de calendario profesional con icono centrado
                IconButton(
                    onClick = onShowPeriodMenu,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = "Seleccionar período",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Botón de análisis financiero
                IconButton(
                    onClick = onShowFinancialFiltersDialog,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AttachMoney,
                        contentDescription = "Análisis Financiero",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Botón de transparencia de pagos
                IconButton(
                    onClick = onShowTransparencyFiltersDialog,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Transparencia de Pagos",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // Calendario inteligente reutilizable
        SmartCalendar(
            selectedPeriod = state.selectedPeriod,
            onPeriodSelected = { period ->
                onPeriodChange(period)
                // Cargar analytics cuando se selecciona un período
                when (period) {
                    "📅 Hoy" -> {
                        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                        onLoadAnalytics(today, today)
                    }
                    "📅 7 días" -> {
                        val endDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                        val startDate = Clock.System.now().minus(7.days).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                        onLoadAnalytics(startDate, endDate)
                    }
                    "📅 30 días" -> {
                        val endDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                        val startDate = Clock.System.now().minus(30.days).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                        onLoadAnalytics(startDate, endDate)
                    }
                    "📅 90 días" -> {
                        val endDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                        val startDate = Clock.System.now().minus(90.days).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                        onLoadAnalytics(startDate, endDate)
                    }
                }
            },
            expanded = state.showPeriodMenu,
            onDismiss = { state.showPeriodMenu = false }
        )
    }
}
