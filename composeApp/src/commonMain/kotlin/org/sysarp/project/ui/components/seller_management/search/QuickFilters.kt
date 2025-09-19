package org.sysarp.project.ui.components.seller_management.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuickFilters(
    filterActive: Boolean?,
    onFilterActiveChange: (Boolean?) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Filtro: Todos
            FilterChip(
                onClick = { onFilterActiveChange(null) },
                label = { Text("Todos", fontSize = 12.sp) },
                selected = filterActive == null,
                leadingIcon = if (filterActive == null) {
                    { Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) }
                } else null,
                shape = RoundedCornerShape(8.dp)
            )
            
            // Filtro: Activos
            FilterChip(
                onClick = { onFilterActiveChange(true) },
                label = { Text("Activos", fontSize = 12.sp) },
                selected = filterActive == true,
                leadingIcon = if (filterActive == true) {
                    { Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) }
                } else null,
                shape = RoundedCornerShape(8.dp)
            )
            
            // Filtro: Inactivos
            FilterChip(
                onClick = { onFilterActiveChange(false) },
                label = { Text("Inactivos", fontSize = 12.sp) },
                selected = filterActive == false,
                leadingIcon = if (filterActive == false) {
                    { Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) }
                } else null,
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}
