package org.sysarp.project.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.payment.PaymentService
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import org.sysarp.project.utils.extractShortYapeCode
import org.sysarp.project.utils.Logger
import org.sysarp.project.service.websocket.PaymentWebSocketService
import org.sysarp.project.service.websocket.WebSocketConnectionState
import org.sysarp.project.service.notifications.PaymentNotificationService
import org.sysarp.project.ui.components.WebSocketStatusIndicator
import org.sysarp.project.ui.components.PaymentNotificationCard
import org.sysarp.project.data.PaymentNotificationData
import org.sysarp.project.data.PaymentResultData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    authService: AuthService,
    paymentService: PaymentService,
    statsService: org.sysarp.project.service.stats.StatsService,
    webSocketService: PaymentWebSocketService,
    notificationService: PaymentNotificationService,
    onNavigateToHistory: () -> Unit,
    onNavigateToPendingPayments: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDeactivationRequest: () -> Unit,
    onLogout: () -> Unit
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
    
    var pendingPayments by remember { mutableStateOf<List<org.sysarp.project.data.SellerPendingPayment>>(emptyList()) }
    var confirmedPaymentsCount by remember { mutableStateOf(0) }
    var totalAmountCollected by remember { mutableStateOf(0.0) }
    var isLoadingStats by remember { mutableStateOf(false) }
    var isLoadingMorePayments by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(0) }
    var hasMorePayments by remember { mutableStateOf(true) }
    var processingPayments by remember { mutableStateOf<Set<Int>>(emptySet()) }
    
    // Función para cargar pagos pendientes (primera carga)
    val loadPendingPayments = {
        if (accessToken != null && userProfile?.sellerId != null) {
            coroutineScope.launch {
                currentPage = 0
                hasMorePayments = true
                paymentService.getPendingPayments(
                    sellerId = userProfile!!.sellerId!!.toInt(),
                    page = 0,
                    limit = 3, // Solo los primeros 3 para el dashboard (prueba)
                    token = accessToken!!
                ).fold(
                    onSuccess = { response ->
                        val allPayments = response.data.payments.filter { it.status == "PENDING" }
                        // Limitar a 3 pagos en el cliente si el servidor no respeta el límite
                        pendingPayments = allPayments.take(3)
                        hasMorePayments = response.data.pagination.currentPage < response.data.pagination.totalPages - 1
                        println("🔍 [SELLER_DASHBOARD] Pagos pendientes cargados: ${pendingPayments.size} (de ${allPayments.size} disponibles)")
                    },
                    onFailure = { error ->
                        println("🔍 [SELLER_DASHBOARD] Error cargando pagos pendientes: ${error.message}")
                        pendingPayments = emptyList()
                    }
                )
            }
        }
    }
    
    // Función para cargar más pagos pendientes
    val loadMorePendingPayments = {
        if (accessToken != null && userProfile?.sellerId != null && hasMorePayments && !isLoadingMorePayments) {
            coroutineScope.launch {
                isLoadingMorePayments = true
                val nextPage = currentPage + 1
                paymentService.getPendingPayments(
                    sellerId = userProfile!!.sellerId!!.toInt(),
                    page = nextPage,
                    limit = 3, // Solo 3 más para la prueba
                    token = accessToken!!
                ).fold(
                    onSuccess = { response ->
                        val allNewPayments = response.data.payments.filter { it.status == "PENDING" }
                        // Limitar a 3 pagos más en el cliente si el servidor no respeta el límite
                        val newPayments = allNewPayments.take(3)
                        pendingPayments = pendingPayments + newPayments
                        currentPage = nextPage
                        hasMorePayments = response.data.pagination.currentPage < response.data.pagination.totalPages - 1
                        isLoadingMorePayments = false
                        println("🔍 [SELLER_DASHBOARD] Más pagos cargados: ${newPayments.size} (de ${allNewPayments.size} disponibles), total: ${pendingPayments.size}")
                    },
                    onFailure = { error ->
                        println("🔍 [SELLER_DASHBOARD] Error cargando más pagos: ${error.message}")
                        isLoadingMorePayments = false
                    }
                )
            }
        }
    }
    
    // Función para cargar estadísticas reales
    val loadSellerStats = {
        println("🔍 [SELLER_DASHBOARD] Intentando cargar estadísticas...")
        println("🔍 [SELLER_DASHBOARD] accessToken: ${accessToken != null}")
        println("🔍 [SELLER_DASHBOARD] userProfile: ${userProfile}")
        println("🔍 [SELLER_DASHBOARD] sellerId: ${userProfile?.sellerId}")
        
        if (accessToken != null && userProfile?.sellerId != null) {
            println("🔍 [SELLER_DASHBOARD] Iniciando carga de estadísticas para sellerId: ${userProfile!!.sellerId}")
            coroutineScope.launch {
                isLoadingStats = true
                
                // Por ahora, usamos los datos de pagos pendientes para calcular estadísticas
                // En el futuro, se puede crear una API específica para estadísticas del vendedor
                paymentService.getPendingPayments(
                    sellerId = userProfile!!.sellerId!!.toInt(),
                    page = 0,
                    limit = 100, // Obtener más pagos para estadísticas
                    token = accessToken!!
                ).fold(
                    onSuccess = { response ->
                        println("🔍 [SELLER_DASHBOARD] Estadísticas cargadas exitosamente")
                        // Calcular estadísticas basándose en los pagos
                        val allPayments = response.data.payments
                        confirmedPaymentsCount = allPayments.count { it.status == "CONFIRMED" }
                        totalAmountCollected = allPayments
                            .filter { it.status == "CONFIRMED" }
                            .sumOf { it.amount }
                        isLoadingStats = false
                        println("🔍 [SELLER_DASHBOARD] Pagos confirmados: $confirmedPaymentsCount, Total: $totalAmountCollected")
                    },
                    onFailure = { error ->
                        println("🔍 [SELLER_DASHBOARD] Error cargando estadísticas: ${error.message}")
                        // En caso de error, mantener valores por defecto
                        confirmedPaymentsCount = 0
                        totalAmountCollected = 0.0
                        isLoadingStats = false
                    }
                )
            }
        } else {
            println("🔍 [SELLER_DASHBOARD] No se puede cargar estadísticas - datos faltantes")
        }
    }
    
    // Función para confirmar un pago
    val claimPayment: (Int) -> Unit = { paymentId ->
        if (userProfile?.sellerId != null && accessToken != null) {
            coroutineScope.launch {
                println("🔍 [SELLER_DASHBOARD] Intentando confirmar pago $paymentId para sellerId: ${userProfile!!.sellerId!!.toInt()} con token: ${accessToken!!.take(20)}...")
                println("🔍 [SELLER_DASHBOARD] UserProfile completo: $userProfile")
                
                // Marcar el pago como procesando
                processingPayments = processingPayments + paymentId
                
                // Debug: Decodificar el token JWT para ver qué ID contiene
                try {
                    val tokenParts = accessToken!!.split(".")
                    if (tokenParts.size >= 2) {
                        val payload = tokenParts[1]
                        // Agregar padding si es necesario
                        val paddedPayload = payload + "=".repeat((4 - payload.length % 4) % 4)
                        val decodedBytes = android.util.Base64.decode(paddedPayload, android.util.Base64.DEFAULT)
                        val decodedPayload = String(decodedBytes)
                        println("🔍 [SELLER_DASHBOARD] Token JWT payload: $decodedPayload")
                    }
                } catch (e: Exception) {
                    println("🔍 [SELLER_DASHBOARD] Error decodificando token: ${e.message}")
                }
                paymentService.claimPayment(
                    sellerId = userProfile!!.sellerId!!.toInt(), // Usar sellerId como debe ser
                    paymentId = paymentId,
                    token = accessToken!!
                ).fold(
                    onSuccess = { response ->
                        // Remover el pago de la lista inmediatamente
                        pendingPayments = pendingPayments.filter { it.paymentId != paymentId }
                        processingPayments = processingPayments - paymentId
                        
                        // Recargar estadísticas
                        loadSellerStats()
                        println("🔍 [SELLER_DASHBOARD] Pago confirmado exitosamente: $paymentId")
                    },
                    onFailure = { error ->
                        val errorMessage = error.message ?: ""
                        processingPayments = processingPayments - paymentId
                        
                        if (errorMessage.contains("El pago ya fue procesado")) {
                            // Si el pago ya fue procesado, simplemente lo removemos de la lista
                            pendingPayments = pendingPayments.filter { it.paymentId != paymentId }
                            println("🔍 [SELLER_DASHBOARD] Pago $paymentId ya fue procesado, removido de la lista")
                        } else {
                            println("🔍 [SELLER_DASHBOARD] Error confirmando pago: $errorMessage")
                        }
                    }
                )
            }
        }
    }
    
    // Función para rechazar un pago
    val rejectPayment: (Int) -> Unit = { paymentId ->
        if (userProfile?.sellerId != null && accessToken != null) {
            coroutineScope.launch {
                println("🔍 [SELLER_DASHBOARD] Intentando rechazar pago $paymentId para sellerId: ${userProfile!!.sellerId!!.toInt()} con token: ${accessToken!!.take(20)}...")
                
                // Marcar el pago como procesando
                processingPayments = processingPayments + paymentId
                
                paymentService.rejectPayment(
                    sellerId = userProfile!!.sellerId!!.toInt(), // Usar sellerId como debe ser
                    paymentId = paymentId,
                    reason = "No es mi pago", // Razón por defecto
                    token = accessToken!!
                ).fold(
                    onSuccess = { response ->
                        // Remover el pago de la lista inmediatamente
                        pendingPayments = pendingPayments.filter { it.paymentId != paymentId }
                        processingPayments = processingPayments - paymentId
                        
                        // Recargar estadísticas
                        loadSellerStats()
                        println("🔍 [SELLER_DASHBOARD] Pago rechazado exitosamente: $paymentId")
                    },
                    onFailure = { error ->
                        val errorMessage = error.message ?: ""
                        processingPayments = processingPayments - paymentId
                        
                        if (errorMessage.contains("El pago ya fue procesado")) {
                            // Si el pago ya fue procesado, simplemente lo removemos de la lista
                            pendingPayments = pendingPayments.filter { it.paymentId != paymentId }
                            println("🔍 [SELLER_DASHBOARD] Pago $paymentId ya fue procesado, removido de la lista")
                        } else {
                            println("🔍 [SELLER_DASHBOARD] Error rechazando pago: $errorMessage")
                        }
                    }
                )
            }
        }
    }
    
    // Cargar datos al iniciar
    LaunchedEffect(accessToken, userProfile) {
        if (accessToken != null && userProfile?.sellerId != null) {
            loadSellerStats()
            loadPendingPayments()
        }
    }
    
    // Manejar notificaciones de nuevos pagos con mejoras UX
    LaunchedEffect(Unit) {
        webSocketService.paymentNotifications.collect { notification ->
            coroutineScope.launch {
                Logger.auth("SELLER_DASHBOARD", "🆕 Nueva notificación recibida: ${notification.paymentId}")
                
                // Procesar notificación con sonido y vibración
                notificationService.processNewPayment(notification)
                
                // Mostrar notificación en UI
                currentNotification = notification
                newPaymentsCount++
                
                // Actualizar TODA la información de la app
                Logger.auth("SELLER_DASHBOARD", "🔄 Actualizando información completa...")
                
                // 1. Recargar pagos pendientes (el nuevo pago aparecerá primero)
                loadPendingPayments()
                
                // 2. Actualizar estadísticas del vendedor
                loadSellerStats()
                
                // 3. Mostrar mensaje de éxito
                showSuccessMessage = "¡Nuevo pago recibido! S/ ${notification.amount}"
                
                Logger.auth("SELLER_DASHBOARD", "✅ Información actualizada completamente")
            }
        }
    }
    
    // Manejar resultados de pagos con mejoras UX
    LaunchedEffect(Unit) {
        webSocketService.paymentResults.collect { result ->
            coroutineScope.launch {
                Logger.auth("SELLER_DASHBOARD", "📊 Resultado de pago recibido: ${result.paymentId} - ${result.status}")
                
                // Procesar resultado
                notificationService.processPaymentResult(result)
                currentResultNotification = result
                
                // Actualizar TODA la información
                Logger.auth("SELLER_DASHBOARD", "🔄 Actualizando información después de procesar pago...")
                
                // 1. Actualizar estadísticas (más importante después de procesar)
                loadSellerStats()
                
                // 2. Recargar pagos pendientes (el pago procesado desaparecerá)
                loadPendingPayments()
                
                // 3. Mostrar mensaje de resultado
                showSuccessMessage = when (result.status) {
                    "CONFIRMED" -> "✅ Pago confirmado exitosamente"
                    "REJECTED_BY_SELLER" -> "❌ Pago rechazado"
                    else -> "📊 Pago procesado: ${result.status}"
                }
                
                Logger.auth("SELLER_DASHBOARD", "✅ Información actualizada después de procesar pago")
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Dashboard Vendedor",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Indicador de nuevos pagos
                    if (newPaymentsCount > 0) {
                        Box {
                            IconButton(onClick = { 
                                newPaymentsCount = 0
                                currentNotification = null
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = "Notificaciones"
                                )
                            }
                            Badge(
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Text(
                                    text = newPaymentsCount.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = "Historial"
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Configuración"
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Filled.Logout,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Indicador de estado WebSocket
            item {
                WebSocketStatusIndicator(
                    connectionState = connectionState,
                    sellerId = userProfile?.sellerId?.toInt(),
                    onReconnectClick = { 
                        coroutineScope.launch {
                            webSocketService.reconnect()
                        }
                    }
                )
            }
            
            // Notificación de nuevo pago
            currentNotification?.let { notification ->
                item {
                    PaymentNotificationCard(
                        notification = notification,
                        onDismiss = { currentNotification = null },
                        onClaim = { 
                            coroutineScope.launch {
                                claimPayment(notification.paymentId)
                                currentNotification = null
                            }
                        },
                        onReject = { 
                            coroutineScope.launch {
                                rejectPayment(notification.paymentId)
                                currentNotification = null
                            }
                        }
                    )
                }
            }
            
            // Notificación de resultado de pago
            currentResultNotification?.let { result ->
                item {
                    PaymentNotificationCard(
                        notification = PaymentNotificationData(
                            paymentId = result.paymentId,
                            amount = 0.0, // No tenemos el monto en el resultado
                            senderName = result.sellerName,
                            yapeCode = "",
                            status = result.status,
                            timestamp = "",
                            message = result.message
                        ),
                        onDismiss = { currentResultNotification = null }
                    )
                }
            }
            // Header con información del vendedor
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = userProfile?.sellerName ?: "Mi Nombre",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = userProfile?.branchName ?: "Mi Sucursal",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Estado: En línea",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Estadísticas del día
            item {
                Text(
                    text = "Estadísticas del Día",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Pagos Confirmados",
                        value = if (isLoadingStats) "..." else confirmedPaymentsCount.toString(),
                        icon = Icons.Filled.CheckCircle,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    StatCard(
                        title = "Total Cobrado",
                        value = if (isLoadingStats) "..." else "S/ ${String.format("%.2f", totalAmountCollected)}",
                        icon = Icons.Filled.AttachMoney,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Pagos pendientes
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pagos Pendientes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                    )
                    
                    if (pendingPayments.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${pendingPayments.size}",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            
            if (pendingPayments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "No hay pagos pendientes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Text(
                                text = "Todos los pagos han sido confirmados",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(pendingPayments) { payment ->
                    val isProcessing = processingPayments.contains(payment.paymentId)
                    
                    if (isProcessing) {
                        // Mostrar indicador de procesamiento
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Procesando pago...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    } else {
                        // Mostrar la tarjeta normal del pago
                        PendingPaymentCard(
                            payment = payment,
                            onConfirm = { 
                                claimPayment(payment.paymentId)
                            },
                            onReject = { 
                                rejectPayment(payment.paymentId)
                            }
                        )
                    }
                }
            }
            
            // Botón "Cargar más" si hay más pagos disponibles
            if (hasMorePayments && pendingPayments.isNotEmpty()) {
                item {
                    Button(
                        onClick = loadMorePendingPayments,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        enabled = !isLoadingMorePayments,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        if (isLoadingMorePayments) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cargando...")
                        } else {
                            Text("Cargar más pagos")
                        }
                    }
                }
            }
            
            // Acciones rápidas
            item {
                Text(
                    text = "Acciones Rápidas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionButton(
                        title = "Ver Historial",
                        icon = Icons.Filled.History,
                        onClick = onNavigateToHistory,
                        modifier = Modifier.weight(1f)
                    )
                    
                    ActionButton(
                        title = "Pagos",
                        icon = Icons.Filled.Payment,
                        onClick = onNavigateToPendingPayments,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionButton(
                        title = "Configuración",
                        icon = Icons.Filled.Settings,
                        onClick = onNavigateToSettings,
                        modifier = Modifier.weight(1f)
                    )
                    
                    ActionButton(
                        title = "Solicitar Baja",
                        icon = Icons.Filled.ExitToApp,
                        onClick = onNavigateToDeactivationRequest,
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Espacio adicional
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
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
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PendingPaymentCard(
    payment: org.sysarp.project.data.SellerPendingPayment,
    onConfirm: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Payment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nuevo Pago Yape",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "¿Es tuyo este pago?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = formatPaymentTimestamp(payment.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Detalles del pago
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Monto:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "S/ ${String.format("%.2f", payment.amount)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "De:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = payment.senderName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Código:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = extractShortYapeCode(payment.yapeCode),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Es Mío")
                }
                
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Cancel,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("No Es Mío")
                }
            }
        }
    }
}

@Composable
fun ActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

