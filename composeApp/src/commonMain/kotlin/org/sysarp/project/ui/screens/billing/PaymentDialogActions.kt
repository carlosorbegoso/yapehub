package org.sysarp.project.ui.screens.billing

import androidx.compose.runtime.Composable
import org.sysarp.project.data.PaymentCode
import org.sysarp.project.service.ImagePickerService
import org.sysarp.project.service.billing.BillingService

/**
 * Acciones y handlers para PaymentDialog
 */

@Composable
fun PaymentDialogActions(
    state: PaymentDialogState,
    paymentCode: PaymentCode,
    billingService: BillingService,
    imagePickerService: ImagePickerService,
    onPaymentCompleted: () -> Unit
) {
    // Manejar la lógica de acciones del diálogo
    // Las acciones específicas se manejan en los componentes individuales
}

@Composable
fun PaymentDialogContent(
    state: PaymentDialogState,
    paymentCode: PaymentCode,
    billingService: BillingService,
    imagePickerService: ImagePickerService,
    onPaymentCompleted: () -> Unit,
    onDismiss: () -> Unit
) {
    when (state.currentStep) {
        PaymentStep.PAYMENT_INFO -> {
            PaymentInfoStep(
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
                onImageSelected = { base64, name ->
                    state.updateSelectedImage(base64, name)
                },
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
        }
        
        PaymentStep.UPLOAD_SUCCESS -> {
            UploadSuccessStep(
                onDismiss = onDismiss,
                onCheckStatus = {
                    state.checkPaymentStatus(
                        onSuccess = { status ->
                            if (status.status == "approved") {
                                onPaymentCompleted()
                            }
                        },
                        onFailure = { error -> state.updateError(error) }
                    )
                },
                isCheckingStatus = state.isCheckingStatus
            )
        }
        
        PaymentStep.SUCCESS -> {
            PaymentSuccessStep(
                paymentCode = paymentCode,
                onDismiss = onDismiss
            )
        }
        
        PaymentStep.ERROR -> {
            PaymentErrorStep(
                error = state.error,
                onDismiss = onDismiss,
                onRetry = { state.resetToInitialStep() }
            )
        }
    }
}

@Composable
fun ImagePickerActions(
    state: PaymentDialogState,
    imagePickerService: ImagePickerService
) {
    // Manejar acciones del selector de imagen
    if (state.showImagePicker) {
        ImagePickerDialog(
            imagePickerService = imagePickerService,
            onImageSelected = { base64, name ->
                state.updateSelectedImage(base64, name)
            },
            onDismiss = { state.hideImagePicker() }
        )
    }
}
