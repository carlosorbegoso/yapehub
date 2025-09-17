package org.sysarp.project.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Función expect para decodificar Base64 a ImageBitmap de manera multiplataforma
 */
expect fun decodeBase64ToImageBitmap(base64String: String): ImageBitmap?

object ImageUtils {
    
    /**
     * Decodifica una imagen Base64 PNG a ImageBitmap usando la implementación multiplataforma
     */
    @Composable
    fun decodeBase64ToImageBitmap(base64String: String): ImageBitmap? {
        return remember(base64String) {
            try {
                org.sysarp.project.utils.decodeBase64ToImageBitmap(base64String)
            } catch (e: Exception) {
                println("❌ [IMAGE_UTILS] Error decodificando imagen Base64: ${e.message}")
                null
            }
        }
    }
}
