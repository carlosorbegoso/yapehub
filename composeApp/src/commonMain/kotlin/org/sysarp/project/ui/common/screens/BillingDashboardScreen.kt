package org.sysarp.project.ui.common.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.BillingDashboard
import org.sysarp.project.data.PaymentCode
import org.sysarp.project.service.billing.BillingService
import org.sysarp.project.ui.common.components.DateFilterComponent
import org.sysarp.project.ui.common.components.rememberDateFilterState
import org.sysarp.project.ui.common.components.topbar.TopBarComponent

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
    var showSuccessAnimation by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var shouldReload by remember { mutableStateOf(false) }
    
    // Estado del filtro de fechas
    val dateFilterState = rememberDateFilterState()
    
    // Función para cargar datos del dashboard
    suspend fun loadBillingDashboard() {
        try {
            isLoading = true
            error = ""
            
            val dashboardResult = billingService.getCurrentBillingDashboard(
                startDate = dateFilterState.startDate,
                endDate = dateFilterState.endDate
            )
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
    
    // Cargar datos del dashboard inicial
    LaunchedEffect(Unit) {
        loadBillingDashboard()
    }
    
    // Recargar cuando cambien las fechas
    LaunchedEffect(dateFilterState.startDate, dateFilterState.endDate) {
        if (dateFilterState.startDate != null || dateFilterState.endDate != null) {
            loadBillingDashboard()
        }
    }
    
    // Recargar cuando se presione el botón
    LaunchedEffect(shouldReload) {
        if (shouldReload) {
            loadBillingDashboard()
            shouldReload = false
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
            // Filtro de fechas
            DateFilterComponent(
                selectedDateRange = dateFilterState.selectedDateRange,
                onDateRangeSelected = { period ->
                    dateFilterState.onDateRangeSelected(period)
                },
                showCalendar = dateFilterState.showCalendarDialog,
                onShowCalendar = dateFilterState.onShowCalendar,
                onDismissCalendar = dateFilterState.onDismissCalendar,
                title = "Filtrar facturación por fecha",
                description = "Selecciona un período para filtrar los datos de facturación"
            )
            
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
                                    shouldReload = true
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
        org.sysarp.project.ui.common.screens.SuccessAnimationOverlay(
            message = successMessage,
            onAnimationComplete = { showSuccessAnimation = false }
        )
    }
    
}