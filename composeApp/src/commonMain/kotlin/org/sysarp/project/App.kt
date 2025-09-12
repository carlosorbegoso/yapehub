package org.sysarp.project

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.sysarp.project.ui.theme.YapeHubTheme
import org.sysarp.project.data.UserProfile
import org.sysarp.project.repository.UserProfileRepository
import org.sysarp.project.repository.YapeTransactionRepository
import org.sysarp.project.service.NotificationCaptureService
import org.sysarp.project.data.YapeTransaction
import org.sysarp.project.data.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.sysarp.project.ui.screens.HomeScreen
import org.sysarp.project.ui.screens.PendingPaymentsScreen
import org.sysarp.project.ui.screens.ProfileSelectionScreen
import org.sysarp.project.ui.screens.ReportsScreen
import org.sysarp.project.ui.screens.SettingsScreen
import org.sysarp.project.ui.screens.SplashScreen
import org.sysarp.project.ui.screens.UserManagementScreen
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
            // Crear repositorio lazy - solo cuando se necesite
            _repository = createRepository()
        }
        return _repository!!
    }
    
    // Función para reinicializar el repositorio cuando el contexto esté disponible
    fun reinitializeRepository() {
        _repository = null // Forzar recreación
    }
}

@Composable
fun YapeApp() {
    // Usar el singleton del repositorio
    val repository = remember {
        RepositorySingleton.getRepository()
    }
    
    val userProfileRepository = remember {
        UserProfileRepository()
    }
    
    val notificationService = remember {
        createNotificationService(repository)
    }
    
    val viewModel = remember {
        YapeViewModel(repository, notificationService, userProfileRepository)
    }
    
    val currentUser by viewModel.currentUser.collectAsState()
    var currentScreen by remember { mutableStateOf("home") }
    var showPendingPayments by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }
    
    // Mostrar splash screen al inicio
    if (showSplash) {
        SplashScreen(
            onSplashFinished = { 
                showSplash = false
                // Asegurar que se muestre la pantalla de selección de perfil
                currentScreen = "home"
            }
        )
    } else if (currentUser == null) {
        ProfileSelectionScreen(
            userProfileRepository = userProfileRepository,
            onProfileSelected = { user ->
                viewModel.setCurrentUser(user)
                currentScreen = "home"
                // Generar pagos pendientes después de un delay para asegurar que las transacciones estén cargadas
                viewModel.generatePendingPaymentsWithDelay()
            }
        )
    } else {
        when (currentScreen) {
            "home" -> HomeScreen(
                viewModel = viewModel,
                onNavigateToReports = { currentScreen = "reports" },
                onNavigateToSettings = { currentScreen = "settings" },
                onNavigateToPendingPayments = { showPendingPayments = true },
                onLogout = { 
                    viewModel.logout()
                    currentScreen = "home"
                    // No necesitamos limpiar currentUser aquí, 
                    // el ViewModel se encarga de eso
                }
            )
            "reports" -> ReportsScreen(
                viewModel = viewModel,
                onNavigateBack = { currentScreen = "home" }
            )
            "settings" -> SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { currentScreen = "home" },
                onNavigateToUserManagement = { currentScreen = "userManagement" }
            )
            "userManagement" -> UserManagementScreen(
                userProfileRepository = userProfileRepository,
                onNavigateBack = { currentScreen = "settings" }
            )
        }
        
        // Pantalla de pagos pendientes (overlay)
        if (showPendingPayments) {
            val pendingPayments by viewModel.pendingPayments.collectAsState()
            PendingPaymentsScreen(
                pendingPayments = viewModel.getPendingPaymentsForVendor(),
                currentUser = currentUser,
                canConfirmPayment = { payment ->
                    viewModel.canVendorConfirmPayment(payment)
                },
                onConfirmPayment = { payment ->
                    viewModel.confirmPayment(payment)
                },
                onRejectPayment = { payment ->
                    viewModel.rejectPayment(payment)
                },
                onNavigateBack = { showPendingPayments = false }
            )
        }
    }
}

// Función para crear el servicio de notificaciones apropiado para cada plataforma
expect fun createNotificationService(repository: YapeTransactionRepository): NotificationCaptureService

// Función para crear el repositorio apropiado para cada plataforma
expect fun createRepository(): YapeTransactionRepository

// Función para solicitar permisos automáticamente
expect fun requestPermissionsAutomatically()

// Función para verificar permisos de notificaciones
expect suspend fun checkNotificationPermission(): Boolean

// Función para verificar permisos de accesibilidad
expect suspend fun checkAccessibilityPermission(): Boolean