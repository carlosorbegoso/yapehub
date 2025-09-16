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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import org.sysarp.project.ui.components.cards.SellerProfileCard
import org.sysarp.project.ui.components.cards.ProfessionalStatsCard
import org.sysarp.project.ui.components.cards.PulsingProfessionalStatsCard
import org.sysarp.project.ui.components.cards.PaymentItemCard
import kotlinx.coroutines.launch

/**
 * Tarjeta de estadística para el seller dashboard con estilo admin
 */
@Composable
fun SellerStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Card(
        modifier = modifier,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Tarjeta de pago pendiente para el seller dashboard con estilo admin
 */
@Composable
fun SellerPaymentCard(
    payment: org.sysarp.project.data.SellerPendingPayment,
    onClaim: (Int) -> Unit,
    onReject: (Int) -> Unit,
    isProcessing: Boolean = false,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pago #${payment.paymentId}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                androidx.compose.material3.Card(
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Pendiente",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Información del pago
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SellerInfoRow(
                    label = "Monto",
                    value = "S/ ${String.format("%.2f", payment.amount)}",
                    icon = Icons.Filled.CheckCircle
                )
                
                SellerInfoRow(
                    label = "Cliente",
                    value = payment.senderName,
                    icon = Icons.Filled.Person
                )
                
                SellerInfoRow(
                    label = "Código Yape",
                    value = org.sysarp.project.utils.extractShortYapeCode(payment.yapeCode),
                    icon = Icons.Filled.CheckCircle
                )
                
                SellerInfoRow(
                    label = "Fecha",
                    value = payment.timestamp,
                    icon = Icons.Filled.Schedule
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botones de acción
            if (isProcessing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    androidx.compose.material3.Button(
                        onClick = { onClaim(payment.paymentId) },
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Confirmar")
                    }
                    
                    androidx.compose.material3.Button(
                        onClick = { onReject(payment.paymentId) },
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Rechazar")
                    }
                }
            }
        }
    }
}

/**
 * Fila de información para la tarjeta de pago
 */
@Composable
private fun SellerInfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Contenido principal del dashboard del vendedor usando componentes reutilizables
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
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
            
            // Perfil del vendedor con padding para evitar solapamiento
            item {
                Box(
                    modifier = Modifier.padding(top = 48.dp)
                ) {
                    if (userProfile != null) {
                        SellerProfileCard(
                            sellerId = userProfile?.sellerId?.toInt(),
                            sellerName = userProfile?.sellerName ?: "Vendedor",
                            branchName = userProfile?.branchName ?: "Sucursal Principal",
                            connectionState = connectionState
                        )
                    } else {
                        // Estado de carga para el perfil
                        androidx.compose.material3.Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Cargando perfil...",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Obteniendo información del vendedor",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Resumen de estadísticas con mejor jerarquía visual
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Resumen de Ventas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Estado actual de tus transacciones",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            // Tarjetas de estadísticas usando componentes reutilizables
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isLoadingStats) {
                        // Usar componentes con estilo admin para estado de carga
                        SellerStatCard(
                            title = "Confirmados",
                            value = "...",
                            icon = Icons.Filled.CheckCircle,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                        
                        SellerStatCard(
                            title = "Total Recaudado",
                            value = "...",
                            icon = Icons.Filled.CheckCircle,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Usar componentes con estilo admin (fondo blanco, mismo tamaño)
                        SellerStatCard(
                            title = "Confirmados",
                            value = confirmedPaymentsCount.toString(),
                            icon = Icons.Filled.CheckCircle,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                        
                        SellerStatCard(
                            title = "Total Recaudado",
                            value = "S/ ${String.format("%.0f", totalAmountCollected)}",
                            icon = Icons.Filled.CheckCircle,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Pagos pendientes - Header mejorado con mejor diseño
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Pagos Pendientes",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Transacciones por confirmar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        
                        if (pendingPayments.isNotEmpty()) {
                            androidx.compose.material3.Surface(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${pendingPayments.size}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
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
            
            // Estado vacío siguiendo el estilo admin
            if (displayedPayments.isEmpty() && !isLoadingMore) {
                item {
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Sin pagos pendientes",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(48.dp)
                            )
                            
                            Text(
                                text = "¡Todo al día!",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Text(
                                text = "No tienes pagos pendientes en este momento",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                    SellerPaymentCard(
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
            
            // Botón "Ver más" siguiendo el estilo admin
            if (pendingPayments.size > 2 && !showAllPayments) {
                item {
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        onClick = { showAllPayments = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ver más pagos",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Text(
                                    text = "Mostrar ${pendingPayments.size - 2} pagos adicionales",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            // Botón "Ver menos" siguiendo el estilo admin
            if (showAllPayments && pendingPayments.size > 2) {
                item {
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        onClick = { showAllPayments = false }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ExpandLess,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ver menos",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Text(
                                    text = "Ocultar pagos adicionales",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            
            // Indicador de carga siguiendo el estilo admin
            if (isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            // Botón para cargar más pagos siguiendo el estilo admin
            if (showAllPayments && hasMorePayments && !isLoadingMore) {
                item {
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        onClick = loadMorePayments
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cargar más pagos",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Text(
                                    text = "Ver pagos adicionales",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
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