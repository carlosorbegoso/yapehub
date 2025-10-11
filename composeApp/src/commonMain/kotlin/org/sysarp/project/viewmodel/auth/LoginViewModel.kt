package org.sysarp.project.viewmodel.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sysarp.project.data.LoginUserData
import org.sysarp.project.service.CredentialStorageService
import org.sysarp.project.service.auth.AuthService

/**
 * ViewModel específico para la pantalla de Login
 * Maneja toda la lógica de negocio relacionada con autenticación
 */
class LoginViewModel(
    private val authService: AuthService,
    private val credentialStorageService: CredentialStorageService,
    private val coroutineScope: CoroutineScope
) {
    
    // Estados de UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    // Estados de validación
    private val _isEmailValid = MutableStateFlow(false)
    val isEmailValid: StateFlow<Boolean> = _isEmailValid.asStateFlow()
    
    private val _isPasswordValid = MutableStateFlow(false)
    val isPasswordValid: StateFlow<Boolean> = _isPasswordValid.asStateFlow()
    
    private val _isFormValid = MutableStateFlow(false)
    val isFormValid: StateFlow<Boolean> = _isFormValid.asStateFlow()
    
    // Estados de credenciales guardadas
    private val _savedCredentials = MutableStateFlow<Pair<String, String>?>(null)
    val savedCredentials: StateFlow<Pair<String, String>?> = _savedCredentials.asStateFlow()
    
    init {
        loadSavedCredentials()
    }
    
    /**
     * Cargar credenciales guardadas
     */
    private fun loadSavedCredentials() {
        coroutineScope.launch {
            try {
                val savedCredentials = credentialStorageService.getSavedCredentials()
                
                if (savedCredentials != null) {
                    _savedCredentials.value = Pair(savedCredentials.email, savedCredentials.password)
                }
            } catch (e: Exception) {
                // Error silencioso - no es crítico
            }
        }
    }
    
    /**
     * Validar email
     */
    fun validateEmail(email: String) {
        val isValid = email.isNotEmpty() && 
                     email.contains("@") && 
                     email.contains(".")
        _isEmailValid.value = isValid
        updateFormValidity()
    }
    
    /**
     * Validar contraseña
     */
    fun validatePassword(password: String) {
        val isValid = password.length >= 6
        _isPasswordValid.value = isValid
        updateFormValidity()
    }
    
    /**
     * Actualizar validez del formulario
     */
    private fun updateFormValidity() {
        _isFormValid.value = _isEmailValid.value && _isPasswordValid.value
    }
    
    /**
     * Realizar login
     */
    fun login(
        email: String,
        password: String,
        rememberCredentials: Boolean = false,
        onSuccess: (LoginUserData) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (!_isFormValid.value) {
            _errorMessage.value = "Por favor completa todos los campos correctamente"
            return
        }
        
        coroutineScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            
            try {
                val result = authService.loginAdmin(email, password)
                
                result.fold(
                    onSuccess = { loginData ->
                        _isLoading.value = false
                        _successMessage.value = "Login exitoso"
                        
                        // Guardar credenciales si se solicita
                        if (rememberCredentials) {
                            credentialStorageService.saveCredentials(email, password)
                        }
                        
                        onSuccess(loginData)
                    },
                    onFailure = { error ->
                        _isLoading.value = false
                        _errorMessage.value = error.message ?: "Error desconocido"
                        onFailure(error.message ?: "Error desconocido")
                    }
                )
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Error inesperado"
                onFailure(e.message ?: "Error inesperado")
            }
        }
    }
    
    /**
     * Limpiar mensajes
     */
    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
    
    /**
     * Usar credenciales guardadas
     */
    fun useSavedCredentials(): Pair<String, String>? {
        return _savedCredentials.value
    }
    
    /**
     * Eliminar credenciales guardadas
     */
    fun clearSavedCredentials() {
        coroutineScope.launch {
            credentialStorageService.clearCredentials()
            _savedCredentials.value = null
        }
    }
}
