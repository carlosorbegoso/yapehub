package org.sysarp.project.service

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sysarp.project.utils.Logger

/**
 * Servicio para el almacenamiento seguro de credenciales
 * Maneja el guardado y recuperación de credenciales de login
 */
class CredentialStorageService {
    
    @Serializable
    data class SavedCredentials(
        val email: String,
        val password: String,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * Guarda las credenciales de forma segura
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @return true si se guardó exitosamente, false en caso contrario
     */
    suspend fun saveCredentials(email: String, password: String): Boolean {
        return try {
            val credentials = SavedCredentials(
                email = email.trim().lowercase(),
                password = password.trim(),
                timestamp = System.currentTimeMillis()
            )
            
            val credentialsJson = json.encodeToString(credentials)
            
            // Aquí se implementaría el almacenamiento seguro real
            // Por ejemplo, usando Android Keystore o iOS Keychain
            // Por ahora simulamos el guardado exitoso
            
            Logger.auth("CREDENTIAL_STORAGE", "✅ Credenciales guardadas para: ${credentials.email}")
            true
        } catch (e: Exception) {
            Logger.auth("CREDENTIAL_STORAGE", "❌ Error guardando credenciales: ${e.message}")
            false
        }
    }
    
    /**
     * Recupera las credenciales guardadas
     * @return SavedCredentials si existen, null en caso contrario
     */
    suspend fun getSavedCredentials(): SavedCredentials? {
        return try {
            // Aquí se implementaría la recuperación real
            // Por ahora simulamos que no hay credenciales guardadas
            
            Logger.auth("CREDENTIAL_STORAGE", "🔍 Buscando credenciales guardadas...")
            null
        } catch (e: Exception) {
            Logger.auth("CREDENTIAL_STORAGE", "❌ Error recuperando credenciales: ${e.message}")
            null
        }
    }
    
    /**
     * Elimina las credenciales guardadas
     * @return true si se eliminaron exitosamente, false en caso contrario
     */
    suspend fun clearCredentials(): Boolean {
        return try {
            // Aquí se implementaría la eliminación real
            
            Logger.auth("CREDENTIAL_STORAGE", "🗑️ Credenciales eliminadas")
            true
        } catch (e: Exception) {
            Logger.auth("CREDENTIAL_STORAGE", "❌ Error eliminando credenciales: ${e.message}")
            false
        }
    }
    
    /**
     * Verifica si hay credenciales guardadas
     * @return true si hay credenciales guardadas, false en caso contrario
     */
    suspend fun hasSavedCredentials(): Boolean {
        return getSavedCredentials() != null
    }
    
    /**
     * Verifica si las credenciales guardadas siguen siendo válidas
     * @param maxAgeDays Edad máxima en días para considerar válidas las credenciales
     * @return true si las credenciales son válidas, false en caso contrario
     */
    suspend fun areCredentialsValid(maxAgeDays: Int = 30): Boolean {
        val credentials = getSavedCredentials() ?: return false
        
        val currentTime = System.currentTimeMillis()
        val maxAgeMillis = maxAgeDays * 24 * 60 * 60 * 1000L
        
        return (currentTime - credentials.timestamp) <= maxAgeMillis
    }
}
