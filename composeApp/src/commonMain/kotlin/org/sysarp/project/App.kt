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
import org.sysarp.project.service.SellerService
import org.sysarp.project.service.SimpleNotificationService
import org.sysarp.project.service.affiliation.AffiliationService
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.branch.BranchService
import org.sysarp.project.service.http.PaymentApiClient
import org.sysarp.project.service.http.StatsApiClient
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

// RepositorySingleton eliminado - no usamos base de datos local

@Composable
fun YapeApp() {
    // Sin base de datos local - eliminado RepositorySingleton
    
    val userProfileRepository = remember {
        UserProfileRepository()
    }
    


    val notificationService = remember {
        object : SimpleNotificationService {
            override fun startCapture() {
                println("🚀 Iniciando captura de notificaciones de Yape")
            }
            override fun stopCapture() {
                println("🛑 Deteniendo captura de notificaciones de Yape")
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
    
    val paymentNotificationService = remember {
        org.sysarp.project.service.notifications.PaymentNotificationService()
    }
    
    // Sin base de datos local, solo usar servicios
    val viewModel = remember {
        YapeViewModel(notificationService, userProfileRepository)
    }
    
    // Iniciar servicios WebSocket
    LaunchedEffect(Unit) {
        webSocketService.startAutoConnect()
    }
    
    // Detener servicios al desmontar
    DisposableEffect(Unit) {
        onDispose {
            webSocketService.stop()
        }
    }
    
    // Sistema de navegación simple multiplataforma
    val navigationManager = rememberNavigationManager()
    
    // Contenido de la aplicación
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
        viewModel = viewModel,
        userProfileRepository = userProfileRepository
    )
}
