package org.sysarp.project.ui.common.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import org.sysarp.project.data.PaymentCode
import org.sysarp.project.data.PaymentStatus
import org.sysarp.project.service.ImagePickerService

/**
 * Componentes UI para PaymentDialog refactorizados
 * Usa componentes modulares para mejor mantenibilidad
 */

@Composable
fun PaymentDialogComponents(
    currentStep: PaymentStep,
    paymentCode: PaymentCode,
    selectedImageBase64: String,
    selectedImageName: String,
    notes: String,
    isLoading: Boolean,
    error: String,
    isCheckingStatus: Boolean,
    paymentStatus: PaymentStatus?,
    showImagePicker: Boolean,
    imagePickerService: ImagePickerService,
    onImageSelected: (String, String) -> Unit,
    onShowImagePicker: () -> Unit,
    onDismissImagePicker: () -> Unit,
    onNotesChange: (String) -> Unit,
    onUploadProof: () -> Unit,
    onDismiss: () -> Unit
) {
    // Crear el estado de los componentes
    val state = remember {
        PaymentDialogComponentsState()
    }
    
    // Sincronizar el estado externo con el estado interno
    LaunchedEffect(
        currentStep,
        paymentCode,
        selectedImageBase64,
        selectedImageName,
        notes,
        isLoading,
        error,
        isCheckingStatus,
        paymentStatus,
        showImagePicker
    ) {
        state.updateCurrentStep(currentStep)
        state.updatePaymentCode(paymentCode)
        state.updateSelectedImage(selectedImageBase64, selectedImageName)
        state.updateNotes(notes)
        state.updateLoading(isLoading)
        state.updateError(error)
        state.updateCheckingStatus(isCheckingStatus)
        state.updatePaymentStatus(paymentStatus)
        state.updateShowImagePicker(showImagePicker)
    }
    
    // Renderizar el contenido de los componentes
    PaymentDialogComponentsContent(
        state = state,
        imagePickerService = imagePickerService,
        onImageSelected = onImageSelected,
        onShowImagePicker = onShowImagePicker,
        onDismissImagePicker = onDismissImagePicker,
        onNotesChange = onNotesChange,
        onUploadProof = onUploadProof,
        onDismiss = onDismiss
    )
    
    // Manejar acciones de los componentes
    PaymentDialogComponentsActions(
        state = state,
        imagePickerService = imagePickerService,
        onImageSelected = onImageSelected,
        onShowImagePicker = onShowImagePicker,
        onDismissImagePicker = onDismissImagePicker,
        onNotesChange = onNotesChange,
        onUploadProof = onUploadProof,
        onDismiss = onDismiss
    )
}
