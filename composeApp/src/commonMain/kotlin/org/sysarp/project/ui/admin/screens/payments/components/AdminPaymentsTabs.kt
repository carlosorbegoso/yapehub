package org.sysarp.project.ui.admin.screens.payments.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Pestañas de navegación mejoradas para administrador con filtros
 * Incluye filtro por código Yape y botón de calendario
 */
@Composable
fun AdminPaymentsTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    yapeCodeFilter: String,
    onYapeCodeFilterChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Filtro por código Yape
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = yapeCodeFilter,
                onValueChange = onYapeCodeFilterChanged,
                label = { Text("Código Yape") },
                placeholder = { Text("Ej: YAPE_210") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Buscar"
                    )
                },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
        
        // Tabs para administrador (incluye ALL)
        val tabs = listOf("Todos", "Pendientes", "Confirmados", "Rechazados")
        
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    text = { 
                        Text(
                            title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = when (index) {
                                0 -> Icons.Filled.ListAlt      // Todos
                                1 -> Icons.Filled.Schedule    // Pendientes
                                2 -> Icons.Filled.CheckCircle // Confirmados
                                3 -> Icons.Filled.Warning     // Rechazados
                                else -> Icons.Filled.ListAlt
                            },
                            contentDescription = title
                        )
                    }
                )
            }
        }
    }
}