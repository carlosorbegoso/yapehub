package org.sysarp.project

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.ktor.client.HttpClient
import org.sysarp.project.navigation.AppContent
import org.sysarp.project.navigation.rememberNavigationManager
import org.sysarp.project.repository.UserProfileRepository
import org.sysarp.project.service.CredentialStorageService
import org.sysarp.project.service.SellerService
import org.sysarp.project.service.SimpleNotificationService
import org.sysarp.project.service.affiliation.AffiliationService
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.billing.BillingService
import org.sysarp.project.service.branch.BranchService
import org.sysarp.project.service.http.PaymentApiClient
import org.sysarp.project.service.http.StatsApiClient
import org.sysarp.project.service.http.billing.BillingApiClient
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.ui.theme.YapeHubTheme
import org.sysarp.project.viewmodel.YapeViewModel

@Composable
fun App() {
    YapeHubTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            YapeApp()
        }
    }
}


@Composable
fun YapeApp() {
    val userProfileRepository = remember {
        UserProfileRepository()
    }
    


    val notificationService = remember {
        object : SimpleNotificationService {
            override fun startCapture() {
            }
            override fun stopCapture() {
            }
            override fun isCapturing(): Boolean = false
        }
    }
    
    val httpClient = remember {
        HttpClient()
    }
    
    val authService = remember {
        AuthService.getInstance()
    }
    
    val sellerService = remember {
        SellerService(authService)
    }
    
    val affiliationService = remember {
        AffiliationService()
    }
    
    val qrService = remember {
        org.sysarp.project.service.qr.QRService()
    }
    
    val branchService = remember {
        BranchService()
    }
    
    val paymentService = remember {
        val paymentApiClient = PaymentApiClient(httpClient)
        PaymentService(paymentApiClient)
    }
    
    val statsService = remember {
        val statsApiClient = StatsApiClient()
        StatsService(statsApiClient)
    }
    
    val webSocketService = remember {
        org.sysarp.project.service.websocket.PaymentWebSocketService(authService)
    }
    
    val billingService = remember {
        val billingApiClient = BillingApiClient()
        BillingService(billingApiClient, authService)
    }
    
    val credentialStorageService = CredentialStorageService

    remember {
        org.sysarp.project.service.notifications.PaymentNotificationService()
    }
    
    val viewModel = remember {
        YapeViewModel(notificationService, userProfileRepository)
    }
    
    LaunchedEffect(Unit) {
        webSocketService.startAutoConnect()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            webSocketService.stop()
        }
    }
    
    val navigationManager = rememberNavigationManager()
    AppContent(
        navigationManager = navigationManager,
        authService = authService,
        sellerService = sellerService,
        paymentService = paymentService,
        statsService = statsService,
        affiliationService = affiliationService,
        qrService = qrService,
        branchService = branchService,
        webSocketService = webSocketService,
        billingService = billingService,
        credentialStorageService = credentialStorageService,
        viewModel = viewModel,
        userProfileRepository = userProfileRepository
    )
}
