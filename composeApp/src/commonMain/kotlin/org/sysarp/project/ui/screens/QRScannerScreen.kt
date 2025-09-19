package org.sysarp.project.ui.screens

import androidx.compose.runtime.Composable
import org.sysarp.project.service.auth.AuthService

@Composable
fun QRScannerScreen(
    onNavigateBack: () -> Unit,
    onQRScanned: (String) -> Unit,
    onManualCodeEntry: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onLoginSuccess: () -> Unit = {},
    authService: AuthService? = null
) {
    // Usar SellerQRLoginScreen para la funcionalidad completa de login por QR
    SellerQRLoginScreen(
        onNavigateBack = onNavigateBack,
        onQRScanned = onQRScanned,
        onLoginSuccess = { sellerData ->
            // Manejar el login exitoso
            onLoginSuccess()
        },
        isLoading = isLoading,
        errorMessage = errorMessage,
        authService = authService
    )
}