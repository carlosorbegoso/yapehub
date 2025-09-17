package org.sysarp.project.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.io.ByteArray
import kotlinx.io.core.toByteArray
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data

/**
 * Implementación iOS para decodificar Base64 a ImageBitmap
 */
actual fun decodeBase64ToImageBitmap(base64String: String): ImageBitmap? {
    return try {
        // Decodificar Base64 a bytes
        val imageBytes = base64String.decodeBase64Bytes()
        
        // Crear Data desde los bytes
        val data = Data.makeFromBytes(imageBytes)
        
        // Crear Codec desde los datos
        val codec = Codec.makeFromData(data)
        
        // Crear Bitmap desde el codec
        val bitmap = Bitmap()
        bitmap.allocPixels(codec.imageInfo)
        codec.getPixels(bitmap.pixels, bitmap.rowBytes)
        
        // Convertir a ImageBitmap de Compose
        bitmap.toComposeImageBitmap()
        
    } catch (e: Exception) {
        println("❌ [IMAGE_UTILS_IOS] Error decodificando imagen Base64: ${e.message}")
        null
    }
}

/**
 * Función helper para decodificar Base64 en iOS
 */
private fun String.decodeBase64Bytes(): ByteArray {
    return try {
        // Implementación simple de decodificación Base64 para iOS
        val cleanString = this.replace("\\s".toRegex(), "")
        val padding = when (cleanString.length % 4) {
            2 -> "=="
            3 -> "="
            else -> ""
        }
        val paddedString = cleanString + padding
        
        // Convertir Base64 a bytes usando una implementación simple
        val result = ByteArray(paddedString.length * 3 / 4)
        var index = 0
        var i = 0
        
        while (i < paddedString.length) {
            val c1 = paddedString[i++].toInt()
            val c2 = paddedString[i++].toInt()
            val c3 = paddedString[i++].toInt()
            val c4 = paddedString[i++].toInt()
            
            val b1 = decodeBase64Char(c1)
            val b2 = decodeBase64Char(c2)
            val b3 = decodeBase64Char(c3)
            val b4 = decodeBase64Char(c4)
            
            result[index++] = ((b1 shl 2) or (b2 shr 4)).toByte()
            if (c3 != '='.toInt()) {
                result[index++] = ((b2 shl 4) or (b3 shr 2)).toByte()
            }
            if (c4 != '='.toInt()) {
                result[index++] = ((b3 shl 6) or b4).toByte()
            }
        }
        
        result.copyOf(index)
    } catch (e: Exception) {
        println("❌ [BASE64_DECODE] Error decodificando Base64: ${e.message}")
        ByteArray(0)
    }
}

private fun decodeBase64Char(c: Int): Int {
    return when (c) {
        in 'A'.toInt()..'Z'.toInt() -> c - 'A'.toInt()
        in 'a'.toInt()..'z'.toInt() -> c - 'a'.toInt() + 26
        in '0'.toInt()..'9'.toInt() -> c - '0'.toInt() + 52
        '+'.toInt() -> 62
        '/'.toInt() -> 63
        '='.toInt() -> 0
        else -> throw IllegalArgumentException("Invalid Base64 character: $c")
    }
}
