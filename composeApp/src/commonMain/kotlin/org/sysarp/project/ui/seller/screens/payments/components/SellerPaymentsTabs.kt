package org.sysarp.project.ui.seller.screens.payments.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

/**
 * Pestañas de navegación para pagos pendientes y confirmados
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerPaymentsTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Pendientes", "Confirmados")
    
    TabRow(
        selectedTabIndex = selectedTab,
        modifier = androidx.compose.ui.Modifier.fillMaxWidth()
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
                            0 -> Icons.Filled.Schedule
                            else -> Icons.Filled.CheckCircle
                        },
                        contentDescription = title
                    )
                }
            )
        }
    }
}
