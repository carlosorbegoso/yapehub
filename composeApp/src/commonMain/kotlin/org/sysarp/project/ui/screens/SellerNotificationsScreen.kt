package org.sysarp.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.SellerNotification
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.notifications.SellerNotificationService
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerNotificationsScreen(
    authService: AuthService,
    onNavigateBack: () -> Unit
) {
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()
    
    val notificationService = remember { SellerNotificationService(authService) }
    val coroutineScope = rememberCoroutineScope()
    
    // Estados
    var notifications by remember { mutableStateOf<List<SellerNotification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var unreadCount by remember { mutableStateOf(0) }
    var currentPage by remember { mutableStateOf(0) }
    var hasMorePages by remember { mutableStateOf(true) }
    
    // Cargar notificaciones
    val loadNotifications = {
        if (accessToken != null) {
            isLoading = true
            errorMessage = ""
        }
    }
    
    // Manejar la carga de notificaciones
    LaunchedEffect(isLoading, currentPage) {
        if (isLoading && accessToken != null) {
            notificationService.getSellerNotifications(currentPage, 20)
                .fold(
                    onSuccess = { data ->
                        if (currentPage == 0) {
                            notifications = data.notifications
                        } else {
                            notifications = notifications + data.notifications
                        }
                        unreadCount = data.unreadCount
                        hasMorePages = data.pagination.currentPage < data.pagination.totalPages - 1
                        isLoading = false
                    },
                    onFailure = { error ->
                        errorMessage = "Error cargando notificaciones: ${error.message}"
                        isLoading = false
                    }
                )
        }
    }
    
    // Cargar notificaciones iniciales
    LaunchedEffect(Unit) {
        loadNotifications()
    }
    
    // Función para marcar como leída
    val markAsRead = { notificationId: Int ->
        coroutineScope.launch {
            notificationService.markNotificationAsRead(notificationId)
                .fold(
                    onSuccess = { data ->
                        // Actualizar la notificación localmente
                        notifications = notifications.map { notification ->
                            if (notification.id == notificationId) {
                                notification.copy(isRead = true, readAt = data.readAt)
                            } else {
                                notification
                            }
                        }
                        unreadCount = data.unreadCount
                    },
                    onFailure = { error ->
                        errorMessage = "Error marcando como leída: ${error.message}"
                    }
                )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Notificaciones")
                    if (unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge {
                            Text(unreadCount.toString())
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        currentPage = 0
                        loadNotifications()
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mensaje de error
            if (errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Error, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(errorMessage, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
            
            if (isLoading && notifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (notifications.isEmpty()) {
                // Estado vacío
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Filled.NotificationsOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "No hay notificaciones",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Las notificaciones aparecerán aquí cuando recibas pagos o actualizaciones",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications) { notification ->
                        NotificationCard(
                            notification = notification,
                            onMarkAsRead = { markAsRead(notification.id) }
                        )
                    }
                    
                    // Cargar más
                    if (hasMorePages && !isLoading) {
                        item {
                            Button(
                                onClick = {
                                    currentPage++
                                    loadNotifications()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cargar más")
                            }
                        }
                    }
                    
                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: SellerNotification,
    onMarkAsRead: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Icono según el tipo
                    Icon(
                        when (notification.type) {
                            "PAYMENT" -> Icons.Filled.Payment
                            "SYSTEM" -> Icons.Filled.Settings
                            "ALERT" -> Icons.Filled.Warning
                            else -> Icons.Filled.Notifications
                        },
                        contentDescription = null,
                        tint = when (notification.type) {
                            "PAYMENT" -> MaterialTheme.colorScheme.primary
                            "SYSTEM" -> MaterialTheme.colorScheme.secondary
                            "ALERT" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.outline
                        }
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold
                        )
                        
                        Text(
                            text = notification.message,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Indicador de no leída
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
            
            // Información adicional
            notification.data?.let { data ->
                if (data.paymentId != null || data.amount != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        data.paymentId?.let { paymentId ->
                            Text(
                                text = "Pago #$paymentId",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        
                        data.amount?.let { amount ->
                            Text(
                                text = "S/ ${String.format("%.2f", amount)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            // Fecha y acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(notification.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                
                if (!notification.isRead) {
                    TextButton(onClick = onMarkAsRead) {
                        Text("Marcar como leída")
                    }
                }
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val instant = Instant.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        dateString
    }
}
