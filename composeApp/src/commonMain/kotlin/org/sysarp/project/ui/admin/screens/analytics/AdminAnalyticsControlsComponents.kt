package org.sysarp.project.ui.admin.screens.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.ui.common.components.calendar.SmartCalendar

/**
 * Componentes UI para AdminAnalyticsControls
 */

@Composable
fun AdminAnalyticsControlsContainer(
    state: AdminAnalyticsControlsState,
    onPeriodChange: (String) -> Unit,
    onLoadAnalytics: (String?, String?) -> Unit,
    onShowFiltersDialog: () -> Unit,
    onShowFinancialFiltersDialog: () -> Unit,
    onShowTransparencyFiltersDialog: () -> Unit,
    onShowPeriodMenu: () -> Unit,
    onClosePeriodMenu: () -> Unit,
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
        Column {
            // Controles principales
            AdminAnalyticsControlsRow(
                state = state,
                onShowFiltersDialog = onShowFiltersDialog,
                onShowFinancialFiltersDialog = onShowFinancialFiltersDialog,
                onShowTransparencyFiltersDialog = onShowTransparencyFiltersDialog,
                onShowPeriodMenu = onShowPeriodMenu
            )
            
            // Calendario inteligente
            AdminAnalyticsCalendar(
                state = state,
                onPeriodChange = onPeriodChange,
                onLoadAnalytics = onLoadAnalytics,
                onClosePeriodMenu = onClosePeriodMenu
            )
        }
    }
}

@Composable
fun AdminAnalyticsControlsRow(
    state: AdminAnalyticsControlsState,
    onShowFiltersDialog: () -> Unit,
    onShowFinancialFiltersDialog: () -> Unit,
    onShowTransparencyFiltersDialog: () -> Unit,
    onShowPeriodMenu: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Información del período
        AdminAnalyticsPeriodInfo(state = state)
        
        // Botones de control
        AdminAnalyticsControlButtons(
            onShowFiltersDialog = onShowFiltersDialog,
            onShowFinancialFiltersDialog = onShowFinancialFiltersDialog,
            onShowTransparencyFiltersDialog = onShowTransparencyFiltersDialog,
            onShowPeriodMenu = onShowPeriodMenu
        )
    }
}

@Composable
fun AdminAnalyticsPeriodInfo(
    state: AdminAnalyticsControlsState
) {
    Column {
        Text(
            text = "Período de análisis",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = state.getCurrentPeriodFormatted(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AdminAnalyticsControlButtons(
    onShowFiltersDialog: () -> Unit,
    onShowFinancialFiltersDialog: () -> Unit,
    onShowTransparencyFiltersDialog: () -> Unit,
    onShowPeriodMenu: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Botón de filtros
        AdminAnalyticsControlButton(
            icon = Icons.Filled.FilterList,
            contentDescription = "Filtros",
            onClick = onShowFiltersDialog
        )
        
        // Botón de calendario
        AdminAnalyticsControlButton(
            icon = Icons.Filled.CalendarToday,
            contentDescription = "Seleccionar período",
            onClick = onShowPeriodMenu
        )
        
        // Botón de análisis financiero
        AdminAnalyticsControlButton(
            icon = Icons.Filled.AttachMoney,
            contentDescription = "Análisis Financiero",
            onClick = onShowFinancialFiltersDialog
        )
        
        // Botón de transparencia de pagos
        AdminAnalyticsControlButton(
            icon = Icons.Filled.Settings,
            contentDescription = "Transparencia de Pagos",
            onClick = onShowTransparencyFiltersDialog
        )
    }
}

@Composable
fun AdminAnalyticsControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AdminAnalyticsCalendar(
    state: AdminAnalyticsControlsState,
    onPeriodChange: (String) -> Unit,
    onLoadAnalytics: (String?, String?) -> Unit,
    onClosePeriodMenu: () -> Unit
) {
    SmartCalendar(
        selectedPeriod = state.getCurrentPeriodFormatted(),
        onPeriodSelected = { period ->
            onPeriodChange(period)
            // Cargar analytics cuando se selecciona un período
            val (startDate, endDate) = state.getPeriodDates()
            onLoadAnalytics(startDate, endDate)
        },
        expanded = state.isPeriodMenuVisible(),
        onDismiss = { 
            state.closePeriodMenu()
            onClosePeriodMenu()
        }
    )
}
