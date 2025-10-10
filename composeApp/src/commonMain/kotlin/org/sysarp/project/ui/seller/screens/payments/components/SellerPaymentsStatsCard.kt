package org.sysarp.project.ui.seller.screens.payments.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.sysarp.project.ui.common.screens.PaymentStatItem
import org.sysarp.project.utils.formatCurrency

/**
 * Tarjeta de estadísticas de pagos de vendedor
 */
@Composable
fun SellerPaymentsStatsCard(
    title: String,
    count: Int,
    totalAmount: Double,
    icon: ImageVector
) {
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
            PaymentStatItem(title, count.toString(), icon)
            PaymentStatItem("Total", formatCurrency(totalAmount), Icons.Filled.AttachMoney)
        }
    }
}
