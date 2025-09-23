package org.sysarp.project.ui.common.screens

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
fun ImagePickerActions(
    state: PaymentDialogState,
    imagePickerService: ImagePickerService
) {
    // Manejar acciones del selector de imagen
    // La lógica se maneja en PaymentDialogComponents
}