package org.sysarp.project.ui.components.seller_unified.fields

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * Formulario de campos del vendedor refactorizado
 * Usa componentes modulares para mejor mantenibilidad
 */
@Composable
fun SellerFormFields(
    affiliationCode: String,
    sellerName: String,
    phone: String,
    isLoading: Boolean,
    errorMessage: String,
    successMessage: String,
    isFormValid: Boolean,
    onAffiliationCodeChange: (String) -> Unit,
    onSellerNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onNavigateToQRScanner: () -> Unit,
    onSubmit: () -> Unit
) {
    // Crear el estado del formulario
    val state = remember {
        SellerFormFieldsState()
    }
    
    // Sincronizar el estado externo con el estado interno
    LaunchedEffect(affiliationCode, sellerName, phone, isLoading, errorMessage, successMessage) {
        state.updateAffiliationCode(affiliationCode)
        state.updateSellerName(sellerName)
        state.updatePhone(phone)
        state.updateLoading(isLoading)
        state.updateErrorMessage(errorMessage)
        state.updateSuccessMessage(successMessage)
    }
    
    // Manejar cambios en el estado interno
    LaunchedEffect(state.affiliationCode) {
        onAffiliationCodeChange(state.affiliationCode)
    }
    
    LaunchedEffect(state.sellerName) {
        onSellerNameChange(state.sellerName)
    }
    
    LaunchedEffect(state.phone) {
        onPhoneChange(state.phone)
    }
    
    // Renderizar el contenido del formulario
    SellerFormFieldsContent(
        state = state,
        onNavigateToQRScanner = onNavigateToQRScanner,
        onSubmit = onSubmit
    )
    
    // Manejar acciones del formulario
    SellerFormFieldsActions(
        state = state,
        onNavigateToQRScanner = onNavigateToQRScanner,
        onSubmit = onSubmit
    )
}
