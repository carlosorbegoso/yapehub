package org.sysarp.project.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import org.jetbrains.skia.Image

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
        codec.readPixels(bitmap) // CORREGIDO: usar readPixels en lugar de getPixels

        val image = Image.makeFromBitmap(bitmap)
        image.toComposeImageBitmap()

        
    } catch (e: Exception) {
        // Error decodificando imagen Base64
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
            val c1 = paddedString[i++].code
            val c2 = paddedString[i++].code
            val c3 = paddedString[i++].code
            val c4 = paddedString[i++].code
            
            val b1 = decodeBase64Char(c1)
            val b2 = decodeBase64Char(c2)
            val b3 = decodeBase64Char(c3)
            val b4 = decodeBase64Char(c4)
            
            result[index++] = ((b1 shl 2) or (b2 shr 4)).toByte()
            if (c3 != '='.code) {
                result[index++] = ((b2 shl 4) or (b3 shr 2)).toByte()
            }
            if (c4 != '='.code) {
                result[index++] = ((b3 shl 6) or b4).toByte()
            }
        }
        
        result.copyOf(index)
    } catch (e: Exception) {
        // Error decodificando Base64
        ByteArray(0)
    }
}

private fun decodeBase64Char(c: Int): Int {
    return when (c) {
        in 'A'.code..'Z'.code -> c - 'A'.code
        in 'a'.code..'z'.code -> c - 'a'.code + 26
        in '0'.code..'9'.code -> c - '0'.code + 52
        '+'.code -> 62
        '/'.code -> 63
        '='.code -> 0
        else -> throw IllegalArgumentException("Invalid Base64 character: $c")
    }
}

