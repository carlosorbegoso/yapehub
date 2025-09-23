package org.sysarp.project.ui.common.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sysarp.project.data.PaymentCode
import org.sysarp.project.data.SubscriptionPlan
import org.sysarp.project.data.SubscriptionStatus
import org.sysarp.project.service.billing.BillingService
import org.sysarp.project.ui.components.ErrorAlertDialog
import org.sysarp.project.ui.components.ErrorType
import org.sysarp.project.ui.common.components.topbar.TopBarComponent
import org.sysarp.project.utils.ErrorInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    billingService: BillingService,
    onNavigateBack: () -> Unit,
    onNavigateToPayment: (PaymentCode) -> Unit
) {
    var currentSubscription by remember { mutableStateOf<SubscriptionStatus?>(null) }
    var availablePlans by remember { mutableStateOf<List<SubscriptionPlan>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var subscriptionError by remember { mutableStateOf("") }
    var plansError by remember { mutableStateOf("") }
    var showTokenPurchaseDialog by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    
    // Estados para manejo elegante de errores
    var showErrorDialog by remember { mutableStateOf(false) }
    var currentError by remember { mutableStateOf<ErrorInfo?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        try {
            
            // Cargar suscripción actual
            val subscriptionResult = billingService.getCurrentSubscription()
            subscriptionResult.fold(
                onSuccess = { subscription ->
                    currentSubscription = subscription
                },
                onFailure = { e ->
                    subscriptionError = e.message ?: "Error cargando suscripción"
                }
            )
            
            // Cargar planes disponibles con manejo elegante de errores
            val (plans, errorInfo) = billingService.getAvailablePlansWithErrorHandling()
            if (plans != null) {
                availablePlans = plans
            } else if (errorInfo != null) {
                availablePlans = billingService.getAvailablePlansLocal()
                
                // Mostrar error elegante si es crítico
                if (errorInfo.type == ErrorType.NETWORK || errorInfo.type == ErrorType.SERVER) {
                    currentError = errorInfo
                    showErrorDialog = true
                }
            }
            
            isLoading = false
        } catch (e: Exception) {
            availablePlans = billingService.getAvailablePlansLocal()
            
            // Mostrar error elegante para errores críticos
            val errorInfo = org.sysarp.project.utils.ErrorManager.parseException(e)
            if (errorInfo.type == ErrorType.NETWORK || errorInfo.type == ErrorType.SERVER) {
                currentError = errorInfo
                showErrorDialog = true
            }
            
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopBarComponent(
                title = "Planes de Suscripción",
                icon = Icons.Filled.Subscriptions,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                // Estado de carga con animaciones
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Animación de pulso para el indicador de carga
                        val infiniteTransition = rememberInfiniteTransition(label = "loading")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.8f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = EaseInOut),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale"
                        )
                        
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(48.dp)
                                .scale(scale),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp
                        )
                        
                        // Animación de fade para el texto
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(800)) + slideInVertically(
                                initialOffsetY = { it / 4 },
                                animationSpec = tween(800, easing = EaseOutCubic)
                            )
                        ) {
                            Text(
                                text = "Cargando planes...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Suscripción actual
                    currentSubscription?.let { subscription ->
                        item {
                            CurrentSubscriptionCard(subscription = subscription)
                        }
                    }
                    
                    // Título de planes disponibles
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(800, delayMillis = 200, easing = EaseOutCubic)
                            ) + fadeIn(animationSpec = tween(800, delayMillis = 200))
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Planes Disponibles",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Cargados: ${availablePlans.size} planes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Planes disponibles con animaciones escalonadas
                    itemsIndexed(availablePlans) { index, plan ->
                        // Animación escalonada para las tarjetas
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(
                                    durationMillis = 600,
                                    delayMillis = index * 100,
                                    easing = EaseOutCubic
                                )
                            ) + fadeIn(
                                animationSpec = tween(
                                    durationMillis = 600,
                                    delayMillis = index * 100
                                )
                            )
                        ) {
                            SubscriptionPlanCard(
                                plan = plan,
                                currentPlanId = currentSubscription?.let { 
                                    // Determinar ID del plan actual basado en el nombre
                                    val planId = when (it.planName.lowercase()) {
                                        "plan gratuito", "gratuito", "free" -> 1
                                        "plan básico", "básico", "basic" -> 2
                                        "plan profesional", "profesional", "professional" -> 3
                                        "plan empresarial", "empresarial", "enterprise" -> 4
                                        else -> {
                                            null
                                        }
                                    }
                                    planId
                                },
                                onSelectPlan = { selectedPlan ->
                                    coroutineScope.launch {
                                        try {
                                            
                                            val paymentResult = billingService.generateSubscriptionPayment(selectedPlan.id)
                                            paymentResult.fold(
                                                onSuccess = { paymentCode ->
                                                    // Mostrar animación de éxito
                                                    successMessage = "¡Código de pago generado exitosamente!"
                                                    showSuccessAnimation = true
                                                    
                                                    // Navegar después de mostrar la animación
                                                    coroutineScope.launch {
                                                        delay(2000) // Mostrar animación por 2 segundos
                                                        onNavigateToPayment(paymentCode)
                                                        showSuccessAnimation = false
                                                    }
                                                    
                                                },
                                                onFailure = { e ->
                                                    // Mostrar error en un snackbar o dialog
                                                }
                                            )
                                        } catch (e: Exception) {
                                            // Mostrar error en un snackbar o dialog
                                        }
                                    }
                                }
                            )
                        }
                    }
                    
                    // Tarjeta de compra de tokens
                    item {
                        TokenPurchaseCard(
                            onPurchaseTokens = { showTokenPurchaseDialog = true }
                        )
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
    
    // Diálogo para compra de tokens
    if (showTokenPurchaseDialog) {
        TokenPurchaseDialog(
            billingService = billingService,
            onDismiss = { showTokenPurchaseDialog = false },
            onNavigateToPayment = onNavigateToPayment
        )
    }
    
    // Diálogo de error elegante
    currentError?.let { errorInfo ->
        ErrorAlertDialog(
            isVisible = showErrorDialog,
            errorType = errorInfo.type,
            title = errorInfo.title,
            message = errorInfo.message,
            details = errorInfo.details,
            onDismiss = { 
                showErrorDialog = false
                currentError = null
            },
            onRetry = if (errorInfo.canRetry) {
                {
                    // Recargar datos
                    coroutineScope.launch {
                        isLoading = true
                        try {
                            val (plans, newErrorInfo) = billingService.getAvailablePlansWithErrorHandling()
                            if (plans != null) {
                                availablePlans = plans
                            } else if (newErrorInfo != null) {
                                availablePlans = billingService.getAvailablePlansLocal()
                                currentError = newErrorInfo
                                showErrorDialog = true
                            }
                        } catch (e: Exception) {
                            availablePlans = billingService.getAvailablePlansLocal()
                            val errorInfo = org.sysarp.project.utils.ErrorManager.parseException(e)
                            currentError = errorInfo
                            showErrorDialog = true
                        }
                        isLoading = false
                    }
                }
            } else null
        )
    }
}