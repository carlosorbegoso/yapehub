package org.sysarp.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.AdminProfileData
import org.sysarp.project.data.UpdateAdminProfileRequest
import org.sysarp.project.service.admin.AdminService
import org.sysarp.project.service.http.AdminProfileApiClient
import org.sysarp.project.service.http.AdminSellerApiClient
import org.sysarp.project.service.http.SellerManagementApiClient
import org.sysarp.project.service.http.AdminStatsApiClient
import org.sysarp.project.service.auth.AuthService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    authService: AuthService,
    onNavigateBack: () -> Unit,
    onNavigateToQR: (org.sysarp.project.data.QRCodeData) -> Unit = {}
) {
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()
    
    val adminService = remember { 
        AdminService(
            AdminProfileApiClient(),
            AdminSellerApiClient(),
            SellerManagementApiClient(),
            AdminStatsApiClient()
        )
    }
    
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
    val coroutineScope = rememberCoroutineScope()
    val saveProfile = {
        if (accessToken != null && userProfile?.adminId != null) {
            isLoading = true
            errorMessage = ""
            
            coroutineScope.launch {
                val updateRequest = UpdateAdminProfileRequest(
                    businessName = businessName.takeIf { it.isNotEmpty() },
                    businessType = businessType.takeIf { it.isNotEmpty() },
                    phone = phone.takeIf { it.isNotEmpty() },
                    address = address.takeIf { it.isNotEmpty() },
                    contactName = contactName.takeIf { it.isNotEmpty() }
                )
                
                try {
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
                } catch (e: Exception) {
                    errorMessage = "Error actualizando perfil: ${e.message}"
                    isLoading = false
                }
            }
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Cargando perfil...")
                    }
                }
            } else {
                // Header elegante y unificado
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Información principal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar más grande y elegante
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(
                                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.secondary
                                                )
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Business,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(20.dp))
                                
                                Column {
                                    Text(
                                        text = profileData?.businessName ?: "Mi Empresa",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = profileData?.businessType ?: "Empresa",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "ID: ${profileData?.id ?: "N/A"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            
                            // Botón QR siempre visible
                            IconButton(
                                onClick = { 
                                    // TODO: Implementar generación de QR
                                    // onNavigateToQR(qrCodeData)
                                },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.QrCode,
                                    contentDescription = "Mi QR",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        
                        // Barra de acciones unificada
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (isEditing) {
                                // Modo edición
                                Button(
                                    onClick = { isEditing = false },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cancelar")
                                }
                                
                                Button(
                                    onClick = saveProfile,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Save,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Guardar")
                                }
                            } else {
                                // Modo visualización
                                Button(
                                    onClick = { isEditing = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Editar Perfil")
                                }
                            }
                        }
                    }
                }
                
                // Información de la empresa - Diseño compacto
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header compacto
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Información de la Empresa",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Grid de información compacto
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Fila 1: Nombre y Tipo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                InfoField(
                                    label = "Empresa",
                                    value = businessName,
                                    onValueChange = { businessName = it },
                                    icon = Icons.Filled.Business,
                                    enabled = isEditing,
                                    modifier = Modifier.weight(1f)
                                )
                                InfoField(
                                    label = "Tipo",
                                    value = businessType,
                                    onValueChange = { businessType = it },
                                    icon = Icons.Filled.Category,
                                    enabled = isEditing,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            // Fila 2: RUC y Teléfono
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                InfoField(
                                    label = "RUC",
                                    value = profileData?.ruc ?: "",
                                    onValueChange = { },
                                    icon = Icons.Filled.Badge,
                                    enabled = false,
                                    modifier = Modifier.weight(1f)
                                )
                                InfoField(
                                    label = "Teléfono",
                                    value = phone,
                                    onValueChange = { phone = it },
                                    icon = Icons.Filled.Phone,
                                    enabled = isEditing,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            // Fila 3: Dirección (ancho completo)
                            InfoField(
                                label = "Dirección",
                                value = address,
                                onValueChange = { address = it },
                                icon = Icons.Filled.LocationOn,
                                enabled = isEditing,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            // Fila 4: Contacto
                            InfoField(
                                label = "Contacto",
                                value = contactName,
                                onValueChange = { contactName = it },
                                icon = Icons.Filled.Person,
                                enabled = isEditing,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                // Estado y fechas - Diseño compacto
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Estado de verificación compacto
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            color = if (profileData?.isVerified == true) 
                                                MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.errorContainer,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (profileData?.isVerified == true) 
                                            Icons.Filled.Verified else Icons.Filled.Warning,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (profileData?.isVerified == true) 
                                            MaterialTheme.colorScheme.onPrimaryContainer 
                                        else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Estado",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (profileData?.isVerified == true) "Verificado" else "Pendiente",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            
                            Text(
                                text = profileData?.createdAt?.substring(0, 10) ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Sucursales si están disponibles
                profileData?.branches?.takeIf { it.isNotEmpty() }?.let { branches ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Store,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sucursales (${branches.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            branches.forEach { branch ->
                                BranchCard(branch = branch)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        leadingIcon = { 
            Icon(
                imageVector = icon, 
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            ) 
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
fun BranchCard(
    branch: org.sysarp.project.data.AdminBranchInfo
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = branch.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = branch.code,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = branch.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (branch.isActive) 
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (branch.isActive) "Activa" else "Inactiva",
                    color = if (branch.isActive) 
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
