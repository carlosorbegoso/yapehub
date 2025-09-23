package org.sysarp.project.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.Store
import org.sysarp.project.data.UserProfile
import org.sysarp.project.data.UserRole
import org.sysarp.project.repository.UserProfileRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
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
                    IconButton(onClick = { showAddUser = true }) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header informativo
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Panel de Administración",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Text(
                            text = "Gestiona usuarios y asigna tiendas a vendedores",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // Estadísticas rápidas
            item {
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
                    onAssignStores = { 
                        selectedUser = user
                        showStoreAssignment = true
                    },
                    onEditUser = { 
                        // Edición de usuario pendiente
                    }
                )
            }
            
            // Espacio adicional
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
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

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                fontWeight = FontWeight.Bold,
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
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Información del usuario
                Column(
                    modifier = Modifier.weight(1f)
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
                
                // Botones de acción
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
            
            if (user.role == UserRole.VENDOR && user.assignedStores.isNotEmpty()) {
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
        }
    }
}

@Composable
fun StoreAssignmentDialog(
    user: UserProfile,
    stores: List<Store>,
    userProfileRepository: UserProfileRepository,
    onStoresAssigned: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStores by remember { mutableStateOf(user.assignedStores.toSet()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Asignar Tiendas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Selecciona las tiendas para ${user.name}:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                stores.forEach { store ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = store.id in selectedStores,
                            onCheckedChange = { isChecked ->
                                selectedStores = if (isChecked) {
                                    selectedStores + store.id
                                } else {
                                    selectedStores - store.id
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = store.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onStoresAssigned()
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun AddUserDialog(
    stores: List<Store>,
    onUserAdded: () -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.VENDOR) }
    var selectedStores by remember { mutableStateOf(setOf<String>()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Agregar Usuario",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Rol:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Row {
                    RadioButton(
                        selected = selectedRole == UserRole.ADMIN,
                        onClick = { selectedRole = UserRole.ADMIN }
                    )
                    Text("Administrador")
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    RadioButton(
                        selected = selectedRole == UserRole.VENDOR,
                        onClick = { selectedRole = UserRole.VENDOR }
                    )
                    Text("Vendedor")
                }
                
                if (selectedRole == UserRole.VENDOR) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Tiendas asignadas:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    stores.forEach { store ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = store.id in selectedStores,
                                onCheckedChange = { isChecked ->
                                    selectedStores = if (isChecked) {
                                        selectedStores + store.id
                                    } else {
                                        selectedStores - store.id
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = store.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onUserAdded()
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
