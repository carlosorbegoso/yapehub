package org.sysarp.project.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.koinInject
import org.sysarp.project.repository.UserProfileRepository
import org.sysarp.project.service.CredentialStorageService
import org.sysarp.project.service.SellerService
import org.sysarp.project.service.affiliation.AffiliationService
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.billing.BillingService
import org.sysarp.project.service.branch.BranchService
import org.sysarp.project.service.notification.HybridNotificationManager
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.service.qr.QRService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.service.websocket.PaymentWebSocketService
import org.sysarp.project.ui.admin.screens.dashboard.AdminDashboardScreen
import org.sysarp.project.ui.admin.screens.management.BranchManagementScreen
import org.sysarp.project.ui.admin.screens.management.SellerManagementScreen
import org.sysarp.project.ui.admin.screens.management.UserManagementScreen
import org.sysarp.project.ui.admin.screens.payments.AdminPaymentsScreen
import org.sysarp.project.ui.admin.screens.profile.AdminProfileScreen
import org.sysarp.project.ui.common.screens.BillingDashboardScreen
import org.sysarp.project.ui.common.screens.DeactivationRequestScreen
import org.sysarp.project.ui.common.screens.ForgotPasswordScreen
import org.sysarp.project.ui.common.screens.LoginScreen
import org.sysarp.project.ui.common.screens.PaymentDialog
import org.sysarp.project.ui.common.screens.ProfileSelectionScreen
import org.sysarp.project.ui.common.screens.QRDisplayScreen
import org.sysarp.project.ui.common.screens.QRScannerScreen
import org.sysarp.project.ui.common.screens.SettingsScreen
import org.sysarp.project.ui.common.screens.SplashScreen
import org.sysarp.project.ui.common.screens.SubscriptionScreen
import org.sysarp.project.ui.screens.admin.AdminAnalyticsScreen
import org.sysarp.project.ui.screens.admin.AdminRegistrationScreen
import org.sysarp.project.ui.screens.seller.SellerAnalyticsScreen
import org.sysarp.project.ui.screens.seller.SellerDashboardScreen
import org.sysarp.project.ui.screens.seller.SellerNotificationsScreen
import org.sysarp.project.ui.seller.screens.payments.SellerPaymentsScreen
import org.sysarp.project.ui.screens.seller.SellerSpecificPaymentsScreen
import org.sysarp.project.ui.screens.seller.SellerUnifiedScreen
import org.sysarp.project.viewmodel.YapeViewModel

@Composable
fun AppContent(
    navigationManager: NavigationManager
) {
    val currentScreen by navigationManager.currentScreen.collectAsState()
    
    when (currentScreen) {
        is Screen.Splash -> {
            SplashScreen(
                onSplashFinished = navigationManager::onSplashFinished
            )
        }
        is Screen.Settings -> {
            val viewModel: YapeViewModel = koinInject()
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = navigationManager::navigateBack,
                onNavigateToUserManagement = { navigationManager.navigateTo(Screen.UserManagement) }
            )
        }
        is Screen.PendingPayments -> {
            AdminPaymentsScreen(
                onNavigateBack = navigationManager::navigateBack
            )
        }
        is Screen.SellerPayments -> {
            val authService: AuthService = koinInject()
            val paymentService: PaymentService = koinInject()
            SellerPaymentsScreen(
                authService = authService,
                paymentService = paymentService,
                onNavigateBack = navigationManager::navigateBack
            )
        }
        is Screen.UserManagement -> {
            val userProfileRepository: UserProfileRepository = koinInject()
            UserManagementScreen(
                userProfileRepository = userProfileRepository,
                onNavigateBack = navigationManager::navigateBack
            )
        }
        is Screen.ProfileSelection -> {
            val authService: AuthService = koinInject()
            ProfileSelectionScreen(
                authService = authService,
                onAdminLogin = { navigationManager.navigateToLogin() },
                onAdminRegistration = { navigationManager.navigateToAdminRegistration() },
                onSellerLogin = { navigationManager.navigateToSellerLogin() },
                onSellerRegistration = { navigationManager.navigateToSellerRegistration() }
            )
        }
        is Screen.Login -> {
            val authService: AuthService = koinInject()
            val credentialStorageService: CredentialStorageService = koinInject()
            val viewModel: YapeViewModel = koinInject()
            LoginScreen(
                authService = authService,
                credentialStorageService = credentialStorageService,
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
            val authService: AuthService = koinInject()
            ForgotPasswordScreen(
                authService = authService,
                onNavigateBack = { navigationManager.navigateBack() },
                onNavigateToLogin = { navigationManager.navigateBackToProfileSelection() }
            )
        }
        is Screen.AdminRegistration -> {
            val authService: AuthService = koinInject()
            AdminRegistrationScreen(
                authService = authService,
                onRegistrationSuccess = { navigationManager.navigateToAdminDashboard() },
                onBackPressed = { navigationManager.navigateBackToProfileSelection() }
            )
        }
        is Screen.SellerLogin -> {
            val sellerService: SellerService = koinInject()
            val authService: AuthService = koinInject()
            val statsService: StatsService = koinInject()
            val webSocketService: PaymentWebSocketService = koinInject()
            val viewModel: YapeViewModel = koinInject()
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
            val sellerService: SellerService = koinInject()
            val authService: AuthService = koinInject()
            val statsService: StatsService = koinInject()
            val webSocketService: PaymentWebSocketService = koinInject()
            val viewModel: YapeViewModel = koinInject()
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
            val sellerService: SellerService = koinInject()
            val authService: AuthService = koinInject()
            val statsService: StatsService = koinInject()
            val webSocketService: PaymentWebSocketService = koinInject()
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
            val authService: AuthService = koinInject()
            val sellerService: SellerService = koinInject()
            val statsService: StatsService = koinInject()
            val affiliationService: AffiliationService = koinInject()
            val qrService: QRService = koinInject()
            val branchService: BranchService = koinInject()
            val webSocketService: PaymentWebSocketService = koinInject()
            val billingService: BillingService = koinInject()
            AdminDashboardScreen(
                authService = authService,
                sellerService = sellerService,
                statsService = statsService,
                affiliationService = affiliationService,
                qrService = qrService,
                branchService = branchService,
                webSocketService = webSocketService,
                billingService = billingService,
                onNavigateToBranchManagement = { navigationManager.navigateTo(Screen.BranchManagement) },
                onNavigateToSellerManagement = { navigationManager.navigateTo(Screen.SellerManagement) },
                onNavigateToAnalytics = { navigationManager.navigateTo(Screen.Analytics) },
                onNavigateToPendingPayments = { navigationManager.navigateTo(Screen.PendingPayments) },
                onNavigateToSettings = { navigationManager.navigateTo(Screen.Settings) },
                onNavigateToDeactivationRequests = { navigationManager.navigateToDeactivationRequest() },
                onNavigateToProfile = { navigationManager.navigateTo(Screen.AdminProfile) },
                onNavigateToBilling = { navigationManager.navigateToBillingDashboard() },
                onLogout = { navigationManager.navigateToProfileSelection() }
            )
        }
        is Screen.SellerManagement -> {
            val sellerService: SellerService = koinInject()
            val authService: AuthService = koinInject()
            SellerManagementScreen(
                sellerService = sellerService,
                authService = authService,
                onNavigateBack = { navigationManager.navigateTo(Screen.AdminDashboard) }
            )
        }
        is Screen.SellerDashboard -> {
            val authService: AuthService = koinInject()
            val paymentService: PaymentService = koinInject()
            val statsService: StatsService = koinInject()
            val webSocketService: PaymentWebSocketService = koinInject()
            val hybridNotificationManager: HybridNotificationManager = koinInject()
            SellerDashboardScreen(
                authService = authService,
                paymentService = paymentService,
                statsService = statsService,
                webSocketService = webSocketService,
                hybridNotificationManager = hybridNotificationManager,
                onNavigateToAnalytics = { navigationManager.navigateTo(Screen.SellerAnalytics) },
                onNavigateToPendingPayments = { navigationManager.navigateTo(Screen.SellerPayments) },
                onNavigateToSettings = { navigationManager.navigateTo(Screen.Settings) },
                onNavigateToDeactivationRequest = { navigationManager.navigateToDeactivationRequest() },
                onNavigateToNotifications = { navigationManager.navigateTo(Screen.SellerNotifications) },
                onLogout = { navigationManager.navigateToProfileSelection() }
            )
        }
        is Screen.SellerAnalytics -> {
            val authService: AuthService = koinInject()
            val statsService: StatsService = koinInject()
            SellerAnalyticsScreen(
                authService = authService,
                statsService = statsService,
                onNavigateBack = { navigationManager.navigateBack() }
            )
        }
        is Screen.BranchManagement -> {
            val authService: AuthService = koinInject()
            val branchService: BranchService = koinInject()
            val userProfile by authService.userProfile.collectAsState()
            val accessToken by authService.accessToken.collectAsState()
            
            if (userProfile?.adminId != null && accessToken != null) {
                BranchManagementScreen(
                    branchService = branchService,
                    adminId = userProfile!!.adminId!!.toInt(),
                    accessToken = accessToken!!,
                    onBackClick = { navigationManager.navigateBack() },
                    onNavigateToBranchDetails = { branch ->
                        // TODO: Implementar navegación a detalles de sucursal
                    },
                    onNavigateToSellers = { branch ->
                        // TODO: Implementar navegación a vendedores de la sucursal
                    },
                    onNavigateToEditBranch = { branch ->
                        // TODO: Implementar navegación a editar sucursal
                    },
                    onNavigateToAddBranch = {
                        // TODO: Implementar navegación a agregar sucursal
                    }
                )
            } else {
                // Manejar caso de error
                Text("Error: No se pudo obtener información del administrador")
            }
        }
        is Screen.Analytics -> {
            val authService: AuthService = koinInject()
            val statsService: StatsService = koinInject()
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
                onShareQR = { },
                onInvalidateQR = { }
            )
        }
        is Screen.DeactivationRequest -> {
            val authService: AuthService = koinInject()
            val sellerService: SellerService = koinInject()
            DeactivationRequestScreen(
                authService = authService,
                sellerService = sellerService,
                onNavigateBack = { navigationManager.navigateBack() }
            )
        }
        is Screen.AdminProfile -> {
            val authService: AuthService = koinInject()
            AdminProfileScreen(
                authService = authService,
                onNavigateBack = { navigationManager.navigateBack() },
                onNavigateToQR = { qrCode -> navigationManager.navigateToQRDisplay(qrCode) }
            )
        }
        is Screen.SellerNotifications -> {
            val authService: AuthService = koinInject()
            SellerNotificationsScreen(
                authService = authService,
                onNavigateBack = { navigationManager.navigateBack() }
            )
        }
        is Screen.SellerSpecificPayments -> {
            val sellerSpecificScreen = currentScreen as Screen.SellerSpecificPayments
            val paymentService: PaymentService = koinInject()
            val authService: AuthService = koinInject()
            SellerSpecificPaymentsScreen(
                sellerId = sellerSpecificScreen.sellerId,
                sellerName = sellerSpecificScreen.sellerName,
                paymentService = paymentService,
                authService = authService,
                onNavigateBack = { navigationManager.navigateBack() }
            )
        }
        is Screen.QRScanner -> {
            val authService: AuthService = koinInject()
            QRScannerScreen(
                onNavigateBack = { navigationManager.navigateBack() },
                onQRScanned = { qrData ->
                    navigationManager.navigateBack()
                },
                onManualCodeEntry = {
                    navigationManager.navigateBack()
                },
                onLoginSuccess = {
                    // Navegar al dashboard del vendedor después del login exitoso
                    navigationManager.navigateToSellerDashboard()
                },
                authService = authService
            )
        }
        is Screen.BillingDashboard -> {
            val billingService: BillingService = koinInject()
            BillingDashboardScreen(
                billingService = billingService,
                onNavigateBack = navigationManager::navigateBack,
                onNavigateToSubscriptions = navigationManager::navigateToSubscriptions,
                onNavigateToPayment = { paymentCode ->
                    navigationManager.navigateToPaymentDialog(paymentCode)
                }
            )
        }
        is Screen.Subscriptions -> {
            val billingService: BillingService = koinInject()
            SubscriptionScreen(
                billingService = billingService,
                onNavigateBack = navigationManager::navigateBack,
                onNavigateToPayment = { paymentCode ->
                    navigationManager.navigateToPaymentDialog(paymentCode)
                }
            )
        }
        is Screen.PaymentDialog -> {
            val billingService: BillingService = koinInject()
            PaymentDialog(
                paymentCode = (currentScreen as Screen.PaymentDialog).paymentCode,
                billingService = billingService,
                onDismiss = navigationManager::navigateBack,
                onPaymentCompleted = {
                    navigationManager.navigateBack()
                }
            )
        }
        else -> {
            // Pantalla no encontrada
            Text("Pantalla no encontrada")
        }
    }
    
}
