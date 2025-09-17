package org.sysarp.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.QRCodeData
import org.sysarp.project.service.QRService
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.SellerService
import org.sysarp.project.ui.components.EditSellerDialog
import org.sysarp.project.ui.components.DeleteSellerDialog
import org.sysarp.project.data.MySeller

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerManagementScreen(
    authService: AuthService,
    sellerService: SellerService,
    onNavigateBack: () -> Unit,
    onNavigateToQR: (QRCodeData) -> Unit = {},
    onNavigateToSellerPayments: (Int, String) -> Unit = { _, _ -> }
) {
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Estados para vendedores
    var sellers by remember { mutableStateOf<List<MySeller>>(emptyList()) }
    var isLoadingSellers by remember { mutableStateOf(false) }
    var sellersError by remember { mutableStateOf("") }
    var currentPage by remember { mutableStateOf(1) }
    var totalPages by remember { mutableStateOf(1) }
    var totalSellers by remember { mutableStateOf(0) }
    
    // Estados para diálogos
    var showEditSellerDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedSeller by remember { mutableStateOf<MySeller?>(null) }
    
    // Estados para filtros y búsqueda
    var searchQuery by remember { mutableStateOf("") }
    var filterActive by remember { mutableStateOf("Todos") }
    var showFilters by remember { mutableStateOf(false) }
    
    // Cargar vendedores
    LaunchedEffect(userProfile?.adminId, accessToken, currentPage, searchQuery, filterActive) {
        if (userProfile?.adminId != null && accessToken != null) {
            isLoadingSellers = true
            sellersError = ""
            
            sellerService.getMySellers(
                adminId = userProfile!!.adminId!!.toInt(),
                page = currentPage,
                limit = 20,
                token = accessToken!!
            ).fold(
                onSuccess = { response ->
                    sellers = response.data?.sellers ?: emptyList()
                    totalPages = response.data?.pagination?.totalPages ?: 1
                    totalSellers = response.data?.pagination?.totalItems ?: 0
                    isLoadingSellers = false
                    println("🔍 [SELLER_MANAGEMENT] Vendedores cargados: ${sellers.size}")
                },
                onFailure = { error ->
                    sellersError = error.message ?: "Error cargando vendedores"
                    isLoadingSellers = false
                    println("🔍 [SELLER_MANAGEMENT] Error cargando vendedores: ${error.message}")
                }
            )
        }
    }
    
    // Calcular estadísticas
    val activeSellers = sellers.count { it.isActive }
    val inactiveSellers = sellers.count { !it.isActive }
    
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = if (showFilters) Icons.Filled.FilterListOff else Icons.Filled.FilterList,
                            contentDescription = "Filtros"
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
            // Header con resumen
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
                            imageVector = Icons.Filled.People,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Gestión de Vendedores",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "Administra tu equipo de vendedores",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // Estadísticas principales
            item {
                Text(
                    text = "Estadísticas Principales",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                if (isLoadingSellers) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (sellersError.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error cargando vendedores",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = sellersError,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val sellerStats = listOf(
                        SellerStat(
                            title = "Total",
                            value = totalSellers.toString(),
                            icon = Icons.Filled.People,
                            color = MaterialTheme.colorScheme.primary,
                            trend = "+${sellers.size}"
                        ),
                        SellerStat(
                            title = "Activos",
                            value = activeSellers.toString(),
                            icon = Icons.Filled.CheckCircle,
                            color = MaterialTheme.colorScheme.tertiary,
                            trend = "+${activeSellers}"
                        ),
                        SellerStat(
                            title = "Inactivos",
                            value = inactiveSellers.toString(),
                            icon = Icons.Filled.PauseCircle,
                            color = MaterialTheme.colorScheme.error,
                            trend = "+${inactiveSellers}"
                        ),
                        SellerStat(
                            title = "En línea",
                            value = sellers.count { it.isOnline }.toString(),
                            icon = Icons.Filled.Wifi,
                            color = MaterialTheme.colorScheme.secondary,
                            trend = "+${sellers.count { it.isOnline }}"
                        )
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(sellerStats) { stat ->
                            SellerStatCard(
                                title = stat.title,
                                value = stat.value,
                                icon = stat.icon,
                                color = stat.color,
                                trend = stat.trend
                            )
                        }
                    }
                }
            }
            
            // Filtros y búsqueda
            if (showFilters) {
                item {
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
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Filtros y Búsqueda",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            // Barra de búsqueda
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Buscar vendedores") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            // Filtros rápidos
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Todos", "Activos", "Inactivos").forEach { filter ->
                                    FilterChip(
                                        onClick = { filterActive = filter },
                                        label = { Text(filter) },
                                        selected = filterActive == filter,
                                        leadingIcon = {
                                            Icon(
                                                imageVector = when (filter) {
                                                    "Todos" -> Icons.Filled.People
                                                    "Activos" -> Icons.Filled.CheckCircle
                                                    "Inactivos" -> Icons.Filled.PauseCircle
                                                    else -> Icons.Filled.People
                                                },
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Lista de vendedores
            item {
                Text(
                    text = "Lista de Vendedores",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            if (sellers.isEmpty() && !isLoadingSellers) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PeopleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "No hay vendedores",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Text(
                                text = "No se encontraron vendedores con los filtros aplicados",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(sellers) { seller ->
                    SellerCardV2(
                        seller = seller,
                        onEdit = {
                            selectedSeller = seller
                            showEditSellerDialog = true
                        },
                        onViewPayments = {
                            onNavigateToSellerPayments(seller.sellerId, seller.name)
                        },
                        onToggleStatus = {
                            coroutineScope.launch {
                                val profile = userProfile
                                if (profile?.adminId != null && accessToken != null) {
                                    sellerService.updateSeller(
                                        sellerId = seller.sellerId,
                                        adminId = profile.adminId!!.toInt(),
                                        name = null,
                                        phone = null,
                                        isActive = !seller.isActive,
                                        token = accessToken!!
                                    ).fold(
                                        onSuccess = { updatedSeller ->
                                            // Actualizar la lista de vendedores
                                            sellers = sellers.map { 
                                                if (it.sellerId == updatedSeller.sellerId) updatedSeller else it 
                                            }
                                        },
                                        onFailure = { error ->
                                            println("Error actualizando estado del vendedor: ${error.message}")
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            }
            
            // Paginación
            if (totalPages > 1) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
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
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { if (currentPage > 1) currentPage-- },
                                    enabled = currentPage > 1
                                ) {
                                    Text("Anterior")
                                }
                                
                                Button(
                                    onClick = { if (currentPage < totalPages) currentPage++ },
                                    enabled = currentPage < totalPages
                                ) {
                                    Text("Siguiente")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Diálogos
    selectedSeller?.let { seller ->
        EditSellerDialog(
            seller = seller,
            onDismiss = { 
                showEditSellerDialog = false
                selectedSeller = null
            },
            onSave = { updatedSeller ->
                // Actualizar la lista de vendedores
                sellers = sellers.map { 
                    if (it.sellerId == updatedSeller.sellerId) updatedSeller else it 
                }
                showEditSellerDialog = false
                selectedSeller = null
            },
            sellerService = sellerService,
            authService = authService
        )
        
        DeleteSellerDialog(
            seller = seller,
            onDismiss = { 
                showDeleteConfirmDialog = false
                selectedSeller = null
            },
            onConfirm = { action ->
                coroutineScope.launch {
                    val profile = userProfile
                    if (profile?.adminId != null && accessToken != null) {
                        sellerService.deleteSeller(
                            sellerId = seller.sellerId,
                            adminId = profile.adminId?.toInt() ?: return@launch,
                            action = action,
                            token = accessToken!!
                        ).fold(
                            onSuccess = {
                                // Remover el vendedor de la lista
                                sellers = sellers.filter { it.sellerId != seller.sellerId }
                                showDeleteConfirmDialog = false
                                selectedSeller = null
                            },
                            onFailure = { error ->
                                // Mostrar error (podrías agregar un estado de error aquí)
                                println("Error ${action} vendedor: ${error.message}")
                                showDeleteConfirmDialog = false
                                selectedSeller = null
                            }
                        )
                    }
                }
            },
            sellerService = sellerService,
            authService = authService
        )
    }
}

@Composable
fun SellerStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    trend: String
) {
    Card(
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = trend,
                style = MaterialTheme.typography.bodySmall,
                color = if (trend.startsWith("+")) 
                    MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SellerCardV2(
    seller: MySeller,
    onEdit: () -> Unit,
    onViewPayments: () -> Unit,
    onToggleStatus: () -> Unit = {}
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
            // Header con información principal
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Card(
                    modifier = Modifier.size(48.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (seller.isActive) 
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = if (seller.isActive) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
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
                        text = seller.branchName ?: "Sin sucursal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = seller.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Estado
                Column(horizontalAlignment = Alignment.End) {
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
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (seller.isActive) 
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (seller.isActive) "Activo" else "Inactivo",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (seller.isActive) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Estadísticas del vendedor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SellerMetricItem(
                    label = "Pagos",
                    value = seller.totalPayments.toString(),
                    icon = Icons.Filled.Payment
                )
                
                SellerMetricItem(
                    label = "Total",
                    value = "S/ ${String.format("%.0f", seller.totalAmount)}",
                    icon = Icons.Filled.CheckCircle
                )
                
                SellerMetricItem(
                    label = "Último",
                    value = seller.lastPayment?.let { "Reciente" } ?: "Nunca",
                    icon = Icons.Filled.Schedule
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar", fontSize = 12.sp)
                }
                
                Button(
                    onClick = onViewPayments,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pagos", fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Botón de estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onToggleStatus,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (seller.isActive)
                            MaterialTheme.colorScheme.secondaryContainer
                        else
                            MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = if (seller.isActive)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (seller.isActive) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (seller.isActive) "Pausar Vendedor" else "Activar Vendedor",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SellerMetricItem(
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
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class SellerStat(
    val title: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color,
    val trend: String
)