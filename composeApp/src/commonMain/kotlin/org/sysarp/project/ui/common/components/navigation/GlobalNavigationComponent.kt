package org.sysarp.project.ui.common.components.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Componente de navegación global compartido para toda la aplicación
 * Diseño moderno con iconos en la parte inferior
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalNavigationComponent(
    currentScreen: GlobalNavItem,
    onNavigate: (GlobalNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 200)
        ) + fadeOut(animationSpec = tween(durationMillis = 200))
    ) {
        BottomAppBar(
            modifier = modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 8.dp,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 8.dp, 
                vertical = 8.dp
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Dashboard
                GlobalNavButton(
                    item = GlobalNavItem.Dashboard,
                    isSelected = currentScreen == GlobalNavItem.Dashboard,
                    onClick = { onNavigate(GlobalNavItem.Dashboard) }
                )
                
                // Botón Gestión
                GlobalNavButton(
                    item = GlobalNavItem.Management,
                    isSelected = currentScreen == GlobalNavItem.Management,
                    onClick = { onNavigate(GlobalNavItem.Management) }
                )
                
                // Botón Analytics
                GlobalNavButton(
                    item = GlobalNavItem.Analytics,
                    isSelected = currentScreen == GlobalNavItem.Analytics,
                    onClick = { onNavigate(GlobalNavItem.Analytics) }
                )
                
                // Botón Pagos
                GlobalNavButton(
                    item = GlobalNavItem.Payments,
                    isSelected = currentScreen == GlobalNavItem.Payments,
                    onClick = { onNavigate(GlobalNavItem.Payments) }
                )
                
                // Botón Configuración
                GlobalNavButton(
                    item = GlobalNavItem.Settings,
                    isSelected = currentScreen == GlobalNavItem.Settings,
                    onClick = { onNavigate(GlobalNavItem.Settings) }
                )
            }
        }
    }
}

/**
 * Botón individual de navegación global
 */
@Composable
private fun GlobalNavButton(
    item: GlobalNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) 
                        MaterialTheme.colorScheme.primaryContainer
                    else 
                        Color.Transparent
                )
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
        
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) 
                MaterialTheme.colorScheme.onPrimaryContainer
            else 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Elementos de navegación global disponibles
 * Estos elementos se pueden usar en toda la aplicación
 */
sealed class GlobalNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    object Dashboard : GlobalNavItem(
        title = "Inicio",
        icon = Icons.Filled.Dashboard,
        route = "dashboard"
    )
    
    object Management : GlobalNavItem(
        title = "Gestión",
        icon = Icons.Filled.Business,
        route = "management"
    )
    
    object Analytics : GlobalNavItem(
        title = "Analytics",
        icon = Icons.Filled.Analytics,
        route = "analytics"
    )
    
    object Payments : GlobalNavItem(
        title = "Pagos",
        icon = Icons.Filled.Payments,
        route = "payments"
    )
    
    object Settings : GlobalNavItem(
        title = "Config",
        icon = Icons.Filled.Settings,
        route = "settings"
    )
}

/**
 * Componente de navegación compacto para pantallas específicas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactGlobalNavigationComponent(
    currentScreen: GlobalNavItem,
    onNavigate: (GlobalNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 250)
        ) + fadeIn(animationSpec = tween(durationMillis = 250))
    ) {
        BottomAppBar(
            modifier = modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 4.dp,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 4.dp, 
                vertical = 4.dp
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    GlobalNavItem.Dashboard,
                    GlobalNavItem.Management,
                    GlobalNavItem.Analytics,
                    GlobalNavItem.Payments,
                    GlobalNavItem.Settings
                ).forEach { item ->
                    IconButton(
                        onClick = { onNavigate(item) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (currentScreen == item) 
                                    MaterialTheme.colorScheme.primaryContainer
                                else 
                                    Color.Transparent
                            )
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (currentScreen == item) 
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
