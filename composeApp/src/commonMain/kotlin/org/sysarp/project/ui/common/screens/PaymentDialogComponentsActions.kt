package org.sysarp.project.ui.common.screens

import androidx.compose.runtime.Composable
import org.sysarp.project.service.ImagePickerService

/**
 * Acciones y handlers para PaymentDialogComponents
 */

@Composable
fun PaymentDialogComponentsActions(
    state: PaymentDialogComponentsState,
    imagePickerService: ImagePickerService,
    onImageSelected: (String, String) -> Unit,
    onShowImagePicker: () -> Unit,
    onDismissImagePicker: () -> Unit,
    onNotesChange: (String) -> Unit,
    onUploadProof: () -> Unit,
    onDismiss: () -> Unit
) {
    // Manejar la lógica de acciones de los componentes
    // Las acciones específicas se manejan en los componentes individuales
}

@Composable
fun PaymentDialogComponentsContent(
    state: PaymentDialogComponentsState,
    imagePickerService: ImagePickerService,
    onImageSelected: (String, String) -> Unit,
    onShowImagePicker: () -> Unit,
    onDismissImagePicker: () -> Unit,
    onNotesChange: (String) -> Unit,
    onUploadProof: () -> Unit,
    onDismiss: () -> Unit
) {
    PaymentDialogContainer(
        state = state,
        onDismiss = onDismiss
    ) {
        PaymentInfoStep(
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
}
