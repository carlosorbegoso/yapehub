package org.sysarp.project.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.ui.common.components.LoadingHandler
import org.sysarp.project.ui.common.components.LoadingMessages
import org.sysarp.project.ui.common.components.launchWithLoading
import org.sysarp.project.ui.common.components.rememberLoadingState
import org.sysarp.project.utils.SecurityUtils
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
    
    // Estado de loading global
    val loadingState = rememberLoadingState()
    
    // Animación de escala del formulario
    val animatedScale by animateFloatAsState(
        targetValue = if (isLoading || loadingState.isLoading) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "formScale"
    )
    
    LoadingHandler(loadingState = loadingState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Header animado
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                )
            ) + fadeIn(animationSpec = tween(800))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Registro de Administrador",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Logo animado
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(
                initialOffsetY = { -it / 2 },
                animationSpec = spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                )
            ) + fadeIn(animationSpec = tween(1000))
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Business,
                    contentDescription = "Admin Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Título animado
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                )
            ) + fadeIn(animationSpec = tween(1200))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "YapeHub Admin",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Configura tu negocio y comienza a gestionar vendedores",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
            
        // Formulario principal con animación
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                )
            ) + fadeIn(animationSpec = tween(1400))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(animatedScale),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                        onValueChange = { 
                            businessName = SecurityUtils.sanitizeInput(it)
                            if (errorMessage.isNotEmpty()) errorMessage = ""
                        },
                        label = { Text("Nombre del negocio") },
                        placeholder = { Text("Ej: Mi Restaurante") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Store,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        isError = businessName.isNotBlank() && businessName.length < 2,
                        supportingText = if (businessName.isNotBlank() && businessName.length < 2) {
                            { Text("El nombre debe tener al menos 2 caracteres", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Tipo de negocio
                    ExposedDropdownMenuBox(
                        expanded = showBusinessTypeDropdown,
                        onExpandedChange = { 
                            showBusinessTypeDropdown = !showBusinessTypeDropdown
                            if (errorMessage.isNotEmpty()) errorMessage = ""
                        }
                    ) {
                        OutlinedTextField(
                            value = if (businessType.isNotEmpty()) getBusinessTypeDisplayName(businessType) else "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de negocio") },
                            placeholder = { Text("Selecciona el tipo de negocio") },
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
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = showBusinessTypeDropdown,
                            onDismissRequest = { showBusinessTypeDropdown = false },
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(4.dp)
                        ) {
                            businessTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = getBusinessTypeDisplayName(type),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 16.sp
                                        ) 
                                    },
                                    onClick = {
                                        businessType = type
                                        showBusinessTypeDropdown = false
                                        if (errorMessage.isNotEmpty()) errorMessage = ""
                                    },
                                    colors = androidx.compose.material3.MenuDefaults.itemColors(
                                        textColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                    
                    // RUC/DNI
                    OutlinedTextField(
                        value = ruc,
                        onValueChange = { 
                            val cleanValue = it.replace(Regex("[^0-9]"), "")
                            ruc = cleanValue
                            if (errorMessage.isNotEmpty()) errorMessage = ""
                        },
                        label = { Text("RUC/DNI") },
                        placeholder = { Text("Ej: 12345678901") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Business,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        isError = ruc.isNotBlank() && !SecurityUtils.isValidRucOrDni(ruc),
                        supportingText = if (ruc.isNotBlank() && !SecurityUtils.isValidRucOrDni(ruc)) {
                            { Text("RUC/DNI debe tener entre 8 y 11 dígitos", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { 
                            email = SecurityUtils.normalizeEmail(it)
                            if (errorMessage.isNotEmpty()) errorMessage = ""
                        },
                        label = { Text("Email") },
                        placeholder = { Text("admin@miempresa.com") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        isError = email.isNotBlank() && !SecurityUtils.isValidEmail(email),
                        supportingText = if (email.isNotBlank() && !SecurityUtils.isValidEmail(email)) {
                            { Text("Formato de email inválido", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            if (errorMessage.isNotEmpty()) errorMessage = ""
                        },
                        label = { Text("Contraseña") },
                        placeholder = { Text("Mínimo 8 caracteres") },
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
                        },
                        isError = password.isNotBlank() && !SecurityUtils.isValidPassword(password),
                        supportingText = if (password.isNotBlank() && !SecurityUtils.isValidPassword(password)) {
                            { Text("Mínimo 8 caracteres, sin caracteres especiales", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Teléfono
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { 
                            phone = SecurityUtils.cleanPhoneNumber(it)
                            if (errorMessage.isNotEmpty()) errorMessage = ""
                        },
                        label = { Text("Teléfono") },
                        placeholder = { Text("Ej: 987654321") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        isError = phone.isNotBlank() && !SecurityUtils.isValidPhone(phone),
                        supportingText = if (phone.isNotBlank() && !SecurityUtils.isValidPhone(phone)) {
                            { Text("Teléfono debe tener entre 9 y 15 dígitos", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Dirección
                    OutlinedTextField(
                        value = address,
                        onValueChange = { 
                            address = SecurityUtils.sanitizeInput(it)
                            if (errorMessage.isNotEmpty()) errorMessage = ""
                        },
                        label = { Text("Dirección") },
                        placeholder = { Text("Av. Principal 123, Lima") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        isError = address.isNotBlank() && address.length < 5,
                        supportingText = if (address.isNotBlank() && address.length < 5) {
                            { Text("La dirección debe tener al menos 5 caracteres", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Nombre de contacto
                    OutlinedTextField(
                        value = contactName,
                        onValueChange = { 
                            contactName = SecurityUtils.sanitizeInput(it)
                            if (errorMessage.isNotEmpty()) errorMessage = ""
                        },
                        label = { Text("Nombre de contacto") },
                        placeholder = { Text("Juan Pérez") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        isError = contactName.isNotBlank() && contactName.length < 2,
                        supportingText = if (contactName.isNotBlank() && contactName.length < 2) {
                            { Text("El nombre debe tener al menos 2 caracteres", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        shape = RoundedCornerShape(12.dp)
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
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Botón de registro
                    Button(
                        onClick = {
                            if (validateForm(businessName, businessType, ruc, email, password, phone, address, contactName)) {
                                errorMessage = ""
                                
                                coroutineScope.launchWithLoading(
                                    loadingState = loadingState,
                                    message = LoadingMessages.REGISTERING,
                                    canDismiss = false,
                                    minDuration = 1000L,
                                    onError = { error ->
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
                                ) {
                                    // Simular pasos del registro para mostrar progreso
                                    loadingState.updateMessage("Validando datos...")
                                    
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
                                            loadingState.updateMessage("Configurando cuenta...")
                                            successMessage = SuccessHandler.Messages.ADMIN_REGISTERED
                                            onRegistrationSuccess()
                                        },
                                        onFailure = { error ->
                                            throw error
                                        }
                                    )
                                }
                            } else {
                                errorMessage = when {
                                    !SecurityUtils.isValidName(businessName) -> "El nombre del negocio es obligatorio y debe tener al menos 2 caracteres"
                                    businessType.isBlank() -> "Debes seleccionar un tipo de negocio"
                                    !SecurityUtils.isValidRucOrDni(ruc) -> "El RUC/DNI es obligatorio y debe tener entre 8 y 11 dígitos"
                                    !SecurityUtils.isValidEmail(email) -> "El email es obligatorio y debe tener un formato válido"
                                    !SecurityUtils.isValidPassword(password) -> "La contraseña es obligatoria y debe tener al menos 8 caracteres sin caracteres especiales"
                                    !SecurityUtils.isValidPhone(phone) -> "El teléfono es obligatorio y debe tener entre 9 y 15 dígitos"
                                    !SecurityUtils.isValidAddress(address) -> "La dirección es obligatoria y debe tener al menos 5 caracteres"
                                    !SecurityUtils.isValidName(contactName) -> "El nombre de contacto es obligatorio y debe tener al menos 2 caracteres"
                                    else -> "Por favor completa todos los campos obligatorios correctamente"
                                }
                            }
                        },
                        enabled = !isLoading && !loadingState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Información adicional animada
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                )
            ) + fadeIn(animationSpec = tween(1600))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Información",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = "• Como administrador podrás gestionar vendedores y sus códigos de afiliación\n" +
                              "• Recibirás notificaciones de todos los pagos Yape realizados\n" +
                              "• Tendrás acceso completo al dashboard de estadísticas y reportes\n" +
                              "• Podrás configurar y personalizar tu negocio",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
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
    return SecurityUtils.isValidName(businessName) && 
           businessType.isNotBlank() &&
           SecurityUtils.isValidRucOrDni(ruc) &&
           SecurityUtils.isValidEmail(email) &&
           SecurityUtils.isValidPassword(password) &&
           SecurityUtils.isValidPhone(phone) &&
           SecurityUtils.isValidAddress(address) &&
           SecurityUtils.isValidName(contactName)
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
