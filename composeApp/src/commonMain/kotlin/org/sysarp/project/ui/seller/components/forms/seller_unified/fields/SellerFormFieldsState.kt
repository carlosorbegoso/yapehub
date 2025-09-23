package org.sysarp.project.ui.components.seller_unified.fields

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Estado y lógica de negocio para SellerFormFields
 */
class SellerFormFieldsState {
    // Estados de campos del formulario
    var affiliationCode by mutableStateOf("")
        private set
    
    var sellerName by mutableStateOf("")
        private set
    
    var phone by mutableStateOf("")
        private set
    
    // Estados de UI
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf("")
        private set
    
    var successMessage by mutableStateOf("")
        private set
    
    /**
     * Establece el código de afiliación
     */
    fun updateAffiliationCode(code: String) {
        val sanitizedValue = code.filter { it.isLetterOrDigit() }.take(10)
        affiliationCode = sanitizedValue
    }
    
    /**
     * Establece el nombre del vendedor
     */
    fun updateSellerName(name: String) {
        val sanitizedValue = name.filter { it.isLetter() || it == ' ' || it == '-' }.take(50)
        sellerName = sanitizedValue
    }
    
    /**
     * Establece el número de teléfono
     */
    fun updatePhone(phoneNumber: String) {
        val sanitizedValue = phoneNumber.filter { it.isDigit() }.take(9)
        phone = sanitizedValue
    }
    
    /**
     * Establece el estado de carga
     */
    fun updateLoading(loading: Boolean) {
        isLoading = loading
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
     * Limpia todos los mensajes
     */
    fun clearAllMessages() {
        clearErrorMessage()
        clearSuccessMessage()
    }
    
    /**
     * Verifica si el formulario es válido
     */
    fun isFormValid(): Boolean {
        return isAffiliationCodeValid() && isSellerNameValid() && isPhoneValid()
    }
    
    /**
     * Verifica si el código de afiliación es válido
     */
    fun isAffiliationCodeValid(): Boolean {
        return affiliationCode.isNotBlank() && affiliationCode.length >= 6
    }
    
    /**
     * Verifica si el nombre del vendedor es válido
     */
    fun isSellerNameValid(): Boolean {
        return sellerName.isNotBlank() && sellerName.length >= 3
    }
    
    /**
     * Verifica si el teléfono es válido
     */
    fun isPhoneValid(): Boolean {
        return phone.isNotBlank() && phone.length == 9
    }
    
    /**
     * Obtiene el número de campos completados
     */
    fun getCompletedFieldsCount(): Int {
        return listOf(
            affiliationCode.isNotBlank(),
            sellerName.isNotBlank(),
            phone.isNotBlank()
        ).count { it }
    }
    
    /**
     * Verifica si todos los campos están completos
     */
    fun areAllFieldsCompleted(): Boolean {
        return getCompletedFieldsCount() == 3
    }
    
    /**
     * Verifica si el código de afiliación tiene error
     */
    fun hasAffiliationCodeError(): Boolean {
        return affiliationCode.isNotBlank() && affiliationCode.length < 6
    }
    
    /**
     * Verifica si el nombre del vendedor tiene error
     */
    fun hasSellerNameError(): Boolean {
        return sellerName.isNotBlank() && sellerName.length < 3
    }
    
    /**
     * Verifica si el teléfono tiene error
     */
    fun hasPhoneError(): Boolean {
        return phone.isNotBlank() && phone.length != 9
    }
    
    /**
     * Obtiene el estado visual del código de afiliación
     */
    fun getAffiliationCodeVisualState(): FieldVisualState {
        return when {
            affiliationCode.isBlank() -> FieldVisualState.Neutral
            isAffiliationCodeValid() -> FieldVisualState.Valid
            else -> FieldVisualState.Error
        }
    }
    
    /**
     * Obtiene el estado visual del nombre del vendedor
     */
    fun getSellerNameVisualState(): FieldVisualState {
        return when {
            sellerName.isBlank() -> FieldVisualState.Neutral
            isSellerNameValid() -> FieldVisualState.Valid
            else -> FieldVisualState.Error
        }
    }
    
    /**
     * Obtiene el estado visual del teléfono
     */
    fun getPhoneVisualState(): FieldVisualState {
        return when {
            phone.isBlank() -> FieldVisualState.Neutral
            isPhoneValid() -> FieldVisualState.Valid
            else -> FieldVisualState.Error
        }
    }
    
    /**
     * Resetea todos los campos del formulario
     */
    fun resetForm() {
        affiliationCode = ""
        sellerName = ""
        phone = ""
        clearAllMessages()
        updateLoading(false)
    }
}

/**
 * Estados visuales de los campos
 */
enum class FieldVisualState {
    Neutral,
    Valid,
    Error
}
