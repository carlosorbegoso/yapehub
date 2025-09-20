package org.sysarp.project.ui.screens.billing

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.sysarp.project.ui.components.topbar.TopBarComponent

@Composable
fun BillingMainScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToTokens: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopBarComponent(
                title = "Centro de Facturación",
                subtitle = "Gestiona tus suscripciones y tokens",
                icon = Icons.Filled.AccountBalanceWallet,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Fondo con gradiente sutil
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                    )
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header con descripción
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(
                            initialOffsetY = { -it / 2 },
                            animationSpec = tween(1000, easing = EaseOutCubic)
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "¿Qué necesitas gestionar?",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Elige el tipo de servicio que deseas administrar",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                // Opción 1: Planes de Suscripción
                item {
                    BillingOptionCard(
                        title = "Planes de Suscripción",
                        subtitle = "Con límites de vendedores y tokens incluidos",
                        description = "Gestiona tu suscripción mensual o anual con límites específicos de vendedores y tokens incluidos.",
                        icon = Icons.Filled.Subscriptions,
                        color = MaterialTheme.colorScheme.primary,
                        isSelected = selectedOption == "subscriptions",
                        onClick = {
                            selectedOption = "subscriptions"
                            onNavigateToSubscriptions()
                        }
                    )
                }
                
                // Opción 2: Paquetes de Tokens
                item {
                    BillingOptionCard(
                        title = "Paquetes de Tokens",
                        subtitle = "Solo para comprar tokens adicionales",
                        description = "Compra tokens adicionales cuando necesites más capacidad para tus operaciones.",
                        icon = Icons.Filled.Token,
                        color = MaterialTheme.colorScheme.secondary,
                        isSelected = selectedOption == "tokens",
                        onClick = {
                            selectedOption = "tokens"
                            onNavigateToTokens()
                        }
                    )
                }
                
                // Opción 3: Dashboard de Facturación
                item {
                    BillingOptionCard(
                        title = "Dashboard de Facturación",
                        subtitle = "Vista general de tu cuenta",
                        description = "Revisa el estado actual de tus tokens, suscripción y historial de pagos.",
                        icon = Icons.Filled.Dashboard,
                        color = MaterialTheme.colorScheme.tertiary,
                        isSelected = selectedOption == "dashboard",
                        onClick = {
                            selectedOption = "dashboard"
                            onNavigateToDashboard()
                        }
                    )
                }
                
                // Espaciado inferior
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun BillingOptionCard(
    title: String,
    subtitle: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else if (isSelected) 8.dp else 4.dp,
        animationSpec = tween(150),
        label = "elevation"
    )
    
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(800, easing = EaseOutCubic)
        ) + fadeIn(animationSpec = tween(800))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) {
                    color.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            border = if (isSelected) {
                androidx.compose.foundation.BorderStroke(
                    2.dp,
                    color.copy(alpha = 0.5f)
                )
            } else null
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = color.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(12.dp),
                            tint = color
                        )
                    }
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = color,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                )
            }
        }
    }
}
