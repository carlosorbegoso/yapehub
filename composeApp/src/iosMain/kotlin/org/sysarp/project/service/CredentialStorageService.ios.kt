package org.sysarp.project.service

import kotlinx.serialization.json.Json
import org.sysarp.project.utils.Logger
import org.sysarp.project.utils.SecurityUtils
import platform.Foundation.NSUserDefaults

/**
 * Implementación iOS del servicio de almacenamiento de credenciales
 * Usa NSUserDefaults para almacenamiento (en producción usar Keychain)
 */
actual object CredentialStorageService {
    
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    actual suspend fun saveCredentials(email: String, password: String): Boolean {
        return try {
            val credentials = SavedCredentials(
                email = SecurityUtils.normalizeEmail(email),
                password = SecurityUtils.sanitizeInput(password),
                timestamp = System.currentTimeMillis()
            )
            
            val credentialsJson = json.encodeToString(SavedCredentials.serializer(), credentials)
            
            userDefaults.setObject(credentialsJson, "saved_credentials")
            userDefaults.setDouble(credentials.timestamp.toDouble(), "credentials_timestamp")
            userDefaults.synchronize()
            
            Logger.auth("CREDENTIAL_STORAGE", "✅ Credenciales guardadas para: ${credentials.email}")
            true
        } catch (e: Exception) {
            Logger.auth("CREDENTIAL_STORAGE", "❌ Error guardando credenciales: ${e.message}")
            false
        }
    }
    
    actual suspend fun getSavedCredentials(): SavedCredentials? {
        return try {
            val credentialsJson = userDefaults.stringForKey("saved_credentials")
            
            if (credentialsJson != null) {
                val credentials = json.decodeFromString(SavedCredentials.serializer(), credentialsJson)
                Logger.auth("CREDENTIAL_STORAGE", "🔍 Credenciales encontradas para: ${credentials.email}")
                credentials
            } else {
                Logger.auth("CREDENTIAL_STORAGE", "🔍 No hay credenciales guardadas")
                null
            }
        } catch (e: Exception) {
            Logger.auth("CREDENTIAL_STORAGE", "❌ Error recuperando credenciales: ${e.message}")
            null
        }
    }
    
    actual suspend fun clearCredentials(): Boolean {
        return try {
            userDefaults.removeObjectForKey("saved_credentials")
            userDefaults.removeObjectForKey("credentials_timestamp")
            userDefaults.synchronize()
            
            Logger.auth("CREDENTIAL_STORAGE", "🗑️ Credenciales eliminadas")
            true
        } catch (e: Exception) {
            Logger.auth("CREDENTIAL_STORAGE", "❌ Error eliminando credenciales: ${e.message}")
            false
        }
    }
    
    actual suspend fun hasSavedCredentials(): Boolean {
        return userDefaults.stringForKey("saved_credentials") != null
    }
    
    actual suspend fun areCredentialsValid(maxAgeDays: Int): Boolean {
        val credentials = getSavedCredentials() ?: return false
        
        val currentTime = System.currentTimeMillis()
        val maxAgeMillis = maxAgeDays * 24 * 60 * 60 * 1000L
        
        return (currentTime - credentials.timestamp) <= maxAgeMillis
    }
}
