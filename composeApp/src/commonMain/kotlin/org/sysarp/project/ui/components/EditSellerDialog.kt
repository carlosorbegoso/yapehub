package org.sysarp.project.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.admin.AdminService

@Composable
fun EditSellerDialog(
    seller: org.sysarp.project.data.MySeller,
    onDismiss: () -> Unit,
    onSave: (org.sysarp.project.data.MySeller) -> Unit,
    adminService: AdminService,
    authService: AuthService
) {
    var name by remember { mutableStateOf(seller.name) }
    var phone by remember { mutableStateOf(seller.phone) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Editar Vendedor",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (errorMessage.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Nombre"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(8.dp)
                )
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = "Teléfono"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || phone.isBlank()) {
                        errorMessage = "Todos los campos son obligatorios"
                        return@Button
                    }
                    
                    val profile = userProfile
                    if (profile?.adminId != null && accessToken != null) {
                        coroutineScope.launch {
                            isLoading = true
                            errorMessage = ""
                            
                            adminService.updateSeller(
                                sellerId = seller.sellerId,
                                adminId = profile.adminId!!.toInt(),
                                name = name,
                                phone = phone,
                                token = accessToken!!
                            ).fold(
                                onSuccess = { updatedSeller: org.sysarp.project.data.MySeller ->
                                    isLoading = false
                                    onSave(updatedSeller)
                                },
                                onFailure = { error: Throwable ->
                                    isLoading = false
                                    errorMessage = "Error actualizando vendedor: ${error.message}"
                                }
                            )
                        }
                    }
                },
                enabled = !isLoading && name.isNotBlank() && phone.isNotBlank(),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}
