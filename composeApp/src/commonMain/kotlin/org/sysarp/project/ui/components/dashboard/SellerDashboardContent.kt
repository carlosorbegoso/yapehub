package org.sysarp.project.ui.components.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.PaymentNotificationData
import org.sysarp.project.data.PaymentResultData
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.notifications.PaymentNotificationService
import org.sysarp.project.service.websocket.PaymentWebSocketService
import org.sysarp.project.service.websocket.WebSocketConnectionState
import org.sysarp.project.ui.components.PaymentNotificationCard
import org.sysarp.project.ui.components.cards.PulsingStatsCard
import org.sysarp.project.ui.components.cards.SellerProfileCard
import org.sysarp.project.ui.components.cards.ProfessionalStatsCard
import org.sysarp.project.ui.components.cards.PulsingProfessionalStatsCard
import org.sysarp.project.ui.components.cards.PaymentItemCard
import org.sysarp.project.utils.extractShortYapeCode
import kotlinx.coroutines.launch

/**
 * Contenido principal del dashboard del vendedor
 */
@Composable
fun SellerDashboardContent(
    authService: AuthService,
    webSocketService: PaymentWebSocketService,
    notificationService: PaymentNotificationService,
    paymentManager: SellerPaymentManager,
    statsManager: SellerStatsManager,
    onNavigateToHistory: () -> Unit,
    onNavigateToPendingPayments: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDeactivationRequest: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Estados del WebSocket
    val connectionState by webSocketService.connectionState.collectAsState()
    val isConnected by webSocketService.isConnected.collectAsState()
    
    // Estados de notificaciones
    var currentNotification by remember { mutableStateOf<PaymentNotificationData?>(null) }
    var currentResultNotification by remember { mutableStateOf<PaymentResultData?>(null) }
    var newPaymentsCount by remember { mutableStateOf(0) }
    var showSuccessMessage by remember { mutableStateOf("") }
    
    // Estados de datos
    var pendingPayments by remember { mutableStateOf<List<SellerPendingPayment>>(emptyList()) }
    var confirmedPaymentsCount by remember { mutableStateOf(0) }
    var totalAmountCollected by remember { mutableStateOf(0.0) }
    var isLoadingStats by remember { mutableStateOf(false) }
    var isLoadingMorePayments by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(0) }
    var hasMorePayments by remember { mutableStateOf(true) }
    var processingPayments by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showAllPayments by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Cargar datos iniciales
    LaunchedEffect(accessToken, userProfile?.sellerId) {
        val token = accessToken
        val profile = userProfile
        if (token != null && profile?.sellerId != null) {
            // Cargar pagos pendientes
            paymentManager.loadPendingPayments(
                accessToken = token,
                sellerId = profile.sellerId.toLong(),
                onSuccess = { payments ->
                    pendingPayments = payments
                },
                onError = { error ->
                    showSuccessMessage = "Error: $error"
                }
            )
            
            // Cargar estadísticas
            isLoadingStats = true
            statsManager.loadSellerStats(
                accessToken = token,
                sellerId = profile.sellerId.toLong(),
                onSuccess = { count, amount ->
                    confirmedPaymentsCount = count
                    totalAmountCollected = amount
                    isLoadingStats = false
                },
                onError = { error ->
                    showSuccessMessage = "Error: $error"
                    isLoadingStats = false
                }
            )
        }
    }
    
    // Escuchar notificaciones WebSocket
    LaunchedEffect(webSocketService) {
        coroutineScope.launch {
            webSocketService.paymentNotifications.collect { notification ->
                currentNotification = notification
                newPaymentsCount++
                
                // Procesar notificación
                notificationService.processNewPayment(notification)
                
                // Recargar datos
                val token = accessToken
                val profile = userProfile
                if (token != null && profile?.sellerId != null) {
                    paymentManager.loadPendingPayments(
                        accessToken = token,
                        sellerId = profile.sellerId.toLong(),
                        onSuccess = { payments ->
                            pendingPayments = payments
                        },
                        onError = { }
                    )
                    
                    statsManager.loadSellerStats(
                        accessToken = token,
                        sellerId = profile.sellerId.toLong(),
                        onSuccess = { count, amount ->
                            confirmedPaymentsCount = count
                            totalAmountCollected = amount
                        },
                        onError = { }
                    )
                }
            }
        }
        
        coroutineScope.launch {
            webSocketService.paymentResults.collect { result ->
                currentResultNotification = result
                
                // Procesar resultado
                notificationService.processPaymentResult(result)
                
                // Recargar datos
                val token = accessToken
                val profile = userProfile
                if (token != null && profile?.sellerId != null) {
                    paymentManager.loadPendingPayments(
                        accessToken = token,
                        sellerId = profile.sellerId.toLong(),
                        onSuccess = { payments ->
                            pendingPayments = payments
                        },
                        onError = { }
                    )
                    
                    statsManager.loadSellerStats(
                        accessToken = token,
                        sellerId = profile.sellerId.toLong(),
                        onSuccess = { count, amount ->
                            confirmedPaymentsCount = count
                            totalAmountCollected = amount
                        },
                        onError = { }
                    )
                }
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 8.dp), // Padding adicional para evitar superposición con TopAppBar
            verticalArrangement = Arrangement.spacedBy(12.dp) // Reducido para móvil
        ) {
            // Notificación de nuevo pago
            currentNotification?.let { notification ->
                item {
                    PaymentNotificationCard(
                        notification = notification,
                        onDismiss = { 
                            currentNotification = null
                            newPaymentsCount = 0
                        },
                        onClaim = {
                            val token = accessToken
                            val profile = userProfile
                            if (token != null && profile?.sellerId != null) {
                                processingPayments = processingPayments + (currentNotification?.paymentId ?: 0)
                                coroutineScope.launch {
                                    paymentManager.claimPayment(
                                        accessToken = token,
                                        paymentId = (currentNotification?.paymentId ?: 0),
                                        sellerId = profile.sellerId.toLong(),
                                        onSuccess = { message ->
                                            showSuccessMessage = message
                                            pendingPayments = pendingPayments.filter { it.paymentId != (currentNotification?.paymentId ?: 0) }
                                            processingPayments = processingPayments - (currentNotification?.paymentId ?: 0)
                                        },
                                        onError = { error ->
                                            showSuccessMessage = "Error: $error"
                                            processingPayments = processingPayments - (currentNotification?.paymentId ?: 0)
                                        }
                                    )
                                }
                            }
                        },
                        onReject = {
                            val token = accessToken
                            val profile = userProfile
                            if (token != null && profile?.sellerId != null) {
                                processingPayments = processingPayments + (currentNotification?.paymentId ?: 0)
                                coroutineScope.launch {
                                    paymentManager.rejectPayment(
                                        accessToken = token,
                                        paymentId = (currentNotification?.paymentId ?: 0),
                                        sellerId = profile.sellerId.toLong(),
                                        reason = "No es mío",
                                        onSuccess = { message ->
                                            showSuccessMessage = message
                                            pendingPayments = pendingPayments.filter { it.paymentId != (currentNotification?.paymentId ?: 0) }
                                            processingPayments = processingPayments - (currentNotification?.paymentId ?: 0)
                                        },
                                        onError = { error ->
                                            showSuccessMessage = "Error: $error"
                                            processingPayments = processingPayments - (currentNotification?.paymentId ?: 0)
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            }
            
            // Perfil del vendedor con padding adicional para evitar superposición
            item {
                Box(
                    modifier = Modifier.padding(top = 4.dp) // Padding adicional para separar del TopAppBar
                ) {
                    SellerProfileCard(
                        sellerId = userProfile?.sellerId?.toInt(),
                        sellerName = userProfile?.sellerName,
                        branchName = userProfile?.branchName,
                        connectionState = connectionState
                    )
                }
            }
            
            // Estadísticas del día
            item {
                Text(
                    text = "Estadísticas del Día",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Tarjetas de estadísticas
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // Menos espacio entre tarjetas
                ) {
                    if (isLoadingStats) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        PulsingProfessionalStatsCard(
                            title = "Confirmados",
                            value = confirmedPaymentsCount.toString(),
                            icon = Icons.Filled.CheckCircle,
                            iconColor = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                            backgroundColor = androidx.compose.ui.graphics.Color(0xFFE8F5E8),
                            modifier = Modifier.weight(1f)
                        )
                        
                        ProfessionalStatsCard(
                            title = "Total",
                            value = "S/ ${String.format("%.0f", totalAmountCollected)}",
                            icon = Icons.Filled.AttachMoney,
                            iconColor = androidx.compose.ui.graphics.Color(0xFF2196F3),
                            backgroundColor = androidx.compose.ui.graphics.Color(0xFFE3F2FD),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Pagos pendientes - Header compacto para móvil
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp), // Menos padding vertical
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pagos Pendientes",
                        style = MaterialTheme.typography.titleMedium, // Tamaño reducido
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (pendingPayments.size > 2) {
                        androidx.compose.material3.Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${pendingPayments.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            
            // Lista de pagos pendientes con paginación inteligente para móvil
            val displayedPayments = if (showAllPayments) {
                pendingPayments
            } else {
                // Mostrar máximo 2 pagos en móvil para optimizar espacio
                pendingPayments.take(2)
            }
            
            // Función para cargar más pagos si es necesario
            val loadMorePayments = {
                if (!isLoadingMore && hasMorePayments && showAllPayments) {
                    isLoadingMore = true
                    val token = accessToken
                    val profile = userProfile
                    if (token != null && profile?.sellerId != null) {
                        coroutineScope.launch {
                            paymentManager.loadMorePendingPayments(
                                accessToken = token,
                                sellerId = profile.sellerId.toLong(),
                                currentPage = currentPage + 1,
                                onSuccess = { newPayments ->
                                    pendingPayments = pendingPayments + newPayments
                                    currentPage++
                                    isLoadingMore = false
                                },
                                onError = { error ->
                                    showSuccessMessage = "Error cargando más pagos: $error"
                                    isLoadingMore = false
                                }
                            )
                        }
                    }
                }
            }
            
            items(
                items = displayedPayments,
                key = { it.paymentId }
            ) { payment ->
                AnimatedVisibility(
                    visible = !processingPayments.contains(payment.paymentId),
                    enter = slideInVertically(
                        initialOffsetY = { -it / 2 },
                        animationSpec = androidx.compose.animation.core.tween(300, easing = androidx.compose.animation.core.EaseOutCubic)
                    ) + fadeIn(
                        animationSpec = androidx.compose.animation.core.tween(300)
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { -it / 2 },
                        animationSpec = androidx.compose.animation.core.tween(250, easing = androidx.compose.animation.core.EaseInCubic)
                    ) + fadeOut(
                        animationSpec = androidx.compose.animation.core.tween(250)
                    )
                ) {
                    PaymentItemCard(
                        payment = payment,
                        isProcessing = processingPayments.contains(payment.paymentId),
                        onClaim = {
                            val token = accessToken
                            val profile = userProfile
                            if (token != null && profile?.sellerId != null) {
                                processingPayments = processingPayments + payment.paymentId
                                coroutineScope.launch {
                                    paymentManager.claimPayment(
                                        accessToken = token,
                                        paymentId = payment.paymentId,
                                        sellerId = profile.sellerId.toLong(),
                                        onSuccess = { message ->
                                            showSuccessMessage = message
                                            pendingPayments = pendingPayments.filter { it.paymentId != payment.paymentId }
                                            processingPayments = processingPayments - payment.paymentId
                                        },
                                        onError = { error ->
                                            showSuccessMessage = "Error: $error"
                                            processingPayments = processingPayments - payment.paymentId
                                        }
                                    )
                                }
                            }
                        },
                        onReject = {
                            val token = accessToken
                            val profile = userProfile
                            if (token != null && profile?.sellerId != null) {
                                processingPayments = processingPayments + payment.paymentId
                                coroutineScope.launch {
                                    paymentManager.rejectPayment(
                                        accessToken = token,
                                        paymentId = payment.paymentId,
                                        sellerId = profile.sellerId.toLong(),
                                        reason = "No es mío",
                                        onSuccess = { message ->
                                            showSuccessMessage = message
                                            pendingPayments = pendingPayments.filter { it.paymentId != payment.paymentId }
                                            processingPayments = processingPayments - payment.paymentId
                                        },
                                        onError = { error ->
                                            showSuccessMessage = "Error: $error"
                                            processingPayments = processingPayments - payment.paymentId
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            }
            
            // Botón "Ver más" compacto para móvil
            if (pendingPayments.size > 2 && !showAllPayments) {
                item {
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        onClick = { showAllPayments = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ver ${pendingPayments.size - 2} pagos más",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // Botón "Ver menos" compacto
            if (showAllPayments && pendingPayments.size > 2) {
                item {
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        onClick = { showAllPayments = false }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ExpandLess,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ver menos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Indicador de carga para más pagos
            if (isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Cargando más pagos...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Botón para cargar más pagos si hay más disponibles
            if (showAllPayments && hasMorePayments && !isLoadingMore) {
                item {
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        onClick = loadMorePayments
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cargar más pagos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // Botones de acción
            item {
                SellerDashboardActions(
                    onNavigateToHistory = onNavigateToHistory,
                    onNavigateToPendingPayments = onNavigateToPendingPayments,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToDeactivationRequest = onNavigateToDeactivationRequest
                )
            }
        }
    }
    
    // Mostrar mensaje de éxito
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(showSuccessMessage)
            showSuccessMessage = ""
        }
    }
}
