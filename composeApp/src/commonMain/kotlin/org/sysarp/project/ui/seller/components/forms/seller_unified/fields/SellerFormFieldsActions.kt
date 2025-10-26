package org.sysarp.project.ui.components.seller_unified.fields

import androidx.compose.runtime.Composable

/**
 * Acciones y handlers para SellerFormFields
 */

@Composable
fun SellerFormFieldsActions(
    state: SellerFormFieldsState,
    onNavigateToQRScanner: () -> Unit,
    onSubmit: () -> Unit
) {
    // Manejar la lógica de acciones del formulario
    // Las acciones específicas se manejan en los componentes individuales
}

@Composable
fun SellerFormFieldsContent(
    state: SellerFormFieldsState,
    onNavigateToQRScanner: () -> Unit,
    onSubmit: () -> Unit
) {
    SellerFormFieldsContainer(
        state = state,
        onNavigateToQRScanner = onNavigateToQRScanner,
        onSubmit = onSubmit
    )
}
