package org.sysarp.project.ui.admin.screens.management

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.sysarp.project.data.BranchInfo

/**
 * Estado y lógica de negocio para BranchManagementComponents
 */
class BranchManagementComponentsState {
    // Estados de datos
    var branchStats by mutableStateOf<BranchStats?>(null)
        private set
    
    var branches by mutableStateOf<List<BranchInfo>>(emptyList())
        private set
    
    // Estados de UI
    var isLoading by mutableStateOf(false)
        private set
    
    var showFilters by mutableStateOf(false)
        private set
    
    var filterStatus by mutableStateOf<String?>(null)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    var selectedBranch by mutableStateOf<BranchInfo?>(null)
        private set
    
    // Estados de paginación
    var currentPage by mutableStateOf(1)
        private set
    
    var totalPages by mutableStateOf(1)
        private set
    
    var hasNextPage by mutableStateOf(false)
        private set
    
    var hasPreviousPage by mutableStateOf(false)
        private set
    
    /**
     * Establece las estadísticas de sucursales
     */
    fun updateBranchStats(stats: BranchStats?) {
        branchStats = stats
    }
    
    /**
     * Establece la lista de sucursales
     */
    fun updateBranches(branchesList: List<BranchInfo>) {
        branches = branchesList
    }
    
    /**
     * Establece el estado de carga
     */
    fun updateLoading(loading: Boolean) {
        isLoading = loading
    }
    
    /**
     * Establece la visibilidad de los filtros
     */
    fun updateShowFilters(show: Boolean) {
        showFilters = show
    }
    
    /**
     * Establece el filtro de estado
     */
    fun updateFilterStatus(status: String?) {
        filterStatus = status
    }
    
    /**
     * Establece el mensaje de error
     */
    fun updateErrorMessage(message: String?) {
        errorMessage = message
    }
    
    /**
     * Limpia el mensaje de error
     */
    fun clearErrorMessage() {
        errorMessage = null
    }
    
    /**
     * Establece la sucursal seleccionada
     */
    fun updateSelectedBranch(branch: BranchInfo?) {
        selectedBranch = branch
    }
    
    /**
     * Establece la página actual
     */
    fun updateCurrentPage(page: Int) {
        currentPage = page
    }
    
    /**
     * Establece el total de páginas
     */
    fun updateTotalPages(total: Int) {
        totalPages = total
    }
    
    /**
     * Establece si hay página siguiente
     */
    fun updateHasNextPage(hasNext: Boolean) {
        hasNextPage = hasNext
    }
    
    /**
     * Establece si hay página anterior
     */
    fun updateHasPreviousPage(hasPrevious: Boolean) {
        hasPreviousPage = hasPrevious
    }
    
    /**
     * Alterna la visibilidad de los filtros
     */
    fun toggleFilters() {
        showFilters = !showFilters
    }
    
    /**
     * Aplica un filtro de estado
     */
    fun applyFilter(status: String?) {
        filterStatus = status
        currentPage = 1 // Resetear a la primera página
    }
    
    /**
     * Limpia todos los filtros
     */
    fun clearFilters() {
        filterStatus = null
        currentPage = 1
    }
    
    /**
     * Navega a la página siguiente
     */
    fun nextPage() {
        if (hasNextPage) {
            currentPage++
        }
    }
    
    /**
     * Navega a la página anterior
     */
    fun previousPage() {
        if (hasPreviousPage) {
            currentPage--
        }
    }
    
    /**
     * Navega a una página específica
     */
    fun goToPage(page: Int) {
        if (page in 1..totalPages) {
            currentPage = page
        }
    }
    
    /**
     * Verifica si hay datos de estadísticas
     */
    fun hasBranchStats(): Boolean {
        return branchStats != null
    }
    
    /**
     * Verifica si hay sucursales
     */
    fun hasBranches(): Boolean {
        return branches.isNotEmpty()
    }
    
    /**
     * Verifica si hay un error
     */
    fun hasError(): Boolean {
        return errorMessage != null
    }
    
    /**
     * Verifica si está cargando
     */
    fun isCurrentlyLoading(): Boolean {
        return isLoading
    }
    
    /**
     * Verifica si los filtros están visibles
     */
    fun areFiltersVisible(): Boolean {
        return showFilters
    }
    
    /**
     * Verifica si hay un filtro activo
     */
    fun hasActiveFilter(): Boolean {
        return filterStatus != null
    }
    
    /**
     * Verifica si hay una sucursal seleccionada
     */
    fun hasSelectedBranch(): Boolean {
        return selectedBranch != null
    }
    
    /**
     * Obtiene las estadísticas actuales
     */
    fun getCurrentBranchStats(): BranchStats? {
        return branchStats
    }
    
    /**
     * Obtiene la lista actual de sucursales
     */
    fun getCurrentBranches(): List<BranchInfo> {
        return branches
    }
    
    /**
     * Obtiene el mensaje de error actual
     */
    fun getCurrentErrorMessage(): String? {
        return errorMessage
    }
    
    /**
     * Obtiene el filtro de estado actual
     */
    fun getCurrentFilterStatus(): String? {
        return filterStatus
    }
    
    /**
     * Obtiene la sucursal seleccionada actual
     */
    fun getCurrentSelectedBranch(): BranchInfo? {
        return selectedBranch
    }
    
    /**
     * Obtiene la página actual
     */
    fun getCurrentPageNumber(): Int {
        return currentPage
    }
    
    /**
     * Obtiene el total de páginas
     */
    fun getTotalPagesCount(): Int {
        return totalPages
    }
    
    /**
     * Obtiene las sucursales filtradas
     */
    fun getFilteredBranches(): List<BranchInfo> {
        return if (filterStatus == null) {
            branches
        } else {
            branches.filter { branch ->
                when (filterStatus) {
                    "active" -> branch.isActive == true
                    "inactive" -> branch.isActive == false
                    else -> true
                }
            }
        }
    }
    
    /**
     * Cambia la página actual
     */
    fun changeCurrentPage(page: Int) {
        currentPage = page
    }
    
    /**
     * Cambia el filtro de estado
     */
    fun changeFilterStatus(status: String?) {
        filterStatus = status
    }
    
    /**
     * Alterna la visibilidad de los filtros
     */
    fun toggleFilters() {
        showFilters = !showFilters
    }

}

/**
 * Datos de estadísticas de sucursales
 */
data class BranchStats(
    val total: Int,
    val active: Int,
    val inactive: Int,
    val totalSellers: Int
)

/**
 * Datos de estadística individual
 */
data class BranchStat(
    val title: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color,
    val trend: String
)
