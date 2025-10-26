package org.sysarp.project.ui.common.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.sysarp.project.data.PaymentCode
import org.sysarp.project.data.PaymentStatus

/**
 * Estado y lógica de negocio para PaymentDialogComponents
 */
class PaymentDialogComponentsState {
    // Estados de datos
    var paymentCode: PaymentCode? = null
        private set
    
    var paymentStatus: PaymentStatus? = null
        private set
    
    // Estados de UI
    var selectedImageBase64 by mutableStateOf("")
        private set
    
    var selectedImageName by mutableStateOf("")
        private set
    
    var notes by mutableStateOf("")
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var isCheckingStatus by mutableStateOf(false)
        private set
    
    var showImagePicker by mutableStateOf(false)
        private set
    
    var error by mutableStateOf("")
        private set
    
    var currentStep by mutableStateOf(PaymentStep.PAYMENT_INFO)
        private set
    
    /**
     * Establece el código de pago
     */
    fun updatePaymentCode(paymentCode: PaymentCode?) {
        this.paymentCode = paymentCode
    }
    
    /**
     * Establece el estado del pago
     */
    fun updatePaymentStatus(status: PaymentStatus?) {
        this.paymentStatus = status
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
     * Establece si mostrar el selector de imagen
     */
    fun updateShowImagePicker(show: Boolean) {
        showImagePicker = show
    }
    
    /**
     * Establece el mensaje de error
     */
    fun updateError(error: String) {
        this.error = error
    }
    
    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        error = ""
    }
    
    /**
     * Establece el paso actual
     */
    fun updateCurrentStep(step: PaymentStep) {
        currentStep = step
    }
    
    /**
     * Verifica si hay una imagen seleccionada
     */
    fun hasSelectedImage(): Boolean {
        return selectedImageBase64.isNotEmpty()
    }
    
    /**
     * Verifica si hay notas
     */
    fun hasNotes(): Boolean {
        return notes.isNotEmpty()
    }
    
    /**
     * Verifica si el formulario está completo
     */
    fun isFormComplete(): Boolean {
        return hasSelectedImage() && notes.isNotEmpty()
    }
    
    /**
     * Verifica si hay un error
     */
    fun hasError(): Boolean {
        return error.isNotEmpty()
    }
    
    /**
     * Verifica si está cargando
     */
    fun isCurrentlyLoading(): Boolean {
        return isLoading
    }
    
    /**
     * Verifica si está verificando el estado
     */
    fun isCurrentlyCheckingStatus(): Boolean {
        return isCheckingStatus
    }
    
    /**
     * Verifica si el selector de imagen está visible
     */
    fun isImagePickerVisible(): Boolean {
        return showImagePicker
    }
    
    /**
     * Obtiene el código de pago actual
     */
    fun getCurrentPaymentCode(): PaymentCode? {
        return paymentCode
    }
    
    /**
     * Obtiene el estado del pago actual
     */
    fun getCurrentPaymentStatus(): PaymentStatus? {
        return paymentStatus
    }
    
    /**
     * Obtiene el paso actual
     */
    fun getCurrentPaymentStep(): PaymentStep {
        return currentStep
    }
    
    /**
     * Obtiene el mensaje de error actual
     */
    fun getCurrentError(): String {
        return error
    }
    
    /**
     * Obtiene las notas actuales
     */
    fun getCurrentNotes(): String {
        return notes
    }
    
    /**
     * Obtiene la imagen seleccionada actual
     */
    fun getCurrentSelectedImage(): Pair<String, String> {
        return Pair(selectedImageBase64, selectedImageName)
    }
    
    /**
     * Resetea todos los estados
     */
    fun resetAll() {
        paymentCode = null
        paymentStatus = null
        clearSelectedImage()
        updateNotes("")
        updateLoading(false)
        updateCheckingStatus(false)
        updateShowImagePicker(false)
        clearError()
        updateCurrentStep(PaymentStep.PAYMENT_INFO)
    }
    
    /**
     * Resetea solo los estados de UI
     */
    fun resetUIStates() {
        clearSelectedImage()
        updateNotes("")
        updateLoading(false)
        updateCheckingStatus(false)
        updateShowImagePicker(false)
        clearError()
    }
    
    /**
     * Verifica si se puede cerrar el diálogo
     */
    fun canDismissDialog(): Boolean {
        return currentStep == PaymentStep.PAYMENT_INFO
    }
    
    /**
     * Verifica si se puede hacer clic fuera del diálogo
     */
    fun canDismissOnClickOutside(): Boolean {
        return false // Siempre false para este diálogo
    }
}
