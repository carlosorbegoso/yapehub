package org.sysarp.project.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Componente reutilizable para mostrar errores de manera elegante
 */
@Composable
fun ErrorAlertDialog(
    isVisible: Boolean,
    errorType: ErrorType,
    title: String,
    message: String,
    details: String? = null,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(
            animationSpec = tween(300, easing = EaseOutBack)
        ) + fadeIn(animationSpec = tween(300)),
        exit = scaleOut(
            animationSpec = tween(200)
        ) + fadeOut(animationSpec = tween(200)),
        modifier = modifier
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = errorType.icon,
                        contentDescription = null,
                        tint = errorType.getColor(),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                    
                    if (details != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = details,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                    
                    // Información adicional según el tipo de error
                    when (errorType) {
                        ErrorType.NETWORK -> {
                            Text(
                                text = "• Verifica tu conexión a internet\n• Intenta nuevamente en unos momentos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Start
                            )
                        }
                        ErrorType.SERVER -> {
                            Text(
                                text = "• El servidor está experimentando problemas\n• Intenta nuevamente más tarde",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Start
                            )
                        }
                        ErrorType.VALIDATION -> {
                            Text(
                                text = "• Revisa los datos ingresados\n• Completa todos los campos requeridos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Start
                            )
                        }
                        ErrorType.AUTHENTICATION -> {
                            Text(
                                text = "• Tu sesión ha expirado\n• Inicia sesión nuevamente",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Start
                            )
                        }
                        ErrorType.PERMISSION -> {
                            Text(
                                text = "• No tienes permisos para esta acción\n• Contacta al administrador",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Start
                            )
                        }
                        ErrorType.UNKNOWN -> {
                            Text(
                                text = "• Ha ocurrido un error inesperado\n• Intenta nuevamente o contacta soporte",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (onRetry != null) {
                    var buttonPressed by remember { mutableStateOf(false) }
                    val buttonScale by animateFloatAsState(
                        targetValue = if (buttonPressed) 0.95f else 1f,
                        animationSpec = tween(150),
                        label = "buttonScale"
                    )
                    
                    Button(
                        onClick = {
                            buttonPressed = true
                            onRetry()
                            onDismiss()
                        },
                        modifier = Modifier.scale(buttonScale),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = errorType.getColor()
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reintentar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}

/**
 * Tipos de errores con sus respectivos iconos y colores
 */
enum class ErrorType(
    val icon: ImageVector,
    val httpCodes: List<Int>
) {
    NETWORK(
        icon = Icons.Filled.WifiOff,
        httpCodes = listOf(0, -1) // Sin conexión, timeout
    ),
    SERVER(
        icon = Icons.Filled.Error,
        httpCodes = listOf(500, 502, 503, 504)
    ),
    VALIDATION(
        icon = Icons.Filled.Warning,
        httpCodes = listOf(400, 422)
    ),
    AUTHENTICATION(
        icon = Icons.Filled.Lock,
        httpCodes = listOf(401, 403)
    ),
    PERMISSION(
        icon = Icons.Filled.Block,
        httpCodes = listOf(403)
    ),
    UNKNOWN(
        icon = Icons.Filled.Help,
        httpCodes = listOf()
    );
    
    @Composable
    fun getColor(): Color {
        return when (this) {
            NETWORK -> MaterialTheme.colorScheme.error
            SERVER -> MaterialTheme.colorScheme.error
            VALIDATION -> MaterialTheme.colorScheme.tertiary
            AUTHENTICATION -> MaterialTheme.colorScheme.secondary
            PERMISSION -> MaterialTheme.colorScheme.secondary
            UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    }
    
    companion object {
        fun fromHttpCode(code: Int): ErrorType {
            return values().find { it.httpCodes.contains(code) } ?: UNKNOWN
        }
    }
}

/**
 * Componente flotante para errores no críticos
 */
@Composable
fun ErrorSnackbar(
    isVisible: Boolean,
    errorType: ErrorType,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300, easing = EaseOutCubic)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(200)
        ) + fadeOut(animationSpec = tween(200)),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = errorType.getColor().copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = errorType.icon,
                    contentDescription = null,
                    tint = errorType.getColor(),
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
