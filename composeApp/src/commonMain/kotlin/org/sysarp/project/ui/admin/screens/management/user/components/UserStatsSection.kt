package org.sysarp.project.ui.admin.screens.management.user.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.Store
import org.sysarp.project.data.UserProfile
import org.sysarp.project.data.UserRole

/**
 * Sección de estadísticas rápidas de usuarios
 */
@Composable
fun UserStatsSection(
    users: List<UserProfile>,
    stores: List<Store>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Total Usuarios",
            value = users.size.toString(),
            icon = Icons.Default.Person,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Vendedores",
            value = users.count { it.role == UserRole.VENDOR }.toString(),
            icon = Icons.Default.Person,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Tiendas",
            value = stores.size.toString(),
            icon = Icons.Default.Info,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Tarjeta de estadística individual
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
