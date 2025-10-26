package org.sysarp.project.ui.common.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sysarp.project.data.PaymentCode
import org.sysarp.project.data.PaymentStatus
import org.sysarp.project.service.ImagePickerService
import org.sysarp.project.service.billing.BillingService

/**
 * Estado y lógica de negocio para PaymentDialog
 */
class PaymentDialogState(
    private val paymentCode: PaymentCode,
    private val billingService: BillingService,
    private val imagePickerService: ImagePickerService,
    private val coroutineScope: CoroutineScope
) {
    // Estados de pasos del diálogo
    var currentStep by mutableStateOf(PaymentStep.PAYMENT_INFO)
        private set
    
    // Estados de imagen
    var selectedImageBase64 by mutableStateOf("")
        private set
    
    var selectedImageName by mutableStateOf("")
        private set
    
    // Estados de notas
    var notes by mutableStateOf("")
        private set
    
    // Estados de carga
    var isLoading by mutableStateOf(false)
        private set
    
    var isCheckingStatus by mutableStateOf(false)
        private set
    
    // Estados de error
    var error by mutableStateOf("")
        private set
    
    // Estados de diálogos
    var showImagePicker by mutableStateOf(false)
        private set
    
    // Estados de pago
    var paymentStatus by mutableStateOf<PaymentStatus?>(null)
        private set
    
    /**
     * Cambia al siguiente paso
     */
    fun changeCurrentStep(step: PaymentStep) {
        currentStep = step
    }
    
    /**
     * Establece la imagen seleccionada
     */
    fun updateSelectedImage(base64: String, name: String) {
        selectedImageBase64 = base64
        selectedImageName = name
    }
    
    /**
     * Limpia la imagen seleccionada
     */
    fun clearSelectedImage() {
        selectedImageBase64 = ""
        selectedImageName = ""
    }
    
    /**
     * Establece las notas
     */
    fun updateNotes(notes: String) {
        this.notes = notes
    }
    
    /**
     * Establece el estado de carga
     */
    fun updateLoading(loading: Boolean) {
        isLoading = loading
    }
    
    /**
     * Establece el estado de verificación
     */
    fun updateCheckingStatus(checking: Boolean) {
        isCheckingStatus = checking
    }
    
    /**
     * Establece el error
     */
    fun updateError(error: String) {
        this.error = error
    }
    
    /**
     * Limpia el error
     */
    fun clearError() {
        error = ""
    }
    
    /**
     * Muestra el selector de imagen
     */
    fun showImagePicker() {
        showImagePicker = true
    }
    
    /**
     * Oculta el selector de imagen
     */
    fun hideImagePicker() {
        showImagePicker = false
    }
    
    /**
     * Establece el estado del pago
     */
    fun updatePaymentStatus(status: PaymentStatus?) {
        paymentStatus = status
    }
    
    /**
     * Sube el comprobante de pago
     */
    fun uploadPaymentProof(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        coroutineScope.launch {
            try {
                isLoading = true
                error = ""
                
                val uploadResult = billingService.uploadPaymentProof(
                    paymentCode.paymentCode,
                    selectedImageBase64,
                    notes.takeIf { it.isNotEmpty() }
                )
                
                uploadResult.fold(
                    onSuccess = {
                        updateLoading(false)
                        changeCurrentStep(PaymentStep.UPLOAD_SUCCESS)
                        onSuccess()
                    },
                    onFailure = { e ->
                        updateLoading(false)
                        updateError(e.message ?: "Error subiendo comprobante")
                        onFailure(error)
                    }
                )
            } catch (e: Exception) {
                updateLoading(false)
                updateError(e.message ?: "Error inesperado")
                onFailure(error)
            }
        }
    }
    
    /**
     * Verifica el estado del pago
     */
    fun checkPaymentStatus(
        onSuccess: (PaymentStatus) -> Unit,
        onFailure: (String) -> Unit
    ) {
        coroutineScope.launch {
            isCheckingStatus = true
            try {
                val statusResult = billingService.checkPaymentStatus(paymentCode.paymentCode)
                statusResult.fold(
                    onSuccess = { status ->
                        updateCheckingStatus(false)
                        updatePaymentStatus(status)
                        onSuccess(status)
                        
                        when (status.status) {
                            "approved" -> {
                                changeCurrentStep(PaymentStep.SUCCESS)
                            }
                            "rejected" -> {
                                changeCurrentStep(PaymentStep.ERROR)
                                updateError(status.message)
                            }
                        }
                    },
                    onFailure = { e ->
                        updateCheckingStatus(false)
                        updateError(e.message ?: "Error verificando estado")
                        onFailure(error)
                    }
                )
            } catch (e: Exception) {
                updateCheckingStatus(false)
                updateError(e.message ?: "Error inesperado")
                onFailure(error)
            }
        }
    }
    
    /**
     * Selecciona imagen desde la galería
     */
    fun selectImageFromGallery(
        onSuccess: (String, String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        coroutineScope.launch {
            try {
                val result = imagePickerService.selectImageFromGallery()
                result.fold(
                    onSuccess = { imageResult ->
                        updateSelectedImage(imageResult.base64, imageResult.fileName)
                        hideImagePicker()
                        onSuccess(imageResult.base64, imageResult.fileName)
                    },
                    onFailure = { e ->
                        onFailure(e.message ?: "Error seleccionando imagen")
                    }
                )
            } catch (e: Exception) {
                onFailure(e.message ?: "Error inesperado")
            }
        }
    }
    
    /**
     * Captura foto desde la cámara
     */
    fun capturePhotoFromCamera(
        onSuccess: (String, String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        coroutineScope.launch {
            try {
                val result = imagePickerService.capturePhotoFromCamera()
                result.fold(
                    onSuccess = { imageResult ->
                        updateSelectedImage(imageResult.base64, imageResult.fileName)
                        hideImagePicker()
                        onSuccess(imageResult.base64, imageResult.fileName)
                    },
                    onFailure = { e ->
                        onFailure(e.message ?: "Error capturando foto")
                    }
                )
            } catch (e: Exception) {
                onFailure(e.message ?: "Error inesperado")
            }
        }
    }
    
    /**
     * Reinicia el diálogo al paso inicial
     */
    fun resetToInitialStep() {
        changeCurrentStep(PaymentStep.PAYMENT_INFO)
        clearSelectedImage()
        updateNotes("")
        clearError()
        updatePaymentStatus(null)
        hideImagePicker()
    }
    
    /**
     * Verifica si se puede subir el comprobante
     */
    fun canUploadProof(): Boolean {
        return selectedImageBase64.isNotEmpty() && !isLoading
    }
    
    /**
     * Verifica si se puede verificar el estado
     */
    fun canCheckStatus(): Boolean {
        return !isCheckingStatus
    }
}

/**
 * Pasos del diálogo de pago
 */
enum class PaymentStep {
    PAYMENT_INFO,
    UPLOAD_SUCCESS,
    SUCCESS,
    ERROR
}
