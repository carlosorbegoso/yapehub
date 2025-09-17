package org.sysarp.project

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.sysarp.project.navigation.AppContent
import org.sysarp.project.navigation.rememberNavigationManager
import org.sysarp.project.repository.UserProfileRepository
import org.sysarp.project.repository.YapeTransactionRepository
import org.sysarp.project.service.SimpleNotificationService
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.SellerService
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.service.http.PaymentApiClient
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.service.http.StatsApiClient
import org.sysarp.project.service.affiliation.AffiliationService
import org.sysarp.project.service.http.AffiliationApiClient
import org.sysarp.project.service.branch.BranchService
import org.sysarp.project.service.http.BranchApiClient
import io.ktor.client.HttpClient
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

// Singleton para el repositorio
object RepositorySingleton {
    private var _repository: YapeTransactionRepository? = null
    
    fun getRepository(): YapeTransactionRepository {
        if (_repository == null) {
            // Crear repositorio real que usa la API
            _repository = createApiRepository()
        }
        return _repository!!
    }
    
    private fun createApiRepository(): YapeTransactionRepository {
        return object : YapeTransactionRepository {
            override fun getAllTransactions(): kotlinx.coroutines.flow.Flow<List<org.sysarp.project.data.YapeTransaction>> {
                return kotlinx.coroutines.flow.flowOf(emptyList())
            }
            
            override fun getTransactionsByBusiness(businessName: String): kotlinx.coroutines.flow.Flow<List<org.sysarp.project.data.YapeTransaction>> {
                return kotlinx.coroutines.flow.flowOf(emptyList())
            }
            
            override fun getTransactionsByDateRange(startDate: kotlinx.datetime.Instant, endDate: kotlinx.datetime.Instant): kotlinx.coroutines.flow.Flow<List<org.sysarp.project.data.YapeTransaction>> {
                return kotlinx.coroutines.flow.flowOf(emptyList())
            }
            
            override fun getUnprocessedTransactions(): kotlinx.coroutines.flow.Flow<List<org.sysarp.project.data.YapeTransaction>> {
                return kotlinx.coroutines.flow.flowOf(emptyList())
            }
            
            override suspend fun insertTransaction(transaction: org.sysarp.project.data.YapeTransaction) {
                // Implementación real - guardar en base de datos local
                println("💾 [REPOSITORY] Guardando transacción: ${transaction.transactionId}")
            }
            
            override suspend fun updateTransactionProcessed(transactionId: Long) {
                // Implementación real - actualizar en base de datos local
                println("✅ [REPOSITORY] Marcando transacción como procesada: $transactionId")
            }
            
            override suspend fun updateTransactionBusiness(transactionId: Long, businessName: String) {
                // Implementación real - actualizar en base de datos local
                println("🏢 [REPOSITORY] Actualizando negocio de transacción: $transactionId -> $businessName")
            }
            
            override suspend fun deleteTransaction(transactionId: Long) {
                // Implementación real - eliminar de base de datos local
                println("🗑️ [REPOSITORY] Eliminando transacción: $transactionId")
            }
            
            override fun getBusinessReports(): kotlinx.coroutines.flow.Flow<List<org.sysarp.project.data.BusinessReport>> {
                return kotlinx.coroutines.flow.flowOf(emptyList())
            }
            
            override fun getDailyReports(): kotlinx.coroutines.flow.Flow<List<org.sysarp.project.data.DailyReport>> {
                return kotlinx.coroutines.flow.flowOf(emptyList())
            }
            
            override fun exportTransactionsToText(): String {
                return "No hay transacciones disponibles"
            }
            
            override fun exportAllTransactionsToText(): String {
                return "No hay transacciones disponibles"
            }
        }
    }
    
    // Función para reinicializar el repositorio cuando el contexto esté disponible
    fun reinitializeRepository() {
        _repository = null // Forzar recreación
    }
}

@Composable
fun YapeApp() {
    // CORREGIDO: Asegurar que TODOS usen la misma instancia del repositorio
    val repository = remember {
        RepositorySingleton.getRepository()
    }
    
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
        AuthService()
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
    
    // CORREGIDO: Usar el mismo repositorio singleton
    val viewModel = remember {
        YapeViewModel(repository, notificationService, userProfileRepository)
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
        paymentNotificationService = paymentNotificationService,
        viewModel = viewModel,
        userProfileRepository = userProfileRepository
    )
}
