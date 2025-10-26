package org.sysarp.project.ui.seller.screens.payments.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

/**
 * Barra superior de la pantalla de pagos de vendedor
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerPaymentsTopBar(
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onCalendarClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = { 
            Text(
                "Mis Pagos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Actualizar"
                )
            }
            
            onCalendarClick?.let { calendarClick ->
                IconButton(onClick = calendarClick) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = "Seleccionar período",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
