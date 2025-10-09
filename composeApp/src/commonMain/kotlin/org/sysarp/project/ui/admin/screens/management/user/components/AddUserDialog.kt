package org.sysarp.project.ui.admin.screens.management.user.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.Store
import org.sysarp.project.data.UserRole

/**
 * Diálogo para agregar un nuevo usuario
 */
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
                // Campo de nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo de email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Selección de rol
                RoleSelectionSection(
                    selectedRole = selectedRole,
                    onRoleSelected = { selectedRole = it }
                )
                
                // Selección de tiendas (solo para vendedores)
                if (selectedRole == UserRole.VENDOR) {
                    StoreSelectionSection(
                        stores = stores,
                        selectedStores = selectedStores,
                        onStoresChanged = { selectedStores = it }
                    )
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

@Composable
private fun RoleSelectionSection(
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit
) {
    Text(
        text = "Rol:",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium
    )
    
    Row {
        RadioButton(
            selected = selectedRole == UserRole.ADMIN,
            onClick = { onRoleSelected(UserRole.ADMIN) }
        )
        Text("Administrador")
        
        Spacer(modifier = Modifier.width(16.dp))
        
        RadioButton(
            selected = selectedRole == UserRole.VENDOR,
            onClick = { onRoleSelected(UserRole.VENDOR) }
        )
        Text("Vendedor")
    }
}

@Composable
private fun StoreSelectionSection(
    stores: List<Store>,
    selectedStores: Set<String>,
    onStoresChanged: (Set<String>) -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = "Tiendas asignadas:",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium
    )
    
    stores.forEach { store ->
        StoreCheckboxItem(
            store = store,
            isChecked = store.id in selectedStores,
            onCheckedChange = { isChecked ->
                onStoresChanged(
                    if (isChecked) {
                        selectedStores + store.id
                    } else {
                        selectedStores - store.id
                    }
                )
            }
        )
    }
}

@Composable
private fun StoreCheckboxItem(
    store: Store,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = store.name,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
