package org.sysarp.project.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import org.sysarp.project.data.AffiliationCodeData
import org.sysarp.project.data.BranchInfo
import org.sysarp.project.service.auth.AuthService

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun GenerateAffiliationCodeDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onGenerate: (Int, Int, Int, String?) -> Unit,
    branches: List<BranchInfo> = emptyList(),
    isLoading: Boolean = false,
    generatedCode: AffiliationCodeData? = null,
    errorMessage: String? = null,
    authService: AuthService? = null
) {
    if (isVisible) {
        val coroutineScope = rememberCoroutineScope()
        var currentStep by remember { mutableStateOf(if (generatedCode == null) 0 else 1) }
        
        // Estados del formulario con valores por defecto mejorados
        var expirationHours by remember { mutableStateOf("24") } // 24 horas por defecto
        var maxUses by remember { mutableStateOf("5") } // 5 usos por defecto
        var selectedBranch by remember { mutableStateOf<BranchInfo?>(branches.firstOrNull()) }
        var notes by remember { mutableStateOf("") }
        var showBranchDropdown by remember { mutableStateOf(false) }
        
        // Estados de validación
        var expirationError by remember { mutableStateOf<String?>(null) }
        var maxUsesError by remember { mutableStateOf<String?>(null) }
        var branchError by remember { mutableStateOf<String?>(null) }
        
        // Validación en tiempo real
        LaunchedEffect(expirationHours) {
            expirationError = when {
                expirationHours.isBlank() -> "Campo requerido"
                expirationHours.toIntOrNull() == null -> "Debe ser un número"
                expirationHours.toInt() < 1 -> "Mínimo 1 hora"
                expirationHours.toInt() > 168 -> "Máximo 168 horas (7 días)"
                else -> null
            }
        }
        
        LaunchedEffect(maxUses) {
            maxUsesError = when {
                maxUses.isBlank() -> "Campo requerido"
                maxUses.toIntOrNull() == null -> "Debe ser un número"
                maxUses.toInt() < 1 -> "Mínimo 1 uso"
                maxUses.toInt() > 100 -> "Máximo 100 usos"
                else -> null
            }
        }
        
        LaunchedEffect(selectedBranch) {
            branchError = if (selectedBranch == null) "Debe seleccionar una sucursal" else null
        }
        
        // Actualizar step cuando se genera código
        LaunchedEffect(generatedCode) {
            if (generatedCode != null) {
                currentStep = 1
            }
        }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Header mejorado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.QrCode,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(8.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = "Código de Afiliación",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (currentStep == 0) "Configurar nuevo código" else "Código generado",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Indicador de pasos
                    StepIndicator(
                        currentStep = currentStep,
                        totalSteps = 2,
                        stepLabels = listOf("Configurar", "Resultado")
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Contenido animado
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            slideInHorizontally(
                                initialOffsetX = { if (targetState > initialState) 300 else -300 },
                                animationSpec = tween(300)
                            ) with slideOutHorizontally(
                                targetOffsetX = { if (targetState > initialState) -300 else 300 },
                                animationSpec = tween(300)
                            )
                        }
                    ) { step ->
                        when (step) {
                            0 -> ConfigurationStep(
                                branches = branches,
                                selectedBranch = selectedBranch,
                                onBranchSelected = { selectedBranch = it },
                                showBranchDropdown = showBranchDropdown,
                                onToggleBranchDropdown = { showBranchDropdown = !showBranchDropdown },
                                expirationHours = expirationHours,
                                onExpirationHoursChange = { expirationHours = it },
                                expirationError = expirationError,
                                maxUses = maxUses,
                                onMaxUsesChange = { maxUses = it },
                                maxUsesError = maxUsesError,
                                notes = notes,
                                onNotesChange = { notes = it },
                                errorMessage = errorMessage,
                                isLoading = isLoading,
                                onGenerate = {
                                    if (selectedBranch != null && expirationError == null && maxUsesError == null) {
                                        onGenerate(
                                            expirationHours.toInt(),
                                            maxUses.toInt(),
                                            selectedBranch!!.branchId,
                                            notes.ifBlank { null }
                                        )
                                    }
                                },
                                onCancel = onDismiss
                            )
                            
                            1 -> ResultStep(
                                generatedCode = generatedCode,
                                authService = authService,
                                onDismiss = onDismiss,
                                onGenerateAnother = { 
                                    currentStep = 0
                                    // Reset form
                                    expirationHours = "24"
                                    maxUses = "5"
                                    notes = ""
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    stepLabels: List<String>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Círculo del paso
                Card(
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(
                        containerColor = if (index <= currentStep) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (index < currentStep) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (index <= currentStep) 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Label del paso
                Text(
                    text = stepLabels.getOrNull(index) ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (index <= currentStep) 
                        MaterialTheme.colorScheme.onSurface 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (index == currentStep) FontWeight.Medium else FontWeight.Normal
                )
                
                // Línea conectora (excepto en el último paso)
                if (index < totalSteps - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Divider(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp),
                        color = if (index < currentStep) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfigurationStep(
    branches: List<BranchInfo>,
    selectedBranch: BranchInfo?,
    onBranchSelected: (BranchInfo) -> Unit,
    showBranchDropdown: Boolean,
    onToggleBranchDropdown: () -> Unit,
    expirationHours: String,
    onExpirationHoursChange: (String) -> Unit,
    expirationError: String?,
    maxUses: String,
    onMaxUsesChange: (String) -> Unit,
    maxUsesError: String?,
    notes: String,
    onNotesChange: (String) -> Unit,
    errorMessage: String?,
    isLoading: Boolean,
    onGenerate: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Selector de sucursal mejorado
        Column {
            Text(
                text = "Sucursal",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = selectedBranch?.name ?: "",
                onValueChange = { },
                placeholder = { Text("Seleccionar sucursal") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Business,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = onToggleBranchDropdown) {
                        Icon(
                            imageVector = if (showBranchDropdown) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null
                        )
                    }
                },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = selectedBranch == null,
                supportingText = if (selectedBranch == null) {
                    { Text("Debe seleccionar una sucursal") }
                } else null
            )
            
            // Dropdown mejorado
            AnimatedVisibility(
                visible = showBranchDropdown,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(branches) { branch ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        onBranchSelected(branch)
                                        onToggleBranchDropdown()
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Business,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = branch.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Código: ${branch.code}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                if (selectedBranch?.branchId == branch.branchId) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Configuración en fila
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Horas de expiración
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Horas de expiración",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = expirationHours,
                    onValueChange = onExpirationHoursChange,
                    placeholder = { Text("24") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = expirationError != null,
                    supportingText = expirationError?.let { { Text(it) } }
                )
            }
            
            // Máximo de usos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Máximo de usos",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = maxUses,
                    onValueChange = onMaxUsesChange,
                    placeholder = { Text("5") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.People,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = maxUsesError != null,
                    supportingText = maxUsesError?.let { { Text(it) } }
                )
            }
        }
        
        // Notas
        Column {
            Text(
                text = "Notas (opcional)",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                placeholder = { Text("Ej: Para nuevos vendedores del mes de octubre") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Note,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3,
                minLines = 2
            )
        }
        
        // Mensaje de error
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Botones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar")
            }
            
            Button(
                onClick = onGenerate,
                enabled = !isLoading && selectedBranch != null && expirationError == null && maxUsesError == null,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isLoading) "Generando..." else "Generar Código")
            }
        }
    }
}

@Composable
private fun ResultStep(
    generatedCode: AffiliationCodeData?,
    authService: AuthService?,
    onDismiss: () -> Unit,
    onGenerateAnother: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var showCopiedMessage by remember { mutableStateOf(false) }
    
    LaunchedEffect(showCopiedMessage) {
        if (showCopiedMessage) {
            kotlinx.coroutines.delay(2000)
            showCopiedMessage = false
        }
    }
    
    if (generatedCode != null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Icono de éxito
            Card(
                shape = RoundedCornerShape(50),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.size(80.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Text(
                text = "¡Código Generado Exitosamente!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            // Código con diseño mejorado
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Código de Afiliación",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = generatedCode.affiliationCode,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(generatedCode.affiliationCode))
                            showCopiedMessage = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (showCopiedMessage) "¡Copiado!" else "Copiar Código",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Información del código
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Expira: ${generatedCode.expiresAt}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.People,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Usos restantes: ${generatedCode.remainingUses}/${generatedCode.maxUses}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Botones finales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onGenerateAnother,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Generar Otro")
                }
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Finalizar")
                }
            }
        }
    }
}