package org.sysarp.project.ui.common.components.cards

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.utils.extractShortYapeCode
import org.sysarp.project.utils.formatCurrency

// ===== Constantes de Colores =====

private val STATUS_COLORS = mapOf(
    "PENDING" to Color(0xFFFF9800),   // Naranja
    "CLAIMED" to Color(0xFF4CAF50),   // Verde
    "REJECTED" to Color(0xFFF44336)   // Rojo
)

private val STATUS_NAMES = mapOf(
    "PENDING" to "Pendiente",
    "CLAIMED" to "Confirmado",
    "REJECTED" to "Rechazado"
)

/**
 * Tarjeta de pago moderna con animaciones
 * Muestra información del pago sin acciones
 */
@Composable
fun ModernPaymentCard(
    payment: SellerPendingPayment,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = getStatusColor(payment.status),
        animationSpec = tween(300),
        label = "statusColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            renderPaymentHeader(payment, statusColor)
            Spacer(modifier = Modifier.height(16.dp))
            renderSenderInfo(payment)
            Spacer(modifier = Modifier.height(8.dp))
            renderYapeCode(payment)
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            renderPaymentDate(payment)
        }
    }
}

/**
 * Tarjeta de pago con botones de acción (Confirmar/Rechazar)
 * Para uso en dashboards de vendedores
 */
@Composable
fun ModernPaymentCardWithActions(
    payment: SellerPendingPayment,
    onClaim: () -> Unit = {},
    onReject: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            ModernPaymentCard(
                payment = payment,
                onClick = { }
            )
            Spacer(modifier = Modifier.height(16.dp))
            renderActionButtons(onReject, onClaim)
        }
    }
}

// ===== Componentes Privados =====

/**
 * Renderiza el header con estado y monto
 */
@Composable
private fun renderPaymentHeader(
    payment: SellerPendingPayment,
    statusColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        renderStatusBadge(payment.status, statusColor)
        Text(
            text = formatCurrency(payment.amount),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Renderiza el badge de estado
 */
@Composable
private fun renderStatusBadge(status: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = getStatusDisplayName(status),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Renderiza información del remitente
 */
@Composable
private fun renderSenderInfo(payment: SellerPendingPayment) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = payment.senderName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Renderiza el código Yape
 */
@Composable
private fun renderYapeCode(payment: SellerPendingPayment) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Código: ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = extractShortYapeCode(payment.yapeCode),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Renderiza la fecha del pago
 */
@Composable
private fun renderPaymentDate(payment: SellerPendingPayment) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Schedule,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = payment.getDisplayDate(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Renderiza los botones de acción
 */
@Composable
private fun renderActionButtons(
    onReject: () -> Unit,
    onClaim: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onReject,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Rechazar")
        }

        Button(
            onClick = onClaim,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Confirmar")
        }
    }
}

// ===== Funciones Auxiliares =====

/**
 * Obtiene el color según el estado del pago
 */
private fun getStatusColor(status: String): Color {
    return STATUS_COLORS[status.uppercase()] ?: Color.Gray
}

/**
 * Obtiene el nombre de visualización del estado
 */
private fun getStatusDisplayName(status: String): String {
    return STATUS_NAMES[status.uppercase()] ?: status
}