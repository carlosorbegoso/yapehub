package org.sysarp.project.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.sysarp.project.repository.UserProfileRepository
import org.sysarp.project.service.SellerService
import org.sysarp.project.service.affiliation.AffiliationService
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.branch.BranchService
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.service.qr.QRService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.service.websocket.PaymentWebSocketService
import org.sysarp.project.ui.components.FloatingDebugOverlay
import org.sysarp.project.ui.screens.admin.AdminDashboardScreen
import org.sysarp.project.ui.screens.admin.AdminPaymentsScreen
import org.sysarp.project.ui.screens.admin.AdminProfileScreen
import org.sysarp.project.ui.screens.admin.AdminRegistrationScreen
import org.sysarp.project.ui.screens.admin.BranchManagementScreen
import org.sysarp.project.ui.screens.admin.UserManagementScreen
import org.sysarp.project.ui.screens.admin.AdminAnalyticsScreen
import org.sysarp.project.ui.screens.common.DeactivationRequestScreen
import org.sysarp.project.ui.screens.common.ForgotPasswordScreen
import org.sysarp.project.ui.screens.common.LoginScreen
import org.sysarp.project.ui.screens.common.ProfileSelectionScreen
import org.sysarp.project.ui.screens.common.QRDisplayScreen
import org.sysarp.project.ui.screens.common.QRScannerScreen
import org.sysarp.project.ui.screens.common.SettingsScreen
import org.sysarp.project.ui.screens.common.SplashScreen
import org.sysarp.project.ui.screens.seller.SellerAnalyticsScreen
import org.sysarp.project.ui.screens.seller.SellerDashboardScreen
import org.sysarp.project.ui.screens.seller.SellerNotificationsScreen
import org.sysarp.project.ui.screens.seller.SellerPaymentsScreen
import org.sysarp.project.ui.screens.seller.SellerSpecificPaymentsScreen
import org.sysarp.project.ui.screens.seller.SellerUnifiedScreen
import org.sysarp.project.viewmodel.YapeViewModel

@Composable
fun AppContent(
    navigationManager: NavigationManager,
    viewModel: YapeViewModel,
    userProfileRepository: UserProfileRepository,
    authService: AuthService,
    sellerService: SellerService,
    paymentService: PaymentService,
    statsService: StatsService,
    affiliationService: AffiliationService,
    qrService: QRService,
    branchService: BranchService,
    webSocketService: PaymentWebSocketService
) {
    val currentScreen by navigationManager.currentScreen.collectAsState()
    
    when (currentScreen) {
        is Screen.Splash -> {
            SplashScreen(
                onSplashFinished = navigationManager::onSplashFinished
            )
        }
        is Screen.Settings -> {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = navigationManager::navigateBack,
                onNavigateToUserManagement = { navigationManager.navigateTo(Screen.UserManagement) }
            )
        }
        is Screen.PendingPayments -> {
            AdminPaymentsScreen(
                authService = authService,
                paymentService = paymentService,
                onNavigateBack = navigationManager::navigateBack
            )
        }
        is Screen.SellerPayments -> {
            SellerPaymentsScreen(
                authService = authService,
                paymentService = paymentService,
                onNavigateBack = navigationManager::navigateBack
            )
        }
        is Screen.UserManagement -> {
            UserManagementScreen(
                userProfileRepository = userProfileRepository,
                onNavigateBack = navigationManager::navigateBack
            )
        }
        is Screen.ProfileSelection -> {
            ProfileSelectionScreen(
                authService = authService,
                onAdminLogin = { navigationManager.navigateToLogin() },
                onAdminRegistration = { navigationManager.navigateToAdminRegistration() },
                onSellerLogin = { navigationManager.navigateToSellerLogin() },
                onSellerRegistration = { navigationManager.navigateToSellerRegistration() }
            )
        }
        is Screen.Login -> {
            LoginScreen(
                authService = authService,
                onLoginSuccess = { role ->
                    // Establecer el usuario en el ViewModel después del login exitoso
                    val userProfile = authService.userProfile.value
                    if (userProfile != null) {
                        viewModel.setCurrentUser(userProfile)
                    }
                    
                    // Navegar al dashboard correspondiente basado en el rol
                    when (role) {
                        "ADMIN" -> navigationManager.navigateToAdminDashboard()
                        "SELLER" -> navigationManager.navigateToSellerDashboard()
                        else -> {
                            // Si no se puede determinar el rol, ir a ProfileSelection
                            navigationManager.navigateToProfileSelection()
                        }
                    }
                },
                onBackPressed = { navigationManager.navigateBackToProfileSelection() },
                onForgotPassword = { navigationManager.navigateToForgotPassword() }
            )
        }
        is Screen.ForgotPassword -> {
            ForgotPasswordScreen(
                authService = authService,
                onNavigateBack = { navigationManager.navigateBack() },
                onNavigateToLogin = { navigationManager.navigateBackToProfileSelection() }
            )
        }
        is Screen.AdminRegistration -> {
            AdminRegistrationScreen(
                authService = authService,
                onRegistrationSuccess = { navigationManager.navigateToAdminDashboard() },
                onBackPressed = { navigationManager.navigateBackToProfileSelection() }
            )
        }
        is Screen.SellerLogin -> {
            SellerUnifiedScreen(
                sellerService = sellerService,
                authService = authService,
                statsService = statsService,
                webSocketService = webSocketService,
                onBackClick = { navigationManager.navigateBackToProfileSelection() },
                onSuccess = { 
                    // Establecer el usuario en el ViewModel después del login exitoso de seller
                    val userProfile = authService.userProfile.value
                    if (userProfile != null) {
                        viewModel.setCurrentUser(userProfile)
                    }
                    navigationManager.navigateToSellerDashboard() 
                },
                onNavigateToQRScanner = { navigationManager.navigateTo(Screen.QRScanner) }
            )
        }
        is Screen.SellerRegistration -> {
            SellerUnifiedScreen(
                sellerService = sellerService,
                authService = authService,
                statsService = statsService,
                webSocketService = webSocketService,
                onBackClick = { navigationManager.navigateBackToProfileSelection() },
                onSuccess = { 
                    // Establecer el usuario en el ViewModel después del registro exitoso de seller
                    val userProfile = authService.userProfile.value
                    if (userProfile != null) {
                        viewModel.setCurrentUser(userProfile)
                    }
                    navigationManager.navigateToSellerDashboard() 
                },
                onNavigateToQRScanner = { navigationManager.navigateTo(Screen.QRScanner) }
            )
        }
        is Screen.SellerAffiliation -> {
            SellerUnifiedScreen(
                sellerService = sellerService,
                authService = authService,
                statsService = statsService,
                webSocketService = webSocketService,
                onBackClick = { navigationManager.navigateBackToProfileSelection() },
                onSuccess = { navigationManager.navigateToSellerDashboard() },
                onNavigateToQRScanner = { navigationManager.navigateTo(Screen.QRScanner) }
            )
        }
        is Screen.AdminDashboard -> {
            AdminDashboardScreen(
                authService = authService,
                sellerService = sellerService,
                statsService = statsService,
                affiliationService = affiliationService,
                qrService = qrService,
                branchService = branchService,
                webSocketService = webSocketService,
                onNavigateToBranchManagement = { navigationManager.navigateTo(Screen.BranchManagement) },
                onNavigateToAnalytics = { navigationManager.navigateTo(Screen.Analytics) },
                onNavigateToPendingPayments = { navigationManager.navigateTo(Screen.PendingPayments) },
                onNavigateToSettings = { navigationManager.navigateTo(Screen.Settings) },
                onNavigateToDeactivationRequests = { navigationManager.navigateToDeactivationRequest() },
                onNavigateToProfile = { navigationManager.navigateTo(Screen.AdminProfile) },
                onLogout = { navigationManager.navigateToProfileSelection() }
            )
        }
        is Screen.SellerDashboard -> {
            SellerDashboardScreen(
                authService = authService,
                paymentService = paymentService,
                statsService = statsService,
                webSocketService = webSocketService,
                onNavigateToAnalytics = { navigationManager.navigateTo(Screen.SellerAnalytics) },
                onNavigateToPendingPayments = { navigationManager.navigateTo(Screen.SellerPayments) },
                onNavigateToSettings = { navigationManager.navigateTo(Screen.Settings) },
                onNavigateToDeactivationRequest = { navigationManager.navigateToDeactivationRequest() },
                onNavigateToNotifications = { navigationManager.navigateTo(Screen.SellerNotifications) },
                onLogout = { navigationManager.navigateToProfileSelection() }
            )
        }
        is Screen.SellerAnalytics -> {
            SellerAnalyticsScreen(
                authService = authService,
                statsService = statsService,
                onNavigateBack = { navigationManager.navigateBack() }
            )
        }
        is Screen.BranchManagement -> {
            val userProfile by authService.userProfile.collectAsState()
            val accessToken by authService.accessToken.collectAsState()
            
            if (userProfile?.adminId != null && accessToken != null) {
                BranchManagementScreen(
                    branchService = branchService,
                    adminId = userProfile!!.adminId!!.toInt(),
                    accessToken = accessToken!!,
                    onBackClick = { navigationManager.navigateBack() }
                )
            } else {
                // Manejar caso de error
                Text("Error: No se pudo obtener información del administrador")
            }
        }
        is Screen.Analytics -> {
            AdminAnalyticsScreen(
                authService = authService,
                statsService = statsService,
                onNavigateBack = { navigationManager.navigateBack() }
            )
        }
        is Screen.QRDisplay -> {
            val qrScreen = currentScreen as Screen.QRDisplay
            QRDisplayScreen(
                qrCode = qrScreen.qrCode,
                onNavigateBack = { navigationManager.navigateBack() },
                onShareQR = { /* TODO: Implementar compartir QR */ },
                onInvalidateQR = { /* TODO: Implementar invalidar QR */ }
            )
        }
        is Screen.DeactivationRequest -> {
            DeactivationRequestScreen(
                authService = authService,
                sellerService = sellerService,
                onNavigateBack = { navigationManager.navigateBack() }
            )
        }
        is Screen.AdminProfile -> {
            AdminProfileScreen(
                authService = authService,
                onNavigateBack = { navigationManager.navigateBack() },
                onNavigateToQR = { qrCode -> navigationManager.navigateToQRDisplay(qrCode) }
            )
        }
        is Screen.SellerNotifications -> {
            SellerNotificationsScreen(
                authService = authService,
                onNavigateBack = { navigationManager.navigateBack() }
            )
        }
        is Screen.SellerSpecificPayments -> {
            val sellerSpecificScreen = currentScreen as Screen.SellerSpecificPayments
            SellerSpecificPaymentsScreen(
                sellerId = sellerSpecificScreen.sellerId,
                sellerName = sellerSpecificScreen.sellerName,
                paymentService = paymentService,
                authService = authService,
                onNavigateBack = { navigationManager.navigateBack() }
            )
        }
        is Screen.QRScanner -> {
            QRScannerScreen(
                onNavigateBack = { navigationManager.navigateBack() },
                onQRScanned = { qrData ->
                    // Aquí manejaremos el QR escaneado
                    // Por ahora solo navegamos de vuelta
                    navigationManager.navigateBack()
                },
                onManualCodeEntry = {
                    // Aquí manejaremos la entrada manual de código
                    // Por ahora solo navegamos de vuelta
                    navigationManager.navigateBack()
                },
                onLoginSuccess = {
                    // Navegar al dashboard del vendedor después del login exitoso
                    navigationManager.navigateToSellerDashboard()
                },
                authService = authService
            )
        }
        else -> {
            // Pantalla no encontrada
            Text("Pantalla no encontrada")
        }
    }
    
    // Overlay flotante de debug (solo en modo desarrollo)
    FloatingDebugOverlay()
}
