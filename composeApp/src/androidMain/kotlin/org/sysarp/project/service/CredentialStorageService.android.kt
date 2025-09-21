package org.sysarp.project.service

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.json.Json
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
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    actual suspend fun getSavedCredentials(): SavedCredentials? {
        return try {
            val prefs = sharedPreferences ?: run {
                return null
            }
            
            val credentialsJson = prefs.getString("saved_credentials", null)
            
            if (credentialsJson != null) {
                val credentials = json.decodeFromString(SavedCredentials.serializer(), credentialsJson)
                credentials
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    actual suspend fun clearCredentials(): Boolean {
        return try {
            val prefs = sharedPreferences ?: run {
                return false
            }
            
            prefs.edit()
                .remove("saved_credentials")
                .remove("credentials_timestamp")
                .apply()
            
            true
        } catch (e: Exception) {
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
