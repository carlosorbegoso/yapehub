package org.sysarp.project.ui.admin.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sysarp.project.data.AdminProfileData
import org.sysarp.project.data.UpdateAdminProfileRequest
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.admin.AdminService
import org.sysarp.project.service.auth.AuthService

/**
 * Estado y lógica de negocio para AdminProfileScreen
 */
class AdminProfileState(
    private val adminService: AdminService,
    private val authService: AuthService,
    private val coroutineScope: CoroutineScope
) {
    // Estados de datos
    var profileData by mutableStateOf<AdminProfileData?>(null)
        private set
    
    // Estados de UI
    var isLoading by mutableStateOf(false)
        private set
    
    var isEditing by mutableStateOf(false)
        private set
    
    // Estados de mensajes
    var errorMessage by mutableStateOf("")
        private set
    
    var successMessage by mutableStateOf("")
        private set
    
    // Campos editables
    var businessName by mutableStateOf("")
        private set
    
    var businessType by mutableStateOf("")
        private set
    
    var phone by mutableStateOf("")
        private set
    
    var address by mutableStateOf("")
        private set
    
    var contactName by mutableStateOf("")
        private set
    
    // Estados de usuario
    var userProfile: UserProfile? = null
        private set
    
    var accessToken: String? = null
        private set
    
    /**
     * Establece los datos del perfil
     */
    fun updateProfileData(data: AdminProfileData?) {
        profileData = data
        if (data != null) {
            businessName = data.businessName
            businessType = data.businessType ?: ""
            phone = data.phone
            address = data.address
            contactName = data.contactName
        }
    }
    
    /**
     * Establece el estado de carga
     */
    fun updateLoading(loading: Boolean) {
        isLoading = loading
    }
    
    /**
     * Establece el estado de edición
     */
    fun updateEditing(editing: Boolean) {
        isEditing = editing
    }
    
    /**
     * Establece el mensaje de error
     */
    fun updateErrorMessage(message: String) {
        errorMessage = message
    }
    
    /**
     * Limpia el mensaje de error
     */
    fun clearErrorMessage() {
        errorMessage = ""
    }
    
    /**
     * Establece el mensaje de éxito
     */
    fun updateSuccessMessage(message: String) {
        successMessage = message
    }
    
    /**
     * Limpia el mensaje de éxito
     */
    fun clearSuccessMessage() {
        successMessage = ""
    }
    
    /**
     * Establece el nombre del negocio
     */
    fun updateBusinessName(name: String) {
        businessName = name
    }
    
    /**
     * Establece el tipo de negocio
     */
    fun updateBusinessType(type: String) {
        businessType = type
    }
    
    /**
     * Establece el teléfono
     */
    fun updatePhone(phoneNumber: String) {
        phone = phoneNumber
    }
    
    /**
     * Establece la dirección
     */
    fun updateAddress(addressText: String) {
        address = addressText
    }
    
    /**
     * Establece el nombre de contacto
     */
    fun updateContactName(name: String) {
        contactName = name
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
     * Carga el perfil del administrador
     */
    fun loadProfile(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (userProfile?.adminId != null && accessToken != null) {
            coroutineScope.launch {
                updateLoading(true)
                clearErrorMessage()
                
                adminService.getAdminProfile(
                    userId = userProfile?.adminId?.toInt() ?: 0,
                    token = accessToken ?: ""
                ).fold(
                    onSuccess = { profile ->
                        updateProfileData(profile)
                        updateLoading(false)
                        onSuccess()
                    },
                    onFailure = { error ->
                        updateErrorMessage("Error cargando perfil: ${error.message}")
                        updateLoading(false)
                        onFailure(error.message ?: "Error desconocido")
                    }
                )
            }
        }
    }
    
    /**
     * Guarda los cambios del perfil
     */
    fun saveProfile(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (accessToken != null && userProfile?.adminId != null) {
            coroutineScope.launch {
                updateLoading(true)
                clearErrorMessage()
                
                val updateRequest = UpdateAdminProfileRequest(
                    businessName = businessName.takeIf { it.isNotEmpty() },
                    businessType = businessType.takeIf { it.isNotEmpty() },
                    phone = phone.takeIf { it.isNotEmpty() },
                    address = address.takeIf { it.isNotEmpty() },
                    contactName = contactName.takeIf { it.isNotEmpty() }
                )
                
                try {
                    adminService.updateAdminProfile(
                        userId = userProfile?.adminId?.toInt() ?: 0,
                        token = accessToken ?: "",
                        profileData = updateRequest
                    ).fold(
                        onSuccess = { updatedProfile ->
                            updateProfileData(updatedProfile)
                            updateEditing(false)
                            updateSuccessMessage("Perfil actualizado exitosamente")
                            updateLoading(false)
                            onSuccess()
                        },
                        onFailure = { error ->
                            updateErrorMessage("Error actualizando perfil: ${error.message}")
                            updateLoading(false)
                            onFailure(error.message ?: "Error al actualizar perfil")
                        }
                    )
                } catch (e: Exception) {
                    updateErrorMessage("Error actualizando perfil: ${e.message}")
                    updateLoading(false)
                    onFailure(e.message ?: "Error al actualizar perfil")
                }
            }
        }
    }
    
    /**
     * Inicia el modo de edición
     */
    fun startEditing() {
        updateEditing(true)
        clearSuccessMessage()
    }
    
    /**
     * Cancela la edición
     */
    fun cancelEditing() {
        updateEditing(false)
        // Restaurar valores originales
        profileData?.let { data ->
            businessName = data.businessName
            businessType = data.businessType ?: ""
            phone = data.phone
            address = data.address
            contactName = data.contactName
        }
        clearErrorMessage()
        clearSuccessMessage()
    }
    
    /**
     * Verifica si se puede cargar el perfil
     */
    fun canLoadProfile(): Boolean {
        return userProfile?.adminId != null && accessToken != null
    }
    
    /**
     * Verifica si hay datos del perfil
     */
    fun hasProfileData(): Boolean {
        return profileData != null
    }
    
    /**
     * Verifica si hay un error
     */
    fun hasError(): Boolean {
        return errorMessage.isNotEmpty()
    }
    
    /**
     * Verifica si hay un mensaje de éxito
     */
    fun hasSuccessMessage(): Boolean {
        return successMessage.isNotEmpty()
    }
    
    /**
     * Verifica si está cargando
     */
    fun isCurrentlyLoading(): Boolean {
        return isLoading
    }
    
    /**
     * Verifica si está en modo de edición
     */
    fun isCurrentlyEditing(): Boolean {
        return isEditing
    }
    
    /**
     * Obtiene los datos del perfil actual
     */
    fun getCurrentProfileData(): AdminProfileData? {
        return profileData
    }
    
    /**
     * Obtiene el mensaje de error actual
     */
    fun getCurrentErrorMessage(): String {
        return errorMessage
    }
    
    /**
     * Obtiene el mensaje de éxito actual
     */
    fun getCurrentSuccessMessage(): String {
        return successMessage
    }
    
    /**
     * Obtiene el nombre del negocio actual
     */
    fun getCurrentBusinessName(): String {
        return businessName
    }
    
    /**
     * Obtiene el tipo de negocio actual
     */
    fun getCurrentBusinessType(): String {
        return businessType
    }
    
    /**
     * Obtiene el teléfono actual
     */
    fun getCurrentPhone(): String {
        return phone
    }
    
    /**
     * Obtiene la dirección actual
     */
    fun getCurrentAddress(): String {
        return address
    }
    
    /**
     * Obtiene el nombre de contacto actual
     */
    fun getCurrentContactName(): String {
        return contactName
    }
    
    /**
     * Verifica si el formulario está completo
     */
    fun isFormComplete(): Boolean {
        return businessName.isNotEmpty() && 
               businessType.isNotEmpty() && 
               phone.isNotEmpty() && 
               address.isNotEmpty() && 
               contactName.isNotEmpty()
    }
    
    /**
     * Resetea todos los estados
     */
    fun resetAll() {
        profileData = null
        updateLoading(false)
        updateEditing(false)
        clearErrorMessage()
        clearSuccessMessage()
        updateBusinessName("")
        updateBusinessType("")
        updatePhone("")
        updateAddress("")
        updateContactName("")
    }
}
