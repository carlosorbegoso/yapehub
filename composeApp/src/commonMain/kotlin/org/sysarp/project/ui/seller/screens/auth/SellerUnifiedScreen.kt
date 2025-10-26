package org.sysarp.project.ui.seller.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.service.SellerService
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.service.websocket.PaymentWebSocketService
import org.sysarp.project.ui.common.components.LoadingHandler
import org.sysarp.project.ui.common.components.rememberLoadingState
import org.sysarp.project.ui.components.dashboard.DashboardAutoRefreshHandler
import org.sysarp.project.ui.components.seller_unified.actions.SellerFormActions
import org.sysarp.project.ui.components.seller_unified.animations.AnimatedForm
import org.sysarp.project.ui.components.seller_unified.animations.AnimatedHeader
import org.sysarp.project.ui.components.seller_unified.animations.AnimatedInfoCard
import org.sysarp.project.ui.components.seller_unified.animations.AnimatedLogo
import org.sysarp.project.ui.components.seller_unified.animations.AnimatedTitle
import org.sysarp.project.ui.components.seller_unified.animations.SellerFormAnimations
import org.sysarp.project.ui.components.seller_unified.fields.SellerFormFields
import org.sysarp.project.ui.components.seller_unified.validation.SellerFormValidation

@Composable
fun SellerUnifiedScreen(
    sellerService: SellerService,
    authService: AuthService,
    statsService: StatsService,
    webSocketService: PaymentWebSocketService,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    onNavigateToQRScanner: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Estados del formulario
    var affiliationCode by remember { mutableStateOf("") }
    var sellerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var showAdditionalFields by remember { mutableStateOf(false) }
    var isExistingSeller by remember { mutableStateOf(false) }
    
    // Estado de loading global
    val loadingState = rememberLoadingState()
    
    // Animación de escala del formulario
    val animatedScale = SellerFormAnimations.getFormScaleAnimation(isLoading || loadingState.isLoading)
    
    // Función para refrescar datos del vendedor
    val refreshSellerData: () -> Unit = {
        SellerFormActions.refreshSellerData(coroutineScope, statsService)
    }
    
    // Auto-refresh handler
    DashboardAutoRefreshHandler(
        authService = authService,
        statsService = statsService,
        webSocketService = webSocketService,
        onRefreshSellerDashboard = refreshSellerData
    )
    
    LoadingHandler(loadingState = loadingState) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // Header animado
        AnimatedHeader(onBackClick = onBackClick)
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Logo animado
        AnimatedLogo()
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Título animado
        AnimatedTitle()
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Formulario principal con animación
        AnimatedForm(animatedScale = animatedScale) {
            SellerFormFields(
                affiliationCode = affiliationCode,
                sellerName = sellerName,
                phone = phone,
                isLoading = isLoading,
                errorMessage = errorMessage,
                successMessage = successMessage,
                isFormValid = SellerFormValidation.isFormValid(affiliationCode, sellerName, phone),
                onAffiliationCodeChange = { newValue ->
                    SellerFormActions.handleFieldValueChange(
                        newValue = newValue,
                        onValueChange = { affiliationCode = it },
                        onErrorClear = { errorMessage = "" }
                    )
                },
                onSellerNameChange = { newValue ->
                    SellerFormActions.handleFieldValueChange(
                        newValue = newValue,
                        onValueChange = { sellerName = it },
                        onErrorClear = { errorMessage = "" }
                    )
                },
                onPhoneChange = { newValue ->
                    SellerFormActions.handleFieldValueChange(
                        newValue = newValue,
                        onValueChange = { phone = it },
                        onErrorClear = { errorMessage = "" }
                    )
                },
                onNavigateToQRScanner = onNavigateToQRScanner,
                onSubmit = {
                    // Sanitizar datos antes de enviar
                    val (sanitizedAffiliationCode, sanitizedSellerName, sanitizedPhone) = 
                        SellerFormActions.sanitizeFormData(affiliationCode, sellerName, phone)
                    
                    // Procesar acción del vendedor
                    SellerFormActions.processSellerAction(
                        coroutineScope = coroutineScope,
                        sellerService = sellerService,
                        authService = authService,
                        affiliationCode = sanitizedAffiliationCode,
                        sellerName = sanitizedSellerName,
                        phone = sanitizedPhone,
                        isExistingSeller = isExistingSeller,
                        onLoadingChange = { isLoading = it },
                        onError = { errorMessage = it },
                        onSuccess = { successMessage = it; onSuccess() }
                    )
                }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
            // Información adicional animada
            AnimatedInfoCard()
        }
    }
}