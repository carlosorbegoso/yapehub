package org.sysarp.project.ui.admin.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.QRCodeData

/**
 * Componentes UI para AdminProfileScreen
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreenContent(
    state: AdminProfileState,
    onNavigateBack: () -> Unit,
    onNavigateToQR: (QRCodeData) -> Unit,
    onEditProfile: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveProfile: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
            if (state.hasError()) {
                AdminProfileErrorCard(errorMessage = state.getCurrentErrorMessage())
            }
            
            if (state.hasSuccessMessage()) {
                AdminProfileSuccessCard(successMessage = state.getCurrentSuccessMessage())
            }
            
            if (state.isCurrentlyLoading()) {
                AdminProfileLoadingCard()
            } else {
                // Header elegante y unificado
                AdminProfileHeaderCard(
                    state = state,
                    onNavigateToQR = onNavigateToQR
                )
                
                // Información de la empresa
                AdminProfileInfoCard(
                    state = state,
                    onEditProfile = onEditProfile,
                    onCancelEdit = onCancelEdit,
                    onSaveProfile = onSaveProfile
                )
                
                // Información adicional
                AdminProfileAdditionalInfoCard(state = state)
            }
        }
    }
}

@Composable
fun AdminProfileErrorCard(errorMessage: String) {
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

@Composable
fun AdminProfileSuccessCard(successMessage: String) {
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

@Composable
fun AdminProfileLoadingCard() {
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
}

@Composable
fun AdminProfileHeaderCard(
    state: AdminProfileState,
    onNavigateToQR: (QRCodeData) -> Unit
) {
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
                            text = state.getCurrentProfileData()?.businessName ?: "Mi Empresa",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = state.getCurrentProfileData()?.businessType ?: "Empresa",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "ID: ${state.getCurrentProfileData()?.id ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Botón QR siempre visible
                IconButton(
                    onClick = { 
                        // Crear QRCodeData con datos del perfil
                        val qrData = QRCodeData(
                            affiliationCode = state.getCurrentProfileData()?.id?.toString() ?: "0",
                            qrBase64 = "", // Se generará en la pantalla QR
                            expiresAt = "",
                            maxUses = 0,
                            remainingUses = 0,
                            branchName = state.getCurrentProfileData()?.businessName ?: "Mi Empresa",
                            adminName = state.getCurrentProfileData()?.contactName ?: "Admin"
                        )
                        onNavigateToQR(qrData)
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
        }
    }
}

@Composable
fun AdminProfileInfoCard(
    state: AdminProfileState,
    onEditProfile: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveProfile: () -> Unit
) {
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
            
            // Campos de información
            AdminProfileField(
                label = "Nombre del Negocio",
                value = state.getCurrentBusinessName(),
                onValueChange = { state.updateBusinessName(it) },
                isEditing = state.isCurrentlyEditing(),
                icon = Icons.Filled.Store
            )
            
            AdminProfileField(
                label = "Tipo de Negocio",
                value = state.getCurrentBusinessType(),
                onValueChange = { state.updateBusinessType(it) },
                isEditing = state.isCurrentlyEditing(),
                icon = Icons.Filled.Category
            )
            
            AdminProfileField(
                label = "Teléfono",
                value = state.getCurrentPhone(),
                onValueChange = { state.updatePhone(it) },
                isEditing = state.isCurrentlyEditing(),
                icon = Icons.Filled.Phone
            )
            
            AdminProfileField(
                label = "Dirección",
                value = state.getCurrentAddress(),
                onValueChange = { state.updateAddress(it) },
                isEditing = state.isCurrentlyEditing(),
                icon = Icons.Filled.LocationOn
            )
            
            AdminProfileField(
                label = "Nombre de Contacto",
                value = state.getCurrentContactName(),
                onValueChange = { state.updateContactName(it) },
                isEditing = state.isCurrentlyEditing(),
                icon = Icons.Filled.Person
            )
            
            // Barra de acciones unificada
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.isCurrentlyEditing()) {
                    // Modo edición
                    Button(
                        onClick = onCancelEdit,
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
                        onClick = onSaveProfile,
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
                        onClick = onEditProfile,
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
}

@Composable
fun AdminProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditing: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        
        if (isEditing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value.ifEmpty { "No especificado" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun AdminProfileAdditionalInfoCard(state: AdminProfileState) {
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
                    imageVector = Icons.Filled.Badge,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Información Adicional",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Información adicional
            AdminProfileField(
                label = "Estado de Verificación",
                value = if (state.getCurrentProfileData()?.isVerified == true) "Verificado" else "Pendiente",
                onValueChange = { },
                isEditing = false,
                icon = Icons.Filled.Verified
            )
            
            AdminProfileField(
                label = "Fecha de Registro",
                value = state.getCurrentProfileData()?.createdAt ?: "N/A",
                onValueChange = { },
                isEditing = false,
                icon = Icons.Filled.Info
            )
        }
    }
}
