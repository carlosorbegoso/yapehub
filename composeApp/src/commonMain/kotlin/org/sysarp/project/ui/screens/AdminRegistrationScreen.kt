package org.sysarp.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import org.sysarp.project.service.AuthService
import org.sysarp.project.utils.SuccessHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRegistrationScreen(
    authService: AuthService,
    onRegistrationSuccess: () -> Unit,
    onBackPressed: () -> Unit
) {
    var businessName by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("RESTAURANT") }
    var ruc by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Estados de validación por campo
    var businessTypeError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var rucError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    
    // Mostrar mensaje de éxito
    SuccessHandler.ShowSuccessMessage(
        message = successMessage,
        snackbarHostState = snackbarHostState
    )
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
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
        }
        
        item {
            // Logo
            Icon(
                imageVector = Icons.Filled.Business,
                contentDescription = "Admin Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
        }
        
        item {
            Text(
                text = "Configura tu negocio",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        
        item {
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
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    
                    // Tipo de negocio - Dropdown
                    var expanded by remember { mutableStateOf(false) }
                    val businessTypes = listOf(
                        "RESTAURANT" to "Restaurante",
                        "RETAIL" to "Retail/Tienda",
                        "SERVICES" to "Servicios",
                        "OTHER" to "Otro"
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = businessTypes.find { it.first == businessType }?.second ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de negocio") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Category,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            isError = businessTypeError.isNotEmpty(),
                            supportingText = if (businessTypeError.isNotEmpty()) {
                                { Text(businessTypeError, color = MaterialTheme.colorScheme.error) }
                            } else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            businessTypes.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        businessType = value
                                        businessTypeError = ""
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // RUC
                    OutlinedTextField(
                        value = ruc,
                        onValueChange = { 
                            ruc = it
                            rucError = ""
                        },
                        label = { Text("RUC") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = rucError.isNotEmpty(),
                        supportingText = if (rucError.isNotEmpty()) {
                            { Text(rucError, color = MaterialTheme.colorScheme.error) }
                        } else null,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Badge,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    
                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            emailError = ""
                        },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = emailError.isNotEmpty(),
                        supportingText = if (emailError.isNotEmpty()) {
                            { Text(emailError, color = MaterialTheme.colorScheme.error) }
                        } else null,
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
                        onValueChange = { 
                            password = it
                            passwordError = ""
                        },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = passwordError.isNotEmpty(),
                        supportingText = if (passwordError.isNotEmpty()) {
                            { Text(passwordError, color = MaterialTheme.colorScheme.error) }
                        } else null,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                                imageVector = Icons.Filled.LocationOn,
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
                    
                    // Mensaje de error general
                    if (errorMessage.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    // Botón de registro
                    Button(
                        onClick = {
                            // Validar formulario y obtener mensaje específico de error
                            val validationResult = validateFormWithMessage(businessName, businessType, ruc, email, password, phone, address, contactName)
                            
                            if (validationResult.isValid) {
                                isLoading = true
                                errorMessage = ""
                                
                                // Registrar administrador usando la API real
                                coroutineScope.launch {
                                    authService.registerAdmin(
                                        businessName = businessName.trim(),
                                        businessType = businessType, // Ya está en mayúsculas
                                        ruc = ruc.trim(),
                                        email = email.trim(),
                                        password = password,
                                        phone = phone.trim(),
                                        address = address.trim(),
                                        contactName = contactName.trim()
                                    ).fold(
                                        onSuccess = { response ->
                                            isLoading = false
                                            if (response.success) {
                                                successMessage = SuccessHandler.Messages.ADMIN_REGISTERED
                                                onRegistrationSuccess()
                                            } else {
                                                errorMessage = response.message
                                            }
                                        },
                                        onFailure = { error ->
                                            isLoading = false
                                            val errorMsg = error.message ?: "Error desconocido"
                                            
                                            // Manejar errores específicos por campo
                                            when {
                                                errorMsg.contains("Business type must be one of") -> {
                                                    businessTypeError = "Tipo de negocio debe ser: RESTAURANT, RETAIL, SERVICES, OTHER"
                                                }
                                                errorMsg.contains("Invalid email format") -> {
                                                    emailError = "Formato de email inválido"
                                                }
                                                errorMsg.contains("Email already exists") -> {
                                                    emailError = "Este email ya está registrado"
                                                }
                                                errorMsg.contains("RUC") -> {
                                                    rucError = "RUC inválido"
                                                }
                                                errorMsg.contains("Password") -> {
                                                    passwordError = "Contraseña no cumple los requisitos"
                                                }
                                                else -> {
                                                    errorMessage = errorMsg
                                                }
                                            }
                                        }
                                    )
                                }
                            } else {
                                errorMessage = validationResult.errorMessage
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
        }
        
        item {
            // Información adicional
            Text(
                text = "Al registrarte como administrador, podrás gestionar vendedores y recibir notificaciones de pagos Yape.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        }
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String
)

private fun validateFormWithMessage(
    businessName: String,
    businessType: String,
    ruc: String,
    email: String,
    password: String,
    phone: String,
    address: String,
    contactName: String
): ValidationResult {
    val validBusinessTypes = listOf("RESTAURANT", "RETAIL", "SERVICES", "OTHER")
    
    // Validación sin logs de debug
    
    // Validaciones específicas con mensajes
    when {
        businessName.isBlank() -> return ValidationResult(false, "El nombre del negocio es obligatorio")
        businessType.isBlank() -> return ValidationResult(false, "Debes seleccionar un tipo de negocio")
        businessType !in validBusinessTypes -> return ValidationResult(false, "Tipo de negocio inválido. Debe ser: RESTAURANT, RETAIL, SERVICES, OTHER")
        ruc.isBlank() -> return ValidationResult(false, "El RUC es obligatorio")
        ruc.length < 8 -> return ValidationResult(false, "El RUC debe tener al menos 8 caracteres")
        email.isBlank() -> return ValidationResult(false, "El email es obligatorio")
        !email.contains("@") -> return ValidationResult(false, "El formato del email no es válido")
        password.isBlank() -> return ValidationResult(false, "La contraseña es obligatoria")
        password.length < 8 -> return ValidationResult(false, "La contraseña debe tener al menos 8 caracteres")
        phone.isBlank() -> return ValidationResult(false, "El teléfono es obligatorio")
        address.isBlank() -> return ValidationResult(false, "La dirección es obligatoria")
        contactName.isBlank() -> return ValidationResult(false, "El nombre de contacto es obligatorio")
    }
    
    return ValidationResult(true, "")
}