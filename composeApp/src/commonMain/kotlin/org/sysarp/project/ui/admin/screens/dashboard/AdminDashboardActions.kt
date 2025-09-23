package org.sysarp.project.ui.admin.screens.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.ui.common.screens.ServerQRDisplayScreen
import org.sysarp.project.ui.common.screens.exportLogs
import org.sysarp.project.ui.components.GenerateAffiliationCodeDialog
import org.sysarp.project.ui.components.dashboard.DashboardAutoRefreshHandler

/**
 * Acciones y handlers para AdminDashboardScreen
 */

@Composable
fun AdminDashboardActions(
    authService: AuthService,
    state: AdminDashboardState,
    userProfile: org.sysarp.project.data.UserProfile?,
    accessToken: String?,
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Manejo de sesión y token refresh
    LaunchedEffect(Unit) {
        authService.updateActivity()
        if (!authService.isSessionValid()) {
            authService.logout()
            onLogout()
        }
    }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(120_000) // 2 minutos
            try {
                val refreshSuccess = authService.checkAndRefreshTokenIfNeeded()
                if (!refreshSuccess) {
                    if (!authService.isSessionValid()) {
                        authService.logout()
                        onLogout()
                        break
                    }
                }
            } catch (e: Exception) {
                // Error silencioso en refresh
            }
        }
    }
    
    // Cargar datos iniciales
    LaunchedEffect(userProfile?.adminId, accessToken) {
        if (userProfile?.adminId != null && accessToken != null) {
            val adminId = userProfile.adminId!!.toInt()
            
            // Cargar estadísticas rápidas
            state.loadQuickStats(adminId, accessToken)
            
            // Cargar estadísticas completas del admin
            state.loadAdminStats(adminId, accessToken)
            
            // Cargar vendedores conectados
            state.loadConnectedSellers(adminId, accessToken)
        }
    }
    
    // Cargar sucursales cuando se muestra el diálogo de afiliación
    LaunchedEffect(state.showAffiliationDialog, userProfile?.adminId, accessToken) {
        if (state.showAffiliationDialog && userProfile?.adminId != null && accessToken != null) {
            state.loadBranches(userProfile.adminId!!.toInt(), accessToken)
        }
    }
    
    // Cargar solicitudes de baja pendientes
    LaunchedEffect(Unit) {
        state.loadPendingRequests()
    }
    
    // Integrar actualización automática
    DashboardAutoRefreshHandler(
        authService = authService,
        statsService = state.statsService,
        webSocketService = state.webSocketService,
        onRefreshAdminDashboard = {
            if (userProfile?.adminId != null && accessToken != null) {
                state.refreshAdminStats(userProfile.adminId!!.toInt(), accessToken)
            }
        }
    )
    
    // Diálogo para generar códigos de afiliación
    GenerateAffiliationCodeDialog(
        isVisible = state.showAffiliationDialog,
        onDismiss = { state.dismissAffiliationDialog() },
        onGenerate = { expirationHours, maxUses, branchId, notes ->
            if (userProfile?.adminId != null && accessToken != null) {
                state.generateAffiliationCode(
                    adminId = userProfile.adminId!!.toInt(),
                    branchId = branchId,
                    expirationHours = expirationHours,
                    maxUses = maxUses,
                    notes = notes ?: "",
                    accessToken = accessToken
                )
            }
        },
        branches = state.branches,
        isLoading = state.isLoadingAffiliation,
        generatedCode = state.generatedAffiliationCode?.data,
        errorMessage = state.affiliationError,
        onGenerateQR = { affiliationCode ->
            if (accessToken != null) {
                state.generateQRFromAffiliationCode(affiliationCode, accessToken)
            }
        },
        isLoadingQR = state.isLoadingQR,
        qrError = state.qrError,
        authService = authService
    )
    
    // Diálogo para mostrar QR generado
    if (state.showQRDialog && state.generatedQRCode != null) {
        ServerQRDisplayScreen(
            qrData = state.generatedQRCode!!,
            onNavigateBack = { state.dismissQRDialog() },
            onShareQR = { 
                // Implementar compartir QR
                if (state.generatedQRCode != null) {
                    val qrData = state.generatedQRCode!!
                    val shareText = buildString {
                        appendLine("🔗 Código QR de Afiliación - YapeHub")
                        appendLine()
                        appendLine("📱 Código de Afiliación: ${qrData.affiliationCode}")
                        appendLine("🏢 Sucursal: ${qrData.branchName}")
                        appendLine("👤 Administrador: ${qrData.adminName}")
                        appendLine("⏰ Válido hasta: ${qrData.expiresAt}")
                        appendLine("🔢 Usos restantes: ${qrData.remainingUses}/${qrData.maxUses}")
                        appendLine()
                        appendLine("📲 Escanea este QR para afiliarte como vendedor")
                        appendLine("💡 Generado con YapeHub")
                    }
                    
                    // Usar el servicio de compartir existente
                    exportLogs(shareText)
                }
            },
            onInvalidateQR = { 
                // Implementar invalidar QR
                if (state.generatedQRCode != null) {
                    val qrData = state.generatedQRCode!!
                    
                    // Mostrar confirmación de invalidación
                    
                    // QR invalidado localmente
                }
                
                // Cerrar diálogo y limpiar estado
                state.dismissQRDialog()
            }
        )
    }
}

/**
 * Crea el menú de la top bar
 */
@Composable
fun createTopBarMenuItems(
    onShowAffiliationDialog: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
): List<org.sysarp.project.ui.common.components.topbar.TopBarMenuItem> {
    return listOf(
        org.sysarp.project.ui.common.components.topbar.TopBarMenuItem(
            title = "Generar código de afiliación",
            icon = Icons.Filled.QrCode,
            onClick = onShowAffiliationDialog
        ),
        org.sysarp.project.ui.common.components.topbar.TopBarMenuItem(
            title = "Mi Perfil",
            icon = Icons.Filled.Person,
            onClick = onNavigateToProfile
        ),
        org.sysarp.project.ui.common.components.topbar.TopBarMenuItem(
            title = "Configuración",
            icon = Icons.Filled.Settings,
            onClick = onNavigateToSettings
        ),
        org.sysarp.project.ui.common.components.topbar.TopBarMenuItem(
            title = "Cerrar sesión",
            icon = Icons.Filled.Logout,
            onClick = onLogout,
            iconColor = MaterialTheme.colorScheme.error
        )
    )
}

/**
 * Handler para logout
 */
fun handleLogout(
    authService: AuthService,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onLogout: () -> Unit
) {
    coroutineScope.launch {
        authService.logout()
        onLogout()
    }
}
