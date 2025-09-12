package org.sysarp.project.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat

@Composable
fun TransactionStatsCard(
    title: String,
    amount: Double,
    icon: ImageVector? = null,
    isCount: Boolean = false,
    isPositive: Boolean? = null,
    isWarning: Boolean = false,
    modifier: Modifier = Modifier
) {
    val animatedAmount by animateFloatAsState(
        targetValue = amount.toFloat(),
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "amount"
    )
    
    val cardColor = when {
        isWarning -> MaterialTheme.colorScheme.errorContainer
        isPositive == true -> MaterialTheme.colorScheme.primaryContainer
        isPositive == false -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = when {
        isWarning -> MaterialTheme.colorScheme.onErrorContainer
        isPositive == true -> MaterialTheme.colorScheme.onPrimaryContainer
        isPositive == false -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val defaultIcon = when {
        isWarning -> Icons.Default.Warning
        isPositive == true -> Icons.Default.KeyboardArrowUp
        isPositive == false -> Icons.Default.KeyboardArrowDown
        else -> Icons.Default.Info
    }
    
    val finalIcon = icon ?: defaultIcon
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            cardColor,
                            cardColor.copy(alpha = 0.9f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(textColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = finalIcon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = textColor
                        )
                    }
                }
                
                Column {
                    Text(
                        text = if (isCount) {
                            animatedAmount.toInt().toString()
                        } else {
                            "S/ ${DecimalFormat("#,##0.00").format(animatedAmount)}"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = if (isCount) 28.sp else 24.sp
                    )
                    
                    if (!isCount) {
                        Text(
                            text = "PEN",
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
