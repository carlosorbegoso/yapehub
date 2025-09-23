package org.sysarp.project.service

import kotlinx.serialization.Serializable

@Serializable
data class SavedCredentials(
    val email: String,
    val password: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Servicio para el almacenamiento seguro de credenciales
 * Maneja el guardado y recuperación de credenciales de login
 */
expect object CredentialStorageService {
    
    /**
     * Guarda las credenciales de forma segura
     * @param email Email del usuario
     * @param password Contraseña del usuario
     * @return true si se guardó exitosamente, false en caso contrario
     */
    suspend fun saveCredentials(email: String, password: String): Boolean
    
    /**
     * Recupera las credenciales guardadas
     * @return SavedCredentials si existen, null en caso contrario
     */
    suspend fun getSavedCredentials(): SavedCredentials?
    
    /**
     * Elimina las credenciales guardadas
     * @return true si se eliminaron exitosamente, false en caso contrario
     */
    suspend fun clearCredentials(): Boolean
    
    /**
     * Verifica si hay credenciales guardadas
     * @return true si hay credenciales guardadas, false en caso contrario
     */
    suspend fun hasSavedCredentials(): Boolean
    
    /**
     * Verifica si las credenciales guardadas siguen siendo válidas
     * @param maxAgeDays Edad máxima en días para considerar válidas las credenciales
     * @return true si las credenciales son válidas, false en caso contrario
     */
    suspend fun areCredentialsValid(maxAgeDays: Int = 30): Boolean
}
