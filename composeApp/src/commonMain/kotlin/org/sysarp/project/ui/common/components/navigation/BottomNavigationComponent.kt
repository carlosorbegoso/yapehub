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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
 * Componente de navegación inferior moderno con iconos
 * Diseño inspirado en las mejores apps móviles modernas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationComponent(
    currentScreen: BottomNavItem,
    onNavigate: (BottomNavItem) -> Unit,
    showFloatingActionButton: Boolean = true,
    onFloatingActionClick: (() -> Unit)? = null,
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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Dashboard
                BottomNavButton(
                    item = BottomNavItem.Dashboard,
                    isSelected = currentScreen == BottomNavItem.Dashboard,
                    onClick = { onNavigate(BottomNavItem.Dashboard) }
                )
                
                // Botón Gestión
                BottomNavButton(
                    item = BottomNavItem.Management,
                    isSelected = currentScreen == BottomNavItem.Management,
                    onClick = { onNavigate(BottomNavItem.Management) }
                )
                
                // Botón Analytics
                BottomNavButton(
                    item = BottomNavItem.Analytics,
                    isSelected = currentScreen == BottomNavItem.Analytics,
                    onClick = { onNavigate(BottomNavItem.Analytics) }
                )
                
                // Botón Pagos
                BottomNavButton(
                    item = BottomNavItem.Payments,
                    isSelected = currentScreen == BottomNavItem.Payments,
                    onClick = { onNavigate(BottomNavItem.Payments) }
                )
                
                // Botón Configuración
                BottomNavButton(
                    item = BottomNavItem.Settings,
                    isSelected = currentScreen == BottomNavItem.Settings,
                    onClick = { onNavigate(BottomNavItem.Settings) }
                )
            }
        }
    }
}

/**
 * Botón individual de navegación inferior
 */
@Composable
private fun BottomNavButton(
    item: BottomNavItem,
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
 * Elementos de navegación inferior disponibles
 */
sealed class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    object Dashboard : BottomNavItem(
        title = "Inicio",
        icon = Icons.Filled.Dashboard,
        route = "dashboard"
    )
    
    object Management : BottomNavItem(
        title = "Gestión",
        icon = Icons.Filled.Business,
        route = "management"
    )
    
    object Analytics : BottomNavItem(
        title = "Analytics",
        icon = Icons.Filled.Analytics,
        route = "analytics"
    )
    
    object Payments : BottomNavItem(
        title = "Pagos",
        icon = Icons.Filled.Payments,
        route = "payments"
    )
    
    object Settings : BottomNavItem(
        title = "Config",
        icon = Icons.Filled.Settings,
        route = "settings"
    )
}

/**
 * Componente de navegación inferior compacto para pantallas específicas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactBottomNavigationComponent(
    currentScreen: BottomNavItem,
    onNavigate: (BottomNavItem) -> Unit,
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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    BottomNavItem.Dashboard,
                    BottomNavItem.Management,
                    BottomNavItem.Analytics,
                    BottomNavItem.Payments,
                    BottomNavItem.Settings
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
