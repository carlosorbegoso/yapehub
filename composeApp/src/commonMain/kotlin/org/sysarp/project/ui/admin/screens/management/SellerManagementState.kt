package org.sysarp.project.ui.admin.screens.management

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sysarp.project.data.MySeller
import org.sysarp.project.data.SellersResponse
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.SellerService
import org.sysarp.project.service.auth.AuthService

/**
 * Estado y lógica de negocio para SellerManagementScreen
 */
class SellerManagementState(
    private val sellerService: SellerService,
    private val authService: AuthService,
    private val coroutineScope: CoroutineScope
) {
    // Estados de datos
    var sellersData by mutableStateOf<SellersResponse?>(null)
        private set
    
    var selectedSeller by mutableStateOf<MySeller?>(null)
        private set
    
    // Estados de UI
    var isLoading by mutableStateOf(true)
        private set
    
    var currentPage by mutableStateOf(1)
        private set
    
    var showAddSellerDialog by mutableStateOf(false)
        private set
    
    var showEditSellerDialog by mutableStateOf(false)
        private set
    
    // Estados de error
    var error by mutableStateOf<String?>(null)
        private set
    
    // Estados de usuario
    var userProfile: UserProfile? = null
        private set
    
    var accessToken: String? = null
        private set
    
    /**
     * Establece los datos de vendedores
     */
    fun updateSellersData(data: SellersResponse?) {
        sellersData = data
    }
    
    /**
     * Establece el vendedor seleccionado
     */
    fun updateSelectedSeller(seller: MySeller?) {
        selectedSeller = seller
    }
    
    /**
     * Establece el estado de carga
     */
    fun updateLoading(loading: Boolean) {
        isLoading = loading
    }
    
    /**
     * Establece la página actual
     */
    fun updateCurrentPage(page: Int) {
        currentPage = page
    }
    
    /**
     * Establece si mostrar el diálogo de agregar vendedor
     */
    fun updateShowAddSellerDialog(show: Boolean) {
        showAddSellerDialog = show
    }
    
    /**
     * Establece si mostrar el diálogo de editar vendedor
     */
    fun updateShowEditSellerDialog(show: Boolean) {
        showEditSellerDialog = show
    }
    
    /**
     * Establece el mensaje de error
     */
    fun updateError(error: String?) {
        this.error = error
    }
    
    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        error = null
    }
    
    /**
     * Establece el perfil de usuario
     */
    fun updateUserProfile(profile: UserProfile?) {
        userProfile = profile
    }
    
    /**
     * Establece el token de acceso
     */
    fun updateAccessToken(token: String?) {
        accessToken = token
    }
    
    /**
     * Carga los vendedores
     */
    fun loadSellers(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (userProfile?.adminId != null && accessToken != null) {
            coroutineScope.launch {
                updateLoading(true)
                clearError()
                
                sellerService.getMySellers(
                    adminId = userProfile?.adminId?.toInt() ?: 0,
                    page = currentPage,
                    limit = 20,
                    token = accessToken ?: ""
                ).fold(
                    onSuccess = { response ->
                        updateSellersData(response)
                        updateLoading(false)
                        onSuccess()
                    },
                    onFailure = { e ->
                        updateError(e.message)
                        updateLoading(false)
                        onFailure(e.message ?: "Error desconocido")
                    }
                )
            }
        }
    }
    
    /**
     * Agrega un nuevo vendedor
     */
    fun addSeller(
        sellerData: SellerData,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (userProfile?.adminId != null && accessToken != null) {
            coroutineScope.launch {
                updateLoading(true)
                clearError()
                
                // TODO: Implementar createSeller en SellerService
                updateLoading(false)
                updateShowAddSellerDialog(false)
                onFailure("Función de crear vendedor no implementada")
            }
        }
    }
    
    /**
     * Edita un vendedor existente
     */
    fun editSeller(
        sellerId: Int,
        sellerData: SellerData,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (accessToken != null) {
            coroutineScope.launch {
                updateLoading(true)
                clearError()
                
                // TODO: Implementar updateSeller en SellerService
                updateLoading(false)
                updateShowEditSellerDialog(false)
                updateSelectedSeller(null)
                onFailure("Función de editar vendedor no implementada")
            }
        }
    }
    
    /**
     * Elimina un vendedor
     */
    fun deleteSeller(
        sellerId: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (accessToken != null) {
            coroutineScope.launch {
                updateLoading(true)
                clearError()
                
                // TODO: Implementar deleteSeller en SellerService
                updateLoading(false)
                onFailure("Función de eliminar vendedor no implementada")
            }
        }
    }
    
    /**
     * Verifica si se puede cargar vendedores
     */
    fun canLoadSellers(): Boolean {
        return userProfile?.adminId != null && accessToken != null
    }
    
    /**
     * Verifica si hay vendedores
     */
    fun hasSellers(): Boolean {
        return sellersData?.data?.sellers?.isNotEmpty() == true
    }
    
    /**
     * Verifica si hay un error
     */
    fun hasError(): Boolean {
        return error != null
    }
    
    /**
     * Verifica si está cargando
     */
    fun isCurrentlyLoading(): Boolean {
        return isLoading
    }
    
    /**
     * Verifica si el diálogo de agregar está visible
     */
    fun isAddDialogVisible(): Boolean {
        return showAddSellerDialog
    }
    
    /**
     * Verifica si el diálogo de editar está visible
     */
    fun isEditDialogVisible(): Boolean {
        return showEditSellerDialog
    }
    
    /**
     * Obtiene los vendedores actuales
     */
    fun getCurrentSellers(): List<MySeller> {
        return sellersData?.data?.sellers ?: emptyList()
    }
    
    /**
     * Obtiene el vendedor seleccionado actual
     */
    fun getCurrentSelectedSeller(): MySeller? {
        return selectedSeller
    }
    
    /**
     * Obtiene el mensaje de error actual
     */
    fun getCurrentError(): String? {
        return error
    }
    
    /**
     * Obtiene la página actual
     */
    fun getCurrentPageNumber(): Int {
        return currentPage
    }
    
    /**
     * Abre el diálogo de agregar vendedor
     */
    fun openAddSellerDialog() {
        showAddSellerDialog = true
    }
    
    /**
     * Cierra el diálogo de agregar vendedor
     */
    fun closeAddSellerDialog() {
        showAddSellerDialog = false
    }
    
    /**
     * Abre el diálogo de editar vendedor
     */
    fun openEditSellerDialog(seller: MySeller) {
        selectedSeller = seller
        showEditSellerDialog = true
    }
    
    /**
     * Cierra el diálogo de editar vendedor
     */
    fun closeEditSellerDialog() {
        showEditSellerDialog = false
        selectedSeller = null
    }
    
    /**
     * Cierra todos los diálogos
     */
    fun closeAllDialogs() {
        closeAddSellerDialog()
        closeEditSellerDialog()
    }
    
    /**
     * Resetea todos los estados
     */
    fun resetAll() {
        sellersData = null
        selectedSeller = null
        updateLoading(false)
        updateCurrentPage(1)
        closeAllDialogs()
        clearError()
    }
}

/**
 * Datos del vendedor para formularios
 */
data class SellerData(
    val name: String,
    val email: String,
    val phone: String,
    val isActive: Boolean
)
