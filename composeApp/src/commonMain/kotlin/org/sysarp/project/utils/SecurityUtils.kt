package org.sysarp.project.utils

/**
 * Utilidades de seguridad para validación y sanitización de datos
 * Previene inyección SQL y otros ataques de seguridad
 */
object SecurityUtils {
    
    /**
     * Sanitiza una entrada de texto removiendo caracteres peligrosos
     * @param input Texto a sanitizar
     * @return Texto sanitizado
     */
    fun sanitizeInput(input: String): String {
        return input
            .replace(Regex("['\"\\\\;<>]"), "") // Remover comillas, backslashes, punto y coma, < >
            .replace(Regex("\\s+"), " ") // Normalizar espacios múltiples
            .trim()
    }
    
    /**
     * Valida si un RUC o DNI es válido
     * @param ruc RUC o DNI a validar
     * @return true si es válido
     */
    fun isValidRucOrDni(ruc: String): Boolean {
        val cleanRuc = ruc.replace(Regex("[^0-9]"), "")
        return cleanRuc.length in 8..11 && cleanRuc.all { it.isDigit() }
    }
    
    /**
     * Valida si un email es válido
     * @param email Email a validar
     * @return true si es válido
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(email)
    }
    
    /**
     * Valida si una contraseña es segura
     * @param password Contraseña a validar
     * @return true si es válida
     */
    fun isValidPassword(password: String): Boolean {
        if (password.isBlank()) return false
        
        if (password.length < 8) return false
        
        val dangerousChars = Regex("['\"\\\\;<>]")
        if (dangerousChars.containsMatchIn(password)) return false
        
        return true
    }
    
    /**
     * Valida si un teléfono es válido
     * @param phone Teléfono a validar
     * @return true si es válido
     */
    fun isValidPhone(phone: String): Boolean {
        if (phone.isBlank()) return false
        
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
        val digitsOnly = cleanPhone.replace("+", "")
        return digitsOnly.length in 9..15 && digitsOnly.all { it.isDigit() }
    }
    
    /**
     * Valida si un nombre es válido
     * @param name Nombre a validar
     * @return true si es válido
     */
    fun isValidName(name: String): Boolean {
        if (name.isBlank()) return false
        
        if (name.length < 2) return false
        
        val dangerousChars = Regex("['\"\\\\;<>]")
        if (dangerousChars.containsMatchIn(name)) return false
        
        return true
    }
    
    /**
     * Valida si una dirección es válida
     * @param address Dirección a validar
     * @return true si es válida
     */
    fun isValidAddress(address: String): Boolean {
        if (address.isBlank()) return false
        
        // Validar longitud mínima
        if (address.length < 5) return false
        
        // Validar que no contenga caracteres peligrosos
        val dangerousChars = Regex("['\"\\\\;<>]")
        if (dangerousChars.containsMatchIn(address)) return false
        
        return true
    }
    
    /**
     * Limpia un número de teléfono para almacenamiento
     * @param phone Teléfono a limpiar
     * @return Teléfono limpio
     */
    fun cleanPhoneNumber(phone: String): String {
        return phone.replace(Regex("[^0-9+]"), "")
    }

    /**
     * Normaliza un email para almacenamiento
     * @param email Email a normalizar
     * @return Email normalizado
     */
    fun normalizeEmail(email: String): String {
        return email.lowercase().trim()
    }
}
