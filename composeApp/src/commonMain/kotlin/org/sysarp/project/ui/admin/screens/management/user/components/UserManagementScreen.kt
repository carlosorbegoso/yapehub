package org.sysarp.project.ui.admin.screens.management.user.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import org.sysarp.project.data.UserProfile
import org.sysarp.project.repository.UserProfileRepository

/**
 * Componente principal que orquesta toda la gestión de usuarios
 * Refactorizado para ser más modular y mantenible
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreenRefactored(
    userProfileRepository: UserProfileRepository,
    onNavigateBack: () -> Unit
) {
    val users by userProfileRepository.getAllUsers().collectAsState(initial = emptyList())
    val stores by userProfileRepository.getAllStores().collectAsState(initial = emptyList())
    
    var selectedUser by remember { mutableStateOf<UserProfile?>(null) }
    var showStoreAssignment by remember { mutableStateOf(false) }
    var showAddUser by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            UserManagementTopBar(
                onNavigateBack = onNavigateBack,
                onAddUser = { showAddUser = true }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddUser = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Agregar usuario"
                )
            }
        }
    ) { paddingValues ->
        UserManagementContent(
            users = users,
            stores = stores,
            onAssignStores = { user ->
                selectedUser = user
                showStoreAssignment = true
            },
            onEditUser = { user ->
                // Edición de usuario pendiente
            }
        )
    }
    
    // Diálogo para asignar tiendas
    if (showStoreAssignment && selectedUser != null) {
        StoreAssignmentDialog(
            user = selectedUser!!,
            stores = stores,
            userProfileRepository = userProfileRepository,
            onStoresAssigned = { 
                showStoreAssignment = false
                selectedUser = null
            },
            onDismiss = { 
                showStoreAssignment = false
                selectedUser = null
            }
        )
    }
    
    // Diálogo para agregar usuario
    if (showAddUser) {
        AddUserDialog(
            stores = stores,
            onUserAdded = { 
                showAddUser = false
            },
            onDismiss = { showAddUser = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserManagementTopBar(
    onNavigateBack: () -> Unit,
    onAddUser: () -> Unit
) {
    TopAppBar(
        title = { 
            Text(
                "Gestión de Usuarios",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        },
        actions = {
            IconButton(onClick = onAddUser) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Agregar usuario",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
