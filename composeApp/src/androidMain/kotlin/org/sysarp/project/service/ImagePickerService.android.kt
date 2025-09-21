package org.sysarp.project.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class ImagePickerService {
    
    private var context: Context? = null
    private var activity: ComponentActivity? = null
    private var galleryLauncher: ActivityResultLauncher<Intent>? = null
    private var cameraLauncher: ActivityResultLauncher<Intent>? = null
    private var currentPhotoUri: Uri? = null
    
    fun setContext(context: Context) {
        this.context = context
        if (context is ComponentActivity) {
            this.activity = context
            setupLaunchers()
        }
    }
    
    private fun setupLaunchers() {
        activity?.let { activity ->
            galleryLauncher = activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                // This will be handled in the suspend function
            }
            
            cameraLauncher = activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                // This will be handled in the suspend function
            }
        }
    }
    
    actual suspend fun selectImageFromGallery(): Result<ImageResult> = suspendCancellableCoroutine { continuation ->
        try {
            val activity = this.activity
            val context = this.context
            
            if (activity == null || context == null) {
                continuation.resumeWithException(Exception("Contexto o Activity no disponible"))
                return@suspendCancellableCoroutine
            }
            
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            
            // Set up a callback to handle the result
            galleryLauncher = activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        try {
                            val imageResult = processImageUri(uri, "comprobante_yape_${getCurrentTimestamp()}.jpg")
                            continuation.resume(Result.success(imageResult))
                        } catch (e: Exception) {
                            continuation.resume(Result.failure(e))
                        }
                    } ?: run {
                        continuation.resume(Result.failure(Exception("No se seleccionó ninguna imagen")))
                    }
                } else {
                    continuation.resume(Result.failure(Exception("Selección de imagen cancelada")))
                }
            }
            
            galleryLauncher?.launch(intent)
            
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }
    
    actual suspend fun capturePhotoFromCamera(): Result<ImageResult> = suspendCancellableCoroutine { continuation ->
        try {
            val activity = this.activity
            val context = this.context
            
            if (activity == null || context == null) {
                continuation.resumeWithException(Exception("Contexto o Activity no disponible"))
                return@suspendCancellableCoroutine
            }
            
            val photoFile = createImageFile()
            val photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            currentPhotoUri = photoUri
            
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            
            cameraLauncher = activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    try {
                        val imageResult = processImageUri(photoUri, "foto_yape_${getCurrentTimestamp()}.jpg")
                        continuation.resume(Result.success(imageResult))
                    } catch (e: Exception) {
                        continuation.resume(Result.failure(e))
                    }
                } else {
                    continuation.resume(Result.failure(Exception("Captura de foto cancelada")))
                }
            }
            
            cameraLauncher?.launch(intent)
            
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }
    
    private fun processImageUri(uri: Uri, fileName: String): ImageResult {
        val context = this.context ?: throw Exception("Contexto no disponible")
        
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        
        if (bitmap == null) {
            throw Exception("No se pudo decodificar la imagen")
        }
        
        // Convertir a base64
        val base64 = bitmapToBase64(bitmap)
        
        return ImageResult(
            base64 = base64,
            fileName = fileName,
            mimeType = "image/jpeg"
        )
    }
    
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64 = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
        return "data:image/jpeg;base64,$base64"
    }
    
    private fun createImageFile(): File {
        val context = this.context ?: throw Exception("Contexto no disponible")
        val timeStamp = getCurrentTimestamp()
        val storageDir = File(context.getExternalFilesDir(null), "Pictures")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }
    
    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return sdf.format(Date())
    }
}
