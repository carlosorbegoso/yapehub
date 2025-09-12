package org.sysarp.project.navigation

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class Screen {
    object Splash : Screen()
    object Main : Screen()
    object Reports : Screen()
    object Settings : Screen()
    object PendingPayments : Screen()
    object UserManagement : Screen()
    object ProfileSelection : Screen()
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
            _currentScreen.value = _navigationStack.removeLastOrNull() ?: Screen.Main
        }
    }
    
    fun navigateToMain() {
        _navigationStack.clear()
        _currentScreen.value = Screen.Main
    }
    
    fun navigateToProfileSelection() {
        _navigationStack.clear()
        _currentScreen.value = Screen.ProfileSelection
    }
    
    fun onSplashFinished() {
        _currentScreen.value = Screen.ProfileSelection
    }
    
}

@Composable
fun rememberNavigationManager(): NavigationManager {
    return remember { NavigationManager() }
}
