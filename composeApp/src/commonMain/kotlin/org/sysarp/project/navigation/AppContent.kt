package org.sysarp.project.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.sysarp.project.ui.screens.HomeScreen
import org.sysarp.project.ui.screens.ReportsScreen
import org.sysarp.project.ui.screens.SettingsScreen
import org.sysarp.project.ui.screens.PaymentsScreen
import org.sysarp.project.ui.screens.UserManagementScreen
import org.sysarp.project.ui.screens.ProfileSelectionScreen
import org.sysarp.project.ui.screens.AdminRegistrationScreen
import org.sysarp.project.ui.screens.SellerAffiliationScreen
import org.sysarp.project.ui.screens.AdminDashboardScreen
import org.sysarp.project.ui.screens.SellerDashboardScreen
import org.sysarp.project.ui.screens.SellerManagementScreen
import org.sysarp.project.ui.screens.AnalyticsScreen
import org.sysarp.project.ui.screens.SplashScreen
import org.sysarp.project.ui.screens.QRDisplayScreen
import org.sysarp.project.ui.screens.LoginScreen
import org.sysarp.project.ui.screens.ForgotPasswordScreen
import org.sysarp.project.ui.screens.DeactivationRequestScreen
import org.sysarp.project.viewmodel.YapeViewModel
import org.sysarp.project.repository.UserProfileRepository
import org.sysarp.project.service.AuthService

@Composable
fun AppContent(
    navigationManager: NavigationManager,
    viewModel: YapeViewModel,
    userProfileRepository: UserProfileRepository,
    authService: AuthService,
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
                authService = authService,
                onNavigateBack = navigationManager::navigateBack,
                onLogout = { navigationManager.navigateToProfileSelection() }
            )
        }
        is Screen.PendingPayments -> {
            PaymentsScreen(
                authService = authService,
                viewModel = viewModel,
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
                onAdminSelected = { navigationManager.navigateToAdminRegistration() },
                onSellerSelected = { navigationManager.navigateToSellerAffiliation() },
                onLoginSelected = { navigationManager.navigateToLogin() }
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
        is Screen.SellerAffiliation -> {
            SellerAffiliationScreen(
                authService = authService,
                onAffiliationSuccess = { navigationManager.navigateToSellerDashboard() },
                onBackPressed = { navigationManager.navigateBackToProfileSelection() }
            )
        }
        is Screen.AdminDashboard -> {
            AdminDashboardScreen(
                authService = authService,
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
                onNavigateToHistory = { navigationManager.navigateTo(Screen.Analytics) },
                onNavigateToPendingPayments = { navigationManager.navigateTo(Screen.PendingPayments) },
                onNavigateToSettings = { navigationManager.navigateTo(Screen.Settings) },
                onNavigateToDeactivationRequest = { navigationManager.navigateToDeactivationRequest() },
                onLogout = { navigationManager.navigateToProfileSelection() }
            )
        }
        is Screen.SellerManagement -> {
            SellerManagementScreen(
                authService = authService,
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
                onNavigateBack = { navigationManager.navigateBack() }
            )
        }
    }
}
