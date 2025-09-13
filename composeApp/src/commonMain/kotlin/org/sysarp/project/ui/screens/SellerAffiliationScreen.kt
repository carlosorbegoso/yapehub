package org.sysarp.project.ui.screens

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.sysarp.project.service.AuthService

@Composable
fun SellerAffiliationScreen(
    authService: AuthService,
    onAffiliationSuccess: () -> Unit,
    onBackPressed: () -> Unit
) {
    var sellerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var affiliationCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var showQRScanner by remember { mutableStateOf(false) }
    var showCodeValidation by remember { mutableStateOf(false) }
    var isValidatingCode by remember { mutableStateOf(false) }
    var showLoginOption by remember { mutableStateOf(false) }
    var isLoggingIn by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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
                text = "Afiliación de Vendedor",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Logo
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = "Seller Logo",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Conéctate con tu administrador",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
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
                // Nombre del vendedor
                OutlinedTextField(
                    value = sellerName,
                    onValueChange = { sellerName = it },
                    label = { Text("Tu nombre completo") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
                
                // Teléfono del vendedor
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Tu número de teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                
                // Código de afiliación
                OutlinedTextField(
                    value = affiliationCode,
                    onValueChange = { affiliationCode = it },
                    label = { Text("Código de afiliación") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Key,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { 
                                if (affiliationCode.isNotEmpty()) {
                                    showCodeValidation = true
                                }
                            },
                            enabled = affiliationCode.isNotEmpty() && !isValidatingCode
                        ) {
                            if (isValidatingCode) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Validar código"
                                )
                            }
                        }
                    }
                )
                
                // Mensajes de error y éxito
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                if (successMessage.isNotEmpty()) {
                    Text(
                        text = successMessage,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Botón de afiliación
                Button(
                    onClick = {
                        if (validateForm(sellerName, phone, affiliationCode)) {
                            isLoading = true
                            errorMessage = ""
                            successMessage = ""
                            
                            // Registrar vendedor usando la API real
                            coroutineScope.launch {
                                try {
                                    val result = authService.registerSeller(
                                        affiliationCode = affiliationCode,
                                        sellerName = sellerName,
                                        phone = phone
                                    )
                                    
                                    result.fold(
                                        onSuccess = { response ->
                                            if (response.success && response.data != null) {
                                                successMessage = "✅ Vendedor afiliado exitosamente"
                                                errorMessage = ""
                                                // Navegar después de un breve delay
                                                kotlinx.coroutines.delay(2000)
                                                onAffiliationSuccess()
                                            } else {
                                                errorMessage = "❌ Error: ${response.message}"
                                                successMessage = ""
                                            }
                                        },
                                        onFailure = { error ->
                                            val errorMsg = error.message ?: "Error desconocido"
                                            
                                            // Si el error indica que el teléfono ya está registrado, ofrecer login
                                            if (errorMsg.contains("ya está registrado") || errorMsg.contains("teléfono") && errorMsg.contains("registrado")) {
                                                errorMessage = "📱 Este teléfono ya está registrado. ¿Quieres hacer login?"
                                                successMessage = ""
                                                // Mostrar botón de login después de un delay
                                                kotlinx.coroutines.delay(1000)
                                                showLoginOption = true
                                            } else {
                                                errorMessage = "❌ Error: $errorMsg"
                                                successMessage = ""
                                            }
                                        }
                                    )
                                } catch (e: Exception) {
                                    errorMessage = "❌ Error: ${e.message}"
                                    successMessage = ""
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            errorMessage = "Por favor completa todos los campos"
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
                            text = "Afiliar Vendedor",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                // Botón de login para vendedores ya registrados
                if (showLoginOption) {
                    Button(
                        onClick = {
                            isLoggingIn = true
                            errorMessage = ""
                            successMessage = ""
                            
                            // Hacer login usando el teléfono
                            coroutineScope.launch {
                                try {
                                    val result = authService.sellerLoginByPhone(phone)
                                    
                                    result.fold(
                                        onSuccess = { response ->
                                            if (response.success && response.data != null) {
                                                successMessage = "✅ Login exitoso"
                                                errorMessage = ""
                                                // Navegar después de un breve delay
                                                kotlinx.coroutines.delay(2000)
                                                onAffiliationSuccess()
                                            } else {
                                                errorMessage = "❌ Error en login: ${response.message}"
                                                successMessage = ""
                                            }
                                        },
                                        onFailure = { error ->
                                            errorMessage = "❌ Error en login: ${error.message}"
                                            successMessage = ""
                                        }
                                    )
                                } catch (e: Exception) {
                                    errorMessage = "❌ Error en login: ${e.message}"
                                    successMessage = ""
                                } finally {
                                    isLoggingIn = false
                                }
                            }
                        },
                        enabled = !isLoggingIn && phone.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        if (isLoggingIn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        } else {
                            Text(
                                text = "📱 Hacer Login con este Teléfono",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
                
                // Botón para escanear QR
                OutlinedButton(
                    onClick = { showQRScanner = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Escanear código QR")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Separador
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "o",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Método alternativo: Código de afiliación
                OutlinedButton(
                    onClick = { 
                        // Mostrar información sobre cómo obtener el código
                        errorMessage = "Contacta a tu administrador para obtener tu código de afiliación único."
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Key,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Usar código de afiliación")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Información sobre métodos de afiliación
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Métodos de afiliación disponibles:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Escanear código QR proporcionado por tu administrador",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• Usar código de afiliación único que te asignó tu administrador",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Información adicional
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Pide a tu administrador que genere un código QR para afiliarte al sistema.",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
    
    // Diálogo para escanear QR
    if (showQRScanner) {
        QRScannerDialog(
            onQRScanned = { qrData ->
                affiliationCode = qrData
                showQRScanner = false
            },
            onDismiss = { showQRScanner = false }
        )
    }
    
    // Diálogo para validar código de afiliación
    if (showCodeValidation) {
        ValidateCodeDialog(
            affiliationCode = affiliationCode,
            onValidated = { isValid ->
                showCodeValidation = false
                if (isValid) {
                    successMessage = "✅ Código válido"
                    errorMessage = ""
                } else {
                    errorMessage = "❌ Código inválido o expirado"
                    successMessage = ""
                }
            },
            onDismiss = { showCodeValidation = false }
        )
    }
}

@Composable
fun QRScannerDialog(
    onQRScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface, // Fondo blanco
        title = {
            Text(
                "Escanear Código QR",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Apunta la cámara al código QR que te proporcionó tu administrador.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Simular escaneo exitoso
                Button(
                    onClick = { 
                        onQRScanned("mock_qr_data_${System.currentTimeMillis()}")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Simular Escaneo")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ValidateCodeDialog(
    affiliationCode: String,
    onValidated: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface, // Fondo blanco
        title = {
            Text(
                "Validar Código",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Validando código...",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Key,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Código: $affiliationCode",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!isLoading) {
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = ""
                        
                        coroutineScope.launch {
                            try {
                                // TODO: Implementar validación real con AuthService
                                // Por ahora simulamos validación
                                kotlinx.coroutines.delay(1000)
                                onValidated(true) // Simular éxito
                            } catch (e: Exception) {
                                errorMessage = "Error al validar código"
                                isLoading = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Validar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

private fun validateForm(
    sellerName: String,
    phone: String,
    affiliationCode: String
): Boolean {
    return sellerName.isNotBlank() && 
           phone.isNotBlank() && 
           affiliationCode.isNotBlank()
}