package org.sysarp.project.ui.common.components.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.ui.common.components.navigation.BottomNavItem
import org.sysarp.project.ui.common.components.navigation.BottomNavigationComponent
import org.sysarp.project.ui.components.topbar.TopBarComponent

/**
 * Layout principal moderno con navegación inferior
 * Diseño inspirado en apps móviles modernas como Instagram, WhatsApp, etc.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernAppLayout(
    currentScreen: BottomNavItem,
    title: String,
    subtitle: String? = null,
    showTopBar: Boolean = true,
    showBottomNavigation: Boolean = true,
    onNavigate: (BottomNavItem) -> Unit,
    onBackClick: (() -> Unit)? = null,
    topBarActions: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            if (showTopBar) {
                TopBarComponent(
                    title = title,
                    subtitle = subtitle,
                    onNavigateBack = onBackClick,
                    actions = topBarActions
                )
            }
        },
        bottomBar = {
            if (showBottomNavigation) {
                BottomNavigationComponent(
                    currentScreen = currentScreen,
                    onNavigate = onNavigate
                )
            }
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            content()
        }
    }
}

/**
 * Layout compacto para pantallas específicas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactAppLayout(
    currentScreen: BottomNavItem,
    title: String,
    onNavigate: (BottomNavItem) -> Unit,
    onBackClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopBarComponent(
                title = title,
                onNavigateBack = onBackClick
            )
        },
        bottomBar = {
            org.sysarp.project.ui.common.components.navigation.CompactBottomNavigationComponent(
                currentScreen = currentScreen,
                onNavigate = onNavigate
            )
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            content()
        }
    }
}

/**
 * Layout sin navegación para pantallas de login, registro, etc.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenLayout(
    title: String? = null,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    topBarActions: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            if (title != null) {
                TopBarComponent(
                    title = title,
                    subtitle = subtitle,
                    onNavigateBack = onBackClick,
                    actions = topBarActions
                )
            }
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            content()
        }
    }
}
