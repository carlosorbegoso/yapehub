package org.sysarp.project.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.TransactionType
import org.sysarp.project.data.YapeTransaction
import org.sysarp.project.utils.formatCurrency

@Composable
fun NotificationCard(
    transaction: YapeTransaction,
    modifier: Modifier = Modifier
) {
    val animatedAmount by animateFloatAsState(
        targetValue = transaction.amount.toFloat(),
        animationSpec = tween(
            durationMillis = 1000,
            easing = EaseOutCubic
        ),
        label = "amount"
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (transaction.transactionType == TransactionType.RECEIVED) 
                MaterialTheme.colorScheme.primaryContainer
            else 
                MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de notificación
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                if (transaction.transactionType == TransactionType.RECEIVED) 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                else 
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                if (transaction.transactionType == TransactionType.RECEIVED) 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else 
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.transactionType == TransactionType.RECEIVED) 
                        Icons.Default.KeyboardArrowUp 
                    else 
                        Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (transaction.transactionType == TransactionType.RECEIVED) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Información de la transacción
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (transaction.transactionType == TransactionType.RECEIVED) 
                        "¡Pago recibido!" 
                    else 
                        "Pago enviado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.transactionType == TransactionType.RECEIVED) 
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else 
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Text(
                    text = formatCurrency(animatedAmount),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.transactionType == TransactionType.RECEIVED) 
                        MaterialTheme.colorScheme.primary
                    else 
                        MaterialTheme.colorScheme.secondary
                )
                
                if (transaction.senderName != null) {
                    Text(
                        text = "De: ${transaction.senderName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (transaction.transactionType == TransactionType.RECEIVED) 
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        else 
                            MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
                
                if (transaction.message != null) {
                    Text(
                        text = "\"${transaction.message}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (transaction.transactionType == TransactionType.RECEIVED) 
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else 
                            MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Indicador de estado
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (transaction.isProcessed) 
                            Color(0xFF4CAF50) 
                        else 
                            Color(0xFFFF9800)
                    )
            )
        }
    }
}

@Composable
fun NotificationBanner(
    message: String,
    type: NotificationType = NotificationType.INFO,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (type) {
                    NotificationType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
                    NotificationType.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                    NotificationType.ERROR -> MaterialTheme.colorScheme.errorContainer
                    NotificationType.INFO -> MaterialTheme.colorScheme.secondaryContainer
                }
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (type) {
                        NotificationType.SUCCESS -> Icons.Default.CheckCircle
                        NotificationType.WARNING -> Icons.Default.Warning
                        NotificationType.ERROR -> Icons.Default.Warning
                        NotificationType.INFO -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = when (type) {
                        NotificationType.SUCCESS -> MaterialTheme.colorScheme.primary
                        NotificationType.WARNING -> MaterialTheme.colorScheme.tertiary
                        NotificationType.ERROR -> MaterialTheme.colorScheme.error
                        NotificationType.INFO -> MaterialTheme.colorScheme.secondary
                    },
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = when (type) {
                        NotificationType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
                        NotificationType.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
                        NotificationType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                        NotificationType.INFO -> MaterialTheme.colorScheme.onSecondaryContainer
                    },
                    modifier = Modifier.weight(1f)
                )
                
                if (onDismiss != {}) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = when (type) {
                                NotificationType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
                                NotificationType.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
                                NotificationType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                                NotificationType.INFO -> MaterialTheme.colorScheme.onSecondaryContainer
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

enum class NotificationType {
    SUCCESS, WARNING, ERROR, INFO
}
