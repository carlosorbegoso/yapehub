package org.sysarp.project.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class Screen {
    object Splash : Screen()
    object ProfileSelection : Screen()
    object Login : Screen()
    object ForgotPassword : Screen()
    object AdminRegistration : Screen()
    object SellerLogin : Screen()
    object SellerRegistration : Screen()
    object SellerAffiliation : Screen()
    object AdminDashboard : Screen()
    object SellerDashboard : Screen()
    object SellerManagement : Screen()
    object Analytics : Screen()
    object Reports : Screen()
    object Settings : Screen()
    object PendingPayments : Screen()
    object SellerPayments : Screen()
    object UserManagement : Screen()
    object DeactivationRequest : Screen()
    data class QRDisplay(val qrCode: org.sysarp.project.service.QRCodeData) : Screen()
}

class NavigationManager {
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()
    
    private val _navigationStack = mutableListOf<Screen>()
    
    fun navigateTo(screen: Screen) {
        _navigationStack.add(_currentScreen.value)
        _currentScreen.value = screen
    }
    
    fun navigateBack() {
        if (_navigationStack.isNotEmpty()) {
            _currentScreen.value = _navigationStack.removeLastOrNull() ?: Screen.AdminDashboard
        }
    }
    
    fun navigateToMain() {
        _navigationStack.clear()
        _currentScreen.value = Screen.AdminDashboard
    }
    
    fun navigateToProfileSelection() {
        _navigationStack.clear()
        _currentScreen.value = Screen.ProfileSelection
    }
    
    fun onSplashFinished() {
        _currentScreen.value = Screen.ProfileSelection
    }
    
    fun navigateToAdminRegistration() {
        _currentScreen.value = Screen.AdminRegistration
    }
    
    fun navigateToSellerLogin() {
        _currentScreen.value = Screen.SellerLogin
    }
    
    fun navigateToSellerRegistration() {
        _currentScreen.value = Screen.SellerRegistration
    }
    
    fun navigateToSellerAffiliation() {
        _currentScreen.value = Screen.SellerAffiliation
    }
    
    fun navigateBackToProfileSelection() {
        _currentScreen.value = Screen.ProfileSelection
    }
    
    fun navigateToAdminDashboard() {
        _currentScreen.value = Screen.AdminDashboard
    }
    
    fun navigateToSellerDashboard() {
        _currentScreen.value = Screen.SellerDashboard
    }
    
    fun navigateToSellerManagement() {
        _currentScreen.value = Screen.SellerManagement
    }
    
    fun navigateToAnalytics() {
        _currentScreen.value = Screen.Analytics
    }
    
    fun navigateToQRDisplay(qrCode: org.sysarp.project.service.QRCodeData) {
        navigateTo(Screen.QRDisplay(qrCode))
    }
    
    fun navigateToLogin() {
        _currentScreen.value = Screen.Login
    }
    
    fun navigateToForgotPassword() {
        _currentScreen.value = Screen.ForgotPassword
    }
    
    fun navigateToDeactivationRequest() {
        _currentScreen.value = Screen.DeactivationRequest
    }
    
    fun navigateToSellerPayments() {
        _currentScreen.value = Screen.SellerPayments
    }
    
}

@Composable
fun rememberNavigationManager(): NavigationManager {
    return remember { NavigationManager() }
}
