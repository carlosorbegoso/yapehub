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

@Composable
fun SellerLoginScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val sellerService = remember { SellerService() }
    val coroutineScope = rememberCoroutineScope()
    
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
                text = "Login de Vendedor",
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
            text = "Inicia sesión con tu número de teléfono",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
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
                // Teléfono
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Número de Teléfono") },
                    placeholder = { Text("Ej: 987654323") },
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
                
                // Login button
                Button(
                    onClick = {
                        if (validateForm(phone)) {
                            isLoading = true
                            errorMessage = ""
                            successMessage = ""
                            
                            coroutineScope.launch {
                                sellerService.loginSellerByPhone(phone.trim()).fold(
                                    onSuccess = { response ->
                                        isLoading = false
                                        successMessage = response.message
                                        onLoginSuccess()
                                    },
                                    onFailure = { error ->
                                        isLoading = false
                                        val errorMsg = error.message ?: "Error desconocido"
                                        errorMessage = when {
                                            errorMsg.contains("404") -> "No se encontró un vendedor con este número de teléfono"
                                            errorMsg.contains("400") -> "Error en los datos enviados: Revisa el número de teléfono"
                                            errorMsg.contains("500") -> "Error del servidor. Intenta más tarde"
                                            errorMsg.contains("network", ignoreCase = true) -> "Error de conexión. Verifica tu internet"
                                            else -> errorMsg
                                        }
                                    }
                                )
                            }
                        } else {
                            errorMessage = when {
                                phone.isBlank() -> "El número de teléfono es obligatorio"
                                phone.length < 8 -> "El número de teléfono debe tener al menos 8 dígitos"
                                else -> "Por favor ingresa un número de teléfono válido"
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = "Iniciar Sesión",
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
            text = "¿No tienes una cuenta de vendedor?",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        TextButton(
            onClick = { /* TODO: Navigate to seller registration */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Afiliarse",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun validateForm(phone: String): Boolean {
    return phone.isNotBlank() && phone.length >= 8
}
