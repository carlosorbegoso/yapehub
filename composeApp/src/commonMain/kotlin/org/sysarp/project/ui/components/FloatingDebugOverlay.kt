package org.sysarp.project.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

object DebugLogManager {
    private val _logs = MutableStateFlow<List<DebugLog>>(emptyList())
    val logs: StateFlow<List<DebugLog>> = _logs.asStateFlow()
    
    fun addLog(log: DebugLog) {
        _logs.value = _logs.value + log
    }
    
    fun clearLogs() {
        _logs.value = emptyList()
    }
    
    fun exportLogs(): String {
        if (_logs.value.isEmpty()) {
            return "=== YapeHub Debug Logs ===\nNo hay logs disponibles"
        }
        
        val header = "=== YapeHub Debug Logs ===\n" +
                "Exportado: ${formatTimestamp(System.currentTimeMillis())}\n" +
                "Total logs: ${_logs.value.size}\n" +
                "================================\n\n"
        
        val logsText = _logs.value.joinToString("\n") { log ->
            "${formatTimestamp(log.timestamp)} [${log.type.name}] ${log.message}" +
            if (log.details.isNotEmpty()) "\n  → ${log.details}" else ""
        }
        
        return header + logsText
    }
}

data class DebugLog(
    val timestamp: Long,
    val type: LogType,
    val message: String,
    val details: String = ""
)

enum class LogType {
    PERMISSION,
    NOTIFICATION,
    API,
    WEBSOCKET,
    ERROR
}

fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val formatter = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault())
    return formatter.format(date)
}

@Composable
fun FloatingDebugOverlay() {
    var isExpanded by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }
    val logs by DebugLogManager.logs.collectAsState()
    
    // Botón flotante para activar/desactivar
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1000f)
    ) {
        // Botón toggle en esquina inferior derecha
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            FloatingActionButton(
                onClick = { 
                    isExpanded = !isExpanded
                },
                modifier = Modifier
                    .padding(16.dp)
                    .size(40.dp), // Botón más pequeño
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.Close else Icons.Filled.BugReport,
                    contentDescription = if (isExpanded) "Cerrar Debug" else "Abrir Debug",
                    modifier = Modifier.size(18.dp) // Icono más pequeño
                )
            }
        }
        
        // Overlay de debug expandido
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(500)
            ) + fadeIn(animationSpec = tween(500)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(500)
            ) + fadeOut(animationSpec = tween(500)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            DebugTerminal(
                logs = logs,
                onClearLogs = { DebugLogManager.clearLogs() },
                onExportLogs = { 
                    // TODO: Implementar exportación real
                    // Por ahora solo simulamos
                },
                onClose = { isExpanded = false }
            )
        }
    }
    
    // Mostrar el botón después de un delay inicial
    LaunchedEffect(Unit) {
        delay(2000) // Mostrar después de 2 segundos
        isVisible = true
    }
}

@Composable
fun DebugTerminal(
    logs: List<DebugLog>,
    onClearLogs: () -> Unit,
    onExportLogs: () -> Unit,
    onClose: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var showCopiedMessage by remember { mutableStateOf(false) }
    
    // Mostrar mensaje de confirmación temporalmente
    LaunchedEffect(showCopiedMessage) {
        if (showCopiedMessage) {
            delay(2000)
            showCopiedMessage = false
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .heightIn(min = 200.dp, max = 400.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E) // Terminal dark theme
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header del terminal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2D2D2D))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Terminal,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF00FF00)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Debug Terminal",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onClearLogs,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Limpiar",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                    
                    IconButton(
                        onClick = { 
                            val logsText = DebugLogManager.exportLogs()
                            clipboardManager.setText(AnnotatedString(logsText))
                            showCopiedMessage = true
                            onExportLogs()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = "Copiar logs",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                    
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                }
            }
            
            // Mensaje de confirmación
            if (showCopiedMessage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF4CAF50))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✅ Logs copiados al portapapeles",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Contenido del terminal
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (logs.isEmpty()) {
                    item {
                        Text(
                            text = "> Esperando logs...",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF888888),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                } else {
                    items(logs.reversed()) { log ->
                        TerminalLogItem(log = log)
                    }
                }
            }
        }
    }
}

@Composable
fun TerminalLogItem(log: DebugLog) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = formatTimestamp(log.timestamp),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF888888),
            modifier = Modifier.width(80.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "[${log.type.name}]",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = when (log.type) {
                LogType.PERMISSION -> Color(0xFF4FC3F7)
                LogType.NOTIFICATION -> Color(0xFF81C784)
                LogType.API -> Color(0xFFFFB74D)
                LogType.WEBSOCKET -> Color(0xFFBA68C8)
                LogType.ERROR -> Color(0xFFE57373)
            },
            modifier = Modifier.width(80.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )
            
            if (log.details.isNotEmpty()) {
                Text(
                    text = "  → ${log.details}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFCCCCCC),
                    fontSize = 10.sp
                )
            }
        }
    }
}

