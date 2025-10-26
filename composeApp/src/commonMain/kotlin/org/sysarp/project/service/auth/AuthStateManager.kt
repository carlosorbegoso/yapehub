package org.sysarp.project.service.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sysarp.project.data.AuthState
import org.sysarp.project.data.UserProfile
import org.sysarp.project.utils.UserProfileFactory

/**
 * Manager para gestionar el estado de autenticación
 * Responsabilidad única: Manejar estado de autenticación
 */
class AuthStateManager {
    
    private val _authState = MutableStateFlow(AuthState.LOADING)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    /**
     * Establecer estado de autenticación
     */
    fun setAuthState(state: AuthState) {
        _authState.value = state
    }
    
    /**
     * Actualizar perfil de usuario
     */
    fun updateUserProfile(
        id: Int,
        name: String,
        email: String,
        role: String,
        branchId: Int?,
        branchName: String?,
        branchCode: String? = null,
        isVerified: Boolean,
        sellerId: Int?,
        affiliationCode: String? = null
    ) {
        _userProfile.value = UserProfileFactory.createSellerProfile(
            id = id,
            name = name,
            email = email,
            role = role,
            branchId = branchId,
            branchName = branchName,
            branchCode = branchCode,
            isVerified = isVerified,
            sellerId = sellerId,
            affiliationCode = affiliationCode
        )
    }
    
    /**
     * Crear perfil de administrador
     */
    fun createAdminProfile(
        id: Int,
        email: String,
        businessId: Int?,
        businessName: String?,
        isVerified: Boolean
    ) {
        _userProfile.value = UserProfileFactory.createAdminProfile(
            id = id,
            email = email,
            businessId = businessId,
            businessName = businessName,
            isVerified = isVerified
        )
    }
    
    /**
     * Limpiar datos de usuario
     */
    fun clearUserData() {
        _userProfile.value = null
        _authState.value = AuthState.UNAUTHENTICATED
    }
}
