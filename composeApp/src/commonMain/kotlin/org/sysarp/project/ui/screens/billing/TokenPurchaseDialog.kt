package org.sysarp.project.ui.screens.billing

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sysarp.project.data.*
import org.sysarp.project.service.billing.BillingService
import org.sysarp.project.utils.Logger
import org.sysarp.project.utils.formatCurrency

@Composable
fun TokenPurchaseDialog(
    billingService: BillingService,
    onDismiss: () -> Unit,
    onNavigateToPayment: (PaymentCode) -> Unit
) {
    var selectedTokenPackage by remember { mutableStateOf<org.sysarp.project.data.TokenPackage?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var tokenPackages by remember { mutableStateOf<List<org.sysarp.project.data.TokenPackage>>(emptyList()) }
    var isLoadingPackages by remember { mutableStateOf(true) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Cargar paquetes de tokens desde la API
    LaunchedEffect(Unit) {
        try {
            Logger.auth("TOKEN_PURCHASE_DIALOG", "🪙 Cargando paquetes de tokens desde API")
            val result = billingService.getAvailableTokenPackages()
            result.fold(
                onSuccess = { packages ->
                    tokenPackages = packages
                    Logger.auth("TOKEN_PURCHASE_DIALOG", "✅ Paquetes cargados desde API: ${packages.size}")
                },
                onFailure = { error ->
                    Logger.auth("TOKEN_PURCHASE_DIALOG", "⚠️ Error cargando desde API, usando fallback: ${error.message}")
                    tokenPackages = billingService.getAvailableTokenPackagesLocal()
                }
            )
        } catch (e: Exception) {
            Logger.auth("TOKEN_PURCHASE_DIALOG", "❌ Error inesperado, usando fallback: ${e.message}")
            tokenPackages = billingService.getAvailableTokenPackagesLocal()
        } finally {
            isLoadingPackages = false
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Comprar Tokens",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Selecciona un paquete de tokens:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (isLoadingPackages) {
                    Box(
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Cargando paquetes...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.heightIn(max = 300.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(tokenPackages) { tokenPackage ->
                            TokenPackageCard(
                                tokenPackage = tokenPackage,
                                isSelected = selectedTokenPackage?.id == tokenPackage.id,
                                onSelect = { selectedTokenPackage = tokenPackage }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            var buttonPressed by remember { mutableStateOf(false) }
            val buttonScale by animateFloatAsState(
                targetValue = if (buttonPressed) 0.95f else 1f,
                animationSpec = tween(150),
                label = "buttonScale"
            )
            
            Button(
                onClick = {
                    selectedTokenPackage?.let { tokenPackage ->
                        buttonPressed = true
                        isLoading = true
                        
                        coroutineScope.launch {
                            try {
                                Logger.auth("TOKEN_PURCHASE", "💳 Generando pago para paquete: ${tokenPackage.name}")
                                
                                val paymentResult = billingService.generateTokenPurchasePayment(tokenPackage.tokens.toString())
                                paymentResult.fold(
                                    onSuccess = { paymentCode ->
                                        // Mostrar animación de éxito
                                        successMessage = "¡Tokens listos para comprar!"
                                        showSuccessAnimation = true
                                        
                                        // Navegar después de mostrar la animación
                                        coroutineScope.launch {
                                            delay(2000) // Mostrar animación por 2 segundos
                                            onNavigateToPayment(paymentCode)
                                            showSuccessAnimation = false
                                            onDismiss()
                                        }
                                        
                                        Logger.auth("TOKEN_PURCHASE", "✅ Código de pago generado: ${paymentCode.paymentCode}")
                                    },
                                    onFailure = { e ->
                                        Logger.auth("TOKEN_PURCHASE", "❌ Error generando pago: ${e.message}")
                                        isLoading = false
                                    }
                                )
                            } catch (e: Exception) {
                                Logger.auth("TOKEN_PURCHASE", "❌ Error inesperado: ${e.message}")
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = selectedTokenPackage != null && !isLoading,
                modifier = Modifier.scale(buttonScale),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "Generar Pago",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Cancelar")
            }
        }
    )
    
    // Animación de éxito
    if (showSuccessAnimation) {
        org.sysarp.project.ui.screens.billing.SuccessAnimationOverlay(
            message = successMessage,
            onAnimationComplete = { showSuccessAnimation = false }
        )
    }
}

@Composable
private fun TokenPackageCard(
    tokenPackage: org.sysarp.project.data.TokenPackage,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 2.dp,
        animationSpec = tween(150),
        label = "elevation"
    )
    
    // Animación suave para el color de fondo
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(300, easing = EaseOutCubic),
        label = "backgroundColor"
    )
    
    // Animación suave para la elevación
    val cardElevation by animateDpAsState(
        targetValue = if (isSelected) 4.dp else 2.dp,
        animationSpec = tween(300, easing = EaseOutCubic),
        label = "cardElevation"
    )
    
    // Animación suave para el color del borde
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.0f)
        },
        animationSpec = tween(300, easing = EaseOutCubic),
        label = "borderColor"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = cardElevation
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            borderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = tokenPackage.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (tokenPackage.isPopular) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Popular",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // Animación suave para el icono de selección
                AnimatedVisibility(
                    visible = isSelected,
                    enter = scaleIn(
                        animationSpec = tween(300, easing = EaseOutBack)
                    ) + fadeIn(animationSpec = tween(300)),
                    exit = scaleOut(
                        animationSpec = tween(200)
                    ) + fadeOut(animationSpec = tween(200))
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Text(
                text = tokenPackage.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${tokenPackage.tokens} tokens",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    if (tokenPackage.discount > 0) {
                        Text(
                            text = formatCurrency(tokenPackage.price),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.graphicsLayer {
                                alpha = 0.6f
                            }
                        )
                        Text(
                            text = formatCurrency(tokenPackage.discountedPrice),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = formatCurrency(tokenPackage.price),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // Mostrar características si existen
            if (tokenPackage.features.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Incluye:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    tokenPackage.features.take(3).forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = feature,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (tokenPackage.features.size > 3) {
                        Text(
                            text = "+${tokenPackage.features.size - 3} más",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}