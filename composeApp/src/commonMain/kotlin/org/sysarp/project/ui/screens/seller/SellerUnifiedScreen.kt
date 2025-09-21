package org.sysarp.project.ui.screens.seller

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.sysarp.project.service.SellerService
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.service.websocket.PaymentWebSocketService
import org.sysarp.project.ui.components.ValidationErrorDisplay
import org.sysarp.project.ui.components.dashboard.DashboardAutoRefreshHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerUnifiedScreen(
    sellerService: SellerService,
    authService: AuthService,
    statsService: StatsService,
    webSocketService: PaymentWebSocketService,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    onNavigateToQRScanner: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    
    var affiliationCode by remember { mutableStateOf("") }
    var sellerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    
    var showAdditionalFields by remember { mutableStateOf(false) }
    var isExistingSeller by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (isLoading) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "formScale"
    )
    
    val refreshSellerData: () -> Unit = {
        coroutineScope.launch {
            // Aquí se pueden agregar llamadas para refrescar estadísticas del vendedor
            // Por ejemplo: statsService.getSellerSummary(), etc.
        }
    }
    DashboardAutoRefreshHandler(
        authService = authService,
        statsService = statsService,
        webSocketService = webSocketService,
        onRefreshSellerDashboard = refreshSellerData
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Acceso de Vendedor",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Logo más compacto
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
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Vendedor",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
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
                    text = "YapeChamo",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Completa tus datos para acceder",
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
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header del formulario
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Datos de Acceso",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Indicador de campos completados
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val completedFields = listOf(affiliationCode.isNotBlank(), sellerName.isNotBlank(), phone.isNotBlank()).count { it }
                            Text(
                                text = "$completedFields/3",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = if (completedFields == 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedTextField(
                            value = affiliationCode,
                            onValueChange = { newValue ->
                                val sanitizedValue = newValue.filter { it.isLetterOrDigit() }.take(10)
                                affiliationCode = sanitizedValue
                                if (errorMessage.isNotEmpty()) {
                                    errorMessage = ""
                                }
                            },
                            label = { Text("Código de Afiliación") },
                            placeholder = { Text("Ej: ABC123") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Key,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (affiliationCode.isNotBlank() && affiliationCode.length >= 6) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Válido",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                } else if (affiliationCode.isNotBlank()) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = "Muy corto",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isLoading,
                            isError = affiliationCode.isNotBlank() && affiliationCode.length < 6,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (affiliationCode.isNotBlank() && affiliationCode.length >= 6) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline,
                                errorBorderColor = MaterialTheme.colorScheme.error
                            )
                        )
                        
                        Text(
                            text = "Código de 6 dígitos del administrador",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                        
                        OutlinedButton(
                            onClick = onNavigateToQRScanner,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Filled.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Escanear Código QR",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedTextField(
                            value = sellerName,
                            onValueChange = { newValue ->
                                val sanitizedValue = newValue.filter { it.isLetter() || it == ' ' || it == '-' }.take(50)
                                sellerName = sanitizedValue
                                if (errorMessage.isNotEmpty()) {
                                    errorMessage = ""
                                }
                            },
                            label = { Text("Nombre Completo") },
                            placeholder = { Text("Tu nombre completo") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (sellerName.isNotBlank() && sellerName.length >= 3) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Válido",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                } else if (sellerName.isNotBlank()) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = "Muy corto",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isLoading,
                            isError = sellerName.isNotBlank() && sellerName.length < 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (sellerName.isNotBlank() && sellerName.length >= 3) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline,
                                errorBorderColor = MaterialTheme.colorScheme.error
                            )
                        )
                        
                        Text(
                            text = "Nombre completo como en tu documento",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { newValue ->
                                val sanitizedValue = newValue.filter { it.isDigit() }.take(9)
                                phone = sanitizedValue
                                if (errorMessage.isNotEmpty()) {
                                    errorMessage = ""
                                }
                            },
                            label = { Text("Número de Teléfono") },
                            placeholder = { Text("Ej: 987654321") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Phone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (phone.isNotBlank() && phone.length == 9) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Válido",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                } else if (phone.isNotBlank()) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = "Incompleto",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            isError = phone.isNotBlank() && phone.length != 9,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (phone.isNotBlank() && phone.length == 9) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline,
                                errorBorderColor = MaterialTheme.colorScheme.error
                            )
                        )
                        
                        Text(
                            text = "Número de 9 dígitos (sin código de país)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                    
                    val isFormValid = affiliationCode.length >= 6 && 
                                    sellerName.length >= 3 && 
                                    phone.length == 9 &&
                                    affiliationCode.all { it.isLetterOrDigit() } &&
                                    sellerName.all { it.isLetter() || it == ' ' || it == '-' } &&
                                    phone.all { it.isDigit() }
                    
                    Button(
                        onClick = {
                            // Sanitización adicional antes de enviar
                            val sanitizedAffiliationCode = affiliationCode.trim().uppercase()
                            val sanitizedSellerName = sellerName.trim().replace(Regex("\\s+"), " ")
                            val sanitizedPhone = phone.trim()
                            
                            // Procesar login o registro directamente
                            processSellerAction(
                                coroutineScope = coroutineScope,
                                sellerService = sellerService,
                                authService = authService,
                                affiliationCode = sanitizedAffiliationCode,
                                sellerName = sanitizedSellerName,
                                phone = sanitizedPhone,
                                isExistingSeller = isExistingSeller,
                                onLoadingChange = { isLoading = it },
                                onError = { errorMessage = it },
                                onSuccess = { successMessage = it; onSuccess() }
                            )
                        },
                        enabled = !isLoading && isFormValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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
                                    imageVector = Icons.Filled.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Afiliarse",
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
                                text = "Tus datos están protegidos con validación estricta",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Mensajes de error y éxito
                    if (errorMessage.isNotEmpty()) {
                        ValidationErrorDisplay(errorMessage = errorMessage)
                    }
                    
                    if (successMessage.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = successMessage,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Información adicional con animación
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
                        text = "• Si ya tienes un código de afiliación, ingrésalo para iniciar sesión\n" +
                              "• Si no tienes código, contacta a tu administrador para obtenerlo\n" +
                              "• El código es único y te identifica como vendedor",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

// Función para verificar si el código de afiliación existe
private fun checkAffiliationCode(
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    sellerService: SellerService,
    affiliationCode: String,
    onLoadingChange: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onShowFields: (Boolean) -> Unit
) {
    coroutineScope.launch {
        onLoadingChange(true)
        try {
            // Simular verificación del código
            // En una implementación real, aquí harías una llamada a la API
            kotlinx.coroutines.delay(1000) // Simular delay de red
            
            // Por ahora, asumimos que si el código tiene más de 3 caracteres, existe
            val codeExists = affiliationCode.length > 3
            
            onShowFields(codeExists)
            onLoadingChange(false)
        } catch (e: Exception) {
            onError("Error verificando código: ${e.message}")
            onLoadingChange(false)
        }
    }
}

// Función para procesar registro y login automáticamente
private fun processSellerAction(
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    sellerService: SellerService,
    authService: AuthService,
    affiliationCode: String,
    sellerName: String,
    phone: String,
    isExistingSeller: Boolean,
    onLoadingChange: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onSuccess: (String) -> Unit
) {
    coroutineScope.launch {
        onLoadingChange(true)
        try {
            // Intentar registro primero (tu lógica es correcta)
            val registerResult = sellerService.registerSeller(
                affiliationCode = affiliationCode,
                sellerName = sellerName,
                phone = phone
            )
            
            registerResult.fold(
                onSuccess = { registrationResponse ->
                    // Registro exitoso, el servidor ya devuelve el token directamente
                    onSuccess("¡Registro exitoso! Bienvenido a YapeChamo")
                    onLoadingChange(false)
                },
                onFailure = { registerError ->
                    // Verificar si el error es porque el vendedor ya existe
                    val isSellerAlreadyExists = registerError.message?.let { message ->
                        message.contains("already exists", ignoreCase = true) ||
                        message.contains("duplicate", ignoreCase = true) ||
                        message.contains("phone already registered", ignoreCase = true) ||
                        message.contains("seller already exists", ignoreCase = true) ||
                        message.contains("ya está registrado", ignoreCase = true)
                    } ?: false
                    
                    if (isSellerAlreadyExists) {
                        // El vendedor ya existe, intentar login directamente
                        val loginResult = sellerService.loginSellerByPhone(phone, affiliationCode)
                        
                        loginResult.fold(
                            onSuccess = { loginResponse ->
                                onSuccess("¡Bienvenido de vuelta!")
                                onLoadingChange(false)
                            },
                        onFailure = { loginError ->
                            onError("Error de acceso: ${loginError.message}")
                            onLoadingChange(false)
                        }
                        )
                    } else {
                        // Mostrar directamente el mensaje del servidor
                        onError("Error en el registro: ${registerError.message}")
                        onLoadingChange(false)
                    }
                }
            )
        } catch (e: Exception) {
            onError("Error inesperado: ${e.message}")
            onLoadingChange(false)
        }
    }
}
