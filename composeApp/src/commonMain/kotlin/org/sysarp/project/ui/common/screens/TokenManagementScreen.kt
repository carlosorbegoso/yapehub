package org.sysarp.project.ui.common.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Token
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.PaymentCode
import org.sysarp.project.data.TokenPackage
import org.sysarp.project.data.TokenStatusResponse
import org.sysarp.project.service.billing.BillingService
import org.sysarp.project.ui.common.components.topbar.TopBarComponent
import org.sysarp.project.utils.formatCurrency

@Composable
fun TokenManagementScreen(
    billingService: BillingService,
    onNavigateBack: () -> Unit,
    onNavigateToPayment: (PaymentCode) -> Unit
) {
    var tokenStatus by remember { mutableStateOf<TokenStatusResponse?>(null) }
    var tokenPackages by remember { mutableStateOf<List<TokenPackage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showTokenPurchaseDialog by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        try {
            
            // Cargar estado de tokens
            val tokenResult = billingService.getCurrentTokenStatus()
            tokenResult.fold(
                onSuccess = { status ->
                    tokenStatus = status
                },
                onFailure = { error ->
                }
            )
            
            // Cargar paquetes de tokens
            val packagesResult = billingService.getAvailableTokenPackages()
            packagesResult.fold(
                onSuccess = { packages ->
                    tokenPackages = packages
                },
                onFailure = { error ->
                    tokenPackages = billingService.getAvailableTokenPackagesLocal()
                }
            )
        } catch (e: Exception) {
            tokenPackages = billingService.getAvailableTokenPackagesLocal()
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopBarComponent(
                title = "Gestión de Tokens",
                subtitle = "Compra tokens adicionales para tus operaciones",
                icon = Icons.Filled.Token,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showTokenPurchaseDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Comprar tokens"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Cargando información de tokens...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Estado actual de tokens
                    tokenStatus?.let { status ->
                        item {
                            TokenStatusOverviewCard(tokenStatus = status)
                        }
                    }
                    
                    // Paquetes disponibles
                    item {
                        Text(
                            text = "Paquetes Disponibles",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(tokenPackages) { tokenPackage ->
                        TokenPackageCard(
                            tokenPackage = tokenPackage,
                            onPurchase = { 
                                showTokenPurchaseDialog = true
                            }
                        )
                    }
                    
                    // Información adicional
                    item {
                        TokenInfoCard()
                    }
                }
            }
        }
    }
    
    // Dialog para comprar tokens
    if (showTokenPurchaseDialog) {
        TokenPurchaseDialog(
            billingService = billingService,
            onDismiss = { showTokenPurchaseDialog = false },
            onNavigateToPayment = { paymentCode ->
                showTokenPurchaseDialog = false
                showSuccessAnimation = true
                successMessage = "Código de pago generado exitosamente"
                onNavigateToPayment(paymentCode)
            }
        )
    }
    
    // Animación de éxito
    if (showSuccessAnimation) {
        SuccessAnimationOverlay(
            message = successMessage,
            onAnimationComplete = { showSuccessAnimation = false }
        )
    }
}

@Composable
private fun TokenStatusOverviewCard(tokenStatus: TokenStatusResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Estado Actual de Tokens",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Icon(
                    imageVector = Icons.Filled.Token,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TokenInfoItem(
                    label = "Disponibles",
                    value = "${tokenStatus.tokensAvailable}",
                    color = MaterialTheme.colorScheme.primary
                )
                TokenInfoItem(
                    label = "Usados",
                    value = "${tokenStatus.tokensUsed}",
                    color = MaterialTheme.colorScheme.secondary
                )
                TokenInfoItem(
                    label = "Total",
                    value = "${tokenStatus.tokensAvailable + tokenStatus.tokensUsed + tokenStatus.tokensPurchased}",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            // Barra de progreso
            val totalTokens = tokenStatus.tokensAvailable + tokenStatus.tokensUsed + tokenStatus.tokensPurchased
            LinearProgressIndicator(
                progress = { tokenStatus.tokensUsed.toFloat() / totalTokens.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun TokenInfoItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TokenPackageCard(
    tokenPackage: TokenPackage,
    onPurchase: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (tokenPackage.isPopular) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (tokenPackage.isPopular) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                
                Text(
                    text = tokenPackage.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "${tokenPackage.tokens} tokens",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (tokenPackage.discount > 0) {
                    Text(
                        text = formatCurrency(tokenPackage.price),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                
                Button(
                    onClick = onPurchase,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Comprar",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TokenInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Información sobre Tokens",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Text(
                text = "Los tokens se utilizan para realizar operaciones en la plataforma. Cada operación consume una cantidad específica de tokens según su complejidad.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "• Procesamiento de pagos: 1 token\n• Generación de QR: 1 token\n• Reportes básicos: 1 token\n• Reportes avanzados: 2 tokens",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
