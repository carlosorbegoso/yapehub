package org.sysarp.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlinx.coroutines.launch
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.QRCodeData
import org.sysarp.project.service.QRService
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.SellerService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerManagementScreen(
    authService: AuthService,
    sellerService: SellerService,
    onNavigateBack: () -> Unit,
    onNavigateToQR: (org.sysarp.project.service.QRCodeData) -> Unit
) {
    var showEditSellerDialog by remember { mutableStateOf(false) }
    var showQRDialog by remember { mutableStateOf(false) }
    var showAffiliationCodeDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedSeller by remember { mutableStateOf<org.sysarp.project.data.SellerInfo?>(null) }
    
    // Estados para la API real
    var sellers by remember { mutableStateOf<List<org.sysarp.project.data.SellerInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var currentPage by remember { mutableStateOf(1) }
    var totalPages by remember { mutableStateOf(1) }
    var totalItems by remember { mutableStateOf(0) }
    
    val qrService = remember { QRService() }
    val activeQRCode by qrService.activeQRCode.collectAsState()
    val userProfile by authService.userProfile.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Cargar vendedores al iniciar
    LaunchedEffect(userProfile) {
        val profile = userProfile
        if (profile != null && profile.adminId != null) {
            loadSellers()
        }
    }
    
    fun loadSellers(page: Int = 1) {
        val profile = userProfile
        if (profile?.adminId == null || profile.accessToken == null) return
        
        coroutineScope.launch {
            isLoading = true
            errorMessage = ""
            
            sellerService.getMySellers(
                adminId = profile.adminId!!.toInt(),
                page = page,
                limit = 30,
                token = profile.accessToken!!
            ).fold(
                onSuccess = { response ->
                    sellers = response.data.sellers
                    currentPage = response.data.pagination.currentPage
                    totalPages = response.data.pagination.totalPages
                    totalItems = response.data.pagination.totalItems
                    isLoading = false
                },
                onFailure = { error ->
                    errorMessage = error.message ?: "Error cargando vendedores"
                    isLoading = false
                }
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Gestión de Vendedores",
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
                actions = {
                    // Botón removido - los vendedores se asocian por QR
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Botón para generar código de afiliación
                FloatingActionButton(
                    onClick = { showAffiliationCodeDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Key,
                        contentDescription = "Generar código de afiliación"
                    )
                }
                
                // Botón para generar QR
                FloatingActionButton(
                    onClick = { showQRDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCode,
                        contentDescription = "Generar QR"
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con estadísticas
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val totalSellers = totalItems
                        val activeSellers = sellers.count { it.isActive }
                        val inactiveSellers = sellers.count { !it.isActive }
                        
                        StatItem("Total", totalSellers.toString(), Icons.Filled.People)
                        StatItem("Activos", activeSellers.toString(), Icons.Filled.CheckCircle)
                        StatItem("Inactivos", inactiveSellers.toString(), Icons.Filled.PauseCircle)
                    }
                }
            }
            
            // Indicador de carga
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            // Mensaje de error
            if (errorMessage.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = errorMessage,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // Lista de vendedores
            items(sellers) { seller ->
                SellerCard(
                    seller = seller,
                    onEdit = { 
                        selectedSeller = seller
                        showEditSellerDialog = true
                    },
                    onToggleStatus = { 
                        // TODO: Implementar cambio de estado via API
                        loadSellers(currentPage) // Recargar para reflejar cambios
                    },
                    onDelete = { 
                        selectedSeller = seller
                        showDeleteConfirmDialog = true
                    }
                )
            }
            
            // Controles de paginación
            if (totalPages > 1) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Página $currentPage de $totalPages",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { loadSellers(currentPage - 1) },
                                    enabled = currentPage > 1 && !isLoading,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ChevronLeft,
                                        contentDescription = "Página anterior",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                Button(
                                    onClick = { loadSellers(currentPage + 1) },
                                    enabled = currentPage < totalPages && !isLoading,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ChevronRight,
                                        contentDescription = "Página siguiente",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Espacio adicional
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    
    // Diálogo para editar vendedor
    if (showEditSellerDialog && selectedSeller != null) {
        EditSellerDialog(
            seller = selectedSeller!!,
            onDismiss = { 
                showEditSellerDialog = false
                selectedSeller = null
            },
            onConfirm = { updatedSellerData ->
                sellers = sellers.map { 
                    if (it.id == updatedSellerData.id) updatedSellerData else it 
                }
                showEditSellerDialog = false
                selectedSeller = null
            }
        )
    }
    
    // Diálogo de confirmación para eliminar
    if (showDeleteConfirmDialog && selectedSeller != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirmDialog = false
                selectedSeller = null
            },
            title = {
                Text(
                    "Eliminar Vendedor",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("¿Estás seguro de que quieres eliminar a ${selectedSeller?.name}? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // TODO: Implementar eliminación via API
                        loadSellers(currentPage) // Recargar lista
                        showDeleteConfirmDialog = false
                        selectedSeller = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteConfirmDialog = false
                        selectedSeller = null
                    },
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
    
    // Diálogo para generar QR
    if (showQRDialog) {
        GenerateQRDialog(
            qrService = qrService,
            userProfile = userProfile,
            activeQRCode = activeQRCode,
            onDismiss = { showQRDialog = false },
            onGenerate = { qrCode ->
                onNavigateToQR(qrCode)
                showQRDialog = false
            }
        )
    }
    
    // Diálogo para generar código de afiliación
    if (showAffiliationCodeDialog) {
        GenerateAffiliationCodeDialog(
            authService = authService,
            onDismiss = { showAffiliationCodeDialog = false },
            onGenerate = { affiliationCode ->
                // Código generado exitosamente
                showAffiliationCodeDialog = false
            }
        )
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun SellerCard(
    seller: org.sysarp.project.data.SellerInfo,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (seller.isActive) 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = if (seller.isActive) 
                            MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = seller.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = seller.branchName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = seller.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = seller.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = if (seller.isActive) "Activo" else "Inactivo",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (seller.isActive) 
                            MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.error
                    )
                }
                
                // Indicador de estado
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = if (seller.isOnline) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.outline,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Estadísticas del vendedor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatChip("Pagos: ${seller.totalPayments}", Icons.Filled.Payment)
                StatChip("S/ ${String.format("%.2f", seller.totalAmount)}", Icons.Filled.AttachMoney)
                StatChip("Último: ${seller.lastPayment ?: "Nunca"}", Icons.Filled.Schedule)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar")
                }
                
                // Botón de Pausar/Activar con mejor diseño
                Button(
                    onClick = onToggleStatus,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (seller.isActive) 
                            MaterialTheme.colorScheme.secondaryContainer
                        else 
                            MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (seller.isActive) 
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else 
                            MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (seller.isActive) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (seller.isActive) "Pausar vendedor" else "Activar vendedor",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (seller.isActive) "Pausar" else "Activar",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Botón de Eliminar con diseño distintivo
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Eliminar vendedor",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Eliminar",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun StatChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AddSellerDialog(
    onDismiss: () -> Unit,
    onConfirm: (SellerData) -> Unit
) {
    var sellerName by remember { mutableStateOf("") }
    var branchCode by remember { mutableStateOf("") }
    var branchName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Agregar Vendedor",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = sellerName,
                    onValueChange = { sellerName = it },
                    label = { Text("Nombre del vendedor") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = branchCode,
                    onValueChange = { branchCode = it },
                    label = { Text("Código de sucursal") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = branchName,
                    onValueChange = { branchName = it },
                    label = { Text("Nombre de sucursal") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (sellerName.isNotBlank() && branchCode.isNotBlank() && branchName.isNotBlank()) {
                        onConfirm(
                            SellerData(
                                id = "seller_${System.currentTimeMillis()}",
                                name = sellerName,
                                branchCode = branchCode,
                                branchName = branchName,
                                isActive = true,
                                isOnline = false,
                                totalPayments = 0,
                                totalAmount = "0.00",
                                lastPayment = "Nunca"
                            )
                        )
                    }
                },
                enabled = sellerName.isNotBlank() && branchCode.isNotBlank() && branchName.isNotBlank()
            ) {
                Text("Agregar")
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

@Composable
fun EditSellerDialog(
    seller: SellerData,
    onDismiss: () -> Unit,
    onConfirm: (SellerData) -> Unit
) {
    var sellerName by remember { mutableStateOf(seller.name) }
    var branchCode by remember { mutableStateOf(seller.branchCode) }
    var branchName by remember { mutableStateOf(seller.branchName) }
    var isActive by remember { mutableStateOf(seller.isActive) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Editar Vendedor",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                
                OutlinedTextField(
                    value = branchCode,
                    onValueChange = { branchCode = it },
                    label = { Text("Código de sucursal") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                
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
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vendedor activo",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (sellerName.isNotBlank() && branchCode.isNotBlank() && branchName.isNotBlank()) {
                        onConfirm(
                            seller.copy(
                                name = sellerName,
                                branchCode = branchCode,
                                branchName = branchName,
                                isActive = isActive
                            )
                        )
                    }
                },
                enabled = sellerName.isNotBlank() && branchCode.isNotBlank() && branchName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Guardar Cambios")
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
fun GenerateQRDialog(
    qrService: QRService,
    userProfile: UserProfile?,
    activeQRCode: QRCodeData?,
    onDismiss: () -> Unit,
    onGenerate: (org.sysarp.project.service.QRCodeData) -> Unit
) {
    val canGenerate = qrService.canGenerateNewQR()
    val qrStatus = qrService.getQRCodeStatus()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Generar Código QR",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCode,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Genera un código QR para que los vendedores puedan afiliarse a tu sistema.",
                    textAlign = TextAlign.Center
                )
                
                // Debug info
                if (userProfile == null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Debug: userProfile es null. Usando datos por defecto.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                
                // Mostrar estado actual del QR
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (activeQRCode?.isExpired() == true) 
                            MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = qrStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (activeQRCode?.isExpired() == true) 
                            MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
                
                Text(
                    text = "El código expirará en 5 minutos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (userProfile != null) {
                        val qrCode = qrService.generateQRCode(
                            businessId = userProfile.adminId ?: "default_business",
                            businessName = userProfile.businessName ?: "Mi Negocio",
                            branchCode = userProfile.branchCode ?: "SUC001",
                            branchName = userProfile.branchName ?: "Sucursal Principal"
                        )
                        onGenerate(qrCode)
                    }
                },
                enabled = canGenerate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (activeQRCode != null) "Generar Nuevo QR" else "Generar QR")
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
fun GenerateAffiliationCodeDialog(
    authService: AuthService,
    onDismiss: () -> Unit,
    onGenerate: (String) -> Unit
) {
    var generatedCode by remember { mutableStateOf("") }
    var showCode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var branchId by remember { mutableStateOf("605") }
    var expirationHours by remember { mutableStateOf("2") }
    var maxUses by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Generar Código de Afiliación",
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
                    imageVector = Icons.Filled.Key,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Genera un código único para que los vendedores se afilien a tu negocio.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campos de entrada
                OutlinedTextField(
                    value = branchId,
                    onValueChange = { branchId = it },
                    label = { Text("ID de Sucursal") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = expirationHours,
                    onValueChange = { expirationHours = it },
                    label = { Text("Horas de Expiración") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = maxUses,
                    onValueChange = { maxUses = it },
                    label = { Text("Usos Máximos") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mensajes de error y éxito
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (successMessage.isNotEmpty()) {
                    Text(
                        text = successMessage,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (showCode && generatedCode.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Código generado:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = generatedCode,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Comparte este código con tus vendedores",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!showCode) {
                        // Generar código usando la API real
                        coroutineScope.launch {
                            isLoading = true
                            errorMessage = ""
                            successMessage = ""
                            
                            try {
                                // TODO: Implementar generación de código de afiliación
                                generatedCode = "AFF${System.currentTimeMillis()}"
                                showCode = true
                                successMessage = "✅ Código generado exitosamente"
                                errorMessage = ""
                            } catch (e: Exception) {
                                errorMessage = "❌ Error: ${e.message}"
                                successMessage = ""
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        onGenerate(generatedCode)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                } else {
                    Text(if (!showCode) "Generar Código" else "Confirmar")
                }
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

