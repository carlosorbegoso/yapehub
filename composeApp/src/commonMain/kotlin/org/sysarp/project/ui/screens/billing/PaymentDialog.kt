package org.sysarp.project.ui.screens.billing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import org.sysarp.project.data.PaymentCode
import org.sysarp.project.service.ImagePickerService
import org.sysarp.project.service.billing.BillingService

/**
 * Diálogo de pago refactorizado
 * Usa componentes modulares para mejor mantenibilidad
 */
@Composable
fun PaymentDialog(
    paymentCode: PaymentCode,
    billingService: BillingService,
    onDismiss: () -> Unit,
    onPaymentCompleted: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val imagePickerService = remember { ImagePickerService() }
    
    // Crear el estado del diálogo
    val state = remember {
        PaymentDialogState(
            paymentCode = paymentCode,
            billingService = billingService,
            imagePickerService = imagePickerService,
            coroutineScope = coroutineScope
        )
    }
    
    PaymentDialogComponents(
        currentStep = state.currentStep,
        paymentCode = paymentCode,
        selectedImageBase64 = state.selectedImageBase64,
        selectedImageName = state.selectedImageName,
        notes = state.notes,
        isLoading = state.isLoading,
        error = state.error,
        isCheckingStatus = state.isCheckingStatus,
        paymentStatus = state.paymentStatus,
        showImagePicker = state.showImagePicker,
        imagePickerService = imagePickerService,
        onImageSelected = { base64, name -> state.updateSelectedImage(base64, name) },
        onShowImagePicker = { state.showImagePicker() },
        onDismissImagePicker = { state.hideImagePicker() },
        onNotesChange = { notes -> state.updateNotes(notes) },
        onUploadProof = { 
            state.uploadPaymentProof(
                onSuccess = { },
                onFailure = { error -> state.updateError(error) }
            )
        },
        onDismiss = onDismiss
    )
    
    // Manejar acciones del diálogo
    PaymentDialogActions(
        state = state,
        paymentCode = paymentCode,
        billingService = billingService,
        imagePickerService = imagePickerService,
        onPaymentCompleted = onPaymentCompleted
    )
    
    // Manejar selector de imagen
    ImagePickerActions(
        state = state,
        imagePickerService = imagePickerService
    )
}
