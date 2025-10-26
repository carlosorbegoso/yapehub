package org.sysarp.project.ui.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Overlay de loading global para mostrar cuando las APIs están cargando
 */
@Composable
fun LoadingOverlay(
    isVisible: Boolean,
    message: String = "Cargando...",
    modifier: Modifier = Modifier,
    canDismiss: Boolean = false,
    onDismiss: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(
            initialScale = 0.8f,
            animationSpec = spring(
                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
            )
        ),
        exit = fadeOut(animationSpec = tween(200)) + scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(200)
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (canDismiss) onDismiss()
                },
            contentAlignment = Alignment.Center
        ) {
            LoadingCard(message = message)
        }
    }
}

/**
 * Card de loading con animación
 */
@Composable
private fun LoadingCard(
    message: String,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500),
        label = "cardAlpha"
    )
    
    Card(
        modifier = modifier
            .alpha(alpha)
            .padding(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            
            Text(
                text = message,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Loading inline para usar dentro de componentes
 */
@Composable
fun InlineLoading(
    message: String = "Cargando...",
    modifier: Modifier = Modifier,
    size: LoadingSize = LoadingSize.Medium
) {
    val indicatorSize = when (size) {
        LoadingSize.Small -> 20.dp
        LoadingSize.Medium -> 32.dp
        LoadingSize.Large -> 48.dp
    }
    
    val fontSize = when (size) {
        LoadingSize.Small -> 12.sp
        LoadingSize.Medium -> 14.sp
        LoadingSize.Large -> 16.sp
    }
    
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(indicatorSize),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp
        )
        
        Text(
            text = message,
            fontSize = fontSize,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Loading para botones
 */
@Composable
fun ButtonLoading(
    text: String = "Cargando...",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
        )
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

/**
 * Loading minimalista para espacios pequeños
 */
@Composable
fun MinimalLoading(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = color,
            strokeWidth = 2.dp
        )
    }
}

/**
 * Tamaños disponibles para el loading
 */
enum class LoadingSize {
    Small, Medium, Large
}

/**
 * Mensajes predefinidos para diferentes operaciones
 */
object LoadingMessages {
    const val LOADING = "Cargando..."
    const val SAVING = "Guardando..."
    const val DELETING = "Eliminando..."
    const val UPDATING = "Actualizando..."
    const val REGISTERING = "Registrando..."
    const val LOGIN = "Iniciando sesión..."
    const val LOGOUT = "Cerrando sesión..."
    const val SENDING = "Enviando..."
    const val PROCESSING = "Procesando..."
    const val VALIDATING = "Validando..."
    const val CONNECTING = "Conectando..."
    const val SYNCING = "Sincronizando..."
    const val UPLOADING = "Subiendo archivo..."
    const val DOWNLOADING = "Descargando..."
    const val GENERATING = "Generando..."
    const val ANALYZING = "Analizando..."
    const val CALCULATING = "Calculando..."
}