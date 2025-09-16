package org.sysarp.project.ui.components.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
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
    
    // Nuevas funcionalidades
    var isRefreshing by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Todos") }
    var isSearchActive by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Función utilitaria para mejorar mensajes de error
    fun getImprovedErrorMessage(error: String): String {
        return when {
            error.contains("ya fue procesado") -> "Este pago ya fue procesado anteriormente"
            error.contains("Invalid paymentId") -> "El pago no es válido o ya fue procesado"
            error.contains("INVALID_FIELD") -> "Error en los datos del pago"
            error.contains("El pago ya fue procesado") -> "Este pago ya fue procesado anteriormente"
            else -> "Error al procesar el pago: $error"
        }
    }
    
    // Función para refrescar datos
    val refreshData = {
        isRefreshing = true
        val token = accessToken
        val profile = userProfile
        if (token != null && profile?.sellerId != null) {
            coroutineScope.launch {
                // Recargar pagos pendientes
                paymentManager.loadPendingPayments(
                    accessToken = token,
                    sellerId = profile.sellerId.toLong(),
                    onSuccess = { payments ->
                        pendingPayments = payments
                        isRefreshing = false
                    },
                    onError = { error ->
                        showSuccessMessage = getImprovedErrorMessage(error)
                        isRefreshing = false
                    }
                )
                
                // Recargar estadísticas
                statsManager.loadSellerStats(
                    accessToken = token,
                    sellerId = profile.sellerId.toLong(),
                    onSuccess = { count, amount ->
                        confirmedPaymentsCount = count
                        totalAmountCollected = amount
                    },
                    onError = { error ->
                        showSuccessMessage = getImprovedErrorMessage(error)
                    }
                )
            }
        }
    }
    
    // Filtrar pagos pendientes
    val filteredPayments = remember(pendingPayments, searchQuery, selectedFilter) {
        var filtered = pendingPayments
        
        // Filtrar por búsqueda
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter { payment ->
                payment.senderName.contains(searchQuery, ignoreCase = true) ||
                payment.yapeCode.contains(searchQuery, ignoreCase = true) ||
                payment.paymentId.toString().contains(searchQuery, ignoreCase = true)
            }
        }
        
        // Filtrar por tipo
        when (selectedFilter) {
            "Monto Alto" -> filtered = filtered.filter { it.amount > 50.0 }
            "Monto Bajo" -> filtered = filtered.filter { it.amount <= 50.0 }
            "Recientes" -> filtered = filtered.sortedByDescending { it.timestamp }.take(5)
        }
        
        filtered
    }
    
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
                                            showSuccessMessage = "✅ Pago confirmado exitosamente - S/ ${String.format("%.2f", currentNotification?.amount ?: 0.0)}"
                                            pendingPayments = pendingPayments.filter { it.paymentId != (currentNotification?.paymentId ?: 0) }
                                            processingPayments = processingPayments - (currentNotification?.paymentId ?: 0)
                                        },
                                        onError = { error ->
                                            showSuccessMessage = getImprovedErrorMessage(error)
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
                                            showSuccessMessage = "❌ Pago rechazado exitosamente - S/ ${String.format("%.2f", currentNotification?.amount ?: 0.0)}"
                                            pendingPayments = pendingPayments.filter { it.paymentId != (currentNotification?.paymentId ?: 0) }
                                            processingPayments = processingPayments - (currentNotification?.paymentId ?: 0)
                                        },
                                        onError = { error ->
                                            showSuccessMessage = getImprovedErrorMessage(error)
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
                    modifier = Modifier.padding(top = 64.dp)
                ) {
                    if (userProfile != null) {
                        SellerProfileCard(
                            sellerId = userProfile?.sellerId?.toInt(),
                            sellerName = userProfile?.sellerName ?: "Vendedor",
                            branchName = userProfile?.branchName ?: "Sucursal Principal",
                            branchCode = userProfile?.branchCode,
                            affiliationCode = userProfile?.affiliationCode,
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
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Botón de refresh
                            androidx.compose.material3.IconButton(
                                onClick = refreshData,
                                modifier = Modifier.size(40.dp)
                            ) {
                                AnimatedVisibility(
                                    visible = isRefreshing,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 2.dp
                                    )
                                }
                                
                                AnimatedVisibility(
                                    visible = !isRefreshing,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Actualizar",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
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
                                            text = "${filteredPayments.size}",
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
            }
            
            // Barra de búsqueda y filtros
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Barra de búsqueda
                    androidx.compose.material3.Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            androidx.compose.material3.TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = "Buscar por cliente, código Yape o ID...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                            )
                            
                            // Botón de filtros
                            androidx.compose.material3.IconButton(
                                onClick = { showFilters = !showFilters }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FilterList,
                                    contentDescription = "Filtros",
                                    tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    
                    // Filtros desplegables
                    AnimatedVisibility(
                        visible = showFilters,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        androidx.compose.material3.Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Filtrar por:",
                                style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Todos", "Monto Alto", "Monto Bajo", "Recientes").forEach { filter ->
                                        androidx.compose.material3.FilterChip(
                                            onClick = { selectedFilter = filter },
                                            label = { Text(filter) },
                                            selected = selectedFilter == filter,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Lista de pagos pendientes con paginación inteligente para móvil
            val displayedPayments = if (showAllPayments) {
                filteredPayments
            } else {
                // Mostrar máximo 2 pagos en móvil para optimizar espacio
                filteredPayments.take(2)
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
            
            // Estado vacío mejorado con animaciones
            if (displayedPayments.isEmpty() && !isLoadingMore) {
                item {
                    val animatedScale by animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                        ),
                        label = "emptyStateScale"
                    )
                    
                    androidx.compose.material3.Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .scale(animatedScale),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Icono animado
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInVertically(
                                    initialOffsetY = { -it },
                                    animationSpec = spring(
                                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                                    )
                                ) + fadeIn(animationSpec = tween(800))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = androidx.compose.foundation.shape.CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Sin pagos pendientes",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                            
                            // Texto principal animado
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInVertically(
                                    initialOffsetY = { it / 2 },
                                    animationSpec = spring(
                                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                                    )
                                ) + fadeIn(animationSpec = tween(1000))
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                            Text(
                                        text = if (searchQuery.isNotEmpty() || selectedFilter != "Todos") {
                                            "No se encontraron resultados"
                                        } else {
                                            "¡Todo al día!"
                                        },
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    
                            Text(
                                        text = if (searchQuery.isNotEmpty() || selectedFilter != "Todos") {
                                            "Intenta ajustar los filtros o la búsqueda"
                                        } else {
                                            "No tienes pagos pendientes en este momento"
                                        },
                                style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                                }
                            }
                            
                            // Botón de acción si hay filtros activos
                            if (searchQuery.isNotEmpty() || selectedFilter != "Todos") {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = slideInVertically(
                                        initialOffsetY = { it },
                                        animationSpec = spring(
                                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                                        )
                                    ) + fadeIn(animationSpec = tween(1200))
                                ) {
                                    androidx.compose.material3.Button(
                                        onClick = {
                                            searchQuery = ""
                                            selectedFilter = "Todos"
                                            showFilters = false
                                        },
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Limpiar filtros")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            items(
                items = displayedPayments,
                key = { it.paymentId }
            ) { payment ->
                val animatedScale by animateFloatAsState(
                    targetValue = if (processingPayments.contains(payment.paymentId)) 0.95f else 1f,
                    animationSpec = spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                    ),
                    label = "paymentCardScale"
                )
                
                val animatedAlpha by animateFloatAsState(
                    targetValue = if (processingPayments.contains(payment.paymentId)) 0.7f else 1f,
                    animationSpec = tween(200),
                    label = "paymentCardAlpha"
                )
                
                AnimatedVisibility(
                    visible = !processingPayments.contains(payment.paymentId),
                    enter = slideInVertically(
                        initialOffsetY = { -it / 2 },
                        animationSpec = spring(
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                        )
                    ) + fadeIn(
                        animationSpec = tween(400)
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { -it / 2 },
                        animationSpec = spring(
                            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                        )
                    ) + fadeOut(
                        animationSpec = tween(300)
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
                                            showSuccessMessage = "✅ Pago confirmado exitosamente - S/ ${String.format("%.2f", payment.amount)}"
                                            pendingPayments = pendingPayments.filter { it.paymentId != payment.paymentId }
                                            processingPayments = processingPayments - payment.paymentId
                                        },
                                        onError = { error ->
                                            showSuccessMessage = getImprovedErrorMessage(error)
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
                                            showSuccessMessage = "❌ Pago rechazado exitosamente - S/ ${String.format("%.2f", payment.amount)}"
                                            pendingPayments = pendingPayments.filter { it.paymentId != payment.paymentId }
                                            processingPayments = processingPayments - payment.paymentId
                                        },
                                        onError = { error ->
                                            showSuccessMessage = getImprovedErrorMessage(error)
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