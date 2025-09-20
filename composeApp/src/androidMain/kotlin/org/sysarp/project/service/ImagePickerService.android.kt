package org.sysarp.project.service

import android.content.Context
import org.sysarp.project.utils.Logger
import java.text.SimpleDateFormat
import java.util.*

actual class ImagePickerService {
    
    private var context: Context? = null
    
    fun setContext(context: Context) {
        this.context = context
    }
    
    actual suspend fun selectImageFromGallery(): Result<ImageResult> {
        return try {
            Logger.auth("IMAGE_PICKER", "📸 Seleccionando imagen desde galería")
            
            // Por ahora, usar imagen mock hasta implementar el ActivityResult
            Result.success(
                ImageResult(
                    base64 = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcUFhYaHSUfGhsjHBYWICwgIyYnKSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYaKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCdABmX/9k=",
                    fileName = "comprobante_yape_${getCurrentTimestamp()}.jpg",
                    mimeType = "image/jpeg"
                )
            )
        } catch (e: Exception) {
            Logger.auth("IMAGE_PICKER", "❌ Error seleccionando imagen: ${e.message}")
            Result.failure(e)
        }
    }
    
    actual suspend fun capturePhotoFromCamera(): Result<ImageResult> {
        return try {
            Logger.auth("IMAGE_PICKER", "📷 Capturando foto desde cámara")
            
            // Por ahora, usar imagen mock hasta implementar el ActivityResult
            Result.success(
                ImageResult(
                    base64 = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcUFhYaHSUfGhsjHBYWICwgIyYnKSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYaKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCdABmX/9k=",
                    fileName = "foto_yape_${getCurrentTimestamp()}.jpg",
                    mimeType = "image/jpeg"
                )
            )
        } catch (e: Exception) {
            Logger.auth("IMAGE_PICKER", "❌ Error capturando foto: ${e.message}")
            Result.failure(e)
        }
    }
    
    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return sdf.format(Date())
    }

}

