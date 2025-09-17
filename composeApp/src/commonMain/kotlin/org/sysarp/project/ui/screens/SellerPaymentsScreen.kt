package org.sysarp.project.ui.screens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.sysarp.project.data.MySeller
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.payment.PaymentService
import java.text.SimpleDateFormat
import java.util.*
import org.sysarp.project.utils.extractShortYapeCode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerPaymentsScreen(
    authService: AuthService,
    paymentService: PaymentService,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var pendingPayments by remember { mutableStateOf<List<org.sysarp.project.data.SellerPendingPayment>>(emptyList()) }
    var confirmedPayments by remember { mutableStateOf<List<org.sysarp.project.data.SellerPendingPayment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val tabs = listOf("Pendientes", "Confirmados")

    // Función para cargar pagos pendientes
    val loadPendingPayments: () -> Unit = {
        if (accessToken != null && userProfile?.sellerId != null) {
            coroutineScope.launch {
                isLoading = true
                errorMessage = ""

                paymentService.getPendingPayments(
                    sellerId = userProfile!!.sellerId!!.toInt(),
                    page = 0,
                    limit = 50, // Cargar más para tener una vista completa
                    token = accessToken!!
                ).fold(
                    onSuccess = { response ->
                        pendingPayments = response.data.payments
                        isLoading = false
                    },
                    onFailure = { error ->
                        errorMessage = error.message ?: "Error cargando pagos pendientes"
                        isLoading = false
                    }
                )
            }
        }
    }

    // Función para cargar pagos confirmados
    val loadConfirmedPayments: () -> Unit = {
        if (accessToken != null && userProfile?.sellerId != null) {
            coroutineScope.launch {
                isLoading = true
                errorMessage = ""

                paymentService.getConfirmedPayments(
                    sellerId = userProfile!!.sellerId!!.toInt(),
                    page = 0,
                    size = 50, // Cargar más para tener una vista completa
                    token = accessToken!!
                ).fold(
                    onSuccess = { response ->
                        confirmedPayments = response.data.payments
                        isLoading = false
                    },
                    onFailure = { error ->
                        errorMessage = error.message ?: "Error cargando pagos confirmados"
                        isLoading = false
                    }
                )
            }
        }
    }

    // Función para confirmar un pago
    val claimPayment: (Int) -> Unit = { paymentId ->
        if (userProfile?.sellerId != null && accessToken != null) {
            coroutineScope.launch {
                paymentService.claimPayment(
                    sellerId = userProfile!!.sellerId!!.toInt(),
                    paymentId = paymentId,
                    token = accessToken!!
                ).fold(
                    onSuccess = { response ->
                        // Recargar ambas listas
                        loadPendingPayments()
                        loadConfirmedPayments()
                    },
                    onFailure = { error ->
                        errorMessage = "Error confirmando pago: ${error.message}"
                    }
                )
            }
        }
    }

    // Cargar pagos al iniciar
    LaunchedEffect(accessToken, userProfile) {
        if (accessToken != null && userProfile?.sellerId != null) {
            loadPendingPayments()
            loadConfirmedPayments()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Mis Pagos",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        loadPendingPayments()
                        loadConfirmedPayments()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Actualizar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { 
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Filled.Schedule
                                    else -> Icons.Filled.CheckCircle
                                },
                                contentDescription = title
                            )
                        }
                    )
                }
            }

            // Contenido según tab seleccionado
            when (selectedTab) {
                0 -> PendingPaymentsContent(
                    payments = pendingPayments,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onRefresh = loadPendingPayments,
                    userProfile = userProfile,
                    accessToken = accessToken,
                    paymentService = paymentService,
                    coroutineScope = coroutineScope,
                    onError = { error ->
                        errorMessage = error
                    }
                )
                1 -> ConfirmedPaymentsContent(
                    payments = confirmedPayments,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onRefresh = loadConfirmedPayments
                )
            }
        }
    }
}

@Composable
fun PendingPaymentsContent(
    payments: List<org.sysarp.project.data.SellerPendingPayment>,
    isLoading: Boolean,
    errorMessage: String,
    onRefresh: () -> Unit,
    userProfile: org.sysarp.project.data.UserProfile?,
    accessToken: String?,
    paymentService: PaymentService,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onError: (String) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (errorMessage.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = "Error",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRefresh,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Reintentar")
                }
            }
        }
    } else if (payments.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay pagos pendientes",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Los pagos aparecerán aquí cuando los clientes realicen transacciones.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        // Estadísticas rápidas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val totalAmount = payments.sumOf { it.amount }
                
                PaymentStatItem("Pendientes", payments.size.toString(), Icons.Filled.Schedule)
                PaymentStatItem("Total", "S/ ${String.format("%.2f", totalAmount)}", Icons.Filled.AttachMoney)
            }
        }

        // Lista de pagos pendientes
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(payments) { payment ->
                PendingPaymentCardWithActions(
                    payment = payment,
                    sellerId = userProfile?.sellerId?.toInt(),
                    accessToken = accessToken,
                    paymentService = paymentService,
                    onClaimPayment = onRefresh,
                    onError = onError
                )
            }

            // Espacio adicional
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun ConfirmedPaymentsContent(
    payments: List<org.sysarp.project.data.SellerPendingPayment>,
    isLoading: Boolean,
    errorMessage: String,
    onRefresh: () -> Unit
) {
    if (isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cargando pagos confirmados...")
        }
    } else if (errorMessage.isNotEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRefresh) {
                Text("Reintentar")
            }
        }
    } else if (payments.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hay pagos confirmados",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Los pagos confirmados aparecerán aquí.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        // Estadísticas rápidas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val totalAmount = payments.sumOf { it.amount }
                
                PaymentStatItem("Confirmados", payments.size.toString(), Icons.Filled.CheckCircle)
                PaymentStatItem("Total", "S/ ${String.format("%.2f", totalAmount)}", Icons.Filled.AttachMoney)
            }
        }

        // Lista de pagos confirmados
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(payments) { payment ->
                ConfirmedPaymentCard(payment = payment)
            }

            // Espacio adicional
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun ConfirmedPaymentCard(
    payment: org.sysarp.project.data.SellerPendingPayment
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
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
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Confirmado",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                PaymentInfoRow(
                    label = "Monto",
                    value = "S/ ${String.format("%.2f", payment.amount)}",
                    icon = Icons.Filled.AttachMoney
                )
                
                PaymentInfoRow(
                    label = "Cliente",
                    value = payment.senderName,
                    icon = Icons.Filled.Person
                )
                
                PaymentInfoRow(
                    label = "Código Yape",
                    value = payment.yapeCode,
                    icon = Icons.Filled.QrCode
                )
                
                PaymentInfoRow(
                    label = "Fecha",
                    value = payment.timestamp,
                    icon = Icons.Filled.Schedule
                )
            }
        }
    }
}

@Composable
fun PaymentInfoRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
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

@Composable
fun PendingPaymentCardWithActions(
    payment: org.sysarp.project.data.SellerPendingPayment,
    sellerId: Int?,
    accessToken: String?,
    paymentService: PaymentService,
    onClaimPayment: () -> Unit,
    onError: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con monto y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "S/ ${String.format("%.2f", payment.amount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = payment.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Información del pago
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Remitente",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = payment.senderName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCode,
                    contentDescription = "Código Yape",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = extractShortYapeCode(payment.yapeCode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = "Fecha",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatPaymentTimestamp(payment.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (payment.message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = payment.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón de confirmación
            Button(
                onClick = {
                    if (sellerId != null && accessToken != null) {
                        coroutineScope.launch {
                            paymentService.claimPayment(
                                sellerId = sellerId,
                                paymentId = payment.paymentId,
                                token = accessToken
                            ).fold(
                                onSuccess = { response ->
                                    onClaimPayment()
                                },
                                onFailure = { error ->
                                    onError("Error confirmando pago: ${error.message}")
                                }
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Confirmar pago",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Confirmar Pago",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun formatPaymentTimestamp(timestamp: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        formatter.format(parser.parse(timestamp) ?: Date())
    } catch (e: Exception) {
        timestamp // Return original if parsing fails
    }
}

// Las funciones PaymentStatItem y PendingPaymentCard están definidas en PendingPaymentsScreen.kt para evitar duplicación
