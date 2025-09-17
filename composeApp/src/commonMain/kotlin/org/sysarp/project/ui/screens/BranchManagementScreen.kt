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
    var showFilters by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Cargar sucursales
    LaunchedEffect(adminId, accessToken, currentPage, filterStatus) {
        isLoading = true
        errorMessage = null
        
        branchService.getBranches(
            adminId = adminId,
            accessToken = accessToken,
            status = filterStatus,
            page = currentPage,
            size = 20
        ).fold(
            onSuccess = { response ->
                branchesData = response
                isLoading = false
                println("🔍 [BRANCH_MANAGEMENT] Sucursales cargadas: ${response.branches.size}")
            },
            onFailure = { error ->
                errorMessage = error.message ?: "Error cargando sucursales"
                isLoading = false
                println("🔍 [BRANCH_MANAGEMENT] Error cargando sucursales: ${error.message}")
            }
        )
    }
    
    val branches = branchesData?.branches ?: emptyList()
    val activeBranches = branches.count { it.isActive }
    val inactiveBranches = branches.count { !it.isActive }
    val totalSellers = branches.sumOf { it.sellersCount }
    
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
                            imageVector = Icons.Filled.Business,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Gestión de Sucursales",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "Administra tus sucursales y equipos",
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
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (errorMessage != null) {
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
                                text = "Error cargando sucursales",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val branchStats = listOf(
                        BranchStat(
                            title = "Total",
                            value = branches.size.toString(),
                            icon = Icons.Filled.Business,
                            color = MaterialTheme.colorScheme.primary,
                            trend = "+${branches.size}"
                        ),
                        BranchStat(
                            title = "Activas",
                            value = activeBranches.toString(),
                            icon = Icons.Filled.CheckCircle,
                            color = MaterialTheme.colorScheme.tertiary,
                            trend = "+${activeBranches}"
                        ),
                        BranchStat(
                            title = "Inactivas",
                            value = inactiveBranches.toString(),
                            icon = Icons.Filled.PauseCircle,
                            color = MaterialTheme.colorScheme.error,
                            trend = "+${inactiveBranches}"
                        ),
                        BranchStat(
                            title = "Vendedores",
                            value = totalSellers.toString(),
                            icon = Icons.Filled.People,
                            color = MaterialTheme.colorScheme.secondary,
                            trend = "+${totalSellers}"
                        )
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(branchStats) { stat ->
                            BranchStatCard(
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
            
            // Filtros
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
                                text = "Filtros",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            // Filtros rápidos
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Todas", "Activas", "Inactivas").forEach { filter ->
                                    FilterChip(
                                        onClick = { 
                                            filterStatus = when (filter) {
                                                "Todas" -> null
                                                "Activas" -> "active"
                                                "Inactivas" -> "inactive"
                                                else -> null
                                            }
                                        },
                                        label = { Text(filter) },
                                        selected = when (filter) {
                                            "Todas" -> filterStatus == null
                                            "Activas" -> filterStatus == "active"
                                            "Inactivas" -> filterStatus == "inactive"
                                            else -> false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = when (filter) {
                                                    "Todas" -> Icons.Filled.Business
                                                    "Activas" -> Icons.Filled.CheckCircle
                                                    "Inactivas" -> Icons.Filled.PauseCircle
                                                    else -> Icons.Filled.Business
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
            
            // Lista de sucursales
            item {
                Text(
                    text = "Lista de Sucursales",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            if (branches.isEmpty() && !isLoading) {
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
                                imageVector = Icons.Filled.BusinessCenter,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "No hay sucursales",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Text(
                                text = "No se encontraron sucursales con los filtros aplicados",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(branches) { branch ->
                    BranchCardV2(
                        branch = branch,
                        onEdit = {
                            selectedBranch = branch
                            showEditDialog = true
                        },
                        onViewSellers = {
                            selectedBranch = branch
                            showSellersDialog = true
                        },
                        onToggleStatus = {
                            selectedBranch = branch
                            // Implementar toggle de estado
                        }
                    )
                }
            }
            
            // Paginación
            if (branchesData?.pagination?.totalPages ?: 0 > 1) {
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
                                text = "Página ${currentPage + 1} de ${branchesData?.pagination?.totalPages ?: 1}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { if (currentPage > 0) currentPage-- },
                                    enabled = currentPage > 0
                                ) {
                                    Text("Anterior")
                                }
                                
                                Button(
                                    onClick = { 
                                        if (currentPage < (branchesData?.pagination?.totalPages ?: 1) - 1) 
                                            currentPage++ 
                                    },
                                    enabled = currentPage < (branchesData?.pagination?.totalPages ?: 1) - 1
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
                            // Recargar datos
                        },
                        onFailure = {
                            // Mostrar error
                        }
                    )
                }
            }
        )
    }
    
    selectedBranch?.let { branch ->
        if (showEditDialog) {
            EditBranchDialog(
                branch = branch,
                onDismiss = { showEditDialog = false },
                onUpdate = { name, code, address, isActive ->
                    coroutineScope.launch {
                        branchService.updateBranch(
                            branchId = branch.branchId,
                            adminId = adminId,
                            name = name,
                            code = code,
                            address = address,
                            isActive = isActive,
                            accessToken = accessToken
                        ).fold(
                            onSuccess = { updatedBranch ->
                                showEditDialog = false
                                // Actualizar la lista de sucursales
                                val currentBranchesData = branchesData
                                if (currentBranchesData != null) {
                                    branchesData = currentBranchesData.copy(
                                        branches = currentBranchesData.branches.map { 
                                            if (it.branchId == updatedBranch.branchId) {
                                                BranchInfo(
                                                    branchId = updatedBranch.branchId,
                                                    name = updatedBranch.name,
                                                    code = updatedBranch.code,
                                                    address = updatedBranch.address,
                                                    isActive = updatedBranch.isActive,
                                                    createdAt = updatedBranch.createdAt,
                                                    updatedAt = updatedBranch.updatedAt,
                                                    sellersCount = updatedBranch.sellersCount // Usar el conteo actualizado
                                                )
                                            } else it 
                                        }
                                    )
                                }
                            },
                            onFailure = { error ->
                                println("Error actualizando sucursal: ${error.message}")
                                showEditDialog = false
                            }
                        )
                    }
                }
            )
        }
        
        if (showSellersDialog) {
            BranchSellersDialog(
                branch = branch,
                branchService = branchService,
                adminId = adminId,
                accessToken = accessToken,
                onDismiss = { showSellersDialog = false }
            )
        }
    }
}

@Composable
fun BranchStatCard(
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
fun BranchCardV2(
    branch: BranchInfo,
    onEdit: () -> Unit,
    onViewSellers: () -> Unit,
    onToggleStatus: () -> Unit
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
                // Icono de sucursal
                Card(
                    modifier = Modifier.size(48.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (branch.isActive) 
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
                            imageVector = Icons.Filled.Business,
                            contentDescription = null,
                            tint = if (branch.isActive) 
                                MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = branch.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "Código: ${branch.code}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = branch.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Estado
                Column(horizontalAlignment = Alignment.End) {
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
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (branch.isActive) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Estadísticas de la sucursal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BranchMetricItem(
                    label = "Vendedores",
                    value = branch.sellersCount.toString(),
                    icon = Icons.Filled.People
                )
                
                BranchMetricItem(
                    label = "Creada",
                    value = branch.createdAt.take(10),
                    icon = Icons.Filled.CalendarToday
                )
                
                BranchMetricItem(
                    label = "Actualizada",
                    value = branch.updatedAt.take(10),
                    icon = Icons.Filled.Update
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
                    onClick = onViewSellers,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.People,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Vendedores", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun BranchMetricItem(
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

data class BranchStat(
    val title: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color,
    val trend: String
)