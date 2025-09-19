package org.sysarp.project.ui.screens.billing

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sysarp.project.data.*
import org.sysarp.project.service.billing.BillingService
import org.sysarp.project.service.ImagePickerService
import org.sysarp.project.utils.Logger
import java.util.Base64

@Composable
fun PaymentDialog(
    paymentCode: PaymentCode,
    billingService: BillingService,
    onDismiss: () -> Unit,
    onPaymentCompleted: () -> Unit
) {
    var currentStep by remember { mutableStateOf(PaymentStep.PAYMENT_INFO) }
    var selectedImageBase64 by remember { mutableStateOf("") }
    var selectedImageName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var paymentStatus by remember { mutableStateOf<PaymentStatus?>(null) }
    var isCheckingStatus by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val imagePickerService = remember { ImagePickerService() }
    
    // Verificar estado del pago solo cuando el usuario lo solicite
    // Eliminamos la verificación automática cada 5 segundos para mejorar el rendimiento
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = currentStep == PaymentStep.PAYMENT_INFO,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            when (currentStep) {
                PaymentStep.PAYMENT_INFO -> {
                    PaymentInfoStep(
                        paymentCode = paymentCode,
                        selectedImageBase64 = selectedImageBase64,
                        selectedImageName = selectedImageName,
                        notes = notes,
                        isLoading = isLoading,
                        error = error,
                        isCheckingStatus = isCheckingStatus,
                        paymentStatus = paymentStatus,
                        showImagePicker = showImagePicker,
                        imagePickerService = imagePickerService,
                        onImageSelected = { base64, name ->
                            selectedImageBase64 = base64
                            selectedImageName = name
                            showImagePicker = false
                        },
                        onShowImagePicker = { showImagePicker = true },
                        onDismissImagePicker = { showImagePicker = false },
                        onNotesChange = { notes = it },
                        onUploadProof = {
                            coroutineScope.launch {
                                try {
                                    isLoading = true
                                    error = ""
                                    
                                    Logger.auth("PAYMENT_DIALOG", "📸 Subiendo comprobante: ${paymentCode.paymentCode}")
                                    
                                    val uploadResult = billingService.uploadPaymentProof(
                                        paymentCode.paymentCode,
                                        selectedImageBase64,
                                        notes.takeIf { it.isNotEmpty() }
                                    )
                                    
                                    uploadResult.fold(
                                        onSuccess = {
                                            currentStep = PaymentStep.UPLOAD_SUCCESS
                                            Logger.auth("PAYMENT_DIALOG", "✅ Comprobante subido exitosamente")
                                        },
                                        onFailure = { e ->
                                            error = e.message ?: "Error subiendo comprobante"
                                            Logger.auth("PAYMENT_DIALOG", "❌ Error subiendo comprobante: ${e.message}")
                                        }
                                    )
                                } catch (e: Exception) {
                                    error = e.message ?: "Error inesperado"
                                    Logger.auth("PAYMENT_DIALOG", "❌ Error inesperado: ${e.message}")
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        onDismiss = onDismiss
                    )
                }
                
                PaymentStep.UPLOAD_SUCCESS -> {
                    UploadSuccessStep(
                        onDismiss = onDismiss,
                        isCheckingStatus = isCheckingStatus,
                        onCheckStatus = {
                            coroutineScope.launch {
                                isCheckingStatus = true
                                try {
                                    Logger.auth("PAYMENT_DIALOG", "🔍 Verificando estado manualmente...")
                                    val statusResult = billingService.checkPaymentStatus(paymentCode.paymentCode)
                                    statusResult.fold(
                                        onSuccess = { status ->
                                            paymentStatus = status
                                            Logger.auth("PAYMENT_DIALOG", "✅ Estado verificado: ${status.status}")
                                            if (status.status == "approved") {
                                                currentStep = PaymentStep.SUCCESS
                                                onPaymentCompleted()
                                            } else if (status.status == "rejected") {
                                                currentStep = PaymentStep.ERROR
                                                error = status.message
                                            }
                                        },
                                        onFailure = { e ->
                                            Logger.auth("PAYMENT_DIALOG", "❌ Error verificando estado: ${e.message}")
                                            error = e.message ?: "Error verificando estado"
                                        }
                                    )
                                } catch (e: Exception) {
                                    Logger.auth("PAYMENT_DIALOG", "❌ Error inesperado verificando estado: ${e.message}")
                                    error = e.message ?: "Error inesperado"
                                } finally {
                                    isCheckingStatus = false
                                }
                            }
                        }
                    )
                }
                
                PaymentStep.SUCCESS -> {
                    PaymentSuccessStep(
                        paymentCode = paymentCode,
                        onDismiss = onDismiss
                    )
                }
                
                PaymentStep.ERROR -> {
                    PaymentErrorStep(
                        error = error,
                        onDismiss = onDismiss,
                        onRetry = { currentStep = PaymentStep.PAYMENT_INFO }
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentInfoStep(
    paymentCode: PaymentCode,
    selectedImageBase64: String,
    selectedImageName: String,
    notes: String,
    isLoading: Boolean,
    error: String,
    isCheckingStatus: Boolean,
    paymentStatus: PaymentStatus?,
    showImagePicker: Boolean,
    imagePickerService: ImagePickerService,
    onImageSelected: (String, String) -> Unit,
    onShowImagePicker: () -> Unit,
    onDismissImagePicker: () -> Unit,
    onNotesChange: (String) -> Unit,
    onUploadProof: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header elegante con icono
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Payment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Realizar Pago",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Cerrar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Información del pago - Diseño limpio sin bordes
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(16.dp)
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Información del Pago",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Grid de información
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Código",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = paymentCode.paymentCode,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Monto",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (paymentCode.amount == 0.0) "Gratis" else "S/ ${paymentCode.amount}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Yape",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = paymentCode.yapeNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Expira",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = paymentCode.expiresAt.take(16),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        // Instrucciones simplificadas
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    RoundedCornerShape(16.dp)
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📱 Pasos para pagar:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            val steps = listOf(
                "Abre tu app Yape",
                "Escanea QR o ingresa: ${paymentCode.yapeNumber}",
                "Monto: S/ ${paymentCode.amount}",
                "Toma captura de pantalla",
                "Sube la imagen aquí"
            )
            
            steps.forEachIndexed { index, step ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Estado del pago
        if (isCheckingStatus) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Verificando estado del pago...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        paymentStatus?.let { status ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        when (status.status) {
                            "approved" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            "rejected" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        },
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = when (status.status) {
                        "approved" -> Icons.Filled.CheckCircle
                        "rejected" -> Icons.Filled.Cancel
                        else -> Icons.Filled.Schedule
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = when (status.status) {
                        "approved" -> MaterialTheme.colorScheme.primary
                        "rejected" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = when (status.status) {
                        "approved" -> "Pago aprobado"
                        "rejected" -> "Pago rechazado"
                        else -> "Pago pendiente"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (status.status) {
                        "approved" -> MaterialTheme.colorScheme.primary
                        "rejected" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
        
        // Sección de carga de imagen - Diseño moderno
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    RoundedCornerShape(16.dp)
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Subir Comprobante",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Selector de imagen
            if (selectedImageBase64.isEmpty()) {
                // Botón para seleccionar imagen
                Button(
                    onClick = onShowImagePicker,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Seleccionar Imagen",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Imagen seleccionada
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Imagen seleccionada:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(onClick = { 
                            // Limpiar imagen seleccionada
                            onImageSelected("", "")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Eliminar",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    
                    Text(
                        text = selectedImageName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Vista previa de la imagen (si es posible)
                    Text(
                        text = "📸 Imagen lista para subir",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Campo de notas
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Notas adicionales (opcional)") },
                placeholder = { Text("Información adicional sobre el pago") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )
            
            // Botón de subida
            Button(
                onClick = onUploadProof,
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedImageBase64.isNotEmpty() && !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Subiendo...",
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Subir Comprobante",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Error
        if (error.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
    
    // Dialog para seleccionar imagen
    if (showImagePicker) {
        ImagePickerDialog(
            imagePickerService = imagePickerService,
            onImageSelected = onImageSelected,
            onDismiss = onDismissImagePicker
        )
    }
}

@Composable
private fun UploadSuccessStep(
    onDismiss: () -> Unit,
    onCheckStatus: () -> Unit,
    isCheckingStatus: Boolean = false
) {
    Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Comprobante Subido",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "Tu comprobante ha sido enviado exitosamente. Nuestro equipo lo revisará y activará tu suscripción o tokens en breve.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Cerrar",
                    fontWeight = FontWeight.Bold
                )
            }
            
            Button(
                onClick = onCheckStatus,
                modifier = Modifier.weight(1f),
                enabled = !isCheckingStatus,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isCheckingStatus) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Verificando...",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Verificar Estado",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentSuccessStep(
    paymentCode: PaymentCode,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "¡Pago Aprobado!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "Tu pago ha sido procesado exitosamente. Tu suscripción o tokens han sido activados.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Continuar",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PaymentErrorStep(
    error: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = "Error en el Pago",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Cerrar",
                    fontWeight = FontWeight.Bold
                )
            }
            
            Button(
                onClick = onRetry,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Reintentar",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ImagePickerDialog(
    imagePickerService: ImagePickerService,
    onImageSelected: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Seleccionar Imagen",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Selecciona una imagen de tu galería o toma una foto nueva",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón Galería
                    Button(
                        onClick = {
                            // Usar el servicio real de selección de imágenes
                            coroutineScope.launch {
                                try {
                                    val result = imagePickerService.selectImageFromGallery()
                                    result.fold(
                                        onSuccess = { imageResult ->
                                            onImageSelected(imageResult.base64, imageResult.fileName)
                                            Logger.auth("IMAGE_PICKER", "✅ Imagen seleccionada: ${imageResult.fileName}")
                                        },
                                        onFailure = { e ->
                                            Logger.auth("IMAGE_PICKER", "❌ Error seleccionando imagen: ${e.message}")
                                        }
                                    )
                                } catch (e: Exception) {
                                    Logger.auth("IMAGE_PICKER", "❌ Error inesperado: ${e.message}")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Galería")
                    }
                    
                    // Botón Cámara
                    Button(
                        onClick = {
                            // Usar el servicio real de captura de cámara
                            coroutineScope.launch {
                                try {
                                    val result = imagePickerService.capturePhotoFromCamera()
                                    result.fold(
                                        onSuccess = { imageResult ->
                                            onImageSelected(imageResult.base64, imageResult.fileName)
                                            Logger.auth("IMAGE_PICKER", "✅ Foto capturada: ${imageResult.fileName}")
                                        },
                                        onFailure = { e ->
                                            Logger.auth("IMAGE_PICKER", "❌ Error capturando foto: ${e.message}")
                                        }
                                    )
                                } catch (e: Exception) {
                                    Logger.auth("IMAGE_PICKER", "❌ Error inesperado: ${e.message}")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cámara")
                    }
                }
                
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Cancelar",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private enum class PaymentStep {
    PAYMENT_INFO,
    UPLOAD_SUCCESS,
    SUCCESS,
    ERROR
}
