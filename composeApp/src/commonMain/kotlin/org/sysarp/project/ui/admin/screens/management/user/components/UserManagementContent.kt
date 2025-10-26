package org.sysarp.project.ui.admin.screens.management.user.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.Store
import org.sysarp.project.data.UserProfile

/**
 * Componente principal de gestión de usuarios
 * Refactorizado para ser más modular y mantenible
 */
@Composable
fun UserManagementContent(
    users: List<UserProfile>,
    stores: List<Store>,
    onAssignStores: (UserProfile) -> Unit,
    onEditUser: (UserProfile) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header informativo
        item {
            UserManagementHeader()
        }
        
        // Estadísticas rápidas
        item {
            UserStatsSection(
                users = users,
                stores = stores
            )
        }
        
        // Lista de usuarios
        item {
            Text(
                text = "Usuarios del Sistema",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        items(users) { user ->
            UserManagementCard(
                user = user,
                stores = stores,
                onAssignStores = { onAssignStores(user) },
                onEditUser = { onEditUser(user) }
            )
        }
        
        // Espacio adicional
        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
