package org.sysarp.project.service

actual class ImagePickerService {
    
    actual suspend fun selectImageFromGallery(): Result<ImageResult> {
        return try {
            
            // Por ahora, usar imagen mock hasta implementar la funcionalidad nativa de iOS
            Result.success(
                ImageResult(
                    base64 = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcUFhYaHSUfGhsjHBYWICwgIyYnKSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYaKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCdABmX/9k=",
                    fileName = "comprobante_yape_${getCurrentTimestamp()}.jpg",
                    mimeType = "image/jpeg"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    actual suspend fun capturePhotoFromCamera(): Result<ImageResult> {
        return try {
            
            // Por ahora, usar imagen mock hasta implementar la funcionalidad nativa de iOS
            Result.success(
                ImageResult(
                    base64 = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcUFhYaHSUfGhsjHBYWICwgIyYnKSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYaKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCdABmX/9k=",
                    fileName = "foto_yape_${getCurrentTimestamp()}.jpg",
                    mimeType = "image/jpeg"
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getCurrentTimestamp(): String {
        // Usar timestamp simple para iOS
        return "ios_${kotlin.random.Random.nextLong(100000, 999999)}"
    }
}
