package org.sysarp.project.ui.screens.billing

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.sysarp.project.data.*
import org.sysarp.project.service.billing.BillingService
import org.sysarp.project.ui.components.topbar.TopBarComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingDashboardScreen(
    billingService: BillingService,
    onNavigateBack: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToPayment: (PaymentCode) -> Unit
) {
    var billingDashboard by remember { mutableStateOf<BillingDashboard?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var showTokenPurchaseDialog by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    
    // Cargar datos del dashboard
    LaunchedEffect(Unit) {
        try {
            
            val dashboardResult = billingService.getCurrentBillingDashboard()
            dashboardResult.fold(
                onSuccess = { dashboard ->
                    billingDashboard = dashboard
                    isLoading = false
                },
                onFailure = { e ->
                    error = e.message ?: "Error desconocido"
                    isLoading = false
                }
            )
        } catch (e: Exception) {
            error = e.message ?: "Error inesperado"
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopBarComponent(
                title = "Dashboard de Facturación",
                subtitle = billingDashboard?.let { "Última actualización: ${it.lastUpdated}" },
                icon = Icons.Filled.AccountBalanceWallet,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
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
                .padding(paddingValues)
        ) {
            if (isLoading) {
                // Estado de carga mejorado con diseño elegante
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Animación de pulso para el indicador de carga
                            val infiniteTransition = rememberInfiniteTransition(label = "loading")
                            val scale by infiniteTransition.animateFloat(
                                initialValue = 0.9f,
                                targetValue = 1.1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1500, easing = EaseInOut),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scale"
                            )
                            
                            val rotation by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(3000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "rotation"
                            )
                            
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(40.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .scale(scale)
                                        .graphicsLayer { rotationZ = rotation },
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp
                                )
                            }
                            
                            // Animación de fade para el texto
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(1200)) + slideInVertically(
                                    initialOffsetY = { it / 3 },
                                    animationSpec = tween(1200, easing = EaseOutCubic)
                                )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Cargando Dashboard",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Preparando información de facturación...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (error.isNotEmpty()) {
                // Estado de error mejorado con diseño elegante
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Animación de shake para el icono de error
                            val infiniteTransition = rememberInfiniteTransition(label = "error")
                            val shake by infiniteTransition.animateFloat(
                                initialValue = -3f,
                                targetValue = 3f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = EaseInOut),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "shake"
                            )
                            
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(40.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Error,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .graphicsLayer { translationX = shake },
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            
                            // Animación de entrada para el texto
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(
                                    initialOffsetY = { it / 2 },
                                    animationSpec = tween(1000, easing = EaseOutCubic)
                                )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Error al Cargar",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = error,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            
                            // Botón con animación mejorada
                            var buttonPressed by remember { mutableStateOf(false) }
                            val buttonScale by animateFloatAsState(
                                targetValue = if (buttonPressed) 0.95f else 1f,
                                animationSpec = tween(150),
                                label = "buttonScale"
                            )
                            
                            Button(
                                onClick = { 
                                    buttonPressed = true
                                    isLoading = true
                                    error = ""
                                    // Recargar datos
                                },
                                modifier = Modifier.scale(buttonScale),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Reintentar",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                // Contenido principal mejorado
                billingDashboard?.let { dashboard ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Resumen de suscripción con animación
                        item {
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(800, easing = EaseOutCubic)
                                ) + fadeIn(animationSpec = tween(800))
                            ) {
                                SubscriptionSummaryCard(
                                    subscription = dashboard.subscriptionStatus,
                                    onUpgradeClick = onNavigateToSubscriptions
                                )
                            }
                        }
                        
                        // Estado de tokens con animación
                        item {
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInHorizontally(
                                    initialOffsetX = { it },
                                    animationSpec = tween(800, delayMillis = 200, easing = EaseOutCubic)
                                ) + fadeIn(animationSpec = tween(800, delayMillis = 200))
                            ) {
                                TokenStatusCard(
                                    tokenStatus = dashboard.tokenStatus,
                                    onPurchaseTokens = { showTokenPurchaseDialog = true }
                                )
                            }
                        }
                        
                        // Uso mensual con animación
                        item {
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInVertically(
                                    initialOffsetY = { it / 2 },
                                    animationSpec = tween(800, delayMillis = 400, easing = EaseOutCubic)
                                ) + fadeIn(animationSpec = tween(800, delayMillis = 400))
                            ) {
                                TokenUsageCard(
                                    monthlyUsage = dashboard.monthlyUsage
                                )
                            }
                        }
                        
                        // Pagos recientes con animación
                        if (dashboard.recentPayments.isNotEmpty()) {
                            item {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = slideInVertically(
                                        initialOffsetY = { it / 3 },
                                        animationSpec = tween(800, delayMillis = 600, easing = EaseOutCubic)
                                    ) + fadeIn(animationSpec = tween(800, delayMillis = 600))
                                ) {
                                    Text(
                                        text = "Pagos Recientes",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            
                            itemsIndexed(dashboard.recentPayments) { index, payment ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = slideInVertically(
                                        initialOffsetY = { it },
                                        animationSpec = tween(
                                            durationMillis = 600,
                                            delayMillis = 800 + (index * 100),
                                            easing = EaseOutCubic
                                        )
                                    ) + fadeIn(
                                        animationSpec = tween(
                                            durationMillis = 600,
                                            delayMillis = 800 + (index * 100)
                                        )
                                    )
                                ) {
                                    RecentPaymentCard(payment = payment)
                                }
                            }
                        }

                        item {
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInVertically(
                                    initialOffsetY = { it / 2 },
                                    animationSpec = tween(800, delayMillis = 1000, easing = EaseOutCubic)
                                ) + fadeIn(animationSpec = tween(800, delayMillis = 1000))
                            ) {
                                BillingSummaryCard(
                                    billingSummary = dashboard.billingSummary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Animación de éxito
    if (showSuccessAnimation) {
        org.sysarp.project.ui.screens.billing.SuccessAnimationOverlay(
            message = successMessage,
            onAnimationComplete = { showSuccessAnimation = false }
        )
    }
    
    // Diálogo para compra de tokens
    if (showTokenPurchaseDialog) {
        TokenPurchaseDialog(
            billingService = billingService,
            onDismiss = { showTokenPurchaseDialog = false },
            onNavigateToPayment = onNavigateToPayment
        )
    }
}