package org.sysarp.project.ui.admin.screens.management.user.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.Store
import org.sysarp.project.data.UserProfile
import org.sysarp.project.data.UserRole

/**
 * Tarjeta individual de usuario en la gestión
 */
@Composable
fun UserManagementCard(
    user: UserProfile,
    stores: List<Store>,
    onAssignStores: () -> Unit,
    onEditUser: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                UserAvatar(user = user)
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Información del usuario
                UserInfo(user = user)
                
                // Botones de acción
                UserActionButtons(
                    user = user,
                    onAssignStores = onAssignStores,
                    onEditUser = onEditUser
                )
            }
            
            // Tiendas asignadas (solo para vendedores)
            if (user.role == UserRole.VENDOR && user.assignedStores.isNotEmpty()) {
                AssignedStoresSection(
                    user = user,
                    stores = stores
                )
            }
        }
    }
}

@Composable
private fun UserAvatar(user: UserProfile) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (user.role == UserRole.ADMIN) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else 
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                        if (user.role == UserRole.ADMIN) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else 
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (user.role == UserRole.ADMIN) Icons.Default.Settings else Icons.Default.Person,
            contentDescription = null,
            tint = if (user.role == UserRole.ADMIN) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun UserInfo(user: UserProfile) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = user.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = if (user.role == UserRole.ADMIN) "Administrador" else "Vendedor",
            style = MaterialTheme.typography.bodySmall,
            color = if (user.role == UserRole.ADMIN) 
                Color(0xFF4CAF50) 
            else 
                Color(0xFF2196F3),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun UserActionButtons(
    user: UserProfile,
    onAssignStores: () -> Unit,
    onEditUser: () -> Unit
) {
    if (user.role == UserRole.VENDOR) {
        IconButton(onClick = onAssignStores) {
            Icon(
                Icons.Default.Info,
                contentDescription = "Asignar tiendas",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
    
    IconButton(onClick = onEditUser) {
        Icon(
            Icons.Default.Edit,
            contentDescription = "Editar usuario",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AssignedStoresSection(
    user: UserProfile,
    stores: List<Store>
) {
    Spacer(modifier = Modifier.height(12.dp))
    
    Text(
        text = "Tiendas asignadas:",
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    
    Spacer(modifier = Modifier.height(4.dp))
    
    val assignedStores = stores.filter { it.id in user.assignedStores }
    assignedStores.forEach { store ->
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = store.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
