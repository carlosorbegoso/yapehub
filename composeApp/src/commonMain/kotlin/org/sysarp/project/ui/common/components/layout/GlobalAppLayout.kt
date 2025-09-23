package org.sysarp.project.ui.common.components.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.sysarp.project.ui.common.components.navigation.GlobalNavItem
import org.sysarp.project.ui.common.components.navigation.GlobalNavigationComponent
import org.sysarp.project.ui.components.topbar.TopBarComponent

/**
 * Layout global compartido para toda la aplicación
 * Incluye TopBar y navegación inferior consistente
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalAppLayout(
    currentScreen: GlobalNavItem,
    title: String,
    subtitle: String? = null,
    showTopBar: Boolean = true,
    showBottomNavigation: Boolean = true,
    onNavigate: (GlobalNavItem) -> Unit,
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
                GlobalNavigationComponent(
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
 * Layout compacto global para pantallas específicas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactGlobalAppLayout(
    currentScreen: GlobalNavItem,
    title: String,
    onNavigate: (GlobalNavItem) -> Unit,
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
            org.sysarp.project.ui.common.components.navigation.CompactGlobalNavigationComponent(
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
fun FullScreenGlobalLayout(
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
