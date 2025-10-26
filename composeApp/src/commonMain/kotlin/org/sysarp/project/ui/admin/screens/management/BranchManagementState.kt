package org.sysarp.project.ui.admin.screens.management

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sysarp.project.data.BranchData
import org.sysarp.project.data.BranchInfo
import org.sysarp.project.data.BranchesData
import org.sysarp.project.service.branch.BranchService

/**
 * Estado y lógica de negocio para BranchManagementScreen
 */
class BranchManagementState(
    private val branchService: BranchService,
    private val adminId: Int,
    private val accessToken: String,
    private val coroutineScope: CoroutineScope
) {
    // Estados de datos principales
    var branchesData by mutableStateOf<BranchesData?>(null)
        private set
    
    var branchDetails by mutableStateOf<BranchData?>(null)
        private set
    
    var selectedBranch by mutableStateOf<BranchInfo?>(null)
        private set
    
    // Estados de carga
    var isLoading by mutableStateOf(false)
        private set
    
    // Estados de error
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    // Estados de diálogos
    var showCreateDialog by mutableStateOf(false)
        private set
    
    var showEditDialog by mutableStateOf(false)
        private set
    
    var showSellersDialog by mutableStateOf(false)
        private set
    
    var showDeleteDialog by mutableStateOf(false)
        private set
    
    var showDetailsDialog by mutableStateOf(false)
        private set
    
    var showFilters by mutableStateOf(false)
        private set
    
    // Estados de paginación y filtros
    var currentPage by mutableStateOf(0)
        private set
    
    var filterStatus by mutableStateOf<String?>(null)
        private set
    
    /**
     * Carga las sucursales con filtros y paginación
     */
    fun loadBranches() {
        coroutineScope.launch {
            if (adminId > 0 && accessToken.isNotEmpty()) {
                isLoading = true
                errorMessage = null
                
                try {
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
                        },
                        onFailure = { error ->
                            errorMessage = error.message ?: "Error cargando sucursales"
                            isLoading = false
                        }
                    )
                } catch (e: Exception) {
                    errorMessage = "Error de conexión: ${e.message}"
                    isLoading = false
                }
            }
        }
    }
    
    /**
     * Carga los detalles de una sucursal específica
     */
    fun loadBranchDetails(branchId: Int) {
        coroutineScope.launch {
            branchService.getBranchDetails(
                branchId = branchId,
                adminId = adminId,
                accessToken = accessToken
            ).fold(
                onSuccess = { details ->
                    branchDetails = details
                    showDetailsDialog = true
                },
                onFailure = { error ->
                    errorMessage = "Error cargando detalles: ${error.message}"
                }
            )
        }
    }
    
    /**
     * Crea una nueva sucursal
     */
    fun createBranch(name: String, code: String, address: String) {
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
                    loadBranches() // Recargar datos
                },
                onFailure = { error ->
                    errorMessage = "Error creando sucursal: ${error.message}"
                }
            )
        }
    }
    
    /**
     * Actualiza una sucursal existente
     */
    fun updateBranch(
        branchId: Int,
        name: String,
        code: String,
        address: String,
        isActive: Boolean
    ) {
        coroutineScope.launch {
            branchService.updateBranch(
                branchId = branchId,
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
                                        sellersCount = updatedBranch.sellersCount
                                    )
                                } else it 
                            }
                        )
                    }
                },
                onFailure = { error ->
                    errorMessage = "Error actualizando sucursal: ${error.message}"
                    showEditDialog = false
                }
            )
        }
    }
    
    /**
     * Elimina una sucursal
     */
    fun deleteBranch(branchId: Int) {
        coroutineScope.launch {
            branchService.deleteBranch(
                branchId = branchId,
                adminId = adminId,
                accessToken = accessToken
            ).fold(
                onSuccess = { success ->
                    if (success) {
                        showDeleteDialog = false
                        currentPage = 0
                        loadBranches() // Recargar la lista
                    } else {
                        errorMessage = "Error al eliminar la sucursal"
                        showDeleteDialog = false
                    }
                },
                onFailure = { error ->
                    errorMessage = "Error al eliminar la sucursal: ${error.message}"
                    showDeleteDialog = false
                }
            )
        }
    }
    
    /**
     * Cambia el filtro de estado
     */
    fun changeFilterStatus(status: String?) {
        filterStatus = status
        currentPage = 0
        loadBranches()
    }
    
    /**
     * Cambia la página actual
     */
    fun changeCurrentPage(page: Int) {
        currentPage = page
        loadBranches()
    }
    
    /**
     * Muestra el diálogo de creación
     */
    fun showCreateDialog() {
        showCreateDialog = true
    }
    
    /**
     * Cierra el diálogo de creación
     */
    fun dismissCreateDialog() {
        showCreateDialog = false
    }
    
    /**
     * Muestra el diálogo de edición
     */
    fun showEditDialog(branch: BranchInfo) {
        selectedBranch = branch
        showEditDialog = true
    }
    
    /**
     * Cierra el diálogo de edición
     */
    fun dismissEditDialog() {
        showEditDialog = false
        selectedBranch = null
    }
    
    /**
     * Muestra el diálogo de vendedores
     */
    fun showSellersDialog(branch: BranchInfo) {
        selectedBranch = branch
        showSellersDialog = true
    }
    
    /**
     * Cierra el diálogo de vendedores
     */
    fun dismissSellersDialog() {
        showSellersDialog = false
        selectedBranch = null
    }
    
    /**
     * Muestra el diálogo de eliminación
     */
    fun showDeleteDialog(branch: BranchInfo) {
        selectedBranch = branch
        showDeleteDialog = true
    }
    
    /**
     * Cierra el diálogo de eliminación
     */
    fun dismissDeleteDialog() {
        showDeleteDialog = false
        selectedBranch = null
    }
    
    /**
     * Muestra el diálogo de detalles
     */
    fun showDetailsDialog(branch: BranchInfo) {
        selectedBranch = branch
        loadBranchDetails(branch.branchId)
    }
    
    /**
     * Cierra el diálogo de detalles
     */
    fun dismissDetailsDialog() {
        showDetailsDialog = false
        branchDetails = null
        selectedBranch = null
    }
    
    /**
     * Alterna la visibilidad de filtros
     */
    fun toggleFilters() {
        showFilters = !showFilters
    }
    
    /**
     * Limpia todos los errores
     */
    fun clearErrors() {
        errorMessage = null
    }
    
    /**
     * Recarga los datos desde la primera página
     */
    fun refresh() {
        currentPage = 0
        loadBranches()
    }
    
    /**
     * Calcula las estadísticas de las sucursales
     */
    fun getBranchStats(): BranchStats {
        val branches = branchesData?.branches ?: emptyList()
        return BranchStats(
            total = branches.size,
            active = branches.count { it.isActive },
            inactive = branches.count { !it.isActive },
            totalSellers = branches.sumOf { it.sellersCount }
        )
    }
    
    /**
     * Alterna el estado activo/inactivo de una sucursal
     */
    fun toggleBranchStatus(branch: BranchInfo) {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            
            try {
                val result = branchService.updateBranch(
                    branchId = branch.branchId,
                    adminId = adminId,
                    name = branch.name,
                    code = branch.code,
                    address = branch.address,
                    isActive = !branch.isActive, // Cambiar el estado
                    accessToken = accessToken
                )
                
                if (result.isSuccess) {
                    // Recargar la lista de sucursales para reflejar el cambio
                    loadBranches()
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Error al actualizar la sucursal"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error inesperado al actualizar la sucursal"
            } finally {
                isLoading = false
            }
        }
    }
    
    /**
     * Elimina una sucursal
     */
    fun deleteBranch(branch: BranchInfo) {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            
            try {
                val result = branchService.deleteBranch(
                    branchId = branch.branchId,
                    adminId = adminId,
                    accessToken = accessToken
                )
                
                if (result.isSuccess) {
                    // Recargar la lista de sucursales para reflejar el cambio
                    loadBranches()
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Error al eliminar la sucursal"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error inesperado al eliminar la sucursal"
            } finally {
                isLoading = false
            }
        }
    }
}

