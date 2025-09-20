package org.sysarp.project.service

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.json.Json
import org.sysarp.project.utils.Logger
import org.sysarp.project.utils.SecurityUtils

/**
 * Implementación Android del servicio de almacenamiento de credenciales
 * Usa EncryptedSharedPreferences para almacenamiento seguro
 */
actual object CredentialStorageService {
    
    private val context: Context? = org.sysarp.project.ContextProvider.getContext()
    private val masterKey: MasterKey? = context?.let { 
        MasterKey.Builder(it)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    private val sharedPreferences: SharedPreferences? = context?.let { ctx ->
        masterKey?.let { key ->
            EncryptedSharedPreferences.create(
                ctx,
                "secure_credentials",
                key,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    actual suspend fun saveCredentials(email: String, password: String): Boolean {
        return try {
            val prefs = sharedPreferences ?: run {
                Logger.auth("CREDENTIAL_STORAGE", "❌ Contexto no disponible para guardar credenciales")
                return false
            }
            
            val credentials = SavedCredentials(
                email = SecurityUtils.normalizeEmail(email),
                password = SecurityUtils.sanitizeInput(password),
                timestamp = System.currentTimeMillis()
            )
            
            val credentialsJson = json.encodeToString(SavedCredentials.serializer(), credentials)
            
            prefs.edit()
                .putString("saved_credentials", credentialsJson)
                .putLong("credentials_timestamp", credentials.timestamp)
                .apply()
            
            Logger.auth("CREDENTIAL_STORAGE", "✅ Credenciales guardadas para: ${credentials.email}")
            true
        } catch (e: Exception) {
            Logger.auth("CREDENTIAL_STORAGE", "❌ Error guardando credenciales: ${e.message}")
            false
        }
    }
    
    actual suspend fun getSavedCredentials(): SavedCredentials? {
        return try {
            val prefs = sharedPreferences ?: run {
                Logger.auth("CREDENTIAL_STORAGE", "❌ Contexto no disponible para recuperar credenciales")
                return null
            }
            
            val credentialsJson = prefs.getString("saved_credentials", null)
            
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
            val prefs = sharedPreferences ?: run {
                Logger.auth("CREDENTIAL_STORAGE", "❌ Contexto no disponible para eliminar credenciales")
                return false
            }
            
            prefs.edit()
                .remove("saved_credentials")
                .remove("credentials_timestamp")
                .apply()
            
            Logger.auth("CREDENTIAL_STORAGE", "🗑️ Credenciales eliminadas")
            true
        } catch (e: Exception) {
            Logger.auth("CREDENTIAL_STORAGE", "❌ Error eliminando credenciales: ${e.message}")
            false
        }
    }
    
    actual suspend fun hasSavedCredentials(): Boolean {
        return sharedPreferences?.contains("saved_credentials") ?: false
    }
    
    actual suspend fun areCredentialsValid(maxAgeDays: Int): Boolean {
        val credentials = getSavedCredentials() ?: return false
        
        val currentTime = System.currentTimeMillis()
        val maxAgeMillis = maxAgeDays * 24 * 60 * 60 * 1000L
        
        return (currentTime - credentials.timestamp) <= maxAgeMillis
    }
}
