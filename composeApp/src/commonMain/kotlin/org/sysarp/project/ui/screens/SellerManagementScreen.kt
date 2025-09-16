package org.sysarp.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.QRCodeData
import org.sysarp.project.service.QRService
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.SellerService
import org.sysarp.project.ui.components.EditSellerDialog
import org.sysarp.project.ui.components.DeleteSellerDialog
import org.sysarp.project.ui.components.seller_management.search.SellerSearchAndFilters
import org.sysarp.project.ui.components.seller_management.search.QuickFilters
import org.sysarp.project.ui.components.seller_management.stats.SellerStatsHeader
import org.sysarp.project.ui.components.seller_management.cards.SellerCard
import org.sysarp.project.ui.components.seller_management.pagination.SellerPaginationControls
import org.sysarp.project.ui.components.seller_management.SellerLoadingState
import org.sysarp.project.ui.components.seller_management.SellerErrorState
import org.sysarp.project.ui.components.seller_management.SellerEmptyState
import org.sysarp.project.ui.components.seller_management.SellerRefreshState

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
    var showPendingPaymentsDialog by remember { mutableStateOf(false) }
    var selectedSeller by remember { mutableStateOf<org.sysarp.project.data.MySeller?>(null) }
    
    // Estados para la API real
    var sellers by remember { mutableStateOf<List<org.sysarp.project.data.MySeller>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var currentPage by remember { mutableStateOf(1) }
    var totalPages by remember { mutableStateOf(1) }
    var totalItems by remember { mutableStateOf(0) }
    
    // Estados para búsqueda y filtros
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var showSearchBar by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var filterActive by remember { mutableStateOf<Boolean?>(null) } // null = todos, true = activos, false = inactivos
    var filterOnline by remember { mutableStateOf<Boolean?>(null) } // null = todos, true = en línea, false = desconectados
    var sortBy by remember { mutableStateOf("name") } // name, payments, amount, date
    var sortOrder by remember { mutableStateOf("asc") } // asc, desc
    var isRefreshing by remember { mutableStateOf(false) }
    
    val qrService = remember { QRService() }
    val activeQRCode by qrService.activeQRCode.collectAsState()
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Función para filtrar y ordenar vendedores
    val filteredSellers = remember(sellers, searchQuery.text, filterActive, filterOnline, sortBy, sortOrder) {
        var filtered = sellers
        
        // Filtrar por búsqueda
        if (searchQuery.text.isNotBlank()) {
            val query = searchQuery.text.lowercase()
            filtered = filtered.filter { seller ->
                seller.name.lowercase().contains(query) ||
                seller.email.lowercase().contains(query) ||
                seller.phone.contains(query) ||
                seller.branchName?.lowercase()?.contains(query) == true
            }
        }
        
        // Filtrar por estado activo/inactivo
        filterActive?.let { active ->
            filtered = filtered.filter { it.isActive == active }
        }
        
        // Filtrar por estado en línea/desconectado
        filterOnline?.let { online ->
            filtered = filtered.filter { it.isOnline == online }
        }
        
        // Ordenar
        filtered = when (sortBy) {
            "payments" -> if (sortOrder == "asc") 
                filtered.sortedBy { it.totalPayments } 
            else 
                filtered.sortedByDescending { it.totalPayments }
            "amount" -> if (sortOrder == "asc") 
                filtered.sortedBy { it.totalAmount } 
            else 
                filtered.sortedByDescending { it.totalAmount }
            "name" -> if (sortOrder == "asc") 
                filtered.sortedBy { it.name } 
            else 
                filtered.sortedByDescending { it.name }
            "date" -> if (sortOrder == "asc") 
                filtered.sortedBy { it.affiliationDate ?: "" } 
            else 
                filtered.sortedByDescending { it.affiliationDate ?: "" }
            else -> filtered
        }
        
        filtered
    }
    
    // Función para cargar vendedores
    val loadSellers: (Int) -> Unit = { page ->
        val profile = userProfile
        if (profile?.adminId != null && accessToken != null) {
            coroutineScope.launch {
                isLoading = true
                errorMessage = ""
                
                sellerService.getMySellers(
                    adminId = profile.adminId!!.toInt(),
                    page = page,
                    limit = 30,
                    token = accessToken!!
                ).fold(
                    onSuccess = { response ->
                        sellers = response.data?.sellers ?: emptyList()
                        currentPage = response.data?.pagination?.currentPage ?: 1
                        totalPages = response.data?.pagination?.totalPages ?: 1
                        totalItems = response.data?.pagination?.totalItems ?: 0
                        isLoading = false
                    },
                    onFailure = { error ->
                        errorMessage = error.message ?: "Error cargando vendedores"
                        isLoading = false
                    }
                )
            }
        }
    }
    
    // Función para refrescar datos
    val refreshData = {
        coroutineScope.launch {
            isRefreshing = true
            loadSellers(currentPage)
            isRefreshing = false
        }
    }
    
    // Cargar vendedores al iniciar
    LaunchedEffect(userProfile, accessToken) {
        val profile = userProfile
        if (profile != null && profile.adminId != null && accessToken != null) {
            loadSellers(1)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Gestión de Vendedores",
                        fontSize = 18.sp,
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
                    // Botón de búsqueda
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(
                            imageVector = if (showSearchBar) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = if (showSearchBar) "Cerrar búsqueda" else "Buscar"
                        )
                    }
                    
                    // Botón de filtros avanzados
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = if (showFilters) Icons.Filled.FilterListOff else Icons.Filled.FilterList,
                            contentDescription = if (showFilters) "Ocultar filtros" else "Mostrar filtros",
                            tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Búsqueda y filtros avanzados
            SellerSearchAndFilters(
                showSearchBar = showSearchBar,
                showFilters = showFilters,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                filterActive = filterActive,
                onFilterActiveChange = { filterActive = it },
                filterOnline = filterOnline,
                onFilterOnlineChange = { filterOnline = it },
                sortBy = sortBy,
                onSortByChange = { sortBy = it },
                sortOrder = sortOrder,
                onSortOrderChange = { sortOrder = it },
                onClearFilters = {
                    searchQuery = TextFieldValue("")
                    filterActive = null
                    filterOnline = null
                    sortBy = "name"
                    sortOrder = "asc"
                }
            )
            
            // Filtros rápidos
            QuickFilters(
                filterActive = filterActive,
                onFilterActiveChange = { filterActive = it }
            )
            
            // Lista de vendedores con pull-to-refresh
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                if (isRefreshing) {
                                    refreshData()
                                }
                            }
                        ) { _, dragAmount ->
                            if (dragAmount.y > 0 && !isRefreshing) {
                                isRefreshing = true
                            }
                        }
                    },
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Indicador de refresh
                if (isRefreshing) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Actualizando vendedores...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            // Header con estadísticas
            item {
                val totalSellers = filteredSellers.size
                val activeSellers = filteredSellers.count { it.isActive }
                val inactiveSellers = filteredSellers.count { !it.isActive }
                
                SellerStatsHeader(
                    totalSellers = totalSellers,
                    activeSellers = activeSellers,
                    inactiveSellers = inactiveSellers
                )
            }
            
            // Indicador de carga
            if (isLoading) {
                item { SellerLoadingState() }
            }
            
            // Mensaje de error
            if (errorMessage.isNotEmpty()) {
                item { 
                    SellerErrorState(
                        errorMessage = errorMessage,
                        onRetry = { loadSellers(currentPage) }
                    )
                }
            }
            
            // Mensaje cuando no hay resultados
            if (filteredSellers.isEmpty() && !isLoading) {
                item { SellerEmptyState() }
            }
            
            // Lista de vendedores filtrados
            items(filteredSellers) { seller ->
                SellerCard(
                    seller = seller,
                    onEdit = { 
                        selectedSeller = seller
                        showEditSellerDialog = true
                    },
                    onToggleStatus = { 
                        // Toggle status directamente
                        coroutineScope.launch {
                            val profile = userProfile
                            if (profile?.adminId != null && accessToken != null) {
                                sellerService.updateSeller(
                                    sellerId = seller.sellerId,
                                    adminId = profile.adminId!!.toInt(),
                                    isActive = !seller.isActive,
                                    token = accessToken!!
                                ).fold(
                                    onSuccess = { updatedSeller ->
                                        // Actualizar la lista local
                                        sellers = sellers.map { if (it.sellerId == updatedSeller.sellerId) updatedSeller else it }
                                    },
                                    onFailure = { error ->
                                        errorMessage = "Error actualizando estado: ${error.message}"
                                    }
                                )
                            }
                        }
                    },
                    onDelete = { 
                        selectedSeller = seller
                        showDeleteConfirmDialog = true
                    },
                    onViewPayments = {
                        selectedSeller = seller
                        showPendingPaymentsDialog = true
                    }
                )
            }
            
            // Controles de paginación
            item {
                SellerPaginationControls(
                    currentPage = currentPage,
                    totalPages = totalPages,
                    totalItems = totalItems,
                    onPageChange = { loadSellers(it) },
                    onFirstPage = { loadSellers(1) },
                    onPreviousPage = { loadSellers(currentPage - 1) },
                    onNextPage = { loadSellers(currentPage + 1) },
                    onLastPage = { loadSellers(totalPages) }
                )
            }
            
            // Espacio adicional
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
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
            onSave = { updatedSeller ->
                showEditSellerDialog = false
                selectedSeller = null
                // Actualizar la lista local
                sellers = sellers.map { if (it.sellerId == updatedSeller.sellerId) updatedSeller else it }
            },
            sellerService = sellerService,
            authService = authService
        )
    }
    
    // Diálogo de confirmación para eliminar/pausar
    if (showDeleteConfirmDialog && selectedSeller != null) {
        DeleteSellerDialog(
            seller = selectedSeller!!,
            onDismiss = { 
                showDeleteConfirmDialog = false
                selectedSeller = null
            },
            onConfirm = { action ->
                showDeleteConfirmDialog = false
                selectedSeller = null
                // Ejecutar acción
                coroutineScope.launch {
                    val profile = userProfile
                    if (profile?.adminId != null && accessToken != null) {
                        sellerService.deleteSeller(
                            sellerId = selectedSeller!!.sellerId,
                            adminId = profile.adminId!!.toInt(),
                            action = action,
                            token = accessToken!!
                        ).fold(
                            onSuccess = { 
                                // Recargar lista para reflejar cambios
                                loadSellers(currentPage)
                            },
                            onFailure = { error ->
                                errorMessage = "Error ${if (action == "pause") "pausando" else "eliminando"} vendedor: ${error.message}"
                            }
                        )
                    }
                }
            },
            sellerService = sellerService,
            authService = authService
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
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SellerCard(
    seller: org.sysarp.project.data.MySeller,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit,
    onViewPayments: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(32.dp)
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
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = seller.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = seller.branchName ?: "Sin sucursal",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = seller.email,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = seller.phone,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = if (seller.isActive) "Activo" else "Inactivo",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (seller.isActive) 
                            MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.error
                    )
                }
                
                // Indicador de estado
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = if (seller.isOnline) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.outline,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Estadísticas del vendedor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatChip("Pagos: ${seller.totalPayments}", Icons.Filled.Payment)
                StatChip("S/ ${String.format("%.2f", seller.totalAmount)}", Icons.Filled.CheckCircle)
                StatChip("Último: ${seller.lastPayment ?: "Nunca"}", Icons.Filled.Schedule)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
                    // Botones de acción
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Primera fila: Editar y Pagos
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
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Editar", fontSize = 11.sp)
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
                                    contentDescription = "Ver pagos pendientes",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Pagos",
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Segunda fila: Activar/Pausar y Eliminar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (seller.isActive) "Pausar" else "Activar",
                                    fontSize = 11.sp
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
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Eliminar",
                                    fontSize = 11.sp
                                )
                            }
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(8.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = text,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
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

