package org.sysarp.project.ui.components.seller_unified.validation

/**
 * Utilidades de validación para el formulario de vendedor
 */
object SellerFormValidation {
    
    /**
     * Valida el código de afiliación
     * @param code Código a validar
     * @return true si es válido, false en caso contrario
     */
    fun isValidAffiliationCode(code: String): Boolean {
        return code.length >= 6 && 
               code.all { it.isLetterOrDigit() } &&
               code.isNotBlank()
    }
    
    /**
     * Valida el nombre del vendedor
     * @param name Nombre a validar
     * @return true si es válido, false en caso contrario
     */
    fun isValidSellerName(name: String): Boolean {
        return name.length >= 3 && 
               name.all { it.isLetter() || it == ' ' || it == '-' } &&
               name.isNotBlank()
    }
    
    /**
     * Valida el número de teléfono
     * @param phone Teléfono a validar
     * @return true si es válido, false en caso contrario
     */
    fun isValidPhone(phone: String): Boolean {
        return phone.length == 9 && 
               phone.all { it.isDigit() } &&
               phone.isNotBlank()
    }
    
    /**
     * Valida todo el formulario
     * @param affiliationCode Código de afiliación
     * @param sellerName Nombre del vendedor
     * @param phone Teléfono
     * @return true si todo el formulario es válido, false en caso contrario
     */
    fun isFormValid(affiliationCode: String, sellerName: String, phone: String): Boolean {
        return isValidAffiliationCode(affiliationCode) &&
               isValidSellerName(sellerName) &&
               isValidPhone(phone)
    }
    
    /**
     * Sanitiza el código de afiliación
     * @param code Código original
     * @return Código sanitizado
     */
    fun sanitizeAffiliationCode(code: String): String {
        return code.trim().uppercase()
    }
    
    /**
     * Sanitiza el nombre del vendedor
     * @param name Nombre original
     * @return Nombre sanitizado
     */
    fun sanitizeSellerName(name: String): String {
        return name.trim().replace(Regex("\\s+"), " ")
    }
    
    /**
     * Sanitiza el número de teléfono
     * @param phone Teléfono original
     * @return Teléfono sanitizado
     */
    fun sanitizePhone(phone: String): String {
        return phone.trim()
    }
    
    /**
     * Obtiene el mensaje de error para el código de afiliación
     * @param code Código a validar
     * @return Mensaje de error o null si es válido
     */
    fun getAffiliationCodeError(code: String): String? {
        return when {
            code.isBlank() -> null
            code.length < 6 -> "El código debe tener al menos 6 caracteres"
            !code.all { it.isLetterOrDigit() } -> "El código solo puede contener letras y números"
            else -> null
        }
    }
    
    /**
     * Obtiene el mensaje de error para el nombre del vendedor
     * @param name Nombre a validar
     * @return Mensaje de error o null si es válido
     */
    fun getSellerNameError(name: String): String? {
        return when {
            name.isBlank() -> null
            name.length < 3 -> "El nombre debe tener al menos 3 caracteres"
            !name.all { it.isLetter() || it == ' ' || it == '-' } -> "El nombre solo puede contener letras, espacios y guiones"
            else -> null
        }
    }
    
    /**
     * Obtiene el mensaje de error para el teléfono
     * @param phone Teléfono a validar
     * @return Mensaje de error o null si es válido
     */
    fun getPhoneError(phone: String): String? {
        return when {
            phone.isBlank() -> null
            phone.length != 9 -> "El teléfono debe tener exactamente 9 dígitos"
            !phone.all { it.isDigit() } -> "El teléfono solo puede contener números"
            else -> null
        }
    }
    
    /**
     * Cuenta los campos completados
     * @param affiliationCode Código de afiliación
     * @param sellerName Nombre del vendedor
     * @param phone Teléfono
     * @return Número de campos completados
     */
    fun getCompletedFieldsCount(affiliationCode: String, sellerName: String, phone: String): Int {
        return listOf(
            affiliationCode.isNotBlank(),
            sellerName.isNotBlank(),
            phone.isNotBlank()
        ).count { it }
    }
    
    /**
     * Verifica si un campo tiene error visual
     * @param value Valor del campo
     * @param isValid Función de validación
     * @return true si debe mostrar error visual
     */
    fun shouldShowFieldError(value: String, isValid: (String) -> Boolean): Boolean {
        return value.isNotBlank() && !isValid(value)
    }
}
