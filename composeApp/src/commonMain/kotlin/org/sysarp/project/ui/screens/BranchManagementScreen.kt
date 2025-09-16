package org.sysarp.project.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import org.sysarp.project.data.BranchInfo
import org.sysarp.project.data.BranchesData
import org.sysarp.project.service.branch.BranchService
import org.sysarp.project.ui.components.branch.BranchCard
import org.sysarp.project.ui.components.branch.CreateBranchDialog
import org.sysarp.project.ui.components.branch.EditBranchDialog
import org.sysarp.project.ui.components.branch.BranchSellersDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchManagementScreen(
    branchService: BranchService,
    adminId: Int,
    accessToken: String,
    onBackClick: () -> Unit
) {
    // Estados
    var branchesData by remember { mutableStateOf<BranchesData?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedBranch by remember { mutableStateOf<BranchInfo?>(null) }
    var showSellersDialog by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(0) }
    var filterStatus by remember { mutableStateOf<String?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Cargar sucursales
    val loadBranches = {
        isLoading = true
        errorMessage = null
        
        coroutineScope.launch {
            branchService.getBranches(
                adminId = adminId,
                accessToken = accessToken,
                status = filterStatus,
                page = currentPage,
                size = 20
            ).fold(
                onSuccess = { data ->
                    branchesData = data
                    isLoading = false
                },
                onFailure = { error ->
                    errorMessage = error.message ?: "Error cargando sucursales"
                    isLoading = false
                }
            )
        }
    }
    
    // Cargar datos iniciales
    LaunchedEffect(adminId, accessToken, currentPage, filterStatus) {
        loadBranches()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Gestión de Sucursales",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Crear sucursal"
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
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Filtros
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Filtros",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { 
                                filterStatus = null
                                currentPage = 0
                            },
                            label = { Text("Todas") },
                            selected = filterStatus == null,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.List,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                        
                        FilterChip(
                            onClick = { 
                                filterStatus = "active"
                                currentPage = 0
                            },
                            label = { Text("Activas") },
                            selected = filterStatus == "active",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                        
                        FilterChip(
                            onClick = { 
                                filterStatus = "inactive"
                                currentPage = 0
                            },
                            label = { Text("Inactivas") },
                            selected = filterStatus == "inactive",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.PauseCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Contenido
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Cargando sucursales...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                errorMessage != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                
                branchesData?.branches?.isEmpty() == true -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Business,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No hay sucursales",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Crea tu primera sucursal para comenzar",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { showCreateDialog = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Crear Sucursal")
                            }
                        }
                    }
                }
                
                else -> {
                    // Lista de sucursales
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(branchesData?.branches ?: emptyList()) { branch ->
                            BranchCard(
                                branch = branch,
                                onEdit = {
                                    selectedBranch = branch
                                    showEditDialog = true
                                },
                                onViewSellers = {
                                    selectedBranch = branch
                                    showSellersDialog = true
                                },
                                onDelete = {
                                    // Implementar eliminación
                                }
                            )
                        }
                        
                        // Paginación
                        branchesData?.pagination?.let { pagination ->
                            if (pagination.totalPages > 1) {
                                item {
                                    BranchPaginationControls(
                                        currentPage = pagination.currentPage,
                                        totalPages = pagination.totalPages,
                                        totalItems = pagination.totalItems,
                                        onPageChange = { page ->
                                            currentPage = page
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Diálogos
    if (showCreateDialog) {
        CreateBranchDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, code, address ->
                coroutineScope.launch {
                    branchService.createBranch(
                        adminId = adminId,
                        name = name,
                        code = code,
                        address = address,
                        accessToken = accessToken
                    ).fold(
                        onSuccess = {
                            showCreateDialog = false
                            loadBranches()
                        },
                        onFailure = { error ->
                            errorMessage = error.message ?: "Error creando sucursal"
                        }
                    )
                }
            }
        )
    }
    
    if (showEditDialog && selectedBranch != null) {
        EditBranchDialog(
            branch = selectedBranch!!,
            onDismiss = { 
                showEditDialog = false
                selectedBranch = null
            },
            onUpdate = { name, code, address, isActive ->
                coroutineScope.launch {
                    branchService.updateBranch(
                        branchId = selectedBranch!!.branchId,
                        adminId = adminId,
                        name = name,
                        code = code,
                        address = address,
                        isActive = isActive,
                        accessToken = accessToken
                    ).fold(
                        onSuccess = {
                            showEditDialog = false
                            selectedBranch = null
                            loadBranches()
                        },
                        onFailure = { error ->
                            errorMessage = error.message ?: "Error actualizando sucursal"
                        }
                    )
                }
            }
        )
    }
    
    if (showSellersDialog && selectedBranch != null) {
        BranchSellersDialog(
            branch = selectedBranch!!,
            branchService = branchService,
            adminId = adminId,
            accessToken = accessToken,
            onDismiss = { 
                showSellersDialog = false
                selectedBranch = null
            }
        )
    }
}

@Composable
fun BranchPaginationControls(
    currentPage: Int,
    totalPages: Int,
    totalItems: Int,
    onPageChange: (Int) -> Unit
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Página ${currentPage + 1} de $totalPages",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Total: $totalItems elementos",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onPageChange(currentPage - 1) },
                    enabled = currentPage > 0,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("Anterior")
                }
                
                OutlinedButton(
                    onClick = { onPageChange(currentPage + 1) },
                    enabled = currentPage < totalPages - 1,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Siguiente")
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
