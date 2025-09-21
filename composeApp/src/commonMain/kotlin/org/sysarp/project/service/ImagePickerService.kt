package org.sysarp.project.service
/**
 * Servicio para selección de imágenes y captura de fotos
 * Maneja la selección desde galería y captura desde cámara
 */
expect class ImagePickerService() {
    /**
     * Selecciona una imagen desde la galería
     * @return Result con la imagen en base64 y el nombre del archivo
     */
    suspend fun selectImageFromGallery(): Result<ImageResult>
    
    /**
     * Captura una foto usando la cámara
     * @return Result con la imagen en base64 y el nombre del archivo
     */
    suspend fun capturePhotoFromCamera(): Result<ImageResult>
}

/**
 * Resultado de la selección/captura de imagen
 */
data class ImageResult(
    val base64: String,
    val fileName: String,
    val mimeType: String = "image/jpeg"
)
