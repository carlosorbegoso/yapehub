package org.sysarp.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.AdminProfileData
import org.sysarp.project.data.UpdateAdminProfileRequest
import org.sysarp.project.service.admin.AdminService
import org.sysarp.project.service.http.AdminApiClient
import org.sysarp.project.service.auth.AuthService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    authService: AuthService,
    onNavigateBack: () -> Unit
) {
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()
    
    val adminService = remember { AdminService(AdminApiClient()) }
    
    // Estados
    var profileData by remember { mutableStateOf<AdminProfileData?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    
    // Campos editables
    var businessName by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    
    // Cargar perfil inicial
    LaunchedEffect(userProfile?.adminId, accessToken) {
        if (userProfile?.adminId != null && accessToken != null) {
            isLoading = true
            errorMessage = ""
            
            adminService.getAdminProfile(userProfile!!.adminId!!.toInt(), accessToken!!)
                .fold(
                    onSuccess = { profile ->
                        profileData = profile
                        businessName = profile.businessName
                        businessType = profile.businessType ?: ""
                        phone = profile.phone
                        address = profile.address
                        contactName = profile.contactName
                        isLoading = false
                    },
                    onFailure = { error ->
                        errorMessage = "Error cargando perfil: ${error.message}"
                        isLoading = false
                    }
                )
        }
    }
    
    // Función para guardar cambios
    val saveProfile = {
        if (accessToken != null && userProfile?.adminId != null) {
            isLoading = true
            errorMessage = ""
        }
    }
    
    // Manejar el guardado del perfil
    LaunchedEffect(isLoading) {
        if (isLoading && accessToken != null && userProfile?.adminId != null) {
            val updateRequest = UpdateAdminProfileRequest(
                businessName = businessName.takeIf { it.isNotEmpty() },
                businessType = businessType.takeIf { it.isNotEmpty() },
                phone = phone.takeIf { it.isNotEmpty() },
                address = address.takeIf { it.isNotEmpty() },
                contactName = contactName.takeIf { it.isNotEmpty() }
            )
            
            adminService.updateAdminProfile(userProfile!!.adminId!!.toInt(), accessToken!!, updateRequest)
                .fold(
                    onSuccess = { updatedProfile ->
                        profileData = updatedProfile
                        isEditing = false
                        successMessage = "Perfil actualizado exitosamente"
                        isLoading = false
                    },
                    onFailure = { error ->
                        errorMessage = "Error actualizando perfil: ${error.message}"
                        isLoading = false
                    }
                )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (isEditing) {
                        TextButton(onClick = { isEditing = false }) {
                            Text("Cancelar")
                        }
                        TextButton(onClick = saveProfile) {
                            Text("Guardar")
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mensajes de estado
            if (errorMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Error, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(errorMessage, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
            
            if (successMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(successMessage, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Información del perfil
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Información de la Empresa",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Email (solo lectura)
                        OutlinedTextField(
                            value = profileData?.email ?: "",
                            onValueChange = { },
                            label = { Text("Email") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) }
                        )
                        
                        // Nombre de la empresa
                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = { Text("Nombre de la Empresa") },
                            enabled = isEditing,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null) }
                        )
                        
                        // Tipo de empresa
                        OutlinedTextField(
                            value = businessType,
                            onValueChange = { businessType = it },
                            label = { Text("Tipo de Empresa") },
                            enabled = isEditing,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Filled.Category, contentDescription = null) }
                        )
                        
                        // RUC (solo lectura)
                        OutlinedTextField(
                            value = profileData?.ruc ?: "",
                            onValueChange = { },
                            label = { Text("RUC") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null) }
                        )
                        
                        // Teléfono
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Teléfono") },
                            enabled = isEditing,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) }
                        )
                        
                        // Dirección
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Dirección") },
                            enabled = isEditing,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) }
                        )
                        
                        // Nombre de contacto
                        OutlinedTextField(
                            value = contactName,
                            onValueChange = { contactName = it },
                            label = { Text("Nombre de Contacto") },
                            enabled = isEditing,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) }
                        )
                    }
                }
                
                // Información adicional
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Información Adicional",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Estado de Verificación:")
                            Text(
                                if (profileData?.isVerified == true) "Verificado" else "Pendiente",
                                fontWeight = FontWeight.Medium,
                                color = if (profileData?.isVerified == true) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.error
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Fecha de Creación:")
                            Text(
                                profileData?.createdAt ?: "",
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Última Actualización:")
                            Text(
                                profileData?.updatedAt ?: "",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
