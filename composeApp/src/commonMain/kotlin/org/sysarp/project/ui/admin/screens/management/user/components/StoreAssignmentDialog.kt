package org.sysarp.project.ui.admin.screens.management.user.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.Store
import org.sysarp.project.data.UserProfile
import org.sysarp.project.repository.UserProfileRepository

/**
 * Diálogo para asignar tiendas a un usuario
 */
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
                    StoreCheckboxItem(
                        store = store,
                        isChecked = store.id in selectedStores,
                        onCheckedChange = { isChecked ->
                            selectedStores = if (isChecked) {
                                selectedStores + store.id
                            } else {
                                selectedStores - store.id
                            }
                        }
                    )
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
private fun StoreCheckboxItem(
    store: Store,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
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
