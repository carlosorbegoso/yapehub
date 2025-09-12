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
import org.sysarp.project.navigation.NavigationManager
import org.sysarp.project.navigation.AppContent
import org.sysarp.project.navigation.rememberNavigationManager
import org.sysarp.project.viewmodel.YapeViewModel
import org.sysarp.project.service.AuthService

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
    // CORREGIDO: Asegurar que TODOS usen la misma instancia del repositorio
    val repository = remember {
        RepositorySingleton.getRepository()
    }
    
    val userProfileRepository = remember {
        UserProfileRepository()
    }
    
    val authService = remember {
        AuthService()
    }
    
    val notificationService = remember {
        createNotificationService(repository)
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
        viewModel = viewModel,
        userProfileRepository = userProfileRepository,
        authService = authService
    )
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