package org.sysarp.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.sysarp.project.service.SellerService
import org.sysarp.project.utils.SuccessHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerRegistrationScreen(
    onBackClick: () -> Unit,
    onRegistrationSuccess: () -> Unit
) {
    val sellerService = remember { SellerService() }
    val coroutineScope = rememberCoroutineScope()
    
    var affiliationCode by remember { mutableStateOf("") }
    var sellerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Afiliación de Vendedor",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Logo/Title
        Text(
            text = "YapeChamo",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Subtitle
        Text(
            text = "Afíliate como vendedor con tu código de afiliación",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Código de afiliación
                OutlinedTextField(
                    value = affiliationCode,
                    onValueChange = { affiliationCode = it },
                    label = { Text("Código de Afiliación") },
                    placeholder = { Text("Ej: AFF_1757836011112_E2F6") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                
                // Nombre del vendedor
                OutlinedTextField(
                    value = sellerName,
                    onValueChange = { sellerName = it },
                    label = { Text("Nombre del Vendedor") },
                    placeholder = { Text("Ej: Luis Vendedor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                
                // Teléfono
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
                    placeholder = { Text("Ej: 98765423") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Error message
                if (errorMessage.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = errorMessage,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Success message
                if (successMessage.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = successMessage,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Affiliate button
                Button(
                    onClick = {
                        if (validateForm(affiliationCode, sellerName, phone)) {
                            isLoading = true
                            errorMessage = ""
                            successMessage = ""
                            
                            coroutineScope.launch {
                                sellerService.registerSeller(
                                    affiliationCode = affiliationCode.trim(),
                                    sellerName = sellerName.trim(),
                                    phone = phone.trim()
                                ).fold(
                                    onSuccess = { response ->
                                        isLoading = false
                                        successMessage = response.message
                                        onRegistrationSuccess()
                                    },
                                    onFailure = { error ->
                                        isLoading = false
                                        val errorMsg = error.message ?: "Error desconocido"
                                        errorMessage = when {
                                            errorMsg.contains("422") -> {
                                                val specificError = errorMsg.substringAfter("422 - ").substringAfter("Error en registro de vendedor: ")
                                                if (specificError.isNotEmpty() && specificError != errorMsg) {
                                                    specificError
                                                } else {
                                                    "Error de validación: Verifica que todos los campos estén completos y sean válidos"
                                                }
                                            }
                                            errorMsg.contains("400") -> "Error en los datos enviados: Revisa la información ingresada"
                                            errorMsg.contains("409") -> "El código de afiliación ya fue usado o el teléfono ya está registrado"
                                            errorMsg.contains("500") -> "Error del servidor. Intenta más tarde"
                                            errorMsg.contains("network", ignoreCase = true) -> "Error de conexión. Verifica tu internet"
                                            else -> errorMsg
                                        }
                                    }
                                )
                            }
                        } else {
                            errorMessage = when {
                                affiliationCode.isBlank() -> "El código de afiliación es obligatorio"
                                sellerName.isBlank() -> "El nombre del vendedor es obligatorio"
                                phone.isBlank() -> "El teléfono es obligatorio"
                                phone.length < 8 -> "El teléfono debe tener al menos 8 dígitos"
                                else -> "Por favor completa todos los campos obligatorios"
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = "Registrar Vendedor",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Info text
        Text(
            text = "¿Ya tienes una cuenta de vendedor?",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        TextButton(
            onClick = { /* TODO: Navigate to seller login */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Iniciar Sesión",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun validateForm(
    affiliationCode: String,
    sellerName: String,
    phone: String
): Boolean {
    return affiliationCode.isNotBlank() && 
           sellerName.isNotBlank() &&
           phone.isNotBlank() && phone.length >= 8
}
