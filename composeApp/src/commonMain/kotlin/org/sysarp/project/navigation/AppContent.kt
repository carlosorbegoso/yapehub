package org.sysarp.project.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.sysarp.project.repository.UserProfileRepository
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.SellerService
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.service.stats.StatsService

import org.sysarp.project.ui.screens.AdminDashboardScreen
import org.sysarp.project.ui.screens.AdminRegistrationScreen
import org.sysarp.project.ui.screens.AnalyticsScreen
import org.sysarp.project.ui.screens.DeactivationRequestScreen
import org.sysarp.project.ui.screens.ForgotPasswordScreen
import org.sysarp.project.ui.screens.LoginScreen
import org.sysarp.project.ui.screens.PaymentsScreen
import org.sysarp.project.ui.screens.ProfileSelectionScreen
import org.sysarp.project.ui.screens.QRDisplayScreen
import org.sysarp.project.ui.screens.ReportsScreen
import org.sysarp.project.ui.screens.SellerAffiliationScreen
import org.sysarp.project.ui.screens.SellerDashboardScreen
import org.sysarp.project.ui.screens.SellerLoginScreen
import org.sysarp.project.ui.screens.SellerManagementScreen
import org.sysarp.project.ui.screens.SellerPaymentsScreen
import org.sysarp.project.ui.screens.SellerRegistrationScreen
import org.sysarp.project.ui.screens.SettingsScreen
import org.sysarp.project.ui.screens.SplashScreen
import org.sysarp.project.ui.screens.UserManagementScreen
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
    modifier: Modifier = Modifier
) {
    val currentScreen by navigationManager.currentScreen.collectAsState()
    
    when (currentScreen) {
        is Screen.Splash -> {
            SplashScreen(
                onSplashFinished = navigationManager::onSplashFinished
            )
        }
        is Screen.Reports -> {
            ReportsScreen(
                viewModel = viewModel,
                onNavigateBack = navigationManager::navigateBack
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
            PaymentsScreen(
                authService = authService,
                viewModel = viewModel,
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
                onBackPressed = { navigationManager.navigateBack() },
                onResetSuccess = { navigationManager.navigateBackToProfileSelection() }
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
            SellerAffiliationScreen(
                sellerService = sellerService,
                onBackClick = { navigationManager.navigateBackToProfileSelection() },
                onAffiliationSuccess = { navigationManager.navigateToSellerDashboard() },
                onLoginSuccess = { navigationManager.navigateToSellerDashboard() }
            )
        }
        is Screen.SellerRegistration -> {
            SellerAffiliationScreen(
                sellerService = sellerService,
                onBackClick = { navigationManager.navigateBackToProfileSelection() },
                onAffiliationSuccess = { navigationManager.navigateToSellerDashboard() },
                onLoginSuccess = { navigationManager.navigateToSellerDashboard() }
            )
        }
        is Screen.SellerAffiliation -> {
            SellerAffiliationScreen(
                sellerService = sellerService,
                onBackClick = { navigationManager.navigateBackToProfileSelection() },
                onAffiliationSuccess = { navigationManager.navigateToSellerDashboard() },
                onLoginSuccess = { navigationManager.navigateToSellerDashboard() }
            )
        }
        is Screen.AdminDashboard -> {
            AdminDashboardScreen(
                authService = authService,
                sellerService = sellerService,
                statsService = statsService,
                onNavigateToSellerManagement = { navigationManager.navigateTo(Screen.SellerManagement) },
                onNavigateToAnalytics = { navigationManager.navigateTo(Screen.Analytics) },
                onNavigateToPendingPayments = { navigationManager.navigateTo(Screen.PendingPayments) },
                onNavigateToSettings = { navigationManager.navigateTo(Screen.Settings) },
                onNavigateToDeactivationRequests = { navigationManager.navigateToDeactivationRequest() },
                onLogout = { navigationManager.navigateToProfileSelection() }
            )
        }
        is Screen.SellerDashboard -> {
            SellerDashboardScreen(
                authService = authService,
                paymentService = paymentService,
                statsService = statsService,
                onNavigateToHistory = { navigationManager.navigateTo(Screen.Analytics) },
                onNavigateToPendingPayments = { navigationManager.navigateTo(Screen.SellerPayments) },
                onNavigateToSettings = { navigationManager.navigateTo(Screen.Settings) },
                onNavigateToDeactivationRequest = { navigationManager.navigateToDeactivationRequest() },
                onLogout = { navigationManager.navigateToProfileSelection() }
            )
        }
        is Screen.SellerManagement -> {
            SellerManagementScreen(
                authService = authService,
                sellerService = sellerService,
                onNavigateBack = { navigationManager.navigateBack() },
                onNavigateToQR = { qrCode -> navigationManager.navigateToQRDisplay(qrCode) }
            )
        }
        is Screen.Analytics -> {
            AnalyticsScreen(
                authService = authService,
                viewModel = viewModel,
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
    }
}
