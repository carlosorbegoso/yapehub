package org.sysarp.project.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.sysarp.project.ui.screens.HomeScreen
import org.sysarp.project.ui.screens.ReportsScreen
import org.sysarp.project.ui.screens.SettingsScreen
import org.sysarp.project.ui.screens.PendingPaymentsScreen
import org.sysarp.project.ui.screens.UserManagementScreen
import org.sysarp.project.ui.screens.ProfileSelectionScreen
import org.sysarp.project.ui.screens.AdminRegistrationScreen
import org.sysarp.project.ui.screens.SellerAffiliationScreen
import org.sysarp.project.ui.screens.SplashScreen
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
        is Screen.Main -> {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToReports = { navigationManager.navigateTo(Screen.Reports) },
                onNavigateToSettings = { navigationManager.navigateTo(Screen.Settings) },
                onNavigateToPendingPayments = { navigationManager.navigateTo(Screen.PendingPayments) },
                onLogout = { navigationManager.navigateToProfileSelection() }
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
            val pendingPayments by viewModel.pendingPayments.collectAsState()
            val currentUser by viewModel.currentUser.collectAsState()
            PendingPaymentsScreen(
                pendingPayments = viewModel.getPendingPaymentsForVendor(),
                currentUser = currentUser,
                canConfirmPayment = { payment -> viewModel.canVendorConfirmPayment(payment) },
                onConfirmPayment = { payment -> viewModel.confirmPayment(payment) },
                onRejectPayment = { payment -> viewModel.rejectPayment(payment) },
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
                onSellerSelected = { navigationManager.navigateToSellerAffiliation() }
            )
        }
        is Screen.AdminRegistration -> {
            AdminRegistrationScreen(
                authService = authService,
                onRegistrationSuccess = { navigationManager.navigateToMain() },
                onBackPressed = { navigationManager.navigateBackToProfileSelection() }
            )
        }
        is Screen.SellerAffiliation -> {
            SellerAffiliationScreen(
                authService = authService,
                onAffiliationSuccess = { navigationManager.navigateToMain() },
                onBackPressed = { navigationManager.navigateBackToProfileSelection() }
            )
        }
    }
}
