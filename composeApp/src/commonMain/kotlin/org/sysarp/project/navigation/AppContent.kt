package org.sysarp.project.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.koinInject
import org.sysarp.project.data.SellerPendingPayment
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
import org.sysarp.project.ui.admin.screens.analytics.AdminAnalyticsScreen
import org.sysarp.project.ui.common.screens.*
import org.sysarp.project.ui.seller.screens.analytics.SellerAnalyticsScreen
import org.sysarp.project.ui.seller.screens.dashboard.SellerDashboardScreen
import org.sysarp.project.ui.seller.screens.notifications.SellerNotificationsScreen
import org.sysarp.project.ui.seller.screens.payments.SellerPaymentsScreen
import org.sysarp.project.ui.seller.screens.payments.SellerSpecificPaymentsScreen
import org.sysarp.project.ui.seller.screens.auth.SellerUnifiedScreen
import org.sysarp.project.ui.screens.admin.AdminRegistrationScreen
import org.sysarp.project.viewmodel.YapeViewModel

@Composable
fun AppContent(navigationManager: NavigationManager) {
    val currentScreen by navigationManager.currentScreen.collectAsState()

    when (currentScreen) {
        is Screen.Splash -> SplashScreenRoute(navigationManager)
        is Screen.Settings -> SettingsScreenRoute(navigationManager)
        is Screen.PendingPayments -> AdminPaymentsScreenRoute(navigationManager)
        is Screen.SellerPayments -> SellerPaymentsScreenRoute(navigationManager)
        is Screen.UserManagement -> UserManagementScreenRoute(navigationManager)
        is Screen.ProfileSelection -> ProfileSelectionScreenRoute(navigationManager)
        is Screen.Login -> LoginScreenRoute(navigationManager)
        is Screen.ForgotPassword -> ForgotPasswordScreenRoute(navigationManager)
        is Screen.AdminRegistration -> AdminRegistrationScreenRoute(navigationManager)
        is Screen.SellerLogin -> SellerLoginScreenRoute(navigationManager)
        is Screen.SellerRegistration -> SellerRegistrationScreenRoute(navigationManager)
        is Screen.SellerAffiliation -> SellerAffiliationScreenRoute(navigationManager)
        is Screen.AdminDashboard -> AdminDashboardScreenRoute(navigationManager)
        is Screen.SellerManagement -> SellerManagementScreenRoute(navigationManager)
        is Screen.SellerDashboard -> SellerDashboardScreenRoute(navigationManager)
        is Screen.SellerAnalytics -> SellerAnalyticsScreenRoute(navigationManager)
        is Screen.BranchManagement -> BranchManagementScreenRoute(navigationManager)
        is Screen.Analytics -> AdminAnalyticsScreenRoute(navigationManager)
        is Screen.QRDisplay -> QRDisplayScreenRoute(navigationManager, currentScreen)
        is Screen.DeactivationRequest -> DeactivationRequestScreenRoute(navigationManager)
        is Screen.AdminProfile -> AdminProfileScreenRoute(navigationManager)
        is Screen.SellerNotifications -> SellerNotificationsScreenRoute(navigationManager)
        is Screen.SellerSpecificPayments -> SellerSpecificPaymentsScreenRoute(navigationManager, currentScreen)
        is Screen.QRScanner -> QRScannerScreenRoute(navigationManager)
        is Screen.BillingDashboard -> BillingDashboardScreenRoute(navigationManager)
        is Screen.Subscriptions -> SubscriptionsScreenRoute(navigationManager)
        is Screen.PaymentDialog -> PaymentDialogScreenRoute(navigationManager, currentScreen)
        else -> UnknownScreenError()
    }
}

// ==================== ROUTE COMPOSABLES ====================

@Composable
private fun SplashScreenRoute(navigationManager: NavigationManager) {
    SplashScreen(onSplashFinished = navigationManager::onSplashFinished)
}

@Composable
private fun SettingsScreenRoute(navigationManager: NavigationManager) {
    val viewModel: YapeViewModel = koinInject()
    SettingsScreen(
        viewModel = viewModel,
        onNavigateBack = navigationManager::navigateBack,
        onNavigateToUserManagement = { navigationManager.navigateTo(Screen.UserManagement) }
    )
}

@Composable
private fun AdminPaymentsScreenRoute(navigationManager: NavigationManager) {
    AdminPaymentsScreen(onNavigateBack = navigationManager::navigateBack)
}

@Composable
private fun SellerPaymentsScreenRoute(navigationManager: NavigationManager) {
    val authService: AuthService = koinInject()
    val paymentService: PaymentService = koinInject()
    val statsService: StatsService = koinInject()

    SellerPaymentsScreen(
        authService = authService,
        paymentService = paymentService,
        statsService = statsService,
        onNavigateBack = navigationManager::navigateBack
    )
}

@Composable
private fun UserManagementScreenRoute(navigationManager: NavigationManager) {
    val userProfileRepository: UserProfileRepository = koinInject()
    UserManagementScreen(
        userProfileRepository = userProfileRepository,
        onNavigateBack = navigationManager::navigateBack
    )
}

@Composable
private fun ProfileSelectionScreenRoute(navigationManager: NavigationManager) {
    val authService: AuthService = koinInject()
    ProfileSelectionScreen(
        authService = authService,
        onAdminLogin = { navigationManager.navigateToLogin() },
        onAdminRegistration = { navigationManager.navigateToAdminRegistration() },
        onSellerLogin = { navigationManager.navigateToSellerLogin() },
        onSellerRegistration = { navigationManager.navigateToSellerRegistration() }
    )
}

@Composable
private fun LoginScreenRoute(navigationManager: NavigationManager) {
    val authService: AuthService = koinInject()
    val credentialStorageService: CredentialStorageService = koinInject()
    val viewModel: YapeViewModel = koinInject()

    LoginScreen(
        authService = authService,
        credentialStorageService = credentialStorageService,
        onLoginSuccess = { role -> handleLoginSuccess(role, authService, viewModel, navigationManager) },
        onBackPressed = { navigationManager.navigateBackToProfileSelection() },
        onForgotPassword = { navigationManager.navigateToForgotPassword() }
    )
}

@Composable
private fun ForgotPasswordScreenRoute(navigationManager: NavigationManager) {
    val authService: AuthService = koinInject()
    ForgotPasswordScreen(
        authService = authService,
        onNavigateBack = { navigationManager.navigateBack() },
        onNavigateToLogin = { navigationManager.navigateBackToProfileSelection() }
    )
}

@Composable
private fun AdminRegistrationScreenRoute(navigationManager: NavigationManager) {
    val authService: AuthService = koinInject()
    AdminRegistrationScreen(
        authService = authService,
        onRegistrationSuccess = { navigationManager.navigateToAdminDashboard() },
        onBackPressed = { navigationManager.navigateBackToProfileSelection() }
    )
}

@Composable
private fun SellerLoginScreenRoute(navigationManager: NavigationManager) {
    val services = SellerScreenServices()

    SellerUnifiedScreen(
        sellerService = services.sellerService,
        authService = services.authService,
        statsService = services.statsService,
        webSocketService = services.webSocketService,
        onBackClick = { navigationManager.navigateBackToProfileSelection() },
        onSuccess = {
            handleSellerLoginSuccess(services.authService, services.viewModel, navigationManager)
        },
        onNavigateToQRScanner = { navigationManager.navigateTo(Screen.QRScanner) }
    )
}

@Composable
private fun SellerRegistrationScreenRoute(navigationManager: NavigationManager) {
    val services = SellerScreenServices()

    SellerUnifiedScreen(
        sellerService = services.sellerService,
        authService = services.authService,
        statsService = services.statsService,
        webSocketService = services.webSocketService,
        onBackClick = { navigationManager.navigateBackToProfileSelection() },
        onSuccess = {
            handleSellerLoginSuccess(services.authService, services.viewModel, navigationManager)
        },
        onNavigateToQRScanner = { navigationManager.navigateTo(Screen.QRScanner) }
    )
}

@Composable
private fun SellerAffiliationScreenRoute(navigationManager: NavigationManager) {
    val services = SellerScreenServices()

    SellerUnifiedScreen(
        sellerService = services.sellerService,
        authService = services.authService,
        statsService = services.statsService,
        webSocketService = services.webSocketService,
        onBackClick = { navigationManager.navigateBackToProfileSelection() },
        onSuccess = { navigationManager.navigateToSellerDashboard() },
        onNavigateToQRScanner = { navigationManager.navigateTo(Screen.QRScanner) }
    )
}

@Composable
private fun AdminDashboardScreenRoute(navigationManager: NavigationManager) {
    val services = AdminDashboardServices()

    AdminDashboardScreen(
        authService = services.authService,
        sellerService = services.sellerService,
        statsService = services.statsService,
        affiliationService = services.affiliationService,
        qrService = services.qrService,
        branchService = services.branchService,
        webSocketService = services.webSocketService,
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

@Composable
private fun SellerManagementScreenRoute(navigationManager: NavigationManager) {
    val sellerService: SellerService = koinInject()
    val authService: AuthService = koinInject()

    SellerManagementScreen(
        sellerService = sellerService,
        authService = authService,
        onNavigateBack = { navigationManager.navigateTo(Screen.AdminDashboard) }
    )
}

@Composable
private fun SellerDashboardScreenRoute(navigationManager: NavigationManager) {
    val services = SellerDashboardServices()

    SellerDashboardScreen(
        authService = services.authService,
        paymentService = services.paymentService,
        statsService = services.statsService,
        webSocketService = services.webSocketService,
        hybridNotificationManager = services.hybridNotificationManager,
        onNavigateToAnalytics = { navigationManager.navigateTo(Screen.SellerAnalytics) },
        onNavigateToPendingPayments = { navigationManager.navigateTo(Screen.SellerPayments) },
        onNavigateToSettings = { navigationManager.navigateTo(Screen.Settings) },
        onNavigateToDeactivationRequest = { navigationManager.navigateToDeactivationRequest() },
        onNavigateToNotifications = { navigationManager.navigateTo(Screen.SellerNotifications) },
        onLogout = { navigationManager.navigateToProfileSelection() }
    )
}

@Composable
private fun SellerAnalyticsScreenRoute(navigationManager: NavigationManager) {
    val authService: AuthService = koinInject()
    val statsService: StatsService = koinInject()

    SellerAnalyticsScreen(
        authService = authService,
        statsService = statsService,
        onNavigateBack = { navigationManager.navigateBack() }
    )
}

@Composable
private fun BranchManagementScreenRoute(navigationManager: NavigationManager) {
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
            onNavigateToBranchDetails = { /* TODO: Implement */ },
            onNavigateToSellers = { /* TODO: Implement */ },
            onNavigateToEditBranch = { /* TODO: Implement */ },
            onNavigateToAddBranch = { /* TODO: Implement */ }
        )
    } else {
        UnknownScreenError()
    }
}

@Composable
private fun AdminAnalyticsScreenRoute(navigationManager: NavigationManager) {
    val authService: AuthService = koinInject()
    val statsService: StatsService = koinInject()

    AdminAnalyticsScreen(
        authService = authService,
        statsService = statsService,
        onNavigateBack = { navigationManager.navigateBack() }
    )
}

@Composable
private fun QRDisplayScreenRoute(navigationManager: NavigationManager, currentScreen: Screen) {
    val qrScreen = currentScreen as Screen.QRDisplay
    QRDisplayScreen(
        qrCode = qrScreen.qrCode,
        onNavigateBack = { navigationManager.navigateBack() },
        onShareQR = { },
        onInvalidateQR = { }
    )
}

@Composable
private fun DeactivationRequestScreenRoute(navigationManager: NavigationManager) {
    val authService: AuthService = koinInject()
    val sellerService: SellerService = koinInject()

    DeactivationRequestScreen(
        authService = authService,
        sellerService = sellerService,
        onNavigateBack = { navigationManager.navigateBack() }
    )
}

@Composable
private fun AdminProfileScreenRoute(navigationManager: NavigationManager) {
    val authService: AuthService = koinInject()
    AdminProfileScreen(
        authService = authService,
        onNavigateBack = { navigationManager.navigateBack() },
        onNavigateToQR = { qrCode -> navigationManager.navigateToQRDisplay(qrCode) }
    )
}

@Composable
private fun SellerNotificationsScreenRoute(navigationManager: NavigationManager) {
    val authService: AuthService = koinInject()
    SellerNotificationsScreen(
        authService = authService,
        onNavigateBack = { navigationManager.navigateBack() }
    )
}

@Composable
private fun SellerSpecificPaymentsScreenRoute(navigationManager: NavigationManager, currentScreen: Screen) {
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

@Composable
private fun QRScannerScreenRoute(navigationManager: NavigationManager) {
    val authService: AuthService = koinInject()
    QRScannerScreen(
        onNavigateBack = { navigationManager.navigateBack() },
        onQRScanned = { navigationManager.navigateBack() },
        onManualCodeEntry = { navigationManager.navigateBack() },
        onLoginSuccess = { navigationManager.navigateToSellerDashboard() },
        authService = authService
    )
}

@Composable
private fun BillingDashboardScreenRoute(navigationManager: NavigationManager) {
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

@Composable
private fun SubscriptionsScreenRoute(navigationManager: NavigationManager) {
    val billingService: BillingService = koinInject()
    SubscriptionScreen(
        billingService = billingService,
        onNavigateBack = navigationManager::navigateBack,
        onNavigateToPayment = { paymentCode ->
            navigationManager.navigateToPaymentDialog(paymentCode)
        }
    )
}

@Composable
private fun PaymentDialogScreenRoute(navigationManager: NavigationManager, currentScreen: Screen) {
    val billingService: BillingService = koinInject()
    PaymentDialog(
        paymentCode = (currentScreen as Screen.PaymentDialog).paymentCode,
        billingService = billingService,
        onDismiss = navigationManager::navigateBack,
        onPaymentCompleted = { navigationManager.navigateBack() }
    )
}

@Composable
private fun UnknownScreenError() {
    Text("Pantalla no encontrada")
}

// ==================== SERVICE CONTAINERS ====================

@Composable
private fun SellerScreenServices() = object {
    val sellerService: SellerService = koinInject()
    val authService: AuthService = koinInject()
    val statsService: StatsService = koinInject()
    val webSocketService: PaymentWebSocketService = koinInject()
    val viewModel: YapeViewModel = koinInject()
}

@Composable
private fun AdminDashboardServices() = object {
    val authService: AuthService = koinInject()
    val sellerService: SellerService = koinInject()
    val statsService: StatsService = koinInject()
    val affiliationService: AffiliationService = koinInject()
    val qrService: QRService = koinInject()
    val branchService: BranchService = koinInject()
    val webSocketService: PaymentWebSocketService = koinInject()
}

@Composable
private fun SellerDashboardServices() = object {
    val authService: AuthService = koinInject()
    val paymentService: PaymentService = koinInject()
    val statsService: StatsService = koinInject()
    val webSocketService: PaymentWebSocketService = koinInject()
    val hybridNotificationManager: HybridNotificationManager = koinInject()
}

// ==================== NAVIGATION HANDLERS ====================

private fun handleLoginSuccess(
    role: String,
    authService: AuthService,
    viewModel: YapeViewModel,
    navigationManager: NavigationManager
) {
    authService.userProfile.value?.let { viewModel.setCurrentUser(it) }

    when (role) {
        "ADMIN" -> navigationManager.navigateToAdminDashboard()
        "SELLER" -> navigationManager.navigateToSellerDashboard()
        else -> navigationManager.navigateToProfileSelection()
    }
}

private fun handleSellerLoginSuccess(
    authService: AuthService,
    viewModel: YapeViewModel,
    navigationManager: NavigationManager
) {
    authService.userProfile.value?.let { viewModel.setCurrentUser(it) }
    navigationManager.navigateToSellerDashboard()
}