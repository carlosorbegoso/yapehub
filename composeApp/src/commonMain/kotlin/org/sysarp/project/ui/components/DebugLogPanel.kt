package org.sysarp.project.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.sysarp.project.service.DebugLogger

@Composable
fun DebugLogPanel(
    modifier: Modifier = Modifier,
    isVisible: Boolean = false,
    onToggle: () -> Unit = {},
    onExportLogs: (String) -> Unit = {},
    onExportDatabase: (String, String) -> Unit = { _, _ -> }
) {
    val logs = DebugLogger.getLogs()
    val logStats = DebugLogger.getLogStats()
    val listState = rememberLazyListState()
    var showStats by remember { mutableStateOf(false) }
    
    // Auto-scroll to bottom when new logs arrive
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }
    
    Column(modifier = modifier) {
        // Toggle button
        FloatingActionButton(
            onClick = onToggle,
            modifier = Modifier.size(48.dp),
            containerColor = if (isVisible) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = if (isVisible) Icons.Default.Close else Icons.Default.Info,
                contentDescription = if (isVisible) "Cerrar Debug" else "Abrir Debug",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        // Debug panel
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Debug Logs (${logs.size})",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Botón de estadísticas
                        IconButton(
                            onClick = { showStats = !showStats },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Estadísticas",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        // Botón de exportar logs
                        IconButton(
                            onClick = { 
                                val logsText = DebugLogger.exportLogsAsText()
                                onExportLogs(logsText)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Descargar logs",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        // Botón de exportar base de datos
                        IconButton(
                            onClick = { 
                                val timestamp = Clock.System.now()
                                val dateFormatter = TimeZone.currentSystemDefault()
                                val dateTime = timestamp.toLocalDateTime(dateFormatter)
                                val fileName = "yapechamo_database_${dateTime.year}${dateTime.monthNumber.toString().padStart(2, '0')}${dateTime.dayOfMonth.toString().padStart(2, '0')}_${dateTime.hour.toString().padStart(2, '0')}${dateTime.minute.toString().padStart(2, '0')}${dateTime.second.toString().padStart(2, '0')}.txt"
                                onExportDatabase("export_database", fileName)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Exportar BD",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        // Botón de limpiar
                        IconButton(
                            onClick = { DebugLogger.clearLogs() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar logs",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    // Panel de estadísticas
                    AnimatedVisibility(
                        visible = showStats,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        LogStatsPanel(
                            stats = logStats,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                    }
                    
                    // Logs list
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(logs) { log ->
                            DebugLogItem(log = log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugLogItem(log: DebugLogger.LogEntry) {
    val backgroundColor = when (log.level) {
        "ERROR" -> Color.Red.copy(alpha = 0.2f)
        "WARN" -> Color.Yellow.copy(alpha = 0.2f)
        "INFO" -> Color.Blue.copy(alpha = 0.2f)
        "DEBUG" -> Color.Green.copy(alpha = 0.2f)
        else -> Color.Gray.copy(alpha = 0.1f)
    }
    
    val textColor = when (log.level) {
        "ERROR" -> Color.Red
        "WARN" -> Color.Yellow
        "INFO" -> Color.Cyan
        "DEBUG" -> Color.Green
        else -> Color.White
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "[${log.level}]",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                
                Text(
                    text = log.timestamp,
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            Text(
                text = log.message,
                color = Color.White,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun LogStatsPanel(
    stats: DebugLogger.LogStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.DarkGray.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "📊 Estadísticas de Logs",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Total",
                    value = stats.totalLogs.toString(),
                    color = Color.White
                )
                
                StatItem(
                    label = "Errores",
                    value = stats.errorCount.toString(),
                    color = Color.Red
                )
                
                StatItem(
                    label = "Advertencias",
                    value = stats.warnCount.toString(),
                    color = Color.Yellow
                )
                
                StatItem(
                    label = "Info",
                    value = stats.infoCount.toString(),
                    color = Color.Cyan
                )
                
                StatItem(
                    label = "Debug",
                    value = stats.debugCount.toString(),
                    color = Color.Green
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 10.sp
        )
    }
}
