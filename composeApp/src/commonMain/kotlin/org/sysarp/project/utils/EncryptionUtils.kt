package org.sysarp.project.utils

import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sysarp.project.data.YapeNotification
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

object EncryptionUtils {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    // Clave de encriptación (en producción debería estar en variables de entorno)
    private const val ENCRYPTION_KEY = "YapeChamo2024SecretKey"
    
    /**
     * Encripta una notificación de Yape
     */
    fun encryptYapeNotification(notification: YapeNotification): String {
        try {
            // Convertir la notificación a JSON
            val jsonString = json.encodeToString(notification)
            
            // Generar clave de encriptación
            val key = generateKey()
            
            // Encriptar el JSON
            val encryptedData = encrypt(jsonString, key)
            
            // Crear el payload encriptado con metadata
            val encryptedPayload = mapOf(
                "data" to encryptedData,
                "key" to key,
                "timestamp" to Clock.System.now().toEpochMilliseconds(),
                "checksum" to generateChecksum(jsonString)
            )
            
            return json.encodeToString(encryptedPayload)
        } catch (e: Exception) {
            throw Exception("Error al encriptar notificación: ${e.message}")
        }
    }
    
    /**
     * Genera una clave de encriptación única
     */
    private fun generateKey(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..16).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }
    
    /**
     * Encripta texto usando AES
     */
    private fun encrypt(text: String, key: String): String {
        val secretKey = SecretKeySpec(key.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(text.toByteArray())
        return android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.DEFAULT)
    }
    
    /**
     * Genera un checksum para verificar integridad
     */
    private fun generateChecksum(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Genera un fingerprint único del dispositivo
     */
    fun generateDeviceFingerprint(): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val random = Random.nextLong()
        val nanos = System.nanoTime()
        val deviceInfo = "device_${timestamp}_${random}_${nanos}_1.0.0"
        return generateChecksum(deviceInfo).take(16)
    }
    
    // Generar fingerprint único para cada notificación
    fun generateUniqueNotificationFingerprint(): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val random = Random.nextLong()
        val nanos = System.nanoTime()
        val threadHash = Thread.currentThread().name.hashCode()
        val objectHash = hashCode()
        val uniqueInfo = "notif_${timestamp}_${random}_${nanos}_${threadHash}_${objectHash}"
        return generateChecksum(uniqueInfo).take(16)
    }

    // Encriptar mensaje con clave específica
    fun encryptWithKey(message: String, key: String): String {
        return xorEncrypt(message, key)
    }

    // Desencriptar mensaje con clave específica
    fun decryptWithKey(encryptedMessage: String, key: String): String {
        val decoded = simpleBase64Decode(encryptedMessage)
        return xorDecrypt(decoded, key)
    }

    // Encriptación XOR simple
    private fun xorEncrypt(text: String, key: String): String {
        val result = StringBuilder()
        val keyBytes = key.toByteArray()
        
        for (i in text.indices) {
            val textByte = text[i].code
            val keyByte = keyBytes[i % keyBytes.size].toInt()
            val encryptedByte = textByte xor keyByte
            result.append(encryptedByte.toChar())
        }
        
        return simpleBase64Encode(result.toString())
    }
    
    // Desencriptación XOR simple
    private fun xorDecrypt(encryptedText: String, key: String): String {
        val result = StringBuilder()
        val keyBytes = key.toByteArray()
        
        for (i in encryptedText.indices) {
            val encryptedByte = encryptedText[i].code
            val keyByte = keyBytes[i % keyBytes.size].toInt()
            val decryptedByte = encryptedByte xor keyByte
            result.append(decryptedByte.toChar())
        }
        
        return result.toString()
    }
    
    // Implementación simple de Base64 para multiplatform
    private fun simpleBase64Encode(input: String): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        val bytes = input.toByteArray()
        val result = StringBuilder()
        
        for (i in bytes.indices step 3) {
            val b1 = bytes[i].toInt() and 0xFF
            val b2 = if (i + 1 < bytes.size) bytes[i + 1].toInt() and 0xFF else 0
            val b3 = if (i + 2 < bytes.size) bytes[i + 2].toInt() and 0xFF else 0
            
            val combined = (b1 shl 16) or (b2 shl 8) or b3
            
            result.append(chars[(combined shr 18) and 63])
            result.append(chars[(combined shr 12) and 63])
            result.append(if (i + 1 < bytes.size) chars[(combined shr 6) and 63] else '=')
            result.append(if (i + 2 < bytes.size) chars[combined and 63] else '=')
        }
        
        return result.toString()
    }
    
    // Implementación simple de decodificación Base64 para multiplatform
    private fun simpleBase64Decode(input: String): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        val result = StringBuilder()
        
        // Remover padding
        val cleanInput = input.replace("=", "")
        
        for (i in cleanInput.indices step 4) {
            val c1 = chars.indexOf(cleanInput[i])
            val c2 = if (i + 1 < cleanInput.length) chars.indexOf(cleanInput[i + 1]) else 0
            val c3 = if (i + 2 < cleanInput.length) chars.indexOf(cleanInput[i + 2]) else 0
            val c4 = if (i + 3 < cleanInput.length) chars.indexOf(cleanInput[i + 3]) else 0
            
            if (c1 == -1 || c2 == -1 || c3 == -1 || c4 == -1) continue
            
            val combined = (c1 shl 18) or (c2 shl 12) or (c3 shl 6) or c4
            
            result.append(((combined shr 16) and 0xFF).toChar())
            if (i + 2 < cleanInput.length) {
                result.append(((combined shr 8) and 0xFF).toChar())
            }
            if (i + 3 < cleanInput.length) {
                result.append((combined and 0xFF).toChar())
            }
        }
        
        return result.toString()
    }
}
