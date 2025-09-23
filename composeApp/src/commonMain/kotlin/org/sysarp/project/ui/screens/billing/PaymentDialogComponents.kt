package org.sysarp.project.ui.screens.billing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.sysarp.project.data.PaymentCode
import org.sysarp.project.data.PaymentStatus
import org.sysarp.project.service.ImagePickerService

/**
 * Componentes UI para PaymentDialog
 */

@Composable
fun PaymentDialogContainer(
    currentStep: PaymentStep,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
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
            content()
        }
    }
}

@Composable
fun PaymentInfoStep(
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
        PaymentDialogHeader(onDismiss = onDismiss)
        
        // Información del pago
        PaymentInfoCard(paymentCode = paymentCode)
        
        // Instrucciones simplificadas
        PaymentInstructionsCard(paymentCode = paymentCode)
        
        // Estado del pago
        PaymentStatusCard(
            isCheckingStatus = isCheckingStatus,
            paymentStatus = paymentStatus
        )
        
        // Sección de carga de imagen
        PaymentImageUploadCard(
            selectedImageBase64 = selectedImageBase64,
            selectedImageName = selectedImageName,
            notes = notes,
            isLoading = isLoading,
            onImageSelected = onImageSelected,
            onShowImagePicker = onShowImagePicker,
            onNotesChange = onNotesChange,
            onUploadProof = onUploadProof
        )
        
        // Error
        if (error.isNotEmpty()) {
            PaymentErrorCard(error = error)
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
fun PaymentDialogHeader(onDismiss: () -> Unit) {
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
}

@Composable
fun PaymentInfoCard(paymentCode: PaymentCode) {
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
}

@Composable
fun PaymentInstructionsCard(paymentCode: PaymentCode) {
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
}

@Composable
fun PaymentStatusCard(
    isCheckingStatus: Boolean,
    paymentStatus: PaymentStatus?
) {
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
}

@Composable
fun PaymentImageUploadCard(
    selectedImageBase64: String,
    selectedImageName: String,
    notes: String,
    isLoading: Boolean,
    onImageSelected: (String, String) -> Unit,
    onShowImagePicker: () -> Unit,
    onNotesChange: (String) -> Unit,
    onUploadProof: () -> Unit
) {
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
}

@Composable
fun PaymentErrorCard(error: String) {
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

@Composable
fun UploadSuccessStep(
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
fun PaymentSuccessStep(
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
fun PaymentErrorStep(
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
fun ImagePickerDialog(
    imagePickerService: ImagePickerService,
    onImageSelected: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
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
                            // La lógica de selección se maneja en el estado
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
                            // La lógica de captura se maneja en el estado
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
