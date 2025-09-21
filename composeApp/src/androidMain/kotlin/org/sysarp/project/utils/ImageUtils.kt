package org.sysarp.project.utils

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Implementación Android para decodificar Base64 a ImageBitmap
 */
actual fun decodeBase64ToImageBitmap(base64String: String): ImageBitmap? {
    return try {
        // Decodificar Base64 a bytes
        val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
        
        // Crear Bitmap desde los bytes
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        
        // Convertir a ImageBitmap de Compose
        bitmap?.asImageBitmap()
        
    } catch (e: Exception) {
        // Error decodificando imagen Base64
        null
    }
}
