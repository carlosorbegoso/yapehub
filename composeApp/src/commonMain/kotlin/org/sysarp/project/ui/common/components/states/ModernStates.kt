package org.sysarp.project.ui.common.components.states

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Estado de carga moderno con animación
 */
@Composable
fun ModernLoadingState(
    message: String = "Cargando pagos...",
    modifier: Modifier = Modifier
) {
    var animationValue by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        animationValue = 1f
    }
    
    val animatedScale by animateFloatAsState(
        targetValue = animationValue,
        animationSpec = tween(600),
        label = "loadingScale"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Estado vacío moderno con icono y mensaje
 */
@Composable
fun ModernEmptyState(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var animationValue by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        animationValue = 1f
    }
    
    val animatedScale by animateFloatAsState(
        targetValue = animationValue,
        animationSpec = tween(600),
        label = "emptyScale"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                actionText?.let { text ->
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Button(
                        onClick = { onAction?.invoke() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text)
                    }
                }
            }
        }
    }
}

/**
 * Estado de error moderno
 */
@Composable
fun ModernErrorState(
    title: String = "Error",
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernEmptyState(
        title = title,
        subtitle = message,
        icon = Icons.Filled.Warning,
        iconColor = MaterialTheme.colorScheme.error,
        actionText = "Reintentar",
        onAction = onRetry,
        modifier = modifier
    )
}

/**
 * Estados específicos para pagos
 */
@Composable
fun PaymentEmptyState(
    isPendingTab: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (title, subtitle, icon) = if (isPendingTab) {
        Triple(
            "No hay pagos pendientes",
            "Los pagos aparecerán aquí cuando los clientes realicen transacciones.",
            Icons.Filled.Schedule
        )
    } else {
        Triple(
            "No hay pagos confirmados",
            "Los pagos confirmados aparecerán aquí.",
            Icons.Filled.CheckCircle
        )
    }
    
    ModernEmptyState(
        title = title,
        subtitle = subtitle,
        icon = icon,
        actionText = "Actualizar",
        onAction = onRefresh,
        modifier = modifier
    )
}

/**
 * Estado de error para pagos
 */
@Composable
fun PaymentErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernErrorState(
        title = "Error al cargar pagos",
        message = message,
        onRetry = onRetry,
        modifier = modifier
    )
}
