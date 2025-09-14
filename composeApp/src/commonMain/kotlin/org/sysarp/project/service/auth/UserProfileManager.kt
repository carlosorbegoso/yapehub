package org.sysarp.project.service.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sysarp.project.data.LoginUserData
import org.sysarp.project.data.SellerUserData
import org.sysarp.project.data.UserProfile
import org.sysarp.project.data.UserRole
import org.sysarp.project.utils.Logger
import org.sysarp.project.utils.UserProfileFactory

/**
 * Gestor especializado para perfiles de usuario
 * Responsabilidad única: Manejo de perfiles de usuario
 */
class UserProfileManager {
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    /**
     * Guarda el perfil del usuario
     */
    fun saveUserProfile(profile: UserProfile) {
        Logger.auth("USER_PROFILE_MANAGER", "Guardando perfil de usuario: ${profile.email}")
        
        _userProfile.value = profile
        
        Logger.success("USER_PROFILE_MANAGER", "Perfil guardado exitosamente")
        Logger.debug("USER_PROFILE_MANAGER", "ID: ${profile.id}, Role: ${profile.role}")
    }
    
    /**
     * Obtiene el perfil actual del usuario
     */
    fun getCurrentProfile(): UserProfile? {
        return _userProfile.value
    }
    
    /**
     * Verifica si el usuario está autenticado
     */
    fun isAuthenticated(): Boolean {
        return _userProfile.value != null
    }
    
    /**
     * Verifica si el usuario es administrador
     */
    fun isAdmin(): Boolean {
        return _userProfile.value?.role == UserRole.ADMIN
    }
    
    /**
     * Verifica si el usuario es vendedor
     */
    fun isVendor(): Boolean {
        return _userProfile.value?.role == UserRole.VENDOR
    }
    
    /**
     * Obtiene el sellerId del usuario actual
     */
    fun getSellerId(): Int? {
        return _userProfile.value?.sellerId?.toIntOrNull()
    }
    
    /**
     * Obtiene el businessId del usuario actual
     */
    fun getBusinessId(): Int? {
        return _userProfile.value?.businessId
    }
    
    /**
     * Actualiza información específica del perfil
     */
    fun updateProfileInfo(
        name: String? = null,
        email: String? = null,
        businessName: String? = null
    ) {
        val currentProfile = _userProfile.value ?: return
        
        val updatedProfile = currentProfile.copy(
            name = name ?: currentProfile.name,
            email = email ?: currentProfile.email,
            businessName = businessName ?: currentProfile.businessName
        )
        
        Logger.auth("USER_PROFILE_MANAGER", "Actualizando información del perfil")
        _userProfile.value = updatedProfile
        Logger.success("USER_PROFILE_MANAGER", "Perfil actualizado exitosamente")
    }
    
    /**
     * Crea un perfil de administrador desde datos de login
     */
    fun createAdminProfileFromLogin(user: LoginUserData): UserProfile {
        Logger.auth("USER_PROFILE_MANAGER", "Creando perfil de administrador desde login")
        
        val profile = UserProfileFactory.createAdminProfile(
            id = user.id,
            email = user.email,
            businessId = user.businessId,
            businessName = user.businessName,
            isVerified = user.isVerified
        )
        
        Logger.success("USER_PROFILE_MANAGER", "Perfil de administrador creado")
        return profile
    }
    
    /**
     * Crea un perfil de vendedor desde datos de login
     */
    fun createSellerProfileFromLogin(user: SellerUserData): UserProfile {
        Logger.auth("USER_PROFILE_MANAGER", "Creando perfil de vendedor desde login")
        
        val profile = UserProfileFactory.createSellerProfile(
            id = user.id,
            name = user.name,
            email = user.email,
            role = user.role ?: "SELLER",
            branchId = user.branchId,
            branchName = user.branchName,
            isVerified = user.isVerified,
            sellerId = user.sellerId
        )
        
        Logger.success("USER_PROFILE_MANAGER", "Perfil de vendedor creado")
        return profile
    }
    
    /**
     * Limpia el perfil del usuario
     */
    fun clearProfile() {
        Logger.auth("USER_PROFILE_MANAGER", "Limpiando perfil de usuario")
        
        _userProfile.value = null
        
        Logger.success("USER_PROFILE_MANAGER", "Perfil limpiado exitosamente")
    }
}
