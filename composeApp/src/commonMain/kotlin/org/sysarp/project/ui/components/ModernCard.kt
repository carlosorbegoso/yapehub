package org.sysarp.project.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun ModernCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    colors: CardColors = CardDefaults.cardColors(),
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    Card(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.05f)
            ),
        shape = shape,
        colors = colors,
        elevation = elevation
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.03f),
                            Color.Transparent
                        ),
                        start = androidx.compose.ui.geometry.Offset(
                            x = shimmerOffset * 1000f,
                            y = 0f
                        ),
                        end = androidx.compose.ui.geometry.Offset(
                            x = shimmerOffset * 1000f + 200f,
                            y = 0f
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                content = content
            )
        }
    }
}

