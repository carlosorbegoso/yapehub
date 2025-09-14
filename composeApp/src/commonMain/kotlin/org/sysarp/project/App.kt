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
            // Crear repositorio mock temporalmente
            _repository = createMockRepository()
        }
        return _repository!!
    }
    
    private fun createMockRepository(): YapeTransactionRepository {
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
                // Mock implementation - no hace nada
            }
            
            override suspend fun updateTransactionProcessed(transactionId: Long) {
                // Mock implementation - no hace nada
            }
            
            override suspend fun updateTransactionBusiness(transactionId: Long, businessName: String) {
                // Mock implementation - no hace nada
            }
            
            override suspend fun deleteTransaction(transactionId: Long) {
                // Mock implementation - no hace nada
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
    
    val authService = remember {
        AuthService()
    }
    
    val sellerService = remember {
        SellerService()
    }
    
    // CORREGIDO: Usar el mismo repositorio singleton
    val viewModel = remember {
        YapeViewModel(repository, notificationService, userProfileRepository)
    }
    
    // Sistema de navegación simple multiplataforma
    val navigationManager = rememberNavigationManager()
    
    // Contenido de la aplicación
    AppContent(
        navigationManager = navigationManager,
        authService = authService,
        sellerService = sellerService,
        viewModel = viewModel,
        userProfileRepository = userProfileRepository
    )
}
