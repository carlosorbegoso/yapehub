package org.sysarp.project.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import org.sysarp.project.service.CredentialStorageService
import org.sysarp.project.utils.SuccessHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authService: AuthService,
    credentialStorageService: CredentialStorageService,
    onLoginSuccess: (String) -> Unit, // Ahora recibe el rol
    onBackPressed: () -> Unit,
    onForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var rememberPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Cargar credenciales guardadas al inicializar
    LaunchedEffect(Unit) {
        try {
            val savedCredentials = credentialStorageService.getSavedCredentials()
            if (savedCredentials != null && credentialStorageService.areCredentialsValid()) {
                email = savedCredentials.email
                password = savedCredentials.password
                rememberPassword = true
                successMessage = "Credenciales cargadas automáticamente"
            }
        } catch (e: Exception) {
            // Error silencioso al cargar credenciales
        }
    }
    
    // Mostrar mensaje de éxito
    SuccessHandler.ShowSuccessMessage(
        message = successMessage,
        snackbarHostState = snackbarHostState
    )
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Iniciar Sesión",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Logo o icono más compacto
            Card(
                modifier = Modifier.size(80.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(40.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Título más compacto
            Text(
                text = "Acceso de Administrador",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Ingresa tus credenciales para acceder",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Campos de entrada
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header del formulario
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Credenciales de Acceso",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${if (email.isNotBlank() && password.isNotBlank()) "2" else "0"}/2",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = if (email.isNotBlank() && password.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    // Email con validación estricta
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { newValue ->
                                // Validación estricta: solo caracteres válidos para email, máximo 50 caracteres
                                val sanitizedValue = newValue.filter { it.isLetterOrDigit() || it == '@' || it == '.' || it == '_' || it == '-' }.take(50)
                                email = sanitizedValue
                                if (errorMessage.isNotEmpty()) {
                                    errorMessage = ""
                                }
                            },
                            label = { Text("Email") },
                            placeholder = { Text("admin@empresa.com") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (email.isNotBlank() && email.contains("@") && email.contains(".")) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Válido",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                } else if (email.isNotBlank()) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = "Formato inválido",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isLoading,
                            isError = email.isNotBlank() && (!email.contains("@") || !email.contains(".")),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (email.isNotBlank() && email.contains("@") && email.contains(".")) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline,
                                errorBorderColor = MaterialTheme.colorScheme.error
                            )
                        )
                        
                        // Texto de ayuda
                        Text(
                            text = "Email corporativo válido",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                    
                    // Contraseña con validación estricta
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { newValue ->
                                // Validación estricta: solo caracteres alfanuméricos y símbolos seguros, máximo 30 caracteres
                                val sanitizedValue = newValue.filter { it.isLetterOrDigit() || it in "!@#$%^&*()_+-=[]{}|;:,.<>?" }.take(30)
                                password = sanitizedValue
                                if (errorMessage.isNotEmpty()) {
                                    errorMessage = ""
                                }
                            },
                            label = { Text("Contraseña") },
                            placeholder = { Text("Tu contraseña segura") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (password.isNotBlank() && password.length >= 6) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Válida",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else if (password.isNotBlank()) {
                                        Icon(
                                            imageVector = Icons.Filled.Warning,
                                            contentDescription = "Muy corta",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = if (showPassword) "Ocultar" else "Mostrar",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isLoading,
                            isError = password.isNotBlank() && password.length < 6,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (password.isNotBlank() && password.length >= 6) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline,
                                errorBorderColor = MaterialTheme.colorScheme.error
                            )
                        )
                        
                        // Texto de ayuda
                        Text(
                            text = "Mínimo 6 caracteres",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                    
                    // Checkbox para recordar contraseña
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberPassword,
                                onCheckedChange = { rememberPassword = it },
                                enabled = !isLoading
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recordar contraseña",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.clickable { rememberPassword = !rememberPassword }
                            )
                        }
                        
                        // Botón para limpiar credenciales guardadas
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    val cleared = credentialStorageService.clearCredentials()
                                    if (cleared) {
                                        email = ""
                                        password = ""
                                        rememberPassword = false
                                        successMessage = "Credenciales eliminadas"
                                    }
                                }
                            },
                            enabled = !isLoading
                        ) {
                            Text(
                                text = "Limpiar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    // Mensaje de error
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
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                    
                    // Botón de iniciar sesión mejorado
                    val isFormValid = email.isNotBlank() && password.isNotBlank() && 
                                    email.contains("@") && email.contains(".") && 
                                    password.length >= 6
                    
                    Button(
                        onClick = {
                            if (isFormValid) {
                                // Sanitización adicional antes de enviar
                                val sanitizedEmail = email.trim().lowercase()
                                val sanitizedPassword = password.trim()
                                
                                isLoading = true
                                errorMessage = ""
                                
                                // Login real usando la API
                                coroutineScope.launch {
                                    authService.loginAdmin(
                                        email = sanitizedEmail,
                                        password = sanitizedPassword
                                        // deviceFingerprint y role se generan automáticamente
                                    ).fold(
                                        onSuccess = { loginData ->
                                            isLoading = false
                                            successMessage = SuccessHandler.Messages.LOGIN_SUCCESS
                                            
                                            // Guardar credenciales si el usuario marcó "Recordar contraseña"
                                            if (rememberPassword) {
                                                coroutineScope.launch {
                                                    val saved = credentialStorageService.saveCredentials(sanitizedEmail, sanitizedPassword)
                                                    if (saved) {
                                                        successMessage = "Credenciales guardadas exitosamente"
                                                    }
                                                }
                                            } else {
                                                // Eliminar credenciales si el usuario desmarcó la opción
                                                coroutineScope.launch {
                                                    credentialStorageService.clearCredentials()
                                                }
                                            }
                                            
                                            onLoginSuccess(loginData.role)
                                        },
                                        onFailure = { error ->
                                            isLoading = false
                                            errorMessage = error.message ?: "Error desconocido"
                                        }
                                    )
                                }
                            } else {
                                errorMessage = "Por favor completa todos los campos correctamente"
                            }
                        },
                        enabled = !isLoading && isFormValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = if (isFormValid) 4.dp else 0.dp,
                            pressedElevation = if (isFormValid) 8.dp else 0.dp
                        )
                    ) {
                        if (isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Procesando...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Login,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Acceder al Dashboard",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    // Mensaje de seguridad
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Conexión segura y datos protegidos",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Botón de olvidé mi contraseña
                    TextButton(
                        onClick = onForgotPassword,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("¿Olvidaste tu contraseña?")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Información adicional más compacta
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¿No tienes cuenta?",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Regístrate como administrador o afíliate como vendedor",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
