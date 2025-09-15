package org.sysarp.project.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.utils.SuccessHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRegistrationScreen(
    authService: AuthService,
    onRegistrationSuccess: () -> Unit,
    onBackPressed: () -> Unit
) {
    var businessName by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("") }
    var ruc by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showBusinessTypeDropdown by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    
    val businessTypes = listOf("RESTAURANT", "RETAIL", "SERVICES", "OTHER")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Registro de Administrador",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Logo
            Icon(
                imageVector = Icons.Filled.Business,
                contentDescription = "Admin Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Configura tu negocio",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Formulario
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Nombre del negocio
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Nombre del negocio") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Store,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    
                    // Tipo de negocio
                    ExposedDropdownMenuBox(
                        expanded = showBusinessTypeDropdown,
                        onExpandedChange = { showBusinessTypeDropdown = !showBusinessTypeDropdown }
                    ) {
                        OutlinedTextField(
                            value = businessType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de negocio") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBusinessTypeDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Business,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = showBusinessTypeDropdown,
                            onDismissRequest = { showBusinessTypeDropdown = false }
                        ) {
                            businessTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(getBusinessTypeDisplayName(type)) },
                                    onClick = {
                                        businessType = type
                                        showBusinessTypeDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // RUC
                    OutlinedTextField(
                        value = ruc,
                        onValueChange = { ruc = it },
                        label = { Text("RUC") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Business,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    
                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    
                    // Contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Visibility,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (showPassword) "Ocultar" else "Mostrar",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                    
                    // Teléfono
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    
                    // Dirección
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Dirección") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    
                    // Nombre de contacto
                    OutlinedTextField(
                        value = contactName,
                        onValueChange = { contactName = it },
                        label = { Text("Nombre de contacto") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    
                    // Mensaje de error
                    if (errorMessage.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Botón de registro
                    Button(
                        onClick = {
                            if (validateForm(businessName, businessType, ruc, email, password, phone, address, contactName)) {
                                isLoading = true
                                errorMessage = ""
                                
                                coroutineScope.launch {
                                    authService.registerAdmin(
                                        businessName = businessName,
                                        businessType = businessType,
                                        ruc = ruc,
                                        email = email,
                                        password = password,
                                        phone = phone,
                                        address = address,
                                        contactName = contactName
                                    ).fold(
                                        onSuccess = { loginData ->
                                            isLoading = false
                                            successMessage = SuccessHandler.Messages.ADMIN_REGISTERED
                                            onRegistrationSuccess()
                                        },
                                        onFailure = { error ->
                                            isLoading = false
                                            val errorMsg = error.message ?: "Error desconocido"
                                            errorMessage = when {
                                                errorMsg.contains("422") -> {
                                                    // Extraer el mensaje específico de validación si está disponible
                                                    val specificError = errorMsg.substringAfter("422 - ").substringAfter("Error en registro de admin: ")
                                                    if (specificError.isNotEmpty() && specificError != errorMsg) {
                                                        specificError
                                                    } else {
                                                        "Error de validación: Verifica que todos los campos estén completos y sean válidos"
                                                    }
                                                }
                                                errorMsg.contains("400") -> "Error en los datos enviados: Revisa la información ingresada"
                                                errorMsg.contains("409") -> "El email ya está registrado. Intenta con otro email"
                                                errorMsg.contains("500") -> "Error del servidor. Intenta más tarde"
                                                errorMsg.contains("network", ignoreCase = true) -> "Error de conexión. Verifica tu internet"
                                                else -> errorMsg
                                            }
                                        }
                                    )
                                }
                            } else {
                                errorMessage = when {
                                    businessName.isBlank() -> "El nombre del negocio es obligatorio"
                                    businessType.isBlank() -> "Debes seleccionar un tipo de negocio"
                                    ruc.isBlank() -> "El RUC es obligatorio"
                                    email.isBlank() -> "El email es obligatorio"
                                    !email.contains("@") -> "El email debe tener un formato válido"
                                    password.isBlank() -> "La contraseña es obligatoria"
                                    password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
                                    phone.isBlank() -> "El teléfono es obligatorio"
                                    address.isBlank() -> "La dirección es obligatoria"
                                    contactName.isBlank() -> "El nombre de contacto es obligatorio"
                                    else -> "Por favor completa todos los campos obligatorios"
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
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = "Registrar Administrador",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Información adicional
            Text(
                text = "Al registrarte como administrador, podrás gestionar vendedores y recibir notificaciones de pagos Yape.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
}

private fun validateForm(
    businessName: String,
    businessType: String,
    ruc: String,
    email: String,
    password: String,
    phone: String,
    address: String,
    contactName: String
): Boolean {
    return businessName.isNotBlank() && 
           businessType.isNotBlank() &&
           ruc.isNotBlank() &&
           email.isNotBlank() && email.contains("@") &&
           password.isNotBlank() && password.length >= 6 &&
           phone.isNotBlank() &&
           address.isNotBlank() &&
           contactName.isNotBlank()
}

private fun getBusinessTypeDisplayName(type: String): String {
    return when (type) {
        "RESTAURANT" -> "Restaurante"
        "RETAIL" -> "Venta al por menor"
        "SERVICES" -> "Servicios"
        "OTHER" -> "Otro"
        else -> type
    }
}
