package org.sysarp.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import org.sysarp.project.service.AuthService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authService: AuthService,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val userProfile by authService.userProfile.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showStorageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Configuración",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información del perfil
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (userProfile?.role == "admin") Icons.Filled.Business else Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = userProfile?.businessName ?: userProfile?.sellerName ?: "Usuario",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = if (userProfile?.role == "ADMIN") "Administrador" else "Vendedor",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        
                        userProfile?.branchName?.let { branchName ->
                        Text(
                                text = branchName,
                            style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        }
                    }
                }
            }
            
            // Configuración de cuenta
            item {
                Text(
                    text = "Cuenta",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SettingsItem(
                        title = "Información Personal",
                        subtitle = "Editar datos del perfil",
                        icon = Icons.Filled.Person,
                        onClick = { showEditProfileDialog = true }
                    )
                    
                    // Solo mostrar cambio de contraseña para administradores
                    if (userProfile?.role == "ADMIN") {
                        SettingsItem(
                            title = "Cambiar Contraseña",
                            subtitle = "Actualizar contraseña de acceso",
                            icon = Icons.Filled.Lock,
                            onClick = { showChangePasswordDialog = true }
                        )
                    }
                    
                    SettingsItem(
                        title = "Notificaciones",
                        subtitle = "Configurar alertas y sonidos",
                        icon = Icons.Filled.Notifications,
                        onClick = { showNotificationsDialog = true }
                    )
                }
            }
            
            // Configuración de la aplicación
            item {
                            Text(
                    text = "Aplicación",
                    style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SettingsItem(
                        title = "Tema",
                        subtitle = "Claro / Oscuro / Automático",
                        icon = Icons.Filled.Palette,
                        onClick = { showThemeDialog = true }
                    )
                    
                    SettingsItem(
                        title = "Idioma",
                        subtitle = "Español",
                        icon = Icons.Filled.Language,
                        onClick = { showLanguageDialog = true }
                    )
                    
                    SettingsItem(
                        title = "Almacenamiento",
                        subtitle = "Gestionar datos locales",
                        icon = Icons.Filled.Storage,
                        onClick = { showStorageDialog = true }
                    )
                }
            }
            
            // Información y soporte
            item {
                                Text(
                    text = "Información",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SettingsItem(
                        title = "Acerca de",
                        subtitle = "Versión 4.2.0",
                        icon = Icons.Filled.Info,
                        onClick = { showAboutDialog = true }
                    )
                    
                    SettingsItem(
                        title = "Ayuda y Soporte",
                        subtitle = "Centro de ayuda y contacto",
                        icon = Icons.Filled.Help,
                        onClick = { showHelpDialog = true }
                    )
                    
                    SettingsItem(
                        title = "Política de Privacidad",
                        subtitle = "Términos y condiciones",
                        icon = Icons.Filled.Policy,
                        onClick = { showPrivacyDialog = true }
                    )
                }
            }
            
            // Cerrar sesión
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    SettingsItem(
                        title = "Cerrar Sesión",
                        subtitle = "Salir de la aplicación",
                        icon = Icons.Filled.Logout,
                        onClick = { showLogoutDialog = true },
                        textColor = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Espacio adicional
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
    
    // Diálogo de confirmación de logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                        Text(
                    "Cerrar Sesión",
                            fontWeight = FontWeight.Bold
                        )
            },
            text = {
                Text("¿Estás seguro de que quieres cerrar sesión?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cerrar Sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
    
    // Diálogo de edición de perfil
    if (showEditProfileDialog) {
        EditProfileDialog(
            userProfile = userProfile,
            onDismiss = { showEditProfileDialog = false },
            onSave = { updatedProfile ->
                // TODO: Implementar guardado de perfil
                showEditProfileDialog = false
            }
        )
    }
    
    // Diálogo de cambio de contraseña
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onSave = { oldPassword, newPassword ->
                // TODO: Implementar cambio de contraseña
                showChangePasswordDialog = false
            }
        )
    }
    
    // Diálogo de configuración de notificaciones
    if (showNotificationsDialog) {
        NotificationsSettingsDialog(
            onDismiss = { showNotificationsDialog = false },
            onSave = { settings ->
                // TODO: Implementar guardado de configuración de notificaciones
                showNotificationsDialog = false
            }
        )
    }
    
    // Diálogo de configuración de tema
    if (showThemeDialog) {
        ThemeSettingsDialog(
            onDismiss = { showThemeDialog = false },
            onSave = { theme ->
                // TODO: Implementar cambio de tema
                showThemeDialog = false
            }
        )
    }
    
    // Diálogo de configuración de idioma
    if (showLanguageDialog) {
        LanguageSettingsDialog(
            onDismiss = { showLanguageDialog = false },
            onSave = { language ->
                // TODO: Implementar cambio de idioma
                showLanguageDialog = false
            }
        )
    }
    
    // Diálogo de gestión de almacenamiento
    if (showStorageDialog) {
        StorageManagementDialog(
            onDismiss = { showStorageDialog = false },
            onClearData = {
                // TODO: Implementar limpieza de datos
                showStorageDialog = false
            }
        )
    }
    
    // Diálogo de información de la app
    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
    
    // Diálogo de ayuda y soporte
    if (showHelpDialog) {
        HelpSupportDialog(
            onDismiss = { showHelpDialog = false }
        )
    }
    
    // Diálogo de política de privacidad
    if (showPrivacyDialog) {
        PrivacyPolicyDialog(
            onDismiss = { showPrivacyDialog = false }
        )
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                
                            Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    userProfile: org.sysarp.project.service.UserProfile?,
    onDismiss: () -> Unit,
    onSave: (org.sysarp.project.service.UserProfile) -> Unit
) {
    var businessName by remember { mutableStateOf(userProfile?.businessName ?: "") }
    var sellerName by remember { mutableStateOf(userProfile?.sellerName ?: "") }
    var branchName by remember { mutableStateOf(userProfile?.branchName ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Editar Perfil",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (userProfile?.role == "ADMIN") {
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Nombre del negocio") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                } else {
                    OutlinedTextField(
                        value = sellerName,
                        onValueChange = { sellerName = it },
                        label = { Text("Nombre del vendedor") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
                
                OutlinedTextField(
                    value = branchName,
                    onValueChange = { branchName = it },
                    label = { Text("Nombre de sucursal") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                // Solo mostrar para vendedores afiliados
                if (userProfile?.role == "SELLER") {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Nota: Tu código de sucursal no se puede cambiar desde aquí. Contacta al administrador si necesitas cambios.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedProfile = userProfile?.copy(
                        businessName = if (userProfile.role == "ADMIN") businessName else userProfile.businessName,
                        sellerName = if (userProfile.role == "SELLER") sellerName else userProfile.sellerName,
                        branchName = branchName
                    )
                    if (updatedProfile != null) {
                        onSave(updatedProfile)
                    }
                },
                enabled = (businessName.isNotBlank() || sellerName.isNotBlank()) && branchName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPasswords by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
                        Text(
                "Cambiar Contraseña",
                            fontWeight = FontWeight.Bold
                        )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Contraseña actual") },
                    visualTransformation = if (showPasswords) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPasswords = !showPasswords }) {
                            Icon(
                                imageVector = if (showPasswords) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showPasswords) "Ocultar" else "Mostrar"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nueva contraseña") },
                    visualTransformation = if (showPasswords) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar nueva contraseña") },
                    visualTransformation = if (showPasswords) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Las contraseñas no coinciden",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (oldPassword.isNotBlank() && newPassword.isNotBlank() && newPassword == confirmPassword) {
                        onSave(oldPassword, newPassword)
                    }
                },
                enabled = oldPassword.isNotBlank() && newPassword.isNotBlank() && newPassword == confirmPassword,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Cambiar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun NotificationsSettingsDialog(
    onDismiss: () -> Unit,
    onSave: (Map<String, Boolean>) -> Unit
) {
    var pushNotifications by remember { mutableStateOf(true) }
    var emailNotifications by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Configuración de Notificaciones",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = pushNotifications,
                        onCheckedChange = { pushNotifications = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Notificaciones push")
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = emailNotifications,
                        onCheckedChange = { emailNotifications = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Notificaciones por email")
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sonido")
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = vibrationEnabled,
                        onCheckedChange = { vibrationEnabled = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vibración")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val settings = mapOf(
                        "push" to pushNotifications,
                        "email" to emailNotifications,
                        "sound" to soundEnabled,
                        "vibration" to vibrationEnabled
                    )
                    onSave(settings)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun ThemeSettingsDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var selectedTheme by remember { mutableStateOf("Sistema") }
    val themes = listOf("Claro", "Oscuro", "Sistema")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Configuración de Tema",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                themes.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTheme = theme },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTheme == theme,
                            onClick = { selectedTheme = theme }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(theme)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedTheme) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Acerca de YapeChamo",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
                        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                
                                Text(
                    text = "YapeChamo v4.2.0",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                            
                            Text(
                    text = "Sistema de gestión de pagos Yape para negocios",
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Desarrollado con Kotlin Multiplatform y Jetpack Compose",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun HelpSupportDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Ayuda y Soporte",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "¿Necesitas ayuda?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "• Revisa la documentación en la sección de configuración\n• Contacta soporte técnico por email\n• Consulta las preguntas frecuentes\n• Reporta problemas o sugerencias",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Contacto de Soporte",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Email: soporte@yapechamo.com\nTeléfono: +51 999 999 999",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Política de Privacidad",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Última actualización: Diciembre 2024",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Recopilación de Datos",
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Recopilamos información necesaria para el funcionamiento del servicio, incluyendo datos de transacciones, información de contacto y datos de uso de la aplicación."
                )
                
                Text(
                    text = "Uso de la Información",
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Utilizamos tu información para procesar pagos, proporcionar soporte técnico y mejorar nuestros servicios."
                )
                
                Text(
                    text = "Protección de Datos",
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Implementamos medidas de seguridad para proteger tu información personal y financiera."
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Entendido")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

// Diálogos adicionales simplificados
@Composable
fun LanguageSettingsDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Idioma", fontWeight = FontWeight.Bold) },
        text = { Text("Actualmente solo está disponible en español.") },
        confirmButton = { Button(onClick = onDismiss) { Text("OK") } },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun StorageManagementDialog(
    onDismiss: () -> Unit,
    onClearData: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gestión de Almacenamiento", fontWeight = FontWeight.Bold) },
        text = { Text("Esta función permitirá limpiar datos locales y gestionar el espacio de almacenamiento.") },
        confirmButton = { Button(onClick = onDismiss) { Text("OK") } },
        shape = RoundedCornerShape(16.dp)
    )
}