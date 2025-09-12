package org.sysarp.project.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import org.sysarp.project.data.TransactionType
import org.sysarp.project.data.YapeTransaction
import org.sysarp.project.ui.components.ModernCard
import org.sysarp.project.ui.components.TransactionCard
import org.sysarp.project.ui.components.TransactionStatsCard
import org.sysarp.project.ui.components.DebugLogPanel
import org.sysarp.project.viewmodel.YapeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: YapeViewModel,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPendingPayments: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val businessReports by viewModel.businessReports.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    
    // Estado para el panel de debug
    var isDebugVisible by remember { mutableStateOf(false) }
    
    
    // Calcular estadísticas (solo transacciones recibidas)
    val totalReceived = transactions
        .filter { it.transactionType == TransactionType.RECEIVED }
        .sumOf { it.amount }
    
    val todayTransactions = transactions
        .filter { it.transactionType == TransactionType.RECEIVED }
        .filter { 
            val today = Clock.System.now().toEpochMilliseconds()
            val transactionDate = it.createdAt.toEpochMilliseconds()
            val dayInMs = 24 * 60 * 60 * 1000L
            (today - transactionDate) < dayInMs
        }
    
    val todayTotal = todayTransactions.sumOf { it.amount }
    
    // Animaciones
    val fadeIn = remember { fadeIn(animationSpec = tween(800)) }
    val slideIn = remember { slideInVertically(animationSpec = tween(800)) { it / 3 } }
    val scaleIn = remember { scaleIn(animationSpec = tween(800)) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Header moderno con gradiente
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn + slideIn
            ) {
                ModernCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "¡Hola, ${currentUser?.name ?: "Usuario"}!",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                    text = when (currentUser?.role) {
                                        org.sysarp.project.data.UserRole.ADMIN -> "Administrador del sistema"
                                        org.sysarp.project.data.UserRole.VENDOR -> "Vendedor - ${currentUser?.assignedStores?.firstOrNull() ?: "Sin tienda asignada"}"
                                        null -> "Selecciona tu perfil"
                                    },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                    )
                                }
                                
                                IconButton(
                                    onClick = onLogout,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = "Cerrar sesión",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Estado de captura con animación
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isCapturing = uiState.isCapturing
                                    val pulseAlpha by animateFloatAsState(
                                        targetValue = when {
                                            uiState.permissionState == org.sysarp.project.service.PermissionState.GRANTED && uiState.isCapturing -> 
                                                1f // Parpadea fuerte cuando está capturando
                                            uiState.permissionState == org.sysarp.project.service.PermissionState.NEEDS_SETUP -> 
                                                0.8f // Parpadea suave cuando necesita configuración
                                            uiState.permissionState == org.sysarp.project.service.PermissionState.DENIED -> 
                                                0.9f // Parpadea fuerte cuando no tiene permisos
                                            else -> 
                                                0.5f // Opaco cuando está desconocido
                                        },
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(
                                                durationMillis = when {
                                                    uiState.permissionState == org.sysarp.project.service.PermissionState.GRANTED && uiState.isCapturing -> 
                                                        800 // Parpadea rápido cuando está capturando
                                                    uiState.permissionState == org.sysarp.project.service.PermissionState.NEEDS_SETUP -> 
                                                        1500 // Parpadea lento cuando necesita configuración
                                                    uiState.permissionState == org.sysarp.project.service.PermissionState.DENIED -> 
                                                        600 // Parpadea muy rápido cuando no tiene permisos
                                                    else -> 
                                                        2000 // Parpadea muy lento cuando está desconocido
                                                },
                                                easing = EaseInOut
                                            ),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "pulse"
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    uiState.permissionState == org.sysarp.project.service.PermissionState.GRANTED && uiState.isCapturing -> 
                                                        Color(0xFF4CAF50) // Verde cuando está capturando
                                                    uiState.permissionState == org.sysarp.project.service.PermissionState.NEEDS_SETUP -> 
                                                        Color(0xFFFFC107) // Amarillo cuando necesita configuración
                                                    uiState.permissionState == org.sysarp.project.service.PermissionState.DENIED -> 
                                                        Color(0xFFF44336) // Rojo cuando no tiene permisos
                                                    else -> 
                                                        Color(0xFF9E9E9E) // Gris cuando está desconocido
                                                }
                                            )
                                            .alpha(
                                                when {
                                                    uiState.permissionState == org.sysarp.project.service.PermissionState.GRANTED && uiState.isCapturing -> 
                                                        pulseAlpha // Parpadea cuando está capturando
                                                    uiState.permissionState == org.sysarp.project.service.PermissionState.NEEDS_SETUP -> 
                                                        0.7f + (pulseAlpha * 0.3f) // Parpadea suave cuando necesita configuración
                                                    uiState.permissionState == org.sysarp.project.service.PermissionState.DENIED -> 
                                                        0.8f + (pulseAlpha * 0.2f) // Parpadea fuerte cuando no tiene permisos
                                                    else -> 
                                                        0.5f // Opaco cuando está desconocido
                                                }
                                            )
                                            .shadow(
                                                elevation = when {
                                                    uiState.permissionState == org.sysarp.project.service.PermissionState.GRANTED && uiState.isCapturing -> 
                                                        8.dp // Sombra fuerte cuando está capturando
                                                    uiState.permissionState == org.sysarp.project.service.PermissionState.NEEDS_SETUP -> 
                                                        4.dp // Sombra media cuando necesita configuración
                                                    uiState.permissionState == org.sysarp.project.service.PermissionState.DENIED -> 
                                                        6.dp // Sombra fuerte cuando no tiene permisos
                                                    else -> 
                                                        2.dp // Sombra suave cuando está desconocido
                                                },
                                                shape = CircleShape,
                                                ambientColor = when {
                                                    uiState.permissionState == org.sysarp.project.service.PermissionState.GRANTED && uiState.isCapturing -> 
                                                        Color(0xFF4CAF50).copy(alpha = 0.3f) // Sombra verde
                                                    uiState.permissionState == org.sysarp.project.service.PermissionState.NEEDS_SETUP -> 
                                                        Color(0xFFFFC107).copy(alpha = 0.3f) // Sombra amarilla
                                                    uiState.permissionState == org.sysarp.project.service.PermissionState.DENIED -> 
                                                        Color(0xFFF44336).copy(alpha = 0.3f) // Sombra roja
                                                    else -> 
                                                        Color(0xFF9E9E9E).copy(alpha = 0.3f) // Sombra gris
                                                }
                                            )
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = when {
                                            currentUser?.role == org.sysarp.project.data.UserRole.ADMIN -> {
                                                when {
                                                    uiState.permissionState == org.sysarp.project.service.PermissionState.GRANTED && uiState.isCapturing -> 
                                                        "✅ Capturando notificaciones de Yape"
                                                    uiState.permissionState == org.sysarp.project.service.PermissionState.NEEDS_SETUP -> 
                                                        "⚠️ Configuración necesaria"
                                                    uiState.permissionState == org.sysarp.project.service.PermissionState.DENIED -> 
                                                        "❌ Permisos denegados"
                                                    else -> 
                                                        "🔄 Verificando permisos..."
                                                }
                                            }
                                            currentUser?.role == org.sysarp.project.data.UserRole.VENDOR -> 
                                                "👥 Esperando confirmaciones de pago"
                                            else -> 
                                                "👤 Selecciona tu perfil"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = when {
                                            uiState.permissionState == org.sysarp.project.service.PermissionState.GRANTED && uiState.isCapturing -> 
                                                Color(0xFF4CAF50) // Verde cuando está capturando
                                            uiState.permissionState == org.sysarp.project.service.PermissionState.NEEDS_SETUP -> 
                                                Color(0xFFFFC107) // Amarillo cuando necesita configuración
                                            uiState.permissionState == org.sysarp.project.service.PermissionState.DENIED -> 
                                                Color(0xFFF44336) // Rojo cuando no tiene permisos
                                            else -> 
                                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f) // Blanco por defecto
                                        }
                                    )
                                }
                                
                                // Mensaje de estado de permisos
                                if (uiState.permissionMessage.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = uiState.permissionMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when (uiState.permissionState) {
                                            org.sysarp.project.service.PermissionState.GRANTED -> MaterialTheme.colorScheme.tertiary
                                            org.sysarp.project.service.PermissionState.DENIED -> MaterialTheme.colorScheme.error
                                            org.sysarp.project.service.PermissionState.NEEDS_SETUP -> MaterialTheme.colorScheme.secondary
                                            else -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Estadísticas modernas
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn + slideIn
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Resumen Financiero",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TransactionStatsCard(
                            title = "Total Recibido",
                            amount = totalReceived,
                            icon = Icons.Default.Star,
                            modifier = Modifier.weight(1f)
                        )
                        
                        TransactionStatsCard(
                            title = "Hoy",
                            amount = todayTotal,
                            icon = Icons.Default.DateRange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        
        // Botón especial para vendedores
        if (currentUser?.role == org.sysarp.project.data.UserRole.VENDOR) {
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn + slideIn
                ) {
                    ModernCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { onNavigateToPendingPayments() },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.tertiary,
                                                MaterialTheme.colorScheme.primary
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(20.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Confirmar Pagos",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "Confirma los pagos que recibiste de tus clientes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Ir a confirmar pagos",
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }
        
        // Transacciones recientes
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn + slideIn
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transacciones Recientes",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        
                        TextButton(
                            onClick = onNavigateToReports,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Ver todas")
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Lista de transacciones
        items(
            items = transactions.take(5),
            key = { it.id }
        ) { transaction ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn + slideIn
            ) {
                TransactionCard(
                    transaction = transaction,
                    onMarkAsProcessed = { _ -> /* TODO: Implementar */ },
                    onAssignBusiness = { _, _ -> /* TODO: Implementar */ },
                    onDelete = { _ -> /* TODO: Implementar */ },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
        
        // Mensaje si no hay transacciones
        if (transactions.isEmpty()) {
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn + slideIn
                ) {
                    ModernCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay transacciones aún",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Las transacciones de Yape aparecerán aquí cuando lleguen",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Panel de Debug (fuera del Scaffold)
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        
        // Panel de Debug (esquina inferior derecha)
        DebugLogPanel(
            isVisible = isDebugVisible,
            onToggle = { isDebugVisible = !isDebugVisible },
            onExportLogs = { logsText ->
                // En Android, esto se manejará en la implementación específica
                exportLogs(logsText)
            },
            onExportDatabase = { _, fileName ->
                // Exportar base de datos a texto plano con nombre específico
                val databaseText = viewModel.exportDatabaseToText()
                exportDatabase(databaseText, fileName)
            },
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

// Función expect para exportar logs (implementada en cada plataforma)
expect fun exportLogs(logsText: String)

// Función expect para exportar base de datos (implementada en cada plataforma)
expect fun exportDatabase(databaseText: String, fileName: String)