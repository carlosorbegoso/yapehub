package org.sysarp.project.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sysarp.project.utils.Logger
import java.io.ByteArrayOutputStream
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
    
    private suspend fun convertUriToBase64(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = context?.contentResolver?.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()
            android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            Logger.auth("IMAGE_PICKER", "❌ Error convirtiendo imagen a base64: ${e.message}")
            ""
        }
    }
}

/**
 * Composable helper para usar el ImagePickerService en Android
 */
@Composable
fun rememberImagePickerService(): ImagePickerService {
    val context = LocalContext.current
    return remember {
        ImagePickerService().apply {
            setContext(context)
        }
    }
}
